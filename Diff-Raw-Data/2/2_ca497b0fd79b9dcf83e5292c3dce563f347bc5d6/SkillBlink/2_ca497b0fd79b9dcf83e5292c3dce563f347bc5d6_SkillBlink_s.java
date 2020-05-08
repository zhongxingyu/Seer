 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.util.BlockIterator;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.SkillResult;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 import com.herocraftonline.dev.heroes.util.Util;
 
 public class SkillBlink extends ActiveSkill {
 
     public SkillBlink(Heroes plugin) {
         super(plugin, "Blink");
         setDescription("Teleports you up to $1 blocks away.");
         setUsage("/skill blink");
         setArgumentRange(0, 0);
         setIdentifiers("skill blink");
         setTypes(SkillType.SILENCABLE, SkillType.TELEPORT);
         
         Bukkit.getServer().getPluginManager().registerEvents(new SkillPlayerListener(this), plugin);
     }
 
     @Override
     public ConfigurationSection getDefaultConfig() {
         ConfigurationSection node = super.getDefaultConfig();
         node.set(Setting.MAX_DISTANCE.node(), 6);
         node.set("restrict-ender-perls", true);
         return node;
     }
 
     @Override
     public SkillResult use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         Location loc = player.getLocation();
         if (loc.getBlockY() > loc.getWorld().getMaxHeight() || loc.getBlockY() < 1) {
             Messaging.send(player, "The void prevents you from blinking!");
             return SkillResult.FAIL;
         }
         int distance = SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE, 6, false);
         Block prev = null;
         Block b;
         BlockIterator iter = null;
         try {
             iter = new BlockIterator(player, distance);
         } catch (IllegalStateException e) {
             Messaging.send(player, "There was an error getting your blink location!");
             return SkillResult.INVALID_TARGET_NO_MSG;
         }
         while (iter.hasNext()) {
             b = iter.next();
             if (Util.transparentBlocks.contains(b.getType()) && (Util.transparentBlocks.contains(b.getRelative(BlockFace.UP).getType()) || Util.transparentBlocks.contains(b.getRelative(BlockFace.DOWN).getType()))) {
                 prev = b;
             } else {
                 break;
             }
         }
         if (prev != null) {
             Location teleport = prev.getLocation().clone();
             // Set the blink location yaw/pitch to that of the player
             teleport.setPitch(player.getLocation().getPitch());
             teleport.setYaw(player.getLocation().getYaw());
             player.teleport(teleport);
             return SkillResult.NORMAL;
         } else {
             Messaging.send(player, "No location to blink to.");
             return SkillResult.INVALID_TARGET_NO_MSG;
         }
     }
 
     @Override
     public String getDescription(Hero hero) {
         int distance = SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE, 6, false);
         return getDescription().replace("$1", distance + "");
     }
     
     public class SkillPlayerListener implements Listener {
 
         private final Skill skill;
         
         public SkillPlayerListener(Skill skill) {
             this.skill = skill;
         }
         
         @EventHandler(priority = EventPriority.LOWEST)
         public void onPlayerTeleport(PlayerTeleportEvent event) {
             if (event.isCancelled()) {
                 return;
             }
             Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
            if (!hero.hasEffect(getName()) && event.getCause() == TeleportCause.ENDER_PEARL && hero.canUseSkill(skill) && SkillConfigManager.getUseSetting(hero, skill, "restrict-ender-pearl", true)) {
                 event.setCancelled(true);
             }
         }
         
     }
 }
