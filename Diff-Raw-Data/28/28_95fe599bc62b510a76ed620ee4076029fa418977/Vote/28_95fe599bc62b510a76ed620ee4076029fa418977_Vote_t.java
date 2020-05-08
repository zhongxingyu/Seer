 package se.diversify.webroti.data;
 
 
 /**
  * A vote
  */
 public class Vote {
 
    private final double value;
 
     /**
      * Create a new vote given a value
      * @param value the value of this vote
      */
     public Vote(double value) {
         this.value = value;
     }
 
     /**
      * Get the vote value
      * @return the vote value
      */
     public double getValue() {
         return value;
     }
 }
