 /**
  * A weight function. Keeps the grass trimmed and the hedges tidy.
  * Implements the weight function described on the HKN website.
  */
 
 package Weighting;
 
 import Items.Slot;
 import Items.Tutor;
 
 public class Gardener implements Weighting {
 
     public double weight(Tutor t, Slot s) {
         double retval = 0.00;
 
         // Return -1 if tutor is already in that timeslot
         if (t.slot != null) {
             if (t.slot.simultaneous(s)) {
                 return -1;
             }
         }
 
         // Adjacencies: +1 if adjacent or no preference
         if (t.adjacentPref == 0) {
            retval += 1;
         } else if (t.adjacentPref == 1) {
             for (Slot adjslot : s.adjacentSlots) {
                 if (adjslot.tutor == t) {
                    retval += 1;
                 }
             }
         }
 
         // Time slots: +1 if not preferred, +10 if ambivalent, +20 if preferred
         retval += timePrefToW[t.timeSlots[s.sid]];
 
         // Office preferences: +[1, 5] depending on office preference
         retval += (t.officePrefs[s.sid] + 3);
 
         return retval;
     }
 
     // 0: unavailable, 1: ambivalent, 2: prefer
     private static double[] timePrefToW = { 1, 10, 20 };
 
 }
