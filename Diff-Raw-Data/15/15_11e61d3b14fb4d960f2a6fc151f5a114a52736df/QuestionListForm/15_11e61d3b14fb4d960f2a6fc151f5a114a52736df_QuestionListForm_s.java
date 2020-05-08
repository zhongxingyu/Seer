 package edu.unsw.cse.comp9323.group1.forms;
 
 import java.util.List;
 
 public class QuestionListForm {
 	
 	private List<QuestionForm> questionForm;
 	private String courseName;
	private int studentId;
 	private int surveyId;
 
 	public List<QuestionForm> getQuestionForm() {
 		return questionForm;
 	}
 
 	public void setQuestionForm(List<QuestionForm> questionForm) {
 		this.questionForm = questionForm;
 	}
 
 	public String getCourseName() {
 		return courseName;
 	}
 
 	public void setCourseName(String courseName) {
 		this.courseName = courseName;
 	}
 
	public int getStudentId() {
 		return studentId;
 	}
 
	public void setStudentId(int studentId) {
 		this.studentId = studentId;
 	}
 
 	public int getSurveyId() {
 		return surveyId;
 	}
 
 	public void setSurveyId(int surveyId) {
 		this.surveyId = surveyId;
 	}
 }
