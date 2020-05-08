 package com.logrit;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 
 import com.logrit.simulator.FCFSQueue;
 import com.logrit.simulator.HRRNQueue;
 import com.logrit.simulator.Process;
 import com.logrit.simulator.ProcessQueue;
 import com.logrit.simulator.ProcessStatistics;
 import com.logrit.simulator.Queue;
 import com.logrit.simulator.SJFQueue;
 
 public class queue_simulator {
 	
 	public static void run(int hdd_speed, Queue queue_type) throws CloneNotSupportedException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
 		ProcessQueue processes = new ProcessQueue();
 		Process.resetPid();
 		
 		for(int i=0; i < 7; i++) {
 			if(i % 2 == 0) {
 				processes.addProcess(Process.makeProcess(4, hdd_speed), 0);
 			} else {
 				processes.addProcess(Process.makeProcess(6, hdd_speed), 0);
 			}
 		}
 		processes.addProcess(Process.makeProcess(128, hdd_speed), 0);
 		
 		Queue queue = queue_type.getClass().getConstructor(processes.getClass()).newInstance((ProcessQueue) processes.clone());
 		queue.run();
 		
 		System.out.println("Results for " + queue + " with hdd speed of " + hdd_speed);
 		// Print the results
 		for(int i=0; i < 7; i++) {
 			ProcessStatistics p = ProcessStatistics.getStatistics(processes.getProcesses().get(i).getProcess());
 			System.out.println(String.format("Response time for PID(%d): %f", p.getProcess().getPid(), p.getResponseTime()));
 			System.out.flush();
 		}
 		int i=7;
 		ProcessStatistics p = ProcessStatistics.getStatistics(processes.getProcesses().get(i).getProcess());
 		System.out.println(String.format("Slowdown  for PID(%d): %f", p.getProcess().getPid(), p.getSlowDown()));
 		System.out.flush();
 		System.out.println("CPU Utilization: " + ProcessStatistics.getCPUUtilization());
 	}
 
 	public static void main(String[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, CloneNotSupportedException {
 		// Using 1ms switch time
 		Queue.CONTEXT_SWITCHING_TIME = 1;
 		run(9, new SJFQueue(null));
 		run(9, new HRRNQueue(null));
 	}
 
 }
