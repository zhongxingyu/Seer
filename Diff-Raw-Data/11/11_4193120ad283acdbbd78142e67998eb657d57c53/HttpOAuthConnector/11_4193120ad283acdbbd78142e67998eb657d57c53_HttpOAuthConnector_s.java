 /**
  * Copyright (C) 20010-2011 Share Extras contributors.
  *
  * This file is part of the Share Extras project.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.sharextras.webscripts.connector;
 
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.crypto.Mac;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.SecretKeySpec;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.extensions.config.RemoteConfigElement.ConnectorDescriptor;
 import org.springframework.extensions.surf.util.Base64;
 import org.springframework.extensions.surf.util.URLEncoder;
 import org.springframework.extensions.webscripts.connector.ConnectorContext;
 import org.springframework.extensions.webscripts.connector.EndpointManager;
 import org.springframework.extensions.webscripts.connector.HttpConnector;
 import org.springframework.extensions.webscripts.connector.RemoteClient;
 import org.springframework.extensions.webscripts.connector.Response;
 import org.springframework.extensions.webscripts.connector.ResponseStatus;
 
 public class HttpOAuthConnector extends HttpConnector
 {
 	public static final String PARAM_CONSUMER_KEY = "consumer-key";
 	public static final String PARAM_CONSUMER_SECRET = "consumer-secret";
 	public static final String PARAM_SIGNATURE_METHOD = "signature-method";
 	public static final String PARAM_VERSION = "version";
 
 	public static final String SIGNATURE_METHOD_PLAINTEXT = "PLAINTEXT";
 	public static final String SIGNATURE_METHOD_HMACSHA1 = "HMAC-SHA1";
 	public static final String SIGNATURE_METHOD_DEFAULT = SIGNATURE_METHOD_PLAINTEXT;
 
 	public static final String HEADER_AUTHORIZATION = "Authorization";
 	public static final String HEADER_AUTHORIZATION_TOKEN_OAUTH = "OAuth";
 	public static final String HEADER_OAUTH_DATA = "X-OAuth-Data";
 
 	public static final String OAUTH_REALM = "oauth_realm";
 	public static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
 	public static final String OAUTH_CONSUMER_SECRET = "oauth_consumer_secret";
 	public static final String OAUTH_TOKEN = "oauth_token";
 	public static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
 	public static final String OAUTH_NONCE = "oauth_nonce";
 	public static final String OAUTH_TIMESTAMP = "oauth_timestamp";
 	public static final String OAUTH_SIGNATURE = "oauth_signature";
 	public static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
 	public static final String OAUTH_VERSION = "oauth_version";
 	public static final String OAUTH_VERSION_1 = "1.0";
 	public static final String OAUTH_VERSION_DEFAULT = OAUTH_VERSION_1;
 	
     private static Log logger = LogFactory.getLog(HttpOAuthConnector.class);
     
 	public HttpOAuthConnector(ConnectorDescriptor descriptor,
 			String endpoint) {
 		super(descriptor, endpoint);
 	}
 	
 	private String getConsumerKey()
 	{
 		return descriptor.getStringProperty(PARAM_CONSUMER_KEY);
 	}
 	
 	private String getConsumerSecret()
 	{
 		return descriptor.getStringProperty(PARAM_CONSUMER_SECRET);
 	}
 	
 	private String getSignatureMethod()
 	{
 		if (descriptor.getStringProperty(PARAM_SIGNATURE_METHOD) != null)
 		{
 			return descriptor.getStringProperty(PARAM_SIGNATURE_METHOD);
 		}
 		else
 		{
 			return SIGNATURE_METHOD_DEFAULT;
 		}
 	}
 	
 	private String getVersion()
 	{
 		if (descriptor.getStringProperty(PARAM_VERSION) != null)
 		{
 			return descriptor.getStringProperty(PARAM_VERSION);
 		}
 		else
 		{
 			return OAUTH_VERSION_DEFAULT;
 		}
 	}
 	
 	private String generateSignature(
 			Map<String, String> authParams, 
 			Map<String, String> extraParams, 
 			String httpMethod, 
 			String url)
 	{
 		Map<String, String> sigParams = new HashMap<String, String>(authParams);
 		if (extraParams != null)
 			sigParams.putAll(extraParams);
 		
 		String sigMethod = sigParams.get(OAUTH_SIGNATURE_METHOD);
 		
 		if (sigMethod.equals(SIGNATURE_METHOD_PLAINTEXT))
 		{
     		if (logger.isDebugEnabled())
     			logger.debug("Generating PLAINTEXT signature");
 			String tokenSecret = authParams.get(OAUTH_TOKEN_SECRET);
 			StringBuffer signatureBuffer = new StringBuffer(getConsumerSecret()).append("&");
 			signatureBuffer.append(tokenSecret != null ? tokenSecret : "");
 			return signatureBuffer.toString();
 		}
 		else if (sigMethod.equals(SIGNATURE_METHOD_HMACSHA1))
 		{
     		if (logger.isDebugEnabled())
     			logger.debug("Generating HMAC-SHA1 signature");
     		
 			StringBuffer baseStrBuffer = new StringBuffer();
 			
 			baseStrBuffer.append(httpMethod).append("&");
 			baseStrBuffer.append(encodeParameter(url));
 			baseStrBuffer.append("&");
 			
 			// Add all request params to the list, combine request and auth params in a single map
 			// as per http://tools.ietf.org/html/rfc5849#section-3.4.1.3.1
 			// TODO Support multiple parameters with same name
 			
 			// Sort keys by param name
 			// TODO Sort *after* encoding
 			List<String> keys = new ArrayList<String>(sigParams.keySet());
 			Collections.sort(keys);
 			int i = 0;
 			for (String key : keys)
 			{
 				if (!key.equals(OAUTH_REALM) && !key.equals(OAUTH_SIGNATURE) && !key.equals(OAUTH_TOKEN_SECRET))
 				{
 					if (i > 0)
 						baseStrBuffer.append(encodeParameter("&"));
 					baseStrBuffer.append(encodeParameter(
 							encodeParameter(key) + "=" +
 							encodeParameter(sigParams.get(key))));
 					i ++;
 				}
 			}
 			
 			// Final base string
 			String baseString = baseStrBuffer.toString();
 			
 			// Key to use for signing
 			String tokenSecret = authParams.get(OAUTH_TOKEN_SECRET);
 			String key = encodeParameter(getConsumerSecret()) + "&" + 
 				encodeParameter(tokenSecret != null ? tokenSecret : "");
 
     		if (logger.isDebugEnabled())
     			logger.debug("Generating signature with key '" + key + "', base string '" + baseString + "'");
     		
 			try
 			{
 				SecretKey keyStr = new SecretKeySpec(key.getBytes(), "HmacSHA1");
 				Mac m = Mac.getInstance("HmacSHA1");
 				m.init(keyStr);
 				m.update(baseString.getBytes());
 				byte[] mac = m.doFinal();
 				return new String(Base64.encodeBytes(mac)).trim();
 			}
 			catch (NoSuchAlgorithmException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (InvalidKeyException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 		else
 		{
 			throw new UnsupportedOperationException();
 		}
 	}
     
     private void signRequest(Map<String, String> authParams, Map<String, String> otherParams, String httpMethod, String url)
     {
 		if (!authParams.containsKey(OAUTH_SIGNATURE))
 		{
 			String signature = generateSignature(authParams, otherParams, httpMethod, url);
     		if (logger.isDebugEnabled())
     			logger.debug("Signing request with signature " + signature);
 			authParams.put(OAUTH_SIGNATURE, signature);
 		}
     }
     
     private void applyAuthParams(Map<String, String> authParams)
     {
     	if (!authParams.containsKey(OAUTH_CONSUMER_KEY))
 		{
 			authParams.put(OAUTH_CONSUMER_KEY, getConsumerKey());
 		}
 		if (!authParams.containsKey(OAUTH_NONCE)) // NONCE value - unique in each request
 		{
 			Random randomGenerator = new Random();
 			authParams.put(OAUTH_NONCE, String.valueOf(randomGenerator.nextInt(1000000000)));
 		}
 		if (!authParams.containsKey(OAUTH_TIMESTAMP)) // Timestamp (must be accurate)
 		{
 			authParams.put(OAUTH_TIMESTAMP, String.valueOf((new Date()).getTime()/1000));
 		}
 		if (!authParams.containsKey(OAUTH_SIGNATURE_METHOD)) // Signature method
 		{
 			authParams.put(OAUTH_SIGNATURE_METHOD, getSignatureMethod());
 		}
 		if (!authParams.containsKey(OAUTH_VERSION)) // OAuth version, optional
 		{
 			authParams.put(OAUTH_VERSION, getVersion());
 		}
     }
     
     private String encodeParameter(String p)
     {
     	String encoded = URLEncoder.encodeUriComponent(p);
     	
     	StringBuffer sb = new StringBuffer(encoded.length());
     	Pattern pattern = Pattern.compile("%[0-9a-f]{2}");
     	Matcher m = pattern.matcher(encoded);
     	int lastEnd = 0;
     	while (m.find())
     	{
 			sb.append(encoded.substring(lastEnd, m.start())).append(m.group().toUpperCase(Locale.ENGLISH));
 			lastEnd = m.end();
 		}
     	sb.append(encoded.substring(lastEnd));
		return sb.toString();
     }
 	
     @SuppressWarnings("unchecked")
 	public Response call(String uri, ConnectorContext context, HttpServletRequest req, HttpServletResponse res)
     {
     	String httpMethod = (context != null ? context.getMethod().toString() : "GET");
     	
         if (logger.isDebugEnabled())
             logger.debug("Requested Method: " + httpMethod + " URI: " + uri);
         
         Response response = null;
         if (EndpointManager.allowConnect(this.endpoint))
         {
             RemoteClient remoteClient = initRemoteClient(context);
             
     		String baseUrl = uri;
     		if (baseUrl.indexOf('?') != -1)
     			baseUrl = baseUrl.substring(0, baseUrl.indexOf('?'));
 
     		String absUrl = baseUrl.startsWith(this.endpoint) ? baseUrl : this.endpoint + baseUrl;
 
     		// Build up a Map with all request parameters
     		Map<String, String> reqParams = new HashMap<String, String>(req.getParameterMap().size());
     		for (Enumeration<String> pn = req.getParameterNames(); pn.hasMoreElements();) {
     			String k = pn.nextElement();
 				reqParams.put(k, req.getParameter(k));
 			}
     		
         	String authHdrStr = req.getHeader(HEADER_OAUTH_DATA);
         	if (authHdrStr != null && !authHdrStr.equals(""))
         	{
         		if (logger.isDebugEnabled())
         			logger.debug("Found OAuth header data " + authHdrStr);
 
     			Pattern p = Pattern.compile("(.+)=\"(.+)\"");
         		String[] parts = authHdrStr.split(",");
         		Map<String, String> authParams = new HashMap<String, String>(parts.length);
         		for (int i = 0; i < parts.length; i++)
         		{
         			Matcher m = p.matcher(parts[i]);
         			if (m.matches())
         			{
         				authParams.put(m.group(1), m.group(2));
         			}
 				}
         		
                 // Fill in missing values
         		this.applyAuthParams(authParams);
         		
         		// Sign request - adds outh_signature param if not already present
         		// TODO Check uri does not contain query string
         		this.signRequest(authParams, reqParams, httpMethod, absUrl);
         		
         		// Build the OAuth header
         		StringBuffer authBuffer = new StringBuffer(HEADER_AUTHORIZATION_TOKEN_OAUTH).append(" ");
         		int i = 0;
         		for (Map.Entry<String, String> entry : authParams.entrySet())
         		{
 					if (!entry.getKey().equals(OAUTH_TOKEN_SECRET)) // only used for signing, should not be forwarded
 					{
 						if (i > 0)
 							authBuffer.append(",");
 						authBuffer.append(encodeParameter(entry.getKey())).
 							append("=\"").
 							append(encodeParameter(entry.getValue())).
 							append("\"");
 						i ++;
 					}
 				}
         		
         		if (logger.isDebugEnabled())
         			logger.debug("Adding Authorization header with data: " + authBuffer.toString());
         		
         		Map<String, String> headers = new HashMap<String, String>(1);
         		headers.put(HEADER_AUTHORIZATION, authBuffer.toString());
         		
         		// Overwrite the old X-OAuth-Data header (we can't explicitly remove it)
         		headers.put(HEADER_OAUTH_DATA, "");
         		
         		remoteClient.setRequestProperties(headers);
         	}
         	else
         	{
         		// Support URL params as well as header-based
         		if (logger.isDebugEnabled())
         			logger.debug("Falling back to request parameters for authentication data");
 
                 // Fill in missing values
         		this.applyAuthParams(reqParams);
         		
         		// Sign request - adds outh_signature param if not already present
         		// TODO Check uri does not contain query string
         		this.signRequest(reqParams, null, httpMethod, absUrl);
         		
         		StringBuffer queryStrBuffer = new StringBuffer("?");
         		int i = 0;
         		for (Map.Entry<String, String> entry : reqParams.entrySet())
         		{
 					if (!entry.getKey().equals(OAUTH_TOKEN_SECRET)) // only used for signing, should not be forwarded
 					{
 						if (i > 0)
 							queryStrBuffer.append("&");
 						queryStrBuffer.append(encodeParameter(entry.getKey())).
 							append("=").
 							append(encodeParameter(entry.getValue()));
 						i ++;
 					}
 				}
         		
         		// Add parameters to the URI
         		uri = baseUrl + queryStrBuffer.toString();
 
         		if (logger.isDebugEnabled())
         			logger.debug("Using final URL " + uri);
         		
         		// TODO Support passing of parameters in the request body, for non-GET requests
         	}
             
             // call client and process response
             response = remoteClient.call(uri, req, res);
             processResponse(remoteClient, response);
         }
         else
         {
             ResponseStatus status = new ResponseStatus();
             status.setCode(ResponseStatus.STATUS_INTERNAL_SERVER_ERROR);
             response = new Response((String) null, status);
         }
         return response;
     }
 
 }
