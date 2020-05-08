 package statistics.store.app;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 import org.geotools.data.DataStore;
 import org.geotools.data.DataStoreFinder;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.geotools.filter.text.cql2.CQL;
 import org.geotools.filter.text.cql2.CQLException;
 import org.geotools.map.FeatureLayer;
 import org.geotools.map.MapContent;
 import org.geotools.styling.SLD;
 import org.geotools.styling.Style;
 import org.geotools.swing.JMapFrame;
 import org.opengis.filter.Filter;
 
 /**
  *
  * @author Andre Matos
  */
 public class App {
 
     public static void main(String[] args) throws IOException, CQLException{
 
         Map<String, Serializable> params = new HashMap<String, Serializable>();
         params.put("ServiceURL", "http://localhost:30355/services/SOAPService.svc");
         DataStore store = DataStoreFinder.getDataStore(params);
 
         String[] typeNames = store.getTypeNames();
         for (int i = 0; i < typeNames.length; i++) {
             System.out.println(typeNames[i]);
         }
         String typeName = typeNames[0];
 
         SimpleFeatureSource featureSource = store.getFeatureSource(typeName);
 
         Filter filter = CQL.toFilter(
                 "BBOX(the_geom, -25.576171875,28.5205078125,-3.076171875,51.0205078125) AND " +
                 "sourceId=1 AND " +
                 "indicatorId=1 AND " +
                "filterdimensions='1,S7A2000#3,T' AND " +
                "projecteddimensions='2-NUTS2'");
         SimpleFeatureCollection collection = featureSource.getFeatures(filter);
 
         System.out.println("featureType.getname() = " + featureSource.getSchema().getName());
         Style style = SLD.createSimpleStyle(featureSource.getSchema());
         MapContent map = new MapContent();
         map.addLayer(new FeatureLayer(collection, style));
 
         JMapFrame.showMap(map);
     }
 }
