 package org.sharextras.webscripts.connector;
 
 import java.io.ByteArrayInputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.Iterator;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 import org.springframework.extensions.surf.RequestContext;
 import org.springframework.extensions.surf.ServletUtil;
 import org.springframework.extensions.surf.exception.ConnectorServiceException;
 import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
 import org.springframework.extensions.webscripts.Status;
 import org.springframework.extensions.webscripts.connector.Connector;
 import org.springframework.extensions.webscripts.connector.ConnectorService;
 import org.springframework.extensions.webscripts.connector.Credentials;
 import org.springframework.extensions.webscripts.connector.Response;
 import org.springframework.extensions.webscripts.connector.SimpleCredentialVault;
 import org.springframework.extensions.webscripts.connector.User;
 
 /**
  * Vault for storing OAuth 2.0 credentials (an access token and an optional refresh token)
  * 
  * This implementation will only store the credentials specifically required for OAuth 2.0
  * and should not be used for other credentials.
  * 
  * @author wabson
  */
 public class OAuth2CredentialVault extends SimpleCredentialVault
 {
     private static final String API_STORE_TOKEN = "/extras/oauth2/token/%s";
     private static final String ENDPOINT_ALFRESCO = "alfresco";
     private static final String JSON_PROP_PROVIDER_ID = "name";
     private static final String JSON_PROP_ACCESS_TOKEN = "accessToken";
     private static final String JSON_PROP_REFRESH_TOKEN = "refreshToken";
 
     private static Log logger = LogFactory.getLog(OAuth2CredentialVault.class);
     
     private static final long serialVersionUID = 4009102141325723492L;
     
     private Connector alfrescoConnector;
     
     private ConnectorService connectorService;
 
     public OAuth2CredentialVault(String id)
     {
         super(id);
     }
 
     @Override
     public void store(Credentials credentials)
     {
         super.store(credentials);
     }
 
     @Override
     public Credentials retrieve(String endpointId)
     {
         logger.debug("Retrieving credentials");
         Credentials credentials = super.retrieve(endpointId);
         if (credentials == null)
         {
             if (load(endpointId))
             {
                 credentials = super.retrieve(endpointId);
             }
         }
         return credentials;
     }
 
     @Override
     public boolean load()
     {
         // We're not able to load all the persisted endpoint credentials, nor should we!
         return true;
     }
 
     protected boolean load(String endpoint)
     {
         RequestContext context = ThreadLocalRequestContext.getRequestContext();
         User user = context.getUser();
         if (user == null)
         {
             logger.error("Could not locate user object in request context");
             return false;
         }
         String userId = user.getId();
         HttpSession httpSession = ServletUtil.getSession();
         if (httpSession == null)
         {
             logger.error("Could not locate session object in request context");
             return false;
         }
         return load(endpoint, httpSession, userId);
     }
 
     protected boolean load(String endpoint, Connector alfrescoConnector)
     {
         // build a new remote client
         String providerId = endpoint, 
                 tokenUrl = getTokenApi(providerId);
 
         Response response = alfrescoConnector.call(tokenUrl);
         
         if (response.getStatus().getCode() == Status.STATUS_OK)
         {
             try
             {
                 JSONObject jsonObject = new JSONObject(new JSONTokener(response.getText()));
                 String accessToken = jsonObject.getString(JSON_PROP_ACCESS_TOKEN), refreshToken = null;
                 if (jsonObject.has(JSON_PROP_REFRESH_TOKEN) && !"".equals(jsonObject.getString(JSON_PROP_REFRESH_TOKEN)))
                 {
                     refreshToken = jsonObject.getString(JSON_PROP_REFRESH_TOKEN);
                 }
                 Credentials credentials = newCredentials(endpoint);
                 credentials.setProperty(OAuth2Credentials.CREDENTIAL_ACCESS_TOKEN, accessToken);
                 credentials.setProperty(OAuth2Credentials.CREDENTIAL_REFRESH_TOKEN, refreshToken);
                 
                 return true;
             }
             catch (JSONException e)
             {
                 logger.error("Could not parse token response JSON", e);
             }
         }
         else
         {
            logger.error("Received response code " + response.getStatus().getCode() + " from token store");
         }
         
         return false;
     }
 
     private boolean load(String endpoint, HttpSession session, String userId)
     {
         try
         {
             return load(endpoint, getAlfrescoConnector(ENDPOINT_ALFRESCO, userId, session));
         }
         catch (ConnectorServiceException e)
         {
             logger.error("Error while attempting to access Alfresco connector", e);
             return false;
         }
     }
     
     @Override
     public boolean save()
     {
         RequestContext context = ThreadLocalRequestContext.getRequestContext();
         User user = context.getUser();
         if (user == null)
         {
             logger.error("Could not locate user object in request context");
             return false;
         }
         String userId = user.getId();
         HttpSession httpSession = ServletUtil.getSession();
         if (httpSession == null)
         {
             logger.error("Could not locate session object in request context");
             return false;
         }
         return save(httpSession, userId);
     }
 
     public boolean save(HttpSession session, String userId)
     {
         try
         {
             return save(getAlfrescoConnector(ENDPOINT_ALFRESCO, userId, session));
         }
         catch (ConnectorServiceException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
             return false;
         }
     }
 
     public boolean save(Connector alfrescoConnector)
     {
         boolean status = true;
         
         try
         {
             // walk through all of the endpoints
             Iterator<String> it = credentialsMap.keySet().iterator();
             while(it.hasNext())
             {
                 //remoteClient.setRequestContentType(Format.JSON.mimetype());
                 
                 String endpointId = (String) it.next(), 
                         providerId = endpointId, token = "", refreshToken = "";
                 
                 Credentials credentials = retrieve(endpointId);
 
                 token = (String) credentials.getProperty(OAuth2Credentials.CREDENTIAL_ACCESS_TOKEN);
                 refreshToken = (String) credentials.getProperty(OAuth2Credentials.CREDENTIAL_REFRESH_TOKEN);
                 
                 // TODO check that access token and refresh token have values
 
                 JSONObject persistParams = new JSONObject();
                 try
                 {
                     persistParams.put(JSON_PROP_PROVIDER_ID, providerId);
                     persistParams.put(JSON_PROP_ACCESS_TOKEN, token);
                     persistParams.put(JSON_PROP_REFRESH_TOKEN, refreshToken);
                 }
                 catch (JSONException e)
                 {
                     // TODO Throw an exception of the correct type
                     status = false;
                 }
                 
                 // Persist the access token
                 String tokenUrl = getTokenApi(providerId);
                 String postBody = persistParams.toString();
                 
                 if (logger.isDebugEnabled())
                     logger.debug("Sending token data:\n" + postBody);  
                 
                 Response response = alfrescoConnector.call(tokenUrl, null, new ByteArrayInputStream(postBody.getBytes("UTF-8")));
                 
                 // read back the ticket
                 if (response.getStatus().getCode() != Status.STATUS_OK)
                 {
                     status = false;
                     
                     if (logger.isDebugEnabled())
                         logger.debug("Could not store OAuth 2.0 credentials, received response code: " + response.getStatus().getCode());
                     return false;          
                 }
                 else
                 {
                     if (logger.isDebugEnabled())
                         logger.debug("Stored credentials successfully");  
                 }
             }
             return status;
             
         }
         catch (UnsupportedEncodingException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
             return false;
         }
     }
     
     public ConnectorService getConnectorService()
     {
         return connectorService;
     }
 
     public void setConnectorService(ConnectorService connectorService)
     {
         this.connectorService = connectorService;
     }
     
     public Connector getAlfrescoConnector()
     {
         return alfrescoConnector;
     }
     
     public Connector getAlfrescoConnector(String endpoint, String userId, HttpSession session) throws ConnectorServiceException
     {
         return this.alfrescoConnector != null ? this.alfrescoConnector : connectorService.getConnector(ENDPOINT_ALFRESCO, userId, session);
     }
     
     public void setAlfrescoConnector(Connector connector)
     {
         this.alfrescoConnector = connector;
     }
     
     public String getTokenApi(String endpointId)
     {
         return String.format(API_STORE_TOKEN, endpointId);
     }
 
 }
