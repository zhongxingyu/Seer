 package com.cs2340.spacetrader;
 
 import java.io.Serializable;
import java.util.ArrayList;
 
 public class Inventory implements Serializable
 {
 	public ArrayList<GoodData> goods;
 	
 	public Inventory()
 	{
 		goods = new ArrayList<GoodData>();
 	}
 	
 	/**
 	 * Gets a list of goods in this inventory object
 	 * @return
 	 */
 	public ArrayList<GoodData> getListofGoods()
 	{
 		return goods;
 	}
 	
 	/**
 	 * Adds a good to inventory
 	 * @param good
 	 * @param quantity
 	 */
 	public void add(Good good, int quantity)
 	{
 		for (int i = 0; i < goods.size(); i++) { // if the good is already present, it just adds to its quantity.
 			String name = goods.get(i).getGood().getName();
 			if (name.equals(good.getName())){
 				goods.get(i).increaseQuantityBy(quantity);
 				return;
 			}
 		}
 		
 		goods.add(new GoodData(good, quantity)); // if good is not already present, it adds it with the specified quantity.
 	}
 	
 	/*
 	 * If the good is not present, this method does not have any effect.
 	 */
 	public void remove(Good good, int quantity)
 	{
 		for (int i = 0; i < goods.size(); i++) { // if the good is already present, it just adds to its quantity.
 			String name = goods.get(i).getGood().getName();
 			if (name.equals(good.getName())){
 				goods.get(i).decreaseQuantityBy(quantity);
 				return;
 			}
 		}
 	}
 	
 	/**
 	 * Checks if this inventory contains a good
 	 * @param good
 	 * @return check result
 	 */
 	public boolean contains (Good good)
 	{
 		for (int i = 0; i < goods.size(); i++) { // if the good is already present, it just adds to its quantity.
 			String name = goods.get(i).getGood().getName();
 			if (name.equals(good.getName())){
 				return true;
 			}
 		}	
 		return false;
 	}
 
 	/**
 	 * Gets a good from this inventory
 	 * @param good
 	 * @return good, if exists
 	 */
 	public GoodData getGoodFromInventory(Good good)
 	{
 		GoodData goodInInventory = null;
 		for (int i = 0; i < goods.size(); i++) { // if the good is already present, it just adds to its quantity.
 			String name = goods.get(i).getGood().getName();
 			if (name.equals(good.getName())){
 				goodInInventory = goods.get(i);
 				return goods.get(i);
 			}
 		}
 		return goodInInventory;
 	}
 
 	/**
 	 * Gets a good based on the good's name
 	 * @param goodName
 	 * @return good, if exists
 	 */
 	public Good getGood(String goodName)
 	{
 		for (int i = 0; i < goods.size(); i++) { // if the good is already present, it just adds to its quantity.
 			String name = goods.get(i).getGood().getName();
 			if (name.equals(goodName)){
 				return goods.get(i).getGood();
 			}
 		}
 		//If the good is not in the list, return a dummy one
 		//TODO think of a better way to propagate result of a bad call
 		return new Good("awefulthings", 0, 0, 0, 0, 0, 0, false);
 	}
 	
 	/**
 	 * Gets number of a good in inventory
 	 * @param goodName
 	 * @return
 	 */
 	public int getGoodAmount(String goodName)
 	{
 		for (int i = 0; i < goods.size(); i++) { // if the good is already present, it just adds to its quantity.
 			String name = goods.get(i).getGood().getName();
 			if (name.equals(goodName)){
 				return goods.get(i).getQuantity();
 			}
 		}
 		return 0;
 	}
 	
 	public Object[] getArray()
 	{
 		return goods.toArray();
 	}
 
 	public GoodData[] array()
 	{
 		GoodData [] array = new GoodData[1];
 		goods.toArray(array);
 		return array;
 	}
 }
