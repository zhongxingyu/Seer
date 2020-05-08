 package net.dmulloy2.swornparkour.commands;
 
 import net.dmulloy2.swornparkour.SwornParkour;
 import net.dmulloy2.swornparkour.parkour.ParkourGame;
 import net.dmulloy2.swornparkour.parkour.objects.ParkourJoinTask;
 
 /**
  * @author dmulloy2
  */
 
 public class CmdJoin extends SwornParkourCommand
 {
 	public CmdJoin(SwornParkour plugin)
 	{
 		super(plugin);
 		this.name = "join";
 		this.aliases.add("j");
 		this.requiredArgs.add("game");
 		this.description = "Join a parkour game!";
 		
 		this.mustBePlayer = true;
 	}
 	
 	@Override
 	public void perform()
 	{
 		int gameId = argAsInt(0, true);
		if (gameId == -1)
			return;
		
 		if (plugin.loadedArenas.size() < gameId)
 		{
 			err("&cNo arena by that number exists!");
 			return;
 		}
 		
 		for (ParkourGame game : getManager().parkourGames)
 		{
 			if (game.getId() == gameId)
 			{
 				err("&cThat game is already in progress!");
 				return;
 			}
 		}
 		
 		if (getManager().isInParkour(player))
 		{
 			err("&cYou are already in a game!");
 			return;
 		}
 		
 		int teleportTimer = plugin.teleportTimer * 20;
 		
 		sendMessage("&ePlease stand still for {0} seconds!", plugin.teleportTimer);
 		
 		new ParkourJoinTask(plugin, player, gameId).runTaskLater(plugin, teleportTimer);
 	}
 }
