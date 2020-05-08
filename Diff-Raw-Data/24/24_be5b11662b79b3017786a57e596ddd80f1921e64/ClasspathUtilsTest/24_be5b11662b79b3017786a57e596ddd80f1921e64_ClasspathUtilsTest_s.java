 /**
  * 
  */
 package info.freelibrary.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.jar.JarFile;
 
 import org.junit.Test;
 
 import static org.junit.Assert.fail;
 
 /**
  * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
  */
 public class ClasspathUtilsTest {
 
 	/**
 	 * Test method for {@link info.freelibrary.util.ClasspathUtils#getDirs()}.
 	 */
 	@Test
 	public void testGetDirs() {
 		for (String filename : ClasspathUtils.getDirs()) {
 			if (!new File(filename).isDirectory()) {
 				fail(filename + " isn't a directory as expected");
 			}
 		}
 	}
 
 	/**
 	 * Test method for
 	 * {@link info.freelibrary.util.ClasspathUtils#getDirFiles()}.
 	 */
 	@Test
 	public void testGetDirFiles() {
 		for (File file : ClasspathUtils.getDirFiles()) {
 			if (!file.isDirectory()) {
 				fail(file.getAbsolutePath() + " isn't a directory as expected");
 			}
 		}
 	}
 
 	/**
 	 * Test method for
 	 * {@link info.freelibrary.util.ClasspathUtils#getDirs(java.io.FilenameFilter)}
 	 * .
 	 */
 	@Test
 	public void testGetDirsFilenameFilter() {
		RegexDirFilter filter = new RegexDirFilter("(.*)classes");
		int count = ClasspathUtils.getDirFiles(filter).length;
 
		if (count != 2) {
			fail("Expected to find 2 matches for regex but found " + count);
 		}
 	}
 
 	/**
 	 * Test method for
 	 * {@link info.freelibrary.util.ClasspathUtils#getDirFiles(java.io.FilenameFilter)}
 	 * .
 	 */
 	@Test
 	public void testGetDirFilesFilenameFilter() {
		RegexDirFilter filter = new RegexDirFilter("(.*)classes");
 		int count = ClasspathUtils.getDirFiles(filter).length;
 
		if (count != 2) {
			fail("Expected to find 2 matches for regex but found " + count);
 		}
 	}
 
 	/**
 	 * Test method for {@link info.freelibrary.util.ClasspathUtils#getJars()}.
 	 */
 	@Test
 	public void testGetJars() {
 		for (String jarName : ClasspathUtils.getJars()) {
 			if (!jarName.endsWith(".jar")) {
 				fail(jarName + " isn't a jar file (or has the wrong extension)");
 			}
 		}
 	}
 
 	/**
 	 * Test method for
 	 * {@link info.freelibrary.util.ClasspathUtils#getJars(java.io.FilenameFilter)}
 	 * .
 	 */
 	@Test
 	public void testGetJarsFilenameFilter() {
 		RegexFileFilter filter = new RegexFileFilter("^junit(.*)$");
 
 		if (ClasspathUtils.getJars(filter).length != 1) {
 			fail("Should have found one and only one junit jar file");
 		}
 	}
 
 	/**
 	 * Test method for
 	 * {@link info.freelibrary.util.ClasspathUtils#getJarFiles()}.
 	 */
 	@Test
 	public void testGetJarFiles() {
 		try {
 			for (JarFile jarFile : ClasspathUtils.getJarFiles()) {
 				if (!jarFile.getName().endsWith(".jar")) {
 					fail(jarFile.getName() + " is not a jar file");
 				}
 			}
 		}
 		catch (IOException details) {
 			fail(details.getMessage());
 		}
 	}
 
 	/**
 	 * Test method for
 	 * {@link info.freelibrary.util.ClasspathUtils#getJarFiles(java.io.FilenameFilter)}
 	 * .
 	 */
 	@Test
 	public void testGetJarFilesFilenameFilter() {
 		try {
 			RegexFileFilter filter = new RegexFileFilter("^junit(.*)$");
 			JarFile[] jars = ClasspathUtils.getJarFiles(filter);
 
 			if (jars.length < 1) {
 				fail("Failed to find junit jar file using regexp filter");
 			}
 		}
 		catch (IOException details) {
 			fail(details.getMessage());
 		}
 	}
 
 	/**
 	 * Test method for
 	 * {@link info.freelibrary.util.ClasspathUtils#findFirst(java.lang.String)}.
 	 */
 	@Test
 	public void testFindFirst() {
 		InputStream inStream;
 
 		try {
 			inStream = ClasspathUtils.findFirst("Messages.xml");
 
 			// Looking for a file at the root of a dir in the classpath
 			if (inStream == null || inStream.available() < 1) {
 				fail("Didn't find Messages.xml like it should");
 			}
 
 			inStream = ClasspathUtils
 					.findFirst("info/freelibrary/xq/Put.class");
 
 			// Looking for a class file that is buried in a dir structure
 			if (inStream == null || inStream.available() < 1) {
 				fail("Didn't find info/freelibrary/xq/Put.class like it should");
 			}
 
 			inStream = ClasspathUtils.findFirst("SOMETHING_NOT_FOUND");
 
 			// Looking for something that doesn't exist
 			if (inStream != null) {
 				fail("Found SOMETHING_NOT_FOUND when it shouldn't have");
 			}
 
 			inStream = ClasspathUtils.findFirst("META-INF/MANIFEST.MF");
 
 			// Looking inside a jar file for its manifest
 			if (inStream == null || inStream.available() < 1) {
 				fail("Didn't find META-INF/MANIFEST.MF like it should");
 			}
 		}
 		catch (IOException details) {
 			fail(details.getMessage());
 		}
 	}
 
 }
