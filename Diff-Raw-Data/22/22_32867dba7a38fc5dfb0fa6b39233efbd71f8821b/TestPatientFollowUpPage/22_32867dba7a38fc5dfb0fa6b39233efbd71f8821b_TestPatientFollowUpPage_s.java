 package com.solidstategroup.radar.test.web;
 
 import com.solidstategroup.radar.model.Demographics;
 import com.solidstategroup.radar.web.pages.PatientPage;
 import org.apache.wicket.Component;
 import org.junit.Test;
 
 /**
  * Simple test using the WicketTester
  */
 public class TestPatientFollowUpPage extends BasePageTest {
 
     @Test
     public void renderFollowUpPage() {
         // Start and render the test page
         Demographics demographics = new Demographics();
         demographics.setId(new Long(238));
         tester.startPage(PatientPage.class, PatientPage.getParameters(demographics));
 
         // Assert rendered page class
         tester.assertRenderedPage(PatientPage.class);
 
         // Try Ajax refresh
         Component followUpLink = tester.getLastRenderedPage().get("linksContainer").get("followUpLink");
         tester.clickLink(followUpLink.getPageRelativePath());
 
         // View the lab results tab
        Component labResultsLink = tester.getLastRenderedPage().get("followUpPanel").get("laboratoryResultsLink");
         tester.clickLink(labResultsLink.getPageRelativePath());
 
         Component labResultsPanel = tester.getLastRenderedPage().get("followUpPanel").get("laboratoryResultsPanel");
         tester.assertVisible(labResultsPanel.getPageRelativePath());
 
         // View the treatment tab
        Component treatmentLink = tester.getLastRenderedPage().get("followUpPanel").get("treatmentLink");
         tester.clickLink(treatmentLink.getPageRelativePath());
 
         Component treatmentPanel = tester.getLastRenderedPage().get("followUpPanel").get("treatmentPanel");
         tester.assertVisible(treatmentPanel.getPageRelativePath());
 
         // View the RRT panel
        Component rrtLink = tester.getLastRenderedPage().get("followUpPanel").get("rrtTherapyLink");
         tester.clickLink(rrtLink.getPageRelativePath());
 
         Component rrtPanel = tester.getLastRenderedPage().get("followUpPanel").get("rrtTherapyPanel");
         tester.assertVisible(rrtPanel.getPageRelativePath());
     }
 }
