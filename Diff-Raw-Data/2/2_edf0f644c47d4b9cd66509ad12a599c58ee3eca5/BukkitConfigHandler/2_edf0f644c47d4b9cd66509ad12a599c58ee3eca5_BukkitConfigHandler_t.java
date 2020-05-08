 /**
  * 
  */
 package com.n8lm.MCShopSystemPlugin.config;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.List;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 import com.n8lm.MCShopSystemPlugin.MainPlugin;
 
 /**
  * @author Alchemist
  *
  */
 public class BukkitConfigHandler extends ConfigHandler {
 
 	public BukkitConfigHandler()
 	{
 		super();
 	}
 	/* (non-Javadoc)
 	 * @see com.n8lm.MCShopSystemPlugin.config.ConfigHandler#loadSettings()
 	 */
 	@Override
 	public Settings loadSettings() {
 
 		// Prepare new settings map
 		Settings settings = new Settings();
 
 		// Set default values
 		settings.setPort(10808);
 		settings.setDebugMode(false);
 		settings.setServerActive(false);
 		
 		FileConfiguration config = MainPlugin.getInstance().getConfig();
 		settings.setDebugMode(config.getBoolean("debug"));
 		settings.setURL(config.getString("url"));
 		settings.setCommand(config.getString("command"));
		settings.setPassword(config.getString("server.pass"));
 		settings.setPort(config.getInt("server.port"));
 		settings.setServerActive(config.getBoolean("server.active"));
 		settings.setSalt(config.getString("server.salt"));
 		List<String> hosts = config.getStringList("server.hosts");
 		for(String value : hosts)
 		{
 			try {
 				InetAddress address = InetAddress.getByName(value);
 				address = InetAddress.getByAddress(address.getAddress());
 				settings.addHost(address);
 				MainPlugin.getMainLogger().info("Add Host '" + address + "' successfully");
 			} catch (UnknownHostException ex) {
 				// TODO Auto-generated catch block
 				MainPlugin.getMainLogger().info("Host '" + value + "' is invaild");
 			}
 		}
 		// TODO Auto-generated method stub
 		return settings;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.n8lm.MCShopSystemPlugin.config.ConfigHandler#generateConfig()
 	 */
 	@Override
 	public void generateConfig() {
 		MainPlugin.getInstance().saveDefaultConfig();
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public Boolean hasConfig() {
 		// TODO Auto-generated method stub
 		MainPlugin.getInstance().saveDefaultConfig();
 		return true;
 	}
 
 }
