 package viz;
 import java.util.*;
 import Interpreter.*;
 
 public class QuestionFactory implements UpdateReasons
 {
 
 	private HashMap<String, Question> endQuestions;
 	private HashMap<String, Question> callQuestions;
 	public QuestionFactory()
 	{
 		endQuestions = new HashMap<String, Question>();
 		callQuestions = new HashMap<String, Question>();
 	}
 	
 	
 	public Question[] getQuestions()
 	{
 	
 		return ((Question[])endQuestions.entrySet().toArray());
 	}
 	
 	public Question addBeginQuestion()
 	{
 		System.out.println("Adding a begin question");
 		String var = getGlobalVar();
 		FIBQuestion question = new FIBQuestion("What will the value of " + var + " when the program has finished executing?");
 		endQuestions.put(var, question);
 		return question;
 	}
 	
 	
	public Question addCallQuestion(HashMap<String, String> paramToArg, String funName)
 	{
		String param = getRandomMember(paramToArg.keySet());
		String arg = paramToArg.get(param);
 		TFQuestion question;
 		question = new TFQuestion("If the evaluation strategy were call by reference instead of call by value, the value of " +
			param + " would have changed when " + funName + " returns");
		question.setExpectedValue(Global.getCurrentSymbolTable().get(arg));
 		question.setAnswer(false);
		callQuestions.put(arg, question);
 		
 		
 		return question;
 		
 	}
 	
 	public Question addAssignmentQuestion(HashMap<String, String> paramArgMap, String leftHandVar)
 	{
 		String arg = paramArgMap.get(leftHandVar);
 		TFQuestion question;
 		if (arg != null)
 		{
 			question = new TFQuestion(arg + "'s value will have changed after the next line of code executes");
 			question.setAnswer(false);
 		}
 		else if (Global.getSymbolTable().get(leftHandVar) != -255)
 		{
 			
 			question = new TFQuestion("In the Global scope, " + leftHandVar + "'s value will have changed after the next line of code executes");
 			if (Global.getCurrentSymbolTable().get(leftHandVar, true) != -255)
 			{
 				question.setAnswer(false);
 			}
 			else
 			{
 				question.setAnswer(true);
 			}
 		}
 		else
 		{
			question = new TFQuestion("If the evaluation strategy were pass by reference instead of pass by value, the outcome of executing the next line of code be the same.");
 			question.setAnswer(true);
 		}
 		return question;
 	}
 	
 	public Question addEndQuestion()
 	{
 		return null;
 	}
 	
 	public void addAnswers(int lineNumber, int reason)
 	{
 		System.out.println("Adding answers");
 		switch (reason)
 		{
 			case UPDATE_REASON_END:
 				System.out.println("Ok, we're updating at the end");
 				for (String key : endQuestions.keySet())
 				{
 					System.out.println(endQuestions.size());
 					Question q = endQuestions.get(key);
 
 					int answer = Global.getCurrentSymbolTable().get(key);
 					((FIBQuestion)q).addAnswer(answer + "");
 					System.out.println(q.getText() + "answer: " + answer);
 				}
 
 				break;
 
 			case UPDATE_REASON_ASSIGNMENT:
 				System.out.println("Leave assignment");
 			case UPDATE_REASON_LEAVEFUN:
 
 				System.out.println("Leave func question updates");
 				for (String key : callQuestions.keySet())
 				{
 					Question q = callQuestions.get(key);
 					if (q.getClass().getName().equals("viz.TFQuestion"))
 					{
 						int answer = Global.getCurrentSymbolTable().get(key);
 						if (((TFQuestion)q).getExpectedValue() != answer)
 						{
 							System.out.println("We werent' expecting that:\n" + key + "'s value was " +
 							answer + " and we thought it should be " + ((TFQuestion)q).getExpectedValue());
 							System.out.println("Current answer: " + ((TFQuestion)q).getAnswer());
 							if(!((TFQuestion)q).getAnswer())
 							{
 								((TFQuestion)q).flipAnswer();
 							}
 						}
 						System.out.println("New answer is " + ((TFQuestion)q).getAnswer());
 					}
 				}
 				break;
 			default:
 				//System.out.println("No answers to add for this reason");
 		}		
 	}
 	
 	private String getGlobalVar()
 	{
 		HashSet<String> varNames = Global.getSymbolTable().getCurrentVarNames();
 		return getRandomMember(varNames);
 	}
 	
 	private void getArgVar()
 	{
 		
 	}
 	
 	private String getRandomMember(HashSet<String> varNames)
 	{
 		Random r = new Random();
 		return (String)varNames.toArray()[r.nextInt(varNames.size())];
 	}
 	
	private String getRandomMember(Set<String> varNames)
 	{
 		Random r = new Random();
 		return (String)varNames.toArray()[r.nextInt(varNames.size())];
 	}
 	
 		
 }
