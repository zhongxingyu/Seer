 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon.failover;
 
import org.cloudifysource.quality.iTests.framework.utils.IOUtils;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.machine.Machine;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 import org.springframework.util.FileSystemUtils;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sagib
  * Date: 3/5/13
  * Time: 5:32 PM
  */
 public class ManagmenetHardShutdownAndRecoveryTest extends AbstractByonManagementPersistencyTest {
 
     private Machine[] gsmMachines;
 
     @Override
     public void shutdownManagement() throws Exception{
         GridServiceManager[] griServiceManagers = admin.getGridServiceManagers().getManagers();
         gsmMachines = new Machine[griServiceManagers.length];
         for (int i = 0 ; i < griServiceManagers.length ; i++) {
             gsmMachines[i] = griServiceManagers[i].getMachine();
         }
        for (Machine machine : gsmMachines) {
            AbstractKillManagementTest.restartMachineAndWait(machine.getHostAddress());
         }
         prepareManagementPersistencyFile();
     }
 
     @BeforeMethod(alwaysRun = true)
     public void bootstrapAndInit() throws Exception{
         super.prepareTest();
     }
 
     @AfterMethod(alwaysRun = true)
     public void cleanup() throws Exception{
 //        super.cleanup(true);
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
     public void testManagementPersistency() throws Exception {
         super.testManagementPersistency();
     }
 
     public void prepareManagementPersistencyFile() throws Exception{
        Resource originalFile = new ClassPathResource("apps/cloudify/cloud/boyn/management-persistency.txt");
         File tempFile = new File("management-persistency.txt");
         FileSystemUtils.copyRecursively(originalFile.getFile(), tempFile);
         Map<String, String> props = new HashMap<String, String>();
         for (int i = 0; i < gsmMachines.length; i++) {
             props.put("MANAGEMENT" + i, gsmMachines[i].getHostAddress());
         }
         IOUtils.replaceTextInFile(tempFile, props);
 
         backupFilePath = tempFile.getAbsolutePath();
 
     }
 

 }
