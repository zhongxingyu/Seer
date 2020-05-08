 package net.worldoftomorrow.nala.ni.tasks;
 
 import java.util.Collection;
 
 import net.worldoftomorrow.nala.ni.EventTypes;
 import net.worldoftomorrow.nala.ni.Perms;
 import net.worldoftomorrow.nala.ni.StringHelper;
 import net.worldoftomorrow.nala.ni.CustomItems.CustomWorkbench;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 
 public class NoCraftTask implements Runnable {
 	private final Player p;
 	private final InventoryView view;
 	private final CustomWorkbench cw;
 	private final short clicked;
 
 	public NoCraftTask(Player p, int clicked, CustomWorkbench cw) {
 		this.p = p;
 		this.view = p.getOpenInventory();
 		this.cw = cw;
 		this.clicked = (short) clicked;
 	}
 
 	public void run() {
 		for(Short rs : cw.getResultSlots()) {
 			ItemStack result = view.getItem(rs);
 			if(result != null && Perms.NOCRAFT.has(p, result)) {
 				ItemStack cstack = view.getItem(clicked);
 				if(cstack != null) {
 					ItemStack give = new ItemStack(cstack);
 					cstack = null;
 					Collection<ItemStack> left = p.getInventory().addItem(give).values();
 					if(!left.isEmpty()) {
 						p.getWorld().dropItem(p.getLocation(), left.iterator().next());
 					}
 				} else {
 					for(Short s : cw.getRecipeSlots()) {
 						ItemStack item = view.getItem(s);
 						if(item != null) {
							ItemStack give = new ItemStack(cstack);
 							item = null;
 							Collection<ItemStack> left = p.getInventory().addItem(give).values();
 							if(!left.isEmpty()) {
 								p.getWorld().dropItem(p.getLocation(), left.iterator().next());
 							}
 						}
 					}
 				}
 				StringHelper.notifyPlayer(p, EventTypes.CRAFT, result);
 				StringHelper.notifyAdmin(p, EventTypes.CRAFT, result);
 				return;
 			}
 		}
 	}
 
 }
