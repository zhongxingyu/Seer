 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
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
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Set;
 
 import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
 import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
 import org.eclipse.virgo.kernel.deployer.test.util.ArtifactLifecycleEvent;
 import org.eclipse.virgo.kernel.deployer.test.util.ArtifactListener;
 import org.eclipse.virgo.kernel.deployer.test.util.TestLifecycleEvent;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;
 import org.eclipse.virgo.util.math.Sets;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.Version;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 
 public class PlanDeploymentTests extends AbstractDeployerIntegrationTest {
 
     private ServiceReference<ConfigurationAdmin> configAdminServiceReference;
 
     private ConfigurationAdmin configAdmin;
 
     private static final String PLAN_SYMBOLIC_NAME_BUG336200 = "bug336200.plan";
 
     private static final String BUG336200_LAZY_BSN = "lazyBundle";
 
     private static final Version VERSION_BUG336200 = new Version(1, 0, 0);
 
     private final ArtifactListener artifactListener = new ArtifactListener();
 
     @Test
     public void planReferencingAPar() throws Exception {
         testPlanDeployment(new File("src/test/resources/test-with-par.plan"), null, "par-deployed-by-plan-1-one");
     }
 
     @Test(expected = DeploymentException.class)
     public void scopedPlanReferencingAPar() throws Exception {
         testPlanDeployment(new File("src/test/resources/scoped-test-with-par.plan"), null);
     }
 
     @Test
     public void scopedBundlesAndConfig() throws Exception {
         String oneBsn = "simple.bundle.one";
         String twoBsn = "simple.bundle.two";
 
         testPlanDeployment(new File("src/test/resources/test.plan"), new File("src/test/resources/plan-deployment/com.foo.bar.properties"), oneBsn,
             twoBsn);
     }
 
     @Test
     public void testSimpleBundleWithFragment() throws Exception {
         String oneBsn = "simple.bundle.one";
         String twoBsn = "simple.fragment.one";
 
         testPlanDeployment(new File("src/test/resources/fragment.plan"), null, oneBsn, twoBsn);
     }
 
     @Test
     public void testUnscopedNonAtomicPlan() throws Exception {
         String oneBsn = "simple.bundle.one";
         String twoBsn = "simple.bundle.two";
 
         testPlanDeployment(new File("src/test/resources/testunscopednonatomic.plan"), new File(
             "src/test/resources/plan-deployment/com.foo.bar.properties"), oneBsn, twoBsn);
     }
 
     @Test
     public void testPlanWithProperties() throws Exception {
         this.deployer.deploy(new File("src/test/resources/properties.plan").toURI());
         Bundle[] bundles = this.context.getBundles();
         boolean found = false;
         for (Bundle bundle : bundles) {
             if ("bundle.properties".equals(bundle.getSymbolicName())) {
                 found = true;
                 assertEquals("foo", bundle.getHeaders().get("Test-Header"));
             }
         }
         assertTrue(found);
     }
 
     @Test
     public void testUnscopedAtomicPlanStartingEventsDuringDeployment() throws Exception {
         testPlanDeploymentStartingEvents(new File("src/test/resources/bug336200.plan"), BUG336200_LAZY_BSN);
     }
 
     @Before
     public void setUp() throws Exception {
         this.configAdminServiceReference = this.context.getServiceReference(ConfigurationAdmin.class);
         this.configAdmin = this.context.getService(this.configAdminServiceReference);
         this.context.registerService(InstallArtifactLifecycleListener.class.getName(), artifactListener, null);
     }
 
     private void testPlanDeployment(File plan, File propertiesFile, String... candidateBsns) throws Exception {
         Bundle[] beforeDeployBundles = this.context.getBundles();
         assertBundlesNotInstalled(beforeDeployBundles, candidateBsns);
 
         DeploymentIdentity deploymentIdentity = this.deployer.deploy(plan.toURI());
         Bundle[] afterDeployBundles = this.context.getBundles();
         assertBundlesInstalled(afterDeployBundles, candidateBsns);
 
         String pid = null;
 
         if (propertiesFile != null) {
             pid = propertiesFile.getName().substring(0, propertiesFile.getName().length() - ".properties".length());
             checkConfigAvailable(pid, propertiesFile);
         }
 
         this.deployer.undeploy(deploymentIdentity);
         Bundle[] afterUndeployBundles = this.context.getBundles();
         assertBundlesNotInstalled(afterUndeployBundles, candidateBsns);
 
         if (propertiesFile != null) {
             checkConfigUnavailable(pid);
         }
     }
 
     private void testPlanDeploymentStartingEvents(File plan, String... candidateBsns) throws Exception {
 
         this.artifactListener.clear();
 
         Set<ArtifactLifecycleEvent> expectedEventSet = new HashSet<ArtifactLifecycleEvent>();
         // events expected due to explicit refresh;
         expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.RESOLVING, "plan", PLAN_SYMBOLIC_NAME_BUG336200, VERSION_BUG336200));
        // Following no longer expected. Spring DM to Gemini Blueprint upgrade affects lazy bundle behaviour.
        //expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.RESOLVED, "plan", PLAN_SYMBOLIC_NAME_BUG336200, VERSION_BUG336200));
         expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.INSTALLING, "plan", PLAN_SYMBOLIC_NAME_BUG336200, VERSION_BUG336200));
         expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.INSTALLED, "plan", PLAN_SYMBOLIC_NAME_BUG336200, VERSION_BUG336200));
         expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STARTING, "plan", PLAN_SYMBOLIC_NAME_BUG336200, VERSION_BUG336200));
         expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STARTED, "plan", PLAN_SYMBOLIC_NAME_BUG336200, VERSION_BUG336200));
         
         expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.RESOLVING, "bundle", BUG336200_LAZY_BSN, VERSION_BUG336200));
         expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.RESOLVED, "bundle", BUG336200_LAZY_BSN, VERSION_BUG336200));
         expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.INSTALLING, "bundle", BUG336200_LAZY_BSN, VERSION_BUG336200));
         expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.INSTALLED, "bundle", BUG336200_LAZY_BSN, VERSION_BUG336200));
         expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STARTING, "bundle", BUG336200_LAZY_BSN, VERSION_BUG336200));
         expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STARTED, "bundle", BUG336200_LAZY_BSN, VERSION_BUG336200));
  
         Bundle[] beforeDeployBundles = this.context.getBundles();
         assertBundlesNotInstalled(beforeDeployBundles, candidateBsns);
         Thread startBundleThread = new Thread(new StartBundleRunnable(this.context));
         startBundleThread.start();
         
         DeploymentIdentity deploymentIdentity = this.deployer.deploy(plan.toURI());
         waitForAndCheckEventsReceived(expectedEventSet, 10000L);
 
         Bundle[] afterDeployBundles = this.context.getBundles();
         assertBundlesInstalled(afterDeployBundles, candidateBsns);
 
         this.deployer.undeploy(deploymentIdentity);
         Bundle[] afterUndeployBundles = this.context.getBundles();
         assertBundlesNotInstalled(afterUndeployBundles, candidateBsns);
     }
 
     private class StartBundleRunnable implements Runnable {
 
         BundleContext context = null;
 
         public StartBundleRunnable(BundleContext bundleContext) {
             this.context = bundleContext;
         }
 
         @Override
         public void run() {
             try {
                 waitAndStartLazyBundle();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             } catch (BundleException e) {
                 e.printStackTrace();
             }
 
         }
 
         private void waitAndStartLazyBundle() throws InterruptedException, BundleException {
             Thread.sleep(2500);
 
             for (Bundle bundle : this.context.getBundles()) {
                 if (bundle.getSymbolicName().equals(BUG336200_LAZY_BSN)) {
                     bundle.start();
                 }
             }
         }
 
     }
 
     private void waitForAndCheckEventsReceived(Set<ArtifactLifecycleEvent> expectedEventSet, long timeout) {
         this.artifactListener.waitForEvents(expectedEventSet, timeout);
         
         Set<ArtifactLifecycleEvent> actualEventSet = new HashSet<ArtifactLifecycleEvent>(this.artifactListener.extract());
 
         Set<ArtifactLifecycleEvent> extraEvents = Sets.difference(actualEventSet, expectedEventSet);
         Set<ArtifactLifecycleEvent> missingEvents = Sets.difference(expectedEventSet, actualEventSet);
 
         assertTrue("More events were received than expected: " + extraEvents, extraEvents.isEmpty());
         assertTrue("There were missing events: " + missingEvents, missingEvents.isEmpty());
         
         List<ArtifactLifecycleEvent> actualEventSetList = this.artifactListener.extract();
         ArtifactLifecycleEvent planStartingEvent = new ArtifactLifecycleEvent(TestLifecycleEvent.STARTING, "bundle", BUG336200_LAZY_BSN, VERSION_BUG336200);
         assertTrue("More than one STARTING event received for the atomic plan.", actualEventSetList.indexOf(planStartingEvent) == actualEventSetList.lastIndexOf(planStartingEvent));
     }
 
     static void assertBundlesNotInstalled(Bundle[] bundles, String... candidateBsns) {
         List<String> installedBsns = getInstalledBsns(bundles);
         for (String candidateBsn : candidateBsns) {
             for (String installedBsn : installedBsns) {
                 if (installedBsn.contains(candidateBsn)) {
                     fail(candidateBsn + " was installed");
                 }
             }
         }
     }
 
     @SuppressWarnings("unchecked")
     private void checkConfigAvailable(String pid, File propertiesFile) throws IOException {
         Configuration configuration = this.configAdmin.getConfiguration(pid, null);
         Dictionary<Object, Object> dictionary = configuration.getProperties();
 
         Properties properties = new Properties();
         properties.load(new FileReader(propertiesFile));
 
         Set<Entry<Object, Object>> entrySet = properties.entrySet();
 
         for (Entry<Object, Object> entry : entrySet) {
             Assert.assertEquals(entry.getValue(), dictionary.get(entry.getKey()));
         }
 
         Assert.assertEquals(pid, dictionary.get("service.pid"));
     }
 
     private void checkConfigUnavailable(String pid) throws IOException {
         Configuration configuration = this.configAdmin.getConfiguration(pid, null);
         Assert.assertNull(configuration.getProperties());
     }
 
     static void assertBundlesInstalled(Bundle[] bundles, String... candidateBsns) {
         List<String> installedBsns = getInstalledBsns(bundles);
         for (String candidateBsn : candidateBsns) {
             boolean found = false;
             for (String installedBsn : installedBsns) {
                 if (installedBsn.contains(candidateBsn)) {
                     found = true;
                 }
             }
             assertTrue(candidateBsn + " was not installed", found);
         }
     }
 
     static List<String> getInstalledBsns(Bundle[] bundles) {
         List<String> installedBsns = new ArrayList<String>(bundles.length);
         for (Bundle bundle : bundles) {
             installedBsns.add(bundle.getSymbolicName());
         }
 
         return installedBsns;
     }
 
 }
