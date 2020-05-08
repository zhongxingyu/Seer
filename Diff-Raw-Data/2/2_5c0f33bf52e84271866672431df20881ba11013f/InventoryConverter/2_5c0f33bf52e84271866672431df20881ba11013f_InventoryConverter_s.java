 package com.gmail.br45entei.enteisinvmanager;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 import com.gmail.br45entei.enteispluginlib.EPLib;
 
 public class InventoryConverter {
 	public static String convertSymbolsForSaving(String str) {
 		str = str.replaceAll(";", "<semi-colon>")
 			.replaceAll("#", "<nunmeral-sign>")
 			.replaceAll(":", "<colon>")
 			.replaceAll("@", "<at-sign>")
 			.replaceAll("~", "<tilde>")
 			.replaceAll("(?i)\u00A7b","&b")
 			.replaceAll("\u00A70","&0")
 			.replaceAll("\u00A79","&9")
 			.replaceAll("(?i)\u00A7l","&l")
 			.replaceAll("\u00A73","&3")
 			.replaceAll("\u00A71","&1")
 			.replaceAll("\u00A78","&8")
 			.replaceAll("\u00A72","&2")
 			.replaceAll("\u00A75","&5")
 			.replaceAll("\u00A74","&4")
 			.replaceAll("\u00A76","&6")
 			.replaceAll("\u00A77","&7")
 			.replaceAll("(?i)\u00A7a","&a")
 			.replaceAll("(?i)\u00A7o","&o")
 			.replaceAll("(?i)\u00A7d","&d")
 			.replaceAll("(?i)\u00A7k","&k")
 			.replaceAll("(?i)\u00A7c","&c")
 			.replaceAll("(?i)\u00A7m","&m")
 			.replaceAll("(?i)\u00A7n","&n")
 			.replaceAll("(?i)\u00A7f","&f")
 			.replaceAll("(?i)\u00A7e","&e")
 			.replaceAll("(?i)\u00A7r","&r");
 		return str;
 	}
 	public static String convertSymbolsForLoading(String str) {
 		str = str.replaceAll("<semi-colon>", ";")
 			.replaceAll("<numeral-sign>", "#")
 			.replaceAll("<colon>", ":")
 			.replaceAll("<at-sign>", "@")
 			.replaceAll("<tilde>", "~")
 			.replaceAll("(?i)&0",EPLib.black+"")
 			.replaceAll("(?i)&9",EPLib.blue+"")
 			.replaceAll("(?i)&l",EPLib.bold+"")
 			.replaceAll("(?i)&3",EPLib.daqua+"")
 			.replaceAll("(?i)&1",EPLib.dblue+"")
 			.replaceAll("(?i)&8",EPLib.dgray+"")
 			.replaceAll("(?i)&2",EPLib.dgreen+"")
 			.replaceAll("(?i)&5",EPLib.dpurple+"")
 			.replaceAll("(?i)&4",EPLib.dred+"")
 			.replaceAll("(?i)&6",EPLib.gold+"")
 			.replaceAll("(?i)&7",EPLib.gray+"")
 			.replaceAll("(?i)&a",EPLib.green+"")
 			.replaceAll("(?i)&o",EPLib.italic+"")
 			.replaceAll("(?i)&d",EPLib.lpurple+"")
 			.replaceAll("(?i)&k",EPLib.magic+"")
 			.replaceAll("(?i)&c",EPLib.red+"")
 			.replaceAll("(?i)&m",EPLib.striken+"")
 			.replaceAll("(?i)&n",EPLib.underline+"")
 			.replaceAll("(?i)&f",EPLib.white+"")
 			.replaceAll("(?i)&e",EPLib.yellow+"")
 			.replaceAll("(?i)&r",EPLib.reset+"");
 		return str;
 	}
 	public static String serializeInventory(Inventory inv) {
 		/*Serialization tags(for easy readability):
 		 * t@ = Item Type
 		 * d@ = Item Durability
 		 * a@ = Item Amount
 		 * e@ = Item Enchantment
 		 * l@ = Item Lore
 		 * n@ = Item Name*/
 		String serialization = inv.getSize() + ";" + inv.getTitle() + ";";
 		for(int i = 0; i < inv.getSize(); i++) {
 			ItemStack is = inv.getItem(i);
 			if(is != null) {
 				String serializedItemStack = new String();
 				String isType = String.valueOf(is.getType().getId());
 				serializedItemStack += "t@" + isType;
 				if(is.getDurability() != 0) {
 					String isDurability = String.valueOf(is.getDurability());
 					serializedItemStack += ":d@" + isDurability;
 				}
 				if(is.getAmount() != 1) {
 					String isAmount = String.valueOf(is.getAmount());
 					serializedItemStack += ":a@" + isAmount;
 				}
 				Map<Enchantment,Integer> isEnch = is.getEnchantments();
 				if(isEnch.size() > 0) {
 					for(Entry<Enchantment,Integer> ench : isEnch.entrySet()) {
 						serializedItemStack += ":e@" + ench.getKey().getId() + "@" + ench.getValue();
 					}
 				}
 				if(is.getItemMeta().hasDisplayName()) {
 					serializedItemStack += ":n@" + convertSymbolsForSaving(is.getItemMeta().getDisplayName());
 				}
 				if(is.getItemMeta().hasLore()) {
 					Iterator<String> it = is.getItemMeta().getLore().iterator();
 					String lores = "";
 					while(it.hasNext()) {
 						lores += "~" + convertSymbolsForSaving(it.next());
 					}
 					serializedItemStack += ":l@" + lores;
 				}
 				serialization += i + "#" + serializedItemStack + ";";
 			}
 		}
 		return serialization;
 	}
 	public static String InventoryToString(Player player, String invToConvert) {
 		Inventory invInventory = Bukkit.getServer().createInventory(player, InventoryType.PLAYER);
 		if(invToConvert.equalsIgnoreCase("inventory")) {
 			invInventory = player.getInventory();
 		} else if(invToConvert.equalsIgnoreCase("armor")) {
 			Inventory newInv = Bukkit.getServer().createInventory(player, 9);
 			int num = 0;
 			for(ItemStack curItem : player.getInventory().getArmorContents()) {
 				newInv.setItem(num, curItem);num++;
 			}
 			invInventory = newInv;
 		} else if(invToConvert.equalsIgnoreCase("enderchest")) {
 			invInventory = player.getEnderChest();
 		}
 		return serializeInventory(invInventory);
 	}
 	@SuppressWarnings("boxing")
 	public static Inventory StringToInventory(String invString, Player player) {
 		if(invString != null) {
 			if(invString.equals("") == false) {
 				String[] serializedBlocks = invString.split(";");
 				//String invInfo = serializedBlocks[0];
 				//Inventory deserializedInventory = Bukkit.getServer().createInventory(player, invType);
 				//Inventory deserializedInventory = Bukkit.getServer().createInventory(player, Integer.valueOf(invInfo));
 				Inventory deserializedInventory = Bukkit.getServer().createInventory(player, Integer.valueOf(serializedBlocks[0]), String.valueOf(serializedBlocks[1]));
 				//for(int i = 1; i < serializedBlocks.length; i++) {
 				for(int i = 2; i < serializedBlocks.length; i++) {
 					String[] serializedBlock = serializedBlocks[i].split("#");
 					int stackPosition = Integer.valueOf(serializedBlock[0]);
 					if(stackPosition >= deserializedInventory.getSize()) {
 						continue;
 					}
 					ItemStack is = null;
 					Boolean createdItemStack = false;
 					String[] serializedItemStack = serializedBlock[1].split(":");
 					for(String itemInfo : serializedItemStack) {
						ItemMeta meta = is.getItemMeta();
 						String[] itemAttribute = itemInfo.split("@");
 						if(itemAttribute[0].equals("t")) {
 							is = new ItemStack(Material.getMaterial(Integer.valueOf(itemAttribute[1])));
 							createdItemStack = true;
 						} else if(itemAttribute[0].equals("d") && createdItemStack) {
 							is.setDurability(Short.valueOf(itemAttribute[1]));
 						} else if(itemAttribute[0].equals("a") && createdItemStack) {
 							is.setAmount(Integer.valueOf(itemAttribute[1]));
 						} else if(itemAttribute[0].equals("n") && createdItemStack) {
 							meta.setDisplayName(convertSymbolsForLoading(itemAttribute[1]));
 						} else if(itemAttribute[0].equals("e") && createdItemStack) {
 							is.addUnsafeEnchantment(Enchantment.getById(Integer.valueOf(itemAttribute[1])), Integer.valueOf(itemAttribute[2]));
 						} else if(itemAttribute[0].equals("l") && createdItemStack) {
 							ArrayList<String> lores = new ArrayList<String>();
 							for(String curStr : itemAttribute[1].split("~")) {
 								lores.add(convertSymbolsForLoading(curStr));
 							}
 							meta.setLore(lores);
 						}
 						is.setItemMeta(meta);
 					}
 					deserializedInventory.setItem(stackPosition, is);
 				}
 				return deserializedInventory;
 			} else if(player != null) {
 				return player.getInventory();
 			}
 		} else if(player != null) {
 			return player.getInventory();
 		}
 		return Bukkit.getServer().createInventory(null, InventoryType.PLAYER);
 	}
 	public static Inventory StringToInventory(String invString) {return StringToInventory(invString, null);}
 	
 }
