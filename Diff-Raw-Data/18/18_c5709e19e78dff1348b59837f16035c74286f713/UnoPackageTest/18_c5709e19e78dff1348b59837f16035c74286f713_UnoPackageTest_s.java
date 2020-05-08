 /*************************************************************************
  * UnoPackageTest.java
  *
  * The Contents of this file are made available subject to the terms of
  * either of the GNU Lesser General Public License Version 2.1
  * 
  * GNU Lesser General Public License Version 2.1
  * =============================================
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1, as published by the Free Software Foundation.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
  * MA 02111-1307 USA
  *
  * Contributor(s): oliver.boehm@agentes.de
  ************************************************************************/
 
 package org.openoffice.plugin.core.model;
 
 import static org.junit.Assert.*;
 
 import java.io.*;
 import java.util.*;
 import java.util.zip.*;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.*;
 
 /**
  * This is the first JUnit test class for the ooo-plugin-package-core project.
  * 
  * @author oliver (oliver.boehm@agentes.de)
  * @since 0.0.1 (16.08.2010)
  */
 public final class UnoPackageTest {
 	
 	private static final Log log = LogFactory.getLog(UnoPackageTest.class);
 	private static final String[] filenames = { "README", "hello.properties",
 			"CVS/Root", "de/dialog.xlb", "de/CVS/Root", "de/CVS/Templates",
 			"config/Addon.xcu", "lib/main.jar", "description.xml" };
 	private static File tmpDir;
 	private File tmpFile;
 	private UnoPackage pkg;
 	
 	/**
 	 * Here we create a tmpdir with some files for testing.
 	 *
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@BeforeClass
 	public static void setUpTmpDir() throws IOException {
 		tmpDir = new File(System.getProperty("java.io.tmpdir", "/tmp"), "oxttest" + System.currentTimeMillis());
 		assertTrue("can't create " + tmpDir, tmpDir.mkdir());
 		assertTrue(tmpDir + " is not a directory", tmpDir.isDirectory());
 		for (int i = 0; i < filenames.length; i++) {
 			addToTmpDir(filenames[i], filenames[i] + " created at " + new Date());
 		}
 		log.info(tmpDir + " with " + filenames.length + " files created");
 		Locale.setDefault(Locale.ENGLISH);
 		log.info("Locale=" + Locale.getDefault());
 	}
 	
 	private static void addToTmpDir(final String filename, final String content) throws IOException {
 		FileUtils.writeStringToFile(new File(tmpDir, filename), content);
 	}
 
 	/**
 	 * Creates a tmp file for testing.
 	 *
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@Before
 	public void setUp() throws IOException {
 		tmpFile = File.createTempFile("test", ".oxt");
 		log.info("using " + tmpFile + " for testing...");
 		pkg = new UnoPackage(tmpFile);
 	}
 	
 	/**
 	 * Here we delete the tmp file after testing.
 	 */
 	@After
 	public void tearDown() {
 		tmpFile.delete();
 		log.info(tmpFile + " is deleted.");
 	}
 	
 	/**
 	 * Here we delete the directory created for testing.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@AfterClass
	public static void tearDownTmpDir() throws IOException {
		FileUtils.deleteDirectory(tmpDir);
		log.info(tmpDir + " is deleted.");
 	}
 
 	/**
 	 * Test method for {@link UnoPackage#UnoPackage(java.io.File)}.
 	 *
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@Test
 	public void testUnoPackage() throws IOException {
 		pkg.close();
 		assertTrue(tmpFile.isFile());
 	}
 	
 	/**
 	 * Here we create just two testfiles and check if this will be part of the
 	 * created oxt file.
 	 *
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@Test
 	public void testAddContent() throws IOException {
 		pkg.addContent(tmpDir);
 		List<File> files = pkg.getContainedFiles();
 		assertEquals(filenames.length, files.size());
 		pkg.close();
 		checkUnoPackage();
 	}
 	
 	/**
 	 * A file added as "test//README" should be added as "test/README".
 	 *
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@Test
 	public void testAddContentNotNormalized() throws IOException {
 		pkg.addContent("test//README", new File(tmpDir + "//README"));
 		List<File> files = pkg.getContainedFiles();
 		assertEquals(1, files.size());
 		assertEquals(new File(tmpDir, "README"), files.get(0));
 		List<String> names = pkg.getContainedNames();
 		assertEquals(1, names.size());
		assertEquals(new File("test", "README").toString(), names.get(0));
 	}
 	
 	/**
 	 * Here we exclude all CVS files and check it if they are really excluded.
 	 *
 	 * @throws ZipException the zip exception
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@Test
 	public void testAddDirectory() throws ZipException, IOException {
 		String[] includes = {};
 		String[] excludes = { "CVS", "**/CVS", "README" };
 		pkg.addDirectory(tmpDir, includes, excludes);
 		List<File> files = pkg.getContainedFiles();
 		assertTrue("no files included", files.size() > 0);
 		for (File file : files) {
 			String dirname = file.getParentFile().getName();
 			String filename = file.getName();
 			assertFalse(file + " should be excluded!", dirname.equals("CVS")
 					|| filename.equals("README"));
 			log.info("contained file: " + file);
 		}
 		pkg.close();
 		checkUnoPackage();
 	}
 	
 	/**
 	 * The UnoPackage seems to delete an existing file "manifest.xml". This
 	 * test tries to reproduce it.
 	 * 
 	 * @throws ZipException the zip exception
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@Test
 	public void testCleanResources() throws ZipException, IOException {
 		File manifest = new File("manifest.xml");
 		assertFalse(manifest.getAbsolutePath(), manifest.exists());
 		FileUtils.writeStringToFile(manifest, "tempory created for testing");
 		assertTrue(manifest.getAbsolutePath(), manifest.exists());
 		pkg.addOtherFile("README", new File(tmpDir, "README"));
 		pkg.close();
 		assertTrue(manifest + " should be not deleted!", manifest.exists());
 		manifest.delete();
 		checkUnoPackage();
 	}
 	
     /**
      * Test manifest recognition. If there is already a manifest file available
      * this one should be used.
      *
      * @throws IOException Signals that an I/O exception has occurred.
      */
 	@Test
     public void testManifestRecognition() throws IOException {
         String manifestContent = "<Test-Manifest/>";
         addToTmpDir("META-INF/manifest.xml", manifestContent);
         pkg.addDirectory(tmpDir);
         pkg.close();
         checkUnoPackage();
         String content = getManifestContent();
         assertEquals(manifestContent, content);
     }
 	
 	/**
 	 * Here we test the generated manifest.
 	 *
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@Test
 	public void testGeneratedManifest() throws IOException {
 		File metaInfDir = new File(tmpDir, "META-INF");
 		FileUtils.deleteDirectory(metaInfDir);
 		File worldJar = new File("src/test/resources/sample/OOHelloWorld.jar");
 		assertTrue(worldJar + " not found", worldJar.exists());
 		FileUtils.copyFileToDirectory(worldJar, tmpDir);
         pkg.addDirectory(tmpDir);
 		pkg.close();
 		String manifest = getManifestContent();
 		log.info("manifest.xml:\n" + manifest);
 		assertTrue("Addon.xcu not found in manifest.xml", manifest.contains("Addon.xcu"));
 		assertTrue("main.jar not found in manifest.xml", manifest.contains("main.jar"));
 		assertTrue("description.xml not found in manifest.xml",
 				manifest.contains("description.xml"));
 		assertTrue(worldJar + " appear as 'uno-component'",
 				manifest.contains(worldJar.getName()));
 		String expectedManifest = getExpectedManifestContent();
 		assertEquals(expectedManifest, manifest);
 	}
 
     private String getManifestContent() throws ZipException, IOException {
         ZipFile zip = new ZipFile(tmpFile);
         try {
             ZipEntry entry = zip.getEntry("META-INF/manifest.xml");
             InputStream istream = zip.getInputStream(entry);
             return IOUtils.toString(istream);
         } finally {
             zip.close();
         }
     }
     
     private String getExpectedManifestContent() throws IOException {
     	InputStream istream = UnoPackageTest.class.getResourceAsStream("manifest.xml");
     	String content = IOUtils.toString(istream);
     	istream.close();
     	return content;
     }
     
     private void checkUnoPackage() throws ZipException, IOException {
 		boolean hasManifest = false;
         ZipFile zip = new ZipFile(tmpFile);
         Enumeration<? extends ZipEntry> entries = zip.entries();
 		while (entries.hasMoreElements()) {
 			ZipEntry entry = entries.nextElement();
 			log.debug("entry: " + entry);
 			String entryName = entry.getName();
 			if (entryName.equals("META-INF/manifest.xml")) {
 				hasManifest = true;
 			} else {
 				assertTrue(entry + " has wrong path",
 						ArrayUtils.contains(filenames, entryName));
 			}
 		}
 		assertTrue("no manifest inside", hasManifest);
 	}
     
     /**
      * Test method for {@link UnoPackage#hasRegistrationHandlerInside(File)}.
      */
     @Test
     public void testHasRegistrationHandlerInside() {
 		File worldJar = new File("src/test/resources/sample/OOHelloWorld.jar");
 		assertTrue(worldJar + " has RegistrationHandler.classes inside",
 				UnoPackage.hasRegistrationHandlerInside(worldJar));
     }
 
 }
