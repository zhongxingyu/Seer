 package me.marwzoor.skillparry;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 
 import com.herocraftonline.heroes.Heroes;
 import com.herocraftonline.heroes.characters.Hero;
 import com.herocraftonline.heroes.characters.effects.EffectType;
 import com.herocraftonline.heroes.characters.skill.PassiveSkill;
 import com.herocraftonline.heroes.characters.skill.Skill;
 import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
 import com.herocraftonline.heroes.characters.skill.SkillSetting;
 import com.herocraftonline.heroes.characters.skill.SkillType;
 import com.herocraftonline.heroes.util.Messaging;
 import com.herocraftonline.heroes.util.Util;
 
 public class SkillParry extends PassiveSkill {
 	
 	public SkillParry(Heroes plugin) {
 		super(plugin, "Parry");
 		setDescription("You have a $1% passive chance to parry melee attacks.");
 		setEffectTypes(new EffectType[] {
 			EffectType.BENEFICIAL
 		});
 		setTypes(new SkillType[] {
 			SkillType.COUNTER, SkillType.BUFF
 		});
 		Bukkit.getServer().getPluginManager().registerEvents(new SkillParryListener(this, plugin), plugin);
 	}
 	
 	public ConfigurationSection getDefaultConfig() {
 		ConfigurationSection node = super.getDefaultConfig();
 		node.set(SkillSetting.CHANCE.node(), Double.valueOf(0.05));
 		node.set(SkillSetting.CHANCE_LEVEL.node(), Double.valueOf(0.001));
 		return node;
 	}
 	
 	public String getDescription(Hero hero) {
 		double chance = SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE, 0.05, false);
 		double chanceLvl = SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE_LEVEL, 0.001, false);
 		int level = hero.getSkillLevel(this);
 		if (level < 1) {
 			level = 1;
 		}
		return  getDescription().replace("$1", Util.stringDouble((chance + chanceLvl * level) * 100));
 	}
 	
 	public class SkillParryListener implements Listener {
 		
 		private final Skill skill;
 		private final Heroes heroes;
 		
 		SkillParryListener(Skill skill, Heroes heroes) {
 			this.skill = skill;
 			this.heroes = heroes;
 		}
 		
 		@EventHandler
 		public void onPlayerTakeDamage(EntityDamageByEntityEvent event) {
 			if (event.isCancelled()) {
 				return;
 			}
 			
 			if (!(event.getEntity() instanceof Player)) {
 				return;
 			}
 			
 			Player player = (Player) event.getEntity();
 			Hero hero = heroes.getCharacterManager().getHero(player);
 			if (!hero.hasEffect("Parry")) {
 				return;
 			}
 			
 			if (event.getDamager() instanceof Player || event.getDamager() instanceof Monster) {
 				double chance = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE, 0.05, false) * 100
 						+ SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE_LEVEL, 0.001, false) * hero.getLevel()
 						* 100;
 				if (Util.nextRand() <= chance) {
 					Messaging.send(hero.getPlayer(), "You parried " + event.getDamage() + " damage!");
 					event.setDamage(0);
 					return;
 				}
 			}
 		}
 	}
 
 }
