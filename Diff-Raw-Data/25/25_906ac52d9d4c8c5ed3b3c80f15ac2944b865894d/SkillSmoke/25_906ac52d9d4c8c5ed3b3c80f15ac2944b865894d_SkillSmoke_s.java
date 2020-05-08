 package com.herocraftonline.dev.heroes.command.skill.skills;
 
 import net.minecraft.server.Packet20NamedEntitySpawn;
 import net.minecraft.server.Packet29DestroyEntity;
 
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 
 public class SkillSmoke extends ActiveSkill {
 
     public SkillSmoke(Heroes plugin) {
         super(plugin);
         name = "Smoke";
         description = "Skill - smoke";
         usage = "/skill smoke";
         minArgs = 0;
         maxArgs = 0;
         identifiers.add("skill smoke");
 
         registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         CraftPlayer craftPlayer = (CraftPlayer) hero.getPlayer();
        craftPlayer.getHandle().netServerHandler.a(new Packet29DestroyEntity(craftPlayer.getEntityId()));
         hero.getEffects().put(getName(), System.currentTimeMillis() + 10000.0);
         return true;
     }
 
     public class SkillEntityListener extends EntityListener {

         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.getEntity() instanceof Player) {
                 Player player = (Player) event.getEntity();
                 Hero hero = plugin.getHeroManager().getHero(player);
                 CraftPlayer craftPlayer = (CraftPlayer) player;
                 if (hero.getEffects().containsKey(getName())) {
                    craftPlayer.getHandle().netServerHandler.a(new Packet20NamedEntitySpawn());
                 }
             }
         }
     }
 }
