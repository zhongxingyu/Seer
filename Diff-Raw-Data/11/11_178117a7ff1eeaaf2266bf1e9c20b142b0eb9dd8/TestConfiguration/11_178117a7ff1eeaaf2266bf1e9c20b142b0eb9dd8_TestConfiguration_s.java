 package org.jenkinsci.plugins.periodicreincarnationtest;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import jenkins.model.Jenkins;
 
 import antlr.ANTLRException;
 import hudson.model.Job;
 import hudson.model.Result;
 
 import org.jenkinsci.plugins.periodicreincarnation.PeriodicReincarnation;
 import org.jenkinsci.plugins.periodicreincarnation.PeriodicReincarnationBuildCause;
 import org.jenkinsci.plugins.periodicreincarnation.PeriodicReincarnationGlobalConfiguration;
 import org.junit.Assert;
 import org.junit.Rule;
 import org.junit.Test;
 import org.jvnet.hudson.test.JenkinsRule;
 import org.jvnet.hudson.test.recipes.LocalData;
 import org.xml.sax.SAXException;
 
 import com.gargoylesoftware.htmlunit.html.HtmlElement;
 import com.gargoylesoftware.htmlunit.html.HtmlForm;
 import com.gargoylesoftware.htmlunit.html.HtmlInput;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.html.HtmlButton;
 import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
 
 /**
  * Test class.
  * 
  * @author yboev
  * 
  */
 public class TestConfiguration {
     @Rule
     public JenkinsRule jenkinsRule = new JenkinsRule();
     /**
      * The global configuration.
      */
     private PeriodicReincarnationGlobalConfiguration config;
     /**
      * The HTML form.
      */
     private HtmlForm form;
 
     /**
      * TestCase for Periodic Reincarnation. Populates the configuration with
      * values and submits it. Checks if the values have been populated right and
      * waits for the PeriodicReincarnation to start restarting jobs.
      * 
      * *** Sometimes the sleep is interrupted and the execution ends without
      * restarting jobs. This leaves some code unexecuted and unchecked, but
      * happens very rarely. Reason - unknown.
      * 
      * @throws Exception
      *             exception.
      */
     @LocalData
     @Test
     public void test1() throws Exception {
         checkPeriodAndCause();
         checkLoadedJobs();
         this.getGlobalForm();
         setConfigurationProperties();
 
         // submit all populated values
         jenkinsRule.submit(this.form);
 
         try {
             TimeUnit.SECONDS.sleep(50);
         } catch (InterruptedException e) {
             // we have been interrupted and test will probably fail due to lack
             // of time.
         }
 
         checkConfiguredValues();
         try {
             TimeUnit.SECONDS.sleep(100);
         } catch (InterruptedException e) {
             // we have been interrupted and test will probably fail due to lack
             // of time.
         }
         assertEquals(2, this.config.getMaxDepth());
 
     }
 
     private void checkPeriodAndCause() {
         final long reccurancePeriod = 60000;
         assertNotNull(PeriodicReincarnation.get());
         final String s = "reg ex hit";
         assertEquals("Periodic Reincarnation - " + s,
                 new PeriodicReincarnationBuildCause(s).getShortDescription());
         assertEquals(reccurancePeriod, PeriodicReincarnation.get()
                 .getRecurrencePeriod());
     }
 
     private void checkConfiguredValues() {
         config = PeriodicReincarnationGlobalConfiguration.get();
         assertNotNull(config);
         assertEquals("* * * * *", config.getCronTime());
         try {
             config.doCheckCronTime(config.getCronTime());
         } catch (ANTLRException e) {
             Assert.fail();
         } catch (NullPointerException e2) {
             Assert.fail();
         }
 
         assertEquals("true", config.getActiveCron());
         assertTrue(config.isCronActive());
         assertTrue(config.isTriggerActive());
         assertEquals("true", config.getActiveCron());
         assertEquals("true", config.getActiveTrigger());
         assertEquals(1, config.getRegExprs().size());
         assertEquals("test", config.getRegExprs().get(0).getValue());
         assertEquals("true", config.getNoChange());
         assertTrue(config.isRestartUnchangedJobsEnabled());
     }
 
     /**
     * Sets the fields in the config(global config).
      * 
      * @throws IOException
      */
     private void setConfigurationProperties() throws IOException {
         final HtmlTextInput cronTime = form.getInputByName("_.cronTime");
         assertNotNull("Cron Time is null!", cronTime);
         cronTime.setValueAttribute("* * * * *");
 
         final HtmlInput activeCron = form.getInputByName("_.activeCron");
         assertNotNull("EnableDisable cron field is null!", activeCron);
         activeCron.setChecked(true);
 
         final HtmlInput activeTrigger = form.getInputByName("_.activeTrigger");
         assertNotNull("EnableDisable trigger field is null!", activeTrigger);
         activeTrigger.setChecked(true);
 
         final HtmlTextInput maxDepth = form.getInputByName("_.maxDepth");
         assertNotNull("MaxDepth field is null!", maxDepth);
         maxDepth.setValueAttribute("2");
 
         final HtmlInput noChange = form.getInputByName("_.noChange");
         assertNotNull("NoChange checkbox was null!", noChange);
         noChange.setChecked(true);
 
         final HtmlElement regExprs = (HtmlElement) form.getByXPath(
                 "//tr[td='Regular Expressions']").get(0);
         assertNotNull("RegExprs is null!", regExprs);
         assertNotNull("Add button not found",
                 regExprs.getFirstByXPath(".//button"));
         ((HtmlButton) regExprs.getFirstByXPath(".//button")).click();
         final HtmlTextInput regEx1 = (HtmlTextInput) regExprs
                 .getFirstByXPath("//input[@name='" + "regExprs.value" + "']");
         assertNotNull("regEx1 is null!", regEx1);
         regEx1.setValueAttribute("test");
 
         final HtmlTextInput nodeAction = (HtmlTextInput) regExprs
                 .getFirstByXPath("//input[@name='" + "regExprs.nodeAction"
                         + "']");
         assertNotNull("nodeAction is null!", nodeAction);
         nodeAction.setValueAttribute("echo 123");
 
         final HtmlTextInput masterAction = (HtmlTextInput) regExprs
                 .getFirstByXPath("//input[@name='" + "regExprs.masterAction"
                         + "']");
         assertNotNull("masterAction is null!", masterAction);
         masterAction.setValueAttribute("echo 123");
     }
 
     /**
      * checks if jobs are loaded by @LocalData.
      */
     private void checkLoadedJobs() {
         final Job<?, ?> job1 = (Job<?, ?>) Jenkins.getInstance().getItem(
                 "test_job");
         assertNotNull("job missing.. @LocalData problem?", job1);
 
         assertEquals(Result.FAILURE, job1.getLastBuild().getResult());
         System.out.println("JOB1 LOG:"
                 + job1.getLastBuild().getLogFile().toString());
 
         final Job<?, ?> job2 = (Job<?, ?>) Jenkins.getInstance().getItem(
                 "no_change");
         assertNotNull("job missing.. @LocalData problem?", job2);
         assertEquals(Result.FAILURE, job2.getLastBuild().getResult());
         assertNotNull(job2.getLastSuccessfulBuild());
     }
 
     /**
      * Finds and sets the global form. Also makes a couple of test to see if
      * everything is correct.
      * 
      * @throws IOException
      *             IO error
      * @throws SAXException
      *             SAX error
      */
     private void getGlobalForm() throws IOException, SAXException {
         final HtmlPage page = jenkinsRule.createWebClient().goTo("configure");
         final String allElements = page.asText();
 
         assertTrue(allElements.contains("Cron Time"));
         assertTrue(allElements.contains("Periodic Reincarnation"));
         assertTrue(allElements.contains("Max restart depth"));
         assertTrue(allElements.contains("Enable afterbuild job reincarnation"));
         assertTrue(allElements.contains("Regular Expressions"));
         assertTrue(allElements.contains("Enable cron job reincarnation"));
         assertTrue(allElements
                 .contains("Restart unchanged projects failing for the first time"));
 
         this.form = page.getFormByName("config");
         assertNotNull("Form is null!", this.form);
     }
 
 }
