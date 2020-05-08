 package com.ciphertool.zodiacengine.genetic;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Required;
 
 public class LiberalCrossoverAlgorithm implements CrossoverAlgorithm {
 	private Logger log = Logger.getLogger(getClass());
 	private FitnessEvaluator fitnessEvaluator;
 	private GeneListDao geneListDao;
 
 	/**
 	 * This crossover algorithm does a liberal amount of changes since it
 	 * replaces genes regardless of their begin and end sequence positions
 	 * 
 	 * @see com.ciphertool.zodiacengine.genetic.CrossoverAlgorithm#crossover(com.ciphertool.zodiacengine.genetic.Chromosome,
 	 *      com.ciphertool.zodiacengine.genetic.Chromosome)
 	 */
 	@Override
 	public Chromosome crossover(Chromosome parentA, Chromosome parentB) {
 		Chromosome child = parentA.clone();
 
 		int childGeneIndex = 0;
 		Gene geneCopy = null;
 		Integer originalFitness = 0;
 
 		/*
 		 * Make sure we don't exceed parentB's index, or else we will get an
 		 * IndexOutOfBoundsException
 		 */
 		while (childGeneIndex < child.getGenes().size()
 				&& childGeneIndex < parentB.getGenes().size()) {
 			/*
 			 * Replace from parentB and reevaluate to see if it improves.
 			 */
 			geneCopy = child.getGenes().get(childGeneIndex).clone();
 
 			originalFitness = child.getFitness();
 
 			child.replaceGene(childGeneIndex, parentB.getGenes().get(childGeneIndex).clone());
 
			/*
			 * TODO: these genes are never removed if the cloned gene does not
			 * increase fitness, and how come we don't run into a problem in the
			 * below method when we comment this out?
			 * 
			 * Moreover, why doesn't this method seem to get called until about
			 * 10 generations in?
			 */
 			while (child.actualSize() < child.targetSize()) {
 				child.addGene(geneListDao.findRandomGene(child, child.actualSize() - 1));
 			}
 
 			fitnessEvaluator.evaluate(child);
 
 			/*
 			 * Revert to the original gene if this did not increase fitness
 			 */
 			if (child.getFitness() <= originalFitness) {
 				child.replaceGene(childGeneIndex, geneCopy);
 			}
 
 			childGeneIndex++;
 		}
 
 		/*
 		 * Child is guaranteed to have at least as good fitness as its parent
 		 */
 		return child;
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
 	 * @param geneListDao
 	 *            the geneListDao to set
 	 */
 	@Required
 	public void setGeneListDao(GeneListDao geneListDao) {
 		this.geneListDao = geneListDao;
 	}
 }
