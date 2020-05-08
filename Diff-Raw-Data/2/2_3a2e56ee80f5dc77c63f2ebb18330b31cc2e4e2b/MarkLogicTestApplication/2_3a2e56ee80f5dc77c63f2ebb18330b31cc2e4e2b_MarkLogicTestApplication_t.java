 package io.cloudsoft.marklogic.brooklynapplications;
 
 import brooklyn.entity.Entity;
 import brooklyn.entity.basic.AbstractApplication;
 import brooklyn.entity.basic.Entities;
 import brooklyn.entity.proxying.EntitySpecs;
 import brooklyn.launcher.BrooklynLauncher;
 import brooklyn.location.Location;
 import brooklyn.util.CommandLineUtil;
 import brooklyn.util.text.Identifiers;
 import com.google.common.collect.Lists;
 import io.cloudsoft.marklogic.clusters.MarkLogicCluster;
 import io.cloudsoft.marklogic.databases.Database;
 import io.cloudsoft.marklogic.databases.Databases;
 import io.cloudsoft.marklogic.forests.Forest;
 import io.cloudsoft.marklogic.forests.Forests;
 import io.cloudsoft.marklogic.forests.UpdatesAllowed;
 import io.cloudsoft.marklogic.groups.MarkLogicGroup;
 import io.cloudsoft.marklogic.nodes.MarkLogicNode;
 
 import java.util.Collection;
 import java.util.List;
 
 import static brooklyn.entity.proxying.EntitySpecs.spec;
 
 /**
  * App to create a MarkLogic cluster (in a single availability zone).
  * <p/>
  * This can be launched by either:
  * <ul>
  * <li>Running the main method
  * <li>Running {@code export BROOKLYN_CLASSPATH=$(pwd)/target/classes; brooklyn launch --app io.cloudsoft.marklogic.MarkLogicApp}
  * </ul>
  */
 public class MarkLogicTestApplication extends AbstractApplication {
 
     // For naming databases/forests, so can tell in cloud provider's console who ran it
     private final String user = System.getProperty("user.name");
 
     private MarkLogicGroup dgroup;
     private Databases databases;
     private Forests forests;
     private MarkLogicCluster markLogicCluster;
 
     @Override
     public void init() {
         markLogicCluster = addChild(spec(MarkLogicCluster.class)
                 .displayName("MarkLogic Cluster")
                 .configure(MarkLogicCluster.INITIAL_D_NODES_SIZE, 1)
                 .configure(MarkLogicCluster.INITIAL_E_NODES_SIZE, 0)
                 .configure(MarkLogicNode.IS_FORESTS_EBS, true)
                 .configure(MarkLogicNode.IS_VAR_OPT_EBS, false)
                 .configure(MarkLogicNode.IS_BACKUP_EBS, false)
         );
         databases = markLogicCluster.getDatabases();
         forests = markLogicCluster.getForests();
         dgroup = markLogicCluster.getDNodeGroup();
     }
 
     @Override
     public void postStart(Collection<? extends Location> locations) {
         super.postStart(locations);
 
         LOG.info("MarkLogic Cluster Members:");
         int k = 1;
         for (Entity entity : dgroup.getMembers()) {
             LOG.info("   " + k + " MarkLogic node http://" + entity.getAttribute(MarkLogicNode.HOSTNAME) + ":8001");
             k++;
         }
 
         LOG.info("MarkLogic server is available at 'http://" +
                 dgroup.getAnyUpMember().getHostName() + ":8000'");
         LOG.info("MarkLogic Cluster summary is available at 'http://" +
                 dgroup.getAnyUpMember().getHostName() +
                 ":8001'");
         LOG.info("MarkLogic Monitoring Dashboard is available at 'http://" +
                 dgroup.getAnyUpMember().getHostName() +
                 ":8002/dashboard'");
 
         Database db = markLogicCluster.getDatabases().createDatabase("peter");
 
 
         try {
             MarkLogicNode node1 = dgroup.getAnyUpMember();
 //            MarkLogicNode node2 = dgroup.getAnyOtherUpMember(node1.getHostName());
 //            MarkLogicNode node3 = dgroup.getAnyOtherUpMember(node1.getHostName(), node2.getHostName());
 //
             Database database = databases.createDatabaseWithSpec(spec(Database.class)
                     .configure(Database.NAME, "database-" + user)
                     .configure(Database.JOURNALING, "strict")
             );
 
             String primaryForestId = Identifiers.makeRandomId(8);
             Forest primaryForest = forests.createForestWithSpec(spec(Forest.class)
                     .configure(Forest.HOST, node1.getHostName())
                     .configure(Forest.NAME, user + "-forest"+primaryForestId)
                     .configure(Forest.DATA_DIR, "/var/opt/mldata/" + primaryForestId)
                     .configure(Forest.LARGE_DATA_DIR, "/var/opt/mldata/" + primaryForestId)
 //                .configure(Forest.FAST_DATA_DIR, "/var/opt/mldata/" + primaryForestId)
                     .configure(Forest.UPDATES_ALLOWED, UpdatesAllowed.ALL)
                     .configure(Forest.REBALANCER_ENABLED, true)
                     .configure(Forest.FAILOVER_ENABLED, true)
             );
 
 //            String forestId2= Identifiers.makeRandomId(8);
 //            Forest forest2 = forests.createForestWithSpec(spec(Forest.class)
 //                    .configure(Forest.HOST, node2.getHostName())
 //                    .configure(Forest.NAME, user + "-forest"+forestId2)
 //                    .configure(Forest.DATA_DIR, "/var/opt/mldata/" + primaryForestId)
 //                    .configure(Forest.LARGE_DATA_DIR, "/var/opt/mldata/" + primaryForestId)
 ////                .configure(Forest.FAST_DATA_DIR, "/var/opt/mldata/" + primaryForestId)
 //                    .configure(Forest.UPDATES_ALLOWED, UpdatesAllowed.ALL)
 //                    .configure(Forest.REBALANCER_ENABLED, true)
 //                    .configure(Forest.FAILOVER_ENABLED, true)
 //            );
 //
 //          //  node1.stop();
 //
 //
 //            String replicaForestId = Identifiers.makeRandomId(8);
 //            Forest replicaForest = forests.createForestWithSpec(spec(Forest.class)
 //                    .configure(Forest.HOST, node2.getHostName())
 //                    .configure(Forest.NAME, user + "-forest-replica")
 //                    .configure(Forest.DATA_DIR, "/var/opt/mldata/" + replicaForestId)
 //                    .configure(Forest.LARGE_DATA_DIR, "/var/opt/mldata/" + replicaForestId)
 ////                    .configure(Forest.FAST_DATA_DIR, "/var/opt/mldata/" + replicaForestId)
 ////         //       .configure(Forest.DATA_DIR, "/tmp/")
 ////         //       .configure(Forest.LARGE_DATA_DIR, "/tmp/")
 ////         //       .configure(Forest.FAST_DATA_DIR, "/tmp/")
 ////
 //                    .configure(Forest.UPDATES_ALLOWED, UpdatesAllowed.ALL)
 //                    .configure(Forest.REBALANCER_ENABLED, true)
 //                    .configure(Forest.FAILOVER_ENABLED, true));
 //
 //            primaryForest.awaitStatus("open");
 
 //             replicaForest.awaitStatus("open");
 
 //            forests.attachReplicaForest(primaryForest.getName(), replicaForest.getName());
 
             databases.attachForestToDatabase(primaryForest.getName(), database.getName());
 
             primaryForest.awaitStatus("open");
 //            replicaForest.awaitStatus("sync replicating");
 
 //            forests.enableForest(primaryForest.getName(), false);
 ///
 //            primaryForest.awaitStatus("unmounted");
 //            replicaForest.awaitStatus("open");
 //
 //            forests.unmountForest(primaryForest.getName());
 //
 //            forests.setForestHost(primaryForest.getName(), node3.getHostName());
 //
 //            forests.mountForest(primaryForest.getName());
 //
 //            forests.enableForest(primaryForest.getName(), true);
 //
 //            primaryForest.awaitStatus("sync replicating");
 //            replicaForest.awaitStatus("open");
 //
 //            forests.enableForest(replicaForest.getName(), false);
 //            forests.enableForest(replicaForest.getName(), true);
 //
 //            primaryForest.awaitStatus("open");
 //            replicaForest.awaitStatus("sync replicating");
 //
             LOG.info("Done");
         } catch (Exception e) {
            LOG.error("Error starting MarkLogic app", e);
         }
     }
 
     /**
      * Launches the application, along with the brooklyn web-console.
      */
     public static void main(String[] argv) throws Exception {
         List<String> args = Lists.newArrayList(argv);
         String port = CommandLineUtil.getCommandLineOption(args, "--port", "8081+");
         String location = CommandLineUtil.getCommandLineOption(args, "--location", "localhost");
 
         BrooklynLauncher launcher = BrooklynLauncher.newInstance()
                 .application(EntitySpecs.appSpec(MarkLogicTestApplication.class).displayName("Brooklyn MarkLogic Application"))
                 .webconsolePort(port)
                 .location(location)
                 .start();
 
         Entities.dumpInfo(launcher.getApplications());
     }
 }
