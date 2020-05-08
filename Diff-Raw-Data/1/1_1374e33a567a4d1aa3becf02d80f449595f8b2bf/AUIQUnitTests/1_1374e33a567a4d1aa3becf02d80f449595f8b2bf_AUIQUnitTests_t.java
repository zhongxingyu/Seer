 package it.com.atlassian.aui.javascript.unitTests;
 
 import com.atlassian.webdriver.AtlassianWebDriver;
 import com.atlassian.webdriver.utils.JavaScriptUtils;
 import com.atlassian.webdriver.utils.element.ElementLocated;
 import com.thoughtworks.selenium.Wait;
 import org.junit.Assert;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 
 import java.util.List;
 
 public class AUIQUnitTests extends AUIWebDriverTestCase
 {
     @Test
     public void testWhenITypeUnitTests()
     {
         openQunitTestPage("whenitype");
         runQunitTests("WhenIType");
     }
 
     @Test
     public void testDialogUnitTests()
     {
         openQunitTestPage("dialog");
         runQunitTests("Dialog");
     }
 
     @Test
     public void testDropdownUnitTests()
     {
         openQunitTestPage("dropdown");
         runQunitTests("Dropdown");
     }
 
     @Test
     public void testFormatUnitTests()
     {
         openQunitTestPage("format");
         runQunitTests("Format");
     }
 
     @Test
     public void testFormsUnitTests()
     {
         openQunitTestPage("forms");
         runQunitTests("Forms");
 
     }
 
     @Test
     public void testInlineDialogUnitTests()
     {
         openQunitTestPage("inline-dialog");
         runQunitTests("Inline-Dialog");
     }
 
     @Test
     public void testMessagesUnitTests()
     {
         openQunitTestPage("messages");
         runQunitTests("Messages");
     }
 
     @Test
     public void testStalkerUnitTests()
     {
         openQunitTestPage("stalker");
         runQunitTests("Stalker");
     }
 
     @Test
     public void testTablesUnitTests()
     {
         openQunitTestPage("tables");
         runQunitTests("Tables");
     }
 
     @Test
     public void testTabsUnitTests()
     {
         openQunitTestPage("tabs");
         runQunitTests("Tabs");
     }
 
     @Test
     public void testToolbarUnitTests()
     {
         openQunitTestPage("toolbar");
         runQunitTests("Toolbar");
     }
 
     @Test
     public void testEventsUnitTests()
     {
         openQunitTestPage("events");
         runQunitTests("Events");
     }
 
     //HELPER FUNCTIONS
 
     //runs qunit tests on the page, component argument for reporting purposes only
     private void runQunitTests(String component)
     {
         AtlassianWebDriver.waitUntil(new ElementLocated(By.id("qunit-testresult")));
 
         //Reveal all failed tests
         clickAll(driver.findElements(By.cssSelector("li.fail strong")));
         List<WebElement> failedTests = driver.findElements(By.cssSelector("li.fail li.fail"));
 
         int numberOfFailedTests = failedTests.size();
 
         if (numberOfFailedTests != 0)
         {
             String failedTestsReport = "";
 
             for (int i = 0; i < failedTests.size(); i++)
             {
 
                 failedTestsReport = failedTestsReport + "FAILED! >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + failedTests.get(i).getText() + "\n";
             }
 
             Assert.fail("There are " + (numberOfFailedTests) + " failed unit tests for" + component + " \n\n" + failedTestsReport);
         }
     }
 
     //Opens a qunit test page for the specified component (assumes correct test file structure)
     private void openQunitTestPage(String component)
     {
         openTestPage("unit-tests/tests/" + component + "-unit-tests/" + component + "-unit-tests.html");
     }
 
     private void clickAll(List<WebElement> find){
         for(int i=0;i<find.size(); i++){
             find.get(i).click();
         }
     }
 }
