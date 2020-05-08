 package models;
 
 import java.util.ArrayList;
 
 import models.Question.QuestionType;
 
 public class Question {
 	public enum QuestionType {
 		QUESTION_RESPONSE,
 		FILL_IN,
 		MULTIPLE_CHOICE,
 		PICTURE_RESPONSE,
 	}
 	
 	protected int id;
 	protected int quizID;
 	protected String question;
 	protected int orderIndex;
 	protected QuestionType type;
 	protected ArrayList<String> correctAnswers;
 
 	public Question() {
 		
 	}
 	
 	public ArrayList<String> getCorrectAnswers() {
 		return this.correctAnswers;
 	}
 	
 	public boolean isCorrectAnswer(String answer) {
 		return correctAnswers.contains(answer);
 	}
 
 	public String getQuestion() {
 		return question;
 	}
 
 	public void setQuestion(String question) {
 		this.question = question;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 	
 	public String getAnswerString() {
		return "";
 	}
 	
 	public int getOrderIndex() {
 		return orderIndex;
 	}
 
 	public void setOrderIndex(int orderIndex) {
 		this.orderIndex = orderIndex;
 	}
 	
 	public int getQuizID() {
 		return this.quizID;
 	}
 	
 	public void setQuizID(int id) {
 		this.quizID = id;
 	}
 	
 	public QuestionType getType() {
 		return this.type;
 	}
 	
 	public int getTypeInt() throws Exception {
 		switch(type) {
 		case QUESTION_RESPONSE:
 			return 0;
 		case FILL_IN:
 			return 1;
 		case MULTIPLE_CHOICE:
 			return 2;
 		case PICTURE_RESPONSE:
 			return 3;
 		default:
 			throw new Exception("Undefined question type: " + type);
 		}
 	}
 
 	public static QuestionType QuestionType(int questionType) throws Exception {
 		switch(questionType) {
 		case 0:
 			return QuestionType.QUESTION_RESPONSE;
 		case 1:
 			return QuestionType.FILL_IN;
 		case 2:
 			return QuestionType.MULTIPLE_CHOICE;
 		case 3:
 			return QuestionType.PICTURE_RESPONSE;
 		default:
 			throw new Exception("Undefined question type: " + questionType);
 		}
 	}
 }
