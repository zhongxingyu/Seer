 package rsmg.util;
 /**
  * Class for representing a two dimensional mathematical vector 
  * @author Johan Grnvall
  *
  */
 public class Vector2d {
 	private double x;
 	private double y;
 	
 	public Vector2d(){
 		x = 0;
 		y = 0;
 	}
	public Vector2d(int x, int y){
 		this.x=x;
 		this.y=y;
 	}
 	
 	public void add(Vector2d vector){
 		x+=vector.getX();
 		y+=vector.getY();
 	}
 	
 	/**
 	 * @return the horizontal size of the vector
 	 */
 	public double getX(){
 		return x;
 	}
 	public void setX(double x){
 		this.x=x;
 	}
 	public void setY(double y){
 		this.y=y;
 	}
 	
 	/**
 	 * @return the vertical size of the vector
 	 */
 	public double getY(){
 		return y;
 	}
 	
 	/**
 	 * this method returns the resulting vectors length using Pythagoras theorem 
 	 * @return the length of the resulting vector.
 	 */
 	public double getlength(){
 		return Math.sqrt(x*x+y*y);
 	}
 	
 	/**
 	 * subtracts current vector with the specified vector
 	 * @param vector
 	 */
 	public void subtract(Vector2d vector){
 		x-=vector.getX();
 		y-=vector.getY();
 	}
 	
 }
