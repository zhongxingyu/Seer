 package ch.cern.atlas.apvs.server;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ServerStorage {
 	private static final String APVS_SERVER_SETTINGS_FILE = "APVS.properties";
 	private static final String comment = "APVS Server Settings";
 	private static Logger log = LoggerFactory.getLogger(ServerStorage.class
 			.getName());
 
 	private static ServerStorage instance;
 	private Properties properties = new Properties();
	private boolean readOnly = false;
 
 	public ServerStorage() throws FileNotFoundException, IOException {
 		try {
 			properties.load(new FileReader(APVS_SERVER_SETTINGS_FILE));
 		} catch (FileNotFoundException e) {
 			log.info("File " + APVS_SERVER_SETTINGS_FILE
 					+ " not found, created one.");
 		}
		try {
			properties.store(new FileWriter(APVS_SERVER_SETTINGS_FILE), comment);
		} catch (IOException e) {
			readOnly = true;
			log.warn("Cannot store Server Settings: "+e.getMessage());
		}
 	}
 
 	public static ServerStorage getLocalStorageIfSupported() {
 		try {
 			if (instance == null) {
 				instance = new ServerStorage();
 			}
 			return instance;
 		} catch (IOException e) {
 			log.warn("Server Settings Storage problem", e);
 		}
 		return null;
 	}
 
 	public String getItem(String name) {
 		return properties.getProperty(name);
 	}
 
 	public void setItem(String name, String value) {
 		properties.setProperty(name, value);
		if (readOnly) {
			return;
		}
		
 		try {
 			properties
 					.store(new FileWriter(APVS_SERVER_SETTINGS_FILE), comment);
 		} catch (IOException e) {
 			log.warn("Server Settings Storage write problem", e);
 		}
 	}
 
 	public int getLength() {
 		return properties.size();
 	}
 
 }
