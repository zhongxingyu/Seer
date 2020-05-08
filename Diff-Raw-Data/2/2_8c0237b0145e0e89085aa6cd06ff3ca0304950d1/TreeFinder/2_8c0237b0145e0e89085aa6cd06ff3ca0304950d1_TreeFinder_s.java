 package pl.bmajsak.github.pages;
 
 import static org.fest.assertions.Assertions.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.Keyboard;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.events.EventFiringWebDriver;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 
 public class TreeFinder {
 
     private static final int POOL_EVERY_MS = 15;
 
     private static final int SECONDS_TO_WAIT = 2;
 
    private static final String TREE_FINDER_RESULTS = "//table[@class='tree-browser']/tbody[@class='js-results-list']//td[2]/a[@class='js-slide-to']";
     
     private final EventFiringWebDriver driver;
 
     public TreeFinder(EventFiringWebDriver driver) {
         this.driver = driver;
     }
     
     public void type(String search) {
         Keyboard keyboard = driver.getKeyboard();
         keyboard.sendKeys(search);
     }
     
     public void contains(String element) {
         final List<String> foundFiles = new ArrayList<String>();
         ExpectedCondition<Boolean> treeFinderListIsNotEmpty = new ExpectedCondition<Boolean>() {
 
             public Boolean apply(WebDriver driver) {
                 List<WebElement> treeFinderResults = driver.findElements(By.xpath(TREE_FINDER_RESULTS ));
                 foundFiles.clear();
                 foundFiles.addAll(Lists.transform(treeFinderResults, new ExtractListedFiles()));
                 return !foundFiles.isEmpty();
             }
             
         };
         WebDriverWait wait = new WebDriverWait(driver, SECONDS_TO_WAIT, POOL_EVERY_MS);
         wait.until(treeFinderListIsNotEmpty);
         
         assertThat(foundFiles).contains(element);
     }
     
     private final class ExtractListedFiles implements Function<WebElement, String> {
         public String apply(WebElement from) {
             return from.getText();
         }
     }
     
 }
