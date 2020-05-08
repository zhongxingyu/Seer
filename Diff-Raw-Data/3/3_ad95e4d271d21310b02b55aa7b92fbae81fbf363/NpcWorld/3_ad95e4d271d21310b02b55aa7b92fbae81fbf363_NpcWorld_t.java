 /*
  * @author afisher
  */
 public class NpcWorld implements World {
     private NpcPopulation population;
 
     private int oldAge;
 
     private double mutationChance;
     private double crossoverChance;
     private double deathChance;
 
     public NpcWorld() {
         population = new NpcPopulation();
 
         oldAge = 100;
 
         mutationChance = 0.1;
         crossoverChance = 0.1;
         deathChance = 0.01;
     }
 
     public Dna crossover(Dna d1, Dna d2) {
         System.out.println("Crossing over!");
         return null;
     }
 
     public Dna mutate(Dna d) {
         System.out.println("Mutating some DNA!");
         return null;
     }
 
     public Population merge(Population p1, Population p2) {
         System.out.println("Merging two populations");
         return null;
     }
 
     public void step() {
         System.out.println("One step");
     }
 
     public void reproduce() {
         System.out.println("Reproducing");
     }
 
     public Individual mate(Individual i1, Individual i2) {
         System.out.println("Mating two individuals");
         return null;
     }
 }
