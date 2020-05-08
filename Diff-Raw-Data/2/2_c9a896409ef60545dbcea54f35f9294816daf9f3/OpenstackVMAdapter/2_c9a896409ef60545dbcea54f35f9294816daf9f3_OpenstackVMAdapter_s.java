 package org.fiteagle.adapter.nodeadapter;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.security.PublicKey;
 import java.security.cert.X509Certificate;
 import java.security.interfaces.DSAParams;
 import java.security.interfaces.DSAPublicKey;
 import java.security.interfaces.RSAPublicKey;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.UUID;
 import java.util.prefs.Preferences;
 
 import net.iharder.Base64;
 
 import org.fiteagle.adapter.common.AdapterConfiguration;
 import org.fiteagle.adapter.common.OpenstackResourceAdapter;
 import org.fiteagle.adapter.common.ResourceAdapter;
 import org.fiteagle.adapter.common.ResourceAdapterStatus;
 import org.fiteagle.adapter.nodeadapter.client.OfflineTestClient;
 import org.fiteagle.adapter.nodeadapter.client.OpenstackClient;
 import org.fiteagle.adapter.nodeadapter.client.Utils;
 import org.fiteagle.adapter.nodeadapter.client.model.Image;
 import org.fiteagle.adapter.nodeadapter.client.model.Images;
 import org.fiteagle.adapter.nodeadapter.client.model.Server;
 
 import com.woorea.openstack.nova.model.Flavor;
 import com.woorea.openstack.nova.model.Flavors;
 import com.woorea.openstack.nova.model.FloatingIp;
 
 public class OpenstackVMAdapter implements
 		OpenstackResourceAdapter {
 
 	private static boolean loaded = false;
 
 	public static boolean utilsConfigured = false;
 
 	private OpenstackClient client;
 	private Image image;
 	private List<Flavor> flavorsList;
 	private Server server = new Server();
 	private String keyPairName;
 	private String vmName;
 	private String imageId;
 	private String flavorId;
 
 	private String floatingIp = null;
 
	private static boolean offlineTestMode = true;
 
 	public OpenstackVMAdapter() {
 		super();
 		if (!utilsConfigured) {
 			this.configureUtils();
 		}
 		this.setType("org.fiteagle.adapter.openstackvmadapter.OpenstackVMAdapter");
 
 	}
 
 	private void configureUtils() {
 		Preferences preferences = Preferences.userNodeForPackage(getClass());
 
 		if (preferences.get("floating_ip_pool_name", null) != null)
 			Utils.FLOATINGIP_POOL_NAME = preferences.get(
 					"floating_ip_pool_name", null);
 		if (preferences.get("keystone_auth_URL", null) != null)
 			Utils.KEYSTONE_AUTH_URL = preferences
 					.get("keystone_auth_URL", null);
 		if (preferences.get("keystone_endpoint", null) != null)
 			Utils.KEYSTONE_ENDPOINT = preferences
 					.get("keystone_endpoint", null);
 		if (preferences.get("keystone_password", null) != null)
 			Utils.KEYSTONE_PASSWORD = preferences
 					.get("keystone_password", null);
 		if (preferences.get("keystone_username", null) != null)
 			Utils.KEYSTONE_USERNAME = preferences
 					.get("keystone_username", null);
 		if (preferences.get("net_endpoint", null) != null)
 			Utils.NET_ENDPOINT = preferences.get("net_endpoint", null);
 		if (preferences.get("net_name", null) != null)
 			Utils.NET_NAME = preferences.get("net_name", null);
 		if (preferences.get("nova_endpoint", null) != null)
 			Utils.NOVA_ENDPOINT = preferences.get("nova_endpoint", null);
 		if (preferences.get("tenant_name", null) != null)
 			Utils.TENANT_NAME = preferences.get("tenant_name", null);
 
 	}
 
 	public void start() {
 	}
 
 	public void stop() {
 		this.getClient().deleteKeyPair(this.getKeyPairName());
 		this.getClient().deleteServer(this.getServer().getId());
 	}
 
 	public void create() {
 	}
 	
 	public void configure(AdapterConfiguration configuration) {
 
 		String sshPubKey = configuration.getUsers().get(0).getSshPublicKeys()
 				.get(0);
 		this.getClient().addKeyPair(keyPairName, sshPubKey);
 
 		System.out.println("creating key pair: " + this.getKeyPairName());
 
 		Server createdServer = this.getClient().createServer(this.imageId,
 				this.flavorId, this.vmName, this.keyPairName);
 
 		System.out
 				.println("creating server(vm) with image id: " + this.imageId);
 
 		this.setServer(createdServer);
 
 		System.out
 				.println("configure on openstack adapter is called configuring the ip ");
 		FloatingIp floatingIp = this.getClient().addFloatingIp();
 
 		System.out.println("adding a floating ip..");
 
 		this.setFloatingIp(floatingIp.getIp());
 		this.server = this.getClient().getServerDetails(server.getId());
 		this.getClient().allocateFloatingIpForServer(server.getId(),
 				floatingIp.getIp());
 
 		System.out.println("allocating floating ip for server "
 				+ server.getId());
 	}
 
 	public void release() {
 		this.getClient().deleteKeyPair(this.getKeyPairName());
 		this.getClient().deleteServer(this.getServer().getId());
 	}
 
 	public static List<OpenstackResourceAdapter> getOpenstackVMAdapters() {
 		List<OpenstackResourceAdapter> resultList = new ArrayList<OpenstackResourceAdapter>();
 
 		if (!utilsConfigured) {
 			new OpenstackVMAdapter();
 		}
 
 		Flavors flavors = createClient().listFlavors();
 		List<Flavor> flavorsList = flavors.getList();
 		Images images = createClient().listImages();
 		List<Image> imagesList = images.getList();
 
 		for (Image image : imagesList) {
 			OpenstackVMAdapter openstackVMAdapter = new OpenstackVMAdapter();
 			openstackVMAdapter.setExclusive(false);
 			openstackVMAdapter.setAvailable(true);
 			openstackVMAdapter.setImage(image);
 			openstackVMAdapter.setFlavorsList(flavorsList);
 			resultList.add(openstackVMAdapter);
 		}
 
 		return resultList;
 	}
 
 	public boolean isLoaded() {
 		return this.loaded;
 	}
 
 	public void setLoaded(boolean loaded) {
 		this.loaded = loaded;
 	}
 
 	public Image getImage() {
 		return image;
 	}
 
 	public void setImage(Image image) {
 		this.image = image;
 	}
 
 	public List<Flavor> getFlavorsList() {
 		return flavorsList;
 	}
 
 	public void setFlavorsList(List<Flavor> flavorsList) {
 		this.flavorsList = flavorsList;
 	}
 
 	private static OpenstackClient createClient() {
 		OpenstackClient newClient = null;
 		if (offlineTestMode) {
 			newClient = new OfflineTestClient();
 		} else {
 			newClient = new OpenstackClient();
 		}
 
 		return newClient;
 	}
 
 	public OpenstackClient getClient() {
 
 		if (this.client == null) {
 			if (offlineTestMode) {
 				this.client = new OfflineTestClient();
 			} else {
 				this.client = new OpenstackClient();
 			}
 		}
 		return client;
 	}
 
 	public void setClient(OpenstackClient client) {
 		this.client = client;
 	}
 
 	public static boolean isOfflineTestMode() {
 		return offlineTestMode;
 	}
 
 	public static void setOfflineTestMode(boolean offlineTestMode) {
 		OpenstackVMAdapter.offlineTestMode = offlineTestMode;
 	}
 
 	public HashMap<String, String> getImageProperties() {
 
 		HashMap<String, String> imageProperties = new HashMap<String, String>();
 
 		imageProperties.put(OpenstackResourceAdapter.IMAGE_ID, image.getId());
 		imageProperties.put(OpenstackResourceAdapter.IMAGE_NAME,
 				image.getName());
 		imageProperties.put(OpenstackResourceAdapter.IMAGE_MINDISK, image
 				.getMinDisk().toString());
 
 		if (image.getCreated() != null)
 			imageProperties.put(OpenstackResourceAdapter.IMAGE_CREATED,
 					getLongValueAsStringOfCalendar((image.getCreated())));
 		imageProperties.put(OpenstackResourceAdapter.IMAGE_MINRAM, image
 				.getMinRam().toString());
 		imageProperties.put(OpenstackResourceAdapter.IMAGE_OSEXTIMG_SIZE, image
 				.getSize().toString());
 		imageProperties.put(OpenstackResourceAdapter.IMAGE_PROGRESS, image
 				.getProgress().toString());
 		imageProperties.put(OpenstackResourceAdapter.IMAGE_STATUS,
 				image.getStatus());
 		if (image.getUpdated() != null)
 			imageProperties.put(OpenstackResourceAdapter.IMAGE_UPDATED,
 					getLongValueAsStringOfCalendar(image.getUpdated()));
 
 		return imageProperties;
 	}
 
 	public List<HashMap<String, String>> getFlavorsProperties() {
 
 		List<HashMap<String, String>> resultList = new ArrayList<HashMap<String, String>>();
 
 		if (this.flavorsList == null || this.flavorsList.isEmpty())
 			return null;
 
 		for (Iterator iterator = flavorsList.iterator(); iterator.hasNext();) {
 
 			Flavor flavor = (Flavor) iterator.next();
 			HashMap<String, String> tmpProperties = new HashMap<String, String>();
 			tmpProperties.put(OpenstackResourceAdapter.FLAVOR_OSFLVDISABLED,
 					flavor.getDisabled().toString());
 			tmpProperties.put(OpenstackResourceAdapter.FLAVOR_DISK,
 					flavor.getDisk());
 			tmpProperties.put(OpenstackResourceAdapter.FLAVOR_ID,
 					flavor.getId());
 			tmpProperties.put(OpenstackResourceAdapter.FLAVOR_NAME,
 					flavor.getName());
 			tmpProperties.put(
 					OpenstackResourceAdapter.FLAVOR_OSFLAVORACCESSISPUBLIC,
 					flavor.isPublic().toString());
 			tmpProperties.put(OpenstackResourceAdapter.FLAVOR_VCPUS,
 					flavor.getVcpus());
 			tmpProperties.put(
 					OpenstackResourceAdapter.FLAVOR_OSFLVEXTDATAEPHEMERAL,
 					flavor.getEphemeral().toString());
 			tmpProperties.put(OpenstackResourceAdapter.FLAVOR_RAM, flavor
 					.getRam().toString());
 			tmpProperties.put(OpenstackResourceAdapter.FLAVOR_RXTXFACTOR,
 					flavor.getRxtxFactor().toString());
 			tmpProperties.put(OpenstackResourceAdapter.FLAVOR_SWAP,
 					flavor.getSwap());
 
 			resultList.add(tmpProperties);
 		}
 
 		return resultList;
 
 	}
 
 	@Override
 	public OpenstackResourceAdapter create(String imageId, String flavorId,
 			String vmName, String keyPairName) {
 
 		if (vmName == null || vmName.compareTo("") == 0) {
 			vmName = generateRandomString();
 		}
 
 		if (keyPairName == null || keyPairName.compareTo("") == 0) {
 			keyPairName = generateRandomString();
 		}
 
 		OpenstackVMAdapter openstackVM = new OpenstackVMAdapter();
 
 		openstackVM.setVmName(vmName);
 		openstackVM.setImageId(imageId);
 		openstackVM.setFlavorId(flavorId);
 		openstackVM.setKeyPairName(keyPairName);
 		openstackVM.setImage(this.image);
 		openstackVM.setFlavorsList(this.flavorsList);
 		return openstackVM;
 	}
 
 	private String generateRandomString() {
 		return UUID.randomUUID().toString();
 	}
 
 	private String getPubKeyString(PublicKey pubKey) throws IOException {
 		String publicKeyEncoded;
 		if (pubKey.getAlgorithm().equals("RSA")) {
 			RSAPublicKey rsaPublicKey = (RSAPublicKey) pubKey;
 			ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
 			DataOutputStream dos = new DataOutputStream(byteOs);
 			dos.writeInt("ssh-rsa".getBytes().length);
 			dos.write("ssh-rsa".getBytes());
 			dos.writeInt(rsaPublicKey.getPublicExponent().toByteArray().length);
 			dos.write(rsaPublicKey.getPublicExponent().toByteArray());
 			dos.writeInt(rsaPublicKey.getModulus().toByteArray().length);
 			dos.write(rsaPublicKey.getModulus().toByteArray());
 			publicKeyEncoded = new String(Base64.encodeBytes(byteOs
 					.toByteArray()));
 			return "ssh-rsa " + publicKeyEncoded;
 		} else if (pubKey.getAlgorithm().equals("DSA")) {
 			DSAPublicKey dsaPublicKey = (DSAPublicKey) pubKey;
 			DSAParams dsaParams = dsaPublicKey.getParams();
 
 			ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
 			DataOutputStream dos = new DataOutputStream(byteOs);
 			dos.writeInt("ssh-dss".getBytes().length);
 			dos.write("ssh-dss".getBytes());
 			dos.writeInt(dsaParams.getP().toByteArray().length);
 			dos.write(dsaParams.getP().toByteArray());
 			dos.writeInt(dsaParams.getQ().toByteArray().length);
 			dos.write(dsaParams.getQ().toByteArray());
 			dos.writeInt(dsaParams.getG().toByteArray().length);
 			dos.write(dsaParams.getG().toByteArray());
 			dos.writeInt(dsaPublicKey.getY().toByteArray().length);
 			dos.write(dsaPublicKey.getY().toByteArray());
 			publicKeyEncoded = new String(Base64.encodeBytes(byteOs
 					.toByteArray()));
 			return "ssh-dss " + publicKeyEncoded;
 		} else {
 			throw new RuntimeException("Unknown public key encoding: "
 					+ pubKey.getAlgorithm());
 		}
 	}
 
 	public Server getServer() {
 		return server;
 	}
 
 	public void setServer(Server server) {
 		this.server = server;
 	}
 
 	@Override
 	public HashMap<String, String> getVMProperties() {
 		HashMap<String, String> vmProperties = new HashMap<String, String>();
 
 		if (server.getAccessIPv4() != null)
 			vmProperties.put(OpenstackResourceAdapter.VM_AccessIPv4,
 					server.getAccessIPv4());
 		if (server.getAccessIPv6() != null)
 			vmProperties.put(OpenstackResourceAdapter.VM_AccessIPv6,
 					server.getAccessIPv6());
 
 		if (server.getConfigDrive() != null)
 			vmProperties.put(OpenstackResourceAdapter.VM_ConfigDrive,
 					server.getConfigDrive());
 
 		if (server.getCreated() != null)
 			vmProperties.put(OpenstackResourceAdapter.VM_Created,
 					server.getCreated());
 
 		if (server.getFlavor() != null && server.getFlavor().getId() != null)
 			vmProperties.put(OpenstackResourceAdapter.VM_FlavorId, server
 					.getFlavor().getId());
 
 		if (server.getHostId() != null)
 			vmProperties.put(OpenstackResourceAdapter.VM_HostId,
 					server.getHostId());
 
 		if (server.getId() != null)
 			vmProperties.put(OpenstackResourceAdapter.VM_Id, server.getId());
 
 		if (server.getImage() != null && server.getImage().getId() != null)
 			vmProperties.put(OpenstackResourceAdapter.VM_ImageId, server
 					.getImage().getId());
 
 		if (server.getKeyName() != null)
 			vmProperties.put(OpenstackResourceAdapter.VM_KeyName,
 					server.getKeyName());
 
 		if (server.getName() != null)
 			vmProperties
 					.put(OpenstackResourceAdapter.VM_Name, server.getName());
 
 		if (server.getDiskConfig() != null)
 			vmProperties.put(OpenstackResourceAdapter.VM_OSDCFDiskConfig,
 					server.getDiskConfig());
 
 		if (server.getAvailabilityZone() != null)
 			vmProperties.put(
 					OpenstackResourceAdapter.VM_OSEXTAZAvailabilityZone,
 					server.getAvailabilityZone());
 
 		if (server.getPowerState() != null)
 			vmProperties.put(OpenstackResourceAdapter.VM_OSEXTSTSPowerState,
 					server.getPowerState());
 
 		if (server.getTaskState() != null)
 			vmProperties.put(OpenstackResourceAdapter.VM_OSEXTSTSTaskState,
 					server.getTaskState());
 
 		if (server.getVmState() != null)
 			vmProperties.put(OpenstackResourceAdapter.VM_OSEXTSTSVmState,
 					server.getVmState());
 
 		if (server.getProgress() != null)
 			vmProperties.put(OpenstackResourceAdapter.VM_Progress, server
 					.getProgress().toString());
 
 		if (server.getStatus() != null)
 			vmProperties.put(OpenstackResourceAdapter.VM_Status,
 					server.getStatus());
 
 		if (server.getTenantId() != null)
 			vmProperties.put(OpenstackResourceAdapter.VM_TenantId,
 					server.getTenantId());
 
 		if (server.getUpdated() != null)
 			vmProperties.put(OpenstackResourceAdapter.VM_Updated,
 					server.getUpdated());
 
 		if (server.getUserId() != null)
 			vmProperties.put(OpenstackResourceAdapter.VM_UserId,
 					server.getUserId());
 
 		if (this.getFloatingIp() != null)
 			vmProperties.put(OpenstackResourceAdapter.VM_FloatingIP,
 					this.getFloatingIp());
 
 		return vmProperties;
 
 	}
 
 	public String getFloatingIp() {
 		return floatingIp;
 	}
 
 	public void setFloatingIp(String floatingIp) {
 		this.floatingIp = floatingIp;
 	}
 
 	public String getKeyPairName() {
 		return keyPairName;
 	}
 
 	public void setKeyPairName(String keyPairName) {
 		this.keyPairName = keyPairName;
 	}
 
 	private String getLongValueAsStringOfCalendar(Calendar calendar) {
 		return String.valueOf(calendar.getTimeInMillis());
 	}
 
 	public String getVmName() {
 		return vmName;
 	}
 
 	public void setVmName(String vmName) {
 		this.vmName = vmName;
 	}
 
 	public String getImageId() {
 		return imageId;
 	}
 
 	public void setImageId(String imageId) {
 		this.imageId = imageId;
 	}
 
 	public String getFlavorId() {
 		return flavorId;
 	}
 
 	public void setFlavorId(String flavorId) {
 		this.flavorId = flavorId;
 	}
 
 	// needed staff for the resource adapter capabilities
 	private HashMap<String, Object> properties = new HashMap<String, Object>();
 	private String type;// class of the implementing adapter
 	private String id;
 	private String groupId;
 	private ResourceAdapterStatus status;
 	private boolean exclusive = false;
 	private boolean available = true;
 	private Date expirationTime;
 
 	public HashMap<String, Object> getProperties() {
 		if (properties != null) {
 			return properties;
 		} else {
 			properties = new HashMap<String, Object>();
 			return properties;
 		}
 	}
 
 	public void setProperties(HashMap<String, Object> properties) {
 		this.properties = properties;
 	}
 
 	public void addProperty(String key, Object value) {
 		this.properties.put(key, value);
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public String getId() {
 		return this.id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getGroupId() {
 		return groupId;
 	}
 
 	public void setGroupId(String groupId) {
 		this.groupId = groupId;
 	}
 
 	public ResourceAdapterStatus getStatus() {
 		return status;
 	}
 
 	public void setStatus(ResourceAdapterStatus status) {
 		this.status = status;
 	}
 
 	public boolean isExclusive() {
 		return exclusive;
 	}
 
 	public void setExclusive(boolean exclusive) {
 		this.exclusive = exclusive;
 	}
 
 	public boolean isAvailable() {
 		return available;
 	}
 
 	public void setAvailable(boolean available) {
 		this.available = available;
 	}
 
 	public Date getExpirationTime() {
 		return expirationTime;
 	}
 
 	public void setExpirationTime(Date expirationTime) {
 		this.expirationTime = expirationTime;
 	}
 
 }
