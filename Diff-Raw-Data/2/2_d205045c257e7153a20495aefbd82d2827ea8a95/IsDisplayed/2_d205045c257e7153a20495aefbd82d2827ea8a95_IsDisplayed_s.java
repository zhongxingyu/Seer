 package com.woselenium;
 
 
 import org.hamcrest.Description;
 import org.hamcrest.TypeSafeMatcher;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.WebElement;
 
 public class IsDisplayed extends TypeSafeMatcher<WebElement> {
 
 	@Override
 	public boolean matchesSafely(WebElement element) {
 		try {
 			return element.isDisplayed();
 		} catch (NoSuchElementException e) {
 			return false;
 		}
 	}
 
 	public void describeTo(Description description) {
		description.appendText("present");
 	}
 	
 	public static IsDisplayed present() {
 		return new IsDisplayed();
 	}
 
 }
