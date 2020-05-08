 package org.titanomachia.mclogcmdexec.command;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.titanomachia.mclogcmdexec.ApplicationContext;
 
 public class Answer extends Command {
 
 	@Override
 	public void execute() {
 		Problem<Problem<?>> problem = ApplicationContext.getValue("quiz.problem." + getUser());
 		
 		if (null == problem) {
 			return;
 		}
 		
 		String answer = getArgs().trim();
 		
 		if ("".equals(answer)) {
 			return;
 		}
 		
 		boolean correct = problem.isCorrectAnswer(answer);
 		
 		String response = correct ? "Correct!" : ("Wrong... " + problem.getAnswer());
 
 		List<Problem<?>> streak = ApplicationContext.getValue("quiz.streak." + getUser());
 		if (correct) {
 			if (null == streak) {
 				streak = new ArrayList<Problem<?>>();
 			}
 			
 			Integer currentPoints = ApplicationContext.getValue("quiz.points." + getUser());
 			if (null == currentPoints) {
 				currentPoints = 0;
 			}
 			
 			currentPoints = currentPoints + problem.calculatePoints(streak);
 			
 			ApplicationContext.setValue("quiz.points." + getUser(), currentPoints);
 			
 			streak.add(problem);
 			
 			response += " That's " + streak.size() + " in a row! You now have " + currentPoints + " point" + (currentPoints == 1 ? "" : "s");
 			CommandUtils.writeToConsole(response, getUser());
 			
 			ApplicationContext.setValue("quiz.streak." + getUser(), streak);
 		}
 		else {
 			if (null != streak && streak.size() > 0) {
 				response += ". You had a streak of " + streak.size() + " going.";
 				
				ApplicationContext.setValue("quiz.streak." + getUser(), new ArrayList<Problem<?>>());
 			}
			
			Integer currentPoints = ApplicationContext.getValue("quiz.points." + getUser());
			if (null == currentPoints) {
				currentPoints = 0;
			}
			
			response += ". You have " + currentPoints + " point" + (currentPoints == 1 ? "" : "s");

	        CommandUtils.writeToConsole(response, getUser());
 		}
 		
 		ApplicationContext.clearValue("quiz.problem." + getUser());
 	}
 }
