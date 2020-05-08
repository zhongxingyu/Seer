 package test.cli.cloudify;
 
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import org.openspaces.admin.AdminFactory;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.DeploymentStatus;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.annotations.AfterTest;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Test;
 
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 
 import test.AbstractTest;
 
 public class PetClinicApplicationTest extends AbstractTest {
 	
 	ProcessingUnit mongod;
 	ProcessingUnit mongocfg;
 	ProcessingUnit mongos;
 	ProcessingUnit tomcat;
 	String petClinicAppName = "petclinic";
 	Machine[] machines;
 	
 	@Override
 	@BeforeTest
 	public void beforeTest() {
 		super.beforeTest();	
 		machines = admin.getMachines().getMachines();
 		admin.close();
 	}
 	
	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = false)
 	public void testPetClinincApplication() {
 		
 		String serviceDir = ScriptUtils.getBuildPath() + "/examples/petclinic";
 		String command = "bootstrap-localcloud ; install-application " + "--verbose -timeout 10 " + serviceDir;
 		try {
 			CommandTestUtils.runCommandAndWait(command);
 			AdminFactory factory = new AdminFactory();
 			for (Machine machine : machines) {
 				LogUtils.log("adding locator to admin : " + machine.getHostName() + ":1468");
 				factory.addLocator(machine.getHostAddress() + ":4168");
 			}
 			LogUtils.log("creating new admin");
 			admin = factory.createAdmin();
 			mongod = admin.getProcessingUnits().waitFor("mongod", 20, TimeUnit.SECONDS);
 			assertNotNull(mongod);
 			assertTrue(mongod.getStatus().equals(DeploymentStatus.INTACT));
 			mongocfg = admin.getProcessingUnits().waitFor("mongo-cfg", 20, TimeUnit.SECONDS);
 			assertNotNull(mongocfg);
 			assertTrue(mongocfg.getStatus().equals(DeploymentStatus.INTACT));
 			mongos = admin.getProcessingUnits().waitFor("mongos", 20, TimeUnit.SECONDS);
 			assertNotNull(mongos);
 			assertTrue(mongos.getStatus().equals(DeploymentStatus.INTACT));
 			tomcat = admin.getProcessingUnits().waitFor("tomcat", 20, TimeUnit.SECONDS);
 			assertNotNull(tomcat);
 			assertTrue(tomcat.getStatus().equals(DeploymentStatus.INTACT));
 			
 		} catch (IOException e) {
 			LogUtils.log("bootstrap-localcloud failed", e);
 			afterTest();
 		} catch (InterruptedException e) {
 			LogUtils.log("bootstrap-localcloud failed", e);
 			afterTest();
 		}
 		
 	}
 	
 	@Override
 	@AfterTest
 	public void afterTest() {
 		try {
 			LogUtils.log("tearing down local cloud");
 			CommandTestUtils.runCommandAndWait("teardown-localcloud");
 		} catch (IOException e) {
 			LogUtils.log("teardown-localcloud failed", e);
 		} catch (InterruptedException e) {
 			LogUtils.log("teardown-localcloud failed", e);
 		}
 		super.afterTest();	
 	}
 
 }
