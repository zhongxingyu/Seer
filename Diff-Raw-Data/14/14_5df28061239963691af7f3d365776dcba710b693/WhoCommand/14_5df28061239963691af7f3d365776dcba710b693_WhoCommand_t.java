 package com.herocraftonline.dev.heroes.command.commands;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.BaseCommand;
 import com.herocraftonline.dev.heroes.persistence.Hero;
import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Properties;
 
 public class WhoCommand extends BaseCommand {
 
     public WhoCommand(Heroes plugin) {
         super(plugin);
         name = "Hero Who";
         description = "Checks the players level and other information";
         usage = "/hero who";
         minArgs = 1;
         maxArgs = 1;
         identifiers.add("hero who");
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if (sender instanceof Player) {
             if (plugin.getServer().getPlayer(args[0]) != null) {
                 Properties prop = this.plugin.getConfigManager().getProperties();
                 Player ePlayer = plugin.getServer().getPlayer(args[0]);
                 Hero hero = plugin.getHeroManager().getHero(ePlayer);
                 int level = prop.getLevel(hero.getExp());
 
                 sender.sendMessage("§c-----[ " + "§f" + ePlayer.getName() + "§c ]-----");
                 sender.sendMessage("  §aClass : " + hero.getHeroClass().getName());
                 sender.sendMessage("  §aLevel : " + level);
 
            } else {
                Messaging.send(sender, "Player not online!");
             }
         }
     }
 
 }
