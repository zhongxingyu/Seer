 package to.richard.tsp;
 
 import java.util.*;
 
 /**
  * Author: Richard To
  * Date: 2/7/13
  */
 
 /**
  * Partially Mapped Crossover implementation.
  *
  * Note: O1 = Offspring 1, P1 = Parent 1, P2_i = Parent2 allele at position i
  *
  * 1. Select a start and end range. Exclusive.
  * 2. For P1, swap alleles within range into offspring.
  * 3. For P2, get a list of alleles within range that are not in offspring.
  * 4. Add these to offspring following these steps:
  *      - To find a new gene position for P2_i, check the value of O1_i.
  *      - Find the location of 01_i in P2. Call this position j.
  *      - If position j is open in O1, put P2_i at O1_j.
  *      - If position j is occupied, find the location of 01_j in P2. Call this position k.
  *      - If position k is open in 01, put P2_i at 01_k.
  * 5. For the remaining alleles in P2, just put them in the corresponding positions at O1.
  * 6. Creating O2 is the same as creating O1. The difference is we start with P1.
  */
 public class PartiallyMappedCrossover implements ICrossoverOperator {
 
     public Recombinator.OFFSPRING numberOfOffspring() {
         return Recombinator.OFFSPRING.PAIR;
     }
 
     public List<Genotype> crossover(
             Genotype genotype1, Genotype genotype2, IRandom random) {
 
         if (genotype1.equals(genotype2)) {
             return Arrays.asList(new Genotype[]{genotype1, genotype2});
         }
 
         int genotypeLength = genotype1.length();
 
         ArrayList<Genotype> newOffspring = new ArrayList<Genotype>();
 
         /**
          * Select two crossover boundaries. Exclusive.
          */
         int start = random.nextInt(genotypeLength);
         int end = random.nextInt(genotypeLength);
         int temp;
         if (start > end) {
             temp = start;
             start = end;
             end = temp;
         }
 
         newOffspring.add(crossover(genotype1, genotype2, start, end));
         newOffspring.add(crossover(genotype2, genotype1, start, end));
         return newOffspring;
     }
 
     private Genotype crossover(Genotype parent1, Genotype parent2, int start, int end) {
 
         int genotypeLength = parent1.length();
 
         MutableGenotype offspring = new MutableGenotype(genotypeLength);
         HashSet<Allele> inheritedAlleles = new HashSet<Allele>();
         ArrayList<Allele> remainingAlleles = new ArrayList<Allele>();
 
         int offspringGenePosition = 0;
         int genePositionCount = 0;
         Integer tempGenePosition = null;
         Allele currentAllele = null;
        Allele tempAllele = null;
 
         /**
          * Add alleles between selected crossover boundaries to offspring.
          */
         for (int i = start + 1; i < end; i++) {
             inheritedAlleles.add(parent1.getAllele(i));
             offspring.setAllele(parent1.getAllele(i), i);
         }
 
         for (int i = 0; i < genotypeLength; i++) {
             currentAllele = parent2.getAllele(i);
             if (i >= start + 1 && i < end && !inheritedAlleles.contains(currentAllele)) {
                 /**
                  * For parent2, find alleles in between crossover boundaries that are unique.
                  * If unique, find an open gene position for the alleles.
                  */
                 genePositionCount = 0;
                 tempGenePosition = i;
                 /**
                  * Look at the allele in the same position as parent2 in offspring1.
                  * Take that allele value and find its position in parent2.
                  * Now check if this position is open in offspring1.
                  * If it is, set the allele value at this position.
                  * If not, the current allele at this position in offspring1 and then
                  * look for it in parent2.
                  *
                  * To avoid an infinite loop, if we checked all gene positions, throw
                  * a crossover failed exception. This should not happen if the algorithm
                  * is correct and the genotypes are valid.
                  */
                 while (genePositionCount < genotypeLength) {
                    tempAllele = offspring.getAllele(tempGenePosition);
                    tempGenePosition = parent2.findAllele(tempAllele);
                    if (!offspring.hasAlleleAt(tempGenePosition)) {
                         break;
                     } else {
                         genePositionCount++;
                     }
                 }
 
                if (genePositionCount >= genotypeLength) {
                     throw new Errors.CrossoverFailed("Could not find open gene position to place allele.");
                 }
 
                 inheritedAlleles.add(currentAllele);
                 offspring.setAllele(currentAllele, tempGenePosition);
             } else {
                 /**
                  * For parent2, if alleles are outside crossover boundaries,
                  * add them to list of remaining alleles to add later.
                  */
                 remainingAlleles.add(currentAllele);
             }
         }
 
         /**
          * Add remaining list of alleles to open spots.
          * List of alleles should be in order (based on gene position in parent2).
          */
         for (Allele allele : remainingAlleles) {
             if (!inheritedAlleles.contains(allele)) {
                 while (offspringGenePosition < genotypeLength) {
                     if (!offspring.hasAlleleAt(offspringGenePosition++)) {
                         offspring.setAllele(allele, offspringGenePosition - 1);
                         break;
                     }
                 }
             }
         }
 
         return offspring;
     }
 }
