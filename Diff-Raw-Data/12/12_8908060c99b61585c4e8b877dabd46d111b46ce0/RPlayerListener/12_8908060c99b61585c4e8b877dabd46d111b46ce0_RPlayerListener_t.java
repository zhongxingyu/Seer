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
		if(event.getPlayer().hasPermission("research")){
			Tech t = TechManager.getCurrentResearch(event.getPlayer());
			if(t == null){
				t = new Tech();
				t.name = "None";
			}
			event.getPlayer().sendMessage("You currently know " + TechManager.getAvailableTech(event.getPlayer()).size() + "technologies" +
        		" and are currently researching " + t.name + ".");
		}
     }
 }
