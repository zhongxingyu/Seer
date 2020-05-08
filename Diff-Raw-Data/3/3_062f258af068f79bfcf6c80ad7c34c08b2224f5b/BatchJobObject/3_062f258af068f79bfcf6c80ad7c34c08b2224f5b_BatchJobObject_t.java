 package grisu.frontend.model.job;
 
 import grisu.control.DefaultResubmitPolicy;
 import grisu.control.JobConstants;
 import grisu.control.ResubmitPolicy;
 import grisu.control.ServiceInterface;
 import grisu.control.SpecificJobsResubmitPolicy;
 import grisu.control.exceptions.BatchJobException;
 import grisu.control.exceptions.JobPropertiesException;
 import grisu.control.exceptions.JobSubmissionException;
 import grisu.control.exceptions.NoSuchJobException;
 import grisu.control.exceptions.RemoteFileSystemException;
 import grisu.control.exceptions.StatusException;
 import grisu.frontend.control.clientexceptions.FileTransactionException;
 import grisu.frontend.control.jobMonitoring.RunningJobManager;
 import grisu.frontend.model.events.BatchJobEvent;
 import grisu.frontend.model.events.BatchJobKilledEvent;
 import grisu.frontend.model.events.NewBatchJobEvent;
 import grisu.jcommons.constants.Constants;
 import grisu.model.FileManager;
 import grisu.model.GrisuRegistryManager;
 import grisu.model.dto.DtoActionStatus;
 import grisu.model.dto.DtoBatchJob;
 import grisu.model.dto.DtoJob;
 import grisu.model.job.JobMonitoringObject;
 import grisu.model.status.ActionStatusEvent;
 import grisu.model.status.StatusObject;
 import grisu.model.status.StatusObject.Listener;
 import grisu.settings.ClientPropertiesManager;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.bushe.swing.event.EventBus;
 
 import ca.odell.glazedlists.BasicEventList;
 import ca.odell.glazedlists.EventList;
 
 import com.google.common.collect.ImmutableList;
 
 /**
  * @author markus
  *
  */
 /**
  * @author markus
  * 
  */
 public class BatchJobObject implements JobMonitoringObject,
 Comparable<BatchJobObject>, Listener {
 
 	static final Logger myLogger = Logger.getLogger(BatchJobObject.class
 			.getName());
 
 	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 
 	// properties
 	public static final String STATUS = "status";
 	public static final String FINISHED = "finished";
 	public static final String FAILED = "failed";
 	public static final String REFRESHING = "refreshing";
 	public static final String RESUBMITTING = "resubmitting";
 	public static final String NUMBER_OF_FAILED_JOBS = "numberOfFailedJobs";
 	public static final String NUMBER_OF_FINISHED_JOBS = "numberOfFinishedJobs";
 	public static final String NUMBER_OF_RUNNING_JOBS = "numberOfRunningJobs";
 	public static final String NUMBER_OF_WAITING_JOBS = "numberOfWaitingJobs";
 	public static final String NUMBER_OF_SUCCESSFULL_JOBS = "numberOfSuccessfulJobs";
 	public static final String NUMBER_OF_UNSUBMITTED_JOBS = "numberOfUnsubmittedJobs";
 	public static final String TOTAL_NUMBER_OF_JOBS = "totalNumberOfJobs";
 	public static final String SUBMITTING = "submitting";
 
 	public static final int UNDEFINED = Integer.MIN_VALUE;
 
 	public static final int DEFAULT_JOB_CREATION_RETRIES = 5;
 
 	public static final int DEFAULT_JOB_CREATION_THREADS = 5;
 
 	private Thread waitThread;
 
 	private Thread refreshThread;
 
 	private int concurrentJobCreationThreads = 0;
 
 	private int concurrentFileUploadThreads = 0;
 
 	protected final ServiceInterface serviceInterface;
 	private final String batchJobname;
 
 	private String submissionFqan;
 
 	private final EventList<JobObject> jobs = new BasicEventList<JobObject>();
 
 	private final List<JobObject> newlyAddedJobs = Collections
 	.synchronizedList(new LinkedList<JobObject>());
 
 	private final Map<String, String> inputFiles = new HashMap<String, String>();
 
 	private DtoBatchJob dtoBatchJob = null;
 	private String[] submissionLocationsToInclude;
 
 	private String[] submissionLocationsToExclude;
 	private int maxWalltimeInSecondsAcrossJobs = 3600;
 
 	private int defaultWalltime = 3600;
 	private String defaultApplication = Constants.GENERIC_APPLICATION_NAME;
 
 	private String defaultVersion = Constants.NO_VERSION_INDICATOR_STRING;
 
 	private int defaultNoCpus = 1;
 
 	private String[] allRemoteJobnames;
 
 	private boolean isRefreshing = false;
 	private boolean isResubmitting = false;
 	private boolean isBeingKilled = false;
 	private boolean isKilled = false;
 
 	private String optimizationResult = null;
 
 	private final List<String> submissionLog = Collections
 	.synchronizedList(new LinkedList<String>());
 
 	private boolean isSubmitting = false;
 
 	/**
 	 * Use this constructor to create a BatchJobObject for a batchjob that
 	 * already exists on the backend.
 	 * 
 	 * @param serviceInterface
 	 *            the serviceinterface
 	 * @param batchJobname
 	 *            the id of the batchjob
 	 * @param refreshJobStatusOnBackend
 	 *            whether to refresh the status of the jobs on the backend.
 	 *            might take quite a while...
 	 * 
 	 * @throws BatchJobException
 	 *             if one of the jobs of the batchjob doesn't exist on the
 	 *             backend
 	 * @throws NoSuchJobException
 	 *             if there is no such batchjob on the backend
 	 */
 	public BatchJobObject(ServiceInterface serviceInterface,
 			String batchJobname, boolean refreshJobStatusOnBackend)
 	throws BatchJobException, NoSuchJobException {
 
 		this.serviceInterface = serviceInterface;
 		this.batchJobname = batchJobname;
 		try {
 
 			dtoBatchJob = getWrappedDtoBatchJob(refreshJobStatusOnBackend);
 
 			if (dtoBatchJob == null) {
 				throw new NoSuchJobException("Could not access batchjob "
 						+ batchJobname + " on the backend.");
 			}
 
 			this.submissionFqan = dtoBatchJob.getSubmissionFqan();
 
 			setDefaultApplication(serviceInterface.getJobProperty(
 					this.batchJobname, Constants.APPLICATIONNAME_KEY));
 			setDefaultVersion(serviceInterface.getJobProperty(
 					this.batchJobname, Constants.APPLICATIONVERSION_KEY));
 
 			addJobLogMessage("Job loaded from backend.");
 		} catch (final Exception e) {
 			e.printStackTrace(System.err);
 			throw new BatchJobException(e.getLocalizedMessage());
 		}
 
 	}
 
 	/**
 	 * Use this constructor if you want to create a new batchjob.
 	 * 
 	 * This constructor creates a batchjob using the
 	 * {@link Constants#UNIQUE_NUMBER_METHOD} to calculate the batchjob name.
 	 * 
 	 * @param serviceInterface
 	 *            the serviceinterface
 	 * @param batchJobname
 	 *            the id of the batchjob
 	 * @param submissionFqan
 	 *            the VO to use to submit the jobs of this batchjob
 	 * @throws BatchJobException
 	 *             if the batchjob can't be created, most likely because the
 	 *             jobname already exists on the backend.
 	 */
 	public BatchJobObject(ServiceInterface serviceInterface,
 			String batchJobname, String submissionFqan,
 			String defaultApplication, String defaultVersion)
 	throws BatchJobException {
 		this.serviceInterface = serviceInterface;
 		this.submissionFqan = submissionFqan;
 
 		dtoBatchJob = serviceInterface.createBatchJob(batchJobname,
 				this.submissionFqan, Constants.UNIQUE_NUMBER_METHOD);
 
 		this.batchJobname = dtoBatchJob.getBatchJobname();
 
 		if (StringUtils.isBlank(defaultApplication)) {
 			defaultApplication = Constants.GENERIC_APPLICATION_NAME;
 		}
 		if (StringUtils.isBlank(defaultVersion)) {
 			defaultVersion = Constants.NO_VERSION_INDICATOR_STRING;
 		}
 		setDefaultApplication(defaultApplication);
 		setDefaultVersion(defaultVersion);
 
 		addJobLogMessage("Empty job created.");
 	}
 
 	/**
 	 * Use this constructor if you want to create a new batchjob and specify the
 	 * way the batchjobname is created.
 	 * 
 	 * Look up the {@link Constants} class to see available jobname creation
 	 * methods.
 	 * 
 	 * @param serviceInterface
 	 *            the serviceinterface
 	 * @param batchJobnameBase
 	 *            the base of the batchjob-name
 	 * @param jobnameCreationMethod
 	 *            the jobname creation method
 	 * @param submissionFqan
 	 *            the VO to use to submit the jobs of this batchjob
 	 * @throws BatchJobException
 	 *             if the batchjob can't be created
 	 */
 	public BatchJobObject(ServiceInterface serviceInterface,
 			String batchJobnameBase, String jobnameCreationMethod,
 			String submissionFqan,
 			String defaultApplication, String defaultVersion)
 	throws BatchJobException {
 		this.serviceInterface = serviceInterface;
 		this.submissionFqan = submissionFqan;
 
 		dtoBatchJob = serviceInterface.createBatchJob(batchJobnameBase,
 				this.submissionFqan, jobnameCreationMethod);
 
 		this.batchJobname = dtoBatchJob.getBatchJobname();
 
 		if (StringUtils.isBlank(defaultApplication)) {
 			defaultApplication = Constants.GENERIC_APPLICATION_NAME;
 		}
 		if (StringUtils.isBlank(defaultVersion)) {
 			defaultVersion = Constants.NO_VERSION_INDICATOR_STRING;
 		}
 		setDefaultApplication(defaultApplication);
 		setDefaultVersion(defaultVersion);
 
 		addJobLogMessage("Empty job created.");
 	}
 
 	/**
 	 * Adds an input file to the pool of shared input files for this batchjob.
 	 * 
 	 * Those get staged in to the common directory on every site that runs parts
 	 * of this batchjob. You can access the relative path from each job
 	 * directory via the {@link #pathToInputFiles()} method. The original
 	 * filename is used.
 	 * 
 	 * Be aware that, if you are specifying a folder here, the containing files
 	 * will not be uploaded concurrently (for reference:
 	 * {@link #setConcurrentInputFileUploadThreads(int)}) but one after another
 	 * If you would specify 2 folders as input, those, however, would be
 	 * uploaded in parallel (if the concurrentUploadThreads parameter is set of
 	 * course).
 	 * 
 	 * @param inputFile
 	 *            the input file
 	 */
 	public void addInputFile(String inputFile) {
 
 		inputFile = FileManager.ensureUriFormat(inputFile);
 
 		if (FileManager.isLocal(inputFile)) {
 			inputFiles.put(inputFile, new File(inputFile).getName());
 		} else {
 			FileManager.getFilename(inputFile);
 			inputFiles.put(inputFile, FileManager.getFilename(inputFile));
 		}
 
 		addJobLogMessage("Added job-wide input file: " + inputFile);
 	}
 
 	/**
 	 * Adds an input file to the pool of shared input files for this batchjob.
 	 * 
 	 * Those get staged in to the common directory on every site that runs parts
 	 * of this batchjob. You can access the relative path from each job
 	 * directory via the {@link #pathToInputFiles()} method.
 	 * 
 	 * Be aware that, if you are specifying a folder here, the containing files
 	 * will not be uploaded concurrently (for reference:
 	 * {@link #setConcurrentInputFileUploadThreads(int)}) but one after another
 	 * If you would specify 2 folders as input, those, however, would be
 	 * uploaded in parallel (if the concurrentUploadThreads parameter is set of
 	 * course).
 	 * 
 	 * 
 	 * @param inputFile
 	 *            the input file
 	 * @param targetFilename
 	 *            the path/filename in the common directory
 	 */
 	public void addInputFile(String inputFile, String targetFilename) {
 		inputFile = FileManager.ensureUriFormat(inputFile);
 		inputFiles.put(inputFile, targetFilename);
 		addJobLogMessage("Added job-wide input file: " + inputFile);
 	}
 
 	/**
 	 * Adds a new job object to this batchjob.
 	 * 
 	 * @param job
 	 *            the new job object
 	 */
 	public void addJob(JobObject job) throws IllegalArgumentException {
 
 		if (StringUtils.isBlank(job.getJobname())) {
 			String singleJobname = GrisuRegistryManager
 			.getDefault(serviceInterface)
 			.getUserEnvironmentManager()
					.calculateUniqueJobname(
							getJobname() + "_job_" + getJobs().size() + 1);
 			job.setJobname(singleJobname);
 		}
 
 		if (getJobs().contains(job)) {
 			throw new IllegalArgumentException("Job: " + job.getJobname()
 					+ " already part of this BatchJob.");
 		}
 
 		if (Arrays.binarySearch(getAllRemoteJobnames(), job.getJobname()) >= 0) {
 			throw new IllegalArgumentException("Job: " + job.getJobname()
 					+ " already exists on the backend.");
 		}
 
 		if (job.getWalltimeInSeconds() <= 0) {
 			EventBus.publish(this.batchJobname, new BatchJobEvent(this,
 					"Setting walltime for job " + job.getJobname()
 					+ " to default walltime: " + defaultWalltime));
 			job.setWalltimeInSeconds(defaultWalltime);
 		} else {
 
 			if (job.getWalltimeInSeconds() > maxWalltimeInSecondsAcrossJobs) {
 				maxWalltimeInSecondsAcrossJobs = job.getWalltimeInSeconds();
 			}
 		}
 
 
 		EventBus.publish(this.batchJobname, new BatchJobEvent(this,
 				"Adding job " + job.getJobname()));
 		final int oldNo = this.getJobs().size();
 		this.getJobs().add(job);
 		this.getNewlyAddedJobs().add(job);
 
 		pcs.firePropertyChange("totalNumberOfJobs", oldNo, this.getJobs()
 				.size());
 		pcs.firePropertyChange("jobs", null, getJobs());
 		addJobLogMessage("Added new sub-job: " + job.getJobname());
 
 	}
 
 	private void addJobLogMessage(String message) {
 		this.submissionLog.add(message);
 		pcs.firePropertyChange("submissionLog", null, getSubmissionLog());
 	}
 
 	/**
 	 * Adds a job property to this job.
 	 * 
 	 * @param key
 	 *            the key
 	 * @param value
 	 *            the value
 	 */
 	public void addJobProperty(String key, String value) {
 
 		try {
 			serviceInterface.addJobProperty(batchJobname, key, value);
 			addJobLogMessage("Added job property: " + key + "/" + value);
 		} catch (final NoSuchJobException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		this.pcs.addPropertyChangeListener(listener);
 	}
 
 	private void checkInterruptedStatus(ExecutorService executor,
 			List<Future<?>> tasks) throws InterruptedException {
 
 		if (Thread.currentThread().isInterrupted()) {
 			executor.shutdownNow();
 
 			for (final Future<?> f : tasks) {
 				f.cancel(true);
 			}
 
 			throw new InterruptedException("Upload input files interrupted.");
 		}
 
 	}
 
 	public int compareTo(BatchJobObject o) {
 
 		return this.getJobname().compareTo(o.getJobname());
 	}
 
 	private void createWaitThread(final int checkIntervallInSeconds) {
 
 		try {
 			// just to make sure we don't create 2 or more threads. Should never
 			// happen.
 			waitThread.interrupt();
 		} catch (final Exception e) {
 			myLogger.debug(e);
 		}
 
 		waitThread = new Thread() {
 			@Override
 			public void run() {
 
 				int oldStatus = getStatus(false);
 				while (oldStatus < JobConstants.BATCH_JOB_FINISHED) {
 
 					if (isInterrupted()) {
 						return;
 					}
 					// if (oldStatus != getStatus(true)) {
 					// EventBus.publish(new BatchJobStatusEvent(
 					// BatchJobObject.this, oldStatus,
 					// BatchJobObject.this.getStatus(false)));
 					// if (StringUtils.isNotBlank(getJobname())) {
 					// EventBus.publish(
 					// BatchJobObject.this.getJobname(),
 					// new BatchJobStatusEvent(
 					// BatchJobObject.this, oldStatus,
 					// BatchJobObject.this
 					// .getStatus(false)));
 					// }
 					// }
 
 					try {
 						Thread.sleep(checkIntervallInSeconds * 1000);
 					} catch (final InterruptedException e) {
 						myLogger.debug("Wait thread for job " + getJobname()
 								+ " interrupted.");
 						return;
 					}
 					oldStatus = getStatus(true);
 					// X.p("New status: " + oldStatus);
 
 					if (isInterrupted()) {
 						return;
 					}
 				}
 				myLogger.debug("BatchJob finished. Exit monitoring...");
 			}
 		};
 
 	}
 
 	public Set<String> currentlyUsedSubmissionLocations() {
 
 		final DtoBatchJob temp = getWrappedDtoBatchJob(false);
 		if (temp == null) {
 			return new HashSet<String>();
 		}
 		return temp.currentlyUsedSubmissionLocations();
 	}
 
 	/**
 	 * Downloads all the required results for this batch job.
 	 * 
 	 * @param onlyDownloadWhenFinished
 	 *            only download results if the (single) job is finished.
 	 * @param parentFolder
 	 *            the folder to download all the results to
 	 * @param patterns
 	 *            a list of patterns that specify which files to download
 	 * @param createSeperateFoldersForEveryJob
 	 *            whether to create a seperete folder for every job (true) or
 	 *            download everything into the same folder (false)
 	 * @param prefixWithJobname
 	 *            whether to prefix downloaded results with the jobname it
 	 *            belongs to (true) or not (false)
 	 * @throws RemoteFileSystemException
 	 *             if a remote filetransfer fails
 	 * @throws FileTransactionException
 	 *             if a filetransfer fails
 	 * @throws IOException
 	 *             if a file can't be saved
 	 */
 	public void downloadResults(boolean onlyDownloadWhenFinished,
 			File parentFolder, String[] patterns,
 			boolean createSeperateFoldersForEveryJob, boolean prefixWithJobname)
 	throws RemoteFileSystemException, FileTransactionException,
 	IOException {
 
 		EventBus.publish(this.batchJobname, new BatchJobEvent(this,
 				"Checking and possibly downloading output files for batchjob: "
 				+ batchJobname + ". This might take a while..."));
 
 		for (final JobObject job : getJobs()) {
 
 			if (onlyDownloadWhenFinished && !job.isFinished(false)) {
 				continue;
 			}
 
 			for (final String child : job.listJobDirectory(-1)) {
 				final String temp = FileManager.getFilename(child);
 				boolean download = false;
 				for (final String pattern : patterns) {
 					if (temp.indexOf(pattern) >= 0) {
 						download = true;
 						break;
 					}
 				}
 
 				if (download) {
 					File cacheFile = null;
 
 					boolean needsDownloading = false;
 
 					try {
 						needsDownloading = GrisuRegistryManager
 						.getDefault(serviceInterface).getFileManager()
 						.needsDownloading(child);
 					} catch (final RuntimeException e) {
 						myLogger.error("Could not access file " + child + ": "
 								+ e.getLocalizedMessage());
 						EventBus.publish(this.batchJobname, new BatchJobEvent(
 								this, "Could not access file " + child + ": "
 								+ e.getLocalizedMessage()));
 						continue;
 					}
 
 					if (needsDownloading) {
 						myLogger.debug("Downloading file: " + child);
 						EventBus.publish(this.batchJobname, new BatchJobEvent(
 								this, "Downloading file: " + child));
 						try {
 							cacheFile = GrisuRegistryManager
 							.getDefault(serviceInterface)
 							.getFileManager().downloadFile(child);
 						} catch (final Exception e) {
 							myLogger.error("Could not download file " + child
 									+ ": " + e.getLocalizedMessage());
 							EventBus.publish(
 									this.batchJobname,
 									new BatchJobEvent(this,
 											"Could not download file " + child
 											+ ": "
 											+ e.getLocalizedMessage()));
 							continue;
 						}
 					} else {
 						cacheFile = GrisuRegistryManager
 						.getDefault(serviceInterface).getFileManager()
 						.getLocalCacheFile(child);
 					}
 					String targetfilename = null;
 					if (prefixWithJobname) {
 						targetfilename = job.getJobname() + "_"
 						+ cacheFile.getName();
 					} else {
 						targetfilename = cacheFile.getName();
 					}
 					if (createSeperateFoldersForEveryJob) {
 						FileUtils.copyFile(
 								cacheFile,
 								new File(new File(parentFolder, job
 										.getJobname()), targetfilename));
 					} else {
 						FileUtils.copyFile(cacheFile, new File(parentFolder,
 								targetfilename));
 					}
 				}
 			}
 
 		}
 
 	}
 
 	/**
 	 * Downloads all the required results for this batch job.
 	 * 
 	 * @param onlyDownloadWhenFinished
 	 *            only download results if the (single) job is finished.
 	 * @param parentFolder
 	 *            the folder to download all the results to
 	 * @param patterns
 	 *            a list of patterns that specify which files to download
 	 * @param createSeperateFoldersForEveryJob
 	 *            whether to create a seperete folder for every job (true) or
 	 *            download everything into the same folder (false)
 	 * @param prefixWithJobname
 	 *            whether to prefix downloaded results with the jobname it
 	 *            belongs to (true) or not (false)
 	 * @throws RemoteFileSystemException
 	 *             if a remote filetransfer fails
 	 * @throws FileTransactionException
 	 *             if a filetransfer fails
 	 * @throws IOException
 	 *             if a file can't be saved
 	 */
 	public void downloadResults(boolean onlyDownloadWhenFinished,
 			String parentFolder, String[] patterns,
 			boolean createSeperateFoldersForEveryJob, boolean prefixWithJobname)
 	throws RemoteFileSystemException, FileTransactionException,
 	IOException {
 
 		File parent = new File(parentFolder);
 
 		if (!parent.exists() ) {
 			boolean worked = parent.mkdir();
 
 			if ( ! worked){
 				throw new FileTransactionException(null, parentFolder, "Can't create parent folder directory: "+parentFolder, null);
 			}
 		}
 
 		if ( !parent.isDirectory() ) {
 			throw new FileTransactionException(null, parentFolder, "Parent folder not a directory: "+parentFolder, null);
 		}
 
 		downloadResults(onlyDownloadWhenFinished, parent, patterns,
 				createSeperateFoldersForEveryJob, prefixWithJobname);
 	}
 
 	@Override
 	public boolean equals(Object o) {
 
 		if (!(o instanceof BatchJobObject)) {
 			return false;
 		}
 
 		final BatchJobObject other = (BatchJobObject) o;
 
 		return getJobname().equals(other.getJobname());
 
 	}
 
 	/**
 	 * Returns all failed jobs.
 	 * 
 	 * @return all failed jobs
 	 */
 	public SortedSet<DtoJob> failedJobs() {
 		final DtoBatchJob temp = getWrappedDtoBatchJob(false);
 		if (temp == null) {
 			return new TreeSet<DtoJob>();
 		}
 		return temp.failedJobs();
 	}
 
 	/**
 	 * Returns all finished jobs.
 	 * 
 	 * @return all finished jobs
 	 */
 	public SortedSet<DtoJob> finishedJobs() {
 		return getWrappedDtoBatchJob(false).finishedJobs();
 	}
 
 	private String[] getAllRemoteJobnames() {
 		if (allRemoteJobnames == null) {
 			allRemoteJobnames = serviceInterface.getAllJobnames(null).asArray();
 			Arrays.sort(allRemoteJobnames);
 		}
 		return allRemoteJobnames;
 	}
 
 	public String getApplication() {
 		return getDefaultApplication();
 	}
 
 	/**
 	 * This method returns how many input file upload threads run at the same
 	 * time.
 	 * 
 	 * @return the number of threads
 	 */
 	public int getConcurrentInputFileUploadThreads() {
 		if (concurrentFileUploadThreads <= 0) {
 			return ClientPropertiesManager.getConcurrentUploadThreads();
 		} else {
 			return concurrentFileUploadThreads;
 		}
 	}
 
 	/**
 	 * This method returns how many job submission threads run at the same time.
 	 * 
 	 * @return the number of threads
 	 */
 	public int getConcurrentJobCreationThreads() {
 		if (concurrentJobCreationThreads <= 0) {
 			return DEFAULT_JOB_CREATION_THREADS;
 		} else {
 			return concurrentJobCreationThreads;
 		}
 	}
 
 	public Set<String> getCurrentlyRunningOrSuccessfullSubmissionLocations() {
 
 		final DtoBatchJob temp = getWrappedDtoBatchJob(false);
 		if (temp == null) {
 			return new HashSet<String>();
 		}
 		return temp.currentlyRunningOrSuccessfullSubmissionLocations();
 	}
 
 	public Set<String> getCurrentlyUsedSubmissionLocations() {
 		final DtoBatchJob temp = getWrappedDtoBatchJob(false);
 		if (temp == null) {
 			return new HashSet<String>();
 		}
 		return temp.currentlyUsedSubmissionLocations();
 	}
 
 	/**
 	 * Returns the default application for this batchjob.
 	 * 
 	 * @return the default application
 	 */
 	public String getDefaultApplication() {
 		return defaultApplication;
 	}
 
 	/**
 	 * Gets the default number of cpus.
 	 * 
 	 * This is used internally to use mds to calculate job distribution.
 	 * 
 	 * @return the default number of cpus across jobs
 	 */
 	public int getDefaultNoCpus() {
 
 		return defaultNoCpus;
 	}
 
 	/**
 	 * Gets the default version for this batchjob.
 	 * 
 	 * This is used internally to use mds to calculate job distribution.
 	 * 
 	 * @return the default version
 	 */
 	public String getDefaultVersion() {
 		return defaultVersion;
 	}
 
 	/**
 	 * If a defaultWalltime is set, this method returns it.
 	 * 
 	 * @return the default walltime.
 	 */
 	public int getDefaultWalltime() {
 		return this.defaultWalltime;
 	}
 
 	/**
 	 * A summary of the status of this job.
 	 * 
 	 * @return a job status detail string
 	 */
 	public String getDetails() {
 
 		final DtoBatchJob temp = getWrappedDtoBatchJob(false);
 
 		if (temp == null) {
 			return "Could not find job.";
 		}
 
 		final StringBuffer buffer = new StringBuffer("Details:\n\n");
 
 		buffer.append("Waiting jobs:\n");
 		if (temp.numberOfWaitingJobs() == 0) {
 			buffer.append("\tNo waiting jobs.\n");
 		} else {
 			for (final DtoJob job : temp.waitingJobs()) {
 				buffer.append("\t" + job.jobname() + ":\t"
 						+ job.statusAsString() + " (submitted to: "
 						+ job.jobProperty(Constants.SUBMISSION_SITE_KEY) + ", "
 						+ job.jobProperty(Constants.QUEUE_KEY) + ")\n");
 			}
 		}
 		buffer.append("Active jobs:\n");
 		if (temp.numberOfRunningJobs() == 0) {
 			buffer.append("\tNo active jobs.\n");
 		} else {
 			for (final DtoJob job : temp.runningJobs()) {
 				buffer.append("\t" + job.jobname() + ":\t"
 						+ job.statusAsString() + " (submitted to: "
 						+ job.jobProperty(Constants.SUBMISSION_SITE_KEY) + ", "
 						+ job.jobProperty(Constants.QUEUE_KEY) + ")\n");
 			}
 		}
 		buffer.append("Successful jobs:\n");
 		if (temp.numberOfSuccessfulJobs() == 0) {
 			buffer.append("\tNo successful jobs.\n");
 		} else {
 			for (final DtoJob job : temp.successfulJobs()) {
 				buffer.append("\t" + job.jobname() + ":\t"
 						+ job.statusAsString() + " (submitted to: "
 						+ job.jobProperty(Constants.SUBMISSION_SITE_KEY) + ", "
 						+ job.jobProperty(Constants.QUEUE_KEY) + ")\n");
 			}
 		}
 		buffer.append("Failed jobs:\n");
 		if (temp.numberOfFailedJobs() == 0) {
 			buffer.append("\tNo failed jobs.\n");
 		} else {
 			for (final DtoJob job : temp.failedJobs()) {
 				buffer.append("\t" + job.jobname() + ":\t"
 						+ job.statusAsString() + " (submitted to: "
 						+ job.jobProperty(Constants.SUBMISSION_SITE_KEY) + ", "
 						+ job.jobProperty(Constants.QUEUE_KEY) + ")\n");
 			}
 		}
 
 		buffer.append("\n");
 
 		return buffer.toString();
 
 	}
 
 	/**
 	 * Returns the fqan that is used for this batchjob.
 	 * 
 	 * @return the fqan
 	 */
 	public String getFqan() {
 		return submissionFqan;
 	}
 
 	/**
 	 * Returns all the input files that are shared among the jobs of this
 	 * batchjob.
 	 * 
 	 * @return the urls of all the input files (local & remote)
 	 */
 	public Map<String, String> getInputFiles() {
 		return inputFiles;
 	}
 
 	/**
 	 * The name of this batchjob.
 	 * 
 	 * @return the id
 	 */
 	public String getJobname() {
 		return batchJobname;
 	}
 
 	/**
 	 * Retrieves a list of all jobs that are part of this batchjob.
 	 * 
 	 * @return all jobs
 	 */
 	public EventList<JobObject> getJobs() {
 
 		return this.jobs;
 	}
 
 	public List<String> getListOfOutputFiles(boolean onlyWhenSubJobFinished,
 			String[] patterns) throws RemoteFileSystemException {
 
 		final List<String> files = new LinkedList<String>();
 
 		for (final JobObject job : getJobs()) {
 
 			if (onlyWhenSubJobFinished && !job.isFinished(false)) {
 				continue;
 			}
 
 			for (final String child : job.listJobDirectory(0)) {
 				final String temp = FileManager.getFilename(child);
 				for (final String pattern : patterns) {
 					if (temp.indexOf(pattern) >= 0) {
 						files.add(child);
 						break;
 					}
 				}
 
 			}
 		}
 
 		return files;
 
 	}
 
 	/**
 	 * Returns all the log messages for this batchjob.
 	 * 
 	 * @param refresh
 	 *            whether to refresh the job on the backend or not
 	 * @return the log messages
 	 */
 	public Map<Date, String> getLogMessages(boolean refresh) {
 
 		final DtoBatchJob temp = getWrappedDtoBatchJob(false);
 		if (temp == null) {
 			return new TreeMap<Date, String>();
 		}
 		return temp.messages();
 
 	}
 
 	/**
 	 * Gets the maximum walltime for this batchjob.
 	 * 
 	 * This is used internally to calculate the job distribution. If it is not
 	 * set manually the largest single job walltime is used.
 	 * 
 	 * @return the max walltime in seconds
 	 */
 	public int getMaxWalltimeInSeconds() {
 		return maxWalltimeInSecondsAcrossJobs;
 	}
 
 	private List<JobObject> getNewlyAddedJobs() {
 
 		return newlyAddedJobs;
 
 	}
 
 	/**
 	 * The number of failed jobs for this batchjob.
 	 * 
 	 * @return the number of failed jobs
 	 */
 	public int getNumberOfFailedJobs() {
 		final DtoBatchJob temp = getWrappedDtoBatchJob(false);
 		if (temp == null) {
 			return 0;
 		}
 		return temp.numberOfFailedJobs();
 	}
 
 	/**
 	 * The number of finished jobs for this batchjob.
 	 * 
 	 * @return the number of finished jobs
 	 */
 	public int getNumberOfFinishedJobs() {
 		final DtoBatchJob temp = getWrappedDtoBatchJob(false);
 		if (temp == null) {
 			return 0;
 		}
 		return temp.numberOfFinishedJobs();
 	}
 
 	/**
 	 * The number of running jobs for this batchjob.
 	 * 
 	 * @return the number of running jobs
 	 */
 	public int getNumberOfRunningJobs() {
 		final DtoBatchJob temp = getWrappedDtoBatchJob(false);
 		if (temp == null) {
 			return 0;
 		}
 		return temp.numberOfRunningJobs();
 	}
 
 	/**
 	 * The number of successful jobs for this batchjob.
 	 * 
 	 * @return the number of successful jobs
 	 */
 	public int getNumberOfSuccessfulJobs() {
 		final DtoBatchJob temp = getWrappedDtoBatchJob(false);
 		if (temp == null) {
 			return 0;
 		}
 		return temp.numberOfSuccessfulJobs();
 	}
 
 	/**
 	 * The number of unsubmitted jobs for this batchjob.
 	 * 
 	 * @return the number of unsubmitted jobs
 	 */
 	public int getNumberOfUnsubmittedJobs() {
 		final DtoBatchJob temp = getWrappedDtoBatchJob(false);
 		if (temp == null) {
 			return 0;
 		}
 		return temp.numberOfUnsubmittedJobs();
 	}
 
 	/**
 	 * The number of waiting jobs for this batchjob.
 	 * 
 	 * @return the number of waiting jobs
 	 */
 	public int getNumberOfWaitingJobs() {
 		final DtoBatchJob temp = getWrappedDtoBatchJob(false);
 		if (temp == null) {
 			return 0;
 		}
 		return temp.numberOfWaitingJobs();
 	}
 
 	/**
 	 * Info about how many jobs were submitted to which submission location.
 	 * 
 	 * You need to call this sometime after the
 	 * {@link #prepareAndCreateJobs(boolean)} method. You need to use the same
 	 * BatchJobObject object where you called this method. If you re-create the
 	 * object, this info will be lost.
 	 * 
 	 * @return info about job distribution
 	 */
 	public String getOptimizationResult() {
 		return optimizationResult;
 	}
 
 	/**
 	 * Displays a summary of the job status.
 	 * 
 	 * @param restarter
 	 *            an (optional) FailedJobRestarter to restart failed jobs or
 	 *            null
 	 * @return the progress summary
 	 */
 	public String getProgress() {
 
 		DtoBatchJob temp;
 		temp = getWrappedDtoBatchJob(false);
 
 		if (temp == null) {
 			return "Could not access job.";
 		}
 
 		final StringBuffer output = new StringBuffer();
 
 		output.append("Total number of jobs: " + temp.totalNumberOfJobs()
 				+ "\n");
 		output.append("Waiting jobs: " + temp.numberOfWaitingJobs() + "\n");
 		output.append("Active jobs: " + temp.numberOfRunningJobs() + "\n");
 		output.append("Successful jobs: " + temp.numberOfSuccessfulJobs()
 				+ "\n");
 		output.append("Failed jobs: " + temp.numberOfFailedJobs() + "\n");
 		if (temp.numberOfFailedJobs() > 0) {
 
 			// if (restarter != null) {
 			// restartFailedJobs(restarter);
 			// }
 
 		} else {
 			output.append("\n");
 		}
 		output.append("Unsubmitted jobs: " + temp.numberOfUnsubmittedJobs()
 				+ "\n");
 
 		return output.toString();
 	}
 
 	/**
 	 * Returns all the properties for this batchjob.
 	 * 
 	 * This method doesn't refresh the underlying object, you might want to do
 	 * that yourself in some cases.
 	 * 
 	 * @return the properties
 	 */
 	public Map<String, String> getProperties() {
 		final DtoBatchJob temp = getWrappedDtoBatchJob(false);
 		if (temp == null) {
 			return new TreeMap<String, String>();
 		}
 		return temp.propertiesAsMap();
 	}
 
 	/**
 	 * Gets a job property for this job.
 	 * 
 	 * @param key
 	 *            the key
 	 * @return the value
 	 */
 	public String getProperty(String key) {
 
 		try {
 			return serviceInterface.getJobProperty(batchJobname, key);
 		} catch (final NoSuchJobException e) {
 			throw new RuntimeException();
 		}
 	}
 
 	public int getStatus(boolean refresh) {
 
 		final DtoBatchJob temp = getWrappedDtoBatchJob(refresh);
 		if (temp == null) {
 			return JobConstants.UNDEFINED;
 		}
 		return temp.getStatus();
 	}
 
 	public Map<Integer, Map<String, Integer>> getStatusMap() {
 
 		final DtoBatchJob temp = getWrappedDtoBatchJob(false);
 		if (temp == null) {
 			return new TreeMap<Integer, Map<String, Integer>>();
 		}
 		return temp.statusMap();
 
 	}
 
 	public List<String> getSubmissionLog() {
 		return submissionLog;
 	}
 
 	public int getTotalNumberOfJobs() {
 		final DtoBatchJob temp = getWrappedDtoBatchJob(false);
 		if (temp == null) {
 			return 0;
 		}
 		return temp.totalNumberOfJobs();
 	}
 
 	private DtoBatchJob getWrappedDtoBatchJob(boolean refresh) {
 		return getWrappedDtoBatchJob(refresh, true);
 	}
 
 	private DtoBatchJob getWrappedDtoBatchJob(final boolean refresh,
 			final boolean waitForRefresh) {
 
 		if (isKilled || isBeingKilled) {
 			myLogger.debug("Job is or is being killed. Not updating dtoobject...");
 			return dtoBatchJob;
 		}
 
 		if (((dtoBatchJob == null) || (!isRefreshing() && refresh))
 				|| (refreshThread == null)
 				|| ((refreshThread != null) && !refreshThread.isAlive())) {
 
 			if ((dtoBatchJob == null) || refresh) {
 
 				refreshThread = new Thread() {
 					@Override
 					public void run() {
 						try {
 							if (refresh) {
 								try {
 									refreshBatchJobStatus(true);
 								} catch (final InterruptedException e) {
 									return;
 								}
 							}
 
 							// getJobs().clear();
 							int oldTotalJobs = UNDEFINED;
 							int oldStatus = UNDEFINED;
 							int oldRunningJobs = UNDEFINED;
 							int oldWaitingJobs = UNDEFINED;
 							int oldFinishedJobs = UNDEFINED;
 							int oldFailedJobs = UNDEFINED;
 							int oldSuccessJobs = UNDEFINED;
 							int oldUnsubmittedJobs = UNDEFINED;
 							boolean oldFailed = false;
 							boolean oldFinished = false;
 
 							if (dtoBatchJob != null) {
 								oldTotalJobs = dtoBatchJob
 								.totalNumberOfJobs();
 								oldRunningJobs = dtoBatchJob
 								.numberOfRunningJobs();
 								oldWaitingJobs = dtoBatchJob
 								.numberOfWaitingJobs();
 								oldFinishedJobs = dtoBatchJob
 								.numberOfFinishedJobs();
 								oldFailedJobs = dtoBatchJob
 								.numberOfFailedJobs();
 								oldSuccessJobs = dtoBatchJob
 								.numberOfSuccessfulJobs();
 								oldUnsubmittedJobs = dtoBatchJob
 								.numberOfUnsubmittedJobs();
 								oldStatus = dtoBatchJob.getStatus();
 								oldFailed = dtoBatchJob.failed();
 								oldFinished = dtoBatchJob.isFinished();
 							}
 
 							dtoBatchJob = serviceInterface
 							.getBatchJob(batchJobname);
 
 							pcs.firePropertyChange(TOTAL_NUMBER_OF_JOBS,
 									oldTotalJobs,
 									dtoBatchJob.totalNumberOfJobs());
 							pcs.firePropertyChange(STATUS, oldStatus,
 									dtoBatchJob.getStatus());
 							pcs.firePropertyChange(FAILED, oldFailed,
 									dtoBatchJob.failed());
 							pcs.firePropertyChange(FINISHED, oldFinished,
 									dtoBatchJob.isFinished());
 							pcs.firePropertyChange(NUMBER_OF_RUNNING_JOBS,
 									oldRunningJobs,
 									dtoBatchJob.numberOfRunningJobs());
 							pcs.firePropertyChange(NUMBER_OF_WAITING_JOBS,
 									oldWaitingJobs,
 									dtoBatchJob.numberOfWaitingJobs());
 							pcs.firePropertyChange(NUMBER_OF_FINISHED_JOBS,
 									oldFinishedJobs,
 									dtoBatchJob.numberOfFinishedJobs());
 							pcs.firePropertyChange(NUMBER_OF_FAILED_JOBS,
 									oldFailedJobs,
 									dtoBatchJob.numberOfFailedJobs());
 							pcs.firePropertyChange(NUMBER_OF_SUCCESSFULL_JOBS,
 									oldSuccessJobs,
 									dtoBatchJob.numberOfSuccessfulJobs());
 							pcs.firePropertyChange(NUMBER_OF_UNSUBMITTED_JOBS,
 									oldUnsubmittedJobs,
 									dtoBatchJob.numberOfUnsubmittedJobs());
 
 							RunningJobManager.updateJobList(serviceInterface,
 									getJobs(), dtoBatchJob.getJobs());
 
 						} catch (final NoSuchJobException e) {
 							e.printStackTrace();
 							if (isBeingKilled || isKilled) {
 								return;
 							}
 							throw new RuntimeException(e);
 						}
 					}
 				};
 
 				refreshThread.start();
 			}
 
 			if ((refreshThread != null)
 					&& ((dtoBatchJob == null) || waitForRefresh)) {
 				try {
 					refreshThread.join();
 				} catch (final InterruptedException e) {
 					throw new RuntimeException(e);
 				}
 			}
 		}
 		return dtoBatchJob;
 	}
 
 	public boolean isBatchJob() {
 		return true;
 	}
 
 	public boolean isBeingKilled() {
 		return isBeingKilled;
 	}
 
 	/**
 	 * Returns whether all jobs within this batchjob are finished (failed or
 	 * not).
 	 * 
 	 * @param refresh
 	 *            whether to refresh all jobs on the backend
 	 * @return whether all jobs are finished or not.
 	 */
 	public boolean isFinished(boolean refresh) {
 		try {
 			final DtoBatchJob temp = getWrappedDtoBatchJob(refresh);
 			if (temp == null) {
 				return false;
 			}
 			return temp.isFinished();
 		} catch (final Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 
 	public boolean isKilled() {
 		return isKilled;
 	}
 
 	public boolean isRefreshing() {
 		return this.isRefreshing;
 	}
 
 	public boolean isResubmitting() {
 		return isResubmitting;
 	}
 
 	public boolean isSubmitting() {
 		return isSubmitting;
 	}
 
 	/**
 	 * Returns whether all jobs within this batchjob finished successfully.
 	 * 
 	 * @param refresh
 	 *            whether to refresh all jobs on the backend
 	 * @return whether all jobs finished successfully
 	 */
 	public boolean isSuccessful(boolean refresh) {
 		final DtoBatchJob temp = getWrappedDtoBatchJob(refresh);
 		if (temp == null) {
 			return false;
 		}
 		return temp.allJobsFinishedSuccessful();
 	}
 
 	public void kill(boolean clean, boolean waitForCompletion) {
 
 		if (refreshThread != null) {
 			try {
 				refreshThread.interrupt();
 			} catch (final Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		setIsBeingKilled(true);
 		try {
 			serviceInterface.kill(this.getJobname(), clean);
 			addJobLogMessage("Job kill command sent to backend.");
 		} catch (final Exception e) {
 			e.printStackTrace();
 		}
 
 		final Thread waitThread = new Thread() {
 			@Override
 			public void run() {
 				final StatusObject statusO = new StatusObject(serviceInterface,
 						getJobname());
 				try {
 					statusO.waitForActionToFinish(3, false, true);
 				} catch (final InterruptedException e) {
 					e.printStackTrace();
 				} catch (final StatusException e) {
 					e.printStackTrace();
 				}
 
 				dtoBatchJob = null;
 				setIsBeingKilled(false);
 				isKilled = true;
 				pcs.firePropertyChange("killed", false, true);
 
 				EventBus.publish(new BatchJobKilledEvent(getJobname(),
 						getApplication()));
 				addJobLogMessage("Job killed on backend.");
 
 			}
 		};
 
 		waitThread.start();
 
 		if (waitForCompletion) {
 			try {
 				waitThread.join();
 			} catch (final InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	/**
 	 * Monitors the status of all jobs of this batchjob.
 	 * 
 	 * If you want to restart failed jobs while this is running, provide a
 	 * {@link FailedJobRestarter}. Prints out a lot of verbose information.
 	 * 
 	 * @param sleeptimeinseconds
 	 *            how long between monitor runs
 	 * @param enddate
 	 *            a date that indicates when the monitoring should stop. Use
 	 *            null if you want to monitor until all jobs are finished
 	 * @param forceSuccess
 	 *            forces monitoring until all jobs are finished successful or
 	 *            enddate is reached. Only a valid option if a restarter is
 	 *            provided.
 	 * @param restarter
 	 *            the restarter (or null if you don't want to restart failed
 	 *            jobs while monitoring)
 	 * 
 	 */
 	public void monitorProgress(int sleeptimeinseconds, Date enddate,
 			boolean forceSuccess) {
 		boolean finished = false;
 		do {
 
 			refresh();
 			final String progress = getProgress();
 
 			DtoBatchJob temp;
 			temp = getWrappedDtoBatchJob(false);
 
 			if (forceSuccess) {
 				finished = temp.allJobsFinishedSuccessful();
 			} else {
 				finished = temp.isFinished();
 			}
 
 			if (finished || ((enddate != null) && new Date().after(enddate))) {
 				break;
 			}
 
 			System.out.println(progress);
 
 			for (final Date date : getLogMessages(false).keySet()) {
 				System.out.println(date.toString() + ": "
 						+ getLogMessages(false).get(date));
 			}
 
 			EventBus.publish(this.batchJobname, new BatchJobEvent(this,
 					progress));
 
 			try {
 				EventBus.publish(this.batchJobname, new BatchJobEvent(this,
 						"Pausing monitoring for " + sleeptimeinseconds
 						+ " seconds..."));
 				Thread.sleep(sleeptimeinseconds * 1000);
 			} catch (final InterruptedException e) {
 				e.printStackTrace();
 			}
 		} while (!finished);
 	}
 
 	/**
 	 * Calculates the relative path from each of the job directories to the
 	 * common input file directory for this batchjob.
 	 * 
 	 * This can be used to create the commandline for each of the part jobs.
 	 * 
 	 * @return the absolute path
 	 */
 	public String pathToInputFiles() {
 		final DtoBatchJob temp = getWrappedDtoBatchJob(false);
 		if (temp == null) {
 			throw new RuntimeException("Could not access batchjob "
 					+ batchJobname);
 		}
 		return temp.pathToInputFiles();
 	}
 
 	/**
 	 * Prepares all jobs and creates them on the backend.
 	 * 
 	 * Internally, this method first adds all the jobs to the batchjob on the
 	 * backend. Then you can choose to optimize the batchpart job which means
 	 * the backend will recalulate all the submission locations based on the
 	 * number of total jobs and it will try to distribute them according to load
 	 * to all the available sites. After that all the input files are staged in.
 	 * 
 	 * @param optimize
 	 *            whether to optimize the job distribution (true) or use the
 	 *            submission locations you specified (or which are set by
 	 *            default -- false)
 	 * @throws JobsException
 	 *             if one or more jobs can't be created
 	 * @throws BackendException
 	 *             if something fails on the backend
 	 * @throws InterruptedException
 	 */
 	public void prepareAndCreateJobs(boolean optimize) throws JobsException,
 	BackendException, InterruptedException {
 		prepareAndCreateJobs(optimize, true);
 	}
 
 	private void prepareAndCreateJobs(boolean optimize,
 			boolean uploadCommonFiles) throws JobsException, BackendException,
 			InterruptedException {
 
 		// TODO check whether any of the jobnames already exist
 
 		myLogger.debug("Creating " + getNewlyAddedJobs().size()
 				+ " jobs as part of batchjob: " + batchJobname);
 		final String message = "Creating " + getNewlyAddedJobs().size()
 		+ " jobs.";
 		EventBus.publish(this.batchJobname, new BatchJobEvent(this, message));
 		addJobLogMessage(message);
 		final ExecutorService executor = Executors
 		.newFixedThreadPool(getConcurrentJobCreationThreads());
 
 		final Map<JobObject, Exception> failedSubmissions = Collections
 		.synchronizedMap(new HashMap<JobObject, Exception>());
 
 		for (final JobObject job : ImmutableList.copyOf(getNewlyAddedJobs())) {
 
 			if (Thread.interrupted()) {
 				executor.shutdownNow();
 				throw new InterruptedException(
 				"Interrupted while creating jobs...");
 			}
 
 			final Thread createThread = new Thread() {
 				@Override
 				public void run() {
 					boolean success = false;
 					Exception lastException = null;
 					for (int i = 0; i < DEFAULT_JOB_CREATION_RETRIES; i++) {
 						try {
 
 							myLogger.info("Adding job: " + job.getJobname()
 									+ " to batchjob: " + batchJobname);
 
 							String jobname = null;
 							final String message = "Adding job "
 								+ job.getJobname()
 								+ " to batchjob on backend.";
 							EventBus.publish(BatchJobObject.this.batchJobname,
 									new BatchJobEvent(BatchJobObject.this,
 											message));
 							addJobLogMessage(message);
 							jobname = serviceInterface.addJobToBatchJob(
 									batchJobname,
 									job.getJobDescriptionDocumentAsString());
 
 							job.setJobname(jobname);
 							job.updateJobDirectory();
 
 							final String message2 = "Creation of job "
 								+ job.getJobname() + " successful.";
 
 							EventBus.publish(BatchJobObject.this.batchJobname,
 									new BatchJobEvent(BatchJobObject.this,
 											message2));
 							addJobLogMessage(message2);
 
 							success = true;
 							break;
 						} catch (final Exception e) {
 							// e.printStackTrace();
 							final String message = "Creation of job "
 								+ job.getJobname()
 								+ " failed or interrupted.\n\t("
 								+ e.getLocalizedMessage() + ")";
 							EventBus.publish(BatchJobObject.this.batchJobname,
 									new BatchJobEvent(BatchJobObject.this,
 											message));
 							addJobLogMessage(message);
 
 							try {
 								serviceInterface.kill(job.getJobname(), true);
 							} catch (final Exception e1) {
 								// doesn't matter
 							}
 							lastException = e;
 							myLogger.error(job.getJobname() + ": " + e);
 						}
 					}
 					if (!success) {
 						failedSubmissions.put(job, lastException);
 					}
 
 				}
 			};
 			executor.execute(createThread);
 		}
 
 		executor.shutdown();
 
 		try {
 			executor.awaitTermination(7200, TimeUnit.SECONDS);
 		} catch (final InterruptedException e) {
 			myLogger.debug("Preparing and creation of jobs cancelled.");
 			executor.shutdownNow();
 			throw new InterruptedException("Interrupted while creating jobs...");
 		}
 		final String message2 = "Finished creation of "
 			+ getNewlyAddedJobs().size() + " jobs as part of batchjob: "
 			+ batchJobname;
 		myLogger.debug(message2);
 		EventBus.publish(this.batchJobname, new BatchJobEvent(this, message2));
 		addJobLogMessage(message2);
 
 		if (failedSubmissions.size() > 0) {
 			myLogger.error(failedSubmissions.size() + " submission failed...");
 			final String message3 = "Not all jobs for batchjob " + batchJobname
 			+ " created successfully. Aborting...";
 			EventBus.publish(this.batchJobname, new BatchJobEvent(this,
 					message3));
 			addJobLogMessage(message3);
 			throw new JobsException(failedSubmissions);
 		}
 
 		if (Thread.interrupted()) {
 			throw new InterruptedException(
 			"Interrupted after creating all jobs.");
 		}
 
 		refresh(false);
 
 		if (optimize) {
 			try {
 				final String message3 = "Optimizing batchjob: " + batchJobname;
 				EventBus.publish(this.batchJobname, new BatchJobEvent(this,
 						message3));
 				addJobLogMessage(message3);
 				try {
 					final String handle = serviceInterface
 					.redistributeBatchJob(this.batchJobname);
 
 					// System.out.println("Handle: " + handle);
 					final StatusObject status = new StatusObject(
 							serviceInterface, handle);
 
 					try {
 						status.waitForActionToFinish(4, false, true,
 						"Redistribution: ");
 					} catch (final InterruptedException e) {
 						e.printStackTrace();
 						throw e;
 					} catch (final StatusException e) {
 						e.printStackTrace();
 					}
 
 					EventBus.publish(this.batchJobname, new BatchJobEvent(this,
 					"Redistribution finished."));
 					addJobLogMessage("Redistribution finished.");
 
 					optimizationResult = serviceInterface.getJobProperty(
 							this.batchJobname,
 							Constants.BATCHJOB_OPTIMIZATION_RESULT);
 
 					if (Thread.interrupted()) {
 						throw new InterruptedException(
 						"Interrupted after creating all jobs.");
 					}
 
 				} catch (final JobPropertiesException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					throw new RuntimeException(e);
 				}
 
 				for (final JobObject job : getNewlyAddedJobs()) {
 					job.updateJobDirectory();
 				}
 
 				final String message4 = "Optimizing of batchjob "
 					+ batchJobname + " finished.\nJob distribution:\n"
 					+ getOptimizationResult();
 				EventBus.publish(this.batchJobname, new BatchJobEvent(this,
 						message4));
 				addJobLogMessage(message4);
 			} catch (final NoSuchJobException e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 		refresh(false);
 
 		try {
 			uploadInputFiles(uploadCommonFiles);
 		} catch (final InterruptedException e) {
 			throw e;
 		} catch (final Exception e) {
 			final String message3 = "Uploading input files for batchjob: "
 				+ batchJobname + " failed: " + e.getLocalizedMessage();
 			EventBus.publish(this.batchJobname, new BatchJobEvent(this,
 					message3));
 			addJobLogMessage(message3);
 			throw new BackendException("Could not upload input files: "
 					+ e.getLocalizedMessage(), e);
 		}
 		getNewlyAddedJobs().clear();
 	}
 
 	/**
 	 * Refresh all jobs on the backend.
 	 * 
 	 * Waits for the refresh to finish.
 	 */
 	public void refresh() {
 		refresh(true);
 	}
 
 	// /**
 	// * Restarts all jobs that failed using the provided
 	// * {@link FailedJobRestarter}.
 	// *
 	// * @param restarter
 	// * the job restarter that contains the logic how to restart the
 	// * job
 	// */
 	// public void restartFailedJobs(FailedJobRestarter restarter) {
 	//
 	// if (restarter == null) {
 	// restarter = new FailedJobRestarter() {
 	//
 	// public void restartJob(JobObject job)
 	// throws JobSubmissionException {
 	// try {
 	// job.restartJob();
 	// } catch (JobPropertiesException e) {
 	// throw new JobSubmissionException("Can't resubmit job: "
 	// + e.getLocalizedMessage());
 	// }
 	// }
 	// };
 	// }
 	//
 	// for (DtoJob dtoJob : getMultiPartJob(true).getFailedJobs().getAllJobs())
 	// {
 	//
 	// JobObject failedJob = null;
 	// try {
 	// failedJob = new JobObject(serviceInterface, dtoJob.jobname());
 	// EventBus.publish(this.batchJobname, new BatchJobEvent(this,
 	// "Restarting job " + failedJob.getJobname())
 	// + "...");
 	// restarter.restartJob(failedJob);
 	// } catch (Exception e) {
 	// if (failedJob != null) {
 	// EventBus.publish(this.batchJobname, new BatchJobEvent(this,
 	// "Restarting of job " + failedJob.getJobname())
 	// + " failed: " + e.getLocalizedMessage());
 	// } else {
 	// EventBus.publish(this.batchJobname, new BatchJobEvent(this,
 	// "Restarting failed: " + e.getLocalizedMessage()));
 	// }
 	// e.printStackTrace();
 	// }
 	// }
 	//
 	// }
 
 	public void refresh(boolean wait) {
 		getWrappedDtoBatchJob(true, wait);
 	}
 
 	private void refreshBatchJobStatus(boolean waitForRefreshToFinish)
 	throws InterruptedException {
 
 		String handle;
 		try {
 			handle = serviceInterface.refreshBatchJobStatus(batchJobname);
 		} catch (final NoSuchJobException e) {
 
 			this.isRefreshing = false;
 			pcs.firePropertyChange(REFRESHING, true, false);
 			myLogger.error(e);
 			return;
 		}
 
 		if (waitForRefreshToFinish) {
 
 			if (Thread.interrupted()) {
 				this.isRefreshing = false;
 				pcs.firePropertyChange(REFRESHING, true, false);
 				Thread.currentThread().interrupt();
 				throw new InterruptedException("Batch job refresh interrupted.");
 			}
 			this.isRefreshing = true;
 			pcs.firePropertyChange(REFRESHING, false, true);
 			DtoActionStatus status = serviceInterface.getActionStatus(handle);
 
 			if (status == null) {
 				this.isRefreshing = false;
 				pcs.firePropertyChange(REFRESHING, true, false);
 				myLogger.error("Can't get status for handle: " + handle);
 				return;
 			}
 
 			while (!status.isFinished()) {
 
 				try {
 					Thread.sleep(ClientPropertiesManager
 							.getJobSubmitStatusRecheckIntervall() * 1000);
 				} catch (final InterruptedException e) {
 					this.isRefreshing = false;
 					pcs.firePropertyChange(REFRESHING, true, false);
 					throw e;
 				}
 
 				status = serviceInterface.getActionStatus(handle);
 
 				if (status == null) {
 					this.isRefreshing = false;
 					pcs.firePropertyChange(REFRESHING, true, false);
 					myLogger.error("Can't get status for handle: " + handle);
 					return;
 				}
 
 			}
 			this.isRefreshing = false;
 			pcs.firePropertyChange(REFRESHING, true, false);
 		}
 
 	}
 
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		this.pcs.removePropertyChangeListener(listener);
 	}
 
 	/**
 	 * Wrapper method for the 2 most commonly used restart methods: Restarting
 	 * failed jobs and restarting jobs that are still in the queue (on locations
 	 * where no job is started yet).
 	 * 
 	 * @param restartFailedJobs
 	 *            restart jobs that failed (those jobs are moved to locations
 	 *            where jobs have successfully finished -- if possible)
 	 * @param resubmitWaitingJobs
 	 *            restart jobs that sit in queues at locations where no jobs
 	 *            have started yet. Those jobs are moved to locations where jobs
 	 *            are already running or have finished successfully.
 	 * @param startUnsubmittedJobs
 	 *            whether to start jobs that are newly added (not submitted at
 	 *            all yet)
 	 * @param waitForRestartToFinish
 	 *            whether to wait for the whole batchjob-restart to finish
 	 * @return whether the restart was successful or not (i.e. an error occured)
 	 * @throws InterruptedException
 	 *             if the creaton of newly added jobs is interrupted
 	 * @throws BackendException
 	 *             if something fails on the backend when creating newly adding
 	 *             jobs
 	 * @throws JobsException
 	 *             if a newly added job can't be created on the backend
 	 */
 	public boolean restart(boolean restartFailedJobs,
 			boolean resubmitWaitingJobs, boolean startUnsubmittedJobs,
 			boolean waitForRestartToFinish) throws JobsException,
 			BackendException, InterruptedException {
 
 		final DefaultResubmitPolicy policy = new DefaultResubmitPolicy();
 
 		policy.setProperty(DefaultResubmitPolicy.RESTART_FAILED_JOBS,
 				restartFailedJobs);
 		policy.setProperty(DefaultResubmitPolicy.RESTART_WAITING_JOBS,
 				resubmitWaitingJobs);
 		policy.setProperty(DefaultResubmitPolicy.START_NEWLY_READY_JOBS,
 				startUnsubmittedJobs);
 
 		return restart(policy, waitForRestartToFinish);
 
 	}
 
 	/**
 	 * Restarts the batchjob using the specified ResubmitPolicy
 	 * 
 	 * @param policy
 	 * @param waitForRestartToFinish
 	 * @return
 	 * @throws InterruptedException
 	 *             if the creaton of newly added jobs is interrupted
 	 * @throws BackendException
 	 *             if something fails on the backend when creating newly adding
 	 *             jobs
 	 * @throws JobsException
 	 *             if a newly added job can't be created on the backend
 	 */
 	public boolean restart(ResubmitPolicy policy,
 			final boolean waitForRestartToFinish) throws JobsException,
 			BackendException, InterruptedException {
 
 		setResubmitting(true);
 
 		prepareAndCreateJobs(false, false);
 
 		try {
 			serviceInterface.restartBatchJob(batchJobname, policy.getName(),
 					policy.getProperties()).propertiesAsMap();
 		} catch (final NoSuchJobException e) {
 			setResubmitting(false);
 			return false;
 		} catch (final JobPropertiesException e) {
 			setResubmitting(false);
 			return false;
 		}
 
 		final Thread waitThread = new Thread() {
 			@Override
 			public void run() {
 
 				final String handle = batchJobname;
 				DtoActionStatus status = serviceInterface
 				.getActionStatus(handle);
 
 				while (!status.isFinished()) {
 
 					try {
 						Thread.sleep(ClientPropertiesManager
 								.getJobSubmitStatusRecheckIntervall() * 1000);
 					} catch (final InterruptedException e) {
 						// doesn't happen
 					}
 					status = serviceInterface.getActionStatus(handle);
 				}
 
 				setResubmitting(false);
 			}
 		};
 
 		waitThread.start();
 
 		// to update all the jobdirectories
 		for (final JobObject job : getJobs()) {
 			job.getAllJobProperties(true);
 		}
 
 		if (waitForRestartToFinish) {
 
 			try {
 				waitThread.join();
 			} catch (final InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 		getWrappedDtoBatchJob(true, false);
 
 		return true;
 	}
 
 	/**
 	 * @param jobnamesToRestart
 	 * @param submissionLocationsToUse
 	 * @param waitForRestartToFinish
 	 * @return
 	 * @throws InterruptedException
 	 *             if the creaton of newly added jobs is interrupted
 	 * @throws BackendException
 	 *             if something fails on the backend when creating newly adding
 	 *             jobs
 	 * @throws JobsException
 	 *             if a newly added job can't be created on the backend
 	 */
 	public boolean restart(Set<String> jobnamesToRestart,
 			Set<String> submissionLocationsToUse, boolean waitForRestartToFinish)
 	throws JobsException, BackendException, InterruptedException {
 
 		final ResubmitPolicy policy = new SpecificJobsResubmitPolicy(
 				jobnamesToRestart, submissionLocationsToUse);
 
 		return restart(policy, waitForRestartToFinish);
 	}
 
 	/**
 	 * Returns all running jobs.
 	 * 
 	 * @return all running jobs
 	 */
 	public SortedSet<DtoJob> runningJobs() {
 		return getWrappedDtoBatchJob(false).runningJobs();
 	}
 
 	/**
 	 * In this method you can specify how many input files are uploaded at the
 	 * same time. This can increase job submission time quite considerably.
 	 * Default is 1.
 	 * 
 	 * @param threads
 	 *            the number of threads
 	 */
 	public void setConcurrentInputFileUploadThreads(int threads) {
 		this.concurrentFileUploadThreads = threads;
 	}
 
 	/**
 	 * In this method you can specify how many jobs should be created at the
 	 * same time.
 	 * 
 	 * Normally you don't need that. But you may experience a bit if you have a
 	 * lot of jobs to create.
 	 * 
 	 * @param threads
 	 *            the number of threads
 	 */
 	public void setConcurrentJobCreationThreads(int threads) {
 		this.concurrentJobCreationThreads = threads;
 	}
 
 	/**
 	 * Sets the default application for this batchjob.
 	 * 
 	 * This is used internally to use mds to calculate the job distribution.
 	 * 
 	 * @param defaultApplication
 	 *            the default application
 	 */
 	private void setDefaultApplication(String defaultApplication) {
 		this.defaultApplication = defaultApplication;
 
 		try {
 			serviceInterface.addJobProperty(this.batchJobname,
 					Constants.APPLICATIONNAME_KEY, defaultApplication);
 		} catch (final NoSuchJobException e) {
 			throw new RuntimeException(e);
 		}
 
 		for (final JobObject job : this.getJobs()) {
 			job.setApplication(defaultApplication);
 		}
 	}
 
 	/**
 	 * In this method you can set the default number of cpus.
 	 * 
 	 * All cpu properties for all jobs that are added at this stage are
 	 * overwritten.
 	 * 
 	 * It is used to calculate job distribution. You can have different numbers
 	 * of cpus for each single job, but the metascheduling might become a tad
 	 * unpredictable/sub-optimal.
 	 * 
 	 * @param defaultNoCpus
 	 *            the default number of cups
 	 */
 	public void setDefaultNoCpus(int defaultNoCpus) {
 		this.defaultNoCpus = defaultNoCpus;
 
 		try {
 			serviceInterface.addJobProperty(this.batchJobname,
 					Constants.NO_CPUS_KEY,
 					new Integer(defaultNoCpus).toString());
 		} catch (final NoSuchJobException e) {
 			throw new RuntimeException(e);
 		}
 
 		for (final JobObject job : this.getJobs()) {
 			job.setCpus(defaultNoCpus);
 		}
 
 	}
 
 	/**
 	 * Sets the default version for this batchjob.
 	 * 
 	 * @param defaultVersion
 	 */
 	private void setDefaultVersion(String defaultVersion) {
 		this.defaultVersion = defaultVersion;
 
 		try {
 			serviceInterface.addJobProperty(this.batchJobname,
 					Constants.APPLICATIONVERSION_KEY, defaultVersion);
 		} catch (final NoSuchJobException e) {
 			throw new RuntimeException(e);
 		}
 
 		for (final JobObject job : this.getJobs()) {
 			job.setApplicationVersion(defaultVersion);
 		}
 
 	}
 
 	/**
 	 * A convenience method to set the same walltime to all jobs.
 	 * 
 	 * @param walltimeInSeconds
 	 *            the walltime.
 	 */
 	public void setDefaultWalltimeInSeconds(int walltimeInSeconds) {
 		this.defaultWalltime = walltimeInSeconds;
 		this.maxWalltimeInSecondsAcrossJobs = walltimeInSeconds;
 
 		try {
 			serviceInterface.addJobProperty(this.batchJobname,
 					Constants.WALLTIME_IN_MINUTES_KEY, new Integer(
 							walltimeInSeconds / 60).toString());
 		} catch (final NoSuchJobException e) {
 			throw new RuntimeException(e);
 		}
 
 		for (final JobObject job : this.getJobs()) {
 			job.setWalltimeInSeconds(walltimeInSeconds);
 		}
 	}
 
 	private void setIsBeingKilled(boolean isBeingKilled) {
 		final boolean old = this.isBeingKilled;
 		this.isBeingKilled = isBeingKilled;
 		pcs.firePropertyChange("isBeingKilled", old, isBeingKilled);
 	}
 
 	/**
 	 * Sets the method to distribute the job.
 	 * 
 	 * Possible methods are: "percentage" (default) and "equal"
 	 * 
 	 * @param method
 	 *            the method
 	 */
 	public void setJobDistributionMethod(String method) {
 
 		try {
 			serviceInterface.addJobProperty(this.batchJobname,
 					Constants.DISTRIBUTION_METHOD, method);
 		} catch (final NoSuchJobException e) {
 			throw new RuntimeException(e);
 		}
 
 	}
 
 	/**
 	 * Sets a filter to only run jobs at sites that don't match.
 	 * 
 	 * You can either set use this method or the
 	 * {@link #setLocationsToInclude(String[])} one. Only the last one set is
 	 * used.
 	 * 
 	 * @param sites
 	 *            a list of simple patterns that specify on which sites to
 	 *            exclude to run jobs
 	 */
 	public void setLocationsToExclude(String[] locationPatterns) {
 		if (locationPatterns == null) {
 			locationPatterns = new String[] {};
 		}
 
 		this.submissionLocationsToExclude = locationPatterns;
 		this.submissionLocationsToInclude = null;
 
 		try {
 			serviceInterface.addJobProperty(this.batchJobname,
 					Constants.LOCATIONS_TO_EXCLUDE_KEY,
 					StringUtils.join(locationPatterns, ","));
 			serviceInterface.addJobProperty(this.batchJobname,
 					Constants.LOCATIONS_TO_INCLUDE_KEY, null);
 		} catch (final NoSuchJobException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Sets a filter to only run jobs at the specified submissionlocations.
 	 * 
 	 * You can either set use this method or the
 	 * {@link #setLocationsToExclude(String[])} one. Only the last one set is
 	 * used.
 	 * 
 	 * @param locationPatterns
 	 *            a list of simple pattern that specify on which submission
 	 *            locations to run jobs
 	 */
 	public void setLocationsToInclude(String[] locationPatterns) {
 
 		if (locationPatterns == null) {
 			locationPatterns = new String[] {};
 		}
 
 		this.submissionLocationsToInclude = locationPatterns;
 		this.submissionLocationsToExclude = null;
 
 		try {
 			serviceInterface.addJobProperty(this.batchJobname,
 					Constants.LOCATIONS_TO_INCLUDE_KEY,
 					StringUtils.join(locationPatterns, ","));
 			serviceInterface.addJobProperty(this.batchJobname,
 					Constants.LOCATIONS_TO_EXCLUDE_KEY, null);
 		} catch (final NoSuchJobException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private void setResubmitting(boolean b) {
 
 		if (isResubmitting == b) {
 			return;
 		}
 
 		isResubmitting = b;
 
 		pcs.firePropertyChange(RESUBMITTING, !b, b);
 
 	}
 
 	public void statusMessage(ActionStatusEvent event) {
 
 		addJobLogMessage(event.toString());
 
 	}
 
 	/**
 	 * Tells the backend to submit this batchjob. Waits for the submission to
 	 * finish.
 	 * 
 	 * 
 	 * @throws JobSubmissionException
 	 *             if the jobsubmission fails
 	 * @throws NoSuchJobException
 	 *             if no such job exists on the backend
 	 * @throws InterruptedException
 	 */
 	public void submit() throws JobSubmissionException, NoSuchJobException,
 	InterruptedException {
 
 		submit(true);
 	}
 
 	/**
 	 * Tells the backend to submit this batchjob.
 	 * 
 	 * @param waitForSubmissionToFinish
 	 *            whether to wait for the submission to finish or not
 	 * 
 	 * @return an {@link DtoActionStatus} object that can be used to monitor the
 	 *         submission progress
 	 * 
 	 * @throws JobSubmissionException
 	 *             if the jobsubmission fails
 	 * @throws NoSuchJobException
 	 *             if no such job exists on the backend
 	 * @throws InterruptedException
 	 */
 	public StatusObject submit(boolean waitForSubmissionToFinish)
 	throws JobSubmissionException, NoSuchJobException,
 	InterruptedException {
 
 		final String message = "Submitting batchjob " + batchJobname
 		+ " to backend...";
 		EventBus.publish(this.batchJobname, new BatchJobEvent(this, message));
 		addJobLogMessage(message);
 
 		isSubmitting = true;
 		pcs.firePropertyChange(SUBMITTING, !isSubmitting, isSubmitting);
 
 		if (Thread.interrupted()) {
 			isSubmitting = false;
 			pcs.firePropertyChange(SUBMITTING, !isSubmitting, isSubmitting);
 			throw new InterruptedException("Interrupted before job submission.");
 		}
 
 		try {
 			serviceInterface.submitJob(batchJobname);
 		} catch (final JobSubmissionException jse) {
 			isSubmitting = false;
 			pcs.firePropertyChange(SUBMITTING, !isSubmitting, isSubmitting);
 			final String message2 = "Job submission for batchjob "
 				+ batchJobname + " failed: " + jse.getLocalizedMessage();
 			EventBus.publish(this.batchJobname, new BatchJobEvent(this,
 					message2));
 			addJobLogMessage(message2);
 			throw jse;
 		} catch (final NoSuchJobException nsje) {
 			isSubmitting = false;
 			pcs.firePropertyChange(SUBMITTING, !isSubmitting, isSubmitting);
 			final String message2 = "Job submitssion for batchjob "
 				+ batchJobname + " failed: " + nsje.getLocalizedMessage();
 			EventBus.publish(this.batchJobname, new BatchJobEvent(this,
 					message2));
 			addJobLogMessage(message2);
 			throw nsje;
 		}
 
 		final StatusObject status = new StatusObject(serviceInterface,
 				BatchJobObject.this.batchJobname);
 
 		final Thread waitThread = new Thread() {
 			@Override
 			public void run() {
 				status.addListener(BatchJobObject.this);
 				try {
 					status.waitForActionToFinish(4, false, true,
 					"Submission status: ");
 				} catch (final InterruptedException e) {
 					e.printStackTrace();
 				} catch (final StatusException e) {
 					e.printStackTrace();
 				} finally {
 					status.removeListener(BatchJobObject.this);
 				}
 			}
 		};
 
 		waitThread.start();
 
 		if (!waitForSubmissionToFinish) {
 			final String message2 = "All jobs of batchjob "
 				+ batchJobname
 				+ " ready for submission. Continuing submission in background...";
 			EventBus.publish(this.batchJobname, new BatchJobEvent(this,
 					message2));
 			addJobLogMessage(message2);
 
 			new Thread() {
 				@Override
 				public void run() {
 					try {
 						waitThread.join();
 						isSubmitting = false;
 						pcs.firePropertyChange(SUBMITTING, !isSubmitting,
 								isSubmitting);
 						EventBus.publish(new NewBatchJobEvent(
 								BatchJobObject.this));
 					} catch (final InterruptedException e) {
 						isSubmitting = false;
 						pcs.firePropertyChange(SUBMITTING, !isSubmitting,
 								isSubmitting);
 
 						// e.printStackTrace();
 					}
 				}
 			}.start();
 
 		} else {
 			try {
 				waitThread.join();
 				isSubmitting = false;
 				pcs.firePropertyChange(SUBMITTING, !isSubmitting, isSubmitting);
 				EventBus.publish(new NewBatchJobEvent(this));
 
 				if (status.getStatus().isFailed()) {
 					throw new JobSubmissionException(
 							"BatchJob submission failed for job "
 							+ getJobname()
 							+ ": "
 							+ status.getStatus().getErrorCause());
 				}
 
 			} catch (final InterruptedException e) {
 				isSubmitting = false;
 				pcs.firePropertyChange(SUBMITTING, !isSubmitting, isSubmitting);
 				waitThread.interrupt();
 			}
 
 		}
 		refresh(false);
 
 		return status;
 
 	}
 
 	/**
 	 * Returns all successful jobs.
 	 * 
 	 * @return all successful jobs
 	 */
 	public SortedSet<DtoJob> successfulJobs() {
 		final DtoBatchJob temp = getWrappedDtoBatchJob(false);
 		if (temp == null) {
 			return new TreeSet<DtoJob>();
 		}
 		return temp.successfulJobs();
 	}
 
 	@Override
 	public String toString() {
 		return getJobname();
 	}
 
 	/**
 	 * Returns all unsubmitted jobs.
 	 * 
 	 * @return all unsubmitted jobs
 	 */
 	public SortedSet<DtoJob> unsubmittedJobs() {
 		final DtoBatchJob temp = getWrappedDtoBatchJob(false);
 		if (temp == null) {
 			return new TreeSet<DtoJob>();
 		}
 		return temp.unsubmittedJobs();
 	}
 
 	private void uploadInputFiles(boolean uploadCommonFiles)
 	throws InterruptedException, FileUploadException {
 
 		myLogger.debug("Uploading batch job input files...");
 
 		final List<Exception> exceptions = Collections
 		.synchronizedList(new LinkedList<Exception>());
 
 		final ExecutorService executor = Executors
 		.newFixedThreadPool(getConcurrentInputFileUploadThreads());
 
 		final List<Future<?>> tasks = new LinkedList<Future<?>>();
 
 		final String messagex = "Starting file staging...";
 		EventBus.publish(getJobname(), new BatchJobEvent(BatchJobObject.this,
 				messagex));
 		addJobLogMessage(messagex);
 
 		// for (final JobObject job : getJobs()) {
 		for (final JobObject job : getNewlyAddedJobs()) {
 
 			checkInterruptedStatus(executor, tasks);
 
 			// uploading single job input files
 			if (job.getInputFiles().size() > 0) {
 				final String message = "Adding input files for job "
 					+ job.getJobname();
 				EventBus.publish(getJobname(), new BatchJobEvent(
 						BatchJobObject.this, message));
 				addJobLogMessage(message);
 				myLogger.debug("Uploading files for job " + job.getJobname());
 
 				final Future<?> f = executor.submit(new Thread() {
 					@Override
 					public void run() {
 						try {
 							try {
 
 								job.stageFiles();
 							} catch (final InterruptedException e) {
 								executor.shutdownNow();
 							}
 						} catch (final FileTransactionException e) {
 							exceptions.add(e);
 							executor.shutdownNow();
 						}
 					}
 				});
 				tasks.add(f);
 			}
 		}
 
 		int i = 0;
 
 		final int all = inputFiles.keySet().size();
 
 		if (all > 0) {
 
 			myLogger.debug("Uploading " + all + " common input files...");
 
 			final String message = "Uploading " + all + " input files ("
 			+ getConcurrentInputFileUploadThreads()
 			+ " concurrent upload threads.)";
 			EventBus.publish(getJobname(), new BatchJobEvent(
 					BatchJobObject.this, message));
 			addJobLogMessage(message);
 
 			if (uploadCommonFiles) {
 				// uploading common job input files
 				for (final String inputFile : inputFiles.keySet()) {
 
 					checkInterruptedStatus(executor, tasks);
 
 					myLogger.debug("Uploading common input file: " + inputFile);
 
 					i = i + 1;
 					final Thread thread = new BatchJobFileUploadThread(
 							serviceInterface, this, inputFile, i, executor,
 							exceptions);
 					final Future<?> f = executor.submit(thread);
 					// so the gridftp servers don't get hit at exactly the same
 					// time, to
 					// distribute the load
 					try {
 						Thread.sleep(500);
 					} catch (final InterruptedException e) {
 						executor.shutdownNow();
 						for (final Future<?> f2 : tasks) {
 							f2.cancel(true);
 						}
 						throw new InterruptedException(
 						"Interrupted while uploading common input files.");
 					}
 				}
 			}
 		}
 
 		executor.shutdown();
 
 		try {
 			myLogger.debug("Waiting for input files to be uploaded...");
 			executor.awaitTermination(10 * 3600, TimeUnit.SECONDS);
 		} catch (final InterruptedException e) {
 			myLogger.debug("Interrupted....");
 			executor.shutdownNow();
 			for (final Future<?> f : tasks) {
 				f.cancel(true);
 			}
 			throw new InterruptedException(
 			"Interrupted while waiting for file uploads to finish.");
 
 		}
 
 		if ((exceptions.size() > 0) && (exceptions.get(0) != null)) {
 			myLogger.debug(exceptions.size()
 					+ " exceptions while uploading. not continuing submission...");
 			for (Exception e : exceptions) {
 				myLogger.error(e);
 				e.printStackTrace();
 			}
 			throw new FileUploadException(exceptions.get(0));
 		}
 
 		final String message2 = "Staging of input files for batchjob: "
 			+ batchJobname + " finished.";
 		EventBus.publish(this.batchJobname, new BatchJobEvent(this, message2));
 		addJobLogMessage(message2);
 
 	}
 
 	/**
 	 * You can use this method to wait for the job to finish (either
 	 * successfully or not) on the endpoint resource.
 	 * 
 	 * This will check the status of all jobs and will return once all of them
 	 * are finished one way or the other. You'll still have to check whether all
 	 * jobs were successful yourself and maybe restart the ones that failed.
 	 * 
 	 * This method is a convenience method and might not be the best way to do
 	 * that. It would be more efficient if you would check for failed jobs with
 	 * every statuscheck and resubmit those failed jobs instantly and not wait
 	 * until everything else is finished (like this method does).
 	 * 
 	 * But it should be good enough for most cases.
 	 * 
 	 * Use the int parameter to specify the sleep interval inbetween status
 	 * checks. Don't use a low number here please (except for testing) because
 	 * it could possibly cause a high load for the backend.
 	 * 
 	 * @param checkIntervallInSeconds
 	 *            the interval inbetween status checks
 	 * @return whether the job is actually finished (true) or the this
 	 *         wait-thread was interrupted otherwise
 	 */
 	public boolean waitForJobToFinish(final int checkIntervallInSeconds) {
 
 		addJobLogMessage("Waiting for job to finish...");
 
 		if (waitThread != null) {
 			if (waitThread.isAlive()) {
 				try {
 					waitThread.join();
 					return isFinished(false);
 				} catch (final InterruptedException e) {
 					myLogger.debug("Job status wait thread interrupted.");
 					return isFinished(false);
 				}
 			}
 		}
 
 		createWaitThread(checkIntervallInSeconds);
 
 		try {
 			waitThread.start();
 			waitThread.join();
 			waitThread = null;
 		} catch (final InterruptedException e) {
 			myLogger.debug("Job status wait thread interrupted.");
 			waitThread = null;
 			return isFinished(false);
 		}
 
 		return isFinished(false);
 
 	}
 
 	/**
 	 * Returns all waiting jobs.
 	 * 
 	 * @return all waiting jobs
 	 */
 	public SortedSet<DtoJob> waitingJobs() {
 		final DtoBatchJob temp = getWrappedDtoBatchJob(false);
 		if (temp == null) {
 			return new TreeSet<DtoJob>();
 		}
 		return temp.waitingJobs();
 	}
 }
