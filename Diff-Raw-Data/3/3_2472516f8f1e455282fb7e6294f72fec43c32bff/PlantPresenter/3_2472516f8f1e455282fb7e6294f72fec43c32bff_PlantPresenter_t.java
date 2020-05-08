 package simulator;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 public class PlantPresenter {
 
 	private Plant plant; 
 	
 	public PlantPresenter()
 	{
 		//Nothing
 	}
 	
 	/* ----------------		Methods	for UI to call	----------------
 	 * There is a method for each command that can be given by the
 	 * user. 
 	 */
 	
 	public void newGame(String operatorName) {
 		ReactorUtils utils = new ReactorUtils();
 		this.plant = utils.createNewPlant(operatorName);
 		printDebugInfo();
 	}
 	
 	//Returns true if saving a game was successful.
 	public boolean saveGame(){
 		// write serialised Plant to file?
 		return false;
 	}
 	
 	//Returns true if loading a game was successful.
 	public boolean loadGame() {
 		// read plant object from file
 		return false;
 	}
 	
 	public void togglePaused() {
 		this.plant.setPaused(!this.plant.isPaused());
 	}
 	
 	/**
 	 * Returns the highscores list.
 	 * @return list of highscores.
 	 */
 	public List<HighScore> getHighScores() {
 		return plant.getHighScores();
 	}
 	
 	/**
 	 * Returns true if command was successful, false if a valve with that ID was not found
 	 * @return true if command was successful, false if a valve with that ID was not found
 	 */
 	public boolean setValve(int valveID, boolean open) {
 		List<Valve> valves = plant.getValves();
 		for (Valve valve : valves) {
 			if (valveID == valve.getID()) {
 				valve.setOpen(open);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Returns true if command was successful, false if a pump with that ID was not found
 	 * @return true if command was successful, false if a pump with that ID was not found
 	 */
 	public boolean setPumpOnOff(int pumpID, boolean on) {
 		List<Pump> pumps = plant.getPumps();
 		for (Pump pump : pumps) {
 			if (pumpID == pump.getID()) {
 				pump.setOn(on);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean setPumpRpm(int pumpID, int rpm) throws IllegalArgumentException {
 		List<Pump> pumps = plant.getPumps();
 		for (Pump pump : pumps) {
 			if (pumpID == pump.getID()) {
 				pump.setRpm(rpm);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void setControlRods(int percentageLowered) {
 		if(percentageLowered >= 0 && percentageLowered <= 100) {
 			Reactor reactor = plant.getReactor();
 			reactor.setPercentageLowered(percentageLowered);
 		}
 	}
 	
 	/**
 	 * Start the repair of the turbine if it has failed.
 	 * @return true only if the turbine has failed and is not already being repaired.
 	 */
 	public boolean repairTurbine() {
 		Turbine turbine = plant.getTurbine();
 		List<PlantComponent> failedComponents = plant.getFailedComponents();
 		List<Repair> beingRepaired = plant.getBeingRepaired();
 		if (failedComponents.contains(turbine)) {
 			for (Repair br : beingRepaired) {
 				if (br.getPlantComponent() == turbine)
 					return false; //Turbine already being repaired
 			}
 			beingRepaired.add(new Repair(turbine));
 			return true; //Turbine has failed and is not being repaired (success)
 		}
 		return false; //Turbine has not failed
 	}
 	
 	public boolean repairPump(int pumpID) {
 		List<Pump> pumps = plant.getPumps();
 		Pump foundPump = null;
 		boolean found = false;
 		List<PlantComponent> failedComponents = plant.getFailedComponents();
 		List<Repair> beingRepaired = plant.getBeingRepaired();
 		for (Pump pump : pumps) { //Find the pump with the selected ID
 			if (pump.getID() == pumpID) {
 				foundPump = pump;
 				found = true;
 			}
 		}
 		if (found && failedComponents.contains(foundPump)) {
 			for (Repair br : beingRepaired) {
 				if (br.getPlantComponent() == foundPump) 
 					return false; //Pump already being repaired
 			}
 			beingRepaired.add(new Repair(foundPump));
 			return true; //Pump has failed and is not being repaired (success)
 		}
 		return false; //Pump not found or has not failed
 	}
 	
 	/**
 	 * Advance the game by a number of time steps.
 	 * 
 	 * @param numSteps number of timesteps to advance the game by.
 	 */
 	public void step(int numSteps) {
 		for (int i = 0; i < numSteps; i++) {
 			checkFailures();
 			updateBeingRepaired();
 			updateFlow();
 			updatePlant();
 		}
 		this.plant.updateTimeStepsUsed(numSteps);
 		printFlowDebugInfo();
 	}
 	
 	// ----------------		Methods used in systemText (TextUI class)	----------------
 	public UIData getUIData() {
 		UIData uidata = new UIData(plant);
 		return uidata;
 	}
 	
 	// ----------------		Debug methods	----------------
 	
 	private void printDebugInfo() {
 		System.out.println("--------------------------");
 		System.out.println("--   Time Step No.: " + this.plant.getTimeStepsUsed() + "\t--");
 		System.out.println("--        Reactor       --");
 		System.out.println("-- Health:\t" + this.plant.getReactor().getHealth() + "\t--");
 		System.out.println("-- Steam Vol:\t" + this.plant.getReactor().getSteamVolume() + "\t--");
 		System.out.println("-- Water Vol:\t" + this.plant.getReactor().getWaterVolume() + "\t--");
 		System.out.println("-- Temp:\t" + this.plant.getReactor().getTemperature() + "\t--");
 		System.out.println("-- Steam Flow:\t" + this.plant.getReactor().getFlowOut().getRate() + "\t--");
 		System.out.println("-- Steam Temp:\t" + this.plant.getReactor().getFlowOut().getTemperature() + "\t--");
 		System.out.println("--------------------------");
 		System.out.println("--       Condenser      --");
 		System.out.println("-- Health:\t" + this.plant.getCondenser().getHealth() + "\t--");
 		System.out.println("-- Steam Vol:\t" + this.plant.getCondenser().getSteamVolume() + "\t--");
 		System.out.println("-- Water Vol:\t" + this.plant.getCondenser().getWaterVolume() + "\t--");
 		System.out.println("-- Temp:\t" + this.plant.getCondenser().getTemperature() + "\t--");
 		System.out.println("-- Stm Flow In:\t" + this.plant.getCondenser().getInput().getFlowOut().getRate() + "\t--");
 		System.out.println("-- Stm Temp In:\t" + this.plant.getCondenser().getInput().getFlowOut().getTemperature() + "\t--");
 	}
 	
 	private void printFlowDebugInfo() {
 		for (PlantComponent pc : this.plant.getPlantComponents()) {
 			System.out.println("-----");
 			System.out.println(pc.getClass().toString());
 			System.out.println("\tFlow Out:" + pc.getFlowOut().getRate());
 			System.out.println("\tTemp Out:" + pc.getFlowOut().getTemperature());
 		}
 			
 	}
 	
 	// ------------		Update Plant Flow Methods	------------
 
 	// Go through all components and call updateState()
 	// This will do things in Reactor and Condenser objects etc.
 	private void updatePlant() {
 		List<PlantComponent> plantComponents = plant.getPlantComponents();
 		for (PlantComponent plantComponent : plantComponents) {
 			plantComponent.updateState();
 		}
 		plant.calcScore();
 	}
 	
 	//	private void startRepairing(PlantComponent toBeRepairedComponent) {
 	//		List<PlantComponent> failedComponents = plant.getFailedComponents(); 
 	//		List<Repair> beingRepairedComponents = plant.getBeingRepaired();
 	//		if (failedComponents.contains(toBeRepairedComponent)) {
 	//			Repair repair = new Repair(toBeRepairedComponent);
 	//			beingRepairedComponents.add(repair);
 	//		}
 	//	}
 	
 	private void updateBeingRepaired() {
 		List<Repair> beingRepaired = plant.getBeingRepaired();
 		List<Repair> finishedRepairing = new ArrayList<Repair>();
 		List<PlantComponent> failedComponents = plant.getFailedComponents();
 		for (Repair repair : beingRepaired) {
 			repair.decTimeStepsRemaining();
 			int timeStepsRemaining = repair.getTimeStepsRemaining();
 			if(timeStepsRemaining <= 0) {
 				finishedRepairing.add(repair);
 			}
 		}
 		for (Repair finished : finishedRepairing) {
 			failedComponents.remove(finished.getPlantComponent());
 			finished.getPlantComponent().setOperational(true);
 			beingRepaired.remove(finished);
 		}
 	}
 	
 	private void checkFailures() {
 		List<PlantComponent> plantComponents  = plant.getPlantComponents();
 		List<PlantComponent> failingComponents = new ArrayList<PlantComponent>();
 		int faults = 0;
 		
 		//Checks all components if they randomly fail
 		for (PlantComponent component : plantComponents) {
 			if (component.checkFailure()) {
 				if (component instanceof Reactor || component instanceof Condenser) {
 					//GAME OVER
 				}
 				else {
 					failingComponents.add(component);
 					faults++;
 					System.out.println("faults++");
 				}
 			}
 		}
 		
 		//Picks only one of all randomly failing components.
 		if(faults > 0) {
 			Random random = new Random();
 			int selection = random.nextInt(faults);
 			PlantComponent failedComponent = failingComponents.get(selection);
 			plant.addFailedComponent(failedComponent);
 			failedComponent.setOperational(false);
 		}
 	}
 	
 	private void updateFlow() {
 		setAllConnectorPipesUnblocked();
 		blockFromValves();
 		blockFromConnectorPipes();
 		resetFlowAllComponents();
 		
 		propagateFlowFromReactor(); // Start propagation of steam flow.
 		propagateFlowFromPumpsToCondenser(); // Total up all pump flows at condenser
 		propagateFlowFromCondenser();	// Start propagation of water flow.
 		propagateFlowFromConnectorPipes();
 		propagateNoFlowBackToReactor(); // Incase all paths out are blocked!
 		moveSteam();
 		moveWater(); 
 	}
 	
 	private void moveWater()
 	{
 		Condenser condenser = this.plant.getCondenser();
 		condenser.updateWaterVolume(-condenser.getFlowOut().getRate());
 		Reactor reactor = this.plant.getReactor();
 		reactor.updateWaterVolume(reactor.getInput().getFlowOut().getRate());
 	}
 
 	/**
 	 * Forcefully removes steam from the reactor and places it into the condenser.
 	 * Based upon the flow! :) 
 	 */
 	private void moveSteam()
 	{
 		Reactor reactor = this.plant.getReactor();
 		reactor.updateSteamVolume(-reactor.getFlowOut().getRate());
 		Condenser condenser = this.plant.getCondenser();
 		condenser.updateSteamVolume(condenser.getInput().getFlowOut().getRate());
 	}
 
 	/**
 	 * If all paths out of the reactor are blocked and the reactor is 
 	 * connected to a ConnectorPipe, then the 'zero flow' will not have been
 	 * propagated back to it's flowOut. We therefore need to check whether or not 
 	 */
 	private void propagateNoFlowBackToReactor()
 	{
 		Reactor reactor = this.plant.getReactor();
 		PlantComponent nextComponent = reactor.getOutput();
 		int	nextComponentFlowRate = nextComponent.getFlowOut().getRate();
 		// If the next component is a ConnectorPipe we need to
 		// remember that the flowOut is divided by the number of active
 		// outputs!
 		if (nextComponent instanceof ConnectorPipe) 
 			nextComponentFlowRate *= ((ConnectorPipe) nextComponent).numOutputs();
 		// If the rate of flow out of the first component is zero, propagate this back to the reactor.
 		if (nextComponentFlowRate == 0) reactor.getFlowOut().setRate(nextComponentFlowRate);
 	}
 
 	/**
 	 * Resets all ConnectorPipe paths to unblocked.
 	 * We do this to all ConnectorPipes at the beginning of each updatePlant()
 	 * before propagating the blockages since valves can change state between 
 	 * steps.
 	 */
 	private void setAllConnectorPipesUnblocked() {
 		for (ConnectorPipe cp : this.plant.getConnectorPipes()) {
 			cp.resetState();
 		}
 	}
 	
 	/**
 	 * Iterates through all valves in the system and if they are closed we
 	 * propagate the blockage through to the next preceding ConnectorPipe.
 	 */
 	private void blockFromValves() {
 		List<Valve> valves = this.plant.getValves();
 		for (Valve v : valves) {
 			if (!v.isOpen()) blockToPrecedingConnectorPipe(v);
 		}
 	}
 	
 	/**
 	 * Iterates through all ConnectorPipes in the system and propagates the blockage,
 	 * if all outputs of that ConnectorPipe is blocked.
 	 * 
 	 * This is done until all blocked ConnectorPipes have had their blockage propagated.
 	 */
 	private void blockFromConnectorPipes() {
 		boolean changed = true;
 		List<ConnectorPipe> connectorPipes = this.plant.getConnectorPipes();
 		Map<ConnectorPipe, Boolean> hasBeenPropagated = new HashMap<ConnectorPipe, Boolean>();
 		while (changed) {
 			changed = false;
 			// iterate through all connector pipes and check if they're blocked up.
 			for (ConnectorPipe c : connectorPipes) {
 				// If we're not already keeping track of c, add it to the hashmap
 				if (!hasBeenPropagated.containsKey(c)) hasBeenPropagated.put(c, false);
 				// If connectorPipe has all of it's outputs blocked
 				// And the blockage hasn't been propagated
 				if (isConnectorBlocking(c) && !hasBeenPropagated.get(c)) {
 					// Block the path leading into it.
 					blockPrecedingFromConnectorPipe(c);
 					hasBeenPropagated.put(c, true);
 					changed = true;
 				}
 			}
 		}
 	}
 	
 	
 	/**
 	 * Returns true if all outputs of a ConnectorPipe are blocked.
 	 * @return true if all outputs of a ConnectorPipe are blocked.
 	 */
 	private boolean isConnectorBlocking(ConnectorPipe cp) {
 		for(Boolean blocked : cp.getOutputsMap().values()) {	
 			if (!blocked) return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Traces back to the first occurring connector pipe and blocks the path out leading 
 	 * to blockedComponent.
 	 * We assume checks have been made to ensure blockedComponent is actually blocked.
 	 * 
 	 * @param blockedComponent component to start from.
 	 */
 	private void blockToPrecedingConnectorPipe(PlantComponent blockedComponent) {
 		PlantComponent currentComponent = blockedComponent.getInput();
 		PlantComponent prevComponent = blockedComponent;
 		boolean doneBlocking = false;
 		while (!doneBlocking) {
 			if (currentComponent instanceof ConnectorPipe) {
 				((ConnectorPipe) currentComponent).setComponentBlocked(prevComponent);
 				doneBlocking = true;
 			} else if (currentComponent instanceof Reactor) {
 				// No need to do anything here, just stop iterating.
 				doneBlocking = true;
 			} else {
 				prevComponent = currentComponent;
 				currentComponent = currentComponent.getInput();
 			}
 		}
 	}
 	
 	/**
 	 * Calls blockPrecedingConnectorPipe() for all input paths into blockedConnector. 
 	 * We assume checks have been made to ensure blockedConnector is actually blocked.
 	 * 
 	 * If an input is a ConnectorPipe, recursively call this function to make sure 
 	 * all blockages are properly propagated. 
 	 * 
 	 * @param blockedConnector the blocked ConnectorPipe to start from.
 	 */
 	private void blockPrecedingFromConnectorPipe(ConnectorPipe blockedConnector) {
 		List<PlantComponent> multipleInputs = ((ConnectorPipe) blockedConnector).getInputs();
 		for (PlantComponent pc : multipleInputs) {
 			if (pc instanceof ConnectorPipe) {
 				blockPrecedingFromConnectorPipe((ConnectorPipe) pc);
 			} else {
 				if (pc != null) blockToPrecedingConnectorPipe(pc);
 			}
 		}
 	}
 	
 	/**
 	 * Resets the flow of all components back ready for the flow around the system to be
 	 * recalculated for the current state of the plant.
 	 */
 	private void resetFlowAllComponents() {
 		for (PlantComponent pc : this.plant.getPlantComponents()) {
 			pc.getFlowOut().setRate(0);
 			pc.getFlowOut().setTemperature(0);
 		}
 	}
 	
 	/**
 	 * Start off propagation of the flow from the reactor to the next 
 	 * ConnectorPipe encountered.
 	 */
 	private void propagateFlowFromReactor()
 	{
 		int flowRate = calcReactorFlowOut();
 		Reactor reactor = this.plant.getReactor();
 		reactor.getFlowOut().setRate(flowRate);
 		reactor.getFlowOut().setTemperature(reactor.getTemperature());
 		propagateFlowToNextConnectorPipe(reactor);
 	}
 	
 	/**
 	 * Calculate and return the flow of steam out of the reactor due to the difference in
 	 * steam volume between the reactor and condenser.
 	 * 
 	 * This method ignores any blockages, these are dealt with when the flow is propagated
 	 * around the system.
 	 *  
 	 * @return rate of flow of steam out of the reactor.
 	 */
 	private int calcReactorFlowOut() {
 		Reactor reactor = this.plant.getReactor();
 		Condenser condenser = this.plant.getCondenser();
 		int steamDiff = Math.abs(reactor.getSteamVolume() - condenser.getSteamVolume());
 		int flowRate;
 		if (steamDiff > this.plant.getMaxSteamFlowRate()) {
 			flowRate = this.plant.getMaxSteamFlowRate();
 		} else {
 			flowRate = steamDiff;
 		}
 		return flowRate;
 	}
 	
 	/**
 	 * Iterates through connector pipes, calculates their flow out & if it has changed,
 	 * propagate this new flow forward to the next connector pipe.
 	 * Do this until nothing in the system changes 
 	 * (Inspired by bubble sort's changed flag... "Good Ol' Bubble Sort!")
 	 */
 	private void propagateFlowFromConnectorPipes()
 	{
 		boolean changed = true;
 		int oldRate;
 		List<ConnectorPipe> connectorPipes = this.plant.getConnectorPipes();
 		while (changed) {
 			changed = false;
 			// iterate through all connector pipes and update their rate.
 			for (ConnectorPipe c : connectorPipes) {
 				oldRate = c.getFlowOut().getRate();
 				calcConnectorFlowOut(c);
 				if (oldRate != c.getFlowOut().getRate()) {
 					propagateFlowFromConnectorPipe(c);
 					changed = true;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Propagates the flow rate and temperature to every component from startComponent
 	 * until a ConnectorPipe is encountered.
 	 * 
 	 * @param startComponent Component to start the propagation from.
 	 */
 	private void propagateFlowToNextConnectorPipe(PlantComponent startComponent) {
 		PlantComponent prevComponent;
 		// If startComponent.isPressurised() (=> it is a reactor or condenser) start from here, not its input. 
 		prevComponent = (startComponent.isPressurised()) ? startComponent : startComponent.getInput();
 		PlantComponent currComponent = startComponent;
 		boolean donePropagating = false;
 		while (!donePropagating) {
 			if (currComponent instanceof ConnectorPipe) {
 				donePropagating = true;
 			} else if (currComponent instanceof Condenser) {
 				donePropagating = true;
 			} else {
 				currComponent.getFlowOut().setRate(prevComponent.getFlowOut().getRate());
 				currComponent.getFlowOut().setTemperature(prevComponent.getFlowOut().getTemperature());
 				prevComponent = currComponent;
 				currComponent = currComponent.getOutput();
 			}
 		}
 	}
 	
 	private void propagateFlowFromConnectorPipe(ConnectorPipe startConnectorPipe) {
 		Map<PlantComponent, Boolean> outputs = startConnectorPipe.getOutputsMap();
 		for (PlantComponent pc : outputs.keySet()) {
 			if (!outputs.get(pc)) {
 				if (pc instanceof ConnectorPipe) {
 					propagateFlowFromConnectorPipe((ConnectorPipe) pc);
 				} else {
 					propagateFlowToNextConnectorPipe(pc);
 				}
 			}
 		}
 	}
 	
 	
 	/**
 	 * Update the Flow out of a connector to reflect it's inputs and outputs.
 	 * @param connector the connector to update.
 	 */
 	private void calcConnectorFlowOut(ConnectorPipe connector) {
 		ArrayList<PlantComponent> inputs = connector.getInputs();
 		int totalFlow = 0;
 		int avgTemp = 0;
 		int numOutputs = connector.numOutputs();
 		int numInputs = 0;
 		for (PlantComponent input : inputs) {
 			if (input != null) {
 				totalFlow += input.getFlowOut().getRate();
 				avgTemp += input.getFlowOut().getTemperature();
 				numInputs++;
 			}
 		}
 		totalFlow = (numOutputs != 0) ? totalFlow / numOutputs : 0; // average the flow across all active outputs.
 		avgTemp = (numInputs != 0) ? avgTemp / numInputs : 0;
 		connector.getFlowOut().setRate(totalFlow);
 		connector.getFlowOut().setTemperature(avgTemp);
 	}
 	
 	private void propagateFlowFromCondenser()
 	{
 		Condenser condenser = this.plant.getCondenser();
 		condenser.getFlowOut().setTemperature(condenser.getTemperature());
 		propagateFlowToNextConnectorPipe(condenser);
 	}
 
 	/**
 	 * Tracks back from a pump and if there is a clear path to the condenser
 	 * adds the flow increase at this pump to the flow out of the condenser.
 	 * 
 	 * This method does not support multiple condensers.
 	 */
 	private void propagateFlowFromPumpsToCondenser()
 	{
 		int flowRate;
 		PlantComponent currentComponent;
 		PlantComponent precedingComponent;
 		boolean blockedPath = false;
 		// Iterate through all pumps and start tracking back through the system
 		for (Pump p : this.plant.getPumps()) {
 			flowRate = calcFlowFromPumpRpm(p);
 			blockedPath = false;
 			currentComponent = p;
 			precedingComponent = p.getInput();
 			// Until we find the condenser or our path is blocked
 			while(!(precedingComponent instanceof Condenser) || blockedPath) {
 				if (!(precedingComponent instanceof ConnectorPipe)) {
 					// Nothing to see here... Move along (to the next component ;)
 					currentComponent = precedingComponent;
 					precedingComponent = precedingComponent.getInput();
 				} else {
 					// Check if the path we've come in from at this connector pipe is blocked
 					if (((ConnectorPipe) precedingComponent).getOutputsMap().get(currentComponent)) {
 						// YOU SHALL NOT PASS!
 						blockedPath = true;
 					} else {
 						currentComponent = precedingComponent;
 						List<PlantComponent> possiblePaths = ((ConnectorPipe) precedingComponent).getInputs();
 						if (possiblePaths.size() > 1) { 
 							/* There is more than one possible path..
 							 * Luckily we know our system will only ever have one so we
 							 * can get away with this hard coded hackery...
 							 * 
 							 * You will need to recursively trace all paths and remember where you've
 							 * been until you find the reactor, should you require strange paths through
 							 * the system. If you do... enjoy!
 							 */
 							precedingComponent = possiblePaths.get(0);
 						} else {
 							precedingComponent = possiblePaths.get(0);
 						}
 					}
 				}
 			}
 			/* If we did indeed find the condenser then add the flow increase from this pump to
 			 * the flow out of the condenser.
 			 */
 			if (precedingComponent instanceof Condenser) 
 				precedingComponent.getFlowOut().setRate(precedingComponent.getFlowOut().getRate() + flowRate);
 		}
 	}
 
 	private int calcFlowFromPumpRpm(Pump p)
 	{
 		int maxRpm = p.getMaxRpm();
		return (int) Math.round(this.plant.getMaxWaterFlowRatePerPump() * (1 - (new Double((maxRpm - p.getRpm())/new Double(maxRpm)))));
 	}
 	
 }
