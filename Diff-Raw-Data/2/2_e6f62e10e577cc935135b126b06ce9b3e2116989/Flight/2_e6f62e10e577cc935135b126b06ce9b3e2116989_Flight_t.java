 package me.ryanclancy000.flight;
 
 import java.io.IOException;
 import java.util.List;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Flight extends JavaPlugin {
 
     private FlightCommands cHandler = new FlightCommands(this);
    public FlightListener listener = new FlightListener(this);
     
     public boolean godMode;
     public boolean useEnabledPlayers;
     public List enablePlayers;
 
     @Override
     public void onDisable() {
     }
 
     @Override
     public void onEnable() {
         
         getCommand("flight").setExecutor(this);
         getServer().getPluginManager().registerEvents(listener, this);
 
         try {
             this.getConfig().options().copyDefaults(true);
             godMode = this.getConfig().getBoolean("god-mode");
             useEnabledPlayers = this.getConfig().getBoolean("use-enabled-players");
             enablePlayers = this.getConfig().getList("enabled-players");
             this.saveConfig();
         } catch (Exception ex) {
             this.getLogger().severe("Could not load config!");
         }
 
         try {
             Metrics metrics = new Metrics(this);
             metrics.start();
         } catch (IOException e) {
             this.getLogger().severe("Could not enable Metrics tracking!");
         }
 
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         if (cmd.getName().equalsIgnoreCase("flight")) {
             return doCommand(sender, args);
         }
 
         return onCommand(sender, cmd, commandLabel, args);
     }
 
     private boolean doCommand(CommandSender sender, String[] args) {
 
         Player player = (Player) sender;
 
         if (args.length == 0) {
             sender.sendMessage(cHandler.pre + ChatColor.GREEN + "by ryanclancy000");
             sender.sendMessage(cHandler.yellow + "- To view commands, do /flight " + cHandler.green + "help");
             return true;
         }
 
         // Help Command
 
         if ("help".equalsIgnoreCase(args[0])) {
 
             if (!sender.hasPermission("flight.help")) {
                 noPerms(sender);
                 return true;
             }
 
             this.cHandler.helpCommand(sender, args);
             return true;
 
         }
 
         // Toggle Command
 
         if ("toggle".equalsIgnoreCase(args[0])) {
 
             if (!sender.hasPermission("flight.toggle")) {
                 noPerms(sender);
                 return true;
             }
 
             this.cHandler.toggleCommand(sender, args);
             return true;
         }
 
         // On Command
 
         if ("on".equalsIgnoreCase(args[0])) {
 
             if (!sender.hasPermission("flight.on")) {
                 noPerms(sender);
                 return true;
             }
 
             this.cHandler.flyOn(sender, args);
             return true;
 
         }
 
         // Off Command
 
         if ("off".equalsIgnoreCase(args[0])) {
 
             if (!sender.hasPermission("flight.off")) {
                 noPerms(sender);
                 return true;
             }
 
             this.cHandler.flyOff(sender, args);
             return true;
 
         }
 
         // Check Command
 
         if ("check".equalsIgnoreCase(args[0])) {
 
             if (!sender.hasPermission("flight.check")) {
                 noPerms(sender);
                 return true;
             }
 
             this.cHandler.checkCommand(sender, args);
             return true;
         }
 
         // List Command
 
         if ("list".equalsIgnoreCase(args[0])) {
 
             if (!sender.hasPermission("flight.list")) {
                 noPerms(sender);
                 return true;
             }
 
             this.cHandler.listCommand(sender, args);
             return true;
 
         }
 
         return false;
     }
 
     public void noPerms(CommandSender sender) {
         sender.sendMessage(ChatColor.RED + "You do not have permission for that command...");
     }
 }
