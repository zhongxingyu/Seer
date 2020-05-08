 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wfs.responses;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.geotools.data.DefaultTransaction;
 import org.geotools.data.FeatureLock;
 import org.geotools.data.FeatureLocking;
 import org.geotools.data.FeatureReader;
 import org.geotools.data.FeatureResults;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.Transaction;
 import org.geotools.factory.FactoryFinder;
 import org.geotools.feature.AttributeType;
 import org.geotools.feature.Feature;
 import org.geotools.feature.IllegalAttributeException;
 import org.geotools.filter.FidFilter;
 import org.geotools.filter.FilterFactory;
 import org.geotools.filter.FilterFactoryFinder;
 import org.vfny.geoserver.Request;
 import org.vfny.geoserver.Response;
 import org.vfny.geoserver.ServiceException;
 import org.vfny.geoserver.global.AttributeTypeInfo;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.global.NameSpaceInfo;
 import org.vfny.geoserver.global.Service;
 import org.vfny.geoserver.wfs.FeatureResponseDelegateProducerSpi;
 import org.vfny.geoserver.wfs.Query;
 import org.vfny.geoserver.wfs.WfsException;
 import org.vfny.geoserver.wfs.requests.FeatureRequest;
 import org.vfny.geoserver.wfs.requests.FeatureWithLockRequest;
 
 /** 
  * Handles a Get Feature request and creates a Get Feature response GML string.
  *
  * @author Chris Holmes, TOPP
  * @author Jody Garnett, Refractions Research
  * @version $Id: FeatureResponse.java,v 1.24 2004/04/05 12:03:19 cholmesny Exp $
  */
 public class FeatureResponse implements Response {
     /** Standard logging instance for class */
     private static final Logger LOGGER = Logger.getLogger(
             "org.vfny.geoserver.responses");
     FeatureResponseDelegate delegate;
 
     /**
      * This is the request provided to the execute( Request ) method.
      * 
      * <p>
      * We save it so we can access the handle provided by the user for error
      * reporting during the writeTo( OutputStream ) opperation.
      * </p>
      * 
      * <p>
      * This value will be <code>null</code> until execute is called.
      * </p>
      */
     private FeatureRequest request;
 
     /**
      * This is the FeatureLock provided by execute( Request ) method.
      * 
      * <p>
      * This will only be non null if RequestFeatureWithLock.
      * </p>
      */
     FeatureLock featureLock;
 
     /**
      * Empty constructor
      */
     public FeatureResponse() {
         request = null;
         featureLock = null;
     }
 
     /**
      * Returns any extra headers that this service might want to set in the HTTP response object.
      * @see org.vfny.geoserver.Response#getResponseHeaders()
      */
     public HashMap getResponseHeaders() {
     	return null;
     }
     
     /**
      * DOCUMENT ME!
      *
      * @param gs DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public String getContentType(GeoServer gs) {
         return delegate.getContentType(gs);
     }
 
     public String getContentEncoding() {
         return delegate.getContentEncoding();
     }
 
     /**
      * Jody here with one pass replacement for writeTo.
      * 
      * <p>
      * This code is a discussion point, when everyone has had there input we
      * will try and set things up properly.
      * </p>
      * 
      * <p>
      * I am providing a mirror of the existing desing: - execute gathers the
      * resultList - sets up the header
      * </p>
      *
      * @param out DOCUMENT ME!
      *
      * @throws ServiceException DOCUMENT ME!
      * @throws IOException DOCUMENT ME!
      * @throws IllegalStateException DOCUMENT ME!
      */
     public void writeTo(OutputStream out) throws ServiceException, IOException {
         if ((request == null) || (delegate == null)) {
             throw new IllegalStateException(
                 "execute has not been called prior to writeTo");
         }
 
         delegate.encode(out);
     }
 
     /**
      * Executes FeatureRequest.
      * 
      * <p>
      * Willing to execute a FetureRequest, or FeatureRequestWith Lock.
      * </p>
      *
      * @param req DOCUMENT ME!
      *
      * @throws ServiceException DOCUMENT ME!
      */
     public void execute(Request req) throws ServiceException {
         execute((FeatureRequest) req);
     }
     
     
     /** 
      * use the SPI mechanism to get a FeatureResponseDelegate for the
      * specified output format.
      * 
      * @param outputFormat
      * @return
      */
     public static FeatureResponseDelegate getDelegate(String outputFormat) 
                throws NoSuchElementException
 	{
     	FeatureResponseDelegateProducerSpi spi;
          Iterator spi_it = FactoryFinder.factories(FeatureResponseDelegateProducerSpi.class);
 
          while (spi_it.hasNext()) 
          {
              spi = (FeatureResponseDelegateProducerSpi) spi_it.next();
              if (spi.canProduce(outputFormat))
              {
              	return spi.createFeatureDelegateProducer(outputFormat);
              }             		
          }
          throw new NoSuchElementException();
 	}
 
     /**
      * Performs a getFeatures, or getFeaturesWithLock (using gt2 locking ).
      * 
      * <p>
      * The idea is to grab the FeatureResulsts during execute, and use them
      * during writeTo.
      * </p>
      *
      * @param request
      *
      * @throws ServiceException
      * @throws WfsException DOCUMENT ME!
      *
      * @task TODO: split this up a bit more?  Also get the proper namespace
      *       declrations and schema locations.  Right now we're back up to
      *       where we were with 1.0., as we can return two FeatureTypes in the
      *       same namespace.  CITE didn't check for two in different
      *       namespaces, and gml builder just couldn't deal.  Now we should be
      *       able to, we just need to get the reporting right, use the
      *       AllSameType function as  Describe does.
      */
     public void execute(FeatureRequest request) throws ServiceException {
         LOGGER.finest("execute FeatureRequest response. Called request is: "
             + request);
         this.request = request;
 
         String outputFormat = request.getOutputFormat();
 
         try {
             delegate = FeatureResponse.getDelegate(outputFormat);
         } catch (NoSuchElementException ex) {
             throw new WfsException("output format: " + outputFormat + " not "
                 + "supported by geoserver", ex);
         }
 
         if (request instanceof FeatureWithLockRequest) {
             featureLock = ((FeatureWithLockRequest) request).toFeatureLock();
 
             String authorization = featureLock.getAuthorization();
             LOGGER.finest("FeatureWithLock using Lock:" + authorization);
         }
 
         GetFeatureResults results = new GetFeatureResults(request);
         results.setFeatureLock(featureLock);
 
         //
         // Optimization Idea
         //
         // We should be able to reduce this to a two pass opperations.
         //
         // Pass #1 execute
         // - Attempt to Locks Fids during the first pass
         // - Also collect Bounds information during the first pass
         //
         // Pass #2 writeTo
         // - Using the Bounds to describe our FeatureCollections
         // - Iterate through FeatureResults producing GML
         //
         // And allways remember to release locks if we are failing:
         // - if we fail to aquire all the locks we will need to fail and
         //   itterate through the the FeatureSources to release the locks
         //
         GeoServer config = request.getWFS().getGeoServer();
         Data catalog = request.getWFS().getData();
         FeatureTypeInfo meta = null;
         NameSpaceInfo namespace;
         Query query;
         int maxFeatures = request.getMaxFeatures();
         int serverMaxFeatures = config.getMaxFeatures();
 
         if (maxFeatures > serverMaxFeatures) {
             maxFeatures = serverMaxFeatures;
         }
 
         Set lockedFids = new HashSet();
         Set lockFailedFids = new HashSet();
 
         FeatureSource source;
         Feature feature;
         String fid;
         FilterFactory filterFactory = FilterFactoryFinder.createFilterFactory();
         FidFilter fidFilter;
         int numberLocked;
 
         try {
             for (Iterator it = request.getQueries().iterator();
                     it.hasNext() && (maxFeatures > 0);) {
                 query = (Query) it.next();
                 meta = catalog.getFeatureTypeInfo(query.getTypeName());
                 namespace = meta.getDataStoreInfo().getNameSpace();
                 source = meta.getFeatureSource();
 
                 List attrs = meta.getAttributes();
 
                 List propNames = query.getPropertyNames(); // REAL LIST: be careful here :)
                 List attributeNames = meta.getAttributeNames();
 
                 for (Iterator iter = propNames.iterator(); iter.hasNext();) {
                     String propName = (String) iter.next();
 
                     if (!attributeNames.contains(propName)) {
                         String mesg = "Requested property: " + propName
                             + " is " + "not available for "
                             + query.getTypeName() + ".  "
                             + "The possible propertyName values are: "
                             + attributeNames;
                         throw new WfsException(mesg);
                     }
                 }
 
                 if (propNames.size() != 0) {
                     Iterator ii = attrs.iterator();
                     List tmp = new LinkedList();
 
                     while (ii.hasNext()) {
                         AttributeTypeInfo ati = (AttributeTypeInfo) ii.next();
 
                         //String attName = (String) ii.next();
                         LOGGER.finer("checking to see if " + propNames
                             + " contains" + ati);
 
                         if (((ati.getMinOccurs() > 0)
                                 && (ati.getMaxOccurs() != 0))
                                 || propNames.contains(ati.getName())) {
                             tmp.add(ati.getName());
                         }
                     }
 
                     query.setPropertyNames(tmp);
                 }
 
                 // This doesn't seem to be working?
                 // Run through features and record FeatureIDs
                 // Lock FeatureIDs as required
                 //}
                 LOGGER.fine("Query is " + query + "\n To gt2: "
                     + query.toDataQuery(maxFeatures));
 
                 //DJB: note if maxFeatures gets to 0 the while loop above takes care of this! (this is a subtle situation)
                 
                 FeatureResults features = source.getFeatures(query.toDataQuery(
                             maxFeatures));
                if (it.hasNext()) { //DJB: dont calculate feature count if you dont have to. The MaxFeatureReader will take care of the last iteration
                        int count = features.getCount();
                        // sometimes a service may return -1 if the count is deemed
                        // to be too expensive, in this case we do a brute force counte
                        // by reading features one by one (and counting)
                        if(count < 0) count = bruteForceCount(features.reader());
                	maxFeatures -= count;
                }
 
                 //GR: I don't know if the featuresults should be added here for later
                 //encoding if it was a lock request. may be after ensuring the lock
                 //succeed?
                 results.addFeatures(meta, features);
 
                 if (featureLock != null) {
                     // geotools2 locking code
                     if (source instanceof FeatureLocking) {
                         ((FeatureLocking) source).setFeatureLock(featureLock);
                     }
 
                     FeatureReader reader = null;
 
                     try {
                         for (reader = features.reader(); reader.hasNext();) {
                             feature = reader.next();
                             fid = feature.getID();
 
                             if (!(source instanceof FeatureLocking)) {
                                 LOGGER.finest("Lock " + fid
                                     + " not supported by data store (authID:"
                                     + featureLock.getAuthorization() + ")");
                                 lockFailedFids.add(fid);
 
                                 continue; // locking is not supported!
                             } else {
                                 fidFilter = filterFactory.createFidFilter(fid);
                                 numberLocked = ((FeatureLocking) source)
                                     .lockFeatures(fidFilter);
 
                                 if (numberLocked == 1) {
                                     LOGGER.finest("Lock " + fid + " (authID:"
                                         + featureLock.getAuthorization() + ")");
                                     lockedFids.add(fid);
                                 } else if (numberLocked == 0) {
                                     LOGGER.finest("Lock " + fid
                                         + " conflict (authID:"
                                         + featureLock.getAuthorization() + ")");
                                     lockFailedFids.add(fid);
                                 } else {
                                     LOGGER.warning("Lock " + numberLocked + " "
                                         + fid + " (authID:"
                                         + featureLock.getAuthorization()
                                         + ") duplicated FeatureID!");
                                     lockedFids.add(fid);
                                 }
                             }
                         }
                     } finally {
                         if (reader != null) {
                             reader.close();
                         }
                     }
 
                     if (!lockedFids.isEmpty()) {
                         Transaction t = new DefaultTransaction();
 
                         try {
                             t.addAuthorization(featureLock.getAuthorization());
                             source.getDataStore().getLockingManager().refresh(featureLock
                                 .getAuthorization(), t);
                         } finally {
                             t.commit();
                         }
                     }
                 }
             }
 
             //end for
             //prepare to encode in the desired output format
             delegate.prepare(outputFormat, results);
 
             if ((featureLock != null) && !lockFailedFids.isEmpty()) {
                 // I think we need to release and fail when lockAll fails
                 //
                 // abort will take care of releasing the locks
                 throw new WfsException("Could not aquire locks for:"
                     + lockFailedFids);
             }
         } catch (IOException e) {
             throw new ServiceException(e, "problem with FeatureResults",
                 request.getHandle());
         } catch (NoSuchElementException e) {
             throw new ServiceException(e, "problem with FeatureResults",
                 request.getHandle());
         } catch (IllegalAttributeException e) {
             throw new ServiceException(e, "problem with FeatureResults",
                 request.getHandle());
         }
     }
 
     /**
     * Counts features by scrolling thru the feature reader
     * @param reader
     * @return
     * @throws IllegalAttributeException 
     * @throws IOException 
     * @throws NoSuchElementException 
     */
    private int bruteForceCount(FeatureReader reader) throws NoSuchElementException, IOException, IllegalAttributeException {
        int count = 0;
        while(reader.hasNext()) {
            reader.next();
            count++;
        }
        return count;
    }

    /**
      * Convenience method to get the handle information from a query, if it
      * exists.
      *
      * @param query the query to get the handle from.
      *
      * @return A string to report more information where things went wrong.
      */
     private static String getLocator(Query query) {
         String locator = query.getHandle();
 
         if ((locator == null) || locator.equals("")) {
             locator = "Class FeatureResponse, in method getQuery";
         }
 
         return locator;
     }
 
     /**
      * Parses the GetFeature request and returns a contentHandler.
      *
      * @param query The geoserver representation of the query.
      * @param meta The info on the featureType.
      * @param maxFeatures The max number of features to get with this query.
      *
      * @return XML response to send to client
      *
      * @throws WfsException For any problems with the DataSource.
      */
     private static FeatureResults getFeatures(Query query,
         FeatureTypeInfo meta, int maxFeatures) throws WfsException {
         LOGGER.finest("about to get query: " + query);
 
         List propertyNames = null;
 
         if (!query.allRequested()) {
             propertyNames = query.getPropertyNames();
         }
 
         FeatureResults features = null;
 
         try {
             FeatureSource data = meta.getFeatureSource();
             LOGGER.finest("filter is " + query.getFilter());
 
             if (!query.allRequested()) {
                 // was getSchema()
                 AttributeType[] mandatoryProps = meta.getFeatureType()
                                                      .getAttributeTypes();
 
                 for (int i = 0; i < mandatoryProps.length; i++) {
                     query.addPropertyName(mandatoryProps[i].getName());
                 }
             }
 
             org.geotools.data.Query dsQuery = query.getDataSourceQuery(maxFeatures);
 
             features = data.getFeatures(dsQuery);
         } catch (IOException e) {
             throw new WfsException(e, "While getting features from datasource",
                 getLocator(query));
         }
 
         LOGGER.finest("successfully retrieved collection");
 
         return features;
     }
 
     /**
      * Release locks if we are into that sort of thing.
      *
      * @see org.vfny.geoserver.Response#abort()
      */
     public void abort(Service gs) {
         if (request == null) {
             return; // request was not attempted
         }
 
         if (featureLock == null) {
             return; // we have no locks
         }
 
         Data catalog = gs.getData();
 
         // I think we need to release and fail when lockAll fails
         //
         catalog.lockRelease(featureLock.getAuthorization());
     }
 
 	public String getContentDisposition() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
