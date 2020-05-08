 package edu.wheaton.simulator.statistics;
 
 import java.util.Collection;
 
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.Prototype;
 
 /**
  * @author Daniel Gill, Nico Lasta
  *
  */
 public class GridRecorder {
 
 	private static int ii = 0; 
	
 	private StatisticsManager statManager;
 
 	/**
 	 * Constructor.
 	 */
 	public GridRecorder(StatisticsManager statManager) {
 		this.statManager = statManager;
 	}
 
 	/**
 	 * Record a step of the simulation. 
 	 * @param grid The grid at the point in time corresponding to step. 
 	 * @param step The point in the simulation being recorded. 
 	 * @param prototypes
 	 */
 	public void recordSimulationStep(Grid grid, Integer step, Collection<Prototype> prototypes) { 
//		System.out.println("=========" + (++ii));
 		for (Prototype prototype : prototypes) { 
 			statManager.addPrototypeSnapshot(SnapshotFactory.makePrototypeSnapshot(prototype, step));
 		}
 		for (Agent agent : grid) { 
			statManager.addGridEntity(SnapshotFactory.makeAgentSnapshot(agent, step));
			System.out.println(SnapshotFactory.makeAgentSnapshot(agent, step).serialize()); //Debugging 
 		}
 	}
	
 	/**
 	 * Record the time of the most recent iteration. 
 	 * @param timeOfRecentIteration the time (in ms) of the most recent turn of the simulation.
 	 */
 	public void updateTime(long timeOfRecentIteration, long addedDuration) { 
 		statManager.updateRecentTime(timeOfRecentIteration);
 		statManager.updateTotalTime(statManager.getSimulationDuration() + addedDuration);
 	}
 }
