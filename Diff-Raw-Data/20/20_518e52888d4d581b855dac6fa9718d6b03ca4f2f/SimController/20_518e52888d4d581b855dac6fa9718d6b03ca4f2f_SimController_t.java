 /*
  * *********************************************************
  * Copyright (c) 2012 - 2012, DHBW Mannheim
  * Project: SoS
  * Date: Apr 9, 2012
  * Author(s): dirk
  * 
  * *********************************************************
  */
 package edu.dhbw.sos.simulation;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.log4j.Logger;
 
 import edu.dhbw.sos.course.Course;
 import edu.dhbw.sos.course.suggestions.SuggestionManager;
 import edu.dhbw.sos.gui.plan.buttons.ForwardBtn;
 import edu.dhbw.sos.gui.plan.buttons.LiveBtn;
 import edu.dhbw.sos.gui.plan.buttons.PlayBtn;
 import edu.dhbw.sos.gui.plan.buttons.RewindBtn;
 import edu.dhbw.sos.helper.CalcVector;
 import edu.dhbw.sos.observers.ICurrentCourseObserver;
 import edu.dhbw.sos.observers.IEditModeObserver;
 import edu.dhbw.sos.observers.ITimeGUIObserver;
 import edu.dhbw.sos.observers.Observers;
 
 
 /**
  * controls the simulation
  * simulates new students states out of old ones
  * 
  * @author DirkK
  * 
  */
 public class SimController implements ActionListener, MouseListener, IEditModeObserver, ITimeGUIObserver,
 		ICurrentCourseObserver {
 	private static final Logger	logger			= Logger.getLogger(SimController.class);
 	
 	private Course						course;
 	private int							currentTime;															// in
 																														// milliseconds
 																														// from
 																														// "begin"
 	private int							speed;																	// in
 																														// milliseconds
 	private int							notifyStep		= 1;
 	public static final int			realInterval	= 1000;												// in milliseconds
 	private int							interval			= 1000;
 	private transient Timer			pulse				= new Timer();
 	private boolean					run				= false;
 	
 	
 	private SuggestionManager		suggestionMgr;
 	
 	
 	/**
 	 * 
 	 * creates a SimController, manages the simulation of one specific course
 	 * 
 	 * @param course
 	 * @param sm
 	 * @author DirkK
 	 */
 	public SimController(Course course, SuggestionManager sm) {
 		this.suggestionMgr = sm;
 		this.course = course;
 		reset();
 	}
 	
 	
 	/**
 	 * 
 	 * reset the course to the start
 	 * 
 	 * @author DirkK
 	 */
 	public void reset() {
 		run = false;
 		stop();
 		course.reset();
 		setCurrentTime(0);
 		setSpeed(1);
 		suggestionMgr.reset(course.getProperties());
 	}
 	
 	
 	/**
 	 * 
 	 * toggle the simulation, start/stop it
 	 * 
 	 * @author DirkK
 	 */
 	private void toggle() {
 		if (run) {
 			stop();
 		} else {
 			run();
 		}
		// run = !run;
 	}
 	
 	
 	/**
 	 * 
 	 * runs a simulation, invokes a simulation step in every interval until the timer is stopped
 	 * 
 	 * @author DirkK
 	 */
 	public void run() {
 		Observers.notifySimulationStarted();
 		if (pulse != null) {
 			pulse.cancel();
 			pulse.purge();
 		}
 		pulse = new Timer();
 		TimerTask simulation = new TimerTask() {
 			@Override
 			public void run() {
 				simulationStep();
 				// Check if end of simulation reached
 				if (course.getLecture().getLength() * 60 < currentTime / realInterval) {
 					stop();
 					run = false;
 				}
 			}
 		};
 		pulse.scheduleAtFixedRate(simulation, 0, interval);
		// run is set to true to indicate that simulation is running, this isn't done in toggle to ensure that the
		// variable is set properly, also when it isn't called by toggle
		run = true;
 		logger.info("Simulation started");
 	}
 	
 	
 	/**
 	 * Stop simulation by canceling the timer and notifing observers
 	 * 
 	 * @author dirk
 	 */
 	public void stop() {
 		pulse.cancel();
 		pulse.purge();
 		Observers.notifySimulationStopped();
		// run is set to false to indicate that simulation isn't running, this isn't done in toggle to ensure that the
		// variable is set properly, also when it isn't called by toggle
		run = false;
 		logger.info("Simulation stopped");
 	}
 	
 	
 	/**
 	 * calculates for each student a new state
 	 * @author dirk
 	 */
 	public void simulationStep() {
 		// handle any suggestions
 		for (CalcVector cv : suggestionMgr.getAndClearInfluences()) {
 			logger.warn("suggestion " + cv.toString());
 			course.suggestionInput(cv);
 		}
 
 		setCurrentTime(currentTime + realInterval);
 		logger.debug("Simulation Step at " + currentTime);
 		course.simulationStep(currentTime);
 		
 		// update suggestions using statistics of whole course
 		suggestionMgr.updateSuggestions(course.getStatAvgStudentState());
 		
 		// notify GUI after simulation
 		notifyStep--;
 		if (notifyStep == 0) {
 			notifyStep = speed;
 			Observers.notifyStudents();
 			Observers.notifySelectedStudent();
 			Observers.notifyStatistics();
 		}
 	}
 	
 	
 	// --- GETTERS and SETTERS ---
 	
 	public int getCurrentTime() {
 		return currentTime;
 	}
 	
 	
 	public void setCurrentTime(int currentTime) {
 		this.currentTime = currentTime;
 		Observers.notifyTime(currentTime);
 	}
 	
 	
 	public int getSpeed() {
 		return speed;
 	}
 	
 	
 	public void setSpeed(int speed) {
 		if (speed > 1024)
 			speed = 1024;
 		if (speed < 1)
 			speed = 1;
 		this.speed = speed;
 		this.interval = realInterval / speed;
 		if (interval == 0)
 			interval = 1;
 		if (run) {
 			stop();
 			run();
 		}
 		Observers.notifySpeed(speed);
 	}
 	
 	
 	// --- action listeners ---
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() instanceof LiveBtn) {
 			stop();
 			setSpeed(1);
 			Observers.notifySpeed(this.getSpeed());
 			setCurrentTime((int) getCurrentSystemTime());
 			Observers.notifyTime(this.getCurrentTime());
 			run();
 		} else if (e.getSource() instanceof PlayBtn) {
 			toggle();
 		} else if (e.getSource() instanceof ForwardBtn) {
 			setSpeed(getSpeed() * 2);
 			Observers.notifySpeed(this.getSpeed());
 		} else if (e.getSource() instanceof RewindBtn) {
 			setSpeed(getSpeed() / 2);
 			Observers.notifySpeed(this.getSpeed());
 		}
 	}
 	
 	
 	/**
 	 * Get the current System Time in milisec. This method will convert the absolute day specific time in a value that
 	 * only contains the time infomration.
 	 * @return System time [ms] in respect to this Lecture
 	 * @author andres
 	 */
 	// FIXME andres current time is int and not long ??
 	private long getCurrentSystemTime() {
 		// Get real System time
 		long hoursInMiliSec = Calendar.HOUR_OF_DAY * 60 * 60 * 1000;
 		long minsInMiliSec = Calendar.MINUTE * 60 * 1000;
 		long secsInMiliSec = Calendar.SECOND * 1000;
 		long miliSec = Calendar.MILLISECOND;
 
 		long timeInMiliSec = hoursInMiliSec + minsInMiliSec + secsInMiliSec + miliSec;
 		
 		// Change the value for internal time
 		long start = course.getLecture().getStartInMilis();
 
 		if (start > timeInMiliSec) // if start is bigger then timeInMilisec, then the current time is at the day after the
 											// start, so add a full day to timeInMiliSec
 			timeInMiliSec += 24 * 60 * 60 * 1000;
 		long virtualSystemTime = timeInMiliSec - start;
 		logger.debug("Start: " + start + "; Time: " + timeInMiliSec);
 		return virtualSystemTime;
 	}
 	
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		float value = -20;
 		// right click
 		if (e.getButton() == MouseEvent.BUTTON3)
 			value *= -1;
 		if (course.getSelectedStudent() != null) {
 			// process don input
 			course.donInputQueue(course.getSelectedProperty(), value);
 		}
 		Observers.notifyStudents();
 	}
 	
 	
 	@Override
 	public void mousePressed(MouseEvent e) {
 	}
 	
 	
 	@Override
 	public void mouseReleased(MouseEvent e) {
 	}
 	
 	
 	@Override
 	public void mouseEntered(MouseEvent e) {
 	}
 	
 	
 	@Override
 	public void mouseExited(MouseEvent e) {
 		
 	}
 	
 	
 	@Override
 	public void enterEditMode() {
 		reset();
 	}
 	
 	
 	@Override
 	public void exitEditMode() {
 		
 	}
 	
 	
 	@Override
 	public void timeChanged(int time) {
 		// stop the simulation to make the correct deletions or to simulate to the correct point
 		stop();
 		
 		course.setTime(currentTime, time);
 		setCurrentTime(time);
 		
 		// if simulation was running, set running again
 		if (run)
 			run();
 		logger.debug(course.getPlace(0, 0).getHistoryStates().size());
 		Observers.notifySelectedStudent();
 
 	}
 	
 	
 	@Override
 	public void updateCurrentCourse(Course course) {
 		// reset old course
 		this.course.reset();
 		
 		// change and reset new course
 		this.course = course;
 		
 		// reset simController
 		reset();
 	}
 	
 	
 	public boolean isRun() {
 		return run;
 	}
 
 
 }
