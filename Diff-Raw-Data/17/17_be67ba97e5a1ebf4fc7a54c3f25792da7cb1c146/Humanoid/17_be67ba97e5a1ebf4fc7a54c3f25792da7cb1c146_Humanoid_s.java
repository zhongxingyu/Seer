 package zuul;
 
 /**
  * Humanoid Class.
  *
  * This is a super class of the player and monster classes
  * This class has the health variable, a name variable, an inventory list and a MAX_HEALTH static variable.
  * The health represents the remaining health of the player or monster.
  * The name represents the name of the player or monster.
  * The MAX_HEALTH is a default health, if there is no other input.
  * The inventory is a list of the items that the player/monster have.
  * Items can be added or removed from the inventory.
  *
  * @author  Jarred Linthorne-Shaw
  * @version 2012.10.23
  */
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import Builders.Builder;
 
 public abstract class Humanoid implements Serializable {
 
 	/**
    * Serial version UID
    */
   private static final long serialVersionUID = 1L;
   private int health;
 	private String name;
 	public static final int MAX_HEALTH = 100;
 	protected ArrayList<Item> inventory;
 	private Room currentRoom;
 	Builder b;
 
 	/**
 	 * This is the constructor for the Humanoid. If no name is passed, the
 	 * default is the name of the class that instantiates it. The health is set,
 	 * and the inventory list is created.
 	 * 
 	 * @param maxHealth
 	 *            : The health of the humanoid
 	 * @param name
 	 *            : The name of the humanoid
 	 */
 	public Humanoid(int maxHealth, String name, Room room) {
 		if (name == null)
 			this.name = getClass().getName();
 		else
 			this.name = name;
 		health = maxHealth;
 		inventory = new ArrayList<Item>();
 		currentRoom = room;
 		if(currentRoom != null) b = currentRoom.getBuilder();
 	}
 
 	/**
 	 * Default constructor for the Humanoid. Calls the constructor above with
 	 * the default health and no name.
 	 */
 	public Humanoid(Room room) {
 		this(MAX_HEALTH, null, room);
 	}
 
 	/**
 	 * Update the Humanoid's health during a fight. Could put removeHealth and
 	 * addHealth into one method, but this way is less error-prone
 	 * 
 	 * @param recovery
 	 *            : The recovery of health for the humanoid
 	 */
 	public void addHealth(int recovery) {
 		health = health + recovery;
 	}
 
 	/**
 	 * Adds an item to the Humanoid's inventory.
 	 * 
 	 * @param i
 	 *            : The item to be added.
 	 */
 	public void addItem(Item i) {
 		inventory.add(i);
 	}
 
 	/**
 	 * This method drops all of the items that the monster is carrying into the
 	 * room it is in.
 	 */
 	public void dropItems() {
 		ArrayList<Item> inventory = getInventory();
 		for (Item i : inventory)
 			currentRoom.addItem(i);
 	}
 
 	/**
 	 * An equals method used to compare two Humanoids.
 	 * 
 	 * @param o
 	 *            : Another humanoid object to compare to.
 	 * @return : True if the Humanoids are the same, false if not.
 	 */
 	@Override
 	public boolean equals(Object o) {
 		if (!(o instanceof Humanoid))
 			return false;
 		Humanoid h = (Humanoid) o;
 		return (health == h.health) && (inventory.equals(h.inventory))
 				&& name.equals(h.name) && currentRoom.equals(h.currentRoom);
 	}
 
 	/**
 	 * Get the best item in the Humanoid's inventory.
 	 * 
 	 * @return : The item with the highest value in the Humanoid's inventory. If
 	 *         the Humanoid has no items, an item with no values is returned.
 	 */
 	public Item getBestItem() {
 		Item tempItem;
 		tempItem = new Item("NO_ITEM", 0, 0, false);
 		for (Item i : inventory)
 			if (i.isWeapon())
 				if (i.getValue() > tempItem.getValue())
 					tempItem = i;
 		return tempItem;
 	}
 
 	/**
 	 * Getter for the current room.
 	 * 
 	 * @return : The room the player is current in.
 	 */
 	public Room getCurrentRoom() {
 		return currentRoom;
 	}
 
 	/**
 	 * Getter for health.
 	 * 
 	 * @return : The current health of the Humanoid.
 	 */
 	public int getHealth() {
 		return health;
 	}
 
 	/**
 	 * Get the inventory of the Humanoid.
 	 * 
 	 * @return : The list of items in the Humanoid's inventory.
 	 */
 	public ArrayList<Item> getInventory() {
 		return inventory;
 	}
 
 	/**
 	 * Get a string containing all of the items in the inventory.
 	 * 
 	 * @return A string of the items if there are any, else a string saying
 	 *         there are no items.
 	 */
 	public String getInventoryString() {
 		String s;
 		if (inventory.isEmpty())
 			s = "The " + this.getClass().getName() + " has no items";
 		else {
 			s = "The " + this.getClass().getName()
 					+ " has the following items:\n";
 			for (Item i : inventory) {
 				s += i.getName();
 				s += "\n";
 			}
 		}
 		return s;
 	}
 
 	/**
 	 * Get the name of the Humanoid.
 	 * 
 	 * @return : The name of the humanoid.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Get the real item in the inventory given only the item's name.
 	 * 
 	 * @param item
 	 *            : The name of the item, not the real item.
 	 * @return : The real Item from the inventory.
 	 */
 	public Item getRealItem(Item item) {
 		int index = inventory.indexOf(item);
 		if (index != -1)
 			return inventory.get(index);
 		else
 			return null;
 	}
 
 	/**
 	 * Update the Humanoid's health during a fight.
 	 * 
 	 * @param damage
 	 *            : The damage done to the humanoid.
 	 */
 	public void removeHealth(int damage) {
 		health = health - damage;
 	}
 
 	/**
 	 * Remove the input item from the inventory.
 	 * 
 	 * @param i
 	 *            : The input item to remove.
 	 * @return : A boolean informing whoever called it if the item was actually
 	 *         removed.
 	 */
 	public boolean removeItem(Item i) {
 		return inventory.remove(i);
 	}
 
 	/**
 	 * Set the humanoid back to its default state
 	 */
 	public void reset() {
 		setHealth(MAX_HEALTH);
 		if (this instanceof Player) { // you cannot chagne the current room of a
 										// monster
			currentRoom = Game.initialize(currentRoom.getBuilder());
 			inventory.clear();
 		} else {
 			// TODO error: reset makes no sense on a monster
 		}
 
 	}
 
 	/**
 	 * Setter for the current room.
 	 */
 	// protected because we dont want to change the room of a monster
 	protected void setCurrentRoom(Room room) {
 		// you cannot chagne the current room of a monster
 		if (this instanceof Player)
 			currentRoom = room;
 		else {
 			// TODO report an error
 		}
 	}
 
 	/**
 	 * Setter for health.
 	 * 
 	 * @param health
 	 *            : The new health of the Humanoid.
 	 */
 	public void setHealth(int health) {
 		this.health = health;
 	}
 
 	/**
 	 * A method to return the name of the Humanoid.
 	 * 
 	 * @return : The name of the Humanoid.
 	 */
 	@Override
 	public String toString() {
 		return name;
 	}
 	
 	public void setRoom(Room r){
 		currentRoom = r;
 	}
 }
