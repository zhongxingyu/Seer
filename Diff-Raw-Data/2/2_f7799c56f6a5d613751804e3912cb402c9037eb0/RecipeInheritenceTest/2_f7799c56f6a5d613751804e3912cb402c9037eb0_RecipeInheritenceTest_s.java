 package test.cli.cloudify.recipes.inheritence;
 
 import java.io.File;
 import java.io.IOException;
 
 import com.gigaspaces.cloudify.dsl.utils.ServiceUtils;
 import com.gigaspaces.log.*;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.testng.annotations.Test;
 
 import com.gigaspaces.cloudify.dsl.Application;
 import com.gigaspaces.cloudify.dsl.Service;
 import com.gigaspaces.cloudify.dsl.internal.DSLException;
 import com.gigaspaces.cloudify.dsl.internal.ServiceReader;
 import com.gigaspaces.cloudify.dsl.internal.packaging.PackagingException;
 
 import test.cli.cloudify.AbstractLocalCloudTest;
 import test.cli.cloudify.CommandTestUtils;
 
 public class RecipeInheritenceTest extends AbstractLocalCloudTest {
 
     private String tomcatParentPath = CommandTestUtils.getPath("apps/USM/usm/tomcatHttpLivenessDetectorPlugin");
     private Application app;
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void simpleInheritenceTest() throws IOException, PackagingException, InterruptedException, DSLException {
         String appChildDirPath = CommandTestUtils.getPath("apps/USM/usm/applications/travelExtended");
 
         Service tomcatParent = ServiceReader.readService(new File(tomcatParentPath));
         installApplication(appChildDirPath);
         Service s1 = app.getServices().get(0);
         Service s2 = app.getServices().get(1);
         Service tomcat = s1.getName().equals("tomcat-extend") ? s1 : s2;
 
         int tomcatParentPort, tomcatChildPort;
         tomcatChildPort = tomcat.getNetwork().getPort();
         tomcatParentPort = tomcatParent.getNetwork().getPort();
 
         assertEquals("tomcat port isn't equal to the tomcat's parent port", tomcatChildPort, tomcatParentPort);
         //uninstallService("tomcatHttpLivenessDetectorPlugin");
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void overrideTomcatPortTest() throws PackagingException, IOException, InterruptedException, DSLException {
         String appChildDirPath = CommandTestUtils.getPath("apps/USM/usm/applications/travelExtendedTomcatPortOverride");
         installApplication(appChildDirPath);
         Service s1 = app.getServices().get(0);
         Service s2 = app.getServices().get(1);
         Service tomcat = s1.getName().equals("tomcat") ? s1 : s2;
         int tomcatChildPort = tomcat.getNetwork().getPort();
         assertEquals("tomcat's child port was not overriden", 9876, tomcatChildPort);
         assertTrue(ServiceUtils.isPortOccupied(9876));
         assertTrue(ServiceUtils.isPortFree(8080));
         //uninstallApplication("travelExtendedTomcatPortOverride");
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = false)
     public void overrideTomcatNumInstancesTest() throws PackagingException, IOException, InterruptedException, DSLException {
         String appChildDirPath = CommandTestUtils.getPath("apps/USM/usm/applications/travelExtendedTomcatNumInstancesOverride");
         installApplication(appChildDirPath);
 
         int tomcatInstances = admin.getProcessingUnits().getProcessingUnit("travelExtendedTomcatNumInstancesOverride.tomcat").getInstances().length;
         assertEquals("tomcat instances where overriden to be 3", 3, tomcatInstances);
         //uninstallApplication("travelExtendedTomcatNumInstancesOverride");
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void overrideCassandraInitFileTest() throws PackagingException, IOException, InterruptedException, DSLException {
         String EXPECTED_PROCESS_PRINTOUTS = "THIS IS OVERRIDED CASSANDRA_POSTSTART.GROOVY";
         String appChildDirPath = CommandTestUtils.getPath("apps/USM/usm/applications/travelExtended");
         installApplication(appChildDirPath);
 
         ProcessingUnitInstance cassandraInstance = admin.getProcessingUnits().getProcessingUnit("travelExtended.cassandra-extend").getInstances()[0];
         long pid = cassandraInstance.getGridServiceContainer().getVirtualMachine().getDetails().getPid();
 
         ContinuousLogEntryMatcher matcher = new ContinuousLogEntryMatcher(new AllLogEntryMatcher(), new AllLogEntryMatcher());
 
         sleep(5000);
         assertTrue(checkForOverrideString(cassandraInstance, pid, matcher, EXPECTED_PROCESS_PRINTOUTS));
         //uninstallApplication("travelExtended");
     }
 
     private void installApplication(String appDirPath) throws PackagingException, IOException, InterruptedException, DSLException {
         File applicationDir = new File(appDirPath);
         app = ServiceReader.getApplicationFromFile(applicationDir).getApplication();
 
        String output = runCommand("connect " + this.restUrl + ";install-application --verbose " + appDirPath);
         assertTrue("couldn't install application", output.contains("installed successfully"));
     }
 
     private boolean checkForOverrideString(ProcessingUnitInstance pui,
                                            long pid, ContinuousLogEntryMatcher matcher, final String expectedValue) {
         LogEntries entries = pui.getGridServiceContainer()
                 .getGridServiceAgent()
                 .logEntries(LogProcessType.GSC, pid, matcher);
         for (LogEntry logEntry : entries) {
             String text = logEntry.getText();
             if (text.contains(expectedValue)) {
                 return true;
             }
         }
         return false;
     }
 }
