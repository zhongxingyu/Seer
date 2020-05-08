 package test.cli.cloudify.cloud.byon;
 
 import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.openspaces.admin.Admin;
 import org.openspaces.admin.AdminFactory;
 import org.testng.ITestContext;
 
 import test.cli.cloudify.cloud.NewAbstractCloudTest;
 import test.cli.cloudify.cloud.services.byon.ByonCloudService;
 import framework.utils.LogUtils;
 
 public class AbstractByonCloudTest extends NewAbstractCloudTest {
 	
 	@Override
 	protected void beforeTeardown() throws Exception {
 		closeAdmin();
 	}
 
 	protected Admin admin;
 	
 	public ByonCloudService getService() {
 		return (ByonCloudService) super.getService();
 	}
 
 
 	@Override
 	protected void bootstrap(ITestContext testContext) {
 		super.bootstrap(testContext);
 	}
 
 
 	@Override
 	protected void afterBootstrap() throws Exception {
 		super.afterBootstrap();
 		createAdmin();
 	}
 
 
 	private void createAdmin() {
 		String[] managementHosts = getService().getRestUrls();
 		AdminFactory factory = new AdminFactory();
 		for (String host : managementHosts) {
 			LogUtils.log("creating admin");
 			String utlNoHttp = host.substring(7); /* remove "http://" */
 			String ip = utlNoHttp.split(":")[0];
 			factory.addLocators(ip + ":" + CloudifyConstants.DEFAULT_LUS_PORT);
 		}
 		admin = factory.createAdmin();
 	}
 	
 	private void closeAdmin() {
 		if (admin != null) {
 			admin.close();
 			admin = null;
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


	@Override
	protected void customizeCloud() throws Exception {
		// TODO Auto-generated method stub
		
	}
 }
