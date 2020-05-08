 package de.ubercode.teatime;
 
 /**
  * A type of tea.
  */
 public class Tea {
     private String name;
     private int brewingTime;
 
     /**
      * Creates a new type of tea.
      * @param The name of this tea.
      * @param The time required for brewing this tea.
      */
     public Tea(String name, int brewingTime) {
         this.name = name;
         this.brewingTime = brewingTime;
     }
 
     /**
      * @return The name of this tea.
      */
    public String getName() {
         return name;
     }
 
     /**
      * @return The time required for brewing this tea.
      */
     public int getBrewingTime() {
         return brewingTime;
     }
 
     public String toString() {
         return name + ((brewingTime > 0)
                        ? " [" + String.valueOf(brewingTime) + "s]" : "");
     }
 }
