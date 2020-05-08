 package org.opencorrelate.ci.selenium;
 
 import java.util.List;
 import java.util.Set;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.Keyboard;
 import org.openqa.selenium.Keys;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.openqa.selenium.support.ui.FluentWait;
 
 import com.google.common.base.Predicate;
 
 
 
 /**
  * 
  * @author Presley H. Cannady, Jr. <revprez@opencorrelate.org>
  *
  */
 public abstract class TestWebDriver implements WebDriver {
 
 	
 	private WebDriver driver;
 	
 	public TestWebDriver(WebDriver driver) {
 		this.driver = driver;
 	}
 	
 	public void get(String url) {
 		driver.get(url);
 	}
 
 	public String getCurrentUrl() {
 		return driver.getCurrentUrl();
 	}
 
 	public String getTitle() {
 		return driver.getTitle();
 	}
 
 	public List<WebElement> findElements(By by) {
 		return driver.findElements(by);
 	}
 
 	public WebElement findElement(By by) {
 		return driver.findElement(by);
 	}
 
 	public String getPageSource() {
 		return driver.getPageSource();
 	}
 
 	public void close() {
 		driver.close();
 	}
 
 	public void quit() {
 		driver.quit();
 	}
 
 	public Set<String> getWindowHandles() {
 		return driver.getWindowHandles();
 	}
 
 	public String getWindowHandle() {
 		return driver.getWindowHandle();
 	}
 
 	public TargetLocator switchTo() {
 		return driver.switchTo();
 	}
 
 	public Navigation navigate() {
 		return driver.navigate();
 	}
 
 	public Options manage() {
 		return driver.manage();
 	}
 
 	
 	public Keyboard getKeyboard() {
 		WebDriver d = (driver instanceof TestWebDriver) ? ((TestWebDriver)driver).getDelegateDriver() : driver;
 		if (d instanceof RemoteWebDriver)
 			return ((RemoteWebDriver)d).getKeyboard();
 		else
 			return null;
 	}
 	
 	public WebDriver getDelegateDriver() {
 		return driver;
 	}
 	
 	/**
 	 * 
 	 * @param by : element locator
 	 * @return true if element is present, false if not
 	 */
 	public boolean isElementPresent(By by) {
 		try {
 			driver.findElement(by);
 			return true;
 		} catch (NoSuchElementException e){
 			return false;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param text : text to be found
 	 * @return true if text is present, false if not
 	 */
 	public boolean isTextPresent(String text) {
 		try {
			driver.findElement(By.xpath(String.format("//body[contains(text(),'%s)']")));
 			return true;
 		} catch (NoSuchElementException e){
 			return false;
 		}
 	}
 	
 	
 	public void inputAndValidate(final By invalid, final By valid, String input) {
 		WebDriver driver = getDelegateDriver();
 		(new FluentWait<WebDriver>(driver)).until(new Predicate<WebDriver>() {
 			public boolean apply(WebDriver driver) { return driver.findElement(invalid).isDisplayed(); }
 		});
 		
 		driver.findElement(invalid).clear();
 		driver.findElement(invalid).sendKeys(input);
 		if (driver instanceof RemoteWebDriver)
 			((RemoteWebDriver)driver).getKeyboard().pressKey(Keys.TAB);
 		
 		(new FluentWait<WebDriver>(driver)).until(new Predicate<WebDriver>() {
 			public boolean apply(WebDriver d) { return d.findElement(valid).isDisplayed(); }
 		});
 	}
 }
