 package org.vpac.grisu.control.serviceInterfaces;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.Collection;
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
 import java.util.UUID;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.vfs.AllFileSelector;
 import org.apache.commons.vfs.FileContent;
 import org.apache.commons.vfs.FileObject;
 import org.apache.commons.vfs.FileSystemException;
 import org.apache.commons.vfs.FileType;
 import org.apache.commons.vfs.FileTypeSelector;
 import org.apache.log4j.Logger;
 import org.vpac.grisu.backend.hibernate.BatchJobDAO;
 import org.vpac.grisu.backend.hibernate.JobDAO;
 import org.vpac.grisu.backend.hibernate.UserDAO;
 import org.vpac.grisu.backend.model.ProxyCredential;
 import org.vpac.grisu.backend.model.RemoteFileTransferObject;
 import org.vpac.grisu.backend.model.User;
 import org.vpac.grisu.backend.model.job.BatchJob;
 import org.vpac.grisu.backend.model.job.Job;
 import org.vpac.grisu.backend.model.job.JobSubmissionManager;
 import org.vpac.grisu.backend.model.job.JobSubmitter;
 import org.vpac.grisu.backend.model.job.gt4.GT4DummySubmitter;
 import org.vpac.grisu.backend.model.job.gt4.GT4Submitter;
 import org.vpac.grisu.backend.utils.CertHelpers;
 import org.vpac.grisu.backend.utils.FileContentDataSourceConnector;
 import org.vpac.grisu.backend.utils.FileSystemStructureToXMLConverter;
 import org.vpac.grisu.control.JobConstants;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.BatchJobException;
 import org.vpac.grisu.control.exceptions.JobPropertiesException;
 import org.vpac.grisu.control.exceptions.JobSubmissionException;
 import org.vpac.grisu.control.exceptions.NoSuchJobException;
 import org.vpac.grisu.control.exceptions.NoValidCredentialException;
 import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
 import org.vpac.grisu.control.info.CachedMdsInformationManager;
 import org.vpac.grisu.model.MountPoint;
 import org.vpac.grisu.model.dto.DtoActionStatus;
 import org.vpac.grisu.model.dto.DtoApplicationDetails;
 import org.vpac.grisu.model.dto.DtoApplicationInfo;
 import org.vpac.grisu.model.dto.DtoBatchJob;
 import org.vpac.grisu.model.dto.DtoDataLocations;
 import org.vpac.grisu.model.dto.DtoFile;
 import org.vpac.grisu.model.dto.DtoFolder;
 import org.vpac.grisu.model.dto.DtoGridResources;
 import org.vpac.grisu.model.dto.DtoHostsInfo;
 import org.vpac.grisu.model.dto.DtoJob;
 import org.vpac.grisu.model.dto.DtoJobProperty;
 import org.vpac.grisu.model.dto.DtoJobs;
 import org.vpac.grisu.model.dto.DtoMountPoints;
 import org.vpac.grisu.model.dto.DtoProperties;
 import org.vpac.grisu.model.dto.DtoStringList;
 import org.vpac.grisu.model.dto.DtoSubmissionLocations;
 import org.vpac.grisu.model.job.JobSubmissionObjectImpl;
 import org.vpac.grisu.settings.Environment;
 import org.vpac.grisu.settings.ServerPropertiesManager;
 import org.vpac.grisu.utils.FileHelpers;
 import org.vpac.grisu.utils.SeveralXMLHelpers;
 import org.vpac.security.light.voms.VO;
 import org.vpac.security.light.voms.VOManagement.VOManagement;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import au.org.arcs.grid.grisu.matchmaker.MatchMakerImpl;
 import au.org.arcs.grid.sched.MatchMaker;
 import au.org.arcs.jcommons.constants.Constants;
 import au.org.arcs.jcommons.constants.JobSubmissionProperty;
 import au.org.arcs.jcommons.interfaces.GridResource;
 import au.org.arcs.jcommons.interfaces.InformationManager;
 import au.org.arcs.jcommons.utils.JsdlHelpers;
 import au.org.arcs.jcommons.utils.SubmissionLocationHelpers;
 
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
 
 	private boolean INCLUDE_MULTIPARTJOBS_IN_PS_COMMAND = true;
 
 	static final Logger myLogger = Logger
 			.getLogger(AbstractServiceInterface.class.getName());
 
 	private InformationManager informationManager = CachedMdsInformationManager
 			.getDefaultCachedMdsInformationManager(Environment
 					.getGrisuDirectory().toString());
 
 	public static final int DEFAULT_JOB_SUBMISSION_RETRIES = 5;
 
 	private UserDAO userdao = new UserDAO();
 
 	protected JobDAO jobdao = new JobDAO();
 
 	protected BatchJobDAO multiPartJobDao = new BatchJobDAO();
 
 	private MountPoint[] mountPointsForThisSession = null;
 
 	private JobSubmissionManager manager = null;
 
 	private User user = null;
 
 	private String[] currentFqans = null;
 
 	private FileSystemStructureToXMLConverter fsconverter = null;
 
 	private MatchMaker matchmaker = new MatchMakerImpl(Environment
 			.getGrisuDirectory().toString());
 
 	private final Map<String, DtoActionStatus> actionStatus = Collections
 			.synchronizedMap(new HashMap<String, DtoActionStatus>());
 
 	public String getInterfaceVersion() {
 		return ServiceInterface.INTERFACE_VERSION;
 	}
 
 	// private Map<String, RemoteFileTransferObject> fileTransfers = new
 	// HashMap<String, RemoteFileTransferObject>();
 
 	/**
 	 * Gets the user of the current session. Also connects the default
 	 * credential to it.
 	 * 
 	 * @return the user or null if user could not be created
 	 * @throws NoValidCredentialException
 	 *             if no valid credential could be found to create the user
 	 */
 	protected final synchronized User getUser() {
 
 		// make sure there is a valid credential
 		if (getCredential() == null || !getCredential().isValid()) {
 			throw new NoValidCredentialException(
 					"No valid credential exists in this session for user: "
 							+ user);
 		}
 
 		// if ( getCredential())
 
 		if (user == null) {
 			// try to look up user in the database
 			user = userdao.findUserByDN(getCredential().getDn());
 
 			if (user == null) {
 				user = new User(getCredential());
 				userdao.saveOrUpdate(user);
 			} else {
 				user.setCred(getCredential());
 			}
 			getFsConverter();
 			df();
 		} else {
 			user.setCred(getCredential());
 		}
 
 		return user;
 	}
 
 	/**
 	 * Just a helper method to convert the filesystem structure to xml. It may
 	 * make sense to replace that with JSON objects in the future.
 	 * 
 	 * @return the converter
 	 */
 	private FileSystemStructureToXMLConverter getFsConverter() {
 		if (fsconverter == null) {
 			fsconverter = new FileSystemStructureToXMLConverter(getUser());
 		}
 		return fsconverter;
 	}
 
 	/**
 	 * Searches for the job with the specified jobname for the current user.
 	 * 
 	 * @param jobname
 	 *            the name of the job (which is unique within one user)
 	 * @return the job
 	 */
 	protected Job getJob(final String jobname) throws NoSuchJobException {
 
 		Job job = jobdao.findJobByDN(getUser().getCred().getDn(), jobname);
 		return job;
 	}
 
 	protected BatchJob getMultiPartJobFromDatabase(final String batchJobname)
 			throws NoSuchJobException {
 
 		BatchJob job = multiPartJobDao.findJobByDN(getUser().getCred().getDn(),
 				batchJobname);
 
 		return job;
 
 	}
 
 	/**
 	 * Creates a new {@link JobSubmissionManager} if it does not exist jet. The
 	 * JobSubmissionManager holds all the possible {@link JobSubmitter}s in a
 	 * HashMap. We only use GT4 (for now).
 	 * 
 	 * @return the JobSubmissionManager
 	 */
 	protected JobSubmissionManager getSubmissionManager() {
 		if (manager == null) {
 			Map<String, JobSubmitter> submitters = new HashMap<String, JobSubmitter>();
 			submitters.put("GT4", new GT4Submitter());
 			submitters.put("GT4Dummy", new GT4DummySubmitter());
 			manager = new JobSubmissionManager(this.informationManager,
 					submitters);
 		}
 		return manager;
 	}
 
 	public String createJob(String jsdlString, final String fqan,
 			final String jobnameCreationMethod) throws JobPropertiesException {
 
 		Document jsdl;
 
 		try {
 			jsdl = SeveralXMLHelpers.fromString(jsdlString);
 		} catch (Exception e3) {
 
 			myLogger.error(e3);
 			throw new RuntimeException("Invalid jsdl/xml format.", e3);
 		}
 
 		return createJob(jsdl, fqan, jobnameCreationMethod, null);
 	}
 
 	private String calculateJobname(Document jsdl, String jobnameCreationMethod)
 			throws JobPropertiesException {
 
 		String jobname = JsdlHelpers.getJobname(jsdl);
 
 		if (jobnameCreationMethod == null
 				|| Constants.FORCE_NAME_METHOD.equals(jobnameCreationMethod)) {
 
 			if (jobname == null) {
 				throw new JobPropertiesException(
 						JobSubmissionProperty.JOBNAME.toString()
 								+ ": "
 								+ "Jobname not specified and job creation method is force-name.");
 			}
 
 			String[] allJobnames = getAllJobnames(null).asArray();
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
 
 			String[] allJobnames = getAllJobnames(null).asArray();
 			Arrays.sort(allJobnames);
 
 			String temp;
 			do {
 				String timestamp = new Long(new Date().getTime()).toString();
 				try {
 					Thread.sleep(1);
 				} catch (InterruptedException e) {
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
 
 		} else {
 			throw new JobPropertiesException(JobSubmissionProperty.JOBNAME
 					.toString()
 					+ ": "
 					+ "Jobname creation method "
 					+ jobnameCreationMethod
 					+ " not supported.");
 		}
 
 		if (jobname == null) {
 			throw new RuntimeException(
 					"Jobname is null. This should never happen. Please report to markus.binsteiner@arcs.org.au");
 		}
 
 		return jobname;
 
 	}
 
 	private String createJob(Document jsdl, final String fqan,
 			final String jobnameCreationMethod,
 			final BatchJob optionalParentBatchJob)
 			throws JobPropertiesException {
 
 		String jobname = calculateJobname(jsdl, jobnameCreationMethod);
 
 		try {
 			BatchJob mpj = getMultiPartJobFromDatabase(jobname);
 			throw new JobPropertiesException(
 					"Could not create job with jobname " + jobname
 							+ ". Multipart job with this id already exists...");
 		} catch (NoSuchJobException e) {
 			// that's good
 		}
 
 		Job job;
 		try {
 			myLogger.debug("Trying to get job that shouldn't exist...");
 			job = getJob(jobname);
 			throw new JobPropertiesException(JobSubmissionProperty.JOBNAME
 					.toString()
 					+ ": "
 					+ "Jobname \""
 					+ jobname
 					+ "\" already taken. Could not create job.");
 		} catch (NoSuchJobException e1) {
 			// that's ok
 			myLogger.debug("Checked jobname. Not yet in database. Good.");
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
 		} catch (NoSuchJobException e) {
 			// that should never happen
 			myLogger
 					.error("Somehow the job was not created although it certainly should have. Must be a bug..");
 			throw new RuntimeException("Job was not created. Internal error.");
 		} catch (Exception e) {
 			myLogger.error("Error when processing job description: "
 					+ e.getLocalizedMessage());
 			try {
 				jobdao.delete(job);
 				myLogger.debug("Deleted job " + jobname
 						+ " from database again.");
 			} catch (Exception e2) {
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
 
 	// public String createJobUsingMap(final DtoJob jobProperties,
 	// final String fqan, final String jobCreationMethod)
 	// throws JobPropertiesException {
 	//
 	// JobSubmissionObjectImpl jso = new JobSubmissionObjectImpl(jobProperties
 	// .propertiesAsMap());
 	//
 	// return createJob(jso.getJobDescriptionDocument(), fqan,
 	// jobCreationMethod);
 	// }
 
 	private void setVO(final Job job, String fqan) throws NoSuchJobException,
 			JobPropertiesException {
 
 		if (fqan == null) {
 			fqan = Constants.NON_VO_FQAN;
 		}
 		job.setFqan(fqan);
 		job.getJobProperties().put(Constants.FQAN_KEY, fqan);
 
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
 	private void processJobDescription(final Job job,
 			final BatchJob multiPartJob) throws NoSuchJobException,
 			JobPropertiesException {
 
 		// TODO check whether fqan is set
 		String jobFqan = job.getFqan();
 		Document jsdl = job.getJobDescription();
 
 		String oldJobDir = job.getJobProperty(Constants.JOBDIRECTORY_KEY);
 		
 		try {
 			if ( StringUtils.isNotBlank(oldJobDir) && ! fileExists(oldJobDir) ) {
 				oldJobDir = null;
 			} else {
 				myLogger.debug("Old jobdir exists.");
 			}
 		} catch (RemoteFileSystemException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		boolean applicationCalculated = false;
 
 		JobSubmissionObjectImpl jobSubmissionObject = new JobSubmissionObjectImpl(
 				jsdl);
 
 		if (jobSubmissionObject.getCommandline() == null) {
 			throw new JobPropertiesException("No commandline specified.");
 		}
 
 		for (JobSubmissionProperty key : jobSubmissionObject
 				.getJobSubmissionPropertyMap().keySet()) {
 			job.getJobProperties().put(key.toString(),
 					jobSubmissionObject.getJobSubmissionPropertyMap().get(key));
 		}
 
 		List<GridResource> matchingResources = null;
 
 		String submissionLocation = null;
 
 		// check whether application is "generic". If that is the case, just
 		// check
 		// if all the necessary fields are specified and then continue without
 		// any
 		// auto-settings
 
 		if (jobSubmissionObject.getApplication() == null) {
 
 			String commandline = jobSubmissionObject.getCommandline();
 
 			String[] apps = informationManager
 					.getApplicationsThatProvideExecutable(jobSubmissionObject
 							.extractExecutable());
 
 			if (apps == null || apps.length == 0) {
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
 
 		if (Constants.GENERIC_APPLICATION_NAME.equals(jobSubmissionObject
 				.getApplication())) {
 
 			submissionLocation = jobSubmissionObject.getSubmissionLocation();
 			if (StringUtils.isBlank(submissionLocation)) {
 				throw new JobPropertiesException(
 						JobSubmissionProperty.SUBMISSIONLOCATION.toString()
 								+ ": "
 								+ "No submission location specified. Since application is of type \"generic\" Grisu can't auto-calculate one.");
 			}
 
 			// check whether submissionlocation is valid
 			String[] allSubLocs = informationManager
 					.getAllSubmissionLocationsForVO(job.getFqan());
 			Arrays.sort(allSubLocs);
 			int i = Arrays.binarySearch(allSubLocs, submissionLocation);
 			if (i < 0) {
 				throw new JobPropertiesException(
 						JobSubmissionProperty.SUBMISSIONLOCATION.toString()
 								+ ": " + "Specified submissionlocation "
 								+ submissionLocation + " not valid for VO "
 								+ job.getFqan());
 			}
 
 			String[] modules = JsdlHelpers.getModules(jsdl);
 			if (modules == null || modules.length == 0) {
 				myLogger
 						.warn("No modules specified for generic application. That might be ok but probably not...");
 			} else {
 				job.addJobProperty(Constants.MODULES_KEY, StringUtils.join(
 						modules, ","));
 			}
 
 			// checking whether application is specified. If not, try to figure
 			// out
 			// from the executable
 		} else {
 			if (StringUtils.isBlank(jobSubmissionObject.getApplication())) {
 				myLogger
 						.debug("No application specified. Trying to calculate it...");
 
 				String[] calculatedApps = informationManager
 						.getApplicationsThatProvideExecutable(JsdlHelpers
 								.getPosixApplicationExecutable(jsdl));
 				for (String app : calculatedApps) {
 					jobSubmissionObject.setApplication(app);
 					matchingResources = matchmaker.findAllResources(
 							jobSubmissionObject.getJobSubmissionPropertyMap(),
 							job.getFqan());
 					if (matchingResources != null
 							&& matchingResources.size() > 0) {
 						JsdlHelpers.setApplicationName(jsdl, app);
 						myLogger.debug("Calculated app: " + app);
 						break;
 					}
 				}
 
 				if (jobSubmissionObject.getApplication() == null
 						|| jobSubmissionObject.getApplication().length() == 0) {
 					throw new JobPropertiesException(
 							JobSubmissionProperty.APPLICATIONNAME.toString()
 									+ ": "
 									+ "No application specified and could not find one in the grid that matches the executable.");
 				}
 
 				applicationCalculated = true;
 			} else {
 
 				myLogger.debug("Trying to find matching grid resources...");
 				matchingResources = matchmaker.findAvailableResources(
 						jobSubmissionObject.getJobSubmissionPropertyMap(), job
 								.getFqan());
 				if (matchingResources != null) {
 					myLogger.debug("Found: " + matchingResources.size()
 							+ " of them...");
 				}
 			}
 
 			submissionLocation = jobSubmissionObject.getSubmissionLocation();
 			// GridResource selectedSubmissionResource = null;
 
 			if (StringUtils.isNotBlank(submissionLocation)) {
 				myLogger
 						.debug("Submission location specified in jsdl: "
 								+ submissionLocation
 								+ ". Checking whether this is valid using mds information.");
 				// check whether submission location is specified. If so, check
 				// whether it is in the list of matching resources
 				boolean submissionLocationIsValid = false;
 				for (GridResource resource : matchingResources) {
 					if (submissionLocation.equals(SubmissionLocationHelpers
 							.createSubmissionLocationString(resource))) {
 						myLogger
 								.debug("Found gridResource object for submission location. Now checking whether version is specified and if it is whether it is available on this resource.");
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
 							myLogger
 									.debug("Specified version is not available on this grid resource: "
 											+ submissionLocation);
 							throw new JobPropertiesException(
 									JobSubmissionProperty.APPLICATIONVERSION
 											.toString()
 											+ ": "
 											+ "Version: "
 											+ jobSubmissionObject
 													.getApplicationVersion()
 											+ " not installed on "
 											+ submissionLocation);
 						}
 						myLogger.debug("Version available or not specified.");
 						// if no application version is specified, auto-set one
 						if (StringUtils.isBlank(jobSubmissionObject
 								.getApplicationVersion())
 								|| Constants.NO_VERSION_INDICATOR_STRING
 										.equals(jobSubmissionObject
 												.getApplicationVersion())) {
 							myLogger
 									.debug("version was not specified. Auto setting the first one for the selected resource.");
 							if (resource.getAvailableApplicationVersion() != null
 									&& resource
 											.getAvailableApplicationVersion()
 											.size() > 0) {
 								List<String> versionsAvail = resource
 										.getAvailableApplicationVersion();
 
 								JsdlHelpers.setApplicationVersion(jsdl,
 										versionsAvail.get(0));
 								myLogger
 										.debug("Set version to be: "
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
 												+ " on " + submissionLocation);
 							}
 						}
 						myLogger
 								.debug("Successfully validated submissionlocation "
 										+ submissionLocation);
 						submissionLocationIsValid = true;
 						// selectedSubmissionResource = resource;
 						break;
 					}
 				}
 
 				if (!submissionLocationIsValid) {
 					myLogger
 							.error("Could not find a matching grid resource object for submissionlocation: "
 									+ submissionLocation);
 					throw new JobPropertiesException(
 							JobSubmissionProperty.SUBMISSIONLOCATION.toString()
 									+ ": " + "Submissionlocation "
 									+ submissionLocation
 									+ " not available for this kind of job");
 				}
 			} else {
 				myLogger
 						.debug("No submission location specified in jsdl document. Trying to auto-find one...");
 				if (matchingResources == null || matchingResources.size() == 0) {
 					myLogger.error("No matching grid resources found.");
 					throw new JobPropertiesException(
 							JobSubmissionProperty.SUBMISSIONLOCATION.toString()
 									+ ": "
 									+ "Could not find any matching resource to run this kind of job on");
 				}
 				// find the best submissionlocation and set it.
 
 				// check for the version of the application to run
 				if (StringUtils.isBlank(jobSubmissionObject
 						.getApplicationVersion())
 						|| Constants.NO_VERSION_INDICATOR_STRING
 								.equals(jobSubmissionObject
 										.getApplicationVersion())) {
 					myLogger
 							.debug("No version specified in jsdl document. Will use the first one for the best grid resource.");
 					for (GridResource resource : matchingResources) {
 						if (resource.getAvailableApplicationVersion() != null
 								&& resource.getAvailableApplicationVersion()
 										.size() > 0) {
 							JsdlHelpers.setApplicationVersion(jsdl, resource
 									.getAvailableApplicationVersion().get(0));
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
 						myLogger
 								.error("Could not find any version of the specified application grid-wide.");
 						throw new JobPropertiesException(
 								JobSubmissionProperty.APPLICATIONVERSION
 										.toString()
 										+ ": "
 										+ "Could not find any version for this application grid-wide. That is probably an error in the mds info.");
 					}
 				} else {
 					myLogger
 							.debug("Version: "
 									+ jobSubmissionObject
 											.getApplicationVersion()
 									+ " specified. Trying to find a matching grid resource...");
 					for (GridResource resource : matchingResources) {
 						if (resource.getAvailableApplicationVersion().contains(
 								jobSubmissionObject.getApplicationVersion())) {
 							submissionLocation = SubmissionLocationHelpers
 									.createSubmissionLocationString(resource);
 							myLogger
 									.debug("Found grid resource with specified application version. Using submissionLocation: "
 											+ submissionLocation);
 							break;
 						}
 					}
 					if (submissionLocation == null) {
 						myLogger
 								.error("Could not find a grid resource with the specified version...");
 						throw new JobPropertiesException(
 								JobSubmissionProperty.APPLICATIONVERSION
 										.toString()
 										+ ": "
 										+ "Could not find desired version: "
 										+ jobSubmissionObject
 												.getApplicationVersion()
 										+ " for application "
 										+ jobSubmissionObject.getApplication()
 										+ " grid-wide.");
 					}
 				}
 
 				// selectedSubmissionResource = matchingResources.get(0);
 				// jobSubmissionObject.setSubmissionLocation(submissionLocation);
 				try {
 					JsdlHelpers.setCandidateHosts(jsdl,
 							new String[] { submissionLocation });
 				} catch (RuntimeException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					throw new JobPropertiesException(
 							JobSubmissionProperty.SUBMISSIONLOCATION.toString()
 									+ ": "
 									+ "Jsdl document malformed. No candidate hosts element.");
 				}
 			}
 		}
 
 		myLogger
 				.debug("Trying to find staging filesystem for subissionlocation: "
 						+ submissionLocation);
 		String[] stagingFileSystems = informationManager
 				.getStagingFileSystemForSubmissionLocation(submissionLocation);
 
 		if (stagingFileSystems == null || stagingFileSystems.length == 0) {
 			myLogger
 					.error("No staging filesystem found for submissionlocation: "
 							+ submissionLocation);
 			throw new JobPropertiesException(
 					JobSubmissionProperty.SUBMISSIONLOCATION.toString()
 							+ ": "
 							+ "Could not find staging filesystem for submissionlocation "
 							+ submissionLocation);
 		}
 
 		myLogger.debug("Trying to find mountpoint for stagingfilesystem...");
 
 		MountPoint mountPointToUse = null;
 		String stagingFilesystemToUse = null;
 		for (String stagingFs : stagingFileSystems) {
 
 			for (MountPoint mp : df_internal()) {
 				if (mp.getRootUrl().startsWith(stagingFs.replace(":2811", ""))
 						&& jobFqan.equals(mp.getFqan())) {
 					mountPointToUse = mp;
 					stagingFilesystemToUse = stagingFs.replace(":2811", "");
 					myLogger.debug("Found mountpoint " + mp.getAlias()
 							+ " for stagingfilesystem "
 							+ stagingFilesystemToUse);
 					break;
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
 			myLogger
 					.error("Could not find a staging filesystem that is accessible for the user for submissionlocation "
 							+ submissionLocation);
 			throw new JobPropertiesException(
 					JobSubmissionProperty.SUBMISSIONLOCATION.toString()
 							+ ": "
 							+ "Could not find stagingfilesystem for submission location: "
 							+ submissionLocation);
 		}
 
 		JsdlHelpers.addOrRetrieveExistingFileSystemElement(jsdl,
 				JsdlHelpers.LOCAL_EXECUTION_HOST_FILESYSTEM,
 				stagingFilesystemToUse);
 
 		// now calculate and set the proper paths
 		String workingDirectory;
 		if (multiPartJob == null) {
 			workingDirectory = mountPointToUse.getRootUrl().substring(
 					stagingFilesystemToUse.length())
 					+ "/"
 					+ ServerPropertiesManager.getGrisuJobDirectoryName()
 					+ "/" + job.getJobname();
 		} else {
 			workingDirectory = mountPointToUse.getRootUrl().substring(
 					stagingFilesystemToUse.length())
 					+ "/"
 					+ multiPartJob
 							.getJobProperty(Constants.RELATIVE_BATCHJOB_DIRECTORY_KEY)
 					+ "/" + job.getJobname();
 		}
 		myLogger.debug("Calculated workingdirectory: " + workingDirectory);
 
 		JsdlHelpers.setWorkingDirectory(jsdl,
 				JsdlHelpers.LOCAL_EXECUTION_HOST_FILESYSTEM, workingDirectory);
 		job.addJobProperty(Constants.MOUNTPOINT_KEY, mountPointToUse
 				.getRootUrl());
 		job.addJobProperty(Constants.STAGING_FILE_SYSTEM_KEY,
 				stagingFilesystemToUse);
 
 		job.addJobProperty(Constants.WORKINGDIRECTORY_KEY, workingDirectory);
 		String submissionSite = informationManager
 				.getSiteForHostOrUrl(SubmissionLocationHelpers
 						.extractHost(submissionLocation));
 		myLogger.debug("Calculated submissionSite: " + submissionSite);
 		job.addJobProperty(Constants.SUBMISSION_SITE_KEY, submissionSite);
 		// job.setJob_directory(stagingFilesystemToUse + workingDirectory);
 		job.getJobProperties().put(Constants.JOBDIRECTORY_KEY,
 				stagingFilesystemToUse + workingDirectory);
 		myLogger.debug("Calculated jobdirectory: " + stagingFilesystemToUse
 				+ workingDirectory);
 		
 		
 		if ( StringUtils.isNotBlank(oldJobDir) ) {
 		try {
 			// if old jobdir exists, try to move it here
 			cpSingleFile(oldJobDir, stagingFilesystemToUse+workingDirectory, true, true);
 			
 			deleteFile(oldJobDir);
 		} catch (Exception e) {
 			e.printStackTrace();
 			//TODO more
 		}
 		}
 		
 
 		myLogger.debug("Fixing urls in datastaging elements...");
 		// fix stage in target filesystems...
 		List<Element> stageInElements = JsdlHelpers.getStageInElements(jsdl);
 		for (Element stageInElement : stageInElements) {
 
 			String filePath = JsdlHelpers.getStageInSource(stageInElement);
 			if ("dummyfile".equals(filePath) || filePath.startsWith("file:")) {
 				continue;
 			}
 			String filename = filePath.substring(filePath.lastIndexOf("/"));
 
 			Element el = JsdlHelpers
 					.getStageInTarget_filesystemPart(stageInElement);
 
 			el.setTextContent(JsdlHelpers.LOCAL_EXECUTION_HOST_FILESYSTEM);
 			JsdlHelpers.getStageInTarget_relativePart(stageInElement)
 					.setTextContent(workingDirectory + filename);
 
 		}
 
 		job.setJobDescription(jsdl);
 		// jobdao.attachDirty(job);
 		myLogger.debug("Preparing job done.");
 	}
 
 	private SortedSet<GridResource> findBestResourcesForMultipartJob(
 			BatchJob mpj) {
 
 		Map<JobSubmissionProperty, String> properties = new HashMap<JobSubmissionProperty, String>();
 
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
 		properties.put(JobSubmissionProperty.NO_CPUS, mpj
 				.getJobProperty(Constants.NO_CPUS_KEY));
 
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
 			for (Job job : mpj.getJobs()) {
 				int wt = new Integer(job
 						.getJobProperty(Constants.WALLTIME_IN_MINUTES_KEY));
 				if (mwt < wt) {
 					mwt = wt;
 				}
 			}
 			maxWalltime = new Integer(mwt).toString();
 		}
 
 		properties.put(JobSubmissionProperty.WALLTIME_IN_MINUTES, maxWalltime);
 
 		SortedSet<GridResource> result = new TreeSet<GridResource>(matchmaker
 				.findAvailableResources(properties, mpj.getFqan()));
 
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
 
 	public DtoProperties redistributeBatchJob(String batchJobname)
 			throws NoSuchJobException {
 
 		BatchJob job = getMultiPartJobFromDatabase(batchJobname);
 		
 		Map<GridResource, Integer> resourcesToUse = null;
 		if ( job.getResourcesToUse() == null ) {
 			resourcesToUse = calculateResourcesToUse(job);
 			job.setResourcesToUse(resourcesToUse);
 		} else { 
 			resourcesToUse = job.getResourcesToUse();
 		}
 		SubmitPolicy sp = new SubmitPolicy(job.getJobs(), new TreeSet<GridResource>(resourcesToUse.keySet()));
 
 		Map<String, Integer> results = optimizeMultiPartJob(sp, job.getJobProperty(Constants.DISTRIBUTION_METHOD), job);
 
 		return DtoProperties.createUserPropertiesIntegerValue(results);
 
 	}
 
 	private boolean isValidSubmissionLocation(String subLoc, String fqan) {
 
 		// TODO i'm sure this can be made much more quicker
 		String[] fs = informationManager
 				.getStagingFileSystemForSubmissionLocation(subLoc);
 
 		for (MountPoint mp : df(fqan)) {
 
 			for (String f : fs) {
 				if (mp.getRootUrl().startsWith(f.replace(":2811", ""))) {
 
 					return true;
 				}
 			}
 
 		}
 
 		return false;
 
 	}
 
 	private Map<GridResource, Integer> calculateResourcesToUse(BatchJob mpj) {
 
 		String locationsToIncludeString = mpj
 				.getJobProperty(Constants.LOCATIONS_TO_INCLUDE_KEY);
 		String[] locationsToInclude = null;
 		if (StringUtils.isNotBlank(locationsToIncludeString)) {
 			locationsToInclude = locationsToIncludeString.split(",");
 		}
 
 		String locationsToExcludeString = mpj
 				.getJobProperty(Constants.LOCATIONS_TO_EXCLUDE_KEY);
 		String[] locationsToExclude = null;
 		if (StringUtils.isNotBlank(locationsToExcludeString)) {
 			locationsToExclude = locationsToExcludeString.split(",");
 		}
 
 		Map<GridResource, Integer> resourcesToUse = new TreeMap<GridResource, Integer>();
 
 		for (GridResource resource : findBestResourcesForMultipartJob(mpj)) {
 
 			String tempSubLocString = SubmissionLocationHelpers.createSubmissionLocationString(resource);
 			
 			if (locationsToInclude != null && locationsToInclude.length > 0) {
 
 				for (String subLoc : locationsToInclude) {
 					if (tempSubLocString.toLowerCase().contains(
 							subLoc.toLowerCase())) {
 						if (isValidSubmissionLocation(tempSubLocString, mpj
 								.getFqan())) {
 							resourcesToUse.put(resource, 0);
 						}
 						break;
 					}
 				}
 
 			} else if (locationsToExclude != null && locationsToExclude.length > 0) {
 
 				boolean useSubLoc = true;
 				for (String subLoc : locationsToExclude) {
 					if (tempSubLocString.toLowerCase().contains(
 							subLoc.toLowerCase())) {
 						useSubLoc = false;
 						break;
 					}
 				}
 				if (useSubLoc) {
 					if (isValidSubmissionLocation(tempSubLocString, mpj
 							.getFqan())) {
 						resourcesToUse.put(resource, 0);
 					}
 				}
 
 			} else {
 
 				if (isValidSubmissionLocation(tempSubLocString, mpj
 						.getFqan())) {
 					resourcesToUse.put(resource, 0);
 				}
 			}
 		}
 
 		return resourcesToUse;
 
 	}
 
 	private Map<String, Integer> optimizeMultiPartJob(SubmitPolicy sp, String distributionMethod, BatchJob possibleParentBatchJob)
 			throws NoSuchJobException {
 		
 		JobDistributor jd;
 
 		if (Constants.DISTRIBUTION_METHOD_EQUAL.equals(distributionMethod)) {
 			jd = new EqualJobDistributor();
 		} else {
 			jd = new PercentageJobDistributor();
 		}
 		
 		
 		Map<String, Integer> results = jd.distributeJobs(sp.getCalculatedJobs(), sp.getCalculatedGridResources());
 		StringBuffer message = new StringBuffer(
 				"Filled submissionlocations for " + results.size() + " jobs: "
 						+ "\n");
 		message.append("Submitted jobs to:\t\t\tAmount\n");
 		for (String sl : results.keySet()) {
 			message.append(sl + "\t\t\t\t" + results.get(sl) + "\n");
 		}
 		myLogger.debug(message.toString());
 
 		// System.out.println("Message length: "+message.length());
 
 		// mpj.addJobProperty(Constants.OPTIMIZE_STATS, message.toString());
 
 		for (Job job : sp.getCalculatedJobs()) {
 			try {
 
 				if (Constants.NO_VERSION_INDICATOR_STRING.equals(possibleParentBatchJob
 						.getJobProperty(Constants.APPLICATIONVERSION_KEY))) {
 					JsdlHelpers.setApplicationVersion(job.getJobDescription(),
 							Constants.NO_VERSION_INDICATOR_STRING);
 				}
 
 				processJobDescription(job, possibleParentBatchJob);
 			} catch (JobPropertiesException e) {
 				e.printStackTrace();
 				throw new RuntimeException(e);
 			}
 			jobdao.saveOrUpdate(job);
 		}
 		if ( possibleParentBatchJob != null ) {
 			possibleParentBatchJob.recalculateAllUsedMountPoints();
 		multiPartJobDao.saveOrUpdate(possibleParentBatchJob);
 		}
 
 		return results;
 	}
 
 	private void submitMultiPartJob(final BatchJob multiJob)
 			throws JobSubmissionException, NoSuchJobException {
 
 		final DtoActionStatus newActionStatus = new DtoActionStatus(multiJob
 				.getBatchJobname(), 100);
 		this.actionStatus.put(multiJob.getBatchJobname(), newActionStatus);
 
 		ExecutorService executor = Executors
 				.newFixedThreadPool(ServerPropertiesManager
 						.getConcurrentMultiPartJobSubmitThreadsPerUser());
 
 		Job[] currentlyCreatedJobs = multiJob.getJobs().toArray(new Job[] {});
 		Arrays.sort(currentlyCreatedJobs);
 
 		final int totalNumberOfJobs = currentlyCreatedJobs.length;
 		newActionStatus.setTotalElements(totalNumberOfJobs);
 
 		for (final Job job : currentlyCreatedJobs) {
 
 			if (job.getStatus() != JobConstants.READY_TO_SUBMIT) {
 				continue;
 			}
 			Thread thread = new Thread() {
 				public void run() {
 					Exception exc = null;
 					for (int i = 0; i < DEFAULT_JOB_SUBMISSION_RETRIES; i++) {
 						try {
 							exc = null;
 
 							DtoActionStatus status = null;
 							status = new DtoActionStatus(job.getJobname(), 0);
 							actionStatus.put(job.getJobname(), status);
 
 							submitJob(job, true, status);
 							newActionStatus.addElement("Added job: "
 									+ job.getJobname());
 
 							break;
 						} catch (Exception e) {
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
 						}
 
 						if (exc != null) {
 							newActionStatus.setFailed(true);
 							myLogger.error("Tried to resubmit job "
 									+ job.getJobname() + " "
 									+ DEFAULT_JOB_SUBMISSION_RETRIES
 									+ " times. Never worked. Giving up...");
 							multiJob.addFailedJob(job.getJobname());
 							newActionStatus.addElement("Tried to resubmit job "
 									+ job.getJobname() + " "
 									+ DEFAULT_JOB_SUBMISSION_RETRIES
 									+ " times. Never worked. Giving up...");
 						}
 
 						if (newActionStatus.getCurrentElements() == newActionStatus
 								.getTotalElements()) {
 							newActionStatus.setFinished(true);
 						}
 
 					}
 				}
 			};
 			// just to get a better chance that the jobs are submitted in the
 			// right order...
 			try {
 				Thread.sleep(500);
 			} catch (InterruptedException e) {
 				myLogger.error(e);
 			}
 			executor.execute(thread);
 		}
 		executor.shutdown();
 
 		// if (waitForSubmissionsToFinish) {
 		//
 		// try {
 		// executor.awaitTermination(3600 * 24, TimeUnit.SECONDS);
 		// } catch (InterruptedException e) {
 		// // TODO Auto-generated catch block
 		// e.printStackTrace();
 		// throw new RuntimeException(e);
 		// }
 		// }
 		//
 		// if (failedJobs.size() > 0) {
 		// throw new JobSubmissionException(
 		// "Not all job submissions successful. Failed jobs: "
 		// + StringUtils.join(failedJobs, ", "));
 		// }
 
 	}
 
 	private synchronized void addLogMessageToPossibleMultiPartJobParent(
 			Job job, String message) {
 
 		String mpjName = job.getJobProperty(Constants.BATCHJOB_NAME);
 
 		if (mpjName != null) {
 			BatchJob mpj = null;
 			try {
 				mpj = getMultiPartJobFromDatabase(mpjName);
 			} catch (NoSuchJobException e) {
 				myLogger.error(e);
 				return;
 			}
 			mpj.addLogMessage(message);
 			multiPartJobDao.saveOrUpdate(mpj);
 		}
 	}
 
 	private void submitJob(final Job job, boolean stageFiles,
 			DtoActionStatus status) throws JobSubmissionException {
 
 		try {
 
 			int noStageins = 0;
 
 			if (stageFiles) {
 				List<Element> stageIns = JsdlHelpers.getStageInElements(job
 						.getJobDescription());
 				noStageins = stageIns.size();
 			}
 
 			status.setTotalElements(status.getTotalElements() + 4 + noStageins);
 
 			myLogger.debug("Preparing job environment...");
 			job.addLogMessage("Preparing job environment.");
 
 			status.addElement("Preparing job environment...");
 
 			addLogMessageToPossibleMultiPartJobParent(job,
 					"Starting job submission for job: " + job.getJobname());
 			prepareJobEnvironment(job);
 			if (stageFiles) {
 				status.addLogMessage("Starting file stage-in.");
 				job.addLogMessage("Staging possible input files.");
 				myLogger.debug("Staging possible input files...");
 				stageFiles(job, status);
 				job.addLogMessage("File staging finished.");
 				status.addLogMessage("File stage-in finished.");
 			}
 		} catch (Exception e) {
 			status.setFailed(true);
 			status.setFinished(true);
 			e.printStackTrace();
 			throw new JobSubmissionException(
 					"Could not access remote filesystem: "
 							+ e.getLocalizedMessage());
 		}
 
 		status.addElement("Setting credential...");
 		if (job.getFqan() != null) {
 			VO vo = VOManagement.getVO(getUser().getFqans().get(job.getFqan()));
 			try {
 				job.setCredential(CertHelpers.getVOProxyCredential(vo, job
 						.getFqan(), getCredential()));
 			} catch (Exception e) {
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
 			handle = getSubmissionManager().submit("GT4", job);
 			job.addLogMessage("Submission finished.");
 		} catch (RuntimeException e) {
 			status.addLogMessage("Job submission failed.");
 			status.setFailed(true);
 			status.setFinished(true);
 			job.addLogMessage("Submission to endpoint failed: "
 					+ e.getLocalizedMessage());
 			addLogMessageToPossibleMultiPartJobParent(job,
 					"Job submission for job: " + job.getJobname() + " failed: "
 							+ e.getLocalizedMessage());
 			e.printStackTrace();
 			throw new JobSubmissionException(
 					"Job submission to endpoint failed: "
 							+ e.getLocalizedMessage());
 		}
 
 		if (handle == null) {
 			status.addLogMessage("Submission finished but no jobhandle...");
 			status.setFailed(true);
 			status.setFinished(true);
 			job.addLogMessage("Submission finished but jobhandle is null...");
 			addLogMessageToPossibleMultiPartJobParent(job,
 					"Job submission for job: " + job.getJobname()
 							+ " finished but jobhandle is null...");
 			throw new JobSubmissionException(
 					"Job apparently submitted but jobhandle is null for job: "
 							+ job.getJobname());
 		}
 
 		job.addJobProperty(Constants.SUBMISSION_TIME_KEY, Long
 				.toString(new Date().getTime()));
 
 		// we don't want the credential to be stored with the job in this case
 		// TODO or do we want it to be stored?
 		job.setCredential(null);
 		job.addLogMessage("Job submission finished successful.");
 		addLogMessageToPossibleMultiPartJobParent(job,
 				"Job submission for job: " + job.getJobname()
 						+ " finished successful.");
 		jobdao.saveOrUpdate(job);
 		myLogger.info("Jobsubmission for job " + job.getJobname()
 				+ " and user " + getDN() + " successful.");
 
 		status.addElement("Job submission finished...");
 		status.setFinished(true);
 	}
 	
 	
 	public void restartBatchJob(final String batchJobname, String restartPolicy, DtoProperties properties) throws NoSuchJobException {
 		
 		BatchJob job = getMultiPartJobFromDatabase(batchJobname);
 		
 		Map<GridResource, Integer> resourcesToUse = null;
 		if ( job.getResourcesToUse() == null ) {
 			resourcesToUse = calculateResourcesToUse(job);
 			job.setResourcesToUse(resourcesToUse);
 		} else { 
 			resourcesToUse = job.getResourcesToUse();
 		}
 		SubmitPolicy sp = new SubmitPolicy(job.getJobs(), new TreeSet<GridResource>(resourcesToUse.keySet()));
 		sp.setSubmitToAllLocations(false);
 		
 		Map<String, Integer> results = optimizeMultiPartJob(sp, job.getJobProperty(Constants.DISTRIBUTION_METHOD), job);
 		
 		for ( Job jobToRestart : sp.getCalculatedJobs() ) {
 			try {
 				restartJob(jobToRestart, null);
 			} catch (JobSubmissionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		
 		
 		
 		
 	}
 	
 	public void restartJob(final String jobname, String changedJsdl)
 	throws JobSubmissionException, NoSuchJobException {
 		
 		Job job = getJob(jobname);
 
 		restartJob(job, changedJsdl);
 	}
 	
 	
 //	private void moveJobDirectory(Job job, String newSubLoc, BatchJob possibleBatchJob) throws NoSuchJobException, JobPropertiesException, RemoteFileSystemException {
 //		
 //
 //		String oldJobDir = job.getJobProperty(Constants.JOBDIRECTORY_KEY); 
 //		JsdlHelpers.setCandidateHosts(job.getJobDescription(), new String[]{newSubLoc});
 //		
 //
 //		processJobDescription(job, possibleBatchJob);
 //		
 //		String newJobDir = job.getJobProperty(Constants.JOBDIRECTORY_KEY);
 //		myLogger.debug("New jobdirectory: "+ newJobDir);
 //		
 //		
 //		cpSingleFile(oldJobDir, newJobDir, true, true);
 //		myLogger.debug("Moved old job dir from "+oldJobDir+" to "+newJobDir);
 //
 //		try {
 //			// try to delete old file
 //			deleteFile(oldJobDir);
 //		} catch (RemoteFileSystemException e) {
 //			// not good, but acceptable
 //			myLogger.error("Could not delete old jobdir.", e);
 //		}
 //
 //		jobdao.saveOrUpdate(job);
 //		
 //	}
 
 	private void restartJob(final Job job, String changedJsdl)
 			throws JobSubmissionException, NoSuchJobException {
 
 
 		DtoActionStatus status = null;
 		status = new DtoActionStatus(job.getJobname(), 5);
 		actionStatus.put(job.getJobname(), status);
 
 		job.addLogMessage("Restarting job...");
 		job.addLogMessage("Killing possibly running job...");
 		status.addElement("Killing job...");
 		kill(job);
 
 		job.setStatus(JobConstants.READY_TO_SUBMIT);
 		status.addElement("Resetting job properties...");
 		job.getJobProperties().remove(Constants.ERROR_REASON);
 
 		String possibleMultiPartJob = job
 				.getJobProperty(Constants.BATCHJOB_NAME);
 
 		BatchJob mpj = null;
 		if (StringUtils.isNotBlank(possibleMultiPartJob)) {
 			mpj = getMultiPartJobFromDatabase(possibleMultiPartJob);
 			addLogMessageToPossibleMultiPartJobParent(job, "Re-submitting job "
 					+ job.getJobname());
 			mpj.removeFailedJob(job.getJobname());
 			multiPartJobDao.saveOrUpdate(mpj);
 		}
 
 		if (StringUtils.isNotBlank(changedJsdl)) {
 			status.addElement("Changing job description...");
 			job.addLogMessage("Changing job properties...");
 			Document newJsdl;
 			Document oldJsdl = job.getJobDescription();
 
 			try {
 				newJsdl = SeveralXMLHelpers.fromString(changedJsdl);
 			} catch (Exception e3) {
 
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
 
 			Integer newTotalCpuTime = JsdlHelpers.getWalltime(newJsdl)
 					* JsdlHelpers.getProcessorCount(newJsdl);
 			job.addLogMessage("Setting totalcputime to: " + newTotalCpuTime);
 			JsdlHelpers.setTotalCPUTimeInSeconds(oldJsdl, newTotalCpuTime);
 			job.addJobProperty(Constants.WALLTIME_IN_MINUTES_KEY, new Integer(
 					JsdlHelpers.getWalltime(newJsdl)).toString());
 
 			Integer newProcCount = JsdlHelpers.getProcessorCount(newJsdl);
 			job.addLogMessage("Setting processor count to: " + newProcCount);
 			JsdlHelpers.setProcessorCount(oldJsdl, newProcCount);
 			job.addJobProperty(Constants.NO_CPUS_KEY, new Integer(newProcCount)
 					.toString());
 
 			// TODO
 			// JsdlHelpers.getTotalMemoryRequirement(newJsdl);
 
 			// JsdlHelpers.getArcsJobType(newJsdl);
 			// JsdlHelpers.getModules(newJsdl);
 			// JsdlHelpers.getPosixApplicationArguments(newJsdl);
 			// JsdlHelpers.getPosixApplicationExecutable(newJsdl);
 			// JsdlHelpers.getPosixStandardError(newJsdl);
 			// JsdlHelpers.getPosixStandardInput(newJsdl);
 			// JsdlHelpers.getPosixStandardOutput(newJsdl);			
 			
 			String[] oldSubLocs = JsdlHelpers.getCandidateHosts(oldJsdl);
 			String oldSubLoc = oldSubLocs[0];
 
 			String[] newSubLocs = JsdlHelpers.getCandidateHosts(newJsdl);
 			String newSubLoc = null;
 			if ( newSubLocs != null && newSubLocs.length >= 1 ) {
 				newSubLoc = newSubLocs[0];
 			}
 
 
 			if ( newSubLoc != null && ! newSubLoc.equals(oldSubLoc) ) {
 				// move job
 				JsdlHelpers.setCandidateHosts(oldJsdl, newSubLocs);
 				job.setJobDescription(oldJsdl);
 
 				status.addElement("Moving job from "+oldSubLoc+" to "+newSubLoc );
 				
 				try {
 					processJobDescription(job, mpj);
 				} catch (JobPropertiesException e) {
 
 					status.addLogMessage("Couldn't process new job description.");
 				}
 			} else {
 				job.setJobDescription(oldJsdl);
 				status.addElement("No need to move job...");
 				// no need to move job
 			}
 			
 			
 			jobdao.saveOrUpdate(job);
 			
 			
 		} else {
 			status.addElement("Keeping job description...");
 			status.addElement("No need to move job...");
 		}
 
 		myLogger.info("Submitting job: " + job.getJobname() + " for user " + getDN());
 		job.addLogMessage("Starting re-submission...");
 		try {
 			submitJob(job, false, status);
 		} catch (JobSubmissionException e) {
 			status.addLogMessage("Job submission failed: " + e.getLocalizedMessage());
 			status.setFailed(true);
 			throw e;
 		}
 		job.addLogMessage("Re-submission finished.");
 
 		status.addElement("Re-submission finished successfully.");
 		status.setFinished(true);
 	}
 
 	public void submitJob(final String jobname) throws JobSubmissionException,
 			NoSuchJobException {
 
 		myLogger.info("Submitting job: " + jobname + " for user " + getDN());
 		Job job;
 
 		DtoActionStatus status = null;
 		status = new DtoActionStatus(jobname, 0);
 		actionStatus.put(jobname, status);
 
 		try {
 			job = getJob(jobname);
 			if (job.getStatus() > JobConstants.READY_TO_SUBMIT) {
 				throw new JobSubmissionException("Job already submitted.");
 			}
 			submitJob(job, true, status);
 
 		} catch (NoSuchJobException e) {
 			// maybe it's a multipartjob
 			final BatchJob multiJob = getMultiPartJobFromDatabase(jobname);
 			submitMultiPartJob(multiJob);
 		}
 
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
 		int old_status = job.getStatus();
 
 		// nothing to kill
 		if (old_status > 999) {
 			return old_status;
 		}
 
 		ProxyCredential cred = job.getCredential();
 		boolean changedCred = false;
 		// TODO check whether cred is stored in the database in that case?
 		if (cred == null || !cred.isValid()) {
 			job.setCredential(user.getCred());
 			changedCred = true;
 		}
 
 		new_status = getSubmissionManager().killJob(job);
 
 		job.addLogMessage("Job killed.");
 		addLogMessageToPossibleMultiPartJobParent(job, "Job: "
 				+ job.getJobname() + " killed, new status: ");
 
 		if (changedCred) {
 			job.setCredential(null);
 		}
 		if (old_status != new_status) {
 			job.setStatus(new_status);
 		}
 		job.addLogMessage("New job status: "
 				+ JobConstants.translateStatus(new_status));
 		addLogMessageToPossibleMultiPartJobParent(job, "Job: "
 				+ job.getJobname() + " killed, new status: "
 				+ JobConstants.translateStatus(new_status));
 		jobdao.saveOrUpdate(job);
 		myLogger.debug("Status of job: " + job.getJobname() + " is: "
 				+ new_status);
 
 		return new_status;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#getJobStatus(java.lang.String)
 	 */
 	public int getJobStatus(final String jobname) {
 
 		myLogger.debug("Start getting status for job: " + jobname);
 		Job job;
 		try {
 			job = getJob(jobname);
 		} catch (NoSuchJobException e) {
 			return JobConstants.NO_SUCH_JOB;
 		}
 
 		int status = Integer.MIN_VALUE;
 		int old_status = job.getStatus();
 
 		// System.out.println("OLDSTAUS "+jobname+": "+JobConstants.translateStatus(old_status));
 		if (old_status <= JobConstants.READY_TO_SUBMIT) {
 			// this couldn't have changed without manual intervention
 			return old_status;
 		}
 
 		if (old_status >= JobConstants.FINISHED_EITHER_WAY) {
 			return old_status;
 		}
 
 		Date lastCheck = job.getLastStatusCheck();
 		Date now = new Date();
 
 		if (old_status != JobConstants.EXTERNAL_HANDLE_READY
 				&& (now.getTime() < lastCheck.getTime()
 						+ (ServerPropertiesManager
 								.getWaitTimeBetweenJobStatusChecks() * 1000))) {
 			myLogger
 					.debug("Last check was: "
 							+ lastCheck.toString()
 							+ ". Too early to check job status again. Returning old status...");
 			return job.getStatus();
 		}
 
 		ProxyCredential cred = job.getCredential();
 		boolean changedCred = false;
 		// TODO check whether cred is stored in the database in that case? also,
 		// is a voms credential needed? -- apparently not - only dn must match
 		if (cred == null || !cred.isValid()) {
 			job.setCredential(getCredential());
 			changedCred = true;
 		}
 
 		myLogger.debug("Getting status for job from submission manager: "
 				+ jobname);
 
 		status = getSubmissionManager().getJobStatus(job);
 		myLogger.debug("Status for job" + jobname
 				+ " from submission manager: " + status);
 		if (changedCred) {
 			job.setCredential(null);
 		}
 		if (old_status != status) {
 			job.setStatus(status);
 			String message = "Job status for job: " + job.getJobname()
 					+ " changed since last check ("
 					+ job.getLastStatusCheck().toString() + ") from: \""
 					+ JobConstants.translateStatus(old_status) + "\" to: \""
 					+ JobConstants.translateStatus(status) + "\"";
 			job.addLogMessage(message);
 			addLogMessageToPossibleMultiPartJobParent(job, message);
 			if (status >= JobConstants.FINISHED_EITHER_WAY
 					&& status != JobConstants.DONE) {
 				job.addJobProperty(Constants.ERROR_REASON,
 						"Job finished with status: "
 								+ JobConstants.translateStatus(status));
 				job.addLogMessage("Job failed. Status: "
 						+ JobConstants.translateStatus(status));
 				String multiPartJobParent = job
 						.getJobProperty(Constants.BATCHJOB_NAME);
 				if (multiPartJobParent != null) {
 					try {
 						BatchJob mpj = getMultiPartJobFromDatabase(multiPartJobParent);
 						mpj.addFailedJob(job.getJobname());
 						addLogMessageToPossibleMultiPartJobParent(job, "Job: "
 								+ job.getJobname() + " failed. Status: "
 								+ JobConstants.translateStatus(job.getStatus()));
 						multiPartJobDao.saveOrUpdate(mpj);
 					} catch (NoSuchJobException e) {
 						// well
 						myLogger.error(e);
 					}
 				}
 			}
 		}
 		job.setLastStatusCheck(new Date());
 		jobdao.saveOrUpdate(job);
 
 		myLogger.debug("Status of job: " + job.getJobname() + " is: " + status);
 		return status;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#ps()
 	 */
 	public DtoJobs ps(String application, boolean refresh) {
 
 		try {
 
 			List<Job> jobs = null;
 			if (StringUtils.isBlank(application)) {
 				jobs = jobdao.findJobByDN(getUser().getDn(),
 						INCLUDE_MULTIPARTJOBS_IN_PS_COMMAND);
 			} else {
 				jobs = jobdao.findJobByDNPerApplication(getUser().getDn(),
 						application, INCLUDE_MULTIPARTJOBS_IN_PS_COMMAND);
 			}
 
 			if (refresh) {
 				refreshJobStatus(jobs);
 			}
 
 			DtoJobs dtoJobs = new DtoJobs();
 			for (Job job : jobs) {
 
 				DtoJob dtojob = DtoJob.createJob(job.getStatus(), job
 						.getJobProperties(), job.getLogMessages());
 
 				// just to make sure
 				dtojob.addJobProperty(Constants.JOBNAME_KEY, job.getJobname());
 				dtoJobs.addJob(dtojob);
 			}
 
 			return dtoJobs;
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 
 	public DtoStringList getAllJobnames(String application) {
 
 		List<String> jobnames = null;
 
 		if (StringUtils.isBlank(application)) {
 			jobnames = jobdao.findJobNamesByDn(getUser().getDn(),
 					INCLUDE_MULTIPARTJOBS_IN_PS_COMMAND);
 		} else {
 			jobnames = jobdao.findJobNamesPerApplicationByDn(getUser().getDn(),
 					application, INCLUDE_MULTIPARTJOBS_IN_PS_COMMAND);
 		}
 
 		return DtoStringList.fromStringList(jobnames);
 	}
 
 	public String refreshBatchJobStatus(String batchJobname)
 			throws NoSuchJobException {
 
 		String handle = "REFRESH_" + batchJobname;
 
 		DtoActionStatus status = actionStatus.get(handle);
 
 		if (status != null && !status.isFinished()) {
 			// refresh in progress. Just give back the handle
 			return handle;
 		}
 
 		BatchJob multiPartJob = getMultiPartJobFromDatabase(batchJobname);
 
 		final DtoActionStatus statusfinal = new DtoActionStatus(handle,
 				multiPartJob.getJobs().size());
 
 		actionStatus.put(handle, statusfinal);
 
 		ExecutorService executor = Executors
 				.newFixedThreadPool(ServerPropertiesManager
 						.getConcurrentJobStatusThreadsPerUser());
 
 		Job[] currentJobs = multiPartJob.getJobs().toArray(new Job[] {});
 		Arrays.sort(currentJobs);
 
 		for (final Job job : currentJobs) {
 			Thread thread = new Thread() {
 				public void run() {
 					statusfinal.addLogMessage("Refreshing job "
 							+ job.getJobname());
 					getJobStatus(job.getJobname());
 					statusfinal.addElement("Job status for job "
 							+ job.getJobname() + " refreshed.");
 
 					if (statusfinal.getTotalElements() <= statusfinal
 							.getCurrentElements()) {
 						statusfinal.setFinished(true);
 					}
 				}
 			};
 			executor.execute(thread);
 		}
 		executor.shutdown();
 
 		return handle;
 
 	}
 
 	/**
 	 * Returns all multipart jobs for this user.
 	 * 
 	 * @return all the multipartjobs of the user
 	 */
 	public DtoBatchJob getBatchJob(String batchJobname)
 			throws NoSuchJobException {
 
 		BatchJob multiPartJob = getMultiPartJobFromDatabase(batchJobname);
 
 		return multiPartJob.createDtoMultiPartJob();
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
 
 		BatchJob multiJob = getMultiPartJobFromDatabase(batchJobname);
 
 		Document jsdl;
 
 		try {
 			jsdl = SeveralXMLHelpers.fromString(jsdlString);
 		} catch (Exception e3) {
 			throw new RuntimeException("Invalid jsdl/xml format.", e3);
 		}
 
 		String jobnameCreationMethod = multiJob
 				.getJobProperty(Constants.JOBNAME_CREATION_METHOD_KEY);
 		if (StringUtils.isBlank(jobnameCreationMethod)) {
 			jobnameCreationMethod = "force-name";
 		}
 		
 		String[] candHosts = JsdlHelpers.getCandidateHosts(jsdl);
 		
 		if ( candHosts == null || candHosts.length == 0 ) {
			Map<GridResource, Integer> resources = calculateResourcesToUse(multiJob);
			multiJob.setResourcesToUse(resources);
 			
 			GridResource leastUsed = null;
 			int amountOfJobs = Integer.MAX_VALUE;
 			for ( GridResource res : resources.keySet() ) {
 				if ( resources.get(res) < amountOfJobs ) {
 					leastUsed = res;
 				}
 			}
 			
 			resources.put(leastUsed, resources.get(leastUsed)+1);
 			
 			String subLoc = SubmissionLocationHelpers.createSubmissionLocationString(leastUsed);
 			JsdlHelpers.setCandidateHosts(jsdl, new String[]{subLoc});
 			
 			
 		}
 
 		String jobname = createJob(jsdl, multiJob.getFqan(), "force-name",
 				multiJob);
 		multiJob.addJob(jobname);
 		multiPartJobDao.saveOrUpdate(multiJob);
 
 		return jobname;
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
 
 		Job job = getJob(jobname);
 		BatchJob multiJob = getMultiPartJobFromDatabase(batchJobname);
 		multiJob.removeJob(job);
 
 		multiPartJobDao.saveOrUpdate(multiJob);
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
 	public DtoBatchJob createBatchJob(String batchJobname, String fqan)
 			throws BatchJobException {
 
 		try {
 			Job possibleJob = getJob(batchJobname);
 			throw new BatchJobException("Can't create multipartjob with id: "
 					+ batchJobname
 					+ ". Non-multipartjob with this id already exists...");
 		} catch (NoSuchJobException e) {
 			// that's good
 		}
 
 		try {
 			BatchJob multiJob = getMultiPartJobFromDatabase(batchJobname);
 		} catch (NoSuchJobException e) {
 			// that's good
 
 			BatchJob multiJobCreate = new BatchJob(getDN(), batchJobname, fqan);
 			multiJobCreate.addJobProperty(Constants.RELATIVE_PATH_FROM_JOBDIR,
 					"../");
 			multiJobCreate.addJobProperty(
 					Constants.RELATIVE_BATCHJOB_DIRECTORY_KEY,
 					ServerPropertiesManager.getGrisuJobDirectoryName() + "/"
 							+ batchJobname);
 
 			multiJobCreate.addLogMessage("MultiPartJob " + batchJobname
 					+ " created.");
 			
 			multiJobCreate.setResourcesToUse(calculateResourcesToUse(multiJobCreate));
 			
 			multiPartJobDao.saveOrUpdate(multiJobCreate);
 
 			try {
 				return multiJobCreate.createDtoMultiPartJob();
 			} catch (NoSuchJobException e1) {
 				// that should never happen
 				e1.printStackTrace();
 			}
 		}
 
 		throw new BatchJobException("MultiPartJob with name " + batchJobname
 				+ " already exists.");
 	}
 
 	/**
 	 * Removes the multipartJob from the server.
 	 * 
 	 * @param batchJobname
 	 *            the name of the multipartJob
 	 * @param deleteChildJobsAsWell
 	 *            whether to delete the child jobs of this multipartjob as well.
 	 */
 	private void deleteMultiPartJob(final BatchJob multiJob, final boolean clean) {
 
 		int size = multiJob.getJobs().size() * 2 + 1;
 
 		if (clean) {
 			size = size + multiJob.getAllUsedMountPoints().size() * 2;
 		}
 
 		final DtoActionStatus newActionStatus = new DtoActionStatus(multiJob
 				.getBatchJobname(), size);
 		this.actionStatus.put(multiJob.getBatchJobname(), newActionStatus);
 
 		ExecutorService executor = Executors
 				.newFixedThreadPool(ServerPropertiesManager
 						.getConcurrentMultiPartJobSubmitThreadsPerUser());
 
 		final Job[] jobs = multiJob.getJobs().toArray(new Job[] {});
 
 		for (Job job : jobs) {
 			multiJob.removeJob(job);
 		}
 		multiPartJobDao.saveOrUpdate(multiJob);
 		for (final Job job : jobs) {
 			Thread thread = new Thread() {
 				public void run() {
 
 					try {
 						newActionStatus.addElement("Killing job: "
 								+ job.getJobname());
 						kill(job, clean);
 						newActionStatus.addElement("Killed job: "
 								+ job.getJobname());
 					} catch (Exception e) {
 						newActionStatus.addElement("Failed killing job "
 								+ job.getJobname() + ": "
 								+ e.getLocalizedMessage());
 						newActionStatus.setFailed(true);
 						e.printStackTrace();
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
 
 		if (clean) {
 			for (String mpRoot : multiJob.getAllUsedMountPoints()) {
 
 				newActionStatus
 						.addElement("Deleting common dir for mountpoint: "
 								+ mpRoot);
 				String url = mpRoot
 						+ multiJob
 								.getJobProperty(Constants.RELATIVE_BATCHJOB_DIRECTORY_KEY);
 				myLogger.debug("Deleting multijobDir: " + url);
 				try {
 					deleteFile(url);
 					newActionStatus
 							.addElement("Deleted common dir for mountpoint: "
 									+ mpRoot);
 				} catch (RemoteFileSystemException e) {
 					newActionStatus
 							.addElement("Couldn't delete common dir for mountpoint: "
 									+ mpRoot);
 					newActionStatus.setFailed(true);
 					myLogger.error("Couldn't delete multijobDir: " + url);
 				}
 
 			}
 		}
 
 		multiPartJobDao.delete(multiJob);
 		newActionStatus.addElement("Deleted multipartjob from database.");
 
 		if (newActionStatus.getTotalElements() <= newActionStatus
 				.getCurrentElements()) {
 			newActionStatus.setFinished(true);
 		}
 
 	}
 
 	public DtoStringList getAllBatchJobnames(String application) {
 
 		List<String> jobnames = null;
 
 		if (StringUtils.isBlank(application)) {
 			jobnames = multiPartJobDao.findJobNamesByDn(getUser().getDn());
 		} else {
 			jobnames = multiPartJobDao.findJobNamesPerApplicationByDn(getUser()
 					.getDn(), application);
 		}
 
 		return DtoStringList.fromStringList(jobnames);
 	}
 
 	/**
 	 * Just a method to refresh the status of all jobs. Could be used by
 	 * something like a cronjob as well. TODO: maybe change to public?
 	 * 
 	 * @param jobs
 	 *            a list of jobs you want to have refreshed
 	 */
 	protected void refreshJobStatus(final Collection<Job> jobs) {
 		for (Job job : jobs) {
 			getJobStatus(job.getJobname());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#mount(java.lang.String,
 	 * java.lang.String)
 	 */
 	public MountPoint mountWithoutFqan(final String url,
 			final String mountpoint, final boolean useHomeDirectory)
 			throws RemoteFileSystemException {
 
 		MountPoint mp = getUser().mountFileSystem(url, mountpoint,
 				useHomeDirectory, informationManager.getSiteForHostOrUrl(url));
 		userdao.saveOrUpdate(getUser());
 		mountPointsForThisSession = null;
 		return mp;
 	}
 
 	public MountPoint mount(final String url, final String mountpoint,
 			String fqan, final boolean useHomeDirectory)
 			throws RemoteFileSystemException {
 		myLogger.debug("Mounting: " + url + " to: " + mountpoint
 				+ " with fqan: " + fqan);
 		if (fqan == null) {
 			fqan = Constants.NON_VO_FQAN;
 		}
 		MountPoint mp = getUser().mountFileSystem(url, mountpoint, fqan,
 				useHomeDirectory, informationManager.getSiteForHostOrUrl(url));
 		userdao.saveOrUpdate(getUser());
 		mountPointsForThisSession = null;
 		return mp;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#umount(java.lang.String)
 	 */
 	public void umount(final String mountpoint) {
 
 		getUser().unmountFileSystem(mountpoint);
 		userdao.saveOrUpdate(getUser());
 		mountPointsForThisSession = null;
 
 	}
 
 	private synchronized MountPoint[] df_internal() {
 
 		if (mountPointsForThisSession == null) {
 
 			// getUser().removeAutoMountedMountpoints();
 			// userdao.attachClean(getUser());
 
 			getUser().setAutoMountedMountPoints(
 					df_auto_mds(getAllSites().asArray()));
 
 			Set<MountPoint> mps = getUser().getAllMountPoints();
 
 			// unmount last automatically mounted filesystems first
 			// for ( MountPoint mp : mps ) {
 			// if ( mp.isAutomaticallyMounted() ) {
 			// getUser().unmountFileSystem(mp.getMountpoint());
 			// }
 			// }
 
 			mountPointsForThisSession = mps.toArray(new MountPoint[mps.size()]);
 		}
 		return mountPointsForThisSession;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#df()
 	 */
 	public synchronized DtoMountPoints df() {
 
 		return DtoMountPoints.createMountpoints(df_internal());
 	}
 
 	/**
 	 * Gets all mountpoints for this fqan.
 	 * 
 	 * @param fqan
 	 *            the fqan
 	 * @return the mountpoints
 	 */
 	protected Set<MountPoint> df(String fqan) {
 
 		Set<MountPoint> result = new HashSet<MountPoint>();
 		for (MountPoint mp : df_internal()) {
 			if (StringUtils.isNotBlank(mp.getFqan())
 					&& mp.getFqan().equals(fqan)) {
 				result.add(mp);
 			}
 		}
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#getMountPointForUri(java.lang
 	 * .String)
 	 */
 	public MountPoint getMountPointForUri(final String uri) {
 
 		return getUser().getResponsibleMountpointForAbsoluteFile(uri);
 	}
 
 	/**
 	 * Calculates the name of the mountpoint for a given server and fqan. It
 	 * does that so the mountpoint looks something like:
 	 * "ng2.vpac.org (StartUp)". Not sure whether that is the way to go, but
 	 * it's the best namingscheme I came up with. Asked in the developers
 	 * mailing list but didn't get any answers that made sense...
 	 * 
 	 * @param server
 	 *            the hostname
 	 * @param fqan
 	 *            the VO
 	 * @return the name of the mountpoint
 	 */
 	private String calculateMountPointName(final String server,
 			final String fqan) {
 
 		URI uri = null;
 		String hostname = null;
 		try {
 			uri = new URI(server);
 			hostname = uri.getHost();
 		} catch (Exception e) {
 			hostname = server;
 		}
 		String name = hostname + " ("
 				+ (fqan.substring(fqan.lastIndexOf("/") + 1) + ")");
 
 		return name;
 	}
 
 	/**
 	 * Calculates all mountpoints that are automatically mounted using mds. At
 	 * the moment, the port part of the gridftp url share is ignored. Maybe I'll
 	 * change that later.
 	 * 
 	 * @param sites
 	 *            the sites that should be used
 	 * @return all MountPoints
 	 */
 	private Set<MountPoint> df_auto_mds(final String[] sites) {
 
 		Set<MountPoint> mps = new TreeSet<MountPoint>();
 
 		// for ( String site : sites ) {
 
 		for (String fqan : getFqans().getStringList()) {
 			Date start = new Date();
 			Map<String, String[]> mpUrl = informationManager
 					.getDataLocationsForVO(fqan);
 			Date end = new Date();
 			myLogger.debug("Querying for data locations for all sites and+ "
 					+ fqan + " took: " + (end.getTime() - start.getTime())
 					+ " ms.");
 			for (String server : mpUrl.keySet()) {
 				for (String path : mpUrl.get(server)) {
 
 					String url = null;
 					if (path.contains("${GLOBUS_USER_HOME}")) {
 
 						try {
 							url = getUser().getFileSystemHomeDirectory(
 									server.replace(":2811", ""), fqan);
 						} catch (FileSystemException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 
 					} else {
 
 						url = server.replace(":2811", "") + path + "/"
 								+ User.get_vo_dn_path(getCredential().getDn());
 
 					}
 
 					if (StringUtils.isBlank(url)) {
 						continue;
 					}
 
 					MountPoint mp = new MountPoint(getUser().getDn(), fqan,
 							url, calculateMountPointName(server, fqan),
 							informationManager.getSiteForHostOrUrl(url), true);
 					// + "." + fqan + "." + path);
 					// + "." + fqan);
 					mps.add(mp);
 				}
 			}
 		}
 
 		// }
 
 		return mps;
 	}
 
 	/**
 	 * Downloads multiple files at once. It's not used at the moment for this
 	 * purpose, though. Only for single file downloads. But maybe in the future.
 	 * 
 	 * @param filenames
 	 *            the urls of the files
 	 * @return the DataSources of the requested files
 	 * @throws RemoteFileSystemException
 	 *             if one of the files doesn't exist
 	 */
 	private DataHandler[] download(final String[] filenames)
 			throws RemoteFileSystemException {
 
 		final DataSource[] datasources = new DataSource[filenames.length];
 		final DataHandler[] datahandlers = new DataHandler[filenames.length];
 
 		for (int i = 0; i < filenames.length; i++) {
 
 			FileObject source = null;
 			DataSource datasource = null;
 			source = getUser().aquireFile(filenames[i]);
 			myLogger.debug("Preparing data for file transmission for file "
 					+ source.getName().toString());
 			try {
 				if (!source.exists()) {
 					throw new RemoteFileSystemException(
 							"Could not provide file: "
 									+ filenames[i]
 									+ " for download: InputFile does not exist.");
 				}
 
 				datasource = new FileContentDataSourceConnector(source
 						.getContent());
 			} catch (FileSystemException e) {
 				throw new RemoteFileSystemException(
 						"Could not find or read file: " + filenames[i] + ": "
 								+ e.getMessage());
 			}
 			datasources[i] = datasource;
 			datahandlers[i] = new DataHandler(datasources[i]);
 		}
 
 		return datahandlers;
 
 	}
 
 	public DataHandler download(final String filename)
 			throws RemoteFileSystemException {
 
 		myLogger.debug("Downloading: " + filename);
 
 		return download(new String[] { filename })[0];
 	}
 
 	private DtoFolder getFolderListing(String url)
 			throws RemoteFileSystemException, FileSystemException {
 
 		DtoFolder folder = new DtoFolder();
 
 		FileObject fo = getUser().aquireFile(url);
 
 		if (!FileType.FOLDER.equals(fo.getType())) {
 			throw new RemoteFileSystemException("Url: " + url
 					+ " not a folder.");
 		}
 
 		folder.setRootUrl(url);
 		folder.setName(fo.getName().getBaseName());
 
 		// TODO the getChildren command seems to throw exceptions without reason
 		// every now and the
 		// probably a bug in commons-vfs-grid. Until this is resolved, I always
 		// try 2 times...
 		FileObject[] children = null;
 		try {
 			children = fo.getChildren();
 		} catch (Exception e) {
 			e.printStackTrace();
 			myLogger.error("Couldn't get children of :"
 					+ fo.getName().toString() + ". Trying one more time...");
 			children = fo.getChildren();
 		}
 
 		for (FileObject child : children) {
 			if (FileType.FOLDER.equals(child.getType())) {
 				DtoFolder childfolder = new DtoFolder();
 				childfolder.setName(child.getName().getBaseName());
 				childfolder.setRootUrl(child.getURL().toString());
 				folder.addChildFolder(childfolder);
 			} else if (FileType.FILE.equals(child.getType())) {
 				DtoFile childFile = new DtoFile();
 				childFile.setName(child.getName().getBaseName());
 				childFile.setRootUrl(child.getURL().toString());
 
 				childFile.setLastModified(child.getContent()
 						.getLastModifiedTime());
 				childFile.setSize(child.getContent().getSize());
 
 				folder.addChildFile(childFile);
 			}
 		}
 
 		return folder;
 	}
 
 	public DtoFolder fillFolder(DtoFolder folder, int recursionLevel)
 			throws FileSystemException, RemoteFileSystemException {
 
 		DtoFolder tempFolder = null;
 		;
 		try {
 			tempFolder = getFolderListing(folder.getRootUrl());
 		} catch (Exception e) {
 			myLogger.error(e);
 			myLogger
 					.error("Error getting folder listing. I suspect this to be a bug in the commons-vfs-grid library. Sleeping for 1 seconds and then trying again...");
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			tempFolder = getFolderListing(folder.getRootUrl());
 		}
 		folder.setChildrenFiles(tempFolder.getChildrenFiles());
 
 		if (recursionLevel <= 0) {
 			folder.setChildrenFolders(tempFolder.getChildrenFolders());
 		} else {
 			for (DtoFolder childFolder : tempFolder.getChildrenFolders()) {
 				folder.addChildFolder(fillFolder(childFolder,
 						recursionLevel - 1));
 			}
 
 		}
 		return folder;
 	}
 
 	public DtoFolder ls(final String directory, int recursion_level)
 			throws RemoteFileSystemException {
 
 		// check whether credential still valid
 		getCredential();
 
 		try {
 
 			DtoFolder rootfolder = getFolderListing(directory);
 			recursion_level = recursion_level - 1;
 			if (recursion_level == 0) {
 				return rootfolder;
 			} else if (recursion_level < 0) {
 				recursion_level = Integer.MAX_VALUE;
 			}
 			fillFolder(rootfolder, recursion_level);
 
 			return rootfolder;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RemoteFileSystemException("Could not list directory "
 					+ directory + ": " + e.getLocalizedMessage());
 		}
 
 		// Document result = null;
 		//
 		// // FileObject dir = null;
 		// // dir = getUser().aquireFile(directory);
 		//
 		// myLogger.debug("Listing directory: " + directory
 		// + " with recursion level: " + recursion_level);
 		//
 		// try {
 		// result = getFsConverter().getDirectoryStructure(directory,
 		// recursion_level, return_absolute_url);
 		// } catch (Exception e) {
 		// myLogger.error("Could not list directory: "
 		// + e.getLocalizedMessage());
 		// // e.printStackTrace();
 		// throw new RemoteFileSystemException("Could not read directory "
 		// + directory + " for ls command: " + e.getMessage());
 		// }
 		//
 		// try {
 		// myLogger.debug(SeveralXMLHelpers.toString(result));
 		// } catch (TransformerFactoryConfigurationError e) {
 		// // TODO Auto-generated catch block
 		// e.printStackTrace();
 		// } catch (TransformerException e) {
 		// // TODO Auto-generated catch block
 		// e.printStackTrace();
 		// }
 		//
 		// return result;
 	}
 
 	/**
 	 * This, well, creates a folder, as one might expect.
 	 * 
 	 * @param folder
 	 *            the folder.
 	 * @throws FileSystemException
 	 *             if the parent folder doesn't exist.
 	 */
 	private void createFolder(final FileObject folder)
 			throws FileSystemException {
 
 		if (!folder.getParent().exists()) {
 			createFolder(folder.getParent());
 		}
 
 		folder.createFolder();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#upload(javax.activation.DataSource
 	 * , java.lang.String)
 	 */
 	public String upload(final DataHandler source, final String filename)
 			throws RemoteFileSystemException {
 
 		myLogger.debug("Receiving file: " + filename);
 		FileObject target = null;
 
 		OutputStream fout = null;
 		try {
 			String parent = filename.substring(0, filename
 					.lastIndexOf(File.separator));
 			FileObject parentObject = getUser().aquireFile(parent);
 			// FileObject tempObject = parentObject;
 
 			createFolder(parentObject);
 			// parentObject.createFolder();
 
 			target = getUser().aquireFile(filename);
 			// just to be sure that the folder exists.
 
 			myLogger.debug("Calculated target: " + target.getName().toString());
 
 			FileContent content = target.getContent();
 			fout = content.getOutputStream();
 		} catch (FileSystemException e) {
 
 			try {
 				fout.close();
 				source.getInputStream().close();
 			} catch (Exception e1) {
 				myLogger.error(e1);
 			}
 
 			// e.printStackTrace();
 			throw new RemoteFileSystemException("Could not open file: "
 					+ filename + ":" + e.getMessage());
 		}
 
 		myLogger.debug("Receiving data for file: " + filename);
 
 		BufferedInputStream buf;
 		try {
 			buf = new BufferedInputStream(source.getInputStream());
 
 			byte[] buffer = new byte[1024]; // byte buffer
 			int bytesRead = 0;
 			while (true) {
 				bytesRead = buf.read(buffer, 0, 1024);
 				// bytesRead returns the actual number of bytes read from
 				// the stream. returns -1 when end of stream is detected
 				if (bytesRead == -1) {
 					break;
 				}
 				fout.write(buffer, 0, bytesRead);
 			}
 
 			if (buf != null) {
 				buf.close();
 			}
 			if (fout != null) {
 				fout.close();
 			}
 		} catch (IOException e) {
 			try {
 				fout.close();
 				source.getInputStream().close();
 			} catch (Exception e1) {
 				myLogger.error(e1);
 			}
 
 			throw new RemoteFileSystemException("Could not write to file: "
 					+ filename + ": " + e.getMessage());
 		}
 
 		myLogger.debug("Data transmission for file " + filename + " finished.");
 
 		buf = null;
 		fout = null;
 		return target.getName().getURI();
 
 	}
 
 	public void copyBatchJobInputFile(String batchJobname, String inputFile,
 			String filename) throws RemoteFileSystemException,
 			NoSuchJobException {
 
 		BatchJob multiJob = getMultiPartJobFromDatabase(batchJobname);
 
 		String relpathFromMountPointRoot = multiJob
 				.getJobProperty(Constants.RELATIVE_BATCHJOB_DIRECTORY_KEY);
 
 		for (String mountPointRoot : multiJob.getAllUsedMountPoints()) {
 
 			String targetUrl = mountPointRoot + "/" + relpathFromMountPointRoot
 					+ "/" + filename;
 			myLogger.debug("Coping multipartjob inputfile " + filename
 					+ " to: " + targetUrl);
 			cpSingleFile(inputFile, targetUrl, true, true);
 
 		}
 
 	}
 
 	public void uploadInputFile(String jobname, final DataHandler source,
 			final String targetFilename) throws NoSuchJobException,
 			RemoteFileSystemException {
 
 		try {
 			final Job job = getJob(jobname);
 
 			// try whether job is single or multi
 			final DtoActionStatus status = new DtoActionStatus(targetFilename,
 					1);
 			actionStatus.put(targetFilename, status);
 
 			new Thread() {
 				public void run() {
 
 					String jobdir = job
 							.getJobProperty(Constants.JOBDIRECTORY_KEY);
 
 					try {
 						upload(source, jobdir + "/" + targetFilename);
 						status.addElement("Upload to " + jobdir + "/"
 								+ targetFilename + " successful.");
 						status.setFinished(true);
 					} catch (RemoteFileSystemException e) {
 						status.addElement("Upload to " + jobdir + "/"
 								+ targetFilename + " failed.");
 						status.setFinished(true);
 						status.setFailed(true);
 					}
 
 				}
 			}.run();
 			return;
 
 		} catch (NoSuchJobException e) {
 			// no single job, let's try a multijob
 		}
 
 		final BatchJob multiJob = getMultiPartJobFromDatabase(jobname);
 
 		myLogger.debug("Receiving datahandler for multipartjob input file...");
 
 		BufferedInputStream buf;
 		try {
 			buf = new BufferedInputStream(source.getInputStream());
 		} catch (IOException e1) {
 			throw new RuntimeException(
 					"Could not get input stream from datahandler...");
 		}
 
 		final FileObject tempFile = getUser().aquireFile(
 				"tmp://" + UUID.randomUUID().toString());
 		OutputStream fout;
 		try {
 			fout = tempFile.getContent().getOutputStream();
 		} catch (FileSystemException e1) {
 			throw new RemoteFileSystemException("Could not create temp file.");
 		}
 		myLogger.debug("Receiving data for file: " + targetFilename);
 
 		try {
 
 			byte[] buffer = new byte[1024]; // byte buffer
 			int bytesRead = 0;
 			while (true) {
 				bytesRead = buf.read(buffer, 0, 1024);
 				// bytesRead returns the actual number of bytes read from
 				// the stream. returns -1 when end of stream is detected
 				if (bytesRead == -1) {
 					break;
 				}
 				fout.write(buffer, 0, bytesRead);
 			}
 
 			if (buf != null) {
 				buf.close();
 			}
 			if (fout != null) {
 				fout.close();
 			}
 		} catch (IOException e) {
 			throw new RemoteFileSystemException("Could not write to file: "
 					+ targetFilename + ": " + e.getMessage());
 		}
 		fout = null;
 
 		ExecutorService executor = Executors.newFixedThreadPool(multiJob
 				.getAllUsedMountPoints().size());
 
 		final DtoActionStatus status = new DtoActionStatus(targetFilename,
 				multiJob.getAllUsedMountPoints().size());
 		actionStatus.put(targetFilename, status);
 
 		for (final String mountPointRoot : multiJob.getAllUsedMountPoints()) {
 
 			Thread thread = new Thread() {
 				public void run() {
 
 					FileObject target = null;
 
 					String relpathFromMountPointRoot = multiJob
 							.getJobProperty(Constants.RELATIVE_BATCHJOB_DIRECTORY_KEY);
 					// String parent = filename.substring(0, filename
 					// .lastIndexOf(File.separator));
 					String parent = mountPointRoot + "/"
 							+ relpathFromMountPointRoot;
 
 					try {
 						FileObject parentObject = getUser().aquireFile(parent);
 						// FileObject tempObject = parentObject;
 
 						createFolder(parentObject);
 						// parentObject.createFolder();
 
 						target = getUser().aquireFile(
 								parent + "/" + targetFilename);
 						// just to be sure that the folder exists.
 
 						myLogger
 								.debug("Calculated target for multipartjob input file: "
 										+ target.getName().toString());
 
 						RemoteFileTransferObject fileTransfer = new RemoteFileTransferObject(
 								tempFile, target, true);
 						myLogger
 								.info("Creating fileTransfer object for source: "
 										+ tempFile.getName()
 										+ " and target: "
 										+ target.toString());
 						// fileTransfers.put(targetFileString, fileTransfer);
 
 						fileTransfer.startTransfer(true);
 						status.addElement("Upload to folder " + parent
 								+ " successful.");
 
 					} catch (Exception e) {
 						e.printStackTrace();
 						status.addElement("Upload to folder " + parent
 								+ " failed: Could not open file: "
 								+ targetFilename + ":" + e.getMessage());
 						status.setFailed(true);
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
 
 		myLogger.debug("All data transmissions for multiPartJob " + jobname
 				+ " started.");
 
 		// buf = null;
 		// try {
 		// tempFile.delete();
 		// } catch (FileSystemException e) {
 		// myLogger.error("Could not delete temp file...", e);
 		// }
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#getFqans()
 	 */
 	public DtoStringList getFqans() {
 
 		if (currentFqans == null) {
 
 			getUser().fillFqans();
 			// TODO store it in database
 			// userdao.attachDirty(getUser());
 			Collection<String> fqans = getUser().getFqans().keySet();
 
 			currentFqans = fqans.toArray(new String[fqans.size()]);
 		}
 		return DtoStringList.fromStringArray(currentFqans);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#getDN()
 	 */
 	public String getDN() {
 		return getUser().getDn();
 	}
 
 	public DtoStringList getAllSites() {
 
 		// if ( ServerPropertiesManager.getMDSenabled() ) {
 		return DtoStringList.fromStringArray(informationManager.getAllSites());
 		// return MountPointManager.getAllSitesFromMDS();
 		// can't enable the mds version right now until the datadirectory thing
 		// works...
 		// return MountPointManager.getAllSites();
 		// } else {
 		// return MountPointManager.getAllSites();
 		// }
 	}
 
 	// public String getStagingFileSystem(String site) {
 	// return MountPointManager.getDefaultFileSystem(site);
 	// }
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#addJobProperty(java.lang.String,
 	 * java.lang.String, java.lang.String)
 	 */
 	public void addJobProperty(final String jobname, final String key,
 			final String value) throws NoSuchJobException {
 
 		try {
 			Job job = getJob(jobname);
 			job.addJobProperty(key, value);
 			jobdao.saveOrUpdate(job);
 			myLogger.debug("Added job property: " + key);
 		} catch (NoSuchJobException e) {
 			BatchJob job = getMultiPartJobFromDatabase(jobname);
 			job.addJobProperty(key, value);
 			multiPartJobDao.saveOrUpdate(job);
 			myLogger.debug("Added multijob property: " + key);
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#addJobProperties(java.lang.String
 	 * , java.util.Map)
 	 */
 	public void addJobProperties(final String jobname, final DtoJob properties)
 			throws NoSuchJobException {
 
 		Job job = getJob(jobname);
 
 		job.addJobProperties(properties.propertiesAsMap());
 		jobdao.saveOrUpdate(job);
 
 		myLogger.debug("Added " + properties.getProperties().size()
 				+ " job properties.");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#getAllJobProperties(java.lang
 	 * .String)
 	 */
 	public DtoJob getAllJobProperties(final String jobname)
 			throws NoSuchJobException {
 
 		Job job = getJob(jobname);
 
 		// job.getJobProperties().put(Constants.JOB_STATUS_KEY,
 		// JobConstants.translateStatus(getJobStatus(jobname)));
 
 		return DtoJob.createJob(job.getStatus(), job.getJobProperties(), job
 				.getLogMessages());
 	}
 
 	public String getJsdlDocument(final String jobname)
 			throws NoSuchJobException {
 
 		Job job = getJob(jobname);
 
 		String jsdlString;
 		jsdlString = SeveralXMLHelpers.toString(job.getJobDescription());
 
 		return jsdlString;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#getJobProperty(java.lang.String,
 	 * java.lang.String)
 	 */
 	public String getJobProperty(final String jobname, final String key)
 			throws NoSuchJobException {
 
 		try {
 			Job job = getJob(jobname);
 
 			return job.getJobProperty(key);
 		} catch (NoSuchJobException e) {
 			BatchJob mpj = getMultiPartJobFromDatabase(jobname);
 			return mpj.getJobProperty(key);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#isFolder(java.lang.String)
 	 */
 	public boolean isFolder(final String file) throws RemoteFileSystemException {
 
 		boolean isFolder;
 		try {
 			isFolder = (getUser().aquireFile(file).getType() == FileType.FOLDER);
 		} catch (Exception e) {
 			myLogger.error("Couldn't access file: " + file
 					+ " to check whether it is a folder."
 					+ e.getLocalizedMessage());
 			// e.printStackTrace();
 			// try again. sometimes it works the second time...
 			try {
 				myLogger.debug("trying a second time...");
 				isFolder = (getUser().aquireFile(file).getType() == FileType.FOLDER);
 			} catch (Exception e2) {
 				// e2.printStackTrace();
 				myLogger.error("Again couldn't access file: " + file
 						+ " to check whether it is a folder."
 						+ e.getLocalizedMessage());
 				throw new RemoteFileSystemException("Could not aquire file: "
 						+ file);
 			}
 		}
 
 		return isFolder;
 
 	}
 
 	public boolean fileExists(final String file)
 			throws RemoteFileSystemException {
 
 		boolean exists;
 
 		try {
 			exists = getUser().aquireFile(file).exists();
 			return exists;
 		} catch (FileSystemException e) {
 
 			throw new RemoteFileSystemException(
 					"Could not connect to filesystem to aquire file: " + file);
 
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#getChildrenFiles(java.lang.String
 	 * , boolean)
 	 */
 	public DtoStringList getChildrenFileNames(final String folder,
 			final boolean onlyFiles) throws RemoteFileSystemException {
 
 		String[] result = null;
 		try {
 			FileObject[] objects = null;
 			if (onlyFiles) {
 				objects = getUser().aquireFile(folder).findFiles(
 						new FileTypeSelector(FileType.FILE));
 			} else {
 				objects = getUser().aquireFile(folder).findFiles(
 						new AllFileSelector());
 			}
 
 			result = new String[objects.length];
 			for (int i = 0; i < objects.length; i++) {
 				result[i] = objects[i].getName().getURI();
 			}
 
 		} catch (FileSystemException e) {
 			throw new RemoteFileSystemException("Could not access folder: "
 					+ folder + ": " + e.getMessage());
 		}
 
 		return DtoStringList.fromStringArray(result);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#getFileSize(java.lang.String)
 	 */
 	public long getFileSize(final String file) throws RemoteFileSystemException {
 
 		FileObject file_object = getUser().aquireFile(file);
 		long size;
 		try {
 			size = file_object.getContent().getSize();
 		} catch (FileSystemException e) {
 			throw new RemoteFileSystemException("Could not get size of file: "
 					+ file + ": " + e.getMessage());
 		}
 
 		return size;
 	}
 
 	public DtoDataLocations getDataLocationsForVO(final String fqan) {
 
 		return DtoDataLocations.createDataLocations(fqan, informationManager
 				.getDataLocationsForVO(fqan));
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#lastModified(java.lang.String)
 	 */
 	public long lastModified(final String url) throws RemoteFileSystemException {
 
 		try {
 			FileObject file = getUser().aquireFile(url);
 			// myLogger.debug(url+" last modified before refresh:
 			// "+file.getContent().getLastModifiedTime());
 			// refresh to get non-cached date
 			// file.refresh();
 			// file.getParent().refresh();
 			// myLogger.debug(url+" last modified after refresh:
 			// "+file.getContent().getLastModifiedTime());
 			return file.getContent().getLastModifiedTime();
 		} catch (FileSystemException e) {
 			throw new RemoteFileSystemException("Could not access file " + url
 					+ ": " + e.getMessage());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#mkdir(java.lang.String)
 	 */
 	public boolean mkdir(final String url) throws RemoteFileSystemException {
 
 		myLogger.debug("Creating folder: " + url + "...");
 		try {
 			FileObject dir = getUser().aquireFile(url);
 			if (!dir.exists()) {
 				dir.createFolder();
 				if (dir.exists()) {
 					return true;
 				} else {
 					return false;
 				}
 			} else {
 				return false;
 			}
 		} catch (FileSystemException e) {
 
 			// try again. Commons-vfs sometimes seems to fail here without any
 			// reason I could figure out...
 			try {
 				FileObject dir = getUser().aquireFile(url);
 				if (!dir.exists()) {
 					dir.createFolder();
 					if (dir.exists()) {
 						return true;
 					} else {
 						return false;
 					}
 				} else {
 					return false;
 				}
 			} catch (Exception e2) {
 				throw new RemoteFileSystemException(
 						"Could not create directory " + url + ": "
 								+ e2.getLocalizedMessage());
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#deleteFile(java.lang.String)
 	 */
 	public void deleteFile(final String file) throws RemoteFileSystemException {
 
 		FileObject fileObject = getUser().aquireFile(file);
 		try {
 			if (fileObject.exists()) {
 				fileObject.delete(new AllFileSelector());
 			}
 		} catch (FileSystemException e) {
 			// TODO Auto-generated catch block
 			// e.printStackTrace();
 			throw new RemoteFileSystemException("Could not delete file: "
 					+ file);
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#deleteFiles(java.lang.String[])
 	 */
 	public void deleteFiles(final DtoStringList files) {
 
 		if (files == null || files.asArray().length == 0) {
 			return;
 		}
 
 		DtoActionStatus status = new DtoActionStatus(files.asArray()[0], files
 				.asArray().length * 2);
 		actionStatus.put(files.asArray()[0], status);
 
 		for (String file : files.getStringList()) {
 			try {
 				status.addElement("Deleting file " + file + "...");
 				deleteFile(file);
 				status.addElement("Success.");
 			} catch (Exception e) {
 				status.addElement("Failed: " + e.getLocalizedMessage());
 				status.setFailed(true);
 				myLogger.error("Could not delete file: " + file);
 				// filesNotDeleted.add(file);
 			}
 		}
 
 		status.setFinished(true);
 
 	}
 
 	public DtoProperties getUserProperties() {
 
 		return DtoProperties
 				.createUserProperties(getUser().getUserProperties());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#getUserProperty(java.lang.String)
 	 */
 	public String getUserProperty(final String key) {
 
 		String value = getUser().getUserProperties().get(key);
 
 		return value;
 	}
 
 	public void setUserProperty(String key, String value) {
 
 		getUser().getUserProperties().put(key, value);
 
 		userdao.saveOrUpdate(getUser());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#submitSupportRequest(java.lang
 	 * .String, java.lang.String)
 	 */
 	public void submitSupportRequest(final String subject,
 			final String description) {
 
 		// TODO
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#getMessagesSince(java.util.Date)
 	 */
 	public Document getMessagesSince(final Date date) {
 
 		// TODO
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#stageFiles(java.lang.String)
 	 */
 	public void stageFiles(final Job job, final DtoActionStatus optionalStatus)
 			throws RemoteFileSystemException, NoSuchJobException {
 
 		// Job job;
 		// job = jobdao.findJobByDN(getUser().getDn(), jobname);
 
 		List<Element> stageIns = JsdlHelpers.getStageInElements(job
 				.getJobDescription());
 
 		for (Element stageIn : stageIns) {
 
 			String sourceUrl = JsdlHelpers.getStageInSource(stageIn);
 			if (optionalStatus != null) {
 				optionalStatus.addElement("Staging file "
 						+ sourceUrl.substring(sourceUrl.lastIndexOf("/") + 1));
 			}
 			// TODO remove that after swing client is fixed.
 			if (sourceUrl.startsWith("file") || sourceUrl.startsWith("dummy")) {
 				continue;
 			}
 			String targetUrl = JsdlHelpers.getStageInTarget(stageIn);
 
 			if (JobConstants.DUMMY_STAGE_FILE.equals(sourceUrl)
 					|| JobConstants.DUMMY_STAGE_FILE.equals(targetUrl)) {
 				continue;
 			}
 
 			if (sourceUrl != null && !"".equals(sourceUrl)) {
 
 				try {
 					if (!getUser().aquireFile(targetUrl).getParent().exists()) {
 						FileObject folder = getUser().aquireFile(targetUrl)
 								.getParent();
 						folder.createFolder();
 					}
 				} catch (FileSystemException e) {
 					if (optionalStatus != null) {
 						optionalStatus
 								.addLogMessage("Error while staging in files.");
 					}
 					throw new RemoteFileSystemException(
 							"Could not create parent folder for file: "
 									+ targetUrl + ": " + e.getMessage());
 				}
 				myLogger.debug("Staging file: " + sourceUrl + " to: "
 						+ targetUrl);
 				cpSingleFile(sourceUrl, targetUrl, true, true);
 				// job.addInputFile(targetUrl);
 			}
 			// }
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
 
 		String jobDir = JsdlHelpers.getAbsoluteWorkingDirectoryUrl(job
 				.getJobDescription());
 
 		myLogger.debug("Using calculated jobdirectory: " + jobDir);
 
 		// job.setJob_directory(jobDir);
 
 		try {
 			FileObject jobDirObject = getUser().aquireFile(jobDir);
 			// have to do this, otherwise exception -> bug in commons vfs?
 			try {
 				jobDirObject.getParent().createFolder();
 			} catch (RuntimeException e) {
 				myLogger
 						.debug("Could not create parent folder. Most likely that's ok. Folder: "
 								+ jobDir);
 			}
 			jobDirObject.createFolder();
 		} catch (FileSystemException e) {
 			throw new RemoteFileSystemException(
 					"Could not create job output folder: " + jobDir);
 		}
 		// now after the jsdl is ready, don't forget to fill the required fields
 		// into the database
 	}
 
 	public void killJobs(final DtoStringList jobnames, final boolean clear) {
 
 		if (jobnames == null || jobnames.asArray().length == 0) {
 			return;
 		}
 
 		DtoActionStatus status = new DtoActionStatus(jobnames.asArray()[0],
 				jobnames.asArray().length * 2);
 		actionStatus.put(jobnames.asArray()[0], status);
 
 		for (String jobname : jobnames.asArray()) {
 			status.addElement("Killing job " + jobname + "...");
 			try {
 				kill(jobname, clear);
 				status.addElement("Success.");
 			} catch (Exception e) {
 				status.addElement("Failed: " + e.getLocalizedMessage());
 				status.setFailed(true);
 				myLogger.error("Could not kill job: " + jobname);
 			}
 		}
 
 		status.setFinished(true);
 	}
 
 	public void kill(final String jobname, final boolean clear)
 			throws RemoteFileSystemException, NoSuchJobException,
 			BatchJobException {
 
 		try {
 			Job job;
 
 			job = jobdao.findJobByDN(getUser().getDn(), jobname);
 
 			kill(job, clear);
 
 		} catch (NoSuchJobException nsje) {
 			BatchJob mpj = getMultiPartJobFromDatabase(jobname);
 			deleteMultiPartJob(mpj, clear);
 		}
 	}
 
 	private void kill(final Job job, final boolean clear)
 			throws BatchJobException {
 
 		// Job job;
 		//
 		// job = jobdao.findJobByDN(getUser().getDn(), jobname);
 
 		kill(job);
 
 		if (clear) {
 
 			if (job.isBatchJob()) {
 
 				try {
 					BatchJob mpj = getMultiPartJobFromDatabase(job
 							.getJobProperty(Constants.BATCHJOB_NAME));
 					mpj.removeJob(job);
 					multiPartJobDao.saveOrUpdate(mpj);
 				} catch (Exception e) {
 					// e.printStackTrace();
 					// doesn't matter
 				}
 
 			}
 
 			if (job.getJobProperty(Constants.JOBDIRECTORY_KEY) != null) {
 
 				try {
 					FileObject jobDir = getUser().aquireFile(
 							job.getJobProperty(Constants.JOBDIRECTORY_KEY));
 					jobDir.delete(new AllFileSelector());
 					jobDir.delete();
 				} catch (Exception e) {
 					// throw new RemoteFileSystemException(
 					// "Could not delete jobdirectory: " + e.getMessage());
 					myLogger
 							.error("Could not delete jobdirectory: "
 									+ e.getMessage()
 									+ " Deleting job anyway and don't throw an exception.");
 				}
 			}
 		}
 
 		if (clear) {
 			jobdao.delete(job);
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#cp(java.lang.String,
 	 * java.lang.String, boolean, boolean)
 	 */
 	public String cp(final DtoStringList sources, final String target,
 			final boolean overwrite, final boolean waitForFileTransferToFinish)
 			throws RemoteFileSystemException {
 
 		String handle = null;
 
 		if (actionStatus.get(target) == null) {
 			handle = target;
 		} else {
 			int counter = 0;
 			do {
 				handle = target + "_" + counter;
 				counter = counter + 1;
 			} while (actionStatus.get(handle) != null);
 		}
 
 		final DtoActionStatus actionStat = new DtoActionStatus(handle, sources
 				.asArray().length * 2);
 
 		actionStatus.put(handle, actionStat);
 
 		final String handleFinal = handle;
 		Thread cpThread = new Thread() {
 			public void run() {
 				try {
 					for (String source : sources.asArray()) {
 						actionStat.addElement("Starting transfer of file: "
 								+ source);
 						String filename = FileHelpers.getFilename(source);
 						RemoteFileTransferObject rto = cpSingleFile(source,
 								target + "/" + filename, overwrite, true);
 
 						if (rto.isFailed()) {
 							actionStat.addElement("Transfer failed: "
 									+ rto.getPossibleException()
 											.getLocalizedMessage());
 							actionStat.setFailed(true);
 							actionStat.setFinished(true);
 							throw new RemoteFileSystemException(rto
 									.getPossibleException()
 									.getLocalizedMessage());
 						} else {
 							actionStat.addElement("Finished transfer of file: "
 									+ source);
 						}
 					}
 					actionStat.setFinished(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 					actionStat.addElement("Transfer failed: "
 							+ e.getLocalizedMessage());
 					actionStat.setFailed(true);
 					actionStat.setFinished(true);
 				}
 			}
 		};
 
 		cpThread.start();
 
 		if (waitForFileTransferToFinish) {
 			try {
 				cpThread.join();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		return handle;
 
 	}
 
 	private RemoteFileTransferObject cpSingleFile(final String source,
 			final String target, final boolean overwrite,
 			final boolean waitForFileTransferToFinish)
 			throws RemoteFileSystemException {
 
 		final FileObject source_file;
 		final FileObject target_file;
 
 		source_file = getUser().aquireFile(source);
 		target_file = getUser().aquireFile(target);
 
 		String targetFileString;
 		try {
 			targetFileString = target_file.getURL().toString();
 		} catch (FileSystemException e1) {
 			myLogger.error("Could not retrieve targetfile url: "
 					+ e1.getLocalizedMessage());
 			throw new RemoteFileSystemException(
 					"Could not retrive targetfile url: "
 							+ e1.getLocalizedMessage());
 		}
 
 		RemoteFileTransferObject fileTransfer = new RemoteFileTransferObject(
 				source_file, target_file, overwrite);
 
 		myLogger.info("Creating fileTransfer object for source: "
 				+ source_file.getName() + " and target: "
 				+ target_file.toString());
 		// fileTransfers.put(targetFileString, fileTransfer);
 
 		fileTransfer.startTransfer(waitForFileTransferToFinish);
 
 		// if ( waitForFileTransferToFinish ) {
 		// myLogger.info("Waiting for filetransfer with target "+targetFileString+" to finish.");
 		// fileTransfer.joinFileTransfer();
 		// }
 
 		// myLogger.info("Filetransfer with target " + targetFileString
 		// + " finished.");
 
 		return fileTransfer;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#getAllSubmissionLocations()
 	 */
 	public synchronized DtoSubmissionLocations getAllSubmissionLocations() {
 
 		return DtoSubmissionLocations
 				.createSubmissionLocationsInfo(informationManager
 						.getAllSubmissionLocations());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#getSubmissionLocationsForApplication
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
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#getSubmissionLocationsForApplication
 	 * (java.lang.String, java.lang.String)
 	 */
 	public DtoSubmissionLocations getSubmissionLocationsForApplicationAndVersion(
 			final String application, final String version) {
 
 		String[] sls = informationManager.getAllSubmissionLocations(
 				application, version);
 
 		return DtoSubmissionLocations.createSubmissionLocationsInfo(sls);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#getVersionsOfApplicationOnSite
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
 
 	public DtoApplicationInfo getSubmissionLocationsPerVersionOfApplication(
 			final String application) {
 		// if (ServerPropertiesManager.getMDSenabled()) {
 		myLogger
 				.debug("Getting map of submissionlocations per version of application for: "
 						+ application);
 		Map<String, String> appVersionMap = new HashMap<String, String>();
 		String[] versions = informationManager
 				.getAllVersionsOfApplicationOnGrid(application);
 		for (int i = 0; versions != null && i < versions.length; i++) {
 			String[] submitLocations = null;
 			try {
 				submitLocations = informationManager.getAllSubmissionLocations(
 						application, versions[i]);
 				if (submitLocations == null) {
 					myLogger
 							.error("Couldn't find submission locations for application: \""
 									+ application
 									+ "\""
 									+ ", version \""
 									+ versions[i]
 									+ "\". Most likely the mds is not published correctly.");
 					continue;
 				}
 			} catch (Exception e) {
 				myLogger
 						.error("Couldn't find submission locations for application: \""
 								+ application
 								+ "\""
 								+ ", version \""
 								+ versions[i]
 								+ "\". Most likely the mds is not published correctly.");
 				continue;
 			}
 			StringBuffer submitLoc = new StringBuffer();
 
 			if (submitLocations != null) {
 				for (int j = 0; j < submitLocations.length; j++) {
 					submitLoc.append(submitLocations[j]);
 					if (j < submitLocations.length - 1) {
 						submitLoc.append(",");
 					}
 				}
 			}
 			appVersionMap.put(versions[i], submitLoc.toString());
 		}
 		return DtoApplicationInfo.createApplicationInfo(application,
 				appVersionMap);
 	}
 
 	public DtoSubmissionLocations getSubmissionLocationsForApplicationAndVersionAndFqan(
 			final String application, final String version, final String fqan) {
 		// TODO implement a method which takes in fqan later on
 
 		return DtoSubmissionLocations
 				.createSubmissionLocationsInfo(informationManager
 						.getAllSubmissionLocations(application, version));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#getSite(java.lang.String)
 	 */
 	public String getSite(final String host_or_url) {
 
 		return informationManager.getSiteForHostOrUrl(host_or_url);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#getAllHosts()
 	 */
 	public synchronized DtoHostsInfo getAllHosts() {
 
 		DtoHostsInfo info = DtoHostsInfo.createHostsInfo(informationManager
 				.getAllHosts());
 
 		return info;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#getAllSubmissionLocations(java
 	 * .lang.String)
 	 */
 	public DtoSubmissionLocations getAllSubmissionLocationsForFqan(
 			final String fqan) {
 
 		return DtoSubmissionLocations
 				.createSubmissionLocationsInfo(informationManager
 						.getAllSubmissionLocationsForVO(fqan));
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#getApplicationDetails(java.lang
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
 
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see
 	// * org.vpac.grisu.control.ServiceInterface#getApplicationDetails(java.lang
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
 
 	public DtoGridResources findMatchingSubmissionLocationsUsingMap(
 			final DtoJob jobProperties, final String fqan,
 			boolean excludeResourcesWithLessCPUslotsFreeThanRequested) {
 
 		LinkedList<String> result = new LinkedList<String>();
 
 		Map<JobSubmissionProperty, String> converterMap = new HashMap<JobSubmissionProperty, String>();
 		for (DtoJobProperty jp : jobProperties.getProperties()) {
 			converterMap.put(JobSubmissionProperty.fromString(jp.getKey()), jp
 					.getValue());
 		}
 
 		List<GridResource> resources = null;
 		if (excludeResourcesWithLessCPUslotsFreeThanRequested) {
 			resources = matchmaker.findAvailableResources(converterMap, fqan);
 		} else {
 			resources = matchmaker.findAllResources(converterMap, fqan);
 		}
 
 		return DtoGridResources.createGridResources(resources);
 	}
 
 	public DtoGridResources findMatchingSubmissionLocationsUsingJsdl(
 			String jsdlString, final String fqan,
 			boolean excludeResourcesWithLessCPUslotsFreeThanRequested) {
 
 		Document jsdl;
 		try {
 			jsdl = SeveralXMLHelpers.fromString(jsdlString);
 		} catch (Exception e) {
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
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.control.ServiceInterface#getAllAvailableApplications(java
 	 * .lang.String[])
 	 */
 	public DtoStringList getAllAvailableApplications(final DtoStringList sites) {
 		Set<String> siteList = new TreeSet<String>();
 
 		if (sites == null) {
 			return DtoStringList.fromStringArray(informationManager
 					.getAllApplicationsOnGrid());
 		}
 		for (String site : sites.getStringList()) {
 			siteList.addAll(Arrays.asList(informationManager
 					.getAllApplicationsAtSite(site)));
 		}
 
 		return DtoStringList.fromStringArray(siteList.toArray(new String[] {}));
 
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
 
 		String[] versions = informationManager.getVersionsOfApplicationOnSite(
 				application, site);
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
 		} catch (NumberFormatException e) {
 			return versions[0];
 		}
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
 			contactString = subLoc.substring(subLoc.indexOf(":") + 1, subLoc
 					.indexOf("#"));
 		} else {
 			contactString = subLoc.substring(subLoc.indexOf(":") + 1);
 		}
 
 		return getSite(contactString);
 	}
 
 	public DtoActionStatus getActionStatus(String actionHandle) {
 
 		DtoActionStatus result = actionStatus.get(actionHandle);
 
 		// System.out.println("Elements before: "+result.getLog().size());
 
 		return result;
 
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
 
 }
