 package webdriverrunner.engine;
 
 import java.util.List;
import org.bouncycastle.crypto.RuntimeCryptoException;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import webdriverrunner.api.Browser;
 
 /**
  *
  * @author Witold Szczerba
  */
 public class WebDriverBrowser implements Browser {
     
     private final RemoteWebDriver webDriver;
 
     public WebDriverBrowser(RemoteWebDriver webDriver) {
         this.webDriver = webDriver;
     }
     
     @Override
     public WebElement find(String description, By id) {
         List<WebElement> elements = webDriver.findElements(id);
         if (elements.isEmpty()) {
            throw new RuntimeCryptoException("Cannot find " + description);
         }
         return elements.get(0);
     }
 
     @Override
     public String getCurrentUrl() {
         return webDriver.getCurrentUrl();
     }
 
     @Override
     public void goToUrl(String url) {
         webDriver.get(url);
     }
     
     @Override
     public void cleanupAfterTest() {
         webDriver.get("about:blank");
         webDriver.manage().deleteAllCookies();
     }
 
     public void exitQuietly() {
         try {
             webDriver.quit();
         } catch (Exception e) {
             // Quiet and no exceptions!
         }
     }
 }
