 package musician101.itembank;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 /**
  * The code to hook into Vault for economy support.
  * 
  * @author Musician101
  */
 public class Econ
 {
 	protected Economy econ = null;
 	private boolean enabled = false;
 	
 	/**
 	 * Constructor for setting up the economy.
 	 */
 	protected Econ()
 	{
 		if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null)
 			enabled = false;
		else
		{	
			RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
			if (rsp == null)
				enabled = false;
 		
			econ = rsp.getProvider();
			enabled = econ != null;
		}
 	}
 	
 	protected void clearData()
 	{
 		econ = null;
 		enabled = false;
 	}
 	
 	/**
 	 * Checks if the economy is available for use.
 	 * 
 	 * @return true if economy plugin is detected, otherwise false.
 	 */
 	public boolean isEnabled()
 	{
 		return enabled;
 	}
 	
 	/**
 	 * Gets how much money a given player has.
 	 * 
 	 * @param player Player's name.
 	 * @return money player has, 0 if no economy plugin was found.
 	 */
 	public double getMoney(String player)
 	{
 		if (!enabled)
 			return 0;
 		
 		if (econ != null)
 			return econ.getBalance(player);
 		
 		return 0;
 	}
 	
 	/**
 	 * Gives money to the player.
 	 * 
 	 * @param player Player's name.
 	 * @param amount Amount to give.
 	 */
 	public void giveMoney(String player, double amount)
 	{
 		if (!enabled || amount == 0)
 			return;
 		
 		if (econ != null)
 		{
 			econ.depositPlayer(player, amount);
 		}
 	}
 	
 	/**
 	 * Takes money to the player.
 	 * 
 	 * @param player Player's name.
 	 * @param amount Amount to take.
 	 */
 	public void takeMoney(String player, double amount)
 	{
 		if (!enabled || amount == 0)
 			return;
 		
 		if (econ != null)
 		{
 			econ.withdrawPlayer(player, amount);
 		}
 	}
 }
