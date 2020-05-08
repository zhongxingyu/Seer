 package com.VSSBudgetBoss.main;
 
 import java.util.ResourceBundle;
 import com.VSSBudgetBoss.budget.Budget;
 import com.VSSBudgetBoss.fileops.*;
 
 public class BudgetBoss {
 	
 	static Opener opener = new Opener();
 	static TheCreator god = new TheCreator();
 	static Budget currentBudget = null;
 	static String defaultDirectory;
 	
 	public static ResourceBundle cliOutput = ResourceBundle.getBundle("cliOutput");
 	
 	public static void printPrompt(String toGet){
 		System.out.println(cliOutput.getString(toGet));
 	}
 	
 	public static void setDefaultDirectory(String newDefaultDirectory){
 		defaultDirectory = newDefaultDirectory;
 	}
 	
 	public static String getDefaultDirectory(){
 		return defaultDirectory;
 	}
 
 	public static void setCurrentBudget(Budget newBudget){
 		currentBudget = newBudget;
 	}
 		
 	public static void main(String args[]){
 		Salvation savior = new Salvation();
 		String currentUser = System.getProperty("user.name");
 		defaultDirectory = "/home/" + currentUser + "/Documents/";
 		
 		System.out.println(cliOutput.getString("welcome"));
 		
 		while(opener.promptNeedsToClear())
 				opener.askToOpenBudget(defaultDirectory);
 		
 		while(TheCreator.isSlackingOnFinances())
 			god.bestMakeABudgetNow();
 		
 		try{	
 			savior.autoSave(currentBudget.getName(), currentBudget, defaultDirectory);
 		}catch (NullPointerException e){
 			System.out.println(cliOutput.getString("noBudgetToSave"));
			
 		}
 	}
 }
 
 
