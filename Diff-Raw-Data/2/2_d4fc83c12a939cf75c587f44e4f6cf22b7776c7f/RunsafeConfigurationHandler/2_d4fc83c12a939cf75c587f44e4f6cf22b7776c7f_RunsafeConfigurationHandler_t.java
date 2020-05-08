 package no.runsafe.framework.configuration;
 
 import no.runsafe.framework.event.IConfigurationChanged;
 import no.runsafe.framework.messaging.IMessageBusService;
 import no.runsafe.framework.messaging.Message;
 import no.runsafe.framework.messaging.MessageBusStatus;
 import no.runsafe.framework.messaging.Response;
 import no.runsafe.framework.output.ChatColour;
 import no.runsafe.framework.output.IOutput;
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 
 public class RunsafeConfigurationHandler implements IConfiguration, IMessageBusService
 {
 	private String configFilePath;
 	private IConfigurationFile configurationFile;
 	private final IOutput pluginOutput;
 	private FileConfiguration configFile;
 	private List<IConfigurationChanged> subscribers;
 
 	public RunsafeConfigurationHandler(IOutput pluginOutput)
 	{
 		this.pluginOutput = pluginOutput;
 	}
 
 	@Override
 	public void setConfigFileProvider(IConfigurationFile provider)
 	{
 		this.configFilePath = provider.getConfigurationPath();
 		this.configurationFile = provider;
 		this.load();
 	}
 
 	@Override
 	public void load()
 	{
 		if (this.configFilePath == null)
 			return;
 
 		File configFile = new File(this.configFilePath);
 
 		this.configFile = YamlConfiguration.loadConfiguration(configFile);
 		InputStream defaults = this.configurationFile.getDefaultConfiguration();
 		if (defaults != null)
 		{
 			this.configFile.setDefaults(YamlConfiguration.loadConfiguration(defaults));
 			this.configFile.options().copyDefaults(true);
 		}
 		this.save();
 		notifySubscribers();
 	}
 
 	// Replaces the current configuration values with the supplied defaults
 	@Override
 	public boolean restoreToDefaults()
 	{
 		if (this.configFile.getDefaults() != null)
 		{
 			this.configFile.options().copyDefaults(true);
 			this.save();
 			this.output("Configuration restored to defaults.");
 			return true;
 		}
 		return false;
 	}
 
 	// Saves the current configuration file to disk
 	@Override
 	public void save()
 	{
 		if (this.configFile != null)
 		{
 			try
 			{
 				this.configFile.save(new File(this.configFilePath));
 			}
 			catch (IOException ex)
 			{
 				this.output(String.format("Unable to save to configuration file: %s", this.configFilePath), Level.SEVERE);
 			}
 		}
 	}
 
 	// Returns a configuration value as a string
 	@Override
 	public String getConfigValueAsString(String value)
 	{
 		if (this.configFile == null)
 			return null;
 		return this.configFile.getString(value);
 	}
 
 	// Returns a configuration value as a boolean
 	@Override
 	public boolean getConfigValueAsBoolean(String key)
 	{
 		String value = this.getConfigValueAsString(key);
 		return value != null && Boolean.parseBoolean(value);
 	}
 
 	// Returns a configuration value as an integer
 	@Override
 	public int getConfigValueAsInt(String key)
 	{
 		String value = this.getConfigValueAsString(key);
 		if (value == null)
 			return 0;
 		return Integer.parseInt(value);
 	}
 
 	// Returns a configuration value as a double
 	@Override
 	public double getConfigValueAsDouble(String key)
 	{
 		String value = this.getConfigValueAsString(key);
 		if (value == null)
 			return 0;
 		return Double.parseDouble(value);
 	}
 
 	// Returns a configuration value as a float
 	@Override
 	public float getConfigValueAsFloat(String key)
 	{
 		String value = this.getConfigValueAsString(key);
 		if (value == null)
 			return 0;
 		return Float.parseFloat(value);
 	}
 
 	@Override
 	public List<String> getConfigValueAsList(String value)
 	{
 		if (this.configFile == null)
 			return null;
 		return this.configFile.getStringList(value);
 	}
 
 	@Override
 	public Map<String, String> getConfigValuesAsMap(String path)
 	{
 		if (this.configFile == null)
 			return null;
 		ConfigurationSection section = this.configFile.getConfigurationSection(path);
 		HashMap<String, String> values = new HashMap<String, String>();
 		for (String key : section.getKeys(false))
			values.put(key, section.getString(key));
 		return values;
 	}
 
 	@Override
 	public Map<String, Map<String, String>> getConfigSectionsAsMap(String path)
 	{
 		if (this.configFile == null)
 			return null;
 		ConfigurationSection section = this.configFile.getConfigurationSection(path);
 		HashMap<String, Map<String, String>> results = new HashMap<String, Map<String, String>>();
 		for (String key : section.getKeys(false))
 			results.put(key, getConfigValuesAsMap(path + "." + key));
 		return results;
 	}
 
 	@Override
 	public Map<String, List<String>> getConfigSectionsAsList(String path)
 	{
 		if (this.configFile == null)
 			return null;
 		ConfigurationSection section = this.configFile.getConfigurationSection(path);
 		HashMap<String, List<String>> results = new HashMap<String, List<String>>();
 		for (String key : section.getKeys(false))
 			results.put(key, getConfigValueAsList(path + "." + key));
 		return results;
 	}
 
 	@Override
 	@Deprecated
 	public ConfigurationSection getSection(String path)
 	{
 		return this.configFile.getConfigurationSection(path);
 	}
 
 	// Sets a configuration value with the specified key -> value
 	@Override
 	public void setConfigValue(String key, Object value)
 	{
 		this.configFile.set(key, value);
 	}
 
 	private void output(String message)
 	{
 		if (this.pluginOutput != null)
 		{
 			this.pluginOutput.outputToConsole(message);
 		}
 	}
 
 	private void output(String message, Level level)
 	{
 		if (this.pluginOutput != null)
 		{
 			this.pluginOutput.outputToConsole(message, level);
 		}
 	}
 
 	@Override
 	public String getServiceName()
 	{
 		return "Configuration";
 	}
 
 	@Override
 	public Response processMessage(Message message)
 	{
 		load();
 		return new Response()
 		{{
 				setStatus(MessageBusStatus.OK);
 			}};
 	}
 
 	@Override
 	public void setListeners(List<IConfigurationChanged> subscribers)
 	{
 		this.subscribers = subscribers;
 		if (this.configFile != null)
 			notifySubscribers();
 	}
 
 	private void notifySubscribers()
 	{
 		if (subscribers != null)
 		{
 			for (IConfigurationChanged sub : subscribers)
 			{
 				try
 				{
 					pluginOutput.outputDebugToConsole(
 						String.format(
 							"Notifying subscriber %s about updated configuration.",
 							sub.getClass().getCanonicalName()
 						),
 						Level.FINE
 					);
 					sub.OnConfigurationChanged(this);
 				}
 				catch (Exception e)
 				{
 					pluginOutput.writeColoured(
 						"Configuration error: %s%s%s\n%s",
 						Level.SEVERE,
 						ChatColour.RED,
 						ExceptionUtils.getMessage(e),
 						ChatColour.RESET,
 						ExceptionUtils.getStackTrace(e)
 					);
 				}
 			}
 			pluginOutput.outputToConsole(
 				String.format("Configuration change notifications sent to %d modules.", subscribers.size())
 			);
 		}
 	}
 }
