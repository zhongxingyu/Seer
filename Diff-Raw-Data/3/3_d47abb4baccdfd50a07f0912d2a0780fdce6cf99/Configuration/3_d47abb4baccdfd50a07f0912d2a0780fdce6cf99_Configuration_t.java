 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.model;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.KeyStoreManager;
 import org.eclipse.emf.emfstore.client.model.util.ConfigurationProvider;
 import org.eclipse.emf.emfstore.client.model.util.DefaultWorkspaceLocationProvider;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.server.LocationProvider;
 import org.eclipse.emf.emfstore.server.model.ClientVersionInfo;
 import org.osgi.framework.Bundle;
 
 /**
  * Represents the current Workspace Configuration.
  * 
  * @author koegel
  */
 public final class Configuration {
 
 	private static final String CLIENT_NAME = "emfstore eclipse client";
 	private static final String MODEL_VERSION_FILENAME = "modelReleaseNumber";
 	private static final String UPS = ".ups";
 	private static final String UOC = ".uoc";
 	private static final String PROJECT_FOLDER = "project";
 	private static final String PS = "ps-";
 	private static final String UPF = ".upf";
 	private static final String PLUGIN_BASEDIR = "pluginData";
 	private static boolean testing;
 	private static LocationProvider locationProvider;
 	private static EditingDomain editingDomain;
 
 	private Configuration() {
 		// nothing to do
 	}
 
 	/**
 	 * Get the Workspace directory.
 	 * 
 	 * @return the workspace directory path string
 	 */
 	public static String getWorkspaceDirectory() {
 		String workspaceDirectory = getLocationProvider()
 				.getWorkspaceDirectory();
 		File workspace = new File(workspaceDirectory);
 		if (!workspace.exists()) {
 			workspace.mkdirs();
 		}
 		if (!workspaceDirectory.endsWith(File.separator)) {
 			return workspaceDirectory + File.separatorChar;
 		}
 		return workspaceDirectory;
 	}
 
 	/**
 	 * Returns the registered {@link LocationProvider} or if not existent, the
 	 * {@link DefaultWorkspaceLocationProvider}.
 	 * 
 	 * @return workspace location provider
 	 */
 	public static LocationProvider getLocationProvider() {
 		if (locationProvider == null) {
 			IConfigurationElement[] rawExtensions = Platform
 					.getExtensionRegistry()
 					.getConfigurationElementsFor(
 							"org.eclipse.emf.emfstore.client.workspaceLocationProvider");
 			for (IConfigurationElement extension : rawExtensions) {
 				try {
 					Object executableExtension = extension
 							.createExecutableExtension("providerClass");
 					if (executableExtension instanceof LocationProvider) {
 						locationProvider = (LocationProvider) executableExtension;
 					}
 				} catch (CoreException e) {
 					String message = "Error while instantiating location provider, switching to default location!";
 					ModelUtil.logWarning(message, e);
 				}
 			}
 			if (locationProvider == null) {
 				locationProvider = new DefaultWorkspaceLocationProvider();
 			}
 		}
 
 		return locationProvider;
 	}
 
 	/**
 	 * Get the Workspace file path.
 	 * 
 	 * @return the workspace file path string
 	 */
 	public static String getWorkspacePath() {
 		return getWorkspaceDirectory() + "workspace.ucw";
 	}
 
 	/**
 	 * Get the default resource save options.
 	 * 
 	 * @return the resource save options
 	 */
 	public static Map<Object, Object> getResourceSaveOptions() {
 		// MK: the options below should only be used with resourcemodification
 		// tracking enabled
 		// if (resourceSaveOptions == null) {
 		// resourceSaveOptions = new HashMap<Object, Object>();
 		// resourceSaveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED,
 		// Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
 		// }
 		return null;
 	}
 
 	/**
 	 * Get the default server info.
 	 * 
 	 * @return server info
 	 */
 	public static List<ServerInfo> getDefaultServerInfos() {
 		IConfigurationElement[] rawExtensions = Platform
 				.getExtensionRegistry()
 				.getConfigurationElementsFor(
 						"org.eclipse.emf.emfstore.client.defaultConfigurationProvider");
 		for (IConfigurationElement extension : rawExtensions) {
 			try {
 				ConfigurationProvider provider = (ConfigurationProvider) extension
 						.createExecutableExtension("providerClass");
 				List<ServerInfo> defaultServerInfos = provider
 						.getDefaultServerInfos();
 				if (defaultServerInfos != null) {
 					return defaultServerInfos;
 				}
 			} catch (CoreException e) {
 				// fail silently
 			}
 		}
 
 		ArrayList<ServerInfo> result = new ArrayList<ServerInfo>();
 		result.add(getLocalhostServerInfo());
 		return result;
 	}
 
 	private static ServerInfo getLocalhostServerInfo() {
 		ServerInfo serverInfo = ModelFactory.eINSTANCE.createServerInfo();
 		serverInfo.setName("Localhost Server");
 		serverInfo.setPort(8080);
 		serverInfo.setUrl("localhost");
 		serverInfo.setCertificateAlias(KeyStoreManager.DEFAULT_CERTIFICATE);
 
 		Usersession superUsersession = ModelFactory.eINSTANCE
 				.createUsersession();
 		superUsersession.setServerInfo(serverInfo);
 		superUsersession.setPassword("super");
 		superUsersession.setSavePassword(true);
 		superUsersession.setUsername("super");
 		serverInfo.setLastUsersession(superUsersession);
 
 		return serverInfo;
 	}
 
 	/**
 	 * Returns maximum number of model elements allowed per resource.
 	 * 
 	 * @return the maximum number
 	 */
 	public static int getMaxMECountPerResource() {
 		return 1000;
 	}
 
 	/**
 	 * Returns maximum size of of a resource file on expand.
 	 * 
 	 * @return the maximum number
 	 */
 	public static int getMaxResourceFileSizeOnExpand() {
 		return 100000;
 	}
 
 	/**
 	 * Get the client version as in the org.eclipse.emf.emfstore.client manifest
 	 * file.
 	 * 
 	 * @return the client version number
 	 */
 	public static ClientVersionInfo getClientVersion() {
 		ClientVersionInfo clientVersionInfo = org.eclipse.emf.emfstore.server.model.ModelFactory.eINSTANCE
 				.createClientVersionInfo();
 		clientVersionInfo.setName(CLIENT_NAME);
 
 		Bundle emfStoreBundle = Platform
 				.getBundle("org.eclipse.emf.emfstore.client");
 		String emfStoreVersionString = (String) emfStoreBundle.getHeaders()
 				.get(org.osgi.framework.Constants.BUNDLE_VERSION);
 
 		clientVersionInfo.setVersion(emfStoreVersionString);
 		return clientVersionInfo;
 	}
 
 	/**
 	 * Determine if this is a release version or not.
 	 * 
 	 * @return true if it is a release version
 	 */
 	public static boolean isReleaseVersion() {
 		return !isInternalReleaseVersion()
 				&& !getClientVersion().getVersion().endsWith("qualifier");
 	}
 
 	/**
 	 * Determines if this is an internal release or not.
 	 * 
 	 * @return true if it is an internal release
 	 */
 	public static boolean isInternalReleaseVersion() {
 		return getClientVersion().getVersion().endsWith("internal");
 	}
 
 	/**
 	 * Determines if this is an developer version or not.
 	 * 
 	 * @return true if it is a developer version
 	 */
 	public static boolean isDeveloperVersion() {
 		return !isReleaseVersion() && !isInternalReleaseVersion();
 	}
 
 	/**
 	 * Return the file extension for project space files.
 	 * 
 	 * @return the file extension
 	 */
 	public static String getProjectSpaceFileExtension() {
 		return UPS;
 	}
 
 	/**
 	 * Return the file extension for operation composite files.
 	 * 
 	 * @return the file extension
 	 */
 	public static String getOperationCompositeFileExtension() {
 		return UOC;
 	}
 
 	/**
 	 * Return the name of the project folder.
 	 * 
 	 * @return the folder name
 	 */
 	public static String getProjectFolderName() {
 		return PROJECT_FOLDER;
 	}
 
 	/**
 	 * Return the prefix of the project space directory.
 	 * 
 	 * @return the prefix
 	 */
 	public static String getProjectSpaceDirectoryPrefix() {
 		return PS;
 	}
 
 	/**
 	 * Return project fragement file extension.
 	 * 
 	 * @return the file extension
 	 */
 	public static String getProjectFragmentFileExtension() {
 		return UPF;
 	}
 
 	/**
 	 * Return the name of the model release number file. This file identifies
 	 * the release number of the model in the workspace.
 	 * 
 	 * @return the file name
 	 */
 	public static String getModelReleaseNumberFileName() {
 		return getWorkspaceDirectory() + MODEL_VERSION_FILENAME;
 	}
 
 	/**
 	 * If we are running tests. In this case the workspace will be created in
 	 * USERHOME/.emfstore.test.
 	 * 
 	 * @param testing
 	 *            the testing to set
 	 */
 	public static void setTesting(boolean testing) {
 		Configuration.testing = testing;
 	}
 
 	/**
 	 * @return if we are running tests. In this case the workspace will be
 	 *         created in USERHOME/.emfstore.test.
 	 */
 	public static boolean isTesting() {
 		return testing;
 	}
 
 	/**
 	 * Return the path of the plugin data directory inside the emfstore
 	 * workspace (trailing file separator included).
 	 * 
 	 * @return the plugin data directory absolute path as string
 	 */
 	public static String getPluginDataBaseDirectory() {
 		return getWorkspaceDirectory() + PLUGIN_BASEDIR + File.separatorChar;
 	}
 
 	/**
 	 * Retrieve the editing domain. Will return null until the domain is
 	 * initialized by the WorkspaceManager.
 	 * 
 	 * @return the workspace editing domain
 	 */
 	public static EditingDomain getEditingDomain() {
 		return Configuration.editingDomain;
 	}
 
 	/**
 	 * Sets the EditingDomain.
 	 * 
 	 * @param editingDomain
 	 *            new domain.
 	 */
 	public static void setEditingDomain(EditingDomain editingDomain) {
 		Configuration.editingDomain = editingDomain;
 	}
 
 	/**
 	 * Determines whether to use resource splitting.
 	 * 
 	 * @return true of resource splitting is enabled.
 	 */
 	private static Boolean resourceSplitting;
 
 	public static boolean isResourceSplittingEnabled() {
 		if (resourceSplitting != null) {
 			return true;
 		}
 		resourceSplitting = new Boolean(false);
 		IConfigurationElement[] rawExtensions = Platform.getExtensionRegistry()
 				.getConfigurationElementsFor(
 						"org.eclipse.emf.emfstore.client.persistence.options");
 		for (IConfigurationElement extension : rawExtensions) {
 			resourceSplitting = new Boolean(extension.getAttribute("enabled"));
 		}

		return resourceSplitting;
 	}
 }
