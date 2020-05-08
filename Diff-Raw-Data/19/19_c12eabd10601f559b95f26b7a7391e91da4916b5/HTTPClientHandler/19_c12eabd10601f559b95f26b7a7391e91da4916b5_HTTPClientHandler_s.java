 // The Grinder
 // Copyright (C) 2000, 2001  Paco Gomez
 // Copyright (C) 2000, 2001  Philip Aston
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.plugin.http;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.HttpURLConnection; // For the (incomplete!) status code definitions only.
 import java.util.Iterator;
 import java.util.HashMap;
 import java.util.Map;
 
 import HTTPClient.Cookie;
 import HTTPClient.CookieModule;
 import HTTPClient.CookiePolicyHandler;
 import HTTPClient.DefaultAuthHandler;
 import HTTPClient.HTTPConnection;
 import HTTPClient.HttpOutputStream;
 import HTTPClient.HTTPResponse;
 import HTTPClient.ModuleException;
 import HTTPClient.NVPair;
 import HTTPClient.ParseException;
 import HTTPClient.ProtocolNotSuppException;
 import HTTPClient.RoRequest;
 import HTTPClient.RoResponse;
 import HTTPClient.URI;
 
 //import net.grinder.plugininterface.PluginException;
 import net.grinder.plugininterface.PluginThreadContext;
 
 
 /**
  * @author Philip Aston
  * @version $Revision$
  */
 class HTTPClientHandler implements HTTPHandler
 {
     private final static Class s_redirectionModule;
     
     static
     {
 	// Use our hack to disable the trailer-related headers. Do
 	// this first before HTTPClient is class loaded.
 	System.getProperties().put("HTTPClient.disableTrailers", "true");
 
 	// Remove standard HTTPClient modules which we don't want.
 	try {
 	    // Don't want additional post-processing of response data.
 	    HTTPConnection.removeDefaultModule(
 		Class.forName("HTTPClient.ContentEncodingModule"));
 	    HTTPConnection.removeDefaultModule(
 		Class.forName("HTTPClient.TransferEncodingModule"));
 
 	    // Don't want to retry requests.
 	    HTTPConnection.removeDefaultModule(
 		Class.forName("HTTPClient.RetryModule"));
 
 	    s_redirectionModule =
 		Class.forName("HTTPClient.RedirectionModule");
 	}
 	catch (ClassNotFoundException e) {
 	    throw new RuntimeException("Failed to load HTTPClient classes");
 	}
 
 	// Turn off cookie permission checks.
 	CookieModule.setCookiePolicyHandler(null);
 
 	// Turn off authorisation UI.
 	DefaultAuthHandler.setAuthorizationPrompter(null);
     }
 
     private final PluginThreadContext m_pluginThreadContext;
     private final boolean m_useCookies;
     private final boolean m_followRedirects;
 
     public HTTPClientHandler(PluginThreadContext pluginThreadContext,
 			     boolean useCookies, boolean followRedirects)
     {
 	m_pluginThreadContext = pluginThreadContext;
 	m_useCookies = useCookies;
 	m_followRedirects = followRedirects;
     }
 
     // For now, just handle a single server.
     private Map m_httpConnections = new HashMap();
 
     private HTTPConnection getConnection(URI uri)
 	throws ParseException, ProtocolNotSuppException
     {
 	final URI keyURI =
 	    new URI(uri.getScheme(), uri.getHost(), uri.getPort(), "");
 
 	HTTPConnection connection =
 	    (HTTPConnection)m_httpConnections.get(keyURI);
 
 	if (connection == null) {
 	    connection = new HTTPConnection(uri);
 	    connection.setContext(HTTPClientHandler.this);
 
 	    if (m_useCookies) {
 		connection.addModule(CookieModule.class, 0);
 	    }
 	    else {
 		connection.removeModule(CookieModule.class);
 	    }
 
 	    if (m_followRedirects) {
 		connection.addModule(s_redirectionModule, 0);
 	    }
 	    else {
 		connection.removeModule(s_redirectionModule);
 	    }
 
 	    m_httpConnections.put(keyURI, connection);
 	}
 
 	return connection;
     }
 
     public String sendRequest(HTTPHandler.RequestData requestData)
 	throws HTTPHandlerException
     {
 	try {
 	    final URI uri = new URI(requestData.getURLString());
 	    final String postString = requestData.getPostString();
 
 	    m_pluginThreadContext.startTimer();
 
 	    final HTTPConnection httpConnection =
 		getConnection(uri);
 
 	    final AuthorizationData authorizationData =
 		requestData.getAuthorizationData();
 
 	    if (authorizationData != null &&
 		authorizationData instanceof
 		HTTPHandler.BasicAuthorizationData) {
 
 		final HTTPHandler.BasicAuthorizationData
 		    basicAuthorizationData =
 		    (HTTPHandler.BasicAuthorizationData)authorizationData;
 
 		httpConnection.addBasicAuthorization(
 		    basicAuthorizationData.getRealm(),
 		    basicAuthorizationData.getUser(),
 		    basicAuthorizationData.getPassword());
 	    }
 
 	    // HTTPClient ignores null header values.
 	    final NVPair[] headers = new NVPair[5];
 	    int nextHeader = 0;
 
 	    final String ifModifiedSince = requestData.getIfModifiedSince();
 
 	    if (ifModifiedSince != null) {
 		headers[nextHeader++] =
 		    new NVPair("If-Modified-Since", ifModifiedSince);
 	    }
 	    
 	    final HTTPResponse response;
 
 	    if (postString == null) {
		// Don't pass the query string to the second
		// parameter, we don't want it to be URL encoded.
		response = httpConnection.Get(uri.getPath() + '?' +
					      uri.getQueryString(),
					      (String)null,
					      headers);
 	    }
 	    else {
 		final String contentType = requestData.getContentType();
 
 		headers[nextHeader++] =
 		    new NVPair("Content-Type",
 			       contentType != null ?
 			       contentType :
 			       "application/x-www-form-urlencoded");
 
 		response = httpConnection.Post(uri.getPath(), postString,
 					       headers);
 	    }
 
 	    final int statusCode = response.getStatusCode();
 	
 	    if (statusCode == HttpURLConnection.HTTP_OK) {
 		final byte[] data = response.getData();
 		final String body = data != null? new String(data) : "";
 
 		m_pluginThreadContext.stopTimer();
 
 		m_pluginThreadContext.logMessage(uri + " OK");
 
 		return body;
 	    }
 	    else if (statusCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
 		response.getInputStream().close();
 		m_pluginThreadContext.stopTimer();
 
 		m_pluginThreadContext.logMessage(uri + " was not modified");
 	    }
 	    else if (statusCode == HttpURLConnection.HTTP_MOVED_PERM ||
 		     statusCode == HttpURLConnection.HTTP_MOVED_TEMP ||
 		     statusCode == 307) {
 
 		response.getInputStream().close();
 		m_pluginThreadContext.stopTimer();
 
 		// It would be possible to perform the check
 		// automatically, but for now just chuck out some
 		// information.
 		m_pluginThreadContext.logMessage(
 		    uri + " returned a redirect (" + statusCode + "). " +
 		    "Ensure the next URL is " +
 		    response.getHeader("Location"));
 
 		// I've seen the code that slurps the body block for non
 		// 200 responses. Can't think off the top of my head how
 		// to code to poll for a body, so for now just ignore the
 		// problem.
 	    }
 	    else {
 		response.getInputStream().close();
 		m_pluginThreadContext.stopTimer();
 
 		m_pluginThreadContext.logError("Unknown response code: " +
 					       statusCode + " (" +
 					       response.getReasonLine() +
 					       ") for " + uri);
 	    }
 
 	    return null;
 	}
 	catch (Exception e) {
 	    throw new HTTPHandlerException(e.getMessage(), e);
 	}
 	finally {
 	    // Back stop.
 	    m_pluginThreadContext.stopTimer();
 	}
     }
 
     public void reset() {
 	final Iterator i = m_httpConnections.values().iterator();
 	
 	while (i.hasNext()) {
 	    ((HTTPConnection)i.next()).stop();
 	}
 
 	CookieModule.discardAllCookies(HTTPClientHandler.this);
 
         m_httpConnections.clear();
     }
 }
