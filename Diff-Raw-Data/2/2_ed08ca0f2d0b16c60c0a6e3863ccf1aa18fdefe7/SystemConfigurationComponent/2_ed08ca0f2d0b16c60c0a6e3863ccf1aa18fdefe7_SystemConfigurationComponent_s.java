 /*******************************************************************************
  * Copyright (c) 2012 Committers of lunifera.org - Lunifera.org.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Cristiano Gavião - initial API and implementation
  *******************************************************************************/
 package org.lunifera.runtime.component.configuration.manager.spi;
 
 import static java.util.Arrays.asList;
 import static org.osgi.framework.Constants.SERVICE_PID;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.lunifera.runtime.component.configuration.manager.service.IConfigurationService;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.BundleListener;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.wiring.BundleWiring;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.osgi.service.component.ComponentContext;
 import org.osgi.service.log.LogService;
 
 /**
  * To use this class you MUST ensure the the following bundles are with
  * StartLevel set to 1 and with auto-start = true: - org.eclipse.equinox.ds -
  * org.eclipse.equinox.cm => org.osgi.service.cm.ConfigurationAdmin -
  * org.eclipse.equinox.log => org.osgi.service.log.LogService
  * 
  * @author cvgaviao
  * 
  */
 public class SystemConfigurationComponent implements IConfigurationService,
 		BundleListener {
 
 	private ConfigurationAdmin configurationAdmin;
 	private LogService logService;
 	private BundleContext bundleContext;
 	private String p_includePattern;
 	private String p_excludePattern;
 
 	private Set<String> processedBundles = new HashSet<String>();
 
 	/**
 	 * It is possible to configure using a Properties File dropped on eclipse
 	 * "dropins" folder, or the Properties cached in OSGi System Preference
 	 * workspace. The system should be instructed to use the newer between both
 	 * options, or you could choose one.
 	 * 
 	 * @param context
 	 * @param properties
 	 */
 	protected void activate(ComponentContext context,
 			Map<String, Object> properties) {
 
 		bundleContext = context.getBundleContext();
 
 		if (getLogService() == null) {
 			System.out
 					.println("OSGi Logging Service was not started properly!");
 		}
 
 		if (getConfigurationAdminService() == null) {
 			getLogService().log(LogService.LOG_ERROR,
 					"ConfigurationAdmin Service was not setup properly !");
 		}
 
 		// get the configuration info from default
 		String p_basePath = (String) properties
 				.get(IConfigurationService.CONFIGURATION_BASE_DIR_FIELD_NAME);
 		p_includePattern = (String) properties
 				.get(IConfigurationService.CONFIGURATION_INCLUDE_PATTERN_FIELD_NAME);
 		p_excludePattern = (String) properties
 				.get(IConfigurationService.CONFIGURATION_EXCLUDE_PATTERN_FIELD_NAME);
 
 		// initialize configurations
 		initialize(bundleContext.getBundle(), p_basePath, p_includePattern,
 				p_excludePattern);
 
 		// scan all Lunifera-Config bundles for configurations
 		bundleContext.addBundleListener(this);
 		for (Bundle bundle : bundleContext.getBundles()) {
 			scanBundle(bundle, p_includePattern, p_excludePattern);
 		}
 	}
 
 	/**
 	 * Scans the given bundle for Lunifera-Config-files.
 	 * 
 	 * @param bundle
 	 *            The bundle to be scanned.
 	 * @param p_includePattern
 	 * @param p_excludePattern
 	 */
 	protected void scanBundle(Bundle bundle, String p_includePattern,
 			String p_excludePattern) {
 		if (bundle.getState() != Bundle.ACTIVE) {
 			return;
 		}
 
 		String root = (String) bundle.getHeaders().get(MANIFESTHEADER__CONFIG);
 		if (root == null) {
 			return;
 		}
 		String basePath = "/";
 		if (!root.equals("")) {
 			basePath += root;
 		}
 		initialize(bundle, basePath, p_includePattern, p_excludePattern);
 	}
 
 	@Override
 	public void bundleChanged(BundleEvent event) {
 		if (event.getBundle() == this.bundleContext.getBundle()) {
 			if (event.getType() == BundleEvent.STOPPED) {
 				bundleContext.removeBundleListener(this);
 				return;
 			}
 		}
 
 		// new bundle was installed
 		if (event.getType() == BundleEvent.STARTED) {
 			if (!processedBundles.contains(event.getBundle().getSymbolicName())) {
 				scanBundle(event.getBundle(), p_includePattern,
 						p_excludePattern);
 			}
 		} else if (event.getType() == BundleEvent.UNINSTALLED) {
 			processedBundles.remove(event.getBundle().getSymbolicName());
 		}
 	}
 
 	/**
 	 * Initializes the configuration after activating.
 	 * 
 	 * @param p_basePath
 	 * @param p_includePattern
 	 * @param p_excludePattern
 	 */
 	protected void initialize(Bundle bundle, String p_basePath,
 			String p_includePattern, String p_excludePattern) {
 
 		processedBundles.add(bundle.getSymbolicName());
 
 		List<String> configFiles = scan(bundle, p_basePath,
 				asList(p_includePattern), asList(p_excludePattern));
 		for (String configFile : configFiles) {
 
 			Map<String, String> pids = extractPidsFromConfigFileName(configFile);
 			Dictionary<String, Object> values = dictionaryFromPropertiesFile(
 					bundle, configFile);
 
 			if (pids.containsKey(SERVICE_FACTORYPID)) {
 				initializeFactoryConfigurationStore(
 						pids.get(SERVICE_FACTORYPID), pids.get(SERVICE_PID),
 						values);
 			} else {
 				initializeConfigurationStore(pids.get(SERVICE_PID), values);
 			}
 		}
 	}
 
 	protected void bindConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
 		this.configurationAdmin = configurationAdmin;
 		getLogService().log(LogService.LOG_DEBUG,
 				"Binded ConfigurationAdmin Service.");
 	}
 
 	protected void bindLogService(LogService logService) {
 		this.logService = logService;
 		getLogService().log(LogService.LOG_DEBUG, "Binded LogService.");
 	}
 
 	protected void deactivate(ComponentContext context,
 			Map<String, Object> properties) {
 		bundleContext.removeBundleListener(this);
 		bundleContext = null;
 
 		getLogService().log(LogService.LOG_DEBUG,
 				"Configuration Manager Wrapper was deactivated.");
 	}
 
 	@Override
 	public void deleteFactoryProperties(String factoryPid, String pid) {
 		Configuration configuration;
 		try {
 			configuration = findFactoryConfiguration(factoryPid, pid);
 			if (configuration != null)
 				try {
 					configuration.delete();
 				} catch (IOException e) {
 					getLogService().log(LogService.LOG_ERROR,
 							"Error on setup Configuration Service", e);
 				}
 			else
 				getLogService().log(
 						LogService.LOG_DEBUG,
 						"no configuration for factoryPid '" + factoryPid
 								+ "' and pid '" + pid + "'");
 		} catch (IOException e1) {
 			getLogService().log(
 					LogService.LOG_ERROR,
 					"no configuration for factoryPid '" + factoryPid
 							+ "' and pid '" + pid + "'", e1);
 		}
 	}
 
 	public void deleteProperties(String pid) {
 		Configuration configuration;
 		try {
 			configuration = findConfiguration(pid);
 			if (configuration != null)
 				try {
 					configuration.delete();
 				} catch (IOException e) {
 					getLogService().log(LogService.LOG_ERROR,
 							"Error on setup Configuration Service", e);
 				}
 			else
 				getLogService().log(LogService.LOG_DEBUG,
 						"no configuration for pid '" + pid + "'");
 		} catch (IOException e1) {
 			getLogService().log(LogService.LOG_ERROR,
 					"no configuration for pid '" + pid + "'", e1);
 		}
 
 	}
 
 	private Dictionary<String, Object> dictionaryFromPropertiesFile(
 			Bundle bundle, String configFile) {
 		// Read properties file.
 		Properties properties = new Properties();
 		Dictionary<String, Object> map = new Hashtable<String, Object>();
 
 		try {
 			URL configURL = bundle.getResource(configFile);
 			if (configURL != null) {
 				InputStream input = null;
 				try {
 					input = configURL.openStream();
 					properties.load(input);
 
 					// process your input here or in separate method
 					Enumeration<?> list = properties.propertyNames();
 					while (list.hasMoreElements()) {
 						String propertyName = (String) list.nextElement();
 						map.put(propertyName, properties.get(propertyName));
 					}
 
 				} finally {
 					input.close();
 				}
 			} else {
 				getLogService()
 						.log(LogService.LOG_ERROR,
 								"Error reading configuration file '"
 										+ configFile + "'");
 			}
 
 		} catch (IOException e) {
 			getLogService().log(LogService.LOG_ERROR,
 					"Error reading configuration file '" + configFile + "'", e);
 		}
 		return map;
 	}
 
 	private Map<String, String> extractPidsFromConfigFileName(String configFile) {
 		Map<String, String> pids = new HashMap<String, String>(2);
		String separator = System.getProperty("file.separator");
 		String filename;
 
 		// Remove the path up to the filename.
 		int lastSeparatorIndex = configFile.lastIndexOf(separator);
 		if (lastSeparatorIndex == -1) {
 			filename = configFile;
 		} else {
 			filename = configFile.substring(lastSeparatorIndex + 1);
 		}
 
 		// Factory (if it exists)
 		int qualifierIndex = filename.lastIndexOf("_");
 		int extensionIndex = filename.lastIndexOf(".");
 		if (qualifierIndex != -1) {
 			pids.put(SERVICE_FACTORYPID, filename.substring(0, qualifierIndex));
 			if (extensionIndex != -1) {
 				String pid = filename.substring(qualifierIndex + 1,
 						extensionIndex);
 				if (!pid.isEmpty()) {
 					pids.put(SERVICE_PID, filename.substring(
 							qualifierIndex + 1, extensionIndex));
 				}
 			} else {
 				pids.put(SERVICE_PID, filename.substring(qualifierIndex));
 			}
 		} else {
 			if (extensionIndex == -1) {
 				pids.put(SERVICE_PID, filename);
 			} else {
 				pids.put(SERVICE_PID, filename.substring(0, extensionIndex));
 			}
 		}
 		return pids;
 	}
 
 	protected Configuration findConfiguration(String pid) throws IOException {
 		// As ConfigurationAdmin.getConfiguration creates the configuration if
 		// it is not yet there, we check its existence first
 		try {
 			Configuration[] configurations = getConfigurationAdminService()
 					.listConfigurations("(service.pid=" + pid + ")");
 			if (configurations != null && configurations.length > 0) {
 				return configurations[0];
 			}
 		} catch (InvalidSyntaxException e) {
 		}
 
 		return null;
 	}
 
 	protected Configuration findFactoryConfiguration(String factoryPid,
 			String pid) throws IOException {
 		// As ConfigurationAdmin.getConfiguration creates the configuration if
 		// it is not yet there, we check its existence first
 		try {
 			String filter = "";
 			if (pid != null && !pid.isEmpty()) {
 				filter = "(&(service.factoryPid=" + factoryPid
 						+ ") (service.pid=" + pid + "))";
 			} else {
 				filter = "(service.factoryPid=" + factoryPid + ")";
 			}
 
 			Configuration[] configurations = getConfigurationAdminService()
 					.listConfigurations(filter);
 			if (configurations != null && configurations.length > 0) {
 				return configurations[0];
 			}
 		} catch (InvalidSyntaxException e) {
 		}
 
 		return null;
 	}
 
 	/**
 	 * Returns the configuration for the given externalPid or <code>null</code>
 	 * if no configuration could be found.
 	 * 
 	 * @param factoryPid
 	 * @param externalPid
 	 * @return
 	 * @throws IOException
 	 */
 	protected Configuration findFactoryConfigurationForExternalPid(
 			String factoryPid, String externalPid) throws IOException {
 		try {
 			String filter = "";
 			if (externalPid != null && !externalPid.isEmpty()) {
 				filter = "(&(service.factoryPid=" + factoryPid
 						+ ") (lunifera.externalPid=" + externalPid + "))";
 			} else {
 				filter = "(service.factoryPid=" + factoryPid + ")";
 			}
 
 			Configuration[] configurations = getConfigurationAdminService()
 					.listConfigurations(filter);
 			if (configurations != null && configurations.length > 0) {
 				return configurations[0];
 			}
 		} catch (InvalidSyntaxException e) {
 		}
 
 		return null;
 	}
 
 	@Override
 	public void displayFactoryConfiguration(String factoryPid) {
 		// As ConfigurationAdmin.getConfiguration creates the configuration if
 		// it is not yet there, we check its existence first
 		try {
 			Configuration[] configurations = getConfigurationAdminService()
 					.listConfigurations(
 							"(service.factoryPid=" + factoryPid + ")");
 			if (configurations != null && configurations.length > 0) {
 				for (int i = 0; i < configurations.length; i++) {
 					System.out.println("Factory PID= '" + factoryPid
 							+ "', PID = '" + configurations[i].getPid()
 							+ "', Location= '"
 							+ configurations[i].getBundleLocation() + "'");
 					System.out.println("Properties:"
 							+ configurations[i].getProperties().toString());
 				}
 			}
 		} catch (InvalidSyntaxException e) {
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected ConfigurationAdmin getConfigurationAdminService() {
 		return configurationAdmin;
 	}
 
 	@Override
 	public Dictionary<String, Object> getFactoryProperties(String factoryPid,
 			String pid) {
 		try {
 			Dictionary<String, Object> allProperties = new Hashtable<String, Object>();
 			String filter = "";
 			if (pid != null && !pid.isEmpty()) {
 				filter = "(&(service.factoryPid=" + factoryPid
 						+ ") (service.pid=" + pid + "))";
 			} else {
 				filter = "(service.factoryPid=" + factoryPid + ")";
 			}
 
 			Configuration[] configurations = getConfigurationAdminService()
 					.listConfigurations(filter);
 			if (configurations != null && configurations.length > 0) {
 				for (Configuration configuration : configurations) {
 					Enumeration<String> keys = configuration.getProperties()
 							.keys();
 					while (keys.hasMoreElements()) {
 						String object = keys.nextElement();
 						String value = (String) configuration.getProperties()
 								.get(object);
 						allProperties.put(object, value);
 					}
 				}
 				return allProperties;
 			}
 		} catch (InvalidSyntaxException e) {
 			getLogService().log(LogService.LOG_ERROR,
 					"Error on setup Configuration Service", e);
 		} catch (IOException e) {
 			getLogService().log(LogService.LOG_ERROR,
 					"Error on setup Configuration Service", e);
 		}
 
 		return null;
 	}
 
 	@Override
 	public String getFactoryProperty(String factoryPid, String pid,
 			String propertyName) {
 		Configuration configuration;
 		try {
 			configuration = findFactoryConfiguration(factoryPid, pid);
 			if (configuration != null)
 				return (String) configuration.getProperties().get(propertyName);
 			else
 				getLogService()
 						.log(LogService.LOG_WARNING,
 								"no configuration for FactoryPID: '"
 										+ factoryPid
 										+ "' and PID: '"
 										+ pid
 										+ "' (use 'initializeFactoryConfigurationStore' to create one)");
 		} catch (IOException e) {
 			getLogService().log(LogService.LOG_ERROR,
 					"Error on setup Configuration Service", e);
 		}
 		return null;
 	}
 
 	protected LogService getLogService() {
 		return logService;
 	}
 
 	public Dictionary<String, Object> getProperties(String pid) {
 		try {
 			Dictionary<String, Object> allProperties = new Hashtable<String, Object>();
 			Configuration[] configurations = getConfigurationAdminService()
 					.listConfigurations("(service.pid=" + pid + ")");
 			if (configurations != null && configurations.length > 0) {
 				for (Configuration configuration : configurations) {
 					Enumeration<String> keys = configuration.getProperties()
 							.keys();
 					while (keys.hasMoreElements()) {
 						String object = keys.nextElement();
 						String value = (String) configuration.getProperties()
 								.get(object);
 						allProperties.put(object, value);
 					}
 				}
 				return allProperties;
 			}
 		} catch (InvalidSyntaxException e) {
 			getLogService().log(LogService.LOG_ERROR,
 					"Error on setup Configuration Service", e);
 		} catch (IOException e) {
 			getLogService().log(LogService.LOG_ERROR,
 					"Error on setup Configuration Service", e);
 		}
 
 		return null;
 	}
 
 	public String getProperty(String pid, String propertyName) {
 		Configuration configuration;
 		try {
 			configuration = findConfiguration(pid);
 			if (configuration != null)
 				return (String) configuration.getProperties().get(propertyName);
 			else
 				getLogService()
 						.log(LogService.LOG_WARNING,
 								"no configuration for pid '"
 										+ pid
 										+ "' (use 'initializeFactoryConfigurationStore' to create one)");
 		} catch (IOException e) {
 			getLogService().log(LogService.LOG_ERROR,
 					"Error on setup Configuration Service", e);
 		}
 		return null;
 	}
 
 	@Override
 	public void initializeConfigurationStore(String pid,
 			Dictionary<String, Object> properties) {
 		Configuration configuration;
 
 		try {
 
 			if (configurationAdmin == null)
 				throw new RuntimeException(
 						"Configuration Admin Manager was not wired !!!");
 
 			configuration = configurationAdmin.getConfiguration(pid, null);
 			// Ensure update is called, when properties are null; otherwise
 			// configuration will not
 			// be returned when listConfigurations is called (see specification
 			// 104.15.3.7)
 			if (configuration.getProperties() == null) {
 				if (properties == null) {
 					properties = new Hashtable<String, Object>();
 				}
 				configuration.update(properties);
 				getLogService().log(
 						LogService.LOG_DEBUG,
 						"Initialized store under PID: '" + pid
 								+ "', with this properties: "
 								+ properties.toString());
 			} else {
 				getLogService().log(
 						LogService.LOG_WARNING,
 						"Configuration for PID was already initialized: ' "
 								+ pid + "'.");
 			}
 		} catch (IOException e) {
 			getLogService().log(LogService.LOG_ERROR,
 					"Error on setup Configuration Service", e);
 		}
 	}
 
 	@Override
 	public String initializeFactoryConfigurationStore(String factoryPid,
 			String externalPid, Dictionary<String, Object> properties) {
 		Configuration configuration;
 
 		String pid = null;
 		try {
 
 			if (configurationAdmin == null)
 				throw new RuntimeException(
 						"Configuration Admin Manager was not wired !!!");
 
 			// try to find an existing configuration
 			configuration = findFactoryConfigurationForExternalPid(factoryPid,
 					externalPid);
 			if (configuration == null) {
 				// create a new configuration
 				configuration = configurationAdmin.createFactoryConfiguration(
 						factoryPid, null);
 			}
 			pid = configuration.getPid();
 
 			if (properties == null) {
 				properties = new Hashtable<String, Object>();
 			}
 
 			// add the externalPid as a property
 			if (externalPid != null && !externalPid.isEmpty())
 				properties.put(EXTERNAL_PID, externalPid);
 
 			configuration.update(properties);
 
 			// / just for test
 			// displayFactoryConfiguration(factoryPid);
 
 			getLogService().log(
 					LogService.LOG_DEBUG,
 					"Initialized store under FactoryPID: '" + factoryPid
 							+ "' and PID: '" + configuration.getPid()
 							+ "' , with this properties: "
 							+ properties.toString());
 
 		} catch (IOException e) {
 			getLogService().log(LogService.LOG_ERROR,
 					"Error on setup Configuration Service", e);
 		}
 		return pid;
 	}
 
 	@Override
 	public void putFactoryProperties(String factoryPid, String pid,
 			Dictionary<String, Object> properties) {
 		Configuration config;
 		try {
 			config = findFactoryConfiguration(factoryPid, pid);
 			if (config == null) {
 				getLogService()
 						.log(LogService.LOG_ERROR,
 								"no configuration for FactoryPID: '"
 										+ factoryPid
 										+ "' and PID: '"
 										+ pid
 										+ "' (use 'initializeFactoryConfigurationStore' to create one)");
 				return;
 			}
 
 			if (properties != null) {
 				properties.put(SERVICE_FACTORYPID, factoryPid);
 				properties.put(SERVICE_PID, pid);
 				config.update(properties);
 			}
 		} catch (IOException e) {
 			getLogService().log(LogService.LOG_ERROR,
 					"Error on setup Configuration Service", e);
 		}
 	}
 
 	@Override
 	public void putFactoryProperty(String factoryPid, String pid,
 			String propertyName, Object value) {
 		Configuration config;
 		try {
 			config = findFactoryConfiguration(factoryPid, pid);
 			if (config == null) {
 				getLogService()
 						.log(LogService.LOG_ERROR,
 								"no configuration for for FactoryPID: '"
 										+ factoryPid
 										+ "' and PID: '"
 										+ pid
 										+ "' (use 'initializeFactoryConfigurationStore' to create one)");
 				return;
 			}
 			if (value != null) {
 				Dictionary<String, Object> properties = config.getProperties();
 				if (properties == null)
 					properties = new Hashtable<String, Object>();
 
 				properties.put(propertyName, value);
 				properties.put(SERVICE_FACTORYPID, factoryPid);
 				properties.put(SERVICE_PID, pid);
 				config.update(properties);
 			}
 		} catch (IOException e) {
 			getLogService()
 					.log(LogService.LOG_ERROR,
 							"no configuration for pid '"
 									+ pid
 									+ "' (use 'initializeFactoryConfigurationStore' to create one)");
 
 		}
 	}
 
 	@Override
 	public void putProperties(String pid, Dictionary<String, Object> properties) {
 		Configuration config;
 		try {
 			config = findConfiguration(pid);
 			if (config == null) {
 				getLogService()
 						.log(LogService.LOG_ERROR,
 								"no configuration for pid '"
 										+ pid
 										+ "' (use 'initializeConfigurationStore' to create one)");
 				return;
 			}
 
 			if (properties != null) {
 
 				config.update(properties);
 			}
 		} catch (IOException e) {
 			getLogService().log(LogService.LOG_ERROR,
 					"Error on setup Configuration Service", e);
 		}
 	}
 
 	public void putProperty(String pid, String propertyName, Object value) {
 		Configuration config;
 		try {
 			config = findConfiguration(pid);
 			if (config == null) {
 				getLogService()
 						.log(LogService.LOG_ERROR,
 								"no configuration for pid '"
 										+ pid
 										+ "' (use 'initializeConfigurationStore' to create one)");
 				return;
 			}
 			if (value != null) {
 				Dictionary<String, Object> properties = config.getProperties();
 				if (properties == null)
 					properties = new Hashtable<String, Object>();
 
 				properties.put(propertyName, value);
 				config.update(properties);
 			}
 		} catch (IOException e) {
 			getLogService()
 					.log(LogService.LOG_ERROR,
 							"no configuration for pid '"
 									+ pid
 									+ "' (use 'initializeConfigurationStore' to create one)");
 
 		}
 
 	}
 
 	protected List<String> scan(Bundle bundle, String basedir,
 			List<String> includes, List<String> excludes) {
 
 		List<String> scannedItens = new ArrayList<String>();
 		BundleWiring wiring = bundle.adapt(BundleWiring.class);
 
 		if (includes != null) {
 			for (String filePattern : includes) {
 				Collection<String> foundIncludedFiles = wiring.listResources(
 						basedir, filePattern,
 						BundleWiring.LISTRESOURCES_RECURSE);
 				if (foundIncludedFiles != null && !foundIncludedFiles.isEmpty()) {
 					scannedItens.addAll(foundIncludedFiles);
 				}
 			}
 		}
 
 		if (excludes != null) {
 			for (String filePattern : excludes) {
 				Collection<String> foundExcludeFiles = wiring.listResources(
 						basedir, filePattern,
 						BundleWiring.LISTRESOURCES_RECURSE);
 				if (foundExcludeFiles != null && !foundExcludeFiles.isEmpty()) {
 					scannedItens.removeAll(foundExcludeFiles);
 				}
 			}
 		}
 
 		return scannedItens;
 	}
 
 	protected void unbindConfigurationAdmin(
 			ConfigurationAdmin configurationAdmin) {
 		if (this.configurationAdmin == configurationAdmin)
 			this.configurationAdmin = null;
 		getLogService().log(LogService.LOG_DEBUG,
 				"Unbinded ConfigurationAdminService");
 	}
 
 	protected void unbindLogService(LogService logService) {
 		if (this.logService == logService) {
 			getLogService().log(LogService.LOG_DEBUG, "Unbinded LogService.");
 			this.logService = null;
 		}
 	}
 }
