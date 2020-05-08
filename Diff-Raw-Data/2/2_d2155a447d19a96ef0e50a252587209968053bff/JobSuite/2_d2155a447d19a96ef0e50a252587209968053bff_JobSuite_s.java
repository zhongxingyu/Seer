 /* Copyright 2010-2014 Norconex Inc.
  *
  * This file is part of Norconex JEF.
  *
  * Norconex JEF is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Norconex JEF is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Norconex JEF. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.norconex.jef4.suite;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.lang.reflect.InvocationTargetException;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.reflect.MethodUtils;
 import org.apache.log4j.Appender;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 
 import com.norconex.commons.lang.file.FileUtil;
 import com.norconex.jef4.JEFException;
 import com.norconex.jef4.JEFUtil;
 import com.norconex.jef4.job.IJob;
 import com.norconex.jef4.job.IJobErrorListener;
 import com.norconex.jef4.job.IJobLifeCycleListener;
 import com.norconex.jef4.job.IJobVisitor;
 import com.norconex.jef4.job.JobErrorEvent;
 import com.norconex.jef4.job.JobException;
 import com.norconex.jef4.job.group.IJobGroup;
 import com.norconex.jef4.log.FileLogManager;
 import com.norconex.jef4.log.ILogManager;
 import com.norconex.jef4.status.FileJobStatusStore;
 import com.norconex.jef4.status.IJobStatus;
 import com.norconex.jef4.status.IJobStatusStore;
 import com.norconex.jef4.status.IJobStatusVisitor;
 import com.norconex.jef4.status.JobState;
 import com.norconex.jef4.status.JobStatusUpdater;
 import com.norconex.jef4.status.JobSuiteStatusSnapshot;
 import com.norconex.jef4.status.MutableJobStatus;
 
 
 //TODO rename JobExecutor and more to root package?
 
 /**
  * A job suite is an amalgamation of jobs, represented as a single executable
  * unit.  It can be seen as of one big job made of several sub-jobs.
  * Configurations applied to a suite affects all jobs associated
  * with the suite.
  * All jobs making up a suite must have unique identifiers.
  * @author Pascal Essiembre
  */
 @SuppressWarnings("nls")
 public final class JobSuite {
 
     //--- NEW STUFF ------------------------------------------------------------
     private static final Logger LOG = LogManager.getLogger(JobSuite.class);
     
     /** Associates job id with current thread. */
     private static final ThreadLocal<String> CURRENT_JOB_ID = 
             new ThreadLocal<String>();
     
 
     
     private final Map<String, IJob> jobs = new HashMap<>();
     private final IJob rootJob;
     private final JobSuiteConfig config;
     private final String workdir;
     private final ILogManager logManager;
     private final IJobStatusStore jobStatusStore;
     private JobSuiteStatusSnapshot jobSuiteStatusSnapshot;
     private final List<IJobLifeCycleListener> jobLifeCycleListeners;
     private final List<IJobErrorListener> jobErrorListeners;
     private final List<ISuiteLifeCycleListener> suiteLifeCycleListeners;
     private final JobHeartbeatGenerator heartbeatGenerator;
     
 
     public JobSuite(final IJob rootJob) {
         this(rootJob, new JobSuiteConfig());
     }
 
     public JobSuite(final IJob rootJob, JobSuiteConfig config) {
         super();
         this.rootJob = rootJob;
         this.config = config;
         this.workdir = resolveWorkdir(config.getWorkdir());
         this.logManager = resolveLogManager(config.getLogManager());
         this.jobStatusStore = 
                 resolveJobStatusStore(config.getJobStatusStore());
         this.jobLifeCycleListeners = 
                 Collections.unmodifiableList(config.getJobLifeCycleListeners());
         this.suiteLifeCycleListeners = Collections.unmodifiableList(
                 config.getSuiteLifeCycleListeners());
         this.jobErrorListeners = 
                 Collections.unmodifiableList(config.getJobErrorListeners());
         this.heartbeatGenerator = new JobHeartbeatGenerator(this);
         
         accept(new IJobVisitor() {
             @Override
             public void visitJob(IJob job, IJobStatus jobStatus) {
                 jobs.put(job.getId(), job);
             }
         });
     }
 
     public IJob getRootJob() {
         return rootJob;
     }
     public JobSuiteConfig getConfig() {
         return config;
     }
 
     public IJobStatus getJobStatus(IJob job) {
         if (job == null) {
             return null;
         }
         return getJobStatus(job.getId());
     }
     public IJobStatus getJobStatus(String jobId) {
         return jobSuiteStatusSnapshot.getJobStatus(jobId);
     }
     
     public boolean execute() {
         return execute(false);
     }
     public boolean execute(boolean resumeIfIncomplete) {
         boolean success = false;
         try {
             success = doExecute(resumeIfIncomplete);
         } catch (Throwable e) {
             LOG.fatal("Job suite execution failed: " + getId(), e);
         } finally {
             fire(suiteLifeCycleListeners, "suiteFinished", this);
         }
         if (!success) {
             fire(suiteLifeCycleListeners, "suiteAborted", this);
         }
         return success;
     }
     
     
     public void accept(IJobStatusVisitor visitor) {
         jobSuiteStatusSnapshot.accept(visitor);
     }
     
     /**
      * Accepts a job suite visitor.
      * @param visitor job suite visitor
      * @since 1.1
      */
     public void accept(IJobVisitor visitor) {
         accept(visitor, null);
     }
     /**
      * Accepts a job suite visitor, filtering jobs and job progresses to
      * those of the same type as the specified job class instance.
      * @param visitor job suite visitor
      * @param jobClassFilter type to filter jobs and job progresses
      * @since 1.1
      */
     public void accept(IJobVisitor visitor, Class<IJob> jobClassFilter) {
         accept(visitor, getRootJob(), jobClassFilter);
     }    
 
     
     /**
      * Gets the job identifier representing the currently running job for the
      * current thread.
      * @return job identifier or <code>null</code> if no job is currently
      *         associated with the current thread
      */
     public static String getCurrentJobId() {
         return CURRENT_JOB_ID.get();
     }
     /**
      * Sets a job identifier as the currently running job for the
      * the current thread.  This method is called by the framework.
      * Framework users may call this method when implementing their own 
      * threads to associated a job with the thread.  Framework code
      * may rely on this to behave as expected.  Otherwise, it is best 
      * advised not to use this method.
      * @param jobId job identifier
      */
     public static void setCurrentJobId(String jobId) {
         CURRENT_JOB_ID.set(jobId);
     }
     
     /*default*/ IJobStatusStore getJobStatusStore() {
         return jobStatusStore;
     }
     public String getId() {
         return getRootJob().getId();
     }
     public String getWorkdir() {
         return workdir;
     }
     public ILogManager getLogManager() {
         return logManager;
     }
     /*default*/ File getSuiteIndexFile() {
         File indexDir = new File(getWorkdir() + File.separator + "latest");
         if (!indexDir.exists()) {
             indexDir.mkdirs();
         }
         return new File(indexDir, 
                 FileUtil.toSafeFileName(getId()) + ".index");
     }
     /*default*/ File getSuiteStopFile() {
         return new File(getWorkdir() + File.separator 
                 + "latest" + File.separator 
                 + FileUtil.toSafeFileName(getId()) + ".stop");
     }
     /*default*/ List<IJobLifeCycleListener> getJobLifeCycleListeners() {
         return jobLifeCycleListeners;
     }
     /*default*/ List<IJobErrorListener> getJobErrorListeners() {
         return jobErrorListeners;
     }
     /*default*/ List<ISuiteLifeCycleListener> getSuiteLifeCycleListeners() {
         return suiteLifeCycleListeners;
     }
 
     private boolean doExecute(boolean resumeIfIncomplete) throws IOException {
         boolean success = false;
 
         LOG.info("Initialization...");
 
         //--- Initialize ---
         initialize(resumeIfIncomplete);
 
         //--- Add Log Appender ---
         Appender appender = getLogManager().createAppender(getId());
         Logger.getRootLogger().addAppender(appender);        
 
         heartbeatGenerator.start();
         
         //TODO add listeners, etc
 
         StopRequestMonitor stopMonitor = new StopRequestMonitor(this);
         stopMonitor.start();
 
         LOG.info("Starting execution.");
         fire(suiteLifeCycleListeners, "suiteStarted", this);
         
         try {
             success = runJob(getRootJob());
         } finally {
             stopMonitor.stopMonitoring();
             if (success 
                     && jobSuiteStatusSnapshot.getRoot().getState() == JobState.COMPLETED) {
                 fire(suiteLifeCycleListeners, "suiteCompleted", this);
             }
             // Remove appender
             Logger.getRootLogger().removeAppender(appender);
             heartbeatGenerator.terminate();
         }
 
         return success;
     }
     
     //TODO make public, to allow to start a specific job??
     //TODO document this is not a public method?
     public boolean runJob(final IJob job) {
         boolean success = false;
         setCurrentJobId(job.getId());
         
         MutableJobStatus status = 
                 (MutableJobStatus) jobSuiteStatusSnapshot.getJobStatus(job);
         if (status.getState() == JobState.COMPLETED) {
             LOG.info("Job skipped: " + job.getId() + " (already completed)");
             fire(jobLifeCycleListeners, "jobSkipped", status);
             return true;
         }
 
         boolean errorHandled = false;
         try {
             if (status.getResumeAttempts() == 0) {
                 status.getDuration().setStartTime(new Date());
                 LOG.info("Running " + job.getId() + ": BEGIN (" 
                         + status.getDuration().getStartTime() + ")");  
                 fire(jobLifeCycleListeners, "jobStarted", status);
             } else {
                 LOG.info("Running " + job.getId()  
                         + ": RESUME (" + new Date() + ")");  
                 fire(jobLifeCycleListeners, "jobResumed", status);
                 status.getDuration().setEndTime(null);
                 status.setNote("");  
             }
 
             heartbeatGenerator.register(status);
             //--- Execute ---
             job.execute(new JobStatusUpdater(status) {
                 protected void statusUpdated(MutableJobStatus status) {
                     try {
                         getJobStatusStore().write(getId(), status);
                     } catch (IOException e) {
                         throw new JEFException(
                                 "Cannot persist status update for job: "
                                         + status.getJobId(), e);
                     }
                     fire(jobLifeCycleListeners, "jobProgressed", status);
                     IJobStatus parentStatus = jobSuiteStatusSnapshot.getParent(status);
                     if (parentStatus != null) {
                         IJobGroup jobGroup = 
                                 (IJobGroup) jobs.get(parentStatus.getJobId());
                         if (jobGroup != null) {
                             jobGroup.groupProgressed(status);
                         }
                     }
                 };
 
             }, this);
             success = true;
         } catch (Exception e) {
             success = false;
             LOG.error("Execution failed for job: " + job.getId(), e);
             fire(jobErrorListeners, "jobError", 
                     new JobErrorEvent(e, this, status));
             if (status != null) {
                 status.setNote("Error occured: " + e.getLocalizedMessage());
             }
             errorHandled = true;
             //System.exit(-1)
         } finally {
             heartbeatGenerator.unregister(status);
             status.getDuration().setEndTime(new Date());
             try {
                 getJobStatusStore().write(getId(), status);
             } catch (IOException e) {
                 LOG.error("Cannot save final status.", e);
             }
             if (!success && !errorHandled) {
                 LOG.fatal("Fatal error occured in job: " + job.getId());
             }
             LOG.info("Running " + job.getId()  
                     + ": END (" + status.getDuration().getStartTime() + ")");  
             if (success) {
                 fire(jobLifeCycleListeners, "jobCompleted", status);
             } else {
                 fire(jobLifeCycleListeners, 
                         "jobTerminatedPrematuraly", status);
             }
         }
         return success;
     }
     
     public void stop() throws IOException {
         getSuiteStopFile().createNewFile();
     }
     
     public static void stop(File indexFile) throws IOException {
         if (indexFile == null || !indexFile.exists() || !indexFile.isFile()) {
             throw new JEFException("Invalid index file: " + indexFile);
         }
         String stopPath = 
                 StringUtils.removeEnd(indexFile.getAbsolutePath(), "index");
         stopPath += ".stop";
         new File(stopPath).createNewFile();
     }
     
     private void accept(
             IJobVisitor visitor, IJob job, Class<IJob> jobClassFilter) {
         if (job == null) {
             return;
         }
         if (jobClassFilter == null || jobClassFilter.isInstance(job)) {
             IJobStatus status = null;
             if (jobSuiteStatusSnapshot != null) {
                 status = jobSuiteStatusSnapshot.getJobStatus(job);
             }
             visitor.visitJob(job, status);
         }
         if (job instanceof IJobGroup) {
             for (IJob childJob : ((IJobGroup) job).getJobs()) {
                 accept(visitor, childJob, jobClassFilter);
             }
         }
     }
     
     private void initialize(boolean resumeIfIncomplete)
             throws IOException {
         JobSuiteStatusSnapshot statusTree = 
                 JobSuiteStatusSnapshot.newSnapshot(getSuiteIndexFile());
         
         if (statusTree != null) {
             LOG.info("Previous execution detected.");
             IJobStatus status = statusTree.getRoot();
             JobState state = status.getState();
             ensureValidExecutionState(state);
             if (resumeIfIncomplete && !state.isOneOf(
                     JobState.COMPLETED, JobState.PREMATURE_TERMINATION)) {
                 LOG.info("Resuming from previous execution.");
                 //TODO increase resume attempts on each incomplete jobs
             } else {
                 // Back-up so we can start clean
                 LOG.info("Backing up previous execution status and log files.");
                 backupSuite(statusTree);
                 statusTree = null;
             }
         } else {
             LOG.info("No previous execution detected.");
             statusTree = JobSuiteStatusSnapshot.create(
                     getRootJob(), getLogManager());
             writeJobSuiteIndex(statusTree);
         }
         this.jobSuiteStatusSnapshot = statusTree;
     }
     
     
     private void ensureValidExecutionState(JobState state) {
         if (state == JobState.RUNNING) {
             throw new JEFException("JOB SUITE ALREADY RUNNING. There is "
                     + "already an instance of this job suite running. "
                     + "Either stop it, or wait for it to complete.");
         }
         if (state == JobState.STOPPING) {
             throw new JEFException("JOB SUITE STOPPING. "
                     + "There is an instance of this job suite currently "
                     + "stopping.  Wait for it to stop, or terminate the "
                     + "process.");
         }
     }
     
     private void backupSuite(JobSuiteStatusSnapshot statusTree) throws IOException {
         IJobStatus suiteStatus = statusTree.getRoot();
         Date backupDate = suiteStatus.getDuration().getEndTime();
         if (backupDate == null) {
             backupDate = suiteStatus.getLastActivity();
         }
         if (backupDate == null) {
             backupDate = new Date();
         }
         // Backup status files
         List<IJobStatus> statuses = statusTree.getJobStatusList();
         for (IJobStatus jobStatus : statuses) {
             getJobStatusStore().backup(
                     getId(), jobStatus.getJobId(), backupDate);
         }
         // Backup log
         getLogManager().backup(getId(), backupDate);
 
         // Backup suite index
         String date = new SimpleDateFormat(
                 "yyyyMMddHHmmssSSSS").format(backupDate);
         File indexFile = getSuiteIndexFile();
         File backupDir =  new File(getWorkdir() + File.separator + "backup");
         try {
             backupDir = FileUtil.createDateDirs(backupDir, backupDate);
         } catch (IOException e) {
             throw new JobException("Could not create backup directory for "
                     + "suite index.");
         }
         File backupFile = new File(backupDir.getAbsolutePath() + "/" 
                 + date + "__"  + FileUtil.toSafeFileName(getId()) + ".index");
         indexFile.renameTo(backupFile);
     }
     
     private void writeJobSuiteIndex(JobSuiteStatusSnapshot statusTree) 
             throws IOException {
         Writer out = new FileWriter(getSuiteIndexFile());
         out.write("<?xml version=\"1.0\" ?><suite-index>");
         
         //--- Log Manager ---
         out.flush();
         getLogManager().saveToXML(out);
 
         //--- JobStatusSerializer ---
         out.flush();
         getJobStatusStore().saveToXML(out);
         
         //--- Job Status ---
         writeJobId(out, statusTree, statusTree.getRoot());
         
         out.write("</suite-index>");
 
         out.flush();
         out.close();
     }
     
     private void writeJobId(Writer out, 
             JobSuiteStatusSnapshot statusTree, IJobStatus status) throws IOException {
         out.write("<job name=\"");
         out.write(StringEscapeUtils.escapeXml(status.getJobId()));
         out.write("\">");
         for (IJobStatus child : statusTree.getChildren(status)) {
             writeJobId(out, statusTree, child);
         }
         out.write("</job>");
     }
     
     private String resolveWorkdir(String configWorkdir) {
         File dir = null;
         if (StringUtils.isBlank(configWorkdir)) {
             dir = JEFUtil.FALLBACK_WORKDIR;
         } else {
             dir = new File(configWorkdir);
             if (dir.exists() && !dir.isDirectory()) {
                 dir = JEFUtil.FALLBACK_WORKDIR;
             }
         }
         if (!dir.exists()) {
             dir.mkdirs();
         }
         LOG.info("JEF work directory is: " + dir);
         return dir.getAbsolutePath();
     }
     private ILogManager resolveLogManager(ILogManager configLogManager) {
         ILogManager logManager = configLogManager;
         if (logManager == null) {
             logManager = new FileLogManager(workdir);
         }
         LOG.info("JEF log manager is : " 
                 + logManager.getClass().getSimpleName());
         return logManager;
     }
     private IJobStatusStore resolveJobStatusStore(
             IJobStatusStore configSerializer) {
         IJobStatusStore serial = configSerializer;
         if (serial == null) {
             serial = new FileJobStatusStore(workdir);
         }
         LOG.info("JEF job status store is : " 
                 + serial.getClass().getSimpleName());
         return serial;
     }
     
     private void fire(List<?> listeners, String methodName, Object argument) {
         for (Object l : listeners) {
             try {
                 MethodUtils.invokeExactMethod(l, methodName, argument);
             } catch (NoSuchMethodException | IllegalAccessException
                     | InvocationTargetException e) {
                 throw new JobException(
                         "Could not fire event \"" + methodName + "\".", e);
             }
         }
     }
 
 }
