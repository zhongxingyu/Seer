 package org.sharextras.webscripts;
 
 import java.io.IOException;
 import java.nio.charset.Charset;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 import org.sharextras.webscripts.connector.OAuth2Credentials;
 import org.springframework.extensions.surf.RequestContext;
 import org.springframework.extensions.surf.ServletUtil;
 import org.springframework.extensions.surf.exception.CredentialVaultProviderException;
 import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
 import org.springframework.extensions.surf.util.URLDecoder;
 import org.springframework.extensions.webscripts.AbstractWebScript;
 import org.springframework.extensions.webscripts.Status;
 import org.springframework.extensions.webscripts.WebScriptException;
 import org.springframework.extensions.webscripts.WebScriptRequest;
 import org.springframework.extensions.webscripts.WebScriptResponse;
 import org.springframework.extensions.webscripts.connector.ConnectorService;
 import org.springframework.extensions.webscripts.connector.CredentialVault;
 import org.springframework.extensions.webscripts.connector.Credentials;
 import org.springframework.extensions.webscripts.connector.User;
 
 /**
  * Landing page web script for returning from a 3rd party OAuth 2.0 authorization page.
  * 
  * <p>The script receives a verifier code from the 3rd party and is responsible for 
  * exchanging this (plus the temporary request token) for a permanent access token, and
  * then persisting this into the repository and redirecting the user to their original
  * page.</p>
  * 
  * @author Will Abson
  */
 public class OAuth2Return extends AbstractWebScript
 {
 	/* URL fragments */
 	public static final String URL_PROXY_SERVLET = "/proxy";
 	
 	/* URL Parameter names */
     public static final String PARAM_CODE = "code";
 	public static final String PARAM_CONNECTOR_ID = "cid";
 	public static final String PARAM_ENDPOINT_ID = "eid";
 	public static final String PARAM_PROVIDER_ID = "pid";
     public static final String PARAM_REDIRECT_PAGE = "rp";
     public static final String PARAM_STATE = "state";
 	
 	/* Connector property names */
 	public static final String PROP_ACCESS_TOKEN_PATH = "access-token-path";
 	
	/* Vault provider class */
 	public static final String VAULT_PROVIDER_ID = "oAuth2CredentialVaultProvider";
 
     private static Log logger = LogFactory.getLog(OAuth2Return.class);
     
     private String endpointId;
     private String clientId;
     private String clientSecret;
     private String accessTokenUrl;
     
     private ConnectorService connectorService;
 
 	/**
 	 * Web Script constructor
 	 */
 	public OAuth2Return()
 	{
 	}
 
 	@Override
 	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException
 	{
 		String code = req.getParameter(PARAM_CODE), // mandatory
 			endpointName = req.getParameter(PARAM_ENDPOINT_ID),
 			tokenName = req.getParameter(PARAM_PROVIDER_ID);
 		
 		// If values are not supplied as parameters then look these up from the script properties
 		
         if (endpointName == null)
         {
             endpointName = getEndpointId();
         }
 
 		req.getExtensionPath();
 		
 		if (code == null || code.length() == 0)
 		{
 			throw new WebScriptException("No OAuth return code was found");
 		}
 		if (tokenName == null || tokenName.length() == 0)
 		{
 			throw new WebScriptException("No token name was specified");
 		}
 		
         if (logger.isDebugEnabled())
         {
             logger.debug("Received OAuth return code " + code);
         }
         
         RequestContext context = ThreadLocalRequestContext.getRequestContext();
         User user = context.getUser();
         String userId = user.getId();
         HttpSession httpSession = ServletUtil.getSession();
         CredentialVault credentialVault;
         try
         {
             credentialVault = connectorService.getCredentialVault(httpSession, userId, VAULT_PROVIDER_ID);
         }
         catch (CredentialVaultProviderException e)
         {
             throw new WebScriptException("Unable to obtain credential vault for OAuth credentials", e);
         }
 
         String accessToken = null, refreshToken = "";
         
         // TODO return a map or object, not a JSON object here
 		JSONObject authParams = requestAccessToken(null, code, req);
 		
 		if (logger.isDebugEnabled())
 		{
             logger.debug("Token data returned");
             try
             {
                 // TODO use constants for parameter names
                 if (authParams.has("access_token"))
                 {
                     logger.debug("access_token: " + authParams.getString("access_token"));
                     accessToken = authParams.getString("access_token");
                 }
                 if (authParams.has("instance_url"))
                 {
                     logger.debug("instance_url: " + authParams.getString("instance_url"));
                 }
                 if (authParams.has("refresh_token"))
                 {
                     logger.debug("refresh_token: " + authParams.getString("refresh_token"));
                     refreshToken = authParams.getString("refresh_token");
                 }
             }
             catch (JSONException e)
             {
                 throw new WebScriptException("Error parsing access token response", e);
             }
 		}
 		
 		if (accessToken == null)
 		{
 	        throw new WebScriptException("No access token was found but this is required");
 		}
 		
 		// Persist the access token
 		Credentials c = credentialVault.retrieve(endpointName);
 		if (c == null)
 		{
 		    c = credentialVault.newCredentials(endpointName);
 		}
         c.setProperty(OAuth2Credentials.CREDENTIAL_ACCESS_TOKEN, accessToken);
         c.setProperty(OAuth2Credentials.CREDENTIAL_REFRESH_TOKEN, refreshToken);
         credentialVault.save();
         
 		executeRedirect(req, resp);
 	}
 	
 	/**
 	 * Obtain a permanent access token from the OAuth service, utilising the OAuth connector to
 	 * perform the necessary signing of requests.
 	 * 
 	 * TODO Check if we can make this more secure by auto-finding the endpoint name
 	 * 
 	 * @param endpointName
 	 * @param verifier
 	 * @param req
 	 * @param oauthConnector
 	 * @return
 	 * @throws HttpException
 	 * @throws IOException
 	 */
 	private JSONObject requestAccessToken(
 			String endpointName, 
 			String verifier,
 			WebScriptRequest req) throws HttpException, IOException
 	{
 	    // TODO use an endpoint to make this connection
 	    
 		HttpClient client = new HttpClient();
 		
 		String tokenUrl = getAccessTokenUrl(),
 		        postUri = endpointName != null ?
 		        req.getServerPath() + req.getContextPath() + URL_PROXY_SERVLET + "/" + 
 		        endpointName + tokenUrl : tokenUrl;
 		
 		PostMethod method = new PostMethod(postUri);
 		
 		if (logger.isDebugEnabled())
 		{
 		    logger.debug("Received OAuth return code " + verifier);
 		}
 		
         String baseUrl = req.getURL();
         if (baseUrl.indexOf('?') != -1)
             baseUrl = baseUrl.substring(0, baseUrl.indexOf('?'));
 		
 		method.addParameter("code", verifier);
 		method.addParameter("grant_type", "authorization_code");
 		method.addParameter("redirect_uri", req.getServerPath() + baseUrl);
 		
 		// Add client ID and secret if specified in the config
         if (clientId != null)
         {
             method.addParameter("client_id", clientId);
         }
         if (clientSecret != null)
         {
             method.addParameter("client_secret", clientSecret);
         }
 		
 		int statusCode = client.executeMethod(method);
 		
 		// errors may be {"error":"invalid_grant","error_description":"expired authorization code"}
 		// or {"error":"redirect_uri_mismatch","error_description":"redirect_uri must match configuration"}
 
         byte[] responseBody = method.getResponseBody();
         String tokenResp = new String(responseBody, Charset.forName("UTF-8"));
         
 	    // do something with the input stream, which contains the new parameters in the body
 	    if (logger.isDebugEnabled())
         {
             logger.debug("Received token response " + tokenResp);
         }
 	    
         try
         {
             JSONObject authResponse = new JSONObject(new JSONTokener(tokenResp));
             if (statusCode == Status.STATUS_OK)
             {
                 return authResponse;
             }
             else
             {
                 @SuppressWarnings("unused")
                 String errorDesc = authResponse.getString("error_description"),
                     errorName = authResponse.getString("error");
                 throw new WebScriptException(statusCode, "A problem occurred while requesting the access token" + 
                         (errorDesc != null ? " - " + errorDesc : ""));
             }
         }
         catch (JSONException e)
         {
             throw new WebScriptException("A problem occurred parsing the JSON response from the provider");
         }
         
 	}
 	
 	/**
 	 * Redirect the user to the location that was specified in the request parameter, or
 	 * to the webapp context root if this was not found
 	 * 
 	 * @param req
 	 * @param resp
 	 */
 	private void executeRedirect(WebScriptRequest req, WebScriptResponse resp)
 	{
 	    String redirectPage = null, state = req.getParameter(PARAM_STATE);
 	    if (req.getParameter(PARAM_REDIRECT_PAGE) != null)
 	    {
 	        redirectPage = req.getParameter(PARAM_REDIRECT_PAGE).indexOf('/') == 0 ? 
 	                req.getParameter(PARAM_REDIRECT_PAGE) : 
 	                    "/" + req.getParameter(PARAM_REDIRECT_PAGE);
 	    }
 	    else if (state != null) // TODO extract into utility method
 	    {
             if (logger.isDebugEnabled())
                 logger.debug("Found state: " + state);
             String rp = null;
             String[] parts = state.split("&");
             for (String s : parts) {
                 String[] pair = s.split("=");
                 if (pair.length == 2)
                     if (PARAM_REDIRECT_PAGE.equals(URLDecoder.decode(pair[0])))
                         rp = URLDecoder.decode(pair[1]);
             }
             if (rp != null)
                 redirectPage = rp.indexOf('/') == 0 ? rp : "/" + rp;
 	    }
 		String redirectLocation = req.getServerPath() + req.getContextPath() + (redirectPage != null ? redirectPage : "");
         if (logger.isDebugEnabled())
             logger.debug("Redirecting user to URL " + redirectLocation);
 		resp.addHeader(WebScriptResponse.HEADER_LOCATION, redirectLocation);
 		resp.setStatus(Status.STATUS_MOVED_TEMPORARILY);
 	}
 
     public String getEndpointId()
     {
         return endpointId;
     }
 
     public void setEndpointId(String endpointId)
     {
         this.endpointId = endpointId;
     }
 
     public String getClientId()
     {
         return clientId;
     }
 
     public void setClientId(String clientId)
     {
         this.clientId = clientId;
     }
 
     public String getClientSecret()
     {
         return clientSecret;
     }
 
     public void setClientSecret(String clientSecret)
     {
         this.clientSecret = clientSecret;
     }
     
     public String getAccessTokenUrl()
     {
         return accessTokenUrl;
     }
 
     public void setAccessTokenUrl(String accessTokenUrl)
     {
         this.accessTokenUrl = accessTokenUrl;
     }
 
     public ConnectorService getConnectorService()
     {
         return connectorService;
     }
 
     public void setConnectorService(ConnectorService connectorService)
     {
         this.connectorService = connectorService;
     }
 
 }
