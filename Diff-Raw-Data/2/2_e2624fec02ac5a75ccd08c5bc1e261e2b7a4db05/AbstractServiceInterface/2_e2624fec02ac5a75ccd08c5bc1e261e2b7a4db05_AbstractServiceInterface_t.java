 
 
 package org.vpac.grisu.control.serviceInterfaces;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.activation.DataSource;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.apache.commons.vfs.AllFileSelector;
 import org.apache.commons.vfs.FileContent;
 import org.apache.commons.vfs.FileObject;
 import org.apache.commons.vfs.FileSystemException;
 import org.apache.commons.vfs.FileType;
 import org.apache.commons.vfs.FileTypeSelector;
 import org.apache.log4j.Logger;
 import org.vpac.grisu.control.JobConstants;
 import org.vpac.grisu.control.JobCreationException;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.SeveralXMLHelpers;
 import org.vpac.grisu.control.exceptions.JobDescriptionNotValidException;
 import org.vpac.grisu.control.exceptions.NoSuchJobException;
 import org.vpac.grisu.control.exceptions.NoValidCredentialException;
 import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
 import org.vpac.grisu.control.exceptions.ServerJobSubmissionException;
 import org.vpac.grisu.control.exceptions.VomsException;
 import org.vpac.grisu.control.info.CachedMdsInformationManager;
 import org.vpac.grisu.control.info.InformationManager;
 import org.vpac.grisu.control.utils.CertHelpers;
 import org.vpac.grisu.control.utils.JsdlModifier;
 import org.vpac.grisu.control.utils.ServerPropertiesManager;
 import org.vpac.grisu.credential.model.ProxyCredential;
 import org.vpac.grisu.credential.model.ProxyCredentialDAO;
 import org.vpac.grisu.fs.control.FileContentDataSourceConnector;
 import org.vpac.grisu.fs.control.utils.FileSystemStructureToXMLConverter;
 import org.vpac.grisu.fs.model.MountPoint;
 import org.vpac.grisu.fs.model.User;
 import org.vpac.grisu.fs.model.UserDAO;
 import org.vpac.grisu.js.control.JobNameManager;
 import org.vpac.grisu.js.control.Utils.JobsToXMLConverter;
 import org.vpac.grisu.js.control.job.JobSubmissionManager;
 import org.vpac.grisu.js.control.job.JobSubmitter;
 import org.vpac.grisu.js.control.job.gt4.GT4Submitter;
 import org.vpac.grisu.js.model.Job;
 import org.vpac.grisu.js.model.JobDAO;
 import org.vpac.grisu.js.model.utils.JsdlHelpers;
 import org.vpac.security.light.voms.VO;
 import org.vpac.security.light.voms.VOManagement.VOManagement;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 
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
  * 
  */
public abstract class AbstractServiceInterface implements ServiceInterface {
 
 	static final Logger myLogger = Logger
 			.getLogger(AbstractServiceInterface.class.getName());
 
 	protected InformationManager informationManager = CachedMdsInformationManager.getDefaultCachedMdsInformationManager();
 
 	protected ProxyCredentialDAO credentialdao = new ProxyCredentialDAO();
 
 	protected UserDAO userdao = new UserDAO();
 
 	protected JobDAO jobdao = new JobDAO();
 
 	protected ProxyCredentialDAO creddao = new ProxyCredentialDAO();
 
 	protected MountPoint[] mountPointsForThisSession = null;
 
 	protected JobSubmissionManager manager = null;
 
 	protected User user = null;
 
 	protected String[] currentFqans = null;
 
 	protected FileSystemStructureToXMLConverter fsconverter = null;
 	
 	public double getInterfaceVersion() {
 		return ServiceInterface.INTERFACE_VERSION;
 	}
 
