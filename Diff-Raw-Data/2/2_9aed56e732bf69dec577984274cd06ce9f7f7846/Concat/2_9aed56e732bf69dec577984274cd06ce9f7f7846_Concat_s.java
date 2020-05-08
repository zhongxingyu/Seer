 package music;
 
 /**
  * Concat represents two pieces of music played one after the other.
  */
 public class Concat implements Music {
     private final Music first;
     private final Music second;
     // Rep invariant: m1, m2 != null
     
     private void checkRep() {
     }
     
     /**
      * Make a Music sequence that plays m1 followed by m2.
      * @param m1 music to play first
      * @param m2 music to play second
      */
     public Concat(Music m1, Music m2) {
         this.first = m1;
         this.second = m2;
         checkRep();
     }
     
     /**
      * @return first piece in this concatenation
      */
     public Music first() {
         return first;
     }
     
     /**
      * Returns the sum of first duration and second duration
      * @return duration of this concatenation
      */
     public double duration() {
         return first.duration()+second.duration();
     }
 
     /**
      * @param v visitor to apply to this object
      */
     public <T> T accept(Visitor<T> v) {
         return v.on(this);
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + first.hashCode();
         result = prime * result + second.hashCode();
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         final Concat other = (Concat) obj;
         if (!first.equals(other.first))
             return false;
         if (!second.equals(other.second))
             return false;
         return true;
     }
 
     @Override
     public String toString() {
         return first + " " + second;
     }
 }
