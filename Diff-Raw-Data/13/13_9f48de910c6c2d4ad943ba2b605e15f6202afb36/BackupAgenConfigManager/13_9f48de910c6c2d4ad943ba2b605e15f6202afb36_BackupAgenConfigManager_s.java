 package ch.fetm.backuptools.common;
 
 import java.beans.XMLDecoder;
 import java.beans.XMLEncoder;
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 
 
 public abstract class BackupAgenConfigManager {
 
 	public final static BackupAgentConfig readConfigurationFile() {
 		BackupAgentConfig config = new BackupAgentConfig();
     	
 		Path configfile = getConfigFile();
 		if(!Files.exists(configfile))
 			return null;
 		
 		try {
 			XMLDecoder in = new XMLDecoder( new BufferedInputStream( 
 												new FileInputStream(configfile.toFile())));
 			config = (BackupAgentConfig) in.readObject();
 			in.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return config;
 	}
 
 	public static void writeConfigurationInFile(BackupAgentConfig config) {
 		Path configfile = getConfigFile();
 		try {	
 			if(Files.exists(configfile)){
 					Files.delete(configfile);
 				
			}else{
				Files.createFile(configfile);
				XMLEncoder out = new XMLEncoder( new FileOutputStream(configfile.toFile()));
				out.writeObject(config);
				out.flush();
				out.close();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 			
     }
 
 	private static Path getConfigFile() {
 		String home = System.getProperty("user.home");
 		
     	Path path = Paths.get( home
     						   + FileSystems.getDefault().getSeparator() 
     						   + ".backuptools");
     	
     	if(!path.toFile().exists()){
     		try {
 				Files.createDirectory(path);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
     	}
     	
     	Path configfile = Paths.get( path.toAbsolutePath().toString() 
     								 + FileSystems.getDefault().getSeparator()
     								 + "config.xml");
    
 		return configfile;
 	}
 
 	public static boolean ConfigfileExist() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
