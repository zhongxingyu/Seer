 /*
  * JBoss, Home of Professional Open Source Copyright 2009, JBoss Inc., and
  * individual contributors as indicated by the @authors tag. See the
  * copyright.txt in the distribution for a full listing of individual
  * contributors.
  * 
  * This is free software; you can redistribute it and/or modify it under the
  * terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This software is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this software; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
  * site: http://www.fsf.org.
  * 
  * 
  * This file incorporates work covered by the following copyright and permission
  * notice:
  * 
  * Copyright 1999-2009 The Apache Software Foundation
  * 
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements. See the NOTICE file distributed with this
  * work for additional information regarding copyright ownership. The ASF
  * licenses this file to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package org.apache.catalina.connector;
 
 import java.io.IOException;
 
 import javax.servlet.AsyncEvent;
 import javax.servlet.AsyncListener;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.SessionTrackingMode;
 
 import org.apache.catalina.Context;
 import org.apache.catalina.Manager;
 import org.apache.catalina.Session;
 import org.apache.catalina.Wrapper;
 import org.apache.catalina.connector.Request.AsyncListenerRegistration;
 import org.apache.catalina.util.StringManager;
 import org.apache.catalina.util.URLEncoder;
 import org.apache.coyote.ActionCode;
 import org.apache.coyote.Adapter;
 import org.apache.tomcat.util.buf.B2CConverter;
 import org.apache.tomcat.util.buf.ByteChunk;
 import org.apache.tomcat.util.buf.CharChunk;
 import org.apache.tomcat.util.buf.MessageBytes;
 import org.apache.tomcat.util.http.Cookies;
 import org.apache.tomcat.util.http.ServerCookie;
 import org.apache.tomcat.util.net.SocketStatus;
 import org.jboss.logging.Logger;
 import org.jboss.servlet.http.HttpEvent;
 
 /**
  * Implementation of a request processor which delegates the processing to a
  * Coyote processor.
  * 
  * @author Craig R. McClanahan
  * @author Remy Maucherat
  * @version $Revision: 1762 $ $Date: 2011-07-04 12:53:24 +0200 (Mon, 04 Jul
  *          2011) $
  */
 
 public class CoyoteAdapter implements Adapter {
 	private static Logger log = Logger.getLogger(CoyoteAdapter.class);
 
 	// -------------------------------------------------------------- Constants
 
 	public static final int ADAPTER_NOTES = 1;
 
 	protected static final boolean ALLOW_BACKSLASH = Boolean.valueOf(
 			System.getProperty("org.apache.catalina.connector.CoyoteAdapter.ALLOW_BACKSLASH",
 					"false")).booleanValue();
 
 	protected static final String X_POWERED_BY = System.getProperty(
 			"org.apache.catalina.connector.CoyoteAdapter.X_POWERED_BY", "Servlet/3.0; JBossWeb-3");
 
 	// ----------------------------------------------------------- Constructors
 
 	/**
 	 * Construct a new CoyoteProcessor associated with the specified connector.
 	 * 
 	 * @param connector
 	 *            CoyoteConnector that owns this processor
 	 */
 	public CoyoteAdapter(Connector connector) {
 		super();
 		this.connector = connector;
 	}
 
 	// ----------------------------------------------------- Instance Variables
 
 	/**
 	 * The CoyoteConnector with which this processor is associated.
 	 */
 	private Connector connector = null;
 
 	/**
 	 * The string manager for this package.
 	 */
 	protected StringManager sm = StringManager.getManager(Constants.Package);
 
 	/**
 	 * Encoder for the Location URL in HTTP redirects.
 	 */
 	protected static URLEncoder urlEncoder;
 
 	// ----------------------------------------------------- Static Initializer
 
 	/**
 	 * The safe character set.
 	 */
 	static {
 		urlEncoder = new URLEncoder();
 		urlEncoder.addSafeCharacter('-');
 		urlEncoder.addSafeCharacter('_');
 		urlEncoder.addSafeCharacter('.');
 		urlEncoder.addSafeCharacter('*');
 		urlEncoder.addSafeCharacter('/');
 	}
 
 	// -------------------------------------------------------- Adapter Methods
 
 	/**
 	 * Event method.
 	 * 
 	 * @return false to indicate an error, expected or not
 	 */
 	public boolean event(org.apache.coyote.Request req, org.apache.coyote.Response res,
 			SocketStatus status) {
 
 		Request request = (Request) req.getNote(ADAPTER_NOTES);
 		Response response = (Response) res.getNote(ADAPTER_NOTES);
 
 		if (request.getWrapper() != null) {
 
 			boolean error = false;
 			boolean read = false;
 			boolean eof = false;
 			boolean close = false;
 			try {
 				if (response.isClosed()) {
 					// The response IO has been closed asynchronously, so call
 					// end
 					// in most cases
 					request.getEvent().setType(HttpEvent.EventType.END);
 					request.setEventMode(false);
 					close = true;
 				}
 				switch (status) {
 				case OPEN_READ:
 					if (!request.isEventMode()) {
 						// The event has been closed asynchronously, so call end
 						// instead of
 						// read to cleanup the pipeline
 						request.getEvent().setType(HttpEvent.EventType.END);
 						close = true;
 					} else {
 						try {
 							// Fill the read buffer of the servlet layer
 							int n = request.read();
 							if (n > 0) {
 								read = true;
 							} else if (n < 0) {
 								eof = true;
 							}
 						} catch (IOException e) {
 							e.printStackTrace();
 							error = true;
 						}
 						if (read) {
 							request.getEvent().setType(HttpEvent.EventType.READ);
 						} else if (error) {
 							request.getEvent().setType(HttpEvent.EventType.ERROR);
 						} else if (eof) {
 							request.getEvent().setType(HttpEvent.EventType.EOF);
 						} else {
 							// Data was present on the socket, but it did not
 							// translate into actual
 							// entity body bytes
 							return true;
 						}
 					}
 					break;
 				case OPEN_WRITE:
 					if (!request.isEventMode()) {
 						// The event has been closed asynchronously, so call end
 						// instead of
 						// read to cleanup the pipeline
 						request.getEvent().setType(HttpEvent.EventType.END);
 						close = true;
 					} else {
 						request.getEvent().setType(HttpEvent.EventType.WRITE);
 					}
 					break;
 				case OPEN_CALLBACK:
 					if (!request.isEventMode()) {
 						// The event has been closed asynchronously, so call end
 						// instead of
 						// read to cleanup the pipeline
 						// In nearly all cases, the close does a resume which
 						// will end up
 						// here
 						request.getEvent().setType(HttpEvent.EventType.END);
 						close = true;
 					} else {
 						request.getEvent().setType(HttpEvent.EventType.EVENT);
 					}
 					break;
 				case DISCONNECT:
 				case ERROR:
 					request.getEvent().setType(HttpEvent.EventType.ERROR);
 					error = true;
 					break;
 				case STOP:
 					request.getEvent().setType(HttpEvent.EventType.END);
 					close = true;
 					break;
 				case TIMEOUT:
 					if (!request.isEventMode()) {
 						// The event has been closed asynchronously, so call end
 						// instead of
 						// read to cleanup the pipeline
 						request.getEvent().setType(HttpEvent.EventType.END);
 						close = true;
 					} else {
 						request.getEvent().setType(HttpEvent.EventType.TIMEOUT);
 					}
 					break;
 				}
 
 				req.getRequestProcessor().setWorkerThreadName(Thread.currentThread().getName());
 
 				// Calling the container
 				connector.getContainer().getPipeline().getFirst()
 						.event(request, response, request.getEvent());
 
 				if (!error && (request.getAttribute(RequestDispatcher.ERROR_EXCEPTION) != null)) {
 					// An unexpected exception occurred while processing the
 					// event, so
 					// error should be called
 					request.getEvent().setType(HttpEvent.EventType.ERROR);
 					error = true;
 					connector.getContainer().getPipeline().getFirst()
 							.event(request, response, request.getEvent());
 				}
 				if (!error && read && request.ready()) {
 					// If this was a read and not all bytes have been read, or
 					// if no data
 					// was read from the connector, then it is an error
 					log.error(sm.getString("coyoteAdapter.read"));
 					request.getEvent().setType(HttpEvent.EventType.ERROR);
 					error = true;
 					connector.getContainer().getPipeline().getFirst()
 							.event(request, response, request.getEvent());
 				}
 				if (!error && !eof && (status == SocketStatus.OPEN_READ) && request.isEof()) {
 					// Send an EOF event
 					request.getEvent().setType(HttpEvent.EventType.EOF);
 					eof = true;
 					connector.getContainer().getPipeline().getFirst()
 							.event(request, response, request.getEvent());
 				}
 				if (!error && request.isEof()) {
 					request.suspend();
 				}
 				if (error || close) {
 					response.finishResponse();
 				}
 				return (!error);
 			} catch (Throwable t) {
 				if (!(t instanceof IOException)) {
 					log.error(sm.getString("coyoteAdapter.service"), t);
 				}
 				error = true;
 				return false;
 			} finally {
 				req.getRequestProcessor().setWorkerThreadName(null);
 				// Recycle the wrapper request and response
 				if (error || close || response.isClosed()) {
 					request.recycle();
 					response.recycle();
 					res.action(ActionCode.ACTION_EVENT_END, null);
 				}
 			}
 
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Service method.
 	 */
 	public void service(org.apache.coyote.Request req, org.apache.coyote.Response res)
 			throws Exception {
 
 		Request request = (Request) req.getNote(ADAPTER_NOTES);
 		Response response = (Response) res.getNote(ADAPTER_NOTES);
 
 		if (request == null) {
 
 			// Create objects
 			request = (Request) connector.createRequest();
 			request.setCoyoteRequest(req);
 			response = (Response) connector.createResponse();
 			response.setCoyoteResponse(res);
 
 			// Link objects
 			request.setResponse(response);
 			response.setRequest(request);
 
 			// Set as notes
 			req.setNote(ADAPTER_NOTES, request);
 			res.setNote(ADAPTER_NOTES, response);
 
 			// Set query string encoding
 			req.getParameters().setQueryStringEncoding(connector.getURIEncoding());
 
 		}
 
 		if (connector.getXpoweredBy()) {
 			response.addHeader("X-Powered-By", X_POWERED_BY);
 		}
 
 		boolean event = false;
 		try {
 
 			// Parse and set Catalina and configuration specific
 			// request parameters
 			req.getRequestProcessor().setWorkerThreadName(Thread.currentThread().getName());
 			if (postParseRequest(req, request, res, response)) {
 
 				// Calling the container
 				connector.getContainer().getPipeline().getFirst().invoke(request, response);
 
 				if (request.isEventMode()) {
 					if (!response.isClosed() && !response.isError()) {
 						res.action(ActionCode.ACTION_EVENT_BEGIN,
 								(request.getAsyncContext() == null) ? Boolean.TRUE : Boolean.FALSE);
 						event = true;
 					}
 				} else if (request.getAsyncContext() != null) {
 					// The AC was closed right away, so call onComplete as no
 					// event callback
 					// will occur in that case
 					Request.AsyncContextImpl asyncContext = (Request.AsyncContextImpl) request
 							.getAsyncContext();
 					for (AsyncListenerRegistration asyncListenerRegistration : asyncContext
 							.getAsyncListeners().values()) {
 						AsyncListener asyncListener = asyncListenerRegistration.getListener();
 						AsyncEvent asyncEvent = new AsyncEvent(asyncContext,
 								asyncListenerRegistration.getRequest(),
 								asyncListenerRegistration.getResponse());
 						try {
 							asyncListener.onComplete(asyncEvent);
 						} catch (Throwable t) {
 							log.error(sm.getString("coyoteAdapter.complete",
 									asyncListener.getClass()), t);
 						}
 					}
 				}
 
 			}
 
 			if (!event) {
 				response.finishResponse();
 				req.action(ActionCode.ACTION_POST_REQUEST, null);
 			}
 
 		} catch (IOException e) {
 			;
 		} catch (Throwable t) {
 			log.error(sm.getString("coyoteAdapter.service"), t);
 		} finally {
 			req.getRequestProcessor().setWorkerThreadName(null);
 			// Recycle the wrapper request and response
 			if (!event) {
 				request.recycle();
 				response.recycle();
 			} else {
 				// Clear converters so that the minimum amount of memory
 				// is used by this processor
 				request.clearEncoders();
 				response.clearEncoders();
 			}
 		}
 
 	}
 
 	// ------------------------------------------------------ Protected Methods
 
 	/**
 	 * Parse additional request parameters.
 	 */
 	protected boolean postParseRequest(org.apache.coyote.Request req, Request request,
 			org.apache.coyote.Response res, Response response) throws Exception {
 
 		// FIXME: The processor needs to set a correct scheme and port prior to
 		// this point,
 		// in ajp13 protocol does not make sense to get the port from the
 		// connector..
 		// otherwise, use connector configuration
 		if (!req.scheme().isNull()) {
 			// use processor specified scheme to determine secure state
 			request.setSecure(req.scheme().equals("https"));
 		} else {
 			// Use connector scheme and secure configuration, (defaults to
 			// "http" and false respectively)
 			req.scheme().setString(connector.getScheme());
 			request.setSecure(connector.getSecure());
 		}
 
 		// FIXME: the code below doesnt belongs to here,
 		// this is only have sense
 		// in Http11, not in ajp13..
 		// At this point the Host header has been processed.
 		// Override if the proxyPort/proxyHost are set
 		String proxyName = connector.getProxyName();
 		int proxyPort = connector.getProxyPort();
 		if (proxyPort != 0) {
 			req.setServerPort(proxyPort);
 		}
 		if (proxyName != null) {
 			req.serverName().setString(proxyName);
 		}
 
 		// URI decoding
 		MessageBytes decodedURI = req.decodedURI();
 		decodedURI.duplicate(req.requestURI());
 
 		if (decodedURI.getType() == MessageBytes.T_BYTES) {
 			// Remove any path parameters
 			ByteChunk uriBB = decodedURI.getByteChunk();
 			int semicolon = uriBB.indexOf(';', 0);
 			if (semicolon > 0) {
 				decodedURI.setBytes(uriBB.getBuffer(), uriBB.getStart(), semicolon);
 			}
 			// %xx decoding of the URL
 			try {
 				req.getURLDecoder().convert(decodedURI, false);
 			} catch (IOException ioe) {
 				res.setStatus(400);
 				res.setMessage("Invalid URI: " + ioe.getMessage());
 				return false;
 			}
 			// Normalization
 			if (!normalize(req.decodedURI())) {
 				res.setStatus(400);
 				res.setMessage("Invalid URI");
 				return false;
 			}
 			// Character decoding
 			convertURI(decodedURI, request);
 			// Check that the URI is still normalized
 			if (!checkNormalize(req.decodedURI())) {
 				res.setStatus(400);
 				res.setMessage("Invalid URI character encoding");
 				return false;
 			}
 		} else {
 			// The URL is chars or String, and has been sent using an in-memory
 			// protocol handler, we have to assume the URL has been properly
 			// decoded already
 			decodedURI.toChars();
 			// Remove any path parameters
 			CharChunk uriCC = decodedURI.getCharChunk();
 			int semicolon = uriCC.indexOf(';');
 			if (semicolon > 0) {
 				decodedURI.setChars(uriCC.getBuffer(), uriCC.getStart(), semicolon);
 			}
 		}
 
 		// Set the remote principal
 		String principal = req.getRemoteUser().toString();
 		if (principal != null) {
 			request.setUserPrincipal(new CoyotePrincipal(principal));
 		}
 
 		// Set the authorization type
 		String authtype = req.getAuthType().toString();
 		if (authtype != null) {
 			request.setAuthType(authtype);
 		}
 
 		// Request mapping.
 		MessageBytes serverName;
 		if (connector.getUseIPVHosts()) {
 			serverName = req.localName();
 			if (serverName.isNull()) {
 				// well, they did ask for it
 				res.action(ActionCode.ACTION_REQ_LOCAL_NAME_ATTRIBUTE, null);
 			}
 		} else {
 			serverName = req.serverName();
 		}
 		connector.getService().getMapper().map(serverName, decodedURI, request.getMappingData());
 		request.setContext((Context) request.getMappingData().context);
 		request.setWrapper((Wrapper) request.getMappingData().wrapper);
 
 		if (request.getMappingData().host == null) {
 			res.setStatus(400);
 			res.setMessage("Host not mapped");
 			return false;
 		}
 		if (request.getMappingData().context == null) {
 			res.setStatus(404);
 			res.setMessage("Context not mapped");
 			return false;
 		}
 		if (connector.getAllowedHosts() != null
 				&& !connector.getAllowedHosts().contains(request.getMappingData().host)) {
 			res.setStatus(403);
 			res.setMessage("Host access is forbidden through this connector");
 			return false;
 		}
 
 		// Filter trace method
 		if (!connector.getAllowTrace() && req.method().equalsIgnoreCase("TRACE")) {
 			Wrapper wrapper = request.getWrapper();
 			String header = null;
 			if (wrapper != null) {
 				String[] methods = wrapper.getServletMethods();
 				if (methods != null) {
 					for (int i = 0; i < methods.length; i++) {
 						if ("TRACE".equals(methods[i])) {
 							continue;
 						}
 						if (header == null) {
 							header = methods[i];
 						} else {
 							header += ", " + methods[i];
 						}
 					}
 				}
 			}
 			res.setStatus(405);
 			res.addHeader("Allow", header);
 			res.setMessage("TRACE method is not allowed");
 			return false;
 		}
 
 		// Discard session id if SessionTrackingMode.URL is disabled
 		if (request.getServletContext().getEffectiveSessionTrackingModes()
 				.contains(SessionTrackingMode.URL)) {
 			// Parse session Id
 			parseSessionId(req, request);
 		} else {
 			request.setRequestedSessionId(null);
 			request.setRequestedSessionURL(false);
 		}
 
 		// Possible redirect
 		MessageBytes redirectPathMB = request.getMappingData().redirectPath;
 		if (!redirectPathMB.isNull()) {
 			String redirectPath = urlEncoder.encode(redirectPathMB.toString());
 			String query = request.getQueryString();
 			if (request.isRequestedSessionIdFromURL()) {
 				// This is not optimal, but as this is not very common, it
 				// shouldn't matter
 				redirectPath = redirectPath
 						+ request.getContext().getSessionCookie().getPathParameterName()
 						+ request.getRequestedSessionId();
 			}
 			if (query != null) {
 				// This is not optimal, but as this is not very common, it
 				// shouldn't matter
 				redirectPath = redirectPath + "?" + query;
 			}
 			response.sendRedirect(redirectPath);
 			return false;
 		}
 
 		// Parse session id if SessionTrackingMode.COOKIE is enabled
 		if (request.getServletContext().getEffectiveSessionTrackingModes()
 				.contains(SessionTrackingMode.COOKIE)) {
 			parseSessionCookiesId(req, request);
 		}
 
 		return true;
 	}
 
 	/**
 	 * Parse session id in URL.
 	 */
 	protected void parseSessionId(org.apache.coyote.Request req, Request request) {
 
 		ByteChunk uriBC = req.requestURI().getByteChunk();
 		String pathParameterName = request.getContext().getSessionCookie().getPathParameterName();
 		int semicolon = uriBC.indexOf(pathParameterName, 0, pathParameterName.length(), 0);
 
 		if (semicolon > 0) {
 
 			// Parse session ID, and extract it from the decoded request URI
 			int start = uriBC.getStart();
 			int end = uriBC.getEnd();
 
 			int sessionIdStart = semicolon + pathParameterName.length();
 			int semicolon2 = uriBC.indexOf(';', sessionIdStart);
 			if (semicolon2 >= 0) {
 				request.setRequestedSessionId(new String(uriBC.getBuffer(), start + sessionIdStart,
 						semicolon2 - sessionIdStart));
 				// Extract session ID from request URI
 				byte[] buf = uriBC.getBuffer();
 				for (int i = 0; i < end - start - semicolon2; i++) {
 					buf[start + semicolon + i] = buf[start + i + semicolon2];
 				}
 				uriBC.setBytes(buf, start, end - start - semicolon2 + semicolon);
 			} else {
 				request.setRequestedSessionId(new String(uriBC.getBuffer(), start + sessionIdStart,
 						(end - start) - sessionIdStart));
 				uriBC.setEnd(start + semicolon);
 			}
 			request.setRequestedSessionURL(true);
 
 		} else {
 			request.setRequestedSessionId(null);
 			request.setRequestedSessionURL(false);
 		}
 
 	}
 
 	/**
 	 * Parse session id in URL.
 	 */
 	protected void parseSessionCookiesId(org.apache.coyote.Request req, Request request) {
 
 		// Parse session id from cookies
 		Cookies serverCookies = req.getCookies();
 		int count = serverCookies.getCookieCount();
 		if (count <= 0)
 			return;
 
 		String cookieName = request.getContext().getSessionCookie().getName();
 		for (int i = 0; i < count; i++) {
 			ServerCookie scookie = serverCookies.getCookie(i);
 			if (scookie.getName().equals(cookieName)) {
 				// Override anything requested in the URL
 				if (!request.isRequestedSessionIdFromCookie()) {
 					// Accept only the first session id cookie
 					convertMB(scookie.getValue());
 					request.setRequestedSessionId(scookie.getValue().toString());
 					request.setRequestedSessionCookie(true);
 					request.setRequestedSessionURL(false);
 				} else {
 					if (!isSessionIdValid(request, request.getRequestedSessionId())) {
 						// Replace the session id until one is valid
 						convertMB(scookie.getValue());
 						request.setRequestedSessionId(scookie.getValue().toString());
 					}
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * Return <code>true</code> if the session identifier specified identifies a
 	 * valid session.
 	 */
 	public boolean isSessionIdValid(Request request, String id) {
 		if (id == null)
 			return (false);
 		Context context = request.getContext();
 		if (context == null)
 			return (false);
 		Manager manager = context.getManager();
 		if (manager == null)
 			return (false);
 		Session session = null;
 		try {
 			session = manager.findSession(id);
 		} catch (IOException e) {
 			session = null;
 		}
 		if ((session != null) && session.isValidInternal())
 			return (true);
 		else
 			return (false);
 	}
 
 	/**
 	 * Character conversion of the URI.
 	 */
 	protected void convertURI(MessageBytes uri, Request request) throws Exception {
 
 		ByteChunk bc = uri.getByteChunk();
 		int length = bc.getLength();
 		CharChunk cc = uri.getCharChunk();
 		cc.allocate(length, -1);
 
 		String enc = connector.getURIEncoding();
 		if (enc != null) {
 			B2CConverter conv = request.getURIConverter();
 			try {
 				if (conv == null) {
 					conv = new B2CConverter(enc);
 					request.setURIConverter(conv);
 				} else {
 					conv.recycle();
 				}
 			} catch (Exception e) {
 				// Ignore
 				log.error("Invalid URI encoding; using HTTP default");
 				connector.setURIEncoding(null);
 			}
 			if (conv != null) {
 				try {
 					conv.convert(bc, cc);
 					uri.setChars(cc.getBuffer(), cc.getStart(), cc.getLength());
 					return;
 				} catch (IOException e) {
 					log.error("Invalid URI character encoding; trying ascii");
 					cc.recycle();
 				}
 			}
 		}
 
 		// Default encoding: fast conversion
 		byte[] bbuf = bc.getBuffer();
 		char[] cbuf = cc.getBuffer();
 		int start = bc.getStart();
 		for (int i = 0; i < length; i++) {
 			cbuf[i] = (char) (bbuf[i + start] & 0xff);
 		}
 		uri.setChars(cbuf, 0, length);
 
 	}
 
 	/**
 	 * Character conversion of the a US-ASCII MessageBytes.
 	 */
 	protected void convertMB(MessageBytes mb) {
 
 		// This is of course only meaningful for bytes
 		if (mb.getType() != MessageBytes.T_BYTES)
 			return;
 
 		ByteChunk bc = mb.getByteChunk();
 		CharChunk cc = mb.getCharChunk();
 		int length = bc.getLength();
 		cc.allocate(length, -1);
 
 		// Default encoding: fast conversion
 		byte[] bbuf = bc.getBuffer();
 		char[] cbuf = cc.getBuffer();
 		int start = bc.getStart();
 		for (int i = 0; i < length; i++) {
 			cbuf[i] = (char) (bbuf[i + start] & 0xff);
 		}
 		mb.setChars(cbuf, 0, length);
 
 	}
 
 	/**
 	 * Normalize URI.
 	 * <p>
 	 * This method normalizes "\", "//", "/./" and "/../". This method will
 	 * return false when trying to go above the root, or if the URI contains a
 	 * null byte.
 	 * 
 	 * @param uriMB
 	 *            URI to be normalized
 	 */
 	public static boolean normalize(MessageBytes uriMB) {
 
 		ByteChunk uriBC = uriMB.getByteChunk();
 		byte[] b = uriBC.getBytes();
 		int start = uriBC.getStart();
 		int end = uriBC.getEnd();
 
 		// An empty URL is not acceptable
 		if (start == end)
 			return false;
 
 		// URL * is acceptable
 		if ((end - start == 1) && b[start] == (byte) '*')
 			return true;
 
 		int pos = 0;
 		int index = 0;
 
 		// Replace '\' with '/'
 		// Check for null byte
 		for (pos = start; pos < end; pos++) {
 			if (b[pos] == (byte) '\\') {
 				if (ALLOW_BACKSLASH) {
 					b[pos] = (byte) '/';
 				} else {
 					return false;
 				}
 			}
 			if (b[pos] == (byte) 0) {
 				return false;
 			}
 		}
 
 		// The URL must start with '/'
 		if (b[start] != (byte) '/') {
 			return false;
 		}
 
 		// Replace "//" with "/"
 		for (pos = start; pos < (end - 1); pos++) {
 			if (b[pos] == (byte) '/') {
 				while ((pos + 1 < end) && (b[pos + 1] == (byte) '/')) {
 					copyBytes(b, pos, pos + 1, end - pos - 1);
 					end--;
 				}
 			}
 		}
 
 		// If the URI ends with "/." or "/..", then we append an extra "/"
 		// Note: It is possible to extend the URI by 1 without any side effect
 		// as the next character is a non-significant WS.
 		if (((end - start) >= 2) && (b[end - 1] == (byte) '.')) {
 			if ((b[end - 2] == (byte) '/')
 					|| ((b[end - 2] == (byte) '.') && (b[end - 3] == (byte) '/'))) {
 				b[end] = (byte) '/';
 				end++;
 			}
 		}
 
 		uriBC.setEnd(end);
 
 		index = 0;
 
 		// Resolve occurrences of "/./" in the normalized path
 		while (true) {
 			index = uriBC.indexOf("/./", 0, 3, index);
 			if (index < 0)
 				break;
 			copyBytes(b, start + index, start + index + 2, end - start - index - 2);
 			end = end - 2;
 			uriBC.setEnd(end);
 		}
 
 		index = 0;
 
 		// Resolve occurrences of "/../" in the normalized path
 		while (true) {
 			index = uriBC.indexOf("/../", 0, 4, index);
 			if (index < 0)
 				break;
 			// Prevent from going outside our context
 			if (index == 0)
 				return false;
 			int index2 = -1;
 			for (pos = start + index - 1; (pos >= 0) && (index2 < 0); pos--) {
 				if (b[pos] == (byte) '/') {
 					index2 = pos;
 				}
 			}
 			copyBytes(b, start + index2, start + index + 3, end - start - index - 3);
 			end = end + index2 - index - 3;
 			uriBC.setEnd(end);
 			index = index2;
 		}
 
 		uriBC.setBytes(b, start, end);
 
 		return true;
 
 	}
 
 	/**
 	 * Check that the URI is normalized following character decoding.
 	 * <p>
 	 * This method checks for "\", 0, "//", "/./" and "/../". This method will
 	 * return false if sequences that are supposed to be normalized are still
 	 * present in the URI.
 	 * 
 	 * @param uriMB
 	 *            URI to be checked (should be chars)
 	 */
 	public static boolean checkNormalize(MessageBytes uriMB) {
 
 		CharChunk uriCC = uriMB.getCharChunk();
 		char[] c = uriCC.getChars();
 		int start = uriCC.getStart();
 		int end = uriCC.getEnd();
 
 		int pos = 0;
 
 		// Check for '\' and 0
 		for (pos = start; pos < end; pos++) {
 			if (c[pos] == '\\') {
 				return false;
 			}
 			if (c[pos] == 0) {
 				return false;
 			}
 		}
 
 		// Check for "//"
 		for (pos = start; pos < (end - 1); pos++) {
 			if (c[pos] == '/') {
 				if (c[pos + 1] == '/') {
 					return false;
 				}
 			}
 		}
 
 		// Check for ending with "/." or "/.."
 		if (((end - start) >= 2) && (c[end - 1] == '.')) {
 			if ((c[end - 2] == '/') || ((c[end - 2] == '.') && (c[end - 3] == '/'))) {
 				return false;
 			}
 		}
 
 		// Check for "/./"
 		if (uriCC.indexOf("/./", 0, 3, 0) >= 0) {
 			return false;
 		}
 
 		// Check for "/../"
 		if (uriCC.indexOf("/../", 0, 4, 0) >= 0) {
 			return false;
 		}
 
 		return true;
 
 	}
 
 	// ------------------------------------------------------ Protected Methods
 
 	/**
 	 * Copy an array of bytes to a different position. Used during
 	 * normalization.
 	 */
 	protected static void copyBytes(byte[] b, int dest, int src, int len) {
 		for (int pos = 0; pos < len; pos++) {
 			b[pos + dest] = b[pos + src];
 		}
 	}
 
 }
