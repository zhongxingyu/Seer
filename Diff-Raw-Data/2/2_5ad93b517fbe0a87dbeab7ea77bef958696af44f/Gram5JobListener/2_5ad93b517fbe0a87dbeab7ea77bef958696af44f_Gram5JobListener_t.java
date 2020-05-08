 package grisu.backend.model.job.gt5;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.globus.gram.GramJob;
 import org.globus.gram.GramJobListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Gram5JobListener implements GramJobListener {
 
 	static final Logger myLogger = LoggerFactory.getLogger(GT5Submitter.class
 			.getName());
 
 	private static Gram5JobListener l = new Gram5JobListener();
 
 	public static Gram5JobListener getJobListener() {
 		return l;
 	}
 
 	private final Map<String, Integer> statuses;
 
 	private final Map<String, Integer> errors;
 
 	private Gram5JobListener() {
 		statuses = Collections.synchronizedMap(new HashMap<String, Integer>());
 		errors = Collections.synchronizedMap(new HashMap<String, Integer>());
 	}
 
 	public Integer getError(String handle) {
 		return errors.get(handle);
 	}
 
 	public Integer getStatus(String handle) {
 		return statuses.get(handle);
 	}
 
 	public void statusChanged(GramJob job) {
                 int jobStatus = job.getStatus();
                 String jobId = job.getIDAsString();
                 myLogger.debug("job status changed to " + jobStatus);
                 statuses.put(jobId, jobStatus);
                 errors.put(jobId, job.getError());
                 try {
                     if ((jobStatus == GramJob.STATUS_DONE) || (jobStatus == GramJob.STATUS_FAILED)){
                         job.signal(GramJob.SIGNAL_COMMIT_END);
                     }
                 } catch (Exception e) {
                     String state = job.getStatusAsString();
                    myLogger.warn("Failed to send COMMIT_END to job " + jobId + " in state " + state, e);
                 }
 	}
 
 }
