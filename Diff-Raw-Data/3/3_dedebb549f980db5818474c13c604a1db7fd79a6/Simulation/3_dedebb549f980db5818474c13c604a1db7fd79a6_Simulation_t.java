 // Simulation.java
 package org.eclipse.stem.jobs.simulation;
 
 /*******************************************************************************
  * Copyright (c) 2006, 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
 import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.core.scenario.Scenario;
 import org.eclipse.stem.core.scenario.ScenarioPackage;
 import org.eclipse.stem.core.scenario.impl.ScenarioImpl;
 import org.eclipse.stem.core.scenario.provider.ScenarioItemProviderAdapterFactory;
 import org.eclipse.stem.core.sequencer.Sequencer;
 import org.eclipse.stem.jobs.Activator;
 import org.eclipse.stem.jobs.execution.Executable;
 import org.eclipse.stem.jobs.preferences.PreferenceConstants;
 import org.eclipse.stem.jobs.preferences.SimulationManagementPreferencePage;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * This class implements the main simulation logic of the STEM system. It runs
  * in the background as a separate {@link Job} in eclipse.
  */
 public class Simulation extends Executable implements ISimulation, IPropertyChangeListener {
 	/**
 	 * The collection of {@link ISimulationListener}'s waiting to be told about
 	 * {@link Simulation}'s events.
 	 */
 	// I think this should really be a {@link CopyOnWriteArrayList} like {@link
 	// #listenersSync}
 	private final List<ISimulationListener> listeners = Collections
 			.synchronizedList(new ArrayList<ISimulationListener>());
 
 	/**
 	 * The collection of {@link IBatchManagerListenerSync}'s waiting to be told
 	 * about {@link BatchManagerEvent}'s
 	 */
 	private final List<ISimulationListenerSync> listenersSync = new CopyOnWriteArrayList<ISimulationListenerSync>();
 
 	/**
 	 * If <code>true</code> then the {@link Simulation} will sleep for a
 	 * specified time period at the end of each simulation cycle.
 	 */
 	public boolean simulationSleep = SimulationManagementPreferencePage.DEFAULT_SIMULATION_SLEEP;
 
 	/**
 	 * If {@link simulationSleep} is <code>true</code>, then this is the number
 	 * of milliseconds at the end of each cycle that the {@link Simulation}
 	 * should sleep.
 	 */
 	private int sleepMilliseconds = SimulationManagementPreferencePage.MIN_SIMULATION_SLEEP_MILLISECONDS;
 
 	/**
 	 * The current state of the {@link Simulation}.
 	 */
 	private SimulationState simulationState;
 
 	/**
 	 * This is the {@link Scenario} being simulated. It contains all of the
 	 * {@link SimulationState} information.
 	 */
 	private Scenario scenario = null;
 	
 	private Adapter adapter = null; 
 
 	/**
 	 * This flag controls the execution of the {@link Simulation}. If it is
 	 * <code>false</code> the {@link Simulation} stops running (sleeps) on the
 	 * next cycle.
 	 * 
 	 * @see #pause()
 	 */
 	private boolean keepRunning = true;
 
 	/**
 	 * This flag controls the state of the {@link Simulation}. If
 	 * <code>true</code> then the {@link Simulation} is stopped if it is running
 	 * and the {@link Scenario} is reset to its initial state. The
 	 * {@link Simulation} does NOT resume running after the reset.
 	 * 
 	 * @see #resetSimulation()
 	 */
 	private boolean reset = false;
 
 	/**
 	 * This flag indicates that the {@link Simulation} should complete a single
 	 * step (cycle) and then pause.
 	 */
 	private boolean stepping = false;
 
 	/**
 	 * If <code>true</code> then the {@link Simulation} is stopping
 	 */
 	private boolean stopping = false;
 
 	ScenarioItemProviderAdapterFactory scenarioItemProviderAdapterFactory = new ScenarioItemProviderAdapterFactory();
 	/**
 	 * Constructor
 	 * 
 	 * @param title
 	 *            the title of the {@link Simulation}.
 	 * @param sequenceNumber
 	 *            the sequence number of the {@link Simulation}
 	 */
 	public Simulation(final String title, final int sequenceNumber) {
 		super(title == null ? "" : title, sequenceNumber); //$NON-NLS-1$
 		simulationState = SimulationState.PAUSED;
 	} // Simulation
 
 	/**
 	 * Constructor
 	 * 
 	 * @param scenario
 	 *            the {@link Scenario} to simulate
 	 * @param sequenceNumber
 	 *            the sequence number of the {@link Simulation}
 	 */
 	public Simulation(final Scenario scenario, final int sequenceNumber) {
 		this(scenario.produceTitle(), sequenceNumber);
 		this.scenario = scenario;
 		setPreferences();
 
 		Activator.getDefault().getPluginPreferences().addPropertyChangeListener(this);
 	} // Simulation
 
 	/**
 	 * Set the preferences.
 	 * 
 	 * @see #simulationSleep
 	 * @see #sleepMilliseconds
 	 */
 	protected void setPreferences() {
 		final Preferences preferences = Activator.getDefault()
 				.getPluginPreferences();
 		simulationSleep = preferences
 				.getBoolean(PreferenceConstants.SIMULATION_SLEEP_BOOLEAN);
 		sleepMilliseconds = preferences
 				.getInt(PreferenceConstants.SIMULATION_SLEEP_MILLISECONDS_INTEGER);
 		ScenarioImpl.reportEachUnresolvedIdentifiable = preferences
 				.getBoolean(PreferenceConstants.REPORT_EACH_UNRESOLVED_IDENTIFIABLE_BOOLEAN);
 		ScenarioImpl.reportDanglingAirTransportEdges = preferences
 			.getBoolean(PreferenceConstants.REPORT_DANGLING_AIR_TRANPORT_EDGES_BOOLEAN);
 
 		ScenarioImpl.reportNumberofUnresolvedIdentifiables = preferences
 				.getBoolean(PreferenceConstants.REPORT_NUMBER_OF_UNRESOLVED_IDENTIFIABLES_BOOLEAN);
 
 	} // setPerferences
 
 	/**
 	 * @return the state of the {@link Simulation}
 	 */
 	public final SimulationState getSimulationState() {
 		return simulationState;
 	} // getSimulationState
 
 	/**
 	 * @param simulationState
 	 *            the {@link SimulationState} to set
 	 */
 	private final void setSimulationState(final SimulationState simulationState) {
 		this.simulationState = simulationState;
 		fireSimulationChanged(simulationState);
 	} // setSimulationState
 
 	/**
 	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	protected IStatus run(final IProgressMonitor monitor) {
 		IStatus retValue = Status.OK_STATUS;
 
 		try {
 			setSimulationState(SimulationState.RUNNING);
 
 			keepRunning = true;
 
 			// Did we get a request to reset?
 			if (reset) {
 				// Yes
 				scenario.reset();
 				reset = false;
 			}
 
 			assert scenario.sane();
 
 			monitor.beginTask(scenario.produceTitle(), TOTAL_WORK);
 			final Sequencer sequencer = scenario.getSequencer();
 
 			// Figure out how much work has been performed for this scenario
 			// already.
 			// $ANALYSIS-IGNORE
 			monitor.worked((int) sequencer.getWorkComplete());
 			final Simulation self = this;
 			adapter = new AdapterImpl() {
 				/**
 				   * @override
 				   */
 				  @Override
 				public void notifyChanged(Notification msg)
 				  {
 					  Scenario scenario = (Scenario)msg.getNotifier();
 					  switch(msg.getFeatureID(Scenario.class)) {
 					  	case ScenarioPackage.SCENARIO__PROGRESS:
 					  		SimulationEvent event = new SimulationEvent(self, SimulationState.RUNNING, scenario.getProgress());
 					  		self.fireSimulationChangedEvent(event);
 					  		break;
 					  }
 				    
 				  }
 			}; 
 			scenario.eAdapters().add(adapter);
 			//ScenarioItemProvider sip = (ScenarioItemProvider)scenarioItemProviderAdapterFactory.adapt(this, ScenarioItemProvider.class);
 			
 			//scenarioItemProviderAdapterFactory.addListener(this); // ugh
 			
 			// Does the sequencer say we've finished before we've started?
 			if (!sequencer.isTimeToStop()) {
 				// No
 				while (keepRunning && !reset) {
 					final STEMTime currentTime = sequencer.getCurrentTime();
 					monitor.subTask(currentTime.toString());
 
 					// Attempt one step (cycle) in the simulation
 					try {
 						
						boolean success = scenario.step();
						if(!success) {keepRunning = false;retValue = Status.CANCEL_STATUS;}
 						assert scenario.sane();
 
 						// To sleep, per chance to dream?
 						if (simulationSleep) {
 							// Yes
 							Thread.sleep(sleepMilliseconds);
 						}
 
 						monitor.worked(sequencer.getWorkIncrement());
 
 						// We stop when the sequencer tells us it is time
 						if (sequencer.isTimeToStop()) {
 							keepRunning = false;
 							retValue = Status.OK_STATUS;
 						}
 						// Or, if things are canceled
 						else if (monitor.isCanceled()) {
 							keepRunning = false;
 							retValue = Status.CANCEL_STATUS;
 						}
 						// Or, if we're stepping
 						else if (stepping) {
 							keepRunning = false;
 						}
 
 					} catch (final Exception e) {
 						// Problem. We're out of here.
 						handleException(scenario, getName(), true, e);
 						keepRunning = false;
 						stopping = true;
 						retValue = Status.CANCEL_STATUS;
 					}
 					setSimulationState(SimulationState.COMPLETED_CYCLE);
 				} // while keepRunning
 
 				// Was it time to stop?
 				if (sequencer.isTimeToStop() && retValue == Status.OK_STATUS) {
 					// Yes
 					setSimulationState(SimulationState.COMPLETED_SEQUENCE);
 				}
 			} // if NOT time to stop before we start
 			else {
 				// Yes
 				// The sequencer says we've finished before we started
 				Activator.logInformation(MessageFormat.format(Messages
 						.getString("Sim.Time_Error"), sequencer
 						.getCurrentTime().toString(), sequencer.getEndTime()
 						.toString()), null);
 			} // else
 
 			// Did we get a request to reset?
 			if (reset) {
 				// Yes
 				scenario.reset();
 				reset = false;
 				setSimulationState(SimulationState.RESET);
 			}
 
 			monitor.done();
 
 		} catch (final RuntimeException e) {
 			handleException(scenario, getName(), true, e);
 			stopping = true;
 		} // catch RuntimeException
 
 		// Are we stopping or just pausing?
 		setSimulationState(stopping ? SimulationState.STOPPED
 				: SimulationState.PAUSED);
 
 		return retValue;
 
 	} // run
 
 	/**
 	 * Do the processing required to handle a {@link Exception}
 	 * 
 	 * @param scenario
 	 *            the {@link Scenario} that caused the {@link Exception}
 	 * @param name
 	 *            the name to use in error messages that identifies the source
 	 *            of the {@link Scenario}
 	 * @param promptUser
 	 *            if <code>true</code> then present the user with a dialog box
 	 *            explaining the message.
 	 * @param e
 	 *            the {@link Exception}
 	 */
 	static public void handleException(final Scenario scenario,
 			final String name, final boolean promptUser, final Exception e) {
 
 		// We can get a exception if the user tries to run a Scenario
 		// that doesn't have a Sequencer or Model specified. Or, we can get one
 		// because of some other internal error.
 
 		String tempErrorMessage = "";
 
 		boolean logIt = false;
 
 		// Potential missing Sequencer or Model?
 		if (e instanceof NullPointerException) {
 			// Yes
 			// Does the Scenario have a Sequencer?
 			if (scenario.getSequencer() == null) {
 				// No
 				tempErrorMessage = MessageFormat.format(Messages
 						.getString("Sim.MissingSeq"), new Object[] { name });
 			} // if missing a sequencer
 			// How about a model?
 			else if (scenario.getModel() == null) {
 				// No
 				tempErrorMessage = MessageFormat.format(Messages
 						.getString("Sim.MissingModel"), new Object[] { name });
 			} // if missing model
 			else {
 				// No
 				// Just some other NPE
 				logIt = true;
 				tempErrorMessage = MessageFormat.format(Messages
 						.getString("Sim.IErr"), new Object[] { name });
 			} // else
 		} // if NullPointerException
 		else {
 			// No
 			logIt = true;
 			tempErrorMessage = MessageFormat.format(Messages
 					.getString("Sim.IErr"), new Object[] { name });
 		} // else
 
 		final String errorMessage = tempErrorMessage;
 
 		// Log it?
 		if (logIt || !promptUser) {
 			// Yes
 			Activator.logError(errorMessage, e);
 		} // if
 
 		// Prompt the user?
 		if (promptUser) {
 			// Yes
 			try {
 				Display.getDefault().syncExec(new Runnable() {
 					public void run() {
 						try {
 							final IWorkbenchWindow window = PlatformUI
 									.getWorkbench().getActiveWorkbenchWindow();
 							final IStatus warning = new Status(IStatus.WARNING,
 									Activator.PLUGIN_ID, 1, errorMessage, null);
 							ErrorDialog.openError(window.getShell(), null, null,
 									warning);
 						} catch(Exception e) {
 							// If we get this exception, it is because we're not running in
 							// eclipse.
 						}
 					} // run
 				});
 			} catch (final Error ncdfe) {
 				// Empty
 			} // catch
 		} // if
 	} // handleRuntimeException
 
 	/**
 	 * Start running the {@link Simulation}.
 	 */
 	public final void run() {
 		stepping = false;
 		schedule();
 	} // run
 
 	/**
 	 * Pause the {@link Simulation}
 	 */
 	public final void pause() {
 		keepRunning = false;
 	} // pause
 
 	/**
 	 * Reset the {@link Simulation}.
 	 */
 	public final void reset() {
 		reset = true;
 		stepping = false;
 		// Is the simulation currently paused?
 		if (getSimulationState().equals(SimulationState.PAUSED)) {
 			// Yes
 			scenario.reset();
 			reset = false;
 			setSimulationState(SimulationState.RESET);
 			setSimulationState(SimulationState.PAUSED);
 		} // if
 	} // reset
 
 	/**
 	 * Step the {@link Simulation} one step/cycle if it hasn't already ended
 	 */
 	public final void step() {
 		stepping = true;
 		schedule();
 	} // stepSimulation
 
 	/**
 	 * Stop the {@link Simulation}.
 	 */
 	public final void stop() {
 		stopping = true;
 		keepRunning = false;
 		// We need to set our state here, which will notify our listeners,
 		// because we may not be scheduled and so the run(IProgressMonitor)
 		// method may not be executing and so would not set the state to STOPPED
 		// (and thus notify listeners)
 		setSimulationState(SimulationState.STOPPED);
 	} // stepSimulation
 
 	/**
 	 * @see org.eclipse.stem.jobs.execution.IExecutable#isRunning()
 	 */
 	public boolean isRunning() {
 		return !simulationState.equals(SimulationState.PAUSED);
 	}
 
 	/**
 	 * @return the {@link Scenario}
 	 */
 	public final Scenario getScenario() {
 		return this.scenario;
 	}
 
 	/**
 	 * @param scenario
 	 *            the {@link Scenario} to set
 	 */
 	protected final void setScenario(final Scenario scenario) {
 		this.scenario = scenario;
 	}
 
 	/**
 	 * @see org.eclipse.stem.jobs.simulation.ISimulation#addSimulationListener(org.eclipse.stem.jobs.simulation.ISimulationListener)
 	 */
 	public void addSimulationListener(final ISimulationListener listener) {
 		if (!listeners.contains(listener)) {
 			listeners.add(listener);
 		}
 	} // addSimulationListener
 
 	/**
 	 * @see org.eclipse.stem.jobs.simulation.ISimulation#addSimulationListenerSync(org.eclipse.stem.jobs.simulation.ISimulationListenerSync)
 	 */
 	public void addSimulationListenerSync(final ISimulationListenerSync listener) {
 		if (!listenersSync.contains(listener)) {
 			listenersSync.add(listener);
 		}
 	} // addSimulationListenerSync
 
 	/**
 	 * @see org.eclipse.stem.jobs.simulation.ISimulation#removeSimulationListener(org.eclipse.stem.jobs.simulation.ISimulationListener)
 	 */
 	public void removeSimulationListener(final ISimulationListener listener) {
 		listeners.remove(listener);
 	} // removeSimulationListener
 
 	/**
 	 * @see org.eclipse.stem.jobs.simulation.ISimulation#removeSimulationListenerSync(org.eclipse.stem.jobs.simulation.ISimulationListenerSync)
 	 */
 	public void removeSimulationListenerSync(
 			final ISimulationListenerSync listener) {
 		listenersSync.remove(listener);
 	} // removeSimulationListenerSync
 
 	/**
 	 * Tell the listeners about the change in the {@link Simulation}'s state
 	 * 
 	 * @param simulationState
 	 *            the new {@link SimulationState} of the {@link Simulation}
 	 */
 	private void fireSimulationChanged(final SimulationState simulationState) {
 		final SimulationEvent event = new SimulationEvent(this, simulationState);
 		fireSimulationChangedEvent(event);
 	} // fireSimulationManagerChanged
 	
 	/**
 	 * Tell the listeners about the change in the {@link Simulation}'s state
 	 * 
 	 * @param simulationState
 	 *            the new {@link SimulationState} of the {@link Simulation}
 	 */
 	void fireSimulationChangedEvent(final SimulationEvent event) {
 		
 		for (final ISimulationListenerSync listener : listenersSync) {
 			listener.simulationChangedSync(event);
 		} // for
 		
 		for (final ISimulationListener listener : listeners) {
 			listener.simulationChanged(event);
 		} // for
 	} // fireSimulationManagerChanged
 	
 	/**
 	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
 	 */
 	public void propertyChange(@SuppressWarnings("unused") final PropertyChangeEvent event) {
 		setPreferences();
 	} // propertyChange
 
 	/**
 	 * @return the title of the {@link Scenario}
 	 */
 	@Override
 	public String toString() {
 		return scenario.produceTitle();
 	}
 	
 	/**
 	 * interruptRequested. Return true if this listener requests 
 	 * that a decorator stops updating labels
 	 * 
 	 * @return boolean True if stop
 	 */
 	
 	public boolean interruptRequested() {
 		return (!this.keepRunning && stopping);
 	}
 	
 	public void destroy() {
 		List<ISimulationListener> tempList = new ArrayList<ISimulationListener>();
 		tempList.addAll(listeners);
 		for (ISimulationListener listener:tempList) {
 			this.removeSimulationListener(listener);
 		}
 		listeners.clear();
 		List<ISimulationListenerSync> tempListSync = new ArrayList<ISimulationListenerSync>();
 		tempListSync.addAll(listenersSync);
 		for (ISimulationListenerSync listener:tempListSync) {
 			this.removeSimulationListenerSync(listener);
 		}
 		listenersSync.clear();
 		tempList.clear();
 		tempListSync.clear();
 		
 		scenario.eAdapters().remove(adapter);
 		
 		Activator.getDefault().getPluginPreferences().removePropertyChangeListener(this);
 	}
 
 } // Simulation
