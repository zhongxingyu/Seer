 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.ec2.storage.staticstorage;
 
 import org.cloudifysource.esc.driver.provisioning.storage.StorageProvisioningException;
 import org.cloudifysource.quality.iTests.framework.utils.ApplicationInstaller;
 import org.cloudifysource.quality.iTests.framework.utils.RecipeInstaller;
 import org.cloudifysource.quality.iTests.framework.utils.ServiceInstaller;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.AbstractStorageAllocationTest;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import java.util.concurrent.TimeoutException;
 
 public class Ec2SimpleStorageTest extends AbstractStorageAllocationTest {
 
     @Override
     protected String getCloudName() {
         return "ec2";
     }
 
     @BeforeClass(alwaysRun = true)
     protected void bootstrap() throws Exception {
         super.bootstrap();
     }
 
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 4, enabled = true)
     public void testLinux() throws Exception {
         storageAllocationTester.testInstallWithStorageLinux();
     }
 
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 4, enabled = true)
     public void testUbuntu() throws Exception {
        storageAllocationTester.testInstallWithStorageUbuntu();
     }
 
     @AfterMethod
     public void cleanup() {
         RecipeInstaller installer = storageAllocationTester.getInstaller();
         if (installer instanceof ServiceInstaller) {
             ((ServiceInstaller) installer).uninstallIfFound();
         } else {
             ((ApplicationInstaller) installer).uninstallIfFound();
         }
     }
 
     @AfterClass
     public void scanForLeakes() throws TimeoutException, StorageProvisioningException {
         super.scanForLeakedVolumesCreatedViaTemplate("SMALL_BLOCK");
     }
 
     @AfterClass(alwaysRun = true)
     protected void teardown() throws Exception {
         super.teardown();
     }
 
 
     @Override
     protected boolean isReusableCloud() {
         return false;
     }
 }
