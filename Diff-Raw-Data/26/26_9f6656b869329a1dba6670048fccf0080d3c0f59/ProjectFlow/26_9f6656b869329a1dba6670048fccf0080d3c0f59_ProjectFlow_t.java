 package com.teamdev.projects.test.web.businessflow;
 
 import com.teamdev.projects.test.data.AreaData;
 import com.teamdev.projects.test.data.ProjectData;
 import com.teamdev.projects.test.data.TaskData;
 import com.teamdev.projects.test.data.UserData;
 import com.teamdev.projects.test.web.component.menupanel.section.ContainerSection;
 import com.teamdev.projects.test.web.component.menupanel.section.project.ProjectItem;
 import com.teamdev.projects.test.web.component.menupanel.section.project.ProjectMenu;
 import com.teamdev.projects.test.web.page.AppPage;
 import com.teamdev.projects.test.webdriver.WebDriverManager;
 
 /**
  * @author Alexander Orlov
  */
 public class ProjectFlow extends WebDriverManager {
 
     private AppPage appPage;
     private ProjectItem projectItem;
     private ProjectData projectData;
 
     public ProjectFlow(ProjectData projectData, AppPage appPage){
         this.appPage = appPage;
         this.projectItem = appPage.menuPanel().project(projectData);
         this.projectData = projectData;
         expandSection(appPage.menuPanel().projectsSection());
     }
 
     public ProjectFlow(AreaData areaData, ProjectData projectData, AppPage appPage) {
         this.appPage = appPage;
         this.projectItem = appPage.menuPanel().projectInArea(areaData, projectData);
         this.projectData = new ProjectData(projectData.getShortName(),projectData.getFullName(),
                 areaData.getName(), projectData.getDescription());
         expandSection(appPage.menuPanel().areasSection());
         expandItem();
     }
 
 
     public AppPage getAppPage() {
         return appPage;
     }
 
     private void expandSection(ContainerSection element) {
         if (!element.wrapper().getAttribute("class").contains("expanded")) {
             element.title().click();
         }
     }
 
     public void expandItem() {
         if (!projectItem.wrapper().getAttribute("class").contains("expanded")) {
             projectItem.title().click();
         }
     }
 
     public void collapseItem() {
         if (projectItem.wrapper().getAttribute("class").contains("expanded")) {
             projectItem.title().click();
         }
     }
 
     /**
      * Hovers over Project title and clicks action button to show menu.
      */
     public void showMenu() {
         projectItem.title().hoverOverAndClickButton(projectItem.actionButton());
     }
 
 
     public void showMenuAndWait() {
         showMenu();
         this.dropDownMenu().editItem().waitForAppearing();
     }
 
     public ProjectMenu dropDownMenu(){
         return projectItem.dropDownMenu(null,this.getProjectData().getShortName());
     }
 
     protected void editProjectDataAndSave() {
         appPage.modalDialog().projectShortNameInputBox().clearAndFill(projectData.getShortName());
         appPage.modalDialog().projectFullNameInputBox().clearAndFill(projectData.getFullName());
         if (projectData.getAreaOfResponsibility() != null && !projectData.getAreaOfResponsibility().isEmpty()) {
             appPage.modalDialog().projectAreaOfResponsibilityInputBox().clearAndFill(projectData.getAreaOfResponsibility());
         }
         appPage.modalDialog().projectDescriptionInputBox().clearAndFill(projectData.getDescription());
        appPage.modalDialog().doneButton().click();
     }
 
     public void edit(ProjectData newProjectData){
         openEditProjectDialog();
         this.projectData=newProjectData;
        editProjectDataAndSave();
        wait(3);
     }
 
     public void openEditProjectDialog() {
         showMenuAndWait();
         this.dropDownMenu().editItem().click();
     }
 
     public void shareTo(UserData userData) {
         showMenuAndWait();
         openShareDialog();
         appPage.modalDialog().participantInputBox().fillIn(userData.getEmail());
         appPage.autoCompleteList().waitForAppearing();
         appPage.modalDialog().participantInputBox().pressEnterKey();
         appPage.modalDialog().participantInputBox().pressTabKey();
         appPage.modalDialog().participantAddButton().click();
         appPage.modalDialog().saveButton().click();
     }
 
     public void openShareDialog() {
         this.dropDownMenu().shareItem().click();
     }
 
     public void passOwnershipTo(UserData userData) {
         openPassOwnershipDialog();
         appPage.modalDialog().ownerInputBox().clearAndFill(userData.getEmail());
 //        appPage.modalDialog().autoCompleteList().waitForAppearing();
         appPage.modalDialog().ownerInputBox().pressTabKey();
         appPage.modalDialog().saveButton().click();
     }
 
     public void remove() {
         openRemoveDialog();
         appPage.modalDialog().saveButton().click();
         wait(2);
     }
 
     public void openRemoveDialog() {
         showMenuAndWait();
         this.dropDownMenu().removeItem().click();
     }
 
     public void clickInboxFocus() {
         expandItem();
         projectItem.initFocus().inbox().click();
         appPage.detailsPanelDefault().totalLogged().waitForAppearing();
     }
 
     public void clickTodayFocus() {
         expandItem();
         projectItem.initFocus().today().click();
         appPage.detailsPanelDefault().totalLogged().waitForAppearing();
     }
 
     public void clickNextFocus() {
         expandItem();
         projectItem.initFocus().next().click();
         appPage.detailsPanelDefault().totalLogged().waitForAppearing();
     }
 
     public void clickScheduledFocus() {
         expandItem();
         projectItem.initFocus().scheduled().click();
         appPage.detailsPanelDefault().totalLogged().waitForAppearing();
     }
 
     public void clickSomedayFocus() {
         expandItem();
         projectItem.initFocus().someday().click();
         appPage.detailsPanelDefault().totalLogged().waitForAppearing();
     }
 
     public void clickCompletedFocus(){
         expandItem();
         projectItem.initFocus().completed().click();
     }
 
     public TaskFlow createTaskInInboxFocus(TaskData taskData) {
         clickInboxFocus();
         TaskFlow taskFlow = new TaskFlow(taskData, appPage);
         taskFlow.createTask();
         return taskFlow;
 
     }
 
     public TaskFlow createTaskInTodayFocus(TaskData taskData) {
         clickTodayFocus();
         TaskFlow taskFlow = new TaskFlow(taskData, appPage);
         taskFlow.createTask();
         return taskFlow;
     }
 
     public TaskFlow createTaskInNextFocus(TaskData taskData) {
         clickNextFocus();
         TaskFlow taskFlow = new TaskFlow(taskData, appPage);
         taskFlow.createTask();
         return taskFlow;
     }
 
     public TaskFlow createTaskInScheduledFocus(TaskData taskData) {
         clickScheduledFocus();
         TaskFlow taskFlow = new TaskFlow(taskData, appPage);
         taskFlow.createTask();
         return taskFlow;
     }
 
     public TaskFlow createTaskInSomedayFocus(TaskData taskData) {
         clickSomedayFocus();
         TaskFlow taskFlow = new TaskFlow(taskData, appPage);
         taskFlow.createTask();
         return taskFlow;
     }
 
 
     public void editTags() {
         showMenuAndWait();
         this.dropDownMenu().editTagsItem().click();
     }
 
     public boolean isPresent() {
         return projectItem.wrapper().isPresent();
     }
 
     public TagFlow addTag(String tagName) {
         appPage.modalDialog().addTagButton().click();
         appPage.modalDialog().tagNameInputBox().fillIn(tagName);
         appPage.modalDialog().tagNameInputBox().pressTabKey();
         return new TagFlow(tagName, appPage);
     }
 
     public String getTitleText() {
         return projectItem.title().getText();
     }
 
     public TaskFlow createTaskForProject(TaskData taskData, ProjectData projectData) {
         appPage.controlPanel().newTaskButton().click();
         appPage.createTaskModalDialog().dropDownToSelectAreaOrProject().click();
         appPage.createTaskModalDialog().projectInDropDownList(projectData).click();
         TaskFlow taskFlow = new TaskFlow(taskData, appPage);
         taskFlow.addTaskData();
         return taskFlow;
     }
 
 
     public void openPassOwnershipDialog() {
         showMenuAndWait();
         this.dropDownMenu().passOwnership().click();
     }
 
     public boolean projectDataIsCorrect(){
         openEditProjectDialog();
         ProjectData read = new ProjectData(getAppPage().modalDialog().projectShortNameInputBox().getText(),
                 getAppPage().modalDialog().projectFullNameInputBox().getText(),
                 getAppPage().modalDialog().projectAreaOfResponsibilityInputBox().getText(),
                 getAppPage().modalDialog().projectDescriptionInputBox().getText());
         System.out.println(read.toString());
         System.out.println(this.projectData.toString());
         getAppPage().modalDialog().doneButton().click();
         return  read.equals(this.projectData);
     }
 
     public void updateProjectLocator(ProjectData newProjectData){
         this.projectItem = getAppPage().menuPanel().project(newProjectData);
     }
 
     public ProjectData getProjectData(){
         return this.projectData;
     }
 
     public void shareAndWait(UserData userData){
         shareTo(userData);
         getAppPage().notifier().waitForPresence("The list of project members was updated.");
 
     }
 }
