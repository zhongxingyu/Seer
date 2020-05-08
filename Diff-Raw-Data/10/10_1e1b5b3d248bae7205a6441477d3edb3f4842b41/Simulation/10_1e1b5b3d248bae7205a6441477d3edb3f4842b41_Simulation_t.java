 /**
  * Simulation.java
  * 
  * A single directional class for simulation. Once the simulation 
  * is completed, a new simulation must be made.
  *
  * @author Agent Team
  */
 
 package edu.wheaton.simulator.simulation;
 
 import java.awt.Color;
 import java.io.File;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import net.sourceforge.jeval.EvaluationException;
 
 import edu.wheaton.simulator.datastructure.Field;
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.AgentID;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.simulation.end.SimulationEnder;
 import edu.wheaton.simulator.statistics.Loader;
 import edu.wheaton.simulator.statistics.StatisticsManager;
 
 public class Simulation {
 
 	/**
 	 * Name of the simulation
 	 */
 	private String name;
 
 	/**
 	 * The Grid to hold all the Agents
 	 */
 	private Grid grid;
 	
 	/**
 	 * Whether or not the simulation will pause on the next step
 	 */
 	private AtomicBoolean isPaused;
 
 	/**
 	 * If the simulation has ended
 	 */
 	private AtomicBoolean isStopped;
 	
 	/**
 	 * Whether or not the simulation has begun
 	 */
 	private AtomicBoolean isStarted;
 
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
 	 * Ending conditions
 	 */
 	
 	private SimulationEnder ender;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param name
 	 * @param gridX
 	 * @param gridY
 	 * @param se
 	 */
 	public Simulation(String name, int gridX, int gridY) {
 		this.name = name;
 		grid = new Grid(gridX, gridY);
 		isPaused = new AtomicBoolean(false);
 		isStopped = new AtomicBoolean(false);
 		isStarted = new AtomicBoolean(false);
 		layerRunning = new AtomicBoolean(false);
 		sleepPeriod = 500;
 		layerRunning.set(false);
 		StatisticsManager.getInstance().initialize(grid);
 		ender = SimulationEnder.getInstance();
 		loadPrototypesFromDirectory(new File("prototypes/"));
 		resetLayer();
 		AgentID.resetIDs();
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param name
 	 * @param grid
 	 * @param se
 	 */
 	public Simulation(String name, Grid grid) {
 		this.name = name;
 		this.grid = grid;
 		isPaused = new AtomicBoolean(false);
 		isStopped = new AtomicBoolean(false);
 		isStarted = new AtomicBoolean(false);
 		layerRunning = new AtomicBoolean(false);
 		sleepPeriod = 500;
 		layerRunning.set(false);
 		StatisticsManager.getInstance().initialize(grid);
 		ender = SimulationEnder.getInstance();
 		loadPrototypesFromDirectory(new File("prototypes/"));
 		resetLayer();
 		AgentID.resetIDs();
 	}
 	
 	/**
 	 * Runs the simulation by updating all the entities
 	 */
 	private final Thread mainThread = new Thread(new Runnable() {
 		@Override
 		public void run() {
 			while (!isStopped.get()) {
 				while (!isPaused.get()) {
 					try {
 						updateEntities();
 						if (layerRunning.get())
 							setLayerExtremes();
 						notifyDisplayObservers();
 						notifyStatsObservers();
 						if(sleepPeriod > 0)
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
 		if (!isStarted.get()) {
 			isStarted.set(true);
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
 	 * Causes all the triggers of all the entities in the simulator's grid to be
 	 * fired
 	 * 
 	 * @throws SimulationPauseException
 	 */
 	public void updateEntities() throws SimulationPauseException {
 		grid.updateEntities();
 	}
 
 	/**
 	 * Tells the loop to stop on the next iteration if the ender evaluates to
 	 * true
 	 */
 	private void checkEndings() {
 		if (ender.evaluate(grid)) {
 			isPaused.set(true);
 			isStopped.set(true);
 			notifyEndObservers();
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
 	 * Begins the field layer
 	 */
 	public void runLayer() {
 		layerRunning.set(true);
 	}
 	
 	/**
 	 * Stops the field layer from displaying
 	 */
 	public void stopLayer() {
 		layerRunning.set(false);
 	}
 	
 	/**
 	 * Resets the min/max values of the layer and then loops through the grid to
 	 * set's a new Layer's min/max values. This must be done before a Layer is
 	 * shown. Usually every step if the Layer is being displayed. PRECONDITION:
 	 * The newLayer method has been called to setup a layer
 	 * 
 	 * @throws EvaluationException
 	 */
 	public void setLayerExtremes() {
 		Layer.getInstance().resetMinMax();
 		for (Agent current : grid) {
 			if (current != null) {
 				Field currentField = current.getField(Layer.getInstance()
 						.getFieldName());
 				Layer.getInstance().setExtremes(currentField);
 			}
 		}
 	}
 	
 	/**
 	 * Clears settings in the Layer singleton
 	 */
 	public void resetLayer() {
 		Layer.getInstance().setFieldName("");
 		Layer.getInstance().setColor(Color.WHITE);
 		Layer.getInstance().resetMinMax();
 	}
 
 	/**
 	 * Notifies all of the GUI observers
 	 */
 	public void notifyDisplayObservers() {
 		grid.notifyDisplayObservers(layerRunning.get());
 	}
 	
 	/**
 	 * Notifies all of the stats observers
 	 */
 	public void notifyStatsObservers() {
 		grid.notifyStatsObservers();
 	}
 	
 	/**
 	 * Updates all of the observers watching for the end of the simulation
 	 */
 	public void notifyEndObservers() {
 		grid.notifyEndObservers(ender);
 	}
 	
 	/**
 	 * Load all prototypes from all prototype files in the directory
 	 * 
 	 * @param file that points to directory where prototype files are located
 	 */
	private static void loadPrototypesFromDirectory(File directory) {
 		Loader l = new Loader();
 		File[] protoFiles;
 		if (directory.isDirectory()) {
 			protoFiles = directory.listFiles();
 			for (File file : protoFiles) {
 				if (file.getName().contains(".agt")) {
 					Prototype.addPrototype(l.loadPrototype(file));
 				}
 				else continue;
 			}
 		}
 	}
 
 	/**
 	 * Provides this simulation's name
 	 * 
 	 * @return
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * Change the name of the simulation
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Provides the Grid the Facade is using
 	 * 
 	 * @return Grid object
 	 */
 	public Grid getGrid() {
 		return grid;
 	}
 }
