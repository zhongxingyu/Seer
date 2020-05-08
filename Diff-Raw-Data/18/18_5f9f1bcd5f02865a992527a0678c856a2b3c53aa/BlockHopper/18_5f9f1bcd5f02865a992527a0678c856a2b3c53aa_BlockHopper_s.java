 package lekro.bukkitplugins.blockhopper;
 
 import org.bukkit.Material;
 import org.bukkit.block.BlockFace;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BlockHopper extends JavaPlugin implements Listener {
 	boolean detectContainers;
 	public void onEnable() {
 		getLogger().info("[BlockHopper] Blocking your Hopper placement now (until Lockette/Deadbolt implements it!) -Lekro");
 		getServer().getPluginManager().registerEvents(this,this);
 		try {
 			detectContainers = (boolean) this.getConfig().get("detect-containers");
 		} catch (NullPointerException e) {
 			this.getConfig().set("detect-containers", true);
 			this.saveConfig();
 			detectContainers = true;
 		}
 	}
 	@EventHandler
 	public void hopperPlaced(BlockPlaceEvent event) {
 		if (event.getBlock().getType().equals(Material.HOPPER)) {
 			
 			if (detectContainers) {
 				Material[] containers = {Material.CHEST, Material.DISPENSER, Material.FURNACE, Material.BURNING_FURNACE}; 
 				for (byte i = 0; i < containers.length; i++) {
 					if (event.getBlock().getRelative(BlockFace.UP).getType().equals(containers[i])) {
 						getServer().broadcastMessage("a[2BlockHoppera]r " + event.getPlayer().getDisplayName() + "c tried to use a Hopper under a container, but was stopped!");
 						event.setCancelled(true);
 					}
 				}
 			} else {
 				getLogger().info(event.getPlayer().getDisplayName() + " placed a Hopper! Blocking!");
 				getServer().broadcastMessage("a[2BlockHoppera]r " + event.getPlayer().getDisplayName() + "c tried to use a Hopper, but was stopped!");
 				event.setCancelled(true);
 			}
 		}
 	}
 }
