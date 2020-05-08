 /*
  * SortalExecutor.java
  * 
  * Copyright (c) 2012 Lolmewn <info@lolmewn.nl>. 
  * 
  * Sortal is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Sortal is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Sortal.  If not, see <http ://www.gnu.org/licenses/>.
  */
 
 package nl.lolmewn.sortal;
 
 import java.util.HashSet;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 /**
  *
  * @author Lolmewn <info@lolmewn.nl>
  */
 
 public class SortalExecutor implements CommandExecutor {
 
     private Main plugin;
     
     public SortalExecutor(Main aThis) {
         this.plugin = aThis;
     }
     
     private Main getPlugin(){
         return this.plugin;
     }
     
     private Localisation getLocalisation(){
         return this.getPlugin().getSettings().getLocalisation();
     }
 
     public boolean onCommand(CommandSender sender, Command cmnd, String string, String[] args) {
         if(args.length == 0){
             sender.sendMessage("===Sortal===");
             sender.sendMessage("Made by Lolmewn");
             sender.sendMessage("For help: /sortal help");
             return true;
         }
         if(args[0].equalsIgnoreCase("warp") || args[0].equalsIgnoreCase("setwarp")){
             if(!sender.hasPermission("sortal.createwarp")){
                 sender.sendMessage(this.getLocalisation().getNoPerms());
                 return true;
             }
             if(!(sender instanceof Player)){
                 sender.sendMessage(this.getLocalisation().getNoPlayer());
                 return true;
             }
             if(args.length == 1){
                 sender.sendMessage(this.getLocalisation().getCreateNameForgot());
                 return true;
             }
             if(args.length == 2){
                 //Just creating a warp
                 String warp = args[1];
                 if(this.getPlugin().getWarpManager().hasWarp(warp)){
                     sender.sendMessage(this.getLocalisation().getNameInUse(warp));
                     return true;
                 }
                 if(sender instanceof Player && !this.getPlugin().pay((Player)sender, this.getPlugin().getSettings().getWarpCreatePrice())){
                     return true; 
                 }
                 Warp w = this.getPlugin().getWarpManager().addWarp(warp, ((Player)sender).getLocation());
                 w.setOwner(sender.getName());
                 sender.sendMessage(this.getLocalisation().getWarpCreated(warp));
                 return true;
             }
             sender.sendMessage("Too many arguments! Correct usage: /sortal " + args[0] + " " + args[1]);
             return true;
         }
         if(args[0].equalsIgnoreCase("delwarp") || args[0].equalsIgnoreCase("delete")){
             if(!sender.hasPermission("sortal.delwarp")){
                 sender.sendMessage(this.getLocalisation().getNoPerms());
                 return true;
             }
             if(args.length == 1){
                 sender.sendMessage(this.getLocalisation().getDeleteNameForgot());
                 return true;
             }
             for(int i = 1; i < args.length; i++){
                 String warp = args[i];
                 if(this.getPlugin().getWarpManager().hasWarp(warp)){
                     this.getPlugin().getWarpManager().removeWarp(warp);
                     sender.sendMessage(this.getLocalisation().getWarpDeleted(warp));
                     int count = 0;
                     for(SignInfo s : this.getPlugin().getWarpManager().getSigns()){
                         if(s.hasWarp() && s.getWarp().equals(warp)){
                             count++;
                         }
                     }
                     if(count != 0){
                         sender.sendMessage("You've broken " + count + " signs by deleting warp " + warp);
                     }
                     continue;
                 }
                 sender.sendMessage(this.getLocalisation().getWarpNotFound(warp));
                 
             }
             return true;
         }
         if(args[0].equalsIgnoreCase("list")){
             if(!sender.hasPermission("sortal.list")){
                 sender.sendMessage(this.getLocalisation().getNoPerms());
                 return true;
             }
             int page;
             if(args.length == 1){
                 //Get page 1
                 page=1;
             }else{
                 try{
                     page = Integer.parseInt(args[1]);
                 }catch(NumberFormatException e){
                     sender.sendMessage("Expected int, got String: ERR");
                     return true;
                 }
             }
             sender.sendMessage(ChatColor.GREEN + "===Sortal Warps===");
             sender.sendMessage("Page " + page + "/" + (this.getPlugin().getWarpManager().getWarps().size() / 8 + 1));
             int count = -1;
             for(Warp warp : this.getPlugin().getWarpManager().getWarps()){
                 count++;
                 if(count < (page-1)*8 || count >= page*8){
                     continue;
                 }
                 StringBuilder sb = new StringBuilder();
                 sb.append(ChatColor.GREEN).append(warp.getName()).append(": ").append(ChatColor.WHITE).append("W: ");
                 String w = warp.getLocation().getWorld().getName();
                 sb.append(ChatColor.LIGHT_PURPLE).append(w).append(ChatColor.WHITE).append(" X: ");
                String x = Double.toString(warp.getLocation().getX());
                sb.append(ChatColor.RED).append(x.substring(0, x.indexOf(".") + 3 > x.length() ? x.length() : x.indexOf(".") + 3)).append(ChatColor.WHITE).append(" Y: ");
                String y = Double.toString(warp.getLocation().getY());
                sb.append(ChatColor.RED).append(y.substring(0, y.indexOf(".") + 2 > y.length() ? y.length() : y.indexOf(".") + 2)).append(ChatColor.WHITE).append(" Z: ");
                String z = Double.toString(warp.getLocation().getZ());
                sb.append(ChatColor.RED).append(z.substring(0, z.indexOf(".") + 3 > z.length() ? z.length() : z.indexOf(".") + 3)).append(ChatColor.WHITE).append(" Y: ");
                 String f = Float.toString(warp.getLocation().getYaw());
                 sb.append(ChatColor.AQUA).append(f.substring(0, f.indexOf(".") + 2)).append(ChatColor.WHITE).append(" P: ");
                 String p = Double.toString(warp.getLocation().getPitch());
                 sb.append(ChatColor.AQUA).append(p.substring(0, p.indexOf(".") + 3));
                 sender.sendMessage(sb.toString());
             }
             if(count == -1){
                 //no warps found!
                 sender.sendMessage(this.getLocalisation().getNoWarpsFound());
             }
             return true;
         }
         if(args[0].equalsIgnoreCase("version")){
             sender.sendMessage("===Sortal===");
             sender.sendMessage("Version " + this.getPlugin().getSettings().getVersion() + 
                     " build " + this.getPlugin().getDescription().getVersion());
             return true;
         }
         if(args[0].equalsIgnoreCase("unregister")){
             if(!(sender instanceof Player)){
                 sender.sendMessage(this.getLocalisation().getNoPlayer());
                 return true;
             }
             if(!sender.hasPermission("sortal.unregister")){
                 sender.sendMessage(this.getLocalisation().getNoPerms());
                 return true;
             }
             if(this.getPlugin().unregister.contains(sender.getName())){
                 this.getPlugin().unregister.remove(sender.getName());
                 sender.sendMessage("No longer unregistering!");
                 return true;
             }
             if(this.getPlugin().setcost.containsKey(sender.getName())){
                 sender.sendMessage("Please finish setting a cost first! (cancel is /sortal setprice)");
                 return true;
             }
             if(this.getPlugin().register.containsKey(sender.getName())){
                 sender.sendMessage("Please finish registering first! (cancel is /sortal register)");
                 return true;
             }
             this.getPlugin().unregister.add(sender.getName());
             sender.sendMessage("Now punch the sign you wish to be unregistered!");
             return true;
         }
         if(args[0].equalsIgnoreCase("register")){
             if(!(sender instanceof Player)){
                 sender.sendMessage(this.getLocalisation().getNoPlayer());
                 return true;
             }
             if(!sender.hasPermission("sortal.register")){
                 sender.sendMessage(this.getLocalisation().getNoPerms());
                 return true;
             }
             if(args.length == 1){
                 if(this.getPlugin().register.containsKey(sender.getName())){
                     this.getPlugin().register.remove(sender.getName());
                     sender.sendMessage("No longer registering!");
                     return true;
                 }
                 sender.sendMessage("Correct usage: /sortal register <warp>");
                 return true;
             }
             if(this.getPlugin().setcost.containsKey(sender.getName())){
                 sender.sendMessage("Please finish setting a cost first! (cancel is /sortal setprice)");
                 return true;
             }
             if(this.getPlugin().register.containsKey(sender.getName())){
                 sender.sendMessage("Please finish registering first! (cancel is /sortal register)");
                 return true;
             }
             if(this.getPlugin().unregister.contains(sender.getName())){
                 sender.sendMessage("Please finish unregistering first! (cancel is /sortal unregister)");
                 return true;
             }
             String warp = args[1];
             if(!this.getPlugin().getWarpManager().hasWarp(warp)){
                 sender.sendMessage(this.getLocalisation().getWarpNotFound(warp));
                 return true;
             }
             this.getPlugin().register.put(sender.getName(), warp);
             sender.sendMessage("Now punch the sign you wish to be pointing to " + warp);
             return true;
         }
         if(args[0].equalsIgnoreCase("setprice") || args[0].equalsIgnoreCase("setcost") || args[0].equalsIgnoreCase("price")){
             if(!(sender instanceof Player)){
                 sender.sendMessage(this.getLocalisation().getNoPlayer());
                 return true;
             }
             if(!sender.hasPermission("sortal.setprice")){
                 sender.sendMessage(this.getLocalisation().getNoPerms());
                 return true;
             }
             if(args.length == 1){
                 if(this.getPlugin().setcost.containsKey(sender.getName())){
                     this.getPlugin().setcost.remove(sender.getName());
                     sender.sendMessage("No longer setting a cost!");
                     return true;
                 }
                 sender.sendMessage("Correct usages: /sortal " + args[0] + " <cost>");
                 sender.sendMessage("Or /sortal " + args[0] + " warp <warpname> <cost>");
                 return true;
             }
             if(this.getPlugin().setcost.containsKey(sender.getName())){
                 sender.sendMessage("Please finish setting a cost first! (cancel is /sortal setprice)");
                 return true;
             }
             if(this.getPlugin().register.containsKey(sender.getName())){
                 sender.sendMessage("Please finish registering first! (cancel is /sortal register)");
                 return true;
             }
             if(this.getPlugin().unregister.contains(sender.getName())){
                 sender.sendMessage("Please finish unregistering first! (cancel is /sortal unregister)");
                 return true;
             }
             if(args[1].equalsIgnoreCase("warp")){
                if(args.length == 2){
                    sender.sendMessage("Correct usage: /sortal " + args[0] + " warp <warpname> <cost>");
                    return true;
                }
                if(args.length == 3){
                    sender.sendMessage("Correct usage: /sortal " + args[0] + " warp " + args[2] + " <cost>");
                    return true;
                }
                try{
                    int price = Integer.parseInt(args[3]);
                    String warp = args[2];
                    if(!this.getPlugin().getWarpManager().hasWarp(warp)){
                        sender.sendMessage(this.getLocalisation().getWarpNotFound(warp));
                        return true;
                    }
                    this.getPlugin().getWarpManager().getWarp(warp).setPrice(price);
                    sender.sendMessage(this.getLocalisation().getCostSet(Integer.toString(price)));
                    return true;
                }catch(NumberFormatException e){
                    sender.sendMessage("Expected int, got string. <price> should be int!");
                    return true;
                }
             }// End of args[1] = warp
             try{
                 int price = Integer.parseInt(args[1]);
                 this.getPlugin().setcost.put(sender.getName(), price);
                 sender.sendMessage("Now punch the sign you want to be costing " + price);
                 return true;
             }catch(NumberFormatException e){
                 sender.sendMessage("Expected int, got string.");
                 sender.sendMessage("Correct usage: /sortal " + args[0] + " <price>");
                 return true;
             }
         }
         if(args[0].equalsIgnoreCase("convert")){
             if(!sender.isOp()){
                 sender.sendMessage(this.getLocalisation().getNoPerms());
                 return true;
             }
             this.getPlugin().getSettings().setUseMySQL(!this.getPlugin().getSettings().useMySQL());
             if(this.getPlugin().getSettings().useMySQL()){
                 if(!this.getPlugin().initMySQL()){
                     sender.sendMessage("Something went wrong while enabling MySQL! Please check the logs. Using flatfile now.");
                     return true;
                 }
                 this.getPlugin().saveData();
                 this.getPlugin().getSettings().addSettingToConfig(this.getPlugin().settingsFile, "useMySQL", true);
                 sender.sendMessage("All data should have been saved to the MySQL table!");
                 return true;
             }
             this.getPlugin().saveData();
             this.getPlugin().getSettings().addSettingToConfig(this.getPlugin().settingsFile, "useMySQL", false);
             sender.sendMessage("All data should have been saved to flatfiles!");
             return true;
         }
         if(args[0].equalsIgnoreCase("help")){
             sender.sendMessage("===Sortal Help Page===");
             if(sender.hasPermission("sortal.createwarp")){
                 sender.sendMessage("/sortal warp <name> - Creates a warp at your location");
             }
             if(sender.hasPermission("sortal.delwarp")){
                 sender.sendMessage("/sortal delwarp <name> - Deletes warp <name>");
             }
             if(sender.hasPermission("sortal.list")){
                 sender.sendMessage("/sortal list (page) - lists all available warps");
             }
             if(sender.hasPermission("sortal.setprice")){
                 sender.sendMessage("/sortal setprice <cost> - Set a price for a sign");
                 sender.sendMessage("/sortal setprice warp <warp> <cost> - Set a price for a warp");
             }
             if(sender.hasPermission("sortal.register")){
                 sender.sendMessage("/sortal register <warp> - Register a sign to TP to <warp>");
             }
             if(sender.isOp()){
                 sender.sendMessage("/sortal convert - Converts from flat-MySQL or back");
             }
             sender.sendMessage("/sortal version - Tells you the version you are using");
         }
         if(args[0].equalsIgnoreCase("goto") || args[0].equalsIgnoreCase("warpto")){
             if(!(sender instanceof Player)){
                 sender.sendMessage(this.getLocalisation().getNoPlayer());
                 return true;
             }
             if(!sender.hasPermission("sortal.directwarp")){
                 sender.sendMessage(this.getLocalisation().getNoPerms());
                 return true;
             }
             if(args.length == 1){
                 sender.sendMessage("Correct usage: /sortal " + args[0] + " <warp>");
                 return true;
             }
             String warp = args[1];
             if(this.getPlugin().getSettings().isPerWarpPerm()){
                 if(!sender.hasPermission("sortal.directwarp." + warp)){
                     sender.sendMessage(this.getLocalisation().getNoPerms());
                     return true;
                 }
             }
             if(!this.getPlugin().getWarpManager().hasWarp(warp)){
                 sender.sendMessage(this.getLocalisation().getWarpNotFound(warp));
                 return true;
             }
             Warp w = this.getPlugin().getWarpManager().getWarp(warp);
             Player p = (Player)sender;
             p.teleport(w.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
             sender.sendMessage(this.getLocalisation().getPlayerTeleported(w.getName()));
             return true;
         }
         if(args[0].equalsIgnoreCase("setuses") || args[0].equalsIgnoreCase("uses")){
             if(!sender.hasPermission("sortal.setuses")){
                 sender.sendMessage(this.getLocalisation().getNoPerms());
                 return true;
             }
             if(args.length == 1){
                 sender.sendMessage(ChatColor.RED + "ERR: Syntax. Correct usages:");
                 sender.sendMessage("/sortal " + args[0] + " <amount> [player|total]");
                 sender.sendMessage("/sortal " + args[0] + " warp <warp> <amount> [player|total]");
                 return true;
             }
             if(args.length == 2){
                 if(args[1].equalsIgnoreCase("warp")){
                     sender.sendMessage("Correct usage: /sortal " + args[0] + " warp <warp> <amount> [player|total]");
                     return true;
                 }
                 try{
                     int uses = Integer.parseInt(args[1]);
                     this.getPlugin().setuses.put(sender.getName(), "player,"+uses);
                     sender.sendMessage("Now punch the sign you wish to be usable " + uses + " times!");
                     return true;
                 }catch(NumberFormatException e){
                     sender.sendMessage("ERR: Int expected, got string!");
                     return true;
                 }
             }
             if(args.length == 3){
                 if(args[1].equalsIgnoreCase("warp")){
                     sender.sendMessage("Correct usage: /sortal " + args[0] + " warp " + args[2] + " <amount> [player|total]");
                     return true;
                 }
                 String type = args[2];
                 if(type.startsWith("pl")){
                     type = "player";
                 }
                 if(type.startsWith("to")){
                     type = "total";
                 }
                 if(!type.equals("player") && !type.equals("total")){
                     sender.sendMessage("Correct usage: /sortal " + args[0] + " " + args[1] + " [player|total]");
                     return true;
                 }
                 try{
                     int uses = Integer.parseInt(args[1]);
                     this.getPlugin().setuses.put(sender.getName(), type + "," + uses);
                     sender.sendMessage("Now punch the sign you wish to be usable " + uses + " times!");
                     return true;
                 }catch(NumberFormatException e){
                     sender.sendMessage("ERR: Int expected, got string!");
                     return true;
                 }
             }
             if(args[1].equalsIgnoreCase("warp")){
                 if(args.length == 3){
                     sender.sendMessage("Correct usage: /sortal " + args[0] + " warp " +  args[2] + " <amount> [player|total]");
                     return true;
                 }
                 String type = args.length == 5 ? args[4] : "player";
                 if(type.startsWith("pl")){
                     type = "player";
                 }
                 if(type.startsWith("to")){
                     type = "total";
                 }
                 if(!type.equals("player") && !type.equals("total")){
                     sender.sendMessage("Correct usage: /sortal " + args[0] + " warp " + args[2] + " " + args[3] + " [player|total]");
                     return true;
                 }
                 String warp = args[2];
                 if(!this.getPlugin().getWarpManager().hasWarp(warp)){
                     sender.sendMessage(this.getLocalisation().getWarpNotFound(warp));
                     return true;
                 }
                 Warp w = this.getPlugin().getWarpManager().getWarp(warp);
                 try{
                     int uses = Integer.parseInt(args[3]);
                     w.setUses(uses);
                     if(type.startsWith("total")){
                         w.setUsedTotalBased(true);
                     }else{
                         w.setUsedTotalBased(false);
                     }
                     sender.sendMessage(this.getLocalisation().getMaxUsesSet(args[3]));
                     return true;
                 }catch(NumberFormatException e){
                     sender.sendMessage("ERR: Int expected, got string!");
                     return true;
                 }
             }
             sender.sendMessage(ChatColor.RED + "ERR: Syntax. Correct usages:");
             sender.sendMessage("/sortal " + args[0] + " <amount> [player|total]");
             sender.sendMessage("/sortal " + args[0] + " warp <warp> <amount> [player|total]");
             return true;
         }
         if(args[0].equalsIgnoreCase("setprivate") || args[0].equalsIgnoreCase("private")){
             if(!sender.hasPermission("sortal.setprivate")){
                 sender.sendMessage(this.getLocalisation().getNoPerms());
                 return true;
             }
             if(!(sender instanceof Player)){
                 sender.sendMessage(this.getLocalisation().getNoPlayer());
                 return true;
             }
             if(args.length == 1){
                 this.getPlugin().setPrivate.add(sender.getName());
                 sender.sendMessage("Now hit the sign you want to be private!");
                 return true;
             }
             HashSet<String> set = new HashSet<String>();
             for(int i = 1; i < args.length; i++){
                 set.add(args[i]);
             }
             this.getPlugin().setPrivateUsers.put(sender.getName(), set);
             sender.sendMessage("Now hit a private sign you want to add " + (args.length == 2? "this one" : "these"));
             return true;
         }
         if(args[0].equalsIgnoreCase("redeem")){
             
         }
         sender.sendMessage("Unknown syntax, /sortal help for commands");
         return true;
     }
 
 }
