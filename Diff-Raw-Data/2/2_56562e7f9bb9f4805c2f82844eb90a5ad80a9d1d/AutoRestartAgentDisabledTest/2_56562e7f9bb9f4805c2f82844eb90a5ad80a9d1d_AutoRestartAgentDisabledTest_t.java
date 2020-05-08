 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon.agentrestart;
 
 import java.util.concurrent.TimeUnit;
 
 import iTests.framework.utils.LogUtils;
 import iTests.framework.utils.MavenUtils;
 import iTests.framework.utils.SSHUtils;
 
 import org.cloudifysource.dsl.utils.IPUtils;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 public class AutoRestartAgentDisabledTest extends AbstractAgentMaintenanceModeTest {
 
 	
 	private static final String LIST_CRONTAB_TASKS = "crontab -l";
 
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 	}
 	
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
     public void testRestartAgentDisabled() throws Exception {
     	final ProcessingUnit pu = admin.getProcessingUnits()
     			.waitFor("rest", DEFAULT_WAIT_MINUTES, TimeUnit.MINUTES);
     	pu.waitFor(1);
     	final String hostName = pu.getInstances()[0].getMachine().getHostName();
     	
     	final String ip = IPUtils.resolveHostNameToIp(hostName);
     	LogUtils.log("listing all crontab scheduled tasks");
     	final String output = SSHUtils.runCommand(ip, DEFAULT_TEST_TIMEOUT / 2,
 				LIST_CRONTAB_TASKS,
 				MavenUtils.username,
				MavenUtils.password, true);
     	LogUtils.log(output);
     	assertTrue("crontab contains auto restart agent task", 
     			!output.contains("@reboot nohup /tmp/tgrid/gigaspaces/tools/cli/cloudify.sh start-management"));
     }
     
 	@Override
 	protected void customizeCloud() throws Exception {
 		super.customizeCloud();
 		getService().setSudo(false);
 		getService().getProperties().put("keyFile", "testKey.pem");
 		getService().getAdditionalPropsToReplace().put("autoRestartAgent true", "autoRestartAgent false");
 	}
 	
 	@Override
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		uninstallServiceIfFound(SERVICE_NAME);
 		super.teardown();
 	}
 }
