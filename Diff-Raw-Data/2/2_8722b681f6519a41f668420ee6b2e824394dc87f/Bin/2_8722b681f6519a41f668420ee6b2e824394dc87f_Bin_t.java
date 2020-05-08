 package edu.upenn.cis.cis350.algorithmvisualization;
 
 import java.util.ArrayList;
 
 class Bin {
 	
 	private double capacity;
 	private double weight;
 	private double value;
 	private ArrayList<BinObject> contents;
 	
 	Bin(double capacity) {
 		this.capacity = capacity;
 		this.contents = new ArrayList<BinObject>();
 		this.weight = 0;
 		this.value = 0;
 	}
 	
 	public double getCapacity() { return capacity; }
 	public double getWeight() { return weight; }
 	public double getValue() { return value; }
 	
 	/**
 	 * Insert an object into the bin.
 	 * @param obj The object to be inserted.
	 * @return true if the object was inserted, false if it didn't fit.
 	 */
 	public boolean insert(BinObject obj) {
 		if (weight + obj.getWeight() > capacity) return false;
 		else {
 			weight += obj.getWeight();
 			value += obj.getValue();
 			contents.add(obj);
 			return true;
 		}
 	}
 	
 	/**
 	 * Remove an object from the bin.
 	 * @param obj The object to be removed.
 	 * @return true if the object was removed, false if the object wasn't in this bin.
 	 */
 	public boolean remove(BinObject obj) {
 		if (contents.contains(obj)) {
 			weight -= obj.getWeight();
 			value -= obj.getValue();
 			contents.remove(obj);
 			return true;
 		} else return false;
 	}
 	
 }
