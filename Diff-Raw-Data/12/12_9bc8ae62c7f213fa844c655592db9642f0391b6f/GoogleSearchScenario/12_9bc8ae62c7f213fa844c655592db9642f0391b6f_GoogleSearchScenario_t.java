 package com.michalvich.cucumber.selenium.example;
 
 import cucumber.annotation.en.Given;
 import cucumber.annotation.en.Then;
 import cucumber.annotation.en.When;
import cucumber.runtime.PendingException;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 public class GoogleSearchScenario {
 
 	private WebDriver driver = new HtmlUnitDriver();
 
	@Given("^the page is open \"([^\"]*)\"$")
 	public void the_page_is_open(String page) throws Throwable {
 		driver.get(page);
 	}
 
	@When("^I search for \"([^\"]*)\"$")
 	public void I_search_for(String search) throws Throwable {
 		WebElement element = driver.findElement(By.name("q"));
 		element.sendKeys(search);
 		element.submit();
 	}
 
	@Then("^a browser title should contains \"([^\"]*)\"$")
 	public void a_browser_title_should_contains(String text) throws Throwable {
 		assertTrue(driver.getTitle().contains(text));
 	}

 }
