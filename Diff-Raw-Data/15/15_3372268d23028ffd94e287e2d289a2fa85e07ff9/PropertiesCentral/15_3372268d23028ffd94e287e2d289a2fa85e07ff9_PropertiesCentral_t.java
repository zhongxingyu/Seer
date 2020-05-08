 package com.operativus.senacrs.audit.properties;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
 import java.util.Properties;
 
 import com.operativus.senacrs.audit.common.AbstractHasLogger;
 import com.operativus.senacrs.audit.exceptions.RuntimeExceptionFactory;
 
 public class PropertiesCentral
 		extends AbstractHasLogger {
 
 	private static final String READ_PROPERTIES_FILE = "Start reading properties file ";
 	private static final String NULL_ARGUMENT_KEY = "Null argument [key]";
 	private static final String NULL_KEY_VALUE = "Key is valid, but its value is null";
 
 	private final Properties properties;
 
 	public PropertiesCentral() {
 
 		super();
 		this.properties = new Properties();
 	}
 
 	public void addPropertiesFile(final String filename) throws IOException {
 
 		File file = null;
 		InputStream in = null;
 
 		if (filename == null) {
 			throw RuntimeExceptionFactory.getInstance().getNullArgumentException("filename");
 		}
 		file = new File(filename);
 		checkFile(file);
 		in = new FileInputStream(file);
 		this.getLogger().info(READ_PROPERTIES_FILE + filename);
 		this.addPropertiesFile(in);
 	}
 
 	private void checkFile(File file) {
 		
 		StringBuilder msg = null;
 
 		msg = new StringBuilder();
 		if (!file.exists()) {
 			msg.append("Informed filename does not exist. ");						
 		}
 		if (!file.isFile()) {
 			msg.append("Informed filename is not a regular file. ");						
 		}
 		if (!file.canRead()) {
 			msg.append("Informed filename cannot be read. ");						
 		}
 		if (msg.length() > 0) {
 			msg.append("File: " + file.getAbsolutePath());
 			throw new IllegalArgumentException(msg.toString());
 		}
 	}
 
 	public void addPropertiesFile(final InputStream in) throws IOException {
 
 		if (in == null) {
 			throw RuntimeExceptionFactory.getInstance().getNullArgumentException("in");
 		}
 		this.properties.load(in);
 	}
 
 	public String getMessage(final PropertyKey key, final Object... arguments) {
 
 		String result = null;
 		String keyValue = null;
 		String tmpl = null;
 
 		keyValue = getKeyValue(key);
 		if (!this.hasKey(key)) {
 			throw new IllegalArgumentException("Informed key [" + keyValue + "] does not exists");
 		}
 		tmpl = this.properties.getProperty(keyValue);
 		result = MessageFormat.format(tmpl, arguments);
 
 		return result;
 	}
 
 	private static String getKeyValue(final PropertyKey key) {
 
 		String result;
 
 		if (key == null) {
 			throw new IllegalArgumentException(NULL_ARGUMENT_KEY);
 		}
 		result = key.getKey();
 		if (result == null) {
 			throw new IllegalArgumentException(NULL_KEY_VALUE);
 		}
 
 		return result;
 	}
 
 	public boolean hasKey(final PropertyKey key) {
 
 		boolean result = false;
 		String keyStr = null;
 
 		keyStr = getKeyValue(key);
 		result = this.properties.containsKey(keyStr);
 
 		return result;
 	}
	
	public List<String> getAvailableKeys() {
		
		List<String> result = null;
		
		result = new LinkedList<String>();
		for (Object k : this.properties.keySet()) {
			result.add((String) k);			
		}
				
		return result;
	}
 }
