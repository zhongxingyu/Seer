 package io.cloudsoft.marklogic.nodes;
 
 import brooklyn.config.ConfigKey;
 import brooklyn.entity.annotation.Effector;
 import brooklyn.entity.annotation.EffectorParam;
 import brooklyn.entity.basic.SoftwareProcess;
 import brooklyn.entity.proxying.ImplementedBy;
 import brooklyn.event.AttributeSensor;
 import brooklyn.event.basic.BasicAttributeSensor;
 import brooklyn.event.basic.BasicAttributeSensorAndConfigKey;
 import brooklyn.event.basic.PortAttributeSensorAndConfigKey;
 import com.google.common.collect.ImmutableList;
 
 import io.cloudsoft.marklogic.api.MarkLogicApi;
 import io.cloudsoft.marklogic.appservers.AppServer;
 import io.cloudsoft.marklogic.clusters.MarkLogicCluster;
 import io.cloudsoft.marklogic.databases.Database;
 import io.cloudsoft.marklogic.forests.Forest;
 
 import java.util.Set;
 
 import static brooklyn.entity.basic.ConfigKeys.*;
 import static brooklyn.event.basic.Sensors.newStringSensor;
 
 /**
  * A single node in a MarkLogic {@link MarkLogicCluster cluster}.
  */
 @ImplementedBy(MarkLogicNodeImpl.class)
 public interface MarkLogicNode extends SoftwareProcess {
 
     ConfigKey<MarkLogicCluster> CLUSTER = newConfigKey(
             MarkLogicCluster.class,
             "marklogic.node.cluster",
             "The cluster this node belongs to");
 
     ConfigKey<NodeType> NODE_TYPE = newConfigKey(
             NodeType.class,
             "marklogic.node-type",
             "The type of the marklogic node; d-type only has forests, e-type only has appservers, d+e-type can have both",
             NodeType.E_D_NODE);
 
     ConfigKey<String> WEBSITE_USERNAME = newStringConfigKey(
             "marklogic.website-username",
             "The username to access MarkLogic Server website");
 
     ConfigKey<String> WEBSITE_PASSWORD = newStringConfigKey(
             "marklogic.website-password",
             "The password to access MarkLogic website");
 
     ConfigKey<String> USER = newStringConfigKey(
             "marklogic.user",
             "The user to access MarkLogic Server Web UI",
             "admin");
 
     ConfigKey<String> PASSWORD = newStringConfigKey("marklogic.password",
             "The password to access MarkLogic Server Web UI",
             "hap00p");
 
     ConfigKey<String> LICENSE_KEY = newStringConfigKey(
             "marklogic.licenseKey",
             "The license key to register the MarkLogic Server");
 
    ConfigKey<String> LICENSE_TYPE = newStringConfigKey(
            "marklogic.licenseType",
            "The type of agreement the licensee has with MarkLogic, e.g. development or evaluation",
            "development");

     ConfigKey<String> LICENSEE = newStringConfigKey(
             "marklogic.licensee",
             "The licensee to register the MarkLogic Server");
 
     ConfigKey<String> HOST = newStringConfigKey(
             "marklogic.host",
             "The internal identifier of this marklogic node");
 
     ConfigKey<String> GROUP = newStringConfigKey(
             "marklogic.group",
             "The MarkLogic group this node belongs to",
             "Default");
 
     ConfigKey<String> CLUSTER_NAME = newStringConfigKey(
             "marklogic.cluster",
             "The cluster name");
 
     ConfigKey<Boolean> IS_INITIAL_HOST = newBooleanConfigKey(
             "marklogic.node.isInitialHost", "Whether this node in the cluster is the initialHost");
 
     ConfigKey<Boolean> IS_FORESTS_EBS = newBooleanConfigKey(
             "marklogic.node.isForestsEbs",
             "Whether the forests should use EBS Volumes",
             true);
 
     ConfigKey<Boolean> IS_VAR_OPT_EBS = newBooleanConfigKey(
             "marklogic.node.isVaroptEbs", "Whether the /var/opt should use an EBS Volume",
             true);
 
     ConfigKey<Boolean> IS_BACKUP_EBS = newBooleanConfigKey(
             "marklogic.node.isBackupEbs",
             "Whether the backup should use an EBS Volume",
             true);
 
     ConfigKey<Boolean> IS_REPLICA_EBS = newBooleanConfigKey(
             "marklogic.node.isReplicaEbs",
             "Whether the replica should use an EBS Volume",
             true);
 
     ConfigKey<Boolean> IS_FASTDIR_EBS = newBooleanConfigKey(
             "marklogic.node.isFastdirEbs",
             "Whether the fastdir should use an EBS Volume",
             true);
 
     // TODO: Change to instance of BlockDevice
     BasicAttributeSensorAndConfigKey<String> VAR_OPT_VOLUME = new BasicAttributeSensorAndConfigKey<String>(
             String.class, "marklogic.node.volumes.varOpt", "EBS Volume ID for /var/opt (or null if does not already exist)", null);
 
     BasicAttributeSensorAndConfigKey<String> BACKUP_VOLUME = new BasicAttributeSensorAndConfigKey<String>(
             String.class, "marklogic.node.volumes.backup", "EBS Volume ID for the backup volume (or null if does not already exist)", null);
 
     // FIXME Should be 100GB, but set to 10GB for now, for cheaper testing!
     ConfigKey<Integer> VOLUME_SIZE = newIntegerConfigKey(
             "marklogic.node.volumes.size",
             "The size of each EBS Volume for /var/opt, regular, fastdir and replica (if being created from scratch)",
             10);
 
     // FIXME Should be 200GB, but set to 10GB for now, for cheaper testing!
     ConfigKey<Integer> BACKUP_VOLUME_SIZE = newIntegerConfigKey(
             "marklogic.node.volumes.backupSize",
             "The size of backup EBS Volume (if being created from scratch)",
             10);
 
     AttributeSensor<String> URL = newStringSensor(
             "marklogic.node.url",
             "Base URL for MarkLogic node");
 
     ConfigKey<Integer> BIND_PORT = newIntegerConfigKey(
             "marklogic.bindPort",
             "The distributed protocol server socket bind internet port number.",
             7999);
 
     ConfigKey<Integer> FOREIGN_BIND_PORT = newIntegerConfigKey(
             "marklogic.foreignBindPort",
             "The distributed protocol server socket bind internet port number.",
             7998);
 
     AttributeSensor<Set<String>> FOREST_NAMES = new BasicAttributeSensor(
             Set.class, "marklogic.node.forests", "Names of forests at this node");
 
     //TODO: This should not be here, it is a temporary hack to let the nginx loadbalancer read out the port.
     PortAttributeSensorAndConfigKey APP_SERVICE_PORT = new PortAttributeSensorAndConfigKey(
             "http.port", "HTTP port", ImmutableList.of(8011));
 
     void createForest(Forest forest);
 
     String getHostname();
 
     void createAppServer(AppServer appServer);
 
     void createGroup(String groupName);
 
     void assignHostToGroup(String hostAddress, String groupName);
 
     String getGroupName();
 
     void createDatabase(Database database);
 
     Set<String> scanForests();
 
     Set<String> scanDatabases();
 
     boolean isUp();
 
     void attachForestToDatabase(String forestName, String databaseName);
 
     MarkLogicCluster getCluster();
 
     void attachReplicaForest(Forest primaryForest, Forest replicaForest);
 
     void enableForest(String forestName);
 
     void disableForest(String forestName);
 
     void setForestHost(String forestName, String hostname);
 
     String getForestStatus(String forestName);
 
     String getUser();
 
     String getPassword();
 
     String getAdminConnectUrl();
 
     @Effector
     void unmount(@EffectorParam(name = "forest") Forest forest);
 
     @Effector
     void mount(@EffectorParam(name = "forest") Forest forest);
 
     /**
      * @return An instance of {@link MarkLogicApi} suitable for operations on this node.
      */
     MarkLogicApi getApi();
 }
