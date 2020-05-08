 package com.mrmag518.LogStream.Util;
 
 import com.mrmag518.LogStream.Files.Config;
 import com.mrmag518.LogStream.Files.Players;
 import com.mrmag518.LogStream.LogStream;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CmdHandler {
     private static boolean cancelModification = false;
     
     public static void handleRemove(CommandSender sender, String[] args) {
         if(args.length == 3) {
             if(!PermHandler.hasPermission(true, sender, "logstream.command.remove.others")) {
                 return;
             }
             String type = args[2];
             String victim = args[1];
             if(type.equals("Join") || type.equals("Quit")) {
                 if(Players.hasPersonal(victim)) {
                     Players.removePlayerOT(victim, type);
                     sender.sendMessage(ChatColor.RED + "Removed " + victim + ChatColor.RED + "'s " + type + " modifications!");
                 } else {
                     sender.sendMessage(ChatColor.RED + "You do not have a stream modification!");
                 }
             } else {
                 if(type.equalsIgnoreCase("all")) {
                     Players.removePlayer(victim);
                 } else {
                     sender.sendMessage(ChatColor.RED + "Only acceptable stream types are: 'Join', 'Quit' and 'all'");
                 }
             }
         } else if(args.length == 2){
             if(!PermHandler.hasPermission(true, sender, "logstream.command.remove.self")) {
                 return;
             }
             String victim = sender.getName();
             String type = args[1];
             if(type.equals("Join") || type.equals("Quit")) {
                 if(Players.hasPersonal(victim)) {
                     Players.removePlayerOT(victim, type);
                     sender.sendMessage(ChatColor.RED + "Removed your " + type + " modifications!");
                 } else {
                     sender.sendMessage(ChatColor.RED + "You do not have a stream modification!");
                 }
             } else {
                 if(type.equalsIgnoreCase("all")) {
                     Players.removePlayer(victim);
                 } else {
                     sender.sendMessage(ChatColor.RED + "Only acceptable stream types are: 'Join', 'Quit' and 'all'");
                 }
             }
         } else {
             sender.sendMessage(ChatColor.RED + "Invalid command usage! Correct: /ls remove <Join/Quit/all> [player]");
         }
     }
     
     public static void handleModifyJoin(CommandSender sender, String[] args, boolean isConsole) {
         if(args.length > 1) {
             String a2 = args[1];
             String newOutput = Strings.getFinalArg(args, 2);
             
             cancelModification = false;
             if(isConsole == false) { // Only check limits when it's a player who alters the output.
                 checkLimits(sender, newOutput.toLowerCase());
             }
             
             if(cancelModification == false) {
                 if(a2.equalsIgnoreCase("me") || a2.equalsIgnoreCase(sender.getName())) {
                     if(isConsole == true) {
                         sender.sendMessage("Don't think consoles can join servers :)");
                         sender.sendMessage("Try to select a specific user instead.");
                         return;
                     }
                     if(!PermHandler.hasPermission(true, sender, "logstream.command.modifyjoin.self")) {
                         return;
                     }
                     if(!Players.hasPersonal(sender.getName())) {
                         Players.addPlayer(sender.getName());
                         Players.modifyJoin(sender.getName(), newOutput);
 
                         if(Config.getConfig().getBoolean("PlayerCustomization.Economy.Enabled") == true && Config.getConfig().getBoolean("Support.EnableEconomySupport") == true) {
                             Eco.chargePlayer(sender, newOutput);
                         }
 
                         sender.sendMessage(ChatColor.GREEN + "Your join output has been modified successfully!");
                         sender.sendMessage(ChatColor.GREEN + "New output: " + ChatColor.WHITE + Strings.colorize(Players.getJoin(sender.getName())));
                     } else {
                         Players.modifyJoin(sender.getName(), newOutput);
 
                         if(Config.getConfig().getBoolean("PlayerCustomization.Economy.Enabled") == true && Config.getConfig().getBoolean("Support.EnableEconomySupport") == true) {
                             Eco.chargePlayer(sender, newOutput);
                         }
 
                         sender.sendMessage(ChatColor.GREEN + "Your join output has been modified successfully!");
                         sender.sendMessage(ChatColor.GREEN + "New output: " + ChatColor.WHITE + Strings.colorize(Players.getJoin(sender.getName())));
                     }
                 } else {
                     if(isConsole == false) {
                         if(!PermHandler.hasPermission(true, sender, "logstream.command.modifyjoin.others")) {
                             return;
                         }
                     }
                     if(!Players.hasPersonal(a2)) {
                         Players.addPlayer(a2);
                         Players.modifyJoin(a2, newOutput);
                         sender.sendMessage(ChatColor.GREEN + a2 + "'s join output has been modified successfully!");
                         sender.sendMessage(ChatColor.GREEN + "New output: " + ChatColor.WHITE + Strings.colorize(Players.getJoin(a2)));
                     } else {
                         Players.modifyJoin(a2, newOutput);
                         sender.sendMessage(ChatColor.GREEN + a2 + "'s join output has been modified successfully!");
                         sender.sendMessage(ChatColor.GREEN + "New output: " + ChatColor.WHITE + Strings.colorize(Players.getJoin(a2)));
                     }
                 }
             }
         } else {
             sender.sendMessage(ChatColor.RED + "Invalid usage of arguments!");
         }
     }
     
     public static void handleModifyQuit(CommandSender sender, String[] args, boolean isConsole) {
         if(args.length > 1) {
             String a2 = args[1];
             String newOutput = Strings.getFinalArg(args, 2);
             
             cancelModification = false;
             if(isConsole == false) { // Only check limits when it's a player who alters the output.
                 checkLimits(sender, newOutput.toLowerCase());
             }
             
             if(cancelModification == false) {
                 if(a2.equalsIgnoreCase("me") || a2.equalsIgnoreCase(sender.getName())) {
                     if(isConsole == true) {
                         sender.sendMessage("Don't think consoles can quit servers :)");
                         sender.sendMessage("Try to select a specific user instead.");
                         return;
                     }
                     if(!PermHandler.hasPermission(true, sender, "logstream.command.modifyquit.self")) {
                         return;
                     }
                     if(!Players.hasPersonal(sender.getName())) {
                         Players.addPlayer(sender.getName());
                         Players.modifyQuit(sender.getName(), newOutput);
 
                         if(Config.getConfig().getBoolean("PlayerCustomization.Economy.Enabled") == true && Config.getConfig().getBoolean("Support.EnableEconomySupport") == true) {
                             Eco.chargePlayer(sender, newOutput);
                         }
 
                         sender.sendMessage(ChatColor.GREEN + "Your quit output has been modified successfully!");
                         sender.sendMessage(ChatColor.GREEN + "New output: " + ChatColor.WHITE + Strings.colorize(Players.getQuit(sender.getName())));
                     } else {
                         Players.modifyQuit(sender.getName(), newOutput);
 
                         if(Config.getConfig().getBoolean("PlayerCustomization.Economy.Enabled") == true && Config.getConfig().getBoolean("Support.EnableEconomySupport") == true) {
                             Eco.chargePlayer(sender, newOutput);
                         }
 
                         sender.sendMessage(ChatColor.GREEN + "Your quit output has been modified successfully!");
                         sender.sendMessage(ChatColor.GREEN + "New output: " + ChatColor.WHITE + Strings.colorize(Players.getQuit(sender.getName())));
                     }
                 } else {
                     if(isConsole == false) {
                         if(!PermHandler.hasPermission(true, sender, "logstream.command.modifyquit.others")) {
                             return;
                         }
                     }
                     if(!Players.hasPersonal(a2)) {
                         Players.addPlayer(a2);
                         Players.modifyQuit(a2, newOutput);
                         sender.sendMessage(ChatColor.GREEN + a2 + "'s quit output has been modified successfully!");
                         sender.sendMessage(ChatColor.GREEN + "New output: " + ChatColor.WHITE + Strings.colorize(Players.getQuit(a2)));
                     } else {
                         Players.modifyQuit(a2, newOutput);
                         sender.sendMessage(ChatColor.GREEN + a2 + "'s quit output has been modified successfully!");
                         sender.sendMessage(ChatColor.GREEN + "New output: " + ChatColor.WHITE + Strings.colorize(Players.getQuit(a2)));
                     }
                 }
             }
         } else {
             sender.sendMessage(ChatColor.RED + "Invalid usage of arguments!");
         }
     }
     
     private static void checkLimits(CommandSender sender, String output) {
         if(Config.getConfig().getBoolean("Support.EnablePlayerCustomization") != true) {
             sender.sendMessage(ChatColor.RED + "Player customization is not enabled!");
             cancelModification = true;
             return;
         }
         
         if(Config.getConfig().getBoolean("PlayerCustomization.Variables.OutputMustContainPlayername") == true) {
             if(!output.contains("%player%") && !output.contains("%dispname%")) {
                 sender.sendMessage(ChatColor.RED + "The new customization must contain the %player% or %dispname% variable!");
                 cancelModification = true;
                 return;
             }
         }
         
         if(!sender.hasPermission("logstream.color.*")) {
             for(String color : LogStream.colorCodes) {
                 if(output.contains(color)) {
                     String disallowedColors = Config.getConfig().getString("PlayerCustomization.Colors.Disallowed");
                     if(disallowedColors.contains(color + ",")) {
                         if(!PermHandler.hasPermission(false, sender, "logstream.color." + color)) {
                             sender.sendMessage(ChatColor.RED + "You are not allowed to take usage of the color code: " + ChatColor.GRAY + color);
                             cancelModification = true;
                             return;
                         }
                     }
                 }
             }
         }
         
         if(output == null || output.equals("") || output.equals(" ")) {
             if(Config.getConfig().getBoolean("PlayerCustomization.Misc.AllowEmptyOutput") != true) {
                 if(!PermHandler.hasPermission(false, sender, "logstream.bypass.emptyoutput")) {
                     sender.sendMessage(ChatColor.RED + "You're not allowed to make empty customizations!");
                     cancelModification = true;
                     return;
                 }
             }
         }
         
         int length = output.length();
         if(length > Config.getMaxLength()) {
             if(!PermHandler.hasPermission(false, sender, "logstream.bypass.maxlength")) {
                 sender.sendMessage(ChatColor.RED + "Your modified output length were longer than what allowed! Max: " 
                         + ChatColor.GRAY + Config.getMaxLength() + ChatColor.RED + " Your output's length: " + ChatColor.GRAY + length);
                 cancelModification = true;
                 return;
             }
         }
         
         if(length < Config.getMinLength()) {
             if(!PermHandler.hasPermission(false, sender, "logstream.bypass.minlength")) {
                 sender.sendMessage(ChatColor.RED + "Your modified output length were shorter than what allowed! Min: " 
                         + ChatColor.GRAY + Config.getMinLength() + ChatColor.RED + " Your output's length: " + ChatColor.GRAY + length);
                 cancelModification = true;
                 return;
             }
         }
         
         Strings.scan(output, (Player)sender);
         
         if(Strings.variablesAmount > Config.getMaxVariables()) {
             if(!PermHandler.hasPermission(false, sender, "logstream.bypass.maxvariables")) {
                 sender.sendMessage(ChatColor.RED + "Your modified output contained more variables than what allowed! Max: " 
                         + ChatColor.GRAY + Config.getMaxVariables() + ChatColor.RED + " Your variables: " + ChatColor.GRAY + Strings.variablesAmount);
                 cancelModification = true;
                 return;
             }
         }
 
         if(Strings.variablesAmount < Config.getMinVariables()) {
             if(!PermHandler.hasPermission(false, sender, "logstream.bypass.minvariables")) {
                 sender.sendMessage(ChatColor.RED + "Your modified output contained less variables than what allowed! Min: " 
                         + ChatColor.GRAY + Config.getMinVariables() + ChatColor.RED + " Your variables: " + ChatColor.GRAY + Strings.variablesAmount);
                 cancelModification = true;
                 return;
             }
         }
         
         Strings.variablesAmount = 0;
         
         for(String word : Config.getConfig().getStringList("PlayerCustomization.Misc.DisallowedStrings")) {
             if(word != null) {
                 if(output.contains(word.toLowerCase())) {
                     if(!PermHandler.hasPermission(false, sender, "logstream.bypass.disallowedwords")) {
                         sender.sendMessage(ChatColor.RED + "Your new output contained a disallowed string '" + ChatColor.GRAY + word + ChatColor.RED + "'!");
                         cancelModification = true;
                         return;
                     }
                 }
             }
         }
     }
     
     public static void handleAdd(CommandSender sender, String[] args) {
         if(!PermHandler.hasPermission(true, sender, "logstream.command.generate")) {
             return;
         }
         if(args.length > 3 || args.length < 2) {
             sender.sendMessage(ChatColor.RED + "Invalid usage of arguments!");
             return;
         }
         String type = "";
         String victim = args[1].toLowerCase();
         
         if(args.length == 2) {
             type = null;
         } else {
             type = args[2];
         }
         
         if(type == null || type.equals("")) {
             if(!Players.hasPersonal(victim)) {
                 Players.addPlayer(victim);
                 sender.sendMessage(ChatColor.GREEN + victim + ChatColor.GOLD + " has been generated!");
             } else {
                 sender.sendMessage(ChatColor.GRAY + victim + ChatColor.RED + " is already generated!");
             }
         } else {
             if(type.equals("Join") || type.equals("Quit")) {
                 if(!Players.hasPersonal(victim)) {
                     Players.addPlayerOT(victim, type);
                     sender.sendMessage(ChatColor.GREEN + victim + ChatColor.GOLD + "'s " + ChatColor.GREEN + type + ChatColor.GOLD + " field has been generated!");
                 } else {
                     sender.sendMessage(ChatColor.GRAY + victim + ChatColor.RED + " already has the " + ChatColor.GRAY + type + ChatColor.RED + " field generated!");
                 }
             } else {
                 sender.sendMessage(ChatColor.RED + "You must choose a valid output type! (Either 'Join' or 'Quit')");
                 sender.sendMessage(ChatColor.RED + "Remember the type is case sensitive.");
             }
         }
     }
     
     public static void displayCharges(CommandSender sender, String[] args, boolean isConsole) {
         if(Config.getConfig().getBoolean("PlayerCustomization.Economy.Enabled") == false) {
             sender.sendMessage(ChatColor.RED + "Economy feature is disabled!");
             return;
         }
         if(!PermHandler.hasPermission(true, sender, "logstream.command.charges")) {
             return;
         }
         boolean hasOutput = false;
         String output = "";
         
         if(args.length >= 2) {
             output = Strings.getFinalArg(args, 1);
             hasOutput = true;
         }
         
         sender.sendMessage("-----------------------------------------------------");
         sender.sendMessage(ChatColor.GREEN + "Main charge: " + ChatColor.GOLD + Eco.getCurrencySymbol() + Eco.getMainCharge());
         sender.sendMessage(ChatColor.GREEN + "Charge increase per letter: " + ChatColor.GOLD + Eco.getCurrencySymbol() + Eco.getIncreasePerLetter());
         sender.sendMessage(ChatColor.GREEN + "Charge increase per variable: " + ChatColor.GOLD + Eco.getCurrencySymbol() + Eco.getIncreasePerVariable());
         sender.sendMessage(ChatColor.GREEN + "Charge increase per color code: " + ChatColor.GOLD + Eco.getCurrencySymbol() + Eco.getIncreasePerColorCode());
         if(hasOutput == true) {
             sender.sendMessage("--");
             sender.sendMessage(ChatColor.GREEN + "The output '" + ChatColor.GRAY + output + ChatColor.GREEN + "' would cost " + ChatColor.GOLD + Eco.getCurrencySymbol() 
                     + Eco.getFinalCharge((Player)sender, output));
             sender.sendMessage("--");
         }
         if(isConsole == false) {
             sender.sendMessage("You got " + ChatColor.AQUA + Eco.getCurrencySymbol() + Eco.eco.getBalance(sender.getName()));
         }
         sender.sendMessage("-----------------------------------------------------");
     }
     
     public static void displayVariables(CommandSender sender) {
         if(!PermHandler.hasPermission(true, sender, "logstream.command.variables")) {
             return;
         }
         
         sender.sendMessage("-----------------------------------------------------");
         sender.sendMessage(ChatColor.AQUA + "Variables found in " + ChatColor.BLUE + "LogStream v" + LogStream.version);
         
         sender.sendMessage(ChatColor.YELLOW + "Color codes can be found here: " + ChatColor.WHITE + "http://ess.khhq.net/mc/");
         
         if(Config.isVariableEnabled("player")) {
             sender.sendMessage(ChatColor.GREEN + "%player%" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "The name of the specific player that is involved in the event. ");
         } else {
             sender.sendMessage(ChatColor.RED + "%player%" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "The name of the specific player that is involved in the event. ");
         }
         
         if(Config.isVariableEnabled("group")) {
             sender.sendMessage(ChatColor.GREEN + "%group%" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "The name of the specific group the player is in. ");
         } else {
             sender.sendMessage(ChatColor.RED + "%group%" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "The name of the specific group the player is in. ");
         }
         
         if(Config.isVariableEnabled("date")) {
             sender.sendMessage(ChatColor.GREEN + "%date%" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "The date when the event ocurred. Will display which hour, minute and second too. ");
         } else {
             sender.sendMessage(ChatColor.RED + "%date%" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "The date when the event ocurred. Will display which hour, minute and second too. ");
         }
         
         if(Config.isVariableEnabled("world")) {
             sender.sendMessage(ChatColor.GREEN + "%world%" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "The world the player is located in. ");
         } else {
             sender.sendMessage(ChatColor.RED + "%world%" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "The world the player is located in. ");
         }
         
         if(Config.isVariableEnabled("gamemode")) {
             sender.sendMessage(ChatColor.GREEN + "%gamemode%" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "The gamemode of the player. ");
         } else {
             sender.sendMessage(ChatColor.RED + "%gamemode%" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "The gamemode of the player. ");
         }
         
         if(Config.isVariableEnabled("ip")) {
             sender.sendMessage(ChatColor.GREEN + "%ip%" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "The IP Address of the player. ");
         } else {
             sender.sendMessage(ChatColor.RED + "%ip%" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "The IP Address of the player. ");
         }
         
         if(Config.isVariableEnabled("dispname")) {
             sender.sendMessage(ChatColor.GREEN + "%dispname%" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "The display name of the player. ");
         } else {
             sender.sendMessage(ChatColor.RED + "%dispname%" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "The display name of the player. ");
         }
         
         if(Config.isVariableEnabled("health")) {
             sender.sendMessage(ChatColor.GREEN + "%health%" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "The current health the player got. ");
         } else {
             sender.sendMessage(ChatColor.RED + "%health%" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "The current health the player got. ");
         }
         sender.sendMessage("Variables in " + ChatColor.RED + "red" + ChatColor.WHITE + " color are disabled variables.");
         sender.sendMessage("----------------------------------------------------");
     }
     
     public static void displayAdminCmds(CommandSender sender) {
         sender.sendMessage("------------------ [" + ChatColor.BLUE + "LogStream v" + LogStream.version + ChatColor.WHITE + "] ------------------");
         sender.sendMessage(ChatColor.GREEN + "/ls reload" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "Reload LogStream.");
        sender.sendMessage(ChatColor.GREEN + "/ls generate <player> [stream type]" + ChatColor.WHITE + " -> " + ChatColor.GOLD 
                + "Generate all or the specific stream field for a player without modifying the stream output of the player.");
        sender.sendMessage(ChatColor.GREEN + "/ls remove [stream type] <player>" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "Will reset all or the specific stream modification(s) from a player.");
         sender.sendMessage("----------------------------------------------------");
     }
     
     public static void displayMainPage(CommandSender sender) {
         sender.sendMessage("------------------ [" + ChatColor.BLUE + "LogStream v" + LogStream.version + ChatColor.WHITE + "] ------------------");
         sender.sendMessage(ChatColor.GREEN + "/ls admincmds" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "Display the admin commands for LogStream.");
         sender.sendMessage(ChatColor.GREEN + "/ls modifyjoin [me/player] <string>" + ChatColor.WHITE + " -> "  + ChatColor.GOLD + "Change the join output for yourself(me) or someone else(player).");
         sender.sendMessage(ChatColor.GREEN + "/ls modifyquit [me/player] <string>" + ChatColor.WHITE + " -> "  + ChatColor.GOLD + "Change the quit output for yourself(me) or someone else(player).");
         sender.sendMessage(ChatColor.GREEN + "/ls variables" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "See the list of variables, info about them & more.");
        sender.sendMessage(ChatColor.GREEN + "/ls charges [output]" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "See info about charges/prices. You can add an output to check the price of it too.");
        sender.sendMessage(ChatColor.GREEN + "/ls remove [stream type]" + ChatColor.WHITE + " -> " + ChatColor.GOLD + "Will reset all or the specific stream modification(s) from yourself.");
         sender.sendMessage("----------------------------------------------------");
     }
 }
