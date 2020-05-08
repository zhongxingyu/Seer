 /*******************************************************************************
  * Copyright (c) May 20, 2011 NetXForge.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  You should have received a copy of the GNU Lesser General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Martin Taal - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.server.job;
 
 import static org.quartz.CalendarIntervalScheduleBuilder.calendarIntervalSchedule;
 import static org.quartz.SimpleScheduleBuilder.repeatSecondlyForTotalCount;
 import static org.quartz.SimpleScheduleBuilder.repeatSecondlyForever;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.cdo.CDOObject;
 import org.eclipse.emf.cdo.common.id.CDOID;
 import org.eclipse.emf.cdo.eresource.CDOResource;
 import org.eclipse.emf.cdo.server.IRepository;
 import org.eclipse.emf.cdo.transaction.CDOTransaction;
 import org.eclipse.emf.cdo.view.CDOAdapterPolicy;
 import org.eclipse.emf.cdo.view.CDOView;
 import org.eclipse.emf.cdo.view.CDOViewInvalidationEvent;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.net4j.util.container.IElementProcessor;
 import org.eclipse.net4j.util.container.IManagedContainer;
 import org.eclipse.net4j.util.event.IListener;
 import org.eclipse.net4j.util.lifecycle.ILifecycleEvent;
 import org.eclipse.net4j.util.lifecycle.ILifecycleEvent.Kind;
 import org.eclipse.net4j.util.lifecycle.LifecycleEventAdapter;
 import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
 import org.eclipse.osgi.framework.console.CommandInterpreter;
 import org.quartz.JobBuilder;
 import org.quartz.JobDataMap;
 import org.quartz.JobDetail;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobKey;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.SimpleScheduleBuilder;
 import org.quartz.Trigger;
 import org.quartz.Trigger.TriggerState;
 import org.quartz.TriggerBuilder;
 import org.quartz.TriggerKey;
 import org.quartz.impl.StdSchedulerFactory;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ordering;
 import com.google.inject.Inject;
 import com.netxforge.netxstudio.common.model.ModelUtils;
 import com.netxforge.netxstudio.data.IDataProvider;
 import com.netxforge.netxstudio.scheduling.Job;
 import com.netxforge.netxstudio.scheduling.JobRunContainer;
 import com.netxforge.netxstudio.scheduling.JobState;
 import com.netxforge.netxstudio.scheduling.MetricSourceJob;
 import com.netxforge.netxstudio.scheduling.SchedulingPackage;
 import com.netxforge.netxstudio.server.IDPProvider;
 import com.netxforge.netxstudio.server.IServerUtils;
 import com.netxforge.netxstudio.server.Server;
 import com.netxforge.netxstudio.server.job.internal.JobActivator;
 
 /**
  * Handles jobs, reads the jobs from the database, initializes quartz and
  * re-initializes if anything changes in the database.
  * 
  * Provides external API for the OSGI command interpreter or remote calls.
  * 
  * @author Martin Taal
  * @author Christophe Bouhier
  */
 public class JobHandler {
 
 	private static JobHandler INSTANCE;
 
 	/*
 	 * Our own instance of IDataProvider. We maintain a view on this provider.
 	 */
 	@Inject
 	@Server
 	private IDPProvider dpProvider;
 
 	private IDataProvider dataProvider;
 
 	@Inject
 	private ModelUtils modelUtils;
 
 	// Our quartz scheduler.
 	private Scheduler scheduler;
 
 	/*
 	 * A state flag, if we are currently initializing.
 	 */
 	private boolean initializing = false;
 
 	// contains all the ids which are relevant to know when creating
 	// removing or updating jobs
 	private List<CDOID> relevantIds = new ArrayList<CDOID>();
 
 	// Map of JobKeys for our CDO Object ID's.
 	// private Map<CDOID, JobKey> jobKeysMap = new HashMap<CDOID, JobKey>();
 
 	// Map of TriggerKeys for our CDO Object ID's.
 	private Map<CDOID, TriggerKey> triggerKeysMap = new HashMap<CDOID, TriggerKey>();
 
 	private CDOView view;
 
 	static void createAndInitialize() {
 		INSTANCE = new JobHandler();
 		INSTANCE.activate();
 		INSTANCE.initialize();
 	}
 
 	public static void deActivate() {
 		if (INSTANCE != null) {
 			INSTANCE.deActivateInstance();
 		}
 		INSTANCE = null;
 	}
 
 	/**
 	 * Start the scheduler.
 	 */
 	public static void status() {
 		status(null);
 	}
 
 	public static void status(CommandInterpreter interpreter) {
 		if (INSTANCE != null) {
 			if (interpreter != null) {
 				interpreter.println(INSTANCE.statusScheduler());
 			} else {
 				System.out.println(INSTANCE.statusScheduler());
 			}
 		}
 	}
 
 	/**
 	 * Start the scheduler.
 	 */
 	public static void start() {
 		start(null);
 	}
 
 	public static void start(CommandInterpreter interpreter) {
 		if (INSTANCE != null) {
 			if (interpreter != null) {
 				interpreter.println(INSTANCE.startScheduler());
 			} else {
 				System.out.println(INSTANCE.startScheduler());
 			}
 		}
 	}
 
 	/**
 	 * Stop the scheduler.
 	 */
 	public static void stop() {
 		stop(null);
 	}
 
 	public static void stop(CommandInterpreter interpreter) {
 		if (INSTANCE != null) {
 			if (interpreter != null) {
 				interpreter.println(INSTANCE.stopScheduler());
 			} else {
 				System.out.println(INSTANCE.stopScheduler());
 			}
 		}
 	}
 
 	/**
 	 * Clean job data
 	 */
 	public static void clean() {
 		clean(null);
 	}
 
 	public static void clean(CommandInterpreter interpreter) {
 		if (interpreter != null && INSTANCE != null) {
 			interpreter.println(INSTANCE.cleanJobRuns());
 		} else {
 			System.out.println(INSTANCE.cleanJobRuns());
 		}
 	}
 
 	private synchronized String cleanJobRuns() {
 
 		return "Not implemented";
 	}
 
 	/**
 	 * list the scheduled jobs to System.out
 	 */
 	public static void scheduled() {
 		scheduled(null);
 	}
 
 	public static void scheduled(CommandInterpreter interpreter) {
 		if (interpreter != null && INSTANCE != null) {
 			interpreter.println(INSTANCE.listScheduler());
 		} else {
 			System.out.println(INSTANCE.listScheduler());
 		}
 	}
 
 	/**
 	 * list the scheduled jobs to System.out
 	 */
 	public static void list() {
 		list(null);
 	}
 
 	public static void list(CommandInterpreter interpreter) {
 		if (interpreter != null && INSTANCE != null) {
 			interpreter.println(INSTANCE.listJobs());
 		} else {
 			System.out.println(INSTANCE.listJobs());
 		}
 	}
 
 	/**
 	 * Pause all jobs.
 	 */
 	public static void pauseAll() {
 		pauseAll(null);
 	}
 
 	public static void pauseAll(CommandInterpreter interpreter) {
 		if (interpreter != null && INSTANCE != null) {
 			interpreter.println(INSTANCE.pauseAllScheduler());
 		}
 	}
 
 	/**
 	 * Resume all jobs
 	 */
 	public static void resumeAll() {
 		resumeAll(null);
 	}
 
 	public static void resumeAll(CommandInterpreter interpreter) {
 		if (interpreter != null && INSTANCE != null) {
 			interpreter.println(INSTANCE.resumeAllScheduler());
 		}
 	}
 
 	/**
 	 * @return the initializing
 	 */
 	public boolean isInitializing() {
 		return initializing;
 	}
 
 	/**
 	 * We could very well be reading jobs and containers which are either
 	 * initialized or updated for a running job, we don't obtain locks.
 	 * 
 	 * @return
 	 */
 	private String listJobs() {
 
 		final StringBuffer sb = new StringBuffer();
 
 		// Append the header.
 		sb.append(modelUtils.fixedLenthString("Index", 10));
 		sb.append(modelUtils.fixedLenthString("| Name", 50));
 		sb.append(modelUtils.fixedLenthString("| State", 15));
 		sb.append(modelUtils.fixedLenthString("| Repeat", 10));
 		sb.append(modelUtils.fixedLenthString("| Interval", 10));
 		sb.append(modelUtils.fixedLenthString("| Start", 30));
 		sb.append(modelUtils.fixedLenthString("| End", 30));
 		sb.append(modelUtils.fixedLenthString("| # Runs", 30));
 		sb.append("\n");
 
 		final CDOResource jobRunContainerResource = (CDOResource) dataProvider
 				.getResource(view, SchedulingPackage.Literals.JOB_RUN_CONTAINER);
 		final List<Job> sortedJobs = sortedJobs(view);
 
 		for (final Job job : sortedJobs) {
 			sb.append(modelUtils.fixedLenthString(
 					new Integer(sortedJobs.indexOf(job)).toString(), 10));
 			sb.append(modelUtils.fixedLenthString("| " + job.getName(), 50));
 			sb.append(modelUtils.fixedLenthString("| "
 					+ job.getJobState().getName(), 15));
 			sb.append(modelUtils.fixedLenthString("| " + job.getRepeat(), 10));
 			sb.append(modelUtils.fixedLenthString("| " + job.getInterval(), 10));
 			sb.append(modelUtils.fixedLenthString("| " + job.getStartTime(), 30));
 			sb.append(modelUtils.fixedLenthString("| " + job.getEndTime(), 30));
 
 			JobRunContainer container = this.getContainer(
 					jobRunContainerResource, job);
 			if (container != null) {
 				sb.append(modelUtils.fixedLenthString("| "
 						+ container.getWorkFlowRuns().size() + " times", 30));
 			}
 			sb.append("\n");
 		}
 		return sb.toString();
 	}
 
 	private synchronized String statusScheduler() {
 		if (isInitializing()) {
 			return "Scheduler initializing";
 		} else {
 			try {
 				if (scheduler != null) {
 					if (scheduler.isStarted()) {
 						if (scheduler.isInStandbyMode()) {
 							return "Scheduler is on standby";
 						} else {
 							return "Scheduler started";
 						}
 					} else if (scheduler.isShutdown()) {
 						return "Scheduler is stopped";
 					}
 				} else {
 					return "Scheduler is not initialized";
 				}
 			} catch (SchedulerException e) {
 				e.printStackTrace();
 			}
 		}
 		return "Scheduler never started";
 	}
 
 	private synchronized String startScheduler() {
 		if (isInitializing()) {
 			return "Currently initializing, sorry";
 		}
 
 		StringBuffer sb = new StringBuffer();
 		try {
 			if (scheduler != null) {
 				if (scheduler.isStarted()) {
 					if (scheduler.isInStandbyMode()) {
 						scheduler.start();
 						sb.append("Scheduler started...");
 					} else {
 						sb.append("Scheduler is started already...");
 					}
 				} else {
 					scheduler.start();
 					sb.append("Scheduler started...");
 				}
 			} else {
 				initialize(null);
 				return "The scheduler is now initializing, but this could fail, try again starting it";
 			}
 		} catch (SchedulerException e) {
 			e.printStackTrace();
 		}
 		return sb.toString();
 	}
 
 	private synchronized String stopScheduler() {
 		if (isInitializing()) {
 			return "Currently initializing, sorry";
 		}
 
 		StringBuffer sb = new StringBuffer();
 		try {
 			if (scheduler != null && scheduler.isStarted()) {
 				scheduler.standby();
 				sb.append("Scheduler stopped (on standby)");
 			} else {
 				sb.append("Scheduler is stopped already...");
 			}
 		} catch (SchedulerException e) {
 			e.printStackTrace();
 		}
 		return sb.toString();
 	}
 
 	private synchronized String listScheduler() {
 		if (isInitializing()) {
 			return "Currently initializing, sorry";
 		}
 
 		StringBuffer sb = new StringBuffer();
 		try {
 			if (scheduler != null) {
 
 				// print the scheduler status first.
 				sb.append("Current scheduler status=" + statusScheduler()
 						+ "\n");
 
 				// currently executing jobs.
 				sb.append("Current running scheduled jobs = "
 						+ scheduler.getCurrentlyExecutingJobs().size() + "\n");
 
 				for (JobExecutionContext context : scheduler
 						.getCurrentlyExecutingJobs()) {
 					sb.append("=> Job with ID:"
 							+ context.getJobDetail().getDescription() + "\n");
 				}
 
 				// current triggers, if the scheduler is stopped, we still have
 				// triggers
 				// but the date will be wrong.
 				sb.append("Current triggers for jobs\n");
 
 				List<TriggerKey> keysToRemove = new ArrayList<TriggerKey>();
 				for (TriggerKey tKey : triggerKeysMap.values()) {
 					if (!scheduler.checkExists(tKey)) {
 						keysToRemove.add(tKey);
 						continue;
 					}
 					Trigger trigger = scheduler.getTrigger(tKey);
 					sb.append(printTrigger(tKey, trigger));
 				}
 
 				for (TriggerKey tKey : keysToRemove) {
 					triggerKeysMap.remove(tKey);
 				}
 			} else {
 				return "The scheduler is not initialized, try to initialize it with : job start";
 			}
 		} catch (SchedulerException e) {
 			e.printStackTrace();
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * @param trigger
 	 */
 	private String printTrigger(TriggerKey tKey, Trigger trigger) {
 
 		StringBuffer sb = new StringBuffer();
 		Date nextFireTime = trigger.getNextFireTime();
 
 		try {
 			JobDetail detail = scheduler.getJobDetail(trigger.getJobKey());
 
 			sb.append("=> Job with ID: " + detail.getDescription()
 					+ " will fire next time " + nextFireTime);
 			TriggerState triggerState = scheduler.getTriggerState(tKey);
 			sb.append(", state=" + triggerState.name() + "\n");
 		} catch (SchedulerException e) {
 			e.printStackTrace();
 		}
 
 		return sb.toString();
 	}
 
 	/**
 	 * Pauzes the scheduler. Any notifications will not be processed while
 	 * pauzed.
 	 */
 	private synchronized String pauseAllScheduler() {
 		if (isInitializing()) {
 			return "Currently initializing, sorry";
 		}
 
 		StringBuffer sb = new StringBuffer();
 		if (scheduler != null) {
 			try {
 				if (scheduler.isStarted()) {
 					scheduler.pauseAll();
 				} else {
 					sb.append("Scheduler is not started");
 				}
 			} catch (SchedulerException e) {
 				e.printStackTrace();
 
 			}
 		}
 		return sb.toString();
 	}
 
 	private synchronized String resumeAllScheduler() {
 		if (isInitializing()) {
 			return "Currently initializing, sorry";
 		}
 
 		StringBuffer sb = new StringBuffer();
 		try {
 			scheduler.resumeAll();
 		} catch (SchedulerException e) {
 			e.printStackTrace();
 		}
 		return sb.toString();
 	};
 
 	public synchronized void initialize() {
 		this.initialize(null);
 	};
 
 	private void addChangeSubscription(CDOView view) {
 
 		boolean doAdd = true;
 		for (CDOAdapterPolicy cdoa : view.options()
 				.getChangeSubscriptionPolicies()) {
 			if (cdoa == CDOAdapterPolicy.CDO) {
 				doAdd = false;
 			}
 
 		}
 		if (doAdd) {
 			view.options().addChangeSubscriptionPolicy(CDOAdapterPolicy.CDO);
 		}
 
 	}
 
 	/**
 	 * Called with the repository starts and when an invalidation occurs on
 	 */
 	public synchronized void initialize(CDOObject change) {
 
 		// monitor, dis-allowing re-entry.
 		initializing = true;
 
 		// Our session, view might not be available....
 		if (view == null || (view != null && !LifecycleUtil.isActive(view))) {
 			// Re-activate.
 			activate();
 		}
 
 		if (JobActivator.DEBUG) {
 			JobActivator.TRACE.trace(JobActivator.TRACE_JOBS_OPTION,
 					"(Re) Initializing jobs");
 
 		}
 
 		try {
 
 			final Resource jobResource = dataProvider.getResource(view,
 					SchedulingPackage.eINSTANCE.getJob());
 
 			if (jobResource == null) {
 				return;
 			}
 
 			relevantIds.clear();
 			triggerKeysMap.clear();
 
 			relevantIds.add(((CDOResource) jobResource).cdoID());
 
 			// CB 26062012, this would only imply that objects in this view
 			// notify,
 			// when invalidated.
 			// It's likely not needed.
 			// view.options().setInvalidationNotificationEnabled(true);
 			// view.options().setInvalidationPolicy(
 			// CDOInvalidationPolicy.DEFAULT);
 
 			if (scheduler == null) {
 				// Instantiate only once.
 				scheduler = StdSchedulerFactory.getDefaultScheduler();
 			} else {
 				scheduler.clear();
 			}
 
 			// if (scheduler != null && !scheduler.isShutdown()) {
 			// // We force a shutdown of the scheduler when initializing.
 			// // report any ongoing jobs.
 			// scheduler.shutdown(true);
 			// }
 
 			// clear the scheduler.
 
 			// now initialize quartz jobs, iterating through all available jobs.
 			for (final EObject eObject : jobResource.getContents()) {
 				final Job job = (Job) eObject;
 
 				relevantIds.add(job.cdoID());
 
 				if (job.getJobState() == JobState.IN_ACTIVE) {
 					continue;
 				}
 				if (job.isDeleted()) {
 					continue;
 				}
 				final int countJobRuns = countJobRuns(job);
 				if (job.getRepeat() > 0 && job.getRepeat() <= countJobRuns) {
 					continue;
 				}
 
 				final JobDataMap map = new JobDataMap();
 				map.put(NetxForgeJob.JOB_PARAMETER, job);
 
 				final String jobIdentity = job.cdoID().toString();
 
 				// Should possible group the jobs byt the type of job it is.
 				String jobGroupName = "NetXStudio job";
 				if (job instanceof MetricSourceJob) {
 					jobGroupName = " Metric source job";
 				}
 
 				// The job detail.
 				final JobDetail jobDetail = JobBuilder
 						.newJob(NetxForgeJob.class)
 						.withIdentity(jobIdentity, jobGroupName)
 						.withDescription(job.getName()).usingJobData(map)
 						.build();
 
 				// Keep a map of CDO job objects and the corresponding quartz
 				// job key.
 				// jobKeysMap.put(job.cdoID(), jobDetail.getKey());
 
 				Trigger newTrigger = this.createNewCalendarTrigger(job,
 						jobIdentity, jobGroupName, countJobRuns);
 
 				// Keep a map of CDO job objects and the corresponding quartz
 				// trigger key.
 				triggerKeysMap.put(job.cdoID(), newTrigger.getKey());
 
 				scheduler.scheduleJob(jobDetail, newTrigger);
 
 			}
 
 			// Do not start the scheduler.
 			// try {
 			// scheduler.start();
 			// } catch (final Exception e) {
 			// // TODO do some form of logging but don't stop everything
 			// e.printStackTrace(System.err);
 			// }
 
 			// CB Keep it open, we need for invalidation events.
 			// dataProvider.closeView();
 		} catch (final Exception e) {
 			if (JobActivator.DEBUG) {
 				JobActivator.TRACE.trace(JobActivator.TRACE_JOBS_OPTION,
 						"Error initializing", e);
 			}
 		} finally {
 			initializing = false;
 		}
 	}
 
 	/*
 	 * Create a trigger. Set the start time. Set the end time, if any specified.
 	 * Set the interval, no smaller then 10 seconds. Set the repeat, considering
 	 * the current job runs.
 	 */
 	@SuppressWarnings("unused")
 	private Trigger createNewTrigger(Job job, String jobIdentity,
 			String jobGroupName, int countJobRuns) {
 		// The trigger for this job.
 		TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
 				.withIdentity(jobIdentity, jobGroupName);
 
 		if (job.getStartTime() != null) {
 			triggerBuilder = triggerBuilder.startAt(job.getStartTime()
 					.toGregorianCalendar().getTime());
 		} else {
 			triggerBuilder = triggerBuilder.startNow();
 		}
 		final SimpleScheduleBuilder scheduleBuilder;
 		// If there is an end time.
 		if (job.getEndTime() != null) {
 			triggerBuilder.endAt(job.getEndTime().toGregorianCalendar()
 					.getTime());
 			scheduleBuilder = repeatSecondlyForever(job.getInterval() > 10 ? job
 					.getInterval() : 10);
 		} else if (job.getRepeat() > 0) { // if it should repeat.
 			scheduleBuilder = repeatSecondlyForTotalCount(job.getRepeat()
 					- countJobRuns, job.getInterval() > 10 ? job.getInterval()
 					: 10);
 		} else if (job.getInterval() > 10) { // if there is an interval > 10
 												// seconds.
 			scheduleBuilder = repeatSecondlyForever(job.getInterval());
 		} else { // Repeat every 10 seconds.
 			scheduleBuilder = repeatSecondlyForever(10);
 		}
 		final Trigger trigger = triggerBuilder.withSchedule(scheduleBuilder)
 				.build();
 		return trigger;
 	}
 
 	private Trigger createNewCalendarTrigger(Job job, String jobIdentity,
 			String jobGroupName, int countJobRuns) {
 		// The trigger for this job.
 		TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
 				.withIdentity(jobIdentity, jobGroupName);
 
 		if (job.getStartTime() != null) {
 			triggerBuilder = triggerBuilder.startAt(job.getStartTime()
 					.toGregorianCalendar().getTime());
 		} else {
 			triggerBuilder = triggerBuilder.startNow();
 		}
 
 		final Trigger trigger;
 		if (job.getEndTime() != null) {
 			triggerBuilder.endAt(job.getEndTime().toGregorianCalendar()
 					.getTime());
 
 			trigger = triggerBuilder.withSchedule(
 					calendarIntervalSchedule().withIntervalInSeconds(
 							job.getInterval() > 10 ? job.getInterval() : 10)
 							.preserveHourOfDayAcrossDaylightSavings(true))
 					.build();
 		} else if (job.getRepeat() > 0) {
 			// We can't support CalendarInterval builder with repeat, so we use
 			// a SimpelSchedule builder, with fixed interval.
 			// Perhaps we can calculate the end time based on the repeat count.
 			trigger = triggerBuilder.withSchedule(
 					repeatSecondlyForTotalCount(job.getRepeat() - countJobRuns,
 							job.getInterval() > 10 ? job.getInterval() : 10))
 					.build();
 		} else if (job.getInterval() > 10) {
 			trigger = triggerBuilder.withSchedule(
					calendarIntervalSchedule()
							.preserveHourOfDayAcrossDaylightSavings(true)
							.withIntervalInSeconds(job.getInterval())).build();
 		} else {
 			trigger = triggerBuilder.withSchedule(
					calendarIntervalSchedule()
							.preserveHourOfDayAcrossDaylightSavings(true)
							.withIntervalInSeconds(10)).build();
 		}
 		return trigger;
 	}
 
 	/*
 	 * Create a trigger. Set the start time. Set the end time, if any specified.
 	 * Set the interval, no smaller then 10 seconds. Set the repeat, considering
 	 * the current job runs.
 	 */
 	@SuppressWarnings("unused")
 	private Trigger updateTrigger(TriggerBuilder<Trigger> tb, Job job,
 			int countJobRuns) {
 
 		// don't manipulate the start time.
 		// if (job.getStartTime() != null) {
 		// triggerBuilder = triggerBuilder.startAt(job.getStartTime()
 		// .toGregorianCalendar().getTime());
 		// } else {
 		// triggerBuilder = triggerBuilder.startNow();
 		// }
 
 		final SimpleScheduleBuilder scheduleBuilder;
 		if (job.getEndTime() != null) {
 			tb.endAt(job.getEndTime().toGregorianCalendar().getTime());
 			scheduleBuilder = SimpleScheduleBuilder.repeatSecondlyForever(job
 					.getInterval() > 10 ? job.getInterval() : 10);
 		} else if (job.getRepeat() > 0) {
 			scheduleBuilder = SimpleScheduleBuilder
 					.repeatSecondlyForTotalCount(
 							job.getRepeat() - countJobRuns,
 							job.getInterval() > 10 ? job.getInterval() : 10);
 		} else if (job.getInterval() > 10) {
 			scheduleBuilder = SimpleScheduleBuilder.repeatSecondlyForever(job
 					.getInterval());
 		} else {
 			scheduleBuilder = SimpleScheduleBuilder.repeatSecondlyForever(10);
 		}
 		final Trigger trigger = tb.withSchedule(scheduleBuilder).build();
 		return trigger;
 	}
 
 	/*
 	 * Call when any of the features of the job which affect the trigger is
 	 * updated by notification. uses an existing TriggerBuilder to reschedule
 	 * the job.
 	 */
 	@SuppressWarnings("unused")
 	private void updateTrigger(Job job) {
 
 		TriggerKey triggerKey = triggerKeysMap.get(job.cdoID());
 		if (triggerKey == null) {
 			return;
 		}
 
 		try {
 			if (scheduler != null && !scheduler.isShutdown()) {
 				Trigger trigger = scheduler.getTrigger(triggerKey);
 				// get the job key.
 				JobKey jobKey = trigger.getJobKey();
 
 				// TriggerBuilder<? extends Trigger> triggerBuilder =
 				// trigger.getTriggerBuilder();
 				// this.updateTrigger(triggerBuilder, job, countJobRuns);
 				//
 				// scheduler.rescheduleJob(triggerKey, null /* TODO our new
 				// trigger */);
 
 			}
 		} catch (SchedulerException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Active when a valid {@link CDOView} is assigned.
 	 */
 	private void activate() {
 		JobActivator.getInstance().getInjector().injectMembers(this);
 
 		dataProvider = dpProvider.get();
 
 		// This session and view remain active.
 		dataProvider.getSession();
 		view = dataProvider.getView();
 
 		// Is this really needed?
 		this.addChangeSubscription(view);
 
 		view.addListener(new IListener() {
 			public void notifyEvent(org.eclipse.net4j.util.event.IEvent arg0) {
 
 				boolean shoudInitialize = false;
 
 				if (arg0 instanceof CDOViewInvalidationEvent
 						&& !JobHandler.this.initializing) {
 					final CDOViewInvalidationEvent event = (CDOViewInvalidationEvent) arg0;
 					// only check changed objects, in case of:
 					// removal: the CDO resource is updated
 					// insert: the CDO resource is update
 					// update: the job object itself is updated
 
 					for (final CDOObject o : event.getDirtyObjects()) {
 						if (relevantIds.contains(o.cdoID())) {
 							shoudInitialize = true;
 						}
 					}
 				}
 				if (shoudInitialize) {
 					JobHandler.this.initialize();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Determine how many times a job has run, based on the entries in the job
 	 * runs.
 	 * 
 	 * @param view
 	 * 
 	 * @param job
 	 * @return
 	 */
 	private int countJobRuns(Job job) {
 		final CDOID cdoId = job.cdoID();
 		final Resource resource = dataProvider.getResource(view,
 				SchedulingPackage.eINSTANCE.getJobRunContainer());
 		for (final EObject eObject : resource.getContents()) {
 			final JobRunContainer container = (JobRunContainer) eObject;
 			final Job containerJob = container.getJob();
 			// sometimes container doesn't have a job??
 			if (containerJob != null) {
 				final CDOID containerJobId = ((CDOObject) containerJob).cdoID();
 				if (cdoId.equals(containerJobId)) {
 					return container.getWorkFlowRuns().size();
 				}
 			}
 		}
 		return 0;
 	}
 
 	private JobRunContainer getContainer(CDOResource containerResource, Job job) {
 		JobRunContainer jobContainer = null;
 
 		// find our jobcontainer .
 		for (final EObject eObject : containerResource.getContents()) {
 			final JobRunContainer container = (JobRunContainer) eObject;
 			final Job containerJob = container.getJob();
 			if (containerJob != null) {
 				final CDOID containerJobId = ((CDOObject) containerJob).cdoID();
 				if (job.cdoID().equals(containerJobId)) {
 					// Container found.
 					jobContainer = container;
 					break;
 				}
 			}
 		}
 		return jobContainer;
 	}
 
 	private void deActivateInstance() {
 		try {
 
 			if (scheduler != null) {
 				scheduler.shutdown();
 			}
 		} catch (final Exception e) {
 			throw new IllegalStateException(e);
 		} finally {
 			dataProvider.closeSession();
 		}
 	}
 
 	/**
 	 * An Initializer if this bundle is launched prior to the main server
 	 * bundle.
 	 * 
 	 * @author Christophe Bouhier
 	 */
 	public static class Initializer implements IElementProcessor {
 
 		public Object process(IManagedContainer container, String productGroup,
 				String factoryType, String description, Object element) {
 			if (element instanceof IRepository) {
 				final IRepository repository = (IRepository) element;
 				repository.addListener(new LifecycleEventAdapter() {
 					@Override
 					public void notifyLifecycleEvent(ILifecycleEvent event) {
 						if (event.getKind() == Kind.ACTIVATED) {
 
 							// In case the server is not initialized yet,
 							// we need to do this first.
 							// A better approach would be to
 							// register for the server services.
 							IServerUtils serverUtils = JobActivator
 									.getInstance().getInjector()
 									.getInstance(IServerUtils.class);
 
 							serverUtils.initializeServer(repository);
 
 							JobHandler.createAndInitialize();
 							// CB 26062012 disable the scheduler, to force a
 							// manual start of the scheduler.
 
 							// CB 12102012 Register ourselves as a service.
 							JobActivator.getContext().registerService(
 									JobHandler.class, JobHandler.INSTANCE,
 									new Hashtable<String, String>());
 
 							stop();
 						}
 					}
 				});
 			}
 			return element;
 		}
 	}
 
 	public static void activate(CommandInterpreter interpreter) {
 		if (interpreter != null && INSTANCE != null) {
 			interpreter.println(INSTANCE.activateJob(interpreter));
 		} else {
 			System.out.println(INSTANCE.activateJob(interpreter));
 		}
 	}
 
 	public static void deActivate(CommandInterpreter interpreter) {
 		if (interpreter != null && INSTANCE != null) {
 			interpreter.println(INSTANCE.deactivateJob(interpreter));
 		} else {
 			System.out.println(INSTANCE.deactivateJob(interpreter));
 		}
 	}
 
 	/**
 	 * @param interpreter
 	 * @return
 	 */
 	private String activateJob(CommandInterpreter interpreter) {
 		String nextArgument = interpreter.nextArgument();
 		String result = "";
 		if (nextArgument == null) {
 			return " Provide a job name to activate \n";
 		} else {
 			CDOTransaction transaction = dataProvider.getTransaction();
 			List<Job> jobTargets = Lists.newArrayList();
 			if (nextArgument.equals("all")) {
 				jobTargets = sortedJobs(transaction);
 			} else {
 				Job j;
 				try {
 					Integer integer = new Integer(nextArgument);
 					j = jobForIndex(transaction, integer);
 				} catch (NumberFormatException nfe) {
 					// look for the job to activate and do it.
 					j = jobForName(nextArgument);
 				}
 
 				if (j != null) {
 					jobTargets.add(j);
 				} else {
 					result = "Oops, job " + nextArgument + " doesn't exist.";
 				}
 			}
 			for (Job target : jobTargets) {
 				if (target.getJobState() == JobState.ACTIVE) {
 					result = "Job(s) is/are already active";
 				} else {
 					target.setJobState(JobState.ACTIVE);
 					result = "Job(s) is/are now activated";
 				}
 			}
 			dataProvider.commitTransactionThenClose();
 		}
 		return result;
 	}
 
 	private String deactivateJob(CommandInterpreter interpreter) {
 		String nextArgument = interpreter.nextArgument();
 		String result = "";
 		if (nextArgument == null) {
 			return " Provide a job name to deactivate or 'all' to de-activate all jobs\n";
 		} else {
 
 			CDOTransaction transaction = dataProvider.getTransaction();
 			List<Job> jobTargets = Lists.newArrayList();
 			if (nextArgument.equals("all")) {
 				jobTargets = sortedJobs(transaction);
 			} else {
 				Job j;
 				try {
 					Integer integer = new Integer(nextArgument);
 					j = jobForIndex(transaction, integer);
 				} catch (NumberFormatException nfe) {
 					// look for the job to activate and do it.
 					j = jobForName(nextArgument);
 				}
 
 				if (j != null) {
 					jobTargets.add(j);
 				} else {
 					result = "Oops, job " + nextArgument + " doesn't exist.";
 				}
 			}
 			for (Job target : jobTargets) {
 				if (target.getJobState() == JobState.IN_ACTIVE) {
 					result = "some job(s) is/are already de-actived";
 				} else {
 					target.setJobState(JobState.IN_ACTIVE);
 					result = "some job(s) is/are de-activated";
 				}
 			}
 			dataProvider.commitTransactionThenClose();
 		}
 		return result;
 	}
 
 	private Job jobForName(String jobName) {
 
 		CDOResource jobResource = (CDOResource) dataProvider.getResource(view,
 				SchedulingPackage.eINSTANCE.getJob());
 
 		// now initialize quartz jobs, iterating through all available jobs.
 		for (final EObject eObject : jobResource.getContents()) {
 			final Job job = (Job) eObject;
 			if (job.getName().equalsIgnoreCase(jobName)) {
 				return job;
 			}
 		}
 
 		return null;
 	}
 
 	private Job jobForIndex(CDOTransaction transaction, int index) {
 
 		List<Job> sortedJobs = sortedJobs(transaction);
 
 		if (sortedJobs.size() > index) {
 			return (Job) sortedJobs.get(index);
 		}
 		return null;
 	}
 
 	/**
 	 * @param v
 	 * @return
 	 */
 	private List<Job> sortedJobs(CDOView v) {
 		CDOResource jobResource = (CDOResource) dataProvider.getResource(v,
 				SchedulingPackage.eINSTANCE.getJob());
 		List<EObject> sortedJobs = Ordering
 				.from(modelUtils
 						.eFeatureComparator(SchedulingPackage.Literals.JOB__NAME))
 				.sortedCopy(jobResource.getContents());
 		return new ModelUtils.CollectionForObjects<Job>()
 				.collectionForObjects(sortedJobs);
 	}
 
 	/**
 	 * Expose the Quarts Scheduler.
 	 * 
 	 * @return
 	 */
 	public synchronized Scheduler getScheduler() {
 		return this.scheduler;
 	}
 
 }
