 package com.BudgetBoss.tests;
 
 import java.io.File;
 
 import org.junit.Test;
 
 import com.BudgetBoss.main.BudgetFinder;
 
 public class BudgetFinderTests {
 
 	@Test
 	public void testFindingBudgets() {
 		//Given: some saved budgets
 		//When: A user wants to check what, if any, budgets are saved
 		//Then: A list of Budgets is compiled
 		File[] foundBudgets;
 		BudgetFinder finder = new BudgetFinder();
		foundBudgets = finder.findBudgets("/home/visionary/Programming/BudgetBoss/SavedBudgets/");
 		//And: Prints them to the console
 		finder.printFoundBudgets(foundBudgets);
 	}
 }
