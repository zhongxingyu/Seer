 package com.teamdev.projects.test.web.component.detailspanel.task;
 
 import com.teamdev.projects.test.util.Elements;
 import com.teamdev.projects.test.web.component.AbstractComponent;
 import com.teamdev.projects.test.web.component.element.Button;
import com.teamdev.projects.test.web.component.element.Label;
 import org.openqa.selenium.By;
 
 /**
  * @author Sergii Moroz
 */
 public class DetailsPanelForTask extends AbstractComponent {
 
     public DetailsPanelForTask(By... byExpressions) {
         super(byExpressions);
     }
 
     private Button linkToTaskElement(){
         return new Button(Elements.addBy(getByExpressions(), Locators.LINK_TO_TASK));
     }
 
    public Label panelTitle(){
        return new Label(Elements.addBy(getByExpressions(), Locators.TITLE_CONTAINER, Locators.INPUT_BOX));
     }
 
     public Button linkToTask(){
         return new Button(Elements.addBy(getByExpressions(), Locators.LINK_TO_TASK));
     }
 
     public Button infoTab(){
         return new Button(Elements.addBy(getByExpressions(), Locators.INFO));
     }
 
     public Button emptyCommentsTab(){
         return new Button(Elements.addBy(getByExpressions(), Locators.COMMENTS_EMPTY_TAB));
     }
 
     public Button CommentsTab(){
         return new Button(Elements.addBy(getByExpressions(), Locators.COMMENTS_TAB_BODY));
     }
 
     public Button logTab(){
         return new Button (Elements.addBy(getByExpressions(), Locators.LOG_TAB_BODY));
     }
 
 }
