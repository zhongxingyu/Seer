 import java.util.ArrayList;
 
 
public class Question {
 
 	private String questionText;
 	private String questionCategory;
 	private ArrayList<Object> answers;
 	
 	
	public Question(String questionText, String questionCategory, ArrayList<Object> answers)
 	{		
 		this.questionText = questionText;
 		this.questionCategory = questionCategory;
 		this.answers = answers;	
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
 	
 }
