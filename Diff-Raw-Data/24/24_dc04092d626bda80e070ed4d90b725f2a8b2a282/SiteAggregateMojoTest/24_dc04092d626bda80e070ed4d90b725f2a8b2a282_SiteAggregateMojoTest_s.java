 package org.jboss.maven.plugins.util.test;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.codehaus.plexus.util.FileUtils;
 import org.jboss.maven.plugins.util.SiteAggregateMojo;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 public class SiteAggregateMojoTest {
 
 	private static final String separatorLine = "======================================================================";
 
 	private File tempDir;
 
 	/**
 	 * @parameter
 	 */
 	private String sourceURL;
 
 	SiteAggregateMojo site;
 
 	String excludes;
 
 	private static final String[] sourceURLs = {
 			"http://download.jboss.org/jbosstools/builds/staging/",
 			"file:///home/nboldt/tru/jmx/" };
 
 	private static boolean verbose;
 
 	@Before
 	public void setUp() throws IOException {
 		tempDir = createTempDir(getClass().getSimpleName());
 
 		if (sourceURL == null) {
 			sourceURL = sourceURLs[0];
 		}
 		site = new SiteAggregateMojo();
 		site.setSourceURL(sourceURL);
 		verbose = site.getVerbose();
 		if (verbose)
 			System.out.println(separatorLine);
 	}
 
 	@After
 	public void tearDown() throws IOException {
 		FileUtils.deleteDirectory(tempDir);
 		verbose = site.getVerbose();
 		if (verbose)
 			System.out.println(separatorLine);
 	}
 
 	@Test
 	public void testGetRemoteSubfolders() throws IOException,
 			MojoExecutionException {
 		site.setSourceURL(sourceURLs[0]);
 		site.fetchSubfolders();
 		Assert.assertTrue(true);
 	}
 
 	@Test
 	public void testCreateRemoteCompositeSiteMetadata() throws IOException,
 			MojoExecutionException {
 		testGetRemoteSubfolders();
 		site.createCompositeSiteMetadata();
 		Assert.assertTrue(true);
 	}
 
 	@Test
 	public void testGetLocalSubfolders() throws IOException,
 			MojoExecutionException {
 		site.setSourceURL(sourceURLs[1]);
 		site.fetchSubfolders();
 		Hashtable<String, String> subfolders = site.getSubfolders();
 		String prefix = "/" + site.getSourceURL().replaceAll("file:/+", "");
 		for (Enumeration<String> e = subfolders.elements(); e.hasMoreElements();) {
 			String subfolder = (String) e.nextElement();
 			subfolder = prefix
 					+ (prefix.endsWith("/") || subfolder.indexOf("/") == 0 ? ""
 							: "/") + subfolder;
 			Assert.assertTrue("File does not exist: " + subfolder, new File(
 					subfolder).exists());
 		}
 	}
 
 	@Test
 	public void testCreateLocalCompositeSiteMetadata() throws IOException,
 			MojoExecutionException {
 		testGetLocalSubfolders();
 		site.createCompositeSiteMetadata("JBoss Tools Staging Repository",
				site.getSourceURL() + (site.getSourceURL().endsWith("/") ? "" : "/"), "", site.getTargetDir());
 		Assert.assertTrue(true);
 	}
 
 	private File createTempDir(String prefix) throws IOException {
 		File directory = File.createTempFile(prefix, "");
 		if (directory.delete()) {
 			directory.mkdirs();
 			return directory;
 		} else {
 			throw new IOException("Could not create temp directory at: "
 					+ directory.getAbsolutePath());
 		}
 	}
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 	}
 
 }
