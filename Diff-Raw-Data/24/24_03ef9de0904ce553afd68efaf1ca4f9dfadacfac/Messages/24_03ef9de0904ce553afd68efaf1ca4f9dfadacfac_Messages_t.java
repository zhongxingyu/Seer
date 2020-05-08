 package fr.aumgn.bukkitutils.command.messages;
 
 import org.bukkit.ChatColor;
 
 public class Messages {
 
     public String usagePrefix() {
         return ChatColor.GREEN + "Usage : " + ChatColor.YELLOW + "/<command> ";
     }
 
     public String permissionMessage() {
         return ChatColor.RED + "You don't have the permission to do that.";
     }
 
     public String playerOnly() {
         return ChatColor.RED + "This command can only be used as a player.";
     }
 
     public String invalidFlag() {
         return "Invalid Flag : %c";
     }
 
     public String missingArguments() {
         return "Missing argument(s) (%d / %d min)";
     }
 
     public String tooManyArguments() {
         return "Too many argument(s) (%d / %d max)";
     }
 
     public String notAValidNumber() {
         return "Expected a number for arg %d.";
     }
 
     public String unexpectedError() {
         return "An unexpected error ocurred while trying to execute command.";
     }
 
     public String noSuchWorld() {
         return "No such world (%s).";
     }
 
     public String noSuchPlayer() {
         return "No such player (%s).";
     }
 
     public String noSuchMaterial() {
         return "No such material (%s).";
     }
 
     public String invalidMaterialAndDataFormat() {
        return "Invalid material and data format : %s.";
     }
 }
