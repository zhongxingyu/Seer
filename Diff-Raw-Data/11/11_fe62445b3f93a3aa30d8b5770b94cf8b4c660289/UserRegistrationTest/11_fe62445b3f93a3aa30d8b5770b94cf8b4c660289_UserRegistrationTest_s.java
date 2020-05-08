 package com.forum.web.page.tests;
 
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.Select;
 import java.util.Random;
 
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 public class UserRegistrationTest extends FunctionalTestBase {
 
     private Random random = new Random();
    int randomNumber = random.nextInt();
 
 
     @Test
     public void shouldGoToRegistrationPage() {
         WebElement joinLink = browser.findElement(By.linkText("Join"));
         joinLink.click();
         browser.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id("register")));
         assertTrue(browser.getCurrentUrl().contains("/join"));
     }
 
     @Test
     public void shouldRegisterSuccessfully(){
         browser.open("/join");
         String userRegistrationTitle = browser.findElement(By.tagName("h1")).getText();
         assertThat(userRegistrationTitle, is("User Registration"));
         assertTrue(browser.getCurrentUrl().contains("/join"));
 
         WebElement usernameField = browser.findElement(By.id("username"));
        usernameField.sendKeys("Stephanie" + randomNumber);
         WebElement passwordField = browser.findElement(By.id("password"));
         passwordField.sendKeys("saxophone");
         WebElement nameField = browser.findElement(By.id("name"));
         nameField.sendKeys("Stephanie");
         WebElement emailField = browser.findElement(By.id("email"));
         emailField.sendKeys("sjacobs" + randomNumber + "@thoughtworks.com");
         Select countryField = new Select(browser.findElement(By.id("country")));
         countryField.selectByVisibleText("United States");
         WebElement genderField = browser.findElement(By.id("genderFemale"));
         genderField.click();
         WebElement ageRange = browser.findElement(By.id("ageRangeLessThan25"));
         ageRange.click();
         WebElement interestField = browser.findElement(By.id("interestMusic"));
         interestField.click();
         WebElement registerButton = browser.findElement(By.id("register"));
         registerButton.click();
 
         WebElement h1 = browser.waitFor(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
         assertThat(h1.getText(), is("Activity Wall"));
     }
 
     @Test
     public void shouldHavePasswordErrorWhenLessThan8() {
         browser.open("/join");
         WebElement passwordField = browser.findElement(By.id("password"));
         passwordField.sendKeys("1234567");
 
         WebElement signUp = browser.findElement(By.id("register"));
         signUp.click();
 
         browser.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id("passwordMsg")));
 
         String errorMsg = browser.findElement(By.id("passwordMsg")).getText();
         assertThat(errorMsg, is("Password should be at least 8 characters."));
     }
 
     @Test
     public void shouldDisplayErrorWhenEmailFormatInvalid() {
         browser.open("/join");
         WebElement emailField = browser.findElement(By.id("email"));
         emailField.sendKeys("123asdf");
 
         WebElement signUp = browser.findElement(By.id("register"));
         signUp.click();
 
         browser.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id("emailMsg")));
 
         String errorMsg = browser.findElement(By.id("emailMsg")).getText();
         assertThat(errorMsg, is("Please enter a valid email address."));
     }
 
     @Test
     public void shouldDisplayTermsAndConditions() {
         browser.open("/join");
         WebElement termsLink = browser.findElement(By.name("tos"));
         termsLink.click();
         WebElement TosTitle = browser.waitFor(ExpectedConditions.visibilityOfElementLocated(By.tagName("h3")));
         assertTrue(browser.getCurrentUrl().contains("/terms"));
         assertThat(TosTitle.getText(), is("The Forum Terms and Conditions"));
     }
 }
