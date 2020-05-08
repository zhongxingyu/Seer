 package de.MiniDigger.ScrollingScoreBoardAnnouncer;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 
 /**
  * Representiert eine Config
  *
  * @author MiniDigger
  *
  */
 public class ScrollingScoreBoardConfig {
 
 	private YamlConfiguration config;
 	private File configFile;
 	private String name;
 
 	/**
 	 * Initalisiert die Config
 	 *
 	 * @param name
 	 *            Name des Standart Config Files
 	 */
 	public ScrollingScoreBoardConfig(String name) {
 		this.name = name;
 		load();
 	}
 
 	/**
 	 * Ld die Config wenn mglich oder erstellt eine neue
 	 */
 	public void load() {
		this.configFile = new File("plugins/ScrollingScoreBoardAnnouncer/" + name);
 
 		if (!this.configFile.exists()) {
 			try {
 				new File("plugins/ScrollingScoreBoardAnnouncer/").mkdirs();
 				this.configFile.createNewFile();
 				copyResourceYAML(getClass().getResourceAsStream(name),
 						this.configFile);
 			} catch (Exception e) {
 				ScrollingScoreBoardAnnouncer.error("Error(" + name + "): " + e);
 				e.printStackTrace();
 			}
 		}
 
 		this.config = new YamlConfiguration();
 		try {
 			this.config.load(this.configFile);
 		} catch (Exception e) {
 			ScrollingScoreBoardAnnouncer.error("Error(" + name + "): " + e);
 			e.printStackTrace();
 
 		}
 	}
 	
 	/**
 	 * Gibt ein Object aus der Config zurck
 	 *
 	 * @param path
 	 *            Der Path zum Objekt
 	 * @return Den Wert des Objekts
 	 */
 	public Object get(String path) {
 		load();
 		return config.get(path);
 	}
 
 	/**
 	 * Setzt ein Object in der Config
 	 *
 	 * @param path
 	 *            Der Path zum Objekt
 	 * @param value
 	 *            Der Wert des Objekts
 	 */
 	public void set(String path, Object value) {
 		load();
 		try {
 			config.set(path, value);
 			config.save(configFile);
 		} catch (Exception e) {
 			ScrollingScoreBoardAnnouncer.error(name + " couldnt saved!");
 		}
 	}
 	
 	/**
 	 * Gibt einen String aus der COnfig zurck
 	 *
 	 * @param path
 	 *            Der Path zum Objekt
 	 * @return Der Wert des Objekts
 	 */
 	public String getString(String path) {
 		load();
 
 		String data = config.getString(path);
 
 		return data;
 	}
 	
 	/**
 	 * Setzt einen String in der Config
 	 *
 	 * @param path
 	 *            Der Path zum Objekt
 	 * @param value
 	 *            Der Wert des Objekts
 	 */
 	public void setString(String path, String value) {
 		load();
 
 		try {
 			ScrollingScoreBoardAnnouncer.debug("Setzte " + name + "." + path + " auf " + value);
 			config.set(path, value);
 			config.save(configFile);
 		} catch (Exception e) {
 			ScrollingScoreBoardAnnouncer.error(name + " couldnt saved!");
 		}
 	}
 	
 	/**
 	 * Gibt einen Boolean zurck
 	 *
 	 * @param string
 	 * @return
 	 */
 	public boolean getBoolean(String path) {
 		load();
 		return config.getBoolean(path);
 	}
 
 
 
 	/**
 	 * Kopiert ein StandartFile in ein anderes
 	 */
 	public void copyResourceYAML(InputStream source, File target) {
 
 		BufferedWriter writer = null;
 		BufferedReader reader = new BufferedReader(
 				new InputStreamReader(source));
 
 		try {
 			writer = new BufferedWriter(new FileWriter(target));
 		} catch (IOException e) {
 			ScrollingScoreBoardAnnouncer.error("Failed to copy the config " + target.getName()
 					+ " (1)");
 		}
 
 		try {
 			try {
 				String buffer = "";
 
 				while ((buffer = reader.readLine()) != null) {
 					writer.write(buffer);
 					writer.newLine();
 				}
 			} catch (IOException e) {
 				ScrollingScoreBoardAnnouncer.error("Failed to copy the config "
 						+ target.getName() + " (2)");
 			}
 		} finally {
 			try {
 				if (writer != null) {
 	                writer.close();
                 }
 				if (reader != null) {
 	                reader.close();
                 }
 			} catch (IOException e) {
 				ScrollingScoreBoardAnnouncer.error("Failed to copy the config "
 						+ target.getName() + " (3)");
 			}
 		}
 	}
 }
