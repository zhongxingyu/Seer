 package mikera.vectorz;
 
 import java.nio.DoubleBuffer;
 
 /**
  * Specialised 3D vector
  * 
  * Represents a point in 3D x,y,z space.
  * 
  * @author Mike
  */
 public final class Vector3 extends APrimitiveVector {
 	private static final long serialVersionUID = 2338611313487869443L;
 
 	public double x;
 	public double y;
 	public double z;
 	
 	public Vector3() {
 		super();
 	}
 	
 	public Vector3(Vector3 source) {
 		this.x=source.x;
 		this.y=source.y;
 		this.z=source.z;
 	}
 	
 	public Vector3(double x, double y, double z) {
 		this.x=x;
 		this.y=y;
 		this.z=z;
 	}
 	
 	@Override
 	public void applyOp(Op op) {
 		x=op.apply(x);
 		y=op.apply(y);
 		z=op.apply(z);
 	}
 	
 	@Override
 	public double normalise() {
 		double d=magnitude();
 		if (d>0) multiply(1.0/d);
 		return d;
 	}
 	
 	public Vector3(double... values) {
 		if (values.length!=length()) throw new IllegalArgumentException("Can't create "+length()+"D vector from: "+values);
 		this.x=values[0];
 		this.y=values[1];
 		this.z=values[2];
 	}
 	
 	public Vector3(AVector v) {
 		assert(v.length()==3);
 		this.set(v);
 	}
 
 	public static Vector3 of(double x, double y, double z) {
 		return new Vector3(x,y,z);
 	}
 	
 	public static Vector3 of(double... values) {
 		return new Vector3(values);
 	}
 	
 	@Override
 	public double angle(AVector v) {
 		if (v instanceof Vector3) {return angle((Vector3)v);}
 		return super.angle(v);
 	}
 	
 	public double angle(Vector3 v) {
 		double mag2=(x*x)+(y*y)+(z*z);
 		double vmag2=(v.x*v.x)+(v.y*v.y)+(v.z*v.z);
 		double dot=(x*v.x)+(y*v.y)+(z*v.z);
 		return Math.acos(dot/Math.sqrt(mag2*vmag2));
 	}
 	
 	public void add(double dx, double dy, double dz) {
 		x+=dx;
 		y+=dy;
 		z+=dz;
 	}
 	
 	@Override 
 	public double magnitudeSquared() {
 		return (x*x)+(y*y)+(z*z);
 	}
 	
 	public double distanceSquared(Vector3 v) {
 		double dx=x-v.x, dy=y-v.y, dz=z-v.z;
 		return (dx*dx)+(dy*dy)+(dz*dz);
 	}
 	
 	public double distance(Vector3 v) {
 		return Math.sqrt(distanceSquared(v));
 	}
 	
 	public double distance(AVector v) {
 		if (v instanceof Vector3) {
 			return distance((Vector3)v);
 		}
 		return super.distance(v);
 	}
 	
 	@Override 
 	public double magnitude() {
 		return Math.sqrt(magnitudeSquared());
 	}
 	
 	public void set(Vector3 a) {
 		this.x=a.x;
 		this.y=a.y;
 		this.z=a.z;
 	}
 	
 	public void addMultiple(double dx, double dy, double dz, double factor) {
 		x+=dx*factor;
 		y+=dy*factor;
 		z+=dz*factor;
 	}
 	
 	@Override
 	public void addMultiple(AVector v, double factor) {
 		if (v instanceof Vector3) {
 			addMultiple((Vector3)v,factor);
 		} else {
 			x+=v.get(0)*factor;
 			y+=v.get(1)*factor;
 			z+=v.get(2)*factor;
 		}
 	}
 	
 	public void addMultiple(Vector3 v, double factor) {
 		x+=v.x*factor;
 		y+=v.y*factor;
 		z+=v.z*factor;
 	}
 	
 	public void addProduct(Vector3 a, Vector3 b) {
 		x+=a.x*b.x;
 		y+=a.y*b.y;
 		z+=a.z*b.z;
 	}
 	
 	public void addProduct(Vector3 a, Vector3 b, double factor) {
 		x+=a.x*b.x*factor;
 		y+=a.y*b.y*factor;
 		z+=a.z*b.z*factor;
 	}
 	
 	public void subtractMultiple(Vector3 v, double factor) {
 		x-=v.x*factor;
 		y-=v.y*factor;
 		z-=v.z*factor;
 	}
 	
 	@Override
 	public void add(AVector v) {
 		if (v instanceof Vector3) {
 			add((Vector3)v);
 		} else {
 			x+=v.get(0);
 			y+=v.get(1);
 			z+=v.get(2);
 		}
 	}
 	
 	public void add(Vector3 v) {
 		x+=v.x;
 		y+=v.y;
 		z+=v.z;
 	}
 	
 	public void sub(Vector3 v) {
 		x-=v.x;
 		y-=v.y;
 		z-=v.z;
 	}
 	
 	public void subMultiple(Vector3 v, double factor) {
 		addMultiple(v,-factor);
 	}
 	
 	public double dotProduct(Vector3 a) {
 		return (x*a.x) + (y*a.y) + (z*a.z);
 	} 
 	
 	@Override
 	public void crossProduct(AVector a) {
 		if (a instanceof Vector3) {
 			crossProduct((Vector3) a);
 			return;
 		}
 		double x2=a.get(0);
 		double y2=a.get(1);
 		double z2=a.get(2);
 		double tx=y*z2-z*y2;
 		double ty=z*x2-x*z2;
 		double tz=x*y2-y*x2;			
 		x=tx;
 		y=ty;
 		z=tz;		
 	}
 	
 	public void crossProduct(Vector3 a) {
 		double tx=y*a.z-z*a.y;
 		double ty=z*a.x-x*a.z;
 		double tz=x*a.y-y*a.x;			
 		x=tx;
 		y=ty;
 		z=tz;
 	}
 	
 	@Override
 	public void projectToPlane(AVector normal, double distance) {
 		if (normal instanceof Vector3) {projectToPlane((Vector3)normal,distance); return;}
 		super.projectToPlane(normal, distance);
 	}
 	
 	public void projectToPlane(Vector3 normal, double distance) {
 		assert(Tools.epsilonEquals(normal.magnitude(), 1.0));
 		double d=dotProduct(normal);
 		addMultiple(normal,distance-d);
 	}
 
 	@Override
 	public int length() {
 		return 3;
 	}
 	
 	@Override
 	public double elementSum() {
 		return x+y+z;
 	}
 	
 	@Override
 	public void scaleAdd(double factor, double constant) {
 		x=(x*factor)+constant;
 		y=(y*factor)+constant;
 		z=(z*factor)+constant;
 	}
 
 	@Override
 	public void add(double constant) {
 		x=x+constant;
 		y=y+constant;
 		z=z+constant;
 	}
 
 	@Override
 	public double get(int i) {
 		switch (i) {
 		case 0: return x;
 		case 1: return y;
 		case 2: return z;
 		default: throw new IndexOutOfBoundsException("Index: "+i);
 		}
 	}
 	
 	@Override 
 	public void set(AVector v) {
 		assert(v.length()==3);
 		x=v.get(0);
 		y=v.get(1);
 		z=v.get(2);
 	}
 	
 	@Override 
 	public void set(double v) {
 		x=v;
 		y=v;
 		z=v;
 	}
 
 	@Override
 	public void set(int i, double value) {
 		switch (i) {
 		case 0: x=value; return;
 		case 1: y=value; return;
 		case 2: z=value; return;
 		default: throw new IndexOutOfBoundsException("Index: i");
 		}
 	}
 	
 	@Override
 	public void addAt(int i, double value) {
 		switch (i) {
 		case 0: x+=value; return;
 		case 1: y+=value; return;
 		case 2: z+=value; return;
		default: throw new IndexOutOfBoundsException("Index: "+i);
 		}
 	}
 	
 	public void setValues(double x, double y, double z) {
 		this.x=x;
 		this.y=y;
 		this.z=z;
 	}
 	
 	@Override
 	public void negate() {
 		x=-x;
 		y=-y;
 		z=-z;
 	}
 	
 	@Override
 	public void copyTo(double[] data, int offset) {
 		data[offset]=x;
 		data[offset+1]=y;
 		data[offset+2]=z;
 	}
 	
 	@Override
 	public void toDoubleBuffer(DoubleBuffer dest) {
 		dest.put(x);
 		dest.put(y);
 		dest.put(z);
 	}
 	
 	@Override
 	public Vector3 clone() {
 		return new Vector3(x,y,z);	
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
 	public Vector3 exactClone() {
 		return clone();
 	}
 
 }
