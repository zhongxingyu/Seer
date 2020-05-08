 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package multiworld.command.move;
 
 import multiworld.ArgumentException;
 import multiworld.CommandException;
 import multiworld.Utils;
 import multiworld.command.Command;
 import multiworld.data.DataHandler;
 import multiworld.data.InternalWorld;
 import multiworld.data.PlayerHandler;
 import multiworld.data.WorldHandler;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Fernando
  */
 public class MoveCommand extends Command
 {
 	private final PlayerHandler p;
 	private final WorldHandler w;
 	private final DataHandler d;
 
 	public MoveCommand(DataHandler data, PlayerHandler player, WorldHandler worlds)
 	{
 		super("move");
 		this.p = player;
 		this.w = worlds;
 		this.d = data;
 	}
 
 	@Override
 	public void runCommand(CommandSender sender, String[] args) throws CommandException
 	{
		if (args.length != 2)
 		{
 			throw new ArgumentException("/mw move <player> <world>"); //NOI18N
 		}
 		else
 		{
 			Player targetPlayer = Bukkit.getPlayer(args[0]);
 			InternalWorld worldObj = Utils.getWorld(args[1], this.d, true);
 			if (targetPlayer == null)
 			{
 				sender.sendMessage(ChatColor.RED + this.d.getLang().getString("PLAYER NOT FOUND"));
 				return;
 			}
 			Location warpLoc = worldObj.getWorld().getSpawnLocation();
 			warpLoc.setWorld(worldObj.getWorld());
 			if (args.length == 4)
 			{
 				Utils.canUseCommand(sender, this.getPermissions() + ".cordinates");
 				double x = getCoordinate(sender, warpLoc.getX(), args[args.length - 3]);
 				double y = getCoordinate(sender, warpLoc.getY(), args[args.length - 2], 0, 0);
 				double z = getCoordinate(sender, warpLoc.getZ(), args[args.length - 1]);
 				if (x == MIN_COORD_MINUS_ONE || y == MIN_COORD_MINUS_ONE || z == MIN_COORD_MINUS_ONE)
 				{
 					sender.sendMessage("Please provide a valid location!");
 					return;
 				}
 				warpLoc.setX(x);
 				warpLoc.setY(y);
 				warpLoc.setZ(z);
 			}
 			p.movePlayer(targetPlayer, warpLoc);
 			targetPlayer.sendMessage("You are been moved to world \"" + worldObj.getName() + "\" by: " + Utils.getPlayerName(sender));
 			sender.sendMessage("Moved player");
 		}
 	}
 
 	@Override
 	public String[] calculateMissingArguments(CommandSender sender, String commandName, String[] split)
 	{
 		if (split.length == 0)
 		{
 			return this.calculateMissingArgumentsPlayer("");
 		}
 		else if (split.length == 1)
 		{
 			return this.calculateMissingArgumentsPlayer(split[0]);
 		}
 		else if (split.length == 2)
 		{
 			return this.calculateMissingArgumentsWorld(split[1]);
 		}
 		else
 		{
 			return EMPTY_STRING_ARRAY;
 		}
 	}
 }
