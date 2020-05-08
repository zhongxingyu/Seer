 package de.tarent.maven.plugins.pkg;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Assert;
 
import org.apache.commons.io.IOUtils;
 import org.apache.maven.model.License;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.SystemStreamLog;
 import org.codehaus.plexus.archiver.ArchiveFile;
 import org.codehaus.plexus.archiver.tar.GZipTarFile;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class UtilsTest extends AbstractMvnPkgPluginTestCase{
 	
 	@Before
 	public void setUp() throws Exception{
 		super.setUp();
 	}
 	
 	@After
 	public void tearDown() throws Exception{
 		super.tearDown();
 	}
 	
 	@Test
 	public void getTargetConfigurationFromString() throws MojoExecutionException{
 		
 		List<TargetConfiguration> l = new ArrayList<TargetConfiguration>();
 		TargetConfiguration t1 = new TargetConfiguration();
 		TargetConfiguration t2 = new TargetConfiguration();
 		TargetConfiguration t3 = new TargetConfiguration();
 		TargetConfiguration t4 = new TargetConfiguration();
 		l.add(t1);
 		l.add(t2);
 		l.add(t3);
 		l.add(t4);
 		
 		t1.setTarget("unwantedConfig");
 		t2.setTarget("unwantedConfig");
 		t4.setTarget("unwantedConfig");	
 		
 		t3.setTarget("wantedConfig");
 		
 		Assert.assertEquals(t3, Utils.getTargetConfigurationFromString("wantedConfig", l));
 		
 		
 	}
 	
 	@Test	
 	@SuppressWarnings("unchecked")
 	public void getLicenseForProject() throws Exception{
 		
 		Packaging p = (Packaging)mockEnvironment(RPMPOM, "pkg");
 		List<License>licenses = createLicenseList("License 1", "License 2");
 		
 		p.project.setLicenses(licenses);
 		String result = Utils.getConsolidatedLicenseString(p.project);
 		Assert.assertEquals("License 1, License 2",result);
 		
 		List<License> l = p.project.getLicenses();
 		l.remove(0);
 		p.project.setLicenses(l);
 		result = Utils.getConsolidatedLicenseString(p.project);
 		Assert.assertEquals("License 2",result);
 		
 	}
 	
 	@Test(expected=MojoExecutionException.class)
 	public void getLicenseForProjectWithEmptyLicenseElement() throws Exception{
 		
 		Packaging p = (Packaging)mockEnvironment(RPMPOM, "pkg");
 		p.project.setLicenses(null);
 		Utils.getConsolidatedLicenseString(p.project);
 	}
 	
 	@Test(expected=MojoExecutionException.class)
 	public void getLicenseForProjectWithoutLicenseElement() throws Exception{
 		
 		Packaging p = (Packaging)mockEnvironment(RPMPOM, "pkg",false);
 		Utils.getConsolidatedLicenseString(p.project);
 	}
 	
 	/**
 	 * This test attempts to grab a local file and checks its contents to verify that the process
 	 * was executed correctly.
 	 *  
 	 * <p>Fetching external (web) resources relays on the same mechanism (java.net.URL.openStream()), 
 	 * so we will just test it locally.</p> 
 	 * @throws IOException
 	 */
 	@Test
 	public void getLicenseFromUrl() throws IOException{
 		String localFile = "file://" + getBasedir() + "/src/test/resources/dummyproject/"+DEBPOM;
 		
 		Assert.assertTrue(Utils.getTextFromUrl(localFile).
 				contains("<groupId>de.maven.plugins.test</groupId>"));
 	}
 	
 	/**
 	 * Checks the core functionality of the {@link Utils#getMergedConfiguration()}
 	 * method.
 	 * 
 	 * <p>Uses a valid dataset.</p>
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void getMergedConfiguration_valid() throws Exception {
 		// Sets up 2 configuration where one inherits from the other.
 		// When the method is called we expect a new instance which has
 		// properties of both.
 		
 		// Just some random values that can be accessed by their
 		// reference later (IOW the value is not important and has
 		// no meaning).
 		String set_in_t1 = "set_in_t1";
 		String overridden_in_t2 = "overridden_in_t2";
 		
 		TargetConfiguration t1 = new TargetConfiguration("t1");
 		t1.setDistro("foo");
 		t1.setPrefix(set_in_t1);
 		t1.setMainClass(set_in_t1);
 		
 		TargetConfiguration t2 = new TargetConfiguration("t2");
 		t2.parent = "t1";
 		t2.setDistro("foo");
 		t2.setMainClass(overridden_in_t2);
 		
 		List<TargetConfiguration> tcs = new LinkedList<TargetConfiguration>();
 		tcs.add(t1);
 		tcs.add(t2);
 		
 		Utils.mergeAllConfigurations(tcs);
 
 		TargetConfiguration resultt1 = tcs.get(0);	
 		TargetConfiguration resultt2 = tcs.get(1);	
 		
 		// The parent should have its configuration intact
 		Assert.assertEquals("t1", resultt1.getTarget());
 		Assert.assertEquals(null, resultt1.parent);
 		Assert.assertEquals(1,resultt1.getDistros().size());
 		Assert.assertTrue(resultt1.getDistros().contains("foo"));
 		Assert.assertEquals(set_in_t1, resultt1.getPrefix());
 		Assert.assertEquals(set_in_t1, resultt1.getMainClass());
 		
 		Assert.assertEquals("t2", resultt2.getTarget());
 		Assert.assertEquals("t1", resultt2.parent);
 		Assert.assertTrue(resultt2.getDistros().contains("foo"));
 		Assert.assertEquals(1,resultt2.getDistros().size());
 		Assert.assertEquals(set_in_t1, resultt2.getPrefix());
 		Assert.assertEquals(overridden_in_t2, resultt2.getMainClass());		
 	}
 
 	/**
 	 * Checks the core functionality of the {@link Utils#getMergedConfiguration()}
 	 * method.
 	 * 
 	 * <p>Uses the distro defined in a parent.</p>
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void getMergedConfiguration_mergeddistros() throws Exception {
 		// Sets up 2 configuration where one inherits from the other.
 		// When the method is called we expect a new instance which has
 		// properties of both.
 		
 		// Just some random values that can be accessed by their
 		// reference later (IOW the value is not important and has
 		// no meaning).
 		String set_in_t1 = "set_in_t1";
 		String overridden_in_t2 = "overridden_in_t2";
 		
 		// T1 supports foo
 		TargetConfiguration t1 = new TargetConfiguration("t1");
 		t1.setDistro("foo");
 		t1.setPrefix(set_in_t1);
 		t1.setMainClass(set_in_t1);
 		
 		// T2 supports baz (
 		TargetConfiguration t2 = new TargetConfiguration("t2");
 		t2.parent = "t1";
 		t2.setDistro("baz");
 		t2.setMainClass(overridden_in_t2);
 		
 		List<TargetConfiguration> tcs = new LinkedList<TargetConfiguration>();
 		tcs.add(t1);
 		tcs.add(t2);
 		
 		Utils.mergeAllConfigurations(tcs);
 		
 		TargetConfiguration result = tcs.get(1);
 		
 		Assert.assertEquals("t2", result.getTarget());
 		Assert.assertEquals("t1", result.parent);
 		// foo is declared in t1 but not t2. This is OK.
 		Assert.assertTrue(result.getDistros().contains("foo"));
 		Assert.assertEquals(set_in_t1, result.getPrefix());
 		Assert.assertEquals(overridden_in_t2, result.getMainClass());
 	}
 
 	/**
 	 * Checks the core functionality of the {@link Utils#getMergedConfiguration()}
 	 * method.
 	 * 
 	 * <p>Uses the distro defined in a parent's parent.</p>
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void getMergedConfiguration_mergeddistros2() throws Exception {
 		// Sets up 3 configuration where one inherits from the other.
 		// When the method is called we expect a new instance which has
 		// properties of both.
 		
 		// Just some random values that can be accessed by their
 		// reference later (IOW the value is not important and has
 		// no meaning).
 		String set_in_t1 = "set_in_t1";
 		String overridden_in_t2 = "overridden_in_t2";
 		
 		// T1 supports foo
 		TargetConfiguration t1 = new TargetConfiguration("t1");
 		t1.setDistro("foo");
 		t1.setPrefix(set_in_t1);
 		t1.setMainClass(set_in_t1);
 		
 		// T2 supports foo indirectly (
 		TargetConfiguration t2 = new TargetConfiguration("t2");
 		t2.parent = "t1";
 		t2.setMainClass(overridden_in_t2);
 
 		// T3 supports foo indirectly (
 		TargetConfiguration t3 = new TargetConfiguration("t3");
 		t3.parent = "t2";
 		
 		List<TargetConfiguration> tcs = new LinkedList<TargetConfiguration>();
 		tcs.add(t1);
 		tcs.add(t2);
 		tcs.add(t3);
 		
 		Utils.mergeAllConfigurations(tcs);
 		
 		TargetConfiguration result = tcs.get(2);
 		
 		Assert.assertEquals("t3", result.getTarget());
 		Assert.assertEquals("t2", result.parent);
 		// foo is declared in t1 but not t2 nor t3. This is OK.
 		Assert.assertTrue(result.getDistros().contains("foo"));
 		Assert.assertEquals(set_in_t1, result.getPrefix());
 		Assert.assertEquals(overridden_in_t2, result.getMainClass());
 	}
 	
 	/**
 	 * Checks the core functionality of the {@link Utils#getMergedConfiguration()}
 	 * method.
 	 * 
 	 * <p>Two targetconfiguration with the same parent.</p>
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void getMergedConfiguration_mergeddistros_sharedparent() throws Exception {
 		// Sets up 3 configuration where one inherits from the other.
 		// When the method is called we expect a new instance which has
 		// properties of both.
 		
 		// Just some random values that can be accessed by their
 		// reference later (IOW the value is not important and has
 		// no meaning).
 		String set_in_t1 = "set_in_t1";
 		String overridden_in_t2a = "overridden_in_t2a";
 		String overridden_in_t2b = "overridden_in_t2b";
 		
 		// T1 supports foo
 		TargetConfiguration t1 = new TargetConfiguration("t1");
 		t1.setDistro("foo");
 		t1.setPrefix(set_in_t1);
 		t1.setMainClass(set_in_t1);
 		
 		TargetConfiguration t2a = new TargetConfiguration("t2a");
 		t2a.setMainClass(overridden_in_t2a);
 		t2a.parent = "t1";
 		List<String> t2aDeps = new ArrayList<String>();
 		t2aDeps.add("t2aDependency");
 		t2a.setManualDependencies(t2aDeps);
 
 		TargetConfiguration t2b = new TargetConfiguration("t2b");
 		t2b.setMainClass(overridden_in_t2b);
 		t2b.parent = "t1";
 		List<String> t2bDeps = new ArrayList<String>();
 		t2bDeps.add("t2bDependency1");
 		t2bDeps.add("t2bDependency2");
 		t2b.setManualDependencies(t2bDeps);
 		
 		List<TargetConfiguration> tcs = new LinkedList<TargetConfiguration>();
 		tcs.add(t1);
 		tcs.add(t2a);
 		tcs.add(t2b);
 		
 		Utils.mergeAllConfigurations(tcs);
 		
 		TargetConfiguration resultt1 = tcs.get(0);
 		TargetConfiguration result2a = tcs.get(1);
 		TargetConfiguration result2b = tcs.get(2);
 		
 		// The parent should have its configuration intact
 		Assert.assertTrue(resultt1.isReady());
 		Assert.assertEquals("t1", resultt1.getTarget());
 		Assert.assertEquals(null, resultt1.parent);
 		Assert.assertTrue(resultt1.getDistros().contains("foo"));
 		Assert.assertEquals(set_in_t1, resultt1.getPrefix());
 		Assert.assertEquals(set_in_t1, resultt1.getMainClass());
 		Assert.assertEquals(0,resultt1.getManualDependencies().size());
 		
 		Assert.assertTrue(result2a.isReady());
 		Assert.assertEquals("t2a", result2a.getTarget());
 		Assert.assertEquals("t1", result2a.parent);
 		Assert.assertTrue(result2a.getDistros().contains("foo"));
 		Assert.assertEquals(set_in_t1, result2a.getPrefix());
 		Assert.assertEquals(overridden_in_t2a, result2a.getMainClass());
 		Assert.assertEquals(1,result2a.getManualDependencies().size());
 		
 		Assert.assertTrue(result2b.isReady());
 		Assert.assertEquals("t2b", result2b.getTarget());
 		Assert.assertEquals("t1", result2b.parent);
 		Assert.assertTrue(result2b.getDistros().contains("foo"));
 		Assert.assertEquals(set_in_t1, result2b.getPrefix());
 		Assert.assertEquals(overridden_in_t2b, result2b.getMainClass());
 		Assert.assertEquals(2,result2b.getManualDependencies().size());		
 		
 		// Besides the basic functionality it is actually important for this test
 		// that the creation of result2b suceeds because a previous bug prevented the
 		// merge request of that one because t1 was already touched.
 	}
 	
 	/**
 	 * A test for the {@link Utils#createBuildChain} method.
 	 * 
 	 * <p>It checks whether the relation between the target configurations
 	 * are properly found and the correct build chain is created.</p>
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void createBuildChain_valid() throws Exception {
 		TargetConfiguration defaultConfig = new TargetConfiguration();
 		defaultConfig.setDistro("foo");
 
 		TargetConfiguration t1 = new TargetConfiguration("t1");
 		t1.setDistro("foo");
 		TargetConfiguration t2 = new TargetConfiguration("t2");
 		t2.setDistro("foo");
 		TargetConfiguration t3 = new TargetConfiguration("t3");
 		t3.setDistro("foo");
 		TargetConfiguration t4 = new TargetConfiguration("t4");
 		t4.setDistro("foo");
 		TargetConfiguration t5 = new TargetConfiguration("t5");
 		t5.setDistro("foo");
 		
 		// Just some random target configurations that have no relation to the
 		// others.
 		TargetConfiguration t1_unrelated = new TargetConfiguration("t1_unrelated");
 		TargetConfiguration t2_unrelated = new TargetConfiguration("t2_unrelated");
 		TargetConfiguration t3_unrelated = new TargetConfiguration("t3_unrelated");
 		TargetConfiguration t4_unrelated = new TargetConfiguration("t4_unrelated");
 		
 		List<TargetConfiguration> tcs = new LinkedList<TargetConfiguration>();
 		tcs.add(t1);
 		tcs.add(t1_unrelated);
 		tcs.add(t2);
 		tcs.add(t2_unrelated);
 		tcs.add(t3);
 		tcs.add(t3_unrelated);
 		tcs.add(t4);
 		tcs.add(t4_unrelated);
 		tcs.add(t5);
 		
 		createRelation(t1, t2);
 		createRelation(t2, t3);
 		createRelation(t3, t4);
 		createRelation(t4, t5);
 		
 		Utils.mergeAllConfigurations(tcs);
 		
 		List<TargetConfiguration> result = Utils.createBuildChain("t1", "foo", tcs);
 		
 		// Now check whether the algorithm really found the right build order
 		// (t5 -> t4 -> t3 -> t2 -> t1)
 		Assert.assertEquals(t5.getTarget(), result.get(0).getTarget());
 		Assert.assertEquals(t4.getTarget(), result.get(1).getTarget());
 		Assert.assertEquals(t3.getTarget(), result.get(2).getTarget());
 		Assert.assertEquals(t2.getTarget(), result.get(3).getTarget());
 		Assert.assertEquals(t1.getTarget(), result.get(4).getTarget());
 	}
 	
 	@Test(expected=MojoExecutionException.class)
 	public void getMergedConfiguration_twice() throws Exception {
 		TargetConfiguration tc1 = new TargetConfiguration("tc1");
 		tc1.setDistro("foo");
 		
 		List<TargetConfiguration> tcs = new LinkedList<TargetConfiguration>();
 		tcs.add(tc1);
 
 		Utils.mergeAllConfigurations(tcs);
 		
 		// Doing it twice should result in an exception
 		Utils.mergeAllConfigurations(tcs);
 	}
 	
 	/**
 	 * A test for the {@link Utils#createBuildChain} method.
 	 * 
 	 * <p>It sets up a few target configurations which are connected through
 	 * so called 'relations'. However the linked configurations do not share
 	 * a common distro which is considered a mistake in the configuration.</p>
 	 * 
 	 * <p>The test checks whether this mistake is detected by means of a
 	 * {@link MojoExecutionException}.</p>
 	 * 
 	 * @throws Exception
 	 */
 	@Test(expected=MojoExecutionException.class)
 	public void createBuildChain_invalid() throws Exception {
 		TargetConfiguration defaultConfig = new TargetConfiguration();
 		defaultConfig.setDistro("foo");
 
 		TargetConfiguration t1 = new TargetConfiguration("t1");
 		t1.setDistro("foo");
 		TargetConfiguration t2 = new TargetConfiguration("t2");
 		t2.setDistro("foo");
 		TargetConfiguration t3 = new TargetConfiguration("t3");
 		t3.setDistro("bar");
 		TargetConfiguration t4 = new TargetConfiguration("t4");
 		t4.setDistro("baz");
 		TargetConfiguration t5 = new TargetConfiguration("t5");
 		t5.setDistro("blub");
 		
 		// Just some random target configurations that have no relation to the
 		// others.
 		TargetConfiguration t1_unrelated = new TargetConfiguration("t1_unrelated");
 		TargetConfiguration t2_unrelated = new TargetConfiguration("t2_unrelated");
 		TargetConfiguration t3_unrelated = new TargetConfiguration("t3_unrelated");
 		TargetConfiguration t4_unrelated = new TargetConfiguration("t4_unrelated");
 		
 		List<TargetConfiguration> tcs = new LinkedList<TargetConfiguration>();
 		tcs.add(t1);
 		tcs.add(t1_unrelated);
 		tcs.add(t2);
 		tcs.add(t2_unrelated);
 		tcs.add(t3);
 		tcs.add(t3_unrelated);
 		tcs.add(t4);
 		tcs.add(t4_unrelated);
 		tcs.add(t5);
 		
 		createRelation(t1, t2);
 		createRelation(t2, t3);
 		createRelation(t3, t4);
 		createRelation(t4, t5);
 		
 		Utils.mergeAllConfigurations(tcs);
 		
 		List<TargetConfiguration> l = Utils.createBuildChain("t1", "foo", tcs);
 		System.out.println(l);
 	}
 
 	/**
 	 * Helper that makes y depend on x.
 	 * 
 	 * @param x
 	 * @param y
 	 */
 	private void createRelation(TargetConfiguration x, TargetConfiguration y) {
 		ArrayList<String> l = new ArrayList<String>();
 		l.add(y.getTarget());
 		
 		x.setRelations(l);
 	}
 	
 	/**
 	 * Tests of the {@link Utils#createPackageName} method without suffix and normal
 	 * lower case names.
 	 */
 	@Test
 	public void createPackageName_basic() {
 		Assert.assertEquals("app100", Utils.createPackageName("app100", null, "foo", false));
 		Assert.assertEquals("app100", Utils.createPackageName("app100", null, "foo", true));
 		Assert.assertEquals("app100", Utils.createPackageName("app100", null, "libs", false));
 		Assert.assertEquals("libapp100-java", Utils.createPackageName("app100", null, "libs", true));
 	}
 	
 	/**
 	 * Tests of the {@link Utils#createPackageName} method <em>with</em> suffix and normal
 	 * lower case names.
 	 */
 	@Test
 	public void createPackageName_suffix() {
 		Assert.assertEquals("app100-suffix", Utils.createPackageName("app100", "suffix", "foo", false));
 		Assert.assertEquals("app100-suffix", Utils.createPackageName("app100", "suffix", "foo", true));
 		Assert.assertEquals("app100-suffix", Utils.createPackageName("app100", "suffix", "libs", false));
 		Assert.assertEquals("libapp100-suffix-java", Utils.createPackageName("app100", "suffix", "libs", true));
 	}
 		
 	/**
 	 * Tests of the {@link Utils#createPackageName} method without suffix and mixed-case names.
 	 */
 	@Test
 	public void createPackageName_basic_mixed() {
 		Assert.assertEquals("APP100", Utils.createPackageName("APP100", null, "foo", false));
 		Assert.assertEquals("app100", Utils.createPackageName("APP100", null, "foo", true));
 		Assert.assertEquals("APP100", Utils.createPackageName("APP100", null, "libs", false));
 		Assert.assertEquals("libapp100-java", Utils.createPackageName("APP100", null, "libs", true));
 	}
 	
 	/**
 	 * Tests of the {@link Utils#createPackageName} method <em>with</em> suffix and mixed-case names.
 	 */
 	@Test
 	public void createPackageName_suffix_mixed() {
 		Assert.assertEquals("APP100-SUFFIX", Utils.createPackageName("APP100", "SUFFIX", "foo", false));
 		Assert.assertEquals("app100-suffix", Utils.createPackageName("APP100", "SUFFIX", "foo", true));
 		Assert.assertEquals("APP100-SUFFIX", Utils.createPackageName("APP100", "SUFFIX", "libs", false));
 		Assert.assertEquals("libapp100-suffix-java", Utils.createPackageName("APP100", "SUFFIX", "libs", true));
 	}
 	
 	/**
 	 * Checks the {@link Utils#createPackageNames} method.
 	 */
 	@Test
 	public void createPackageNames() throws Exception {
 		TargetConfiguration tc1 = new TargetConfiguration("tc1");
 		tc1.setPackageNameSuffix("GUI");
 		tc1.setSection("office");
 		tc1.fixate();
 
 		TargetConfiguration tc2 = new TargetConfiguration("tc2");
 		tc2.setPackageNameSuffix("CORE");
 		tc2.setSection("libs");
 		tc2.fixate();
 		
 		ArrayList<TargetConfiguration> tcs = new ArrayList<TargetConfiguration>();
 		tcs.add(tc1);
 		tcs.add(tc2);
 		
 		List<String> result = Utils.createPackageNames("base", tcs, false);
 		Assert.assertEquals(2, result.size());
 		Assert.assertEquals("base-GUI", result.get(0));
 		Assert.assertEquals("base-CORE", result.get(1));
 
 		result = Utils.createPackageNames("base", tcs, true);
 		Assert.assertEquals(2, result.size());
 		Assert.assertEquals("base-gui", result.get(0));
 		Assert.assertEquals("libbase-core-java", result.get(1));
 	}
 	
 	/**
 	 * Tests {@link Utils#toMap} with valid parameters.
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void toMap_valid() throws Exception {
 		TargetConfiguration tc1 = new TargetConfiguration("tc1");
 		TargetConfiguration tc2 = new TargetConfiguration("tc2");
 		TargetConfiguration tc3 = new TargetConfiguration("tc3");
 		List<TargetConfiguration> tcs = new ArrayList<TargetConfiguration>();
 		tcs.add(tc1);
 		tcs.add(tc2);
 		tcs.add(tc3);
 		
 		Map<String, TargetConfiguration> map = Utils.toMap(tcs);
 		
 		Assert.assertEquals(tc1, map.get("tc1"));
 		Assert.assertEquals(tc2, map.get("tc2"));
 		Assert.assertEquals(tc3, map.get("tc3"));
 	}
 
 	/**
 	 * Tests {@link Utils#toMap} with invalid parameters, ie. a list that
 	 * contains two {@link TargetConfiguration} instances with same name.
 	 * 
 	 * @throws Exception
 	 */
 	@Test(expected=MojoExecutionException.class)
 	public void toMap_invalid() throws Exception {
 		TargetConfiguration tc1 = new TargetConfiguration("tc1");
 		TargetConfiguration tc2 = new TargetConfiguration("tc2");
 		TargetConfiguration tc3 = new TargetConfiguration("tc3");
 		TargetConfiguration tc1_copy = new TargetConfiguration("tc1");
 
 		List<TargetConfiguration> tcs = new ArrayList<TargetConfiguration>();
 		tcs.add(tc1);
 		tcs.add(tc2);
 		tcs.add(tc3);
 		tcs.add(tc1_copy);
 		
 		Utils.toMap(tcs);
 	}
 
 	/**
 	 * Test the {@link Utils#resolveConfigurations} method with valid arguments which
 	 * means that all requested target names exist in the map.
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void resolveConfigurations_valid() throws Exception {
 		TargetConfiguration tc1 = new TargetConfiguration("tc1");
 		TargetConfiguration tc2 = new TargetConfiguration("tc2");
 		TargetConfiguration tc3 = new TargetConfiguration("tc3");
 		TargetConfiguration tc4 = new TargetConfiguration("tc4");
 		TargetConfiguration tc5 = new TargetConfiguration("tc5");
 		List<TargetConfiguration> tcs = new ArrayList<TargetConfiguration>();
 		tcs.add(tc1);
 		tcs.add(tc2);
 		tcs.add(tc3);
 		tcs.add(tc4);
 		tcs.add(tc5);
 		
 		Map<String, TargetConfiguration> map = Utils.toMap(tcs);
 		
 		ArrayList<String> list = new ArrayList<String>();
 		list.add("tc1");
 		list.add("tc3");
 		list.add("tc5");
 		
 		List<TargetConfiguration> result = Utils.resolveConfigurations(list, map);
 		
 		Assert.assertEquals(3, result.size());
 		Assert.assertEquals(tc1, result.get(0));
 		Assert.assertEquals(tc3, result.get(1));
 		Assert.assertEquals(tc5, result.get(2));
 	}
 
 	/**
 	 * Test the {@link Utils#resolveConfigurations} method with invalid arguments which
 	 * means that at least one requested target names does not exist in the map.
 	 * 
 	 * @throws Exception
 	 */
 	@Test(expected=MojoExecutionException.class)
 	public void resolveConfigurations_invalid() throws Exception {
 		TargetConfiguration tc1 = new TargetConfiguration("tc1");
 		TargetConfiguration tc2 = new TargetConfiguration("tc2");
 		TargetConfiguration tc3 = new TargetConfiguration("tc3");
 		TargetConfiguration tc4 = new TargetConfiguration("tc4");
 		TargetConfiguration tc5 = new TargetConfiguration("tc5");
 		List<TargetConfiguration> tcs = new ArrayList<TargetConfiguration>();
 		tcs.add(tc1);
 		tcs.add(tc2);
 		tcs.add(tc3);
 		tcs.add(tc4);
 		tcs.add(tc5);
 		
 		Map<String, TargetConfiguration> map = Utils.toMap(tcs);
 		
 		ArrayList<String> list = new ArrayList<String>();
 		list.add("tc1");
 		list.add("tc3");
 		list.add("tc-doesnotexist");
 		list.add("tc5");
 		
 		Utils.resolveConfigurations(list, map);
 	}
 
 	@Test
 	public void getDefaultDistro_singledistro() throws Exception {
 		String distro = "bla";
 		TargetConfiguration tc = new TargetConfiguration("test");
 		tc.setDistro(distro);
 		tc.fixate();
 		
 		List<TargetConfiguration> tcs = new ArrayList<TargetConfiguration>();
 		tcs.add(tc);
 		
 		Assert.assertEquals(distro, Utils.getDefaultDistro("test", tcs, new SystemStreamLog()));
 	}
 
 	/**
 	 * Tests that requesting the default distro which is not available in a
 	 * given @{TargetConfiguration} instance but in its parent.
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void getDefaultDistro_singledistro_singleinheritance() throws Exception {
 		String distro = "bla";
 		TargetConfiguration t1 = new TargetConfiguration("t1");
 		t1.setDistro(distro);
 
 		TargetConfiguration t2 = new TargetConfiguration("t2");
 		t2.parent = "t1";
 		
 		List<TargetConfiguration> tcs = new ArrayList<TargetConfiguration>();
 		tcs.add(t1);
 		tcs.add(t2);
 		Utils.mergeAllConfigurations(tcs);
 		Assert.assertEquals(distro, Utils.getDefaultDistro("t2", tcs, new SystemStreamLog()));
 	}
 	
 	@Test
 	public void getDefaultDistro_manydistros_withdefault() throws Exception {
 		String distro = "bla";
 		TargetConfiguration tc = new TargetConfiguration("test");
 		tc.setDistros(new HashSet<String>(Arrays.asList(new String[] { "bar", "baz", distro, "foo" })));
 		tc.setDefaultDistro(distro);
 		tc.fixate();
 		
 		List<TargetConfiguration> tcs = new ArrayList<TargetConfiguration>();
 		tcs.add(tc);
 		
 		Assert.assertEquals(distro, Utils.getDefaultDistro("test", tcs, new SystemStreamLog()));
 	}
 
 	@Test(expected=MojoExecutionException.class)
 	public void getDefaultDistro_nodistro() throws Exception {
 		TargetConfiguration tc = new TargetConfiguration("test");
 		tc.fixate();
 		
 		List<TargetConfiguration> tcs = new ArrayList<TargetConfiguration>();
 		tcs.add(tc);
 		
 		Utils.getDefaultDistro("test", tcs, new SystemStreamLog());
 	}
 	
 	@Test(expected=MojoExecutionException.class)
 	public void getDefaultDistro_manydistros_nodefault() throws Exception {
 		String distro = "bla";
 		TargetConfiguration tc = new TargetConfiguration("test");
 		tc.setDistros(new HashSet<String>(Arrays.asList(new String[] { "bar", "baz", distro, "foo" })));
 		tc.fixate();
 		
 		List<TargetConfiguration> tcs = new ArrayList<TargetConfiguration>();
 		tcs.add(tc);
 		
 		Utils.getDefaultDistro("test", tcs, new SystemStreamLog());
 	}
 	
 	@Test
 	public void getFileFromArchiveSucessfully() throws MojoExecutionException, URISyntaxException, IOException{
 		  ArchiveFile archive = new GZipTarFile(new File(UtilsTest.class.getResource("testarchive.tar.gz").toURI()));
 		  File f = Utils.getFileFromArchive(archive, "testfile");
 	      BufferedReader input =  new BufferedReader(new FileReader(f));
 		  assertTrue(input.readLine().equals("test contents"));
		  IOUtils.closeQuietly(input);
 	}
 	
 	@Test(expected=MojoExecutionException.class)
 	public void getFileFromArchiveThrowsExceptionWhenNotFound() throws MojoExecutionException, URISyntaxException, IOException{
 		  ArchiveFile archive = new GZipTarFile(new File(UtilsTest.class.getResource("testarchive.tar.gz").toURI()));
 		  try{
 			  Utils.getFileFromArchive(archive, "filenotavailable");
 		  }catch(MojoExecutionException ex){
 			  assertTrue(ex.getMessage().contains("Desired file not found"));
 			  throw ex;
 		  }
 		  
 	}
 	
 	@Test
 	public void checkDebianPackageNameCompatibility(){		
 		String goodName1 = "bind9";
 		String goodName2 = "bind+9";
 		String badName1  = "BIND9";
 		String badName2  = "-bind9";
 		String badName3  = "bind9-";
 		assertTrue(Utils.checkDebianPackageNameConvention(goodName1));
 		assertTrue(Utils.checkDebianPackageNameConvention(goodName2));
 		assertFalse(Utils.checkDebianPackageNameConvention(badName1));	
 		assertFalse(Utils.checkDebianPackageNameConvention(badName2));
 		assertFalse(Utils.checkDebianPackageNameConvention(badName3));			
 	}
 	
 	@Test
 	public void checkDebianPackageVersionCompatibility(){		
 		String goodVersion1 = "9.7.3.dfsg-1~squeeze4";
 		String goodVersion2 = "9.7.3.dfsg";
 		String goodVersion3 = "1328106678:9.7.3-DFSG";
 		String badVersion1  = "r9.7.3.dfsg-1~squeeze4";
 		String badVersion2  = "R9.7.3.dfsg";
 		String badVersion3  = "R9.7.3._dfsg";
 		String badVersion4  = "9A:.7.3._dfsg";
 		String badVersion5 = "9.7.3.DFSG-";
 		assertTrue(Utils.checkDebianPackageVersionConvention(goodVersion1));
 		assertTrue(Utils.checkDebianPackageVersionConvention(goodVersion2));
 		assertTrue(Utils.checkDebianPackageVersionConvention(goodVersion3));
 		assertFalse(Utils.checkDebianPackageVersionConvention(badVersion1));	
 		assertFalse(Utils.checkDebianPackageVersionConvention(badVersion2));	
 		assertFalse(Utils.checkDebianPackageVersionConvention(badVersion3));	
 		assertFalse(Utils.checkDebianPackageVersionConvention(badVersion4));	
 		assertFalse(Utils.checkDebianPackageVersionConvention(badVersion5));			
 	}
 }
 
