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
 * $Id: WebtopNaming.java,v 1.5 2007-10-08 19:38:04 huacui Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.iplanet.services.naming;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import com.sun.identity.shared.debug.Debug;
 import com.iplanet.am.util.SystemProperties;
 import com.iplanet.services.comm.client.PLLClient;
 import com.iplanet.services.comm.client.SendRequestException;
 import com.iplanet.services.comm.share.Request;
 import com.iplanet.services.comm.share.RequestSet;
 import com.iplanet.services.comm.share.Response;
 import com.iplanet.services.naming.service.NamingService;
 import com.iplanet.services.naming.share.NamingBundle;
 import com.iplanet.services.naming.share.NamingRequest;
 import com.iplanet.services.naming.share.NamingResponse;
 import com.sun.identity.shared.Constants;
 
 /**
  * The <code>WebtopNaming</code> class is used to get URLs for various
  * services such as session, profile, logging etc. The lookup is based on the
  * service name and the host name. The Naming Service shall contain URLs for all
  * services on all servers. For instance, two machines might host session
  * services. The Naming Service profile may look like the following:
  * 
  * <pre>
  *      host1.session.URL=&quot;http://host1:8080/SessionServlet&quot;
  *      host2.session.URL=&quot;https://host2:9090/SessionServlet&quot;
  * </pre>
  */
 
 public class WebtopNaming {
 
     public static final String NAMING_SERVICE = "com.iplanet.am.naming";
 
     public static final String NODE_SEPARATOR = "|";
 
    private static final String AM_NAMING_PREFIX = "iplanet-am-naming-";

    private static final String FAM_NAMING_PREFIX = "sun-naming-";

     private static Hashtable namingTable = null;
 
     private static Hashtable serverIdTable = null;
 
     private static Hashtable siteIdTable = null;
 
     private static Vector platformServers = new Vector();
 
     private static String namingServiceURL[] = null;
 
     private static Vector platformServerIDs = new Vector();
 
     protected static Debug debug = Debug.getInstance("amNaming");
 
     private static boolean serverMode = false;
 
     private static String amServerProtocol = null;
 
     private static String amServer = null;
 
     private static String amServerPort = null;
 
     private static SiteMonitor monitorThread = null;
 
     static {
         serverMode = Boolean.valueOf(
                 System.getProperty(Constants.SERVER_MODE, SystemProperties.get(
                         Constants.SERVER_MODE, "false"))).booleanValue();
         try {
             getAMServer();
         } catch (Exception ex) {
             debug.error("Failed to initialize server properties", ex);
         }
     }
 
     public static boolean isServerMode() {
         return serverMode;
     }
 
     public static boolean isSiteEnabled(String protocol, String host,
             String port) throws Exception {
         String serverid = getServerID(protocol, host, port);
 
         return isSiteEnabled(serverid);
     }
 
     public static boolean isSiteEnabled(String serverid) throws Exception {
         String siteid = (String) siteIdTable.get(serverid);
 
         return (!serverid.equals(siteid));
     }
 
     public static String getAMServerID() throws ServerEntryNotFoundException {
         return getServerID(amServerProtocol, amServer, amServerPort);
     }
 
     private static void getAMServer() {
         amServer = SystemProperties.get(Constants.AM_SERVER_HOST);
         amServerPort = SystemProperties.get(Constants.AM_SERVER_PORT);
         amServerProtocol = SystemProperties.get(Constants.AM_SERVER_PROTOCOL);
         return;
     }
 
     private static void initializeNamingService() {
         try {
             // Initilaize the list of naming URLs
             getNamingServiceURL();
             if ((isServerMode() == false) && (namingServiceURL.length > 1)) {
                 startSiteMonitor(namingServiceURL);
             }
         } catch (Exception ex) {
             debug.error("Failed to initialize naming service", ex);
         }
     }
 
     /**
      * This method returns the URL of the specified service on the specified
      * host.
      * 
      * @param service
      *            The name of the service.
      * @param protocol
      *            The service protocol
      * @param host
      *            The service host name
      * @param port
      *            The service listening port
      * @return The URL of the specified service on the specified host.
      */
     public static URL getServiceURL(String service, String protocol,
             String host, String port) throws URLNotFoundException {
         boolean validate = isServerMode();
 
         return (getServiceURL(service, protocol, host, port, validate));
     }
 
     /**
      * This method returns the URL of the specified service on the specified
      * host.
      * 
      * @param service
      *            The name of the service.
      * @param protocol
      *            The service protocol
      * @param host
      *            The service host name
      * @param port
      *            The service listening port
      * @param validate
      *            Validate the protocol, host and port of AM server
      * @return The URL of the specified service on the specified host.
      */
     public static URL getServiceURL(String service, String protocol,
             String host, String port, boolean validate)
             throws URLNotFoundException {
         try {
             // check before the first naming table update to avoid deadlock
             if (protocol == null || host == null || port == null
                     || protocol.length() == 0 || host.length() == 0
                     || port.length() == 0) {
                 throw new Exception(NamingBundle.getString("noServiceURL")
                         + service);
             }
 
             if (namingTable == null) {
                 getNamingProfile(false);
             }
             String url = null;
            String name = AM_NAMING_PREFIX + service.toLowerCase() + "-url";
             url = (String) namingTable.get(name);
            if (url == null) {
                name = FAM_NAMING_PREFIX + service.toLowerCase() + "-url";
                url = (String)namingTable.get(name);
            }
             if (url != null) {
                 // If replacement is required, the protocol, host, and port
                 // validation is needed against the server list
                 // (iplanet-am-platform-server-list)
                 if (validate && url.indexOf("%") != -1) {
                     validate(protocol, host, port);
                 }
                 // %protocol processing
                 int idx;
                 if ((idx = url.indexOf("%protocol")) != -1) {
                     url = url.substring(0, idx)
                             + protocol
                             + url.substring(idx + "%protocol".length(), url
                                     .length());
                 }
 
                 // %host processing
                 if ((idx = url.indexOf("%host")) != -1) {
                     url = url.substring(0, idx)
                             + host
                             + url.substring(idx + "%host".length(), url
                                     .length());
                 }
 
                 // %port processing
                 if ((idx = url.indexOf("%port")) != -1) {
                     // plugin the server name
                     url = url.substring(0, idx)
                             + port
                             + url.substring(idx + "%port".length(), url
                                     .length());
                 }
                 return new URL(url);
             } else {
                 throw new Exception(NamingBundle.getString("noServiceURL")
                         + service);
             }
         } catch (Exception e) {
             throw new URLNotFoundException(e.getMessage());
         }
     }
 
     /**
      * This method returns all the URLs of the specified service based on the
      * servers in platform server list.
      * 
      * @param service
      *            The name of the service.
      * @return The URL of the specified service on the specified host.
      */
     public static Vector getServiceAllURLs(String service)
             throws URLNotFoundException {
         Vector allurls = null;
 
         try {
             if (namingTable == null) {
                 getNamingProfile(false);
             }
 
            String name = AM_NAMING_PREFIX + service.toLowerCase() + "-url";            
            String url = (String)namingTable.get(name);
            if (url == null) {
                name = FAM_NAMING_PREFIX + service.toLowerCase() + "-url";
                url = (String)namingTable.get(name);
            }
             if (url != null) {
                 allurls = new Vector();
                 if (monitorThread == null) {
                     allurls.add(getServiceURL(service, amServerProtocol,
                                       amServer, amServerPort));
                 } else {
                     if (url.indexOf("%") != -1) {
                         Vector servers =  SiteMonitor.getAvailableSites();
                         Iterator it = servers.iterator();
                         while (it.hasNext()) {
                             String server = getServerFromID((String)it.next());
                             URL serverURL = new URL(server);
                             allurls.add(getServiceURL(service,
                                       serverURL.getProtocol(),
                                       serverURL.getHost(),
                                       String.valueOf(serverURL.getPort())));
                         }
                     } else {
                         allurls.add(new URL(url));
                     }
                 }
             }
 
             return allurls;
         } catch (Exception e) {
             throw new URLNotFoundException(e.getMessage());
         }
     }
 
     /**
      * This method returns all the platform servers note: This method should be
      * used only for the remote sdk, calling this method may cause performance
      * abuse, as it involves xml request over the wire.An alternative to this
      * method is use SMS getNamingProfile(true) is called over here if there's
      * any change in server list dynamically at the server side.
      */
     public static Vector getPlatformServerList() throws Exception {
          return getPlatformServerList(true);
     }
 
      public static Vector getPlatformServerList(boolean update)
              throws Exception {
          getNamingProfile(update);
          return platformServers;
      }
 
     /**
      * This method returns key value from a hashtable, ignoring the case of the
      * key.
      */
     private static String getValueFromTable(Hashtable table, String key) {
         if (table.contains(key)) {
             return (String) table.get(key);
         }
         for (Enumeration keys = table.keys(); keys.hasMoreElements();) {
             String tmpKey = (String) keys.nextElement();
             if (tmpKey.equalsIgnoreCase(key)) {
                 return (String) table.get(tmpKey);
             }
         }
         return null;
     }
 
     /**
      * This function gets the server id that is there in the platform server
      * list for a corresponding server. One use of this function is to keep this
      * server id in our session id.
      */
     public static String getServerID(String protocol,
                                      String host,
                                      String port)
         throws ServerEntryNotFoundException {
         return getServerID(protocol, host, port, true);
     }
 
     public static String getServerID(String protocol, 
                                      String host, 
                                      String port,
                                      boolean updatetbl)
         throws ServerEntryNotFoundException {
         try {
             // check before the first naming table update to avoid deadlock
             if (protocol == null || host == null || port == null ||
                 protocol.length() == 0 || host.length() == 0 ||
                 port.length() == 0) {
                 throw new Exception(NamingBundle.getString("noServerID"));
             }
 
             String server = protocol + ":" + "//" + host + ":" + port;
             String serverID = null;
             if (serverIdTable != null) {
                 serverID = getValueFromTable(serverIdTable, server);
             }
             //update the naming table and as well as server id table
             //if it can not find it
             if (( serverID == null ) && (updatetbl == true)) {
                 getNamingProfile(true);
                 serverID = getValueFromTable(serverIdTable, server);
             }
 
             if (serverID == null) {
                 throw new ServerEntryNotFoundException(
                     NamingBundle.getString("noServerID"));
             }
             return serverID;
         } catch (Exception e) {
             throw new ServerEntryNotFoundException(e);
         }
     }
 
     /**
      * This function returns server from the id.
      */
 
     public static String getServerFromID(String serverID)
             throws ServerEntryNotFoundException {
         String server = null;
         try {
             // refresh local naming table in case the key is not found
             if (namingTable != null) {
                 server = getValueFromTable(namingTable, serverID);
             }
             if (server == null) {
                 getNamingProfile(true);
                 server = getValueFromTable(namingTable, serverID);
             }
             if (server == null) {
                 throw new ServerEntryNotFoundException(NamingBundle
                         .getString("noServer"));
             }
 
         } catch (Exception e) {
             throw new ServerEntryNotFoundException(e);
         }
         return server;
     }
 
     public static Vector getAllServerIDs() throws Exception  {
         if (namingTable == null) {
             getNamingProfile(false);
         }
 
         return platformServerIDs;
     }
 
     /**
      * This function gets the server id that is there in the platform server
      * list for a corresponding server. One use of this function is to keep this
      * server id in our session id.
      */
     public static String getSiteID(String protocol, String host, String port)
             throws ServerEntryNotFoundException {
         String serverid = getServerID(protocol, host, port);
 
         return getSiteID(serverid);
     }
 
     /**
      * Extended ServerID syntax : localserverID | lbID-1 | lbID-2 | lbID-3 | ...
      * It returns lbID-1
      */
     public static String getSiteID(String serverid) {
         String primary_site = null;
         String sitelist = null;
 
         if (siteIdTable == null) {
             return null;
         }
 
         sitelist = (String) siteIdTable.get(serverid);
         StringTokenizer tok = new StringTokenizer(sitelist, NODE_SEPARATOR);
         if (tok != null) {
             primary_site = tok.nextToken();
         }
 
         if (debug.messageEnabled()) {
             debug.message("WebtopNaming : SiteID for " + serverid + " is "
                     + primary_site);
         }
 
         return primary_site;
     }
 
     /**
      * Extended ServerID syntax : localserverID | lbID-1 | lbID-2 | lbID-3 | ...
      * It returns lbID-2 | lbID-3 | ...
      */
     public static String getSecondarySites(String serverid) {
         String sitelist = null;
         String secondarysites = null;
 
         if (siteIdTable == null) {
             return null;
         }
 
         sitelist = (String) siteIdTable.get(serverid);
         if (sitelist == null) {
             return null;
         }
 
         int index = sitelist.indexOf(NODE_SEPARATOR);
         if (index != -1) {
             secondarysites = sitelist.substring(index + 1, sitelist.length());
         }
 
         if (debug.messageEnabled()) {
             debug.message("WebtopNaming : SecondarySites for " + serverid
                     + " is " + secondarysites);
         }
 
         return secondarysites;
     }
 
     /**
      * This method returns all the node id for the site
      * 
      * @param one
      *            of node id, it can be serverid or lb's serverid
      * @return HashSet has all the node is for the site.
      */
     public static Set getSiteNodes(String serverid) throws Exception {
         HashSet nodeset = new HashSet();
 
         if (namingTable == null) {
             getNamingProfile(false);
         }
 
         String siteid = getSiteID(serverid);
 
         Enumeration e = siteIdTable.keys();
         while (e.hasMoreElements()) {
             String node = (String) e.nextElement();
             if (siteid.equalsIgnoreCase(node)) {
                 continue;
             }
 
             if (siteid.equalsIgnoreCase(getSiteID(node))) {
                 nodeset.add(node);
             }
         }
 
         return nodeset;
     }
 
     /**
      * This method returns the class of the specified service.
      * 
      * @param service
      *            The name of the service.
      * @return The class name of the specified service.
      */
     public static String getServiceClass(String service)
             throws ClassNotFoundException {
         try {
             if (namingTable == null) {
                 getNamingProfile(false);
             }
             String cls = null;
            String name = AM_NAMING_PREFIX + service.toLowerCase()
                     + "-class";
             cls = (String) namingTable.get(name);
             if (cls == null) {
                name = FAM_NAMING_PREFIX + service.toLowerCase() + "-class";
                cls = (String)namingTable.get(name);
            }
            if (cls == null) {
                 throw new Exception(NamingBundle.getString("noServiceClass")
                         + service);
             }
             return cls;
         } catch (Exception e) {
             throw new ClassNotFoundException(e.getMessage());
         }
     }
 
     /**
      * This method returns the URL of the notification service on the local
      * host.
      */
     public synchronized static URL getNotificationURL()
             throws URLNotFoundException {
         try {
             String url = System.getProperty(Constants.AM_NOTIFICATION_URL,
                     SystemProperties.get(Constants.AM_NOTIFICATION_URL));
             if (url == null) {
                 throw new URLNotFoundException(NamingBundle
                         .getString("noNotificationURL"));
             }
             return new URL(url);
         } catch (Exception e) {
             throw new URLNotFoundException(e.getMessage());
         }
     }
 
     private synchronized static void getNamingProfile(boolean update)
             throws Exception {
         if (update || namingTable == null) {
             updateNamingTable();
         }
     }
 
     private static void updateServerProperties(URL url) {
         amServerProtocol = url.getProtocol();
         amServer = url.getHost();
         amServerPort = Integer.toString(url.getPort());
 
         SystemProperties.initializeProperties(Constants.AM_SERVER_PROTOCOL,
                 amServerProtocol);
         SystemProperties.initializeProperties(Constants.AM_SERVER_HOST,
                 amServer);
         SystemProperties.initializeProperties(Constants.AM_SERVER_PORT,
                 amServerPort);
         if (debug.messageEnabled()) {
             debug.message("Server Properties are changed : ");
             debug.message(Constants.AM_SERVER_PROTOCOL + " : "
                     + SystemProperties.get(Constants.AM_SERVER_PROTOCOL, null));            
             debug.message(Constants.AM_SERVER_HOST + " : "
                     + SystemProperties.get(Constants.AM_SERVER_HOST, null));
             debug.message(Constants.AM_SERVER_PORT + " : "
                     + SystemProperties.get(Constants.AM_SERVER_PORT, null));
         }
     }
 
     private static Hashtable getNamingTable(URL nameurl) throws Exception {
         Hashtable nametbl = null;
         NamingRequest nrequest = new NamingRequest(NamingRequest.reqVersion);
         Request request = new Request(nrequest.toXMLString());
         RequestSet set = new RequestSet(NAMING_SERVICE);
         set.addRequest(request);
         Vector responses = null;
 
         try {
             responses = PLLClient.send(nameurl, set);
             if (responses.size() != 1) {
                 throw new Exception(NamingBundle
                         .getString("unexpectedResponse"));
             }
 
             Response res = (Response) responses.elementAt(0);
             NamingResponse nres = NamingResponse.parseXML(res.getContent());
             if (nres.getException() != null) {
                 throw new Exception(nres.getException());
             }
             nametbl = nres.getNamingTable();
         } catch (SendRequestException sre) {
             debug.error("Naming service connection failed for " + nameurl, sre);
         } catch (Exception e) {
             debug.error("getNamingTable: ", e);
         }
 
         return nametbl;
     }
 
     private static void updateNamingTable() throws Exception {
 
         if (!isServerMode()) {
             if (namingServiceURL == null) {
                 initializeNamingService();
             }
 
             // Try for the primary server first, if it fails and then
             // for the second server. We get connection refused error
             // if it doesn't succeed.
             namingTable = null;
             URL tempNamingURL = null;
             for (int i = 0; ((namingTable == null) && 
                     (i < namingServiceURL.length)); i++) {
                 tempNamingURL = new URL(namingServiceURL[i]);
                 namingTable = getNamingTable(tempNamingURL);
             }
 
             if (namingTable == null) {
                 debug.error("updateNamingTable : "
                         + NamingBundle.getString("noNamingServiceAvailable"));
                 throw new Exception(NamingBundle
                         .getString("noNamingServiceAvailable"));
             }
 
             updateServerProperties(tempNamingURL);
         } else {
             namingTable = NamingService.getNamingTable();
         }
 
         String servers = (String) namingTable.get(Constants.PLATFORM_LIST);
 
         if (servers != null) {
             StringTokenizer st = new StringTokenizer(servers, ",");
             platformServers.clear();
             while (st.hasMoreTokens()) {
                 platformServers.add((st.nextToken()).toLowerCase());
             }
         }
         updateServerIdMappings();
         updateSiteIdMappings();
         updatePlatformServerIDs();
 
         if (debug.messageEnabled()) {
             debug.message("Naming table -> " + namingTable.toString());
             debug.message("Platform Servers -> " + platformServers.toString());
             debug.message("Platform Server IDs -> "
                           + platformServerIDs.toString());
         }
     }
 
     /*
      * this method is to update the servers and their ids in a seprate hash and
      * will get updated each time when the naming table gets updated note: this
      * table will have all the entries in naming table but in a reverse order
      * except the platform server list We can just as well keep only server id
      * mappings, but we need to exclude each other entry which is there in.
      */
     private static void updateServerIdMappings() {
         serverIdTable = new Hashtable();
         Enumeration e = namingTable.keys();
         while (e.hasMoreElements()) {
             String key = (String) e.nextElement();
             String value = (String) namingTable.get(key);
             if ((key == null) || (value == null)) {
                 continue;
             }
             // If the key is server list skip it, since it would
             // have the same value
             if (key.equals(Constants.PLATFORM_LIST)) {
                 continue;
             }
             serverIdTable.put(value, key);
         }
     }
 
     private static void updateSiteIdMappings() {
         siteIdTable = new Hashtable();
         String serverSet = (String) namingTable.get(Constants.SITE_ID_LIST);
 
         if ((serverSet == null) || (serverSet.length() == 0)) {
             return;
         }
 
         StringTokenizer tok = new StringTokenizer(serverSet, ",");
         while (tok.hasMoreTokens()) {
             String serverid = tok.nextToken();
             String siteid = serverid;
             int idx = serverid.indexOf(NODE_SEPARATOR);
             if (idx != -1) {
                 siteid = serverid.substring(idx + 1, serverid.length());
                 serverid = serverid.substring(0, idx);
             }
             siteIdTable.put(serverid, siteid);
         }
 
         if (debug.messageEnabled()) {
             debug.message("SiteID table -> " + siteIdTable.toString());
         }
 
         return;
     }
 
     private static void updatePlatformServerIDs()
         throws MalformedURLException, ServerEntryNotFoundException {
         Iterator it = platformServers.iterator();
         while (it.hasNext()) {
             String plaformURL = (String) it.next();
             URL url = new URL(plaformURL);
             String serverID = getServerID(url.getProtocol(), url.getHost(),
                 Integer.toString(url.getPort()));
             if (platformServerIDs.contains(serverID) == false) {
                 platformServerIDs.add(serverID);
             }
         }
     }
 
     private static void validate(String protocol, String host, String port)
             throws URLNotFoundException {
         String server = (protocol + "://" + host + ":" + port).toLowerCase();
         try {
             // first check if this is the local server, proto, and port,
             // if it is there is no need to
             // validate that it is in the trusted server platform server list
             if (protocol.equalsIgnoreCase(amServerProtocol)
                     && host.equalsIgnoreCase(amServer)
                     && port.equals(amServerPort)) {
                 return;
             }
             if (debug.messageEnabled()) {
                 debug.message("platformServers: " + platformServers);
             }
             if (!platformServers.contains(server)) {
                 getNamingProfile(true);
                 if (!platformServers.contains(server)) {
                     throw new URLNotFoundException(NamingBundle
                             .getString("invalidServiceHost")
                             + " " + server);
                 }
             }
         } catch (Exception e) {
             debug.error("platformServers: " + platformServers, e);
             throw new URLNotFoundException(e.getMessage());
         }
     }
 
     /**
      * This method returns the list URL of the naming service url
      * 
      * @return Array of naming service url.
      * @throws Exception -
      *             if there is no configured urls or any problem in getting urls
      */
     public synchronized static String[] getNamingServiceURL() throws Exception {
         if (namingServiceURL == null) {
             // Initilaize the list of naming URLs
             ArrayList urlList = new ArrayList();
 
             // Get the naming service URLs from properties files
             String configURLListString =
                                SystemProperties.get(Constants.AM_NAMING_URL);
             if (configURLListString != null) {
                 StringTokenizer stok = new StringTokenizer(configURLListString);                
                 while (stok.hasMoreTokens()) {
                     String nextURL = stok.nextToken();
                     if (urlList.contains(nextURL)) {
                         if (debug.warningEnabled()) {
                             debug.warning(
                                 "Duplicate naming service URL specified "
                                 + nextURL + ", will be ignored.");
                         }
                     } else {
                         urlList.add(nextURL);
                     }
                 }
             }
 
             if (urlList.size() == 0) {
                 throw new Exception(
                     NamingBundle.getString("noNamingServiceURL"));
             } else {
                 if (debug.messageEnabled()) {
                     debug.message("Naming service URL list: " + urlList);
                 }
             }
 
             namingServiceURL = new String[urlList.size()];
             System.arraycopy(urlList.toArray(), 0, namingServiceURL, 0,
                 urlList.size());
         }
 
         return namingServiceURL;
     }
 
     private static synchronized void startSiteMonitor(String[] urlList) {
         // Site monitor is already started.
         if (monitorThread != null) {
             return;
         }
 
         // Start naming service monitor if more than 1 naming URLs are found
         if (urlList.length > 1) {
             monitorThread = new SiteMonitor(urlList);
             monitorThread.setDaemon(true);
             monitorThread.setPriority(Thread.MIN_PRIORITY);
             monitorThread.start();
         } else {
             if (debug.messageEnabled()) {
                 debug.message("Only one naming service URL specified."
                     + " NamingServiceMonitor will be disabled.");
             }
         }
     }
 
     static public void removeFailedSite(String server) {
         if (monitorThread != null) {
             try {
                 URL url = new URL(server);
                 removeFailedSite(url);
             } catch (MalformedURLException e) {
                 debug.error("Server URL is not valid : ", e);
             }
         }
 
         return;
     }
 
     static public void removeFailedSite(URL url) {
         if (monitorThread != null) {
             try {
                 String serverid = getServerID(url.getProtocol(),
                                      url.getHost(),
                                      String.valueOf(url.getPort()));
                 SiteMonitor.removeFailedSite(serverid);
             } catch (ServerEntryNotFoundException e) {
                 debug.error("Can not find server ID : ", e);
             }
         }
 
         return;
     }
 
 static public class SiteMonitor extends Thread {
     static long sleepInterval;
     static Vector availableSiteList = new Vector();
     static String currentSiteID = null;
     static SiteStatusCheck siteChecker = null;
     static String[] siteUrlList = null;
     static public boolean keepMonitoring = false;
 
     static {
         try {
             String checkClass =
                 SystemProperties.get(Constants.SITE_STATUS_CHECK_CLASS,
                     "com.iplanet.services.naming.SiteStatusCheckThreadImpl");
             if (debug.messageEnabled()) {
                 debug.message("SiteMonitor : SiteStatusCheck class = "
                         + checkClass);
             }
             siteChecker =
                 (SiteStatusCheck) Class.forName(checkClass).newInstance();
             sleepInterval = Long.valueOf(SystemProperties.
                     get(Constants.MONITORING_INTERVAL, "60000")).longValue();
             getNamingProfile(false);
             currentSiteID =
                 getServerID(amServerProtocol, amServer, amServerPort);
         } catch (Exception e) {
             debug.message("SiteMonitor initialization failed : ", e);
         }
     }
 
     public SiteMonitor(String[] urlList) {
         siteUrlList = urlList;
     }
 
     public void run() {
         if (debug.messageEnabled()) {
             debug.message("SiteMonitor started");
         }
         keepMonitoring = true;
         while(keepMonitoring) {
             try {
                 runCheckValidSite();
                 Sleep();
             } catch (Exception e) {
                 debug.error("SiteMonitor run failed : ", e);
             }
         }
     }
 
     static void runCheckValidSite() {
         Vector siteList = checkAvailableSiteList();
         updateSiteList(siteList);
         updateCurrentSite(siteList);
     }
 
     public static boolean checkSiteStatus(URL siteurl) {
         return siteChecker.doCheckSiteStatus(siteurl);
     }
 
     private static Vector checkAvailableSiteList() {
         Vector siteList = new Vector();
         for (int i = 0; i < siteUrlList.length; i++) {
             try {
                 URL siteurl = new URL(siteUrlList[i]);
                 if (siteChecker.doCheckSiteStatus(siteurl) == false) {
                     continue;
                 }
 
                 String serverid = getServerID(siteurl.getProtocol(),
                                        siteurl.getHost(),
                                String.valueOf(siteurl.getPort()));
                 siteList.add(serverid);
             } catch (MalformedURLException ex) {
                 if (debug.messageEnabled()) {
                     debug.message("SiteMonitor: Site URL "
                          + siteUrlList[i] + " is not valid.", ex);
                 }
             } catch (ServerEntryNotFoundException ex) {
                 if (debug.messageEnabled()) {
                     debug.message("SiteMonitor: Site URL "
                          + siteUrlList[i] + " is not available.", ex);
                 }
             }
         }
 
         return siteList;
     }
 
     static void Sleep() {
         try {
             Thread.sleep(sleepInterval);
         } catch (InterruptedException ex) {
             debug.error("SiteMonitor: monitor interrupted", ex);
         }
         return;
     }
 
     public static boolean isAvailable(URL url) throws Exception {
         if ((namingTable == null) || (keepMonitoring == false)) {
             return true;
         }
 
         String serverID = null;
         try {
             serverID = getServerID(url.getProtocol(), url.getHost(),
                 Integer.toString(url.getPort()), false);
         } catch (ServerEntryNotFoundException e) {
             if (debug.messageEnabled()) {
                 debug.message("URL is not part of AM setup.");
             }
             return true;
         }
 
         Vector sites = getAvailableSites();
         boolean available = false;
         Iterator it = sites.iterator();
         while (it.hasNext()) {
             String server = (String)it.next();
             if (serverID.equalsIgnoreCase(server)) {
                 available = true;
                 break;
             }
         }
 
         if (debug.messageEnabled()) {
             debug.message("In SiteMonitor.isAvailable()");
             if (available) {
                 debug.message("SiteID " + url.toString() + " is UP.");
             } else {
                 debug.message("SiteID " + url.toString() + " is DOWN.");
             }
         }
 
         return available;
     }
     
     public static boolean isCurrentSite(URL url) throws Exception {
         if ((namingTable == null) || !keepMonitoring) {
             return true;
         }
 
         String serverID = null;
         try {
             serverID = getServerID(url.getProtocol(), url.getHost(),
                 Integer.toString(url.getPort()), false);
         } catch (ServerEntryNotFoundException e) {
             if (debug.messageEnabled()) {
                 debug.message("URL is not part of AM setup.");
             }
             return true;
         }
 
         Vector sites = getAvailableSites();
         boolean isCurrent = false;
         if (!sites.isEmpty()) {
             String serverid = (String)sites.firstElement();
             if (serverid != null) {
                 isCurrent = serverid.equalsIgnoreCase(serverID);
             }
         }
 
         return isCurrent;
     }
 
     static Vector getAvailableSites() throws Exception {
         Vector sites = null;
         if (availableSiteList.size() == 0) {
             String[] namingURLs = getNamingServiceURL();
             for (int i = 0; i < namingURLs.length; i++) {
                 URL url = new URL(namingURLs[i]);
                 availableSiteList.add(getServerID(url.getProtocol(),
                                                   url.getHost(),
                                   String.valueOf(url.getPort())));
             }
 
             updateCurrentSite(availableSiteList);
         }
 
         sites = new Vector(availableSiteList);
         if (debug.messageEnabled()) {
             debug.message("In SiteMonitor.getAvailableSites()");
             debug.message("availableSiteList : " + sites.toString());
         }
 
         return sites;
     }
 
     static void removeFailedSite(String site) {
         if ((keepMonitoring == true) && (availableSiteList.contains(site))) {
             availableSiteList.remove(site);
         }
 
         return;
     }
 
     private static void updateSiteList(Vector list) {
         availableSiteList = list;
 
         if (debug.messageEnabled()) {
             debug.message("In SiteMonitor.updateSiteList()");
             debug.message("availableSiteList : "
                     + availableSiteList.toString());
         }
         return;
     }
 
     private static void updateCurrentSite(Vector list) {
         if (isServerMode() == true) {
             return;
         }
 
         if ((list == null) || (list.size() == 0)) {
             return;
         }
 
         String sid = (String)list.firstElement();
         if (!currentSiteID.equalsIgnoreCase(sid)) {
             if (debug.messageEnabled()) {
                 debug.message("Invoke updateServerProperties() : " +
                         "Server properties are changed for service failover");
             }
 
             try {
                 currentSiteID = sid;
                 String serverurl = getServerFromID(currentSiteID);
                 updateServerProperties(new URL(serverurl));
             } catch (Exception e) {
                 debug.error("SiteMonitor: ", e);
             }
         }
 
         return;
     }
 }
 
 /**
  * The interface <code>SiteStatusCheck</code> provides
  * method that will be used by SiteMonitor to check each site is alive.
  * Each implementation class has to implement doCheckSiteStatus method.
  */
 public interface SiteStatusCheck {
     public boolean doCheckSiteStatus(URL siteurl);
 }
 }
