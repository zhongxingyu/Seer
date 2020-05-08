 package models;
 
 import java.util.ArrayList;
 
 public class MultipleChoiceQuestion extends Question {
 	ArrayList<String> choices;
 	
 	public MultipleChoiceQuestion(int id, String prompt, ArrayList<String> choices, ArrayList<String> answers) {
 		this.id = id;
 		this.question = prompt;
 		this.correctAnswers = new ArrayList<String>();
 		this.correctAnswers = answers;
 		this.choices = choices;
 		this.type = Question.QuestionType.MULTIPLE_CHOICE;
 	}
 	
 	public MultipleChoiceQuestion(String question, ArrayList<String> choices, ArrayList<String> answers) {
 		this.question = question;
 		this.correctAnswers = new ArrayList<String>();
 		this.correctAnswers = answers;
 		this.choices = choices;
 		this.type = Question.QuestionType.MULTIPLE_CHOICE;
 	}
 	
 	public String getAnswerString() {
 		String answer = "";
 		for(int i = 1; i < correctAnswers.size(); i++) {
			answer += correctAnswers.get(i) + "";
 		}
 		return answer;
 	}
 
 	public ArrayList<String> getChoices() {
 		return choices;
 	}
 
 	public void setChoices(ArrayList<String> choices) {
 		this.choices = choices;
 	}
 }
