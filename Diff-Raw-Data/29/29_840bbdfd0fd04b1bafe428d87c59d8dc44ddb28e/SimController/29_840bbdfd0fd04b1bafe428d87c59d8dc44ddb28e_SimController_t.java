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
 import java.util.LinkedList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.log4j.Logger;
 
 import edu.dhbw.sos.course.Course;
 import edu.dhbw.sos.course.Courses;
 import edu.dhbw.sos.course.ICurrentCourseObserver;
 import edu.dhbw.sos.course.ISimulation;
 import edu.dhbw.sos.course.suggestions.SuggestionManager;
 import edu.dhbw.sos.gui.plan.ForwardBtn;
 import edu.dhbw.sos.gui.plan.LiveBtn;
 import edu.dhbw.sos.gui.plan.PlayBtn;
 import edu.dhbw.sos.gui.plan.RewindBtn;
 import edu.dhbw.sos.gui.right.IEditModeObserver;
 import edu.dhbw.sos.helper.CalcVector;
 
 
 /**
  * controls the simulation
  * simulates new students states out of old ones
  * 
  * @author DirkK
  * 
  */
 
 public class SimController implements ActionListener, MouseListener, IEditModeObserver, ITimeObserver,
 		ICurrentCourseObserver {
 	private static final Logger			logger						= Logger.getLogger(SimController.class);
 	
 	private Course								course;
 	private int									currentTime;																		// in
 																																			// milliseconds
 																																			// from
 																																			// "begin"
 	private int									speed;																				// in
 																																			// milliseconds
 	private int									notifyStep					= 1;
 	private int									realInterval				= 1000;
 	private int									interval						= 1000;
 	private transient Timer					pulse							= new Timer();
 	private boolean							run							= false;
 	
 	private LinkedList<ISpeedObserver>	speedObservers				= new LinkedList<ISpeedObserver>();
 	private LinkedList<ITimeObserver>	timeObservers				= new LinkedList<ITimeObserver>();
 	private LinkedList<ISimulation>		simulationOberservers	= new LinkedList<ISimulation>();
 	
 	private SuggestionManager				sm;
 
 
 	public SimController(Course course, SuggestionManager sm) {
 		this.sm = sm;
 		reset(course);
 	}
 	
 	
 	public void reset() {
 		stop();
 		course.reset();
 		setCurrentTime(0);
 		setSpeed(1);
 		sm.reset(course.getProperties());
 		run = false;
 	}
 	
 	
 	public void reset(Course course) {
 		this.course = course;
 		reset();
 	}
 	
 
 	public void notifySpeedObservers() {
 		for (ISpeedObserver so : speedObservers) {
 			so.speedChanged(speed);
 		}
 	}
 	
 	
 	public void subscribeSpeed(ISpeedObserver so) {
 		speedObservers.add(so);
 	}
 	
 	
 	public void notifyTimeObservers() {
 		int timeInMin = currentTime / 60000;
 		for (ITimeObserver to : timeObservers) {
 			to.timeChanged(timeInMin);
 		}
 	}
 	
 	
 	public void subscribeTime(ITimeObserver to) {
 		if (to != null)
 			timeObservers.add(to);
 	}
 	
 	
 	public void notifySimulationStarted() {
 		for (ISimulation cco : simulationOberservers) {
 			cco.simulationStarted();
 		}
 	}
 	
 	
 	public void notifySimulationStopped() {
 		for (ISimulation cco : simulationOberservers) {
 			cco.simulationStopped();
 		}
 	}
 	
 	
 	public void subscribeSimulation(ISimulation cco) {
 		simulationOberservers.add(cco);
 	}
 
 
 	public boolean toggle() {
 		if (run) {
 			logger.info("Simulation stopped");
 			stop();
 		} else {
 			logger.info("Simulation started");
 			run();
 		}
 		run = !run;
 		return true;
 	}
 	
 	
 	public void run() {
 		notifySimulationStarted();
 		pulse = new Timer();
 		TimerTask simulation = new TimerTask() {
 			@Override
 			public void run() {
 				simulationStep();
				if (course.getLecture().getLength() * 60 < currentTime / realInterval) {
					stop();
				}
 			}
 		};
 		pulse.scheduleAtFixedRate(simulation, 0, interval);
 	}
 	
 	
 	public void stop() {
 		pulse.cancel();
 		pulse.purge();
 		notifySimulationStopped();
 	}
 	
 	
 	/**
 	 * calculates for each student a new state
 	 * @author dirk
 	 */
 	private void simulationStep() {
 		setCurrentTime(currentTime + realInterval);
 		logger.debug("Simulation Step at " + currentTime);
 		course.simulationStep(currentTime);
 		logger.debug("History states: " + course.getPlace(0, 0).getHistoryStates().size());
 		
 		// calculate state statistics for whole course
 		course.calcStatistics(currentTime);
 		// update suggestions using statistics of whole course
 		sm.updateSuggestions(course.getStatState());
 		
 		// notify GUI after simulation
 		notifyStep--;
 		if (notifyStep == 0) {
 			notifyStep = speed;
 			Courses.notifyStudentsObservers();
 			Courses.notifySelectedStudentObservers();
 			Courses.notifyStatisticsObservers();
 		}
 		
 		// handle any suggestions
 		for (CalcVector cv : sm.getAndClearInfluences()) {
 			course.suggestionInput(cv);
 		 }
 	}
 	
 	
 	public void jumpTo(int time) {
 		currentTime = time;
 		// fetch start state from history
 	}
 	
 	
 	// --- GETTERS and SETTERS ---
 	
 	public int getCurrentTime() {
 		return currentTime;
 	}
 	
 	
 	public void setCurrentTime(int currentTime) {
 		this.currentTime = currentTime;
 		notifyTimeObservers();
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
 		notifySpeedObservers();
 	}
 	
 	
 	// --- action listeners ---
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() instanceof LiveBtn) {
 			// FIXME Not implemented
 		} else if (e.getSource() instanceof PlayBtn) {
 			if (toggle()) {
 				// ((PlayBtn) e.getSource()).toggle();
 			}
 		} else if (e.getSource() instanceof ForwardBtn) {
 			setSpeed(getSpeed() * 2);
 			notifySpeedObservers();
 		} else if (e.getSource() instanceof RewindBtn) {
 			setSpeed(getSpeed() / 2);
 			notifySpeedObservers();
 		}
 	}
 	
 	
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		float value = -20;
 		// right click
 		if (e.getButton() == MouseEvent.BUTTON3)
 			value *= -1;
 		if (course.getSelectedStudent() != null) {
 			course.donInputQueue(course.getSelectedProperty(), value, currentTime);
 		}
 		Courses.notifyStudentsObservers();
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
 	}
 	
 	
 	@Override
 	public void updateCurrentCourse(Course course) {
 		reset(course);
 	}
 }
