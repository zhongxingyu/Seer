 package me.marwzoor.skillstalemate;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 
 import com.herocraftonline.heroes.Heroes;
 import com.herocraftonline.heroes.api.SkillResult;
 import com.herocraftonline.heroes.characters.Hero;
 import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
 import com.herocraftonline.heroes.characters.skill.Skill;
 import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
 import com.herocraftonline.heroes.characters.skill.SkillSetting;
 import com.herocraftonline.heroes.characters.skill.SkillType;
 import com.herocraftonline.heroes.characters.skill.TargettedSkill;
 import com.herocraftonline.heroes.util.Messaging;
 
 public class SkillStalemate extends TargettedSkill
 {
 	public static Heroes plugin;
 	public static SkillStalemate skill;
 	
 	public SkillStalemate(Heroes instance)
 	{
 		super(instance, "Stalemate");
 		plugin=instance;
 		skill=this;
 		setArgumentRange(0,0);
 		setIdentifiers(new String[] { "skill stalemate", "skill smate" });
 		setDescription("You cannot damage your target, nor can they damage you for $1 seconds. (PvP only)");
 		setTypes(new SkillType[] { SkillType.COUNTER, SkillType.SILENCABLE });
 		
 		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
 	}
 	
 	public ConfigurationSection getDefaultConfig()
 	{
 		ConfigurationSection node = super.getDefaultConfig();
 		node.set(SkillSetting.DURATION.node(), Integer.valueOf(10000));
 		node.set(SkillSetting.DURATION_INCREASE.node(), Integer.valueOf(10));
 		node.set(SkillSetting.MANA.node(), Integer.valueOf(20));
 		node.set(SkillSetting.COOLDOWN.node(), Integer.valueOf(60000));
 		return node;
 	}
 	
 	public String getDescription(Hero hero)
 	{
 		if(hero.hasAccessToSkill(skill))
 		{
 			String desc = getDescription();
 			int duration = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, Integer.valueOf(10000), false);
 			duration += SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, Integer.valueOf(10), false);
 			duration = duration/1000;
 			return desc.replace("$1", duration + "");
 		}
 		else
 		{
 		return getDescription().replace("$1", "X");
 		}
 	}
 	
 	public SkillResult use(Hero hero, LivingEntity target, String[] args)
 	{
 		int duration = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, Integer.valueOf(10000), false);
 		duration +=  SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, Integer.valueOf(10), false);
 		
 		if(target.equals(hero.getPlayer()))
 		{
 			return SkillResult.INVALID_TARGET;
 		}
 		
 		if(!(target instanceof Player))
 		{
 			Messaging.send(hero.getPlayer(), "Invalid Target! (PvP only)");
 			return SkillResult.INVALID_TARGET_NO_MSG;
 		}
 		
 		Player tplayer = (Player) target;
 		
 		StalemateEffect sEffect = new StalemateEffect(skill, duration, tplayer);
 		
 		if(hero.hasEffect("Stalemate"))
 		{
 			hero.removeEffect(hero.getEffect("Stalemate"));
 		}
 		
 		hero.addEffect(sEffect);
 		
 		skill.broadcast(tplayer.getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + tplayer.getDisplayName() + ChatColor.GRAY + " is now " + hero.getPlayer().getDisplayName() + "'s " + ChatColor.WHITE + "Stalemate" + ChatColor.GRAY + "!");
 		
 		return SkillResult.NORMAL;
 	}
 	
 	public class SkillHeroListener implements Listener
 	{
		@EventHandler(priority = EventPriority.HIGH, ignoreCancelled=false)
 		public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event)
 		{
 			if(event.getDamager() instanceof Arrow)
 			{
 				if(((Arrow)event.getDamager()).getShooter() instanceof Player)
 				{
 					Player player = (Player) ((Arrow)event.getDamager()).getShooter();
 					
 					if(event.getEntity() instanceof Player)
 					{
 						Player tplayer = (Player) event.getEntity();
 						
 						Hero hero = plugin.getCharacterManager().getHero(player);
 						Hero thero = plugin.getCharacterManager().getHero(tplayer);
 						
 						if(hero.hasEffect("Stalemate"))
 						{
 							StalemateEffect sEffect = (StalemateEffect) hero.getEffect("Stalemate");
 							
 							if(sEffect.getStalemate().equals(tplayer))
 							{
 								player.sendMessage(ChatColor.GRAY + "You can't attack your " + ChatColor.WHITE + "Stalemate" + ChatColor.GRAY + "!");
 								event.setCancelled(true);
 								return;
 							}
 						}
 						
 						if(thero.hasEffect("Stalemate"))
 						{
 							StalemateEffect sEffect = (StalemateEffect) hero.getEffect("Stalemate");
 							
 							if(sEffect.getStalemate().equals(player))
 							{
 								player.sendMessage(ChatColor.GRAY + "You can't attack your " + ChatColor.WHITE + "Stalemate" + ChatColor.GRAY + "!");
 								event.setCancelled(true);
 								return;
 							}
 						}
 					}
 				}
 			}
 			else if(event.getDamager() instanceof Player)
 			{
 				Player player = (Player) event.getDamager();
 				
 				if(event.getEntity() instanceof Player)
 				{
 					Player tplayer = (Player) event.getEntity();
 					
 					Hero hero = plugin.getCharacterManager().getHero(player);
 					Hero thero = plugin.getCharacterManager().getHero(tplayer);
 					
 					if(hero.hasEffect("Stalemate"))
 					{
 						StalemateEffect sEffect = (StalemateEffect) hero.getEffect("Stalemate");
 						
 						if(sEffect.getStalemate().equals(tplayer))
 						{
 							player.sendMessage(ChatColor.GRAY + "You can't attack your " + ChatColor.WHITE + "Stalemate" + ChatColor.GRAY + "!");
 							event.setCancelled(true);
 							return;
 						}
 					}
 					
 					if(thero.hasEffect("Stalemate"))
 					{
 						StalemateEffect sEffect = (StalemateEffect) hero.getEffect("Stalemate");
 						
 						if(sEffect.getStalemate().equals(player))
 						{
 							player.sendMessage(ChatColor.GRAY + "You can't attack your " + ChatColor.WHITE + "Stalemate" + ChatColor.GRAY + "!");
 							event.setCancelled(true);
 							return;
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public class StalemateEffect extends ExpirableEffect
 	{
 		private Player stalemate;
 		
 		public StalemateEffect(Skill skill, int duration, Player stalemate)
 		{
 			super(skill, "Stalemate", duration);
 			this.stalemate=stalemate;
 		}
 		
 		public Player getStalemate()
 		{
 			return this.stalemate;
 		}
 		
 		public void applyToHero(Hero hero)
 		{
 			super.applyToHero(hero);
 		}
 		
 		public void removeFromHero(Hero hero)
 		{
 			skill.broadcast(stalemate.getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + stalemate.getDisplayName() + ChatColor.GRAY + " is no longer " + hero.getPlayer().getDisplayName() + "'s " + ChatColor.WHITE + "Stalemate" + ChatColor.GRAY + "!");
 			super.removeFromHero(hero);
 		}
 	}
 }
