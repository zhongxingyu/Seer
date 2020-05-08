 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon.persistence;
 
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 /**
  * User: nirb
  * Date: 13/03/13
  */
 public class CorruptedPersistencyDirectoryTest extends AbstractByonManagementPersistencyTest {
 
     @BeforeClass(alwaysRun = true)
     public void bootstrapAndInit() throws Exception{
         super.bootstrap();
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
     public void testCorruptedPersistencyDirectory() throws Exception {
         super.testCorruptedPersistencyDirectory();
     }
 
    @AfterClass(alwaysRun = true)
    public void teardown() throws Exception{
        super.teardown();
    }
 }
