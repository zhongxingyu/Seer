 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.ec2.storage;
 
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.quality.iTests.framework.tools.SGTestHelper;
 import org.cloudifysource.quality.iTests.framework.utils.IOUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.AbstractStorageTest;
import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.ec2.Ec2CloudService;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.io.File;
 
 /**
  * Author: nirb
  * Date: 28/02/13
  */
 public class Ec2StorageTwoTemplatesTest extends AbstractStorageTest {
 
    private static final String CUSTOM_EC2_DSL_FILE_PATH = SGTestHelper.getCustomCloudConfigDir("ec2") + "/storage-two-templates/ec2-cloud.groovy";
 
     @BeforeMethod(alwaysRun = true)
     public void bootstrapAndInit() throws Exception{
         super.bootstrapAndInit();
     }
 
     @AfterMethod(alwaysRun = true)
     public void cleanup() throws Exception{
         super.cleanup();
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
     public void testTwoTemplates() throws Exception {
         super.testTwoTemplates();
     }
 
     @Override
     protected String getCloudName() {
         return "ec2";
     }
 
     @Override
     protected boolean isReusableCloud() {
         return false;
     }

    @Override
    protected void customizeCloud() throws Exception {
        super.customizeCloud();
        File customCloudFile = new File(CUSTOM_EC2_DSL_FILE_PATH);
        ((Ec2CloudService)getService()).setCloudGroovy(customCloudFile);
    }
 }
