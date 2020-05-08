 package io.cloudsoft.socialapps.drupal.examples;
 
 import static java.util.Arrays.asList;
 import io.cloudsoft.socialapps.drupal.Drupal;
 
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import brooklyn.config.BrooklynProperties;
 import brooklyn.entity.basic.AbstractApplication;
 import brooklyn.entity.database.mysql.MySqlNode;
 import brooklyn.event.basic.DependentConfiguration;
 import brooklyn.launcher.BrooklynLauncher;
 import brooklyn.location.basic.LocationRegistry;
 import brooklyn.location.basic.SshMachineLocation;
 import brooklyn.location.basic.jclouds.JcloudsLocation;
 import brooklyn.util.MutableMap;
 
 /**
  * This example Application starts up a single Ubuntu machine in Amazon EC2 that runs both Drupal and MySQL.
  * <p/>
  * To open the Brooklyn WebConsole open: http://localhost:8081 and login with admin/password.
  */
 public class BasicDrupalApp extends AbstractApplication {
 
     public static final Logger log = LoggerFactory.getLogger(BasicDrupalApp.class);
 
     private final static String SCRIPT = "create database drupal; " +
             "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, INDEX, ALTER, CREATE TEMPORARY TABLES, LOCK TABLES ON drupal.* TO 'drupal'@'127.0.0.1'  IDENTIFIED BY 'password'; " +
             "FLUSH PRIVILEGES;";
 
     private Drupal drupal;
     private MySqlNode mySqlNode;
 
     public BasicDrupalApp() {
         Map mysqlConf = MutableMap.of("creationScriptContents", SCRIPT);
         mySqlNode = new MySqlNode(mysqlConf, this);
 
         drupal = new Drupal(this);
         drupal.setConfig(Drupal.DATABASE_HOST, "127.0.0.1");
         drupal.setConfig(Drupal.DATABASE_SCHEMA, "drupal");
         drupal.setConfig(Drupal.DATABASE_USER, "drupal");
         drupal.setConfig(Drupal.DATABASE_PORT, DependentConfiguration.attributeWhenReady(mySqlNode, MySqlNode.MYSQL_PORT));
         drupal.setConfig(Drupal.DATABASE_PASSWORD, "password");
         drupal.setConfig(Drupal.ADMIN_EMAIL, "foo@bar.com");
         drupal.setConfig(Drupal.DATABASE_UP, DependentConfiguration.attributeWhenReady(mySqlNode, MySqlNode.SERVICE_UP));
     }
 
     // can start in AWS by running this -- or use brooklyn CLI/REST for most clouds, or programmatic/config for set of fixed IP machines
     public static void main(String[] args) throws Exception {
         BasicDrupalApp app = new BasicDrupalApp();
         BrooklynLauncher.manage(app, 8081);
 
         BrooklynProperties brooklynProperties = BrooklynProperties.Factory.newDefault();
         //brooklynProperties.put("brooklyn.jclouds.aws-ec2.image-name-regex","ubuntu-oneiric");
         brooklynProperties.put("brooklyn.jclouds.cloudservers-uk.image-name-regex", "Debian");
         brooklynProperties.remove("brooklyn.jclouds.cloudservers-uk.image-id");
         LocationRegistry locationRegistry = new LocationRegistry(brooklynProperties);
         JcloudsLocation jcloudsLocation = (JcloudsLocation) locationRegistry.resolve("cloudservers-uk");
 
         log.info("Creating Machine (to be shared with MySQL/Drupal). This can take a few minutes.");
         SshMachineLocation sshMachineLocation = jcloudsLocation.obtain();
         log.info("Finished creating Machine");
 
         log.info("Starting BasicDrupalApp");
         app.start(asList(sshMachineLocation));
         log.info("Finished creating BasicDrupalApp");
     }
}
