 package test.cli.cloudify.xen;
 
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.internal.gsa.InternalGridServiceAgent;
 import org.openspaces.admin.lus.LookupService;
 import org.openspaces.cloud.xenserver.XenServerMachineProvisioningConfig;
 import org.testng.annotations.BeforeMethod;
 
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 import framework.utils.LogUtils;
 import framework.utils.SSHUtils;
 import framework.utils.WebUtils;
 import framework.utils.xen.AbstractXenGSMTest;
 
 public class AbstractStartManagementXenTest extends AbstractXenGSMTest {
 	
     private static final String SHUTDOWN_MANAGEMENT_COMMAND = "/opt/gigaspaces/tools/cli/cloudify.sh shutdown-management --verbose";
 	private static final String SHUTDOWN_AGENT_COMMAND = "/opt/gigaspaces/tools/cli/cloudify.sh shutdown-agent --verbose -force";
 		
 	@Override
 	protected void overrideXenServerProperties(XenServerMachineProvisioningConfig machineProvisioningConfig) {
         super.overrideXenServerProperties(machineProvisioningConfig);
         machineProvisioningConfig.setFileAgentRemoteLocation("/opt/cloudify-start-management.sh");
         
         // skipping the first (as it belongs to the master machine)
         // We are going to assume that the next first two mac addresses of the configuration
         // will be free when we start the machines. if they aren't, hell breaks loose.
         
         Map<String, String> mapping = loadXenServerMappingProperties();
         
         String mac1 = machineProvisioningConfig.getMachineMacAddresses()[1].replaceAll(":", "");
         String mac2 = machineProvisioningConfig.getMachineMacAddresses()[2].replaceAll(":", "");
         
         assertTrue("Mapping for " + mac1 + "does not exist", 
                 mapping.containsKey(mac1));
         assertTrue("Mapping for " + mac2 + "does not exist", 
                 mapping.containsKey(mac2));
         
         String[] lookupLocators = new String[] {
             mapping.get(mac1),
             mapping.get(mac2)
         };
         
         machineProvisioningConfig.setXapLocators(lookupLocators);
     }
 	
 	@Override
 	@BeforeMethod
 	public void beforeTest() {
 	    super.setAcceptGSCsOnStartup(true);
 	    super.beforeTest();
 
 		GridServiceContainer restGsc = restGscWorkaround();
 		
 		startAdditionalManagement();
 
 		// Here we rely on the ESM to restart to rest service
 		restGsc.kill();
 		
         assertManagementStarted();
 		repetitiveAssertNumberOfGSAsAdded(2, OPERATION_TIMEOUT);
 	    repetitiveAssertNumberOfGSAsRemoved(0, OPERATION_TIMEOUT);
 	}
 
     // this is a hack. we need to have a handle to the GSC currently holding the rest instance so we could restart it after the second management machine starts
     // Problem: the rest depends on the management space. in this specific case the space is defined as 'highlyAvailable' meaning it would not be instantiated until 
     // after the second xen machine is started and a backup space is deployed on it. 
     // Workaround: We restart the rest GSC after the second management space is deployed on the second machine.
     // TODO: change start management implementation so that rest deployment depends on management space
 	// See also: CLOUDIFY-451
     private GridServiceContainer restGscWorkaround() {
 
 		GridServiceContainer restGsc = null;
 		admin.getGridServiceContainers().waitFor(2, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
 		for (GridServiceContainer container : admin.getGridServiceContainers()) {
 			if (container.getZones().containsKey("rest")) {
 				restGsc = container;
 			}
 		}
 		
 		if (restGsc == null) {
 			AssertFail("Failed finding rest GSC");
 		}
         return restGsc;
     }
     
 	protected void assertManagementStarted() {
 		// test management rest and webui services
 		for (GridServiceManager gsm : admin.getGridServiceManagers()) {
 	    	String host = gsm.getMachine().getHostAddress();
 	    
             try {
                 final URL restAdminURI = new URI("http", null, host, 8100, null, null, null).toURL();
                 final URL webUIURI = new URI("http", null, host, 8099, null, null, null).toURL();
 
                 repetitiveAssertTrue("Failed waiting for REST/WebIU services",
                         new RepetitiveConditionProvider() {
                             public boolean getCondition() {
                                 try {
                                     return WebUtils.isURLAvailable(restAdminURI) &&
                                            WebUtils.isURLAvailable(webUIURI);
                                 } catch (Exception e) {
                                     return false;
                                 }
                             }
                         }, OPERATION_TIMEOUT);
             } catch (MalformedURLException e) {
                 AssertFail("Setup failed", e);
             } catch (URISyntaxException e) {
                 AssertFail("Setup failed", e);
             }
 	    }
 	}
 	
     private void startAdditionalManagement() {
         final XenServerMachineProvisioningConfig agentConfig = getMachineProvisioningConfig();
         agentConfig.setFileAgentRemoteLocation("/opt/cloudify-start-additional-management.sh");     
         super.startNewVM(0, 0, agentConfig, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS); 
     }
 	
 	protected void startAgent(int extraMemoryCapacityInMB, String... zones) {
 		final XenServerMachineProvisioningConfig agentConfig = getMachineProvisioningConfig();
 	    agentConfig.setFileAgentRemoteLocation("/opt/cloudify-start-agent.sh");
	    agentConfig.setGridServiceAgentZones(new ExactZonesConfigurer().addZones(zones).create());
 	    int memoryCapacityInMB = agentConfig.getMemoryCapacityPerMachineInMB() + extraMemoryCapacityInMB;
 	    super.startNewVM(0, memoryCapacityInMB, agentConfig, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS); 
 	}
 	
 	@Override
 	public void teardownAllVMs() {
 		shutdownAgents();
 		shutdownManagements();
 	    super.teardownAllVMs();
 	}
 	
 	private void shutdownManagements() {
 	    final List<GridServiceAgent> agentsShutdown = new ArrayList<GridServiceAgent>(); 
 		if (admin != null) {
 		    for (GridServiceAgent agent : admin.getGridServiceAgents()) {
 		    	if (isManagementMachine(agent)) {
 		    		agentsShutdown.add(agent);
 		    		runCommand(agent, SHUTDOWN_MANAGEMENT_COMMAND);
 		    	}
 		    }
 		}
 	    waitForAgentsShutdown(agentsShutdown);
 	}
 
 	private void shutdownAgents() {
 		final List<GridServiceAgent> agentsShutdown = new ArrayList<GridServiceAgent>(); 
 		if (admin != null) {
 			for (GridServiceAgent agent : admin.getGridServiceAgents()) {
 		    	if (!isManagementMachine(agent)) {
 		    		agentsShutdown.add(agent);
 		    		runCommand(agent, SHUTDOWN_AGENT_COMMAND);
 		    	}
 		    }
 		}
 	    waitForAgentsShutdown(agentsShutdown);
 	}
 	
 	private void waitForAgentsShutdown(final List<GridServiceAgent> agentsShutdown) {
 		repetitiveAssertTrue("Failed waiting for all agents to be removed", new RepetitiveConditionProvider() {
             public boolean getCondition() {
                 int agentsAlive = 0;
                 for (final GridServiceAgent agent : agentsShutdown) {
                 	if (isManagementMachine(agent)) {
 	                	try {
 	    					((InternalGridServiceAgent)agent).getGSA().ping();
 	    					agentsAlive++;
 	    					LogUtils.log("Agent on " + agent.getMachine().getHostAddress() + " still alive");
 	    				} catch (final RemoteException e) {
 	    					//Probably NoSuchObjectException meaning the GSA is going down
 	    				}	
                 	}
                 }
             	return agentsAlive == 0;
             }
         }, OPERATION_TIMEOUT);
 	}
 
 	public boolean isManagementMachine(GridServiceAgent agent) {
 		boolean isManagementMachine = false;
 		for (LookupService lus : admin.getLookupServices()) {
 			if (agent.equals(lus.getGridServiceAgent())) {
 				isManagementMachine = true;
 				break;
 			}
 		}
 		return isManagementMachine;
 	}
 
 	private void runCommand(GridServiceAgent agent, String command) {
 	    String username = getMachineProvisioningConfig().getSshUsername();
 	    String password = getMachineProvisioningConfig().getSshPassword();
 	    String host = agent.getMachine().getHostAddress();		
 		SSHUtils.runCommand(host, 1000 * 60, command, username, password);
 	}
 
 }
 
 
