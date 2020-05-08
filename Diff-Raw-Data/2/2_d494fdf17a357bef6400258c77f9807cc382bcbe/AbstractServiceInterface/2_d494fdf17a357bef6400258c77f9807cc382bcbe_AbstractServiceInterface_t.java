 package grisu.control.serviceInterfaces;
 
 import grisu.GrisuVersion;
 import grisu.backend.hibernate.BatchJobDAO;
 import grisu.backend.hibernate.JobDAO;
 import grisu.backend.info.InformationManagerManager;
 import grisu.backend.model.ProxyCredential;
 import grisu.backend.model.RemoteFileTransferObject;
 import grisu.backend.model.User;
 import grisu.backend.model.fs.GrisuOutputStream;
 import grisu.backend.model.job.BatchJob;
 import grisu.backend.model.job.Job;
 import grisu.backend.model.job.ServerJobSubmissionException;
 import grisu.backend.utils.LocalTemplatesHelper;
 import grisu.control.JobConstants;
 import grisu.control.ServiceInterface;
 import grisu.control.exceptions.BatchJobException;
 import grisu.control.exceptions.JobPropertiesException;
 import grisu.control.exceptions.JobSubmissionException;
 import grisu.control.exceptions.NoSuchJobException;
 import grisu.control.exceptions.NoValidCredentialException;
 import grisu.control.exceptions.RemoteFileSystemException;
 import grisu.jcommons.constants.Constants;
 import grisu.jcommons.constants.JobSubmissionProperty;
 import grisu.jcommons.interfaces.GridResource;
 import grisu.jcommons.interfaces.InformationManager;
 import grisu.jcommons.interfaces.MatchMaker;
 import grisu.jcommons.utils.JsdlHelpers;
 import grisu.jcommons.utils.SubmissionLocationHelpers;
 import grisu.model.FileManager;
 import grisu.model.MountPoint;
 import grisu.model.dto.DtoActionStatus;
 import grisu.model.dto.DtoApplicationDetails;
 import grisu.model.dto.DtoApplicationInfo;
 import grisu.model.dto.DtoBatchJob;
 import grisu.model.dto.DtoGridResources;
 import grisu.model.dto.DtoHostsInfo;
 import grisu.model.dto.DtoJob;
 import grisu.model.dto.DtoJobs;
 import grisu.model.dto.DtoMountPoints;
 import grisu.model.dto.DtoProperties;
 import grisu.model.dto.DtoProperty;
 import grisu.model.dto.DtoStringList;
 import grisu.model.dto.DtoSubmissionLocations;
 import grisu.model.dto.GridFile;
 import grisu.model.job.JobSubmissionObjectImpl;
 import grisu.model.utils.InformationUtils;
 import grisu.settings.ServerPropertiesManager;
 import grisu.utils.FileHelpers;
 import grisu.utils.SeveralXMLHelpers;
 import grith.jgrith.control.CertificateFiles;
 import grith.jgrith.control.VomsesFiles;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.UUID;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import javax.activation.DataHandler;
 import javax.annotation.security.RolesAllowed;
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.globus.common.CoGProperties;
 import org.simpleframework.xml.Serializer;
 import org.simpleframework.xml.core.Persister;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * This abstract class implements most of the methods of the
  * {@link ServiceInterface} interface. This way developers don't have to waste
  * time to implement the whole interface just for some things that are site/grid
  * specific. Currently there are two classes that extend this abstract class:
  * {@link LocalServiceInterface} and WsServiceInterface (which can be found in
  * the grisu-ws module).
  * 
  * The {@link LocalServiceInterface} is used to work with a small local database
  * like hsqldb so a user has got the whole grisu framework on his desktop. Of
  * course, all required ports have to be open from the desktop to the grid. On
  * the other hand no web service server is required.
  * 
  * The WsServiceInterface is the main one and it is used to set up a web service
  * somewhere. So light-weight clients can talk to it.
  * 
  * @author Markus Binsteiner
  */
 public abstract class AbstractServiceInterface implements ServiceInterface {
 
 	static Logger myLogger = null;
 	public static CacheManager cache;
 	static {
 
 		String log4jPath = "/etc/grisu/grisu-log4j.xml";
 		if (new File(log4jPath).exists() && (new File(log4jPath).length() > 0)) {
 			try {
 				DOMConfigurator.configure(log4jPath);
 			} catch (Exception e) {
 				myLogger.error(e);
 			}
 		}
 
 		myLogger = Logger.getLogger(AbstractServiceInterface.class.getName());
 
 		myLogger.debug("Logging initiated...");
 
 		myLogger.info("============================================");
 		myLogger.info("Starting up backend...");
 		myLogger.info("============================================");
 
 		myLogger.info("Setting networkaddress.cache.ttl java security property to -1...");
 		java.security.Security.setProperty("networkaddress.cache.ttl", "" + -1);
 
 		CoGProperties.getDefault().setProperty(
 				CoGProperties.ENFORCE_SIGNING_POLICY, "false");
 
 
 		try {
 			LocalTemplatesHelper.copyTemplatesAndMaybeGlobusFolder();
 			VomsesFiles.copyVomses();
 			CertificateFiles.copyCACerts(false);
 		} catch (final Exception e) {
 			// TODO Auto-generated catch block
 			myLogger.error(e.getLocalizedMessage());
 			// throw new
 			// RuntimeException("Could not initiate local backend: "+e.getLocalizedMessage());
 		}
 
 		// create ehcache manager singleton
 		try {
 			URL url = ClassLoader.getSystemResource("/grisu-ehcache.xml");
 			if (url == null) {
 				url = myLogger.getClass().getResource("/grisu-ehcache.xml");
 			}
 
 			CacheManager.create(url);
 			cache = CacheManager.getInstance();
 
 			Cache session = cache.getCache("session");
 			if (session == null) {
 				myLogger.debug("Session cache is null");
 			}
 		} catch (Exception e) {
 			myLogger.error(e);
 		}
 	}
 
 	public static final String BACKEND_VERSION = GrisuVersion.get("grisu-core");
 
 	public final static boolean INCLUDE_MULTIPARTJOBS_IN_PS_COMMAND = false;
 
 	public static final String REFRESH_STATUS_PREFIX = "REFRESH_";
 
 	public static String GRISU_BATCH_JOB_FILE_NAME = ".grisubatchjob";
 
 	public static final int DEFAULT_JOB_SUBMISSION_RETRIES = 5;
 
 	public static final InformationManager informationManager = createInformationManager();
 
 	private static final MatchMaker matchmaker = createMatchMaker();
 
 	public static InformationManager createInformationManager() {
 		return InformationManagerManager
 				.getInformationManager(ServerPropertiesManager
 						.getInformationManagerConf());
 	}
 
 	public static MatchMaker createMatchMaker() {
 		return InformationManagerManager.getMatchMaker(ServerPropertiesManager
 				.getMatchMakerConf());
 	}
 
 	public static Cache eternalCache() {
 		return cache.getCache("eternal");
 	}
 
 	public static Object getFromEternalCache(Object key) {
 		if ((key != null) && (eternalCache().get(key) != null)) {
 			return eternalCache().get(key).getObjectValue();
 		} else {
 			return null;
 		}
 	}
 
 	public static Object getFromSessionCache(Object key) {
 		if ((key != null) && (sessionCache().get(key) != null)) {
 			return sessionCache().get(key).getObjectValue();
 		} else {
 			return null;
 		}
 	}
 
 	public static Object getFromShortCache(Object key) {
 		if ((key != null) && (shortCache().get(key) != null)) {
 			return shortCache().get(key).getObjectValue();
 		} else {
 			return null;
 		}
 	}
 
 	public static void putIntoEternalCache(Object key, Object value) {
 		net.sf.ehcache.Element e = new net.sf.ehcache.Element(key, value);
 		eternalCache().put(e);
 	}
 
 	public static void putIntoSessionCache(Object key, Object value) {
 		net.sf.ehcache.Element e = new net.sf.ehcache.Element(key, value);
 		sessionCache().put(e);
 	}
 
 	public static void putIntoShortCache(Object key, Object value) {
 		net.sf.ehcache.Element e = new net.sf.ehcache.Element(key, value);
 		shortCache().put(e);
 	}
 
 	public static Cache sessionCache() {
 
 		return cache.getCache("session");
 	}
 
 	public static Cache shortCache() {
 		return cache.getCache("short");
 	}
 
 	// private final Map<String, List<Job>> archivedJobs = new HashMap<String,
 	// List<Job>>();
 	private String backendInfo = null;
 	private final boolean checkFileSystemsBeforeUse = false;
 	// protected final UserDAO userdao = new UserDAO();
 	protected final JobDAO jobdao = new JobDAO();
 
 	protected final BatchJobDAO batchJobDao = new BatchJobDAO();
 
 	private int SUBMIT_PROXY_LIFETIME = -1;
 
 	// private Map<String, RemoteFileTransferObject> fileTransfers = new
 	// HashMap<String, RemoteFileTransferObject>();
 
 	public void addArchiveLocation(String alias, String value) {
 
 		if (StringUtils.isBlank(value)) {
 			getUser().removeArchiveLocation(alias);
 		} else {
 			getUser().addArchiveLocation(alias, value);
 		}
 
 	}
 
 	public void addBookmark(String alias, String value) {
 
 		if (StringUtils.isBlank(value)) {
 			getUser().removeBookmark(alias);
 		} else {
 			getUser().addBookmark(alias, value);
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#addJobProperties(java.lang.String ,
 	 * java.util.Map)
 	 */
 	public void addJobProperties(final String jobname, final DtoJob properties)
 			throws NoSuchJobException {
 
 		final Job job = getUser().getJobFromDatabaseOrFileSystem(jobname);
 
 		final Map<String, String> temp = properties.propertiesAsMap();
 
 		// String urls = temp.get(Constants.INPUT_FILE_URLS_KEY);
 		// if ( StringUtils.isNotBlank(urls) ) {
 		temp.remove(Constants.INPUT_FILE_URLS_KEY);
 		// job.addInputFiles(Arrays.asList(urls.split(",")));
 		// }
 
 		job.addJobProperties(temp);
 		jobdao.saveOrUpdate(job);
 
 		// myLogger.debug("Added " + properties.getProperties().size()
 		// + " job properties.");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#addJobProperty(java.lang.String,
 	 * java.lang.String, java.lang.String)
 	 */
 	public void addJobProperty(final String jobname, final String key,
 			final String value) throws NoSuchJobException {
 
 		try {
 			final Job job = getUser().getJobFromDatabaseOrFileSystem(jobname);
 
 			// input files are added automatically
 			if (!Constants.INPUT_FILE_URLS_KEY.equals(key)) {
 				// if ( StringUtils.isBlank(value) ) {
 				// job.removeAllInputFiles();
 				// } else {
 				// job.addInputFiles(Arrays.asList(value.split(",")));
 				// }
 				// } else {
 				job.addJobProperty(key, value);
 				jobdao.saveOrUpdate(job);
 				// myLogger.debug("Added job property: " + key);
 			}
 		} catch (final NoSuchJobException e) {
 			final BatchJob job = getUser().getBatchJobFromDatabase(jobname);
 			job.addJobProperty(key, value);
 			batchJobDao.saveOrUpdate(job);
 			myLogger.debug("Added multijob property: " + key);
 		}
 
 	}
 
 	/**
 	 * Adds the specified job to the mulitpartJob.
 	 * 
 	 * @param batchJobname
 	 *            the batchJobname
 	 * @param jobname
 	 *            the jobname
 	 * @throws NoSuchJobException
 	 * @throws JobPropertiesException
 	 * @throws NoSuchJobException
 	 */
 	public String addJobToBatchJob(String batchJobname, String jsdlString)
 			throws JobPropertiesException, NoSuchJobException {
 
 		final BatchJob multiJob = getUser().getBatchJobFromDatabase(
 				batchJobname);
 
 		Document jsdl;
 
 		try {
 			jsdl = SeveralXMLHelpers.fromString(jsdlString);
 		} catch (final Exception e3) {
 			throw new RuntimeException("Invalid jsdl/xml format.", e3);
 		}
 
 		String jobnameCreationMethod = multiJob
 				.getJobProperty(Constants.JOBNAME_CREATION_METHOD_KEY);
 		if (StringUtils.isBlank(jobnameCreationMethod)) {
 			jobnameCreationMethod = "force-name";
 		}
 
 		final String jobname = createJob(jsdl, multiJob.getFqan(),
 				"force-name", multiJob);
 		multiJob.addJob(jobname);
 		multiJob.setStatus(JobConstants.READY_TO_SUBMIT);
 		batchJobDao.saveOrUpdate(multiJob);
 
 		return jobname;
 	}
 
 	private void archiveBatchJob(final BatchJob batchJob, final String target)
 			throws NoSuchJobException {
 
 		if ((getSessionActionStatus().get(batchJob.getBatchJobname()) != null)
 				&& !getSessionActionStatus().get(batchJob.getBatchJobname())
 				.isFinished()) {
 			// this should not really happen
 			myLogger.error("Not archiving job because jobsubmission is still ongoing.");
 			return;
 		}
 
 		final DtoActionStatus status = new DtoActionStatus(
 				ServiceInterface.ARCHIVE_STATUS_PREFIX
 				+ batchJob.getBatchJobname(), (batchJob.getJobs()
 						.size() * 3) + 3);
 		getSessionActionStatus().put(
 				ServiceInterface.ARCHIVE_STATUS_PREFIX
 				+ batchJob.getBatchJobname(), status);
 
 		final Thread archiveThread = new Thread() {
 			@Override
 			public void run() {
 
 				status.addElement("Starting to archive batchjob "
 						+ batchJob.getBatchJobname());
 
 				final ExecutorService executor = Executors
 						.newFixedThreadPool(ServerPropertiesManager
 								.getConcurrentFileTransfersPerUser());
 
 				for (final Job job : batchJob.getJobs()) {
 					status.addElement("Creating job archive thread for job "
 							+ job.getJobname());
 					final String jobdirUrl = job
 							.getJobProperty(Constants.JOBDIRECTORY_KEY);
 					final String targetDir = target + "/"
 							+ FileManager.getFilename(jobdirUrl);
 
 					String tmp = targetDir;
 					int i = 1;
 					try {
 						while (fileExists(tmp)) {
 							i = i + 1;
 							tmp = targetDir + "_" + i;
 						}
 					} catch (RemoteFileSystemException e2) {
 						myLogger.error(e2);
 						return;
 					}
 
 					final Thread archiveThread = archiveSingleJob(job, tmp,
 							status);
 					executor.execute(archiveThread);
 				}
 
 				executor.shutdown();
 
 				try {
 					executor.awaitTermination(24, TimeUnit.HOURS);
 				} catch (final InterruptedException e) {
 					myLogger.error(e);
 					status.setFailed(true);
 					status.setErrorCause(e.getLocalizedMessage());
 					status.setFinished(true);
 					status.addElement("Killing of sub-jobs interrupted: "
 							+ e.getLocalizedMessage());
 					return;
 				}
 
 				status.addElement("Killing batchjob.");
 				// now kill batchjob
 				final Thread deleteThread = deleteMultiPartJob(batchJob, true);
 
 				try {
 					deleteThread.join();
 					status.addElement("Batchjob killed.");
 				} catch (final InterruptedException e) {
 					status.setFailed(true);
 					status.setErrorCause("Archiving interrupted.");
 					status.setFinished(true);
 					myLogger.error(e);
 					return;
 				}
 
 				status.setFinished(true);
 			}
 		};
 
 		archiveThread.start();
 
 	}
 
 	public String archiveJob(String jobname, String target)
 			throws JobPropertiesException, NoSuchJobException,
 			RemoteFileSystemException {
 
 		if ((getSessionActionStatus().get(jobname) != null)
 				&& !getSessionActionStatus().get(jobname).isFinished()) {
 
 			myLogger.debug("not archiving job because jobsubmission is still ongoing.");
 			throw new JobPropertiesException(
 					"Job (re-)submission is still ongoing in background.");
 		}
 
 		if (StringUtils.isBlank(target)) {
 
 			String defArcLoc = getUser().getDefaultArchiveLocation();
 
 			if (StringUtils.isBlank(defArcLoc)) {
 				throw new RemoteFileSystemException(
 						"Archive location not specified.");
 			} else {
 				target = defArcLoc;
 			}
 		}
 
 		String url = null;
 		// make sure users can specify direct urls or aliases
 		for (String alias : getUser().getArchiveLocations().keySet()) {
 
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
 			final BatchJob job = getUser().getBatchJobFromDatabase(jobname);
 			final String jobdirUrl = job
 					.getJobProperty(Constants.JOBDIRECTORY_KEY);
 
 			final String targetDir = url + "/"
 					+ FileManager.getFilename(jobdirUrl);
 
 			archiveBatchJob(job, targetDir);
 			return targetDir;
 		} catch (final NoSuchJobException e) {
 			final Job job = getUser().getJobFromDatabaseOrFileSystem(jobname);
 
 			final String jobdirUrl = job
 					.getJobProperty(Constants.JOBDIRECTORY_KEY);
 			final String targetDir = url + "/"
 					+ FileManager.getFilename(jobdirUrl);
 
 			String tmp = targetDir;
 			int i = 1;
 			while (fileExists(tmp)) {
 				i = i + 1;
 				tmp = targetDir + "_no_" + i + "_";
 			}
 
 			final Thread archiveThread = archiveSingleJob(job, tmp, null);
 			archiveThread.start();
 
 			return tmp;
 		}
 	}
 
 	private Thread archiveSingleJob(final Job job, final String targetDirUrl,
 			final DtoActionStatus optionalBatchJobStatus) {
 
 		final DtoActionStatus status = new DtoActionStatus(
 				ServiceInterface.ARCHIVE_STATUS_PREFIX + job.getJobname(), 5);
 
 		getSessionActionStatus().put(status.getHandle(), status);
 
 		final Thread archiveThread = new Thread() {
 			@Override
 			public void run() {
 
 				if (optionalBatchJobStatus != null) {
 					optionalBatchJobStatus
 					.addElement("Starting archiving of job: "
 							+ job.getJobname());
 				}
 
 				if ((getSessionActionStatus().get(job.getJobname()) != null)
 						&& !getSessionActionStatus().get(job.getJobname())
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
 					rftp = cpSingleFile(
 							job.getJobProperty(Constants.JOBDIRECTORY_KEY),
 							targetDirUrl, false, true, true);
 					status.addElement("Deleting old jobdirectory: "
 							+ job.getJobProperty(Constants.JOBDIRECTORY_KEY));
 					deleteFile(job.getJobProperty(Constants.JOBDIRECTORY_KEY));
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
 
 				status.addElement("Creating " + GRISU_JOB_FILE_NAME + " file.");
 
 				final String grisuJobFileUrl = targetDirUrl + "/"
 						+ GRISU_JOB_FILE_NAME;
 				GrisuOutputStream fout = null;
 
 				try {
 					fout = getUser().getFileSystemManager().getOutputStream(
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
 					} catch (Exception e) {
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
 
 		return archiveThread;
 
 	}
 
 	private String calculateJobname(String jobname, String jobnameCreationMethod)
 			throws JobPropertiesException {
 
 		if ((jobnameCreationMethod == null)
 				|| Constants.FORCE_NAME_METHOD.equals(jobnameCreationMethod)) {
 
 			if (jobname == null) {
 				throw new JobPropertiesException(
 						JobSubmissionProperty.JOBNAME.toString()
 						+ ": "
 						+ "Jobname not specified and job creation method is force-name.");
 			}
 
 			final String[] allJobnames = getAllJobnames(null).asArray();
 			Arrays.sort(allJobnames);
 			if (Arrays.binarySearch(allJobnames, jobname) >= 0) {
 				throw new JobPropertiesException(
 						JobSubmissionProperty.JOBNAME.toString()
 						+ ": "
 						+ "Jobname "
 						+ jobname
 						+ " already exists and job creation method is force-name.");
 			}
 		} else if (Constants.UUID_NAME_METHOD.equals(jobnameCreationMethod)) {
 			if (jobname != null) {
 				jobname = jobname + "_" + UUID.randomUUID().toString();
 			} else {
 				jobname = UUID.randomUUID().toString();
 			}
 		} else if (Constants.TIMESTAMP_METHOD.equals(jobnameCreationMethod)) {
 
 			final String[] allJobnames = getAllJobnames(null).asArray();
 			Arrays.sort(allJobnames);
 
 			String temp;
 			do {
 				final String timestamp = new Long(new Date().getTime())
 				.toString();
 				try {
 					Thread.sleep(1);
 				} catch (final InterruptedException e) {
 					myLogger.debug(e);
 				}
 
 				temp = jobname;
 				if (temp == null) {
 					temp = timestamp;
 				} else {
 					temp = temp + "_" + timestamp;
 				}
 			} while (Arrays.binarySearch(allJobnames, temp) >= 0);
 
 			jobname = temp;
 
 		} else if (Constants.UNIQUE_NUMBER_METHOD.equals(jobnameCreationMethod)) {
 
 			String temp = jobname;
 			int i = 1;
 
 			SortedSet<String> jobNames = getAllJobnames(Constants.ALLJOBS_INCL_BATCH_KEY).asSortedSet();
 			jobNames.addAll(getAllBatchJobnames(null).asSortedSet());
 
 			while (jobNames.contains(temp)) {
 				temp = jobname + "_" + i;
 				i = i + 1;
 			}
 
 			jobname = temp;
 
 		} else {
 			throw new JobPropertiesException(
 					JobSubmissionProperty.JOBNAME.toString() + ": "
 							+ "Jobname creation method "
 							+ jobnameCreationMethod + " not supported.");
 		}
 
 		if (jobname == null) {
 			throw new RuntimeException(
 					"Jobname is null. This should never happen. Please report to markus.binsteiner@arcs.org.au");
 		}
 
 		return jobname;
 
 	}
 
 	private SortedSet<GridResource> calculateResourcesToUse(BatchJob mpj) {
 
 		final String locationsToIncludeString = mpj
 				.getJobProperty(Constants.LOCATIONS_TO_INCLUDE_KEY);
 		String[] locationsToInclude = null;
 		if (StringUtils.isNotBlank(locationsToIncludeString)) {
 			locationsToInclude = locationsToIncludeString.split(",");
 		}
 
 		final String locationsToExcludeString = mpj
 				.getJobProperty(Constants.LOCATIONS_TO_EXCLUDE_KEY);
 		String[] locationsToExclude = null;
 		if (StringUtils.isNotBlank(locationsToExcludeString)) {
 			locationsToExclude = locationsToExcludeString.split(",");
 		}
 
 		final SortedSet<GridResource> resourcesToUse = new TreeSet<GridResource>();
 
 		for (final GridResource resource : findBestResourcesForMultipartJob(mpj)) {
 
 			final String tempSubLocString = SubmissionLocationHelpers
 					.createSubmissionLocationString(resource);
 
 			// check whether subloc is available for vo
 			final String[] allSubLocs = informationManager
 					.getAllSubmissionLocationsForVO(mpj.getFqan());
 			Arrays.sort(allSubLocs);
 			final int i = Arrays.binarySearch(allSubLocs, tempSubLocString);
 			if (i < 0) {
 				continue;
 			}
 
 			if ((locationsToInclude != null) && (locationsToInclude.length > 0)) {
 
 				for (final String subLoc : locationsToInclude) {
 					if (tempSubLocString.toLowerCase().contains(
 							subLoc.toLowerCase())) {
 						if (isValidSubmissionLocation(tempSubLocString,
 								mpj.getFqan())) {
 							resourcesToUse.add(resource);
 						}
 						break;
 					}
 				}
 
 			} else if ((locationsToExclude != null)
 					&& (locationsToExclude.length > 0)) {
 
 				boolean useSubLoc = true;
 				for (final String subLoc : locationsToExclude) {
 					if (tempSubLocString.toLowerCase().contains(
 							subLoc.toLowerCase())) {
 						useSubLoc = false;
 						break;
 					}
 				}
 				if (useSubLoc) {
 					if (isValidSubmissionLocation(tempSubLocString,
 							mpj.getFqan())) {
 						resourcesToUse.add(resource);
 					}
 				}
 
 			} else {
 
 				if (isValidSubmissionLocation(tempSubLocString, mpj.getFqan())) {
 					resourcesToUse.add(resource);
 				}
 			}
 		}
 
 		if (checkFileSystemsBeforeUse) {
 
 			// myLogger.debug("Checking filesystems to use...");
 
 			final ExecutorService executor1 = Executors
 					.newFixedThreadPool(ServerPropertiesManager
 							.getConcurrentFileTransfersPerUser());
 
 			final Set<GridResource> failSet = Collections
 					.synchronizedSet(new HashSet<GridResource>());
 
 			for (final GridResource gr : resourcesToUse) {
 
 				final String subLoc = SubmissionLocationHelpers
 						.createSubmissionLocationString(gr);
 
 				final String[] fs = informationManager
 						.getStagingFileSystemForSubmissionLocation(subLoc);
 
 				for (final MountPoint mp : getUser().df(mpj.getFqan())) {
 
 					for (final String f : fs) {
 						if (mp.getRootUrl().startsWith(f.replace(":2811", ""))) {
 
 							final Thread thread = new Thread() {
 								@Override
 								public void run() {
 									try {
 										if (!fileExists(mp.getRootUrl())) {
 											myLogger.error("Removing sub loc "
 													+ subLoc);
 											failSet.add(gr);
 										}
 									} catch (final RemoteFileSystemException e) {
 										myLogger.error("Removing sub loc "
 												+ subLoc + ": "
 												+ e.getLocalizedMessage());
 										failSet.add(gr);
 									}
 								}
 							};
 							executor1.execute(thread);
 						}
 					}
 				}
 			}
 
 			executor1.shutdown();
 
 			try {
 				executor1.awaitTermination(3600, TimeUnit.SECONDS);
 			} catch (final InterruptedException e) {
 				myLogger.error(e);
 			}
 
 			resourcesToUse.removeAll(failSet);
 			// myLogger.debug("Checking filesystems to use: finished");
 			myLogger.debug("Removed filesystems for batchjob: "
 					+ StringUtils.join(failSet, ","));
 		}
 
 		return resourcesToUse;
 
 	}
 
 	private boolean checkWhetherGridResourceIsActuallyAvailable(
 			GridResource resource) {
 
 		final String[] filesystems = informationManager
 				.getStagingFileSystemForSubmissionLocation(SubmissionLocationHelpers
 						.createSubmissionLocationString(resource));
 
 		for (final MountPoint mp : df().getMountpoints()) {
 
 			for (final String fs : filesystems) {
 				if (mp.getRootUrl().startsWith(fs.replace(":2811", ""))) {
 					return true;
 				}
 			}
 
 		}
 
 		return false;
 
 	}
 
 	public void copyBatchJobInputFile(String batchJobname, String inputFile,
 			String filename) throws RemoteFileSystemException,
 			NoSuchJobException {
 
 		final BatchJob multiJob = getUser().getBatchJobFromDatabase(
 				batchJobname);
 
 		final String relpathFromMountPointRoot = multiJob
 				.getJobProperty(Constants.RELATIVE_BATCHJOB_DIRECTORY_KEY);
 
 		for (final String mountPointRoot : multiJob.getAllUsedMountPoints()) {
 
 			final String targetUrl = mountPointRoot + "/"
 					+ relpathFromMountPointRoot + "/" + filename;
 			myLogger.debug("Coping multipartjob inputfile " + filename
 					+ " to: " + targetUrl);
 			cpSingleFile(inputFile, targetUrl, true, true, true);
 
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#cp(java.lang.String,
 	 * java.lang.String, boolean, boolean)
 	 */
 	public String cp(final DtoStringList sources, final String target,
 			final boolean overwrite, final boolean waitForFileTransferToFinish)
 					throws RemoteFileSystemException {
 
 		String handle = null;
 
 		if (getSessionActionStatus().get(target) == null) {
 			handle = target;
 		} else {
 			int counter = 0;
 			do {
 				handle = target + "_" + counter;
 				counter = counter + 1;
 			} while (getSessionActionStatus().get(handle) != null);
 		}
 
 		final DtoActionStatus actionStat = new DtoActionStatus(handle,
 				sources.asArray().length * 2);
 
 		getSessionActionStatus().put(handle, actionStat);
 
 		final String handleFinal = handle;
 		final Thread cpThread = new Thread() {
 			@Override
 			public void run() {
 				try {
 					for (final String source : sources.asArray()) {
 						actionStat.addElement("Starting transfer of file: "
 								+ source);
 						final String filename = FileHelpers.getFilename(source);
 						final RemoteFileTransferObject rto = cpSingleFile(
 								source, target + "/" + filename, overwrite,
 								true, true);
 
 						if (rto.isFailed()) {
 							actionStat.setFailed(true);
 							actionStat.setErrorCause(rto
 									.getPossibleExceptionMessage());
 							actionStat.setFinished(true);
 							actionStat.addElement("Transfer failed: "
 									+ rto.getPossibleException()
 									.getLocalizedMessage());
 							throw new RemoteFileSystemException(rto
 									.getPossibleException()
 									.getLocalizedMessage());
 						} else {
 							actionStat.addElement("Finished transfer of file: "
 									+ source);
 						}
 					}
 					actionStat.setFinished(true);
 				} catch (final Exception e) {
 					myLogger.error(e);
 					actionStat.setFailed(true);
 					actionStat.setErrorCause(e.getLocalizedMessage());
 					actionStat.setFinished(true);
 					actionStat.addElement("Transfer failed: "
 							+ e.getLocalizedMessage());
 				}
 
 			}
 		};
 
 		cpThread.start();
 
 		if (waitForFileTransferToFinish) {
 			try {
 				cpThread.join();
 
 				if (actionStat.isFailed()) {
 					throw new RemoteFileSystemException(
 							DtoActionStatus.getLastMessage(actionStat));
 				}
 			} catch (final InterruptedException e) {
 				myLogger.error(e);
 			}
 		}
 
 		return handle;
 
 	}
 
 	private RemoteFileTransferObject cpSingleFile(final String source,
 			final String target, final boolean overwrite,
 			final boolean startFileTransfer,
 			final boolean waitForFileTransferToFinish)
 					throws RemoteFileSystemException {
 
 		final RemoteFileTransferObject fileTransfer = getUser()
 				.getFileSystemManager().copy(source, target, overwrite);
 
 		if (startFileTransfer) {
 			fileTransfer.startTransfer(waitForFileTransferToFinish);
 		}
 
 		return fileTransfer;
 	}
 
 	/**
 	 * Creates a multipartjob on the server.
 	 * 
 	 * A multipartjob is just a collection of jobs that belong together to make
 	 * them more easily managable.
 	 * 
 	 * @param batchJobname
 	 *            the id (name) of the multipartjob
 	 * @throws JobPropertiesException
 	 */
 	public DtoBatchJob createBatchJob(String batchJobnameBase, String fqan,
 			String jobnameCreationMethod) throws BatchJobException {
 
 		String batchJobname = null;
 		try {
 			batchJobname = calculateJobname(batchJobnameBase,
 					jobnameCreationMethod);
 		} catch (JobPropertiesException e2) {
 			throw new BatchJobException("Can't calculate jobname: "
 					+ e2.getLocalizedMessage(), e2);
 		}
 
 		if (Constants.NO_JOBNAME_INDICATOR_STRING.equals(batchJobname)) {
 			throw new BatchJobException("BatchJobname can't be "
 					+ Constants.NO_JOBNAME_INDICATOR_STRING);
 		}
 
 		try {
 			final Job possibleJob = getUser().getJobFromDatabaseOrFileSystem(
 					batchJobname);
 			throw new BatchJobException("Can't create multipartjob with id: "
 					+ batchJobname
 					+ ". Non-multipartjob with this id already exists...");
 		} catch (final NoSuchJobException e) {
 			// that's good
 		}
 
 		try {
 			final BatchJob multiJob = getUser().getBatchJobFromDatabase(
 					batchJobname);
 		} catch (final NoSuchJobException e) {
 			// that's good
 
 			final BatchJob multiJobCreate = new BatchJob(getDN(), batchJobname,
 					fqan);
 			multiJobCreate.addJobProperty(Constants.RELATIVE_PATH_FROM_JOBDIR,
 					"../");
 			multiJobCreate.addJobProperty(
 					Constants.RELATIVE_BATCHJOB_DIRECTORY_KEY,
 					ServerPropertiesManager.getRunningJobsDirectoryName() + "/"
 							+ batchJobname);
 
 			multiJobCreate.addLogMessage("MultiPartJob " + batchJobname
 					+ " created.");
 
 			// multiJobCreate
 			// .setResourcesToUse(calculateResourcesToUse(multiJobCreate));
 
 			multiJobCreate.setStatus(JobConstants.JOB_CREATED);
 
 			batchJobDao.saveOrUpdate(multiJobCreate);
 
 			try {
 				return multiJobCreate.createDtoMultiPartJob();
 			} catch (final NoSuchJobException e1) {
 				myLogger.error(e1);
 			}
 		}
 
 		throw new BatchJobException("MultiPartJob with name " + batchJobname
 				+ " already exists.");
 	}
 
 	private String createJob(Document jsdl, final String fqan,
 			final String jobnameCreationMethod,
 			final BatchJob optionalParentBatchJob)
 					throws JobPropertiesException {
 
 		String jobname = JsdlHelpers.getJobname(jsdl);
 
 		jobname = calculateJobname(jobname, jobnameCreationMethod);
 
 		if (Constants.NO_JOBNAME_INDICATOR_STRING.equals(jobname)) {
 			throw new JobPropertiesException("Jobname can't be "
 					+ Constants.NO_JOBNAME_INDICATOR_STRING);
 		}
 
 		try {
 			final BatchJob mpj = getUser().getBatchJobFromDatabase(jobname);
 			throw new JobPropertiesException(
 					"Could not create job with jobname " + jobname
 					+ ". Multipart job with this id already exists...");
 		} catch (final NoSuchJobException e) {
 			// that's good
 		}
 
 		Job job;
 		try {
 			// myLogger.debug("Trying to get job that shouldn't exist...");
 			job = getUser().getJobFromDatabaseOrFileSystem(jobname);
 			throw new JobPropertiesException(
 					JobSubmissionProperty.JOBNAME.toString() + ": "
 							+ "Jobname \"" + jobname
 							+ "\" already taken. Could not create job.");
 		} catch (final NoSuchJobException e1) {
 			// that's ok
 			// myLogger.debug("Checked jobname. Not yet in database. Good.");
 		}
 
 		// creating job
 		getCredential(); // just to be sure that nothing stale get's created in
 		// the db unnecessary
 		job = new Job(getCredential().getDn(), jobname);
 
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
 
 	public String createJob(String jsdlString, final String fqan,
 			final String jobnameCreationMethod) throws JobPropertiesException {
 
 		Document jsdl;
 
 		try {
 			jsdl = SeveralXMLHelpers.fromString(jsdlString);
 		} catch (final Exception e3) {
 
 			myLogger.error(e3);
 			throw new RuntimeException("Invalid jsdl/xml format.", e3);
 		}
 
 		return createJob(jsdl, fqan, jobnameCreationMethod, null);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#deleteFile(java.lang.String)
 	 */
 	public void deleteFile(final String file) throws RemoteFileSystemException {
 
 		getUser().getFileSystemManager().deleteFile(file);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#deleteFiles(java.lang.String[])
 	 */
 	public String deleteFiles(final DtoStringList files) {
 
 		// TODO implement that as background task
 
 		if ((files == null) || (files.asArray().length == 0)) {
 			return null;
 		}
 
 		final DtoActionStatus status = new DtoActionStatus(files.asArray()[0],
 				files.asArray().length * 2);
 		getSessionActionStatus().put(files.asArray()[0], status);
 
 		for (final String file : files.getStringList()) {
 			try {
 				status.addElement("Deleting file " + file + "...");
 				deleteFile(file);
 				status.addElement("Success.");
 			} catch (final Exception e) {
 				status.addElement("Failed: " + e.getLocalizedMessage());
 				status.setFailed(true);
 				status.setErrorCause(e.getLocalizedMessage());
 				myLogger.error("Could not delete file: " + file);
 				// filesNotDeleted.add(file);
 			}
 		}
 
 		status.setFinished(true);
 
 		return null;
 
 	}
 
 	/**
 	 * Removes the multipartJob from the server.
 	 * 
 	 * @param batchJobname
 	 *            the name of the multipartJob
 	 * @param deleteChildJobsAsWell
 	 *            whether to delete the child jobs of this multipartjob as well.
 	 */
 	private Thread deleteMultiPartJob(final BatchJob multiJob,
 			final boolean clean) {
 
 		int size = (multiJob.getJobs().size() * 2) + 1;
 
 		if (clean) {
 			size = size + (multiJob.getAllUsedMountPoints().size() * 2);
 		}
 
 		final DtoActionStatus newActionStatus = new DtoActionStatus(
 				multiJob.getBatchJobname(), size);
 		this.getSessionActionStatus().put(multiJob.getBatchJobname(),
 				newActionStatus);
 
 		final ExecutorService executor = Executors
 				.newFixedThreadPool(ServerPropertiesManager
 						.getConcurrentMultiPartJobSubmitThreadsPerUser());
 
 		final Job[] jobs = multiJob.getJobs().toArray(new Job[] {});
 
 		for (final Job job : jobs) {
 			multiJob.removeJob(job);
 		}
 		batchJobDao.saveOrUpdate(multiJob);
 		for (final Job job : jobs) {
 			final Thread thread = new Thread("killing_" + job.getJobname()) {
 				@Override
 				public void run() {
 					try {
 						myLogger.debug("Killing job " + job.getJobname()
 								+ " in thread "
 								+ Thread.currentThread().getName());
 
 						newActionStatus.addElement("Killing job: "
 								+ job.getJobname());
 						kill(job, clean, clean);
 						myLogger.debug("Killed job " + job.getJobname()
 								+ " in thread "
 								+ Thread.currentThread().getName());
 						newActionStatus.addElement("Killed job: "
 								+ job.getJobname());
 					} catch (final Exception e) {
 						newActionStatus.addElement("Failed killing job "
 								+ job.getJobname() + ": "
 								+ e.getLocalizedMessage());
 						newActionStatus.setFailed(true);
 						newActionStatus.setErrorCause(e.getLocalizedMessage());
 						myLogger.error(e);
 					}
 					if (newActionStatus.getTotalElements() <= newActionStatus
 							.getCurrentElements()) {
 						newActionStatus.setFinished(true);
 					}
 
 				}
 			};
 
 			executor.execute(thread);
 		}
 
 		executor.shutdown();
 
 		final Thread cleanupThread = new Thread() {
 
 			@Override
 			public void run() {
 
 				try {
 					executor.awaitTermination(2, TimeUnit.HOURS);
 				} catch (final InterruptedException e1) {
 					myLogger.error(e1);
 				}
 
 				try {
 					if (clean) {
 						for (final String mpRoot : multiJob
 								.getAllUsedMountPoints()) {
 
 							newActionStatus
 							.addElement("Deleting common dir for mountpoint: "
 									+ mpRoot);
 							final String url = mpRoot
 									+ multiJob
 									.getJobProperty(Constants.RELATIVE_BATCHJOB_DIRECTORY_KEY);
 							myLogger.debug("Deleting multijobDir: " + url);
 							try {
 								deleteFile(url);
 								newActionStatus
 								.addElement("Deleted common dir for mountpoint: "
 										+ mpRoot);
 							} catch (final RemoteFileSystemException e) {
 								newActionStatus
 								.addElement("Couldn't delete common dir for mountpoint: "
 										+ mpRoot);
 								newActionStatus.setFailed(true);
 								newActionStatus.setErrorCause(e
 										.getLocalizedMessage());
 								myLogger.error("Couldn't delete multijobDir: "
 										+ url);
 							}
 
 						}
 					}
 
 					batchJobDao.delete(multiJob);
 					newActionStatus
 					.addElement("Deleted multipartjob from database.");
 
 				} finally {
 					newActionStatus.setFinished(true);
 				}
 
 			}
 		};
 
 		cleanupThread.start();
 
 		return cleanupThread;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#df()
 	 */
 	public synchronized DtoMountPoints df() {
 
 		return DtoMountPoints.createMountpoints(getUser().getAllMountPoints());
 	}
 
 	public DataHandler download(final String filename)
 			throws RemoteFileSystemException {
 
 		// myLogger.debug("Downloading: " + filename);
 
 		return getUser().getFileSystemManager().download(filename);
 	}
 
 	public boolean fileExists(final String file)
 			throws RemoteFileSystemException {
 
 		return getUser().getFileSystemManager().fileExists(file);
 
 	}
 
 	public GridFile fillFolder(GridFile folder, int recursionLevel)
 			throws RemoteFileSystemException {
 
 		GridFile tempFolder = null;
 
 		try {
 			tempFolder = getUser().getFileSystemManager().getFolderListing(
 					folder.getUrl(), 1);
 		} catch (final Exception e) {
 			// myLogger.error(e);
 			myLogger.error(
 					"Error getting folder listing. I suspect this to be a bug in the commons-vfs-grid library. Sleeping for 1 seconds and then trying again...",
 					e);
 			try {
 				Thread.sleep(1000);
 			} catch (final InterruptedException e1) {
 				myLogger.error(e1);
 			}
 			tempFolder = getUser().getFileSystemManager().getFolderListing(
 					folder.getUrl(), 1);
 
 		}
 		folder.setChildren(tempFolder.getChildren());
 
 		if (recursionLevel > 0) {
 			for (final GridFile childFolder : tempFolder.getChildren()) {
 				if (childFolder.isFolder()) {
 					folder.addChild(fillFolder(childFolder, recursionLevel - 1));
 				}
 			}
 
 		}
 		return folder;
 	}
 
 	private SortedSet<GridResource> findBestResourcesForMultipartJob(
 			BatchJob mpj) {
 
 		final Map<JobSubmissionProperty, String> properties = new HashMap<JobSubmissionProperty, String>();
 
 		String defaultApplication = mpj
 				.getJobProperty(Constants.APPLICATIONNAME_KEY);
 		if (StringUtils.isBlank(defaultApplication)) {
 			defaultApplication = Constants.GENERIC_APPLICATION_NAME;
 		}
 		properties.put(JobSubmissionProperty.APPLICATIONNAME,
 				defaultApplication);
 
 		String defaultCpus = mpj.getJobProperty(Constants.NO_CPUS_KEY);
 		if (StringUtils.isBlank(defaultCpus)) {
 			defaultCpus = "1";
 		}
 		properties.put(JobSubmissionProperty.NO_CPUS,
 				mpj.getJobProperty(Constants.NO_CPUS_KEY));
 
 		String defaultVersion = mpj
 				.getJobProperty(Constants.APPLICATIONVERSION_KEY);
 		if (StringUtils.isBlank(defaultVersion)) {
 			defaultVersion = Constants.NO_VERSION_INDICATOR_STRING;
 		}
 		properties
 		.put(JobSubmissionProperty.APPLICATIONVERSION, defaultVersion);
 
 		String maxWalltime = mpj
 				.getJobProperty(Constants.WALLTIME_IN_MINUTES_KEY);
 		if (StringUtils.isBlank(maxWalltime)) {
 			int mwt = 0;
 			for (final Job job : mpj.getJobs()) {
 				final int wt = new Integer(
 						job.getJobProperty(Constants.WALLTIME_IN_MINUTES_KEY));
 				if (mwt < wt) {
 					mwt = wt;
 				}
 			}
 			maxWalltime = new Integer(mwt).toString();
 		}
 
 		properties.put(JobSubmissionProperty.WALLTIME_IN_MINUTES, maxWalltime);
 
 		final SortedSet<GridResource> result = new TreeSet<GridResource>(
 				matchmaker.findAvailableResources(properties, mpj.getFqan()));
 
 		// StringBuffer message = new StringBuffer(
 		// "Finding best resources for mulipartjob " + batchJobname
 		// + " using:\n");
 		// message.append("Version: " + defaultVersion + "\n");
 		// message.append("Walltime in minutes: " +
 		// maxWalltimeInSecondsAcrossJobs
 		// / 60 + "\n");
 		// message.append("No cpus: " + defaultNoCpus + "\n");
 
 		return result;
 
 	}
 
 	public DtoGridResources findMatchingSubmissionLocationsUsingJsdl(
 			String jsdlString, final String fqan,
 			boolean excludeResourcesWithLessCPUslotsFreeThanRequested) {
 
 		Document jsdl;
 		try {
 			jsdl = SeveralXMLHelpers.fromString(jsdlString);
 		} catch (final Exception e) {
 			throw new RuntimeException(e);
 		}
 
 		// LinkedList<String> result = new LinkedList<String>();
 
 		List<GridResource> resources = null;
 		if (excludeResourcesWithLessCPUslotsFreeThanRequested) {
 			resources = matchmaker.findAvailableResources(jsdl, fqan);
 		} else {
 			resources = matchmaker.findAllResources(jsdl, fqan);
 		}
 
 		return DtoGridResources.createGridResources(resources);
 
 	}
 
 	public DtoGridResources findMatchingSubmissionLocationsUsingMap(
 			final DtoJob jobProperties, final String fqan,
 			boolean excludeResourcesWithLessCPUslotsFreeThanRequested) {
 
 		final LinkedList<String> result = new LinkedList<String>();
 
 		final Map<JobSubmissionProperty, String> converterMap = new HashMap<JobSubmissionProperty, String>();
 		for (final DtoProperty jp : jobProperties.getProperties()) {
 			converterMap.put(JobSubmissionProperty.fromString(jp.getKey()),
 					jp.getValue());
 		}
 
 		List<GridResource> resources = null;
 		if (excludeResourcesWithLessCPUslotsFreeThanRequested) {
 			resources = matchmaker.findAvailableResources(converterMap, fqan);
 		} else {
 			resources = matchmaker.findAllResources(converterMap, fqan);
 		}
 
 		return DtoGridResources.createGridResources(resources);
 	}
 
 	public DtoActionStatus getActionStatus(String actionHandle) {
 
 		final DtoActionStatus result = getSessionActionStatus().get(
 				actionHandle);
 
 		// System.out.println("Elements before: " + result.getLog().size());
 
 		return result;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#ps()
 	 */
 	public DtoJobs getActiveJobs(String application, boolean refresh) {
 
 		try {
 
 			List<Job> jobs = getUser().getActiveJobs(application, refresh);
 
 			final DtoJobs dtoJobs = new DtoJobs();
 			for (final Job job : jobs) {
 
 				final DtoJob dtojob = DtoJob.createJob(job.getStatus(),
 						job.getJobProperties(), job.getInputFiles(),
 						job.getLogMessages(), false);
 
 				// just to make sure
 				dtojob.addJobProperty(Constants.JOBNAME_KEY, job.getJobname());
 				dtoJobs.addJob(dtojob);
 			}
 
 			return dtoJobs;
 		} catch (final Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getAllAvailableApplications(java
 	 * .lang.String[])
 	 */
 	public DtoStringList getAllAvailableApplications(final DtoStringList fqans) {
 		final Set<String> fqanList = new TreeSet<String>();
 
 		if ((fqans == null) || (fqans.asSortedSet().size() == 0)) {
 			return DtoStringList.fromStringArray(informationManager
 					.getAllApplicationsOnGrid());
 		}
 
 		for (final String fqan : fqans.getStringList()) {
 			fqanList.addAll(Arrays.asList(informationManager
 					.getAllApplicationsOnGridForVO(fqan)));
 
 		}
 
 		return DtoStringList.fromStringArray(fqanList.toArray(new String[] {}));
 
 	}
 
 	public DtoStringList getAllBatchJobnames(String application) {
 
 		List<String> jobnames = null;
 
 		if (StringUtils.isBlank(application)
 				|| Constants.ALLJOBS_KEY.equals(application)) {
 			jobnames = batchJobDao.findJobNamesByDn(getUser().getDn());
 		} else {
 			jobnames = batchJobDao.findJobNamesPerApplicationByDn(getUser()
 					.getDn(), application);
 		}
 
 		return DtoStringList.fromStringList(jobnames);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getAllHosts()
 	 */
 	public synchronized DtoHostsInfo getAllHosts() {
 
 		final DtoHostsInfo info = DtoHostsInfo
 				.createHostsInfo(informationManager.getAllHosts());
 
 		return info;
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
 
 	public DtoStringList getAllSites() {
 
 		Date now = new Date();
 		DtoStringList result = DtoStringList.fromStringArray(informationManager
 				.getAllSites());
 		myLogger.debug("Login benchmark - getting all sites: "
 				+ (new Date().getTime() - now.getTime()) + " ms");
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getAllSubmissionLocations()
 	 */
 	public synchronized DtoSubmissionLocations getAllSubmissionLocations() {
 
 		final DtoSubmissionLocations locs = DtoSubmissionLocations
 				.createSubmissionLocationsInfo(informationManager
 						.getAllSubmissionLocations());
 
 		locs.removeUnuseableSubmissionLocations(informationManager, df()
 				.getMountpoints());
 		return locs;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getAllSubmissionLocations(java
 	 * .lang.String)
 	 */
 	public DtoSubmissionLocations getAllSubmissionLocationsForFqan(
 			final String fqan) {
 
 		final DtoSubmissionLocations locs = DtoSubmissionLocations
 				.createSubmissionLocationsInfo(informationManager
 						.getAllSubmissionLocationsForVO(fqan));
 
 		locs.removeUnuseableSubmissionLocations(informationManager, df()
 				.getMountpoints());
 		return locs;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getApplicationDetails(java.lang
 	 * .String, java.lang.String, java.lang.String)
 	 */
 	public DtoApplicationDetails getApplicationDetailsForVersionAndSubmissionLocation(
 			final String application, final String version,
 			final String submissionLocation) {
 
 		// String site = site_or_submissionLocation;
 		// if (isSubmissionLocation(site_or_submissionLocation)) {
 		// myLogger.debug("Parameter " + site_or_submissionLocation
 		// + "is submission location not site. Calculating site...");
 		// site = getSiteForSubmissionLocation(site_or_submissionLocation);
 		// myLogger.debug("Site is: " + site);
 		// }
 
 		return DtoApplicationDetails.createDetails(application,
 				informationManager.getApplicationDetails(application, version,
 						submissionLocation));
 	}
 
 	public String[] getApplicationPackagesForExecutable(String executable) {
 
 		return informationManager
 				.getApplicationsThatProvideExecutable(executable);
 
 	}
 
 	public DtoJobs getArchivedJobs(String application) {
 
 		List<Job> jobs = getUser().getArchivedJobs(application);
 
 		final DtoJobs dtoJobs = new DtoJobs();
 		for (final Job job : jobs) {
 
 			final DtoJob dtojob = DtoJob.createJob(job.getStatus(),
 					job.getJobProperties(), job.getInputFiles(),
 					job.getLogMessages(), false);
 
 			// just to make sure
 			dtojob.addJobProperty(Constants.JOBNAME_KEY, job.getJobname());
 			dtoJobs.addJob(dtojob);
 		}
 
 		return dtoJobs;
 	}
 
 	public DtoProperties getArchiveLocations() {
 
 		return DtoProperties.createProperties(getUser().getArchiveLocations());
 
 	}
 
 	private String getBackendInfo() {
 
 		if (StringUtils.isBlank(backendInfo)) {
 			String host = getInterfaceInfo("HOSTNAME");
 			if (StringUtils.isBlank(host)) {
 				host = "Host unknown";
 			}
 			String version = getInterfaceInfo("VERSION");
 			if (StringUtils.isBlank(version)) {
 				version = "Version unknown";
 			}
 			String name = getInterfaceInfo("NAME");
 			if (StringUtils.isBlank(name)) {
 				name = "Backend name unknown";
 			}
 
 			backendInfo = name + " / " + host + " / version:" + version;
 
 		}
 		return backendInfo;
 	}
 
 	/**
 	 * Returns all multipart jobs for this user.
 	 * 
 	 * @return all the multipartjobs of the user
 	 */
 	public DtoBatchJob getBatchJob(String batchJobname)
 			throws NoSuchJobException {
 
 		final BatchJob multiPartJob = getUser().getBatchJobFromDatabase(
 				batchJobname);
 
 		// TODO enable loading of batchjob from jobdirectory url
 
 		return multiPartJob.createDtoMultiPartJob();
 	}
 
 	public DtoProperties getBookmarks() {
 
 		return DtoProperties.createProperties(getUser().getBookmarks());
 	}
 
 	/**
 	 * This method has to be implemented by the endpoint specific
 	 * ServiceInterface. Since there are a few different ways to get a proxy
 	 * credential (myproxy, just use the one in /tmp/x509..., shibb,...) this
 	 * needs to be implemented differently for every single situation.
 	 * 
 	 * @return the proxy credential that is used to contact the grid
 	 */
 	protected abstract ProxyCredential getCredential();
 
 	/**
 	 * This is mainly for testing, to enable credentials with specified
 	 * lifetimes.
 	 * 
 	 * @param fqan the vo
 	 * @param lifetime
 	 *            the lifetime in seconds
 	 * @return the credential
 	 */
 	protected abstract ProxyCredential getCredential(String fqan, int lifetime);
 
 	// public DtoDataLocations getDataLocationsForVO(final String fqan) {
 	//
 	// return DtoDataLocations.createDataLocations(fqan,
 	// informationManager.getDataLocationsForVO(fqan));
 	//
 	// }
 
 	/**
 	 * Calculates the default version of an application on a site. This is
 	 * pretty hard to do, so, if you call this method, don't expect anything
 	 * that makes 100% sense, I'm afraid.
 	 * 
 	 * @param application
 	 *            the name of the application
 	 * @param site
 	 *            the site
 	 * @return the default version of the application on this site
 	 */
 	private String getDefaultVersionForApplicationAtSite(
 			final String application, final String site) {
 
 		final String[] versions = informationManager
 				.getVersionsOfApplicationOnSite(application, site);
 		double latestVersion = 0;
 		int index = 0;
 		try {
 			latestVersion = Double.valueOf(versions[0]).doubleValue();
 			for (int i = 1; i < versions.length; i++) {
 				if (Double.valueOf(versions[i]).doubleValue() > latestVersion) {
 					index = i;
 				}
 			}
 			return versions[index];
 		} catch (final NumberFormatException e) {
 			return versions[0];
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getDN()
 	 */
 	@RolesAllowed("User")
 	public String getDN() {
 		return getUser().getDn();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getFileSize(java.lang.String)
 	 */
 	public long getFileSize(final String file) throws RemoteFileSystemException {
 
 		return getUser().getFileSystemManager().getFileSize(file);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getFqans()
 	 */
 	public DtoStringList getFqans() {
 		return DtoStringList.fromStringColletion(getUser().getFqans().keySet());
 	}
 
 	abstract public String getInterfaceInfo(String key);
 
 	public int getInterfaceVersion() {
 		return API_VERSION;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getAllJobProperties(java.lang
 	 * .String)
 	 */
 	public DtoJob getJob(final String jobnameOrUrl) throws NoSuchJobException {
 
 		Job job = getUser().getJobFromDatabaseOrFileSystem(jobnameOrUrl);
 
 		// job.getJobProperties().put(Constants.JOB_STATUS_KEY,
 		// JobConstants.translateStatus(getJobStatus(jobname)));
 
 		return DtoJob.createJob(job.getStatus(), job.getJobProperties(),
 				job.getInputFiles(), job.getLogMessages(), job.isArchived());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getJobProperty(java.lang.String,
 	 * java.lang.String)
 	 */
 	public String getJobProperty(final String jobname, final String key)
 			throws NoSuchJobException {
 
 		try {
 			final Job job = getUser().getJobFromDatabaseOrFileSystem(jobname);
 
 			if (Constants.INPUT_FILE_URLS_KEY.equals(key)) {
 				return StringUtils.join(job.getInputFiles(), ",");
 			}
 
 			return job.getJobProperty(key);
 		} catch (final NoSuchJobException e) {
 			final BatchJob mpj = getUser().getBatchJobFromDatabase(jobname);
 			return mpj.getJobProperty(key);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getJobStatus(java.lang.String)
 	 */
 	public int getJobStatus(final String jobname) {
 
 		return getUser().getJobStatus(jobname);
 	}
 
 	public String getJsdlDocument(final String jobname)
 			throws NoSuchJobException {
 
 		final Job job = getUser().getJobFromDatabaseOrFileSystem(jobname);
 
 		String jsdlString;
 		jsdlString = SeveralXMLHelpers.toString(job.getJobDescription());
 
 		return jsdlString;
 
 	}
 
 	// public String getStagingFileSystem(String site) {
 	// return MountPointManager.getDefaultFileSystem(site);
 	// }
 
 	// abstract protected DtoStringList getSessionFqans();
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getMessagesSince(java.util.Date)
 	 */
 	public Document getMessagesSince(final Date date) {
 
 		// TODO
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getMountPointForUri(java.lang
 	 * .String)
 	 */
 	public MountPoint getMountPointForUri(final String uri) {
 
 		return getUser().getResponsibleMountpointForAbsoluteFile(uri);
 	}
 
 	protected Map<String, DtoActionStatus> getSessionActionStatus() {
 		return getUser().getActionStatuses();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getSite(java.lang.String)
 	 */
 	public String getSite(final String host_or_url) {
 
 		return informationManager.getSiteForHostOrUrl(host_or_url);
 
 	}
 
 	/**
 	 * Returns the name of the site for the given submissionLocation.
 	 * 
 	 * @param subLoc
 	 *            the submissionLocation
 	 * @return the name of the site for the submissionLocation or null, if the
 	 *         site can't be found
 	 */
 	public String getSiteForSubmissionLocation(final String subLoc) {
 
 		// subLoc = queuename@cluster:contactstring#JobManager
 		// String queueName = subLoc.substring(0, subLoc.indexOf(":"));
 		String contactString = "";
 		if (subLoc.indexOf("#") > 0) {
 			contactString = subLoc.substring(subLoc.indexOf(":") + 1,
 					subLoc.indexOf("#"));
 		} else {
 			contactString = subLoc.substring(subLoc.indexOf(":") + 1);
 		}
 
 		return getSite(contactString);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seeorg.vpac.grisu.control.ServiceInterface#
 	 * getStagingFileSystemForSubmissionLocation(java.lang.String)
 	 */
 	public DtoStringList getStagingFileSystemForSubmissionLocation(
 			final String subLoc) {
 		return DtoStringList.fromStringArray(informationManager
 				.getStagingFileSystemForSubmissionLocation(subLoc));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getSubmissionLocationsForApplication
 	 * (java.lang.String)
 	 */
 	public DtoSubmissionLocations getSubmissionLocationsForApplication(
 			final String application) {
 
 		return DtoSubmissionLocations
 				.createSubmissionLocationsInfo(informationManager
 						.getAllSubmissionLocationsForApplication(application));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getSubmissionLocationsForApplication
 	 * (java.lang.String, java.lang.String)
 	 */
 	public DtoSubmissionLocations getSubmissionLocationsForApplicationAndVersion(
 			final String application, final String version) {
 
 		final String[] sls = informationManager.getAllSubmissionLocations(
 				application, version);
 
 		return DtoSubmissionLocations.createSubmissionLocationsInfo(sls);
 	}
 
 	// public UserDAO getUserDao() {
 	// return userdao;
 	// }
 
 	public DtoSubmissionLocations getSubmissionLocationsForApplicationAndVersionAndFqan(
 			final String application, final String version, final String fqan) {
 		// TODO implement a method which takes in fqan later on
 
 		return DtoSubmissionLocations
 				.createSubmissionLocationsInfo(informationManager
 						.getAllSubmissionLocations(application, version));
 	}
 
 	public DtoApplicationInfo getSubmissionLocationsPerVersionOfApplication(
 			final String application) {
 		// if (ServerPropertiesManager.getMDSenabled()) {
 		// myLogger.debug("Getting map of submissionlocations per version of application for: "
 		// + application);
 		final Map<String, String> appVersionMap = new HashMap<String, String>();
 		final String[] versions = informationManager
 				.getAllVersionsOfApplicationOnGrid(application);
 		for (int i = 0; (versions != null) && (i < versions.length); i++) {
 			String[] submitLocations = null;
 			try {
 				submitLocations = informationManager.getAllSubmissionLocations(
 						application, versions[i]);
 				if (submitLocations == null) {
 					myLogger.error("Couldn't find submission locations for application: \""
 							+ application
 							+ "\""
 							+ ", version \""
 							+ versions[i]
 									+ "\". Most likely the mds is not published correctly.");
 					continue;
 				}
 			} catch (final Exception e) {
 				myLogger.error("Couldn't find submission locations for application: \""
 						+ application
 						+ "\""
 						+ ", version \""
 						+ versions[i]
 								+ "\". Most likely the mds is not published correctly.");
 				continue;
 			}
 			final StringBuffer submitLoc = new StringBuffer();
 
 			if (submitLocations != null) {
 				for (int j = 0; j < submitLocations.length; j++) {
 					submitLoc.append(submitLocations[j]);
 					if (j < (submitLocations.length - 1)) {
 						submitLoc.append(",");
 					}
 				}
 			}
 			appVersionMap.put(versions[i], submitLoc.toString());
 		}
 		return DtoApplicationInfo.createApplicationInfo(application,
 				appVersionMap);
 	}
 
 	public DtoStringList getUsedApplications() {
 
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
 
 		return DtoStringList.fromStringColletion(apps);
 
 	}
 
 	public DtoStringList getUsedApplicationsBatch() {
 
 		List<BatchJob> jobs = null;
 		jobs = batchJobDao.findMultiPartJobByDN(getUser().getDn());
 
 		final Set<String> apps = new TreeSet<String>();
 
 		for (final BatchJob job : jobs) {
 			final String app = job
 					.getJobProperty(Constants.APPLICATIONNAME_KEY);
 			if (StringUtils.isNotBlank(app)) {
 				apps.add(app);
 			}
 		}
 
 		return DtoStringList.fromStringColletion(apps);
 
 	}
 
 	/**
 	 * Gets the user of the current session. Also connects the default
 	 * credential to it.
 	 * 
 	 * @return the user or null if user could not be created
 	 * @throws NoValidCredentialException
 	 *             if no valid credential could be found to create the user
 	 */
 
 	abstract protected User getUser();
 
 	public DtoProperties getUserProperties() {
 
 		return DtoProperties.createProperties(getUser().getUserProperties());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getUserProperty(java.lang.String)
 	 */
 	public String getUserProperty(final String key) {
 
 		final String value = getUser().getUserProperties().get(key);
 
 		return value;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getVersionsOfApplicationOnSite
 	 * (java.lang.String, java.lang.String)
 	 */
 	public String[] getVersionsOfApplicationOnSite(final String application,
 			final String site) {
 
 		return informationManager.getVersionsOfApplicationOnSite(application,
 				site);
 
 	}
 
 	public DtoStringList getVersionsOfApplicationOnSubmissionLocation(
 			final String application, final String submissionLocation) {
 		return DtoStringList.fromStringArray(informationManager
 				.getVersionsOfApplicationOnSubmissionLocation(application,
 						submissionLocation));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#isFolder(java.lang.String)
 	 */
 	public boolean isFolder(final String file) throws RemoteFileSystemException {
 
 		return getUser().getFileSystemManager().isFolder(file);
 
 	}
 
 	/**
 	 * Tests whether the provided String is a valid submissionLocation. All this
 	 * does at the moment is to check whether there is a ":" within the string,
 	 * so don't depend with your life on the answer to this question...
 	 * 
 	 * @param submissionLocation
 	 *            the submission location
 	 * @return whether the string is a submission location or not
 	 */
 	public boolean isSubmissionLocation(final String submissionLocation) {
 
 		if (submissionLocation.indexOf(":") >= 0) {
 			return true;
 		} else {
 			return false;
 		}
 
 	}
 
 	private boolean isValidSubmissionLocation(String subLoc, String fqan) {
 
 		// TODO i'm sure this can be made much more quicker
 		final String[] fs = informationManager
 				.getStagingFileSystemForSubmissionLocation(subLoc);
 
 		for (final MountPoint mp : getUser().df(fqan)) {
 
 			for (final String f : fs) {
 				if (mp.getRootUrl().startsWith(f.replace(":2811", ""))) {
 
 					return true;
 				}
 			}
 
 		}
 
 		return false;
 
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
 	protected int kill(final Job job) {
 
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
 
 		final ProxyCredential cred = job.getCredential();
 		boolean changedCred = false;
 		// TODO check whether cred is stored in the database in that case?
 		if ((cred == null) || !cred.isValid()) {
 			job.setCredential(getUser().getCred());
 			changedCred = true;
 		}
 
 		new_status = getUser().getSubmissionManager().killJob(job);
 
 		job.addLogMessage("Job killed.");
 		getUser().addLogMessageToPossibleMultiPartJobParent(job,
 				"Job: " + job.getJobname() + " killed, new status: ");
 
 		if (changedCred) {
 			job.setCredential(null);
 		}
 		if (old_status != new_status) {
 			job.setStatus(new_status);
 		}
 		job.addLogMessage("New job status: "
 				+ JobConstants.translateStatus(new_status));
 		getUser().addLogMessageToPossibleMultiPartJobParent(
 				job,
 				"Job: " + job.getJobname() + " killed, new status: "
 						+ JobConstants.translateStatus(new_status));
 		jobdao.saveOrUpdate(job);
 		// myLogger.debug("Status of job: " + job.getJobname() + " is: "
 		// + new_status);
 
 		return new_status;
 	}
 
 	private void kill(final Job job, final boolean removeFromDB,
 			final boolean delteJobDirectory) {
 
 		// Job job;
 		//
 		// job = jobdao.findJobByDN(getUser().getDn(), jobname);
 
 		kill(job);
 
 		if (delteJobDirectory) {
 
 			if (job.isBatchJob()) {
 
 				try {
 					final BatchJob mpj = getUser().getBatchJobFromDatabase(
 							job.getJobProperty(Constants.BATCHJOB_NAME));
 					mpj.removeJob(job);
 					batchJobDao.saveOrUpdate(mpj);
 				} catch (final Exception e) {
 					// e.printStackTrace();
 					// doesn't matter
 				}
 
 			}
 
 			if (job.getJobProperty(Constants.JOBDIRECTORY_KEY) != null) {
 
 				try {
 					// myLogger.debug("Deleting jobdir for " + job.getJobname()
 					// + " in thread " + Thread.currentThread().getName());
 					deleteFile(job.getJobProperty(Constants.JOBDIRECTORY_KEY));
 					// myLogger.debug("Deleting success for jobdir for "
 					// + job.getJobname() + " in thread "
 					// + Thread.currentThread().getName());
 					// FileObject jobDir = getUser().aquireFile(
 					// job.getJobProperty(Constants.JOBDIRECTORY_KEY));
 					// jobDir.delete(new AllFileSelector());
 					// jobDir.delete();
 				} catch (final Exception e) {
 					// myLogger.debug("Deleting NOT success for jobdir for "
 					// + job.getJobname() + " in thread "
 					// + Thread.currentThread().getName() + ": "
 					// + e.getLocalizedMessage());
 					// throw new RemoteFileSystemException(
 					// "Could not delete jobdirectory: " + e.getMessage());
 					// myLogger.error(Thread.currentThread().getName());
 					myLogger.error("Could not delete jobdirectory: "
 							+ e.getMessage()
 							+ " Deleting job anyway and don't throw an exception.");
 				}
 			}
 		}
 
 		if (removeFromDB) {
 			jobdao.delete(job);
 			// X.p("Deleted from db.");
 		}
 
 	}
 
 	public void kill(final String jobname, final boolean clear)
 			throws RemoteFileSystemException, NoSuchJobException,
 			BatchJobException {
 
 		try {
 			Job job;
 
 			job = jobdao.findJobByDN(getUser().getDn(), jobname);
 
 			if (clear) {
 				kill(job, true, true);
 			} else {
 				kill(job, false, false);
 			}
 
 		} catch (final NoSuchJobException nsje) {
 			final BatchJob mpj = getUser().getBatchJobFromDatabase(jobname);
 			deleteMultiPartJob(mpj, clear);
 		}
 	}
 
 	public String killJobs(final DtoStringList jobnames, final boolean clear) {
 
 		if ((jobnames == null) || (jobnames.asArray().length == 0)) {
 			return null;
 		}
 
 		final String handle = UUID.randomUUID().toString();
 		final DtoActionStatus status = new DtoActionStatus(
 				handle,
 				jobnames.asArray().length * 2);
		getSessionActionStatus().put(handle, status);
 
 		Thread killThread = new Thread() {
 			@Override
 			public void run() {
 
 				for (final String jobname : jobnames.asArray()) {
 					status.addElement("Killing job " + jobname + "...");
 					try {
 						kill(jobname, clear);
 						status.addElement("Success.");
 					} catch (final Exception e) {
 						status.addElement("Failed: " + e.getLocalizedMessage());
 						status.setFailed(true);
 						status.setErrorCause(e.getLocalizedMessage());
 						myLogger.error("Could not kill job: " + jobname);
 					}
 				}
 
 				status.setFinished(true);
 			}
 		};
 		killThread.start();
 
 		return handle;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#lastModified(java.lang.String)
 	 */
 	public long lastModified(final String url) throws RemoteFileSystemException {
 
 		return getUser().getFileSystemManager().lastModified(url);
 	}
 
 	public GridFile ls(final String directory, int recursion_level)
 			throws RemoteFileSystemException {
 
 		// check whether credential still valid
 		getCredential();
 
 		return getUser().ls(directory, recursion_level);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#mkdir(java.lang.String)
 	 */
 	public boolean mkdir(final String url) throws RemoteFileSystemException {
 
 		// myLogger.debug("Creating folder: " + url + "...");
 		return getUser().getFileSystemManager().createFolder(url);
 
 	}
 
 	public MountPoint mount(final String url, final String mountpoint,
 			String fqan, final boolean useHomeDirectory)
 					throws RemoteFileSystemException {
 		// myLogger.debug("Mounting: " + url + " to: " + mountpoint
 		// + " with fqan: " + fqan);
 		if (fqan == null) {
 			fqan = Constants.NON_VO_FQAN;
 		}
 		final MountPoint mp = getUser().mountFileSystem(url, mountpoint, fqan,
 				useHomeDirectory, informationManager.getSiteForHostOrUrl(url));
 		getUser().resetMountPoints();
 		return mp;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#mount(java.lang.String,
 	 * java.lang.String)
 	 */
 	public MountPoint mountWithoutFqan(final String url,
 			final String mountpoint, final boolean useHomeDirectory)
 					throws RemoteFileSystemException {
 
 		final MountPoint mp = getUser().mountFileSystem(url, mountpoint,
 				useHomeDirectory, informationManager.getSiteForHostOrUrl(url));
 		getUser().resetMountPoints();
 		return mp;
 	}
 
 	private Map<String, Integer> optimizeMultiPartJob(final SubmitPolicy sp,
 			final String distributionMethod,
 			final BatchJob possibleParentBatchJob) throws NoSuchJobException,
 			JobPropertiesException {
 
 		JobDistributor jd;
 
 		if (Constants.DISTRIBUTION_METHOD_PERCENTAGE.equals(distributionMethod)) {
 			jd = new PercentageJobDistributor();
 		} else {
 			jd = new EqualJobDistributor();
 		}
 
 		final Map<String, Integer> results = jd.distributeJobs(
 				sp.getCalculatedJobs(), sp.getCalculatedGridResources());
 		final StringBuffer message = new StringBuffer(
 				"Filled submissionlocations for "
 						+ sp.getCalculatedJobs().size() + " jobs: " + "\n");
 		message.append("Submitted jobs to:\t\t\tAmount\n");
 		for (final String sl : results.keySet()) {
 			message.append(sl + "\t\t\t\t" + results.get(sl) + "\n");
 		}
 		myLogger.debug(message.toString());
 
 		final ExecutorService executor = Executors
 				.newFixedThreadPool(ServerPropertiesManager
 						.getConcurrentMultiPartJobSubmitThreadsPerUser());
 
 		final List<Exception> ex = Collections
 				.synchronizedList(new ArrayList<Exception>());
 
 		for (final Job job : sp.getCalculatedJobs()) {
 
 			final Thread thread = new Thread() {
 				@Override
 				public void run() {
 					try {
 						if (job.getStatus() > JobConstants.READY_TO_SUBMIT) {
 							try {
 								kill(job);
 							} catch (final Exception e) {
 								myLogger.error(e);
 							}
 							job.setStatus(JobConstants.READY_TO_SUBMIT);
 						}
 
 						if (Constants.NO_VERSION_INDICATOR_STRING
 								.equals(possibleParentBatchJob
 										.getJobProperty(Constants.APPLICATIONVERSION_KEY))) {
 							JsdlHelpers.setApplicationVersion(
 									job.getJobDescription(),
 									Constants.NO_VERSION_INDICATOR_STRING);
 						}
 
 						processJobDescription(job, possibleParentBatchJob);
 						jobdao.saveOrUpdate(job);
 					} catch (final JobPropertiesException e) {
 						ex.add(e);
 						executor.shutdownNow();
 						jobdao.saveOrUpdate(job);
 					} catch (final NoSuchJobException e) {
 						ex.add(e);
 						executor.shutdownNow();
 						jobdao.saveOrUpdate(job);
 					}
 				}
 			};
 
 			executor.execute(thread);
 		}
 
 		executor.shutdown();
 
 		try {
 			executor.awaitTermination(10 * 3600, TimeUnit.SECONDS);
 		} catch (final InterruptedException e) {
 			executor.shutdownNow();
 			Thread.currentThread().interrupt();
 			return null;
 		}
 
 		if (ex.size() > 0) {
 			throw new JobPropertiesException(
 					"Couldn't prepare at least one job: "
 							+ ex.get(0).getLocalizedMessage());
 		}
 
 		if (possibleParentBatchJob != null) {
 			possibleParentBatchJob.recalculateAllUsedMountPoints();
 			batchJobDao.saveOrUpdate(possibleParentBatchJob);
 		}
 
 		return results;
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
 
 		final String jobDir = JsdlHelpers.getAbsoluteWorkingDirectoryUrl(job
 				.getJobDescription());
 
 		// myLogger.debug("Using calculated jobdirectory: " + jobDir);
 
 		// job.setJob_directory(jobDir);
 
 		getUser().getFileSystemManager().createFolder(jobDir);
 
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
 	private void processJobDescription(final Job job, final BatchJob parentJob)
 			throws NoSuchJobException, JobPropertiesException {
 
 		// TODO check whether fqan is set
 		final String jobFqan = job.getFqan();
 		final Document jsdl = job.getJobDescription();
 
 		String oldJobDir = job.getJobProperty(Constants.JOBDIRECTORY_KEY);
 
 		try {
 			if (StringUtils.isNotBlank(oldJobDir)) {
 
 				if (fileExists(oldJobDir)) {
 
 					final GridFile fol = ls(oldJobDir, 1);
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
 
 			final String[] apps = informationManager
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
 				.getApplication())) {
 
 			submissionLocation = jobSubmissionObject.getSubmissionLocation();
 			if (StringUtils.isBlank(submissionLocation)
 					|| Constants.NO_SUBMISSION_LOCATION_INDICATOR_STRING
 					.equals(submissionLocation)) {
 				throw new JobPropertiesException(
 						JobSubmissionProperty.SUBMISSIONLOCATION.toString()
 						+ ": "
 						+ "No submission location specified. Since application is of type \"generic\" Grisu can't auto-calculate one. Please either specify package or submissionn location.");
 			}
 			stagingFileSystems = informationManager
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
 			if (StringUtils.isBlank(jobSubmissionObject.getApplication())) {
 				myLogger.debug("No application specified. Trying to calculate it...");
 
 				final String[] calculatedApps = informationManager
 						.getApplicationsThatProvideExecutable(JsdlHelpers
 								.getPosixApplicationExecutable(jsdl));
 				for (final String app : calculatedApps) {
 					jobSubmissionObject.setApplication(app);
 					matchingResources = matchmaker.findAllResources(
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
 
 					String version = jobSubmissionObject
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
 				matchingResources = matchmaker.findAllResources(
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
 
 				stagingFileSystems = informationManager
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
 									} catch (Exception e) {
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
 						stagingFileSystems = informationManager
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
 						stagingFileSystems = informationManager
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
 		final String submissionSite = informationManager
 				.getSiteForHostOrUrl(SubmissionLocationHelpers
 						.extractHost(submissionLocation));
 		myLogger.debug("Calculated submissionSite: " + submissionSite);
 		job.addJobProperty(Constants.SUBMISSION_SITE_KEY, submissionSite);
 		final String queue = SubmissionLocationHelpers
 				.extractQueue(submissionLocation);
 		job.addJobProperty(Constants.QUEUE_KEY, queue);
 		String newJobdir = stagingFilesystemToUse + workingDirectory;
 
 		try {
 			mkdir(newJobdir);
 		} catch (RemoteFileSystemException e1) {
 			throw new JobPropertiesException(
 					"Could not create new jobdirectory " + newJobdir
 					+ " (using VO: " + jobFqan + "): " + e1);
 		}
 
 		job.addJobProperty(Constants.JOBDIRECTORY_KEY, newJobdir);
 		myLogger.debug("Calculated jobdirectory: " + stagingFilesystemToUse
 				+ workingDirectory);
 
 		job.addJobProperty(Constants.SUBMISSIONBACKEND_KEY, getBackendInfo());
 
 		if (StringUtils.isNotBlank(oldJobDir)) {
 			try {
 				// if old jobdir exists, try to move it here
 				cpSingleFile(oldJobDir, newJobdir, true, true, true);
 
 				deleteFile(oldJobDir);
 			} catch (final Exception e) {
 				myLogger.error(e);
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
 
 	public String redistributeBatchJob(String batchJobname)
 			throws NoSuchJobException, JobPropertiesException {
 
 		final BatchJob job = getUser().getBatchJobFromDatabase(batchJobname);
 
 		if ((getSessionActionStatus().get(batchJobname) != null)
 				&& !getSessionActionStatus().get(batchJobname).isFinished()) {
 
 			// System.out
 			// .println("Submission: "
 			// + actionStatus.get(batchJobname)
 			// .getCurrentElements() + " / "
 			// + actionStatus.get(batchJobname).getTotalElements());
 
 			// we don't want to interfere with a possible ongoing jobsubmission
 			// myLogger.debug("not redistributing job because jobsubmission is still ongoing.");
 			throw new JobPropertiesException(
 					"Job submission is still ongoing in background.");
 		}
 
 		final String handleName = Constants.REDISTRIBUTE + batchJobname;
 
 		final DtoActionStatus status = new DtoActionStatus(handleName, 2);
 		getSessionActionStatus().put(handleName, status);
 
 		new Thread() {
 			@Override
 			public void run() {
 
 				status.addElement("Calculating redistribution...");
 				try {
 					final SortedSet<GridResource> resourcesToUse = calculateResourcesToUse(job);
 
 					final SubmitPolicy sp = new DefaultSubmitPolicy(
 							job.getJobs(), resourcesToUse, null);
 
 					final Map<String, Integer> results = optimizeMultiPartJob(
 							sp,
 							job.getJobProperty(Constants.DISTRIBUTION_METHOD),
 							job);
 
 					final StringBuffer optimizationResult = new StringBuffer();
 					for (final String subLoc : results.keySet()) {
 						optimizationResult.append(subLoc + " : "
 								+ results.get(subLoc) + "\n");
 					}
 					status.addLogMessage(optimizationResult.toString());
 					job.addJobProperty(Constants.BATCHJOB_OPTIMIZATION_RESULT,
 							optimizationResult.toString());
 					batchJobDao.saveOrUpdate(job);
 					status.addElement("Finished.");
 					status.setFinished(true);
 
 				} catch (final Exception e) {
 					status.setFailed(true);
 					status.setErrorCause(e.getLocalizedMessage());
 					status.setFinished(true);
 					status.addElement("Failed: " + e.getLocalizedMessage());
 				}
 
 			}
 		}.start();
 
 		return handleName;
 
 	}
 
 	public String refreshBatchJobStatus(String batchJobname)
 			throws NoSuchJobException {
 
 		final String handle = REFRESH_STATUS_PREFIX + batchJobname;
 
 		final DtoActionStatus status = getSessionActionStatus().get(handle);
 
 		if ((status != null) && !status.isFinished()) {
 			// refresh in progress. Just give back the handle
 			return handle;
 		}
 
 		final BatchJob multiPartJob = getUser().getBatchJobFromDatabase(
 				batchJobname);
 
 		final DtoActionStatus statusfinal = new DtoActionStatus(handle,
 				multiPartJob.getJobs().size());
 
 		getSessionActionStatus().put(handle, statusfinal);
 
 		final ExecutorService executor = Executors
 				.newFixedThreadPool(ServerPropertiesManager
 						.getConcurrentJobStatusThreadsPerUser());
 
 		final Job[] currentJobs = multiPartJob.getJobs().toArray(new Job[] {});
 
 		if (currentJobs.length == 0) {
 			multiPartJob.setStatus(JobConstants.JOB_CREATED);
 			batchJobDao.saveOrUpdate(multiPartJob);
 			statusfinal.addLogMessage("No jobs. Returning.");
 			statusfinal.setFailed(false);
 			statusfinal.setFinished(true);
 			return handle;
 		}
 
 		Arrays.sort(currentJobs);
 
 		for (final Job job : currentJobs) {
 			final Thread thread = new Thread() {
 				@Override
 				public void run() {
 					statusfinal.addLogMessage("Refreshing job "
 							+ job.getJobname());
 					getJobStatus(job.getJobname());
 					statusfinal.addElement("Job status for job "
 							+ job.getJobname() + " refreshed.");
 
 					if (statusfinal.getTotalElements() <= statusfinal
 							.getCurrentElements()) {
 						statusfinal.setFinished(true);
 						if (multiPartJob.getFailedJobs().size() > 0) {
 							statusfinal.setFailed(true);
 							statusfinal
 							.setErrorCause("Undefined error: not all subjobs accessed.");
 							multiPartJob.setStatus(JobConstants.FAILED);
 						} else {
 							multiPartJob.setStatus(JobConstants.DONE);
 						}
 						batchJobDao.saveOrUpdate(multiPartJob);
 					}
 				}
 			};
 			executor.execute(thread);
 		}
 		executor.shutdown();
 
 		return handle;
 
 	}
 
 	/**
 	 * Removes the specified job from the mulitpartJob.
 	 * 
 	 * @param batchJobname
 	 *            the batchJobname
 	 * @param jobname
 	 *            the jobname
 	 */
 	public void removeJobFromBatchJob(String batchJobname, String jobname)
 			throws NoSuchJobException {
 
 		final Job job = getUser().getJobFromDatabaseOrFileSystem(jobname);
 		final BatchJob multiJob = getUser().getBatchJobFromDatabase(
 				batchJobname);
 		multiJob.removeJob(job);
 
 		batchJobDao.saveOrUpdate(multiJob);
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
 
 	public DtoProperties restartBatchJob(final String batchJobname,
 			String restartPolicy, DtoProperties properties)
 					throws NoSuchJobException, JobPropertiesException {
 
 		final BatchJob job = getUser().getBatchJobFromDatabase(batchJobname);
 
 		if ((getSessionActionStatus().get(batchJobname) != null)
 				&& !getSessionActionStatus().get(batchJobname).isFinished()) {
 
 			// System.out
 			// .println("Submission: "
 			// + actionStatus.get(batchJobname)
 			// .getCurrentElements() + " / "
 			// + actionStatus.get(batchJobname).getTotalElements());
 
 			// we don't want to interfere with a possible ongoing jobsubmission
 			// myLogger.debug("not restarting job because jobsubmission is still ongoing.");
 			throw new JobPropertiesException(
 					"Job submission is still ongoing in background.");
 		}
 
 		final DtoActionStatus status = new DtoActionStatus(batchJobname, 3);
 		getSessionActionStatus().put(batchJobname, status);
 
 		status.addElement("Finding resources to use...");
 		final SortedSet resourcesToUse = calculateResourcesToUse(job);
 
 		status.addElement("Investigating batchjob...");
 		if (properties == null) {
 			properties = DtoProperties
 					.createProperties(new HashMap<String, String>());
 		}
 
 		SubmitPolicy sp = null;
 
 		if (Constants.SUBMIT_POLICY_RESTART_DEFAULT.equals(restartPolicy)) {
 			sp = new DefaultResubmitSubmitPolicy(job.getJobs(), resourcesToUse,
 					properties.propertiesAsMap());
 		} else if (Constants.SUBMIT_POLICY_RESTART_SPECIFIC_JOBS
 				.equals(restartPolicy)) {
 			sp = new RestartSpecificJobsRestartPolicy(job.getJobs(),
 					resourcesToUse, properties.propertiesAsMap());
 		} else {
 			throw new JobPropertiesException("Restart policy \""
 					+ restartPolicy + "\" not supported.");
 		}
 
 		if ((sp.getCalculatedGridResources().size() == 0)
 				|| (sp.getCalculatedJobs().size() == 0)) {
 
 			status.addElement("No locations or no jobs to submit found. Doing nothing...");
 			status.setFinished(true);
 			// nothing we can do...
 			return DtoProperties
 					.createProperties(new HashMap<String, String>());
 		} else {
 			status.setTotalElements(3 + (sp.getCalculatedJobs().size() * 2));
 			status.addLogMessage("Found " + sp.getCalculatedJobs().size()
 					+ " jobs to resubmit.");
 		}
 
 		status.addElement("Optimizing job distribution...");
 		final Map<String, Integer> results = optimizeMultiPartJob(sp,
 				job.getJobProperty(Constants.DISTRIBUTION_METHOD), job);
 
 		batchJobDao.saveOrUpdate(job);
 
 		final ExecutorService executor = Executors
 				.newFixedThreadPool(ServerPropertiesManager
 						.getConcurrentMultiPartJobSubmitThreadsPerUser());
 
 		for (final Job jobToRestart : sp.getCalculatedJobs()) {
 
 			final Thread thread = new Thread() {
 				@Override
 				public void run() {
 					try {
 						status.addElement("Starting resubmission of job: "
 								+ jobToRestart.getJobname());
 						restartJob(jobToRestart, null);
 						status.addElement("Resubmission of job "
 								+ jobToRestart.getJobname() + " successful.");
 					} catch (final JobSubmissionException e) {
 						status.addElement("Resubmission of job "
 								+ jobToRestart.getJobname() + " failed: "
 								+ e.getLocalizedMessage());
 						status.setFailed(true);
 						status.setErrorCause(e.getLocalizedMessage());
 						myLogger.debug(e);
 					} catch (final NoSuchJobException e1) {
 						status.addElement("Resubmission of job "
 								+ jobToRestart.getJobname() + " failed: "
 								+ e1.getLocalizedMessage());
 						status.setFailed(true);
 						myLogger.debug(e1);
 					}
 
 					if (status.getTotalElements() <= status
 							.getCurrentElements()) {
 						status.setFinished(true);
 					}
 				}
 			};
 			executor.execute(thread);
 		}
 
 		executor.shutdown();
 
 		return DtoProperties.createUserPropertiesIntegerValue(results);
 
 	}
 
 	private void restartJob(final Job job, String changedJsdl)
 			throws JobSubmissionException, NoSuchJobException {
 
 		DtoActionStatus status = null;
 		status = new DtoActionStatus(job.getJobname(), 5);
 		getSessionActionStatus().put(job.getJobname(), status);
 
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
 			mpj = getUser().getBatchJobFromDatabase(possibleMultiPartJob);
 			getUser().addLogMessageToPossibleMultiPartJobParent(job,
 					"Re-submitting job " + job.getJobname());
 			mpj.removeFailedJob(job.getJobname());
 			batchJobDao.saveOrUpdate(mpj);
 		}
 
 		if (StringUtils.isNotBlank(changedJsdl)) {
 			status.addElement("Changing job description...");
 			job.addLogMessage("Changing job properties...");
 			Document newJsdl;
 			final Document oldJsdl = job.getJobDescription();
 
 			try {
 				newJsdl = SeveralXMLHelpers.fromString(changedJsdl);
 			} catch (final Exception e3) {
 
 				myLogger.error(e3);
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
 				+ getDN());
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
 
 		final Job job = getUser().getJobFromDatabaseOrFileSystem(jobname);
 
 		restartJob(job, changedJsdl);
 	}
 
 	public void setDebugProperties(Map<String, String> props) {
 
 		for ( String key : props.keySet() ) {
 
 			if ( "submitProxyLifetime".equals(key) ) {
 				int lt = -1;
 				try {
 					lt = Integer.parseInt(props.get(key));
 					SUBMIT_PROXY_LIFETIME = lt;
 				} catch (NumberFormatException e) {
 					SUBMIT_PROXY_LIFETIME = -1;
 				}
 			}
 
 		}
 
 	}
 
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see
 	// * grisu.control.ServiceInterface#getApplicationDetails(java.lang
 	// * .String, java.lang.String)
 	// */
 	// public DtoApplicationDetails getApplicationDetailsForSubmissionLocation(
 	// final String application, final String site_or_submissionLocation) {
 	//
 	// String site = site_or_submissionLocation;
 	// if (isSubmissionLocation(site_or_submissionLocation)) {
 	// myLogger.debug("Parameter " + site_or_submissionLocation
 	// + "is submission location not site. Calculating site...");
 	// site = getSiteForSubmissionLocation(site_or_submissionLocation);
 	// myLogger.debug("Site is: " + site);
 	// }
 	//
 	// return getApplicationDetailsForVersionAndSite(application,
 	// getDefaultVersionForApplicationAtSite(application, site), site);
 	//
 	// }
 
 	public void setUserProperty(String key, String value) {
 
 		getUser().setUserProperty(key, value);
 
 	}
 
 	private void setVO(final Job job, String fqan) throws NoSuchJobException,
 	JobPropertiesException {
 
 		if (fqan == null) {
 			fqan = Constants.NON_VO_FQAN;
 		}
 		job.setFqan(fqan);
 		job.addJobProperty(Constants.FQAN_KEY, fqan);
 
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
 
 		final List<Element> stageIns = JsdlHelpers.getStageInElements(job
 				.getJobDescription());
 
 		for (final Element stageIn : stageIns) {
 
 			final String sourceUrl = JsdlHelpers.getStageInSource(stageIn);
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
 
 				// try {
 				// getUser().getFileSystemManager().createFolder(
 				// FileManager.calculateParentUrl(targetUrl));
 				//
 				// } catch (final RemoteFileSystemException e) {
 				// if (optionalStatus != null) {
 				// optionalStatus
 				// .addLogMessage("Error while staging in files: "
 				// + e.getLocalizedMessage());
 				// }
 				// throw e;
 				// }
 				myLogger.debug("Staging file: " + sourceUrl + " to: "
 						+ targetUrl);
 				job.addInputFile(sourceUrl);
 				jobdao.saveOrUpdate(job);
 				cpSingleFile(sourceUrl, targetUrl, true, true, true);
 				// job.addInputFile(targetUrl);
 			}
 			// }
 		}
 	}
 
 	private void submitBatchJob(final BatchJob multiJob)
 			throws JobSubmissionException, NoSuchJobException {
 
 		final DtoActionStatus newActionStatus = new DtoActionStatus(
 				multiJob.getBatchJobname(), 100);
 		this.getSessionActionStatus().put(multiJob.getBatchJobname(),
 				newActionStatus);
 
 		final ExecutorService executor = Executors
 				.newFixedThreadPool(ServerPropertiesManager
 						.getConcurrentMultiPartJobSubmitThreadsPerUser());
 
 		final Job[] currentlyCreatedJobs = multiJob.getJobs().toArray(
 				new Job[] {});
 		Arrays.sort(currentlyCreatedJobs);
 
 		final int totalNumberOfJobs = currentlyCreatedJobs.length;
 		newActionStatus.setTotalElements(totalNumberOfJobs);
 
 		for (final Job job : currentlyCreatedJobs) {
 
 			if (job.getStatus() != JobConstants.READY_TO_SUBMIT) {
 				continue;
 			}
 			final Thread thread = new Thread() {
 				@Override
 				public void run() {
 
 					Exception exc = null;
 					for (int i = 0; i < DEFAULT_JOB_SUBMISSION_RETRIES; i++) {
 						try {
 							exc = null;
 
 							DtoActionStatus status = null;
 							status = new DtoActionStatus(job.getJobname(), 0);
 							getSessionActionStatus().put(job.getJobname(),
 									status);
 
 							submitJob(job, true, status);
 							newActionStatus.addElement("Added job: "
 									+ job.getJobname());
 
 							break;
 						} catch (final Exception e) {
 							myLogger.error("Job submission for multipartjob: "
 									+ multiJob.getBatchJobname() + ", "
 									+ job.getJobname() + " failed: "
 									+ e.getLocalizedMessage());
 							myLogger.error("Trying again...");
 							newActionStatus
 							.addLogMessage("Failed to submit job "
 									+ job.getJobname() + ": "
 									+ e.getLocalizedMessage()
 									+ ". Trying again...");
 							exc = e;
 							executor.shutdownNow();
 						}
 					}
 
 					if (exc != null) {
 						newActionStatus.setFailed(true);
 						newActionStatus.setErrorCause("Tried to resubmit job "
 								+ job.getJobname() + " "
 								+ DEFAULT_JOB_SUBMISSION_RETRIES
 								+ " times. Never worked. Giving up...");
 						myLogger.error("Tried to resubmit job "
 								+ job.getJobname() + " "
 								+ DEFAULT_JOB_SUBMISSION_RETRIES
 								+ " times. Never worked. Giving up...");
 						multiJob.addFailedJob(job.getJobname());
 						batchJobDao.saveOrUpdate(multiJob);
 						newActionStatus.addElement("Tried to resubmit job "
 								+ job.getJobname() + " "
 								+ DEFAULT_JOB_SUBMISSION_RETRIES
 								+ " times. Never worked. Giving up...");
 						executor.shutdownNow();
 
 					}
 
 					if (newActionStatus.getCurrentElements() >= newActionStatus
 							.getTotalElements()) {
 						newActionStatus.setFinished(true);
 						multiJob.setStatus(JobConstants.ACTIVE);
 						batchJobDao.saveOrUpdate(multiJob);
 					}
 
 				}
 			};
 
 			executor.execute(thread);
 		}
 		executor.shutdown();
 
 	}
 
 	private void submitJob(final Job job, boolean stageFiles,
 			DtoActionStatus status) throws JobSubmissionException {
 
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
 
 			getUser().addLogMessageToPossibleMultiPartJobParent(job,
 					"Starting job submission for job: " + job.getJobname());
 			prepareJobEnvironment(job);
 			if (stageFiles) {
 				status.addLogMessage("Starting file stage-in.");
 				job.addLogMessage("Staging possible input files.");
 				// myLogger.debug("Staging possible input files...");
 				stageFiles(job, status);
 				job.addLogMessage("File staging finished.");
 				status.addLogMessage("File stage-in finished.");
 			}
 		} catch (final Exception e) {
 			status.setFailed(true);
 			status.setErrorCause(e.getLocalizedMessage());
 			status.setFinished(true);
 			myLogger.error(e);
 			throw new JobSubmissionException(
 					"Could not access remote filesystem: "
 							+ e.getLocalizedMessage());
 		}
 
 		status.addElement("Setting credential...");
 		if (job.getFqan() != null) {
 
 			try {
 				if (SUBMIT_PROXY_LIFETIME <= 0) {
 					job.setCredential(getUser().getCred(job.getFqan()));
 				} else {
 					job.setCredential(getCredential(job.getFqan(),
 							SUBMIT_PROXY_LIFETIME));
 				}
 			} catch (final Exception e) {
 				status.setFailed(true);
 				status.setErrorCause(e.getLocalizedMessage());
 				status.setFinished(true);
 				myLogger.error(e);
 				throw new JobSubmissionException(
 						"Could not create credential to use to submit the job: "
 								+ e.getLocalizedMessage());
 			}
 		} else {
 			job.addLogMessage("Setting non-vo credential: " + job.getFqan());
 			job.setCredential(getCredential());
 		}
 
 		String handle = null;
 		myLogger.debug("Submitting job to endpoint...");
 
 		try {
 			status.addElement("Starting job submission using GT4...");
 			job.addLogMessage("Submitting job to endpoint...");
 			final String candidate = JsdlHelpers.getCandidateHosts(job
 					.getJobDescription())[0];
 			final GridResource resource = informationManager
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
 				handle = getUser().getSubmissionManager().submit(
 						submissionType, job);
 			} catch (ServerJobSubmissionException e) {
 
 				status.addLogMessage("Job submission failed on server.");
 				status.setFailed(true);
 				status.setFinished(true);
 				status.setErrorCause(e.getLocalizedMessage());
 				job.addLogMessage("Submission to endpoint failed: "
 						+ e.getLocalizedMessage());
 				getUser().addLogMessageToPossibleMultiPartJobParent(
 						job,
 						"Job submission for job: " + job.getJobname()
 						+ " failed: " + e.getLocalizedMessage());
 				throw new JobSubmissionException(
 						"Submission to endpoint failed: "
 								+ e.getLocalizedMessage());
 			}
 
 			job.addLogMessage("Submission finished.");
 		} catch (final RuntimeException e) {
 			// e.printStackTrace();
 			status.addLogMessage("Job submission failed.");
 			status.setFailed(true);
 			status.setFinished(true);
 			job.addLogMessage("Submission to endpoint failed: "
 					+ e.getLocalizedMessage());
 			getUser().addLogMessageToPossibleMultiPartJobParent(
 					job,
 					"Job submission for job: " + job.getJobname() + " failed: "
 							+ e.getLocalizedMessage());
 			myLogger.error(e);
 			throw new JobSubmissionException(
 					"Job submission to endpoint failed: "
 							+ e.getLocalizedMessage(), e);
 		}
 
 		if (handle == null) {
 			status.addLogMessage("Submission finished but no jobhandle...");
 			status.setFailed(true);
 			status.setErrorCause("No jobhandle");
 			status.setFinished(true);
 			job.addLogMessage("Submission finished but jobhandle is null...");
 			getUser().addLogMessageToPossibleMultiPartJobParent(
 					job,
 					"Job submission for job: " + job.getJobname()
 					+ " finished but jobhandle is null...");
 			throw new JobSubmissionException(
 					"Job apparently submitted but jobhandle is null for job: "
 							+ job.getJobname());
 		}
 
 		try {
 
 			job.addJobProperty(Constants.SUBMISSION_TIME_KEY,
 					Long.toString(new Date().getTime()));
 
 			// we don't want the credential to be stored with the job in this case
 			// TODO or do we want it to be stored?
 			job.setCredential(null);
 			job.addLogMessage("Job submission finished successful.");
 
 			getUser().addLogMessageToPossibleMultiPartJobParent(
 					job,
 					"Job submission for job: " + job.getJobname()
 					+ " finished successful.");
 			jobdao.saveOrUpdate(job);
 
 			myLogger.info("Jobsubmission for job " + job.getJobname()
 					+ " and user " + getDN() + " successful.");
 
 			status.addElement("Job submission finished...");
 			status.setFinished(true);
 		} catch (Exception e) {
 			status.addLogMessage("Submission finished, error in wrap-up...");
 			status.setFailed(true);
 			status.setFinished(true);
 			status.setErrorCause(e.getLocalizedMessage());
 			job.addLogMessage("Submission finished, error in wrap-up...");
 			getUser().addLogMessageToPossibleMultiPartJobParent(
 					job,
 					"Job submission for job: " + job.getJobname()
 					+ " finished but error in wrap-up...");
 			throw new JobSubmissionException(
 					"Job apparently submitted but error in wrap-up for job: "
 							+ job.getJobname());
 		}
 	}
 
 	public void submitJob(final String jobname) throws JobSubmissionException,
 	NoSuchJobException {
 
 		final DtoActionStatus status = new DtoActionStatus(jobname, 0);
 		getSessionActionStatus().put(jobname, status);
 
 		try {
 			final Job job = getUser().getJobFromDatabaseOrFileSystem(jobname);
 			if (job.getStatus() > JobConstants.READY_TO_SUBMIT) {
 				throw new JobSubmissionException("Job already submitted.");
 			}
 			new Thread() {
 				@Override
 				public void run() {
 					try {
 						submitJob(job, true, status);
 					} catch (Exception e) {
 						myLogger.error(e);
 					}
 				}
 			}.start();
 
 		} catch (final NoSuchJobException e) {
 			// maybe it's a multipartjob
 			final BatchJob multiJob = getUser()
 					.getBatchJobFromDatabase(jobname);
 
 			new Thread() {
 				@Override
 				public void run() {
 					try {
 						submitBatchJob(multiJob);
 					} catch (JobSubmissionException e) {
 						myLogger.error(e);
 					} catch (NoSuchJobException e) {
 						myLogger.error(e);
 					}
 				}
 			}.start();
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#submitSupportRequest(java.lang
 	 * .String, java.lang.String)
 	 */
 	public void submitSupportRequest(final String subject,
 			final String description) {
 
 		// TODO
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#umount(java.lang.String)
 	 */
 	public void umount(final String mountpoint) {
 
 		getUser().unmountFileSystem(mountpoint);
 
 		getUser().resetMountPoints();
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#upload(javax.activation.DataSource ,
 	 * java.lang.String)
 	 */
 	public String upload(final DataHandler source, final String filename)
 			throws RemoteFileSystemException {
 
 		return getUser().getFileSystemManager().upload(source, filename);
 
 	}
 
 	public void uploadInputFile(String jobname, final DataHandler source,
 			final String targetFilename) throws NoSuchJobException,
 			RemoteFileSystemException {
 
 		// Thread.dumpStack();
 
 		try {
 			final Job job = getUser().getJobFromDatabaseOrFileSystem(jobname);
 
 			// try whether job is single or multi
 			final DtoActionStatus status = new DtoActionStatus(targetFilename,
 					1);
 			getSessionActionStatus().put(targetFilename, status);
 
 			// new Thread() {
 			// @Override
 			// public void run() {
 
 			final String jobdir = job
 					.getJobProperty(Constants.JOBDIRECTORY_KEY);
 
 			try {
 				final String tarFileName = jobdir + "/" + targetFilename;
 				upload(source, tarFileName);
 				status.addElement("Upload to " + tarFileName + " successful.");
 				job.addInputFile(tarFileName);
 				jobdao.saveOrUpdate(job);
 
 				status.setFinished(true);
 			} catch (final RemoteFileSystemException e) {
 				myLogger.error(e);
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
 		}
 
 		final BatchJob multiJob = getUser().getBatchJobFromDatabase(jobname);
 
 		multiJob.setStatus(JobConstants.INPUT_FILES_UPLOADING);
 		batchJobDao.saveOrUpdate(multiJob);
 
 		final String relpathFromMountPointRoot = multiJob
 				.getJobProperty(Constants.RELATIVE_BATCHJOB_DIRECTORY_KEY);
 
 		Set<String> urls = new HashSet<String>();
 
 		for (final String mountPointRoot : multiJob.getAllUsedMountPoints()) {
 
 			final String parent = mountPointRoot + "/"
 					+ relpathFromMountPointRoot;
 			urls.add(parent);
 		}
 
 		final DtoActionStatus status = new DtoActionStatus(targetFilename,
 				multiJob.getAllUsedMountPoints().size());
 		getSessionActionStatus().put(targetFilename, status);
 
 		getUser().getFileSystemManager().uploadFileToMultipleLocations(urls,
 				source, targetFilename, status);
 
 		// TODO monitor status and set jobstatus to ready_to_submit?
 
 	}
 
 }
