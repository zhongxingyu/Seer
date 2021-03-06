 package com.herocraftonline.dev.heroes.skill.skills;
 
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.SkillResult;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Setting;
 import net.minecraft.server.MobEffect;
 import org.bukkit.Location;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 
 public class SkillDespair extends ActiveSkill {
     private String applyText;
     private String expireText;
 
     public SkillDespair(Heroes plugin) {
         super(plugin, "Despair");
         setDescription("Blinds all enemies near you");
         setUsage("/skill despair");
         setArgumentRange(0, 1);
         setIdentifiers(new String[] { "skill despair" });
         
         setTypes(SkillType.DARK, SkillType.SILENCABLE, SkillType.HARMFUL);
     }
 
     @Override
     public String getDescription(Hero hero) {
         long duration = (long) (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 10000, false) +
                 (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getLevel())) / 1000;
         duration = duration > 0 ? duration : 0;
         int damage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 0, false) +
                 (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getLevel()));
         damage = damage > 0 ? damage : 0;
         int radius = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 10, false) +
                 (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0.0, false) * hero.getLevel()));
         radius = radius > 0 ? radius : 0;
         String description = getDescription().replace("$1", duration + "").replace("$2", damage + "").replace("$3", radius + "");
         
         //COOLDOWN
         int cooldown = (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 0, false)
                 - SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getLevel()) / 1000;
         if (cooldown > 0) {
             description += " CD:" + cooldown + "s";
         }
         
         //MANA
         int mana = SkillConfigManager.getUseSetting(hero, this, Setting.MANA.node(), 10, false)
                 - (SkillConfigManager.getUseSetting(hero, this, Setting.MANA_REDUCE.node(), 0, false) * hero.getLevel());
         if (mana > 0) {
             description += " M:" + mana;
         }
         
         //HEALTH_COST
         int healthCost = SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST, 0, false) - 
                 (SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST_REDUCE, mana, true) * hero.getLevel());
         if (healthCost > 0) {
             description += " HP:" + healthCost;
         }
         
         //STAMINA
         int staminaCost = SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA.node(), 0, false)
                 - (SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA_REDUCE.node(), 0, false) * hero.getLevel());
         if (staminaCost > 0) {
             description += " FP:" + staminaCost;
         }
         
         //DELAY
         int delay = SkillConfigManager.getUseSetting(hero, this, Setting.DELAY.node(), 0, false) / 1000;
         if (delay > 0) {
             description += " W:" + delay + "s";
         }
         
         //EXP
         int exp = SkillConfigManager.getUseSetting(hero, this, Setting.EXP.node(), 0, false);
         if (exp > 0) {
             description += " XP:" + exp;
         }
         return description;
     }
 
     @Override
     public ConfigurationSection getDefaultConfig() {
         ConfigurationSection node = super.getDefaultConfig();
         node.set(Setting.RADIUS.node(), 10);
         node.set("radius-increase", 0);
         node.set(Setting.DURATION.node(), 10000);
         node.set("duration-increase", 0);
         node.set(Setting.DAMAGE.node(), 0);
         node.set("damage-increase", 0);
         node.set("exp-per-blinded-player", 0);
         node.set("apply-text", "%hero% has blinded %target% with %skill%!");
         node.set("expire-text", "%hero% has recovered their sight!");
         return node;
     }
     
     @Override
     public void init() {
         super.init();
         applyText = SkillConfigManager.getRaw(this, "apply-text", "%hero% has blinded %target% with %skill%!").replace("%hero%", "$1").replace("%target%", "$2").replace("%skill%", "$3");
         expireText = SkillConfigManager.getRaw(this, "expire-text", "%hero% has recovered their sight!").replace("%hero%", "$1").replace("%target%", "$2").replace("%skill%", "$3");
     }
 
     @Override
     public SkillResult use(Hero hero, String[] args) {
         int radius = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 10, false) +
                 (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0.0, false) * hero.getLevel()));
         radius = radius > 0 ? radius : 0;
         int radiusSquared = (int) Math.pow(radius, 2);
         int duration = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 10000, false) +
                 (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getLevel())) / 50;
         duration = duration > 0 ? duration : 0;
         Player player = hero.getPlayer();
         Location location = player.getLocation();
         int damage = SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 0, false);
         int exp = SkillConfigManager.getUseSetting(hero, this, "exp-per-blinded-player", 0, false);
         for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
             if ((e instanceof Player) && location.distanceSquared(e.getLocation()) < radiusSquared) {
                 Player p = (Player) e;
                if ((!hero.hasParty() || !hero.getParty().isPartyMember(p)) && damageCheck(p, player)) {
                     Hero dHero = plugin.getHeroManager().getHero(p);
                     dHero.addEffect(new DespairEffect(this, duration, player));
                     if (damage > 0) {
                         p.damage(damage, player);
                     }
                     if (exp > 0) {
                         if (hero.hasParty()) {
                             hero.getParty().gainExp(exp, ExperienceType.SKILL, location);
                         } else {
                             hero.gainExp(exp, ExperienceType.SKILL);
                         }
                     }
                 }
                 
             }
         }
         broadcastExecuteText(hero);
         return SkillResult.NORMAL;
     }
 
     public class DespairEffect extends ExpirableEffect {
         private final int time;
         private final Player player;
         public DespairEffect(Skill skill, int duration, Player player) {
             super(skill, "Despair", (long) duration);
             this.time = duration;
             this.player = player;
             this.types.add(EffectType.HARMFUL);
             this.types.add(EffectType.DARK);
         }
         
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
             CraftPlayer p = (CraftPlayer) hero.getPlayer();
             p.getHandle().addEffect(new MobEffect(15, time, 3));
             broadcast(hero.getPlayer().getLocation(), applyText, player.getDisplayName(), hero.getPlayer().getDisplayName(), "Despair");
         }
         
         @Override
         public void remove(Hero hero) {
             super.remove(hero);
             CraftPlayer p = (CraftPlayer) hero.getPlayer();
             p.getHandle().addEffect(new MobEffect(15, 0, 0));
             broadcast(hero.getPlayer().getLocation(), expireText, player.getDisplayName(), hero.getPlayer().getDisplayName(), "Despair");
         }
     }
     
   /*public void loadLists()
   {
     this.buffIds.put(Integer.valueOf(1), MobEffectList.FASTER_MOVEMENT);
     this.buffIds.put(Integer.valueOf(2), MobEffectList.SLOWER_MOVEMENT);
     this.buffIds.put(Integer.valueOf(3), MobEffectList.FASTER_DIG);
     this.buffIds.put(Integer.valueOf(4), MobEffectList.SLOWER_DIG);
     this.buffIds.put(Integer.valueOf(5), MobEffectList.INCREASE_DAMAGE);
     this.buffIds.put(Integer.valueOf(6), MobEffectList.HEAL);
     this.buffIds.put(Integer.valueOf(7), MobEffectList.HARM);
     this.buffIds.put(Integer.valueOf(8), MobEffectList.JUMP);
     this.buffIds.put(Integer.valueOf(9), MobEffectList.CONFUSION);
     this.buffIds.put(Integer.valueOf(10), MobEffectList.REGENERATION);
     this.buffIds.put(Integer.valueOf(11), MobEffectList.RESISTANCE);
     this.buffIds.put(Integer.valueOf(12), MobEffectList.FIRE_RESISTANCE);
     this.buffIds.put(Integer.valueOf(13), MobEffectList.WATER_BREATHING);
     this.buffIds.put(Integer.valueOf(14), MobEffectList.INVISIBILITY);
     this.buffIds.put(Integer.valueOf(15), MobEffectList.BLINDNESS);
     this.buffIds.put(Integer.valueOf(16), MobEffectList.NIGHT_VISION);
     this.buffIds.put(Integer.valueOf(17), MobEffectList.HUNGER);
     this.buffIds.put(Integer.valueOf(18), MobEffectList.WEAKNESS);
     this.buffIds.put(Integer.valueOf(19), MobEffectList.POISON);
   }*/
 
 }
