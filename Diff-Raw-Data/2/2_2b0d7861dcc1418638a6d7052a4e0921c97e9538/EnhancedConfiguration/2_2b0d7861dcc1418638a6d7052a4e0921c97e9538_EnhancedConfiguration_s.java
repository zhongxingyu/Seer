 package com.feildmaster.lib.configuration;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.bukkit.configuration.MemoryConfiguration;
 import org.bukkit.plugin.Plugin;
 
 /*
  * __ Things to do __
  * Path Comments
  * - The next thing to code
  * Case Insensitivity
  * - The "proper" way for this would increase configuration memory. Look into further.
  * Lowercase Keys
  * - This is terrible to implement as well...
  * Comments. Steal from here
  * https://github.com/dumptruckman/PluginTemplate/blob/master/src/main/java/com/dumptruckman/plugintemplate/config/CommentedConfig.java
  * I want to add 'literalsections' :D
  */
 
 /**
  * Enhancing configuration to do the following: <li>Stores a file for configuration to use.</li> <li>Self contained "load," "reload," and "save" functions.</li> <li>Self contained "loadDefaults" functions that set defaults.</li> <li>Adds "getLastException" to return the last exception from self contained functions.</li> <li>Adds "options().header(String, String)" to build multiline headers easier(?)</li>
  * 
  * @author Feildmaster
  */
 public class EnhancedConfiguration extends org.bukkit.configuration.file.YamlConfiguration {
 	private final Pattern pattern = Pattern.compile("\n"); // Static? Maybe bad? I'm not sure.
 	private final File file;
 	private final Plugin plugin;
 	private Exception exception;
 	private Map<String, Object> cache = new HashMap<String, Object>();
 	private boolean modified = false;
 	private long last_modified = -1L;
 
 	/**
 	 * Creates a new EnhancedConfiguration with a file named "config.yml," stored in the plugin DataFolder
 	 * <p />
 	 * Will fail if plugin is null.
 	 * 
 	 * @param plugin The plugin registered to this Configuration
 	 */
 	public EnhancedConfiguration(Plugin plugin){
 		this("config.yml", plugin);
 	}
 
 	/**
 	 * Creates a new EnhancedConfiguration with a file stored in the plugin DataFolder
 	 * <p />
 	 * Will fail if plugin is null.
 	 * 
 	 * @param file The name of the file
 	 * @param plugin The plugin registered to this Configuration
 	 */
 	public EnhancedConfiguration(String file, Plugin plugin){
 		this(new File(plugin.getDataFolder(), file), plugin);
 	}
 
 	/**
 	 * Creates a new EnhancedConfiguration with the file provided and a null {@link Plugin}
 	 * 
 	 * @param file The file to store in this configuration
 	 */
 	public EnhancedConfiguration(File file){
 		this(file, null);
 	}
 
 	/**
 	 * Creates a new EnhancedConfiguration with given File and Plugin.
 	 * 
 	 * @param file The file to store in this configuration
 	 * @param plugin The plugin registered to this Configuration
 	 */
 	public EnhancedConfiguration(File file, Plugin plugin){
 		this.file = file;
 		this.plugin = plugin;
 		options = new EnhancedConfigurationOptions(this);
 		load();
 	}
 
 	/**
 	 * Loads set file
 	 * <p />
 	 * Does not load if file has not been changed since last load
 	 * <p />
 	 * Stores exception if possible.
 	 * 
 	 * @return True on successful load
 	 */
 	public final boolean load(){
 		if(last_modified != -1L && !isFileModified()) { // File hasn't been modified since last load
 			return true;
 		}
 
 		try {
 			load(file);
 			clearCache();
 			last_modified = file.lastModified();
 			return true;
 		} catch(Exception ex) {
 			exception = ex;
 			return false;
 		}
 	}
 
 	/**
 	 * Saves to the set file
 	 * <p />
 	 * Stores exception if possible.
 	 * 
 	 * @return True on successful save
 	 */
 	public final boolean save(){
 		try {
 			save(file);
 			modified = false;
 			return true;
 		} catch(Exception ex) {
 			exception = ex;
 			return false;
 		}
 	}
 
 	/**
 	 * Returns the last stored exception
 	 * 
 	 * @return Last stored Exception
 	 */
 	public Exception getLastException(){
 		return exception;
 	}
 
 	/**
 	 * Loads defaults based off the name of stored file.
 	 * <p />
 	 * Stores exception if possible.
 	 * <p />
 	 * Will fail if Plugin is null.
 	 * 
 	 * @return True on success
 	 */
 	public boolean loadDefaults(){
 		try {
 			return loadDefaults(file.getName());
 		} catch(Exception ex) {
 			exception = ex;
 			return false;
 		}
 	}
 
 	/**
 	 * Sets your defaults after loading the Plugin file.
 	 * <p />
 	 * Stores exception if possible.
 	 * <p />
 	 * Will fail if Plugin is null.
 	 * 
 	 * @param filename File to load from Plugin jar
 	 * @return True on success
 	 */
 	public boolean loadDefaults(String filename){
 		try {
 			return loadDefaults(plugin.getResource(filename));
 		} catch(Exception ex) {
 			exception = ex;
 			return false;
 		}
 	}
 
 	/**
 	 * Sets your defaults after loading them.
 	 * <p />
 	 * Stores exception if possible.
 	 * 
 	 * @param filestream Stream to load defaults from
 	 * @return True on success, false otherwise.
 	 */
 	public boolean loadDefaults(InputStream filestream){
 		try {
 			setDefaults(loadConfiguration(filestream));
 			clearCache();
 			return true;
 		} catch(Exception ex) {
 			exception = ex;
 			return false;
 		}
 	}
 
 	/**
 	 * Saves configuration with all defaults
 	 * 
 	 * @return True if saved
 	 */
 	public boolean saveDefaults(){
 		options().copyDefaults(true); // These stay so future saves continue to copy over.
 		options().copyHeader(true);
 		return save();
 	}
 
 	/**
 	 * Check loaded defaults against current configuration
 	 * 
 	 * @return false When all defaults aren't present in config
 	 */
 	public boolean checkDefaults(){
 		if(getDefaults() == null) {
 			return true;
 		}
 		return getKeys(true).containsAll(getDefaults().getKeys(true));
 	}
 
 	/**
 	 * Clear the defaults from memory
 	 */
 	public final void clearDefaults(){
 		setDefaults(new MemoryConfiguration());
 	}
 
 	/**
 	 * Clears the file of all values in memory
 	 */
 	public final void clearFile(){
 		cache.clear();
 		super.map.clear();
 	}
 
 	/**
 	 * Checks if the file exists, contains all defaults and if this configuration has been modified.
 	 * 
 	 * @return True if the file should be updated (saved)
 	 */
 	public boolean needsUpdate(){
 		return !fileExists() || !checkDefaults() || isModified();
 	}
 
 	/**
 	 * @return True if file exists, False if not, or if there was an exception.
 	 */
 	public boolean fileExists(){
 		try {
 			return file.exists();
 		} catch(Exception ex) {
 			exception = ex;
 			return false;
 		}
 	}
 
 	/**
 	 * @return {@link EnhancedConfigurationOptions}
 	 */
 	@Override
 	public EnhancedConfigurationOptions options(){
 		return (EnhancedConfigurationOptions) options;
 	}
 
 	@Override
 	public Object get(String path, Object def){
 		Object value = cache.get(path);
 		if(value != null) {
 			return value;
 		}
 
 		value = super.get(path, def);
 		if(value != null) {
 			cache.put(path, value);
 		}
 
 		return value;
 	}
 
 	@Override
 	public void set(String path, Object value){
 		if(value == null && cache.containsKey(path)) {
 			cache.remove(path);
 			modified = true;
 		} else if(value != null) {
 			if(!value.equals(get(path))) {
 				modified = true;
 			}
 			cache.put(path, value);
 
 		}
 		super.set(path, value);
 	}
 
 	/**
 	 * Removes the specified path from the configuration.
 	 * <p />
 	 * Currently equivilent to set(path, null).
 	 * 
 	 * @param path The path to remove
 	 */
 	public void unset(String path){
 		set(path, null);
 	}
 
 	@Override
 	public List<?> getList(String path, List<?> def){
 		List<?> list = super.getList(path, def);
		return list == null ? new ArrayList(0) : list;
 	}
 
 	/**
 	 * Call this method to clear the cache manually.
 	 * 
 	 * Automatically clears on "load"
 	 */
 	public void clearCache(){
 		cache.clear();
 	}
 
 	// Replaces \n with System line.separator
 	@Override
 	public String saveToString(){ // TODO: Custom YAML loader/saver?
 		String separator = System.getProperty("line.separator");
 		if(separator.equals("\n")) { // Do nothing
 			return super.saveToString();
 		}
 		return pattern.matcher(super.saveToString()).replaceAll(separator);
 	}
 
 	/**
 	 * @return The plugin associated with this configuration.
 	 */
 	public Plugin getPlugin(){
 		return plugin;
 	}
 
 	protected File getFile(){
 		return file;
 	}
 
 	/**
 	 * Checks if loaded configuration (not the file) has been modified.
 	 * 
 	 * @return True if local configuration has been modified
 	 */
 	public boolean isModified(){
 		return modified;
 	}
 
 	/**
 	 * Checks if file has been modified since last load().
 	 * 
 	 * @return True if file has been modified
 	 */
 	public boolean isFileModified(){
 		try {
 			return last_modified != file.lastModified();
 		} catch(Exception e) {
 			this.exception = e;
 			return false;
 		}
 	}
 }
