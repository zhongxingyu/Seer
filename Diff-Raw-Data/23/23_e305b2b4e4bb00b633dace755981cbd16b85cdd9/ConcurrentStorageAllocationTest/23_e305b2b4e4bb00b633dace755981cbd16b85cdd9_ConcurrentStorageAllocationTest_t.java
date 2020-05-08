 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.ec2.dynamicstorage;
 
 import java.io.File;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.cloudifysource.quality.iTests.framework.tools.SGTestHelper;
 import org.cloudifysource.quality.iTests.framework.utils.AssertUtils;
 import org.cloudifysource.quality.iTests.framework.utils.ServiceInstaller;
import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.ec2.Ec2CloudService;
 import org.jclouds.ec2.domain.Volume;
 import org.jclouds.ec2.domain.Volume.Status;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
 
 public class ConcurrentStorageAllocationTest extends AbstractDynamicStorageTest {
 
 	private ServiceInstaller installer;
 	
 	private static final String FOLDER_NAME = "concurrent";
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 	}
 	
	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 4, enabled = true)
	public void testLinux() throws Exception {
		super.testLinux(false);
	}
	
 	@Override
 	public void doTest() throws Exception {
 		
 		installer = new ServiceInstaller(getRestUrl(), SERVICE_NAME);
 		installer.recipePath(FOLDER_NAME);
 		installer.install();
 		
 		Set<Volume> volumesByName = storageHelper.getVolumesByName(System.getProperty("user.name") + "-" + this.getClass().getSimpleName().toLowerCase());
 		
 		// two volumes should have been created since the service has two instances, each creating a volume.
 		AssertUtils.assertEquals("Wrong number of volumes created", 2, volumesByName.size());
 		
 		Set<String> attachmentIds = new HashSet<String>();
 		
 		for (Volume vol : volumesByName) {
 			// the install should have created and attached a volume with a name prefix of the class name. see customizeCloud below.			
 			AssertUtils.assertNotNull("could not find the required volume after install service", vol);
 			// also check it is attached.
 			AssertUtils.assertEquals("the volume should have one attachements", 1, vol.getAttachments().size());
 			attachmentIds.add(vol.getAttachments().iterator().next().getId());
 		}
 		
 		// the volumes should be attached to different instances
 		AssertUtils.assertEquals("the volumes are not attached to two different instances", 2, attachmentIds.size());
 		
 		installer.uninstall();	
 
 		// after uninstall the volumes should be deleted
 		for (Volume vol : volumesByName) {
 			Volume currentVol = storageHelper.getVolumeById(vol.getId());
 			AssertUtils.assertTrue("volume with id " + vol.getId() + " was not deleted after uninstall", currentVol == null || currentVol.getStatus().equals(Status.DELETING));
 		}		
 	}
 	
 	@Override
 	protected void customizeCloud() throws Exception {
 		super.customizeCloud();
 		File customCloudFile = new File(SGTestHelper.getCustomCloudConfigDir(getCloudName()) + "/dynamic-storage-concurrent/ec2-cloud.groovy");
 		((Ec2CloudService)getService()).setCloudGroovy(customCloudFile);
 	}
 
 	@Override
 	public String getServiceFolder() {
 		return FOLDER_NAME;
 	}
 
 	@Override
 	protected String getCloudName() {
 		return "ec2";
 	}
 
 	@Override
 	protected boolean isReusableCloud() {
 		return false;
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		super.teardown();
 	}
 }
