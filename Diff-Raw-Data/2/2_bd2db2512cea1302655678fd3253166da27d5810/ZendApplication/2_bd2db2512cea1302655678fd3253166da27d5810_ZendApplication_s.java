 /*******************************************************************************
  * Copyright (c) May 24, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdklib;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Random;
 
 import org.zend.sdklib.internal.library.AbstractLibrary;
 import org.zend.sdklib.internal.target.UserBasedTargetLoader;
 import org.zend.sdklib.manager.TargetsManager;
 import org.zend.sdklib.target.IZendTarget;
 import org.zend.webapi.core.WebApiClient;
 import org.zend.webapi.core.WebApiException;
 import org.zend.webapi.core.connection.auth.BasicCredentials;
 import org.zend.webapi.core.connection.auth.WebApiCredentials;
 import org.zend.webapi.core.connection.data.ApplicationInfo;
 import org.zend.webapi.core.connection.data.ApplicationsList;
 
 /**
  * Utility class which provides methods to perform operations on application.
  * 
  * @author Wojciech Galanciak, 2011
  * 
  */
 public class ZendApplication extends AbstractLibrary {
 
 	private final TargetsManager manager;
 
 	public ZendApplication() {
 		super();
 		manager = new TargetsManager(new UserBasedTargetLoader());
 	}
 
 	/**
 	 * Provides information about status of specified application(s) in selected
 	 * target.
 	 * 
 	 * @param targetId
 	 * @param applicationIds
 	 *            - array of application id(s) for which status should be
 	 *            checked
 	 * @return instance of {@link ApplicationsList} or <code>null</code> if
 	 *         there where problems with connections or target with specified id
 	 *         does not exist
 	 */
 	public ApplicationsList getStatus(String targetId, String... applicationIds) {
 		try {
 			WebApiClient client = getClient(targetId);
 			applicationIds = applicationIds == null ? new String[0]
 					: applicationIds;
 			return client.applicationGetStatus(applicationIds);
 		} catch (MalformedURLException e) {
 			log.error(e);
 		} catch (WebApiException e) {
 			log.error("Error duirng getting application status from '"
 					+ targetId + "'.");
 			log.error("\tpossible error: " + e.getMessage());
 		}
 		return null;
 	}
 
 	/**
 	 * Deploys a new application to the specified target.
 	 * 
 	 * @param path
 	 *            - path to project location or application package
 	 * @param baseUrl
 	 *            - base URL to deploy the application to. Must be an HTTP URL.
 	 * @param targetId
 	 *            - target id
 	 * @param propertiesFile
 	 *            - path to properties file which consists user deployment
 	 *            parameters
 	 * 
 	 * @param appName
 	 *            - application name
 	 * @param ignoreFailures
 	 *            - ignore failures during staging if only some servers reported
 	 *            failures
 	 * @param createVhost
 	 *            - create a virtual host based on the base URL if such a
 	 *            virtual host wasn't already created by Zend Server.
 	 * @param defaultServer
 	 *            - deploy the application on the default server; the base URL
 	 *            host provided will be ignored and replaced with
 	 *            <default-server>.
 	 * @return instance of {@link ApplicationInfo} or <code>null</code> if there
 	 *         where problems with connections or target with specified id does
 	 *         not exist or there is no package/project in specified path
 	 */
 	public ApplicationInfo deploy(String path, String baseUrl, String targetId,
 			String propertiesFile, String appName, Boolean ignoreFailures,
 			Boolean createVhost, Boolean defaultServer) {
 		File file = new File(path);
 		if (!file.exists()) {
 			log.error("Path does not exist: " + file);
 			return null;
 		}
 		File zendPackage = null;
 		// check if it is a package or a folder (project)
 		File tempFile = null;
 		if (file.isDirectory()) {
 			final String tempDir = System.getProperty("java.io.tmpdir");
 			tempFile = new File(tempDir + File.separator
 					+ new Random().nextInt());
 			tempFile.mkdir();
 			PackageBuilder builder = new PackageBuilder(path);
 			zendPackage = builder.createDeploymentPackage(tempFile);
 		} else {
 			zendPackage = file;
 		}
 		if (zendPackage != null) {
 			try {
 				WebApiClient client = getClient(targetId);
 				Map<String, String> userParams = null;
 				if (propertiesFile != null) {
 					File propsFile = new File(propertiesFile);
 					if (propsFile.exists()) {
 						userParams = getUserParameters(propsFile);
 					}
 				}
 				return client.applicationDeploy(zendPackage, baseUrl,
						ignoreFailures, userParams, appName, defaultServer,
 						defaultServer);
 			} catch (MalformedURLException e) {
 				log.error(e);
 			} catch (WebApiException e) {
 				log.error("Error during deploying application to '" + targetId
 						+ "':");
 				log.error("\tpossible error: " + e.getMessage());
 			}
 		}
 		if (tempFile != null) {
 			tempFile.deleteOnExit();
 		}
 		return null;
 	}
 
 	/**
 	 * Redeploys an existing application, whether in order to fix a problem or
 	 * to reset an installation.
 	 * 
 	 * @param targetId
 	 *            - target id
 	 * @param appId
 	 *            - application id
 	 * @param servers
 	 *            - array of server id(s) on which application should be
 	 *            redeployed
 	 * @param ignoreFailures
 	 *            - ignore failures during staging if only some servers reported
 	 *            failures
 	 * @return instance of {@link ApplicationInfo} or <code>null</code> if there
 	 *         where problems with connections or target with specified id does
 	 *         not exist or there is no application with specified id in the
 	 *         target
 	 */
 	public ApplicationInfo redeploy(String targetId, String appId,
 			String[] servers, boolean ignoreFailures) {
 		try {
 			WebApiClient client = getClient(targetId);
 			int appIdint = Integer.parseInt(appId);
 			return client
 					.applicationRedeploy(appIdint, ignoreFailures, servers);
 		} catch (MalformedURLException e) {
 			log.error(e);
 		} catch (NumberFormatException e) {
 			log.error(e.getMessage());
 		} catch (WebApiException e) {
 			log.error("Error during redeploying application to '" + targetId
 					+ "':");
 			log.error("\tpossible error: " + e.getMessage());
 		}
 		return null;
 	}
 
 	/**
 	 * Removes/undeploys an existing application.
 	 * 
 	 * @param targetId
 	 *            - target id
 	 * @param appId
 	 *            - application id
 	 * @return instance of {@link ApplicationInfo} or <code>null</code> if there
 	 *         where problems with connections or target with specified id does
 	 *         not exist or there is no application with specified id in the
 	 *         target
 	 */
 	public ApplicationInfo remove(String targetId, String appId) {
 		try {
 			WebApiClient client = getClient(targetId);
 			int appIdint = Integer.parseInt(appId);
 			return client.applicationRemove(appIdint);
 		} catch (MalformedURLException e) {
 			log.error(e);
 		} catch (NumberFormatException e) {
 			log.error(e.getMessage());
 		} catch (WebApiException e) {
 			log.error("Error during removing application from '" + targetId
 					+ "':");
 			log.error("\tpossible error: " + e.getMessage());
 		}
 		return null;
 	}
 
 	/**
 	 * Updates/redeploys an existing application.
 	 * 
 	 * @param path
 	 *            - path to project location or application package
 	 * @param targetId
 	 *            - target id
 	 * @param appId
 	 *            - application id
 	 * @param propertiesFile
 	 *            - path to properties file which consists user deployment
 	 *            parameters
 	 * @param ignoreFailures
 	 *            - ignore failures during staging if only some servers reported
 	 *            failures
 	 * @return instance of {@link ApplicationInfo} or <code>null</code> if there
 	 *         where problems with connections or target with specified id does
 	 *         not exist or there is no package/project in specified path
 	 */
 	public ApplicationInfo update(String path, String targetId, String appId,
 			String propertiesFile, Boolean ignoreFailures) {
 		File file = new File(path);
 		if (!file.exists()) {
 			log.error("Path does not exist: " + file);
 			return null;
 		}
 		File zendPackage = null;
 		// check if it is a package or a folder (project)
 		File tempFile = null;
 		if (file.isDirectory()) {
 			final String tempDir = System.getProperty("java.io.tmpdir");
 			tempFile = new File(tempDir + File.separator
 					+ new Random().nextInt());
 			tempFile.mkdir();
 			PackageBuilder builder = new PackageBuilder(path);
 			zendPackage = builder.createDeploymentPackage(tempFile);
 		} else {
 			zendPackage = file;
 		}
 		try {
 			int appIdint = Integer.parseInt(appId);
 			WebApiClient client = getClient(targetId);
 			Map<String, String> userParams = null;
 			if (propertiesFile != null) {
 				File propsFile = new File(propertiesFile);
 				if (propsFile.exists()) {
 					userParams = getUserParameters(propsFile);
 				}
 			}
 			return client.applicationUpdate(appIdint, zendPackage,
 					ignoreFailures, userParams);
 		} catch (MalformedURLException e) {
 			log.error(e);
 		} catch (WebApiException e) {
 			log.error("Error during updating application on '" + targetId
 					+ "':");
 			log.error("\tpossible error: " + e.getMessage());
 		}
 		if (tempFile != null) {
 			tempFile.deleteOnExit();
 		}
 		return null;
 	}
 
 	/**
 	 * @param targetId
 	 *            - target id
 	 * @return instance of a WebAPI client for specified target id. If target
 	 *         does not exist, it returns <code>null</code>
 	 * @throws MalformedURLException
 	 */
 	public WebApiClient getClient(String targetId) throws MalformedURLException {
 		IZendTarget target = manager.getTargetById(targetId);
 		if (target == null) {
 			log.info("Target with id '" + targetId + "' does not exist.");
 			return null;
 		}
 		WebApiCredentials credentials = new BasicCredentials(target.getKey(),
 				target.getSecretKey());
 		String hostname = target.getHost().toString();
 		if (target.getHost().getPort() == -1) {
 			hostname += ":10081";
 		}
 		return new WebApiClient(credentials, hostname);
 	}
 
 	private Map<String, String> getUserParameters(File propsFile) {
 		Map<String, String> result = null;
 		Properties p = new Properties();
 		try {
 			p.load(new FileReader(propsFile));
 			Enumeration<?> e = p.propertyNames();
 			if (e.hasMoreElements()) {
 				result = new HashMap<String, String>();
 			}
 			while (e.hasMoreElements()) {
 				String key = (String) e.nextElement();
 				result.put(key, p.getProperty(key));
 			}
 		} catch (FileNotFoundException e) {
 			log.error(e);
 		} catch (IOException e) {
 			log.error(e);
 		}
 		return result;
 	}
 
 }
