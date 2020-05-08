 package de.tarent.maven.plugins.pkg;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import org.junit.Assert;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import de.tarent.maven.plugins.pkg.helper.Helper;
 import de.tarent.maven.plugins.pkg.map.PackageMap;
 import de.tarent.maven.plugins.pkg.map.Visitor;
 
 public class HelperTest extends AbstractMvnPkgPluginTestCase{
 	Packaging packaging;
 	Helper helper;
 	TargetConfiguration targetConfiguration;
 	boolean previousfilefound;
 	String homedir = System.getProperty("user.home");
 	File f = new File(homedir + "/.rpmmacros");
 	File buildDir = new File("src/test/resources/target");
 	String ignorePackagingTypes = "pom";
 	List<TargetConfiguration> resolvedConfigurations;
 	
 	PackageMap packageMap;
 	
 	@Before
 	public void setUp() throws Exception{
 		super.setUp();
 		packaging = (Packaging) mockEnvironment(RPMPOM,"pkg",true);
 		
 		// Blank instance that tests can modify at will.
 		targetConfiguration = new TargetConfiguration();
 		
 		// A blank list of resolvedConfigurations which can be modified at will
 		// by test cases.
 		resolvedConfigurations = new ArrayList<TargetConfiguration>();
 		
 		packageMap = new PackageMap(null, null, "default", null);
 		
 		// Helper needs a PackageMap instance and that cannot be changed afterwards. However
 		// for testing purposes we want to be able to change every bit of the implementation.
 		// For this the following instance of PackageMap delegates all calls to the 'packageMap'
 		// instance. The test implementor can then set her *own* instance to packageMap. The
 		// default used above is for those who are not interested in changing the packageMap
 		// at all.
 		PackageMap delegatingPackageMap = new PackageMap(null, null, homedir, null) {
 
 			@Override
 			public String getPackaging() {
 				return packageMap.getPackaging();
 			}
 
 			@Override
 			public String getDefaultDependencyLine() {
 				return packageMap.getDefaultDependencyLine();
 			}
 
 			@Override
 			public String getDefaultJarPath() {
 				return packageMap.getDefaultJarPath();
 			}
 
 			@Override
 			public String getDefaultJNIPath() {
 				return packageMap.getDefaultJNIPath();
 			}
 
 			@Override
 			public String getDistroLabel() {
 				return packageMap.getDistroLabel();
 			}
 
 			@Override
 			public String getRepositoryName() {
 				return packageMap.getRepositoryName();
 			}
 
 			@Override
 			public String getDefaultBinPath() {
 				return packageMap.getDefaultBinPath();
 			}
 
 			@Override
 			public boolean isDebianNaming() {
 				return packageMap.isDebianNaming();
 			}
 
 			@Override
 			public boolean hasNoPackages() {
 				return packageMap.hasNoPackages();
 			}
 
 			@Override
			public void iterateDependencyArtifacts(Log l, Collection deps,
 					Visitor v, boolean bundleNonExisting) {
 				packageMap.iterateDependencyArtifacts(l, deps, v, bundleNonExisting);
 			}
 			
 		};
 		
 		helper = new Helper();
 		helper.init(packaging, delegatingPackageMap, targetConfiguration, resolvedConfigurations,"foobar");
 		previousfilefound = false;
 		if(f.exists()){
 			FileUtils.moveFile(f, new File(homedir + "/.rpmmacros_Test_backup"));
 			previousfilefound = true;
 		}		
 	}
 
 	
 	@After
 	public void tearDown() throws Exception{
 		super.tearDown();
 		if(previousfilefound){
 			f.delete();
 			FileUtils.moveFile(new File(homedir + "/.rpmmacros_Test_backup"),f);
 		}
 		
 	}
 	
 	@Test
 	public void creatingRpmmacrosfileWithoutMaintainerAndRemovingSuccessfully() throws IOException, MojoExecutionException{
 		// Depends on fixates TargetConfiguration.
 		targetConfiguration.fixate();
 		
 		helper.setBasePkgDir(new File("/"));		
 		helper.createRpmMacrosFile();
 		Assert.assertTrue("File not found",f.exists());
 		helper.restoreRpmMacrosFileBackup(null);
 	}
 	
 	@Test
 	public void creatingRpmmacrosfileWitMaintainerAndRemovingSuccessfully() throws IOException, MojoExecutionException{
 		targetConfiguration.setMaintainer("Dummy maintainer");	
 		targetConfiguration.fixate();
 		
 		helper.setBasePkgDir(new File("/"));		
 		helper.createRpmMacrosFile();
 		Assert.assertTrue(f.exists());
 		Assert.assertTrue("String not found", filecontains(f, "%_gpg_name       Dummy maintainer"));
 		helper.restoreRpmMacrosFileBackup(null);
 	}
 	
 	/**
 	 * Checks whether macro file generation works without exception with an unconfigured
 	 * {@link TargetConfiguration}.
 	 * 
 	 * @throws IOException
 	 * @throws MojoExecutionException
 	 */
 	public void testCreatingRpmmacrosfileWithoutBaseDir() throws IOException, MojoExecutionException{
 		targetConfiguration.fixate();
 		
 		helper.createRpmMacrosFile();
 	}
 	
 	@Test
 	public void prepareInitialDirectoriesSuccesfully_RPM() throws MojoExecutionException, IOException{
 		// This method needs the Helper to be in RPM mode
 		helper.setStrategy(Helper.RPM_STRATEGY);
 		
 		File base = new File(TARGETDIR,"/BaseTestTemp/");
 		helper.setBasePkgDir(base);
 		helper.prepareInitialDirectories();
 		Assert.assertTrue(base.exists());
 		Assert.assertEquals(new File(helper.getBasePkgDir(),"/BUILD"),helper.getBaseBuildDir());
 		Assert.assertEquals(new File(helper.getBasePkgDir(),"/SPECS"),helper.getBaseSpecsDir());		
 		FileUtils.deleteDirectory(base);
 		
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void prepareInitialDirectoriesWithoutBaseOrTempRootThrowsException() throws MojoExecutionException{
 		helper.prepareInitialDirectories();
 	}
 	@Test
 	public void generateFilelist() throws MojoExecutionException, IOException{
 		File tempdir = File.createTempFile("temp", "file");
 		helper.setBaseBuildDir(tempdir.getParentFile());
 		Assert.assertTrue(helper.generateFilelist().size()>0);
 
 	}
 	
 	@Test
 	public void getVersionReturnsPackagingVersion_RPM() throws Exception {
 		targetConfiguration.setDistro("foobar");
 		targetConfiguration.fixate();
 		
 		helper.setStrategy(Helper.RPM_STRATEGY);
 		
 		Assert.assertTrue(helper.getPackageVersion().contains(packaging.project.getVersion()));
 	}
 	
 	@Test
 	public void testVersion_plain_DEB() throws Exception {
 		targetConfiguration.fixate();
 		
 		String expected = packaging.project.getVersion();
 		Assert.assertEquals(expected, helper.getPackageVersion());
 	}
 
 	@Test
 	public void testVersion_versionsuffix_DEB() throws Exception {
 		String vs = "debianrules";
 		targetConfiguration.setPackageVersionSuffix(vs);
 		targetConfiguration.fixate();
 		
 		String expected = packaging.project.getVersion() + "-0" + vs;
 		Assert.assertEquals(expected, helper.getPackageVersion());
 	}
 
 	@Test
 	public void testVersion_revision_DEB() throws Exception {
 		String rs = "r0";
 		targetConfiguration.setRevision(rs);
 		targetConfiguration.fixate();
 		
 		String expected = packaging.project.getVersion() + "-" + rs;
 		Assert.assertEquals(expected, helper.getPackageVersion());
 	}
 
 	@Test
 	public void testVersion_versionsuffix_revision_DEB() throws Exception {
 		String vs = "debianrules";
 		targetConfiguration.setPackageVersionSuffix(vs);
 		String rs = "r0";
 		targetConfiguration.setRevision(rs);
 		targetConfiguration.fixate();
 		
 		String expected = String.format("%s-0%s-%s", packaging.project.getVersion(), vs, rs);
 		Assert.assertEquals(expected, helper.getPackageVersion());
 	}
 	
 	@Test
 	public void getDstArtifactFileReturnsgetBaseBuildDirAndgetTargetArtifactFiletoStringIfNotSet_RPM(){
 		// This test needs the Helper to be in RPM mode
 		helper.setStrategy(Helper.RPM_STRATEGY);
 		
 		File testTempdir = new File(TARGETDIR,"/BaseTestTemp/");
 		File testArtifactfile = new File("file1");
 		helper.setBaseBuildDir(testTempdir);
 		helper.setTargetArtifactFile(testArtifactfile);
 		Assert.assertEquals(helper.getBaseBuildDir()+"/"+helper.getTargetArtifactFile().toString(),helper.getDstArtifactFile().toString());
 		
 	}
 	
 	/**
 	 * Tests that {@link Helper#createDependencyLine} puts the so-called
 	 * dependency line from the {@lin PackageMap} into the final dependency
 	 * line.
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void testCreateDependencyLine_defaultDependencyLine() throws Exception {
 		final String elString = "PackagingSessionTargetDependencyRelationshipsArtifact";
 		
 		// Creates a PackageMap which has no influence on the 
 		packageMap = new PackageMap(null, null, "default", null) {
 			@Override
 			public String getDefaultDependencyLine() {
 				return elString;
 			}
 
 			@Override
 			public boolean isDebianNaming() {
 				// Not really necessary for this test but the test string contains
 				// uppercase letters and those are not allowed for Debian names.
 				return false;
 			}
 			
 			
 		};
 		
 		targetConfiguration.setTarget("foo");
 		targetConfiguration.fixate();
 
 		Set<Artifact> deps = helper.resolveProjectDependencies();
 		String line = helper.createDependencyLine(deps);
 		Assert.assertEquals(elString, line);
 	}
 	
  	/**
 	 * Tests the successful addition of resolved dependencies to the
 	 * dependency line created by {@link Helper#createDependencyLine}.
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void testCreateDependencyLine_resolvedDeps() throws Exception {
 		// Creates a PackageMap which has no influence on the 
 		packageMap = new PackageMap(null, null, "default", null) {
 			@Override
 			public String getDefaultDependencyLine() {
 				return "";
 			}
 			
 		};
 		
 		targetConfiguration.setTarget("foo");
 		targetConfiguration.fixate();
 
 		TargetConfiguration t1 = new TargetConfiguration("t1");
 		t1.setSection("misc");
 		t1.setPackageNameSuffix("t1");
 		t1.fixate();
 
 		TargetConfiguration t2 = new TargetConfiguration("t2");
 		t2.setSection("misc");
 		t2.setPackageNameSuffix("t2");
 		t2.fixate();
 		
 		TargetConfiguration t3 = new TargetConfiguration("t3");
 		t3.setSection("misc");
 		t3.setPackageNameSuffix("t3");
 		t3.fixate();
 		
 		// Modifies the resolvedConfiguration used by the Helper
 		resolvedConfigurations.add(t1);
 		resolvedConfigurations.add(t2);
 		resolvedConfigurations.add(t3);
 		
 		Set<Artifact> deps = helper.resolveProjectDependencies();
 		String line = helper.createDependencyLine(deps);
 		Assert.assertEquals("dummyproject-t1, dummyproject-t2, dummyproject-t3", line);
 	}
 }
