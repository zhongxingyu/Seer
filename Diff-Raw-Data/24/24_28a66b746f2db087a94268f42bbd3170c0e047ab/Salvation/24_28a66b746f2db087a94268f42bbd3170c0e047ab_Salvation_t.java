 package com.VSSBudgetBoss.fileops;
 
 import java.io.*;
 import com.VSSBudgetBoss.budget.Budget;
 import com.VSSBudgetBoss.cli.*;
 import com.VSSBudgetBoss.main.BudgetBoss;
 
 public class Salvation {
 	
 	public void autoSave(String fileName, Budget budget, String pathToSalvation){
 		InputValidator validator = new InputValidator();
 		InputListener listener = new InputListener();
 		System.out.println("Save in " + pathToSalvation + "? (y/n)");
 		String validatedInput = validator.inputIsEitherYOrN(listener.listenForInput());
 		if(validatedInput.equals("y"))
 			writeBudgetToDisk(fileName, budget, pathToSalvation);
 		else if(validatedInput.equals("n")){
 			BudgetBoss.printPrompt("getSaveDirectoryPath");
 			pathToSalvation = getUserSaveDirectory(pathToSalvation);
 			writeBudgetToDisk(fileName, budget, pathToSalvation);		
 		}
 	}
 	
 	private String getUserSaveDirectory(String defaultDirectory){
 		String userPath = "ERROR";
 		while(userPath.equals("ERROR")){
 			InputValidator validator = new InputValidator();
 			InputListener listener = new InputListener();
 			userPath = listener.listenForInput();
 			userPath = validator.defaultDirectoryCheck(userPath, defaultDirectory);
 		}
 		return userPath;				
 	}
 
 	private void writeBudgetToDisk(String fileName, Budget budget, String pathToSalvation){
 			try{
 				FileOutputStream autoSave = new FileOutputStream(pathToSalvation + fileName + ".bgt");
 				ObjectOutputStream saveOutput = new ObjectOutputStream(autoSave);
 				saveOutput.writeObject(budget);
 				saveOutput.close();
 				BudgetBoss.printPrompt("adios");
 			}catch(Exception ex){
 				ex.printStackTrace();
 			}
 		}
 	}
 
