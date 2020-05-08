 package ltguide.base.data;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import ltguide.base.Base;
 import ltguide.base.Debug;
import ltguide.clearinventory.data.Commands;
import ltguide.clearinventory.data.Messages;
 
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.yaml.snakeyaml.error.YAMLException;
 
 public class Configuration extends YamlConfiguration {
 	private final File file;
 	protected final JavaPlugin plugin;
 	protected int[] oldVersion;
 	
 	public Configuration(final JavaPlugin instance) {
 		this(instance, "config.yml");
 	}
 	
 	public Configuration(final JavaPlugin instance, final String config) {
 		plugin = instance;
 		file = new File(plugin.getDataFolder(), config);
 	}
 	
 	protected void loadConfig() {
 		try {
 			load(file);
 		}
 		catch (final FileNotFoundException e) {}
 		catch (final IOException e) {
 			Base.logException(e, "cannot load " + file);
 		}
 		catch (final InvalidConfigurationException e) {
 			if (e.getCause() instanceof YAMLException) Base.severe("Config file " + file + " isn't valid! " + e.getCause());
 			else if (e.getCause() == null || e.getCause() instanceof ClassCastException) Base.severe("Config file " + file + " isn't valid!");
 			else Base.logException(e, "cannot load " + file + ": " + e.getCause().getClass());
 		}
 		
 		final InputStream inStream = plugin.getResource(file.getName());
 		if (inStream != null) setDefaults(YamlConfiguration.loadConfiguration(inStream));
 	}
 	
 	public boolean upgradeConfig() {
 		if (Debug.ON) Debug.info("upgradeConfig()");
 		
 		String old = "<none>";
 		final String current = plugin.getDescription().getVersion();
 		
 		if (isSet("version-nomodify")) old = getString("version-nomodify");
 		else {
 			Base.debug("writing default configuration to " + file);
 			options().copyDefaults(true);
 		}
 		
 		if (current.equals(old)) return false;
 		
 		oldVersion = getVersionInt(old);
 		set("version-nomodify", current);
 		
 		return true;
 	}
 	
 	public void saveConfig() {
 		try {
 			save(file);
 		}
 		catch (final IOException e) {
 			Base.logException(e, "could not save " + file);
 		}
 	}
 	
	public void setDefaults() {
 		options().copyDefaults(true);
 		Base.setDebug(getBoolean("debug"));
 		
		for (final Messages messages : Messages.values())
			Message.setConfig(messages.name(), getString("messages." + messages.name().toLowerCase()));
 		
		for (final Commands command : Commands.values()) {
 			final String path = "commands." + command.name().toLowerCase();
 			
 			Command.setConfig(command.name(), getString(path + ".description"), null);
 		}
 	}
 	
 	protected int[] getVersionInt(final String version) {
 		final String[] split = version.split("\\.");
 		final int[] num = new int[split.length];
 		
 		for (int i = 0; i < split.length; i++)
 			try {
 				num[i] = Integer.parseInt(split[i]);
 			}
 			catch (final NumberFormatException e) {
 				num[i] = 0;
 			}
 		
 		return num;
 	}
 	
 	protected boolean versionCompare(final int[] old, final int[] current) {
 		if (old.length != current.length) return false;
 		
 		for (int i = 0; i < current.length; i++)
 			if (old[i] > current[i]) return false;
 		
 		return true;
 	}
 }
