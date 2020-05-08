 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.ec2.dynamicstorage;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.TimeoutException;
 
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.quality.iTests.framework.utils.AssertUtils;
 import org.cloudifysource.quality.iTests.framework.utils.Ec2StorageApiForRegionHelper;
 import org.cloudifysource.quality.iTests.framework.utils.IOUtils;
 import org.cloudifysource.quality.iTests.framework.utils.LogUtils;
 import org.cloudifysource.quality.iTests.framework.utils.ScriptUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.NewAbstractCloudTest;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.ec2.Ec2CloudService;
 import org.jclouds.ec2.domain.Volume;
 import org.jclouds.ec2.domain.Volume.Status;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 
 public abstract class AbstractDynamicStorageTest extends NewAbstractCloudTest {
 	
 	private static final long VOLUME_WAIT_TIMEOUT = 3 * 60 * 1000; 
 	
 	private static final String PATH_TO_SERVICE = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/dynamicstorage/");;
 	protected static final String SERVICE_NAME = "groovy";
 	
 	protected Ec2StorageApiForRegionHelper storageHelper;
 
 	public abstract void doTest() throws Exception ;
 	
 	public abstract String getServiceFolder();
 	
 	@Override
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 		Ec2CloudService ec2CloudService = (Ec2CloudService)getService();
 		this.storageHelper = new Ec2StorageApiForRegionHelper(ec2CloudService.getRegion(), ec2CloudService.getComputeServiceContext());
 	}
 	
 	@BeforeMethod
 	public void prepareService() throws IOException {
 		String buildRecipesServicesPath = ScriptUtils.getBuildRecipesServicesPath();
 		File serviceFolder = new File(buildRecipesServicesPath, getServiceFolder());
 		if (serviceFolder.exists()) {
 			FileUtils.deleteDirectory(serviceFolder);
 		}
 		FileUtils.copyDirectoryToDirectory( new File(PATH_TO_SERVICE + "/" + getServiceFolder()), new File(buildRecipesServicesPath));
 	}
 	
 	public void testUbuntu() throws Exception {
 		setTemplate("SMALL_UBUNTU", false);
 		doTest();
 		
 	}
 	
 	public void testLinux(final boolean useManagement) throws Exception {
 		setTemplate("SMALL_LINUX", useManagement);
 		doTest();
 	}
 	
 	public void testLinux() throws Exception {
 		testLinux(true);
 	}
 	
 	@Override
 	protected void customizeCloud() throws Exception {
 		super.customizeCloud();
 		((Ec2CloudService)getService()).getAdditionalPropsToReplace().put("cloudify-storage-volume", System.getProperty("user.name") + "-" + this.getClass().getSimpleName().toLowerCase());
 		
 	}
 	
 	public void scanForLeakedVolumes(final String name) throws TimeoutException {
 		
 		Set<Volume> volumesByName = storageHelper.getVolumesByName(name);
         Set<Volume> nonDeletingVolumes = new HashSet<Volume>();
 
         for (Volume volumeByName : volumesByName) {
             if (volumeByName != null && !volumeByName.getStatus().equals(Status.DELETING)) {
                 LogUtils.log("Found a leaking volume " + volumeByName + ". status is " + volumeByName.getStatus());
                 LogUtils.log("Volume attachments are : " + volumeByName.getAttachments());
                 if (volumeByName.getAttachments() != null && !volumeByName.getAttachments().isEmpty()) {
                     LogUtils.log("Detaching attachment before deletion");
                     storageHelper.detachVolume(volumeByName.getId());
                     waitForVolumeStatus(volumeByName, Status.AVAILABLE);
                 }
                 waitForVolumeStatus(volumeByName, Status.AVAILABLE);
                 LogUtils.log("Deleting volume " + volumeByName.getId());
                 storageHelper.deleteVolume(volumeByName.getId());
             } else {
                 nonDeletingVolumes.add(volumeByName);
             }
         }
         if (!nonDeletingVolumes.isEmpty()) {
            AssertUtils.assertFail("Found leaking volumes after test ended :" + nonDeletingVolumes);
         }
 
 
 	}
 	
 	public void scanForLeakedVolumesCreatedViaTemplate(final String templateName) throws TimeoutException {
 		final String name = getService().getCloud().getCloudStorage().getTemplates().get(templateName).getNamePrefix();
 		scanForLeakedVolumes(name);
 	}
 	
 	private void waitForVolumeStatus(final Volume vol, final Status status) throws TimeoutException {
 		
 		final long end = System.currentTimeMillis() + VOLUME_WAIT_TIMEOUT;
 		
 		while (System.currentTimeMillis() < end) {
 			
 			Volume volume = storageHelper.getVolumeById(vol.getId());
 			if (volume.getStatus().equals(status)) {
 				return;
 			} else {
 				try {
 					LogUtils.log("Waiting for volume " + vol.getId() 
 							+ " to reach status " + status + " . current status is : " + volume.getStatus());
 					Thread.sleep(5000);
 				} catch (InterruptedException e) {
 				}
 			}
 			
 		}
 		throw new TimeoutException("Timed out waiting for volume " + vol + " to reach status " + status);
 		
 	}
 
 	
 	@AfterMethod
 	public void deleteService() throws IOException {
 		FileUtils.deleteDirectory(new File(ScriptUtils.getBuildRecipesServicesPath() + "/" + getServiceFolder()));
 	}
 
 	
 	protected void setTemplate(final String computeTemplateName, boolean useManagement) throws Exception {
 		File serviceFile = new File(ScriptUtils.getBuildRecipesServicesPath() + "/" + getServiceFolder(), SERVICE_NAME + "-service.groovy");
 		Map<String, String> props = new HashMap<String,String>();
 		props.put("ENTER_TEMPLATE", computeTemplateName);
 		if (!useManagement) {
 			props.put("useManagement true", "useManagement false");
 		}
 		IOUtils.replaceTextInFile(serviceFile.getAbsolutePath(), props);
 	}
 }
