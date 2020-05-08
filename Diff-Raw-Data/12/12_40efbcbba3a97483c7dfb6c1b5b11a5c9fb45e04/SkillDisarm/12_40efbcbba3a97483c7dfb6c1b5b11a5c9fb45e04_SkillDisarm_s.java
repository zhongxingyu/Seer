 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.HashMap;
 
 import org.bukkit.Material;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 import com.herocraftonline.dev.heroes.util.Util;
 
 public class SkillDisarm extends TargettedSkill {
 
     private String applyText;
     private String expireText;
     private HashMap<Hero, ItemStack> ismap = new HashMap<Hero, ItemStack>();
     private PlayerListener pListener;
 
     public SkillDisarm(Heroes plugin) {
         super(plugin, "Disarm");
         setDescription("Disarm your target");
         setUsage("/skill disarm <target>");
         setArgumentRange(0, 1);
         setTypes(SkillType.PHYSICAL, SkillType.DEBUFF, SkillType.HARMFUL);
         setIdentifiers("skill disarm");
 
         pListener = new SkillPlayerListener();
         registerEvent(Type.PLAYER_ITEM_HELD, pListener, Priority.Highest);
         registerEvent(Type.PLAYER_PICKUP_ITEM, pListener, Priority.Highest);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty(Setting.DURATION.node(), 3000);
         node.setProperty(Setting.APPLY_TEXT.node(), "%target% was disarmed!");
         node.setProperty(Setting.EXPIRE_TEXT.node(), "%target% has found his weapon again!");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
         applyText = getSetting(null, Setting.APPLY_TEXT.node(), "%target% has stopped regenerating mana!").replace("%target%", "$1");
         expireText = getSetting(null, Setting.EXPIRE_TEXT.node(), "%target% is once again regenerating mana!").replace("%target%", "$1");
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
 
         if (!(target instanceof Player)) {
             Messaging.send(player, "You must target another player!");
             return false;
         }
 
         Hero tHero = plugin.getHeroManager().getHero((Player) target);
 
         if (!Util.isWeapon(player.getItemInHand().getType())) {
             Messaging.send(hero.getPlayer(), "You cannot disarm bare hands!");
             return false;
         }
 
         if (ismap.containsKey(tHero)) {
             Messaging.send(hero.getPlayer(), "%target% is already disarmed.");
             return false;
         }
 
         int duration = getSetting(hero.getHeroClass(), Setting.DURATION.node(), 500);
         tHero.addEffect(new DisarmEffect(this, duration));
         broadcastExecuteText(hero, target);
         return true;
     }
 
     public class DisarmEffect extends ExpirableEffect {
 
         public DisarmEffect(Skill skill, long duration) {
             super(skill, "Disarm", duration);
             this.types.add(EffectType.HARMFUL);
             this.types.add(EffectType.DISARM);
         }
 
         @Override
         public void apply(Hero hero) {
             Player player = hero.getPlayer();
             ItemStack is = player.getItemInHand();
            if (!ismap.containsKey(hero)) {
                ismap.put(hero, is);
                player.setItemInHand(null);
                super.apply(hero);
                broadcast(player.getLocation(), applyText, player.getDisplayName());
             }
         }
 
         @Override
         public void remove(Hero hero) {
             super.remove(hero);
             Player player = hero.getPlayer();
             if (ismap.containsKey(hero)) {
                 ItemStack is = ismap.get(hero);
                 player.getInventory().addItem(is);
                 ismap.remove(hero);
             }
             broadcast(player.getLocation(), expireText, player.getDisplayName());
         }
     }
 
     public class SkillPlayerListener extends PlayerListener {
 
         @Override
         public void onItemHeldChange(PlayerItemHeldEvent event) {
             Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
             if (!hero.hasEffectType(EffectType.DISARM))
                 return;
 
             Inventory inv = event.getPlayer().getInventory();
             Material mat = inv.getItem(event.getNewSlot()).getType();
             // Swap the items back into their original locations
             if (Util.isWeapon(mat)) {
                 ItemStack carry = inv.getItem(event.getNewSlot());
                 inv.setItem(event.getNewSlot(), inv.getItem(event.getPreviousSlot()));
                 inv.setItem(event.getPreviousSlot(), carry);
             }
         }
 
         @Override
         public void onPlayerPickupItem(PlayerPickupItemEvent event) {
             if (event.isCancelled())
                 return;
 
             Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
             if (hero.hasEffectType(EffectType.DISARM) && Util.isWeapon(event.getItem().getItemStack().getType())) {
                 event.setCancelled(true);
             }
         }
     }
 }
