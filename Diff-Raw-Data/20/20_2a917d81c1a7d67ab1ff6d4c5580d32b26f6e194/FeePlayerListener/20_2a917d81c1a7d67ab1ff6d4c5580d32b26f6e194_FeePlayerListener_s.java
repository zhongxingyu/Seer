 package org.melonbrew.fee;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class FeePlayerListener implements Listener {
 	private final Fee plugin;
 	
 	public FeePlayerListener(Fee plugin){
 		this.plugin = plugin;
 		
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 	
 	@EventHandler
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
 		Player player = event.getPlayer();
 		
 		String key = plugin.getKey(event.getMessage());
 		
 		if (key != null){
 			double money = plugin.getKeyMoney(key);
 			
 			if (!plugin.getEconomy().has(player.getName(), money)){
				player.sendMessage(Phrase.DOES_NOT_HAVE_ENOUGH_MONEY.parseWithPrefix());

 				return;
 			}
 
 			plugin.addCommand(player, key);
 
 			player.sendMessage(Phrase.COMMAND_WILL_COST.parseWithPrefix(plugin.getEconomy().format(money)));
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event){
 		removeCommand(event.getPlayer());
 	}
 	
 	@EventHandler
 	public void onPlayerKick(PlayerKickEvent event){
 		removeCommand(event.getPlayer());
 	}
 	
 	private void removeCommand(Player player){
 		plugin.removeCommand(player);
 	}
 }
