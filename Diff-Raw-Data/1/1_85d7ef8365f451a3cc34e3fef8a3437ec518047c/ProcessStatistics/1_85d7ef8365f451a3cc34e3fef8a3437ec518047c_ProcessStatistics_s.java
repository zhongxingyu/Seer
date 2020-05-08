 package com.logrit.simulator;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 public class ProcessStatistics {
 	private Process process;
 	int total_execution_time;
 	int total_sleep_time;
 	int total_wait_time;
 	
 	ProcessStatistics(Process process, int total_execution_time, int total_sleep_time, int total_wait_time) {
 		this.process = process;
 		this.total_execution_time = total_execution_time;
 		this.total_sleep_time = total_sleep_time;
 		this.total_wait_time = total_wait_time;
 	}
 	
 	public double getResponseTime() {
 		int total_runs = total_execution_time/process.getBurst_time();
 		return ((double)(total_execution_time + total_wait_time))/total_runs;
 	}
 	
 	public Process getProcess() {
 		return this.process;
 	}
 	
 	public double getSlowDown() {
 		return ((double)getTotalTime())/total_execution_time;
 	}
 	
 	public void addExecutionTime(int i) {
 		assert(i >= 0);
 		total_execution_time += i;
 	}
 	
 	public void addSleepTime(int i) {
 		assert(i >= 0);
 		total_sleep_time += i;
 	}
 	
 	public void addWaitTime(int i) {
 		assert(i >= 0);
 		total_wait_time += i;
 	}
 	
 	private static HashMap<Process, ProcessStatistics> _statistics = new HashMap<Process, ProcessStatistics>();
 	public static int _total_time = 0;
 	public static int running_time = 0;
 	
 	public static ProcessStatistics getStatistics(Process p) {
 		if(!_statistics.containsKey(p)) {
 			_statistics.put(p, new ProcessStatistics(p, 0, 0, 0));
 		}
 		return _statistics.get(p);
 	}
 	
 	public static void setTotalTime(int i) {
 		assert(i >= _total_time);
 		_total_time = i;
 	}
 	
 	public static int getTotalTime() {
 		return _total_time;
 	}
 	
 	public static double getCPUUtilization() {
 		return ((double)running_time)/_total_time;
 	}
 	
 	public static Collection<ProcessStatistics> getAllStats() {
 		return _statistics.values();
 	}
 	
 	public static void reset() {
 		_statistics = new HashMap<Process, ProcessStatistics>();
 		_total_time = 0;
 		running_time = 0;
 	}
 	
 	public static void createStats(ArrayList<State> states) {
 		State prev = states.remove(0);
 		for(State s : states) {
 			// If the state was in burst, and is now in sleep, it executed burst_time
 			for(ProcessState p : s.sleep_queue.getProcesses()) {
 				ProcessState prev_state = prev.queue.findProcessState(p.process);
 				if(prev_state != null) {
 					getStatistics(p.process).addExecutionTime(p.process.getBurst_time());
 				}
 			}
 			
 			// If the state was sleeping, but moved to the burst_queue, it slept sleept_time
 			for(ProcessState p : s.queue.getProcesses()) {
 				ProcessState prev_state = prev.sleep_queue.findProcessState(p.process);
 				if(prev_state != null) {
 					getStatistics(p.process).addSleepTime(p.process.getSleep_time());
 				}
 			}
 			
 			// If it was in the burst queue, and still it is, it had to wait
 			for(ProcessState p : s.queue.getProcesses()) {
 				ProcessState prev_state = prev.queue.findProcessState(p.process);
 				if(prev_state != null) {
 					// Make sure to NOT cummulate time from the previous waiting
 					getStatistics(p.process).addWaitTime(prev_state.arrive_at);
 					getStatistics(p.process).addWaitTime(-1 * p.arrive_at);
 				}
 			}
 		}
 	}
 }
