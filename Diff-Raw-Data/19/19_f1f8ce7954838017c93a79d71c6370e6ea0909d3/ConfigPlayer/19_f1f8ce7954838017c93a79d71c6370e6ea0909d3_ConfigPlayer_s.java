 package eu.ha3.bukkit.intermission;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 public class ConfigPlayer
 {
 	private File file;
 	private Player ply;
 	
 	private YamlConfiguration config;
 	
 	public ConfigPlayer(Player ply, File file)
 	{
 		this.ply = ply;
 		this.file = file;
 		
 		if (file.exists())
 		{
 			this.config = YamlConfiguration.loadConfiguration(file);
 		}
 		else
 		{
 			this.config = new YamlConfiguration();
 		}
 	}
 	
 	public Player getPlayer()
 	{
 		return this.ply;
 	}
 	
 	public void subscribeTo(String subscription)
 	{
 		this.config.getConfigurationSection("subscriptions").set(subscription, true);
 		try
 		{
 			this.config.save(this.file);
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public void unsubscribeFrom(String subscription)
 	{
 		this.config.getConfigurationSection("subscriptions").set(subscription, false);
 		try
 		{
 			this.config.save(this.file);
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean hasChosenSubscription(String subscription)
 	{
 		return this.config.getConfigurationSection("subscriptions").get(subscription) != null;
 	}
 	
 	public boolean isSubscribed(String subscription)
 	{
 		return this.config.getConfigurationSection("subscriptions").getBoolean(subscription);
 	}
 }
