 package io.cloudsoft.marklogic.brooklynapplications;
 
 import static brooklyn.entity.java.JavaEntityMethods.javaSysProp;
 import static brooklyn.event.basic.DependentConfiguration.attributeWhenReady;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Lists;
 
 import brooklyn.entity.basic.AbstractApplication;
 import brooklyn.entity.basic.Attributes;
 import brooklyn.entity.proxy.AbstractController;
 import brooklyn.entity.proxy.nginx.NginxController;
 import brooklyn.entity.proxying.EntitySpec;
 import brooklyn.entity.webapp.ControlledDynamicWebAppCluster;
 import brooklyn.entity.webapp.JavaWebAppService;
 import brooklyn.entity.webapp.WebAppService;
 import brooklyn.entity.webapp.WebAppServiceConstants;
 import brooklyn.entity.webapp.jboss.JBoss7Server;
 import brooklyn.launcher.BrooklynLauncher;
 import brooklyn.location.Location;
 import brooklyn.policy.autoscaling.AutoScalerPolicy;
 import brooklyn.util.CommandLineUtil;
 import brooklyn.util.text.Identifiers;
 import io.cloudsoft.marklogic.clusters.MarkLogicCluster;
 import io.cloudsoft.marklogic.databases.Database;
 import io.cloudsoft.marklogic.databases.Databases;
 import io.cloudsoft.marklogic.forests.Forest;
 import io.cloudsoft.marklogic.forests.Forests;
 import io.cloudsoft.marklogic.forests.UpdatesAllowed;
 import io.cloudsoft.marklogic.groups.MarkLogicGroup;
 import io.cloudsoft.marklogic.nodes.MarkLogicNode;
 
 public class MarkLogicDemoApplication extends AbstractApplication {
 
     private static final Logger LOG = LoggerFactory.getLogger(MarkLogicDemoApplication.class);
 
     private final String user = System.getProperty("user.name");
 
     private final int appServicePort = 8011;
     private final String password = "hap00p";
     private final String username = "admin";
 
     private ControlledDynamicWebAppCluster web;
     private MarkLogicCluster markLogicCluster;
 
     @Override
     public void init() {
         boolean deployWeb = false;
 
         EntitySpec<NginxController> loadBalancerSpec = EntitySpec.create(NginxController.class)
                 .displayName("LoadBalancer")
                 .configure("port", 80)
                 //todo: temporary hack to feed the app port to nginx.
                 .configure("portNumberSensor", MarkLogicNode.APP_SERVICE_PORT);
 
         // Minimum volume size in Rackspace is 100Gb
         int volumeSize = 100;
         int backupVolumeSize = 100;
 
         markLogicCluster = addChild(EntitySpec.create(MarkLogicCluster.class)
                 .displayName("MarkLogic Cluster")
                 .configure(MarkLogicCluster.INITIAL_D_NODES_SIZE, 3)
                 .configure(MarkLogicCluster.INITIAL_E_NODES_SIZE, 1)
                 .configure(MarkLogicCluster.LOAD_BALANCER_SPEC, loadBalancerSpec)
                 .configure(MarkLogicNode.VOLUME_SIZE, volumeSize)
                 .configure(MarkLogicNode.BACKUP_VOLUME_SIZE, backupVolumeSize)
                 .configure(MarkLogicNode.IS_FORESTS_EBS, true)
                 .configure(MarkLogicNode.IS_REPLICA_EBS, true)
                 .configure(MarkLogicNode.IS_VAR_OPT_EBS, false)
                 .configure(MarkLogicNode.IS_BACKUP_EBS, false)
                 .configure(MarkLogicNode.IS_FASTDIR_EBS, false));
 
         if (deployWeb) {
             web = addChild(EntitySpec.create(ControlledDynamicWebAppCluster.class)
                     .displayName("WebApp cluster")
                     .configure("initialSize", 1)
                     .configure(ControlledDynamicWebAppCluster.CONTROLLER_SPEC, EntitySpec.create(NginxController.class)
                             .displayName("WebAppCluster Nginx")
                             .configure("port", 8080)
                             .configure("portNumberSensor", WebAppService.HTTP_PORT))
                     .configure(ControlledDynamicWebAppCluster.MEMBER_SPEC, EntitySpec.create(JBoss7Server.class)
                             .configure("initialSize", 1)
                             .configure("httpPort", 8080)
                            .configure(javaSysProp("marklogic.host"), attributeWhenReady(markLogicCluster.getLoadBalancer(), Attributes.HOSTNAME))
                             .configure(javaSysProp("marklogic.port"), "" + appServicePort)
                             .configure(javaSysProp("marklogic.password"), password)
                             .configure(javaSysProp("marklogic.user"), username)
                             .configure(JavaWebAppService.ROOT_WAR, "classpath:/demo-war-0.1.0-SNAPSHOT.war")));
 
             web.getCluster().addPolicy(AutoScalerPolicy.builder()
                     .metric(WebAppServiceConstants.REQUESTS_PER_SECOND_LAST)
                     .sizeRange(1, 5)
                     .metricRange(10, 100)
                     .build());
         }
     }
 
 
     @Override
     public void postStart(Collection<? extends Location> locations) {
         LOG.info("=========================== MarkLogicDemoApp: Starting postStart =========================== ");
 
         super.postStart(locations);
 
         printInfo();
 
         MarkLogicGroup dgroup = markLogicCluster.getDNodeGroup();
         Databases databases = markLogicCluster.getDatabases();
         MarkLogicNode node1 = dgroup.getAnyUpMember();
         MarkLogicNode node2 = dgroup.getAnyOtherUpMember(node1.getHostName());
         Forests forests = markLogicCluster.getForests();
 
         Database database = databases.createDatabaseWithSpec(EntitySpec.create(Database.class)
                 .configure(Database.NAME, "database-" + user)
                 .configure(Database.JOURNALING, "strict")
         );
 
         createReplicatedForest(databases, node1, node2, forests, database, "forest1");
         createReplicatedForest(databases, node2, node1, forests, database, "forest2");
 
         String appServiceName = "DemoService";
         markLogicCluster.getAppServices().createRestAppServer(appServiceName, database.getName(), "Default", appServicePort);
 
         LOG.info("=========================== MarkLogicDemoApp: Finished postStart =========================== ");
     }
 
     private void createReplicatedForest(Databases databases, MarkLogicNode node1, MarkLogicNode node2, Forests forests, Database database, String forestBaseName) {
         String primaryForestId = Identifiers.makeRandomId(8);
         Forest primaryForest = forests.createForestWithSpec(EntitySpec.create(Forest.class)
                 .configure(Forest.HOST, node1.getHostName())
                 .configure(Forest.NAME, forestBaseName + "Primary")
                 .configure(Forest.DATA_DIR, "/var/opt/mldata/" + primaryForestId)
                 .configure(Forest.LARGE_DATA_DIR, "/var/opt/mldata/" + primaryForestId)
                 .configure(Forest.UPDATES_ALLOWED, UpdatesAllowed.ALL)
                 .configure(Forest.REBALANCER_ENABLED, true)
                 .configure(Forest.FAILOVER_ENABLED, true)
         );
 
         String replicaForestId = Identifiers.makeRandomId(8);
         Forest replicaForest = forests.createForestWithSpec(EntitySpec.create(Forest.class)
                 .configure(Forest.HOST, node2.getHostName())
                 .configure(Forest.NAME, forestBaseName + "Replica")
                 .configure(Forest.DATA_DIR, "/var/opt/mldata/" + replicaForestId)
                 .configure(Forest.LARGE_DATA_DIR, "/var/opt/mldata/" + replicaForestId)
                 .configure(Forest.UPDATES_ALLOWED, UpdatesAllowed.ALL)
                 .configure(Forest.REBALANCER_ENABLED, true)
                 .configure(Forest.MASTER, primaryForest.getName())
                 .configure(Forest.FAILOVER_ENABLED, true));
 
         primaryForest.awaitStatus("open");
         databases.attachForestToDatabase(primaryForest, database);
 
         primaryForest.awaitStatus("open");
         replicaForest.awaitStatus("sync replicating");
     }
 
     private void printInfo() {
         MarkLogicNode node = markLogicCluster.getENodeGroup().getAnyUpMember();
         String hostName = node.getHostName();
 
         LOG.info("MarkLogic Nginx http://" + markLogicCluster.getLoadBalancer().getAttribute(Attributes.HOSTNAME));
 //        LOG.info("Web Nginx  http://" + web.getController().getAttribute(Attributes.HOSTNAME));
         int k = 1;
 //        for (Entity entity : web.getCluster().getMembers()) {
 //            LOG.info("   " + k + " JBoss member  http://" + entity.getAttribute(Attributes.HOSTNAME) + ":" + entity.getAttribute(JBoss7Server.HTTP_PORT));
 //            k++;
 //        }
 
         LOG.info("MarkLogic Cluster is available at 'http://" + hostName + ":8000'");
         LOG.info("MarkLogic Cluster summary is available at 'http://" + hostName + ":8001'");
         LOG.info("E-Nodes");
         k = 1;
         for (MarkLogicNode eNode : markLogicCluster.getENodeGroup()) {
             LOG.info("   " + k + " MarkLogic node http://" + eNode.getAttribute(MarkLogicNode.HOSTNAME) + ":8000");
             k++;
         }
 
         LOG.info("D-Nodes");
         k = 1;
         for (MarkLogicNode dNode : markLogicCluster.getDNodeGroup()) {
             LOG.info("   " + k + " MarkLogic node http://" + dNode.getAttribute(MarkLogicNode.HOSTNAME) + ":8000");
             k++;
         }
         LOG.info("MarkLogic Monitoring Dashboard is available at 'http://" + hostName + ":8002/dashboard'");
     }
 
     public static void main(String[] argv) {
         List<String> args = Lists.newArrayList(argv);
         String port = CommandLineUtil.getCommandLineOption(args, "--port", "8081+");
         String location = CommandLineUtil.getCommandLineOption(args, "--location", "localhost");
 
         BrooklynLauncher launcher = BrooklynLauncher.newInstance()
                 .application(
                         EntitySpec.create(MarkLogicDemoApplication.class)
                                 .displayName("MarkLogic demo"))
                 .webconsolePort(port)
                 .location(location)
                 .start();
     }
 }
