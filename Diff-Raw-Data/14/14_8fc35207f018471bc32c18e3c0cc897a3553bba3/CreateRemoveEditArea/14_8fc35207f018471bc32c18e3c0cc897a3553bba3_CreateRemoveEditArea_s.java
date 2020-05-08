 package com.teamdev.projects.test.area;
 
 import com.teamdev.projects.test.SingleAccountTest;
 import com.teamdev.projects.test.data.AreaData;
 import com.teamdev.projects.test.web.businessflow.AreaFlow;
 import com.teamdev.projects.test.web.businessflow.ProjectFlow;
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.*;
 
 /**
  * @author Alexander Orlov
  */
 public class CreateRemoveEditArea extends SingleAccountTest {
 
     @Test
    public void createAndRemoveArea() {
         AreaFlow areaFlow = generalFlow.createArea(randomArea());
         assertTrue(generalFlow.isModalDialogClosed(), "Modal dialog is not closed");
         assertTrue(areaFlow.isPresent(), "Area is not displayed in the list immediately");
         generalFlow.refresh();
         assertTrue(areaFlow.isPresent(), "Area is not displayed in the list after a refresh");
         assertTrue(areaFlow.dataIsCorrect(), "Area has wrong data");
         areaFlow.remove();
         assertTrue(generalFlow.isModalDialogClosed(), "Modal dialog is not closed");
         assertFalse(areaFlow.isPresent(), "Area was not removed immediately");
         generalFlow.refresh();
         assertFalse(areaFlow.isPresent(), "Area was not removed after a refresh");
 
     }
 
     @Test
    public void shouldNotCreateAreaWithoutName() {
         AreaData areaDataBlank = new AreaData("");
         generalFlow.createAreaWithWrongData(areaDataBlank);
         assertFalse(generalFlow.isModalDialogClosed(), "Modal dialog is closed");
         String alertForTooSmallAreaName = "Area name cannot be less than 3 characters.";
         assertEquals(generalFlow.getAlertForWrongAreaName(), alertForTooSmallAreaName);
     }
 
     @Test
    public void shouldNotCreateAreaWithDuplicatedName() {
         AreaData areaName = randomArea();
         generalFlow.createArea(areaName);
         generalFlow.createAreaWithWrongData(areaName);
         assertFalse(generalFlow.isModalDialogClosed(), "Modal dialog is closed");
         String alertForDuplicatedAreaName = "The area with the “" + areaName.getName() + "” already exists.";
         assertEquals(generalFlow.getAlertForWrongAreaName(), alertForDuplicatedAreaName);
     }
 
     @Test
    public void editArea() {
         AreaData newAreaData = randomArea();
         AreaFlow areaFlow = generalFlow.createArea(randomArea());
         areaFlow.edit(newAreaData);
         assertTrue(generalFlow.isModalDialogClosed(), "Modal dialog is not closed");
         assertEquals(areaFlow.getTitleText(), newAreaData.getName(), "Area name was not changed in the list immediately");
         generalFlow.refresh();
         areaFlow.updateAreaLocator(newAreaData);
         assertTrue(areaFlow.isPresent(), "Edited Area is not displayed after a refresh");
         assertTrue(areaFlow.dataIsCorrect(), "Area has wrong data");
         areaFlow.remove();
     }
 
     @Test
    public void createProjectInArea() {
         AreaFlow areaFlow = generalFlow.createArea(randomArea());
         ProjectFlow projectInArea = areaFlow.createProject(randomProject());
         assertTrue(generalFlow.isModalDialogClosed(), "Modal dialog is not closed");
         assertTrue(projectInArea.isPresent(), "Project was not displayed immediately");
         generalFlow.refresh();
         areaFlow.expand();
         assertTrue(projectInArea.isPresent(), "Project was not displayed after a refresh");
         assertTrue(projectInArea.projectDataIsCorrect(), "Project data is not correct");
         areaFlow.remove();
     }
 
 }
