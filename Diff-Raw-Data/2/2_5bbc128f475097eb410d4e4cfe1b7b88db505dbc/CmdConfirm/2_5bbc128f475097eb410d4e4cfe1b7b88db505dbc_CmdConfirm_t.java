 package net.dmulloy2.teamsparkle.commands;
 
 import net.dmulloy2.teamsparkle.SparkledInstance;
 import net.dmulloy2.teamsparkle.TeamSparkle;
 import net.dmulloy2.teamsparkle.permissions.Permission;
 
 /**
  * @author dmulloy2
  */
 
 public class CmdConfirm extends TeamSparkleCommand
 {
 	public CmdConfirm(TeamSparkle plugin)
 	{
 		super(plugin);
 		this.name = "confirm";
 		this.description = "Confirm a recruit with their pin";
 		this.permission = Permission.CMD_CONFIRM;
 		
 		this.mustBePlayer = true;
 	}
 
 	@Override
 	public void perform() 
 	{
 		if (plugin.pinMap.containsKey(player.getName()))
 		{
 			int pin = argAsInt(0, true);
 			if (pin == -1)
 			{
 				err(getMessage("invalid_pin"));
 				return;
 			}
 			
 			SparkledInstance si = plugin.pinMap.get(player.getName());
 			if (pin == si.getPin())
 			{
				plugin.rewardSparkledPlayer(si.getSparkled(), si.getSparkler());
 				return;
 			}
 			
 			err(getMessage("invalid_pin"));
 		}
 	}
 
 }
