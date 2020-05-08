 package net.mctitan.infraction.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import net.mctitan.infraction.Infraction;
 import net.mctitan.infraction.InfractionManager;
 import net.mctitan.infraction.InfractionPerm;
 import net.mctitan.infraction.InfractionPlugin;
 
 /**
  * Commands that administrate the given infractions
  * 
  * @author mindless728
  */
 public class AdminCommands implements CommandExecutor {
     InfractionManager manager;
     InfractionPlugin plugin;
     
     public AdminCommands() {
         manager = InfractionManager.getInstance();
         plugin = InfractionPlugin.getInstance();
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command command, String Label, String [] args) {
         if(Label.equals("infracts")) {
             if(args.length == 0)
                 infracts(sender, sender.getName(), "1");
             else if(args.length == 1)
                 infracts(sender, args[0], "1");
             else if(args.length == 2)
                 infracts(sender, args[0], args[1]);
         } else if(Label.equals("pardon")) {
             if(args.length == 1)
                 pardon(sender, args[0], "1");
             else if(args.length == 2)
                 pardon(sender, args[0], args[1]);
         } else if(Label.equals("delete")) {
             if(args.length == 1)
                 delete(sender, args[0], "1");
             else if(args.length == 2)
                 delete(sender, args[0], args[1]);
         } else if(Label.equals("deleteall") && args.length == 1) {
             deleteAll(sender, args[0]);
         } else if(Label.equals("check")) {
             if(args.length == 1)
                 check(sender, args[0], "1");
             else if(args.length == 2)
                 check(sender, args[0], args[1]);
         }
         
         return true;
     }
     
     public void infracts(CommandSender sender, String name, String spage) {
         Player player = plugin.getServer().getPlayer(name);
         if(player != null)
             name = player.getName();
         
         if(!sender.getName().equals(name) &&
            (!sender.hasPermission(InfractionPerm.MODERATOR.toString()) &&
            !sender.hasPermission(InfractionPerm.ADMIN.toString()))) {
             sender.sendMessage(ChatColor.RED+"You do not have permissions to do that");
             return;
         }
         
         int page = 1;
         try {
             page = Integer.parseInt(spage);
         } catch(Exception e) {
             sender.sendMessage(ChatColor.RED+"Page given was not a number");
             return;
         }
         
         for(String s : manager.getInfractionOutput(name, page))
             sender.sendMessage(s);
     }
     
     public void pardon(CommandSender sender, String name, String sid) {
         Player player = plugin.getServer().getPlayer(name);
         if(player != null)
             name = player.getName();
         
         if(sender.getName().equals(name)) {
             sender.sendMessage(ChatColor.RED+"You cannot pardon yourself");
             return;
         }
         
         if(!sender.hasPermission(InfractionPerm.MODERATOR.toString()) &&
            !sender.hasPermission(InfractionPerm.ADMIN.toString())) {
             sender.sendMessage(ChatColor.RED+"You do not have permissions to do that");
             return;
         }
         
         int id = 1;
         try {
             id = Integer.parseInt(sid);
         } catch(Exception e) {
             sender.sendMessage(ChatColor.RED+"ID given was not a number");
             return;
         }
         
         Infraction infract = manager.pardonInfraction(sender.getName(), name, id);
         
         if(infract == null) {
             sender.sendMessage(ChatColor.GREEN+"That infraction already pardoned or doesn't exist");
             return;
         }
         
         sender.sendMessage(infract.getOutput(sender.getName()));
         for(Player p : plugin.getServer().getOnlinePlayers())
             if(!p.getName().equals(sender.getName()))
                p.sendRawMessage(infract.getOutput(name));
     }
     
     public void delete(CommandSender sender, String name, String sid) {
         Player player = plugin.getServer().getPlayer(name);
         if(player != null)
             name = player.getName();
         
         if(!sender.hasPermission(InfractionPerm.ADMIN.toString())) {
             sender.sendMessage(ChatColor.RED+"You do not have permissions to do that");
             return;
         }
         
         int id = 1;
         try {
             id = Integer.parseInt(sid);
         } catch(Exception e) {
             sender.sendMessage(ChatColor.RED+"ID given was not a number");
             return;
         }
         
         Infraction infract = manager.deleteInfraction(sender.getName(), name, id);
         if(infract == null) {
             sender.sendMessage(ChatColor.RED+"Infraction not found, cannot delete");
             return;
         }
         
         sender.sendMessage(ChatColor.GREEN+"Deleted infraction "+ChatColor.GOLD+id+
                            ChatColor.GREEN+" on "+ChatColor.RED+name);
     }
     
     public void deleteAll(CommandSender sender, String name) {
         //@TODO deleteall command
     }
     
     public void check(CommandSender sender, String name, String spage) {
         Player player = plugin.getServer().getPlayer(name);
         if(player != null)
             name = player.getName();
         
         if(!sender.hasPermission(InfractionPerm.ADMIN.toString())) {
             sender.sendMessage(ChatColor.RED+"You do not have permissions to do that");
             return;
         }
         
         int page = 1;
         try {
             page = Integer.parseInt(spage);
         } catch(Exception e) {
             sender.sendMessage(ChatColor.RED+"Page given was not a number");
             return;
         }
         
         for(String s : manager.getInfractionOutput(name, page, true))
             sender.sendMessage(s);
     }
 }
