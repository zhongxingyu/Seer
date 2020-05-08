 package au.com.addstar.pandora.modules;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityPortalEnterEvent;
 import org.bukkit.event.entity.EntityPortalExitEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 
 import au.com.addstar.pandora.MasterPlugin;
 import au.com.addstar.pandora.Module;
 
 public class AntiPortalTrap implements Module, Listener{
 	
 	private MasterPlugin mPlugin;
 	private Map<Player, Location> portalPlayers;
 	
 	public AntiPortalTrap(){
 		portalPlayers = new HashMap<Player, Location>();
 	}
 
 	@Override
 	public void onEnable() {
 	}
 
 	@Override
 	public void onDisable() {
 	}
 
 	@Override
 	public void setPandoraInstance(MasterPlugin plugin) {
 		mPlugin = plugin;
 	}
 	
 	@EventHandler
 	private void playerLogin(PlayerLoginEvent event){
 		final Player ply = event.getPlayer();
 		final Location pos = Bukkit.getServer().getWorlds().get(0).getSpawnLocation();
 		Bukkit.getScheduler().scheduleSyncDelayedTask(mPlugin, new Runnable() {
 			
 			@Override
 			public void run() {
 				portalPlayers.put(ply, pos);
 				Bukkit.getScheduler().scheduleSyncDelayedTask(mPlugin, new Runnable() {
 					
 					@Override
 					public void run() {
 						portalPlayers.remove(ply);
 					}
 				}, 20);
 			}
 		}, 200);
 	}
 	
 	@EventHandler
 	private void playerEnterPortal(EntityPortalEnterEvent event){
 		if(event.getEntity() instanceof Player){
 			Player ply = (Player)event.getEntity();
 			if(portalPlayers.containsKey(ply)){
 				ply.teleport(portalPlayers.get(ply));
				portalPlayers.remove(ply);
 			}
 		}
 	}
 	
 	@EventHandler
 	private void playerExitPortal(EntityPortalExitEvent event){
 		if(event.getEntity() instanceof Player){
 			final Player ply = (Player)event.getEntity();
 			final Location pos = ply.getLocation().clone().add(0, 1, 0);
 			Bukkit.getScheduler().scheduleSyncDelayedTask(mPlugin, new Runnable() {
 				
 				@Override
 				public void run() {
 					portalPlayers.put(ply, pos);
 					Bukkit.getScheduler().scheduleSyncDelayedTask(mPlugin, new Runnable() {
 						
 						@Override
 						public void run() {
 							portalPlayers.remove(ply);
 						}
 					}, 20);
 				}
 			}, 200);
 		}
 	}
 }
