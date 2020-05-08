 package Scheduler;
 
 import Scheduler.jobs.Job;
 
 import java.util.ArrayList;
 import static Scheduler.RunScheduler.OUTPUT;
 
 /**
  * User: jpipe
  * Date: 2/20/13
  */
 public class Scheduler {
 
     private ArrayList<Job> readyQ = new ArrayList<Job>();
 
    private ArrayList<String> gannt = new ArrayList<String>();
     private long lastStart = 0;
 
     public Job makeRun(long currentTime) {
         if (hasJobs()) {
             Job next = readyQ.remove(0);
            gannt.add(currentTime + "\t\t" + (currentTime  - lastStart) + "\t\t" + next.getName());
             OUTPUT.println("Starting " + next.getName() + " at " + currentTime);
             lastStart = currentTime;
             next.run();
             return next;
         }
         return null;
     }
 
     public void add(Job j, long currentTime) {
         OUTPUT.println("Adding " + j.getName() + " to the queue at " + currentTime);
         readyQ.add(j);
 
     }
 
     public boolean hasJobs() {
         return readyQ.size() > 0;
     }
 
    public void printGannt() {
        OUTPUT.println("GANNT CHART\nTIME\t\tDELTA\t\tNAME\n" +
                 "------------------------------------------");
        for (String s: gannt) {
             OUTPUT.println(s);
         }
     }
 }
