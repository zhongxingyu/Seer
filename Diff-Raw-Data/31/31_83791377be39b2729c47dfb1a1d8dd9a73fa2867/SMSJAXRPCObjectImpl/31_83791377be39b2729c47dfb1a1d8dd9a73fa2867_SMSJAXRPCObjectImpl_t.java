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
 * $Id: SMSJAXRPCObjectImpl.java,v 1.6 2006-12-12 23:34:31 arviranga Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.sm.jaxrpc;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 
 import javax.naming.directory.BasicAttribute;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.ModificationItem;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.iplanet.am.util.SystemProperties;
 import com.iplanet.services.comm.server.PLLServer;
 import com.iplanet.services.comm.server.SendNotificationException;
 import com.iplanet.services.comm.share.Notification;
 import com.iplanet.services.comm.share.NotificationSet;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOTokenManager;
 import com.sun.identity.jaxrpc.JAXRPCUtil;
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.shared.xml.XMLUtils;
 import com.sun.identity.sm.CachedSMSEntry;
 import com.sun.identity.sm.SMSEntry;
 import com.sun.identity.sm.SMSException;
 import com.sun.identity.sm.SMSObject;
 import com.sun.identity.sm.SMSObjectListener;
 import com.sun.identity.sm.SMSUtils;
 
 public class SMSJAXRPCObjectImpl implements SMSObjectIF, SMSObjectListener {
 
     static Debug debug = SMSEntry.debug;
 
     static SMSObject smsObject = SMSEntry.getSMSObject();
 
     static Map notificationURLs = new HashMap();
 
     static SSOTokenManager tokenMgr;
 
     static SSOException initializationError;
 
     static String baseDN;
 
     static boolean initialized;
 
     static String serverURL;
 
     // Cache of modified DNs for the last 30 minutes
     static int cacheSize = 30;
 
     static LinkedList cacheIndices = new LinkedList();
 
     static HashMap cache = new HashMap(cacheSize);
 
     // Default constructor
     public SMSJAXRPCObjectImpl() {
         initialize();
     }
 
     // Initialization to register the callback handler
     private void initialize() {
         if (!initialized) {
             try {
                 tokenMgr = SSOTokenManager.getInstance();
             } catch (SSOException ssoe) {
                 debug.error("SMSJAXRPCObject: "
                         + "Unable to get SSO Token Manager");
                 initializationError = ssoe;
             }
             synchronized (smsObject) {
                 if (!initialized) {
                     try {
                         SMSEntry.registerCallbackHandler(null, this);
                     } catch (Exception e) {
                         debug.warning("SMSJAXRPCObjectImpl(): unable to "
                                 + "register for callback handler", e);
                     }
                 }
             }
             // Construct server URL
             String namingURL = SystemProperties.get(
                 "com.iplanet.am.naming.url").toLowerCase();
             if (namingURL != null) {
                 int index = namingURL.indexOf("/namingservice");
                 if (index != -1) {
                     serverURL = namingURL.substring(0, index);
                 } else {
                     serverURL = "";
                 }
             } else {
                 serverURL = "";
             }
             initialized = true;
         }
     }
 
     // Method to check if service is local and also to
     // test if the server is down
     public void checkForLocal() {
         SMSJAXRPCObject.isLocal = true;
     }
 
     /**
      * Returns the attribute names and values of the provided object using the
      * identity of the provided SSO Token
      */
     public Map read(String tokenID, String objName)
         throws SMSException, SSOException, RemoteException {
         if (debug.messageEnabled()) {
             debug.message("SMSJAXRPCObjectImpl::read dn: " + objName);
         }
 
        Map ans = null;
 
         if (objName.equals("o=" + SMSJAXRPCObject.AMJAXRPCVERSIONSTR)) {
            ans = new HashMap();
            ans.put(SMSJAXRPCObject.AMJAXRPCVERSIONSTR,
                SMSJAXRPCObject.AMJAXRPCVERSION);
         } else {
             CachedSMSEntry ce = CachedSMSEntry.getInstance(getToken(tokenID),
                 objName, null);
             Map attrs = ce.getSMSEntry().getAttributes();
             if (attrs != null) {
                ans = new HashMap();
                 for (Iterator i = attrs.keySet().iterator(); i.hasNext();) {
                     String attrName = i.next().toString();
                     Object o = attrs.get(attrName);
                     ans.put(attrName, o);
                 }
             }
         }
         return (ans);
     }
 
     /**
      * Creates an entry in the persistent store. Throws an exception if the
      * entry already exists
      */
     public void create(String tokenID, String objName, Map attributes)
             throws SMSException, SSOException, RemoteException {
         if (debug.messageEnabled()) {
             debug.message("SMSJAXRPCObjectImpl::create dn: " + objName);
         }
         smsObject.create(getToken(tokenID), objName, attributes);
         // Send notifications
         SMSEntry.notifyObjectChanged(objName, SMSObjectListener.ADD);
     }
 
     /**
      * Modifies the attributes to the object.
      */
     public void modify(String tokenID, String objName, String mods)
             throws SMSException, SSOException, RemoteException {
         if (debug.messageEnabled()) {
             debug.message("SMSJAXRPCObjectImpl::modify dn: " + objName);
         }
         smsObject.modify(getToken(tokenID), objName, getModItems(mods));
         // Send notifications
         SMSEntry.notifyObjectChanged(objName, SMSObjectListener.MODIFY);
     }
 
     /**
      * Delete the entry in the datastore. This should delete sub-entries also
      */
     public void delete(String tokenID, String objName) throws SMSException,
             SSOException, RemoteException {
         if (debug.messageEnabled()) {
             debug.message("SMSJAXRPCObjectImpl::delete dn: " + objName);
         }
         smsObject.delete(getToken(tokenID), objName);
         // Send notifications
         SMSEntry.notifyObjectChanged(objName, SMSObjectListener.DELETE);
     }
 
     /**
      * Returns the suborganization names. Returns a set of SMSEntry objects that
      * are suborganization names. The paramter <code>numOfEntries</code>
      * identifies the number of entries to return, if code>0</code> returns
      * all the entries.
      */
     public Set searchSubOrgNames(String tokenID, String dn, String filter,
             int numOfEntries, boolean sortResults, boolean ascendingOrder,
             boolean recursive) throws SMSException, SSOException {
         if (debug.messageEnabled()) {
             debug.message("SMSJAXRPCObjectImpl::searchSubOrgNames dn: " + dn);
         }
         return (smsObject.searchSubOrgNames(getToken(tokenID), dn, filter,
                 numOfEntries, sortResults, ascendingOrder, recursive));
     }
 
     /**
      * Returns the organization names. Returns a set of SMSEntry objects that
      * are organization names. The paramter <code>numOfEntries</code>
      * identifies the number of entries to return, if <code>0</code> returns
      * all the entries.
      */
     public Set searchOrganizationNames(String tokenID, String dn,
             int numOfEntries, boolean sortResults, boolean ascendingOrder,
             String serviceName, String attrName, Set values)
             throws SMSException, SSOException {
         if (debug.messageEnabled()) {
             debug.message("SMSJAXRPCObjectImpl::searchOrganizationNames dn: "
                     + dn);
         }
         return (smsObject.searchOrganizationNames(getToken(tokenID), dn,
                 numOfEntries, sortResults, ascendingOrder, serviceName,
                 attrName, values));
     }
 
     /**
      * Returns the sub-entries. Returns a set of SMSEntry objects that are
      * sub-entries. The paramter <code>numOfEntries</code> identifies the
      * number of entries to return, if <code>0</code> returns all the entries.
      */
     public Set subEntries(String tokenID, String dn, String filter,
             int numOfEntries, boolean sortResults, boolean ascendingOrder)
             throws SMSException, SSOException {
         if (debug.messageEnabled()) {
             debug.message("SMSJAXRPCObjectImpl::subentries dn: " + dn);
         }
         return (smsObject.subEntries(getToken(tokenID), dn, filter,
                 numOfEntries, sortResults, ascendingOrder));
     }
 
     /**
      * Returns the sub-entries matching the schema id. Returns a set of SMSEntry
      * objects that are sub-entries for the provided schema id. The paramter
      * <code>numOfEntries</code> identifies the number of entries to return,
      * if <code>0</code> returns all the entries.
      */
     public Set schemaSubEntries(String tokenID, String dn, String filter,
             String sidFilter, int numOfEntries, boolean sortResults, boolean ao)
             throws SMSException, SSOException {
         if (debug.messageEnabled()) {
             debug.message("SMSJAXRPCObjectImpl::subentries dn: " + dn);
         }
         return (smsObject.schemaSubEntries(getToken(tokenID), dn, filter,
                 sidFilter, numOfEntries, sortResults, ao));
     }
 
     /**
      * Searchs the data store for objects that match the filter
      */
     public Set search(String tokenID, String startDN, String filter)
             throws SMSException, SSOException, RemoteException {
         if (debug.messageEnabled()) {
             debug.message("SMSJAXRPCObjectImpl::search dn: " + startDN
                     + " filter: " + filter);
         }
         return (smsObject.search(getToken(tokenID), startDN, filter));
     }
 
     /**
      * Checks if the provided DN exists. Used by PolicyManager.
      */
     public boolean entryExists(String tokenID, String objName)
             throws SSOException, RemoteException {
         if (debug.messageEnabled()) {
             debug.message("SMSJAXRPCObjectImpl::exists dn: " + objName);
         }
         return (smsObject.entryExists(getToken(tokenID), objName));
     }
 
     /**
      * Returns the root suffix (i.e., base DN) for the SMS objects. All
      * SMSEntries will end with this root suffix.
      */
     public String getRootSuffix() throws RemoteException {
         if (baseDN == null) {
             baseDN = smsObject.getRootSuffix();
         }
         return (baseDN);
     }
 
     // Implementation to receive requests from clients
     // Returns changes in the past <i>time</i> minutes
     public synchronized Set objectsChanged(int time) throws RemoteException {
         Set answer = new HashSet();
         // Get the cache index for times upto time+2
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(new Date());
         // Add 1 minute to offset, the initial lookup
         calendar.add(Calendar.MINUTE, 1);
         for (int i = 0; i < time + 3; i++) {
             calendar.add(Calendar.MINUTE, -1);
             String cacheIndex = calendarToString(calendar);
             Set modDNs = (Set) cache.get(cacheIndex);
             if (modDNs != null)
                 answer.addAll(modDNs);
         }
         return (answer);
     }
 
     // Implementation for SMSObjectListener
     public synchronized void objectChanged(String name, int type) {
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(new Date());
         String cacheIndex = calendarToString(calendar);
         Set modDNs = (Set) cache.get(cacheIndex);
         if (modDNs == null) {
             modDNs = new HashSet();
             cache.put(cacheIndex, modDNs);
             // Maintain cacheIndex
             cacheIndices.addFirst(cacheIndex);
             if (cacheIndices.size() > cacheSize) {
                 cacheIndices.removeLast();
             }
         }
         String modItem = null;
         switch (type) {
         case ADD:
             modItem = "ADD:" + name;
             break;
         case DELETE:
             modItem = "DEL:" + name;
             break;
         default:
             modItem = "MOD:" + name;
         }
         modDNs.add(modItem);
 
         // If notification URLs are present, send notifications
         for (Iterator entries = notificationURLs.entrySet().iterator(); entries
                 .hasNext();) {
             Map.Entry entry = (Map.Entry) entries.next();
             String id = (String) entry.getKey();
             URL url = (URL) entry.getValue();
 
             // Construct NotificationSet
             Notification notification = new Notification(modItem);
             NotificationSet ns = new NotificationSet(JAXRPCUtil.SMS_SERVICE);
             ns.addNotification(notification);
             try {
                 PLLServer.send(url, ns);
                 if (debug.messageEnabled()) {
                     debug.message("SMSJAXRPCObjectImpl:sentNotification "
                             + "URL: " + url + " Data: " + ns);
                 }
             } catch (SendNotificationException ne) {
                 if (debug.warningEnabled()) {
                     debug.warning("SMSJAXRPCObject: failed sending "
                             + "notification to: " + url + "\nRemoving "
                             + "URL from notification list.", ne);
                 }
                 // Remove the URL from Notification List
                 notificationURLs.remove(id);
             }
         }
     }
 
     public void allObjectsChanged() {
         // do nothing
     }
 
     // Methods to register notification URLs
     public synchronized String registerNotificationURL(String url)
             throws RemoteException {
         String id = SMSUtils.getUniqueID();
         try {
             // Check URL is not the local server
             if (!url.toLowerCase().startsWith(serverURL)) {
                 notificationURLs.put(id, new URL(url));
                 if (debug.messageEnabled()) {
                     debug.message("SMSJAXRPCObjectImpl:register for "
                             + "notification URL: " + url);
                 }
             } else {
                 // Cannot add this server for notifications
                 if (debug.warningEnabled()) {
                     debug.warning("SMSJAXRPCObjectImpl:registerURL "
                             + "cannot add local server: " + url);
                 }
                 throw (new RemoteException("invalid-notification-URL"));
             }
         } catch (MalformedURLException e) {
             if (debug.warningEnabled()) {
                 debug.warning("SMSJAXRPCObjectImpl:registerNotificationURL "
                         + " invalid URL: " + url, e);
             }
         }
         return (id);
     }
 
     public synchronized void deRegisterNotificationURL(String id)
             throws RemoteException {
         notificationURLs.remove(id);
     }
 
     public void notifyObjectChanged(String name, int type)
             throws RemoteException {
         SMSEntry.objectChanged(name, type);
     }
 
     private static String calendarToString(Calendar calendar) {
         // Get year, month, date, hour and minute
         int year = calendar.get(Calendar.YEAR);
         int month = calendar.get(Calendar.MONTH);
         int date = calendar.get(Calendar.DATE);
         int hour = calendar.get(Calendar.HOUR);
         int minute = calendar.get(Calendar.MINUTE);
         // Clear the calendar, set the params and get the string
         calendar.clear();
         calendar.set(year, month, date, hour, minute);
         return (serverURL + calendar.toString());
     }
 
     /**
      * Returns SSOToken from token ID
      */
     private static SSOToken getToken(String tokenID) throws SSOException {
         if (initializationError != null)
             throw (initializationError);
         return (tokenMgr.createSSOToken(tokenID));
     }
 
     /**
      * Returns an array of ModificationItems converted from string
      * representation of mods. The string representation is of the format:
      * <pre>
      * <Modifications size="xx"> <AttributeValuePair event="ADD | REPLACE |
      * DELETE"> <Attribute name="attrName" /> <Value>...</Value>
      * </AttributeValuePair> </Modifications>
      * </pre>
      */
     static ModificationItem[] getModItems(String mods) throws SMSException {
         if (debug.messageEnabled()) {
             debug.message("SMSJAXRPCObject::StringToMods: " + mods);
         }
         ModificationItem[] answer = null;
         try {
             if (mods != null) {
                 mods = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + mods;
                 Document doc = XMLUtils.toDOMDocument(mods, debug);
                 Node root = XMLUtils.getRootNode(doc, "Modifications");
                 int modsSize = Integer.parseInt(XMLUtils.getNodeAttributeValue(
                         root, "size"));
                 answer = new ModificationItem[modsSize];
                 NodeList nl = root.getChildNodes();
                 for (int i = 0; i < modsSize; i++) {
                     Node node = nl.item(i);
                     if (node.getNodeName().equals("AttributeValuePair")) {
                         String eventS = XMLUtils.getNodeAttributeValue(node,
                                 "event");
                         int event = DirContext.ADD_ATTRIBUTE;
                         if (eventS.equals("REPLACE"))
                             event = DirContext.REPLACE_ATTRIBUTE;
                         else if (eventS.equals("DELETE"))
                             event = DirContext.REMOVE_ATTRIBUTE;
                         Node attrNode = XMLUtils
                                 .getChildNode(node, "Attribute");
                         String attrName = XMLUtils.getNodeAttributeValue(
                                 attrNode, "name");
                         Set values = XMLUtils.getAttributeValuePair(node);
                         // Construct ModificationItem
                         BasicAttribute attr = new BasicAttribute(attrName);
                         for (Iterator it = values.iterator(); it.hasNext();)
                             attr.add(it.next());
                         answer[i] = new ModificationItem(event, attr);
                     }
                 }
             }
         } catch (Exception e) {
             throw (new SMSException(e,
                     "sms-JAXRPC-cannot-copy-fromModStringToModItem"));
         }
         return (answer);
     }
 }
