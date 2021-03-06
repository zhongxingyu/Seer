 package com.alexecollins.releasemanager.web;
 
 import com.alexecollins.web.ExamplesLoader;
 import org.junit.Test;
 import org.openqa.selenium.WebElement;
 import org.seleniumhq.selenium.fluent.FluentMatcher;
 
 import static org.openqa.selenium.By.linkText;
 import static org.openqa.selenium.By.name;
 
 /**
  * @author alexec (alex.e.c@gmail.com)
  */
 public class ReleaseIT extends AbstractIT {
 
 	@Test
 	public void createARelease() throws Exception {
 		newGoodRelease();
 	}
 
 	private String newGoodRelease() {
 		return verifyExists(newRelease("tomorrow"));
 	}
 
 	private String verifyExists(final String name) {
 		fluent.h1().getText().shouldBe(name);
 		return name;
 	}
 
 	@Test
 	public void givenABadDateWhenICreateAReleaseThenIGetAnError() throws Exception {
 		newRelease("balls date");
 		fluent.h1().getText().shouldBe("Error");
 	}
 
 	@Test
 	public void givenAnExistingReleaseWhenItIsUpdatedThereAreNoErrors() throws Exception {
 		final String name = newGoodRelease();
 		fluent.link(linkText("Releases")).click();
 		fluent.link(linkText(name)).click();
 		fluent.link(linkText("Edit")).click();
 		fluent.input(name("when")).clearField().sendKeys("wednesday");
 		fluent.button().click();
 		verifyExists(name);
 	}
 
 	@Test
 	public void givenANewReleaseWhenWeRemoveItThenWeAreOnTheReleasePage() throws Exception {
 		final String name = newGoodRelease();
		fluent.link(linkText("Releases")).click();
 		fluent.trs().first(new FluentMatcher() {
 			@Override
 			public boolean matches(WebElement webElement) {
				try {
					return webElement.findElement(linkText(name)).isDisplayed();
				} catch (Exception ignored) {
					return false;
				}
 			}
		}).input().click();
 		fluent.h1().getText().shouldBe("Releases");
 	}
 
 	private String newRelease(String when) {
 		fluent.link(linkText("Releases")).click();
 		fluent.link(linkText("Create")).click();
 		final String name = "Test Release " + System.currentTimeMillis();
 		fluent.input(name("name")).sendKeys(name);
 		fluent.input(name("when")).sendKeys(when);
 		fluent.textarea(name("desc")).sendKeys("Test Release Desc\n---\n" + ExamplesLoader.LIPSUM);
 		fluent.button().click();
 		return name;
 	}
 }
