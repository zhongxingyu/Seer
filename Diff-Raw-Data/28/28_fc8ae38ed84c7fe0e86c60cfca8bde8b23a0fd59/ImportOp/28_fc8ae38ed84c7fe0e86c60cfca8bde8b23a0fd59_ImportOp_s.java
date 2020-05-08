 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 
 package org.geogit.geotools.plumbing;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.geogit.api.AbstractGeoGitOp;
 import org.geogit.api.RevFeatureType;
 import org.geogit.api.RevTree;
 import org.geogit.geotools.plumbing.GeoToolsOpException.StatusCode;
import org.geogit.repository.WorkingTree;
 import org.geotools.data.DataStore;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.opengis.feature.Feature;
 import org.opengis.feature.type.Name;
 import org.opengis.util.ProgressListener;
 
 import com.google.common.collect.AbstractIterator;
 import com.google.inject.Inject;
 
 /**
  * Internal operation for importing tables from a GeoTools {@link DataStore}.
  * 
  * @see DataStore
  */
 public class ImportOp extends AbstractGeoGitOp<RevTree> {
 
     private boolean all = false;
 
     private String table = null;
 
     private DataStore dataStore;
 
     /**
      * Constructs a new {@code ImportOp} operation.
      */
     @Inject
     public ImportOp() {
     }
 
     /**
      * Executes the import operation using the parameters that have been specified. Features will be
      * added to the working tree, and a new working tree will be constructed. Either {@code all} or
      * {@code table}, but not both, must be set prior to the import process.
      * 
      * @return RevTree the new working tree
      */
     @SuppressWarnings("deprecation")
     @Override
     public RevTree call() {
         if (dataStore == null) {
             throw new GeoToolsOpException(StatusCode.DATASTORE_NOT_DEFINED);
         }
 
         if ((table == null || table.isEmpty()) && all == false) {
             throw new GeoToolsOpException(StatusCode.TABLE_NOT_DEFINED);
         }
 
         if (table != null && !table.isEmpty() && all == true) {
             throw new GeoToolsOpException(StatusCode.ALL_AND_TABLE_DEFINED);
         }
 
         boolean foundTable = false;
 
         List<Name> typeNames;
         try {
             typeNames = dataStore.getNames();
         } catch (Exception e) {
             throw new GeoToolsOpException(StatusCode.UNABLE_TO_GET_NAMES);
         }
 
         getProgressListener().started();
         int tableCount = 0;
         for (Name typeName : typeNames) {
             tableCount++;
             if (!all && !table.equals(typeName.toString()))
                 continue;
 
             foundTable = true;
 
             String tableName = String.format("%-16s", typeName.getLocalPart());
             if (typeName.getLocalPart().length() > 16) {
                 tableName = tableName.substring(0, 13) + "...";
             }
             getProgressListener().setDescription(
                     "Importing " + tableName + " (" + (all ? tableCount : 1) + "/"
                             + (all ? typeNames.size() : 1) + ")... ");
 
             SimpleFeatureSource featureSource;
             SimpleFeatureCollection features;
             try {
                 featureSource = dataStore.getFeatureSource(typeName);
                 features = featureSource.getFeatures();
             } catch (Exception e) {
                 throw new GeoToolsOpException(StatusCode.UNABLE_TO_GET_FEATURES);
             }
 
             RevFeatureType revType = RevFeatureType.build(featureSource.getSchema());
 
             String treePath = revType.getName().getLocalPart();
 
             final SimpleFeatureIterator featureIterator = features.features();
 
             Iterator<Feature> iterator = new AbstractIterator<Feature>() {
                 @Override
                 protected Feature computeNext() {
                     if (!featureIterator.hasNext()) {
                         return super.endOfData();
                     }
                     return featureIterator.next();
                 }
             };
 
             try {
                ProgressListener taskProgress = subProgress(100.f / (all ? typeNames.size() : 1f));
                Integer collectionSize = features.size();
                WorkingTree workTree = getWorkTree();
                workTree.delete(revType.getName());
                getWorkTree().insert(treePath, iterator, taskProgress, null, collectionSize);
             } catch (Exception e) {
                 throw new GeoToolsOpException(StatusCode.UNABLE_TO_INSERT);
             } finally {
                 featureIterator.close();
             }
         }
         if (!foundTable) {
             if (all) {
                 throw new GeoToolsOpException(StatusCode.NO_FEATURES_FOUND);
             } else {
                 throw new GeoToolsOpException(StatusCode.TABLE_NOT_FOUND);
             }
         }
         getProgressListener().progress(100.f);
         getProgressListener().complete();
         return getWorkTree().getTree();
     }
 
     /**
      * @param all if this is set, all tables from the data store will be imported
      * @return {@code this}
      */
     public ImportOp setAll(boolean all) {
         this.all = all;
         return this;
     }
 
     /**
      * @param table if this is set, only the specified table will be imported from the data store
      * @return {@code this}
      */
     public ImportOp setTable(String table) {
         this.table = table;
         return this;
     }
 
     /**
      * @param dataStore the data store to use for the import process
      * @return {@code this}
      */
     public ImportOp setDataStore(DataStore dataStore) {
         this.dataStore = dataStore;
         return this;
     }
 }
