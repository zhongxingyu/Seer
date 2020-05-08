 package com.teamdev.projects.test.screenshots;
 
 import com.teamdev.projects.test.SingleAccountTest;
 import com.teamdev.projects.test.data.UserData;
 import com.teamdev.projects.test.web.businessflow.ProjectFlow;
 import com.teamdev.projects.test.web.businessflow.TaskFlow;
 
 import java.util.Map;
 
 import static com.teamdev.projects.test.webdriver.WebDriverManager.stopDriver;
 
 /**
  * @author Dmitry Kalmetyev
  */
 class ManagingTasksScreenshots extends SingleAccountTest {
 
     private UserData secondUser;
     private Map<String, TaskFlow> tasks;
 
     public ManagingTasksScreenshots(Map<String, ProjectFlow> projects, UserData userData, Map<String, TaskFlow> tasks){
         this.secondUser = userData;
         this.tasks=tasks;
         generalFlow = setup("1024,700");
         searchScreenshot();
         stopDriver();
         generalFlow=setup("1024,850");
 
         //Collapse area section to reduce screenshot height
         generalFlow.getAppPage().menuPanel().areasSection().title().click();
         projects.get("Image Editor").clickTodayFocus();
 
         taskDetailsButtonsScreenshot();
         taskNotesEditingScreenshot();
         taskEditing();
         taskCompletion();
         stopDriver();
 
     }
 
 
     private void searchScreenshot() {
         //Collapse project and area sections to reduce screenshot height
         generalFlow.getAppPage().menuPanel().projectsSection().title().click();
         generalFlow.getAppPage().menuPanel().areasSection().title().click();
         generalFlow.getAppPage().header().search("name:update to:"+secondUser.getEmail());
         try {
             Thread.sleep(2000);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         Image.takeScreenshot("Search");
         Image.crop("Search", "Search_Crop", 193, 37, 471, 43);
         Image.resize("Search", "Search_Small");
         Image.svgToPng("Managing_Tasks_Search");
         generalFlow.getAppPage().menuPanel().personalFocus().today().click();
         //Expand project and area sections back
         generalFlow.getAppPage().menuPanel().projectsSection().title().click();
         generalFlow.getAppPage().menuPanel().areasSection().title().click();
 
     }
 
     private void taskDetailsButtonsScreenshot() {
 
        tasks.get("Check unit tests code coverage").select();
         Image.takeScreenshot("Task_Details_Buttons");
         Image.resize("Task_Details_Buttons", "Task_Details_Buttons_Small");
         Image.crop("Task_Details_Buttons", "Task_Details_Buttons_Crop", 654, 724, 125, 44);
         Image.svgToPng("Managing_Tasks_Task_Details_Buttons");
        tasks.get("Check unit tests code coverage").deselect();
 
     }
 
     private void taskNotesEditingScreenshot(){
         tasks.get("Check unit tests code coverage").select();
         tasks.get("Check unit tests code coverage").addNote("Less that 70% should be considered inappropriate. Please also pay atten");
         Image.takeScreenshot("Task_Notes_Editing");
         Image.resize("Task_Notes_Editing", "Task_Notes_Editing_Small");
         Image.crop("Task_Notes_Editing", "Task_Notes_Editing_Crop", 650, 265, 357, 72);
         Image.svgToPng("Managing_Tasks_Task_Notes_Editing");
         tasks.get("Check unit tests code coverage").deselect();
 
     }
 
     private void taskEditing(){
         tasks.get("Implement image resizing").select();
         Image.takeScreenshot("Task_Editing");
         Image.crop("Task_Editing", "Task_Editing_Crop", 398, 95, 96, 43);
         Image.resize("Task_Editing", "Task_Editing_Small");
         generalFlow.getAppPage().controlPanel().clickEditButtonAndWait(generalFlow.getAppPage());
           Image.takeScreenshot("Task_Editing_Dialog");
         Image.crop("Task_Editing_Dialog", "Task_Editing_Dialog_Crop", 175, 205, 664, 358);
         Image.svgToPng("Managing_Tasks_Task_Editing");
         generalFlow.getAppPage().modalDialog().closeButton().click();
         tasks.get("Implement image resizing").deselect();
 
     }
 
     private void taskCompletion(){
         tasks.get("Implement JPG support").select();
         generalFlow.getAppPage().controlPanel().completeTaskButton().click();
         generalFlow.getAppPage().notifier().waitForPresence("Task is completed");
         generalFlow.getAppPage().notifier().waitForDisappear("Task is completed");
         tasks.get("Check unit tests code coverage").select();
         Image.takeScreenshot("Task_Completion");
         Image.resize("Task_Completion", "Task_Completion_Small");
         Image.crop("Task_Completion", "Task_Completion_Button_Crop", 578, 95, 96, 43);
         Image.crop("Task_Completion", "Task_Completion_Task_Crop", 195, 302, 458, 64);
         Image.crop("Task_Completion", "Task_Completion_List_Crop", 30, 538, 130, 25);
         Image.svgToPng("Managing_Tasks_Task_Completion");
 
     }
 
 }
