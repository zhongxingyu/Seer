 package workloadgen.loadjobs;
 
 import java.io.IOException;
 
 
 import workloadgen.loadjobs.LoadJob.JobState;
 import workloadgen.utils.LoadJobQueue;
 
 public class LoadJobController implements Runnable{
 
 	//I have to set these variables, since in JobControl, the runnerState is private
 	// The thread can be in one of the following state
 	
 	private enum JobControllerState{
 		RUNNING,
 		SUSPENDED,
 		STOPPED,
 		STOPPING,
 		READY
 	}
 	
 	private JobControllerState controllerState;
 	private int suspendDuration = 0;
 	private int currentTime = 0;
 	private int totalJobNum = 0;
 	
 	private LoadJobQueue<LoadJob> waitingQueue;
 	private LoadJobQueue<LoadJob> readyQueue;
 	private LoadJobQueue<LoadJob> runningQueue;
 	private LoadJobQueue<LoadJob> successfulQueue;
 	private LoadJobQueue<LoadJob> failedQueue;
 	
 	private long nextJobID = -1;
 	private String groupName;
 	
 	//cluster status
 	private int totalMapSlots = -1;
 	private int totalReduceSlots = -1;
 	private int runningMapNum = -1;
 	private int runningReduceNum = -1;
 	private int idleMapSlots = -1;
 	private int idleReduceSlots = -1;
 	
 	public LoadJobController(String gName) {
 		this.waitingQueue = new LoadJobQueue<LoadJob>();
 	    this.readyQueue = new LoadJobQueue<LoadJob>();
 	    this.runningQueue = new LoadJobQueue<LoadJob>();
 	    this.successfulQueue = new LoadJobQueue<LoadJob>();
 	    this.failedQueue = new LoadJobQueue<LoadJob>();
 	    controllerState = JobControllerState.READY;
 		currentTime = 0;
 		groupName = gName;
 	}
 	
 	public String getClusterStatus() throws IOException{
 		updateClusterStatus();
 		StringBuffer sb = new StringBuffer();
 		sb.append("totalMapSlots:" + this.totalMapSlots).append("\n");
 		sb.append("totalReduceSlots:" + this.totalReduceSlots).append("\n");
 		sb.append("RunningMapTasks:" + this.runningMapNum).append("\n");
 		sb.append("RunningReduceTasks:" + this.runningReduceNum).append("\n");
 		sb.append("IdleMapSlots:" + this.idleMapSlots).append("\n");
 		sb.append("IdleReduceSlots:" + this.idleReduceSlots).append("\n");
 		return sb.toString();
 	}
 	
 	private void updateClusterStatus() throws IOException{
 		if (this.runningQueue.size() > 0){
 			LoadJob firstJob = this.runningQueue.element();
 			this.totalMapSlots = firstJob.jc.getClusterStatus().getMaxMapTasks();
 			this.totalReduceSlots = firstJob.jc.getClusterStatus().getMaxReduceTasks();
 			this.runningMapNum = firstJob.jc.getClusterStatus().getMapTasks();
 			this.runningReduceNum = firstJob.jc.getClusterStatus().getReduceTasks();
 			this.idleMapSlots = this.totalMapSlots - this.runningMapNum;
 			this.idleReduceSlots = this.totalReduceSlots - this.runningReduceNum;
 		}
 		else{
 			this.runningMapNum = 0;
 			this.runningReduceNum = 0;
 			this.idleMapSlots = this.totalMapSlots - this.runningMapNum;
 			this.idleReduceSlots = this.totalReduceSlots - this.runningReduceNum;
 		}
 	}
 	
 	private String getNextJobID() {
 		nextJobID += 1;
 		return this.groupName + this.nextJobID;
 	}
 	
 	public int getTotalJobNum(){
 		return this.totalJobNum;
 	}
 	
 	public int getWaitingNum(){
 		return this.waitingQueue.size();
 	}
 	
 	public int getReadyNum(){
 		return this.readyQueue.size();
 	}
 	
 	public int getRunningNum(){
 		return this.runningQueue.size();
 	}
 	
 	public int getSuccessNum(){
 		return this.successfulQueue.size();
 	}
 	
 	public int getFailedNum(){
 		return this.failedQueue.size();
 	}
 	
 	synchronized public String addJob(LoadJob job){
 		String id = this.getNextJobID();
 	    job.setJobID(id);
 	    job.setState(LoadJob.JobState.WAITING);
 	    this.addToQueue(job);
 	    this.totalJobNum++;
 		return job.getJobID();
 	}
 	
 	public void suspend(int duration){
 		if (this.controllerState == JobControllerState.RUNNING) {
 			this.controllerState = JobControllerState.SUSPENDED;
 		}
 		this.suspendDuration = duration;
 	}
 	
 	public void stop(){
 		System.out.println("stopping jobcontroller");
 		this.controllerState = JobControllerState.STOPPING;
 	}
 	
 	@Override
 	public String toString(){
 		StringBuffer sb = new StringBuffer();
 		for (LoadJob job: waitingQueue){
 			sb.append(job).append("\n");
 		}
 		return sb.toString();
 	}
 	
 	private static void addToQueue(LoadJob job, LoadJobQueue<LoadJob> queue) {
 		synchronized (queue) {
 			queue.add(job);
 		}
 	}
 	
 	private void addToQueue(LoadJob job) {
 		addToQueue(job, getQueue(job.getState()));
 	}
 	
 	private LoadJobQueue<LoadJob> getQueue(JobState state) {
 		LoadJobQueue<LoadJob> retv = null;
 		if (state == JobState.WAITING) {
 			retv = this.waitingQueue;
 		} else if (state == JobState.READY) {
 			retv = this.readyQueue;
 		} else if (state == JobState.RUNNING) {
 			retv = this.runningQueue;
 		} else if (state == JobState.SUCCESS) {
 			retv = this.successfulQueue;
 		} else if (state == JobState.FAILED || state == JobState.DEPENDENT_FAILED) {
 			retv = this.failedQueue;
 		}
 		return retv;
 	}
 	
 	synchronized private boolean completed() {
 		return (this.waitingQueue.size() == 0 && this.readyQueue.size() == 0
 				&& this.runningQueue.size() == 0);
 	}
 	
 	synchronized public boolean stopped(){
 		return this.controllerState == JobControllerState.STOPPED;
 	}
 
 	synchronized private void checkWaitingJobs(){
 		int s = -1;
 		LoadJobQueue<LoadJob> oldjobs = waitingQueue;
 		waitingQueue = new LoadJobQueue<LoadJob>();
 		for (LoadJob job : oldjobs){
 			job.checkState(currentTime);
 			System.out.println("job " + job.getJobID() + " with state " + 
 					job.getState().toString());
 			if (job.getState() == JobState.WAITING && s == -1){
 				s = job.submitTime - currentTime;
 			}
 			this.addToQueue(job);
 		}
		suspendDuration = s; 
 	}
 	
 	synchronized private void checkRunningJobs(){
 		LoadJobQueue<LoadJob> oldJobs = null;
 	    oldJobs = this.runningQueue;
 	    this.runningQueue = new LoadJobQueue<LoadJob>();
 		
 	    for (LoadJob nextJob : oldJobs) {
 	    	nextJob.checkState(this.currentTime);
 	    	this.addToQueue(nextJob);
 	    }
 	}
 	
 	synchronized private void startReadyJobs(){
 		LoadJobQueue<LoadJob> oldjobs = readyQueue;
 		readyQueue = new LoadJobQueue<LoadJob>();
 		for (LoadJob job: oldjobs){
 			System.out.println("start job:" + job.getJobID());
 			job.submit();
 			addToQueue(job);
 		}
 	}
 	
 	@Override
 	public void run() {
 		this.controllerState = JobControllerState.RUNNING;
 		while (this.controllerState != JobControllerState.STOPPING) {
 			while (this.controllerState == JobControllerState.SUSPENDED) {
 				try {
 					System.out.println("will sleep for " + suspendDuration + " seconds");
 					Thread.sleep(suspendDuration * 1000);
 					if (this.controllerState == JobControllerState.SUSPENDED){
 						this.controllerState = JobControllerState.RUNNING;
 					}
 					currentTime += suspendDuration;
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			checkRunningJobs();
 			checkWaitingJobs();
 			startReadyJobs();
 			if (this.completed()){
 				this.stop();
 				break;
 			}
 			else{
 				this.controllerState = JobControllerState.SUSPENDED;
 			}
 		}
 		this.controllerState = JobControllerState.STOPPED;
 	}
 }
