 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 public class SeleniumSample
 {
 	public static void main(String args[])
 	{
 		WebDriver driver = new FirefoxDriver();
 		openQQSignUpPage(driver);
 		waitPageReady(driver);
 		setUpEmail(driver);
 		setUpNickName(driver);
 		setUpPassWord(driver);
 		setUpDateOfBirth(driver);
 		submitInfo(driver);

		closeBrowser(driver);
 	}
 
 	private static void openQQSignUpPage(WebDriver driver)
 	{
 		driver.get("http://zc.qq.com/en/index.html");
 	}
 
 	private static void waitPageReady(WebDriver driver)
 	{
 		(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By.id("other_email")));
 	}
 
 	private static void setUpEmail(WebDriver driver)
 	{
 		(driver.findElement(By.id("other_email"))).sendKeys("test@user.com");
 	}
 
 	private static void setUpNickName(WebDriver driver)
 	{
 		(driver.findElement(By.id("nick"))).sendKeys("nickname");
 	}
 
 	private static void setUpPassWord(WebDriver driver)
 	{
 		(driver.findElement(By.id("password"))).sendKeys("password123");
 		(driver.findElement(By.id("password_again"))).sendKeys("password123");
 	}
 
 	private static void setUpDateOfBirth(WebDriver driver)
 	{
 		WebElement year_value = driver.findElement(By.id("year_value"));
 		year_value.clear();
 		year_value.sendKeys("1990");

		(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By.id("month_value")));
 
 		WebElement month_value = driver.findElement(By.id("month_value"));
 		month_value.clear();
 		month_value.sendKeys("10");
 
 		(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By.id("day_value")));
 
 		WebElement day_value = driver.findElement(By.id("day_value"));
 		day_value.clear();
 		day_value.sendKeys("10");
 	}
 
 	private static void submitInfo(WebDriver driver)
 	{
 		driver.findElement(By.id("submit")).submit();
 	}
 
 	private static void closeBrowser(WebDriver driver)
 	{
 		driver.quit();
 	}
 }
