 /* This file is part of Eternity II Editor.
  * 
  * fuck yeah seaking
  */
 
 package org.alcibiade.eternity.editor.solver.genetic;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import org.alcibiade.eternity.editor.model.GridModel;
 import org.alcibiade.eternity.editor.solver.ClusterManager;
 import org.alcibiade.eternity.editor.solver.EternitySolver;
 
 public abstract class GeneticSolver extends EternitySolver {
 
 	protected GridModel problemGrid;
 	protected GridModel solutionGrid;
 	protected final GridModel originalGrid;
 	protected ArrayList<GridModel> population;
 	protected long iterations;
 	
 	public GeneticSolver(GridModel grid, GridModel solutionGrid, ClusterManager clusterManager, int populationSize) {
 		super(clusterManager);	
 		
 		this.problemGrid = grid;
		this.solutionGrid = solutionGrid;
		problemGrid.copyTo(solutionGrid);
 		this.originalGrid = grid.clone();
 		originalGrid.setReadOnly(true);

 		this.population = new ArrayList<GridModel>();
 		
 		populate(populationSize, grid);
 	}
 	
 	private void populate(int populationSize, GridModel theGrid) {
 		while (population.size() < populationSize) {
 			GridModel somegrid = theGrid.clone();
 			somegrid.shuffle();
 			population.add(somegrid);
 		}
 	
 	}
 	
 	/**
 	 * returns the fitness of a grid (0..1)
 	 */
 	public float fitness(GridModel grid) {
 		
 		float connections = grid.countConnections();
 		float pairs = grid.countPairs();
 		float fitness = pairs / connections;
 
 		return fitness;
 	}
 	
 	/**
 	 * returns the most fit individual
 	 */
 	public GridModel getMostFitIndividual() {
 		
 		assert this.population.size() > 0;
 		
 		Iterator it = this.population.iterator();
 		GridModel bestIndividual = null;
 		float bestFitness = 0;
 		
 		while (it.hasNext()) {
 		
 			GridModel thisIndividual = (GridModel)it.next();
 			float thisFitness = fitness(thisIndividual);
 			
 			if (thisFitness >= bestFitness) {
 				bestIndividual = thisIndividual;
 				bestFitness = thisFitness;
 			}
 		}
 		
 		return bestIndividual;
 	}
 	
 	/*
 	 * Get least fit individual
 	 */
 	public GridModel getLeastFitIndividual() {
 		
 		assert this.population.size() > 0;
 		
 		Iterator it = this.population.iterator();
 		GridModel worstIndividual = null;
 		float worstFitness = Integer.MAX_VALUE;
 		
 		while (it.hasNext()) {
 		
 			GridModel thisIndividual = (GridModel)it.next();
 			float thisFitness = fitness(thisIndividual);
 			
 			if (thisFitness < worstFitness) {
 				worstIndividual = thisIndividual;
 				worstFitness = thisFitness;
 			}
 		}
 		
 		return worstIndividual;
 	}	
 
 	@Override
 	public long getIterations() {
 		return iterations;
 	}
 	
 	
 }
