 package coolawesomeme.basics_plugin;
 
 import org.bukkit.Bukkit;
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
 					}
 				}, 20L);
 			}else{
 				event.getPlayer().sendMessage("Welcome to the " + Bukkit.getServerName() + ", " + event.getPlayer().getDisplayName() + "!");
 				event.getPlayer().sendMessage("");
 				event.getPlayer().sendMessage(MinecraftColors.red + "Server is currently in BRB mode because the server owner is brbing!");
 				event.getPlayer().sendMessage(Basics.message);
 			}
 		}else{
 			event.getPlayer().sendMessage("Welcome to the " + Bukkit.getServerName() + ", " + event.getPlayer().getDisplayName() + "!");
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
 		if(Basics.disallowGamemode && !event.getPlayer().hasPermission("basics.gamemode.change")){
			if(event.getMessage().toLowerCase().startsWith("/gamemode") || event.getMessage().toLowerCase().startsWith("/gm") || event.getMessage().toLowerCase().startsWith("gamemode") || event.getMessage().toLowerCase().startsWith("gm")){
 				event.setCancelled(true);
 				event.getPlayer().sendMessage(MinecraftColors.red + "Sorry, this server has disabled the changing of gamemodes by players.");
 				event.getPlayer().sendMessage(MinecraftColors.red + "Only the Console may change gamemodes.");
 			}
 		}
 	}
 }
