 package com.atlassian.sal.api.scheduling;
 
 import java.util.Date;
 import java.util.Map;
 
 /**
  * Interface for scheduling jobs
  *
  * @since 2.0
  */
 public interface PluginScheduler
 {
     /**
      * Schedule the given job.
      *
      * <p> <strong>WARNING</strong>: it is very important not to try to call this method until the underlying application
      * is fully started. You should implement {@link com.atlassian.sal.api.lifecycle.LifecycleAware} and call scheduleJob()
      * only on {@link com.atlassian.sal.api.lifecycle.LifecycleAware#onStart()}
      *
     * @param jobKey         A unique key of the job
     * @param jobClass       The class for the job
      * @param jobDataMap     Any data that needs to be passed to the job.  This map instance will always be the same
      *                       instance that is given to the job when it executes.
      * @param startTime      The time the job is to start.
      * @param repeatInterval How long the interval between repeats, in milliseconds.  Note, some implementations
      */
    void scheduleJob(String jobKey,
                     Class<? extends PluginJob> jobClass, Map<String, Object> jobDataMap,
                     Date startTime, long repeatInterval);
 
     /**
     * Unschedule the given job. If the job doesn't exist then IllegalArgumentException will be thrown.
      *
     * @param jobKey The job key to unschedule
     * @throws  IllegalArgumentException If the job doesn't exist thus cannot be unscheduled.
      */
    void unscheduleJob(String jobKey);
}
