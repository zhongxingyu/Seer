 package com.censoredsoftware.demigods.engine.listener;
 
 import java.util.UUID;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import com.censoredsoftware.demigods.engine.Demigods;
 import com.censoredsoftware.demigods.engine.data.DataManager;
 import com.censoredsoftware.demigods.engine.data.TributeManager;
 import com.censoredsoftware.demigods.engine.deity.Deity;
 import com.censoredsoftware.demigods.engine.language.Translation;
 import com.censoredsoftware.demigods.engine.player.DCharacter;
 import com.censoredsoftware.demigods.engine.player.DPlayer;
 import com.censoredsoftware.demigods.engine.structure.Structure;
 import com.censoredsoftware.demigods.engine.structure.StructureData;
 import com.censoredsoftware.demigods.engine.util.Configs;
 import com.censoredsoftware.demigods.engine.util.Zones;
 
 public class TributeListener implements Listener
 {
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onTributeInteract(PlayerInteractEvent event)
 	{
 		if(Zones.inNoDemigodsZone(event.getPlayer().getLocation())) return;
 
 		// Return if the player is mortal
 		if(!DPlayer.Util.isImmortal(event.getPlayer())) return;
 		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
 
 		// Define variables
 		Location location = event.getClickedBlock().getLocation();
 		Player player = event.getPlayer();
 		DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 
 		if(Structure.Util.partOfStructureWithFlag(location, Structure.Flag.TRIBUTE_LOCATION))
 		{
 			// Cancel the interaction
 			event.setCancelled(true);
 
 			// Define the shrine
 			StructureData save = Structure.Util.getStructureRegional(location);
 
 			// Return if they aren't clicking the gold block
 			if(!save.getClickableBlocks().contains(event.getClickedBlock().getLocation())) return;
 
 			// Return if the player is mortal
 			if(!DPlayer.Util.getPlayer(player).hasCurrent())
 			{
 				player.sendMessage(ChatColor.RED + Demigods.LANGUAGE.getText(Translation.Text.DISABLED_MORTAL));
 				return;
 			}
 			else if(save.getOwner() != null && !character.getDeity().equals(DCharacter.Util.load(save.getOwner()).getDeity()))
 			{
 				player.sendMessage(Demigods.LANGUAGE.getText(Translation.Text.MUST_BE_ALLIED_TO_TRIBUTE).replace("{deity}", DCharacter.Util.load(save.getOwner()).getDeity().getName()));
 				return;
 			}
 			tribute(character, save);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerTribute(InventoryCloseEvent event)
 	{
 		if(Zones.inNoDemigodsZone(event.getPlayer().getLocation())) return;
 
 		// Define player and character
 		Player player = (Player) event.getPlayer();
 		DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 
 		// Make sure they have a character and are immortal
 		if(character == null) return;
 
 		// If it isn't a tribute chest then break the method
		if(!Demigods.LANGUAGE.getText(Translation.Text.CHEST_TRIBUTE_TO).contains(event.getInventory().getName()) || !Structure.Util.partOfStructureWithFlag(player.getTargetBlock(null, 10).getLocation(), Structure.Flag.TRIBUTE_LOCATION)) return;
 
 		// Get the creator of the shrine
 		StructureData save = StructureData.Util.load(UUID.fromString(DataManager.getValueTemp(player.getName(), character.getName()).toString()));
 
 		// Calculate the tribute value
 		int tributeValue = 0, items = 0;
 		for(ItemStack item : event.getInventory().getContents())
 		{
 			if(item != null)
 			{
 				tributeValue += TributeManager.processTribute(item);
 				items += item.getAmount();
 			}
 		}
 
 		// Return if it's empty
 		if(items == 0) return;
 
 		// Handle the multiplier
 		tributeValue *= Configs.getSettingDouble("multipliers.favor");
 
 		// Get the current favor for comparison
 		int favorBefore = character.getMeta().getFavor();
 		int maxFavorBefore = character.getMeta().getMaxFavor();
 
 		// Update the character's favor
 		character.getMeta().addFavor(tributeValue / 3);
 		character.getMeta().addMaxFavor(tributeValue);
 
 		// Define the shrine owner
 		if(save.getOwner() != null)
 		{
 			DCharacter shrineOwner = DCharacter.Util.load(save.getOwner());
 			OfflinePlayer shrineOwnerPlayer = shrineOwner.getOfflinePlayer();
 
 			if(character.getMeta().getMaxFavor() >= Configs.getSettingInt("caps.favor") && !player.getName().equals(shrineOwnerPlayer.getName()))
 			{
 				// Give them some of the blessings
 				shrineOwner.getMeta().addMaxFavor(tributeValue / 5);
 
 				// Message them
 				if(shrineOwnerPlayer.isOnline() && DPlayer.Util.getPlayer(shrineOwner.getOfflinePlayer()).getCurrent().getId().equals(shrineOwner.getId()))
 				{
 					((Player) shrineOwnerPlayer).sendMessage(Demigods.LANGUAGE.getText(Translation.Text.EXTERNAL_SHRINE_TRIBUTE));
 					((Player) shrineOwnerPlayer).sendMessage(Demigods.LANGUAGE.getText(Translation.Text.FAVOR_CAP_INCREASED).replace("{cap}", shrineOwner.getMeta().getMaxFavor().toString()));
 				}
 			}
 			else if(character.getMeta().getMaxFavor() > maxFavorBefore && !player.getName().equals(shrineOwnerPlayer.getName()))
 			{
 				// Define variables
 				int ownerFavorBefore = shrineOwner.getMeta().getMaxFavor();
 
 				// Give them some of the blessings
 				shrineOwner.getMeta().addMaxFavor(tributeValue / 5);
 
 				// Message them
 				if(shrineOwnerPlayer.isOnline() && DPlayer.Util.getPlayer(shrineOwner.getOfflinePlayer()).getCurrent().getId().equals(shrineOwner.getId()))
 				{
 					((Player) shrineOwnerPlayer).sendMessage(Demigods.LANGUAGE.getText(Translation.Text.EXTERNAL_SHRINE_TRIBUTE));
 					if(shrineOwner.getMeta().getMaxFavor() > ownerFavorBefore) ((Player) shrineOwnerPlayer).sendMessage(Demigods.LANGUAGE.getText(Translation.Text.FAVOR_CAP_INCREASED).replace("{cap}", shrineOwner.getMeta().getMaxFavor().toString()));
 				}
 			}
 		}
 
 		// Handle messaging and Shrine owner updating
 		if(character.getMeta().getMaxFavor() >= Configs.getSettingInt("caps.favor"))
 		{
 			// They have already met the max favor cap
 			player.sendMessage(Demigods.LANGUAGE.getText(Translation.Text.DEITY_PLEASED).replace("{deity}", character.getDeity().getName()));
 			if(character.getMeta().getFavor() > favorBefore) player.sendMessage(Demigods.LANGUAGE.getText(Translation.Text.BLESSED_WITH_FAVOR).replace("{favor}", "" + (character.getMeta().getFavor() - favorBefore)));
 		}
 		else if(character.getMeta().getMaxFavor() > maxFavorBefore)
 		{
 			// Message the tributer
 			player.sendMessage(Demigods.LANGUAGE.getText(Translation.Text.DEITY_PLEASED).replace("{deity}", character.getDeity().getName()));
 			player.sendMessage(Demigods.LANGUAGE.getText(Translation.Text.FAVOR_CAP_INCREASED).replace("{cap}", character.getMeta().getMaxFavor().toString()));
 		}
 		else if(items > 0)
 		{
 			// They aren't good enough, let them know!
 			player.sendMessage(Demigods.LANGUAGE.getText(Translation.Text.INSUFFICIENT_TRIBUTES).replace("{deity}", character.getDeity().getName()));
 		}
 
 		// Clear the tribute case
 		event.getInventory().clear();
 	}
 
 	private static void tribute(DCharacter character, StructureData save)
 	{
 		Player player = character.getOfflinePlayer().getPlayer();
 		Deity shrineDeity = character.getDeity();
 
 		// Open the tribute inventory
 		Inventory ii = Bukkit.getServer().createInventory(player, 27, Demigods.LANGUAGE.getText(Translation.Text.CHEST_TRIBUTE_TO).replace("{deity}", shrineDeity.getName()));
 		player.openInventory(ii);
 		DataManager.saveTemp(player.getName(), character.getName(), save.getId());
 	}
 }
