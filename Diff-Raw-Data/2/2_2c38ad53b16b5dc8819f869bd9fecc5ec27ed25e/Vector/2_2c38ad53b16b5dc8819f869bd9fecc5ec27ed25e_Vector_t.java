 /********************************************************************************
  *                                                                              *
  *  This file is part of the Tiny Math Library.                                 *
  *                                                                              *
  *  Copyright 2012 Chris Foster                                                 *
  *                                                                              *
  ********************************************************************************/
 
 package math;
 
 import java.io.Serializable;
 
 public class Vector implements Serializable
 {
 	public float x;
 	public float y;
 	public float z;
 
 	public Vector(float x, float y, float z)
 	{
 		this.x = x;
 		this.y = y;
 		this.z = z;
 	}
 
 	public Vector(Vector b)
 	{
 		this.x = b.x;
 		this.y = b.y;
 		this.z = b.z;
 	}
 
 	public Vector()
 	{
 		this.x = 0.0f;
 		this.y = 0.0f;
 		this.z = 0.0f;
 	}
 
 	public Vector add(Vector b)
 	{
 		return new Vector(this.x + b.x, this.y + b.y, this.z + b.z);
 	}
 
 	public Vector add(float b)
 	{
 		return new Vector(this.x + b, this.y + b, this.z + b);
 	}
 
 	public Vector subtract(Vector b)
 	{
 		return new Vector(this.x - b.x, this.y - b.y, this.z - b.z);
 	}
 
 	public Vector subtract(float b)
 	{
 		return new Vector(this.x - b, this.y - b, this.z - b);
 	}
 
 	public Vector multiply(Vector b)
 	{
 		return new Vector(this.x * b.x, this.y * b.y, this.z * b.z);
 	}
 
 	public Vector multiply(float b)
 	{
 		return new Vector(this.x * b, this.y * b, this.z * b);
 	}
 
 	public Vector divide(Vector b)
 	{
 		return new Vector(this.x / b.x, this.y / b.y, this.z / b.z);
 	}
 
 	public Vector divide(float b)
 	{
 		return new Vector(this.x / b, this.y / b, this.z / b);
 	}
 
 	public float dot(Vector b)
 	{
 		return (this.x * b.x + this.y * b.y + this.z * b.z);
 	}
 
 	public float length()
 	{
 		return (float)Math.sqrt(this.lengthSquared());
 	}
 
 	public float lengthSquared()
 	{
 		return this.dot(this);
 	}
 
 	public Vector normalize()
 	{
 		return this.normalize(this.length());
 	}
 
 	public Vector normalize(float w)
 	{
 		Vector result = this.divide(w);
 		this.x = result.x;
 		this.y = result.y;
 		this.z = result.z;
 		return this;
 	}
 
 	public Vector normalized()
 	{
 		return this.normalized(this.length());
 	}
 
 	public Vector normalized(float w)
 	{
 		return this.divide(w);
 	}
 
 	public Vector cross(Vector b)
 	{
 		return new Vector(this.y * b.z - this.z * b.y,
 						  this.z * b.x - this.x * b.z,
 						  this.x * b.y - this.y * b.x);
 	}
 
 	public boolean isZero()
 	{
 		if (Math.abs(this.x) < Constants.FLT_EPSILON)
 			if (Math.abs(this.y) < Constants.FLT_EPSILON)
 				if (Math.abs(this.z) < Constants.FLT_EPSILON)
 					return true;
 		return false;
 	}
 
 	@Override
 	public boolean equals(Object b)
 	{
 		if (this == b)
 			return true;
 		if (b == null)
 			return false;
 		if (this.getClass() != b.getClass())
 			return false;
 
 		Vector c = (Vector)b;
 
 		if (Math.abs(this.x - c.x) > Constants.FLT_EPSILON)
 			return false;
 		if (Math.abs(this.y - c.y) > Constants.FLT_EPSILON)
 			return false;
 		if (Math.abs(this.z - c.z) > Constants.FLT_EPSILON)
 			return false;
 
 		return true;
 	}
 
 	@Override
 	public int hashCode()
 	{
 		int hash = 15; // The starting value is arbitrary.
 
 		// 17 is an odd prime.
 		hash = 17 * hash + (new Float(this.x)).hashCode();
 		hash = 17 * hash + (new Float(this.y)).hashCode();
 		hash = 17 * hash + (new Float(this.z)).hashCode();
 
 		return hash;
 	}
 
 	@Override
 	public Vector clone()
 	{
 		return new Vector(this.x, this.y, this.z);
 	}
 
 	@Override
 	public String toString()
 	{
		return String.format("( %f, %f, %f )", this.x, this.y, this.z);
 	}
 }
