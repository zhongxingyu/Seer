 package test.cli.cloudify;
 
 import static org.testng.AssertJUnit.fail;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.DeploymentStatus;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
 import org.openspaces.pu.service.CustomServiceMonitors;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Test;
 
 
 import com.gargoylesoftware.htmlunit.BrowserVersion;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gigaspaces.cloudify.dsl.internal.packaging.PackagingException;
 
 import framework.utils.AssertUtils;
 import framework.utils.LogUtils;
 import framework.utils.ProcessingUnitUtils;
 import framework.utils.SSHUtils;
 import framework.utils.ScriptUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 
 /**
  * 1. install tomcat service
  * 2. kill tomcat(the actual process, not the USM pu)
  * 3. assert tomcat is recovered by USM
  * 4. repeat steps 1-3
  * 5. kill tomcat in such a way the USM pu cannot launch it again
  * 6. assert USM pu shuts itself down, 
  * 	  allowing the gsm to redeploy it and thus recovering the tomcat
  * @author elip
  *
  */
 public class RepetitiveActualServiceFailoverTest extends AbstractCommandTest {
 	
 	Long tomcatPId;
 	Machine machineA;
 	WebClient client;
 	
 	@Override
 	@BeforeTest
 	public void beforeTest() {
 		super.beforeTest();
 		client = new WebClient(BrowserVersion.getDefault());
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
 	public void tomcatServiceDownAndCorruptedTest() throws IOException, InterruptedException, PackagingException {
 		
 		String serviceDir = ScriptUtils.getBuildPath() + "/recipes/tomcat";
 		String command = "connect " + restUrl + ";install-service --verbose -timeout 10 " + serviceDir;
 		try {
 			LogUtils.log("installing tomcat service using Cli");
 			runCommand(command);
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		ProcessingUnit tomcat = admin.getProcessingUnits().waitFor("tomcat", 10, TimeUnit.SECONDS);
 		machineA = admin.getGridServiceContainers().getContainers()[0].getMachine();
 		
 			
 		final CountDownLatch removed = new CountDownLatch(1);
 		final CountDownLatch added = new CountDownLatch(2);
 		
 		LogUtils.log("adding a lifecycle listener to tomcat pu");
 		ProcessingUnitInstanceLifecycleEventListener eventListener = new ProcessingUnitInstanceLifecycleEventListener() {
 			
 			@Override
 			public void processingUnitInstanceRemoved(
 					ProcessingUnitInstance processingUnitInstance) {
 				LogUtils.log("USM processing unit instance has been removed due to tomcat failure");
 				removed.countDown();	
 			}
 			
 			@Override
 			public void processingUnitInstanceAdded(
 					ProcessingUnitInstance processingUnitInstance) {
 				LogUtils.log("USM processing unit instance has been added");
 				added.countDown();	
 			}
 		};
 		tomcat.addLifecycleListener(eventListener);
 		
 		for (int i = 0 ; i < 2 ; i++) {
 			LogUtils.log("Killing tomcat process");
 			tomcatPId = getTomcatPId();
 			if (ScriptUtils.isWindows()) {	
 				int result = ScriptUtils.killWindowsProcess(tomcatPId.intValue());
 				assertTrue(result == 0);	
 			}
 			else {
 				SSHUtils.killProcess(machineA.getHostAddress(), tomcatPId.intValue());
 			}
 			LogUtils.log("Waiting for tomcat to recover");
 			RepetitiveConditionProvider condition = new RepetitiveConditionProvider() {	
 				@Override
 				public boolean getCondition() {
 					return isTomcatPageExists(client);
 				}
 			};
 			AssertUtils.repetitiveAssertTrue("cannot connect to tompage", condition, DEFAULT_TEST_TIMEOUT);
 		}
 		
 		LogUtils.log("Killing tomcat process and corrupting its install folder");
 		String pathToTomcat;
 		LogUtils.log("deleting catalina.sh/bat from pu folder");
 		String catalinaPath = "/work/processing-units/tomcat_1/ext/install/apache-tomcat-7.0.22/bin/catalina.";
 		String filePath = ScriptUtils.getBuildPath()+ catalinaPath;
 		if (ScriptUtils.isWindows()) {
 			pathToTomcat = filePath + "bat";
 		}
 		else {
 			pathToTomcat = filePath + "sh";
 		}
 		File tomcatRun = new File(pathToTomcat);
 		assertTrue(tomcatRun.delete());
 		LogUtils.log("killing tomcat process");
 		if (ScriptUtils.isWindows()) {	
 			int result = ScriptUtils.killWindowsProcess(tomcatPId.intValue());
 			assertTrue(result == 0);	
 		}
 		else {
 			SSHUtils.killProcess(machineA.getHostAddress(), tomcatPId.intValue());
 		}
 		
 		LogUtils.log("waiting for tomcat pu instances to decrease");
 		removed.await();
 		assertTrue("ProcessingUnitInstanceRemoved event has not been fired", removed.getCount() == 0);
 		LogUtils.log("waiting for tomcat pu instances to increase");
 		added.await();
 		assertTrue("ProcessingUnitInstanceAdded event has not been fired", added.getCount() == 0);	
 		LogUtils.log("verifiying tomcat service in running");
 		isTomcatPageExists(client);	
 		LogUtils.log("all's well that ends well :)");
 	}
 	
 	private boolean isTomcatPageExists(WebClient client) {
 		
         HtmlPage page = null;
         try {
             page = client.getPage("http://" + machineA.getHostAddress() + ":8080");
         } catch (IOException e) {
             fail(e.getMessage());
         }
         return "OK".equals(page.getWebResponse().getStatusMessage());
 		
 	}
 	
 	private Long getTomcatPId() {
 		
 		ProcessingUnit tomcat = admin.getProcessingUnits().getProcessingUnit("tomcat");
 		ProcessingUnitUtils.waitForDeploymentStatus(tomcat, DeploymentStatus.INTACT);
 		assertTrue(tomcat.getStatus().equals(DeploymentStatus.INTACT));
 		
 		ProcessingUnitInstance tomcatInstance = tomcat.getInstances()[0];	
 		CustomServiceMonitors customServiceDetails = (CustomServiceMonitors) tomcatInstance.getStatistics().getMonitors().get("USM");
 		return (Long) customServiceDetails.getMonitors().get("Actual Process ID");
 	}
 
 }
