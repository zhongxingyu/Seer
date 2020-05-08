 package test.cli.cloudify.cloud.services.rackspace;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.cloudifysource.esc.driver.provisioning.openstack.Node;
 import org.cloudifysource.esc.driver.provisioning.openstack.OpenstackException;
 
 import test.cli.cloudify.cloud.services.AbstractCloudService;
 import test.cli.cloudify.cloud.services.tools.openstack.OpenstackClient;
 import test.cli.cloudify.cloud.services.tools.openstack.RackspaceClient;
 import framework.utils.IOUtils;
 import framework.utils.LogUtils;
 
 public class RackspaceCloudService extends AbstractCloudService {
 
 	private String user = "gsrackspace";
 	private String apiKey = "1ee2495897b53409f4643926f1968c0c";
 	private String tenant = "658142";
 	private RackspaceClient rackspaceClient;
 
 	public RackspaceCloudService() {
 		super("rsopenstack");
 
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
 	public void injectCloudAuthenticationDetails()
 			throws IOException {
 
 		getProperties().put("user", this.user);
 		getProperties().put("apiKey", this.apiKey);
 		getProperties().put("tenant", this.tenant);
 		
 		Map<String, String> propsToReplace = new HashMap<String, String>();
 		propsToReplace.put("machineNamePrefix " + "\"agent\"", "machineNamePrefix " + '"' + getMachinePrefix()
 				+ "cloudify-agent" + '"');
 		propsToReplace.put("managementGroup " + "\"management\"", "managementGroup " + '"' + getMachinePrefix()
 				+ "cloudify-manager" + '"');
 		propsToReplace.put("numberOfManagementMachines 1", "numberOfManagementMachines " + getNumberOfManagementMachines());
 		propsToReplace.put("\"openstack.wireLog\": \"false\"", "\"openstack.wireLog\": \"true\"");
 
 		IOUtils.replaceTextInFile(getPathToCloudGroovy(), propsToReplace);
 	}
 
 	private RackspaceClient createClient() {
 		RackspaceClient client = new RackspaceClient();
 		client.setConfig(getCloud());
 		return client;
 	}
 
 	@Override
 	public boolean scanLeakedAgentNodes() {
 		
 		if (rackspaceClient == null) {
 			this.rackspaceClient = createClient();
 		}
 		
 		String token = rackspaceClient.createAuthenticationToken();
 
 		final String agentPrefix = getCloud().getProvider().getMachineNamePrefix();
 
 		return checkForLeakedNode(token, agentPrefix);
 	
 
 	}
 	@Override
 	public boolean scanLeakedAgentAndManagementNodes() {
 		if(rackspaceClient == null) {
 			rackspaceClient = createClient();
 		}
 		String token = rackspaceClient.createAuthenticationToken();
 
 		final String agentPrefix = getCloud().getProvider().getMachineNamePrefix();
 		final String mgmtPrefix = getCloud().getProvider().getManagementGroup();
 		
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
