 package com.teamdev.projects.test.web.component.detailspanel.task;
 
 import com.teamdev.projects.test.web.component.element.Button;
 import com.teamdev.projects.test.web.component.element.InputBox;
 import org.openqa.selenium.WebElement;
 
 import static com.teamdev.projects.test.webdriver.WebDriverManager.getDriver;
 
 /**
  * @author Sergii Moroz
 */
 public class DetailsPanelForTask {
 
     private WebElement linkToTaskElement(){
         return  getDriver().findElement(Locators.DETAILS_PANEL).findElement(Locators.LINK_TO_TASK);
     }
 
     public InputBox panelTitle(){
        return new InputBox(Locators.DETAILS_PANEL, Locators.TITLE_CONTAINER, Locators.INPUT_BOX);
     }
 
     public Button linkToTask(){
         return new Button(Locators.DETAILS_PANEL, Locators.LINK_TO_TASK);
     }
 
     public Button infoTab(){
         return new Button(Locators.DETAILS_PANEL, Locators.INFO);
     }
 
     public Button emptyCommentsTab(){
         return new Button(Locators.DETAILS_PANEL, Locators.COMMENTS_EMPTY_TAB);
     }
 
     public Button CommentsTab(){
         return new Button(Locators.DETAILS_PANEL, Locators.COMMENTS_TAB_BODY);
     }
 
     public Button logTab(){
         return new Button (Locators.DETAILS_PANEL, Locators.LOG_TAB_BODY);
     }
 
 }
