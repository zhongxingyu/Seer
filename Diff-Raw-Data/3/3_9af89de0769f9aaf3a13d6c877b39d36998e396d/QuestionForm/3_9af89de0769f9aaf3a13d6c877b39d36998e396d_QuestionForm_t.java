 package edu.unsw.cse.comp9323.group1.forms;
 
 public class QuestionForm {
 	
 	private String title;
 	private int surveyId;
 	private String id;
 	private String response;
 	
	public QuestionForm(){
		
	}
 	
 	public QuestionForm(String id, String title, String response, int surveyId){
 		this.id = id;
 		this.surveyId = surveyId;
 		this.response = response;
 		this.title = title;
 	}
 	
 	
 	public String getTitle() {
 		return title;
 	}
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	public int getSurveyId() {
 		return surveyId;
 	}
 	public void setSurveyId(int surveyId) {
 		this.surveyId = surveyId;
 	}
 	public String getId() {
 		return id;
 	}
 	public void setId(String id) {
 		this.id = id;
 	}
 	public String getResponse() {
 		return response;
 	}
 	public void setResponse(String response) {
 		this.response = response;
 	}
 	
 	
 
 }
