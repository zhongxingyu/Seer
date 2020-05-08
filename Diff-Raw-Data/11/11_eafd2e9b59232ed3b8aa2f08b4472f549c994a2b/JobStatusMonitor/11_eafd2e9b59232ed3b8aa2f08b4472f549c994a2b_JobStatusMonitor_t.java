 package org.imirsel.nema.flowservice.monitor;
 
 import org.imirsel.nema.model.Job;
 
 
 /**
  * Interface for monitoring running jobs.
  *
  * @author shirk
  * @since 0.4.0
  */
 public interface JobStatusMonitor {
 
    //~ Methods -----------------------------------------------------------------
 
    /**
     * Start to monitor the specified job and notify the specified handler when
     * an update occurs. 
     *
     * @param job The {@link Job} to monitor.
    * @param updateListener The {@link JobStatusUpdateListener} to notify when an
     * update occurs.
     */
   public void start(Job job, JobStatusUpdateListener updateListener);
 
    /**
     * Explicitly stop monitoring the specified job. The status monitor will
     * automatically stop monitoring a job once it has ended. Therefore, you do
     * not need to call this method unless you want to stop receiving status
     * updates before the job has ended.
     *
     * @param job The {@link Job} to stop monitoring.
    * @param updateListener The {@link JobStatusUpdateListener} that was specified
     * when monitoring was started.
     */
   public void stop(Job job, JobStatusUpdateListener updateListener);
 }
