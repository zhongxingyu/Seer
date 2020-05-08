 package ecprac.era270;
 
 import java.util.Random;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Collections;
 import java.io.IOException;
 
 import ecprac.torcs.genome.Utilities;
 
 abstract class GenericEA<G extends GenericGenome> {
 
     // population
     protected List<G> population = null;
     // random number generator
     protected Random r = new Random(); 
     // how far we are in the evolutionary process
     protected int curEvaluation;
 
     // constructor parameters
     protected int populationSize;
     protected int matingPoolSize;
     protected int nRuns;
     protected String algorithmName;
 
     public GenericEA(int populationSize, int matingPoolSize, int nRuns, 
                         String algorithmName) 
     {
         this.populationSize = populationSize;
         this.matingPoolSize = matingPoolSize;
         this.nRuns = nRuns;
         this.algorithmName = algorithmName;
     }
 
     // methods to be implemented by specific EAs 
     abstract G createIndividual();
     abstract List<G> evaluateFitness(List<G> individuals);
     abstract List<G> selectParents();
     abstract List<G> recombine(List<G> parents);
     abstract G mutate(G individual);
     abstract void selectSurvivors();
 
     void initPopulation() {
         population = new LinkedList<G>();
 
         for (int i=0; i<populationSize; i++)
             population.add(createIndividual());
     }
 
     private List<G> getBestOrWorst(int n, boolean best) {
         List<G> individuals = new LinkedList<G>();
 
         // sort individuals by their fitness value
         Collections.sort(population);
 
         for (int i=0; i<n; i++) 
             individuals.add(population.get(best ? populationSize - i - 1 : i));
         
         return individuals;
     }
 
     List<G> getBest(int n) {
         return getBestOrWorst(n, true);
     }
 
     List<G> getWorst(int n) {
         return getBestOrWorst(n, false);
     }
 
     void evolve() {
         // parents selection
         List<G> parents = selectParents();
 
         // recombination
         List<G> offsprings = recombine(parents);
 
         // mutation
         for (G offspring: offsprings)
             population.add(mutate(offspring));
 
         // survivor selection
         selectSurvivors();
 
         G best = getBest(1).get(0);
         System.out.println(curEvaluation + " " + best.fitness);
     }
 
     private void saveBest(String description) {
         try {
             G best = getBest(1).get(0);
             Utilities.saveGenome(best, 
                 "best-" + algorithmName + "-" + description + ".genome");
         }
         catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     void run() {
         initPopulation();
 
         for (curEvaluation=0; curEvaluation < nRuns; curEvaluation++) {
             evolve();
 
            double progress = (double)curEvaluation / nRuns;
 
            if (progress == 0.1 || progress == 0.2 ||
                progress == 0.5 || progress == 0.7)
 
                saveBest("" + (int)(progress * 10));
         }
 
         saveBest("100");
     }
 }
