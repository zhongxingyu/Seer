 package workloadgen.loadjobs;
 
 import java.io.IOException;
 
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.JobID;
 import org.apache.hadoop.mapred.RunningJob;
 import org.apache.hadoop.util.StringUtils;
 
 import workloadgen.utils.LoadJobQueue;
 
 public class LoadJob{
 	
 	enum JobState{
 		SUCCESS,
 		WAITING,
 		RUNNING,
 		READY,
 		FAILED,
 		DEPENDENT_FAILED
 	}
 	
 	private JobConf theJobConf;
 	private String jobID; // assigned and used by JobControl class
 	private JobID mapredJobID; // the job ID assigned by map/reduce
 	private String jobName; // external name, assigned/used by client app
 	private String message; // some info for human consumption,
 	// e.g. the reason why the job failed
 	private LoadJobQueue<LoadJob> dependingJobs; // the jobs the current job depends on
 	JobClient jc = null; // the map reduce job client
 
 	private JobState state;
 	int submitTime = 0;
 	
 	public LoadJob(JobConf jobConf, LoadJobQueue<LoadJob> dependingJobs, int submitPoint) throws IOException {
 		this.theJobConf = jobConf;
 	    this.dependingJobs = dependingJobs;
 	    this.state = JobState.WAITING;
 	    this.jobID = "unassigned";
 	    this.mapredJobID = null; //not yet assigned 
 	    this.jobName = "unassigned";
 	    this.message = "just initialized";
 	    this.jc = new JobClient(jobConf);
 		this.submitTime = submitPoint;
 		this.setJobName(jobConf.getJobName());
 	}
 	
 
 	/**
 	 * Construct a job.
 	 * 
 	 * @param jobConf
 	 *            mapred job configuration representing a job to be executed.
 	 * @throws IOException
 	 */
 	public LoadJob(JobConf jobConf, int submitTime) throws IOException {
 		this(jobConf, null, submitTime);
 	}
 	
 	/**
 	 * Check the state of this running job. The state may remain the same,
 	 * become SUCCESS or FAILED.
 	 */
 	private void checkRunningState() {
 		RunningJob running = null;
 		try {
 			running = jc.getJob(this.mapredJobID);
 			if (running.isComplete()) {
 				if (running.isSuccessful()) {
 					this.state = JobState.SUCCESS;
 				} else {
 					this.state = JobState.FAILED;
 					this.message = "Job failed! Error - "
 							+ running.getFailureInfo();
 					try {
 						running.killJob();
 					} catch (IOException e1) {
 
 					}
 					try {
 						this.jc.close();
 					} catch (IOException e2) {
 
 					}
 				}
 			}
 
 		} catch (IOException ioe) {
 			this.state = JobState.FAILED;
 			this.message = StringUtils.stringifyException(ioe);
 			System.out.println(this.message);
 			try {
 				if (running != null)
 					running.killJob();
 			} catch (IOException e1) {
 
 			}
 			try {
 				this.jc.close();
 			} catch (IOException e1) {
 
 			}
 		}
 	}
 
 	/**
 	 * @return the message of this job
 	 */
 	public String getMessage() {
 		return this.message;
 	}
 
 	/**
 	 * Set the message for this job.
 	 * 
 	 * @param message
 	 *            the message for this job.
 	 */
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 	/**
 	 * Check and update the state of this job. The state changes depending on
 	 * its current state and the states of the depending jobs.
 	 */
 	synchronized JobState checkState(int curTime) {
 		if (this.state == JobState.RUNNING) {
 			checkRunningState();
 		}
 		if (this.state != JobState.WAITING) {
 			return this.state;
 		}
 		if (this.dependingJobs == null || this.dependingJobs.size() == 0){
 			if (checkSubmitState(curTime)){
 				this.state = JobState.READY;
 			}
 			return this.state;
 		}
 		//here below are the job chains
 		for (LoadJob pred : dependingJobs) {
 			JobState s = pred.checkState(curTime);
 			if (s == JobState.WAITING || s == JobState.READY || s == JobState.RUNNING) {
 				break; // a pred is still not completed, continue in WAITING
 				// state
 			}
 			if (s == JobState.FAILED || s == JobState.DEPENDENT_FAILED) {
 				this.state = JobState.DEPENDENT_FAILED;
 				this.message = "depending job " + " with jobID "
 						+ pred.getJobID() + " failed. " + pred.getMessage();
 				break;
 			}
 			// pred must be in success state
 			if (pred == dependingJobs.tail()) {
 				this.state = JobState.READY;
 			}
 		}
 		return this.state;
 	}
 	
 	private boolean checkSubmitState(int curTime){
		//System.out.println("time:" + curTime + " submitTime:" + submitTime);
 		return (curTime >= submitTime);
 	}
 	
 	public void setState(JobState s){
 		this.state = s;
 	}
 	
 	synchronized public JobState getState(){
 		return this.state;
 	}
 
 	/**
 	 * @return the job name of this job
 	 */
 	public String getJobName() {
 		return this.jobName;
 	}
 
 	/**
 	 * Set the job name for this job.
 	 * 
 	 * @param jobName
 	 *            the job name
 	 */
 	public void setJobName(String jobName) {
 		this.jobName = jobName;
 	}
 
 	/**
 	 * @return the job ID of this job assigned by JobControl
 	 */
 	public String getJobID() {
 		return this.jobID;
 	}
 
 	/**
 	 * Set the job ID for this job.
 	 * 
 	 * @param id	the job ID
 	 */
 	public void setJobID(String id) {
 		this.jobID = id;
 	}
 	
 	public JobConf getJobConf(){
 		return this.theJobConf;
 	}
 	
 	/**
 	 * upgrade the visibility of parent's function
 	 */
 	synchronized void submit(){
 		try {
 			FileSystem fs = FileSystem.get(theJobConf);
 			if (theJobConf.getBoolean("create.empty.dir.if.nonexist", false)) {
 				//System.out.println("FS default path:" + FileSystem.getDefaultUri(theJobConf));
 				Path inputPaths[] = FileInputFormat.getInputPaths(theJobConf);
 				for (int i = 0; i < inputPaths.length; i++) {
 					if (!fs.exists(inputPaths[i])) {
 						try {
 							fs.mkdirs(inputPaths[i]);
 						} catch (IOException e) {
 
 						}
 					}
 				}
 			}
 			RunningJob running = jc.submitJob(theJobConf);
 			this.mapredJobID = running.getID();
 			this.state = JobState.RUNNING;
 			System.out.println("run job:" + this.mapredJobID);
 		} catch (Exception ioe) {
 			this.state = JobState.FAILED;
 			this.message = StringUtils.stringifyException(ioe);
 			System.out.println(this.jobID + ":" + this.message);
 		}
 	}
 	
 	@Override
 	public String toString() {
 		StringBuffer ret = new StringBuffer();
 		StringBuffer sb = new StringBuffer();
 		sb.append("job name:\t").append(this.jobName).append("\n");
 		sb.append("job id:\t").append(this.jobID).append("\n");
 		sb.append("job state:\t").append(this.state).append("\n");
 		sb.append("job mapred id:\t").append(this.mapredJobID == null ? "unassigned"
 						: this.mapredJobID).append("\n");
 		sb.append("job message:\t").append(this.message).append("\n");
 
 		if (this.dependingJobs == null || this.dependingJobs.size() == 0) {
 			sb.append("job has no depending job:\t").append("\n");
 		} else {
 			sb.append("job has ").append(this.dependingJobs.size()).append(" dependeng jobs:\n");
 			for (LoadJob dependingjob : dependingJobs){
 				sb.append("\t depending job ").append(dependingjob.getJobID()).append(":\t");
 			}
 		}
 		ret.append("job Mapper:" + this.theJobConf.getMapperClass().toString()).append("\n");
 		ret.append("job Reducer:" + this.theJobConf.getReducerClass().toString()).append("\n");
 		return ret.toString();
 	}
 }
