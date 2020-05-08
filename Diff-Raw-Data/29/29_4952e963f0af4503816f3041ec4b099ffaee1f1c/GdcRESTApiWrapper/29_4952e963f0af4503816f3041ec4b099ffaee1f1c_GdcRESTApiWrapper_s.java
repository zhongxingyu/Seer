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
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONException;
 import net.sf.json.JSONObject;
 
 import org.apache.commons.httpclient.Cookie;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.log4j.Logger;
 
 import com.gooddata.exception.GdcLoginException;
 import com.gooddata.exception.GdcProjectAccessException;
 import com.gooddata.exception.GdcRestApiException;
 import com.gooddata.exception.HttpMethodException;
 import com.gooddata.integration.model.DLI;
 import com.gooddata.integration.model.DLIPart;
 import com.gooddata.integration.model.Project;
 import com.gooddata.integration.rest.configuration.NamePasswordConfiguration;
 
 /**
  * The GoodData REST API Java wrapper.
  *
  * @author jiri.zaloudek
  * @author Zdenek Svoboda <zd@gooddata.org>
  * @version 1.0
  */
 public class GdcRESTApiWrapper {
 
     private static Logger l = Logger.getLogger(GdcRESTApiWrapper.class);
 
     /**
      * GDC URIs
      */
     private static final String MD_URI = "/gdc/md/";
     private static final String LOGIN_URI = "/gdc/account/login";
     private static final String TOKEN_URI = "/gdc/account/token";
     private static final String DATA_INTERFACES_URI = "/ldm/dataloadinterface";
     private static final String PROJECTS_URI = "/gdc/projects";
     private static final String PULL_URI = "/etl/pull";
     private static final String DLI_DESCRIPTOR_URI = "/descriptor";
     public static final String MAQL_EXEC_URI = "/ldm/manage";
 
     public static final String DLI_MANIFEST_FILENAME = "upload_info.json";
 
     protected HttpClient client;
     protected NamePasswordConfiguration config;
     private String ssToken;
 
     /**
      * Constructs the GoodData REST API Java wrapper
      *
      * @param config NamePasswordConfiguration object with the GDC name and password configuration
      */
     public GdcRESTApiWrapper(NamePasswordConfiguration config) {
         this.config = config;
         client = new HttpClient();
     }
 
     /**
      * GDC login - obtain GDC SSToken
      *
      * @return the new SS token
      * @throws GdcLoginException
      */
     public String login() throws GdcLoginException {
         l.debug("Logging into GoodData.");
         if (ssToken != null) {
             return ssToken;
         }
         JSONObject loginStructure = getLoginStructure();
         PostMethod loginPost = new PostMethod(config.getUrl() + LOGIN_URI);
         setJsonHeaders(loginPost);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(loginStructure.toString().getBytes()));
         loginPost.setRequestEntity(request);
         try {
             executeMethodOk(loginPost, false); // do not re-login on SC_UNAUTHORIZED
             // read SST from cookie
             for (Cookie cookie : client.getState().getCookies()) {
                 if ("GDCAuthSST".equals(cookie.getName())) {
                     ssToken = cookie.getValue();
                     setTokenCookie();
                     l.debug("Succesfully logged into GoodData.");
                     return ssToken;
                 }
             }
             l.debug("Error logging into GoodData.");
             throw new GdcLoginException("GDCAuthSST was not found in cookies after login.");
         } catch (HttpMethodException ex) {
            l.error("Error logging to GoodData.",ex);
             throw new GdcLoginException("Login to GDC failed: " + ex.getMessage());
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
      * @throws GdcLoginException
      */
     private void setTokenCookie() throws GdcLoginException {
         HttpMethod secutityTokenGet = new GetMethod(config.getUrl() + TOKEN_URI);
 
         setJsonHeaders(secutityTokenGet);
 
         // set SSToken from config
         Cookie sstCookie = new Cookie(config.getGdcHost(), "GDCAuthSST", ssToken, "/", -1, false);
         sstCookie.setPathAttributeSpecified(true);
         client.getState().addCookie(sstCookie);
 
         try {
             executeMethodOk(secutityTokenGet);
         } catch (HttpMethodException ex) {
             throw new GdcLoginException("Cannot login to:" + config.getUrl() + TOKEN_URI + ". Reason: "
                     + ex.getMessage());
         } finally {
             secutityTokenGet.releaseConnection();
         }
     }
 
 
     /**
      * Retrieves the project info by the project's name
      *
      * @param name the project name
      * @return the GoodDataProjectInfo populated with the project's information
      * @throws HttpMethodException
      * @throws GdcProjectAccessException
      */
     public Project getProjectByName(String name) throws HttpMethodException, GdcProjectAccessException {
         l.debug("Getting project by name="+name);
         for (Iterator<JSONObject> linksIter = getProjectsLinks(); linksIter.hasNext();) {
             JSONObject link = (JSONObject) linksIter.next();
             String cat = link.getString("category");
             if (!"project".equalsIgnoreCase(cat)) {
                 continue;
             }
             String title = link.getString("title");
             if (title.equals(name)) {
                 Project proj = new Project(link);
                 l.debug("Got project by name="+name);
                 return proj;
             }
         }
         l.debug("The project name=" + name + " doesn't exists.");
         throw new GdcProjectAccessException("The project name=" + name + " doesn't exists.");
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
         l.debug("Getting project by id="+id);
         for (Iterator<JSONObject> linksIter = getProjectsLinks(); linksIter.hasNext();) {
             JSONObject link = (JSONObject) linksIter.next();
             String cat = link.getString("category");
             if (!"project".equalsIgnoreCase(cat)) {
                 continue;
             }
             String name = link.getString("identifier");
             if (name.equals(id)) {
                 Project proj = new Project(link);
                 l.debug("Got project by id="+id);
                 return proj;
             }
         }
         l.debug("The project id=" + id + " doesn't exists.");
         throw new GdcProjectAccessException("The project id=" + id + " doesn't exists.");
     }
 
     /**
      * Returns the existing projects links
      *
      * @return accessible projects links
      * @throws com.gooddata.exception.HttpMethodException
      */
     @SuppressWarnings("unchecked")
     private Iterator<JSONObject> getProjectsLinks() throws HttpMethodException {
         HttpMethod req = new GetMethod(config.getUrl() + MD_URI);
         setJsonHeaders(req);
         String resp = executeMethodOk(req);
         JSONObject parsedResp = JSONObject.fromObject(resp);
         JSONObject about = parsedResp.getJSONObject("about");
         JSONArray links = about.getJSONArray("links");
         return links.iterator();
     }
 
     /**
      * Returns the List of GoodDataProjectInfo structures for the accessible projects
      *
      * @return the List of GoodDataProjectInfo structures for the accessible projects
      * @throws HttpMethodException
      */
     public List<Project> listProjects() throws HttpMethodException {
         l.debug("Listing projects.");
         List<Project> list = new ArrayList<Project>();
         for (Iterator<JSONObject> linksIter = getProjectsLinks(); linksIter.hasNext();) {
             JSONObject link = linksIter.next();
             String cat = link.getString("category");
             if (!"project".equalsIgnoreCase(cat)) {
                 continue;
             }
             Project proj = new Project(link);
             list.add(proj);
         }
         l.debug("Found projects "+list);
         return list;
     }
 
 
     /**
      * Finds a project DLI by it's name
      *
      * @param name the DLI name
      * @param projectId the project id 
      * @return the DLI
      * @throws GdcProjectAccessException if the DLI doesn't exist
      * @throws HttpMethodException if there is a communication issue with the GDC platform
      */
     public DLI getDLIByName(String name, String projectId) throws GdcProjectAccessException, HttpMethodException {
         l.debug("Get DLI by name="+name+" project id="+projectId);
         List<DLI> dlis = getDLIs(projectId);
         for (DLI dli : dlis) {
             if (name.equals(dli.getName())) {
                 l.debug("Got DLI by name="+name+" project id="+projectId);
                 return dli;
             }
         }
         l.debug("The DLI name=" + name + " doesn't exist in the project id="+projectId);
         throw new GdcProjectAccessException("The DLI name=" + name + " doesn't exist!");
     }
 
     /**
      * Finds a project DLI by it's id
      *
      * @param id the DLI id
      * @param projectId the project id
      * @return the DLI
      * @throws GdcProjectAccessException if the DLI doesn't exist
      * @throws HttpMethodException if there is a communication issue with the GDC platform 
      */
     public DLI getDLIById(String id, String projectId) throws GdcProjectAccessException, HttpMethodException {
         l.debug("Get DLI by id="+id+" project id="+projectId);
         List<DLI> dlis = getDLIs(projectId);
         for (DLI dli : dlis) {
             if (id.equals(dli.getId())) {
                 l.debug("Got DLI by id="+id+" project id="+projectId);
                 return dli;
             }
         }
         l.debug("The DLI id=" + id+ " doesn't exist in the project id="+projectId);
         throw new GdcProjectAccessException("The DLI id=" + id + " doesn't exist!");
     }
 
 
     public String getToken() {
         return ssToken;
     }
 
     /**
      * Returns a list of project's data loading interfaces
      *
      * @param projectId project's ID
      * @return a list of project's data loading interfaces
      * @throws HttpMethodException if there is a communication error
      * @throws GdcProjectAccessException if the DLI doesn't exist
      */
     public List<DLI> getDLIs(String projectId) throws HttpMethodException, GdcProjectAccessException {
         l.debug("Getting DLIs from project id="+projectId);
         List<DLI> list = new ArrayList<DLI>();
         String ifcUri = getDLIsUri(projectId);
         HttpMethod interfacesGet = new GetMethod(ifcUri);
         setJsonHeaders(interfacesGet);
         String response = executeMethodOk(interfacesGet);
         JSONObject responseObject = JSONObject.fromObject(response);
         if (responseObject == null) {
             throw new GdcProjectAccessException("The project id=" + projectId + " doesn't exist!");
         }
         JSONObject interfaceQuery = responseObject.getJSONObject("about");
         if (interfaceQuery == null) {
             throw new GdcProjectAccessException("The project id=" + projectId + " doesn't exist!");
         }
         JSONArray links = interfaceQuery.getJSONArray("links");
         if (links == null) {
             throw new GdcProjectAccessException("The project id=" + projectId + " doesn't exist!");
         }
         for (Object ol : links) {
             JSONObject link = (JSONObject) ol;
             String descriptorUri = config.getUrl() + link.getString("link") + "/descriptor";
             DLI ii = new DLI(link);
             list.add(ii);
         }
         l.debug("Got DLIs "+list+" from project id="+projectId);
         return list;
     }
 
     /**
      * Returns a list of DLI parts
      *
      * @param dliId DLI ID
      * @param projectId project's ID
      * @return a list of project's data loading interfaces
      * @throws HttpMethodException if there is a communication error
      */
     public List<DLIPart> getDLIParts(String dliId, String projectId) throws HttpMethodException {
         l.debug("Getting DLI parts DLI id = "+dliId+" from project id="+projectId);
         List<DLIPart> list = new ArrayList<DLIPart>();
         String dliUri = getDLIUri(dliId, projectId);
         HttpMethod dliGet = new GetMethod(dliUri);
         setJsonHeaders(dliGet);
         String response = executeMethodOk(dliGet);
         JSONObject partsResponseObject = JSONObject.fromObject(response);
         if (partsResponseObject == null) {
             l.debug("No DLI parts DLI id = "+dliId+" from project id="+projectId);
             throw new GdcProjectAccessException("No DLI parts DLI id = "+dliId+" from project id="+projectId);
         }
         JSONObject dli = partsResponseObject.getJSONObject("dataSetDLI");
         if (dli == null) {
             l.debug("No DLI parts DLI id = "+dliId+" from project id="+projectId);
             throw new GdcProjectAccessException("No DLI parts DLI id = "+dliId+" from project id="+projectId);
         }
         JSONArray parts = dli.getJSONArray("parts");
         for (Object op : parts) {
             JSONObject part = (JSONObject) op;
             list.add(new DLIPart(part));
         }
         l.debug("Got DLI parts "+list+" DLI id = "+dliId+" from project id="+projectId);
         return list;
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
         l.debug("Initiating data load project id="+projectId+" remoteDir="+remoteDir);
         PostMethod pullPost = new PostMethod(getProjectMdUrl(projectId) + PULL_URI);
         setJsonHeaders(pullPost);
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
         l.debug("Data load project id="+projectId+" remoteDir="+remoteDir+" initiated. Status is on uri="+taskLink);
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
         l.debug("Getting data loading status uri="+link);
         HttpMethod ptm = new GetMethod(config.getUrl() + link);
         setJsonHeaders(ptm);
         String response = executeMethodOk(ptm);
         JSONObject task = JSONObject.fromObject(response);
         String status = task.getString("taskStatus");
         l.debug("Loading status="+status);
         return status;
     }
 
     
 
     /**
      * Kicks the GDC platform to inform it that the FTP transfer is finished.
      *
      * @param name project name
      * @param desc project description
      * @return the project Id
      * @throws GdcRestApiException
      */
     public String createProject(String name, String desc) throws GdcRestApiException {
         l.debug("Creating project name="+name);
         PostMethod createProjectPost = new PostMethod(config.getUrl() + PROJECTS_URI);
         setJsonHeaders(createProjectPost);
         JSONObject createProjectStructure = getCreateProject(name, desc);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 createProjectStructure.toString().getBytes()));
         createProjectPost.setRequestEntity(request);
         String uri = null;
         try {
             String response = executeMethodOk(createProjectPost);
             JSONObject responseObject = JSONObject.fromObject(response);
             uri = responseObject.getString("uri");
         } catch (HttpMethodException ex) {
             l.error("Creating project fails: ",ex);
             throw new GdcRestApiException("Creating project fails: ",ex);
         } finally {
             createProjectPost.releaseConnection();
         }
 
         if(uri != null && uri.length() > 0) {
             String id = getProjectId(uri);
             l.debug("Created project id="+id);
             return id;
         }
         throw new GdcRestApiException("Error creating project.");
     }
 
     /**
      * Returns the create project JSON structure
      *
      * @param name project name
      * @param desc project description
      * @return the create project JSON structure
      */
     private JSONObject getCreateProject(String name, String desc) {
         JSONObject meta = new JSONObject();
         meta.put("title", name);
         meta.put("summary", desc);
         JSONObject content = new JSONObject();
         //content.put("state", "ENABLED");
         content.put("guidedNavigation","1");
         JSONObject project = new JSONObject();
         project.put("meta", meta);
         project.put("content", content);
         JSONObject createStructure  = new JSONObject();
         createStructure.put("project", project);
         return createStructure;
     }
 
     /**
      * Retrieves the project id from the URI returned by the create project
      * @param uri the create project URI
      * @return project id
      * @throws GdcRestApiException in case the project doesn't exist
      */
     protected String getProjectId(String uri) throws GdcRestApiException {
         l.debug("Getting project id by uri="+uri);
         HttpMethod req = new GetMethod(config.getUrl() + uri);
         setJsonHeaders(req);
         String resp = executeMethodOk(req);
         JSONObject parsedResp = JSONObject.fromObject(resp);
         if(parsedResp == null) {
             l.error("Can't get project from "+uri);
             throw new GdcRestApiException("Can't get project from "+uri);
         }
         JSONObject project = parsedResp.getJSONObject("project");
         if(project == null) {
             l.error("Can't get project from "+uri);
             throw new GdcRestApiException("Can't get project from "+uri);
         }
         JSONObject links = project.getJSONObject("links");
         if(links == null) {
             l.error("Can't get project from "+uri);
             throw new GdcRestApiException("Can't get project from "+uri);
         }
         String mdUrl = links.getString("metadata");
         if(mdUrl != null && mdUrl.length()>0) {
             String[] cs = mdUrl.split("/");
             if(cs != null && cs.length > 0) {
                 l.debug("Got project id="+cs[cs.length -1]+" by uri="+uri);
                 return cs[cs.length -1];
             }
         }
         throw new GdcRestApiException("Can't get project from "+uri); 
     }
 
     /**
      * Executes the MAQL and creates/modifies the project's LDM
      *
      * @param projectId the project's ID
      * @param maql String with the MAQL statements
      * @return result String
      * @throws GdcRestApiException
      */
     public String[] executeMAQL(String projectId, String maql) throws GdcRestApiException {
         l.debug("Executing MAQL projectId="+projectId+" MAQL:\n"+maql);
         PostMethod maqlPost = new PostMethod(getProjectMdUrl(projectId) + MAQL_EXEC_URI);
         setJsonHeaders(maqlPost);
         JSONObject maqlStructure = getMAQLExecStructure(maql);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 maqlStructure.toString().getBytes()));
         maqlPost.setRequestEntity(request);
         String result = null;
         try {
             String response = executeMethodOk(maqlPost);
             JSONObject responseObject = JSONObject.fromObject(response);
             JSONArray uris = responseObject.getJSONArray("uris");
             return (String[])uris.toArray(new String[]{""});
         } catch (HttpMethodException ex) {
             l.error("MAQL execution: ",ex);
             throw new GdcRestApiException("MAQL execution: ",ex);
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
 
     /**
      * Executes HttpMethod and test if the response if 200(OK)
      *
      * @param method the HTTP method
      * @return response body as String
      * @throws HttpMethodException
      */
     protected String executeMethodOk(HttpMethod method) throws HttpMethodException {
     	return executeMethodOk(method, true);
     }
 
     /**
      * Executes HttpMethod and test if the response if 200(OK)
      *
      * @param method the HTTP method
      * @param reLoginOn401 flag saying whether we should call login() and retry on {@link HttpStatus.SC_UNAUTHORIZED}
      * @return response body as String
      * @throws HttpMethodException
      */
     private String executeMethodOk(HttpMethod method, boolean reLoginOn401) throws HttpMethodException {
         try {
             client.executeMethod(method);
 
             if (method.getStatusCode() == HttpStatus.SC_OK) {
                 return method.getResponseBodyAsString();
             } else if (method.getStatusCode() == HttpStatus.SC_UNAUTHORIZED && reLoginOn401) {
             	// retry
             	login();
             	return executeMethodOk(method, false);
             } else {
                 String msg = method.getStatusCode() + " " + method.getStatusText();
                 String body = method.getResponseBodyAsString();
                 if (body != null) {
                     msg += ": ";
                     try {
                         JSONObject parsedBody = JSONObject.fromObject(body);
                         msg += parsedBody.toString();
                     } catch (JSONException jsone) {
                         msg += body;
                     }
                 }
                 throw new HttpMethodException("Exception executing " + method.getName() + " on " + method.getPath() + ": " + msg);
             }
         } catch (HttpException e) {
             throw new HttpMethodException("HttpException: " + e.getMessage());
         } catch (IOException e) {
             l.error("Error invoking GoodData API.",e);
             throw new HttpMethodException("IOException: " + e.getMessage());
         }
     }
 
 
     /**
      * Sets the JSON request headers
      *
      * @param request the http request
      */
     protected void setJsonHeaders(HttpMethod request) {
         request.setRequestHeader("Content-Type", "application/json");
         request.setRequestHeader("Accept", "application/json");
     }
 
     /**
      * Returns the data interfaces URI
      *
      * @param projectId project ID
      * @return DLI collection URI
      */
     public String getDLIsUri(String projectId) {
         return getProjectMdUrl(projectId) + DATA_INTERFACES_URI;
     }
 
     /**
      * Returns the DLI URI
      *
      * @param dliId DLI ID
      * @param projectId project ID
      * @return DLI URI
      */
     public String getDLIUri(String dliId, String projectId) {
         return getProjectMdUrl(projectId) + DATA_INTERFACES_URI + "/" + dliId + DLI_DESCRIPTOR_URI;
     }
 
 
     /**
      * Constructs project's metadata uri
      *
      * @param projectId project ID
      */
     protected String getProjectMdUrl(String projectId) {
         return config.getUrl() + MD_URI + projectId;
     }
 
 }
