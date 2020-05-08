 package it.uniroma3.dia.alfred.mpi;
 
 import it.uniroma3.dia.alfred.mpi.model.ConfigHolder;
 import it.uniroma3.dia.alfred.mpi.model.DomainHolder;
 import it.uniroma3.dia.alfred.mpi.model.constants.ConfigHolderKeys;
 import it.uniroma3.dia.alfred.mpi.model.constants.DomainHolderKeys;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 public class ConfiguratorParser {
 	// static class
 	private ConfiguratorParser() {}
 	
 	// File that contains the all the configurations
 	private final static String MAIN_CONF_FILE = "configurations.properties";
 
 	// File that contains all the description about domains
 	private final static String MAIN_DOMAIN_FILE = "domains.properties";
 
 	// Delimiter used in the properties file
 	private final static String DELIMITER = "=";
 	private final static String SECOND_DELIMITER = ",";
 	private final static String COMMENT = "//";
 
 	/**
 	 * This map will have for each domain (represented by a String)
 	 * a class {@link DomainHolder} containing all the parameters
 	 */
 	private static Map<String, DomainHolder> name2domain;
 
 	/**
 	 * Get a long config file and split it in many ConfigHolder
 	 * @return
 	 */
 	public static List<ConfigHolder> readConfig() {
 		return readConfig(null, null);
 	}
 	
 	/**
 	 * 
 	 * @param filePathConfigurations
 	 * @param filePathDomains
 	 * @return
 	 */
 	public static List<ConfigHolder> readConfig(String filePathConfigurations, String filePathDomains) {
 	
		if (readDomains(filePathDomains) == false){
			return null;
		}
 		
 		BufferedReader reader = null;
 		
 		if (filePathConfigurations == null){
 			reader = new BufferedReader(new InputStreamReader(ConfiguratorParser.class.getResourceAsStream(MAIN_CONF_FILE)));
 		} else {
 			try {
 				reader = new BufferedReader(new FileReader(filePathConfigurations));
 			} catch (FileNotFoundException e) {
 				System.err.println("Error in opening " + filePathConfigurations);
 				e.printStackTrace();
				return null;
 			}
 		}
 		
 		LinkedList<ConfigHolder> configList = new LinkedList<ConfigHolder>();
 		
 		String currentLine = null;
 		ConfigHolder currentConfigHolder = null;
 		Map<String, String> key2value = new HashMap<String, String>();
 		String key = null;	
 		String value = null;
 		
 		// We're parsing this
 		String currentConfigurationName = null;
 		
 		try {
 			while( (currentLine = reader.readLine()) != null) {
 				
 				// if the line is empty
 				if (currentLine.matches("^\\s*$")) {
 					continue;
 				}
 				
 				// if the line is a comment
 				if (currentLine.matches("^//.*")) {
 					continue;
 				}
 				
 				// if contains a new definition
 				if (currentLine.matches("^\\[.*\\]$")) {
 					currentConfigurationName = currentLine.substring(1, currentLine.length()-1);
 					continue;
 				}
 				
 				key = currentLine.split(DELIMITER)[0];
 				
 				if (currentLine.matches(".*"+DELIMITER+".*"+DELIMITER+".*")) {
 					value = currentLine.substring(key.length()+1);
 				} else {
 					value = currentLine.split(DELIMITER)[1];
 				}
 				
 				if (key.equals(ConfigHolderKeys.DOMAINS_KEY)) {
 					for (String domainName : value.split(SECOND_DELIMITER)){
 						currentConfigHolder = new ConfigHolder();
 						currentConfigHolder.setUid(ConfigHolderIdBroker.getId(currentConfigurationName, domainName));
 						
 						// Put config inside configuration holder
 						for(String keyMap: key2value.keySet()) {
 							currentConfigHolder.setConfigurationValue(keyMap, key2value.get(keyMap));
 						}
 						
 						// Put domain configuration
 						currentConfigHolder.setAssociatedDomain(name2domain.get(domainName));
 						
 						configList.add(currentConfigHolder);
 						
 					}
 					key2value.clear();
 					continue;
 				}
 				
 				key2value.put(key, value);
 				
 			}
 		} catch (IOException e) {
 			System.err.println("Error in reading configuratios file");
 			e.printStackTrace();
			return null;
 		}
 		
 		// ConfiguratorParser.class.getResourceAsStream(MAIN_CONF_FILE).close();
 		
 		if (filePathConfigurations != null) {
 			try {
 				reader.close();
 			} catch (IOException e) {
 				System.err.println("Error in closing properties file");
 				e.printStackTrace();
				return null;
 			}			
 		}
 		
 		return configList;
 		
 	}
 	
 	/**
 	 * 
 	 * @param filePathDomains
 	 */
	private static boolean readDomains(String filePathDomains){
 		
 		name2domain = new HashMap<String, DomainHolder>();
 		
 		BufferedReader reader = null;
 		
 		// open the file
 		if (filePathDomains == null){
 			reader = new BufferedReader(new InputStreamReader(ConfiguratorParser.class.getResourceAsStream(MAIN_DOMAIN_FILE)));
 		} else {
 			try {
 				reader = new BufferedReader(new FileReader(filePathDomains));
 			} catch (FileNotFoundException e) {
 				System.err.println("Error in opening " + filePathDomains);
 				e.printStackTrace();
				return false;
 			}
 		}
 		
 		String currentLine = null;
 		DomainHolder currentDomainHolder = null;
 		
 		String key = null, value = null;
 		
 		try {
 			while( (currentLine = reader.readLine()) != null) {
 				
 				// if the line is empty
 				if (currentLine.matches("\\s*")) {
 					continue;
 				}
 				
 				// if contains a new definition
 				if (currentLine.matches("\\[.*\\]")) {
 					currentDomainHolder = new DomainHolder();
 					continue;
 				}
 				
 				key = currentLine.split(DELIMITER)[0];
 				
 				if (currentLine.matches(".*"+DELIMITER+".*"+DELIMITER+".*")) {
 					value = currentLine.substring(key.length()+1);
 				} else {
 					value = currentLine.split(DELIMITER)[1];
 				}
 
 				if (key.equalsIgnoreCase(DomainHolderKeys.DOMAIN_ID_KEY)){
 					currentDomainHolder.setConfigurationValue(key, value);
 					name2domain.put(value, currentDomainHolder);
 					continue;
 				}
 				
 				if (key.equalsIgnoreCase(DomainHolderKeys.FIRST_PAGE_KEY) ||
 						key.equalsIgnoreCase(DomainHolderKeys.BUCKET_S3_KEY) ||
 						key.equalsIgnoreCase(DomainHolderKeys.SITE_KEY) ){
 					currentDomainHolder.setConfigurationValue(key, value);
 					continue;
 				}
 				
 				currentDomainHolder.setGoldenXPath(key,value);
 				
 			}
 		} catch (IOException e) {
 			System.err.println("Error in reading domains file");
 			e.printStackTrace();
			return false;
 		}
 		
 		if (filePathDomains != null) {
 			try {
 				reader.close();
 			} catch (IOException e) {
 				System.err.println("Error in closing domains file");
 				e.printStackTrace();
				return false;
 			}
 		}
 		
		return true;
		
 	}
 	
 }
