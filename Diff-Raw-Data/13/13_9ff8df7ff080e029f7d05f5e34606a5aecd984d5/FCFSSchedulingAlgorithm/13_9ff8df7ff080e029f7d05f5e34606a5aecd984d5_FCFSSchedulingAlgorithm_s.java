 /** FCFSSchedulingAlgorithm.java
  * 
  * A first-come first-served scheduling algorithm.
  *
  * @author: Jon Masukawa (33128396), Yan Zhao (31018809)
  * Group #32
  * 
  * Winter 2013
  *
  */
 package com.jimweller.cpuscheduler;
 
 import java.util.*;
 
 public class FCFSSchedulingAlgorithm extends BaseSchedulingAlgorithm {
 
 	protected LinkedList<Process> jobs;
 	
 	protected Process activeJob;
 	
     FCFSSchedulingAlgorithm(){
     	this.jobs = new LinkedList<Process>();
     	this.activeJob = null;
     }
 
     /** Add the new job to the correct queue.*/
     public void addJob(Process p){
     	jobs.add(p);
     }
     
     /** Returns true if the job was present and was removed. */
     public boolean removeJob(Process p){
     	return jobs.remove(p); // Just a place holder to get things to compile, remove this when implementing
     }
 
     /** Transfer all the jobs in the queue of a SchedulingAlgorithm to another, such as
 	when switching to another algorithm in the GUI */
     public void transferJobsTo(SchedulingAlgorithm otherAlg) {
     	ArrayList<Process> temp = new ArrayList<Process>();
     	Iterator<Process> iterator = jobs.iterator();
     	
     	while (iterator.hasNext()) {
     		temp.add(iterator.next());
     	}
     	
     	for (Process process : temp) {
 			otherAlg.addJob(process);
 			this.removeJob(process);
 		}
     }
 
     public boolean shouldPreempt(long currentTime){
     	return false; // Just a place holder to get things to compile, remove this when implementing
     }
 
     /** Returns the next process that should be run by the CPU, null if none available.*/
     public Process getNextJob(long currentTime){
     	if (activeJob == null || activeJob.isFinished()) {
     		activeJob = jobs.remove();
     	}
     		return activeJob; // Just a place holder to get things to compile, remove this when implementing
     }
 
     public String getName(){
     	return "First-come first-served";
     }
 }
