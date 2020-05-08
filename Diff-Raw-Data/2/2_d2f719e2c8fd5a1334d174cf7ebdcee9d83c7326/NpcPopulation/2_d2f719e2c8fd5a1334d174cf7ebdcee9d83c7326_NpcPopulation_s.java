 /*
  * @author afisher
  */
 
 import java.util.HashMap;
 
 public class NpcPopulation implements Population {
     private HashMap<Integer, NpcIndividual> individuals; // key is the ID#
 
     public NpcPopulation() {
         individuals = new HashMap<Integer, NpcIndividual>();
     }
 
     public int getMaxAge() {
         int max = 0;
         for (Individual i : individuals.values()) {
             int cur = i.getAge();
             if (cur > max) max = cur;
         }
         return max;
     }
 
     public int getMinAge() {
         int min = Integer.MAX_VALUE;
         for (Individual i : individuals.values()) {
             int cur = i.getAge();
             if (cur < min) min = cur;
         }
         return min;
     }
 
     public int getAverageAge() {
         int total = 0;
         for (Individual i : individuals.values()) {
             total += i.getAge();
         }
         return total / individuals.size();
     }
 
     public int getSize() { return individuals.size(); }
 
     public void add(Individual i) {
         individuals.put(((NpcIndividual) i).getID(), (NpcIndividual) i);
     }
 
     public void remove(Individual i) {
         individuals.remove(((NpcIndividual) i).getID());
     }
 
     public void remove(int id) {
         individuals.remove(id);
     }
 }
