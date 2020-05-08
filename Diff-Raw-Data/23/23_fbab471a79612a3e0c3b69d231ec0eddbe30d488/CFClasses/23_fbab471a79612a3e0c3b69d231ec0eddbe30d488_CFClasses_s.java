 package org.github.craftfortress2;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
public class CFClasses implements Listener{
 	static ArrayList<PlayerInventory> playerInventories = new ArrayList<PlayerInventory>();
	static ArrayList<Player> players = new ArrayList<Player>(); // Maybe use the one in CFCE later?
 	@EventHandler
 	public void onFoodBarLower(FoodLevelChangeEvent event){
			Player player = ((Player) event).getPlayer();
 				player.setExhaustion(0);
 				player.setFoodLevel(17);
	}
 	public static void saveInv(Player player){
 		PlayerInventory inv = player.getInventory();
 		playerInventories.add(inv);
 		players.add(player);
 	}
 	public static void loadInv(Player player){
 		PlayerInventory inv = getInv(player);
		ItemStack[] is = inv.getContents();
		inv.setContents(is);
 	}
 	public static PlayerInventory getInv(Player player){
 		int index = players.lastIndexOf(player);
 		if (index == -1){
 			return null;
 		}
 		return playerInventories.get(index);
 	}
 }
