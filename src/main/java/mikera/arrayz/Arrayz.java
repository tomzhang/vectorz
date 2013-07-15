package mikera.arrayz;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;

import mikera.matrixx.Matrix;
import mikera.matrixx.Matrixx;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ArrayIndexScalar;
import mikera.vectorz.impl.ArraySubVector;
import mikera.vectorz.impl.DoubleScalar;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.util.VectorzException;

/**
 * Static function class for array operations
 * 
 * @author Mike
 */
public class Arrayz {
	/**
	 * Creates an array from the given data
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static INDArray create(Object object) {
		if (object instanceof INDArray) return ((INDArray)object).clone();
		
		if (object instanceof double[]) return Vector.of((double[])object);
		if (object instanceof List<?>) {
			List<?> list=(List<Object>) object;
			if (list.size()==0) return Vector0.INSTANCE;
			Object o1=list.get(0);
			if ((o1 instanceof AScalar)||(o1 instanceof Number)) {
				return Vectorz.create((List<Object>)object);
			} else if (o1 instanceof AVector) {
				return Matrixx.create((List<Object>)object);
			} else if (o1 instanceof INDArray) {
				return SliceArray.create((List<INDArray>)object);				
			} else {
				ArrayList<INDArray> al=new ArrayList<INDArray>();
				for (Object o: list) {
					al.add(create(o));
				}
				return Arrayz.create(al);
			}
		}
		
		if (object instanceof Number) return DoubleScalar.create(((Number)object).doubleValue());
		
		throw new VectorzException("Don't know how to create array from: "+object.getClass());
	}
	
	public static INDArray newArray(int... shape) {
		int dims=shape.length;
		
		switch (dims) {
			case 0: return DoubleScalar.create(0.0);
			case 1: return Vector.createLength(shape[0]);
			case 2: return Matrix.create(shape[0], shape[1]);
			default: return NDArray.newArray(shape);
		}
	}
	
	public static INDArray create(Object... data) {
		int n=data.length;
		INDArray[] as=new INDArray[n];
		for (int i=0; i<n; i++) {
			as[i]=create((Object)data);
		}
		return SliceArray.create(as);
	}
	
	public static INDArray wrap(double[] data, int[] shape) {
		switch (shape.length) {
			case 0:
				return ArrayIndexScalar.wrap(data,0);
				
			case 1:
				int n=shape[0];
				if (n==data.length) {
					return Vector.wrap(data); 
				} else {
					return ArraySubVector.wrap(data, 0, n);
				}
				
			case 2:
				int rc=shape[0], cc=shape[1];
				if (rc*cc==data.length) {
					return Matrix.wrap(rc,cc, data);
				} else {
					return NDArray.wrap(data, shape);
				}
		
			default:
				return NDArray.wrap(data, shape);
		}
	}

	public static INDArray createFromVector(AVector a, int[] shape) {
		int dims=shape.length;
		if (dims==0) {
			return DoubleScalar.create(a.get(0));
		} else if (dims==1) {
			return a.subVector(0, shape[0]);
		} else if (dims==2) {
			return Matrixx.createFromVector(a, shape[0], shape[1]);
		} else {
			int n=shape[0];
			int[] ss=Arrays.copyOfRange(shape, 1, dims);
			int skip=(int)Arrayz.elementCount(ss);
			ArrayList<INDArray> al=new ArrayList<INDArray>();
			for (int i=0; i<n; i++) {
				al.add(createFromVector(a.subVector(i*skip, skip),ss));
			}
			return SliceArray.create(al);
		}
	}
	
	public static INDArray load(Reader reader) {
		Parseable pbr=Parsers.newParseable(reader);
		Parser p = Parsers.newParser(Parsers.defaultConfiguration());
		return Arrayz.create(p.nextValue(pbr));
	}
	
	public static INDArray parse(String ednString) {
		return load(new StringReader(ednString));	
	}

	private static long elementCount(int[] ss) {
		long r=1;
		for (int x:ss) {
			r*=x;
		}
		return r;
	}
}
