 package com.sdl.selenium.web.button;
 
 import com.extjs.selenium.Utils;
 import com.sdl.bootstrap.button.RunExe;
 import com.sdl.selenium.web.WebLocator;
 import org.apache.log4j.Logger;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.interactions.Actions;
 
 import java.awt.*;
 import java.awt.event.KeyEvent;
 
 public class SelectFilesHandler {
 
     private static final Logger logger = Logger.getLogger(SelectFilesHandler.class);
     private WebLocator buttonElement;
 
     public SelectFilesHandler() {
 
     }
 
     public SelectFilesHandler(WebLocator buttonElement) {
         setButtonElement(buttonElement);
     }
 
     public WebLocator getButtonElement() {
         return buttonElement;
     }
 
     public void setButtonElement(WebLocator buttonElement) {
         this.buttonElement = buttonElement;
     }
 
     public void browse(String filePath) {
         logger.info("browse filePath : " + filePath);
         openBrowseWindow();
         selectFiles(filePath);
     }
 
     /**
      * Upload file with AutoIT exe
      * Use only this: button.browseWithAutoIT(new String[] {"C:\\upload.exe", "C:\\text.txt"}, "Open");
      *
      * @param filePath   path to upload.exe
      * @param uploadWindowName upload window name
     * @return true or false
      */
     public boolean browseWithAutoIT(String[] filePath, String uploadWindowName) {
         openBrowseWindow();
        return RunExe.getInstance().upload(filePath, uploadWindowName);
     }
 
     /**
      * Upload file with AutoIT exe
      * Use only this: button.browseWithAutoIT(new String[] {"C:\\upload.exe", "C:\\text.txt"});
      */
     public boolean browseWithAutoIT(String[] filePath) {
         return RunExe.getInstance().upload(filePath);
     }
 
     public boolean isElementPresent() {
         return getButtonElement().isElementPresent();
     }
 
     public void openBrowseWindow() {
         WebDriver driver = WebLocator.getDriver();
         driver.switchTo().window(driver.getWindowHandle()); // TODO is not ready 100% (need to focus on browser)
         buttonElement.focus();
 //        buttonElement.sendKeys(Keys.TAB);
         Actions builder = new Actions(driver);
         builder.moveToElement(buttonElement.currentElement).build().perform();
         builder.click().build().perform();
         driver.switchTo().defaultContent();
     }
 
     public void selectFiles(String path) {
         Robot robot = null;
         try {
             robot = new Robot();
         } catch (AWTException e) {
             logger.error(e);
         }
 
         writeFileName(path, robot);
         // press enter to close Open dialog
         pressEnter(robot);
     }
 
     public void downloadFiles(String path) {
         Robot robot = null;
         try {
             robot = new Robot();
         } catch (AWTException e) {
             logger.error(e);
         }
 
         pressEnter(robot); // open window
 
         writeFileName(path, robot);
         // press enter to close Open dialog
         pressEnter(robot);
     }
 
     private void pressEnter(Robot robot) {
         robot.keyPress(KeyEvent.VK_ENTER);
         logger.debug("keyPress VK_ENTER");
         robot.keyRelease(KeyEvent.VK_ENTER);
         logger.debug("keyRelease VK_ENTER");
         Utils.sleep(1000);
     }
 
     private void writeFileName(String filePath, Robot robot) {
         logger.info("path written " + filePath);
         char[] pathChars = filePath.toUpperCase().toCharArray();
         for (char c : pathChars) {
             if (c == '\\') {
                 robot.keyPress(KeyEvent.VK_BACK_SLASH);
                 robot.keyRelease(KeyEvent.VK_BACK_SLASH);
             } else if (c == ':') {
                 robot.keyPress(KeyEvent.VK_SHIFT);
                 robot.keyPress(KeyEvent.VK_SEMICOLON);
                 robot.keyRelease(KeyEvent.VK_SEMICOLON);
                 robot.keyRelease(KeyEvent.VK_SHIFT);
             } else if (c == '_') {
                 robot.keyPress(KeyEvent.VK_SHIFT);
                 robot.keyPress(KeyEvent.VK_MINUS);
                 robot.keyRelease(KeyEvent.VK_MINUS);
                 robot.keyRelease(KeyEvent.VK_SHIFT);
             } else {
                 robot.keyPress(c);
                 robot.keyRelease(c);
             }
         }
         Utils.sleep(500);
     }
 }
