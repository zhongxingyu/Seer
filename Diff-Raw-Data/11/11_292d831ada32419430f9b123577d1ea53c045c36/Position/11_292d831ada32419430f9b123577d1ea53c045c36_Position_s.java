 /**
  * 
  */
 package util;
 
 /**
  * Position.java
  *
  * @author 	Chris B
  * @date	22 Mar 2013
  * @version	1.0
  */
 public class Position {
 
 	private int x; 
 	private int y;
 	
 	/**
 	 * Create a position x = 0 and y = 0
 	 */
 	public Position() {
 		this(0,0);
 	}
 
 	/**
 	 * Create a position with the given x and y coordinates
 	 * 
 	 * @param x the x coordinate
 	 * @param y the y coordinate
 	 */
 	public Position(int x, int y) {
 		super();
 		this.x = x;
 		this.y = y;
 	}
 
 	/**
 	 * Create a position by copying the x and y coordinates from
 	 * the given position.
 	 * 
 	 * @param position
 	 */
 	public Position(Position position) {
 		super();
 		this.x = position.x;
 		this.y = position.y;
 	}
 	
 	/**
 	 * @return the x
 	 */
 	public int getX() {
 		return x;
 	}
 
 	/**
 	 * @param x the x to set
 	 */
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	/**
 	 * @return the y
 	 */
 	public int getY() {
 		return y;
 	}
 
 	/**
 	 * @param y the y to set
 	 */
 	public void setY(int y) {
 		this.y = y;
 	}
 	
 	public void set(Position p) {
 		this.x = p.getX();
 		this.y = p.getY();
 	}
 	
 	@Override
 	public boolean equals(Object other) {
 		if(other instanceof Position) {
 			Position p = (Position) other; 
 			return this.x == p.x && this.y == p.y;
 		}
 		else {
 			return false;
 		}	
 	}
 	
 	public void add(int x, int y) {
 		this.x += x;
 		this.y += y;
 	}
 	
 	/**
 	 * Given a direction, calculates the position 1 move in that direction.
 	 * The new position is returned.
 	 * 
 	 * @param direction
 	 * @return
 	 */
 	public Position getPositionInDirection(int direction) {
 		//  4 5
 		// 3 C 0
 		//  2 1
 		
 		int dx = 0;
 		int dy = 0;
 		
 		if(direction == 0) {
 			dx = 1;
 		}
 		else if(direction == 1) {
 			dy = 1;
 		}
 		else if(direction == 2) {
			dx = -1;
 			dy = 1;	
 		}
 		else if(direction == 3) {
 			dx = -1;
 		}
 		else if(direction == 4) {
			dx = -1;
 			dy = -1;
 		}
 		else if(direction == 5) {
 			dy = -1;
 		}
 		
 		Position p = new Position(this);
 		p.add(dx, dy);
 		
 		return p;
 	}
 }
