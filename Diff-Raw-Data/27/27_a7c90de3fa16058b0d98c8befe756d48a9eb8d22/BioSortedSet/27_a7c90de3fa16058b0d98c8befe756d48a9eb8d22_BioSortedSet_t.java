 package nzgot.core.util;
 
 import java.util.TreeSet;
 
 /**
  * BioSortedSet
  * @author Walter Xie
  */
 public class BioSortedSet<E> extends TreeSet<E> implements Comparable<E>{
 
 //    public SortedSet<E> elementsSet = new TreeSet<>(); // elements are unique, and sorted
 
     protected String name;
 
     public BioSortedSet(String name) {
         super();
         setName(name);
     }
 
     /**
      * add a unique element to the set, if duplication found, then throw Exception
      * @param e
      * @throws IllegalArgumentException
      */
     public void addUniqueElement(E e) throws IllegalArgumentException {
         if (!add(e))
             throw new IllegalArgumentException("Error: find duplicate " +
                     e.toString() + " in " + getName() + " !");
     }
 
     /**
      * given name to find the element matching this name in the set
      * @param name
      * @return
      * @throws IllegalArgumentException
      */
     public E getUniqueElement(String name) throws IllegalArgumentException {
         for (E e : this) {
            if (e.toString().contentEquals(name))
                return e;
         }
         return null;
     }
 
     public String getName() {
         if (name == null ) setName("Unknown");
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String toString() {
         return getName();
     }
 
     public boolean equalsIgnoreCase(String name) {
         return this.name.equalsIgnoreCase(name);
     }
 
     @Override
     public int compareTo(E o) {
         return name.compareTo(o.toString());
     }
 }
