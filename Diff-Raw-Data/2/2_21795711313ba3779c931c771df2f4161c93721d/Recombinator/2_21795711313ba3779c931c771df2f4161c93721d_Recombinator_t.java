 package to.richard.tsp;
 
 /**
  * Author: Richard To
  * Date: 2/7/13
  */
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Performs recombination step using specified recombination strategy.
  *
  * Recombination/Crossover rate works the same as mutation rate from
  * Mutator class.
  *
  */
 public class Recombinator {
 
     public enum OFFSPRING {
         SINGLE(1), PAIR(2);
 
         private int _value;
 
         private OFFSPRING(int value) {
             _value = value;
         }
 
         public int value() {
             return _value;
         }
     }
 
     private IRandom _random;
     private double _recombinationRate;
     private ICrossoverOperator _crossoverOperator;
 
     public Recombinator(double recombinationRate, ICrossoverOperator crossoverOperator, IRandom random) {
         _random = random;
         _recombinationRate = recombinationRate;
         _crossoverOperator = crossoverOperator;
     }
 
     /**
      * In the case of an odd number of parents (is that allowed?),
      * those will just be passed along asexually.
      *
      * This is kind of a problem in the case of 100% recombination since
      * 1 will be left out.
      *
      * Another problem is the number of offspring. Some can create 1 offspring. Others 2.
      * And others can do more using multi-parent crossover? Not sure how to deal with that yet.
      *
      * For now limit offspring to 1 or 2. Additionally for the case of 1 offspring the parent
      * matching will work as follows. Parent1 and Parent2, Parent2 and Parent3 will be chosen
      * for recombination if under recombination rate. If not, only the first parent will move on
      * asexually. This is different from the case of two parents where both parents will move on
      * asexually.
      *
      * Part of the reason for the limitation of 1 or 2 offspring and exactly 2 parents is because of
      * the use of the template pattern here.
      *
      * @param genotypes
      * @return
      */
     public List<Genotype> recombine(List<Genotype> genotypes) {
         double probability = 0.0;
         int index1 = 0;
         int index2 = 0;
         int offSpringPerCrossover = _crossoverOperator.numberOfOffspring().value();
         int pairs = genotypes.size() / offSpringPerCrossover;
         int remainders = genotypes.size() % offSpringPerCrossover;
         ArrayList<Genotype> newGenotypes = new ArrayList<Genotype>();
 
         for (int i = 0; i < pairs; i++) {
             index1 = i * offSpringPerCrossover;
             index2 = i * offSpringPerCrossover + 1;
             probability = _random.nextDouble();
             if (probability < _recombinationRate) {
                 newGenotypes.addAll(_crossoverOperator.crossover(genotypes.get(index1), genotypes.get(index2), _random));
             } else {
                for (int g = 0; g < offSpringPerCrossover; g++) {
                     newGenotypes.add(genotypes.get(index1 + g));
                 }
             }
         }
 
         for (int i = genotypes.size() - remainders; i < genotypes.size(); i++) {
             newGenotypes.add(genotypes.get(i));
         }
 
         return newGenotypes;
     }
 }
