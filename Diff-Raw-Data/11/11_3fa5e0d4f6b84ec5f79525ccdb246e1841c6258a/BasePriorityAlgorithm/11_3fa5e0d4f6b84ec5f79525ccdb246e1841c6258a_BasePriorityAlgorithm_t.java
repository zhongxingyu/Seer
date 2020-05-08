 /** BasePriorityAlgorithm.java
  * 
  * An abstract scheduling algorithm for others to inherit from.
  *
  * @author: Jon Masukawa (33128396), Yan Zhao (31018809)
  * Group #32
  * 
  * Winter 2013
  *
  */
 package com.jimweller.cpuscheduler;
 
 import java.util.*;
 
 public abstract class BasePriorityAlgorithm implements SchedulingAlgorithm {
     /** The currently running process, null if none. */
     protected Process activeJob;
 
     /** The priority queue for jobs to be stored. */
     protected PriorityQueue<Process> jobs;
     
     /** The boolean value that toggles preemptiveness. */
     protected boolean preemptive;
     
     /** Add the new job to the correct queue.*/
     public void addJob(Process p){
     	this.jobs.add(p);
     }
 
     /** Returns true if the job was present and was removed. */
     public boolean removeJob(Process p){
     	return this.jobs.remove(p);
     }
 
     /** Transfer all the jobs in the queue of a SchedulingAlgorithm to another, such as
 	when switching to another algorithm in the GUI */
     public void transferJobsTo(SchedulingAlgorithm otherAlg) {
     	Iterator<Process> iterator = this.jobs.iterator();
    	ArrayList<Process> processList = new ArrayList<Process>();
    	
     	while(iterator.hasNext()){
     		Process job = iterator.next();
    		processList.add(job);
    	}
    	
    	for(Process p : processList){
    		otherAlg.addJob(p);
    		this.removeJob(p);
     	}
     }
     
     /** Returns the next process that should be run by the CPU, null if none available.*/
     public Process getNextJob(long currentTime){
 
     	// If not preemptive, only get the next job when the current one is done
     	if (this.isPreemptive()){
         	activeJob = this.jobs.peek();    		
     	}else
     	{
     		// If no job, get the next priority one, otherwise only get
     		// The next job when the current one is done
     		if (activeJob == null)
     			activeJob = this.jobs.peek();
     		else
     		if (activeJob.isFinished())
     			activeJob = this.jobs.peek();    		
     	}
     	return activeJob;
     }
     
     /** Return a human-readable name for the algorithm. */
     public abstract String getName();
 
     /** Returns true if the current job is finished or there is no such job. */
     public boolean isJobFinished(){
 	if (activeJob != null)
 	    return activeJob.isFinished();
 	else
 	    return true;
     }
     
     /**
      * @return Value of preemptive.
      */
     public boolean isPreemptive(){
     	return preemptive;
     }
     
     /**
      * @param v  Value to assign to preemptive.
      */
     public void setPreemptive(boolean  v){
     	preemptive = v;
     }
 }
