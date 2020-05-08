 /* Copyright (c) 2001 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root 
  * application directory.
  */
 package org.vfny.geoserver.responses;
 
//import java.util.List;
//import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.logging.Logger;
 import java.sql.Connection;
 import java.sql.SQLException;
 import org.geotools.data.DataSource;
 import org.geotools.data.DataSourceMetaData;
 import org.geotools.data.DataSourceException;
//import org.geotools.data.postgis.PostgisConnectionFactory;
//import org.geotools.data.postgis.PostgisDataSource;
 import org.geotools.filter.Filter;
 import org.geotools.feature.AttributeType;
 import org.geotools.feature.FeatureType;
 import org.geotools.feature.FeatureTypeFlat;
 import org.geotools.feature.FeatureTypeFactory;
 import org.geotools.feature.Feature;
 import org.geotools.feature.SchemaException;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureCollectionDefault;
 import org.vfny.geoserver.requests.TransactionRequest;
 import org.vfny.geoserver.requests.SubTransactionRequest;
 import org.vfny.geoserver.requests.DeleteRequest;
 import org.vfny.geoserver.requests.UpdateRequest;
 import org.vfny.geoserver.requests.InsertRequest;
 import org.vfny.geoserver.config.TypeInfo;
 import org.vfny.geoserver.config.TypeRepository;
 
 
 /**
  * Handles a Transaction request and creates a TransactionResponse string.
  *
  *@author Chris Holmes, TOPP
  *@version $VERSION$
  */
 public class TransactionResponse {
 
     /** Standard logging instance for class */
     private static final Logger LOGGER = 
         Logger.getLogger("org.vfny.geoserver.responses");
     
     /** The handle of the transaction */
     //HACK: this is not safe, this class should probably not be static.
     //TODO: just get rid of this, it should always be set in Transaction
     //anyways.
     private static String transHandle;
 
     private static TypeRepository repository = TypeRepository.getInstance();
 
     /** Constructor, which is required to take a request object. */ 
     private TransactionResponse () {}
 
     /**
      * Parses the GetFeature reqeust and returns a contentHandler.
      * @return XML response to send to client
      */ 
     public static String getXmlResponse(TransactionRequest request) 
	throws WfsTransactionException {
 	LOGGER.finer("doing transaction response");
 	//        TypeRepository repository = TypeRepository.getInstance();
 	transHandle = request.getHandle();            
 	// main handler and return string
         //  generate GML for heander for each table requested
 	WfsTransResponse response = new WfsTransResponse
 	    (WfsTransResponse.SUCCESS, request.getHandle());
 	SubTransactionRequest subRequest = request.getSubRequest(0);
 	String lockId = request.getLockId();
 	LOGGER.finer("got lockId: " + lockId);
	try {
 	for(int i = 0, n = request.getSubRequestSize(); i < n; i++) {  
 	    subRequest = request.getSubRequest(i);
 	    String typeName = subRequest.getTypeName(); 
 	    Filter filter = null;
 	    if (subRequest.getOpType() == SubTransactionRequest.DELETE){
 		filter = ((DeleteRequest)subRequest).getFilter();
 	    } else if (subRequest.getOpType() == SubTransactionRequest.UPDATE){
 		filter = ((UpdateRequest)subRequest).getFilter();
 	    } //REVISIT: should inserts detect if the whole featureType is 
 	    //locked?  Hard to check right now.
 	    if (repository.isLocked(typeName, filter, lockId)) {
 		LOGGER.finer("it's locked, should throw exception");
 		String message = ("Feature Type: " + typeName + " is locked," +
 				  " and the lockId of the request - " + lockId +
 				  " - is not the proper lock");
		//should this be transaction exception?  Spec says this, but
		//seems like transaction exception would give user more info.
 		throw new WfsException(message, subRequest.getHandle()); 
		//moot now, we're catching all and turning into transactionExcs
 	    } 
 	     
 	    Collection addedFids = getSub(subRequest);
 	    if (addedFids != null) {
 		response.addInsertResult(subRequest.getHandle(), addedFids);
 	    }
 	    
 	}
	} catch (WfsException e){
	    throw new WfsTransactionException(e, subRequest.getHandle(), 
					      request.getHandle());
	}
 	commitAll(request);
 	repository.unlock(request);
 	return response.getXmlResponse();
 
     }
 
     /**
      * Commits all the sub transactions.
      * 
      * @param request holds the subTransactions that were just completed.
      * @throws WfsException if there was problems with the datasource.
      * @tasks REVISIT: This leaves a bunch of connections open with postgis.
      * Should have a close() method in the datasource interface.
      */
     private static void commitAll(TransactionRequest request) 
 	throws WfsTransactionException {
 	SubTransactionRequest subRequest = request.getSubRequest(0);
 	try {
 	    for(int i = 0, n = request.getSubRequestSize(); i < n; i++) {  
 		subRequest = request.getSubRequest(i);
 		String typeName = subRequest.getTypeName(); 
 		TypeInfo typeInfo = repository.getType(typeName);
 		typeInfo.getTransactionDataSource().commit();
 	    }
 	} catch (DataSourceException e) {
 	    String message = "Problem accessing datasource: " 
 		+ e.getMessage() +", " + e.getCause();
 	    LOGGER.info(message);
 	    throw new WfsTransactionException(message, subRequest.getHandle());
 	} catch (WfsException e){
 	    throw new WfsTransactionException(e, subRequest.getHandle(),
 					      transHandle);
 	}
     }
 
 
     /**
      * Parses the GetFeature reqeust and returns a contentHandler.
      * @return XML response to send to client
      */ 
     private static Collection getSub(SubTransactionRequest sub)
 	throws WfsTransactionException {
 
         String result = new String();
 	LOGGER.finer("sub request is " + sub);
 	String typeName = sub.getTypeName();
 
 
 	try {
 	    DataSource data = 
 		repository.getType(typeName).getTransactionDataSource();
 	    DataSourceMetaData metad = data.getMetaData();
 	    LOGGER.finer("metad is " + metad);
 	    //LOGGER.finer(" supports trans: " + metad.supportsTransactions());
 	    
 	    if (!metad.supportsAdd()){
 		String message = typeName + " does not support transactions";
 		throw new WfsTransactionException(message, sub.getHandle());
 	    }
 	    data.setAutoCommit(false);
 	    switch (sub.getOpType()) {
 	    case SubTransactionRequest.UPDATE: 
 		UpdateRequest update = (UpdateRequest)sub;
 		doUpdate(update, data);
 		break;
 	    case SubTransactionRequest.INSERT:
 		InsertRequest insert = (InsertRequest)sub;
 		return doInsert(insert, data);
 	    case SubTransactionRequest.DELETE:
 		LOGGER.finer("about to perform delete: " + sub);
 		doDelete((DeleteRequest)sub, data);
 		break;
 	    default: break;
 	    }
 	} catch (DataSourceException e) {
 	  String message = "Problem creating datasource: " 
 		+ e.getMessage() +", " + e.getCause();
 	  LOGGER.info(message);
 	  throw new WfsTransactionException(message, sub.getHandle());
 	} catch (WfsException e){
 		throw new WfsTransactionException(e, sub.getHandle(),
 						  transHandle);
 	} 
 	return null;
     }
 
     /**
      * Performs the insert operation.
      * @param insert the request to perform.
      * @param data the datasource to add features to.
      */
     private static Collection doInsert(InsertRequest insert, DataSource data) 
 	throws WfsTransactionException {
 	String handle = insert.getHandle();
 	try {
 	    FeatureCollection newFeatures = new FeatureCollectionDefault();
 	    //TODO: allow different features types (need to change in request),
 	    //maybe then divide up by type, get the right datasources.
 	    Feature[] featureArr = insert.getFeatures();
 	    newFeatures.addFeatures(featureArr);
 	    return data.addFeatures(newFeatures);
 	} catch (DataSourceException e) {
 	    LOGGER.info("Problem with datasource " + e + " cause: " 
 			+ e.getCause());
 	    try {
 		data.rollback();
 	    } catch (DataSourceException de){
 		//should we null the datasource?  Close the connection?
 		//What we really need is a connection pool, seperate for
 		//transactions and requests.
 		LOGGER.info("could not roll back datasource");
 	    }
 	    throw new WfsTransactionException("Problem updating features: " 
 					      +e.toString() + " cause: " + 
 					      e.getCause(), handle);
 	} 
     }
 
      /**
      * Performs the update operation.
      * @param update the request to perform.
      * @param data the datasource to remove features from.
      */
     private static void doUpdate(UpdateRequest update, DataSource data) 
 	throws WfsTransactionException {
 	String handle = update.getHandle();
 	try {
 	    FeatureType schema = data.getSchema();
 	    //schema = schema.setTypeName("dude");
 	    AttributeType[] types = update.getTypes(schema);
 	    LOGGER.fine("attribut type is " + types[0] + ", values is " + 
 		     update.getValues()[0]);
 	    data.modifyFeatures(types, update.getValues(), update.getFilter());
 	} catch (DataSourceException e) {
 	    LOGGER.info("Problem with datasource " + e + " cause: " 
 			+ e.getCause());
 	    try {
 		data.rollback();
 	    } catch (DataSourceException de){
 		//should we null the datasource?  Close the connection?
 		//What we really need is a connection pool, seperate for
 		//transactions and requests.
 		LOGGER.info("could not roll back datasource");
 	    }
 	    throw new WfsTransactionException("Problem updating features: " 
 					      +e.toString() + " cause: " + 
 					      e.getCause(), handle);
 	} catch (SchemaException e) {
 	     throw new WfsTransactionException(e.toString(), handle);
 	}
     }
 
     /**
      * Performs the delete operation.
      * @param delete the request to perform.
      * @param data the datasource to remove features from.
      */
     private static void doDelete(DeleteRequest delete, DataSource data) 
 	throws WfsTransactionException {
 	String retString = new String();
 	String handle = delete.getHandle();
 	try {
 	    data.removeFeatures(delete.getFilter());
 	} catch (DataSourceException e) {
 	    LOGGER.info("Problem with datasource " + e + " cause: " 
 			+ e.getCause());
 	    try {
 		data.rollback();
 	    } catch (DataSourceException de){
 		//should we null the datasource?  Close the connection?
 		//What we really need is a connection pool, seperate for
 		//transactions and requests.
 		LOGGER.info("could not roll back datasource");
 	    }
 	    throw new WfsTransactionException("Problem removing features: " 
 					      + e.getCause(),
 					      delete.getHandle());
 	}
 
     }
     
 }
