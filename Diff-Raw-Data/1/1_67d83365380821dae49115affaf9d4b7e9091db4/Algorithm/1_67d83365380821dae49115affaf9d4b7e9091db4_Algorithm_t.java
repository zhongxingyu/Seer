 /** Copyright 2012, Adam L. Davis */
 package com.adamldavis.ga;
 
 import java.util.Random;
 
 /**
  * Main class defining the GA algorithm.
  * 
  * <p>
  * Example usage:
  * 
  * <pre>
  * {@code
  * 		// Create an initial population
  *  	Individual.setDefaultGeneLength(32);
  * 		Population myPop = new Population(fitnessCalc, 50, true);
  * 
  * 		// Evolve our population until we reach an optimum solution
  * 		while (myPop.getFittest().getFitness() < fitnessCalc.getMaxFitness()) {
  * 			myPop = Algorithm.evolvePopulation(myPop);
  * 		}
 * }
  * </pre>
  * 
  * @author Adam Davis
  * 
  */
 public class Algorithm {
 
 	/* GA parameters */
 	private static final double uniformRate = 0.5;
 	private static final double mutationRate = 0.015;
 	private static final int tournamentSize = 5;
 	private static final boolean elitism = true;
 
 	/* Public methods */
 
 	// Evolve a population
 	public static Population evolvePopulation(Population pop) {
 		Population newPopulation = new Population(pop.size());
 
 		// Keep our best individual
 		if (elitism) {
 			newPopulation.saveIndividual(0, pop.getFittest());
 		}
 
 		// Crossover population
 		int elitismOffset;
 		if (elitism) {
 			elitismOffset = 1;
 		} else {
 			elitismOffset = 0;
 		}
 		// Loop over the population size and create new individuals with
 		// crossover
 		for (int i = elitismOffset; i < pop.size(); i++) {
 			Individual indiv1 = tournamentSelection(pop);
 			Individual indiv2 = tournamentSelection(pop);
 			Individual newIndiv = crossover(indiv1, indiv2);
 			newPopulation.saveIndividual(i, newIndiv);
 		}
 
 		// Mutate population
 		for (int i = elitismOffset; i < newPopulation.size(); i++) {
 			mutate(newPopulation.getIndividual(i));
 		}
 
 		return newPopulation;
 	}
 
 	// Crossover individuals
 	private static Individual crossover(Individual indiv1, Individual indiv2) {
 		Individual newSol = new Individual(indiv1.fitnessCalculator);
 		// Loop through genes
 		for (int i = 0; i < indiv1.size(); i++) {
 			// Crossover
 			if (Math.random() <= uniformRate) {
 				newSol.setGene(i, indiv1.getGene(i));
 			} else {
 				newSol.setGene(i, indiv2.getGene(i));
 			}
 		}
 		return newSol;
 	}
 
 	// Mutate an individual
 	private static void mutate(Individual indiv) {
 		// Loop through genes
 		for (int i = 0; i < indiv.size(); i++) {
 			if (Math.random() <= mutationRate) {
 				indiv.mutateGene(i);
 			}
 		}
 	}
 
 	/** Select individuals for crossover. */
 	private static Individual tournamentSelection(Population pop) {
 		// Create a tournament population
 		Population tournament = new Population(tournamentSize);
 		Random rnd = new Random();
 		// For each place in the tournament get a random individual
 		for (int i = 0; i < tournamentSize; i++) {
 			int randomId = rnd.nextInt(pop.size());
 			tournament.saveIndividual(i, pop.getIndividual(randomId));
 		}
 		return tournament.getFittest();
 	}
 }
