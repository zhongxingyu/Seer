 package thredds.catalog;
 
 import org.junit.After;
 import java.util.Date;
 import org.junit.BeforeClass;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import javax.naming.NamingException;
 import org.junit.Ignore;
 import gov.usgs.cida.ncetl.utils.DatabaseUtil;
 import java.sql.Connection;
 import java.sql.Statement;
 import java.net.URI;
 import org.junit.Before;
 import gov.usgs.cida.ncetl.utils.FileHelper;
 import gov.usgs.webservices.jdbc.util.SqlUtils;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import org.apache.commons.io.FileUtils;
 import org.junit.Test;
 import static org.hamcrest.MatcherAssert.*;
 import static org.hamcrest.Matchers.*;
 
 /**
  *
  * @author Ivan Suftin <isuftin@usgs.gov>
  */
 public class CatalogHelperTest {
     public static String tempDir = FileHelper.getTempDirectory();
     public static File tempLocation;
     public static File dbTemp;
     public static String knownName = "Derpy Derpy Doo";
     public CatalogHelperTest() {
     }
 
     
     @BeforeClass
     public static void setUpAll() throws Exception {
         tempLocation = new File(tempDir + CatalogHelper.getDefaultCatalogFilename());
         tempLocation.deleteOnExit();
         
         dbTemp = new File(tempDir + "db" + new Date().getTime());
         dbTemp.deleteOnExit();
         String DB_URL = "jdbc:derby:" + dbTemp.getPath() + ";create=true";
         DatabaseUtil.setupDatabase(DB_URL, "org.apache.derby.jdbc.EmbeddedDriver");
         Connection connection = null;
         Statement stmt = null;
         try {
             connection = SqlUtils.getConnection("");
             stmt = connection.createStatement();
             String sql = "INSERT INTO catalogs (id, location, name) VALUES (13, '" + 
                     tempLocation.toURI() + "', 'testName')";
             stmt.execute(sql);
         }
         finally {
             stmt.close();
             SqlUtils.closeConnection(connection);
         }
     }
     
     @Before
     public void setUp() throws Exception {
         tempLocation = new File(tempDir + CatalogHelper.getDefaultCatalogFilename());
         tempLocation.deleteOnExit();
     }
 
     @After
     public void tearDown() throws Exception {
         if (tempLocation.exists()) FileUtils.forceDelete(tempLocation);
         //DatabaseUtil.shutdownDatabase("jdbc:derby:" + dbTemp.getPath() + ";shutdown=true");
     }
     
     @Test
     public void testSetupCatalogUsingDefaultCatalogName() throws URISyntaxException, FileNotFoundException, IOException {
         CatalogHelper.setupCatalog(tempLocation);
         assertThat(tempLocation.exists(), is(true));
         
         String catalog = FileUtils.readFileToString(tempLocation);
         assertThat(catalog.contains(CatalogHelper.getDefaultCatalogName()), is(true));
     }
     
     @Test
     public void testSetupCatalogUsingNoDefaults() throws URISyntaxException, FileNotFoundException, IOException {
         CatalogHelper.setupCatalog(tempLocation, knownName);
         assertThat(tempLocation.exists(), is(true));
         
         String catalog = FileUtils.readFileToString(tempLocation);
         assertThat(catalog.contains(knownName), is(true));
     }
     
     /**
      * Here we want to test that if we feed the setupCatalog() function an input of a location
      * with an existing catalog, it won't try to set it up again
      * 
      * @throws URISyntaxException
      * @throws FileNotFoundException
      * @throws IOException 
      */
     @Test
     public void testSetupCatalogUsingExistingCatalogAsInput() throws URISyntaxException, FileNotFoundException, IOException {
         CatalogHelper.setupCatalog(tempLocation, knownName);
         assertThat(tempLocation.exists(), is(true));
         // We now have our catalog. But let's try to do that again with a different catalog name
         
         CatalogHelper.setupCatalog(tempLocation, "OH HAI I AM A CATALOG");
         assertThat(tempLocation.exists(), is(true));
         // At this point this catalog should not have been created
         
         String catalog =  FileUtils.readFileToString(tempLocation);
         assertThat(catalog.contains(knownName), is(true));
         assertThat(catalog.contains("OH HAI I AM A CATALOG"), is(false));
     }
     
     @Test
     public void testCreateCatalogImpl() throws URISyntaxException, FileNotFoundException, IOException {
         URI uri = new URI("file://" + tempDir + CatalogHelper.getDefaultCatalogFilename());
         InvCatalogImpl test = CatalogHelper.createCatalogImpl(knownName, uri);
         assertThat(test, is(notNullValue()));
         assertThat(test.name, is(equalTo(knownName)));
     }
     
     @Test
     public void testAddDataset() throws URISyntaxException, FileNotFoundException, IOException {
         URI uri = new URI("file://" + tempDir + CatalogHelper.getDefaultCatalogFilename());
         InvCatalogImpl cat = CatalogHelper.createCatalogImpl(knownName, uri);
         InvDatasetBuilder ds = new InvDatasetBuilder("test", "id");
         CatalogHelper.addDataset(cat, ds.build());
         InvDataset test = cat.findDatasetByID("id");
         assertThat(test, is(notNullValue()));
         assertThat(test.name, is(equalTo("test")));
     }
     
     @Test
     public void testReadCatalog() throws URISyntaxException, FileNotFoundException, IOException {
         CatalogHelper.createNewCatalog(knownName, tempLocation.getPath());
         InvCatalog readCatalog = CatalogHelper.readCatalog(tempLocation.toURI());
         assertThat(readCatalog.name, is(equalTo(knownName)));
     }
     
     @Test
     public void testRemoveDataset() throws URISyntaxException, FileNotFoundException, IOException {
         URI uri = new URI("file://" + tempDir + CatalogHelper.getDefaultCatalogFilename());
         InvCatalogImpl cat = CatalogHelper.createCatalogImpl(knownName, uri);
         InvDatasetBuilder ds = new InvDatasetBuilder("test", "id");
         CatalogHelper.addDataset(cat, ds.build());
         InvDataset test = cat.findDatasetByID("id");
         assertThat(test, is(notNullValue()));
         CatalogHelper.removeDataset(cat, "id");
         test = cat.findDatasetByID("id");
         assertThat(test, is(nullValue()));
     }
     
     @Test
     public void testInsertWorked() throws SQLException, NamingException, ClassNotFoundException {
         Connection connection = null;
         Statement stmt = null;
         ResultSet rs = null;
         try {
             connection = SqlUtils.getConnection("");
             stmt = connection.createStatement();
             String sql = "SELECT count(*) FROM catalogs";
             stmt.execute(sql);
             rs = stmt.getResultSet();
             rs.next();
             int count = rs.getInt(1);
             assertThat(count, is(equalTo(1)));
         }
         finally {
             rs.close();
             stmt.close();
             SqlUtils.closeConnection(connection);
         }
     }
     
     @Test
     public void testLoadCatalog() throws URISyntaxException, FileNotFoundException, IOException {
         CatalogHelper.createNewCatalog(knownName, tempLocation.getPath());
        InvCatalog cat = CatalogHelper.loadDatabase(tempLocation.toURI());
        InvCatalogSetter setter = new InvCatalogSetter(cat);
        setter.setName("test");
        assertThat(cat.getName(), is(equalTo("test")));
         CatalogHelper.writeCatalog(cat);
         InvCatalog readCatalog = CatalogHelper.readCatalog(tempLocation.toURI());
        assertThat(readCatalog.getName(), is(equalTo("test")));
     }
     
 }
