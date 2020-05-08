 package gov.usgs.cida.coastalhazards.wps;
 
 import gov.usgs.cida.coastalhazards.util.FeatureCollectionFromShp;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.net.URL;
 import org.apache.commons.io.IOUtils;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.junit.Ignore;
 import org.junit.Test;
 
 /**
  *
  * @author jiwalker
  */
 public class CreateResultsLayerProcessTest {
     
     /**
      * Test of execute method, of class CreateResultsLayerProcess.
      */
     @Test
     //@Ignore
     public void testExecute() throws Exception {
         URL resource = CreateResultsLayerProcessTest.class.getClassLoader()
                .getResource("gov/usgs/cida/coastalhazards/jersey/NewJerseyN_results.txt");
         URL transects = CreateResultsLayerProcessTest.class.getClassLoader()
                 .getResource("gov/usgs/cida/coastalhazards/jersey/NewJerseyNa_transects.shp");
         BufferedReader reader = new BufferedReader(new FileReader(resource.getFile()));
         StringBuffer buffer = new StringBuffer();
         String line = null;
         while (null != (line = reader.readLine())) {
             buffer.append(line);
             buffer.append("\n");
         }
         IOUtils.closeQuietly(reader);
         SimpleFeatureCollection transectfc = (SimpleFeatureCollection)
                 FeatureCollectionFromShp.featureCollectionFromShp(transects);
         // need to get the matching transect layer to run against
         CreateResultsLayerProcess createResultsLayerProcess = new CreateResultsLayerProcess(new DummyImportProcess(), new DummyCatalog());
         createResultsLayerProcess.execute(buffer, transectfc, null, null, null);
     }
     
 }
