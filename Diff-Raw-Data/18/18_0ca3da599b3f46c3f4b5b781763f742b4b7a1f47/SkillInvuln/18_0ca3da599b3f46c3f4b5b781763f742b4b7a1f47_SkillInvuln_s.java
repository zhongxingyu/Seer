 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 
 public class SkillInvuln extends ActiveSkill {
 
     private String applyText;
     private String expireText;
 
     public SkillInvuln(Heroes plugin) {
         super(plugin);
         setName("Invuln");
         setDescription("Grants total damage immunity");
         setUsage("/skill invuln");
         setMinArgs(0);
         setMaxArgs(0);
         getIdentifiers().add("skill invuln");
 
         registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("duration", 10000);
         node.setProperty("apply-text", "%hero% has become invulnerable!");
         node.setProperty("expire-text", "%hero% is once again vulnerable!");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
         applyText = getSetting(null, "apply-text", "%hero% has become invulnerable!").replace("%hero%", "$1");
         expireText = getSetting(null, "expire-text", "%hero% is once again vulnerable!").replace("%hero%", "$1");
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         broadcastExecuteText(hero);

        int duration = getSetting(hero.getHeroClass(), "duration", 5000);
         hero.addEffect(new InvulnerabilityEffect(this, duration));
 
         return true;
     }
 
     public class InvulnerabilityEffect extends ExpirableEffect {
 
         public InvulnerabilityEffect(Skill skill, long duration) {
            super(skill, "Invulnerability", duration);
         }
 
         @Override
         public void apply(Hero hero) {
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), applyText, player.getDisplayName());
         }
 
         @Override
         public void remove(Hero hero) {
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), expireText, player.getDisplayName());
         }
 
     }
 
     public class SkillEntityListener extends EntityListener {
 
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.isCancelled()) return;
 
             Entity defender = event.getEntity();
             if (defender instanceof Player) {
                 Player player = (Player) defender;
                 Hero hero = plugin.getHeroManager().getHero(player);
                if (hero.hasEffect(getName())) {
                     event.setCancelled(true);
                 }
             }
         }
     }
 }
