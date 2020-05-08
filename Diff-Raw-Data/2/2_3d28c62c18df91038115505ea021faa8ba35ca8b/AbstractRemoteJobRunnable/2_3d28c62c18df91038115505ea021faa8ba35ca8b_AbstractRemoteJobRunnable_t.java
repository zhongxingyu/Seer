 package de.otto.jobstore.common;
 
 import de.otto.jobstore.common.properties.JobInfoProperty;
 import de.otto.jobstore.service.RemoteJobExecutorService;
 import de.otto.jobstore.service.exception.JobException;
 import de.otto.jobstore.service.exception.RemoteJobAlreadyRunningException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.net.URI;
 import java.util.List;
 
 public abstract class AbstractRemoteJobRunnable implements JobRunnable {
 
     private final RemoteJobExecutorService remoteJobExecutorService;
    protected Logger log = LoggerFactory.getLogger(this.getClass());
 
     protected String id;
 
     protected AbstractRemoteJobRunnable(RemoteJobExecutorService remoteJobExecutorService) {
         this.remoteJobExecutorService = remoteJobExecutorService;
     }
 
     @Override
     public String getId() {
         return id;
     }
 
     @Override
     public void setId(String id) {
         this.id = id;
     }
 
     @Override
     public final boolean isRemote() {
         return true;
     }
 
     protected abstract List<Parameter> getParameters();
 
     @Override
     public void execute(JobLogger jobLogger) throws JobException {
         try {
             log.info("Trigger remote job '{}' [{}] ...", getName(), getId());
             final URI uri = remoteJobExecutorService.startJob(new RemoteJob(getName(), getId(), getParameters()));
             jobLogger.insertOrUpdateAdditionalData(JobInfoProperty.REMOTE_JOB_URI.val(), uri.toString());
         } catch (RemoteJobAlreadyRunningException e) {
             log.info("Remote job '{}' [{}] is already running: " + e.getMessage(), getName(), getId());
             jobLogger.insertOrUpdateAdditionalData("resumedAlreadyRunningJob", e.getJobUri().toString());
             jobLogger.insertOrUpdateAdditionalData(JobInfoProperty.REMOTE_JOB_URI.val(), e.getJobUri().toString());
         }
     }
 
     @Override
     public void executeOnSuccess() throws JobException {}
 
 }
