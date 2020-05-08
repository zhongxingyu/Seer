 package com.kolinkrewinkel.BitLimitBlockRegression;
 
 public class RegressionCommandExecutor implements CommandExecutor {
 
     public RegressionCommandExecutor(BitLimitBlockRegression plugin) {
         this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, this.plugin);
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
