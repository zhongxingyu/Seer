 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.global;
 
 import com.vividsolutions.jts.geom.Envelope;
 import org.geotools.data.DataSourceException;
 import org.geotools.data.DataStore;
 import org.geotools.data.DefaultQuery;
 import org.geotools.data.FeatureListener;
 import org.geotools.data.FeatureLocking;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.FeatureStore;
 import org.geotools.data.Query;
 import org.geotools.data.crs.ForceCoordinateSystemFeatureResults;
 import org.geotools.factory.CommonFactoryFinder;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureType;
 import org.geotools.feature.SchemaException;
 import org.geotools.filter.AbstractFilter;
 import org.opengis.filter.Filter;
 import org.opengis.filter.FilterFactory;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Logger;
 
 
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
  * so?
  * </p>
  *
  * @author Gabriel Rold�n
  * @version $Id$
  */
 public class GeoServerFeatureSource implements FeatureSource {
     /** Shared package logger */
     private static final Logger LOGGER = Logger.getLogger("org.vfny.geoserver.global");
 
     /** FeatureSource being served up */
     protected FeatureSource source;
 
     /**
      * GeoTools2 Schema information
      *
      * <p>
      * Is this the same as source.getSchema() or is it used supply the order
      * that GeoServer requires attributes to be returned in?
      * </p>
      */
     private FeatureType schema;
 
     /** Used to constrain the Feature made available to GeoServer. */
     private Filter definitionQuery = Filter.INCLUDE;
 
     /** Geometries will be forced to this CRS (or null, if no forcing is needed) */
     private CoordinateReferenceSystem forcedCRS;
 
     /**
      * Creates a new GeoServerFeatureSource object.
      *
      * @param source GeoTools2 FeatureSource
      * @param schema FeatureType returned by this FeatureSource
      * @param definitionQuery Filter used to limit results
      * @param forcedCRS Geometries will be forced to this CRS (or null, if no forcing is needed)
      */
     GeoServerFeatureSource(FeatureSource source, FeatureType schema, Filter definitionQuery,
         CoordinateReferenceSystem forcedCRS) {
         this.source = source;
         this.schema = schema;
         this.definitionQuery = definitionQuery;
         this.forcedCRS = forcedCRS;
 
         if (this.definitionQuery == null) {
             this.definitionQuery = Filter.INCLUDE;
         }
     }
 
     /**
      * Factory that make the correct decorator for the provided featureSource.
      *
      * <p>
      * This factory method is public and will be used to create all required
      * subclasses. By comparison the constructors for this class have package
      * visibiliy.
      * </p>
      *
      * @param featureSource
      * @param schema DOCUMENT ME!
      * @param definitionQuery DOCUMENT ME!
      * @param forcedCRS Geometries will be forced to this CRS (or null, if no forcing is needed)
      *
      * @return
      */
     public static GeoServerFeatureSource create(FeatureSource featureSource, FeatureType schema,
         Filter definitionQuery, CoordinateReferenceSystem forcedCRS) {
         if (featureSource instanceof FeatureLocking) {
             return new GeoServerFeatureLocking((FeatureLocking) featureSource, schema,
                 definitionQuery, forcedCRS);
         } else if (featureSource instanceof FeatureStore) {
             return new GeoServerFeatureStore((FeatureStore) featureSource, schema, definitionQuery,
                 forcedCRS);
         }
 
         return new GeoServerFeatureSource(featureSource, schema, definitionQuery, forcedCRS);
     }
 
     /**
      * Takes a query and adapts it to match re definitionQuery filter
      * configured for a feature type.
      *
      * @param query Query against this DataStore
      *
      * @return Query restricted to the limits of definitionQuery
      *
      * @throws IOException See DataSourceException
      * @throws DataSourceException If query could not meet the restrictions of
      *         definitionQuery
      */
     protected Query makeDefinitionQuery(Query query) throws IOException {
         if ((query == Query.ALL) || query.equals(Query.ALL)) {
             return query;
         }
 
         try {
             String[] propNames = extractAllowedAttributes(query);
             Filter filter = query.getFilter();
             filter = makeDefinitionFilter(filter);
 
             DefaultQuery defQuery = new DefaultQuery(query);
             defQuery.setFilter(filter);
             defQuery.setPropertyNames(propNames);
 
             //set sort by
             if (query.getSortBy() != null) {
                 defQuery.setSortBy(query.getSortBy());
             }
 
             return defQuery;
         } catch (Exception ex) {
             throw new DataSourceException(
                 "Could not restrict the query to the definition criteria: " + ex.getMessage(), ex);
         }
     }
 
     /**
      * List of allowed attributes.
      *
      * <p>
      * Creates a list of FeatureTypeInfo's attribute names based on the
      * attributes requested by <code>query</code> and making sure they not
      * contain any non exposed attribute.
      * </p>
      *
      * <p>
      * Exposed attributes are those configured in the "attributes" element of
      * the FeatureTypeInfo's configuration
      * </p>
      *
      * @param query User's origional query
      *
      * @return List of allowed attribute types
      */
     private String[] extractAllowedAttributes(Query query) {
         String[] propNames = null;
 
         if (query.retrieveAllProperties()) {
             propNames = new String[schema.getAttributeCount()];
 
             for (int i = 0; i < schema.getAttributeCount(); i++) {
                 propNames[i] = schema.getAttributeType(i).getName();
             }
         } else {
             String[] queriedAtts = query.getPropertyNames();
             int queriedAttCount = queriedAtts.length;
             List allowedAtts = new LinkedList();
 
             for (int i = 0; i < queriedAttCount; i++) {
                 if (schema.getAttributeType(queriedAtts[i]) != null) {
                     allowedAtts.add(queriedAtts[i]);
                 } else {
                     LOGGER.info("queried a not allowed property: " + queriedAtts[i]
                         + ". Ommitting it from query");
                 }
             }
 
             propNames = (String[]) allowedAtts.toArray(new String[allowedAtts.size()]);
         }
 
         return propNames;
     }
 
     /**
      * If a definition query has been configured for the FeatureTypeInfo, makes
      * and return a new Filter that contains both the query's filter and the
      * layer's definition one, by logic AND'ing them.
      *
      * @param filter Origional user supplied Filter
      *
      * @return Filter adjusted to the limitations of definitionQuery
      *
      * @throws DataSourceException If the filter could not meet the limitations
      *         of definitionQuery
      */
     protected Filter makeDefinitionFilter(Filter filter)
         throws DataSourceException {
         Filter newFilter = filter;
 
         try {
             if (definitionQuery != Filter.INCLUDE) {
                 FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
                 newFilter = ff.and(definitionQuery, filter);
             }
         } catch (Exception ex) {
             throw new DataSourceException("Can't create the definition filter", ex);
         }
 
         return newFilter;
     }
 
     /**
      * Implement getDataStore.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @return
      *
      * @see org.geotools.data.FeatureSource#getDataStore()
      */
     public DataStore getDataStore() {
         return source.getDataStore();
     }
 
     /**
      * Implement addFeatureListener.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param listener
      *
      * @see org.geotools.data.FeatureSource#addFeatureListener(org.geotools.data.FeatureListener)
      */
     public void addFeatureListener(FeatureListener listener) {
         source.addFeatureListener(listener);
     }
 
     /**
      * Implement removeFeatureListener.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param listener
      *
      * @see org.geotools.data.FeatureSource#removeFeatureListener(org.geotools.data.FeatureListener)
      */
     public void removeFeatureListener(FeatureListener listener) {
         source.removeFeatureListener(listener);
     }
 
     /**
      * Implement getFeatures.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param query
      *
      * @return
      *
      * @throws IOException
      *
      * @see org.geotools.data.FeatureSource#getFeatures(org.geotools.data.Query)
      */
     public FeatureCollection getFeatures(Query query) throws IOException {
         Query newQuery = makeDefinitionQuery(query);
 
         // see if the CRS got xfered over
         // a. old had a CRS, new doesnt
         boolean requireXferCRS = (newQuery.getCoordinateSystem() == null)
             && (query.getCoordinateSystem() != null);
 
         if ((newQuery.getCoordinateSystem() != null) && (query.getCoordinateSystem() != null)) {
             //b. both have CRS, but they're different
             requireXferCRS = !(newQuery.getCoordinateSystem().equals(query.getCoordinateSystem()));
         }
 
         if (requireXferCRS) {
             //carry along the CRS
             if (!(newQuery instanceof DefaultQuery)) {
                 newQuery = new DefaultQuery(newQuery);
             }
 
             ((DefaultQuery) newQuery).setCoordinateSystem(query.getCoordinateSystem());
         }
 
         try {
             FeatureCollection fc = source.getFeatures(newQuery);
 
            if (forcedCRS != null) {
                 return new ForceCoordinateSystemFeatureResults(fc, forcedCRS);
             } else {
                 return fc;
             }
         } catch (SchemaException e) {
             throw new DataSourceException(e);
         }
     }
 
     public FeatureCollection getFeatures(Filter filter)
         throws IOException {
         return getFeatures(new DefaultQuery(schema.getTypeName(), filter));
     }
 
     public FeatureCollection getFeatures() throws IOException {
         return getFeatures(Query.ALL);
     }
 
     /**
      * Implement getSchema.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @return
      *
      * @see org.geotools.data.FeatureSource#getSchema()
      */
     public FeatureType getSchema() {
         return schema;
     }
 
     /**
      * Retrieves the total extent of this FeatureSource.
      *
      * <p>
      * Please note this extent will reflect the provided definitionQuery.
      * </p>
      *
      * @return Extent of this FeatureSource, or <code>null</code> if no
      *         optimizations exist.
      *
      * @throws IOException If bounds of definitionQuery
      */
     public Envelope getBounds() throws IOException {
         // since CRS is at most forced, we don't need to change this code
         if (definitionQuery == Filter.INCLUDE) {
             return source.getBounds();
         } else {
             Query query = new DefaultQuery(getSchema().getTypeName(), definitionQuery);
 
             return source.getBounds(query);
         }
     }
 
     /**
      * Retrive the extent of the Query.
      *
      * <p>
      * This method provides access to an optimized getBounds opperation. If no
      * optimized opperation is available <code>null</code> will be returned.
      * </p>
      *
      * <p>
      * You may still make use of getFeatures( Query ).getCount() which will
      * return the correct answer (even if it has to itterate through all the
      * results to do so.
      * </p>
      *
      * @param query User's query
      *
      * @return Extend of Query or <code>null</code> if no optimization is
      *         available
      *
      * @throws IOException If a problem is encountered with source
      */
     public Envelope getBounds(Query query) throws IOException {
         // since CRS is at most forced, we don't need to change this code
         try {
             query = makeDefinitionQuery(query);
         } catch (IOException ex) {
             return null;
         }
 
         return source.getBounds(query);
     }
 
     /**
      * Adjust query and forward to source.
      *
      * <p>
      * This method provides access to an optimized getCount opperation. If no
      * optimized opperation is available <code>-1</code> will be returned.
      * </p>
      *
      * <p>
      * You may still make use of getFeatures( Query ).getCount() which will
      * return the correct answer (even if it has to itterate through all the
      * results to do so).
      * </p>
      *
      * @param query User's query.
      *
      * @return Number of Features for Query, or -1 if no optimization is
      *         available.
      */
     public int getCount(Query query) {
         try {
             query = makeDefinitionQuery(query);
         } catch (IOException ex) {
             return -1;
         }
 
         try {
             return source.getCount(query);
         } catch (IOException e) {
             return 0;
         }
     }
 }
