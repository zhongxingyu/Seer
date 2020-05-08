 package grisu.backend.model.job;
 
 import grisu.backend.model.User;
 import grisu.control.exceptions.NoSuchJobException;
 import grisu.model.dto.DtoProperties;
 import grisu.model.dto.DtoStringList;
 
 import java.util.Collection;
 
 import javax.activation.DataHandler;
 
 import com.google.common.collect.Sets;
 
 public class UserBatchJobManager {
 
 	public UserBatchJobManager(User user) {
 		// TODO Auto-generated constructor stub
 	}
 
 	public void addJobProperty(String jobname, String key, String value) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public String addJobToBatchJob(String batchjobname, String jobdescription) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String archiveBatchJob(String jobname, String url)
 			throws NoSuchJobException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public DtoStringList getAllBatchJobnames(Object object) {
		return DtoStringList.fromString("dummy");
 	}
 
 	public BatchJob getBatchJobFromDatabase(String mpjName)
 			throws NoSuchJobException {
 		throw new NoSuchJobException("Dummy");
 	}
 
 	public Collection<String> getUsedApplicationsBatch() {
 		return Sets.newTreeSet();
 	}
 
 	public String kill(String jobname, boolean clear) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String redistributeBatchJob(String batchjobname) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String refreshBatchJobStatus(String batchJobname) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void removeJobFromBatchJob(String batchJobname, String jobname) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public DtoProperties restartBatchJob(String batchjobname,
 			String restartPolicy, DtoProperties properties) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void saveOrUpdate(BatchJob mpj) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public String submitBatchJob(String jobname) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void uploadInputFile(String jobname, DataHandler source,
 			String targetFilename) {
 		// TODO Auto-generated method stub
 
 	}
 
 	// static final Logger myLogger = LoggerFactory
 	// .getLogger(UserBatchJobManager.class.getName());
 	//
 	// protected final BatchJobDAO batchJobDao = new BatchJobDAO();
 	//
 	// private final User user;
 	//
 	// public UserBatchJobManager(User user) {
 	// this.user = user;
 	// }
 	//
 	// public void addJobProperty(final String jobname, final String key,
 	// final String value) throws NoSuchJobException {
 	//
 	//
 	// final BatchJob job = getBatchJobFromDatabase(jobname);
 	// job.addJobProperty(key, value);
 	// saveOrUpdate(job);
 	// myLogger.debug("Added multijob property: " + key);
 	//
 	//
 	// }
 	//
 	// /**
 	// * Adds the specified job to the mulitpartJob.
 	// *
 	// * @param batchJobname
 	// * the batchJobname
 	// * @param jobname
 	// * the jobname
 	// * @throws NoSuchJobException
 	// * @throws JobPropertiesException
 	// * @throws NoSuchJobException
 	// */
 	// public String addJobToBatchJob(String batchJobname, String jsdlString)
 	// throws JobPropertiesException, NoSuchJobException {
 	//
 	// final BatchJob multiJob = getBatchJobFromDatabase(batchJobname);
 	//
 	// Document jsdl;
 	//
 	// try {
 	// jsdl = SeveralXMLHelpers.fromString(jsdlString);
 	// } catch (final Exception e3) {
 	// throw new RuntimeException("Invalid jsdl/xml format.", e3);
 	// }
 	//
 	// String jobnameCreationMethod = multiJob
 	// .getJobProperty(Constants.JOBNAME_CREATION_METHOD_KEY);
 	// if (StringUtils.isBlank(jobnameCreationMethod)) {
 	// jobnameCreationMethod = "force-name";
 	// }
 	//
 	// final String jobname = getUser().getJobManager().createJob(jsdl,
 	// multiJob.getFqan(), "force-name", multiJob);
 	// multiJob.addJob(jobname);
 	// multiJob.setStatus(JobConstants.READY_TO_SUBMIT);
 	// saveOrUpdate(multiJob);
 	//
 	// return jobname;
 	// }
 	//
 	// public String archiveBatchJob(String jobname, String target)
 	// throws NoSuchJobException, JobPropertiesException {
 	// final BatchJob job = getBatchJobFromDatabase(jobname);
 	// final String jobdirUrl = job.getJobProperty(Constants.JOBDIRECTORY_KEY);
 	//
 	// final String targetDir = target + "/"
 	// + FileManager.getFilename(jobdirUrl);
 	//
 	// archiveBatchJobObject(job, targetDir);
 	// return targetDir;
 	// }
 	//
 	// private void archiveBatchJobObject(final BatchJob batchJob, final String
 	// target)
 	// throws NoSuchJobException, JobPropertiesException {
 	//
 	// if (batchJob.getStatus() <= JobConstants.FINISHED_EITHER_WAY) {
 	// // this should not really happen
 	// myLogger.error("Not archiving job because job is not finished.");
 	// throw new JobPropertiesException(
 	// "Can't archive batchjob because it is not finished yet.");
 	// }
 	//
 	// final DtoActionStatus status = new DtoActionStatus(target, (batchJob
 	// .getJobs().size() * 3) + 3);
 	// getUser().getActionStatuses().put(target, status);
 	//
 	// final Thread archiveThread = new Thread() {
 	// @Override
 	// public void run() {
 	//
 	// status.addElement("Starting to archive batchjob "
 	// + batchJob.getBatchJobname());
 	// final NamedThreadFactory tf = new NamedThreadFactory(
 	// "archiveBatchJob " + batchJob.getBatchJobname() + " / "
 	// + getUser().getDn());
 	// final ExecutorService executor = Executors.newFixedThreadPool(
 	// ServerPropertiesManager
 	// .getConcurrentFileTransfersPerUser(), tf);
 	//
 	// for (final Job job : batchJob.getJobs()) {
 	// status.addElement("Creating job archive thread for job "
 	// + job.getJobname());
 	// final String jobdirUrl = job
 	// .getJobProperty(Constants.JOBDIRECTORY_KEY);
 	// final String targetDir = target + "/"
 	// + FileManager.getFilename(jobdirUrl);
 	//
 	// String tmp = targetDir;
 	// int i = 1;
 	// try {
 	// while (getUser().getFileManager().fileExists(tmp)) {
 	// i = i + 1;
 	// tmp = targetDir + "_" + i;
 	// }
 	// } catch (final RemoteFileSystemException e2) {
 	// myLogger.error(e2.getLocalizedMessage(), e2);
 	// return;
 	// }
 	//
 	// final Thread archiveThread =
 	// getUser().getJobManager().archiveSingleJob(job, tmp,
 	// status);
 	// archiveThread.setName("archive_batchJob "
 	// + batchJob.getBatchJobname() + " / "
 	// + getUser().getDn() + " / " + status.getHandle());
 	// executor.execute(archiveThread);
 	// }
 	//
 	// executor.shutdown();
 	//
 	// try {
 	// executor.awaitTermination(24, TimeUnit.HOURS);
 	// } catch (final InterruptedException e) {
 	// myLogger.error(e.getLocalizedMessage(), e);
 	// status.setFailed(true);
 	// status.setErrorCause(e.getLocalizedMessage());
 	// status.setFinished(true);
 	// status.addElement("Killing of sub-jobs interrupted: "
 	// + e.getLocalizedMessage());
 	// return;
 	// }
 	//
 	// status.addElement("Killing batchjob.");
 	// // now kill batchjob
 	// final Thread deleteThread = deleteBatchJob(batchJob, true);
 	//
 	// try {
 	// deleteThread.join();
 	// status.addElement("Batchjob killed.");
 	// } catch (final InterruptedException e) {
 	// status.setFailed(true);
 	// status.setErrorCause("Archiving interrupted.");
 	// status.setFinished(true);
 	// myLogger.error(e.getLocalizedMessage(), e);
 	// return;
 	// }
 	//
 	// status.setFinished(true);
 	// }
 	// };
 	// archiveThread.setName("archive batchjob " + batchJob.getBatchJobname()
 	// + " / " + getUser().getDn());
 	// archiveThread.start();
 	//
 	// }
 	//
 	// private SortedSet<GridResource> calculateResourcesToUse(BatchJob mpj) {
 	//
 	// final String locationsToIncludeString = mpj
 	// .getJobProperty(Constants.LOCATIONS_TO_INCLUDE_KEY);
 	// String[] locationsToInclude = null;
 	// if (StringUtils.isNotBlank(locationsToIncludeString)) {
 	// locationsToInclude = locationsToIncludeString.split(",");
 	// }
 	//
 	// final String locationsToExcludeString = mpj
 	// .getJobProperty(Constants.LOCATIONS_TO_EXCLUDE_KEY);
 	// String[] locationsToExclude = null;
 	// if (StringUtils.isNotBlank(locationsToExcludeString)) {
 	// locationsToExclude = locationsToExcludeString.split(",");
 	// }
 	//
 	// final SortedSet<GridResource> resourcesToUse = new
 	// TreeSet<GridResource>();
 	//
 	// for (final GridResource resource : findBestResourcesForBatchJob(mpj)) {
 	//
 	// final String tempSubLocString = SubmissionLocationHelpers
 	// .createSubmissionLocationString(resource);
 	//
 	// // check whether subloc is available for vo
 	// final Collection<Queue> allqueues = getUser()
 	// .getInformationManager()
 	// .getAllSubmissionLocationsForVO(mpj.getFqan());
 	//
 	// final Collection<String> allSubLocs = Collections2.transform(
 	// allqueues, Functions.toStringFunction());
 	//
 	// if (!allSubLocs.contains(tempSubLocString)) {
 	// continue;
 	// }
 	//
 	// if ((locationsToInclude != null) && (locationsToInclude.length > 0)) {
 	//
 	// for (final String subLoc : locationsToInclude) {
 	// if (tempSubLocString.toLowerCase().contains(
 	// subLoc.toLowerCase())) {
 	// if (getUser().isValidSubmissionLocation(
 	// tempSubLocString, mpj.getFqan())) {
 	// resourcesToUse.add(resource);
 	// }
 	// break;
 	// }
 	// }
 	//
 	// } else if ((locationsToExclude != null)
 	// && (locationsToExclude.length > 0)) {
 	//
 	// boolean useSubLoc = true;
 	// for (final String subLoc : locationsToExclude) {
 	// if (tempSubLocString.toLowerCase().contains(
 	// subLoc.toLowerCase())) {
 	// useSubLoc = false;
 	// break;
 	// }
 	// }
 	// if (useSubLoc) {
 	// if (getUser().isValidSubmissionLocation(tempSubLocString,
 	// mpj.getFqan())) {
 	// resourcesToUse.add(resource);
 	// }
 	// }
 	//
 	// } else {
 	//
 	// if (getUser().isValidSubmissionLocation(tempSubLocString,
 	// mpj.getFqan())) {
 	// resourcesToUse.add(resource);
 	// }
 	// }
 	// }
 	//
 	// if (getUser().checkFileSystemsBeforeUse) {
 	//
 	// // myLogger.debug("Checking filesystems to use...");
 	// final NamedThreadFactory tf = new NamedThreadFactory(
 	// "batchJobResourceCalculation");
 	// final ExecutorService executor1 = Executors
 	// .newFixedThreadPool(ServerPropertiesManager
 	// .getConcurrentFileTransfersPerUser(), tf);
 	//
 	// final Set<GridResource> failSet = Collections
 	// .synchronizedSet(new HashSet<GridResource>());
 	//
 	// for (final GridResource gr : resourcesToUse) {
 	//
 	// final String subLoc = SubmissionLocationHelpers
 	// .createSubmissionLocationString(gr);
 	//
 	// final Set<Directory> fs = getUser().getInformationManager()
 	// .getStagingFileSystemForSubmissionLocation(subLoc);
 	//
 	// for (final MountPoint mp : getUser().df(mpj.getFqan())) {
 	//
 	// for (final Directory f : fs) {
 	// if (mp.getRootUrl().startsWith(
 	// f.getUrl().replace(":2811", ""))) {
 	//
 	// final Thread thread = new Thread() {
 	// @Override
 	// public void run() {
 	// try {
 	// if (!getUser().getFileManager()
 	// .fileExists(mp.getRootUrl())) {
 	// myLogger.error("Removing sub loc "
 	// + subLoc);
 	// failSet.add(gr);
 	// }
 	// } catch (final RemoteFileSystemException e) {
 	// myLogger.error("Removing sub loc "
 	// + subLoc + ": "
 	// + e.getLocalizedMessage());
 	// failSet.add(gr);
 	// }
 	// }
 	// };
 	//
 	// executor1.execute(thread);
 	// }
 	// }
 	// }
 	// }
 	//
 	// executor1.shutdown();
 	//
 	// try {
 	// executor1.awaitTermination(3600, TimeUnit.SECONDS);
 	// } catch (final InterruptedException e) {
 	// myLogger.error(e.getLocalizedMessage(), e);
 	// }
 	//
 	// resourcesToUse.removeAll(failSet);
 	// // myLogger.debug("Checking filesystems to use: finished");
 	// myLogger.debug("Removed filesystems for batchjob: "
 	// + StringUtils.join(failSet, ","));
 	// }
 	//
 	// return resourcesToUse;
 	//
 	// }
 	//
 	// private void delete(BatchJob batchJob) {
 	// batchJobDao.delete(batchJob);
 	// }
 	//
 	// /**
 	// * Removes the multipartJob from the server.
 	// *
 	// * @param batchJobname
 	// * the name of the multipartJob
 	// * @param deleteChildJobsAsWell
 	// * whether to delete the child jobs of this multipartjob as well.
 	// */
 	// private Thread deleteBatchJob(final BatchJob multiJob,
 	// final boolean clean) {
 	//
 	// int size = (multiJob.getJobs().size() * 2) + 1;
 	//
 	// if (clean) {
 	// size = size + (multiJob.getAllUsedMountPoints().size() * 2);
 	// }
 	//
 	// final DtoActionStatus newActionStatus = new DtoActionStatus(
 	// multiJob.getBatchJobname(), size);
 	// getUser().getActionStatuses().put(multiJob.getBatchJobname(),
 	// newActionStatus);
 	// final NamedThreadFactory tf = new NamedThreadFactory("deleteBatchJob");
 	// final ExecutorService executor = Executors.newFixedThreadPool(
 	// ServerPropertiesManager
 	// .getConcurrentMultiPartJobSubmitThreadsPerUser(), tf);
 	//
 	// final Job[] jobs = multiJob.getJobs().toArray(new Job[] {});
 	//
 	// for (final Job job : jobs) {
 	// multiJob.removeJob(job);
 	// }
 	// saveOrUpdate(multiJob);
 	// for (final Job job : jobs) {
 	// final Thread thread = new Thread("killing_" + job.getJobname()) {
 	// @Override
 	// public void run() {
 	// try {
 	// myLogger.debug("Killing job " + job.getJobname()
 	// + " in thread "
 	// + Thread.currentThread().getName());
 	//
 	// newActionStatus.addElement("Killing job: "
 	// + job.getJobname());
 	// getUser().getJobManager().kill(job, clean, clean);
 	// myLogger.debug("Killed job " + job.getJobname()
 	// + " in thread "
 	// + Thread.currentThread().getName());
 	// newActionStatus.addElement("Killed job: "
 	// + job.getJobname());
 	// } catch (final Exception e) {
 	// newActionStatus.addElement("Failed killing job "
 	// + job.getJobname() + ": "
 	// + e.getLocalizedMessage());
 	// newActionStatus.setFailed(true);
 	// newActionStatus.setErrorCause(e.getLocalizedMessage());
 	// myLogger.error(e.getLocalizedMessage(), e);
 	// }
 	// if (newActionStatus.getTotalElements() <= newActionStatus
 	// .getCurrentElements()) {
 	// newActionStatus.setFinished(true);
 	// }
 	//
 	// }
 	// };
 	//
 	// executor.execute(thread);
 	// }
 	//
 	// executor.shutdown();
 	//
 	// final Thread cleanupThread = new Thread() {
 	//
 	// @Override
 	// public void run() {
 	//
 	// try {
 	// executor.awaitTermination(2, TimeUnit.HOURS);
 	// } catch (final InterruptedException e1) {
 	// myLogger.error(e1.getLocalizedMessage(), e1);
 	// }
 	//
 	// try {
 	// if (clean) {
 	// for (final String mpRoot : multiJob
 	// .getAllUsedMountPoints()) {
 	//
 	// newActionStatus
 	// .addElement("Deleting common dir for mountpoint: "
 	// + mpRoot);
 	// final String url = mpRoot
 	// + multiJob
 	// .getJobProperty(Constants.RELATIVE_BATCHJOB_DIRECTORY_KEY);
 	// myLogger.debug("Deleting multijobDir: " + url);
 	// try {
 	// getUser().getFileManager().deleteFile(
 	// url, true);
 	// newActionStatus
 	// .addElement("Deleted common dir for mountpoint: "
 	// + mpRoot);
 	// } catch (final RemoteFileSystemException e) {
 	// newActionStatus
 	// .addElement("Couldn't delete common dir for mountpoint: "
 	// + mpRoot);
 	// newActionStatus.setFailed(true);
 	// newActionStatus.setErrorCause(e
 	// .getLocalizedMessage());
 	// myLogger.error("Couldn't delete multijobDir: "
 	// + url);
 	// }
 	//
 	// }
 	// }
 	//
 	// delete(multiJob);
 	// newActionStatus
 	// .addElement("Deleted multipartjob from database.");
 	//
 	// } finally {
 	// newActionStatus.setFinished(true);
 	// }
 	//
 	// }
 	// };
 	//
 	// cleanupThread.setName("deleteBatchJob " + multiJob.getBatchJobname()
 	// + " / " + getUser().getDn());
 	// cleanupThread.start();
 	//
 	// return cleanupThread;
 	// }
 	//
 	// private SortedSet<GridResource> findBestResourcesForBatchJob(
 	// BatchJob mpj) {
 	//
 	// final Map<JobSubmissionProperty, String> properties = new
 	// HashMap<JobSubmissionProperty, String>();
 	//
 	// String defaultApplication = mpj
 	// .getJobProperty(Constants.APPLICATIONNAME_KEY);
 	// if (StringUtils.isBlank(defaultApplication)) {
 	// defaultApplication = Constants.GENERIC_APPLICATION_NAME;
 	// }
 	// properties.put(JobSubmissionProperty.APPLICATIONNAME,
 	// defaultApplication);
 	//
 	// String defaultCpus = mpj.getJobProperty(Constants.NO_CPUS_KEY);
 	// if (StringUtils.isBlank(defaultCpus)) {
 	// defaultCpus = "1";
 	// }
 	// properties.put(JobSubmissionProperty.NO_CPUS,
 	// mpj.getJobProperty(Constants.NO_CPUS_KEY));
 	//
 	// String defaultVersion = mpj
 	// .getJobProperty(Constants.APPLICATIONVERSION_KEY);
 	// if (StringUtils.isBlank(defaultVersion)) {
 	// defaultVersion = Constants.NO_VERSION_INDICATOR_STRING;
 	// }
 	// properties
 	// .put(JobSubmissionProperty.APPLICATIONVERSION, defaultVersion);
 	//
 	// String maxWalltime = mpj
 	// .getJobProperty(Constants.WALLTIME_IN_MINUTES_KEY);
 	// if (StringUtils.isBlank(maxWalltime)) {
 	// int mwt = 0;
 	// for (final Job job : mpj.getJobs()) {
 	// final int wt = new Integer(
 	// job.getJobProperty(Constants.WALLTIME_IN_MINUTES_KEY));
 	// if (mwt < wt) {
 	// mwt = wt;
 	// }
 	// }
 	// maxWalltime = new Integer(mwt).toString();
 	// }
 	//
 	// properties.put(JobSubmissionProperty.WALLTIME_IN_MINUTES, maxWalltime);
 	//
 	// throw new RuntimeException("To Implement...");
 	// // final SortedSet<GridResource> result = new TreeSet<GridResource>(
 	// // getUser().getMatchMaker().findAvailableResources(properties,
 	// // mpj.getFqan()));
 	// //
 	// //
 	// // return result;
 	//
 	// }
 	//
 	// public DtoStringList getAllBatchJobnames(String application) {
 	//
 	// List<String> jobnames = null;
 	//
 	// if (StringUtils.isBlank(application)
 	// || Constants.ALLJOBS_KEY.equals(application)) {
 	// jobnames = batchJobDao.findJobNamesByDn(getUser().getDn());
 	// } else {
 	// jobnames = batchJobDao.findJobNamesPerApplicationByDn(getUser()
 	// .getDn(), application);
 	// }
 	//
 	// return DtoStringList.fromStringList(jobnames);
 	// }
 	//
 	// @Transient
 	// public BatchJob getBatchJobFromDatabase(final String batchJobname)
 	// throws NoSuchJobException {
 	//
 	// final BatchJob job = batchJobDao.findJobByDN(getUser().getDn(),
 	// batchJobname);
 	//
 	// return job;
 	//
 	// }
 	//
 	// public Set<String> getUsedApplicationsBatch() {
 	//
 	// List<BatchJob> jobs = null;
 	// jobs = batchJobDao.findBatchJobByDN(getUser().getDn());
 	//
 	// final Set<String> apps = new TreeSet<String>();
 	//
 	// for (final BatchJob job : jobs) {
 	// final String app = job
 	// .getJobProperty(Constants.APPLICATIONNAME_KEY);
 	// if (StringUtils.isNotBlank(app)) {
 	// apps.add(app);
 	// }
 	// }
 	//
 	// return apps;
 	//
 	// }
 	//
 	// public User getUser() {
 	// return user;
 	// }
 	//
 	// public String kill(final String jobname, final boolean clear)
 	// throws BatchJobException, NoSuchJobException {
 	// try {
 	// final BatchJob mpj = getBatchJobFromDatabase(jobname);
 	// deleteBatchJob(mpj, clear);
 	// return mpj.getBatchJobname();
 	// } catch (final NoSuchJobException nsje2) {
 	// throw new NoSuchJobException("No job or batchjob with name: "
 	// + jobname);
 	// } catch (final Exception e) {
 	// throw new BatchJobException(e);
 	// }
 	// }
 	//
 	// private Map<String, Integer> optimizeBatchJob(final SubmitPolicy sp,
 	// final String distributionMethod,
 	// final BatchJob possibleParentBatchJob) throws NoSuchJobException,
 	// JobPropertiesException {
 	//
 	// JobDistributor jd;
 	//
 	// if (Constants.DISTRIBUTION_METHOD_PERCENTAGE.equals(distributionMethod))
 	// {
 	// jd = new PercentageJobDistributor();
 	// } else {
 	// jd = new EqualJobDistributor();
 	// }
 	//
 	// final Map<String, Integer> results = jd.distributeJobs(
 	// sp.getCalculatedJobs(), sp.getCalculatedGridResources());
 	// final StringBuffer message = new StringBuffer(
 	// "Filled submissionlocations for "
 	// + sp.getCalculatedJobs().size() + " jobs: " + "\n");
 	// message.append("Submitted jobs to:\t\t\tAmount\n");
 	// for (final String sl : results.keySet()) {
 	// message.append(sl + "\t\t\t\t" + results.get(sl) + "\n");
 	// }
 	// myLogger.debug(message.toString());
 	//
 	// final NamedThreadFactory tf = new NamedThreadFactory("optimizeBatchJob");
 	// final ExecutorService executor = Executors.newFixedThreadPool(
 	// ServerPropertiesManager
 	// .getConcurrentMultiPartJobSubmitThreadsPerUser(), tf);
 	//
 	// final List<Exception> ex = Collections
 	// .synchronizedList(new ArrayList<Exception>());
 	//
 	// for (final Job job : sp.getCalculatedJobs()) {
 	//
 	// final Thread thread = new Thread() {
 	// @Override
 	// public void run() {
 	// try {
 	// if (job.getStatus() > JobConstants.READY_TO_SUBMIT) {
 	// try {
 	// getUser().getJobManager().kill(job);
 	// } catch (final Exception e) {
 	// myLogger.error(e.getLocalizedMessage(), e);
 	// }
 	// job.setStatus(JobConstants.READY_TO_SUBMIT);
 	// }
 	//
 	// if (Constants.NO_VERSION_INDICATOR_STRING
 	// .equals(possibleParentBatchJob
 	// .getJobProperty(Constants.APPLICATIONVERSION_KEY))) {
 	// JsdlHelpers.setApplicationVersion(
 	// job.getJobDescription(),
 	// Constants.NO_VERSION_INDICATOR_STRING);
 	// }
 	//
 	// getUser().getJobManager().processJobDescription(job,
 	// possibleParentBatchJob);
 	// getUser().getJobManager().saveOrUpdate(job);
 	// } catch (final JobPropertiesException e) {
 	// ex.add(e);
 	// executor.shutdownNow();
 	// getUser().getJobManager().saveOrUpdate(job);
 	// } catch (final NoSuchJobException e) {
 	// ex.add(e);
 	// executor.shutdownNow();
 	// getUser().getJobManager().saveOrUpdate(job);
 	// }
 	// }
 	// };
 	//
 	// executor.execute(thread);
 	// }
 	//
 	// executor.shutdown();
 	//
 	// try {
 	// executor.awaitTermination(10 * 3600, TimeUnit.SECONDS);
 	// } catch (final InterruptedException e) {
 	// executor.shutdownNow();
 	// Thread.currentThread().interrupt();
 	// return null;
 	// }
 	//
 	// if (ex.size() > 0) {
 	// throw new JobPropertiesException(
 	// "Couldn't prepare at least one job: "
 	// + ex.get(0).getLocalizedMessage());
 	// }
 	//
 	// if (possibleParentBatchJob != null) {
 	// possibleParentBatchJob.recalculateAllUsedMountPoints();
 	// batchJobDao.saveOrUpdate(possibleParentBatchJob);
 	// }
 	//
 	// return results;
 	// }
 	//
 	// public String redistributeBatchJob(String batchJobname)
 	// throws NoSuchJobException, JobPropertiesException {
 	//
 	// final BatchJob job = getBatchJobFromDatabase(batchJobname);
 	//
 	// if ((getUser().getActionStatuses().get(batchJobname) != null)
 	// && !getUser().getActionStatuses().get(batchJobname)
 	// .isFinished()) {
 	//
 	// // System.out
 	// // .println("Submission: "
 	// // + actionStatus.get(batchJobname)
 	// // .getCurrentElements() + " / "
 	// // + actionStatus.get(batchJobname).getTotalElements());
 	//
 	// // we don't want to interfere with a possible ongoing jobsubmission
 	// //
 	// myLogger.debug("not redistributing job because jobsubmission is still ongoing.");
 	// throw new JobPropertiesException(
 	// "Job submission is still ongoing in background.");
 	// }
 	//
 	// final String handleName = Constants.REDISTRIBUTE + batchJobname;
 	//
 	// final DtoActionStatus status = new DtoActionStatus(handleName, 2);
 	// getUser().getActionStatuses().put(handleName, status);
 	//
 	// Thread t =
 	// new Thread() {
 	// @Override
 	// public void run() {
 	//
 	// status.addElement("Calculating redistribution...");
 	// try {
 	// final SortedSet<GridResource> resourcesToUse =
 	// calculateResourcesToUse(job);
 	//
 	// final SubmitPolicy sp = new DefaultSubmitPolicy(
 	// job.getJobs(), resourcesToUse, null);
 	//
 	// final Map<String, Integer> results = optimizeBatchJob(
 	// sp,
 	// job.getJobProperty(Constants.DISTRIBUTION_METHOD),
 	// job);
 	//
 	// final StringBuffer optimizationResult = new StringBuffer();
 	// for (final String subLoc : results.keySet()) {
 	// optimizationResult.append(subLoc + " : "
 	// + results.get(subLoc) + "\n");
 	// }
 	// status.addLogMessage(optimizationResult.toString());
 	// job.addJobProperty(Constants.BATCHJOB_OPTIMIZATION_RESULT,
 	// optimizationResult.toString());
 	// batchJobDao.saveOrUpdate(job);
 	// status.addElement("Finished.");
 	// status.setFinished(true);
 	//
 	// } catch (final Exception e) {
 	// status.setFailed(true);
 	// status.setErrorCause(e.getLocalizedMessage());
 	// status.setFinished(true);
 	// status.addElement("Failed: " + e.getLocalizedMessage());
 	// }
 	//
 	// }
 	// };
 	// t.setName(handleName);
 	// t.start();
 	//
 	// return handleName;
 	//
 	// }
 	//
 	// public String refreshBatchJobStatus(String batchJobname)
 	// throws NoSuchJobException {
 	//
 	// final String handle = AbstractServiceInterface.REFRESH_STATUS_PREFIX
 	// + batchJobname;
 	//
 	// final DtoActionStatus status = getUser().getActionStatuses()
 	// .get(handle);
 	//
 	// if ((status != null) && !status.isFinished()) {
 	// // refresh in progress. Just give back the handle
 	// return handle;
 	// }
 	//
 	// final BatchJob multiPartJob = getBatchJobFromDatabase(batchJobname);
 	//
 	// final DtoActionStatus statusfinal = new DtoActionStatus(handle,
 	// multiPartJob.getJobs().size());
 	//
 	// getUser().getActionStatuses().put(handle, statusfinal);
 	//
 	// final NamedThreadFactory tf = new NamedThreadFactory(
 	// "refreshBatchJobStatus");
 	// final ExecutorService executor = Executors.newFixedThreadPool(
 	// ServerPropertiesManager.getConcurrentJobStatusThreadsPerUser(),
 	// tf);
 	//
 	// final Job[] currentJobs = multiPartJob.getJobs().toArray(new Job[] {});
 	//
 	// if (currentJobs.length == 0) {
 	// multiPartJob.setStatus(JobConstants.JOB_CREATED);
 	// batchJobDao.saveOrUpdate(multiPartJob);
 	// statusfinal.addLogMessage("No jobs. Returning.");
 	// statusfinal.setFailed(false);
 	// statusfinal.setFinished(true);
 	// return handle;
 	// }
 	//
 	// Arrays.sort(currentJobs);
 	//
 	// for (final Job job : currentJobs) {
 	// final Thread thread = new Thread() {
 	// @Override
 	// public void run() {
 	// statusfinal.addLogMessage("Refreshing job "
 	// + job.getJobname());
 	// getUser().getJobManager().getJobStatus(job.getJobname());
 	// statusfinal.addElement("Job status for job "
 	// + job.getJobname() + " refreshed.");
 	//
 	// if (statusfinal.getTotalElements() <= statusfinal
 	// .getCurrentElements()) {
 	// statusfinal.setFinished(true);
 	// if (multiPartJob.getFailedJobs().size() > 0) {
 	// statusfinal.setFailed(true);
 	// statusfinal
 	// .setErrorCause("Undefined error: not all subjobs accessed.");
 	// multiPartJob.setStatus(JobConstants.FAILED);
 	// } else {
 	// multiPartJob.setStatus(JobConstants.DONE);
 	// }
 	// batchJobDao.saveOrUpdate(multiPartJob);
 	// }
 	// }
 	// };
 	// executor.execute(thread);
 	// }
 	// executor.shutdown();
 	//
 	// return handle;
 	//
 	// }
 	//
 	// /**
 	// * Removes the specified job from the mulitpartJob.
 	// *
 	// * @param batchJobname
 	// * the batchJobname
 	// * @param jobname
 	// * the jobname
 	// */
 	// public void removeJobFromBatchJob(String batchJobname, String jobname)
 	// throws NoSuchJobException {
 	//
 	// final Job job =
 	// getUser().getJobManager().getJobFromDatabaseOrFileSystem(jobname);
 	// final BatchJob multiJob = getBatchJobFromDatabase(batchJobname);
 	// multiJob.removeJob(job);
 	//
 	// batchJobDao.saveOrUpdate(multiJob);
 	// }
 	//
 	// public DtoProperties restartBatchJob(final String batchJobname,
 	// String restartPolicy, DtoProperties properties)
 	// throws NoSuchJobException, JobPropertiesException {
 	//
 	// final BatchJob job = getBatchJobFromDatabase(batchJobname);
 	//
 	// if ((getUser().getActionStatuses().get(batchJobname) != null)
 	// && !getUser().getActionStatuses().get(batchJobname)
 	// .isFinished()) {
 	//
 	// // System.out
 	// // .println("Submission: "
 	// // + actionStatus.get(batchJobname)
 	// // .getCurrentElements() + " / "
 	// // + actionStatus.get(batchJobname).getTotalElements());
 	//
 	// // we don't want to interfere with a possible ongoing jobsubmission
 	// //
 	// myLogger.debug("not restarting job because jobsubmission is still ongoing.");
 	// throw new JobPropertiesException(
 	// "Job submission is still ongoing in background.");
 	// }
 	//
 	// final DtoActionStatus status = new DtoActionStatus(batchJobname, 3);
 	// getUser().getActionStatuses().put(batchJobname, status);
 	//
 	// status.addElement("Finding resources to use...");
 	// final SortedSet resourcesToUse = calculateResourcesToUse(job);
 	//
 	// status.addElement("Investigating batchjob...");
 	// if (properties == null) {
 	// properties = DtoProperties
 	// .createProperties(new HashMap<String, String>());
 	// }
 	//
 	// SubmitPolicy sp = null;
 	//
 	// if (Constants.SUBMIT_POLICY_RESTART_DEFAULT.equals(restartPolicy)) {
 	// sp = new DefaultResubmitSubmitPolicy(job.getJobs(), resourcesToUse,
 	// properties.propertiesAsMap());
 	// } else if (Constants.SUBMIT_POLICY_RESTART_SPECIFIC_JOBS
 	// .equals(restartPolicy)) {
 	// sp = new RestartSpecificJobsRestartPolicy(job.getJobs(),
 	// resourcesToUse, properties.propertiesAsMap());
 	// } else {
 	// throw new JobPropertiesException("Restart policy \""
 	// + restartPolicy + "\" not supported.");
 	// }
 	//
 	// if ((sp.getCalculatedGridResources().size() == 0)
 	// || (sp.getCalculatedJobs().size() == 0)) {
 	//
 	// status.addElement("No locations or no jobs to submit found. Doing nothing...");
 	// status.setFinished(true);
 	// // nothing we can do...
 	// return DtoProperties
 	// .createProperties(new HashMap<String, String>());
 	// } else {
 	// status.setTotalElements(3 + (sp.getCalculatedJobs().size() * 2));
 	// status.addLogMessage("Found " + sp.getCalculatedJobs().size()
 	// + " jobs to resubmit.");
 	// }
 	//
 	// status.addElement("Optimizing job distribution...");
 	// final Map<String, Integer> results = optimizeBatchJob(sp,
 	// job.getJobProperty(Constants.DISTRIBUTION_METHOD), job);
 	//
 	// batchJobDao.saveOrUpdate(job);
 	//
 	// final NamedThreadFactory tf = new NamedThreadFactory("restartBatchJob");
 	// final ExecutorService executor = Executors.newFixedThreadPool(
 	// ServerPropertiesManager
 	// .getConcurrentMultiPartJobSubmitThreadsPerUser(), tf);
 	//
 	// for (final Job jobToRestart : sp.getCalculatedJobs()) {
 	//
 	// final Thread thread = new Thread() {
 	// @Override
 	// public void run() {
 	// try {
 	// status.addElement("Starting resubmission of job: "
 	// + jobToRestart.getJobname());
 	// getUser().getJobManager()
 	// .restartJob(jobToRestart, null);
 	// status.addElement("Resubmission of job "
 	// + jobToRestart.getJobname() + " successful.");
 	// } catch (final JobSubmissionException e) {
 	// status.addElement("Resubmission of job "
 	// + jobToRestart.getJobname() + " failed: "
 	// + e.getLocalizedMessage());
 	// status.setFailed(true);
 	// status.setErrorCause(e.getLocalizedMessage());
 	// myLogger.debug(e.getLocalizedMessage(), e);
 	// } catch (final NoSuchJobException e1) {
 	// status.addElement("Resubmission of job "
 	// + jobToRestart.getJobname() + " failed: "
 	// + e1.getLocalizedMessage());
 	// status.setFailed(true);
 	// myLogger.debug(e1.getLocalizedMessage(), e1);
 	// }
 	//
 	// if (status.getTotalElements() <= status
 	// .getCurrentElements()) {
 	// status.setFinished(true);
 	// }
 	// }
 	// };
 	// executor.execute(thread);
 	// }
 	//
 	// executor.shutdown();
 	//
 	// return DtoProperties.createUserPropertiesIntegerValue(results);
 	//
 	// }
 	//
 	// public void saveOrUpdate(BatchJob instance) {
 	// batchJobDao.saveOrUpdate(instance);
 	// }
 	//
 	// private void submitBatchJob(final BatchJob multiJob)
 	// throws JobSubmissionException, NoSuchJobException {
 	//
 	// final DtoActionStatus newActionStatus = new DtoActionStatus(
 	// multiJob.getBatchJobname(), 100);
 	// getUser().getActionStatuses().put(multiJob.getBatchJobname(),
 	// newActionStatus);
 	//
 	// final NamedThreadFactory tf = new NamedThreadFactory("submitBatchJob");
 	// final ExecutorService executor = Executors
 	// .newFixedThreadPool(ServerPropertiesManager
 	// .getConcurrentMultiPartJobSubmitThreadsPerUser());
 	//
 	// final Job[] currentlyCreatedJobs = multiJob.getJobs().toArray(
 	// new Job[] {});
 	// Arrays.sort(currentlyCreatedJobs);
 	//
 	// final int totalNumberOfJobs = currentlyCreatedJobs.length;
 	// newActionStatus.setTotalElements(totalNumberOfJobs);
 	//
 	// for (final Job job : currentlyCreatedJobs) {
 	//
 	// if (job.getStatus() != JobConstants.READY_TO_SUBMIT) {
 	// continue;
 	// }
 	// final Thread thread = new Thread() {
 	// @Override
 	// public void run() {
 	//
 	// Exception exc = null;
 	// for (int i = 0; i < getUser().DEFAULT_JOB_SUBMISSION_RETRIES; i++) {
 	// try {
 	// exc = null;
 	//
 	// DtoActionStatus status = null;
 	// status = new DtoActionStatus(job.getJobname(), 0);
 	// getUser().getActionStatuses().put(job.getJobname(),
 	// status);
 	//
 	// getUser().getJobManager().submitJob(job, true,
 	// status);
 	// newActionStatus.addElement("Added job: "
 	// + job.getJobname());
 	//
 	// break;
 	// } catch (final Exception e) {
 	// myLogger.error("Job submission for batchjob: "
 	// + multiJob.getBatchJobname() + ", "
 	// + job.getJobname() + " failed: "
 	// + e.getLocalizedMessage());
 	// myLogger.error("Trying again...");
 	// newActionStatus
 	// .addLogMessage("Failed to submit job "
 	// + job.getJobname() + ": "
 	// + e.getLocalizedMessage()
 	// + ". Trying again...");
 	// exc = e;
 	// executor.shutdownNow();
 	// }
 	// }
 	//
 	// if (exc != null) {
 	// newActionStatus.setFailed(true);
 	// newActionStatus.setErrorCause("Tried to resubmit job "
 	// + job.getJobname() + " "
 	// + getUser().DEFAULT_JOB_SUBMISSION_RETRIES
 	// + " times. Never worked. Giving up...");
 	// myLogger.error("Tried to resubmit job "
 	// + job.getJobname() + " "
 	// + getUser().DEFAULT_JOB_SUBMISSION_RETRIES
 	// + " times. Never worked. Giving up...");
 	// multiJob.addFailedJob(job.getJobname());
 	// batchJobDao.saveOrUpdate(multiJob);
 	// newActionStatus.addElement("Tried to resubmit job "
 	// + job.getJobname() + " "
 	// + getUser().DEFAULT_JOB_SUBMISSION_RETRIES
 	// + " times. Never worked. Giving up...");
 	// executor.shutdownNow();
 	//
 	// }
 	//
 	// if (newActionStatus.getCurrentElements() >= newActionStatus
 	// .getTotalElements()) {
 	// newActionStatus.setFinished(true);
 	// multiJob.setStatus(JobConstants.ACTIVE);
 	// batchJobDao.saveOrUpdate(multiJob);
 	// }
 	//
 	// }
 	// };
 	//
 	// executor.execute(thread);
 	// }
 	// executor.shutdown();
 	//
 	// }
 	//
 	// public String submitBatchJob(final String jobname)
 	// throws NoSuchJobException {
 	// final BatchJob multiJob = getBatchJobFromDatabase(jobname);
 	//
 	// final String handle = "submision_status_batch_" + jobname + "_"
 	// + new Date().getTime();
 	// final DtoActionStatus status = new DtoActionStatus(handle, 0);
 	// getUser().getActionStatuses().put(handle, status);
 	//
 	//
 	// final Thread t = new Thread() {
 	// @Override
 	// public void run() {
 	// try {
 	// submitBatchJob(multiJob);
 	// } catch (final JobSubmissionException e) {
 	// status.setFailed(true);
 	// status.setFinished(true);
 	// status.setErrorCause(e.getLocalizedMessage());
 	// myLogger.error(e.getLocalizedMessage(), e);
 	// } catch (final NoSuchJobException e) {
 	// status.setFailed(true);
 	// status.setFinished(true);
 	// status.setErrorCause(e.getLocalizedMessage());
 	// myLogger.error(e.getLocalizedMessage(), e);
 	// } catch (final Throwable e) {
 	// status.setFailed(true);
 	// status.setFinished(true);
 	// status.setErrorCause(e.getLocalizedMessage());
 	// myLogger.error(e.getLocalizedMessage(), e);
 	// }
 	// }
 	// };
 	// t.setName(status.getHandle());
 	// t.start();
 	// return handle;
 	// }
 	//
 	// public void uploadInputFile(String jobname, final DataHandler source,
 	// final String targetFilename) throws NoSuchJobException,
 	// RemoteFileSystemException {
 	//
 	// final BatchJob multiJob = getBatchJobFromDatabase(jobname);
 	//
 	// multiJob.setStatus(JobConstants.INPUT_FILES_UPLOADING);
 	// saveOrUpdate(multiJob);
 	//
 	// final String relpathFromMountPointRoot = multiJob
 	// .getJobProperty(Constants.RELATIVE_BATCHJOB_DIRECTORY_KEY);
 	//
 	// final Set<String> urls = new HashSet<String>();
 	//
 	// for (final String mountPointRoot : multiJob.getAllUsedMountPoints()) {
 	//
 	// final String parent = mountPointRoot + "/"
 	// + relpathFromMountPointRoot;
 	// urls.add(parent);
 	// }
 	//
 	// final DtoActionStatus status = new DtoActionStatus(targetFilename,
 	// multiJob.getAllUsedMountPoints().size());
 	// getUser().getActionStatuses().put(targetFilename, status);
 	//
 	// getUser().getFileManager().uploadFileToMultipleLocations(urls, source,
 	// targetFilename, status);
 	//
 	// }
 
 }
