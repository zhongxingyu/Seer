 /* Copyright (c) 2001 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root 
  * application directory.
  */
 package org.vfny.geoserver.requests;
 
 import java.io.*;
 import java.util.*;
 import java.util.logging.Logger;
 import org.geotools.filter.Filter;
 import org.vfny.geoserver.responses.WfsTransactionException;
 
 /**
  * Transactional request object, consisting of one or more sub transactional
  * requests.
  *
  * @author Rob Hranac, TOPP
  * @author Chris Holmes, TOPP
  * @version $version$
  */
 public class TransactionRequest 
     extends Request {
 
     /** Standard logging instance for class */
     private static final Logger LOGGER = 
         Logger.getLogger("org.vfny.geoserver.requests");
     
     protected List subRequests = new ArrayList();
         
     protected String lockId = null;
 
     protected boolean releaseAll = true;
 
    /** Specifices the user-defined name for the entire get feature request */
     protected String handle = null;
 
     /** Empty constructor. */ 
     public TransactionRequest () { setRequest("Transaction"); }
 
 
     /*************************************************************************
      * SIMPLE GET AND SET METHODS                                            *
      *************************************************************************/
 
     /** adds a delete, insert, or update request to this transaction. */
     public void addSubRequest(SubTransactionRequest subRequest) {
         subRequests.add(subRequest);
     }
 
     /** 
      * Gets the request held at position i.
      *
      * @param i the position of the operation request to get.
      * @return the request at i.
      */
     public SubTransactionRequest getSubRequest(int i) {
 	//if (subRequest.size() == 0 || subRequest.size < i){
 	    return (SubTransactionRequest) subRequests.get(i);
 	    //} else {
 	    //throw new WfsTransactionException("
     }
 
     /** Gets the number of delete, update, and insert operations*/
     public int getSubRequestSize() {
         return subRequests.size();
     }
 
     /** Sets the  user-defined name for the entire Transaction request. */
     public void setHandle(String handle) { this.handle = handle; }
     
     /** Returns the user-defined name for the entire Transaction request. */ 
     public String getHandle() { return handle; }
 
     
     public void setReleaseAll(String releaseAction) 
 	throws WfsTransactionException {
 	if (releaseAction != null) {
 	    if(releaseAction.toUpperCase().equals("ALL")) {
                 this.releaseAll = true;                
             } else if(releaseAction.toUpperCase().equals("SOME")) {
                 this.releaseAll = false;                
             } else {
                 throw new WfsTransactionException("Illegal releaseAction: " + 
 						  releaseAction + ", only " +
 						  "SOME or ALL allowed", 
 						  handle, handle);
             }
 	}
     }
 
     /** Sets the release for the Transaction request. */
     public void setReleaseAll(boolean release) { this.releaseAll = release; }
     
     /** Returns the release for the Transaction request. */ 
     public boolean getReleaseAll() { return releaseAll; }
 
     /** Sets the lock id for the entire Transaction request. */
     public void setLockId(String lockId) { this.lockId = lockId; }
     
     /** Returns the lock id for the entire Transaction request. */ 
     public String getLockId() { return lockId; }
     
     public String toString() {
 	StringBuffer tRequest = new StringBuffer("Lock Id: " + lockId + "\n");
 	tRequest.append("release all: " + releaseAll + "\n");
	tRequest.append("\nhandle: " + handle + "\n");
 	for (int i = 0; i < subRequests.size(); i++) {
 	    tRequest.append(subRequests.get(i).toString() + "\n");
 	}
 	return tRequest.toString();
     }    
 
      /**
      * helper function for equals.  Checks for nulls, as
      * this class can hold nulls, and will be equal if
      * two of the fields are both null.
      *
      * @param mine The field of this object.
      * @param test the field of to test.
      *
      * @return true if mine equals test, including if they
      * are both null.
      */
     private boolean testField(Object mine, Object test) {
 	if (mine != null) {
 	    return mine.equals(test);
 	} else {
 	    return test == null;
 	}
     }
 
     public boolean equals(Object obj) {
 	if (obj != null && obj.getClass() == this.getClass()){
 	    TransactionRequest testTrans = (TransactionRequest)obj;
 	    boolean isEqual = true;
 	    if(this.releaseAll == testTrans.getReleaseAll()) {
 		isEqual = testField(this.lockId, testTrans.getLockId());
 		isEqual = testField(this.handle, testTrans.getHandle()) && isEqual;
 		//isEqual = testField(this.version, testTrans.version) && isEqual;
 		if(this.subRequests.size() == testTrans.subRequests.size()){
 		    for(int i = 0; i < subRequests.size(); i++) {
 			isEqual = isEqual && subRequests.contains
 			    (testTrans.getSubRequest(i));
 		    }
 		} else {
 		    isEqual = false;
 		}
 	    } else {
 		isEqual = false;
 	    }
 	    return isEqual;
 	} else {
 	    return false;
 	}
     }
     
     
 }
