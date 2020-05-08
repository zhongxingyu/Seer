 package grisu.control;
 
 import grisu.control.exceptions.BatchJobException;
 import grisu.control.exceptions.JobPropertiesException;
 import grisu.control.exceptions.JobSubmissionException;
 import grisu.control.exceptions.NoSuchJobException;
 import grisu.control.exceptions.NoSuchTemplateException;
 import grisu.control.exceptions.NoValidCredentialException;
 import grisu.control.exceptions.RemoteFileSystemException;
 import grisu.jcommons.constants.Constants;
 import grisu.model.MountPoint;
 import grisu.model.dto.*;
 import grisu.model.info.dto.Application;
 import grisu.model.info.dto.DtoProperties;
 import grisu.model.info.dto.DtoStringList;
 import grisu.model.info.dto.JobQueueMatch;
 import grisu.model.info.dto.Package;
 import grisu.model.info.dto.Queue;
 import grisu.model.info.dto.Site;
 import grisu.model.info.dto.Version;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.annotation.security.PermitAll;
 import javax.annotation.security.RolesAllowed;
 import javax.jws.WebService;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.xml.bind.annotation.XmlMimeType;
 
 /**
  * This is the central interface of grisu. These are the methods the web service
  * provices for the clients to access. I tried to keep the number of methods as
  * small as possible but I'm sure I'll have to add a few methods in the future.
  *
  * @author Markus Binsteiner
  *
  */
 
 @WebService(targetNamespace = "http://api.grisu", serviceName = "GrisuService")
 public interface ServiceInterface {
 
 	public static final int API_VERSION = 17;
 
 	public static final String VIRTUAL_GRID_PROTOCOL_NAME = "grid";
 	public static String GRISU_JOB_FILE_NAME = ".grisujob";
 	public static String GRISU_BATCH_JOB_FILE_NAME = ".grisubatchjob";
 
     public static final String LAST_INFO_UPDATE_KEY = "lastInfoUpdate";
     public static final String LAST_CONFIG_UPDATE_KEY = "lastConfigUpdate";
     public static final String LAST_TEMPLATES_UPDATE_KEY = "lastTemplatesUpdate";
 
 
 
     /**
 	 * Adds an archive location.
 	 *
 	 * @param alias
 	 *            the alias of the archive location
 	 *
 	 * @param value
 	 *            the url of the archive location (or null to delete an existing
 	 *            one)
 	 */
 	@RolesAllowed("User")
 	@PUT
 	@Path("/user/archive/{alias}")
 	void addArchiveLocation(@PathParam("alias") String alias,
 			@QueryParam("value") String value);
 
 	/**
 	 * Adds a bookmarks.
 	 *
 	 * @param alias
 	 *            the alias of the bookmark
 	 *
 	 * @param value
 	 *            the url of the bookmark (or null to delete an existing one)
 	 */
 	@RolesAllowed("User")
 	@PUT
 	@Path("/user/bookmarks/{alias}")
 	void addBookmark(@PathParam("alias") String alias,
 			@QueryParam("value") String value);
 
 	/**
 	 * Adds multiple job propeties in one go.
 	 *
 	 * @param jobname
 	 *            the name of the job
 	 * @param properties
 	 *            the properties you want to connect to the job
 	 * @throws NoSuchJobException
 	 *             if there is no job with this jobname in the database
 	 */
 	@PUT
 	@Path("/job/{jobname}/property/")
 	@RolesAllowed("User")
 	void addJobProperties(@PathParam("jobname") String jobname,
 			@QueryParam("properties") DtoJob properties)
 					throws NoSuchJobException;
 
 	/**
 	 * If you want to store certain values along with the job which can be used
 	 * after the job is finished. For example the name of an output directory
 	 * that is stored in one of the input files. That way you don't have to
 	 * download the input file again and parse it.
 	 *
 	 * @param jobname
 	 *            the name of the job
 	 * @param key
 	 *            the key for the value you want to add
 	 * @param value
 	 *            the value
 	 * @throws NoSuchJobException
 	 *             if there is no job with this jobname in the database
 	 */
 	@PUT
 	@Path("/job/{jobname}/property/{key}")
 	@RolesAllowed("User")
 	void addJobProperty(@PathParam("jobname") String jobname,
 			@PathParam("key") String key, @QueryParam("value") String value)
 					throws NoSuchJobException;
 
 	/**
 	 * Adds the specified job to the mulitpartJob.
 	 *
 	 * @param batchjobname
 	 *            the batchjobname
 	 * @param jobdescription
 	 *            the jsdl string
 	 *
 	 * @return the new child-job name
 	 */
 	@POST
 	@Path("/batchjob/{batchjobname}/add")
 	@RolesAllowed("User")
 	@Produces("text/plain")
 	String addJobToBatchJob(@PathParam("batchjobname") String batchjobname,
 			@QueryParam("jsdl") String jobdescription)
 					throws NoSuchJobException, JobPropertiesException;
 
 	/**
 	 * Admin method for stuff like reloading config, vos and such...
 	 *
 	 * @param command
 	 *            the command
 	 * @param config
 	 *            the config for the command
 	 * @return the command output
 	 */
 	DtoStringList admin(String command, DtoProperties config);
 
 	/**
 	 * Archives this job to the specified url and deletes it from the database.
 	 *
 	 * If target is null, the user property
 	 * {@link Constants#DEFAULT_JOB_ARCHIVE_LOCATION} is used. This operation
 	 * will be executed in the background, you can query its status using the
 	 * {@link #getActionStatus(String)} using the return value as handle.
 	 *
 	 * The default {@link Constants#DEFAULT_JOB_ARCHIVE_LOCATION} can be set via
 	 * the {@link #setUserProperty(String, String)} method, you can use the same
 	 * method to add more filesystems that should be used to query archived jobs
 	 * by using {@link Constants#JOB_ARCHIVE_LOCATION} as the key. That'll add
 	 * the specified value (format: alias;url) to the list of archive locations.
 	 *
 	 * @param jobname
 	 *            the jobname
 	 * @param target
 	 *            the url (of the parent dir) to archive the job to or null to
 	 *            use the (previously set) default archive location
 	 * @return the url of the target directory that can also be used as handle
 	 *         to query the status of the archiving process
 	 * @throws NoSuchJobException
 	 *             if no such job exists
 	 * @throws JobPropertiesException
 	 *             if the job is not finished yet
 	 * @throws RemoteFileSystemException
 	 *             if the archive location is not specified or there is some
 	 *             other kind of file related exception
 	 */
 	@POST
 	@Path("/job/{jobname}/archive")
 	@RolesAllowed("User")
 	@Produces("text/plain")
 	String archiveJob(@PathParam("jobname") String jobname,
 			@QueryParam("target") String target) throws NoSuchJobException,
 			JobPropertiesException, RemoteFileSystemException;
 
 	/**
 	 * Distributes a remote input file to all the filesystems that are used in
 	 * this batchjob.
 	 *
 	 * Use this after you created all jobs for this batchjob.
 	 *
 	 * @param batchJobname
 	 *            the name of the batchjob
 	 * @param inputFile
 	 *            the url of the inputfile
 	 * @param filename
 	 *            the target filename
 	 * @throws RemoteFileSystemException
 	 *             if there is a problem copying / accessing the file
 	 * @throws NoSuchJobException
 	 *             if the specified batchjob doesn't exist
 	 */
 	@RolesAllowed("User")
 	@POST
 	@Path("/batchjob/{batchjobname}/distribute_remote_inputfile")
 	void copyBatchJobInputFile(@PathParam("batchjobname") String batchJobname,
 			@QueryParam("inputFile") String inputFile,
 			@QueryParam("filename") String filename)
 					throws RemoteFileSystemException, NoSuchJobException;
 
 	/**
 	 * Copies one file to another location (recursively if it's a directory).
 	 *
 	 * @param source
 	 *            the source file
 	 * @param target
 	 *            the target directory
 	 * @param overwrite
 	 *            whether to overwrite a possible target file
 	 * @param waitForFileTransferToFinish
 	 *            whether to wait for the file transfer to finish (true) or
 	 *            whether to run the filetransfer in the background
 	 * @return the filetransfer handle
 	 * @throws RemoteFileSystemException
 	 *             if the remote source file system could not be read/mounted or
 	 *             the remote target file system could not be written to
 	 */
 	@POST
 	@Path("/files/cp")
 	@RolesAllowed("User")
 	@Produces("text/plain")
 	String cp(@QueryParam("sources") DtoStringList sources,
 			@QueryParam("target") String target,
 			@DefaultValue("false") @QueryParam("overwrite") boolean overwrite,
 			@DefaultValue("false") @QueryParam("wait") boolean wait)
 					throws RemoteFileSystemException;
 
 	/**
 	 * Creates a batchjob on the server.
 	 *
 	 * A batchjob is just a collection of jobs that belong together to make them
 	 * more easily managable.
 	 *
 	 * @param batchJobname
 	 *            the id (name) of the batchjob
 	 * @param fqan
 	 *            the vo to use
 	 * @param jobnameCreationMethod
 	 *            the method to use to (possibly) auto-calculate the jobname (if
 	 *            one with the specfied jobname in the jobProperties already
 	 *            exists). This defaults to "force-name" if you specify null.
 	 * @return the name of the job (auto-calculated or not) which is used as a
 	 * @throws JobPropertiesException
 	 */
 	@RolesAllowed("User")
 	@PUT
 	@Path("/batchjob/{batchjobname}")
 	DtoBatchJob createBatchJob(
 			@PathParam("batchjobname") String batchJobname,
 			@QueryParam("group") String fqan,
 			@DefaultValue(Constants.UNIQUE_NUMBER_METHOD) @QueryParam("jobname_creation_method") String jobnameCreationMethod)
 					throws BatchJobException;
 
 	/**
 	 * This method calls {@link #createJobUsingMap(Map, String, String)}
 	 * internally with a map of job properties that are extracted from the jsdl
 	 * document.
 	 *
 	 * @param jsdl
 	 *            a jsdl document
 	 * @param fqan
 	 *            the vo tu use to submit the job
 	 * @param jobnameCreationMethod
 	 *            the method to use to (possibly) auto-calculate the jobname (if
 	 *            one with the specfied jobname in the jobProperties already
 	 *            exists). This defaults to "force-name" if you specify null.
 	 * @return the name of the job (auto-calculated or not) which is used as a
 	 *         handle
 	 * @throws JobPropertiesException
 	 *             if the job could not be created (maybe because the jobname
 	 *             already exists and force-jobname is specified as jobname
 	 *             creation method).
 	 */
 	@PUT
 	@Path("/job/{jobname}")
 	@RolesAllowed("User")
 	@Produces("text/plain")
 	String createJob(
 			@QueryParam("jsdl") String jsdl,
 			@QueryParam("group") String fqan,
 			@DefaultValue(Constants.UNIQUE_NUMBER_METHOD) @QueryParam("jobname-creation-method") String jobnameCreationMethod)
 					throws JobPropertiesException;
 
 	/**
 	 * Deletes a remote file.
 	 *
 	 * @param file
 	 *            the file to delete
 	 * @return the handle for the file delete
 	 * @throws RemoteFileSystemException
 	 *             if the filesystem could not be accessed
 	 */
 	@RolesAllowed("User")
 	@DELETE
 	@Path("/files/{url}/delete")
 	String deleteFile(@PathParam("url") String url)
 			throws RemoteFileSystemException;
 
 	/**
 	 * Deletes a bunch of remote files.
 	 *
 	 * This doesn't throw an exception if the deletion of one file fails.
 	 *
 	 * @param files
 	 *            the files to delete
 	 * @return a handle for the file deletion actionstatus
 	 */
 	@RolesAllowed("User")
 	@DELETE
 	@Path("/files/delete")
 	@Produces("text/plain")
 	String deleteFiles(@QueryParam("urls") DtoStringList files);
 
 	/**
 	 * Lists all the mountpoints of the user's virtual filesystem.
 	 *
 	 * @return all the MountPoints
 	 */
 	@GET
 	@Path("/user/mountpoints")
 	@RolesAllowed("User")
 	DtoMountPoints df();
 
 	/**
 	 * Download a file to the client.
 	 *
 	 * @param url
 	 *            the filename of the file either absolute or "user-space" url
 	 * @return the data
 	 * @throws RemoteFileSystemException
 	 *             if the remote (source) file system could not be conntacted
 	 *             /mounted / is not readable
 	 */
 	@XmlMimeType("application/octet-stream")
 	@GET
 	@RolesAllowed("User")
 	@Path("/files/{url}/download")
 	DataHandler download(@PathParam("url") String url)
 			throws RemoteFileSystemException;
 
 	/**
 	 * Checks whether the specified file/folder exists.
 	 *
 	 * @param url
 	 *            the file or folder
 	 * @return true - exists, false - doesn't exist
 	 * @throws RemoteFileSystemException
 	 *             if the file system can't be accessed to determine whether the
 	 *             file exists
 	 */
 	@GET
 	@Path("/files/{url}/exists")
 	@RolesAllowed("User")
 	@Produces("text/plain")
 	boolean fileExists(@PathParam("url") String url)
 			throws RemoteFileSystemException;
 
 
 
 	/**
 	 * Takes job properties and returns a list of queues along with information
 	 * how well (or if at all) the specified job would run on each queue.
 	 *
 	 * @param jobProperties
 	 *            the job
 	 * @param fqan
 	 *            the group
 	 * @return the list of queues
 	 */
 	@GET
 	@Path("/info/matches/properties")
 	@PermitAll
 	List<JobQueueMatch> findMatches(
 			@QueryParam("jobProperties") DtoProperties jobProperties,
 			@QueryParam("fqan") String fqan);
 
 	/**
 	 * Takes a job properties and returns a list of submission locations that
 	 * match the requirements.
 	 *
 	 * @param jobProperties
 	 *            the job Properties (have alook at the
 	 *            {@link EnunciateServiceInterface} interface for supported
 	 *            keys)
 	 * @param fqan
 	 *            the fqan to use to submit the job
 	 * @param include
 	 *            whether to include Queues for that fqan but don't fit one or
 	 *            more of the job properties
 	 *
 	 * @return a list of matching submissionLoctations
 	 */
 	@GET
 	@Path("/info/queues/matching/properties")
 	@PermitAll
 	List<Queue> findQueues(
 			@QueryParam("jobProperties") DtoProperties jobProperties,
 			@QueryParam("fqan") String fqan);
 
 	/**
 	 * Returns the current status of an ongoing action.
 	 *
 	 * This is not stored in the database, so you can only access a status for
 	 * an action that was created in the same session.
 	 *
 	 * @param actionHandle
 	 *            the (unique) handle of the action (e.g. the jobname or target
 	 *            url)
 	 * @return the status object
 	 */
 	@RolesAllowed("User")
 	@Path("/user/status/{handle}")
 	@GET
 	DtoActionStatus getActionStatus(@PathParam("handle") String actionHandle);
 
 	/**
 	 * Returns a list of all the current (non-archived) jobs of the user with
 	 * details about the jobs.
 	 *
 	 * @param application
 	 *            filter by application or {@link Constants#ALLJOBS_KEY}/null
 	 *            (for all jobs)
 	 * @param refreshJobStatus
 	 *            whether to refresh the status of all the jobs. This can take
 	 *            quite some time.
 	 *
 	 * @return xml formated information about all the users jobs
 	 */
 	@GET
 	@Path("/jobs")
 	@RolesAllowed("User")
 	DtoJobs getActiveJobs(
 			@DefaultValue(Constants.ALLJOBS_KEY) @QueryParam("application") String application,
 			@DefaultValue("false") @QueryParam("refresh") boolean refreshJobStatus);
 
 	/**
 	 * Returns all applications that are available grid-wide or for a certain
 	 * vo.
 	 *
 	 * @param fqans
 	 *            all the fqans you want to query or null for a grid-wide search
 	 *            (fqan-independent).
 	 * @return all applications
 	 */
 	@GET
 	@Path("/info/applications")
 	@PermitAll
 	Application[] getAllAvailableApplications(
 			@DefaultValue("") @QueryParam("groups") DtoStringList fqans);
 
 	/**
 	 * Returns a list of all batch jobnames that are currently stored on this
 	 * backend
 	 *
 	 * @return all batchjobnames
 	 */
 	@RolesAllowed("User")
 	@GET
 	@Path("/batchjobs/names")
 	DtoStringList getAllBatchJobnames(
 			@DefaultValue(Constants.ALLJOBS_KEY) @QueryParam("application") String application);
 
 
 
 	/**
 	 * Returns a list of all jobnames that are currently stored on this backend.
 	 *
 	 * By default it doesn't include batchjobs, but if you specify
 	 * {@link Constants#ALLJOBS_INCL_BATCH_KEY} as parameter, it will return all
 	 * (single-)jobnames. If you specify null or {@link Constants#ALLJOBS_KEY},
 	 * it will return all single jobs excuding childs of batchjobs.
 	 *
 	 * @param application
 	 *            the name of the application of the jobs you are interested or
 	 *            {@link Constants#ALLJOBS_KEY} or null
 	 * @return all jobnames
 	 */
 	@GET
 	@Path("/jobs/names/")
 	@RolesAllowed("User")
 	DtoStringList getAllJobnames(
 			@DefaultValue(Constants.ALLJOBS_KEY) @QueryParam("application") String application);
 
 	/**
 	 * I don't know whether this one should sit on the web service side or the
 	 * client side. Anyway, here it is for now. It tells the client all sites a
 	 * job can be submitted to.
 	 *
 	 * @return all sites
 	 */
 	@GET
 	@Path("/info/sites")
 	@PermitAll
 	Site[] getAllSites();
 
 	/**
 	 * Queries for all submission locations on the grid. Returns an array of
 	 * Strings in the format: <queuename>:<submissionHost>[#porttype] (porttype
 	 * can be ommitted if it's pbs.
 	 *
 	 * @return all queues grid-wide
 	 */
 	@GET
 	@Path("/info/queues")
 	@PermitAll
 	Queue[] getAllSubmissionLocations();
 
 	/**
 	 * Returns all submission locations for this VO. Needed for better
 	 * performance.
 	 *
 	 * @param fqan
 	 *            the VO
 	 * @return all submission locations
 	 */
 	@GET
 	@Path("/info/queues/{group}")
 	@PermitAll
 	Queue[] getAllSubmissionLocationsForFqan(
 			@PathParam("group") String fqan);
 
 	/**
 	 * Returns all the details that are know about this version of the
 	 * application. The return will look something like this: module=namd/2
 	 * executable=/usr/local/bin/namd2 or whatever.
 	 *
 	 * @param application
 	 *            the name of the application
 	 * @param version
 	 *            the version of the application
 	 * @param subloc
 	 *            the submission location where you want to run the application
 	 * @return details about the applications
 	 */
 	@GET
 	@Path("/info/application/{application}/{version}/{queue}")
 	@PermitAll
 	Package getApplicationDetailsForVersionAndSubmissionLocation(
 			@PathParam("application") String application,
 			@DefaultValue(Constants.NO_VERSION_INDICATOR_STRING) @PathParam("version") String version,
 			@DefaultValue("") @PathParam("queue") String subloc);
 
 
 	/**
 	 * Returns a xml document that contains all the jobs of the user with
 	 * information about the jobs.
 	 *
 	 *
 	 * @param application
 	 *            filter by application or null (for all jobs)
 	 *
 	 * @return xml formated information about all the users jobs
 	 */
 	@GET
 	@Path("/jobs/archived")
 	@RolesAllowed("User")
 	DtoJobs getArchivedJobs(
 			@DefaultValue("") @QueryParam("application") String application);
 
 	/**
 	 * Returns the users archive locations.
 	 *
 	 * An archive location is an url (along with an alias as the key) where the
 	 * user archived jobs.
 	 *
 	 * @return the archive locations of the current user
 	 */
 	@RolesAllowed("User")
 	@GET
 	@Path("/user/archives")
 	DtoProperties getArchiveLocations();
 
 	/**
 	 * Returns the {@link DtoBatchJob} with the specified name.
 	 *
 	 * This method doesn't refresh the jobs that belong to this batchjob. Call
 	 * {@link #refreshBatchJobStatus(String)} and monitor the action status for
 	 * this until it finishes before you retrieve the batchjob if you want an
 	 * up-to-date version of the batchjob.
 	 *
 	 * @return the batchjob
 	 */
 	@RolesAllowed("User")
 	@GET
 	@Path("/batchjob/{batchjobname}")
 	DtoBatchJob getBatchJob(@PathParam("batchjobname") String batchJobname)
 			throws NoSuchJobException;
 
 	/**
 	 * Gets the users bookmarks
 	 *
 	 * @param alias
 	 */
 	@RolesAllowed("User")
 	@GET
 	@Path("/user/bookmarks")
 	DtoProperties getBookmarks();
 
 	/**
 	 * Returns the end time of the credential used.
 	 *
 	 * @return the end time or -1 if the endtime couldn't be determined
 	 */
 	@RolesAllowed("User")
 	@GET
 	@Path("/user/session/credential_endtime")
 	long getCredentialEndTime();
 
 	// ---------------------------------------------------------------------------------------------------
 	//
 	// Filesystem methods
 	//
 	// ---------------------------------------------------------------------------------------------------
 
 	/**
 	 * Checks the current certificate and returns its' dn.
 	 *
 	 * @return the dn of the users' certificate
 	 */
 	@GET
 	@RolesAllowed("User")
 	@Path("/user/dn")
 	String getDN();
 
 	/**
 	 * Returns the size of the file in bytes. This will probably replaced in a
 	 * future version with a more generic method to get file properties.
 	 * Something like public Map<String, String> getFileSize(String[]
 	 * propertyNames)...
 	 *
 	 * @param file
 	 *            the url of the file
 	 * @return the size of the file in bytes
 	 * @throws RemoteFileSystemException
 	 *             if the file can't be accessed
 	 */
 	@RolesAllowed("User")
 	@POST
 	@Path("/files/{url}/size")
 	long getFileSize(@PathParam("url") String url)
 			throws RemoteFileSystemException;
 
 	/**
 	 * Returns all fqans of the user for the vo's that are configured on the
 	 * machine where this serviceinterface is hosted.
 	 *
 	 * @return all fqans of the user
 	 */
 	@GET
 	@Path("/user/groups")
 	@RolesAllowed("User")
 	DtoStringList getFqans();
 
 	/**
 	 * Can provide information about the interface (like version, hostname where
 	 * it runs on, load).
 	 *
 	 * Didn't figure out a set of available keys. I would suggest, for now
 	 * implement "VERSION" and "HOSTNAME".
 	 *
 	 * @param key
 	 *            the key
 	 * @return the value
 	 */
 	@GET
 	@Path("/info/backend/{key}")
 	@PermitAll
 	String getInterfaceInfo(@PathParam("key") String key);
 
 	/**
 	 * The version of the serviceInterface for this backend.
 	 *
 	 * @return the version
 	 */
 	@GET
 	@Path("/info/backend/version")
 	@PermitAll
 	int getInterfaceVersion();
 
 	/**
 	 * Returns the job details.
 	 *
 	 * Does not refresh the job status.
 	 *
 	 * @param jobname
 	 *            the name of the job
 	 * @return the job properties
 	 * @throws NoSuchJobException
 	 *             if no such job exists
 	 */
 	@GET
 	@Path("/job/{jobname}")
 	@RolesAllowed("User")
 	DtoJob getJob(@PathParam("jobname") String jobname)
 			throws NoSuchJobException;
 
 	/**
 	 * Return the value of a property that is stored along with a job.
 	 *
 	 * The name of the job property keys can be looked up in {@link Constants}.
 	 *
 	 * @param jobname
 	 *            the name of the job
 	 * @param key
 	 *            the key for the value you are interested in
 	 * @return the value
 	 * @throws NoSuchJobException
 	 *             if no such job exists
 	 */
 	@POST
 	@Path("/job/{jobname}/property/{key}")
 	@RolesAllowed("User")
 	String getJobProperty(@PathParam("jobname") String jobname,
 			@PathParam("key") String key) throws NoSuchJobException;
 
 	/**
 	 * Method to query the status of a job. The String representation of the
 	 * status can be obtained by calling
 	 * {@link JobConstants#translateStatus(int)}
 	 *
 	 * @param jobname
 	 *            the name of the job to query
 	 * @return the status of the job
 	 * @throws NoSuchJobException
 	 *             if no job with the specified jobname exists
 	 */
 	@POST
 	@Path("/job/{jobname}/status")
 	@RolesAllowed("User")
 	int getJobStatus(@PathParam("jobname") String jobname);
 
 	/**
 	 * Returns the jsdl document that was used to create this job.
 	 *
 	 * @param jobname
 	 *            the name of the job
 	 * @return the jsdl document
 	 * @throws NoSuchJobException
 	 *             if no such job exists
 	 */
 	@GET
 	@Path("/job/{jobname}/jsdl")
 	@RolesAllowed("User")
 	@Produces("text/xml")
 	String getJsdlDocument(@PathParam("jobname") String jobname)
 			throws NoSuchJobException;
 
 	/**
 	 * Returns the mountpoint that is used to acccess this uri.
 	 *
 	 * @param uri
 	 *            the uri
 	 * @return the mountpoint or null if no mountpoint can be found
 	 */
 	@POST
 	@RolesAllowed("User")
 	@Path("/files/{url}/mountpoint")
 	MountPoint getMountPointForUri(@PathParam("url") String url);
 
 	/**
 	 * Returns the name of the site this host belongs to.
 	 *
 	 * @param host
 	 *            the host
 	 * @return the site
 	 */
 	@GET
 	@Path("/info/{host}/site")
 	@PermitAll
 	String getSite(@PathParam("host") String host);
 
 	/**
 	 * Returns an array of the gridftp servers for the specified submission
 	 * locations.
 	 *
 	 * @param subLoc
 	 *            the submission location
 	 *            (queuename@cluster:contactstring#jobmanager)
 	 * @return the gridftp servers
 	 */
 	@GET
 	@Path("/info/{subloc}/staging_areas")
 	@PermitAll
 	DtoStringList getStagingFileSystemForSubmissionLocation(
 			@PathParam("subloc") String subLoc);
 
 	/**
 	 * Returns all sites/queues that support this application. If "null" is
 	 * provided, this method returns all available submission queues.
 	 *
 	 * The format of the output a String for each submission location which
 	 * looks like: <queuename>:<submissionHost>[#porttype] (porttype can be
 	 * ommitted if it's pbs.
 	 *
 	 * @param application
 	 *            the application.
 	 * @return all sites that support this application.
 	 */
 	@GET
 	@Path("/info/application/{application}/queues")
 	@PermitAll
 	Queue[] getSubmissionLocationsForApplication(
 			@PathParam("application") String application);
 
 	/**
 	 * Returns all sites/queues that support this version of this application.
 	 *
 	 * The format of the output a String for each submission location which
 	 * looks like: <queuename>:<submissionHost>[#porttype] (porttype can be
 	 * ommitted if it's pbs.
 	 *
 	 * @param application
 	 *            the application.
 	 * @param version
 	 *            the version
 	 * @return all sites that support this application.
 	 */
 	@GET
 	@Path("info/application/{application}/{version}/queues")
 	@PermitAll
 	Queue[] getSubmissionLocationsForApplicationAndVersion(
 			@PathParam("application") String application,
 			@PathParam("version") String version);
 
 	/**
 	 * Returns all sites/queues that support this version of this application if
 	 * the job is submitted with the specified fqan.
 	 *
 	 * The format of the output a String for each submission location which
 	 * looks like: <queuename>:<submissionHost>[#porttype] (porttype can be
 	 * ommitted if it's pbs.
 	 *
 	 * @param application
 	 *            the application.
 	 * @param version
 	 *            the version
 	 * @param fqan
 	 *            the fqan
 	 * @return all sites that support this application.
 	 */
 	@GET
 	@Path("/info/application/{application}/{version}/{group}/queues")
 	@PermitAll
 	Queue[] getSubmissionLocationsForApplicationAndVersionAndFqan(
 			@PathParam("application") String application,
 			@PathParam("version") String version,
 			@PathParam("group") String fqan);
 
 	// /**
 	// * Returns a map of all versions and all submission locations of this
 	// * application. The key of the map is the version, and the
 	// * submissionlocations are the values. If there is more than one
 	// * submissionLocation for a version, then they are seperated via commas.
 	// *
 	// * @param application
 	// * the name of the application
 	// * @return a map with all versions of the application as key and the
 	// * submissionLocations as comma
 	// */
 	// @GET
 	// @Path("/info/application/{application}/queues")
 	// @PermitAll
 	// DtoApplicationInfo getSubmissionLocationsPerVersionOfApplication(
 	// @PathParam("application") String application);
 
 	/**
 	 * Gets the template Document for this application.
 	 *
 	 * @param application
 	 *            the name of the application
 	 * @return a jsdl template document
 	 * @throws NoSuchTemplateException
 	 *             if a template for that particular application does not exist
 	 */
 	@PermitAll
 	@GET
 	@Path("/info/template/{template_name}")
 	String getTemplate(@PathParam("template_name") String application)
 			throws NoSuchTemplateException;
 
 	/**
 	 * Returns a list of all applications that are currently used for
 	 * (non-batch-)jobs.
 	 *
 	 * @return the list of applications
 	 */
 	@GET
 	@Path("/user/all_used_applications")
 	@RolesAllowed("User")
 	DtoStringList getUsedApplications();
 
 	// ---------------------------------------------------------------------------------------------------
 	//
 	// Job management methods
 	//
 	// ---------------------------------------------------------------------------------------------------
 
 	/**
 	 * Returns a list of all applications that are currently used for batchjobs.
 	 *
 	 * @return the list of applications
 	 */
 	@GET
 	@Path("/user/all_used_applications_batch")
 	@RolesAllowed("User")
 	DtoStringList getUsedApplicationsBatch();
 
 	/**
 	 * Gets all the properties stored for the current user.
 	 *
 	 * @return all userproperties
 	 */
 	@RolesAllowed("User")
 	@GET
 	@Path("/user/properties")
 	DtoProperties getUserProperties();
 
 	/**
 	 * Returns an array of strings that are associated with this key. The
 	 * developer can store all kinds of stuff he wants to associate with the
 	 * user. Might be useful for history and such.
 	 *
 	 * Not yet implemented though.
 	 *
 	 * @param key
 	 *            the key
 	 * @return the value
 	 */
 	@RolesAllowed("User")
 	@GET
 	@Path("/user/properties/{key}")
 	String getUserProperty(@PathParam("key") String key);
 
 	/**
 	 * Returns an array of the versions of the specified application that a
 	 * submissionlocation supports.
 	 *
 	 * @param application
 	 *            the application
 	 * @param site
 	 *            the site
 	 * @return the supported versions
 	 */
 	@GET
 	@Path("/info/application/{application}/{queue}/versions")
 	List<Version> getVersionsOfApplicationOnSubmissionLocation(
 			@PathParam("application") String application,
 			@PathParam("queue") String submissionLocation);
 
 	/**
 	 * Checks whether the specified file is a folder or not.
 	 *
 	 * @param url
 	 *            the file
 	 * @return true - if folder; false - if not
 	 * @throws RemoteFileSystemException
 	 *             if the files can't be accessed
 	 */
 	@RolesAllowed("User")
 	@GET
 	@Path("/files/{url}/isFolder")
 	boolean isFolder(@QueryParam("url") String url)
 			throws RemoteFileSystemException;
 
 	/**
 	 * Deletes the whole jobdirectory (if specified) and if successful, the job
 	 * from the database.
 	 *
 	 * Also works with batchjobs.
 	 *
 	 * @param jobname
 	 *            the name of the job
 	 * @param clean
 	 *            whether to clean/delete the jobdirectory if possible
 	 * @return the handle to the kill task
 	 *
 	 * @throws NoSuchJobException
 	 *             if no such job exists
 	 * @throws BatchJobException
 	 *             if the job is part of a batchjob
 	 */
 	@POST
 	@Path("job/{jobname}/kill")
 	@RolesAllowed("User")
 	String kill(@PathParam("jobname") String jobname,
 			@DefaultValue("false") @QueryParam("clean") boolean clean)
 					throws NoSuchJobException, BatchJobException;
 
 	/**
 	 * Deletes the whole jobdirectory (if specified) and if successful, the job
 	 * from the database.
 	 *
 	 * This one doesn't throw an exception if something goes wrong. Contrary to
 	 * {@link #kill(String, boolean)} this method also accepts bash-style globs
 	 * as a jobname and it'll match that against all existing jobs and
 	 * batchjobs.
 	 *
 	 * @param jobnames
 	 *            a list of jobs to kill
 	 * @param clean
 	 *            whether to clean/delete the jobdirectory if possible
 	 * @return the handle to the jobkilling task or null if no jobs to kill
 	 */
 	@POST
 	@Path("/jobs/kill")
 	@RolesAllowed("User")
 	String killJobs(@QueryParam("jobnames") DtoStringList jobnames,
 			@DefaultValue("false") @QueryParam("clean") boolean clean);
 
 	/**
 	 * Returns the date when the file was last modified.
 	 *
 	 * @param remoteFile
 	 *            the file to check
 	 * @return the last modified date
 	 * @throws RemoteFileSystemException
 	 *             if the file could not be accessed
 	 */
 	@RolesAllowed("User")
 	@GET
 	@Path("/files/{url}/last_modified")
 	long lastModified(@PathParam("url") String url)
 			throws RemoteFileSystemException;
 
 	/**
 	 * Lists all applications that are supported by this deployment of a service
 	 * interface. Basically it's a list of all the application where the service
 	 * interface has got a template jsdl.
 	 *
 	 * @return a list of all applications
 	 */
 	@PermitAll
 	@GET
 	@Path("/info/templates")
 	String[] listHostedApplicationTemplates();
 
 //	/**
 //	 * Starts a session. For some service interfaces this could be just a dummy
 //	 * method. Ideally a char[] would be used for the password, but jax-ws
 //	 * doesn't support this.
 //	 *
 //	 * @param username
 //	 *            the username (probably for myproxy credential)
 //	 * @param password
 //	 *            the password (probably for myproxy credential)
 //	 * @throws NoValidCredentialException
 //	 *             if the login was not successful
 //	 */
 	@POST
 	@Path("/user/login")
 	void login(@QueryParam("username") String username,
 			@QueryParam("password") String password,
 			@QueryParam("host") String host, @QueryParam("port") int port);
 
 	/**
 	 * Logout of the service. Performs housekeeping tasks and usually deletes
 	 * the Credential.
 	 *
 	 * @return a logout message
 	 */
 	@POST
 	@Path("/user/logout")
 	String logout();
 
 	/**
 	 * Lists the content of the specified directory.
 	 *
 	 * @param directory
 	 *            the directory you want to have a listing of. This has to be an
 	 *            absolute path (either something like: /ngdata_vpac/file.txt or
 	 *            gsiftp://ngdata.vpac.org/home/san04/markus/file.txt
 	 * @param recursion_level
 	 *            the level of recursion for the directory listing, use a value
 	 *            <= -1 for infinite but beware, the filelisting can take a
 	 *            long, long time. Use 0 to only get the properties (filesize,
 	 *            lastmodified) of the specified url, without possible children.
 	 *            Usually you would specify 1 and fill your filetree on the
 	 *            clientside on demand.
 	 * @return the content of the directory or null if the directory is empty.
 	 *         If the specified directory is a file, only information about this
 	 *         one file is returned.
 	 * @throws RemoteFileSystemException
 	 *             if the remote directory could not be read/mounted
 	 */
 	@GET
 	@Path("/files/{url}")
 	@RolesAllowed("User")
 	GridFile ls(@PathParam("url") String url,
 			@DefaultValue("1") @QueryParam("recursionLevel") int recursionLevel)
 					throws RemoteFileSystemException;
 
 	/**
 	 * Creates the specified folder (and it's parent folders if they don't
 	 * exist).
 	 *
 	 * @param folder
 	 *            the url of the folder
 	 * @return true - if the folder has been created successfully, false - if
 	 *         the folder already existed or could not be created
 	 * @throws RemoteFileSystemException
 	 *             if the filesystem could not be accessed
 	 */
 	@RolesAllowed("User")
 	@PUT
 	@Path("/files/{url}/mkdir")
 	boolean mkdir(@QueryParam("url") String url)
 			throws RemoteFileSystemException;
 
 	/**
 	 * Tries to figure out the best submission locations for all the jobs that
 	 * this batchpartjob consists of.
 	 *
 	 * Call this after you added all jobs to the batchjob and before you
 	 * upload/crosstage any files. It will overwrite possibly specified
 	 * submission locations on jobs.
 	 *
 	 * @param batchjobname
 	 *            the name of the batchjob
 	 * @throws NoSuchJobException
 	 *             if there is no batchjob with such an id
 	 * @throws JobPropertiesException
 	 *             if one of the jobs can't be re-created
 	 */
 	@RolesAllowed("User")
 	@POST
 	@Path("/batchjob/{batchjobname}/redistribute")
 	String redistributeBatchJob(@PathParam("batchjobname") String batchjobname)
 			throws NoSuchJobException, JobPropertiesException;
 
 	/**
 	 * Refreshes the status of all jobs that belong to this batchjob.
 	 *
 	 * This returns immediately. You need to watch the action status if you want
 	 * to know when all sub-jobs are refreshed.
 	 *
 	 * @param batchJobname
 	 *            the name of the batchjob
 	 * @return the action status handle for this. Use
 	 *         {@link #getActionStatus(String)} to find out when this is
 	 *         finished.
 	 * @throws NoSuchJobException
 	 *             if there is no batchjob with the specified id
 	 */
 	@RolesAllowed("User")
 	@POST
 	@Path("/batchjob/{batchjobname}/refresh")
 	String refreshBatchJobStatus(String batchJobname) throws NoSuchJobException;
 
 	/**
 	 * Removes the specified job from the batchJob.
 	 *
 	 * @param batchJobname
 	 *            the batchJobname
 	 * @param jobname
 	 *            the jobname
 	 */
 	@RolesAllowed("User")
 	@POST
 	@Path("/batchjob/{batchjobname}/{jobname}/remove")
 	void removeJobFromBatchJob(@PathParam("batchjobname") String batchJobname,
 			@PathParam("jobname") String jobname) throws NoSuchJobException;
 
 	/**
 	 * Restarts a batch job.
 	 *
 	 * Depending on the restart policy and the supplied properties, the backend
 	 * will calculate which jobs to restart and how.
 	 *
 	 * @param batchjobname
 	 *            the batchjobname
 	 * @param restartPolicy
 	 *            the restart policy //TODO not implemented yet, only default
 	 *            one supported
 	 * @param properties
 	 *            the restart policy properties //TODO not implemented yet, only
 	 *            default one supported
 	 * @return the job distribution figures of the re-submitted jobs
 	 * @throws NoSuchJobException
 	 *             if no such batchjob exists
 	 * @throws JobPropertiesException
 	 *             if one of the jobs can't be recreated or if the job
 	 *             submission of this batch job is still ongoing in the
 	 *             background
 	 */
 	@RolesAllowed("User")
 	@POST
 	@Path("/batchjob/{batchjobname}/restart")
 	DtoProperties restartBatchJob(
 			@PathParam("batchjobname") final String batchjobname,
 			@DefaultValue(Constants.SUBMIT_POLICY_RESTART_DEFAULT) @QueryParam("restartPolicy") String restartPolicy,
 			@DefaultValue("") DtoProperties properties)
 					throws NoSuchJobException, JobPropertiesException;
 
 	/**
 	 * Resubmit a job. Kills the old one if it's still running.
 	 *
 	 * This uses the same job properties as the old job. If you want some of the
 	 * properties changed, you need to provide an updated jsdl file. Be aware
 	 * that not all properties can be changed (for example you can't change the
 	 * filesystem the job runs on or the fqan). Have a look at the implemenation
 	 * of this method to find out what can't be changed and what not. Anyway,
 	 * use this with caution and prefer to just submit a new job if possible.
 	 *
 	 * @param jobname
 	 *            the name of the job
 	 * @param changedJsdl
 	 *            the updated jsdl or null (if you want to re-run the same job)
 	 * @throws JobSubmissionException
 	 *             if the job could not be resubmitted
 	 * @throws NoSuchJobException
 	 *             if no job with the specified jobname exists
 	 */
 	@RolesAllowed("User")
 	@POST
 	@Path("/job/{jobname}/restart")
 	void restartJob(@PathParam("jobname") final String jobname,
 			@DefaultValue("") @QueryParam("jsdl") String changedJsdl)
 					throws JobSubmissionException, NoSuchJobException;
 
 	/**
 	 * Sets a batch of user properties.
 	 *
 	 * @param properties
 	 *            the properties
 	 */
 	void setUserProperties(DtoProperties properties);
 
 	/**
 	 * Sets a user property.
 	 *
 	 * <p>
 	 * There are special user properties that can be set by using one of the
 	 * following strings as key:
 	 * </p>
 	 * <p>
 	 * {@link Constants#CLEAR_MOUNTPOINT_CACHE}(clearMountPointCache): prompts
 	 * Grisu to delete the cached filesystems for the user. Next startup will be
 	 * slower but with up-to-date mountpoints. This caching can be disabled on
 	 * the backend, so setting this property might not have any effect.
 	 * </p>
 	 * <p>
 	 * {@link Constants#JOB_ARCHIVE_LOCATION}(archiveLocation): as described in {@link #archiveJob(String, String), this allows to tell Grisu about locations where archived Grisu jobs are located. Use this string as key and a ;-separated alias;url string as value to add such a location.
 	 *
 	 * @param key
 	 *            the key
 	 * @param value
 	 *            the value
 	 */
 	@RolesAllowed("User")
 	@PUT
 	@Path("/user/property/{key}")
 	void setUserProperty(@PathParam("key") String key,
 			@QueryParam("value") String value);
 
 	/**
 	 * Submits the job that was prepared before using
 	 * {@link #createJobUsingMap(Map, String, String)} or
 	 * {@link #createJob(String, String, String)} to the specified submission
 	 * location.
 	 *
 	 * @param jobname
 	 *            the jobname
 	 * @return the handle to check the status of the job submission
 	 * @throws JobSubmissionException
 	 *             if the job could not submitted
 	 * @throws NoSuchJobException
 	 *             if no such job exists
 	 */
 	@POST
 	@Path("/job/{jobname}/submit")
 	@RolesAllowed("User")
 	String submitJob(@PathParam("jobname") String jobname)
 			throws JobSubmissionException, NoSuchJobException;
 
 
 	/**
 	 * Unmounts a filesystem.
 	 *
 	 * @param mountpoint
 	 *            the mountpoint
 	 * @return whether it worked or not
 	 */
 	@POST
 	@Path("/user/mountpoint/{alias}/unmount")
 	@RolesAllowed("User")
 	void umount(@PathParam("alias") String alias);
 
 	/**
 	 * Upload a {@link DataSource} to the users' virtual filesystem.
 	 *
 	 * @param file
 	 *            the (local) file you want to upload
 	 * @param filename
 	 *            the location you want the file upload to
 	 * @return the new path of the uploaded file or null if the upload failed
 	 * @throws RemoteFileSystemException
 	 *             if the remote (target) filesystem could not be connected /
 	 *             mounted / is not writeable
 	 */
 	@PUT
 	@RolesAllowed("User")
 	@Path("/files/{url}/upload")
 	@Produces("text/plain")
 	String upload(@XmlMimeType("application/octet-stream") DataHandler file,
 			@PathParam("filename") String filename)
 					throws RemoteFileSystemException;
 
 
     /**
      * Enables the user to send a message (incl. attachment) to request support.
      *
      * @param title the title of the message
      * @param message the message
      * @param file an (optional) attachment
      * @return an acknowledgement string
      */
     @PUT
 	@RolesAllowed("User")
 	@Path("/support/upload")
 	@Produces("text/plain")
	String requestSupport(@QueryParam("title") String title, @QueryParam("message")String message, @XmlMimeType("application/octet-stream") DataHandler file);
 
     /**
      * Returns all messages for this user.
      *
      * @return the messages
      */
     @GET
 	@Path("/support/messages")
 	@RolesAllowed("User")
     DtoMessages getMessages();
 
     /**
      * Removes the message with the specified title from the users message queue.
      *
      * @param title the title
      */
     @DELETE
     @Path("/support/messages/{title}/remove")
     void removeMessage(@PathParam("title") String title);
 
     @XmlMimeType("application/octet-stream")
 	@GET
 	@RolesAllowed("User")
 	@Path("/files/shared/{token}")
 	DataHandler downloadSharedFile(@PathParam("token") String token)
 			throws RemoteFileSystemException;
 
 
     /**
      * Sends a message to one or multiple users
      * @param users the users
      * @param message the message
      * @return an acknowledgement
      */
     @PUT
 	@RolesAllowed("User")
 	@Path("/support/messages/send")
 	@Produces("text/plain")
    String sendMessage(@QueryParam("users") DtoStringList users, @QueryParam("message") DtoMessage message);
 
     /**
      * Uploads a file to be shared later.
      *
      * @param file the file
      * @param allowedUsers who is allowed to view this file
      * @return a token
      */
     @PUT
 	@RolesAllowed("User")
 	@Path("/files/shared/upload")
 	@Produces("text/plain")
	String uploadSharedFile(@XmlMimeType("application/octet-stream") DataHandler file, @QueryParam("users") DtoStringList allowedUsers);
 
 	/**
 	 * Uploads input file for job or distributes an input file to all the
 	 * filesystems that are used in a batchjob if job is batchjob.
 	 *
 	 * In case of single job: this gets put in the jobdirectory of the job and
 	 * waits until the copying finished
 	 *
 	 * In case of batchjob: You need to reverence to the input file using
 	 * relative paths in the commandline you specify in the jobs that need this
 	 * inputfile. Use this after you created all jobs for this batchjob. In the
 	 * case of batchjob this method returns immediately after the file upload
 	 * and you have to monitor the progress using the
 	 * {@link #getActionStatus(String)} method if you want to know when the file
 	 * is copied to all targets.
 	 *
 	 * @param jobname
 	 *            the jobname
 	 * @param inputFile
 	 *            the inputfile
 	 * @param relativePath
 	 *            the path relative to the jobdirectory
 	 */
 	@POST
 	@RolesAllowed("User")
 	@Path("/job/{jobname}/upload_input_file")
 	void uploadInputFile(@QueryParam("jobname") String jobname,
 			@XmlMimeType("application/octet-stream") DataHandler inputFile,
 			@QueryParam("relativePath") String relativePath)
 					throws RemoteFileSystemException, NoSuchJobException;
 
 }
