 package com.photon.phresco.framework.param.impl;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.List;
 import java.util.Map;
 
 import com.photon.phresco.api.ConfigManager;
 import com.photon.phresco.api.DynamicParameter;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.configuration.Configuration;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.impl.ConfigManagerImpl;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.util.PomProcessor;
 
 public class DynamicFetchSqlImpl implements DynamicParameter, Constants {
 
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	public PossibleValues getValues(Map<String, Object> map) throws PhrescoException, ConfigurationException {
 		PossibleValues possibleValues = new PossibleValues();
 		try {
 			String dbVersion = "";
 			String sqlFileName = "";
 			String path = "";
 			ApplicationInfo applicationInfo = (ApplicationInfo) map.get(KEY_APP_INFO);
 			String sqlFilePath = getSqlFilePath(applicationInfo);
 			String appDirectory = applicationInfo.getAppDirName();
 	    	String configPath = getConfigurationPath(appDirectory).toString();
 	    	String envName = (String) map.get(KEY_ENVIRONMENT);
 	    	String dbname = (String) map.get(KEY_DATABASE);
 	    	ConfigManager configManager = new ConfigManagerImpl(new File(configPath));
 			List<Configuration> configurations = configManager.getConfigurations(envName, SETTINGS_TEMPLATE_DB);
 			for (Configuration configuration : configurations) {
				String dbType = configuration.getProperties().getProperty(DB_TYPE).toLowerCase();
 				if (dbname.equals(dbType)) { 
 					dbVersion =configuration.getProperties().getProperty(DB_VERSION);
 					File[] dbSqlFiles = new File(Utility.getProjectHome() + applicationInfo.getAppDirName() + sqlFilePath + dbname
 							+ File.separator + dbVersion).listFiles(new DumpFileNameFilter());
 					for (int i = 0; i < dbSqlFiles.length; i++) {
 						if (!dbSqlFiles[i].isDirectory()) {
 						Value value = new Value();
 						sqlFileName = dbSqlFiles[i].getName();
 						path = sqlFilePath + dbname + "/" +  dbVersion + "#SEP#" +  sqlFileName ;
 						value.setValue(path);
 			    		possibleValues.getValue().add(value);
 					}
 				  }
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return possibleValues;
 	}
 	
 	class DumpFileNameFilter implements FilenameFilter {
 
 		public boolean accept(File dir, String name) {
 			return !(name.startsWith("."));
 		}
 	}
 	
 	private StringBuilder getConfigurationPath(String projectDirectory) {
 		 StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 		 builder.append(projectDirectory);
 		 builder.append(File.separator);
 		 builder.append(DOT_PHRESCO_FOLDER);
 		 builder.append(File.separator);
 		 builder.append(CONFIGURATION_INFO_FILE);
 		 
 		 return builder;
 	 }
 	
 	private String getSqlFilePath(ApplicationInfo applicationInfo) throws PhrescoPomException {
 		PomProcessor pomPath = new PomProcessor(new File(Utility.getProjectHome() + applicationInfo.getAppDirName() + File.separator + "pom.xml"));
 		String sqlFilePath = pomPath.getProperty(POM_PROP_KEY_SQL_FILE_DIR);
 
 		return sqlFilePath;
 	}
 }