 	/**
 	 * Gets the user of the current session. Also connects the default
 	 * credential to it.
 	 * 
 	 * @return the user or null if user could not be created
 	 * @throws NoValidCredentialException
 	 *             if no valid credential could be found to create the user
 	 */
 	protected User getUser() {
 
 		// make sure there is a valid credential
 		if (getCredential() == null || !getCredential().isValid())
 			throw new NoValidCredentialException(
 					"No valid credential exists in this session for user: "
 							+ user);
 
 		// if ( getCredential())
 
 		if (user == null) {
 			// try to look up user in the database
 			user = userdao.findUserByDN(getCredential().getDn());
 
 			if (user == null) {
 				user = new User(getCredential());
 				userdao.save(user);
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
 	 * Searches for the job with the specified jobname for the current user
 	 * 
 	 * @param jobname
 	 *            the name of the job (which is unique within one user)
 	 * @return the job
 	 */
 	protected Job getJob(String jobname) throws NoSuchJobException {
 
 		Job job = jobdao.findJobByDN(getUser().getCred().getDn(), jobname);
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
 			manager = new JobSubmissionManager(this, submitters);
 		}
 		return manager;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#createJob(java.lang.String,
 	 *      int)
 	 */
 	public String createJob(String jobname, int createJobNameMethod)
 			throws JobCreationException {
 
 		Job job = null;
 		getCredential();
 
 		String newJobname = JobNameManager.getJobname(getUser().getDn(),
 				jobname, createJobNameMethod);
 
 		job = new Job(getCredential().getDn(), newJobname);
 
 		// check again whether there is not a job with this jobname in the
 		// database
 		try {
 			Job testJob = getJob(job.getJobname());
 			throw new JobCreationException(
 					"Could not save job in database: jobname already taken.");
 		} catch (NoSuchJobException e) {
 			// good.
 		}
 
 		job.setStatus(JobConstants.JOB_CREATED);
 		jobdao.save(job);
 
 		myLogger.debug("Job \""+job.getJobname()+"\" successfully created in database.");
 		
 		return job.getJobname();
 	}
 
 //	/**
 //	 * Helper method to figure out a free jobname for the above
 //	 * {@link #createJob(String, int) method.
 //	 * 
 //	 * @param proposedJobname
 //	 *            the jobname you would like to have
 //	 * @param createJobNameMethod
 //	 *            the method on how to create the jobname
 //	 * @return the new jobname that is unique
 //	 * @throws JobCreationException
 //	 *             if it's not possible to create a jobname
 //	 */
 //	protected String calculateJobname(String proposedJobname,
 //			int createJobNameMethod) throws JobCreationException {
 //
 //		return JobNameManager.getJobname(getUser().getDn(), proposedJobname,
 //				createJobNameMethod);
 //	}
 	
 //	/*
 //	 * (non-Javadoc)
 //	 * 
 //	 * @see org.vpac.grisu.control.ServiceInterface#createJob(org.w3c.dom.Document,
 //	 *      int)
 //	 */
 //	public String createJob(Document jsdl, int createJobNameMethod)
 //			throws JobDescriptionNotValidException, JobCreationException {
 //
 //		Job job = null;
 //		String jobname = JsdlHelpers.getJobname(jsdl);
 //
 //		// this throws a JobCreationException if necessary
 //		String newJobname = JobNameManager.getJobname(getUser().getDn(),
 //				jobname, createJobNameMethod);
 //
 //		if (!jobname.equals(newJobname)) {
 //			try {
 //				JsdlHelpers.setJobname(jsdl, newJobname);
 //			} catch (XPathExpressionException e) {
 //				throw new JobCreationException("Could not create job: "
 //						+ e.getLocalizedMessage());
 //			}
 //		}
 //
 //		try {
 //			job = new Job(getCredential().getDn(), jsdl);
 //		} catch (XPathExpressionException e) {
 //			throw new JobDescriptionNotValidException(
 //					"Could not calculate the jobname for the job: "
 //							+ e.getMessage());
 //		} catch (SAXException e1) {
 //			throw new JobDescriptionNotValidException(
 //					"Job description is not valid: " + e1.getMessage());
 //		}
 //
 //		// check again whether there is not a job with this jobname in the
 //		// database
 //		try {
 //			Job testJob = getJob(job.getJobname());
 //			throw new JobCreationException(
 //					"Could not save job in database: jobname already taken.");
 //		} catch (NoSuchJobException e) {
 //			// good.
 //		}
 //
 //		job.setStatus(JobConstants.JOB_CREATED);
 //		jobdao.save(job);
 //
 //		return job.getJobname();
 //	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#setJobDescription(java.lang.String,
 	 *      org.w3c.dom.Document)
 	 */
 	public void setJobDescription(String jobname, Document jsdl)
 			throws JobDescriptionNotValidException, NoSuchJobException {
 
 		Job job = getJob(jobname);
 		
 		myLogger.debug("Adding job description to job: "+jobname);
 		myLogger.debug(SeveralXMLHelpers.toStringWithoutAnnoyingExceptions(jsdl));
 
 		job.setJobDescription(jsdl);
 		jobdao.attachDirty(job);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#setJobDescription_string(java.lang.String, java.lang.String)
 	 */
 	public void setJobDescription_string(String jobname, String jsdl) throws JobDescriptionNotValidException, NoSuchJobException {
 		
 		try {
 			Document jsdl_doc = SeveralXMLHelpers.fromString(jsdl);
 			setJobDescription(jobname, jsdl_doc);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new JobDescriptionNotValidException("Could not parse string into xml document: "+e.getLocalizedMessage());
 		}		
 		
 		
 		
 	}
 
 	/**
 	 * Kills the job with the specified jobname. Before it does that it checks the database whether the job may be already finished. In that case it doesn't need to contact globus, which is much faster.
 	 * 
 	 * @param jobname the name of the job
 	 * @return the new status of the job
 	 */
 	protected int kill(String jobname) {
 
 		Job job;
 		try {
 			job = jobdao.findJobByDN(getUser().getDn(), jobname);
 		} catch (NoSuchJobException e) {
 			return JobConstants.NO_SUCH_JOB;
 		}
 
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
 
 		if (changedCred)
 			job.setCredential(null);
 		if (old_status != new_status) {
 			job.setStatus(new_status);
 			jobdao.attachDirty(job);
 		}
 		myLogger.debug("Status of job: " + job.getJobname() + " is: "
 				+ new_status);
 
 		return new_status;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#getJobStatus(java.lang.String)
 	 */
 	public int getJobStatus(String jobname) {
 
 		Job job;
 		try {
 			job = getJob(jobname);
 		} catch (NoSuchJobException e) {
 			return JobConstants.NO_SUCH_JOB;
 		}
 
 		int status = Integer.MIN_VALUE;
 		int old_status = job.getStatus();
 		if (old_status == JobConstants.JOB_CREATED) {
 			// this couldn't have changed without manual intervtion
 			return old_status;
 		}
 		
 		if ( old_status >= JobConstants.FINISHED_EITHER_WAY ) {
 			return old_status;
 		}
 
 		ProxyCredential cred = job.getCredential();
 		boolean changedCred = false;
 		// TODO check whether cred is stored in the database in that case?
 		if (cred == null || !cred.isValid()) {
 			job.setCredential(getCredential());
 			changedCred = true;
 		}
 
 		status = getSubmissionManager().getJobStatus(job);
 		if (changedCred)
 			job.setCredential(null);
 		if (old_status != status) {
 			job.setStatus(status);
 			jobdao.attachDirty(job);
 		}
 		myLogger.debug("Status of job: " + job.getJobname() + " is: " + status);
 		return status;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#ps()
 	 */
 	public Document ps() {
 
 		List<Job> jobs = jobdao.findJobByDN(getUser().getDn());
 
 		refreshJobStatus(jobs);
 
 		Document info = JobsToXMLConverter.getJobsInformation(jobs);
 
 		return info;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#ps_string()
 	 */
 	public String ps_string() {
 		
 		String result = null;
 		
 		try {
 			result = SeveralXMLHelpers.toString(ps());
 		} catch (TransformerFactoryConfigurationError e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return result;
 		
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getAllJobnames()
 	 */
 	public String[] getAllJobnames() {
 		
 		List<String> jobnames = jobdao.findJobNamesByDn(getUser().getDn());
 		
 		return jobnames.toArray(new String[]{});
 	}
 
 	/**
 	 * Just a method to refresh the status of all jobs. Could be used by
 	 * something like a cronjob as well. TODO: maybe change to public?
 	 * 
 	 * @param jobs
 	 *            a list of jobs you want to have refreshed
 	 */
 	protected void refreshJobStatus(List<Job> jobs) {
 		for (Job job : jobs) {
 			getJobStatus(job.getJobname());
 		}
 	}
 
 //	/*
 //	 * (non-Javadoc)
 //	 * 
 //	 * @see org.vpac.grisu.control.ServiceInterface#saveDefaultCredentialToDatabase()
 //	 */
 //	public Long saveDefaultCredentialToDatabase() {
 //
 //		Long credID = null;
 //		credID = credentialdao.save(getCredential());
 //
 //		return credID;
 //	}
 
 	/**
 	 * This one takes care of all the housekeeping tasks that have to be done
 	 * before a job is submitted. At the moment it only creates a folder with
 	 * the name of the job in the execution host fs.
 	 * 
 	 * @param job
 	 *            the job you want the environment for
 	 * @throws RemoteFileSystemException
 	 *             if the job folder could not be created
 	 */
 
 	/**
 	 * This one parses the job description and fills in necessary fields into
 	 * the database. It's not really necessary because all the information is in
 	 * the jsdl document but that way the database is much easier to search.
 	 * 
 	 * @param job
 	 *            the jobname of the job you want to parse
 	 */
 	protected void parseJobDescription(Job job) {
 
 		Document jsdl = job.getJobDescription();
 
 		Map<String, String> jobProperties = job.getJobProperties();
 
 		jobProperties.put("applicationType", JsdlHelpers
 				.getApplicationName(jsdl));
 		// // TODO jobname as workindirectory
 		// jobProperties.put("jobDirectory", job.getJobname());
 		jobProperties.put("stdout", JsdlHelpers.getPosixStandardOutput(jsdl));
 		jobProperties.put("stderr", JsdlHelpers.getPosixStandardError(jsdl));
 		jobProperties.put("fqan", null);
 		jobProperties.put("executionHostFileSystem", getUser()
 				.returnAbsoluteUrl(JsdlHelpers.getUserExecutionHostFs(jsdl)));
 
 		// fill info
 		// this will disapear later and only jobProperties will be used.
 		String app = JsdlHelpers.getApplicationName(job.getJobDescription());
 		job.setApplication(app);
 
 		String stdout = JsdlHelpers.getPosixStandardOutput(job
 				.getJobDescription());
 		job.setStdout(job.getJob_directory() + File.separator + stdout);
 		String stderr = JsdlHelpers.getPosixStandardError(job
 				.getJobDescription());
 		job.setStderr(job.getJob_directory() + File.separator + stderr);
 
 		jobdao.attachDirty(job);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#mount(java.lang.String,
 	 *      java.lang.String)
 	 */
 	public MountPoint mount(String url, String mountpoint,
 			boolean useHomeDirectory) throws RemoteFileSystemException,
 			VomsException {
 
 		MountPoint mp = getUser().mountFileSystem(url, mountpoint,
 				useHomeDirectory);
 		userdao.attachDirty(getUser());
 		mountPointsForThisSession = null;
 		return mp;
 	}
 
 	public MountPoint mount(String url, String mountpoint, String fqan,
 			boolean useHomeDirectory) throws RemoteFileSystemException,
 			VomsException {
 		myLogger.debug("Mounting: " + url + " to: " + mountpoint
 				+ " with fqan: " + fqan);
 		MountPoint mp = getUser().mountFileSystem(url, mountpoint, fqan,
 				useHomeDirectory);
 		userdao.attachDirty(getUser());
 		mountPointsForThisSession = null;
 		return mp;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#umount(java.lang.String)
 	 */
 	public void umount(String mountpoint) {
 
 		getUser().unmountFileSystem(mountpoint);
 		userdao.attachDirty(getUser());
 		mountPointsForThisSession = null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#df()
 	 */
 	public MountPoint[] df() {
 
 		if (mountPointsForThisSession == null) {
 
 			// getUser().removeAutoMountedMountpoints();
 			// userdao.attachClean(getUser());
 
 			getUser().setAutoMountedMountPoints(df_auto_mds(getAllSites()));
 
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
 	
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getMountPointForUri(java.lang.String)
 	 */
 	public MountPoint getMountPointForUri(String uri) {
 		
 		return getUser().getResponsibleMountpointForAbsoluteFile(uri);
 	}
 
 	/**
 	 * Calculates the name of the mountpoint for a given server and fqan. It does that so the mountpoint looks something like:
 	 * "ng2.vpac.org (StartUp)". Not sure whether that is the way to go, but it's the best namingscheme I came up with.
 	 * Asked in the developers mailing list but didn't get any answers that made sense...
 	 * 
 	 * @param server the hostname
 	 * @param fqan the VO
 	 * @return the name of the mountpoint
 	 */
 	private String calculateMountPointName(String server, String fqan) {
 
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
 	 * Calculates all mountpoints that are automatically mounted using mds. At the moment, the port of the gridftp file share is
 	 * ignored. Maybe I'll change that later.
 	 * @param sites the sites that should be used
 	 * @return all MountPoints
 	 */
 	private Set<MountPoint> df_auto_mds(String[] sites) {
 
 		Set<MountPoint> mps = new TreeSet<MountPoint>();
 
 		// for ( String site : sites ) {
 
 		for (String fqan : getFqans()) {
 			Date start = new Date();
 			Map<String, String[]> mpUrl = getDataLocationsForVO(fqan);
 			Date end = new Date();
 			myLogger.debug("Querying for data locations for all sites and+ "
 					+ fqan + " took: " + (end.getTime() - start.getTime())
 					+ " ms.");
 			for (String server : mpUrl.keySet()) {
 				for (String path : mpUrl.get(server)) {
 					MountPoint mp = new MountPoint(getUser().getDn(), fqan,
 							server.replace(":2811", "") + path + "/"
 									+ User.GET_VO_DN_PATH(getCredential().getDn()),
 							calculateMountPointName(server, fqan), true);
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
 	 * Downloads multiple files at once. It's not used at the moment for this purpose, though. Only for single file
 	 * downloads. But maybe in the future.
 	 * 
 	 * @param filenames the urls of the files
 	 * @return the DataSources of the requested files
 	 * @throws RemoteFileSystemException if one of the files doesn't exist
 	 * @throws VomsException if one of the files can't be accessed
 	 */
 	private DataSource[] download(String[] filenames)
 			throws RemoteFileSystemException, VomsException {
 
 		final DataSource[] datasources = new DataSource[filenames.length];
 
 		for (int i = 0; i < filenames.length; i++) {
 
 			FileObject source = null;
 			DataSource datasource = null;
 			source = getUser().aquireFile(filenames[i]);
 			myLogger.debug("Preparing data for file transmission for file "+source.getName().toString());
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
 		}
 
 		return datasources;
 
 	}
 	
 
 
 //	public void downloadFolder(String folder) throws RemoteFileSystemException,
 //			VomsException {
 //
 //		try {
 //			final FileObject source = getUser().aquireFile(folder);
 //			final FileObject[] targets = source.findFiles(new FileTypeSelector(
 //					FileType.FILE));
 //
 //			FileObject target = null;
 //
 //			// for ( int i = 0; i<targets.length; i++ ) {
 //			//			
 //			// new Thread() {
 //			// public void run() {
 //			//					
 //			// FileObject target = null;
 //			// target.copyFrom(source[i]);
 //			//					
 //			//					
 //			// }
 //			// }.start();
 //			// }
 //			//		
 //			FileSystemManager fsManager = VFS.getManager();
 //			target = fsManager.resolveFile("file://tmp/test");
 //			Date startDate = new Date();
 //			myLogger.debug("Starting download...");
 //			target.copyFrom(source, new AllFileSelector());
 //			myLogger.debug("Finished download.");
 //			Date endDate = new Date();
 //			long difference = endDate.getTime() - startDate.getTime();
 //			System.out.println("Time to copy using folder copy: " + difference
 //					/ 1000 + " seconds.");
 //		} catch (Exception e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 //
 //	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#download(java.lang.String)
 	 */
 	public DataSource download(String filename)
 			throws RemoteFileSystemException, VomsException {
 
 		myLogger.debug("Downloading: " + filename);
 
 		return download(new String[] { filename })[0];
 	}
 	
 //	public byte[] downloadByteArray(String filename) throws RemoteFileSystemException, VomsException {
 //		
 //		myLogger.debug("Downloading: " + filename);
 //		byte[] result = null;
 //		FileObject source = null;
 ////		DataSource datasource = null;
 //		source = getUser().aquireFile(filename);
 //
 //		try {
 //			if (!source.exists()) {
 //				throw new RemoteFileSystemException(
 //						"Could not provide file: "
 //								+ filename
 //								+ " for download: InputFile does not exist.");
 //			}
 //
 //			byte[] buffer = new byte[1024];
 //
 //			InputStream inputStream = source.getContent().getInputStream();
 //			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 //			
 //		    try {
 //		        while (true) {
 //		          int amountRead;
 //				try {
 //					amountRead = inputStream.read(buffer);
 //				} catch (IOException e) {
 //					// TODO Auto-generated catch block
 //					e.printStackTrace();
 //					throw new RemoteFileSystemException("Can't read file "+filename+": "+e.getLocalizedMessage());
 //				}
 //		          if(amountRead == -1) {
 //		            break;
 //		          }
 //		          outputStream.write(buffer, 0, amountRead);
 //		        }
 //		      } finally {
 //		        try {
 //		          inputStream.close();
 //		        } catch(Exception ex) {
 //		        	ex.printStackTrace();
 //		        }
 //		        try {
 //		          outputStream.close();
 //		        } catch(Exception ex) {
 //		        	ex.printStackTrace();
 //		        }
 //		      }
 //
 //		      result = outputStream.toByteArray();
 ////			datasource = new FileContentDataSourceConnector(source
 ////					.getContent());
 //		} catch (FileSystemException e) {
 //			throw new RemoteFileSystemException(
 //					"Could not find or read file: " + filename + ": "
 //							+ e.getMessage());
 //		}
 //
 //		return result;
 //	}
 	
 //	/**
 //	 * Sean's C client calls this..
 //	 * <pre>
 //	 *   int offset = 0;
 //	 *   int length = 16kb
 //	 *   byte[] bytes = downloadByteArray(file, offset, length);
 //	 *   while (bytes.length > 0) { // or same as saying until EOF is not reached
 //	 *     //process bytes
 //	 *     offset += length;
 //	 *     bytes = downloadByteArray(file, offset, length);
 //	 *   }
 //	 * </pre>
 //	 * @param filename
 //	 * @param offset in bytes
 //	 * @param length in bytes
 //	 * @return
 //	 * @throws RemoteFileSystemException
 //	 * @throws VomsException
 //	 */
 //	public byte[] downloadByteArray(String filename, int offset, int length) throws RemoteFileSystemException, VomsException {
 //		
 //		// for the meantime, let's set a limit to length to be 1MB (1048576 bytes)
 //		// it should also be greater than 1kb
 //		// we should throw an exception if length exceeds the limit or just
 //		// return null. let's do the latter for now..
 //		
 //		int DOWNLOAD_LIMIT = 1048576; // put this in the correct place later on
 //		int BYTE_ARRAY_SIZE_LIMIT = 1024;
 //		
 //		if (length < BYTE_ARRAY_SIZE_LIMIT || length > DOWNLOAD_LIMIT) {
 //			return null;
 //		}		
 //		
 //		myLogger.debug("Downloading: " + filename);
 //		byte[] result = null;
 //		FileObject source = null;
 //		source = getUser().aquireFile(filename);
 //
 //		try {
 //			if (!source.exists()) {
 //				throw new RemoteFileSystemException(
 //						"Could not provide file: "
 //								+ filename
 //								+ " for download: InputFile does not exist.");
 //			}			
 //			
 //			byte[] buffer = new byte[BYTE_ARRAY_SIZE_LIMIT];
 //
 //			InputStream inputStream = source.getContent().getInputStream();
 //			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 //			
 //		    try {
 //		    	// assuming that a file is 35kb
 //		    	
 //		    	inputStream.skip(offset);
 //		    	
 //		    	for (int i = offset; i < offset + length; i+=BYTE_ARRAY_SIZE_LIMIT) {
 //		    		int amountRead;
 //		    		try {
 //		    			amountRead = inputStream.read(buffer);
 //		    		} catch (IOException e) {
 //		    			// TODO Auto-generated catch block
 //		    			e.printStackTrace();
 //		    			throw new RemoteFileSystemException("Can't read file "+filename+": "+e.getLocalizedMessage());
 //			        }
 //		    		if(amountRead == -1) {
 //		    			break;
 //			        }
 //		    		outputStream.write(buffer, 0, amountRead);
 //		    	}
 //		    } catch (IOException e ) {
 //		    	e.printStackTrace();
 //		    }	finally {
 //		    	try {
 //		    		inputStream.close();
 //		    	} catch(Exception ex) {
 //		    		ex.printStackTrace();
 //		    	}
 //		    	try {
 //		    		outputStream.close();
 //		    	} catch(Exception ex) {
 //		    		ex.printStackTrace();
 //		    	}
 //		    }
 //
 //		    result = outputStream.toByteArray();
 //		} catch (FileSystemException e) {
 //			throw new RemoteFileSystemException(
 //					"Could not find or read file: " + filename + ": "
 //							+ e.getMessage());
 //		}
 //
 //		return result;
 //	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#ls(java.lang.String, int,
 	 *      boolean)
 	 */
 	public Document ls(String directory, int recursion_level,
 			boolean return_absolute_url) throws RemoteFileSystemException {
 
 		// check whether credential still valid
 		getCredential();
 
 		Document result = null;
 		// FileObject dir = null;
 		// dir = getUser().aquireFile(directory);
 
 		myLogger.debug("Listing directory: " + directory
 				+ " with recursion level: " + recursion_level);
 
 		try {
 			result = getFsConverter().getDirectoryStructure(directory,
 					recursion_level, return_absolute_url);
 		} catch (Exception e) {
 			myLogger.error("Could not list directory: "
 					+ e.getLocalizedMessage());
 //			e.printStackTrace();
 			throw new RemoteFileSystemException("Could not read directory "
 					+ directory + " for ls command: " + e.getMessage());
 		}
 
 		try {
 			myLogger.debug(SeveralXMLHelpers.toString(result));
 		} catch (TransformerFactoryConfigurationError e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#ls_string(java.lang.String, int, boolean)
 	 */
 	public String ls_string(String directory, int recursion_level, boolean return_absolute_url) throws RemoteFileSystemException {
 		
 		String result = null;
 		try {
 			result = SeveralXMLHelpers.toString(ls(directory, recursion_level, return_absolute_url));
 		} catch (TransformerFactoryConfigurationError e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return result;
 		
 	}
 	
 	/**
 	 * This, well, creates a folder, as one might expect.
 	 * 
 	 * @param folder the folder.
 	 * @throws FileSystemException if the parent folder doesn't exist.
 	 */
 	private void createFolder(FileObject folder) throws FileSystemException {
 
 		if (!folder.getParent().exists()) {
 			createFolder(folder.getParent());
 		}
 
 		folder.createFolder();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#upload(javax.activation.DataSource,
 	 *      java.lang.String)
 	 */
 	public String upload(DataSource source, String filename,
 			boolean return_absolute_url) throws RemoteFileSystemException,
 			VomsException {
 
 		myLogger.debug("Receiving file: "+filename);
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
 
 			myLogger.debug("Calculated target: "+target.getName().toString());
 			
 			FileContent content = target.getContent();
 			fout = content.getOutputStream();
 		} catch (FileSystemException e) {
 //			e.printStackTrace();
 			throw new RemoteFileSystemException("Could not open file: "
 					+ filename + ":" + e.getMessage());
 		}
 
 		myLogger.debug("Receiving data for file: " + filename);
 
 		BufferedInputStream buf;
 		try {
 			buf = new BufferedInputStream(source.getInputStream());
 
 			byte[] buffer = new byte[1024];// byte buffer
 			int bytesRead = 0;
 			while (true) {
 				bytesRead = buf.read(buffer, 0, 1024);
 				// bytesRead returns the actual number of bytes read from
 				// the stream. returns -1 when end of stream is detected
 				if (bytesRead == -1)
 					break;
 				fout.write(buffer, 0, bytesRead);
 			}
 
 			if (buf != null)
 				buf.close();
 			if (fout != null)
 				fout.close();
 		} catch (IOException e) {
 			throw new RemoteFileSystemException("Could not write to file: "
 					+ filename + ": " + e.getMessage());
 		}
 
 		myLogger.debug("Data transmission for file "+filename+" finished.");
 		
 		buf = null;
 		fout = null;
 		if (!return_absolute_url)
 			return filename;
 		else
 			return target.getName().getURI();
 	}
 
 //	public String uploadByteArray(byte[] source, String filename,
 //			boolean return_absolute_url) throws RemoteFileSystemException,
 //			VomsException {
 //		
 //		// TODO: for the meantime we'll allow files to be uploaded directly without
 //		// breaking them up into pieces as long as they're less than 2MB
 //		int BYTE_ARRAY_SIZE_LIMIT = 2097152;
 //		if (source.length > BYTE_ARRAY_SIZE_LIMIT) {
 //			throw new RemoteFileSystemException("Source file too big to upload! Try uploading a file less than " + BYTE_ARRAY_SIZE_LIMIT + " bytes.");
 //		}
 //
 //		FileObject target = null;
 //
 //		OutputStream fout = null;
 //		try {
 //			String parent = filename.substring(0, filename
 //					.lastIndexOf(File.separator));
 //			FileObject parentObject = getUser().aquireFile(parent);
 //			// FileObject tempObject = parentObject;
 //
 //			createFolder(parentObject);
 //			// parentObject.createFolder();
 //
 //			target = getUser().aquireFile(filename);
 //			// just to be sure that the folder exists.
 //
 //			FileContent content = target.getContent();
 //			fout = content.getOutputStream();
 //		} catch (FileSystemException e) {
 //			e.printStackTrace();
 //			throw new RemoteFileSystemException("Could not open file: "
 //					+ filename + ":" + e.getMessage());
 //		}
 //
 //		myLogger.debug("Receiving data for file: " + filename);
 //
 //		BufferedInputStream buf;
 //		try {
 //			buf = new BufferedInputStream(new ByteArrayInputStream(source));
 //
 //			byte[] buffer = new byte[1024];// byte buffer
 //			int bytesRead = 0;
 //			while (true) {
 //				bytesRead = buf.read(buffer, 0, 1024);
 //				// bytesRead returns the actual number of bytes read from
 //				// the stream. returns -1 when end of stream is detected
 //				if (bytesRead == -1)
 //					break;
 //				fout.write(buffer, 0, bytesRead);
 //			}
 //
 //			if (buf != null)
 //				buf.close();
 //			if (fout != null)
 //				fout.close();
 //		} catch (IOException e) {
 //			throw new RemoteFileSystemException("Could not write to file: "
 //					+ filename + ": " + e.getMessage());
 //		}
 //
 //		buf = null;
 //		fout = null;
 //		if (!return_absolute_url)
 //			return filename;
 //		else
 //			return target.getName().getURI();
 //	}
 	
 	
 //	/**
 //	 * 
 //	 * 
 //	 * 
 //	 * @param source
 //	 * @param filename
 //	 * @param return_absolute_url
 //	 * @param n
 //	 * @return
 //	 * @throws RemoteFileSystemException
 //	 * @throws VomsException
 //	 */
 //	public String uploadByteArray(byte[] source, String filename,
 //			boolean return_absolute_url, int offset, int length) throws RemoteFileSystemException,
 //			VomsException {
 //
 //		int UPLOAD_LIMIT = 1048576; // put this in the correct place later on
 //		int BYTE_ARRAY_SIZE_LIMIT = 1024;
 //		
 //		// make sure the user is not transferring a very big byte[]
 //		if (length > UPLOAD_LIMIT) {
 //			return null;
 //		}
 //		
 //		FileObject target = null;
 //
 //		//OutputStream fout = null;
 //		RandomAccessContent fout = null;
 //		
 //		try {
 //			String parent = filename.substring(0, filename
 //					.lastIndexOf(File.separator));
 //			FileObject parentObject = getUser().aquireFile(parent);
 //			// FileObject tempObject = parentObject;
 //
 //			createFolder(parentObject);
 //			// parentObject.createFolder();
 //
 //			target = getUser().aquireFile(filename);
 //			// just to be sure that the folder exists.
 //
 //			FileContent content = target.getContent();
 //			//fout = content.getOutputStream();
 //			fout = content.getRandomAccessContent(RandomAccessMode.READWRITE);
 //			
 //		} catch (FileSystemException e) {
 //			e.printStackTrace();
 //			throw new RemoteFileSystemException("Could not open file: "
 //					+ filename + ":" + e.getMessage());
 //		}
 //
 //		myLogger.debug("Receiving data for file: " + filename);
 //
 //		ByteArrayInputStream buf;
 //		try {
 //			buf = new ByteArrayInputStream(source, 0, length);
 //
 //			byte[] buffer = new byte[BYTE_ARRAY_SIZE_LIMIT];// byte buffer
 //			int bytesRead = 0;
 //			fout.seek(offset);
 //			while (true) {
 //				bytesRead = buf.read(buffer);
 //				if (bytesRead == -1)
 //					break;
 //				fout.write(buffer, 0, bytesRead);
 //	    		
 //			}
 //
 //			if (buf != null)
 //				buf.close();
 //			if (fout != null)
 //				fout.close();
 //		} catch (IOException e) {
 //			throw new RemoteFileSystemException("Could not write to file: "
 //					+ filename + ": " + e.getMessage());
 //		}
 //
 //		buf = null;
 //		fout = null;
 //		if (!return_absolute_url)
 //			return filename;
 //		else
 //			return target.getName().getURI();
 //	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#getJobDetails(java.lang.String)
 	 */
 	public Document getJobDetails(String jobname) throws NoSuchJobException {
 
 		getJobStatus(jobname);
 
 		Job job;
 		job = jobdao.findJobByDN(getUser().getDn(), jobname);
 
 		Document info = JobsToXMLConverter.getDetailedJobInformation(job);
 		return info;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getJobDetails_string(java.lang.String)
 	 */
 	public String getJobDetails_string(String jobname) throws NoSuchJobException {
 		
 		String result = null;
 		
 		try {
 			result = SeveralXMLHelpers.toString(getJobDetails(jobname));
 		} catch (TransformerFactoryConfigurationError e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return result;
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getFqans()
 	 */
 	public String[] getFqans() {
 
 		if (currentFqans == null) {
 
 			getUser().fillFqans();
 			// TODO store it in database
 			// userdao.attachDirty(getUser());
 			Collection<String> fqans = getUser().getFqans().keySet();
 
 			currentFqans = fqans.toArray(new String[fqans.size()]);
 		}
 		return currentFqans;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getDN()
 	 */
 	public String getDN() {
 		return getUser().getDn();
 	}
 
 	/**
 	 * Fixes the path to the working directory. A user (or grisu-client) would
 	 * set something like grisu-jobs/diff-job as working directory. The real
 	 * working directory on the cluster would probably be something like
 	 * <dn_of_the_user>/grisu-jobs/diff-job, depending on the fqan the job is
 	 * submitted with
 	 * 
 	 * @param job
 	 *            the job
 	 * @param dn
 	 *            the dn which is used to submit the job.
 	 * @param absoluteUrl
 	 *            whether the path of the working directory is absolute or
 	 *            relative
 	 * @throws ServerJobSubmissionException
 	 *             if something goes wrong
 	 */
 	protected Document recalculateWorkingDirectory(Job job,
 			String clusterRootUrl, boolean absolutePath)
 			throws ServerJobSubmissionException {
 
 		// set working directory
 		try {
 			myLogger
 					.debug("Recalculating work directory for cluster root url: "
 							+ clusterRootUrl);
 			Document new_jsdl = JsdlModifier.recalculateFileSystems(job
 					.getJobDescription(), clusterRootUrl.replace(":2811", ""),
 					absolutePath);
 			return new_jsdl;
 		} catch (Exception e) {
 //			e.printStackTrace();
 			throw new ServerJobSubmissionException("Could not recalculate directory.");
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getAllSites()
 	 */
 	public String[] getAllSites() {
 
 		// if ( ServerPropertiesManager.getMDSenabled() ) {
 		return informationManager.getAllSites();
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
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#addJobProperty(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	public void addJobProperty(String jobname, String key, String value)
 			throws NoSuchJobException {
 
 		Job job = getJob(jobname);
 
 		job.addJobProperty(key, value);
 		jobdao.attachDirty(job);
 
 		myLogger.debug("Added job property: " + key);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#addJobProperties(java.lang.String, java.util.Map)
 	 */
 	public void addJobProperties(String jobname, Map<String, String> properties)
 			throws NoSuchJobException {
 
 		Job job = getJob(jobname);
 
 		job.addJobProperties(properties);
 		jobdao.attachDirty(job);
 
 		myLogger.debug("Added " + properties.size() + " job properties.");
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getAllJobProperties(java.lang.String)
 	 */
 	public Map<String, String> getAllJobProperties(String jobname)
 			throws NoSuchJobException {
 
 		Job job = getJob(jobname);
 
 		job.getJobProperties().put(JobConstants.JOBPROPERTYKEY_VO, job.getFqan());
 		job.getJobProperties().put(JobConstants.JOBPROPERTYKEY_JOBDIRECTORY, getJobDirectory(jobname));
 		job.getJobProperties().put(JobConstants.JOBPROPERTYKEY_HOSTNAME, job.getSubmissionHost());
 
 		job.getJobProperties().put(JobConstants.JOBPROPERTYKEY_STATUS,
 				JobConstants.translateStatus(getJobStatus(jobname)));
 
 		return job.getJobProperties();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getJobProperty(java.lang.String, java.lang.String)
 	 */
 	public String getJobProperty(String jobname, String key)
 			throws NoSuchJobException {
 
 		Job job = getJob(jobname);
 
 		return job.getJobProperty(key);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getJobFqan(java.lang.String)
 	 */
 	public String getJobFqan(String jobname) throws NoSuchJobException {
 		Job job = getJob(jobname);
 
 		return job.getFqan();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#isFolder(java.lang.String)
 	 */
 	public boolean isFolder(String file) throws RemoteFileSystemException,
 			VomsException {
 
 		boolean isFolder;
 		try {
 			isFolder = (getUser().aquireFile(file).getType() == FileType.FOLDER);
 		} catch (Exception e) {
 			myLogger.error("Couldn't access file: "+file+" to check whether it is a folder."+e.getLocalizedMessage());
 //			e.printStackTrace();
 			// try again. sometimes it works the second time...
 			try {
 				myLogger.debug("trying a second time...");
 				isFolder = (getUser().aquireFile(file).getType() == FileType.FOLDER);
 			} catch (Exception e2) {
 //				e2.printStackTrace();
 				myLogger.error("Again couldn't access file: "+file+" to check whether it is a folder."+e.getLocalizedMessage());
 				throw new RemoteFileSystemException("Could not aquire file: "
 						+ file);
 			}
 		}
 
 		return isFolder;
 
 	}
 	
 	public boolean fileExists(String file) throws RemoteFileSystemException, VomsException {
 		
 		boolean exists;
 		
 		try {
 			exists = getUser().aquireFile(file).exists();
 			return exists;
 		} catch (FileSystemException e) {
 
 			throw new RemoteFileSystemException("Could not connect to filesystem to aquire file: "+ file);
 			
 		}
 		
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getChildrenFiles(java.lang.String, boolean)
 	 */
 	public String[] getChildrenFiles(String folder, boolean onlyFiles)
 			throws RemoteFileSystemException, VomsException {
 
 		String[] result = null;
 		try {
 			FileObject[] objects = null;
 			if (onlyFiles)
 				objects = getUser().aquireFile(folder).findFiles(
 						new FileTypeSelector(FileType.FILE));
 			else
 				objects = getUser().aquireFile(folder).findFiles(
 						new AllFileSelector());
 
 			result = new String[objects.length];
 			for (int i = 0; i < objects.length; i++) {
 				result[i] = objects[i].getName().getURI();
 			}
 
 		} catch (FileSystemException e) {
 			throw new RemoteFileSystemException("Could not access folder: "
 					+ folder + ": " + e.getMessage());
 		}
 
 		return result;
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getFileSize(java.lang.String)
 	 */
 	public long getFileSize(String file) throws RemoteFileSystemException,
 			VomsException {
 
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
 
 	// public String getExecutionFileSystem(String site) {
 	//		
 	// String basePath = MountPointManager.getDefaultFileSystem(site);
 	//		
 	// // String
 	//		
 	// return null;
 	// }
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#calculateAbsoluteJobDirectory(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	public String calculateAbsoluteJobDirectory(String jobname, String subLoc,
 			String fqan) {
 
 		myLogger.debug("Calculating jobdirectory of jobname: " + jobname
 				+ ", submissionLocation: " + subLoc + " and fqan: " + fqan);
 		// gsiftp://ngdata.vpac.org/markus
 		// String executionHostFs =
 		// MountPointManager.getDefaultFileSystem(site);
 		String[] stagingFilesystems = getStagingFileSystemForSubmissionLocation(subLoc);
 
 		MountPoint mountpoint = null;
 		for (String fs : stagingFilesystems) {
 			mountpoint = getUser().getFirstResponsibleMountPointForHostAndFqan(
 					fs, fqan);
 			if (mountpoint != null)
 				break;
 		}
 
 		if (mountpoint == null) {
 			return null;
 		}
 		myLogger.debug("Responsible mountpoint: " + mountpoint.getMountpoint());
 		String jobDir = getWorkingDirectoryRelativeToMountPoint(jobname);
 		myLogger.debug("Jobdirectory: " + jobDir);
 		return mountpoint.getRootUrl() + "/" + jobDir;
 
 	}
 
 	/**
 	 * Calculates the working directory relative to a MountPoint. Since grisu-backends can be configured
 	 * (and in fact should) to have an extra directory for all the grisu jobs (so the users directory doesn't get cluttered
 	 * with jobdirectories) this is needed. It would, for instance, return something like: "grisu-job-dir/testjob"
 	 * 
 	 * @param jobname the name of the job
 	 * @return the directory relative to a mountpoint root
 	 */
 	protected String getWorkingDirectoryRelativeToMountPoint(String jobname) {
 
 		String jobSubDir = ServerPropertiesManager.getGrisuJobDirectoryName();
 		String replacement = null;
 		if (jobSubDir == null) {
 			replacement = jobname;
 		} else {
 			// don't use File.seperator here because this could run on a windows machine
 			replacement = jobSubDir + "/" + jobname;
 		}
 		return replacement;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getJobDirectory(java.lang.String)
 	 */
 	public String getJobDirectory(String jobname) throws NoSuchJobException {
 
 		Job job = getJob(jobname);
 
 		// test whether job exists
 		if (job == null) {
 			return null;
 		}
 		// if (job.getStatus() < 0)
 		// return null;
 
 		// String fqan = job.getFqan();
 
 		String relativeDir = calculateRelativeJobDirectory(jobname);
 		String jobfs = job.getJobProperty("executionHostFileSystem");
 		if (jobfs == null)
 			return null;
 
 		return jobfs + "/" + relativeDir;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getDataLocationsForVO(java.lang.String)
 	 */
 	public Map<String, String[]> getDataLocationsForVO(String fqan) {
 
 		return informationManager.getDataLocationsForVO(fqan);
 
 		// if ( ServerPropertiesManager.getMDSenabled() ) {
 		// return MountPointManager.getDataLocationsForSiteAndVO(site, fqan);
 		// } else {
 		// return null;
 		// }
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#calculateRelativeJobDirectory(java.lang.String)
 	 */
 	public String calculateRelativeJobDirectory(String jobname) {
 
 		// Job job = getJob(jobname);
 		String jobDir = null;
 
 		jobDir = getWorkingDirectoryRelativeToMountPoint(jobname);
 
 		return jobDir;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#lastModified(java.lang.String)
 	 */
 	public long lastModified(String url) throws RemoteFileSystemException,
 			VomsException {
 
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
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#mkdir(java.lang.String)
 	 */
 	public boolean mkdir(String url) throws RemoteFileSystemException,
 			VomsException {
 		myLogger.debug("Creating folder: "+url+"...");
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
 			throw new RemoteFileSystemException("Could not create directory: "
 					+ url);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#deleteFile(java.lang.String)
 	 */
 	public void deleteFile(String file) throws RemoteFileSystemException,
 			VomsException {
 
 		FileObject fileObject = getUser().aquireFile(file);
 		try {
 			if (fileObject.exists()) {
 				fileObject.delete(new AllFileSelector());
 			}
 		} catch (FileSystemException e) {
 			// TODO Auto-generated catch block
 //			e.printStackTrace();
 			throw new RemoteFileSystemException("Could not delete file: "
 					+ file);
 		}
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#deleteFiles(java.lang.String[])
 	 */
 	public void deleteFiles(String[] files) throws RemoteFileSystemException,
 			VomsException {
 
 		// ArrayList<String> filesNotDeleted = new ArrayList<String>();
 		for (String file : files) {
 			try {
 				deleteFile(file);
 			} catch (Exception e) {
 				myLogger.error("Could not delete file: " + file);
 				// filesNotDeleted.add(file);
 			}
 		}
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getUserProperty(java.lang.String)
 	 */
 	public String[] getUserProperty(String key) {
 
 		List<String> values = getUser().getUserProperties().get(key);
 
 		if (values == null || values.size() == 0) {
 			return null;
 		} else {
 			return values.toArray(new String[] {});
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#submitSupportRequest(java.lang.String, java.lang.String)
 	 */
 	public void submitSupportRequest(String subject, String description) {
 
 		// TODO
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getMessagesSince(java.util.Date)
 	 */
 	public Document getMessagesSince(Date date) {
 
 		// TODO
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#stageFiles(java.lang.String)
 	 */
 	public void stageFiles(String jobname) throws RemoteFileSystemException,
 			VomsException, NoSuchJobException {
 
 		Job job;
 		job = jobdao.findJobByDN(getUser().getDn(), jobname);
 
 		List<Element> stageIns = JsdlHelpers.getStageInElements(job
 				.getJobDescription());
 
 		for (Element stageIn : stageIns) {
 
 			String sourceUrl = JsdlHelpers.getStageInSource(stageIn);
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
 					throw new RemoteFileSystemException(
 							"Could not create parent folder for file: "
 									+ targetUrl + ": " + e.getMessage());
 				}
 				myLogger.debug("Staging file: " + sourceUrl + " to: "
 						+ targetUrl);
 				cp(sourceUrl, targetUrl, true, true);
 				job.addInputFile(targetUrl);
 			}
 			// }
 		}
 	}
 
 	/**
 	 * Prepares the environment for the job. Mainly it creates the job directory remotely.
 	 * @param job the name of the job
 	 * @throws RemoteFileSystemException if the job directory couldn't be created 
 	 * @throws VomsException if there was a permission problem with the job directory
 	 */
 	protected void prepareJobEnvironment(Job job)
 			throws RemoteFileSystemException, VomsException {
 
 		String jobDir = JsdlHelpers.getAbsoluteWorkingDirectoryUrl(job
 				.getJobDescription());
 
 		myLogger.debug("Using calculated jobdirectory: " + jobDir);
 
 		job.setJob_directory(jobDir);
 
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
 					"Could not create job output folder: "
 							+ job.getJob_directory());
 		}
 		// now after the jsdl is ready, don't forget to fill the required fields
 		// into the database
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.ServiceInterface#submitJob(java.lang.String)
 	 */
 	public void submitJob(String jobname, String fqan)
 			throws ServerJobSubmissionException, NoValidCredentialException,
 			RemoteFileSystemException, VomsException, NoSuchJobException {
 
 		Job job = getJob(jobname);
 
 		// change paths within the job description to adapt to site's
 		// environment
 		String submissionFS = JsdlHelpers.getUserExecutionHostFs(job
 				.getJobDescription());
 		// // String submissionSite = MountPointManager.getSite(submissionFS);
 		String submissionSite = informationManager
 				.getSiteForHostOrUrl(submissionFS);
 		String clusterRootUrl = null;
 
 		// try {
 		Map<String, String[]> fileSystems = getDataLocationsForVO(fqan);
 
 		boolean absolutePath;
 		// String[] fileSystems =
 		// getStagingFileSystemForSubmissionLocation(JsdlHelpers.getCandidateHosts(job.getJobDescription())[0]);
 		if (fileSystems == null || fileSystems.keySet().size() == 0) {
 			absolutePath = false;
 			// this is a fallback if mds doesn't contain the staging filesystem
 			FileObject submissionFsFileObject = getUser().aquireFile(
 					submissionFS);
 			FileObject clusterRootFileObject;
 			try {
 				clusterRootFileObject = submissionFsFileObject.getFileSystem()
 						.resolveFile(
 								(String) submissionFsFileObject.getFileSystem()
 										.getAttribute("HOME_DIRECTORY"));
 			} catch (FileSystemException e) {
 				throw new ServerJobSubmissionException(
 						"Could not find any root directory/filesystem for this job submission.");
 			}
 			clusterRootUrl = clusterRootFileObject.getName().toString();
 		} else {
 			absolutePath = true;
 			for (String fs : fileSystems.keySet()) {
 				// for ( String fsHome : fileSystems.get(fs) ) {
 				// String compare = (fs+fsHome).replace(":2811", "");
 				if (submissionFS.startsWith(fs.replace(":2811", ""))) {
 					clusterRootUrl = fs.replace(":2811", "");
 					break;
 				}
 				// }
 			}
 
 		}
 		if (clusterRootUrl == null) {
 			throw new ServerJobSubmissionException(
 					"Could not calculate cluster root directory/filesystem.");
 		}
 
 		// FileObject clusterRootFileObject =
 		// submissionFsFileObject.getFileSystem().resolveFile((String)
 		// submissionFsFileObject.getFileSystem().getAttribute("HOME_DIRECTORY"));
 		// clusterRootUrl = clusterRootFileObject.getName().toString();
 		// } catch (FileSystemException e) {
 		// throw new JobSubmissionException("Could not determine cluster root
 		// url.");
 		// }
 
 		String workingDirRelativeToUserFS = JsdlHelpers.getWorkingDirectory(job
 				.getJobDescription());
 		job.setJob_directory(submissionFS + "/" + workingDirRelativeToUserFS);
 		job.getJobProperties().put("jobDirectory", workingDirRelativeToUserFS);
 
 		Document newJsdl = recalculateWorkingDirectory(job, clusterRootUrl,
 				absolutePath);
 		job.setJobDescription(newJsdl);
 
 		// create job folder
 		prepareJobEnvironment(job);
 		// stage files
 		stageFiles(jobname);
 		// fill necessary info about the job into the database
 		parseJobDescription(job);
 
 		job.setFqan(fqan);
 		if (fqan != null) {
 			VO vo = VOManagement.getVO(getUser().getFqans().get(fqan));
 			job.setCredential(CertHelpers.getVOProxyCredential(vo,
 					fqan, getCredential()));
 		} else {
 			job.setCredential(getCredential());
 		}
 
 		String handle = null;
 		handle = getSubmissionManager().submit("GT4", job);
 
 		if (handle == null) {
 			throw new ServerJobSubmissionException(
 					"Job apparently submitted but jobhandle is null for job: "
 							+ jobname);
 		}
 
 		job.addJobProperty("submissionTime", Long.toString(new Date().getTime()));
 		job.addJobProperty("submissionSite", submissionSite);
 		// we don't want the credential to be stored with the job in this case
 		// TODO or do we want it to be stored?
 		job.setCredential(null);
 		jobdao.attachDirty(job);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#kill(java.lang.String, boolean)
 	 */
 	public void kill(String jobname, boolean clear)
 			throws RemoteFileSystemException, VomsException, NoSuchJobException {
 
 		Job job;
 
 		job = jobdao.findJobByDN(getUser().getDn(), jobname);
 
 		kill(jobname);
 
 		if (clear) {
 
 			if (job.getJob_directory() != null) {
 
 				try {
 					FileObject jobDir = getUser().aquireFile(
 							job.getJob_directory());
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
 
 		if (clear)
 			jobdao.delete(job);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#cp(java.lang.String, java.lang.String, boolean, boolean)
 	 */
 	public String cp(String source, String target, boolean overwrite,
 			boolean return_absolute_url) throws RemoteFileSystemException,
 			VomsException {
 
 		FileObject source_file = null;
 		FileObject target_file = null;
 
 		try {
 			source_file = getUser().aquireFile(source);
 			target_file = getUser().aquireFile(target);
 
 			if (source_file.getName().getURI().equals(
 					target_file.getName().getURI())) {
 				myLogger
 						.debug("Input file and target file are the same. No need to copy...");
 				if (return_absolute_url)
 					return target_file.getName().getURI();
 				else
 					return getUser().returnUserSpaceUrl(
 							target_file.getName().getURI());
 			}
 
 			if (!source_file.exists()) {
 				throw new RemoteFileSystemException("Could not copy file: "
 						+ source + ": " + "InputFile does not exist.");
 			}
 
 			if (!overwrite && target_file.exists()) {
 				throw new RemoteFileSystemException("Could not copy to file: "
 						+ target + ": " + "InputFile exists.");
 			} else if (target_file.exists()) {
 				if (!target_file.delete()) {
 					throw new RemoteFileSystemException(
 							"Could not copy to file: " + target + ": "
 									+ "Could not delete target file.");
 				}
 			}
 			myLogger.debug("Copying: " + source_file.getName().toString()
 					+ " to: " + target_file.getName().toString());
 			target_file.copyFrom(source_file, new AllFileSelector());
 
 			if (!target_file.exists()) {
 				throw new RemoteFileSystemException("Could not copy file: "
 						+ source + " to: " + target
 						+ ": target file does not exist after copying.");
 			}
 		} catch (FileSystemException e) {
 			throw new RemoteFileSystemException("Could not copy \"" + source
 					+ "\" to \"" + target + "\": " + e.getMessage());
 		}
 
 		if (return_absolute_url)
 			return target_file.getName().getURI();
 		else
 			return getUser().returnUserSpaceUrl(target_file.getName().getURI());
 	}
 
 
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getAllSubmissionLocations()
 	 */
 	public String[] getAllSubmissionLocations() {
 
 		return informationManager.getAllSubmissionLocations();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getSubmissionLocationsForApplication(java.lang.String)
 	 */
 	public String[] getSubmissionLocationsForApplication(String application) {
 
 		return informationManager
 				.getAllSubmissionLocationsForApplication(application);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getSubmissionLocationsForApplication(java.lang.String, java.lang.String)
 	 */
 	public String[] getSubmissionLocationsForApplication(String application,
 			String version) {
 
 		return informationManager.getAllSubmissionLocations(application, version);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getVersionsOfApplicationOnSite(java.lang.String, java.lang.String)
 	 */
 	public String[] getVersionsOfApplicationOnSite(String application,
 			String site) {
 
 		return informationManager.getVersionsOfApplicationOnSite(application,
 				site);
 
 	}
 	
 	public String[] getVersionsOfApplicationOnSubmissionLocation(String application, String submissionLocation) {
 		return informationManager.getVersionsOfApplicationOnSubmissionLocation(application, submissionLocation);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getSubmissionLocationsPerVersionOfApplication(java.lang.String)
 	 */
 	public Map<String, String> getSubmissionLocationsPerVersionOfApplication(
 			String application) {
 //		if (ServerPropertiesManager.getMDSenabled()) {
 		myLogger.debug("Getting map of submissionlocations per version of application for: "+application);
 			Map<String, String> appVersionMap = new HashMap<String, String>();
 			String[] versions = informationManager
 					.getAllVersionsOfApplicationOnGrid(application);
 			for (int i = 0; versions != null && i < versions.length; i++) {
 				String[] submitLocations = null;
 				try {
 					submitLocations = getSubmissionLocationsForApplication(
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
 						if (j < submitLocations.length - 1)
 							submitLoc.append(",");
 					}
 				}
 				appVersionMap.put(versions[i], submitLoc.toString());
 			}
 			return appVersionMap;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getSubmissionLocationsForApplication(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	public String[] getSubmissionLocationsForApplication(String application,
 			String version, String fqan) {
 		// TODO implement a method which takes in fqan later on
 
 		return informationManager.getAllSubmissionLocations(application, version);
 
 		// if ( ServerPropertiesManager.getMDSenabled() ) {
 		// return QueueManager.getAllSubmissionQueuesFromMDS(application,
 		// version);
 		// } else {
 		// return null;
 		// }
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getSite(java.lang.String)
 	 */
 	public String getSite(String host_or_url) {
 
 		return informationManager.getSiteForHostOrUrl(host_or_url);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getAllHosts()
 	 */
 	public Map<String, String> getAllHosts() {
 
 		return informationManager.getAllHosts();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getAllSubmissionLocations(java.lang.String)
 	 */
 	public String[] getAllSubmissionLocations(String fqan) {
 
 		return informationManager.getAllSubmissionLocationsForVO(fqan);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getApplicationDetails(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	public Map<String, String> getApplicationDetails(String application,
 			String version, String site_or_submissionLocation) {
 
 		String site = site_or_submissionLocation;
 		if ( isSubmissionLocation(site_or_submissionLocation) ) {
 			myLogger.debug("Parameter "+site_or_submissionLocation+"is submission location not site. Calculating site...");
 			site = getSiteForSubmissionLocation(site_or_submissionLocation);
 			myLogger.debug("Site is: "+site);
 		}
 		
 		return informationManager.getApplicationDetails(application, version,
 				site);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getApplicationDetails(java.lang.String, java.lang.String)
 	 */
 	public Map<String, String> getApplicationDetails(String application,
 			String site_or_submissionLocation) {
 		
 		String site = site_or_submissionLocation;
 		if ( isSubmissionLocation(site_or_submissionLocation) ) {
 			myLogger.debug("Parameter "+site_or_submissionLocation+"is submission location not site. Calculating site...");
 			site = getSiteForSubmissionLocation(site_or_submissionLocation);
 			myLogger.debug("Site is: "+site);
 		}
 
 		return getApplicationDetails(application,
 				getDefaultVersionForApplicationAtSite(application, site), site);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getAllAvailableApplications(java.lang.String[])
 	 */
 	public String[] getAllAvailableApplications(String[] sites) {
 		Set<String> siteList = new TreeSet<String>();
 
 		if (sites == null) {
 			return informationManager.getAllApplicationsOnGrid();
 		}
 		for (int i = 0; i < sites.length; i++) {
 			siteList.addAll(Arrays.asList(informationManager
 					.getAllApplicationsAtSite(sites[i])));
 		}
 		return siteList.toArray(new String[] {});
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.ServiceInterface#getStagingFileSystemForSubmissionLocation(java.lang.String)
 	 */
 	public String[] getStagingFileSystemForSubmissionLocation(String subLoc) {
 		return informationManager
 				.getStagingFileSystemForSubmissionLocation(subLoc);
 	}
 
 	/**
 	 * Calculates the default version of an application on a site. This is pretty hard to do, so, if you
 	 * call this method, don't expect anything that makes 100% sense, I'm afraid.
 	 * @param application the name of the application
 	 * @param site the site
 	 * @return the default version of the application on this site
 	 */
 	private String getDefaultVersionForApplicationAtSite(String application,
 			String site) {
 
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
 	 * Tests whether the provided String is a valid submissionLocation. All this does at the moment is to check
 	 * whether there is a ":" within the string, so don't depend with your life on the answer to this question...
 	 * 
 	 * @param submissionLocation the submission location
 	 * @return whether the string is a submission location or not
 	 */
 	public boolean isSubmissionLocation(String submissionLocation) {
 		
 		if ( submissionLocation.indexOf(":") >= 0 ) {
 			return true;
 		} else {
 			return false;
 		}
 		
 	}
 	
 	
 	/**
 	 * Returns the name of the site for the give submissionLocation.
 	 * @param subLoc the submissionLocation
 	 * @return the name of the site for the submissionLocation or null, if the site can't be found
 	 */
 	public String getSiteForSubmissionLocation(String subLoc) {
 		
 		// subLoc = queuename@cluster:contactstring#JobManager
 //		String queueName = subLoc.substring(0, subLoc.indexOf(":"));
 		String contactString = "";
 		if (subLoc.indexOf("#") > 0) {
 			contactString = subLoc.substring(subLoc.indexOf(":") + 1, subLoc
 					.indexOf("#"));
 		} else {
 			contactString = subLoc.substring(subLoc.indexOf(":") + 1);
 		}
 		
 		return getSite(contactString);
 	}
 	
 	public String getCurrentStatusMessage() {
 		return "Backend status report not implemented yet.";
 	}
 
 	/**
 	 * This method has to be implemented by the endpoint specific ServiceInterface. Since there are a few different ways to 
 	 * get a proxy credential (myproxy, just use the one in /tmp/x509..., shibb,...) this needs to be implemented differently
 	 * for every single situation.
 	 * 
 	 * @return the proxy credential that is used to contact the grid
 	 */
 	abstract protected ProxyCredential getCredential()
 			throws NoValidCredentialException;
 
 }
