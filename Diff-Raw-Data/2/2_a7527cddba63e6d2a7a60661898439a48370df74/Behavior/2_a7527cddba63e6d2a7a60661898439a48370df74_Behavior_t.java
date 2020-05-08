 /*******************************************************************************
  * Copyright (c) 2013 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Otto von Wesendonk
  * Edgar Mueller
  * Maximilian Koegel
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.configuration;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.emfstore.client.ESServer;
 import org.eclipse.emf.emfstore.client.handler.ESChecksumErrorHandler;
 import org.eclipse.emf.emfstore.client.provider.ESClientConfigurationProvider;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESExtensionElement;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESExtensionPoint;
 import org.eclipse.emf.emfstore.internal.client.model.ModelFactory;
 import org.eclipse.emf.emfstore.internal.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.internal.client.model.Usersession;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.KeyStoreManager;
 import org.eclipse.emf.emfstore.internal.client.model.impl.api.ESServerImpl;
 import org.eclipse.emf.emfstore.internal.client.model.util.ChecksumErrorHandler;
 
 /**
  * Configuration options that influence the behavior of the client.
  * This currently includes:
  * <ul>
  * <li>checksum error handler</li>
  * <li>autosave</li>
  * <li>default server configuration</li>
  * <li>
  * 
  * @author emueller
  * @author ovonwesen
  * @author mkoegel
  */
 public class Behavior {
 
 	/**
 	 * The checksum value that is used in case no checksum should be computed.
 	 */
 	public static final long NO_CHECKSUM = -1;
 	private static final String AUTO_SAVE_EXTENSION_POINT_ATTRIBUTE_NAME = "autoSave";
 	private static Boolean autoSave;
 	private ESChecksumErrorHandler checksumErrorHandler;
 
 	public Behavior() {
 	}
 
 	/**
 	 * Whether to enable the automatic saving of the workspace.
 	 * If disabled, performance improves vastly, but clients have to
 	 * perform the saving of the workspace manually.
 	 * 
 	 * @param enabled whether to enable auto save
 	 */
 	public void setAutoSave(boolean enabled) {
 		autoSave = new Boolean(enabled);
 	}
 
 	/**
 	 * Whether auto-save is enabled.
 	 * 
 	 * @return true, if auto-save is enabled, false otherwise
 	 */
 	public static boolean isAutoSaveEnabled() {
 		if (autoSave == null) {
 			autoSave = new ESExtensionPoint("org.eclipse.emf.emfstore.client.recordingOptions")
 				.getBoolean(
 					AUTO_SAVE_EXTENSION_POINT_ATTRIBUTE_NAME, false);
 		}
 		return autoSave;
 	}
 
 	/**
 	 * Whether the checksum check is active. If true, and checksum comparison fails, an {@link ESChecksumErrorHandler}
 	 * will be active.
 	 * 
 	 * @return true, if the checksum comparison is activated, false otherwise
 	 */
 	public boolean isChecksumCheckActive() {
 		ESExtensionPoint extensionPoint = new ESExtensionPoint("org.eclipse.emf.emfstore.client.checksumErrorHandler");
 		return extensionPoint.getBoolean("isActive", true);
 	}
 
 	/**
 	 * Returns the active {@link ESChecksumErrorHandler}. The default is {@link ChecksumErrorHandler#AUTOCORRECT}.
 	 * 
 	 * @return the active checksum error handler
 	 */
 	public ESChecksumErrorHandler getChecksumErrorHandler() {
 
 		if (checksumErrorHandler == null) {
 
 			ESExtensionPoint extensionPoint = new ESExtensionPoint(
 				"org.eclipse.emf.emfstore.client.checksumErrorHandler");
 
 			ESExtensionElement elementWithHighestPriority = extensionPoint.getElementWithHighestPriority();
 
 			if (elementWithHighestPriority != null) {
 				ESChecksumErrorHandler errorHandler = elementWithHighestPriority
 					.getClass("errorHandler",
 						ESChecksumErrorHandler.class);
 
 				if (errorHandler != null) {
 					checksumErrorHandler = errorHandler;
 				}
 			}
 
 			if (checksumErrorHandler == null) {
				checksumErrorHandler = ChecksumErrorHandler.CANCEL;
 			}
 		}
 
 		return checksumErrorHandler;
 	}
 
 	/**
 	 * Set the active {@link ESChecksumErrorHandler}.
 	 * 
 	 * @param errorHandler
 	 *            the error handler to be set
 	 */
 	public void setChecksumErrorHandler(ESChecksumErrorHandler errorHandler) {
 		checksumErrorHandler = errorHandler;
 	}
 
 	/**
 	 * Get the default server info.
 	 * 
 	 * @return server info
 	 */
 	public List<ServerInfo> getDefaultServerInfos() {
 		ESClientConfigurationProvider provider = new ESExtensionPoint(
 			"org.eclipse.emf.emfstore.client.defaultConfigurationProvider")
 			.getClass("providerClass",
 				ESClientConfigurationProvider.class);
 		ArrayList<ServerInfo> result = new ArrayList<ServerInfo>();
 		if (provider != null) {
 			List<ESServer> defaultServerInfos = provider.getDefaultServerInfos();
 
 			for (ESServer server : defaultServerInfos) {
 				result.add(((ESServerImpl) server).toInternalAPI());
 			}
 
 			return result;
 		}
 		result.add(getLocalhostServerInfo());
 		return result;
 	}
 
 	private ServerInfo getLocalhostServerInfo() {
 		ServerInfo serverInfo = ModelFactory.eINSTANCE.createServerInfo();
 		serverInfo.setName("Localhost Server");
 		serverInfo.setPort(8080);
 		serverInfo.setUrl("localhost");
 		serverInfo.setCertificateAlias(KeyStoreManager.DEFAULT_CERTIFICATE);
 
 		Usersession superUsersession = ModelFactory.eINSTANCE.createUsersession();
 		superUsersession.setServerInfo(serverInfo);
 		superUsersession.setPassword("super");
 		superUsersession.setSavePassword(true);
 		superUsersession.setUsername("super");
 		serverInfo.setLastUsersession(superUsersession);
 
 		return serverInfo;
 	}
 
 }
