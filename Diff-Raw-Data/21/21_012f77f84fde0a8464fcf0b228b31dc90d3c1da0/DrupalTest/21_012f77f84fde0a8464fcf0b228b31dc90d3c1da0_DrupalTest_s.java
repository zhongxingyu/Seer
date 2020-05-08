 package io.cloudsoft.socialapps.drupal;
 
 
 import java.util.Arrays;
 import java.util.Map;
 
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import brooklyn.entity.basic.Entities;
 import brooklyn.entity.database.mysql.MySqlNode;
 import brooklyn.event.basic.DependentConfiguration;
 import brooklyn.location.Location;
 import brooklyn.management.ManagementContext;
 import brooklyn.test.HttpTestUtils;
 import brooklyn.test.entity.TestApplication;
 import brooklyn.util.MutableMap;
 
 public class DrupalTest {
 
     private Location location;
 
     private final static String SCRIPT = "create database drupal; " +
             "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, INDEX, ALTER, CREATE TEMPORARY TABLES, LOCK TABLES ON drupal.* TO 'drupal'@'localhost'  IDENTIFIED BY 'password'; " +
             "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, INDEX, ALTER, CREATE TEMPORARY TABLES, LOCK TABLES ON drupal.* TO 'drupal'@'127.0.0.1'  IDENTIFIED BY 'password'; " +
             "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, INDEX, ALTER, CREATE TEMPORARY TABLES, LOCK TABLES ON drupal.* TO 'drupal'@'%'  IDENTIFIED BY 'password';" +
             "FLUSH PRIVILEGES;";
     private TestApplication app;
 
     @BeforeMethod(alwaysRun = true)
     public void setUp() {
         app = new TestApplication();
     }
 
     @AfterMethod(alwaysRun = true)
     public void tearDown() {
         if (app != null) Entities.destroy(app);
     }
 
     @Test(groups = "Live")
     public void test() {
 
         Map mysqlConf = MutableMap.of("creationScriptContents", SCRIPT);
         MySqlNode mySqlNode = new MySqlNode(mysqlConf, app);
 
         Drupal  drupal = new Drupal(app);
         drupal.setConfig(Drupal.DATABASE_HOST, "127.0.0.1");
         drupal.setConfig(Drupal.DATABASE_SCHEMA, "drupal");
         drupal.setConfig(Drupal.DATABASE_USER, "drupal");
         drupal.setConfig(Drupal.DATABASE_PORT, DependentConfiguration.attributeWhenReady(mySqlNode, MySqlNode.MYSQL_PORT));
         drupal.setConfig(Drupal.DATABASE_PASSWORD, "password");
         drupal.setConfig(Drupal.ADMIN_EMAIL, "foo@bar.com");
         drupal.setConfig(Drupal.DATABASE_UP, DependentConfiguration.attributeWhenReady(mySqlNode, MySqlNode.SERVICE_UP));
 
         ManagementContext mgmt = Entities.startManagement(app);
 
         location = mgmt.getLocationRegistry().resolve("aws-ec2:us-east-1");
         app.start(Arrays.asList(location));
 
         HttpTestUtils.assertContentEventuallyContainsText("http://" + drupal.getAttribute(Drupal.HOSTNAME) + "/index.php", "Welcome");
     }
 }
 
