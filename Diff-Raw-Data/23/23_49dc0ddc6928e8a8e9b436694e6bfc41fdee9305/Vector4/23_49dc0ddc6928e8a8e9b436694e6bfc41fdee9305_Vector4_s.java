 package mikera.vectorz;
 
 import java.nio.DoubleBuffer;
 
 /**
  * Specialised 4D vector
  * 
  * @author Mike
  */
 public final class Vector4 extends APrimitiveVector {
 	private static final long serialVersionUID = -6018622211027585397L;
 
 	public double x;
 	public double y;
 	public double z;
 	public double t;
 	
 	public Vector4() {
 		super();
 	}
 	
 	public Vector4(Vector4 source) {
 		this.x=source.x;
 		this.y=source.y;
 		this.z=source.z;
 		this.t=source.t;
 	}
 	
 	public Vector4(double x, double y, double z, double t) {
 		this.x=x;
 		this.y=y;
 		this.z=z;
 		this.t=t;
 	}
 	
 	public Vector4(double... values) {
 		if (values.length!=length()) throw new IllegalArgumentException("Can't create "+length()+"D vector from: "+values);
 		this.x=values[0];
 		this.y=values[1];
 		this.z=values[2];
 		this.t=values[3];
 	}
 	
 	public static Vector4 of(double x, double y, double z, double t) {
 		return new Vector4(x,y,z,t);
 	}
 	
 	public static Vector4 of(double... values) {
 		return new Vector4(values);
 	}
 	
 	@Override
 	public void applyOp(Op op) {
 		x=op.apply(x);
 		y=op.apply(y);
 		z=op.apply(z);
 		t=op.apply(t);
 	}
 
 	
 	public void add(double dx, double dy, double dz, double dt) {
 		x+=dx;
 		y+=dy;
 		z+=dz;
 		t+=dt;
 	}
 	
 	public void set(Vector4 a) {
 		this.x=a.x;
 		this.y=a.y;
 		this.z=a.z;
 		this.t=a.t;
 	}
 	
 	@Override
 	public void negate() {
 		x=-x;
 		y=-y;
 		z=-z;
 		t=-t;
 	}
 	
 	public void addMultiple(double dx, double dy, double dz, double dt, double factor) {
 		x+=dx*factor;
 		y+=dy*factor;
 		z+=dz*factor;
 		t+=dt*factor;
 	}
 	
 	@Override
 	public void addMultiple(AVector v, double factor) {
 		if (v instanceof Vector4) {
 			addMultiple((Vector4)v,factor);
 		} else {
 			x+=v.get(0)*factor;
 			y+=v.get(1)*factor;
 			z+=v.get(2)*factor;
 			t+=v.get(3)*factor;
 		}
 	}
 	
 	public void addMultiple(Vector4 v, double factor) {
 		x+=v.x*factor;
 		y+=v.y*factor;
 		z+=v.z*factor;
 		t+=v.t*factor;
 	}
 	
 	public void addProduct(Vector4 a, Vector4 b) {
 		x+=a.x*b.x;
 		y+=a.y*b.y;
 		z+=a.z*b.z;
 		t+=a.t*b.t;
 	}
 	
 	public void addProduct(Vector4 a, Vector4 b, double factor) {
 		x+=a.x*b.x*factor;
 		y+=a.y*b.y*factor;
 		z+=a.z*b.z*factor;
 		t+=a.t*b.t*factor;
 	}
 	
 	public double dotProduct(Vector4 a) {
 		return (x*a.x)+(y*a.y)+(z*a.z)+(t*a.t);
 	}
 
 	
 	@Override
 	public int length() {
 		return 4;
 	}
 	
 	@Override
 	public double elementSum() {
 		return x+y+z+t;
 	}
 
 	@Override
 	public double get(int i) {
 		switch (i) {
 		case 0: return x;
 		case 1: return y;
 		case 2: return z;
 		case 3: return t;
 		default: throw new IndexOutOfBoundsException("Index: i");
 		}
 	}
 
 	@Override
 	public void set(int i, double value) {
 		switch (i) {
 		case 0: x=value; return;
 		case 1: y=value; return;
 		case 2: z=value; return;
 		case 3: t=value; return;
 		default: throw new IndexOutOfBoundsException("Index: i");
 		}
 	}
 	
 	@Override
 	public void addAt(int i, double value) {
 		switch (i) {
 		case 0: x+=value; return;
 		case 1: y+=value; return;
 		case 2: z+=value; return;
 		case 3: t+=value; return;
		default: throw new IndexOutOfBoundsException("Index: i");
 		}
 	}
 	
 	public void setValues(double x, double y, double z, double t) {
 		this.x=x;
 		this.y=y;
 		this.z=z;
 		this.t=t;
 	}
 	
 	@Override
 	public void copyTo(double[] data, int offset) {
 		data[offset]=x;
 		data[offset+1]=y;
 		data[offset+2]=z;
 		data[offset+3]=t;
 	}
 	
 	@Override
 	public void toDoubleBuffer(DoubleBuffer dest) {
 		dest.put(x);
 		dest.put(y);
 		dest.put(z);
 		dest.put(t);
 	}
 	
 	@Override
 	public Vector4 clone() {
 		return new Vector4(x,y,z,t);	
 	}
 
 	@Override
 	public double getX() {
 		return x;
 	}
 	
 	@Override
 	public double getY() {
 		return y;
 	}
 	
 	@Override
 	public double getZ() {
 		return z;
 	}
 	
 	@Override
 	public double getT() {
 		return t;
 	}
 	
 	@Override 
 	public Vector4 exactClone() {
 		return clone();
 	}
 }
