 package net.LoadingChunks.SyncingFeeling.Inventory;
 
 import java.util.HashMap;
import java.util.Map;
 import java.util.Map.Entry;
 
 import net.LoadingChunks.SyncingFeeling.util.SQLWrapper;
 
import org.bukkit.craftbukkit.libs.joptsimple.util.KeyValuePair;
import org.bukkit.craftbukkit.v1_5_R3.inventory.CraftInventory;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class SerializableInventory {
 	private HashMap<Integer, ItemStack> slots = new HashMap<Integer, ItemStack>();
 	private Player player;
 	
 	public static SerializableInventory fromInventory(Player p, PlayerInventory inv) {
 		SerializableInventory ret = new SerializableInventory();
 		ret.player = p;
 		Integer i = 0;
 		for(ItemStack stack : inv.getContents()) {
 			ret.addStack(i, stack);
 			i++;
 		}
 		return ret;
 	}
 	
 	void addStack(Integer i, ItemStack stack) {
 		if(stack != null)
 			this.slots.put(i, stack);
 	}
 	
 	public void commit() {
 		this.commitInventory(true);
 		for(Entry<Integer, ItemStack> item : slots.entrySet()) {
 			SQLWrapper.commitSlot(this.player, item.getKey(), item.getValue());
 		}
 	}
 	
 	public void commitInventory(boolean clear) {
 		SQLWrapper.commitInventory(this, player, clear);
 	}
 	
 	public HashMap<Integer, Object> serialize() {
 		HashMap<Integer, Object> inv = new HashMap<Integer, Object>();
 		
 		for(Entry<Integer, ItemStack> stack : slots.entrySet()) {
 			inv.put(stack.getKey(), stack.getValue().serialize());
 		}
 		
 		return inv;
 	}
 	
 	public static void recover(Player p) {
 		String latest = SQLWrapper.checkLatest(p);
 		if(!latest.equalsIgnoreCase(SQLWrapper.getPlugin().getConfig().getString("general.server.name"))) {
 			SQLWrapper.recoverLatest(p, latest);
 		}
 	}
 }
