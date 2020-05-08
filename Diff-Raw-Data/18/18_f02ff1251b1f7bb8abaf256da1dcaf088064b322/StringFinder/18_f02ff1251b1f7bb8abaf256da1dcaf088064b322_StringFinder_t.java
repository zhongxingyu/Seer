 /**
  * Genetic Algorithm that tries to find a String
  * 
  * Author: Christophe VG
  */
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Collections;
 
 import java.util.Random;
 
 public class StringFinder {
   private int    popSize        = 2000;
   private int    maxIterations  = 10000;
 
   private double eliteRate      = 0.10;
   private int    eliteSize      = (int)(popSize * eliteRate);
   private double mutationRate   = 0.65;
   private int    mutationLimit  = (int)(Integer.MAX_VALUE * mutationRate);
 
   private String target         = "";
   private int    targetLength   = 0;
 
   private final  Random random = new Random();
 
   private class GA {
     private int    length;
     private String string;
     private int    fitness;
     private final Random random = new Random();
     
     public GA(int length) {
       this.length  = length;
       this.string  = ""; 
       this.fitness = 0;
     }
 
     public GA(String string) {
       this.length  = string.length();
       this.string  = string; 
       this.fitness = 0;
     }
     
     public void generateRandomString() {
       for(int i=0; i<this.length;i++ ) {
         int rand = this.random.nextInt(90);
         this.string += (char)(rand+32);
       }
     }
     
     public String getString() {
       return this.string;
     }
 
     public void setString(String string) {
       this.string = string;
     }
     
     public void assignFitness(int fitness) {
       this.fitness = fitness;
     }
 
     public int getFitness() {
       return this.fitness;
     }
 
     public String toString() {
       return this.string + " : " + this.fitness;
     }
   }
 
   private List<GA> population     = new ArrayList<GA>();
   private List<GA> nextGeneration = new ArrayList<GA>();
 
   public StringFinder(String string) {
     this.target = string;
     this.targetLength = string.length();
 
     this.initPopulation();
     this.breed();
   }
   
   private void initPopulation() {
     for( int i=0; i<this.popSize; i++ ) {
       GA ga = new GA(this.targetLength);
       ga.generateRandomString();
       this.population.add(ga);
     }
   }
   
   private void breed() {
     for( int i=0; i<this.maxIterations; i++ ) {
       this.calcFitness();
       this.sortPopulation();
       if( i % 100 == 0 ) {
         System.out.println( i + ". " + this.population.get(0) );
       }
       if( this.population.get(0).getFitness() == 0 ) {
         System.out.println( i + ". " + this.population.get(0) );
         break;
       }
       this.mate();
      this.acceptNextGeneration();
     }
   }
 
   private void calcFitness() {
     for( GA ga : this.population ) {
       String string = ga.getString();
       int fitness = 0;
       for( int c=0; c < this.targetLength; c++ ) {
         fitness += (string.charAt(c) == this.target.charAt(c)) ? 0 : 1;
       }
       ga.assignFitness(fitness);
     }
   }
   
   private void sortPopulation() {
    Collections.sort(this.population, new Comparator<GA>(){
      public int compare(GA ga1, GA ga2) {
        return ( ga1.getFitness() < ga2.getFitness() ) ? 0 : 1;
       }
     } );
   }
   
   private void mate() {
     this.selectElite();
     for( int i=this.eliteSize; i<this.popSize; i++ ) {
       GA mate1 = this.population.get(this.random.nextInt(this.popSize/2));
       GA mate2 = this.population.get(this.random.nextInt(this.popSize/2));
       int split = this.random.nextInt(this.targetLength);
       String part1 = mate1.getString().substring(0, split);
       String part2 = mate2.getString().substring(split, this.targetLength);
       GA offspring = new GA(part1 + part2);
       if( this.random.nextInt() < this.mutationLimit ) {
         this.mutate(offspring);
       }
       this.nextGeneration.add(offspring);
     }
   }
  
  private void acceptNextGeneration() {
    this.population.clear();
    for( GA ga : this.nextGeneration ) {
      this.population.add(ga);
    }
  }
 
   private int selectElite() {
     this.nextGeneration.clear();
     for( int i=0; i< this.eliteSize; i++ ) {
       this.nextGeneration.add(this.population.get(i));
     }
     return eliteSize;
   }
 
   private void mutate(GA ga) {
     int pos    = this.random.nextInt(this.targetLength);
     int change = this.random.nextInt(90) + 32;
     String string = ga.getString();
     char newChar = (char)((((int)string.charAt(pos)) + change) % 122);
     StringBuffer buf = new StringBuffer( string );
     buf.setCharAt(pos, newChar);
     ga.setString(buf.toString());
     
   }
   
   public static void main(String[] args) {
     new StringFinder("A long string, to test how long a string can become, " +
                  "before it really takes too many iterations to find an " +
                  "optimal solution. So far, it feels acceptable, but how " +
                  "far can I push this? The string/bytecode for a VM that " +
                  "can drive a robot will probably be longer?" );
   }
 }
