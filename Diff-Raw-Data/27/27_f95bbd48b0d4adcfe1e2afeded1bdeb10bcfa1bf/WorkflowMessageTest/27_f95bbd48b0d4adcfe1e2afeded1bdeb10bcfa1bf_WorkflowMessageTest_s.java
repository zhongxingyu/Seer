 package edu.northwestern.bioinformatics.studycalendar.service.presenter;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import junit.framework.TestCase;
 
 /**
  * @author Rhett Sutphin
  */
 public class WorkflowMessageTest extends TestCase {
     public void testHtmlMessageContainsLinkWhenCanPerform() throws Exception {
         WorkflowMessage actual = new WorkflowMessage(WorkflowStep.ASSIGN_SITE, null, true);
         actual.setUriVariable("study-id", "14");
         assertEquals("Wrong message HTML",
             "needs at least one site <a href=\"/pages/cal/assignSite?id=14\" class=\"control\">assigned</a> for participation.",
             actual.getHtml());
     }
 
     public void testHtmlMessageRespectsApplicationMountPoint() throws Exception {
         WorkflowMessage actual = new WorkflowMessage(WorkflowStep.ASSIGN_SITE, "/psc-prod", true);
         actual.setUriVariable("study-id", "14");
         assertEquals("Wrong message HTML",
             "needs at least one site <a href=\"/psc-prod/pages/cal/assignSite?id=14\" class=\"control\">assigned</a> for participation.",
             actual.getHtml());
     }
 
     public void testHtmlMessageContainsApplicableRoleWhenCannotPerform() throws Exception {
         WorkflowMessage actual = new WorkflowMessage(WorkflowStep.ASSIGN_SITE, null, false);
         assertEquals("Wrong message HTML",
             "needs at least one site assigned for participation.  A <em>Study Site Participation Administrator</em> can do this.",
             actual.getHtml());
     }
 
     public void testTextMessageContainsAffirmativeMessageWhenCanPerform() throws Exception {
         WorkflowMessage actual = new WorkflowMessage(WorkflowStep.ASSIGN_SITE, null, true);
         actual.setUriVariable("studyId", "14");
         assertEquals("Wrong message text",
             "needs at least one site assigned for participation.  You can do this.",
             actual.getText());
     }
 
     public void testTextMessageContainsApplicableRoleWhenCannotPerform() throws Exception {
         WorkflowMessage actual = new WorkflowMessage(WorkflowStep.ASSIGN_SITE, null, false);
         assertEquals("Wrong message text",
             "needs at least one site assigned for participation.  A Study Site Participation Administrator can do this.",
             actual.getText());
     }
 
     public void testExceptionWhenCanPerformButMissingUriVariable() throws Exception {
         WorkflowMessage actual = new WorkflowMessage(WorkflowStep.ASSIGN_SITE, null, true);
         try {
             actual.getHtml();
             fail("Exception not thrown");
         } catch (StudyCalendarSystemException scse) {
             assertEquals("Missing study-id variable value for workflow message", scse.getMessage());
         }
     }
 
     public void testExceptionWhenCanPerformButMissingMessageVariable() throws Exception {
         WorkflowMessage actual = new WorkflowMessage(WorkflowStep.STUDY_SEGMENT_NO_PERIODS, null, true);
         try {
             actual.getText();
             fail("Exception not thrown");
         } catch (StudyCalendarSystemException scse) {
             assertEquals("Missing segment-name variable value for workflow message", scse.getMessage());
         }
     }
 
     public void testHtmlForMessageWithoutAction() throws Exception {
         WorkflowMessage actual = new WorkflowMessage(WorkflowStep.ADD_AT_LEAST_ONE_EPOCH, null, true);
         assertEquals("Wrong message HTML", "Please add at least one epoch.", actual.getHtml());
     }
 
     public void testHtmlForMessageWithMessageVariables() throws Exception {
         WorkflowMessage actual = new WorkflowMessage(
             WorkflowStep.PERIOD_NO_PLANNED_ACTIVITIES, null, true);
         actual.setMessageVariable("period-name", "P1");
         actual.setMessageVariable("segment-name", "Alfa");
         actual.setUriVariable("period-id", "5");
         assertEquals("Wrong message HTML",
             "Period P1 in Alfa does not have any planned activities.  <a href=\"/pages/cal/managePeriodActivities?period=5\" class=\"control\">Add some.</a>",
             actual.getHtml());
     }
 
     public void testTextForMessageWithMessageVariables() throws Exception {
         WorkflowMessage actual = new WorkflowMessage(
             WorkflowStep.PERIOD_NO_PLANNED_ACTIVITIES, null, true);
         actual.setMessageVariable("period-name", "P1");
         actual.setMessageVariable("segment-name", "Alfa");
         assertEquals("Wrong message text",
             "Period P1 in Alfa does not have any planned activities.  Add some.  You can do this.",
             actual.getText());
     }
 
     public void testUriWhenApplicationContextPathPointNull() throws Exception {
         WorkflowMessage actual = new WorkflowMessage(WorkflowStep.ASSIGN_SITE, null, true);
         actual.setUriVariable("study-id", "18");
         assertEquals("/pages/cal/assignSite?id=18", actual.getUri());
     }
 
     public void testUriWhenApplicationContextPathPointSlash() throws Exception {
         WorkflowMessage actual = new WorkflowMessage(WorkflowStep.ASSIGN_SITE, "/", true);
         actual.setUriVariable("study-id", "18");
         assertEquals("/pages/cal/assignSite?id=18", actual.getUri());
     }
 
     public void testUriWhenApplicationContextPathPointSet() throws Exception {
         WorkflowMessage actual = new WorkflowMessage(WorkflowStep.ASSIGN_SITE, "/psc-demo", true);
         actual.setUriVariable("study-id", "18");
         assertEquals("/psc-demo/pages/cal/assignSite?id=18", actual.getUri());
     }
 
     public void testUriWhenApplicationContextPathPointWithTrailingSlash() throws Exception {
         WorkflowMessage actual = new WorkflowMessage(WorkflowStep.ASSIGN_SITE, "/psc-arg/", true);
         actual.setUriVariable("study-id", "18");
         assertEquals("/psc-arg/pages/cal/assignSite?id=18", actual.getUri());
     }
 }
