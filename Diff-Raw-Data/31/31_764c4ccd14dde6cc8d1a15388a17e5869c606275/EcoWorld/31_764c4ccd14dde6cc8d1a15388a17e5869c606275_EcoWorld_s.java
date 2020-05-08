 package chalmers.dax021308.ecosystem.model.environment;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import chalmers.dax021308.ecosystem.model.environment.obstacle.EllipticalObstacle;
 import chalmers.dax021308.ecosystem.model.environment.obstacle.IObstacle;
 import chalmers.dax021308.ecosystem.model.environment.obstacle.RectangularObstacle;
 import chalmers.dax021308.ecosystem.model.population.AbstractPopulation;
 import chalmers.dax021308.ecosystem.model.population.DeerPopulation;
 import chalmers.dax021308.ecosystem.model.population.DummyPredatorPopulation;
 import chalmers.dax021308.ecosystem.model.population.DummyPreyPopulation;
 import chalmers.dax021308.ecosystem.model.population.GrassPopulation;
 import chalmers.dax021308.ecosystem.model.population.IPopulation;
 import chalmers.dax021308.ecosystem.model.population.PigPopulation;
 import chalmers.dax021308.ecosystem.model.population.WolfPopulation;
 import chalmers.dax021308.ecosystem.model.util.CircleShape;
 import chalmers.dax021308.ecosystem.model.util.IShape;
 import chalmers.dax021308.ecosystem.model.util.Log;
 import chalmers.dax021308.ecosystem.model.util.Position;
 import chalmers.dax021308.ecosystem.model.util.SquareShape;
 import chalmers.dax021308.ecosystem.model.util.Stat;
 import chalmers.dax021308.ecosystem.model.util.TimerHandler;
 import chalmers.dax021308.ecosystem.model.util.TriangleShape;
 
 /**
  * Ecosystem main class.
  * <p>
  * Recieves notifications from the {@link TimerHandler} and the
  * {@link IEnvironment}.
  * <p>
  * Use the proper constructor for the wanted behavior of EcoWorld.
  * 
  * 
  * @author Erik Ramqvist
  * 
  */
 public class EcoWorld implements IModel {
 
 	//Please don't auto-indent this class, thanks.
 	
 	/* Property change events constants */
 	public static final String EVENT_TICK               = "chalmers.dax021308.ecosystem.model.Ecoworld.event_tick";
 	public static final String EVENT_STOP               = "chalmers.dax021308.ecosystem.model.Ecoworld.event_stop";
 	public static final String EVENT_START              = "chalmers.dax021308.ecosystem.model.Ecoworld.event_start";
 	public static final String EVENT_PAUSE              = "chalmers.dax021308.ecosystem.model.Ecoworld.event_pause";
 	public static final String EVENT_RECORDING_FINISHED = "chalmers.dax021308.ecosystem.model.Ecoworld.event_recording_started";
 	public static final String EVENT_RECORDING_STARTED  = "chalmers.dax021308.ecosystem.model.Ecoworld.event_recording_finished";
 	public static final String EVENT_DIMENSIONCHANGED   = "chalmers.dax021308.ecosystem.model.Ecoworld.event_dimension_changed";
 	public static final String EVENT_DELAY_CHANGED      = "chalmers.dax021308.ecosystem.model.Ecoworld.event_delay_changed";
 	public static final String EVENT_SHAPE_CHANGED      = "chalmers.dax021308.ecosystem.model.Ecoworld.event_shape_changed";
 	public static final String EVENT_ITERATION_FINISHED = "chalmers.dax021308.ecosystem.model.Ecoworld.event_iteration_finished";
 
 
 	/* State variables */
 	private boolean environmentFinished = false;
 	private boolean timerFinished       = false;
 	private boolean shouldRun           = false;
 	private boolean runWithoutTimer;
 	private boolean recordSimulation;
 	private boolean skipBoolean;
 	private boolean playRecording;
 
 	/* Simulation settings */
 	private int numIterations;
 	private TimerHandler timer;
 	private EnvironmentScheduler env;
 	private int tickTime;
 	private PropertyChangeSupport observers;
 	private List<IPopulation> recycledPopulationList;
 	private SimulationRecording recording;
 
 	/* Time measurements variables (in ns).*/
 	private long startIterationTime;
 	private double elapsedTime;
 	private Stat<Double> statTime;
 
 	/**
 	 * Each list in the list contains one snapshot of frame;
 	 */
 	private List<List<IPopulation>> recordedSimulation;
 	/**
 	 * Simple object, used for synchronizing the {@link TimerHandler} and the
 	 * {@link IEnvironment} {@link OnFinishListener}. This object makes the change of state done in mutual exclusion.
 	 */
 	private Object syncObject = new Object();
 	private int numUpdates = 0;
 	private Dimension d;
 	private ExecutorService executor;
 	private ExecutorService notifierExecutor = Executors.newSingleThreadExecutor();
 	
 	private ObserverNotifier notifier = new ObserverNotifier();
 
 	private OnFinishListener mOnFinishListener = new OnFinishListener() {
 
 
 		@Override
 		public void onFinish(List<IPopulation> popList, List<IObstacle> obsList) {
 			if (!shouldRun) {
 				return;
 			}
 			long start = System.nanoTime();
 			elapsedTime = (0.000001 * (start - startIterationTime));
 			// Fire state changed to observers, notify there has been an update.
 			/*if (recordSimulation) {
 				recordedSimulation.add(AbstractPopulation.clonePopulationList(popList));
 			} else {
 				//Send out the new cloned population list and obstacle list.
 //				recycledPopulationList = AbstractPopulation.clonePopulationListWithRecycledList(recycledPopulationList, popList);
 				observers.firePropertyChange(EVENT_TICK, obsList, AbstractPopulation.clonePopulationList(popList));
 				observers.firePropertyChange(EVENT_ITERATION_FINISHED, null, elapsedTime);
 			}*/
 			
 			notifier.popList = AbstractPopulation.clonePopulationList(popList);
 			notifier.obsList = obsList;
 			notifierExecutor.execute(notifier);
 //			double observerTime = (0.000001 * (System.nanoTime() - start));
 //			Log.v("Observer propertychange time: " + observerTime);
 			if (runWithoutTimer) {
 				scheduleEnvironmentUpdate();
 			} else {
 				synchronized (syncObject) {
 					// Environment finished
 					if (timerFinished) {
 						// Environment: Timer is finished, doing Environment update
 						environmentFinished = false;
 						timerFinished = false;
 						scheduleEnvironmentUpdate();
 					} else {
 						// Environment: Timer NOT finished, waiting...
 						environmentFinished = true;
 					}
 				}
 			}
 		}
 	};
 	
 
 	private class ObserverNotifier implements Runnable {
 		public List<IPopulation> popList;
 		public List<IObstacle> obsList;
 
 		@Override
 		public void run() {
 			if (recordSimulation && recording != null) {
 				recording.appendFrame(popList);
 				//recordedSimulation.add(AbstractPopulation.clonePopulationList(popList));
 			} 
 				//Send out the new cloned population list and obstacle list.
 //				recycledPopulationList = AbstractPopulation.clonePopulationListWithRecycledList(recycledPopulationList, popList);
 			observers.firePropertyChange(EVENT_TICK, obsList, popList);
 			observers.firePropertyChange(EVENT_ITERATION_FINISHED, null, elapsedTime);
 		}
 	};
 
 	private OnTickUpdate onTickListener = new OnTickUpdate() {
 		@Override
 		public void onTick() {
 			if (!shouldRun) {
 				return;
 			}
 			synchronized (syncObject) {
 				// Timer: Finished.
 				if (environmentFinished) {
 					// Timer: Environment is finished, doing Environment update.
 					timerFinished = false;
 					environmentFinished = false;
 					scheduleEnvironmentUpdate();
 				} else {
 					// Timer: Environment NOT finished, waiting...
 					timerFinished = true;
 				}
 			}
 		}
 	};
 
 	/**
 	 * Create the EcoWorld object. 
 	 * Needs to be loaded with a {@link SimulationSettings} before starting.
 	 */
 	public EcoWorld() {
 		this.d = new Dimension(1000, 1000);
 		this.timer = new TimerHandler();
 		this.observers = new PropertyChangeSupport(this);
 
 	}
 
 	private void setSimulationDimension(Dimension d) {
 		this.d = d;
 		observers.firePropertyChange(EVENT_DIMENSIONCHANGED, null, d);
 	}
 
 	private void setSimulationDimension(String dimConstant) {
 		if (dimConstant == SimulationSettings.DIM_XLARGE) {
 			d = SimulationSettings.D_XLARGE;
 		} else if (dimConstant == SimulationSettings.DIM_LARGE) {
 			d = SimulationSettings.D_LARGE;
 		} else if (dimConstant == SimulationSettings.DIM_MEDIUM) {
 			d = SimulationSettings.D_MEDIUM;
 		} else if (dimConstant == SimulationSettings.DIM_SMALL) {
 			d = SimulationSettings.D_SMALL;
 		}
 		observers.firePropertyChange(EVENT_DIMENSIONCHANGED, null, d);
 	}
 
 
 
 	/**
 	 * Load a {@link SimulationSettings} into EcoWorld.
 	 * <p>
 	 * The simulation should be stopped before loading.
 	 * 
 	 * @param s
 	 * @throws IllegalArgumentException
 	 */
 	public void loadSimulationSettings(SimulationSettings s) throws IllegalArgumentException {
 		if(s.getSimDimension() == null && s.getSimDimensionConstant() != null) {
 			setSimulationDimension(s.getSimDimensionConstant());
 		} else if(s.getSimDimension() != null && s.getSimDimensionConstant() == null) {
 			setSimulationDimension(s.getSimDimension());
 		}
 		
 		List<IPopulation> populations = new ArrayList<IPopulation>();
 		/*
 		 * Creating obstacles here for test. This should be done in a proper way later.
 		 */
 		List<IObstacle> obstacles = new ArrayList<IObstacle>();
 		
 		statTime = new Stat<Double>();
 
 		IPopulation prey = null;
 		IPopulation pred = null;
 		IPopulation grass = null;
 		IShape shape = null;
 		
 		
 		if(s.getObstacle() == SimulationSettings.OBSTACLE_ELLIPTICAL){
 			obstacles.add(new EllipticalObstacle(d.getWidth()*0.2, d.getHeight()*0.15, 
 					new Position(d.getWidth()/2,d.getHeight()/2),new Color(0, 128, 255)));
 		} else if (s.getObstacle() == SimulationSettings.OBSTACLE_RECTANGULAR) {
 			obstacles.add(new RectangularObstacle(d.getWidth()*0.1, d.getHeight()*0.02, 
 					new Position(d.getWidth()/2,d.getHeight()/2),new Color(0, 128, 255)));
 		}
 		
 		if (s.getShapeModel() == SimulationSettings.SHAPE_SQUARE) {
 			shape = new SquareShape();
 			observers.firePropertyChange(EVENT_SHAPE_CHANGED, null, shape);
 		} else if (s.getShapeModel() == SimulationSettings.SHAPE_CIRCLE) {
 			shape = new CircleShape();
 			observers.firePropertyChange(EVENT_SHAPE_CHANGED, null, shape);
 		} else if (s.getShapeModel() == SimulationSettings.SHAPE_TRIANGLE){
 			shape = new TriangleShape();
 			observers.firePropertyChange(EVENT_SHAPE_CHANGED, null, shape);
 		}
 		if (s.getPredatorModel() == SimulationSettings.POP_DUMMYPRED) {
 			pred = new DummyPredatorPopulation(d, s.getPredPopSize(), Color.red, 3,
 					0.75, 275, shape);
 		} else if (s.getPredatorModel() == SimulationSettings.POP_WOLF) {
 			pred = new WolfPopulation("Wolves", d, s.getPredPopSize(), Color.red, 2.2,
 					0.8, 250, true, shape, obstacles);
 		} 
 
 		if (s.getPreyModel() == SimulationSettings.POP_DEER) {
 			prey = new DeerPopulation("Deers", d, s.getPreyPopSize(), Color.blue, 2.0, 2,
 					200, true, shape, obstacles);
 		} else if (s.getPreyModel() == SimulationSettings.POP_DUMMYPREY) {
 			prey = new DummyPreyPopulation(d, s.getPreyPopSize(), Color.blue, 2.2, 2, 250, shape);
 		} else if (s.getPreyModel() == SimulationSettings.POP_PIG) {
 			prey = new PigPopulation("Filthy Pigs", d, s.getPreyPopSize(), Color.pink,
 					2.0, 1.5, 225, shape);
 		}
 
 		if (s.getGrassModel() == SimulationSettings.POP_GRASS) {
 			grass = new GrassPopulation("Grass", d, s.getGrassPopSize(), new Color(69,139,00), 1,
 					1, 0, 800, shape, obstacles);
 		} 
 
 		if (prey == null || pred == null || grass == null || shape == null) {
 			throw new IllegalArgumentException("Wrong populations set.");
 		}
 
 		prey.addPredator(pred);
 		prey.addPrey(grass);
 
 		pred.addPrey(prey);
 		populations.add(grass);
 		populations.add(prey);
 		populations.add(pred);
 		recordSimulation = s.isRecordSimulation();
 		runWithoutTimer = s.isRunWithoutTimer();
 		tickTime = s.getDelayLength();
 		numIterations = s.getNumIterations();
 		if (recordSimulation) {
 			this.recording = new SimulationRecording();
 			recording.initWriting("Testrecording1.sim");
 			recording.appendHeader(obstacles, d, s.getShapeModel());
 		}
 		this.env = new EnvironmentScheduler(populations, obstacles,
 				mOnFinishListener, d.height, d.width, s.getNumThreads());
 	}
 	
 	/**
 	 * Loads a simulation from the given filePath.
 	 * 
 	 * @param selectedFile
 	 */
 	public boolean loadRecordedSimulation(File selectedFile) {
 		this.recording = new SimulationRecording();
		if(recording.initReading(selectedFile)) {
			playRecording = true;
			return true;
 		}
 		List<IObstacle> obsList = recording.readHeader();
 		Dimension simDim = recording.getLoadedDimension();
 		String shapeModel = recording.getShapeConstant();
 		IShape shape;
 		if(shapeModel == null || simDim == null || obsList == null) {
 			throw new NullPointerException();
 		}
 		observers.firePropertyChange(EVENT_DIMENSIONCHANGED, null, simDim);
 		if (shapeModel.equals(SimulationSettings.SHAPE_SQUARE)) {
 			shape = new SquareShape();
 			observers.firePropertyChange(EVENT_SHAPE_CHANGED, null, shape);
 		} else if (shapeModel.equals(SimulationSettings.SHAPE_CIRCLE)) {
 			shape = new CircleShape();
 			observers.firePropertyChange(EVENT_SHAPE_CHANGED, null, shape);
 		} else if (shapeModel.equals(SimulationSettings.SHAPE_TRIANGLE)){
 			shape = new TriangleShape();
 			observers.firePropertyChange(EVENT_SHAPE_CHANGED, null, shape);
 		}
		return false;
 	}
 	
 	/**
 	 * Saves the recording to a file.
 	 * @param f
 	 */
 	public boolean saveRecordingToFile(File f) {
 		if(recordSimulation && recording != null) {
 			return recording.saveToFile(f);
 		}
 		return false;
 	}
 
 	private List<IObstacle> readObsticlesFromFile() {
 		List<IObstacle> obsList = new ArrayList<IObstacle>();
 		obsList.add(new EllipticalObstacle(0,0, new Position(), Color.black));
 		return obsList;
 	}
 
 
 	/**
 	 * Start the EcoWorld simulation program.
 	 * <p>
 	 * If already started {@link IllegalStateException} will be thrown.
 	 * 
 	 */
 	public void start() throws IllegalStateException {
 		synchronized (syncObject) {
 			if (!shouldRun) {
 				executor = Executors.newSingleThreadExecutor();
 				this.timer = new TimerHandler();
 				shouldRun = true;
 				scheduleEnvironmentUpdate();
 				Log.i("EcoWorld started.");
 				if (recordSimulation) {
 					observers.firePropertyChange(EVENT_RECORDING_FINISHED,
 							null, null);
 				} else {
 					observers.firePropertyChange(EVENT_START, null, null);
 				}
 			} else {
 				throw new IllegalStateException("EcoWorld already started.");
 			}
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
 		synchronized (syncObject) {
 			if (shouldRun) {
 				shouldRun = false;
 				executor.shutdownNow();
 				timer.stop();
 				numUpdates = 0;
 				Log.i("EcoWorld paused.");
 				observers.firePropertyChange(EVENT_PAUSE, null, null);
 				if (recordSimulation) {
 					observers.firePropertyChange(EVENT_RECORDING_FINISHED,
 							null, null);
 				}
 			} else {
 				throw new IllegalStateException("EcoWorld already paused.");
 			}
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
 		synchronized (syncObject) {
 			if (shouldRun) {
 				shouldRun = false;
 				executor.shutdownNow();
 				timer.stop();
 				numUpdates = 0;
 				Log.i("EcoWorld stopped.");
 				observers.firePropertyChange(EVENT_STOP, null, null);
 				if (recordSimulation) {
 					observers.firePropertyChange(EVENT_RECORDING_FINISHED,
 							null, null);
 				}
 			} else {
 				throw new IllegalStateException("EcoWorld already stopped");
 			}
 		}
 	}
 	
 	/**
 	 * One way shutdown of EcoWorld and its workers.
 	 * Should be used when exiting the program or creating a new instance of EcoWorld.
 	 */
 	public void shutdownNow() {
 		try {
 			stop();
 			if(executor != null) executor.shutdownNow();
 			if(env != null) env.shutdown();
 			if(timer != null) timer.stop();
 			if(recording != null) recording.close();
 			if(notifierExecutor != null) notifierExecutor.shutdownNow();
 		} catch (Exception e) {
 			//e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Plays the recorded simulation (if any). Assumes the recording is recorded
 	 * in 60 fps.
 	 * <P>
 	 * Uses internal {@link TimerHandler} for smooth playing.
 	 */
 	public void playRecordedSimulation() throws IllegalStateException {
 		final TimerHandler t = new TimerHandler();
 		if(recording == null || !playRecording) {
 			throw new IllegalStateException("No recorded simulation loaded.");
 		}
 		t.start(17, new OnTickUpdate() {
 			@Override
 			public void onTick() {
 				List<IPopulation> frame = recording.readFrame();
 				List<IObstacle> obsList = recording.getObstacles();
 				if(frame == null) {
 					t.stop();
 				} else {
 					observers.firePropertyChange(EVENT_TICK, obsList, frame);
 					t.start(17, this);
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
 			StringBuffer sb = new StringBuffer("-- Simulation model Update: ");
 			sb.append(++numUpdates);
 			if (startIterationTime != 0) {
 				sb.append(" - Iteration time:");
 				sb.append(roundTwoDecimals(elapsedTime));
 				sb.append(" ms.");
 				statTime.addObservation(elapsedTime);
 				sb.append(" mean value: ");
 				sb.append(roundTwoDecimals(statTime.getMean()));
 				sb.append(" sample variance: ");
 				sb.append(roundTwoDecimals(statTime.getSampleVariance()));
 			}
 			Log.v(sb.toString());
 			executor.execute(env);
 			startIterationTime = System.nanoTime();
 		} else {
 			stop();
 			if(recording != null) recording.close();
 			if(recordSimulation) {
 				loadRecordedSimulation(new File("Testrecording1.sim") );
 				playRecordedSimulation();
 			}
 			/*if (recordSimulation) {
 				playRecordedSimulation(recordedSimulation);
 			}*/
 		}
 	}
 	
 	public static double roundTwoDecimals(double num) {
 		double result = num * 100;
 		result = Math.round(result);
 		result = result / 100;
 		return result;
 	}
 	
 	/**
 	 * Adjust the tick rate of the next iteration. The currently executing
 	 * iteration will not be affected.
 	 * 
 	 * @param newTickRate
 	 */
 	public void setDelayLength(int newDelay) {
 		synchronized (syncObject) {
 			this.tickTime = newDelay;
 		}
 		observers.firePropertyChange(EVENT_DELAY_CHANGED, null, newDelay);
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
 
 	@Override
 	public void addObserver(PropertyChangeListener listener) {
 		observers.addPropertyChangeListener(listener);
 	}
 
 	@Override
 	public void removeObserver(PropertyChangeListener listener) {
 		observers.removePropertyChangeListener(listener);
 	}
 
 
 
 }
