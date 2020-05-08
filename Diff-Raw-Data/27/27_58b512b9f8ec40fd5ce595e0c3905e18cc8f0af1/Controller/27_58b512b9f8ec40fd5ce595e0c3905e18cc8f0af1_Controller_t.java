 package edu.gatech.cs2340_sp13.teamrocket.findmythings;
 
 import java.util.ArrayList;
 
 public class Controller {
 	
 	//Holds items
 	private static ArrayList<Item> items;
 	
 	/**
 	 * Adds a few generic items to the arraylist
 	 */
 	public Controller() {
 		//TODO: Add
 		items = new ArrayList<Item>();
 		
 	}
 
 	/**
 	 * Adds an item to the arraylist
 	 * @param i
 	 */
 	public void addItem(Item i) {
 		items.add(i);
 	}
 	/**
 	 * create a new item (lost, found, donated or requested) 
 	 * @param m
 	 * @return
 	 */
 	public Item createItem(Member m) {
 		return null;
 	}
 	
 	/**
 	 * get info of item
 	 * @param i
 	 * @return
 	 */
	@SuppressWarnings("unused")
 	private ArrayList<Item> getInfo(Item i) {
 		return null;
 	}
 	
 	/**
 	 * get an arrayList of all the items that have the require characteristics
 	 * given in the parameter array
 	 * @param searches
 	 * @return
 	 */
 	public ArrayList<Item> doSearch(int[] searches) {
 		return null;
 	}
 	
 	
 	/**
 	 * get contact information of a member and give it back to the requester
 	 * @param m
 	 * @param requester
 	 */
 	public void getContact (Member m, Member requester) {
 		
 	}
 
 }
 
