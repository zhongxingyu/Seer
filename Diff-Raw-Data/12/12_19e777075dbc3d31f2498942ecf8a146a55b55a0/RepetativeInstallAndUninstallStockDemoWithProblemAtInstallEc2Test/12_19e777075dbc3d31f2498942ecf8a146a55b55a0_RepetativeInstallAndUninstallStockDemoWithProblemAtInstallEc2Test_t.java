 package test.cli.cloudify.cloud;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 import junit.framework.Assert;
 
 import org.apache.commons.io.FileUtils;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.CloudTestUtils;
 import test.cli.cloudify.CommandTestUtils;
 import test.cli.cloudify.cloud.ec2.Ec2CloudService;
 import framework.tools.SGTestHelper;
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 import framework.utils.WebUtils;
 /**
  * runs on ec2<p>
  * loops several times:<p>
 	1. try to install stockdemo on and fail<p>
 	2. uninstall<p>
 	3. try to install again <p>
 	
 	Details: the failour at step 1 is achieved by renaming cassandra's post start script.
 	before step 3 is done the script is renamed back to the original name, this is why step 3 
 	is asserted.
  * @author gal
  *
  */
 public class RepetativeInstallAndUninstallStockDemoWithProblemAtInstallEc2Test extends AbstractCloudTest {
 
 	private final int repetitions = 3 ;
 	private String cassandraPostStartScriptPath = null;
 	private String newPostStartScriptPath = null;
 	private Ec2CloudService service;
 	private URL stockdemoUrl;
 	
 	@BeforeMethod
 	public void bootstrap() throws IOException, InterruptedException {	
 		
 		File cloudPluginDir = new File(ScriptUtils.getBuildPath() + "/tools/cli/plugins/esc/ec2");
 		File bootstapManagementInSGTest = new File(SGTestHelper.getSGTestRootDir() + "/apps/cloudify/cloud/ec2/bootstrap-management.sh");
 		File bootstapManagementInBuild = new File(cloudPluginDir.getAbsolutePath() + "/upload/bootstrap-management.sh");
 		bootstapManagementInBuild.delete();
 		FileUtils.copyFile(bootstapManagementInSGTest, bootstapManagementInBuild);
 		
 		File xapLicense = new File(SGTestHelper.getSGTestRootDir() + "/apps/cloudify/cloud/gslicense.xml");
 		File cloudifyOverrides = new File(cloudPluginDir.getAbsolutePath() + "/upload/cloudify-overrides");
 		if (!cloudifyOverrides.exists()) {
 			cloudifyOverrides.mkdir();
 		}
 		FileUtils.copyFileToDirectory(xapLicense, cloudifyOverrides);
 		
 		
 		service = new Ec2CloudService();
 		service.setMachinePrefix(this.getClass().getName() + CloudTestUtils.SGTEST_MACHINE_PREFIX);
 		service.bootstrapCloud();
 		setService(service);
 		String hostIp = service.getRestUrl().substring(0, service.getRestUrl().lastIndexOf(':'));
		stockdemoUrl = new URL(hostIp + ":8080/stockdemo.StockDemo/");
 	}
 	
 	
 	@AfterMethod(alwaysRun = true)
 	public void teardown() throws IOException {
 		try {
 			service.teardownCloud();
 		}
 		catch (Throwable e) {
 			LogUtils.log("caught an exception while tearing down ec2", e);
 			sendTeardownCloudFailedMail("ec2", e);
 		}
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT *(1 + repetitions), groups = "1", enabled = true)
 	public void installAndUninstallTest() throws Exception {
 		
 		
 		final String stockdemoAppPath = CommandTestUtils.getPath("apps/USM/usm/applications/stockdemo");	
 		cassandraPostStartScriptPath = stockdemoAppPath + "/cassandra/cassandra_poststart.groovy";	
 		newPostStartScriptPath = stockdemoAppPath + "/cassandra/cassandra_poststart123.groovy";
 		int secondInstallationSuccessCounter = 0;
 		int secondInstallationFailCounter = 0;
 		int firstInstallSuccessCounter = 0;
 		
 		for(int i=0 ; i < repetitions ; i++){
 			LogUtils.log("starting iteration " + i);
 			switch(installUninstallInstall(stockdemoAppPath, cassandraPostStartScriptPath ,newPostStartScriptPath)){
 			case 1: {firstInstallSuccessCounter++;
 					break;
 					}
 			case 2: {secondInstallationSuccessCounter++;
 					break;
 					}
 			case 3: {secondInstallationFailCounter++;
 					break;
 					}				
 			}
 			LogUtils.log("uninstalling stockdemo after iteration " + i);
 			uninstallApplicationAndWait("stockdemo");
 			LogUtils.log("asserting all services are down");
 			assertUninstallWasSuccessful();
 		}
 		LogUtils.log(firstInstallSuccessCounter + "/" + repetitions + " times the first installation succeedded, this should not happen");
 		LogUtils.log(secondInstallationSuccessCounter + "/" + repetitions + " times the second installation succeedded");
 		LogUtils.log(secondInstallationFailCounter + "/" + repetitions + " times the second installation failed - THIS IS WHAT WE TEST FOR");
 		Assert.assertTrue("second install should never fail, it failed " + secondInstallationFailCounter + " times", secondInstallationFailCounter==0);
 	}
 
 	private int installUninstallInstall(String stockdemoAppPath, String cassandraPostStartScriptPath ,String  newPostStartScriptPath) throws Exception {
 		LogUtils.log("corrupting cassandra service");
 		corruptCassandraService(cassandraPostStartScriptPath ,newPostStartScriptPath);
 		try{
 			LogUtils.log("first installation of stockdemo - this should fail");
 			installApplication(stockdemoAppPath, "stockdemo", 5, true, true);		
 		}catch(AssertionError e){
 			return 1;
 		}
 		LogUtils.log("fixing cassandra service");
 		fixCassandraService(cassandraPostStartScriptPath , newPostStartScriptPath);
 		LogUtils.log("uninstalling stockdemo");
 		uninstallApplicationAndWait("stockdemo");
 		LogUtils.log("asserting all services are down");
 		assertUninstallWasSuccessful();
 		try{
 			LogUtils.log("second installation of stockdemo - this should succeed");
 			installApplication(stockdemoAppPath, "stockdemo", 5, true, true);
 			LogUtils.log("checking second installation's result");
 			Assert.assertTrue("The applications home page isn't available, counts as not installed properly" ,
 					WebUtils.isURLAvailable(stockdemoUrl));
 			return 2;
 		}catch(AssertionError e){
 			return 3;
 		}
 
 	}
 	
 	@Override
 	@AfterMethod
 	public void afterTest(){
 		try {
 			fixCassandraService(cassandraPostStartScriptPath , newPostStartScriptPath);
 		} catch (IOException e) {
 			LogUtils.log("FAILED FIXING CASSANDRA SERVICE AFTER TEST");
 		}
 	}
 
 	private void corruptCassandraService(String cassandraPostStartScriptPath , String newPostStartScriptPath) throws IOException {
 		File cassandraPostStartScript = new File(cassandraPostStartScriptPath);
 		boolean success = cassandraPostStartScript.renameTo(new File(newPostStartScriptPath));
 		if(!success)
 			throw new IOException("Test error: failed renaming " +  cassandraPostStartScriptPath + " to " + newPostStartScriptPath);
 		}
 	
 	private void fixCassandraService(String cassandraPostStartScriptPath , String newPostStartScriptPath) throws IOException {
 		File cassandraPostStartScript = new File(newPostStartScriptPath);
 		boolean success = cassandraPostStartScript.renameTo(new File(cassandraPostStartScriptPath));
 		if(!success)
 			throw new IOException("Test error: failed renaming " +  newPostStartScriptPath + " to " + cassandraPostStartScriptPath);
 	}
 	
 	private void assertUninstallWasSuccessful() throws Exception{
 		
 		URL cassandraPuAdminUrl = new URL(service.getRestUrl() + "/admin/ProcessingUnits/Names/stockdemo.cassandra");
 		URL stockAnalyticsMirrorPuAdminUrl = new URL(service.getRestUrl() + "/admin/ProcessingUnits/Names/stockdemo.stockAnalyticsMirror");
 		URL stockAnalyticsSpacePuAdminUrl = new URL(service.getRestUrl() + "/admin/ProcessingUnits/Names/stockdemo.stockAnalyticsSpace");
 		URL stockAnalyticsProcessorPuAdminUrl = new URL(service.getRestUrl() + "/admin/ProcessingUnits/Names/stockdemo.stockAnalyticsProcessor");
 		URL StockDemoPuAdminUrl = new URL(service.getRestUrl() + "/admin/ProcessingUnits/Names/stockdemo.StockDemo");
 		URL stockAnalyticsPuAdminUrl = new URL(service.getRestUrl() + "/admin/ProcessingUnits/Names/stockdemo.stockAnalytics");
 		URL stockAnalyticsFeederPuAdminUrl = new URL(service.getRestUrl() + "/admin/ProcessingUnits/Names/stockdemo.stockAnalyticsFeeder");
 		
 		assertTrue(!WebUtils.isURLAvailable(cassandraPuAdminUrl));
 		assertTrue(!WebUtils.isURLAvailable(stockAnalyticsMirrorPuAdminUrl));
 		assertTrue(!WebUtils.isURLAvailable(stockAnalyticsSpacePuAdminUrl));
 		assertTrue(!WebUtils.isURLAvailable(stockAnalyticsProcessorPuAdminUrl));
 		assertTrue(!WebUtils.isURLAvailable(StockDemoPuAdminUrl));
 		assertTrue(!WebUtils.isURLAvailable(stockAnalyticsPuAdminUrl));
 		assertTrue(!WebUtils.isURLAvailable(stockAnalyticsFeederPuAdminUrl));
 	}
 	
 
 	
 
 	
 }
