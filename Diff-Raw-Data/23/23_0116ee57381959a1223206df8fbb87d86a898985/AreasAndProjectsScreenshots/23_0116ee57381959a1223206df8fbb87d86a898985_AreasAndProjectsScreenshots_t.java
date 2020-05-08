 package com.teamdev.projects.screenshots;
 
 import com.teamdev.projects.test.SingleAccountTest;
 import com.teamdev.projects.test.data.AreaData;
 import com.teamdev.projects.test.data.ProjectData;
 import com.teamdev.projects.test.data.UserData;
 import com.teamdev.projects.test.web.businessflow.AreaFlow;
 import com.teamdev.projects.test.web.businessflow.PersonFlow;
 import com.teamdev.projects.test.web.businessflow.ProjectFlow;
 
 import java.util.Map;
 
 import static com.teamdev.projects.test.webdriver.WebDriverManager.stopDriver;
 
 /**
  * @author Dmitry Kalmetyev
  */
 
 public class AreasAndProjectsScreenshots extends SingleAccountTest {
     private Map<String, ProjectFlow> projects;
     private Map<String, AreaFlow> areas;
     UserData secondUser;
 
     public AreasAndProjectsScreenshots(Map<String, ProjectFlow> projects, Map<String, AreaFlow> areas, UserData secondUser){
         this.projects = projects;
         this.areas = areas;
         this.secondUser = secondUser;
 
         generalFlow = setup("1024,830");
         projectMenuScreenshot();
         areaMenuScreenshot();
         createAreaDialogScreenshot();
         createProjectDialogScreenshot();
         editAreaMenuScreenshot();
         editProjectMenuScreenshot();
         editAreaDialogScreenshot();
         editProjectDialogScreenshot();
         workReportProjectMenuScreenshot();
         settingsProjectMenuScreenshot();
         settingsDialogScreenshot();
         passOwnershipScreenshot();
         removeProjectMenuScreenshot();
         removeAreaDialogScreenshot();
         removeProjectDialogScreenshot();
         stopDriver();
     }
 
     private void projectMenuScreenshot(){
         Image.svgToPng("Areas_And_Projects_Project_Menu");
 
     }
 
     private void areaMenuScreenshot(){
         generalFlow.getAppPage().menuPanel().projectsSection().title().click();
         PersonFlow person = new PersonFlow(secondUser, generalFlow.getAppPage());
         person.add();
         areas.get("R&D").showMenuAndWait();
         Image.takeScreenshot("Area_Menu");
        Image.crop("Area_Menu", "Area_Menu_Crop", 6, 452, 316, 230);
         Image.resize("Area_Menu", "Area_Menu_Small");
         Image.svgToPng("Areas_And_Projects_Area_Menu");
         areas.get("R&D").showMenu();
         person.remove();
         generalFlow.getAppPage().menuPanel().projectsSection().title().click();
     }
 
     private void createAreaDialogScreenshot(){
         AreaData areaData = new AreaData("Human Resources", "This area of responsibility includes vacancies management, staff management and so on...");
         generalFlow.openNewAreaDialogAndFillData(areaData);
         Image.takeScreenshot("Create_Area_Dialog");
        Image.crop("Create_Area_Dialog", "Create_Area_Dialog_Crop", 136, 118, 742, 514);
         Image.svgToPng("Areas_And_Projects_Create_Area_Dialog");
         generalFlow.getAppPage().modalDialog().closeButton().click();
     }
 
     private void createProjectDialogScreenshot(){
         ProjectData projectData = new ProjectData("Relocation", "Moving to the new office", "Human Resources", "Create polls to find out staff preferences on different rooms, confirm relocation time, etc.");
         generalFlow.openNewProjectDialogAndFillData(projectData);
         Image.takeScreenshot("Create_Project_Dialog");
        Image.crop("Create_Project_Dialog", "Create_Project_Dialog_Crop", 136, 118, 742, 514);
         Image.svgToPng("Areas_And_Projects_Create_Project_Dialog");
         generalFlow.getAppPage().modalDialog().closeButton().click();
 
     }
 
     private void editAreaMenuScreenshot(){
         Image.svgToPng("Areas_And_Projects_Edit_Area_Menu");
     }
 
     private void editProjectMenuScreenshot(){
         Image.svgToPng("Areas_And_Projects_Edit_Project_Menu");
     }
 
 
     private void editAreaDialogScreenshot(){
         areas.get("Human Resources").openEditAreaDialog();
         Image.takeScreenshot("Edit_Area_Dialog");
        Image.crop("Edit_Area_Dialog", "Edit_Area_Dialog_Crop", 136, 118, 742, 514);
         Image.svgToPng("Areas_And_Projects_Edit_Area_Dialog");
         generalFlow.getAppPage().modalDialog().closeButton().click();
     }
 
     private void editProjectDialogScreenshot(){
         projects.get("Text Editor").openEditProjectDialog();
         Image.takeScreenshot("Edit_Project_Dialog");
        Image.crop("Edit_Project_Dialog", "Edit_Project_Dialog_Crop", 136, 118, 742, 514);
         Image.svgToPng("Areas_And_Projects_Edit_Project_Dialog");
         generalFlow.getAppPage().modalDialog().closeButton().click();
     }
     private void workReportProjectMenuScreenshot(){
         Image.svgToPng("Areas_And_Projects_Work_Report_Project_Menu");
     }
 
     private void settingsProjectMenuScreenshot(){
         Image.svgToPng("Areas_And_Projects_Settings_Project_Menu");
     }
 
     private void settingsDialogScreenshot(){
         projects.get("Text Editor").openEditProjectDialog();
         generalFlow.getAppPage().modalDialog().settingsButton().click();
 //        projects.get("Image Editor").openSettingsDialog();
         Image.takeScreenshot("Settings_Dialog");
        Image.crop("Settings_Dialog", "Settings_Dialog_Crop", 136, 118, 742, 514);
         Image.svgToPng("Areas_And_Projects_Settings_Dialog");
         generalFlow.getAppPage().modalDialog().closeButton().click();
     }
 
     private void passOwnershipScreenshot(){
         projects.get("Image Editor").openPassOwnershipDialog();
         Image.takeScreenshot("Pass_Ownership_Dialog");
        Image.crop("Pass_Ownership_Dialog", "Pass_Ownership_Dialog_Crop", 272, 272, 472, 206);
         Image.svgToPng("Areas_And_Projects_Pass_Project_Ownership");
         generalFlow.getAppPage().modalDialog().closeButton().click();
 
     }
 
     private void removeProjectMenuScreenshot(){
         Image.svgToPng("Areas_And_Projects_Remove_Project_Menu");
     }
 
     private void removeAreaDialogScreenshot(){
         areas.get("R&D").openRemoveDialog();
         generalFlow.getAppPage().modalDialog().closeButton().waitForAppearing();
         Image.takeScreenshot("Remove_Area_Dialog");
        Image.crop("Remove_Area_Dialog", "Remove_Area_Dialog_Crop", 271, 285, 472, 178);
         Image.svgToPng("Areas_And_Projects_Remove_Area_Dialog");
         generalFlow.getAppPage().modalDialog().closeButton().click();
     }
 
     private void removeProjectDialogScreenshot(){
         projects.get("Image Editor").openRemoveDialog();
         generalFlow.getAppPage().modalDialog().closeButton().waitForAppearing();
         Image.takeScreenshot("Remove_Project_Dialog");
         Image.crop("Remove_Project_Dialog", "Remove_Project_Dialog_Crop", 271, 285, 472, 178);
         Image.svgToPng("Areas_And_Projects_Remove_Project_Dialog");
         generalFlow.getAppPage().modalDialog().closeButton().click();
     }
 
 
 }
 
