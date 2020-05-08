 package org.processmining.plugins.bpmn.exporting.metrics;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 public class BPMNPerfMetrics {
 	Map<String, TaskPerfMetrics> taskMetrics;
 	Map<String, JoinPerfMetrics> joinMetrics;
 	String TraceName=null;
 	
 	
 	public BPMNPerfMetrics(String traceName) {
 		TraceName = traceName;
 		taskMetrics = new LinkedHashMap<String, TaskPerfMetrics>();
 		joinMetrics = new LinkedHashMap<String, JoinPerfMetrics>();
 	}
 	public String getTraceName() {
 		return TraceName;
 	}
 
 
 	public void setTraceName(String traceName) {
 		TraceName = traceName;
 	}
 
 
 	public Map<String, TaskPerfMetrics> getTaskMetrics() {
 		return taskMetrics;
 	}
 
 
 	public void setTaskMetrics(Map<String, TaskPerfMetrics> taskMetrics) {
 		this.taskMetrics = taskMetrics;
 	}
 	
 	public void addTaskMetrics(String key, TaskPerfMetrics taskMetrics) {
 		this.taskMetrics.put(key, taskMetrics);
 	}
 
 
 	public Map<String, JoinPerfMetrics> getForkMetrics() {
 		return joinMetrics;
 	}
 
 
 	public void setJoinMetrics(Map<String, JoinPerfMetrics> joinMetrics) {
 		this.joinMetrics = joinMetrics;
 	}
 	
 	public void addJoinMetrics(String key , JoinPerfMetrics  joinMetrics) {
 		this.joinMetrics.put(key, joinMetrics);
 	}
 	
 	public String toString(){
 		String task = taskMetrics.toString();
 		String join = joinMetrics.toString();
		return "NomeTraccia "+TraceName+"\n"+task+"\n"+join+"\n\n";
 		
 	}
 	
 	
 	
 }
