 package edu.wheaton.simulator.statistics;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 
 import edu.wheaton.simulator.entity.AgentID;
 
 public class StatisticsManager {
 
 	/**
 	 * The table on which all entity snapshots will be stored.
 	 */
 	private AgentSnapshotTable table;
 
 	/**
 	 * The number of steps the simulation has taken. Effectively it is the
 	 * largest step it has encountered in a Snapshot given to it.
 	 */
 	private int lastStep;
 
 	/**
 	 * The GridOberserver keeps track of changes in the grid.
 	 */
 	private SimulationRecorder gridObserver;
 
 	/**
 	 * Each index in the List stores the prototype snapshot associated with
 	 * that step in the simulation
 	 */
 	private HashMap<Integer, Map<String, PrototypeSnapshot>> prototypes;
 
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
 		table = new AgentSnapshotTable();
 		gridObserver = new SimulationRecorder(this);
 		prototypes = new HashMap<Integer, Map<String, PrototypeSnapshot>>();
 		lastStep = 0; 
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
 	public SimulationRecorder getGridObserver() {
 		return gridObserver;
 	}
 
 	/**
 	 * Add a PrototypeSnapshot to the StatisticsManager. 
 	 * @param prototypeSnapshot The new prototype being recorded. 
 	 */
 	public void addPrototypeSnapshot(PrototypeSnapshot prototypeSnapshot) { 
 		if (prototypeSnapshot.step > lastStep) 
 			lastStep = prototypeSnapshot.step; 
 		Map<String, PrototypeSnapshot> typeMap; 
 		if ((typeMap = prototypes.get(prototypeSnapshot.step)) != null) { 
 			typeMap.put(prototypeSnapshot.categoryName, prototypeSnapshot);
 		} else { 
 			typeMap = new TreeMap<String, PrototypeSnapshot>();
 			prototypes.put(new Integer(prototypeSnapshot.step), typeMap); 
 		}
 	}
 
 	/**
 	 * Store a snapshot of a gridEntity.
 	 * 
 	 * @param agentSnapshot
 	 *            The Snapshot to be stored.
 	 */
 	public void addGridEntity(AgentSnapshot agentSnapshot) {
 		table.putEntity(agentSnapshot);
 
 		if (agentSnapshot.step > lastStep)
 			lastStep = agentSnapshot.step;
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
 			String prototypeName, Integer step) {
 		ImmutableSet.Builder<AgentSnapshot> builder = new ImmutableSet.Builder<AgentSnapshot>();
 		ImmutableMap<AgentID, AgentSnapshot> totalPopulation = table
 				.getSnapshotsAtStep(step);
 		for (AgentID currentID : table.getSnapshotsAtStep(step).keySet()) {
 			AgentSnapshot currentEntity;
 			if ((currentEntity = totalPopulation.get(currentID)) != null) {
 				AgentSnapshot currentAgent = currentEntity;
 				if (currentAgent.prototypeName.equals(prototypeName))
 					builder.add(currentAgent);
 			}
 		}
 		return builder.build();
 	}
 
 	// TODO Fix documentation once testing is finished.
 	/**
 	 * Get data for a graph of the population of a certain GridEntity over time
 	 * 
 	 * @param id
 	 *            The PrototypeID of the GridEntity to be tracked
 	 * @return An array where indexes refer to the step in the simulation and
 	 *         the value refers to the population of the targeted entity at
 	 *         that time
 	 */
 	// TODO Make sure getPopVsTime is working correctly
 	public int[] getPopVsTime(String prototypeName) { // name - name of Prototype
 		int[] data = new int[lastStep+1];	
 		
 		//Populate agentsByStep
 		for (int i = 0; i <= lastStep; i++) {
 			Set<AgentSnapshot> stepPop = getPopulationAtStep(prototypeName, i);
 			data[i] = stepPop.size(); 
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
 	public double[] getAvgFieldValue(String prototypeName, String FieldName) {
 		// set of steps in table
 		Set<Integer> steps = table.getAllSteps();
 
 		// array of averages
 		double[] averages = new double[steps.size()];
 
 		// marker for double[]
 		int i = 0;
 
 		// arraylist of the values at each step to average up
 		ArrayList<Double> stepVals = new ArrayList<Double>();
 
 		for (int step : steps) {
 			ImmutableSet<AgentSnapshot> agents = getPopulationAtStep(prototypeName, step);
 
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
 	public double getAvgLifespan(String prototypeName) {
 		// List with index = step in the simulation, value = set of all agents
 		// alive at that time
 		List<Set<AgentSnapshot>> agentsByStep = new ArrayList<Set<AgentSnapshot>>();
 
 		//Set of all Agent IDs
 		Set<AgentID> allIDs = new HashSet<AgentID>(); 
 
 		//Populate agentsByStep
 		for (int i = 0; i <= lastStep; i++) {
 			Set<AgentSnapshot> stepData = getPopulationAtStep(prototypeName, i);
 
 			//Populate the list of unique Agent IDs for the Agents we're tracking
 			for(AgentSnapshot snap : stepData){
 				allIDs.add(snap.id); 
 			}
 
 			//Populate the list of agent snapshots by step
 			agentsByStep.add(i, stepData);
 		}
 
 		double avg = 0.0;
 
 		//Get the lifespan of each agent
 		for (AgentID ID : allIDs) {
 			int birthTime = getBirthStep(agentsByStep, ID);
 			int deathTime = getDeathStep(agentsByStep, ID);
 
 			// Build the sum of all lifetimes - we'll divide by the number of
 			// agents at the end to get the average
 			int lifespan = (deathTime - birthTime); 
 			avg += lifespan; 
 		}	
 
 		return avg / allIDs.size();
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
 			AgentID target) {
 		for (int i = 0; i < lastStep; i++){
 			for(AgentSnapshot s : agentsByStep.get(i)){
 				if(s.id.equals(target))
 					return i;
 			}
 		}
 
 		throw new IllegalArgumentException(
 				"The target AgentID was not found");
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
	private static int getDeathStep(List<Set<AgentSnapshot>> agentsByStep,
 			AgentID target) {
 		for (int i = agentsByStep.size()-1; i >= 0; i--){
 			if (agentsByStep.get(i) != null){
 				for(AgentSnapshot s : agentsByStep.get(i)){
 					if(s.id.equals(target))
 						return i;
 				}
 			}	
 		}
 
 		throw new IllegalArgumentException(
 				"The target AgentID was not found");
 	}
 }
