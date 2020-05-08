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
 
 import chalmers.dax021308.ecosystem.model.agent.AbstractAgent;
 import chalmers.dax021308.ecosystem.model.agent.IAgent;
 import chalmers.dax021308.ecosystem.model.population.AbstractPopulation;
 import chalmers.dax021308.ecosystem.model.population.DeerPopulation;
 import chalmers.dax021308.ecosystem.model.population.DummyPredatorPopulation;
 import chalmers.dax021308.ecosystem.model.population.DummyPreyPopulation;
 import chalmers.dax021308.ecosystem.model.population.GrassPopulation;
 import chalmers.dax021308.ecosystem.model.population.IPopulation;
 import chalmers.dax021308.ecosystem.model.population.PigPopulation;
 import chalmers.dax021308.ecosystem.model.population.RabbitPopulation;
 import chalmers.dax021308.ecosystem.model.population.WolfPopulation;
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
	public static final String EVENT_START              = "chalmers.dax021308.ecosystem.model.Ecoworld.event_start";
 	public static final String EVENT_PAUSE		        = "chalmers.dax021308.ecosystem.model.Ecoworld.event_pause";
 	public static final String EVENT_RECORDING_FINISHED = "chalmers.dax021308.ecosystem.model.Ecoworld.event_recordingstarted";
 	public static final String EVENT_RECORDING_STARTED  = "chalmers.dax021308.ecosystem.model.Ecoworld.event_recordingfinished";
 	
 	/* Population constants */
 	public static final String POP_PIG       = "Pig Population";
 	public static final String POP_RABBIT    = "Rabbit Population";
 	public static final String POP_DUMMYPREY = "Dummy Prey Population";
 	public static final String POP_DEER      = "Deer Population";
 	public static final String POP_GRASS     = "Grass Population";
 	public static final String POP_DUMMYPRED = "Dummy Predator Population";
 	public static final String POP_WOLF      = "Wolf Population";
 	
 	/* Population array based on predator-prey model */
 	public static final String[] PREY_VALUES = { POP_DEER, POP_PIG, POP_RABBIT, POP_DUMMYPREY };
 	public static final String[] PRED_VALUES  = { POP_WOLF, POP_DUMMYPRED };
 	public static final String[] GRASS_VALUES = { POP_GRASS };
 	
 	
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
 	
 	public static WorldGrid worldGrid;
 	
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
 	private ExecutorService executor;
 
 	private OnFinishListener mOnFinishListener = new OnFinishListener() {
 
 		@Override
 		public void onFinish(List<IPopulation> popList, List<IObstacle> obsList) {
 			if(!shouldRun.get()) {
 				return;
 			}
 			elapsedTime = System.nanoTime() - startIterationTime;
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
 			if(!shouldRun.get()) {
 				return;
 			}
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
 		
 		worldGrid = new WorldGrid(d, 100);
 
 
 		
 		this.runWithoutTimer = false;
 		this.numIterations = numIterations;
 		
 		this.observers = new PropertyChangeSupport(this);
 	}
 	
 	public void setRecordSimulation(boolean recordSimulation) {
 		this.recordSimulation = recordSimulation;
 	}
 	
 	
 	public synchronized void setNumIterations(int numIterations) {
 		this.numIterations = numIterations;
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
 
 	public void createInitialPopulations(String predatorModel, int predPop, String preyModel, int preyPop, String grassModel, int grassPop) throws IllegalArgumentException {
 		List<IPopulation> populations = new ArrayList<IPopulation>();
 		
 		IPopulation prey = null;
 		IPopulation pred = null;
 		IPopulation grass = null;
 
 		if(predatorModel == POP_DUMMYPRED) {
 			pred = new DummyPredatorPopulation(d, predPop, Color.red, 2.5, 0.75,275);
 		} else if(predatorModel == POP_WOLF) {
 			pred = new WolfPopulation("Wolves", d, predPop, Color.red, 2.5, 0.75,250);
 		} 
 
 		if(preyModel == POP_DEER) {
 			prey = new DeerPopulation("Deers", d, preyPop, Color.blue, 2.2, 2, 200, true);
 		} else if(preyModel == POP_RABBIT) {
 			prey = new RabbitPopulation("Rabbits", d, preyPop, Color.lightGray, 10, 10, 200);
 		} else if(preyModel == POP_DUMMYPREY) {
 			prey = new DummyPreyPopulation(d, preyPop, Color.blue, 2.2, 2, 250);
 		} else if(preyModel == POP_PIG) {
 			prey = new PigPopulation("Filthy Pigs", d, preyPop, Color.pink, 2.0, 1.5, 225);
 		}
 		
 		if(grassModel == POP_GRASS) {
 			grass = new GrassPopulation("Grass", d, grassPop, Color.green, 1, 1, 0, 1500);
 		}
 
 		if(prey == null || pred == null || grass == null) {
 			throw new IllegalArgumentException("Wrong populations set.");
 		}
 		
 		prey.addPredator(pred);
 		prey.addPrey(grass);
 		
 		pred.addPrey(prey);
 		populations.add(prey);
 		populations.add(pred);
 		populations.add(grass);
 		
 		if(recordSimulation) {
 			recordedSimulation = new ArrayList<List<IPopulation>>(numIterations);
 		}
 		this.env = new SquareEnvironment(populations, readObsticlesFromFile(), mOnFinishListener, d.height, d.width);
 	}
 
 	private List<IObstacle> readObsticlesFromFile() {
 		List<IObstacle> obsList = new ArrayList<IObstacle>();
 		obsList.add(new Obstacle("Obstacle.txt"));
 		return obsList;
 	}
 
 	/**
 	 * Start the EcoWorld simulation program.
 	 * <p>
 	 * If already started {@link IllegalStateException} will be thrown.
 	 * 
 	 */
 	public synchronized void start() throws IllegalStateException{
 		if(!shouldRun.get()) {
 			executor = Executors.newSingleThreadExecutor();
 			this.timer = new TimerHandler();
 			shouldRun.set(true);
 			scheduleEnvironmentUpdate();
 			Log.i("EcoWorld started.");
 			if(recordSimulation) {
 				observers.firePropertyChange(EVENT_RECORDING_FINISHED, null, null);
 			} else {
 				observers.firePropertyChange(EVENT_START, null, null);
 			}
 		} else {
 			 throw new IllegalStateException("EcoWorld already started.");
 		}
 	}
 	
 	/**
 	 * Stops the scheduling algorithms.
 	 * <p>
 	 * Warning! WILL affect ongoing execution!
 	 * <p>
 	 * If already stopped {@link IllegalStateException} will be thrown.	
 	 *  
 	 */
 	public void pause() throws IllegalStateException {
 		if(shouldRun.get()) {
 			shouldRun.set(false);
 			executor.shutdownNow();
 			timer.stop();
 			numUpdates = 0;
 			Log.i("EcoWorld paused.");
 			observers.firePropertyChange(EVENT_PAUSE, null, null);
 			if(recordSimulation) {
 				observers.firePropertyChange(EVENT_RECORDING_FINISHED, null, null);
 			}
 		} else {
 			 throw new IllegalStateException("EcoWorld already paused.");
 		}
 	}
 
 	/**
 	 * Stops the scheduling algorithms.
 	 * <p>
 	 * Warning! WILL affect ongoing execution!
 	 * <p>
 	 * If already stopped {@link IllegalStateException} will be thrown.	
 	 *  
 	 */
 	public void stop() throws IllegalStateException {
 		if(shouldRun.get()) {
 			shouldRun.set(false);
 			executor.shutdownNow();
 			timer.stop();
 			numUpdates = 0;
 			Log.i("EcoWorld stopped.");
 			observers.firePropertyChange(EVENT_STOP, null, null);
 			if(recordSimulation) {
 				observers.firePropertyChange(EVENT_RECORDING_FINISHED, null, null);
 			}
 		} else {
 			 throw new IllegalStateException("EcoWorld already stopped");
 		}
 	}
 	
 	
 	/**
 	 * Plays the recorded simulation (if any).
 	 * <P>
 	 * Uses internal {@link TimerHandler} for smooth playing.
 	 */
 	public void playRecordedSimulation(final List<List<IPopulation>> recordedSim) {
 		final TimerHandler t = new TimerHandler();
 		
 		t.start(17, new OnTickUpdate() {
 			@Override
 			public void onTick() {
 				if(recordedSim.size() > 0) {
 					List<IPopulation> popList = recordedSim.get(0);
 					recordedSim.remove(0);
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
 	 * Starts the {@link TimerHandler} and executes one Environment iteration.
 	 * <p>
 	 * (Gets the ball running.)
 	 */
 	private void scheduleEnvironmentUpdate() {
 		if (numIterations-- > 0) {
 			if (!runWithoutTimer) {
 				timer.start(tickTime, onTickListener);
 			}
 			StringBuffer sb = new StringBuffer("---- Simulation model Update ---- Number of updates: ");
 			sb.append(++numUpdates);
 			if(startIterationTime != 0) {
 				sb.append(" - Iteration time:" );
 				sb.append((long) (0.000001*elapsedTime));
 				sb.append(" ms.");
 			} 
 			Log.v(sb.toString());
 			executor.execute(env);
 			startIterationTime = System.nanoTime();
 		} else {
 			stop();
 			if(recordSimulation) {
 				playRecordedSimulation(recordedSimulation);
 			}
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
 	
 	public Dimension getSize() {
 		return d;
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
 	 * Plays the loaded simulation, or throws {@link IllegalStateException} if not any recording is loaded.
 	 */
 	public void playRecentLoadedSimulation() throws IllegalStateException {
 		if(recordedSimulation != null && !recordedSimulation.isEmpty()) {
 			playRecordedSimulation(recordedSimulation);
 		} else {
 			throw new IllegalStateException("No recording loaded");
 		}
 	}
 	
 	/**
 	 * Reads the recording from the given filePath.
 	 * <p>
 	 * Untested!
 	 * @param filePath
 	 * @return True if success, otherwise false.
 	 */
 	public boolean readRecordFromDisk(String filePath) {
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
 			List<List<IPopulation>> readInput = parseFile(br);
 			recordedSimulation = readInput;
 			br.close();
 			fileStream.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return false;
 	}
 	
 	private List<List<IPopulation>> parseFile(BufferedReader br) {
 		String frameDivider = "FRAME";
 		String populationDivider = "POPULATION";
 		String agentDivider = "AGENT";
 		
 		List<List<IPopulation>> result = new ArrayList<List<IPopulation>>();
 		
 		List<IPopulation> currentFrame = null;
 		IPopulation currentPop = null;
 		
 		String input = null;
 		try {
 			input = br.readLine();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		while(input != null) {
 			try {
 				input = br.readLine();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			if(input.startsWith(frameDivider)) {
 				if(currentFrame != null) {
 					result.add(currentFrame);
 				}
 				currentFrame = new ArrayList<IPopulation>();
 			} else if(input.startsWith(populationDivider)) {
 				if(currentPop != null) {
 					currentFrame.add(currentPop);
 				}
 				String [] inputArr = input.split(";", 2);
 				currentPop = AbstractPopulation.createFromFile(inputArr[1]);
 			} else if(input.startsWith(agentDivider)) {
 				if(currentPop != null) {
 					String [] inputArr = input.split(";", 2);
 					IAgent newIAgent = AbstractAgent.createFromFile(inputArr[1]); 
 					currentPop.getAgents().add(newIAgent);
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Saves the given recording to the filePath
 	 * 
 	 * @param record
 	 * @param filePath
 	 * @return
 	 */
 	public boolean dumpRecordToDisk(List<List<IPopulation>> record, String filePath) {
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
 				pw.println(populationDivider + ';' + p.toBinaryString() );
 				for(IAgent a : p.getAgents()) {
 					pw.println(agentDivider + ';' + a.toBinaryString());
 				}
 			}
 		}
 		pw.close();
 		return true;
 	}
 }
