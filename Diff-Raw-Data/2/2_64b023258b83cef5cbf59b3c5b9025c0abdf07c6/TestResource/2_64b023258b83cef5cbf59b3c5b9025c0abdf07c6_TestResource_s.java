 package net.meisen.general.genmisc;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import net.meisen.general.genmisc.resources.Resource;
 import net.meisen.general.genmisc.resources.ResourceInfo;
 import net.meisen.general.genmisc.types.Files;
 
 /**
  * Tests used to test the implemented functionality of the {@link Resource}
  * class.
  * 
  * @author pmeisen
  * 
  */
 public class TestResource {
 	private static final String RESOURCE_DIR = "resources";
 
 	// the working directory used for the test
 	private static String workingDir;
 	private static String resourceDir = Files.getCanonicalPath(".."
 			+ File.separatorChar + RESOURCE_DIR);
 	private static FileManager fileManager = new FileManager();
 	private static String testFileName = "dummy.txt";
 
 	/**
 	 * Helper class to modify the ClassLoader on runtime
 	 * 
 	 * @author pmeisen
 	 */
 	public class DynamicURLClassLoader extends URLClassLoader {
 
 		/**
 		 * Default constructor to be used.
 		 * 
 		 * @param classLoader
 		 *          the <code>URLClassLoader</code> to take the URLs from
 		 */
 		public DynamicURLClassLoader(final ClassLoader classLoader) {
 			super(new URL[0], null);
 
 			if (classLoader instanceof URLClassLoader) {
 				final URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
 				for (final URL url : urlClassLoader.getURLs()) {
 					addURL(url);
 				}
 			}
 		}
 
 		@Override
 		public void addURL(final URL url) {
 			super.addURL(url);
 		}
 	}
 
 	/**
 	 * Initializes the resources available
 	 */
 	@BeforeClass
 	public static void init() {
 
 		// set the working directory and print it
 		workingDir = Files.getCanonicalPath(System.getProperty("user.dir"));
 
 		// print the working directory
 		System.out.println("The working directory is: " + workingDir);
 		System.out.println("- The following directory must be on the "
 				+ "class-path: " + resourceDir);
 		System.out.println("- The working directory cannot be write protected");
 	}
 
 	/**
 	 * Checks the {@link Resource#isPathResolvable(String)} functionality
 	 */
 	@Test
 	public void testIsPathResolvable() {
 		Boolean isPath;
 
 		// check a path on the class-path
 		isPath = Resource.isPathResolvable("./net/meisen/general/gendummy");
 		assertEquals(isPath, true);
 
 		// check a path on the file-system
 		isPath = Resource.isPathResolvable("C:\\");
 		assertEquals(isPath, true);
 
 		// check a not existing path
 		isPath = Resource.isPathResolvable("notexistingdir");
 		assertEquals(isPath, false);
 	}
 
 	/**
 	 * Checks the functionality of the implementation of
 	 * {@link Resource#resolvePath(String)}
 	 */
 	@Test
 	public void testResolvePath() {
 		String resolved;
 
 		resolved = Resource.resolvePath("./net/meisen/general/gendummy");
 		assertEquals(resolved.endsWith("net/meisen/general/gendummy/"), true);
 
 		resolved = Resource.resolvePath(workingDir);
 		assertEquals(resolved, workingDir);
 		resolved = Resource.resolvePath("notexistingdir");
 		assertEquals(resolved, null);
 	}
 
 	/**
 	 * Check the {@link Resource#resolveResource(String)} functionality
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void testResolveResource() throws IOException {
 		String path;
 
 		// generate a sample file in the working directory
 		final File file = fileManager.createFile(workingDir);
 
 		/*
 		 * check the resolution of a relative path
 		 */
 		path = Resource.resolveResource("./" + file.getName());
 		assertEquals(path, workingDir + file.getName());
 
 		/*
 		 * a relative path should be resolved to a class-path if not available on
 		 * file-system
 		 */
 		path = Resource
 				.resolveResource(".\\net\\meisen\\general\\gendummy\\dummy.txt");
 		assertEquals(path.endsWith("net/meisen/general/gendummy/dummy.txt"), true);
 
 		/*
 		 * check the resolution of an absolute path
 		 */
 		path = Resource.resolveResource(workingDir + file.getName());
 		assertEquals(path, workingDir + file.getName());
 
 		/*
 		 * check the resolution of a file in a jar
 		 */
 		path = Resource
 				.resolveResource("./net\\meisen/general/gendummy/dummy2.txt");
 		assertEquals(path, null);
 
 		path = Resource.resolveResource("./net/meisen/general/gendummy/dummy.txt");
 		assertEquals(path.endsWith("net/meisen/general/gendummy/dummy.txt"), true);
 	}
 
 	/**
 	 * Check the {@link Resource#hasResource(String)} functionality
 	 * 
 	 * @throws IOException
 	 *           if a test-file cannot be created
 	 */
 	@Test
 	public void testHasResource() throws IOException {
 		Boolean hasResource = null;
 
 		// generate a sample file in the working directory
 		final File file = fileManager.createFile(workingDir);
 
 		/*
 		 * check the resolution of a relative path
 		 */
 		hasResource = Resource.hasResource("./" + file.getName());
 		assertEquals(hasResource, true);
 
 		/*
 		 * check the resolution of a relative path
 		 */
 		hasResource = Resource.hasResource("C:\\notexistingfile");
 		assertEquals(hasResource, false);
 
 		/*
 		 * a relative path should be resolved to a class-path if not available on
 		 * file-system
 		 */
 		hasResource = Resource
 				.hasResource(".\\net\\meisen\\general\\gendummy\\Dummy.java");
 		assertEquals(hasResource, false);
 
 		/*
 		 * check the resolution of an absolute path
 		 */
 		hasResource = Resource.hasResource(workingDir + file.getName());
 		assertEquals(hasResource, true);
 
 		/*
 		 * create a file on the class-path and try to get it
 		 */
 		// create the file
 		final File f = fileManager.createFile(resourceDir);
 
 		// search the file on the class-path
 		hasResource = Resource.hasResource(".\\" + f.getName());
 
 		// check the result
 		assertEquals(hasResource, true);
 
 		/*
 		 * check a file within a jar
 		 */
 		hasResource = Resource
 				.hasResource("./net/meisen/general/gendummy/dummy.txt");
 		assertEquals(hasResource, true);
 
 		/*
 		 * check a file within a jar with a weird path
 		 */
 		hasResource = Resource
 				.hasResource("./net/meisen/../meisen/general/gendummy/dummy.txt");
 		assertEquals(hasResource, true);
 
 		/*
 		 * check a file which doesn't exists at all
 		 */
 		hasResource = Resource
 				.hasResource("./net\\meisen/general/gendummy/notexist.txt");
 		assertEquals(hasResource, false);
 	}
 
 	/**
 	 * Checks to retrieve a specific resource as stream
 	 */
 	@Test
 	public void testResourceAsStream() {
 		InputStream is;
 
 		// get an input-stream for a relative resource on class-path
 		is = Resource
 				.getResourceAsStream("./net\\meisen/general/gendummy/dummy.txt");
 		assertEquals(is != null, true);
 
 		// get an input-stream for a relative resource on class-path
 		is = Resource
 				.getResourceAsStream("./net\\meisen/general/gendummy/dummy.txt");
 		assertEquals(is != null, true);
 	}
 
 	/**
 	 * Tests the availability of resources
 	 * 
 	 * @throws IOException
 	 *           if a test-file cannot be created
 	 */
 	@Test
 	public void testGetAvailableResources() throws IOException {
 		Collection<String> files;
 
 		// generate a sample file in the working directory
 		final File file = fileManager.createFile(workingDir);
 
 		/*
 		 * check the working directory
 		 */
 		files = Resource.getAvailableResources("./");
 		assertEquals(files.contains(file.getName()), true);
 
 		/*
 		 * check not existing directory
 		 */
 		files = Resource.getAvailableResources("C:\\notexistingfolder");
 		assertEquals(files, null);
 
 		/*
 		 * lets create something on the classpath and check for it
 		 */
 		final File d = fileManager.createDir(workingDir);
 
 		// create ten files
 		final ArrayList<String> sampleFiles = new ArrayList<String>();
 		for (int i = 0; i < 10; i++) {
 
 			// create the file
 			final File f = fileManager.createFile(d.getAbsolutePath());
 			sampleFiles.add(f.getName());
 		}
 
 		// create a sub-directory which should not be added
 		final File sd = fileManager.createDir(d.getAbsolutePath());
 		for (int i = 0; i < 10; i++) {
 
 			// create some files
 			fileManager.createFile(sd.getAbsolutePath());
 		}
 
 		// get the results
 		files = Resource.getAvailableResources(".\\" + d.getName());
 
 		// now check the results
 		assertEquals(files.size(), sampleFiles.size());
 		for (final String sampleFile : sampleFiles) {
 			assertEquals(files.contains(sampleFile), true);
 		}
 
 		/*
 		 * lets check something within a jar file
 		 */
 		files = Resource.getAvailableResources("./net\\meisen/general/gendummy/");
 		assertEquals(files.size(), 2);
 		assertEquals(files.contains(testFileName), true);
 		assertEquals(files.contains("Dummy.class"), true);
 
 		/*
 		 * lets check if we pass a "file" (i.e. without ending "/")
 		 */
 		files = Resource.getAvailableResources("net/meisen/general/gendummy");
 		assertEquals(files.size(), 2);
 		assertEquals(files.contains(testFileName), true);
 		assertEquals(files.contains("Dummy.class"), true);
 		/*
 		 * lets check if we pass an invalid location
 		 */
 		files = Resource
 				.getAvailableResources("net/meisen/general/gendummy/dummy.txt");
 		assertEquals(files, null);
 
 		/*
 		 * lets use a resolved path for a resource
 		 */
 		files = Resource.getAvailableResources(Resource
 				.resolvePath("./net/meisen/general/gendummy"));
 		assertEquals(files.size(), 2);
 		assertEquals(files.contains(testFileName), true);
 		assertEquals(files.contains("Dummy.class"), true);
 
 		/*
 		 * lets use a resolved path for a resource
 		 */
 		files = Resource.getAvailableResources("./net/meisen/general/gendummy",
 				true);
 		assertEquals(files.size(), 7);
 		assertEquals(files.contains("moredummy/"), true);
 		assertEquals(files.contains("moredummy2/"), false);
 		assertEquals(files.contains("moredummy/dummy1.txt"), true);
 		assertEquals(files.contains("moredummy/dummy2.txt"), true);
 		assertEquals(files.contains("moredummy/dummy3.txt"), false);
 		assertEquals(files.contains(testFileName), true);
 		assertEquals(files.contains("Dummy.class"), true);
 		assertEquals(files.contains("gt me  tst/"), true);
 		assertEquals(
 				files.contains("gt me  tst/a fle with special chrcters.txt"),
 				true);
 
 		// generate a sample file in the working directory
 		final File dir = fileManager.createDir(workingDir);
 		final File subDir1 = fileManager.createDir(dir.getAbsolutePath());
 		final File subDir2 = fileManager.createDir(dir.getAbsolutePath());
 		final File subSubDir1 = fileManager.createDir(subDir1.getAbsolutePath());
 		final File subFile1 = fileManager.createFile(subDir1.getAbsolutePath());
 		final File subFile2 = fileManager.createFile(subDir1.getAbsolutePath());
 		files = Resource.getAvailableResources(dir.getAbsolutePath(), true);
 
 		assertEquals(files.size(), 5);
 		final String subDir1Name = subDir1.getName() + File.separatorChar;
 		final String subDir2Name = subDir2.getName() + File.separatorChar;
 		final String subSubDir1Name = subDir1Name + subSubDir1.getName()
 				+ File.separatorChar;
 		final String subFile1Name = subDir1Name + subFile1.getName();
 		final String subFile2Name = subDir1Name + subFile2.getName();
 		assertEquals(files.contains(subDir1Name), true);
 		assertEquals(files.contains(subDir2Name), true);
 		assertEquals(files.contains(subSubDir1Name), true);
 		assertEquals(files.contains(subFile1Name), true);
 		assertEquals(files.contains(subFile2Name), true);
 	}
 
 	/**
 	 * Tests the retrieval of resources fromt he classpath
 	 * 
 	 * @throws IOException
 	 *           if a file cannot be read or written
 	 */
 	@Test
 	public void testGetResourcesFromClasspath() throws IOException {
 
 		// create a new resource
 		final File dir = fileManager.createDir(resourceDir);
 		final File dummyRes = new File(resourceDir, testFileName);
 		dummyRes.createNewFile();
 		final File dummySubRes = new File(dir, testFileName);
 		dummySubRes.createNewFile();
 
 		// we also need the test-jar file from the class-path
 		final String pathSep = System.getProperty("path.separator");
 		final String classPath = System.getProperty("java.class.path", ".");
 		final String[] classPathElements = classPath.split(pathSep);
 		File jarTestFile = null;
 		for (final String element : classPathElements) {
 			final File posFile = new File(element);
			if ("net-meisen-general-gen-dummy-1.0.0-SNAPSHOT.jar".equals(posFile
 					.getName())) {
 				jarTestFile = posFile;
 				break;
 			}
 		}
 
 		// check the resources
 		final Collection<ResourceInfo> files = Resource.getResources(testFileName,
 				true, false);
 		// assertEquals("Expected to find exactly 4 files.", 4, files.size());
 		assertEquals("The dummyRes '" + dummyRes.getCanonicalPath()
 				+ "' was not found.",
 				files.contains(new ResourceInfo(dummyRes.getCanonicalPath(), true)),
 				true);
 		assertEquals("The dummySubRes '" + dummySubRes.getCanonicalPath()
 				+ "' was not found.",
 				files.contains(new ResourceInfo(dummySubRes.getCanonicalPath(), true)),
 				true);
 		assertEquals(
 				"The resource '"
 						+ testFileName
 						+ "' was not found in the jarTestFile '"
 						+ jarTestFile.getCanonicalPath()
 						+ " (ResInfo: "
 						+ new ResourceInfo(testFileName, jarTestFile.getCanonicalPath(),
 								true) + ")", files.contains(new ResourceInfo(testFileName,
 						jarTestFile.getCanonicalPath(), true)), true);
 		assertEquals("The file in the subdirectories of the jarTestFile '"
 				+ jarTestFile.getCanonicalPath() + "' was not found",
 				files.contains(new ResourceInfo(
 						"net/meisen/general/gendummy/dummy.txt", jarTestFile
 								.getCanonicalPath(), true)), true);
 
 		// delete the dummy file now
 		dummyRes.delete();
 	}
 
 	/**
 	 * Tests the retrieval of resources fromt he classpath
 	 * 
 	 * @throws IOException
 	 *           if a file cannot be read or written
 	 */
 	@Test
 	public void testGetResourcesFromWorkingDir() throws IOException {
 
 		// create a new resource
 		final File dummyRes = new File(workingDir, testFileName);
 		dummyRes.createNewFile();
 		final File dir = fileManager.createDir(workingDir);
 		final File dummySubRes = new File(dir, testFileName);
 		dummySubRes.createNewFile();
 
 		// check the resources
 		final Collection<ResourceInfo> files = Resource.getResources(testFileName,
 				false, true);
 
 		assertEquals(files.size(), 2);
 		assertEquals(
 				files.contains(new ResourceInfo(dummyRes.getCanonicalPath(), true)),
 				true);
 		assertEquals(
 				files.contains(new ResourceInfo(dummySubRes.getCanonicalPath(), true)),
 				true);
 
 		// delete the dummy file now
 		dummyRes.delete();
 	}
 
 	/**
 	 * Modify the classpath and look for a resource on it
 	 * 
 	 * @throws Exception
 	 *           if the classpath cannot be modified
 	 */
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testWithModifiedClassPath() throws Exception {
 		Collection<ResourceInfo> resources;
 
 		// we need some new files for the test
 		final String tmpSysDir = System.getProperty("java.io.tmpdir");
 		final FileManager fileManager = new FileManager();
 		final File tmpDir = fileManager.createDir(tmpSysDir);
 		final File tmpFile = fileManager.createFile(tmpDir.toString());
 
 		// create a new ClassLoader
 		final DynamicURLClassLoader newClassLoader = new DynamicURLClassLoader(
 				getClass().getClassLoader());
 		newClassLoader.addURL(tmpDir.toURI().toURL());
 
 		final Class<?> clazz = newClassLoader.loadClass(Resource.class.getName());
 		final Method m1 = clazz.getMethod("getResources", String.class,
 				boolean.class, boolean.class);
 
 		resources = (Collection<ResourceInfo>) m1.invoke(null, tmpFile.getName(),
 				true, false);
 		assertEquals(resources.size(), 1);
 
 		final Method m2 = clazz.getMethod("getResources", Pattern.class,
 				boolean.class, boolean.class);
 		final Pattern pattern = Pattern.compile(tmpFile.getName().substring(0, 5)
 				+ ".+");
 		resources = (Collection<ResourceInfo>) m2
 				.invoke(null, pattern, true, false);
 		assertEquals(resources.size(), 1);
 
 		// cleanUp
 		fileManager.cleanUp();
 	}
 
 	/**
 	 * Cleans up after the tests
 	 */
 	@AfterClass
 	public static void cleanUp() {
 
 		// lets clean up for the test
 		final List<File> listWorkingDir = Files.getFilelist(new File(workingDir),
 				null, testFileName);
 		Files.bulkDeleteFiles(listWorkingDir);
 		final List<File> listResourceDir = Files.getFilelist(new File(resourceDir),
 				null, testFileName);
 		Files.bulkDeleteFiles(listResourceDir);
 
 		// cleanup
 		fileManager.cleanUp();
 	}
 }
