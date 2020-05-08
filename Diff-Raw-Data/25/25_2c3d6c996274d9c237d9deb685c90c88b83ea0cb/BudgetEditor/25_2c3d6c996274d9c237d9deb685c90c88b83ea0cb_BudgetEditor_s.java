 package com.VSSBudgetBoss.budget;
 
 import com.VSSBudgetBoss.cli.*;
 import com.VSSBudgetBoss.main.*;
 
 public class BudgetEditor implements MenuOption, MasterMenu{
 	
 	private Budget toEdit;
 	private int currentMenuChoice;
 	private boolean stillEditing = true;
 	private InputValidator validator = new InputValidator();
 
 	public BudgetEditor(Budget toEdit){
 		this.toEdit = toEdit;
 	}
 	
 	public boolean stillEditingBudget(){
 		return stillEditing;
 	}
 	
 	@Override
 	public int getNumberOfOptions() {
 		return menuOptions.length;
 	}
 	
 	private MenuOption[] menuOptions = new MenuOption[] {
 		new MenuOption() {public void chooseOption() {System.out.println(toEdit.toString());}},
 		new MenuOption() {public void chooseOption() {getNewName();}},
 		new MenuOption() {public void chooseOption() {getNewStartDate();}},
 		new MenuOption() {public void chooseOption() {getNewEndDate();}},
 		new MenuOption() {public void chooseOption() {stillEditing = false;}}
 	};
 	
 	private void getNewName(){
 		Prompter.printPrompt("getNewName");
 		String userInput = Listener.getInput();
 		toEdit.setName(userInput);
 	}
 	
 	private void getNewStartDate(){
 		Prompter.printPrompt("getNewStartDate");
 		String userInput = Listener.getInput();
 		while(validator.dateIsInvalid(userInput))
 			userInput = Listener.getInput();
 		toEdit.setStartDate(userInput);
 	}
 	
 	private void getNewEndDate(){
 		Prompter.printPrompt("getNewEndDate");
 		String userInput = Listener.getInput();
 		while(validator.dateIsInvalid(userInput))
 			userInput = Listener.getInput();
 		toEdit.setEndDate(userInput);
 	}
 	
 	public void displayEditorMainMenu(){
 		Prompter.printPrompt("editorMainMenu");
 		InputValidator validator = new InputValidator();
 		String userInput = Listener.getInput();
		if(validator.menuChoiceIsInvalid(userInput, this)){
			currentMenuChoice = Integer.valueOf(userInput);
			chooseOption();
		}
 	}
 	
 	public void chooseOption(){
 		int index = (currentMenuChoice -1);
 		menuOptions[index].chooseOption();
 	}
 }
