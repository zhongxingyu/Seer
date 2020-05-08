 package org.vpac.grisu.clients.gridTests.testElements;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.UUID;
 
 import org.bushe.swing.event.annotation.AnnotationProcessor;
 import org.bushe.swing.event.annotation.EventSubscriber;
 import org.vpac.grisu.clients.gridTests.GridTestInfo;
 import org.vpac.grisu.clients.gridTests.GridTestStage;
 import org.vpac.grisu.clients.gridTests.GridTestStageStatus;
 import org.vpac.grisu.clients.gridTests.Utils;
 import org.vpac.grisu.control.JobConstants;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.JobPropertiesException;
 import org.vpac.grisu.control.exceptions.JobSubmissionException;
 import org.vpac.grisu.frontend.control.clientexceptions.MdsInformationException;
 import org.vpac.grisu.frontend.model.events.JobStatusEvent;
 import org.vpac.grisu.frontend.model.job.JobObject;
 
 public abstract class GridTestElement implements Comparable<GridTestElement> {
 
 	protected final ServiceInterface serviceInterface;
 	protected final String version;
 	protected final String submissionLocation;
 
 	protected final String id;
 	protected final String jobname;
 
 	protected final String fqan;
 
 	private final List<GridTestStage> testStages = new LinkedList<GridTestStage>();
 
 	protected final JobObject jobObject;
 
 	private GridTestStage currentStage;
 
 	private boolean failed = false;
 	private boolean interrupted = false;
 
 	private List<Exception> exceptions = new LinkedList<Exception>();
 
 	private final static String END_STAGE = "endStage";
 
 	public static boolean useMds(String application) {
 
 		if ("java".equals(application)) {
 			return true;
 		} else if ("unixcommands".equals(application)) {
 			return true;
 		} else if ("underworld".equals(application)) {
 			return true;
 		} else {
 			return false;
 		}
 
 	}
 
 	protected final GridTestInfo info;
 
 	private final Date startDate;
 
 	private Date endDate = null;
 
 	protected GridTestElement(GridTestInfo info, String version,
 			String submissionLocation, String fqan)
 			throws MdsInformationException {
 		this.info = info;
 		this.fqan = fqan;
 		startDate = new Date();
 		endDate = new Date();
 		beginNewStage("Initializing test element...");
 		this.serviceInterface = info.getController().getServiceInterface();
 		this.version = version;
 		this.submissionLocation = submissionLocation;
 		addMessage("Creating JobObject...");
 		this.jobObject = createJobObject();
 		this.id = UUID.randomUUID().toString();
 		this.jobname = this.info.getApplicationName() + "_" + this.version
 				+ "_" + this.id;
 		this.jobObject.setJobname(jobname);
 		this.jobObject.setSubmissionLocation(submissionLocation);
 		addMessage("JobObject created.");
 		AnnotationProcessor.process(this);
 		currentStage.setStatus(GridTestStageStatus.FINISHED_SUCCESS);
 	}
 
 	protected void addMessage(String message) {
 		if (currentStage != null) {
 			currentStage.addMessage(message);
 		}
 	}
 	private boolean beginNewStage(String stageName) {
 
 		boolean lastStageSuccess = true;
 		if (currentStage != null && !currentStage.wasSuccessful()) {
 			lastStageSuccess = false;
 			if (currentStage.getPossibleException() != null) {
 				exceptions.add(currentStage.getPossibleException());
 			}
 		}
 
 		if (END_STAGE.equals(stageName)) {
 			addMessage("Finished test.");
 			currentStage = null;
 			endDate = new Date();
 			return lastStageSuccess;
 		}
 
 		currentStage = new GridTestStage(stageName);
 		testStages.add(currentStage);
 		currentStage.setStatus(GridTestStageStatus.RUNNING);
 
 		return lastStageSuccess;
 	}
 
 	abstract protected boolean checkJobSuccess();
 
 	public void checkWhetherJobDidWhatItWasSupposedToDo() {
 
 		if (!failed) {
 
 			if (beginNewStage("Checking job status and output...")) {
 
 				boolean success = true;
 				try {
 					success = checkJobSuccess();
 				} catch (Exception e) {
 					System.out.println("Error checking job " + toString()
 							+ ": " + e.getLocalizedMessage());
 					this.setPossibleExceptionForCurrentStage(e);
 					success = false;
 				}
 
 				if (success) {
 					currentStage
 							.setStatus(GridTestStageStatus.FINISHED_SUCCESS);
 				} else {
 					currentStage.setStatus(GridTestStageStatus.FINISHED_ERROR);
 					failed = true;
 				}
 			} else {
 				currentStage.setStatus(GridTestStageStatus.NOT_EXECUTED);
 			}
 
 		}
 
 	}
 
 	public int compareTo(GridTestElement o) {
 		int testname = this.info.getTestname().compareTo(
 				o.getTestInfo().getTestname());
 		if (testname != 0) {
 			return testname;
 		}
 		int application = this.info.getApplicationName().compareTo(
 				o.getTestInfo().getApplicationName());
 		if (application != 0) {
 			return application;
 		}
 		int version = this.getVersion().compareTo(o.getVersion());
 		if (version != 0) {
 			return version;
 		}
 		int subLoc = this.getSubmissionLocation().compareTo(
 				o.getSubmissionLocation());
 		if (subLoc != 0) {
 			return subLoc;
 		}
 		int fqan = this.getFqan().compareTo(o.getFqan());
 		if (fqan != 0) {
 			return fqan;
 		}
 
 		int id = this.getTestId().compareTo(o.getTestId());
 
 		return id;
 
 	}
 
 	public void createJob(String fqan) {
 
 		beginNewStage("Creating job on backend...");
 
 		try {
 			jobObject.createJob(fqan);
 			currentStage.setStatus(GridTestStageStatus.FINISHED_SUCCESS);
 		} catch (JobPropertiesException e) {
 			currentStage.setPossibleException(e);
 			currentStage.setStatus(GridTestStageStatus.FINISHED_ERROR);
 			failed = true;
 		}
 
 	}
 
 	abstract protected JobObject createJobObject()
 			throws MdsInformationException;
 
 	@Override
 	public boolean equals(Object o) {
		
		if ( o == null ) {
			return false;
		}
 
 		GridTestElement other = null;
 		try {
 			other = (GridTestElement) o;
 		} catch (Exception e) {
 			return false;
 		}
 
 		if (!this.getTestInfo().getApplicationName().equals(
 				other.getTestInfo().getApplicationName())) {
 			return false;
 		}
 		if (!this.getTestInfo().getTestname().equals(
 				other.getTestInfo().getTestname())) {
 			return false;
 		}
 		if (!this.getVersion().equals(other.getVersion())) {
 			return false;
 		}
 		if (!this.getFqan().equals(other.getFqan())) {
 			return false;
 		}
 
 		if (!this.getTestId().equals(other.getTestId())) {
 			return false;
 		}
 
 		return true;
 	}
 
 	public boolean failed() {
 		return failed;
 	}
 
 	public void finishTest() {
 
 		beginNewStage(END_STAGE);
 
 		// housekeeping
 		try {
 			jobObject.kill(true);
 		} catch (Exception e) {
 			// doesn't matter
 		}
 	}
 
 	public Date getEndDate() {
 		return endDate;
 	}
 
 	public List<Exception> getExceptions() {
 		return exceptions;
 	}
 
 	public String getFqan() {
 		return fqan;
 	}
 
 	public int getJobStatus(boolean forceRefresh) {
 		return this.jobObject.getStatus(forceRefresh);
 	}
 
 	public String getResultString() {
 		StringBuffer result = new StringBuffer();
 		for (GridTestStage stage : getTestStages()) {
 			result.append("Stage: " + stage.getName() + "\n");
 			result.append("Started: " + stage.getBeginDate() + "\n");
 			result.append(stage.getMessagesString() + "\n");
 			result.append("Ended: " + stage.getEndDate() + "\n");
 			result.append("Status: " + stage.getStatus() + "\n");
 			if (stage.getStatus().equals(GridTestStageStatus.FINISHED_ERROR)
 					&& stage.getPossibleException() != null) {
 				result.append("Error: "
 						+ stage.getPossibleException().getLocalizedMessage()
 						+ "\n");
 				if (stage.getPossibleException().getCause() != null) {
 					result.append("Cause: "
 							+ Utils.fromException(stage.getPossibleException()
 									.getCause()));
 				}
 			}
 			result.append("\n");
 		}
 		return result.toString();
 	}
 
 	// public void waitForJobToFinish() {
 	//
 	// if ( beginNewStage("Waiting for job to finish...") ) {
 	//
 	// while (this.jobObject.getStatus(true) < JobConstants.FINISHED_EITHER_WAY)
 	// {
 	// if (this.jobObject.getStatus(false) == JobConstants.NO_SUCH_JOB) {
 	// addMessage("Could not find job anymore. Probably a problem with the container...");
 	// currentStage.setStatus(GridTestStageStatus.FINISHED_ERROR);
 	// failed = true;
 	// return;
 	// }
 	//
 	// try {
 	// addMessage("Waiting 2 seconds before new check. Current Status: "
 	// + JobConstants.translateStatus(this.jobObject
 	// .getStatus(false)));
 	// Thread.sleep(2000);
 	// } catch (InterruptedException e) {
 	// currentStage.setPossibleException(e);
 	// currentStage.setStatus(GridTestStageStatus.FINISHED_ERROR);
 	// failed = true;
 	// return;
 	// }
 	// }
 	//
 	// addMessage("Job finished one way or another.");
 	// currentStage.setStatus(GridTestStageStatus.FINISHED_SUCCESS);
 	// } else {
 	// currentStage.setStatus(GridTestStageStatus.NOT_EXECUTED);
 	// }
 	//
 	// }
 
 	public Date getStartDate() {
 		return startDate;
 	}
 
 	public String getSubmissionLocation() {
 		return submissionLocation;
 	}
 
 	public String getTestId() {
 		return this.id;
 	}
 
 	public GridTestInfo getTestInfo() {
 		return this.info;
 	}
 
 	public List<GridTestStage> getTestStages() {
 		return testStages;
 	}
 
 	public String getVersion() {
 		return this.version;
 	}
 
 	@Override
 	public int hashCode() {
 
 		return (23 * getTestInfo().getApplicationName().hashCode())
 				+ (12 * getTestInfo().getTestname().hashCode())
 				+ (3 * getVersion().hashCode())
 				+ (5 * getFqan().hashCode() + 3 * getTestId().hashCode());
 
 	}
 
 	public void interruptRunningJob() {
 		try {
 			jobObject.kill(false);
 		} catch (Exception e) {
 			// doesn't matter
 		}
 		this.interrupted = true;
 		this.failed = true;
 	}
 
 	public void killAndClean() {
 
 		if (!failed) {
 
 			// execute that anyway
 			beginNewStage("Killing and cleaning job...");
 
 			try {
 				jobObject.kill(true);
 			} catch (Exception e) {
 				currentStage.setPossibleException(e);
 				currentStage.setStatus(GridTestStageStatus.FINISHED_ERROR);
 				failed = true;
 			}
 
 			currentStage.setStatus(GridTestStageStatus.FINISHED_SUCCESS);
 		}
 
 	}
 
 	@EventSubscriber(eventClass = JobStatusEvent.class)
 	public void onEvent(JobStatusEvent statusEvent) {
 
 		if (this.jobname.equals(statusEvent.getJob().getJobname())) {
 			addMessage("New job status: "
 					+ JobConstants.translateStatus(statusEvent.getNewStatus()));
 		}
 
 	}
 
 	public void printTestResults() {
 		for (GridTestStage stage : getTestStages()) {
 			System.out.println("Stage: " + stage.getName());
 			System.out.println("Started: " + stage.getBeginDate());
 			stage.printMessages();
 			System.out.println("Ended: " + stage.getEndDate());
 			System.out.println("Status: " + stage.getStatus());
 			if (stage.getStatus().equals(GridTestStageStatus.FINISHED_ERROR)) {
 				System.out.println("Error: "
 						+ stage.getPossibleException().getLocalizedMessage());
 			}
 			System.out.println();
 		}
 	}
 
 	protected void setPossibleExceptionForCurrentStage(Exception e) {
 		if (currentStage != null) {
 			currentStage.setPossibleException(e);
 		}
 	}
 
 	public void submitJob() {
 
 		if (!failed) {
 
 			if (beginNewStage("Submitting job to backend...")) {
 
 				try {
 					jobObject.submitJob();
 					currentStage
 							.setStatus(GridTestStageStatus.FINISHED_SUCCESS);
 				} catch (JobSubmissionException e) {
 					e.printStackTrace();
 					currentStage.setPossibleException(e);
 					currentStage.setStatus(GridTestStageStatus.FINISHED_ERROR);
 					failed = true;
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 					currentStage.setPossibleException(e);
 					currentStage.setStatus(GridTestStageStatus.FINISHED_ERROR);
 					failed = true;
 				}
 			} else {
 				currentStage.setStatus(GridTestStageStatus.NOT_EXECUTED);
 			}
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "Test: " + info.getTestname() + ", Application: "
 				+ info.getApplicationName() + ",  version: " + version
 				+ ", submissionlocation: " + submissionLocation + ", fqan: "
 				+ fqan;
 	}
 
 	// abstract public String getApplicationSupported();
 
 	public boolean wasInterrupted() {
 		return interrupted;
 	}
 
 	// abstract protected boolean useMDS();
 
 	// abstract public String getTestName();
 
 	// abstract public String getTestDescription();
 
 }
