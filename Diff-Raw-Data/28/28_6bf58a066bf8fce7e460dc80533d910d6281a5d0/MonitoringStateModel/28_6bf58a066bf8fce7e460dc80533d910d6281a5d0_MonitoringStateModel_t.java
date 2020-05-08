 /*******************************************************************************
  * Copyright (c) 29 jan. 2013 NetXForge.
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details. You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Christophe Bouhier - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.common.model;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.IJobChangeListener;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.ecore.EObject;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.netxforge.netxstudio.common.context.IComputationContext;
 import com.netxforge.netxstudio.common.internal.CommonActivator;
 import com.netxforge.netxstudio.library.Component;
 import com.netxforge.netxstudio.library.LevelKind;
 import com.netxforge.netxstudio.library.NetXResource;
 import com.netxforge.netxstudio.operators.Marker;
 import com.netxforge.netxstudio.operators.Node;
 import com.netxforge.netxstudio.operators.Operator;
 import com.netxforge.netxstudio.operators.ResourceMonitor;
 import com.netxforge.netxstudio.operators.ToleranceMarker;
 import com.netxforge.netxstudio.operators.ToleranceMarkerDirectionKind;
 import com.netxforge.netxstudio.services.RFSService;
 import com.netxforge.netxstudio.services.ServiceMonitor;
 
 /**
  * Maintains a monitoring state and can be queried for state information. The
  * following state information can be retrieved; </p> </br>
  * <ul>
  * <li>Tolerance breach markers</li>
  * <li>Red Amber Green state (RAG)</li>
  * <li>Artifact count which are monitored</li>
  * </ul>
  * </br> Depending on a context. The context can be broad, encompassing several
  * monitoring layers. </p> the following "layers" are defined: </p>
  * <ul>
  * <li>{@link Operator operator monitoring}</li>
  * <li>{@link RFSService service monitoring}</li>
  * <li>{@link Node node monitoring}</li>
  * <li>{@link Component Funtion or Equipment monitoring}</li>
  * <li>{@link NetXResource resource monitoring}</li>
  * </ul>
  * Ultimately, the state on all layers is computed from the markers on a
  * {@link NetXResource resource}.</br> Currently, the {@link ToleranceMarker
  * tolerance markers} are the base to determine the state of a resource.</br>
  * The state will only be (fully) computed if the context is present for
  * computation. </p> The following context objects are required for a full
  * computation.
  * <ul>
  * <li>The {@link RFSService} (As it contains the {@link ServiceMonitor} holding
  * information about monitoring</li>
  * <li>The monitoring period</li>
  * </ul>
  * Clients wishing to use the {@link MonitoringStateModel} should call one of
  * the following </p> {@link MonitoringStateModel#summary(Object)}
  * 
  * @author Christophe Bouhier
  */
 @Singleton
 public class MonitoringStateModel {
 
 	/**
 	 * In this mode an ongoing computation is not cancelled, but an additional
 	 * job is scheduled. A new {@link MonitoringStateJob} is instantiated and
 	 * scheduled for launch with no delay. (The default mode has a delay of 100
 	 * ms).</p> Note: It's not possible to add a listener to job lifecycle in
 	 * this mode. (It could be by registration an IJobChangeListener, which is
 	 * added to each produced job).
 	 */
 	public static final int MONITOR_COMPUTATION_REPETITIVE_MODE = 100;
 
 	/**
 	 * In this mode an ongoing computation is cancelled. The computaton delay is
 	 * 100 ms.
 	 */
 	public static final int MONITOR_COMPUTATION_SINGLE_MODE = 200;
 
 	/**
 	 * A Call back interface when computing is done.
 	 * 
 	 * @author Christophe Bouhier
 	 * 
 	 */
 	public interface MonitoringStateCallBack {
 		public void callBackEvent(MonitoringStateEvent event);
 	}
 
 	/**
 	 * A job for monitoring computation.
 	 * 
 	 * @author Christophe Bouhier
 	 */
	public class MonitoringStateJob extends Job {
 
 		/** Our context which will determine which Summary is returned. */
 		private Object target;
 
 		/** the produced summary for this job. */
 		private IMonitoringSummary summary;
 
 		private boolean running = false;
 
 		private IComputationContext[] contextObjects;
 
 		/**
 		 * A specialized call back handler, when a computing job completed.
 		 * Others can subscribe to the job status to be notified and process
 		 * accordingly.
 		 * 
 		 */
 		private JobCallBack callBackHandler;
 
 		public MonitoringStateJob() {
 			super("Creating Summary...");
 			callBackHandler = new JobCallBack();
 			addJobChangeListener(callBackHandler);
 			setUser(true);
 		}
 
 		public void go() {
 			schedule(100);
 		}
 
 		public void goNow() {
 			schedule();
 		}
 
 		public void go(int delay) {
 			schedule(delay);
 		}
 
 		public void addNotifier(IJobChangeListener notifier) {
 			addJobChangeListener(notifier);
 		}
 
 		public void removeNotifier(IJobChangeListener notifier) {
 			removeJobChangeListener(notifier);
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 
 			processReadingInternal(monitor);
 			return Status.OK_STATUS;
 		}
 
 		protected void processReadingInternal(final IProgressMonitor monitor) {
 			if (CommonActivator.DEBUG) {
 				CommonActivator.TRACE
 						.trace(CommonActivator.TRACE_COMMON_MONITORING_OPTION,
 								" creating summary for:"
 										+ modelUtils
 												.printModelObject((EObject) target));
 			}
 			summary = summary(monitor, target, contextObjects);
 		}
 
 		public void done(IJobChangeEvent event) {
 			running = false;
 		}
 
 		public void running(IJobChangeEvent event) {
 			this.running = true;
 		}
 
 		/**
 		 * @return the summary
 		 */
 		public IMonitoringSummary getMonitoringSummary() {
 			return summary;
 		}
 
 		/**
 		 * @return the running
 		 */
 		public boolean isRunning() {
 			return running;
 		}
 
 		public void setContextObjects(IComputationContext... contextObjects) {
 			this.contextObjects = contextObjects;
 		}
 
 		public void setTarget(Object context) {
 			this.target = context;
 		}
 
 		public JobCallBack getCallBackHandler() {
 			return callBackHandler;
 		}
 
 	}
 
 	@Inject
 	protected ModelUtils modelUtils;
 
 	private MonitoringStateJob job = new MonitoringStateJob();
 
 	/** Our adapter factory for monitoring **/
 	@Inject
 	private MonitoringAdapterFactory monAdapterFactory;
 
 	public void summary(MonitoringStateCallBack callBack, Object target) {
 		prepSummary(callBack, target, MONITOR_COMPUTATION_SINGLE_MODE,
 				(IComputationContext[]) null);
 	}
 
 	/**
 	 * Produce a summary for the target object. When done,
	 * {@link MonitoringStateCallBack#callBackEvent(MonitoringStateEvent)} is
	 * called.
 	 * 
 	 * @param callBack
 	 * @param target
 	 * @param computeMode
 	 */
 	public void summary(MonitoringStateCallBack callBack, Object target,
 			int computationMode) {
 		prepSummary(callBack, target, computationMode,
 				(IComputationContext[]) null);
 	}
 
 	/**
 	 * Produce an {@link IMonitoringSummary summary} for a give target and
 	 * context.
 	 * 
 	 * @param callBack
 	 * @param target
 	 * @param contextObjects
 	 */
 	public void summary(MonitoringStateCallBack callBack, Object target,
 			IComputationContext... contextObjects) {
 
 		Assert.isNotNull(target);
 		Assert.isNotNull(contextObjects);
 
 		prepSummary(callBack, target, MONITOR_COMPUTATION_SINGLE_MODE,
 				contextObjects);
 	}
 
 	/**
 	 * Make sure we access the monitoring job object in a synchronized fashion.
 	 * 
 	 * @return
 	 */
 	private synchronized MonitoringStateJob getJob() {
 		return job;
 	}
 
 	class JobCallBack extends JobChangeAdapter {
 
 		private MonitoringStateCallBack callBack;
 
 		public void setCallBack(MonitoringStateCallBack callBack) {
 			this.callBack = callBack;
 		}
 
 		@Override
 		public void done(IJobChangeEvent event) {
 			if (callBack != null) {
 				Job job = event.getJob();
 				if (job instanceof MonitoringStateJob) {
 					final MonitoringStateEvent monitoringStateEvent = new MonitoringStateEvent();
					monitoringStateEvent.setResult(((MonitoringStateJob) job)
							.getMonitoringSummary());
 					callBack.callBackEvent(monitoringStateEvent);
 				}
 			}
 		}
 
 	}
 
 	public MonitoringAdapterFactory getMonAdapterFactory() {
 		return monAdapterFactory;
 	}
 
 	/**
 	 * Prepares the monitoring state.
 	 */
 	private void prepSummary(final MonitoringStateCallBack callBack,
 			Object target, int computationMode,
 			IComputationContext... contextObjects) {
 
 		MonitoringStateJob job = null;
 
 		switch (computationMode) {
 
 		case MONITOR_COMPUTATION_SINGLE_MODE: {
 			// Force a restart, if we are operational.
 			if (getJob().isRunning()) {
 
 				if (CommonActivator.DEBUG) {
 					CommonActivator.TRACE.trace(
 							CommonActivator.TRACE_COMMON_MONITORING_OPTION,
 							"Cancel monitoring production");
 				}
 				// This will abrupt the job but on demand, so we can't really
 				// start
 				// a new job here.
 				getJob().cancel();
 			}
 
 			job = getJob();
 		}
 			break;
 		case MONITOR_COMPUTATION_REPETITIVE_MODE: {
 			job = new MonitoringStateJob();
 		}
 			break;
 		}
 
 		if (job != null) {
 			job.setTarget(target);
 			job.setContextObjects(contextObjects);
 			// Make sure we set the correct callback, Don't use final arguments
 			// for
 			// callback!!!!! It will cost you 2 more hours to debug :-)
 			job.getCallBackHandler().setCallBack(callBack);
 
 			if (computationMode == MONITOR_COMPUTATION_REPETITIVE_MODE) {
 				job.go(1000);
 			} else {
 				job.go(); // Should spawn a job processing the xls.
 			}
 		}
 
 	}
 
 	/**
 	 * Allow external clients to be notified on the job progress.
 	 */
 	public void addJobNotifier(IJobChangeListener listener) {
 		if (getJob() != null) {
 			getJob().addNotifier(listener);
 		}
 	}
 
 	/**
 	 * Allow external clients to be notified on the job progress.
 	 */
 	public void removeJobNotifier(IJobChangeListener listener) {
 		if (getJob() != null) {
 			getJob().removeNotifier(listener);
 		}
 	}
 
 	// PROCESSING STATE:
 
 	/**
 	 * Is any of the background processing jobs running?
 	 * 
 	 * @return
 	 */
 	public boolean isRunning() {
 		return getJob().isRunning();
 	}
 
 	/**
 	 * Abort background processing jobs.
 	 */
 	public void abort() {
 		if (getJob().isRunning()) {
 			getJob().cancel();
 		}
 	}
 
 	/**
 	 * Get a summary, without computation.
 	 * 
 	 * @param target
 	 * @return
 	 */
 	public IMonitoringSummary summary(Object target) {
 		final IMonitoringSummary adapt = (IMonitoringSummary) monAdapterFactory
 				.adapt(target, IMonitoringSummary.class);
 		return adapt;
 
 	}
 
 	public IMonitoringSummary summary(IProgressMonitor monitor, Object target,
 			IComputationContext... context) {

 		final IMonitoringSummary adapt;
 		if (!isAdapted((EObject) target)) {
 			// Adapt for the target, note this will also self-adapt the
 			// contained
 			// (loaded) children.
 			adapt = (IMonitoringSummary) monAdapterFactory.adapt(target,
 					IMonitoringSummary.class);
 		} else {
 			adapt = getAdapted((EObject) target);
 		}
 
 		// Non adapted objects will be null
 		if (adapt == null) {
 			return adapt;
 		}
 
 		adapt.clearContextObject();
 
 		// Add the context object, guard for null, as we might do a computation
 		// without a context.
 		if (context != null) {
 			adapt.addContextObjects(context);
 		}
 
 		// Do some initial computation, note this will auto compute the
 		// adapted children. Non-adapted children will
 		adapt.compute(monitor);
 
 		if (CommonActivator.DEBUG) {
 			CommonActivator.TRACE.trace(
 					CommonActivator.TRACE_COMMON_MONITORING_OPTION,
 					"result adapter: " + adapt);
 		}
 
 		return adapt;
 
 	}
 
 	/**
 	 * Count the RAG per NetXResource's markers. Note: Resources which are not
 	 * referenced by a Resource Monitor from the specified node, are ignored.
 	 * 
 	 * @param markersPerResource
 	 * 
 	 * @param sm
 	 * @param n
 	 * @return
 	 */
 	public int[] ragCountResourcesForNode(
 			Map<NetXResource, List<Marker>> markersPerResource,
 			IProgressMonitor monitor) {
 
 		int red = 0, amber = 0, green = 0;
 
 		for (NetXResource res : markersPerResource.keySet()) {
 
 			if (monitor != null && monitor.isCanceled()) {
 				return new int[] { red, amber, green };
 			}
 			List<Marker> markers = markersPerResource.get(res);
 			int[] rag = ragForMarkers(markers);
 			red += rag[0];
 			amber += rag[1];
 			green += rag[2];
 
 		}
 
 		return new int[] { red, amber, green };
 	}
 
 	/**
 	 * @param rm
 	 * @return
 	 */
 	private List<Marker> toleranceMarkersForResourceMonitor(ResourceMonitor rm) {
 		final List<Marker> toleranceMarkers = Lists.newArrayList(Iterables
 				.filter(rm.getMarkers(), modelUtils.toleranceMarkers()));
 		return toleranceMarkers;
 	}
 
 	/**
 	 * For a collection of marker determine the Red Amber Green status. Higher
 	 * levels take precedence over lower levels. Lower levels are in case
 	 * preceded by a Higher level, cleared.
 	 * 
 	 * @param markersForNodeList
 	 * @return
 	 */
 	public int[] ragForMarkers(List<Marker> markersForNodeList) {
 
 		int red = 0, amber = 0, green = 0;
 		Marker[] markerForNodeArray = new Marker[markersForNodeList.size()];
 		markersForNodeList.toArray(markerForNodeArray);
 		// Iterate markers per level.
 		for (LevelKind lk : LevelKind.VALUES) {
 
 			final ToleranceMarker tm = lastToleranceMarker(lk,
 					markerForNodeArray);
 			if (tm != null) {
 				switch (tm.getLevel().getValue()) {
 				case LevelKind.RED_VALUE: {
 					if (isStartOrUp(tm)) {
 						red++;
 					}
 				}
 					break;
 				case LevelKind.AMBER_VALUE: {
 					if (isStartOrUp(tm)) {
 						amber++;
 					}
 				}
 					break;
 				case LevelKind.GREEN_VALUE: {
 					if (isStartOrUp(tm)) {
 						green++;
 					}
 				}
 				case LevelKind.YELLOW_VALUE: {
 					// what to do with yellow??
 				}
 					break;
 				}
 			}
 		}
 
		// Clear or set the correct level based on the result. 
 		if (red > 0) {
 			amber = 0;
 			green = 0;
		} else if (amber > 0) {
 			green = 0;
		}else {
			green = 1;
 		}
 
 		return new int[] { red, amber, green };
 	}
 
 	/**
 	 * Return the last marker which is either START or UP. The lists is sorted
 	 * and analyzed from the tail. (Newest first). Return null, if we can't find
 	 * a marker matching the Level Kind.
 	 * 
 	 * @param lk
 	 * @param markers
 	 * @return
 	 */
 	public ToleranceMarker lastToleranceMarker(LevelKind lk, Marker... markers) {
 		ToleranceMarker tm = null;
 		List<Marker> markerList = Lists.newArrayList(markers);
 		markerList = modelUtils.sortMarkersByTimeStamp(markerList);
 		Collections.reverse(markerList);
 		for (Marker m : markerList) {
 			if (m instanceof ToleranceMarker
 					&& ((ToleranceMarker) m).getLevel() == lk) {
 				tm = (ToleranceMarker) m;
 				break;
 			}
 		}
 		return tm;
 	}
 
 	public boolean isStartOrUp(ToleranceMarker tm) {
 		return tm.getDirection() == ToleranceMarkerDirectionKind.UP
 				|| tm.getDirection() == ToleranceMarkerDirectionKind.START;
 	}
 
 	/**
 	 * Overall RAG Status.
 	 * 
 	 * @param sm
 	 * @return
 	 * @deprecated DO NOT USE, no distinction per NetXResource.
 	 */
 	public int[] ragCountResources(ServiceMonitor sm) {
 
 		int red = 0, amber = 0, green = 0;
 
 		for (ResourceMonitor rm : sm.getResourceMonitors()) {
 			int[] rag = ragForMarkers(rm.getMarkers());
 			red += rag[0];
 			amber += rag[1];
 			green += rag[2];
 		}
 		return new int[] { red, amber, green };
 
 	}
 
 	public boolean ragShouldReport(int[] ragStatus) {
 		if (ragStatus.length != 3) {
 			return false;
 		}
 
 		if (ragStatus[0] > 0) {
 			return true;
 		}
 
 		if (ragStatus[1] > 0) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * The {@link ToleranceMarker} which belong to the given {@link List
 	 * ServiceMonitor service monitors} and the given {@link NetXResource}.
 	 * 
 	 * @param serviceMonitors
 	 * @param netxRes
 	 * @param monitor
 	 * @return
 	 */
 	public List<Marker> toleranceMarkersForServiceMonitorsAndResource(
 			List<ServiceMonitor> serviceMonitors, NetXResource netxRes,
 			IProgressMonitor monitor) {
 
 		final List<Marker> markers = Lists.newArrayList();
 
 		for (ServiceMonitor sm : serviceMonitors) {
 			for (ResourceMonitor rm : sm.getResourceMonitors()) {
 
 				// Abort the task if we are cancelled.
 				if (monitor != null && monitor.isCanceled()) {
 					return markers;
 				}
 
 				if (rm.getResourceRef() == netxRes) {
 					markers.addAll(toleranceMarkersForResourceMonitor(rm));
 				}
 			}
 		}
 		return markers;
 	}
 
 	public static boolean isAdapted(EObject object) {
 		boolean isAdapted = false;
 		for (Adapter a : object.eAdapters()) {
 			if (a instanceof IMonitoringSummary) {
 				isAdapted = true;
 			}
 		}
 		return isAdapted;
 	}
 
 	public static IMonitoringSummary getAdapted(EObject object) {
 		for (Adapter a : object.eAdapters()) {
 			if (a instanceof IMonitoringSummary) {
 				return (IMonitoringSummary) a;
 			}
 		}
 		return null;
 	}
 
 }
