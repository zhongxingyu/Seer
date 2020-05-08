 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Module;
 import iTests.framework.utils.LogUtils;
 import org.cloudifysource.dsl.utils.IPUtils;
 import org.cloudifysource.esc.jclouds.WindowsServerEC2ReviseParsedImage;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CloudTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.security.SecurityConstants;
 import org.jclouds.ContextBuilder;
 import org.jclouds.aws.ec2.compute.strategy.AWSEC2ReviseParsedImage;
 import org.jclouds.compute.ComputeServiceContext;
 import org.jclouds.compute.domain.ComputeMetadata;
 import org.jclouds.compute.domain.NodeMetadata;
 import org.jclouds.compute.domain.NodeMetadata.Status;
 import org.jclouds.logging.jdk.config.JDKLoggingModule;
 
 import java.util.*;
 import java.util.Map.Entry;
 
 public abstract class JCloudsCloudService extends AbstractCloudService {
 
     protected final String LOCATION_ID_PROP = "locationId";
     protected final String HARDWARE_ID_PROP = "hardwareId";
     protected final String KEYPAIR_PROP = "keyPair";
     protected final String KEYFILE_PROP = "keyFile";
     protected final String LINUX_IMAGE_ID_PROP = "linuxImageId";
     protected final String UBUNTU_IMAGE_ID_PROP = "ubuntuImageId";
     protected final String USER_PROP = "user";
     protected final String API_KEY_PROP = "apiKey";
     protected final String SECURITY_GROUP_PROP = "securityGroup";
     private static final boolean enableLogstash = Boolean.parseBoolean(System.getProperty("iTests.enableLogstash", "false"));
 
     private static final String DEFAULT_REST_PORT = "8100";
 	private static final String MANAGER_ID = "-manager";
 	protected ComputeServiceContext context;
 	
 	public ComputeServiceContext getComputeServiceContext() {
 		if (context == null) {
 			context = createContext();
 		}
 		return context;
 	}
 	
 	public abstract void addOverrides(Properties overridesProps);
 
 	public JCloudsCloudService(String cloudName) {
 		super(cloudName);
 	}
 
 	protected ComputeServiceContext createContext() {
 		final Set<Module> wiring = new HashSet<Module>();
 		wiring.add(new AbstractModule() {
 	
 			@Override
 			protected void configure() {
 				bind(AWSEC2ReviseParsedImage.class).to(WindowsServerEC2ReviseParsedImage.class);
 //                bind(VirtualGuestToNodeMetadata.class).to(VirtualGuestToReducedNodeMetaData.class);
 			}
 	
 		});
 	
 		// Enable logging using gs_logging
 		wiring.add(new JDKLoggingModule());
 	
 		Properties overrides = new Properties();
 		
 		Set<Entry<String, Object>> entries = getCloud().getCloudCompute().getTemplates().get(getCloud().getConfiguration().getManagementMachineTemplate()).getOverrides().entrySet();
 		for (Entry<String, Object> entry : entries) {
 			overrides.setProperty(entry.getKey(), (String)entry.getValue());
 		}
 		
 		addOverrides(overrides);	
 		
 		final String provider = getCloud().getProvider().getProvider();
 		final String user = getCloud().getUser().getUser();
 		final String key = getCloud().getUser().getApiKey();
 		LogUtils.log("Creating jclouds context, this may take a few seconds");
 
 		return ContextBuilder.newBuilder(provider)
 				.credentials(user, key)
 				.modules(wiring)
 				.overrides(overrides)
 				.buildView(ComputeServiceContext.class);
 	}
 
 	@Override
 	public boolean scanLeakedAgentAndManagementNodes() {
 	
 		if (this.context == null) {
 			this.context = createContext();
 		}
 		
 		final String agentPrefix = getCloud().getProvider().getMachineNamePrefix();
 		final String managerPrefix = getCloud().getProvider().getManagementGroup();
 	
 		final List<ComputeMetadata> leakedNodes = checkForLeakedNodesWithPrefix(agentPrefix, managerPrefix);
 	
 		return leakedNodes.isEmpty();
 	
 	}
 
 	@Override
 	public void teardownCloud() throws Exception {
 		super.teardownCloud();
 		
 		if(!getBootstrapper().isTeardownExpectedToFail()){		
 			LogUtils.log("Closing jclouds ComputeServiceContext");
 			this.context.close();
 			this.context = null;
 		}
 	}
 
 	@Override
 	public boolean scanLeakedAgentNodes() {
 		
 		if (this.context == null) {
 			this.context = createContext();
 		}
 		
 		final String agentPrefix = getCloud().getProvider().getMachineNamePrefix();
 	
 		final List<ComputeMetadata> leakedNodes = checkForLeakedNodesWithPrefix(agentPrefix);
 	
 		return leakedNodes.isEmpty();
 	
 	}
 	
 	@Override
 	public boolean scanLeakedManagementNodes() {
 		
 		if (this.context == null) {
 			this.context = createContext();
 		}
 		
 		final String managerPrefix = getCloud().getProvider().getManagementGroup();
 	
 		final List<ComputeMetadata> leakedNodes = checkForLeakedNodesWithPrefix(managerPrefix);
 	
 		return leakedNodes.isEmpty();
 	
 	}
 
 	private List<ComputeMetadata> checkForLeakedNodesWithPrefix(final String... prefixes) {
 		List<ComputeMetadata> leakedNodes = null;
 		int iteration = 0;
 		while (iteration < 5) {
 			leakedNodes = new LinkedList<ComputeMetadata>();
 			LogUtils.log("Checking for leaked nodes with prefix: " + Arrays.toString(prefixes));
 			final Set<? extends ComputeMetadata> allNodes = this.context.getComputeService().listNodes();
 			
 			boolean foundPendingNodes = scanNodes(leakedNodes, allNodes,
 					prefixes);
 	
 			if (!foundPendingNodes && leakedNodes.size() == 0) {
 				break; // no need to re-run the scan
 			}
 			try {
 				LogUtils.log("Sleeping for several seconds to give pending nodes time to reach their new state");
 				Thread.sleep(10000);
 			} catch (InterruptedException e) {
 				// ignore.
 			}
 			++iteration;
 		}
 	
 		if (leakedNodes.size() > 0) {
 			LogUtils.log("Killing leaked nodes");
 			for (final ComputeMetadata leakedNode : leakedNodes) {
 				if (leakedNode.getName().contains(MANAGER_ID)) {
                     if(!enableLogstash){
                         LogUtils.log("found leaking management machine. attempting to dump logs.");
                         try {
                             String publicAddress = ((NodeMetadata) leakedNode).getPublicAddresses().iterator().next();
                             String managerIp = IPUtils.getSafeIpAddress(publicAddress) + ":" + DEFAULT_REST_PORT;
                             if (getBootstrapper().isSecured()) {
                                 CloudTestUtils.dumpMachines("https://" + managerIp, SecurityConstants.USER_PWD_ALL_ROLES, SecurityConstants.USER_PWD_ALL_ROLES);
                             } else {
                                 CloudTestUtils.dumpMachines("http://" + managerIp, null, null);
                             }
                             LogUtils.log("Leaked management machine logs dumped successfully");
                         } catch (Exception e) {
                             LogUtils.log("Failed getting leaked management machine logs. Reason: " + e.getMessage(), e);
                         }
                     }
 				}
 				LogUtils.log("Killing node: " + leakedNode.getName() + ": " + leakedNode);
 				try {
 					this.context.getComputeService().destroyNode(leakedNode.getId());
 				} catch (final Exception e) {
 					LogUtils.log("Failed to kill a leaked node. This machine may be leaking!", e);
 				}
 			}
 	
 		}
 		return leakedNodes;
 	}
 
     private boolean isNodeFitsTestPrefix(ComputeMetadata computeMetadata, String prefix){
         String name = computeMetadata.getName();
         // case1: name exists (not null and length>0) and the name starts with the prefix of the test
         boolean hasNameAndFitsPrefix = name != null && name.length() > 0 && name.startsWith(prefix);
         // case2: name does not exists because of a bug (name is null or length is 0), in this case
         // check if the group of the node starts with the prefix of the test
         boolean noNameAndGroupFitsPrefix =
                 ((name == null || name.length() == 0)
                 &&((NodeMetadata) computeMetadata).getGroup() != null
                 &&((NodeMetadata) computeMetadata).getGroup().startsWith(prefix));
 
         if (hasNameAndFitsPrefix || noNameAndGroupFitsPrefix){
             return true;
         }
         return false;
     }
 
 	private boolean scanNodes(final List<ComputeMetadata> leakedNodes, final Set<? extends ComputeMetadata> allNodes, final String... prefixes) {
 		boolean foundPendingNodes = false;
 		for (final ComputeMetadata computeMetadata : allNodes) {
 			final String name = computeMetadata.getName();
 			final Status status = ((NodeMetadata) computeMetadata).getStatus();
 			switch (status) {
 			case TERMINATED:
 				// ignore - node is shut down
 				break;
 			case PENDING:
 			case ERROR:
 			case RUNNING:
 			case UNRECOGNIZED:
 			case SUSPENDED:
 			default:
                 for (String prefix : prefixes) {
                     if (isNodeFitsTestPrefix(computeMetadata,prefix)){
                         if (status.equals(Status.PENDING)) {
                             // node is transitioning from one state to another
                             // 		- might be shutting down, might be starting up!
                             // Must try again later.
                             foundPendingNodes = true;
                             LogUtils.log("While scanning for leaked nodes, found a node in state PENDING. "
                                     + "Leak scan will be retried. Node in state pending was: "
                                     + computeMetadata);
                         } else {
                             LogUtils.log("ERROR: Found a leaking node: " + computeMetadata);
                             leakedNodes.add(computeMetadata);
                         }
                     }
                 }
 				break;
 			}
 		}
 		return foundPendingNodes;
 	}
 
 }
