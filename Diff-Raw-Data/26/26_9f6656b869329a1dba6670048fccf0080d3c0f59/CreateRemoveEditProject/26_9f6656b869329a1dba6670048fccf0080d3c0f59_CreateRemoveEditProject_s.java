 package com.teamdev.projects.test.project;
 
 
 import com.teamdev.projects.test.SingleAccountTest;
 import com.teamdev.projects.test.data.ProjectData;
 import com.teamdev.projects.test.web.businessflow.ProjectFlow;
 import org.testng.annotations.Test;
 
 import static com.teamdev.projects.test.util.Strings.randomizeName;
 import static org.testng.Assert.*;
 
 /**
  * @author Alexander Orlov
  */
 public class CreateRemoveEditProject extends SingleAccountTest {
 
     @Test
     public void createAndRemoveProject() {    //verified on Sep, 11
         ProjectFlow project = generalFlow.createProject(randomProject());
         assertTrue(generalFlow.isModalDialogClosed(), "Modal dialog is not closed");
         assertTrue(project.isPresent(), "Project was not created immediately");
         generalFlow.refresh();
         assertTrue(project.isPresent(), "Project was not created after a refresh");
         assertTrue(project.projectDataIsCorrect(), "Project data is not correct");
         project.remove();
         assertTrue(generalFlow.isModalDialogClosed(), "Modal dialog is closed");
         assertFalse(project.isPresent(), "Project was not removed immediately");
         generalFlow.refresh();
         assertFalse(project.isPresent(), "Project was not removed after a refresh");
     }
 
     @Test
    public void fullNameAutoSuggested(){   //verified on Feb, 8
         ProjectData projectDataWithShortNameOnly = new ProjectData(randomizeName("Short Name"));
         ProjectFlow project = generalFlow.createProject(projectDataWithShortNameOnly);
         generalFlow.refresh();
         assertTrue(project.projectDataIsCorrect(), "Project data is not correct");
         project.remove();
     }
 
     @Test
    public void shouldNotCreateProjectWithoutNames() {  //verified on Feb, 8
         ProjectData projectDataBlank = new ProjectData("", "");
         generalFlow.createProjectWithWrongData(projectDataBlank);
         assertFalse(generalFlow.isModalDialogClosed(), "Modal dialog is closed");
         String alertMessageForTooSmallShortName = "Project short name cannot be empty";
         String alertMessageForTooSmallFullName = "Project full name cannot be empty";
         assertEquals(generalFlow.getAlertForWrongShortName(), alertMessageForTooSmallShortName);
         assertEquals(generalFlow.getAlertForWrongFullName(), alertMessageForTooSmallFullName);
     }
 
     @Test
    public void shouldNotCreateProjectWithDuplicatedName() {  //verified on Feb, 8
         ProjectData projectData = randomProject();
         generalFlow.createProject(projectData);
         generalFlow.createProjectWithWrongData(projectData);
         assertFalse(generalFlow.isModalDialogClosed(), "Modal dialog is closed");
         String alertMessageForDuplicatedShortName = "The project with short name “" + projectData.getShortName() + "” already exists.";
         assertEquals(generalFlow.getAlertForWrongShortName(), alertMessageForDuplicatedShortName);
     }
 
     @Test
    public void shouldNotCreateProjectForNonExistArea() {  //verified on Feb, 8
         ProjectData projectDataWithArea = new ProjectData(randomizeName("Edited Short"), randomizeName("Edited Full"), randomizeName("Area"), "");
         generalFlow.createProjectWithWrongData(projectDataWithArea);
         assertFalse(generalFlow.isModalDialogClosed(), "Modal dialog is closed");
         String alertMessageForNonExistArea = "Such area doesn't exist.";
         assertEquals(generalFlow.getAlertForWrongAreaOfResponsibilityName(), alertMessageForNonExistArea);
     }
 
     @Test
    public void editProject() {  //verified on Feb, 8
         ProjectData newProjectData = randomProject();
         ProjectFlow project = generalFlow.createProject(randomProject());
         project.edit(newProjectData);
         assertTrue(generalFlow.isModalDialogClosed(), "Modal dialog is not closed");
         assertEquals(project.getTitleText(), newProjectData.getShortName(), "Project name was not changed in the list immediately");
         generalFlow.refresh();
         project.updateProjectLocator(newProjectData);
         assertTrue(project.isPresent(), "Project is not displayed after a refresh");
         assertTrue(project.projectDataIsCorrect(),"Project data is wrong after a refresh");
         project.remove();
     }
 }
