 package me.Whatshiywl.heroesskilltree;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.herocraftonline.heroes.Heroes;
 import com.herocraftonline.heroes.characters.Hero;
 import com.herocraftonline.heroes.characters.classes.HeroClass;
 import com.herocraftonline.heroes.characters.skill.Skill;
 import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
 
 public class HeroesSkillTree extends JavaPlugin {
 	
 	public final Logger logger = Logger.getLogger("Minecraft");
 	public HeroesSkillTree plugin;
     public final EventListener HEventListener = new EventListener(this);
 	public Boolean hasHeroes;
 	public FileConfiguration config;
 	private FileConfiguration playerConfig = null;
 	private File playerConfigFile = null;
 	private FileConfiguration heroesClassConfig = null;
 	private File heroesClassConfigFile = null;
 	public Heroes heroes = (Heroes)Bukkit.getServer().getPluginManager().getPlugin("Heroes");
 	public List<Skill> SkillStrongParents = new ArrayList<Skill>();
 	public List<Skill> SkillWeakParents = new ArrayList<Skill>();
 	
 	@Override
 	public void onDisable() 
 	{
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.logger.info(pdfFile.getName() + " Has Been Disabled!");
 		savePlayerConfig();
 	}
 	
 	@Override	
 	public void onEnable() 
 	{
 		plugin = this;
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.logger.info("[" + pdfFile.getName() + "] Version " + pdfFile.getVersion() + " Has Been Enabled!");
 		PluginManager pm = getServer().getPluginManager();
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		getPlayerConfig().options().copyDefaults(true);
 		savePlayerConfig();
 		final Heroes heroes = (Heroes)plugin.getServer().getPluginManager().getPlugin("Heroes");
 		if(heroes.isEnabled())
 		{
 			pm.registerEvents(this.HEventListener, this);
 		}
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 
 		final Heroes heroes = (Heroes)plugin.getServer().getPluginManager().getPlugin("Heroes");
 		if(sender instanceof Player){
 			Player player = (Player) sender;
 			Hero hero = heroes.getCharacterManager().getHero(player);
 			
 			//SKILLUP
 			if(commandLabel.equalsIgnoreCase("skillup")){
 				if(!player.hasPermission("skilltree.up")){
 					player.sendMessage(ChatColor.RED + "You don't have enough permissions!");
 					return false;
 				}
 				if(args.length > 0){
 					if(hero.hasAccessToSkill(args[0])){
 						Skill skill = heroes.getSkillManager().getSkill(args[0]);
 						int i;
 						if(args.length > 1) i = Integer.parseInt(args[1]);
 						else i = 1;
 						if(getPlayerPoints(hero) >= i){
 							if(getSkillMaxLevel(hero, skill) >= getSkillLevel(hero, skill) + i){
 								if(isLocked(hero, skill)){
 									if(canUnlock(hero, skill)){
 										if(!player.hasPermission("skilltree.override.usepoints")) setPlayerPoints(hero, getPlayerPoints(hero) - i);
 										setSkillLevel(hero, skill, getSkillLevel(hero, skill) + i);
 										savePlayerConfig();
 										player.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "You have unlocked " + skill.getName() + "! Level: " + getSkillLevel(hero, skill));
 									}
 									else player.sendMessage(ChatColor.RED + "You can't unlock this skill! /skillinfo (skill) to see requirements.");
 								}
 								else{
 									if(!player.hasPermission("skilltree.override.usepoints")) setPlayerPoints(hero, getPlayerPoints(hero) - i);
 									setSkillLevel(hero, skill, getSkillLevel(hero, skill) + i);
 									savePlayerConfig();
 									if(isMastered(hero, skill)) player.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.GREEN + "You have mastered " + skill.getName() + " at level " + getSkillLevel(hero, skill) + "!");
 									else player.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + skill.getName() + " level up: " + getSkillLevel(hero, skill));
 								}
 							}
 							else player.sendMessage(ChatColor.RED + "This skill has already been mastered.");
 						}
 						else player.sendMessage(ChatColor.RED + "You don't have enough SkillPoints.");
 					}
 					else if(heroes.getSkillManager().getSkills().contains(args[0])) player.sendMessage(ChatColor.RED + "You don't have this skill");
 					else player.sendMessage(ChatColor.RED + "This skill doesn't exist");
 				}
 				else player.sendMessage(ChatColor.RED + "No skill given: /skillup (skill) [amount]");
 			}
 			
 			//SKILLDOWN
 			else if(commandLabel.equalsIgnoreCase("skilldown")){
 				if(!player.hasPermission("skilltree.down")){
 					player.sendMessage(ChatColor.RED + "You don't have enough permissions!");
 					return false;
 				}
 				if(args.length > 0){
 					if(hero.hasAccessToSkill(args[0])){
 						Skill skill = heroes.getSkillManager().getSkill(args[0]);
 						int i;
 						if(args.length > 1) i = Integer.parseInt(args[1]);
 						else i = 1;
 						if(getSkillLevel(hero, skill) >= i){
 							if(getSkillLevel(hero, skill) - i >= 1){
 								if(player.hasPermission("skilltree.lock")){
 									player.sendMessage(ChatColor.RED + "You don't have enough permissions!");
 									return false;
 								}
 								if(!player.hasPermission("skilltree.override.usepoints")) setPlayerPoints(hero, getPlayerPoints(hero) + i);
 								setSkillLevel(hero, skill, getSkillLevel(hero, skill) - i);
 								savePlayerConfig();
 								if(isLocked(hero, skill)) player.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "You have locked " + skill.getName() + "!");
 								else player.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + skill.getName() + "level down: " + getSkillLevel(hero, skill));
 							}
 						}
 						else player.sendMessage(ChatColor.RED + "This skill is not a high enough level");
 					}
 					else if(heroes.getSkillManager().getSkills().contains(args[0])) player.sendMessage(ChatColor.RED + "You don't have this skill");
 					else player.sendMessage(ChatColor.RED + "This skill doesn't exist");
 				}
 				else player.sendMessage(ChatColor.RED + "No skill given: /skilldown (skill) [amount]");
 			}
 			
 			//SKILLINFO
 			else if(commandLabel.equalsIgnoreCase("skillinfo")){
 				if(player.hasPermission("skilltree.info")){
 					if(args.length > 0){
 						if(hero.hasAccessToSkill(args[0])){
 							Skill skill = heroes.getSkillManager().getSkill(args[0]);
 							player.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + skill.getName() + "'s info:");
 							if(isLocked(hero, skill)) player.sendMessage(ChatColor.RED + "This skill is currently locked!");
 							else if(isMastered(hero, skill)) player.sendMessage(ChatColor.GREEN + "This skill has been mastered at level " + getSkillLevel(hero, skill) + "!");
 							else{
 								player.sendMessage(ChatColor.AQUA + "Level: " + getSkillLevel(hero, skill));
 								player.sendMessage(ChatColor.AQUA + "Mastering level: " + getSkillMaxLevel(hero, skill));
 							}
 							if(isLocked(hero, skill)){
 								if(getStrongParentSkills(hero, skill) != null && getWeakParentSkills(hero, skill) != null){
 									player.sendMessage(ChatColor.AQUA + "Requirements:");
 									if(getStrongParentSkills(hero, skill) != null) player.sendMessage(ChatColor.AQUA + "Strong: " + getStrongParentSkills(hero, skill).toString());
 									if(getWeakParentSkills(hero, skill) != null) player.sendMessage(ChatColor.AQUA + "Weak: " + getWeakParentSkills(hero, skill).toString());
 								}
 							}
 							if(player.hasPermission("skilltree.points")) player.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "You currently have " + getPlayerPoints(hero) + " SkillPoints.");
 						}
 						else if(heroes.getSkillManager().getSkills().contains(args[0])) player.sendMessage(ChatColor.RED + "You don't have this skill");
 						else player.sendMessage(ChatColor.RED + "This skill doesn't exist");
 					}
 					else player.sendMessage(ChatColor.RED + "/skillinfo (skill)");
 				}
 				else player.sendMessage(ChatColor.RED + "You don't have enough permissions!");
 			}
 			
 			//SKILLPOINTS
 			else if(commandLabel.equalsIgnoreCase("skillpoints")){
 				if(player.hasPermission("skilltree.points")) player.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "You currently have " + getPlayerPoints(hero) + " SkillPoints.");
 				else player.sendMessage(ChatColor.RED + "You don't have enough permissions!");
 			}
			
 			//SKILLADMIN
 			else if(commandLabel.equalsIgnoreCase("skilladmin")){
 				if(args.length > 0){
 					if(args[0].equalsIgnoreCase("clear")){
 						if(player.hasPermission("skilladmin.clear")){
 							if(args.length == 2){
 								if(Bukkit.getPlayer(args[1]) != null){
 									Hero thero = heroes.getCharacterManager().getHero(Bukkit.getPlayer(args[1]));
 									setPlayerPoints(thero, 0);
 								}
 								else player.sendMessage(ChatColor.RED + "Sorry, " + args[1] + " is not online.");
 							}
 							else setPlayerPoints(hero, 0);
 						}
 						else player.sendMessage(ChatColor.RED + "You don't have enough permissions!");
 					}
 					else if(args[0].equalsIgnoreCase("reset")){
 						if(player.hasPermission("skilladmin.reset")){
 							if(args.length == 2){
 								if(Bukkit.getPlayer(args[1]) != null){
 									resetPlayer(Bukkit.getPlayer(args[1]));
 								}
 								else player.sendMessage(ChatColor.RED + "Sorry, " + args[1] + " is not online.");
 							}
 							else{
 								resetPlayer(player);
 							}
 						}
 						else player.sendMessage(ChatColor.RED + "You don't have enough permissions!");
 					}
 					else if(args.length > 1){
 						if(args[0].equalsIgnoreCase("set")){
 							if(player.hasPermission("skilladmin.set")){
 								if(args.length > 2){
 									if(Bukkit.getPlayer(args[2]) != null){
 										Hero thero = heroes.getCharacterManager().getHero(Bukkit.getPlayer(args[2]));
 										setPlayerPoints(thero, Integer.parseInt(args[1]));
 									}
 									else player.sendMessage(ChatColor.RED + "Sorry, " + args[2] + " is not online.");
 								}
 								else setPlayerPoints(hero, Integer.parseInt(args[1]));
 							}
 							else player.sendMessage(ChatColor.RED + "You don't have enough permissions!");
 						}
 						else if(args[0].equalsIgnoreCase("give")){
 							if(player.hasPermission("skilladmin.give")){
 								if(args.length > 2){
 									if(Bukkit.getPlayer(args[2]) != null){
 										Hero thero = heroes.getCharacterManager().getHero(Bukkit.getPlayer(args[2]));
 										setPlayerPoints(thero, getPlayerPoints(thero) + Integer.parseInt(args[1]));
 									}
 									else player.sendMessage(ChatColor.RED + "Sorry, " + args[2] + " is not online.");
 								}
 								else setPlayerPoints(hero, getPlayerPoints(hero) + Integer.parseInt(args[1]));
 							}
 							else player.sendMessage(ChatColor.RED + "You don't have enough permissions!");
 						}
 						else if(args[0].equalsIgnoreCase("remove")){
 							if(player.hasPermission("skilladmin.remove")){
 								if(args.length > 2){
 									if(Bukkit.getPlayer(args[2]) != null){
 										Hero thero = heroes.getCharacterManager().getHero(Bukkit.getPlayer(args[2]));
 										setPlayerPoints(thero, getPlayerPoints(thero) - Integer.parseInt(args[1]));
 									}
 									else player.sendMessage(ChatColor.RED + "Sorry, " + args[2] + " is not online.");
 								}
 								else setPlayerPoints(hero, getPlayerPoints(hero) - Integer.parseInt(args[1]));
 							}
 							else player.sendMessage(ChatColor.RED + "You don't have enough permissions!");
 						}
 						else player.sendMessage(ChatColor.RED + "/skilladmin (set/give/remove/clear/reset)");
 					}
 					else player.sendMessage(ChatColor.RED + "Not enough arguments: /skilladmin <command> (amount) [player]");
 				}
 				else player.sendMessage(ChatColor.RED + "/skilladmin <command> (amount) [player]");
 			}
 		}
 		return false;
 	}
 
 	public ConfigurationSection loadPlayer(Player player){
 		Hero hero = heroes.getCharacterManager().getHero(player);
 		if(!getPlayerConfig().contains(player.getName())){
 			//Creates new player section
 			getPlayerConfig().createSection(player.getName());
 			savePlayerConfig();
 		}
 
 		if(!getPlayerConfig().getConfigurationSection(player.getName()).contains("Points")){
 			//Creates point recorder for player
 			getPlayerConfig().getConfigurationSection(player.getName()).createSection("Points");
 			savePlayerConfig();
 		}
 		
 		for(Skill skill : heroes.getSkillManager().getSkills()){
 			if(hero.hasAccessToSkill(skill)){
 				if(!getPlayerConfig().getConfigurationSection(player.getName()).contains(skill.getName())){
 					//Creates new skills to player's section
 					getPlayerConfig().getConfigurationSection(player.getName()).set(skill.getName(), 0);
 					savePlayerConfig();
 				}
 			}
 		}
 		return getPlayerConfig().getConfigurationSection(player.getName());
 	}
 	
 	public void savePlayer(Player player){
 		Hero hero = heroes.getCharacterManager().getHero(player);
 		setPlayerPoints(hero, getPlayerPoints(hero));
 		for(Skill skill : heroes.getSkillManager().getSkills()){
 			if(hero.hasAccessToSkill(skill)){
 				if(!getPlayerConfig().getConfigurationSection(player.getName()).contains(skill.getName())){
 					//Creates new skills to player's section
 					getPlayerConfig().getConfigurationSection(player.getName()).set(skill.getName(), 0);
 					savePlayerConfig();
 				}
 			}
 		}
 		savePlayerConfig();
 	}
 	
 	public void resetPlayer(Player player){
 		Hero hero = heroes.getCharacterManager().getHero(player);
 		setPlayerPoints(hero, 0);
 		for(Skill skill : heroes.getSkillManager().getSkills()){
 			if(!isLocked(hero, skill)){
 				setSkillLevel(hero, skill, 0);
 			}
 		}
 	}
 	
 	public int getPlayerPoints(Hero hero){
 		return getPlayerConfig().getConfigurationSection(hero.getPlayer().getName()).getInt("Points");
 	}
 	
 	public void setPlayerPoints(Hero hero, int i){
 		getPlayerConfig().getConfigurationSection(hero.getPlayer().getName()).set("Points", i);
 		savePlayerConfig();
 	}
 	
 	public int getSkillLevel(Hero hero, Skill skill){
 		return getPlayerConfig().getConfigurationSection(hero.getPlayer().getName()).getInt(skill.getName());
 	}
 	
 	public void setSkillLevel(Hero hero, Skill skill, int i){
 		getPlayerConfig().getConfigurationSection(hero.getPlayer().getName()).set(skill.getName(), i);
 		savePlayerConfig();
 	}
 	
 	public int getSkillMaxLevel(Hero hero, Skill skill){
 		return (int) SkillConfigManager.getUseSetting(hero, skill, "max-level", 1, false);
 	}
 	
 	public List<String> getStrongParentSkills(Hero hero, Skill skill){
 		if((getHeroesClassConfig(hero.getHeroClass()).getConfigurationSection("permitted-skills." + skill.getName()).contains("parents")) &&
 			(getHeroesClassConfig(hero.getHeroClass()).getConfigurationSection("permitted-skills." + skill.getName() + ".parents").contains("strong"))){
 				return getHeroesClassConfig(hero.getHeroClass()).
 						getConfigurationSection("permitted-skills." + skill.getName() + ".parents").getStringList("strong");
 		}
 		else return null;
 	}
 	
 	public List<String> getWeakParentSkills(Hero hero, Skill skill){
 		if((getHeroesClassConfig(hero.getHeroClass()).getConfigurationSection("permitted-skills." + skill.getName()).contains("parents")) &&
 			(getHeroesClassConfig(hero.getHeroClass()).getConfigurationSection("permitted-skills." + skill.getName() + ".parents").contains("weak"))){
 				return getHeroesClassConfig(hero.getHeroClass()).
 						getConfigurationSection("permitted-skills." + skill.getName() + ".parents").getStringList("weak");
 		}
 		else return null;
 	}
 	
 	public boolean isLocked(Hero hero, Skill skill){ if(hero.hasAccessToSkill(skill)) return (getSkillLevel(hero, skill) <= 0); return true;}
 	
 	public boolean isMastered(Hero hero, Skill skill){ if(hero.hasAccessToSkill(skill)) return (getSkillLevel(hero, skill) >= getSkillMaxLevel(hero, skill)); return false;}
 	
 	public boolean canUnlock(Hero hero, Skill skill){
 		if(hero.hasAccessToSkill(skill) && (hero.canUseSkill(skill))){
 			if(getStrongParentSkills(hero, skill) == null && getWeakParentSkills(hero, skill) == null) return true;
 			else{
 				if(getStrongParentSkills(hero, skill) != null){
 					for(String name : getStrongParentSkills(hero, skill)){ if(!isMastered(hero, heroes.getSkillManager().getSkill(name))) return false;}
 					if(getWeakParentSkills(hero, skill) == null) return true;
 				}
 				if(getWeakParentSkills(hero, skill) != null){ 
 					for(String name : getWeakParentSkills(hero, skill)) if(isMastered(hero, heroes.getSkillManager().getSkill(name)))return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public void reloadPlayerConfig() {
 	    if (playerConfigFile == null) {
 	    	playerConfigFile = new File(getDataFolder(), "players.yml");
 	    }
 	    playerConfig = YamlConfiguration.loadConfiguration(playerConfigFile);
 	 
 	    // Look for defaults in the jar
 	    InputStream defConfigStream = this.getResource("players.yml");
 	    if (defConfigStream != null) {
 	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 	        playerConfig.setDefaults(defConfig);
 	    }
 	}
 	
 	public FileConfiguration getPlayerConfig() {
 	    if (playerConfig == null) {
 	        this.reloadPlayerConfig();
 	    }
 	    return playerConfig;
 	}
 	
 	public void savePlayerConfig() {
 	    if (playerConfig == null || playerConfigFile == null) {
 	    return;
 	    }
 	    try {
 	        getPlayerConfig().save(playerConfigFile);
 	    } catch (IOException ex) {
 	        this.getLogger().log(Level.SEVERE, "Could not save config to " + playerConfigFile, ex);
 	    }
 	}
 
 	public void reloadHeroesClassConfig(HeroClass HClass) {
 	    if (heroesClassConfigFile == null) {
 	    	heroesClassConfigFile = new File(heroes.getDataFolder() + "/classes", HClass.getName() + ".yml");
 	    }
 	    heroesClassConfig = YamlConfiguration.loadConfiguration(heroesClassConfigFile);
 	}
 	
 	public FileConfiguration getHeroesClassConfig(HeroClass HClass) {
 	    if (heroesClassConfig == null) {
 	        this.reloadHeroesClassConfig(HClass);
 	    }
 	    return heroesClassConfig;
 	}
 }
