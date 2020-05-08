 package grid;
 
 import effect.*;
 import exception.*;
 import game.*;
 import item.*;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Set;
 
 import obstacle.*;
 
 
 
 /**
  * A class of squares. A square is part of a grid and 
  * can have a player, an obstacle or items on it.
  * It implements the events that can happen on this square.
  * 
  * @invar 	This square has a proper grid
  * 			| hasProperGrid()
  * @invar This square has a valid coordinate
  * 			| isValidCoordinate(getCoordinate())
  * 			
  * 
  * @author 	Groep 8
  * @version April 2013
  */
 public class Square implements Event {
 	
 	/**
 	 * The grid that owns this square
 	 */
 	private Grid grid;
 	
 	/**
 	 * The coordinate of this square
 	 */
 	private Coordinate coordinate;
 	
 	/**
 	 * The player that's on this square
 	 */
 	private Player player;
 	
 	/**
 	 * The obstacle that covers this square
 	 */
 	private Set<Obstacle> obstacles = new HashSet<Obstacle>();
 	
 	/**
 	 * The items that are on this square
 	 */
 	private Set<Item> items = new HashSet<Item>();
 	
 	/**
 	 * Represents the effects that this square has on stakeholders of the game.
 	 */
 	private LinkedList<Effect> effects = new LinkedList<Effect>();
 
 	
 	/********************
 	 * GETTERS & SETTERS
 	 ********************/
 	
 	/**
 	 * Return the grid that owns this square.
 	 */
 	public Grid getGrid() {
 		return grid;
 	}
 
 	/**
 	 * Set the grid that owns this square
 	 * 
 	 * @param 	grid
 	 * 			The grid that owns this square
 	 * 
 	 * @pre 	If the given grid is effective, it does refer to this square as its square
 	 * 			| (grid == null) || (grid.hasAsSquare(this))
 	 * @pre 	If the given grid is not effective and this square has a grid, this square can't have a grid that
 	 * 			refers to this square
 	 * 			| ( (grid != null) || (getGrid() == null)) || !getGrid().hasAsSquare(this)
 	 * 
 	 * @post	The grid is set to the given grid
 	 * 			| new.getGrid() == grid
 	 */
 	protected void setGrid(Grid grid) {
 		assert (grid == null) || (grid.hasAsSquare(this));
 		assert ( (grid != null) || (getGrid() == null)) || !getGrid().hasAsSquare(this);
 		this.grid = grid;
 	}
 
 	/**
 	 * Check whether this square has a proper grid
 	 * 
 	 * @return 	True iff the grid is effective and contains this square
 	 * 			| result == ((getGrid() != null) && getGrid().hasAsSquare(this))
 	 */
 	public boolean hasProperGrid(){
 		return (getGrid() != null) && getGrid().hasAsSquare(this);
 	}
 
 	/**
 	 * Return the coordinate of this square
 	 */
 	public Coordinate getCoordinate() {
 		return coordinate;
 	}
 
 	/**
 	 * Set the coordinate of this square to the given coordinate
 	 * 
 	 * @param 	coordinate
 	 * 			The coordinate to set
 	 * 
 	 * @post 	The coordinate of this square is set to the given coordinate
 	 * 			|new.getCoordinate() == coordinate
 	 * 
 	 * @throws	IllegalArgumentException
 	 * 			The coordinate is null
 	 */
 	public void setCoordinate(Coordinate coordinate) {
 		if(!isValidCoordinate(coordinate)){
 			throw new IllegalArgumentException();
 		}
 		this.coordinate = coordinate;
 	}
 
 	/**
 	 * Check whether the given coordinate is valid
 	 * 
 	 * @param 	coordinate 
 	 * 			The coordinate to check
 	 * 
 	 * @return 	True iff the coordinate is not null
 	 */
 	public static boolean isValidCoordinate(Coordinate coordinate){
 		return coordinate != null;
 	}
 	
 	/**
 	 * Return the player on this square
 	 */
 	public Player getPlayer() {
 		return player;
 	}
 
 	/**
 	 * Set the player that's on this square to the given player
 	 * 
 	 * @param	player
 	 * 			The player to place on this square
 	 * 
 	 * @pre 	If the given player is effective it does refer to this square as its location
 	 * 			| (player == null) || (player.getLocation() == this)
 	 * @pre 	If the given player is not effective and this square has a player on it, this
 	 * 			square can't have a player that refers to this square
 	 * 			| ( (player != null) || (! hasPlayer()) ) || (getPlayer().getLocation() != this)
 	 * 
 	 * @post 	The new player of this square is set to the given player
 	 * 			| new.getPlayer() == player 
 	 */
 	public void setPlayer(Player player) {
 		assert (player == null) || (player.getLocation() == this);
 		assert ( (player != null) || (! hasPlayer()) ) || (getPlayer().getLocation() != this);
 		this.player = player;
 	}	
 	
 	/**
 	 * Return whether this square has a player on it
 	 * 
 	 * @return 	True iff there is a player on this square.
 	 * 			|result == (getPlayer()!=null)
 	 */
 	public boolean hasPlayer(){
 		return getPlayer() != null;
 	}
 	
 	/**
 	 * Return the number of items on this square
 	 * 
 	 * @return 	The number of items on this square
 	 * 			|result == (this.items.size())
 	 */
 	public int getNbItems(){
 		return this.items.size();
 	}
 	
 	/**
 	 * Check whether this square has the given item as one of its items
 	 * 
 	 * @param 	item
 	 * 			The item to check
 	 * 
 	 * @return 	True iff this square has the given item as one of its items
 	 * 			| result == (items.contains(item)
 	 */
 	public boolean hasAsItem(Item item){
 		return items.contains(item);
 	}
 	
 	/**
 	 * Check whether this square can have the given item as one of its items
 	 * 
 	 * @param 	item
 	 * 			The item to check
 	 * 
 	 * @return 	True iff it's an effective item, and the item is compatible with this square.
 	 * 			| result == (item != null) && item.canBeOnSquareWith(this)
 	 */
 	public boolean canHaveAsItem(Item item){
 		return item != null && item.canBeOnSquare(this);
 	}
 	
 	/**
 	 * Check whether this square has proper items on it.
 	 * 
 	 * @return 	True iff this square can have each item that's on it
 	 * 			|result ==
 	 * 			|for each item in items
 	 * 			|	canHaveAsItem(item)
 	 */
 	public boolean hasProperItems(){
 		for(Item i: items) {
 			if(!canHaveAsItem(i)){
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Add an item to this square
 	 * @param 	item
 	 * 			The item to add to this square
 	 * 
 	 * @effect 	If this square can have the given item as one of its items
 	 * 			it is added to this square
 	 * 			| items.add(item)
 	 * @effect	If this square can have the given item as one of its items,
 	 * 			then the location of that item is set to this square.
 	 * 			| item.setLocation(this)
 	 * 
 	 * @throws 	IllegalArgumentException
 	 * 			This square cannot have the given item as one of its items
 	 */
 	public void addItem(Item item){
 		if(!canHaveAsItem(item)){
 			throw new IllegalArgumentException();
 		}
 		items.add(item);
 		item.setLocation(this);
 		if(item instanceof PortableItem)
 			((PortableItem) item).onAdd();
 	}
 	
 	/**
 	 * Remove an item from this square.
 	 * 
 	 * @param 	item
 	 * 			The item to remove.
 	 * 
 	 * @effect  If this square contains the given item, it is removed from this square.
 	 * 			| items.remove(item)
 	 * @effect	If this square contains the given item, then the location
 	 * 			of that item is set to null.
 	 * 			| item.setLocation(null)
 	 * 
 	 * @throws 	NoItemException
 	 * 			The item is not on this square
 	 */
 	public void removeItem(Item item) throws NoItemException{
 		if(hasAsItem(item)){
 			items.remove(item);
 			item.setLocation(null);
 			if(item instanceof PortableItem)
 				((PortableItem) item).onRemove();			
 		} else {
 			throw new NoItemException();
 		}
 	}
 	
 	/**
 	 * Return an array containing the items on this square
 	 * 
 	 * @return 	An array containing the items on this square
 	 */
 	public Item[] getItems() {
 		return items.toArray(new Item[getNbItems()]);
 	}
 	
 	/**
 	 * Return an array containing the visible portable items on this square
 	 * 
 	 * @return 	An array containing the visible portable items on this square
 	 */
 	public PortableItem[] getVisiblePortableItems() {
 		ArrayList<PortableItem> itemList = new ArrayList<PortableItem>();
 		for(Item i: items){
 			if(i.isVisible() && i instanceof PortableItem){
 				itemList.add((PortableItem) i);
 			}
 		}
 		return itemList.toArray(new PortableItem[itemList.size()]);
 	}
 	
 	/**
 	 * Return the obstacle that's on this square
 	 */
 	public Obstacle[] getObstacles() {
 		return obstacles.toArray(new Obstacle[obstacles.size()]);
 	}
 
 	/**
 	 * Return whether this square has an obstacle on it
 	 * 
 	 * @return 	True iff it has an obstacle on it
 	 * 			| result == (getObstacle() != null)
 	 */
 	public boolean hasObstacle() {
 		return obstacles.size() > 0;
 	}
 	
 	public boolean hasAsObstacle(Obstacle obstacle) {
 		return obstacles.contains(obstacle);
 	}
 
 	/**
 	 * TODO: commentaar
 	 */
 	public void addObstacle(Obstacle obstacle) {
 		if(canHaveAsObstacle(obstacle)) {
 			obstacles.add(obstacle);
 		} else throw new IllegalArgumentException();
 	}
 	
 	/**
 	 * TODO: commentaar
 	 */
 	public void removeObstacle(Obstacle obstacle) {
 		if(obstacles.contains(obstacle) && obstacle != null) {
 			obstacles.remove(obstacle);
 		} else throw new IllegalArgumentException();
 	}
 	
 	public boolean canHaveAsObstacle(Obstacle obstacle) {
 		for(Obstacle obs: obstacles)
 			if(!obs.canBeOnSquareWith(obstacle))
 				return false;
 		return true;
 	}
 
 	/**
 	 * Returns the effects of this square.
 	 * 
 	 * @return	The effects of this square.
 	 */
 	public Effect[] getEffects() {
 		return effects.toArray(new Effect[effects.size()]);
 	}
 	
 	/**
 	 * Link the given effect to the effects on this square.
 	 * 
 	 * @param	effect
 	 * 			The effect that has to be linked.
 	 * 
 	 * @effect	If the given effect can be combined with other effects,
 	 * 			then combine it. Otherwise, add the effect apart from the others.
 	 */
 	public void linkEffect(Effect effect) {
 		boolean combined = false;
 		for(Effect e: effects)
 			if(e.canCombineWith(effect))
 				combined = true;
 		
 		if(combined) {
 			HashSet<Effect> eclone = new HashSet<Effect>(effects);
 			for(Effect e: eclone)
 				e.linkEffect(effect);
 		} else addEffect(effect);
 	}
 
 	/**
 	 * Unlink  the given effect from the effects on this square.
 	 * 
 	 * @param	effect
 	 * 			The effect that has to be unlinked.
 	 * 
 	 * @effect	Unlink the effect from all other effects (split).
 	 */
 	public void unlinkEffect(Effect effect)	{
 		HashSet<Effect> eclone = new HashSet<Effect>(effects);
 		for(Effect e: eclone)
 			e.unlinkEffect(effect);
 	}
 
 	/**
 	 * Adds the effect to this square.
 	 * 
 	 * @param	effect
 	 * 			The effect that has to be added to the list of effects.
 	 * 
 	 * @effect	The effect is added to the list of effects.
 	 * 			| effects.add(effect)
 	 * @effect	The square on which the effect has effect is set to this square.
 	 * 			| effect.setSquare(this)
 	 * 
 	 * @note	Use linkEffect to add effects to a square.
 	 */
 	public void addEffect(Effect effect) {
 		Iterator<Effect> it = effects.iterator();
 		while(it.hasNext()) {
 			Effect element = it.next();
 			if(effect.isPriorTo(element)) {
 				effects.add(effects.indexOf(element), effect);
 				break;
			} else if(!it.hasNext()) {
				effects.add(effect);
 			}
 		}
 		
 		effect.setSquare(this);
 	}
 	
 	/**
 	 * Removes the effect from this square.
 	 * 
 	 * @param	effect
 	 * 			The effect that has to be removed from the list of effects.
 	 * 
 	 * @effect	The effect is removed from the list of effects.
 	 * 			| effects.remove(effect)
 	 * @effect	The square on which the effect has effect is set to null.
 	 * 			| effect.setSquare(null)
 	 * 
 	 * @note	Use unlinkEffect to remove effects from a square.
 	 */
 	public void removeEffect(Effect effect) {
 		effects.remove(effect);
 		effect.setSquare(null);
 	}
 	
 	
 	/********************
 	 * CORE FUNCTIONALITY
 	 ********************/
 
 	/**
 	 * Return the square that is in the given direction of the given square.
 	 * 
 	 * @param 	direction
 	 *  		The direction of the square you want relative to the given square.
 	 * @return 	The square at the given direction of the given square.
 	 * 			| result == getSquareAtDirection(this, direction)
 	 * @throws	OutsideTheGridException [must]
 	 * 			When there is no neighbour and you would get of the grid.
 	 */
 	public Square getNeighbour(Direction direction) throws OutsideTheGridException {
 		return getGrid().getSquareAtDirection(this, direction);
 	}
 	
 	/**
 	 * Returns whether a move in the given direction is a valid move or not.
 	 * 
 	 * @param 	dir
 	 * 			The direction in which you want to move.
 	 * 
 	 * @return	True, if this is a valid move. False, otherwise.
 	 * 			| getGrid().isValidMove(this, dir)
 	 */
 	public boolean isValidMove(Direction dir) {
 		return getGrid().isValidMove(this, dir);
 	}
 	
 	/**
 	 * Returns whether a player can step on this square.
 	 * 
 	 * @return	True, if there is no obstacle, no player and
 	 * 			the player can step on all items.
 	 */
 	public boolean canBeSteppedOn() {
 		boolean canBeSteppedOnItems = true;
 		boolean canBeSteppedOnObstacle = true;
 		
 		for(Item item: items) {
 			if(!item.canBeSteppedOn())
 				canBeSteppedOnItems = false;
 		}
 		for(Obstacle obstacle: obstacles) {
 			if(!obstacle.canEnter())
 				canBeSteppedOnObstacle = false;
 		}
 		
 		return canBeSteppedOnObstacle && !hasPlayer() && canBeSteppedOnItems;
 	}
 	
 	public boolean canBeLandedOn() {
 		boolean canBeLandedOnItems = true;
 		boolean canBeLandedOnObstacle = true;
 		
 		for(Item item: items) {
 			if(!item.canBeLandedOn())
 				canBeLandedOnItems = false;
 		}
 		for(Obstacle obstacle: obstacles) {
 			if(!obstacle.canEnter())
 				canBeLandedOnObstacle = false;
 		}
 		
 		return canBeLandedOnObstacle && !hasPlayer() && canBeLandedOnItems;
 	}
 
 	/**
 	 * Returns whether a player can leave this square.
 	 */
 	public boolean canBeLeaved() {
 		for(Obstacle obstacle : obstacles) {
 			if(!(obstacle.canLeave()))
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Defines the on-step action of this square.
 	 * It executes the effects that are bound on the square,
 	 * and the effects it has on each item.
 	 * 
 	 * @effect	Execute each effect that is bound on the square.
 	 * 			| for each effect in effects
 	 * 			| 	 effect.onStep(actor)
 	 * @effect	For each item on this square,
 	 * 			execute their on-step action.
 	 * 			| for each item in items do
 	 * 			|	 item.onStep(actor)
 	 */
 	public void onStep(Actor actor) {
 		LinkedList<Effect> eclone = new LinkedList<Effect>(effects);
 		for(Effect effect: eclone)
 			effect.onStep(actor);
 		
 		HashSet<Item> iclone = new HashSet<Item>(items);
 		for(Item item : iclone) {
 			item.onStep(actor);
 		}
 	}
 	
 	/**
 	 * Defines the on-leave action of this square.
 	 * It executes the effects that are bound on the square,
 	 * and the effects it has on each item.
 	 * 		
 	 * @effect	Execute each on-leave effect that is bound on the square.
 	 * 			| for each effect in effects
 	 * 			| 	 effect.onLeave(actor)
 	 * @effect	For each item on this square,
 	 * 			execute their on-leave action.
 	 * 			| for each item in items do
 	 * 			| 	 item.onLeave()
 	 */
 	public void onLeave(Actor actor) {
 		LinkedList<Effect> eclone = new LinkedList<Effect>(effects);
 		for(Effect effect: eclone)
 			effect.onLeave(actor);
 		
 		HashSet<Item> iclone = new HashSet<Item>(items);
 		for(Item item : iclone) {
 			item.onLeave(actor);
 		}
 	}
 
 	/**
 	 * Defines the on-start action of this square.
 	 * It executes the effects that are bound on the square,
 	 * and the effects it has on each item.
 	 * 	
 	 * @effect	Execute each on-start effect that is bound on the square.
 	 * 			| for each effect in effects
 	 * 			| 	 effect.onStart(actor)
 	 * @effect	For each item on this square,
 	 * 			execute their on-start action.
 	 * 			| for each item in items do
 	 * 			| 	 item.onStart(actor)
 	 */
 	public void onStart(Actor actor) {
 		LinkedList<Effect> eclone = new LinkedList<Effect>(effects);
 		for(Effect effect: eclone)
 			effect.onStart(actor);
 		
 		HashSet<Item> iclone = new HashSet<Item>(items);
 		for(Item item : iclone) {
 			item.onStart(actor);
 		}
 	}
 	
 	/**
 	 * Defines the on-land action of this square.
 	 * It executes the effects that are bound on the square,
 	 * and the effects it has on each item.
 	 * 	
 	 * @effect	Execute each on-land effect that is bound on the square.
 	 * 			| for each effect in effects
 	 * 			| 	 effect.onLand(actor)
 	 * @effect	For each item on this square,
 	 * 			execute their on-land action.
 	 * 			| for each item in items do
 	 * 			| 	 item.onLand(actor)
 	 */
 	@Override
 	public void onLand(Actor actor) {
 		LinkedList<Effect> eclone = new LinkedList<Effect>(effects);
 		for(Effect effect: eclone)
 			effect.onLand(actor);
 		
 		HashSet<Item> iclone = new HashSet<Item>(items);
 		for(Item item: iclone)
 			item.onLand(actor);
 	}
 	
 	/**
 	 * Returns a string representation of this square.
 	 * 
 	 * @return 	A string with information about the coordinate of this square.
 	 * 			| result.equals("square at coordinate "+this.getCoordinate())
 	 * 
 	 * @see 	java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "square at " + this.getCoordinate();
 	}
 	
 	
 	/**
 	 * Temporary marker for testing purposes.
 	 */
 	public boolean marker;
 	
 }
