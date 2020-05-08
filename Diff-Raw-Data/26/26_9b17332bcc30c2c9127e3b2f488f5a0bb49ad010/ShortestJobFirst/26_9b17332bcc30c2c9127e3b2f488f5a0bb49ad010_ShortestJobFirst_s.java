 package org.processmanagement.scheduling;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import org.processmanagement.processes.Process;
 
 public class ShortestJobFirst extends Scheduler{
 
 	/**
 	 * Sorting class to sort queue by burst time
 	 */
 	private ArrayList<Process> sortQueue(ArrayList<Process> list){
 		
 		//java sort method with a comparator class to compare arrival times
 		Collections.sort(list, new Comparator<Process>() {
 			public int compare(Process process1, Process process2) {
 				 return process1.getBurst() - process2.getBurst();
 			}
 		});
 		return list;
 	}
 	
 	/**
 	 * Method that starts the shortest job first scheduler simulation.
 	 */			
 	public void start(){
 		for(Process p:pList){
 			totalBurst += p.getBurst();
 		}
 		printProcesses();
 		size = pList.size();
 		pList = sortQueue(pList);//sorts the processList
 		System.out.println("Running Processes...");
 		//step through the queue and simulate the burst time for each process	
 		sjf();
 		printStats();
 	}
 
 	public void sjf() {
 		int elapsedBurst = 0;
 		for (Process p : pList) {
 			readyQueue.add(p.deepCopy());
 		}
 		while (!readyQueue.isEmpty()) {
 			boolean idle = true;
 			for (int i = 0; i < readyQueue.size(); i++) {
 				Process p = readyQueue.get(i);
 				if (p.getArrivalTime() <= elapsedBurst) {
 					System.out.print(elapsedBurst + "---[" + p.getName()
 							+ "]---");
 					idle = false;
 					p.setWaitTime(elapsedBurst - p.getArrivalTime());
 					totalWait += p.getWaitTime();
 					elapsedBurst += p.getBurst();
 					totalComp += (elapsedBurst - p.getArrivalTime());
 					p.setCompletionTime(elapsedBurst - p.getArrivalTime());
					pList.set(pList.indexOf(p), p);
 					readyQueue.remove(p);
 					i--;
 				}
 			}
 			if (idle) {
 				System.out.print(elapsedBurst + "---[IDLE]---");
 				elapsedBurst = getNextArrivalTime();
 			}
 		}
 		System.out.println(elapsedBurst);
 	}
 	
 	
 }
