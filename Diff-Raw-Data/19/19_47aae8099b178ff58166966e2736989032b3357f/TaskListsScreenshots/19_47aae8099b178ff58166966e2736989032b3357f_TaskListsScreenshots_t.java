 package com.teamdev.projects.test.screenshots;
 
 
 import com.teamdev.projects.test.SingleAccountTest;
 import com.teamdev.projects.test.web.businessflow.ProjectFlow;
 import com.teamdev.projects.test.web.businessflow.TaskFlow;
 
 import java.util.Map;
 
 import static com.teamdev.projects.test.webdriver.WebDriverManager.stopDriver;
 
 /**
  * @author Dmitry Kalmetyev
  */
 
 public class TaskListsScreenshots extends SingleAccountTest {
     Map<String, ProjectFlow> projects;
     Map<String, TaskFlow> tasks;
 
 
     public TaskListsScreenshots(Map<String, ProjectFlow> projects, Map<String, TaskFlow> tasks){
         this.projects = projects;
         this.tasks = tasks;
 
         generalFlow = setup("1024,790");
         rightSideStatisticsScreenshot();
         inboxScreenshot();
         stopDriver();
     }
 
     private void rightSideStatisticsScreenshot() {
         Image.crop("Create_New_Task_Dialog", "Statistics_Crop", 653, 210, 253, 282 );
         Image.svgToPng("Task_Lists_Right_Side_Statistics");
     }
 
     private void inboxScreenshot(){
         projects.get("Image Editor").clickInboxFocus();
        tasks.get("Redesign Welcome dialog").select();
         Image.takeScreenshot("Inbox_List");
         Image.crop("Inbox_List", "Inbox_List_Crop", 35, 429, 160, 28);
         Image.resize("Inbox_List", "Inbox_List_Small");
         Image.svgToPng("Task_Lists_Inbox");
 
     }
 
 
 
 }
