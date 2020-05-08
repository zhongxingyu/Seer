 package grisu.backend.model.job;
 
 import grisu.backend.hibernate.JobDAO;
 import grisu.backend.model.RemoteFileTransferObject;
 import grisu.backend.model.User;
 import grisu.backend.model.fs.GrisuInputStream;
 import grisu.backend.model.fs.GrisuOutputStream;
 import grisu.control.JobConstants;
 import grisu.control.ServiceInterface;
 import grisu.control.exceptions.BatchJobException;
 import grisu.control.exceptions.JobPropertiesException;
 import grisu.control.exceptions.JobSubmissionException;
 import grisu.control.exceptions.NoSuchJobException;
 import grisu.control.exceptions.RemoteFileSystemException;
 import grisu.control.serviceInterfaces.AbstractServiceInterface;
 import grisu.jcommons.constants.Constants;
 import grisu.jcommons.constants.JobSubmissionProperty;
 import grisu.jcommons.interfaces.GridResource;
 import grisu.jcommons.utils.JsdlHelpers;
 import grisu.jcommons.utils.SubmissionLocationHelpers;
 import grisu.model.FileManager;
 import grisu.model.MountPoint;
 import grisu.model.dto.DtoActionStatus;
 import grisu.model.dto.DtoJob;
 import grisu.model.dto.DtoStringList;
 import grisu.model.dto.GridFile;
 import grisu.model.job.JobSubmissionObjectImpl;
 import grisu.model.status.StatusObject;
 import grisu.model.utils.InformationUtils;
 import grisu.settings.ServerPropertiesManager;
 import grisu.utils.ServiceInterfaceUtils;
 import grisu.utils.SeveralXMLHelpers;
 import grith.jgrith.credential.Credential;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import javax.activation.DataHandler;
 import javax.persistence.Transient;
 
 import net.sf.ehcache.util.NamedThreadFactory;
 
 import org.apache.commons.lang.StringUtils;
 import org.globus.exec.utils.ManagedJobFactoryConstants;
 import org.simpleframework.xml.Serializer;
 import org.simpleframework.xml.core.Persister;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.google.common.collect.Sets;
 
 /**
  * The JobSubmissionManager class provides an interface between grisu and the
  * several grid middlewares. It takes a jsdl document as input, converts it into
  * the proper format and then submits the job to the proper endpoint. At the
  * moment only gt4 job submission is supported.
  * 
  * @author Markus Binsteiner
  * 
  */
 public class UserJobManager {
 
 	static final Logger myLogger = LoggerFactory
 			.getLogger(UserJobManager.class.getName());
 
 	private static void setVO(final Job job, String fqan) throws NoSuchJobException,
 	JobPropertiesException {
 
 		if (fqan == null) {
 			fqan = Constants.NON_VO_FQAN;
 		}
 		job.setFqan(fqan);
 		job.addJobProperty(Constants.FQAN_KEY, fqan);
 
 	}
 
 	private Map<String, JobSubmitter> submitters = new HashMap<String, JobSubmitter>();
 
 	private final User user;
 
 	protected final JobDAO jobdao = new JobDAO();
 
 
 	public final static boolean INCLUDE_MULTIPARTJOBS_IN_PS_COMMAND = false;
 
 	/**
 	 * Initializes the JobSubmissionManager with all supported
 	 * {@link JobSubmitter}s.
 	 * 
 	 * @param submitters
 	 *            the supported JobSubmitters
 	 */
 	public UserJobManager(User user,
 			final Map<String, JobSubmitter> submitters) {
 		this.user = user;
 		this.submitters = submitters;
 	}
 
 	public void addJobProperties(final String jobname, final DtoJob properties)
 			throws NoSuchJobException {
 
 		final Job job = getJobFromDatabaseOrFileSystem(jobname);
 
 		final Map<String, String> temp = properties.propertiesAsMap();
 
 		// String urls = temp.get(Constants.INPUT_FILE_URLS_KEY);
 		// if ( StringUtils.isNotBlank(urls) ) {
 		temp.remove(Constants.INPUT_FILE_URLS_KEY);
 		// job.addInputFiles(Arrays.asList(urls.split(",")));
 		// }
 
 		job.addJobProperties(temp);
 		saveOrUpdate(job);
 
 		// myLogger.debug("Added " + properties.getProperties().size()
 		// + " job properties.");
 	}
 
 	public void addJobProperty(final String jobname, final String key,
 			final String value) throws NoSuchJobException {
 
 		try {
 			final Job job = getJobFromDatabaseOrFileSystem(jobname);
 
 			// input files are added automatically
 			if (!Constants.INPUT_FILE_URLS_KEY.equals(key)) {
 				// if ( StringUtils.isBlank(value) ) {
 				// job.removeAllInputFiles();
 				// } else {
 				// job.addInputFiles(Arrays.asList(value.split(",")));
 				// }
 				// } else {
 				job.addJobProperty(key, value);
 				saveOrUpdate(job);
 				// myLogger.debug("Added job property: " + key);
 			}
 		} catch (final NoSuchJobException e) {
 			getUser().getBatchJobManager().addJobProperty(jobname, key, value);
 		}
 
 	}
 
 
 	public synchronized void addLogMessageToPossibleMultiPartJobParent(Job job,
 			String message) {
 
 		final String mpjName = job.getJobProperty(Constants.BATCHJOB_NAME);
 
 		if (mpjName != null) {
 			BatchJob mpj = null;
 			try {
 				mpj = getUser().getBatchJobManager().getBatchJobFromDatabase(
 						mpjName);
 			} catch (final NoSuchJobException e) {
 				myLogger.error(e.getLocalizedMessage(), e);
 				return;
 			}
 			mpj.addLogMessage(message);
 			getUser().getBatchJobManager().saveOrUpdate(mpj);
 		}
 	}
 
 
 
 	public String archiveJob(String jobname, String target)
 			throws JobPropertiesException, NoSuchJobException,
 			RemoteFileSystemException {
 
 		if (getJobFromDatabaseOrFileSystem(jobname).getStatus() < JobConstants.FINISHED_EITHER_WAY) {
 
 			myLogger.debug("not archiving job because job is not finished yet");
 			throw new JobPropertiesException("Job not finished.");
 		}
 
 		if (StringUtils.isBlank(target)) {
 
 			final String defArcLoc = getUser().getDefaultArchiveLocation();
 
 			if (StringUtils.isBlank(defArcLoc)) {
 				throw new RemoteFileSystemException(
 						"Archive location not specified.");
 			} else {
 				target = defArcLoc;
 			}
 		}
 
 		String url = null;
 		// make sure users can specify direct urls or aliases
 		for (final String alias : getUser().getArchiveLocations().keySet()) {
 
 			if (alias.equals(target)) {
 				url = getUser().getArchiveLocations().get(alias);
 				break;
 			}
 			if (target.equals(getUser().getArchiveLocations().get(alias))) {
 				url = target;
 				break;
 			}
 		}
 
 		if (StringUtils.isBlank(url)) {
 			getUser().addArchiveLocation(target, target);
 			url = target;
 		}
 
 		try {
 			return getUser().getBatchJobManager().archiveBatchJob(jobname, url);
 		} catch (final NoSuchJobException e) {
 			final Job job = getJobFromDatabaseOrFileSystem(jobname);
 
 			final String jobdirUrl = job
 					.getJobProperty(Constants.JOBDIRECTORY_KEY);
 			final String targetDir = url + "/"
 					+ FileManager.getFilename(jobdirUrl);
 
 			String tmp = targetDir;
 			int i = 1;
 			while (getUser().getFileManager().fileExists(tmp)) {
 				i = i + 1;
 				tmp = targetDir + "_no_" + i + "_";
 			}
 
 			final Thread archiveThread = archiveSingleJob(job, tmp, null);
 			archiveThread.start();
 
 			return tmp;
 		}
 	}
 
 	public Thread archiveSingleJob(final Job job, final String targetDirUrl,
 			final DtoActionStatus optionalBatchJobStatus) {
 
 		final DtoActionStatus status = new DtoActionStatus(targetDirUrl, 5);
 
 		getUser().getActionStatuses().put(status.getHandle(), status);
 
 		final Thread archiveThread = new Thread() {
 			@Override
 			public void run() {
 
 				if (optionalBatchJobStatus != null) {
 					optionalBatchJobStatus
 					.addElement("Starting archiving of job: "
 							+ job.getJobname());
 				}
 
 				if ((getUser().getActionStatuses().get(job.getJobname()) != null)
 						&& !getUser().getActionStatuses().get(job.getJobname())
 						.isFinished()) {
 
 					if (optionalBatchJobStatus != null) {
 						optionalBatchJobStatus.setFailed(true);
 						optionalBatchJobStatus
 						.setErrorCause("Cancelling archiving of job because it seems to be still submitting.");
 						optionalBatchJobStatus
 						.addElement("Cancelling archiving of job "
 								+ job.getJobname()
 								+ " because it seems to be still submitting.");
 					}
 
 					// this should not really happen
 					myLogger.error("Not archiving job because jobsubmission is still ongoing.");
 					return;
 				}
 
 				status.addElement("Transferring jobdirectory to: "
 						+ targetDirUrl);
 				RemoteFileTransferObject rftp = null;
 				try {
 					rftp = getUser().getFileManager().cpSingleFile(
 							job.getJobProperty(Constants.JOBDIRECTORY_KEY),
 							targetDirUrl, false, true, true);
 					status.addElement("Deleting old jobdirectory: "
 							+ job.getJobProperty(Constants.JOBDIRECTORY_KEY));
 					getUser().getFileManager().deleteFile(
 							job.getJobProperty(Constants.JOBDIRECTORY_KEY),
 							true);
 				} catch (final RemoteFileSystemException e1) {
 					if (optionalBatchJobStatus != null) {
 						optionalBatchJobStatus.setFailed(true);
 						optionalBatchJobStatus.setErrorCause(e1
 								.getLocalizedMessage());
 						optionalBatchJobStatus
 						.addElement("Failed archiving job "
 								+ job.getJobname() + ": "
 								+ e1.getLocalizedMessage());
 					}
 					status.setFailed(true);
 					status.setErrorCause(e1.getLocalizedMessage());
 					status.setFinished(true);
 					status.addElement("Transfer failed: "
 							+ e1.getLocalizedMessage());
 					return;
 				}
 
 				if ((rftp != null) && rftp.isFailed()) {
 					if (optionalBatchJobStatus != null) {
 						optionalBatchJobStatus.setFailed(true);
 						optionalBatchJobStatus.setErrorCause(rftp
 								.getPossibleExceptionMessage());
 						optionalBatchJobStatus
 						.addElement("Failed archiving job "
 								+ job.getJobname());
 					}
 					status.setFailed(true);
 					status.setErrorCause(rftp.getPossibleExceptionMessage());
 					status.setFinished(true);
 					final String message = rftp.getPossibleExceptionMessage();
 					status.addElement("Transfer failed: " + message);
 					return;
 				}
 
 				job.setArchived(true);
 				job.addJobProperty(Constants.JOBDIRECTORY_KEY, targetDirUrl);
 
 				status.addElement("Creating "
 						+ ServiceInterface.GRISU_JOB_FILE_NAME + " file.");
 
 				final String grisuJobFileUrl = targetDirUrl + "/"
 						+ ServiceInterface.GRISU_JOB_FILE_NAME;
 				GrisuOutputStream fout = null;
 
 				try {
 					fout = getUser().getFileManager().getOutputStream(
 							grisuJobFileUrl);
 				} catch (final RemoteFileSystemException e1) {
 					if (optionalBatchJobStatus != null) {
 						optionalBatchJobStatus.setFailed(true);
 						optionalBatchJobStatus.setErrorCause(e1
 								.getLocalizedMessage());
 						optionalBatchJobStatus
 						.addElement("Failed archiving job "
 								+ job.getJobname() + ": "
 								+ e1.getLocalizedMessage());
 					}
 					try {
 						fout.close();
 					} catch (final Exception e) {
 					}
 					status.setFailed(true);
 					status.setErrorCause(e1.getLocalizedMessage());
 					status.setFinished(true);
 					final String message = rftp.getPossibleExceptionMessage();
 					status.addElement("Could not access grisufile url when archiving job: "
 							+ message);
 					return;
 				}
 				final Serializer serializer = new Persister();
 
 				try {
 					serializer.write(job, fout.getStream());
 				} catch (final Exception e) {
 					if (optionalBatchJobStatus != null) {
 						optionalBatchJobStatus.setFailed(true);
 						optionalBatchJobStatus.setErrorCause(e
 								.getLocalizedMessage());
 						optionalBatchJobStatus
 						.addElement("Failed archiving job "
 								+ job.getJobname() + ": "
 								+ e.getLocalizedMessage());
 					}
 					status.setFailed(true);
 					status.setErrorCause(e.getLocalizedMessage());
 					status.setFinished(true);
 					final String message = rftp.getPossibleExceptionMessage();
 					status.addElement("Could not serialize job object.");
 					return;
 				} finally {
 					fout.close();
 				}
 
 				status.addElement("Killing job.");
 				kill(job, true, false);
 
 				// if (optionalBatchJobStatus == null) {
 				// new Thread() {
 				// @Override
 				// public void run() {
 				// Job job = null;
 				// ;
 				// try {
 				// job = loadJobFromFilesystem(grisuJobFileUrl);
 				// DtoJob j = DtoJob.createJob(job.getStatus(),
 				// job.getJobProperties(),
 				// job.getInputFiles(),
 				// job.getLogMessages(), job.isArchived());
 				//
 				// getArchivedJobs(null).addJob(j);
 				// } catch (NoSuchJobException e) {
 				// e.printStackTrace();
 				// }
 				// }
 				// }.start();
 				// }
 
 				status.setFinished(true);
 				status.addElement("Job archived successfully.");
 				if (optionalBatchJobStatus != null) {
 					optionalBatchJobStatus
 					.addElement("Successfully archived job: "
 							+ job.getJobname());
 				}
 
 			}
 		};
 
 		archiveThread.setName("archive job " + job.getJobname() + " / "
 				+ getUser().getDn());
 
 		return archiveThread;
 
 	}
 
 
 
 	private boolean checkWhetherGridResourceIsActuallyAvailable(
 			GridResource resource) {
 
 		final String[] filesystems = user.getInformationManager()
 				.getStagingFileSystemForSubmissionLocation(SubmissionLocationHelpers
 						.createSubmissionLocationString(resource));
 
 		for (final MountPoint mp : getUser().getAllMountPoints()) {
 
 			for (final String fs : filesystems) {
 				if (mp.getRootUrl().startsWith(fs.replace(":2811", ""))) {
 					return true;
 				}
 			}
 
 		}
 
 		return false;
 
 	}
 
 	public String createJob(Document jsdl, final String fqan,
 			final String jobnameCreationMethod,
 			final BatchJob optionalParentBatchJob)
 					throws JobPropertiesException {
 		String jobname = JsdlHelpers.getJobname(jsdl);
 
 		jobname = Jobhelper.calculateJobname(getUser(), jobname,
 				jobnameCreationMethod);
 
 		if (Constants.NO_JOBNAME_INDICATOR_STRING.equals(jobname)) {
 			throw new JobPropertiesException("Jobname can't be "
 					+ Constants.NO_JOBNAME_INDICATOR_STRING);
 		}
 
 		try {
 			final BatchJob mpj = getUser().getBatchJobManager()
 					.getBatchJobFromDatabase(jobname);
 			throw new JobPropertiesException(
 					"Could not create job with jobname " + jobname
 					+ ". Multipart job with this id already exists...");
 		} catch (final NoSuchJobException e) {
 			// that's good
 		}
 
 		Job job;
 		try {
 			// myLogger.debug("Trying to get job that shouldn't exist...");
 			job = getJobFromDatabaseOrFileSystem(jobname);
 			throw new JobPropertiesException(
 					JobSubmissionProperty.JOBNAME.toString() + ": "
 							+ "Jobname \"" + jobname
 							+ "\" already taken. Could not create job.");
 		} catch (final NoSuchJobException e1) {
 			// that's ok
 			// myLogger.debug("Checked jobname. Not yet in database. Good.");
 		}
 
 		// creating job
 		job = new Job(getUser().getDn(), jobname);
 
 		job.setStatus(JobConstants.JOB_CREATED);
 		job.addLogMessage("Job " + jobname + " created.");
 		jobdao.saveOrUpdate(job);
 
 		job.setJobDescription(jsdl);
 
 		try {
 			setVO(job, fqan);
 			processJobDescription(job, optionalParentBatchJob);
 		} catch (final NoSuchJobException e) {
 			// that should never happen
 			myLogger.error("Somehow the job was not created although it certainly should have. Must be a bug..");
 			throw new RuntimeException("Job was not created. Internal error.");
 		} catch (final Exception e) {
 			myLogger.error("Error when processing job description: "
 					+ e.getLocalizedMessage());
 			try {
 				jobdao.delete(job);
 				// myLogger.debug("Deleted job " + jobname
 				// + " from database again.");
 			} catch (final Exception e2) {
 				myLogger.error("Could not delete job from database: "
 						+ e2.getLocalizedMessage());
 			}
 			if (e instanceof JobPropertiesException) {
 				throw (JobPropertiesException) e;
 			} else {
 				throw new RuntimeException(
 						"Unknown error while trying to create job: "
 								+ e.getLocalizedMessage(), e);
 			}
 		}
 
 		job.setStatus(JobConstants.READY_TO_SUBMIT);
 		job.addLogMessage("Job " + jobname + " ready to submit.");
 
 		jobdao.saveOrUpdate(job);
 		return jobname;
 	}
 
 
 
 
 
 
 	@Transient
 	public List<Job> getActiveJobs(String application, boolean refresh) {
 
 		boolean inclBatchJobs = UserJobManager.INCLUDE_MULTIPARTJOBS_IN_PS_COMMAND;
 		if (Constants.ALLJOBS_INCL_BATCH_KEY.equals(application)) {
 			inclBatchJobs = true;
 		}
 
 		try {
 
 			List<Job> jobs = null;
 			if (StringUtils.isBlank(application)
 					|| Constants.ALLJOBS_KEY.equals(application)
 					|| Constants.ALLJOBS_INCL_BATCH_KEY.equals(application)) {
 				jobs = jobdao.findJobByDN(getUser().getDn(), inclBatchJobs);
 			} else {
 				jobs = jobdao.findJobByDNPerApplication(getUser().getDn(),
 						application,
 						inclBatchJobs);
 			}
 
 			if (refresh) {
 				refreshJobStatus(jobs);
 			}
 
 			return jobs;
 
 		} catch (final Exception e) {
 			myLogger.error(e.getLocalizedMessage(), e);
 			throw new RuntimeException(e);
 		}
 
 	}
 
 
 	public DtoStringList getAllJobnames(String application) {
 
 		boolean alljobs = INCLUDE_MULTIPARTJOBS_IN_PS_COMMAND;
 		if (Constants.ALLJOBS_INCL_BATCH_KEY.equals(application)) {
 			alljobs = true;
 		}
 
 		List<String> jobnames = null;
 
 		if (StringUtils.isBlank(application)
 				|| Constants.ALLJOBS_KEY.equals(application)
 				|| Constants.ALLJOBS_INCL_BATCH_KEY.equals(application)) {
 			jobnames = jobdao.findJobNamesByDn(getUser().getDn(), alljobs);
 		} else {
 			jobnames = jobdao.findJobNamesPerApplicationByDn(getUser().getDn(),
 					application, alljobs);
 		}
 
 		return DtoStringList.fromStringList(jobnames);
 	}
 
 	@Transient
 	public List<Job> getArchivedJobs(final String application) {
 
 		try {
 
 			final List<Job> archivedJobs = Collections
 					.synchronizedList(new LinkedList<Job>());
 
 			final int noArchiveLocations = getUser().getArchiveLocations()
 					.size();
 
 			if (noArchiveLocations <= 0) {
 				return archivedJobs;
 			}
 
 			final NamedThreadFactory tf = new NamedThreadFactory(
 					"getArchivedJobs");
 			final ExecutorService executor = Executors.newFixedThreadPool(
 					getUser().getArchiveLocations().size(), tf);
 
 			for (final String archiveLocation : getUser().getArchiveLocations()
 					.values()) {
 
 				final Thread t = new Thread() {
 					@Override
 					public void run() {
 
 						myLogger.debug(getUser().getDn()
 								+ ":\tGetting archived job on: "
 								+ archiveLocation);
 						List<Job> jobObjects = null;
 						try {
 							jobObjects = getArchivedJobsFromFileSystem(archiveLocation);
 							if (StringUtils.isBlank(application)) {
 								for (final Job job : jobObjects) {
 									archivedJobs.add(job);
 								}
 							} else {
 
 								for (final Job job : jobObjects) {
 
 									final String app = job.getJobProperties()
 											.get(Constants.APPLICATIONNAME_KEY);
 
 									if (application.equals(app)) {
 										archivedJobs.add(job);
 									}
 								}
 							}
 						} catch (final RemoteFileSystemException e) {
 							myLogger.error(e.getLocalizedMessage(), e);
 						}
 
 					}
 				};
 				executor.execute(t);
 			}
 
 			executor.shutdown();
 
 			executor.awaitTermination(3, TimeUnit.MINUTES);
 
 			return archivedJobs;
 
 		} catch (final Exception e) {
 			myLogger.error(e.getLocalizedMessage(), e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Transient
 	private List<Job> getArchivedJobsFromFileSystem(String fs)
 			throws RemoteFileSystemException {
 
 		if (StringUtils.isBlank(fs)) {
 			fs = getUser().getDefaultArchiveLocation();
 		}
 
 		if (fs == null) {
 			return new LinkedList<Job>();
 		}
 
 		synchronized (fs) {
 
 			final List<Job> jobs = Collections
 					.synchronizedList(new LinkedList<Job>());
 
 			final GridFile file = getUser().ls(fs, 1);
 
 			final NamedThreadFactory tf = new NamedThreadFactory(
 					"getArchivedJobsFromFS");
 
 			final ExecutorService executor = Executors
 					.newFixedThreadPool(ServerPropertiesManager
 							.getConcurrentArchivedJobLookupsPerFilesystem(), tf);
 
 			for (final GridFile f : file.getChildren()) {
 				final Thread t = new Thread() {
 					@Override
 					public void run() {
 
 						try {
 							final Job job = loadJobFromFilesystem(f.getUrl());
 							jobs.add(job);
 
 						} catch (final NoSuchJobException e) {
 							myLogger.debug("No job for url: " + f.getUrl());
 						}
 					}
 				};
 				executor.execute(t);
 			}
 
 			executor.shutdown();
 
 			try {
 				executor.awaitTermination(2, TimeUnit.MINUTES);
 			} catch (final InterruptedException e) {
 				myLogger.error(e.getLocalizedMessage(), e);
 			}
 
 			return jobs;
 
 		}
 
 	}
 
 
 
 	/**
 	 * Searches for the job with the specified jobname for the current user.
 	 * 
 	 * @param jobname
 	 *            the name of the job (which is unique within one user)
 	 * @return the job
 	 */
 	@Transient
 	public Job getJobFromDatabaseOrFileSystem(String jobnameOrUrl)
 			throws NoSuchJobException {
 
 		Job job = null;
 		try {
 			job = jobdao.findJobByDN(getUser().getDn(), jobnameOrUrl);
 			return job;
 		} catch (final NoSuchJobException nsje) {
 
 			if (jobnameOrUrl.startsWith("gridftp://")) {
 
 				for (final Job archivedJob : getArchivedJobs(null)) {
 					if (job.getJobProperty(Constants.JOBDIRECTORY_KEY).equals(
 							jobnameOrUrl)) {
 						return job;
 					}
 				}
 			}
 
 		}
 		throw new NoSuchJobException("Job with name " + jobnameOrUrl
 				+ "does not exist.");
 	}
 
 	/**
 	 * Monitors the status of a job. Since the {@link JobSubmitter} that was
 	 * used to submit the job is stored in the {@link Job#getSubmissionType()}
 	 * property it does not have to be specified here again.
 	 * 
 	 * @param job
 	 *            the job
 	 * @return the status of the job (have a look at
 	 *         {@link JobConstants#translateStatus(int)} for a human-readable
 	 *         version of the status)
 	 */
 	public final int getJobStatus(final Job job) {
 
 		JobSubmitter submitter = null;
 
 		final boolean disableFinishedJobStatusCaching = ServerPropertiesManager
 				.getDisableFinishedJobStatusCaching();
 
 		if (disableFinishedJobStatusCaching
 				|| (job.getStatus() < JobConstants.FINISHED_EITHER_WAY)) {
 			submitter = submitters.get(job.getSubmissionType());
 
 			if (submitter == null) {
 				throw new NoSuchJobSubmitterException(
 						"Can't find JobSubmitter: " + job.getSubmissionType());
 			}
 		} else {
 			return job.getStatus();
 		}
 
 		return submitter.getJobStatus(job, job.getCredential());
 
 	}
 
 	@Transient
 	public int getJobStatus(final String jobname) {
 
 		// myLogger.debug("Start getting status for job: " + jobname);
 		Job job;
 		try {
 			job = getJobFromDatabaseOrFileSystem(jobname);
 		} catch (final NoSuchJobException e) {
 			return JobConstants.NO_SUCH_JOB;
 		}
 
 		int status = Integer.MIN_VALUE;
 		final int old_status = job.getStatus();
 
 		// System.out.println("OLDSTAUS "+jobname+": "+JobConstants.translateStatus(old_status));
 		if (old_status <= JobConstants.READY_TO_SUBMIT) {
 			// this couldn't have changed without manual intervention
 			return old_status;
 		}
 
 		// check whether the no_such_job check is necessary
 		if (old_status >= JobConstants.FINISHED_EITHER_WAY) {
 			return old_status;
 		}
 
 		final Date lastCheck = job.getLastStatusCheck();
 		final Date now = new Date();
 
 		if ((old_status != JobConstants.EXTERNAL_HANDLE_READY)
 				&& (old_status != JobConstants.UNSUBMITTED)
 				&& (now.getTime() < (lastCheck.getTime() + (ServerPropertiesManager
 						.getWaitTimeBetweenJobStatusChecks() * 1000)))) {
 			myLogger.debug("Last check for job "
 					+ jobname
 					+ " was: "
 					+ lastCheck.toString()
 					+ ". Too early to check job status again. Returning old status...");
 			return job.getStatus();
 		}
 
 		final Credential cred = job.getCredential();
 		boolean changedCred = false;
 		// TODO check whether cred is stored in the database in that case? also,
 		// is a voms credential needed? -- apparently not - only dn must match
 		if ((cred == null) || !cred.isValid()) {
 
 			job.setCredential(getUser().getCredential(job.getFqan()));
 			changedCred = true;
 		}
 
 		// myLogger.debug("Getting status for job from submission manager: "
 		// + jobname);
 
 		status = getJobStatus(job);
 		// myLogger.debug("Status for job" + jobname
 		// + " from submission manager: " + status);
 		if (changedCred) {
 			job.setCredential(null);
 		}
 		if (old_status != status) {
 			job.setStatus(status);
 			final String message = "Job status for job: " + job.getJobname()
 					+ " changed since last check ("
 					+ job.getLastStatusCheck().toString() + ") from: \""
 					+ JobConstants.translateStatus(old_status) + "\" to: \""
 					+ JobConstants.translateStatus(status) + "\"";
 			job.addLogMessage(message);
 			addLogMessageToPossibleMultiPartJobParent(job, message);
 			if ((status >= JobConstants.FINISHED_EITHER_WAY)
 					&& (status != JobConstants.DONE)) {
 				// job.addJobProperty(Constants.ERROR_REASON,
 				// "Job finished with status: "
 				// + JobConstants.translateStatus(status));
 				job.addLogMessage("Job failed. Status: "
 						+ JobConstants.translateStatus(status));
 				final String multiPartJobParent = job
 						.getJobProperty(Constants.BATCHJOB_NAME);
 				if (multiPartJobParent != null) {
 					try {
 						final BatchJob mpj = getUser().getBatchJobManager()
 								.getBatchJobFromDatabase(multiPartJobParent);
 						mpj.addFailedJob(job.getJobname());
 						addLogMessageToPossibleMultiPartJobParent(job, "Job: "
 								+ job.getJobname() + " failed. Status: "
 								+ JobConstants.translateStatus(job.getStatus()));
 						getUser().getBatchJobManager().saveOrUpdate(mpj);
 					} catch (final NoSuchJobException e) {
 						// well
 						myLogger.error(e.getLocalizedMessage(), e);
 					}
 				}
 			}
 		}
 		job.setLastStatusCheck(new Date());
 		jobdao.saveOrUpdate(job);
 
 		// myLogger.debug("Status of job: " + job.getJobname() + " is: " +
 		// status);
 		return status;
 	}
 
 	public Set<String> getUsedApplications() {
 
 		List<Job> jobs = null;
 		jobs = jobdao.findJobByDN(getUser().getDn(), false);
 
 		final Set<String> apps = new TreeSet<String>();
 
 		for (final Job job : jobs) {
 			final String app = job
 					.getJobProperty(Constants.APPLICATIONNAME_KEY);
 			if (StringUtils.isNotBlank(app)) {
 				apps.add(app);
 			}
 		}
 
 		return apps;
 
 	}
 
 
 
 	private User getUser() {
 		return this.user;
 	}
 
 
 
 	/**
 	 * Kills the job with the specified jobname. Before it does that it checks
 	 * the database whether the job may be already finished. In that case it
 	 * doesn't need to contact globus, which is much faster.
 	 * 
 	 * @param jobname
 	 *            the name of the job
 	 * @return the new status of the job
 	 */
 	int kill(final Job job) {
 
 		// Job job;
 		// try {
 		// job = jobdao.findJobByDN(getUser().getDn(), jobname);
 		// } catch (NoSuchJobException e) {
 		// return JobConstants.NO_SUCH_JOB;
 		// }
 
 		job.addLogMessage("Trying to kill job...");
 		int new_status = Integer.MIN_VALUE;
 		final int old_status = job.getStatus();
 
 		// nothing to kill
 		if (old_status > 999) {
 			return old_status;
 		}
 
 		if (old_status > JobConstants.READY_TO_SUBMIT) {
 
 			final Credential cred = job.getCredential();
 			boolean changedCred = false;
 			// TODO check whether cred is stored in the database in that case?
 			if ((cred == null) || !cred.isValid()) {
 				job.setCredential(getUser().getCredential());
 				changedCred = true;
 			}
 
 			new_status = getUser().getJobManager().killJob(job);
 
 			job.addLogMessage("Job killed.");
 			addLogMessageToPossibleMultiPartJobParent(job,
 					"Job: " + job.getJobname() + " killed, new status: ");
 
 			if (changedCred) {
 				job.setCredential(null);
 			}
 
 		} else {
 			job.addLogMessage("Job removed from grisu db.");
 			addLogMessageToPossibleMultiPartJobParent(job,
 					"Job: " + job.getJobname() + "removed from grisu db.");
 			new_status = JobConstants.NO_SUCH_JOB;
 		}
 
 		if (old_status != new_status) {
 			job.setStatus(new_status);
 		}
 		job.addLogMessage("New job status: "
 				+ JobConstants.translateStatus(new_status));
 		addLogMessageToPossibleMultiPartJobParent(
 				job,
 				"Job: " + job.getJobname() + " killed, new status: "
 						+ JobConstants.translateStatus(new_status));
 		jobdao.saveOrUpdate(job);
 		// myLogger.debug("Status of job: " + job.getJobname() + " is: "
 		// + new_status);
 
 		return new_status;
 	}
 
 	public String kill(final Job job, final boolean removeFromDB,
 			final boolean delteJobDirectory) {
 
 		final String handle = "kill_" + job.getJobname() + "_"
 				+ new Date().getTime();
 		int amount = 4;
 		if (!delteJobDirectory) {
 			amount = amount - 1;
 		}
 		if (!removeFromDB) {
 			amount = amount - 1;
 		}
 
 		final DtoActionStatus status = new DtoActionStatus(handle, 2);
 		getUser().getActionStatuses().put(handle, status);
 
 		final Thread t = new Thread() {
 			@Override
 			public void run() {
 
 				try {
 
 					myLogger.debug("Killing job " + job.getJobname()
 							+ "through jobsubmitter...");
 					status.addLogMessage("Killing job through jobmanager...");
 					kill(job);
 					status.addElement("Job killed through jobmanager...");
 					myLogger.debug("Killing job " + job.getJobname()
 							+ "through jobsubmitter finished.");
 
 					if (delteJobDirectory) {
 
 						if (job.isBatchJob()) {
 
 							try {
 								status.addLogMessage("Removing job from parent batchjob.");
 								final BatchJob mpj = getUser()
 										.getBatchJobManager()
 										.getBatchJobFromDatabase(
 												job.getJobProperty(Constants.BATCHJOB_NAME));
 								mpj.removeJob(job);
 								getUser().getBatchJobManager()
 								.saveOrUpdate(mpj);
 							} catch (final Exception e) {
 								// e.printStackTrace();
 								// doesn't matter
 							}
 
 						}
 						status.addLogMessage("Removing job directory.");
 						if (job.getJobProperty(Constants.JOBDIRECTORY_KEY) != null) {
 
 							try {
 								myLogger.debug("Deleting jobdir for "
 										+ job.getJobname());
 
 								getUser()
 								.getFileManager()
 								.deleteFile(
 										job.getJobProperty(Constants.JOBDIRECTORY_KEY),
 										true);
 								myLogger.debug("Deleting success for jobdir for "
 										+ job.getJobname());
 							} catch (final Exception e) {
 								myLogger.error("Could not delete jobdirectory: "
 										+ e.getMessage()
 										+ " Deleting job anyway and don't throw an exception.");
 							}
 						}
 						status.addElement("Job directory deleted.");
 					}
 
 					if (removeFromDB) {
 						status.addLogMessage("Removing job from db...");
 						myLogger.debug("Removing job " + job.getJobname()
 								+ " from db.");
 						jobdao.delete(job);
 						myLogger.debug("Removing job " + job.getJobname()
 								+ " from db finished.");
 						status.addElement("Job removed from db.");
 
 					}
 
 					status.addElement("Job killed.");
 					status.setFinished(true);
 					status.setFailed(false);
 				} catch (final Throwable e) {
 					status.addElement("Failed: " + e.getLocalizedMessage());
 					status.setFailed(true);
 					status.setErrorCause(e.getLocalizedMessage());
 					myLogger.error("Could not kill job: " + job.getJobname());
 				}
 
 			}
 		};
 		t.setName(handle);
 		t.start();
 
 		return handle;
 
 	}
 
 	public String kill(final String jobname, final boolean clear)
 			throws NoSuchJobException, BatchJobException {
 
 		try {
 			Job job;
 
 			if (clear) {
 				try {
 					job = jobdao.findJobByDN(getUser().getDn(), jobname);
 				} catch (final NoSuchJobException nsje) {
 					throw nsje;
 				} catch (final Exception e) {
 					myLogger.debug("Failed killing job: " + e);
 
 					try {
 						// try to delete jobs in case of db corruption
 						final List<Job> jobs = jobdao.findRogueJobsByDN(
 								getUser().getDn(), jobname);
 						String handle = null;
 						for (final Job tmp : jobs) {
 							try {
 								handle = kill(tmp, true, true);
 							} catch (final Exception e3) {
 								myLogger.debug("Can't kill job: "
 										+ e3.getLocalizedMessage());
 							}
 						}
 						return handle;
 
 					} catch (final NoSuchJobException nsje) {
 						// that's ok
 						throw nsje;
 					}
 				}
 
 				final String handle = kill(job, true, true);
 				return handle;
 
 			} else {
 				job = jobdao.findJobByDN(getUser().getDn(), jobname);
 				final String handle = kill(job, false, false);
 				return handle;
 			}
 
 		} catch (final NoSuchJobException nsje) {
 			return getUser().getBatchJobManager().kill(jobname, clear);
 		}
 	}
 
 	/**
 	 * Kills the job. Since the {@link JobSubmitter} that was used to submit the
 	 * job is stored in the {@link Job#getSubmissionType()} property it does not
 	 * have to be specified here again.
 	 * 
 	 * @param job
 	 *            the job to kill
 	 * @return the new status of the job. It may be worth checking whether the
 	 *         job really was killed or not
 	 */
 	public final int killJob(final Job job) {
 
 		synchronized (job.getJobname()) {
 			JobSubmitter submitter = null;
 			submitter = submitters.get(job.getSubmissionType());
 
 			if (submitter == null) {
 				// throw new NoSuchJobSubmitterException(
 				// "Can't find JobSubmitter: " + job.getSubmissionType());
 				myLogger.error("Can't find jobsubitter: " + job.getSubmissionType());
 				return JobConstants.KILLED;
 			}
 
 			return submitter.killJob(job, job.getCredential());
 		}
 	}
 
 	public synchronized String killJobs(final DtoStringList jobnames,
 			final boolean clear) {
 
 		if ((jobnames == null) || (jobnames.asArray().length == 0)) {
 			return null;
 		}
 
 		final Set<String> alljobnames = getAllJobnames(null).asSortedSet();
 		final Set<String> allbatchJobnames = getUser().getBatchJobManager()
 				.getAllBatchJobnames(null).asSortedSet();
 
 		alljobnames.addAll(allbatchJobnames);
 
 		// process globs
 		final Set<String> alljobnamestoKill = Sets.newTreeSet();
 		for (String name : jobnames.asArray()) {
 
 			// ServiceInterfaceUtils.filterJobNames(, name);
 
 			List<String> matches = ServiceInterfaceUtils.filterJobNames(
 					alljobnames, name);
 
 			alljobnamestoKill.addAll(matches);
 		}
 
 		final String handle = "kill_" + alljobnamestoKill.size()
 				+ "_jobs_" + new Date().getTime();
 		final DtoActionStatus status = new DtoActionStatus(handle,
 				alljobnamestoKill.size() * 2);
 		getUser().getActionStatuses().put(handle, status);
 
 		final Thread killThread = new Thread() {
 			@Override
 			public void run() {
 
 				final NamedThreadFactory tf = new NamedThreadFactory("killJobs");
 				final ExecutorService executor = Executors
 						.newFixedThreadPool(ServerPropertiesManager
 								.getConcurrentJobsToBeKilled(),
 								tf);
 
 
 				for (final String jobname : alljobnamestoKill) {
 					final Thread t = new Thread() {
 						@Override
 						public void run() {
 
 							status.addElement("Killing job " + jobname + "...");
 							try {
 								final String handle = kill(jobname, clear);
 
 								DtoActionStatus as = getUser()
 										.getActionStatuses().get(handle);
 
 								StatusObject so = new StatusObject(as);
 
 								so.waitForActionToFinish(
 										2,
 										false,
 										ServerPropertiesManager
 										.getJobCleanThresholdInSeconds());
 
 
 								if (as.isFailed()) {
 									status.addElement("Killing of job "
 											+ jobname + " failed.");
 									throw new Exception(as
 											.getErrorCause());
 								} else {
 									status.addElement("Killing of job "
 											+ jobname + " finished");
 								}
 							} catch (final Exception e) {
 								status.addElement("Failed: "
 										+ e.getLocalizedMessage());
 								status.setFailed(true);
 								status.setErrorCause(e.getLocalizedMessage());
 								myLogger.error("Could not kill job: " + jobname);
 							}
 						}
 
 					};
 					t.setName(status.getHandle());
 					executor.execute(t);
 					// // wait a bit in order to distribute filesystem access a
 					// bit
 					// // more...
 					// int waitAbit = new Random().nextInt(2000);
 					// try {
 					// Thread.sleep(new Long(waitAbit));
 					// } catch (InterruptedException e) {
 					// }
 				}
 
 				executor.shutdown();
 				try {
 					executor.awaitTermination(4, TimeUnit.HOURS);
 				} catch (final InterruptedException e) {
 					myLogger.debug(e.getLocalizedMessage(), e);
 					status.setFailed(true);
 				} finally {
 					status.setFinished(true);
 				}
 
 			}
 		};
 
 		killThread.setName(handle);
 		killThread.start();
 
 		return handle;
 	}
 
 	private Job loadJobFromFilesystem(String url) throws NoSuchJobException {
 		String grisuJobPropertiesFile = null;
 
 		if (url.endsWith(ServiceInterface.GRISU_JOB_FILE_NAME)) {
 			grisuJobPropertiesFile = url;
 		} else {
 			if (url.endsWith("/")) {
 				grisuJobPropertiesFile = url
 						+ ServiceInterface.GRISU_JOB_FILE_NAME;
 			} else {
 				grisuJobPropertiesFile = url + "/"
 						+ ServiceInterface.GRISU_JOB_FILE_NAME;
 			}
 
 		}
 
 		Job job = null;
 
 		synchronized (grisuJobPropertiesFile) {
 
 			try {
 
 				if (getUser().getFileManager().fileExists(
 						grisuJobPropertiesFile)) {
 
 					final Object cacheJob = AbstractServiceInterface
 							.getFromSessionCache(grisuJobPropertiesFile);
 
 					if (cacheJob != null) {
 						return (Job) cacheJob;
 					}
 
 					final Serializer serializer = new Persister();
 
 					GrisuInputStream fin = null;
 					try {
 						fin = getUser().getFileManager().getInputStream(
 								grisuJobPropertiesFile);
 						job = serializer.read(Job.class, fin.getStream());
 						fin.close();
 
 						AbstractServiceInterface.putIntoSessionCache(
 								grisuJobPropertiesFile, job);
 
 						return job;
 					} catch (final Exception e) {
 						myLogger.error(e.getLocalizedMessage(), e);
 						throw new NoSuchJobException(
 								"Can't find job at location: " + url);
 					} finally {
 						try {
 							fin.close();
 						} catch (final Exception e) {
 							myLogger.error(e.getLocalizedMessage(), e);
 							throw new NoSuchJobException(
 									"Can't find job at location: " + url);
 						}
 					}
 
 				} else {
 					throw new NoSuchJobException("Can't find job at location: "
 							+ url);
 				}
 			} catch (final RemoteFileSystemException e) {
 				throw new NoSuchJobException("Can't find job at location: "
 						+ url);
 			}
 		}
 	}
 
 
 
 	/**
 	 * Prepares the environment for the job. Mainly it creates the job directory
 	 * remotely.
 	 * 
 	 * @param job
 	 *            the name of the job
 	 * @throws RemoteFileSystemException
 	 *             if the job directory couldn't be created
 	 */
 	protected void prepareJobEnvironment(final Job job)
 			throws RemoteFileSystemException {
 
 		final String debug_token = "SUBMIT_" + job.getJobname() + ": ";
 		try {
 
 			myLogger.debug(debug_token
 					+ "Getting absolute workingdirectoryurl...");
 			final String jobDir = JsdlHelpers
 					.getAbsoluteWorkingDirectoryUrl(job.getJobDescription());
 			myLogger.debug(debug_token + "Found absolute workingdirectoryurl: "
 					+ jobDir);
 			// myLogger.debug("Using calculated jobdirectory: " + jobDir);
 
 			// job.setJob_directory(jobDir);
 			myLogger.debug(debug_token + "Creating jobdir...");
 			getUser().getFileManager().createFolder(jobDir);
 			myLogger.debug(debug_token + "Jobdir created.");
 		} catch (final Throwable e) {
 			myLogger.error(e.getLocalizedMessage(), e);
 			myLogger.debug(debug_token + "Error creating jobdir: "
 					+ e.getLocalizedMessage());
 			throw new RemoteFileSystemException(
 					"Could not prepare job environment: "
 							+ e.getLocalizedMessage());
 		}
 
 		// now after the jsdl is ready, don't forget to fill the required fields
 		// into the database
 	}
 
 	/**
 	 * This method tries to auto-fill in missing values like which
 	 * submissionlocation to submit to, which version to use (if not specified)
 	 * and so on.
 	 * 
 	 * @param jobname
 	 * @throws NoSuchJobException
 	 * @throws JobPropertiesException
 	 */
 	void processJobDescription(final Job job, final BatchJob parentJob)
 			throws NoSuchJobException, JobPropertiesException {
 
 		// TODO check whether fqan is set
 		final String jobFqan = job.getFqan();
 		final Document jsdl = job.getJobDescription();
 
 		String oldJobDir = job.getJobProperty(Constants.JOBDIRECTORY_KEY);
 
 		try {
 			if (StringUtils.isNotBlank(oldJobDir)) {
 
 				if (getUser().getFileManager().fileExists(oldJobDir)) {
 
 					final GridFile fol = getUser().ls(oldJobDir, 1);
 					if (fol.getChildren().size() > 0) {
 
 						// myLogger.debug("Old jobdir exists.");
 					} else {
 						oldJobDir = null;
 					}
 				} else {
 					oldJobDir = null;
 				}
 			} else {
 				oldJobDir = null;
 			}
 		} catch (final RemoteFileSystemException e1) {
 			oldJobDir = null;
 		}
 
 		boolean applicationCalculated = false;
 
 		final JobSubmissionObjectImpl jobSubmissionObject = new JobSubmissionObjectImpl(
 				jsdl);
 
 		if (jobSubmissionObject.getCommandline() == null) {
 			throw new JobPropertiesException("No commandline specified.");
 		}
 
 		for (final JobSubmissionProperty key : jobSubmissionObject
 				.getJobSubmissionPropertyMap().keySet()) {
 			job.addJobProperty(key.toString(), jobSubmissionObject
 					.getJobSubmissionPropertyMap().get(key));
 		}
 
 		final String executable = jobSubmissionObject.extractExecutable();
 		job.addJobProperty(Constants.EXECUTABLE_KEY, executable);
 
 		List<GridResource> matchingResources = null;
 
 		String submissionLocation = null;
 		String[] stagingFileSystems = null;
 
 		// check whether application is "generic". If that is the case, just
 		// check
 		// if all the necessary fields are specified and then continue without
 		// any
 		// auto-settings
 
 		if (jobSubmissionObject.getApplication() == null) {
 
 			final String commandline = jobSubmissionObject.getCommandline();
 
 			final String[] apps = user.getInformationManager()
 					.getApplicationsThatProvideExecutable(jobSubmissionObject
 							.extractExecutable());
 
 			if ((apps == null) || (apps.length == 0)) {
 				jobSubmissionObject
 				.setApplication(Constants.GENERIC_APPLICATION_NAME);
 			} else if (apps.length > 1) {
 				throw new JobPropertiesException(
 						"More than one application names for executable "
 								+ jobSubmissionObject.extractExecutable()
 								+ " found.");
 			} else {
 				jobSubmissionObject.setApplication(apps[0]);
 			}
 
 		}
 
 		// System.out.println("Subloc in si: "
 		// + jobSubmissionObject.getSubmissionLocation());
 
 		// if "generic" app, submission location needs to be specified.
 		if (Constants.GENERIC_APPLICATION_NAME.equals(jobSubmissionObject
 				.getApplication())
 				&& (StringUtils.isNotBlank(jobSubmissionObject
 						.getSubmissionLocation()) && !(Constants.NO_SUBMISSION_LOCATION_INDICATOR_STRING
 								.equals(jobSubmissionObject.getSubmissionLocation())))) {
 
 			submissionLocation = jobSubmissionObject.getSubmissionLocation();
 
 			stagingFileSystems = user.getInformationManager()
 					.getStagingFileSystemForSubmissionLocation(submissionLocation);
 
 			if ((stagingFileSystems == null)
 					|| (stagingFileSystems.length == 0)) {
 				myLogger.error("No staging filesystem found for submissionlocation: "
 						+ submissionLocation);
 				throw new JobPropertiesException(
 						JobSubmissionProperty.SUBMISSIONLOCATION.toString()
 						+ ": "
 						+ "Could not find staging filesystem for submissionlocation "
 						+ submissionLocation);
 			}
 
 			// if not "generic" application...
 		} else {
 			// ...either try to find a suitable one...
 			if (StringUtils.isBlank(jobSubmissionObject.getApplication())
 					|| (StringUtils.equals(Constants.GENERIC_APPLICATION_NAME,
 							jobSubmissionObject.getApplication()) && StringUtils
 							.isBlank(jobSubmissionObject
 									.getSubmissionLocation()))
 
 					) {
 				myLogger.debug("No application specified. Trying to calculate it...");
 
 				final String[] calculatedApps = user
 						.getInformationManager()
 						.getApplicationsThatProvideExecutable(JsdlHelpers
 								.getPosixApplicationExecutable(jsdl));
 				for (final String app : calculatedApps) {
 					jobSubmissionObject.setApplication(app);
 					matchingResources = user.getMatchMaker().findAllResources(
 							jobSubmissionObject.getJobSubmissionPropertyMap(),
 							job.getFqan());
 					removeResourcesWithUnaccessableFilesystems(matchingResources);
 					if ((matchingResources != null)
 							&& (matchingResources.size() > 0)) {
 						JsdlHelpers.setApplicationName(jsdl, app);
 						myLogger.debug("Calculated app: " + app);
 						break;
 					}
 				}
 
 				if ((jobSubmissionObject.getApplication() == null)
 						|| (jobSubmissionObject.getApplication().length() == 0)) {
 
 					final String version = jobSubmissionObject
 							.getApplicationVersion();
 					if (StringUtils.isNotBlank(version)
 							&& !Constants.NO_VERSION_INDICATOR_STRING
 							.equals(version)) {
 						throw new JobPropertiesException(
 								JobSubmissionProperty.APPLICATIONNAME
 								.toString()
 								+ ": "
 								+ "No application specified (but application version) and could not find one in the grid that matches the executable "
 								+ JsdlHelpers
 								.getPosixApplicationExecutable(jsdl)
 								+ ".");
 					} else {
 						jobSubmissionObject
 						.setApplication(Constants.GENERIC_APPLICATION_NAME);
 					}
 				}
 
 				applicationCalculated = true;
 				JsdlHelpers.setApplicationName(jsdl,
 						jobSubmissionObject.getApplication());
 				job.addJobProperty(Constants.APPLICATIONNAME_KEY,
 						jobSubmissionObject.getApplication());
 				job.addJobProperty(Constants.APPLICATIONNAME_CALCULATED_KEY,
 						"true");
 				// ... or use the one specified.
 			} else {
 
 				myLogger.debug("Trying to find matching grid resources...");
 				matchingResources = user.getMatchMaker().findAllResources(
 						jobSubmissionObject.getJobSubmissionPropertyMap(),
 						job.getFqan());
 				removeResourcesWithUnaccessableFilesystems(matchingResources);
 				if (matchingResources != null) {
 					myLogger.debug("Found: " + matchingResources.size()
 							+ " of them: "
 							+ StringUtils.join(matchingResources, " / "));
 				}
 			}
 
 			submissionLocation = jobSubmissionObject.getSubmissionLocation();
 			// GridResource selectedSubmissionResource = null;
 
 			if (StringUtils.isNotBlank(submissionLocation)
 					&& !Constants.NO_SUBMISSION_LOCATION_INDICATOR_STRING
 					.equals(submissionLocation)) {
 				myLogger.debug("Submission location specified in jsdl: "
 						+ submissionLocation
 						+ ". Checking whether this is valid using mds information.");
 
 				stagingFileSystems = user.getInformationManager()
 						.getStagingFileSystemForSubmissionLocation(submissionLocation);
 				if ((stagingFileSystems == null)
 						|| (stagingFileSystems.length == 0)) {
 					myLogger.error("No staging filesystem found for submissionlocation: "
 							+ submissionLocation);
 					throw new JobPropertiesException(
 							JobSubmissionProperty.SUBMISSIONLOCATION.toString()
 							+ ": "
 							+ "Could not find staging filesystem for submissionlocation "
 							+ submissionLocation + " (using VO: "
 							+ jobFqan + ")");
 				}
 
 				boolean submissionLocationIsValid = false;
 
 				if (Constants.GENERIC_APPLICATION_NAME
 						.equals(jobSubmissionObject.getApplication())) {
 					// let's just assume, shall we? No other option...
 					submissionLocationIsValid = true;
 				} else {
 
 					// check whether submission location is specified. If so,
 					// check
 					// whether it is in the list of matching resources
 					for (final GridResource resource : matchingResources) {
 						if (submissionLocation.equals(SubmissionLocationHelpers
 								.createSubmissionLocationString(resource))) {
 							myLogger.debug("Found gridResource object for submission location. Now checking whether version is specified and if it is whether it is available on this resource.");
 							// now check whether a possible selected version is
 							// available on this resource
 							if (StringUtils.isNotBlank(jobSubmissionObject
 									.getApplicationVersion())
 									&& !Constants.NO_VERSION_INDICATOR_STRING
 									.equals(jobSubmissionObject
 											.getApplicationVersion())
 											&& !resource
 											.getAvailableApplicationVersion()
 											.contains(
 													jobSubmissionObject
 													.getApplicationVersion())) {
 								myLogger.debug("Specified version is not available on this grid resource: "
 										+ submissionLocation);
 								throw new JobPropertiesException(
 										JobSubmissionProperty.APPLICATIONVERSION
 										.toString()
 										+ ": "
 										+ "Version: "
 										+ jobSubmissionObject
 										.getApplicationVersion()
 										+ " not installed on "
 										+ submissionLocation
 										+ " (using VO: "
 										+ jobFqan
 										+ ")");
 							}
 							myLogger.debug("Version available or not specified.");
 							// if no application version is specified, auto-set
 							// one
 							if (StringUtils.isBlank(jobSubmissionObject
 									.getApplicationVersion())
 									|| Constants.NO_VERSION_INDICATOR_STRING
 									.equals(jobSubmissionObject
 											.getApplicationVersion())) {
 								myLogger.debug("version was not specified. Auto setting the first one for the selected resource.");
 								if ((resource.getAvailableApplicationVersion() != null)
 										&& (resource
 												.getAvailableApplicationVersion()
 												.size() > 0)) {
 									final List<String> versionsAvail = resource
 											.getAvailableApplicationVersion();
 
 									String latest = null;
 									try {
 										latest = InformationUtils
 												.guessLatestVersion(versionsAvail);
 									} catch (final Exception e) {
 										myLogger.debug("Could not guess latest version: "
 												+ e.getLocalizedMessage());
 										// using random version
 									}
 
 									if (StringUtils.isNotBlank(latest)) {
 										JsdlHelpers.setApplicationVersion(jsdl,
 												latest);
 									} else {
 										JsdlHelpers.setApplicationVersion(jsdl,
 												versionsAvail.get(0));
 									}
 
 									job.addJobProperty(
 											Constants.APPLICATIONVERSION_KEY,
 											versionsAvail.get(0));
 									job.addJobProperty(
 											Constants.APPLICATIONVERSION_CALCULATED_KEY,
 											"true");
 									myLogger.debug("Set version to be: "
 											+ resource
 											.getAvailableApplicationVersion()
 											.get(0));
 									// jobSubmissionObject.setApplicationVersion(resource.getAvailableApplicationVersion().get(0));
 								} else {
 									throw new JobPropertiesException(
 											JobSubmissionProperty.APPLICATIONVERSION
 											.toString()
 											+ ": "
 											+ "Could not find any installed version for application "
 											+ jobSubmissionObject
 											.getApplication()
 											+ " on "
 											+ submissionLocation
 											+ " (using VO: "
 											+ jobFqan
 											+ ")");
 								}
 							}
 							myLogger.debug("Successfully validated submissionlocation "
 									+ submissionLocation);
 							submissionLocationIsValid = true;
 							// selectedSubmissionResource = resource;
 							break;
 						}
 					}
 				}
 
 				if (!submissionLocationIsValid) {
 					myLogger.error("Could not find a matching grid resource object for submissionlocation: "
 							+ submissionLocation);
 					throw new JobPropertiesException(
 							JobSubmissionProperty.SUBMISSIONLOCATION.toString()
 							+ ": "
 							+ "Submissionlocation "
 							+ submissionLocation
 							+ " not available for this kind of job (using VO: "
 							+ jobFqan + ")");
 				}
 			} else {
 				myLogger.debug("No submission location specified in jsdl document. Trying to auto-find one...");
 				if ((matchingResources == null)
 						|| (matchingResources.size() == 0)) {
 					myLogger.error("No matching grid resources found.");
 					throw new JobPropertiesException(
 							JobSubmissionProperty.SUBMISSIONLOCATION.toString()
 							+ ": "
 							+ "Could not find any matching resource to run this kind of job on. Using VO: "
 							+ jobFqan);
 				}
 				// find the best submissionlocation and set it.
 
 				// check for the version of the application to run
 				if (StringUtils.isBlank(jobSubmissionObject
 						.getApplicationVersion())
 						|| Constants.NO_VERSION_INDICATOR_STRING
 						.equals(jobSubmissionObject
 								.getApplicationVersion())) {
 					myLogger.debug("No version specified in jsdl document. Will use the first one for the best grid resource.");
 					for (final GridResource resource : matchingResources) {
 
 						final String temp = SubmissionLocationHelpers
 								.createSubmissionLocationString(resource);
 						stagingFileSystems = user
 								.getInformationManager()
 								.getStagingFileSystemForSubmissionLocation(temp);
 						if ((stagingFileSystems == null)
 								|| (stagingFileSystems.length == 0)) {
 							myLogger.debug("SubLoc: "
 									+ temp
 									+ " has no staging file system. Trying next one.");
 							continue;
 						}
 
 						if ((resource.getAvailableApplicationVersion() != null)
 								&& (resource.getAvailableApplicationVersion()
 										.size() > 0)) {
 							JsdlHelpers.setApplicationVersion(jsdl, resource
 									.getAvailableApplicationVersion().get(0));
 							job.addJobProperty(
 									Constants.APPLICATIONVERSION_KEY, resource
 									.getAvailableApplicationVersion()
 									.get(0));
 							job.addJobProperty(
 									Constants.APPLICATIONVERSION_CALCULATED_KEY,
 									"true");
 
 							// jobSubmissionObject.setApplicationVersion(resource.getAvailableApplicationVersion().get(0));
 							submissionLocation = SubmissionLocationHelpers
 									.createSubmissionLocationString(resource);
 							myLogger.debug("Using submissionlocation: "
 									+ submissionLocation
 									+ " and application version: "
 									+ resource.getAvailableApplicationVersion()
 									.get(0));
 							break;
 						}
 					}
 					if (submissionLocation == null) {
 						myLogger.error("Could not find any version of the specified application grid-wide.");
 						throw new JobPropertiesException(
 								JobSubmissionProperty.APPLICATIONVERSION
 								.toString()
 								+ ": "
 								+ "Could not find any version for this application grid-wide. That is probably an error in the mds info (VO used: "
 								+ jobFqan + ".");
 					}
 				} else {
 					myLogger.debug("Version: "
 							+ jobSubmissionObject.getApplicationVersion()
 							+ " specified. Trying to find a matching grid resource...");
 					for (final GridResource resource : matchingResources) {
 
 						final String temp = SubmissionLocationHelpers
 								.createSubmissionLocationString(resource);
 						stagingFileSystems = user
 								.getInformationManager()
 								.getStagingFileSystemForSubmissionLocation(temp);
 						if ((stagingFileSystems == null)
 								|| (stagingFileSystems.length == 0)) {
 							myLogger.debug("SubLoc: "
 									+ temp
 									+ " has no staging file system. Trying next one.");
 							continue;
 						}
 
 						if (resource.getAvailableApplicationVersion().contains(
 								jobSubmissionObject.getApplicationVersion())) {
 							submissionLocation = SubmissionLocationHelpers
 									.createSubmissionLocationString(resource);
 							myLogger.debug("Found grid resource with specified application version. Using submissionLocation: "
 									+ submissionLocation);
 							break;
 						}
 					}
 					if (submissionLocation == null) {
 						myLogger.error("Could not find a grid resource with the specified version...");
 						throw new JobPropertiesException(
 								JobSubmissionProperty.APPLICATIONVERSION
 								.toString()
 								+ ": "
 								+ "Could not find desired version: "
 								+ jobSubmissionObject
 								.getApplicationVersion()
 								+ " for application "
 								+ jobSubmissionObject.getApplication()
 								+ " grid-wide. VO used: " + jobFqan);
 					}
 				}
 
 				// selectedSubmissionResource = matchingResources.get(0);
 				// jobSubmissionObject.setSubmissionLocation(submissionLocation);
 				try {
 					JsdlHelpers.setCandidateHosts(jsdl,
 							new String[] { submissionLocation });
 					job.addJobProperty(
 							Constants.SUBMISSIONLOCATION_CALCULATED_KEY, "true");
 				} catch (final RuntimeException e) {
 					throw new JobPropertiesException(
 							JobSubmissionProperty.SUBMISSIONLOCATION.toString()
 							+ ": "
 							+ "Jsdl document malformed. No candidate hosts element.",
 							e);
 				}
 			}
 		}
 
 		myLogger.debug("Trying to find staging filesystem for subissionlocation: "
 				+ submissionLocation);
 
 		if ((stagingFileSystems == null) || (stagingFileSystems.length == 0)) {
 			myLogger.error("No staging filesystem found for submissionlocation: "
 					+ submissionLocation);
 			throw new JobPropertiesException(
 					JobSubmissionProperty.SUBMISSIONLOCATION.toString()
 					+ ": "
 					+ "Could not find staging filesystem for submissionlocation "
 					+ submissionLocation + " (using VO: " + jobFqan
 					+ ")");
 		}
 
 		myLogger.debug("Trying to find mountpoint for stagingfilesystem...");
 
 		MountPoint mountPointToUse = null;
 		String stagingFilesystemToUse = null;
 		for (final String stagingFs : stagingFileSystems) {
 
 			for (final MountPoint mp : getUser().getAllMountPoints()) {
 				if (mp.getRootUrl().startsWith(stagingFs.replace(":2811", ""))
 						&& jobFqan.equals(mp.getFqan())
 						&& mp.isVolatileFileSystem()) {
 					mountPointToUse = mp;
 					stagingFilesystemToUse = stagingFs.replace(":2811", "");
 					myLogger.debug("Found mountpoint " + mp.getAlias()
 							+ " for stagingfilesystem "
 							+ stagingFilesystemToUse);
 					break;
 				}
 			}
 
 			// in case we didn't find a volatile filesystem, we try again
 			// considering all of them...
 			if (mountPointToUse == null) {
 				for (final MountPoint mp : getUser().getAllMountPoints()) {
 					if (mp.getRootUrl().startsWith(
 							stagingFs.replace(":2811", ""))
 							&& jobFqan.equals(mp.getFqan())) {
 						mountPointToUse = mp;
 						stagingFilesystemToUse = stagingFs.replace(":2811", "");
 						myLogger.debug("Found mountpoint " + mp.getAlias()
 								+ " for stagingfilesystem "
 								+ stagingFilesystemToUse);
 						break;
 					}
 				}
 			}
 
 			if (mountPointToUse != null) {
 				myLogger.debug("Mountpoint set to be: "
 						+ mountPointToUse.getAlias()
 						+ ". Not looking any further...");
 				break;
 			}
 
 		}
 
 		if (mountPointToUse == null) {
 			myLogger.error("Could not find a staging filesystem that is accessible for the user for submissionlocation "
 					+ submissionLocation);
 			throw new JobPropertiesException(
 					JobSubmissionProperty.SUBMISSIONLOCATION.toString()
 					+ ": "
 					+ "Could not find stagingfilesystem for submission location: "
 					+ submissionLocation + " (using VO: " + jobFqan
 					+ ")");
 		}
 
 		JsdlHelpers.addOrRetrieveExistingFileSystemElement(jsdl,
 				JsdlHelpers.LOCAL_EXECUTION_HOST_FILESYSTEM,
 				stagingFilesystemToUse);
 
 		// now calculate and set the proper paths
 		String workingDirectory;
 		if (parentJob == null) {
 			workingDirectory = mountPointToUse.getRootUrl().substring(
 					stagingFilesystemToUse.length())
 					+ "/"
 					+ ServerPropertiesManager.getRunningJobsDirectoryName()
 					+ "/" + job.getJobname();
 		} else {
 			workingDirectory = mountPointToUse.getRootUrl().substring(
 					stagingFilesystemToUse.length())
 					+ "/"
 					+ parentJob
 					.getJobProperty(Constants.RELATIVE_BATCHJOB_DIRECTORY_KEY)
 					+ "/" + job.getJobname();
 		}
 		myLogger.debug("Calculated workingdirectory: " + workingDirectory);
 
 		JsdlHelpers.setWorkingDirectory(jsdl,
 				JsdlHelpers.LOCAL_EXECUTION_HOST_FILESYSTEM, workingDirectory);
 		job.addJobProperty(Constants.MOUNTPOINT_KEY,
 				mountPointToUse.getRootUrl());
 		job.addJobProperty(Constants.STAGING_FILE_SYSTEM_KEY,
 				stagingFilesystemToUse);
 
 		job.addJobProperty(Constants.WORKINGDIRECTORY_KEY, workingDirectory);
 		final String submissionSite = user.getInformationManager()
 				.getSiteForHostOrUrl(SubmissionLocationHelpers
 						.extractHost(submissionLocation));
 		myLogger.debug("Calculated submissionSite: " + submissionSite);
 		job.addJobProperty(Constants.SUBMISSION_SITE_KEY, submissionSite);
 		final String queue = SubmissionLocationHelpers
 				.extractQueue(submissionLocation);
 		job.addJobProperty(Constants.QUEUE_KEY, queue);
 		final String newJobdir = stagingFilesystemToUse + workingDirectory;
 
 		try {
 			getUser().getFileManager().createFolder(newJobdir);
 		} catch (final RemoteFileSystemException e1) {
 			throw new JobPropertiesException(
 					"Could not create new jobdirectory " + newJobdir
 					+ " (using VO: " + jobFqan + "): " + e1);
 		}
 
 		job.addJobProperty(Constants.JOBDIRECTORY_KEY, newJobdir);
 		myLogger.debug("Calculated jobdirectory: " + stagingFilesystemToUse
 				+ workingDirectory);
 
 		job.addJobProperty(Constants.SUBMISSIONBACKEND_KEY,
 				AbstractServiceInterface.getBackendInfo());
 
 		if (StringUtils.isNotBlank(oldJobDir)) {
 			try {
 				// if old jobdir exists, try to move it here
 				getUser().getFileManager().cpSingleFile(oldJobDir,
 						newJobdir, true, true, true);
 
 				getUser().getFileManager().deleteFile(oldJobDir);
 			} catch (final Exception e) {
 				myLogger.error(e.getLocalizedMessage(), e);
 			}
 		}
 
 		myLogger.debug("Fixing urls in datastaging elements...");
 		// fix stage in target filesystems...
 		final List<Element> stageInElements = JsdlHelpers
 				.getStageInElements(jsdl);
 		for (final Element stageInElement : stageInElements) {
 
 			final String filePath = JsdlHelpers
 					.getStageInSource(stageInElement);
 			if ("dummyfile".equals(filePath) || filePath.startsWith("file:")) {
 				continue;
 			}
 			final String filename = filePath.substring(filePath
 					.lastIndexOf("/"));
 
 			final Element el = JsdlHelpers
 					.getStageInTarget_filesystemPart(stageInElement);
 
 			el.setTextContent(JsdlHelpers.LOCAL_EXECUTION_HOST_FILESYSTEM);
 
 			final Element finalNameEl = JsdlHelpers
 					.getStageInTarget_relativePart(stageInElement);
 			final String finalName = finalNameEl.getTextContent();
 
 			if (StringUtils.isBlank(finalName)) {
 				finalNameEl.setTextContent(workingDirectory + filename);
 			} else {
 				if (workingDirectory.endsWith("/") || finalName.startsWith("/")) {
 					finalNameEl.setTextContent(workingDirectory + finalName);
 				} else {
 					finalNameEl.setTextContent(workingDirectory + "/"
 							+ finalName);
 				}
 			}
 
 		}
 
 		job.setJobDescription(jsdl);
 
 		// jobdao.attachDirty(job);
 		myLogger.debug("Preparing job done.");
 	}
 
 
 
 
 	/**
 	 * Just a method to refresh the status of all jobs. Could be used by
 	 * something like a cronjob as well. TODO: maybe change to public?
 	 * 
 	 * @param jobs
 	 *            a list of jobs you want to have refreshed
 	 */
 	protected void refreshJobStatus(final Collection<Job> jobs) {
 		for (final Job job : jobs) {
 			getJobStatus(job.getJobname());
 		}
 	}
 
 
 
 	private void removeResourcesWithUnaccessableFilesystems(
 			List<GridResource> resources) {
 
 		final Iterator<GridResource> i = resources.iterator();
 		while (i.hasNext()) {
 			if (!checkWhetherGridResourceIsActuallyAvailable(i.next())) {
 				i.remove();
 			}
 		}
 
 	}
 
 
 	public void restartJob(final Job job, String changedJsdl)
 			throws JobSubmissionException, NoSuchJobException {
 
 		DtoActionStatus status = null;
 		status = new DtoActionStatus(job.getJobname(), 5);
 		getUser().getActionStatuses().put(job.getJobname(), status);
 
 		job.addLogMessage("Restarting job...");
 		job.addLogMessage("Killing possibly running job...");
 		status.addElement("Killing job...");
 
 		if (job.getStatus() >= JobConstants.UNSUBMITTED) {
 			kill(job);
 		}
 
 		job.setStatus(JobConstants.READY_TO_SUBMIT);
 		status.addElement("Resetting job properties...");
 		// job.getJobProperties().clear();
 
 		final String possibleMultiPartJob = job
 				.getJobProperty(Constants.BATCHJOB_NAME);
 
 		BatchJob mpj = null;
 		if (StringUtils.isNotBlank(possibleMultiPartJob)) {
 			mpj = getUser().getBatchJobManager().getBatchJobFromDatabase(
 					possibleMultiPartJob);
 			addLogMessageToPossibleMultiPartJobParent(job,
 					"Re-submitting job " + job.getJobname());
 			mpj.removeFailedJob(job.getJobname());
 			getUser().getBatchJobManager().saveOrUpdate(mpj);
 		}
 
 		if (StringUtils.isNotBlank(changedJsdl)) {
 			status.addElement("Changing job description...");
 			job.addLogMessage("Changing job properties...");
 			Document newJsdl;
 			final Document oldJsdl = job.getJobDescription();
 
 			try {
 				newJsdl = SeveralXMLHelpers.fromString(changedJsdl);
 			} catch (final Exception e3) {
 
 				myLogger.debug(e3.getLocalizedMessage(), e3);
 				throw new JobSubmissionException("Invalid jsdl/xml format.", e3);
 			}
 
 			// String newAppname = JsdlHelpers.getApplicationName(newJsdl);
 			// JsdlHelpers.setApplicationName(oldJsdl, newAppname);
 			// job.addJobProperty(Constants.APPLICATIONNAME_KEY, newAppname);
 			// String newAppVersion =
 			// JsdlHelpers.getApplicationVersion(newJsdl);
 			// JsdlHelpers.setApplicationVersion(oldJsdl, newAppVersion);
 			// job.addJobProperty(Constants.APPLICATIONVERSION_KEY,
 			// newAppVersion);
 
 			final Integer newTotalCpuTime = JsdlHelpers.getWalltime(newJsdl)
 					* JsdlHelpers.getProcessorCount(newJsdl);
 			job.addLogMessage("Setting totalcputime to: " + newTotalCpuTime);
 			JsdlHelpers.setTotalCPUTimeInSeconds(oldJsdl, newTotalCpuTime);
 			job.addJobProperty(Constants.WALLTIME_IN_MINUTES_KEY, new Integer(
 					JsdlHelpers.getWalltime(newJsdl)).toString());
 
 			final Integer newProcCount = JsdlHelpers.getProcessorCount(newJsdl);
 			job.addLogMessage("Setting processor count to: " + newProcCount);
 			JsdlHelpers.setProcessorCount(oldJsdl, newProcCount);
 			job.addJobProperty(Constants.NO_CPUS_KEY,
 					new Integer(newProcCount).toString());
 
 			// TODO
 			// JsdlHelpers.getTotalMemoryRequirement(newJsdl);
 
 			// JsdlHelpers.getArcsJobType(newJsdl);
 			// JsdlHelpers.getModules(newJsdl);
 			// JsdlHelpers.getPosixApplicationArguments(newJsdl);
 			// JsdlHelpers.getPosixApplicationExecutable(newJsdl);
 			// JsdlHelpers.getPosixStandardError(newJsdl);
 			// JsdlHelpers.getPosixStandardInput(newJsdl);
 			// JsdlHelpers.getPosixStandardOutput(newJsdl);
 
 			final String[] oldSubLocs = JsdlHelpers.getCandidateHosts(oldJsdl);
 			final String oldSubLoc = oldSubLocs[0];
 
 			final String[] newSubLocs = JsdlHelpers.getCandidateHosts(newJsdl);
 			String newSubLoc = null;
 			if ((newSubLocs != null) && (newSubLocs.length >= 1)) {
 				newSubLoc = newSubLocs[0];
 			}
 
 			if ((newSubLoc != null) && !newSubLoc.equals(oldSubLoc)) {
 				// move job
 				JsdlHelpers.setCandidateHosts(oldJsdl, newSubLocs);
 				job.setJobDescription(oldJsdl);
 
 				status.addElement("Moving job from " + oldSubLoc + " to "
 						+ newSubLoc);
 
 				try {
 					processJobDescription(job, mpj);
 				} catch (final JobPropertiesException e) {
 
 					status.addLogMessage("Couldn't process new job description.");
 				}
 			} else {
 				job.setJobDescription(oldJsdl);
 				status.addElement("No need to move job...");
 				// no need to move job
 			}
 
 		} else {
 			status.addElement("Keeping job description...");
 			status.addElement("No need to move job...");
 		}
 
 		myLogger.info("Submitting job: " + job.getJobname() + " for user "
 				+ getUser().getDn());
 		job.addLogMessage("Starting re-submission...");
 		jobdao.saveOrUpdate(job);
 		try {
 			submitJob(job, false, status);
 		} catch (final JobSubmissionException e) {
 			status.addLogMessage("Job submission failed: "
 					+ e.getLocalizedMessage());
 			status.setFailed(true);
 			status.setErrorCause(e.getLocalizedMessage());
 			throw e;
 		}
 
 		status.addElement("Re-submission finished successfully.");
 		status.setFinished(true);
 
 	}
 
 	public void restartJob(final String jobname, String changedJsdl)
 			throws JobSubmissionException, NoSuchJobException {
 
 		if (StringUtils.isBlank(changedJsdl)) {
 			changedJsdl = null;
 		}
 
 		final Job job = getJobFromDatabaseOrFileSystem(jobname);
 
 		restartJob(job, changedJsdl);
 	}
 
 
 
 	public void saveOrUpdate(Job instance) {
 		jobdao.saveOrUpdate(instance);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#stageFiles(java.lang.String)
 	 */
 	public void stageFiles(final Job job, final DtoActionStatus optionalStatus)
 			throws RemoteFileSystemException, NoSuchJobException {
 
 		// Job job;
 		// job = jobdao.findJobByDN(getUser().getDn(), jobname);
 
 		final String debugToken = "SUBMIT_" + job.getJobname() + ": ";
 
 		final List<Element> stageIns = JsdlHelpers.getStageInElements(job
 				.getJobDescription());
 
 		for (final Element stageIn : stageIns) {
 
 			final String sourceUrl = JsdlHelpers.getStageInSource(stageIn);
 
 			myLogger.debug(debugToken + "staging in " + sourceUrl);
 
 			if (optionalStatus != null) {
 				optionalStatus.addElement("Staging file "
 						+ sourceUrl.substring(sourceUrl.lastIndexOf("/") + 1));
 			}
 			// TODO remove that after swing client is fixed.
 			if (sourceUrl.startsWith("file") || sourceUrl.startsWith("dummy")) {
 				continue;
 			}
 			final String targetUrl = JsdlHelpers.getStageInTarget(stageIn);
 
 			if (JobConstants.DUMMY_STAGE_FILE.equals(sourceUrl)
 					|| JobConstants.DUMMY_STAGE_FILE.equals(targetUrl)) {
 				continue;
 			}
 
 			if (StringUtils.isNotBlank(sourceUrl)) {
 
 				myLogger.debug(debugToken + "Start staging of file: "
 						+ sourceUrl + " to: " + targetUrl);
 				job.addInputFile(sourceUrl);
 				jobdao.saveOrUpdate(job);
 				getUser().getFileManager().cpSingleFile(sourceUrl, targetUrl, true, true, true);
 				myLogger.debug(debugToken + "Finished staging of file: "
 						+ sourceUrl + " to: " + targetUrl);
 				// job.addInputFile(targetUrl);
 			}
 			// }
 		}
 	}
 
 	/**
 	 * Submits the job to the specified {@link JobSubmitter}.
 	 * 
 	 * @param submitter_name
 	 *            the JobSubmitter
 	 * @param job
 	 *            the Job
 	 * @return the (JobSubmitter-specific) handle to reconnect to the job later
 	 * @throws ServerJobSubmissionException
 	 *             if the job could not be submitted successful
 	 */
 	public final String submit(final String submitter_name, final Job job)
 			throws ServerJobSubmissionException {
 
 		Document jsdl = null;
 		jsdl = job.getJobDescription();
 		final JobSubmitter submitter = submitters.get(submitter_name);
 
 		if (submitter == null) {
 			throw new NoSuchJobSubmitterException("Can't find JobSubmitter: "
 					+ submitter_name);
 		}
 
 		// String translatedJobDescription =
 		// submitter.convertJobDescription(job);
 
 		String host = JsdlHelpers.getCandidateHosts(jsdl)[0];
 		// TODO change that once I know how to handle queues properly
 
 		// String queue = null;
 		if (host.indexOf(":") != -1) {
 			// queue = host.substring(0, host.indexOf(":"));
 			host = host.substring(host.indexOf(":") + 1);
 		}
 		myLogger.debug("Submission host is: " + host);
 
 		// don't know whether factory type should be in here or in the
 		// GT4Submitter (more likely the latter)
 		String factoryType = null;
 		if (host.indexOf("#") != -1) {
 			factoryType = host.substring(host.indexOf("#") + 1);
 			if ((factoryType == null) || (factoryType.length() == 0)) {
 				factoryType = ManagedJobFactoryConstants.FACTORY_TYPE.PBS;
 			}
 			host = host.substring(0, host.indexOf("#"));
 		} else {
 			factoryType = ManagedJobFactoryConstants.FACTORY_TYPE.PBS;
 		}
 		job.addJobProperty(Constants.FACTORY_TYPE_KEY, factoryType);
 
 		myLogger.debug("FactoryType is: " + factoryType);
 		final String submitHostEndpoint = submitter.getServerEndpoint(host);
 
 		String handle = null;
 		handle = submitter.submit(user.getInformationManager(), submitHostEndpoint,
 				factoryType, job);
 
 		job.setJobhandle(handle);
 		job.addJobProperty(Constants.SUBMISSION_HOST_KEY, host);
 		job.setSubmissionType(submitter_name);
 		job.addJobProperty(Constants.SUBMISSION_TYPE_KEY, submitter_name);
 		// if ((queue != null) && !"".equals(queue)) {
 		// job.addJobProperty(Constants.QUEUE_KEY, queue);
 		// }
 		job.setStatus(JobConstants.EXTERNAL_HANDLE_READY);
 
 		return handle;
 
 	}
 
 
 
 	public void submitJob(final Job job, boolean stageFiles,
 			DtoActionStatus status) throws JobSubmissionException {
 
 		final String debug_token = "SUBMIT_" + job.getJobname() + ": ";
 		try {
 
 			int noStageins = 0;
 
 			if (stageFiles) {
 				final List<Element> stageIns = JsdlHelpers
 						.getStageInElements(job.getJobDescription());
 				noStageins = stageIns.size();
 			}
 
 			status.setTotalElements(status.getTotalElements() + 4 + noStageins);
 
 			// myLogger.debug("Preparing job environment...");
 			job.addLogMessage("Preparing job environment.");
 
 			status.addElement("Preparing job environment...");
 
 			addLogMessageToPossibleMultiPartJobParent(job,
 					"Starting job submission for job: " + job.getJobname());
 			myLogger.debug(debug_token + "preparing job environment...");
 			prepareJobEnvironment(job);
 			myLogger.debug(debug_token + "preparing job environment finished.");
 			if (stageFiles) {
 				myLogger.debug(debug_token + "staging in files started...");
 				status.addLogMessage("Starting file stage-in.");
 				job.addLogMessage("Staging possible input files.");
 				// myLogger.debug("Staging possible input files...");
 				stageFiles(job, status);
 				job.addLogMessage("File staging finished.");
 				status.addLogMessage("File stage-in finished.");
 				myLogger.debug(debug_token + "staging in files finished.");
 			}
 			status.addElement("Job environment prepared...");
 		} catch (final Throwable e) {
 			myLogger.debug(debug_token + "error: " + e.getLocalizedMessage());
 			status.setFailed(true);
 			status.setErrorCause(e.getLocalizedMessage());
 			status.setFinished(true);
 			throw new JobSubmissionException(
 					"Could not access remote filesystem: "
 							+ e.getLocalizedMessage());
 		}
 
 		status.addElement("Setting credential...");
 		myLogger.debug(debug_token + "setting credential started...");
 		if (job.getFqan() != null) {
 			try {
 				job.setCredential(getUser().getCredential(job.getFqan()));
 
 			} catch (final Throwable e) {
 				status.setFailed(true);
 				status.setErrorCause(e.getLocalizedMessage());
 				status.setFinished(true);
 				myLogger.error(e.getLocalizedMessage(), e);
 				throw new JobSubmissionException(
 						"Could not create credential to use to submit the job: "
 								+ e.getLocalizedMessage());
 			}
 		} else {
 			job.addLogMessage("Setting non-vo credential: " + job.getFqan());
 			job.setCredential(getUser().getCredential());
 		}
 		myLogger.debug(debug_token + "setting credential finished.");
 
 		myLogger.debug(debug_token
 				+ "adding job properties as env variables to jsdl..");
 		Document oldJsdl = job.getJobDescription();
 		for (String key : job.getJobProperties().keySet()) {
			key = "GRISU_" + key.toUpperCase();
 			String value = job.getJobProperty(key);
 			if (StringUtils.isNotBlank(value)) {
 				Element e = JsdlHelpers
 						.addOrRetrieveExistingApplicationEnvironmentElement(
 								oldJsdl, key);
 				e.setTextContent(value);
 			}
 		}
 
 		job.setJobDescription(oldJsdl);
 
 		String handle = null;
 		myLogger.debug(debug_token + "submitting job to endpoint...");
 
 		try {
 			status.addElement("Starting job submission using GT4...");
 			job.addLogMessage("Submitting job to endpoint...");
 			final String candidate = JsdlHelpers.getCandidateHosts(job
 					.getJobDescription())[0];
 			final GridResource resource = getUser().getInformationManager()
 					.getGridResource(candidate);
 			String version = resource.getGRAMVersion();
 
 			if (version == null) {
 				// TODO is that good enough?
 				version = "4.0.0";
 			}
 
 			String submissionType = null;
 			if (version.startsWith("5")) {
 				submissionType = "GT5";
 			} else {
 				submissionType = "GT4";
 
 			}
 			try {
 				myLogger.debug(debug_token + "submitting...");
 				handle = getUser().getJobManager().submit(
 						submissionType, job);
 				myLogger.debug(debug_token + "submittission finished...");
 			} catch (final ServerJobSubmissionException e) {
 				myLogger.debug(debug_token + "submittission failed: "
 						+ e.getLocalizedMessage());
 				status.addLogMessage("Job submission failed on server.");
 				status.setFailed(true);
 				status.setFinished(true);
 				status.setErrorCause(e.getLocalizedMessage());
 				job.addLogMessage("Submission to endpoint failed: "
 						+ e.getLocalizedMessage());
 				addLogMessageToPossibleMultiPartJobParent(
 						job,
 						"Job submission for job: " + job.getJobname()
 						+ " failed: " + e.getLocalizedMessage());
 				throw new JobSubmissionException(
 						"Submission to endpoint failed: "
 								+ e.getLocalizedMessage());
 			}
 
 			job.addLogMessage("Submission finished.");
 		} catch (final Throwable e) {
 			myLogger.debug(debug_token + "something failed: "
 					+ e.getLocalizedMessage());
 
 			// e.printStackTrace();
 			status.addLogMessage("Job submission failed.");
 			status.setFailed(true);
 			status.setFinished(true);
 			job.addLogMessage("Submission to endpoint failed: "
 					+ e.getLocalizedMessage());
 			addLogMessageToPossibleMultiPartJobParent(
 					job,
 					"Job submission for job: " + job.getJobname() + " failed: "
 							+ e.getLocalizedMessage());
 			myLogger.error(e.getLocalizedMessage(), e);
 			throw new JobSubmissionException(
 					"Job submission to endpoint failed: "
 							+ e.getLocalizedMessage(), e);
 		}
 
 		if (handle == null) {
 			myLogger.debug(debug_token
 					+ "submission finished but no jobhandle.");
 			status.addLogMessage("Submission finished but no jobhandle...");
 			status.setFailed(true);
 			status.setErrorCause("No jobhandle");
 			status.setFinished(true);
 			job.addLogMessage("Submission finished but jobhandle is null...");
 			addLogMessageToPossibleMultiPartJobParent(
 					job,
 					"Job submission for job: " + job.getJobname()
 					+ " finished but jobhandle is null...");
 			throw new JobSubmissionException(
 					"Job apparently submitted but jobhandle is null for job: "
 							+ job.getJobname());
 		}
 
 		try {
 
 			myLogger.debug(debug_token + "wrapping up started");
 			job.addJobProperty(Constants.SUBMISSION_TIME_KEY,
 					Long.toString(new Date().getTime()));
 
 			// we don't want the credential to be stored with the job in this
 			// case
 			// TODO or do we want it to be stored?
 			job.setCredential(null);
 			job.addLogMessage("Job submission finished successful.");
 
 			addLogMessageToPossibleMultiPartJobParent(
 					job,
 					"Job submission for job: " + job.getJobname()
 					+ " finished successful.");
 			jobdao.saveOrUpdate(job);
 			myLogger.debug(debug_token + "wrapping up finished");
 			myLogger.info("Jobsubmission for job " + job.getJobname()
 					+ " and user " + getUser().getDn() + " successful.");
 
 			status.addElement("Job submission finished...");
 			status.setFinished(true);
 		} catch (final Throwable e) {
 			myLogger.debug(debug_token + "wrapping up failed: "
 					+ e.getLocalizedMessage());
 			status.addLogMessage("Submission finished, error in wrap-up...");
 			status.setFailed(true);
 			status.setFinished(true);
 			status.setErrorCause(e.getLocalizedMessage());
 			job.addLogMessage("Submission finished, error in wrap-up...");
 			addLogMessageToPossibleMultiPartJobParent(
 					job,
 					"Job submission for job: " + job.getJobname()
 					+ " finished but error in wrap-up...");
 			throw new JobSubmissionException(
 					"Job apparently submitted but error in wrap-up for job: "
 							+ job.getJobname());
 		}
 	}
 
 
 	public String submitJob(final String jobname)
 			throws JobSubmissionException, NoSuchJobException {
 
 
 
 		try {
 			final Job job = getJobFromDatabaseOrFileSystem(jobname);
 
 			final String handle = "submision_status_" + jobname + "_"
 					+ new Date().getTime();
 			final DtoActionStatus status = new DtoActionStatus(handle, 0);
 			getUser().getActionStatuses().put(handle, status);
 
 			try {
 
 				if (job.getStatus() > JobConstants.READY_TO_SUBMIT) {
 					throw new JobSubmissionException("Job already submitted.");
 				}
 				final Thread t = new Thread() {
 					@Override
 					public void run() {
 						try {
 							submitJob(job, true, status);
 						} catch (final Throwable e) {
 							status.setFailed(true);
 							status.setFinished(true);
 							status.setErrorCause(e.getLocalizedMessage());
 							myLogger.error(e.getLocalizedMessage(), e);
 						}
 					}
 				};
 				t.setName(status.getHandle());
 				t.start();
 
 				return handle;
 
 			} catch (Throwable e) {
 				status.setFailed(true);
 				status.setFinished(true);
 				status.setErrorCause(e.getLocalizedMessage());
 				myLogger.error(e.getLocalizedMessage(), e);
 				throw new JobSubmissionException("Could not submit job.", e);
 			}
 
 		} catch (final NoSuchJobException e) {
 			// maybe it's a multipartjob
 			return getUser().getBatchJobManager().submitBatchJob(jobname);
 		}
 
 
 
 	}
 
 	public void uploadInputFile(String jobname, final DataHandler source,
 			final String targetFilename) throws NoSuchJobException,
 			RemoteFileSystemException {
 
 		// Thread.dumpStack();
 
 		try {
 			final Job job = getJobFromDatabaseOrFileSystem(
 					jobname);
 
 			// try whether job is single or multi
 			final DtoActionStatus status = new DtoActionStatus(targetFilename,
 					1);
 			getUser().getActionStatuses().put(targetFilename, status);
 
 			// new Thread() {
 			// @Override
 			// public void run() {
 
 			final String jobdir = job
 					.getJobProperty(Constants.JOBDIRECTORY_KEY);
 
 			try {
 				final String tarFileName = jobdir + "/" + targetFilename;
 				getUser().getFileManager().upload(source, tarFileName);
 				status.addElement("Upload to " + tarFileName + " successful.");
 				job.addInputFile(tarFileName);
 				saveOrUpdate(job);
 
 				status.setFinished(true);
 			} catch (final RemoteFileSystemException e) {
 				myLogger.error(e.getLocalizedMessage(), e);
 				status.addElement("Upload to " + jobdir + "/" + targetFilename
 						+ " failed: " + e.getLocalizedMessage());
 				status.setFinished(true);
 				status.setFailed(true);
 				status.setErrorCause(e.getLocalizedMessage());
 				// } finally {
 				// getUser().closeFileSystems();
 			}
 
 			// }
 			// }.start();
 			return;
 
 		} catch (final NoSuchJobException e) {
 			// no single job, let's try a multijob
 			getUser().getBatchJobManager().uploadInputFile(jobname, source,
 					targetFilename);
 		}
 
 
 
 		// TODO monitor status and set jobstatus to ready_to_submit?
 
 	}
 
 
 
 }
