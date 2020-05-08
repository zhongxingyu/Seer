 package com.andoutay.jailhelper;
 
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
				p.sendMessage(JHUtils.formatMsgFromConfig(JHConfig.repeatedMsg, p, plugin));
 		
 		JHUtils.setMsgTimeout(plugin);
 	}
 	
 	public static String formatMsgFromConfig(String msg, Player p, JailHelper plugin)
 	{
		String ans = msg;
 		JailAPI jail = JHUtils.getJailAPI(plugin);
 		
		ans = ans.replace("$1", jail.getPrisoner(p.getName()).getReason());
		ans = ans.replace("$2", "" + jail.getPrisoner(p.getName()).getRemainingTimeMinutes());
 		
 		return ans;
 	}
 }
