 package brooklyn.location.ibm.smartcloud;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static java.util.concurrent.TimeUnit.MILLISECONDS;
 import static java.util.concurrent.TimeUnit.SECONDS;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Random;
 import java.util.concurrent.Callable;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.io.Charsets;
 
 import brooklyn.location.NoMachinesAvailableException;
 import brooklyn.location.basic.SshMachineLocation;
 import brooklyn.location.cloud.AbstractCloudMachineProvisioningLocation;
 import brooklyn.util.collections.MutableMap;
 import brooklyn.util.config.ConfigBag;
 import brooklyn.util.internal.Repeater;
 import brooklyn.util.time.Time;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicate;
 import com.google.common.base.Throwables;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.io.Files;
 import com.ibm.cloud.api.rest.client.DeveloperCloud;
 import com.ibm.cloud.api.rest.client.DeveloperCloudClient;
 import com.ibm.cloud.api.rest.client.bean.Image;
 import com.ibm.cloud.api.rest.client.bean.Instance;
 import com.ibm.cloud.api.rest.client.bean.InstanceType;
 import com.ibm.cloud.api.rest.client.bean.Key;
 import com.ibm.cloud.api.rest.client.bean.Location;
 import com.ibm.cloud.api.rest.client.exception.InsufficientResourcesException;
 import com.ibm.cloud.api.rest.client.exception.InvalidConfigurationException;
 import com.ibm.cloud.api.rest.client.exception.KeyExistsException;
 import com.ibm.cloud.api.rest.client.exception.KeyGenerationFailedException;
 import com.ibm.cloud.api.rest.client.exception.PaymentRequiredException;
 import com.ibm.cloud.api.rest.client.exception.UnauthorizedUserException;
 import com.ibm.cloud.api.rest.client.exception.UnknownErrorException;
 import com.ibm.cloud.api.rest.client.exception.UnknownKeyException;
 
 public class IbmSmartCloudLocation extends AbstractCloudMachineProvisioningLocation implements IbmSmartLocationConfig {
 
     private static final long serialVersionUID = -828289137296787878L;
 
     private final Map<IbmSmartCloudSshMachineLocation, String> serverIds = Maps.newLinkedHashMap();
     private final List<String> keyPairNames = Lists.newArrayList();
     private final DeveloperCloudClient client;
     private final ConfigBag setup;
 
     public IbmSmartCloudLocation(Map<?, ?> conf) {
         super(conf);
         setup = ConfigBag.newInstanceExtending(getConfigBag(), conf);
         client = DeveloperCloud.getClient();
         client.setRemoteCredentials(getIdentity(), getCredential());
     }
 
     public String getIdentity() {
         return getConfig(ACCESS_IDENTITY);
     }
 
     public String getCredential() {
         return getConfig(ACCESS_CREDENTIAL);
     }
 
     public String getUser() {
         return getConfig(USER);
     }
 
     public String getLocation() {
         return getConfig(LOCATION);
     }
 
     public String getImage() {
         return getConfig(IMAGE);
     }
 
     public String getInstanceType() {
         return getConfig(INSTANCE_TYPE_LABEL);
     }
 
     public IbmSmartCloudSshMachineLocation obtain(Map<?, ?> flags) throws NoMachinesAvailableException {
         String postfix = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
         String serverName = name("brooklyn-instance", postfix);
         String keyName = name("brooklyn-keypair", postfix);
         String dataCenterID = findLocation(getLocation()).getId();
         String imageID = findImage(getImage(), dataCenterID).getID();
         String instanceTypeID = findInstanceType(imageID, getInstanceType()).getId();
         try {
             Key key = getKeyPairOrCreate(keyName);
             String privateKeyPath = storePrivateKeyOnTempFile(keyName, key.getMaterial());
             Instance instance = createInstanceWithRetryStrategy(
                     getConfig(IbmSmartLocationConfig.INSTANCE_CREATION_RETRIES), serverName, keyName, dataCenterID,
                     imageID, instanceTypeID);
             LOG.info("Using server-supplied private key for " + instance.getName() + " (" + instance.getIP() + "): "
                     + privateKeyPath);
             return registerIbmSmartCloudSshMachineLocation(instance.getIP(), instance.getID(), privateKeyPath);
         } catch (Exception e) {
             LOG.error(String.format("Cannot obtain a new machine with serverName(%s), keyName(%s), dataCenterID(%s), " +
             		"imageID(%s), instanceTypeID(%s)", serverName, keyName, dataCenterID, imageID, instanceTypeID), e);
             throw Throwables.propagate(e);
         }
     }
 
     public void release(SshMachineLocation machine) {
         try {
             String serverIdMsg = String.format("Server ID for machine(%s) must not be null", machine.getDisplayName());
             String serverId = checkNotNull(serverIds.get(machine), serverIdMsg);
             client.deleteInstance(serverId);
             waitForInstance(Instance.Status.REMOVED, serverId,
                     getConfig(IbmSmartLocationConfig.CLIENT_POLL_TIMEOUT_MILLIS),
                     getConfig(IbmSmartLocationConfig.CLIENT_POLL_PERIOD_MILLIS));
         } catch (Exception e) {
             throw Throwables.propagate(e);
         } finally {
             for (String keyName : keyPairNames) {
                 try {
                     client.removeKey(keyName);
                 } catch (Exception e) {
                     LOG.error("Cannot delete keypair({})", keyName);
                     throw Throwables.propagate(e);
                 }
             }
         }
     }
 
     private String storePrivateKeyOnTempFile(String keyName, String keyMaterial) throws IOException {
         File privateKey = File.createTempFile(keyName, "_rsa");
         Files.write(keyMaterial, privateKey, Charsets.UTF_8);
         Runtime.getRuntime().exec("chmod 400 " + privateKey.getAbsolutePath());
         privateKey.deleteOnExit();
         keyPairNames.add(keyName);
         return privateKey.getAbsolutePath();
     }
 
     private Key getKeyPairOrCreate(String keyName) throws IOException, UnauthorizedUserException,
             UnknownErrorException, KeyExistsException, KeyGenerationFailedException {
         try {
             return client.describeKey(keyName);
         } catch (UnknownKeyException e) {
             LOG.info("Created new keyPair({})", keyName);
             return client.generateKeyPair(keyName);
         }
     }
 
     private Instance createInstanceWithRetryStrategy(int retries, String serverName, String keyName,
             String dataCenterID, String imageID, String instanceTypeID) throws Exception {
         boolean foundInstance = false;
         int failures = 0;
         Instance instance;
         do {
             instance = createInstance(serverName + "_" + failures, dataCenterID, imageID,
                     instanceTypeID, keyName);
             LOG.info("Created new instance: name({}), keyname({}), location({}), id({})", 
                     new Object[] { instance.getName(), instance.getKeyName(), 
                     client.describeLocation(instance.getLocation()).getName(), instance.getID() });
             try {
                 foundInstance = waitForInstance(Instance.Status.ACTIVE, instance.getID(),
                         getConfig(IbmSmartLocationConfig.CLIENT_POLL_TIMEOUT_MILLIS),
                         getConfig(IbmSmartLocationConfig.CLIENT_POLL_PERIOD_MILLIS));
             } catch (IllegalStateException e) {
                 failures++;
                 client.deleteInstance(instance.getID());
                 // no need to delete keypair - reuse keyName already created before
             }
         } while (!foundInstance  && failures < retries);
         if(!foundInstance) {
             throw new RuntimeException("Instance with serverId(" + instance.getID() + ") is not running");
         }
         return client.describeInstance(instance.getID());
     }
     
     private Instance createInstance(String serverName, String dataCenterID, String imageID, String instanceTypeID,
             String keyName) throws InsufficientResourcesException, InvalidConfigurationException,
             PaymentRequiredException, UnauthorizedUserException, UnknownErrorException, IOException {
         List<Instance> instances = client.createInstance(serverName, dataCenterID, imageID, instanceTypeID, keyName,
                 null);
         return Iterables.getOnlyElement(instances);
     }
 
     /**
      * waits for the instance to be listed by the client; it is not necessarily
      * sshable however
      */
     private boolean waitForInstance(final Instance.Status desiredStatus, final String serverId, long timeoutMillis,
             long periodMillis) throws Exception {
         return Repeater.create("Wait until the instance is ready").until(new Callable<Boolean>() {
             public Boolean call() {
                 Instance instance;
                 try {
                     instance = client.describeInstance(serverId);
                 } catch (Exception e) {
                     LOG.error(String.format("Cannot find instance with serverId(%s)", serverId), e);
                     return false;
                 }
                 Instance.Status status = instance.getStatus();
                 if (Instance.Status.FAILED.equals(desiredStatus)) {
                     LOG.error(String.format("Instance with serverId(%s) has status=failed", serverId));
                     throw new IllegalStateException("Instance " + instance.getName() + " has status=failed");
                 }
                 LOG.debug("looking for IBM SCE server " + serverId + ", status: " + status.name());
                 return status.equals(desiredStatus);
             }
         }).every(periodMillis, TimeUnit.MILLISECONDS).limitTimeTo(timeoutMillis, TimeUnit.MILLISECONDS).run();
     }
 
     protected void waitForSshable(final SshMachineLocation machine, long delayMs) {
         LOG.info("Started VM in {}; waiting {} for it to be sshable on {}@{}", new Object[] { setup.getDescription(),
                 Time.makeTimeStringRounded(delayMs), machine.getUser(), machine.getAddress(), });
 
         boolean reachable = new Repeater().repeat().every(1, SECONDS).until(new Callable<Boolean>() {
             public Boolean call() {
                 return machine.isSshable();
             }
         }).limitTimeTo(delayMs, MILLISECONDS).run();
 
         if (!reachable) {
             throw new IllegalStateException("SSH failed for " + machine.getUser() + "@" + machine.getAddress() + " ("
                     + setup.getDescription() + ") after waiting " + Time.makeTimeStringRounded(delayMs));
         }
     }
 
     protected IbmSmartCloudSshMachineLocation registerIbmSmartCloudSshMachineLocation(String ipAddress,
             String serverId, String privateKeyPath) {
         IbmSmartCloudSshMachineLocation machine = createIbmSmartCloudSshMachineLocation(ipAddress, serverId,
                 privateKeyPath);
         machine.setParent(this);
 
         waitForSshable(machine, getConfig(IbmSmartLocationConfig.SSH_REACHABLE_TIMEOUT_MILLIS));
 
         if (getConfig(IbmSmartLocationConfig.SSHD_SUBSYSTEM_ENABLE)) {
             LOG.debug(this + ": machine " + ipAddress + " is sshable, enabling sshd subsystem section");
             machine.run("sudo sed -i \"s/#Subsystem/Subsystem/\" /etc/ssh/sshd_config");
             machine.run("sudo /etc/init.d/sshd restart");
 
             // TODO remove it and use `Apply same securityGroups rules to iptables, if iptables is running on the node`
             machine.run("sudo service iptables stop");
             machine.run("sudo chkconfig iptables off");
             // wait 30s for ssh to restart (overkill, but safety first; cloud is so slow it won't matter!)
             Time.sleep(30 * 1000L);
         } else {
             LOG.debug(this + ": machine " + ipAddress + " is not yet sshable");
         }

         serverIds.put(machine, serverId);
         return machine;
     }
 
     protected IbmSmartCloudSshMachineLocation createIbmSmartCloudSshMachineLocation(String ipAddress, String serverId,
             String privateKeyPath) {
         if (LOG.isDebugEnabled())
             LOG.debug("creating IbmSmartCloudSshMachineLocation representation for {}@{}", new Object[] { getUser(),
                     ipAddress, setup.getDescription() });
         return new IbmSmartCloudSshMachineLocation(MutableMap.builder().put("serverId", serverId)
                 .put("address", ipAddress).put("displayName", ipAddress).put(USER, getUser())
                 .put(PRIVATE_KEY_FILE, privateKeyPath).build());
     }
 
     private Location findLocation(final String location) {
         Preconditions.checkNotNull(location, "location must not be null");
         List<Location> locations;
         try {
             locations = client.describeLocations();
         } catch (Exception e) {
             throw Throwables.propagate(e);
         }
         Optional<Location> result = Iterables.tryFind(locations, new Predicate<Location>() {
             public boolean apply(Location input) {
                 return input.getName().contains(location);
             }
         });
         if (!result.isPresent()) {
             LOG.warn("IBM SmartCloud unknown location " + location);
             LOG.info("IBM SmartCloud locations (" + locations.size() + ") are:");
             for (Location l : locations)
                 LOG.info("  " + l.getName() + " " + l.getLocation() + " " + l.getId());
             throw new NoSuchElementException("Unknown IBM SmartCloud location " + location);
         }
         return result.get();
     }
 
     private Image findImage(final String imageName, final String dataCenterID) {
         Preconditions.checkNotNull(imageName, "image must not be null");
         List<Image> images;
         try {
             images = client.describeImages();
         } catch (Exception e) {
             throw Throwables.propagate(e);
         }
         Optional<Image> result = Iterables.tryFind(images, new Predicate<Image>() {
             public boolean apply(Image input) {
                 if (!input.getName().contains(imageName))
                     return false;
                 if (!input.getLocation().equals(dataCenterID))
                     return false;
                 return true;
             }
         });
 
         if (!result.isPresent()) {
             LOG.warn("IBM SmartCloud unknown image " + imageName + " (in location " + dataCenterID + ")");
             LOG.info("IBM SmartCloud images (" + images.size() + ") are:");
             for (Image img : images)
                 LOG.info("  " + img.getName() + " " + img.getLocation() + " " + img.getOwner() + " " + img.getID());
             throw new NoSuchElementException("Unknown IBM SmartCloud image " + imageName);
         }
         return result.get();
     }
 
     private InstanceType findInstanceType(String imageId, final String instanceType) {
         List<InstanceType> instanceTypes;
         try {
             instanceTypes = client.describeImage(imageId).getSupportedInstanceTypes();
         } catch (Exception e) {
             throw Throwables.propagate(e);
         }
         Optional<InstanceType> result = Iterables.tryFind(instanceTypes, new Predicate<InstanceType>() {
             public boolean apply(InstanceType input) {
                 return input.getLabel().contains(instanceType);
             }
         });
         if (!result.isPresent()) {
             LOG.warn("IBM SmartCloud unknown instanceType " + instanceType);
             LOG.info("IBM SmartCloud instanceTypes (" + instanceTypes.size() + ") are:");
             for (InstanceType i : instanceTypes)
                 LOG.info("  " + i.getLabel() + " " + i.getDetail() + " " + i.getId());
             throw new NoSuchElementException("Unknown IBM SmartCloud instanceType " + instanceType);
         }
         return result.get();
     }
 
     private String name(String prefix, String postfix) {
         return String.format("%s-%s", prefix, postfix);
     }
 
 }
