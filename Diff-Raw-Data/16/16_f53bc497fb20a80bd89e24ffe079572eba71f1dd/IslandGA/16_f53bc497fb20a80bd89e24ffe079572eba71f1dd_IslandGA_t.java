 package edu.cs523.project3.ga.island;
 
 import java.util.ArrayList;
 
 import edu.cs523.project3.model.Ship;
 
 public class IslandGA 
 {
 	private double crossoverRate; // from 0 to 1
 	private double mutationRate;  // from 0 to 1
 	private double mutationRange;
 	
 	private ArrayList<Ship> ships;
 	private int populationSize;
 	
 	private double bestScore;
 	private double avgScore;
 	private Ship fittestShip;
 	
 	public IslandGA(int populationSize)
 	{
 		ships = new ArrayList<Ship>();
 		this.populationSize = populationSize;
 		
 		this.crossoverRate = 0.1;
 		this.mutationRate = 0.1;
 		this.mutationRange = 0.1; // equivalent to about 6 degrees mutation
 		
 		bestScore = 0;
 		avgScore = 0;
 		fittestShip = null;
 		
 		init(); // initializes the GA
 	}
 	
 	public IslandGA(int populationSize, Ship seed)
 	{
 		ships = new ArrayList<Ship>();
 		this.populationSize = populationSize;
 		
 		this.crossoverRate = 0.1;
 		this.mutationRate = 0.1;
 		this.mutationRange = 0.1; // equivalent to about 6 degrees mutation
 		
 		bestScore = 0;
 		avgScore = 0;
 		fittestShip = null;
 		
 		initWithSeed(seed); // initializes the GA
 	}
 	
 	public IslandGA(int populationSize, double crossoverRate, double mutationRate, double mutationRange)
 	{
 		ships = new ArrayList<Ship>();
 		
 		this.populationSize = populationSize;
 		this.crossoverRate = crossoverRate;
 		this.mutationRate = mutationRate;
 		this.mutationRange = mutationRange;
 		
 		bestScore = 0;
 		avgScore = 0;
 		fittestShip = null;
 		
 		init(); // initializes the GA
 	}
 	
 	
 	public double getCrossoverRate()
 	{
 		return this.crossoverRate;
 	}
 	
 	public double getMutationRange()
 	{
 		return this.mutationRange;
 	}
 	
 	public Ship getShip(int index)
 	{
 		return ships.get(index);
 	}
 	
 	public void evolve() 
 	{
 		ArrayList<Ship> newShips = new ArrayList<Ship>();
 		
 		// collecting some statistics
 		bestScore = 0;
 		fittestShip = null;
 		double totalScores = 0;
 		
 		for (int i = 0; i < ships.size(); i++)
 		{
 			double energy = ships.get(i).getScore().energy;
 			
 			totalScores += energy;
 			if (energy > bestScore) 
 			{
 				bestScore = energy;
 				fittestShip = ships.get(i);
 			}
 		}
 		
 		avgScore = totalScores / ships.size();
 		
 		// evolution step
 		for (int i = 0; i < ships.size(); i++)
 		{
 			int[] parents = new int[2];
 			int[] candidates = new int[2];
 			
 			for (int j = 0; j < parents.length; j++)
 			{
 				for (int k = 0; k < candidates.length; k++)
 					candidates[k] = (int)(Math.random() * populationSize);
 					
 				while (candidates[0] == candidates[1])
 					candidates[1] = (int)(Math.random() * populationSize);
 				
 				if (ships.get(candidates[0]).getScore().energy > ships.get(candidates[1]).getScore().energy)
 					parents[j] = candidates[0];
 				else
 					parents[j] = candidates[1];
 			}
 			
 			Ship child = ships.get(parents[1]);
 			for (int j = 0; j < IslandShipInitializer.getNumberOfSensors(); j++)
 			{
 				if (Math.random() < this.crossoverRate)
					child.getSensors().set(j, ships.get(parents[0]).getSensors().get(j));
 			}
 			
 			mutate(child);
 			
 			newShips.add(child);
 		}
 		
 		this.ships = newShips;
 	}
 	
 	
 	public double getBestScore()
 	{
 		return this.bestScore;
 	}
 	
 	public double getAverageScore()
 	{
 		return this.avgScore;
 	}
 	
 	public Ship getFittestShip()
 	{
 		return this.fittestShip;
 	}
 	
 	public ArrayList<Ship> getShips()
 	{
 		return this.ships;
 	}
 	
 	
 	private void init() 
 	{
 		for (int i = 0; i < populationSize; i++)
 			ships.add(IslandShipInitializer.getInitializedShip());
 	}
 	
 	private void initWithSeed(Ship seed)
 	{
 		for (int i = 0; i < populationSize; i++)
 			ships.add(IslandShipInitializer.getInitializedShipFromSeed(seed));
 	}
 	
 	private void mutate(Ship ship)
 	{
 		for (int i = 0; i < IslandShipInitializer.getNumberOfSensors(); i++)
 		{
 			
 		}
 	}
 }
