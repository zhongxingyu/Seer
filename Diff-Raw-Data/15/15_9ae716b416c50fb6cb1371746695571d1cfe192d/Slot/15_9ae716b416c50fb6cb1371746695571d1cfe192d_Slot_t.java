 /**
  * Stores data about the preferences of an individual office hour slot.
  */
 
 package Items;
 
 import java.util.ArrayList;
 
 public class Slot {
     public Slot() {
         tutors = new ArrayList <Tutor> ();
     }
     
     // identifying information
     public int sid;
     public String name;
     public String day;
     public int hour;
     public String office;
 
     // preference information
     public int[] courses;
     public int[] adjacentSlotIDs;
     public Slot[] adjacentSlots;
     public Slot[] simultaneousSlots;
 
     public ArrayList <Tutor> tutors;
 
     public void assign (Tutor t) {
         tutors.add (t);
     }
     
     public boolean simultaneous(Slot s) {
        return (hour == s.hour) && str_equals (day, s.day);
     }
     
     public boolean adjacent (Slot s) {
         for (int i = 0; i < adjacentSlots.length; i++)
             if (adjacentSlots [i].equals(s))
                 return true;
         return false;
     }
     
     @Override
     public boolean equals(Object other) {
         if (!(other instanceof Slot)) return false;
         return sid == ((Slot)other).sid 
            && hour == ((Slot)other).hour
            && str_equals (name, ((Slot)other).name)
            && str_equals (day, ((Slot)other).day)
            && str_equals (office, ((Slot)other).office);
    }
    
    private static boolean str_equals (String a, String b) {
        return a == null ? b == null : a.equals(b);
     }
 
     @Override
     public String toString() {
         return sid + "@" + name;
     }
 }
