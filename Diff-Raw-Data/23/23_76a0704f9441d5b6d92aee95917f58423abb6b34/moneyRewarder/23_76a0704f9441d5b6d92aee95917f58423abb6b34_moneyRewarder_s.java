 package de.brightstorm;
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 import net.milkbowl.vault.economy.Economy;
 
 public class moneyRewarder implements Runnable {
 	private Economy economy;
     private boolean setupEconomy()
     {
         RegisteredServiceProvider<Economy> economyProvider = payday.dies.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
         if (economyProvider != null) {
             economy = economyProvider.getProvider();
         }
 
         return (economy != null);
     }
 	
 	public  moneyRewarder() {
 		setupEconomy();
 		payday.log.info("Hooked into "+economy.getName());
 	}
 	
 	private void doJob(PDPlayer p) {
 		   if(payday.users.contains(p.getPlayer().getName())) payday.users.set(p.getPlayer().getName(), payday.users.getInt(p.getPlayer().getName())+1);
 		   else payday.users.set(p.getPlayer().getName(), 0);
 		   String group = p.getGroup();
 		   if(payday.users.getInt(p.getPlayer().getName())>=payday.dies.getConfig().getInt(group+".time")) {
 			   String raw = payday.dies.getConfig().getString("message");
 			   String ph1 = StringUtils.replace(raw, "%a", payday.dies.getConfig().getDouble(group+".amount")+" "+economy.currencyNamePlural());
 			   String message = StringUtils.replace(ph1, "%t", String.valueOf(payday.dies.getConfig().getInt(group+".time")));
 			   economy.depositPlayer(p.getPlayer().getName(), payday.dies.getConfig().getDouble(group+".amount"));
 			   g.paid+=payday.dies.getConfig().getDouble(group+".amount");
 			   payday.users.set(p.getPlayer().getName(), 0);
 			   p.getPlayer().sendMessage(ChatColor.BLUE+message);
 			   payday.log.info(p.getPlayer().getName()+" just got "+payday.dies.getConfig().getDouble(group+".amount")+" "+economy.currencyNamePlural()+" for being online "+payday.dies.getConfig().getInt(group+".time")+" minutes.");
 		   }
 
 	}
 	
 	@Override
 	public void run() {
 		   payday.groups = payday.dies.getConfig().getStringList("groups");
 		   for(int i=0; i<payday.dies.getServer().getOnlinePlayers().length; i++) {
 			   Player pl = payday.dies.getServer().getOnlinePlayers()[i];
 			   PDPlayer p = new PDPlayer(pl);
 			   p.findGroup();
 			   if(g.useEssentials) {
 				   EssentialsInterface ei = new EssentialsInterface();
 				   if(!ei.isAfk(p.getPlayer())) doJob(p);
			   } else doJob(p);
 		   }
 	}
 
 }
