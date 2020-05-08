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
 
     private ArrayList<String> gantt = new ArrayList<String>();
     private long lastStart = 0;
 
     public Job makeRun(long currentTime) {
         if (hasJobs()) {
             Job next = readyQ.remove(0);
            gantt.add(currentTime + "\t\t" + (currentTime - lastStart) + "\t\t" + next.getName());
             OUTPUT.println("Starting " + next.getName() + " at " + currentTime);
             lastStart = currentTime;
             next.run();
             return next;
         }
         return null;
     }
 
     public void idle(long currentTime) {
         gantt.add(currentTime + "\t\t" + (currentTime - lastStart) + "\t\tIDLE");
         lastStart = currentTime;
     }
 
     public void add(Job j, long currentTime) {
         OUTPUT.println("Adding " + j.getName() + " to the queue at " + currentTime);
         readyQ.add(j);
 
     }
 
     public boolean hasJobs() {
         return readyQ.size() > 0;
     }
 
    public void printGantt() {
        OUTPUT.println("GANTT CHART\nTIME\t\tDELTA\t\tNAME\n" +
                 "------------------------------------------");
         for (String s: gantt) {
             OUTPUT.println(s);
         }
     }
 }
