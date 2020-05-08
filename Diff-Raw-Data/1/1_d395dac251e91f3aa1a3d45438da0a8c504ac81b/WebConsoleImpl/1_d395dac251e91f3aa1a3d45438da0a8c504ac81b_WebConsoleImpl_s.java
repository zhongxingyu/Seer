 /*
  * Copyright 2011 Future Systems
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.webconsole.impl;
 
 import java.net.InetSocketAddress;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.httpd.BundleResourceServlet;
 import org.araqne.httpd.FileDownloadService;
 import org.araqne.httpd.FileDownloadServlet;
 import org.araqne.httpd.HttpContext;
 import org.araqne.httpd.HttpContextRegistry;
 import org.araqne.httpd.HttpService;
 import org.araqne.httpd.WebSocket;
 import org.araqne.httpd.WebSocketFrame;
 import org.araqne.httpd.WebSocketListener;
 import org.araqne.msgbus.Message;
 import org.araqne.msgbus.MessageBus;
 import org.araqne.msgbus.Session;
 import org.araqne.msgbus.handler.CallbackType;
 import org.araqne.msgbus.handler.MsgbusMethod;
 import org.araqne.msgbus.handler.MsgbusPlugin;
 import org.araqne.webconsole.WebConsole;
 import org.osgi.framework.BundleContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @MsgbusPlugin
 @Component(name = "webconsole")
 @Provides(specifications = { WebConsole.class })
 public class WebConsoleImpl implements WebConsole, WebSocketListener {
 	private final Logger logger = LoggerFactory.getLogger(WebConsoleImpl.class.getName());
 
 	private BundleContext bc;
 
 	@Requires
 	private HttpService httpd;
 
 	@Requires
 	private MessageBus msgbus;
 
 	@Requires
 	private FileDownloadService downloadService;
 
 	private ConcurrentMap<InetSocketAddress, WebSocketSession> sessions;
 
 	public WebConsoleImpl(BundleContext bc) {
 		this.bc = bc;
 	}
 
 	@Validate
 	public void start() {
 		sessions = new ConcurrentHashMap<InetSocketAddress, WebSocketSession>();
 		HttpContextRegistry contextRegistry = httpd.getContextRegistry();
 		HttpContext ctx = contextRegistry.ensureContext("webconsole");
 		ctx.addServlet("webconsole", new BundleResourceServlet(bc.getBundle(), "/WEB-INF"), "/*");
 		ctx.addServlet("downloader", new FileDownloadServlet(downloadService), "/downloader");
 		ctx.getWebSocketManager().addListener(this);
 	}
 
 	@Invalidate
 	public void stop() {
 		if (httpd != null) {
 			HttpContextRegistry contextRegistry = httpd.getContextRegistry();
 			HttpContext ctx = contextRegistry.ensureContext("webconsole");
 			ctx.removeServlet("webconsole");
 			ctx.removeServlet("downloader");
 			ctx.getWebSocketManager().removeListener(this);
 		}
 	}
 
 	@Override
 	public void onConnected(WebSocket socket) {
 		logger.trace("araqne webconsole: websocket connected [{}]", socket);
 		WebSocketSession webSocketSession = new WebSocketSession(socket);
 		sessions.put(socket.getRemoteAddress(), webSocketSession);
 		msgbus.openSession(webSocketSession);
 	}
 
 	@Override
 	public void onDisconnected(WebSocket socket) {
 		WebSocketSession session = sessions.get(socket.getRemoteAddress());
 		if (session == null)
 			return;
 
 		logger.trace("araqne webconsole: websocket disconnected [{}]", session);
 		msgbus.closeSession(session);
 	}
 
 	@Override
 	public void onMessage(WebSocket socket, WebSocketFrame frame) {
 		WebSocketSession session = sessions.get(socket.getRemoteAddress());
 		if (session == null) {
 			logger.error("araqne webconsole: session not found for [{}]", socket);
 			return;
 		}
 
 		logger.trace("araqne webconsole: websocket frame [{}]", frame);
 		Message msg = AraqneMessageDecoder.decode(session, frame.getTextData());
 		if (msg != null)
 			msgbus.dispatch(session, msg);
 	}
 
 	@MsgbusMethod(type = CallbackType.SessionClosed)
 	public void onSessionClose(Session session) {
 		logger.debug("araqne webconsole: session [{}] closed", session);
 
 		for (InetSocketAddress key : sessions.keySet()) {
 			WebSocketSession wss = sessions.get(key);
 			if (wss.getGuid().equals(session.getGuid())) {
 				logger.info("araqne webconsole: kill websocket session [{}]", wss);
 				wss.close();
 				sessions.remove(key);
 				break;
 			}
 		}
 	}
 }
