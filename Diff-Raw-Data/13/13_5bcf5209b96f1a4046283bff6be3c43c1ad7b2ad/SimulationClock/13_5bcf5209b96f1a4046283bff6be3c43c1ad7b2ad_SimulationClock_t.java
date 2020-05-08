 /*  	CASi Context Awareness Simulation Software
  *   Copyright (C) 2012  Moritz Bürger, Marvin Frick, Tobias Mende
  *
  *  This program is free software. It is licensed under the
  *  GNU Lesser General Public License with one clarification.
  *  
  *  You should have received a copy of the 
  *  GNU Lesser General Public License along with this program. 
  *  See the LICENSE.txt file in this projects root folder or visit
  *  <http://www.gnu.org/licenses/lgpl.html> for more details.
  */
 package de.uniluebeck.imis.casi.simulation.engine;
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Logger;
 
 import de.uniluebeck.imis.casi.CASi;
 import de.uniluebeck.imis.casi.simulation.model.SimulationTime;
 import de.uniluebeck.imis.casi.utils.Listenable;
 
 /**
  * Simulation Clock maps real time on simulated time
  * 
  * @author Tobias Mende
  * 
  */
 public class SimulationClock implements Listenable<ISimulationClockListener> {
 	/** The maximum scale factor (the slowest speed) for the clock */
 	public static final int MAXIMUM_SCALE_FACTOR = 2000;
 	/** The minimum scale factor (the highest speed) for the clock */
 	public static final int MINIMUM_SCALE_FACTOR = 100;
 	
 	/** The instance of this singleton class */
 	private static SimulationClock instance;
 	/** The starttime of this simulation */
 	private SimulationTime simulationStartTime;
 	/** The current time in the simulation */
 	private SimulationTime currentTime;
 	/**
 	 * Factor: tick every scaleFactor milliseconds (1 simulated second = scale
 	 * factor real milliseconds)
 	 */
 	private int scaleFactor;
 	/** The default scale factor */
 	public static final int DEFAULT_SCALE_FACTOR = 500;
 	/** flag shows whether simulation is started or not */
 	private boolean started;
 	/** flag shows whether simulation is paused */
 	private boolean paused;
 	/** The timer which handles the system time */
 	private Timer timer;
	/** Sets whether the timer is invalid or not */
	private boolean invalidateTimer;
 	/** Listeners are informed about every tick of the clock */
 	private ArrayList<ISimulationClockListener> listeners = new ArrayList<ISimulationClockListener>();
 
 	/** The application logger */
 	private Logger log = Logger.getLogger(SimulationClock.class.getName());
 
 	/**
 	 * Private singleton constructor
 	 */
 	private SimulationClock() {
 		// just here for prohibiting external access
 	}
 
 	/**
 	 * Getter for the instance of the singleton SimulationClock
 	 * 
 	 * @return the instance
 	 */
 	public static SimulationClock getInstance() {
 		if (instance == null) {
 			instance = new SimulationClock();
 		}
 		return instance;
 	}
 
 	/**
 	 * Initializes the simulation clock
 	 * 
 	 * @param startTime
 	 *            the exact time, when the simulation starts (just eye candy)
 	 * @throws IllegalStateException
 	 *             if the clock was started before
 	 */
 	public synchronized void init(SimulationTime startTime)
 			throws IllegalStateException {
 		init(startTime, DEFAULT_SCALE_FACTOR);
 	}
 
 	/**
 	 * Initializes the simulation clock
 	 * 
 	 * @param startTime
 	 *            the exact time, when the simulation starts (just eye candy)
 	 * @param factor
 	 *            the factor (1 simulated second = factor real milliseconds)
 	 * @throws IllegalStateException
 	 *             if the clock was started before
 	 */
 	public synchronized void init(SimulationTime startTime, int factor)
 			throws IllegalStateException {
 		init(startTime, (SimulationTime) startTime.clone(), factor);
 	}
 
 	/**
 	 * Initializes the simulation clock
 	 * 
 	 * @param startTime
 	 *            the exact time, when the simulation starts (just eye candy)
 	 * @param currentTime
 	 *            the current time usually not needed, use
 	 *            {@link SimulationClock#init(SimulationTime, int)} instead
 	 * @param factor
 	 *            the factor (1 simulated second = factor real milliseconds)
 	 * @throws IllegalStateException
 	 *             if the clock was started before
 	 */
 	public synchronized void init(SimulationTime startTime,
 			SimulationTime currentTime, int factor)
 			throws IllegalStateException {
 		if (started) {
 			throw new IllegalStateException("Can't init clock after starting");
 		}
 		simulationStartTime = startTime;
 		this.currentTime = currentTime;
 		scaleFactor = factor;
 	}
 
 	/**
 	 * Starts the simulation clock (the simulation)
 	 * 
 	 * @throws IllegalStateException
 	 *             if the clock wasn't initialized before or if it was started
 	 *             yet.
 	 */
 	public synchronized void start() throws IllegalStateException {
 		if (started) {
 			throw new IllegalStateException("Simulation was already started");
 		} else if (simulationStartTime == null) {
 			throw new IllegalStateException("Can't start before initialization");
 		}
 		renewTimer();
 		started = true;
 		informListenerAboutSimulationStart();
 	}
 
 	/**
 	 * Method for renewing the timer with a new timer task. Should be called
 	 * e.g. after scale factor changes.
 	 */
 	private synchronized void renewTimer() {
 		if (timer != null) {
 			timer.cancel();
 			timer = null;
 		}
		invalidateTimer = false;
 		timer = new Timer("CASi-System-Clock", true);
 		timer.schedule(new TimerTask() {
 
 			@Override
 			public void run() {
				if(invalidateTimer) {
					timer.cancel();
					renewTimer();
					return;
				}
 				if (!paused) {
 					// incrementing time
 					currentTime.increment();
 					// informing listeners about time change
 					informListenerAboutTimeChange();
 				}
 			}
 		}, 0, scaleFactor);
 	}
 
 	/**
 	 * Stops the simulation
 	 * 
 	 * @throws IllegalStateException
 	 *             if the simulation wasn't started before.
 	 */
 	public synchronized void stop() throws IllegalStateException {
 		if (!started) {
 			throw new IllegalStateException("Simulation isn't started yet.");
 		}
 		timer.cancel();
 		started = false;
 		informListenerAboutSimulationStop();
 	}
 
 	/**
 	 * Setter for the pause state
 	 * 
 	 * @param pause
 	 *            set to <code>true</code> if the simulation should be paused,
 	 *            set to <code>false</code> otherwise.
 	 */
 	public synchronized void setPaused(boolean pause) {
 		paused = pause;
 		informListenerAboutPause(pause);
 	}
 
 	/**
 	 * Getter for the pause state
 	 * 
 	 * @return whether the simulation is paused or not
 	 */
 	public synchronized boolean isPaused() {
 		return paused;
 	}
 
 	/**
 	 * Getter for the simulation state
 	 * 
 	 * @return <code>true</code> if the simulation is started yet,
 	 *         <code>false</code> otherwise.
 	 */
 	public synchronized boolean isStarted() {
 		return started;
 	}
 
 	@Override
 	public synchronized void addListener(ISimulationClockListener listener) {
 		if (!listeners.contains(listener)) {
 			listeners.add(listener);
 		}
 	}
 
 	@Override
 	public synchronized void removeListener(ISimulationClockListener listener) {
 		listeners.remove(listener);
 	}
 
 	/**
 	 * Getter for the current time
 	 * 
 	 * @return the current time
 	 */
 	public synchronized SimulationTime getCurrentTime() {
 		return currentTime;
 	}
 
 	/**
 	 * Setter for the scale factor (meaning the simulation speed a high scale
 	 * factor results in a slow speed)
 	 * 
 	 * @param scaleFactor
 	 *            the factor to set
 	 */
 	public synchronized void setScaleFactor(int scaleFactor) {
 		if(scaleFactor > MAXIMUM_SCALE_FACTOR) {
 			CASi.SIM_LOG.warning("scale factor was too high. Set scale factor to "+MAXIMUM_SCALE_FACTOR);
 			this.scaleFactor = MAXIMUM_SCALE_FACTOR;
 		} else if(scaleFactor < MINIMUM_SCALE_FACTOR) {
 			CASi.SIM_LOG.warning("scale factor was too low. Set scale factor to "+MINIMUM_SCALE_FACTOR);
 			this.scaleFactor = MINIMUM_SCALE_FACTOR;
 		} else {
 			this.scaleFactor = scaleFactor;
 		}
 		if (started) {
			invalidateTimer = true;
 		}
 	}
 
 	/**
 	 * Getter for the current factor
 	 * 
 	 * @return the current factor (the amount of real milliseconds which must
 	 *         elapse before one simulated second is finished)
 	 */
 	public synchronized int getScaleFactor() {
 		return scaleFactor;
 	}
 
 	/**
 	 * Informs the listeners about the simulation's start
 	 */
 	private synchronized void informListenerAboutSimulationStart() {
 		for (ISimulationClockListener l : listeners) {
 			l.simulationStarted();
 		}
 		log.fine("Informed " + listeners.size()
 				+ " clock listeners about simulation start");
 	}
 
 	/**
 	 * Informs the listeners about the simulation's stop
 	 */
 	private synchronized void informListenerAboutSimulationStop() {
 		for (ISimulationClockListener l : listeners) {
 			l.simulationStopped();
 		}
 		log.fine("Informed " + listeners.size()
 				+ " clock listeners about simulation stop");
 	}
 
 	/**
 	 * Informs the listeners about a time change
 	 */
 	private synchronized void informListenerAboutTimeChange() {
 		for (ISimulationClockListener l : listeners) {
 			l.timeChanged(currentTime);
 		}
 		log.fine("Informed " + listeners.size()
 				+ " clock listeners about time change");
 	}
 
 	/**
 	 * Informs the listeners about a change of the pause state
 	 * 
 	 * @param pause
 	 *            if <code>true</code> the simulation is paused, else it
 	 *            continues.
 	 */
 	private synchronized void informListenerAboutPause(boolean pause) {
 		for (ISimulationClockListener l : listeners) {
 			l.simulationPaused(pause);
 		}
 		log.fine("Informed " + listeners.size()
 				+ " clock listeners about pause was set to " + pause);
 	}
 
 }
