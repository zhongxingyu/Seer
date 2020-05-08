 package com.amg.mywow.common;
 
 import java.io.Serializable;
 
 public class Position implements Serializable{
 	
 	private static final long serialVersionUID = 1L;
 	
 	private int x;
 	private int y;
 	
 	public Position() {
 	}
 	
 	public Position(int y, int x) {
 		this.y = y;
 		this.x = x;
 	}
 	
 	public int getX() {
 		return x;
 	}
 	
 	public void setX(int x) {
 		this.x = x;
 	}
 	
 	public int getY() {
 		return y;
 	}
 	
 	public void setY(int y) {
 		this.y = y;
 	}
 	
 	public String toString() {
		return String.format("[%s,%s]", x, y);
 	}
 	
 	public final float distance(Position other) {
         float dx = x - other.x;
         float dy = y - other.y;
         return (float)Math.sqrt((dx * dx) + (dy * dy));
     }
     
 	public int hashCode()
 	{
 		return y ^ x;
 		//return 997 * ((int)y) ^ 991 * ((int)x); //large primes! 
 	}
 	
 	public boolean equals(Object o) { 
 		if ((o instanceof Position) && (((Position)o).y == this.y) && (((Position)o).x == this.x)) { 
 			return true;
 		} else { 
 			return false;
 		}
 	}
 }
