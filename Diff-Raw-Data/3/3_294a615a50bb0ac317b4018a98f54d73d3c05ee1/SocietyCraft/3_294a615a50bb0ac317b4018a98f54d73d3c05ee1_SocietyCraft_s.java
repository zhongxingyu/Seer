 package thisisboris.SocietyCraft;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 import thisisboris.SocietyCraft.includes.CommandManager;
 import thisisboris.SocietyCraft.includes.SCLogger;
 import thisisboris.SocietyCraft.commands.SocietyCraftcmd;
 
 
 /**
  * SocietyCraft - A world enhancing plugin for Bukkit
  *
  * @author Thisisboris, cskiwi
  */
 
 public class SocietyCraft extends JavaPlugin {
 	private final SCPlayerListener playerListener = new SCPlayerListener(this);
     private final SCBlockListener blockListener = new SCBlockListener(this);
     private final CommandManager commandManager = new CommandManager(this);
     private final List<Player> debugees = new ArrayList<Player>();
 	public static String name;
     public static String version;
     private static boolean debugging;
     
     // Methods
 
     
     public void onEnable() {
         // TODO: Place any custom enable code here including the registration of any events
     	name = this.getDescription().getName();
         version = this.getDescription().getVersion();
         
     	SCLogger.initialize(Logger.getLogger("Minecraft"));
     	
         // Register our events
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.BLOCK_CANBUILD, blockListener, Priority.Normal, this);
 
         // Register our commands
         SCLogger.info("Setting command");
         setupCommands();
 
         // EXAMPLE: Custom code, here we just output some info so we can check all is well
         // PluginDescriptionFile pdfFile = this.getDescription();
         // System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
         // you can uuse above or below for console output, prefered by MC below
         SCLogger.info(name + " version " + version + " is enabled!");
     }
     
     /*
      * Sets up the core commands of the plugin.
      */
     private void setupCommands() {
         // Add command labels here.
         // For example in "/template version" and "/template reload" the label for both is "template".
         // Make your commands in the template.commands package. Each command is a separate class.
     	SCLogger.info("Adding command");
         addCommand("SocietyCraft", new SocietyCraftcmd(this));
         addCommand("SC", new SocietyCraftcmd(this));
         addCommand("sc", new SocietyCraftcmd(this));
         
     }
 
     /*
      * Executes a command when a command event is received.
      * 
      * @param sender    The thing that sent the command.
      * @param cmd       The complete command object.
      * @param label     The label of the command.
      * @param args      The arguments of the command.
      */
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         return commandManager.dispatch(sender, cmd, label, args);
     }
 
     /*
      * Adds the specified command to the command manager and server.
      * 
      * @param command   The label of the command.
      * @param executor  The command class that excecutes the command.
      */
     private void addCommand(String command, CommandExecutor executor) {
         getCommand(command).setExecutor(executor);
         commandManager.addCommand(command, executor);
     }
 
     /*
      * This method runs when the plugin is disabling.
      */
     @Override
     public void onDisable() {
         //TDatabase.disable();
 
     	SCLogger.info(name + " DISABLED! ");
     	
     }
     
     /*
      * Checks is the plugin is in debug mode.
      */
     public boolean inDebugMode(){
         return !debugees.isEmpty() || debugging;
     }
 
     /*
      * Checks if a player is in debug mode.
      * 
      * @param player    The player to check.
      */
     public boolean isDebugging(final Player player) {
         return debugees.contains(player);
     }
 
     /*
      * Sets a players debug mode.
      * 
      * @param player    The player to set the debug mode of.
      */
     public void startDebugging(final Player player) {
         debugees.add(player);
     }
     
     public void startDebugging() {
         debugging = true;
     }
     
     public void stopDebugging(final Player player) {
         debugees.remove(player);
     }
     
     public void stopDebugging() {
         for(Player player : debugees) {
             player.sendMessage("You are no longer in debug mode.");
         }
         debugees.clear();
         debugging = false;
     }
     
 }
