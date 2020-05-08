 package com.teamdev.projects.test.web.businessflow;
 
 
 import com.teamdev.projects.test.data.AreaData;
 import com.teamdev.projects.test.data.ProjectData;
 import com.teamdev.projects.test.web.businessflow.detailspanel.*;
 import com.teamdev.projects.test.web.component.detailspanel.focus.DetailsPanelDefault;
 import com.teamdev.projects.test.web.page.AppPage;
 
 import static com.teamdev.projects.test.webdriver.WebDriverManager.*;
 
 
 /**
  * @author Alexander Orlov
  *         <p/>
  *         Summary:
  *         This class provides all business actions that could be done just after navigating to The Projects and all
  *         time while using application. E.g. We can try to create area always, without adding any additional content.
  *         We can check if Modal Dialog closed or not, check some alerts in Modal Dialogs.
  */
 public class GeneralFlow {
 
     private AppPage appPage;
 
     public GeneralFlow(AppPage appPage) {
         this.appPage = appPage;
     }
 
     private void add(AreaData areaData) {
         openNewAreaDialogAndFillData(areaData);
         appPage.modalDialog().saveButton().click();
     }
 
     public void openNewAreaDialogAndFillData(AreaData areaData) {
         appPage.menuPanel().areasSection().actionButton().click();
         appPage.menuPanel().areasSection().createItemInDropDownMenu().click();
         appPage.modalDialog().areaNameInputBox().fillIn(areaData.getName());
         appPage.modalDialog().areaDescriptionInputBox().fillIn(areaData.getDescription());
     }
 
     private void add(ProjectData projectData) {
         openNewProjectDialogAndFillData(projectData);
         appPage.modalDialog().saveButton().click();
     }
 
     public void openNewProjectDialogAndFillData(ProjectData projectData) {
         appPage.menuPanel().projectsSection().actionButton().click();
         appPage.menuPanel().projectsSection().createItemInDropDownMenu().click();
         appPage.modalDialog().projectShortNameInputBox().fillIn(projectData.getShortName());
         if (!projectData.getFullName().isEmpty() && projectData.getFullName()!=null){
         appPage.modalDialog().projectFullNameInputBox().clearAndFill(projectData.getFullName());
         }
         if (projectData.getAreaOfResponsibility() != null) {
             appPage.modalDialog().projectAreaOfResponsibilityInputBox().fillIn(projectData.getAreaOfResponsibility());
         }
         appPage.modalDialog().projectDescriptionInputBox().fillIn(projectData.getDescription());
     }
 
 
     public AreaFlow createArea(AreaData areaData) {
         add(areaData);
         return new AreaFlow(areaData, appPage);
     }
 
     public GeneralFlow createAreaWithWrongData(AreaData areaData) {
         add(areaData);
         return this;
     }
 
     public String getAlertForWrongAreaName() {
         return appPage.modalDialog().alertLabelForWrongAreaName().getText();
     }
 
     public ProjectFlow createProject(ProjectData projectData) {
         add(projectData);
         return new ProjectFlow(projectData, appPage);
     }
 
     public GeneralFlow createProjectWithWrongData(ProjectData projectData) {
         add(projectData);
         return this;
     }
 
     public String getAlertForWrongShortName() {
         return appPage.modalDialog().alertLabelForWrongShortName().getText();
     }
 
     public String getAlertForWrongFullName() {
         return appPage.modalDialog().alertLabelForWrongFullName().getText();
     }
 
     public String getAlertForWrongAreaOfResponsibilityName() {
         return appPage.modalDialog().alertLabelForWrongAreaOfResponsibility().getText();
     }
 
     public String getProjectInAreaTitle(ProjectData projectData, AreaData areaData) {
         return appPage.menuPanel().projectInArea(areaData, projectData).title().getText();
     }
 
     public boolean isModalDialogClosed() {
         return appPage.modalDialog().isClosed();
     }
 
     public boolean isModalDialogForTaskCreationClosed() {
         return appPage.createTaskModalDialog().isClosed();
     }
 
     public void selectTodayFocus() {
         appPage.menuPanel().personalFocus().today().click();
     }
 
     public void selectNextFocus() {
         appPage.menuPanel().personalFocus().next().click();
     }
 
     public void selectScheduledFocus() {
         appPage.menuPanel().personalFocus().scheduled().click();
     }
 
     public void selectSomedayFocus() {
         appPage.menuPanel().personalFocus().someday().click();
     }
 
     public DetailsPanelFlowDefault getDefaultFlow(){
         return new DetailsPanelFlowDefault(appPage);
     }
 
     public DetailsPanelForTaskFlow getDetailsPanelForTask(){
         return new DetailsPanelForTaskFlow(appPage);
     }
 
     public DetailsPanelForTaskInfoFlow getInfoTabFlow(){
         return new DetailsPanelForTaskInfoFlow(appPage);
     }
 
     public DetailsPanelForTaskCommentsFlow getCommentsTabFlow(){
         return new DetailsPanelForTaskCommentsFlow(appPage);
     }
 
     public DetailsPanelForTaskLogFlow getLogTabFlow (){
         return new DetailsPanelForTaskLogFlow(appPage);
     }
 
     public DetailsPanelDefault getDefaultDetailsPanel(){
         return new DetailsPanelDefault();
     }
 
     public void refresh(){
         getDriver().navigate().refresh();
        getDriver().navigate().refresh(); //some magic. After one refresh - application isn't appear.
         appPage.controlPanel().newTaskButton().waitForAppearing();
     }
 
     public AppPage getAppPage(){
         return this.appPage;
     }
 }
