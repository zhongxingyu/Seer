 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.gooddata.integration.rest;
 
 import com.gooddata.exception.*;
 import com.gooddata.integration.model.Column;
 import com.gooddata.integration.model.Project;
 import com.gooddata.integration.model.SLI;
 import com.gooddata.integration.rest.configuration.NamePasswordConfiguration;
 import com.gooddata.util.FileUtil;
 import com.gooddata.util.NetUtil;
 
 import net.sf.json.JSON;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.DeleteMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.log4j.Logger;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * The GoodData REST API Java wrapper.
  *
  * @author Zdenek Svoboda <zd@gooddata.org>
  * @version 1.0
  */
 public class GdcRESTApiWrapper {
 
     private static Logger l = Logger.getLogger(GdcRESTApiWrapper.class);
 
     /**
      * GDC URIs
      */
     private static final String PLATFORM_URI = "/gdc/";
     private static final String MD_URI = "/gdc/md/";
     private static final String LOGIN_URI = "/gdc/account/login";
     private static final String DOMAIN_URI = "/gdc/account/domains";
     private static final String DOMAIN_USERS_SUFFIX = "/users";
     private static final String PROJECT_USERS_SUFFIX = "/users";
     private static final String PROJECT_ROLES_SUFFIX = "/roles";
     private static final String TOKEN_URI = "/gdc/account/token";
     private static final String DATA_INTERFACES_URI = "/ldm/singleloadinterface";
     private static final String PROJECTS_URI = "/gdc/projects";
     private static final String PULL_URI = "/etl/pull";
     private static final String IDENTIFIER_URI = "/identifiers";
     private static final String SLI_DESCRIPTOR_URI = "/descriptor";
     public static final String MAQL_EXEC_URI = "/ldm/manage";
     public static final String MAQL_ASYNC_EXEC_URI = "/ldm/manage2";
     public static final String DML_EXEC_URI = "/dml/manage";
     public static final String PROJECT_EXPORT_URI = "/maintenance/export";
     public static final String PROJECT_IMPORT_URI = "/maintenance/import";
     public static final String PROJECT_PARTIAL_EXPORT_URI = "/maintenance/partialmdexport";
     public static final String PROJECT_PARTIAL_IMPORT_URI = "/maintenance/partialmdimport";
     public static final String REPORT_QUERY = "/query/reports";
     public static final String ATTR_QUERY = "/query/attributes";
     public static final String EXECUTOR = "/gdc/xtab2/executor3";
     public static final String EXPORT_EXECUTOR = "/gdc/exporter/executor";
     public static final String INVITATION_URI = "/invitations";
     public static final String ETL_MODE_URI = "/etl/mode";
     public static final String OBJ_URI = "/obj";
     public static final String ROLES_URI = "/roles";
     public static final String USERS_URI = "/users";
     public static final String ETL_MODE_DLI = "DLI";
     public static final String ETL_MODE_VOID = "VOID";
     public static final String LINKS_UPLOADS_KEY = "uploads";
 
     public static final String DLI_MANIFEST_FILENAME = "upload_info.json";
     
     public static final String QUERY_PROJECTDASHBOARDS = "projectdashboards";
     public static final String QUERY_FOLDERS = "folders";
     public static final String QUERY_DATASETS = "datasets";
     public static final String QUERY_DIMENSIONS = "dimensions";
     public static final String QUERY_PREFIX = "/query/";
 
     protected HttpClient client;
     protected NamePasswordConfiguration config;
     private JSONObject userLogin = null;
     private JSONObject profile;
 
     private static HashMap<String, String> ROLES = new HashMap<String, String>();
 
     /* TODO This is fragile and may not work for all projects and/or future versions.
      * Use /gdc/projects/{projectId}/roles to retrieve roles for a particular project.
      */
     static {
         ROLES.put("ADMIN", "adminRole");
         ROLES.put("EDITOR", "editorRole");
         ROLES.put("DASHBOARD ONLY", "dashboardOnlyRole");
         ROLES.put("UNVERIFIED ADMIN", "unverifiedAdminRole");
         ROLES.put("READONLY", "readOnlyUserRole");
     }
 
     /**
      * Constructs the GoodData REST API Java wrapper
      *
      * @param config NamePasswordConfiguration object with the GDC name and password configuration
      */
     public GdcRESTApiWrapper(NamePasswordConfiguration config) {
         this.config = config;
         client = new HttpClient();
         NetUtil.configureHttpProxy(client);
     }
 
     /**
      * GDC login - obtain GDC SSToken
      *
      * @throws HttpMethodException
      */
     public void login() throws HttpMethodException {
         //logout();
         l.debug("Logging into GoodData.");
         JSONObject loginStructure = getLoginStructure();
         PostMethod loginPost = createPostMethod(getServerUrl() + LOGIN_URI);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(loginStructure.toString().getBytes()));
         loginPost.setRequestEntity(request);
         try {
             String resp = executeMethodOk(loginPost, false); // do not re-login on SC_UNAUTHORIZED
             // enabling this prevents the following message:
             // WARN org.apache.commons.httpclient.HttpMethodDirector -
             // Unable to respond to any of these challenges:
             // {gooddata=GoodData realm="GoodData API" cookie=GDCAuthTT}
             // appearing always after those:
             // DEBUG com.gooddata.integration.rest.GdcRESTApiWrapper -
             // Logging into GoodData.
             // DEBUG com.gooddata.integration.rest.GdcRESTApiWrapper -
             // Successfully logged into GoodData.
             setTokenCookie();
             l.debug("Successfully logged into GoodData.");
             JSONObject rsp = JSONObject.fromObject(resp);
             userLogin = rsp.getJSONObject("userLogin");
             String profileUri = userLogin.getString("profile");
             if (profileUri != null && profileUri.length() > 0) {
                 GetMethod gm = createGetMethod(getServerUrl() + profileUri);
                 try {
                 	resp = executeMethodOk(gm);
                 	this.profile = JSONObject.fromObject(resp);
                 }
                 finally {
                     gm.releaseConnection();
                 }
             } else {
                 l.debug("Empty account profile.");
                 throw new GdcRestApiException("Empty account profile.");
             }
         } finally {
             loginPost.releaseConnection();
         }
 
     }
 
     /**
      * Creates a new login JSON structure
      *
      * @return the login JSON structure
      */
     private JSONObject getLoginStructure() {
         JSONObject credentialsStructure = new JSONObject();
         credentialsStructure.put("login", config.getUsername());
         credentialsStructure.put("password", config.getPassword());
         credentialsStructure.put("remember", 1);
 
         JSONObject loginStructure = new JSONObject();
         loginStructure.put("postUserLogin", credentialsStructure);
         return loginStructure;
     }
 
     /**
      * Sets the SS token
      *
      * @throws HttpMethodException
      */
     private void setTokenCookie() throws HttpMethodException {
         HttpMethod secutityTokenGet = createGetMethod(getServerUrl() + TOKEN_URI);
 
         try {
             executeMethodOk(secutityTokenGet);
         } finally {
             secutityTokenGet.releaseConnection();
         }
     }
 
 
     /**
      * GDC logout - remove active session, if any exists
      *
      * @throws HttpMethodException
      */
     public void logout() throws HttpMethodException {
         if (userLogin == null)
             return;
         l.debug("Logging out.");
         DeleteMethod logoutDelete = createDeleteMethod(getServerUrl() + userLogin.getString("state"));
         try {
             String resp = executeMethodOk(logoutDelete, false); // do not re-login on SC_UNAUTHORIZED
             userLogin = null;
             profile = null;
             l.debug("Successfully logged out.");
         } finally {
             logoutDelete.releaseConnection();
         }
         this.client = new HttpClient();
         NetUtil.configureHttpProxy( client );
     }
 
     /**
      * Retrieves the project info by the project's ID
      *
      * @param id the project id
      * @return the GoodDataProjectInfo populated with the project's information
      * @throws HttpMethodException
      * @throws GdcProjectAccessException
      */
     public Project getProjectById(String id) throws HttpMethodException, GdcProjectAccessException {
         l.debug("Getting project by id=" + id);
         HttpMethod req = createGetMethod(getServerUrl() + PROJECTS_URI + "/" + id);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             if(parsedResp != null && !parsedResp.isEmpty() && !parsedResp.isNullObject()) {
                 JSONObject project = parsedResp.getJSONObject("project");
                 if(project != null && !project.isEmpty() && !project.isNullObject()) {
                     JSONObject meta = project.getJSONObject("meta");
                     String title = meta.getString("title");
                     if(title != null && title.length() > 0)
                         return new Project(MD_URI + "/" + id, id, title);
                     else
                         throw new InvalidArgumentException("getProjectById: The project structure doesn't contain the title key.");
                 }
                 else {
                     throw new InvalidArgumentException("getProjectById: The project structure doesn't contain the project key.");
                 }
             } else {
                 throw new InvalidArgumentException("getProjectById: Invalid response.");
             }
         } catch (HttpMethodException e) {
             l.debug("The project id=" + id + " doesn't exists.");
             throw new GdcProjectAccessException("The project id=" + id + " doesn't exists.");
         } finally {
             req.releaseConnection();
         }
     }
 
    /**
      * Returns the global platform links
      *
      * @return accessible platform links
      * @throws com.gooddata.exception.HttpMethodException
      *
      */
     @SuppressWarnings("unchecked")
     private Iterator<JSONObject> getPlatformLinks() throws HttpMethodException {
         l.debug("Getting project links.");
         HttpMethod req = createGetMethod(getServerUrl() + PLATFORM_URI);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             JSONObject about = parsedResp.getJSONObject("about");
             JSONArray links = about.getJSONArray("links");
             l.debug("Got platform links " + links);
             return links.iterator();
         } finally {
             req.releaseConnection();
         }
     }
 
     /**
      *
      *
      * @return the WebDav URL from the platform configuration
      */
     public URL getWebDavURL() {
         Iterator<JSONObject> links = getPlatformLinks();
         while(links.hasNext()) {
             JSONObject link = links.next();
             if(link != null && !link.isEmpty() && !link.isNullObject()) {
                 String category = link.getString("category");
                 if(category != null && category.length() > 0 && category.equalsIgnoreCase(LINKS_UPLOADS_KEY)) {
                     try {
                         String uri = link.getString("link");
                         if(uri != null && uri.length()>0) {
                             if(uri.startsWith("/")) {
                                 uri = getServerUrl() + uri;
                             }
                             return new URL(uri);
                         }
                         else {
                             throw new InvalidArgumentException("No uploads URL configured for the server: "+category);
                         }
                     }
                     catch (MalformedURLException e) {
                         throw new InvalidArgumentException("Invalid uploads URL configured for the server: "+category);
                     }
                 }
             }
         }
         throw new InvalidArgumentException("No uploads platform link configured for the GoodData cluster.");
     }
 
     /**
      * Returns a list of project's SLIs
      *
      * @param projectId project's ID
      * @return a list of project's SLIs
      * @throws HttpMethodException       if there is a communication error
      * @throws GdcProjectAccessException if the SLI doesn't exist
      */
     public List<SLI> getSLIs(String projectId) throws HttpMethodException, GdcProjectAccessException {
         l.debug("Getting SLIs from project id=" + projectId);
         List<SLI> list = new ArrayList<SLI>();
         String ifcUri = getSLIsUri(projectId);
         HttpMethod interfacesGet = createGetMethod(ifcUri);
         try {
             String response = executeMethodOk(interfacesGet);
             JSONObject responseObject = JSONObject.fromObject(response);
             if (responseObject.isNullObject()) {
                 l.debug("The project id=" + projectId + " doesn't exist!");
                 throw new GdcProjectAccessException("The project id=" + projectId + " doesn't exist!");
             }
             JSONObject interfaceQuery = responseObject.getJSONObject("about");
             if (interfaceQuery.isNullObject()) {
                 l.debug("The project id=" + projectId + " doesn't exist!");
                 throw new GdcProjectAccessException("The project id=" + projectId + " doesn't exist!");
             }
             JSONArray links = interfaceQuery.getJSONArray("links");
             if (links == null) {
                 l.debug("The project id=" + projectId + " doesn't exist!");
                 throw new GdcProjectAccessException("The project id=" + projectId + " doesn't exist!");
             }
             for (Object ol : links) {
                 JSONObject link = (JSONObject) ol;
                 SLI ii = new SLI(link);
                 list.add(ii);
             }
             l.debug("Got SLIs " + list + " from project id=" + projectId);
         } finally {
             interfacesGet.releaseConnection();
         }
         return list;
     }
 
 
     /**
      * Retrieves the SLI columns
      *
      * @param uri the SLI uri
      * @return list of SLI columns
      * @throws GdcProjectAccessException if the SLI doesn't exist
      * @throws HttpMethodException       if there is a communication issue with the GDC platform
      */
     public List<Column> getSLIColumns(String uri) throws GdcProjectAccessException, HttpMethodException {
         l.debug("Retrieveing SLI columns for SLI uri=" + uri);
         List<Column> list = new ArrayList<Column>();
         HttpMethod sliGet = createGetMethod(getServerUrl() + uri + "/manifest");
         try {
             String response = executeMethodOk(sliGet);
             JSONObject responseObject = JSONObject.fromObject(response);
             if (responseObject.isNullObject()) {
                 l.debug("The SLI uri=" + uri + " doesn't exist!");
                 throw new GdcProjectAccessException("The SLI uri=" + uri + " doesn't exist!");
             }
             JSONObject dataSetSLIManifest = responseObject.getJSONObject("dataSetSLIManifest");
             if (dataSetSLIManifest.isNullObject()) {
                 l.debug("The SLI uri=" + uri + " doesn't exist!");
                 throw new GdcProjectAccessException("The SLI uri=" + uri + " doesn't exist!");
             }
             JSONArray parts = dataSetSLIManifest.getJSONArray("parts");
             for (Object oPart : parts) {
                 list.add(new Column((JSONObject) oPart));
             }
         } finally {
             sliGet.releaseConnection();
         }
         return list;
     }
 
     /**
      * Retrieves the SLI column data type
      *
      * @param projectId           projectId
      * @param sliColumnIdentifier SLI column identifier (name in the SLI manifest)
      * @return the SLI column datatype
      */
     public String getSLIColumnDataType(String projectId, String sliColumnIdentifier) {
         l.debug("Retrieveing SLI column datatype projectId=" + projectId + " SLI column name=" + sliColumnIdentifier);
         MetadataObject o = getMetadataObject(projectId, sliColumnIdentifier);
         if (o != null) {
             JSONObject c = o.getContent();
             if (c != null) {
                 String type = c.getString("columnType");
                 if (type != null && type.length() > 0) {
                     return type;
                 } else {
                     l.debug("Error Retrieveing SLI column datatype projectId=" + projectId + " SLI column name=" + sliColumnIdentifier + " No columnType key in the content.");
                     throw new GdcRestApiException("Error Retrieveing SLI column datatype projectId=" + projectId + " SLI column name=" + sliColumnIdentifier + " No columnType key in the content.");
                 }
             } else {
                 l.debug("Error Retrieveing SLI column datatype projectId=" + projectId + " SLI column name=" + sliColumnIdentifier + " No content structure.");
                 throw new GdcRestApiException("Error Retrieveing SLI column datatype projectId=" + projectId + " SLI column name=" + sliColumnIdentifier + " No content structure.");
             }
         } else {
             l.debug("Error Retrieveing SLI column datatype projectId=" + projectId + " SLI column name=" + sliColumnIdentifier + " MD object doesn't exist.");
             throw new GdcRestApiException("Error Retrieveing SLI column datatype projectId=" + projectId + " SLI column name=" + sliColumnIdentifier + " MD object doesn't exist.");
         }
     }
 
     /**
      * Retrieves the SLI columns
      *
      * @param uri the SLI uri
      * @return JSON manifest
      * @throws GdcProjectAccessException if the SLI doesn't exist
      * @throws HttpMethodException       if there is a communication issue with the GDC platform
      */
     public JSONObject getSLIManifest(String uri) throws GdcProjectAccessException, HttpMethodException {
         l.debug("Retrieveing SLI columns for SLI uri=" + uri);
         List<Column> list = new ArrayList<Column>();
         HttpMethod sliGet = createGetMethod(getServerUrl() + uri + "/manifest");
         try {
             String response = executeMethodOk(sliGet);
             JSONObject responseObject = JSONObject.fromObject(response);
             if (responseObject.isNullObject()) {
                 l.debug("The SLI uri=" + uri + " doesn't exist!");
                 throw new GdcProjectAccessException("The SLI uri=" + uri + " doesn't exist!");
             }
             return responseObject;
         } finally {
             sliGet.releaseConnection();
         }
     }
 
     /**
      * Finds a project SLI by it's id
      *
      * @param id        the SLI id
      * @param projectId the project id
      * @return the SLI
      * @throws GdcProjectAccessException if the SLI doesn't exist
      * @throws HttpMethodException       if there is a communication issue with the GDC platform
      */
     public SLI getSLIById(String id, String projectId) throws GdcProjectAccessException, HttpMethodException {
         l.debug("Get SLI by id=" + id + " project id=" + projectId);
         List<SLI> slis = getSLIs(projectId);
         return getSLIById(id, slis, projectId);
     }
 
     /**
      * Finds a project SLI in list of SLI
      *
      * @param id        the SLI id
      * @param slis      of SLI (related to one project)
      * @param projectId the project id
      * @return the SLI
      * @throws GdcProjectAccessException if the SLI doesn't exist
      */
     public static SLI getSLIById(String id, List<SLI> slis, String projectId) throws GdcProjectAccessException {
         l.debug("Get SLI by id=" + id + " project id=" + projectId);
         for (SLI sli : slis) {
             if (id.equals(sli.getId())) {
                 l.debug("Got SLI by id=" + id + " project id=" + projectId);
                 return sli;
             }
         }
         l.debug("The SLI id=" + id + " doesn't exist in the project id=" + projectId);
         throw new GdcProjectAccessException("The SLI id=" + id + " doesn't exist in the project id=" + projectId);
     }
 
     /**
      * Enumerates all attributes in the project
      *
      * @param projectId project Id
      * @return LIst of attr uris
      */
     public List<String> enumerateAttributes(String projectId) {
         l.debug("Enumerating attributes for project id=" + projectId);
         List<String> list = new ArrayList<String>();
         String qUri = getProjectMdUrl(projectId) + ATTR_QUERY;
         HttpMethod qGet = createGetMethod(qUri);
         try {
             String qr = executeMethodOk(qGet);
             JSONObject q = JSONObject.fromObject(qr);
             if (q.isNullObject()) {
                 l.debug("Enumerating attributes for project id=" + projectId + " failed.");
                 throw new GdcProjectAccessException("Enumerating attributes for project id=" + projectId + " failed.");
             }
             JSONObject qry = q.getJSONObject("query");
             if (qry.isNullObject()) {
                 l.debug("Enumerating attributes for project id=" + projectId + " failed.");
                 throw new GdcProjectAccessException("Enumerating reports for project id=" + projectId + " failed.");
             }
             JSONArray entries = qry.getJSONArray("entries");
             if (entries == null) {
                 l.debug("Enumerating attributes for project id=" + projectId + " failed.");
                 throw new GdcProjectAccessException("Enumerating reports for project id=" + projectId + " failed.");
             }
             for (Object oentry : entries) {
                 JSONObject entry = (JSONObject) oentry;
                 list.add(entry.getString("link"));
             }
         } finally {
             qGet.releaseConnection();
         }
         return list;
     }
 
     /**
      * Gets attribute PK
      *
      * @param attrUri attribute URI
      * @return list of attribute PKs (columns)
      */
     public List<JSONObject> getAttributePk(String attrUri) {
         List<JSONObject> ret = new ArrayList<JSONObject>();
         JSONObject attr = getObjectByUri(attrUri);
         JSONObject a = attr.getJSONObject("attribute");
         if (a != null && !a.isEmpty() && !a.isEmpty()) {
             JSONObject c = a.getJSONObject("content");
             if (c != null && !c.isEmpty() && !c.isEmpty()) {
                 JSONArray pks = c.getJSONArray("pk");
                 if (pks != null && !pks.isEmpty()) {
                     Object[] p = pks.toArray();
                     for (Object pko : p) {
                         JSONObject pk = (JSONObject) pko;
                         String columnUri = pk.getString("data");
                         if (columnUri != null) {
                             ret.add(getObjectByUri(columnUri));
                         } else {
                             l.debug("Error getting attribute PK. No PK data.");
                             throw new GdcProjectAccessException("Error getting attribute PK. No PK data.");
                         }
                     }
                 }
             } else {
                 l.debug("Error getting attribute PK. No content.");
                 throw new GdcProjectAccessException("Error getting attribute PK. No content.");
             }
 
         } else {
             l.debug("Error getting attribute PK. No attribute.");
             throw new GdcProjectAccessException("Error getting attribute PK. No attribute.");
         }
         return ret;
     }
 
     /**
      * Gets attribute FK
      *
      * @param attrUri attribute URI
      * @return list of attribute FKs (columns)
      */
     public List<JSONObject> getAttributeFk(String attrUri) {
         List<JSONObject> ret = new ArrayList<JSONObject>();
         JSONObject attr = getObjectByUri(attrUri);
         JSONObject a = attr.getJSONObject("attribute");
         if (a != null && !a.isEmpty() && !a.isEmpty()) {
             JSONObject c = a.getJSONObject("content");
             if (c != null && !c.isEmpty() && !c.isEmpty()) {
                 if (c.containsKey("fk")) {
                     JSONArray pks = c.getJSONArray("fk");
                     if (pks != null && !pks.isEmpty()) {
                         Object[] p = pks.toArray();
                         for (Object pko : p) {
                             JSONObject pk = (JSONObject) pko;
                             String columnUri = pk.getString("data");
                             if (columnUri != null && columnUri.trim().length() > 0) {
                                 ret.add(getObjectByUri(columnUri));
                             } else {
                                 l.debug("Error getting attribute FK. No FK data.");
                                 throw new GdcProjectAccessException("Error getting attribute FK. No FK data.");
                             }
                         }
                     }
                 }
             } else {
                 l.debug("Error getting attribute FK. No content.");
                 throw new GdcProjectAccessException("Error getting attribute FK. No content.");
             }
 
         } else {
             l.debug("Error getting attribute FK. No attribute.");
             throw new GdcProjectAccessException("Error getting attribute FK. No attribute.");
         }
         return ret;
     }
 
     /**
      * Gets column DB name
      *
      * @param column column object
      * @return column DB name
      */
     public String getColumnDbName(JSONObject column) {
         JSONObject cl = column.getJSONObject("column");
         if (cl != null && !cl.isEmpty() && !cl.isEmpty()) {
             JSONObject c = cl.getJSONObject("content");
             if (c != null && !c.isEmpty() && !c.isEmpty()) {
                 String cn = c.getString("columnDBName");
                 if (cn != null && cn.trim().length() > 0) {
                     return cn;
                 } else {
                     l.debug("Error getting column name. No columnDBName.");
                     throw new GdcProjectAccessException("Error getting column name. No columnDBName.");
                 }
             } else {
                 l.debug("Error getting column name. No content.");
                 throw new GdcProjectAccessException("Error getting column name. No content.");
             }
         } else {
             l.debug("Error getting column name. No column.");
             throw new GdcProjectAccessException("Error getting column name. No column.");
         }
     }
 
     /**
      * Gets column table name
      *
      * @param column column object
      * @return column table name
      */
     public String getColumnTableName(JSONObject column) {
         JSONObject cl = column.getJSONObject("column");
         if (cl != null && !cl.isEmpty() && !cl.isEmpty()) {
             JSONObject c = cl.getJSONObject("content");
             if (c != null && !c.isEmpty() && !c.isEmpty()) {
                 String t = c.getString("table");
                 if (t != null && t.trim().length() > 0) {
                     JSONObject tbl = getObjectByUri(t);
                     JSONObject root = tbl.getJSONObject("table");
                     if (root != null && !root.isEmpty() && !root.isEmpty()) {
                         c = root.getJSONObject("content");
                         if (c != null && !c.isEmpty() && !c.isEmpty()) {
                             String dl = c.getString("activeDataLoad");
                             if (dl != null && dl.trim().length() > 0) {
                                 JSONObject tdl = getObjectByUri(dl);
                                 root = tdl.getJSONObject("tableDataLoad");
                                 if (root != null && !root.isEmpty() && !root.isEmpty()) {
                                     c = root.getJSONObject("content");
                                     if (c != null && !c.isEmpty() && !c.isEmpty()) {
                                         String tn = c.getString("dataSourceLocation");
                                         if (tn != null && tn.trim().length() > 0) {
                                             return tn;
                                         } else {
                                             l.debug("Error getting column name. No dataSourceLocation.");
                                             throw new GdcProjectAccessException("Error getting column name. No dataSourceLocation.");
                                         }
                                     } else {
                                         l.debug("Error getting column name. No active table data load content.");
                                         throw new GdcProjectAccessException("Error getting column name. No active table data load content.");
                                     }
                                 } else {
                                     l.debug("Error getting column name. No table data load root.");
                                     throw new GdcProjectAccessException("Error getting column name. No table data load root.");
                                 }
                             } else {
                                 l.debug("Error getting column name. No active data load.");
                                 throw new GdcProjectAccessException("Error getting column name. No active data load.");
                             }
                         } else {
                             l.debug("Error getting column name. No table content.");
                             throw new GdcProjectAccessException("Error getting column name. No table content.");
                         }
                     } else {
                         l.debug("Error getting column table. No table root.");
                         throw new GdcProjectAccessException("Error getting column table. No table root.");
                     }
                 } else {
                     l.debug("Error getting column name. No table.");
                     throw new GdcProjectAccessException("Error getting column name. No table.");
                 }
             } else {
                 l.debug("Error getting column name. No content.");
                 throw new GdcProjectAccessException("Error getting column name. No content.");
             }
         } else {
             l.debug("Error getting column name. No column.");
             throw new GdcProjectAccessException("Error getting column name. No column.");
         }
     }
 
     /**
      * Enumerates all attributes in the project
      *
      * @param attrUri attribute URI
      * @return attribute object
      */
     public JSONObject getAttribute(String attrUri) {
         l.debug("Getting attribute uri=" + attrUri);
         String qUri = getServerUrl() + attrUri;
         HttpMethod qGet = createGetMethod(qUri);
         try {
             String qr = executeMethodOk(qGet);
             return JSONObject.fromObject(qr);
         } finally {
             qGet.releaseConnection();
         }
     }
 
     /**
      * Enumerates all reports on in a project
      *
      * @param projectId project Id
      * @return LIst of report uris
      */
     public List<String> enumerateReports(String projectId) {
         l.debug("Enumerating reports for project id=" + projectId);
         List<String> list = new ArrayList<String>();
         String qUri = getProjectMdUrl(projectId) + REPORT_QUERY;
         HttpMethod qGet = createGetMethod(qUri);
         try {
             String qr = executeMethodOk(qGet);
             JSONObject q = JSONObject.fromObject(qr);
             if (q.isNullObject()) {
                 l.debug("Enumerating reports for project id=" + projectId + " failed.");
                 throw new GdcProjectAccessException("Enumerating reports for project id=" + projectId + " failed.");
             }
             JSONObject qry = q.getJSONObject("query");
             if (qry.isNullObject()) {
                 l.debug("Enumerating reports for project id=" + projectId + " failed.");
                 throw new GdcProjectAccessException("Enumerating reports for project id=" + projectId + " failed.");
             }
             JSONArray entries = qry.getJSONArray("entries");
             if (entries == null) {
                 l.debug("Enumerating reports for project id=" + projectId + " failed.");
                 throw new GdcProjectAccessException("Enumerating reports for project id=" + projectId + " failed.");
             }
             for (Object oentry : entries) {
                 JSONObject entry = (JSONObject) oentry;
                 int deprecated = entry.getInt("deprecated");
                 if (deprecated == 0)
                     list.add(entry.getString("link"));
             }
         } finally {
             qGet.releaseConnection();
         }
         return list;
     }
 
     private String getProjectIdFromObjectUri(String uri) {
         Pattern regexp = Pattern.compile("gdc/md/.*?/");
         Matcher m = regexp.matcher(uri);
         if (m.find()) {
             return m.group().split("/")[2];
         } else {
             l.debug("The passed string '" + uri + "' doesn't have the GoodData URI structure!");
             throw new InvalidParameterException("The passed string '" + uri + "' doesn't have the GoodData URI structure!");
         }
     }
 
     /**
      * Computes the metric value
      *
      * @param metricUri metric URI
      * @return the metric value
      */
     public double computeMetric(String metricUri) {
         l.debug("Computing metric uri=" + metricUri);
         double retVal = 0;
         String projectId = getProjectIdFromObjectUri(metricUri);
         JSONObject reportDefinition = new JSONObject();
 
         JSONObject metric = new JSONObject();
         metric.put("alias", "");
         metric.put("uri", metricUri);
         JSONArray metrics = new JSONArray();
         metrics.add(metric);
         JSONArray columns = new JSONArray();
         columns.add("metricGroup");
         JSONObject grid = new JSONObject();
         grid.put("metrics", metrics);
         grid.put("columns", columns);
         grid.put("rows", new JSONArray());
         grid.put("columnWidths", new JSONArray());
 
         JSONObject sort = new JSONObject();
         sort.put("columns", new JSONArray());
         sort.put("rows", new JSONArray());
 
         grid.put("sort", sort);
 
         JSONObject content = new JSONObject();
         content.put("grid", grid);
         content.put("filters", new JSONArray());
         content.put("format", "grid");
 
         reportDefinition.put("content", content);
 
         JSONObject meta = new JSONObject();
         meta.put("category", "reportDefinition");
         meta.put("title", "N/A");
 
         reportDefinition.put("meta", meta);
 
         MetadataObject obj = new MetadataObject();
         obj.put("reportDefinition", reportDefinition);
         MetadataObject resp = new MetadataObject(createMetadataObject(projectId, obj));
 
         int retryCnt = 1000;
         boolean hasFinished = false;
         while (retryCnt-- > 0 && !hasFinished) {
             try {
                 String dataResultUri = executeReportDefinition(resp.getUri());
                 JSONObject result = getObjectByUri(dataResultUri);
                 hasFinished = true;
                 if (result != null && !result.isEmpty() && !result.isNullObject()) {
                     JSONObject xtabData = result.getJSONObject("xtab_data");
                     if (xtabData != null && !xtabData.isEmpty() && !xtabData.isNullObject()) {
                         JSONArray data = xtabData.getJSONArray("data");
                         if (data != null && !data.isEmpty()) {
                             retVal = data.getJSONArray(0).getDouble(0);
                         } else {
                             l.debug("Can't compute the metric. No data structure in result.");
                             throw new InvalidParameterException("Can't compute the metric. No data structure in result.");
                         }
                     } else {
                         l.debug("Can't compute the metric. No xtab_data structure in result.");
                         throw new InvalidParameterException("Can't compute the metric. No xtab_data structure in result.");
                     }
                 } else {
                     l.debug("Can't compute the metric. No result from XTAB.");
                     throw new InvalidParameterException("Can't compute the metric. No result from XTAB.");
                 }
             } catch (HttpMethodNotFinishedYetException e) {
                 l.debug("computeMetric: Waiting for DataResult");
                 try {
                     Thread.sleep(500);
                 } catch (InterruptedException ex) {
                     // do nothing
                 }
             }
         }
         l.debug("Metric uri=" + metricUri + " computed. Result is " + retVal);
         return retVal;
     }
 
     /**
      * Computes a simple report and returns the report text
      *
      * @param reportUri report URI
      * @return the report rendered in text
      */
     public String computeReport(String reportUri) {
         l.debug("Computing report uri=" + reportUri);
         String retVal = "";
         int retryCnt = 1000;
         boolean hasFinished = false;
         while (retryCnt-- > 0 && !hasFinished) {
             try {
                 String dataResultUri = executeReport(reportUri);
                 JSONObject result = getObjectByUri(dataResultUri);
                 hasFinished = true;
                 if (result != null && !result.isEmpty() && !result.isNullObject()) {
                     JSONObject xtabData = result.getJSONObject("xtab_data");
                     if (xtabData != null && !xtabData.isEmpty() && !xtabData.isNullObject()) {
                         JSONArray data = xtabData.getJSONArray("data");
                         if (data != null && !data.isEmpty()) {
                             double[] values = new double[data.size()];
                             for (int i = 0; i < data.size(); i++) {
                                 JSONArray vals = data.getJSONArray(i);
                                 values[i] = vals.getDouble(0);
                             }
                             JSONObject rows = xtabData.getJSONObject("rows");
                             if (rows != null && !rows.isEmpty() && !rows.isNullObject()) {
                                 JSONArray lookups = rows.getJSONArray("lookups");
                                 if (lookups != null && !lookups.isEmpty()) {
                                     Map<String, String> attributes = new HashMap<String, String>();
                                     JSONObject lkpData = lookups.getJSONObject(0);
                                     for (Object key : lkpData.keySet()) {
                                         Object value = lkpData.get(key);
                                         if (key != null && value != null)
                                             attributes.put(key.toString(), value.toString());
                                     }
                                     JSONObject tree = rows.getJSONObject("tree");
                                     if (tree != null && !tree.isEmpty() && !tree.isNullObject()) {
                                         Map<String, Integer> indexes = new HashMap<String, Integer>();
                                         JSONObject index = tree.getJSONObject("index");
                                         if (index != null && !index.isEmpty()) {
                                             for (Object key : index.keySet()) {
                                                 if (key != null) {
                                                     JSONArray valIdxs = index.getJSONArray(key.toString());
                                                     if (valIdxs != null && !valIdxs.isEmpty()) {
                                                         indexes.put(key.toString(), valIdxs.getInt(0));
                                                     }
                                                 }
 
                                             }
                                             JSONArray children = tree.getJSONArray("children");
                                             if (children != null && !children.isEmpty()) {
                                                 for (int i = 0; i < children.size(); i++) {
                                                     JSONObject c = children.getJSONObject(i);
                                                     String id = c.getString("id");
                                                     if (id != null && id.length() > 0) {
                                                         String attribute = attributes.get(id);
                                                         int v = indexes.get(id);
                                                         double vl = values[v];
                                                         if (retVal.length() > 0) {
                                                             retVal += ", " + attribute + " : " + vl;
                                                         } else {
                                                             retVal += attribute + " : " + vl;
                                                         }
                                                     } else {
                                                         l.debug("Can't compute the report. No id in children.");
                                                         throw new InvalidParameterException("Can't compute the report. No id in children.");
                                                     }
                                                 }
                                             } else {
                                                 l.debug("Can't compute the report. No tree structure in result.");
                                                 throw new InvalidParameterException("Can't compute the report. No tree structure in result.");
                                             }
                                         } else {
                                             l.debug("Can't compute the report. No index structure in result.");
                                             throw new InvalidParameterException("Can't compute the report. No index structure in result.");
                                         }
                                     } else {
                                         l.debug("Can't compute the report. No tree structure in result.");
                                         throw new InvalidParameterException("Can't compute the report. No tree structure in result.");
                                     }
                                 } else {
                                     l.debug("Can't compute the report. No lookups structure in result.");
                                     throw new InvalidParameterException("Can't compute the report. No lookups structure in result.");
                                 }
                             } else {
                                 l.debug("Can't compute the report. No rows structure in result.");
                                 throw new InvalidParameterException("Can't compute the report. No rows structure in result.");
                             }
 
 
                         } else {
                             l.debug("Can't compute the report. No data structure in result.");
                             throw new InvalidParameterException("Can't compute the report. No data structure in result.");
                         }
                     } else {
                         l.debug("Can't compute the report. No xtab_data structure in result.");
                         throw new InvalidParameterException("Can't compute the report. No xtab_data structure in result.");
                     }
                 } else {
                     l.debug("Can't compute the report. No result from XTAB.");
                     throw new InvalidParameterException("Can't compute the metric. No result from XTAB.");
                 }
             } catch (HttpMethodNotFinishedYetException e) {
                 l.debug("computeReport: Waiting for DataResult");
                 try {
                     Thread.sleep(500);
                 } catch (InterruptedException ex) {
                     // do nothing
                 }
             }
         }
         l.debug("Report uri=" + reportUri + " computed.");
         return retVal;
     }
 
 
     /**
      * Report definition to execute
      *
      * @param reportDefUri report definition to execute
      */
     public String executeReportDefinition(String reportDefUri) {
         l.debug("Executing report definition uri=" + reportDefUri);
         PostMethod execPost = createPostMethod(getServerUrl() + EXECUTOR);
         JSONObject execDef = new JSONObject();
         execDef.put("reportDefinition", reportDefUri);
         JSONObject exec = new JSONObject();
         exec.put("report_req", execDef);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(exec.toString().getBytes()));
         execPost.setRequestEntity(request);
         try {
             String task = executeMethodOk(execPost);
             if (task != null && task.length() > 0) {
                 JSONObject tr = JSONObject.fromObject(task);
                 if (tr.isNullObject()) {
                     l.debug("Executing report definition uri=" + reportDefUri + " failed. Returned invalid result result=" + tr);
                     throw new GdcRestApiException("Executing report definition uri=" + reportDefUri + " failed. " +
                             "Returned invalid result result=" + tr);
                 }
                 JSONObject reportResult = tr.getJSONObject("execResult");
                 if (reportResult.isNullObject()) {
                     l.debug("Executing report definition uri=" + reportDefUri + " failed. Returned invalid result result=" + tr);
                     throw new GdcRestApiException("Executing report definition uri=" + reportDefUri + " failed. " +
                             "Returned invalid result result=" + tr);
                 }
                 String dataResult = reportResult.getString("dataResult");
                 if (dataResult == null || dataResult.length()<=0) {
                     l.debug("Executing report definition uri=" + reportDefUri + " failed. Returned invalid result result=" + tr);
                     throw new GdcRestApiException("Executing report definition uri=" + reportDefUri + " failed. " +
                             "Returned invalid result result=" + tr);
                 }
                 return dataResult;
             } else {
                 l.debug("Executing report definition uri=" + reportDefUri + " failed. Returned invalid task link uri=" + task);
                 throw new GdcRestApiException("Executing report definition uri=" + reportDefUri +
                         " failed. Returned invalid task link uri=" + task);
             }
         } catch (HttpMethodException ex) {
             l.debug("Executing report definition uri=" + reportDefUri + " failed.", ex);
             throw new GdcRestApiException("Executing report definition uri=" + reportDefUri + " failed.");
         } finally {
             execPost.releaseConnection();
         }
     }
 
     /**
      * Report to execute
      *
      * @param reportUri report definition to execute
      */
     public String executeReport(String reportUri) {
         l.debug("Executing report uri=" + reportUri);
         PostMethod execPost = createPostMethod(getServerUrl() + EXECUTOR);
         JSONObject execDef = new JSONObject();
         execDef.put("report", reportUri);
         JSONObject exec = new JSONObject();
         exec.put("report_req", execDef);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(exec.toString().getBytes()));
         execPost.setRequestEntity(request);
         String taskLink = null;
         try {
             String task = executeMethodOk(execPost);
             if (task != null && task.length() > 0) {
                 JSONObject tr = JSONObject.fromObject(task);
                 if (tr.isNullObject()) {
                     l.debug("Executing report uri=" + reportUri + " failed. Returned invalid result=" + tr);
                     throw new GdcRestApiException("Executing report uri=" + reportUri + " failed. " +
                             "Returned invalid result result=" + tr);
                 }
                 JSONObject reportResult = tr.getJSONObject("execResult");
                 if (reportResult.isNullObject()) {
                     l.debug("Executing report uri=" + reportUri + " failed. Returned invalid result=" + tr);
                     throw new GdcRestApiException("Executing report uri=" + reportUri + " failed. " +
                             "Returned invalid result result=" + tr);
                 }
                 String dataResult = reportResult.getString("dataResult");
                 if (dataResult == null || dataResult.length()<=0) {
                     l.debug("Executing report uri=" + reportUri + " failed. Returned invalid dataResult=" + tr);
                     throw new GdcRestApiException("Executing report uri=" + reportUri + " failed. " +
                             "Returned invalid dataResult=" + tr);
                 }
                 return dataResult;
             } else {
                 l.debug("Executing report uri=" + reportUri + " failed. Returned invalid task link uri=" + task);
                 throw new GdcRestApiException("Executing report uri=" + reportUri +
                         " failed. Returned invalid task link uri=" + task);
             }
         } catch (HttpMethodException ex) {
             l.debug("Executing report uri=" + reportUri + " failed.", ex);
             throw new GdcRestApiException("Executing report uri=" + reportUri + " failed.");
         } finally {
             execPost.releaseConnection();
         }
     }
 
     /**
      * Export a report result
      *
      * @param resultUri report result to export
      * @param format    export format (pdf | xls | png | csv)
      */
     public byte[] exportReportResult(String resultUri, String format) {
         l.debug("Exporting report result uri=" + resultUri);
         PostMethod execPost = createPostMethod(getServerUrl() + EXPORT_EXECUTOR);
         JSONObject execDef = new JSONObject();
         execDef.put("report", resultUri);
         execDef.put("format", format);
         JSONObject exec = new JSONObject();
         exec.put("result_req", execDef);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(exec.toString().getBytes()));
         execPost.setRequestEntity(request);
         String taskLink = null;
         try {
             String task = executeMethodOk(execPost);
             if (task != null && task.length() > 0) {
                 JSONObject tr = JSONObject.fromObject(task);
                 if (tr.isNullObject()) {
                     l.debug("Exporting report result uri=" + resultUri + " failed. Returned invalid result=" + tr);
                     throw new GdcRestApiException("Exporting report result uri=" + resultUri + " failed. " +
                             "Returned invalid result=" + tr);
                 }
                 String uri = tr.getString("uri");
                 if (uri != null && uri.length() > 0) {
                     return getReportResult(uri);
                 } else {
                     l.debug("Exporting report result uri=" + resultUri + " failed. Returned invalid result=" + tr);
                     throw new GdcRestApiException("Exporting report result uri=" + resultUri + " failed. " +
                             "Returned invalid result=" + tr);
                 }
             } else {
                 l.debug("Exporting report result uri=" + resultUri + " failed. Returned invalid task link uri=" + task);
                 throw new GdcRestApiException("Exporting report result uri=" + resultUri +
                         " failed. Returned invalid task link uri=" + task);
             }
         } catch (HttpMethodException ex) {
             l.debug("Exporting report result uri=" + resultUri + " failed.", ex);
             throw new GdcRestApiException("Exporting report result uri=" + resultUri + " failed.");
         } finally {
             execPost.releaseConnection();
         }
     }
 
     /**
      * Retrieves the report export result
      *
      * @param uri the export result
      * @return attribute object
      */
     public byte[] getReportResult(String uri) {
         l.debug("Retrieving export result uri=" + uri);
         byte[] buf = null;
         String qUri = getServerUrl() + uri;
         boolean finished = false;
         do {
             HttpMethod qGet = createGetMethod(qUri);
             try {
                 executeMethodOkOnly(qGet);
                 finished = true;
                 buf = qGet.getResponseBody();
             } catch (HttpMethodNotFinishedYetException e) {
                 l.debug("Waiting for exporter to finish.");
                 try {
                     Thread.currentThread().sleep(1000);
                 } catch (InterruptedException ex) {
                     // do nothing
                 }
             } catch (IOException e) {
                 l.debug("Network error during the report result export.", e);
                 throw new GdcRestApiException("Network error during the report result export.", e);
             } finally {
                 qGet.releaseConnection();
             }
         } while (!finished);
         return buf;
     }
 
     /**
      * Kicks the GDC platform to inform it that the FTP transfer is finished.
      *
      * @param projectId the project's ID
      * @param remoteDir the remote (FTP) directory that contains the data
      * @return the link that is used for polling the loading progress
      * @throws GdcRestApiException
      */
     public String startLoading(String projectId, String remoteDir) throws GdcRestApiException {
         l.debug("Initiating data load project id=" + projectId + " remoteDir=" + remoteDir);
         PostMethod pullPost = createPostMethod(getProjectMdUrl(projectId) + PULL_URI);
         JSONObject pullStructure = getPullStructure(remoteDir);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(pullStructure.toString().getBytes()));
         pullPost.setRequestEntity(request);
         String taskLink = null;
         try {
             String response = executeMethodOk(pullPost);
             JSONObject responseObject = JSONObject.fromObject(response);
             taskLink = responseObject.getJSONObject("pullTask").getString("uri");
         } catch (HttpMethodException ex) {
             throw new GdcRestApiException("Loading fails: " + ex.getMessage());
         } finally {
             pullPost.releaseConnection();
         }
         l.debug("Data load project id=" + projectId + " remoteDir=" + remoteDir + " initiated. Status is on uri=" + taskLink);
         return taskLink;
     }
 
     /**
      * Returns the pull API JSON structure
      *
      * @param directory the remote directory
      * @return the pull API JSON structure
      */
     private JSONObject getPullStructure(String directory) {
         JSONObject pullStructure = new JSONObject();
         pullStructure.put("pullIntegration", directory);
         return pullStructure;
     }
 
 
     /**
      * Checks if the loading is finished
      *
      * @param link the link returned from the start loading
      * @return the loading status
      */
     public String getLoadingStatus(String link) throws HttpMethodException {
         l.debug("Getting data loading status uri=" + link);
         HttpMethod ptm = createGetMethod(getServerUrl() + link);
         try {
             String response = executeMethodOk(ptm);
             JSONObject task = JSONObject.fromObject(response);
             String status = task.getString("taskStatus");
             l.debug("Loading status=" + status);
             return status;
         } finally {
             ptm.releaseConnection();
         }
     }
 
 
     /**
      * Create a new GoodData project
      *
      * @param name        project name
      * @param desc        project description
      * @param templateUri project template uri
      * @param driver underlying database driver
      * @param accessToken access token
      * @return the project Id
      * @throws GdcRestApiException
      */
     public String createProject(String name, String desc, String templateUri, String driver, String accessToken) throws GdcRestApiException {
         l.debug("Creating project name=" + name);
         PostMethod createProjectPost = createPostMethod(getServerUrl() + PROJECTS_URI);
         JSONObject createProjectStructure = getCreateProject(name, desc, templateUri, driver, accessToken);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 createProjectStructure.toString().getBytes()));
         createProjectPost.setRequestEntity(request);
         String uri = null;
         try {
             String response = executeMethodOk(createProjectPost);
             JSONObject responseObject = JSONObject.fromObject(response);
             uri = responseObject.getString("uri");
         } catch (HttpMethodException ex) {
             l.debug("Creating project fails: ", ex);
             throw new GdcRestApiException("Creating project fails: ", ex);
         } finally {
             createProjectPost.releaseConnection();
         }
 
         if (uri != null && uri.length() > 0) {
             String id = getProjectId(uri);
             l.debug("Created project id=" + id);
             return id;
         }
         l.debug("Error creating project.");
         throw new GdcRestApiException("Error creating project.");
     }
 
     /**
      * Returns the create project JSON structure
      *
      * @param name        project name
      * @param desc        project description
      * @param templateUri project template uri
      * @param driver underlying database driver
      * @param accessToken access token
      * @return the create project JSON structure
      */
     private JSONObject getCreateProject(String name, String desc, String templateUri, String driver, String accessToken) {
         JSONObject meta = new JSONObject();
         meta.put("title", name);
         meta.put("summary", desc);
         if (templateUri != null && templateUri.length() > 0) {
             meta.put("projectTemplate", templateUri);
         }
         JSONObject content = new JSONObject();
         //content.put("state", "ENABLED");
         content.put("guidedNavigation", "1");
         if(driver != null && driver.length()>0) {
             content.put("driver", driver);
         }
         if(accessToken != null && accessToken.length()>0) {
             content.put("authorizationToken", accessToken);
         }
         JSONObject project = new JSONObject();
         project.put("meta", meta);
         project.put("content", content);
         JSONObject createStructure = new JSONObject();
         createStructure.put("project", project);
         return createStructure;
     }
 
     /**
      * Returns the project status
      *
      * @param id project ID
      * @return current project status
      */
     public String getProjectStatus(String id) {
         l.debug("Getting project status for project " + id);
 
         HttpMethod req = createGetMethod(getServerUrl() + PROJECTS_URI + "/" + id);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             JSONObject project = parsedResp.getJSONObject("project");
             JSONObject content = project.getJSONObject("content");
             String state = content.getString("state");
             return state;
         } catch (HttpMethodException e) {
             l.debug("The project id=" + id + " doesn't exists.");
             throw new GdcProjectAccessException("The project id=" + id + " doesn't exists.");
         } finally {
             req.releaseConnection();
         }
     }
 
     /**
      * Drops a GoodData project
      *
      * @param projectId project id
      * @throws GdcRestApiException
      */
     public void dropProject(String projectId) throws GdcRestApiException {
         l.debug("Dropping project id=" + projectId);
         DeleteMethod dropProjectDelete = createDeleteMethod(getServerUrl() + PROJECTS_URI + "/"+projectId);
         try {
             executeMethodOk(dropProjectDelete);
         } catch (HttpMethodException ex) {
             l.debug("Dropping project id=" + projectId + " failed.", ex);
             throw new GdcRestApiException("Dropping project id=" + projectId + " failed.", ex);
         } finally {
             dropProjectDelete.releaseConnection();
         }
         l.debug("Dropped project id=" + projectId);
     }
 
     /**
      * Retrieves the project id from the URI returned by the create project
      *
      * @param uri the create project URI
      * @return project id
      * @throws GdcRestApiException in case the project doesn't exist
      */
     protected String getProjectId(String uri) throws GdcRestApiException {
         l.debug("Getting project id by uri=" + uri);
         if (uri != null && uri.length() > 0) {
             String[] cs = uri.split("/");
             if (cs != null && cs.length > 0) {
                 l.debug("Got project id=" + cs[cs.length - 1] + " by uri=" + uri);
                 return cs[cs.length - 1];
             }
         }
         l.debug("Can't get project from " + uri);
         throw new GdcRestApiException("Can't get project from " + uri);
     }
 
     /**
      * Executes the MAQL and creates/modifies the project's LDM
      *
      * @param projectId the project's ID
      * @param maql      String with the MAQL statements
      * @return result String
      * @throws GdcRestApiException
      */
     public String[] executeMAQL(String projectId, String maql) throws GdcRestApiException {
         l.debug("Executing MAQL projectId=" + projectId + " MAQL:\n" + maql);
         PostMethod maqlPost = createPostMethod(getProjectMdUrl(projectId) + MAQL_EXEC_URI);
         JSONObject maqlStructure = getMAQLExecStructure(maql);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 maqlStructure.toString().getBytes()));
         maqlPost.setRequestEntity(request);
         String result = null;
         try {
             String response = executeMethodOk(maqlPost);
             JSONObject responseObject = JSONObject.fromObject(response);
             JSONArray uris = responseObject.getJSONArray("uris");
             return (String[]) uris.toArray(new String[]{""});
         } catch (HttpMethodException ex) {
             l.debug("MAQL execution: ", ex);
             throw new GdcRestApiException("MAQL execution: " + ex.getMessage(), ex);
         } finally {
             maqlPost.releaseConnection();
         }
     }
 
     /**
      * Executes the MAQL and creates/modifies the project's LDM asynchronously
      *
      * @param projectId the project's ID
      * @param maql      String with the MAQL statements
      * @return result String
      * @throws GdcRestApiException
      */
     public void executeMAQLAsync(String projectId, String maql) throws GdcRestApiException {
         l.debug("Executing async MAQL projectId=" + projectId + " MAQL:\n" + maql);
         PostMethod maqlPost = createPostMethod(getProjectMdUrl(projectId) + MAQL_ASYNC_EXEC_URI);
         JSONObject maqlStructure = getMAQLExecStructure(maql);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 maqlStructure.toString().getBytes()));
         maqlPost.setRequestEntity(request);
         String result = null;
         try {
             String response = executeMethodOk(maqlPost);
             JSONObject responseObject = JSONObject.fromObject(response);
             JSONArray uris = responseObject.getJSONArray("entries");
             String taskmanUri = "";
             for(Object ouri : uris) {
                 JSONObject uri = (JSONObject)ouri;
                 String category = uri.getString("category");
                 if(category.equals("tasks-status")) {
                     taskmanUri = uri.getString("link");
                 }
             }
             if(taskmanUri != null && taskmanUri.length()>0) {
                 l.debug("Checking async MAQL DDL execution status.");
                 String status = "";
                 while (!"OK".equalsIgnoreCase(status) && !"ERROR".equalsIgnoreCase(status) && !"WARNING".equalsIgnoreCase(status)) {
                     status = getTaskManStatus(taskmanUri);
                     l.debug("Async MAQL DDL status = " + status);
                     Thread.sleep(500);
                 }
                 l.info("Async MAQL DDL finished with status " + status);
                if (!("OK".equalsIgnoreCase(status) || "WARNING".equalsIgnoreCase(status))) {
                     throw new GdcRestApiException("Async MAQL execution failed with status "+status);
                 }
             }
         } catch (HttpMethodException ex) {
             l.debug("MAQL execution: ", ex);
             throw new GdcRestApiException("MAQL execution: " + ex.getMessage(), ex);
         }  catch (InterruptedException e) {
             throw new InternalErrorException(e);
         } finally {
             maqlPost.releaseConnection();
         }
     }
 
     public static class ProjectExportResult {
         private String taskUri;
         private String exportToken;
 
         public String getTaskUri() {
             return taskUri;
         }
 
         public void setTaskUri(String taskUri) {
             this.taskUri = taskUri;
         }
 
         public String getExportToken() {
             return exportToken;
         }
 
         public void setExportToken(String exportToken) {
             this.exportToken = exportToken;
         }
     }
 
     /**
      * Exports the project
      *
      * @param projectId       the project's ID
      * @param exportUsers     flag
      * @param exportData      flag
      * @param authorizedUsers list of authorized users
      * @return result the taskUri and the export token
      * @throws GdcRestApiException
      */
     public ProjectExportResult exportProject(String projectId, boolean exportUsers, boolean exportData, String[] authorizedUsers)
             throws GdcRestApiException {
         l.debug("Exporting project projectId=" + projectId + " users:" + exportUsers + " data:" + exportData + " authorized users:" +
                 authorizedUsers);
         PostMethod req = createPostMethod(getProjectMdUrl(projectId) + PROJECT_EXPORT_URI);
         JSONObject param = getProjectExportStructure(exportUsers, exportData, authorizedUsers);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 param.toString().getBytes()));
         req.setRequestEntity(request);
         ProjectExportResult result = null;
         try {
             String response = executeMethodOk(req);
             result = new ProjectExportResult();
             JSONObject responseObject = JSONObject.fromObject(response);
             JSONObject exportArtifact = responseObject.getJSONObject("exportArtifact");
             JSONObject status = exportArtifact.getJSONObject("status");
             result.setTaskUri(status.getString("uri"));
             result.setExportToken(exportArtifact.getString("token"));
             return result;
         } catch (HttpMethodException ex) {
             l.debug("Error exporting project", ex);
             throw new GdcRestApiException("Error exporting project", ex);
         } finally {
             req.releaseConnection();
         }
     }
 
     private JSONObject getProjectExportStructure(boolean exportUsers, boolean exportData, String[] authorizedUsers) {
         JSONObject param = new JSONObject();
         JSONObject exportProject = new JSONObject();
         exportProject.put("exportUsers", (exportUsers) ? (1) : (0));
         exportProject.put("exportData", (exportData) ? (1) : (0));
         if (authorizedUsers != null && authorizedUsers.length > 0) {
             JSONArray aUsers = new JSONArray();
             aUsers.addAll(Arrays.asList(authorizedUsers));
             exportProject.put("authorizedUsers", aUsers);
         }
         param.put("exportProject", exportProject);
         return param;
     }
 
     private GdcRole getRoleFromUri(String roleUri) {
         l.debug("Getting role from uri: " + roleUri);
         HttpMethod req = createGetMethod( getServerUrl() + roleUri);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             if (parsedResp == null || parsedResp.isNullObject() || parsedResp.isEmpty()) {
                 l.debug("Can't getRoleFromUri for uri " + roleUri + ". Invalid response.");
                 throw new GdcRestApiException("Can't getRoleFromUri for uri " + roleUri + ". Invalid response.");
             }
             return new GdcRole(parsedResp);
         } catch (HttpMethodException ex) {
             l.debug("Error getRoleFromUri.", ex);
             throw new GdcRestApiException("Error getRoleFromUri", ex);
         } finally {
             req.releaseConnection();
         }
     }
 
     private GdcUser getUserFromUri(String userUri) {
         l.debug("Getting user from uri: " + userUri);
         HttpMethod req = createGetMethod( getServerUrl() + userUri);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             if (parsedResp == null || parsedResp.isNullObject() || parsedResp.isEmpty()) {
                 l.debug("Can't getUserFromUri for uri " + userUri + ". Invalid response.");
                 throw new GdcRestApiException("Can't getUserFromUri for uri " + userUri + ". Invalid response.");
             }
             return new GdcUser(parsedResp);
         } catch (HttpMethodException ex) {
             l.debug("Error getUserFromUri.", ex);
             throw new GdcRestApiException("Error getUserFromUri", ex);
         } finally {
             req.releaseConnection();
         }
     }
 
     public static class GdcRole{
 
         private String name;
         private String identifier;
         private String uri;
 
         public GdcRole() {
         }
 
         public GdcRole(JSONObject role) {
             if (role == null || role.isEmpty() || role.isNullObject()) {
                 throw new GdcRestApiException("Can't extract role from JSON. The JSON is empty.");
             }
             JSONObject pr = role.getJSONObject("projectRole");
             if (pr == null || pr.isEmpty() || pr.isNullObject()) {
                 throw new GdcRestApiException("Can't extract role from JSON. No projectRole key in the JSON.");
             }
             JSONObject m = pr.getJSONObject("meta");
             if (m == null || m.isEmpty() || m.isNullObject()) {
                 throw new GdcRestApiException("Can't extract role from JSON. No meta key in the JSON.");
             }
             JSONObject l = pr.getJSONObject("links");
             if (l == null || l.isEmpty() || l.isNullObject()) {
                 throw new GdcRestApiException("Can't extract role from JSON. No links key in the JSON.");
             }
             String title = m.getString("title");
             if (title == null || title.trim().length() <= 0) {
                 throw new GdcRestApiException("Can't extract user from JSON. No email key in the JSON.");
             }
             this.setName(title);
             String u = l.getString("roleUsers");
             if (u == null || u.trim().length() <= 0) {
                 throw new GdcRestApiException("Can't extract role from JSON. No roleUsers key in the JSON.");
             }
             this.setUri(u.replace(USERS_URI,""));
             String i = m.getString("identifier");
             if (i == null || i.trim().length() <= 0) {
                 throw new GdcRestApiException("Can't extract user from JSON. No email key in the JSON.");
             }
             this.setIdentifier(i);
 
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public String getIdentifier() {
             return identifier;
         }
 
         public void setIdentifier(String identifier) {
             this.identifier = identifier;
         }
 
         public String getUri() {
             return uri;
         }
 
         public void setUri(String uri) {
             this.uri = uri;
         }
 
         public boolean validate() {
             if (getName() != null && getIdentifier().length() > 0 && getUri() != null) // email is not mandatory
                 return true;
             return false;
         }
     }
 
 
     public static class GdcUser {
         private String login;
         private String email;
         private String licence;
         private String firstName;
         private String lastName;
         private String companyName;
         private String position;
         private String timezone;
         private String country;
         private String phoneNumber;
         private String password;
         private String verifyPassword;
         private String ssoProvider;
         private String status;
         private String uri;
 
 
         public GdcUser() {
         }
 
         public GdcUser(JSONObject user) {
             if (user == null || user.isEmpty() || user.isNullObject()) {
                 throw new GdcRestApiException("Can't extract user from JSON. The JSON is empty.");
             }
             JSONObject u = user.getJSONObject("user");
             if (u == null || u.isEmpty() || u.isNullObject()) {
                 throw new GdcRestApiException("Can't extract user from JSON. No user key in the JSON.");
             }
             JSONObject c = u.getJSONObject("content");
             if (c == null || c.isEmpty() || c.isNullObject()) {
                 throw new GdcRestApiException("Can't extract user from JSON. No content key in the JSON.");
             }
             String v = c.getString("email");
             if (v == null || v.trim().length() <= 0) {
                 throw new GdcRestApiException("Can't extract user from JSON. No email key in the JSON.");
             }
             this.setLogin(v);
             v = c.getString("firstname");
             if (v != null && v.trim().length() > 0) {
                 this.setFirstName(v);
             }
             v = c.getString("lastname");
             if (v != null && v.trim().length() > 0) {
                 this.setLastName(v);
             }
             v = c.getString("email");
             if (v != null && v.trim().length() > 0) {
                 this.setEmail(v);
             }
             v = c.getString("phonenumber");
             if (v != null && v.trim().length() > 0) {
                 this.setPhoneNumber(v);
             }
             v = c.getString("status");
             if (v != null && v.trim().length() > 0) {
                 this.setStatus(v);
             }
             JSONObject l = u.getJSONObject("links");
             if (l == null || l.isEmpty() || l.isNullObject()) {
                 throw new GdcRestApiException("Can't extract user from JSON. No links key in the JSON.");
             }
             v = l.getString("self");
             if (v == null || v.trim().length() <= 0) {
                 throw new GdcRestApiException("Can't extract user from JSON. No self key in the JSON.");
             }
             this.setUri(v);
         }
 
         public boolean validate() {
             if (getLogin() != null && getLogin().length() > 0 && getPassword() != null
                     && getPassword().length() > 0 && getVerifyPassword() != null
                     && getVerifyPassword().length() > 0 && getFirstName() != null
                     && getFirstName().length() > 0 && getLastName() != null
                     && getLastName().length() > 0) // email is not mandatory
                 return true;
             return false;
         }
 
         public String getLogin() {
             return login;
         }
 
         public void setLogin(String login) {
             this.login = login;
         }
 
         public String getUri() {
             return uri;
         }
 
         public void setUri(String u) {
             this.uri = u;
         }
 
         public String getStatus() {
             return status;
         }
 
         public void setStatus(String s) {
             this.status = s;
         }
 
         public String getLicence() {
             return licence;
         }
 
         public void setLicence(String licence) {
             this.licence = licence;
         }
 
         public String getFirstName() {
             return firstName;
         }
 
         public void setFirstName(String firstName) {
             this.firstName = firstName;
         }
 
         public String getLastName() {
             return lastName;
         }
 
         public void setLastName(String lastName) {
             this.lastName = lastName;
         }
 
         public String getCompanyName() {
             return companyName;
         }
 
         public void setCompanyName(String companyName) {
             this.companyName = companyName;
         }
 
         public String getPosition() {
             return position;
         }
 
         public void setPosition(String position) {
             this.position = position;
         }
 
         public String getTimezone() {
             return timezone;
         }
 
         public void setTimezone(String timezone) {
             this.timezone = timezone;
         }
 
         public String getCountry() {
             return country;
         }
 
         public void setCountry(String country) {
             this.country = country;
         }
 
         public String getPhoneNumber() {
             return phoneNumber;
         }
 
         public void setPhoneNumber(String phoneNumber) {
             this.phoneNumber = phoneNumber;
         }
 
         public String getPassword() {
             return password;
         }
 
         public void setPassword(String password) {
             this.password = password;
         }
 
         public String getVerifyPassword() {
             return verifyPassword;
         }
 
         public void setVerifyPassword(String verifyPassword) {
             this.verifyPassword = verifyPassword;
         }
 
 
         public String getSsoProvider() {
             return ssoProvider;
         }
 
         public void setSsoProvider(String ssoProvider) {
             this.ssoProvider = ssoProvider;
         }
 
 		public String getEmail() {
 			return email;
 		}
 
 		public void setEmail(String email) {
 			this.email = email;
 		}
 		
 		@Override
         public String toString() {
             return "DWGdcUser [getLogin()=" + getLogin() + ", getUri()=" + getUri() + ", getStatus()=" + getStatus()
                 + ", getLicence()=" + getLicence() + ", getFirstName()=" + getFirstName() + ", getLastName()="
                 + getLastName() + ", getCompanyName()=" + getCompanyName() + ", getPosition()=" + getPosition()
                 + ", getTimezone()=" + getTimezone() + ", getCountry()=" + getCountry() + ", getPhoneNumber()="
                 + getPhoneNumber() + ", getPassword()=" + getPassword() + ", getVerifyPassword()="
                 + getVerifyPassword() + ", getEmail()=" + getEmail() + "," +  " getSsoProvider()=" + getSsoProvider() + "]";
         }
 
     }
 
     /**
      * Create a new user
      *
      * @param domain the domain where the user is going to be created
      * @param user   new user data
      * @return the new user's URI
      * @throws GdcRestApiException
      */
     public String createUser(String domain, GdcUser user)
             throws GdcRestApiException {
         if (user != null && user.validate()) {
             l.debug("Creating new user " + user.getLogin() + " in domain " + domain);
             PostMethod req = createPostMethod(getServerUrl() + DOMAIN_URI + "/" + domain + DOMAIN_USERS_SUFFIX);
             JSONObject param = getCreateUserStructure(user);
             InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                     param.toString().getBytes()));
             req.setRequestEntity(request);
             String result = null;
             try {
                 String response = executeMethodOk(req);
                 JSONObject responseObject = JSONObject.fromObject(response);
                 result = responseObject.getString("uri");
                 return result;
             } catch (HttpMethodException ex) {
                 l.debug("Error creating user ", ex);
                 throw new GdcRestApiException("Error creating user ", ex);
             } finally {
                 req.releaseConnection();
             }
         } else {
             throw new InvalidParameterException("The new user must contain valid login, firstName, lastName, and password fields.");
         }
     }
 
     private JSONObject getCreateUserStructure(GdcUser user) {
         JSONObject param = new JSONObject();
         JSONObject accountSetting = new JSONObject();
         accountSetting.put("login", user.getLogin());
         accountSetting.put("password", user.getPassword());
         accountSetting.put("verifyPassword", user.getVerifyPassword());
         accountSetting.put("firstName", user.getFirstName());
         accountSetting.put("lastName", user.getLastName());
 
         if (user.getCompanyName() != null && user.getCompanyName().length() > 0)
             accountSetting.put("companyName", user.getCompanyName());
         if (user.getPosition() != null && user.getPosition().length() > 0)
             accountSetting.put("position", user.getPosition());
         if (user.getCountry() != null && user.getCountry().length() > 0)
             accountSetting.put("country", user.getCountry());
         if (user.getTimezone() != null && user.getTimezone().length() > 0)
             accountSetting.put("timezone", user.getTimezone());
         else
             accountSetting.put("timezone", null);
         if (user.getPhoneNumber() != null && user.getPhoneNumber().length() > 0)
             accountSetting.put("phoneNumber", user.getPhoneNumber());
         if (user.getSsoProvider() != null && user.getSsoProvider().length() > 0)
             accountSetting.put("ssoProvider", user.getSsoProvider());
         if (user.getEmail() != null && user.getEmail().length() > 0)
             accountSetting.put("email", user.getEmail());
         param.put("accountSetting", accountSetting);
         return param;
     }
 
     private String getRoleUri(String projectId, String role) {
 
         String roleUri = null;
 
         // for backward compatibility
 
         if(ROLES.containsKey(role.toUpperCase()))  {
             role = ROLES.get(role.toUpperCase());
         }
 
         List<GdcRole> roles = getProjectRoles(projectId);
         for(GdcRole r : roles) {
             String identifier = r.getIdentifier();
             if(identifier.equalsIgnoreCase(role)) {
                 roleUri = r.getUri();
             }
         }
         return roleUri;
     }
 
     /**
      * Create a new user
      *
      * @param projectId project ID
      * @param uris      user URIs
      * @param role      user's role
      * @return the new user's URI
      * @throws GdcRestApiException
      */
     public void addUsersToProject(String projectId, List<String> uris, String role)
             throws GdcRestApiException {
 
         l.debug("Adding users " + uris + " to project " + projectId + " in role "+ role);
         String projectsUrl = getProjectUrl(projectId);
 
         String roleUri = getRoleUri(projectId, role);
 
         addUsersToProjectWithRoleUri(projectId, uris, roleUri);
     }
 
     public void addUsersToProjectWithRoleUri(String projectId, List<String> uris, String roleUri)
             throws GdcRestApiException {
 
         l.debug("Adding users " + uris + " to project " + projectId + " with roleUri "+ roleUri);
         String projectsUrl = getProjectUrl(projectId);
 
         PostMethod req = createPostMethod(projectsUrl + PROJECT_USERS_SUFFIX);
         JSONObject param = getAddUsersToProjectStructure(uris, roleUri);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 param.toString().getBytes()));
         req.setRequestEntity(request);
         String result = null;
         try {
             String response = executeMethodOk(req);
             JSONObject responseObject = JSONObject.fromObject(response);
             JSONObject projectUsersUpdateResult = responseObject.getJSONObject("projectUsersUpdateResult");
             JSONArray failed = projectUsersUpdateResult.getJSONArray("failed");
             if (!failed.isEmpty()) {
                 String errMsg = "Following users can't be added to the project:";
                 for (Object uri : failed.toArray()) {
                     errMsg += " " + uris.toString();
                 }
                 l.debug(errMsg);
                 throw new GdcRestApiException(errMsg);
             }
             //JSONArray successful = projectUsersUpdateResult.getJSONArray("successful");
         } catch (HttpMethodException ex) {
             l.debug("Error adding users " + uris + " to project", ex);
             throw new GdcRestApiException("Error adding users " + uris + " to project ", ex);
         } finally {
             req.releaseConnection();
         }
     }
 
     private JSONObject getAddUsersToProjectStructure(List<String> uris, String roleUri) {
         JSONObject param = new JSONObject();
         JSONArray users = new JSONArray();
         JSONArray roles = null;
         if (roleUri != null && roleUri.trim().length() > 0) {
             roles = new JSONArray();
             roles.add(roleUri);
         }
         for (String uri : uris) {
             JSONObject user = new JSONObject();
             JSONObject content = new JSONObject();
             if (roles != null)
                 content.put("userRoles", roles);
             content.put("status", "ENABLED");
             user.put("content", content);
             JSONObject links = new JSONObject();
             links.put("self", uri);
             user.put("links", links);
             JSONObject item = new JSONObject();
             item.put("user", user);
             users.add(item);
         }
         param.put("users", users);
         return param;
     }
 
     /**
      * Disables a user in project
      *
      * @param projectId project ID
      * @param uris      user URIs
      * @throws GdcRestApiException
      */
     public void disableUsersInProject(String projectId, List<String> uris)
             throws GdcRestApiException {
 
         l.debug("Disabling users " + uris + " in project " + projectId);
         String projectsUrl = getProjectUrl(projectId);
 
         PostMethod req = createPostMethod(projectsUrl + PROJECT_USERS_SUFFIX);
         JSONObject param = getDisableUsersInProjectStructure(uris);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 param.toString().getBytes()));
         req.setRequestEntity(request);
         String result = null;
         try {
             String response = executeMethodOk(req);
             JSONObject responseObject = JSONObject.fromObject(response);
             JSONObject projectUsersUpdateResult = responseObject.getJSONObject("projectUsersUpdateResult");
             JSONArray failed = projectUsersUpdateResult.getJSONArray("failed");
             if (!failed.isEmpty()) {
                 String errMsg = "Following users can't be disabled in the project:";
                 for (Object uri : failed.toArray()) {
                     errMsg += " " + uris.toString();
                 }
                 l.debug(errMsg);
                 throw new GdcRestApiException(errMsg);
             }
             //JSONArray successful = projectUsersUpdateResult.getJSONArray("successful");
         } catch (HttpMethodException ex) {
             l.debug("Error disabling users " + uris + " in project", ex);
             throw new GdcRestApiException("Error disabling users " + uris + " in project ", ex);
         } finally {
             req.releaseConnection();
         }
     }
 
     private JSONObject getDisableUsersInProjectStructure(List<String> uris) {
         JSONObject param = new JSONObject();
         JSONArray users = new JSONArray();
         for (String uri : uris) {
             JSONObject user = new JSONObject();
             JSONObject content = new JSONObject();
             content.put("status", "DISABLED");
             user.put("content", content);
             JSONObject links = new JSONObject();
             links.put("self", uri);
             user.put("links", links);
             JSONObject item = new JSONObject();
             item.put("user", user);
             users.add(item);
         }
         param.put("users", users);
         return param;
     }
 
     /**
      * Returns the selected project's roles
      *
      * @param pid             project ID
      * @return array of the project's users
      */
     public ArrayList<GdcRole> getProjectRoles(String pid) {
         ArrayList<GdcRole> ret = new ArrayList<GdcRole>();
         l.debug("Executing getProjectRoles for project id=" + pid);
         HttpMethod req = createGetMethod(getProjectUrl(pid) + ROLES_URI);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             if (parsedResp == null || parsedResp.isNullObject() || parsedResp.isEmpty()) {
                 l.debug("Can't getProjectRoles for project id=" + pid + ". Invalid response.");
                 throw new GdcRestApiException("Can't getProjectRoles for project id=" + pid + ". Invalid response.");
             }
             JSONObject projectRoles = parsedResp.getJSONObject("projectRoles");
             if (projectRoles == null || projectRoles.isNullObject() || projectRoles.isEmpty()) {
                 l.debug("Can't getProjectRoles for project id=" + pid + ". No projectRoles key in the response.");
                 throw new GdcRestApiException("Can't getProjectRoles for project id=" + pid + ". No projectRoles key in the response.");
             }
             JSONArray roles = projectRoles.getJSONArray("roles");
             if (roles == null) {
                 l.debug("Can't getRoleUsers. No getProjectRoles key in the response.");
                 throw new GdcRestApiException("Can't getProjectRoles. No roles key in the response.");
             }
             for (Object o : roles) {
                 String role = (String) o;
                 GdcRole g = getRoleFromUri(role);
                 ret.add(g);
             }
             return ret;
         } finally {
             req.releaseConnection();
         }
     }
 
 
 
     /**
      * Returns the selected project's roles
      *
      * @return array of the project's users
      */
     public ArrayList<String> getRoleUsers(GdcRole role, boolean activeUsersOnly) {
         ArrayList<String> ret = new ArrayList<String>();
         if(role == null || role.getIdentifier() == null || role.getIdentifier().length() == 0 || role.getUri() == null
                 || role.getUri().length() == 0 || role.getName() == null || role.getName().length() == 0) {
             l.debug("Can't getRoleUsers . Invalid role object passed.");
             throw new GdcRestApiException("Can't getRoleUsers. Invalid role object passed.");
         }
         l.debug("Executing getRoleUsers for role "+role.getIdentifier());
         HttpMethod req = createGetMethod(getServerUrl() + role.getUri() + USERS_URI);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             if (parsedResp == null || parsedResp.isNullObject() || parsedResp.isEmpty()) {
                 l.debug("Can't getRoleUsers. Invalid response.");
                 throw new GdcRestApiException("Can't getRoleUsers. Invalid response.");
             }
             JSONObject associatedUsers = parsedResp.getJSONObject("associatedUsers");
             if (associatedUsers == null || associatedUsers.isNullObject() || associatedUsers.isEmpty()) {
                 l.debug("Can't getRoleUsers. Invalid response. No associatedUsers key.");
                 throw new GdcRestApiException("Can't getRoleUsers. Invalid response. No associatedUsers key.");
             }
             JSONArray users = associatedUsers.getJSONArray("users");
             if (users == null) {
                 l.debug("Can't getRoleUsers. No users key in the response.");
                 throw new GdcRestApiException("Can't getRoleUsers. No users key in the response.");
             }
             for (Object o : users) {
                 String user = (String) o;
                 ret.add(user);
             }
             return ret;
         } finally {
             req.releaseConnection();
         }
     }
 
     /**
      * Returns the selected project's users
      *
      * @param pid             project ID
      * @param activeUsersOnly lists only active users
      * @return array of the project's users
      */
     public ArrayList<GdcUser> getProjectUsers(String pid, boolean activeUsersOnly) {
         ArrayList<GdcUser> ret = new ArrayList<GdcUser>();
         l.debug("Executing getProjectUsers for project id=" + pid);
         HttpMethod req = createGetMethod(getProjectUrl(pid) + PROJECT_USERS_SUFFIX);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             if (parsedResp == null || parsedResp.isNullObject() || parsedResp.isEmpty()) {
                 l.debug("Can't getProjectUsers for project id=" + pid + ". Invalid response.");
                 throw new GdcRestApiException("Can't getProjectUsers for project id=" + pid + ". Invalid response.");
             }
             JSONArray users = parsedResp.getJSONArray("users");
             if (users == null) {
                 l.debug("Can't getProjectUsers for project id=" + pid + ". No users key in the response.");
                 throw new GdcRestApiException("Can't getProjectUsers for project id=" + pid + ". No users key in the response.");
             }
             for (Object o : users) {
                 JSONObject user = (JSONObject) o;
                 GdcUser g = new GdcUser(user);
                 if ((activeUsersOnly && "ENABLED".equalsIgnoreCase(g.getStatus())) || (!activeUsersOnly)) {
                     ret.add(g);
                 }
             }
             return ret;
         } finally {
             req.releaseConnection();
         }
     }
 
 
     /**
      * Imports the project
      *
      * @param projectId the project's ID
      * @param token     export token
      * @return result the taskUri
      * @throws GdcRestApiException
      */
     public String importProject(String projectId, String token)
             throws GdcRestApiException {
         l.debug("Importing project projectId=" + projectId + " token:" + token);
         PostMethod req = createPostMethod(getProjectMdUrl(projectId) + PROJECT_IMPORT_URI);
         JSONObject param = getImportProjectStructure(token);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 param.toString().getBytes()));
         req.setRequestEntity(request);
         String result = null;
         try {
             String response = executeMethodOk(req);
             JSONObject responseObject = JSONObject.fromObject(response);
             result = responseObject.getString("uri");
             return result;
         } catch (HttpMethodException ex) {
             l.debug("Error importing project", ex);
             throw new GdcRestApiException("Error importing project", ex);
         } finally {
             req.releaseConnection();
         }
     }
 
     private JSONObject getImportProjectStructure(String token) {
         JSONObject param = new JSONObject();
         JSONObject importProject = new JSONObject();
         importProject.put("token", token);
         param.put("importProject", importProject);
         return param;
     }
 
 
     /**
      * Imports a MD object to the project
      *
      * @param projectId the project's ID
      * @param token     export token
      * @param overwrite overwrite existing objects
      * @param updateLDM update LDM names, descriptions and tags
      * @return result the taskUri
      * @throws GdcRestApiException
      */
     public String importMD(String projectId, String token, boolean overwrite, boolean updateLDM)
             throws GdcRestApiException {
         l.debug("Importing metadata objects for projectId=" + projectId + " token:" + token);
         PostMethod req = createPostMethod(getProjectMdUrl(projectId) + PROJECT_PARTIAL_IMPORT_URI);
         JSONObject param = getMDImportStructure(token, overwrite, updateLDM);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 param.toString().getBytes()));
         req.setRequestEntity(request);
         String result = null;
         try {
             String response = executeMethodOk(req);
             JSONObject responseObject = JSONObject.fromObject(response);
             result = responseObject.getString("uri");
             return result;
         } catch (HttpMethodException ex) {
             l.debug("Error importing metadata objects for projectId=" + projectId + " token:" + token, ex);
             throw new GdcRestApiException("Error importing metadata objects for projectId=" + projectId + " token:" + token, ex);
         } finally {
             req.releaseConnection();
         }
     }
 
     private JSONObject getMDImportStructure(String token, boolean overwrite, boolean updateLDM) {
         JSONObject param = new JSONObject();
         JSONObject importMD = new JSONObject();
         importMD.put("token", token);
         importMD.put("overwriteNewer", (overwrite) ? (1) : (0));
         importMD.put("updateLDMObjects", (updateLDM) ? (1) : (0));
         param.put("partialMDImport", importMD);
         return param;
     }
 
     /**
      * Exports selected MD object with dependencies from the project
      *
      * @param projectId the project's ID
      * @param ids       - list of the exported MD objects IDs
      * @return result the taskUri and the export token
      * @throws GdcRestApiException
      */
     public ProjectExportResult exportMD(String projectId, List<Integer> ids)
             throws GdcRestApiException {
         l.debug("Exporting metadata objects with IDs " + ids + " from project " + projectId);
         PostMethod req = createPostMethod(getProjectMdUrl(projectId) + PROJECT_PARTIAL_EXPORT_URI);
         JSONObject param = getMDExportStructure(projectId, ids);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 param.toString().getBytes()));
         req.setRequestEntity(request);
         ProjectExportResult result = null;
         try {
             String response = executeMethodOk(req);
             result = new ProjectExportResult();
             JSONObject responseObject = JSONObject.fromObject(response);
             JSONObject exportArtifact = responseObject.getJSONObject("partialMDArtifact");
             JSONObject status = exportArtifact.getJSONObject("status");
             result.setTaskUri(status.getString("uri"));
             result.setExportToken(exportArtifact.getString("token"));
             return result;
         } catch (HttpMethodException ex) {
             l.debug("Error exporting metadata objects with IDs " + ids + " from project " + projectId, ex);
             throw new GdcRestApiException("Error exporting metadata objects with IDs " + ids + " from project " + projectId, ex);
         } finally {
             req.releaseConnection();
         }
     }
 
     private JSONObject getMDExportStructure(String projectId, List<Integer> ids) {
         JSONObject param = new JSONObject();
         String puri = "/gdc/md/" + projectId;
         JSONObject partialMDExport = new JSONObject();
         JSONArray uris = new JSONArray();
         for (Integer id : ids) {
             uris.add(puri + "/obj/" + id);
         }
         partialMDExport.put("uris", uris);
         param.put("partialMDExport", partialMDExport);
         return param;
     }
 
     /**
      * Checks if the migration is finished
      *
      * @param link the link returned from the start loading
      * @return the loading status
      */
     public String getMigrationStatus(String link) throws HttpMethodException {
         l.debug("Getting project migration status uri=" + link);
         HttpMethod ptm = createGetMethod(getServerUrl() + link);
         try {
             String response = executeMethodOk(ptm);
             JSONObject task = JSONObject.fromObject(response);
             JSONObject state = task.getJSONObject("taskState");
             if (state != null && !state.isNullObject() && !state.isEmpty()) {
                 String status = state.getString("status");
                 l.debug("Migration status=" + status);
                 return status;
             } else {
                 l.debug("No taskState structure in the migration status!");
                 throw new GdcRestApiException("No taskState structure in the migration status!");
             }
         } finally {
             ptm.releaseConnection();
         }
     }
 
     /**
      * Executes the MAQL and creates/modifies the project's LDM
      *
      * @param projectId the project's ID
      * @param maql      String with the MAQL statements
      * @return result String
      * @throws GdcRestApiException
      */
     public String executeDML(String projectId, String maql) throws GdcRestApiException {
         l.debug("Executing MAQL DML projectId=" + projectId + " MAQL DML:\n" + maql);
         PostMethod maqlPost = createPostMethod(getProjectMdUrl(projectId) + DML_EXEC_URI);
         JSONObject maqlStructure = getMAQLExecStructure(maql);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 maqlStructure.toString().getBytes()));
         maqlPost.setRequestEntity(request);
         String result = null;
         try {
             String response = executeMethodOk(maqlPost);
             JSONObject responseObject = JSONObject.fromObject(response);
             String uris = responseObject.getString("uri");
             return uris;
         } catch (HttpMethodException ex) {
             l.debug("MAQL DML execution: ", ex);
             throw new GdcRestApiException("MAQL DML execution: ", ex);
         } finally {
             maqlPost.releaseConnection();
         }
     }
 
 
     /**
      * Returns the pull API JSON structure
      *
      * @param maql String with the MAQL statements
      * @return the MAQL API JSON structure
      */
     private JSONObject getMAQLExecStructure(String maql) {
         JSONObject maqlStructure = new JSONObject();
         JSONObject maqlObj = new JSONObject();
         maqlObj.put("maql", maql);
         maqlStructure.put("manage", maqlObj);
         return maqlStructure;
     }
 
     protected String executeMethodOk(HttpMethod method) throws HttpMethodException {
         return executeMethodOk(method, true);
     }
 
     protected String executeMethodOk(HttpMethod method, boolean reloginOn401) throws HttpMethodException {
         return executeMethodOk(method, reloginOn401, 16);
     }
 
     /**
      * Executes HttpMethod and test if the response if 200(OK)
      *
      * @param method the HTTP method
      * @return response body as String
      * @throws HttpMethodException
      */
     private String executeMethodOk(HttpMethod method, boolean reloginOn401, int retries) throws HttpMethodException {
         try {
             executeMethodOkOnly(method, reloginOn401, retries);
             return method.getResponseBodyAsString();
         } catch (IOException e) {
             l.debug("Error invoking GoodData REST API.", e);
             throw new HttpMethodException("Error invoking GoodData REST API.", e);
         }
     }
 
     private void executeMethodOkOnly(HttpMethod method) throws HttpMethodException {
         executeMethodOkOnly(method, true);
     }
 
     private void executeMethodOkOnly(HttpMethod method, boolean reloginOn401) throws HttpMethodException {
         executeMethodOk(method, reloginOn401, 16);
     }
 
     /**
      * Executes HttpMethod and test if the response if 200(OK)
      *
      * @param method the HTTP method
      * @return response as Stream
      * @throws HttpMethodException
      */
     private void executeMethodOkOnly(HttpMethod method, boolean reloginOn401, int retries) throws HttpMethodException, IOException {
         try {
             client.executeMethod(method);
 
             /* HttpClient is rather unsupportive when it comes to robust interpreting
              * of response classes; which is mandated by RFC and extensively used in
              * GoodData API. Let us grok the classes ourselves. */
 
             /* 2xx success class */
             if (method.getStatusCode() == HttpStatus.SC_CREATED) {
                 return;
             } else if (method.getStatusCode() == HttpStatus.SC_ACCEPTED) {
                 throw new HttpMethodNotFinishedYetException(method.getResponseBodyAsString());
             } else if (method.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                 return;
             } else if (method.getStatusCode() >= HttpStatus.SC_OK
                     && method.getStatusCode() < HttpStatus.SC_BAD_REQUEST) {
                 return;
 
                 /* 4xx user errors and
             * 5xx backend trouble */
             } else if (method.getStatusCode() == HttpStatus.SC_UNAUTHORIZED && reloginOn401) {
                 // refresh the temporary token
                 setTokenCookie();
                 executeMethodOkOnly(method, false, retries);
                 return;
             } else if (method.getStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE && retries-- > 0
                     && method.getResponseHeader("Retry-After") != null) {
                 /* This is recommended by RFC 2616 and should probably be dealt with by the
                  * client library. May god have mercy with it. */
                 int timeout = Integer.parseInt(method.getResponseHeader("Retry-After").getValue());
                 l.debug("Remote asked us to retry after " + timeout + " seconds, sleeping.");
                 l.debug(retries + " more retries");
                 try {
                     Thread.currentThread().sleep(1000 * timeout);
                 } catch (java.lang.InterruptedException e) {
                 }
                 executeMethodOkOnly(method, false, retries);
                 return;
             } else if (method.getStatusCode() == HttpStatus.SC_GONE) {
                 throw new GdcProjectAccessException("Invalid project.");
             } else if (method.getStatusCode() >= HttpStatus.SC_BAD_REQUEST
                     && method.getStatusCode() < 600) {
                 throw new HttpMethodException(method);
 
                 /* 1xx informational responses class and
      * 3xx redirects should not get past the client library internals. */
             } else {
                 throw new HttpMethodException("Unsupported HTTP status received from remote: " +
                         method.getStatusCode());
             }
 
         } catch (HttpException e) {
             l.debug("Error invoking GoodData REST API.", e);
             throw new HttpMethodException("Error invoking GoodData REST API.", e);
         }
     }
 
 
     /**
      * Returns the data interfaces URI
      *
      * @param projectId project ID
      * @return SLI collection URI
      */
     public String getSLIsUri(String projectId) {
         return getProjectMdUrl(projectId) + DATA_INTERFACES_URI;
     }
 
     /**
      * Returns the SLI URI
      *
      * @param sliId     SLI ID
      * @param projectId project ID
      * @return DLI URI
      */
     public String getSLIUri(String sliId, String projectId) {
         return getProjectMdUrl(projectId) + DATA_INTERFACES_URI + "/" + sliId + SLI_DESCRIPTOR_URI;
     }
 
 
     protected String getServerUrl() {
         return config.getUrl();
     }
 
     /**
      * Constructs project's metadata uri
      *
      * @param projectId project ID
      */
     protected String getProjectMdUrl(String projectId) {
         return getServerUrl() + MD_URI + projectId;
     }
 
     /**
      * Constructs project's projects uri
      *
      * @param projectId project ID
      */
     protected String getProjectUrl(String projectId) {
         return getServerUrl() + PROJECTS_URI + "/" + projectId;
     }
 
     /**
      * Gets the project ID from the project URI
      *
      * @param projectUri project URI
      * @return the project id
      */
     public String getProjectIdFromUri(String projectUri) {
         String[] cmpnts = projectUri.split("/");
         if (cmpnts != null && cmpnts.length > 0) {
             String id = cmpnts[cmpnts.length - 1];
             return id;
         } else
             throw new GdcRestApiException("Invalid project uri structure uri=" + projectUri);
     }
 
     /**
      * Gets the project delete URI from the project id
      *
      * @param projectId project ID
      * @return the project delete URI
      */
     public String getProjectDeleteUri(String projectId) {
         return PROJECTS_URI + "/" + projectId;
     }
 
     /**
      * Profile getter
      *
      * @return the profile of the currently logged user
      */
     protected JSONObject getProfile() {
         return profile;
     }
 
     /**
      * Invites a new user to a project
      *
      * @param projectId project ID
      * @param eMail     invited user e-mail
      * @param message   invitation message
      */
     public void inviteUser(String projectId, String eMail, String message) {
         this.inviteUser(projectId, eMail, message, null);
     }
 
     /**
      * Invites a new user to a project
      *
      * @param projectId project ID
      * @param eMail     invited user e-mail
      * @param message   invitation message
      */
     public void inviteUser(String projectId, String eMail, String message, String role) {
         l.debug("Executing inviteUser projectId=" + projectId + " e-mail=" + eMail + " message=" + message);
         PostMethod invitePost = createPostMethod(getServerUrl() + getProjectDeleteUri(projectId) + INVITATION_URI);
         JSONObject inviteStructure = getInviteStructure(projectId, eMail, message, role);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 inviteStructure.toString().getBytes()));
         invitePost.setRequestEntity(request);
         try {
             executeMethodOk(invitePost);
         } catch (HttpMethodException ex) {
             l.debug("Failed executing inviteUser projectId=" + projectId + " e-mail=" + eMail + " message=" + message);
             throw new GdcRestApiException("Failed executing inviteUser projectId=" + projectId + " e-mail=" + eMail + " message=" + message, ex);
         } finally {
             invitePost.releaseConnection();
         }
     }
 
     /**
      * Creates a new invitation structure
      *
      * @param pid   project id
      * @param eMail e-mail
      * @param msg   invitation message
      * @return the new invitation structure
      */
     private JSONObject getInviteStructure(String pid, String eMail, String msg, String role) {
         JSONObject content = new JSONObject();
         content.put("firstname", "");
         content.put("lastname", "");
         content.put("email", eMail);
         if (role != null && role.length() > 0) {
             String roleUri = getRoleUri(pid, role);
             if (roleUri == null)
                 throw new InvalidParameterException("The role '" + role + "' is not recognized by the GoodData platform.");
             content.put("role", roleUri);
         }
         JSONObject action = new JSONObject();
         action.put("setMessage", msg);
         content.put("action", action);
         JSONObject invitation = new JSONObject();
         invitation.put("content", content);
         JSONObject invitations = new JSONObject();
         JSONArray ia = new JSONArray();
         JSONObject inve = new JSONObject();
         inve.put("invitation", invitation);
         ia.add(inve);
         invitations.put("invitations", ia);
         return invitations;
     }
 
     /**
      * Converst MD identifier to uri
      *
      * @param projectId   project ID
      * @param identifiers MD object identifiers
      * @return map identifier:uri
      */
     public Map<String, String> identifierToUri(String projectId, String[] identifiers) {
         l.debug("Executing identifierToUri identifier=" + identifiers);
         Map<String, String> result = new HashMap<String, String>();
         PostMethod p = createPostMethod(getProjectMdUrl(projectId) + IDENTIFIER_URI);
         JSONObject is = getIdentifiersStructure(identifiers);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 is.toString().getBytes()));
         p.setRequestEntity(request);
         try {
             String resp = executeMethodOk(p);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             JSONArray idents = parsedResp.getJSONArray("identifiers");
             if (idents != null && !idents.isEmpty()) {
                 for (int i = 0; i < idents.size(); i++) {
                     JSONObject ident = idents.getJSONObject(i);
                     result.put(ident.getString("identifier"), ident.getString("uri"));
                 }
 
             }
 
         } catch (HttpMethodException ex) {
             l.debug("Failed executing identifierToUri identifier=" + identifiers);
             throw new GdcRestApiException("Failed executing identifierToUri identifier=" + identifiers, ex);
         } finally {
             p.releaseConnection();
         }
         return result;
     }
 
 
     /**
      * Creates a new identifiers structure
      *
      * @param identifiers MD object identifier
      * @return the new identifiers structure
      */
     private JSONObject getIdentifiersStructure(String[] identifiers) {
         JSONObject identifierToUri = new JSONObject();
         JSONArray ids = new JSONArray();
         for (int i = 0; i < identifiers.length; i++) {
             ids.add(identifiers[i]);
         }
         identifierToUri.put("identifierToUri", ids);
         return identifierToUri;
     }
 
 
     /**
      * Retrieves a metadata object definition by Uri
      *
      * @param objectUri object uri
      * @return the object to get
      */
     public JSONObject getObjectByUri(String objectUri) {
         l.debug("Executing getObjectByUri uri=" + objectUri);
         HttpMethod req = createGetMethod(getServerUrl() + objectUri);
         try {
             String resp = executeMethodOk(req);
             // workaround for a possible mess in MAQL source and missing charset in /obj response
             resp = resp.replace("\\\\_", " ").replace("\u00A0", " ");
             JSONObject parsedResp = JSONObject.fromObject(resp);
             if (parsedResp.isNullObject()) {
                 l.debug("Can't getObjectByUri object uri=" + objectUri);
                 throw new GdcRestApiException("Can't getObjectByUri object uri=" + objectUri);
             }
             return parsedResp;
         } finally {
             req.releaseConnection();
         }
     }
 
     /**
      * Retrieves a metadata object definition
      *
      * @param objectUri object uri
      * @return the object to get
      */
     public MetadataObject getMetadataObject(String objectUri) {
         l.debug("Executing getMetadataObject uri=" + objectUri);
         MetadataObject o = new MetadataObject(getObjectByUri(objectUri));
         return o;
     }
 
     /**
      * Retrieves a metadata object definition
      *
      * @param projectId project id (hash)
      * @param objectId  object id (integer)
      * @return the object to get
      */
     public MetadataObject getMetadataObject(String projectId, int objectId) {
         l.debug("Executing getMetadataObject id=" + objectId + " on project id=" + projectId);
         return getMetadataObject(MD_URI + projectId + OBJ_URI + "/" + objectId);
     }
 
     /**
      * Retrieves a metadata object definition
      *
      * @param projectId  project id (hash)
      * @param identifier object identifier
      * @return the object to get
      */
     public MetadataObject getMetadataObject(String projectId, String identifier) {
         l.debug("Executing getObjectByIdentifier identifier=" + identifier);
         Map<String, String> uris = identifierToUri(projectId, new String[]{identifier});
         if (uris != null && uris.size() > 0) {
             String uri = uris.get(identifier);
             if (uri != null && uri.length() > 0)
                 return getMetadataObject(uri);
             else {
                 l.debug("Can't getObjectByIdentifier identifier=" + identifier + " The identifier doesn't exists.");
                 throw new GdcRestApiException("Can't getObjectByIdentifier identifier=" + identifier + " The identifier doesn't exists.");
             }
         } else {
             l.debug("Can't getObjectByIdentifier identifier=" + identifier + " The identifier doesn't exists.");
             throw new GdcRestApiException("Can't getObjectByIdentifier identifier=" + identifier + " The identifier doesn't exists.");
         }
     }
 
     /**
      * Returns the dependent objects
      *
      * @param uri the uri of the top-level object
      * @return list of dependent objects
      */
     public List<JSONObject> using(String uri) {
         l.debug("Executing using uri=" + uri);
         List<JSONObject> ret = new ArrayList<JSONObject>();
         //HACK!
         String usedUri = uri.replace("/obj/", "/using/");
         HttpMethod req = createGetMethod(getServerUrl() + usedUri);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             if (parsedResp == null || parsedResp.isNullObject() || parsedResp.isEmpty()) {
                 l.debug("Can't call using on uri=" + uri + ". Invalid response.");
                 throw new GdcRestApiException("Can't call using on uri=" + uri + ". Invalid response.");
             }
             JSONObject using = parsedResp.getJSONObject("using");
             if (using == null || using.isNullObject() || using.isEmpty()) {
                 l.debug("Can't call using on uri=" + uri + ". No using data.");
                 throw new GdcRestApiException("Can't call using on uri=" + uri + ". No using data.");
             }
             JSONArray nodes = using.getJSONArray("nodes");
             if (nodes == null) {
                 l.debug("Can't call using on uri=" + uri + ". No nodes key in the response.");
                 throw new GdcRestApiException("Can't call using on uri=" + uri + ". No nodes key in the response.");
             }
             for (Object o : nodes) {
                 JSONObject obj = (JSONObject) o;
                 ret.add(obj);
             }
             return ret;
         } finally {
             req.releaseConnection();
         }
     }
 
     /**
      * Returns the dependent objects
      *
      * @param uri the uri of the top-level object
      * @return list of dependent objects
      */
     public List<JSONObject> usedBy(String uri) {
         l.debug("Executing usedby uri=" + uri);
         List<JSONObject> ret = new ArrayList<JSONObject>();
         //HACK!
         String usedUri = uri.replace("/obj/", "/usedby/");
         HttpMethod req = createGetMethod(getServerUrl() + usedUri);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             if (parsedResp == null || parsedResp.isNullObject() || parsedResp.isEmpty()) {
                 l.debug("Can't call usedby on uri=" + uri + ". Invalid response.");
                 throw new GdcRestApiException("Can't call usedby on uri=" + uri + ". Invalid response.");
             }
             JSONObject usedby = parsedResp.getJSONObject("usedby");
             if (usedby == null || usedby.isNullObject() || usedby.isEmpty()) {
                 l.debug("Can't call usedby on uri=" + uri + ". No usedby data.");
                 throw new GdcRestApiException("Can't call usedby on uri=" + uri + ". No usedby data.");
             }
             JSONArray nodes = usedby.getJSONArray("nodes");
             if (nodes == null) {
                 l.debug("Can't call usedby on uri=" + uri + ". No nodes key in the response.");
                 throw new GdcRestApiException("Can't call usedby on uri=" + uri + ". No nodes key in the response.");
             }
             for (Object o : nodes) {
                 JSONObject obj = (JSONObject) o;
                 ret.add(obj);
             }
             return ret;
         } finally {
             req.releaseConnection();
         }
     }
 
     /**
      * Creates a new object in the metadata server
      *
      * @param projectId project id (hash)
      * @param content   the new object content
      * @return the new object
      */
     public JSONObject createMetadataObject(String projectId, JSON content) {
         l.debug("Executing createMetadataObject on project id=" + projectId + "content='" + content.toString() + "'");
         PostMethod req = createPostMethod(getProjectMdUrl(projectId) + OBJ_URI + "?createAndGet=true");
         try {
             String str = content.toString();
             InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(str.getBytes("utf-8")));
             req.setRequestEntity(request);
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             return parsedResp;
         } catch (HttpMethodException ex) {
             l.debug("Failed executing createMetadataObject on project id=" + projectId + "content='" + content.toString() + "'");
             throw new GdcRestApiException("Failed executing createMetadataObject on project id=" + projectId + "content='" + content.toString() + "'", ex);
         } catch (UnsupportedEncodingException e) {
             l.debug("String#getBytes(\"utf-8\") threw UnsupportedEncodingException", e);
             throw new IllegalStateException(e);
         } finally {
             req.releaseConnection();
         }
     }
 
     /**
      * Modifies an object in the metadata server
      *
      * @param projectId project id (hash)
      * @param objectId  object id (integer)
      * @param content   the new object content
      * @return the new object
      */
     public JSONObject modifyMetadataObject(String projectId, int objectId, JSON content) {
         l.debug("Executing modifyMetadataObject on project id=" + projectId + " objectId=" + objectId + " content='" + content.toString() + "'");
         return modifyMetadataObject(MD_URI + projectId + OBJ_URI + "/" + objectId, content);
     }
 
     /**
      * Modifies an object in the metadata server
      *
      * @param uri     object uri
      * @param content the new object content
      * @return the new object
      */
     public JSONObject modifyMetadataObject(String uri, JSON content) {
         l.debug("Executing modifyMetadataObject on uri=" + uri + " content='" + content.toString() + "'");
         PostMethod req = createPostMethod(getServerUrl() + uri);
         try {
             InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                     content.toString().getBytes("utf-8")));
             req.setRequestEntity(request);
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             return parsedResp;
         } catch (HttpMethodException ex) {
             l.debug("Failed executing modifyMetadataObject on uri=" + uri + " content='" + content.toString() + "'");
             throw new GdcRestApiException("Failed executing modifyMetadataObject on uri=" + uri + " content='" + content.toString() + "'", ex);
         } catch (UnsupportedEncodingException e) {
             l.debug("String#getBytes(\"utf-8\") threw UnsupportedEncodingException", e);
             throw new IllegalStateException(e);
         } finally {
             req.releaseConnection();
         }
     }
 
     /**
      * Deletes an object in the metadata server
      *
      * @param projectId project id (hash)
      * @param objectId  object id (integer)
      * @return the new object
      */
     public void deleteMetadataObject(String projectId, int objectId) {
         l.debug("Executing deleteMetadataObject on project id=" + projectId + " objectId=" + objectId);
         deleteMetadataObject(MD_URI + projectId + OBJ_URI + "/" + objectId);
     }
 
     /**
      * Deletes an object in the metadata server
      *
      * @param uri object uri
      * @return the new object
      */
     public void deleteMetadataObject(String uri) {
         l.debug("Executing deleteMetadataObject on project uri=" + uri);
         DeleteMethod req = createDeleteMethod(getServerUrl() + uri);
         try {
             String resp = executeMethodOk(req);
         } catch (HttpMethodException ex) {
             l.debug("Failed executing deleteMetadataObject on project uri=" + uri);
             throw new GdcRestApiException("Failed executing deleteMetadataObject on uri=" + uri, ex);
         } finally {
             req.releaseConnection();
         }
     }
 
     /**
      * Determines the projet's ETL mode (SLI/DLI/VOID)
      *
      * @param pid project id
      * @return project's ETL mode
      */
     public String getProjectEtlMode(String pid) {
         l.debug("Getting project etl status.");
         GetMethod req = createGetMethod(getProjectMdUrl(pid) + ETL_MODE_URI);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             if (parsedResp != null && !parsedResp.isNullObject() && !parsedResp.isEmpty()) {
                 JSONObject etlMode = parsedResp.getJSONObject("etlMode");
                 if (etlMode != null && !etlMode.isNullObject() && !etlMode.isEmpty()) {
                     String mode = etlMode.getString("mode");
                     if (mode != null && mode.length() > 0) {
                         return mode;
                     } else {
                         l.debug("Getting project etl status. No mode in the result: " + etlMode.toString());
                         throw new GdcRestApiException("Getting project etl status. No mode in the result: " + etlMode.toString());
                     }
                 } else {
                     l.debug("Getting project etl status. No etlMode in the result: " + parsedResp.toString());
                     throw new GdcRestApiException("Getting project etl status. No etlMode in the result: " + parsedResp.toString());
                 }
             } else {
                 l.debug("Getting project etl status. Empty result.");
                 throw new GdcRestApiException("Getting project etl status. Empty result.");
             }
         } finally {
             req.releaseConnection();
         }
     }
 
     protected JSONObject getMigrationRequest(List<String> manifests) {
         JSONObject etlMode = new JSONObject();
         etlMode.put("mode", "SLI");
         JSONArray mnfsts = new JSONArray();
         mnfsts.addAll(manifests);
         etlMode.put("sli", mnfsts);
         JSONObject ret = new JSONObject();
         ret.put("etlMode", etlMode);
         return ret;
     }
 
     /**
      * Checks if the migration is finished
      *
      * @param link the link returned from the start loading
      * @return the loading status
      */
     public String getTaskManStatus(String link) throws HttpMethodException {
         l.debug("Getting TaskMan status uri=" + link);
         HttpMethod ptm = createGetMethod(getServerUrl() + link);
         try {
             String response = "";
             boolean isFinished = false;
             while (!isFinished) {
                 try {
                     response = executeMethodOk(ptm);
                     isFinished = true;
                 } catch (HttpMethodNotFinishedYetException e) {
                     l.debug("getTaskManStatus: Waiting for status");
                     try {
                         Thread.sleep(500);
                     } catch (InterruptedException ex) {
                         // do nothing
                     }
                 }
             }
             JSONObject task = JSONObject.fromObject(response);
             JSONObject state = task.getJSONObject("wTaskStatus");
             if (state != null && !state.isNullObject() && !state.isEmpty()) {
                 String status = state.getString("status");
                 l.debug("TaskMan status=" + status);
                 return status;
             } else {
                 l.debug("No wTaskStatus structure in the migration status!");
                 throw new GdcRestApiException("No wTaskStatus structure in the migration status!");
             }
         } finally {
             ptm.releaseConnection();
         }
     }
 
     /**
      * Migrates project datasets from DLI to SLI
      *
      * @param pid       project ID
      * @param manifests array of all dataset's manifests
      */
     public String migrateDataSets(String pid, List<String> manifests) {
         l.debug("Migrating project to SLI.");
         String currentMode = getProjectEtlMode(pid);
         l.debug("Migrating project to SLI: current status is " + currentMode);
         if (ETL_MODE_DLI.equalsIgnoreCase(currentMode) || ETL_MODE_VOID.equalsIgnoreCase(currentMode)) {
             PostMethod req = createPostMethod(getProjectMdUrl(pid) + ETL_MODE_URI);
             InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(getMigrationRequest(manifests).toString().getBytes()));
             req.setRequestEntity(request);
             try {
                 String resp = executeMethodOk(req);
                 JSONObject responseObject = JSONObject.fromObject(resp);
                 String taskLink = responseObject.getString("uri");
                 return taskLink;
 
             } catch (HttpMethodException ex) {
                 l.debug("Migrating project to SLI failed.", ex);
                 throw new GdcRestApiException("Migrating project to SLI failed.", ex);
             } finally {
                 req.releaseConnection();
             }
 
         } else {
             l.debug("Migrating project to SLI: no migration needed. Skipping.");
             return "";
         }
     }
 
     private static GetMethod createGetMethod(String path) {
         return configureHttpMethod(new GetMethod(path));
     }
 
     private static PostMethod createPostMethod(String path) {
         return configureHttpMethod(new PostMethod(path));
     }
 
     private static DeleteMethod createDeleteMethod(String path) {
         return configureHttpMethod(new DeleteMethod(path));
     }
 
     private static <T extends HttpMethod> T configureHttpMethod(T request) {
         request.setRequestHeader("Content-Type", "application/json; charset=utf-8");
         request.setRequestHeader("Accept", "application/json");
         request.setRequestHeader("Accept-Charset", "utf-u");
         request.setRequestHeader("User-Agent", "GoodData CL/1.2.65");
         return request;
     }
 
     protected void finalize() throws Throwable {
         try {
            // logout();
         } finally {
             super.finalize();
         }
     }
     
     /**
      * API for querying users in a domain
      * 
      * @param domain
      * @return
      */
     public Map<String, GdcUser> getUsers(String domain) {
 	Map<String, GdcUser> users = new HashMap<String, GdcUser>();
 
 	String url = "/gdc/account/domains/" + domain + "/users";
 	JSONObject jsonObject = getObjectByUri(url);
 	if (jsonObject == null) {
 	    return users;
 	}
 	JSONObject accountSettings = jsonObject
 		.getJSONObject("accountSettings");
 	if (accountSettings == null) {
 	    return users;
 	}
 	JSONArray items = (JSONArray) accountSettings.get("items");
 	if (items == null) {
 	    return users;
 	}
 	for (Object item : items) {
 	    JSONObject itemJSON = JSONObject.fromObject(item);
 	    if (itemJSON == null) {
 		continue;
 	    }
 	    JSONObject accountSetting = itemJSON
 		    .getJSONObject("accountSetting");
 	    if (accountSetting == null) {
 		continue;
 	    }
 	    GdcUser user = new GdcUser();
 	    user.setLogin(accountSetting.getString("login"));
 	    user.setFirstName(accountSetting.getString("firstName"));
 	    user.setLastName(accountSetting.getString("lastName"));
 	    user.setCompanyName(accountSetting.getString("companyName"));
 	    user.setPosition(accountSetting.getString("position"));
 	    user.setCountry(accountSetting.getString("country"));
 	    user.setTimezone(accountSetting.getString("timezone"));
 	    user.setPhoneNumber(accountSetting.getString("phoneNumber"));
 	    user.setEmail(accountSetting.getString("email"));
 	    JSONObject links = accountSetting.getJSONObject("links");
 	    if (links == null)
 		throw new GdcException(
 			"The URL link for a user cannot be null: "
 				+ user.getLogin());
 	    String uri = links.getString("self");
 	    if (uri == null)
 		throw new GdcException("The URL for a user cannot be null: "
 			+ user.getLogin());
 	    user.setUri(uri);
 	    users.put(user.getLogin(), user);
 	}
 	return users;
     }
 
     public List<String> enumerateDimensions(String projectId) {
 	return enumerateResource(projectId, QUERY_DIMENSIONS);
     }
 
     public List<String> enumerateDataSets(String projectId) {
 	return enumerateResource(projectId, QUERY_DATASETS);
     }
 
     public List<String> enumerateFolders(String projectId) {
 	return enumerateResource(projectId, QUERY_FOLDERS);
     }
 
     public List<String> enumerateDashboards(String projectId) {
 	return enumerateResource(projectId, QUERY_PROJECTDASHBOARDS);
     }
 
     protected List<String> enumerateResource(String projectId, String resource) {
 	l.debug("Enumerating attributes for project id=" + projectId);
 	List<String> list = new ArrayList<String>();
 	String qUri = getProjectMdUrl(projectId) + QUERY_PREFIX + resource;
 	HttpMethod qGet = createGetMethod(qUri);
 	try {
 	    String qr = executeMethodOk(qGet);
 	    JSONObject q = JSONObject.fromObject(qr);
 	    if (q.isNullObject()) {
 		l.debug("Enumerating "+resource+" for project id="+projectId+" failed.");
 		throw new GdcException(
 			"Enumerating "+resource+" for project id="+projectId+" failed.");
 	    }
 	    JSONObject qry = q.getJSONObject("query");
 	    if (qry.isNullObject()) {
 		l.debug("Enumerating "+resource+" for project id="+projectId+" failed.");
 		throw new GdcException(
 			"Enumerating "+resource+" for project id="+projectId+" failed.");
 	    }
 	    JSONArray entries = qry.getJSONArray("entries");
 	    if (entries == null) {
 		l.debug("Enumerating "+resource+" for project id="+projectId+" failed.");
 		throw new GdcException(
 			"Enumerating "+resource+" for project id="+projectId+" failed.");
 	    }
 	    for (Object oentry : entries) {
 		JSONObject entry = (JSONObject) oentry;
 		list.add(entry.getString("link"));
 	    }
 	} finally {
 	    qGet.releaseConnection();
 	}
 	return list;
     }
 
     public ProjectExportResult exportMDByUrl(String projectId, List<String> urls) {
 	l.debug("Exporting metadata objects with URls " + urls
 		+ " from project " + projectId);
 	PostMethod req = createPostMethod(getProjectMdUrl(projectId)
 		+ PROJECT_PARTIAL_EXPORT_URI);
 	JSONObject param = getMDExportStructureStrings(projectId, urls);
 	InputStreamRequestEntity request = new InputStreamRequestEntity(
 		new ByteArrayInputStream(param.toString().getBytes(
 			Charset.forName("UTF-8"))));
 	req.setRequestEntity(request);
 	ProjectExportResult result = null;
 	try {
 	    String response = executeMethodOk(req);
 	    result = new ProjectExportResult();
 	    JSONObject responseObject = JSONObject.fromObject(response);
 	    JSONObject exportArtifact = responseObject
 		    .getJSONObject("partialMDArtifact");
 	    JSONObject status = exportArtifact.getJSONObject("status");
 	    result.setTaskUri(status.getString("uri"));
 	    result.setExportToken(exportArtifact.getString("token"));
 	    return result;
 	} catch (HttpMethodException ex) {
 	    l.debug("Error exporting metadata objects with URls " + urls
 		    + " from project " + projectId, ex);
 	    throw new GdcRestApiException(
 		    "Error exporting metadata objects with URls " + urls
 			    + " from project " + projectId, ex);
 	} finally {
 	    req.releaseConnection();
 	}
     }
 
     protected JSONObject getMDExportStructureStrings(String projectId,
 	    List<String> urls) {
 	JSONObject param = new JSONObject();
 	JSONObject partialMDExport = new JSONObject();
 	JSONArray uris = new JSONArray();
 	for (String url : urls) {
 	    uris.add(url);
 	}
 	partialMDExport.put("uris", uris);
 	param.put("partialMDExport", partialMDExport);
 	return param;
     }
 
     public NamePasswordConfiguration getNamePasswordConfiguration() {
 	return config;
     }
 
     /**
      * Checks if report copying is finished. Workaround implementation due to
      * wrong handling of status code.
      * 
      * @param link
      *            the link returned from the start loading
      * @return the loading status
      */
     public String getCopyStatus(String link) {
 	l.debug("Getting Cloning Status status uri=" + link);
 	HttpMethod ptm = createGetMethod(getServerUrl() + link);
 
 	try {
 	    String response = executeMethodOk(ptm);
 	    if (response != null && !response.isEmpty()) {
 		JSONObject task = JSONObject.fromObject(response);
 		JSONObject state = task.getJSONObject("taskState");
 		if (state != null && !state.isNullObject() && !state.isEmpty()) {
 		    String status = state.getString("status");
 		    l.debug("TaskMan status=" + status);
 		    return status;
 		} else {
 		    l.debug("No wTaskStatus structure in the migration status!");
 		    throw new GdcRestApiException(
 			    "No wTaskStatus structure in the migration status!");
 		}
 	    }
 	    return "RUNNING";
 	} catch (HttpMethodException e) {
 	    // workaround implementation due to wrong handling (at least for
 	    // this status)
 	    if (e instanceof HttpMethodNotFinishedYetException
 		    || (e.getCause() != null && e.getCause() instanceof HttpMethodNotFinishedYetException)) {
 		l.debug("getTaskManStatus: Waiting for status");
 		return "RUNNING";
 	    }
 	    throw e;
 	} finally {
 	    ptm.releaseConnection();
 	}
     }
     
 
     /**
      * Retrieves the project info by the project's name
      * 
      * @param name
      *            the project name
      * @return the GoodDataProjectInfo populated with the project's information
      * @throws HttpMethodException
      * @throws GdcProjectAccessException
      */
     @Deprecated
     public Project getProjectByName(String name) throws HttpMethodException,
 	    GdcProjectAccessException {
 	l.debug("Getting project by name=" + name);
 	for (Iterator<JSONObject> linksIter = getProjectsLinks(); linksIter
 		.hasNext();) {
 	    JSONObject link = linksIter.next();
 	    String cat = link.getString("category");
 	    if (!"project".equalsIgnoreCase(cat)) {
 		continue;
 	    }
 	    String title = link.getString("title");
 	    if (title.equals(name)) {
 		Project proj = new Project(link);
 		l.debug("Got project by name=" + name);
 		return proj;
 	    }
 	}
 	l.debug("The project name=" + name + " doesn't exists.");
 	throw new GdcProjectAccessException("The project name=" + name
 		+ " doesn't exists.");
     }
 
     /**
      * Returns the existing projects links
      * 
      * @return accessible projects links
      * @throws com.gooddata.exception.HttpMethodException
      */
     @Deprecated
     @SuppressWarnings("unchecked")
     private Iterator<JSONObject> getProjectsLinks() throws HttpMethodException {
 	l.debug("Getting project links.");
 	HttpMethod req = createGetMethod(getServerUrl() + MD_URI);
 	try {
 	    String resp = executeMethodOk(req);
 	    JSONObject parsedResp = JSONObject.fromObject(resp);
 	    JSONObject about = parsedResp.getJSONObject("about");
 	    JSONArray links = about.getJSONArray("links");
 	    l.debug("Got project links " + links);
 	    return links.iterator();
 	} finally {
 	    req.releaseConnection();
 	}
     }
 
     /**
      * Create a new GoodData project
      * 
      * @param name
      *            project name
      * @param desc
      *            project description
      * @param templateUri
      *            project template uri
      * @return the project Id
      * @throws GdcRestApiException
      */
     @Deprecated
     public String createProject(String name, String desc, String templateUri)
 	    throws GdcRestApiException {
 	    return this.createProject(name, desc, templateUri, null, null);
     }
     
     /**
      * Returns the List of GoodDataProjectInfo structures for the accessible
      * projects
      * 
      * @return the List of GoodDataProjectInfo structures for the accessible
      *         projects
      * @throws HttpMethodException
      */
     @Deprecated
     public List<Project> listProjects() throws HttpMethodException {
     l.debug("Listing projects.");
     List<Project> list = new ArrayList<Project>();
     for (Iterator<JSONObject> linksIter = getProjectsLinks(); linksIter
         .hasNext();) {
         JSONObject link = linksIter.next();
         String cat = link.getString("category");
         if (!"project".equalsIgnoreCase(cat)) {
         continue;
         }
         Project proj = new Project(link);
         list.add(proj);
     }
     l.debug("Found projects " + list);
     return list;
     }
 
     /**
      * Gets a report definition from the report uri (/gdc/obj...)
      *
      * @param reportUri report uri (/gdc/obj...)
      * @return report definition
      */
     @Deprecated
     public String getReportDefinition(String reportUri) {
         l.debug( "Getting report definition for report uri=" + reportUri );
         String qUri = getServerUrl() + reportUri;
         HttpMethod qGet = createGetMethod( qUri );
         try {
             String qr = executeMethodOk( qGet );
             JSONObject q = JSONObject.fromObject( qr );
             if (q.isNullObject()) {
                 l.debug("Error getting report definition for report uri=" + reportUri);
                 throw new GdcProjectAccessException("Error getting report definition for report uri=" + reportUri);
             }
             JSONObject report = q.getJSONObject("report");
             if (report.isNullObject()) {
                 l.debug("Error getting report definition for report uri=" + reportUri);
                 throw new GdcProjectAccessException("Error getting report definition for report uri=" + reportUri);
             }
             JSONObject content = report.getJSONObject("content");
             if (content.isNullObject()) {
                 l.debug("Error getting report definition for report uri=" + reportUri);
                 throw new GdcProjectAccessException("Error getting report definition for report uri=" + reportUri);
             }
             JSONArray definitions = content.getJSONArray("definitions");
             if (definitions == null) {
                 l.debug("Error getting report definition for report uri=" + reportUri);
                 throw new GdcProjectAccessException("Error getting report definition for report uri=" + reportUri);
             }
             if (definitions.size() > 0) {
                 String lastDefUri = definitions.getString(definitions.size() - 1);
                 qUri = getServerUrl() + lastDefUri;
                 return lastDefUri;
             }
             else {
                 l.debug("Error getting report definition for report uri=" + reportUri);
                 throw new GdcProjectAccessException("Error getting report definition for report uri=" + reportUri);
             }
         } finally {
             if (qGet != null)
                 qGet.releaseConnection();
         }
     }
 
 
 }
 
 
