 package me.marwzoor.skillcageofblood;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Damageable;
 
 import com.herocraftonline.heroes.Heroes;
 import com.herocraftonline.heroes.api.SkillResult;
 import com.herocraftonline.heroes.characters.Hero;
 import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
 import com.herocraftonline.heroes.characters.effects.common.InvulnerabilityEffect;
 import com.herocraftonline.heroes.characters.effects.common.StunEffect;
 import com.herocraftonline.heroes.characters.skill.ActiveSkill;
 import com.herocraftonline.heroes.characters.skill.Skill;
 import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
 import com.herocraftonline.heroes.characters.skill.SkillSetting;
 import com.herocraftonline.heroes.characters.skill.SkillType;
 import com.herocraftonline.heroes.util.Messaging;
 
 public class SkillCageofBlood extends ActiveSkill {
 	public Heroes plugin;
 
 	public SkillCageofBlood(Heroes instance) {
 		super(instance, "CageofBlood");
 		this.plugin = instance;
 		setDescription("You seal yourself in a cage of blood, setting your health to 1, making you invulnerable but preventing you from moving or casting spells for $1 seconds. When you leave the cage you have full health.");
 		setIdentifiers(new String[] {
 				"skill cageofblood"
 		});
 		setTypes(new SkillType[] {
 				SkillType.HEAL
 		});
 	}
 	
 	public ConfigurationSection getDefaultConfig() {
 		ConfigurationSection node = super.getDefaultConfig();
 		node.set(SkillSetting.DURATION.node(), Integer.valueOf(5000));
 		return node;
 	}
 	
 	public String getDescription(Hero hero) {
 		if (hero.hasAccessToSkill(this)) {
 			int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(5000), false) / 1000;
 			return super.getDescription().replace("$1", duration + "");
 		} else {
 			return super.getDescription().replace("$1", "X");
 		}
 	}
 	
 	public SkillResult use(Hero hero, String[] args) {
 		int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(5000), false);
 		
 		CageofBloodEffect cobEffect = new CageofBloodEffect(this, plugin, duration);
 		
 		if (hero.hasEffect("CageofBlood")) {
			Messaging.send("You cannot use that at this time!");
 			return SkillResult.NORMAL;
 		}
 		
 		hero.addEffect(cobEffect);
 		
 		return SkillResult.NORMAL;
 	}
 	
 	public class CageofBloodEffect extends ExpirableEffect {
 		private int duration;
 		private Skill skill;
 		
 		public CageofBloodEffect(Skill skill, Heroes plugin, int duration) {
 			super(skill, plugin, "CageofBlood", duration);
 			this.skill = skill;
 			this.duration = duration;
 		}
 		
 		@Override
 		public void applyToHero(Hero hero) {
 			super.applyToHero(hero);
 			hero.getPlayer().setHealth(1.0D);
 			hero.addEffect(new StunEffect(skill, duration));
 			hero.addEffect(new InvulnerabilityEffect(skill, duration));
 		}
 		
 		@Override
 		public void removeFromHero(Hero hero) {
 			super.removeFromHero(hero);
 			Messaging.send(hero.getPlayer(), "You are no longer restricted by Cage of Blood and have healed fully!");
 			hero.getPlayer().setHealth(((Damageable) hero.getPlayer()).getMaxHealth());
 		}
 	}
 }
