 
 package com.myfitnesspal.qa.test.acceptance;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.PageFactory;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import com.myfitnesspal.qa.foundation.BasicTestCase;
 import com.myfitnesspal.qa.pages.user.AddCardioExercisePage;
 import com.myfitnesspal.qa.pages.user.AddCardioExercisePage.AddingType;
 import com.myfitnesspal.qa.pages.user.ExerciseDiaryPage;
 import com.myfitnesspal.qa.pages.user.LoginPage;
 import com.myfitnesspal.qa.pages.user.MyExercisesPage;
 import com.myfitnesspal.qa.utils.rerunner.RetryAnalyzer;
 
 public class CardioExerciseSearchAndAddTest extends BasicTestCase
 {
 
 	private LoginPage loginPage;
 
 	ExerciseDiaryPage exerciseDiaryPage;
 
 	MyExercisesPage myExercisesPage;
 
 	AddCardioExercisePage addCardioExercisePage;
 
 	@Test(groups = { "ui_acceptance" } , retryAnalyzer = RetryAnalyzer.class)
 	public void testCardioExerciseSearchAndAdd()
 	{
 		String expPreviouslySearchedExercises = "Pushing or pulling stroller with child";
 		String exercise2search = "football";
 
 		loginPage = PageFactory.initElements(driver, LoginPage.class);
 		loginPage.open();
 		loginPage.login(mfpUser.getLogin(), mfpUser.getPassword());
 
 		exerciseDiaryPage = loginPage.getTopMainMenu().clickExercise();
 		exerciseDiaryPage.initUrl(mfpUser.getLogin());
 		Assert.assertTrue(isPageOpened(exerciseDiaryPage), "Exercise Diary page wasn't opened");
 		addCardioExercisePage = exerciseDiaryPage.clickAddCardiovascularExercise();
 		Assert.assertTrue(isPageOpened(addCardioExercisePage), "Add Cardio Exercise page wasn't opened");
 
 		List<WebElement> previouslySearchedExercises = addCardioExercisePage.previouslySearchedExercises;
 		List<String> exerciseNames = new ArrayList<String>();
 		for (WebElement ex : previouslySearchedExercises)
 		{
 			exerciseNames.add(ex.getText());
 		}
 		Assert.assertTrue(exerciseNames.contains(expPreviouslySearchedExercises),
 				"Previosly searched exercises list doesn't contain expected value " + expPreviouslySearchedExercises);
 
 		/**************** Adding of new exercise test ****************/
 
 		addCardioExercisePage.searchExercise(exercise2search);
 		List<WebElement> searchedLinks = addCardioExercisePage.searchResults;
 		String firstSearchedLink = searchedLinks.get(0).getText();
 		click(firstSearchedLink, driver.findElement(By.linkText(firstSearchedLink)));
 		exerciseDiaryPage = addCardioExercisePage.addExercise(AddingType.HOW_LONG, "3");
		Assert.assertTrue(isElementPresent(firstSearchedLink, driver.findElement(By.linkText(firstSearchedLink))),
 				String.format("Link %s doesn't present in exercise diary page", firstSearchedLink));
		int elementsCount = driver.findElements(By.linkText(firstSearchedLink)).size();
 
 		// checking if added exercise is presented in Most Recent list
 		addCardioExercisePage = exerciseDiaryPage.clickAddCardiovascularExercise();
 		previouslySearchedExercises = addCardioExercisePage.previouslySearchedExercises;
 		exerciseNames = new ArrayList<String>();
 		for (WebElement ex : previouslySearchedExercises)
 		{
 			exerciseNames.add(ex.getText());
 		}
 		Assert.assertTrue(exerciseNames.contains(firstSearchedLink), "Previosly searched exercises list doesn't contain expected value "
 				+ firstSearchedLink);
 		exerciseDiaryPage = addCardioExercisePage.getTopMainMenu().getTopExerciseMenu().clickExerciseDiary();
 		click("remove" + firstSearchedLink,
 				driver.findElement(By.xpath(String.format(AddCardioExercisePage.IMG_REMOVE_EX_XPATH, firstSearchedLink))));
		Assert.assertTrue(driver.findElements(By.linkText(firstSearchedLink)).size() == (elementsCount - 1),
 				String.format("Link %s wasn't successfully deleted from exercise diary page", firstSearchedLink));
 	}
 
 }
