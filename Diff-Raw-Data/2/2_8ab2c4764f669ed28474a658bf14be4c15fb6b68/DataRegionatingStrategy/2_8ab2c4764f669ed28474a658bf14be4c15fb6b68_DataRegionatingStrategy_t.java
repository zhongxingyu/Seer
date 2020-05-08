 package org.vfny.geoserver.wms.responses.map.kml;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 
 import org.geotools.data.DefaultQuery;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.Query;
 import org.geotools.factory.CommonFactoryFinder;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.map.MapLayer;
 import org.geotools.referencing.CRS;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.filter.FilterFactory;
 import org.opengis.filter.sort.SortBy;
 import org.opengis.filter.sort.SortOrder;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.vfny.geoserver.global.GeoserverDataDirectory;
 import org.vfny.geoserver.global.MapLayerInfo;
 import org.vfny.geoserver.wms.WMSMapContext;
 import org.geoserver.ows.HttpErrorCodeException;
 
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Geometry;
 
 /**
  * Strategy for regionating based on algorithmic stuff related to the actual
  * data. This strategy is fairly ill-defined and should be considered highly
  * experimental.
  * 
  * @author David Winslow
  */
 public class DataRegionatingStrategy implements RegionatingStrategy {
 
     private static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.geosearch");
     private static long MAX_LEVELS = 5;
     private static long FEATURES_PER_TILE = 100;
     private static String myAttributeName;
 
     private Set myAcceptableFeatures;
     private Number myMin;
     private Number myMax;
     private long myZoomLevel;
 
     /**
      * Create a data regionating strategy that uses the specified attribute.
      * @param attname the name of the attribute to use.  The attribute must be numeric.
      */
     public DataRegionatingStrategy(String attname){
         myAttributeName = attname;
     }
 
     public void preProcess(WMSMapContext con, MapLayer layer) {
         myZoomLevel = getZoomLevel(con, layer);
         try{
             myAcceptableFeatures = getRangesFromDB(con, layer);
         } catch (Exception e){
             LOGGER.log(Level.INFO, "No cached tile hierarchy found; constructing quad tree from data.", e);
             TileLevel root = preProcessHierarchical(con, layer); // note: This has the side effect of populating myAcceptableFeatures
             LOGGER.info("Created tile hierarchy: " + root);
             addRangesToDB(con, layer, root);
         }
 
         if (myAcceptableFeatures.size() == 0){
             throw new HttpErrorCodeException(204); 
         }
     }
 
     private void addRangesToDB(WMSMapContext con, MapLayer layer, TileLevel ranges){
         try{
             Class.forName("org.h2.Driver");
             String dataDir = con.getRequest().getWMS().getData().getBaseDir().getCanonicalPath();
             Connection connection = 
             	DriverManager.getConnection(
             			"jdbc:h2:file:" + dataDir + "/h2database/regionate", "geoserver", "geopass"
             			);
             String tableName = findCacheTable(con, layer);
 
             Statement statement = connection.createStatement();
             statement.execute("DROP TABLE IF EXISTS " + tableName);
             statement.execute("CREATE TABLE " + tableName + " ( x integer, y integer, z integer, fid varchar(50))");
             statement.execute("CREATE INDEX ON " + tableName + " (x, y, z)");
 
             ranges.writeTo(statement, tableName);
             statement.close();
             connection.close();
         } catch (Exception e){
             LOGGER.log(Level.WARNING, "Unable to store range information in database.", e);
         }
     }
 
     private Set getRangesFromDB(WMSMapContext con, MapLayer layer) throws Exception{
         Class.forName("org.h2.Driver");
         String dataDir = con.getRequest().getWMS().getData().getBaseDir().getCanonicalPath();
         Connection connection = 
         	DriverManager.getConnection(
         			"jdbc:h2:file:" + dataDir + "/h2database/regionate", "geoserver", "geopass"
         			);
         String tableName = findCacheTable(con, layer);
 
         long[] coords = TileLevel.getTileCoords(con.getAreaOfInterest(), TileLevel.getWorldBounds());
 
         Statement statement = connection.createStatement();
         String sql = "SELECT fid FROM " + tableName + " WHERE x = " + coords[0] + " AND y = " + coords[1] + " AND z = " + coords[2];
         statement.execute( sql );
 
         ResultSet results = statement.getResultSet();
         Set returnable = new TreeSet();
         while (results.next()){
             returnable.add(results.getString(1));
         }
 
         return returnable;
     }
 
     private String findCacheTable(WMSMapContext con, MapLayer layer){
         try{
             FeatureSource source = layer.getFeatureSource();
             MapLayerInfo[] config = con.getRequest().getLayers();
             for (int i = 0; i < config.length; i++){
                 MapLayerInfo theLayer = config[i];
                 if (theLayer.getName().equals(layer.getTitle())){
                     return theLayer.getDirName() + "_" + myAttributeName;
                 } 
             }
         } catch (Exception e){
             LOGGER.log(Level.SEVERE, "Exception while finding the location for the cachefile!", e);
         }
         return null;
     }
 
     private TileLevel preProcessHierarchical(WMSMapContext con, MapLayer layer){
         LOGGER.info("Getting ready to do the hierarchy thing!");
         try{
             FeatureSource<SimpleFeatureType, SimpleFeature> source = 
                 (FeatureSource<SimpleFeatureType, SimpleFeature>) layer.getFeatureSource();
             CoordinateReferenceSystem nativeCRS = source.getSchema().getDefaultGeometry().getCRS();
             ReferencedEnvelope fullBounds = TileLevel.getWorldBounds();
             fullBounds = fullBounds.transform(nativeCRS, true);
             TileLevel root = TileLevel.makeRootLevel(fullBounds, FEATURES_PER_TILE);
             
             FilterFactory ff = (FilterFactory)CommonFactoryFinder.getFilterFactory(null);
             DefaultQuery query = new DefaultQuery(Query.ALL);
             SortBy sortBy = ff.sort(myAttributeName, SortOrder.DESCENDING);
             query.setSortBy(new SortBy[]{sortBy});
             FeatureCollection col = source.getFeatures(query);
 
             root.populate(col);
             
             TileLevel requestTile = root.findTile(con.getAreaOfInterest());
            myAcceptableFeatures = (requestTile != null ? requestTile.getFeatureSet() : root.getFeatureSet());
 
             return root;
         } catch (Exception e){
             LOGGER.log(Level.SEVERE, "Error while trying to regionate by data (hierarchical)): ", e);
         }
 
         return null;
     }
 
     private long getZoomLevel(WMSMapContext context, MapLayer layer){
         try{
             FeatureSource<SimpleFeatureType, SimpleFeature> source = 
                 (FeatureSource<SimpleFeatureType, SimpleFeature>) layer.getFeatureSource();
             ReferencedEnvelope fullBounds = TileLevel.getWorldBounds();
             ReferencedEnvelope requestBounds = context.getAreaOfInterest();
             requestBounds = requestBounds.transform(fullBounds.getCoordinateReferenceSystem(), true);
             long level = 0 - Math.round(
                     Math.log(requestBounds.getWidth() / fullBounds.getWidth()) / 
                     Math.log(2)
                     );
             if (level < 0) throw new Exception ("Request bounds " + requestBounds + " larger than the world apparently!");
             return level;
         } catch (Exception e){
             LOGGER.log(Level.SEVERE, "Zoom Level calculation failed, error was: ", e);
             return 1;
         }
     }
 
     public boolean include(SimpleFeature feature) {
         try {
             return myAcceptableFeatures.contains(feature.getID());
         } catch (Exception e) {
             LOGGER.log(Level.WARNING, "Encountered problem while trying to apply data regionating filter: ", e);
         }
         return false;
     }
 }
