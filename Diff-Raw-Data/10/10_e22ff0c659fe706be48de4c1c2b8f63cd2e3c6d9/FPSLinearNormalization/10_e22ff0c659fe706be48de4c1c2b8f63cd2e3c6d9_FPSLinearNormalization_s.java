 package to.richard.tsp;
 
 import java.util.*;
 
 /**
  * Author: Richard To
  * Date: 2/10/13
  */
 
 /**
  * Implements Linear Normalization (rank selection)
  *
  * Use FitnessMaximizationComparator or FitnessMinimizationComparator to appropriately.
  *
  * 1. Sort genotypes by fitness
  * 2. Assigns fitness by rank. So highest rank would be size of population.
  * 3. In the case of ties, get the mean. In other words ranks 2 and 3 would equal 2.5.
  */
 public class FPSLinearNormalization implements IFPSTransform {
 
     private Comparator<Pair<Double, Genotype>> _comparator;
 
     public FPSLinearNormalization(Comparator<Pair<Double, Genotype>> comparator) {
         _comparator = comparator;
     }
 
     @Override
     public List<Pair<Double, Genotype>> transform(List<Pair<Double, Genotype>> genotypeFitnessList) {
         int population = genotypeFitnessList.size();
         double totalRank = 0;
         double avgRank = 0;
         double previousFitness = 0;
         Pair<Double, Genotype> currentPair;
         Pair<Double, Genotype> tempPair;
        ArrayList<Integer> sameFitnessIndex = new ArrayList<Integer>();
         ArrayList<Pair<Double, Genotype>> listToSort = new ArrayList<Pair<Double, Genotype>>(genotypeFitnessList);
         ArrayList<Pair<Double, Genotype>> newList = new ArrayList<Pair<Double, Genotype>>();
 
         Collections.sort(listToSort, _comparator);
 
         for (int i = population - 1; i >= 0; i--) {
             currentPair = listToSort.get(i);
             if (currentPair.getFirstValue() == previousFitness) {
                 sameFitnessIndex.add(i);
             } else {
                 if (sameFitnessIndex.size() > 1) {
                     avgRank = totalRank = 0;
                     for (Integer index : sameFitnessIndex) {
                         totalRank += index + 1;
                     }
                     avgRank = totalRank / sameFitnessIndex.size();
                     for (Integer index : sameFitnessIndex) {
                        newList.get(index).setFirstValue(avgRank);
                     }
                 }
                 previousFitness = currentPair.getFirstValue();
                 sameFitnessIndex.clear();
                 sameFitnessIndex.add(i);
             }
             newList.add(new Pair<Double, Genotype>((double)(i + 1), currentPair.getSecondValue()));
         }
         return newList;
     }
 }
