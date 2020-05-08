 package edu.wheaton.simulator.statistics;
 
 import java.util.Collection;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.entity.Slot;
 import edu.wheaton.simulator.simulation.Grid;
 
 /**
  * @author Daniel Gill, Nico Lasta
  *
  */
 public class GridObserver {
 
 	private StatisticsManager statManager;
 
 	/**
 	 * Constructor.
 	 */
 	public GridObserver(StatisticsManager statManager) {
 		this.statManager = statManager;
 	}
 
 	/**
 	 * Record a step of the simulation. 
 	 * @param grid The grid at the point in time corresponding to step. 
 	 * @param step The point in the simulation being recorded. 
 	 * @param prototypes
 	 */
 	public void recordSimulationStep(Grid grid, Integer step, Collection<Prototype> prototypes) { 
 		
 		for (Prototype prototype : prototypes) { 
 			statManager.addPrototypeSnapshot(SnapshotFactory.makePrototypeSnapshot(prototype, step));
 		}
 		
 		for (Slot s : grid) { 
 			statManager.addGridEntity(SnapshotFactory.makeSlotSnapshot(s, step));
 			Agent agent; 
 			if ((agent = s.getAgent()) != null) { 
 				statManager.addGridEntity(SnapshotFactory.makeAgentSnapshot(agent, step));
 			}
 		}
 		
 		
 		
 		for (Slot s : grid) { 
 //			EntitySnapshot slotSnap = SnapshotFactory.makeSlotSnapshot(s, step);
 //			statManager.addGridEntity(slotSnap);
 //			Agent agent = s.getAgent();
 //			if (agent == null) 
 //				continue;
 //			AgentSnapshot agentSnap = SnapshotFactory.makeAgentSnapshot(agent, step);
 //			statManager.addGridEntity(agentSnap);
 //			for (String currentPrototypeName : prototypes.keySet()) { 
 //				PrototypeSnapshot currentSnapshot;
 //				currentSnapshot = SnapshotFactory.makePrototypeSnapshot(
 //						prototypes.get(currentPrototypeName), step);
 //			}
 //			// TODO Make sure this method is completely implemented! (for the most part)
 		}
 	}
 }
