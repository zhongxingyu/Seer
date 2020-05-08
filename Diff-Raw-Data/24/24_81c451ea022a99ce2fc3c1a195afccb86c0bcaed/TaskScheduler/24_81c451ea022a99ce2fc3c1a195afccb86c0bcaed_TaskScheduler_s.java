 package cmu.ds.mr.mapred;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 
 
 import cmu.ds.mr.conf.JobConf;
 import cmu.ds.mr.mapred.TaskStatus.TaskState;
 import cmu.ds.mr.mapred.TaskStatus.TaskType;
 import cmu.ds.mr.util.Log;
 
 class TaskScheduler {
   public static final Log LOG = new Log("TaskScheduler.class");
   
   private Queue<JobInProgress> jobQueue;
   private Map<JobID, JobInProgress> jobTable;
   private Queue<MapTask> maptaskQueue = new LinkedList();
   private Queue<ReduceTask> reducetaskQueue = new LinkedList();  
   
   public TaskScheduler(Queue<JobInProgress> jobQueue, Map<JobID, JobInProgress> jobTable){
     this.jobQueue = jobQueue;
     this.jobTable = jobTable;
   }
   /**
    * Lifecycle method to allow the scheduler to start any work in separate
    * threads.
    * @throws IOException
    */
   public void start() throws IOException {
     // do nothing
   }
   
   /**
    * Lifecycle method to allow the scheduler to stop any work it is doing.
    * @throws IOException
    */
   public void terminate() throws IOException {
     // do nothing
   }
 
   
   private synchronized boolean addTasks(){
     
     if(jobQueue.isEmpty()){
       LOG.info("addTasks(): no more jobs in job queue");
       return false;
     }
     JobInProgress jip = jobQueue.poll();
     for(int i = 1; i <= jip.getJobconf().getNumMapTasks(); ++i){
       TaskID tid = new TaskID(jip.getJobid(), TaskType.MAP, i, 1);
       maptaskQueue.offer(new MapTask(tid, jip.getJobconf(), new TaskStatus(tid, TaskState.READY, TaskType.MAP)));
     }
     
     for(int i = 1; i <= jip.getJobconf().getNumReduceTasks(); ++i){
       TaskID tid = new TaskID(jip.getJobid(), TaskType.REDUCE, i, 1);
       reducetaskQueue.offer(new ReduceTask(tid, jip.getJobconf(), new TaskStatus(tid, TaskState.READY, TaskType.REDUCE)));
     }
     return true;
   }
   
   
   /**
    * Returns the tasks we'd like the TaskTracker to execute right now.
    * 
    * @param taskTracker The TaskTracker for which we're looking for tasks.
    * @return A list of tasks to run on that TaskTracker, possibly empty.
    */
   public synchronized Task assignTask(){
 
           if(reducetaskQueue.isEmpty()){
             if(!addTasks()){
               return null;
             }
           }
           
           JobID toreducejob = reducetaskQueue.peek().getJobid();
           if(jobTable.get(toreducejob).getStatus().getMapProgress() >= 0.99){
             return reducetaskQueue.poll();
           }
         
 
           if(maptaskQueue.isEmpty()){
             if(!addTasks()){
               return null;
             }
           }
           
           return maptaskQueue.poll();
   }
   
   public synchronized Task assignTaskbasedonType(TaskType type){
     if(type == TaskType.REDUCE){
       if(reducetaskQueue.isEmpty()){
         if(!addTasks()){
           return null;
         }
       }
       JobID toreducejob = reducetaskQueue.peek().getJobid();
       if(jobTable.get(toreducejob).getStatus().getMapProgress() >= 0.99){
         return reducetaskQueue.poll();
       }
       return null;
     }
     else{
       if(maptaskQueue.isEmpty()){
         if(!addTasks()){
           return null;
         }
       }
       
       return maptaskQueue.poll();
     }
 }
   
   public synchronized List<Task> assignTasks(TaskTrackerStatus taskTracker){
     //TODO: give tasks to tasktrackers based on their status 
     return null;
   }
   
 
   /**
    * Returns a collection of jobs in an order which is specific to 
    * the particular scheduler.
    * @param queueName
    * @return
    */
   public Collection<JobInProgress> getJobs(String queueName){
     return null;
   }
    
 }
