 package com.logrit.simulator;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 public class HRRNQueue extends Queue {
 	
 	public HRRNQueue(ProcessQueue bursting) {
 		super(bursting);
 	}
 	public HRRNQueue(ProcessQueue bursting, ProcessQueue sleeping,
 			int running_time, ArrayList<State> states) {
 		super(bursting, sleeping, running_time, states);
 	}
 
 	@Override
 	public ProcessState selectProcess() {
 		ArrayList<ProcessState> processes = bursting.getProcesses();
 		
 		Collections.sort(processes, new Comparator<ProcessState>() {
 			public int compare(ProcessState a, ProcessState b) {
 				int pA = 1 + (-1 * a.arrive_at)/a.process.getBurst_time();
 				int pB = 1 + (-1 * b.arrive_at)/b.process.getBurst_time();
 				
 				if(pA == pB)
					return a.process.getBurst_time() - b.process.getBurst_time();
				return pA - pB;
 			}
 		});
 		
 		return processes.get(0);
 	}
 	
 	public String toString() {
 		return "Highest response ratio next Queue";
 	}
 
 }
