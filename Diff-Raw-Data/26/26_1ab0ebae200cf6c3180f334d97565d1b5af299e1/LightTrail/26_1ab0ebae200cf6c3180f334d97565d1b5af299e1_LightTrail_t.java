 package obstacle;
 
 import item.Item;
 import item.Teleporter;
 
 import java.util.*;
 
 import exception.OutsideTheGridException;
 import grid.*;
 import game.*;
 
 /**
  * This class represents a light trail, which a player leaves
  * when he moves through the grid.
  * 
  * @invar	Each square that has a light trail piece of this square, is in the duration map.
  * 			| for each square in squares
  * 			|	 isInDurationMap(square)
  * @invar	A square cannot have a light trail longer than 3 actions.
  * 			| lighttrail.maxDuration == 3
  * @invar	After each action, the duration of a square in the light trail is lowered.
  * 			| for each square in squares
  * 			|	lowerDuration()
  * @invar	When a square has a duration of 0, it is removed from the light trail.
  * 			| for each square in squares
  * 			| 	if duration == 0
  * 			|		squares.remove(square)
  * @invar	A light trail can not be entered.
  * 			| canEnter() == false;
  * @invar 	A light trail can be left.
  * 			| canLeave() == true;
 * @invar	The pieces of a light trail are contiguous and can also be connected by a teleporter.
  * 
  * @author 	Groep 8
  * @version	May 2013
  */
 public class LightTrail extends Obstacle implements Observer {
 
 	/**
 	 * Represents the owner of the light trail, which is a player.
 	 */
 	private Player owner;
 	
 	/**
 	 * This map maps a Square, on which there is a light trail,
 	 * to an integer value, that indicates the duration that the square
 	 * is part of the light trail yet.
 	 */
 	private HashMap<Square, Integer> durationMap = new HashMap<Square, Integer>(); 
 	
 	/**
 	 * Constant that represents the duration during which the light trail
 	 * can still exist.
 	 */
 	public static final int DURATION = 3;
 	
 	/**
 	 * Return the player that owns this light trail.
 	 * 
 	 * @return	The player that owns this light trail.
 	 */
 	public Player getOwner() {
 		return owner;
 	}
 	
 	/**
 	 * Set the player that owns this light trail
 	 * 
 	 * @param 	owner
 	 * 			The player that owns this light trail
 	 * 
 	 * @pre 	If the given player is effective, it does refer to this light trail as its light trail
 	 * @pre 	If the given player is not effective and this light trail has a player, this light trail can't have a player that
 	 * 			refers to this light trail
 	 * 
 	 * @post	The owner is set.
 	 * 			| this.owner = owner
 	 * 
 	 * @note	This method is set public to maintain a bidirectional association
 	 * 			and should not be used in another way.
 	 */
 	public void setOwner(Player owner) {
 		assert (owner == null) || (owner.getLightTrail() == this);
 		assert ( (owner != null) || (getOwner()== null) ) || (getOwner().getLightTrail() != this);
 		this.owner = owner;
 	}
 
 	/**
 	 * Returns the map that takes part of the association between
 	 * the square in the light trail and the duration it has left.
 	 * 
 	 * @return	An unmodifiable map that takes part of the durations.
 	 * 			| return Collections.unmodifiableMap(durationMap)
 	 */
 	public Map<Square, Integer> getDurationMap() {
 		return Collections.unmodifiableMap(durationMap);
 	}
 	
 	/**
 	 * Adds a square to the duration map.
 	 * 
 	 * @param 	square
 	 * 			The square that has to be added or updated
 	 * 			in the duration map.
 	 * @effect	Adds the given square to the obstacle.
 	 * 			| super.addSquare(square)
 	 * @effect	Puts a the begin duration in the duration map, increased by one.
 	 * 			One extra for the light trail at the players' square.
 	 * 			| durationMap.put(square, DURATION + 1)
 	 * @effect	Add an observer to the player of the light trail.
 	 * 			| owner.addObserver(this);
 	 */
 	@Override
 	public void addSquare(Square square) {
 		super.addSquare(square);
 		if(square != null) {
 			durationMap.put(square, DURATION + 1);
 			owner.addObserver(this);
 		}
 	}
 	
 	/**
 	 * Removes a square from the duration map.
 	 * 
 	 * @effect	This light trail is not subscribed to its owner anymore
 	 * 			if there are no parts left.
 	 * 			| if getSquares().length == 1 then
 	 * 			|	 owner.deleteObserver(this)
 	 * @effect	Removes the given square from the obstacle.
 	 * 			| super.removeSquare(square)
 	 * @effect	Removes the given square from the duration map.
 	 * 			| durationMap.remove(square)
 	 */
 	@Override
 	public void removeSquare(Square square) {
 		if(getSquares().length == 1)
 			owner.deleteObserver(this);
 		super.removeSquare(square);
 		durationMap.remove(square);
 	}
 	
 	/**
 	 * Check whether the given square is contiguous to the light trail's squares.
 	 * 
 	 * @param 	square
 	 * 			The square to check.
 	 * 
 	 * @return	True, if the given square is contiguous to this light trail.
 	 */
 	@Override
 	public boolean staysContiguousWith(Square square) {
 		for(Direction dir: Direction.values()) {
 			try {
 				
 				try {
 					boolean hasTeleporter = false;
 					for(Item it: getSquareList().getFirst().getItems()) {
 						if(it instanceof Teleporter) {
 							hasTeleporter = true;
 						}
 					}
 					
 					if(getSquareList().getFirst().getNeighbour(dir) == square || hasTeleporter) {
 						return true;
 					}
 				} catch (OutsideTheGridException e) {
 					// do nothing
 				}
 				
 				try {
 					boolean hasTeleporter = false;
 					for(Item it: getSquareList().getLast().getItems()) {
 						if(it instanceof Teleporter) {
 							hasTeleporter = true;
 						}
 					}
 					
 					if(getSquareList().getLast().getNeighbour(dir) == square || hasTeleporter) {
 						return true;
 					}
 				} catch (OutsideTheGridException e) {
 					// do nothing
 				}
 
 			} catch (NoSuchElementException e) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Updates the square in the duration map.
 	 * 
 	 * @param 	square
 	 * 			The square that has to be added or updated
 	 * 			in the duration map.
 	 * @param 	duration
 	 * 			The duration that the square still has left.
 	 * @effect	Puts a duration in the duration map.
 	 * 			| durationMap.put(square, duration)
 	 */
 	private void updateDurationMap(Square square, int duration) {
 		durationMap.put(square, duration);
 	}
 	
 	/**
 	 * Checks if the given square is in the duration map.
 	 * 
 	 * @param 	square
 	 * 			The square that has to be checked.
 	 * @return	True, if the square is in the duration map.
 	 * 			False, if the square is not in the duration map.
 	 * 			| durationMap.containsKey(square)
 	 */
 	public boolean isInDurationMap(Square square) {
 		return durationMap.containsKey(square);
 	}
 	
 	/**
 	 * Lowers the duration of all squares in the duration map
 	 * and removes the squares that have no duration left.
 	 * 
 	 * @effect	For each square, lower the current duration with 1.
 	 * 			| for each square in durationMap.keySet() do
 	 * 			|	 if duration - 1 > 0 then
 	 * 			|		 updateDurationMap(square, duration - 1)
 	 * @effect	For each square that has no duration left, remove
 	 * 			the square from the map.
 	 * 			| while(toRemove.contains(getSquareList().getFirst())) do
 	 *			| 	removeSquare(getSquareList().getFirst())
 	 */
 	private void lowerDuration() {
 		HashMap<Square, Integer> cloneMap = new HashMap<Square,Integer>(durationMap);	
 		ArrayList<Square> toRemove = new ArrayList<Square>();
 		for(Square square : cloneMap.keySet()) {
 			int currDuration = durationMap.get(square);
 			if(currDuration - 1 > 0) {
 				updateDurationMap(square, currDuration - 1);
 			} else {
 				toRemove.add(square);
 			}
 		}	
 		try {
 			while(toRemove.contains(getSquareList().getFirst())) {
 				removeSquare(getSquareList().getFirst());
 			}
 		} catch (NoSuchElementException e) {
 			return;
 		}
 	}
 
 	/**
 	 * This method returns whether an actor can enter this light trail.
 	 * 
 	 * @return 	False
 	 */
 	@Override
 	public boolean canEnter() {
 		return false;
 	}
 	
 	/**
 	 * This method returns whether an actor can leave this light trail.
 	 * 
 	 * @return 	True
 	 */
 	@Override
 	public boolean canLeave() {
 		return true;
 	}
 
 	/**
 	 * This method returns whether an obstacle can be combined with this Light trail.
 	 * 
 	 * @param	obstacle
 	 * 			The obstacle we want to combine.
 	 * @return	True if the obstacle is a force field.
 	 * 			| return obstacle instanceof ForceField
 	 */
 	@Override
 	public boolean canBeOnSquareWith(Obstacle obstacle) {
 		return (obstacle instanceof ForceField);
 	}
  
 	/**
 	 * Terminates this entire light trail.
 	 * 
 	 * @effect All squares in the map are removed.
 	 */
 	public void terminate() {
 		while(getDurationMap().size() > 0)
 			removeSquare(getSquareList().getFirst());
 	}
 	
 	/**
 	 * Update method that lowers the duration of this light trail.
 	 * 
 	 * @param	o
 	 * 			The observable object. In this case always a player.
 	 * @param	arg
 	 * 			Extra argument.
 	 * 
 	 * @effect	Lower the duration of the current light trail.
 	 * 			| lowerDuration()
 	 * @effect	Add a new part at the end of the light trail.
 	 * 			| addSquare(((Player) o).getLocation())
 	 * 
 	 * @note 	This method will be called by the observer.
 	 */
 	@Override
 	public void update(Observable o, Object arg) {
 		lowerDuration();
 		addSquare(((Player) o).getLocation());
 	}
 	
 }
