 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.feature.retype;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.geoserver.feature.RetypingFeatureCollection;
 import org.geotools.data.DataSourceException;
 import org.geotools.data.DataStore;
 import org.geotools.data.DefaultQuery;
 import org.geotools.data.FeatureLocking;
 import org.geotools.data.FeatureReader;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.FeatureStore;
 import org.geotools.data.FeatureWriter;
 import org.geotools.data.LockingManager;
 import org.geotools.data.Query;
 import org.geotools.data.Transaction;
 import org.geotools.feature.SchemaException;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.filter.Filter;
 
 /**
  * A simple data store that can be used to rename feature types (despite the name, the only retyping
  * considered is the name change, thought it would not be that hard to extend it so that it
  * could shave off some attribute too) 
  */
 public class RetypingDataStore implements DataStore {
     DataStore wrapped;
 
     Map forwardMap = new HashMap();
 
     Map backwardsMap = new HashMap();
 
     public RetypingDataStore(DataStore wrapped) throws IOException {
         this.wrapped = wrapped;
         // force update of type mapping maps
         getTypeNames();
     }
 
     public void createSchema(SimpleFeatureType featureType) throws IOException {
         throw new UnsupportedOperationException(
                 "GeoServer does not support schema creation at the moment");
     }
 
     public void updateSchema(String typeName, SimpleFeatureType featureType) throws IOException {
         throw new UnsupportedOperationException(
                 "GeoServer does not support schema updates at the moment");
     }
 
     public FeatureWriter getFeatureWriter(String typeName, Filter filter, Transaction transaction)
             throws IOException {
         FeatureTypeMap map = getTypeMapBackwards(typeName);
         updateMap(map, false);
         FeatureWriter writer = wrapped.getFeatureWriter(map.getOriginalName(), filter, transaction);
         if (map.isUnchanged())
             return writer;
         return new RetypingFeatureCollection.RetypingFeatureWriter(writer, map.getFeatureType());
     }
 
     public FeatureWriter getFeatureWriter(String typeName, Transaction transaction)
             throws IOException {
         FeatureTypeMap map = getTypeMapBackwards(typeName);
         updateMap(map, false);
         FeatureWriter writer = wrapped.getFeatureWriter(map.getOriginalName(), transaction);
         if (map.isUnchanged())
             return writer;
         return new RetypingFeatureCollection.RetypingFeatureWriter(writer, map.getFeatureType());
     }
 
     public FeatureWriter getFeatureWriterAppend(String typeName, Transaction transaction)
             throws IOException {
         FeatureTypeMap map = getTypeMapBackwards(typeName);
         updateMap(map, false);
         FeatureWriter writer = wrapped.getFeatureWriterAppend(map.getOriginalName(), transaction);
         if (map.isUnchanged())
             return writer;
         return new RetypingFeatureCollection.RetypingFeatureWriter(writer, map.getFeatureType());
     }
 
     public SimpleFeatureType getSchema(String typeName) throws IOException {
         FeatureTypeMap map = getTypeMapBackwards(typeName);
         updateMap(map, true);
         return map.getFeatureType();
     }
 
     public String[] getTypeNames() throws IOException {
         // here we transform the names, and also refresh the type maps so that
         // they
         // don't contain stale elements
         String[] names = wrapped.getTypeNames();
         String[] transformedNames = new String[names.length];
         Map backup = new HashMap(forwardMap);
         forwardMap.clear();
         backwardsMap.clear();
         for (int i = 0; i < names.length; i++) {
             String original = names[i];
             transformedNames[i] = transformFeatureTypeName(original);
 
             FeatureTypeMap map = (FeatureTypeMap) backup.get(original);
             if (map == null) {
                 map = new FeatureTypeMap(original, transformedNames[i]);
             }
             forwardMap.put(map.getOriginalName(), map);
             backwardsMap.put(map.getName(), map);
         }
         return transformedNames;
     }
 
     public FeatureReader getFeatureReader(Query query, Transaction transaction) throws IOException {
         FeatureTypeMap map = getTypeMapBackwards(query.getTypeName());
         updateMap(map, false);
         FeatureReader reader = wrapped.getFeatureReader(retypeQuery(query, map), transaction);
         if (map.isUnchanged())
             return reader;
         return new RetypingFeatureCollection.RetypingFeatureReader(reader, map.getFeatureType());
     }
 
     public FeatureSource getFeatureSource(String typeName) throws IOException {
         FeatureTypeMap map = getTypeMapBackwards(typeName);
         updateMap(map, false);
         FeatureSource source = wrapped.getFeatureSource(map.getOriginalName());
         if (map.isUnchanged())
             return source;
         if (source instanceof FeatureLocking) {
             FeatureLocking locking = (FeatureLocking) source;
             return new RetypingFeatureLocking(this, locking, map);
         } else if (source instanceof FeatureStore) {
             FeatureStore store = (FeatureStore) source;
             return new RetypingFeatureStore(this, store, map);
         }
         return new RetypingFeatureSource(this, source, map);
     }
 
     public LockingManager getLockingManager() {
         return wrapped.getLockingManager();
     }
 
     public FeatureSource getView(Query query) throws IOException, SchemaException {
         FeatureTypeMap map = getTypeMapBackwards(query.getTypeName());
         updateMap(map, false);
         FeatureSource view = wrapped.getView(query);
         return new RetypingFeatureSource(this, view, map);
     }
 
     /**
      * Returns the type map given the external type name
      * 
      * @param externalTypeName
      * @return
      * @throws IOException
      */
     FeatureTypeMap getTypeMapBackwards(String externalTypeName) throws IOException {
         FeatureTypeMap map = (FeatureTypeMap) backwardsMap.get(externalTypeName);
         if (map == null)
             throw new IOException("Type mapping has not been established for type  "
                     + externalTypeName + ". "
                     + "Make sure you access types using getTypeNames() or getSchema() "
                     + "before trying to read/write onto them");
         return map;
     }
 
     /**
      * Make sure the FeatureTypeMap is fully loaded
      * 
      * @param map
      * @throws IOException
      */
     void updateMap(FeatureTypeMap map, boolean forceUpdate) throws IOException {
         try {
             if (map.getFeatureType() == null || forceUpdate) {
                 SimpleFeatureType original = wrapped.getSchema(map.getOriginalName());
                 SimpleFeatureType transformed = transformFeatureType(original);
                 map.setFeatureTypes(original, transformed);
             }
         } catch (IOException e) {
             // if the feature type cannot be found in the original data store,
             // remove it from the map
             backwardsMap.remove(map.getName());
             forwardMap.remove(map.getOriginalName());
         }
     }
 
     /**
      * Transforms the original feature type into a destination one according to
      * the renaming rules. For the moment, it's just a feature type name
      * replacement
      * 
      * @param original
      * @return
      * @throws IOException
      */
     protected SimpleFeatureType transformFeatureType(SimpleFeatureType original) throws IOException {
         String transfomedName = transformFeatureTypeName(original.getTypeName());
         if (transfomedName.equals(original.getTypeName()))
             return original;
 
         try {
             SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
             b.init(original);
             b.setName(transfomedName);
             return b.buildFeatureType();
         } catch (Exception e) {
             throw new DataSourceException("Could not build the renamed feature type.", e);
         }
     }
 
     /**
      * Just transform the feature type name
      * 
      * @param originalName
      * @return
      */
     protected String transformFeatureTypeName(String originalName) {
 //        if(originalName.indexOf(":") >= 0) {
 //            return originalName.substring(originalName.indexOf(":") + 1);
 //        } else {
 //            return originalName;
 //        }
          return originalName.replaceAll(":", "_");
     }
 
     public void dispose() {
         wrapped.dispose();
     }
     
     /**
      * Retypes a query from the extenal type to the internal one using the
      * provided typemap
      * @param q
      * @param typeMap
      * @return
      * @throws IOException
      */
     Query retypeQuery(Query q, FeatureTypeMap typeMap) {
         DefaultQuery modified = new DefaultQuery(q);
         modified.setTypeName(typeMap.getOriginalName());
         modified.setFilter(retypeFilter(q.getFilter(), typeMap));
         return modified;
     }
 
     /**
      * Retypes a filter making sure the fids are using the internal typename prefix
      * @param filter
      * @param typeMap
      * @return
      */
     Filter retypeFilter(Filter filter, FeatureTypeMap typeMap) {
         FidTransformeVisitor visitor = new FidTransformeVisitor(typeMap);
         return (Filter) filter.accept(visitor, null);
     }
     
     
 }
