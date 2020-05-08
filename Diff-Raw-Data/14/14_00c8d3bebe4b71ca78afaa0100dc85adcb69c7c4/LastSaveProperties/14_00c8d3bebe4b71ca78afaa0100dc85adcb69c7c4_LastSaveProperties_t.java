 package de.feu.showgo.io;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 import de.feu.showgo.ShowGoDAO;
 
 public class LastSaveProperties {
 
	private static final String FILE_NAME = ".showgo.properties";
	private static final String LAST_SAVE_FILE_ATTR_NAME = "lastSaveFile";
 	private static final Logger log = Logger.getLogger(LastSaveProperties.class);
 	
 	public static String getLastSaveFile(){
 		String userHome = System.getProperty("user.home");
		File f = new File(userHome, FILE_NAME);
 		log.debug("Loading Properties file " + f);
 		if(!f.exists()){
 			log.debug("properties file does not exists");
 			return null;
 		}
 		log.debug("properties file exists");
 		
 		Properties defaultProps = new Properties();
 		FileInputStream in;
 		
 		try {
 			in = new FileInputStream(f);
 			defaultProps.load(in);
			String lastSave = defaultProps.getProperty(LAST_SAVE_FILE_ATTR_NAME);
 			if(lastSave != null){
 				log.debug("last save file: "+lastSave);
 				return lastSave;
 			}
 		} catch (FileNotFoundException e) {
 			log.error("",e);
 		} catch (IOException e) {
 			log.error("",e);
 		}
 		
 		return null;
 	}
 
 	public static void writeLastSaveFile(File saveFile){
 		Properties prop = new Properties();
 		try {
 			log.debug("writing .showgo.properties");
 			log.debug("last saved file: " + saveFile.getCanonicalPath());
			prop.setProperty(LAST_SAVE_FILE_ATTR_NAME, saveFile.getCanonicalPath());
 			
 			String userHome = System.getProperty("user.home");
			File f = new File(userHome, FILE_NAME);
 			log.debug("Saving to file " + f);
 			prop.store(new FileOutputStream(f), null);
 		} catch (IOException e) {
 			log.error("",e);
 		}
 	}
 	
 }
