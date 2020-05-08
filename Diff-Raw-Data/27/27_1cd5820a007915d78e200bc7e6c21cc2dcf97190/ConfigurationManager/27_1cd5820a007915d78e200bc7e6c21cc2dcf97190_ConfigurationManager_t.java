 package org.cagrid.grape;
 
 import gov.nih.nci.cagrid.common.Utils;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.namespace.QName;
 
 import org.apache.log4j.Logger;
 import org.cagrid.grape.configuration.Grid;
 import org.cagrid.grape.configuration.TargetGridsConfiguration;
 import org.cagrid.grape.model.Configuration;
 import org.cagrid.grape.model.ConfigurationDescriptor;
 import org.cagrid.grape.model.ConfigurationDescriptors;
 import org.cagrid.grape.model.ConfigurationGroup;
 import org.cagrid.grape.model.ConfigurationGroups;
 
 /**
  * @author <A href="mailto:langella@bmi.osu.edu">Stephen Langella </A>
  * @author <A href="mailto:oster@bmi.osu.edu">Scott Oster </A>
  * @author <A href="mailto:hastings@bmi.osu.edu">Shannon Hastings </A>
  * @created Oct 14, 2004
  */
 public class ConfigurationManager {
 
 	private static final String DEFAULT = "default";
 
	private static final String DEFAULT_CONFIGURATION_DIR = Utils.getCaGridUserHome()
			.getAbsolutePath() + File.separator + "grape";
 
 	private static Logger log = Logger.getLogger(ConfigurationManager.class);
 
 	private ConfigurationSynchronizer synchronizer;
 	
 	private String configurationDirectory = DEFAULT_CONFIGURATION_DIR;
 	
 	private Map<String, ConfigurationObjects> configurations = null;
 	
 	private String activeConfigurationName = DEFAULT;
 		
 	public ConfigurationManager() {
 		
 	}
 
 	public ConfigurationManager(Configuration configuration,
 			ConfigurationSynchronizer synchronizer, String configurationDirectory,
 			Grid grid) throws Exception {
 
 		if (configurationDirectory != null) {
 			this.configurationDirectory = configurationDirectory;
 		}
 		
 		if (grid == null) {
 			grid = new Grid();
 			grid.setSystemName(DEFAULT);
 		}
 
 		Map<String, ConfigurationDescriptor> confsByName = new HashMap<String, ConfigurationDescriptor>();
 		Map<String, Object> objectsByName = new HashMap<String, Object>();
 		
 		this.synchronizer = synchronizer;
 
 		configurations = new HashMap<String, ConfigurationObjects>();
 		
 		ConfigurationObjects configurationObjects = new ConfigurationObjects(confsByName, objectsByName, configuration, grid);
 		configurations.put(grid.getSystemName(), configurationObjects);
 		
 		if (configuration != null) {
 			File f = new File(this.configurationDirectory);
 			f.mkdirs();
 			configurationObjects.processConfigurationGroups(configuration
 					.getConfigurationGroups());
 			configurationObjects.processConfigurationDescriptors(configuration
 					.getConfigurationDescriptors());
 		}
 		
 		activeConfigurationName = grid.getSystemName();
 		
 	}
 	
 	public ConfigurationManager(Configuration configuration,
 			ConfigurationSynchronizer synchronizer, String configurationDirectory) throws Exception {
 		this(configuration, synchronizer, configurationDirectory, null);
 	}
 	
 	public ConfigurationManager(Configuration configuration,
 			ConfigurationSynchronizer synchronizer) throws Exception {
 		this(configuration, synchronizer, null);
 	}
 	
 	public void addConfiguration(Configuration configuration, Grid grid) throws Exception {		
 		Map<String, ConfigurationDescriptor> confsByName = new HashMap<String, ConfigurationDescriptor>();
 		Map<String, Object> objectsByName = new HashMap<String, Object>();
 
 		ConfigurationObjects configurationObjects = new ConfigurationObjects(confsByName, objectsByName, configuration, grid);
 		configurationObjects.processConfigurationGroups(configuration
 				.getConfigurationGroups());
 		configurationObjects.processConfigurationDescriptors(configuration
 				.getConfigurationDescriptors());
 
 
 		configurations.put(grid.getSystemName(), configurationObjects);
 	}
 
 	public Configuration getConfiguration() {
 		ConfigurationObjects configurationObjects = configurations.get(DEFAULT);
 		return configurationObjects.getConfiguration();
 	}
 
 	public Configuration getConfiguration(String configurationName) {
 		ConfigurationObjects configurationObjects = configurations.get(configurationName);
 		return configurationObjects.getConfiguration();
 	}
 	
 	public Set<String> getConfigurationNames() {
 		return configurations.keySet();
 	}
 	
 	public Grid getConfigurationGrid(String configurationName) {
 		return configurations.get(configurationName).getGrid();
 	}
 
 	public ConfigurationDescriptor getConfigurationDescriptor(String systemName, String configurationName)
 			throws Exception {
 		ConfigurationObjects configurationObjects = configurations.get(configurationName);		
 		if (configurationObjects.getConfsByName().containsKey(systemName)) {
 			return configurationObjects.getConfsByName().get(systemName);
 		} else {
 			throw new Exception("The configuration " + systemName
 					+ " does not exist!!!");
 		}
 	}
 	
 	public ConfigurationDescriptor getConfigurationDescriptor(String systemName)
 	throws Exception {
 		return getConfigurationDescriptor(systemName, DEFAULT);
 	}
 
 	public Object getConfigurationObject(String systemName) throws Exception {
 		ConfigurationObjects configurationObjects = configurations.get(DEFAULT);		
 		if (configurationObjects.getObjectsByName().containsKey(systemName)) {
 			return configurationObjects.getObjectsByName().get(systemName);
 		} else {
 			throw new Exception("The configuration " + systemName
 					+ " does not exist!!!");
 		}
 	}
 
 	public Object getConfigurationObjectByConfiguration(String systemName, String configurationName) throws Exception {
 		ConfigurationObjects configurationObjects = configurations.get(configurationName);		
 		if (configurationObjects.getObjectsByName().containsKey(systemName)) {
 			return configurationObjects.getObjectsByName().get(systemName);
 		} else {
 			throw new Exception("The configuration " + systemName
 					+ " does not exist!!!");
 		}
 	}
 
 	public Object getActiveConfigurationObject(String systemName) throws Exception {
 		ConfigurationObjects configurationObjects = configurations.get(activeConfigurationName);		
 		if (configurationObjects.getObjectsByName().containsKey(systemName)) {
 			return configurationObjects.getObjectsByName().get(systemName);
 		} else {
 			throw new Exception("The configuration " + systemName
 					+ " does not exist!!!");
 		}
 	}
 	
 	public void saveAll() throws Exception {
 		for (ConfigurationObjects configurationObjects : configurations
 				.values()) {
 			Iterator itr = configurationObjects.getObjectsByName().keySet()
 					.iterator();
 			while (itr.hasNext()) {
 				save((String) itr.next(), false, configurationObjects.getConfigurationName());
 			}
 			if (synchronizer != null) {
 				synchronizer.syncronize();
 			}
 		}
 	}
 
 	public void save(String systemName, boolean sync, String configurationName) throws Exception {
 		try {
 			String tmpConfName = null;
 			tmpConfName = ("default".equals(configurationName)) ? "" : configurationName;
 			ConfigurationObjects configurationObjects = configurations.get(configurationName);		
 			ConfigurationDescriptor des = getConfigurationDescriptor(systemName, configurationName);
 			Object obj = configurationObjects.getObjectsByName().get(systemName);
 			File conf = new File(configurationDirectory + File.separator + tmpConfName + File.separator
 					+ des.getSystemName() + "-conf.xml");
 			QName ns = new QName(des.getQname().getNamespace(), des.getQname()
 					.getName());
 			Utils.serializeDocument(conf.getAbsolutePath(), obj, ns);
 
 			if ((sync) && (synchronizer != null)) {
 				synchronizer.syncronize();
 			}
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 			throw new Exception("Error saving the configuration " + systemName
 					+ ":\n" + e.getMessage());
 		}
 	}
 	
 	public void save(String systemName, boolean sync) throws Exception {
 		save(systemName, sync, "default");
 	}
 
 	public void reload() throws Exception {	
 		for (ConfigurationObjects configurationObjects : configurations
 				.values()) {
 			configurationObjects.reload();
 		}
 	}
 
 	public String getConfigurationDirectory() {
 		return configurationDirectory;
 	}
 
 	public void setConfigurationDirectory(String configurationDirectory) {
 		this.configurationDirectory = configurationDirectory;
 	}
 	
 	public void setActiveConfiguration(String configurationName) {
 		if (activeConfigurationName.equals(configurationName)) {
 			return;
 		}
 		activeConfigurationName = configurationName;
 		
 		ConfigurationObjects configurationObjects = configurations.get(DEFAULT);
 		Map<String, Object> objectsByName = configurationObjects.getObjectsByName();
 		TargetGridsConfiguration targetGridsConfiguration = (TargetGridsConfiguration) objectsByName.get("target-grid");
 				
 		if (targetGridsConfiguration.getActiveGrid().equals(configurationName)) {
 			return;
 		}
 
 		targetGridsConfiguration.setActiveGrid(configurationName);
 		try {
 			this.save("target-grid", false);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	
 	private class ConfigurationObjects {
 		private Map<String, ConfigurationDescriptor> confsByName = null;
 
 		private Map<String, Object> objectsByName = null;
 
 		private Configuration configuration = null;
 		
 		private Grid grid = null;
 
 		public ConfigurationObjects(
 				Map<String, ConfigurationDescriptor> confsByName,
 				Map<String, Object> objectsByName, Configuration configuration,
 				Grid grid) {
 			this.confsByName = confsByName;
 			this.objectsByName = objectsByName;
 			this.configuration = configuration;
 			this.grid = grid;
 		}
 		
 		public String getConfigurationName() {
 			return grid.getSystemName();
 		}
 		
 		public String getConfigurationDisplayName() {
 			return grid.getDisplayName();
 		}
 
 
 		public Map<String, ConfigurationDescriptor> getConfsByName() {
 			return confsByName;
 		}
 
 		public void setConfsByName(
 				Map<String, ConfigurationDescriptor> confsByName) {
 			this.confsByName = confsByName;
 		}
 
 		public Map<String, Object> getObjectsByName() {
 			return objectsByName;
 		}
 
 		public void setObjectsByName(Map<String, Object> objectsByName) {
 			this.objectsByName = objectsByName;
 		}
 
 		public Configuration getConfiguration() {
 			return configuration;
 		}
 
 		public void setConfiguration(Configuration configuration) {
 			this.configuration = configuration;
 		}
 
 		public Grid getGrid() {
 			return grid;
 		}
 		
 		private void processConfigurationDescriptors(
 				ConfigurationDescriptors list) throws Exception {
 			if (list != null) {
 				ConfigurationDescriptor[] des = list
 						.getConfigurationDescriptor();
 				if (des != null) {
 					for (int i = 0; i < des.length; i++) {
 						this.processConfigurationDescriptor(des[i]);
 					}
 
 				}
 			}
 		}
 
 		private void processConfigurationGroup(ConfigurationGroup des)
 				throws Exception {
 			if (des != null) {
 				processConfigurationDescriptors(des
 						.getConfigurationDescriptors());
 			}
 
 		}
 
 		private void processConfigurationDescriptor(
 				final ConfigurationDescriptor des) throws Exception {
 			if (confsByName.containsKey(des.getSystemName())) {
 				throw new Exception(
 						"Error configuring the application, more than one configuration was specified with the system name "
 								+ des.getSystemName() + "!!!");
 			} else {
 				String tmpConfName = (DEFAULT.equals(grid.getSystemName())) ? "" : grid.getSystemName();
 				Object obj = null;
 				File conf = new File(configurationDirectory + File.separator + tmpConfName + File.separator
 						+ des.getSystemName() + "-conf.xml");
 				if (!conf.exists()) {
					File template = new File(des.getDefaultFile());
					if (!template.exists()) {
						template = new File(configurationDirectory
 							+ File.separator  + tmpConfName + File.separator + des.getDefaultFile());
					}
 					if (!template.exists()) {
 						throw new Exception(
 								"Error configuring the application,the default file specified for the configuration "
 										+ des.getSystemName()
 										+ " does not exist!!!\n"
 										+ template.getAbsolutePath()
 										+ " not found!!!");
 					} else {
 						obj = Utils.deserializeDocument(template
 								.getAbsolutePath(), Class.forName(des
 								.getModelClassname()));
 						log.info("Loading configuration for "
 								+ des.getDisplayName() + " from "
 								+ template.getAbsolutePath());
 					}
 				} else {
 					obj = Utils.deserializeDocument(conf.getAbsolutePath(),
 							Class.forName(des.getModelClassname()));
 					log.info("Loading configuration for "
 							+ des.getDisplayName() + " from "
 							+ conf.getAbsolutePath());
 				}
 
 				confsByName.put(des.getSystemName(), des);
 				objectsByName.put(des.getSystemName(), obj);
 			}
 		}
 
 		private void processConfigurationGroups(ConfigurationGroups list)
 				throws Exception {
 			if (list != null) {
 				ConfigurationGroup[] group = list.getConfigurationGroup();
 				if (group != null) {
 					for (int i = 0; i < group.length; i++) {
 						this.processConfigurationGroup(group[i]);
 					}
 
 				}
 			}
 		}
 
 		public void reload() throws Exception {
 			confsByName.clear();
 			objectsByName.clear();
 			this.processConfigurationGroups(configuration
 					.getConfigurationGroups());
 			this.processConfigurationDescriptors(configuration
 					.getConfigurationDescriptors());
 
 		}
 		
 	}
 }
