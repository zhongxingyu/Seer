 package com.ggervais.gameengine.math;
 
import java.util.Vector;

 public class Vector3D {
 	private float x;
 	private float y;
 	private float z;
     private float w;
 	
 	public Vector3D() {
 		this(0, 0, 0);
 	}
 	
 	public Vector3D(float x, float y, float z) {
 		this.x = x;
 		this.y = y;
 		this.z = z;
         this.w = 1;
 	}
 
     public Vector3D(float x, float y, float z, float w) {
         this(x, y, z);
         w(w);
     }
 	
 	public static Vector3D zero() {
 		return new Vector3D(0f, 0f, 0f);
 	}
 	
 	public void add(Vector3D v) {
 		x(x() + v.x());
 		y(y() + v.y());
 		z(z() + v.z());
 	}
 	
 	public static Vector3D add(Vector3D v1, Vector3D v2) {
 		return new Vector3D(v1.x() + v2.x(), v1.y() + v2.y(), v1.z() + v2.z());
 	}
 	
 	public void sub(Vector3D v) {
 		x(x() - v.x());
 		y(y() - v.y());
 		z(z() - v.z());
 	}
 	
 	public static Vector3D sub(Vector3D v1, Vector3D v2) {
 		return new Vector3D(v1.x() - v2.x(), v1.y() - v2.y(), v1.z() - v2.z());
 	}
 	
 	public float length() {
 		return (float) Math.sqrt(Math.pow(x(), 2) + Math.pow(y(), 2) + Math.pow(z(), 2));
 	}
 	
 	public float lengthSquared() {
 		return (float) (Math.pow(x(), 2) + Math.pow(y(), 2) + Math.pow(z(), 2));
 	}
 	
 	public float dotProduct(Vector3D v) {
 		return x() * v.x() + y() * v.y() + z() * v.z();
 	}
 	
 	public static float dotProduct(Vector3D v1, Vector3D v2) {
 		return v1.x() * v2.x() + v1.y() * v2.y() + v1.z() * v2.z();
 	}
 	
 	public Vector3D crossProduct(Vector3D v) {
 		return new Vector3D(y() * v.z() - v.y() * z(),
 							z() * v.x() - v.z() * x(),
 							x() * v.y() - v.x() * y());
 	}
 	
 	public static Vector3D crossProduct(Vector3D v1, Vector3D v2) {
 		return new Vector3D(v1.y() * v2.z() - v2.y() * v1.z(),
 							v1.z() * v2.x() - v2.z() * v1.x(),
 							v1.x() * v2.y() - v2.x() * v1.y());
 	}
 	
 	public void normalize() {
 		float length = length();
 		x(x() / length);
 		y(y() / length);
 		z(z() / length);
 	}
 	
 	public Vector3D normalized() {
 		float length = length();
        if (length == 0) {
            return Vector3D.zero();
        }
 		return new Vector3D(x() / length, y() / length, z() / length);
 	}
 	
 	public static Vector3D normalized(Vector3D v) {
 		
 		if (v == null) {
 			return Vector3D.zero();
 		}
 		
 		float length = v.length();
 		if (length == 0) {
 			return Vector3D.zero();
 		}
 		
 		return new Vector3D(v.x() / length, v.y() / length, v.z() / length);
 	}
 	
 	public void multiply(float k) {
 		x(x() * k);
 		y(y() * k);
 		z(z() * k);
 	}
 	
 	public Vector3D multiplied(float k) {
 		return new Vector3D(x() * k, y() * k, z() * k);
 	}
 	
 	public static Vector3D multiply(Vector3D v, float k) {
 		return new Vector3D(v.x() * k, v.y() * k, v.z() * k);
 	}
 	
 	public float x() { return this.x; }
 	public float y() { return this.y; }
 	public float z() { return this.z; }
     public float w() { return this.w; }
 	
 	public void x(float x) { this.x = x; }
 	public void y(float y) { this.y = y; }
 	public void z(float z) { this.z = z; }
     public void w(float w) { this.w = w; }
 
     public Vector3D copy() {
         return new Vector3D(this.x, this.y, this.z);
     }
 
     public static Vector3D copy(Vector3D vector) {
         return vector.copy();
     }
 
     public static Vector3D createFromPolarCoordinates(float length, float theta, float phi) {
         float x = length * (float) Math.cos(theta) * (float) Math.cos(phi);
         float y = length * (float) Math.sin(theta);
         float z = length * (float) Math.cos(theta) * (float) Math.sin(phi);
         return new Vector3D(x, y, z);
     }
 
     public boolean equals(Vector3D vector) {
         return this.x == vector.x() && this.y == vector.y() && this.z()  == vector.z();
     }
 
     public String toString() { return "[" + x() + ", " + y() + ", " + z() + "]"; }
 
     public float get(int index) {
         switch(index) {
             case 0: return x();
             case 1: return y();
             case 2: return z();
         }
 
         return 0;
     }
 
     public void set(int index, float value) {
         switch (index) {
             case 0: x(value); break;
             case 1: y(value); break;
             case 2: z(value); break;
         }
     }
 }
