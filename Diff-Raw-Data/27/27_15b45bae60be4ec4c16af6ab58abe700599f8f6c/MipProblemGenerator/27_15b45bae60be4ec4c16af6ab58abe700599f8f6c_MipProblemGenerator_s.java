 package coalitiongames;
 
 import java.util.ArrayList;
 import java.util.List;
 
 abstract class MipProblemGenerator {
     
     public static void main(final String[] args) {
         runSmallProblem();
     }
     
     @SuppressWarnings("unused")
     private static void repeatLargeProblem() {
         final int iterations = 100;
         for (int i = 0; i < iterations; i++) {
             System.out.println("\n\n");
             runLargeProblem();
         }
     }
     
     private static void runLargeProblem() {
         final int otherAgents = 49;
         final int valueRange = 5;
         final int priceRange = 2;
         final int kMax = 5;
         final int kMin = 5;
         runProblem(
             otherAgents, 
             valueRange, 
             priceRange, 
             kMax, 
             kMin
         );
     }
     
     private static void runSmallProblem() {
         final int otherAgents = 20;
         final int valueRange = 10;
         final int priceRange = 4;
         final int kMax = 5;
         final int kMin = 0;
         runProblem(
             otherAgents, 
             valueRange, 
             priceRange, 
             kMax, 
             kMin
         );
     }
     
     private static void runProblem(
         final int n,
         final double valueRange,
         final double priceRange,
         final int kMax,
         final int kMin
     ) {
         final double baseValue = 50.0;
         final double basePrice = MipGenerator.MIN_BUDGET / kMax;
         final List<Double> values = new ArrayList<Double>();
         final List<Double> prices = new ArrayList<Double>();
         final double budget = 
             MipGenerator.MIN_BUDGET 
             + Math.random() * MipGenerator.MIN_BUDGET / n;
         final double maxPrice = 
             MipGenerator.MIN_BUDGET + MipGenerator.MIN_BUDGET / n;
         
         for (int i = 1; i <= n; i++) {
             double newValue = 
                 baseValue + Math.random() * valueRange - valueRange / 2.0;
             if (newValue < 0) {
                 newValue = 0;
             }
             values.add(newValue);
             
             double newPrice = 
                 basePrice + Math.random() * priceRange - priceRange / 2.0;
             if (newPrice < 0) {
                 newPrice = 0;
             }
             if (newPrice > maxPrice) {
                 newPrice = maxPrice;
             }
             prices.add(newPrice);
         }
         
         System.out.println("Prices:");
         System.out.println(prices);
         
         System.out.println("\nValues:");
         System.out.println(values);
         
         System.out.println("\nBudget:");
         System.out.println(budget);
         System.out.println();
         
         final MipResult result = MipGenerator.getLpSolution(
             values, 
             prices, 
             budget, 
             kMax, 
             kMin, 
             maxPrice
         );
         System.out.println(result);
     }
 }
