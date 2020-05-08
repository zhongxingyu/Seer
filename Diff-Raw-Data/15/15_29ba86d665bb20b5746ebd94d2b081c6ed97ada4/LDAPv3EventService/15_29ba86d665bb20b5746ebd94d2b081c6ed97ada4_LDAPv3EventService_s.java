 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: LDAPv3EventService.java,v 1.10 2006-12-13 02:02:54 kenwho Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.idm.plugins.ldapv3;
 
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import netscape.ldap.LDAPAttribute;
 import netscape.ldap.LDAPAttributeSet;
 import netscape.ldap.LDAPConnection;
 import netscape.ldap.LDAPControl;
 import netscape.ldap.LDAPEntry;
 import netscape.ldap.LDAPException;
 import netscape.ldap.LDAPMessage;
 import netscape.ldap.LDAPResponse;
 import netscape.ldap.LDAPSearchConstraints;
 import netscape.ldap.LDAPSearchListener;
 import netscape.ldap.LDAPSearchResult;
 import netscape.ldap.LDAPSearchResultReference;
 import netscape.ldap.LDAPSearchResults;
 import netscape.ldap.LDAPv2;
 import netscape.ldap.LDAPv3;
 import netscape.ldap.controls.LDAPEntryChangeControl;
 import netscape.ldap.controls.LDAPPersistSearchControl;
 import netscape.ldap.controls.LDAPProxiedAuthControl;
 import netscape.ldap.factory.JSSESocketFactory;
 
 import com.sun.identity.idm.IdRepoListener;
 import com.sun.identity.shared.debug.Debug;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.idm.IdRepoBundle;
 import com.sun.identity.idm.IdRepoException;
 
 /**
  * Event Service monitors changes on the server. Implemented with the persistant
  * search control. Uses ldapjdk asynchronous interfaces so that multiple search
  * requests can be processed by a single thread
  * 
  * The Type of changes that can be monitored are: 
  * - LDAPPersistSearchControl.ADD 
  * - LDAPPersistSearchControl.DELETE 
  * - LDAPPersistSearchControl.MODIFY 
  * - LDAPPersistSearchControl.MODDN
  * 
  * A single connection is established initially and reused to service all
  * notification requests.
  * 
  */
 public class LDAPv3EventService implements Runnable {
 
     // list that holds notification requests
     Hashtable _requestList = new Hashtable();
 
     // Thread that listens to DS notifications
     Thread _monitorThread = null;
 
     // search listener for asynch ldap searches
     LDAPSearchListener _msgQueue;
 
     // default server port
     private final int DEFAULTPORT = 389;
 
     private int connNumRetry = 3;
 
     private int connRetryInterval = 1000;
 
     // Don't want the server to return all the
     // entries. return only the changes.
     private static final boolean CHANGES_ONLY = true;
 
     // Want the server to return the entry
     // change control in the search result
     private static final boolean RETURN_CONTROLS = true;
 
     // Don't perform search if Persistent
     // Search control is not supported.
     private static final boolean IS_CRITICAL = true;
 
     protected static Debug debugger = Debug.getInstance("LDAPv3EventService");
 
     // Parameters in AMConfig, that provide values for connection retries
     protected static final String EVENT_CONNECTION_NUM_RETRIES = 
         "com.iplanet.am.event.connection.num.retries";
 
     protected static final String EVENT_CONNECTION_RETRY_INTERVAL = 
         "com.iplanet.am.event.connection.delay.between.retries";
 
     protected static final String EVENT_CONNECTION_ERROR_CODES = 
         "com.iplanet.am.event.connection.ldap.error.codes.retries";
 
     // Idle timeout in minutes
     protected static final String EVENT_IDLE_TIMEOUT_INTERVAL = 
         "com.sun.am.event.connection.idle.timeout";
 
     private static final String LDAPv3Config_LDAP_SERVER = 
         "sun-idrepo-ldapv3-config-ldap-server";
 
     private static final String LDAPv3Config_LDAP_IDLETIMEOUT = 
         "sun-idrepo-ldapv3-config-idletimeout";
 
     private static final String LDAPv3Config_LDAP_NUM_RETRIES = 
         "sun-idrepo-ldapv3-config-numretires";
 
     private static final String LDAPv3Config_LDAP_RETRY_INTERVAL = 
         "com.iplanet.am.ldap.connection.delay.between.retries";
 
     private static final String LDAPv3Config_LDAP_ERROR_CODES = 
         "sun-idrepo-ldapv3-config-errorcodes";
 
     private static final String LDAPv3Config_LDAP_SSL_ENABLED = 
         "sun-idrepo-ldapv3-config-ssl-enabled";
 
     private static final String LDAPv3Config_LDAP_PORT =
         "sun-idrepo-ldapv3-config-ldap-port";
 
     private static final String LDAPv3Config_AUTHID = 
         "sun-idrepo-ldapv3-config-authid";
 
     private static final String LDAPv3Config_AUTHPW =
         "sun-idrepo-ldapv3-config-authpw";
     
     private static final String LDAPv3Config_LDAP_TIME_LIMIT =
         "sun-idrepo-ldapv3-config-time-limit";
 
     private static final String CLASS_NAME = 
         "com.sun.identity.idm.plugins.ldapv3.LDAPv3EventService";
 
     private int _numRetries = 3;
 
     private int _retryInterval = 3000;
 
     protected HashSet _retryErrorCodes;
 
     // Connection Time Out parameters
     protected int _idleTimeOut = 0; // Idle timeout in minutes.
 
     protected long _idleTimeOutMills;
 
     private boolean pSearchSupported = false;
 
     int randomID = 0;
 
     // for Active Directory
     private static final String ATTR_WHEN_CREATED = "whenCreated";
 
     private static final String ATTR_WHEN_CHANGED = "whenChanged";
 
     private static final String ATTR_IS_DELETED = "isDeleted";
 
     private static final String ATTR_OBJECT_GUID = "objectGUID";
 
     private static final String AD_NOTIFICATION_OID = "1.2.840.113556.1.4.528";
 
     private boolean adNotificationSupported = false;
 
     private int getPropertyIntValue(Map configParams, String key,
             int defaultValue) {
         int value = defaultValue;
         try {
             Set valueSet = (Set) configParams.get(key);
             if (valueSet != null && !valueSet.isEmpty()) {
                 value = Integer.parseInt((String) valueSet.iterator().next());
             }
         } catch (NumberFormatException nfe) {
             value = defaultValue;
         }
         if (debugger.messageEnabled()) {
             debugger.message("LDAPv3EventService.getPropertyIntValue(): " + key
                     + " = " + value);
         }
         return value;
     }
 
     private String getPropertyStringValue(Map configParams, String key) {
         String value = null;
         Set valueSet = (Set) configParams.get(key);
         if (valueSet != null && !valueSet.isEmpty()) {
             value = (String) valueSet.iterator().next();
         } else {
             debugger.error("LDAPv3EventService.getPropertyStringValue failed:"
                     + key);
         }
 
         if (debugger.messageEnabled()) {
             if (!key.equals(LDAPv3Config_AUTHPW)) {
                 debugger.message("LDAPv3EventService.getPropertyStringValue(): "
                                 + key + " = " + value);
             } else {
                 if ((value == null) || (value.length() == 0)) {
                     debugger.message(
                             "LDAPv3EventService.getPropertyStringValue(): "
                                     + key + " = NULL or ZERO LENGTH");
                 } else {
                     debugger.message(
                             "LDAPv3EventService.getPropertyStringValue(): "
                                     + key + " = has value XXX");
                 }
             }
         }
 
         return value;
     }
 
     private HashSet getPropertyRetryErrorCodes(Map configParams, String key) {
         HashSet codes = new HashSet();
         Set retryErrorSet = (Set) configParams.get(key);
         Iterator itr = retryErrorSet.iterator();
         while (itr.hasNext()) {
             codes.add(itr.next());
         }
         if (debugger.messageEnabled()) {
             debugger.message("LDAPv3EventService.getPropertyRetryErrorCodes: "
                     + key + "retryErrorSet=" + retryErrorSet + " ; codes="
                     + codes);
         }
         return codes;
     }
 
     /**
      * Private Constructor
      */
     protected LDAPv3EventService(Map pluginConfig, String serverNames)
             throws LDAPException {
         // Determine the Idle time out value for Event Service (LB/Firewall)
         // scenarios. Value == 0 imples no idle timeout.
         Random randomGen = new Random();
         randomID = randomGen.nextInt();
         if (debugger.messageEnabled()) {
             debugger.message("LDAPv3EventService.constructor. randomID="
                     + randomID);
         }
         _idleTimeOut = getPropertyIntValue(pluginConfig,
                 LDAPv3Config_LDAP_IDLETIMEOUT, _idleTimeOut);
         _idleTimeOutMills = _idleTimeOut * 60000;
 
         // Determine the Number of retries for Event Service Connections
         _numRetries = getPropertyIntValue(pluginConfig,
                 LDAPv3Config_LDAP_NUM_RETRIES, _numRetries);
 
         // Determine the Event Service retry interval.
         _retryInterval = getPropertyIntValue(pluginConfig,
                 LDAPv3Config_LDAP_RETRY_INTERVAL, _retryInterval);
 
         // Determine the Event Service retry error codes
         _retryErrorCodes = getPropertyRetryErrorCodes(pluginConfig,
                 LDAPv3Config_LDAP_ERROR_CODES);
 
         LDAPConnection lc = null;
         try {
             lc = getConnection(pluginConfig, serverNames);
         } catch (LDAPException le) {
             debugger.error("EventService.constructor - Failed to "
                     + "connect to server." + " randomID=" + randomID, le);
             throw le;
         }
 
         int retry = 0;
         while (retry <= 3) {
             try {
                 checkSupportedControls(lc);
                 break;
             } catch (LDAPException le) {
                 debugger.error("EventService.constructor  retry=" + retry);
                 debugger.error("EventService.constructor - Failed to "
                         + "determine if server supports control."
                         + " randomID=" + randomID, le);
                 if (retry == 3) {
                     throw le;
                 } else {
                     sleepRetryInterval();
                     retry++;
                 }
             }
         } // while
         lc.disconnect();
     }
 
     protected String getName() {
         return "LDAPv3EventService";
     }
 
     /**
      * At the end, close THE Event Manager's connections Abandon all previous
      * persistent search requests.
      *
      */
     public void finalize() {
         Collection requestObjs = _requestList.values();
         Iterator iter = requestObjs.iterator();
         while (iter.hasNext()) {
             Request request = (Request) iter.next();
             removeListener(request);
         }
         _requestList.clear();
     }
 
     private Request findRequst(LDAPv3Repo target) {
         if (debugger.messageEnabled()) {
             debugger.message("LDAPv3EventService.findRequest: requestSize="
                     + _requestList.size() + " target=" + target);
         }
         Request owner = null;
         Collection requestObjs = _requestList.values();
         Iterator iter = requestObjs.iterator();
         while (iter.hasNext()) {
             Request request = (Request) iter.next();
             LDAPv3Repo tmpOwner = request.getOwner();
             if (debugger.messageEnabled()) {
                 debugger.message("LDAPv3EventService.findRequest: request="
                         + tmpOwner);
             }
             if (tmpOwner == target) {
                 owner = request;
                 debugger.message("LDAPv3EventService.findRequest. found it");
                 break;
             }
         }
         return owner;
     }
 
     public synchronized void removeListener(LDAPv3Repo target) {
         // get the request for this object.
         // set the request status to stop.
         // when the request count is 0. the thread should exit.
 
         if (debugger.messageEnabled()) {
             debugger.message("LDAPv3EventService.removeListener: target="
                     + target);
         }
         Request myRequest = findRequst(target);
        myRequest.setStopStatus(true);
        _requestList.remove(myRequest);
        removeListener(myRequest);
        dispatchEventAllChanged(myRequest);
        // generate a interupt to wake up the process
         // should we generate interrupt before or after removing listener.
         // we want to interrupt after removing request from _requestList
         // so the all relevent info will have been updated when the tread
         // wakes up.
         _monitorThread.interrupt();
     }
 
     /**
      * Adds a listener to the directory.
      *
      */
     public synchronized String addListener(SSOToken token,
             IdRepoListener listener, String base, int scope, String filter,
             int operations, Map pluginConfig, LDAPv3Repo pluginInstance,
             String serverNames) throws LDAPException, IdRepoException {
 
         if (!pSearchSupported && !adNotificationSupported) {
             debugger.error("LDAPv3EventService.addListener: unable to " +
                     "determine if psearch or notification is supported."
                             + " randomID=" + randomID);
             Object[] args = { CLASS_NAME };
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "218", args);
         }
         LDAPConnection lc = null;
         try {
             lc = getConnection(pluginConfig, serverNames);
         } catch (LDAPException le) {
             debugger.error("LDAPv3EventService.addListener: "
                     + "unable to connect to ldap server. randomID=" + randomID);
             throw le;
         }
 
         LDAPSearchConstraints cons = lc.getSearchConstraints();
 
         String[] attrs = null;
         if (pSearchSupported) {
 
             // Create Persistent Search Control object
             LDAPPersistSearchControl psearchCtrl = new LDAPPersistSearchControl(
                     operations, CHANGES_ONLY, RETURN_CONTROLS, IS_CRITICAL);
 
             // Add LDAPControl array to the search constraint object
             cons.setServerControls(psearchCtrl);
             // Listeners can not read attributes from the event.
             // Request only javaClassName to be able to determine object type
             attrs = new String[] { "objectclass" };
         } else {
             LDAPControl adCtrl = new LDAPControl(AD_NOTIFICATION_OID, true,
                     new byte[] {});
             cons.setServerControls(adCtrl);
             attrs = new String[] { "objectclass", ATTR_WHEN_CREATED,
                     ATTR_WHEN_CHANGED, ATTR_IS_DELETED, ATTR_OBJECT_GUID };
             if (filter == null) {
                 debugger.error("LDAPv3EventService.addListener: "
                         + "Filter has to be (objectclass=*)");
                 Object[] args = { CLASS_NAME };
                 throw new IdRepoException(
                         IdRepoBundle.BUNDLE_NAME, "218", args);
             }
         }
 
         cons.setBatchSize(1);
 
         // Listeners can not read attributes from the event.
         // Request only javaClassName to be able to determine object type
         LDAPSearchListener searchListener = null;
         // Set (asynchronous) persistent search request in the DS
         try {
             if (debugger.messageEnabled()) {
                 debugger.message("LDAPv3EventService.addListener() - " +
                         "Submitting Persistent Search on: " + base + 
                         " for listener: " + listener + " scope=" + scope +
                         " randomID=" + randomID+ " serverNames=" + serverNames);
             }
 
             searchListener = lc.search(base, scope, filter, attrs, false, null,
                     cons);
         } catch (LDAPException le) {
             debugger.error("LDAPv3EventService.addListener() - Failed to set "
                     + "Persistent Search" + " randomID=" + randomID
                     + le.getMessage());
             throw le;
         }
 
         int[] outstandingRequests = searchListener.getMessageIDs();
         int id = outstandingRequests[outstandingRequests.length - 1];
 
         String reqID = Integer.toString(id);
         long startTime = System.currentTimeMillis();
         Request request = new Request(id, reqID, token, base, scope, filter,
                 attrs, operations, listener, lc, startTime, pluginConfig,
                 pluginInstance, serverNames);
         _requestList.put(reqID, request);
 
         // Add this search request to the m_msgQueue so it can be
         // processed by the monitor thread
         if (_msgQueue == null) {
             _msgQueue = searchListener;
         } else {
             _msgQueue.merge(searchListener);
             if (debugger.messageEnabled()) {
                 outstandingRequests = _msgQueue.getMessageIDs();
                 debugger.message(
                         "LDAPv3EventService.addListener(): merged Listener: " +
                         "requestID: " + reqID + " & Request: " + request + 
                         " on to message Queue. No. of current outstanding " + 
                         "requests = " + outstandingRequests.length + 
                         " randomID=" + randomID + " serverNames=" + 
                         serverNames);
             }
         }
 
         // Create new (LDAPv3EventService) Thread, if one doesn't exist.
         startMonitorThread();
         return reqID;
     }
 
     /**
      * Main monitor thread loop. Wait for persistent search change notifications
      *
      */
     public void run() {
         if (debugger.messageEnabled()) {
             debugger.message(
                     "LDAPv3EventService.run(): Event Thread is running! No " +
                     "Idle timeout Set: " + _idleTimeOut + " minutes." + 
                     " randomID=" + randomID);
         }
 
         boolean successState = true;
         LDAPMessage message = null;
         while (successState) {
             try {
                 try {
                     if (debugger.messageEnabled()) {
                         debugger.message(
                                 "LDAPv3EventService.run(): Waiting for " +
                                 "response" + " randomID=" + randomID);
                     }
                     message = _msgQueue.getResponse();
                     successState = processResponse(message);
                 } catch (LDAPException ex) {
                     int resultCode = ex.getLDAPResultCode();
                     if (debugger.warningEnabled()) {
                         debugger.warning(
                                 "LDAPv3EventService.run() LDAPException " +
                                 "received:" + " randomID=" + randomID, ex);
                     }
                     if (_retryErrorCodes.contains("" + resultCode)) {
                         successState = resetAllSearches(true);
                     } else { // Some other network error
                         processNetworkError(ex);
                     }
                 }
             } catch (Throwable t) {
                 // Catching Throwable to prevent the thread from exiting.
                 if (debugger.warningEnabled()) {
                     debugger.warning(
                             "LDAPv3EventService.run(): Unknown exception "
                                     + "caught. Sleeping for a while.. "
                                     + " randomID=" + randomID, t);
                 }
                 if (_requestList.size() > 0) {
                     sleepRetryInterval();
                 }
             }
             // if no more request exit.
             if (debugger.warningEnabled()) {
                 debugger.warning(
                         "LDAPv3EventServicePolling.run(). _requestList.size()="
                                 + _requestList.size());
             }
             if (_requestList.size() == 0) {
                 successState = false;
             }
         } // end of while loop
 
         finalize(); // ok to remove since number of request will always be zero
                     // anyway.
 
         if (debugger.warningEnabled()) {
             debugger.warning("LDAPv3EventService.run() - Monitor thread is "
                     + "terminating! Persistent Searches will no longer be "
                     + "operational." + " randomID=" + randomID);
         }
         return; // Gracefully exit the thread
     } // end of thread
 
     private void startMonitorThread() {
         if (_monitorThread == null || (!_monitorThread.isAlive())) {
             // Even if the monitor thread is not alive, we should use the
             // same instance of Event Service object (as it maintains all
             // the listener information)
             _monitorThread = new Thread(this, getName());
             _monitorThread.setDaemon(true);
             _monitorThread.start();
         }
     }
 
     /**
      * Method which process the Response received from the DS.
      * 
      * @param message -
      *            the LDAPMessage received as response
      * @return true if the reset was successful. False Otherwise.
      */
     protected synchronized boolean processResponse(LDAPMessage message) {
         if (message == null) {
             // Some problem with the message queue. We should
             // try to reset it.
             debugger.warning(
                     "LDAPv3EventService.processResponse() - Received a " +
                     "NULL Response. Attempting to re-start persistent " +
                     "searches" + " randomID=" + randomID);
             return resetAllSearches(false); // return false if not successful
         }
 
         if (debugger.messageEnabled()) {
             debugger.message("LDAPv3EventService.processResponse() - received "
                     + "DS message  => " + message.toString() + " randomID="
                     + randomID);
         }
 
         // To determine if the monitor thread needs to be stopped.
         boolean successState = true;
 
         Request request = getRequestEntry(message.getMessageID());
 
         // If no listeners, abandon this message id
         if (request == null) {
             // We do not have anything stored about this message id.
             // So, just log a message and do nothing.
             if (debugger.messageEnabled()) {
                 debugger.message(
                         "LDAPv3EventService.processResponse() - Received " +
                         "ldap message with unknown id = " + 
                         message.getMessageID() + " randomID=" + randomID);
             }
         } else if (message instanceof LDAPSearchResult) {
             // then must be a LDAPSearchResult carrying change control
             processSearchResultMessage((LDAPSearchResult) message, request);
             request.setLastUpdatedTime(System.currentTimeMillis());
         } else if (message instanceof LDAPResponse) {
             // Check for error message ...
             LDAPResponse rsp = (LDAPResponse) message;
             successState = processResponseMessage(rsp, request);
         } else if (message instanceof LDAPSearchResultReference) { // Referral
             processSearchResultRef(
                     (LDAPSearchResultReference) message, request);
         }
         return successState;
     }
 
     /**
      * removes the listener from the list of Persistent Search listeners of the
      * asynchronous seach for the given search ID.
      * 
      * @param request
      *            The request ID returned by the addListener
      *
      */
     protected void removeListener(Request request) {
         if (debugger.messageEnabled()) {
             debugger.message("LDAPv3EventService.removeListener: " +
                     "_requestList.size()=" + _requestList.size());
         }
         LDAPConnection connection = request.getLDAPConnection();
         if (connection != null) {
             if (debugger.messageEnabled()) {
                 debugger.message("LDAPv3EventService.removeListener(): " +
                         "Removing " + "listener requestID: "+ 
                         request.getRequestID() + " Listener: " + 
                         request.getListener() + " randomID=" + randomID + 
                         " serverNames=" + request.getServerNames());
             }
             try {
                 connection.abandon(request.getId());
                 connection.disconnect();
             } catch (LDAPException le) {
                 // Might have to check the reset codes and try to reset
                 if (debugger.warningEnabled()) {
                     debugger.warning("LDAPv3EventService.removeListener(): "
                             + "LDAPException, when trying to remove listener"
                             + " randomID=" + randomID, le);
                 }
             }
         }
     }
 
     /**
      * Reset all searches. Clear cache only if true is passed to argument
      * 
      * @param clearCaches
      *          generate a event to clears the cache if <code>true</code>
      *          before resetting all searches.
      * @return true if successfully reset all searches.
      */
     protected synchronized boolean resetAllSearches(boolean clearCaches) {
 
         Hashtable tmpReqList = (Hashtable) _requestList.clone();
         _requestList.clear(); // Will be updated in addListener method
         Collection reqList = tmpReqList.values();
 
         int retry = 1;
         boolean doItAgain = ((_numRetries == -1) 
                 || ((_numRetries != 0) && (retry <= _numRetries))) 
                 ? true
                 : false;
 
         if (clearCaches) {
             dispatchAllEntriesChangedEvent();
         }
         while (doItAgain) { // Re-try starts from 0.
             try {
                 sleepRetryInterval();
                 if (debugger.messageEnabled()) {
                     String str = (_numRetries == -1) ? "indefinitely" : Integer
                             .toString(retry);
                     debugger.message("LDAPv3EventService.resetAllSearches(): "
                             + "retrying = " + str + " randomID=" + randomID);
                 }
 
                 // Re-initialize message queue
                 _msgQueue = null;
 
                 // we want to do the addListener in a seperate loop from the
                 // above removeListener because we want to remove all the
                 // listener first then do the add.
                 Iterator iter = reqList.iterator();
                 while (iter.hasNext()) {
                     Request request = (Request) iter.next();
                     // Abandon the search & disconnect
                     // keep remove and add together to minimum down time.
                     if (request.getStopStatus() == false) {
                         removeListener(request);
                         addListener(request.getRequester(), request
                                 .getListener(), request.getBaseDn(), request
                                 .getScope(), request.getFilter(), request
                                 .getOperations(), request.getPluginConfig(),
                                 request.getOwner(), request.getServerNames());
                     }
                 }
                 return true;
             } catch (LDAPException le) {
                 // Ignore exception and retry as we are in the process of
                 // re-establishing the searches. Notify Listeners after the
                 // attempt
                 if (retry == _numRetries) {
                     processNetworkError(le);
                 }
             } catch (IdRepoException ide) {
                 if (retry == _numRetries) {
                     processNetworkError(ide);
                 }
             }
             if (_numRetries != -1) {
                 doItAgain = (++retry <= _numRetries) ? true : false;
             }
         } // end while loop
         if (debugger.warningEnabled()) {
             debugger.warning("LDAPv3EventService.resetAllSearches exit: "
                     + "_requestList.size()=" + _requestList.size());
         }
         return false;
     }
 
     protected void sleepRetryInterval() {
         try {
             Thread.sleep(_retryInterval);
         } catch (InterruptedException ie) {
             // ignore
         }
     }
 
     private void dispatchException(Exception e, Request request) {
         if (debugger.messageEnabled()) {
             debugger.message("LDAPv3EventService.dispatchException() - " 
                     + "dispatching exception to the listener: " 
                     + request.getRequestID()
                     + " Listener: " + request.getListener() + " randomID="
                     + randomID + " serverNames=" + request.getServerNames(), e);
         }
         // el.eventError(e.toString());
     }
 
     private void dispatchEventAllChanged(Request request) {
         IdRepoListener el = request.getListener();
         LDAPv3Repo myldapv3 = request.getOwner();
         myldapv3.clearCache();
         el.allObjectsChanged();
     }
 
     /**
      * Dispatch naming event to all listeners
      */
     private void dispatchEvent(String dn, int changeType, Request request) {
         IdRepoListener el = request.getListener();
         Map configParams = el.getConfigMap();
         if (debugger.messageEnabled()) {
             debugger.message("LDAPv3EventService.dispatchEvent() - dn=" + dn
                     + " changeType=" + changeType + " configParams="
                     + configParams);
         }
         LDAPv3Repo myldapv3 = request.getOwner();
         myldapv3.objectChanged(dn, changeType);
         el.objectChanged(dn, changeType, configParams);
     }
 
     /**
      * Dispatch event to all listeners for all entris changed
      * 
      */
     private synchronized void dispatchAllEntriesChangedEvent() {
         Collection reqList = _requestList.values();
         Iterator iter = reqList.iterator();
         while (iter.hasNext()) {
             Request req = (Request) iter.next();
             IdRepoListener el = req.getListener();
             el.allObjectsChanged();
             LDAPv3Repo myldapv3 = req.getOwner();
             myldapv3.clearCache();
         }
     }
 
     /**
      * On network error, create ExceptionEvent and delever it to all listeners
      * on all events.
      */
     protected void processNetworkError(Exception ex) {
         Collection reqList = _requestList.values();
         Iterator iter = reqList.iterator();
         while (iter.hasNext()) {
             Request request = (Request) iter.next();
             dispatchException(ex, request);
         }
     }
 
     /**
      * Response message carries a LDAP error. Response with the code 0
      * (SUCCESS), should never be received as persistent search never completes,
      * it has to be abandon. Referral messages are ignored
      */
     private boolean processResponseMessage(LDAPResponse rsp, Request request) {
         if (debugger.messageEnabled()) {
             debugger.message(
                     "LDAPv3EventService.processResponseMessage().entry - "
                             + "request=" + request + " rsp=" + rsp);
         }
         boolean successState = true;
         if (_retryErrorCodes.contains("" + rsp.getResultCode())) {
             if (debugger.messageEnabled()) {
                 debugger.message(
                         "LDAPv3EventService.processResponseMessage() - "
                                 + "received LDAP Response for requestID: "
                                 + request.getRequestID()
                                 + " Listener: "
                                 + request.getListener()
                                 + "Need restarting"
                                 + " randomID="
                                 + randomID
                                 + " serverNames="
                                 + request.getServerNames());
             }
             successState = resetAllSearches(false);
         } else if (rsp.getResultCode() != 0
                 || rsp.getResultCode() != LDAPException.REFERRAL) {
             // If not neither of the cases then
             LDAPException ex = new LDAPException("Error result", rsp
                     .getResultCode(), rsp.getErrorMessage(), 
                                             rsp.getMatchedDN());
             dispatchException(ex, request);
             if (rsp.getResultCode() == LDAPException.INSUFFICIENT_ACCESS_RIGHTS)
                 {
                 // if annoyous bind and it does have sufficient access. quit.
                 successState = false;
             }
         } else {
             sleepRetryInterval();
         }
         return successState;
     }
 
     /**
      * Process change notification attached as the change control to the message
      */
     private synchronized void processSearchResultMessage(LDAPSearchResult res,
             Request req) {
         if (debugger.messageEnabled()) {
             debugger.message(
                     "LDAPv3EventService.processSearchResultMessage().entry"
                             + " res=" + res + " req=" + req);
         }
         LDAPEntry modEntry = res.getEntry();
 
         LDAPAttributeSet findAttrs = modEntry.getAttributeSet();
         Enumeration enumAttrs = findAttrs.getAttributes();
         while (enumAttrs.hasMoreElements()) {
             LDAPAttribute anAttr = (LDAPAttribute) enumAttrs.nextElement();
             Enumeration enumVals = anAttr.getStringValues();
             while (enumVals.hasMoreElements()) {
                 enumVals.nextElement();
             }
         }
 
         if (debugger.messageEnabled()) {
             debugger.message
             ("LDAPv3EventService.processSearchResultMessage() - "
                             + "Changed "
                             + modEntry.getDN()
                             + " randomID="
                             + randomID);
         }
 
         if (pSearchSupported) {
 
             /* Get any entry change controls. */
             LDAPControl[] ctrls = res.getControls();
 
             // Can not create event without change control
             if (ctrls == null) {
                 Exception ex = new Exception(
                         "LDAPv3EventService - Cannot create "
                                 + "NamingEvent, no change control info");
                 dispatchException(ex, req);
             } else {
                 // Multiple controls might be in the message
                 for (int i = 0; i < ctrls.length; i++) {
                     LDAPEntryChangeControl changeCtrl = null;
 
                     if (ctrls[i] instanceof LDAPEntryChangeControl) {
                         changeCtrl = (LDAPEntryChangeControl) ctrls[i];
                         if (debugger.messageEnabled()) {
                             debugger.message(
                                     "LDAPv3EventService." +
                                     "processSearchResultMessage() changeCtrl = "
                                             + changeCtrl.toString()
                                             + " randomID=" + randomID);
                         }
 
                         // Can not create event without change control
                         if (changeCtrl.getChangeType() == -1) {
                             Exception ex = new Exception(
                                     "LDAPv3EventService - Cannot create " +
                                     "NamingEvent, no change control info");
                             dispatchException(ex, req);
                         }
 
                         // Convert control into a DSEvent and dispatch to
                         // listeners
                         try {
                             dispatchEvent(modEntry.getDN(), changeCtrl
                                     .getChangeType(), req);
                         } catch (Exception ex) {
                             dispatchException(ex, req);
                         }
                     }
                 }
             }
         } else {
             // AD Notification
             try {
                 createDSEventAD(modEntry, req);
             } catch (Exception ex) {
                 dispatchException(ex, req);
             }
         }
     }
 
     /**
      * Search continuation messages are ignored.
      */
     private void processSearchResultRef(LDAPSearchResultReference ref,
             Request req) {
         // Do nothing, message ignored, do not dispatch ExceptionEvent
         if (debugger.messageEnabled()) {
             debugger.message("LDAPv3EventService.processSearchResultRef() - "
                     + "Ignoring.." + " randomID=" + randomID);
         }
     }
 
     /**
      * Find event entry by message ID
      */
     private Request getRequestEntry(int id) {
         return (Request) _requestList.get(Integer.toString(id));
     }
 
     private LDAPConnection getConnection(Map pluginConfig, String serverNames)
             throws LDAPException {
 
         LDAPConnection conn = null;
 
         String ssl = getPropertyStringValue(pluginConfig,
                 LDAPv3Config_LDAP_SSL_ENABLED);
 
         // serverNames is a list of ldap sever names seperated by sapce for
         // failover purposes. LDAPConnection will automatcially handle failover.
         if (debugger.messageEnabled()) {
             debugger.message("    LDAPv3Config_LDAP_SERVER: serverNames"
                     + serverNames);
         }
 
         // port will not be used since serverNames is in the following format:
         // nameOfLDAPhost:portNumber.
         int ldapPort = DEFAULTPORT;
 
         String authid = getPropertyStringValue(pluginConfig,
                 LDAPv3Config_AUTHID);
         String authpw = getPropertyStringValue(pluginConfig,
                 LDAPv3Config_AUTHPW);
 
         if (ssl != null && ssl.equalsIgnoreCase("true")) {
             try {
                 conn = new LDAPConnection(new JSSESocketFactory(null));
             } catch (Exception e) {
                 debugger.error("LDAPv3EventService.getConnection."
                         + "JSSESocketFactory randomID=" + randomID, e);
                 int errorCode = LDAPException.OPERATION_ERROR;
                 throw new LDAPException(e.getMessage(), errorCode);
             }
         } else {
             conn = new LDAPConnection();
         }
 
         int timeLimit = getPropertyIntValue(pluginConfig,
                 LDAPv3Config_LDAP_TIME_LIMIT, 3);
         int retry = 0;
         while(retry <= connNumRetry) {
             if (debugger.messageEnabled()) {
                 debugger.message("LDAPv3EventService.GetConnection retry: "
                     + retry + " randomID=" + randomID);
             }
 
             try {
                 conn.setOption(LDAPv3.PROTOCOL_VERSION, new Integer(3));
                 conn.setOption(LDAPv2.TIMELIMIT, new Integer(0));
                 conn.setOption(LDAPv2.SIZELIMIT, new Integer(0));
                 LDAPSearchConstraints constraints = conn.getSearchConstraints();
                 conn.setSearchConstraints(constraints);
 
                 if (timeLimit > 0) {
                     conn.setConnectTimeout(timeLimit);
                 } else {
                     conn.setConnectTimeout(3);
                 }
                 if ((authid != null) && (authpw != null)) {
                     conn.connect(3, serverNames, ldapPort, authid, authpw);
                 } else {
                     conn.setOption(LDAPv3.PROTOCOL_VERSION, new Integer(3));
                     conn.connect(serverNames, ldapPort);
                 }
                 constraints = conn.getSearchConstraints();
                 constraints.setServerTimeLimit(0);
                 conn.setSearchConstraints(constraints);
                 conn.setOption(LDAPv2.SIZELIMIT, new Integer(0));
                 break;
             }
             catch (LDAPException e) {
                 try {
                     conn.disconnect();
                 } catch (LDAPException lde) {
                     debugger.message("LDAPv3EventService disconnct " +
                         " excection: " + lde.getLDAPResultCode());
                 }
                 if (!_retryErrorCodes.contains(""+ e.getLDAPResultCode())||
                     retry == connNumRetry ) {
                     debugger.error("LDAPv3EventService.Connection to " +
                         "LDAP server threw exception: " +
                         " randomID=" + randomID, e);
                     throw e;
                 }
                 retry++;
                 try {
                     Thread.sleep(connRetryInterval);
                 }
                 catch (InterruptedException ex) {
                 }
             }
         }
 
         return conn;
     }
 
     private void checkSupportedControls(LDAPConnection conn)
             throws LDAPException {
         String _FILT = "(objectclass=*)";
         String _BASE = "";
         String _ATTRS[] = { "supportedControl" };
         boolean ProxiedAuthSupported = false;
         LDAPSearchResults res = null;
         String PERSISTENT_SEARCH_CONTROL_OID = "2.16.840.1.113730.3.4.3";
         String PROXIED_AUTH_CONTROL_OID = "2.16.840.1.113730.3.4.12";
 
         try {
             res = conn.search(_BASE, LDAPConnection.SCOPE_BASE, _FILT, _ATTRS,
                     false);
         } catch (LDAPException le) {
             debugger.error("LDAPv3EventService().CheckSupportedControls() - " +
                     "Error encountered while checking for supported controls"
                             + " randomID=" + randomID);
             throw le;
         }
 
         while (res.hasMoreElements()) {
             LDAPEntry findEntry = (LDAPEntry) res.nextElement();
             LDAPAttributeSet findAttrs = findEntry.getAttributeSet();
             Enumeration enumAttrs = findAttrs.getAttributes();
             if (debugger.messageEnabled()) {
                 debugger.message("LDAPv3EventService.checkSupportedControls:"
                         + " findAttrs:" + findAttrs + "; enumAttrs:"
                         + enumAttrs);
             }
             while (enumAttrs.hasMoreElements()) {
                 LDAPAttribute anAttr = (LDAPAttribute) enumAttrs.nextElement();
                 String attrName = anAttr.getName();
                 Enumeration enumVals = anAttr.getStringValues();
                 if (debugger.messageEnabled()) {
                     debugger.message("   inside while: attrName:" + attrName
                             + "; enumVals: " + enumVals);
                 }
 
                 while (enumVals.hasMoreElements()) {
                     String aVal = (String) enumVals.nextElement();
                     if (debugger.messageEnabled()) {
                         debugger.message("   inside inside:  aVal:" + aVal);
                     }
                     // PERSISTENT SEARCH CONTROL OID =
                     // "2.16.840.1.113730.3.4.3";
                     if (aVal.equals(LDAPPersistSearchControl.PERSISTENTSEARCH)) 
                     {
                         pSearchSupported = true;
                     } else if (aVal.equals(AD_NOTIFICATION_OID)) {
                         adNotificationSupported = true;
                     } else if (aVal.equals(
                     // PROXIED AUTH CONTROL OID = "2.16.840.1.113730.3.4.12";
                             LDAPProxiedAuthControl.PROXIEDAUTHREQUEST)) {
                         ProxiedAuthSupported = true;
                     }
                 } // inner most while
             } // inside while
         } // outer most while.
         if (debugger.messageEnabled()) {
             debugger.message("EventService.checkSupportedControls: "
                     + "pSearchSupported = " + pSearchSupported
                     + ", adNotificationSupported = " + adNotificationSupported
                     + " randomID=" + randomID);
         }
     }
 
     public boolean persistentSearchSupported() {
         return pSearchSupported;
     }
 
     public boolean ADNotificationSupported() {
         return adNotificationSupported;
     }
 
     private void createDSEventAD(LDAPEntry entry, Request req)
             throws LDAPException {
 
         if (debugger.messageEnabled()) {
             debugger.message("EventService.createDSEventAD - entry" + " = "
                     + entry);
             debugger.message("EventService.createDSEventAD - Event Requestor"
                     + " = " + req.getRequester() + " randomID=" + randomID);
         }
         int op;
         LDAPAttribute attr = entry.getAttribute(ATTR_IS_DELETED);
         boolean isDeleted = false;
         if (attr != null && attr.size() == 1) {
             isDeleted = attr.getStringValueArray()[0].equalsIgnoreCase("true");
         }
         if (isDeleted) {
             op = LDAPPersistSearchControl.DELETE;
         } else {
             attr = entry.getAttribute(ATTR_WHEN_CREATED);
             if (attr == null || attr.size() == 0) {
                 if (debugger.warningEnabled()) {
                     debugger.warning("EventService.createDSEventAD: missing "
                             + "attribute '" + ATTR_WHEN_CREATED + "'"
                             + " randomID=" + randomID);
 
                 }
                 return;
             }
             String whenCreated = attr.getStringValueArray()[0];
 
             attr = entry.getAttribute(ATTR_WHEN_CHANGED);
             if (attr == null || attr.size() == 0) {
                 if (debugger.warningEnabled()) {
                     debugger.warning("EventService.createDSEventAD: missing "
                             + "attribute '" + ATTR_WHEN_CHANGED + "'"
                             + " randomID=" + randomID);
                 }
                 return;
             }
             String whenChanged = attr.getStringValueArray()[0];
             if (whenCreated.equals(whenChanged)) {
                 op = LDAPPersistSearchControl.ADD;
             } else {
                 op = LDAPPersistSearchControl.MODIFY;
             }
         }
         if ((op & req.getOperations()) == 0) {
             return;
         }
         if (op != LDAPPersistSearchControl.DELETE) {
             dispatchEvent(entry.getDN(), op, req);
         } else {
             // everything changed. mark everything as changed
             dispatchEventAllChanged(req);
         }
 
     }
 }
