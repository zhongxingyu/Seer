 package io.cloudsoft.socialapps.wordpress.examples;
 
 import io.cloudsoft.socialapps.wordpress.Wordpress;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import brooklyn.catalog.Catalog;
 import brooklyn.config.BrooklynProperties;
 import brooklyn.entity.basic.ApplicationBuilder;
 import brooklyn.entity.basic.Entities;
 import brooklyn.entity.basic.StartableApplication;
 import brooklyn.entity.database.mysql.MySqlNode;
 import brooklyn.entity.proxying.BasicEntitySpec;
 import brooklyn.event.basic.DependentConfiguration;
 import brooklyn.launcher.BrooklynLauncher;
 import brooklyn.launcher.BrooklynServerDetails;
 import brooklyn.location.Location;
 import brooklyn.util.CommandLineUtil;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 
 @Catalog(name="Simple WordPress", 
         description="WordPress - the free and open source blogging tool and a content management system",
        iconUrl="classpath:///io/cloudsoft/socialapps/wordpress/wordpress-logo-notext-rgb.png")
 public class BasicWordpressApp extends ApplicationBuilder {
     
     // TODO Currently only works on CentOS or RHEL
     
     public static final Logger log = LoggerFactory.getLogger(BasicWordpressApp.class);
 
     final static String SCRIPT = "create database wordpress; " +
             "grant all privileges on wordpress.* TO 'wordpress'@'localhost'  IDENTIFIED BY 'password'; " +
             "grant all privileges on wordpress.* TO 'wordpress'@'127.0.0.1'  IDENTIFIED BY 'password'; " +
             "grant all privileges on wordpress.* TO 'wordpress'@'%'  IDENTIFIED BY 'password';" +
             "flush privileges;";
     
     private MySqlNode mysql;
     private Wordpress wordpress;
 
     @Override
     protected void doBuild() {
         mysql = createChild(BasicEntitySpec.newInstance(MySqlNode.class)
                 .configure("creationScriptContents", SCRIPT));
 
         wordpress = createChild(BasicEntitySpec.newInstance(Wordpress.class)
                 .configure(Wordpress.DATABASE_UP, DependentConfiguration.attributeWhenReady(mysql, MySqlNode.SERVICE_UP))
                 .configure(Wordpress.DATABASE_HOSTNAME, DependentConfiguration.attributeWhenReady(mysql, MySqlNode.HOSTNAME))
                 .configure(Wordpress.DATABASE_NAME, "wordpress")
                 .configure(Wordpress.DATABASE_USER, "wordpress")
                 .configure(Wordpress.DATABASE_PASSWORD, "password")
                 .configure(Wordpress.WEBLOG_TITLE, "my custom title")
                 .configure(Wordpress.WEBLOG_ADMIN_EMAIL, "aled.sage@gmail.com"));
     }
 
     public static void main(String[] argv) throws Exception {
         List<String> args = Lists.newArrayList(argv);
         String port =  CommandLineUtil.getCommandLineOption(args, "--port", "8081+");
         String location = CommandLineUtil.getCommandLineOption(args, "--location", "aws-ec2:us-east-1");
 
         // Image: {id=us-east-1/ami-7d7bfc14, providerId=ami-7d7bfc14, name=RightImage_CentOS_6.3_x64_v5.8.8.5, location={scope=REGION, id=us-east-1, description=us-east-1, parent=aws-ec2, iso3166Codes=[US-VA]}, os={family=centos, arch=paravirtual, version=6.0, description=rightscale-us-east/RightImage_CentOS_6.3_x64_v5.8.8.5.manifest.xml, is64Bit=true}, description=rightscale-us-east/RightImage_CentOS_6.3_x64_v5.8.8.5.manifest.xml, version=5.8.8.5, status=AVAILABLE[available], loginUser=root, userMetadata={owner=411009282317, rootDeviceType=instance-store, virtualizationType=paravirtual, hypervisor=xen}}
         BrooklynProperties brooklynProperties = BrooklynProperties.Factory.newDefault();
         brooklynProperties.put("brooklyn.jclouds.aws-ec2.image-id", "us-east-1/ami-7d7bfc14");
         
         BrooklynServerDetails server = BrooklynLauncher.newLauncher()
                 .brooklynProperties(brooklynProperties)
                 .webconsolePort(port)
                 .launch();
 
         Location loc = server.getManagementContext().getLocationRegistry().resolve(location);
 
         StartableApplication app = new BasicWordpressApp()
                 .appDisplayName("Simple wordpress app")
                 .manage(server.getManagementContext());
         
         app.start(ImmutableList.of(loc));
         
         Entities.dumpInfo(app);
     }
 }
