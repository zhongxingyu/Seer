 package gov.usgs.cida.gdp.dataaccess;
 
 import gov.usgs.cida.gdp.constants.AppConstant;
 import gov.usgs.cida.gdp.utilities.XMLUtils;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.InvalidParameterException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathExpressionException;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author razoerb
  */
 public class ManageGeoserverWorkspace{
     private static org.slf4j.Logger log = LoggerFactory.getLogger(ManageGeoserverWorkspace.class);
 
     private String geoServerURLString = "";
 
     public ManageGeoserverWorkspace() {
         this.geoServerURLString = AppConstant.WFS_ENDPOINT.getValue();
     }
 
     public ManageGeoserverWorkspace(String geoServerURL) throws MalformedURLException {
         this.geoServerURLString = geoServerURL;
     }
 
     public ManageGeoserverWorkspace(URL geoServerURL) {
         this.geoServerURLString = geoServerURL.toExternalForm();
     }
 
     public boolean createDataStore(String shapefilePath, String layerName, String workspace, String nativeCRS, String declaredCRS) throws IOException {
         return createDataStore(shapefilePath, layerName, workspace, this.geoServerURLString, nativeCRS, declaredCRS);
     }
 
     public String updateGeoServer() throws IOException{
         return updateGeoServer(getGeoServerURLString());
     }
 
     public String updateGeoServer(String geoServerURL) throws IOException{
         return getResponse(new URL(geoServerURL + "/rest/reload/"), "POST", "text/xml", null, null, null);
     }
 
     public boolean createDataStore(String shapefilePath, String layerName, String workspace, String geoServerURL, String nativeCRS, String declaredCRS) throws IOException {
         log.debug(new StringBuilder("Attempting to create datastore on WFS server located at: ").append(geoServerURL).toString());
         // create the workspace if it doesn't already exist
         URL workspacesURL = new URL(geoServerURL + "/rest/workspaces/");
         if (!workspaceExists(workspace)) {
             log.debug(new StringBuilder("Workspace does not currently exist. Creating...").toString());
             String workspaceXML = createWorkspaceXML(workspace);
             sendPacket(workspacesURL, "POST", "text/xml", workspaceXML);
             log.trace(new StringBuilder("...done.").toString());
         }
 
         URL dataStoresURL = new URL(workspacesURL + workspace + "/datastores/");
         String namespace = "";
         Matcher nsMatcher = Pattern.compile(".*<uri>(.*)</uri>.*").matcher(getNameSpaceForWorkSpace(workspace));
         if (nsMatcher.matches()) namespace = nsMatcher.group(1);
         String dataStoreXML = createDataStoreXML(layerName, workspace, namespace, shapefilePath);
         if (!dataStoreExists(workspace, layerName)) {
             log.debug(new StringBuilder("Data store does not currently exist. Creating...").toString());
             // send POST to create the datastore if it doesn't exist
             sendPacket(dataStoresURL, "POST", "text/xml", dataStoreXML);
             log.trace(new StringBuilder("...done.").toString());
         } else {
             // otherwise send PUT to ensure that it's pointing to the correct shapefile
             sendPacket(new URL(dataStoresURL + layerName + ".xml"), "PUT", "text/xml", dataStoreXML);
         }
 
         if (!layerExists(workspace, layerName, layerName)) {
             log.debug(new StringBuilder("Layer does not currently exist. Creating...").toString());
             // create featuretype based on the datastore
             String featureTypeXML = createFeatureTypeXML(layerName, workspace, nativeCRS, declaredCRS);
             URL featureTypesURL = new URL(dataStoresURL + layerName + "/featuretypes.xml");
             sendPacket(featureTypesURL, "POST", "text/xml", featureTypeXML);
 
             // This directive generates an error when included with initial POST, we have to include
             // in separate update via PUT.
             URL featureTypeUpdateURL = new URL(dataStoresURL + layerName + "/featuretypes/" + layerName + ".xml");
             sendPacket(featureTypeUpdateURL, "PUT", "text/xml",
                     "<featureType><projectionPolicy>REPROJECT_TO_DECLARED</projectionPolicy></featureType>");
             log.trace(new StringBuilder("...done.").toString());
         }
 
         // Make sure we render using the default polygon style, and not whatever
         // colored style might have been used before
         sendPacket(new URL(geoServerURL + "/rest/layers/" + workspace + ":" + layerName), "PUT", "text/xml",
                 "<layer><defaultStyle><name>polygon</name></defaultStyle>"
                 + "<enabled>true</enabled></layer>");
         log.debug(new StringBuilder("Datastore successfully created on WFS server located at: ").append(geoServerURL).toString());
         return true;
     }
 
     public String getNameSpaceForWorkSpace(String workspace) throws MalformedURLException, IOException {
         return getResponse(new URL(this.getGeoServerURLString() + "/rest/namespaces/" + workspace + ".xml"), "GET", "text/xml", null, null, null);
     }
 
     /**
      * @See http://internal.cida.usgs.gov/jira/browse/GDP-174?focusedCommentId=18712&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#action_18712
      * @param workspaces
     * @param username 
     * @param password 
      * @param maximumFileAge
      * @throws IOException
      * @throws SAXException
      * @throws MalformedURLException
      * @throws ParserConfigurationException
      * @throws XPathExpressionException
      */
     public void scanGeoserverWorkspacesForOutdatedDatastores(final long maximumFileAge, final String username, final String password, final String... workspaces) throws IOException, SAXException, MalformedURLException, ParserConfigurationException, XPathExpressionException {
         log.info("Beginning wipe old files task for existing workspaces.");
         long now = new Date().getTime();
         for (String workspace : workspaces) {
             // Check that the workspace exists
             if (!this.workspaceExists(workspace)) log.info("Workspace '" + workspace + "' does not exist on Geoserver. Skipping.");
             else {
                 log.info("Checking workspace: " + workspace);
                 List<String> dataStoreNames = this.createDatastoreListFromWorkspace(workspace);
                 // Check the data stores in this workspace
                 for (String datastore : dataStoreNames) {
                     // Let's get the disk location for this store -- We must remove the "file:" part of the return string
                     String diskLocation = this.retrieveDiskLocationOfDatastore(workspace, datastore).split("file:")[1];
                     File diskLocationFileObject = new File(diskLocation);
 
                     // If either the file is on Geoserver and is old or
                     // the file doesn't exist on Geoserver, we will delete the
                     // the datastore from geoserver
                     boolean deleteFromGeoserver = false;
                     if (diskLocationFileObject.exists()) {
                         if (diskLocationFileObject.lastModified() < now - maximumFileAge) {
                             log.info("File " + diskLocationFileObject.getPath() + " older than cutoff. Will attempt to remove from Geoserver.");
                             deleteFromGeoserver = true;
                         }
                     } else {
                         log.info("File " + diskLocationFileObject.getPath() + " not found on disk. Will attempt to remove from Geoserver.");
                         deleteFromGeoserver = true;
                     }
 
                     if (deleteFromGeoserver) {
                         if(this.deleteDatastoreFromGeoserver(workspace, datastore, username, password, true)) {
                             log.info("Successfully deleted datastore '"+datastore+"' from geoserver or file from disk.");
                         }
                         else {
                             log.warn("An error occured while attempting to delete datastore '"+datastore+"'from geoserver or file from disk.");
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Deletes a specified datastore under the specified workspace, optionally deleting the directory the
      * shapefiles are located in on disk.
      *
      * @param workspace
      * @param datastore
      * @param username Geoserver username
      * @param password Geoserver password
      * @param wipeAssociatedFiles
      * @return
      * @throws MalformedURLException
      * @throws IOException
      * @throws XPathExpressionException
      */
     public boolean deleteDatastoreFromGeoserver(final String workspace, final String datastore, final String username, final String password, final boolean wipeAssociatedFiles) throws MalformedURLException, IOException, XPathExpressionException {
         if (StringUtils.isEmpty(workspace) || StringUtils.isEmpty(username) || StringUtils.isEmpty(password) || StringUtils.isEmpty(datastore)) throw new InvalidParameterException();
         String diskLocation = this.retrieveDiskLocationOfDatastore(workspace, datastore).split("file:")[1];
 
         // Now we try to remove the datastore
         if (!this.deleteDatastore(workspace, datastore, username, password, true)) {
             log.warn("Aborting attempt to delete datastore " + datastore + " under workspace " + workspace);
             return false;
         }
 
         // Did the user want to wipe the files?
         if (wipeAssociatedFiles) {
             // Get the location of the file from GeoServer
             File diskLocationFileObject = new File(diskLocation);
             if (diskLocationFileObject.isDirectory() &&  diskLocationFileObject.listFiles().length == 0) {
                 // The location is a directory. Delete it
                 try { FileUtils.deleteDirectory(diskLocationFileObject); }
                 catch (IOException e) { log.warn("An error occurred while trying to delete directory" + diskLocationFileObject.getPath() + ". This may need to be deleted manually. \nError: "+e.getMessage()+"\nContinuing."); }
             } else {
                 if(!FileUtils.deleteQuietly(new File(diskLocationFileObject.getParent()))) {
                     log.warn("Could not fully remove the directory: " + diskLocationFileObject.getParent() + "\nPossibly files left over.");
                 }
             }
         }
 
         return true;
     }
 
     /**
      * Check to make sure that a workspace exists in GeoServer
      *
      * @see http://docs.geoserver.org/latest/en/user/restconfig/rest-config-api.html#workspaces
      * @param workspace
      * @return
      * @throws MalformedURLException
      * @throws IOException
      */
     boolean workspaceExists(final String workspace) throws MalformedURLException, IOException {
         int responseCode = 0;
 
         // First check to see that a layer exists on the server
         responseCode = getResponseCode(new URL(this.getGeoServerURLString() + "/rest/workspaces/" + workspace), "GET", "text/html", null, null, null);
 
         switch(responseCode) {
             case 200: return true;
             case 404: return false;
             default: return false;
         }
     }
 
 
     /**
      * Checks to see if a datastore exists on Geoserver instance.
      *
      * @see http://docs.geoserver.org/latest/en/user/restconfig/rest-config-api.html#data-stores
      * @param workspace
      * @param dataStore
      * @return
      * @throws IOException
      */
     boolean dataStoreExists(String workspace, String dataStore) throws IOException {
         int responseCode = 0;
 
         // First check to see that a layer exists on the server
         responseCode = getResponseCode(new URL(this.getGeoServerURLString() + "/rest/workspaces/" + workspace + "/datastores/" + dataStore), "GET", "text/xml", null, null, null);
 
         switch(responseCode) {
             case 200: return true;
             case 404: return false;
             default: return false;
         }
     }
 
     /**
      * Checks to see if a layer exists on Geoserver instance.
      *
      * @see http://docs.geoserver.org/latest/en/user/restconfig/rest-config-api.html#layers
      * @param workspace
      * @param dataStore
      * @return
      * @throws IOException
      */
     boolean layerExists(String workspace, String dataStore, String layerName) throws IOException {
         int responseCode = 0;
 
         // First check to see that a layer exists on the server
         responseCode = getResponseCode(new URL(this.getGeoServerURLString() + "/rest/workspaces/"
                     + workspace + "/datastores/" + dataStore + "/featuretypes/" + layerName + ".xml"),
                     "GET", "text/xml", null, null, null);
 
         switch(responseCode) {
             case 200: return true;
             case 404: return false;
             default: return false;
         }
     }
 
     /**
      * Checks to see if a style exists on Geoserver instance.
      *
      * @see http://docs.geoserver.org/latest/en/user/restconfig/rest-config-api.html#styles
      * @param workspace
      * @param dataStore
      * @return
      * @throws IOException
      */
     boolean styleExists(String styleName) throws IOException {
         int responseCode = 0;
 
         // First check to see that a layer exists on the server
         responseCode = getResponseCode(new URL(this.getGeoServerURLString() + "/rest/styles/" + styleName), "GET", "text/html", null, null, null);
 
         switch(responseCode) {
             case 200: return true;
             case 404: return false;
             default: return false;
         }
     }
 
     /**
      * Attempts to remove a layer from the Geoserver server.
      *
      * @see http://docs.geoserver.org/latest/en/user/restconfig/rest-config-api.html#layers
      * @param layerName
      * @param recursive @see http://docs.geoserver.org/latest/en/user/restconfig/rest-config-api.html#layer-recurse
      * @return
      * @throws MalformedURLException
      * @throws IOException
      */
     boolean deleteLayer(final String layerName, final String username, final String password, final boolean recursive) throws MalformedURLException, IOException {
         if (StringUtils.isEmpty(layerName) || StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) throw new InvalidParameterException();
         int responseCode = 0;
 
         log.info("Attempting to delete layer '"+layerName+"'");
         responseCode = this.getResponseCode(new URL(this.getGeoServerURLString() + "/rest/layers/" + layerName + ((recursive) ? "?recurse=true" : "")), "DELETE", "text/xml", null, username, password);
         switch (responseCode) {
             case 200: {
                 log.info("Layer '"+layerName+"' was successfully deleted.");
                 return true;
             } // Layer successfully removed
             case 404: {
                 log.info("Layer '"+layerName+"' was not found on server.");
                 return false;
             }
             case 500: {
                 log.info("Layer '"+layerName+"' could not be deleted.");
                 return false;
             }
             default: {
                 log.info("Layer '"+layerName+"' could not be deleted.");
                 return false;
             }
         }
     }
 
     /**
      * Attempts to remove a datastore from the Geoserver server.
      *
      * @see http://docs.geoserver.org/latest/en/user/restconfig/rest-config-api.html#data-stores
      * @param workspace
      * @param datastoreName
      * @param recursive @see http://docs.geoserver.org/latest/en/user/restconfig/rest-config-api.html#datastore-recurse
      * @return
      * @throws MalformedURLException
      * @throws IOException
      */
     boolean deleteDatastore(final String workspace, final String datastoreName, final String username, final String password, final boolean recursive) throws MalformedURLException, IOException, XPathExpressionException {
         if (StringUtils.isEmpty(workspace) || StringUtils.isEmpty(username) || StringUtils.isEmpty(password) || StringUtils.isEmpty(datastoreName)) throw new InvalidParameterException();
 
         int responseCode = 0;
         if (!recursive) {
             // We must first delete all featuretypes for this datastore - we should be using recursive but if it's an
             // earlier version of Geoserver, this is not available
             List<String> featureTypeList = this.retrieveFeatureTypeListFromDatastore(workspace, datastoreName);
             for (String featureType : featureTypeList) {
                 if (!this.deleteFeatureType(workspace, datastoreName, featureType, username, password, true)) {
                     log.warn("One or more feature types under datastore " + datastoreName + " under workspace " + workspace + " could not be deleted. Aborting attempt to delete datastore.");
                     return false;
                 }
             }
         }
         log.info("Attempting to delete datastore '"+datastoreName+"' under workspace '"+workspace+"'");
         responseCode = this.getResponseCode(new URL(this.getGeoServerURLString() + "/rest/workspaces/" + workspace + "/datastores/" + datastoreName + ((recursive) ? "?recurse=true" : "")), "DELETE", "text/xml", null, username, password);
 
         switch (responseCode) {
             case 200: {
                 log.info("Datastore '"+datastoreName+"' under workspace '"+workspace+"' was successfully deleted.");
                 return true;
             } // datastore successfully removed
             case 403: {
                 log.info("Datastore '"+datastoreName+"' under workspace '"+workspace+"' could not be deleted.");
                 return false;
             }
             case 404: {
                 log.info("Datastore '"+datastoreName+"' under workspace '"+workspace+"' was not found on server.");
                 return false;
             }
             case 500: {
                 log.info("Datastore '"+datastoreName+"' under workspace '"+workspace+"' could not be deleted.");
                 return false;
             }
             default: {
                 log.info("Datastore '"+datastoreName+"' under workspace '"+workspace+"' could not be deleted.");
                 return false;
             }
         }
     }
 
     /**
      * Attempts to remove a featuretype from underneath a datastore on the Geoserver server.
      *
      * @param workspace
      * @param datastore
      * @param featuretype
      * @return
      */
     boolean deleteFeatureType(final String workspace, final String datastore, final String featuretype, final String username, final String password, boolean recursive) throws MalformedURLException, IOException {
         if (StringUtils.isEmpty(workspace) || StringUtils.isEmpty(datastore) || StringUtils.isEmpty(featuretype)|| StringUtils.isEmpty(username) || StringUtils.isEmpty(password))throw new InvalidParameterException();
 
         log.info("Attempting to delete feature type '"+featuretype+"' under datastore '"+datastore+"' under workspace '"+workspace+"'");
 
         int responseCode = 0;
 
         responseCode = this.getResponseCode(new URL(this.getGeoServerURLString() + "/rest/workspaces/" + workspace + "/datastores/" + datastore + "/featuretypes/" + featuretype + ((recursive) ? "?recurse=true" : "")), "DELETE", "text/xml", null, username, password);
         switch (responseCode) {
             case 200: {
                 log.info("Feature type '"+featuretype+"' under datastore '"+datastore+"' under workspace '"+workspace+"' was successfully deleted.");
                 return true;
             } // Layer successfully removed
             case 404: {
                 log.info("Feature type '"+featuretype+"' under datastore '"+datastore+"' under workspace '"+workspace+"' was not found on server.");
                 return true;
             }
             case 500: {
                 log.info("Feature type '"+featuretype+"' under datastore '"+datastore+"' under workspace '"+workspace+"' could not be deleted.");
                 return false;
             }
             default: {
                 log.info("Feature type '"+featuretype+"' under datastore '"+datastore+"' under workspace '"+workspace+"' could not be deleted.");
                 return false;
             }
         }
     }
 
     /**
      * Attempts to retrieve a feature type list from a named datastore
      *
      * @param workspace
      * @param datastore
      * @return
      * @throws XPathExpressionException
      * @throws UnsupportedEncodingException
      * @throws MalformedURLException
      * @throws IOException
      */
     public List<String> retrieveFeatureTypeListFromDatastore(final String workspace, final String datastore) throws XPathExpressionException, UnsupportedEncodingException, MalformedURLException, IOException {
         if (StringUtils.isEmpty(workspace) || StringUtils.isEmpty(datastore))throw new InvalidParameterException();
         List<String> result = new ArrayList<String>();
 
         String responseXML = this.listFeatureTypes(workspace, datastore);
         NodeList nodeList = XMLUtils.createNodeListUsingXPathExpression("/featureTypes/featureType/name", responseXML);
 
         for (int nodeIndex = 0;nodeIndex < nodeList.getLength();nodeIndex++) {
             result.add(nodeList.item(nodeIndex).getTextContent());
         }
 
         return result;
     }
 
     public String retrieveDiskLocationOfDatastore(final String workspace, final String datastore) throws MalformedURLException, IOException, XPathExpressionException {
         String result = null;
 
         String responseXML = getResponse(new URL(this.getGeoServerURLString() + "/rest/workspaces/" + workspace + "/datastores/" + datastore + ".xml"), "GET", "text/html", null, null, null);
         result = XMLUtils.createNodeUsingXPathExpression("/dataStore/connectionParameters/entry[@key='url']", responseXML).getTextContent();
 
         return result;
     }
 
     public List<String> createWorkspaceNamesList() throws MalformedURLException, IOException, XPathExpressionException, ParserConfigurationException, SAXException {
         List<String> result = new ArrayList<String>();
 
         NodeList nodeList = XMLUtils.createNodeListUsingXPathExpression("/workspaces/workspace/name", this.listWorkSpaces());
 
         for (int nodeIndex = 0;nodeIndex < nodeList.getLength();nodeIndex++) {
             result.add(nodeList.item(nodeIndex).getTextContent());
         }
 
         return result;
     }
 
     public List<String> createDatastoreListFromWorkspace(final String workspace) throws MalformedURLException, IOException, XPathExpressionException, ParserConfigurationException, SAXException {
         if (!this.workspaceExists(workspace)) return new ArrayList<String>();
 
         List<String> result = new ArrayList<String>();
 
         NodeList nodeList = XMLUtils.createNodeListUsingXPathExpression("/dataStores/dataStore/name", this.listDataStores(workspace));
 
         for (int nodeIndex = 0;nodeIndex < nodeList.getLength();nodeIndex++) {
             result.add(nodeList.item(nodeIndex).getTextContent());
         }
 
         return result;
     }
 
     /**
      * Lists available workspaces
      * @return
      */
     public String listWorkSpaces() throws MalformedURLException, IOException {
         return getResponse(new URL(this.getGeoServerURLString() + "/rest/workspaces.xml"), "GET", "text/xml", null, null, null);
     }
 
     /**
      * @see ManageWorkSpace#listDataStores(java.lang.String)
      *
      * @return
      * @throws MalformedURLException
      */
     public String listDataStores() throws MalformedURLException, IOException {
         return listDataStores("gdp");
     }
 
     /**
      * Lists data stores in a given workspace
      *
      * @param workspace
      * @return
      */
     public String listDataStores(String workspace) throws MalformedURLException, IOException {
         return getResponse(new URL(this.getGeoServerURLString() + "/rest/workspaces/" + workspace + "/datastores.xml"), "GET", "text/html", null, null, null);
     }
 
     public String listFeatureTypes(final String workspace, final String datastore) throws MalformedURLException, IOException {
          return getResponse(new URL(this.getGeoServerURLString() + "/rest/workspaces/" + workspace + "/datastores/" + datastore + "/featuretypes.xml"), "GET", "text/html", null, null, null);
     }
 
     public void createColoredMap(String dataFileLoc, String workspace, String layer, String fromDateString,
             String toDateString, String stat, String attribute, String delim) throws IOException, ParseException {
         log.debug(new StringBuilder("Attempting to create colored map on WFS server located at: ").append(this.getGeoServerURLString()).toString());
         File dataFile = new File(dataFileLoc);
         DateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
         Date fromDate, toDate = null;
         try {
             fromDate = df.parse(fromDateString);
             if (!toDateString.equals("")) {
                 toDate = df.parse(toDateString);
             }
         } catch (ParseException e) {
             log.error("ERROR: could not parse requested date.");
             System.err.println("ERROR: could not parse requested date.");
             return;
         }
 
         // these ArrayList's are populated in parseCSV
         ArrayList<String> attributeValues = new ArrayList<String>();
         ArrayList<Float> requestedStats = new ArrayList<Float>();
 
         parseCSV(dataFile, fromDate, toDate, stat, delim, attributeValues, requestedStats);
         String sld = createStyle(attributeValues, requestedStats, attribute);
 
         if (sld == null) {
             System.err.println("Could not create map style.");
             return;
         }
 
         String styleName = "colors" + workspace;
 
         // create style in geoserver
         if (!styleExists(styleName)) {
             sendPacket(new URL(this.getGeoServerURLString() + "/rest/styles?name=" + styleName),
                     "POST", "application/vnd.ogc.sld+xml", sld);
         } else {
             sendPacket(new URL(this.getGeoServerURLString() + "/rest/styles/" + styleName),
                     "PUT", "application/vnd.ogc.sld+xml", sld);
         }
 
         // set layer to use the new style
         sendPacket(new URL(this.getGeoServerURLString() + "/rest/layers/" + workspace + ":" + layer), "PUT", "text/xml",
                 "<layer><defaultStyle><name>" + styleName + "</name></defaultStyle>"
                 + "<enabled>true</enabled></layer>");
         log.debug(new StringBuilder("Successfully created colored map on WFS server located at: ").append(this.getGeoServerURLString()).toString());
 
     }
 
     String getResponse(URL url, String requestMethod, String contentType, String content, String user, String pass, String... requestProperties) throws MalformedURLException, IOException {
 
         HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
         httpConnection.setDoOutput(true);
         httpConnection.setRequestMethod(requestMethod);
 
         //Set authentication
         String u = (user == null || "".equals(user)) ? AppConstant.WFS_USER.getValue() : user;
         String p = (pass == null || "".equals(pass)) ? AppConstant.WFS_PASS.getValue() : pass;
         String encoding = new sun.misc.BASE64Encoder().encode((u + ":" + p).getBytes());
         httpConnection.addRequestProperty("Authorization", "Basic " + encoding);
 
         if (contentType != null)  httpConnection.addRequestProperty("Content-Type", contentType);
 
         for (int i = 0; i < requestProperties.length; i += 2)  httpConnection.addRequestProperty(requestProperties[i], requestProperties[i + 1]);
 
         int responseCode = httpConnection.getResponseCode();
         String responseMessage = httpConnection.getResponseMessage();
 
         StringBuilder responseString = new StringBuilder();
         BufferedReader br = null;
         try {
             String line = null;
             br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream(), "UTF-8"));
             while ((line = br.readLine()) != null) {
                 responseString.append(line);
             }
         } finally {
             if (br != null) br.close();
             httpConnection.disconnect();
         }
         return responseString.toString();
     }
 
     int getResponseCode(URL url, String requestMethod, String contentType, String content, String user, String pass, String... requestProperties) throws MalformedURLException, IOException {
         HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
         httpConnection.setDoOutput(true);
         httpConnection.setRequestMethod(requestMethod);
 
         //Set authentication
         String u = (user == null || "".equals(user)) ? AppConstant.WFS_USER.getValue() : user;
         String p = (pass == null || "".equals(pass)) ? AppConstant.WFS_PASS.getValue() : pass;
         String encoding = new sun.misc.BASE64Encoder().encode((u + ":" + p).getBytes());
         httpConnection.addRequestProperty("Authorization", "Basic " + encoding);
 
         if (contentType != null)  httpConnection.addRequestProperty("Content-Type", contentType);
 
         for (int i = 0; i < requestProperties.length; i += 2)  httpConnection.addRequestProperty(requestProperties[i], requestProperties[i + 1]);
 
         if (content != null) {
             OutputStreamWriter workspacesWriter = new OutputStreamWriter(httpConnection.getOutputStream());
             workspacesWriter.write(content);
             workspacesWriter.close();
         }
 
         return httpConnection.getResponseCode();
     }
 
     void sendPacket(URL url, String requestMethod, String contentType, String content,
             String... requestProperties) throws IOException {
 
         HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
         httpConnection.setDoOutput(true);
         httpConnection.setRequestMethod(requestMethod);
 
         String u = (AppConstant.WFS_USER.getValue() == null || "".equals(AppConstant.WFS_USER.getValue())) ? "admin" : AppConstant.WFS_USER.getValue();
         String p = (AppConstant.WFS_PASS.getValue() == null || "".equals(AppConstant.WFS_PASS.getValue())) ? "geoserver" : AppConstant.WFS_PASS.getValue();
         String encoding = new sun.misc.BASE64Encoder().encode((u + ":" + p).getBytes());
         httpConnection.addRequestProperty("Authorization", "Basic " + encoding);
 
         if (contentType != null) {
             httpConnection.addRequestProperty("Content-Type", contentType);
         }
 
         for (int i = 0; i < requestProperties.length; i += 2) {
             httpConnection.addRequestProperty(requestProperties[i], requestProperties[i + 1]);
         }
 
         if (content != null) {
             OutputStreamWriter workspacesWriter = new OutputStreamWriter(httpConnection.getOutputStream());
             workspacesWriter.write(content);
             workspacesWriter.close();
         }
 
         // For some reason this has to be here for the packet above to be sent //
         BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
         String line;
         while ((line = reader.readLine()) != null) {
             //System.out.println(line);
         }
         reader.close();
     }
 
 
     String createWorkspaceXML(String workspace) {
         return new String("<workspace><name>" + workspace + "</name></workspace>");
     }
 
     String createDataStoreXML(String name, String workspace, String namespace, String url) {
 
         return  "<dataStore>"
                 + "  <name>" + name + "</name>"
                 + "  <type>Shapefile</type>"
                 + "  <enabled>true</enabled>"
                 + "  <workspace>"
                 + "    <name>" + workspace + "</name>"
                 + "  </workspace>"
                 + "  <connectionParameters>"
                 + "    <entry key=\"memory mapped buffer\">true</entry>"
                 + "    <entry key=\"create spatial index\">true</entry>"
                 + "    <entry key=\"charset\">ISO-8859-1</entry>"
                 + "    <entry key=\"url\">file:" + url + "</entry>"
                 + "    <entry key=\"namespace\">" + namespace + "</entry>"
                 + "  </connectionParameters>"
                 + "</dataStore>";
     }
 
     String createFeatureTypeXML(String name, String workspace, String nativeCRS, String declaredCRS) {
 
         return  "<featureType>"
                 + "  <name>" + name + "</name>"
                 + "  <nativeName>" + name + "</nativeName>"
                 + "  <namespace>"
                 + "    <name>" + workspace + "</name>"
                 + "  </namespace>"
                 + "  <title>" + name + "</title>"
                 + "  <enabled>true</enabled>"
                 // use CDATA as this may contain WKT with XML reserved characters
                 + "  <nativeCRS><![CDATA[" + nativeCRS + "]]></nativeCRS>"
                 + "  <srs>" + declaredCRS + "</srs>"
                 + "  <store class=\"dataStore\">"
                 + "    <name>" + name + "</name>" // this is actually the datastore name (we keep it the same as the layer name)
                 + "  </store>"
                 + "</featureType>";
     }
 
     public ArrayList<String> parseDates(File data, String delim) {
         ArrayList<String> dates = new ArrayList<String>();
 
         try {
             BufferedReader reader = new BufferedReader(new FileReader(data));
             String line;
 
             line = reader.readLine();
             line = reader.readLine(); // skip past column labels
 
             String firstValue;
 
             while (reader.ready()) {
                 line = reader.readLine();
                 firstValue = line.split(delim)[0];
 
                 if ("ALL TIMESTEPS".equals(firstValue)) {
                     break;
                 }
 
                 dates.add(firstValue);
             }
         } catch (IOException e) {
             System.err.println("Error retrieving dates");
         }
 
         return dates;
     }
 
     // TODO: need to get this info some way other than parsing the csv
     void parseCSV(File data, Date fromDate, Date toDate, String stat, String delim,
             ArrayList<String> attributeValues, //
             ArrayList<Float> requestedStats) //
     {
 
         DateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
 
         try {
             BufferedReader reader = new BufferedReader(new FileReader(data));
             String line;
 
             // Count number of stats per attribute
             line = reader.readLine();
             String dupHeaderValues[] = line.split(delim);
             if (!"ALL ATTRIBUTES".equals(dupHeaderValues[dupHeaderValues.length - 1])) {
                 System.out.println("ERROR: Last header value is not ALL ATTRIBUTES");
                 return;
             }
 
             int statsPerHeaderValue = 0;
             String gc = dupHeaderValues[dupHeaderValues.length - 1];
             while ("ALL ATTRIBUTES".equals(gc)) {
                 statsPerHeaderValue++;
                 gc = dupHeaderValues[dupHeaderValues.length - 1 - statsPerHeaderValue];
             }
 
             System.out.println("Stats per header value: " + statsPerHeaderValue);
 
             // Find location of chosen stat
             line = reader.readLine();
             String stats[] = line.split(delim);
             if (!"TIMESTEP".equals(stats[0])) {
                 System.out.println("ERROR: First value is not TIMESTEP");
                 return;
             }
 
             int firstStatIndex = 1;
             String val = stats[firstStatIndex];
             while (!val.startsWith(stat)) {
                 firstStatIndex++;
                 val = stats[firstStatIndex];
 
                 if (firstStatIndex > statsPerHeaderValue) {
                     System.out.println("ERROR: stat doesn't exist");
                     return;
                 }
             }
 
             System.out.println("First stat index: " + firstStatIndex);
 
             // Find chosen date
             String firstValue;
             while (reader.ready()) {
                 line = reader.readLine();
                 firstValue = line.split(delim)[0];
 
                 if ("ALL TIMESTEPS".equals(firstValue)) {
                     System.out.println("ERROR: from date not found");
                     return;
                 }
 
                 if (df.parse(firstValue).compareTo(fromDate) == 0) {
                     break;
                 }
             }
 
             String values[] = line.split(delim);
             //							 don't read in totals at end
             for (int i = firstStatIndex; i < values.length - statsPerHeaderValue; i += statsPerHeaderValue) {
                 requestedStats.add(Float.valueOf(values[i]));
                 attributeValues.add(dupHeaderValues[i]);
             }
 
             float rangeStats[] = new float[requestedStats.size()];
 
             if (toDate != null) {
                 while (reader.ready()) {
                     line = reader.readLine();
                     firstValue = line.split(delim)[0];
 
                     if ("ALL".equals(firstValue)) {
                         System.out.println("ERROR: to date not found");
                         return;
                     }
 
                     values = line.split(delim);
                     for (int i = firstStatIndex, j = 0; i < values.length - statsPerHeaderValue; i += statsPerHeaderValue, j++) {
                         rangeStats[j] += Float.parseFloat(values[i]);
                     }
 
                     if (df.parse(firstValue).compareTo(toDate) == 0) {
                         break;
                     }
                 }
 
                 for (int i = 0; i < requestedStats.size(); i++) {
                     requestedStats.set(i, Float.valueOf(requestedStats.get(i).floatValue() + rangeStats[i]));
                 }
             }
         } catch (IOException e) {
         } catch (ParseException e) {
         }
     }
 
     String createStyle(ArrayList<String> attributeValues, ArrayList<Float> requestedStats, String attribute) {
 
         if (attributeValues.size() == 0 || requestedStats.size() == 0) {
             return null;
         }
 
         // Calculate spread of data
         float maxVal = Float.NEGATIVE_INFINITY;
         float minVal = Float.POSITIVE_INFINITY;
         for (Float f : requestedStats) {
             if (f.floatValue() < minVal) {
                 minVal = f.floatValue();
             }
             if (f.floatValue() > maxVal) {
                 maxVal = f.floatValue();
             }
         }
         float spread = maxVal - minVal;
         if (spread == 0) {
             spread = 1;
         }
 
         String style =
                 "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
                 + "<StyledLayerDescriptor version=\"1.0.0\""
                 + "    xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd\""
                 + "    xmlns=\"http://www.opengis.net/sld\""
                 + "    xmlns:ogc=\"http://www.opengis.net/ogc\""
                 + "    xmlns:xlink=\"http://www.w3.org/1999/xlink\""
                 + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                 + "  <NamedLayer>"
                 + "    <Name>Colors</Name>"
                 + "    <UserStyle>"
                 + "      <Title>Colors</Title>"
                 + "      <FeatureTypeStyle>";
 
         String color;
         for (int i = 0; i < requestedStats.size(); i++) {
             float f = requestedStats.get(i).floatValue();
             String attributeValue = attributeValues.get(i);
 
             //                                  avoid divide by zero
             float temp = -(f - minVal) / spread + 1;
             temp = temp * 200;
             String blgr = String.format("%02x", (int) temp);
 
             color = "FF" + blgr + blgr;
 
             style += "   <Rule>"
                     + "          <ogc:Filter>"
                     + "            <ogc:PropertyIsEqualTo>"
                     + "              <ogc:PropertyName>" + attribute + "</ogc:PropertyName>"
                     + "              <ogc:Literal>" + attributeValue + "</ogc:Literal>"
                     + "            </ogc:PropertyIsEqualTo>"
                     + "          </ogc:Filter>"
                     + "          <PolygonSymbolizer>"
                     + "            <Fill>"
                     + "              <CssParameter name=\"fill\">#" + color + "</CssParameter>"
                     + "            </Fill>"
                     + "			 <Stroke>"
                     + "			   <CssParameter name=\"stroke\">#000000</CssParameter>"
                     + "			   <CssParameter name=\"stroke-width\">1</CssParameter>"
                     + "			 </Stroke>"
                     + "          </PolygonSymbolizer>"
                     + "        </Rule>";
         }
 
         style += "	   </FeatureTypeStyle>"
                 + "    </UserStyle>"
                 + "  </NamedLayer>"
                 + "</StyledLayerDescriptor>";
 
         return style;
     }
 
     /**
      * @return the geoServerURLString
      */
     public URL getGeoServerURL() {
         return getGeoServerURL();
     }
 
     /**
      * @param geoServerURLString the geoServerURLString to set
      */
     public void setGeoServerURL(URL geoServerURL) {
         this.setGeoServerURL(geoServerURL);
     }
 
     /**
      * @return the geoServerURLString
      */
     public String getGeoServerURLString() {
         return geoServerURLString;
     }
 
     /**
      * @param geoServerURLString the geoServerURLString to set
      */
     public void setGeoServerURLString(String geoServerURLString) {
         this.geoServerURLString = geoServerURLString;
     }
 
 }
