 package net.croxis.plugins.research;
 
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 
 public class RPlayerListener extends PlayerListener{
 	private Research plugin;
 	
 	public RPlayerListener(Research plugin) {
 		super();
 		this.plugin = plugin;
 	}
 
 	@Override
     public void onPlayerJoin(PlayerJoinEvent event) {
		if(event.getPlayer().hasPermission("research"))
			event.getPlayer().sendMessage("You currently know " + TechManager.getAvailableTech(event.getPlayer()) + "technologies" +
        		" and are currently researching " + TechManager.getCurrentResearch(event.getPlayer()).name + ".");
     }
 }
