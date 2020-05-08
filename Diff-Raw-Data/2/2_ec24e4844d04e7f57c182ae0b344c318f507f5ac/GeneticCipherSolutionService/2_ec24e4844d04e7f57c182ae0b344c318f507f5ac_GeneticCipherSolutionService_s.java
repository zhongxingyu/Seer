 /**
  * Copyright 2012 George Belden
  * 
  * This file is part of ZodiacEngine.
  * 
  * ZodiacEngine is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * ZodiacEngine is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * ZodiacEngine. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.ciphertool.zodiacengine.gui.service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Required;
 
 import com.ciphertool.genetics.algorithms.GeneticAlgorithm;
 import com.ciphertool.genetics.entities.Chromosome;
 import com.ciphertool.zodiacengine.dao.SolutionSetDao;
 import com.ciphertool.zodiacengine.entities.Solution;
 import com.ciphertool.zodiacengine.entities.SolutionId;
 import com.ciphertool.zodiacengine.entities.SolutionSet;
 import com.ciphertool.zodiacengine.genetic.adapters.SolutionChromosome;
 
 public class GeneticCipherSolutionService extends AbstractCipherSolutionService {
 	private Logger log = Logger.getLogger(getClass());
 
 	private GeneticAlgorithm geneticAlgorithm;
 	private SolutionSetDao solutionSetDao;
 	private String[] commandsBefore;
 	private String[] commandsAfter;
 	private long start;
 
 	/**
 	 * @param args
 	 * @throws InterruptedException
 	 */
 	public void start() throws InterruptedException {
 		start = System.currentTimeMillis();
 
 		geneticAlgorithm.spawnInitialPopulation();
 
 		log.info("Took " + (System.currentTimeMillis() - start)
 				+ "ms to spawn initial population of size "
 				+ geneticAlgorithm.getPopulation().size());
 
 		geneticAlgorithm.iterateUntilTermination();
 	}
 
 	public void endImmediately() {
 		geneticAlgorithm.requestStop();
 	}
 
 	public void stop() {
 		persistPopulation();
 
 		List<Chromosome> bestFitIndividuals = geneticAlgorithm.getBestFitIndividuals();
 
 		/*
 		 * Print out summary information
 		 */
 		log.info("Took " + (System.currentTimeMillis() - start) + "ms to finish.");
 		log.info("Best " + bestFitIndividuals.size() + " solutions in ascending order: ");
 		for (Chromosome bestFitIndividual : bestFitIndividuals) {
 			log.info(bestFitIndividual);
 		}
 	}
 
 	protected void setUp() {
 		/*
 		 * currentCommand is really just used for exception catching.
 		 */
 		String currentCommand = "";
 
 		try {
 			for (String commandBefore : commandsBefore) {
 				log.info("Executing shell command on start up: " + commandBefore);
 				currentCommand = commandBefore;
 
 				Runtime.getRuntime().exec(commandBefore);
 			}
 		} catch (IOException e) {
 			log.warn("Unable to execute commmand before app begin: " + currentCommand
 					+ ".  Continuing.");
 		}
 	}
 
 	protected void tearDown() {
 		/*
 		 * currentCommand is really just used for exception catching.
 		 */
 		String currentCommand = "";
 
 		try {
 			for (String commandAfter : commandsAfter) {
 				log.info("Executing shell command on termination: " + commandAfter);
 				currentCommand = commandAfter;
 
 				Runtime.getRuntime().exec(commandAfter);
 			}
 		} catch (IOException e) {
 			log.warn("Unable to execute commmand after app end: " + currentCommand
 					+ ".  Continuing.");
 		}
 	}
 
 	private void persistPopulation() {
		log.info("Please wait while the population of size is persisted to database.");
 
 		List<Chromosome> individuals = geneticAlgorithm.getPopulation().getIndividuals();
 
 		SolutionSet solutionSet = new SolutionSet();
 		solutionSet.setSolutions(new ArrayList<Solution>());
 
 		int nextId = 0;
 		for (Chromosome individual : individuals) {
 			solutionSet.getSolutions().add((SolutionChromosome) individual);
 
 			nextId++;
 			SolutionId solutionId = new SolutionId(nextId, solutionSet);
 			((SolutionChromosome) individual).setSolutionId(solutionId);
 		}
 
 		solutionSetDao.insert(solutionSet);
 	}
 
 	/**
 	 * @param geneticAlgorithm
 	 *            the geneticAlgorithm to set
 	 */
 	@Required
 	public void setGeneticAlgorithm(GeneticAlgorithm geneticAlgorithm) {
 		this.geneticAlgorithm = geneticAlgorithm;
 	}
 
 	/**
 	 * @param commandsBefore
 	 *            the commandsBefore to set
 	 */
 	@Required
 	public void setCommandsBefore(String[] commandsBefore) {
 		this.commandsBefore = commandsBefore;
 	}
 
 	/**
 	 * @param commandsAfter
 	 *            the commandsAfter to set
 	 */
 	@Required
 	public void setCommandsAfter(String[] commandsAfter) {
 		this.commandsAfter = commandsAfter;
 	}
 
 	/**
 	 * @param solutionSetDao
 	 *            the solutionSetDao to set
 	 */
 	@Required
 	public void setSolutionSetDao(SolutionSetDao solutionSetDao) {
 		this.solutionSetDao = solutionSetDao;
 	}
 }
