 package graindcafe.tribu.Player;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class BeforeGamePlayerState {
 	Player p;
 	double health;
 	int foodLevel;
 	float exp;
 	int level;
 	Location point;
 	Location bed;
 	List<ItemStack> inventories = null;
 	List<ItemStack> armors = null;
 
 	public BeforeGamePlayerState(Player p, boolean inventory) {
 		this.p = p;
 		health = p.getHealth();
 		foodLevel = p.getFoodLevel();
 		exp = p.getExp();
 		level = p.getLevel();
 		point = p.getLocation();
 		bed = p.getBedSpawnLocation();
 		if (inventory)
 			addInventory();
 	}
 
 	public void restore() {
 		restore(true, true, true, true, true);
 	}
 
 	public void restore(boolean location, boolean bedLoc, boolean hp,
 			boolean hunger, boolean xp) {
 		if (hp)
 			p.setHealth(health);
 		if (hunger)
 			p.setFoodLevel(foodLevel);
 		if (xp) {
 			p.setLevel(level);
 			p.setExp(exp);
 		}
 		if (location)
 			p.teleport(point);
 		if (bedLoc)
 			p.setBedSpawnLocation(bed, true);
 		restoreInventory();
 	}
 
 	public void resetBedSpawn() {
 		p.setBedSpawnLocation(bed, true);
 	}
 
 	public void addInventory() {
 		final PlayerInventory pInv = p.getInventory();
 
 		inventories = Arrays.asList(pInv.getContents().clone());
 		armors = Arrays.asList(pInv.getArmorContents().clone());
 		p.getInventory().clear();
 	}
 
	@SuppressWarnings("deprecation")
 	public void restoreInventory() {
 		if (inventories != null)
 			uncheckedRestoreInventory();
 		if (armors != null)
 			uncheckedRestoreArmor();
		p.updateInventory();
 	}
 
 	protected void uncheckedRestoreArmor() {
 		p.getInventory().setArmorContents((ItemStack[]) armors.toArray());
 	}
 
 	protected void uncheckedRestoreInventory() {
 		p.getInventory().setContents((ItemStack[]) inventories.toArray());
 	}
 }
