 package de.mh4j.examples.maxknapsack.solver;
 
 import java.util.List;
 
 import de.mh4j.examples.maxknapsack.model.Item;
 import de.mh4j.examples.maxknapsack.model.Knapsack;
 import de.mh4j.solver.simulatedAnnealing.AbstractSimulatedAnnealingSolver;
 import de.mh4j.solver.termination.StagnationTermination;
 import de.mh4j.util.Util;
 
 public class SimulatedAnnealingKnapsackSolver extends AbstractSimulatedAnnealingSolver<Knapsack> {
 
     private final int knapsackCapacity;
     private final List<Item> availableItems;
 
     protected enum NeighborFunction {
         ADD, SWAP;
 
         static NeighborFunction getFromId(int i) {
             switch (i) {
                 default:
                 case 0:
                     return ADD;
                 case 1:
                     return SWAP;
             }
         }
     }
 
     public SimulatedAnnealingKnapsackSolver(int knapsackCapacity, List<Item> availableItems) {
         super(new KnapsackCoolingScheme());
         this.knapsackCapacity = knapsackCapacity;
         this.availableItems = availableItems;
 
         addTerminationCondition(new StagnationTermination(this, 100));
     }
 
     @Override
     protected Knapsack createInitialSolution() {
         Knapsack knapsack = new Knapsack(knapsackCapacity);
 
         boolean itemHasBeenAdded;
         do {
             Item randomItem = Util.getRandomEntryFrom(availableItems);
             itemHasBeenAdded = knapsack.addItem(randomItem);
         } while (itemHasBeenAdded);
 
         return knapsack;
     }
 
     @Override
     protected Knapsack createRandomNeighbor() {
         Knapsack neighbor = new Knapsack(currentSolution);
         switch (NeighborFunction.getFromId(randomizer.nextInt(2))) {
             default:
             case ADD:
                 return createNeighborFromAdd(neighbor);
             case SWAP:
                 return createNeighborFromSwap(neighbor);
 
         }
     }
 
     private Knapsack createNeighborFromAdd(Knapsack neighbor) {
         Item randomItem = Util.getRandomEntryFrom(availableItems);
         neighbor.addItem(randomItem);
         return neighbor;
     }
 
     private Knapsack createNeighborFromSwap(Knapsack neighbor) {
        int randomIndex = randomizer.nextInt(neighbor.getNumberOfItems());
         neighbor.removeItem(randomIndex);
         return createNeighborFromAdd(neighbor);
     }
 
 }
