 package test.cli.cloudify.pu;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.URL;
 
 import junit.framework.Assert;
 
 import org.apache.commons.io.FileUtils;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.AbstractLocalCloudTest;
 import test.cli.cloudify.CommandTestUtils;
 import framework.utils.ApplicationInstaller;
 import framework.utils.AssertUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 import framework.utils.LogUtils;
 import framework.utils.WebUtils;
 /**
  * runs on localcloud<p>
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
 public class RepetativeInstallAndUninstallStockDemoWithProblemAtInstallTest extends AbstractLocalCloudTest {
 
 	private final int repetitions = 4 ;
 	private String cassandraPostStartScriptPath = null;
 	private String newPostStartScriptPath = null;
 	private URL stockdemoUrl;
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * repetitions *2 , groups = "1", enabled = true)
 	public void installAndUninstallTest() throws Exception {
 
 		stockdemoUrl = new URL("http://" + InetAddress.getLocalHost().getHostAddress() + ":8080/stockdemo.StockDemo/");
 		String stockdemoAppPath = CommandTestUtils.getPath("src/main/resources/apps/USM/usm/applications/stockdemo");
 		
 		final File workStockDemoAppFolder = new File(new File(stockdemoAppPath).getParent(), "stockdemo-work");
 		
		FileUtils.copyFile(new File(stockdemoAppPath), workStockDemoAppFolder);
 		
 		stockdemoAppPath = workStockDemoAppFolder.getAbsolutePath();
 		
 		
 		cassandraPostStartScriptPath = stockdemoAppPath + "/cassandra/cassandra_poststart.groovy";	
 		newPostStartScriptPath = stockdemoAppPath + "/cassandra/cassandra_poststart123.groovy";
 		int secondInstallationSuccessCounter = 0;
 		int secondInstallationFailCounter = 0;
 		int firstInstallSuccessCounter = 0;
 
 		for(int i=0 ; i < repetitions ; i++){
 			LogUtils.log("starting iteration " + i);
 			switch(installUninstallInstall(stockdemoAppPath, cassandraPostStartScriptPath ,newPostStartScriptPath)){
 			case 1: {firstInstallSuccessCounter++;break;}
 			case 2: {secondInstallationSuccessCounter++;break;}
 			case 3: {secondInstallationFailCounter++;break;}	
 			}
 			LogUtils.log("uninstalling stockdemo after iteration " + i);
 			runCommand("connect " + restUrl + ";uninstall-application --verbose stockdemo");
 			LogUtils.log("asserting all services are down");
 			assertUninstallWasSuccessful();
 		}
 		LogUtils.log(firstInstallSuccessCounter + "/" + repetitions + " times the first installation succeedded, these runs are irrelavent");
 		LogUtils.log(secondInstallationSuccessCounter + "/" + repetitions + " times the second installation succeedded");
 		LogUtils.log(secondInstallationFailCounter + "/" + repetitions + " times the second installation failed - THIS IS WHAT WE TEST FOR");
 		Assert.assertTrue("second install should never fail, it failed " + secondInstallationFailCounter + " times", secondInstallationFailCounter==0);
 	}
 
 	private int installUninstallInstall(String stockdemoAppPath, String cassandraPostStartScriptPath ,String  newPostStartScriptPath) throws Exception {
 		LogUtils.log("corrupting cassandra service");
 		corruptCassandraService(cassandraPostStartScriptPath ,newPostStartScriptPath);
 		LogUtils.log("first installation of stockdemo - this should fail");
 		String failOutput = CommandTestUtils.runCommand("connect " + restUrl + ";install-application --verbose -timeout 2 " + stockdemoAppPath, true, true);		
 		if(!failOutput.toLowerCase().contains("operation failed"))
 			return 1;
 		LogUtils.log("fixing cassandra service");
 		fixCassandraService(cassandraPostStartScriptPath , newPostStartScriptPath);
 		LogUtils.log("uninstalling stockdemo");
 		runCommand("connect " + restUrl + ";uninstall-application --verbose stockdemo");
 		LogUtils.log("asserting all services are down");
 		assertUninstallWasSuccessful();
 		LogUtils.log("second installation of stockdemo - this should succeed");
 		String successOutput = CommandTestUtils.runCommand("connect " + restUrl + ";install-application --verbose -timeout 5 " + stockdemoAppPath, true, true);
 		LogUtils.log("checking second installation's result");
 		if(successOutput.toLowerCase().contains("installed successfully") && WebUtils.isURLAvailable(stockdemoUrl))
 			return 2;
 		else
 			return 3;
 	}
 
 	@Override
 	@AfterMethod
 	public void afterTest() throws Exception {
 		super.afterTest();
 		try {
 			if ((newPostStartScriptPath != null) && (cassandraPostStartScriptPath != null)) {
 				fixCassandraService(cassandraPostStartScriptPath , newPostStartScriptPath);				
 			}
 		} catch (IOException e) {
 			LogUtils.log("FAILED FIXING CASSANDRA SERVICE!!!");
 		}
 	}
 	
 	@AfterClass(alwaysRun = true)
 	public void uninstallApplicationIfFound() {
 		ApplicationInstaller uninstaller = new ApplicationInstaller(restUrl, "stockdemo");
 		uninstaller.timeoutInMinutes(5);
 		uninstaller.uninstallIfFound();
 	}
 
 	private void corruptCassandraService(String cassandraPostStartScriptPath , String newPostStartScriptPath) throws IOException {
 		File cassandraPostStartScript = new File(cassandraPostStartScriptPath);
 		boolean success = cassandraPostStartScript.renameTo(new File(newPostStartScriptPath));
 		if(!success)
 			throw new IOException("Test error: failed renaming " +  cassandraPostStartScriptPath + " to " + newPostStartScriptPath);
 	}
 
 	private void fixCassandraService(String cassandraPostStartScriptPath , String newPostStartScriptPath) throws IOException {
 		File cassandraPostStartScript = new File(newPostStartScriptPath);
 		if(!cassandraPostStartScript.exists())
 			return;
 		boolean success = cassandraPostStartScript.renameTo(new File(cassandraPostStartScriptPath));
 		if(!success)
 			throw new IOException("Test error: failed renaming " +  newPostStartScriptPath + " to " + cassandraPostStartScriptPath);
 	}
 
 	private void assertUninstallWasSuccessful() throws Exception{
 
 		URL cassandraPuAdminUrl = new URL(restUrl + "/admin/ProcessingUnits/Names/stockdemo.cassandra");
 		URL stockAnalyticsMirrorPuAdminUrl = new URL(restUrl + "/admin/ProcessingUnits/Names/stockdemo.stockAnalyticsMirror");
 		URL stockAnalyticsSpacePuAdminUrl = new URL(restUrl + "/admin/ProcessingUnits/Names/stockdemo.stockAnalyticsSpace");
 		URL stockAnalyticsProcessorPuAdminUrl = new URL(restUrl + "/admin/ProcessingUnits/Names/stockdemo.stockAnalyticsProcessor");
 		URL stockDemoPuAdminUrl = new URL(restUrl + "/admin/ProcessingUnits/Names/stockdemo.StockDemo");
 		URL stockAnalyticsPuAdminUrl = new URL(restUrl + "/admin/ProcessingUnits/Names/stockdemo.stockAnalytics");
 		URL stockAnalyticsFeederPuAdminUrl = new URL(restUrl + "/admin/ProcessingUnits/Names/stockdemo.stockAnalyticsFeeder");
 
 		repetitiveAssertUrlIsNotAvailable(cassandraPuAdminUrl);
 		repetitiveAssertUrlIsNotAvailable(stockAnalyticsMirrorPuAdminUrl);
 		repetitiveAssertUrlIsNotAvailable(stockAnalyticsSpacePuAdminUrl);
 		repetitiveAssertUrlIsNotAvailable(stockAnalyticsProcessorPuAdminUrl);
 		repetitiveAssertUrlIsNotAvailable(stockDemoPuAdminUrl);
 		repetitiveAssertUrlIsNotAvailable(stockAnalyticsPuAdminUrl);
 		repetitiveAssertUrlIsNotAvailable(stockAnalyticsFeederPuAdminUrl);
 	}
 
 	private void repetitiveAssertUrlIsNotAvailable(final URL url) {
 		
 		RepetitiveConditionProvider conditionProvider = new RepetitiveConditionProvider() {
 			
 			@Override
 			public boolean getCondition() {
 				try {
 					LogUtils.log("Checking if url " + url + " is available.");
 					boolean urlAvailable = WebUtils.isURLAvailable(url);
 					LogUtils.log("result = " + urlAvailable);
 					return (!urlAvailable);
 				} catch (Exception e) {
 					LogUtils.log("Caught an exception while polling url : " + e.getMessage());
 					return false;
 				}
 			}
 		};
 		AssertUtils.repetitiveAssertTrue("URL = " + url + " is still available. waited for 10 seconds", conditionProvider, 10 * 1000);
 	}
 		
 	}
 
