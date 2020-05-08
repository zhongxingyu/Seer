 package com.timwu.Particles.math;
 
 public class Vector2d implements Cloneable {
 	private float x, y;
 	
 	public Vector2d(float x, float y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	public Vector2d(Vector2d v) {
 		x = v.getX();
 		y = v.getY();
 	}
 	
 	public Vector2d normalize() {
 		scale(1.0f / length());
 		return this;
 	}
 	
 	public Vector2d multiplyAdd(float a, Vector2d v) {
 		x += a * v.getX();
 		y += a * v.getY();
 		return this;
 	}
 	
 	public Vector2d multiply(float a, float b) {
 		x *= a;
 		y *= b;
 		return this;
 	}
 	
 	public Vector2d scale(float a) {
 		x *= a;
 		y *= a;
 		return this;
 	}
 	
 	public Vector2d reflect(Vector2d n) {
 		float proj = dot(n);
 		float nParallel = -2.0f * proj;
 		return multiplyAdd(nParallel , n);
 	}
 	
 	public float length() {
 		return (float) Math.sqrt(dot(this));
 	}
 	
 	public float dot(Vector2d right) {
 		return x * right.getX() + y * right.getY();
 	}
 	
 	public float cross(Vector2d right) {
 		return x * right.getY() - y * right.getX();
 	}
 	
 	public void set(float x, float y) {
 		this.x = x;
 		this.y = y;
 	}
 	
 	public void set(Vector2d v) {
 		x = v.getX();
 		y = v.getY();
 	}
 	
 	public float getX() {
 		return x;
 	}
 	
 	public float getY() {
 		return y;
 	}
 
 	@Override
 	public String toString() {
		return String.format("(%1$.2f, %2$.2f)[%3$.2f]", x, y, length());
 	}
 }
