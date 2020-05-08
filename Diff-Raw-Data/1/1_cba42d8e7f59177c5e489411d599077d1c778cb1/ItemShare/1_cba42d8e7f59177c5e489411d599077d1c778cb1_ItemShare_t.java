 package be.infogroep.optalk;
 
 //import java.util.Map;
 
 import java.util.*;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.*;
 import org.bukkit.inventory.*;
 import org.bukkit.inventory.meta.*;
 import org.bukkit.potion.*;
 
 
 public class ItemShare {
 
 	private int header = 4;
 	private boolean potion = false;
 
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
 			itemName_ = // item_.getType().name();
 			item_.getData().getClass().getName();
 
 		Map<Enchantment, Integer> enchants = item_.getEnchantments();
 		Collection<PotionEffect> effects = null;
 
 		Material material_ = item_.getType();
 
 		if (material_.getId() == 387) {// special case book
 			BookMeta BM = (BookMeta) meta_;
 			itemName_ = BM.getTitle() + " - " + BM.getAuthor();
 
 		}
 		if (material_.getId() == 403) {// special case enchanted book
 			EnchantmentStorageMeta ESM = (EnchantmentStorageMeta) meta_;
 			enchants = ESM.getStoredEnchants();
 		}
 
 		if (material_.getId() == 373) {// special case potions
			potion = true;
 			PotionMeta PM = (PotionMeta) meta_;
 //			if (PM.hasDisplayName())
 //				itemName_ = PM.getDisplayName();
 //			effects = PM.getCustomEffects();
 		Potion p = (Potion) PM;
 		effects = p.getEffects();
 
 		}
 
 		// Recipe R = ShapedRecipe(item_);
 
 		int EnchantsSize = enchants.size();
 
 		int EnchantsHeaderSize = 1;
 		if (EnchantsSize > 0)
 			EnchantsHeaderSize = EnchantsSize;
 		else if (effects.size() != 0)
 			EnchantsHeaderSize = effects.size();
 
 		String[] message = new String[header + EnchantsHeaderSize];
 		message[0] = "§5" + sender.getName() + ": §6shared an item";
 		message[1] = "§aItem name: §b" + itemName_;
 		message[2] = "§aType: §b"
 				+ material_.name().replace("_", " ").toLowerCase();
 		if (potion)
 			message[3] = "§aEffect: ";
 		else
 			message[3] = "§aEnchanments: ";
 
 		int index = 4;
 
 		if (EnchantsSize > 0) {
 
 			for (Map.Entry<Enchantment, Integer> current : enchants.entrySet()) {
 				message[index] = current.getKey().getName() + " Level: "
 						+ current.getValue();
 				index = index + 1;
 			}
 		} else if (!effects.isEmpty()) {
 			for(PotionEffect P: effects){
 				message[index] = P.getType().getName() + ": " + P.getDuration();
 			}
 		} else {
 			message[index] = "None";
 			index = index + 1;
 		}
 
 		if (null == receiver_) {
 			if (msg.length == 0) {
 				Bukkit.getServer().broadcastMessage("§a§nPublicly shared item");
 				for (String s : message) {
 					Bukkit.getServer().broadcastMessage(s);
 				}
 
 			} else {
 				sender.sendMessage("§ccould not find player: " + msg[0]);
 			}
 		} else {
 			receiver_.sendMessage(message);
 			sender.sendMessage("§asuccesfully shared your item with " + msg[0]);
 		}
 	}
 }
