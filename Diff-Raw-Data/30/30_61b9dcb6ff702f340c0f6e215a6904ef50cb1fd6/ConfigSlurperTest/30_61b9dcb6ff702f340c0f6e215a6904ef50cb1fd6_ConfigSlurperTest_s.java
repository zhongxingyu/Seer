 package test.cli.cloudify;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.pu.service.ServiceDetails;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import test.usm.USMTestUtils;
 
 import org.cloudifysource.dsl.utils.ServiceUtils;
 
 public class ConfigSlurperTest extends AbstractLocalCloudTest {
 
     @Test
     public void test() throws IOException, InterruptedException {
         final String serviceDir = CommandTestUtils.getPath("apps/USM/usm/slurper");
         String command = "connect " + restUrl + ";install-service --verbose " + serviceDir + ";exit";
 
         runCommand(command);
        String absolutePuName = ServiceUtils.getAbsolutePUName(DEFAULT_APPLICTION_NAME, "slurper");
         ProcessingUnit pu = admin.getProcessingUnits().waitFor(absolutePuName);
         assertTrue("USM Service state is NOT RUNNING", USMTestUtils.waitForPuRunningState(absolutePuName, 60, TimeUnit.SECONDS, admin));
         Map<String, ServiceDetails> serviceDetails = pu.getInstances()[0].getServiceDetailsByServiceId();
         Map<String, Object> attributes = serviceDetails.get("USM").getAttributes();
         Assert.assertTrue(attributes.get("icon").equals("icon.png"));
         Assert.assertTrue(attributes.get("jmx-port").toString().equals("9999"));
         Assert.assertTrue(attributes.get("name").equals("slurper"));
         Assert.assertTrue(attributes.get("type").equals("WEB_SERVER"));
         Assert.assertTrue(attributes.get("tripletype").equals("WEB_SERVERWEB_SERVERWEB_SERVER"));
         Assert.assertTrue(attributes.get("placeholderProp").toString().equals("string"));
 
         if (admin.getMachines().getMachines()[0].getOperatingSystem().getDetails().getName().equals("Linux"))
             Assert.assertTrue(attributes.get("systemProp").toString().equals(":"));
         else
             Assert.assertTrue(attributes.get("systemProp").toString().equals(";"));
 
         command = "connect " + restUrl + ";" + "uninstall-service slurper;exit";
         runCommand(command);
     }
 }
