 /**
  * 
  */
 package datamodel.rucksack;
 
 import java.security.InvalidParameterException;
 import java.util.Vector;
 
 
 /**
  * class which represents the rucksack
  * 
  * @author Jakob Karolus, Kevin Munk
  * @version 1.0
  * 
  */
 public class Rucksack {
 
 	private int capacity;
 	private Vector<RucksackObject> objects= new Vector<RucksackObject>();
 
 	/**
 	 * constructor
 	 * 
 	 * @param capacity
 	 *            the max capacity of this rucksack
 	 * 
 	 */
 	public Rucksack(int capacity) {
 		if (capacity >= 0){
 			this.capacity = capacity;
 			this.objects.clear();
 		} else {
 			throw new InvalidParameterException("No legal capacity");
 		}
 	}
 
 	// TODO: implement necessary methods and attributes, f.e. getters, toString-method, equals...
 
 
 	/**
 	 * @return the capacity of the Rucksack.
 	 */
 	public int getCapacity(){
 		return this.capacity;
 	}
 
 
 	/**
 	 * set the capacity of the Rucksack new, if it legal.
 	 * @param capacity
 	 * 		the new capacity.
 	 */
 	public void setCapacity(int capacity){
 		if (capacity >= 0 && capacity >= getWeightOfRucksack()){
 			this.capacity = capacity;
 		} else {
 			throw new InvalidParameterException("No legal capacity");
 		}
 	}
 
 
 	/** 
 	 * @return the complete weight of the Rucksack.
 	 */
 	public int getWeightOfRucksack(){
 		int weight = 0;
 		
 		for (int i = 0; i < objects.size(); i++){
 			weight += objects.elementAt(i).getWeight();
 		}
 		
 		return weight;
 	}
 
 
 	/**
 	 * @return the complete value of all RucksackObjects.
 	 */
 	public int getValueOfRucksack(){
 		int value = 0;
 		
 		for (int i = 0; i < objects.size(); i++){
 			value += objects.elementAt(i).getValue();
 		}
 		
 		return value;
 	}
 
 
 	/**
 	 * @param i
 	 * @return object at position i
 	 */
 	public RucksackObject getObject(int i){
 		if (i < objects.size()){
 			return objects.elementAt(i);
 		} else {
 			throw new InvalidParameterException("No RucksackObject at this index");
 		}
 	}
 
 
 	public Rucksack clone(){
 		Rucksack r = new Rucksack(this.capacity);
 		for (int i = 0; i < objects.size(); i++){
 			r.insert(objects.elementAt(i));
 		}
 		return r;
 	}
 
 
 	/**
 	 * removes a element at a specific position i
 	 * @param i
 	 */
 	public void removeAt(int i){
 		if (i < objects.size()){
 			objects.remove(i);
 		} else {
 			throw new InvalidParameterException("No RucksackObject at this index");
 		}
 	}
 
 
 	/**
 	 * remove all RucksackObjects
 	 */
 	public void removeAll(){
 		objects.clear();
 	}
 
 
 	/**
 	 * check if r and this rucksack are equal bags.
 	 * @param r
 	 * @param r2
 	 * @return true if equal, else false.
 	 */
	public boolean equals(Rucksack r){
		if (r != null){
 			if (r.getCapacity() != getCapacity()
 					|| r.objects.size() != objects.size()
 					|| r.getValueOfRucksack() != getValueOfRucksack()
 					|| r.getWeightOfRucksack() != getWeightOfRucksack()){
 				return false;
 			} 
 			// capacity, size of objectsvector, value and weight is equal
 			// so check if the objects are the same
 			for (int i = 0; i < r.objects.size(); i++){
 				if (!(objects.contains(r.getObject(i)))){
 					return false;
 				}
 				if (!(r.objects.contains(getObject(i)))){
 					return false;
 				}
 			}
 		} else {
			throw new InvalidParameterException("The rucksack is null");
 		}
		return true;
 	}
 
 
 	/**
 	 * tries to add this RucksackObject to the rucksack
 	 * 
 	 * @param o
 	 *            the RucksackObject to insert
 	 * @return true if the object fits into the rucksack; otherwise false
 	 */
 	public boolean insert(RucksackObject o) {
 		if (objects.isEmpty() && o.getWeight() <= capacity){
 			objects.add(o);
 			return true;
 		} else {
 			if (o.getWeight() + getWeightOfRucksack() <= capacity){
 				objects.add(o);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public int getAmountOfObjects(){
 		return objects.size();
 	}
 
 }
