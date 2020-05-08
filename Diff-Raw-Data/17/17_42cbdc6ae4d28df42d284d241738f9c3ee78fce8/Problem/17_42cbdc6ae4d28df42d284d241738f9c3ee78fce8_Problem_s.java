 package com.crowdgame.model;
 
 import java.util.List;
 
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 
 import org.hibernate.annotations.IndexColumn;
 
 import com.google.common.collect.Lists;
 
 @Entity
 public class Problem {
 
 	@Id
 	@GeneratedValue
 	private Integer id;
 	
 	private Integer batchId;
 	
 	private Integer taskId;
 	
 	private String type;
 	
 	private String word;
 	
 	private String display;
 	
 	@ElementCollection(fetch=FetchType.EAGER)
     @IndexColumn(name = "POSITION", base = 1)
 	private List<String> answers;
 	
 	@ElementCollection(fetch=FetchType.EAGER)
     @IndexColumn(name = "POSITION", base = 1)
 	private List<String> displayText;
 	
 	public Problem() {
 		
 	}
 	
 	public Problem(String type, String word, String display, List<String> answers) {
 		this.type = type;
 		this.word = word;
 		this.display = display;
 		this.answers = answers;
 		generateProblem();
 	}
 	
 	public Problem(TaskInput task) {
 		this.batchId = task.getBatchId();
		this.taskId = task.getId();
 		this.type = task.getType();
 		this.word = task.getWord();
 		this.display = task.getDisplayText();
 		this.answers = task.getAnswers();
 		generateProblem();
 	}
 	
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
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
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public String getWord() {
 		return word;
 	}
 
 	public void setWord(String word) {
 		this.word = word;
 	}
 
 	public List<String> getAnswers() {
 		return answers;
 	}
 
 	public void setAnswers(List<String> answers) {
 		this.answers = answers;
 	}
 
 	public List<String> getDisplayText() {
 		return displayText;
 	}
 
 	public void setDisplayText(List<String> displayText) {
 		this.displayText = displayText;
 	}
 	
 	public String getDisplay() {
 		return display;
 	}
 
 	public void setDisplay(String display) {
 		this.display = display;
 		generateProblem();
 	}
 
 	public void generateProblem() {
 		this.displayText = Lists.newArrayList();
 		this.display = this.display.replace("_", " ");
 		if (this.display.charAt(0) == '[') {
 			String cutString = this.display.substring(1, this.display.length() - 1);
 			String[] parts = cutString.split("\\|");
 			for (String part : parts) {
 				displayText.add(part);
 			}
 		} else {
 			String[] parts = this.display.split("\\|");
 			for (int i = 0; i < parts.length; ++i) {
 				if (i % 2 == 0) {
 					char[] letters = parts[i].toCharArray();
 					for (char letter : letters) {
 						displayText.add(String.valueOf(letter));
 					}
 				} else {
 					displayText.add(parts[i]);
 				}
 			}
 		}
 	}
 }
