 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveEffectSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillTeleport extends ActiveEffectSkill {
 
     public SkillTeleport(Heroes plugin) {
         super(plugin);
         name = "Teleport";
         description = "Teleports you to (roughly) to your party member!";
         usage = "/skill teleport <player>";
         minArgs = 1;
         maxArgs = 1;
         identifiers.add("skill teleport");
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         String playerName = player.getName();
         
        if(!(hero.getParty() != null || hero.getParty().getMembers().size() > 0)) {
             Messaging.send(player, "Sorry, you need to be in a party with players!");
             return false;
         }
         
         Player targetPlayer = plugin.getServer().getPlayer(args[0]);
         if(targetPlayer == null) {
             Messaging.send(player, "Sorry, that player doesn't exist!");
             return false;
         }
         
         if(!hero.getParty().isPartyMember(targetPlayer)) {
             Messaging.send(player, "Sorry, that player isn't in your party!");
             return false;
         }
 
         Location targetLocation = targetPlayer.getLocation().add(Math.random()*((-50 + hero.getLevel()) - (50 - hero.getLevel())), Math.random()*((-50 + hero.getLevel()) - (50 - hero.getLevel())), Math.random()*((-50 + hero.getLevel()) - (50 - hero.getLevel())));
         
         player.teleport(targetLocation);
         
         notifyNearbyPlayers(player.getLocation(), useText, playerName, name);
         return true;
     }
 }
