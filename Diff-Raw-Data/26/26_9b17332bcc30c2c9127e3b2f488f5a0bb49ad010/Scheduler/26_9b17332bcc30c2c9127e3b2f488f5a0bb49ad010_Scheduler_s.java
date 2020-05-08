 package org.processmanagement.scheduling;
 
 import java.util.ArrayList;
 
 import org.processmanagement.processes.PRandom;
 import org.processmanagement.processes.Process;
 
 /**
  * General scheduler class that is used as a baseline for the other scheduling methods * 
  *
  */
 public class Scheduler {
 	
 	//processes will be taken from the process list and put into the ReadyQueue;
 	ArrayList<Process> pList;
 	ArrayList<Process> readyQueue = new ArrayList<Process>();
 	PRandom rand = new PRandom();
 	
 	int totalBurst = 0;
 	int totalWait = 0;
 	int totalComp = 0;
 	int size = 0;
 	
 	public Scheduler(){
 		pList = new ArrayList<Process>();
 		readyQueue = new ArrayList<Process>();
 	}
 	/**
 	 * generate random processes for the queue
 	 */
 	public void genProcesses(){
 		pList = rand.genProcesses();
		System.out.println(size + " processes have been generated!"+'\n');
 	}
 	/**
 	 * Print out various stats calculated by the simulation.
 	 */
 	public void printStats(){
 		System.out.println();
 		System.out.println("------------------------------------------------------------");
 		System.out.println("Total Burst Time: " + totalBurst);
		System.out.println("Average Burst Time: " + (totalBurst/size));
		System.out.println("Average Wait Time: " + (totalWait/size));
		System.out.println("Average Completion Time: " + (totalComp/size));
 	}
 	public void printProcesses(){
 		for(Process p:pList){
 			System.out.println(p.getName()+": ");
 			System.out.println("	Arrival Time = "+p.getArrivalTime());
 			System.out.println("	Burst Time = "+p.getBurst());
 			//System.out.println("	Wait Time = "+p.getWaitTime());
 			//System.out.println("	Completion Time = "+p.getCompletionTime());
 		}
 	}
 	public int getNextArrivalTime(){
 		int time = pList.get(0).getArrivalTime();
 		for(Process p: pList){
 			if (p.getArrivalTime()<time)
 				time = p.getArrivalTime();
 		}
 		return time;
 	}
 	public ArrayList<Process> getReadyQueue(){
 		return this.readyQueue;
 	}
 	public void setReadyQueue(ArrayList<Process> queue){
 		this.readyQueue = queue;
 	}
 	public ArrayList<Process> getPList(){
 		return this.pList;
 	}
 	public void setPList(ArrayList<Process> queue){
 		this.pList = queue;
 	}
 
 }
