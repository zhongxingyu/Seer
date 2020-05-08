 package Scheduler;
 
 import Scheduler.jobs.Job;
 
 import static Scheduler.RunScheduler.OUTPUT;
 
 /**
  * User: jpipe
  * Date: 2/20/13
  */
 public class SystemSimulator extends Thread {
 
     private static final long QUANTUM = 1000L;
 
     private Scheduler scheduler;
 
     private boolean jobsStillToSubmit = true;
     private long startTime;
     private Job currentJob;
 
     public SystemSimulator() {
         this.scheduler = new Scheduler();
         this.setName("System Simulator");
     }
 
     public void run() {
         startTime = System.currentTimeMillis();
         while (jobsStillToSubmit || scheduler.hasJobs()) {
             try {
                 if (interrupted()) {
                     throw new InterruptedException();
                 }
                 sleep(QUANTUM);
             } catch (InterruptedException e) {
                 log(currentJob, " terminated at " + relativeTime());
                 currentJob = scheduler.makeRun(relativeTime());
             }
         }
 
        scheduler.printGantt();
     }
 
     public void noMoreJobsToSubmit() {
         OUTPUT.println("No more jobs");
         jobsStillToSubmit = false;
     }
 
     public void addNewProcess(Job j) {
         scheduler.add(j, relativeTime());
         interrupt();
     }
 
     public void exit() {
         interrupt();
     }
 
     public long relativeTime() {
         return System.currentTimeMillis() - startTime;
     }
 
     private void log(Job j, String s) {
         if (j != null) {
             OUTPUT.println(j.getName() + s);
         }
     }
 }
