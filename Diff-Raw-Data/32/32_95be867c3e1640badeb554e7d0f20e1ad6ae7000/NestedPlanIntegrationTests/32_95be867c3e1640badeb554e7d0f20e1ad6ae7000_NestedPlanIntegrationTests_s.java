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
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
 import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
 import org.eclipse.virgo.kernel.model.Artifact;
 import org.eclipse.virgo.kernel.model.ArtifactState;
 import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
 import org.eclipse.equinox.region.Region;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.Version;
 import org.osgi.service.packageadmin.ExportedPackage;
 
 
 /**
  * These tests cover parent and child plans in all combinations of scoped/unscoped and atomic/non-atomic.
  * <p/>
  * A bundle "global" exports the package "global". A bundle "parent" in the parent plans exports the package "parent"
  * imports the bundle "global" with application import scope. A bundle "child" in the child plans exports the package
  * "child".
  * <p/>
  * An inner class called {@link Model} encapsulates how the various combinations should work in terms of scoping and
  * atomicity.
  * <p/>
  * When scoping occurs, the tests check the synthetic context bundle and the promotion of the "global" package when
  * appropriate. The lifecycle dependence or independence is checked between the bundles "parent" and "child" and between
  * the parent and child plans.
  * 
  */
 @SuppressWarnings("deprecation")
 public class NestedPlanIntegrationTests extends AbstractDeployerIntegrationTest {
     
     private static final String GLOBAL_PACKAGE = "global";
 
     private static final String PARENT_PACKAGE_NAME = "parent";
 
     private static final String CHILD_PACKAGE_NAME = "child";
 
     private static final String SCOPE_SEPARATOR = "-";
 
     private static final String SYNTHETIC_CONTEXT_BSN_SUFFIX = "-synthetic.context";
 
     private static final String PLAN_TYPE = "plan";
 
     private static final String BUNDLE_TYPE = "bundle";
 
     private static final int WATCH_INTERVAL_MS = 1000;
 
     /*
      * An artifact must be present in a watched repository directory for up to two watch intervals before it will be
      * processed. Then it will take a while (a surprisingly long while on some operating systems) to be scheduled and
      * complete processing.
      */
     private static final int WATCH_WAIT_INTERVAL_MS = 2 * WATCH_INTERVAL_MS + 1000;
 
     private static final String TEST_RESOURCES_DIRECTORY = "src/test/resources/plan-deployment/";
 
     private static final String GENERATED_PLAN_DIRECTORY = "target/watched/";
 
     private static final String PLAN_EXTENSION = ".plan";
 
     private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
 
     private static final String NAMESPACES = "    xmlns=\"http://www.eclipse.org/virgo/schema/plan\" \n    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n    xsi:schemaLocation=\" http://www.eclipse.org/virgo/schema/plan http://www.eclipse.org/virgo/schema/plan/eclipse-virgo-plan.xsd\"";
 
     private static final Version DEFAULT_VERSION = new Version("0");
 
     private static TestArtifactInfo GLOBAL_BUNDLE_INFO = new TestArtifactInfo("bundle", GLOBAL_PACKAGE, DEFAULT_VERSION);
 
     private static TestArtifactInfo PARENT_BUNDLE_INFO = new TestArtifactInfo("bundle", PARENT_PACKAGE_NAME, DEFAULT_VERSION);
 
     private static TestArtifactInfo CHILD_BUNDLE_INFO = new TestArtifactInfo("bundle", CHILD_PACKAGE_NAME, DEFAULT_VERSION);
 
     static {
         GLOBAL_BUNDLE_INFO.setFile(new File(TEST_RESOURCES_DIRECTORY + "global.jar"));
     }
 
    private RuntimeArtifactRepository ram;
     
     private Region globalRegion;
 
     private static TestPlanArtifactInfo[] PARENTS;
 
     private DeploymentIdentity globalBundleDeploymentIdentity;
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         File generatedPlanDirectory = new File(GENERATED_PLAN_DIRECTORY);
         generatedPlanDirectory.mkdirs();
 
         generatePlans();
     }
 
     @Before
     public void setUp() throws Exception {
         ServiceReference<RuntimeArtifactRepository> runtimeArtifactRepositoryServiceReference = context.getServiceReference(RuntimeArtifactRepository.class);
         if (runtimeArtifactRepositoryServiceReference != null) {
             this.ram = context.getService(runtimeArtifactRepositoryServiceReference);
         }
         
         Collection<ServiceReference<Region>> regionServiceReferences = context.getServiceReferences(Region.class, "(org.eclipse.virgo.kernel.region.name=global)");
         if (regionServiceReferences != null && regionServiceReferences.size() == 1) {
             this.globalRegion = context.getService(regionServiceReferences.iterator().next());
         }
 
         globalBundleDeploymentIdentity = deploy(GLOBAL_BUNDLE_INFO);
     }
 
     @After
     public void tearDown() throws Exception {
         if (this.globalBundleDeploymentIdentity != null) {
             this.deployer.undeploy(this.globalBundleDeploymentIdentity);
             this.globalBundleDeploymentIdentity = null;
         }
     }
 
     @Test
     public void testUnscopedNonatomicParentOfUnscopedNonatomicChild() throws IOException, DeploymentException, InterruptedException {
         testParent("unscoped.nonatomic.parentOf.unscoped.nonatomic.child");
     }
 
     @Test
     public void testUnscopedAtomicParentOfUnscopedNonatomicChild() throws IOException, DeploymentException, InterruptedException {
         testParent("unscoped.atomic.parentOf.unscoped.nonatomic.child");
     }
 
     @Test
     public void testScopedNonatomicParentOfUnscopedNonatomicChild() throws IOException, DeploymentException, InterruptedException {
         testParent("scoped.nonatomic.parentOf.unscoped.nonatomic.child");
     }
 
     @Test
     public void testScopedAtomicParentOfUnscopedNonatomicChild() throws IOException, DeploymentException, InterruptedException {
         testParent("scoped.atomic.parentOf.unscoped.nonatomic.child");
     }
 
     @Test
     public void testUnscopedNonatomicParentOfUnscopedAtomicChild() throws IOException, DeploymentException, InterruptedException {
         testParent("unscoped.nonatomic.parentOf.unscoped.atomic.child");
     }
 
     @Test
     public void testUnscopedAtomicParentOfUnscopedAtomicChild() throws IOException, DeploymentException, InterruptedException {
         testParent("unscoped.atomic.parentOf.unscoped.atomic.child");
     }
 
     @Test
     public void testScopedNonatomicParentOfUnscopedAtomicChild() throws IOException, DeploymentException, InterruptedException {
         testParent("scoped.nonatomic.parentOf.unscoped.atomic.child");
     }
 
     @Test
     public void testScopedAtomicParentOfUnscopedAtomicChild() throws IOException, DeploymentException, InterruptedException {
         testParent("scoped.atomic.parentOf.unscoped.atomic.child");
     }
 
     @Test
     public void testUnscopedNonatomicParentOfScopedNonatomicChild() throws IOException, DeploymentException, InterruptedException {
         testParent("unscoped.nonatomic.parentOf.scoped.nonatomic.child");
     }
 
     @Test
     public void testUnscopedAtomicParentOfScopedNonatomicChild() throws IOException, DeploymentException, InterruptedException {
         testParent("unscoped.atomic.parentOf.scoped.nonatomic.child");
     }
 
     @Test
     public void testScopedNonatomicParentOfScopedNonatomiChild() throws IOException, DeploymentException, InterruptedException {
         testParent("scoped.nonatomic.parentOf.scoped.nonatomic.child");
     }
 
     @Test
     public void testScopedAtomicParentOfScopedNonatomicChild() throws IOException, DeploymentException, InterruptedException {
         testParent("scoped.atomic.parentOf.scoped.nonatomic.child");
     }
 
     @Test
     public void testUnscopedNonatomicParentOfScopedAtomicChild() throws IOException, DeploymentException, InterruptedException {
         testParent("unscoped.nonatomic.parentOf.scoped.atomic.child");
     }
 
     @Test
     public void testUnscopedAtomicParentOfScopedAtomicChild() throws IOException, DeploymentException, InterruptedException {
         testParent("unscoped.atomic.parentOf.scoped.atomic.child");
     }
 
     @Test
     public void testScopedNonatomicParentOfScopedAtomicChild() throws IOException, DeploymentException, InterruptedException {
         testParent("scoped.nonatomic.parentOf.scoped.atomic.child");
     }
 
     @Test
     public void testScopedAtomicParentOfScopedAtomicChild() throws IOException, DeploymentException, InterruptedException {
         testParent("scoped.atomic.parentOf.scoped.atomic.child");
     }
 
     /**
      * @throws IOException 
      * @throws DeploymentException  
      * @throws InterruptedException 
      */
     private void testParent(String parentName) throws IOException, DeploymentException, InterruptedException {
         for (int p = 0; p < PARENTS.length; p++) {
             TestPlanArtifactInfo parent = PARENTS[p];
             if (parentName.equals(parent.getName())) {
                 testParent(parent);
             }
         }
     }
 
     private void testParent(TestPlanArtifactInfo parent) {
         Model model = new Model(parent);
 
         boolean expectDeployOk = model.shouldDeployOk();
 
         try {
             DeploymentIdentity parentId = deploy(parent);
 
             // Fail the test if the plan should not have deployed ok.
             assertTrue(expectDeployOk);
 
             checkScoping(parent, model);
 
             checkAtomicity(parent, model);
 
             this.deployer.undeploy(parentId);
         } catch (DeploymentException e) {
             // Fail the test if the plan should have deployed ok.
             assertFalse("The plan '" + parent.getName() + "' failed to deploy '" + e.getMessage() + "'", expectDeployOk);
         }
     }
 
     private static void generatePlans() throws IOException, InterruptedException {
         // Generate the child plans
         TestPlanArtifactInfo[] children = generateChildPlans();
 
         // Wait for watched directory interval to pass.
         Thread.sleep(WATCH_WAIT_INTERVAL_MS);
 
         PARENTS = generateParentPlans(children);
     }
 
     private void checkScoping(TestPlanArtifactInfo parent, Model model) {
 
         if (model.shouldHaveScope()) {
             checkSyntheticContextBundle(model);
             checkImportPromotion(model);
             checkPlanScoping(parent, model);
         } else {
             checkNoSyntheticContextBundle();
             checkNoImportPromotion(model);
         }
     }
 
     private void checkPlanScoping(TestPlanArtifactInfo parent, Model model) {
         if (parent.isScoped()) {
             Artifact parentPlan = getPlan(parent);
             Set<Artifact> children = parentPlan.getDependents();
             for (Artifact child : children) {
                 if (PLAN_TYPE.equals(child.getType())) {
                     String childPlanName = child.getName();
                     String unscopedChildPlanName = parent.getChildPlan().getName();
                     String expectedChildPlanName = model.getScopeName() + SCOPE_SEPARATOR + unscopedChildPlanName;
                     assertEquals(expectedChildPlanName, childPlanName);
                 }
             }
         }
 
     }
 
     private void checkSyntheticContextBundle(Model model) {
         Bundle syntheticContextBundle = getSyntheticContextBundle(model.getScopeName());
         assertNotNull(syntheticContextBundle);
 
         if (model.syntheticContextShouldIncludeParent()) {
             assertTrue(contains(getBundlesImportingPackage(PARENT_PACKAGE_NAME), syntheticContextBundle));
         }
 
         if (model.syntheticContextShouldIncludeChild()) {
             assertTrue(contains(getBundlesImportingPackage(CHILD_PACKAGE_NAME), syntheticContextBundle));
         }
     }
 
     private static boolean contains(Bundle[] importingBundles, Bundle syntheticContextBundle) {
         if (importingBundles != null) {
             for (Bundle bundle : importingBundles) {
                 if (bundle == syntheticContextBundle) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     private Bundle[] getBundlesImportingPackage(String pkg) {
         ExportedPackage[] exportedPackages = this.packageAdmin.getExportedPackages(pkg);
 
         // The tests should not export a package from multiple bundles at any point in time.
         assertEquals("The Package '" + pkg + "' is exported from " + exportedPackages.length + " bundles.", 1, exportedPackages.length);
 
         ExportedPackage parentExportedPackage = exportedPackages[0];
         Bundle[] importingBundles = parentExportedPackage.getImportingBundles();
         return importingBundles;
     }
 
     private void checkNoSyntheticContextBundle() {
         Bundle[] bundles = this.framework.getBundleContext().getBundles();
         for (Bundle bundle : bundles) {
             assertFalse(bundle.getSymbolicName().endsWith(SYNTHETIC_CONTEXT_BSN_SUFFIX));
         }
     }
 
     private Bundle getSyntheticContextBundle(String scopeName) {
         String bsn = scopeName + SYNTHETIC_CONTEXT_BSN_SUFFIX;
         Bundle[] bundles = this.packageAdmin.getBundles(bsn, null);
         assertTrue(bundles.length <= 1);
         return bundles.length == 0 ? null : bundles[0];
     }
 
     private void checkImportPromotion(Model model) {
         if (model.importShouldBePromoted()) {
             assertTrue(globalImportHasBeenPromoted(model));
         }
     }
 
     private void checkNoImportPromotion(Model model) {
         assertFalse(globalImportHasBeenPromoted(model));
     }
 
     private boolean globalImportHasBeenPromoted(Model model) {
         // The global import has been promoted if and only if the child bundle imports it.
         Bundle childBundle = model.getChildBundle();
         Bundle[] bundlesImportingGlobalPackage = getBundlesImportingPackage(GLOBAL_PACKAGE);
         return contains(bundlesImportingGlobalPackage, childBundle);
     }
 
     private void checkAtomicity(TestPlanArtifactInfo parent, Model model) {
         TestPlanArtifactInfo childPlan = parent.getChildPlan();
         
         Artifact parentArtifact = getPlan(parent);
         Artifact childArtifact;
         
         if (parent.isScoped()) {
             childArtifact = getPlan(childPlan.getType(), model.getScopeName() + SCOPE_SEPARATOR + childPlan.getName(), childPlan.getVersion(), globalRegion);
         } else {
             childArtifact = getPlan(childPlan);
         }
         
         if (parent.isAtomic() && childPlan.isAtomic()) {
             checkParentAndChildBundleLifecyclesAreTied(model.getParentBundle(), model.getChildBundle(), parentArtifact, childArtifact);
         } else {
             checkParentAndChildBundleLifecyclesAreIndependent(parent.isAtomic(), model.getParentBundle(), model.getChildBundle(), parentArtifact, childArtifact);
         }                
                 
         if (parent.isAtomic()) {
             checkParentAndChildPlanLifecyclesAreTied(parentArtifact, childArtifact);
         } else {
             checkParentAndChildPlanLifecyclesAreIndependent(parentArtifact, childArtifact);
         }
     }
 
     private Artifact getPlan(TestPlanArtifactInfo plan) {
         return getPlan(plan.getType(), plan.getName(), plan.getVersion(), globalRegion);       
     }
     
     private Artifact getPlan(String type, String name, Version version, Region region) {
         Artifact planArtifact = this.ram.getArtifact(type, name, version, region);
         assertNotNull(planArtifact);
         return planArtifact;
     }
 
     private void checkParentAndChildPlanLifecyclesAreTied(Artifact parentPlan, Artifact childPlan) {
         waitUntilActive(parentPlan);
         waitUntilActive(childPlan);
 
         parentPlan.stop();
         assertEquals(ArtifactState.RESOLVED, parentPlan.getState());
         assertEquals(ArtifactState.RESOLVED, childPlan.getState());
 
         parentPlan.start();
         waitUntilActive(parentPlan);
         waitUntilActive(childPlan);
 
         childPlan.stop();
         assertEquals(ArtifactState.RESOLVED, parentPlan.getState());
         assertEquals(ArtifactState.RESOLVED, childPlan.getState());
 
         childPlan.start();
         waitUntilActive(parentPlan);
         waitUntilActive(childPlan);
     }
 
     private void checkParentAndChildPlanLifecyclesAreIndependent(Artifact parentPlan, Artifact childPlan) {
         waitUntilActive(parentPlan);
         waitUntilActive(childPlan);
 
         parentPlan.stop();
         assertEquals(ArtifactState.RESOLVED, parentPlan.getState());
         // Stopping the parent plan stops the child plan regardless of the parent's atomicity.
         assertEquals(ArtifactState.RESOLVED, childPlan.getState());
 
         parentPlan.start();
         waitUntilActive(parentPlan);
         // Starting the parent plan stops the child plan regardless of the parent's atomicity.
         waitUntilActive(childPlan);
 
         childPlan.stop();
         assertEquals(ArtifactState.ACTIVE, parentPlan.getState());
         assertEquals(ArtifactState.RESOLVED, childPlan.getState());
 
         childPlan.start();
         waitUntilActive(parentPlan);
         waitUntilActive(childPlan);
     }
 
     private void waitUntilActive(Artifact parentPlan) {
         int i = 0;
         while (parentPlan.getState() != ArtifactState.ACTIVE) {
             if (i++ > 10) {
                 // Took too long - give up.
                 assertTrue(false);
             }
             try {
                 Thread.sleep(100);
             } catch (InterruptedException e) {
             }
         }
     }
 
     private void checkParentAndChildBundleLifecyclesAreTied(Bundle parentBundle, Bundle childBundle, Artifact parentPlan, Artifact childPlan) {
         assertEquals(Bundle.ACTIVE, parentBundle.getState());
         assertEquals(Bundle.ACTIVE, childBundle.getState());
         waitUntilActive(parentPlan);
         waitUntilActive(childPlan);
         try {
             parentBundle.stop();
             assertEquals(Bundle.RESOLVED, parentBundle.getState());
             assertEquals(Bundle.RESOLVED, childBundle.getState());
 
             parentBundle.start();
             assertEquals(Bundle.ACTIVE, parentBundle.getState());
             assertEquals(Bundle.ACTIVE, childBundle.getState());
             waitUntilActive(parentPlan);
             waitUntilActive(childPlan);
 
             childBundle.stop();
             assertEquals(Bundle.RESOLVED, parentBundle.getState());
             assertEquals(Bundle.RESOLVED, childBundle.getState());
 
             childBundle.start();
             assertEquals(Bundle.ACTIVE, parentBundle.getState());
             assertEquals(Bundle.ACTIVE, childBundle.getState());
             waitUntilActive(parentPlan);
             waitUntilActive(childPlan);
         } catch (BundleException e) {
             e.printStackTrace();
             assertTrue(false);
         }
     }
 
     private void checkParentAndChildBundleLifecyclesAreIndependent(boolean atomicParentPlan, Bundle parentBundle, Bundle childBundle,
         Artifact parentPlan, Artifact childPlan) {
         assertEquals(Bundle.ACTIVE, parentBundle.getState());
         assertEquals(Bundle.ACTIVE, childBundle.getState());
         waitUntilActive(parentPlan);
         waitUntilActive(childPlan);
         try {
             parentBundle.stop();
             assertEquals(Bundle.RESOLVED, parentBundle.getState());
             // Stopping the parent bundle stops parent plan if the parent is atomic which stops the child plan, and its
             // bundles.
             assertEquals(atomicParentPlan ? Bundle.RESOLVED : Bundle.ACTIVE, childBundle.getState());
 
             parentBundle.start();
             assertEquals(Bundle.ACTIVE, parentBundle.getState());
             assertEquals(Bundle.ACTIVE, childBundle.getState());
             waitUntilActive(parentPlan);
             waitUntilActive(childPlan);
 
             childBundle.stop();
             assertEquals(Bundle.ACTIVE, parentBundle.getState());
             assertEquals(Bundle.RESOLVED, childBundle.getState());
 
             childBundle.start();
             assertEquals(Bundle.ACTIVE, parentBundle.getState());
             assertEquals(Bundle.ACTIVE, childBundle.getState());
             waitUntilActive(parentPlan);
             waitUntilActive(childPlan);
         } catch (BundleException e) {
             e.printStackTrace();
             assertTrue(false);
         }
     }
 
     private Bundle getSpecificBundle(String bsn, Version version) {
         Bundle[] bundles = packageAdmin.getBundles(bsn, "[" + version.toString() + ", " + version.toString() + "]");
         assertNotNull(bundles);
         assertEquals(1, bundles.length);
         return bundles[0];
     }
 
     /**
      * The Model class encapsulates the expected behaviour of this testcase.
      */
     private final class Model {
 
         private final TestPlanArtifactInfo parent;
 
         private final TestPlanArtifactInfo child;
 
         public Model(TestPlanArtifactInfo parent) {
             this.parent = parent;
 
             TestPlanArtifactInfo[] childPlans = parent.getChildPlans();
             assertEquals(1, childPlans.length);
             this.child = childPlans[0];
         }
 
         public boolean importShouldBePromoted() {
             return this.parent.isScoped();
         }
 
         public boolean shouldDeployOk() {
             // A parent and child should deploy ok if and only if they are not both scoped.
             return !(this.parent.isScoped() && this.child.isScoped());
         }
 
         public boolean shouldHaveScope() {
             return shouldChildBeScoped();
         }
 
         public String getScopeName() {
             assertTrue(shouldHaveScope());
             return getScopeName(this.parent.isScoped() ? this.parent : this.child);
         }
 
         private String getScopeName(TestPlanArtifactInfo scopedPlan) {
             assertTrue(scopedPlan.isScoped());
             StringBuffer scopeName = new StringBuffer();
             scopeName.append(scopedPlan.getName() + SCOPE_SEPARATOR + versionToShortString(scopedPlan.getVersion()));
             return scopeName.toString();
         }
 
         private String versionToShortString(Version version) {
             String result = version.toString();
             while (result.endsWith(".0")) {
                 result = result.substring(0, result.length() - 2);
             }
             return result;
         }
 
         public boolean syntheticContextShouldIncludeParent() {
             return this.parent.isScoped();
         }
 
         public boolean syntheticContextShouldIncludeChild() {
             return shouldChildBeScoped();
         }
 
         private boolean shouldChildBeScoped() {
             return this.parent.isScoped() || this.child.isScoped();
         }
 
         public Bundle getParentBundle() {
             return getBundle(this.parent, this.parent.isScoped() ? getScopeName() : null);
         }
 
         public Bundle getChildBundle() {
             return getBundle(this.child, shouldChildBeScoped() ? getScopeName() : null);
         }
 
         private Bundle getBundle(TestPlanArtifactInfo planContainingBundle, String scopeName) {
             TestArtifactInfo[] childBundles = planContainingBundle.getChildBundles();
             assertEquals(1, childBundles.length);
             TestArtifactInfo childBundle = childBundles[0];
 
             String bsn = childBundle.getName();
             Version version = childBundle.getVersion();
 
             if (scopeName != null) {
                 return getSpecificBundle(scopeName + SCOPE_SEPARATOR + bsn, version);
             } else {
                 return getSpecificBundle(bsn, version);
             }
         }
 
     }
 
     private static TestPlanArtifactInfo[] generateParentPlans(TestArtifactInfo[] children) throws IOException {
         TestPlanArtifactInfo[] parents = new TestPlanArtifactInfo[16];
         int p = 0;
         for (int c = 0; c < children.length; c++) {
             for (int s = 0; s < 2; s++) {
                 boolean scopedParent = s == 1;
                 for (int a = 0; a < 2; a++) {
                     boolean atomicParent = a == 1;
                     TestArtifactInfo childPlan = children[c];
                     String parentName = parentPlanName(scopedParent, atomicParent, childPlan);
                     parents[p++] = createPlanFile(parentName, DEFAULT_VERSION, scopedParent, atomicParent, PARENT_BUNDLE_INFO, childPlan);
                 }
             }
         }
         return parents;
     }
 
     private static TestPlanArtifactInfo[] generateChildPlans() throws IOException {
         TestPlanArtifactInfo[] children = new TestPlanArtifactInfo[4];
         int c = 0;
         for (int s = 0; s < 2; s++) {
             boolean scopedChild = s == 1;
             for (int a = 0; a < 2; a++) {
                 boolean atomicChild = a == 1;
                 String childName = childPlanName(scopedChild, atomicChild);
                 children[c++] = createPlanFile(childName, DEFAULT_VERSION, scopedChild, atomicChild, CHILD_BUNDLE_INFO);
             }
         }
         return children;
     }
 
     private static String childPlanName(boolean scopedChild, boolean atomicChild) {
         return scopingAtomicityName(scopedChild, atomicChild) + ".child";
     }
 
     private static String parentPlanName(boolean scopedParent, boolean atomicParent, TestArtifactInfo childPlan) {
         return scopingAtomicityName(scopedParent, atomicParent) + ".parentOf." + childPlan.getName();
     }
 
     private static String scopingAtomicityName(boolean scoped, boolean atomic) {
         return (scoped ? "scoped" : "unscoped") + "." + (atomic ? "atomic" : "nonatomic");
     }
 
    private DeploymentIdentity deploy(TestArtifactInfo i) throws DeploymentException {
        DeploymentIdentity identity = this.deployer.deploy(i.getFile().toURI());
        Assert.assertEquals(i.getType(), identity.getType());
        Assert.assertEquals(i.getName(), identity.getSymbolicName());
        Assert.assertEquals(i.getVersion(), new Version(identity.getVersion()));
         return identity;
     }
 
     public static TestPlanArtifactInfo createPlanFile(String planName, Version planVersion, boolean scoped, boolean atomic, TestArtifactInfo... children) throws IOException {
         StringBuffer planContent = new StringBuffer(1024);
         planContent.append(XML_HEADER);
         planContent.append("<plan name=\"" + planName + "\" version=\"" + planVersion + "\" scoped=\"" + new Boolean(scoped) + "\" atomic=\"" + new Boolean(atomic) + "\" \n" + NAMESPACES + ">\n");
 
         for (TestArtifactInfo childInfo : children) {
             Version childVersion = childInfo.getVersion();
             planContent.append("    <artifact type=\"" + childInfo.getType() + "\" name=\"" + childInfo.getName() + "\" version=\"[" + childVersion + ", " + childVersion + "]\"/>\n");
         }
 
         planContent.append("</plan>");
 
         TestPlanArtifactInfo info = new TestPlanArtifactInfo(planName, planVersion, scoped, atomic, children);
 
         String fileName = planName + "-" + planVersion + PLAN_EXTENSION;
         File planFile = new File(GENERATED_PLAN_DIRECTORY + fileName);
         Writer writer = new FileWriter(planFile);
         try {
             writer.write(planContent.toString());
         } finally {
             writer.close();
         }
 
         info.setFile(planFile);
 
         return info;
     }
 
     private static final class TestPlanArtifactInfo extends TestArtifactInfo {
 
         private static final TestPlanArtifactInfo[] EMPTY_CHILD_PLAN_ARRAY = new TestPlanArtifactInfo[0];
 
         private final boolean scoped;
 
         private final boolean atomic;
 
         public TestPlanArtifactInfo(String name, Version version, Boolean scoped, Boolean atomic, TestArtifactInfo[] children) {
             super(PLAN_TYPE, name, version, children);
             this.scoped = scoped;
             this.atomic = atomic;
         }
 
         public TestPlanArtifactInfo[] getChildPlans() {
             List<TestPlanArtifactInfo> childPlans = new ArrayList<TestPlanArtifactInfo>();
             for (TestArtifactInfo child : getChildren()) {
                 if (child instanceof TestPlanArtifactInfo) {
                     childPlans.add((TestPlanArtifactInfo) child);
                 }
             }
             return childPlans.toArray(EMPTY_CHILD_PLAN_ARRAY);
         }
 
         public TestPlanArtifactInfo getChildPlan() {
             TestPlanArtifactInfo[] childPlans = getChildPlans();
             assertEquals(1, childPlans.length);
             return childPlans[0];
         }
 
         public boolean isScoped() {
             return scoped;
         }
 
         public boolean isAtomic() {
             return atomic;
         }
 
     }
 
     private static class TestArtifactInfo {
 
         private static final TestArtifactInfo[] EMPTY_CHILD_ARRAY = new TestArtifactInfo[0];
 
         private final String type;
 
         private final String name;
 
         private final Version version;
 
         private File file;
 
         private TestArtifactInfo[] children;
 
         public TestArtifactInfo(String type, String name, Version version, TestArtifactInfo[] children) {
             this.type = type;
             this.name = name;
             this.version = version;
             this.children = children == null ? EMPTY_CHILD_ARRAY : children;
         }
 
         public TestArtifactInfo(String type, String name, Version version) {
             this(type, name, version, null);
         }
 
         public String getType() {
             return type;
         }
 
         public String getName() {
             return name;
         }
 
         public Version getVersion() {
             return version;
         }
 
         public TestArtifactInfo[] getChildren() {
             return this.children;
         }
 
         public TestArtifactInfo[] getChildBundles() {
             List<TestArtifactInfo> childBundles = new ArrayList<TestArtifactInfo>();
             for (TestArtifactInfo child : getChildren()) {
                 if (BUNDLE_TYPE.equals(child.getType())) {
                     childBundles.add(child);
                 }
             }
             return childBundles.toArray(EMPTY_CHILD_ARRAY);
         }
 
         public void setFile(File file) {
             this.file = file;
         }
 
         public File getFile() {
             if (this.file == null) {
                 throw new IllegalStateException("TestArtifactInfo file not set");
             }
             return file;
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public String toString() {
             return /* "TestArtifactInfo [type=" + type + ", name=" + */name /* + ", version=" + version + "]" */;
         }
 
     }
 
 }
