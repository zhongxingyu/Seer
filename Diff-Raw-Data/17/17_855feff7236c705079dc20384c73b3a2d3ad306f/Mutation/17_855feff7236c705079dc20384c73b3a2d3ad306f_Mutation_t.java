 package com.akavrt.csp.genetic;
 
 import com.akavrt.csp.core.Order;
 import com.akavrt.csp.core.Roll;
 import com.akavrt.csp.solver.genetic.Chromosome;
 import com.akavrt.csp.solver.genetic.Gene;
 import com.akavrt.csp.solver.genetic.GeneticExecutionContext;
 import com.akavrt.csp.solver.genetic.GeneticOperator;
 import com.akavrt.csp.solver.pattern.PatternGenerator;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 /**
  * User: akavrt
  * Date: 29.03.13
  * Time: 02:10
  */
 public class Mutation implements GeneticOperator {
     private static final Logger LOGGER = LogManager.getLogger(Mutation.class);
     private final PatternGenerator generator;
     private final Random rGen;
 
     public Mutation(PatternGenerator generator) {
         this.generator = generator;
         rGen = new Random();
     }
 
     @Override
     public void initialize(GeneticExecutionContext context) {
         LOGGER.debug("MT: initializing pattern generator, allowed number of cuts is {}",
                      context.getProblem().getAllowedCutsNumber());
         generator.initialize(context.getProblem());
     }
 
     @Override
     public Chromosome apply(Chromosome... chromosomes) {
         if (chromosomes.length < 1 || chromosomes[0] == null) {
             return null;
         }
 
         Chromosome original = chromosomes[0];
         Chromosome mutated;
         double draw = rGen.nextDouble();

         if (draw < 0.33) {
             mutated = useExistingRoll(original);
         } else if (draw < 0.66) {
             mutated = useNewRoll(original);
         } else {
             mutated = heuristic(original);
         }
 
         return mutated;
     }
 
     private Chromosome useExistingRoll(Chromosome chromosome) {
         Chromosome mutated = new Chromosome(chromosome);
 
         int index = rGen.nextInt(mutated.size());
         LOGGER.debug("MT: remove gene {}/{}", index + 1, mutated.size());
 
         Roll roll = mutated.removeGene(index).getRoll();
 
         if (roll != null && getRatio(mutated) > getRatio(chromosome)) {
             // calculate residual demands for orders' length
             int[] demand = calcDemand(roll, mutated);
 
             // generate new pattern
             // TODO move allowed trim ratio to algorithm parameters
             int[] pattern = generator.generate(roll.getWidth(), demand, 0.1);
 
             // insert new gene into mutated chromosome
             Gene replacement = new Gene(pattern, roll);
             mutated.addGene(index, replacement);
 
             LOGGER.debug("MT: insert gene {}/{} (existing roll, new pattern)", index + 1,
                          mutated.size());
         }
 
         return mutated;
     }
 
     private Chromosome useNewRoll(Chromosome chromosome) {
         Chromosome mutated = new Chromosome(chromosome);
 
         int index = rGen.nextInt(mutated.size());
         LOGGER.debug("MT: remove gene {}/{}", index + 1, mutated.size());
 
         mutated.removeGene(index);
 
         if (getRatio(mutated) > getRatio(chromosome)) {
             // pick one of the unused rolls at random
             Roll roll = pickRoll(mutated);
 
             if (roll != null) {
                 // calculate residual demands for orders' length
                 int[] demand = calcDemand(roll, mutated);
 
                 // generate new pattern
                 // TODO move allowed trim ratio to algorithm parameters
                 int[] pattern = generator.generate(roll.getWidth(), demand, 0.1);
 
                 // insert new gene into mutated chromosome
                 Gene replacement = new Gene(pattern, roll);
                 mutated.addGene(index, replacement);
 
                 LOGGER.debug("MT: insert gene {}/{} (new roll, new pattern)", index + 1,
                              mutated.size());
             }
         }
 
         return mutated;
     }
 
     private Chromosome heuristic(Chromosome chromosome) {
         Chromosome mutated = new Chromosome(chromosome);
 
         if (chromosome.size() < 2) {
             return mutated;
         }
 
         int firstIndex = rGen.nextInt(mutated.size());
         Gene firstGene = mutated.getGene(firstIndex);
 
         if (firstGene.getRoll() == null) {
             return mutated;
         }
 
         int secondIndex = firstIndex;
         for (int i = 0; i < mutated.size(); i++) {
             Gene candidate = mutated.getGene(i);
             if (i != firstIndex && candidate.getRoll() != null
                     && candidate.getRoll().getWidth() == firstGene.getRoll().getWidth()
                     && candidate.getPatternHashCode() != firstGene.getPatternHashCode()) {
                 secondIndex = i;
                 break;
             }
         }
 
         if (firstIndex != secondIndex) {
             // gene with suitable roll was found
             // replace pattern in the second gene
             // with pattern from the first gene
             LOGGER.debug("MTH: copying pattern from gene {} to gene {}", firstIndex, secondIndex);
 
             int[] pattern = firstGene.getPattern().clone();
             Roll roll = mutated.getGene(secondIndex).getRoll();
 
             Gene replacement = new Gene(pattern, roll);
            mutated.setGene(secondIndex, replacement);
         } else {
             // gene with suitable roll wasn't found
             // pick one of the spare rolls with suitable width,
             // replace pattern and roll in randomly selected gene
             // with pattern from the first gene and previously
             // picked spare roll, respectively
             Roll roll = pickRoll(mutated, firstGene.getRoll());
             if (roll != null) {
                 secondIndex = rGen.nextInt(mutated.size());
                 LOGGER.debug("MTH: copying pattern from gene {} to gene {}, replacing roll in it",
                              firstIndex, secondIndex);
 
                 int[] pattern = firstGene.getPattern().clone();
 
                 Gene replacement = new Gene(pattern, roll);
                mutated.setGene(secondIndex, replacement);
             }
         }
 
         return mutated;
     }
 
     private double getRatio(Chromosome chromosome) {
         return chromosome.getMetricProvider().getAverageUnderProductionRatio() +
                 chromosome.getMetricProvider().getAverageOverProductionRatio();
     }
 
     private Roll pickRoll(Chromosome chromosome) {
         // mutable list of rolls
         List<Roll> rolls = Lists.newArrayList(chromosome.getContext().getProblem().getRolls());
 
         Set<Integer> usedRollIds = Sets.newHashSet();
         for (Gene gene : chromosome.getGenes()) {
             if (gene.getRoll() != null) {
                 usedRollIds.add(gene.getRoll().getInternalId());
             }
         }
 
         int i = 0;
         while (i < rolls.size()) {
             int id = rolls.get(i).getInternalId();
             if (usedRollIds.contains(id)) {
                 rolls.remove(i);
             } else {
                 i++;
             }
         }
 
         Roll picked = null;
         if (rolls.size() > 0) {
             int index = rGen.nextInt(rolls.size());
             picked = rolls.get(index);
         }
 
         return picked;
     }
 
     private Roll pickRoll(Chromosome chromosome, Roll protorype) {
         // mutable list of rolls
         List<Roll> rolls = Lists.newArrayList(chromosome.getContext().getProblem().getRolls());
 
         Set<Integer> usedRollIds = Sets.newHashSet();
         for (Gene gene : chromosome.getGenes()) {
             if (gene.getRoll() != null) {
                 usedRollIds.add(gene.getRoll().getInternalId());
             }
         }
 
         int i = 0;
         while (i < rolls.size()) {
             int id = rolls.get(i).getInternalId();
             if (usedRollIds.contains(id)) {
                 rolls.remove(i);
             } else {
                 i++;
             }
         }
 
         Roll picked = null;
         for (Roll roll : rolls) {
             if (roll.getWidth() == protorype.getWidth()) {
                 picked = roll;
                 break;
             }
         }
 
         return picked;
     }
 
     private int[] calcDemand(Roll roll, Chromosome chromosome) {
         List<Order> orders = chromosome.getContext().getProblem().getOrders();
 
         int[] demand = new int[orders.size()];
         for (int i = 0; i < orders.size(); i++) {
             double produced = chromosome.getProductionLengthForOrder(i);
             double orderLength = orders.get(i).getLength();
 
             if (orderLength > produced) {
                 double ratio = (orderLength - produced) / roll.getLength();
                 demand[i] = ratio < 1 ? 1 : (int) Math.floor(ratio);
             }
         }
 
         return demand;
     }
 
 }
