 package com.teamdev.projects.test.web.businessflow.detailspanel;
 
 import com.teamdev.projects.test.web.page.AppPage;
 
 /**
  * @author Sergii Moroz
  */
 public class DetailsPanelForTaskFlow {
     private AppPage appPage;
 
     public DetailsPanelForTaskFlow(AppPage appPage){
         this.appPage = appPage;
     }
 
     public AppPage getAppPage() {
         return appPage;
     }
 
     public String getPanelTitle(){
         return appPage.getDetailsPanelForTask().panelTitle().getText();
     }
 
     public void pressInfoTab(){
         appPage.getDetailsPanelForTask().infoTab().click();
     }
 
     public void pressEmptyCommentsTab(){
         appPage.getDetailsPanelForTask().emptyCommentsTab().click();
     }
 
     public void pressLogTab(){
         appPage.getDetailsPanelForTask().logTab().click();
     }
 
     public void changeTaskName(String taskName){
         appPage.getDetailsPanelForTask().panelTitle().clearAndFill(taskName);
         appPage.getDetailsPanelForTask().panelTitle().pressTabKey();
        appPage.notifier().waitForPresence("Task was renamed.");
     }
 }
