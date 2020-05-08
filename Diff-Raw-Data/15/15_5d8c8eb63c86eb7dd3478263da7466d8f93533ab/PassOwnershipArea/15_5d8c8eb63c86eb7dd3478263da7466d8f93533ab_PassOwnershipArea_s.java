 package com.teamdev.projects.test.area;
 
 import com.teamdev.projects.test.PassOwnershipTest;
 import com.teamdev.projects.test.web.businessflow.AreaFlow;
 import com.teamdev.projects.test.web.businessflow.ProjectFlow;
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 /**
  * @author Alexander Orlov
  */
 public class PassOwnershipArea extends PassOwnershipTest {
 
     @Test
     public void passOwnershipOfArea() {
         AreaFlow areaFlow = generalFlow.createArea(randomArea());
         areaFlow.passOwnershipTo(secondUser);
         assertTrue(generalFlow.isModalDialogClosed(), "Modal dialog is not closed");
         areaFlow.showMenu();
         assertFalse(areaFlow.getItemElement().dropDownMenu().passOwnership().isPresent(), "First user has an ownership");
         generalFlow.refresh();
         areaFlow.showMenu();
         assertFalse(areaFlow.getItemElement().dropDownMenu().passOwnership().isPresent(), "First user has an ownership after a refresh");
        generalFlow.reLoginAs(secondUser);
         assertTrue(areaFlow.isPresent(), "Area is not appeared for second user after passing ownership");
         areaFlow.showMenu();
         assertTrue(areaFlow.getItemElement().dropDownMenu().passOwnership().isPresent(), "Second does not have an owner permissions (though area is displayed)");
     }
 
     @Test
     public void passOwnershipOfAreaWithRelatedProject() {
         AreaFlow areaFlow = generalFlow.createArea(randomArea());
         ProjectFlow projectInArea = areaFlow.createProject(randomProject());
         areaFlow.passOwnershipTo(thirdUser);
         assertTrue(generalFlow.isModalDialogClosed(), "Modal dialog is not closed");
         areaFlow.showMenu();
         assertFalse(areaFlow.getItemElement().dropDownMenu().passOwnership().isPresent(), "First user has an ownership");
         generalFlow.refresh();
         areaFlow.showMenu();
         assertFalse(areaFlow.getItemElement().dropDownMenu().passOwnership().isPresent(), "First user has an ownership after a refresh");
        generalFlow.reLoginAs(thirdUser);
         assertTrue(areaFlow.isPresent(), "Area is not appeared for second user after passing ownership");
         assertFalse(projectInArea.isPresent(), "Project is appeared for second user after passing ownership associated with it area");
     }
 }
