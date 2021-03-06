 /*
  * RHQ Management Platform
  * Copyright (C) 2005-2010 Red Hat, Inc.
  * All rights reserved.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License, version 2, as
  * published by the Free Software Foundation, and/or the GNU Lesser
  * General Public License, version 2.1, also as published by the Free
  * Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License and the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public License
  * and the GNU Lesser General Public License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  */
 package org.rhq.plugins.ant;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.List;
 import java.util.Properties;
 
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import org.rhq.core.domain.bundle.Bundle;
 import org.rhq.core.domain.bundle.BundleDeployment;
 import org.rhq.core.domain.bundle.BundleDestination;
 import org.rhq.core.domain.bundle.BundleResourceDeployment;
 import org.rhq.core.domain.bundle.BundleResourceDeploymentHistory;
 import org.rhq.core.domain.bundle.BundleType;
 import org.rhq.core.domain.bundle.BundleVersion;
 import org.rhq.core.domain.configuration.Configuration;
 import org.rhq.core.domain.configuration.PropertySimple;
 import org.rhq.core.domain.content.PackageType;
 import org.rhq.core.domain.content.PackageVersion;
 import org.rhq.core.domain.content.Repo;
 import org.rhq.core.domain.resource.Resource;
 import org.rhq.core.domain.resource.ResourceCategory;
 import org.rhq.core.domain.resource.ResourceType;
 import org.rhq.core.domain.resource.group.ResourceGroup;
 import org.rhq.core.pluginapi.bundle.BundleDeployRequest;
 import org.rhq.core.pluginapi.bundle.BundleDeployResult;
 import org.rhq.core.pluginapi.bundle.BundleManagerProvider;
 import org.rhq.core.pluginapi.inventory.ResourceContext;
 import org.rhq.core.system.SystemInfoFactory;
 import org.rhq.core.util.file.FileUtil;
 import org.rhq.core.util.stream.StreamUtil;
 import org.rhq.core.util.updater.DeploymentProperties;
 import org.rhq.core.util.updater.DeploymentsMetadata;
 
 @Test
 public class AntBundlePluginComponentTest {
     private AntBundlePluginComponent plugin;
     private File tmpDir;
     private File bundleFilesDir;
     private File destDir;
 
     @BeforeClass
     public void initDirs() throws Exception {
         this.tmpDir = new File("target/antbundletest/tmp");
         FileUtil.purge(this.tmpDir, true);
         this.bundleFilesDir = new File("target/antbundletest/bundlefiles");
         FileUtil.purge(this.bundleFilesDir, true);
         this.destDir = new File("target/antbundletest/destination");
         FileUtil.purge(this.destDir, true);
     }
 
     @BeforeMethod
     public void prepareBeforeTestMethod() throws Exception {
         if (!this.tmpDir.mkdirs()) {
             throw new IllegalStateException("Failed to create temp dir '" + this.tmpDir + "'.");
         }
         if (!this.bundleFilesDir.mkdirs()) {
             throw new IllegalStateException("Failed to create bundle files dir '" + this.bundleFilesDir + "'.");
         }
         this.plugin = new AntBundlePluginComponent();
         ResourceType type = new ResourceType("antBundleTestType", "antBundleTestPlugin", ResourceCategory.SERVER, null);
         Resource resource = new Resource("antBundleTestKey", "antBundleTestName", type);
         @SuppressWarnings("unchecked")
         ResourceContext<?> context = new ResourceContext(resource, null, null,
             SystemInfoFactory.createJavaSystemInfo(), tmpDir, null, "antBundleTestPC", null, null, null, null, null);
         this.plugin.start(context);
     }
 
     @AfterMethod(alwaysRun = true)
     public void cleanPluginDirs() {
         FileUtil.purge(this.tmpDir, true);
         FileUtil.purge(this.bundleFilesDir, true);
     }
 
     @AfterMethod(alwaysRun = true)
     public void cleanDestDir() {
         FileUtil.purge(this.destDir, true);
     }
 
     public void testAntBundleRevert() throws Exception {
         // install then upgrade a bundle first
         testAntBundleUpgrade();
         cleanPluginDirs(); // clean everything but the dest dir - we want to keep the metadata
         prepareBeforeTestMethod(); // prepare for our new test
 
         // we installed version 2.5 then upgraded to 3.0
         // now we want to revert back to 2.5
         ResourceType resourceType = new ResourceType("testSimpleBundle2Type", "plugin", ResourceCategory.SERVER, null);
         BundleType bundleType = new BundleType("testSimpleBundle2BType", resourceType);
         Repo repo = new Repo("test-bundle-two");
         PackageType packageType = new PackageType("test-bundle-two", resourceType);
         Bundle bundle = new Bundle("test-bundle-two", bundleType, repo, packageType);
         BundleVersion bundleVersion = new BundleVersion("test-bundle-two", "2.5", bundle,
             getRecipeFromFile("test-bundle-two.xml"));
         BundleDestination destination = new BundleDestination(bundle, "testSimpleBundle2Dest", new ResourceGroup(
             "testSimpleBundle2Group"), this.destDir.getAbsolutePath());
 
         Configuration config = new Configuration();
         String customPropName = "custom.prop";
         String customPropValue = "ABC-revert";
         String onePropName = "one.prop";
         String onePropValue = "111-revert";
         config.put(new PropertySimple(customPropName, customPropValue));
         config.put(new PropertySimple(onePropName, onePropValue));
 
         BundleDeployment deployment = new BundleDeployment();
         deployment.setId(789);
         deployment.setName("test bundle 2 deployment name - REVERT");
         deployment.setBundleVersion(bundleVersion);
         deployment.setConfiguration(config);
         deployment.setDestination(destination);
 
         // copy the test archive file to the bundle files dir
         FileUtil.copyFile(new File("src/test/resources/test-bundle-two-archive.zip"), new File(this.bundleFilesDir,
             "test-bundle-two-archive.zip"));
 
         // create test.properties file in the bundle files dir
         File file1 = new File(this.bundleFilesDir, "test.properties");
         Properties props = new Properties();
         props.setProperty(customPropName, "@@" + customPropName + "@@");
         FileOutputStream outputStream = new FileOutputStream(file1);
         props.store(outputStream, "test.properties comment");
         outputStream.close();
 
         BundleDeployRequest request = new BundleDeployRequest();
         request.setBundleFilesLocation(this.bundleFilesDir);
         request.setResourceDeployment(new BundleResourceDeployment(deployment, null));
         request.setBundleManagerProvider(new MockBundleManagerProvider());
         request.setRevert(true);
 
         BundleDeployResult results = plugin.deployBundle(request);
 
         assertResultsSuccess(results);
 
         // test that the prop was replaced in raw file test.properties
         Properties realizedProps = new Properties();
         realizedProps.load(new FileInputStream(new File(this.destDir, "config/test.properties")));
         assert customPropValue.equals(realizedProps.getProperty(customPropName)) : "didn't replace prop";
 
         // test that the archive was extracted properly. These are the files in the archive:
         // zero-file.txt (content: "zero")
         // one/one-file.txt (content: "@@one.prop@@") <-- recipe says this is to be replaced
         // two/two-file.txt (content: "@@two.prop@@") <-- recipe does not say to replace this
         // REMOVED: three/three-file.txt <-- this existed in the upgrade, but not the original
         // ----- the following was backed up and should be reverted
         // extra/extra-file.txt
 
         File zeroFile = new File(this.destDir, "zero-file.txt");
         File oneFile = new File(this.destDir, "one/one-file.txt");
         File twoFile = new File(this.destDir, "two/two-file.txt");
         File threeFile = new File(this.destDir, "three/three-file.txt");
         assert zeroFile.exists() : "zero file should have been restored during revert";
         assert oneFile.exists() : "one file missing";
         assert twoFile.exists() : "two file missing";
         assert !threeFile.exists() : "three file should have been deleted during revert";
 
         assert readFile(zeroFile).startsWith("zero") : "bad restore of zero file";
         assert readFile(oneFile).startsWith(onePropValue);
         assert readFile(twoFile).startsWith("@@two.prop@@");
 
         // make sure the revert restored the backed up files
         // TODO: uncomment once we fix the problem that ant launcher invokes the deploy multiple times
         //File extraFile = new File(this.destDir, "extra/extra-file.txt");
         //assert extraFile.exists() : "extra file should have been restored due to revert deployment request";
         //assert readFile(extraFile).startsWith("extra") : "bad restore of extra file";
 
         DeploymentsMetadata metadata = new DeploymentsMetadata(this.destDir);
         DeploymentProperties deploymentProps = metadata.getDeploymentProperties(deployment.getId());
         assert deploymentProps.getDeploymentId() == deployment.getId();
         assert deploymentProps.getBundleName().equals(bundle.getName());
         assert deploymentProps.getBundleVersion().equals(bundleVersion.getVersion());
 
         DeploymentProperties currentProps = metadata.getCurrentDeploymentProperties();
         assert deploymentProps.equals(currentProps);
 
         // check the backup directory - note, clean flag is irrelevent when determining what should be backed up 
         File backupDir = metadata.getDeploymentBackupDirectory(deployment.getId());
         // TODO: uncomment once we fix the problem that ant launcher invokes the deploy multiple times
         //assert backupDir.list().length == 0 : "should not have backups: " + Arrays.deepToString(backupDir.listFiles());
 
         DeploymentProperties previousProps = metadata.getPreviousDeploymentProperties(789);
         assert previousProps != null : "There should be previous deployment metadata";
         // TODO: uncomment once we fix the problem that ant launcher invokes the deploy multiple times
         //assert previousProps.getDeploymentId() == 456 : "bad previous deployment metadata"; // testAntBundleUpgrade used 456
         //assert previousProps.getBundleName().equals(deploymentProps.getBundleName());
         //assert previousProps.getBundleVersion().equals("3.0"); // testAntBundleUpgrade deployed version 3.0
     }
 
     public void testAntBundleUpgrade() throws Exception {
         upgrade(false);
     }
 
     public void testAntBundleCleanUpgrade() throws Exception {
         upgrade(true);
     }
 
     private void upgrade(boolean clean) throws Exception {
         testAntBundleInitialInstall(); // install a bundle first
         cleanPluginDirs(); // clean everything but the dest dir - we want to upgrade the destination
         prepareBeforeTestMethod(); // prepare for our new test
 
         // deploy upgrade and test it
         ResourceType resourceType = new ResourceType("testSimpleBundle2Type", "plugin", ResourceCategory.SERVER, null);
         BundleType bundleType = new BundleType("testSimpleBundle2BType", resourceType);
         Repo repo = new Repo("test-bundle-two");
         PackageType packageType = new PackageType("test-bundle-two", resourceType);
         Bundle bundle = new Bundle("test-bundle-two", bundleType, repo, packageType);
         BundleVersion bundleVersion = new BundleVersion("test-bundle-two", "3.0", bundle,
             getRecipeFromFile("test-bundle-three.xml"));
         BundleDestination destination = new BundleDestination(bundle, "testSimpleBundle2Dest", new ResourceGroup(
             "testSimpleBundle2Group"), this.destDir.getAbsolutePath());
 
         Configuration config = new Configuration();
         String customPropName = "custom.prop";
         String customPropValue = "DEF";
         String onePropName = "one.prop";
         String onePropValue = "one-one-one";
         String threePropName = "three.prop";
         String threePropValue = "333";
         config.put(new PropertySimple(customPropName, customPropValue));
         config.put(new PropertySimple(onePropName, onePropValue));
         config.put(new PropertySimple(threePropName, threePropValue));
 
         BundleDeployment deployment = new BundleDeployment();
         deployment.setId(456);
         deployment.setName("test bundle 3 deployment name - upgrades test bundle 2");
         deployment.setBundleVersion(bundleVersion);
         deployment.setConfiguration(config);
         deployment.setDestination(destination);
 
         // copy the test archive file to the bundle files dir
         FileUtil.copyFile(new File("src/test/resources/test-bundle-three-archive.zip"), new File(this.bundleFilesDir,
             "test-bundle-three-archive.zip"));
 
         // create test.properties file in the bundle files dir
         File file1 = new File(this.bundleFilesDir, "test.properties");
         Properties props = new Properties();
         props.setProperty(customPropName, "@@" + customPropName + "@@");
         FileOutputStream outputStream = new FileOutputStream(file1);
         props.store(outputStream, "test.properties comment");
         outputStream.close();
 
         // create some additional files - note: receipe says to ignore "ignore/**"
         File ignoreDir = new File(this.destDir, "ignore");
         File extraDir = new File(this.destDir, "extra");
         ignoreDir.mkdirs();
         extraDir.mkdirs();
         File ignoredFile = new File(ignoreDir, "ignore-file.txt");
         File extraFile = new File(extraDir, "extra-file.txt");
         FileUtil.writeFile(new ByteArrayInputStream("ignore".getBytes()), ignoredFile);
         FileUtil.writeFile(new ByteArrayInputStream("extra".getBytes()), extraFile);
 
         BundleDeployRequest request = new BundleDeployRequest();
         request.setBundleFilesLocation(this.bundleFilesDir);
         request.setResourceDeployment(new BundleResourceDeployment(deployment, null));
         request.setBundleManagerProvider(new MockBundleManagerProvider());
         request.setCleanDeployment(clean);
 
         BundleDeployResult results = plugin.deployBundle(request);
 
         assertResultsSuccess(results);
 
         // test that the prop was replaced in raw file test.properties
         Properties realizedProps = new Properties();
         realizedProps.load(new FileInputStream(new File(this.destDir, "config/test.properties")));
         assert customPropValue.equals(realizedProps.getProperty(customPropName)) : "didn't replace prop";
 
         // test that the archive was extracted properly. These are the files in the archive or removed from original:
         // REMOVED: zero-file.txt
         // one/one-file.txt (content: "@@one.prop@@") <-- recipe says this is to be replaced
         // two/two-file.txt (content: "@@two.prop@@") <-- recipe does not say to replace this
         // three/three-file.txt (content: "@@three.prop@@") <-- recipe says this is to be replaced
         File zeroFile = new File(this.destDir, "zero-file.txt");
         File oneFile = new File(this.destDir, "one/one-file.txt");
         File twoFile = new File(this.destDir, "two/two-file.txt");
         File threeFile = new File(this.destDir, "three/three-file.txt");
         assert !zeroFile.exists() : "zero file should have been removed during upgrade";
         assert oneFile.exists() : "one file missing";
         assert twoFile.exists() : "two file missing";
         assert threeFile.exists() : "three file missing";
         if (clean) {
             assert !ignoredFile.exists() : "ignored file should have been deleted due to clean deployment request";
             assert !extraFile.exists() : "extra file should have been deleted due to clean deployment request";
         } else {
             assert ignoredFile.exists() : "ignored file wasn't ignored, it was deleted";
             assert !extraFile.exists() : "extra file ignored, but it should have been deleted/backed up";
         }
         assert readFile(oneFile).startsWith(onePropValue);
         assert readFile(twoFile).startsWith("@@two.prop@@");
         assert readFile(threeFile).startsWith(threePropValue);
 
         DeploymentsMetadata metadata = new DeploymentsMetadata(this.destDir);
         DeploymentProperties deploymentProps = metadata.getDeploymentProperties(deployment.getId());
         assert deploymentProps.getDeploymentId() == deployment.getId();
         assert deploymentProps.getBundleName().equals(bundle.getName());
         assert deploymentProps.getBundleVersion().equals(bundleVersion.getVersion());
 
         DeploymentProperties currentProps = metadata.getCurrentDeploymentProperties();
         assert deploymentProps.equals(currentProps);
 
         // check the backup directory - note, clean flag is irrelevent when determining what should be backed up 
         File backupDir = metadata.getDeploymentBackupDirectory(deployment.getId());
         File extraBackupFile = new File(backupDir, extraDir.getName() + File.separatorChar + extraFile.getName());
         File ignoredBackupFile = new File(backupDir, ignoreDir.getName() + File.separatorChar + ignoredFile.getName());
         assert !ignoredBackupFile.exists() : "ignored file was backed up but it should not have been";
         assert extraBackupFile.exists() : "extra file was not backed up";
         assert "extra".equals(new String(StreamUtil.slurp(new FileInputStream(extraBackupFile)))) : "bad backup of extra";
 
         DeploymentProperties previousProps = metadata.getPreviousDeploymentProperties(456);
         assert previousProps != null : "There should be previous deployment metadata";
         // TODO: uncomment once we fix the problem that ant launcher invokes the deploy multiple times
         //assert previousProps.getDeploymentId() == 123 : "bad previous deployment metadata"; // testAntBundleInitialInstall used 123
         //assert previousProps.getBundleName().equals(deploymentProps.getBundleName());
         //assert previousProps.getBundleVersion().equals("2.5"); // testAntBundleInitialInstall deployed version 2.5
     }
 
     /**
      * Test deployment of an RHQ bundle recipe with archive file and raw file
      */
     public void testAntBundleInitialInstall() throws Exception {
         ResourceType resourceType = new ResourceType("testSimpleBundle2Type", "plugin", ResourceCategory.SERVER, null);
         BundleType bundleType = new BundleType("testSimpleBundle2BType", resourceType);
         Repo repo = new Repo("test-bundle-two");
         PackageType packageType = new PackageType("test-bundle-two", resourceType);
         Bundle bundle = new Bundle("test-bundle-two", bundleType, repo, packageType);
         BundleVersion bundleVersion = new BundleVersion("test-bundle-two", "2.5", bundle,
             getRecipeFromFile("test-bundle-two.xml"));
         BundleDestination destination = new BundleDestination(bundle, "testSimpleBundle2Dest", new ResourceGroup(
             "testSimpleBundle2Group"), this.destDir.getAbsolutePath());
 
         Configuration config = new Configuration();
         String customPropName = "custom.prop";
         String customPropValue = "ABC";
         String onePropName = "one.prop";
         String onePropValue = "111";
         config.put(new PropertySimple(customPropName, customPropValue));
         config.put(new PropertySimple(onePropName, onePropValue));
 
         BundleDeployment deployment = new BundleDeployment();
         deployment.setId(123);
         deployment.setName("test bundle 2 deployment name");
         deployment.setBundleVersion(bundleVersion);
         deployment.setConfiguration(config);
         deployment.setDestination(destination);
 
         // copy the test archive file to the bundle files dir
         FileUtil.copyFile(new File("src/test/resources/test-bundle-two-archive.zip"), new File(this.bundleFilesDir,
             "test-bundle-two-archive.zip"));
 
         // create test.properties file in the bundle files dir
         File file1 = new File(this.bundleFilesDir, "test.properties");
         Properties props = new Properties();
         props.setProperty(customPropName, "@@" + customPropName + "@@");
         FileOutputStream outputStream = new FileOutputStream(file1);
         props.store(outputStream, "test.properties comment");
         outputStream.close();
 
         BundleDeployRequest request = new BundleDeployRequest();
         request.setBundleFilesLocation(this.bundleFilesDir);
         request.setResourceDeployment(new BundleResourceDeployment(deployment, null));
         request.setBundleManagerProvider(new MockBundleManagerProvider());
 
         BundleDeployResult results = plugin.deployBundle(request);
 
         assertResultsSuccess(results);
 
         // test that the prop was replaced in raw file test.properties
         Properties realizedProps = new Properties();
         realizedProps.load(new FileInputStream(new File(this.destDir, "config/test.properties")));
         assert customPropValue.equals(realizedProps.getProperty(customPropName)) : "didn't replace prop";
 
         // test that the archive was extracted properly. These are the files in the archive:
         // zero-file.txt (content: "zero")
         // one/one-file.txt (content: "@@one.prop@@") <-- recipe says this is to be replaced
         // two/two-file.txt (content: "@@two.prop@@") <-- recipe does not say to replace this
         File zeroFile = new File(this.destDir, "zero-file.txt");
         File oneFile = new File(this.destDir, "one/one-file.txt");
         File twoFile = new File(this.destDir, "two/two-file.txt");
         assert zeroFile.exists() : "zero file missing";
         assert oneFile.exists() : "one file missing";
         assert twoFile.exists() : "two file missing";
         assert readFile(zeroFile).startsWith("zero");
         assert readFile(oneFile).startsWith(onePropValue);
         assert readFile(twoFile).startsWith("@@two.prop@@");
 
         DeploymentsMetadata metadata = new DeploymentsMetadata(this.destDir);
         DeploymentProperties deploymentProps = metadata.getDeploymentProperties(deployment.getId());
         assert deploymentProps.getDeploymentId() == deployment.getId();
         assert deploymentProps.getBundleName().equals(bundle.getName());
         assert deploymentProps.getBundleVersion().equals(bundleVersion.getVersion());
         DeploymentProperties currentProps = metadata.getCurrentDeploymentProperties();
         assert deploymentProps.equals(currentProps);
         DeploymentProperties previousProps = metadata.getPreviousDeploymentProperties(deployment.getId());
         // TODO: uncomment once we fix the problem that ant launcher invokes the deploy multiple times
        assert previousProps == null : "There should not be any previous deployment metadata";
     }
 
     /**
      * Test deployment of an RHQ bundle recipe.
      */
     public void testAntBundle() throws Exception {
         ResourceType resourceType = new ResourceType("testSimpleBundle", "plugin", ResourceCategory.SERVER, null);
         BundleType bundleType = new BundleType("testSimpleBundle", resourceType);
         Repo repo = new Repo("testSimpleBundle");
         PackageType packageType = new PackageType("testSimpleBundle", resourceType);
         Bundle bundle = new Bundle("testSimpleBundle", bundleType, repo, packageType);
         BundleVersion bundleVersion = new BundleVersion("testSimpleBundle", "1.0", bundle,
             getRecipeFromFile("test-bundle.xml"));
         BundleDestination destination = new BundleDestination(bundle, "testSimpleBundle", new ResourceGroup(
             "testSimpleBundle"), this.destDir.getAbsolutePath());
 
         Configuration config = new Configuration();
         String realPropValue = "ABC123";
         config.put(new PropertySimple("custom.prop1", realPropValue));
 
         BundleDeployment deployment = new BundleDeployment();
         deployment.setName("test bundle deployment name");
         deployment.setBundleVersion(bundleVersion);
         deployment.setConfiguration(config);
         deployment.setDestination(destination);
 
         // create test file
         File file1 = new File(this.bundleFilesDir, "test.properties");
         Properties props = new Properties();
         props.setProperty("custom.prop1", "@@custom.prop1@@");
         FileOutputStream outputStream = new FileOutputStream(file1);
         props.store(outputStream, "replace");
         outputStream.close();
 
         // create noreplace test file
         File noreplacefile = new File(this.bundleFilesDir, "noreplace.properties");
         outputStream = new FileOutputStream(noreplacefile);
         props.store(outputStream, "noreplace");
         outputStream.close();
 
         BundleDeployRequest request = new BundleDeployRequest();
         request.setBundleFilesLocation(this.bundleFilesDir);
         request.setResourceDeployment(new BundleResourceDeployment(deployment, null));
         request.setBundleManagerProvider(new MockBundleManagerProvider());
 
         BundleDeployResult results = plugin.deployBundle(request);
 
         assertResultsSuccess(results);
 
         // test that the prop was replaced in test.properties
         Properties realizedProps = new Properties();
         realizedProps.load(new FileInputStream(new File(this.destDir, "config/test.properties")));
         assert realPropValue.equals(realizedProps.getProperty("custom.prop1")) : "didn't replace prop";
 
         // test that the prop was not replaced in noreplace.properties
         Properties notrealizedProps = new Properties();
         notrealizedProps.load(new FileInputStream(new File(this.destDir, "config/noreplace.properties")));
         assert "@@custom.prop1@@".equals(notrealizedProps.getProperty("custom.prop1")) : "replaced prop when it shouldn't";
     }
 
     private void assertResultsSuccess(BundleDeployResult results) {
         assert (results.getErrorMessage() == null) : "Failed to process bundle: [" + results.getErrorMessage() + "]";
         assert results.isSuccess() : "Failed to process bundle!: [" + results.getErrorMessage() + "]";
     }
 
     private String getRecipeFromFile(String filename) {
         InputStream stream = getClass().getClassLoader().getResourceAsStream(filename);
 
         byte[] contents = StreamUtil.slurp(stream);
         return new String(contents);
     }
 
     private String readFile(File file) throws Exception {
         return new String(StreamUtil.slurp(new FileInputStream(file)));
     }
 
     private class MockBundleManagerProvider implements BundleManagerProvider {
         public void auditDeployment(BundleResourceDeployment deployment, String action, String info,
             BundleResourceDeploymentHistory.Category category, BundleResourceDeploymentHistory.Status status,
             String message, String attachment) throws Exception {
             System.out.println("Auditing deployment step [" + message + "]...");
         }
 
         public List<PackageVersion> getAllBundleVersionPackageVersions(BundleVersion bundleVersion) throws Exception {
             return null;
         }
 
         public long getFileContent(PackageVersion packageVersion, OutputStream outputStream) throws Exception {
             return 0;
         }
     }
 }
