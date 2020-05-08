 package org.vpac.grisu.client.control.jobs;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.log4j.Logger;
 import org.vpac.grisu.client.control.EnvironmentManager;
 import org.vpac.grisu.client.model.files.GrisuFileObject;
 import org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObject;
 import org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObjectImpl;
 import org.vpac.grisu.control.exceptions.NoSuchJobException;
 import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
 import org.vpac.grisu.model.MountPoint;
 
 import ca.odell.glazedlists.BasicEventList;
 import ca.odell.glazedlists.EventList;
 
 public class JobManager {
 	
 	static final Logger myLogger = Logger.getLogger(JobManager.class
 			.getName());
 
 	public static final DateFormat inputDateFormat = new SimpleDateFormat(
 			"EEE MMM dd HH:mm:ss z yyyy");
 //	public static DateFormat outputDateFormat = new SimpleDateFormat(
 //			"dd MMM yyyy, HH:mm");
 
 	EnvironmentManager em = null;
 
 	EventList<GrisuJobMonitoringObject> allJobs = new BasicEventList<GrisuJobMonitoringObject>();
 	Set<String> allJobnames = null;
 
 	public JobManager(EnvironmentManager em) {
 		this.em = em;
 		refreshAllJobs(true);
 	}
 	
 //	public EventList<GrisuJobMonitoringObject> getAllJobsVariable() {
 //		return allJobs;
 //	}
 
 	/**
 	 * Returns all jobs that are in the Grisu servers database.
 	 * 
 	 * @param refresh whether to refresh the status of every job or use a possible cached state
 	 * @return a list of all Jobs
 	 */
 	public synchronized EventList<GrisuJobMonitoringObject> getAllJobs(boolean refresh) {
 
 //		if (allJobs.size() == 0) {
 //			retrieveAllJobs();
 //		}
 		
 		if ( refresh ) {
 			refreshJobs(allJobs);
 		}
 
 		return allJobs;
 	}
 	
 	public Set<String> getAllJobnames(boolean forceRefresh) {
 		if ( allJobnames == null ) {
 			getAllJobs(forceRefresh);
 		}
 		return allJobnames;
 	}
 	
 	public GrisuJobMonitoringObject getJob(String name, boolean refreshJobList) throws NoSuchJobException {
 		if ( refreshJobList ) {
 			refreshAllJobs(true);
 		}
 		return getJob(name);
 	}
 	
 	public GrisuJobMonitoringObject getJob(String name) throws NoSuchJobException {
 		
 		if ( name == null || "".equals(name) ) {
 			throw new NoSuchJobException("Not a valid jobname.");
 		}
 		for ( GrisuJobMonitoringObject job : getAllJobs(false) ) {
 			if ( name.equals(job.getName()) ) {
 				return job;
 			}
 		}
 		
 		throw new NoSuchJobException("No job with jobname \""+name+"\" in database.");
 	}
 	
 	/**
 	 * This can be used to check the status of jobs instead of implementing a loop on the list itself.
 	 * It'll use threads soon so it should be faster...
 	 * @param jobs the jobs to refresh
 	 */
 	public void refreshJobs(List<GrisuJobMonitoringObject> jobs) {
 		
 		allJobs.getReadWriteLock().readLock().lock();
 		try { 
 		for ( GrisuJobMonitoringObject job : jobs ) {
 			refreshJob(job);
 		}
 		} finally {
 			allJobs.getReadWriteLock().readLock().unlock();
 		}
 		
 	}
 	
 	public void refreshAllJobs(boolean forceRefreshJobList) {
 		
 		
 
 		if ( forceRefreshJobList ) {
 				retrieveAllJobs();
 
 		} else {
 			refreshJobs(getAllJobs(false));
 		}
 		
 	}
 	
 	/**
 	 * Refreshes the jobs with the specified jobnames.
 	 * @param jobnames the names of the jobs to refresh
 	 * @throws NoSuchJobException if one of the specified jobnames doesn't exist. All jobnames before the failed one were refreshed in that case.
 	 */
 	public void refreshJobs(String[] jobnames) throws NoSuchJobException {
 		for ( String jobname : jobnames ) {
 			refreshJob(jobname);
 		}
 	}
 	
 	public void refreshJob(GrisuJobMonitoringObject job) {
 		
 		synchronized ( job ) {
 			job.getStatus(true);
 		}
 		
 	}
 	
 	/**
 	 * Refreshes the status of the job with the specified jobname.
 	 * @param jobname the name of the job in question
 	 * @throws NoSuchJobException if the job doesn't exist in the databse
 	 */
 	public void refreshJob(String jobname) throws NoSuchJobException {
 		
 		refreshJob(getJob(jobname));
 		
 	}
 	
 	public void killJobs(List<GrisuJobMonitoringObject> jobs) throws NoSuchJobException {
 		
 		String[] jobnames = new String[jobs.size()];
 		
 		for ( int i=0; i<jobnames.length; i++ ) {
 			jobnames[i] = jobs.get(i).getName();
 		}
 		
 		JobHelper.killJobs(jobnames, em);
 		refreshJobs(jobnames);
 	}
 	
	public void cleanJob(GrisuJobMonitoringObject job) throws RemoteFileSystemException, NoSuchJobException {
 		List<GrisuJobMonitoringObject> tempList = new LinkedList<GrisuJobMonitoringObject>();
 		tempList.add(job);
 		cleanJobs(tempList);
 	}
 	
 	public void cleanJobs(List<GrisuJobMonitoringObject> jobs) throws RemoteFileSystemException, NoSuchJobException {
 		
 		String[] jobnames = new String[jobs.size()];
 		
 		for ( int i=0; i<jobnames.length; i++ ) {
 			jobnames[i] = jobs.get(i).getName();
 		}
 		
 		JobHelper.cleanJobs(jobnames, em);
 		
 		//TODO check for exception and update allJobs list if necessary
 		allJobs.getReadWriteLock().writeLock().lock();
 		try {
 			for ( GrisuJobMonitoringObject job : jobs ) {
 				allJobnames.remove(job.getName());
 				allJobs.remove(job);
 			}
 		//allJobs.removeAll(jobs);
 		} finally {
 			allJobs.getReadWriteLock().writeLock().unlock();
 		}
 		
 	}
 	
 	public void newJobSubmitted(final String newJobName) {
 		
 //		new Thread() {
 //			public void run() {
 		
 		GrisuJobMonitoringObject newJob = buildJobMonitoringObject(newJobName);
 
 		if ( ! getAllJobs(false).contains(newJob) ) {
 
 			allJobs.getReadWriteLock().writeLock().lock();
 			try {
 				newJob.fillJobDetails();
 				allJobs.add(newJob);	
 				allJobnames.add(newJob.getName());
 			} catch (NoSuchJobException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} finally {
 				allJobs.getReadWriteLock().writeLock().unlock();
 			}
 
 		}
 		
 //			}
 //		}.start();
 		
 	}
 	
 
 	public GrisuFileObject getJobRootDirectory(GrisuJobMonitoringObject job) {
 
 		URI uri = null;
 		try {
 			uri = new URI(job.getJobDirectory());
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 
 		GrisuFileObject file = em.getFileManager().getFileObject(uri);
 		if (file == null) {
 			// TODO that's a workaround that doesn't work all the time
 			try {
 				MountPoint mp = em.getResponsibleMountpointForFile(uri
 						.toString());
 				// build the root tree for this filesystem
 				em.getFileManager().getFileSystemBackend(mp.getAlias())
 						.getRoot().getChildren();
 				file = em.getFileManager().getFileObject(uri);
 			} catch (Exception e) {
 				myLogger.error(e);
 			}
 			if (file == null) {
 				myLogger
 						.error("Still no job directory file object. This is a bug.");
 
 			}
 		}
 
 		return file;
 	}
 	
 	private GrisuJobMonitoringObject buildJobMonitoringObject(String jobname) {
 
 		GrisuJobMonitoringObject object = null;
 
 		object = new GrisuJobMonitoringObjectImpl(jobname, em);
 
 		return object;
 	}
 	
 	public synchronized void fillAllJobProperties() {
 		
 		for ( GrisuJobMonitoringObject job : getAllJobs(false) ) {
 			try {
 				job.fillJobDetails();
 			} catch (NoSuchJobException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 	}
 	
 	//updates allJobanmes everytime it is called
 	private synchronized void retrieveAllJobs() {
 		
 		allJobs.getReadWriteLock().writeLock().lock();
 		allJobs.clear();
 		
 		String[] allJobnamesTemp = em.getServiceInterface().getAllJobnames();
 		allJobnames = new TreeSet<String>();
 		
 		for ( String jobname : allJobnamesTemp ) {
 			GrisuJobMonitoringObject object = buildJobMonitoringObject(jobname);
 			allJobs.add(object);
 			allJobnames.add(object.getName());
 		}
 		
 		allJobs.getReadWriteLock().writeLock().unlock();
 
 //		Document allJobsXml = em.getServiceInterface().ps();
 //
 //		
 //		allJobs.getReadWriteLock().writeLock().lock();
 //		allJobs.clear();
 //		
 //
 //		allJobnames = new TreeSet<String>();
 ////		EventList tempJobs = new BasicEventList<GrisuJobMonitoringObject>();
 //
 //		NodeList jobElements = allJobsXml.getFirstChild().getChildNodes();
 //
 //		for (int i = 0; i < jobElements.getLength(); i++) {
 //			Element jobElement = (Element) jobElements.item(i);
 //
 //			String jobname = jobElement.getAttribute("jobname");
 //
 //			GrisuJobMonitoringObject object = buildJobMonitoringObject(jobname);
 //			
 //			allJobs.add(object);
 //			allJobnames.add(object.getName());
 //		}
 //		allJobs.getReadWriteLock().writeLock().unlock();
 	}
 
 
 }
