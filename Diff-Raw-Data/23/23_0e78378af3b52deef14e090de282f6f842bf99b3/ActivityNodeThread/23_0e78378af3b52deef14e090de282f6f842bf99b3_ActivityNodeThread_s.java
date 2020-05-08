 /*
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Philip Langer - initial API and implementation
  */
 package org.modelexecution.fumldebug.debugger.model;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.debug.core.DebugEvent;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.debug.core.model.IStackFrame;
 import org.eclipse.debug.core.model.IThread;
 import org.modelexecution.fumldebug.core.Breakpoint;
 import org.modelexecution.fumldebug.core.event.ActivityExitEvent;
 import org.modelexecution.fumldebug.core.event.ActivityNodeEvent;
 import org.modelexecution.fumldebug.core.event.BreakpointEvent;
 import org.modelexecution.fumldebug.core.event.Event;
 import org.modelexecution.fumldebug.core.event.SuspendEvent;
 import org.modelexecution.fumldebug.core.event.TraceEvent;
 import org.modelexecution.fumldebug.debugger.breakpoints.ActivityNodeBreakpoint;
 import org.modelexecution.fumldebug.debugger.process.internal.ErrorEvent;
 
 import fUML.Syntax.Activities.IntermediateActivities.ActivityNode;
 
 public class ActivityNodeThread extends ActivityDebugElement implements IThread {
 
 	private int rootExecutionId = -1;
 	private int currentExecutionId = -1;
 	private int currentChangeReason = -1;
 	private List<ActivityNodeBreakpoint> currentlyHitBreakpoints = new ArrayList<ActivityNodeBreakpoint>();
 	private ActivityNode activityNode;
 	private Set<Integer> allExecutionIds = new HashSet<Integer>();
 	private ActivityNodeStackFrame topStackFrame;
 
 	private boolean isTerminated = false;
 	private boolean isStepping = false;
 
 	public ActivityNodeThread(ActivityDebugTarget target,
 			ActivityNode activityNode, int executionId) {
 		super(target);
 		this.activityNode = activityNode;
 		this.rootExecutionId = executionId;
 		setCurrentExecutionId(executionId);
 		startListeningToEvents();
 		initializeTopStackFrame();
 		fireCreationEvent();
 	}
 
 	private void initializeTopStackFrame() {
 		this.topStackFrame = new ActivityNodeStackFrame(this);
 	}
 
 	protected ActivityNode getActivityNode() {
 		return activityNode;
 	}
 
 	@Override
 	public void notify(Event event) {
 		if (isNonStepOrBreakpointTraceEvent(event)) {
 			TraceEvent traceEvent = (TraceEvent) event;
 			clearCurrentlyHitBreakpoint();
 			if (originatedFromThisActivityNode(traceEvent))
 				setCurrentExecutionId(traceEvent.getActivityExecutionID());
 			if (isFinalExitEvent(traceEvent))
 				doTermination();
 		} else if (isBreakpointEvent(event)) {
 			BreakpointEvent breakpointEvent = (BreakpointEvent) event;
 			if (concernsThisThread(breakpointEvent))
 				saveCurrentlyHitBreakpoint(breakpointEvent);
 		} else if (isSuspendEvent(event)) {
 			SuspendEvent stepEvent = (SuspendEvent) event;
 			if (concernsThisThread(stepEvent))
 				handleSuspendEvent(stepEvent);
 		} else if (isErrorEventForThisThread(event))
 			doTermination();
 	}
 
 	private boolean isNonStepOrBreakpointTraceEvent(Event event) {
 		return isTraceEvent(event) && !isSuspendEvent(event)
 				&& !isBreakpointEvent(event);
 	}
 
 	private boolean isTraceEvent(Event event) {
 		return event instanceof TraceEvent;
 	}
 
 	private boolean isSuspendEvent(Event event) {
 		return event instanceof SuspendEvent;
 	}
 
 	private boolean isBreakpointEvent(Event event) {
 		return event instanceof BreakpointEvent;
 	}
 
 	private boolean originatedFromThisActivityNode(TraceEvent traceEvent) {
 		return concernsCurrentActivityNode(traceEvent)
 				|| concernsParentEventsCurrentActivity(traceEvent);
 	}
 
 	private boolean concernsCurrentActivityNode(Event event) {
 		if (event instanceof ActivityNodeEvent) {
 			ActivityNodeEvent activityNodeEvent = (ActivityNodeEvent) event;
 			if (activityNode.equals(activityNodeEvent.getNode())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean concernsParentEventsCurrentActivity(TraceEvent traceEvent) {
 		TraceEvent currentEvent = traceEvent;
 		Event parentEvent = null;
 		while ((parentEvent = currentEvent.getParent()) != null) {
 			if (concernsCurrentActivityNode(parentEvent)) {
 				return true;
 			} else if (isTraceEvent(parentEvent)) {
 				currentEvent = (TraceEvent) parentEvent;
 			} else {
 				return false;
 			}
 		}
 		return false;
 	}
 
 	private void setCurrentExecutionId(int executionId) {
 		currentExecutionId = executionId;
 		allExecutionIds.add(executionId);
 	}
 
 	private boolean isFinalExitEvent(TraceEvent traceEvent) {
 		if (traceEvent instanceof ActivityExitEvent) {
 			ActivityExitEvent activityExitEvent = (ActivityExitEvent) traceEvent;
 			return hasRootExecutionId(activityExitEvent);
 		}
 		return false;
 	}
 
 	private boolean hasRootExecutionId(ActivityExitEvent activityExitEvent) {
 		return rootExecutionId == activityExitEvent.getActivityExecutionID();
 	}
 
 	private boolean concernsThisThread(TraceEvent traceEvent) {
 		return allExecutionIds.contains(traceEvent.getActivityExecutionID());
 	}
 
 	private void saveCurrentlyHitBreakpoint(BreakpointEvent breakpointEvent) {
 		// TODO test
		for(Breakpoint breakpoint : breakpointEvent.getBreakpoints()) {		
 			currentlyHitBreakpoints.add(getActivityDebugTarget().getBreakpoint(
				breakpoint.getActivityNode()));
 		}
 		currentChangeReason = DebugEvent.BREAKPOINT;
 	}
 
 	private void clearCurrentlyHitBreakpoint() {
 		currentlyHitBreakpoints.clear();
 	}
 
 	private void handleSuspendEvent(SuspendEvent stepEvent) {
 		setSteppingStopped();
 		updateState(stepEvent);
 		setCurrentExecutionId(stepEvent.getActivityExecutionID());
 		fireSuspendEvent(currentChangeReason);
 	}
 
	private void updateState(SuspendEvent stepEvent) {
		if (stepEvent.getNewEnabledNodes().isEmpty()) {
			doTermination();
		} else if (stepEvent.getNewEnabledNodes().size() == 1) {
			activityNode = stepEvent.getNewEnabledNodes().get(0);
 		} else {
 			List<ActivityNode> otherEnabledNodes = new ArrayList<ActivityNode>(
					stepEvent.getNewEnabledNodes());
 			activityNode = otherEnabledNodes.get(0);
 			otherEnabledNodes.remove(activityNode);
 			getActivityDebugTarget().addThreads(otherEnabledNodes,
					stepEvent.getActivityExecutionID());
 		}
 	}
 
 	private boolean isErrorEventForThisThread(Event event) {
 		if (event instanceof ErrorEvent) {
 			ErrorEvent errorEvent = (ErrorEvent) event;
 			return allExecutionIds
 					.contains(errorEvent.getActivityExecutionID());
 		}
 		return false;
 	}
 
 	@Override
 	public boolean canResume() {
 		return !isTerminated;
 	}
 
 	@Override
 	public boolean canSuspend() {
 		return false;
 	}
 
 	@Override
 	public boolean isSuspended() {
 		return !isTerminated;
 	}
 
 	@Override
 	public void resume() throws DebugException {
 		currentChangeReason = DebugEvent.RESUME;
 		getDebugTarget().resume();
 	}
 
 	@Override
 	public void suspend() throws DebugException {
 		getDebugTarget().suspend();
 	}
 
 	@Override
 	public boolean canStepInto() {
 		return !isTerminated();
 	}
 
 	@Override
 	public boolean canStepOver() {
 		return !isTerminated();
 	}
 
 	@Override
 	public boolean canStepReturn() {
 		return !isTerminated();
 	}
 
 	@Override
 	public boolean isStepping() {
 		return isStepping;
 	}
 
 	private void setSteppingStarted() {
 		isStepping = true;
 	}
 
 	private void setSteppingStopped() {
 		isStepping = false;
 	}
 
 	@Override
 	public void stepInto() throws DebugException {
 		currentChangeReason = DebugEvent.STEP_INTO;
 		setSteppingStarted();
 		getActivityProcess().stepInto(currentExecutionId, activityNode);
 	}
 
 	@Override
 	public void stepOver() throws DebugException {
 		currentChangeReason = DebugEvent.STEP_OVER;
 		setSteppingStarted();
 		getActivityProcess().stepOver(currentExecutionId, activityNode);
 	}
 
 	@Override
 	public void stepReturn() throws DebugException {
 		currentChangeReason = DebugEvent.STEP_RETURN;
 		setSteppingStarted();
 		getActivityProcess().stepReturn(currentExecutionId);
 	}
 
 	private void startListeningToEvents() {
 		getActivityProcess().addEventListener(this);
 	}
 
 	private void stopListeningToEvents() {
 		getActivityProcess().removeEventListener(this);
 	}
 
 	@Override
 	public boolean canTerminate() {
 		return !isTerminated;
 	}
 
 	@Override
 	public boolean isTerminated() {
 		return isTerminated;
 	}
 
 	@Override
 	public void terminate() throws DebugException {
 		doTermination();
 	}
 
 	private void doTermination() {
 		isTerminated = true;
 		stopListeningToEvents();
 		getActivityDebugTarget().removeThread(this);
 		fireTerminateEvent();
 	}
 
 	@Override
 	public IStackFrame[] getStackFrames() throws DebugException {
 		return new IStackFrame[] { topStackFrame };
 	}
 
 	@Override
 	public boolean hasStackFrames() throws DebugException {
 		return getStackFrames().length > 0;
 	}
 
 	@Override
 	public int getPriority() throws DebugException {
 		return 0;
 	}
 
 	@Override
 	public IStackFrame getTopStackFrame() throws DebugException {
 		return topStackFrame;
 	}
 
 	@Override
 	public String getName() throws DebugException {
 		return activityNode.qualifiedName + " [" + currentExecutionId + "]";
 	}
 
 	@Override
 	public IBreakpoint[] getBreakpoints() {
 		if (isSuspended() && currentlyHitBreakpoints.size() > 0) {
 			return (IBreakpoint[]) currentlyHitBreakpoints.toArray();
 		}
 		return new IBreakpoint[] {};
 	}
 
 }
