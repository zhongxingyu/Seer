 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud;
 
 import com.j_spaces.kernel.PlatformVersion;
 import org.cloudifysource.dsl.Service;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.cloudifysource.esc.driver.provisioning.storage.VolumeDetails;
 import org.cloudifysource.quality.iTests.framework.utils.AssertUtils;
 import org.cloudifysource.quality.iTests.framework.utils.JCloudsUtils;
 import org.cloudifysource.quality.iTests.framework.utils.LogUtils;
 import org.cloudifysource.quality.iTests.framework.utils.StorageUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils.ProcessResult;
 import org.cloudifysource.restclient.GSRestClient;
 import org.cloudifysource.restclient.RestException;
 import org.jclouds.compute.domain.NodeMetadata;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;
 
 /**
  * 
  * verifies the volume was mounted and is writable.
  * contains generic tests.
  *
  * @author adaml, nirb
  *
  */
 public abstract class AbstractStorageTest extends NewAbstractCloudTest{
 
 	final static String WRITE_FILE_COMMAND_NAME = "writeToStorage";
 	final static String LIST_FILES_COMMAND_NAME = "listFilesInStorage";
 	final static String LIST_MOUNTED_DEVICES_COMMAND_NAME = "listMount";
 	final static String MOUNT_COMMAND_NAME = "mount";
 	final static String UNMOUNT_COMMAND_NAME = "unmount";
 
     protected final static String SERVICE_NAME = "simpleStorage";
     protected final static String SERVICE_PATH = CommandTestUtils.getPath("src/main/resources/apps/USM/usm/" + SERVICE_NAME);
     private final static String SERVICE_FILE_PATH = SERVICE_PATH + "/simple-service.groovy";
     private final static String SERVICE_REST_URL = "ProcessingUnits/Names/default." + SERVICE_NAME;
     protected final static String CUSTOM_SERVICE_NAME = "customStorageTemplateService";
     protected final static String CUSTOM_SERVICE_PATH = CommandTestUtils.getPath("src/main/resources/apps/USM/usm/" + CUSTOM_SERVICE_NAME);
     private final static String CUSTOM_SERVICE_FILE_PATH = CUSTOM_SERVICE_PATH + "/storage-service.groovy";
 
     private final static String TESTING_FILE_NAME = "foo.txt";
 	private static final long ONE_MINUTE_IN_MILLIS = 60 * 1000;
 	private static final long FIVE_SECONDS_IN_MILLIS = 5 * 1000;
 	private static final int FAILED_INSTALL_SERVICE_TIMEOUT = 2;
 	private static final int INSTALL_SERVICE_TIMEOUT = 15;
     protected static final int MAXIMUM_UNINSTALL_TIME = 5;
 	private static final long MOUNT_AFTER_ATTACH_TIMEOUT = 10 * 1000;
 	protected static final long POLLING_TIMEOUT = 2 * 1000;
 
     public void bootstrapAndInit() throws Exception{
 
         super.bootstrap();
 
         File serviceFile = new File(SERVICE_FILE_PATH);
         Service service = ServiceReader.readService(serviceFile);
 
         StorageUtils.init(getService().getCloud(), service.getCompute().getTemplate(), service.getStorage().getTemplate(), getService());
     }
 
     public void cleanup() throws Exception{
         cleanup(false);
     }
 
     public void cleanup(boolean expectedVolumeLeak) throws Exception{
 
         super.teardown();
         StorageUtils.scanAndDeleteLeakedVolumes(expectedVolumeLeak);
         StorageUtils.close();
     }
 
 	public void testWriteToStorage() throws IOException, InterruptedException {
 		
 		LogUtils.log("creating a new file called foo.txt in the storage volume. " 
 						+ "running 'touch ~/storage/foo.txt' command on remote machine.");
 		ProcessResult invokeCommandResult = invokeCommand(SERVICE_NAME, WRITE_FILE_COMMAND_NAME); 
 		
 		assertTrue("create file command exited with an abnormal exit code. Output was " + invokeCommandResult.getOutput(),
 				invokeCommandResult.getExitcode() == 0);
 		
 		LogUtils.log("listing all files inside mounted storage folder. running 'ls ~/storage/' command");
 		ProcessResult listFilesResult = invokeCommand(SERVICE_NAME, LIST_FILES_COMMAND_NAME); 
 		
 		assertTrue("failed listing files. Output was " + listFilesResult.getOutput(), listFilesResult.getExitcode() == 0);
 		assertTrue("File was not created in storage volume. Output was " + listFilesResult.getOutput(),
 				listFilesResult.getOutput().contains("foo.txt"));
 	}
 	
 	protected void testStorageVolumeMounted(String expectedMountOutput) throws IOException, InterruptedException {
 		
 		LogUtils.log("Listing all mounted devices. running command 'mount -l' on remote machine");
 		ProcessResult listMountedResult = invokeCommand(SERVICE_NAME, LIST_MOUNTED_DEVICES_COMMAND_NAME);
 		
 		assertTrue("failed listing all mounted devices. Output was " + listMountedResult.getOutput(),
 				listMountedResult.getExitcode() == 0);
 		assertTrue("device is not in the mounted devices list: " + listMountedResult.getOutput(),
 				listMountedResult.getOutput().contains(expectedMountOutput));
 	}
 
 	// deleteOnExit = true
     public void testInstallWithStorage() throws Exception{
 
         installServiceAndWait(SERVICE_PATH, SERVICE_NAME);
 
         AssertUtils.assertTrue(StorageUtils.verifyVolumeConfiguration(SERVICE_FILE_PATH));
 
         uninstallServiceIfFound(SERVICE_NAME);
 
         assertVolumeDeleted();
     }
 
     // deleted volumes might still be returned in listAllVolumes. 
     // Wait for a minute until volume is removed from list. 
     private void assertVolumeDeleted() throws Exception {
     	final long end = System.currentTimeMillis() + ONE_MINUTE_IN_MILLIS;
     	while (System.currentTimeMillis() < end) {
     		if (!isVolumeUp()) {
     			return;
     		}
     		Thread.sleep(FIVE_SECONDS_IN_MILLIS);
     	}
     	AssertUtils.assertTrue("volumes were not deleted", !isVolumeUp());
 	}
 
 	public void testDeleteOnExitFalse() throws Exception{
 
         installServiceAndWait(SERVICE_PATH, SERVICE_NAME);
 
         AssertUtils.assertTrue("volume not started", isVolumeUp());
 
         uninstallServiceIfFound(SERVICE_NAME);
 
         assertVolumeNotDeleted();
     }
 
 	public void testFailedInstall() throws Exception {
 
 		installServiceAndWait(SERVICE_PATH, SERVICE_NAME, FAILED_INSTALL_SERVICE_TIMEOUT, true);
 
 		AssertUtils.repetitiveAssertTrue("volume started", new AssertUtils.RepetitiveConditionProvider() {
 			@Override
 			public boolean getCondition() {
 				boolean result = false;
 				try {
 					Thread.sleep(POLLING_TIMEOUT);
 					result =  !isVolumeUp();
 				} catch (Exception e) {
 					AssertUtils.assertFail("exception thrown during volume verification", e);
 				}
 
 				return result;
 			}
 		}, OPERATION_TIMEOUT);
 
 	}
 
 
     public void testMount() throws Exception {
 
         installServiceAndWait(SERVICE_PATH, SERVICE_NAME);
 
         LogUtils.log("creating a new file called foo.txt in the storage volume. "
                 + "running 'touch ~/storage/foo.txt' command on remote machine.");
         ProcessResult invokeCommandResult = invokeCommand(SERVICE_NAME, WRITE_FILE_COMMAND_NAME);
 
         assertTrue("create file command exited with an abnormal exit code. Output was " + invokeCommandResult.getOutput(),
                 invokeCommandResult.getExitcode() == 0);
 
         LogUtils.log("listing all files inside mounted storage folder. running 'ls ~/storage/' command");
         ProcessResult listFilesResult = invokeCommand(SERVICE_NAME, LIST_FILES_COMMAND_NAME);
 
         assertTrue("failed listing files. Output was " + listFilesResult.getOutput(), listFilesResult.getExitcode() == 0);
         assertTrue("File was not created in storage volume. Output was " + listFilesResult.getOutput(),
                 listFilesResult.getOutput().contains(TESTING_FILE_NAME));
 
         ///////debug
         Map<String,Set<String>> servicesToMachines = StorageUtils.getServicesToMachines();
         for(String service : servicesToMachines.keySet()){
                  LogUtils.log("found service " + service);
         }
         ///////debug
 
         String machineIp = StorageUtils.getServicesToMachines().get(SERVICE_NAME).iterator().next();
         String volumeId = StorageUtils.getServiceNamedVolumes(SERVICE_FILE_PATH).iterator().next().getId();
 
         LogUtils.log("detaching volume from the service machine");
         invokeCommand(SERVICE_NAME, UNMOUNT_COMMAND_NAME);
 
         ///////debug
         Set<VolumeDetails> listVolumes = StorageUtils.listVolumes(machineIp, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
         for (VolumeDetails volumeDetails : listVolumes) {
             LogUtils.log("found volume " + volumeDetails.getId());
         }
         ///////debug
         Thread.sleep(MOUNT_AFTER_ATTACH_TIMEOUT);
         StorageUtils.detachVolume(volumeId, machineIp, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
 
         //asserting the file is not in the mounted directory
         LogUtils.log("listing all files inside mounted storage folder. running 'ls ~/storage/' command");
         listFilesResult = invokeCommand(SERVICE_NAME, LIST_FILES_COMMAND_NAME);
         assertTrue("failed listing files. Output was " + listFilesResult.getOutput(), listFilesResult.getExitcode() == 0);
 
         assertTrue("the newly created file is in the mounted directory after detachment", !listFilesResult.getOutput().contains(TESTING_FILE_NAME));
 
         LogUtils.log("reattaching the volume to the service machine");
        StorageUtils.attachVolume(volumeId, machineIp, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
         Thread.sleep(MOUNT_AFTER_ATTACH_TIMEOUT);
         invokeCommand(SERVICE_NAME, MOUNT_COMMAND_NAME);
 
         //asserting the file is in the mounted directory
         LogUtils.log("listing all files inside mounted storage folder. running 'ls ~/storage/' command");
         listFilesResult = invokeCommand(SERVICE_NAME, LIST_FILES_COMMAND_NAME);
         assertTrue("failed listing files. Output was " + listFilesResult.getOutput(), listFilesResult.getExitcode() == 0);
 
         assertTrue("the created file is not in the mounted directory after reattachment", listFilesResult.getOutput().contains(TESTING_FILE_NAME));    }
 
     public void testTwoTemplates() throws Exception {
 
         installServiceAndWait(CUSTOM_SERVICE_PATH, CUSTOM_SERVICE_NAME, INSTALL_SERVICE_TIMEOUT);
         installServiceAndWait(SERVICE_PATH, SERVICE_NAME, INSTALL_SERVICE_TIMEOUT);
 
         StorageUtils.verifyVolumeConfiguration(SERVICE_FILE_PATH);
         StorageUtils.verifyVolumeConfiguration(CUSTOM_SERVICE_FILE_PATH);
     }
 
     public void testFailover() throws Exception {
 
         JCloudsUtils.createContext(getService());
 
         installServiceAndWait(SERVICE_PATH, SERVICE_NAME, INSTALL_SERVICE_TIMEOUT);
         StorageUtils.verifyVolumeConfiguration(SERVICE_FILE_PATH);
 
         Set<String> machineIps = StorageUtils.getServicesToMachines().get(SERVICE_NAME);
         Set<? extends NodeMetadata> allRunningNodes = JCloudsUtils.getAllRunningNodes();
         Set<NodeMetadata> serviceMachines = new HashSet<NodeMetadata>();
 
         for(NodeMetadata node : allRunningNodes){
             for(String machineIp : machineIps){
                 if(machineIp.equalsIgnoreCase(node.getPrivateAddresses().iterator().next())){
                     serviceMachines.add(node);
                 }
             }
         }
 
         //taking a snapshot of the currently running nodes
         StorageUtils.beforeServiceInstallation();
 
         for(NodeMetadata machine : serviceMachines){
             LogUtils.log("shutting down machine " + machine.getId());
             JCloudsUtils.shutdownServer(machine.getId());
         }
 
         StorageUtils.afterServiceUninstallation(SERVICE_NAME);
 
         final GSRestClient client = new GSRestClient("", "", new URL(getRestUrl()), PlatformVersion.getVersionNumber());
         final AtomicReference<String> atomicServiceStatus = new AtomicReference<String>();
 
         LogUtils.log("waiting for service to break due to machine shutdown");
         AssertUtils.repetitiveAssertTrue("service didn't break", new AssertUtils.RepetitiveConditionProvider() {
             @Override
             public boolean getCondition() {
                 String brokenString = "broken";
                 try {
                     atomicServiceStatus.set((String) client.getAdminData(SERVICE_REST_URL).get("Status-Enumerator"));
                 } catch (RestException e) {
                     throw new RuntimeException("caught a RestException", e);
                 }
                 return brokenString.equalsIgnoreCase(atomicServiceStatus.get());
             }
         } , OPERATION_TIMEOUT*2);
 
         LogUtils.log("waiting for service to restart on a new machine");
         AssertUtils.repetitiveAssertTrue("service didn't recover", new AssertUtils.RepetitiveConditionProvider() {
             @Override
             public boolean getCondition() {
                 String intactString = "intact";
                 try {
                     atomicServiceStatus.set((String) client.getAdminData(SERVICE_REST_URL).get("Status-Enumerator"));
                 } catch (RestException e) {
                     throw new RuntimeException("caught a RestException", e);
                 }
                 return intactString.equalsIgnoreCase(atomicServiceStatus.get());
             }
         } , OPERATION_TIMEOUT*3);
 
         //taking a new snapshot of the machines and updating the service machines
         StorageUtils.afterServiceInstallation(SERVICE_NAME);
 
         StorageUtils.verifyVolumeConfiguration(SERVICE_FILE_PATH);
 
         JCloudsUtils.closeContext();
     }
 
     private void assertVolumeNotDeleted() throws Exception {
     	final long end = System.currentTimeMillis() + ONE_MINUTE_IN_MILLIS;
     	while (System.currentTimeMillis() < end) {
     		if (!isVolumeUp()) {
     			AssertUtils.assertFail("volume was deleted");
     		}
     		Thread.sleep(FIVE_SECONDS_IN_MILLIS);
     	}
 	}
 
 	private boolean isVolumeUp() throws Exception {
         return isVolumeUp(SERVICE_FILE_PATH);
     }
 
 	private boolean isVolumeUp(String serviceFilePath) throws Exception {
 
         Set<VolumeDetails> serviceNamedVolumes = StorageUtils.getServiceNamedVolumes(serviceFilePath);
         for(VolumeDetails vd :serviceNamedVolumes){
             LogUtils.log("in isVolumeUp - found volume " + vd.getName());
         }
 
         return !serviceNamedVolumes.isEmpty();
     }
 }
