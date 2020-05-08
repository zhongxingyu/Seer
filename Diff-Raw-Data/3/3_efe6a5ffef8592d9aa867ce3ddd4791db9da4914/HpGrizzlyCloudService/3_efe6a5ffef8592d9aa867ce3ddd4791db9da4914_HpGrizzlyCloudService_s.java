 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.hpgrizzly;
 
 import iTests.framework.utils.IOUtils;
 import iTests.framework.utils.LogUtils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.esc.driver.provisioning.CloudProvisioningException;
 import org.cloudifysource.esc.driver.provisioning.ComputeDriverConfiguration;
 import org.cloudifysource.esc.driver.provisioning.openstack.OpenStackCloudifyDriver;
 import org.cloudifysource.esc.driver.provisioning.openstack.OpenStackNetworkClient;
 import org.cloudifysource.esc.driver.provisioning.openstack.OpenstackException;
 import org.cloudifysource.esc.driver.provisioning.openstack.OpenstackJsonSerializationException;
 import org.cloudifysource.esc.driver.provisioning.openstack.rest.Network;
 import org.cloudifysource.esc.driver.provisioning.openstack.rest.Port;
 import org.cloudifysource.esc.driver.provisioning.openstack.rest.RouteFixedIp;
 import org.cloudifysource.esc.driver.provisioning.openstack.rest.Router;
 import org.cloudifysource.esc.driver.provisioning.openstack.rest.SecurityGroup;
 import org.cloudifysource.quality.iTests.framework.utils.compute.ComputeApiHelper;
 import org.cloudifysource.quality.iTests.framework.utils.compute.OpenstackComputeApiHelper;
 import org.cloudifysource.quality.iTests.framework.utils.network.NetworkApiHelper;
 import org.cloudifysource.quality.iTests.framework.utils.network.OpenstackNetworkApiHelper;
 import org.cloudifysource.quality.iTests.framework.utils.storage.OpenstackStorageApiHelper;
 import org.cloudifysource.quality.iTests.framework.utils.storage.StorageApiHelper;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.JCloudsCloudService;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.security.SecurityConstants;
 
 
 
 public class HpGrizzlyCloudService extends JCloudsCloudService {
 
 	private static final String CREDENTIALS_PROPERTIES = CREDENTIALS_FOLDER + "/cloud/hp-grizzly/hp-grizzly-cred.properties";
 
 	public static final String USER_PROP = "user";
 	public static final String TENANT_PROP = "tenant";
 	public static final String API_KEY_PROP = "apiKey";
 	public static final String KEYPAIR_PROP = "keyPair";
 	public static final String KEYFILE_PROP = "keyFile";
 	public static final String ENDPOINT_PROP = "openstackUrl";
 	public static final String HARDWARE_PROP = "hardwareId";
 	public static final String SMALL_HARDWARE_PROP = "smallHardwareId";
 	public static final String IMAGE_PROP = "imageId";
 	public static final String AVAILABILITY_ZONE_PROP = "availabilityZone";
 	public static final String ROUTER_NAME = "routerName";
 	public static final String MANAGMENT_MACHINE_PREFIX = "cloudify-managememnt-";
 
 	private final Properties properties = getCloudProperties(CREDENTIALS_PROPERTIES);
 
 	private String user = properties.getProperty("user");
 	private String tenant = properties.getProperty("tenant");
 	private String apiKey = properties.getProperty("apiKey");
 	private String keyPair = properties.getProperty("keyPair");
 	private String endpoint = properties.getProperty("openstackUrl");
 	private String hardwareId = properties.getProperty("hardwareId");
 	private String smallHardwareId = properties.getProperty("smallHardwareId");
 	private String imageId = properties.getProperty("imageId");
 	private String availabilityZone = properties.getProperty("availabilityZone");
 	private String routerName = properties.getProperty("routerName");
 
 
 	private boolean securityEnabled = false;
 
 	private OpenStackCloudifyDriver openstackCloudDriver;
 
 	private OpenStackNetworkClient networkClient;
 
 	public HpGrizzlyCloudService() {
 		super("hp-grizzly");
 		LogUtils.log("credentials file is at: " + CREDENTIALS_PROPERTIES);
 		this.getBootstrapper().skipValidation(false);
 	}
 
 	public HpGrizzlyCloudService(boolean securityEnabled) {
 		super("hp-grizzly");
 		LogUtils.log("credentials file is at: " + CREDENTIALS_PROPERTIES);
 		this.securityEnabled = securityEnabled;
 		this.getBootstrapper().skipValidation(false);
 	}
 
 	@Override
 	public String getRegion() {
 		return System.getProperty(availabilityZone);
 	}
 
 	@Override
 	public boolean scanLeakedAgentAndManagementNodes() {
 		boolean leakedNodesResult = super.scanLeakedAgentAndManagementNodes();
 		boolean leakedSecurityResult = true;
 		boolean leakedNetworksResult = true;
 		try {
 			initCloudDriver();
 			initNetworkClient();
 		} catch (final CloudProvisioningException e) {
 			LogUtils.log("Failed creating an instance of openstack cloud driver. Resource leak scan will be skipped.");
 			return true;
 		} catch (final OpenstackJsonSerializationException e) {
 			LogUtils.log("Failed creating an instance of openstack network driver. Resource leak scan will be skipped.");
 			return true;
 		}
 		
 		leakedSecurityResult = scanForLeakedSecurityGroups();
 		leakedNetworksResult = scanForLeakedNetworkComponents();
 
 		return leakedNodesResult && leakedNetworksResult && leakedSecurityResult;
 	}
 
 	private boolean scanForLeakedNetworkComponents() {
 		List<Router> routers = null;
 		List<Network> networksByPrefix = null;
 		boolean networksResult = true;
 		boolean routerResult = true;
 		try {
 			networksByPrefix = networkClient.getNetworkByPrefix(this.machinePrefix);
 		} catch (final OpenstackException e) {
 			LogUtils.log("Failed listing all aviliable networks. Network and router leak scan failed. Reason " + e.getMessage(), e);
 		}
 		try {
 			routers = networkClient.getRouters();
 		} catch (final OpenstackException e) {
 			LogUtils.log("Failed listing all aviliable routers. Network and router leak scan terminated. Reason " + e.getMessage(), e);
 		}
 		if (routers != null && networksByPrefix != null) {
 			for (Router router : routers) {
 				for (Network network : networksByPrefix) {
 					LogUtils.log("Found leaking network with name " + network.getName());
 					//compare common interfaces between network and router and remove leaked interfaces.
 					scanForLeakedInterfaces(router, network);
 					//remove the leaked network.
 					removeLeakedNetwork(network);
 				}
 				// after removing all interfaces, remove routers with name prefix.
 				if (router.getName().startsWith(this.machinePrefix)) {
 					routerResult = false;
 					LogUtils.log("Found leaking router: " + router.getName() + ". Attempting to delete resource.");
 					try {
 						networkClient.deleteRouter(router.getId());
 						LogUtils.log("Router " + router.getName() + " was removed successfully.");
 					} catch (final OpenstackException e) {
 						LogUtils.log("Failed to delete router with name " + router.getName() + ". Reason " + e.getMessage(), e);
 					}
 				}
 			}
 		}
 
 		if (networksByPrefix != null) {
 			networksResult = networksByPrefix.size() == 0;
 		}
 
 		return networksResult && routerResult;
 	}
 
 	void removeLeakedNetwork(Network network) {
 		LogUtils.log("Removing leaked network " + network.getName());
 		try {
 			networkClient.deleteNetwork(network.getId());
 			LogUtils.log("Network " + network.getName() + " was removed successfully.");
 		} catch (final OpenstackException e) {
 			LogUtils.log("Failed to delete network with name " + network.getName() + ". Reason " + e.getMessage(), e);
 		}
 	}
 
 	void scanForLeakedInterfaces(Router router, Network network) {
 		List<Port> ports = null;
 		try {
 			ports = networkClient.getPortsByDeviceId(router.getId());
 		} catch (final OpenstackException e) {
 			LogUtils.log("Failed listing router interfaces. Reason " + e.getMessage(), e);
 		}
 		if (ports != null) {
 			for (Port port : ports) {
 				for (RouteFixedIp routeFixedIp : port.getFixedIps()) {
 					if (Arrays.asList(network.getSubnets()).contains(routeFixedIp.getSubnetId())) {
 						LogUtils.log("Found leaking interface in router: " + router.getName() + " that is connected to network: " + network.getName()
 								+ ". Removing interface.");
 						try {
 							networkClient.deleteRouterInterface(router.getId(), routeFixedIp.getSubnetId());
 							LogUtils.log("interface with ID: " + port.getId() + " was successfully removed.");
 						} catch (final OpenstackException e) {
 							LogUtils.log("Failed deleting leaking interface with ID: " + port.getId() + ". Reason " + e.getMessage(), e);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private boolean scanForLeakedSecurityGroups() {
 		boolean result = true;
 		List<SecurityGroup> securityGroupsByName = null;
 		try {
 			securityGroupsByName = this.networkClient.getSecurityGroupsByPrefix(this.machinePrefix);
 		} catch (final OpenstackException e) {
 			LogUtils.log("Failed getting security group list. Reason " + e.getMessage(), e);
 		}
 		if (securityGroupsByName.size() > 0) {
 			result = false;
 			for (final SecurityGroup securityGroup : securityGroupsByName) {
 				try {
 					LogUtils.log("Found leaking security group '" + securityGroup.getName() + "'.");
 					this.networkClient.deleteSecurityGroup(securityGroup.getId());
 					LogUtils.log("Security group '" + securityGroup.getName() + "' deleted successfully.");
 				} catch (final OpenstackException e) {
 					LogUtils.log("Failed to delete security group. Reason: " + e.getMessage(), e);
 				}
 			}
 		}
 		return result;
 	}
 
 	private void initCloudDriver() throws CloudProvisioningException {
 		openstackCloudDriver = new OpenStackCloudifyDriver();
 		ComputeDriverConfiguration conf = new ComputeDriverConfiguration();
 		conf.setCloud(getCloud());
 		conf.setServiceName("default.simple");
 		conf.setCloudTemplate("SMALL_LINUX");
 		conf.setManagement(true);
 		openstackCloudDriver.setConfig(conf);
 	}
 
 	private void initNetworkClient() throws OpenstackJsonSerializationException {
 		networkClient = new OpenStackNetworkClient(endpoint, user, apiKey, tenant, "region-b.geo-1");
 	}
 
 	@Override
 	public void injectCloudAuthenticationDetails() throws IOException {
 		final Map<String, String> propsToReplace = new HashMap<String, String>();
 
 		// add a pem file
 		final String sshKeyPemName = this.keyPair + ".pem";
 
 		getProperties().put(USER_PROP, this.user);
 		getProperties().put(TENANT_PROP, this.tenant);
 		getProperties().put(API_KEY_PROP, this.apiKey);
 		getProperties().put(KEYPAIR_PROP, this.keyPair);
 		getProperties().put(KEYFILE_PROP, sshKeyPemName);
 		getProperties().put(ENDPOINT_PROP, this.endpoint);
 		getProperties().put(HARDWARE_PROP, this.hardwareId);
 		getProperties().put(SMALL_HARDWARE_PROP, this.smallHardwareId);
 		getProperties().put(IMAGE_PROP, this.imageId);
 		getProperties().put(AVAILABILITY_ZONE_PROP, this.availabilityZone);
 		getProperties().put(ROUTER_NAME, this.routerName);
 
 		propsToReplace.put("machineNamePrefix \"cloudify-agent-\"", "machineNamePrefix \"" + getMachinePrefix() + "-cloudify-agent-\"");
 		propsToReplace.put("managementGroup \"cloudify-manager\"", "managementGroup \"" + getMachinePrefix() + "-cloudify-manager-\"");
 		propsToReplace.put("numberOfManagementMachines 1", "numberOfManagementMachines " + getNumberOfManagementMachines());
 		propsToReplace.put("javaUrl", "// javaUrl");
 		propsToReplace.put("// \"externalRouterName\" : \"router-ext\",", "\"externalRouterName\" : routerName,");
 
 		String pathToCloudGroovy = getPathToCloudGroovy();
 		IOUtils.replaceTextInFile(pathToCloudGroovy, propsToReplace);
 
 		// Copy pem file
 		final File fileToCopy = new File(CREDENTIALS_FOLDER + "/cloud/" + getCloudName() + "/" + sshKeyPemName);
 		final File targetLocation = new File(getPathToCloudFolder() + "/upload/");
 		FileUtils.copyFileToDirectory(fileToCopy, targetLocation);
 
 		if (securityEnabled) {
 			File keystoreSrc = new File(SecurityConstants.DEFAULT_KEYSTORE_FILE_PATH);
 			File keystoreDest = new File(getPathToCloudFolder());
 			FileUtils.copyFileToDirectory(keystoreSrc, keystoreDest);
 		}
 	}
 
 	@Override
 	public String getUser() {
 		return user;
 	}
 
 	@Override
 	public String getApiKey() {
 		return apiKey;
 	}
 
 	public String getCloudProperty(String key) {
 		String pathToCloudProperty = getPathToCloudFolder() + "/" + getCloudName() + "-cloud.properties";
 		Properties props = new Properties();
 		try {
 			props.load(new FileInputStream(pathToCloudProperty));
 		} catch (Exception e) {
 			return null;
 		}
 		String property = (String) props.getProperty(key);
 		if (property != null) {
 			if (property.startsWith("\"")) {
 				property = property.substring(1);
 			}
 			if (property.endsWith("\"")) {
 				property = property.substring(0, property.length() - 1);
 			}
 		}
 		return property;
 	}
 
 	@Override
 	public void addOverrides(Properties overridesProps) {
 
 	}
 
 	@Override
 	public boolean isComputeApiHelperSupported() {
 		return true;
 	}
 
 	@Override
 	public boolean isStorageApiHelperSupported() {
 		return true;
 	}
 
 	@Override
 	public boolean isNetworkApiHelperSupported() {
 		return true;
 	}
 	
 	@Override
 	public ComputeApiHelper createComputeApiHelper() {
 		final String managementTemplateName = getCloud().getConfiguration().getManagementMachineTemplate();
     	return new OpenstackComputeApiHelper(getCloud(), managementTemplateName);
 	}
 	
 	@Override
 	public StorageApiHelper createStorageApiHelper() {
 		return new OpenstackStorageApiHelper(getCloud(), getCloud().getConfiguration().getManagementMachineTemplate(),
         		getComputeServiceContext());
 	}
 	
	public NetworkApiHelper createnetwApiHelper() {
 		return new OpenstackNetworkApiHelper(getCloud(), getCloud().getConfiguration().getManagementMachineTemplate());
 	}
 }
