 package coolawesomeme.basics_plugin;
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import coolawesomeme.basics_plugin.commands.BrbCommand;
 import coolawesomeme.basics_plugin.commands.ServerHelpCommand;
 
 public class EventListener implements Listener{
 	
 	@EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
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
     			event.getPlayer().sendMessage("Welcome to the " + Basics.serverName + ", " + event.getPlayer().getDisplayName() + "!");
     			event.getPlayer().sendMessage("");
     			event.getPlayer().sendMessage(MinecraftColors.lightRed + "Server is currently in BRB mode because the server owner is brbing!");
     		}
     	}else{
    		event.getPlayer().sendMessage("Welcome to the " + Basics.serverName + ", " + event.getPlayer().getDisplayName() + "!");
         }
     }
 	
 }
