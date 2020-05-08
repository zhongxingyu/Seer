 package taskdispatcher;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Queue;
 import java.util.Set;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 /**
  * Dispatches Jobs to TaskRunners.
  * Is able to have more workers added during run time.
  * Will ballance the load evenly across workers.
  * If a task runner fails, then the jobs yet to be run for
  * that runner will be reassigned to the others.
  * @author gg32
  * @param <R> The type of the TaskRunner used by this instance.
 * @param <J> The type of job to be taskdispatcher
  */
 public abstract class AbstractTaskDispatcher <R extends AbstractTaskRunner<J>, J extends Job> {
     //TODO Error recovery from failed jobs.
     //TODO Once all jobs have been issued, reissue those that are not yet
     //completed to other machines
 
     /**
      * A thread-safe list of the task runners used by this dispatcher.
      * This list may include runners which are no longer active.
      */
     protected List<R> taskRunners = new CopyOnWriteArrayList<>();
     
     private Thread mainThread;
     private HashMap<String, J> jobs = new HashMap<>();
     private HashMap<String, J> finishedJobs;
     private Queue<String> progressQueue = new ConcurrentLinkedQueue<>();
     protected TaskProgress progress = null;
     private int generation = 0;
     private int jobNum=0;
     private boolean failed;
     private String error;
     
     /**
      * Create a new TaskDispatcher.
      * This must be called from the thread that will call the start function.
      */
     public AbstractTaskDispatcher(){
         mainThread = Thread.currentThread();
     }
     
     /**
      * Publish a message to the TaskProgress if one has been attached.
      * @param message The message to be send to the TP.
      */
     protected void publishMessage(String message){
         if(progress!=null)progress.message(message);
     }
     
     /**
      * Set up this TaskDispatcher. Children should implement this method to
      * setup and sockets or threads that they wish to use. This method should be
      * called after the constructor, and before the start method.
      */
     public abstract void setUp();
     
     /**
      * Register a TaskProgress to be updated of the progress of this dispatcher.
      * @param tp the TaskProgress
      */
     public void addTaskProgress(TaskProgress tp){
         progress = tp;
     }
     
     /**
      * Starts issuing the jobs that have been added to the dispatcher.
      * Jobs are sent evenly across all the runners that this dispatcher is
      * using. This method will return once all jobs have been completed.
      * Or if there has been a problem running one of the Jobs.
      * @return Returns true if there has been a failure.
      */
     public boolean start(){
         finishedJobs = new HashMap<>(jobs.size());
         issueJobs();
         receiveAll();
         return failed;
     }
     
     /**
      * Clears the job list. Should be called after a set a complete generation
      * has been executed.
      */
     public void newGeneration(){
         jobs.clear();
         jobNum=0;
         generation++;
     }
     
     /**
      * Ends any working threads and closes any communication channels.
      * This should be called once you have no more use of this object.
      */
     public abstract void end();
     
     /**
      * Add a job to executed by this task dispatcher.
      * @param job The job to be executed.
      */
     public void addJob(J job){
         jobs.put(job.getID(), job);
     }
     
     /**
      * Returns all the finished jobs.
      * @return A map of the ID of a job to the job itself.
      */
     public HashMap<String, J> getFinishedJobs(){
         return finishedJobs;
     }
     
     /**
      * Runs until either all jobs are finished or there has been an error 
      * running a job. If there has been no error then the collection of
      * finished jobs will have been filled.
      */
     private void receiveAll(){
         boolean anyLeft = true;
         while(!failed && anyLeft){
             updateProgress();
             anyLeft = false;
             for(R cm : taskRunners){
                 if(cm.isAlive()){
                     if(!cm.jobs.isEmpty()){
                         anyLeft = true;
                         break;
                     }
                 }
                 else{
                     if(!cm.jobs.isEmpty()){
                         Set<String> failedJobs = cm.jobs.keySet();
                         cm.jobs.clear();
                         for(String jID : failedJobs){
                             if(failed)return;
                             issueJob(jID);
                         }
                         anyLeft = true;
                     }
                 }
             }
             synchronized(this){
                 if(anyLeft&&!Thread.interrupted()){
                     try{
                         wait(2000);
                     }
                     catch(InterruptedException e){
                     
                     }
                 }
             }
         }
         if(progress!=null)progress.message("All jobs finished for generation " + generation);
         for(R cm : taskRunners){
             finishedJobs.putAll(cm.finishedJobs);
             cm.finishedJobs.clear();
         }
         updateProgress();        
     }
     
     /**
      * If no TaskRunner has any free space for jobs then this function will wait
      * until at least one space is free.
      */
     private void waitForSpace(){
         boolean noSpace = true;
         while(!failed && noSpace){
             updateProgress();
             for(R cm : taskRunners){
                 if(cm.isAlive()&&cm.maxJobs!=cm.jobs.size()){
                     noSpace=false;
                     break;
                 }
             }
             synchronized(this) {
                 if(noSpace&&!Thread.interrupted()){
                     try{
                         wait(2000);
                     }
                     catch(InterruptedException e){
                     
                     }
                 }
             }
 
         }
         updateProgress();
     }
     
     /**
      * Update the TaskProgress listener with any progress from the TaskRunners.
      */
     private void updateProgress(){
         while(progress!=null&&progressQueue.peek()!=null)
             progress.tick(progressQueue.poll());
     }
     
     /**
      * Issue jobs to the TaskRunners. This will return when all jobs have been
      * issued or there has been a failure.
      */
     private void issueJobs(){
         for(String job : jobs.keySet()){
             if(failed)return;
             issueJob(job);
         }
     }
     
     /**
      * Issue the job with the given jobID to a TaskRunner. A job will be issued
      * to the TaskRunner with the most free processing elements that is still
      * alive. If there is no free space on a TaskRunner then this method will
      * wait until there is space.
      * @param jobID 
      */
     private void issueJob(String jobID){
         AbstractTaskRunner<J> min = null;
         int nFree = -1;
         for(R cm : taskRunners){
             if(cm.isAlive()&&cm.jobs.size()<cm.maxJobs&&(nFree<0||(cm.maxJobs-cm.jobs.size())>nFree)){
                 min = cm;
                 nFree = cm.maxJobs-cm.jobs.size();
             }
         }
         if(nFree==-1){
             waitForSpace();
             issueJob(jobID);
         }
         else{
             min.addTask(jobs.get(jobID));
         }
     }
     
     /**
      * To be called by a TaskRunner once a job has been completed.
      * @param jobID The ID of the completed job (unused).
      */
     public void jobCompleted(String jobID){
         mainThread.interrupt();
         if(progress!=null)
             progressQueue.add("Completed Generation: " + generation + " \tTask Number: " + ++jobNum);
     }
     
     /**
      * Report that a job on a TaskRunner has failed. That is that the job
      * managed to complete but in an incorrect state. It is assumed at this point
 	 * that there is no point continuing with other jobs.
      * @param jobID
      * @param e 
      */
     public void jobFailed(String jobID, String e){
         //What to do when we fail?
         //Quit the evo prog?
         System.out.println("Called jobFailed");
         failed = true;
         error = e;
         if(progress!=null)progress.fail("Error : " + e);
         mainThread.interrupt();
     }
     
     /**
      * If this TaskDispatcher has failed then return the error that caused
      * failure.
      * @return The cause of failure.
      */
     public String getError(){
         return error;
     }
 
 }
