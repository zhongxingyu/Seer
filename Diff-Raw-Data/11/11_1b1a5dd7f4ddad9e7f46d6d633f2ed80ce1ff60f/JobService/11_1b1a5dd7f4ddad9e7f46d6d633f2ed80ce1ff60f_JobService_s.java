 package de.otto.jobstore.service;
 
 import de.otto.jobstore.common.*;
 import de.otto.jobstore.common.properties.JobInfoProperty;
 import de.otto.jobstore.common.util.InternetUtils;
 import de.otto.jobstore.repository.JobDefinitionRepository;
 import de.otto.jobstore.repository.JobInfoRepository;
 import de.otto.jobstore.service.exception.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import java.util.*;
 import java.util.concurrent.*;
 
 /**
  *  This service allows to handle multiple jobs and their associated runnables. A job has to be registered before it
  *  can be executed or cued. The service allows only one queued and one running job for each distinct job name.
  *
  *  In order to execute jobs they have to be queued and afterwards executed by callings {#executeQueuedJobs}. By adding
  *  running constraints it is possible to define jobs that are not allowed to run at the same time.
  */
 public class JobService {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(JobService.class);
 
     private final Map<String, JobRunnable> jobs = new ConcurrentHashMap<>();
     private final Set<Set<String>> runningConstraints = new CopyOnWriteArraySet<>();
     private final JobDefinitionRepository jobDefinitionRepository;
     private final JobInfoRepository jobInfoRepository;
     private long jobInfoCacheUpdateInterval = 10000;
 
     /**
      * Creates a JobService Object.
      *
      * @param jobDefinitionRepository
      * @param jobInfoRepository The jobInfo Repository to store the jobs in
      */
     public JobService(JobDefinitionRepository jobDefinitionRepository, final JobInfoRepository jobInfoRepository) {
         this.jobDefinitionRepository = jobDefinitionRepository;
         this.jobInfoRepository = jobInfoRepository;
         this.jobDefinitionRepository.addOrUpdate(StoredJobDefinition.JOB_EXEC_SEMAPHORE);
     }
 
     public boolean isExecutionEnabled() {
         return isJobEnabled(StoredJobDefinition.JOB_EXEC_SEMAPHORE.getName());
     }
 
     /**
      * Disables or enables Job execution. Default value is true.
      *
      * @param executionEnabled true - Jobs will be executed<br/>
      *                         false - No jobs will be executed
      */
     public void setExecutionEnabled(boolean executionEnabled) {
         jobDefinitionRepository.setJobExecutionEnabled(StoredJobDefinition.JOB_EXEC_SEMAPHORE.getName(), executionEnabled);
     }
 
     /**
      * Registers a job with the given runnable in this job service
      *
      * @param jobRunnable The jobRunnable
      * @return true - The job was successfully registered<br>
      *     false - A job with the given name is already registered
      */
     public boolean registerJob(final JobRunnable jobRunnable) {
         final JobDefinition jobDefinition = jobRunnable.getJobDefinition();
         final String name = jobDefinition.getName();
         final boolean inserted;
         if (jobs.containsKey(name)) {
             LOGGER.warn("ltag=JobService.createJob.registerJob Tried to re-register job with name={}", name);
             inserted = false;
         } else {
             jobs.put(name, jobRunnable);
             jobDefinitionRepository.addOrUpdate(new StoredJobDefinition(jobDefinition));
             inserted = true;
         }
         return inserted;
     }
 
     /**
      * Check if execution of a job is enabled
      *
      * @param name The name of the job to check
      * @return true - The execution of the job is enabled<br>
      *        false - The execution of the job is disabled
      * @throws JobNotRegisteredException If the job is not registered with this jobService instance
      */
     public boolean isJobExecutionEnabled(final String name) throws JobNotRegisteredException {
         final StoredJobDefinition jobDefinition = jobDefinitionRepository.find(checkJobName(name));
         return !jobDefinition.isDisabled();
     }
 
     /**
      * Disable or enable the execution of a job
      *
      * @param name The name of the job
      * @param executionEnabled true - The execution of the job is enabled<br>
      *        false - The execution of the job is disabled
      * @throws JobNotRegisteredException If the job is not registered with this jobService instance
      */
     public void setJobExecutionEnabled(String name, boolean executionEnabled) throws JobNotRegisteredException {
         jobDefinitionRepository.setJobExecutionEnabled(checkJobName(name), executionEnabled);
     }
 
     /**
      * Adds a running constraint to this JobService instance.
      *
      * @param constraint The names of the jobs that are not allowed to run at the same time
      * @return true - If the running constraint was successfully added<br>
      *     false - If the running constraint already exists
      * @throws de.otto.jobstore.service.exception.JobNotRegisteredException Thrown if the constraint contains a name of
      * a job which is not registered with this JobService instance
      */
     public boolean addRunningConstraint(final Set<String> constraint) throws JobNotRegisteredException {
         for (String name : constraint) {
             checkJobName(name);
         }
         return runningConstraints.add(Collections.unmodifiableSet(constraint));
     }
 
     /**
      * Removes a job with the given from the queue and sets its result state to not executed.
      *
      * @param name The name of the job
      * @return true - If a queued job with the given name was found</br>
      *         false - If no queued job with the given name could be found
      *
      */
     public boolean removeJobFromQueue(String name) {
         return jobInfoRepository.markQueuedAsNotExecuted(name);
     }
 
     /**
      * Executes a job with the given name and returns its ID. If a job is already running or running it would violate
      * running constraints it this job will be added to the queue. If a job is already queued an exception will be thrown.
      *
      * @param name The name of the job to execute
      * @return The id of the executing or queued job
      * @throws JobNotRegisteredException Thrown if no job with the given name was registered with this JobService instance
      * @throws JobAlreadyQueuedException If a job with the given name is already queued for execution or another
      * JobService instance queued the job while this method was executed
      * @throws JobAlreadyRunningException If another JobService instance executed a job with the given name while this
      * method was executed
      * @throws JobExecutionNotNecessaryException If the execution of the job was not necessary
      * @throws JobExecutionDisabledException If job execution has been disabled
      */
     public String executeJob(final String name) throws JobNotRegisteredException, JobAlreadyQueuedException,
             JobAlreadyRunningException, JobExecutionNotNecessaryException, JobExecutionDisabledException {
         return executeJob(name, JobExecutionPriority.CHECK_PRECONDITIONS);
     }
 
     /**
      * Executes a job with the given name and returns its ID. If a job is already running or running it would violate
      * running constraints it this job will be added to the queue. If a job is already queued an exception will be thrown.
      *
      * @param name The name of the job to execute
      * @param executionPriority The priority with which the job is to be executed
      * @return The id of the executing or queued job
      * @throws JobNotRegisteredException Thrown if no job with the given name was registered with this JobService instance
      * @throws JobAlreadyQueuedException If a job with the given name is already queued for execution or another
      * JobService instance queued the job while this method was executed
      * @throws JobAlreadyRunningException If another JobService instance executed a job with the given name while this
      * method was executed
      * @throws JobExecutionNotNecessaryException If the execution of the job was not necessary
      * @throws JobExecutionDisabledException If job execution has been disabled
      */
     public String executeJob(final String name, final JobExecutionPriority executionPriority) throws JobNotRegisteredException,
             JobAlreadyQueuedException, JobAlreadyRunningException, JobExecutionNotNecessaryException,
             JobExecutionDisabledException {
         final String id;
         final StoredJobDefinition jobDefinition = getJobDefinition(checkJobName(name));
         final JobRunnable runnable = jobs.get(name);
         if (jobDefinition.isDisabled()) {
             throw new JobExecutionDisabledException("Execution of jobs with name " + jobDefinition.getName() + " has been disabled");
         }
         if (!isExecutionEnabled()) {
             throw new JobExecutionDisabledException("Execution of jobs has been disabled");
         }
         final JobInfo queuedJobInfo = jobInfoRepository.findByNameAndRunningState(name, RunningState.QUEUED);
         if (queuedJobInfo != null) {
             if (queuedJobInfo.getExecutionPriority().hasLowerPriority(executionPriority)) {
                 jobInfoRepository.remove(queuedJobInfo.getId());
                 id = queueJob(runnable, executionPriority, "A job with name " + name + " is already running and queued for execution");
             } else {
                 throw new JobAlreadyQueuedException("A job with name " + name + " is already queued for execution");
             }
         } else {
             final JobInfo runningJobInfo = jobInfoRepository.findByNameAndRunningState(name, RunningState.RUNNING);
             if (runningJobInfo == null) {
                 id = queueJob(runnable, executionPriority, "A job with name " + name + " is already running and queued for execution");
                 LOGGER.debug("ltag=JobService.createJob.executingJob jobInfoName={}", name);
                 executeQueuedJob(runnable, id, executionPriority);
             } else if (runningJobInfo.getExecutionPriority().hasLowerPriority(executionPriority)) {
                 id = queueJob(runnable, executionPriority, "A job with name " + name + " is already running and queued for execution");
             } else {
                 throw new JobExecutionNotNecessaryException("Execution of job " + name + " was not necessary");
             }
         }
         return id;
     }
 
     public void abortJob(String id) {
         jobInfoRepository.abortJob(id);
     }
 
     /**
      * Executes all queued jobs registered with this JobService instance asynchronously in the order they were queued.
      */
     public void executeQueuedJobs() {
         if (isExecutionEnabled()) {
             LOGGER.info("ltag=JobService.executeQueuedJobs");
             for (JobInfo jobInfo : jobInfoRepository.findQueuedJobsSortedAscByCreationTime()) {
                 final StoredJobDefinition jobDefinition = getJobDefinition(jobInfo.getName());
                 if (jobDefinition.isDisabled()) {
                     LOGGER.info("ltag=JobService.executeQueuedJobs.isDisabled jobName={}", jobInfo.getName());
                 } else if (!jobs.containsKey(jobInfo.getName())) {
                     LOGGER.info("ltag=JobService.executeQueuedJobs.notRegistered jobName={}", jobInfo.getName());
                 } else {
                     final JobRunnable runnable = jobs.get(jobInfo.getName());
                     executeQueuedJob(runnable, jobInfo.getId(), jobInfo.getExecutionPriority());
                 }
             }
         }
     }
 
     /**
      * Polls all remote jobs and updates their status if necessary
      */
     public void pollRemoteJobs() {
         if (isExecutionEnabled()) {
             for (JobRunnable jobRunnable : jobs.values()) {
                 if (jobRunnable.getJobDefinition().isRemote()) {
                     final JobDefinition definition = jobRunnable.getJobDefinition();
                     final JobInfo runningJob = jobInfoRepository.findByNameAndRunningState(definition.getName(), RunningState.RUNNING);
                     if (runningJob != null && jobRequiresUpdate(runningJob.getLastModifiedTime(), System.currentTimeMillis(), definition.getPollingInterval()) &&
                             runningJob.getAdditionalData().containsKey(JobInfoProperty.REMOTE_JOB_URI.val())) {
                         final JobRunnable runnable = jobs.get(definition.getName());
                         final RemoteJobStatus remoteJobStatus = runnable.getRemoteStatus(
                                 createJobExecutionContext(runningJob.getId(), runningJob.getName(), runningJob.getExecutionPriority(), null));
                         if (remoteJobStatus != null) {
                             updateJobStatus(runningJob, runnable, remoteJobStatus);
                         }
                     } else {
                         LOGGER.info("ltag=JobService.pollRemoteJobs jobName={} " + runningJob == null ? "has no running instance." : "is still fresh.", definition.getName());
                     }
                 }
             }
         }
     }
 
     @PostConstruct
     public void startup() {
         shutdown = false;
     }
 
     /**
      * Stops all jobs registered with this JobService and running on this host.
      */
     @PreDestroy
     public void shutdownJobs() {
        shutdown = true;

         if (isExecutionEnabled()) {
             for (JobRunnable jobRunnable : jobs.values()) {
                 if (!jobRunnable.getJobDefinition().isRemote()) {
                     final String name = jobRunnable.getJobDefinition().getName();
                     final JobInfo runningJob = jobInfoRepository.findByNameAndRunningState(name, RunningState.RUNNING);
                     if (runningJob != null && runningJob.getHost().equals(InternetUtils.getHostName())) {
                         LOGGER.info("ltag=JobService.shutdownJobs jobInfoName={}", name);
                         abortJob(runningJob.getId());
                         jobInfoRepository.markAsFinished(runningJob.getId(), ResultCode.ABORTED, "shutdownJobs called from executing host");
                     }
                 }
             }
         }
 
         try {
             jobExecutorService.awaitTermination(30, TimeUnit.SECONDS);
         } catch (InterruptedException e) {
             LOGGER.warn("could not terminate all running threads");
         }
     }
 
     /**
      * Removed all registered jobs and constraints from the JobService instance
      */
     public void clean() {
         jobs.clear();
         runningConstraints.clear();
     }
 
     /**
      * Returns the Names of all registered jobs
      */
     public Collection<String> listJobNames() {
         final List<String> jobNames = new ArrayList<>(jobs.keySet());
         Collections.sort(jobNames);
         return jobNames;
     }
 
     /**
      * Returns all registered JobRunnable Objects
      */
     public Collection<JobRunnable> listJobRunnables() {
         final List<JobRunnable> jobRunnables = new ArrayList<>(jobs.values());
         Collections.sort(jobRunnables, new Comparator<JobRunnable>() {
             @Override
             public int compare(JobRunnable o1, JobRunnable o2) {
                 return o1.getJobDefinition().getName().compareTo(o2.getJobDefinition().getName());
             }
         });
         return jobRunnables;
     }
 
     /**
      * Returns the Set of all constraints
      */
     public Set<Set<String>> listRunningConstraints() {
         return Collections.unmodifiableSet(runningConstraints);
     }
 
     private boolean jobRequiresUpdate(Date lastModificationTime, long currentTime, long pollingInterval) {
         return new Date(currentTime - pollingInterval).after(lastModificationTime);
     }
 
     private void updateJobStatus(JobInfo jobInfo, JobRunnable runnable, RemoteJobStatus remoteJobStatus) {
         LOGGER.info("ltag=JobService.updateJobStatus jobName={} jobId={} status={}", new Object[]{jobInfo.getName(), jobInfo.getId(), remoteJobStatus.status});
         if (remoteJobStatus.logLines != null && !remoteJobStatus.logLines.isEmpty()) {
             jobInfoRepository.appendLogLines(jobInfo.getId(), remoteJobStatus.logLines);
         }
         if (remoteJobStatus.message != null && remoteJobStatus.message.length() > 0) {
             jobInfoRepository.setStatusMessage(jobInfo.getId(), remoteJobStatus.message);
         }
         if (remoteJobStatus.status == RemoteJobStatus.Status.FINISHED) {
             LOGGER.info("ltag=JobService.updateJobStatus.statusFinish jobName={} result={}", jobInfo.getName(), remoteJobStatus.result);
             final JobExecutionContext context = createJobExecutionContext(jobInfo.getId(), jobInfo.getName(), jobInfo.getExecutionPriority(), remoteJobStatus.logLines);
             context.setResultCode(remoteJobStatus.result.ok ? ResultCode.SUCCESSFUL : ResultCode.FAILED);
             context.setResultMessage(remoteJobStatus.message);
             if (remoteJobStatus.result.ok) {
                 try {
                     runnable.afterExecution(context);
                     jobInfoRepository.markAsFinished(context.getId(), context.getResultCode(), remoteJobStatus.result.message);
                 } catch (Exception e) {
                     LOGGER.error("ltag=JobService.updateJobStatus.afterExecution jobName=" + jobInfo.getName() + " jobId=" + jobInfo.getId() + " failed: " + e.getMessage(), e);
                     jobInfoRepository.markAsFinished(context.getId(), e);
                 }
             } else {
                 LOGGER.warn("ltag=JobService.updateJobStatus.resultNotOk jobName={} jobId={} exitCode={} message={}",
                         new Object[]{jobInfo.getName(), jobInfo.getId(), remoteJobStatus.result.exitCode, remoteJobStatus.result.message});
                 jobInfoRepository.addAdditionalData(jobInfo.getId(), "exitCode", String.valueOf(remoteJobStatus.result.exitCode));
                 jobInfoRepository.markAsFinished(jobInfo.getId(), ResultCode.FAILED, remoteJobStatus.result.message);
             }
         }
     }
 
 
 
     private ExecutorService jobExecutorService = Executors.newCachedThreadPool();
 
     private volatile boolean shutdown = false;
 
     private void executeJob(JobRunnable runnable, String id, JobExecutionPriority executionPriority) {
         final JobDefinition definition = runnable.getJobDefinition();
 
         jobExecutorService.execute(new JobExecutionRunnable(
                 runnable, jobInfoRepository, createJobExecutionContext(id, definition.getName(), executionPriority, null)));
     }
 
     private JobExecutionContext createJobExecutionContext(String jobId, String jobName, JobExecutionPriority priority, List<String> logLines) {
         final JobLogger jobLogger = new SimpleJobLogger(jobId, jobName, jobInfoRepository, logLines);
         final JobInfoCache jobInfoCache = new JobInfoCache(jobId, jobInfoRepository, jobInfoCacheUpdateInterval);
         return new JobExecutionContext(jobId, jobLogger, jobInfoCache, priority);
     }
 
     private void executeQueuedJob(JobRunnable runnable, String id, JobExecutionPriority executionPriority) {
         final String name = runnable.getJobDefinition().getName();
         if (jobInfoRepository.hasJob(name, RunningState.RUNNING)) {
             LOGGER.info("ltag=JobService.executeQueuedJob.alreadyRunning jobInfoName={} jobInfoId={}", name, id);
         } else if (violatesRunningConstraints(name)) {
             LOGGER.info("ltag=JobService.executeQueuedJob.violatesRunningConstraints jobInfoName={} jobInfoId={}", name, id);
         } else {
             activateQueuedJob(runnable, id, executionPriority);
         }
     }
 
     private String queueJob(JobRunnable runnable, JobExecutionPriority jobExecutionPriority, String exceptionMessage)
             throws JobAlreadyQueuedException{
         final JobDefinition jobDefinition = runnable.getJobDefinition();
         final String id = jobInfoRepository.create(jobDefinition.getName(), jobDefinition.getMaxIdleTime(), jobDefinition.getMaxExecutionTime(),
                 RunningState.QUEUED, jobExecutionPriority, runnable.getParameters(), null);
         if (id == null) {
             throw new JobAlreadyQueuedException(exceptionMessage);
         }
         return id;
     }
 
     private void activateQueuedJob(JobRunnable runnable, String id, JobExecutionPriority executionPriority) {
         final String name = runnable.getJobDefinition().getName();
         if (jobInfoRepository.activateQueuedJob(name)) {
             jobInfoRepository.updateHostThreadInformation(id);
             LOGGER.info("ltag=JobService.activateQueuedJob.activate jobInfoName={} jobInfoId={}", name, id);
             executeJob(runnable, id, executionPriority);
         } else {
             LOGGER.warn("ltag=JobService.activateQueuedJob.jobIsNotQueuedAnyMore jobInfoName={} jobInfoId={}", name, id);
         }
     }
 
     private String checkJobName(final String name) throws JobNotRegisteredException {
         if (jobs.containsKey(name)) {
             return name;
         } else {
             throw new JobNotRegisteredException("job with name " + name + " is not registered with this jobService instance");
         }
     }
 
     private boolean violatesRunningConstraints(final String name) {
         for (Set<String> constraint : runningConstraints) {
             if (constraint.contains(name)) {
                 for (String constraintJobName : constraint) {
                     if (jobInfoRepository.hasJob(constraintJobName, RunningState.RUNNING)) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     private boolean isJobEnabled(String name) {
         if(shutdown) return false;
 
         final StoredJobDefinition semaphore = getJobDefinition(name);
         return !semaphore.isDisabled();
     }
 
     private StoredJobDefinition getJobDefinition(String name) {
         return jobDefinitionRepository.find(name);
     }
 
 }
