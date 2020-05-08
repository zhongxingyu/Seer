 package org.geoserver.test;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.geoserver.data.CatalogWriter;
import org.geoserver.data.test.IOUtils;
 import org.geoserver.data.test.MockData;
 import org.geoserver.data.test.TestData;
 import org.geotools.data.SampleDataAccess;
 import org.geotools.data.SampleDataAccessData;
 import org.geotools.data.SampleDataAccessFactory;
 
 /**
  * Mock data for testing integration of {@link SampleDataAccess} with GeoServer.
  * 
  * Inspired by {@link MockData}.
  */
 public class SampleDataAccessMockData implements TestData {
 
     public static String DEFAULT_PREFIX = "gsml";
 
     public static String DEFAULT_URI = SampleDataAccessData.NAMESPACE;
 
     private File data;
 
     /**
      * Constructor. Creates empty mock data directory.
      * 
      * @throws IOException
      */
     public SampleDataAccessMockData() throws IOException {
         data = IOUtils.createRandomDirectory("./target", "sample-data-access-mock", "data");
         data.delete();
         data.mkdir();
     }
 
     /**
      * Returns the root of the mock data directory,
      * 
      * @see org.geoserver.data.test.TestData#getDataDirectoryRoot()
      */
     public File getDataDirectoryRoot() {
         return data;
     }
 
     /**
      * Returns true.
      * 
      * @see org.geoserver.data.test.TestData#isTestDataAvailable()
      */
     public boolean isTestDataAvailable() {
         return true;
     }
 
     /**
      * Configures mock data directory.
      * 
      * @see org.geoserver.data.test.TestData#setUp()
      */
     public void setUp() throws Exception {
         setUpCatalog();
         copyTo(MockData.class.getResourceAsStream("services.xml"), "services.xml");
     }
 
     /**
      * Removes the mock data directory.
      * 
      * @see org.geoserver.data.test.TestData#tearDown()
      */
     public void tearDown() throws Exception {
         IOUtils.delete(data);
         data = null;
     }
 
     /**
      * Writes catalog.xml to the data directory.
      * 
      * @throws IOException
      */
     @SuppressWarnings("serial")
     protected void setUpCatalog() throws IOException {
         CatalogWriter writer = new CatalogWriter();
         writer.dataStores(new HashMap<String, Map<String, Serializable>>() {
             {
                 put("dummy", SampleDataAccessFactory.PARAMS);
             }
         }, new HashMap<String, String>() {
             {
                 put("dummy", "gsml");
             }
         }, Collections.<String> emptySet());
         writer.coverageStores(new HashMap(), new HashMap(), Collections.EMPTY_SET);
         writer.namespaces(new HashMap<String, String>() {
             {
                 put("gsml", SampleDataAccessData.NAMESPACE);
             }
         });
         writer.styles(Collections.<String, String> emptyMap());
         writer.write(new File(data, "catalog.xml"));
     }
 
     /**
      * Copies from an {@link InputStream} to path under the mock data directory.
      * 
      * @param input
      *            source from which file content is copied
      * @param location
      *            path relative to mock data directory
      */
     public void copyTo(InputStream input, String location) throws IOException {
         IOUtils.copy(input, new File(getDataDirectoryRoot(), location));
     }
 
 }
