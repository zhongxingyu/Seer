 
 
 import static org.testng.AssertJUnit.*;
 
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import com.eviware.soapui.tools.SoapUITestCaseRunner;
 
 
 public class CRMService {
 
 	private SoapUITestCaseRunner runner;
 	@BeforeClass(alwaysRun = true)
 	public void setUp() {
 		runner = new SoapUITestCaseRunner();
		runner.setProjectFile("MyIntegrationTests-soapui-project.xml");
 		String[] props = {
 				"crmId=" + Init.getProperty("crmId")
 				};
 				System.out.println("crmId=" + Init.getProperty("crmId"));
 		runner.setProjectProperties(props);
 		runner.setJUnitReport(true);
 		runner.setPrintReport(true);
 		runner.setExportAll(true);
 		runner.setOutputFolder("target/surefire-reports");
 		runner.setSettingsFile("soapui-settings.xml");
 		runner.setTestSuite("CRMService");
 	}
 
 	@Test(groups = {"MyApp1", "createAndGet"}, dependsOnGroups = { "crmDatabase" })
 	public void createAndGet() throws Exception {
 		assertNotNull("crmId missing, please define with -DcrmId=<id>", Init.getProperty("crmId"));
 		runner.setTestCase("CreateAndGet");
 		runner.run();
 	}
 
 	@Test(groups = {"MyApp1", "echo"})
 	public void echo() throws Exception {
 		assertTrue("Error echo not echoing", Math.random() < 0.8);
 	}
 }
