 package io.cloudsoft.socialapps.drupal;
 
 
 import static brooklyn.entity.basic.lifecycle.CommonCommands.installPackage;
 import static brooklyn.entity.basic.lifecycle.CommonCommands.sudo;
 import static com.google.common.collect.ImmutableMap.of;
 import static java.lang.String.format;
import static java.util.Arrays.asList;
 
 import java.io.ByteArrayInputStream;
 import java.util.LinkedList;
 import java.util.List;
 
 import brooklyn.entity.basic.AbstractSoftwareProcessSshDriver;
 import brooklyn.entity.basic.Attributes;
 import brooklyn.entity.basic.lifecycle.CommonCommands;
 import brooklyn.entity.webapp.WebAppService;
 import brooklyn.location.basic.SshMachineLocation;
 import brooklyn.util.ResourceUtils;
 
 public class DrupalSshDriver extends AbstractSoftwareProcessSshDriver implements DrupalDriver {
 
     public DrupalSshDriver(Drupal entity, SshMachineLocation machine) {
         super(entity, machine);
     }
 
     @Override
     public void install() {
         //this call lets drupal wait till the database completes
         log.info("Waiting till the database is up");
         entity.getConfig(Drupal.DATABASE_UP);
         log.info("Database is up and running");
 
         String version = getVersion();
 
        //configures postfix so we can do a headless install.
        getLocation().execCommands("creating postfix config",asList(sudo("mkdir /etc/postfix")));
        String postfixConfig = new ResourceUtils(DrupalSshDriver.class).getResourceAsString("classpath://io/cloudsoft/socialapps/drupal/main.cf");
        getLocation().copyTo(new ByteArrayInputStream(postfixConfig.getBytes()), "/etc/postfix/main.cf");

         List<String> commands = new LinkedList<String>();
         commands.add(CommonCommands.INSTALL_TAR);
         commands.add(CommonCommands.INSTALL_WGET);
         commands.add(installPackage(of("yum", "httpd", "apt", "apache2"), null));
         commands.add(installPackage(of("yum", "php", "apt", "php5"), null));
         commands.add(installPackage(of("yum", "php-mysql", "apt", "php5-mysql"), null));
         commands.add(installPackage(of("yum", "php-gd", "apt", "php5-gd"), null));
         commands.add(installPackage(of("apt", "libapache2-mod-php5"), null));
         commands.add(sudo("/etc/init.d/apache2 stop"));
         commands.add(format("wget http://ftp.drupal.org/files/projects/drupal-%s.tar.gz", version));
         commands.add(format("tar -xvf drupal-%s.tar.gz", version));
         commands.add(sudo(format("cp -R drupal-%s/*  /var/www/", version)));
         commands.add("cd /var/www");
         commands.add(sudo("cp sites/default/default.settings.php sites/default/settings.php"));
         commands.add(sudo("mkdir -p /var/www/sites/default/files"));
         commands.add(sudo("chmod o+w sites/default/settings.php"));
         commands.add(sudo("chmod o+w sites/default"));
         commands.add(sudo("chown -R www-data.www-data /var/www"));
         commands.add(sudo("chgrp -R www-data /var/www/sites/default/files"));
         commands.add(sudo("chmod -R g+u /var/www/sites/default/files"));
         commands.add(sudo("rm index.html"));
        commands.add("set HOSTNAME = 'hostname'");
        commands.add(sudo("sed -i.bk s/HOST_NAME/$HOSTNAME/g /etc/postfix/main.cf"));
        commands.add("set DEBIAN_FRONTEND='noninteractive'");
        commands.add(installPackage(of("apt","postfix"),null));

 
         newScript(INSTALLING).
                 failOnNonZeroResultCode().
                 body.append(commands).execute();
     }
 
     @Override
     public void customize() {
         String setupDrupalScript = new ResourceUtils(DrupalSshDriver.class).getResourceAsString("classpath://io/cloudsoft/socialapps/drupal/setup-drupal.php");
         setupDrupalScript = setupDrupalScript.replaceAll("\\$site_name", entity.getConfig(Drupal.SITE_NAME));
         setupDrupalScript = setupDrupalScript.replaceAll("\\$site_mail", entity.getConfig(Drupal.SITE_MAIL));
         setupDrupalScript = setupDrupalScript.replaceAll("\\$admin_name", entity.getConfig(Drupal.ADMIN_NAME));
         setupDrupalScript = setupDrupalScript.replaceAll("\\$admin_email", entity.getConfig(Drupal.ADMIN_EMAIL));
         setupDrupalScript = setupDrupalScript.replaceAll("\\$admin_password", entity.getConfig(Drupal.ADMIN_PASSWORD));
         setupDrupalScript = setupDrupalScript.replaceAll("\\$database_port", "" + entity.getConfig(Drupal.DATABASE_PORT));
         setupDrupalScript = setupDrupalScript.replaceAll("\\$database_schema", entity.getConfig(Drupal.DATABASE_SCHEMA));
         setupDrupalScript = setupDrupalScript.replaceAll("\\$database_user", entity.getConfig(Drupal.DATABASE_USER));
         setupDrupalScript = setupDrupalScript.replaceAll("\\$database_password", entity.getConfig(Drupal.DATABASE_PASSWORD));
         setupDrupalScript = setupDrupalScript.replaceAll("\\$database_host", entity.getConfig(Drupal.DATABASE_HOST));
         setupDrupalScript = setupDrupalScript.replaceAll("\\$database_driver", entity.getConfig(Drupal.DATABASE_DRIVER));
 
         getLocation().copyTo(new ByteArrayInputStream(setupDrupalScript.getBytes()), "/tmp/setup-drupal.php");
 
         List<String> commands = new LinkedList<String>();
         commands.add("cd /var/www");
         commands.add(sudo("cp /tmp/setup-drupal.php ."));
         commands.add(sudo("php setup-drupal.php"));
         commands.add(sudo("rm setup-drupal.php"));
 
         newScript(CUSTOMIZING).
                 failOnNonZeroResultCode().
                 body.append(commands).execute();
     }
 
     @Override
     public void launch() {
         List<String> commands = new LinkedList<String>();
         commands.add(sudo("/etc/init.d/apache2 start"));
 
         newScript(LAUNCHING).
                 failOnNonZeroResultCode().
                 body.append(commands).execute();
         
         entity.setAttribute(WebAppService.HTTP_PORT, 80);
         entity.setAttribute(WebAppService.ROOT_URL, "http://"+entity.getAttribute(Attributes.HOSTNAME)+"/");
     }
 
     @Override
     public boolean isRunning() {
         List<String> commands = new LinkedList<String>();
         commands.add(sudo("/etc/init.d/apache2 status"));
 
         return newScript(CHECK_RUNNING).
                 body.append(commands).execute() == 0;
     }
 
     @Override
     public void stop() {
         List<String> commands = new LinkedList<String>();
         commands.add(sudo("/etc/init.d/apache2 stop"));
 
         newScript(STOPPING).
                 failOnNonZeroResultCode().
                 body.append(commands).execute();
     }
 }
