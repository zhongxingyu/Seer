 package knapsackj;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * The actual Knapsack. Where all the fun happens.
  *
  * @author Deepfreeze32
  */
 public class Knapsack {
 
     public ArrayList<Item> items;
     private int capacity;
 
     /**
      * Default constructor. Mostly useless.
      */
     public Knapsack() {
         capacity = 1;
         items = new ArrayList<Item>();
     }
 
     /**
      * Constructor. Takes an ItemCollection.
      *
      * @param ic An ItemCollection to take as input.
      */
     public Knapsack(ItemCollection ic) {
         items = ic.items;
         capacity = ic.capacity;
         Collections.sort(items);
     }
 
     /**
      * More direct constructor. Takes a generic list and capacity.
      *
      * @param size The capacity to be used in this knapsack.
      * @param itemList The list of items to use.
      */
     public Knapsack(int size, List<Item> itemList) {
         capacity = size;
         items = new ArrayList<Item>(itemList);
         Collections.sort(items);
     }
 
     /**
      * Copy constructor. Can resize capacity if desired.
      *
      * @param newSize The capacity to resize to.
      * @param knapsack The old knapsack.
      */
     public Knapsack(int newSize, Knapsack knapsack) {
         capacity = newSize;
         items = knapsack.items;
     }
 
     /**
      * Get size of the knapsack.
      *
      * @return The size.
      */
     public int size() {
         return items.size();
     }
 
     /**
      * Stringify the Knapsack.
      *
      * @return The Knapsack in String form.
      */
     @Override
     public String toString() {
         return capacity + ": " + items;
     }
 
     /**
      * Calculates the total value given a configuration of items.
      *
      * @param amounts The amount taken of each item.
      * @return The total value of this solution.
      */
     public double getTotalValue(ArrayList<Double> amounts) {
         double total = 0;
         if (amounts.size() < items.size()) {
             for (int i = amounts.size(); i < items.size(); i++) {
                 amounts.add(0.0);
             }
         }
         for (int i = 0; i < amounts.size(); i++) {
             total += amounts.get(i) * items.get(i).getValue();
         }
         return total;
     }
     
     /**
      * Calculates the total value given a configuration of items.
      *
      * @param amounts The amount taken of each item.
      * @return The total value of this solution.
      */
     public double getTotalWeight(ArrayList<Double> amounts) {
         double total = 0;
         if (amounts.size() < items.size()) {
             for (int i = amounts.size(); i < items.size(); i++) {
                 amounts.add(0.0);
             }
         }
         for (int i = 0; i < amounts.size(); i++) {
             total += amounts.get(i) * items.get(i).getWeight();
         }
         return total;
     }
 
     /**
      * Function to solve Knapsack using fractional ratios.
      *
      * @return An Inventory object containing the best solution.
      */
     public Inventory solveFractional() {
         int availableCapacity = capacity;
         ArrayList<Double> amounts = new ArrayList<Double>();
         if (availableCapacity <= 0) {
             return new Inventory(this, amounts);
         }
         for (Item i : items) {
             if (availableCapacity < i.getWeight()) {
                 amounts.add(((double) availableCapacity) / i.getWeight());
                 return new Inventory(this, amounts);
             }
             availableCapacity -= i.getWeight();
             amounts.add(1.0);
         }
         return new Inventory(this, amounts);
     }
 
     /**
      * Function to solve Knapsack using a greedy algorithm.
      *
      * @return An Inventory object containing the best solution.
      */
     public Inventory solveGreedily() {
         int availableCapacity = capacity;
         ArrayList<Double> amounts = new ArrayList<Double>();
         if (availableCapacity <= 0) {
             return new Inventory(this, amounts);
         }
 
         for (Item i : items) {
             if (availableCapacity < i.getWeight()) {
                 amounts.add(0.0);
             } else {
                 availableCapacity -= i.getWeight();
                 amounts.add(1.0);
             }
         }
         return new Inventory(this, amounts);
     }
 
     /**
      * Function to solve Knapsack using dynamic programming.
      *
      * @return An Inventory object containing the best solution.
      */
     public Inventory solveDynamically() {
         int[] solution = new int[capacity + 1];
         ArrayList<Double> amounts = new ArrayList<Double>();
         boolean[][] isIncluded = new boolean[items.size()][capacity + 1];
 
         if (capacity < 0) {
             return new Inventory(this, amounts);
         }
   
         for (int i = 0; i < items.size(); i++) {
             amounts.add(0.0);
             Item item = items.get(i);
             int value = item.getValue();
             int weight = item.getWeight();
             for (int c = capacity; c >= weight; c--) {
                 int newValue = value + solution[c - weight];
                 if (newValue > solution[c]) {
                     isIncluded[i][c] = true;
                     solution[c] = newValue;
                 }
             }
         }
  
         int c = capacity;
         int p = 0;
         for (int i = items.size() - 1; i >= 0; i--) {
             boolean b = isIncluded[i][c];
             if (b) {
                 c -= items.get(i).getWeight();
                 p += items.get(i).getValue();
                 amounts.set(i, 1.0);
             }
         }
         return new Inventory(this, amounts);
     }
 
     /**
      * The starting point for exhaustive search.
      *
      * @return The Inventory object containing the best solution.
      */
     public Inventory solveExhaustively() {
         int availableCapacity = capacity;
         return solveExhaustivelyRecursively(0, availableCapacity, new ArrayList<Double>(), 0);
     }
 
     /**
      * The recursive function used to solve Knapsack recursively.
      *
      * @param indexOfNextItem The next index to use.
      * @param remainingCapacity The remaining capacity of the knapsack.
      * @param result The resulting amounts of items. This is passed around.
      * @param value The running total.
      * @return An Inventory object containing the current configuration.
      */
     private Inventory solveExhaustivelyRecursively(int indexOfNextItem, int remainingCapacity, ArrayList<Double> result, int value) {
         if (indexOfNextItem >= items.size()) {
             return new Inventory(this, result, value);
         }
 
         Item nextItem = items.get(indexOfNextItem);
 
         result.add(0.0);
         Inventory resultWhenExcluded = solveExhaustivelyRecursively(indexOfNextItem + 1, remainingCapacity, result, value);
 
        result.set(result.size() - 1, 1.0);
        Inventory resultWhenIncluded = solveExhaustivelyRecursively(indexOfNextItem + 1, remainingCapacity, result, value + nextItem.getValue());
        result.remove(result.size() - 1);
        if (resultWhenIncluded.getTotalValue() >= resultWhenExcluded.getTotalValue() && resultWhenIncluded.getTotalWeight() <= remainingCapacity) {
            return resultWhenIncluded;
        } else {
             return resultWhenExcluded;
        }
     }
     
     /**
      * The starting point for smarter search.
      *
      * @return The Inventory object containing the best solution.
      */
     public Inventory solveSmartly() {
         int availableCapacity = capacity;
         return solveSmartlyRecursively(0, availableCapacity, new ArrayList<Double>(), 0);
     }
 
     /**
      * The recursive function used to solve Knapsack recursively in a smart manner.
      *
      * @param indexOfNextItem The next index to use.
      * @param remainingCapacity The remaining capacity of the knapsack.
      * @param result The resulting amounts of items. This is passed around.
      * @param value The running total.
      * @return An Inventory object containing the current configuration.
      */
     private Inventory solveSmartlyRecursively(int indexOfNextItem, int remainingCapacity, ArrayList<Double> result, int value) {
         if (indexOfNextItem >= items.size()) {
             return new Inventory(this, result, value);
         }
         
         Item nextItem = items.get(indexOfNextItem);
 
         result.add(0.0);
         Inventory resultWhenExcluded = solveSmartlyRecursively(indexOfNextItem + 1, remainingCapacity, result, value);
 
         if (nextItem.getWeight() > remainingCapacity) {
             result.remove(result.size() - 1);
             return resultWhenExcluded;
         } else {
             result.set(result.size() - 1, 1.0);
             Inventory resultWhenIncluded = solveSmartlyRecursively(indexOfNextItem + 1, remainingCapacity - nextItem.getWeight(), result, value + nextItem.getValue());
             result.remove(result.size() - 1);
             if (resultWhenIncluded.getTotalValue() >= resultWhenExcluded.getTotalValue()) {
                 return resultWhenIncluded;
             } else {
                 return resultWhenExcluded;
             }
         }
     }
 }
