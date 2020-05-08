 package cucumber.todo.pageobject;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.Keys;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.interactions.Actions;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 public class ToDoPage {
     private final int timeOutInSeconds = 5;
     private WebDriver driver;
 
     public ToDoPage(WebDriver driver) {
         this.driver = driver;
         driver.manage().timeouts().implicitlyWait(timeOutInSeconds, TimeUnit.SECONDS);
     }
 
     public void open(String url) {
         driver.get(url);
     }
 
     public void close() {
         driver.close();
     }
 
     public void fillInNewItem(String label) {
         findNewItemField().sendKeys(label);
     }
 
     public void pressReturnOnCurrentItem() {
         findCurrentInputField().sendKeys(Keys.RETURN);
     }
 
     private WebElement findNewItemField() {
         return driver.findElement(By.id("new-todo"));
     }
 
     private WebElement findCurrentInputField() {
         By selector = By.cssSelector("input:focus");
         wait(selector);
 
         return driver.findElement(selector);
     }
 
     public WebElement findItem(String label) {
         String xpath = String
                .format("//li[contains(.,'%s')]", label);
        ////label[contains(.,'%s')]
         By selector = By.xpath(xpath);
         wait(selector);
 
         return driver.findElement(selector);
     }
 
     public List<WebElement> findItems(String label) {
         String xpath = String
                 .format("//label[contains(., '%s')]/ancestor::li[1]", label);
 
         return driver.findElements(By.xpath(xpath));
     }
 
     public WebElement findByText(String text) {
         String xpath = String.format("//*[contains(., '%s')]", text);
         By selector = By.xpath(xpath);
 
         wait(selector);
 
         return driver.findElement(selector);
     }
 
     public WebElement findAndClickOnItemCheckbox(String label) {
         By by = By.cssSelector("input[type='checkbox']");
         wait(by);
         WebElement checkbox = findItem(label).findElement(by);
         checkbox.click();
         checkbox = findItem(label).findElement(by);
 
         return checkbox;
     }
 
     public void findAndEditItemField(String oldLabel, String newLabel) {
         WebElement editItemField = findEditItemField(oldLabel);
         editItemField.clear();
         editItemField.sendKeys(newLabel);
     }
 
     public void performDoubleClickOnItem(String label) {
         WebElement item = findItem(label);
         WebElement labelElement = item.findElement(By.tagName("label"));
 
         Actions actions = new Actions(driver);
         actions.doubleClick(labelElement).build().perform();
     }
 
     private WebElement findEditItemField(String oldLabel) {
         By by = By.cssSelector("input.edit[type='text']");
         wait(by);
 
         return findItem(oldLabel).findElement(by);
     }
 
     public void clickOnLink(String label) {
         driver.findElement(By.partialLinkText(label)).click();
     }
 
     private void wait(By selector) {
         WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds);
         wait.until(ExpectedConditions.visibilityOfElementLocated(selector));
     }
 
 }
