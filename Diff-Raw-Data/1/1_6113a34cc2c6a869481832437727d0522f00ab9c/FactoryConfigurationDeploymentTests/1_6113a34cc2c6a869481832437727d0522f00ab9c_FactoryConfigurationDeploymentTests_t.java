 /*
  * This file is part of the Eclipse Virgo project.
  *
  * Copyright (c) 2011 Chariot Solutions, LLC
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    dsklyut - initial contribution
  */
 
 package org.eclipse.virgo.kernel.deployer.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.Properties;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.eclipse.virgo.kernel.deployer.core.ApplicationDeployer;
 import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedServiceFactory;
 
 /**
  * Tests for Configuration artifacts that support ManagedServiceFactory
  *
  */
 public class FactoryConfigurationDeploymentTests extends AbstractDeployerIntegrationTest {
 
     private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
 
     private static String getTimestamp() {
         Calendar calendar = Calendar.getInstance();
         calendar.setTimeInMillis(System.currentTimeMillis());
         String timestamp = "[" + TIMESTAMP_FORMAT.format(calendar.getTime()) + "]";
         return timestamp;
     }
 
     private ServiceReference<ApplicationDeployer> appDeployerServiceReference;
 
     private ApplicationDeployer appDeployer;
 
     private ServiceReference<ConfigurationAdmin> configAdminServiceReference;
 
     private ConfigurationAdmin configAdmin;
 
     @Before
     public void setUp() throws Exception {
         this.appDeployerServiceReference = this.context.getServiceReference(ApplicationDeployer.class);
         this.appDeployer = this.context.getService(this.appDeployerServiceReference);
         this.configAdminServiceReference = this.context.getServiceReference(ConfigurationAdmin.class);
         this.configAdmin = this.context.getService(this.configAdminServiceReference);
     }
 
     @After
     public void tearDown() throws Exception {
         if (this.appDeployerServiceReference != null) {
             this.context.ungetService(this.appDeployerServiceReference);
         }
         if (this.configAdminServiceReference != null) {
             this.context.ungetService(this.configAdminServiceReference);
         }
     }
 
     @SuppressWarnings("rawtypes")
     private static class TestManagedServiceFactory implements ManagedServiceFactory {
 
         private volatile Dictionary properties;
 
         private final AtomicInteger updateCallCount = new AtomicInteger(0);
 
         private final AtomicInteger deleteCallCount = new AtomicInteger(0);
 
         @Override
         public String getName() {
             return "Test Managed Service Factory";
         }
 
         @Override
         public void updated(String pid, Dictionary properties) throws ConfigurationException {
             System.out.println(getTimestamp() + " updated pid '" + pid + "' with properties '" + properties + "'");
             this.updateCallCount.incrementAndGet();
             this.properties = properties;
         }
 
         @Override
         public void deleted(String pid) {
             this.deleteCallCount.incrementAndGet();
         }
 
         Dictionary getProperties() {
             return this.properties;
         }
 
         int updateCount() {
             return this.updateCallCount.get();
         }
 
         int deleteCount() {
             return this.deleteCallCount.get();
         }
     }
 
     @Test
     @SuppressWarnings("rawtypes")
     public void testSimpleDeployUndeployOfFactoryConfig() throws Exception {
 
         Hashtable<String, String> properties = new Hashtable<String, String>();
         properties.put(Constants.SERVICE_PID, "test.factory.pid.a");
         TestManagedServiceFactory service = new TestManagedServiceFactory();
         this.context.registerService(ManagedServiceFactory.class, service, properties);
 
         // make sure that we are starting off with a clean slate
         assertEquals(0, countFactoryConfigurations("test.factory.pid.a"));
 
         File configurationFile = new File("src/test/resources/configuration.deployment/factory-config-a.properties");
 
         DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(configurationFile.toURI());
         assertNotNull(deploymentIdentity);
 
         // let it deploy
         sleep(1000);
 
         assertEquals(1, countFactoryConfigurations("test.factory.pid.a"));
         assertEquals(1, service.updateCount());
         assertEquals(0, service.deleteCount());
         Dictionary propertiesFromService = service.getProperties();
         assertNotNull(propertiesFromService);
         assertEquals("prop1", propertiesFromService.get("prop1"));
         assertEquals("2", propertiesFromService.get("prop2"));
 
         this.appDeployer.undeploy(deploymentIdentity);
 
         // give time for events to percolate
         sleep(1000);
 
         assertEquals(0, countFactoryConfigurations("test.factory.pid.a"));
         assertEquals(1, service.updateCount());
         assertEquals(1, service.deleteCount());
 
         // now lets make sure that we can deploy it again
         deploymentIdentity = this.appDeployer.deploy(configurationFile.toURI());
         sleep(1000);
         assertEquals(1, countFactoryConfigurations("test.factory.pid.a"));
         assertEquals(2, service.updateCount());
         assertEquals(1, service.deleteCount());
 
         this.appDeployer.undeploy(deploymentIdentity);
     }
 
     @Test
     @SuppressWarnings("rawtypes")
     public void testHotDeployFactoryConfiguration() throws Exception {
 
         final String factoryPid = "test.factory.pid.hot";
         final Properties hotDeployConfiguration = new Properties();
         hotDeployConfiguration.setProperty(ConfigurationAdmin.SERVICE_FACTORYPID, factoryPid);
         hotDeployConfiguration.setProperty("prop1", "prop1");
         hotDeployConfiguration.setProperty("prop2", "2");
 
         File target = new File("target/pickup/factory-config-a-hot.properties");
 
         if (target.exists()) {
             assertTrue(target.delete());
         }
 
         try {
             Hashtable<String, String> properties = new Hashtable<String, String>();
             properties.put(Constants.SERVICE_PID, factoryPid);
             TestManagedServiceFactory service = new TestManagedServiceFactory();
             this.context.registerService(ManagedServiceFactory.class, service, properties);
 
             // make sure that we are starting off with a clean slate
             assertEquals(0, countFactoryConfigurations(factoryPid));
 
             // copy file to hot deploy location
             writePropertiesFile(hotDeployConfiguration, target, "no comment");
 
             ConfigurationTestUtils.pollUntilFactoryInConfigurationAdmin(this.configAdmin, factoryPid);
             assertEquals(1, countFactoryConfigurations(factoryPid));
             assertEquals(1, service.updateCount());
             assertEquals(0, service.deleteCount());
 
             Dictionary propertiesFromService = service.getProperties();
             assertNotNull(propertiesFromService);
             assertEquals("prop1", propertiesFromService.get("prop1"));
             assertEquals("2", propertiesFromService.get("prop2"));
 
             // remove the file and let it be removed
             target.delete();
             ConfigurationTestUtils.pollUntilFactoryNotInConfigurationAdmin(this.configAdmin, factoryPid);
 
             assertEquals(0, countFactoryConfigurations(factoryPid));
             assertEquals(1, service.updateCount());
             assertEquals(1, service.deleteCount());
         } finally {
             if (target.exists()) {
                 target.delete();
             }
         }
 
     }
 
     @Test
     @SuppressWarnings("rawtypes")
     public void testHotDeployWithUpdateFactoryConfiguration() throws Exception {
 
         final String factoryPid = "test.factory.pid.hot.update";
         final Properties hotDeployConfiguration = new Properties();
         hotDeployConfiguration.setProperty(ConfigurationAdmin.SERVICE_FACTORYPID, factoryPid);
         hotDeployConfiguration.setProperty("prop1", "prop1");
         hotDeployConfiguration.setProperty("prop2", "2");
 
         File target = new File("target/pickup/factory-config-a-hot-update.properties");
 
         if (target.exists()) {
             assertTrue(target.delete());
         }
 
         try {
 
             Hashtable<String, String> properties = new Hashtable<String, String>();
             properties.put(Constants.SERVICE_PID, factoryPid);
             TestManagedServiceFactory service = new TestManagedServiceFactory();
             this.context.registerService(ManagedServiceFactory.class, service, properties);
 
             // make sure that we are starting off with a clean slate
             assertEquals(0, countFactoryConfigurations(factoryPid));
 
             writePropertiesFile(hotDeployConfiguration, target, "initial");
 
             ConfigurationTestUtils.pollUntilFactoryInConfigurationAdmin(this.configAdmin, factoryPid);
             // let events propagate
             sleep(1000);
             assertEquals(1, countFactoryConfigurations(factoryPid));
             assertEquals(1, service.updateCount());
             assertEquals(0, service.deleteCount());
 
             Dictionary propertiesFromService = service.getProperties();
             assertNotNull(propertiesFromService);
             assertEquals("prop1", propertiesFromService.get("prop1"));
             assertEquals("2", propertiesFromService.get("prop2"));
 
             // update configuration
             hotDeployConfiguration.setProperty("prop2", "22");
             // save updated configuration
             writePropertiesFile(hotDeployConfiguration, target, "updated");
 
             // let events propagate and update happen
             sleep(2501);
             assertEquals(1, countFactoryConfigurations(factoryPid));
             assertEquals(2, service.updateCount()); // This takes several seconds to change from 1 to 2
             assertEquals(0, service.deleteCount());
 
             propertiesFromService = service.getProperties();
             assertNotNull(propertiesFromService);
             assertEquals("prop1", propertiesFromService.get("prop1"));
             assertEquals("22", propertiesFromService.get("prop2"));
 
             // remove the file and let it be removed
             target.delete();
             ConfigurationTestUtils.pollUntilFactoryNotInConfigurationAdmin(this.configAdmin, factoryPid);
 
             assertEquals(0, countFactoryConfigurations(factoryPid));
             assertEquals(2, service.updateCount());
             assertEquals(1, service.deleteCount());
         } finally {
             if (target.exists()) {
                 target.delete();
             }
         }
     }
     
     private static void sleep(long millis) throws InterruptedException {
         System.out.println(getTimestamp() + " entered sleep(" + millis + ")");
         Thread.sleep(millis);
         System.out.println(getTimestamp() + " exited sleep(" + millis + ")");
     }
 
     private void writePropertiesFile(final Properties hotDeployConfiguration, File target, String comment) throws IOException, FileNotFoundException {
         FileOutputStream stream = new FileOutputStream(target);
         hotDeployConfiguration.store(stream, comment);
        stream.flush();
         stream.close();
     }
 
     @Test
     @SuppressWarnings("rawtypes")
     public void testHotDeployMultipleFactoryConfiguration() throws Exception {
 
         final String factoryPid = "test.factory.pid.hot.multiple";
 
         final Properties configOne = new Properties();
         configOne.setProperty(ConfigurationAdmin.SERVICE_FACTORYPID, factoryPid);
         configOne.setProperty("prop1", "prop1");
         configOne.setProperty("prop2", "1");
 
         final Properties configTwo = new Properties();
         configTwo.setProperty(ConfigurationAdmin.SERVICE_FACTORYPID, factoryPid);
         configTwo.setProperty("prop1", "prop2");
         configTwo.setProperty("prop2", "2");
 
         final File targetOne = new File("target/pickup/factory-config-a-hot-update-1.properties");
         final File targetTwo = new File("target/pickup/factory-config-a-hot-update-2.properties");
 
         if (targetOne.exists()) {
             assertTrue(targetOne.delete());
         }
         if (targetTwo.exists()) {
             assertTrue(targetTwo.delete());
         }
 
         try {
 
             Hashtable<String, String> properties = new Hashtable<String, String>();
             properties.put(Constants.SERVICE_PID, factoryPid);
             TestManagedServiceFactory service = new TestManagedServiceFactory();
             this.context.registerService(ManagedServiceFactory.class, service, properties);
 
             // make sure that we are starting off with a clean slate
             assertEquals(0, countFactoryConfigurations(factoryPid));
 
             // copy file to hot deploy location
             writePropertiesFile(configOne, targetOne, "initial");
 
             ConfigurationTestUtils.pollUntilFactoryInConfigurationAdmin(this.configAdmin, factoryPid);
             // let events propagate
             sleep(100);
             assertEquals(1, countFactoryConfigurations(factoryPid));
             assertEquals(1, service.updateCount());
             assertEquals(0, service.deleteCount());
 
             // validate first configuration
             Dictionary propertiesFromService = service.getProperties();
             assertNotNull(propertiesFromService);
             assertEquals("prop1", propertiesFromService.get("prop1"));
             assertEquals("1", propertiesFromService.get("prop2"));
 
             writePropertiesFile(configTwo, targetTwo, "initial");
             sleep(2502);
             assertEquals(2, countFactoryConfigurations(factoryPid));
             assertEquals(2, service.updateCount());
             assertEquals(0, service.deleteCount());
 
             propertiesFromService = service.getProperties();
             assertNotNull(propertiesFromService);
             assertEquals("prop2", propertiesFromService.get("prop1"));
             assertEquals("2", propertiesFromService.get("prop2"));
 
             assertTrue(targetOne.delete());
             assertTrue(targetTwo.delete());
 
             // let events propagate and update happen
             ConfigurationTestUtils.pollUntilFactoryNotInConfigurationAdmin(this.configAdmin, factoryPid);
             assertEquals(0, countFactoryConfigurations(factoryPid));
             assertEquals(2, service.updateCount());
             assertEquals(2, service.deleteCount());
 
         } finally {
             if (targetOne.exists()) {
                 targetOne.delete();
             }
             if (targetTwo.exists()) {
                 targetTwo.delete();
             }
         }
     }
 
     private int countFactoryConfigurations(String factoryPid) throws Exception {
         Configuration[] configurations = this.configAdmin.listConfigurations(null);
         int counter = 0;
         for (Configuration c : configurations) {
             if (factoryPid.equals(c.getFactoryPid())) {
                 counter++;
             }
         }
         return counter;
     }
 }
