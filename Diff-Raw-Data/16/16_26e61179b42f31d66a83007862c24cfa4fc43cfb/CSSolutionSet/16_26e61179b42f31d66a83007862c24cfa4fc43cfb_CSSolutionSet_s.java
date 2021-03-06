 package solutions;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Random;
 
 import problems.OptimizationProblem;
 
 public class CSSolutionSet implements SolutionSet {
     private ArrayList<CSSolution> solutions;
     private Random rand;
     private int numNests;
     private int numVars;
     
     public CSSolutionSet(int numNests, int numVars) {
         this.numNests = numNests;
         this.numVars  = numVars;
         this.rand = new Random();
         this.solutions = new ArrayList<CSSolution>(numNests);
         
//        System.out.printf("LOL");
        
         for (int i = 0; i < numNests; i++) {
//            System.out.printf("i: %d", i);
            this.solutions.set(i, new CSSolution(numVars));
         }
     }
     
     public int getNumSols() {
         return numNests;
     }
     
     public void replace(int j, CSSolution sol) {
         this.solutions.set(j, sol);
     }
     
     public CSSolution getSol(int i) {
     	return this.solutions.get(i);
     }
     
     public CSSolution getRandSol() {
         return this.solutions.get(rand.nextInt(getNumSols()));
     }
     
     public CSSolution getMostFitSolution(OptimizationProblem optProb) {
         this.sortByFitness(optProb);
         return this.solutions.get(0);
     }
     
     public class SolutionByFitnessComparator implements Comparator<Solution> {
         public int compare(Solution s1, Solution s2) {
             double s1Fitness = s1.getFitness();
             double s2Fitness = s2.getFitness();
             if (s1Fitness < s2Fitness) return -1;
             else if (s1Fitness > s2Fitness) return 1;
             else return 0;
         }
     }
     
     /** 
      * Sorts all solutions with most fit solution being solutions.get(0).
      */
     public void sortByFitness(OptimizationProblem optProb) {
         // Evaluates the fitness of each solution in solutions.
         for (int i = 0; i < this.numNests; i++)
             solutions.get(0).evalFitness(optProb);
         // Sorts solutions by their respective fitness numbers.
         Collections.sort(this.solutions, new SolutionByFitnessComparator());
     }
 
     public void abandonWorstSols(OptimizationProblem optProb, double abandonmentRatio) {
         this.sortByFitness(optProb);
         int numToAbandon = (int) (abandonmentRatio * this.numNests);
         int numToKeep = this.numNests - numToAbandon;
         for (int i = numToKeep; i < numNests; i++) {
             solutions.get(i).setAsRandSol(optProb);
         }
     }
 
 }
