 package grid;
 
 import effect.*;
 import exception.*;
 import game.*;
 import item.*;
 
 import java.util.ArrayList;
 import java.util.HashSet;
import java.util.LinkedList;
 import java.util.Set;
 
 import coverage.*;
 
 
 /**
  * A class of squares. A square is part of a grid and 
  * can have a player, an obstacle or items on it.
  * It implements the events that can happen on this square.
  * 
  * @invar 	This square has a proper grid
  * 			| hasProperGrid()
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
 	 * The player that's on this square
 	 */
 	private Player player;
 	
 	/**
 	 * The obstacle that covers this square
 	 */
 	private Obstacle obstacle;
 	
 	/**
 	 * The coordinate of this square
 	 */
 	private Coordinate coordinate;
 	
 	/**
 	 * The items that are on this square
 	 */
 	private Set<Item> items = new HashSet<Item>();
 	
 	/**
 	 * Represents the power failure on this square.
 	 */
 	private PowerFailure powerFailure = null;
 	
 	/**
 	 * Represents the effects that this square has on stakeholders of the game.
 	 */
	private LinkedList<Effect> effects = new LinkedList<Effect>();
 
 	/**
 	 * Initialize this square with a given coordinate.
 	 * 
 	 * @param 	coordinate 
 	 * 			The coordinate of this square
 	 */
 	public Square(Coordinate coordinate){
 		setCoordinate(coordinate);
 	}
 	
 	/********************
 	 * GETTERS & SETTERS
 	 ********************/
 
 	/**
 	 * Returns the power failure on this square.
 	 * 	
 	 * @return	The power failure on this square.
 	 * 			| result == powerFailure
 	 */
 	public PowerFailure getPowerFailure() {
 		return powerFailure;
 	}
 	
 	/**
 	 * Set the power failure on this square to the given power failure.
 	 * 
 	 * @param 	powerFailure
 	 * 			The new power failure on this square.
 	 * 
 	 * @post	The power failure on this square is set to the new power failure.
 	 * 			| new.powerFailure = powerFailure
 	 */
 	public void setPowerFailure(PowerFailure powerFailure) {
 		this.powerFailure = powerFailure;			
 	}
 	
 	/**
 	 * Returns whether this square has a power failure or not.
 	 * 
 	 * @return	True, if this square has a power failure.
 	 * 			False, if not.
 	 * 			| result == (getPowerFailure() != null)
 	 */
 	public boolean hasPowerFailure() {
 		return getPowerFailure() != null;
 	}
 	
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
 	public void setGrid(Grid grid) {
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
 	 * Return the coordinate of this square
 	 */
 	public Coordinate getCoordinate() {
 		return coordinate;
 	}
 
 	/**
 	 * Set the coordinate of this square to the given coordinate
 	 * @param 	coordinate
 	 * 			The coordinate to set
 	 * @post 	The coordinate of this square is set to the given coordinate
 	 * 			|new.getCoordinate() == coordinate
 	 */
 	private void setCoordinate(Coordinate coordinate) {
 		this.coordinate = coordinate;
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
 	 * @return 	True iff it's an effective item, the item is compatible
 	 * 			with the items already on the square, the square has no Wall on it and
 	 * 			the square is not a starting position.
 	 * 			| result == (item != null) && item.canBeOnSquareWith(getItems()) 
 	 * 			| && !(obstacle instanceof Wall) && !getGrid().getStartingPositions().contains(this)
 	 */
 	public boolean canHaveAsItem(Item item){
 		return item != null && item.canBeOnSquareWith(getItems()) && !(obstacle instanceof Wall)
 				&& !getGrid().getStartingPositions().contains(this);
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
 		for(Item i: items){
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
 		if(this.hasAsItem(item)){
 			items.remove(item);
 			item.setLocation(null);
 		}
 		else{
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
 	public Obstacle getObstacle() {
 		return obstacle;
 	}
 
 	/**
 	 * Return whether this square has an obstacle on it
 	 * 
 	 * @return 	True iff it has an obstacle on it
 	 * 			| result == (getObstacle() != null)
 	 */
 	public boolean hasObstacle() {
 		return getObstacle() != null;
 	}
 
 	/**
 	 * Set the obstacle that covers this square
 	 * 
 	 * @param 	obstacle
 	 * 			The obstacle that covers this square
 	 * 
 	 * @pre 	If the given obstacle is effective, it does refer to
 	 * 		 	this square as one of the squares it covers
 	 * 			|(obstacle == null) || (obstacle.coversSquare(this))
 	 * @pre 	If the given obstacle is not effective and this square has 
 	 * 			an obstacle, this square can't have an obstacle that
 	 * 			covers this square
 	 * 			| ( (obstacle != null) || (getObstacle() == null)) || !getObstacle().coversSquare(this)
 	 * 
 	 * @post 	The obstacle is set to the given obstacle
 	 * 			| new.getObstacle() == obstacle
 	 */
 	public void setObstacle(Obstacle obstacle) {
 		assert (obstacle == null) || (obstacle.coversSquare(this));
 		assert ( (obstacle != null) || (getObstacle() == null)) || !getObstacle().coversSquare(this);
 		this.obstacle = obstacle;
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
 	 * Adds the effect to this square.
 	 * 
 	 * @param	e
 	 * 			The effect that has to be added to the list of effects.
 	 * 
 	 * @effect	The effect is added to the list of effects.
 	 * 			| effects.add(e)
 	 * @effect	The square on which the effect has effect is set to this square.
 	 * 			| e.setSquare(this)
 	 */
 	public void addEffect(Effect e) {
		if(e instanceof TeleporterEffect)
			effects.addLast(e);
		else
			effects.addFirst(e);
 		e.setSquare(this);
 	}
 	
 	/**
 	 * Removes the effect from this square.
 	 * 
 	 * @param	e
 	 * 			The effect that has to be removed from the list of effects.
 	 * 
 	 * @effect	The effect is removed from the list of effects.
 	 * 			| effects.remove(e)
 	 * @effect	The square on which the effect has effect is set to null.
 	 * 			| e.setSquare(null)
 	 */
 	public void removeEffect(Effect e) {
 		effects.remove(e);
 		e.setSquare(null);
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
 	 * Adds a power failure to this square.
 	 * 
 	 * @effect	Set the power failure on this square to a new power failure.
 	 * 			| setPowerFailure(new PowerFailure())
 	 * @effect	Set the location of the power failure to this location.
 	 * 			| getPowerFailure().setLocation(this)
 	 * @effect	Set the effect on this square to a combined effect with losing power.
 	 * 			| linkEffect(new PowerFailureEffect())
 	 */
 	public void addPowerFailure() {
 		setPowerFailure(new PowerFailure());
 		getPowerFailure().setLocation(this);
 		linkEffect(new PowerFailureEffect());
 	}
 	
 	/**
 	 * Removes a power failure to this square.
 	 * 
 	 * @effect	Set the location of the power failure to null.
 	 * 			| getPowerFailure().setLocation(null)
 	 * @effect	Set the power failure on this square to null
 	 * 			| setPowerFailure(null)
 	 * @effect	Set the effect on this square to a combined effect without losing power.
 	 * 			| unlinkEffect(new PowerFailureEffect());
 	 */
 	public void removePowerFailure() {
 		getPowerFailure().setLocation(null);
 		setPowerFailure(null);
 		unlinkEffect(new PowerFailureEffect());
 	}
 
 	/**
 	 * Make this square lose its power, or lose even more power.
 	 * 
 	 * @effect	If this square already has a power failure, make it even worse.
 	 * 			| if hasPowerFailure() then
 	 * 			|	 getPowerFailure().losePower()
 	 * @effect	If this square has not got any power failure yet, then add a power failure.
 	 * 			| if !hasPowerFailure() then
 	 * 			| 	 addPowerFailure()
 	 */
 	public void losePower() {
 		if(hasPowerFailure()) {
 			getPowerFailure().losePower();
 		} else {
 			addPowerFailure();
 		}
 	}
 	
 	/**
 	 * Win the power of this square a bit back, if it has a power failure.
 	 * 
 	 * @effect	If the square has a power failure, lower the turns of the power failure.
 	 * 			| if hasPowerFailure() then
 	 * 			|	 getPowerFailure().lowerTurnsLeft()
 	 */
 	public void winPower() {
 		if(hasPowerFailure())
 			getPowerFailure().lowerTurnsLeft();
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
 		for(Item item: items) {
 			if(!item.canBeSteppedOn())
 				canBeSteppedOnItems = false;
 		}
 		return !hasObstacle() && !hasPlayer() && canBeSteppedOnItems;
 	}
 
 	/**
 	 * Link the given effect to the effects on this square.
 	 * 
 	 * @param	effect
 	 * 			The effect that has to be linked.
 	 * 
 	 * @effect	If the given effect can be combined with other effects,
 	 * 			then combine it. Otherwise, add the effect as an apartly from the others.
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
