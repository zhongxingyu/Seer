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
 
     private TileLevel myTileLevel;
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
         myTileLevel = getRangesFromCache(con, layer);
         if (myTileLevel == null){
             myTileLevel = preProcessHierarchical(con, layer);
             LOGGER.info("Created tile hierarchy: " + myTileLevel);
             addRangesToCache(con, layer, myTileLevel);
         }
 
         TileLevel temp = myTileLevel.findTile(con.getAreaOfInterest());
         if (temp != null && temp.getZoomLevel() == myZoomLevel) myTileLevel = temp;
     }
 
     /**
      * Check the cache and find the cached range values for the current combination of (layer, zoomlevel, attribute). 
      * If the values are not cached, return null.
      *
      * @paramRanges con the WMSMapContext for the current request
      * @param index the numeric index of the layer currently being processed
      */
     private TileLevel getRangesFromCache(WMSMapContext con, MapLayer layer){
         try{
             File cache = findCacheFile(con, layer, myZoomLevel);
             ObjectInputStream in = new ObjectInputStream(new FileInputStream(cache));
             return (TileLevel)in.readObject();
         } catch (Exception e){
            LOGGER.log(Level.INFO, "Error while trying to read range cache from disk.", e);
         }
 
         return null;
     }
 
     /**
      * Cache the range values for the current combination of (layer, zoomlevel, attribute)
      * @param con the WMSMapContext for the current request
      * @param index the numeric index of the layer currently being processed
      * @param ranges the range values for the current request 
      */
     private void addRangesToCache(WMSMapContext con, MapLayer layer, TileLevel ranges){
         try{
             File cache = findCacheFile(con, layer, myZoomLevel);
             ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(cache));
             out.writeObject(ranges);
             out.flush();
             out.close();
         } catch (Exception e){
             // it's okay, we just won't cache these values
            LOGGER.log(Level.INFO, "Error while trying to write range cache to disk.", e);
         }
     }
 
     private File findCacheFile(WMSMapContext con, MapLayer layer, long zoomLevel){
         File f = null;
         try{
             FeatureSource source = layer.getFeatureSource();
             MapLayerInfo[] config = con.getRequest().getLayers();
             for (int i = 0; i < config.length; i++){
                 MapLayerInfo theLayer = config[i];
                 if (theLayer.getName().equals(layer.getTitle())){
                     f = GeoserverDataDirectory.findCreateConfigDir("featureTypes");
                     f = new File(f, theLayer.getDirName());
                     f = new File(f, myAttributeName + ".cache");
                     break;
                 } 
             }
         } catch (Exception e){
            LOGGER.log(Level.SEVERE, "Exception while finding the location for the cachefile!", e);
         }
         return f;
     }
 
     private TileLevel preProcessHierarchical(WMSMapContext con, MapLayer layer){
         LOGGER.info("Getting ready to do the hierarchy thing!");
         try{
             FeatureSource<SimpleFeatureType, SimpleFeature> source = 
                 (FeatureSource<SimpleFeatureType, SimpleFeature>) layer.getFeatureSource();
             CoordinateReferenceSystem nativeCRS = source.getSchema().getDefaultGeometry().getCRS();
             ReferencedEnvelope fullBounds = getWorldBounds();
             fullBounds = fullBounds.transform(nativeCRS, true);
             TileLevel root = TileLevel.makeRootLevel(fullBounds, FEATURES_PER_TILE);
             
             FilterFactory ff = (FilterFactory)CommonFactoryFinder.getFilterFactory(null);
             DefaultQuery query = new DefaultQuery(Query.ALL);
             SortBy sortBy = ff.sort(myAttributeName, SortOrder.DESCENDING);
             query.setSortBy(new SortBy[]{sortBy});
             FeatureCollection col = source.getFeatures(query);
 
             root.populate(col);
 
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
             ReferencedEnvelope fullBounds = getWorldBounds();
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
 
     static ReferencedEnvelope getWorldBounds(){
     	try{
     	    return new ReferencedEnvelope(-180.0, 180.0, -90.0, 90.0, CRS.decode("EPSG:4326"));
     	} catch (Exception e){
     	    LOGGER.log(Level.SEVERE, "Failure to find EPSG:4326!!", e);
     	}
     	
         return null;
     }
 
     public boolean include(SimpleFeature feature) {
         try {
             return myTileLevel.include(feature);
         } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Encountered problem while trying to apply data regionating filter: ", e);
         }
         return false;
     }
 }
