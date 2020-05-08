 /*******************************************************************************
  * Copyright (c) 2011 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.deployer.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 
 import javax.management.MBeanServerConnection;
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 import javax.management.openmbean.CompositeDataSupport;
 import javax.management.openmbean.TabularDataSupport;
 
 import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
 import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.osgi.framework.ServiceReference;
 
 /**
  * Test deployed artifacts show up correctly in JMX, in both the older Model and the newer, region-aware ArtifactModel.
  * 
  */
 public class JmxArtifactModelTests extends AbstractDeployerIntegrationTest {
 
     private static final String GLOBAL_USER_REGION = "global";
 
     private static final String CONFIGURATION_TYPE = "configuration";
 
     private static final String CONFIGURATION_NAME = "t";
 
     private static final String CONFIGURATION_VERSION = "0.0.0";
 
     private ServiceReference<ApplicationDeployer> appDeployerServiceReference;
 
     private ApplicationDeployer appDeployer;
 
     private MBeanServerConnection mBeanServerConnection;
 
     @Before
     public void setUp() throws Exception {
         this.appDeployerServiceReference = this.context.getServiceReference(ApplicationDeployer.class);
         this.appDeployer = (ApplicationDeployer) this.context.getService(this.appDeployerServiceReference);
         this.mBeanServerConnection = getMBeanServerConnection();
     }
 
     @After
     public void tearDown() throws Exception {
         if (this.appDeployerServiceReference != null) {
             this.context.ungetService(this.appDeployerServiceReference);
         }
     }
     
     @Test
     public void testConfigModel() throws Exception {
         File file = new File("src/test/resources/configuration.deployment/t.properties");
 
         DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(file.toURI());
 
         assertDeploymentIdentityEquals(deploymentIdentity, "t.properties", CONFIGURATION_TYPE, CONFIGURATION_NAME, "0");
 
         assertArtifactState(CONFIGURATION_TYPE, CONFIGURATION_NAME, CONFIGURATION_VERSION, "ACTIVE");
 
         TabularDataSupport attribute = (TabularDataSupport) this.mBeanServerConnection.getAttribute(
             getObjectName(CONFIGURATION_TYPE, CONFIGURATION_NAME, CONFIGURATION_VERSION), "Properties");
         assertEquals("b", getStringValue(attribute, "a"));
         assertEquals("d", getStringValue(attribute, "c"));
 
         this.appDeployer.undeploy(deploymentIdentity);
     }
 
     @Test
     public void testConfigArtifactModel() throws Exception {
         File file = new File("src/test/resources/configuration.deployment/t.properties");
 
         DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(file.toURI());
 
         assertDeploymentIdentityEquals(deploymentIdentity, "t.properties", CONFIGURATION_TYPE, CONFIGURATION_NAME, "0");
 
         assertArtifactState(GLOBAL_USER_REGION, CONFIGURATION_TYPE, CONFIGURATION_NAME, CONFIGURATION_VERSION, "ACTIVE");
 
         TabularDataSupport attribute = (TabularDataSupport) this.mBeanServerConnection.getAttribute(
             getObjectName(GLOBAL_USER_REGION, CONFIGURATION_TYPE, CONFIGURATION_NAME, CONFIGURATION_VERSION), "Properties");
         assertEquals("b", getStringValue(attribute, "a"));
         assertEquals("d", getStringValue(attribute, "c"));
 
         this.appDeployer.undeploy(deploymentIdentity);
 
     }
     
     //TODO: test other artefact types
 
     private String getStringValue(TabularDataSupport attribute, String key) {
         Object[] keys = { key };
         CompositeDataSupport cds = (CompositeDataSupport) attribute.get(keys);
         return (String) cds.get("value");
     }
 
     private void assertArtifactState(String type, String name, String version, String state) throws MalformedObjectNameException, IOException,
         Exception {
         assertArtifactExists(type, name, version);
         assertEquals(String.format("Artifact %s:%s:%s is not in state %s", type, name, version, state), state,
             this.mBeanServerConnection.getAttribute(getObjectName(type, name, version), "State"));
     }
 
     private void assertArtifactExists(String type, String name, String version) throws IOException, Exception, MalformedObjectNameException {
         assertTrue(String.format("Artifact %s:%s:%s does not exist", type, name, version),
             this.mBeanServerConnection.isRegistered(getObjectName(type, name, version)));
     }
 
     protected static ObjectName getObjectName(String type, String name, String version) throws MalformedObjectNameException {
        return new ObjectName(String.format("org.eclipse.virgo.kernel:type=ArtifactModel,artifact-type=%s,name=%s,version=%s", type, name, version));
     }
 
     private void assertArtifactState(String region, String type, String name, String version, String state) throws MalformedObjectNameException,
         IOException, Exception {
         assertArtifactExists(region, type, name, version);
         assertEquals(String.format("Artifact %s:%s:%s:%s is not in state %s", region, type, name, version, state), state,
             this.mBeanServerConnection.getAttribute(getObjectName(region, type, name, version), "State"));
     }
 
     private void assertArtifactExists(String region, String type, String name, String version) throws IOException, Exception, MalformedObjectNameException {
         assertTrue(String.format("Artifact %s:%s:%s:%s does not exist", region, type, name, version),
             this.mBeanServerConnection.isRegistered(getObjectName(region, type, name, version)));
     }
 
     protected static ObjectName getObjectName(String region, String type, String name, String version) throws MalformedObjectNameException {
         return new ObjectName(String.format("org.eclipse.virgo.kernel:type=ArtifactModel,region=%s,artifact-type=%s,name=%s,version=%s", region,
             type, name, version));
     }
 
     private static MBeanServerConnection getMBeanServerConnection() throws Exception {
         return ManagementFactory.getPlatformMBeanServer();
     }
 
 }
