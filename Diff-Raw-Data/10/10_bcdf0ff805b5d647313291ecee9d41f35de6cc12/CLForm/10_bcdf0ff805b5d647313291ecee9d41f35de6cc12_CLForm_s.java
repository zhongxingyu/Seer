 package commandline;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class CLForm extends CLCompositeOption {
 
 	private List<CLQuestion> questions = new ArrayList<CLQuestion>();
 	private int currentQuestion = 0;
 	private String result = null;
 	private String message;
 
 	public CLForm(String optionText, String presentMessage) {
 		super(optionText, presentMessage);
 	}
 	
 	@Override
 	public String getText() {
 		
 		StringBuffer text = new StringBuffer();
 		
 		// already finished. I'll show the result
 		if (result!= null){
 			
 			text.append(result);
 			text.append("\n");
 			
 		} else {
 			
 			if (currentQuestion==0) {
 				text.append(super.getText());
 				text.append("\n");
 			}
 			
 			if (message!=null) {
 				text.append(message);
 				text.append("\n");
 				message = null;
 			}
 			
 			text.append(questions.get(currentQuestion).getText());
 			text.append("\n");
 			
 		}
 		
 		return text.toString();
 		
 	}
 
 	public void addQuestion(String question) {
 		this.addQuestion(question, new AnswerValidator() {
 			public boolean isAnswerValid(String answer) {
 				return true;
 			}
 		});
 		
 	}
 	
 	public void addQuestion(String question, AnswerValidator answerValidator) {
 		questions.add(new CLQuestion(question, answerValidator));
 	}
 
 	
 	public String getAnswer(int i) {
 		return questions.get(i).getAnswer();
 	}
 
 	@Override
 	public CLCompositeOption answer(String answer) {
 
 		// have just ran
 		if (result!=null) {
 			
 			// reset the form
 			currentQuestion = 0;
 			result = null;
 			
 			// return to the parent menu
 			return getSuperMenu();
 			
 		} else {
 			
 			if (questions.get(currentQuestion).answer(answer)){
 				
 				currentQuestion++;
 				
 				// this was the last question
 				if (currentQuestion==questions.size()) {
 					result = run();
 				}
 				
 			} else {
				message = Constants.TEXT_ERROR + "Valor inválido!" + Constants.TEXT_NORMAL;
 				
 			}
 			
 			// maintain in this composite
 			return this;
 			
 		}
 		
 	}
 
 }
