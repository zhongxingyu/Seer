 package me.lenis0012.pvp;
 
 import java.util.WeakHashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class MainListener implements Listener
 {
 	private WeakHashMap<Player, Player> killers =  new WeakHashMap<Player, Player>();
 	private Main plugin;
 	public MainListener(Main i) {plugin = i; }
 	
 	//register new player
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		Player player = event.getPlayer();
 		String pname = player.getName();
 		DataHandler data = plugin.getDB();
 		if(!data.isset(pname))
 		{
 			data.setValue(pname, 0, 0, 0);
 		}
 	}
 	
 	//set players level in chat
 	@EventHandler (priority = EventPriority.HIGHEST)
 	public void onPlayerChat(AsyncPlayerChatEvent event)
 	{
 		Player player = event.getPlayer();
 		PvpPlayer pp = new PvpPlayer(player);
 		String format = event.getFormat();
 		String lvl = String.valueOf(pp.getLevel());
 		if(format.contains("{Lvl}"))
 			format = format.replace("{Lvl}", lvl);
 		else
 			format = "["+ChatColor.GREEN+"Lvl "+lvl+ChatColor.WHITE+"] "+format;
 		event.setFormat(format);
 	}
 	
 	//trigger unconfirmed death
 	@EventHandler (priority = EventPriority.HIGHEST)
 	public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
 	{
 		if(event.isCancelled())
 			return;
 		
 		Entity defender = event.getEntity(), attacker = event.getDamager();
 		int damage = event.getDamage();
 		if(defender instanceof Player && attacker instanceof Player)
 		{
 			Player p1 = (Player)defender;
 			Player p2 = (Player)attacker;
 			if(p1.getHealth() - damage <= 0)
 			{
 				if(killers.containsKey(p1))
 				{
 					if(killers.get(p1) != p2)
 					{
 						killers.put(p1, p2);
 					}
 				}else
 					killers.put(p1, p2);
 			}
 		}
 	}
 	
 	//confirm death
 	@EventHandler
 	public void onEntityDeath(EntityDeathEvent event)
 	{
 		Entity entity = event.getEntity();
 		
 		if(entity instanceof Player)
 		{
 			Player p1 = (Player)entity;
 			if(killers.containsKey(p1))
 			{
 				Player p2 = killers.get(p1);
 				if(p2.isOnline())
 				{
 					//Player is death and killer is online
 					PvpPlayer pp = new PvpPlayer(p2);
 					if(pp.getLevel() <= plugin.getConfig().getInt("settings.max lvl"))
 					{
 						pp.setKills(pp.getKills() + 1);
 						if(plugin.levels.contains(pp.getKills()))
 						{
 							p2.sendMessage(ChatColor.GREEN+"Level up!");
 							pp.setLevel(pp.getLevel() + 1);
 							this.sendRewards(p2);
 						}
 					}
 				}
 				killers.remove(p1);
 			}
 		}
 	}
 	
 	//send rewards to player
 	public void sendRewards(Player player)
 	{
 		PvpPlayer pp =  new PvpPlayer(player);
 		String lvl = String.valueOf(pp.getLevel());
 		if(plugin.getConfig().getString("settings.reward.lvl."+lvl+".command") != null)
 		{
 			String cmd = plugin.getConfig().getString("settings.reward.lvl."+lvl+".command");
			cmd = cmd.replace("{Player}", player.getName());
 			CommandSender sender = (CommandSender)Bukkit.getServer().getConsoleSender();
 			Bukkit.getServer().dispatchCommand(sender, cmd);
 		}
 		if(plugin.getConfig().getBoolean("settings.reward.item.use"))
 		{
 			int id = plugin.getConfig().getInt("settings.reward.item.id");
 			int amount = plugin.getConfig().getInt("settings.reward.item.amount");
 			ItemStack it = new ItemStack(Material.getMaterial(id), amount);
 			
 			player.getInventory().addItem(it);
 			player.sendMessage(ChatColor.GREEN+"You gained "+String.valueOf(amount)+" of " + it.getType().toString().toLowerCase());
 		}
 		if(plugin.economy != null)
 		{
 			if(plugin.getConfig().getBoolean("settings.reward.money.use"))
 			{
 				double amount = plugin.getConfig().getInt("settings.reward.money.amount");
 				plugin.economy.depositPlayer(player.getName(), amount);
 				
 				player.sendMessage(ChatColor.GREEN+"You gained $"+String.valueOf(amount));
 			}
 		}
 		if(plugin.getConfig().getBoolean("settings.reward.command.use"))
 		{
 			String cmd = plugin.getConfig().getString("settings.reward.command.command");
			cmd = cmd.replace("{Player}", player.getName());
 			
 			CommandSender sender = (CommandSender)Bukkit.getServer().getConsoleSender();
 			Bukkit.getServer().dispatchCommand(sender, cmd);
 		}
 	}
 }
