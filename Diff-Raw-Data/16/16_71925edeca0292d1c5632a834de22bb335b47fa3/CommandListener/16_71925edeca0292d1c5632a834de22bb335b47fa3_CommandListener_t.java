 package com.censoredsoftware.Demigods.Engine.Listener;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 import com.censoredsoftware.Demigods.Engine.Ability.Ability;
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedBattle;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedPlayer;
 import com.censoredsoftware.Demigods.Engine.Utility.ZoneUtility;
 
 public class CommandListener implements Listener
 {
	@EventHandler(priority = EventPriority.HIGHEST)
 	public static void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
 	{
 		String message = event.getMessage();
 		message = message.substring(1);
 
 		String[] args = message.split("\\s+");
 
 		Player player = event.getPlayer();
 
 		if(ZoneUtility.canTarget(player) && TrackedBattle.isInAnyActiveBattle(TrackedPlayer.getTracked(player).getCurrent()))
 		{
 			if(TrackedBattle.isBlockedCommand(args[0]))
 			{
 				player.sendMessage(ChatColor.GRAY + "That command is blocked during a battle.");
 				event.setCancelled(true);
 				return;
 			}
 		}
 
 		try
 		{
 			if(Ability.invokeAbilityCommand(player, args[0], args.length == 2 && args[1].equalsIgnoreCase("bind")))
 			{
 				Demigods.message.info(event.getPlayer().getName() + " used the command: /" + message);
 				event.setCancelled(true);
 			}
 		}
 		catch(Exception e)
 		{
 			// Not a command
 			e.printStackTrace();
 		}
 	}
 }
