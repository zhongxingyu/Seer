 package JBCod;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Egg;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class CarePackageHandler {
 	@EventHandler
 	public void onThrow(PlayerEggThrowEvent e) {
 		e.setHatching(false);
 	}
 
 	@EventHandler
 	public void onHit(ProjectileHitEvent event) {
 		if (event.getEntity() instanceof Egg) {
 			Player p = (Player) event.getEntity().getShooter();
 			Location loc = event.getEntity().getLocation();
 			loc.setY(event.getEntity().getLocation().getY());
 			loc.setX(event.getEntity().getLocation().getX());
 			loc.setZ(event.getEntity().getLocation().getZ());
 			carePackage(p);
 		}
 	}
 
 	public void carePackage(Player pl) {
 		Player p = pl.getPlayer();
 		Location loc = p.getLocation();
 		loc.setY(p.getLocation().getY() + 20);
 		Byte blockData = 0x0;
 		Chest c = (Chest) loc.getBlock();
 		p.getWorld().spawnFallingBlock(loc, Material.CHEST, blockData);
 		loc.setX(p.getLocation().getX() + 1);
 		ItemStack ammo = new ItemStack(Material.GHAST_TEAR, 64);
 		Inventory inv = c.getInventory();
 		inv.addItem(ammo);
 		c.update();
 
 	}
 }
