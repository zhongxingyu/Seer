 /* Copyright 2010-2013 Norconex Inc.
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
 package com.norconex.jef;
 
 import java.io.IOException;
 import java.util.Date;
 
 import org.apache.log4j.Appender;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 
 import com.norconex.commons.lang.Sleeper;
 import com.norconex.jef.error.IErrorEvent;
 import com.norconex.jef.error.IErrorHandler;
 import com.norconex.jef.progress.IJobProgressListener;
 import com.norconex.jef.progress.IJobStatus;
 import com.norconex.jef.progress.JobElapsedTime;
 import com.norconex.jef.progress.JobProgress;
 import com.norconex.jef.suite.IJobSuiteStopRequestHandler;
 import com.norconex.jef.suite.ISuiteLifeCycleListener;
 import com.norconex.jef.suite.ISuiteStopRequestListener;
 import com.norconex.jef.suite.JobSuite;
 
 /**
  * Responsible for managing the execution of a suite and its related jobs.
  * @author <a href="mailto:pascal.essiembre@norconex.com">Pascal Essiembre</a>
  */
 @SuppressWarnings("nls")
 public class JobRunner {
 
     /** Logger. */
     private static final Logger LOG = LogManager.getLogger(JobRunner.class);
     /** Associates job id with current thread. */
     private static final ThreadLocal<String> CURRENT_JOB_ID = 
             new ThreadLocal<String>();
 
     /**
      * Runs a job suite.  If the execution of a suite has been stopped,
      * in mid-process an exception will be thrown.  Use the
      * <code>runSuite(JobSuite, boolean</code> variant for resuming jobs.
      * @param suite the job suite to run
      * @return <code>true</code> if the suite ran successfully
      */
     public final boolean runSuite(final JobSuite suite) {
         return runSuite(suite, false);
     }
     /**
      * Runs a job suite.
      * @param suite the job suite to run
      * @param resumeIfIncomplete if <code>true</code>, 
      *        stopped and aborted jobs jobs will be resumed
      * @return <code>true</code> if the suite ran successfully
      */
     public final boolean runSuite(
             final JobSuite suite, final boolean resumeIfIncomplete) {
         boolean success = false;
         try {
             ensureCleanState(suite, resumeIfIncomplete);
 
             // Add appender
             Appender appender = suite.getLogManager().createAppender(
                     suite.getNamespace());
             Logger.getRootLogger().addAppender(appender);
 
             // Execute suite
             fireSuiteStarted(suite);
             IJobSuiteStopRequestHandler detector = 
                         suite.getStopRequestHandler();
             detector.startListening(new ISuiteStopRequestListener() {
                 private static final long serialVersionUID = 
                         9037290977306436552L;
                 @Override
                 public void stopRequestReceived() {
                     suite.stop();
                     fireStopRequested(suite);
                 }
             });
             
             success = runJob(suite.getRootJob(), suite);
 
             detector.stopListening();
             
             
             if (success && recoverProgress(suite, suite.getRootJob())
                     .getStatus() == IJobStatus.Status.COMPLETED) {
                 fireSuiteCompleted(suite);
             }
             // Remove appender
             Logger.getRootLogger().removeAppender(appender);
         } catch (Throwable e) {
             LOG.fatal("Job suite execution failed: " + suite.getNamespace(), e);
             handleError(e, null, suite);
             success = false;
         } finally {
             // Suite finished
             fireSuiteFinished(suite);
         }
 
         if (!success) {
             fireSuiteAborted(suite);
         }
         return success;
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
     /**
      * Gets the job identifier representing the currently running job for the
      * current thread.
      * @return job identifier or <code>null</code> if no job is currently
      *         associated with the current thread
      */
     public static String getCurrentJobId() {
         return (String) CURRENT_JOB_ID.get();
     }
     
     /**
      * Runs a job.
      * @param job the job to run
      * @param suite the job suite
      * @return <code>true</code> if the job ran successfully
      */
     /*default*/ 
     final boolean runJob(final IJob job, final JobSuite suite) {
         boolean success = false;
         setCurrentJobId(job.getId());
         final JobElapsedTime elapsedTime = new JobElapsedTime();
 
         final JobProgress progress = createProgress(job, suite, elapsedTime);
         suite.addSuiteStopRequestListener(progress);
         
         // If progress is completed, do not proceed.
         if (IJobStatus.Status.COMPLETED == progress.getStatus()) {
             LOG.info("Job skipped: " + job.getId() + " (already completed)");
             fireJobSkipped(progress, suite.getJobProgressListeners());
             return true;
         }
 
         // Proceed
         IJobProgressListener[] progressListeners =
                 suite.getJobProgressListeners();
         // Register our first activity so status can be RUNNING:
         elapsedTime.setLastActivity(new Date());
         
         
         // Add progress tracking for job.
         for (int i = 0; i < progressListeners.length; i++) {
             progress.addJobProgressListener(progressListeners[i]);
         }
 
         boolean errorHandled = false;
         try {
             if (!progress.isRecovery()) {
                 elapsedTime.setStartTime(new Date());
                 LOG.info("Running " + job.getId()  
                         + ": BEGIN (" + elapsedTime.getStartTime() + ")");  
                 fireJobStarted(progress, suite.getJobProgressListeners());
             } else {
                 LOG.info("Running " + job.getId()  
                         + ": RESUME (" + new Date() + ")");  
                 fireJobResumed(progress, suite.getJobProgressListeners());
                 elapsedTime.setEndTime(null);
                 progress.setNote("");  
             }
             Thread activityNotifier = createActivityNotifier(
                     job, suite, elapsedTime, progress);
             activityNotifier.start();
             job.execute(progress, suite);
             success = true;
         } catch (Exception e) {
             success = false;
             LOG.error("Execution failed for job: " + job.getId(), e);
             handleError(e, progress, suite);
             errorHandled = true;
             //System.exit(-1)
         } finally {
             elapsedTime.setEndTime(new Date());
            if (!success && !errorHandled) {
                 LOG.fatal("Fatal error occured in job: " + job.getId());
             }
             LOG.info("Running " + job.getId()  
                     + ": END (" + elapsedTime.getStartTime() + ")");  
             if (success) {
                 fireJobCompleted(progress, suite.getJobProgressListeners());
             } else {
                 fireJobTerminatedPrematuraly(
                         progress, suite.getJobProgressListeners());
             }
             // Remove progress tracking for job.
             for (int i = 0; i < progressListeners.length; i++) {
                 progress.removeJobProgressListener(progressListeners[i]);
             }
         }
         return success;
     }
 
     private JobProgress createProgress(final IJob job, final JobSuite suite,
             final JobElapsedTime elapsedTime) {
         JobProgress progress;
         JobProgress recoveredProgress = recoverProgress(suite, job);
         if (recoveredProgress != null
                 && recoveredProgress.getStartTime() != null) {
             progress = new JobProgress(job.getId(), suite.getJobContext(job),
                     recoveredProgress, elapsedTime);
         } else {
             progress = new JobProgress(
                     job.getId(), suite.getJobContext(job), elapsedTime);
         }
         return progress;
     }
     
     private Thread createActivityNotifier(
             final IJob job, final JobSuite suite,
             final JobElapsedTime elapsedTime, final JobProgress finalProgress) {
         return new Thread(
                 "activityTracker_" + job.getId()) {
             public void run() {
                 while (finalProgress.isStatus(
 //                        IJobStatus.Status.STARTED, 
                         IJobStatus.Status.RUNNING,
                         IJobStatus.Status.STOPPING)) {
                     Sleeper.sleepMillis(JobProgress.ACTIVITY_CHECK);
                     elapsedTime.setLastActivity(new Date());
                     if (LOG.isDebugEnabled()) {
                         LOG.debug(job.getId() + " last activity: " 
                                 + elapsedTime.getLastActivity());
                     }
                     fireJobRunningVerified(
                             finalProgress, suite.getJobProgressListeners());
                 }
             };
         };
     }
 
     /**
      * Ensures the suite is in a clean state before executing it.
      * @param suite suite to check state
      * @param resumeIfIncomplete are we resuming if stopped or aborted
      * @throws IOException problem reading progress
      */
     private void ensureCleanState(
             final JobSuite suite, final boolean resumeIfIncomplete)
             throws IOException {
         //If suite completed, throw exception, unless we backup first
         JobProgress existingProgress =
                 recoverProgress(suite, suite.getRootJob());
 
         // If job is already clean, do nothing
         if (existingProgress == null) {
             return; 
         }
         
         // Abort if running already
         if (existingProgress.isStatus(IJobStatus.Status.RUNNING)) {
             throw new JobException(
                     "Suite already running.");  
         }
 
 
         // Back-up if starting form scratch
         if (!resumeIfIncomplete || existingProgress.isStatus(
                 IJobStatus.Status.COMPLETED, 
                 IJobStatus.Status.PREMATURE_TERMINATION)) {
             Date backupDate = existingProgress.getEndTime();
             if (backupDate == null) {
                 backupDate = existingProgress.getLastActivity();
             }
             if (backupDate == null) {
             	backupDate = new Date();
             }
             String[] ids = suite.getJobIds();
             for (int i = 0; i < ids.length; i++) {
                 String jobId = ids[i];
                 suite.getJobProgressSerializer().backup(
                         suite.getNamespace(), jobId, backupDate);
             }
             // Backcup log
             suite.getLogManager().backup(suite.getNamespace(), backupDate);
         }
     }
 
     private void fireSuiteStarted(final JobSuite suite) {
         for (ISuiteLifeCycleListener l : suite.getSuiteLifeCycleListeners()) {
             l.suiteStarted(suite);
         }
     }
     private void fireSuiteAborted(final JobSuite suite) {
         for (ISuiteLifeCycleListener l : suite.getSuiteLifeCycleListeners()) {
             l.suiteAborted(suite);
         }
     }
     private void fireSuiteFinished(final JobSuite suite) {
         for (ISuiteLifeCycleListener l : suite.getSuiteLifeCycleListeners()) {
             l.suiteTerminatedPrematuraly(suite);
         }
     }
     private void fireSuiteCompleted(final JobSuite suite) {
         for (ISuiteLifeCycleListener l : suite.getSuiteLifeCycleListeners()) {
             l.suiteCompleted(suite);
         }
     }
     private void fireStopRequested(final JobSuite suite) {
         for (ISuiteStopRequestListener l : 
                 suite.getSuiteStopRequestListeners()) {
             l.stopRequestReceived();
         }
     }
     private void fireJobStarted(
             final JobProgress progress,
             final IJobProgressListener[] listeners) {
         for (int i = 0; i < listeners.length; i++) {
             listeners[i].jobStarted(progress);
         }
     }
     private void fireJobResumed(
             final JobProgress progress,
             final IJobProgressListener[] listeners) {
         for (int i = 0; i < listeners.length; i++) {
             listeners[i].jobResumed(progress);
         }
     }
     private void fireJobRunningVerified(
             final JobProgress progress,
             final IJobProgressListener[] listeners) {
         for (int i = 0; i < listeners.length; i++) {
             listeners[i].jobRunningVerified(progress);
         }
     }
     private void fireJobSkipped(
             final JobProgress progress,
             final IJobProgressListener[] listeners) {
         for (int i = 0; i < listeners.length; i++) {
             listeners[i].jobSkipped(progress);
         }
     }
     private void fireJobCompleted(
             final JobProgress progress,
             final IJobProgressListener[] listeners) {
         for (int i = 0; i < listeners.length; i++) {
             listeners[i].jobCompleted(progress);
         }
     }
     private void fireJobTerminatedPrematuraly(
             final JobProgress progress,
             final IJobProgressListener[] listeners) {
         for (int i = 0; i < listeners.length; i++) {
             listeners[i].jobTerminatedPrematuraly(progress);
         }
     }
     private void handleError(
             final Throwable t,
             final JobProgress progress,
             final JobSuite suite) {
         IErrorEvent event = new IErrorEvent() {
             public Throwable getException() {
                 return t;
             }
             public JobSuite getJobSuite() {
                 return suite;
             }
             public JobProgress getProgress() {
                 return progress;
             }
         };
         IErrorHandler[] handlers = suite.getErrorHandlers();
         for (int i = 0; i < handlers.length; i++) {
             handlers[i].handleError(event);
         }
         if (progress != null) {
             progress.setNote("Error occured: "  
                     + t.getLocalizedMessage());
         }
     }
 
     /**
      * Recovers a job progress.
      * @param suite job suite
      * @param job job for which to recover progress
      * @return recovered progress
      */
     private JobProgress recoverProgress(final JobSuite suite, final IJob job) {
         JobProgress recoveredProgress;
         try {
             recoveredProgress = suite.getJobProgressSerializer().deserialize(
                     suite.getNamespace(), 
                     job.getId(), 
                     suite.getJobContext(job));
         } catch (IOException e) {
             throw new JobException(
                     "Cannot deserialize progress for job: "  
                             + job.getId(), e);
         }
         return recoveredProgress;
     }
 }
