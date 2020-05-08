 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.ec2.storage;
 
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.quality.iTests.framework.tools.SGTestHelper;
 import org.cloudifysource.quality.iTests.framework.utils.IOUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.AbstractStorageTest;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.io.File;
 
 /**
  * Author: nirb
  * Date: 28/02/13
  */
 public class Ec2StorageTwoTemplatesTest extends AbstractStorageTest {
 
    private static final String ORIGINAL_EC2_DSL_FILE_PATH = SGTestHelper.getBuildDir() + "/clouds/ec2/ec2-cloud.groovy";
    private static final String CUSTOM_EC2_DSL_FILE_PATH = SGTestHelper.getSGTestRootDir() + "/src/main/resources/apps/USM/usm/customStorageTemplateService/ec2-cloud.groovy";
    private static String BACKUP_EC2_DSL_FILE_PATH = null;
 
     @BeforeMethod(alwaysRun = true)
     public void bootstrapAndInit() throws Exception{

        BACKUP_EC2_DSL_FILE_PATH = IOUtils.backupFile(ORIGINAL_EC2_DSL_FILE_PATH);
        IOUtils.replaceFile(ORIGINAL_EC2_DSL_FILE_PATH, CUSTOM_EC2_DSL_FILE_PATH);

         super.bootstrapAndInit();
     }
 
     @AfterMethod(alwaysRun = true)
     public void cleanup() throws Exception{
         super.cleanup();

        FileUtils.deleteQuietly(new File(ORIGINAL_EC2_DSL_FILE_PATH));
        IOUtils.replaceFileWithMove(new File(ORIGINAL_EC2_DSL_FILE_PATH), new File(BACKUP_EC2_DSL_FILE_PATH));
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
 }
