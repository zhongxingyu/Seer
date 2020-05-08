 package com.VSSBudgetBoss.main;
 
 import com.VSSBudgetBoss.budget.*;
 import com.VSSBudgetBoss.cli.*;
 import com.VSSBudgetBoss.fileops.Salvation;
 
 public class MainMenu implements MenuOption, MasterMenu{
 	
 	private Budget currentBudget;
 	private int currentMenuChoice;
 
 	public MainMenu(Budget currentBudget){
 		this.currentBudget = currentBudget;
 	}
 		
 	@Override
 	public int getNumberOfOptions(){
 		return menuOptions.length;
 	}
 	
 	private MenuOption[] menuOptions = new MenuOption[]{
 		new MenuOption(){public void chooseOption() {System.out.println(currentBudget.toString());}},
 		new MenuOption(){public void chooseOption() {startEditor();}},
 		new MenuOption(){public void chooseOption() {choseToSave();}},
 		new MenuOption(){public void chooseOption() {choseToOpen();}},
 		new MenuOption(){public void chooseOption() {choseToCreate();}},
 		new MenuOption(){public void chooseOption() {choseToClose();}}
 	};
 	
 	private void startEditor(){
 		BudgetEditor editor = new BudgetEditor(currentBudget);
 		while(editor.stillEditingBudget())
 			editor.displayEditorMainMenu();
 	}
 	
 	public void displayMainMenu(){
 		Prompter.printPrompt("mainMenu");
		System.out.println("Currently opened budget: " + currentBudget.getName());
 		Prompter.printPrompt("mainMenuChoices");
 		InputValidator validator = new InputValidator();
 		String userInput = Listener.getInput();
 		while(validator.menuChoiceIsInvalid(userInput, this))
 			userInput = Listener.getInput();
 		currentMenuChoice = Integer.valueOf(userInput);
 		chooseOption();			
 	}
 
 	private void choseToSave(){
 		Salvation savior = new Salvation();
 		savior.saveBudget(currentBudget.getName(), currentBudget);
 	}
 	
 	private void choseToOpen(){
 		BudgetBoss.loadSavedBudget();
 		BudgetBoss.endNeedNewBudget();
 	}
 	
 	private void choseToClose(){
 		BudgetBoss.doneUsingBudgetBoss();
 		BudgetBoss.endLoadSavedBudget();
 		BudgetBoss.endNeedNewBudget();
 	}
 	
 	private void choseToCreate(){
 		BudgetBoss.endLoadSavedBudget();
 		BudgetBoss.needNewBudget();
 	}
 	
 	public void chooseOption(){
 		int index = (currentMenuChoice -1);
 		menuOptions[index].chooseOption();
 	}
 }
