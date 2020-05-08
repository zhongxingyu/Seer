 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.ec2;
 
 import com.j_spaces.kernel.PlatformVersion;
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.LogUtils;
 import iTests.framework.utils.ScriptUtils;
 import org.cloudifysource.domain.Application;
 import org.cloudifysource.domain.Service;
 import org.cloudifysource.dsl.internal.DSLException;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.NewAbstractCloudTest;
 import org.cloudifysource.restclient.GSRestClient;
 import org.cloudifysource.restclient.RestException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 /**
  * This test concurrently installs an application (NUM_OF_THREADS threads installs at the same time), verifies install
  * was finished successfully and then concurrently uninstalls the application, then again install and uninstall.
  * The management machine's template is SMALL_LINUX but it's attributes (hardwareId,memory) are set as LARGE_LINUX in order to
  * support concurrent installation without crashing.
  */
 public class Ec2ConcurrentInstallUninstallTest extends NewAbstractCloudTest {
 
     private static String applicationName = "petclinic-simple";
    private static final String APP_PATH = ScriptUtils.getBuildRecipesApplicationsPath() + System.getProperty("line.separator") + applicationName;
     private static final int NUM_OF_THREADS = 6;
     private static final int TIMEOUT = 30;
     protected Application application;
 
     @BeforeClass(alwaysRun = true)
     protected void bootstrap() throws Exception {
         super.bootstrap();
     }
 
     @Override
     protected void customizeCloud() throws Exception {
         String oldMemory = "machineMemoryMB 1600";
         String newMemory = "machineMemoryMB 7400";
         getService().getAdditionalPropsToReplace().put(oldMemory, newMemory);
         String oldHardwareId = "hardwareId hardwareId";
         String newHardwareId = "hardwareId \"m1.large\"";
         getService().getAdditionalPropsToReplace().put(oldHardwareId, newHardwareId);
     }
 
     @Override
     protected String getCloudName() {
        return "ec2";
     }
 
     @AfterClass(alwaysRun = true)
     protected void teardown() throws Exception {
         super.teardown();
     }
 
     @Override
     protected boolean isReusableCloud() {
         return false;
     }
 
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 4, enabled = true)
     public void testConcurrentInstallUninstall() throws IOException, DSLException {
         LogUtils.log("Reading Application from file : " + APP_PATH);
         application = ServiceReader.getApplicationFromFile(new File(APP_PATH)).getApplication();
         LogUtils.log("Succesfully read Application : " + application);
         applicationName = application.getName();
         LogUtils.log("Application name is " + applicationName);
         // install and uninstall concurrently 2 times using NUM_OF_THREADS threads
         concurrentInstallOrUninstall(true);
         concurrentInstallOrUninstall(false);
         concurrentInstallOrUninstall(true);
         concurrentInstallOrUninstall(false);
         super.scanForLeakedAgentNodes();
     }
 
     private void concurrentInstallOrUninstall(boolean install) {
         if (install){
             LogUtils.log("Installing the applictaion using " + NUM_OF_THREADS + " threads");
             ExecutorService installService = Executors.newFixedThreadPool(NUM_OF_THREADS);
             List<Future<?>> installFutures = new ArrayList<Future<?>>();
             for (int i=0; i<NUM_OF_THREADS; i++){
                 installFutures.add(installService.submit(new InstallTask(i)));
             }
             LogUtils.log("waiting for threads to finish installing");
             for (Future<?> f:installFutures){
                 try {
                     f.get();
                 } catch (Exception e) {
                     AssertFail("Failed to execute install", e);
                 }
             }
             LogUtils.log("Installed the applictaion!");
         }
         else {
             LogUtils.log("Uninstalling the applictaion using "+NUM_OF_THREADS+" threads");
             ExecutorService uninstallService = Executors.newFixedThreadPool(NUM_OF_THREADS);
             List<Future<?>> uninstallFutures = new ArrayList<Future<?>>();
             for (int i=0; i<NUM_OF_THREADS; i++){
                 uninstallFutures.add(uninstallService.submit(new UninstallTask(i)));
             }
             LogUtils.log("waiting for threads to finish uninstalling");
             for (Future<?> f:uninstallFutures){
                 try {
                     f.get();
                 } catch (Exception e) {
                     AssertFail("Failed to execute uninstall", e);
                 }
             }
             LogUtils.log("Uninstalled the applictaion!");
         }
     }
 
     private void verifyServices(String applicationName, List<Service> services) throws IOException, InterruptedException {
         String command = "connect " + getRestUrl() + ";use-application " + applicationName + ";list-services";
         String output = CommandTestUtils.runCommandAndWait(command);
 
         for(Service singleService : services){
             AssertUtils.assertTrue("the service " + singleService.getName() + " is not running", output.contains(singleService.getName()));
         }
     }
 
     private void verifyApplicationDependencies(final String applicationName, final Application application) throws
             MalformedURLException, RestException {
         final GSRestClient client = new GSRestClient("", "", new URL(getRestUrl()), PlatformVersion.getVersionNumber());
 
         for (Service service : application.getServices()) {
             validateServiceDependency(client, service, applicationName);
         }
     }
 
     private void validateServiceDependency(final GSRestClient client,
                                            final Service service, final String applicationName) throws RestException {
         String absolutePUName = ServiceUtils.getAbsolutePUName(applicationName, service.getName());
         Map<String, Object> adminData = (Map<String, Object>) client.getAdminData(
                 "ProcessingUnits/Names/" + absolutePUName + "/ApplicationDependencies");
 
         String dependencyList = (String) adminData.get("ApplicationDependencies");
         for (String dependency : service.getDependsOn()) {
             String absoluteDependencyName = ServiceUtils.getAbsolutePUName(applicationName, dependency);
             if (!dependencyList.contains(absoluteDependencyName)) {
                 AssertUtils.assertFail("Service " + service.getName() + " does not contain the " +
                         absoluteDependencyName + " dependency");
             }
         }
     }
 
     public class InstallTask implements Runnable{
         private String appName;
 
         public InstallTask (int ind) {
             appName =applicationName + ind;
         }
 
         @Override
         public void run() {
             try {
                 installApplicationAndWait(APP_PATH, appName, TIMEOUT);
                 verifyServices(appName, application.getServices());
                 verifyApplicationDependencies(appName, application);
             } catch (IOException e) {
                 AssertFail("Failed to install",e);
             } catch (InterruptedException e) {
                 AssertFail("Failed to install", e);
             } catch (RestException e) {
                 AssertFail("Failed to install", e);
             }
         }
     }
 
     public class UninstallTask implements Runnable{
 
         public UninstallTask (int ind) {
             index = ind;
         }
 
         private int index;
         @Override
         public void run() {
             try {
                 uninstallApplicationAndWait(applicationName+index, false, TIMEOUT);
             } catch (IOException e) {
                 AssertFail("Failed to uninstall", e);
             } catch (InterruptedException e) {
                 AssertFail("Failed to uninstall", e);
             }
         }
     }
 }
