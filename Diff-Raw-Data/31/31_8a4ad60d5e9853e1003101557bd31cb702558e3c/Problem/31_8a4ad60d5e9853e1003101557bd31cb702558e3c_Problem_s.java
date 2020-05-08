 package com.crowdgame.model;
 
 import java.util.List;
 
 import com.google.common.collect.Lists;
 
 public class Problem {
 
 	private Integer id;
 	
 	private String type;
 	
 	private String word;
 	
 	private String display;
 	
 	private List<String> answers;
 	
 	private List<String> displayText;
 	
 	public Problem() {
 		
 	}
 	
 	public Problem(TaskInput task) {
		this.id = task.getTaskId();
 		this.type = task.getType();
 		this.word = task.getWord();
 		this.display = task.getDisplayText();
 		this.answers = task.getAnswers();
 	}
 	
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
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
