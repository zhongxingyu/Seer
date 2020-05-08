 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.bff.javampd.objects;
 
 /**
  * Abstract base class for all MPD related objects.
  *
  * @author Bill Findeisen
  */
 public abstract class MPDItem implements Comparable<MPDItem> {
 
     private String name;
 
     /**
      * Default constructor for a MPDItem
      */
     public MPDItem() {
     }
 
     /**
      * Constructor for a MPDItem
      *
      * @param name the name of the item
      */
     public MPDItem(String name) {
         this.name = name;
     }
 
     /**
      * Returns the name of the item.
      *
      * @return the name
      */
     public String getName() {
         return name;
     }
 
     /**
      * Sets the name of the item.
      *
      * @param name the name to set
      */
     public void setName(String name) {
         this.name = name;
     }
 
     @Override
     public String toString() {
         return getName();
     }
 
     @Override
     public boolean equals(Object object) {
 
         if (this == object) {
             return (true);
         }
 
         if ((object == null) || (object.getClass() != this.getClass())) {
             return false;
         }
 
        MPDItem other = (MPDItem) object;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
        hash = 31 * hash + name.length();
        hash = 31 * hash + (null == name ? 0 : name.hashCode());
         return (hash);
     }
 
     @Override
     public int compareTo(MPDItem item) {
    	if (this.toString() == null)
    		return -1;
    	
    	if (item.toString() == null)
    		return 1;
    	
         return this.toString().compareTo(item.toString());
     }
 }
