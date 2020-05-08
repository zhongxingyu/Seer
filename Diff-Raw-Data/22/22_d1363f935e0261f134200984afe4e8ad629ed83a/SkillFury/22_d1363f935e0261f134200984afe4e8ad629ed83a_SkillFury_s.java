 package me.marwzoor.skillfury;
 
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.herocraftonline.heroes.Heroes;
 import com.herocraftonline.heroes.api.SkillResult;
 import com.herocraftonline.heroes.api.events.HeroLeaveCombatEvent;
 import com.herocraftonline.heroes.api.events.HeroRegainManaEvent;
 import com.herocraftonline.heroes.api.events.SkillUseEvent;
 import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
 import com.herocraftonline.heroes.characters.CharacterTemplate;
 import com.herocraftonline.heroes.characters.Hero;
 import com.herocraftonline.heroes.characters.skill.ActiveSkill;
 import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
 import com.herocraftonline.heroes.characters.skill.SkillType;
 import com.herocraftonline.heroes.util.Messaging;
 
 public class SkillFury extends ActiveSkill
 {
 	public static Heroes plugin;
 	public static SkillFury skill;
 	public static HashMap<Player, Integer> schedules = new HashMap<Player, Integer>();
 	
 	
 	public SkillFury(Heroes instance)
 	{
 		super(instance, "Fury");
 		plugin=instance;
 		skill=this;
 		setDescription("Fury is what flows through a true warriors vains, it's what gives the warrior its powers.");
 		setIdentifiers(new String[] { "skill fury" });
 		setArgumentRange(0, 0);
 		setTypes(new SkillType[] { SkillType.BUFF });
 		
 		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
 	}
 	
 	public String getDescription(Hero hero)
 	{
 		String desc = super.getDescription();
 		return desc;
 	}
 	
 	public static SkillFury getSkillFury()
 	{
 		return skill;
 	}
 	
 	public ConfigurationSection getDefaultConfig()
 	{
 		ConfigurationSection node = super.getDefaultConfig();
 		node.set("fury-loss", Integer.valueOf(5));
 		node.set("fury-gain", Integer.valueOf(5));
 		node.set("extradamage-50", Integer.valueOf(5));
 		node.set("extradamage-75", Integer.valueOf(10));
 		node.set("extradamage-100", Integer.valueOf(25));
 		return node;
 	}
 	
 	public SkillResult use(Hero hero, String[] args)
 	{
 		Messaging.send(hero.getPlayer(), ChatColor.DARK_RED + "FURY " + createFuryBar(hero.getMana(), hero.getMaxMana()));
 		return SkillResult.NORMAL;
 	}
 	
 	public class SkillHeroListener implements Listener
 	{
 		@EventHandler
 		public void onPlayerJoinEvent(PlayerJoinEvent event)
 		{
 			Player player = event.getPlayer();
 			Hero hero = plugin.getCharacterManager().getHero(player);
 			
 			if(hero.hasAccessToSkill(skill))
 			{
 				if(hero.getMana()==hero.getMaxMana())
 				{
 					int furyloss = SkillConfigManager.getUseSetting(hero, skill, "fury-loss", Integer.valueOf(5), false);
 					hero.setMana(hero.getMana()-furyloss);
 				}
 			}
 		}
 		
 		@EventHandler
 		public void onHeroRegainManaEvent(HeroRegainManaEvent event)
 		{
 			Hero hero = event.getHero();
 			
 			if(hero.hasAccessToSkill(skill))
 			{	
 				if(!hero.isInCombat())
 				{
 					int furyloss = SkillConfigManager.getUseSetting(hero, skill, "fury-loss", Integer.valueOf(5), false);
 					if(hero.getMana()<furyloss)
 					{
 						if(hero.getMana()!=0)
 						{
 							hero.setMana(0);
 							Messaging.send(hero.getPlayer(), ChatColor.DARK_RED + "FURY " + createFuryBar(hero.getMana(), hero.getMaxMana()));
 						}
 					}
 					else
 					{
 						hero.setMana(hero.getMana()-furyloss);
 						Messaging.send(hero.getPlayer(), ChatColor.DARK_RED + "FURY " + createFuryBar(hero.getMana(), hero.getMaxMana()));
 					}
 				}
 				else
 				{
 				Messaging.send(hero.getPlayer(), ChatColor.DARK_RED + "FURY " + createFuryBar(hero.getMana(), hero.getMaxMana()));
 				}
 				event.setCancelled(true);
 			}
 		}
 		
 		@EventHandler
 		public void onCommandPreProcessEvent(PlayerCommandPreprocessEvent event)
 		{
 			String[] command = event.getMessage().split(" ");
 			
 			if(command[0].equalsIgnoreCase("/fury") || command[0].equalsIgnoreCase("/mana"))
 			{
 				Player player = event.getPlayer();
 				Hero hero = plugin.getCharacterManager().getHero(player);
 				
 				if(hero.hasAccessToSkill(skill))
 				{
 					event.setMessage("/skill fury");
 				}
 			}
 		}
 		
 		@EventHandler
 		public void onHeroLeaveCombatEvent(HeroLeaveCombatEvent event)
 		{
 			Hero hero = event.getHero();
 			
 			if(hero.hasAccessToSkill(skill))
 			{
 				int furyloss = SkillConfigManager.getUseSetting(hero, skill, "fury-loss", Integer.valueOf(5), false);
 				if(hero.getMana()==hero.getMaxMana())
 				{
 					hero.setMana(hero.getMana()-furyloss);
 					Messaging.send(hero.getPlayer(), ChatColor.DARK_RED + "FURY " + createFuryBar(hero.getMana(), hero.getMaxMana()));
 				}
 				
 				if(schedules.containsKey(hero.getPlayer()))
 				{
 					Bukkit.getScheduler().cancelTask(schedules.get(hero.getPlayer()));
 					schedules.remove(hero.getPlayer());
 				}
 			}
 		}
 		
 		@EventHandler(ignoreCancelled=true)
 		public void onSkillUseEvent(SkillUseEvent event)
 		{
 			Hero hero = event.getHero();
 			
 			if(hero.hasAccessToSkill(skill))
 			{
 				int fury = event.getManaCost();
 				event.setManaCost(0);
 				if(hero.getMana()<fury)
 				{
 					Messaging.send(hero.getPlayer(), ChatColor.GRAY + "[" + ChatColor.DARK_GREEN + "Skill" + ChatColor.GRAY + "] Not enough " + ChatColor.DARK_RED + "Fury" + ChatColor.GRAY + "!");
 					event.setCancelled(true);
 				}
 				else
 				{
 					hero.setMana(hero.getMana()-fury);
 					if(!event.getSkill().equals(skill))
 					{
 						Messaging.send(hero.getPlayer(), ChatColor.DARK_RED + "FURY " + createFuryBar(hero.getMana(), hero.getMaxMana()));
 					}
 					if(schedules.containsKey(hero.getPlayer()))
 					{
 						Bukkit.getScheduler().cancelTask(schedules.get(hero.getPlayer()));
 						schedules.remove(hero.getPlayer());
 					}
 				}
 			}
 			
 		}
 		
 		@EventHandler
 		public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event)
 		{
 			if(event.getDamager() instanceof Player)
 			{
 				Player player = (Player) event.getDamager();
 				
				if(event.isCancelled())
					return;
				
 				if(event.getEntity() instanceof Monster || event.getEntity() instanceof Player)
 				{
 					final Hero hero = plugin.getCharacterManager().getHero(player);
 					
 					if(hero.hasAccessToSkill(skill) && isWeapon(player.getItemInHand()))
 					{
 						int furygain = SkillConfigManager.getUseSetting(hero, skill, "fury-gain", Integer.valueOf(5), false);
 						
 						int fury = hero.getMana();
 						int maxfury = hero.getMaxMana();
 						
 						if(fury+furygain>maxfury)
 						{
 							if(fury!=maxfury)
 							{
 								hero.setMana(maxfury);
 								Messaging.send(hero.getPlayer(), ChatColor.DARK_RED + "FURY " + createFuryBar(hero.getMana(), hero.getMaxMana()));
 								Messaging.send(hero.getPlayer(), ChatColor.RED + "Your " + ChatColor.DARK_RED + "FURY " + ChatColor.RED + "is now full!");
 								int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
 								{
 									public void run()
 									{
 										if(hero!=null)
 										{
 										hero.getPlayer().getWorld().playEffect(hero.getPlayer().getLocation(), Effect.MOBSPAWNER_FLAMES, 4);
 										}
 									}
 								}, 20L, 20L);
 								schedules.put(hero.getPlayer(), id);
 							}
 						}
 						else if(fury+furygain==maxfury)
 						{
 							hero.setMana(maxfury);
 							Messaging.send(hero.getPlayer(), ChatColor.DARK_RED + "FURY " + createFuryBar(hero.getMana(), hero.getMaxMana()));
 							Messaging.send(hero.getPlayer(), ChatColor.RED + "Your " + ChatColor.DARK_RED + "FURY " + ChatColor.RED + "is now full!");
 							int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
 							{
 								public void run()
 								{
 									if(hero!=null)
 									{
 									hero.getPlayer().getWorld().playEffect(hero.getPlayer().getLocation(), Effect.MOBSPAWNER_FLAMES, 4);
 									}
 								}
 							}, 20L, 20L);
 							schedules.put(hero.getPlayer(), id);
 						}
 						else
 						{
 							hero.setMana(fury+furygain);
 							Messaging.send(hero.getPlayer(), ChatColor.DARK_RED + "FURY " + createFuryBar(hero.getMana(), hero.getMaxMana()));
 						}
 					}
 				}
 			}
 		}
 		
 		@EventHandler
 		public void onWeaponDamageEvent(WeaponDamageEvent event)
 		{
 			if(event.getCause() != DamageCause.ENTITY_ATTACK)
 			{
 				return;
 			}
 			
 			CharacterTemplate character = event.getDamager();
 			if(character instanceof Hero)
 			{
 				Hero hero = (Hero) character;
 				
 				if(hero.hasAccessToSkill(skill))
 				{
 					double extradamage = getExtraDamage(hero);
 					
 					extradamage+=100;
 
 					double percentage = (extradamage/100);
 					
 					double damage = event.getDamage();
 					damage = damage*percentage;
 					
 					event.setDamage((int) damage);
 				}
 			}
 		}
 	}
 	public static String createFuryBar(double fury, double maxFury) 
 	{
 	    StringBuilder furyBar = new StringBuilder(new StringBuilder().append(ChatColor.RED).append("[").append(ChatColor.DARK_RED).toString());
 	    int percent = (int)(fury / maxFury * 100.0D);
 	    int progress = percent / 2;
 	    for (int i = 0; i < progress; i++) {
 	      furyBar.append('|');
 	    }
 	    furyBar.append(ChatColor.DARK_GRAY);
 	    for (int i = 0; i < 50 - progress; i++) {
 	      furyBar.append('|');
 	    }
 	    furyBar.append(ChatColor.RED).append(']');
 	    return new StringBuilder().append(furyBar).append(" - ").append(ChatColor.DARK_RED).append(percent).append("%").toString();
 	}
 	
 	public static int getExtraDamage(Hero hero)
 	{
 		if(hero.getMana()==100)
 		{
 			return SkillConfigManager.getUseSetting(hero, skill, "extradamage-100", Integer.valueOf(25), false);
 		}
 		else if(hero.getMana()>=75)
 		{
 			return SkillConfigManager.getUseSetting(hero, skill, "extradamage-75", Integer.valueOf(10), false);
 		}
 		else if(hero.getMana()>=50)
 		{
 			return SkillConfigManager.getUseSetting(hero, skill, "extradamage-50", Integer.valueOf(5), false);
 		}
 		else
 		{
 			return 0;
 		}
 	}
 	
 	public boolean isWeapon(ItemStack is)
 	{
 		int itemid = is.getTypeId();
 		
 		switch(itemid)
 		{
 		case 272: return true;
 		case 267: return true;
 		case 276: return true;
 		case 275: return true;
 		case 279: return true;
 		case 283: return true;
 		case 286: return true;
 		case 258: return true;
 		default: return false;
 		}
 	}
 }
