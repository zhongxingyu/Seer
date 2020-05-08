 package net.dmulloy2.swornparkour.commands;
 
 import net.dmulloy2.swornparkour.SwornParkour;
 import net.dmulloy2.swornparkour.parkour.objects.ParkourKickReason;
 
 /**
  * @author dmulloy2
  */
 
 public class CmdLeave extends SwornParkourCommand
 {
 	public CmdLeave(SwornParkour plugin)
 	{
 		super(plugin);
 		this.name = "leave";
 		this.aliases.add("l");
 		this.description = "Leave a parkour game!";
 		
 		this.mustBePlayer = true;
 	}
 	
 	@Override
 	public void perform()
 	{
 		if (!getManager().isInParkour(player))
 		{
 			err("You are not in a parkour game!");
 		}
 		
 		getManager().getParkourGame(player).kick(ParkourKickReason.LEAVE);
 	}
 }
