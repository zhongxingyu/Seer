 package com.ninja_squad.codestory.planning;
 
 import com.google.common.base.Predicate;
 
 import java.util.Iterator;
 import java.util.List;
 
 public class Planning implements Comparable<Planning> {
 
     private final List<Vol> path;
 
     private Integer gain;
 
     public Planning(List<Vol> path) {
         this.path = path;
     }
 
     static final Predicate<Planning> POSSIBLE = new Predicate<Planning>() {
         @Override
         public boolean apply(Planning input) {
             return input.isPossible();
         }
     };
 
     public boolean isPossible() {
         boolean possible = true;
         if (path.size() > 1) {
             Vol previous = null;
             for (Iterator<Vol> iVol = path.iterator(); possible && iVol.hasNext(); ) {
                 Vol current = iVol.next();
                 if (previous != null) {
                     possible = (current.getDepart() >= previous.getArrivee());
                 }
                 previous = current;
             }
         }
         return possible;
     }
 
     public int getGain() {
         if (this.gain == null) {
             int computedGain = 0;
             for (Vol vol : getPath()) {
                 computedGain += vol.getPrix();
             }
             this.gain = Integer.valueOf(computedGain);
         }
         return this.gain.intValue();
     }
 
     public List<Vol> getPath() {
         return path;
     }
 
     @Override
     public int compareTo(Planning o) {
        return Integer.compare(getGain(), o.getGain());
     }
 }
