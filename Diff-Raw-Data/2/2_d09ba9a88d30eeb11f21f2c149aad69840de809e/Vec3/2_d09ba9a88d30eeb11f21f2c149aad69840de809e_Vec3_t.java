 
 package com.slsw.fiarworks.firework;
 
 import java.util.Random;
 
 public class Vec3
 {
 	public float x;
 	public float y;
 	public float z;
 	private static Random r = new Random();
 	public static float vecPrecision = 0.001f;
 
 	public Vec3()
 	{
 		x = 0.0f;
 		y = 0.0f;
 		z = 0.0f;
 	}
 
 	public Vec3(float x_, float y_, float z_)
 	{
 		x = x_;
 		y = y_;
 		z = z_;
 	}
 	
 	public Vec3(final Vec3 other)
 	{
 		this.x = other.x;
 		this.y = other.y;
 		this.z = other.z;
 	}
 
 	public void add(final Vec3 B)
 	{
 //		Changed to modify existing object instead of returning a new one.
 //		return new Vec3(this.x+B.x, this.y+B.y, this.z+B.z);
 		this.x += B.x;
 		this.y += B.y;
 		this.z += B.z;
 	}
 
 	public void add(float xdiff, float ydiff, float zdiff)
 	{
 		this.x += xdiff;
 		this.y += ydiff;
 		this.z += zdiff;		
 	}
 
 	public void sub(float xdiff, float ydiff, float zdiff)
 	{
 		this.x -= xdiff;
 		this.y -= ydiff;
 		this.z -= zdiff;
 	}
 
 	public void sub(final Vec3 B)
 	{
 		this.sub(B.x, B.y, B.z);
 	}
 
 	public void neg()
 	{
 		this.x = -this.x;
 		this.y = -this.y;
 		this.z = -this.z;
 	}
 
 	public float dot(final Vec3 B)
 	{
 		return (this.x * B.x + this.y * B.y + this.z * B.z);
 	}
 
 	public void mult(final float scalar)
 	{
 		this.x *= scalar;
 		this.y *= scalar;
 		this.z *= scalar;
 	}
 
 	// i j k
 	// x y z
 	// X Y Z
 	// 
 	// cross product:
 	// 
 	// <yZ-zY, -(xZ-zX), xY-yX>
 	// <yZ-zY, zX-xZ, xY-yX>
 
 	// returns this cross B as new
 	public Vec3 cross(final Vec3 B)
 	{
 		return new Vec3(y*B.z - z*B.y, z*B.x - x*B.z, x*B.y - y*B.x);
 	}
 
 	// get length of vector
 	public double len() {
 		return Math.sqrt((double)x*x + (double)y*y + (double)z*z);
 	}
 	
 	// retain vector direction but make length roughly equal to 1
 	public void unitize() {
 		double len = this.len();
 		// avoid division by zero
		if (Math.abs(len)<vecPrecision) return;
 		// don't unitize roughly unit vectors
 		if (Math.abs(len-1)<vecPrecision) return;
 		// divide dimensions by length to unitize
 		float invlen = (float) (1.0 / len);
 		this.x *= invlen;
 		this.y *= invlen;
 		this.z *= invlen;
 	}
 	
 	// returns true if vector almost points directly up or down
 	// (assuming +Z is up in world coordinates)
 	public boolean isAlmostVertical() {
 		// estimation heuristic: if world x and y are both small, the vector is almost vertical
 		if (Math.abs(x)<vecPrecision && Math.abs(y)<vecPrecision) return true;
 		else return false;
 	}
 
 	static Vec3 random_velocity(float speed)
 	{
 		float x = (r.nextFloat() - 0.5f) * speed;
 		float y = (r.nextFloat() - 0.5f) * speed;
 		float z = (r.nextFloat() - 0.5f) * speed;
 
 		return new Vec3(x, y, z);
 	}
 }
