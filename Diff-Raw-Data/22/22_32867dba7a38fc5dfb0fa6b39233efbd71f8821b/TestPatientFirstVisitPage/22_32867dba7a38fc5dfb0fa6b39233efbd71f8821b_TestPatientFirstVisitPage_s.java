 package com.solidstategroup.radar.test.web;
 
 import com.solidstategroup.radar.model.Demographics;
 import com.solidstategroup.radar.web.pages.PatientPage;
 import org.apache.wicket.Component;
 import org.junit.Test;
 
 /**
  * Simple test using the WicketTester
  */
 public class TestPatientFirstVisitPage extends BasePageTest {
 
     @Test
     public void renderPatientPage() {
         Demographics demographics = new Demographics();
         demographics.setId(new Long(238));
 
         // Start and render the test page
         tester.startPage(PatientPage.class, PatientPage.getParameters(demographics));
 
         // Assert rendered page class
         tester.assertRenderedPage(PatientPage.class);
 
         // Try Ajax refresh
         Component firstVisitLink = tester.getLastRenderedPage().get("linksContainer").get("firstVisitLink");
         tester.clickLink(firstVisitLink.getPageRelativePath());
 
         // View the lab results tab
        Component labResultsLink = tester.getLastRenderedPage().get("firstVisitPanel").get("laboratoryResultsLink");
         tester.clickLink(labResultsLink.getPageRelativePath());
 
         Component labResultsPanel = tester.getLastRenderedPage().get("firstVisitPanel").get("laboratoryResultsPanel");
         tester.assertVisible(labResultsPanel.getPageRelativePath());
 
         // View the treatment tab
        Component treatmentLink = tester.getLastRenderedPage().get("firstVisitPanel").get("treatmentLink");
         tester.clickLink(treatmentLink.getPageRelativePath());
 
         Component treatmentPanel = tester.getLastRenderedPage().get("firstVisitPanel").get("treatmentPanel");
         tester.assertVisible(treatmentPanel.getPageRelativePath());
     }
 }
