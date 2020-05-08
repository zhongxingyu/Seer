 package com.timwu.Particles.math;
 
 import android.graphics.Color;
 
 public class Particle {
 	private Vector2d pos, v;
 	private float r;
 	private int color;
 	private float bounce = 0.5f;
 	
 	public Particle(Vector2d pos, Vector2d v, float radius, int color) {
 		this.pos = new Vector2d(pos);
 		this.v  = new Vector2d(v);
 		this.r = radius;
 		this.color = color;
 	}
 	
 	public void move(float dt) {
 		// Just doing a naive implementation where velocity will be
 		// constant over the timeslice. Probably not too bad an approximation.
 		pos.multiplyAdd(dt, v);
 	}
 	
 	public void accelerate(float dt, Vector2d a) {
 		v.multiplyAdd(dt, a);
 	}
 	
 	public void bounce(Vector2d normal) {
 		v.reflect(normal);
 		v.scale(bounce);
 	}
 	
 	public float impactTime(Segment s) {
		// Get the vector going from the position to one of the segment's endpoints
		// Will use this to determine if the particle is even towards the segment.
 		Vector2d startToPos = new Vector2d(pos).multiplyAdd(-1.0f, s.getStart());
 		
 		// Calculate the time to impact
		float tImpact = (r - startToPos.dot(s.getN())) / v.dot(s.getN());
 		
 		// Check if the impact lies on the segment, if not there's no impact
 		float gImpact = startToPos.dot(s.getG()) + v.dot(s.getG()) * tImpact;
		if (tImpact < Physics.FUDGE || gImpact < Physics.FUDGE || gImpact > s.getLength()) {
 			color = Color.YELLOW;
 			return Float.MAX_VALUE;
 		}
 		color = Color.CYAN;
 		return tImpact;
 	}
 	
 	public Vector2d getPos() {
 		return pos;
 	}
 	
 	public Vector2d getV() {
 		return v;
 	}
 	
 	public float getR() {
 		return r;
 	}
 	
 	public int getColor() {
 		return color;
 	}
 	
 	public void setColor(int c) {
 		color = c;
 	}
 
 	public String toString() {
 		return "Particle @ " + pos + " going " + v;
 	}
 }
