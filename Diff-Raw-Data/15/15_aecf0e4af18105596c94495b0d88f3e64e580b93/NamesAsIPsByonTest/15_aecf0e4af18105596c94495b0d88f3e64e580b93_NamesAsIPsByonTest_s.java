 package test.cli.cloudify.cloud.byon;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.openspaces.admin.Admin;
 import org.openspaces.admin.AdminFactory;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.cloud.NewAbstractCloudTest;
 import test.cli.cloudify.cloud.services.byon.ByonCloudService;
 import framework.tools.SGTestHelper;
 import framework.utils.AssertUtils;
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 
 
 /**
  * This test installs petclinic-simple on byon after changing the byon-cloud.groovy file to contain machine names instead of IPs.
  * <p>It checks whether the bootstrap and instal have succeeded.
  * <p>Note: this test uses 3 fixed machines - 192.168.9.115, 192.168.9.116, 192.168.9.120.
  */
 public class NamesAsIPsByonTest extends NewAbstractCloudTest{
 
 	private static final String TEST_UNIQUE_NAME = "NamesAsIPsByonTest";
 	private static final String IP_LIST_PROPERTY = "ipList";
 
 	private String namesList = "pc-lab95,pc-lab96,pc-lab100";
 	
 	private Admin admin;
 	
 	@Override
 	protected void customizeCloud() throws Exception {
 		
 		System.setProperty(IP_LIST_PROPERTY, namesList);
 		ByonCloudService byonService = (ByonCloudService) cloud;
 		byonService.setMachinePrefix(this.getClass().getName());
 
 		// replace the default bootstap-management.sh with a multicast version one
 		File standardBootstrapManagement = new File(byonService.getPathToCloudFolder() + "/upload", "bootstrap-management.sh");
 		File customBootstrapManagement = new File(SGTestHelper.getSGTestRootDir() + "/apps/cloudify/cloud/byon/bootstrap-management-" + byonService.getServiceFolder() + ".sh");
 		Map<File, File> filesToReplace = new HashMap<File, File>();
 		filesToReplace.put(standardBootstrapManagement, customBootstrapManagement);
 		byonService.addFilesToReplace(filesToReplace);
 	}
 
 	@Override
 	protected void afterBootstrap() throws Exception {
 		LogUtils.log("creating admin");
 		AdminFactory factory = new AdminFactory();
 		factory.addGroup(TEST_UNIQUE_NAME);
 		admin = factory.createAdmin();
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void testPetclinic() throws IOException, InterruptedException{
 
 		installApplicationAndWait(ScriptUtils.getBuildPath() + "/recipes/apps/petclinic-simple", "petclinic");
 		
 		//TODO : edit this, so if it fails it won't be on NPE!
 		AssertUtils.assertTrue("petclinic.mongod is not up - install failed", admin.getProcessingUnits().getProcessingUnit("petclinic.mongod") != null);		
 		AssertUtils.assertTrue("petclinic.tomcat is not up - install failed", admin.getProcessingUnits().getProcessingUnit("petclinic.tomcat") != null);
 	}
 	
 	@Override
 	protected void beforeTeardown() throws Exception {
 		// clean
 		if (admin != null) {
 			admin.close();
 			admin = null;
 		}
 		
 		try{
 			uninstallApplicationAndWait("petclinic");
 		} catch(Exception e) {
 			LogUtils.log("Failed to uninstall application petclinic", e);
 		} finally {
 			System.clearProperty(IP_LIST_PROPERTY);
 		}
 	}
 	
 	@Override
 	protected String getCloudName() {
 		return "byon";
 	}
 
 	@Override
 	protected boolean isReusableCloud() {
 		return false;
 	}
 
 }
