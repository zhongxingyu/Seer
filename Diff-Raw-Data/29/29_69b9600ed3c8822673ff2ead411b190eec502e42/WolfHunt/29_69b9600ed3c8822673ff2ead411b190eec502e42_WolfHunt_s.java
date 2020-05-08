 package me.Kruithne.WolfHunt;
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class WolfHunt extends JavaPlugin {
 	
 	Server server;
 	Logger log = Logger.getLogger("Minecraft");
 	Configuration config = null;
 	CommandHandler commandHandler = null;
 	WolfHuntPlayerListener playerListener = null;
 	
 	public void onEnable()
 	{
 		this.server = this.getServer();
 		this.config = new Configuration(this);
 		this.playerListener = new WolfHuntPlayerListener(this);
 		this.commandHandler = new CommandHandler(this);
 		
 		this.config.loadConfiguration();
 	}
 	
 	public void trackPlayers(Player player)
 	{
 		List<Player> worldPlayers = player.getWorld().getPlayers();
 		Player closestPlayer = null;
 		Location playerLocation = player.getLocation();
 		
 		if (!worldPlayers.isEmpty())
 		{
 			for (int playerIndex = 0; playerIndex < worldPlayers.size(); playerIndex++)
 			{
 				Player checkPlayer = worldPlayers.get(playerIndex);
				Location checkLocation = checkPlayer.getLocation();
 				
				if (playerLocation.distance(checkLocation) < this.config.trackingRadius)
 				{
					this.outputToPlayer(Constants.messageNearby, player);
					break;
				}
				else
				{
					if (closestPlayer == null)
 					{
						closestPlayer = checkPlayer;
 					}
 					else
 					{
						if (playerLocation.distance(checkLocation) < playerLocation.distance(closestPlayer.getLocation()))
 						{
 							closestPlayer = checkPlayer;
 						}
 					}
 				}
 			}
 			
 			if (closestPlayer != null)
 			{
 				float yaw = playerLocation.getYaw();
 				this.outputToPlayer(String.format(Constants.messageDetected, Math.ceil(yaw)), player);
 			}
 			else
 			{
 				this.outputToPlayer(Constants.messageNoPlayers, player);
 			}
 		}
 		else
 		{
 			this.outputToPlayer(Constants.messageNoPlayers, player);
 		}
 	}
 	
 	public boolean hasPermission(String permKey, Player player)
 	{
 		if (player.isOp() && this.config.allowOpOverride)
 		{
 			return true;
 		}
 		else if (player.hasPermission(String.format(Constants.pluginNodePath, permKey)))
 		{
 			return true;
 		}
 		
 		return false;
 	}
 
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] arguments)
 	{
 		return this.commandHandler.handleCommand(sender, command, arguments);
 	}
 	
 	public void outputToConsole(String message, Level outputType)
 	{
 		this.log.log(outputType, String.format(Constants.outputToConsoleFormat, Constants.outputPluginTag, message));
 	}
 	
 	public void outputToPlayer(String message, Player player)
 	{
 		player.sendMessage(String.format(Constants.outputToPlayerFormat, ChatColor.DARK_AQUA, message));
 	}
 
 }
