 package listeners;
 
 import net.illusiononline.factioncore.FactionCore;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 
 public class EntityListener implements Listener{
 	
 	FactionCore plugin;
 	
 	public EntityListener(FactionCore plugin) {
 		this.plugin = plugin;
 	}
 	
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
 		if (event.isCancelled()) return;
 		Player player_e = null;
 		Player player_d = null;
 		if (event.getEntity() instanceof Player) player_e = (Player) event.getEntity();
 		if (event.getDamager() instanceof Player) player_d = (Player) event.getDamager();
 		if (player_e == null || player_d == null) return;
 		
 		//Future zone for PvP-Zone check
 		
 		String faction1 = FactionCore.getSqlManager().getFactionbyMember(player_e.getName());
 		String faction2 = FactionCore.getSqlManager().getFactionbyMember(player_d.getName());
 		
 		if (faction1.equalsIgnoreCase(faction2)){
 			player_d.sendMessage(ChatColor.RED+"You can't attack someone in your faction!");
 			event.setCancelled(true);
 			return;
 		} else if (FactionCore.getFactionManager().isFactionAlly(faction1, faction2)){
 			player_d.sendMessage(ChatColor.RED+"You can't attack a faction ally!");
 			event.setCancelled(true);
 			return;
 		}
 	}
 }
