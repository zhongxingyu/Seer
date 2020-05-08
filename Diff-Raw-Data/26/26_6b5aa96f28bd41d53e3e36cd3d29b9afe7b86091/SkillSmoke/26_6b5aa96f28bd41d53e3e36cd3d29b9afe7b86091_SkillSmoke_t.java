 package com.herocraftonline.dev.heroes.skill.skills;
 
 import net.minecraft.server.EntityHuman;
 import net.minecraft.server.Packet20NamedEntitySpawn;
 import net.minecraft.server.Packet29DestroyEntity;
 
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.Beneficial;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 
 public class SkillSmoke extends ActiveSkill {
 
     private String applyText;
     private String expireText;
 
     public SkillSmoke(Heroes plugin) {
         super(plugin, "Smoke");
         setDescription("You completely disappear from view");
         setUsage("/skill smoke");
         setArgumentRange(0, 0);
         setIdentifiers(new String[]{"skill smoke"});
         setNotes(new String[]{"Note: Taking damage removes the effect"});
 
         registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("duration", 20000);
         node.setProperty("apply-text", "%hero% vanished in a cloud of smoke!");
         node.setProperty("expire-text", "%hero% reappeared!");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
         applyText = getSetting(null, "apply-text", "%hero% vanished in a cloud of smoke!").replace("%hero%", "$1");
         expireText = getSetting(null, "expire-text", "%hero% reappeard!").replace("%hero%", "$1");
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         broadcastExecuteText(hero);
 
        long duration = getSetting(hero.getHeroClass(), "duration", 20000);
         Player player = hero.getPlayer();
         player.getWorld().playEffect(player.getLocation(), org.bukkit.Effect.SMOKE, 4);
         hero.addEffect(new SmokeEffect(this, duration));
 
         return true;
     }
 
     public class SkillEntityListener extends EntityListener {
 
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.getEntity() instanceof Player) {
                 Player player = (Player) event.getEntity();
                 Hero hero = getPlugin().getHeroManager().getHero(player);
                 if (hero.hasEffect("Smoke")) {
                     hero.removeEffect(hero.getEffect("Smoke"));
                 }
             }
         }
     }
 
     public class SkillPlayerListener extends PlayerListener {
 
         @Override
         public void onPlayerInteract(PlayerInteractEvent event) {
             if (event.getAction() != Action.PHYSICAL) {
                 Player player = event.getPlayer();
                 Hero hero = getPlugin().getHeroManager().getHero(player);
                 if (hero.hasEffect("Smoke")) {
                     hero.removeEffect(hero.getEffect("Smoke"));
                 }
             }
         }
     }
 
     public class SmokeEffect extends ExpirableEffect implements Beneficial {
 
         public SmokeEffect(Skill skill, long duration) {
             super(skill, "Smoke", duration);
         }
 
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
             Player player = hero.getPlayer();
             CraftPlayer craftPlayer = (CraftPlayer) hero.getPlayer();
             // Tell all the logged in Clients to Destroy the Entity - Appears Invisible.
             final Player[] players = getPlugin().getServer().getOnlinePlayers();
             for (Player onlinePlayer : players) {
                 CraftPlayer hostilePlayer = (CraftPlayer) onlinePlayer;
                 hostilePlayer.getHandle().netServerHandler.sendPacket(new Packet29DestroyEntity(craftPlayer.getEntityId()));
             }
 
             broadcast(player.getLocation(), applyText, player.getDisplayName());
         }
 
         @Override
         public void remove(Hero hero) {
             Player player = hero.getPlayer();
             EntityHuman entity = ((CraftPlayer) player).getHandle();
             final Player[] players = getPlugin().getServer().getOnlinePlayers();
             for (Player onlinePlayer : players) {
                 if (onlinePlayer.equals(player)) {
                     continue;
                 }
                 CraftPlayer hostilePlayer = (CraftPlayer) onlinePlayer;
                 hostilePlayer.getHandle().netServerHandler.sendPacket(new Packet20NamedEntitySpawn(entity));
             }
 
             broadcast(player.getLocation(), expireText, player.getDisplayName());
         }
 
     }
 }
