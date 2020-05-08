 package com.insofar.actor.commands.author;
 
 import java.util.HashMap;
 
 import org.bukkit.entity.Player;
 
 import com.insofar.actor.ActorAPI;
 import com.insofar.actor.ActorPlugin;
 import com.insofar.actor.Author;
 import com.insofar.actor.EntityActor;
 import com.insofar.actor.Recording;
 import com.insofar.actor.permissions.PermissionHandler;
 import com.insofar.actor.permissions.PermissionNode;
 
 /**
  * ActorPlugin command to playback actors and record at the same time.
  * 
  * @author Joshua Weinberg
  *
  */
 public class ActionRecord extends AuthorBaseCommand {
 
 	public ActionRecord()
 	{
 		super();
 	}
 
 	/********************************************************************
 	 * 
 	 * BUKKIT COMMAND
 	 * 
 	 ********************************************************************/
 
 	@Override
 	/**
 	 * Playback all actors which this player can view and record 
 	 */
 	public boolean execute()
 	{
 		if (!PermissionHandler.has(player, PermissionNode.COMMAND_ACTIONREC))
 		{
 			player.sendMessage("Lack permission: "
 					+ PermissionNode.COMMAND_ACTIONREC.getNode());
 			return true;
 		}
 		
 		// Record part
 		
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
 		
 		player.sendMessage("Recording started.");
 		
 		// Action part
 		
 		String actorName = args.length > 1 ? args[1] : "";
 		
 		for (EntityActor actor : plugin.actors)
 		{
 			if (actor.getOwner() == player &&
 					(actor.getActorName().equals(actorName) || actorName.equals("")))
 			{
 				actor.setIsPlayback(true);
 			}
 		}
 		
 		return true;
 	}
 }
