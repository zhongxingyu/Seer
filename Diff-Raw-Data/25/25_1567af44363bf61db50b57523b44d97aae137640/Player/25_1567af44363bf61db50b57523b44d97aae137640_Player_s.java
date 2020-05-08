 package game;
 
 import coverage.*;
 import grid.*;
 import item.*;
 import exception.*;
 
 /**
  * This class represents a player in the game.
  * 
  * @invar 	The player has a valid inventory
  * 			| canHaveAsInventory(this.getInventory())
  * 
  * @author 	Groep 8
  * @version April 2013
  */
 public class Player implements Actor {
 	
 	/**
 	 * TODO
 	 */
 	private TurnHandler turnHandler;
 	
 	/**
 	 * Represents the current location (current square) of the player.
 	 */
 	private Square location;
 	
 	/**
 	 * Represents the light trail of the player.
 	 */
 	private LightTrail lightTrail;
 	
 	/**
 	 * Represents the inventory of the player.
 	 */
 	private final Inventory inventory;
 	
 	/**
 	 * This represents the number of actions this player has
 	 * still left in this turn.
 	 */
 	private int actionsLeft = NUMBER_OF_ACTIONS;
 	
 	/**
 	 * This represents the maximum number of actions a player can do
 	 * in one turn.
 	 */
 	public static final int NUMBER_OF_ACTIONS = 3;
 	
 	/**
 	 * Represents the colour of this player in the game.
 	 */
 	private final PlayerColour playerColour;
 	
 	/**
 	 * Represents the starting position of this player in the game.
 	 */
 	private final Square startingPosition;
 	
 	/**
 	 * Represents the damage in actions a player has.
 	 * The player has to lower his number of actions with
 	 * this number of actions in a next turn.
 	 */
 	private int actionDamage = 0;
 	
 	/**
 	 * Indicating whether this player has moved in the current turn
 	 */
 	private boolean hasMoved;
 	
 	/**
 	 * Indicating whether this player must skip its next turn
 	 */
 	private boolean skipsTurn;
 	
 	/**
 	 * Variable indicating whether this player has received damage.
 	 */
 	private boolean receivedDamage;
 	
 	/**
 	 * Creates a new player with a given location.
 	 * 
 	 * @param	startingPosition
 	 * 			The initial location of the player.
 	 * @param	playerColour
 	 * 			The initial colour of the player
 	 * 
 	 * @post	The player colour is set to the given colour.
 	 * 			| new.playerColour = playerColour
 	 * @post	The starting position is set to the given position.
 	 * 			| new.startingPosition = startingPosition
 	 * @effect	The location of the player is set to the starting position.
 	 * 			| changeLocation(startingPosition)
 	 * @effect	The light trail is initialized.
 	 * 			| setLightTrail(new LightTrail())
 	 * @effect	Initialize the inventory.
 	 * 			| inventory = new Inventory()
 	 * 			| inventory.setOwner(this)
 	 * 
 	 */
 	public Player(Square startingPosition, PlayerColour playerColour) {
 		this.playerColour = playerColour;
 		this.startingPosition = startingPosition;
 		changeLocation(startingPosition);
 		setLightTrail(new LightTrail(getLocation()));;
 		inventory = new Inventory();
 		inventory.setOwner(this);
 	}
 	
 	/*******************
 	 * GETTERS & SETTERS
 	 *******************/
 	
 	/**
 	 * Return the starting position of the player in the game.
 	 * 
 	 * @return	The starting position of this player.
 	 */
 	public Square getStartingPosition() {
 		return startingPosition;
 	}
 	
 	/**
 	 * Returns the colour of the player in the game.
 	 * 
 	 * @return	The colour of the player in the game.
 	 */
 	public PlayerColour getPlayerColour() {
 		return playerColour;
 	}
 	
 	/**
 	 * Returns the location square on which this player stands.
 	 * 
 	 * @return The square on which this player stands.
 	 */
 	public Square getLocation() {
 		return location;
 	}
 
 	/**
 	 * Set the location of this player to the given location.
 	 * 
 	 * @param 	location
 	 * 			The new location of this player
 	 * 
 	 * @post	The current location of the player is set to the given location.
 	 * 			| new.location = location
 	 * 
 	 * @throws 	IllegalArgumentException
 	 * 			If the given square is not a valid location
 	 * 			for this player.
 	 */
 	private void setLocation(Square location) throws IllegalArgumentException{
 		if(!canHaveAsNewLocation(location)){
 			throw new IllegalArgumentException("Invalid square!");
 		}
 		this.location = location;
 	}
 	
 	/**
 	 * Return the light trail of this player
 	 * 
 	 * @return	The light trail of this player
 	 */
 	public LightTrail getLightTrail() {
 		return lightTrail;
 	}
 	
 	/**
 	 * Set the light trail of this player to the given light trail.
 	 * 
 	 * @param 	lightTrail
 	 * 			The new light trail to set.
 	 * 
 	 * @post 	This player has the given light trail as its light trail
 	 * 			| new.getLightTrail() == lightTrail
 	 * @post 	The given light trail has this player as its player
 	 * 			| new.getLightTrail().getPlayer() == this
 	 * 
 	 * @throws 	IllegalArgumentException 
 	 * 			If the lightTrail is not a valid light trail.
 	 */
 	private void setLightTrail(LightTrail lightTrail) throws IllegalArgumentException{
 		if(!canHaveAsLightTrail(lightTrail)){
 			throw new IllegalArgumentException();
 		}
 		this.lightTrail = lightTrail;
 		this.lightTrail.setPlayer(this);
 	}
 	
 	/**
 	 * Returns the item inventory of the player.
 	 * @return	The inventory of the player.
 	 */
 	
 	public Inventory getInventory() {
 		return inventory;
 	}
 
 	/**
 	 * Return the amount of actions this player has left.
 	 * 
 	 * @return	The amount of actions this player has left.
 	 */
 	public int getActionsLeft() {
 		return actionsLeft;
 	}
 
 	/**
 	 * Sets the actions left to the given number of actions.
 	 * 
 	 * @param 	actionsLeft
 	 * 			The new number of actions the player has left.
 	 */
 	private void setActionsLeft(int actionsLeft) {
 		this.actionsLeft = actionsLeft;
 	}
 	
 	/**
 	 * Returns the damage to the number of actions a player will receive
 	 * in future turns.
 	 * 
 	 * @return	The action damage.
 	 */
 	public int getActionDamage() {
 		return actionDamage;
 	}
 
 	/**
 	 * Sets the action damage to the given number of actions.
 	 * 
 	 * @param 	actionDamage
 	 * 			The new action damage.
 	 * 
 	 * @post	The new action damage is set to the given action damage.
 	 * 			| this.actionDamage = actionDamage
 	 */
 	protected void setActionDamage(int actionDamage) {
 		this.actionDamage = actionDamage;
 	}
 	
 	/**
 	 * Return whether the player has moved
 	 * @return 	True if the player has moved.
 	 */
 	public boolean hasMoved(){
 		return hasMoved;
 	}
 	
 	/**
 	 * Set whether the player has moved.
 	 * 
 	 * @param 	moved
 	 * 			A boolean indicating whether the player has moved
 	 * 			
 	 * @post 	This players's moved status is set to moved
 	 * 			| new.hasMoved() == moved
 	 */
 	public void setHasMoved(boolean moved){
 		hasMoved = moved;
 	}
 	
 	/**
 	 * TODO
 	 */
 	public boolean skipsTurn() {
 		return skipsTurn;
 	}
 
 	/**
 	 * TODO
 	 */
 	public void skipTurn() {
 		skipsTurn = true;
 		emptyActions();
 	}
 	
 	/**
 	 * TODO
 	 */
 	private void resetSkipsTurn() {
 		skipsTurn = false;
 	}
 
 	/**
 	 * Check whether this player can be positioned on the given square.
 	 * 
 	 * @param 	location
 	 * 			The square to check.
 	 * 
 	 * @return 	True iff the square is effective and has no obstacle or player on it
 	 * 			| location != null && location.hasObstacle() && 
 	 * 		 	| (!location.hasPlayer() || location.getPlayer() == this)
 	 */
 	public boolean canHaveAsNewLocation(Square location){
 		return location != null && !location.hasObstacle() && 
 				(!location.hasPlayer() || location.getPlayer() == this);
 	}
 	
 	/**
 	 * Change the location of this player to the given location.
 	 * 
 	 * @param 	location
 	 * 			The new location of this player.
 	 * 
 	 * @effect	Set the location to the new location.
 	 * 			| setLocation(location)
 	 * @effect	Set the player on this location, to this player.
 	 * 			| location.setPlayer(this)
 	 * @effect	Set the player of the previous location, to null;
 	 * 			| previousLocation.setPlayer(null)
 	 * 
 	 * @throws 	IllegalArgumentException 
 	 * 			If the given square is not a valid square to move to.
 	 */
 	protected void changeLocation(Square location) throws IllegalArgumentException{
 		Square previousLocation = getLocation();
 		setLocation(location);
 		location.setPlayer(this);
 		if(previousLocation != null){
 			previousLocation.setPlayer(null);
 		}
 	}
 	
 	/**
 	 * Checks whether the given lightTrail is a valid light trail for this player
 	 * 
 	 * @param 	lightTrail
 	 * 			The light trail to check.
 	 * 
 	 * @return 	True iff it's an effective light trail
 	 * 			| lightTrail != null
 	 */
 	public boolean canHaveAsLightTrail(LightTrail lightTrail){
 		return lightTrail != null;
 	}
 	
 	/**
 	 * Checks whether the given inventory is a valid inventory for this player
 	 * 
 	 * @param 	inventory
 	 * 			The inventory to check.
 	 * 
 	 * @return 	True iff it's an effective inventory
 	 * 			| result == (inventory != null)
 	 */
 	public boolean canHaveAsInventory(Inventory inventory){
 		return inventory != null;
 	}
 	
 	/**
 	 * Checks whether the player has received damage or not.
 	 * 
 	 * @return	True, if the player has received damage.
 	 * 			False, if not.
 	 * 			| result == receivedDamage
 	 */
 	public boolean hasReceivedDamage() {
 		return receivedDamage;
 	}
 	
 	/**
 	 * Sets whether the player has received damage or not.
 	 * 
 	 * @param 	receivedDamage
 	 * 			True, if the player has received damage. False, otherwise.
 	 * 
 	 * @post	The receivedDamage-boolean is set to the given value.
 	 * 			| new.receivedDamage = receivedDamage
 	 */
 	public void setReceivedDamage(Boolean receivedDamage) {
 		this.receivedDamage = receivedDamage;
 	}
 
 
 	/*********************
 	 * CORE FUNCTIONALITY
 	 *********************/
 	
 	/**
 	 * Pick up the given item.
 	 * 
 	 * @param 	item
 	 * 			The item that has to be picked up.
 	 * 
 	 * @effect	An action is done.
 	 * 			| doAction()
 	 * @effect	The item is removed from the location.
 	 * 			| getLocation().removeItem(item)
 	 * @effect	The item is added to the inventory
 	 * 			| getInventory().addToInventory(item)
 	 * 
 	 * @throws 	NoItemException
 	 * 			If the item is not present.
 	 * @throws 	OverCapacityException
 	 * 			If the inventory is full.
 	 * @throws 	NoMoreActionsException
 	 * 			If the player has no more actions left.
 	 */
 	public void pickUpItem(PortableItem item) throws NoItemException, OverCapacityException, NoMoreActionsException {
 		doAction();
 		getLocation().removeItem(item);
 		getInventory().addToInventory(item);
 		turnHandler.checkTurn();
 	}
 	
 	/**
 	 * Use the given item.
 	 * 
 	 * @param 	item
 	 * 			The item that must be used.
 	 * 
 	 * @effect	Do an action.
 	 * 			| doAction()
 	 * @effect	Let the item be used.
 	 * 			| item.use()
 	 * @effect	Delete the given item from the inventory.
 	 * 			| getInventory().deleteFromInventory(item)
 	 * @effect 	Add the given item to the current location of this player
 	 * 			| getLocation().addItem(item)
 	 * 
 	 * @throws 	NoItemException
 	 * 			If the item is not present.
 	 * @throws 	NoMoreActionsException
 	 * 			If the player has no actions left.
 	 */
 	public void useItem(PortableItem item) throws NoItemException, NoMoreActionsException {
 		if(!getInventory().containsItem(item)) {
 			throw new NoItemException();
 		} else {
 			doAction();
 			item.use();
 			getInventory().deleteFromInventory(item);	
 			turnHandler.checkTurn();
 		}
 	}
 	
 	/**
 	 * Move this player to square at the given direction.
 	 * 
 	 * @param 	dir
 	 * 			The direction to move to.
 	 * 
 	 * @effect 	Do an action
 	 * 			| doAction()
 	 * @effect	On leaving the current Square, do the on-leave action.
 	 * 			| getLocation().onLeave(this)
 	 * @effect 	Change the location of this player to the square at the given direction
 	 * 			| changeLocation(getLocation().getNeighbour(dir))
 	 * @effect	On entering the new square, to the on-stop action.
 	 * 			| new.getLocation().onStep(this)
 	 * @effect	Add the location of the player to the duration map.
 	 * 			| getLightTrail().addSquare(new.getLocation())
 	 * @effect	The player has moved.
 	 * 			| setHasMoved(true)
 	 * 
 	 * @throws 	NoMoreActionsException
 	 * 			There are no actions left.
 	 * @throws 	OutsideTheGridException
 	 * 			The square you want to move to is outside the grid.
 	 * @throws	InvalidMoveException
 	 * 			If the move is not a valid move in the grid.
 	 * 			| if !getLocation().isValidMove(dir) then 
 	 * 			|	 throw InvalidMoveException
 	 */
 	@Override
 	public void move(Direction dir) 
 			throws NoMoreActionsException, OutsideTheGridException, InvalidMoveException {
 		if(!getLocation().isValidMove(dir)) throw new InvalidMoveException();
 		doAction();
 		getLocation().onLeave(this);
 		changeLocation(getLocation().getNeighbour(dir));
 		getLocation().onStep(this);
 		getLightTrail().addSquare(getLocation());
 		setHasMoved(true);
 		turnHandler.checkWin();
 		turnHandler.checkLose();
 		turnHandler.checkTurn();
 	}
 	
 	/**
 	 * Teleport the player to the given teleporter.
 	 * 
 	 * @param 	destination
 	 * 			Teleporter to which the player has to be teleported.
 	 * 
 	 * @effect	Add the lighttrail to the square before teleporting.
 	 * 			| getLightTrail().addSquare(getLocation())
 	 * @effect	Change the location of the player to the square of
 	 * 			the destination teleporter.
 	 * 			| changeLocation(destination.getLocation())
 	 * @effect	Execute the on-land action of the destination square.
 	 * 			| getLocation().onLand(this)
 	 */
 	@Override
 	public void teleport(Teleporter destination) {
 		getLightTrail().addSquare(getLocation());
 		changeLocation(destination.getLocation());
 		getLocation().onLand(this);
 	}
 	
 	/**
 	 * Apply the action damage to this player.
 	 * 
 	 * @param	damage
 	 * 
 	 * @effect	The action damage is set to the initial number of actions,
 	 * 			minus the actions that the player has left.
 	 * @effect	Perform empty actions for all the actions the player has left.
 	 * 			| emptyActions()
 	 */
 	public void applyActionDamage(int damage) {
 		setActionDamage(damage - getActionsLeft());
 		emptyActions();
 	}		
 	
 	/**
 	 * Do an action. 
 	 * 
 	 * @effect	Lower the duration of the whole light trail.
 	 * 			| getLightTrail().lowerDuration()
 	 * @effect	The player has an action less left.
 	 * 			| setActionsLeft(getActionsLeft() - 1)
 	 * 
 	 * @throws 	NoMoreActionsException
 	 * 			There are no more actions left.
 	 */
 	private void doAction() throws NoMoreActionsException {
 		if(getActionsLeft() <= 0) {
 			throw new NoMoreActionsException();
 		} else {
 			getLightTrail().lowerDuration();
 			setActionsLeft(getActionsLeft() - 1);
 		}
 	}
 	
 	/**
 	 * Do empty actions until there are no actions left.
 	 * 
 	 * @effect	Do actions until there are no actions left.
 	 * 			| for i from 0 to getActionsLeft()
 	 * 			|	 doAction()
 	 */
 	public void emptyActions() {
 		int actionsLeft = getActionsLeft();
 		for (int i=0; i<actionsLeft;i++) {
 			try {
 				doAction();
 			} catch (NoMoreActionsException e) {
 				// Will not occur because of bounded i.
 			}
		}
		turnHandler.checkTurn();
 	}
 	
 	/**
 	 * Do a number of empty actions.
 	 * 
 	 * @param	number
 	 * 			The given number of empty actions to be performed.
 	 * 
 	 * @effect	Do actions until there are no actions left or the given number of actions is reached.
 	 * 			| for i from 0 to getActionsLeft()
 	 * 			|	 doAction()
 	 */
 	public void emptyActions(int number) {
 		int actionsLeft = getActionsLeft();
 		for (int i=0; i<actionsLeft && i<number; i++) {
 			try {
 				doAction();
 			} catch (NoMoreActionsException e) {
 				// Will not occur because of bounded i.
 			}
 		}
 	}
 	
 	/**
 	 * Renew the actions of the player.
 	 * 
 	 * @effect	Set the actions that are left to the normal number
 	 * 			of actions, minus the action damage.
 	 * 			| setActionsLeft(NUMBER_OF_ACTIONS - getActionDamage())
 	 * @effect	Set the action damage to zero.
 	 * 			| setActionDamage(0)
 	 */
 	protected void renewActions() {
 		setActionsLeft(NUMBER_OF_ACTIONS - getActionDamage());
 		int next = getActionDamage() - NUMBER_OF_ACTIONS;
 		if(next < 0) next = 0;
 		setActionDamage(next);
 	}
 
 	/**
 	 * Start a new turn of this player
 	 * 
 	 * @effect 	Renew the actions of this player
 	 * 			| renewActions()
 	 * @effect 	Set the moved status to false
 	 * 			| setMoved(false);
 	 * @effect 	Execute the start on action of the location
 	 * 			| getLocation().onStartOn()
 	 */
 	public void startTurn() {
 		if(getActionDamage() > 0) {
 			setReceivedDamage(true);
 		}
 		else setReceivedDamage(false);
 		resetSkipsTurn();
 		renewActions();
 		setHasMoved(false);
 		getLocation().onStart(this);
 	}
 	
 	/**
 	 * Returns whether a player is trapped in its current position.
 	 * 
 	 * @return	True if there is any direction in which a player can perform a valid move.
 	 * 			False if not.
 	 */
 	public boolean isTrapped() {
 		boolean isTrapped = true;
 		for(int i=0; i<Direction.values().length; i++) {
 			if(getLocation().isValidMove(Direction.values()[i]))
 				isTrapped = false;
 		}
 		return isTrapped;
 	}
 
 	/** Return a string representation of this player
 	 * @return A string representation of this player
 	 * 			|result.equals(this.playerColour + " player at location "+this.getLocation().getCoordinate())
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return this.playerColour + " player at location "+this.getLocation().getCoordinate();
 	}
 
 	public void setTurnHandler(TurnHandler turnHandler) {
 		this.turnHandler = turnHandler;
 	}
 	
 	
 
 }
