 package io.cloudsoft.marklogic;
 
 import static brooklyn.entity.proxying.EntitySpecs.spec;
 import static java.lang.String.format;
 import io.cloudsoft.marklogic.forests.Forests;
 import io.cloudsoft.marklogic.groups.MarkLogicGroup;
 import io.cloudsoft.marklogic.nodes.MarkLogicNode;
 
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import brooklyn.config.BrooklynProperties;
 import brooklyn.entity.basic.ApplicationBuilder;
 import brooklyn.entity.basic.Entities;
 import brooklyn.entity.trait.Startable;
 import brooklyn.location.Location;
 import brooklyn.location.basic.SshMachineLocation;
 import brooklyn.management.ManagementContext;
 import brooklyn.management.internal.LocalManagementContext;
 import brooklyn.test.EntityTestUtils;
 import brooklyn.test.entity.TestApplication;
 import brooklyn.util.MutableMap;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 
 @Test(groups = {"Live"})
 public class ForestTest {
 
     public static final Logger LOG = LoggerFactory.getLogger(ForestTest.class);
 
     public static final String PROVIDER = "aws-ec2";
 
     public static final String MEDIUM_HARDWARE_ID = "m1.medium";
 
     protected BrooklynProperties brooklynProperties;
     protected ManagementContext ctx;
 
     protected TestApplication app;
     protected Location jcloudsLocation;
     private MarkLogicGroup group;
     private Forests forests;
 
     @BeforeMethod(alwaysRun = true)
     public void setUp() throws Exception {
         // Don't let any defaults from brooklyn.properties (except credentials) interfere with test
         brooklynProperties = BrooklynProperties.Factory.newDefault();
         brooklynProperties.remove("brooklyn.jclouds." + PROVIDER + ".image-description-regex");
         brooklynProperties.remove("brooklyn.jclouds." + PROVIDER + ".image-name-regex");
         brooklynProperties.remove("brooklyn.jclouds." + PROVIDER + ".image-id");
         brooklynProperties.remove("brooklyn.jclouds." + PROVIDER + ".inboundPorts");
         brooklynProperties.remove("brooklyn.jclouds." + PROVIDER + ".hardware-id");
         // Also removes scriptHeader (e.g. if doing `. ~/.bashrc` and `. ~/.profile`, then that can cause "stdin: is not a tty")
         brooklynProperties.remove("brooklyn.ssh.config.scriptHeader");
 
         String regionName = "us-east-1";
         String amiId = "ami-3275ee5b";
         String loginUser = "ec2-user";
         String imageId = format("%s/%s", regionName, amiId);
         Map<?, ?> flags = ImmutableMap.of("imageId", imageId, "hardwareId", MEDIUM_HARDWARE_ID, "loginUser", loginUser);
 
         Map<?, ?> jcloudsFlags = MutableMap.builder().putAll(flags).build();
         String locationSpec = format("%s:%s", "ec2", regionName);
 
         ctx = new LocalManagementContext(brooklynProperties);
         jcloudsLocation = ctx.getLocationRegistry().resolve(locationSpec, jcloudsFlags);
         app = ApplicationBuilder.newManagedApp(TestApplication.class, ctx);
         group = app.createAndManageChild(spec(MarkLogicGroup.class));
         forests = app.createAndManageChild(spec(Forests.class)
                 .configure(Forests.GROUP, group)
         );
     }
 
     @AfterMethod(alwaysRun = true)
     public void tearDown() throws Exception {
         if (app != null) Entities.destroyAll(app);
     }
 
     @Test
     public void testCreateForest() throws Exception {
         app.start(ImmutableList.of(jcloudsLocation));
         LOG.info("Waiting for app to start");
         EntityTestUtils.assertAttributeEqualsEventually(app, Startable.SERVICE_UP, true);
         LOG.info("App started");
 
         String forestName = "peter" + System.currentTimeMillis();
         MarkLogicNode node = group.getAnyUpMember();
         forests.createForest(forestName, node.getHostName(), null, null, null, "all", true, false);
 
 
         String username = node.getConfig(MarkLogicNode.USER);
         String password = node.getConfig(MarkLogicNode.PASSWORD);
 
         List<String> checkIfExistCommand = ImmutableList.of(
                format("curl -L --digest -u %s:%s 'http://localhost:8001/forest-summary.xqy?section=forest' | grep %s", username, password, forestName));
 
         SshMachineLocation sshMachineLocation = (SshMachineLocation) node.getLocations().iterator().next();
 
         int exitCode = sshMachineLocation.exec(ImmutableMap.of(), checkIfExistCommand);
         Assert.assertEquals(0, exitCode, "forest:" + forestName + " was not found");
     }
 }
