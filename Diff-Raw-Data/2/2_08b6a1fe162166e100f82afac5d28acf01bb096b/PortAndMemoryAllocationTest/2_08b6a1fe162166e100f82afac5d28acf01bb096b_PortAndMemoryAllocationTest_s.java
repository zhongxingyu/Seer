 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketAddress;
 
 import org.cloudifysource.dsl.cloud.GridComponents;
 import org.openspaces.admin.machine.Machine;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import iTests.framework.utils.LogUtils;
 import iTests.framework.utils.SSHUtils;
 /**
  * This test uses a predefined cloud groovy holding all of the service grid port and memory config 
  * and asserts these config properties were indeed used.
  * 
  * @author adaml
  *
  */
 public class PortAndMemoryAllocationTest extends AbstractByonCloudTest {
 	private final static String CLOUD_GROOVY_PATH = CommandTestUtils.getPath("src/main/resources/apps/cloudify/cloud/byon/customCloudGroovy/byon-cloud.groovy");
 	private final static String SIMPLE_RECIPE_FOLDER = CommandTestUtils.getPath("src/main/resources/apps/USM/usm/simple");
 	private final static String SERVICE_NAME = "simple";
 	
 	private static final String GSA_PROCESS_NAME = "com.gigaspaces.start.services=\\\"GSA\\\""; //"GSA";
 	private static final String GSM_PROCESS_NAME = "com.gigaspaces.start.services=\\\"GSM\\\""; //"GSM";
 	private static final String LUS_PROCESS_NAME = "com.gigaspaces.start.services=\\\"LH\\\"";  //"LH";
 	private static final String ESM_PROCESS_NAME = "com.gigaspaces.start.services=\\\"ESM\\\""; //"ESM";
 	private static final String GSC_PROCESS_NAME = "com.gigaspaces.start.services=\\\"GSC\\\""; //"GSC";
 	private static final String REST_PROCESS_NAME = "com.gs.zones=rest";
 	private static final String WEBUI_PROCESS_NAME = "com.gs.zones=webui";
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void testPortAndMemoryAllocation() throws IOException, InterruptedException {
 		
 		installServiceAndWait(SIMPLE_RECIPE_FOLDER, SERVICE_NAME);
 		
 		assertRemotePortsOccupied();
 		
 		assertMemoryAllocated();
 	}
 	
 	private void assertRemotePortsOccupied() {
 		GridComponents components = getService().getCloud().getConfiguration().getComponents();
 		int initGscPort = getInitPortFromRange(components.getUsm().getPortRange());
 		String[] restUrls = getService().getRestUrls();
 		LogUtils.log("asserting management machine LRMI and web services ports are occupied");
 		for (String restUrl : restUrls) {
 			String restHostAddress = restUrl.substring(restUrl.lastIndexOf('/') + 1, restUrl.lastIndexOf(':'));
 			assertTrue("Agent LRMI port " + components.getAgent().getPort() + " not occupied as expected", 
 						isPortOccupied(restHostAddress, components.getAgent().getPort()));
 			assertTrue("GSM LRMI port " + components.getDeployer().getPort() + " not occupied as expected", 
 						isPortOccupied(restHostAddress, components.getDeployer().getPort()));
 			assertTrue("GSM Webster port " + components.getDeployer().getPort() + " not occupied as expected", 
 						isPortOccupied(restHostAddress, components.getDeployer().getWebsterPort()));
 			assertTrue("GSM Webster port " + components.getDeployer().getPort() + " not occupied as expected", 
 					isPortOccupied(restHostAddress, components.getDeployer().getWebsterPort()));
 			assertTrue("LUS LRMI port " + components.getDiscovery().getPort() + " not occupied as expected", 
 					isPortOccupied(restHostAddress, components.getDiscovery().getPort()));
 			assertTrue("LUS discovery port " + components.getDiscovery().getDiscoveryPort() + " not occupied as expected", 
 					isPortOccupied(restHostAddress, components.getDiscovery().getDiscoveryPort()));
 			assertTrue("ESM LRMI port " + components.getOrchestrator().getPort() + " not occupied as expected", 
 					isPortOccupied(restHostAddress, components.getOrchestrator().getPort()));
 			assertTrue("REST port " + components.getRest().getPort() + " not occupied as expected", 
 					isPortOccupied(restHostAddress, components.getRest().getPort()));
 			assertTrue("WEBUI port " + components.getWebui().getPort() + " not occupied as expected", 
 					isPortOccupied(restHostAddress, components.getWebui().getPort()));
 			
 			//The three GSCs LRMI ports (RESTful, Webui and space)
 			assertTrue("GSC LRMI port " + initGscPort + " not occupied as expected", 
 					isPortOccupied(restHostAddress, initGscPort));
 			assertTrue("GSC LRMI port " + initGscPort + 1 + " not occupied as expected", 
 					isPortOccupied(restHostAddress, initGscPort + 1));
 			assertTrue("GSC LRMI port " + initGscPort + 2 + " not occupied as expected", 
 					isPortOccupied(restHostAddress, initGscPort + 2));
 		}
 		LogUtils.log("asserting service machine LRMI ports are occupied");
 		//validate new service machine agent and gsc port
 		String serviceHostAddress = getServiceHostAddress();
 		assertTrue("Agent LRMI port " + components.getAgent().getPort() + " not occupied as expected", 
 				isPortOccupied(serviceHostAddress, components.getAgent().getPort()));
 		assertTrue("GSC LRMI port " + initGscPort + " not occupied as expected", 
 				isPortOccupied(serviceHostAddress, initGscPort));
 	}
 
 	private void assertMemoryAllocated() {
 		GridComponents components = getService().getCloud().getConfiguration().getComponents();
 		String[] restUrls = getService().getRestUrls();
 		for (String restUrl : restUrls) {
 			String host = restUrl.substring(restUrl.lastIndexOf('/') + 1, restUrl.lastIndexOf(':'));
 			assertComponentMemory(components.getAgent().getMaxMemory(), components.getAgent().getMinMemory(), host, GSA_PROCESS_NAME);
 			assertComponentMemory(components.getDeployer().getMaxMemory(), components.getDeployer().getMinMemory(), host, GSM_PROCESS_NAME);
 			assertComponentMemory(components.getDiscovery().getMaxMemory(), components.getDiscovery().getMinMemory(), host, LUS_PROCESS_NAME);
 			assertComponentMemory(components.getOrchestrator().getMaxMemory(), components.getOrchestrator().getMinMemory(), host, ESM_PROCESS_NAME);
 			assertComponentMemory(components.getRest().getMaxMemory(), components.getRest().getMinMemory(), host, REST_PROCESS_NAME);
 			assertComponentMemory(components.getWebui().getMaxMemory(), components.getWebui().getMinMemory(), host, WEBUI_PROCESS_NAME);
 		}
 		String serviceHostAddress = getServiceHostAddress();
		assertComponentMemory(components.getUsm().getMaxMemory(), components.getUsm().getMinMemory(), serviceHostAddress, GSA_PROCESS_NAME);
 		assertComponentMemory(components.getUsm().getMaxMemory(), components.getUsm().getMinMemory(), serviceHostAddress, GSC_PROCESS_NAME);
 	}
 
 	private void assertComponentMemory(String maxMemory, String minMemory,
 			String host, String componentName) {
 		LogUtils.log("asserting component " + componentName + " was allocated with the configured memory size");
 		String componentCommandlineArgs = getComponentCommandlineArgs(host, componentName);
 		int maxMemoryIndex = componentCommandlineArgs.lastIndexOf("-Xmx");
 		int minMemoryIndex = componentCommandlineArgs.lastIndexOf("-Xms");
 		LogUtils.log("component " + componentName + " commandline args are " + componentCommandlineArgs);
 		String componentMaxMemory = componentCommandlineArgs.substring(maxMemoryIndex, maxMemoryIndex + "-Xmx".length() + maxMemory.length());
 		String componentMinMemory = componentCommandlineArgs.substring(minMemoryIndex, minMemoryIndex + "-Xms".length() + minMemory.length());
 		assertTrue("component " + componentName + " was not allocated with the expected max memory of " + maxMemory + " Instead found " + componentMaxMemory
 				+ " commandLine Args are: " + componentCommandlineArgs,componentMaxMemory.endsWith(maxMemory));
 		//rest and webui currently do not override the min memory option.
 		if (!componentName.equals(REST_PROCESS_NAME) && !componentName.equals(WEBUI_PROCESS_NAME))
 		assertTrue("component " + componentName + " was not allocated with the expected min memory of " + minMemory + " Instead found " + componentMinMemory
 				+ " commandLine Args are: " + componentCommandlineArgs, componentMinMemory.endsWith(minMemory));
 	}
 
 	String getServiceHostAddress() {
 		String serviceHostAddress;
 		Machine[] machines = admin.getMachines().getMachines();
 		Machine machineA = machines[0];
 		Machine machineB = machines[1];
 		if (isManagement(machineA.getHostAddress())) {
 			serviceHostAddress = machineB.getHostAddress();
 		} else {
 			serviceHostAddress = machineA.getHostAddress();
 		}
 		return serviceHostAddress;
 	}
 	
 	private boolean isManagement(String hostAddress) {
 		return getService().getIpList().contains(hostAddress);
 	}
 
 	private static String getComponentCommandlineArgs(String host, String processName) {
 		LogUtils.log("executing remote command: ps -ef | grep " + processName + " on host " + host);
 		String output = SSHUtils.runCommand(host, 10000, "ps -ef| grep " + processName, "tgrid", "tgrid");
 		return output;
 	}
 	
 	private int getInitPortFromRange(String portRange) {
 		return Integer.parseInt(portRange.substring(0,portRange.lastIndexOf('-')));
 	}
 
 	
 	private boolean isPortOccupied(String host, int port) {
 	    Socket s = null;
 	    try {
 	        s = new Socket();
 	        s.setReuseAddress(true);
 	        SocketAddress sa = new InetSocketAddress(host, port);
 	        s.connect(sa, 1000);
 	        return true;
 	    } catch (IOException e) {
 	        e.printStackTrace();
 	    } finally {
 	        if (s != null) {
 	            try {
 	                s.close();
 	                s = null;
 	            } catch (IOException e) {
 	            }
 	        }
 	    }
 	    return false;
 	}
 	
 	@Override
 	protected void customizeCloud() throws Exception {
 		File newCloudFile = new File(CLOUD_GROOVY_PATH);
 		getService().setCloudGroovy(newCloudFile);
 		super.customizeCloud();
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		super.teardown();
 	}
 }
