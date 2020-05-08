 package de.brightstorm;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 
 public class itemRewarder implements Runnable {
 	Material item;
 	public itemRewarder() {
 		item = Material.getMaterial( payday.dies.getConfig().getInt("reward_item"));
 	}
 	
 	private void doJob(PDPlayer p) {
 		   if(payday.users.contains(p.getPlayer().getName())) payday.users.set(p.getPlayer().getName(), payday.users.getInt(p.getPlayer().getName())+1);
 		   else payday.users.set(p.getPlayer().getName(), 0);
 		   String group = p.getGroup();
 		   if(payday.users.getInt(p.getPlayer().getName())>=payday.dies.getConfig().getInt(group+".time")) {
 			   String raw = payday.dies.getConfig().getString("message");
 			   String ph1 = StringUtils.replace(raw, "%a", payday.dies.getConfig().getDouble(group+".amount")+" "+item.name());
 			   String message = StringUtils.replace(ph1, "%t", String.valueOf(payday.dies.getConfig().getInt(group+".time")));
 			   ItemStack s = new ItemStack(item, payday.dies.getConfig().getInt(group+".amount"));
 			   p.getPlayer().getInventory().addItem(s);
 			   g.given+=payday.dies.getConfig().getInt(group+".amount");
 			   payday.users.set(p.getPlayer().getName(), 0);
 			   p.getPlayer().sendMessage(ChatColor.BLUE+message);
 			   payday.log.info(p.getPlayer().getName()+" just got "+payday.dies.getConfig().getDouble(group+".amount")+" "+item.name()+" for being online "+payday.dies.getConfig().getInt(group+".time")+" minutes.");
 		   }
 	}
 	
 	@Override
 	public void run() {
 		   payday.groups = payday.dies.getConfig().getStringList("groups");
 		   for(int i=0; i<payday.dies.getServer().getOnlinePlayers().length; i++) {
 			   PDPlayer p=new PDPlayer(payday.dies.getServer().getOnlinePlayers()[i]);
 			   p.findGroup();
 			   if(g.useEssentials) {
 				   EssentialsInterface ei = new EssentialsInterface();
 				   if(!ei.isAfk(p.getPlayer())) doJob(p);
			   } else doJob(p);
 		   }
 	}
 }
