 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.SkillResult;
 import com.herocraftonline.dev.heroes.api.SkillResult.ResultType;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillPort extends ActiveSkill {
 
     public SkillPort(Heroes plugin) {
         super(plugin, "Port");
         setDescription("Teleports you and your nearby party to the set location!");
         setUsage("/skill port <location>");
         setArgumentRange(1, 1);
         setIdentifiers("skill port");
         setTypes(SkillType.TELEPORT, SkillType.SILENCABLE);
     }
 
     @Override
     public ConfigurationSection getDefaultConfig() {
         ConfigurationSection node = super.getDefaultConfig();
         node.set(Setting.RADIUS.node(), 10);
         return node;
     }
 
     @Override
     public SkillResult use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
 
         if (args[0].equalsIgnoreCase("list")) {
             for (String n : SkillConfigManager.getUseSettingKeys(hero, this, null)) {
                 String retrievedNode = SkillConfigManager.getUseSetting(hero, this, n, (String) null);
                 if (retrievedNode != null && retrievedNode.split(":").length == 5) {
                     Messaging.send(player, "$1 - $2", n, retrievedNode);
                 }
             }
             return SkillResult.SKIP_POST_USAGE;
         }
 
         String portInfo = SkillConfigManager.getUseSetting(hero, this, args[0].toLowerCase(), (String) null);
         if (portInfo != null) {
             String[] splitArg = portInfo.split(":");
             int levelRequirement = Integer.parseInt(splitArg[4]);
             World world = plugin.getServer().getWorld(splitArg[0]);
             if (world == null) {
                 Messaging.send(player, "That teleport location no longer exists!");
                 return SkillResult.INVALID_TARGET_NO_MSG;
             }
 
             if (hero.getSkillLevel(this) < levelRequirement) {
                 return new SkillResult(ResultType.LOW_LEVEL, true, levelRequirement);
             }
 
             int range = (int) Math.pow(SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS, 10, false), 2);
             Location loc = new Location(world, Double.parseDouble(splitArg[1]), Double.parseDouble(splitArg[2]), Double.parseDouble(splitArg[3]));
             broadcastExecuteText(hero);
             if (!hero.hasParty()) {
                 player.teleport(loc);
                 return SkillResult.NORMAL;
             }
 
            Location castLocation = player.getLocation().clone();
             for (Hero pHero : hero.getParty().getMembers()) {
                 if (!castLocation.getWorld().equals(player.getWorld()))
                     continue;
                 
                 double distance = castLocation.distanceSquared(pHero.getPlayer().getLocation());
                 if (distance <= range) {
                     pHero.getPlayer().teleport(loc);
                 }
             }
 
             return SkillResult.NORMAL;
         } else
             return SkillResult.FAIL;
     }
 }
