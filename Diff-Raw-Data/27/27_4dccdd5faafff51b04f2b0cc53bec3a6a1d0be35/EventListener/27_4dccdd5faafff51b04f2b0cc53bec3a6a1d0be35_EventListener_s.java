 package coolawesomeme.basics_plugin;
 
import java.util.Calendar;

 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import coolawesomeme.basics_plugin.commands.BrbCommand;
 import coolawesomeme.basics_plugin.commands.ServerHelpCommand;
 import coolawesomeme.basics_plugin.commands.TagCommand;
 
 public class EventListener implements Listener{
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerJoin(PlayerJoinEvent event) {
 		if(Basics.getServerThreatLevel() != ThreatLevel.NULL){
 			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "kick " + event.getPlayer().getName() + " Server is in lockdown mode!");
 		}
     }
 	
 	@EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerJoinLower(PlayerJoinEvent event) {
 		PlayerDataStorage.makePlayerDataFile(event.getPlayer().getName());
 		if(BrbCommand.isOwnerBRBing){
 			if(!event.getPlayer().hasPlayedBefore()){
 				final PlayerJoinEvent newEvent = event;
 				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(new Basics(), new Runnable() {
 					@Override 
 					public void run() {
 						ServerHelpCommand.actualServerHelp(newEvent.getPlayer());
 						newEvent.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "Server is currently in BRB mode because the server owner is brbing!");
 					}
 				}, 20L);
 			}else{
 				event.getPlayer().sendMessage("Welcome to " + Bukkit.getServerName() + ", " + event.getPlayer().getName() + "!");
 				event.getPlayer().sendMessage("");
 				event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "Server is currently in BRB mode because the server owner is brbing!");
 			}
 		}else{
 			event.getPlayer().sendMessage("Welcome to " + Bukkit.getServerName() + ", " + event.getPlayer().getName() + "!");
     	}
 	}
 	
 	@EventHandler
	public void onPlayerInteractWithEntity(PlayerInteractEntityEvent event){
		if(TagCommand.isTagOn){
			if(TagCommand.getTaggedPlayers().contains(event.getPlayer()) && TagCommand.getNonTaggedPlayers().contains(event.getRightClicked())){
				TagCommand.tagPlayer((Player)event.getRightClicked());
			}
		}
	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event){
 		if(Basics.disallowGamemode && !event.getPlayer().hasPermission("basics.gamemode_ban.change")){
 			if(event.getMessage().toLowerCase().startsWith("/gamemode") || event.getMessage().toLowerCase().startsWith("/gm") || event.getMessage().toLowerCase().startsWith("gamemode") || event.getMessage().toLowerCase().startsWith("gm")){
 				event.setCancelled(true);
 				event.getPlayer().sendMessage(ChatColor.RED + "Sorry, this server has disabled the changing of gamemodes by players.");
 				event.getPlayer().sendMessage(ChatColor.RED + "Only the Console may change players' gamemodes.");
 			}
 		}
 	}
 }
