 package com.usulful.jenkins.test;
 
 import com.usulful.jenkins.RestApi;
 import com.usulful.jenkins.page.MainPage;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.support.PageFactory;
 
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 public class PerformanceIntegrationTest {
 
     private static final String MAIN_URL = "http://localhost:9080/";
     public static final String DEFAULT_JOB_NAME = "Jenkins_Acceptance_Tests";
     private static final String JOB_NAME = System.getProperty("jobName", DEFAULT_JOB_NAME);
     private WebDriver driver;
 
     @Test
     public void shouldShowPerformanceOfJenkinsAtsLevel1() {
         openMainPage()
                 .waitForInitialization()
                 .clickNewJob()
                 .enterJobName(JOB_NAME)
                 .freestyleBuild()
                 .clickOK()
                         //job configuration
                 .scm("Git")
                 .enter("git.repo.url", "https://github.com/usultis/at-performance-report.git")
                 .addBuildStep("Invoke top-level Maven targets")
                 .addPostBuildStep("Publish Performance test result report")
                 .clickButton("Add a new report")
                 .clickLinkWithText("JMeter")
                 .enter("_.glob", "**/*.jtl")
                 .clickMavenAdvanced()
                 .buildProperties(
                        "jetty.skip=true\n" +
                                 "it.test=PerformanceIntegrationTest#shouldShowPerformanceOfJenkinsAtsLevel2\n" +
                                 "jobName=" + JOB_NAME + "Level2")
                 .goals("verify")
                 .save()
                         //main page
                 .openJob(JOB_NAME)
                 .buildNow()
                 .openConsole()
                 .waitForBuildToFinish()
                 .backToProject()
                 .performanceTrend()
                 .clickLastReport()
                 .assertHasEntry("JobConfigurationPage.save");
     }
 
     @Test
     public void shouldShowPerformanceOfJenkinsAtsLevel2() {
         openMainPage()
                 .clickNewJob()
                 .enterJobName(JOB_NAME)
                 .freestyleBuild()
                 .clickOK()
                         //job configuration
                 .scm("Git")
                 .enter("git.repo.url", "https://github.com/usultis/at-performance-report.git")
                 .addBuildStep("Invoke top-level Maven targets")
                 .addPostBuildStep("Publish Performance test result report")
                 .clickButton("Add a new report")
                 .clickLinkWithText("JMeter")
                 .enter("_.glob", "**/*.jtl")
                 .clickMavenAdvanced()
                 .buildProperties("jetty.skip=true")
                 .goals("verify")
                 .save()
                         //main page
                 .openJob(JOB_NAME)
                 .assertTitleContains(JOB_NAME);
     }
 
     private MainPage openMainPage() {
         return mainPage();
     }
 
     private MainPage mainPage() {
         driver.get(MAIN_URL);
         return PageFactory.initElements(driver, MainPage.class);
     }
 
     @Before
     public void before() throws IOException {
         new RestApi(MAIN_URL).deleteJob(JOB_NAME);
         driver = new FirefoxDriver();
         driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
     }
 
     @After
     public void after() throws IOException {
        driver.quit();
        new RestApi(MAIN_URL).stopBuild(JOB_NAME, 1);
         new RestApi(MAIN_URL).deleteJob(JOB_NAME);
     }
 }
