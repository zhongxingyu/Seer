 package com.meltmedia.cadmium.deployer.jboss7;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.reflect.TypeToken;
 import com.meltmedia.cadmium.core.config.ConfigManager;
 import com.meltmedia.cadmium.core.config.ConfigurationListener;
 import com.meltmedia.cadmium.deployer.JBossUtil;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.auth.params.AuthPNames;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.params.AuthPolicy;
 import org.apache.http.entity.ContentType;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.inject.Singleton;
 import java.io.Closeable;
 import java.io.File;
 import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
 
 /**
  * JBoss 7 rest management API client.
  *
  * List Wars:
  *   curl -v http://localhost:9990/management -u admin -H "Content-Type: application/json"
  *    -d '{"operation":"read-resource","recursive":true, "address":["deployment", "*"]}' --digest
  *
  * Undeploy:
  *   curl -v http://localhost:9990/management -u admin -H "Content-Type: application/json"
  *    -d '{"operation":"remove", "address":["deployment", "test.cadmium.localhost.war"]}' --digest
  *
  * Deploy:
  * upload content:
  *   curl -v --digest -u "admin" -F "file=@git/cadmium/test.cadmium.localhost.war;filename=test.cadmium.localhost.war" http://localhost:9990/management/add-content
  *    result: {"outcome" : "success", "result" : { "BYTES_VALUE" : "SbejgggTNOuHdke5k6EeKdB8Zfo=" }}
  * add and deploy:
  *   curl -v http://localhost:9990/management -u admin -H "Content-Type: application/json"
  *     -d '{"operation":"add", "content":[{"hash":{"BYTES_VALUE" : "SbejgggTNOuHdke5k6EeKdB8Zfo="}}],
  *          "enabled":true, "runtime-name":"test.cadmium.localhost.war",
  *          "address":["deployment", "test.cadmium.localhost.war"]}' --digest
  *
  * List VHosts:
  *   curl -v http://localhost:9990/management -u admin -H "Content-Type: application/json"
  *     -d '{"operation":"read-resource","recursive":"true", "address":["subsystem", "web", "virtual-server","*"]}' --digest
  *
  * Add VHost:
  *   curl -v http://localhost:9990/management -u admin -H "Content-Type:application/json"
  *     -d '{"operation":"add", "name":"test2.cadmium.localhost", "enable-welcome-root":"false",
  *          "address":["subsystem","web","virtual-server","test2.cadmium.localhost"]}' --digest
  *
  * Remove VHost:
  *   curl -v http://localhost:9990/management -u admin -H "Content-Type: application/json"
  *     -d '{"operation":"remove","address":["subsystem", "web", "virtual-server","test2.cadmium.localhost"]}' --digest
  *
  * @author jmcentire
  */
 @Singleton
 public class JBossAdminApi implements ConfigurationListener<JBossManagementConfiguration>, Closeable {
   protected Logger logger = LoggerFactory.getLogger(getClass());
   protected HttpClient client = null;
   protected static final String DATA_KEY = "jboss.server.deploy.dir";
   protected static final String NODE_NAME_KEY = "jboss.node.name";
   protected static final String SERVER_NAME_KEY = "jboss.server.name";
   protected String host = "localhost";
   protected Integer port = 9990;
   protected String serverGroup = null;
   protected String profile = null;
   protected String serverName = null;
   protected String hostName = null;
   protected Boolean domainHost = null;
 
 
   public JBossAdminApi(String username, String password) {
     addAuthentication(username, password, null, null);
   }
 
   public JBossAdminApi(ConfigManager configManager) throws IllegalArgumentException {
     try {
       JBossManagementConfiguration config = configManager.getConfiguration(JBossManagementConfiguration.KEY, JBossManagementConfiguration.class);
 
       addAuthentication(config.getUsername(), config.getPassword(), config.getHost(), config.getPort());
     } catch(Exception e) {
       throw new IllegalArgumentException("No configuration found in Config manager.", e);
     }
   }
 
   public JBossAdminApi(){}
 
   private void addAuthentication(String username, String password, String host, Integer port) {
     if(host != null) {
       this.host = host;
     }
     if(port != null && port > 0 && port <= 65535) {
       this.port = port;
     }
     List<String> authpref = new ArrayList<String>();
     DefaultHttpClient client = new DefaultHttpClient();
     authpref.add(AuthPolicy.DIGEST);
     authpref.add(AuthPolicy.BASIC);
     client.getParams().setParameter(AuthPNames.PROXY_AUTH_PREF, authpref);
     client.getParams().setParameter(AuthPNames.TARGET_AUTH_PREF, authpref);
     client
         .getCredentialsProvider()
         .setCredentials(
             new AuthScope(this.host, this.port),
             new UsernamePasswordCredentials(username, password));
     this.client = client;
   }
 
   public void undeploy(String war) throws Exception {
     //Build request
     Map<String, Object> request = new LinkedHashMap<String, Object>();
     request.put("operation", "remove");
     if(isDomainHost()) {
       String serverGroup = getServerGroup();
 
       request.put("address", Arrays.asList("server-group", serverGroup, "deployment", war));
 
       apiRequest(request);
     }
     request.put("address", Arrays.asList("deployment", war));
 
     apiRequest(request);
   }
 
   public void deploy(String war, String locationRef) throws Exception {
     boolean domainMode = isDomainHost();
     //Build request
     Map<String, Object> request = new LinkedHashMap<String, Object>();
     request.put("operation", "add");
     if(!domainMode) {
       request.put("enabled", true);
     }
     request.put("runtime-name", war);
     Map<String, Object> content = new LinkedHashMap<String, Object>();
     Map<String, Object> byteValue = new LinkedHashMap<String, Object>();
     byteValue.put("BYTES_VALUE", locationRef);
     content.put("hash", byteValue);
     request.put("content", Arrays.asList(content));
     request.put("address", Arrays.asList("deployment", war));
 
     apiRequest(request);
 
     if(domainMode) {
       request.put("enabled", true);
       request.put("address", Arrays.asList("server-group", getServerGroup(), "deployment", war));
 
       apiRequest(request);
     }
   }
 
   /*
    * curl -v --digest -u "admin" -F "file=@git/cadmium/test.cadmium.localhost.war;filename=test.cadmium.localhost.war" http://localhost:9990/management/add-content
    *    result: {"outcome" : "success", "result" : { "BYTES_VALUE" : "SbejgggTNOuHdke5k6EeKdB8Zfo=" }}
    */
   public String uploadWar(String warName, File warFile) throws Exception {
 
     HttpPost post = new HttpPost("http://"+host+":"+port+"/management/add-content");
 
     try {
       MultipartEntity entity = new MultipartEntity();
       entity.addPart("filename", new StringBody(warName));
       entity.addPart("attachment", new FileBody(warFile, ContentType.APPLICATION_OCTET_STREAM.getMimeType()));
       post.setEntity(entity);
 
       HttpResponse response = client.execute(post);
       if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
         String respStr = EntityUtils.toString(response.getEntity());
         Map<String, Object> respObj = new Gson().fromJson(respStr, new TypeToken<Map<String, Object>>(){}.getType());
         if("success".equals(respObj.get("outcome"))) {
           Object resultObj = respObj.get("result");
           if(resultObj instanceof Map) {
             Map<String, Object> resultMap = (Map<String, Object>) resultObj;
             return (String) resultMap.get("BYTES_VALUE");
           }
         } else {
           String failureMessage = (String) respObj.get("failure-description");
           if (failureMessage == null) {
             failureMessage = "Failed to process request.";
           }
           logger.warn(failureMessage);
           throw new Exception("Received " + failureMessage + " response from management api.");
         }
       }
     } finally {
       post.releaseConnection();
     }
     return null;
   }
 
   public void removeVHost(String vhost) throws Exception {
     //Build request
     Map<String, Object> request = new LinkedHashMap<String, Object>();
     request.put("operation", "remove");
     if (!isDomainHost()) {
       request.put("address", Arrays.asList("subsystem", "web", "virtual-server", vhost));
     } else {
       String serverGroup = getServerGroup();
       String profile = getProfile(serverGroup);
       request.put("address", Arrays.asList("profile", profile, "subsystem", "web", "virtual-server", vhost));
     }
 
     apiRequest(request);
   }
 
   public void addVHost(String vhost) throws Exception {
     //Build request
     Map<String, Object> request = new LinkedHashMap<String, Object>();
     request.put("operation", "add");
     request.put("name", vhost);
     request.put("enable-welcome-root", false);
     if (!isDomainHost()) {
       request.put("address", Arrays.asList("subsystem", "web", "virtual-server", vhost));
     } else {
       String serverGroup = getServerGroup();
       String profile = getProfile(serverGroup);
       request.put("address", Arrays.asList("profile", profile, "subsystem", "web", "virtual-server", vhost));
     }
 
     apiRequest(request);
   }
 
   public List<String> listVHosts() throws Exception {
     List<String> vHosts = new ArrayList<String>();
 
     //Build request
     Map<String, Object> request = new LinkedHashMap<String, Object>();
     request.put("operation", "read-resource");
     request.put("recursive", "true");
     if(!isDomainHost()) {
       request.put("address", Arrays.asList("subsystem", "web", "virtual-server", "*"));
     } else {
       String serverGroup = getServerGroup();
       String profile = getProfile(serverGroup);
       request.put("address", Arrays.asList("profile", profile, "subsystem", "web", "virtual-server", "*"));
     }
 
     List<Object> results = (List<Object>) apiRequest(request);
     if(results != null) {
       for(Object result : results) {
         if(result instanceof Map) {
           Map<?, ?> resultMap = (Map<?, ?>) result;
           if(resultMap.containsKey("result")) {
             Object innerResult = resultMap.get("result");
             if(innerResult instanceof Map) {
               String name = (String) ((Map<?, ?>) innerResult).get("name");
               if(StringUtils.isNotBlank(name)) {
                 vHosts.add(name);
               }
             }
           }
         }
       }
     }
 
     return vHosts;
   }
 
  public Boolean isWarDeployed(String warName) throws com.meltmedia.cadmium.deployer.NoDeploymentFoundException

  {
     Boolean isEnabled = null;
 
     //Build request
     Map<String, Object> request = new LinkedHashMap<String, Object>();
     request.put("operation", "read-resource");
     request.put("recursive", "true");
     if (!isDomainHost()) {
       request.put("address", Arrays.asList("deployment", warName));
     } else {
       String serverGroup = getServerGroup();
       request.put("address", Arrays.asList("server-group", serverGroup, "deployment", warName));
     }
     try {
       Map<String, Object> result = (Map<String, Object>) apiRequest(request);
 
       if (result != null) {
         isEnabled = (Boolean) result.get("enabled");
       }
     } catch (Exception e) {
      throw new com.meltmedia.cadmium.deployer.NoDeploymentFoundException();
     }
 
     return isEnabled;
   }
 
   public File getDeploymentLocation(String warName) throws Exception {
     File warFile = null;
 
     //Build request
     Map<String, Object> request = new LinkedHashMap<String, Object>();
     request.put("operation", "read-resource");
     request.put("recursive", "true");
     request.put("address", Arrays.asList("deployment", warName));
 
     Map<String, Object> result = (Map<String, Object>) apiRequest(request);
 
     if (result != null) {
       logger.debug("Found {} content {}", warName, result.get("content"));
       warFile = getWarFile(result.get("content"));
     }
 
     return warFile;
   }
 
   public List<String> listDeployedCadmiumWars() throws Exception {
     List<String> warList = new ArrayList<String>();
 
     //Build request
     Map<String, Object> request = new LinkedHashMap<String, Object>();
     request.put("operation", "read-resource");
     request.put("recursive", "true");
     if(!isDomainHost()) {
       request.put("address", Arrays.asList("deployment", "*"));
     } else {
       String serverGroup = getServerGroup();
       request.put("address", Arrays.asList("server-group", serverGroup, "deployment", "*"));
     }
 
     List<Object> results = (List<Object>) apiRequest(request);
     if(results != null) {
       for(Object result : results) {
         if(result instanceof Map) {
           Map<?, ?> resultMap = (Map<?, ?>)result;
           if(resultMap.containsKey("result")) {
             Object deploymentObj = resultMap.get("result");
             if(deploymentObj instanceof Map) {
               Map<?, ?> deployment = (Map<?, ?>)deploymentObj;
               String name = (String)deployment.get("name");
               Boolean enabled = (Boolean) deployment.get("enabled");
               if(enabled != null && enabled && name != null) {
                 if(!isDomainHost()) {
                   Object content = deployment.get("content");
                   if(isCadmiumWar(content, logger)) {
                     warList.add(name);
                   }
                 } else {
                   logger.debug("Getting deployment for {}", name);
                   try {
                   File warFile = new File(name);
                   if(warFile != null && isCadmiumWar(warFile)) {
                     warList.add(name);
                   }
                   } catch(Exception e) {
                     logger.warn("Failed to check if "+ name+" is a cadmium war.", e);
                   }
                 }
               }
             }
           }
         }
       }
     }
 
     return warList;
   }
 
   protected Object apiRequest(Map<String, Object> requestObj) throws Exception {
     HttpPost post = new HttpPost("http://"+host+":"+port+"/management");
     Object result = null;
     try {
       post.setEntity(new StringEntity(new GsonBuilder().disableHtmlEscaping().create().toJson(requestObj), ContentType.APPLICATION_JSON));
 
       HttpResponse response = client.execute(post);
       if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
         String responseBody = EntityUtils.toString(response.getEntity());
         Map<String, Object> responseObj = new Gson().fromJson(responseBody, new TypeToken<Map<String, Object>>() {}.getType());
         if("success".equals(responseObj.get("outcome"))) {
           if(responseObj.containsKey("result")) {
             result = responseObj.get("result");
           }
         } else {
           String failureMessage = (String) responseObj.get("failure-description");
           if (failureMessage == null) {
             failureMessage = "Failed to process request.";
           }
           logger.warn(failureMessage);
           throw new Exception("Received " + failureMessage + " response from management api.");
         }
       } else {
         logger.warn("Failed request to JBoss management API: {}", EntityUtils.toString(response.getEntity()));
         throw new Exception("Received " + response.getStatusLine() + " response from management api.");
       }
     } finally {
       post.releaseConnection();
     }
     return result;
   }
 
   public boolean isCadmiumWar(File warFile) {
     String warName = warFile.getName();
     try {
       warFile = getDeploymentLocation(warName);
       return JBossUtil.isCadmiumWar(warFile, logger);
     } catch(Exception e) {
       return false;
     }
   }
 
   protected static boolean isCadmiumWar(Object content, Logger logger) {
     File warFile = getWarFile(content);
     if(warFile != null) {
       return JBossUtil.isCadmiumWar(warFile, logger);
     }
     return false;
   }
 
   private static File getWarFile(Object content) {
     File warFile = null;
     if(content != null) {
       if(content instanceof List) {
         List<Object> contentArr = (List<Object>)content;
         if(contentArr.size() == 1) {
           Object contentEl = contentArr.get(0);
           if(contentEl instanceof Map) {
             Map<?, ?> contentMap = (Map<?, ?>)contentEl;
             if(contentMap.containsKey("hash")) {
               Object hashVal = contentMap.get("hash");
               if(hashVal instanceof Map) {
                 Map<?, ?> hashMap = (Map<?, ?>)hashVal;
                 if(hashMap.containsKey("BYTES_VALUE")) {
                   warFile = decodeByteSValue((String) hashMap.get("BYTES_VALUE"));
                 }
               }
             } else if (contentMap.containsKey("path") && contentMap.containsKey("relative-to")) {
               String path = (String) contentMap.get("path");
               String baseDir = System.getProperty((String) contentMap.get("relative-to"));
               File aFile = new File(baseDir, path);
               if(aFile.exists()) {
                 warFile = aFile;
               }
             }
           }
         }
       }
     }
     return warFile;
   }
 
   protected static File decodeByteSValue(String byteSValue) {
     File deployDir = new File(System.getProperty(DATA_KEY)).getAbsoluteFile();
     byte bytes[] = Base64.decodeBase64(byteSValue);
     String byteStrings[] = new String[bytes.length];
     for(int i=0; i<bytes.length; i++) {
       byteStrings[i] = String.format("%02x", bytes[i]);
     }
     for(int i=1; i<=byteStrings.length; i++) {
       File directory = new File(deployDir, org.apache.commons.lang3.StringUtils.join(byteStrings, "", 0, i));
       if(directory.isDirectory()){
         if(i<byteStrings.length) {
           String childDir = org.apache.commons.lang3.StringUtils.join(byteStrings, "", i, byteStrings.length);
           directory = new File(directory, childDir);
           if(!directory.isDirectory()) {
             continue;
           }
         }
         directory = new File(directory, "content");
         if(directory.exists() && directory.isFile()) {
           return directory;
         }
       }
     }
     return null;
   }
 
 
   @Override
   public void configurationUpdated(Object configuration) {
     configurationNotFound();
     JBossManagementConfiguration config = (JBossManagementConfiguration) configuration;
 
     addAuthentication(config.getUsername(), config.getPassword(), config.getHost(), config.getPort());
   }
 
   @Override
   public void configurationNotFound() {
     IOUtils.closeQuietly(this);
     host = "localhost";
     port = 9990;
   }
 
   @Override
   public void close() throws IOException {
     if(client != null) {
       client.getConnectionManager().shutdown();
       client = null;
     }
     this.domainHost = null;
     this.profile = null;
     this.serverGroup = null;
     this.serverName = null;
     this.hostName = null;
   }
 
   private boolean isDomainHost() {
     if(domainHost != null) {
       return domainHost;
     }
     //Build request
     Map<String, Object> request = new LinkedHashMap<String, Object>();
     request.put("operation", "read-attribute");
     request.put("name", "launch-type");
 
     try {
       domainHost = "DOMAIN".equals(apiRequest(request));
       return domainHost;
     } catch(Exception e) {
       return false;
     }
   }
 
   protected String getServerGroup() {
     if(isDomainHost()) {
       if(serverGroup == null) {
         String nodeName = System.getProperty(NODE_NAME_KEY);
         if(StringUtils.isNotBlank(nodeName) && nodeName.contains(":")) {
           String nodeNameParams[] = nodeName.split("\\:");
           hostName = nodeNameParams[0];
           serverName = System.getProperty(SERVER_NAME_KEY, nodeNameParams[1]);
           //Build request
           Map<String, Object> request = new LinkedHashMap<String, Object>();
           request.put("operation", "read-resource");
           request.put("address", Arrays.asList("host", hostName, "server-config", serverName));
           try {
             Object responseObj = apiRequest(request);
             if(responseObj instanceof Map) {
               Map<String, Object> response = (Map<String, Object>) responseObj;
               serverGroup = (String) response.get("group");
               logger.info("Server group: {}, Host: {}, Server: {}", new Object[] {serverGroup, hostName, serverName});
             }
           } catch (Throwable t) {
             logger.warn("Failed to get server group name.", t);
           }
         }
       }
     }
     return serverGroup;
   }
 
   protected String getProfile(String serverGroup) {
     if(isDomainHost()) {
       if(profile == null) {
         Map<String, Object> request = new LinkedHashMap<String, Object>();
         request.put("operation", "read-resource");
         request.put("address", Arrays.asList("server-group", serverGroup));
         try {
           Object responseObj = apiRequest(request);
           if(responseObj instanceof Map) {
             Map<String, Object> result = (Map<String, Object>) responseObj;
             profile = (String) result.get("profile");
             logger.info("Profile: {}", profile);
           }
         } catch(Throwable t) {
           logger.warn("Failed to get server profile.", t);
         }
       }
     }
     return profile;
   }
 }
