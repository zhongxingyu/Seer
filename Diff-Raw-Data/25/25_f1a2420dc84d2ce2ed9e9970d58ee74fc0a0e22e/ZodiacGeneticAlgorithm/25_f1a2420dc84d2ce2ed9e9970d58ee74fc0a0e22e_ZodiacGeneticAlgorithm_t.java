 package com.ciphertool.zodiacengine.genetic;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Required;
 
 public class ZodiacGeneticAlgorithm implements GeneticAlgorithm {
 	private Logger log = Logger.getLogger(getClass());
 	private Integer populationSize;
 	private Double survivalRate;
 	private Double mutationRate;
 	private Double crossoverRate;
 	private Integer maxGenerations;
 	private Population population;
 	private CrossoverAlgorithm crossoverAlgorithm;
 	private FitnessEvaluator fitnessEvaluator;
 	private FitnessComparator fitnessComparator;
 
 	public ZodiacGeneticAlgorithm() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.ciphertool.zodiacengine.genetic.GeneticAlgorithm#iterateUntilTermination
 	 * ()
 	 */
 	@Override
 	public Chromosome iterateUntilTermination() {
 		if (this.population == null) {
 			log.info("Attempted to start algorithm with a null population.  Spawning population of size "
 					+ populationSize + ".");
 
 			this.spawnInitialPopulation();
 		}
 
 		for (int i = 1; i <= maxGenerations; i++) {
 			log.info("Generation " + i);
 
 			crossover();
 
 			mutate();
 
 			select();
 
 			population.populateIndividuals(populationSize);
 
 			population.evaluateFitness();
 		}
 
 		return population.getBestFitIndividual();
 	}
 
 	@Override
 	public void select() {
 		List<Chromosome> individuals = this.population.getIndividuals();
 
 		Collections.sort(individuals, fitnessComparator);
 
 		int initialPopulationSize = this.population.size();
 
 		int survivorIndex = (int) (initialPopulationSize * survivalRate);
 
 		for (int i = survivorIndex; i < initialPopulationSize; i++) {
 			individuals.remove(survivorIndex);
 		}
 	}
 
 	/*
 	 * Roulette Wheel Selection
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see com.ciphertool.zodiacengine.genetic.GeneticAlgorithm#crossover()
 	 */
 	@Override
 	public void crossover() {
 		if (this.population.size() < 2) {
 			log.info("Unable to perform crossover because there is only 1 individual in the population. Returning.");
 
 			return;
 		}
 
 		Chromosome mom = null;
 		Chromosome dad = null;
 		Chromosome child1 = null;
 		Chromosome child2 = null;
 
 		for (int i = 0; i < crossoverRate; i++) {
 			mom = this.population.spinRouletteWheel();
 
 			/*
 			 * Keep retrying until we find a different Chromosome.
 			 */
 			do {
 				dad = this.population.spinRouletteWheel();
			} while (mom == dad);
 
 			child1 = crossoverAlgorithm.crossover(mom, dad);
 
 			child2 = crossoverAlgorithm.crossover(dad, mom);
 		}
 
 		/*
 		 * Remove the parents from the populationa and add the children since
 		 * they are guaranteed to be at least as fit as their parents
 		 */
 		this.population.removeIndividual(mom);
 		this.population.removeIndividual(dad);
 
 		this.population.addIndividual(child1);
 		this.population.addIndividual(child2);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.ciphertool.zodiacengine.genetic.GeneticAlgorithm#mutate()
 	 */
 	@Override
 	public void mutate() {
 		for (int i = 0; i < mutationRate; i++) {
 			/*
 			 * Mutate a gene within a Chromosome
 			 */
 			Chromosome original = this.population.spinRouletteWheel();
 
 			Chromosome mutation = original.clone();
 
 			mutation.mutateRandomGene();
 
 			fitnessEvaluator.evaluate(mutation);
 
 			if (mutation.getFitness() > original.getFitness()) {
 				this.population.removeIndividual(original);
 				this.population.addIndividual(mutation);
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.ciphertool.zodiacengine.genetic.GeneticAlgorithm#spawnInitialPopulation
 	 * ()
 	 */
 	@Override
 	public void spawnInitialPopulation() {
 		this.population.populateIndividuals(populationSize);
 
 		this.population.evaluateFitness();
 	}
 
 	/**
 	 * @return the population
 	 */
 	public Population getPopulation() {
 		return population;
 	}
 
 	/**
 	 * @param population
 	 *            the population to set
 	 */
 	@Required
 	public void setPopulation(Population population) {
 		this.population = population;
 	}
 
 	/**
 	 * @param populationSize
 	 */
 	@Required
 	public void setPopulationSize(Integer populationSize) {
 		this.populationSize = populationSize;
 	}
 
 	/**
 	 * @param mutationRate
 	 *            the mutationRate to set
 	 */
 	@Required
 	public void setMutationRate(Double mutationRate) {
 		this.mutationRate = mutationRate;
 	}
 
 	/**
 	 * @param crossoverRate
 	 *            the crossoverRate to set
 	 */
 	@Required
 	public void setCrossoverRate(Double crossoverRate) {
 		this.crossoverRate = crossoverRate;
 	}
 
 	/**
 	 * @param maxGenerations
 	 *            the maxGenerations to set
 	 */
 	@Required
 	public void setMaxGenerations(Integer maxGenerations) {
 		this.maxGenerations = maxGenerations;
 	}
 
 	/**
 	 * @param crossoverAlgorithm
 	 *            the crossoverAlgorithm to set
 	 */
 	@Required
 	public void setCrossoverAlgorithm(CrossoverAlgorithm crossoverAlgorithm) {
 		this.crossoverAlgorithm = crossoverAlgorithm;
 	}
 
 	/**
 	 * @param fitnessEvaluator
 	 *            the fitnessEvaluator to set
 	 */
 	@Required
 	public void setFitnessEvaluator(FitnessEvaluator fitnessEvaluator) {
 		this.fitnessEvaluator = fitnessEvaluator;
 	}
 
 	/**
 	 * @param survivalRate
 	 *            the survivalRate to set
 	 */
 	@Required
 	public void setSurvivalRate(Double survivalRate) {
 		this.survivalRate = survivalRate;
 	}
 
 	/**
 	 * @param fitnessComparator
 	 *            the fitnessComparator to set
 	 */
 	@Required
 	public void setFitnessComparator(FitnessComparator fitnessComparator) {
 		this.fitnessComparator = fitnessComparator;
 	}
 }
