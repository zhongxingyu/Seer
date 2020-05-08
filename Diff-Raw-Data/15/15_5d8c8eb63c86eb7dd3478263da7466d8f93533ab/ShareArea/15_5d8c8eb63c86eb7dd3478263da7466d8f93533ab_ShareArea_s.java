 package com.teamdev.projects.test.area;
 
 import com.teamdev.projects.test.ShareTest;
 import com.teamdev.projects.test.web.businessflow.AreaFlow;
 import com.teamdev.projects.test.web.businessflow.ProjectFlow;
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 /**
  * @author Alexander Orlov
  */
 public class ShareArea extends ShareTest {
 
     @Test
     public void shareArea() {
         AreaFlow areaFlow = generalFlow.createArea(randomArea());
         areaFlow.shareTo(secondUser);
         assertTrue(generalFlow.isModalDialogClosed(), "Modal dialog is not closed");
        generalFlow.reLoginAs(secondUser);
         assertTrue(areaFlow.isPresent(), "Area is not appeared for second user after sharing");
     }
 
     @Test
     public void shareAreaWithProject() {
         AreaFlow areaFlow = generalFlow.createArea(randomArea());
         ProjectFlow projectInArea = areaFlow.createProject(randomProject());
         areaFlow.shareTo(thirdUser);
         assertTrue(generalFlow.isModalDialogClosed(), "Modal dialog is not closed");
        generalFlow.reLoginAs(thirdUser);
         assertTrue(areaFlow.isPresent(), "Area is not appeared for second user after sharing");
         assertFalse(projectInArea.isPresent(), "Project is appeared for second user after sharing associated with it area");
     }
 }
