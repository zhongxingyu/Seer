package com.cs2340.spacetrader;
 
 import java.io.Serializable;
 
 /**
  * 
  * @author The Droids You Are Looking For
  * @version 1.0 * This class helps to encapsulate the concept of having a
  *          certain quantity of a specific good. This class has just two data
  *          fields, Good and the amount of it held in an inventory. The
  *          Inventory class holds a list of GoodData objects, not Good objects.
  * 
  */
 public class GoodData implements Serializable {
 	/** good object for this goodData **/
 	private Good good;
 
 	/** quantity of the good **/
 	private int quantity;
 
 	/**
 	 * Constructor for GoodData class
 	 * 
 	 * @param good
 	 * @param quantity
 	 */
 	public GoodData(Good good, int quantity) {
 		this.good = good;
 		this.quantity = quantity;
 	}
 
 	/**
 	 * Gets the good
 	 * 
 	 * @return good
 	 */
 	public Good getGood() {
 		return good;
 	}
 
 	/**
 	 * Gets the quantity of a good
 	 * 
 	 * @return quantity
 	 */
 	public int getQuantity() {
 		return quantity;
 	}
 
 	/**
 	 * Increases quantity by some integer
 	 * 
 	 * @param delta
 	 */
 	public void increaseQuantityBy(int delta) {
 		quantity = quantity + delta;
 	}
 
 	/**
 	 * This will be handled while making a transaction.
 	 * 
 	 * @param delta
 	 */
 	public void decreaseQuantityBy(int delta) {
 		quantity = quantity - delta;
 	}
 }
