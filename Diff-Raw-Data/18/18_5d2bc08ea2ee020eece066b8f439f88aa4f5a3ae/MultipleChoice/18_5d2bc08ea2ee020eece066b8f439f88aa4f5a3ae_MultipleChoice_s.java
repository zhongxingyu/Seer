 package quiz;
 
 import java.util.ArrayList;
 import java.util.Set;
 
import com.sun.tools.javac.util.List;
 
 public class MultipleChoice extends Question {
 	
 	private List<String> choices;
 	private String prompt;
 	
 	
 
 	public MultipleChoice(int question_id, int quiz_id, int question_number, String question, List<String> choice_strs, Set<String> answer_strings) {
 		questionID = question_id;
 		quizID = quiz_id;
 		question_num = question_number;
 		prompt = question;
 		choices = choice_strs;
 		question_text = (question + "<br />Choices: " + choice_strs.toString());
 		answers = answer_strings;
 	}
 	
 	@Override
 	public String PrintQuestion() {
 		String question_str = (prompt + "<br />");
 		for (int i = 0; i < choices.size(); i++) {
 			String option = "<input type=\"radio\" name=\"" + questionID + "\" value=\"" + choices.get(i) + "\"> " + choices.get(i);
 			question_str += option;
 		}
 		return question_str;
 	}
 
 	@Override
 	public boolean CheckAnswer(String input) {
 		for (String answer : answers) {
 			if (answer.compareToIgnoreCase(input) == 0) return true;
 		}
 		return false;
 	}
 
 }
