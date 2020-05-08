 package chalmers.dax021308.ecosystem.model.environment;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.jfree.text.G2TextMeasurer;
 
 import chalmers.dax021308.ecosystem.model.agent.IAgent;
 import chalmers.dax021308.ecosystem.model.population.AbstractPopulation;
 import chalmers.dax021308.ecosystem.model.population.DeerPopulation;
 import chalmers.dax021308.ecosystem.model.population.DummyPredatorPopulation;
 import chalmers.dax021308.ecosystem.model.population.DummyPreyPopulation;
 import chalmers.dax021308.ecosystem.model.population.PigPopulation;
 import chalmers.dax021308.ecosystem.model.population.WolfPopulation;
 import chalmers.dax021308.ecosystem.model.population.GrassPopulation;
 import chalmers.dax021308.ecosystem.model.population.IPopulation;
 import chalmers.dax021308.ecosystem.model.population.RabbitPopulation;
 import chalmers.dax021308.ecosystem.model.util.Log;
 import chalmers.dax021308.ecosystem.model.util.TimerHandler;
 
 /**
  * Ecosystem main class.
  * <p>
  * Recieves notifications from the {@link TimerHandler} and the
  * {@link IEnvironment}.
  * <p>
  * Use the proper constructor for the wanted behavior of EcoWorld.
  * 
  * @author Erik Ramqvist
  * 
  */
 public class EcoWorld {
 	
 	/* Property change events constants */
 	public static final String EVENT_TICK               = "chalmers.dax021308.ecosystem.model.Ecoworld.event_tick";
 	public static final String EVENT_STOP               = "chalmers.dax021308.ecosystem.model.Ecoworld.event_stop";
 	public static final String EVENT_START              = "chalmers.dax021308.ecosystem.model.Ecoworld.event_stop";
 	public static final String EVENT_PAUSE		        = "chalmers.dax021308.ecosystem.model.Ecoworld.event_pause";
 	public static final String EVENT_RECORDING_FINISHED = "chalmers.dax021308.ecosystem.model.Ecoworld.event_recordingstarted";
 	public static final String EVENT_RECORDING_STARTED  = "chalmers.dax021308.ecosystem.model.Ecoworld.event_recordingfinished";
 	/* 								   */
 	
 	private AtomicBoolean environmentFinished = new AtomicBoolean(false);
 	private AtomicBoolean timerFinished       = new AtomicBoolean(false);
 	private AtomicBoolean shouldRun           = new AtomicBoolean(false);
 	private boolean runWithoutTimer;
 	private boolean recordSimulation;
 	
 	private int numIterations;
 	private TimerHandler timer;
 	private IEnvironment env;
 	private int tickTime;
 	private PropertyChangeSupport observers;
 	
 	private long startIterationTime;
 	private long elapsedTime;
 	
 	/**
 	 * Each list in the list contains one snapshot of frame;
 	 */
 	private List<List<IPopulation>> recordedSimulation;
 	/**
 	 * Simple object, used for synchronizing the {@link TimerHandler} and the
 	 * {@link IEnvironment} {@link OnFinishListener}
 	 */
 	private Object syncObject = new Object();
 	private int numUpdates = 0;
 	private Dimension d;
 	//private static final int NUM_THREAD = 1;
 	private ExecutorService executor = Executors.newSingleThreadExecutor();
 
 	private OnFinishListener mOnFinishListener = new OnFinishListener() {
 
 		@Override
 		public void onFinish(List<IPopulation> popList, List<IObstacle> obsList) {
 			elapsedTime = System.currentTimeMillis() - startIterationTime;
 			// Fire state changed to observers, notify there has been an update.
 			if(recordSimulation) {
 				recordedSimulation.add(clonePopulationList(popList));
 			} else {
 				observers.firePropertyChange(EVENT_TICK, obsList, popList);
 			}
 			if (runWithoutTimer) {
 				scheduleEnvironmentUpdate();		
 			} else {
 				synchronized (syncObject) {
 					//Log.v("Environment: Finished.");
 					if (timerFinished.get()) {
 						//Log.v("Environment: Timer is finished, doing Environment update");
 						environmentFinished.set(false);
 						timerFinished.set(false);
 						scheduleEnvironmentUpdate();
 					} else {
 						//Log.v("Environment: Timer NOT finished, waiting...");
 						environmentFinished.set(true);
 					}
 				}
 			}
 		}
 	};
 
 	private OnTickUpdate onTickListener = new OnTickUpdate() {
 		@Override
 		// När timer är klar.
 		public void onTick() {
 			synchronized (syncObject) {
 				//Log.v("Timer: Finished.");
 				if (environmentFinished.get()) {
 					//Log.v("Timer: Environment is finished, doing Environment update");
 					timerFinished.set(false);
 					environmentFinished.set(false);
 					scheduleEnvironmentUpdate();
 				} else {
 					//Log.v("Timer: Environment NOT finished, waiting...");
 					timerFinished.set(true);
 				}
 			}
 		}
 	};
 	
 
 	/**
 	 * Start EcoWorld with a tick-timer.
 	 * 
 	 * @param tickTime
 	 *            Minimum time it will take for one tick to complete.
 	 * @param numIterations
 	 *            Number of iterations before the program finishes.
 	 *            
 	 * @param d Dimension of the simulation.
 	 */
 	public EcoWorld(Dimension d, int tickTime, int numIterations, boolean recordSimulation) {
 		this.tickTime = tickTime;
 		this.timer = new TimerHandler();
 		this.d = d;
 		this.recordSimulation = recordSimulation;
 		if(recordSimulation) {
 			recordedSimulation = new ArrayList<List<IPopulation>>(numIterations);
 		}
 
 		/* Uncomment to test ticking functionality */
 		// this.env = new Environment(mOnFinishListener);
 
 		/* Use SquareEnvironment instead. */
 		this.env = new SquareEnvironment(createInitialPopulations(d),
 				readObsticlesFromFile(), mOnFinishListener, d.height, d.width);
 
 		this.runWithoutTimer = false;
 		this.numIterations = numIterations;
 		this.observers = new PropertyChangeSupport(this);
 	}
 
 	/**
 	 * Start EcoWorld WITHOUT a tick-timer.
 	 * <p>
 	 * EcoWorld simulation will run as fast as it can, without delays.
 	 * 
 	 * @param numIterations
 	 *            Number of iterations before the program finishes.
 	 *            
 	 * @param d Dimension of the simulation.
 	 */
 	public EcoWorld(Dimension d, int numIterations) {
 		this(d, 0, numIterations, false);
 		this.runWithoutTimer = true;
 	}
 
 	/**
 	 * Start EcoWorld WITHOUT a tick-timer.
 	 * <p>
 	 * EcoWorld simulation will run as fast as it can, without delays. For a
 	 * very long time.
 	 *            
 	 * @param d Dimension of the simulation.
 	 * 
 	 */
 	public EcoWorld(Dimension d) {
 		this(d, Integer.MAX_VALUE);
 	}
 
 	private List<IPopulation> createInitialPopulations(Dimension dim) {
 		List<IPopulation> populations = new ArrayList<IPopulation>();
 		//IPopulation rabbits = new RabbitPopulation(300, dim);
 		//rabbits.addPrey(rabbits);
 		//populations.add(rabbits);
 		
 
 //		IPopulation prey = new DummyPreyPopulation(dim, 500, Color.blue, 2.2, 2, 250);
 //		IPopulation predator = new DummyPredatorPopulation(dim, 10, Color.red, 2.5, 0.75,275);
 		
 		IPopulation prey = new DeerPopulation("Deers", dim, 1000, Color.blue, 2.2, 2, 250);
 //		IPopulation prey = new PigPopulation("Filthy Pigs", dim, 100, Color.pink, 2.0, 1.5, 225);
		IPopulation predator = new WolfPopulation("Wolves", dim, 400, Color.red, 2.5, 0.75,275);
		IPopulation grass = new GrassPopulation("Grass", dim, 60000, Color.green, 1, 1, 0, 1500);
 		
 		prey.addPredator(predator);
 		prey.addPrey(grass);
 		predator.addPrey(prey);
 		populations.add(prey);
 		populations.add(predator);
 		populations.add(grass);
 		return populations;
 	}
 
 	private List<IObstacle> readObsticlesFromFile() {
 		List<IObstacle> obsList = new ArrayList<IObstacle>();
 		obsList.add(new Obstacle("Obstacle.txt"));
 		return obsList;
 	}
 
 	/**
 	 * Start the EcoWorld simulation program.
 	 * 
 	 */
 	public void start() {
 		shouldRun.set(true);
 		scheduleEnvironmentUpdate();
 		Log.i("EcoWorld started.");
 		if(recordSimulation) {
 			observers.firePropertyChange(EVENT_RECORDING_FINISHED, null, null);
 		} else {
 			observers.firePropertyChange(EVENT_START, null, null);
 		}
 	}
 
 	/**
 	 * Stops the scheduling algorithms.
 	 * <p>
 	 * Warning! Will not affect ongoing execution!
 	 * 
 	 */
 	public void stop() {
 		shouldRun.set(false);
 		executor.shutdown();
 		timer.stop();
 		numUpdates = 0;
 		Log.i("EcoWorld stopped.");
 		if(recordSimulation) {
 //			for(List<IPopulation> list : recordedSimulation) {
 //				for(IPopulation pop : list) {
 //					Log.v("Population: " + pop);
 //					for(IAgent a : pop.getAgents()) {
 //						Log.v("Population: " + pop.toString() + " Agent:" + a.toString());
 //					}
 //				}
 //			}
 			observers.firePropertyChange(EVENT_RECORDING_FINISHED, null, null);
 		}
 	}
 	
 	/**
 	 * Plays the recorded simulation (if any), otherwise throws {@link IllegalArgumentException}.
 	 * <P>
 	 * Uses internal {@link TimerHandler} for smooth playing.
 	 */
 	public void playRecordedSimulation() {
 		if(!recordSimulation) {
 			throw new IllegalStateException("No simulation has been recorded");
 		}
 		final TimerHandler t = new TimerHandler();
 		t.start(17, new OnTickUpdate() {
 			@Override
 			public void onTick() {
 				if(recordedSimulation.size() > 0) {
 					List<IPopulation> popList = recordedSimulation.get(0);
 					recordedSimulation.remove(0);
 					observers.firePropertyChange(EVENT_TICK, Collections.emptyList(), popList);
 					t.start(17, this);
 				} else {
 					t.stop();
 					observers.firePropertyChange(EVENT_STOP, Collections.emptyList(), Collections.emptyList());
 				}
 			}
 		});
 	}
 
 	/**
 	 * Forces the ongoing execution to stop!
 	 * <p>
 	 * Warning! Untested method, might not work.
 	 * 
 	 */
 	public void forceStop() {
 		shouldRun.set(false);
 		executor.shutdownNow();
 		timer.stop();
 		numUpdates = 0;
 		Log.i("EcoWorld stopped.");
 	}
 
 	/**
 	 * Starts the {@link TimerHandler} and executes one Environment iteration.
 	 */
 	private void scheduleEnvironmentUpdate() {
 		if (numIterations-- > 0) {
 			if (!runWithoutTimer) {
 				timer.start(tickTime, onTickListener);
 			}
 			if(startIterationTime != 0) {
 				Log.v("---- Simulation model Update ---- Number of updates: "
 						+ ++numUpdates + " - Iteration time:" + elapsedTime);
 			} else {
 				Log.v("---- Simulation model Update ---- Number of updates: "
 					+ ++numUpdates);
 			}
 			executor.execute(env);
 			startIterationTime = System.currentTimeMillis();
 		} else {
 			stop();
 			playRecordedSimulation();
 		}
 	}
 	
 	/**
 	 * Adjust the tick rate of the next iteration. The currently executing
 	 * iteration will not be affected.
 	 * 
 	 * @param newTickRate
 	 */
 	public void adjustTickRate(int newTickRate) {
 		this.tickTime = newTickRate;
 	}
 
 	/**
 	 * Tick listener for the TimerHandler. Called when timer has expired.
 	 * 
 	 * @author Erik
 	 * 
 	 */
 	public interface OnTickUpdate {
 		public void onTick();
 	}
 
 	public void setRunWithoutTimer(boolean runWithoutTimer) {
 		this.runWithoutTimer = runWithoutTimer;
 	}
 
 	/**
 	 * Environment onFinish listener. Called when one iteration of the
 	 * Environment is done.
 	 * 
 	 * @author Erik
 	 * 
 	 */
 	public interface OnFinishListener {
 		public void onFinish(List<IPopulation> popList,
 				List<IObstacle> obstacleList);
 	}
 
 	public void addObserver(PropertyChangeListener listener) {
 		observers.addPropertyChangeListener(listener);
 	}
 	
 	public void removeObserver(PropertyChangeListener listener) {
 		observers.removePropertyChangeListener(listener);
 	}
 	
 	/**
 	 * Clones the given list with {@link IPopulation#clonePopulation()} method.
 	 */
 	private List<IPopulation> clonePopulationList(List<IPopulation> popList) {
 		List<IPopulation> list = new ArrayList<IPopulation>(popList.size());
 		for(IPopulation p : popList) {
 			list.add(p.clonePopulation());
 		}
 		return list;
 	}
 	
 	/**
 	 * Reads the recording from the given filePath.
 	 * <p>
 	 * Untested!
 	 * @param filePath
 	 * @return True if success, otherwise false.
 	 */
 	private boolean readRecordFromDisk(String filePath) {
 		String frameDivider = "FRAME";
 		String populationDivider = "POPULATION";
 		String agentDivider = "AGENT";
 		File f = new File(filePath);
 		if(!f.exists()) {
 			return false;
 		}
 		if(!f.canRead()) {
 			return false;
 		}
 		try {
 			FileInputStream fileStream = new FileInputStream(f);
 			Charset utf8 = Charset.forName("UTF-8");     
 			BufferedReader br = new BufferedReader(new InputStreamReader(fileStream, utf8));
 			String input = br.readLine();
 			while(input != null) {
 				input = br.readLine();
 				handleInput(input);
 			}
 			br.close();
 			fileStream.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return false;
 	}
 	
 	/**
 	 * Handles the input and parses it to the correct value.
 	 * @param input
 	 */
 	private void handleInput(String input) {
 		// TODO: Implement parser
 		String frameDivider = "FRAME";
 		String populationDivider = "POPULATION";
 		String agentDivider = "AGENT";
 		if(input.startsWith(frameDivider)) {
 			//Add the population-list to the record
 		} else if(input.startsWith(populationDivider)) {
 			//Create new population
 		} else if(input.startsWith(agentDivider)) {
 			//Parse agent and add it to the last population.
 		}
 	}
 
 	/**
 	 * Saves the given recording to the filePath
 	 * 
 	 * @param record
 	 * @param filePath
 	 * @return
 	 */
 	private boolean dumpRecordToDisk(List<List<IPopulation>> record, String filePath) {
 		String frameDivider = "FRAME";
 		String populationDivider = "POPULATION";
 		String agentDivider = "AGENT";
 		File f = new File(filePath);
 		if(f.exists()) {
 			f.delete();
 		} 
 		try {
 			f.createNewFile();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		PrintWriter pw = null;
 		try {
 			pw = new PrintWriter(f);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		}
 		for(List<IPopulation> popList : record) {
 			pw.println(frameDivider);
 			for(IPopulation p : popList) {
 				pw.println(populationDivider + ';' /*+ p.toBinaryString()*/ );
 				for(IAgent a : p.getAgents()) {
 					pw.println(agentDivider + ';' /*+a.toBinaryString() */);
 				}
 			}
 		}
 		pw.close();
 		return true;
 	}
 	
 }
