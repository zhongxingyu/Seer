 package com.timwu.Particles.math;
 
 import android.graphics.Color;
 
 public class Segment {
 	private Vector2d start, end, n;
 	private int color = Color.WHITE;
 	
 	public Segment(float sx, float sy, float ex, float ey) {
 		start = new Vector2d(sx, sy);
 		end = new Vector2d(ex, ey);
		calculateNormal();
	}
	
	private void calculateNormal() {
		n = new Vector2d(start.getY() - end.getY(), end.getX() - start.getX()).normalize();
 	}
 	
 	public void setStart(float sx, float sy) {
 		start.set(sx, sy);
		calculateNormal();
 	}
 	
 	public void setStart(Vector2d s) {
 		start.set(s);
		calculateNormal();
 	}
 	
 	public void setEnd(float ex, float ey) {
 		end.set(ex, ey);
		calculateNormal();
 	}
 	
 	public void setEnd(Vector2d e) {
 		end.set(e);
		calculateNormal();
 	}
 	
 	public Vector2d getStart() {
 		return start;
 	}
 	
 	public Vector2d getEnd() {
 		return end;
 	}
 	
 	public Vector2d getN() {
 		return n;
 	}
 	
 	public int getColor() {
 		return color;
 	}
 }
