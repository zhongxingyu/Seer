 package cmu.ds.mr.mapred;
 
 import java.io.IOException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.TreeMap;
 
 import cmu.ds.mr.conf.JobConf;
 import cmu.ds.mr.mapred.JobStatus.JobState;
 import cmu.ds.mr.util.Log;
 import cmu.ds.mr.util.Util;
 
 public class JobTracker implements JobSubmissionProtocol{
   private static final Log LOG = new Log("JobTracker.class");
 //  public static enum State { INITIALIZING, RUNNING }
 //  State state = State.INITIALIZING;
 
   private Queue<JobInProgress> jobQueue = new LinkedList<JobInProgress>();
   private Map<JobID, JobInProgress> jobTable = new TreeMap<JobID, JobInProgress>();
   private Map<TaskTracker, Boolean> tasktrackers;
   
   
   private TaskScheduler taskscheduler = new TaskScheduler(jobQueue, jobTable);
   private String jobIdentifier;  
 //  private final TaskScheduler taskScheduler = new TaskScheduler();
   
   private int nextID = 1;
   int totalSubmissions = 0;
   
   
   public JobTracker(){
     super();
   }
   
   
   public JobState submitJob(int JobID){
     if(jobQueue.contains(JobID)){
       return null;
     }
     return null;
   }
 
 
   @Override
   public JobID getNewJobId() throws IOException {
     return new JobID(jobIdentifier, nextID++);
   }
 
 
   @Override
   public synchronized JobStatus submitJob(JobID jobid, JobConf jobConf) throws IOException {
     
     //check if job already running, don't start twice
     if(jobTable.containsKey(jobid)){
       return jobTable.get(jobid).getStatus();
     }
     JobInProgress job = new JobInProgress(jobid, this, jobConf);
     
     //TODO: need to check Queue later
     if(!jobQueue.offer(job)){
 //      LOG.info("submitJob: Cannot enqueue the job");
       return null;
     }
    
     return addJob(jobid, job);
     
   }
   
   
   private synchronized JobStatus addJob(JobID jobId, JobInProgress job) {
     totalSubmissions++;
 
     synchronized (jobTable) {
         jobTable.put(jobId, job);
         job.getStatus().setState(JobState.WAITING);
 //        LOG.info("addJob(): finish adding job #" + job.getJobid().getId());
     }
     return job.getStatus();
   }
 
 
   @Override
   public void killJob(JobID jobid) throws IOException {
     // TODO Auto-generated method stub
     if (null == jobid) {
 //      LOG.info("Null jobid object sent to JobTracker.killJob()");
       return;
     }
     
     JobInProgress job = jobTable.get(jobid);
     
     if (null == job) {
 //      LOG.info("killJob(): JobId " + jobid.toString() + " is not a valid job");
       return;
     }
     
     job.kill();
   }
 
 
   @Override
   public JobStatus getJobStatus(JobID jobid) throws IOException {
     if (null == jobid) {
 //      LOG.warn("JobTracker.getJobStatus() cannot get status for null jobid");
       return null;
     }
     synchronized (this) {
       JobInProgress job = jobTable.get(jobid);
       if (job == null) {
 //        LOG.warn("JobTracker.getJobStatus() cannot get job from the given jobid");
       } 
       return job.getStatus();
     }
   }
 
   private synchronized JobStatus[] getJobStatus(Collection<JobInProgress> jips,
           boolean toComplete) {
         if(jips == null || jips.isEmpty()) {
           return new JobStatus[]{};
         }
         ArrayList<JobStatus> jobStatusList = new ArrayList<JobStatus>();
         for(JobInProgress jip : jips) {
           JobStatus status = jip.getStatus();
           status.setStartTime(jip.getStartTime());
           if(toComplete) {
             if(status.getState() == JobState.RUNNING)
               jobStatusList.add(status);
           }
           else{
             jobStatusList.add(status);
           }
         }
         return (JobStatus[]) jobStatusList.toArray(
             new JobStatus[jobStatusList.size()]);
       }
 
   @Override
   public JobStatus[] jobsToComplete() throws IOException {
     return getJobStatus(jobTable.values(), true);
   }
 
 
   @Override
   public JobStatus[] getAllJobs() throws IOException {
     return getJobStatus(jobTable.values(),false);
   }
 
 
   @Override
   public String getSystemDir() {
     // TODO Auto-generated method stub
     return null;
   }
   
   public List<Task> heartbeat(TaskTrackerStatus tasktracker){
     return taskscheduler.assignTasks(tasktracker);
   }
   
   
   public static void main(String[] args) {
     if (System.getSecurityManager() == null) {
         System.setSecurityManager(new SecurityManager());
     }
     
 //    LOG.isInfoEnabled();
 //    LOG.info("Starting jobtracker");
     try {
         String name = Util.SERVICE_NAME;
         JobTracker jobtracker = new JobTracker();
         JobSubmissionProtocol stub =
             (JobSubmissionProtocol) UnicastRemoteObject.exportObject(jobtracker, 0);
        Registry registry = LocateRegistry.getRegistry(3944);
 
         registry.rebind(name, stub);
         System.out.println("ComputeEngine bound");
 
 //        LOG.info("jobtracker bound");
     } catch (Exception e) {
       System.err.println("ComputeEngine exception:");
       e.printStackTrace();
 //        LOG.error("JobTracker exception:" + Util.stringifyException(e));
     }
   }
   
   
 }
