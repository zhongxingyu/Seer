 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.responses.wfs;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.geotools.data.DataSourceException;
 import org.geotools.data.DataStore;
 import org.geotools.data.DataUtilities;
 import org.geotools.data.DefaultQuery;
 import org.geotools.data.DefaultTransaction;
 import org.geotools.data.FeatureLocking;
 import org.geotools.data.FeatureReader;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.FeatureStore;
 import org.geotools.data.FeatureWriter;
 import org.geotools.data.Transaction;
 import org.geotools.feature.AttributeType;
 import org.geotools.feature.Feature;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureType;
 import org.geotools.feature.SchemaException;
 import org.geotools.filter.Filter;
 import org.geotools.filter.FilterFactory;
 import org.geotools.validation.Validation;
 import org.geotools.validation.ValidationProcessor;
 import org.geotools.validation.ValidationResults;
 import org.vfny.geoserver.ServiceException;
 import org.vfny.geoserver.WfsException;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.global.Service;
 import org.vfny.geoserver.global.dto.WFSDTO;
 import org.vfny.geoserver.requests.Request;
 import org.vfny.geoserver.requests.wfs.DeleteRequest;
 import org.vfny.geoserver.requests.wfs.InsertRequest;
 import org.vfny.geoserver.requests.wfs.SubTransactionRequest;
 import org.vfny.geoserver.requests.wfs.TransactionRequest;
 import org.vfny.geoserver.requests.wfs.UpdateRequest;
 import org.vfny.geoserver.responses.Response;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 
 /**
  * Handles a Transaction request and creates a TransactionResponse string.
  *
  * @author Chris Holmes, TOPP
 * @version $Id: TransactionResponse.java,v 1.13 2004/02/20 22:23:45 jive Exp $
  */
 public class TransactionResponse implements Response {
     /** Standard logging instance for class */
     private static final Logger LOGGER = Logger.getLogger(
             "org.vfny.geoserver.responses");
 
     /** Response to be streamed during writeTo */
     private WfsTransResponse response;
 
     /** Request provided to Execute method */
     private TransactionRequest request;
 
     /** Geotools2 transaction used for this opperations */
     protected Transaction transaction;
 
     /**
      * Constructor
      */
     public TransactionResponse() {
         transaction = null;
     }
 
     public void execute(Request request) throws ServiceException, WfsException {
         if (!(request instanceof TransactionRequest)) {
             throw new WfsException(
                 "bad request, expected TransactionRequest, but got " + request);
         }
        if( (request.getWFS().getServiceLevel() | WFSDTO.TRANSACTIONAL ) == 0 ){
         	throw new ServiceException("Transaction support is not enabled");
         }
         //REVISIT: this should maybe integrate with the other exception 
         //handlers better - but things that go wrong here should cause 
         //transaction exceptions.
         try {
             execute((TransactionRequest) request);
         } catch (Throwable thrown) {
             throw new WfsTransactionException(thrown);
         }
     }
 
     /**
      * Execute Transaction request.
      * 
      * <p>
      * The results of this opperation are stored for use by writeTo:
      * 
      * <ul>
      * <li>
      * transaction: used by abort & writeTo to commit/rollback
      * </li>
      * <li>
      * request: used for users getHandle information to report errors
      * </li>
      * <li>
      * stores: FeatureStores required for Transaction
      * </li>
      * <li>
      * failures: List of failures produced
      * </li>
      * </ul>
      * </p>
      * 
      * <p>
      * Because we are using geotools2 locking facilities our modification will
      * simply fail with IOException if we have not provided proper
      * authorization.
      * </p>
      * 
      * <p>
      * The specification allows a WFS to implement PARTIAL sucess if it is
      * unable to rollback all the requested changes.  This implementation is
      * able to offer full Rollback support and will not require the use of
      * PARTIAL success.
      * </p>
      *
      * @param transactionRequest
      *
      * @throws WfsException
      * @throws WfsTransactionException DOCUMENT ME!
      */
     protected void execute(TransactionRequest transactionRequest)
         throws ServiceException, WfsException {
         request = transactionRequest; // preserved toWrite() handle access 
         transaction = new DefaultTransaction();
         LOGGER.fine("request is " + request);
 
         Data catalog = transactionRequest.getWFS().getData();
 
         WfsTransResponse build = new WfsTransResponse(WfsTransResponse.SUCCESS,
                 transactionRequest.getGeoServer().isVerbose());
 
         // Map of required FeatureStores by typeName
         Map stores = new HashMap();
 
         // Gather FeatureStores required by Transaction Elements
         // and configure them with our transaction
         //
         // (I am using element rather than transaction sub request
         // to agree with the spec docs)
         for (int i = 0; i < request.getSubRequestSize(); i++) {
             SubTransactionRequest element = request.getSubRequest(i);
             String typeName = element.getTypeName();
 
             if (!stores.containsKey(typeName)) {
                 FeatureTypeInfo meta = catalog.getFeatureTypeInfo(typeName);
  
                 try {
                     FeatureSource source = meta.getFeatureSource();
 
                     if (source instanceof FeatureStore) {
                         FeatureStore store = (FeatureStore) source;
                         store.setTransaction(transaction);
                         stores.put(typeName, source);
                     } else {
                         throw new WfsTransactionException(typeName
                             + " is read-only", element.getHandle(),
                             request.getHandle());
                     }
                 } catch (IOException ioException) {
                     throw new WfsTransactionException(typeName
                         + " is not available:" + ioException,
                         element.getHandle(), request.getHandle());
                 }
             }
         }
 
         // provide authorization for transaction
         // 
         String authorizationID = request.getLockId();
 
         if (authorizationID != null) {
            if( (request.getWFS().getServiceLevel() | WFSDTO.SERVICE_LOCKING ) == 0 ){
                 // could we catch this during the handler, rather than during execution?
                 throw new ServiceException("Lock support is not enabled");
             }            
             LOGGER.finer("got lockId: " + authorizationID);
 			if (!catalog.lockExists(authorizationID)) {
 						   String mesg = "Attempting to use a lockID that does not exist"
 							   + ", it has either expired or was entered wrong.";
 						   throw new WfsException(mesg);
 					   }
             try {
                 transaction.addAuthorization(authorizationID);
             } catch (IOException ioException) {
                 // This is a real failure - not associated with a element
                 //
                 throw new WfsException("Authorization ID '" + authorizationID
                     + "' not useable", ioException);
             }
         }
 
         // execute elements in order,
         // recording results as we go
         //
         // I will need to record the damaged area for
         // pre commit validation checks
         //
         Envelope envelope = new Envelope();
 
         for (int i = 0; i < request.getSubRequestSize(); i++) {
             SubTransactionRequest element = request.getSubRequest(i);
             String typeName = element.getTypeName();
             String handle = element.getHandle();
             FeatureStore store = (FeatureStore) stores.get(typeName);
 
             if (element instanceof DeleteRequest) {
                if( (request.getWFS().getServiceLevel() | WFSDTO.SERVICE_DELETE ) == 0 ){
                     // could we catch this during the handler, rather than during execution?
                     throw new ServiceException("Transaction Delete support is not enabled");
                 }
                 try {
                     DeleteRequest delete = (DeleteRequest) element;
                     Filter filter = delete.getFilter();
 
                     Envelope damaged = store.getBounds(new DefaultQuery(
                                 delete.getTypeName(), filter));
 
                     if (damaged == null) {
                         damaged = store.getFeatures(filter).getBounds();
                     }
 
                     if ((request.getLockId() != null)
                             && store instanceof FeatureLocking
                             && (request.getReleaseAction() == TransactionRequest.SOME)) {
                         FeatureLocking locking = (FeatureLocking) store;
 
                         // TODO: Revisit Lock/Delete interaction in gt2 
                         if (false) {
                             // REVISIT: This is bad - by releasing locks before
                             // we remove features we open ourselves up to the danger
                             // of someone else locking the features we are about to
                             // remove.
                             //
                             // We cannot do it the other way round, as the Features will
                             // not exist
                             //
                             // We cannot grab the fids offline using AUTO_COMMIT
                             // because we may have removed some of them earlier in the
                             // transaction
                             //
                             locking.unLockFeatures(filter);
                             store.removeFeatures(filter);
                         } else {
                             // This a bit better and what should be done, we will
                             // need to rework the gt2 locking api to work with
                             // fids or something
                             //
                             // The only other thing that would work would be
                             // to specify that FeatureLocking is required to
                             // remove locks when removing Features.
                             // 
                             // While that sounds like a good idea, it would be
                             // extra work when doing release mode ALL.
                             // 
                             DataStore data = store.getDataStore();
                             FilterFactory factory = FilterFactory
                                 .createFilterFactory();
                             FeatureWriter writer;
                             writer = data.getFeatureWriter(typeName, filter,
                                     transaction);
 
                             try {
                                 while (writer.hasNext()) {
                                     String fid = writer.next().getID();
                                     locking.unLockFeatures(factory
                                         .createFidFilter(fid));
                                     writer.remove();
                                 }
                             } finally {
                                 writer.close();
                             }
 
                             store.removeFeatures(filter);
                         }
                     } else {
                         // We don't have to worry about locking right now
                         //
                         store.removeFeatures(filter);
                     }
 
                     envelope.expandToInclude(damaged);
                 } catch (IOException ioException) {
                     throw new WfsTransactionException(ioException.getMessage(),
                         element.getHandle(), request.getHandle());
                 }
             }
 
             if (element instanceof InsertRequest) {
                if( (request.getWFS().getServiceLevel() | WFSDTO.SERVICE_INSERT ) != 0 ){
                     // could we catch this during the handler, rather than during execution?
                     throw new ServiceException("Transaction INSERT support is not enabled");
                 }
                 try {
                     InsertRequest insert = (InsertRequest) element;
                     FeatureCollection collection = insert.getFeatures();
 
                     FeatureReader reader = DataUtilities.reader(collection);
 
                     //
                     featureValidation(store.getSchema(), collection);
 
                     Set fids = store.addFeatures(reader);
                     build.addInsertResult(element.getHandle(), fids);
 
                     //
                     // Add to validation check envelope                                
                     envelope.expandToInclude(collection.getBounds());
                 } catch (IOException ioException) {
                     throw new WfsTransactionException(ioException.getMessage(),
                         element.getHandle(), request.getHandle());
                 }
             }
 
             if (element instanceof UpdateRequest) {
                if( (request.getWFS().getServiceLevel() | WFSDTO.SERVICE_UPDATE ) == 0 ){
                     // could we catch this during the handler, rather than during execution?
                     throw new ServiceException("Transaction Update support is not enabled");
                 }
                 try {
                     UpdateRequest update = (UpdateRequest) element;
                     Filter filter = update.getFilter();
 
                     AttributeType[] types = update.getTypes(store.getSchema());
                     Object[] values = update.getValues();
 
                     DefaultQuery query = new DefaultQuery(update.getTypeName(),
                             filter);
 
                     //These are commented out because PostgisDataStore
                     //getBounds is not yet implemented.  It returns null which
                     //breaks this.  But we probably should program in case
                     //datasources don't implement, use the FeatureSource 
                     //bounds, or FeatureResults?  Whichever one will compute
                     //it for you if datastore can't.
                     //envelope.expandToInclude(store.getBounds(query));
                     if (types.length == 1) {
                         store.modifyFeatures(types[0], values[0], filter);
                     } else {
                         store.modifyFeatures(types, values, filter);
                     }
 
                     if ((request.getLockId() != null)
                             && store instanceof FeatureLocking
                             && (request.getReleaseAction() == TransactionRequest.SOME)) {
                         FeatureLocking locking = (FeatureLocking) store;
                         locking.unLockFeatures(filter);
                     }
 
                     // we only have to do this again if values contains a geometry type
                     //
                     //envelope.expandToInclude(store.getBounds(
                     //      new DefaultQuery(update.getTypeName(), filter)));
                 } catch (IOException ioException) {
                     throw new WfsTransactionException(ioException.getMessage(),
                         element.getHandle(), request.getHandle());
                 } catch (SchemaException typeException) {
                     throw new WfsTransactionException(typeName
                         + " inconsistent with update:"
                         + typeException.getMessage(), element.getHandle(),
                         request.getHandle());
                 }
             }
         }
 
         // All opperations have worked thus far
         // 
         // Time for some global Validation Checks against envelope
         //
         try {
             integrityValidation(stores, envelope);
         } catch (IOException invalid) {
             throw new WfsTransactionException(invalid);
         }
 
         // we will commit in the writeTo method
         // after user has got the response
         response = build;
     }
 
     protected void featureValidation(FeatureType type,
         FeatureCollection collection)
         throws IOException, WfsTransactionException {
         ValidationProcessor validation = request.getValidationProcessor();
 
         final Map failed = new TreeMap();
         ValidationResults results = new ValidationResults() {
                 String name;
                 String description;
 
                 public void setValidation(Validation validation) {
                     name = validation.getName();
                     description = validation.getDescription();
                 }
 
                 public void error(Feature feature, String message) {
                     LOGGER.warning(name + ": " + message + " (" + description
                         + ")");
                     failed.put(feature.getID(),
                         name + ": " + message + " " + "(" + description + ")");
                 }
 
                 public void warning(Feature feature, String message) {
                     LOGGER.warning(name + ": " + message + " (" + description
                         + ")");
                 }
             };
 
         try {
             validation.runFeatureTests(type, collection, results);
         } catch (Exception badIdea) {
             // ValidationResults should of handled stuff will redesign :-)
             throw new DataSourceException("Validation Failed", badIdea);
         }
 
         if (failed.isEmpty()) {
             return; // everything worked out
         }
 
         StringBuffer message = new StringBuffer();
 
         for (Iterator i = failed.entrySet().iterator(); i.hasNext();) {
             Map.Entry entry = (Map.Entry) i.next();
             message.append(entry.getKey());
             message.append(" failed test ");
             message.append(entry.getValue());
             message.append("\n");
         }
 
         throw new WfsTransactionException(message.toString(), "validation");
     }
 
     protected void integrityValidation(Map stores, Envelope check)
         throws IOException, WfsTransactionException {
         Data catalog = request.getWFS().getData();
         ValidationProcessor validation = request.getValidationProcessor();
 
         // go through each modified typeName
         // and ask what we need to check
         //
         Set typeNames = new HashSet();
 
         for (Iterator i = stores.keySet().iterator(); i.hasNext();) {
             String typeName = (String) i.next();
 
             //typeNames.addAll( validation.getDependencies( typeName ) ); 
         }
 
         // Grab a source for each typeName we need to check
         // Grab from the provided stores - so we check against
         // the transaction 
         //
         Map sources = new HashMap();
 
         for (Iterator i = typeNames.iterator(); i.hasNext();) {
             String typeName = (String) i.next();
 
             if (stores.containsKey(typeName)) {
                 sources.put(typeName, stores.get(typeName));
             } else {
                 // These will be using Transaction.AUTO_COMMIT
                 // this is okay as they were not involved in our
                 // Transaction...
                 FeatureTypeInfo meta = catalog.getFeatureTypeInfo(typeName);
                 sources.put(typeName, meta.getFeatureSource());
             }
         }
 
         final Map failed = new TreeMap();
         ValidationResults results = new ValidationResults() {
                 String name;
                 String description;
 
                 public void setValidation(Validation validation) {
                     name = validation.getName();
                     description = validation.getDescription();
                 }
 
                 public void error(Feature feature, String message) {
                     LOGGER.warning(name + ": " + message + " (" + description
                         + ")");
                     failed.put(feature.getID(),
                         name + ": " + message + " " + "(" + description + ")");
                 }
 
                 public void warning(Feature feature, String message) {
                     LOGGER.warning(name + ": " + message + " (" + description
                         + ")");
                 }
             };
 
         try {
             validation.runIntegrityTests(stores, check, results);
         } catch (Exception badIdea) {
             // ValidationResults should of handled stuff will redesign :-)
             throw new DataSourceException("Validation Failed", badIdea);
         }
 
         if (failed.isEmpty()) {
             return; // everything worked out
         }
 
         StringBuffer message = new StringBuffer();
 
         for (Iterator i = failed.entrySet().iterator(); i.hasNext();) {
             Map.Entry entry = (Map.Entry) i.next();
             message.append(entry.getKey());
             message.append(" failed test ");
             message.append(entry.getValue());
             message.append("\n");
         }
 
         throw new WfsTransactionException(message.toString(), "validation");
     }
 
     /**
      * Responce MIME type as define by ServerConig.
      *
      * @param gs DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public String getContentType(GeoServer gs) {
         return gs.getMimeType();
     }
 
     /**
      * Writes generated xmlResponse.
      * 
      * <p>
      * I have delayed commiting the result until we have returned it to the
      * user, this gives us a chance to rollback if we are not able to provide
      * a response.
      * </p>
      * I could not quite figure out what to about releasing locks. It could be
      * we are supposed to release locks even if the transaction fails, or only
      * if it succeeds.
      *
      * @param out DOCUMENT ME!
      *
      * @throws ServiceException DOCUMENT ME!
      * @throws IOException DOCUMENT ME!
      */
     public void writeTo(OutputStream out) throws ServiceException, IOException {
         if ((transaction == null) || (response == null)) {
             throw new ServiceException("Transaction not executed");
         }
 
         if (response.status == WfsTransResponse.PARTIAL) {
             throw new ServiceException("Canceling PARTIAL response");
         }
 
         try {
             Writer writer;
 
             writer = new OutputStreamWriter(out);
             writer = new BufferedWriter(writer);
 
             response.writeXmlResponse(writer, request);
             writer.flush();
 
             switch (response.status) {
             case WfsTransResponse.SUCCESS:
                 transaction.commit();
 
                 break;
 
             case WfsTransResponse.FAILED:
                 transaction.rollback();
 
                 break;
             }
         } catch (IOException ioException) {
             transaction.rollback();
             throw ioException;
         } finally {
             transaction.close();
             transaction = null;
         }
 
         // 
         // Lets deal with the locks
         //
         // Q: Why talk to Data you ask
         // A: Only class that knows all the DataStores
         //
         // We really need to ask all DataStores to release/refresh
         // because we may have locked Features with this Authorizations
         // on them, even though we did not refer to them in this transaction.
         //
         // Q: Why here, why now?
         // A: The opperation was a success, and we have completed the opperation
         //
         // We also need to do this if the opperation is not a success,
         // you can find this same code in the abort method
         // 
         Data catalog = request.getWFS().getData();
 
         if (request.getLockId() != null) {
             if (request.getReleaseAction() == TransactionRequest.ALL) {
                 catalog.lockRelease(request.getLockId());
             } else if (request.getReleaseAction() == TransactionRequest.SOME) {
                 catalog.lockRefresh(request.getLockId());
             }
         }
     }
 
     /* (non-Javadoc)
      * @see org.vfny.geoserver.responses.Response#abort()
      */
     public void abort(Service gs) {
         if (transaction == null) {
             return; // no transaction to rollback
         }
 
         try {
             transaction.rollback();
             transaction.close();
         } catch (IOException ioException) {
             // nothing we can do here
             LOGGER.log(Level.SEVERE,
                 "Failed trying to rollback a transaction:" + ioException);
         }
 
         if (request != null) {
             // 
             // TODO: Do we need release/refresh during an abort?               
             if (request.getLockId() != null) {
                 Data catalog = gs.getData();
 
                 if (request.getReleaseAction() == TransactionRequest.ALL) {
                     catalog.lockRelease(request.getLockId());
                 } else if (request.getReleaseAction() == TransactionRequest.SOME) {
                     catalog.lockRefresh(request.getLockId());
                 }
             }
         }
 
         request = null;
         response = null;
     }
 }
