 /*
  * Coordinate.java	
  * Version 1.0 (2013-05-30)
  */
 
 package battleships.game;
 
 /**
  * Represents a coordinate in the game battleship.
  * The coordinates's value is meant not to be changed after initiation, but has methods for getting the values.
  * 
  * @author Åsa Waldhe
  */
 public class Coordinate {
 	
 	/** x-coordinate */
	private final Integer X;
 	/** y-coordinate */
	private final Integer Y;
 	
 	/** Max value for coordinate. */
 	final static int MAX=9;
 	/** Min value for coordinate. */
 	final static int MIN=0;
 	
 	
 	/**
 	 * Initiates x- and y-coordinates respectively.
 	 * 
 	 * @param x					x-coordinate
 	 * @param y					y-coordinate
 	 */
 	public Coordinate( int x, int y){
 		if(x<0||x>9||y<0||y>9){
 			//throw									Skapa exception för detta!!
 		}
 		this.X=x;
 		this.Y=y;		
 	}
 	
 	/** 
 	 * @return					X-coordinate
 	 */
 	public Integer getX(){
 		return X;
 	}
 	
 	/**
 	 * @return					Y-coordinate
 	 */
 	public Integer getY(){
 		return Y;
 	}
 	
 	/**
 	 * Overrides the Object-method toString, to get a string-representation of the object.
 	 * 
 	 * @return					A string-representation of the coordinate.
 	 */
 	public String toString(){		
 		return "("+X+","+Y+")";
 	}
 	
 	/**
 	 * Tells whether two coordinates are equals.
 	 * 
 	 * @param coord				The coordinate to compare to.
 	 * @return 					Equals or not. 
 	 */
 	@Override
 	public boolean equals(Object obj){
 		if(obj == null || obj.getClass() != getClass())
 			return false;
 		else{
 			Coordinate coord = (Coordinate) obj;
 			return (this.X==coord.getX())&&(this.Y==coord.getY());	
 		}
 			
 	}
 	
 	/**
 	 * 
 	 */
 	@Override
 	public int hashCode(){
 		return 100*this.X+Y;
 	}
 	
 
 }
