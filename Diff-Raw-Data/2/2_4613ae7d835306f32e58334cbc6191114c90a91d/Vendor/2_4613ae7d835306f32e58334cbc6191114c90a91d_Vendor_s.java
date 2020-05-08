 package eu.ha3.bukkit.intermission;
 
 import java.io.BufferedReader;
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 
 public class Vendor implements Runnable
 {
 	private IntermissionPlugin plugin;
 	private String name;
 	private ConfigurationSection config;
 	
 	private boolean isRunning;
 	private int taskId;
 	
 	private List<String> messages;
 	private int pointer;
 	
 	public Vendor(IntermissionPlugin plugin, String name, ConfigurationSection config)
 	{
 		this.plugin = plugin;
 		this.name = name;
 		this.config = config;
 		
 		this.isRunning = false;
 		
 		this.messages = new ArrayList<String>();
 		this.pointer = 0;
 		
 		loadMessages();
 	}
 	
 	@Override
 	public void run()
 	{
 		displayNow();
 	}
 	
 	public void displayNow()
 	{
 		if (this.messages.size() == 0)
 			return;
 		
 		if (this.pointer >= this.messages.size())
 		{
 			this.pointer = 0;
 		}
 		
 		String message = this.messages.get(this.pointer);
 		this.plugin.dispatchMessage(this, ChatColor.translateAlternateColorCodes(
 			'&', String.format(this.config.getString("display.format"), message)));
 		
 		this.pointer = this.pointer + 1;
 		
 	}
 	
 	public void startRunning()
 	{
 		if (this.isRunning)
 			return;
 		
 		if (!this.config.getBoolean("enable"))
 			return;
 		
 		this.isRunning = true;
 		this.taskId =
 			this.plugin
 				.getServer()
 				.getScheduler()
 				.scheduleSyncRepeatingTask(
 					this.plugin, this,
 					20 * this.config.getLong("display.delay", this.config.getLong("display.period")),
					this.config.getLong("display.period"));
 		
 	}
 	
 	public void stopRunning()
 	{
 		if (!this.isRunning)
 			return;
 		
 		this.isRunning = false;
 		this.plugin.getServer().getScheduler().cancelTask(this.taskId);
 	}
 	
 	private void loadMessages()
 	{
 		this.messages.clear();
 		for (String filename : this.config.getStringList("messages"))
 		{
 			BufferedReader reader = null;
 			try
 			{
 				File file = new File(this.plugin.getDataFolder(), filename);
 				reader = new BufferedReader(new FileReader(file));
 				
 				String line;
 				while ((line = reader.readLine()) != null)
 				{
 					if (line.length() > 0)
 					{
 						this.messages.add(line);
 					}
 				}
 				
 			}
 			catch (Exception e)
 			{
 				this.plugin.getLogger().severe("Vendor " + this.name + " could not load file " + filename);
 				
 			}
 			finally
 			{
 				closeSilently(reader);
 			}
 			
 		}
 		
 		if (this.config.getBoolean("display.shuffle"))
 		{
 			Collections.shuffle(this.messages);
 			this.pointer = 0;
 		}
 		
 		if (this.messages.size() == 0)
 		{
 			this.plugin.getLogger().severe("Vendor " + this.name + " has no messages!");
 		}
 		
 	}
 	
 	public void reloadMessages()
 	{
 		loadMessages();
 		
 	}
 	
 	private void closeSilently(Closeable closeable)
 	{
 		if (closeable == null)
 			return;
 		try
 		{
 			closeable.close();
 		}
 		catch (Throwable e)
 		{
 		}
 		
 	}
 	
 	/**
 	 * Tell if the player should reveive the message according to their
 	 * privileges and preferences. If the player happens to have a preference
 	 * but not the privilege, then the privilege takes over.
 	 * 
 	 * @param ply
 	 * @return
 	 */
 	public boolean shouldPlayerReceive(ConfigPlayer ply)
 	{
 		// If the Vendor only publishes to restricted audience
 		// and the user does NOT meet the audience criteria
 		// then do NOT publish.
 		if (this.config.getBoolean("visibility.enable")
 			&& !ply.getPlayer().hasPermission(this.config.getString("visibility.node")))
 			return false;
 		
 		// If the Vendor only publishes to subscribers
 		if (this.config.getBoolean("subscription_based.enable"))
 		{
 			// If the Vendor can be subscribed from/to only for certain elite users
 			// but the user has NOT the right to change the subscription node
 			// then ignore that user preference and use the default value
 			if (this.config.getBoolean("subscription_based.permission_to_change_subscription.enable")
 				&& !ply.getPlayer().hasPermission(
 					this.config.getString("subscription_based.permission_to_change_subscription.node")))
 				return this.config.getBoolean("subscription_based.default");
 			
 			// Otherwise, anyone can subscribe or unsubscribe. In these cases,
 			// check if the player has actually set a preference
 			if (ply.hasChosenSubscription(this.name))
 				return ply.isSubscribed(this.name);
 			else
 				return this.config.getBoolean("subscription_based.default");
 		}
 		
 		// If it is not subscription based, then show it.
 		
 		return true;
 	}
 	
 	/**
 	 * Tell if the player is able to subscribe to this Vendor. This returns
 	 * false if the Vendor is not subscription based.
 	 * 
 	 * @param ply
 	 * @return
 	 */
 	public boolean canPlayerSubscribe(Player ply)
 	{
 		// If this is not subscription based, then you can't subscribe to it
 		if (!this.config.getBoolean("subscription_based.enable"))
 			return false;
 		
 		// If the Vendor only publishes to restricted audience
 		// and the user does NOT meet the audience criteria
 		// then do NOT reveal, even if this is subscription based.
 		if (this.config.getBoolean("visibility.enable") && !ply.hasPermission(this.config.getString("visibility.node")))
 			return false;
 		
 		// If the Vendor can be subscribed from/to only for certain elite users
 		// but the user has NOT the right to change the subscription node
 		// then the user can NOT change the preference
 		// (if the preference has been changed before, then it is ignored even though the preference will remain stored)
 		if (this.config.getBoolean("subscription_based.permission_to_change_subscription.enable")
 			&& !ply.hasPermission(this.config.getString("subscription_based.permission_to_change_subscription.node")))
 			return false;
 		
 		return true;
 	}
 	
 	/**
 	 * Tell if the player could potentially see messages dispatched by this
 	 * Vendor if the player has the right preferences set, regardless of the
 	 * current player preferences.
 	 * 
 	 * @param ply
 	 * @return
 	 */
 	public boolean couldPlayerReceive(Player ply)
 	{
 		// If the Vendor only publishes to restricted audience
 		// and the user does NOT meet the audience criteria
 		// then the user can NOT possibly receive such message
 		if (this.config.getBoolean("visibility.enable") && !ply.hasPermission(this.config.getString("visibility.node")))
 			return false;
 		
 		// If the Vendor only publishes to subscribers
 		if (this.config.getBoolean("subscription_based.enable"))
 		{
 			// If the Vendor can be subscribed from/to only for certain elite users
 			// but the user has NOT the right to change the subscription node
 			// and the default subscription is set to false
 			// then the user can NOT possibly receive such message
 			if (this.config.getBoolean("subscription_based.permission_to_change_subscription.enable")
 				&& !ply.hasPermission(this.config
 					.getString("subscription_based.permission_to_change_subscription.node"))
 				&& this.config.getBoolean("subscription_based.default") == false)
 				return false;
 			
 		}
 		
 		return true;
 	}
 	
 	public String getName()
 	{
 		return this.name;
 		
 	}
 	
 	public String getDescription()
 	{
 		return this.config.getString("description", "");
 		
 	}
 	
 	public boolean isRunning()
 	{
 		return this.isRunning;
 	}
 	
 	public boolean isSubscriptionBased()
 	{
 		return this.config.getBoolean("subscription_based.enable");
 	}
 	
 	public boolean shouldDisplayOnConsole()
 	{
 		return this.config.getBoolean("display.console", false);
 	}
 }
