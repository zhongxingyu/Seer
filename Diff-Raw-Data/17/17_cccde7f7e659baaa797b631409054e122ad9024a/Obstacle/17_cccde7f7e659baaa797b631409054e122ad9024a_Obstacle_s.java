 package obstacle;
 
 import java.util.LinkedList;
 import java.util.Observable;
 import java.util.Observer;
 
 import grid.Square;
 
 /**
  * This abstract class represents obstacles.
  * A player cannot move through such obstacles.
  * 
  * 
  * @author	Groep 8
  * @version	May 2013
  */
 public abstract class Obstacle implements Observer {
 	
 	/**
 	 * Represents a list of square, of which the obstacle exists.
 	 * Operations on this list are implemented as a queue.
 	 */
 	private LinkedList<Square> squareList = new LinkedList<Square>();
 
 	/**
 	 * Returns the square list of the obstacle. 
 	 * 
 	 * @return	The square list of the obstacle
 	 */
 	protected LinkedList<Square> getSquareList() {
 		return squareList;
 	}
 
 	/**
 	 * Add a square to the list of squares that are covered by this obstacle.
 	 * 
 	 * @param 	square
 	 * 			The square to add to the square list.
 	 * 
 	 * @effect	The square is added to the list of squares.
 	 * 			| squareList.add(square)
 	 * @effect	The added square now has a reference to this obstacle.
 	 * 			| square.setObstacle(this)
 	 */
 	public void addSquare(Square square) {
 		if(square != null && !coversSquare(square)) {
 			squareList.add(square);
 			square.addObstacle(this);
 		}
 	}
 	
 	/**
 	 * Remove the first square from the list of squares that are covered by this obstacle
 	 * 
 	 * @param 	square
 	 * 			The square to remove from this obstacle.
 	 * @effect	The removed square now has not any reference to it anymore.
 	 * 			| squareList.getFirst().setObstacle(null)
 	 * @effect	The first square is removed from the square list.
 	 * 			| squareList.removeFirst()
 	 * @return The square you removed
 	 * 
 	 */
 	public Square removeSquare() {
 		Square s = squareList.getFirst();
 		squareList.removeFirst();
		s.addObstacle(null);
 		return s;
 	}
 	
 	/**
 	 * Check whether this obstacle covers the given square.
 	 * 
 	 * @param 	square
 	 * 			The square to check
 	 * @return 	True iff the given square covers this grid
 	 * 			| result == squareList.contains(square)
 	 */
 	public boolean coversSquare(Square square){
 		return squareList.contains(square);
 	}
 	
 	/**
 	 * Return the number of squares covered by this obstacle
 	 * @return The number of squares coverd by this obstacle
 	 */
 	public int getNbSquares(){
 		return squareList.size();
 	}
 	
 	/**
 	 * Return the index of the given square
 	 * @param s The square for which you want to know the index
 	 * @return The index of the given square
 	 */
 	public int indexOf(Square s){
 		return squareList.indexOf(s);
 	}
 	
 	/**
 	 * This method returns whether an actor can enter this obstacle.
 	 * 
 	 * @return 	True if the obstacle can be entered, otherwise false.
 	 */
 	public abstract boolean canEnter();
 	
 	/**
 	 * This method returns whether an actor can leave this obstacle.
 	 * 
 	 * @return	True if the obstacle can be entered, otherwise false.
 	 */
 	public abstract boolean canLeave();
 
 	public abstract boolean canBeOnSquareWith(Obstacle obstacle);
 	
 	public void update(Observable observable, Object object) {
 		// by default, do nothing
 	}
 }
