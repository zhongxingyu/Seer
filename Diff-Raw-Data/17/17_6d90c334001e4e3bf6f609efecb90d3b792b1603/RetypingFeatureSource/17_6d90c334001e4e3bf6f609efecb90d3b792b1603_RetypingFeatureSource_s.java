 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.feature.retype;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.geoserver.feature.RetypingFeatureCollection;
 import org.geotools.data.DataStore;
 import org.geotools.data.DefaultQuery;
 import org.geotools.data.FeatureListener;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.Query;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.filter.Filter;
 
 /**
  * Renaming wrapper for a {@link FeatureSource} instance, to be used along with
  * {@link RetypingDataStore}
  */
 public class RetypingFeatureSource implements FeatureSource {
 
     FeatureSource wrapped;
 
     FeatureTypeMap typeMap;
 
     RetypingDataStore store;
 
     Map listeners = new HashMap();
 
     RetypingFeatureSource(RetypingDataStore ds, FeatureSource wrapped, FeatureTypeMap typeMap) {
         this.store = ds;
         this.wrapped = wrapped;
         this.typeMap = typeMap;
     }
 
     public void addFeatureListener(FeatureListener listener) {
         FeatureListener wrapper = new WrappingFeatureListener(this, listener);
         listeners.put(listener, wrapper);
         wrapped.addFeatureListener(wrapper);
     }
 
     public void removeFeatureListener(FeatureListener listener) {
         FeatureListener wrapper = (FeatureListener) listeners.get(listener);
         if (wrapper != null) {
             wrapped.removeFeatureListener(listener);
             listeners.remove(listener);
         }
     }
 
     public ReferencedEnvelope getBounds() throws IOException {
         // not fully correct if we use this to shave attributes too, but this is
         // not in the scope now
         return wrapped.getBounds();
     }
 
     public ReferencedEnvelope getBounds(Query query) throws IOException {
         // not fully correct if we use this to shave attributes too, but this is
         // not in the scope now
         return wrapped.getBounds(store.retypeQuery(query, typeMap));
     }
 
     public int getCount(Query query) throws IOException {
         return wrapped.getCount(store.retypeQuery(query, typeMap));
     }
 
     public DataStore getDataStore() {
         return store;
     }
 
     public FeatureCollection getFeatures() throws IOException {
         return getFeatures(Query.ALL);
     }
 
     public FeatureCollection getFeatures(Query query) throws IOException {
         if (query.getTypeName() == null) {
             query = new DefaultQuery(query);
             ((DefaultQuery) query).setTypeName(typeMap.getName());
         } else if (!typeMap.getName().equals(query.getTypeName())) {
             throw new IOException("Cannot query this feature source with " + query.getTypeName()
                     + " since it serves only " + typeMap.getName());
         }
         return new RetypingFeatureCollection(wrapped.getFeatures(store.retypeQuery(query, typeMap)), typeMap
                 .getFeatureType());
     }
 
     public FeatureCollection getFeatures(Filter filter) throws IOException {
         return getFeatures(new DefaultQuery(typeMap.getName(), filter));
     }
 
     public SimpleFeatureType getSchema() {
         return typeMap.getFeatureType();
     }
 
     public Set getSupportedHints() {
         return wrapped.getSupportedHints();
     }
 
 }
