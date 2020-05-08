 package com.example.model;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 public class ExecutionInfo {
 
 	private Integer taskId;
 	
 	private String contents;
 	
 	public ExecutionInfo(ExecutionResults results) {
 		this.taskId = results.getId();
 		this.contents = encodeContents(results);
 	}
 	
 	private String encodeContents(ExecutionResults results) {
 		ObjectMapper mapper = new ObjectMapper();
 		ObjectNode node = mapper.createObjectNode();
 		node.put("failedAttempts", results.getFailedAttempts());
 		node.put("timeSpent", results.getTimeSpent());
 		ArrayNode array = mapper.createArrayNode();
 		for (String wrongAnswer : results.getWrongAnswers()) {
 			array.add(wrongAnswer);
 		}
 		node.put("wrongAnswers", array);
 		return node.toString();
 	}
 
 	public Integer getTaskId() {
 		return taskId;
 	}
 
 	public void setTaskId(Integer taskId) {
 		this.taskId = taskId;
 	}
 
 	public String getContents() {
 		return contents;
 	}
 
 	public void setContents(String contents) {
 		this.contents = contents;
 	}
 	
 	
 }
