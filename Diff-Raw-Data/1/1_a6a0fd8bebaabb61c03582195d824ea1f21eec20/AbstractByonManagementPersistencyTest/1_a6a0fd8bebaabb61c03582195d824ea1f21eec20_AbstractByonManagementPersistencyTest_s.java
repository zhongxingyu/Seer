 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon.persistence;
 
 import iTests.framework.tools.SGTestHelper;
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.IOUtils;
 import iTests.framework.utils.LogUtils;
 import iTests.framework.utils.NetworkUtils;
 import iTests.framework.utils.SSHUtils;
 import iTests.framework.utils.ScriptUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.*;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.bouncycastle.util.IPAddress;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.cloudifysource.quality.iTests.framework.utils.CloudBootstrapper;
 import org.cloudifysource.quality.iTests.framework.utils.ServiceInstaller;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon.AbstractByonCloudTest;
 import org.cloudifysource.restclient.GSRestClient;
 import org.cloudifysource.restclient.RestException;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEvent;
 import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEventListener;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnits;
 import org.openspaces.grid.gsm.machines.plugins.events.MachineStartedEvent;
 
 import com.j_spaces.kernel.PlatformVersion;
 
 /**
  * User: nirb
  * Date: 06/03/13
  */
 public abstract class AbstractByonManagementPersistencyTest extends AbstractByonCloudTest {
 
     private static final String PATH_TO_SERVICE = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/custom-tomcat");
 
     private static final String BOOTSTRAP_SUCCEEDED_STRING = "Successfully created Cloudify Manager";
 
     private static final String APPLICATION_NAME = "default";
 
     private final int numOfManagementMachines = 2;
 
     protected String backupFilePath = SGTestHelper.getBuildDir() + "/backup-details.txt";
 
     private final Map<String, Integer> installedServices = new HashMap<String, Integer>();
 
     private final List<String> attributesList = new LinkedList<String>();
 
     protected void installTomcatService(final int numberOfInstances, final String overrideName) throws IOException, InterruptedException {
 
         copyCustomTomcatToBuild();
 
         try {
 
             // replace number of instances
             File customTomcatGroovy = new File(ScriptUtils.getBuildRecipesServicesPath() + "/custom-tomcat", "tomcat-service.groovy");
             IOUtils.replaceTextInFile(customTomcatGroovy.getAbsolutePath(), "ENTER_NUMBER_OF_INSTANCES", "" + numberOfInstances + "");
 
             // TODO - Once CLOUDIFY-1591 is fixed, use -name option to override a service installation name.
             // replace name if needed
             String actualServiceName;
             if (overrideName != null) {
                 actualServiceName = overrideName;
             } else {
                 actualServiceName = "tomcat";
             }
             IOUtils.replaceTextInFile(customTomcatGroovy.getAbsolutePath(), "ENTER_NAME", actualServiceName);
 
             // install the custom tomcat
             ServiceInstaller tomcatInstaller = new ServiceInstaller(getRestUrl(), actualServiceName);
             tomcatInstaller.recipePath("custom-tomcat");
             tomcatInstaller.timeoutInMinutes(10 * numberOfInstances);
             tomcatInstaller.install();
 
             installedServices.put(actualServiceName, numberOfInstances);
             CloudBootstrapper bootstrapper = getService().getBootstrapper();
             String attributes = bootstrapper.listServiceInstanceAttributes(APPLICATION_NAME, actualServiceName, 1, false);
             attributesList.add(attributes.substring(attributes.indexOf("catalinaHome")));
 
         } finally  {
             deleteCustomTomcatFromBuild();
         }
 
     }
 
     private void copyCustomTomcatToBuild() throws IOException {
         deleteCustomTomcatFromBuild();
         FileUtils.copyDirectoryToDirectory(new File(PATH_TO_SERVICE), new File(ScriptUtils.getBuildRecipesServicesPath()));
     }
 
     private void deleteCustomTomcatFromBuild() throws IOException {
         File customTomcat = new File(ScriptUtils.getBuildRecipesServicesPath(), "custom-tomcat");
         if (customTomcat.exists()) {
             FileUtils.deleteDirectory(customTomcat);
         }
     }
 
 
     /**
      * 1. Shutdown management machines.
      * 2. Bootstrap using the persistence file.
      * 3. Retrieve attributes from space and compare with the ones before the shutdown.
      * 4. Shutdown an instance agent and wait for recovery.
      * @throws Exception
      */
     public void testManagementPersistency() throws Exception {
 
         shutdownManagement();
 
         CloudBootstrapper bootstrapper = getService().getBootstrapper();
         bootstrapper.scanForLeakedNodes(false);
         bootstrapper.useExistingFilePath(backupFilePath);
         bootstrapper.bootstrap();
         bootstrapper.setRestUrl(getRestUrl());
 
         List<String> newAttributesList = new LinkedList<String>();
 
         for (String serviceName : installedServices.keySet()) {
             String attributes = bootstrapper.listServiceInstanceAttributes(APPLICATION_NAME, serviceName, 1, false);
             newAttributesList.add(attributes.substring(attributes.indexOf("catalinaHome")));
         }
 
         List<String> differenceAttributesList = new LinkedList<String>(attributesList);
         differenceAttributesList.removeAll(newAttributesList);
 
         AssertUtils.assertTrue("the service attributes post management restart are not the same as the attributes pre restart", differenceAttributesList.isEmpty());
 
         String serviceToShutdown = installedServices.keySet().iterator().next();
         LogUtils.log("Shutting down GSA that belonges to " + serviceToShutdown);
         final String fullPuName = ServiceUtils.getAbsolutePUName(APPLICATION_NAME, serviceToShutdown);
         ProcessingUnits processingUnits = admin.getProcessingUnits();
         AssertUtils.assertNotNull("Admin Failed to discover any processing units after management shutdown", processingUnits);
         ProcessingUnit pu = processingUnits.waitFor(fullPuName, 20, TimeUnit.SECONDS);
         AssertUtils.assertNotNull("Could not find PU: " + fullPuName, pu);
         final boolean foundInstance = pu.waitFor(1, 20, TimeUnit.SECONDS);
         AssertUtils.assertTrue("Could not find instance of PU: " + fullPuName, foundInstance);
 
         pu.getInstances()[0].getGridServiceContainer().getGridServiceAgent().shutdown();
 
         LogUtils.log("Waiting for service to restart on a new machine");
         final GSRestClient client = new GSRestClient("", "", new URL(getRestUrl()), PlatformVersion.getVersionNumber());
 
         final AtomicReference<String> brokenService = new AtomicReference<String>();
 
         AssertUtils.repetitiveAssertTrue("Service didn't break", new AssertUtils.RepetitiveConditionProvider() {
             @Override
             public boolean getCondition() {
                 try {
 
                     // we don't know which service the agent we shutdown belonged to.
                     // query all installed services to find out.
                     for (String serviceName : installedServices.keySet()) {
                         String serviceRestUrl = "ProcessingUnits/Names/" + APPLICATION_NAME + "." + serviceName;
                         int numberOfInstances = (Integer)client.getAdminData(serviceRestUrl).get("Instances-Size");
                         LogUtils.log("Number of " + serviceName + " instances is " + numberOfInstances);
                         if (numberOfInstances < installedServices.get(serviceName)) {
                             LogUtils.log(serviceName + " service broke. it now has only " + numberOfInstances + " instances");
                             brokenService.set(serviceName);
                         }
                     }
                     return (brokenService.get() != null);
                 } catch (RestException e) {
                     throw new RuntimeException(e);
                 }
 
             }
         } , OPERATION_TIMEOUT * 4);
 
         // now we already know the service that broke.
         // so we wait for it to recover.
         AssertUtils.repetitiveAssertTrue(brokenService.get() + " service did not recover", new AssertUtils.RepetitiveConditionProvider() {
             @Override
             public boolean getCondition() {
                 final String brokenServiceRestUrl = "ProcessingUnits/Names/" + APPLICATION_NAME + "." + brokenService.get();
                 try {
                     int numOfInst = (Integer) client.getAdminData(brokenServiceRestUrl).get("Instances-Size");
                     return (installedServices.get(brokenService.get()) == numOfInst);
 
 /* CLOUDIFY-1602
                     int numOfPlannedInstances = Integer.parseInt((String) client.getAdminData(brokenServiceRestUrl).get("PlannedNumberOfInstances"));
                     return (installedServices.get(brokenService.get()) == numOfPlannedInstances);
 */
 
                 } catch (RestException e) {
                     throw new RuntimeException("caught a RestException", e);
                 }
             }
         } , OPERATION_TIMEOUT * 3);
     }
 
     /**
      * 1. Shutdown management machines.
      * 2. Corrupt the persistence file.
      * 3. Bootstrap with bad file.
      * @throws Exception
      */
     protected void testBadPersistencyFile() throws Exception {
 
         shutdownManagement();
 
         IOUtils.replaceTextInFile(backupFilePath, "instanceId", "instnceId");
 
         CloudBootstrapper bootstrapper = getService().getBootstrapper();
         bootstrapper.useExistingFilePath(backupFilePath);
         bootstrapper.setBootstrapExpectedToFail(true);
         bootstrapper.bootstrap();
 
         String output = bootstrapper.getLastActionOutput();
 
         AssertUtils.assertTrue("bootstrap succeeded with a defective persistence file", !output.contains(BOOTSTRAP_SUCCEEDED_STRING));
     }
 
     /**
      * 1. Shutdown management machines.
      * 2. Bootstrap without persistence file. (Only for DefaultProvisioningDriver)
      * 3. Check management machines are the same.
      * 4. repeat 1-3, 4 times.
      * @throws Exception
      */
     protected void testRepetitiveShutdownManagersBootstrap() throws Exception {
 
         // retrieve the rest url's before we start the chaos.
         final Set<String> originalRestUrls = toSet(getService().getRestUrls());
 
         int repetitions = 4;
 
         for(int i=0; i < repetitions; i++){
 
             shutdownManagement();
 
             bootstrapUsingBackupFile();
 
             String output = getService().getBootstrapper().getLastActionOutput();
 
             AssertUtils.assertTrue("bootstrap failed", output.contains("Successfully created Cloudify Manager"));
 
             // check the rest urls are the same;
             final Set<String> newRestUrls = new HashSet<String>();
             for (URL url : getService().getBootstrapper().getRestAdminUrls()) {
                 newRestUrls.add(url.toString());
             }
             AssertUtils.assertEquals("Expected rest url's not to change after re-bootstrapping", originalRestUrls, newRestUrls);
         }
     }
 
     /**
      * 1. Shutdown management machines.
      * 2. Bootstrap using existing file.
      * 3. Perform manual scale out.
      * 4. make sure a used machine is not being started again.
      * @see "https://cloudifysource.atlassian.net/browse/CLOUDIFY-1724"
      * @throws Exception
      */
     public void testScaleoutAfterRecovery() throws Exception {
 
         final String FULL_SERVICE_NAME = "default.tomcat";
         final CountDownLatch machineStartedLatch = new CountDownLatch(1);
         final AtomicReference<String> machineStarted = new AtomicReference<String>();
         final CountDownLatch installedFinishedLatch = new CountDownLatch(1);
 
         List<String> machinesBeforeRecovery = getAddressesOfService(FULL_SERVICE_NAME);
 
         shutdownManagement();
         bootstrapUsingBackupFile();
 
         List<String> machinesAfterRecovery = getAddressesOfService(FULL_SERVICE_NAME);
 
         AssertUtils.assertEquals("Machines of service tomcat are not the same before and after recovery",
                 machinesAfterRecovery, machinesBeforeRecovery);
         LogUtils.log(FULL_SERVICE_NAME + " was installed on : " + StringUtils.join(machinesAfterRecovery, ","));
 
         // register for machine started events from the ESM
         admin.getMachines().getElasticMachineProvisioningProgressChanged().add(new ElasticMachineProvisioningProgressChangedEventListener() {
             @Override
             public void elasticMachineProvisioningProgressChanged(ElasticMachineProvisioningProgressChangedEvent event) {
                 if (event instanceof MachineStartedEvent) {
                     MachineStartedEvent machineStartedEvent = (MachineStartedEvent) event;
                     String puName = machineStartedEvent.getProcessingUnitName();
                     if (puName.equals(FULL_SERVICE_NAME)) {
                         String hostAddress = machineStartedEvent.getHostAddress();
                         if (!IPAddress.isValidIPv4(hostAddress)) {
                             // hostname and not address
                             try {
                                 hostAddress = NetworkUtils.resolveHostNameToIp(hostAddress);
                             } catch (UnknownHostException e) {
                                 LogUtils.log(ExceptionUtils.getFullStackTrace(e));
                                 hostAddress = null;
                             }
                         }
                         LogUtils.log("A new machine was started for service " + FULL_SERVICE_NAME
                                 + " : " + hostAddress);
 
                         machineStarted.set(hostAddress);
                         machineStartedLatch.countDown();
                     }
                 }
             }
         }, false);
 
         LogUtils.log("Waiting for a request to start a new machine.");
 
         new Thread(new Runnable() {
             @Override
             public void run() {
                 ServiceInstaller installer = new ServiceInstaller(getRestUrl(), "tomcat");
                 installer.recipePath("tomcat");
                 installer.timeoutInMinutes(1); // no need to actually do the install. we just want to trigger the event.
                 installer.setInstances(2);
                 installedFinishedLatch.countDown();
             }
         }).start();
 
         machineStartedLatch.await(OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
         String newlyStartedMachine = machineStarted.get();
 
         // lets wait for the install thread to terminate.
         LogUtils.log("Waiting for tomcat installation thread to terminate");
         installedFinishedLatch.await(90, TimeUnit.SECONDS);
         AssertUtils.assertNotNull("newly started machine is null. this probably means there was an exception", newlyStartedMachine);
         AssertUtils.assertTrue("Machine " + newlyStartedMachine + " was started again even though it is is use",
                 !machinesBeforeRecovery.contains(newlyStartedMachine));
 
     }
 
     protected void bootstrapUsingBackupFile() throws Exception {
         CloudBootstrapper bootstrapper = getService().getBootstrapper();
         bootstrapper.scanForLeakedNodes(false);
         bootstrapper.useExistingFilePath(backupFilePath);
         bootstrapper.bootstrap();
         admin = createAdmin();
     }
 
     protected void shutdownManagement() throws Exception{
 
         CloudBootstrapper bootstrapper = getService().getBootstrapper();
         bootstrapper.setRestUrl(getRestUrl());
 
         LogUtils.log("shutting down managers");
         File persistenceFile = new File(backupFilePath);
         if (persistenceFile.exists()) {
             FileUtils.deleteQuietly(persistenceFile);
         }
         closeAdmin();
         bootstrapper.shutdownManagers("default", backupFilePath, false);
     }
 
     private Set<String> toSet(final String[] array) {
         final Set<String> set = new HashSet<String>();
         Collections.addAll(set, array);
         return set;
     }
 
     private List<String> getAddressesOfService(final String fullServiceName) {
         List<String> existingAddresses = new ArrayList<String>();
         for (Machine m : getProcessingUnitMachines(fullServiceName)) {
             existingAddresses.add(m.getHostAddress());
         }
         return existingAddresses;
     }
 
     public void testCorruptedPersistencyDirectory() throws Exception {
 
         String persistencyFolderPath = getService().getCloud().getConfiguration().getPersistentStoragePath();
         String fileToDeletePath = persistencyFolderPath + "/management-space/db.h2.h2.db";
 
         admin.getGridServiceManagers().waitFor(numOfManagementMachines);
         Iterator<GridServiceManager> gsmIterator = admin.getGridServiceManagers().iterator();
         String machineIp1 = gsmIterator.next().getMachine().getHostAddress();
         String machineIp2 = gsmIterator.next().getMachine().getHostAddress();
         SSHUtils.runCommand(machineIp1, OPERATION_TIMEOUT, "rm -rf " + fileToDeletePath, getService().getUser(), getService().getApiKey());
         SSHUtils.runCommand(machineIp2, OPERATION_TIMEOUT, "rm -rf " + fileToDeletePath, getService().getUser(), getService().getApiKey());
 
         shutdownManagement();
 
         CloudBootstrapper bootstrapper = getService().getBootstrapper();
         bootstrapper.useExistingFilePath(backupFilePath);
         bootstrapper.killJavaProcesses(false);
         bootstrapper.setBootstrapExpectedToFail(true);
         bootstrapper.timeoutInMinutes(7);
         bootstrapper.bootstrap();
 
         String output = bootstrapper.getLastActionOutput();
         AssertUtils.assertTrue("bootstrap succeeded with a corrupted persistency folder", !output.contains(BOOTSTRAP_SUCCEEDED_STRING));
     }
 
     @Override
     protected void customizeCloud() throws Exception {
         super.customizeCloud();
         getService().setNumberOfManagementMachines(numOfManagementMachines);
         getService().getProperties().put("persistencePath", "/tmp/byon/persistency");
     }
 
     @Override
     protected void afterTeardown() throws Exception {
         getService().removePersistencyFolder();
     }
 }
