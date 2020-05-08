 package test.cli.cloudify.cloud.services.rackspace;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import test.cli.cloudify.cloud.services.AbstractCloudService;
 import test.cli.cloudify.cloud.services.tools.openstack.Node;
 import test.cli.cloudify.cloud.services.tools.openstack.OpenstackClient;
 import test.cli.cloudify.cloud.services.tools.openstack.OpenstackException;
 import test.cli.cloudify.cloud.services.tools.openstack.RackspaceClient;
 import framework.utils.IOUtils;
 import framework.utils.LogUtils;
 
 public class RackspaceCloudService extends AbstractCloudService {
 
 	private static final String CLOUD_NAME = "rsopenstack";
 	private String user = "gsrackspace";
 	private String apiKey = "1ee2495897b53409f4643926f1968c0c";
 	private String tenant = "658142";
 	private RackspaceClient rackspaceClient;
 
 	public RackspaceCloudService(String uniqueName) {
 		super(uniqueName, CLOUD_NAME);
 
 	}
 
 	public String getUser() {
 		return user;
 	}
 
 	public void setUser(String user) {
 		this.user = user;
 	}
 
 	public String getApiKey() {
 		return apiKey;
 	}
 
 	public void setApiKey(String apiKey) {
 		this.apiKey = apiKey;
 	}
 
 	public String getTenant() {
 		return tenant;
 	}
 
 	public void setTenant(String tenant) {
 		this.tenant = tenant;
 	}
 
 	@Override
 	public void injectServiceAuthenticationDetails()
 			throws IOException {
 
 		// cloud plugin should include recipe that includes secret key
 		/*
 		 * File cloudPluginDir = new File(ScriptUtils.getBuildPath() , "tools/cli/plugins/esc/" + getCloudName() + "/");
 		 * File originalCloudDslFile = new File(cloudPluginDir, getCloudName() + "-cloud.groovy"); File
 		 * backupCloudDslFile = new File(cloudPluginDir, getCloudName() + "-cloud.backup"); // first make a backup of
 		 * the original file FileUtils.copyFile(originalCloudDslFile, backupCloudDslFile);
 		 */
 
 		Map<String, String> propsToReplace = new HashMap<String, String>();
 		propsToReplace.put("USER_NAME", user);
 		propsToReplace.put("API_KEY", apiKey);
 		propsToReplace.put("machineNamePrefix " + "\"agent\"", "machineNamePrefix " + '"' + this.machinePrefix
 				+ "cloudify-agent" + '"');
 		propsToReplace.put("managementGroup " + "\"management\"", "managementGroup " + '"' + this.machinePrefix
 				+ "cloudify-manager" + '"');
 		propsToReplace.put("ENTER_TENANT", tenant);
 		propsToReplace.put("numberOfManagementMachines 1", "numberOfManagementMachines " + numberOfManagementMachines);
 		propsToReplace.put("\"openstack.wireLog\": \"false\"", "\"openstack.wireLog\": \"true\"");
 
 		IOUtils.replaceTextInFile(getPathToCloudGroovy(), propsToReplace);
 	}
 
 
 	@Override
 	public void beforeBootstrap() {
 		this.rackspaceClient = new RackspaceClient();
 		rackspaceClient.setConfig(this.cloudConfiguration);
 
 		LogUtils.log("Getting list of current machines from Rackspace cloud");
 		String token = rackspaceClient.createAuthenticationToken();
 
 		final String agentPrefix = this.cloudConfiguration.getProvider().getMachineNamePrefix();
 		final String mgmtPrefix = this.cloudConfiguration.getProvider().getManagementGroup();
 
 		List<Node> nodes;
 		try {
 			nodes = rackspaceClient.listServers(token);
 		} catch (OpenstackException e) {
 			throw new IllegalStateException("Failed to query openstack cloud for current servers", e);
 		}
 
 		for (Node node : nodes) {
 			if (node.getStatus().equals(OpenstackClient.MACHINE_STATUS_ACTIVE)) {
 				if (node.getName().startsWith(mgmtPrefix) || node.getName().startsWith(agentPrefix)) {
 					throw new IllegalStateException("Before bootstrap, found an active node with name: "
 							+ node.getName() + ". Details: " + node);
 				}
 			}
 		}
 		LogUtils.log("No leaked machine found");
 
 	}
 
 	@Override
 	public boolean afterTest() {
 		String token = rackspaceClient.createAuthenticationToken();
 
 		final String agentPrefix = this.cloudConfiguration.getProvider().getMachineNamePrefix();
 
 		return checkForLeakedNode(token, agentPrefix);
 
 	}
 	@Override
 	public boolean afterTeardown() {
 		if(rackspaceClient == null) {
 			LogUtils.log("Openstack client was not initialized, so no test was performed after teardown");
 			return true;
 		}
 		String token = rackspaceClient.createAuthenticationToken();
 
 		final String agentPrefix = this.cloudConfiguration.getProvider().getMachineNamePrefix();
 		final String mgmtPrefix = this.cloudConfiguration.getProvider().getManagementGroup();
 		
 		final boolean result = checkForLeakedNode(token, agentPrefix, mgmtPrefix);
 		this.rackspaceClient.close();
 		return result;
 
 	}
 
 	private boolean checkForLeakedNode(String token, final String... prefixes) {
 		List<Node> nodes;
 		try {
 			nodes = rackspaceClient.listServers(token);
 		} catch (OpenstackException e) {
 			throw new IllegalStateException("Failed to query openstack cloud for current servers", e);
 		}
 
 		List<Node> leakedNodes = new LinkedList<Node>();
 		for (Node node : nodes) {
 			if (node.getStatus().equals(OpenstackClient.MACHINE_STATUS_ACTIVE)) {
 				for (String prefix : prefixes) {
 					if (node.getName().startsWith(prefix)) {
 						leakedNodes.add(node);
 					}
 				}
 
 			}
 		}
 
 		if (leakedNodes.size() > 0) {
			LogUtils.log("Found leaking nodes in Rackspace cloud after teardown");
 			for (Node node : leakedNodes) {
 				LogUtils.log("Shutting down: " + node);
 				try {
 					rackspaceClient.terminateServer(node.getId(), token, System.currentTimeMillis() + 60000);
 				} catch (Exception e) {
 					LogUtils.log("Failed to terminate HP openstack node: " + node.getId()
 							+ ". This node may be leaking. Node details: " + node + ", Error was: " + e.getMessage(), e);
 				}
 			}
 			return false;
 		}
 		
 		return true;
 	}
 }
