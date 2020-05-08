 import java.util.ArrayList;
 
 
 public class Question {
 
 	private String questionText;
 	private String questionCategory;
	private ArrayList<AnswerChoice> answers;
 	
 	
	public Question(String questionText, String questionCategory, ArrayList<AnswerChoice> answers)
 	{		
 		this.questionText = questionText;
 		this.questionCategory = questionCategory;
		this.setAnswers(answers);	
 	}
 
 
 	/**
 	 * @return the questionText
 	 */
 	public String getQuestionText() {
 		
 		
 		return questionText;
 	}
 
 
 	/**
 	 * @param questionText the questionText to set
 	 */
 	public void setQuestionText(String questionText) {
 		this.questionText = questionText;
 	}
 
 
 	/**
 	 * @return the questionCategory
 	 */
 	public String getQuestionCategory() {
 		return questionCategory;
 	}
 
 
 	/**
 	 * @param questionCategory the questionCategory to set
 	 */
 	public void setQuestionCategory(String questionCategory) {
 		this.questionCategory = questionCategory;
 	}


	/**
	 * @return the answers
	 */
	public ArrayList<AnswerChoice> getAnswers() {
		return answers;
	}


	/**
	 * @param answers the answers to set
	 */
	public void setAnswers(ArrayList<AnswerChoice> answers) {
		this.answers = answers;
	}
 	
 }
