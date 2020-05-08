 package test.cli.cloudify.cloud.byon;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.openspaces.admin.Admin;
 import org.openspaces.admin.AdminFactory;
 import org.testng.ITestContext;
 
 import test.cli.cloudify.cloud.NewAbstractCloudTest;
 import test.cli.cloudify.cloud.services.byon.ByonCloudService;
 import framework.tools.SGTestHelper;
 import framework.utils.LogUtils;
 
 public class AbstractByonCloudTest extends NewAbstractCloudTest {
 	
 	protected Admin admin;
 
 	@Override
 	protected void bootstrap(ITestContext testContext) {
 		super.bootstrap(testContext);
 	}
 
 
 	@Override
 	protected void afterBootstrap() throws Exception {
 		super.afterBootstrap();
 		String[] managementHosts = getService().getRestUrls();
 		AdminFactory factory = new AdminFactory();
 		for (String host : managementHosts) {
 			LogUtils.log("creating admin");
			String ipNoHttp = host.substring(7); // remove "http://"
			factory.addLocators(ipNoHttp + ":" + CloudifyConstants.DEFAULT_LUS_PORT);
 		}
 		admin = factory.createAdmin();
 	}
 
 
 	public ByonCloudService getService() {
 		return (ByonCloudService) super.getService();
 	}
 	
 	@Override
 	protected void customizeCloud() throws Exception {
 		// use a script that does not install java
 		File standardBootstrapManagement = new File(getService().getPathToCloudFolder() + "/upload", "bootstrap-management.sh");
 		File customBootstrapManagement = new File(SGTestHelper.getSGTestRootDir() + "/apps/cloudify/cloud/byon/bootstrap-management.sh");
 		Map<File, File> filesToReplace = new HashMap<File, File>();
 		filesToReplace.put(standardBootstrapManagement, customBootstrapManagement);
 		getService().addFilesToReplace(filesToReplace);
 
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
