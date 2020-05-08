 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.global;
 
 import java.io.IOException;
 import java.util.Set;
 
 import org.geotools.data.FeatureReader;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.FeatureStore;
 import org.geotools.data.Transaction;
 import org.geotools.feature.AttributeType;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureType;
 import org.geotools.filter.Filter;
 
 
 /**
  * GeoServer wrapper for backend Geotools2 DataStore.
  * 
  * <p>
  * Support FeatureSource decorator for FeatureTypeInfo that takes care of
  * mapping the FeatureTypeInfo's FeatureSource with the schema and definition
  * query configured for it.
  * </p>
  * 
  * <p>
  * Because GeoServer requires that attributes always be returned in the same
  * order we need a way to smoothly inforce this. Could we use this class to do
  * so? It would need to support writing and locking though.
  * </p>
  *
  * @author Gabriel Rold?n
  * @version $Id: GeoServerFeatureStore.java,v 1.5 2004/02/09 23:29:41 dmzwiers Exp $
  */
 public class GeoServerFeatureStore extends GeoServerFeatureSource
    implements FeatureSource {
     /**
      * Creates a new DEFQueryFeatureLocking object.
      *
      * @param store GeoTools2 FeatureSource
      * @param schema FeatureType served by source
      * @param definitionQuery Filter that constrains source
      */
     GeoServerFeatureStore(FeatureStore store, FeatureType schema,
         Filter definitionQuery) {
         super(store, schema, definitionQuery);
     }
 
     /**
      * FeatureStore access (to save casting)
      *
      * @return DOCUMENT ME!
      */
     FeatureStore store() {
         return (FeatureStore) source;
     }
     
     /**
      * see interface for details.
      * @param fc
      * @return
      * @throws IOException
      */
     public Set addFeatures(FeatureCollection fc) throws IOException 
 	{
         return store().addFeatures(fc.reader());
     }
     
 
     /**
      * addFeatures purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param reader Reader over Feature to be added
      *
      * @return Set of FIDs added
      *
      * @throws IOException If contents of reader could not be added
      */
     public Set addFeatures(FeatureReader reader) throws IOException {
         return store().addFeatures(reader);
     }
     
     /**
      * DOCUMENT ME!
      *
      * @param filter DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     public void removeFeatures(Filter filter) throws IOException {
         filter = makeDefinitionFilter(filter);
 
         store().removeFeatures(filter);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param type DOCUMENT ME!
      * @param value DOCUMENT ME!
      * @param filter DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      *
      * @task REVISIT: should we check that non exposed attributes are requiered
      *       in <code>type</code>?
      */
     public void modifyFeatures(AttributeType[] type, Object[] value,
         Filter filter) throws IOException {
         filter = makeDefinitionFilter(filter);
 
         store().modifyFeatures(type, value, filter);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param type DOCUMENT ME!
      * @param value DOCUMENT ME!
      * @param filter DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     public void modifyFeatures(AttributeType type, Object value, Filter filter)
         throws IOException {
         filter = makeDefinitionFilter(filter);
 
         store().modifyFeatures(type, value, filter);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param reader DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     public void setFeatures(FeatureReader reader) throws IOException {
         store().setFeatures(reader);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param transaction DOCUMENT ME!
      */
     public void setTransaction(Transaction transaction) {
         store().setTransaction(transaction);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public Transaction getTransaction() {
         return store().getTransaction();
     }
 }
