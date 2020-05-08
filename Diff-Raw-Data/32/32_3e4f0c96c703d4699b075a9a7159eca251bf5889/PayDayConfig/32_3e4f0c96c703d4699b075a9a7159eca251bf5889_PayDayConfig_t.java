 package me.greatman.Craftconomy.utils;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import me.greatman.Craftconomy.Craftconomy;
 import me.greatman.Craftconomy.Currency;
 import me.greatman.Craftconomy.CurrencyHandler;
 
import org.bukkit.World;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class PayDayConfig {
 
 	private static YamlConfiguration file;
 	
 	public String groupName;
 	public PayDayConfig()
 	{
 		try {
 			File theFile = new File(Craftconomy.plugin.getDataFolder(), "payday.yml");
 			if (!theFile.exists())
 			{
 				theFile.getParentFile().mkdirs();
 				copy(Craftconomy.plugin.getResource("payday.yml"), theFile);
 			}
 			file = new YamlConfiguration();
 			file.load(theFile);
 			
 		} catch (FileNotFoundException e) {
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	private void copy(InputStream in, File file) {
 	    try {
 	        OutputStream out = new FileOutputStream(file);
 	        byte[] buf = new byte[1024];
 	        int len;
 	        while((len=in.read(buf))>0){
 	            out.write(buf,0,len);
 	        }
 	        out.close();
 	        in.close();
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	    }
 	}
	public static World getWorld(String groupName)
	{
		return Craftconomy.plugin.getServer().getWorld(file.getString(groupName + ".world"));
	}
 	/**
 	 * Get the type of the Payday (Wage or Tax)
 	 * @param groupName The Group name we want to get
 	 * @return tax or wage or null if the entry in the configuration file is wrong
 	 */
 	public static String getType(String groupName)
 	{
 		String type = file.getString(groupName + ".type");
 		if (type.equalsIgnoreCase("wage") || type.equalsIgnoreCase("tax"))
 			return type;
 		return null;
 	}
 	/**
 	 * Check if the payday is disabled for that group
 	 * @param groupName The group we want to get
 	 * @return True if disabled else false
 	 */
 	public static boolean isDisabled(String groupName)
 	{
 		return file.getBoolean(groupName + ".disabled");
 	}
 	
 	/**
 	 * Get the player who pays the tax or wage
 	 * @param groupName The group we want to get
 	 * @return none if it's the server that pays. Else the player name
 	 */
 	public static String whoPay(String groupName)
 	{
 		return file.getString(groupName + ".master");
 	}
 	/**
 	 * Get if it works only on Online players or anybody
 	 * @param groupName The group we want to get
 	 * @return True if it is only online players else false
 	 */
 	public static boolean isOnlineMode(String groupName)
 	{
 		return file.getBoolean(groupName + ".onlineMode");
 	}
 	/**
 	 * Get at what interval in minutes the PayDay is run
 	 * @param groupName The group we want to get
 	 * @return The interval
 	 */
 	public static int getInterval(String groupName)
 	{
 		return file.getInt(groupName + ".interval");
 	}
 	/**
 	 * Get the amount we want to give to players
 	 * @param groupName the group we want to get
 	 * @return The amount we want to give 
 	 */
 	public static double getAmount(String groupName)
 	{
 		return file.getDouble(groupName + ".value");
 	}
 	/**
 	 * Get the currency this Payday is working
 	 * @param groupName the group we want to get
 	 * @return A instance of the Currency
 	 */
 	public static Currency getCurrency(String groupName)
 	{
 		return CurrencyHandler.getCurrency(file.getString(groupName + ".currency"), true);
 	}
 	
 	public static boolean exists(String groupName)
 	{
 		return (file.get(groupName + ".interval") != null);
 	}
 }
