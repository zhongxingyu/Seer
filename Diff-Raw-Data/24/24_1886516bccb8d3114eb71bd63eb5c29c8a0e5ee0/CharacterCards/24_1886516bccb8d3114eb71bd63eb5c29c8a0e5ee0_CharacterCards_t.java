 package com.gildorym.charactercards;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.gildorym.basicchar.BasicChar;
 
 public class CharacterCards extends JavaPlugin {
 	
 	private Map<String, CharacterCard> characterCards = new HashMap<String, CharacterCard>();
 	
 	/**
 	 * @return the characterCards
 	 */
 	public Map<String, CharacterCard> getCharacterCards() {
 		return characterCards;
 	}
 
 	public void onEnable() {
 		SaveDataManager.loadData(this);
 		this.registerListeners(new Listener[] {
 				new PlayerInteractEntityListener(this),
 				new PlayerDeathListener(this),
 				new EntityRegainHealthListener(),
 		});
 		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 
 			@Override
 			public void run() {
 				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
 					BasicChar basicChar = (BasicChar) Bukkit.getServer().getPluginManager().getPlugin("BasicChar");
 					if (basicChar.levels.get(player.getName()) != null) {
 						Integer maxHealth = (int) (5 + Math.floor(basicChar.levels.get(player.getName()) / 5));
						if (CharacterCards.this.getCharacterCards().get(player.getName()) != null) {
							if (CharacterCards.this.getCharacterCards().get(player.getName()).getRace() == Race.ELF) {
								maxHealth -= 1;
							}
							if (CharacterCards.this.getCharacterCards().get(player.getName()).getRace() == Race.DWARF) {
								maxHealth += 1;
							}
							if (CharacterCards.this.getCharacterCards().get(player.getName()).getRace() == Race.GNOME) {
								maxHealth += 1;
							}
							if (CharacterCards.this.getCharacterCards().get(player.getName()).getHealth() < maxHealth) {
								CharacterCards.this.getCharacterCards().get(player.getName()).setHealth(CharacterCards.this.getCharacterCards().get(player.getName()).getHealth() + 1);
							}
 						}
 					}
 				}
 			}
 			
 		}, 7200L, 7200L);
 		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			
 			@Override
 			public void run() {
 				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
 					if (player.getHealth() < player.getMaxHealth() && player.isSleeping()) {
 						player.setHealth(player.getHealth() + Math.min(Math.round((float) player.getMaxHealth() / 20F), player.getMaxHealth() - player.getHealth()));
 						player.sendMessage(ChatColor.GREEN + "" + ChatColor.ITALIC + "You feel a little more refreshed from sleep. +" + Math.min(Math.round((float) player.getMaxHealth() / 20F), player.getMaxHealth() - player.getHealth()) + "HP");
 					}
 				}
 			}
 		
 		}, 2400L, 2400L);
 		this.getCommand("setage").setExecutor(new SetAgeCommand(this));
 		this.getCommand("setgender").setExecutor(new SetGenderCommand(this));
 		this.getCommand("setrace").setExecutor(new SetRaceCommand(this));
 		this.getCommand("setinfo").setExecutor(new SetInfoCommand(this));
 		this.getCommand("addinfo").setExecutor(new AddInfoCommand(this));
 		this.getCommand("char").setExecutor(new CharCommand(this));
 		this.getCommand("takehit").setExecutor(new TakeHitCommand(this));
 	}
 	
 	public void onDisable() {
 		SaveDataManager.saveData(this);
 	}
 	
 	private void registerListeners(Listener... listeners) {
 		for (Listener listener : listeners) {
 			this.getServer().getPluginManager().registerEvents(listener, this);
 		}
 	}
 
 }
