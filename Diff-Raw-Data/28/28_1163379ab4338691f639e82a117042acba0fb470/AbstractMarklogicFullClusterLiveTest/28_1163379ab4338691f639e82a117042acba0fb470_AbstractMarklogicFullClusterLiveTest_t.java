 package io.cloudsoft.marklogic;
 
 import brooklyn.config.BrooklynProperties;
 import brooklyn.entity.basic.ApplicationBuilder;
 import brooklyn.entity.basic.Entities;
 import brooklyn.entity.trait.Startable;
 import brooklyn.location.Location;
 import brooklyn.management.ManagementContext;
 import brooklyn.management.internal.LocalManagementContext;
 import brooklyn.test.EntityTestUtils;
 import brooklyn.test.entity.TestApplication;
 import brooklyn.util.MutableMap;
 import brooklyn.util.text.Identifiers;
 import io.cloudsoft.marklogic.appservers.AppServices;
 import io.cloudsoft.marklogic.clusters.MarkLogicCluster;
 import io.cloudsoft.marklogic.databases.Database;
 import io.cloudsoft.marklogic.databases.Databases;
 import io.cloudsoft.marklogic.forests.Forest;
 import io.cloudsoft.marklogic.forests.Forests;
 import io.cloudsoft.marklogic.forests.UpdatesAllowed;
 import io.cloudsoft.marklogic.groups.MarkLogicGroup;
 import io.cloudsoft.marklogic.nodes.MarkLogicNode;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 
 import java.util.Arrays;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import static brooklyn.entity.proxying.EntitySpecs.spec;
 
 public abstract class AbstractMarklogicFullClusterLiveTest {
 
     public static final Logger LOG = LoggerFactory.getLogger(AbstractMarklogicFullClusterLiveTest.class);
     public final String user = System.getProperty("user.name");
 
     public static final String PROVIDER = "aws-ec2";
     public static final String REGION_NAME = "us-east-1";
     public static final String TINY_HARDWARE_ID = "t1.micro";
     public static final String SMALL_HARDWARE_ID = "m1.small";
     public static final String MEDIUM_HARDWARE_ID = "m1.medium";
 
     public static BrooklynProperties brooklynProperties;
     public static ManagementContext ctx;
 
     public static final AtomicInteger ID_GENERATOR = new AtomicInteger();
     public static AppServices appServices;
     public static MarkLogicGroup egroup;
     public static TestApplication app;
     public static Location jcloudsLocation;
     public static Forests forests;
     public static MarkLogicCluster markLogicCluster;
     public static Databases databases;
     public static MarkLogicGroup dgroup;
     public static MarkLogicNode dNode1;
     public static MarkLogicNode dNode2;
     public static MarkLogicNode dNode3;
 
     @BeforeClass(alwaysRun = true)
     public static void beforeClass() throws Exception {
         try {
             ctx = new LocalManagementContext();
             brooklynProperties = (BrooklynProperties) ctx.getConfig();
 
             // Don't let any defaults from brooklyn.properties (except credentials) interfere with test
             brooklynProperties.remove("brooklyn.jclouds." + PROVIDER + ".image-description-regex");
             brooklynProperties.remove("brooklyn.jclouds." + PROVIDER + ".image-name-regex");
             brooklynProperties.remove("brooklyn.jclouds." + PROVIDER + ".image-id");
             brooklynProperties.remove("brooklyn.jclouds." + PROVIDER + ".inboundPorts");
             brooklynProperties.remove("brooklyn.jclouds." + PROVIDER + ".hardware-id");
 
             // Also removes scriptHeader (e.g. if doing `. ~/.bashrc` and `. ~/.profile`, then that can cause "stdin: is not a tty")
             brooklynProperties.remove("brooklyn.ssh.config.scriptHeader");
 
             app = ApplicationBuilder.newManagedApp(TestApplication.class, ctx);
             markLogicCluster = app.createAndManageChild(spec(MarkLogicCluster.class)
                     .configure(MarkLogicCluster.INITIAL_D_NODES_SIZE, 3)
                     .configure(MarkLogicCluster.INITIAL_E_NODES_SIZE, 1)
                     .configure(MarkLogicNode.IS_FORESTS_EBS, true)
                     .configure(MarkLogicNode.IS_VAR_OPT_EBS, false)
                     .configure(MarkLogicNode.IS_BACKUP_EBS, false)
             );
 
             //general purpose centos image
             String ami = "ami-3275ee5b";
             //custom image based on the general purpose centos image, but with the marklogic rpm downloaded
             //String ami = "ami-c4f78aad";
 
             Map<String, ?> jcloudsFlags = MutableMap.of("imageId", REGION_NAME + "/" + ami, "user", "ec2-user", "hardwareId", MEDIUM_HARDWARE_ID);
             jcloudsLocation = ctx.getLocationRegistry().resolve(PROVIDER + ":" + REGION_NAME, jcloudsFlags);
 
             app.start(Arrays.asList(jcloudsLocation));
             EntityTestUtils.assertAttributeEqualsEventually(app, Startable.SERVICE_UP, true);
 
             databases = markLogicCluster.getDatabases();
             appServices = markLogicCluster.getAppservices();
             forests = markLogicCluster.getForests();
             dgroup = markLogicCluster.getDNodeGroup();
             egroup = markLogicCluster.getENodeGroup();
             dNode1 = dgroup.getAnyUpMember();
             dNode2 = dgroup.getAnyOtherUpMember(dNode1.getHostName());
             dNode3 = dgroup.getAnyOtherUpMember(dNode1.getHostName(), dNode2.getHostName());
         } catch (Exception e) {
             LOG.error("Failed to setup cluster", e);
             throw e;
         }
     }
 
     @AfterClass(alwaysRun = true)
     public static void afterClass() throws Exception {
         if (app != null) {
             //for (Entity entity : forests.getChildren()) {
             //    if (entity instanceof Forest) {
             //        Forest forest = (Forest) entity;
             //        if (forest.getDataDir() != null) {
             //            forests.enableForest(forest.getName(), false);
             //            forests.unmountForest(forest.getName());
             //        }
             //    }
             //}
            Entities.destroyAll(app.getManagementContext());
         }
     }
 
     public Forest createForest(MarkLogicNode node) {
         return createForest(node,null);
     }
 
     public Forest createForest(MarkLogicNode node, String master) {
         String forestId = Identifiers.makeRandomId(8);
         return forests.createForestWithSpec(spec(Forest.class)
                 .configure(Forest.HOST, node.getHostName())
                 .configure(Forest.NAME, user + "Forest" + ID_GENERATOR.incrementAndGet())
                 .configure(Forest.DATA_DIR, "/var/opt/mldata/" + forestId)
                 .configure(Forest.LARGE_DATA_DIR, "/var/opt/mldata/" + forestId)
                         //.configure(Forest.FAST_DATA_DIR, "/var/opt/mldata/" + forestId)
                 .configure(Forest.MASTER, master)
                 .configure(Forest.UPDATES_ALLOWED, UpdatesAllowed.ALL)
                 .configure(Forest.REBALANCER_ENABLED, true)
                 .configure(Forest.FAILOVER_ENABLED, true)
         );
     }
 
     public Database createDatabase() {
         return databases.createDatabaseWithSpec(spec(Database.class)
                 .configure(Database.NAME, "database-" + user + ID_GENERATOR.incrementAndGet())
                 .configure(Database.JOURNALING, "strict")
         );
     }
 }
