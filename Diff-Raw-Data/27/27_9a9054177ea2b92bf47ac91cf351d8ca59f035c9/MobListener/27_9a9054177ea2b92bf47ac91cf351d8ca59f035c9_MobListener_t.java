 package me.bluejelly.main.listeners;
 
 import me.bluejelly.main.GuildZ;
import me.bluejelly.main.configs.GuildConfig;
import me.bluejelly.main.configs.PlayerConfig;
 
import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 public class MobListener implements Listener {
 
 	static GuildZ main;
 	
 	public MobListener(GuildZ instance)
 	{
 		main = instance;
 	}
 	
 	@EventHandler(ignoreCancelled=true)
 	public void onMobDeath(EntityDeathEvent event)
 	{
		Player player;
		if(event.getEntity().getKiller() instanceof Player) {
			player = (Player) event.getEntity().getKiller();
		} else {return;}
 		
		if(!PlayerConfig.config.contains(player.getName())) {
			return;
 		}
 		
		int guildID = PlayerConfig.config.getInt(player.getName() + ".guildID");
		int guildLevel = -1;
		guildLevel = GuildConfig.config.getInt(guildID + ".level");
		
		if(guildLevel == -1) {return;}
		
		long XP = event.getDroppedExp();
		
		event.setDroppedExp( (int) (XP+(XP/20*(guildLevel*3))));
		
 	}
 }
