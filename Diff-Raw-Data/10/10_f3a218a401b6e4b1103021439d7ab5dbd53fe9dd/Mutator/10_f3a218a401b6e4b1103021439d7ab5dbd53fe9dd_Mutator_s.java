 package to.richard.tsp;
 
 /**
  * Author: Richard To
  * Date: 2/7/13
  */
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
 * Abstract class that handles some boiler plate initialization for mutation.
  *
  * Mutation rate should be between 0-1. But this is not checked.
  * A mutation less than 0 means that same as a 0% probability. And a mutation
  * rate greater than 1 means the same as 100% probability.
  *
  * A genotype will be mutated if random probability is less than mutation rate value.
  *
  * Here mutation rate applies to entire genotype, since this is for permutation. So this
  * is different than checking for mutation at the gene level that could be used in
  * bit representations.
  *
  * A mutation algorithm needs to be passed in. Ie. Swap, Scramble, or Inversion for this case.
  */
 public class Mutator {
 
     private IRandom _random;
     private double _mutationRate;
     private IMutationStrategy _mutationStrategy;
 
     public Mutator(double mutationRate, IMutationStrategy mutationStrategy, IRandom random) {
         _random = random;
         _mutationRate = mutationRate;
         _mutationStrategy = mutationStrategy;
     }
 
     public List<Genotype> mutate(List<Genotype> genotypes) {
        double probabilty = 0.0;
         ArrayList<Genotype> newGenotypes = new ArrayList<Genotype>();
         for (Genotype genotype : genotypes) {
            probabilty = _random.nextDouble();
            if (probabilty < _mutationRate) {
                 newGenotypes.add(_mutationStrategy.mutate(genotype, _random));
             } else {
                 newGenotypes.add(genotype);
             }
         }
         return newGenotypes;
     }
 }
