 package me.Whatshiywl.heroesskilltree;
 
 import com.herocraftonline.heroes.Heroes;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.herocraftonline.heroes.api.events.*;
 import com.herocraftonline.heroes.characters.Hero;
 import com.herocraftonline.heroes.characters.classes.HeroClass;
 import com.herocraftonline.heroes.characters.effects.Effect;
 import com.herocraftonline.heroes.characters.skill.Skill;
 import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
 import org.bukkit.Bukkit;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 
 public class EventListener implements Listener {
     private static HeroesSkillTree plugin;
     public EventListener(HeroesSkillTree instance) {
         EventListener.plugin = instance;
     }
 
     @EventHandler
     public void onPluginEnable(PluginEnableEvent event) {
         if (event.getPlugin().getDescription().getName().equals("Heroes")) {
             HeroesSkillTree.heroes = (Heroes) event.getPlugin();
         }
     }
 
     @EventHandler
     public void onPluginDisable(PluginDisableEvent event) {
         if (event.getPlugin().getDescription().getName().equals("Heroes")) {
             Bukkit.getPluginManager().disablePlugin(plugin);
         }
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event){
         Player player = event.getPlayer();
         final Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero(player);
         plugin.loadPlayerConfig(player.getName());
         plugin.recalcPlayerPoints(hero, hero.getHeroClass());
         Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             @Override
             public void run() {
                 for (Effect effect : hero.getEffects()) {
                     Skill skill = HeroesSkillTree.heroes.getSkillManager().getSkill(effect.getName());
                     if (skill == null) {
                         continue;
                     }
                     if (plugin.isLocked(hero, skill)) {
                         hero.removeEffect(effect);
                     }
                 }
             }
         }, 1L);
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onLevelChangeEvent(HeroChangeLevelEvent event) {
         final Hero hero = event.getHero();
         plugin.setPlayerPoints(hero, plugin.getPlayerPoints(hero) +
                 ((event.getTo() - event.getFrom()) * plugin.getPointsPerLevel()));
         hero.getPlayer().sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "SkillPoints: " + plugin.getPlayerPoints(hero));
         plugin.savePlayerConfig(hero.getPlayer().getName());
 
 
         Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             @Override
             public void run() {
                 for (Effect effect : hero.getEffects()) {
                     Skill skill = HeroesSkillTree.heroes.getSkillManager().getSkill(effect.getName());
                     if (skill == null) {
                         continue;
                     }
                     if (plugin.isLocked(hero, skill)) {
                         hero.removeEffect(effect);
                     }
                 }
             }
         }, 1L);
 
         /*for(Skill skill : HeroesSkillTree.heroes.getSkillManager().getSkills()){
             if (plugin.isLocked(hero, skill) && hero.hasEffect(skill.getName())) {
                 hero.removeEffect(hero.getEffect(skill.getName()));
             }
         }*/
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onClassChangeEvent(ClassChangeEvent event) {
         /*if(event.getTo() == null) {
             return;
         }*/
         final Hero hero = event.getHero();
         final ClassChangeEvent evt = event;
         /*else {
             plugin.recalcPlayer(hero.getPlayer(), event.getTo());
             plugin.savePlayerConfig(hero.getPlayer().getName());
 
         }*/
         //TODO change this to actually store skills per classes
         /*plugin.resetPlayer(hero.getPlayer());
         for(Skill skill : HeroesSkillTree.heroes.getSkillManager().getSkills()){
             if(plugin.isLocked(hero, skill) && hero.hasEffect(skill.getName())){
                 hero.removeEffect(hero.getEffect(skill.getName()));
             }
         }*/
         Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             @Override
             public void run() {
                 boolean reset = true;
                 if(evt.getTo().isDefault()) {
                     outer: for(HeroClass hClass: HeroesSkillTree.heroes.getClassManager().getClasses()){
                         if(hero.getExperience(hClass)!=0) {
                             reset = false;
                            continue outer;
                         }
                     }
                 }
                 if(reset){
                     plugin.resetPlayer(hero.getPlayer());
                 } else {
                     plugin.recalcPlayerPoints(hero, evt.getTo());
                 }
                 for (Effect effect : hero.getEffects()) {
                     Skill skill = HeroesSkillTree.heroes.getSkillManager().getSkill(effect.getName());
                     if (skill == null) {
                         continue;
                     }
                     if (plugin.isLocked(hero, skill)) {
                         hero.removeEffect(effect);
                     }
                 }
             }
         }, 1L);
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerUseSkill(SkillUseEvent event){
         Hero hero = event.getHero();
         Skill skill = event.getSkill();
         if(plugin.isLocked(event.getHero(), event.getSkill()) && !event.getPlayer().hasPermission("skilltree.override.locked")){
             event.getPlayer().sendMessage(ChatColor.RED + "This skill is still locked! /skillup (skill) to unlock it.");
             event.getHero().hasEffect(event.getSkill().getName());
             event.setCancelled(true);
             return;
         }
         //HEALTH
         int health = (int) ((SkillConfigManager.getUseSetting(hero, skill, "hst-health", 0.0, false)) *
                 (plugin.getSkillLevel(hero, skill) - 1));
         health = health > 0 ? health : 0;
         event.setHealthCost(event.getHealthCost() + health);
 
         //MANA
         int mana = (int) ((SkillConfigManager.getUseSetting(hero, skill, "hst-mana", 0.0, false)) *
                 (plugin.getSkillLevel(hero, skill) - 1));
         mana = mana > 0 ? mana : 0;
         event.setManaCost(event.getManaCost() + mana);
 
         //REAGENT
         int reagent = (int) ((SkillConfigManager.getUseSetting(hero, skill, "hst-reagent", 0.0, false)) *
                 (plugin.getSkillLevel(hero, skill) - 1));
         reagent = reagent > 0 ? reagent : 0;
 
         ItemStack is = event.getReagentCost();
         if(is != null) {
             is.setAmount(event.getReagentCost().getAmount() + reagent);
         }
         event.setReagentCost(is);
 
         //STAMINA
         int stamina = (int) (SkillConfigManager.getUseSetting(hero, skill, "hst-stamina", 0.0, false) *
                 plugin.getSkillLevel(hero, skill) - 1);
         stamina = stamina > 0 ? stamina : 0;
         event.setStaminaCost(event.getStaminaCost() + stamina);
     }
 
     /*@EventHandler(priority = EventPriority.HIGHEST)
     public void onWeaponDamage (WeaponDamageEvent event) {
         if(!(event.getDamager() instanceof Hero)) {
             return;
         }
         Hero hero = (Hero) event.getDamager();
         for(Skill skill : HeroesSkillTree.heroes.getSkillManager().getSkills()){
             if(plugin.isLocked(hero, skill)) {
                 if(hero.hasEffect(skill.getName())){
                     hero.removeEffect(hero.getEffect(skill.getName()));
                     }
                     else if(hero.hasEffect(skill.getName())){
                     //DAMAGE
                     int damage = (int) ((SkillConfigManager.getUseSetting(hero, skill, "hst-damage", 0.0, false)) *
                     (plugin.getSkillLevel(hero, skill) - 1));
                     damage = damage > 0 ? damage : 0;
                     event.setDamage(event.getDamage() + damage);
                 }
             }
         }
     }*/
 }
