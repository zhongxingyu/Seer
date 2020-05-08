 package psy.ActivityHistory.cmd;
 
import java.sql.SQLException;

 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
 
 import psy.ActivityHistory.ActivityHistory;
 
 /**
  * Used as a command placeholder for when player queries are disabled.
  */
 public class DisabledPQCE extends PlayerQueryCommandExecutor{
	private ActivityHistory plugin;
	private final SQLException exception;
	public DisabledPQCE(Plugin pl){
		exception = null;
		plugin = (ActivityHistory)pl;
    }
	
    public DisabledPQCE(Plugin pl, SQLException e){
    	exception = e;
    	plugin = (ActivityHistory)pl;
     }
     
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
         sender.sendMessage(ActivityHistory.messages.getString("info.playerDisabled"));
        if(plugin.accessConfig().getString("general.debugMode").equalsIgnoreCase("advanced") && exception!=null)
        	exception.printStackTrace();
         return true;
     }
 }
