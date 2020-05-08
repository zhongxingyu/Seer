 package de.mh4j.examples.maxknapsack.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.mh4j.solver.Solution;
 
 public class Knapsack implements Solution<Knapsack> {
 
     private final int totalCapacity;
     private final List<Item> items;
 
     private int remainingCapacity;
     private int costs;
 
     public Knapsack(int capacity) {
         totalCapacity = capacity;
         remainingCapacity = totalCapacity;
         items = new ArrayList<>();
         costs = 0;
     }
 
     public Knapsack(Knapsack original) {
         this.totalCapacity = original.totalCapacity;
         this.items = new ArrayList<>(original.items);
         this.remainingCapacity = original.remainingCapacity;
         this.costs = original.costs;
     }
 
     @Override
     public int getCosts() {
         return costs;
     }
 
     public int getCapacity() {
         return totalCapacity;
     }
 
     @Override
     public boolean isBetterThan(Knapsack otherSolution) {
        return otherSolution.getCosts() < costs;
     }
 
     public boolean addItem(Item item) {
         int newRemainingCapacity = remainingCapacity - item.volume;
 
         if (newRemainingCapacity >= 0) {
             items.add(item);
             remainingCapacity = newRemainingCapacity;
             costs += item.price;
         }
 
         return newRemainingCapacity >= 0;
     }
 
     public int getNumberOfItems() {
         return items.size();
     }
 
     public int getRemainingCapacity() {
         return remainingCapacity;
     }
 
     public Item removeItem(int index) {
         Item removedItem = items.remove(index);
         costs -= removedItem.price;
         remainingCapacity += removedItem.volume;
         return removedItem;
     }
 
     public boolean isFull() {
         return remainingCapacity == 0;
     }
 
     @Override
     public boolean equals(Object otherObject) {
         if (otherObject instanceof Knapsack == false) {
             return false;
         }
 
         Knapsack otherKnapsack = (Knapsack) otherObject;
         return this.items.equals(otherKnapsack.items) && this.totalCapacity == otherKnapsack.totalCapacity;
     }
 
     @Override
     public String toString() {
         return (totalCapacity - remainingCapacity) + "/" + totalCapacity + ": " + items;
     }
 }
