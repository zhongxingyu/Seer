 package test.cli.cloudify.cloud.byon;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.Assert;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.cloud.services.byon.MultipleTemplatesByonCloudService;
 import framework.tools.SGTestHelper;
 import framework.utils.AssertUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 import framework.utils.IOUtils;
 import framework.utils.LogUtils;
 
 /**
  * This test installs petclinic with 3 different templates on a byon cloud. It checks that each service was
  * assigned to the correct template, according to byon-cloud.groovy. After the installation completes, the
  * test checks the uninstall and teardown operations.
  * 
  * Note: this test uses 5 fixed machines -
  * 192.168.9.115,192.168.9.116,192.168.9.120,192.168.9.125,192.168.9.126.
  */
 public class MultipleMachineTemplatesTest extends AbstractByonCloudTest {
 	
 	/* holds which template is used by which service */
 	protected Map<String, String> templatePerService = new HashMap<String, String>();
 	
 	private MultipleTemplatesByonCloudService service = new MultipleTemplatesByonCloudService();
 	
 	private static final long MY_OPERATION_TIMEOUT = 1 * 60 * 1000;
 
 	protected static final String PETCLINIC_MULTIPLE_TEMPLATES_SG_PATH = SGTestHelper.getSGTestRootDir() + "/src/main/resources/apps/cloudify/recipes/petclinic-multiple-templates";
 	protected static final String PETCLINIC_MULTIPLE_TEMPLATES_BUILD_PATH = SGTestHelper.getBuildDir() + "/recipes/apps/petclinic-multiple-templates";
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		super.bootstrap(service);
 	}
 
 	@BeforeMethod(alwaysRun = true)
 	public void perpareApplication() throws IOException {					
 		LogUtils.log("copying application " + PETCLINIC_MULTIPLE_TEMPLATES_SG_PATH + " to " + PETCLINIC_MULTIPLE_TEMPLATES_BUILD_PATH);
 		FileUtils.copyDirectory(new File(PETCLINIC_MULTIPLE_TEMPLATES_SG_PATH), new File(PETCLINIC_MULTIPLE_TEMPLATES_BUILD_PATH));
 	}
 
 	/**
 	 * check that each service was assigned to the correct template, according to byon-cloud.groovy.
 	 * 
 	 * @throws Exception
 	 */
	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true, priority = 1)
 	public void test() throws Exception {
 		
 		String[] temlpateNames = service.getTemlpateNames();
 		
 		injectTemplateInService(temlpateNames[0], "mongos");
 		injectTemplateInService(temlpateNames[0], "mongoConfig");
 		injectTemplateInService(temlpateNames[1], "tomcat");
 		injectTemplateInService(temlpateNames[1], "apacheLB");
 		injectTemplateInService(temlpateNames[2], "mongod");
 		
 		installApplicationAndWait(PETCLINIC_MULTIPLE_TEMPLATES_BUILD_PATH, "petclinic");
 
 		Map<String, List<String>> hostsPerTemplate = service.getHostsPerTemplate();
 		
 		LogUtils.log("hosts per template = " + hostsPerTemplate);
 		LogUtils.log("template per service = " + templatePerService);
 		Assert.assertTrue(hostsPerTemplate.get(templatePerService.get("petclinic.mongod")).contains(getPuHost("petclinic.mongod")));
 		Assert.assertTrue(hostsPerTemplate.get(templatePerService.get("petclinic.mongos")).contains(getPuHost("petclinic.mongos")));
 		Assert.assertTrue(hostsPerTemplate.get(templatePerService.get("petclinic.mongoConfig")).contains(getPuHost("petclinic.mongoConfig")));
 		Assert.assertTrue(hostsPerTemplate.get(templatePerService.get("petclinic.tomcat")).contains(getPuHost("petclinic.tomcat")));
 		Assert.assertTrue(hostsPerTemplate.get(templatePerService.get("petclinic.apacheLB")).contains(getPuHost("petclinic.apacheLB")));
 		
 		uninstallApplicationAndWait("petclinic");
 
 		assertServiceIsDown("petclinic.mongod");
 		assertServiceIsDown("petclinic.mongos");
 		assertServiceIsDown("petclinic.mongoConfig");
 		assertServiceIsDown("petclinic.tomcat");
 		assertServiceIsDown("petclinic.apacheLB");
 		
 		super.scanForLeakedAgentNodes();
 	}
 	
 	protected void setService(MultipleTemplatesByonCloudService service) {
 		this.service = service;
 	}
 	
 	private void assertManagementIsDown() {		
 		assertServiceIsDown("webui");
 		assertServiceIsDown("rest");
 		assertServiceIsDown("cloudifyManagementSpace");
 	}
 
 	protected void injectTemplateInService(final String template, String serviceName) throws IOException {
 		
 		String serviceGroovyPath = PETCLINIC_MULTIPLE_TEMPLATES_BUILD_PATH + "/" + serviceName + "/" + serviceName + "-service.groovy";
 		
 		// "ENTER_TEMPLATE" is the hook that resides in the service file for purposes of replacing it
 		LogUtils.log("Service " + serviceName + " will use template " + template);
 		IOUtils.replaceTextInFile(serviceGroovyPath, "ENTER_TEMPLATE", template);
 		templatePerService.put(ServiceUtils.getAbsolutePUName("petclinic", serviceName), template);
 		
 	}
 	
 	@AfterMethod(alwaysRun = true)
 	public void cleanup() throws IOException, InterruptedException {
 		super.uninstallApplicationIfFound("petclinic");
 		super.scanForLeakedAgentNodes();
 	}
 
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		super.teardown();
 	}
 
 	/**
 	 * Gets the address of the machine on which the given processing unit is deployed. 
 	 * @param puName The name of the processing unit to look for
 	 * @return The address of the machine on which the processing unit is deployed.
 	 */
 	protected String getPuHost(final String puName) {
 		ProcessingUnit pu = admin.getProcessingUnits().getProcessingUnit(puName);
 		Assert.assertNotNull(pu.getInstances()[0], puName + " processing unit is not found");
 		return pu.getInstances()[0].getMachine().getHostAddress();		
 	}
 	
 	@Override
 	public void beforeTeardown() {
 		// override so to not close admin. we will use it after teardown.
 	}
 
 	@Override
 	protected void afterTeardown() throws Exception {		
 		assertManagementIsDown();
 		super.closeAdmin();
 	}
 
 	private void assertServiceIsDown(final String serviceFullName) {
 		AssertUtils.repetitiveAssertTrue(serviceFullName + " is not down", new RepetitiveConditionProvider() {
 			public boolean getCondition() {
 				try {
 					return (admin.getProcessingUnits().getProcessingUnit(serviceFullName) == null);
 				} catch (Exception e) {
 					return false;
 				}
 			}
 		}, MY_OPERATION_TIMEOUT);
 	}
 }
