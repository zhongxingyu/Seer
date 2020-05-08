 package me.marwzoor.skillcompanion;
 
 import java.util.Random;
 
 import net.smudgecraft.companions.ComWolf;
 import net.smudgecraft.companions.Companions;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.DyeColor;
 import org.bukkit.Location;
 import org.bukkit.Sound;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import com.herocraftonline.heroes.Heroes;
 import com.herocraftonline.heroes.api.SkillResult;
 import com.herocraftonline.heroes.api.events.ClassChangeEvent;
 import com.herocraftonline.heroes.api.events.HeroChangeLevelEvent;
 import com.herocraftonline.heroes.characters.Hero;
 import com.herocraftonline.heroes.characters.skill.ActiveSkill;
 import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
 import com.herocraftonline.heroes.characters.skill.SkillType;
 import com.herocraftonline.heroes.util.Messaging;
 
 public class SkillCompanion extends ActiveSkill
 {
 	public static Heroes plugin;
 	public static SkillCompanion skill;
 	
 	public SkillCompanion(Heroes instance)
 	{
 		super(instance, "Companion");
 		plugin=instance;
 		skill=this;
 		setDescription("You spawn your wolf companion to aid you in battle. HP: %1 DMG: %2");
 		setArgumentRange(0, 0);
 		setIdentifiers(new String[] { "skill companion" });
 		setTypes(new SkillType[] { SkillType.SUMMON });
 		
 		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
 	}
 	
 	public String getDescription(Hero hero)
 	{
 		String desc = super.getDescription();
 		int health = SkillConfigManager.getUseSetting(hero, skill, "wolfhealth", Integer.valueOf(300), false);
 		health += SkillConfigManager.getUseSetting(hero, skill, "wolfhealth-increase", Integer.valueOf(5), false) * hero.getSkillLevel(skill);
 		double damage = SkillConfigManager.getUseSetting(hero, skill, "wolfdamage", Double.valueOf(40), false);
 		damage += SkillConfigManager.getUseSetting(hero, skill, "wolfdamage-increase", Double.valueOf(0.2), false) * hero.getSkillLevel(skill);
 		desc = desc.replace("%1", health + "");
 		desc = desc.replace("%2", damage + "");
 		return desc;
 	}
 	
 	public ConfigurationSection getDefaultDescription()
 	{
 		ConfigurationSection node = super.getDefaultConfig();
 		node.set("wolfhealth", Integer.valueOf(300));
 		node.set("wolfhealth-increase", Integer.valueOf(5));
 		node.set("wolfdamage", Integer.valueOf(40));
 		node.set("wolfdamage-increase", Double.valueOf(0.2));
 		return node;
 	}
 	
 	public SkillResult use(Hero hero, String[] args)
 	{
		String[] names = new String[]{ "§4Aslan", "§4Raiku", "§bTyrion", "§bMerez", "§2Mundu", "§2Roof", "§eDanion", "§eHowleth", "§0Syric", "§0Mandrew", "§5Undion", "§5Quaz", "§4Wereth", "§4Fury", "§bAxlith", "§bOrion" };
 		
 		Random rand = new Random();
 		
 		String name = names[rand.nextInt(names.length)];
 		
 		Player player = hero.getPlayer();
 		
 		if(Companions.cwolves.hasWolf(player))
 		{
 			Messaging.send(player, ChatColor.RED + "You already have your companion spawned!");
 			
 			return SkillResult.CANCELLED;
 		}
 		
 		FileConfiguration config = Companions.getFileConfig();
 		
 		if(!config.contains("players"))
 		{
 			config.createSection("players");
 		}
 		
 		if(config.getConfigurationSection("players").contains(player.getName()) && config.getConfigurationSection("players").getString(player.getName() + ".wolf.name")!=null)
 		{
 			name = config.getConfigurationSection("players").getString(player.getName() + ".wolf.name");
 		}
 		
 		Location loc = hero.getPlayer().getLocation();
 		
 		int maxhealth = SkillConfigManager.getUseSetting(hero, skill, "wolfhealth", Integer.valueOf(300), false);
 		maxhealth += SkillConfigManager.getUseSetting(hero, skill, "wolfhealth-increase", Integer.valueOf(5), false) * hero.getSkillLevel(skill);
 		
 		double damage = SkillConfigManager.getUseSetting(hero, skill, "wolfdamage", Double.valueOf(40), false);
 		damage += SkillConfigManager.getUseSetting(hero, skill, "wolfdamage-increase", Double.valueOf(0.2), false) * hero.getSkillLevel(skill);
 		
 		ComWolf cwolf = Companions.spawnNewComWolf(loc, player.getName(), maxhealth, (int) damage, maxhealth, name, DyeColor.BLUE);
 		
 		Companions.saveWolf(cwolf);
 		
 		Messaging.send(hero.getPlayer(), "You have summoned your" + ChatColor.WHITE + " Companion " + ChatColor.GRAY + "to aid you in battle!", new Object());
 		
 		return SkillResult.NORMAL;
 	}
 	
 	public class SkillHeroListener implements Listener
 	{
 		@EventHandler
 		public void onHeroGainLevelEvent(HeroChangeLevelEvent event)
 		{
 			if(event.getFrom()<event.getTo())
 			{
 				Hero hero = event.getHero();
 				Player player = hero.getPlayer();
 				if(Companions.cwolves.hasWolf(player))
 				{
 					ComWolf cwolf = Companions.cwolves.getComWolf(player);
 					
 					int maxhealth = SkillConfigManager.getUseSetting(hero, skill, "wolfhealth", Integer.valueOf(300), false);
 					maxhealth += SkillConfigManager.getUseSetting(hero, skill, "wolfhealth-increase", Integer.valueOf(5), false) * hero.getSkillLevel(skill);
 					
 					double damage = SkillConfigManager.getUseSetting(hero, skill, "wolfdamage", Double.valueOf(40), false);
 					damage += SkillConfigManager.getUseSetting(hero, skill, "wolfdamage-increase", Double.valueOf(0.2), false) * hero.getSkillLevel(skill);
 					
 					cwolf.setMaxHealth(maxhealth);
 					cwolf.setHealth(maxhealth);
 					
 					cwolf.setDamage((int) damage);
 					
 					Companions.saveWolf(cwolf);
 				}
 			}
 		}
 		
 		@EventHandler
 		public void onHeroChangeClassEvent(ClassChangeEvent event)
 		{
 			final Hero hero = event.getHero();
 			if(event.getFrom().getSkillNames().contains("companion"))
 			{
 				if(event.getTo().isPrimary())
 				{
 					if(Companions.cwolves.hasWolf(hero.getPlayer()))
 					{
 						if(!event.getTo().getSkillNames().contains("companion"))
 						{
 							ComWolf cwolf = Companions.cwolves.getComWolf(hero.getPlayer());
 							cwolf.kill();
 							cwolf.getLocation().getWorld().playSound(cwolf.getLocation(), Sound.WOLF_WHINE, 10, 1);
 							hero.getPlayer().sendMessage(ChatColor.GRAY + "Your " + ChatColor.WHITE + "Companion" + ChatColor.GRAY + " is very sad because it has to leave you now...");
 							Companions.cwolves.removeComWolf(cwolf);
 						}
 					}
 				}
 			}
 		}
 		
 		@EventHandler
 		public void onPlayerJoinEvent(PlayerJoinEvent event)
 		{
 			Player player = event.getPlayer();
 			Hero hero = plugin.getCharacterManager().getHero(player);
 			
 			if(hero.hasAccessToSkill("Companion"))
 			{		
 				FileConfiguration config = Companions.getFileConfig();
 				
 				ConfigurationSection players = config.getConfigurationSection("players");
 				
 				String pname = player.getName();
 				
 				if(players.contains(pname + ".wolf.location.world"))
 				{
 				int health = players.getInt(pname + ".wolf.health");
 				int maxhealth = players.getInt(pname + ".wolf.maxhealth");
 				String name = players.getString(pname + ".wolf.name");
 				String owner = pname;
 				int damage = players.getInt(pname + ".wolf.damage");
 				
 				if(health!=0)
 				{
 				ComWolf cwolf = Companions.spawnNewComWolf(player.getLocation(), owner, maxhealth, (int) damage, health, name, DyeColor.BLUE);
 				
 				Companions.saveWolf(cwolf);
 				}
 				}
 			}
 		}
 		
 		@EventHandler
 		public void onPlayerQuitEvent(PlayerQuitEvent event)
 		{
 			Player player = event.getPlayer();
 			Hero hero = plugin.getCharacterManager().getHero(player);
 			
 			if(hero.hasAccessToSkill("Companion"))
 			{
 				if(Companions.cwolves.hasWolf(player))
 				{
 					ComWolf cwolf = Companions.cwolves.getComWolf(player);
 					
 					Companions.saveWolf(cwolf);
 					
 					cwolf.kill();
 					
 					Companions.cwolves.removeComWolf(cwolf);
 				}
 			}
 		}
 	}
 }
