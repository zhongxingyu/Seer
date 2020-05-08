 package com.VSSBudgetBoss.cli;
 
 import java.io.File;
 
 import com.VSSBudgetBoss.main.BudgetBoss;
 
 public class InputValidator {
 		
 	public String inputIsEitherYOrN(String toCheck){
 		if(toCheck.equals("y") || toCheck.equals("Y"))
 			return toCheck.toLowerCase();
 		else if(toCheck.equals("n") || toCheck.equals("N"))
 			return toCheck.toLowerCase();
 		else
 			return "invalidEntry";
 	}
 	
 	public boolean validateUserPath(String toCheck){
 		File directory = new File(toCheck);
		if(directory.isDirectory())
			return true;
 		else
 			return false;
 	}
 	
 	public String defaultDirectoryCheck(String toCheck, String defaultDirectory){
 		if(toCheck.equals("y") || toCheck.equals("Y"))
 			return defaultDirectory;
 		else if(validateUserPath(toCheck)){
 			BudgetBoss.setDefaultDirectory(toCheck);
 			return toCheck;
 		}
 		else{
 			BudgetBoss.printPrompt("badPathInput");
 			System.out.println("The default directory is " + defaultDirectory);
 			return "ERROR";
 		}
 	}
 	
 	public boolean validateBudgetSelection(String toCheck, File[] foundBudgets){
 		try{
 			Integer.parseInt(toCheck);
 		}catch(NumberFormatException e){
 			BudgetBoss.printPrompt("notEvenANumberGenius");
 			return false;
 		}
 		int highestSelection = foundBudgets.length;
 		int userSelection = Integer.valueOf(toCheck);
 		if(userSelection > highestSelection){
 			BudgetBoss.printPrompt("thatsNotAChoice");
 			return false;
 		}
 		else
 			return true;
 	}
 }
 
