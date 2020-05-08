 package gov.usgs.cida.coastalhazards.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.PrefixFileFilter;
 import org.geoserver.catalog.CascadeDeleteVisitor;
 import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogFacade;
 import org.geoserver.catalog.DataStoreInfo;
 import org.geoserver.catalog.LayerInfo;
 import org.geoserver.catalog.ProjectionPolicy;
import org.geoserver.catalog.ResourceInfo;
 import org.geoserver.catalog.WorkspaceInfo;
 import org.geoserver.wps.gs.ImportProcess;
 import org.geotools.data.DataAccess;
import org.geotools.data.DataUtilities;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
 import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.process.ProcessException;
 import org.geotools.util.DefaultProgressListener;
 import org.opengis.feature.Feature;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.FeatureType;
 import org.opengis.util.ProgressListener;
 
 /**
  *
  * @author isuftin
  */
 public class GeoserverUtils {
 
     private Catalog catalog;
 
     public GeoserverUtils(Catalog catalog) {
         this.catalog = catalog;
     }
 
     public WorkspaceInfo getWorkspaceByName(String workspace) {
         WorkspaceInfo ws;
         ws = catalog.getWorkspaceByName(workspace);
         if (ws == null) {
             throw new ProcessException("Could not find workspace " + workspace);
         }
         return ws;
     }
 
     public DataStoreInfo getDataStoreByName(String workspace, String store) {
         DataStoreInfo ds = catalog.getDataStoreByName(workspace, store);
         if (ds == null) {
             throw new ProcessException("Could not find store " + store + " in workspace " + workspace);
         }
         return ds;
     }
 
     public DataAccess<? extends FeatureType, ? extends Feature> getDataAccess(DataStoreInfo store, ProgressListener listener) {
         DataAccess<? extends FeatureType, ? extends Feature> da;
         try {
             da = store.getDataStore(listener == null ? new DefaultProgressListener() : listener);
         } catch (IOException ioe) {
             throw new ProcessException(ioe);
         }
         return da;
     }
     
     public FeatureSource<? extends FeatureType, ? extends Feature> getFeatureSource(DataStoreInfo store, String layer, ProgressListener listener) {
         DataAccess<? extends FeatureType, ? extends Feature> dataAccess;
          try {
             dataAccess = store.getDataStore(listener == null ? new DefaultProgressListener() : listener);
         } catch (IOException ioe) {
             throw new ProcessException(ioe);
         }
          return getFeatureSource(dataAccess, layer);
     }
     
     public FeatureSource<? extends FeatureType, ? extends Feature> getFeatureSource(DataAccess<? extends FeatureType, ? extends Feature> dataAccess, String layer) {
         FeatureSource<? extends FeatureType, ? extends Feature> featureSource;
          try {
             featureSource = dataAccess.getFeatureSource(new NameImpl(layer));
         } catch (IOException ioe) {
             throw new ProcessException(ioe);
         }
          return featureSource;
     }
     
     public FeatureCollection<? extends FeatureType, ? extends Feature> getFeatureCollection(FeatureSource<? extends FeatureType, ? extends Feature> featureSource) {
         FeatureCollection<? extends FeatureType, ? extends Feature> fc;
         try {
             fc  = featureSource.getFeatures();
         } catch (IOException ex) {
             throw new ProcessException(ex);
         }
         return fc;
     }
     
     public String replaceLayer(FeatureCollection<SimpleFeatureType, SimpleFeature> collection, String layer, DataStoreInfo dataStore, WorkspaceInfo workspace, ImportProcess importProc) {
        LayerInfo layerByName = catalog.getLayerByName(layer);
         new CascadeDeleteVisitor(catalog).visit(layerByName);
         try {
             File diskDirectory = new File(dataStore.getDataStore(new DefaultProgressListener()).getInfo().getSource());
             Collection<File> listFiles = FileUtils.listFiles(diskDirectory, new PrefixFileFilter(layerByName.getName()), null);
             for (File file : listFiles) {
                 FileUtils.deleteQuietly(file);
             }
         } catch (IOException ex) {
             throw new ProcessException(ex);
         }
         catalog.save(dataStore);
         catalog.save(workspace);
         
         LayerImportUtil importer = new LayerImportUtil(catalog, importProc);
         return importer.importLayer((SimpleFeatureCollection) collection, workspace.getName(), dataStore.getName(), layer, collection.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem(), ProjectionPolicy.REPROJECT_TO_DECLARED);
     }
        
 }
