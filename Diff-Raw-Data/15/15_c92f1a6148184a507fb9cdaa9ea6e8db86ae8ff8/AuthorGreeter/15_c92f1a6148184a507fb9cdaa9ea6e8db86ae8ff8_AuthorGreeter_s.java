 package tzer0.AuthorGreeter;
 
 
 
 import java.util.LinkedList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.event.server.ServerListener;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.iConomy.iConomy;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 /**
  * A plugin which welcomes plugin authors!
  * 
  * @author TZer0
  */
 public class AuthorGreeter extends JavaPlugin {
     private final AuthorGreeterPlayerListener listener = new AuthorGreeterPlayerListener();
     PluginDescriptionFile pdfFile;
     Configuration conf;
     PermissionHandler permissions;
     boolean adminOnly;
     boolean longVersion;
     LinkedList<String> ignore;
     @SuppressWarnings("unused")
     private final String name = "AuthorGreeter";
     public iConomy iConomy;
 
     /* (non-Javadoc)
      * @see org.bukkit.plugin.Plugin#onDisable()
      */
     public void onDisable() {
         System.out.println(pdfFile.getName() + " disabled.");
     }
 
     /* (non-Javadoc)
      * @see org.bukkit.plugin.Plugin#onEnable()
      */
     public void onEnable() {
         setupPermissions();
         conf = getConfiguration();
         ignore = new LinkedList<String>();
         for (String str : conf.getStringList("ignore", null)) {
             ignore.add(str.toLowerCase());
         }
         pdfFile = getDescription();
         adminOnly = conf.getBoolean("adminOnly", false);
         longVersion = conf.getBoolean("longVersion", true);
         listener.setPointers(conf, this, permissions);
         PluginManager tmp = getServer().getPluginManager();
        Server sl = new Server(this);
         tmp.registerEvent(Event.Type.PLAYER_JOIN, listener, Priority.Normal, this);
         tmp.registerEvent(Event.Type.PLUGIN_DISABLE, sl, Priority.Normal, this);
         tmp.registerEvent(Event.Type.PLUGIN_ENABLE, sl, Priority.Normal, this);
         System.out.println(pdfFile.getName() + " version "
                 + pdfFile.getVersion() + " is enabled!");
     }
     /* (non-Javadoc)
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(CommandSender sender, Command cmd,
             String commandLabel, String[] args) {
         Player player = null;
         if (sender instanceof Player) {
             player = (Player) sender;
         }
         int l = args.length;
         boolean help = false;
         if (player != null) {
             // user-accessible commands
             if (!(permissions == null || (permissions != null && permissions.has(player, "authorgreeter.admin")))) {
                 player.sendMessage(ChatColor.RED + "You do not have access to this command.");
                 return true;
             }
         }
         if (l >= 1) {
             if (args[0].equalsIgnoreCase("adminonly") || args[0].equalsIgnoreCase("ao")) {
                 if (l == 2) {
                     adminOnly = args[1].equalsIgnoreCase("t") || args[1].equalsIgnoreCase("true");
                     conf.setProperty("adminOnly", adminOnly);
                     conf.save();
                 }
                 String state = "";
                 if (adminOnly) {
                     state = "on";
                 } else {
                     state = "off";
                 }
                 sender.sendMessage(ChatColor.YELLOW + String.format("Admin only mode is now %s", state));
             } else if (args[0].equalsIgnoreCase("longversion") || args[0].equalsIgnoreCase("lv")) {
                 if (l == 2) {
                     longVersion = args[1].equalsIgnoreCase("t") || args[1].equalsIgnoreCase("true");
                     conf.setProperty("longVersion", longVersion);
                     conf.save();
                 }
                 String state = "";
                 if (longVersion) {
                     state = "on";
                 } else {
                     state = "off";
                 }
                 sender.sendMessage(ChatColor.YELLOW + String.format("Long version mode is now %s", state));
             } else if (args[0].equalsIgnoreCase("addignore") || args[0].equalsIgnoreCase("ai")) {
                 if (l == 2) {
                     ignore.add(args[1].toLowerCase());
                     conf.setProperty("ignore", ignore.toArray());
                     conf.save();
                     sender.sendMessage(ChatColor.GREEN + "Done.");
                 } else {
                     sender.sendMessage(ChatColor.RED + "Please provide a name");
                 }
             } else if (args[0].equalsIgnoreCase("removeignore") || args[0].equalsIgnoreCase("ri")) {
                 if (l == 2) {
                     ignore.remove(args[1].toLowerCase());
                     conf.setProperty("ignore", ignore.toArray());
                     conf.save();
                     sender.sendMessage(ChatColor.GREEN + "Done.");
                 } else {
                     sender.sendMessage(ChatColor.RED + "Please provide a name");
                 }
             } else if (args[0].equalsIgnoreCase("listignore") || args[0].equalsIgnoreCase("li")) {
                 int page = 0;
                 if (l == 2) {
                     page = toInt(args[1]);
                 }
                 sender.sendMessage(ChatColor.GREEN + String.format("Ignore authors (showing: %d to %d):", 
                         page*10, Math.min(((page+1)*10), ignore.size())));
                 int i = 0;
                 for (String str : ignore) {
                     if (i >= page*10) {
                         sender.sendMessage(ChatColor.GREEN + str);
                     } else if (i > (page+1)*10) {
                         break;
                     }
                     i++;
                 }
                 if (i < ignore.size()-1) {
                     sender.sendMessage(ChatColor.GREEN + String.format("Next page: /ag li %d", page+1));
                 }
             } else if (args[0].equalsIgnoreCase("cashreward") || args[0].equalsIgnoreCase("cr")) {
                 if (l == 2) {
                     double newValue = 0.0;
                     try {
                         newValue = Double.parseDouble(args[1]);
                         conf.setProperty("cashreward", newValue);
                         conf.save();
                     } catch (NumberFormatException e) {
                         sender.sendMessage(ChatColor.RED + "Invalid double-format");
                         newValue = conf.getDouble("cashreward", 0.0);
                     }
                 }
                 sender.sendMessage(ChatColor.GREEN + String.format("Current cash-reward is set to %.2f.", conf.getDouble("cashreward", 0.0)));
             } else if (args[0].equalsIgnoreCase("perplugin") || args[0].equalsIgnoreCase("pp")) {
                 boolean value = conf.getBoolean("perplugin", true);
                 if (l == 2) {
                     value = args[1].equalsIgnoreCase("t") || args[1].equalsIgnoreCase("true");
                     conf.setProperty("perplugin", value);
                     conf.save();
                 }
                 if (value) {
                     sender.sendMessage(ChatColor.GREEN + String.format("Authors are rewarded once per plugin."));
                 } else {
                     sender.sendMessage(ChatColor.GREEN + String.format("Authors are rewarded once only."));
                 }
             } else if (args[0].equalsIgnoreCase("itemreward") || args[0].equalsIgnoreCase("ir")) {
                 if (l == 1) {
                     sender.sendMessage(ChatColor.GREEN + "Reward-items:");
                     sender.sendMessage(ChatColor.GREEN + String.format("%5s %5s", "id", "amount"));
                     for (String key : conf.getKeys("itemreward.")) {
                         sender.sendMessage(ChatColor.YELLOW + String.format("%5s %5d", key, conf.getInt("itemreward."+key, 0)));
                     }
                 } else if (l >= 3) {
                     if (args[1].equalsIgnoreCase("a") || args[1].equalsIgnoreCase("add")) {
                         int numberof = 1;
                         int id = toInt(args[2]);
                         if (l >= 4) {
                             numberof = toInt(args[3]);
                         }
                         if (numberof == 0) {
                             conf.removeProperty("itemreward."+id);
                             sender.sendMessage(ChatColor.GREEN + "Removed.");
                         } else {
                             conf.setProperty("itemreward."+id, numberof);
                             sender.sendMessage(ChatColor.GREEN + "Added.");
                         }
                     } else if (args[1].equalsIgnoreCase("d") || args[1].equalsIgnoreCase("delete")) {
                         conf.removeProperty("itemreward."+toInt(args[2]));
                         sender.sendMessage(ChatColor.GREEN + "Removed.");
                     } else {
                         sender.sendMessage(ChatColor.RED + "No such parameter.");
                     }
                     conf.save();
                 } else {
                     sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
                     help = true;
                 }
             } else if (args[0].equalsIgnoreCase("visited") || args[0].equalsIgnoreCase("v")) {
                 int page = 0;
                 if (l == 2) {
                     page = toInt(args[1]);
                 }
                 sender.sendMessage(ChatColor.GREEN + String.format("Authors that have been on this server (showing: %d to %d):", 
                         page*10, Math.min(((page+1)*10), conf.getKeys("visited.").size())));
                 int i = 0;
                 for (String str : conf.getKeys("visited.")) {
                     if (i >= page*10) {
                         sender.sendMessage(ChatColor.GREEN + String.format("%s - claimed rewards for %d plugins", str, conf.getInt("visited."+str, 0)));
                     } else if (i > (page+1)*10) {
                         break;
                     }
                     i++;
                 }
                 if (i < ignore.size()-1) {
                     sender.sendMessage(ChatColor.GREEN + String.format("Next page: /ag lv %d", page+1));
                 }
             }
         } else {
             help = true;
         }
         if (help) {
             sender.sendMessage(ChatColor.YELLOW + pdfFile.getFullName() + ChatColor.YELLOW +  " by TZer0");
             sender.sendMessage(ChatColor.GREEN + "[] denote optional arguments, {} optional, () alises");
             sender.sendMessage(ChatColor.GREEN + "/ag (a)dmin(o)nly [t/f] " + ChatColor.YELLOW + "- show or toggle adminonly mode");
             sender.sendMessage(ChatColor.GREEN + "/ag (l)ong(v)ersion [t/f] " + ChatColor.YELLOW + "- show or toggle longversion mode");
             sender.sendMessage(ChatColor.GREEN + "/ag (a)dd(i)gnore name " + ChatColor.YELLOW + "- add someone to the ignore-list");
             sender.sendMessage(ChatColor.GREEN + "/ag (r)emove(i)gnore " + ChatColor.YELLOW + "- remove someone to the ignore-list");
             sender.sendMessage(ChatColor.GREEN + "/ag (l)ist(i)gnore " + ChatColor.YELLOW + "- list ignored authors");
             sender.sendMessage(ChatColor.GREEN + "/ag (c)ash(r)eward {[value]} " + ChatColor.YELLOW + "- lists or sets cash-reward");
             sender.sendMessage(ChatColor.GREEN + "/ag (p)er(p)lugin {[t/f]} " + ChatColor.YELLOW + "- sets or shows perplugin setting");
             sender.sendMessage(ChatColor.GREEN + "/ag (i)tem(r)eward {(a)dd/(d)elete} {[itemid]} {[numberof]} ");
             sender.sendMessage(ChatColor.YELLOW + "- shows, sets and removes itemrewards");
             sender.sendMessage(ChatColor.GREEN + "/ag (v)isited " + ChatColor.YELLOW + "- list authors who have been on the server");
         }
         return true;
     }
     public int toInt(String in) {
         int out = 0;
         if (checkInt(in)) {
             out = Integer.parseInt(in);
         }
         return out;
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
    class Server extends ServerListener {
         private AuthorGreeter plugin;
 
        public Server(AuthorGreeter plugin) {
             this.plugin = plugin;
         }
         public void onPluginDisable(PluginDisableEvent event) {
             if (plugin.iConomy != null) {
                 if (event.getPlugin().getDescription().getName().equals("iConomy")) {
                     plugin.iConomy = null;
                     System.out.println("[AuthorGreeter] un-hooked from iConomy.");
                 }
             }
         }
         public void onPluginEnable(PluginEnableEvent event) {
             if (plugin.iConomy == null) {
                 Plugin iConomy = plugin.getServer().getPluginManager().getPlugin("iConomy");
 
                 if (iConomy != null) {
                     if (iConomy.isEnabled()) {
                         plugin.iConomy = (com.iConomy.iConomy)iConomy;
                         System.out.println("[AuthorGreeter] hooked into iConomy.");
                     }
                 }
             }
         }
     }
 }
