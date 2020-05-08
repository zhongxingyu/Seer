 /**
  * Simulator.java
  * 
  * Runnable simulator that in a way acts as a facade to the Agent code. 
  * The simulator holds simulations.
  *
  * @author Agent Team
  */
 
 package edu.wheaton.simulator.simulation;
 
 import java.awt.Color;
 import java.io.File;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import net.sourceforge.jeval.EvaluationException;
 import sampleAgents.Bouncer;
 import sampleAgents.Confuser;
 import sampleAgents.ConwaysAliveBeing;
 import sampleAgents.ConwaysDeadBeing;
 import sampleAgents.Multiplier;
 import sampleAgents.Paper;
 import sampleAgents.RightTurner;
 import sampleAgents.Rock;
 import sampleAgents.Scissors;
 
 import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.datastructure.Field;
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.datastructure.GridObserver;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.simulation.end.SimulationEnder;
 import edu.wheaton.simulator.statistics.Loader;
 import edu.wheaton.simulator.statistics.Saver;
 
 public class Simulator {
 
 	/**
 	 * Single instance of the simulator
 	 */
 	private static Simulator simulator = null;
 
 	/**
 	 * Houses the properties of a simulation
 	 */
 	private Simulation simulation;
 
 	/**
 	 * Whether or not the simulation will pause on the next step
 	 */
 	private AtomicBoolean isPaused;
 
 	/**
 	 * If the simulation has ended
 	 */
 	private AtomicBoolean isStopped;
 
 	/**
 	 * Time (in milliseconds) in between each step
 	 */
 	private int sleepPeriod;
 
 	/**
 	 * If a layer is being displayed
 	 */
 	private AtomicBoolean layerRunning;
 
 	/**
 	 * Monitor for sync
 	 */
 	private static final Object lock = new Object();
 
 	/**
 	 * Constructor
 	 */
 	private Simulator() {
 		isPaused = new AtomicBoolean(false);
 		isStopped = new AtomicBoolean(false);
 		layerRunning = new AtomicBoolean(false);
 		sleepPeriod = 500;
 		layerRunning.set(false);
 	}
 
 	/**
 	 * Get the instance of the simulator
 	 * 
 	 * @return
 	 */
 	public static Simulator getInstance() {
 		if (simulator == null)
 			simulator = new Simulator();
 		return simulator;
 	}
 
 	/**
 	 * Runs the simulation by updating all the entities
 	 */
 	public final Thread mainThread = new Thread(new Runnable() {
 		@Override
 		public void run() {
 			while (!isStopped.get()) {
 				while (!isPaused.get()) {
 					try {
 						simulation.updateEntities();
 						simulation.notifyObservers(layerRunning.get());
 						if (layerRunning.get())
 							setLayerExtremes();
 						Thread.sleep(sleepPeriod);
 					} catch (SimulationPauseException e) {
 						isPaused.set(true);
 						System.err.println(e.getMessage());
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 					checkEndings();
 				}
 				synchronized (lock) {
 					try {
 						lock.wait();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 						Thread.currentThread().interrupt();
 						return;
 					}
 				}
 			}
 		}
 	});
 
 	/**
 	 * Resumes the simulation
 	 */
 	public void play() {
		if (simulation.getStarted()) {
 			simulation.setStarted();
 			mainThread.start();
 		} else if (!isStopped.get() && isPaused.get()) {
 			isPaused.set(false);
 			synchronized (lock) {
 				lock.notifyAll();
 			}
 		}
 	}
 
 	/**
 	 * Pauses the update of the simulation. This will happen on the next
 	 * iteration.
 	 */
 	public void pause() {
 		isPaused.set(true);
 	}
 
 	/**
 	 * Tells the grid to stop on the next iteration if the ender evaluates to
 	 * true
 	 */
 	private void checkEndings() {
 		if (simulation.shouldEnd()) {
 			isPaused.set(true);
 			isStopped.set(true);
 		}
 	}
 
 	/**
 	 * Changes how long the simulation waits after each step
 	 * 
 	 * @param sleepPeriod
 	 *            Time in milliseconds
 	 */
 	public void setSleepPeriod(int sleepPeriod) {
 		this.sleepPeriod = sleepPeriod;
 	}
 
 	/**
 	 * Provides the time (in milliseconds) the simulator waits after each step
 	 * 
 	 * @return
 	 */
 	public int getSleepPeriod() {
 		return sleepPeriod;
 	}
 
 	/**
 	 * Adds a Prototype to the prototype HashMap
 	 * 
 	 * @param n
 	 * @param g
 	 * @param c
 	 */
 	public static void createPrototype(String n, Color c) {
 		Prototype.addPrototype(new Prototype(c, n));
 	}
 
 	/**
 	 * Adds a Prototype (with a design) to the HashMap
 	 * 
 	 * @param n
 	 * @param g
 	 * @param c
 	 * @param d
 	 */
 	public static void createPrototype(String n, Color c, byte[] d) {
 		Prototype.addPrototype(new Prototype(c, d, n));
 	}
 
 	/**
 	 * Returns the Prototype that corresponds to the given string.
 	 * 
 	 * @param n
 	 * @return
 	 */
 	public static Prototype getPrototype(String n) {
 		return Prototype.getPrototype(n);
 	}
 
 	/**
 	 * Resets the static list of prototypes
 	 */
 	public static void clearPrototypes() {
 		Prototype.clearPrototypes();
 	}
 
 	/**
 	 * Gets a Set of the prototype names
 	 * 
 	 * @return
 	 */
 	public static Set<String> prototypeNames() {
 		return Prototype.prototypeNames();
 	}
 
 	/**
 	 * Provides the Grid object that the simulation houses
 	 * 
 	 * @return
 	 */
 	private Grid simulationGrid() {
 		return simulation.getGrid();
 	}
 
 	/**
 	 * 
 	 * @return a String with the name of the current update method
 	 */
 	public String currentUpdater() {
 		return simulationGrid().currentUpdater();
 	}
 
 	/**
 	 * Sets the update method to use the PriorityUpdate system
 	 */
 	public void setPriorityUpdate(int minPriority, int maxPriority) {
 		simulationGrid().setPriorityUpdater(minPriority, maxPriority);
 	}
 
 	/**
 	 * Sets the update method to use the AtomicUpdate system
 	 */
 	public void setAtomicUpdate() {
 		simulationGrid().setAtomicUpdater();
 	}
 
 	/**
 	 * Sets the update method to use the LinearUpdate system LinearUpdate is the
 	 * default
 	 */
 	public void setLinearUpdate() {
 		simulationGrid().setLinearUpdater();
 	}
 
 	/**
 	 * Places an new agent (that follows the given prototype) at the given
 	 * coordinates. This method replaces (kills) anything that is currently in
 	 * that position. The Agent's own position is also updated accordingly.
 	 * 
 	 * @param prototypeName
 	 * @param x
 	 * @param y
 	 * @return false if the x/y values are invalid
 	 */
 	public boolean addAgent(String prototypeName, int x, int y) {
 		Agent toAdd = getPrototype(prototypeName).createAgent(simulationGrid());
 		boolean toReturn = simulationGrid().addAgent(toAdd, x, y);
 		simulation.notifyObservers(layerRunning.get());
 		return toReturn;
 	}
 
 	/**
 	 * Places an new agent (that follows the given prototype) at a random
 	 * position in the grid. This method replaces (kills) anything that is
 	 * currently in that position. The Agent's own position is also updated
 	 * accordingly.
 	 * 
 	 * @param prototypeName
 	 * @return returns true if successful
 	 */
 	public boolean addAgent(String prototypeName) {
 		Agent toAdd = getPrototype(prototypeName).createAgent(simulationGrid());
 		boolean toReturn = simulationGrid().addAgent(toAdd);
 		simulation.notifyObservers(layerRunning.get());
 		return toReturn;
 	}
 
 	/**
 	 * Fills the entire grid with agents that follow the given prototype name
 	 * 
 	 * @param prototypeName
 	 */
 	public void fillAll(String prototypeName) {
 		for(int x = 0; x < getWidth(); x++) 
 			for(int y = 0; y < getHeight(); y++)
 				addAgent(prototypeName, x, y);
 		simulation.notifyObservers(layerRunning.get());
 	}
 	
 	/**
 	 * Clears all the Agents from the grid
 	 * 
 	 * @param a
 	 */
 	public void clearAll(String prototypeName) {
 		for(int x = 0; x < getWidth(); x++) 
 			for(int y = 0; y < getHeight(); y++)
 				removeAgent(x, y);
 		simulation.notifyObservers(layerRunning.get());
 	}
 	
 	/**
 	 * Returns the Agent at the given coordinates
 	 * 
 	 * @param x
 	 * @param y
 	 */
 	public Agent getAgent(int x, int y) {
 		return simulationGrid().getAgent(x, y);
 	}
 
 	/**
 	 * Removes an Agent at the given coordinates
 	 * 
 	 * @param x
 	 * @param y
 	 */
 	public void removeAgent(int x, int y) {
 		simulationGrid().removeAgent(x, y);
 		simulation.notifyObservers(layerRunning.get());
 	}
 
 	/**
 	 * Provides the simulation's name
 	 * 
 	 * @return
 	 */
 	public String getName() {
 		return simulation.getName();
 	}
 
 	/**
 	 * Change the name of the simulation
 	 */
 	public void setName(String name) {
 		simulation.setName(name);
 	}
 
 	/**
 	 * Makes a new Layer.
 	 * 
 	 * @param fieldName
 	 *            The name of the Field that the Layer will represent
 	 * @param c
 	 *            The Color that will be shaded differently to represent Field
 	 *            values
 	 */
 	public void displayLayer(String fieldName, Color c) {
 		layerRunning.set(true);
 		Layer.getInstance().setFieldName(fieldName);
 		Layer.getInstance().setColor(c);
 		Layer.getInstance().resetMinMax();
 		simulation.notifyObservers(layerRunning.get());
 	}
 
 	/**
 	 * Stops the layer from displaying
 	 */
 	public void clearLayer() {
 		layerRunning.set(false);
 		simulation.notifyObservers(layerRunning.get());
 	}
 
 	/**
 	 * Resets the min/max values of the layer and then loops through the grid to
 	 * set's a new Layer's min/max values. This must be done before a Layer is
 	 * shown. Usually every step if the Layer is being displayed. PRECONDITION:
 	 * The newLayer method has been called to setup a layer
 	 * 
 	 * @throws EvaluationException
 	 */
 	private void setLayerExtremes() {
 		Layer.getInstance().resetMinMax();
 		for (Agent current : simulationGrid()) {
 			if (current != null) {
 				Field currentField = current.getField(Layer.getInstance()
 						.getFieldName());
 				Layer.getInstance().setExtremes(currentField);
 			}
 		}
 	}
 
 	/**
 	 * Adds the some sample prototypes
 	 */
 	public void initSamples() {
 		new Multiplier().initSampleAgent(new Prototype(Color.BLUE, "Multiplier"));
 		new Bouncer().initSampleAgent(new Prototype(Color.RED, "bouncer"));
 		new RightTurner().initSampleAgent(new Prototype(Color.BLACK, "rightTurner"));
 		new Confuser().initSampleAgent(new Prototype(Color.GREEN, "confuser"));
 	}
 
 	/**
 	 * Sample simulation: Conway's Game of Life
 	 */
 	public void initGameOfLife() {
 		clearPrototypes();
 		simulationGrid().setPriorityUpdater(0, 50);
 
 		// add prototypes
 		new ConwaysDeadBeing().initSampleAgent(new Prototype(new Color(219, 219, 219), "deadBeing"));
 		new ConwaysAliveBeing().initSampleAgent(new Prototype(new Color(93, 198, 245), "aliveBeing"));
 
 		// Place dead beings in Grid with some that are alive
 		for (int x = 0; x < simulationGrid().getWidth(); x++)
 			for (int y = 0; y < simulationGrid().getHeight(); y++) {
 				if (x == simulationGrid().getWidth() / 2) {
 					simulationGrid().addAgent(
 							Prototype.getPrototype("aliveBeing").createAgent(simulationGrid()),
 							x, y);
 				} else {
 					simulationGrid().addAgent(
 							Prototype.getPrototype("deadBeing").createAgent(simulationGrid()),
 							x, y);
 				}
 			}
 		simulation.notifyObservers(layerRunning.get());
 	}
 
 	/**
 	 * Sets up the rock paper and scissors sample units
 	 */
 	public void initRockPaperScissors() {
 		setPriorityUpdate(0, 60);
 		new Rock().initSampleAgent(new Prototype("rock"));
 		new Paper()
 		.initSampleAgent(new Prototype("paper"));
 		new Scissors().initSampleAgent(new Prototype("scissors"));
 		simulation.notifyObservers(layerRunning.get());
 	}
 
 	/**
 	 * Gets a field with the given string. Simple wrapper function.
 	 * 
 	 * @param s
 	 *            The name of the field.
 	 * @return The field to return.
 	 */
 	public Field getGlobalField(String s) {
 		return simulationGrid().getField(s);
 	}
 
 	/**
 	 * Gets a map holding all values for the global fields.
 	 * 
 	 * @return A map holding all values for the global fields.
 	 */
 	public Map<String, String> getGlobalFieldMap() {
 		return simulationGrid().getFieldMap();
 	}
 
 	/**
 	 * Updates the field with the given name to the given value.
 	 * 
 	 * @param name
 	 *            The name of the field to update.
 	 * @param value
 	 *            The new value of the field.
 	 */
 	public void updateGlobalField(String name, String value) {
 		try {
 			simulationGrid().updateField(name, value);
 		} catch (NoSuchElementException e) {
 			System.out
 			.println("Attempting to update a nonexistent global field.");
 			System.err.print(e);
 		}
 	}
 
 	/**
 	 * Adds a global field to the simulation.
 	 * 
 	 * @param name
 	 * @param startingValue
 	 */
 	public void addGlobalField(String name, String startingValue) {
 		try {
 			simulationGrid().addField(name, startingValue);
 		} catch (ElementAlreadyContainedException e) {
 			System.out
 			.println("Problem adding a global field. Name already in the map. Exiting.");
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 
 	/**
 	 * Removes the global field with the given name.
 	 * 
 	 * @param name
 	 *            The name of the field to remove.
 	 */
 	public void removeGlobalField(String name) {
 		try {
 			simulationGrid().removeField(name);
 		} catch (NoSuchElementException e) {
 			System.out.println("Attempting to remove a nonexistant field.");
 			System.err.print(e);
 		}
 	}
 
 	/**
 	 * Provides the width of the grid
 	 */
 	public int getWidth() {
 		return simulationGrid().getWidth();
 	}
 
 	/**
 	 * Provides the height of the grid
 	 */
 	public int getHeight() {
 		return simulationGrid().getHeight();
 	}
 
 	/**
 	 * Changes the size of the grid
 	 * 
 	 * @param width
 	 * @param height
 	 */
 	public void resizeGrid(int width, int height) {
 		simulationGrid().resizeGrid(width, height);
 	}
 
 	/**
 	 * Adds the given observer to the grid
 	 */
 	public void addGridObserver(GridObserver ob) {
 		simulationGrid().addObserver(ob);
 	}
 
 	/**
 	 * Creates a new simulation with a blank grid
 	 * 
 	 * @param name
 	 * @param width
 	 * @param height
 	 * @param se
 	 */
 	public void load(String name, int width, int height, SimulationEnder se) {
 		simulation = new Simulation(name, width, height, se);
 	}
 	
 	/**
 	 * Replicates the simulation from the given file
 	 * 
 	 * @param file
 	 */
 	public void loadFromString(File file){
 		Loader l = new Loader();
 		l.loadSimulation(file);
 		try{
 			load(l.getName(), l.getGrid(), l.getPrototypes(), l.getSimEnder());
 		}catch(Exception e){
 			System.out.println("No grid has been loaded yet!"); 
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Loads a simulation from a grid and prototypes
 	 * 
 	 * @param name
 	 * @param grid
 	 * @param prototypes
 	 * @param se
 	 */
 	private void load(String name, Grid grid, Set<Prototype> prototypes,
 			SimulationEnder se) {
 		simulation = new Simulation(name, grid, se);
 		for (Prototype current : prototypes)
 			Prototype.addPrototype(current);
 	}
 	
 	/**
 	 * Load a prototype from a file and add it to the simulation
 	 *
 	 * @param file
 	 */
 	public void loadPrototypeFromString(File file){
 		Loader l = new Loader();
 		Prototype.addPrototype(l.loadPrototype(file));
 	}
 
 	/**
 	 * Saves a simulation to a given file.
 	 *
 	 * @param filename
 	 */
 	public void saveToString(String filename, SimulationEnder ender){
 		Saver s = new Saver();
 		Set<Agent> agents = new HashSet<Agent>();
 		Grid grid = simulationGrid();
 		for (Agent agent : grid)
 			if (agent != null)
 				agents.add(agent);
 
 		s.saveSimulation(filename, agents, Prototype.getPrototypes(),
 				getGlobalFieldMap(),
 				grid.getWidth(), grid.getHeight(), ender);
 	}
 	
 	
 	
 	/**
 	 * Saves a given prototype to a string representation and put into file
 	 *
 	 * @param proto
 	 */
 	public void savePrototypeToString(Prototype proto){
 		Saver s = new Saver();
 		s.savePrototype(proto);
 	}
 
 }
