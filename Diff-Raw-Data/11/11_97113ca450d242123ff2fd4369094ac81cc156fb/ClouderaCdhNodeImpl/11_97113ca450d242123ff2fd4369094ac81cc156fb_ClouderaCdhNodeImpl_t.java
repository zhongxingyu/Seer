 package io.cloudsoft.cloudera.brooklynnodes;
 
 import static io.cloudsoft.cloudera.brooklynnodes.ClouderaManagerNode.log;
 
 import java.io.File;
import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.TimeUnit;
 
 import org.jclouds.compute.domain.OsFamily;
 import org.jclouds.googlecomputeengine.GoogleComputeEngineApiMetadata;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import brooklyn.entity.annotation.Effector;
 import brooklyn.entity.annotation.EffectorParam;
 import brooklyn.entity.basic.Lifecycle;
 import brooklyn.entity.basic.SoftwareProcessImpl;
 import brooklyn.entity.basic.lifecycle.ScriptHelper;
 import brooklyn.event.feed.function.FunctionFeed;
 import brooklyn.event.feed.function.FunctionPollConfig;
 import brooklyn.location.MachineProvisioningLocation;
 import brooklyn.location.jclouds.JcloudsLocation;
 import brooklyn.location.jclouds.JcloudsLocationConfig;
 import brooklyn.location.jclouds.templates.PortableTemplateBuilder;
 import brooklyn.util.collections.MutableMap;
 import brooklyn.util.collections.MutableSet;
 import brooklyn.util.net.Cidr;
 import brooklyn.util.text.Strings;
 import brooklyn.util.time.Time;
 
 import com.google.common.base.Functions;
 import com.google.common.collect.Iterables;
 
 public class ClouderaCdhNodeImpl extends SoftwareProcessImpl implements ClouderaCdhNode {
 
     FunctionFeed feed;
 
     @Override
     public void waitForEntityStart() {
         if (log.isDebugEnabled()) log.debug("waiting to ensure {} doesn't abort prematurely", this);
         long startTime = System.currentTimeMillis();
         long waitTime = startTime + 200000; // FIXME magic number; should be config key with default value?
         boolean isRunningResult = false;
         while (!isRunningResult && System.currentTimeMillis() < waitTime) {
             Time.sleep(1000); // FIXME magic number; should be config key with default value?
             try {
                 isRunningResult = getDriver().isRunning();
             } catch (Exception  e) {
                 setAttribute(SERVICE_STATE, Lifecycle.ON_FIRE);
                 // provide extra context info, as we're seeing this happen in strange circumstances
                 if (getDriver()==null) throw new IllegalStateException(this+" concurrent start and shutdown detected");
                 throw new IllegalStateException("Error detecting whether "+this+" is running: "+e, e);
             }
             if (log.isDebugEnabled()) log.debug("checked {}, is running returned: {}", this, isRunningResult);
         }
         if (!isRunningResult) {
             String msg = "Software process entity "+this+" did not appear to start within "+
                     Time.makeTimeStringRounded(System.currentTimeMillis()-startTime)+
                     "; setting state to indicate problem and throwing; consult logs for more details";
             log.warn(msg);
             setAttribute(SERVICE_STATE, Lifecycle.ON_FIRE);
             throw new IllegalStateException(msg);
         }
     }
 
     private static final Logger log = LoggerFactory.getLogger(ClouderaCdhNodeImpl.class);
     
     public Class getDriverInterface() { return ClouderaCdhNodeSshDriver.class; }
     
     protected Map<String, Object> obtainProvisioningFlags(MachineProvisioningLocation location) {
         Map flags = super.obtainProvisioningFlags(location);
         PortableTemplateBuilder portableTemplateBuilder = new PortableTemplateBuilder();
         if (isJcloudsLocation(location, "aws-ec2")) {
             portableTemplateBuilder.osFamily(OsFamily.UBUNTU).osVersionMatches("12.04").os64Bit(true).minRam(2500);
             flags.put(JcloudsLocationConfig.TEMPLATE_BUILDER.getName(), portableTemplateBuilder);
         } else if (isJcloudsLocation(location, "google-compute-engine")) {
             flags.putAll(GoogleComputeEngineApiMetadata.defaultProperties());
             flags.put("groupId", "brooklyn-cdh");
             flags.put(JcloudsLocationConfig.TEMPLATE_BUILDER.getName(),
             portableTemplateBuilder.osFamily(OsFamily.CENTOS).osVersionMatches("6").os64Bit(true)
             .locationId("us-central1-a").minRam(2560));
         } else if (isJcloudsLocation(location, "openstack-nova")) {
             String imageId = location.getConfig(JcloudsLocationConfig.IMAGE_ID);
             if(imageId != null) {
                 portableTemplateBuilder.imageId(imageId);
             }
             String hardwareId = location.getConfig(JcloudsLocationConfig.HARDWARE_ID);
             if(hardwareId != null) {
                 portableTemplateBuilder.hardwareId(hardwareId);
             }
             flags.put(JcloudsLocationConfig.TEMPLATE_BUILDER.getName(), portableTemplateBuilder);
         } else if (isJcloudsLocation(location, "rackspace-cloudservers-uk") || isJcloudsLocation(location, "cloudservers-uk")) {
             // securityGroups are not supported
             flags.put(JcloudsLocationConfig.TEMPLATE_BUILDER.getName(),
             portableTemplateBuilder.osFamily(OsFamily.CENTOS).osVersionMatches("6").os64Bit(true)
             .minRam(2560));
         } else if (isJcloudsLocation(location, "bluelock-vcloud-zone01")) {
             System.setProperty("jclouds.vcloud.timeout.task-complete", 600 * 1000 + "");
             // this is a constraint for dns name on vcloud (3-15 characters)
             flags.put("groupId", "brooklyn");
             flags.put(JcloudsLocationConfig.TEMPLATE_BUILDER.getName(),
         //    portableTemplateBuilder.osFamily(OsFamily.CENTOS).osVersionMatches("6").os64Bit(true));
             portableTemplateBuilder.imageId("https://zone01.bluelock.com/api/v1.0/vAppTemplate/vappTemplate-e0717fc0-0b7f-41f7-a275-3e03881d99db"));
         }
         return flags;
         }
     
     private boolean isJcloudsLocation(MachineProvisioningLocation location, String providerName) {
         return location instanceof JcloudsLocation && ((JcloudsLocation) location).getProvider().equals(providerName);
     }
 
    @Override
     protected Collection<Integer> getRequiredOpenPorts() {
        return MutableSet.<Integer>builder().addAll(super.getRequiredOpenPorts()).
                addAll(Arrays.asList(22, 2181, 7180, 7182, 8088, 8888, 50030, 50060, 50070, 50090, 60010, 60020, 60030)).
                build();
     }
     
     @Override
     protected void preStart() {
         super.preStart();
         
         try {
             TempCloudUtils.authorizePing(new Cidr(), Iterables.getFirst(getLocations(), null));
         } catch (Throwable t) {
             log.warn("can't setup firewall/ping: "+t, t);
         }
     }
     
     public void connectSensors() {
         super.connectSensors();
         feed = FunctionFeed.builder()
                 .entity(this)
                 .poll(new FunctionPollConfig<Boolean,Boolean>(SERVICE_UP)
                     .period(30, TimeUnit.SECONDS)
                     .callable(new Callable<Boolean>() {
                         @Override
                         public Boolean call() throws Exception {
                             try {
                                 return getManagedHostId() != null;
                             } catch (Exception e) {
                                 log.error("ID is not available for "+ClouderaCdhNodeImpl.this+": "+e, e);
                                 return false;
                             }
                         }
                     })
                     .onFailureOrException(Functions.constant(false)))
                 .poll(new FunctionPollConfig<String, String>(CDH_HOST_ID)
                     .period(30, TimeUnit.SECONDS)
                     .callable(new Callable<String>() {
                         @Override
                         public String call() throws Exception {
                             return getManagedHostId();
                         }
                     })
                     .onFailureOrException(Functions.constant((String)null)))
                 .build();
     }
     
     @Override
     protected void disconnectSensors() {
        super.disconnectSensors();
        if (feed != null) feed.stop();
     }
     
     public String getManagedHostId() {
         ClouderaManagerNode mgr = getConfig(MANAGER);
         List managedHosts = mgr == null ? null : mgr.getAttribute(ClouderaManagerNode.MANAGED_HOSTS);
         if (managedHosts==null || managedHosts.isEmpty()) return null;
         String hostname = getAttribute(HOSTNAME);
         if (Strings.isNonEmpty(hostname) && managedHosts.contains(hostname)) {
             log.debug("ManagedHostId is hostname: "+ hostname);
             return hostname;
         }
         String privateHostname = getAttribute(PRIVATE_HOSTNAME);
         if (Strings.isNonEmpty(privateHostname)) {
             // manager might view it as ip-10-1-1-1.ec2.internal whereas node knows itself as just ip-10-1-1-1
             // TODO better might be to compare private IP addresses of this node with IP of managed nodes at CM
             String pm = null;
             for (Object it: managedHosts) {
                 String h = String.valueOf(it);
                 int localdomainIndex = h.indexOf(".localdomain");
                 if (h.startsWith(privateHostname) ||
                         (localdomainIndex != -1 && privateHostname.startsWith(h.substring(0, localdomainIndex)))) {
                     log.debug("ManagedHostId: "+ h);
                     return h;
                 }
             }
             log.debug("ManagedHostId: "+ privateHostname);
             return privateHostname;
         }
         return null;
     }
 
     public ScriptHelper newScript(String summary) {
         return new ScriptHelper((ClouderaCdhNodeDriver)getDriver(), summary);
     }
 
     /**
      * Start the entity in the given collection of locations.
      */
     @Effector(description="Collect metrics files from this host and save to a file on this machine, as a subdir of the given dir, returning the name of that subdir")
     public String collectMetrics(@EffectorParam(name="targetDir") String targetDir) {
         targetDir = targetDir + "/" + getAttribute(CDH_HOST_ID);
         new File(targetDir).mkdir();
         // TODO allow wildcards, or batch on server then copy down?
         int i=0;
         for (String role : new String[] { "datanode","namenode","master","regionserver" }) {
             try {
                 ((ClouderaCdhNodeDriver)getDriver()).getMachine().copyFrom(
                         MutableMap.of("sshTries", 1),
                         "/tmp/${role}-metrics.out", targetDir+"/${role}-metrics.out");
             } catch (Exception e) {
                 //not serious, file probably doesn't exist
                 log.debug("Unable to copy /tmp/${role}-metrics.out from ${this} (file may not exist): "+e);
             }
         }
         for (String role : new String[] { "mr","jvm" }) {
             try {
                 ((ClouderaCdhNodeDriver)getDriver()).getMachine().copyFrom(
                         MutableMap.of("sshTries", 1),
                         "/tmp/${role}metrics.log", targetDir+"/${role}metrics.log");
             } catch (Exception e) {
                 //not serious, file probably doesn't exist
                 log.debug("Unable to copy /tmp/${role}metrics.log from ${this} (file may not exist): "+e);
             }
         }
         log.debug("Copied ${i} metrics files from ${this}");
         return targetDir;
     }
 
 }
