 package edu.wheaton.simulator.statistics;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
import javax.naming.NameNotFoundException;

 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 
 import edu.wheaton.simulator.entity.EntityID;
 import edu.wheaton.simulator.entity.PrototypeID;
 
 public class StatisticsManager {
 
 	/**
 	 * The table on which all entity snapshots will be stored.
 	 */
 	private EntitySnapshotTable table;
 
 	/**
 	 * The number of steps the simulation has taken. Effectively it is the
 	 * largest step it has encountered in a Snapshot given to it.
 	 */
 	private int lastStep;
 
 	/**
 	 * The GridOberserver keeps track of changes in the grid.
 	 */
 	private GridRecorder gridObserver;
 
 	/**
 	 * Each index in the List stores the prototype snapshot associated with
 	 * that step in the simulation
 	 */
 	private HashMap<Integer, Map<PrototypeID, PrototypeSnapshot>> prototypes;
 
 	/**
 	 * The point at which the simulation started. 
 	 */
 	private long startTime; 
 	
 	/**
 	 * The point in time of the most recent step in the simulation. 
 	 */
 	private long mostRecentTime; 
 	
 	/**
 	 * The total duration of the simulation so far.
 	 */
 	private long totalTime;
 	
 	// TODO: Some sort of behavior queue mapping AgentID's to behavior
 	// representations.
 
 	/**
 	 * Private constructor to prevent wanton instantiation.
 	 */
 	public StatisticsManager() {
 		table = new EntitySnapshotTable();
 		gridObserver = new GridRecorder(this);
 		prototypes = new HashMap<Integer, Map<PrototypeID, PrototypeSnapshot>>();
 	}
 	
 	/**
 	 * Set the point in time at which the simulation began. 
 	 * @param startTime The time (in ms) when the simulation started.
 	 */
 	public void setStartTime(long startTime) { 
 		this.startTime = startTime; 
 	}
 	
 	/**
 	 * Update the most recent point in time in the simulation. 
 	 * @param mostRecentTime The time (in ms) at which the most recent iteration occurred.  
 	 */
 	public void updateRecentTime(long mostRecentTime) { 
 		this.mostRecentTime = Math.max(this.mostRecentTime, mostRecentTime); 
 	}
 	
 	/**
 	 * Update the total duration of the simulation.
 	 * @param newTime The new total time (in ms).
 	 */
 	public void updateTotalTime(long newTime) {
 		this.totalTime = newTime;
 	}
 	
 	/**
 	 * Get the number of steps taken in the simulation so far.
 	 * @return The number of steps taken in the simulation so far.
 	 */
 	public int getLastStep() {
 		return lastStep;
 	}
 	
 	/**
 	 * Get the starting time of the simulation.
 	 * @return The starting time of the simulation (System time, in ms).
 	 */
 	public long getSimulationStartTime() {
 		return startTime;
 	}
 	
 	/**
 	 * Get the duration of the simulation in ms. 
 	 * @return The duration of the simulation in ms. 
 	 */
 	public long getSimulationDuration() { 
 		return totalTime;
 	}
 
 	/**
 	 * Get the grid entity Observer.
 	 * 
 	 * @return The GridEntityObserver associated with this StatisticsManager.
 	 */
 	public GridRecorder getGridObserver() {
 		return gridObserver;
 	}
 
 	/**
 	 * Add a PrototypeSnapshot to the StatisticsManager. 
 	 * @param prototypeSnapshot The new prototype being recorded. 
 	 */
 	public void addPrototypeSnapshot(PrototypeSnapshot prototypeSnapshot) { 
 		if (prototypeSnapshot.step > lastStep) 
 			lastStep = prototypeSnapshot.step; 
 		Map<PrototypeID, PrototypeSnapshot> typeMap; 
 		if ((typeMap = prototypes.get(prototypeSnapshot.step)) != null) { 
 			typeMap.put(prototypeSnapshot.id, prototypeSnapshot);
 		} else { 
 			typeMap = new TreeMap<PrototypeID, PrototypeSnapshot>();
 			prototypes.put(new Integer(prototypeSnapshot.step), typeMap); 
 		}
 	}
 
 	/**
 	 * Store a snapshot of a gridEntity.
 	 * 
 	 * @param gridEntity
 	 *            The Snapshot to be stored.
 	 */
 	public void addGridEntity(EntitySnapshot gridEntity) {
 		table.putEntity(gridEntity);
 		if (gridEntity.step > lastStep)
 			lastStep = gridEntity.step;
 	}
 
 	/**
 	 * Get the IDs of all prototypes at the end of the simulation.
 	 * 
 	 * @return An ImmutableMap of PrototypeIDs extant at the end of the
 	 *         simulation.
 	 */
 	public ImmutableMap<String, PrototypeID> getProtypeIDs() {
 		return getPrototypeIDs(lastStep);
 	}
 
 	/**
 	 * Get the IDs of all prototypes at the given point in time.
 	 * 
 	 * @param step
 	 *            The specified point in the simulation.
 	 * @return An ImmutableMap of PrototypeIDs extant at the given step.
 	 */
 	public ImmutableMap<String, PrototypeID> getPrototypeIDs(int step) {
 		ImmutableMap.Builder<String, PrototypeID> builder = new ImmutableMap.Builder<String, PrototypeID>();
 		Map<PrototypeID, PrototypeSnapshot> map = prototypes.get(lastStep);
 		for (PrototypeID id : map.keySet()) {
 			builder.put(map.get(id).categoryName, id);
 		}
 		return builder.build();
 	}
 
 	/**
 	 * Returns the entire population at a given step of a given category of
 	 * Agent.
 	 * 
 	 * @param typeID
 	 *            The ID of the prototype of the desired type of Agent.
 	 * @param step
 	 *            The relevant moment in time.
 	 * @return An ImmutableSet of AgentSnapshots of typeID at step.
 	 */
 	private ImmutableSet<AgentSnapshot> getPopulationAtStep(
 			PrototypeID typeID, Integer step) {
 		ImmutableSet.Builder<AgentSnapshot> builder = new ImmutableSet.Builder<AgentSnapshot>();
 		ImmutableMap<EntityID, EntitySnapshot> totalPopulation = table
 				.getSnapshotsAtStep(step);
 		for (EntityID currentID : table.getSnapshotsAtStep(step).keySet()) {
 			EntitySnapshot currentEntity;
 			if ((currentEntity = totalPopulation.get(currentID)) instanceof AgentSnapshot) {
 				AgentSnapshot currentAgent = (AgentSnapshot) currentEntity;
 				if (currentAgent.prototype == typeID)
 					builder.add(currentAgent);
 			}
 		}
 		return builder.build();
 	}
 
 	/**
 	 * Get data for a graph of the population of a certain GridEntity over time
 	 * 
 	 * @param id
 	 *            The PrototypeID of the GridEntity to be tracked
 	 * @return An array where indexes refer to the step in the simulation and
 	 *         the value refers to the population of the targeted entity at
 	 *         that time
 	 */
 	public int[] getPopVsTime(PrototypeID id) {
 		int[] data = new int[lastStep];
 
 		for (int i = 0; i < data.length; i++) {
 			Map<PrototypeID, PrototypeSnapshot> map; 
 			PrototypeSnapshot currentSnapshot;
 			if((map = prototypes.get(i)) != null){
 				if ((currentSnapshot = prototypes.get(i).get(id)) != null) {
 					data[i] = currentSnapshot.population;
 				}
 			}
 		}
 		return data;
 	}
 
 	/**
 	 * Get data for a graph of the average value of a field over time
 	 * 
 	 * @param id
 	 *            The PrototypeID of the GridEntity to be tracked
 	 * @param FieldName
 	 *            The name of the field to be tracked
 	 * @return An array where indexes refer to the step in the simulation and
 	 *         the value refers to average field value at that time
 	 */
 	public double[] getAvgFieldValue(PrototypeID id, String FieldName) {
 		// set of steps in table
 		Set<Integer> steps = table.getAllSteps();
 
 		// array of averages
 		double[] averages = new double[steps.size()];
 
 		// marker for double[]
 		int i = 0;
 
 		// arraylist of the values at each step to average up
 		ArrayList<Double> stepVals = new ArrayList<Double>();
 
 		for (int step : steps) {
 			ImmutableSet<AgentSnapshot> agents = getPopulationAtStep(id, step);
 
 			for (AgentSnapshot agent : agents) {
 				ImmutableMap<String, FieldSnapshot> fields = agent.fields;
 
 				if (fields.containsKey(FieldName))
 					if (fields.get(FieldName).isNumber)
 						stepVals.add(fields.get(FieldName).getNumericalValue());
 			}
 
 			double total = 0;
 			for (Double val : stepVals)
 				total += val;
 			averages[i] = total / (agents.size());
 			total = 0;
 			i++;
 			stepVals.clear();
 		}
 		return averages;
 	}
 
 	/**
 	 * Get the average lifespan of a given GridEntity
 	 * 
 	 * @param id
 	 *            The PrototypeID of the GridEntity to be tracked
 	 * @return The average lifespan of the specified GridEntity
 	 */
 	public double getAvgLifespan(PrototypeID id) {
 		// List with index = step in the simulation, value = set of all agents
 		// alive at that time
 		List<Set<AgentSnapshot>> agentsByStep = new ArrayList<Set<AgentSnapshot>>();
 
 		// Set of all AgentSnapshots
 		Set<AgentSnapshot> allAgents = new HashSet<AgentSnapshot>();
 
 		for (int i = 0; i < lastStep; i++) {
 			Set<AgentSnapshot> stepData = getPopulationAtStep(id, i);
 			agentsByStep.set(i, stepData);
 			allAgents.addAll(stepData);
 		}
 
 		double avg = 0.0;
 
 		for (AgentSnapshot snap : allAgents) {
 			int birthTime = getBirthStep(agentsByStep, snap);
 			int deathTime = getDeathStep(agentsByStep, snap);
 
 			// Build the sum of all lifetimes - we'll divide by the number of
 			// agents at the end to get the average
 			System.out.println("\t\tB: " + birthTime);
 			System.out.println("\t\tD: " + deathTime);
 			avg += deathTime - birthTime;
 		}
 System.out.println("avg: " + avg);
 System.out.println("allAgents.size(): " + allAgents.size());
 		return avg / allAgents.size();
 	}
 
 	/**
 	 * Get the step number in which the Agent represented by a given
 	 * AgentSnapshot was born
 	 * 
 	 * @param agentsByStep
 	 *            A List with index = step in the simulation, value = set of
 	 *            all agents born at that time
 	 * @param target
 	 *            The AgentSnapshot of the agent we're looking for
 	 * @return The step number of the target Agent's birth
 	 * @throws NameNotFoundException
 	 *             the target Agent wasn't found
 	 */
 	private int getBirthStep(List<Set<AgentSnapshot>> agentsByStep,
 			AgentSnapshot target) {
 		for (int i = 0; i < lastStep; i++)
 			if (agentsByStep.get(i).contains(target))
 				return i;
 
 		throw new IllegalArgumentException(
 				"The target AgentSnapshot was not found");
 	}
 
 	/**
 	 * Get the step number in which the Agent represented by a given
 	 * AgentSnapshot died
 	 * 
 	 * @param agentsByStep
 	 *            A List with index = step in the simulation, value = set of
 	 *            all agents born at that time
 	 * @param target
 	 *            The AgentSnapshot of the agent we're looking for
 	 * @return The step number of the target Agent's death
 	 * @throws NameNotFoundException
 	 *             the target Agent wasn't found
 	 */
 	private int getDeathStep(List<Set<AgentSnapshot>> agentsByStep,
 			AgentSnapshot target) {
 		for (int i = lastStep; i > 0; i--)
 			if (agentsByStep.get(i).contains(target))
 				return i;
 
 		throw new IllegalArgumentException(
 				"The target AgentSnapshot was not found");
 	}
 }
