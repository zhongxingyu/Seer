 /** @author Eldar Damari, Ory Band. */
 
 package company;
 
 import java.lang.Comparable;
 
 
 public class Laboratory implements Comparable<Laboratory> {
 
     final private String name;
     final private String specialization;
     final private int numberOfScientists;
     final private int price;
 
 
     public Laboratory(
             String headLabName,
             String specialization,
             int numOfScientists,
             int price) {
 
         this.name = new String(headLabName);
         this.specialization = new String(specialization);
         this.numberOfScientists = numOfScientists;
         this.price = price;
     }
 
 
     /**
      * Sorts labs by most scientists, then by cheapest price.
      *
      * @param l lab to compare against.
      */
     public int compareTo(Laboratory l) {
         if (this.numberOfScientists == l.getNumOfScientists()) {
             return - (this.price - l.getPrice());
         } else {
             return this.numberOfScientists - l.getNumOfScientists();
         }
     }
 
 
     // Getters.
     public String getName() {
         return this.name;
     }
 
     public String getSpecialization() {
         return this.specialization;
     }
 
     public int getNumOfScientists() {
         return this.numberOfScientists;
     }
 
     public int getPrice() {
         return this.price;
     }
 
 
     public String toString() {
 
         StringBuilder result = new StringBuilder();
         String N = System.getProperty("line.separator");
 
         result.append(N);
         result.append(this.price + "$, ");
         result.append(this.name + ", ");
         result.append(this.specialization + ", ");
        result.append(this.numberOfScientists + " scientists");
 
         return result.toString();
     }
 }
