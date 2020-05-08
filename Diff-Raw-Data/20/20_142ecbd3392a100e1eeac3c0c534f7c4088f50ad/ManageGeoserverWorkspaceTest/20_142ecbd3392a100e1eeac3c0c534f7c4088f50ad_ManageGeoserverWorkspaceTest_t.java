 package gov.usgs.cida.gdp.dataaccess;
 
 import javax.xml.parsers.ParserConfigurationException;
 import java.util.List;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import javax.xml.xpath.XPathExpressionException;
 import org.junit.Before;
 import org.junit.Test;
 import static org.hamcrest.Matchers.*;
 import org.junit.After;
 import static org.junit.Assert.*;
 import org.junit.Ignore;
 import org.xml.sax.SAXException;
 
 
 /**
  *
  * @author isuftin
  */
 public class ManageGeoserverWorkspaceTest {
     ManageGeoserverWorkspace obj = null;
 
     @Before
     public void beforeTest() throws Exception{
         obj = new ManageGeoserverWorkspace(new URL("http://localhost:8081/geoserver"));
     }
 
     @After
     public void afterTest() throws Exception {
         obj = null;
     }
 
     @Test
     @Ignore
     public void listWorkSpacesTest() throws MalformedURLException, IOException {
         String result = obj.listWorkSpaces();
         assertThat(result, is(notNullValue()));
     }
 
     @Test
     @Ignore
     public void getWorkspaceNamesList() throws MalformedURLException, IOException, XPathExpressionException, ParserConfigurationException, SAXException {
         List<String> result = obj.createWorkspaceNamesList();
         assertThat(result, is(notNullValue()));
         assertThat(result.size(), is(greaterThan(0)));
     }
 
     @Test
     @Ignore
     public void getDatastoresNamesList() throws MalformedURLException, IOException, XPathExpressionException, ParserConfigurationException, SAXException {
         List<String> result = obj.createDatastoreListFromWorkspace("sample");
         assertThat(result, is(notNullValue()));
         assertThat(result.size(), is(greaterThan(0)));
     }
 
     @Test
     @Ignore
     public void retrieveDiskLocationOfDatastoreTest() throws MalformedURLException, IOException, XPathExpressionException {
         String result = obj.retrieveDiskLocationOfDatastore("upload", "shapefile");
         assertThat(result, is(notNullValue()));
         assertThat(result, is(equalTo("file:/home/isuftin/Work/dev-apache-29/temp/GDP/baac5c4b-9068-44f4-b592-72a7ff52bf78/")));
     }
 
     @Test
     @Ignore
     public void deleteLayerTest() throws MalformedURLException, IOException, XPathExpressionException {
         boolean result = obj.deleteLayer("dbshapefiles", "admin", "geoserver", true);
         assertThat(result, is(true));
     }
 
     @Test
     @Ignore
     public void deleteDataStoreTest() throws MalformedURLException, IOException, XPathExpressionException {
         boolean result = obj.deleteDatastore("sample", "dbshapefileszz", "admin", "geoserver",  true);
         assertThat(result, is(false)); 
 
         result = obj.deleteDatastore("upload", "dbshapefiles", "admin", "geoserver",  true);
         assertThat(result, is(true));
     }
 
     @Test
     @Ignore
     public void layerExistsTest() throws MalformedURLException, IOException {
        boolean result = obj.layerExists("sample", "demo_HUCss", "demo_HUCss");
         assertThat(result, is(false));
 
        result = obj.layerExists("sample", "demo_HUCs","demo_HUCs");
         assertThat(result, is(true));
     }
 
     @Test
     @Ignore
     public void workspaceExistsTest() throws MalformedURLException, IOException {
         boolean result = obj.workspaceExists("test");
         assertThat(result, is(false));
 
         result = obj.workspaceExists("sample");
         assertThat(result, is(true));
     }
 
     @Test
     @Ignore
     public void deleteFromGeoserverTest() throws MalformedURLException, IOException, XPathExpressionException {
         boolean result = obj.deleteDatastoreFromGeoserver("upload", "dbshapefiles", "admin", "geoserver",  true);
         assertThat(result, is(true));
     }
 
 }
