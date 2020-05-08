 package org.vu.contest.team24.tuner;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.vu.contest.team24.tuner.FitnessComparator;
 import org.vu.contest.team24.RandomSingleton;
 import org.vu.contest.team24.SimpleEvolutionaryStrategy;
 import org.vu.contest.team24.tuner.model.Gene;
 import org.vu.contest.team24.tuner.model.Individual;
 
 public class ParameterTuner {
 	private Class<?> evaluationClass;
 	private String evaluationClassName;
 	private List<Individual> population;
 	private TunerContestEvaluation evaluation;
 	private Random random;
 
 	public ParameterTuner() {
 	}
 	
 	public static void main(String[] args) {
 		new ParameterTuner().run(args);
 	}
 	
 	public void run(String[] args) {
 		for (String arg : args) {
 			if (arg.startsWith("-evaluation="))
 				evaluationClassName = arg.split("=")[1];
 			else
 				System.out.println("Invalid flag: '" + arg + "' !");
 		}
 
 		if (evaluationClassName == null)
 			throw new Error(
 					"Evaluation ID was not specified! Cannot run...\n Use -evaluation=<classnamehere> to specify the name of the evaluation class.");
 		
 		try {
 			evaluationClass = Class.forName(evaluationClassName);
 		} catch (Throwable throwable) {
 			System.err.println("Could not load evaluation class for evaluation '" + evaluationClassName + "'");
 			throwable.printStackTrace();
 			System.exit(1);
 		}
 		
 		try {
 			this.evaluation = (TunerContestEvaluation)this.evaluationClass.newInstance();
 		} catch (Throwable throwable) {
 			System.err.println("ExecutionError: Could not instantiate evaluation object for evaluation '" + evaluationClassName + "'");
 			throwable.printStackTrace();
 			System.exit(1);
 		}
 		
 		RandomSingleton randomSingleton = RandomSingleton.getInstance();
 		Random random = new Random();
 		random.setSeed(new Date().getTime());
 		randomSingleton.setRandom(random);
 		this.random = random;
 		for(int generation = 0; generation < 5; generation++) {
 			evolveGeneration();
 					
 			System.out.println("Generation "+ generation);
 			
 
 		}
 	}
 	
 	private void evolveGeneration() {
 		int populationSize = 16;
 		if(this.population == null) {
 			this.population = new ArrayList<Individual>(populationSize);
 			for(int n = 0; n < populationSize; n++) {
 				this.population.add(new Individual());
 			}
 		}
 		
		TunerContestEvaluation evaluation = this.evaluation;
 		Map<Individual, Double> fitnesses = new HashMap<Individual, Double>(this.population.size());
 		for (Individual individual : this.population) {
 			SimpleEvolutionaryStrategy strategy = new SimpleEvolutionaryStrategy(evaluation);
 			individual.configureStrategy(strategy);
 			
 			ParameterEvaluator parameterEvaluator = new ParameterEvaluator(evaluation, strategy);
 			parameterEvaluator.run();
 			evaluation.reset();
 			System.out.println(individual);
 			fitnesses.put(individual, parameterEvaluator.getFitness());
 		}
 		
 		List<Individual> nextPopulation = selectParents(this.population, fitnesses);
 		for (Individual individual : nextPopulation) {
 			for(Gene g : individual.getGenes()) {
 				if(!(this.random.nextDouble() > 0.5)) {
 					g.mutate();
 				}
 			}
 		}
 		
 		for(int i = 0; i < populationSize; i++) {
 			Individual individual = nextPopulation.get(i);
 			
 			if(!(this.random.nextDouble() > 0.2)) {
 				int j;
 				do {
 					j = this.random.nextInt(populationSize);
 				} while(j == i);
 				Individual otherIndividual = nextPopulation.get(j);
 				
 				int chromosomeSize = individual.getGenes().size();
 				for(int n = 0; n < chromosomeSize; n++) {
 					Gene gene = individual.getGene(n);
 					Gene otherGene = otherIndividual.getGene(n);
 					gene.crossover(otherGene);
 				}
 			}
 		}
 		
 		this.population = nextPopulation;
 		
 	}
 	
 	private List<Individual> selectParents(List<Individual> individuals, Map<Individual, Double> fitnesses) {
 		// Sort list to get the ranking.
 		individuals = new ArrayList<Individual>(individuals);
 		Collections.sort(individuals, new FitnessComparator(fitnesses));
 		
 		Individual fittestIndividual = individuals.get(0);
 		System.out.println("Fittest individual has fitness: "+fitnesses.get(fittestIndividual));
 		
 		int populationSize = individuals.size();
 		double spacing = 1 / populationSize;
 		double position = this.random.nextDouble() * spacing;
 		List<Individual> parents = new ArrayList<Individual>(populationSize);
 		
 		int i = 0;
 		int currentParent = 0;
 		double currentCumulative = probabilityForRank(i, populationSize, 2.0);
 		while(currentParent < populationSize) {
 			while(currentCumulative < position) {
 				currentCumulative += probabilityForRank(++i, populationSize, 2.0);
 			}
 			
 			parents.add(new Individual(individuals.get(i)));
 			currentParent++;
 
 			position += spacing;
 			// We need to implement wraparound since this is also the case in the (n-armed)
 			// roulette wheel...
 			if(position > 1.0) {
 				position -= 1.0;
 				i = 0;
 				currentCumulative = probabilityForRank(i, populationSize, 2.0);
 			}
 		}
 		
 		return parents;
 	}
 	
 	private double probabilityForRank(int rank, int size, double expectedOffspring) {
 		return ((2 - expectedOffspring) / size) + (((2 * rank) * (expectedOffspring - 1)) / (size * (size - 1)));
 	}
 	
 }
