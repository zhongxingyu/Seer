 
 				/*
  *  Adito
  *
  *  Copyright (C) 2003-2006 3SP LTD. All Rights Reserved
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2 of
  *  the License, or (at your option) any later version.
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 			
 package com.adito.reverseproxy;
 
 import java.io.ByteArrayOutputStream;
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import javax.servlet.http.Cookie;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.maverick.http.AuthenticationCancelledException;
 import com.maverick.http.HttpAuthenticatorFactory;
 import com.maverick.http.HttpClient;
 import com.maverick.http.HttpException;
 import com.maverick.http.HttpResponse;
 import com.maverick.http.PasswordCredentials;
 import com.maverick.http.UnsupportedAuthenticationException;
 import com.maverick.util.URLUTF8Encoder;
 import com.adito.boot.ContextHolder;
 import com.adito.boot.HttpConstants;
 import com.adito.boot.RequestHandler;
 import com.adito.boot.RequestHandlerException;
 import com.adito.boot.RequestHandlerRequest;
 import com.adito.boot.RequestHandlerResponse;
 import com.adito.boot.SystemProperties;
 import com.adito.boot.Util;
 import com.adito.core.MultiMap;
 import com.adito.core.stringreplacement.SessionInfoReplacer;
 import com.adito.core.stringreplacement.VariableReplacement;
 import com.adito.policyframework.LaunchSession;
 import com.adito.policyframework.LaunchSessionFactory;
 import com.adito.security.Constants;
 import com.adito.security.LogonControllerFactory;
 import com.adito.security.SessionInfo;
 import com.adito.util.ProxiedHttpMethod;
 import com.adito.vfs.webdav.DAVUtilities;
 import com.adito.webforwards.AbstractAuthenticatingWebForwardHandler;
 import com.adito.webforwards.ReverseProxyWebForward;
 import com.adito.webforwards.WebForwardPlugin;
 import com.adito.webforwards.WebForwardTypes;
 
 /**
  * Request handler that deals with both <i>Reverse Proxy</i> and <i>Replacement
  * Proxy</i> web forwards.
  */
 public class ReverseProxyMethodHandler extends AbstractAuthenticatingWebForwardHandler implements RequestHandler {
 
 	/**
 	 * Launch session attribute for storing whether authentication has been
 	 * posted yet
 	 */
 	public static final String LAUNCH_ATTR_AUTH_POSTED = "authPosted";
 
 	final static String sessionCookie = SystemProperties.get("adito.cookie", "JSESSIONID");
 
 	static HashSet<String> ignoredHeaders = new HashSet<String>();
 
 	static {
 		ignoredHeaders.add("Location".toUpperCase());
 		ignoredHeaders.add("Server".toUpperCase());
 		ignoredHeaders.add("Date".toUpperCase());
 	}
 
 	static Log log = LogFactory.getLog(ReverseProxyMethodHandler.class);
 
 	public boolean handle(String pathInContext, String pathParams, RequestHandlerRequest request, RequestHandlerResponse response)
 					throws RequestHandlerException, IOException {
 		if (log.isDebugEnabled())
 			log.debug("Check if Reverse Proxy Request for: " + pathInContext);
 
 		/*
 		 * First try and locate the session, if there is no session then this is
 		 * definitely not a reverse proxy request
 		 */
 		LaunchSession launchSession = null;
 		SessionInfo session = locateSession(request, response);
 
 		if (session == null) {
 			// If we have no session, then this cannot be a reverse proxy
 			// request
 			if (log.isDebugEnabled())
 				log.debug("No session, not a reverse proxy.");
 			return false;
 		}
 		
 		try {
 			// Perhaps this is a reverse proxy?
 			String host = request.getHost();
 			ReverseProxyWebForward wf = null;
 
 			// Active Proxy
 
 			if (host != null && !host.equals("") && host.indexOf('.') > -1) {
 				int idx = host.indexOf('.');
 				if (idx != -1) {
 					try {
 						String uniqueId = host.substring(0, idx);
 						launchSession = LaunchSessionFactory.getInstance().getLaunchSession(session, uniqueId);
 						if (launchSession != null) {
 							wf = (ReverseProxyWebForward) launchSession.getResource();
 							launchSession.checkAccessRights(null, session);
 							if (!((ReverseProxyWebForward) wf).getActiveDNS()) {
 								throw new Exception("Appears to be an active DNS request but the associated web forward is not active DNS. Is someone trying something funny???");
 							}
 	                        LogonControllerFactory.getInstance().addCookies(request, response, session.getLogonTicket(), session);
 							return handleReverseProxy(pathInContext, pathParams, request, response, launchSession);
 						}
 
 					} catch (Exception ex) {
 						if (log.isDebugEnabled())
 							log.debug("Active DNS web forward lookup failed", ex);
 					}
 				} else {
 					if (log.isDebugEnabled())
 						log.debug("Not active DNS.");
 				}
 			}
 
 			String hostHeader = request.getHost();
 			int idx = hostHeader.indexOf(':');
 			if (idx > -1)
 				hostHeader = hostHeader.substring(0, idx);
 
 			/* Ordinary reverse proxy? There can only ever be one launch session per reverse proxy
 			 * as there is no way of maintaining the session across requests. If a user launches the
 			 * resource more than once, the old launch session will be removed
 			 */ 
 
 			for (LaunchSession rs : LaunchSessionFactory.getInstance().getLaunchSessionsForType(session,
 				WebForwardPlugin.WEBFORWARD_RESOURCE_TYPE)) {
 				if (rs.getResource() instanceof ReverseProxyWebForward) {
 					wf = (ReverseProxyWebForward) rs.getResource();
 					// Check that its not reverseProxyRedirect.jsp because if we don't it breaks access after first attempt in same session
 					if (wf.isValidPath(pathInContext) || (wf.getHostHeader() != null && wf.getHostHeader().equals(hostHeader) && !pathInContext.startsWith("/reverseProxyRedirect.jsp"))) {
 						rs.checkAccessRights(null, session);
 						return handleReverseProxy(pathInContext, pathParams, request, response, rs);
 					}
 				}
 			}
 		} catch (Exception e) {
 			log.error("Failed to process web forward.", e);
 			if (session != null) {
 				session.getHttpSession().setAttribute(Constants.EXCEPTION, e);
 				response.sendRedirect("/showPopupException.do");
 			} else {
 				throw new RequestHandlerException("Failed to process web forward.", 500);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	private boolean handleReverseProxy(String path, String params, RequestHandlerRequest request, RequestHandlerResponse response,
 										LaunchSession launchSession) throws IOException {
 
 		ReverseProxyWebForward webForward = (ReverseProxyWebForward) launchSession.getResource();
 		boolean connectionError = true;
 		
 		/* Because we are in a request handler, the session's last access time
 		 * does not get updated by the container
 		 */
 		launchSession.getSession().access();
 
 		/***
 		 * LDP - DO NOT use request parameter map until the encoding has been set. If you
 		 * call getParameters it decodes the parameters so this can only be done once the 
 		 * character set has been set. 
 		 */
 
 		try {
 			URL target = getTarget(launchSession, request);
 			setRequestEncoding(launchSession, target, request);
 			HttpClient client = getClient(launchSession, target);
 			ProxiedHttpMethod method = getMethod(client, launchSession, request, target);
 			processPortsAndXForwarding(method, request);
 			checkProcessedContent(launchSession, method, request);
 			addCustomHeaders(webForward, method);
 			
 			/* If this webforward has JavaScript form authentication and this hasn't
 			 * yet been processed, then we make sure the content that comes back from
 			 * the target is not encoded using gzip or any other compression methods.
 			 * This is because we will be tacking on some content and its easy to
 			 * deal with unencoded. 
 			 * 
 			 * TODO We may want to support at least Gzip at some point
 			 */
 			if (webForward.getFormType().equals(WebForwardTypes.FORM_SUBMIT_JAVASCRIPT)
 					&& !Boolean.TRUE.equals(launchSession.getAttribute(LAUNCH_ATTR_AUTH_POSTED))) {
 				method.getProxiedRequest().removeFields("Accept-Encoding");
 			}
 			
 			com.maverick.http.HttpResponse clientResponse = doExecute(client, method);
 			connectionError = false;
 			checkInsecureIIS(webForward, clientResponse);
 			filterUnsupportedAuthMethods(clientResponse);
 			processStatus(clientResponse, response);
 			processRedirects(clientResponse, request, response);
 			processHeaders(clientResponse, response);
 
 			/*
 			 * If the content type is HTML, this webforward is configured for
 			 * automatic JavaScript authentication and authentication has not
 			 * yet been performed, then tack the JavaScript on to the end of the
 			 * content. This requires that the content is read into memory and
 			 * the content length adjusted
 			 */
 			if (clientResponse.getStatus() == 200 && webForward.getFormType().equals(WebForwardTypes.FORM_SUBMIT_JAVASCRIPT)
 				&& "text/html".equals(clientResponse.getContentTypeWithoutParameter())
 				&& !Boolean.TRUE.equals(launchSession.getAttribute(LAUNCH_ATTR_AUTH_POSTED))) {
 				ByteArrayOutputStream baos = new ByteArrayOutputStream();
 				Util.copy(clientResponse.getInputStream(), baos, -1, 16384);
 				addJavaScriptAuthenticationCode(launchSession, baos, 0);
 				byte[] arr = baos.toByteArray();
 				if (clientResponse.getHeaderField("Content-Length") != null) {
 					response.setField("Content-Length", String.valueOf(arr.length));
 				}
 				response.getOutputStream().write(arr);
 				launchSession.setAttribute(LAUNCH_ATTR_AUTH_POSTED, Boolean.TRUE);
 			} else {
 				Util.copy(clientResponse.getInputStream(), response.getOutputStream(), -1, 16384);
 			}
 			
 			clientResponse.close();
 
 			return true;
 		} catch (UnsupportedAuthenticationException ex) {
 			log.error("?", ex);
 		} catch (com.maverick.http.HttpException ex) {
 			log.error("?", ex);
 		} catch (EOFException e) {
 			/*
 			 * This is probably just because the user clicked on a link before
 			 * the page had finished downloading.
 			 */
 			if (log.isDebugEnabled())
 				log.debug("Received EOF in reverse proxy request [THIS IS PROBABLY NOT FATAL]", e);
 		} catch (IOException ex) {
 			if (connectionError) {
 				response.sendError(404,
 					"The proxied web server could not be contacted or did not respond correctly to the request! " + ex.getMessage());
 			}
 			log.error("?", ex);
 		} catch (AuthenticationCancelledException ex) {
 			log.error("?", ex);
 		}
 		return true;
 
 	}
 
 	HttpResponse doExecute(HttpClient client, ProxiedHttpMethod method) throws UnknownHostException, IOException,
 					HttpException, UnsupportedAuthenticationException, AuthenticationCancelledException {
 		if (log.isDebugEnabled()) {
 			log.debug("Connecting to " + client.getHost() + ":" + client.getPort() + " (Secure = " + client.isSecure() + ")");
 		}
 		return client.execute(method);
 	}
 
 	URL getTarget(LaunchSession launchSession, RequestHandlerRequest request) throws MalformedURLException {
 		ReverseProxyWebForward webForward = (ReverseProxyWebForward) launchSession.getResource();
 		VariableReplacement r = new VariableReplacement();
 		r.setRequest(request);
 		r.setSession(launchSession.getSession());
 		r.setPolicy(launchSession.getPolicy());
 
 		URL target = new URL(r.replace(webForward.getDestinationURL()));
 
 		if (log.isDebugEnabled()) {
 			log.debug("Reverse proxy target  " + target.toExternalForm());
 		}
 		return target;
 	}
 
 	void checkProcessedContent(LaunchSession launchSession, ProxiedHttpMethod method, RequestHandlerRequest request)
 					throws IOException {
 		String contentType = request.getContentType();
 		int contentLength = request.getContentLength();
 
 		boolean hasProcessedContent = contentType != null && request.getMethod().equals("POST")
 			&& contentType.equals("application/x-www-form-urlencoded");
 
 		if (contentLength > 0 && !hasProcessedContent) {
 			if(log.isDebugEnabled())
 				log.debug("Setting request content of " + contentLength + " bytes with content type " + contentType + " available=" + request.getInputStream().available());
 			method.setContent(request.getInputStream(), contentLength, contentType);
 		}
 
 	}
 
 	private void processRequestHeaders(RequestHandlerRequest request, ProxiedHttpMethod method) {
 		String header;
 
 		for (Enumeration e = request.getFieldNames(); e.hasMoreElements();) {
 			header = (String) e.nextElement();
 
 			// Skip the connection header as our client maintains its own
 			// connections
 			if (header.equalsIgnoreCase(HttpConstants.HDR_CONNECTION) || header.equalsIgnoreCase(HttpConstants.HDR_KEEP_ALIVE)) {
 				continue;
 			}
 
 			for (Enumeration j = request.getFieldValues(header); j.hasMoreElements();) {
 				String val = (String) j.nextElement();
 				if (header.equalsIgnoreCase("cookie")) {
 					String[] cookieVals = val.split("\\;");
 					StringBuffer newVal = new StringBuffer();
 					for (int i = 0; i < cookieVals.length; i++) {
 						if (log.isDebugEnabled())
 							log.debug("Cookie = " + cookieVals[i]);
 						
 						// Its possible cookies may be sent without values
 						int idx = cookieVals[i].indexOf('=');
 						String cn = idx == -1 ? cookieVals[i] : Util.trimBoth(cookieVals[i].substring(0, idx));
 						String cv = idx == -1 ? null : Util.trimBoth(cookieVals[i].substring(idx + 1));
 						
 						// Ignore SSL-Exploer cookies
 						if (cn.equals(Constants.LOGON_TICKET) || cn.equals(Constants.DOMAIN_LOGON_TICKET)
							|| cn.equals(SystemProperties.get("adito.cookie", "SSLX_SSESHID"))) {
 							if (log.isDebugEnabled())
 								log.debug("  Omiting cookie " + cn + "=" + cv);
 						} else {
 							if (newVal.length() > 0) {
 								newVal.append("; ");
 							}
 							newVal.append(cn);
 							if(cv != null) {
 								newVal.append("=");
 								newVal.append(cv);
 							}
 						}
 					}
 					if (newVal.length() > 0) {
 						method.getProxiedRequest().addHeaderField(header, newVal.toString());
 						if (log.isDebugEnabled())
 							log.debug("HEADER: " + header + " " + val);
 					}
 				} else {
 					method.getProxiedRequest().addHeaderField(header, val);
 					if (log.isDebugEnabled())
 						log.debug("HEADER: " + header + " " + val);
 				}
 			}
 		}
 
 	}
 
 	void processPortsAndXForwarding(ProxiedHttpMethod method, RequestHandlerRequest request) {
 
 		String thisHost = ContextHolder.getContext().getHostname();
 		int thisPort = ContextHolder.getContext().getPort();
 
 		// Check for a non default port (we dont care if we receive on 443
 		// but
 		// forward to 80 as this would not change the host header
 		if (thisPort != 443 && thisPort != 80) {
 			thisHost += ":" + thisPort;
 		}
 
 		method.getProxiedRequest().setHeaderField("X-Forwarded-Host", thisHost);
 		method.getProxiedRequest().setHeaderField("X-Forwarded-For", request.getRemoteHost());
 		method.getProxiedRequest().setHeaderField("X-Forwarded-Server", thisHost);
 		method.getProxiedRequest().setHeaderField("X-Forwarded-Port", String.valueOf(thisPort));
 
 	}
 
 	void processStatus(HttpResponse clientResponse, RequestHandlerResponse response) {
 
 		if (log.isDebugEnabled())
 			log.debug("HTTP response is " + clientResponse.getStartLine());
 
 		response.setStatus(clientResponse.getStatus());
 		response.setReason(clientResponse.getReason());
 
 	}
 
 	void processHeaders(HttpResponse clientResponse, RequestHandlerResponse response) {
 
 		String header;
 		for (Enumeration e = clientResponse.getHeaderFieldNames(); e.hasMoreElements();) {
 			header = (String) e.nextElement();
 			if (log.isDebugEnabled()) {
 				log.debug("Received header " + header);
 			}
 			String[] val = clientResponse.getHeaderFields(header);
 			if(val == null) {
 				log.debug("No value???");
 			}
 
 			if (ignoredHeaders.contains(header.toUpperCase())) {
 				if (log.isDebugEnabled())
 					log.debug("Ignoring header " + header);
 				continue;
 			}
 
 			for (int i = 0; i < val.length; i++) {
 				if (log.isDebugEnabled()) {
 					log.debug("Adding value " + val[i] + " for " + header);
 				}
 				if (i == 0)
 					response.setField(header, val[i]);
 				else
 					response.addField(header, val[i]);
 			}
 		}
 	}
 
 	void processRedirects(HttpResponse clientResponse, RequestHandlerRequest request, RequestHandlerResponse response) {
 
 		/**
 		 * Process redirect Location headers, the location may be a HTTP
 		 * resource which will require changing to HTTPS
 		 */
 		if (clientResponse.getStatus() >= 300 && clientResponse.getStatus() < 400) {
 			switch (clientResponse.getStatus()) {
 				case 300: // Multiple choices
 				case 301: // Moved permanentley
 				case 302: // Found
 				case 303: // See other
 				case 307: // Temporarily redirect
 					String[] locations = clientResponse.getHeaderFields(HttpConstants.HDR_LOCATION);
 					response.removeField(HttpConstants.HDR_LOCATION);
 					for (int i = 0; i < locations.length; i++) {
                         String originatingHost = clientResponse.getConnection().getHost();
                         String location = rebuildLocation(Util.urlDecode(locations[i]), request.getHost(), originatingHost);
                         response.addField("Location", location);
                         if (log.isDebugEnabled())
                             log.debug("Location is now '" + location + "'");
                     }
 					break;
 				case 304: // Not Modified
 					// Do nothing return as is
 					break;
 				case 305: // Use proxy
 					log.warn("Detected HTTP response 305 [Use proxy] this may break reverse proxy!");
 					break;
 				default:
 					log.error("Got unknown 3XX response code from server " + clientResponse.getStatus());
 			}
 		}
 	}
     
     static final String rebuildLocation(String location, String host, String originatingHost) {
         // Check against the requests Host value and change the Location if required
         if (location.startsWith("http://" + host)) {
             String protocolStripped = stripProtocol(location);
             return encodeURL("https://" + protocolStripped);
         } else if (location.startsWith("http://" + originatingHost) || location.startsWith("https://" + originatingHost)) {
             String protocolStripped = stripProtocol(location);
             int indexOf = protocolStripped.indexOf('/');
             if (indexOf == -1) {
                 return encodeURL("https://" + host);
             } else {
                 String remainingPath = protocolStripped.substring(indexOf);
                 return encodeURL("https://" + host + remainingPath);
             }
         } else {
             if (log.isDebugEnabled()) {
                 log.debug("Redirect location may result in reverse proxy error " + location);
             }
         }
         return encodeURL(location);
     }
     
     static final String stripProtocol(String url) {
         if(url.startsWith("http://")) {
             return url.substring(7);
         } else if(url.startsWith("https://")) {
             return url.substring(8);
         }
         return url;
     }
     
     void addCustomHeaders(ReverseProxyWebForward webForward, ProxiedHttpMethod method) {
 
 		/**
 		 * Add any custom headers to the request
 		 */
 		Map customHeaders = webForward.getCustomHeaders();
 		String header;
 		for (Iterator it = customHeaders.entrySet().iterator(); it.hasNext();) {
 			Map.Entry entry = (Map.Entry) it.next();
 			header = (String) entry.getKey();
 			Vector v = (Vector) entry.getValue();
 			for (Iterator it2 = v.iterator(); it2.hasNext();) {
 				method.getProxiedRequest().addHeaderField(header, (String) it2.next());
 			}
 		}
 	}
 
 	void checkInsecureIIS(ReverseProxyWebForward webForward, HttpResponse clientResponse) {
 
 		/**
 		 * Perform a check to see if we're connected to an IIS server and if the
 		 * backend server is insecure. If it is set the customer
 		 * Front-End-Https: on header.
 		 */
 		String server = clientResponse.getHeaderField("Server");
 
 		if (server != null && server.startsWith("Microsoft-IIS")) {
 			if (!webForward.containsCustomHeader("Front-End-Https"))
 				webForward.setCustomHeader("Front-End-Https", "on");
 		}
 	}
 
 	void filterUnsupportedAuthMethods(HttpResponse clientResponse) {
 
 		/**
 		 * Filter out unsupported authentication methods because they dont work
 		 * through the reverse proxy - the best way to enable these will be to
 		 * allow credentials to be set on the reverse proxy web forward.
 		 */
 		String[] challenges = clientResponse.getHeaderFields("www-authenticate");
 
 		if (challenges != null) {
 			clientResponse.removeFields("www-authenticate");
 
 			for (int i = 0; i < challenges.length; i++) {
 				if (challenges[i].toLowerCase().startsWith("basic") || challenges[i].toLowerCase().startsWith("digest")
 					|| challenges[i].toLowerCase().startsWith("ntlm")) {
 					clientResponse.setHeaderField("WWW-Authenticate", challenges[i]);
 				}
 			}
 		}
 	}
 
 	ProxiedHttpMethod getMethod(HttpClient client, LaunchSession launchSession, RequestHandlerRequest request, URL target) {
 
 		ReverseProxyWebForward webForward = (ReverseProxyWebForward) launchSession.getResource();
 		ProxiedHttpMethod method;
 
 		VariableReplacement v = new VariableReplacement();
 		v.setRequest(request);
 		v.setSession(launchSession.getSession());
 		v.setPolicy(launchSession.getPolicy());
 		
 		/**
 		 * POST parameters are now not being
 		 */
 
 		if (!webForward.getFormType().equals(WebForwardTypes.FORM_SUBMIT_NONE) 
 				&& !webForward.getFormType().equals(WebForwardTypes.FORM_SUBMIT_NONE)
 				&& !webForward.getFormType().equals("")
 				&& !webForward.getFormType().equals(WebForwardTypes.FORM_SUBMIT_JAVASCRIPT)
 				&& !Boolean.TRUE.equals(launchSession.getAttribute(LAUNCH_ATTR_AUTH_POSTED))) {
 
 			/**
 			 * This code will automatically submit form parameters. If it is a post,
 			 * then we ignore the parameters request and use the webforward target. 
 			 */
 			method = new ProxiedHttpMethod(webForward.getFormType(),
 							target.getFile(),
 							webForward.getFormType().equals(WebForwardTypes.FORM_SUBMIT_POST) ?  new MultiMap() : new MultiMap(request.getParameters()),
 							launchSession.getSession(),
 							webForward.getFormType().equals(WebForwardTypes.FORM_SUBMIT_POST));
 
 			if (webForward.getCharset() != null 
 					&& !webForward.getCharset().equals("")
 					&& !webForward.getCharset().equals(WebForwardTypes.DEFAULT_ENCODING))
 				method.setCharsetEncoding(webForward.getCharset());
 
 			StringTokenizer tokens = new StringTokenizer(webForward.getFormParameters(), "\n");
 			int idx;
 			String param;
 			
 			while (tokens.hasMoreTokens()) {
 				param = v.replace(tokens.nextToken().trim());
 				idx = param.indexOf('=');
 				if (idx > -1) {
 					method.addParameter(param.substring(0, idx), param.substring(idx + 1));
 				} else
 					method.addParameter(param, "");
 			}
 			
 			launchSession.setAttribute(LAUNCH_ATTR_AUTH_POSTED, Boolean.TRUE);
 			processRequestHeaders(request, method);
 			
 			// Do not send through any cookies on the authentication request
 			method.getProxiedRequest().removeFields(HttpConstants.HDR_COOKIE);
 			client.removeAllCookies();
 
 		} else {
 			method = new ProxiedHttpMethod(request.getMethod(),
 							request.getURIEncoded(),
 							new MultiMap(request.getParameters()),
 							launchSession.getSession(),
 							request.getContentType() != null && request.getContentType()
 											.toLowerCase()
 											.startsWith("application/x-www-form-urlencoded"));
 			if (webForward.getCharset() != null 
 					&& !webForward.getCharset().equals("")
 					&& !webForward.getCharset().equals(WebForwardTypes.DEFAULT_ENCODING))
 				method.setCharsetEncoding(webForward.getCharset());
 			processRequestHeaders(request, method);
 		}
 
 		return method;
 	}
 
 	HttpClient getClient(LaunchSession launchSession, URL target) {
 
 		ReverseProxyWebForward webForward = (ReverseProxyWebForward) launchSession.getResource();
 
 		String hostname = target.getHost();
 		boolean isSecure = target.getProtocol().equalsIgnoreCase("https");
 		int connectPort = target.getPort() == -1 ? (isSecure ? 443 : 80) : target.getPort();
 
 		HttpClient client;
 
 		SessionClients clients = null;
 		// CookieMap cookieMap = null;
 		synchronized (launchSession.getSession().getHttpSession()) {
 			clients = (SessionClients) launchSession.getSession().getHttpSession().getAttribute(Constants.HTTP_CLIENTS);
 			if (clients == null) {
 				clients = new SessionClients();
 				launchSession.getSession().getHttpSession().setAttribute(Constants.HTTP_CLIENTS, clients);
 			}
 		}
 
 		synchronized (clients) {
 			String key = hostname + ":"
 				+ connectPort
 				+ ":"
 				+ isSecure
 				+ ":"
 				+ webForward.getResourceId()
 				+ ":"
 				+ Thread.currentThread().getName()
 				+ ":"
 				+ launchSession.getSession().getId();
 			client = (HttpClient) clients.get(key);
 
 			if (client == null) {
 				client = new HttpClient(hostname, connectPort, isSecure);
 				client.setIncludeCookies(false);
 				
 				if (!webForward.getPreferredAuthenticationScheme().equals(HttpAuthenticatorFactory.NONE) && !webForward.getAuthenticationUsername()
 								.equals("")
 					&& !webForward.getAuthenticationPassword().equals("")) {
 					PasswordCredentials pwd = new PasswordCredentials();
 					pwd.setUsername(SessionInfoReplacer.replace(launchSession.getSession(), webForward.getAuthenticationUsername()));
 					pwd.setPassword(SessionInfoReplacer.replace(launchSession.getSession(), webForward.getAuthenticationPassword()));
 					client.setCredentials(pwd);
 				}
 
 				// Set the preferred scheme
 				client.setPreferredAuthentication(webForward.getPreferredAuthenticationScheme());
 
 				// If we're using basic authentication then preempt the 401
 				// response
 				client.setPreemtiveAuthentication(webForward.getPreferredAuthenticationScheme().equalsIgnoreCase("BASIC"));
 
 				clients.put(key, client);
 			}
 		}
 
 		return client;
 	}
 
 	void setRequestEncoding(LaunchSession launchSession, URL target, RequestHandlerRequest request) {
 
 		ReverseProxyWebForward webForward = (ReverseProxyWebForward) launchSession.getResource();
 
 		/**
 		 * This code sets the character encoding of the request. This may be
 		 * overridden because some servers assume the character set and there is
 		 * no way for us work this.
 		 */
 		try {
 			if (webForward.getCharset() != null 
 					&& !webForward.getCharset().equals("")
 					&& !webForward.getCharset().equals(WebForwardTypes.DEFAULT_ENCODING))
 				request.setCharacterEncoding(webForward.getCharset());
 		} catch (UnsupportedEncodingException ex) {
 			log.error("Java runtime does not support encoding", ex);
 		}
 	}
 
 	SessionInfo locateSession(String pathInContext, String pathParams, RequestHandlerRequest request,
 								RequestHandlerResponse response) {
 		/*
 		 * When not authenticated, dont reverse proxy anything. We use the logon
 		 * ticket to get the HttpSession in use
 		 */
 		SessionInfo session = null;
 
 		/**
 		 * The launching of a reverse proxy will always be a GET. This change
 		 * will allow us to set the character encoding of the request later so
 		 * that POST parameters are not incorrectly encoded.
 		 */
 		if (request.getMethod().equals("GET") && request.getParameters().containsKey(LaunchSession.LONG_LAUNCH_ID)) {
 			String launchId = (String) request.getParameters().get(LaunchSession.LONG_LAUNCH_ID);
 
 			// Get the actual session for the reverse proxy
 			LaunchSession launchSession = LaunchSessionFactory.getInstance().getLaunchSession(launchId);
 			if (launchSession != null) {
 
 				// If the launch session is not for a reverse proxy web forward
 				// then ignore
 				if (launchSession.isTracked() && launchSession.getResource() instanceof ReverseProxyWebForward) {
 					session = launchSession.getSession();
 
 					Cookie[] cookies = request.getCookies();
 					if (cookies != null) {
 						for (int i = 0; i < cookies.length; i++) {
 							if (cookies[i].getName().equalsIgnoreCase(sessionCookie)) {
 								LogonControllerFactory.getInstance().attachSession(cookies[i].getValue(), session);
 								break;
 							}
 						}
 					}
 
 					LogonControllerFactory.getInstance().addCookies(request, response, session.getLogonTicket(), session);
 				}
 			}
 
 		} else {
 			Cookie[] cookies = request.getCookies();
 
 			if (cookies != null) {
 				for (int i = 0; i < cookies.length; i++) {
 					if (cookies[i].getName().equalsIgnoreCase(sessionCookie)) {
 						session = LogonControllerFactory.getInstance().getSessionInfoBySessionId(cookies[i].getValue());
 						if (session != null) {
 							LogonControllerFactory.getInstance().addCookies(request, response, session.getLogonTicket(), session);
 							session.access();
 							break;
 						}
 					}
 					if (cookies[i].getName().equalsIgnoreCase(Constants.DOMAIN_LOGON_TICKET) || cookies[i].getName()
 									.equalsIgnoreCase(Constants.LOGON_TICKET)) {
 						session = LogonControllerFactory.getInstance().getSessionInfo(cookies[i].getValue());
 						if (session != null) {
 							LogonControllerFactory.getInstance().addCookies(request, response, session.getLogonTicket(), session);
 							session.access();
 							break;
 						}
 					}
 
 				}
 			}
 		}
 
 		if (session != null) {
 			session.access();
 		}
 
 		return session;
 	}
 	
 	/**
 	 * Takes an unencoded URL query string, and encodes it. 
 	 * @param query
 	 * @return
 	 */
 	public static final String encodeQuery(String query) {
 		String encoded = "";
 		StringTokenizer pairs = new StringTokenizer(query, "&");
 		while(pairs.hasMoreTokens()) {
 			StringTokenizer pair = new StringTokenizer(pairs.nextToken(), "=");
 			if(pair.hasMoreTokens()) {
 				encoded += (encoded.length()==0 ? "" : "&") + URLUTF8Encoder.encode(pair.nextToken(), true);
 				if(pair.hasMoreTokens()) {
 					encoded += "=" + URLUTF8Encoder.encode(pair.nextToken(), true);
 				}
 				
 			}
 		}
 		return encoded;
 	}
 
 	/**
 	 * Encodes a URL 
 	 * @param location
 	 * @return
 	 */
 	public static final String encodeURL(String location) {
 		
 		try {
 			URL url = new URL(location);
 			
 			StringBuffer buf = new StringBuffer();
 			buf.append(url.getProtocol());
 			buf.append("://");
             if(!Util.isNullOrTrimmedBlank(url.getUserInfo())) {
                 buf.append(DAVUtilities.encodeURIUserInfo(url.getUserInfo()));
                 buf.append("@");
             }
 			buf.append(url.getHost());
 			if(url.getPort() != -1) {
 			    buf.append(":");
 			    buf.append(url.getPort());
 			}
             if(!Util.isNullOrTrimmedBlank(url.getPath())) {
                 buf.append(URLUTF8Encoder.encode(url.getPath(), false));
             }
 			if(!Util.isNullOrTrimmedBlank(url.getQuery())) {
 			    buf.append("?");
 			    buf.append(encodeQuery(url.getQuery()));
 			}
 			
 			return buf.toString();
 		} catch (MalformedURLException e) {
 			
 			int idx = location.indexOf('?'); 
 			if(idx > -1 && idx < location.length()-1) {
 				return URLUTF8Encoder.encode(location.substring(0, idx), false) + "?" + encodeQuery(location.substring(idx+1));
 			} else
 				return URLUTF8Encoder.encode(location, false);
 		}
 	}
 
 }
