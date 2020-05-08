 package org.hackystat.projectbrowser.page.telemetry;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import javax.xml.datatype.XMLGregorianCalendar;
 import org.apache.wicket.Component;
 import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.ListMultipleChoice;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.util.tester.FormTester;
 import org.apache.wicket.util.tester.WicketTester;
 import org.hackystat.projectbrowser.ProjectBrowserApplication;
 import org.hackystat.projectbrowser.ProjectBrowserProperties;
 import org.hackystat.projectbrowser.authentication.SigninPage;
 import org.hackystat.projectbrowser.page.loadingprocesspanel.LoadingProcessPanel;
 import org.hackystat.projectbrowser.page.telemetry.datapanel.TelemetryDataPanel;
 import org.hackystat.projectbrowser.page.telemetry.inputpanel.TelemetryDescriptionPanel;
 import org.hackystat.projectbrowser.page.telemetry.inputpanel.TelemetryInputPanel;
 import org.hackystat.projectbrowser.test.ProjectBrowserTestHelper;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.telemetry.service.resource.chart.jaxb.TelemetryChartDefinition;
 import org.hackystat.utilities.tstamp.Tstamp;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Tests for Telemetry page.
  * @author Shaoxuan Zhang
  */
 public class TestTelemetryPage extends ProjectBrowserTestHelper {
   /** the test user. */
   private String testUser = "TestTelemetryUser";
   /** email of the test user. */
   private String testUserEmail = "TestTelemetryUser@hackystat.org";
   /** the test project. */
   private String testProject = "TestTelemetryProject";
   /** String of telemetry, for properties. */
   private String telemetry = ".telemetry";
   /** String of true. */
   private String trueString = "true";
   /** String of false. */
   private String falseString = "false";
   /** String of signinForm. */
   private String signinFormKey = "signinForm";
   /** String of user. */
   private String userKey = "user";
   /** String of password. */
   private String passwordKey = "password";
   /** String of Signin. */
   private String signinButtonKey = "Signin";
   /**
    * Initialize data for testing.
    */
   @Before
   public void setUp() {
     this.generateSimData(testUser, testProject, Tstamp.makeTimestamp(), 0);
   }
   
   /**
    * Test the daily project data page.
    * From opening the page to showing the telemetry chart.
    * Most information during navigation is checked.
    */
   @Test 
   public void testTelemetryPageNormalNavigation() {
     this.generateSimData(testUser, testProject, Tstamp.makeTimestamp(), 3);
 
     Properties testProperties = getTestProperties();
     testProperties.put(ProjectBrowserProperties.AVAILABLEPAGE_KEY + telemetry, trueString);
     testProperties.put(ProjectBrowserProperties.BACKGROUND_PROCESS_KEY + telemetry, falseString);
     WicketTester tester = new WicketTester(new ProjectBrowserApplication(testProperties));
     tester.setupRequestAndResponse();
     
     tester.startPage(SigninPage.class); 
     // Let's sign in.
     FormTester signinForm = tester.newFormTester(signinFormKey);
     signinForm.setValue(userKey, testUserEmail);
     signinForm.setValue(passwordKey, testUserEmail);
     signinForm.submit(signinButtonKey);
     //first, go to daily project data page.
     tester.clickLink("TelemetryPageLink");
     tester.assertRenderedPage(TelemetryPage.class);
     
     tester.assertComponent("inputPanel", TelemetryInputPanel.class);
     
     FormTester inputForm = tester.newFormTester("inputPanel:inputForm");
     //checkt the date field.
     inputForm.setValue("endDateTextField", getDateBeforeAsString(2));
     inputForm.setValue("startDateTextField", getDateBeforeAsString(5));
 
     //check telemetry choices.
     Component telemetryComponent = inputForm.getForm().get("telemetryMenu");
     assertTrue("Check telemetry select field", telemetryComponent instanceof DropDownChoice);
     DropDownChoice telemetryChoice = (DropDownChoice) telemetryComponent;
     assertFalse("Telemetry should not be null", telemetryChoice.getChoices().isEmpty());
     String telemetryName = (String)telemetryChoice.getModelObjectAsString();
     //inputForm.select("telemetryMenu", 1);
 
     //check the project list content.
     Component projectComponent = inputForm.getForm().get("projectMenu");
     assertTrue("Check project select field", projectComponent instanceof ListMultipleChoice);
     ListMultipleChoice projectChoice = (ListMultipleChoice) projectComponent;
     int defaultIndex = -1;
     int testProjectIndex = -1;
     for (int i = 0; i < projectChoice.getChoices().size(); i++) {
       Project project = (Project)projectChoice.getChoices().get(i);
       if ("Default".equals(project.getName())) {
         defaultIndex = i;
       }
       else if (testProject.equals(project.getName())) {
         testProjectIndex = i;
       }
     }
     if (defaultIndex < 0) {
       fail("Default project not found in project list.");
     }
     if (testProjectIndex < 0) {
       fail(testProject + " not found in project list.");
     }
     //select those choices.
     inputForm.selectMultiple("projectMenu", new int[]{defaultIndex, testProjectIndex});
     
     inputForm.submit("submit");
     //check the result.
     tester.assertRenderedPage(TelemetryPage.class);
     tester.assertComponent("dataPanel", TelemetryDataPanel.class);
     tester.assertLabel("dataPanel:telemetryName", telemetryName);
     //chart image should be empty initially.
     tester.assertInvisible("dataPanel:selectedChart");
     
     FormTester streamForm = tester.newFormTester("dataPanel:streamForm");
     
     Component c1 = streamForm.getForm().get("dateList");
     assertTrue("dateList should be ListView", c1 instanceof ListView);
     ListView dateList = (ListView) c1;
     assertEquals("There should be 4 dates in the table.", 4, dateList.getList().size());
 
     Component c2 = streamForm.getForm().get("projectTable");
     assertTrue("dateList should be ListView", c2 instanceof ListView);
     ListView projectTable = (ListView) c2;
     assertEquals("There should be 2 projects in the table.", 2, projectTable.getList().size());
     List projects = projectTable.getList();
     List<String> projectNames = new ArrayList<String>();
     projectNames.add(((Project)projects.get(0)).getName());
     projectNames.add(((Project)projects.get(1)).getName());
     assertTrue("Default project missed", projectNames.contains("Default"));
     assertTrue("testProject project missed", projectNames.contains(testProject));
 
     //test selected chart display.
     streamForm.setValue("projectTable:0:projectStream:0:streamCheckBox", String.valueOf(true));
     streamForm.setValue("projectTable:1:projectStream:0:streamCheckBox", String.valueOf(true));
     streamForm.submit();
    tester.assertVisible("dataPanel:selectedChart");
     String selectedChartUrl = tester.getTagByWicketId("selectedChart").getAttribute("src");
     assertTrue("chart image should be displayed now.", 
         selectedChartUrl.contains("http://chart.apis.google.com/chart?"));
   }
 
   /**
    * Test telemetry page with background process.
    * The loading panel will show probably.
    * The cancel button is working correctly.
    */
   @Test
   public void testTelemetryPageBackgroundProcess() {
     Properties testProperties = getTestProperties();
     testProperties.put(ProjectBrowserProperties.AVAILABLEPAGE_KEY + telemetry, trueString);
     testProperties.put(ProjectBrowserProperties.BACKGROUND_PROCESS_KEY + telemetry, trueString);
     WicketTester tester = new WicketTester(new ProjectBrowserApplication(testProperties));
     tester.setupRequestAndResponse();
     
     tester.startPage(SigninPage.class); 
     // Let's sign in.
     FormTester signinForm = tester.newFormTester(signinFormKey);
     signinForm.setValue(userKey, testUserEmail);
     signinForm.setValue(passwordKey, testUserEmail);
     signinForm.submit(signinButtonKey);
     //first, go to daily project data page.
     tester.clickLink("TelemetryPageLink");
     tester.assertRenderedPage(TelemetryPage.class);
     tester.assertComponent("inputPanel", TelemetryInputPanel.class);
     
     FormTester inputForm = tester.newFormTester("inputPanel:inputForm");
     //check telemetry choices.
     Component telemetryComponent = inputForm.getForm().get("telemetryMenu");
     assertTrue("Check telemetry select field", telemetryComponent instanceof DropDownChoice);
     DropDownChoice telemetryChoice = (DropDownChoice) telemetryComponent;
     assertFalse("Telemetry should not be null", telemetryChoice.getChoices().isEmpty());
     String telemetryName = (String)telemetryChoice.getModelObjectAsString();
     inputForm.submit("submit");
     
     //check the result.
     tester.assertRenderedPage(TelemetryPage.class);
     //loadingProcessPanel should be visible.
     tester.assertComponent("loadingProcessPanel", LoadingProcessPanel.class);
     //dataPanel should be invisible.
     tester.assertInvisible("dataPanel");
     //Check message in loadingProcessPanel
     String msg = tester.getComponentFromLastRenderedPage("loadingProcessPanel:processingMessage").
     getModelObjectAsString();
     assertTrue("message error in loading panel", 
         msg.contains("Retrieving telemetry") && msg.contains(telemetryName));
     //check the cancel button.
     FormTester cancelForm = tester.newFormTester("inputPanel:cancelForm");
     cancelForm.submit("cancel");
     assertTrue("Check process cancelled message."
         , tester.getComponentFromLastRenderedPage("loadingProcessPanel:processingMessage").
         getModelObjectAsString().contains("Process Cancelled."));
   }
   
   /**
    * Test pop up windows in telemetry page.
    * All choices in telemetry menu should be in the description pop up window.
    */
   @Test
   public void testTelemetryPopUpPanel() {
     Properties testProperties = getTestProperties();
     testProperties.put(ProjectBrowserProperties.AVAILABLEPAGE_KEY + telemetry, trueString);
     testProperties.put(ProjectBrowserProperties.BACKGROUND_PROCESS_KEY + telemetry, falseString);
     WicketTester tester = new WicketTester(new ProjectBrowserApplication(testProperties));
     tester.setupRequestAndResponse();
     
     tester.startPage(SigninPage.class); 
     // Let's sign in.
     FormTester signinForm = tester.newFormTester(signinFormKey);
     signinForm.setValue(userKey, testUserEmail);
     signinForm.setValue(passwordKey, testUserEmail);
     signinForm.submit(signinButtonKey);
     //first, go to daily project data page.
     tester.clickLink("TelemetryPageLink");
     tester.assertRenderedPage(TelemetryPage.class);
     tester.assertComponent("inputPanel", TelemetryInputPanel.class);
     
     FormTester inputForm = tester.newFormTester("inputPanel:inputForm");
     //get telemetry choices.
     Component telemetryComponent = inputForm.getForm().get("telemetryMenu");
     assertTrue("Check telemetry select field", telemetryComponent instanceof DropDownChoice);
     DropDownChoice telemetryChoice = (DropDownChoice) telemetryComponent;
     assertFalse("Telemetry should not be null", telemetryChoice.getChoices().isEmpty());
     List choices = telemetryChoice.getChoices();
     //click out the pop up window
     tester.clickLink("inputPanel:inputForm:chartDefPopup:showModalWindow");
     tester.assertComponentOnAjaxResponse("inputPanel:inputForm:chartDefPopup:modalWindow");
     ModalWindow modalWindow = (ModalWindow) 
       tester.getComponentFromLastRenderedPage("inputPanel:inputForm:chartDefPopup:modalWindow");
     //get the description list
     TelemetryDescriptionPanel p = (TelemetryDescriptionPanel)
                                         modalWindow.get(modalWindow.getContentId());
     ListView listView = (ListView)p.get("descriptions");
     List<String> telemetryInDescriptions = new ArrayList<String>();
     for (Object o : listView.getList()) {
       telemetryInDescriptions.add(((TelemetryChartDefinition)o).getName());
     }
     
     assertTrue("All choices in telemetry menu should be in the description list.", 
         telemetryInDescriptions.containsAll(choices));
     //assertTrue(choices.containsAll(telemetryInDescriptions));
   }
 
   /**
    * Test telemetry page with page parameters.
    */
   /*
   @Test
   public void testTelemetryWithPageParamters() {  //NOPMD WicketTester has its own assert classes.
     Properties testProperties = getTestProperties();
     testProperties.put(ProjectBrowserProperties.AVAILABLEPAGE_KEY + telemetry, trueString);
     testProperties.put(ProjectBrowserProperties.BACKGROUND_PROCESS_KEY + telemetry, falseString);
     WicketTester tester = new WicketTester(new ProjectBrowserApplication(testProperties));
     tester.setupRequestAndResponse();
     
     
     //but still need to sign in first.
     tester.startPage(SigninPage.class);
     
     FormTester signinForm = tester.newFormTester(signinFormKey);
     signinForm.setValue(userKey, testUserEmail);
     signinForm.setValue(passwordKey, testUserEmail);
     signinForm.submit(signinButtonKey);
     
     //TODO add test.
     PageParameters param = new PageParameters("Build/Day/2008-05-24/2008-06-02/" + 
         this.testProject + "-" + this.testUser + "/*,*,*,*,false");
     tester.startPage(TelemetryPage.class, param); 
     tester.assertRenderedPage(TelemetryPage.class);
     
   }
   */
   
   /**
    * Clear testing data.
    */
   @After
   public void clear() {
     this.clearData(testUserEmail);
   }
   
   /**
    * return a String that represent a date before today.
    * @param i the number of days before today.
    * @return a String represent today.
    */
   public String getDateBeforeAsString(int i) {
     XMLGregorianCalendar time = Tstamp.incrementDays(Tstamp.makeTimestamp(), -i);
     String timeString = time.getYear() + "-";
     timeString += (time.getMonth() >= 10) ? time.getMonth() : "0" + time.getMonth();
     timeString += "-";
     timeString += (time.getDay() >= 10) ? time.getDay() : "0" + time.getDay();
     return timeString;
   }
 }
