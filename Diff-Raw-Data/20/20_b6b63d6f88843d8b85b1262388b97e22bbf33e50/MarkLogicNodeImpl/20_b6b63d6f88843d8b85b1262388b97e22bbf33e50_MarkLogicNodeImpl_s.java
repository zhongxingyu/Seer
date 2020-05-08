 package io.cloudsoft.marklogic.nodes;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import brooklyn.config.render.RendererHints;
 import brooklyn.entity.basic.BrooklynConfigKeys;
 import brooklyn.entity.basic.Lifecycle;
 import io.cloudsoft.marklogic.api.MarkLogicApi;
 import io.cloudsoft.marklogic.appservers.AppServer;
 import io.cloudsoft.marklogic.clusters.MarkLogicCluster;
 import io.cloudsoft.marklogic.databases.Database;
 import io.cloudsoft.marklogic.forests.Forest;
 import io.cloudsoft.marklogic.forests.Forests;
 import brooklyn.entity.basic.SoftwareProcess;
 import brooklyn.entity.basic.SoftwareProcessImpl;
 import brooklyn.event.AttributeSensor;
 import brooklyn.event.SensorEvent;
 import brooklyn.event.SensorEventListener;
 import brooklyn.event.feed.function.FunctionFeed;
 import brooklyn.event.feed.function.FunctionPollConfig;
 
 import java.util.Collection;
 import java.util.Set;
 import java.util.concurrent.Callable;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Functions;
 import com.google.common.base.Objects;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 
 public class MarkLogicNodeImpl extends SoftwareProcessImpl implements MarkLogicNode {
 
     private static final Logger LOG = LoggerFactory.getLogger(MarkLogicNodeImpl.class);
 
     private final Object attributeSetMutex = new Object();
     private FunctionFeed serviceUp;
 
     static {
         RendererHints.register(URL, new RendererHints.NamedActionWithUrl("Open"));
     }
 
     @Override
     public void init() {
         //we give it a bit longer timeout for starting up
         setConfig(BrooklynConfigKeys.START_TIMEOUT, 240);
 
         //todo: ugly.. we don't want to get the properties  this way, but for the time being it works.
         setConfig(WEBSITE_USERNAME, getManagementContext().getConfig().getFirst("brooklyn.marklogic.website-username"));
         setConfig(WEBSITE_PASSWORD, getManagementContext().getConfig().getFirst("brooklyn.marklogic.website-password"));
         setConfig(LICENSE_KEY, getManagementContext().getConfig().getFirst("brooklyn.marklogic.license-key"));
         setConfig(LICENSEE, getManagementContext().getConfig().getFirst("brooklyn.marklogic.licensee"));
        setConfig(LICENSE_TYPE, getManagementContext().getConfig().getFirst("brooklyn.marklogic.license-type"));
         setConfig(CLUSTER_NAME, getManagementContext().getConfig().getFirst("brooklyn.marklogic.cluster"));
 
         String configuredVersion = getManagementContext().getConfig().getFirst("brooklyn.marklogic.version");
         if (configuredVersion != null && !configuredVersion.isEmpty()) {
             setConfig(SoftwareProcess.SUGGESTED_VERSION, configuredVersion);
         }
 
         subscribe(this, MarkLogicNode.SERVICE_UP, new SensorEventListener<Boolean>() {
             Boolean previous;
 
             @Override
             public void onEvent(SensorEvent<Boolean> event) {
                 Boolean newValue = event.getValue();
                 if (newValue != null && !newValue.equals(previous)) {
                     onServiceUp(newValue);
                 }
                 previous = newValue;
             }
         });
     }
 
     private void onServiceUp(boolean up) {
         if (up && Lifecycle.STOPPING.equals(getAttribute(SERVICE_STATE))) {
             LOG.warn("{} got erroneous notification of SERVICE_UP when it is in Lifecycle.STOPPING. Ignoring and not attempting to move forests to the node.", this);
             return;
         }
         if (up) {
             LOG.info("MarkLogic node is up: {}", this);
             // Finds a forest to move to the new node
             if (getCluster() != null && !getNodeType().equals(NodeType.E_NODE)) {
                 Forests forests = getCluster().getForests();
                 Forest targetForest = null;
                 for (Forest forest : forests) {
                     // Choose a non-MarkLogic forest that isn't a replica.
                     if (forest.createdByBrooklyn() && forest.getMaster() == null) {
                         targetForest = forest;
                         break;
                     }
                 }
 
                 if (targetForest != null)
                     forests.moveForest(targetForest.getName(), getHostname());
             } else {
                 String reason = (getCluster() == null)
                         ? "Cluster is null"
                         : "Node type is: " + getNodeType().name();
                 LOG.info("Skipped move of forests onto new node {}: {}", this, reason);
             }
         } else {
             LOG.info("MarkLogic node is down: {}", this);
         }
     }
 
    @Override
    public void doStop() {
         Lifecycle clusterState = getCluster().getAttribute(MarkLogicCluster.SERVICE_STATE);
         boolean moveForests = (clusterState != Lifecycle.STOPPING && clusterState != Lifecycle.STOPPED);
 
         if (moveForests) {
             LOG.info("Stopping {}: Moving all forests out", this);
             getCluster().getForests().moveAllForestsFromNode(this);
             LOG.info("Stopping {}: Moved all forests from node, continue to stop", this);
             getDriver().removeNodeFromCluster();
         } else {
             LOG.info("Stopping MarkLogicNode (and cluster) {} Not moving forests out", this);
         }
         super.doStop();
         LOG.info("MarkLogicNode terminated: {}", this);
    }
 
     public Class getDriverInterface() {
         return MarkLogicNodeDriver.class;
     }
 
     /**
      * Sets up the polling of sensors.
      */
     @Override
     protected void connectSensors() {
         super.connectSensors();
         serviceUp = FunctionFeed.builder()
                 .entity(this)
                 .period(5000)
                 .poll(new FunctionPollConfig<Boolean, Boolean>(SERVICE_UP)
                         .onException(Functions.constant(Boolean.FALSE))
                         .callable(new Callable<Boolean>() {
                             public Boolean call() {
                                 return getDriver().isRunning();
                             }
                         }))
                 .build();
     }
 
 
     @Override
     protected void disconnectSensors() {
         super.disconnectSensors();
         if (serviceUp != null) serviceUp.stop();
     }
 
     /**
      * The ports to be opened in the VM (e.g. in the aws-ec2 security group created by jclouds).
      */
     @Override
     protected Collection<Integer> getRequiredOpenPorts() {
         // TODO What ports need to be open?
         // I got these from `sudo netstat -antp` for the MarkLogic daemon
         // TODO If want to use a pre-existing security group instead, can add to
         //      obtainProvisioningFlags() something like:
         //      .put("securityGroups", groupName)
         //TODO: Ports 8006 and 8011 have been added so we can register app services on them.
         // In the future this needs to come from the application, but for the time being it is hard coded.
         int bindPort = getConfig(BIND_PORT);
         int foreignBindPort = getConfig(FOREIGN_BIND_PORT);
         // FIXME hack to open 80,443 (because on GCE shared by network for all nodes)
         return ImmutableSet.copyOf(Iterables.concat(super.getRequiredOpenPorts(),
                 ImmutableList.of(22, bindPort, foreignBindPort,
                         8000, 8001, 8002, 8006, 8011, 80, 443)));
     }
 
     private NodeType getNodeType() {
         return getConfig(NODE_TYPE);
     }
 
     @Override
     public MarkLogicNodeDriver getDriver() {
         return (MarkLogicNodeDriver) super.getDriver();
     }
 
     public String getHostname() {
         return getAttribute(HOSTNAME);
     }
 
     public String getGroupName() {
         return getConfig(GROUP);
     }
 
     @Override
     public void createForest(Forest forest) {
         checkNotNull(forest.getName(), "Forest requires a name");
         getDriver().createForest(forest);
         addToAttributeSet(FOREST_NAMES, forest.getName());
     }
 
     @Override
     public void createDatabase(Database database) {
         getDriver().createDatabase(database);
     }
 
     @Override
     public void createAppServer(AppServer appServer) {
         getDriver().createAppServer(appServer);
     }
 
     @Override
     public void attachForestToDatabase(String forestName, String databaseName) {
         getDriver().attachForestToDatabase(forestName, databaseName);
     }
 
     @Override
     public void unmount(Forest forest) {
         getDriver().unmountForest(forest);
         removeFromAttributeSet(FOREST_NAMES, forest.getName());
     }
 
     @Override
     public void mount(Forest forest) {
         getDriver().mountForest(forest);
         addToAttributeSet(FOREST_NAMES, forest.getName());
     }
 
     @Override
     public void createGroup(String groupName) {
         getDriver().createGroup(groupName);
     }
 
     @Override
     public void assignHostToGroup(String hostName, String groupName) {
         getDriver().assignHostToGroup(hostName, groupName);
     }
 
     @Override
     public Set<String> scanForests() {
         return getDriver().scanForests();
     }
 
     @Override
     public Set<String> scanDatabases() {
         return getDriver().scanDatabases();
     }
 
     @Override
     public boolean isUp() {
         return getAttribute(SERVICE_UP);
     }
 
     @Override
     public MarkLogicCluster getCluster() {
         return getConfig(CLUSTER);
     }
 
     @Override
     public void attachReplicaForest(Forest primaryForest, Forest replicaForest) {
         getDriver().attachReplicaForest(primaryForest, replicaForest);
     }
 
     @Override
     public void disableForest(String forestName) {
         getDriver().disableForest(forestName);
     }
 
     @Override
     public void enableForest(String forestName) {
         getDriver().enableForest(forestName);
     }
 
     @Override
     public void setForestHost(String forestName, String hostname) {
         getDriver().setForestHost(forestName, hostname);
     }
 
     @Override
     public String getForestStatus(String forestName) {
         return getDriver().getForestStatus(forestName);
     }
 
     @Override
     public String getPassword() {
         return getConfig(PASSWORD);
     }
 
     @Override
     public String getAdminConnectUrl() {
         return "http://"+getAttribute(MarkLogicNode.HOSTNAME)+":8001";
     }
 
     @Override
     public String getUser() {
         return getConfig(USER);
     }
 
     @Override
     public MarkLogicApi getApi() {
         return getDriver().getApi();
     }
 
     private <T> void addToAttributeSet(AttributeSensor<Set<T>> attribute, T newval) {
         Set<T> newvals = Sets.newLinkedHashSet();
         newvals.add(newval);
         synchronized (attributeSetMutex) {
             Set<T> existing = getAttribute(attribute);
             if (existing != null) newvals.addAll(existing);
             setAttribute(attribute, newvals);
         }
     }
     
     private <T> void removeFromAttributeSet(AttributeSensor<Set<T>> attribute, T oldval) {
         Set<T> newvals = Sets.newLinkedHashSet();
         synchronized (attributeSetMutex) {
             Set<T> existing = getAttribute(attribute);
             if (existing != null) newvals.addAll(existing);
             newvals.remove(oldval);
             setAttribute(attribute, newvals);
         }
     }
 
     @Override
     public String toString() {
         return Objects.toStringHelper(this)
             .add("host", getHostname())
             .add("group", getGroupName())
             .add("type", getNodeType().name())
             .toString();
     }
 }
