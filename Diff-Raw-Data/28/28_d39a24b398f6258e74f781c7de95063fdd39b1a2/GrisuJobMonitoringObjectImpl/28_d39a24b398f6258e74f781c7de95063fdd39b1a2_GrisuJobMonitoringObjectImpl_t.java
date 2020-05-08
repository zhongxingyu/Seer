 
 
 package org.vpac.grisu.client.model.jobs;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TimeZone;
 
 import org.apache.log4j.Logger;
 import org.vpac.grisu.client.control.EnvironmentManager;
 import org.vpac.grisu.client.control.jobs.JobManager;
 import org.vpac.grisu.client.model.files.GrisuFileObject;
 import org.vpac.grisu.control.JobConstants;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.NoSuchJobException;
 import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
 import org.vpac.grisu.control.exceptions.VomsException;
 
 /**
  * This is a wrapper class for easy access to the properties of a job.
  * 
  * @author markus
  * 
  */
 public class GrisuJobMonitoringObjectImpl implements GrisuJobMonitoringObject {
 	
 	static final Logger myLogger = Logger.getLogger(GrisuJobMonitoringObjectImpl.class
 			.getName());
 	
 	
 
 	public static final String NOT_AVAILABLE_STRING = "n/a";
 
 	private ServiceInterface serviceInterface = null;
 	private EnvironmentManager em = null;
 
 	private GrisuFileObject jobDirectoryObject = null;
 
 	private Map<String, String> jobProperties = null;
 
 	private String name = null;
 	private String applicationType = NOT_AVAILABLE_STRING;
 	private String walltime = NOT_AVAILABLE_STRING;
 	private String cpus = NOT_AVAILABLE_STRING;
 	private String submissionHost = NOT_AVAILABLE_STRING;
 	private String submissionQueue = NOT_AVAILABLE_STRING;
 	private String submissionTime = NOT_AVAILABLE_STRING;
 	private String fqan = NOT_AVAILABLE_STRING;
 	private String jobDirectory = NOT_AVAILABLE_STRING;
 	private String stdout = NOT_AVAILABLE_STRING;
 	private String stderr = NOT_AVAILABLE_STRING;
 
 	private Map<String, String> otherProperties = null;
 
 	private String status = null;
 	private Date statusLastChecked = null;
 	private boolean preventStatusUpdate = true;
 
 	public GrisuJobMonitoringObjectImpl(String jobname, EnvironmentManager em) {
 
 		this.name = jobname;
 		this.em = em;
 		this.serviceInterface = em.getServiceInterface();
 
 	}
 
 	public void fillJobDetails() {
 
 		try {
 			myLogger.debug("Filling job details for job: "+this.name);
 			this.jobProperties = serviceInterface.getAllJobProperties(this.name);
 		} catch (NoSuchJobException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 		fillJobDetails(this.jobProperties);
 	}
 
 	public void fillJobDetails(Map<String, String> jobProperties) {
 		this.jobProperties = jobProperties;
 
 		otherProperties = new HashMap<String, String>(jobProperties);
 
 		applicationType = jobProperties.get("applicationType");
 		if (applicationType == null)
 			applicationType = JobConstants.NOT_AVAILABLE_STRING;
 
 		otherProperties.remove("applicationType");
 
 		try {
 			walltime = new Integer(jobProperties.get("walltime")).toString();
 		} catch (NumberFormatException e) {
 			// does not matter
 		}
 		if (walltime == null)
 			walltime = JobConstants.NOT_AVAILABLE_STRING;
 		otherProperties.remove("walltime");
 
 		try {
 			cpus = new Integer(jobProperties.get("cpus")).toString();
 		} catch (NumberFormatException e) {
 			// does not matter
 		}
 		if (cpus == null) {
 			cpus = JobConstants.NOT_AVAILABLE_STRING;
 		}
 		otherProperties.remove("cpus");
 
 		submissionHost = jobProperties.get("hostname");
 		if (submissionHost == null) {
 			submissionHost = JobConstants.NOT_AVAILABLE_STRING;
 		}
 		otherProperties.remove("hostname");
 
 		submissionQueue = jobProperties.get("submissionQueue");
 		if (submissionQueue == null) {
 			submissionQueue = JobConstants.NOT_AVAILABLE_STRING;
 		}
 		otherProperties.remove("submissionQueue");
 
 		submissionTime = jobProperties.get("submissionTime");
 		if (submissionTime == null) {
 			submissionTime = JobConstants.NOT_AVAILABLE_STRING;
 		}
 		otherProperties.remove("submissionTime");
 
 		fqan = jobProperties.get("fqan");
 		if (fqan == null) {
 			fqan = "Non-VO job";
 		}
 		otherProperties.remove("fqan");
 
 		status = jobProperties.get("status");
 		if (status == null) {
 			status = JobConstants.UNDEFINED_STRING;
 		}
 		otherProperties.remove("status");
 		// statusLastChecked = new Date();
 		preventStatusUpdate = false;
 		getStatus(true);
 		// ----------------------------------------------------
 		// files
 		jobDirectory = jobProperties.get("jobDirectory");
 		otherProperties.remove("jobDirectory");
 
 		stdout = jobProperties.get("stdout");
 		if (jobDirectory == null || stdout == null) {
 			stdout = "n/a";
 		} else {
 			stdout = jobDirectory + "/" + stdout;
 		}
 		otherProperties.remove("stdout");
 
 		stderr = jobProperties.get("stderr");
 		if (jobDirectory == null || stderr == null) {
 			stderr = "n/a";
 		} else {
 			stderr = jobDirectory + "/" + stderr;
 		}
 		otherProperties.remove("stderr");
 
 		if (jobDirectory == null) {
 			jobDirectory = JobConstants.NOT_AVAILABLE_STRING;
 		}
 
 		// getJobDirectoryObject().getFileSystemBackend().getRoot();
 		// -----------------------------------------------------
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObjectImpl#getServiceInterface()
 	 */
 	public ServiceInterface getServiceInterface() {
 		return serviceInterface;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObjectImpl#getJobProperties()
 	 */
 	public Map<String, String> getJobProperties() {
 		
 		if ( jobProperties == null ) {
 				fillJobDetails();
 		}
 		
 		return jobProperties;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObjectImpl#getName()
 	 */
 	public String getName() {
 			
 		if ( name == null || NOT_AVAILABLE_STRING.equals(name) ) 
 			fillJobDetails();
 		
 		return name;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObjectImpl#getApplicationType()
 	 */
 	public String getApplicationType() {
 		
 		if ( applicationType == null || NOT_AVAILABLE_STRING.equals(name) ) 
 			fillJobDetails();
 		
 		return applicationType;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObjectImpl#getWalltime()
 	 */
 	public String getWalltime() {
 		
 		if ( walltime == null || NOT_AVAILABLE_STRING.equals(name) ) 
 			fillJobDetails();
 		
 		return walltime;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObjectImpl#getCpus()
 	 */
 	public String getCpus() {
 		
 		if ( cpus == null || NOT_AVAILABLE_STRING.equals(name) ) 
 			fillJobDetails();
 		
 		return cpus;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObjectImpl#getSubmissionHost()
 	 */
 	public String getSubmissionHost() {
 		
 		if ( submissionHost == null || NOT_AVAILABLE_STRING.equals(name) ) 
 			fillJobDetails();
 		
 		return submissionHost;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObjectImpl#getSubmissionQueue()
 	 */
 	public String getSubmissionQueue() {
 		
 		if ( submissionQueue == null || NOT_AVAILABLE_STRING.equals(name) ) 
 			fillJobDetails();
 		
 		return submissionQueue;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObjectImpl#getSubmissionTime()
 	 */
 	public String getSubmissionTime() {
 		
 		if ( submissionTime == null || NOT_AVAILABLE_STRING.equals(name) )
 			fillJobDetails();
 		
		Date date = null;
 		try {
			
			date = new Date(Long.parseLong(submissionTime));
			
		} catch (Exception e) {
			myLogger.warn("Job seems to be in old time format. Trying other method of parsing it...");
			try {
				date = new SimpleDateFormat().parse(submissionTime);
			} catch (ParseException e1) {
				myLogger.error("Could not parse submission time. Returning the String without conversion.");
				return submissionTime;
			}
 		}
 		
 		JobManager.inputDateFormat.setTimeZone(TimeZone.getDefault());
 		
		return JobManager.inputDateFormat.format(date);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObjectImpl#getFqan()
 	 */
 	public String getFqan() {
 		
 		if ( fqan == null || NOT_AVAILABLE_STRING.equals(name) ) 
 			fillJobDetails();
 			
 		return fqan;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObjectImpl#getStatus()
 	 */
 	public String getStatus() {
 		
 		return getStatus(false);
 	}
 	
 	public String getStatus(boolean forceRefresh) {
 
 		if (preventStatusUpdate && ! forceRefresh) {
 			return JobConstants.LOADING_STRING;
 		}
 
 		// only check every min delta seconds
 
 		if (forceRefresh == true || (statusLastChecked == null)) {
 			// || newDate.getTime() - statusLastChecked.getTime() >
 			// MIN_DELTA_BETWEEN_STATUS_CHECKS) ) {
 
 			// this is only needed if the job is still running
 			if (JobConstants.translateStatusBack(status) < JobConstants.FINISHED_EITHER_WAY) {
 				status = JobConstants.translateStatus(serviceInterface
 						.getJobStatus(name));
 				statusLastChecked = new Date();
 			}
 		}
 		return status;
 	}
 
 	public int getStatusAsInt() {
 		return JobConstants.translateStatusBack(getStatus());
 	}
 
 	public int getStatusAsInt(boolean forceRefresh) {
 		return JobConstants.translateStatusBack(getStatus(forceRefresh));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObjectImpl#getJobDirectory()
 	 */
 	public String getJobDirectory() {
 		
 		if ( jobDirectory == null || NOT_AVAILABLE_STRING.equals(jobDirectory) ) 
 			fillJobDetails();
 		
 		return jobDirectory;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObjectImpl#getStdout()
 	 */
 	public String getStdout() {
 		
 		if ( stdout == null || NOT_AVAILABLE_STRING.equals(name) ) 
 			fillJobDetails();
 		
 		return stdout;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObjectImpl#getStderr()
 	 */
 	public String getStderr() {
 		
 		if ( stderr == null || NOT_AVAILABLE_STRING.equals(name) ) 
 			fillJobDetails();
 		
 		return stderr;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObjectImpl#getOtherProperties()
 	 */
 	public Map<String, String> getOtherProperties() {
 		
 		if ( otherProperties == null ) 
 			fillJobDetails();
 		
 		return otherProperties;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObjectImpl#getJobDirectoryObject()
 	 */
 	public GrisuFileObject getJobDirectoryObject() {
 		if (jobDirectoryObject == null) {
 			try {
 
 				jobDirectoryObject = em.getFileManager().getFileObject(
 						new URI(getJobDirectory()));
 			} catch (URISyntaxException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return jobDirectoryObject;
 	}
 
 	public void kill() throws VomsException, NoSuchJobException {
 		try {
 			getServiceInterface().kill(getName(), false);
 		} catch (RemoteFileSystemException e) {
 			// should never happen since the files are not touched
 			e.printStackTrace();
 		}
 	}
 
 	public void killAndClean() throws RemoteFileSystemException, VomsException,
 			NoSuchJobException {
 		// getServiceInterface().kill(getName(), true);
 
 		em.getJobManager().cleanJob(this);
 
 	}
 
 	public EnvironmentManager getEnvironmentManager() {
 		return em;
 	}
 
 }
