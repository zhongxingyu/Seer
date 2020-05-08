 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon.agentrestart;
 
 import iTests.framework.utils.LogUtils;
 import iTests.framework.utils.MavenUtils;
 import iTests.framework.utils.SSHUtils;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.concurrent.TimeUnit;
 
 import org.cloudifysource.dsl.utils.IPUtils;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon.AbstractByonCloudTest;
 import org.openspaces.admin.pu.ProcessingUnit;
 
 public class AbstractAgentMaintenanceModeTest extends AbstractByonCloudTest {
 
 	protected static final String SERVICE_NAME = "simpleRestartAgent";
 	protected static final String APP_NAME = "default";
 	protected final static String absolutePuName = ServiceUtils.getAbsolutePUName(APP_NAME, SERVICE_NAME);
 	protected static final String REBOOT_COMMAND = "sudo reboot";
 	protected static final long DEFAULT_WAIT_MINUTES = DEFAULT_TEST_TIMEOUT / 2;
 	
 	
 	protected void startMaintenanceMode(final long timeoutInSeconds) throws IOException, InterruptedException {
     	CommandTestUtils.runCommand("connect " + this.getRestUrl() + ";" 
     			+ " invoke simpleRestartAgent startMaintenanceMode " + timeoutInSeconds);
 	}
     
 	protected void stopMaintenanceMode(final String processingUnitName) throws IOException, InterruptedException {
 		CommandTestUtils.runCommand("connect " + this.getRestUrl() + ";" 
 				+ " invoke simpleRestartAgent stopMaintenanceMode");
 	}
     
     protected String getServicePath(final String serviceName) {
     	return CommandTestUtils.getPath("src/main/resources/apps/USM/usm/" + serviceName);
     }
     
     	
     protected void restartAgentMachine(final String puName) throws IOException {
     	final ProcessingUnit pu = admin.getProcessingUnits()
     			.waitFor(puName, DEFAULT_WAIT_MINUTES, TimeUnit.MINUTES);
    	pu.waitFor(1);
     	final String hostName = pu.getInstances()[0].getMachine().getHostName();
     	
     	final String ip = IPUtils.resolveHostNameToIp(hostName);
     	assertMachineState(ip, true);
     	LogUtils.log("rebooting machine with ip " + ip);
     	LogUtils.log(SSHUtils.runCommand(ip, DEFAULT_TEST_TIMEOUT / 2, 
     						REBOOT_COMMAND, 
     						MavenUtils.username, 
     						MavenUtils.password));
     	assertMachineState(ip, false);
     	LogUtils.log("Machine with ip " + ip + " is rebooting...");
     	
     	//For some reason, when restarting machine using custom command, the machine stops.
 //    	final String connectCommand 		= "connect " + this.getRestUrl();
 //    	final String getOpSystemCommand 	= connectCommand + ";invoke simpleRestartAgent getOpSystem";
 //    	final String operatingSystem 		= CommandTestUtils.runCommandAndWait(getOpSystemCommand);
 //    	String restartCommand 				= connectCommand;
 //    	
 //    	if (operatingSystem.contains("Linux") || operatingSystem.contains("Mac OS X")) {
 //    		restartCommand 					+= ";invoke simpleRestartAgent restartLinux";
 //    	} else if (operatingSystem.contains("Windows")) {
 //    		restartCommand 					+= ";invoke simpleRestartAgent restartWindows";
 //    	} else {
 //    		throw new RuntimeException("Unsupported operating system.");
 //    	}
 //
 //    	String restartOut = CommandTestUtils.runCommandAndWait(restartCommand);
 //    	assertTrue(restartOut.contains("invocation completed successfully."));
     }
 	
     private void assertMachineState(String ip, boolean isRunningExpected) 
     		throws UnknownHostException, IOException {
     	LogUtils.log("checking if machine is running");
     	if (isRunningExpected) {
     		assertTrue("Machine is expected to be running", InetAddress.getByName(ip).isReachable(3000));
     		LogUtils.log("Machine with ip " + ip + " is running");
     	} else {
     		assertTrue("Expecting machine to be in resart mode", InetAddress.getByName(ip).isReachable(3000));
     		LogUtils.log("Machine with ip " + ip + " is rebooting");
     	}
     }
     
 	protected void assertNumberOfMachines(final int numberOfMachines) {
 		assertTrue("expecting number of machines to be " + numberOfMachines + " but found " +  admin.getMachines().getSize(),
 				admin.getMachines().getSize() == numberOfMachines);
 	}
 	
 	protected void gracefullyShutdownAgent(final String puName) throws IOException, InterruptedException {
     	final String connectCommand 		= "connect " + this.getRestUrl();
     	final String shutdownCommand 	= connectCommand + ";invoke simpleRestartAgent shutdownAgent";
     	String shutdownOut = CommandTestUtils.runCommandAndWait(shutdownCommand);
     	assertTrue(shutdownOut.contains("invocation completed successfully."));
     }
 
 }
