 /*
  * Copyright (c) Istituto Nazionale di Fisica Nucleare (INFN). 2006-2010.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package it.grid.storm.https;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpHead;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import it.grid.storm.filesystem.FilesystemPermission;
 import it.grid.storm.filesystem.LocalFile;
 import it.grid.storm.griduser.LocalUser;
 import it.grid.storm.https.HTTPSPluginInterface;
 import it.grid.storm.https.configuration.Configurator;
 import it.grid.storm.https.remotecall.GridHttpsServiceConstants;
 import it.grid.storm.https.remotecall.MapperServiceConstants;
 
 /**
  * @author Michele Dibenedetto
  */
 public class GhttpsHTTPSPluginInterface implements HTTPSPluginInterface
 {
 
 
     private final Logger log = LoggerFactory.getLogger(GhttpsHTTPSPluginInterface.class);
     private int serverUserUID;
     private int serverUserGID;
     private int serverMappingPort;
     private LocalUser serverLocalUser;
 
 
     public GhttpsHTTPSPluginInterface() throws InstantiationException
     {
         String configurationFilePath = GridHttpsServiceConstants.CONFIGURATION_FILE_LOCATION_PATH + File.separatorChar
                 + GridHttpsServiceConstants.CONFIGURATION_FILE_NAME;
         try
         {
             Configurator.init(configurationFilePath);
         }
         catch (ConfigurationException e)
         {
             log.error("Unable to initialize the Configurator object! ConfigurationException: " + e.getMessage());
             throw new InstantiationException("Error during Configurator initialization");
         }
         try
         {
             serverUserUID = Configurator.getServerUserUID();
             serverUserGID = Configurator.getServerUserGID();
             serverMappingPort = Configurator.getMappingPort();
         }
         catch (ConfigurationException e)
         {
             log.error("Unable to get all the mandatory configuration parameters. ConfigurationException: " + e.getMessage());
             throw new InstantiationException("Error during mandatory configuration parameters retrieving");
         }
         setLocaUser();
     }
 
 
     private void setLocaUser()
     {
         serverLocalUser = new LocalUser(serverUserUID, serverUserGID);
     }
 
 
     /*
      * (non-Javadoc)
      * @see it.grid.storm.https.HTTPSPluginInterface#grantGroupPermission(it.grid.storm.filesystem.LocalFile,
      * it.grid.storm.griduser.LocalUser, it.grid.storm.filesystem.FilesystemPermission)
      */
     @Override
     public void grantGroupPermission(LocalFile localFile, LocalUser localUser, FilesystemPermission permission)
     {
         grantServiceGroupPermission(localFile, permission);
     }
 
 
     @Override
     public void grantServiceGroupPermission(LocalFile localFile, FilesystemPermission permission)
     {
         localFile.grantGroupPermission(serverLocalUser, permission);
         log.debug("Granted group permission " + permission.toString() + " on file " + localFile.toString() + " to service group " + serverLocalUser.toString());
     }
     
 
     /*
      * (non-Javadoc)
      * @see it.grid.storm.https.HTTPSPluginInterface#grantUserPermission(it.grid.storm.filesystem.LocalFile,
      * it.grid.storm.griduser.LocalUser, it.grid.storm.filesystem.FilesystemPermission)
      */
     @Override
     public void grantUserPermission(LocalFile localFile, LocalUser localUser, FilesystemPermission permission)
     {
         grantServiceUserPermission(localFile, permission);
     }
     
     
     @Override
     public void grantServiceUserPermission(LocalFile localFile, FilesystemPermission permission)
     {
         localFile.grantUserPermission(serverLocalUser, permission);
         log.debug("Granted user permission " + permission.toString() + " on file " + localFile.toString() + " to service user " + serverLocalUser.toString());
     }
 
 
     /*
      * (non-Javadoc)
      * @see it.grid.storm.https.HTTPSPluginInterface#removeGroupPermission(it.grid.storm.filesystem.LocalFile,
      * it.grid.storm.griduser.LocalUser)
      */
     @Override
     public void removeGroupPermission(LocalFile localFile, LocalUser localUser)
     {
         localFile.removeGroupPermission(serverLocalUser);
         log.debug("Removed group permission from file " + localFile.toString() + " to group " + localUser.toString());
     }
 
 
     /*
      * (non-Javadoc)
      * @see it.grid.storm.https.HTTPSPluginInterface#removeUserPermission(it.grid.storm.filesystem.LocalFile,
      * it.grid.storm.griduser.LocalUser)
      */
     @Override
     public void removeUserPermission(LocalFile localFile, LocalUser localUser)
     {
         localFile.removeUserPermission(serverLocalUser);
         log.debug("Removed user permission from file " + localFile.toString() + " to user " + localUser.toString());
     }
 
 
     /*
      * (non-Javadoc)
      * @see it.grid.storm.https.HTTPSPluginInterface#revokeGroupPermission(it.grid.storm.filesystem.LocalFile,
      * it.grid.storm.griduser.LocalUser, it.grid.storm.filesystem.FilesystemPermission)
      */
     @Override
     public void revokeGroupPermission(LocalFile localFile, LocalUser localUser, FilesystemPermission permission)
     {
         localFile.revokeGroupPermission(serverLocalUser, permission);
         log.debug("Revoked group permission " + permission.toString() + " from file " + localFile.toString() + " to group " +
                   localUser.toString());
     }
 
 
     /*
      * (non-Javadoc)
      * @see it.grid.storm.https.HTTPSPluginInterface#revokeUserPermission(it.grid.storm.filesystem.LocalFile,
      * it.grid.storm.griduser.LocalUser, it.grid.storm.filesystem.FilesystemPermission)
      */
     @Override
     public void revokeUserPermission(LocalFile localFile, LocalUser localUser, FilesystemPermission permission)
     {
         localFile.revokeUserPermission(serverLocalUser, permission);
         log.debug("Revoked user permission " + permission.toString() + " from file " + localFile.toString() + " to user " + localUser.toString());
     }
 
 
     /*
      * (non-Javadoc)
      * @see it.grid.storm.https.HTTPSPluginInterface#setGroupPermission(it.grid.storm.filesystem.LocalFile,
      * it.grid.storm.griduser.LocalUser, it.grid.storm.filesystem.FilesystemPermission)
      */
     @Override
     public void setGroupPermission(LocalFile localFile, LocalUser localUser, FilesystemPermission permission)
     {
         localFile.setGroupPermission(serverLocalUser, permission);
         log.debug("Setted group permission " + permission.toString() + " on file " + localFile.toString() + " to group " + localUser.toString());
     }
 
 
     /*
      * (non-Javadoc)
      * @see it.grid.storm.https.HTTPSPluginInterface#setUserPermission(it.grid.storm.filesystem.LocalFile, it.grid.storm.griduser.LocalUser,
      * it.grid.storm.filesystem.FilesystemPermission)
      */
     @Override
     public void setUserPermission(LocalFile localFile, LocalUser localUser, FilesystemPermission permission)
     {
         localFile.setUserPermission(serverLocalUser, permission);
         log.debug("Setted user permission " + permission.toString() + " on file " + localFile.toString() + " to user " + localUser.toString());
     }
 
 
     @Override
     public void removeAllPermissions(LocalFile localFile)
     {
         localFile.removeGroupPermission(serverLocalUser);
         localFile.removeUserPermission(serverLocalUser);
         log.debug("Removing all permissions from file " + localFile.toString());
     }
 
     /**
      * Do nothing, permissions are stored as ACLs and moved with the file
      */
     @Override
     public void moveAllPermissions(LocalFile fromLocalFile, LocalFile toLocalFile)
     {
         log.debug("Permissions moving, from file " + fromLocalFile.toString() + " to file " + toLocalFile.toString()
                 + " . Nothing to do, permissions are moved with the file");
     }
 
     /* (non-Javadoc)
      * @see it.grid.storm.https.HTTPSPluginInterface#MapLocalPath(java.lang.String)
      */
     @Override
     public String mapLocalPath(String serverHost, String localAbsolutePath) throws HTTPSPluginException
     {
         log.debug("Mapping local path " + localAbsolutePath + " to gridhttp relative URL");
         URI uri = buildMapperServiceUri(localAbsolutePath);
         log.debug("Mapping Service call uri = " + uri.toString());
         HttpGet httpget = new HttpGet(uri);
         HttpClient httpclient = new DefaultHttpClient();
         HttpResponse httpResponse;
         try
         {
             httpResponse = httpclient.execute(httpget);
         }
         catch (ClientProtocolException e)
         {
             log.error("Error executing http call. ClientProtocolException " + e.getLocalizedMessage());
             throw new HTTPSPluginException("Error contacting Mapping Service.");
         }
         catch (IOException e)
         {
             log.error("Error executing http call. IOException " + e.getLocalizedMessage());
             throw new HTTPSPluginException("Error contacting Mapping Service.");
         }
         StatusLine status = httpResponse.getStatusLine();
         if (status == null)
         {
             // never return null
             log.error("Unexpected error! response.getStatusLine() returned null!");
             throw new HTTPSPluginException("Unexpected error! response.getStatusLine() returned null! Please contact storm support");
         }
         int httpCode = status.getStatusCode();
         String httpMessage = status.getReasonPhrase();
         HttpEntity entity = httpResponse.getEntity();
         String output = "";
         if (entity != null)
         {
             InputStream responseIS;
             try
             {
                 responseIS = entity.getContent();
             }
             catch (IllegalStateException e)
             {
                 log.error("Unable to get the input content stream from server answer. IllegalStateException "
                         + e.getLocalizedMessage());
                 throw new HTTPSPluginException("Error comunicationg with the Mapping Service.");
             }
             catch (IOException e)
             {
                 log.error("Unable to get the input content stream from server answer. IOException "
                         + e.getLocalizedMessage());
                 throw new HTTPSPluginException("Error comunicationg with the Mapping Servicee.");
             }
             int l;
             byte[] tmp = new byte[1024];
             try
             {
                 while ((l = responseIS.read(tmp)) != -1)
                 {
                     output += new String(tmp, 0, l);
                 }
             }
             catch (IOException e)
             {
                 log.error("Error reading from the connection error stream. IOException " + e.getMessage());
                 throw new HTTPSPluginException("Error comunicationg with the Mapping Service.");
             }
         }
         else
         {
             log.error("No HttpEntity found in the response. Unable to determine the answer");
             throw new HTTPSPluginException("Unable to get a valid Mapping response from the server.");
         }
         log.debug("Mapping Service response is : \'" + output + "\'");
         if (httpCode != HttpURLConnection.HTTP_OK)
         {
             log.error("Unable to get a valid response from server. Received a non HTTP 200 response from the server : \'"
                     + httpCode + "\' " + httpMessage);
             throw new HTTPSPluginException("Unable to get a valid response from server. " + httpMessage);
         }
         String URLPath = decodeHttpsPath(output);
         log.info("Mapped local absolute path \'" + localAbsolutePath + "\' to \'" + URLPath + "\' gridhttp relative URL");
         return URLPath;
     }
 
     /**
      * @param localAbsolutePath
      * @return
      * @throws HTTPSPluginException
      */
     private URI buildMapperServiceUri(String serverHost, String localAbsolutePath) throws HTTPSPluginException
     {
         log.debug("Encoding parameters");
         List<NameValuePair> qparams = new ArrayList<NameValuePair>();
         qparams.add(new BasicNameValuePair(MapperServiceConstants.RESOURCE_MAPPING_PATH_KEY, localAbsolutePath));
         URI uri;
         try
         {
             uri = new URI("http", null, serverHost, serverMappingPort, MapperServiceConstants.SERVICE_PATH, URLEncodedUtils.format(qparams, "UTF-8"), null);
         }
         catch (URISyntaxException e)
         {
             log.error("Unable to create Mapper Service URI. URISyntaxException " + e.getLocalizedMessage());
             throw new HTTPSPluginException("Unable to create Mapper Service URI");
         }
         return uri;
     }
 
     /**
      * @param output
      * @return
      */
     private static String decodeHttpsPath(String output)
     {
         String httpsPath = "";
         httpsPath = output.trim();
         return httpsPath;
     }
     
     @Override
     public ServiceStatus getServiceStatus(String hostname, int port, Protocol protocol) throws HTTPSPluginException
     {
         log.debug("Checking server status for : " + protocol + hostname + ":" + port);
         URI uri;
         switch (protocol)
         {
             case HTTP:
                 try
                 {
                     uri = new URI(protocol, null, hostname, port, "/", null, null);
                 }
                 catch (URISyntaxException e)
                 {
                     log.error("Unable to create Server root URI. URISyntaxException " + e.getLocalizedMessage());
                     throw new HTTPSPluginException("Unable to create Server root URI");
                 }                
                 break;
             case HTTPS:
                 //TODO move to querying the https service using local host certificate and truststore certificate 
                 try
                 {
                     uri = new URI("http", null, hostname, serverMappingPort, MapperServiceConstants.SERVICE_PATH, null, null);
                 }
                 catch (URISyntaxException e)
                 {
                     log.error("Unable to create Server root URI. URISyntaxException " + e.getLocalizedMessage());
                     throw new HTTPSPluginException("Unable to create Server root URI");
                 }                
                 break;
             default:
                 break;
         }
         
         log.debug("Server root uri = " + uri.toString());
         HttpHead httphead = new HttpHead(uri);
         HttpClient httpclient = new DefaultHttpClient();
         
         HttpResponse httpResponse;
         try
         {
             httpResponse = httpclient.execute(httphead);
         }
         catch (ClientProtocolException e)
         {
             log.error("Error executing http call. ClientProtocolException " + e.getLocalizedMessage());
             throw new HTTPSPluginException("Error contacting Mapping Service.");
         }
         catch (IOException e)
         {
             log.error("Error executing http call. IOException " + e.getLocalizedMessage());
             return ServiceStatus.NOT_RESPONDING; 
         }
         
         StatusLine status = httpResponse.getStatusLine();
         if (status == null)
         {
             // never return null
             log.error("Unexpected error! response.getStatusLine() returned null!");
             throw new HTTPSPluginException("Unexpected error! response.getStatusLine() returned null! Please contact StoRM support");
         }
         int httpCode = status.getStatusCode();
         if(httpCode == HttpStatus.SC_OK)
         {
             return ServiceStatus.RUNNING;
         }
        return ServiceStatus.UNEXPECTED_BEHAVIOUR
     }
 
 }
