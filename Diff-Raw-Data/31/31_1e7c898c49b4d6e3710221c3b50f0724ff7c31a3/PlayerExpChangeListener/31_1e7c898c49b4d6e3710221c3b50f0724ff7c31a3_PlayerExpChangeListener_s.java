 package com.gildorymrp.gildorymclasses;
 
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerExpChangeEvent;
 
 import com.gildorymrp.charactercards.CharacterCard;
 import com.gildorymrp.charactercards.GildorymCharacterCards;
 import com.gildorymrp.charactercards.Race;
 
 public class PlayerExpChangeListener implements Listener {
 	private GildorymClasses plugin;
 
 	public PlayerExpChangeListener(GildorymClasses plugin) {
 		this.plugin = plugin;
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerExpChange(PlayerExpChangeEvent event) {
 		Player player = event.getPlayer();
 		Map<String, Integer> levelMap = this.plugin.levels;
 		Map<String, Integer> experienceMap = this.plugin.experience;
 		GildorymCharacterCards gildorymCharacterCards = (GildorymCharacterCards) Bukkit.getServer().getPluginManager().getPlugin("GildorymCharacterCards");
 		Map<String, CharacterCard> cardMap = gildorymCharacterCards.getCharacterCards();
 
 		event.setAmount(0);
 		if (levelMap.get(player.getName()) == null) {
 			levelMap.put(player.getName(), Integer.valueOf(1));
 		}
 		if (experienceMap.get(player.getName()) == null) {
 			experienceMap.put(player.getName(), Integer.valueOf(0));
 		}
 		int expToNextLevel = Math.round(1000 * ((Integer) levelMap.get(player.getName())).intValue());
 		while (((Integer) experienceMap.get(player.getName())).intValue() >= expToNextLevel) {
 			experienceMap.put(player.getName(), Integer.valueOf(((Integer) experienceMap.get(player.getName())).intValue() - expToNextLevel));
 			levelMap.put(player.getName(), Integer.valueOf(((Integer) levelMap.get(player.getName())).intValue() + 1));
 			expToNextLevel = 1000 * ((Integer) levelMap.get(player.getName())).intValue();
 		}
		player.setExp(((Integer) experienceMap.get(player.getName())).intValue() / expToNextLevel);
 		
 		CharacterClass clazz = this.plugin.classes.get(player.getName());
 		Integer level = levelMap.get(player.getName());
 		Race race = cardMap.get(player.getName()).getRace();
 		Integer pvpHealth = CharacterCard.calculateHealth(clazz, race, level);
 		
 		player.setMaxHealth(pvpHealth * 5);
 		player.setLevel(((Integer) levelMap.get(player.getName())).intValue());
 	}
 }
