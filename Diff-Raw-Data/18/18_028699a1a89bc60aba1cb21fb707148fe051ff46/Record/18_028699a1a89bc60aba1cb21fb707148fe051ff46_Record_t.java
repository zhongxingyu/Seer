 package com.insofar.actor.commands.author;
 
 import java.util.HashMap;
 
 import org.bukkit.entity.Player;
 
 import com.insofar.actor.ActorAPI;
 import com.insofar.actor.ActorPlugin;
 import com.insofar.actor.Author;
 import com.insofar.actor.Recording;
 import com.insofar.actor.permissions.PermissionHandler;
 import com.insofar.actor.permissions.PermissionNode;
 
 /**
  * ActorPlugin command to record a player
  * 
  * @author Joshua Weinberg
  *
  */
 public class Record extends AuthorBaseCommand {
 
 	public Record()
 	{
 		super();
 	}
 
 	/*********************************************************************
 	 * 
 	 * BUKKIT COMMAND
 	 * 
 	 *********************************************************************/
 
 	@Override
 	/**
 	 * Record command
 	 */
 	public boolean execute()
 	{
 		if (!PermissionHandler.has(player, PermissionNode.COMMAND_RECORD))
 		{
 			player.sendMessage("Lack permission: "
 					+ PermissionNode.COMMAND_RECORD.getNode());
 			return true;
 		}
 		Author author = ActorAPI.getAuthor(player);
 		if (author.getTroupeMembers().size() < 1)
 		{
 			player.sendMessage("No troupe members to record.");
 			return true;
 		}
 		
 		author.setRecording(true);
 		HashMap<String,Recording> recMap = author.getTroupRecMap();
 		
 		for (Player member : author.getTroupeMembers())
 		{
 			// If no recording exists make one
 			Recording r = recMap.get(member.getName());
 			if (r == null)
 			{
 				r = new Recording();
 				recMap.put(member.getName(), r);
 			}
 			
 			ActorAPI.record(member, author.getTroupRecMap().get(member.getName()));
 			
 			if (member != player)
				member.sendMessage(ActorPlugin.TAG + " Recording started");
 		}
 		
 		player.sendMessage("Started recording troupe");
 		return true;
 	}
 }
