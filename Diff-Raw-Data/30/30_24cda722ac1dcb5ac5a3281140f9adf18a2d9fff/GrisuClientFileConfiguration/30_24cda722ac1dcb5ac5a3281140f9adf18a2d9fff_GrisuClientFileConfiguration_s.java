 package org.vpac.grisu.client.gricli;
 
 import org.apache.commons.configuration.HierarchicalINIConfiguration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.vpac.grisu.control.Environment;
 
 import java.io.File;
 import java.util.HashMap;
 
 /**
    The default options for grisu command line client can be specified in INI configuration file
  */
 public class GrisuClientFileConfiguration {
 
 	public final static String CONFIG_FILE_PATH = Environment.GRISU_DIRECTORY+ File.separator + "gricli.config";
 	private static HashMap<String,GrisuClientFileConfiguration> instances = new HashMap<String,GrisuClientFileConfiguration>();
 
 	private HierarchicalINIConfiguration configuration  = null;
 
 	public static GrisuClientFileConfiguration getConfiguration(String path) throws ConfigurationException{
 		path = (path == null)?CONFIG_FILE_PATH:path;
 		GrisuClientFileConfiguration instance = instances.get(path);
 		if (instance == null) {
 			instance = new GrisuClientFileConfiguration(path);
 			instances.put(path,instance);
 			return instance;
 		}
 		else {
 			return instance;
 		}
 	}
 
 	private GrisuClientFileConfiguration(String path) throws ConfigurationException{
 		this.configuration = new HierarchicalINIConfiguration(path);
 	}
 
 	public String getCommonOption(String key) throws ConfigurationException{
 		try {
 			return (String)configuration.getProperty("common." + key);
 		}
 		catch (NullPointerException ex){
 			return null;
 		}
 	}
 
 	public String getJobOption(String key) throws ConfigurationException{
 		try {
 			return (String)configuration.getProperty("job." + key);			
 		} catch (NullPointerException e) {
 			return null;
 		}
 	}
 }
