 package rockbeatspaper.Azimuth;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.entity.Player;
 
 public class Azimuth extends JavaPlugin 
 {
     Logger log = null;
     private final CompassPlayerListener playerListener = new CompassPlayerListener( this );
     private final CompassEntityListener entityListener = new CompassEntityListener( this, playerListener );
     
     
 	public void onEnable() 
 	{
 		log = Logger.getLogger("Minecraft");
 		log.info("Your plugin Azimuth(Compass) has been enabled.");	
 		
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvent( Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this );
 		pm.registerEvent( Event.Type.PLAYER_RESPAWN, playerListener, Event.Priority.Normal, this );
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
 		pm.registerEvent( Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Normal, this );
 		
 		//TODO grab everyone on the server and run through
 		Player[] onlinePlayers = this.getServer().getOnlinePlayers(); 
 		for( Player player: onlinePlayers)
 		{
 			playerListener.createPlayer(player);
 		}
 		
 	}
     
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
 	{	  
 		//behavior for compassAdd command
 		if(commandLabel.equalsIgnoreCase("compassAdd"))
 		{ 
			//Don't check number of arguments, don't need to
 			
 			//Console cannot add to compass rotation
 			if( ! (sender instanceof Player) )
 			{
 				return false;
 			}
 			
 			//concatenates the argument array into one string
 			String newArgs = "";
 			for( int i=0; i != args.length; ++i)
 			{
 				newArgs += args[i];
 			}
 			
 			newArgs = newArgs.toLowerCase();
 			//TODO make argument checking smarter
 			newArgs = newArgs.replaceAll("[\\s\\W]", ""); //replace all whitespace or non-alphanumeric chars with empty
 			
 			if( playerListener.addModeToPlayer( (Player) sender, newArgs) )
 			{
 				sender.sendMessage(ChatColor.GREEN + "Compass mode added to rotation.");
 			}
 			
 			return true; //finished command code
 		}
 		if(commandLabel.equalsIgnoreCase("compassRemove"))
 		{ 
			//Don't check number of arguments, don't need to
 			
 			//Console cannot add to compass rotation
 			if( ! (sender instanceof Player) )
 			{
 				return false;
 			}
 			
 			//concatenates the argument array into one string
 			String newArgs = "";
 			for( int i=0; i != args.length; ++i)
 			{
 				newArgs += args[i];
 			}
 			
 			newArgs = newArgs.toLowerCase();
 			//TODO make argument checking smarter
 			newArgs = newArgs.replaceAll("[\\s\\W]", ""); //replace all whitespace or non-alphanumeric chars with empty
 			
 			if( playerListener.removeModeFromPlayer( (Player) sender, newArgs) )
 			{
 				sender.sendMessage(ChatColor.GREEN + "Compass mode removed from rotation.");
 			}
 			
 			return true; //finished command code
 		}
 		//behavior for azimuth command
 		if(commandLabel.equalsIgnoreCase("azimuth"))
 		{
 			//Don't check number of arguments, don't need to
 			
 			//send message to commandSender
			sender.sendMessage("/compassAdd [] will add new points to your rotation");
			sender.sendMessage("/compassRemove [] will remove points to your compass rotation");
 			
 			return true;
 		}
 		
 		return false; //just in case, to cover my ass
 	}
 	
     public void onDisable() 
     {
 		// TODO Auto-generated method stub
     	log.info("Your plugin Azimuth(Compass) has been disabled.");
 	}
     
 }
