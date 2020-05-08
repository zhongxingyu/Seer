 package org.shininet.bukkit.itemrenamer;
 
 import java.util.List;
 
 import net.milkbowl.vault.chat.Chat;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.shininet.bukkit.itemrenamer.configuration.DamageLookup;
 import org.shininet.bukkit.itemrenamer.configuration.ItemRenamerConfiguration;
 import org.shininet.bukkit.itemrenamer.configuration.RenameRule;
 
 import com.comphenix.protocol.wrappers.nbt.NbtCompound;
 import com.comphenix.protocol.wrappers.nbt.NbtFactory;
 import com.google.common.collect.Lists;
 
 public class RenameProcessor {
 	private static final String MARKER_KEY = "com.comphenix.marker";
 
 	private ItemRenamerConfiguration config;
 	
 	// Vault
 	private Chat chat;
 	
 	/**
 	 * A marker telling us that this is ItemStack was renamed by ItemRenamer.
 	 */
 	private final int MARKER = 0xD17065B1;
 	
 	public RenameProcessor(ItemRenamerConfiguration config, Chat chat) {
 		this.config = config;
 		this.chat = chat;
 	}
 
 	private void packName(ItemMeta itemMeta, RenameRule rule) {
 		if (rule.getName() != null) {
 			itemMeta.setDisplayName(ChatColor.RESET + 
 					ChatColor.translateAlternateColorCodes('&', rule.getName()) + ChatColor.RESET);
 		}
 	}
 	
 	private void packLore(ItemMeta itemMeta, RenameRule rule) {
 		// Don't process empty rules
 		if (rule.getLoreSections().size() == 0)
 			return;
 		
 		List<String> output = Lists.newArrayList(rule.getLoreSections());
 		
 		// Translate color codes as well
 		for (int i = 0; i < output.size(); i++) {
 			output.set(i, ChatColor.translateAlternateColorCodes('&', output.get(i)) + ChatColor.RESET);
 		}
 		itemMeta.setLore(output);
 	}
 
 	public ItemStack process(Player player, ItemStack input) {
 		return process(getPack(player), input);
 	}
 	
 	private ItemStack process(String pack, ItemStack input) {
 		// The item stack has already been cloned in the packet
 		if (input != null && pack != null) {
 			DamageLookup lookup = config.getRenameConfig().getLookup(pack, input.getTypeId());
 		
 			if (lookup != null) {
 				RenameRule rule = lookup.getRule(input.getDurability());
 				
 				if (rule == null)
 					return input;
 				ItemMeta itemMeta = input.getItemMeta();
 				
 				// May have been set by a plugin or a player
 				if ((itemMeta.hasDisplayName()) || (itemMeta.hasLore())) 
 					return input;
				// Fix a client bug
				if (itemMeta instanceof BookMeta) {
					BookMeta book = (BookMeta) itemMeta;
					
					// Create the pages NBT tag
					if (book.getPageCount() == 0) {
						book.setPages("");
					}
				}
 				
 				packName(itemMeta, rule);
 				packLore(itemMeta, rule);
 				input.setItemMeta(itemMeta);
 				
 				// Add a simple marker allowing us to detect renamed items
 				// Note that this MUST be exected after ItemMeta
 				getCompound(input).put(MARKER_KEY, MARKER);
 			}
 		}
 		
 		// Just return it - for chaining
 		return input;
 	}
 	
 	/**
 	 * Undo a item rename, or leave as is.
 	 * @param stack - the stack to undo.
 	 * @return TRUE if we removed the rename and lore, FALSE otherwise.
 	 */
 	public boolean unprocess(ItemStack input) {
 		if (input != null) {
 			ItemMeta meta = input.getItemMeta();
 			
 			// It has not been touched by anyone
 			if (!meta.hasDisplayName() && !meta.hasLore())
 				return false;
 			NbtCompound data = getCompound(input);
 			
 			// Check for our marker
 			if (data.containsKey(MARKER_KEY) && data.getInteger(MARKER_KEY) == MARKER) {
 				// Not necessary today, but we will not assume it is in the future
 				data.getValue().remove(MARKER_KEY);
 				
 				// Remove name and lore modifications
 				meta.setDisplayName(null);
 				meta.setLore(null);
 				input.setItemMeta(meta); 
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public ItemStack[] process(Player player, ItemStack[] input) {
 		String pack = getPack(player);
 		
 		if (input != null) {
 			for (int i = 0; i < input.length; i++) {
 				process(pack, input[i]);
 			}
 		}
 		return input;
 	}
 	
 	private String getPack(Player player) {
 		if (chat != null) {
 			String pack = chat.getPlayerInfoString(player, "itempack", null);
 
 			// Use this pack instead
 			if (pack != null && pack.length() > 0)
 				return pack;
 		}
 		return config.getWorldPack(player.getWorld().getName());
 	}
 	
 	private NbtCompound getCompound(ItemStack stack) {
 		// It should have been a compound in the API ...
 		return NbtFactory.asCompound(NbtFactory.fromItemTag(stack));
 	}
 }
