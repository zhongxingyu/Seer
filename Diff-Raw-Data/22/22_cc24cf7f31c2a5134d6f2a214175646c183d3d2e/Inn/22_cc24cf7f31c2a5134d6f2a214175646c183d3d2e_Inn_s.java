 package me.greatman.plugins.inn;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import me.greatman.plugins.inn.commands.InnCmd;
 import me.greatman.plugins.inn.extras.CommandManager;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.nijikokun.register.payment.Method;
 
 public class Inn extends JavaPlugin {
 	public static String name,version;
 	public Configuration config;
 	public IConfig IConfig;
 	private Map<String, PlayerData> playerData;
 	private final CommandManager commandManager = new CommandManager(this);
 	private final IPlayerListener playerListener = new IPlayerListener(this);
 	private final IServerListener serverListener = new IServerListener(this);
 	public Method Method;
     public void onDisable() {
         // TODO: Place any custom disable code here.
         System.out.println(this + " is now disabled!");
     }
 
     public void onEnable() {
     	setPlayerData(Collections.synchronizedMap(new HashMap<String, PlayerData>()));
     	name = this.getDescription().getName();
 		version = this.getDescription().getVersion();
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_BED_ENTER, playerListener, Priority.Lowest, this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Monitor,this);
 		pm.registerEvent(Event.Type.PLAYER_MOVE,playerListener,Priority.Monitor,this);
 		pm.registerEvent(Event.Type.PLUGIN_DISABLE, serverListener,Priority.Monitor,this);
 		pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener,Priority.Monitor,this);
 		ILogger.initialize(Logger.getLogger("Minecraft"));
 		IConfig IConfig = new IConfig(this);
 		IConfig.configCheck();
 		IPermissions.initialize(this);
 		InnCmd InnCmd = new InnCmd(this);
 		addCommand("inn",InnCmd);
 	    ILogger.info("initialized!");
     }
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
     public void setPlayerData(Map<String, PlayerData> playerData) {
         this.playerData = playerData;
     }
 
     public Map<String, PlayerData> getPlayerData() {
         return playerData;
     }
     public Player getPlayer(CommandSender sender) {
         Player player = null;
         if (isPlayer(sender)) {
             player = (Player) sender;
         }
         return player;
     }
     public boolean isPlayer(CommandSender sender) {
         return sender != null && sender instanceof Player;
     }
 }
