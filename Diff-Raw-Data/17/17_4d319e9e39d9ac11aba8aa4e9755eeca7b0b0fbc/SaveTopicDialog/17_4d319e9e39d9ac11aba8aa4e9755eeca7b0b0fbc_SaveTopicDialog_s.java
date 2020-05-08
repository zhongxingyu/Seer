 package org.jboss.pressgang.ccms.page.topic;
 
 import lombok.extern.slf4j.Slf4j;
 import org.jboss.pressgang.ccms.page.AbstractPage;
 import org.openqa.selenium.Alert;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.FindBy;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static org.jboss.pressgang.ccms.util.Constants.THIRTY_SEC;
 
 /**
  * @author kamiller@redhat.com (Katie Miller)
  */
 @Slf4j
 public class SaveTopicDialog<T extends CreateTopicPage> extends AbstractPage {
 
     private T currentPane;
 
     @FindBy(id = "OKSaveDialog")
     private WebElement okButton;
 
     @FindBy(id = "MessageSaveDialog")
     private WebElement messageInput;
 
     @FindBy(id = "UsernameSaveDialog")
     private WebElement userInput;
 
     @FindBy(id = "MinorChangeSaveDialog")
     private WebElement minorChangeRadioButton;
 
     @FindBy(id = "MajorChangeSaveDialog")
     private WebElement majorChangeRadioButton;
 
     public SaveTopicDialog(WebDriver driver, T currentPane) {
         super(driver);
         this.currentPane = currentPane;
     }
 
     public SaveTopicDialog inputMessage(String message) {
         messageInput.clear();
         messageInput.sendKeys(message);
         return this;
     }
 
     public SaveTopicDialog inputUser(String user) {
         userInput.clear();
         userInput.sendKeys(user);
         return this;
     }
 
     public SaveTopicDialog selectChangePriority(boolean isMajorChange) {
         if (isMajorChange) {
             majorChangeRadioButton.click();
         } else {
             minorChangeRadioButton.click();
         }
         return this;
     }
 
     @SuppressWarnings("unchecked")
     public T clickOk() {
         okButton.click();
         Alert alert = new WebDriverWait(getDriver(), THIRTY_SEC).until(ExpectedConditions.alertIsPresent());
         if (alert == null) {
             log.warn("Alert expected but not found");
         }
         assert (alert != null);
        Matcher matcher = Pattern.compile("[0-9]+$").matcher(alert.getText());
         String topicId = "";
        if (!matcher.matches()) {
            log.warn("Could not find new Topic ID");
         } else {
             topicId = matcher.group();
         }
         alert.accept();
         currentPane.setLastCreatedTopicId(topicId);
         return currentPane;
     }
 }
