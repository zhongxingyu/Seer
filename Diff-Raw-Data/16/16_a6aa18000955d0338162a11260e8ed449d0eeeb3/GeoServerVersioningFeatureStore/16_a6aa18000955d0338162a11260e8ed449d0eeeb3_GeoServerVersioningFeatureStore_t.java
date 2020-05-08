 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.global;
 
 import org.geotools.data.DataSourceException;
 import org.geotools.data.DefaultQuery;
 import org.geotools.data.Query;
 import org.geotools.data.VersioningFeatureSource;
 import org.geotools.data.VersioningFeatureStore;
 import org.geotools.data.postgis.FeatureDiffReader;
 import org.geotools.feature.FeatureCollection;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.filter.Filter;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import java.io.IOException;
 
 
 public class GeoServerVersioningFeatureStore extends GeoServerFeatureStore
     implements VersioningFeatureStore {
     GeoServerVersioningFeatureStore(VersioningFeatureStore store, SimpleFeatureType schema,
         Filter definitionQuery, CoordinateReferenceSystem declaredCRS, int srsHandling) {
         super(store, schema, definitionQuery, declaredCRS, srsHandling);
     }
 
     public void rollback(String toVersion, Filter filter, String[] users)
         throws IOException {
         ((VersioningFeatureStore) source).rollback(toVersion, filter, users);
     }
 
     public FeatureDiffReader getDifferences(String fromVersion, String toVersion, Filter filter, String[] users)
         throws IOException {
         // TODO: if we are bound to a smaller schema, we should remove the
         // hidden attributes from the differences
         return ((VersioningFeatureSource) source).getDifferences(fromVersion, toVersion, filter, users);
     }
 
     public FeatureCollection getLog(String fromVersion, String toVersion, Filter filter, String[] users, int maxFeatures)
         throws IOException {
         return ((VersioningFeatureSource) source).getLog(fromVersion, toVersion, filter, users, maxFeatures);
     }
 
     public FeatureCollection getVersionedFeatures(Query query) throws IOException {
         final VersioningFeatureSource versioningSource = ((VersioningFeatureSource) source);
         Query newQuery = adaptQuery(query, versioningSource.getVersionedFeatures().getSchema());
         
        CoordinateReferenceSystem targetCRS = query.getCoordinateSystemReproject();
         try {
             //this is the raw "unprojected" feature collection
             
             FeatureCollection fc = versioningSource.getVersionedFeatures(newQuery);
 
             return reprojectFeatureCollection(targetCRS, fc);
         } catch (Exception e) {
             throw new DataSourceException(e);
         }
     }
 
     public FeatureCollection getVersionedFeatures(Filter filter)
             throws IOException {
         return getFeatures(new DefaultQuery(schema.getTypeName(), filter));
     }
 
     public FeatureCollection getVersionedFeatures() throws IOException {
         return getFeatures(Query.ALL);
     }
 }
