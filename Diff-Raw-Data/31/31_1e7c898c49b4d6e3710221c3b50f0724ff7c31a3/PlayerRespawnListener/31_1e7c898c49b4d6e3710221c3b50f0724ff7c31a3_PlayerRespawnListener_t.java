 package com.gildorymrp.gildorymclasses;
 
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 import com.gildorymrp.charactercards.CharacterCard;
 import com.gildorymrp.charactercards.GildorymCharacterCards;
 import com.gildorymrp.charactercards.Race;
 
 public class PlayerRespawnListener implements Listener {
 	private GildorymClasses plugin;
 
 	public PlayerRespawnListener(GildorymClasses plugin) {
 		this.plugin = plugin;
 	}
 
 	@EventHandler
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		final Player player = event.getPlayer();
 		this.plugin.getServer().getScheduler()
 		.scheduleSyncDelayedTask(this.plugin, new Runnable() {
 			public void run() {
 				Map<String, Integer> levelMap = PlayerRespawnListener.this.plugin.levels;
 				Map<String, Integer> experienceMap = PlayerRespawnListener.this.plugin.experience;
 				GildorymCharacterCards gildorymCharacterCards = (GildorymCharacterCards) Bukkit.getServer().getPluginManager().getPlugin("GildorymCharacterCards");
 				Map<String, CharacterCard> cardMap = gildorymCharacterCards.getCharacterCards();
 				
 				if (levelMap.get(player.getName()) == null) {
 					levelMap.put(player.getName(), Integer.valueOf(1));
 				}
 				if (experienceMap.get(player.getName()) == null) {
 					experienceMap.put(player.getName(),
 							Integer.valueOf(0));
 				}
 				int expToNextLevel = Math.round(1000 * ((Integer) levelMap.get(player.getName())).intValue());
 				while (((Integer) experienceMap.get(player.getName())).intValue() >= expToNextLevel) {
 					experienceMap.put(player.getName(), Integer.valueOf(((Integer) experienceMap.get(player.getName())).intValue() - expToNextLevel));
 					levelMap.put(player.getName(), Integer.valueOf(((Integer) levelMap.get(player.getName())).intValue() + 1));
 					expToNextLevel = 1000 * ((Integer) levelMap.get(player.getName())).intValue();
 				}
 				
 				CharacterClass clazz = PlayerRespawnListener.this.plugin.classes.get(player.getName());
 				Integer level = levelMap.get(player.getName());
 				Race race = cardMap.get(player.getName()).getRace();
 				Integer pvpHealth = CharacterCard.calculateHealth(clazz, race, level);
 				
				player.setExp((float) (((Integer) experienceMap.get(player.getName())).intValue() / (double) expToNextLevel));
 				player.setLevel(((Integer) levelMap.get(player.getName())).intValue());
 				player.setMaxHealth(pvpHealth * 5);
 			}
 		}, 20L);
 	}
 }
