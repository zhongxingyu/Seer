 package edu.illinois.cs.mapreduce;
 
 import java.io.Serializable;
 
 public class TaskExecutorStatus implements Serializable {
	private final double cpuUtilization;
	private final int queueLength;
	private final double throttle;
	
	public TaskExecutorStatus(double cpuUtil, int length, double throttle) {
		this.cpuUtilization = cpuUtil;
		this.queueLength = length;
		this.throttle = throttle;
	}
	
 	
 }
