 package item;
 
import obstacle.*;
 import game.*;
 import grid.Square;
 
 /**
  * This abstract class represents an item.
  * All items must inherit from this class
  * 
  * @author 	Groep 8
  * @version	February 2013
  */
 public abstract class Item {
 	
 	/**
 	 * Represents the inventory, in which the item is.
 	 */
 	private Inventory inventory;
 	
 	/**
 	 * Represents the location square of the item.
 	 */
 	private Square location;
 	
 	/**
 	 * Represents whether the item is used or not.
 	 */
 	private boolean used;
 	
 	
 	/********************
 	 * GETTERS & SETTERS
 	 ********************/
 	
 	/**
 	 * Returns the inventory, in which the item
 	 * currently is.
 	 * 
 	 * @return	The inventory of the item.
 	 * 			Null, if there is none.
 	 */
 	public Inventory getInventory() {
 		return inventory;
 	}
 
 	/**
 	 * Sets the current inventory to the given inventory.
 	 * 
 	 * @param 	owner
 	 * 			The new inventory in which the item is.
 	 */
 	public void setInventory(Inventory inventory) {
 		this.inventory = inventory;
 	}
 	
 	public Square getLocation() {
 		return location;
 	}
 
 	private void setLocation(Square location) {
 		this.location = location;
 	}
 
 	/**
 	 * Returns whether the item is used or not.
 	 * 
 	 * @return	True if the item is used.
 	 * 			False if the item is not used.
 	 */
 	public boolean isUsed() {
 		return used;
 	}
 
 	/**
 	 * Sets whether the item is used or not. 
 	 * 
 	 * @param 	used
 	 * 			Boolean, which is true if the item is used and
 	 * 			false if not.
 	 * 
 	 * @post	The 'used'-variable will be set the the given boolean.
 	 * 			| new.used = used
 	 */
 	protected void setUsed(boolean used) {
 		this.used = used;
 	}
 	
 	/*********************
 	 * CORE FUNCTIONALITY
 	 *********************/
 
 	/**
 	 * Use the item.
 	 * 
 	 * @effect	The 'used' variable will be set to true.
 	 * 			| setUsed(true)
 	 */
 	public void use() {
 		setUsed(true);
 	}
 
 	/**
 	 * Indicates whether this item can be on a square
 	 * with the given list of items.
 	 * 
 	 * @param 	items
 	 * 			The items from which must be investigated
 	 * 			that they can be on the same square with this item.
 	 * 			
 	 * @return 	True, if this item can be on the same square with the given items.
 	 * 			False, if not.
 	 */
 	public abstract boolean canBeOnSquareWith(Item... items);
 	
 	/**
 	 * This method will specify what happens when you step on a normal square containing this item.
 	 */
 	public abstract void onStepOnNormal();
 	
 	/**
 	 * This method will specify what happens when you leave a normal square containing this item.
 	 */
 	public abstract void onLeaveNormal();
 	
 	/**
 	 * This method will specify what happens when you start on a normal square containing this item.
 	 */
 	public abstract void onStartOnNormal();
 	
 	/**
 	 * This method will specify what happens when you step on a power failure square containing this item.
 	 */
 	public abstract void onStepOnPowerFail();
 	
 	/**
 	 * This method will specify what happens when you leave a power failure square containing this item.
 	 */
 	public abstract void onLeavePowerFail();
 	
 	/**
 	 * This method will specify what happens when you start on a power failure square containing this item.
 	 */
 	public abstract void onStartOnPowerFail();
 	
 }
