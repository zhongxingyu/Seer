 package org.vpac.grisu.model.dto;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.vpac.grisu.control.JobConstants;
 
 import au.org.arcs.jcommons.constants.Constants;
 
 @XmlRootElement(name = "multiPartJob")
 public class DtoBatchJob implements Comparable<DtoBatchJob> {
 
 	private String batchJobname;
 	private String submissionFqan;
 
 	private boolean isFinished = true;
 
 	private int status = JobConstants.UNDEFINED;
 
 	private DtoJobs jobs = new DtoJobs();
 
 	private DtoLogMessages messages = new DtoLogMessages();
 
 	/**
 	 * The list of job properties.
 	 */
 	private List<DtoJobProperty> properties = new LinkedList<DtoJobProperty>();
 	// /**
 	// * The status of the job. Be aware that, depending on how you queried for
 	// this job, this can be stale information.
 	// */
 	// private int status;
 
 	private DtoJobs failedJobs = new DtoJobs();
 
 	public DtoBatchJob() {
 	}
 
 	public DtoBatchJob(String batchJobname) {
 		this.batchJobname = batchJobname;
 	}
 
 	public synchronized void addJob(DtoJob job) {
 		this.jobs.addJob(job);
 		if (job.getStatus() < JobConstants.FINISHED_EITHER_WAY) {
 			this.isFinished = false;
 		}
 	}
 
 	// @XmlAttribute(name="failed")
 	// public boolean isFailed() {
 	// return this.isFailed;
 	// }
 	//	
 	// public void setFailed(boolean f) {
 	// this.isFailed = f;
 	// }
 
 	public boolean allJobsFinishedSuccessful() {
 
 		if (!isFinished()) {
 			return false;
 		}
 		if (totalNumberOfJobs() == numberOfSuccessfulJobs()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public int compareTo(DtoBatchJob o) {
 
 		return getBatchJobname().compareTo(o.getBatchJobname());
 	}
 
 	public boolean failed() {
 
 		if (failedJobs().size() > 0) {
 			return true;
 		} else {
 			return false;
 		}
 
 	}
 
 	public SortedSet<DtoJob> failedJobs() {
 
 		SortedSet<DtoJob> result = new TreeSet<DtoJob>();
 
 		for (DtoJob job : jobs.getAllJobs()) {
 			if (job.getStatus() >= JobConstants.FINISHED_EITHER_WAY
 					&& job.getStatus() != JobConstants.DONE) {
 				result.add(job);
 			}
 		}
 
 		return result;
 	}
 
 	public SortedSet<DtoJob> finishedJobs() {
 
 		SortedSet<DtoJob> result = new TreeSet<DtoJob>();
 
 		for (DtoJob job : jobs.getAllJobs()) {
 			if (job.getStatus() >= JobConstants.FINISHED_EITHER_WAY) {
 				result.add(job);
 			}
 		}
 
 		return result;
 	}
 
 	@XmlElement(name = "batchJobname")
 	public String getBatchJobname() {
 		return batchJobname;
 	}
 
 	@XmlElement(name = "failedJob")
 	public DtoJobs getFailedJobs() {
 		return failedJobs;
 	}
 
 	@XmlElement(name = "jobs")
 	public DtoJobs getJobs() {
 		return jobs;
 	}
 
 	@XmlElement(name = "logMessages")
 	public DtoLogMessages getMessages() {
 		return messages;
 	}
 
 	public Double getPercentFinished() {
 		if (totalNumberOfJobs() <= 0) {
 			return 0.0;
 		}
 		return new Double((numberOfSuccessfulJobs() * 100)
 				/ totalNumberOfJobs());
 	}
 
 	@XmlElement(name = "jobproperty")
 	public List<DtoJobProperty> getProperties() {
 		return properties;
 	}
 
 	@XmlElement(name = "status")
 	public int getStatus() {
 		return this.status;
 	}
 
 	@XmlElement(name = "fqan")
 	public String getSubmissionFqan() {
 		return this.submissionFqan;
 	}
 
 	@XmlAttribute(name = "finished")
 	public boolean isFinished() {
		
		if ( this.totalNumberOfJobs() <= 0 ) {
			return false;
		} else {
			return this.isFinished;
		}
 	}
 
 	public Map<Date, String> messages() {
 		Map<Date, String> result = new TreeMap<Date, String>();
 
 		for (DtoLogMessage m : getMessages().getMessages()) {
 			result.put(m.getDate(), m.getMessage());
 		}
 		return result;
 	}
 
 	public int numberOfFailedJobs() {
 		int failed = 0;
 		for (DtoJob job : jobs.getAllJobs()) {
 			if (job.getStatus() >= JobConstants.FINISHED_EITHER_WAY
 					&& job.getStatus() != JobConstants.DONE) {
 				failed = failed + 1;
 			}
 		}
 		return failed;
 	}
 
 	public int numberOfFinishedJobs() {
 		int finished = 0;
 		for (DtoJob job : jobs.getAllJobs()) {
 			if (job.getStatus() >= JobConstants.FINISHED_EITHER_WAY) {
 				finished = finished + 1;
 			}
 		}
 		return finished;
 	}
 
 	public int numberOfRunningJobs() {
 		int running = 0;
 		for (DtoJob job : jobs.getAllJobs()) {
 			if (job.getStatus() == JobConstants.ACTIVE) {
 				running = running + 1;
 			}
 		}
 
 		return running;
 	}
 
 	public int numberOfSuccessfulJobs() {
 		int successful = 0;
 		for (DtoJob job : jobs.getAllJobs()) {
 			if (job.getStatus() == JobConstants.DONE) {
 				successful = successful + 1;
 			}
 		}
 		return successful;
 	}
 
 	public int numberOfUnsubmittedJobs() {
 		int unsubmitted = 0;
 		for (DtoJob job : jobs.getAllJobs()) {
 			if (job.getStatus() <= JobConstants.UNSUBMITTED) {
 				unsubmitted = unsubmitted + 1;
 			}
 		}
 		return unsubmitted;
 	}
 
 	public int numberOfWaitingJobs() {
 		int waiting = 0;
 		for (DtoJob job : jobs.getAllJobs()) {
 			if (job.getStatus() >= JobConstants.UNSUBMITTED
 					&& job.getStatus() <= JobConstants.PENDING) {
 				waiting = waiting + 1;
 			}
 		}
 		return waiting;
 	}
 
 	public String pathToInputFiles() {
 
 		return propertiesAsMap().get(Constants.RELATIVE_PATH_FROM_JOBDIR);
 
 	}
 
 	public Map<String, String> propertiesAsMap() {
 
 		Map<String, String> map = new HashMap<String, String>();
 
 		for (DtoJobProperty prop : getProperties()) {
 			map.put(prop.getKey(), prop.getValue());
 		}
 
 		return map;
 	}
 
 	// public boolean allJobsFinished() {
 	//		
 	// if ( totalNumberOfJobs() == numberOfFinishedJobs() ) {
 	// return true;
 	// } else {
 	// return false;
 	// }
 	//		
 	// }
 
 	public DtoJob retrieveJob(String jobname) {
 		return getJobs().retrieveJob(jobname);
 	}
 
 	public SortedSet<DtoJob> runningJobs() {
 
 		SortedSet<DtoJob> result = new TreeSet<DtoJob>();
 
 		for (DtoJob job : jobs.getAllJobs()) {
 			if (job.getStatus() == JobConstants.ACTIVE) {
 				result.add(job);
 			}
 		}
 
 		return result;
 	}
 
 	public void setBatchJobname(String batchJobname) {
 		this.batchJobname = batchJobname;
 	}
 
 	public void setFailedJobs(DtoJobs failedJobs) {
 		this.failedJobs = failedJobs;
 	}
 
 	public void setFinished(boolean f) {
 		this.isFinished = f;
 	}
 
 	public void setJobs(DtoJobs jobs) {
 		this.jobs = jobs;
 	}
 
 	public void setMessages(DtoLogMessages messages) {
 		this.messages = messages;
 	}
 
 	public void setProperties(List<DtoJobProperty> properties) {
 		this.properties = properties;
 	}
 
 	public void setStatus(int status) {
 		this.status = status;
 	}
 
 	public void setSubmissionFqan(String fqan) {
 		this.submissionFqan = fqan;
 	}
 
 	public SortedSet<DtoJob> successfulJobs() {
 
 		SortedSet<DtoJob> result = new TreeSet<DtoJob>();
 
 		for (DtoJob job : jobs.getAllJobs()) {
 			if (job.getStatus() == JobConstants.DONE) {
 				result.add(job);
 			}
 		}
 
 		return result;
 	}
 
 	@Override
 	public String toString() {
 		return getBatchJobname();
 	}
 
 	public int totalNumberOfJobs() {
 		return this.jobs.getAllJobs().size();
 	}
 
 	public SortedSet<DtoJob> unsubmittedJobs() {
 
 		SortedSet<DtoJob> result = new TreeSet<DtoJob>();
 
 		for (DtoJob job : jobs.getAllJobs()) {
 			if (job.getStatus() <= JobConstants.UNSUBMITTED) {
 				result.add(job);
 			}
 		}
 
 		return result;
 	}
 
 	public SortedSet<DtoJob> waitingJobs() {
 
 		SortedSet<DtoJob> result = new TreeSet<DtoJob>();
 
 		for (DtoJob job : jobs.getAllJobs()) {
 			if (job.getStatus() >= JobConstants.UNSUBMITTED
 					&& job.getStatus() <= JobConstants.PENDING) {
 				result.add(job);
 			}
 		}
 
 		return result;
 	}
 
 }
