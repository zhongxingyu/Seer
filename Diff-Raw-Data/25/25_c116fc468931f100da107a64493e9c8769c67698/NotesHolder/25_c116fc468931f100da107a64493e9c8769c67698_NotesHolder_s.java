 package ua.in.dmitry404.money;
 
 import java.util.Collections;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 /**
  * Class that contains money with ability to put and get them
  *
  * @author Dmitriy Butakov
  */
 public class NotesHolder implements Cloneable {
     private SortedMap<Integer, Integer> notes = new TreeMap<Integer, Integer>(Collections.reverseOrder());
 
     /**
      * Put new value with specified quantity (rewrite old quantity if this value exists)
      *
      * @param value value of banknote
      * @param quantity quantity of banknotes
      */
     public void put(int value, int quantity) {
         notes.put(value, quantity);
     }
 
     /**
      * Add (increment) quantity of notes to specified value
      *
      * @param value value of banknote
      * @param quantity quantity of banknotes
      */
     public void add(int value, int quantity) {
         if (notes.containsKey(value)) {
             quantity += notes.get(value);
         }
         put(value, quantity);
     }
 
     /**
      * Pop (get with removing) values with specified quantity
      *
      * @param value banknote value
      * @param quantity quantity of banknotes
      * @return true if value will be removed, false otherwise
      */
     public boolean pop(int value, int quantity) {
         int currentQuantity = getQuantity(value);
         if (currentQuantity > quantity) {
             put(value, currentQuantity - quantity);
         } else if(currentQuantity == quantity) {
             notes.remove(value);
         } else {
             return false;
         }
 
         return true;
     }
 
     /**
      * Check of existing banknote
      *
      * @param value banknote value
      * @return true if banknote exists, false otherwise
      */
     public boolean contains(int value) {
         return notes.containsKey(value);
     }
 
     /**
      * Return available banknotes values
      *
      * @return Set of existing banknotes
      */
     public Set<Integer> getValues() {
         return notes.keySet();
     }
 
     /**
      * Return quantity of banknotes with specified value
      *
      * @param value banknote value
      * @return quantity of banknotes
      */
     public int getQuantity(int value) {
         return notes.get(value);
     }
 
     /**
      * Check NotesHolder on empty
      *
      * @return true if holder is empty, false otherwise
      */
     public boolean isEmpty() {
         return (notes.size() == 0);
     }
 
     /**
      * Clone implementation of NotesHolder
      *
      * @return cloned instance
      */
     public NotesHolder clone() {
         try {
             NotesHolder holder = (NotesHolder) super.clone();
             holder.notes = new TreeMap<Integer, Integer>(notes);
 
             return holder;
         } catch (CloneNotSupportedException e) {
             return null;
         }
     }
 }
