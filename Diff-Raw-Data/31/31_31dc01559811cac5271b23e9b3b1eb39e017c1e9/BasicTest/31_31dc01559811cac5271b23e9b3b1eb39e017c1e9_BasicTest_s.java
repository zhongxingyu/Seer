 /**
  * 
  */
 package pl.psnc.dl.wf4ever.dlibra;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Date;
 import java.util.Properties;
 
 import junit.framework.Assert;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import pl.psnc.dl.wf4ever.common.HibernateUtil;
 import pl.psnc.dl.wf4ever.common.ResearchObject;
 import pl.psnc.dl.wf4ever.common.UserProfile;
 import pl.psnc.dl.wf4ever.dlibra.helpers.DLibraDataSource;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 
 /**
  * @author piotrhol
  * 
  */
 public class BasicTest {
 
     private static final String MAIN_FILE_MIME_TYPE = "text/plain";
 
     private static final String MAIN_FILE_CONTENT = "test";
 
     private static final String MAIN_FILE_PATH = "mainFile.txt";
 
     private String host;
 
     private int port;
 
     private long workspacesDirectory;
 
     private long collectionId;
 
     private static final String USERNAME = "John Doe";
 
     private static final String ADMIN_ID = "wfadmin";
 
     private static final String ADMIN_PASSWORD = "wfadmin!!!";
 
     private static final String USER_ID = "test-" + new Date().getTime();
 
     private static final String USER_PASSWORD = "password";
 
     private static final URI RO_URI = URI.create("http://example.org/ROs/foobar/");
 
     private ResearchObject ro;
 
 
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp()
             throws Exception {
         HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
         InputStream inputStream = BasicTest.class.getClassLoader().getResourceAsStream("connection.properties");
         Properties properties = new Properties();
         properties.load(inputStream);
         host = properties.getProperty("host");
         port = Integer.parseInt(properties.getProperty("port"));
         workspacesDirectory = Long.parseLong(properties.getProperty("workspacesDir"));
         collectionId = Long.parseLong(properties.getProperty("collectionId"));
 
         ro = ResearchObject.create(RO_URI);
     }
 
 
     @After
     public void tearDown() {
         try {
             DigitalLibrary dl = new DLibraDataSource(host, port, workspacesDirectory, collectionId, USER_ID,
                     USER_PASSWORD);
             dl.deleteResearchObject(ro);
         } catch (Exception e) {
 
         }
         try {
             DigitalLibrary dlA = new DLibraDataSource(host, port, workspacesDirectory, collectionId, ADMIN_ID,
                     ADMIN_PASSWORD);
             dlA.deleteUser(USER_ID);
         } catch (Exception e) {
 
         }
         HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.dlibra.helpers.DLibraDataSource#createVersion(java.lang.String, java.lang.String, java.lang.String, java.net.URI)}
      * .
      * 
      * @throws DigitalLibraryException
      * @throws ConflictException
      * @throws NotFoundException
      * @throws IOException
      * @throws AccessDeniedException
      */
     @Test
     public final void testCreateVersionStringStringStringURI()
             throws DigitalLibraryException, NotFoundException, ConflictException, IOException, AccessDeniedException {
         DigitalLibrary dlA = new DLibraDataSource(host, port, workspacesDirectory, collectionId, ADMIN_ID,
                 ADMIN_PASSWORD);
         assertTrue(dlA.createUser(USER_ID, USER_PASSWORD, USERNAME));
         DigitalLibrary dl = new DLibraDataSource(host, port, workspacesDirectory, collectionId, USER_ID, USER_PASSWORD);
         dl.createResearchObject(ro, new ByteArrayInputStream(MAIN_FILE_CONTENT.getBytes()), MAIN_FILE_PATH,
             MAIN_FILE_MIME_TYPE);
         InputStream in = dl.getFileContents(ro, MAIN_FILE_PATH);
         try {
             String file = IOUtils.toString(in);
             assertEquals("Manifest is properly saved", MAIN_FILE_CONTENT, file);
         } finally {
             in.close();
         }
     }
 
 
     @Test
     public final void testGetUserProfile()
             throws DigitalLibraryException, IOException, NotFoundException {
         DigitalLibrary dlA = new DLibraDataSource(host, port, workspacesDirectory, collectionId, ADMIN_ID,
                 ADMIN_PASSWORD);
         assertTrue(dlA.createUser(USER_ID, USER_PASSWORD, USERNAME));
         assertFalse(dlA.createUser(USER_ID, USER_PASSWORD, USERNAME));
         DigitalLibrary dl = new DLibraDataSource(host, port, workspacesDirectory, collectionId, USER_ID, USER_PASSWORD);
         UserProfile user = dl.getUserProfile();
         Assert.assertEquals("User login is equal", USER_ID, user.getLogin());
         Assert.assertEquals("User name is equal", USERNAME, user.getName());
     }
 
 
     @Test
     public final void testCreateDuplicateVersion()
             throws DigitalLibraryException, IOException, ConflictException, AccessDeniedException {
         DigitalLibrary dlA = new DLibraDataSource(host, port, workspacesDirectory, collectionId, ADMIN_ID,
                 ADMIN_PASSWORD);
         dlA.createUser(USER_ID, USER_PASSWORD, USERNAME);
         DigitalLibrary dl = new DLibraDataSource(host, port, workspacesDirectory, collectionId, USER_ID, USER_PASSWORD);
         dl.createResearchObject(ro, new ByteArrayInputStream(MAIN_FILE_CONTENT.getBytes()), MAIN_FILE_PATH,
             MAIN_FILE_MIME_TYPE);
         try {
             dl.createResearchObject(ro, new ByteArrayInputStream(MAIN_FILE_CONTENT.getBytes()), MAIN_FILE_PATH,
                 MAIN_FILE_MIME_TYPE);
             fail("Should throw conflict exception");
         } catch (ConflictException e) {
             // good
         } catch (Exception e) {
             fail("Threw a wrong exception: " + e.getClass().toString());
         }
     }
 
 
     @Test
     public final void testStoreAttributes()
             throws DigitalLibraryException, IOException, ConflictException, NotFoundException, AccessDeniedException {
         DigitalLibrary dlA = new DLibraDataSource(host, port, workspacesDirectory, collectionId, ADMIN_ID,
                 ADMIN_PASSWORD);
         dlA.createUser(USER_ID, USER_PASSWORD, USERNAME);
         DigitalLibrary dl = new DLibraDataSource(host, port, workspacesDirectory, collectionId, USER_ID, USER_PASSWORD);
         dl.createResearchObject(ro, new ByteArrayInputStream(MAIN_FILE_CONTENT.getBytes()), MAIN_FILE_PATH,
             MAIN_FILE_MIME_TYPE);
         Multimap<URI, Object> atts = HashMultimap.create();
         atts.put(URI.create("a"), "foo");
         atts.put(URI.create("a"), "bar");
         atts.put(URI.create("b"), "lorem ipsum");
         dl.storeAttributes(ro, atts);
     }
 }
