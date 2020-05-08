 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.SkillResult;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.effects.PeriodicDamageEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 import com.herocraftonline.dev.heroes.util.Util;
 
 public class SkillAssassinsBlade extends ActiveSkill {
 
     private String applyText;
     private String expireText;
 
     public SkillAssassinsBlade(Heroes plugin) {
         super(plugin, "AssassinsBlade");
         setDescription("You poison your blade which will deal an extra $1 damage every $2 seconds.");
         setUsage("/skill ablade");
         setArgumentRange(0, 0);
         setIdentifiers("skill ablade", "skill assassinsblade");
         setTypes(SkillType.BUFF);
         Bukkit.getServer().getPluginManager().registerEvents(new SkillDamageListener(this), plugin);
     }
 
     @Override
     public ConfigurationSection getDefaultConfig() {
         ConfigurationSection node = super.getDefaultConfig();
         node.set("weapons", Util.swords);
         node.set("buff-duration", 600000); // 10 minutes in milliseconds
         node.set("poison-duration", 10000); // 10 seconds in milliseconds
         node.set(Setting.PERIOD.node(), 2000); // 2 seconds in milliseconds
         node.set("tick-damage", 2);
         node.set("attacks", 1); // How many attacks the buff lasts for.
         node.set(Setting.APPLY_TEXT.node(), "%target% is poisoned!");
         node.set(Setting.EXPIRE_TEXT.node(), "%target% has recovered from the poison!");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
         applyText = SkillConfigManager.getRaw(this, Setting.APPLY_TEXT, "%target% is poisoned!").replace("%target%", "$1");
         expireText = SkillConfigManager.getRaw(this, Setting.EXPIRE_TEXT, "%target% has recovered from the poison!").replace("%target%", "$1");
     }
 
     @Override
     public SkillResult use(Hero hero, String[] args) {
         long duration = SkillConfigManager.getUseSetting(hero, this, "buff-duration", 600000, false);
         int numAttacks = SkillConfigManager.getUseSetting(hero, this, "attacks", 1, false);
         hero.addEffect(new AssassinBladeBuff(this, duration, numAttacks));
         broadcastExecuteText(hero);
         return SkillResult.NORMAL;
     }
 
     public class AssassinBladeBuff extends ExpirableEffect {
 
         private int applicationsLeft = 1;
 
         public AssassinBladeBuff(Skill skill, long duration, int numAttacks) {
             super(skill, "PoisonBlade", duration);
             this.applicationsLeft = numAttacks;
             this.types.add(EffectType.BENEFICIAL);
             this.types.add(EffectType.POISON);
         }
 
         /**
          * @return the applicationsLeft
          */
         public int getApplicationsLeft() {
             return applicationsLeft;
         }
 
         @Override
         public void remove(Hero hero) {
             super.remove(hero);
             Messaging.send(hero.getPlayer(), "Your blade is no longer poisoned!");
         }
 
         /**
          * @param applicationsLeft
          *            the applicationsLeft to set
          */
         public void setApplicationsLeft(int applicationsLeft) {
             this.applicationsLeft = applicationsLeft;
         }
     }
 
     public class AssassinsPoison extends PeriodicDamageEffect {
 
         public AssassinsPoison(Skill skill, long period, long duration, int tickDamage, Player applier) {
             super(skill, "AssassinsPoison", period, duration, tickDamage, applier);
             this.types.add(EffectType.POISON);
         }
 
         @Override
         public void apply(LivingEntity lEntity) {
             super.apply(lEntity);
             broadcast(lEntity.getLocation(), applyText, Messaging.getLivingEntityName(lEntity).toLowerCase());
         }
 
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), applyText, player.getDisplayName());
         }
 
         @Override
         public void remove(LivingEntity lEntity) {
             super.remove(lEntity);
             broadcast(lEntity.getLocation(), expireText, Messaging.getLivingEntityName(lEntity).toLowerCase());
         }
 
         @Override
         public void remove(Hero hero) {
             super.remove(hero);
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), expireText, player.getDisplayName());
         }
     }
 
     public class SkillDamageListener implements Listener {
 
         private final Skill skill;
 
         public SkillDamageListener(Skill skill) {
             this.skill = skill;
         }
 
         @EventHandler(priority = EventPriority.MONITOR)
         public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || !(event instanceof EntityDamageByEntityEvent) || event.getDamage() == 0) {
                 return;
             }
             
             // If our target isn't a creature or player lets exit
             if (!(event.getEntity() instanceof LivingEntity) && !(event.getEntity() instanceof Player)) {
                 return;
             }
 
             EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
             if (!(subEvent.getDamager() instanceof Player)) {
                 return;
             }
 
             Player player = (Player) subEvent.getDamager();
             ItemStack item = player.getItemInHand();
             Hero hero = plugin.getHeroManager().getHero(player);
             if (!SkillConfigManager.getUseSetting(hero, skill, "weapons", Util.swords).contains(item.getType().name())) {
                 return;
             }
 
             if (hero.hasEffect("PoisonBlade")) {
                 long duration = SkillConfigManager.getUseSetting(hero, skill, "poison-duration", 10000, false);
                 long period = SkillConfigManager.getUseSetting(hero, skill, Setting.PERIOD, 2000, false);
                 int tickDamage = SkillConfigManager.getUseSetting(hero, skill, "tick-damage", 2, false);
                 AssassinsPoison apEffect = new AssassinsPoison(skill, period, duration, tickDamage, player);
                 Entity target = event.getEntity();
                 if (event.getEntity() instanceof Player) {
                     Hero targetHero = plugin.getHeroManager().getHero((Player) target);
                     targetHero.addEffect(apEffect);
                     checkBuff(hero);
                 } else if (target instanceof LivingEntity) {
                     plugin.getEffectManager().addEntityEffect((LivingEntity) target, apEffect);
                     checkBuff(hero);
                 }
             }
         }
 
         private void checkBuff(Hero hero) {
             AssassinBladeBuff abBuff = (AssassinBladeBuff) hero.getEffect("PoisonBlade");
             abBuff.applicationsLeft -= 1;
             if (abBuff.applicationsLeft < 1) {
                 hero.removeEffect(abBuff);
             }
         }
     }
 
     @Override
     public String getDescription(Hero hero) {
         int damage = SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE, 2, false);
         double seconds = SkillConfigManager.getUseSetting(hero, this, "poison-duration", 10000, false) / 1000.0;
         String s = getDescription().replace("$1", damage + "").replace("$2", seconds + "");
         return s;
     }
 }
