 package com.andoutay.jailhelper;
 
 import java.text.DecimalFormat;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.scheduler.BukkitScheduler;
 
 import com.matejdro.bukkit.jail.Jail;
 import com.matejdro.bukkit.jail.JailAPI;
 
 public class JHUtils
 {
 	public static JailAPI getJailAPI(JailHelper plugin)
 	{
 		JailAPI jail = null;
 		Plugin temp = plugin.getServer().getPluginManager().getPlugin("Jail");
 		if (temp != null)
 			jail = ((Jail)temp).API;
 		else
 			plugin.getServer().getPluginManager().disablePlugin(plugin);
 		
 		return jail;
 	}
 	
 	public static void setMsgTimeout(final JailHelper plugin)
 	{
 		BukkitScheduler scheduler = plugin.getServer().getScheduler();
 		
 		if (JHConfig.repeatTime > 0) scheduler.runTaskLater(plugin, new Runnable () { public void run() { JHUtils.msgJailedPlayers(plugin); }}, 20 * 60 * JHConfig.repeatTime);
 	}
 	
 	public static void msgJailedPlayers(final JailHelper plugin)
 	{
 		JailAPI jail = JHUtils.getJailAPI(plugin);
 		for (Player p : plugin.getServer().getOnlinePlayers())
 			if (jail.isPlayerJailed(p.getName()) && p.hasPermission("jailhelper.showperiodically"))
 				p.sendMessage(formatMsgFromConfig(JHConfig.repeatedMsg, p, plugin));
 		
 		JHUtils.setMsgTimeout(plugin);
 	}
 	
 	public static String formatMsgFromConfig(String msg, Player p, JailHelper plugin)
 	{
 		JailAPI jail = JHUtils.getJailAPI(plugin);
		String ans = msg, rsn = jail.getPrisoner(p.getName()).getReason();
 		if (rsn.equalsIgnoreCase("")) rsn = "no reason";
 		
 		ans = ans.replace("$1", rsn);
 		ans = ans.replace("$2", "" + round1Decimal(jail.getPrisoner(p.getName()).getRemainingTimeMinutes()));
 		
 		return ans;
 	}
 	
 	public static double round1Decimal(double d)
 	{
         DecimalFormat oneDForm = new DecimalFormat("#.#");
         return Double.valueOf(oneDForm.format(d));
 	}
 }
