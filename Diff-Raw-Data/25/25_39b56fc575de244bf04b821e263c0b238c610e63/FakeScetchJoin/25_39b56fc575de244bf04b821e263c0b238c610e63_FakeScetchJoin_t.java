 package net.nocheatplus.fakescetchjoin;
  
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.java.JavaPlugin;
  
 public final class FakeScetchJoin extends JavaPlugin implements Listener {
 
 	@Override
	public void onEnable(){
 		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("FakeScetchJoin enabled!");
	}
 
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent evt) { 
		getServer().broadcastMessage(ChatColor.YELLOW + "Scetch joined the game");
	}
  
	@Override
	public void onDisable() {
		getLogger().info("FakeScetchJoin disabled!");
	}
 }
