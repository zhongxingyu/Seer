 package org.cloudifysource.quality.iTests.framework.utils.storage;
 
 import com.j_spaces.kernel.PlatformVersion;
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.dsl.Service;
 import org.cloudifysource.dsl.context.blockstorage.LocalStorageOperationException;
 import org.cloudifysource.dsl.internal.DSLException;
 import org.cloudifysource.dsl.internal.DSLReader;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.cloudifysource.dsl.internal.packaging.PackagingException;
 import org.cloudifysource.esc.driver.provisioning.MachineDetails;
 import org.cloudifysource.esc.driver.provisioning.storage.VolumeDetails;
 import org.cloudifysource.quality.iTests.framework.utils.*;
 import org.cloudifysource.quality.iTests.framework.utils.compute.ComputeApiHelper;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.CloudService;
 import org.cloudifysource.restclient.GSRestClient;
 import org.cloudifysource.restclient.RestException;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicReference;
 
 /**
  *
  * Provides some generic test cases to be used by tests that run on different clouds.
  * Created with IntelliJ IDEA.
  * User: elip
  * Date: 4/10/13
  * Time: 5:48 PM
  * To change this template use File | Settings | File Templates.
  */
 public class StorageAllocationTester {
 
     private String restUrl;
     private StorageApiHelper storageApiHelper;
     private CloudService cloudService;
     private ComputeApiHelper computeApiHelper;
 
     public RecipeInstaller getInstaller() {
         return installer;
     }
 
     private RecipeInstaller installer;
 
     public StorageAllocationTester(String restUrl, StorageApiHelper storageApiHelper, CloudService cloudService, ComputeApiHelper computeApiHelper) {
         this.restUrl = restUrl;
         this.storageApiHelper = storageApiHelper;
         this.cloudService = cloudService;
         this.computeApiHelper = computeApiHelper;
     }
 
     private static final String RECIPES_SERVICES_FOLDER = ScriptUtils.getBuildRecipesServicesPath();
 
     /* Test Methods to be called by test classes */
 
     public void testStorageVolumeMountedLinux() throws Exception {
         String folderName = "simple-storage-with-custom-commands";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/staticstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_LINUX", true);
         testStorageVolumeMounted(folderName);
     }
 
     public void testStorageVolumeMountedUbuntu() throws Exception {
         String folderName = "simple-storage-with-custom-commands";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/staticstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_UBUNTU", false);
         testStorageVolumeMounted(folderName);
     }
 
     public void testWriteToStorageLinux() throws Exception {
         String folderName = "simple-storage-with-custom-commands";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/staticstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_LINUX", true);
         testWriteToStorage(folderName);
     }
 
     public void testWriteToStorageUbuntu() throws Exception {
         String folderName = "simple-storage-with-custom-commands";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/staticstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_UBUNTU", false);
         testWriteToStorage(folderName);
     }
 
     public void testMountLinux() throws Exception {
         String folderName = "simple-storage-with-custom-commands";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/staticstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_LINUX", true);
         testMount(folderName);
     }
 
     public void testMountUbuntu() throws Exception {
         String folderName = "simple-storage-with-custom-commands";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/staticstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_UBUNTU", false);
         testMount(folderName);
     }
 
     public void testInstallWithStorageLinux() throws Exception {
         String folderName = "simple-storage";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/staticstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_LINUX", true);
         testInstallWithStorage(folderName);
     }
 
     public void testInstallWithStorageUbuntu() throws Exception {
         String folderName = "simple-storage";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/staticstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_UBUNTU", false);
         testInstallWithStorage(folderName);
     }
 
     public void testInstallWithDynamicStorageLinux() throws Exception {
         String folderName = "create-and-attach";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/dynamicstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_LINUX", true);
         testInstallWithStorage(folderName);
     }
 
     public void testInstallWithDynamicStorageUbuntu() throws Exception {
         String folderName = "create-and-attach";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/dynamicstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_UBUNTU", false);
         testInstallWithStorage(folderName);
     }
 
     public void testDeleteOnExitFalseLinux() throws Exception {
         String folderName = "simple-storage";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/staticstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_LINUX", true);
         testDeleteOnExitFalse(folderName);
     }
 
     public void testDeleteOnExitFalseUbuntu() throws Exception {
         String folderName = "simple-storage";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/staticstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_UBUNTU", false);
         testDeleteOnExitFalse(folderName);
     }
 
     public void testFaultyInstallLinux() throws Exception {
         String folderName = "faulty-install";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/staticstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_LINUX", true);
         testFaultyInstall(folderName);
     }
 
     public void testFaultyInstallUbuntu() throws Exception {
         String folderName = "faulty-install";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/staticstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_UBUNTU", false);
         testFaultyInstall(folderName);
     }
 
     public void testFailedToAttachLinux() throws Exception {
         String folderName = "simple-storage";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/staticstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_LINUX", true);
         testFailedToAttach(folderName);
     }
 
     public void testFailedToAttachUbuntu() throws Exception {
         final String folderName = "simple-storage";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/staticstorage/" + folderName);
         copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_UBUNTU", false);
         testFailedToAttach(folderName);
     }
 
     public void testTwoTemplates() throws Exception {
 
         installer = new ApplicationInstaller(restUrl, "groovy-App");
         installer.recipePath(CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/staticstorage/groovyApp"));
         installer.install();
 
         final String groovy1VolumePrefix = cloudService.getCloud().getCloudStorage().getTemplates().get("GROOVY1").getNamePrefix();
         LogUtils.log("Volume prefix for groovy1 is " + groovy1VolumePrefix);
         final String groovy2VolumePrefix = cloudService.getCloud().getCloudStorage().getTemplates().get("GROOVY2").getNamePrefix();
         LogUtils.log("Volume prefix for groovy2 is " + groovy2VolumePrefix);
 
         LogUtils.log("Retrieving volumes for groovy1");
         Set<VolumeDetails> groovy1Volumes = storageApiHelper.getVolumesByPrefix(groovy1VolumePrefix);
         AssertUtils.assertEquals("Wrong number of volumes detected after installation for service groovy1", 1, groovy1Volumes.size());
 
         LogUtils.log("Retrieving volumes for groovy2");
         Set<VolumeDetails> groovy2Volumes = storageApiHelper.getVolumesByPrefix(groovy2VolumePrefix);
         AssertUtils.assertEquals("Wrong number of volumes detected after installation for service groovy2", 1, groovy2Volumes.size());
 
         AssertUtils.assertTrue("Both volumes should not be attached to different same instance",
                 !storageApiHelper.getVolumeAttachments(groovy1Volumes.iterator().next().getId())
                         .equals(storageApiHelper.getVolumeAttachments(groovy2Volumes.iterator().next().getId())));
 
         installer.uninstall();
     }
 
     public void testMultitenantStorageAllocation() throws Exception {
 
         installer = new ApplicationInstaller(restUrl, "groovy-App-multitenant");
         installer.recipePath(CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/staticstorage/groovyApp-multitenant"));
         installer.install();
 
         final String groovy1VolumePrefix = cloudService.getCloud().getCloudStorage().getTemplates().get("GROOVY1").getNamePrefix();
         LogUtils.log("Volume prefix for groovy1 is " + groovy1VolumePrefix);
         final String groovy2VolumePrefix = cloudService.getCloud().getCloudStorage().getTemplates().get("GROOVY2").getNamePrefix();
         LogUtils.log("Volume prefix for groovy2 is " + groovy2VolumePrefix);
 
         LogUtils.log("Retrieving volumes for groovy1");
         Set<VolumeDetails> groovy1Volumes = storageApiHelper.getVolumesByPrefix(groovy1VolumePrefix);
         AssertUtils.assertEquals("Wrong number of volumes detected after installation for service groovy1", 1, groovy1Volumes.size());
 
         LogUtils.log("Retrieving volumes for groovy2");
         Set<VolumeDetails> groovy2Volumes = storageApiHelper.getVolumesByPrefix(groovy2VolumePrefix);
         AssertUtils.assertEquals("Wrong number of volumes detected after installation for service groovy2", 1, groovy2Volumes.size());
 
         AssertUtils.assertEquals("Both volumes should be attached to different same instance",
                 storageApiHelper.getVolumeAttachments(groovy1Volumes.iterator().next().getId()),
                 storageApiHelper.getVolumeAttachments(groovy2Volumes.iterator().next().getId()));
 
         installer.uninstall();
     }
 
     public void testConcurrentAllocation() throws Exception {
 
         String folderName = "concurrent";
         folderName = copyServiceToRecipesFolder(CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/dynamicstorage/" + folderName), folderName);
         String serviceName = ServiceReader.readService(new File(ScriptUtils.getBuildRecipesServicesPath() + "/" + folderName)).getName();
 
         installer = new ServiceInstaller(restUrl, serviceName);
         installer.recipePath(folderName);
         installer.setDisableSelfHealing(true);
         installer.install();
 
         LogUtils.log("Retrieving all volumes with prefix " + getVolumePrefixForTemplate("INSTANCE_1"));
         Set<VolumeDetails> volumesByName = storageApiHelper.getVolumesByPrefix(getVolumePrefixForTemplate("INSTANCE_1"));
         LogUtils.log("Volumes are : " + volumesByName);
 
         // two volumes should have been created since the service has two instances, each creating a volume.
         AssertUtils.assertEquals("Wrong number of volumes created", 2, volumesByName.size());
 
         LogUtils.log("Collecting attachments of newly created");
         Set<String> attachmentIps = new HashSet<String>();
 
         for (VolumeDetails vol : volumesByName) {
             // the install should have created and attached a volume with a name prefix of the class name. see customizeCloud below.
             AssertUtils.assertNotNull("could not find the required volume after install service", vol);
             // also check it is attached.
             Set<String> volumeAttachments = storageApiHelper.getVolumeAttachments(vol.getId());
             AssertUtils.assertEquals("the volume should have one attachements", 1, volumeAttachments.size());
             attachmentIps.add(volumeAttachments.iterator().next());
         }
         LogUtils.log("Attachments are " + attachmentIps);
 
         // the volumes should be attached to different instances
         AssertUtils.assertEquals("the volumes are not attached to two different instances", 2, attachmentIps.size());
 
         installer.uninstall();
 
         // after uninstall the volumes should be deleted
         for (VolumeDetails vol : volumesByName) {
             VolumeDetails currentVol = storageApiHelper.getVolumeById(vol.getId());
             AssertUtils.assertTrue("volume with id " + vol.getId() + " was not deleted after uninstall", currentVol == null || storageApiHelper.isVolumeDeleting(currentVol.getId()));
         }
 
     }
 
     public void testFailover() throws Exception {
 
         String folderName = "simple-storage";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/staticstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_LINUX", false);
 
         final String serviceName = ServiceReader.readService(new File(ScriptUtils.getBuildRecipesServicesPath() + "/" + folderName)).getName();
 
         installer = new ServiceInstaller(restUrl, serviceName);
         installer.recipePath(folderName);
         installer.setDisableSelfHealing(true);
         installer.install();
 
         LogUtils.log("Searching for volumes created by the service installation");
         // the install should have created and attached a volume with a name prefix of the class name. see customizeCloud below.
         VolumeDetails ourVolume = storageApiHelper.getVolumesByPrefix(getVolumePrefixForTemplate("SMALL_BLOCK")).iterator().next();
 
         AssertUtils.assertNotNull("could not find the required volume after install service", ourVolume);
         LogUtils.log("Found volume : " + ourVolume);
         // also check it is attached.
         AssertUtils.assertEquals("the volume should have one attachments", 1, storageApiHelper.getVolumeAttachments(ourVolume.getId()).size());
 
         LogUtils.log("Shutting down agent");
         String attachmentId = storageApiHelper.getVolumeAttachments(ourVolume.getId()).iterator().next();
         computeApiHelper.shutdownServerByAttachmentId(attachmentId);
 
         final GSRestClient client = new GSRestClient("", "", new URL(restUrl), PlatformVersion.getVersionNumber());
 
         LogUtils.log("Waiting for service " + serviceName + " to restart on a new machine");
 
         AssertUtils.repetitiveAssertTrue("Service " + serviceName + " didn't break", new AssertUtils.RepetitiveConditionProvider() {
             @Override
             public boolean getCondition() {
             try {
                 // we don't know which service the agent we shutdown belonged to.
                 // query all installed services to find out.
                 String serviceRestUrl = "ProcessingUnits/Names/default." + serviceName;
                 int numberOfInstances = (Integer)client.getAdminData(serviceRestUrl).get("Instances-Size");
                 LogUtils.log("Number of " + serviceName + " instances is " + numberOfInstances);
                 if (numberOfInstances < 1) {
                     LogUtils.log(serviceName + " service broke. it now has only " + numberOfInstances + " instances");
                     return true;
                 }
                 return false;
             } catch (RestException e) {
                 throw new RuntimeException(e);
             }
 
             }
         } , AbstractTestSupport.OPERATION_TIMEOUT * 4);
 
         LogUtils.log("Deleting previous volume : " + ourVolume.getId());
         Set<String> volumeAttachments = storageApiHelper.getVolumeAttachments(ourVolume.getId());
         if (volumeAttachments != null && !volumeAttachments.isEmpty()) {
             String instanceId = volumeAttachments.iterator().next();
             LogUtils.log("Detaching volume with id " + ourVolume.getId() + " from instance " + instanceId);
             storageApiHelper.detachVolume(ourVolume.getId(), computeApiHelper.getServerByAttachmentId(instanceId).getPrivateAddress());
         }
         storageApiHelper.deleteVolume(ourVolume.getId());
 
         AssertUtils.repetitiveAssertTrue(serviceName + " service did not recover", new AssertUtils.RepetitiveConditionProvider() {
             @Override
             public boolean getCondition() {
                 final String brokenServiceRestUrl = "ProcessingUnits/Names/default." + serviceName;
                 try {
                     int numOfInst = (Integer) client.getAdminData(brokenServiceRestUrl).get("Instances-Size");
                     return (1 == numOfInst);
                 } catch (RestException e) {
                     throw new RuntimeException("caught a RestException", e);
                 }
             }
         } , AbstractTestSupport.OPERATION_TIMEOUT * 3);
 
         LogUtils.log("Searching for volumes created by the service failover healing");
         // the install should have created and attached a volume with a name prefix of the class name. see customizeCloud below.
         ourVolume = storageApiHelper.getVolumesByPrefix(getVolumePrefixForTemplate("SMALL_BLOCK")).iterator().next();
 
         AssertUtils.assertNotNull("could not find the required volume after install service", ourVolume);
         LogUtils.log("Found volume : " + ourVolume);
         // also check it is attached.
         AssertUtils.assertEquals("the volume should have one attachments", 1, storageApiHelper.getVolumeAttachments(ourVolume.getId()).size());
     }
 
     private String getVolumePrefixForTemplate(final String template) {
         return cloudService.getCloud().getCloudStorage().getTemplates().get(template).getNamePrefix();
     }
 
     public void testDynamicStorageAttachmentLinux() throws Exception {
         String folderName = "attach-only";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/dynamicstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_LINUX", true);
         testDynamicStorageAttachment(folderName);
     }
 
     public void testDynamicStorageAttachmentUbuntu() throws Exception {
         String folderName = "attach-only";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/dynamicstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_UBUNTU", false);
         testDynamicStorageAttachment(folderName);
     }
 
     public void testNonPrivileged() throws Exception {
 
         String folderName = "non-sudo";
         folderName = copyServiceToRecipesFolder(CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/dynamicstorage/" + folderName), folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_LINUX", true);
         String serviceName = ServiceReader.readService(new File(ScriptUtils.getBuildRecipesServicesPath() + "/" + folderName)).getName();
 
         final String expectedOutput = "Cannot format when not running in privileged mode";
 
         installer = new ServiceInstaller(restUrl, serviceName);
         installer.recipePath(folderName);
         installer.timeoutInMinutes(10);
         installer.setDisableSelfHealing(true);
         String installOutput = installer.install();
 
         // the installation should not succeed because the user is not sudo
         // see src/main/resources/apps/USM/usm/dynamicstroage/non-sudo/groovy.service
         // so we expect the IllegalStateException to propagate to the CLI.
         AssertUtils.assertTrue("installation output should have contained '" + expectedOutput + "'", installOutput.toLowerCase().contains(expectedOutput.toLowerCase()));
 
         installer.uninstall();
 
     }
 
     public void testSmallCreateVolumeTimeout() throws Exception {
 
         String folderName = "small-create-volume-timeout";
         folderName = copyServiceToRecipesFolder(CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/dynamicstorage/" + folderName), folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_LINUX", true);
         String serviceName = ServiceReader.readService(new File(ScriptUtils.getBuildRecipesServicesPath() + "/" + folderName)).getName();
 
         installer = new ServiceInstaller(restUrl, serviceName);
         installer.recipePath(folderName);
         installer.setDisableSelfHealing(true);
         installer.install();
     }
 
     public void testSmallFormatTimeoutLinux() throws Exception {
         String folderName = "small-format-timeout";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/dynamicstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_LINUX", true);
         testSmallFormatTimeout(folderName);
     }
 
     public void testSmallFormatTimeoutUbuntu() throws Exception {
         String folderName = "small-format-timeout";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/dynamicstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_UBUNTU", false);
         testSmallFormatTimeout(folderName);
     }
 
     public void testUnsupportedFileSystemLinux() throws Exception {
         String folderName = "unsupported-fs";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/dynamicstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_LINUX", true);
         testUnsupportedFileSystem(folderName);
     }
 
     public void testUnsupportedFileSystemUbuntu() throws Exception {
         String folderName = "unsupported-fs";
         final String servicePath = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/dynamicstorage/" + folderName);
         folderName = copyServiceToRecipesFolder(servicePath, folderName);
         setTemplate(RECIPES_SERVICES_FOLDER + "/" + folderName, "SMALL_UBUNTU", false);
         testUnsupportedFileSystem(folderName);
     }
 
     /* Private logical tests methods */
 
     private void testUnsupportedFileSystem(final String folderName) throws Exception {
 
         String serviceName = ServiceReader.readService(new File(ScriptUtils.getBuildRecipesServicesPath() + "/" + folderName)).getName();
 
         final String expectedException = LocalStorageOperationException.class.getSimpleName();
 
         installer = new ServiceInstaller(restUrl, serviceName);
         installer.recipePath(folderName);
         installer.timeoutInMinutes(10);
         installer.setDisableSelfHealing(true);
         String installOutput = installer.install();
 
         // this installs a service that tries to mount a device onto a non supported file system(foo)
         // see src/main/resources/apps/USM/usm/dynamicstroage/unsupported-fs/groovy.service
         // so we except the LocalStorageOperationException to propagate to the CLI.
 
         AssertUtils.assertTrue("install output should have contained " + expectedException, installOutput.contains(expectedException));
 
         installer.uninstall();
 
     }
 
     private void testSmallFormatTimeout(final String folderName) throws Exception {
 
         String serviceName = ServiceReader.readService(new File(ScriptUtils.getBuildRecipesServicesPath() + "/" + folderName)).getName();
 
         installer = new ServiceInstaller(restUrl, serviceName);
         installer.recipePath(folderName);
         installer.timeoutInMinutes(10);
         installer.setDisableSelfHealing(true);
         String installOutput = installer.install();
 
         // the installation should not succeed because the format timeout is extremely small (5 millis)
         // see src/main/resources/apps/USM/usm/dynamicstroage/small-format-time/groovy.service
         // so we expect the TimeoutException to propagate to the CLI.
         AssertUtils.assertTrue("installation output should have contained a TimeoutException", installOutput.contains("TimeoutException"));
 
         installer.uninstall();
 
     }
 
     private void testDynamicStorageAttachment(final String folderName) throws Exception {
 
         Service service = ServiceReader.readService(new File(ScriptUtils.getBuildRecipesServicesPath() + "/" + folderName));
         String serviceName = service.getName();
 
         installer = new ServiceInstaller(restUrl, serviceName);
         installer.recipePath(folderName);
         installer.setDisableSelfHealing(true);
         installer.install();
 
         String machinePrefix;
         String templateName;
 
         LogUtils.log("Retrieving machine prefix for the installed service");
         if (service.getIsolationSLA().getGlobal().isUseManagement()) {
             machinePrefix = cloudService.getCloud().getProvider().getManagementGroup();
             templateName = cloudService.getCloud().getConfiguration().getManagementMachineTemplate();
         } else {
             machinePrefix = cloudService.getCloud().getProvider().getMachineNamePrefix();
             templateName = service.getCompute().getTemplate();
         }
         LogUtils.log("Machine prefix is " + machinePrefix);
 
         String locationId = cloudService.getCloud().getCloudCompute().getTemplates().get(templateName).getLocationId();
         LogUtils.log("Creating volume in location " + locationId);
         VolumeDetails details = storageApiHelper.createVolume("SMALL_BLOCK", locationId);
         LogUtils.log("Volume created : " + details);
 
         LogUtils.log("Attaching volume with id " + details.getId() + " to service");
         ((ServiceInstaller)installer).invoke("attachVolume " + details.getId() + " " + "/dev/xvdc");
 
         LogUtils.log("Checking volume with id " + details.getId() + " is really attached");
         VolumeDetails volume = storageApiHelper.getVolumeById(details.getId());
         Set<String> volumeAttachments = storageApiHelper.getVolumeAttachments(volume.getId());
         AssertUtils.assertEquals("volume with id " + volume.getId() + " should have one attachement after invoking attachVolume", 1, volumeAttachments.size());
         LogUtils.log("Volume attachment is " + volumeAttachments);
 
         LogUtils.log("Detaching volume with id " + details.getId() + " from service");
         ((ServiceInstaller)installer).invoke("detachVolume" + " " + details.getId());
 
         LogUtils.log("Checking volume with id " + details.getId() + " is really detached");
         volume = storageApiHelper.getVolumeById(details.getId());
         volumeAttachments = storageApiHelper.getVolumeAttachments(volume.getId());
         // volume should still exist
         AssertUtils.assertTrue("volume with id " + details.getId() + " should not have been deleted after calling detachVolume(delteOnExit = false)", volume != null);
         // though it should have no attachments
         AssertUtils.assertTrue("volume with id " + details.getId() + " should not have no attachments after calling detachVolume(delteOnExit = false)",
                 volumeAttachments == null || volumeAttachments.isEmpty());
 
         LogUtils.log("Deleting volume with id " + details.getId());
         storageApiHelper.deleteVolume(volume.getId());
 
         installer.uninstall();
 
     }
 
     private void testFailedToAttach(final String folderName) throws Exception {
 
         String serviceName = ServiceReader.readService(new File(ScriptUtils.getBuildRecipesServicesPath() + "/" + folderName)).getName();
 
         installer = new ServiceInstaller(restUrl, serviceName);
         installer.recipePath(folderName);
         installer.timeoutInMinutes(3);
         installer.expectToFail(true);
         installer.install();
 
         LogUtils.log("Searching for volumes created by the service installation");
         // the install should have created and attached a volume with a name prefix of the class name. see customizeCloud below.
         Set<VolumeDetails> ourVolumes = storageApiHelper.getVolumesByPrefix(getVolumePrefixForTemplate("SMALL_BLOCK"));
 
         AssertUtils.assertEquals("Found leaking volumes created by failed installation", 0, ourVolumes.size());
 
         installer.expectToFail(false);
         installer.uninstall();
     }
 
     private void testFaultyInstall(final String folderName) throws Exception {
 
         String serviceName = ServiceReader.readService(new File(ScriptUtils.getBuildRecipesServicesPath() + "/" + folderName)).getName();
 
         installer = new ServiceInstaller(restUrl, serviceName);
         installer.recipePath(folderName);
         installer.timeoutInMinutes(3);
         installer.setDisableSelfHealing(true);
         installer.expectToFail(true);
 
         // this installation will fail at install event.
         // causing the USM to shutdown and de-allocate the storage.
         installer.install();
 
         installer.expectToFail(false);
         installer.uninstall();
     }
 
     private void testWriteToStorage(final String folderName) throws Exception {
 
         Service service = ServiceReader.readService(new File(ScriptUtils.getBuildRecipesServicesPath() + "/" + folderName));
 
         installer = new ServiceInstaller(restUrl, service.getName());
         installer.recipePath(folderName);
         installer.setDisableSelfHealing(true);
         installer.install();
 
         LogUtils.log("Creating a new file called foo.txt in the storage volume. " + "running 'touch ~/storage/foo.txt' command on remote machine.");
         invokeCommand(service.getName(), "writeToStorage");
 
         LogUtils.log("listing all files inside mounted storage folder. running 'ls ~/storage/' command");
         String listFilesResult = invokeCommand(service.getName(), "listFilesInStorage");
 
         AssertUtils.assertTrue("File was not created in storage volume. Output was " + listFilesResult, listFilesResult.contains("foo.txt"));
 
         installer.uninstall();
     }
 
     private void testMount(final String folderName) throws Exception {
 
         Service service = ServiceReader.readService(new File(ScriptUtils.getBuildRecipesServicesPath() + "/" + folderName));
         String serviceName = service.getName();
 
         installer = new ServiceInstaller(restUrl, serviceName);
         installer.recipePath(folderName);
         installer.setDisableSelfHealing(true);
         installer.install();
 
         LogUtils.log("Creating a new file called foo.txt in the storage volume. " + "running 'touch ~/storage/foo.txt' command on remote machine.");
         invokeCommand(serviceName, "writeToStorage");
 
         LogUtils.log("listing all files inside mounted storage folder. running 'ls ~/storage/' command");
         String listFilesResult = invokeCommand(serviceName, "listFilesInStorage");
 
         AssertUtils.assertTrue("File was not created in storage volume. Output was " + listFilesResult, listFilesResult.contains("foo.txt"));
 
         LogUtils.log("Unmounting volume from file system");
         invokeCommand(serviceName, "unmount");
 
         LogUtils.log("Detaching volume");
         VolumeDetails vol = storageApiHelper.getVolumesByPrefix(getVolumePrefixForTemplate("SMALL_BLOCK")).iterator().next();
         String attachmentId = storageApiHelper.getVolumeAttachments(vol.getId()).iterator().next();
         storageApiHelper.detachVolume(vol.getId(), computeApiHelper.getServerByAttachmentId(attachmentId).getPrivateAddress());
 
         //asserting the file is not in the mounted directory
         LogUtils.log("listing all files inside mounted storage folder. running 'ls ~/storage/' command");
         listFilesResult = invokeCommand(serviceName, "listFilesInStorage");
 
         AssertUtils.assertTrue("The newly created file is in the mounted directory after detachment", !listFilesResult.contains("foo.txt"));
 
         LogUtils.log("Reattaching the volume to the service machine");
 
         MachineDetails agent = computeApiHelper.getServerByAttachmentId(attachmentId);
         storageApiHelper.attachVolume(vol.getId(), cloudService.getCloud().getCloudStorage().getTemplates().get("SMALL_BLOCK").getDeviceName(), agent.getPrivateAddress());
         invokeCommand(serviceName, "mount");
 
         //asserting the file is in the mounted directory
         LogUtils.log("listing all files inside mounted storage folder. running 'ls ~/storage/' command");
         listFilesResult = invokeCommand(serviceName, "listFilesInStorage");
 
         AssertUtils.assertTrue("the created file is not in the mounted directory after reattachment", listFilesResult.contains("foo.txt"));
     }
 
     private void testStorageVolumeMounted(final String folderName) throws Exception {
 
         String serviceName = ServiceReader.readService(new File(ScriptUtils.getBuildRecipesServicesPath() + "/" + folderName)).getName();
 
         final String expectedMountOutput = "/dev/xvdc on /home/ec2-user/storage type ext4 (rw)";
 
         installer = new ServiceInstaller(restUrl, serviceName);
         installer.recipePath(folderName);
         installer.setDisableSelfHealing(true);
         installer.install();
 
         LogUtils.log("Listing all mounted devices. running command 'mount -l' on remote machine");
         String listMountedResult = invokeCommand(serviceName, "listMount");
 
         AssertUtils.assertTrue("device is not in the mounted devices list: " + listMountedResult,
                 listMountedResult.contains(expectedMountOutput));
 
         installer.uninstall();
     }
 
     private void testInstallWithStorage(final String folderName) throws Exception {
 
         String serviceName = ServiceReader.readService(new File(ScriptUtils.getBuildRecipesServicesPath() + "/" + folderName)).getName();
 
         installer = new ServiceInstaller(restUrl, serviceName);
         installer.recipePath(folderName);
         installer.setDisableSelfHealing(true);
         installer.install();
 
         LogUtils.log("Searching for volumes created by the service installation");
         // the install should have created and attached a volume with a name prefix of the class name. see customizeCloud below.
         VolumeDetails ourVolume = storageApiHelper.getVolumesByPrefix(getVolumePrefixForTemplate("SMALL_BLOCK")).iterator().next();
 
         AssertUtils.assertNotNull("could not find the required volume after install service", ourVolume);
         LogUtils.log("Found volume : " + ourVolume);
         // also check it is attached.
         AssertUtils.assertEquals("the volume should have one attachments", 1, storageApiHelper.getVolumeAttachments(ourVolume.getId()).size());
 
         // TODO elip - assert Volume configuration?
 
         installer.uninstall();
     }
 
     private void testDeleteOnExitFalse(final String folderName) throws Exception {
 
         String serviceName = ServiceReader.readService(new File(ScriptUtils.getBuildRecipesServicesPath() + "/" + folderName)).getName();
 
         installer = new ServiceInstaller(restUrl, serviceName);
         installer.recipePath(folderName);
         installer.setDisableSelfHealing(true);
         installer.install();
 
         LogUtils.log("Searching for volumes created by the service installation");
         // the install should have created and attached a volume with a name prefix of the class name. see customizeCloud below.
         VolumeDetails ourVolume = storageApiHelper.getVolumesByPrefix(getVolumePrefixForTemplate("SMALL_BLOCK")).iterator().next();
 
         AssertUtils.assertNotNull("could not find the required volume after install service", ourVolume);
         LogUtils.log("Found volume : " + ourVolume);
         // also check it is attached.
         AssertUtils.assertEquals("the volume should have one attachments", 1, storageApiHelper.getVolumeAttachments(ourVolume.getId()).size());
 
         // TODO elip - assert Volume configuration?
 
         installer.uninstall();
 
         LogUtils.log("Searching for volumes created by the service after uninstall");
         // the install should have created and attached a volume with a name prefix of the class name. see customizeCloud below.
         ourVolume = storageApiHelper.getVolumesByPrefix(getVolumePrefixForTemplate("SMALL_BLOCK")).iterator().next();
 
         AssertUtils.assertNotNull("could not find the required volume after install service", ourVolume);
         LogUtils.log("Found volume : " + ourVolume);
         storageApiHelper.deleteVolume(ourVolume.getId());
 
     }
 
     private String copyServiceToRecipesFolder(final String path, final String originalFolderName) throws IOException, DSLException, PackagingException {
         String buildRecipesServicesPath = ScriptUtils.getBuildRecipesServicesPath();
 
         String folderName = originalFolderName + System.currentTimeMillis();
 
         File serviceFolder = new File(buildRecipesServicesPath, folderName);
         if (serviceFolder.exists()) {
             FileUtils.deleteDirectory(serviceFolder);
         }
         serviceFolder.mkdir();
         FileUtils.copyDirectory(new File(path), serviceFolder);
         return folderName;
     }
 
     private void setTemplate(final String path, final String computeTemplateName, boolean useManagement) throws Exception {
         File serviceFile = DSLReader.findDefaultDSLFile(org.cloudifysource.dsl.internal.DSLUtils.SERVICE_DSL_FILE_NAME_SUFFIX, new File(path));
         Map<String, String> props = new HashMap<String,String>();
         props.put("ENTER_TEMPLATE", computeTemplateName);
         if (!useManagement) {
             props.put("useManagement true", "useManagement false");
         }
         IOUtils.replaceTextInFile(serviceFile.getAbsolutePath(), props);
     }
 
     private String invokeCommand(String serviceName, String commandName)
             throws IOException, InterruptedException {
         ServiceInstaller installer = new ServiceInstaller(restUrl, serviceName);
         return installer.invoke(commandName);
     }
 }
