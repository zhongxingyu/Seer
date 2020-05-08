 package com.crowdgame.model;
 
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.collect.Maps;
 
 
 
 
 public class TaskInput {
 
 	private Integer batchId;
 	
 	private Integer taskId;
 	
 	private Map<String, Object> contents;
 
 	public TaskInput() {
 		this.contents = Maps.newHashMap();
 	}
 
 	public Integer getBatchId() {
 		return batchId;
 	}
 
 	public void setBatchId(Integer batchId) {
 		this.batchId = batchId;
 	}
 
 	public Integer getTaskId() {
 		return taskId;
 	}
 
 	public void setTaskId(Integer taskId) {
 		this.taskId = taskId;
 	}
 
 	public void setContents(Map<String, Object> contents) {
 		this.contents = contents;
 	}
 
 	public String getType() {
 		return (String) contents.get("type");
 	}
 
 	public String getWord() {
 		return (String) contents.get("word");
 	}
 
 	public Integer getProblemId() {
 		return (Integer) contents.get("id");
 	}
 	
 	public Integer getLevel() {
 		return (Integer) contents.get("level");
 	}
 	
 	public String getLanguage() {
 		return (String) contents.get("language");
 	}
 	
 	public String getDisplayText() {
 		return (String) contents.get("display");
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<String> getAnswers() {
 		return (List<String>) contents.get("answers");
 	}
 }
