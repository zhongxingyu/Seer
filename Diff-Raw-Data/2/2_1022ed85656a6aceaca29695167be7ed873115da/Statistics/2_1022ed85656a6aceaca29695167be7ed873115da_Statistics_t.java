 package galapagos;
 
 public class Statistics {
     private int population;
     private int born;
     private int deadByAge;
     private int deadByTicks;
     
     public Statistics () {
         population = 0;
         born = 0;
         deadByAge = 0;
         deadByTicks = 0;
     }
     
    public int getPopulation () {
         return population;
     }
     
     public int getBorn () {
         return born;
     }
     
     public int getDeadByAge () {
         return deadByAge;
     }
     
     public int getDeadByTicks () {
         return deadByTicks;
     }
     
     public void incPopulation () {
         population++;
     }
     
     public void decPopulation () {
         population--;
     }
     
     public void incBorn () {
         born++;
     }
     
     public void incDeadByAge () {
         deadByAge++;
     }
     
     public void incDeadByTicks () {
         deadByTicks++;
     }
 }
