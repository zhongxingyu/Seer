 package io.cloudsoft.socialapps.drupal.examples;
 
 import static brooklyn.event.basic.DependentConfiguration.attributeWhenReady;
 import io.cloudsoft.socialapps.drupal.Drupal;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import brooklyn.config.BrooklynProperties;
 import brooklyn.entity.basic.ApplicationBuilder;
 import brooklyn.entity.basic.Entities;
 import brooklyn.entity.basic.StartableApplication;
 import brooklyn.entity.database.mysql.MySqlNode;
 import brooklyn.entity.proxying.BasicEntitySpec;
 import brooklyn.launcher.BrooklynLauncher;
 import brooklyn.launcher.BrooklynServerDetails;
 import brooklyn.location.Location;
 import brooklyn.util.CommandLineUtil;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 
 /**
  * This example Application starts up a single Ubuntu machine in Amazon EC2 that runs both Drupal and MySQL.
  * <p/>
  * To open the Brooklyn WebConsole open: http://localhost:8081 and login with admin/password.
  */
 public class BasicDrupalApp extends ApplicationBuilder {
 
     public static final Logger log = LoggerFactory.getLogger(BasicDrupalApp.class);
 
     private final static String SCRIPT = "create database drupal; " +
            "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, INDEX, ALTER, CREATE TEMPORARY TABLES, LOCK TABLES ON drupal.* TO 'drupal'@'127.0.0.1'  IDENTIFIED BY 'password'; " +
             "FLUSH PRIVILEGES;";
 
     private Drupal drupal;
     private MySqlNode mySqlNode;
 
     @Override
     protected void doBuild() {
        createChild(BasicEntitySpec.newInstance(MySqlNode.class)
                 .configure(MySqlNode.CREATION_SCRIPT_CONTENTS, SCRIPT));
 
        createChild(BasicEntitySpec.newInstance(Drupal.class)
                 .configure(Drupal.DATABASE_UP, attributeWhenReady(mySqlNode, MySqlNode.SERVICE_UP))
                 .configure(Drupal.DATABASE_HOST, attributeWhenReady(mySqlNode, MySqlNode.HOSTNAME))
                 .configure(Drupal.DATABASE_PORT, attributeWhenReady(mySqlNode, MySqlNode.MYSQL_PORT))
                 .configure(Drupal.DATABASE_SCHEMA, "drupal")
                 .configure(Drupal.DATABASE_USER, "drupal")
                 .configure(Drupal.DATABASE_PASSWORD, "password")
                .configure(Drupal.ADMIN_EMAIL, "foo@bar.com"));
     }
 
     // can start in AWS by running this -- or use brooklyn CLI/REST for most clouds, or programmatic/config for set of fixed IP machines
     public static void main(String[] argv) throws Exception {
         List<String> args = Lists.newArrayList(argv);
         String port =  CommandLineUtil.getCommandLineOption(args, "--port", "8081+");
         String location = CommandLineUtil.getCommandLineOption(args, "--location", "cloudservers-uk");
 
         BrooklynProperties brooklynProperties = BrooklynProperties.Factory.newDefault();
         //brooklynProperties.put("brooklyn.jclouds.aws-ec2.image-name-regex","ubuntu-oneiric");
         brooklynProperties.put("brooklyn.jclouds.cloudservers-uk.image-name-regex", "Debian");
         brooklynProperties.remove("brooklyn.jclouds.cloudservers-uk.image-id");
         
         BrooklynServerDetails server = BrooklynLauncher.newLauncher()
                 .brooklynProperties(brooklynProperties)
                 .webconsolePort(port)
                 .launch();
 
         Location loc = server.getManagementContext().getLocationRegistry().resolve(location);
 
         StartableApplication app = new BasicDrupalApp()
                 .appDisplayName("Simple drupal app")
                 .manage(server.getManagementContext());
         
         app.start(ImmutableList.of(loc));
         
         Entities.dumpInfo(app);
     }
 }
