 
 package com.myfitnesspal.qa.test.acceptance;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.PageFactory;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import com.myfitnesspal.qa.foundation.BasicTestCase;
 import com.myfitnesspal.qa.pages.user.AddStrengthExercisePage;
 import com.myfitnesspal.qa.pages.user.ExerciseDiaryPage;
 import com.myfitnesspal.qa.pages.user.LoginPage;
import com.myfitnesspal.qa.pages.user.MyExercisesPage;
 
 public class StrengthExerciseSearchAndAddTest extends BasicTestCase
 {
 
 	private LoginPage loginPage;
 
	ExerciseDiaryPage exerciseDiaryPage;
 
	MyExercisesPage myExercisesPage;

	AddStrengthExercisePage addStrengthExercisePage;
 
 	@Test(groups = { "ui_acceptance" })
 	public void testStrengthExerciseSearchAndAdd()
 	{
 		String key2search = "push";
 		String expectedResults = "Push Ups (push-ups); Triceps Push Down";
 
 		loginPage = PageFactory.initElements(driver, LoginPage.class);
 		loginPage.open();
 		loginPage.login(mfpUser1.getLogin(), mfpUser1.getPassword());
 
 		exerciseDiaryPage = loginPage.getTopMainMenu().clickExercise();
 		exerciseDiaryPage.initUrl(mfpUser1.getLogin());
 		Assert.assertTrue(isPageOpened(exerciseDiaryPage), "Exercise Diary page wasn't opened");
 		addStrengthExercisePage = exerciseDiaryPage.clickAddStrengthTraining();
 		Assert.assertTrue(isPageOpened(addStrengthExercisePage), "Add Strength Exercise page wasn't opened");
 		addStrengthExercisePage.searchExercise(key2search);
 
 		List<WebElement> searchedExercises = addStrengthExercisePage.searchResults;
 		List<String> exerciseNames = new ArrayList<String>();
 		for (WebElement ex : searchedExercises)
 		{
 			exerciseNames.add(ex.getText());
 		}
 		boolean result = true;
 		String[] expectedNames = expectedResults.split("; ");
 		for (String expectedName : expectedNames)
 		{
 			if (!exerciseNames.contains(expectedName))
 			{
 				result = false;
 			}
 		}
 		Assert.assertTrue(result, "All expected exercise names weren't presented in searched list");
 	}
 }
