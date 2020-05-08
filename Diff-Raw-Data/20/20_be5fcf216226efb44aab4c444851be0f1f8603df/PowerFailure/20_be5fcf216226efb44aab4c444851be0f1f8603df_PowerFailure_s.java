 package item;
 
 
 import grid.Square;
 
 import java.util.Observer;
 
 /**
  * This class represents a power failure on a square.
  * 
  * @invar	The duration left of this power failure is greater than or equal to 0
  * 			| getDurationLeft() >= 0
  * 
  * @author 	Groep 8
  * 
  * @version	May 2013
  */
 public abstract class PowerFailure extends UnportableItem implements Observer {	
 	
 	/**
 	 * The duration that this power failure square has left to stay active.
 	 */
 	private int durationLeft;
 	
 	/**
 	 * Initialize this power failure with a given duration left and players to observe.
 	 * 
 	 * @effect 	The duration left is set to the duration
 	 * 		 	|	setDurationLeft(getDuration());
 	 */
 	public PowerFailure() {
 		super();
 		setDurationLeft(getDuration());
 	}
 	
 	/**
 	 * Returns the duration a power failure still remains active.
 	 * 
 	 * @return	The duration the power failure still remains active.
 	 * 			| result == durationLeft
 	 */
 	public int getDurationLeft() {
 		return durationLeft;
 	}
 
 	/**
 	 * Sets the duration the power failure has left to be active,
 	 * to the given duration.
 	 * 
 	 * @param 	durationLeft
 	 * 			The duration the square has left.
 	 * 
 	 * @post	The number of turns left, is set to the given number.
 	 * 			| new.durationLeft = durationLeft
 	 */
	private void setDurationLeft(int durationLeft) {
 		this.durationLeft = durationLeft;
 	}
 	
 	/**
 	 * Return the duration of this power failure
 	 */
 	public abstract int getDuration();
 
 	/**
 	 * Lowers the duration during which this power failure still
 	 * remains active.
 	 * 
 	 * @effect	Decrease the durationLeft by 1.
 	 * 			| setDurationLeft(getDurationLeft()-1)
 	 */
 	public void lowerDurationLeft() {
 		setDurationLeft(getDurationLeft()-1);
 	}
 	
 	/**
 	 * Generate power failures on the grid
 	 */
 	public abstract void generate();
 	
 	/**
 	 * Terminate this power failure
 	 */
 	public abstract void terminate();
 
 	/**
 	 * Check whether this power failure is visible
 	 * @return True
 	 * @see item.Item#isVisible()
 	 */
 	@Override
 	public boolean isVisible() {
 		return true;
 	}
 
 	/**
 	 * Check whether this power failure can be on the same square with the given items
 	 * @return True
 	 * @see item.Item#canBeOnSquareWith(item.Item[])
 	 */
 	@Override
 	public boolean canBeOnSquareWith(Square square, Item... items) {
 		return true;
 	}
 
 	/**
 	 * Check whether the item can be stepped on
 	 * @return True
 	 */
 	@Override
 	public boolean canBeSteppedOn() {
 		return true;
 	}
 	
 	
 
 }
