 /**
  * 
  */
 package pl.psnc.dl.wf4ever.fs;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Date;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import pl.psnc.dl.wf4ever.common.HibernateUtil;
 import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
 import pl.psnc.dl.wf4ever.dl.ConflictException;
 import pl.psnc.dl.wf4ever.dl.DigitalLibrary;
 import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dl.NotFoundException;
 import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
 import pl.psnc.dl.wf4ever.dl.UserMetadata;
 import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;
 
 /**
  * @author piotrek
  * 
  */
 public class FlowTests {
 
     private static final UserMetadata ADMIN = new UserMetadata("wfadmin", "John Doe", Role.ADMIN);
 
     private static final UserMetadata USER = new UserMetadata("test-" + new Date().getTime(), "test user",
             Role.AUTHENTICATED);
 
     private DigitalLibrary dl;
 
     private static final FileRecord[] files = new FileRecord[3];
 
     private static final String[] directories = { "", "dir/", "testdir" };
 
     private static final String MAIN_FILE_MIME_TYPE = "text/plain";
 
     private static final String MAIN_FILE_CONTENT = "test";
 
     private static final String MAIN_FILE_PATH = "mainFile.txt";
 
     private static final URI RO_URI = URI.create("http://example.org/ROs/foobar/");
 
     private static final String BASE = "/tmp/testdl/";
 
     private static final String USER_PASSWORD = "foo";
 
 
     /**
      * @throws java.lang.Exception
      */
     @BeforeClass
     public static void setUpBeforeClass()
             throws Exception {
     }
 
 
     /**
      * @throws java.lang.Exception
      */
     @AfterClass
     public static void tearDownAfterClass() {
     }
 
 
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp()
             throws Exception {
         HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
         dl = new FilesystemDL(BASE, ADMIN);
         dl.createUser(USER.getLogin(), USER_PASSWORD, USER.getName());
         dl = new FilesystemDL(BASE, USER);
         dl.createResearchObject(RO_URI, new ByteArrayInputStream(MAIN_FILE_CONTENT.getBytes()), MAIN_FILE_PATH,
             MAIN_FILE_MIME_TYPE);
 
        files[0] = new FileRecord("file1.txt", "file1.txt", "text/plain");
        files[1] = new FileRecord("file2.txt", "dir/file2.txt", "text/plain");
        files[2] = new FileRecord("file3.jpg", "testdir/file3.jpg", "image/jpg");
         HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
     }
 
 
     /**
      * @throws java.lang.Exception
      */
     @After
     public void tearDown()
             throws Exception {
         HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
         dl = new FilesystemDL(BASE, ADMIN);
         dl.deleteResearchObject(RO_URI);
         dl = new FilesystemDL(BASE, ADMIN);
         dl.deleteUser(USER.getLogin());
         HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
     }
 
 
     @Test
     public final void testAddingResources()
             throws DigitalLibraryException, IOException, NotFoundException, ConflictException, AccessDeniedException {
         createOrUpdateFile(files[0]);
         createOrUpdateFile(files[1]);
         getZippedVersion();
         getFileContent(files[0]);
         getFileContent(files[1]);
         checkFileExists(files[0].path);
         getZippedFolder(directories[1]);
         createOrUpdateFile(files[0]);
         createOrUpdateFile(files[1]);
         deleteFile(files[0].path);
         deleteFile(files[1].path);
         checkNoFile(files[0].path);
         checkNoFile(files[1].path);
     }
 
 
     @Test
     @Ignore
     public final void testEmptyDirectory()
             throws DigitalLibraryException, IOException, NotFoundException, AccessDeniedException {
         createOrUpdateDirectory(directories[1]);
         getZippedFolder(directories[1]);
         createOrUpdateFile(files[1]);
         deleteFile(files[1].path);
         getZippedFolder(directories[1]);
         deleteFile(directories[1]);
         checkNoFile(directories[1]);
     }
 
 
     @Test
     @Ignore
     public final void testPermissions()
             throws DigitalLibraryException, IOException, NotFoundException, ConflictException, AccessDeniedException {
         createOrUpdateFile(files[0]);
         createOrUpdateFile(files[1]);
         HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
         dl = new FilesystemDL(BASE, USER);
         HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
         getFileContent(files[0]);
         getFileContent(files[1]);
         checkCantCreateOrUpdateFile(files[0]);
         checkCantCreateOrUpdateFile(files[1]);
     }
 
 
     private void checkNoFile(String path)
             throws DigitalLibraryException, IOException {
         HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
         try {
             dl.getFileContents(RO_URI, path).close();
             fail("Deleted file doesn't throw IdNotFoundException");
         } catch (NotFoundException e) {
             // good
         } finally {
             HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
         }
     }
 
 
     private void checkFileExists(String path)
             throws DigitalLibraryException, NotFoundException {
         HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
         try {
             Assert.assertTrue(dl.fileExists(RO_URI, path));
         } finally {
             HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
         }
     }
 
 
     private void deleteFile(String path)
             throws DigitalLibraryException, NotFoundException {
         HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
         try {
             dl.deleteFile(RO_URI, path);
         } finally {
             HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
         }
     }
 
 
     private void getZippedFolder(String path)
             throws DigitalLibraryException, IOException, NotFoundException {
         HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
         try {
             InputStream zip = dl.getZippedFolder(RO_URI, path);
             assertNotNull(zip);
             zip.close();
         } finally {
             HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
         }
     }
 
 
     private void getFileContent(FileRecord file)
             throws DigitalLibraryException, IOException, NotFoundException, AccessDeniedException {
         HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
         try {
             InputStream f = dl.getFileContents(RO_URI, file.path);
             assertNotNull(f);
             f.close();
             assertEquals(file.mimeType, dl.getFileInfo(RO_URI, file.path).getMimeType());
         } finally {
             HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
         }
     }
 
 
     private void getZippedVersion()
             throws DigitalLibraryException, NotFoundException {
         HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
         try {
             InputStream zip1 = dl.getZippedResearchObject(RO_URI);
             assertNotNull(zip1);
         } finally {
             HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
         }
     }
 
 
     private void createOrUpdateFile(FileRecord file)
             throws DigitalLibraryException, IOException, NotFoundException, AccessDeniedException {
         HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
         try {
             InputStream f = file.open();
             ResourceMetadata r1 = dl.createOrUpdateFile(RO_URI, file.path, f, file.mimeType);
             f.close();
             assertNotNull(r1);
         } finally {
             HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
         }
     }
 
 
     private void checkCantCreateOrUpdateFile(FileRecord file)
             throws DigitalLibraryException, IOException, NotFoundException {
         HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
         InputStream f = file.open();
         try {
             dl.createOrUpdateFile(RO_URI, file.path, f, file.mimeType);
             fail("Should throw an exception when creating file");
         } catch (Exception e) {
             // good
         } finally {
             f.close();
             HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
         }
 
     }
 
 
     private void createOrUpdateDirectory(String path)
             throws DigitalLibraryException, IOException, NotFoundException, AccessDeniedException {
         HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
         try {
             ResourceMetadata r1 = dl.createOrUpdateFile(RO_URI, path, new ByteArrayInputStream(new byte[0]),
                 "text/plain");
             assertNotNull(r1);
         } finally {
             HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
         }
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
