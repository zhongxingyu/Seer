 package com.group5.diceroller;
 
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 public class DiceSet
     implements Iterable<Dice> {
 
     Set<Dice> dice_set;
     int id;
     String name;
     int modifier;
 
     public static final int NOT_SAVED = -1;
 
     /**
      * Constructs a DiceSet with the given id, name, and modifier.
      */
     public DiceSet(int id, String name, int modifier) {
         this.id = id;
         this.name = name;
         this.modifier = modifier;
         dice_set = new TreeSet<Dice>();
     }
 
     /**
      * Construts a DiceSet with the given name and modifier. The set_id is
      * defaulted to NOT_SAVED.
      */
     public DiceSet(String name, int modifier) {
         this(NOT_SAVED, name, modifier);
     }
 
     /**
      * Constructs a new set as a deep copy of the given dice set.
      */
     public DiceSet(DiceSet other) {
         this(other.id, other.name, other.modifier);
         for (Dice d : other)
             add(new Dice(d));
     }
 
     /**
      * Loads all the dice sets in the local database into a list.
      * @return A list of dice sets from the database
      */
     public static List<DiceSet> LoadAllFromDB() {
         return DiceDBOpenHelper.getDB().loadSets();
     }
 
     /**
      * Returns a standard dice notation string describing the dice in this set.
      * For example: "1D6, 2D4, 3D8". If a set has no dice, this returns the
      * empty string.
      * @return A description of this dice set.
      */
     public String description() {
         StringBuilder ret = new StringBuilder();
         final String seperator = ", ";
         boolean first = true;
 
         ret.append('(');
         for (Dice d : dice_set) {
             if (!first)
                 ret.append(seperator);
             first = false;
             ret.append(d.count);
             ret.append('D');
             ret.append(d.faces);
         }
         ret.append(')');
 
         // At least one item in the set
         if (!first) {
             if (modifier > 0)
                 ret.append(" + " + modifier);
             return ret.toString();
         }
 
         // Nothing in the set, return empty string.
         return "";
     }
 
     /**
      * Returns the user-defined name for this Dice Set. If the user did not
      * define a name, this should return the empty string.
      * @return The user-defined name of this dice set.
      */
     public String name() {
         return name;
     }
 
     /**
      * Saves this set to the database. If the ID is DiceSet.NOT_SAVED, then it
      * creates a new set and updates this set's id to represent the newly-saved
      * id.
      */
     public void save() {
         DiceDBOpenHelper.getDB().saveSet(this);
     }
 
     /**
      * Deletes the representation of this set in the database. Also deletes all
      * dice associated with this set.
      */
     public void delete() {
         DiceDBOpenHelper.getDB().deleteSet(this);
     }
 
     /**
      * Adds a dice to this DiceSet. If the dice to add has the same face count
      * as another Dice in the set, its count is added into the count of the
      * Dice in the set.
      * @param dice The dice to add.
      * @return True if the dice was added to the set, false if another dice
      *   with the same number of faces was already in the set.
      */
     public boolean add(Dice dice) {
         return dice_set.add(dice);
     }
 
     /**
      * Removes a dice from the set which has the given number of faces.
      * @param dice The dice to remove.
      * @return True if a Dice was removed from the set, false otherwise.
      */
     public boolean remove(Dice dice) {
         return dice_set.remove(dice);
     }
 
     /**
      * Removes all dice from this set.
      */
     public void clear() {
         dice_set.clear();
     }
 
     /**
      * Checks if two dice sets contain the same dice. 
      * @return True if for every Dice of a given face and count in one DiceSet,
      *   a dice with the same face and count is in the other.
      */
     public boolean equals(DiceSet other) {
         for (Dice d : other) {
             if (!dice_set.contains(d))
                 return false;
         }
         return true;
     }
 
     /**
      * This version of equals needed to use DiceSets in a hash table.
      */
     public boolean equals(Object other) {
         if (other instanceof DiceSet)
             return equals((DiceSet) other);
         return super.equals(other);
     }
 
     /**
      * Returns a hash code of this dice set.
      */
     public int hashCode() {
         return label().hashCode();
     }
 
     /**
      * Randomizes the dice in this set.
      */
     public void roll() {
         for (Dice d : dice_set)
             d.roll();
     }
 
     /**
      * Returns an itrator over the dice in this set.
      */
     public Iterator<Dice> iterator() {
         return dice_set.iterator();
     }
 
     /**
      * Returns the number of dice in this set.
      */
     public int size() {
         return dice_set.size();
     }
 
     /**
      * Returns the sum of the dice in this set.
      */
     public int sum() {
        int sum = 0;
         for (Dice d : dice_set) {
             for (Integer i : d) {
                 sum += i.intValue();
             }
         }
         return sum;
     }
 
     /**
      * Returns human-readable label for this set.
      * @return The name if it is available, the description if not.
      */
     public String label() {
         String label = name();
         if (label.equals(""))
             label = description();
         return label;
     }
 }
