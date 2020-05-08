 package com.teamdev.projects.test.web.component.modaldialog;
 
 import com.teamdev.projects.test.data.AreaData;
 import com.teamdev.projects.test.data.ProjectData;
 import com.teamdev.projects.test.util.Elements;
 import com.teamdev.projects.test.web.component.AbstractComponent;
 import com.teamdev.projects.test.web.component.element.AutoCompleteList;
 import com.teamdev.projects.test.web.component.element.Button;
 import com.teamdev.projects.test.web.component.element.InputBox;
 import com.teamdev.projects.test.web.component.element.Link;
 import org.openqa.selenium.By;
 
 import static com.teamdev.projects.test.webdriver.WebDriverManager.getDriver;
 
 /**
  * @author Alexander Orlov
  */
 public class CreateTaskModalDialog extends AbstractComponent {
 
     public CreateTaskModalDialog(By... byExpressions) {
         super(byExpressions);
     }
 
     public InputBox nameInputBox() {
         return new InputBox(Locators.TASK_NAME_INPUT, Locators.INPUT_BOX);
     }
 
     public InputBox notesTextArea() {
         return new InputBox(Locators.TASK_NOTES_INPUT, Locators.TEXT_AREA);
     }
 
     public InputBox tagsInputBox() {
        return new InputBox(Locators.TAGS_INPUT);
     }
 
     public InputBox assigneeInputBox() {
         return new InputBox(Locators.TASK_ASSIGNEE_INPUT, Locators.INPUT_BOX);
     }
 
     public InputBox estimateInputBox() {
         return new InputBox(Locators.TASK_ESTIMATE_INPUT, Locators.INPUT_BOX);
     }
 
     public InputBox dueByInputBox() {
         return new InputBox(Locators.TASK_DUE_BY, Locators.INPUT_BOX);
     }
 
     public InputBox scheduleInputBox() {
         return new InputBox(Locators.TASK_SCHEDULE, Locators.INPUT_BOX);
     }
 
     public Button createButton() {
         return new Button(Locators.TASK_CREATE_BUTTON);
     }
 
     public Button dropDownToSelectAreaOrProject() {
         return new Button(Locators.TASK_DROP_LINK_FOR_PROJECT_AREAS_LIST);
     }
 
     public Link areaInDropDownList(AreaData areaData) {
         return new Link(Locators.TASK_DROP_SECTION,
                 By.linkText(areaData.getName()));
     }
 
     public Link projectInDropDownList(ProjectData projectData) {
         if (projectData.getAreaOfResponsibility().isEmpty() || projectData.getAreaOfResponsibility()==""){
         return new Link(Locators.TASK_DROP_SECTION,
                 By.linkText(projectData.getShortName()));
         }
         else
         {
             return new Link(Locators.TASK_DROP_SECTION,
                     By.linkText(projectData.getAreaOfResponsibility() + ": " + projectData.getShortName()));
         }
     }
 
     public boolean isClosed() {
         return !getDriver().findElement(Locators.MODAL_WINDOW).isDisplayed();
     }
 
     public AutoCompleteList assigneeDropdown(){
         return new AutoCompleteList(Locators.AUTO_COMPLETE_LIST);
     }
 
 
         public Button closeButton() {
             return new Button(Elements.addBy(getByExpressions(), Locators.CLOSE_BUTTON));
         }
 }
