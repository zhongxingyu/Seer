 package jobs;
 
 
 import java.util.LinkedList;
 import java.util.List;
 
 public class JobQueue {
 
     private List<Job> jobList;
 
     public JobQueue() {
         jobList = new LinkedList<Job>();
     }
 
     public void append(Job job) {
         synchronized (this) {
             jobList.add(job);
         }
     }
 
     public Job peek() {
         synchronized (this) {
            return jobList.get(0);
         }
     }
 
     public Job pop() {
         synchronized (this) {
            return jobList.remove(0);
         }
     }
 
     public boolean isEmpty() {
         synchronized (this) {
             return jobList.isEmpty();
         }
     }
 
     public Integer size() {
         synchronized (this) {
             return jobList.size();
         }
     }
 }
