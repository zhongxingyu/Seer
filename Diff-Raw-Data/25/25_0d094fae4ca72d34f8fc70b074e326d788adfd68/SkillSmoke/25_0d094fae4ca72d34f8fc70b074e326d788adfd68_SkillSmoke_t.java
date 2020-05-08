 package com.herocraftonline.dev.heroes.command.skill.skills;
 
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.skill.Skill;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 
 public class SkillSmoke extends Skill{
 
     public SkillSmoke(Heroes plugin) {
         super(plugin);
         name = "Smoke";
         description = "Skill - smoke";
         usage = "/smoke";
         minArgs = 0;
         maxArgs = 0;
         identifiers.add("smoke");
         configs.put("mana", "20");
         configs.put("level", "20");
 
        plugin.getServer().getPluginManager().registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal, plugin);
     }
 
     @Override
     public void use(Player player, String[] args) {
         CraftPlayer player1 = (CraftPlayer)player;
         //player1.getHandle().a.b(new Packet29DestroyEntity(player1.getEntityId())); // This lines erroring tut tut.
         plugin.getHeroManager().getHero(player1).getEffects().put(getName(), System.currentTimeMillis() + 10000);
     }
 
     public class SkillEntityListener extends EntityListener {
 
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             if(event.getEntity() instanceof Player){
                 Player player = (Player) event.getEntity();
                 Hero hero = plugin.getHeroManager().getHero(player);
 
                 if (hero.getEffects().containsKey(getName())) {
                     if (hero.getEffects().get(getName()) > System.currentTimeMillis()) {
                         player.setVelocity(player.getLocation().getDirection().multiply(1.3).setY(0));
                     }
                 }
             }
         }
     }
 }
 
 
 
