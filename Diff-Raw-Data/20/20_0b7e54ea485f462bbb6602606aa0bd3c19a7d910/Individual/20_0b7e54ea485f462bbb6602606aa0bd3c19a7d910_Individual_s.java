 package org.vu.contest.team24.tuner.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.vu.contest.team24.SimpleEvolutionaryStrategy;
 
 public class Individual {
 	public static final int GENE_INDEX_POPULATION_SIZE = 0;
 	public static final int GENE_INDEX_MUTATION_PROBABILITY = 1;
 	public static final int GENE_INDEX_MUTATION_PROBABILITY_SCALING_FACTOR = 2;
 	public static final int GENE_INDEX_MUTATION_STANDARD_DEVIATION = 3;
 	public static final int GENE_INDEX_MUTATION_STANDARD_DEVIATION_SCALING_FACTOR = 4;
 	public static final int GENE_INDEX_CROSSOVER_PROBABILITY = 5;
 	public static final int GENE_INDEX_CROSSOVER_PROBABILITY_SCALING_FACTOR = 6;
 	public static final int GENE_INDEX_FITTEST_EXPECTED_OFFSPRING = 7;
 	
 	private List<Gene> genes;
 	
 	public Individual() {
 		this.genes = new ArrayList<Gene>();
 		this.genes.add(new MultipleChoiceGene("PopulationSize", new Object[] {10, 15, 30, 50}));
		this.genes.add(new DoubleGene(0.0, 1.0)); // Mutation chance
 		this.genes.add(new DoubleGene(0.75, 1.0)); // Mutation chance scaling
 		this.genes.add(new DoubleGene(0.1, 1.0)); // Mutation stdev
 		this.genes.add(new DoubleGene(0.75, 1.0)); // Mutation stdev scaling
		this.genes.add(new DoubleGene(0.0, 1.0)); // Crossover chance
 		this.genes.add(new DoubleGene(0.75, 1.0)); // Crossover chance scaling
 		this.genes.add(new DoubleGene(1.5, 2.0)); // Expected offspring for fittest parent
 	}
 	
 	public Individual(Individual individual) {
 		this.genes = new ArrayList<Gene>(individual.genes.size());
 		for (Gene gene : individual.genes) {
 			if(gene instanceof DoubleGene) {
 				this.genes.add(new DoubleGene((DoubleGene)gene));
 			} else if(gene instanceof MultipleChoiceGene) {
 				this.genes.add(new MultipleChoiceGene((MultipleChoiceGene)gene));
 			}
 		}
 	}
 	
 	public Gene getGene(int index) {
 		return this.genes.get(index);
 	}
 	
 	public List<Gene> getGenes() {
 		return this.genes;
 	}
 	
 	public void configureStrategy(SimpleEvolutionaryStrategy strategy) {
 		MultipleChoiceGene populationSizeGene = (MultipleChoiceGene)this.genes.get(GENE_INDEX_POPULATION_SIZE);
 		DoubleGene mutationProbabilityGene = (DoubleGene)this.genes.get(GENE_INDEX_MUTATION_PROBABILITY);
 		DoubleGene mutationStandardDeviationGene = (DoubleGene)this.genes.get(GENE_INDEX_MUTATION_STANDARD_DEVIATION);
 		DoubleGene mutationProbabilityScalingGene = (DoubleGene)this.genes.get(GENE_INDEX_MUTATION_PROBABILITY_SCALING_FACTOR);
 		DoubleGene mutationStandardDeviationScalingGene = (DoubleGene)this.genes.get(GENE_INDEX_MUTATION_STANDARD_DEVIATION_SCALING_FACTOR);
 		DoubleGene crossoverProbabilityGene = (DoubleGene)this.genes.get(GENE_INDEX_CROSSOVER_PROBABILITY);
 		DoubleGene crossoverProbabilityScalingGene = (DoubleGene)this.genes.get(GENE_INDEX_CROSSOVER_PROBABILITY_SCALING_FACTOR);
 		DoubleGene fittestExpectedOffspringGene = (DoubleGene)this.genes.get(GENE_INDEX_FITTEST_EXPECTED_OFFSPRING);
 		
 		strategy.setPopulationSize((Integer)populationSizeGene.getValue());
 		strategy.setMutationChance((Double)mutationProbabilityGene.getValue());
 		strategy.setMutationChanceScalingFactor((Double)mutationProbabilityScalingGene.getValue());
 		strategy.setMutationStandardDeviation((Double)mutationStandardDeviationGene.getValue());
 		strategy.setMutationStandardDeviationScalingFactor((Double)mutationStandardDeviationScalingGene.getValue());
 		strategy.setCrossoverChance((Double)crossoverProbabilityGene.getValue());
 		strategy.setCrossoverChanceScalingFactor((Double)crossoverProbabilityScalingGene.getValue());
 		strategy.setFittestExpectedOffspring((Double)fittestExpectedOffspringGene.getValue());
 	}
 }
