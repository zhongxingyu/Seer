 package de.tarent.maven.plugins.pkg;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.junit.Assert;
 import org.junit.Test;
 
 public class TargetConfigurationTest {
 
 	/**
 	 * Tests that accessing a property in a non-fixated target configuration results
 	 * in a {@link IllegalStateException}.
 	 */
 	@Test(expected=IllegalStateException.class)
 	public void testFixationRequirement() {
 		TargetConfiguration tc = new TargetConfiguration("bla");
 		
 		// Results in an exception
 		tc.getArchitecture();
 	}
 
 	/**
 	 * Tests that accessing a property in a fixated target configuration works
 	 * as expected.
 	 */
 	@Test
 	public void testFixationRequirementMet() throws Exception {
 		TargetConfiguration tc = new TargetConfiguration("bla");
 		
 		tc.fixate();
 		
		// Must not result in an exception
 		tc.getArchitecture();
 	}
 	
 	/**
 	 * Tests whether the JNI file sets are really merged.
 	 * @throws MojoExecutionException 
 	 */
 	@Test
 	public void testJNIFileMerge() throws MojoExecutionException
 	{
 		JniFile f;
 		List<JniFile> expected = new ArrayList<JniFile>();
 		
 		TargetConfiguration tc1 = new TargetConfiguration();
 		List<JniFile> l1 = new ArrayList<JniFile>();
 		
 		f = new JniFile();
 		f.from = "bla";
 		f.to = "blu/";
 		expected.add(f);
 		
 		l1.add(f);
 		
 		tc1.setJniFiles(l1);
 		
 		TargetConfiguration tc2 = new TargetConfiguration();
 		List<JniFile> l2 = new ArrayList<JniFile>();
 		
 		f = new JniFile();
 		f.from = "foo";
 		f.to = "bar/";
 		expected.add(f);
 		
 		l2.add(f);
 		tc2.setJniFiles(l2);
 		
 		TargetConfiguration merged = Utils.mergeConfigurations(tc2,tc1);
 		Assert.assertEquals(expected, merged.getJniFiles());
 	}
 	
 	/**
 	 * Tests whether the distros property is really and properly merged.
 	 * @throws MojoExecutionException 
 	 */
 	@Test
 	public void testDistrosMerge() throws MojoExecutionException
 	{
 		String d;
 		Set<String> expected = new HashSet<String>();
 		
 		TargetConfiguration tc1 = new TargetConfiguration();
 		Set<String> l1 = new HashSet<String>();
 		
 		d = "ubuntu_jaunty";
 		expected.add(d);
 		l1.add(d);
 		
 		tc1.setDistros(l1);
 		
 		TargetConfiguration tc2 = new TargetConfiguration();
 		Set<String> l2 = new HashSet<String>();
 		
 		d = "debian_lenny";
 		expected.add(d);
 		l2.add(d);
 		
 		tc2.setDistros(l2);
 		
 		TargetConfiguration merged = Utils.mergeConfigurations(tc2,tc1);
 		Assert.assertEquals(expected, merged.getDistros());
 	}
 
 	/**
 	 * Tests whether the distros property is really and properly merged.
 	 * @throws MojoExecutionException 
 	 */
 	@Test
 	public void testSystemPropertiesMerge() throws MojoExecutionException
 	{
 		Properties expected = new Properties();
 		
 		TargetConfiguration tc1 = new TargetConfiguration();
 		Properties l1 = new Properties();
 		
 		expected.setProperty("bla", "blavalue");
 		l1.setProperty("bla", "blavalue");
 		tc1.setSystemProperties(l1);
 		
 		TargetConfiguration tc2 = new TargetConfiguration();
 		Properties l2 = new Properties();
 		
 		expected.setProperty("foo", "foovalue");
 		l2.setProperty("foo", "foovalue");
 		
 		tc2.setSystemProperties(l2);
 		
 		TargetConfiguration merged = Utils.mergeConfigurations(tc2,tc1);
 		Assert.assertEquals(expected, merged.getSystemProperties());
 	}
 
 	/**
 	 * Tests whether the manual dependencies property is really
 	 * and properly merged.
 	 * @throws MojoExecutionException 
 	 */
 	@Test
 	public void testManualDependenciesMerge() throws MojoExecutionException
 	{
 		String d;
 		List<String> expected = new ArrayList<String>();
 		
 		TargetConfiguration tc1 = new TargetConfiguration();
 		List<String> l1 = new ArrayList<String>();
 		
 		d = "foo";
 		expected.add(d);
 		l1.add(d);
 		
 		tc1.setManualDependencies(l1);
 		
 		TargetConfiguration tc2 = new TargetConfiguration();
 		List<String> l2 = new ArrayList<String>();
 		
 		d = "bar";
 		expected.add(d);
 		l2.add(d);
 		
 		tc2.setManualDependencies(l2);
 		
 		TargetConfiguration merged = Utils.mergeConfigurations(tc2,tc1);
 		Assert.assertEquals(expected, merged.getManualDependencies());
 	}
 
 	/**
 	 * Tests whether an unset string property with no default value is properly
 	 * fixated.
 	 * 
 	 * @throws MojoExecutionException 
 	 */
 	@Test
 	public void testStringPropertyMerge_unset_nodefault_fixate() throws MojoExecutionException
 	{
 		TargetConfiguration tc1 = new TargetConfiguration("tc1");
 		tc1.fixate();
 		
 		Assert.assertEquals(null, tc1.getPackageNameSuffix());
 		Assert.assertEquals(null, tc1.getPackageVersionSuffix());
 		Assert.assertEquals(null, tc1.getRevision());
 	}
 
 	/**
 	 * Tests whether an unset string property with a default value is properly
 	 * fixated.
 	 * 
 	 * @throws MojoExecutionException 
 	 */
 	@Test
 	public void testStringPropertyMerge_unset_fixate() throws MojoExecutionException
 	{
 		TargetConfiguration tc1 = new TargetConfiguration("tc1");
 		tc1.fixate();
 		
 		Assert.assertEquals("unknown", tc1.getRelease());
 	}
 	
 	/**
 	 * Tests whether a set string property with a default value is properly
 	 * fixated.
 	 * 
 	 * @throws MojoExecutionException 
 	 */
 	@Test
 	public void testStringPropertyMerge_set_fixate() throws MojoExecutionException
 	{
 		TargetConfiguration tc1 = new TargetConfiguration("tc1");
 		tc1.setRelease("heavens");
 		tc1.fixate();
 		
 		Assert.assertEquals("heavens", tc1.getRelease());
 	}
 	
 	/**
 	 * Tests whether a string property with a default value is properly
 	 * inherited.
 	 * 
 	 * @throws MojoExecutionException 
 	 */
 	@Test
 	public void testStringPropertyMerge_inherit() throws MojoExecutionException
 	{
 		TargetConfiguration tc1 = new TargetConfiguration("tc1");
 		tc1.setRelease("inheritme");
 		
 		TargetConfiguration tc2 = new TargetConfiguration("tc2");
 		
 		Utils.mergeConfigurations(tc2, tc1);
 		
 		Assert.assertEquals("inheritme", tc2.getRelease());
 	}
 	
 	/**
 	 * Tests whether a string property with a default value is properly
 	 * overridden.
 	 * 
 	 * @throws MojoExecutionException 
 	 */
 	@Test
 	public void testStringPropertyMerge_override() throws MojoExecutionException
 	{
 		TargetConfiguration tc1 = new TargetConfiguration("tc1");
 		
 		TargetConfiguration tc2 = new TargetConfiguration("tc2");
 		tc1.setRelease("overrideme");
 		
 		Utils.mergeConfigurations(tc2, tc1);
 		
 		Assert.assertEquals("overrideme", tc2.getRelease());
 	}
 	
 	/**
 	 * Tests if a target configuration is ready to be used.
 	 * It should only be ready when it has been fixated or merged at least once.
 	 * @throws MojoExecutionException 
 	 */
 	@Test
 	public void testTargetConfigurationIsReady() throws MojoExecutionException{
 		TargetConfiguration tc = new TargetConfiguration();
 		Assert.assertFalse(tc.isReady());
 		tc.fixate();
 		Assert.assertTrue(tc.isReady());
 		tc = new TargetConfiguration();
 		Assert.assertFalse(tc.isReady());
 		Utils.mergeConfigurations(tc,new TargetConfiguration());
 		Assert.assertTrue(tc.isReady());
 		
 	}
 }
