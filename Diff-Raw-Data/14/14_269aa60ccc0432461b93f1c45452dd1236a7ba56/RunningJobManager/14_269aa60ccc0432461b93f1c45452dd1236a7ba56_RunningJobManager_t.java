 package org.vpac.grisu.frontend.control.jobMonitoring;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.TreeSet;
 
 import org.apache.log4j.Logger;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.BatchJobException;
 import org.vpac.grisu.control.exceptions.NoSuchJobException;
 import org.vpac.grisu.frontend.model.job.BatchJobObject;
 import org.vpac.grisu.frontend.model.job.JobObject;
 import org.vpac.grisu.model.GrisuRegistryManager;
 import org.vpac.grisu.model.UserEnvironmentManager;
 import org.vpac.grisu.model.dto.DtoJob;
 import org.vpac.grisu.model.dto.DtoJobs;
 
 import ca.odell.glazedlists.BasicEventList;
 import ca.odell.glazedlists.EventList;
 
 public class RunningJobManager {
 
 	private class UpdateTimerTask extends TimerTask {
 
 		@Override
 		public void run() {
 
 			for (String application : cachedBatchJobsPerApplication.keySet()) {
 				updateBatchJobList(application);
 			}
 
 			for (BatchJobObject bj : getAllCurrentlyWatchedBatchJobs()) {
 				if (!bj.isFinished(false) && !bj.isRefreshing()) {
 					myLogger.debug("Refreshing job: " + bj.getJobname());
 					bj.refresh(true);
 				}
 			}
 
 			updateTimer.schedule(new UpdateTimerTask(),
 					UPDATE_TIME_IN_SECONDS * 1000);
 		}
 	}
 
 	static final Logger myLogger = Logger.getLogger(RunningJobManager.class
 			.getName());
 
 	public static void updateJobList(ServiceInterface si,
 			EventList<JobObject> jobObjectList, DtoJobs newJobs) {
 
 		Set<JobObject> toRemove = new HashSet<JobObject>();
 		Set<DtoJob> newJobsCopy = new HashSet<DtoJob>(newJobs.getAllJobs());
 
 		for (JobObject jo : jobObjectList) {
 			boolean inList = false;
 
 			for (DtoJob job : newJobs.getAllJobs()) {
 				if (jo.getJobname().equals(job.jobname())) {
 					inList = true;
 					jo.updateWithDtoJob(job);
 					newJobsCopy.remove(job);
 					break;
 				}
 			}
 			if (!inList) {
 				toRemove.add(jo);
 			}
 		}
		
		jobObjectList.removeAll(toRemove);
 
 		for (DtoJob newJob : newJobsCopy) {
 			try {
 				JobObject jo = new JobObject(si, newJob);
 				jobObjectList.add(jo);
 			} catch (NoSuchJobException e) {
 				// probably not that important
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	private final int UPDATE_TIME_IN_SECONDS = 10;
 
 	private static Map<ServiceInterface, RunningJobManager> cachedRegistries = new HashMap<ServiceInterface, RunningJobManager>();
 	public static RunningJobManager getDefault(final ServiceInterface si) {
 
 		if (si == null) {
 			throw new RuntimeException(
 					"ServiceInterface not initialized yet. Can't get default registry...");
 		}
 
 		synchronized (si) {
 			if (cachedRegistries.get(si) == null) {
 				RunningJobManager m = new RunningJobManager(si);
 				cachedRegistries.put(si, m);
 			}
 		}
 
 		return cachedRegistries.get(si);
 	}
 
 	private final UserEnvironmentManager em;
 
 	private final ServiceInterface si;
 
 	private Map<String, BatchJobObject> cachedAllBatchJobs = Collections
 			.synchronizedMap(new HashMap<String, BatchJobObject>());
 
 	private Map<String, EventList<BatchJobObject>> cachedBatchJobsPerApplication = Collections
 			.synchronizedMap(new HashMap<String, EventList<BatchJobObject>>());;
 
 	private Timer updateTimer = new Timer();
 
 	public RunningJobManager(ServiceInterface si) {
 		this.si = si;
 		this.em = GrisuRegistryManager.getDefault(si)
 				.getUserEnvironmentManager();
 
 		startAutoRefresh();
 	}
 
 	private synchronized Collection<BatchJobObject> getAllCurrentlyWatchedBatchJobs() {
 
 		return cachedAllBatchJobs.values();
 
 	}
 
 	public BatchJobObject getBatchJob(String jobname) throws NoSuchJobException {
 
 		synchronized (jobname) {
 
 			if (cachedAllBatchJobs.get(jobname) == null) {
 
 				try {
 					BatchJobObject temp = new BatchJobObject(si, jobname, false);
 					cachedAllBatchJobs.put(jobname, temp);
 				} catch (BatchJobException e) {
 					throw new RuntimeException(e);
 				}
 
 			}
 			return cachedAllBatchJobs.get(jobname);
 
 		}
 	}
 
 	public synchronized EventList<BatchJobObject> getBatchJobs(
 			String application) {
 
 		if (cachedBatchJobsPerApplication.get(application) == null) {
 
 			EventList<BatchJobObject> temp = new BasicEventList<BatchJobObject>();
 
 			for (String jobname : em
 					.getCurrentBatchJobnames(application, false)) {
 				try {
 					temp.add(getBatchJob(jobname));
 				} catch (NoSuchJobException e) {
 					throw new RuntimeException(e);
 				}
 			}
 
 			cachedBatchJobsPerApplication.put(application, temp);
 
 		}
 		return cachedBatchJobsPerApplication.get(application);
 	}
 
 	public void startAutoRefresh() {
 
 		updateTimer.schedule(new UpdateTimerTask(), 0);
 
 	}
 
 	/**
 	 * Updates the list of jobnames for this application.
 	 * 
 	 * This doesn't update the batchjobs itself.
 	 * 
 	 * @param application
 	 *            the application
 	 */
 	public synchronized void updateBatchJobList(String application) {
 
 		EventList<BatchJobObject> list = getBatchJobs(application);
 
 		SortedSet<String> jobnames = em.getCurrentBatchJobnames(application,
 				true);
 		SortedSet<String> jobnamesNew = new TreeSet<String>(jobnames);
 
 		for (BatchJobObject bj : list) {
 			jobnamesNew.remove(bj.getJobname());
 		}
 		for (String name : jobnamesNew) {
 			try {
 				list.add(getBatchJob(name));
 			} catch (NoSuchJobException e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 		Set<BatchJobObject> toRemove = new HashSet<BatchJobObject>();
 		for (BatchJobObject bj : list) {
 			if (!jobnames.contains(bj.getJobname())) {
 				toRemove.add(bj);
 			}
 		}
 		list.removeAll(toRemove);
 
 	}
 
 }
