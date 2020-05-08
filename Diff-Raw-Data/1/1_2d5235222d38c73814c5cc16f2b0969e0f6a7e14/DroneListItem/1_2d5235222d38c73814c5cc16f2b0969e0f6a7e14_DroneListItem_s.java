 package net.grosinger.nomads;
 
 import java.util.ArrayList;
 
 import net.grosinger.nomads.Upgrade.UpgradeType;
 import net.grosinger.nomads.exceptions.FullInventoryException;
 
 /**
  * A class that allows a drone to be part of a linked list Provides reference to
  * the next drone, the previous drone, and the drone object
  * 
  * Most of the inner workings of the program that we do not want a drone to have
  * access to will take place here
  * 
  * Previous --> Towards the first item Next --> Towards the last item
  */
 public class DroneListItem {
 	private DroneListItem next;
 	private DroneListItem previous;
 	private Drone current;
 	private DroneTeam team;
 
 	/**
 	 * The DroneTools for this Drone
 	 */
 	private DroneTools yourTools;
 
 	public enum EnumMove {
 		NoMove, North, South, East, West, Upgrade, Attack, Steal
 	}
 
 	public enum Direction {
 		N, S, E, W
 	}
 
 	// Statistics about this robot
 
 	private int visibleDistance; // Distance that this drone can see buildings
 									// and other drones
 	private int lumaLocatorDistance;
 	private int objectLocatorDistance;
 	private int reliability;
 	private int attack; // Between 1 and 2 - reflects how well can attack
 	private int defenses; // Between 1 and 2 - reflects ability to block
 							// steal
 	private int speed; // Reflected in movements per turn
 	private int cargoSpace;
 	private int theft; // Between 1 and 2 - reflects how well can steal
 
 	// Info about this robot
 
 	private int age;
 	private int x;
 	private int y;
 	private int waiting; // Is the drone building another drone or house?
 	private boolean wanted; // Is the drone wanted by the police?
 	private Inventory inventory;
 	private ArrayList<Objective> currentObjectives;
 
 	/*
 	 * Default constructor, includes all references
 	 */
 	public DroneListItem(DroneListItem theNext, DroneListItem thePrevious, Drone theCurrent,
 			DroneTeam theTeam) {
 		next = theNext;
 		previous = thePrevious;
 		current = theCurrent;
 		visibleDistance = Nomads.BASE_VISIBLEDISTANCE;
 		lumaLocatorDistance = Nomads.BASE_LUMALOCATORDISTANCE;
 		objectLocatorDistance = Nomads.BASE_OBJECTLOCATORDISTANCE;
 		reliability = Nomads.BASE_RELIABILITY;
 		attack = Nomads.BASE_ATTACK;
 		defenses = Nomads.BASE_DEFENSES;
 		speed = Nomads.BASE_SPEED;
 		cargoSpace = Nomads.BASE_CARGOSPACE;
 		theft = Nomads.BASE_THEFT;
 		team = theTeam;
 		waiting = 0;
 		inventory = new Inventory(this);
 
 		// Place itself in the world
 		Nomads.awesomeWorld.placeNewDrone(this);
 
 		// Give the Drone it's tools
 		yourTools = new DroneTools(current, this, Nomads.awesomeWorld);
 		current.setDroneTools(yourTools);
 	}
 
 	// Getters and Setters
 
 	/**
 	 * Retrieve the next DroneListItem in the Linked List
 	 * 
 	 * @return <code>DroneListItem</code>
 	 */
 	public DroneListItem getNext() {
 		return next;
 	}
 
 	/**
 	 * Retrieve the previous DroneListItem in the Linked List
 	 * 
 	 * @return <code>DroneListItem</code>
 	 */
 	public DroneListItem getPrevious() {
 		return previous;
 	}
 
 	/**
 	 * Retrieve the Drone associated with the current DroneListItem
 	 * 
 	 * @return <code>Drone</code>
 	 */
 	public Drone getCurrent() {
 		return current;
 	}
 
 	/**
 	 * Retrieve the Inventory associated with this DroneListItem
 	 * 
 	 * @return <code>Inventory</code>
 	 */
 	public Inventory getInventory() {
 		return inventory;
 	}
 
 	/**
 	 * Retrieve the distance this drone can see other drones
 	 * 
 	 * @return <code>int</code> Visible Distance
 	 */
 	public int getVisibleDistance() {
 		return visibleDistance;
 	}
 
 	/**
 	 * Retrieve the distance from which this drone can spot a LumaPile
 	 * 
 	 * @return <code>int</code> Visible Distance
 	 */
 	public int getLumaLocatorDistance() {
 		return lumaLocatorDistance;
 	}
 
 	/**
 	 * Retrieve the distance from which this drone can spot an object
 	 * 
 	 * @return <code>int</code> Visible Distance
 	 */
 	public int getObjectLocatorDistance() {
 		return objectLocatorDistance;
 	}
 
 	/**
 	 * Retrieve the reliability factor of this drone
 	 * 
 	 * @return <code>int</code>
 	 */
 	public int getReliability() {
 		return reliability;
 	}
 
 	/**
 	 * Retrieve the attack factor of this drone
 	 * 
 	 * @return <code>int</code>
 	 */
 	public int getAttack() {
 		return attack;
 	}
 
 	/**
 	 * Retrieve the defenses factor of this drone
 	 * 
 	 * @return <code>int</code>
 	 */
 	public int getDefenses() {
 		return defenses;
 	}
 
 	/**
 	 * Retrieve the speed factor of this drone
 	 * 
 	 * @return <code>int</code>
 	 */
 	public int getSpeed() {
 		return speed;
 	}
 
 	/**
 	 * Retrieve the total space in the cargo hold of this drone. Does include
 	 * 
 	 * space that is currently occupied.
 	 * 
 	 * @return <code>int</code>
 	 */
 	public int getCargoSpace() {
 		return cargoSpace;
 	}
 
 	/**
 	 * Retrieve the level of this drone in thieving
 	 * 
 	 * @return <code>int</code>
 	 */
 	public int getTheft() {
 		return theft;
 	}
 
 	/**
 	 * Returns how many turns this drone has been alive
 	 * 
 	 * @return How many turns this drone has been alive
 	 */
 	public int getAge() {
 		return age;
 	}
 
 	/**
 	 * Returns the x index of this drone
 	 * 
 	 * @return <code>int</code>
 	 */
 	public int getX() {
 		return x;
 	}
 
 	/**
 	 * Returns the y index of this drone
 	 * 
 	 * @return <code>int</code>
 	 */
 	public int getY() {
 		return y;
 	}
 
 	/**
 	 * Returnes the list of all objectives this drone should be currently
 	 * looking for
 	 * 
 	 * @return <code>ArrayList</code>
 	 */
 	public ArrayList<Objective> getCurrentObjectives() {
 		return currentObjectives;
 	}
 
 	public boolean getCurrentObjectivesFull() {
 		return currentObjectives.size() >= Nomads.MAXREQUESTEDOBJECTIVES;
 	}
 
 	/**
 	 * Returns if the drone is wanted
 	 * 
 	 * @return <code>boolean</code>
 	 */
 	public boolean isWanted() {
 		return wanted;
 	}
 
 	/**
 	 * Retrieve reference to the team this drone belongs to
 	 * 
 	 * @return <code>DroneTeam</code>
 	 */
 	public DroneTeam getTeam() {
 		return team;
 	}
 
 	/**
 	 * Find if the inventory is full
 	 * 
 	 * @return <code>Boolean</code>
 	 */
 	public boolean getInventoryIsFull() {
 		return inventory.isFull();
 	}
 
 	/**
 	 * Sets the next DroneListItem in the Linked List
 	 * 
 	 * @param theNext
 	 *            <code>DroneListItem</code>
 	 */
 	public void setNext(DroneListItem theNext) {
 		next = theNext;
 	}
 
 	/**
 	 * Sets the previous DroneListItem in the Linked List
 	 * 
 	 * @param thePrevious
 	 *            <code>DroneListItem</code>
 	 */
 	public void setPrevious(DroneListItem thePrevious) {
 		previous = thePrevious;
 	}
 
 	/**
 	 * Sets the visible distance for this drone
 	 * 
 	 * @param newDistance
 	 *            <code>int</code> New Distance
 	 */
 	public void setVisibleDistance(int newDistance) {
 		visibleDistance = newDistance;
 	}
 
 	/**
 	 * Used when adding the drone to the map
 	 * 
 	 * @param newX
 	 *            <code>int</code> new X location
 	 */
 	public void setX(int newX) {
 		x = newX;
 	}
 
 	/**
 	 * Used when adding the drone to the map
 	 * 
 	 * @param newY
 	 *            <code>int</code> new Y location
 	 */
 	public void setY(int newY) {
 		y = newY;
 	}
 
 	/**
 	 * Adds the existing waiting time (which should be 0) to the provided time
 	 * 
 	 * @param newWaiting
 	 *            - Amount of time to add to waiting timer
 	 */
 	public void setWaiting(int newWaiting) {
 		waiting += newWaiting;
 	}
 
 	/**
 	 * Adds a new objective to the list of current objectives
 	 * 
 	 * @param newObj
 	 *            Objective to add to list
 	 */
 	public void addObjective(Objective newObj) {
 		currentObjectives.add(newObj);
 	}
 
 	/**
 	 * Removes an objective from the list of current objectives
 	 * 
 	 * @param oldObj
 	 *            Objective to remove from list
 	 */
 	public void removeObjective(Objective oldObj) {
 		currentObjectives.remove(oldObj);
 	}
 
 	/**
 	 * Increases the Visible Distance by specified amount
 	 * 
 	 * @param amount
 	 *            <code>int</code> How much to increase the distance
 	 */
 	public void increaseVisibleDistance(int amount) {
 		visibleDistance += amount;
 	}
 
 	/**
 	 * Increments the level specified by the type.
 	 * 
 	 * @param type
 	 *            Level to be increased
 	 */
 	public void incrementLevel(UpgradeType type) {
 		switch (type) {
 		case visibleDistance: {
 			visibleDistance++;
 			break;
 		}
 		case lumaLocatorDistance: {
 			lumaLocatorDistance++;
 			break;
 		}
 		case objectLocatorDistance: {
 			objectLocatorDistance++;
 			break;
 		}
 		case reliability: {
 			reliability++;
 			break;
 		}
 		case attack: {
 			attack++;
 			break;
 		}
 		case defenses: {
 			defenses++;
 			break;
 		}
 		case speed: {
 			speed++;
 			break;
 		}
 		case cargoSpace: {
 			cargoSpace++;
 			break;
 		}
 		case theft: {
 			theft++;
 			break;
 		}
 		default: {
 			// Must specify an Upgrade Type
 		}
 		}
 	}
 
 	/**
 	 * Increases the age of the drone by 1 turn
 	 */
 	public final void incrementAge() {
 		age++;
 	}
 
 	// Actions
 
 	/**
 	 * Removes this drone from the team
 	 */
 	public void die() {
 		team.removeDrone(this);
 	}
 
 	/**
 	 * Will ask the Drone what direction it would like to move
 	 * 
 	 * @return <code>boolean</code> if a move was made
 	 */
 	public boolean makeMove() {
 		// Call the Drone's Move method
 		EnumMove move = current.move();
 
 		switch (move) {
 		case NoMove: {
 			// Default move has not been overridden
 			return true;
 		}
 		case North: {
 			if (yourTools.canMoveNorth()) {
 				moveDrone(Direction.N);
 				return true;
 			} else
 				return false;
 		}
 		case South: {
 			if (yourTools.canMoveSouth()) {
 				moveDrone(Direction.S);
 				return true;
 			} else
 				return false;
 		}
 		case East: {
 			if (yourTools.canMoveEast()) {
 				moveDrone(Direction.E);
 				return true;
 			} else
 				return false;
 		}
 		case West: {
 			if (yourTools.canMoveWest()) {
 				moveDrone(Direction.W);
 				return true;
 			} else
 				return false;
 		}
 		case Attack: {
 			doAttack();
 			return true;
 		}
 		case Steal: {
 			doSteal();
 			return true;
 		}
 		default: {
 			// No move was made
 			return false;
 		}
 		}
 	}
 
 	// Movement
 
 	/**
 	 * Finds who the drone wants to attack and attempts to perform attack.
 	 */
 	private void doAttack() {
 		NeighborDrone victimNeighbor = current.attack();
 
 		if (victimNeighbor == null) {
 			// Seems they did something wrong. Turn wasted.
 			return;
 		}
 
 		DroneListItem victimList = Nomads.UIDToListItem(victimNeighbor.getUID());
 
 		int victimDefence = victimList.getDefenses();
 
 		// Find a random number between 0-100 to determine success.
 		// Min + (int)(Math.random() * ((Max - Min) + 1))
 		int rand = 0 + (int) (Math.random() * ((100 - 0) + 1));
 
 		// Find modified random number based on stats
 		int newRand = rand * (victimDefence - attack) * -1;
 		newRand += rand;
 
 		// If number is <= 35, drone becomes wanted
 		// If number is <= 70, attack fails
 		// If number is > 70, kill
 
 		if (newRand <= 35) {
 			wanted = true;
 		} else if (newRand <= 70) {
 			// Attack Failed
 		} else if (newRand > 70) {
 			// Drone Killed
 			killOtherDrone(victimList);
 		} else {
 			// Not sure what happened here
 		}
 	}
 
 	/**
 	 * Finds who the drone wants to steal from and attempts to perform steal.
 	 */
 	private void doSteal() {
 		NeighborDrone victimNeighbor = current.steal();
 
 		if (victimNeighbor == null) {
 			// Seems they did something wrong. Turn wasted.
 			return;
 		}
 
 		DroneListItem victimList = Nomads.UIDToListItem(victimNeighbor.getUID());
 
 		int victimDefence = victimList.getDefenses();
 
 		// Find a random number between 0-100 to determine success.
 		// Min + (int)(Math.random() * ((Max - Min) + 1))
 		int rand = 0 + (int) (Math.random() * ((100 - 0) + 1));
 
 		// Find modified random number based on stats
 		int newRand = rand * (victimDefence - theft) * -1;
 		newRand += rand;
 
 		// If number is <= 35, drone becomes wanted
 		// If number is <= 70, steal fails
 		// If number is > 70, steal
 		// Depending on how much higher than 70, more items will be stolen
 
 		int itemsInVictimInventory = victimList.inventory.size();
 		Double percentToTake = .00;
 
 		if (newRand <= 35) {
 			wanted = true;
 		} else if (newRand <= 70) {
 			percentToTake = .00;
 		} else if (newRand > 70 && newRand <= 80) {
 			percentToTake = .2;
 		} else if (newRand > 80 && newRand >= 90) {
 			percentToTake = .4;
 		} else if (newRand >= 100 && newRand <= 98) {
 			percentToTake = .7;
 		} else if (newRand > 98) {
 			percentToTake = 1.0;
 		} else {
 			// Not sure what happened here
 		}
 
 		int itemsToTake = (int) Math.ceil(itemsInVictimInventory * percentToTake);
 		while (itemsToTake < 0 && !getInventoryIsFull() && victimList.inventory.size() > 0) {
 			// Min + (int)(Math.random() * ((Max - Min) + 1))
 			int randIndex = 0 + (int) (Math.random() * (((victimList.inventory.size() - 1) - 0) + 1));
 
 			try {
 				inventory.addItem(victimList.inventory.getItem(randIndex));
 			} catch (FullInventoryException e) {
 				e.printStackTrace();
 			}
 
 			victimList.inventory.remove(randIndex);
 		}
 	}
 
 	/**
 	 * Move the drone in a specified direction
 	 * 
 	 * @param direction
 	 *            <code>Direction</code> to move
 	 */
 	private void moveDrone(Direction direction) {
 		if (waiting != 0) {
 			waiting--;
 			return;
 		}
 
 		int amountN = 0;
 		int amountE = 0;
 		switch (direction) {
 		case N: {
 			amountN = -1;
 			break;
 		}
 		case S: {
 			amountN = 1;
 			break;
 		}
 		case E: {
 			amountE = 1;
 			break;
 		}
 		case W: {
 			amountE = -1;
 			break;
 		}
 		}
 
 		if (getX() + amountE > Nomads.awesomeWorld.getWorldSize() - 1 || getX() + amountE < 0
 				|| getY() + amountN > Nomads.awesomeWorld.getWorldSize() - 1
 				|| getY() + amountN < 0) {
 			return;
 		}
 
 		// Check to see if there is a MoneyPile or Objective there
 		GameObject objectHere = Nomads.awesomeWorld.getObjectAt(getX() + amountE, getY() + amountN);
 
 		if (objectHere != null) {
 			if (!inventory.isFull()) {
 				if (objectHere instanceof MoneyPile) {
 					try {
 						inventory.addItem(objectHere);
 					} catch (FullInventoryException e) {
 						e.printStackTrace();
 					}
 				} else if (objectHere instanceof Objective) {
 					String objUID = ((Objective) objectHere).getUID();
 					String droneUID = current.getUID();
 
 					if (objUID.equals(droneUID)) {
 						try {
 							inventory.addItem(objectHere);
 						} catch (FullInventoryException e) {
 							e.printStackTrace();
 						}
 					} else {
 						return;
 					}
 				}
 			} else {
 				return;
 			}
 		}
 
 		// Make the move
 		Nomads.awesomeWorld.moveObjectAt(getX(), getY(), amountN, amountE);
 
 		// Update the saved coordinates
 		if (amountN != 0)
 			setY(getY() + amountN);
 		if (amountE != 0)
 			setX(getX() + amountE);
 	}
 
 	/**
 	 * Kills another drone. Will take a random assortment of the items in their
 	 * inventory.
 	 * 
 	 * @param victim
 	 *            - <code>DroneListItem</code> to be killed
 	 */
 	private void killOtherDrone(DroneListItem victim) {
 		while (!inventory.isFull() && !victim.inventory.isEmpty()) {
 			// Take items from their inventory
 			int randIndex = 0 + (int) (Math.random() * (((victim.inventory.size() - 1) - 0) + 1));
 			try {
 				inventory.addItem(victim.inventory.getItem(randIndex));
 			} catch (FullInventoryException e) {
 				e.printStackTrace();
 			}
 			victim.inventory.remove(randIndex);
 		}
 
 		victim.die();
 	}
 }
