 package io.cloudsoft.cloudera.brooklynnodes;
 
 import static io.cloudsoft.cloudera.brooklynnodes.ClouderaManagerNode.log;
 import io.cloudsoft.cloudera.rest.ClouderaRestCaller;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.whirr.Cluster.Instance;
 import org.apache.whirr.ClusterSpec;
 import org.jclouds.aws.util.AWSUtils;
 import org.jclouds.compute.ComputeServiceContext;
 import org.jclouds.compute.domain.OsFamily;
 import org.jclouds.compute.domain.TemplateBuilder;
 import org.jclouds.ec2.EC2ApiMetadata;
 import org.jclouds.ec2.EC2Client;
 import org.jclouds.ec2.domain.IpProtocol;
 import org.jclouds.ec2.domain.Reservation;
 import org.jclouds.ec2.domain.RunningInstance;
 import org.jclouds.googlecomputeengine.GoogleComputeEngineApiMetadata;
 
 import brooklyn.config.render.RendererHints;
 import brooklyn.entity.Entity;
 import brooklyn.entity.basic.SoftwareProcessImpl;
 import brooklyn.event.feed.function.FunctionFeed;
 import brooklyn.event.feed.function.FunctionPollConfig;
 import brooklyn.location.Location;
 import brooklyn.location.MachineProvisioningLocation;
 import brooklyn.location.jclouds.JcloudsLocation;
 import brooklyn.location.jclouds.JcloudsLocationConfig;
 import brooklyn.location.jclouds.JcloudsSshMachineLocation;
 import brooklyn.location.jclouds.templates.PortableTemplateBuilder;
 import brooklyn.util.MutableMap;
 import brooklyn.util.MutableSet;
 import brooklyn.util.exceptions.Exceptions;
 import brooklyn.util.internal.Repeater;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Functions;
 import com.google.common.base.Throwables;
 import com.google.common.collect.Iterables;
 import com.google.common.io.Files;
 
 public class DirectClouderaManagerImpl extends SoftwareProcessImpl implements DirectClouderaManager {
 
     static {
         RendererHints.register(CLOUDERA_MANAGER_URL, new RendererHints.NamedActionWithUrl("Open"));
     }
 
     private FunctionFeed sensorFeed;
 
     @Override
     public Class getDriverInterface() {
         return DirectClouderaManagerDriver.class;
     }
 
     protected Collection<Integer> getRequiredOpenPorts() {
         return MutableSet.<Integer>builder().addAll(super.getRequiredOpenPorts()).
                 addAll(Arrays.asList(22, 2181, 7180, 7182, 8088, 8888, 50030, 50060, 50070, 50090, 60010, 60020, 60030)).
                 build();
     }
     
     protected Map<String, Object> getProvisioningFlags(MachineProvisioningLocation location) {
         return obtainProvisioningFlags(location);
     }
     
     protected Map<String,Object> obtainProvisioningFlags(MachineProvisioningLocation location) {
         /*
         Map flags = super.obtainProvisioningFlags(location);
         flags.put(JcloudsLocationConfig.TEMPLATE_BUILDER.getName(), new PortableTemplateBuilder().
             osFamily(OsFamily.UBUNTU).osVersionMatches("12.04").
             os64Bit(true).
             minRam(2560));
         flags.put(JcloudsLocationConfig.SECURITY_GROUPS.getName(), "universal");
         
         if (location instanceof JcloudsLocation
                 && ((JcloudsLocation) location).getProvider().equals("google-compute-engine")) {
             flags.remove(JcloudsLocationConfig.SECURITY_GROUPS.getName());
             flags.remove(JcloudsLocationConfig.TEMPLATE_BUILDER.getName());
             flags.putAll(GoogleComputeEngineApiMetadata.defaultProperties());
             flags.put(JcloudsLocationConfig.GROUP_ID.getName(), "brooklyn-cdh");
         }
         return flags;
         */
         Map flags = super.obtainProvisioningFlags(location); 
         if (location instanceof JcloudsLocation && ((JcloudsLocation)location).getProvider().equals("google-compute-engine")) {
             flags.putAll(GoogleComputeEngineApiMetadata.defaultProperties());
             flags.put("groupId", "brooklyn-cdh");
            flags.put(JcloudsLocationConfig.TEMPLATE_BUILDER.getName(), new PortableTemplateBuilder().
                    osFamily(OsFamily.CENTOS).osVersionMatches("6").
                    os64Bit(true).
                    locationId("us-central1-a").
                    minRam(2560));
            } else if (location instanceof JcloudsLocation && ((JcloudsLocation)location).getProvider().equals("openstack-nova")) {
                 flags.put(JcloudsLocationConfig.KEY_PAIR.getName(), "andrea");  
                 try {
                     flags.put(JcloudsLocationConfig.LOGIN_USER_PRIVATE_KEY_DATA.getName(), 
                             Files.toString(new File("/home/brooklyn/andrea.pem"), Charsets.UTF_8));
                 } catch (IOException e) {
                     throw Throwables.propagate(e);
                 }  
         } else {
            flags.put(JcloudsLocationConfig.TEMPLATE_BUILDER.getName(), new PortableTemplateBuilder().
                    osFamily(OsFamily.UBUNTU).osVersionMatches("12.04").
                    os64Bit(true).
                    minRam(2560));
             flags.put("groupId", "brooklyn-cdh");
         }
         flags.put(JcloudsLocationConfig.SECURITY_GROUPS.getName(), 
                System.getProperty("jclouds.securityGroups", "universal"));
         return flags;
     }
     
     @Override
     protected void postDriverStart() {
         super.postDriverStart();
 
         String cmHost =
                 getAttribute(HOSTNAME);
                 //cmServer.publicHostName
         // the above can come back being the internal hostname, in some situations
 //        try {
 //            InetAddress addr = NetworkUtils.resolve(cmHost);
 //            if (addr==null) throw new NullPointerException("Cannot resolve "+cmHost);
 //            log.debug("Whirr-reported hostname "+cmHost+" for "+this+" resolved as "+addr);
 //        } catch (Exception e) {
 //            if (GroovyJavaMethods.truth(cmServer.publicIp)) {
 //                log.info("Whirr-reported hostname "+cmHost+" for "+this+" is not resolvable. Reverting to public IP "+cmServer.publicIp);
 //                cmHost = cmServer.publicIp;
 //            } else {
 //                log.warn("Whirr-reported hostname "+cmHost+" for "+this+" is not resolvable. No public IP available. Service may be unreachable.");
 //            }
 //        }
 
         try {
 //            authorizeIngress(controller.getCompute().apply(clusterSpec), [cmServer] as Set, clusterSpec,
 //                ["0.0.0.0/0"], 22, 2181, 7180, 7182, 8088, 8888, 50030, 50060, 50070, 50090, 60010, 60020, 60030);
             authorizePing("0.0.0.0/0", Iterables.getFirst(getLocations(), null));
         } catch (Throwable t) {
             log.warn("can't setup firewall/ping: "+t, t);
         }
 
         setAttribute(CLOUDERA_MANAGER_HOSTNAME, cmHost);
         setAttribute(CLOUDERA_MANAGER_URL, "http://" + cmHost + ":7180/");
         
         if (!Repeater.create(MutableMap.of("timeout", 5*60*1000, "description", "Waiting for successful REST call to "+this))
                     .rethrowException().repeat().every(1, TimeUnit.SECONDS)
                     .until(new Callable<Boolean>() {
                         public Boolean call() {
                             resetRestCaller();
                             return (getRestCaller().getClusters()!=null);
                     }}).run()) {
             throw new IllegalStateException("Timeout waiting for successful REST call to ${this}");
         }
         log.debug("Detected REST online for DirectCM "+this);
         //TODO accept default license / configurable license ? (is just a gui thing though...)
         
         //TODO change password on server, ensure RestCallers are reset to use right password
     }
 
     private ClouderaRestCaller _caller;
     public synchronized ClouderaRestCaller getRestCaller() {
         if (_caller != null) return _caller;
         // TODO use config
         return _caller = ClouderaRestCaller.newInstance(getAttribute(CLOUDERA_MANAGER_HOSTNAME),
             "admin", "admin");
     }
     private synchronized void resetRestCaller() {
         _caller = null;
     }
 
     public ClouderaCdhNode findEntityForHostId(String hostId) {
         return findEntityForHostIdIn(hostId, getParent());
     }
     public static ClouderaCdhNode findEntityForHostIdIn(String hostId, Entity root) {
         if ((root instanceof ClouderaCdhNode) && hostId.equals(root.getAttribute(ClouderaCdhNode.CDH_HOST_ID)))
             return (ClouderaCdhNode)root;
         for (Entity child: root.getChildren()) {
             ClouderaCdhNode result = findEntityForHostIdIn(hostId, child);
             if (result!=null) return result;
         }
         return null;
     }
 
     @Override
     public void connectSensors() {
 //        if (sensorRegistry==null) sensorRegistry = new SensorRegistry(this);
 //        ConfigSensorAdapter.apply(this);
 
         sensorFeed = FunctionFeed.builder()
                 .entity(this)
                 .poll(new FunctionPollConfig<Boolean,Boolean>(SERVICE_UP)
                         .period(15, TimeUnit.SECONDS)
                         .callable(new Callable<Boolean>() {
                             @Override
                             public Boolean call() throws Exception {
                                 try { return (getRestCaller().getHosts()!=null); } 
                                 catch (Exception e) { return false; }
                             }
                           })
                           .onSuccess(Functions.<Boolean>identity())
                         )
                 .poll(new FunctionPollConfig<List,List>(MANAGED_HOSTS)
                         .period(5, TimeUnit.SECONDS)
                         .callable(new Callable<List>() {
                             @Override
                             public List call() throws Exception {
                                 return getRestCaller().getHosts();
                             }
                           })
                           .onSuccess(Functions.<List>identity())
                         )
                 .poll(new FunctionPollConfig<List,List>(MANAGED_CLUSTERS)
                         .period(5, TimeUnit.SECONDS)
                         .callable(new Callable<List>() {
                             @Override
                             public List call() throws Exception {
                                 return getRestCaller().getClusters();
                             }
                           })
                           .onSuccess(Functions.<List>identity())
                         )
                 .build();
 
 //        FunctionSensorAdapter fnSensorAdaptor = sensorRegistry.register(new FunctionSensorAdapter({}, period: 30*TimeUnit.SECONDS));
 //        fnSensorAdaptor.poll(SERVICE_UP, { try { return (getRestCaller().getHosts()!=null) } catch (Exception e) { return false; } });
 //        fnSensorAdaptor.poll(MANAGED_HOSTS, { getRestCaller().getHosts() });
 //        fnSensorAdaptor.poll(MANAGED_CLUSTERS, { getRestCaller().getClusters() });
         
 //        sensorRegistry.activateAdapters();
     }
 
     @Override
     public void disconnectSensors() {
         disconnectServiceUpIsRunning();
         if (sensorFeed != null) {
             sensorFeed.stop();
         }
     }
 
     public static void authorizeIngress(ComputeServiceContext computeServiceContext,
         Set<Instance> instances, ClusterSpec clusterSpec, List<String> cidrs, int... ports) {
         
         if (EC2ApiMetadata.CONTEXT_TOKEN.isAssignableFrom(computeServiceContext.getBackendType())) {
 //        from:
 //            FirewallManager.authorizeIngress(computeServiceContext, instances, clusterSpec, cidrs, ports);
             
             // This code (or something like it) may be added to jclouds (see
             // http://code.google.com/p/jclouds/issues/detail?id=336).
             // Until then we need this temporary workaround.
             String region = AWSUtils.parseHandle(Iterables.get(instances, 0).getId())[0];
             EC2Client ec2Client = computeServiceContext.unwrap(EC2ApiMetadata.CONTEXT_TOKEN).getApi();
             String groupName = "jclouds#" + clusterSpec.getClusterName();
             for (String cidr : cidrs) {
                 for (int port : ports) {
                     try {
                         ec2Client.getSecurityGroupServices()
                                 .authorizeSecurityGroupIngressInRegion(region, groupName,
                                 IpProtocol.TCP, port, port, cidr);
                     } catch(IllegalStateException e) {
                         LOG.warn(e.getMessage());
                         /* ignore, it means that this permission was already granted */
                     }
                 }
             }
         } else {
             // TODO generalise the above, or support more clouds, or bypass whirr
             LOG.debug("Skipping port ingress modifications for "+instances+" in cloud "+computeServiceContext.getBackendType());
         }
     }
         
     private void authorizePing(String cidr, Location ssh) {
         JcloudsLocation jcl = null;
         JcloudsSshMachineLocation jclssh = null;
         if (ssh instanceof JcloudsSshMachineLocation) {
             jclssh = ((JcloudsSshMachineLocation)ssh);
             jcl = jclssh.getParent();
         }
         if (jcl!=null) {
             ComputeServiceContext ctx = jcl.getComputeService().getContext();
             if (ctx.unwrap() instanceof EC2Client) {
                 // This code (or something like it) may be added to jclouds (see
                 // http://code.google.com/p/jclouds/issues/detail?id=336).
                 // Until then we need this temporary workaround.
 //                String region = AWSUtils.parseHandle(Iterables.get(instances, 0).getId())[0];
                 try {
                     String region = jcl.getRegion();
                     EC2Client ec2Client = EC2Client.class.cast(ctx.unwrap());
                     String id = jclssh.getNode().getId();
                     // string region prefix from id
                     if (id.indexOf('/')>=0) id = id.substring(id.indexOf('/')+1);
                     Set<? extends Reservation<? extends RunningInstance>> instances = ec2Client.getInstanceServices().describeInstancesInRegion(region, id);
                     Set<String> groupNames = Iterables.getOnlyElement(instances).getGroupNames(); 
                     String groupName = Iterables.getFirst(groupNames, null);
                     //                  "jclouds#" + clusterSpec.getClusterName(); // + "#" + region;
                     log.info("Authorizing ping for "+groupName+": "+cidr);
                     ec2Client.getSecurityGroupServices()
                         .authorizeSecurityGroupIngressInRegion(region, groupName,
                             IpProtocol.ICMP, -1, -1, cidr);
                     return;
                 } catch(Exception e) {
                     log.warn("Problem authorizing ping (possibly already authorized) for "+this+": "+e.getMessage());
                     log.debug("Details for roblem authorizing ping (possibly already authorized) for "+this+": "+e.getMessage(), e);
                     /* ignore, usually means that this permission was already granted */
                     Exceptions.propagateIfFatal(e);
                 }
             }
         }
         LOG.debug("Skipping ping authorization for "+this+"; not required or not supported");
         // TODO for JcloudsSsh and Jclouds -- in AWS
         // see WhirrClusterManager for example
     }
 
 
 }
