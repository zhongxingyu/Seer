 package jgpfun;
 
 import jgpfun.jgp.OpCode;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import jgpfun.crossover.CrossoverOperator;
 import jgpfun.crossover.TwoPointCrossover;
 import jgpfun.util.EvoUtils;
 import jgpfun.util.MutationUtils;
 import jgpfun.world2d.World2d;
 
 /**
  *
  * @author hansinator
  */
 public class PoolingPopulationManager extends AbstractPopulationManager {
 
     public static final int maxPoolSize = 26;
 
     private List<Organism> organismPool;
 
     private int bestInPool;
 
     private int foodCollected;
 
     private int totalFit;
 
 
     public PoolingPopulationManager(World2d world, int popSize, int progSize) {
         super(world, popSize, progSize);
         organismPool = new ArrayList<Organism>(maxPoolSize);
     }
 
 
     @Override
     public void printStats(long rps) {
         System.out.println("RPS: " + rps);
 
         int avgProgSize = 0;
         for (Organism o : organismPool) {
             avgProgSize += o.program.length;
         }
         avgProgSize /= (organismPool.size() > 0) ? organismPool.size() : 1;
         
         System.out.println("Avg pool prog size (current generation): " + avgProgSize);
         System.out.println("Round food: " + foodCollected);
         System.out.println("Pool food: " + totalFit);
         System.out.println("Best in pool: " + bestInPool);
         System.out.println("Pool avg food: " + (totalFit / ((organismPool.size() > 0) ? organismPool.size() : 1)));
         System.out.println("Round avg food: " + (foodCollected / ants.size()));
     }
 
 
     private void printPool() {
         System.out.println("Pool:");
         for (int i = 0; i < organismPool.size(); i++) {
             System.out.println("" + i + ":\t" + organismPool.get(i).food);
         }
     }
 
 
     //this funtion ensures that the populatio pool
     //does not exceed it's maximum size by purging
     //the least successful organisms
 
     private void updatePool() {
         if (organismPool.size() > maxPoolSize) {
             //sort the list so that the fittest organisms are on top
             Collections.sort(organismPool);
             Collections.reverse(organismPool);
 
             //drop all superfluous organisms
             for (int i = organismPool.size() - 1; i > (maxPoolSize - 1); i--) {
                 organismPool.remove(i);
             }
 
             //reshuffle to make roulettewheel work better
             Collections.shuffle(organismPool);
             Collections.shuffle(organismPool);
         }
     }
 
 
     @Override
     public int newGeneration() {
         double mutador;
        List<OpCode> parent1 = new ArrayList<OpCode>(), parent2 = new ArrayList<OpCode>();
         List<Organism> newAnts = new ArrayList<Organism>(ants.size());
 
         //choose crossover operator
         CrossoverOperator crossOp = new TwoPointCrossover();
 
         //enqueue all current organisms into our pool
         organismPool.addAll(ants);
 
         //get the fitness
         //call order is important, because of:
         //TODO: global variable bestInPool...
         foodCollected = calculateFitness(ants);
         totalFit = calculateFitness(organismPool);
 
         //create new genomes via cloning and mutation or crossover
         for (int i = 0; i < (ants.size() / 2); i++) {
             //select two source genomes and clone them
             //note: you must copy/clone the genomes before modifying them,
             //as the genome is passed by reference
             parent1.addAll(Arrays.asList(EvoUtils.rouletteWheel(organismPool, totalFit, rnd).clone()));
             parent2.addAll(Arrays.asList(EvoUtils.rouletteWheel(organismPool, totalFit, rnd).clone()));
 
             //mutate or crossover with a user defined chance
             mutador = rnd.nextDouble();
             if (mutador > crossoverRate) {
                 //mutate genomes
                 MutationUtils.mutate(parent1, rnd.nextInt(maxMutations) + 1, progSize, rnd);
                 MutationUtils.mutate(parent2, rnd.nextInt(maxMutations) + 1, progSize, rnd);
             }
             else {
                 //perform crossover
                 crossOp.cross(parent1, parent2, rnd);
             }
 
             //create new ants with the modified genomes and save them
             newAnts.add(new Organism(parent1.toArray(new OpCode[parent1.size()]), world.worldWidth, world.worldHeight, world.foodFinder));
             newAnts.add(new Organism(parent2.toArray(new OpCode[parent2.size()]), world.worldWidth, world.worldHeight, world.foodFinder));
         }
 
         //replace and leave the other to GC
         ants = newAnts;
 
         //update the pool
         updatePool();
 
         return foodCollected;
     }
 
 
     //the team effort
     private int calculateFitness(List<Organism> organisms) {
         int totalFit = 0;
         bestInPool = 0;
 
         for (Organism o : organisms) {
             totalFit += o.food;
 
             //remember the best
             if (o.food > bestInPool) {
                 bestInPool = o.food;
             }
         }
 
         return totalFit;
     }
 
 }
