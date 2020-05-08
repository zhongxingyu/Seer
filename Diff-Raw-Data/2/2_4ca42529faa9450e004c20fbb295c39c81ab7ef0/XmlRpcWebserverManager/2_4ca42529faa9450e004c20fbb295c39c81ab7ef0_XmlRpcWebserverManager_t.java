 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * wesendon
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.server.connection.xmlrpc;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLServerSocketFactory;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.apache.xmlrpc.server.PropertyHandlerMapping;
 import org.apache.xmlrpc.server.XmlRpcServer;
 import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
 import org.apache.xmlrpc.webserver.WebServer;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.internal.server.EMFStoreController;
 import org.eclipse.emf.emfstore.internal.server.ServerConfiguration;
 import org.eclipse.emf.emfstore.internal.server.connection.ServerKeyStoreManager;
 import org.eclipse.emf.emfstore.internal.server.connection.xmlrpc.util.EObjectTypeConverterFactory;
 import org.eclipse.emf.emfstore.internal.server.connection.xmlrpc.util.EObjectTypeFactory;
 import org.eclipse.emf.emfstore.internal.server.exceptions.FatalESException;
 import org.eclipse.emf.emfstore.internal.server.exceptions.ServerKeyStoreException;
 
 /**
  * Manages the webserver for XML RPC connections.
  * 
  * @author wesendon
  */
 public final class XmlRpcWebserverManager {
 
 	/**
 	 * Initializes the singleton instance statically.
 	 */
 	private static class SingletonHolder {
 		public static final XmlRpcWebserverManager INSTANCE = new XmlRpcWebserverManager();
 	}
 
 	/**
 	 * Returns an instance of the webserver manager.
 	 * 
 	 * @return instance of websever manager.
 	 */
 	public static XmlRpcWebserverManager getInstance() {
 		return SingletonHolder.INSTANCE;
 	}
 
 	private WebServer webServer;
 	private final int port;
 
 	private XmlRpcWebserverManager() {
 		int tmp = 8080;
 		try {
 			tmp = Integer.valueOf(ServerConfiguration.getProperties().getProperty(ServerConfiguration.XML_RPC_PORT));
 		} catch (final NumberFormatException e) {
 			tmp = Integer.valueOf(ServerConfiguration.XML_RPC_PORT_DEFAULT);
 		}
 		port = tmp;
 	}
 
 	/**
 	 * Starts the server.
 	 * 
 	 * @throws FatalESException in case of failure
 	 */
 	public void initServer() throws FatalESException {
 		if (webServer != null) {
 			return;
 		}
 		try {
			webServer = new WebServer(port) {
 
 				@Override
 				protected ServerSocket createServerSocket(int pPort, int backlog, InetAddress addr) throws IOException {
 
 					SSLServerSocketFactory serverSocketFactory = null;
 
 					try {
 						final SSLContext context = SSLContext.getInstance("TLS");
 						context.init(ServerKeyStoreManager.getInstance().getKeyManagerFactory().getKeyManagers(), null,
 							null);
 						serverSocketFactory = context.getServerSocketFactory();
 					} catch (final NoSuchAlgorithmException exception) {
 						shutdown(serverSocketFactory, exception);
 					} catch (final KeyManagementException exception) {
 						shutdown(serverSocketFactory, exception);
 					} catch (final ServerKeyStoreException exception) {
 						shutdown(serverSocketFactory, exception);
 					}
 
 					return serverSocketFactory.createServerSocket(pPort, backlog, addr);
 				}
 
 				private void shutdown(SSLServerSocketFactory serverSocketFactory, Exception e) {
 					if (serverSocketFactory == null) {
 						ModelUtil.logException("Couldn't initialize server socket.", e);
 						EMFStoreController.getInstance().shutdown(new FatalESException());
 					}
 				}
 			};
 
 			ModelUtil.logInfo("Started XML RPC Webserver on port: " + port);
 
 			final XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
 			xmlRpcServer.setTypeFactory(new EObjectTypeFactory(xmlRpcServer));
 			final EObjectTypeConverterFactory pFactory = new EObjectTypeConverterFactory();
 			xmlRpcServer.setTypeConverterFactory(pFactory);
 
 			final PropertyHandlerMapping phm = new PropertyHandlerMapping();
 
 			phm.setVoidMethodEnabled(true);
 			phm.setTypeConverterFactory(pFactory);
 
 			xmlRpcServer.setHandlerMapping(phm);
 
 			final XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
 			serverConfig.setEnabledForExtensions(true);
 			serverConfig.setEnabledForExceptions(true);
 			serverConfig.setContentLengthOptional(true);
 
 			webServer.start();
 		} catch (final IOException e) {
 			throw new FatalESException("Couldn't start webserver", e);
 		}
 	}
 
 	/**
 	 * Adds a handler to the webserver.
 	 * 
 	 * @param handlerName handler name
 	 * @param clazz class of server interface
 	 * @throws FatalESException in case of failure
 	 */
 	public void addHandler(String handlerName, Class<?> clazz) throws FatalESException {
 		try {
 			final PropertyHandlerMapping mapper = (PropertyHandlerMapping) webServer.getXmlRpcServer()
 				.getHandlerMapping();
 			mapper.addHandler(handlerName, clazz);
 		} catch (final XmlRpcException e) {
 			throw new FatalESException("Couldn't add handler", e);
 		}
 	}
 
 	/**
 	 * Removes a handler from the Webserver.
 	 * 
 	 * @param handlerName the handler's name
 	 * @return true, if other handler still available
 	 */
 	public boolean removeHandler(String handlerName) {
 		final PropertyHandlerMapping mapper = (PropertyHandlerMapping) webServer.getXmlRpcServer().getHandlerMapping();
 		mapper.removeHandler(handlerName);
 		try {
 			return mapper.getListMethods().length > 0;
 		} catch (final XmlRpcException e) {
 			return false;
 		}
 	}
 
 	/**
 	 * Stops the server.
 	 * 
 	 * @param force
 	 *            should be set to {@code true}, if handler should be stopped forcefully
 	 */
 	public void stopServer(boolean force) {
 		webServer.shutdown();
 		webServer = null;
 	}
 }
