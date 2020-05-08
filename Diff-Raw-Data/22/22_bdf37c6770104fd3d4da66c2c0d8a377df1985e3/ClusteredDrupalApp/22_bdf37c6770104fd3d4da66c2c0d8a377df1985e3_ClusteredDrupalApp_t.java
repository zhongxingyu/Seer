 package io.cloudsoft.socialapps.drupal.examples;
 
 import static brooklyn.event.basic.DependentConfiguration.attributeWhenReady;
 import io.cloudsoft.socialapps.drupal.Drupal;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import brooklyn.config.BrooklynProperties;
 import brooklyn.enricher.basic.SensorPropagatingEnricher;
 import brooklyn.entity.basic.ApplicationBuilder;
 import brooklyn.entity.basic.Entities;
 import brooklyn.entity.basic.StartableApplication;
 import brooklyn.entity.database.mysql.MySqlNode;
 import brooklyn.entity.proxying.BasicEntitySpec;
 import brooklyn.entity.proxying.EntitySpec;
 import brooklyn.entity.webapp.ControlledDynamicWebAppCluster;
 import brooklyn.entity.webapp.WebAppService;
 import brooklyn.launcher.BrooklynLauncher;
 import brooklyn.launcher.BrooklynServerDetails;
 import brooklyn.location.Location;
 import brooklyn.util.CommandLineUtil;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 
 /**
  * This example Application starts up a Scalable drupal environment.
  * <p/>
  * TODO: This is work in progres..
  * <p/>
  * <p/>
  * http://www.johnandcailin.com/blog/john/scaling-drupal-open-source-infrastructure-high-traffic-drupal-sites
  */
 public class ClusteredDrupalApp extends ApplicationBuilder {
 
     public static final Logger log = LoggerFactory.getLogger(BasicDrupalApp.class);
 
     private final static String SCRIPT = "create database drupal; " +
             "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, INDEX, ALTER, CREATE TEMPORARY TABLES, LOCK TABLES ON drupal.* TO 'drupal'@'%'  IDENTIFIED BY 'password'; " +
             "FLUSH PRIVILEGES;";
 
     private MySqlNode mySqlNode;
     private ControlledDynamicWebAppCluster cluster;
 
     @Override
     protected void doBuild() {
        mySqlNode = createChild(BasicEntitySpec.newInstance(MySqlNode.class)
                 .configure(MySqlNode.CREATION_SCRIPT_CONTENTS, SCRIPT));
 
         EntitySpec<Drupal> drupalSpec = BasicEntitySpec.newInstance(Drupal.class)
                 .configure(Drupal.DATABASE_UP, attributeWhenReady(mySqlNode, MySqlNode.SERVICE_UP))
                 .configure(Drupal.DATABASE_HOST, attributeWhenReady(mySqlNode, MySqlNode.HOSTNAME))
                 .configure(Drupal.DATABASE_PORT, attributeWhenReady(mySqlNode, MySqlNode.MYSQL_PORT))
                 .configure(Drupal.DATABASE_SCHEMA, "drupal")
                 .configure(Drupal.DATABASE_USER, "drupal")
                 .configure(Drupal.DATABASE_PASSWORD, "password")
                .configure(Drupal.ADMIN_EMAIL, "foo@example.com");
 
         cluster = createChild(BasicEntitySpec.newInstance(ControlledDynamicWebAppCluster.class)
                 .configure(ControlledDynamicWebAppCluster.MEMBER_SPEC, drupalSpec)
                 .configure(ControlledDynamicWebAppCluster.INITIAL_SIZE, 2));
         
         SensorPropagatingEnricher.newInstanceListeningTo(cluster, WebAppService.ROOT_URL).addToEntityAndEmitAll(getApp());
     }
 
     // can start in AWS by running this -- or use brooklyn CLI/REST for most clouds, or programmatic/config for set of fixed IP machines
     public static void main(String[] argv) throws Exception {
         List<String> args = Lists.newArrayList(argv);
         String port =  CommandLineUtil.getCommandLineOption(args, "--port", "8081+");
         String location = CommandLineUtil.getCommandLineOption(args, "--location", "aws-ec2:us-east-1");
 
         // Image: {id=us-east-1/ami-1970da70, providerId=ami-1970da70, name=RightImage_Ubuntu_12.04_x64_v5.8.8, location={scope=REGION, id=us-east-1, description=us-east-1, parent=aws-ec2, iso3166Codes=[US-VA]}, os={family=ubuntu, arch=paravirtual, version=12.04, description=rightscale-us-east/RightImage_Ubuntu_12.04_x64_v5.8.8.manifest.xml, is64Bit=true}, description=rightscale-us-east/RightImage_Ubuntu_12.04_x64_v5.8.8.manifest.xml, version=5.8.8, status=AVAILABLE[available], loginUser=root, userMetadata={owner=411009282317, rootDeviceType=instance-store, virtualizationType=paravirtual, hypervisor=xen}}
         // TODO Set for only us-east-1 region, rather than all aws-ec2
         BrooklynProperties brooklynProperties = BrooklynProperties.Factory.newDefault();
         brooklynProperties.put("brooklyn.jclouds.aws-ec2.image-id", "us-east-1/ami-1970da70");
         
         BrooklynServerDetails server = BrooklynLauncher.newLauncher()
                 .brooklynProperties(brooklynProperties)
                 .webconsolePort(port)
                 .launch();
 
         Location loc = server.getManagementContext().getLocationRegistry().resolve(location);
 
         StartableApplication app = new ClusteredDrupalApp()
                 .appDisplayName("Clustered drupal app")
                 .manage(server.getManagementContext());
         
         app.start(ImmutableList.of(loc));
         
         Entities.dumpInfo(app);
     }
 }
