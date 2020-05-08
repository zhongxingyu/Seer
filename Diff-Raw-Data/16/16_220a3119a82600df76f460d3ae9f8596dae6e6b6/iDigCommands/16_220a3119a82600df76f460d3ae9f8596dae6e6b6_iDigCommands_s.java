 package in.idig.iDigCommands;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 
 public class iDigCommands extends JavaPlugin{
 	
 	private Logger logger = Logger.getLogger("Minecraft");
 	private CommandsHandler commandsHandler;
 	private smackHandler smackHandler;
 	private rulerHandler rulerHandler;
 	private evilchaseHandler evilchaseHandler;

 	public CommandsHandler getCommandsHandler() {
 		return commandsHandler;
 	}
 	public smackHandler getsmackHandler() {
 		return smackHandler;
 	}
 	public rulerHandler getrulerHandler() {
 		return rulerHandler;
 	}
 	public evilchaseHandler getevilchaseHandler() {
 		return evilchaseHandler;
 	}
 	
 	public void onEnable(){
 		commandsHandler = new CommandsHandler(this);
 		smackHandler = new smackHandler(this);
 		rulerHandler = new rulerHandler(this);
 		evilchaseHandler = new evilchaseHandler(this);
 		getCommand("idig").setExecutor(commandsHandler);
 		getCommand("smack").setExecutor(smackHandler);
 		getCommand("ruler").setExecutor(rulerHandler);
 		getCommand("evilchase").setExecutor(evilchaseHandler);
 		
 		String PluginVersion = version();
 		this.logMessage("***************************");
 		this.logMessage("*   iDig Suite Enabled    *");
 		this.logMessage("*   Version" + PluginVersion +"    *");
 		this.logMessage("***************************");
 		
 	}
 
 	public void onDisable(){
 		this.logMessage("***************************");
 		this.logMessage("*   iDig Suite Disabled    *");
 		this.logMessage("***************************");
 	}
 
 	public void logMessage(String msg){
 		PluginDescriptionFile pdFile = this.getDescription();
 		//this.log.info(pdFile.getName() + " " + pdFile.getVersion() + ": " + msg);
 		this.logger.info("[" + pdFile.getName() +"] "+ msg);
 	}
 	
 	public void logWarning(Exception ex) {
 		this.logger.log(Level.WARNING,"[LogBlock] Exception in commands handler: ",ex);	
 	}
 
 	public String version(){
 		PluginDescriptionFile pdFile = this.getDescription();
 		return pdFile.getVersion();
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (true)
 			sender.sendMessage(ChatColor.RED + "iDig CommandSender Linked in");
 		return true;
 	}
 
 }
