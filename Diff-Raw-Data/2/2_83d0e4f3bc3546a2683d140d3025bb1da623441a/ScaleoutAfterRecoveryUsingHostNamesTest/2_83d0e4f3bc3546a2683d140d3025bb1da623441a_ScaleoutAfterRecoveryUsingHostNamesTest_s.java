 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon.persistence;
 
import org.cloudifysource.quality.iTests.framework.utils.LogUtils;
 import org.cloudifysource.quality.iTests.framework.utils.NetworkUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.byon.MultipleTemplatesByonCloudService;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 /**
  * Created with IntelliJ IDEA.
  * User: elip
  * Date: 5/6/13
  * Time: 11:24 AM
  * To change this template use File | Settings | File Templates.
  */
 public class ScaleoutAfterRecoveryUsingHostNamesTest extends AbstractByonManagementPersistencyTest {
 
     private MultipleTemplatesByonCloudService service = new MultipleTemplatesByonCloudService();
 
     @BeforeClass(alwaysRun = true)
     protected void bootstrap() throws Exception {
 
         String[] machines = service.getMachines();
         LogUtils.log("Converting IP Addresses to host names");
         String[] machinesHostNames = NetworkUtils.resolveIpsToHostNames(machines);
         service.setMachines(machinesHostNames);
 
         service.setManagementMachineTemplate("TEMPLATE_1");
 
         service.setNumberOfHostsForTemplate("TEMPLATE_1", 2); // assign two hosts for management.
         // all other hosts will be assigned to the default "SMALL_LINUX" template.
 
         super.bootstrap(service);
         super.installTomcatService(1, null);
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
     public void testScaleoutAfterRecovery() throws Exception {
         super.testScaleoutAfterRecovery();
     }
 
     @AfterClass(alwaysRun = true)
     public void teardown() throws Exception{
         super.teardown();
     }
 }
