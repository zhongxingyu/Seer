 package spaceshooters.util.config;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
import java.net.URISyntaxException;
 import java.nio.file.Files;
 import java.util.Properties;
 
 import spaceshooters.util.Logger;
 
 public class Configuration {
 	
 	private static Configuration config = new Configuration();
 	private Properties props;
 	private String path = System.getenv("APPDATA") + "/Spaceshooters 2/config.cfg";
 	
 	private Configuration() {
 		props = new Properties();
 		try {
 			this.load(new FileInputStream(path));
 		} catch (IOException e) {
 			this.copy();
 			try {
 				this.load(new FileInputStream(path));
 			} catch (IOException e2) {
 				Logger.error("I can't load the file I have just created.");
 				Logger.error("I am a moron, I know :(");
 			}
 		}
 	}
 	
 	/**
 	 * Grabs a boolean value from the config.
 	 * 
 	 * @param key
 	 *            The key from EnumConfig.
 	 * @return The value of given key in the config.
 	 */
 	public boolean getBoolean(EnumConfig key) {
 		try {
 			int i = Integer.parseInt(props.getProperty(key.toString()));
 			if (i == 0) {
 				return false;
 			} else if (i == 1) {
 				return true;
 			} else {
 				throw new NumberFormatException();
 			}
 		} catch (NumberFormatException e) {
 			Logger.error("The key " + key + " cannot be found or has an invalid value!");
 			Logger.error("Defaulting to " + key.getDefaultValue() + "!");
 			this.setBoolean(key, key.getDefaultValue());
 			return key.getDefaultValue();
 		}
 	}
 	
 	/**
 	 * Grabs a Integer value from the config.
 	 * 
 	 * @param key
 	 *            The key from EnumConfig.
 	 * @return The value of given key in the config.
 	 */
 	public int getInt(EnumConfig key) {
 		return Integer.parseInt(props.getProperty(key.toString()));
 	}
 	
 	/**
 	 * Sets a Integer value.
 	 * 
 	 * @param key
 	 *            The key from EnumConfig.
 	 * @param value
 	 *            Integer.
 	 */
 	public void setInt(EnumConfig key, int value) {
 		props.setProperty(key.toString(), Integer.toString(value));
 		this.save();
 	}
 	
 	/**
 	 * Sets a boolean value.
 	 * 
 	 * @param key
 	 *            The key from EnumConfig.
 	 * @param value
 	 *            True or false.
 	 */
 	public void setBoolean(EnumConfig key, boolean value) {
 		String configValue;
 		if (Boolean.valueOf(value)) {
 			configValue = "1";
 		} else {
 			configValue = "0";
 		}
 		props.setProperty(key.toString(), configValue);
 		this.save();
 	}
 	
 	/**
 	 * Checks for potential errors in the configuration.
 	 */
 	private void checkForErrors() {
 		if ((this.getBoolean(EnumConfig.USE_VSYNC)) && (this.getBoolean(EnumConfig.LIMIT_FPS))) {
 			this.setBoolean(EnumConfig.LIMIT_FPS, false);
 			Logger.error("Configuration error fixed! VSync and FPS limitation cannot be on simultaneously!");
 		}
 	}
 	
 	/**
 	 * Loads the configuration.
 	 * 
 	 * @param stream
 	 *            The input stream.
 	 * @throws IOException
 	 *             When configuration is not found.
 	 */
 	private void load(FileInputStream stream) throws IOException {
 		props.load(stream);
 		this.checkForErrors();
 		props.load(stream);
 	}
 	
 	/**
 	 * Saves the configuration.
 	 */
 	public void save() {
 		try {
 			props.store(new FileWriter(new File(path)), null);
 		} catch (IOException e) {
 			Logger.error("Something went wrong!");
 			Logger.error("I cannot save my configuration :(");
 		}
 	}
 	
 	/**
 	 * Copies the configuration file to %appdata%/Spaceshooters 2/config.cfg
 	 */
 	private void copy() {
 		new File(System.getenv("APPDATA") + "/Spaceshooters 2").mkdir();
 		try {
			File src = new File(this.getClass().getResource("/config.cfg").toURI());
 			File dest = new File(path);
			Files.copy(src.toPath(), dest.toPath());
		} catch (IOException | URISyntaxException e) {
 			Logger.error("I cannot copy my configuration.");
 			Logger.error("I tried really hard, I tell you!");
 		}
 	}
 	
 	public static Configuration getConfiguration() {
 		if (config == null) {
 			return new Configuration();
 		}
 		return config;
 	}
 }
