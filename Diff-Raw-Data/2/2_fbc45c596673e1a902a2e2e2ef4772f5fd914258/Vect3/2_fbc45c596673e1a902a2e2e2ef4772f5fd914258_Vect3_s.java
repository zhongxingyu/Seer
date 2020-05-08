 package com.irr310.server;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.vecmath.Tuple3d;
 import javax.vecmath.Vector3d;
 import javax.vecmath.Vector3f;
 
 import fr.def.iss.vd2.lib_v3d.V3DVect3;
 
 public class Vect3 {
 
 	public  Double x;
 	public  Double y;
 	public  Double z;
 	
 	private List<Vect3ChangeListener>  changeListeners;
 
 	public Vect3(double x, double y, double z) {
 		this.x = x;
 		this.y = y;
 		this.z = z;
 		changeListeners = new ArrayList<Vect3.Vect3ChangeListener>();
 	}
 
 	public Vect3(int x, int y, int z) {
 		this((double) x, (double) y,(double) z);
 	}
 
 
 	public Vect3(float x, float y, float z) {
 		this((double) x, (double) y,(double) z);
 	}
 
 	public V3DVect3 toV3DVect3() {
 		return new V3DVect3(x.floatValue(), y.floatValue(), z.floatValue());
 	}
 
 	public Vector3f toVector3f() {
 		return new Vector3f(x.floatValue(), y.floatValue(), z.floatValue());
 	}
 
 	public Vector3d toVector3d() {
 		return new Vector3d(x, y, z);
 	}
 
 	public void set(float x, float y, float z) {
 		this.x = (double) x;
 		this.y = (double) y;
 		this.z = (double) z;
 		fireChanged();
 	}
 	
 	public void set(Vect3 vect) {
 		this.x = vect.x;
 		this.y = vect.y;
 		this.z = vect.z;
 		fireChanged();
 		
 	}
 
 	public static Vect3 origin() {
 		return new Vect3(0, 0, 0);
 	}
 	
 	public static Vect3 one() {
 		return new Vect3(1, 1, 1);
 	}
 	
 	public void addListener(Vect3ChangeListener listener) {
 		changeListeners.add(listener);
 	}
 	
 	public void fireChanged() {
 		for(Vect3ChangeListener listener: changeListeners) {
 			listener.valueChanged();
 		}
 	}
 	
 	public interface Vect3ChangeListener {
 		
 		public void valueChanged();
 	}
 
 	public Vect3 divide(double i) {
		return new Vect3(x/2.0, y/2.0, y/2.0);
 	}
 
 	
 	public Double length() {
 		return Math.sqrt(x*x+y*y+z*z);
 	}
 	
 	public Double distanceTo(Vect3 vect) {
 		return this.diff(vect).length();
 	}
 	
 	public Vect3 diff(Vect3 vect) {
 		return new Vect3(vect.x - x, vect.y - y, vect.z - z);
 	}
 	
 	public Vect3 plus(Vect3 vect) {
 		return new Vect3(vect.x + x, vect.y + y, vect.z + z);
 	}
 
 	@Override
 	public String toString() {
 		return "[x=" + x + ", y=" + y + ", z=" + z + "]";
 	}
 
 	
 
 	
 	
 	
 
 }
