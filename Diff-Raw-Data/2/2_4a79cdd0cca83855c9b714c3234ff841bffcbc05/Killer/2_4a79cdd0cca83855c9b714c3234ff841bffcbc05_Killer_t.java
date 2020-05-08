 package net.krinsoft.killsuite;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author krinsdeath
  */
 public class Killer {
     private final int ID;
     private final String name;
 
     private final int[] killed = new int[32];
 
     private static Map<String, Integer> totals = new HashMap<String, Integer>();
 
     public Killer(String name, Map<Monster, Integer> kills) {
         this.ID = name.hashCode();
         this.name = name;
         for (Monster m : kills.keySet()) {
             killed[m.ordinal()] = kills.get(m);
         }
     }
     
     public String getName() {
         return this.name;
     }
     
     public int get(String field) {
         Monster m = Monster.getType(field);
         if (m == null) { return -1; }
         return killed[m.ordinal()];
     }
 
     public int get(Monster field) {
         return killed[field.ordinal()];
     }
     
     public int update(String field) {
         Monster m = Monster.getType(field);
         if (m == null) { return -1; }
         killed[m.ordinal()] += 1;
         attenuate();
         return killed[m.ordinal()];
     }
 
     private void attenuate() {
         Integer total = totals.get(this.name);
        totals.put(this.name, total != null ? ++total : 1);
     }
 
     public int getAttenuation() {
         return totals.get(this.name);
     }
 
     public void resetAttenuation() {
         totals.put(this.name, 0);
     }
 
     public long total() {
         long total = 0;
         for (Integer k : killed) {
             total += k;
         }
         return total;
     }
 
     @Override
     public String toString() {
         return "Killer{name=" + this.name + "}@" + this.ID;
     }
     
     @Override
     public int hashCode() {
         int hash = 19;
         hash = hash * 15 + (this.toString().hashCode());
         hash = hash + this.ID;
         hash = hash + Arrays.toString(this.killed).hashCode();
         return hash;
     }
     
     @Override
     public boolean equals(Object o) {
         if (o == null) { return false; }
         if (this == o) { return true; }
         if (this.getClass() != o.getClass()) { return false; }
         Killer killer = (Killer) o;
         return killer.hashCode() == this.hashCode();
     }
 }
