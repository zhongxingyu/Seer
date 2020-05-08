 package com.kolinkrewinkel.BitLimitBlockRegression;
 
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

 public class RegressionCommandExecutor implements CommandExecutor {
    private final BitLimitBlockRegression plugin;
 
     public RegressionCommandExecutor(BitLimitBlockRegression plugin) {
         this.plugin = plugin;
     }
     
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         return false;
     }
 
     private boolean isValidBooleanInput(String string) {
         return string.equals("enable") || string.equals("enabled") || string.equals("true") || string.equals("YES") || string.equals("yes") || string.equals("disable") || string.equals("disabled") || string.equals("false") || string.equals("NO") || string.equals("no");
     }
 
     private boolean parsedBooleanInput(String string) {
         if (string.equals("enable") || string.equals("enabled") || string.equals("true") || string.equals("YES") || string.equals("yes")) {
             return true;
         } else if (string.equals("disable") || string.equals("disabled") || string.equals("false") || string.equals("NO") || string.equals("no")) {
             return false;
         }
         return false;
     }
 }
