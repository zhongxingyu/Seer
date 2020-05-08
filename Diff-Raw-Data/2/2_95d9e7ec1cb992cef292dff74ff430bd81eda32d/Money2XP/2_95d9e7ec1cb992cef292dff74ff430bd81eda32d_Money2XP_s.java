 package tzer0.Money2XP;
 
 import java.util.LinkedHashSet;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.gmail.nossr50.mcMMO;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 // TODO: Auto-generated Javadoc
 /**
  * Plugin for bukkit allowing players to purchase mcMMO-experience points using iConomy-money.
  * 
  * @author TZer0
  */
 public class Money2XP extends JavaPlugin {
     private final Money2XPPlayerListener listener = new Money2XPPlayerListener();
     public PermissionHandler permissions;
     public LinkedHashSet<String> skillnames = new LinkedHashSet<String>();
     public mcMMO mcmmo;
     @SuppressWarnings("unused")
     private final String name = "Money2XP";
 
     /* (non-Javadoc)
      * @see org.bukkit.plugin.Plugin#onDisable()
      */
     public void onDisable() {
         PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println(pdfFile.getName() + " disabled.");
     }
 
     /* (non-Javadoc)
      * @see org.bukkit.plugin.Plugin#onEnable()
      */
     public void onEnable() {
         if (skillnames.size() == 0) {
             String []names = {"acrobatics", "archery", "axes", "excavation", "herbalism", "mining", "repair", "swords", "unarmed", "woodcutting"};
             for (String name : names) {
                 skillnames.add(name);
             }
         }
         setupPermissions();
         PluginDescriptionFile pdfFile = this.getDescription();
         listener.setPointers(getConfiguration(), this, permissions);
         getServer().getPluginManager().registerEvent(
                 Event.Type.PLAYER_COMMAND_PREPROCESS, listener,
                 Priority.Normal, this);
         System.out.println(pdfFile.getName() + " version "
                 + pdfFile.getVersion() + " is enabled!");
         mcmmo = (mcMMO) getServer().getPluginManager().getPlugin("mcMMO");
     }
 
     /* (non-Javadoc)
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(CommandSender sender, Command cmd,
             String commandLabel, String[] args) {
         boolean help = false;
         if (commandLabel.equalsIgnoreCase("m2x")) {
             if (args.length >= 1 && args[0].equalsIgnoreCase("list")) {
                 showSkills(sender);
                 return true;
             } else {
                 help = true;
             }
         } else if (commandLabel.equalsIgnoreCase("m2xset")) {
             if ((!(args.length == 2) || !modValue(args[0], args[1], sender))) {
                 help = true;
             } else {
                 return true;
             }
         }
         if (help) {
             sender.sendMessage(ChatColor.GREEN + "Money2XP by TZer0 (TZer0.jan@gmail.com)");
             sender.sendMessage(ChatColor.YELLOW + "Available commands:");
             sender.sendMessage(ChatColor.YELLOW + "/m2xset [skillname] [price_per_xp] - sets xp-cost for a skill");
             sender.sendMessage(ChatColor.YELLOW + "/m2x list - list skills and prices");
             return true;
         }
         return false;
     }
 
     /**
      * Modifies a settings-value.
      *
      * @param key The key to modify or delete
      * @param value The value which will be set
      * @param sender Whoever is sending doing this command 
      * @return true, Wether it worked or not.
      */
     public boolean modValue(String key, String value, CommandSender sender) {
         if (checkInt(value)) {
             if (!(key.equals("default") || skillnames.contains(key))) {
                 sender.sendMessage(ChatColor.RED + "This skill does not exist.");
                 return true;
             }
             int i = Integer.parseInt(value);
             if (i == 0) {
                 getConfiguration().removeProperty(key);
                 value = String.format("default(%d)",
                         getConfiguration().getInt("default", 100));
             } else {
                 if (i == -1) {
                     value = "unavailable";
                 }
                 getConfiguration().setProperty(key.toLowerCase(), i);
             }
             getConfiguration().save();
             sender.sendMessage(ChatColor.GREEN
                     + String.format("Price per xp for %s has been set to %s",
                             key, value));
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Basic Permissions-setup, see more here: https://github.com/TheYeti/Permissions/wiki/API-Reference
      */
     private void setupPermissions() {
         Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
 
         if (this.permissions == null) {
             if (test != null) {
                 this.permissions = ((Permissions) test).getHandler();
             } else {
                 System.out.println(ChatColor.YELLOW
                         + "Permissons not detected - defaulting to OP!");
             }
         }
     }
     /**
      * Displays a page containing price-information about xp
      * 
      * @param page The page to display
      * @param player
      */
     public void showSkills(CommandSender sender) {
         int def = getConfiguration().getInt("default", 100);
         int value = 0;
         sender.sendMessage(ChatColor.WHITE + "Name - Cost");
         for (String name : skillnames) {
             value = getConfiguration().getInt(name, def);
            if (value <= 0) {
                 sender.sendMessage(ChatColor.GREEN + String.format("%s - %d", name, value));
             } else {
                 sender.sendMessage(ChatColor.RED + String.format("%s is not available", name));
             }
         }
     }
     
     /**
      * Check if the string is valid as an int (accepts signs).
      *
      * @param in The string to be checked
      * @return boolean Success
      */
     public boolean checkInt(String in) {
         char chars[] = in.toCharArray();
         for (int i = 0; i < chars.length; i++) {
             if (!(Character.isDigit(chars[i]) || (i == 0 && chars[i] == '-'))) {
                 return false;
             }
         }
         return true;
     }
 }
