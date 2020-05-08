 package com.group5.diceroller;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Iterator;
 
 public class Dice
     implements Comparable<Dice>, Iterable<Integer> {
 
     int faces; // The number of faces each dice in this object has.
     int count; // The number of like dice in this object.
     int set_id; // ID of the set this dice belongs to.
     List<Integer> last_roll; // List of rolled values.
 
     // Default constructor, intended to use set() functions after creation
     public Dice() {
         faces = 6;
         count = 1;
         set_id = DiceSet.NOT_SAVED;
         last_roll = new ArrayList<Integer>();
     }
     
     // Copy constructor
     public Dice(Dice tocopy) {
         this.faces = tocopy.faces;
         this.count = tocopy.count;
         this.set_id = tocopy.set_id;
         this.last_roll = new ArrayList<Integer>();
        for (Integer i : tocopy.last_roll)
            last_roll.add(new Integer(i.intValue()));
     }
     
     
     public Dice(int faces, int count) {
     	this.faces = faces;
     	this.count = count;
     	this.set_id = DiceSet.NOT_SAVED;
     	last_roll = new ArrayList<Integer>();
     }
     
     public Dice(int faces, int count, int set_id) {
     	this.faces = faces;
     	this.count = count;
     	this.set_id = set_id;
     	last_roll = new ArrayList<Integer>();
     }
     
     /**
      * Randomizes the values of the dice in this object.
      */
     public void roll() {
         // create roller
         Random roller = new Random();
         roller.range(1, faces);
         
         // clear previous results
         last_roll.clear();
         
         // do roll for each die and record result
         for (int d = 0; d < count; d++) {
             last_roll.add(roller.roll());
         }
     }
 
     /**
      * Saves this dice in the database with the associated set id.
      * TODO implement:Padraic
      *
      * @param set_id The ID of the set this dice belongs to.
      */
     public void save(int set_id) {
     }
 
     /**
      * Returns true iff the two dice have the same count, number of faces, and
      * set id.
      *
      * @param other The Dice to compare against
      */
     public boolean equals(Dice other) {
     	if ((this.count) == (other.count) && (this.faces == other.faces) && (this.set_id == other.set_id)) 
     		return true;
     	else
     		return false;
     }
 
     /**
      * Compares two dice based on their face value and count.
      *
      * @param other The dice to compare against.
      */
     public int compareTo(Dice other) {
         if (this.faces != other.faces)
         	return (this.faces - other.faces);
         else
         	return (this.count - other.count);
     }
 
     /**
      * Returns an iterator over the rolled values in this Dice object.
      */
     public Iterator<Integer> iterator() {
         return last_roll.iterator();
     }
     
     public int getFaces() {
         return faces;
     }
 
     public void setFaces(int faces) {
         this.faces = faces;
     }
 
     public int getCount() {
         return count;
     }
 
     public void setCount(int count) {
         this.count = count;
     }
 
     public int getSet_id() {
         return set_id;
     }
 
     public void setSet_id(int set_id) {
         this.set_id = set_id;
     }
 
     public List<Integer> getLast_roll() {
         return last_roll;
     }
 
     public void setLast_roll(List<Integer> last_roll) {
         this.last_roll = last_roll;
     }
 }
