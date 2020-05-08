 package com.herocraftonline.dev.heroes.command.skills;
 
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Slime;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.classes.HeroClass.Spells;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 
 public class SkillSummon extends Skill {
 
     // TODO: Register this command in Heroes
     public SkillSummon(Heroes plugin) {
         super(plugin);
         name = "Summon";
         description = "Skill - Summon";
         usage = "/summon <monster>";
         minArgs = 1;
         maxArgs = 1;
         identifiers.add("summon");
     }
 
     @Override
     public void use(Player player, String[] args) {
         Hero hero = plugin.getHeroManager().getHero(player);
         HeroClass heroClass = hero.getPlayerClass();
 
         // This spells will have no CD, as it has a max limit and will take mana.
         if (!(heroClass.getSpells().contains(Spells.SUMMON))) {
             plugin.getMessager().send(player, "Sorry, that ability isn't for your class!");
             return;
         }
 
         CreatureType creatureType = CreatureType.fromName(args[0].toUpperCase());
         if (creatureType != null && hero.getSummons().size() <= heroClass.getSummonMax()) {
             Entity spawnedEntity = player.getWorld().spawnCreature(player.getLocation(), creatureType);
             if (spawnedEntity instanceof Creature && spawnedEntity instanceof Ghast && spawnedEntity instanceof Slime) {
                 spawnedEntity.remove();
                 return;
             }
             hero.getSummons().put(spawnedEntity, creatureType);
             plugin.getMessager().send(player, "You have succesfully summoned a " + creatureType.toString());
         }
     }
 }
