 package com.randrdevelopment.propertygroup;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.logging.Level;
 
import org.bukkit.configuration.Configuration;

 import com.randrdevelopment.propertygroup.log.PropertyGroupLogger;
 
 public class DirectoryStructure {
 	private static File pluginDir = new File("plugins", "PropertyGroup");
 	private static File schematicsDir;
 	private static final String schematicsFoldername = "schematics";
 	private static File cfgProperties;
 	private static final String configProperties = "propertygroups.yml";
	private static PropertyGroup plugin;
	
	public static Configuration getConfig() {
		return plugin.getConfig();
	}
	
 	public static void setup() {
 		pluginDir = PropertyGroup.getInstance().getDataFolder();
 				
 		setupDirectoryStructure();
		setupDefaultConfiguration();
 		extractResources();
 	}
 
 	public static File getPluginDirectory() {
 		return pluginDir;
 	}
 
 	public static File getSchematicsDirectory() {
 		return schematicsDir;
 	}
 	
 	public static File getCfgProperties() {
 		return cfgProperties;
 	}
 
 	private static void setupDirectoryStructure() {
 		schematicsDir = new File(schematicsFoldername);
 
 		cfgProperties = new File(pluginDir, configProperties);
 
 		createDir(pluginDir);
 	}
 	
	private static void setupDefaultConfiguration() {
		plugin.getConfig().options().copyDefaults(true);
		//Configuration config = plugin.getConfig();
		
		plugin.saveConfig();
	}
	
 	public static File getJarFile() {
 		return new File("plugins", "PropertyGroup.jar");
 	}
 	
 	private static void extractResources() {
 		//extractResource("/settings.yml", pluginDir); 
 	}
 
 	private static void createDir(File dir) {
 		if (dir.isDirectory()) {
 			return;
 		}
 		if (!dir.mkdir()) {
 			PropertyGroupLogger.warning("Can't make directory " + dir.getName()); 
 		}
 	}
 
 	//private static void extractResource(String from, File toDir) {
 	//	extractResource(from, toDir, false);
 	//}
 
 	static void extractResource(String from, File to, boolean force) {
 		File of = to;
 		if (to.isDirectory()) {
 			String fname = new File(from).getName();
 			of = new File(to, fname);
 		} else if (!of.isFile()) {
 			return;
 		}
 		
 		// if the file exists and is newer than the JAR, then we'll leave it alone
 		if (of.exists() && of.lastModified() > getJarFile().lastModified() && !force) {
 			return;
 		}
 
 		OutputStream out = null;
 		try {
 			// Got to jump through hoops to ensure we can still pull messages from a JAR
 			// file after it's been reloaded...
 			URL res = PropertyGroup.class.getResource(from);
 			if (res == null) {
 				PropertyGroupLogger.warning("can't find " + from + " in plugin JAR file"); //$NON-NLS-1$
 				return;
 			}
 			URLConnection resConn = res.openConnection();
 			resConn.setUseCaches(false);
 			InputStream in = resConn.getInputStream();
 
 			if (in == null) {
 				PropertyGroupLogger.warning("can't get input stream from " + res); //$NON-NLS-1$
 			} else {
 				out = new FileOutputStream(of);
 				byte[] buf = new byte[1024];
 				int len;
 				while ((len = in.read(buf)) > 0) {
 					out.write(buf, 0, len);
 				}
 				in.close();
 				out.close();
 			}
 		} catch (Exception ex) {
 			PropertyGroupLogger.log(Level.SEVERE, null, ex);
 		} finally {
 			try {
 				if (out != null) {
 					out.close();
 				}
 			} catch (Exception ex) { //IOException
 
 			}
 		}
 	}
 
 	/**
 	 * Find a YAML resource file in the given directory.  Look first in the custom/ subdirectory
 	 * and then in the directory itself.
 	 * 
 	 * @param dir
 	 * @param filename
 	 * @param saving
 	 * @return
 	 * @throws ChessException
 	 */
 	public static File getResourceFile(File dir, String filename, boolean saving) {
 		File f = new File(dir, "custom" + File.separator + filename.toLowerCase() + ".yml");
 		if (!f.canRead() && !saving) {
 			f = new File(dir, filename.toLowerCase() + ".yml");
 			if (!f.canRead()) {
 				
 			}
 		}
 		return f;
 	}
 	
 	public static File getResourceFile(File dir, String filename) {
 		return getResourceFile(dir, filename, false);
 	}
 	
 	
 }
