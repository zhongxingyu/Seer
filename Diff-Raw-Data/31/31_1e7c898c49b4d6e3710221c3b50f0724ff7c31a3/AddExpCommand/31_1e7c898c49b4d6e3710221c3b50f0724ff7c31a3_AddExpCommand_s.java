 package com.gildorymrp.gildorymclasses;
 
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.gildorymrp.charactercards.CharacterCard;
 import com.gildorymrp.charactercards.GildorymCharacterCards;
 import com.gildorymrp.charactercards.Race;
 
 public class AddExpCommand implements CommandExecutor {
 	private GildorymClasses plugin;
 
 	public AddExpCommand(GildorymClasses plugin) {
 		this.plugin = plugin;
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		if (sender.hasPermission("basicchar.command.addexp")) {
 			Player player = this.plugin.getServer().getPlayer(args[0]);
 			Map<String, Integer> levelMap = this.plugin.levels;
 			Map<String, Integer> experienceMap = this.plugin.experience;
 			GildorymCharacterCards gildorymCharacterCards = (GildorymCharacterCards) Bukkit.getServer().getPluginManager().getPlugin("GildorymCharacterCards");
 			Map<String, CharacterCard> cardMap = gildorymCharacterCards.getCharacterCards();
 
 			if (player != null) {
 				if (levelMap.get(player.getName()) == null) {
 					levelMap.put(player.getName(), Integer.valueOf(1));
 				}
 				if (experienceMap.get(player.getName()) == null) {
 					experienceMap.put(player.getName(), Integer.valueOf(0));
 				}
 				experienceMap.put(player.getName(),
 						Integer.valueOf(((Integer) experienceMap.get(player
 								.getName())).intValue()
 								+ Integer.parseInt(args[1])));
 				int expToNextLevel = 1000 * ((Integer) levelMap.get(player
 						.getName())).intValue();
 				while (((Integer) experienceMap.get(player.getName()))
 						.intValue() >= expToNextLevel) {
 					experienceMap.put(
 							player.getName(),
 							Integer.valueOf(((Integer) experienceMap.get(player
 									.getName())).intValue() - expToNextLevel));
 					levelMap.put(player.getName(), Integer
 							.valueOf(((Integer) levelMap.get(player.getName()))
 									.intValue() + 1));
 					expToNextLevel = 1000 * ((Integer) levelMap.get(player
 							.getName())).intValue();
 				}
 				
 				CharacterClass clazz = this.plugin.classes.get(player.getName());
 				Integer level = levelMap.get(player.getName());
 				Race race = cardMap.get(player.getName()).getRace();
 				Integer pvpHealth = CharacterCard.calculateHealth(clazz, race, level);
 				
				player.setExp(((Integer) experienceMap.get(player.getName()))
						.intValue() / expToNextLevel);
 				player.setLevel(((Integer) levelMap.get(player.getName()))
 						.intValue());
 				player.setMaxHealth(pvpHealth * 5);
 				player.sendMessage(ChatColor.YELLOW + "+"
 						+ Integer.parseInt(args[1]) + " exp");
 				player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE
 						+ experienceMap.get(player.getName()) + "/"
 						+ expToNextLevel);
 				sender.sendMessage(ChatColor.GREEN + "Gave " + player.getName()
 						+ " " + Integer.parseInt(args[1]) + " exp");
 			}
 		} else
 			sender.sendMessage(ChatColor.RED + "You do not have permission.");
 
 		return true;
 	}
 }
