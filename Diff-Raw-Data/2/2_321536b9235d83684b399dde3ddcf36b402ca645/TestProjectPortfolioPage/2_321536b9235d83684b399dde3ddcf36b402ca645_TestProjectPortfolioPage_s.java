 package org.hackystat.projectbrowser.page.projectportfolio;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import org.apache.wicket.Component;
 import org.apache.wicket.markup.html.form.ListMultipleChoice;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.util.tester.FormTester;
 import org.apache.wicket.util.tester.WicketTester;
 import org.hackystat.projectbrowser.ProjectBrowserApplication;
 import org.hackystat.projectbrowser.ProjectBrowserProperties;
 import org.hackystat.projectbrowser.authentication.SigninPage;
 import org.hackystat.projectbrowser.page.projectportfolio.configurationpanel.
 ProjectPortfolioConfigurationPanel;
 import org.hackystat.projectbrowser.page.projectportfolio.detailspanel.ProjectPortfolioDetailsPanel;
 import org.hackystat.projectbrowser.page.projectportfolio.detailspanel.chart.MiniBarChart;
 import org.hackystat.projectbrowser.page.projectportfolio.inputpanel.ProjectPortfolioInputPanel;
 import org.hackystat.projectbrowser.test.ProjectBrowserTestHelper;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.utilities.tstamp.Tstamp;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Tests for ProjectPortfolio page.
  * 
  * @author Shaoxuan Zhang
  */
 public class TestProjectPortfolioPage extends ProjectBrowserTestHelper {
 
   /** the test user. */
   private String testUser = "TestProjectPortfolioUser";
   /** email of the test user. */
   private String testUserEmail = testUser + "@hackystat.org";
   /** the test user. */
   private String testUser2 = "TestProjectPortfolioUser2";
   /** the test project. */
   private String testProject = "TestProjectPortfolioProject";
 
   /** The word of "true". */
   private static final String TRUE = "true";
   /** The word of "false". */
   private static final String FALSE = "false";
 
   /** The start date. */
   private static final String testStartDate = "2007-01-06";
   /** The end date. */
   private static final String testEndDate = "2007-01-08";
 
   /** path of configuration panel. */
   private static final String configurationPanelPath = "configurationPanel";
   /** path of configuration form. */
   private static final String configurationFormPath = configurationPanelPath + ":configurationForm";
   /** path of input panel. */
   private static final String inputPanelPath = "inputPanel";
   /** path of input form. */
   private static final String inputFormPath = inputPanelPath + ":inputForm";
   /** path of configuration link. */
   private static final String configurationLink = "inputPanel:inputForm:configuration";
 
   /** The test properties. */
   private Properties testProperties;
 
   /** default HigherThreshold of Coverage. */
   String defaultCoverageHigherThreshold = "90";
   /** default LowerThreshold of Coverage. */
   String defaultCoverageLowerThreshold = "40";
 
   /** The wicket tester. */
   private WicketTester tester;
 
   /**
    * Initialize data for testing.
    * 
    * @throws Exception when error occur.
    */
   @Before
   public void setUp() throws Exception {
     this.generateSimData(testUser, testProject, Tstamp.makeTimestamp(testEndDate), 0);
     // prepare test properties.
     testProperties = getTestProperties();
     testProperties.put(ProjectBrowserProperties.AVAILABLEPAGE_KEY + ".projectportfolio", TRUE);
     testProperties
         .put(ProjectBrowserProperties.BACKGROUND_PROCESS_KEY + ".projectportfolio", FALSE);
     tester = new WicketTester(new ProjectBrowserApplication(testProperties));
 
     tester.startPage(SigninPage.class);
     // Let's sign in.
     FormTester signinForm = tester.newFormTester("signinForm");
     signinForm.setValue("user", testUserEmail);
     signinForm.setValue("password", testUserEmail);
     signinForm.submit("Signin");
     // first, go to project portfolio page.
     tester.clickLink("ProjectPortfolioPageLink");
     tester.assertRenderedPage(ProjectPortfolioPage.class);
   }
 
   /**
    * Test the daily project data page.
    * 
    * @throws Exception error occur
    */
   @Test
   public void testProjectPortfolioPage() throws Exception {
     
     this.generateSimData(testUser, testProject, Tstamp.makeTimestamp(testEndDate), 3);
     this.generateSimData(testUser2, testProject, Tstamp.makeTimestamp(testEndDate), 2);
     this.addMember(testProject, testUser, testUser2);
 
     tester.assertInvisible(configurationPanelPath);
     tester.clickLink(configurationLink);
     tester.assertComponent(configurationPanelPath, ProjectPortfolioConfigurationPanel.class);
 
     FormTester configurationForm = tester.newFormTester(configurationFormPath);
     configurationForm.submit("reset");
     // tester.clickLink(configurationLink);
     
     configurationForm = tester.newFormTester(configurationFormPath);
     ListView measureList = (ListView) configurationForm.getForm().get("measureList");
     assertTrue("There should be at least 10 measures.", measureList.getList().size() >= 10);
     // set the first measure(Coverage)'s granularity parameter to line.
     assertEquals("Check measure name", "Coverage", tester.getComponentFromLastRenderedPage(
         "configurationPanel:configurationForm:measureList:0:measureNameLabel")
         .getModelObjectAsString());
     configurationForm.setValue("measureList:0:parameterList:1:field", "line");
     // set the second measure to be uncolorable.
     configurationForm.setValue("measureList:1:colorableCheckBox", FALSE);
     // set all others to be disable.
     for (int i = 4; i < measureList.getList().size(); i++) {
       configurationForm.setValue("measureList:" + i + ":enableCheckBox", FALSE);
     }
     configurationForm.submit("submit");
     tester.assertInvisible(configurationPanelPath);
 
     tester.assertComponent(inputPanelPath, ProjectPortfolioInputPanel.class);
     FormTester inputForm = tester.newFormTester(inputFormPath);
 
     // check the project list content.
     Component component = inputForm.getForm().get("projectMenu");
     assertTrue("Check project select field", component instanceof ListMultipleChoice);
     ListMultipleChoice projectChoice = (ListMultipleChoice) component;
     boolean pass = false;
     int index = 0;
     for (int i = 0; i < projectChoice.getChoices().size(); i++) {
       Project project = (Project) projectChoice.getChoices().get(i);
       if (this.testProject.equals(project.getName())) {
         index = i;
         pass = true;
       }
     }
     if (!pass) {
      fail(testProject + " not found in project list.");
     }
     // select the default project.
     inputForm.select("projectMenu", index);
     inputForm.select("granularity", 0);
     inputForm.setValue("startDateTextField", testStartDate);
     inputForm.setValue("endDateTextField", testEndDate);
     inputForm.submit("submit");
     // check the result.
     tester.assertRenderedPage(ProjectPortfolioPage.class);
 
     tester.isInvisible("loadingProcessPanel");
     tester.assertComponent("detailPanel", ProjectPortfolioDetailsPanel.class);
 
     ListView measureheads = (ListView) tester
         .getComponentFromLastRenderedPage("detailPanel:measureHeads");
     assertEquals("Should be only 4 measure heads.", 4, measureheads.size());
     assertEquals("Check the first measure's display name", "Coverage", tester
         .getComponentFromLastRenderedPage("detailPanel:measureHeads:0:measureName")
         .getModelObjectAsString());
     assertEquals("Check the second measure's display name", "Complexity", tester
         .getComponentFromLastRenderedPage("detailPanel:measureHeads:1:measureName")
         .getModelObjectAsString());
     assertEquals("Check the third measure's display name", "Coupling", tester
         .getComponentFromLastRenderedPage("detailPanel:measureHeads:2:measureName")
         .getModelObjectAsString());
     assertEquals("Check the fourth measure's display name", "Churn", tester
         .getComponentFromLastRenderedPage("detailPanel:measureHeads:3:measureName")
         .getModelObjectAsString());
 
     ListView measures = (ListView) tester
         .getComponentFromLastRenderedPage("detailPanel:projectTable:0:measures");
     assertEquals("Should be only 4 measures there.", 4, measures.size());
     List<String> projectNames = new ArrayList<String>();
     projectNames.add(tester.getComponentFromLastRenderedPage(
         "detailPanel:projectTable:0:projectName").getModelObjectAsString());
     projectNames.add(tester.getComponentFromLastRenderedPage(
         "detailPanel:projectTable:1:projectName").getModelObjectAsString());
     assertTrue("Default should in the detail table", projectNames.contains("Default"));
     assertTrue(testProject + " should in the detail table", projectNames.contains(testProject));
     String testProjectPath = "";
     if (testProject.equals(projectNames.get(0))) {
       testProjectPath = "detailPanel:projectTable:0:";
     }
     else {
       testProjectPath = "detailPanel:projectTable:1:";
     }
     // check value
     assertEquals("Check Coverage value", "50.0", tester.getComponentFromLastRenderedPage(
         testProjectPath + "measures:0:value").getModelObjectAsString());
     assertEquals("Check Complexity value", "3.0", tester.getComponentFromLastRenderedPage(
         testProjectPath + "measures:1:value").getModelObjectAsString());
     assertEquals("Check Coupling value", "N/A", tester.getComponentFromLastRenderedPage(
         testProjectPath + "measures:2:value").getModelObjectAsString());
     assertEquals("Check Churn value", "80.0", tester.getComponentFromLastRenderedPage(
         testProjectPath + "measures:3:value").getModelObjectAsString());
     
     // check inner data.
     MiniBarChart coverage = (MiniBarChart) measures.getList().get(0);
     assertEquals("Check measure name.", "Coverage", coverage.getConfiguration().getName());
     assertTrue("Coverage should be colorable", coverage.getConfiguration().isColorable());
 
     MiniBarChart complexity = (MiniBarChart) measures.getList().get(1);
     assertEquals("Check measure name.", "CyclomaticComplexity", complexity.getConfiguration()
         .getName());
     assertFalse("Complexity should be uncolorable", complexity.getConfiguration().isColorable());
 
   }
 
 
   /**
    * Test the configuration panel.
    */
   @Test
   public void testConfigurationPanel() {
     tester.clickLink(configurationLink);
     tester.assertComponent(configurationPanelPath, ProjectPortfolioConfigurationPanel.class);
 
     String firstHigherThreshold = "measureList:0:higherThreshold";
     String firstlowerThreshold = "measureList:0:lowerThreshold";
 
     // Test validator
     FormTester configurationForm = tester.newFormTester(configurationFormPath);
     configurationForm.setValue(firstHigherThreshold, "10");
     configurationForm.setValue(firstlowerThreshold, "20");
     configurationForm.submit();
     tester.assertErrorMessages(new String[] { "Value of higherThreshold in Coverage "
         + "is not bigger than that of lowerThreshold." });
     // set new value
     configurationForm = tester.newFormTester(configurationFormPath);
     configurationForm.setValue(firstHigherThreshold, "40");
     configurationForm.setValue(firstlowerThreshold, "20");
     configurationForm.submit();
 
     // test persistance
     tester.destroy();
     tester = new WicketTester(new ProjectBrowserApplication(testProperties));
 
     tester.startPage(SigninPage.class);
     // sign in.
     FormTester signinForm = tester.newFormTester("signinForm");
     signinForm.setValue("user", testUserEmail);
     signinForm.setValue("password", testUserEmail);
     signinForm.submit("Signin");
     // go to project portfolio page again.
     tester.clickLink("ProjectPortfolioPageLink");
     tester.assertRenderedPage(ProjectPortfolioPage.class);
     // open configuration panel
     tester.clickLink(configurationLink);
     assertEquals("HigherThreshold should set to default value.", "40", configurationForm
         .getTextComponentValue(firstHigherThreshold));
     assertEquals("LowerThreshold should set to default value.", "20", configurationForm
         .getTextComponentValue(firstlowerThreshold));
 
     // test reset
     configurationForm = tester.newFormTester(configurationFormPath);
     configurationForm.submit("reset");
     configurationForm = tester.newFormTester(configurationFormPath);
     assertEquals("HigherThreshold should set to default value.", defaultCoverageHigherThreshold,
         configurationForm.getTextComponentValue(firstHigherThreshold));
     assertEquals("LowerThreshold should set to default value.", defaultCoverageLowerThreshold,
         configurationForm.getTextComponentValue(firstlowerThreshold));
 
     tester.clickLink("configurationPanel:configurationForm:instructionPopup:showModalWindow");
     tester
         .assertComponentOnAjaxResponse(
             "configurationPanel:configurationForm:instructionPopup:modalWindow");
   }
 
   /**
    * Clear testing data.
    */
   @After
   public void clear() {
     this.clearData(testUserEmail);
   }
 
 }
