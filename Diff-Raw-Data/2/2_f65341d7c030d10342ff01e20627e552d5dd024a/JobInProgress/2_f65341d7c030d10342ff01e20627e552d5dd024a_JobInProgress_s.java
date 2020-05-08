 package cmu.ds.mr.mapred;
 
 
 
 import cmu.ds.mr.conf.JobConf;
 import cmu.ds.mr.util.Log;
 
 public class JobInProgress {
   static final Log LOG = new Log("JobInProgress.class");
   
   private JobID jobid;
   private JobStatus status;
   private JobConf jobconf;
   
   private JobTracker jobtracker;
   
   private int maxMapsPerNode;
   private int maxReducesPerNode;
   private int runningMapLimit;
   private int runningReduceLimit;
   
   private volatile boolean launchedCleanup = false;
   private volatile boolean launchedSetup = false;
   private volatile boolean jobKilled = false;
   private volatile boolean jobFailed = false;
   JobInitKillStatus jobInitKillStatus = new JobInitKillStatus();
   
   long startTime;
   long launchTime;
   long finishTime;
   
   
   public long getStartTime() {
     return startTime;
   }
 
   public void setStartTime(long startTime) {
     this.startTime = startTime;
   }
   
   public JobInProgress(JobID jobid, JobTracker tracker, JobConf jobconf){
     this.jobid = jobid;
     this.jobtracker = tracker;
     this.jobconf = jobconf;
     
     this.startTime = System.currentTimeMillis();
     this.status.setStartTime(startTime);
   }
 
   public JobID getJobid() {
     return jobid;
   }
 
   public void setJobid(JobID jobid) {
     this.jobid = jobid;
   }
 
   public JobStatus getStatus() {
     return status;
   }
 
   public void setStatus(JobStatus status) {
     this.status = status;
   }
 
   public JobConf getJobconf() {
     return jobconf;
   }
 
   public void setJobconf(JobConf jobconf) {
     this.jobconf = jobconf;
   }
 
   public JobTracker getJobtracker() {
     return jobtracker;
   }
 
   public void setJobtracker(JobTracker jobtracker) {
     this.jobtracker = jobtracker;
   }
   
   public void kill(){
     boolean killNow = false;
     synchronized(jobInitKillStatus) {
       if(jobInitKillStatus.killed) {//job is already marked for killing
         return;
       }
       jobInitKillStatus.killed = true;
       //if not in middle of init, terminate it now
       if(!jobInitKillStatus.initStarted || jobInitKillStatus.initDone) {
         //avoiding nested locking by setting flag
         killNow = true;
       }
     }
     if(killNow) {
       terminate(JobStatus.JobState.KILLED);
     }
   }
   
   private synchronized void terminate(JobStatus.JobState jobTerminationState) {
     //TODO: how to termintate a job
   }
   
   
   public synchronized Task obtainNewNonLocalMapTask(TaskTrackerStatus tts){
     
     //TODO: need to implement
     
     return null;
   }
   
   
   
   private static class JobInitKillStatus {
     //flag to be set if kill is called
     boolean killed;
     
     boolean initStarted;
     boolean initDone;
   }
 }
