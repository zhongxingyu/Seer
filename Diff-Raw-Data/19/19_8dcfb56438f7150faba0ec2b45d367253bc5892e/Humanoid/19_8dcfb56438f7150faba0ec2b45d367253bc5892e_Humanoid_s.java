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
 
 import java.util.ArrayList;
 
 public abstract class Humanoid{
 
 	private int health;
 	private String name;
 	public static final int MAX_HEALTH = 100;
 	protected ArrayList<Item> inventory;
 
 	/**
 	 * This is the constructor for the Humanoid.
 	 * If no name is passed, the default is the name of the class that instantiates it.
 	 * The health is set, and the inventory list is created.
 	 * @param maxHealth : The health of the humanoid
 	 * @param name : The name of the humanoid
 	 */
 	public Humanoid(int maxHealth, String name){
 		if(name == null) {
 			this.name = getClass().getName();
 		} else {
 			this.name = name;
 		}
 		health = maxHealth;
 		inventory = new ArrayList<Item>();
 	}
 
 	/**
 	 * Default constructor for the Humanoid.
 	 * Calls the constructor above with the default health and no name.
 	 */
 	public Humanoid(){
 		this(MAX_HEALTH, null);
 	}
 
 	/**
 	 * Getter for health.
 	 * @return : The current health of the Humanoid.
 	 */
 	public int getHealth(){
 		return health;
 	}
 
 	/**
 	 * Setter for health.
 	 * @param health : The new health of the Humanoid.
 	 */
 	public void setHealth(int health){
 		this.health = health;
 	}
 
 	/**
 	 * Update the Humanoid's health during a fight.
 	 * @param damage : The damage done to the humanoid.
 	 */
 	public void removeHealth(int damage){
 		health = health - damage;
 	}
 
 	/**
 	 * Update the Humanoid's health during a fight.
 	 * Could put removeHealth and addHealth into one method, but this way is less error-prone
 	 * @param recovery : The recovery of health for the humanoid
 	 */
 	public void addHealth(int recovery){
 		health = health + recovery;
 	}
 
 	/**
 	 * Adds an item to the Humanoid's inventory.
 	 * @param i : The item to be added.
 	 */
 	public void addItem(Item i){
 		inventory.add(i);
 	}
 
 	/**
 	 * Remove the input item from the inventory.
 	 * @param i : The input item to remove.
 	 * @return : A boolean informing whoever called it if the item was actually removed.
 	 */
 	public boolean removeItem(Item i){
 		return inventory.remove(i);
 	}
 
 	/**
 	 * Get the inventory of the Humanoid.
 	 * @return : The list of items in the Humanoid's inventory.
 	 */
 	public ArrayList<Item> getInventory(){
 		return inventory;
 	}
 
 	/**
 	 * Get the best item in the Humanoid's inventory.
 	 * @return : The item with the highest value in the Humanoid's inventory.
 	 * If the Humanoid has no items, an item with no values is returned.
 	 */
 	public Item getBestItem(){
 		Item tempItem;
 	//	if(inventory.isEmpty()){
 	//		return new Item("NO_ITEM", 0, 0, false);
 	//	}
 		tempItem = new Item("NO_ITEM", 0, 0, false);
 		for(Item i: inventory){
 			if(i.isWeapon()){
 				if(i.getValue()>tempItem.getValue()){
 					tempItem = i;
 				}
 			}
 		}
 		return tempItem;
 	}
 
 	/**
 	 * An equals method used to compare two Humanoids.
 	 * @return : True if the Humanoid are the same, false if not.
 	 */
 	@Override
 	public boolean equals(Object o){
 		if(o instanceof Humanoid){
 			return (this.health == ((Humanoid) o).health) && (this.inventory.equals(((Humanoid) o).inventory));
 		}
 		return false;
 	}
 
 	/**
 	 * A method to return the name of the Humanoid.
 	 * @return : The name of the Humanoid.
 	 */
 	@Override
 	public String toString(){
 		return name;
 	}
 
 	/**
 	 * Get a string containing all of the items in the inventory.
 	 * @return A string of the items if there are any, else a string saying there are no items.
 	 */
 	public String getInventoryString(){
 		String s;
 		if(inventory.isEmpty()){
 			s = "The " + this.getClass().getName() + " has no items";
 		} else {
 			s = "The " + this.getClass().getName() + " has the following items:\n";
 			for(Item i: inventory){
 				s+= i.getName();
 				s+= "\n";
 			}
 		}
 		return s;
 	}
 }
