 import java.util.ArrayList;
 import java.util.Set;
 import java.util.Collections;
 /*
  * @author afisher
  */
 public class NpcWorld implements World {
     private NpcPopulation population;
 
     private ArrayList<NpcIndividual> matingPoolMales;
     private ArrayList<NpcIndividual> matingPoolFemales;
 
     private ArrayList<NpcIndividual> migrationPool;
 
     // current availability
     private int eatingAvailability;
     private int sleepingAvailability;
 
     private int stepNumber;
 
     private int numMales;
     private int numFemales;
 
     private boolean[] availableIDs = new boolean[Const.MAX_POPULATION_SIZE];
 
     public NpcWorld() {
         population = new NpcPopulation();
 
         matingPoolMales   = new ArrayList<NpcIndividual>();
         matingPoolFemales = new ArrayList<NpcIndividual>();
         migrationPool     = new ArrayList<NpcIndividual>();
 
         for (int i = 0; i < availableIDs.length; i++) {
             availableIDs[i] = true;
         }
 
         numMales   = 0;
         numFemales = 0;
 
         // initialize the population
         for (int i = 0; i < Const.INITIAL_POPULATION_SIZE; i++) {
             NpcIndividual ind = new NpcIndividual(i);
 
             if (ind.getGender() == Const.MALE) numMales++;
             else numFemales++;
 
             population.add(ind);
 
             availableIDs[i] = false;
         }
 
         stepNumber = 0;
 
         eatingAvailability   = Settings.eatingCapacity;
         sleepingAvailability = Settings.sleepingCapacity;
     }
 
     public NpcPopulation getPopulation() {
         return population;
     }
 
     public void step() {
         stepNumber++;
 
         freeAvailabilities();
 
         ArrayList<Integer> keys = new ArrayList<Integer>(population.getKeys());
         Collections.shuffle(keys);
 
         matingPoolMales   = new ArrayList<NpcIndividual>();
         matingPoolFemales = new ArrayList<NpcIndividual>();
         migrationPool     = new ArrayList<NpcIndividual>();
 
         numMales   = 0;
         numFemales = 0;
 
         for (Integer k : keys) {
             NpcIndividual ind = (NpcIndividual)population.get(k);
 
             boolean killed = reap(ind);
             if (!killed) {
                 makeAction(ind);
                 if (ind.getGender() == Const.MALE) numMales++;
                 else numFemales++;
             }
         }
 
         reproduce(); // mate the individuals who chose to mate
     }
 
     // genetic operators
     public Dna crossover(Dna d1, Dna d2) {
         boolean[] n1;
         boolean[] n2;
 
         // 50/50 chance of which DNA to start out with
         if (Math.random() < 0.5) {
             n1 = d1.getNucleotides();
             n2 = d2.getNucleotides();
         } else {
             n1 = d2.getNucleotides();
             n2 = d1.getNucleotides();
         }
 
         boolean[] newDNA = new boolean[n1.length];
 
         boolean crossover = true;
 
         for (int i = 0; i < newDNA.length; i++) {
             if (Math.random() < Settings.crossoverChance) {
                 crossover = !crossover;
             }
 
             if (crossover) {
                 newDNA[i] = n1[i];
             } else {
                 newDNA[i] = n2[i];
             }
         }
 
         return new NpcDna(newDNA);
     }
 
     public Dna mutate(Dna d) {
         boolean[] nucleotides = d.getNucleotides();
         for (int i = 0; i < nucleotides.length; i++) {
             if (Math.random() < Settings.mutationChance) {
                 nucleotides[i] = !nucleotides[i];
             }
         }
         return new NpcDna(nucleotides);
     }
 
     private void freeAvailabilities() {
         eatingAvailability   = Settings.eatingCapacity;
         sleepingAvailability = Settings.sleepingCapacity;
 
         // go through all of the individuals and decrement availabilities
         // if the individual is not done with its action
         for (NpcIndividual i : population.getIndividuals()) {
             if (i.getStepsRemaining() != 0) {
                 if (i.getCurrentAction() == Const.EATING) {
                     eatingAvailability--;
                 } else if (i.getCurrentAction() == Const.SLEEPING) {
                     sleepingAvailability--;
                 }
             }
         }
     }
 
     private void makeAction(NpcIndividual ind) {
         // make the individual choose an action and act on it
         // based on the current state of the population
         if (ind.getStepsRemaining() == 0) {
             ArrayList<Integer> actions = new ArrayList<Integer>();
 
             if (eatingAvailability   > 0) actions.add(Const.EATING  );
             if (sleepingAvailability > 0) actions.add(Const.SLEEPING);
             if (Settings.migrationEnabled) actions.add(Const.MIGRATING);
             actions.add(Const.MATING);
             actions.add(Const.PLAYING);
 
             int action = ind.chooseAction(actions);
 
             if      (action == Const.EATING)    eatingAvailability--;
             else if (action == Const.SLEEPING)  sleepingAvailability--;
             else if (action == Const.MATING) {
                 if (ind.getGender() == Const.MALE) {
                     matingPoolMales.add(ind);
                 } else {
                     matingPoolFemales.add(ind);
                 }
             }
             else if (action == Const.MIGRATING) {
                 migrationPool.add(ind);
                 removeIndividual(ind);
             }
 
         }
 
         ind.decreaseStepsRemaining();
 
         int action = ind.getCurrentAction();
 
         if      (action == Const.EATING)   ind.decreaseHunger();
         else if (action == Const.SLEEPING) ind.decreaseExhaustion();
 
         ind.increaseHunger();
         ind.increaseExhaustion();
         ind.increaseAge();
     }
 
     private boolean reap(NpcIndividual ind) {
         if (ind.getHunger()     > Settings.maxHunger
         ||  ind.getExhaustion() > Settings.maxExhaustion
         ||  Math.random()       < chanceOfDeath(ind.getAge()))
         {
             removeIndividual(ind);
             return true;
         }
         return false;
     }
 
     public void reproduce() {
         for (int i = 0; i < matingPoolMales.size(); i++) {
             if (i < matingPoolMales.size() && i < matingPoolFemales.size()) {
                 NpcIndividual male   = matingPoolMales.get(i);
                 NpcIndividual female = matingPoolFemales.get(i);
 
                 Individual child = mate(male, female);
                 if (child != null) {
                     population.add(child);
 
                     availableIDs[((NpcIndividual)child).getID()] = false;
 
                     male.mated();
                     female.mated();
                 }
             }
         }
     }
 
     public Individual mate(Individual i1, Individual i2) {
         Dna dna = mutate(crossover(i1.getDna(), i2.getDna()));
 
         for (int i = 0; i < availableIDs.length; i++) {
             if (availableIDs[i]) {
                 return new NpcIndividual(i, (NpcDna)dna);
             }
         }
 
         return null;
     }
 
     public double chanceOfDeath(int age) {
         if (age < Settings.oldAge) return Settings.deathChance;
 
         int ageDiff = age - Settings.oldAge;
         double curDeathChance = Settings.deathChance + Settings.deathChanceChange * ageDiff;
 
         return Math.min(curDeathChance, Settings.deathChanceMax);
     }
 
     public int percentMale() {
         return (int)((100.0 * numMales) / population.getSize());
     }
 
     public int percentFemale() {
         return (int)((100.0 * numFemales) / population.getSize());
     }
 
     public ArrayList<NpcIndividual> getMigrationPool() {
         return migrationPool;
     }
     
     public int getStepNumber(){
         return stepNumber;
     }
 
     public void addIndividual(NpcIndividual ind) {
         int id = 0;
         while(!availableIDs[id]) {
             id++;
            if (id >= availableIDs.length) return;
         }
         ind.setID(id);
         availableIDs[id] = false;
         ind.migrated();
         population.add(ind);
     }
 
     public void removeIndividual(NpcIndividual ind) {
         population.remove(ind);
         availableIDs[ind.getID()] = true;
     }
 }
