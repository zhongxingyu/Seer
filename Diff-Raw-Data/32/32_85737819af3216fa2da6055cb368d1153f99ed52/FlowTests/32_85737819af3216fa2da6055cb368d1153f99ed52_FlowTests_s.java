 /**
  * 
  */
 package pl.psnc.dl.wf4ever.dlibra;
 
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Date;
 import java.util.Properties;
 import java.util.Set;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import pl.psnc.dl.wf4ever.dlibra.helpers.DLibraDataSource;
 import pl.psnc.dlibra.metadata.Edition;
 import pl.psnc.dlibra.service.IdNotFoundException;
 
 /**
  * @author piotrek
  * 
  */
 public class FlowTests {
 
 	private static String host;
 
 	private static int port;
 
 	private static long workspacesDirectory;
 
 	private static long collectionId;
 
 	private static final String ADMIN_ID = "wfadmin";
 
 	private static final String ADMIN_PASSWORD = "wfadmin!!!";
 
	private static final String USER_ID = "test-" + new Date().getTime();
 
 	private static final String USER_PASSWORD = "password";
 
 	private DLibraDataSource dl;
 
 	private static final FileRecord[] files = new FileRecord[3];
 
 	private static final String[] directories = { "", "dir/", "testdir" };
 
 	private static final String w = "w";
 	private static final String r = "r";
 	private static final String v = "v";
 	private static final String v2 = "v2";
 
 	private static final URI versionURI = URI.create("http://example.com/workspaces/w/ros/r/v");
 	private static final URI versionURI2 = URI.create("http://example.com/workspaces/w/ros/r/v2");
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		InputStream inputStream = FlowTests.class.getClassLoader().getResourceAsStream("connection.properties");
 		Properties properties = new Properties();
 		properties.load(inputStream);
 		host = properties.getProperty("host");
 		port = Integer.parseInt(properties.getProperty("port"));
 		workspacesDirectory = Long.parseLong(properties.getProperty("workspacesDir"));
 		collectionId = Long.parseLong(properties.getProperty("collectionId"));
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		dl = new DLibraDataSource(host, port, workspacesDirectory, collectionId, ADMIN_ID, ADMIN_PASSWORD);
		dl.createUser(USER_ID, USER_PASSWORD);
		dl = new DLibraDataSource(host, port, workspacesDirectory, collectionId, USER_ID, USER_PASSWORD);
 		dl.createWorkspace("w");
 		dl.createResearchObject("w", "r");
 		dl.createVersion("w", "r", "v", versionURI);
 
 		files[0] = new FileRecord("file1.txt", "file1.txt", "text/plain");
 		files[1] = new FileRecord("file2.txt", "dir/file2.txt", "text/plain");
 		files[2] = new FileRecord("file3.jpg", "testdir/file3.jpg", "image/jpg");
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 		dl.deleteWorkspace("w");
 		dl = new DLibraDataSource(host, port, workspacesDirectory, collectionId, ADMIN_ID, ADMIN_PASSWORD);
		dl.deleteUser(USER_ID);
 	}
 
 	@Test
 	public final void testAddingResources() throws IdNotFoundException, DigitalLibraryException, IOException {
 		createOrUpdateFile(files[0]);
 		createOrUpdateFile(files[1]);
 		getZippedVersion();
 		getFileContent(files[0]);
 		getFileContent(files[1]);
		getZippedFolder(files[1].path);
 		createOrUpdateFile(files[0]);
 		createOrUpdateFile(files[1]);
 		createVersionAsCopy();
 		deleteFile(files[0].path);
 		deleteFile(files[1].path);
 		checkNoFile(files[0].path);
 		checkNoFile(files[1].path);
 	}
 
 	@Test
 	public final void testEmptyDirectory() throws IdNotFoundException, DigitalLibraryException, IOException {
 		createOrUpdateDirectory(directories[1]);
 		getZippedFolder(directories[1]);
 		createOrUpdateFile(files[1]);
 		deleteFile(files[1].path);
 		getZippedFolder(directories[1]);
 		deleteFile(directories[1]);
 		checkNoFile(directories[1]);
 	}
 
 	@Test
 	public final void testEditions() throws IdNotFoundException, DigitalLibraryException, IOException {
 		long edId1 = createEdition();
 		createOrUpdateFile(files[0]);
 		createOrUpdateFile(files[1]);
 		checkNoEditionPublished();
 		publishEdition();
 		checkPublished(edId1);
 		long edId2 = createEdition();
 		createOrUpdateFile(files[2]);
 		deleteFile(files[0].path);
 		getFileContent(files[1]);
 		getFileContent(files[2]);
 		checkNoFile(files[0].path);
 		getFileContent(files[0], edId1);
		getFileContent(files[2], edId1);
		checkNoFile(files[0].path, edId1);
 		checkPublished(edId1);
 		publishEdition();
 		checkPublished(edId2);
 		unpublishEdition();
 		checkNoEditionPublished();
 	}
 
 	private void unpublishEdition() throws IdNotFoundException, DigitalLibraryException {
 		dl.unpublishVersion(w, r, v);
 	}
 
 	private void publishEdition() throws IdNotFoundException, DigitalLibraryException {
 		dl.publishVersion(w, r, v);
 	}
 
 	private void checkPublished(long edId) throws IdNotFoundException, DigitalLibraryException {
 		Set<Edition> eds = dl.getEditionList(w, r, v);
 		for (Edition ed : eds) {
 			if (ed.getId().getId() == edId) {
 				assertTrue("Edition should be published", ed.isPublished());
 			} else {
 				assertFalse("No edition should be published", ed.isPublished());
 			}
 		}
 	}
 
 	private void checkNoEditionPublished() throws IdNotFoundException, DigitalLibraryException {
 		Set<Edition> eds = dl.getEditionList(w, r, v);
 		for (Edition ed : eds) {
 			assertFalse("No edition should be published", ed.isPublished());
 		}
 	}
 
 	private long createEdition() throws IdNotFoundException, DigitalLibraryException {
 		return dl.createEdition(w, v, r, v).getId();
 	}
 
 	private void checkNoFile(String path) throws DigitalLibraryException, IdNotFoundException {
 		try {
 			dl.getFileContents(w, r, v, path);
 			fail("Deleted file doesn't throw IdNotFoundException");
 		} catch (IdNotFoundException e) {
 			// good
 		}
 	}
 
 	private void checkNoFile(String path, long edId) throws DigitalLibraryException, IdNotFoundException {
 		try {
 			dl.getFileContents(w, r, v, path, edId);
 			fail("Deleted file doesn't throw IdNotFoundException");
 		} catch (IdNotFoundException e) {
 			// good
 		}
 	}
 
 	private void deleteFile(String path) throws DigitalLibraryException, IdNotFoundException {
 		dl.deleteFile(versionURI, w, r, v, path);
 	}
 
 	private void createVersionAsCopy() throws DigitalLibraryException, IdNotFoundException {
 		dl.createVersion(w, r, v2, versionURI2);
 	}
 
 	private void getZippedFolder(String path) throws DigitalLibraryException, IdNotFoundException, IOException {
 		InputStream zip = dl.getZippedFolder(w, r, v, path);
 		assertNotNull(zip);
 		zip.close();
 	}
 
 	private void getFileContent(FileRecord file, long edId) throws IdNotFoundException, DigitalLibraryException,
 			IOException {
 		InputStream f = dl.getFileContents(w, r, v, file.path, edId);
 		assertNotNull(f);
 		f.close();
		assertEquals(files[0].mimeType, dl.getFileMimeType(w, r, v, file.path));
 	}
 
 	private void getFileContent(FileRecord file) throws DigitalLibraryException, IdNotFoundException, IOException {
 		InputStream f = dl.getFileContents(w, r, v, file.path);
 		assertNotNull(f);
 		f.close();
		assertEquals(files[0].mimeType, dl.getFileMimeType(w, r, v, file.path));
 	}
 
 	private void getZippedVersion() throws DigitalLibraryException, IdNotFoundException {
 		InputStream zip1 = dl.getZippedVersion(w, r, v);
 		assertNotNull(zip1);
 	}
 
 	private void createOrUpdateFile(FileRecord file) throws DigitalLibraryException, IdNotFoundException, IOException {
		InputStream f = files[0].open();
 		ResourceInfo r1 = dl.createOrUpdateFile(versionURI, w, r, v, file.path, f, file.mimeType);
 		f.close();
 		assertNotNull(r1);
 	}
 
 	private void createOrUpdateDirectory(String path) throws DigitalLibraryException, IdNotFoundException, IOException {
 		ResourceInfo r1 = dl.createOrUpdateFile(versionURI, w, r, v, path, new ByteArrayInputStream(new byte[0]),
 				"text/plain");
 		assertNotNull(r1);
 	}
 
 	private class FileRecord {
 		public String name;
 		public String path;
 		public String mimeType;
 
 		/**
 		 * @param name
 		 * @param dir
 		 * @param path
 		 */
 		public FileRecord(String name, String path, String mimeType) {
 			this.name = name;
 			this.path = path;
 			this.mimeType = mimeType;
 		}
 
 		public InputStream open() {
 			return this.getClass().getClassLoader().getResourceAsStream(name);
 		}
 	}
 
 }
