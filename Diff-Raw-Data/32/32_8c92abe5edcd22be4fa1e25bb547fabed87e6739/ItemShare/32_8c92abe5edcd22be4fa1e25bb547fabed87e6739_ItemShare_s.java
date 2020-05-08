 package be.infogroep.optalk;
 
 //import java.util.Map;
 
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.*;
 import org.bukkit.inventory.*;
 import org.bukkit.inventory.meta.*;
 
 public class ItemShare {
 
 	private int header = 4;
 
 	private Player receiver_ = null;
 	private Player sender_;
 	private ItemStack item_;
 	private ItemMeta meta_;
 	private String itemName_;
 
 	ItemShare(CommandSender sender, String[] msg) {
 		Bukkit.getLogger().info("ItemShare");
 
 		if (msg.length > 0) {
 			String name = msg[0];
 			receiver_ = Bukkit.getServer().getPlayer(name);
 		}
 
 		sender_ = (Player) sender;
 		item_ = sender_.getItemInHand();
 
 		meta_ = item_.getItemMeta();
 
 		if (meta_ != null) {
 			if (meta_.hasDisplayName())
 				itemName_ = meta_.getDisplayName();
 			else
 				itemName_ = item_.getType().name();
 		} else
 			itemName_ = item_.getType().name();
 
 		Map<Enchantment, Integer> enchants = item_.getEnchantments();
 		int EnchantsSize = enchants.size();
		
//		Recipe R = ShapedRecipe(item_);
 
		String[] message = new String[header + EnchantsSize];
		message[0] = "§5" + sender.getName() + ":§6shared an item";
		message[1] = "§aItem name: §6" + itemName_;
		message[2] = "§aType: " + item_.getType().name();
 		message[3] = "§aEnchanments: ";
 
 		int index = 4;
 
 		if (EnchantsSize > 0) {
 
 			for (Map.Entry<Enchantment, Integer> current : enchants.entrySet()) {
 				message[index] = current.getKey().getName() + " Level: "
 						+ current.getValue();
 				index = index + 1;
 			}
 		} else {
 			message[index] = "No Enchantments";
 			index = index + 1;
 		}
 
 		if (null == receiver_) {
			for (String s : message) {
				Bukkit.getServer().broadcastMessage(s);
 			}
 		} else {
 			receiver_.sendMessage(message);
			receiver_.openWorkbench(null, true);
 		}
 	}
 }
