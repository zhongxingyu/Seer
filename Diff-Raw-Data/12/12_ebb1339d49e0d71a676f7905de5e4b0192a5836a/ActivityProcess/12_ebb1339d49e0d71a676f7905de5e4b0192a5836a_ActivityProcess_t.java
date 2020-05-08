 /*
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Philip Langer - initial API and implementation
  */
 package org.modelexecution.fumldebug.debugger.process;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.PlatformObject;
 import org.eclipse.debug.core.DebugEvent;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.model.IDebugTarget;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.debug.core.model.IStreamsProxy;
 import org.modelexecution.fumldebug.core.event.ActivityEntryEvent;
 import org.modelexecution.fumldebug.core.event.Event;
 import org.modelexecution.fumldebug.debugger.FUMLDebuggerPlugin;
 import org.modelexecution.fumldebug.debugger.logger.ConsoleLogger;
import org.modelexecution.fumldebug.debugger.process.internal.ErrorEvent;
 import org.modelexecution.fumldebug.debugger.process.internal.InternalActivityProcess;
 
 public class ActivityProcess extends PlatformObject implements IProcess {
 
 	private InternalActivityProcess activityProcess;
 	private ILaunch launch;
 	private String name;
 	@SuppressWarnings("rawtypes")
 	private Map attributes;
 
 	private List<Event> allEvents = new ArrayList<Event>();
 	private List<Event> lastEvents = new ArrayList<Event>();
 
 	private ConsoleLogger consoleLogger = new ConsoleLogger();
 
 	private boolean isStarted = false;
 	private boolean isTerminated = false;
 
 	public ActivityProcess(ILaunch launch, Process process, String name,
 			@SuppressWarnings("rawtypes") Map attributes) {
 		setFields(launch, process, name, attributes);
 		runActivityProcess();
 	}
 
 	private void setFields(ILaunch launch, Process process, String name,
 			@SuppressWarnings("rawtypes") Map attributes) {
 		this.launch = launch;
 		assertActivityProcess(process);
 		this.activityProcess = (InternalActivityProcess) process;
 		launch.addProcess(this);
 		this.name = name;
 		this.attributes = attributes;
 	}
 
 	private void assertActivityProcess(Process process) {
 		Assert.isTrue(process instanceof InternalActivityProcess,
 				"Process must be of type InternalActivityProcess.");
 	}
 
 	private void runActivityProcess() {
 		clearEventLists();
 		resetStateFlags();
 		this.activityProcess.run();
 		processEvents();
 	}
 
 	private void clearEventLists() {
 		allEvents.clear();
 		lastEvents.clear();
 	}
 
 	private void resetStateFlags() {
 		isStarted = false;
 		isTerminated = false;
 	}
 
 	private void processEvents() {
 		updateEventLists();
 		logNewEvents();
 		updateState();
 	}
 
 	private void updateEventLists() {
 		lastEvents.clear();
 		lastEvents.addAll(activityProcess.pollEvents());
 		allEvents.addAll(lastEvents);
 	}
 
 	private void logNewEvents() {
 		for (Event event : lastEvents) {
 			try {
 				// TODO use a event to string writer
				// TODO let consoleLogger decide whether to put it into error or not
				if (event instanceof ErrorEvent) {
					consoleLogger.writeError(event.toString());
				} else {
					consoleLogger.write(event.toString() + "\n");
				}
 			} catch (IOException e) {
 				FUMLDebuggerPlugin.log(e);
 			}
 		}
 	}
 
 	private void updateState() {
 		if (lastEvents.isEmpty())
 			return;
 
 		if (isStarting()) {
 			setStarted(true);
 		}
 
 		if (isTerminating()) {
 			try {
 				terminate();
 			} catch (DebugException e) {
 				FUMLDebuggerPlugin.log(e);
 			}
 		}
		
		// TODO check for error event
 	}
 
 	private boolean isStarting() {
 		return !isStarted && !isTerminated
 				&& lastEvents.get(0) instanceof ActivityEntryEvent;
 	}
 
 	private void setStarted(boolean started) {
 		isStarted = started;
 		fireChangeEvent();
 	}
 
 	private boolean isTerminating() {
 		return isStarted
 				&& !isTerminated
 				&& activityProcess.isLastActivityExitEvent(lastEvents
 						.get(lastEvents.size() - 1));
 	}
 
 	@Override
 	public synchronized boolean canTerminate() {
 		return activityProcess != null && isStarted && !isTerminated;
 	}
 
 	@Override
 	public synchronized boolean isTerminated() {
 		return isTerminated;
 	}
 
 	public boolean isStarted() {
 		return isStarted;
 	}
 
 	@Override
 	public void terminate() throws DebugException {
 		activityProcess.destroy();
 		isTerminated = true;
 		fireTerminateEvent();
 	}
 
 	public void resume() {
 		activityProcess.resume();
 		processEvents();
 		fireChangeEvent();
 	}
 
 	public void suspend() {
 		activityProcess.suspend();
 		processEvents();
 		fireChangeEvent();
 	}
 
 	@Override
 	public IStreamsProxy getStreamsProxy() {
 		return consoleLogger;
 	}
 
 	public String getName() {
 		return activityProcess.getActivityName() + " (" //$NON-NLS-1$
 				+ activityProcess.getRootExecutionID() + ")"; //$NON-NLS-1$
 	}
 
 	public int getRootExecutionId() {
 		return activityProcess.getRootExecutionID();
 	}
 
 	protected void fireEvent(DebugEvent event) {
 		DebugPlugin manager = DebugPlugin.getDefault();
 		if (manager != null) {
 			manager.fireDebugEventSet(new DebugEvent[] { event });
 		}
 	}
 
 	protected void fireTerminateEvent() {
 		fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
 	}
 
 	protected void fireChangeEvent() {
 		fireEvent(new DebugEvent(this, DebugEvent.CHANGE));
 	}
 
 	@Override
 	public synchronized int getExitValue() throws DebugException {
 		return InternalActivityProcess.EXIT_VALUE;
 	}
 
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
 		if (adapter.equals(IProcess.class)) {
 			return this;
 		}
 		if (adapter.equals(IDebugTarget.class)) {
 			ILaunch launch = getLaunch();
 			IDebugTarget[] targets = launch.getDebugTargets();
 			for (int i = 0; i < targets.length; i++) {
 				if (this.equals(targets[i].getProcess())) {
 					return targets[i];
 				}
 			}
 			return null;
 		}
 		if (adapter.equals(ILaunch.class)) {
 			return getLaunch();
 		}
 		// CONTEXTLAUNCHING
 		if (adapter.equals(ILaunchConfiguration.class)) {
 			return getLaunch().getLaunchConfiguration();
 		}
 		return super.getAdapter(adapter);
 	}
 
 	@Override
 	public String getLabel() {
 		return name;
 	}
 
 	@Override
 	public ILaunch getLaunch() {
 		return launch;
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void setAttribute(String key, String value) {
 		if (attributes == null) {
 			attributes = new HashMap(5);
 		}
 		Object origVal = attributes.get(key);
 		if (origVal != null && origVal.equals(value)) {
 			return; // nothing changed.
 		}
 
 		attributes.put(key, value);
 		fireChangeEvent();
 	}
 
 	@Override
 	public String getAttribute(String key) {
 		if (attributes == null) {
 			return null;
 		}
 		return (String) attributes.get(key);
 	}
 
 }
