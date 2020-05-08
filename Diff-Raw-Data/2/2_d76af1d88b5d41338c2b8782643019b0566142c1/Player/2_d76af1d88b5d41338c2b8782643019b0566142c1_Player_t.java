 package game;
 
 import exception.InvalidMoveException;
 import exception.NoItemException;
 import exception.NoMoreActionsException;
 import exception.OutsideTheGridException;
 import exception.OverCapacityException;
 import grid.Direction;
 import grid.Square;
 import item.PortableItem;
 import item.Teleporter;
 
 import java.util.Observable;
 
 import obstacle.LightTrail;
 
 /**
  * This class represents a player in the game.
  * 
  * @invar 	The player has a valid inventory.
  * 			| canHaveAsInventory(this.getInventory())
  * @invar	The player has a valid playercolour.
  * 			| playerColour == RED || playerColour == BLUE
  * @invar	The player has a valid starting location.
  * @invar	The player starts the game on its starting location.
  * 			| location == startingPosition
  * @invar	The player has a valid lighttrail.
  * 			| canHaveAsLightTrail(this.getLightTrail())
  * @invar	The player cannot have more than 3 actions per turn.
  * 			| NUMBER_OF_ACTIONS == 3
  * 
  * @author 	Group 8
  * 
  * @version April 2013
  */
 public class Player extends Observable implements Actor {
 	
 	/**
 	 * Handles the turns of the player
 	 */
 	private TurnHandler turnHandler;
 	
 	/**
 	 * Represents the current location (current square) of the player.
 	 */
 	private Square location;
 	
 	/**
 	 * Represents the light trail of the player.
 	 */
 	private final LightTrail lightTrail;
 	
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
 	public static final int NUMBER_OF_ACTIONS = 4;
 	
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
 	 * Indicating whether this player has skipped its turn
 	 */
 	private boolean skipsTurn;
 	
 	/**
 	 * Variable indicating whether this player has received damage.
 	 */
 	private boolean hasReceivedDamage;
 
 	private boolean mustLose = false;
 	
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
 	 * 
 	 * @effect	The location of the player is set to the starting position.
 	 * 			| changeLocation(startingPosition)
 	 * @effect	The light trail is initialized.
 	 * 			| lightTrail = new LightTrail()
 	 * 			| lightTrail.setPlayer(this)
 	 * @effect	Initialize the inventory.
 	 * 			| inventory = new Inventory()
 	 * 			| inventory.setOwner(this)
 	 * 
 	 */
 	public Player(Square startingPosition, PlayerColour playerColour) {
 		this.playerColour = playerColour;
 		this.startingPosition = startingPosition;
 		changeLocation(startingPosition);
 		lightTrail = new LightTrail(getLocation());
 		lightTrail.setPlayer(this);
 		inventory = new Inventory();
 		inventory.setOwner(this);
 	}
 	
 	/*******************
 	 * GETTERS & SETTERS
 	 *******************/
 	
 	/**
 	 * This method sets the turnhandler for the player.
 	 * 
 	 * @param 	turnHandler
 	 * 			The turnhandler to set.
 	 * 
 	 * @effect	The turnhandler is set.
 	 * 			| new.turnHandler = turnHandler
 	 */
 	public void setTurnHandler(TurnHandler turnHandler) {
 		this.turnHandler = turnHandler;
 	}
 	
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
 	 * 			If the given square is not a valid location for this player.
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
 	 * 
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
 	private void setHasMoved(boolean moved){
 		hasMoved = moved;
 	}
 	
 	/**
 	 * Returns whether the player has skipped a turn.
 	 * 
 	 * @return 	True if the player has skipped a turn.
 	 * 			| return skipsTurn
 	 */
 	public boolean skipsTurn() {
 		return skipsTurn;
 	}
 
 	/**
 	 * Skip the players turn.
 	 * 
 	 * @effect	The player has skipped a turn.
 	 * 			| skipsTurn = true;
 	 * 
 	 * @post	The player has no more actions left.
 	 * 			| emptyActions()
 	 */
 	public void skipTurn() {
 		skipsTurn = true;
 		emptyActions();
 	}
 	
 	/**
 	 * This method sets skipsTurn to false.
 	 * 
 	 * @effect	skipsTurn is set to false.
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
 	 * @return 	True if the square is effective and has no obstacle or player on it
 	 * 			| location != null && location.hasObstacle() && 
 	 * 		 	| (!location.hasPlayer() || location.getPlayer() == this)
 	 */
 	public boolean canHaveAsNewLocation(Square location){
 		return location != null && 
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
 	 * @return 	True if it's an effective light trail
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
 	 * @return 	True if it's an effective inventory
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
 	 * 			| result == hasReceivedDamage
 	 */
 	public boolean hasReceivedDamage() {
 		return hasReceivedDamage;
 	}
 	
 	/**
 	 * Sets whether the player has received damage or not.
 	 * 
 	 * @param 	hasReceivedDamage
 	 * 			True, if the player has received damage. False, otherwise.
 	 * 
 	 * @post	The receivedDamage-boolean is set to the given value.
 	 * 			| new.hasReceivedDamage = hasReceivedDamage
 	 */
 	private void setHasReceivedDamage(Boolean hasReceivedDamage) {
 		this.hasReceivedDamage = hasReceivedDamage;
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
 	 * @effect	The item is added to the inventory
 	 * 			| getInventory().addToInventory(item)
 	 * @effect	The item is removed from the location.
 	 * 			| getLocation().removeItem(item)
 	 * @effect	An action is done.
 	 * 			| doAction()
 	 * @effect	The turn is checked.
 	 * 			| turnHandler.checkTurn()
 	 * 
 	 * @throws 	NoItemException
 	 * 			If the item is not present.
 	 * @throws 	OverCapacityException
 	 * 			If the inventory is full.
 	 * @throws 	NoMoreActionsException
 	 * 			If the player has no more actions left.
 	 */
 	public void pickUpItem(PortableItem item) throws NoItemException, OverCapacityException, NoMoreActionsException {
 		getInventory().addToInventory(item);
 		getLocation().removeItem(item);
 		doAction();
 		turnHandler.getGame().getActionNotifier().notifyObservers();
 		turnHandler.checkTurn();
 	}
 	
 	/**
 	 * Use the given item.
 	 * 
 	 * @param 	item
 	 * 			The item that must be used.
 	 * 
 	 * @effect	Let the item be used.
 	 * 			| item.use()
 	 * @effect	Delete the given item from the inventory.
 	 * 			| getInventory().deleteFromInventory(item)
 	 * @effect	Do an action.
 	 * 			| doAction()
 	 * @effect	Check the turn.
 	 * 			| turnHandler.checkTurn()
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
 			item.onUse();
 			getInventory().deleteFromInventory(item);	
 			doAction();
 			turnHandler.getGame().getActionNotifier().notifyObservers();
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
 	 * @effect	Check the turn.
 	 * 			| turnHandler.checkTurn()
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
 		turnHandler.getGame().getActionNotifier().notifyObservers();
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
 			setHasReceivedDamage(true);
 		}
 		else setHasReceivedDamage(false);
 		resetSkipsTurn();
 		renewActions();
 		setHasMoved(false);
 		getLocation().onStart(this);
 	}
 
 	/**
 	 * Lets the player end its turn.
 	 * 
 	 * @effect	Empty all the actions of the player that were left.
 	 * 			| emptyActions()
 	 * @effect	Check the turn (winning, losing).
 	 * 			| turnHandler.checkTurn()
 	 */
 	public void endTurn() {
 		emptyActions();
 		turnHandler.checkTurn();
 	}
 	
 	/**
 	 * Lets the player lose its turn.
 	 * This differs from ending its turn, by being damaged in this case.
 	 * 
 	 * @effect	The player has received damage.
 	 * 			| setHasRecievedDamage(true)
 	 * @effect	Empty the actions of the player.
 	 * 			| emptyActions()
 	 */
 	public void loseTurn() {
 		setHasReceivedDamage(true);
 		emptyActions();
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
 		if(damage >= getActionsLeft()) {
 			setHasReceivedDamage(true);
 			setActionDamage(damage - getActionsLeft());
 			emptyActions();
 		} else {
 			emptyActions(damage);
 		}
 	}
 	
 	/**
 	 * TODO: integrate with loseTurn() and applyActionDamage() (not now!!!)
 	 * BART, BLIJF ERAF :p
 	 */
 	private void loseActions() {
 		setActionsLeft(0);
 	}
 	
 	/**
 	 * TODO: integrate with loseTurn() and applyActionDamage() (not now!!!)
 	 * BART, BLIJF ERAF :p
 	 */
 	private void loseActions(int number) {
 		setActionsLeft(actionsLeft - number);
 	}
 
 	/**
 	 * Do empty actions until there are no actions left.
 	 * 
 	 * @effect	Do actions until there are no actions left.
 	 * 			| for i from 0 to getActionsLeft()
 	 * 			|	 doAction()
 	 */
 	private void emptyActions() {
 		int actionsLeft = getActionsLeft();
 		for (int i=0; i<actionsLeft;i++) {
 			try {
 				doAction();
 				turnHandler.getGame().getActionNotifier().notifyObservers();
 			} catch (NoMoreActionsException e) {
 				// Will not occur because of bounded i.
 			}
 		}		
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
 	private void emptyActions(int number) {
 		int actionsLeft = getActionsLeft();
 		for (int i=0; i<actionsLeft && i<number; i++) {
 			try {
 				doAction();
 				turnHandler.getGame().getActionNotifier().notifyObservers();
 			} catch (NoMoreActionsException e) {
 				// Will not occur because of bounded i.
 			}
 		}
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
 			turnHandler.getGame().getActionNotifier().change();
 		}
 	}
 
 	/**
 	 * Renew the actions of the player.
 	 * 
 	 * @effect	Set the actions that are left to the normal number
 	 * 			of actions, minus the action damage.
 	 * 			| setActionsLeft(NUMBER_OF_ACTIONS - getActionDamage())
 	 * @effect	Set the action damage to the rest.
 	 * 			| setActionDamage(getActionDamage() - NUMBER_OF_ACTIONS;)
 	 */
 	protected void renewActions() {
 		setActionsLeft(NUMBER_OF_ACTIONS - getActionDamage());
 		int next = getActionDamage() - NUMBER_OF_ACTIONS;
 		if(next < 0) next = 0;
 		setActionDamage(next);
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
		if(this.getLocation().hasObstacle() && !this.getLocation().getObstacle().canLeave())
			isTrapped = false;
 		if(mustLose)
 			isTrapped = true;
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
 
 	public void setLose(boolean mustlose) {
 		this.mustLose  = mustlose;
 	}	
 	
 	public boolean mustLose(){
 		return this.mustLose;
 	}
 
 }
