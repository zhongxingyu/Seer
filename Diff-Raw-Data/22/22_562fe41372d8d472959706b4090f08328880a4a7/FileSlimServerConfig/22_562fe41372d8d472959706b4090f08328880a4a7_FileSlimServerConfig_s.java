 /**
  * 
  */
 package uk.co.unclealex.music.core;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.stereotype.Service;
 
 /**
  * @author alex
  *
  */
 @Service("slimServerConfig")
 public class FileSlimServerConfig implements SlimServerConfig {
 
 	private List<String> i_definiteArticles = new LinkedList<String>();
 	private boolean i_initialised = false;
	private File i_configFile;
 	
 	/**
 	 * Read a slimserver configuration file. Currently, only single line properties are supported. 
 	 * @param in
 	 * @throws IOException
 	 */
 	public void initialise() throws IOException {
 		if (i_initialised) {
 			throw new IllegalStateException("This configuration has already been initialised.");
 		}
 		i_initialised = true;
		FileReader in = new FileReader(getConfigFile());
 		Map<String, String> properties = new HashMap<String, String>();
 		BufferedReader bIn = new BufferedReader(in);
 		String line;
 		while ((line = bIn.readLine()) != null) {
 			int delimiterIndex = line.indexOf(':');
 			if (
 					delimiterIndex > 0 && delimiterIndex != line.length() - 1 && 
 					!Character.isWhitespace(line.charAt(0))) {
 				String key = line.substring(0, delimiterIndex).trim();
 				String value = line.substring(delimiterIndex + 1).trim();
 				if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
 					value = value.substring(1, value.length() - 1);
 				}
 				properties.put(key.toLowerCase(), value);
 			}
 		}
 		bIn.close();
 		
 		i_definiteArticles = Arrays.asList(StringUtils.splitByWholeSeparator(properties.get("ignoredarticles"), null));
 	}
 	
 	public List<String> getDefiniteArticles() {
 		return i_definiteArticles;
 	}
 	/**
 	 * @return the initialised
 	 */
 	public boolean isInitialised() {
 		return i_initialised;
 	}
 
	@Required
	public File getConfigFile() {
		return i_configFile;
 	}
 
	public void setConfigFile(File configFile) {
		i_configFile = configFile;
 	}
 	
 	
 }
