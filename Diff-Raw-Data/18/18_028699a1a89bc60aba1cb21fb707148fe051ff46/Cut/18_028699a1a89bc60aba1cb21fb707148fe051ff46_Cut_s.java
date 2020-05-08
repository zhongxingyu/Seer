 package com.insofar.actor.commands.author;
 
 import org.bukkit.entity.Player;
 
 import com.insofar.actor.ActorAPI;
 import com.insofar.actor.Author;
 import com.insofar.actor.EntityActor;
 import com.insofar.actor.Recording;
 import com.insofar.actor.permissions.PermissionHandler;
 import com.insofar.actor.permissions.PermissionNode;
 
 /**
  * ActorPlugin command to stop playback on an actor.
  * 
  * @author Joshua Weinberg
  *
  */
 public class Cut extends AuthorBaseCommand {
 
 	public Cut()
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
 	 * bukkit command to stop playback and recording 
 	 */
 	public boolean execute()
 	{
 		if (!PermissionHandler.has(player, PermissionNode.COMMAND_CUT))
 		{
 			player.sendMessage("Lack permission: "
 					+ PermissionNode.COMMAND_CUT.getNode());
 			return true;
 		}
 		
 		Author author = ActorAPI.getAuthor(player);
 		
 		// Stop troupe recording
 		if (author.isRecording())
 		{
 			player.sendMessage("Troupe recording stopped.");
 			author.setRecording(false);
 		}
 		
 		// Rewind troupe member recordings
 		for (Player member : author.getTroupeMembers())
 		{
			member.sendMessage("Recording stopped.");
 			Recording r = author.getTroupRecMap().get(member.getName());
 			if (r != null)
 				r.rewind();
 		}
 		
 		// Stop playback on all actors
 		for (EntityActor actor : plugin.actors)
 		{
 			if (actor.getOwner() == player)
 				actor.setIsPlayback(false);
 		}
 		
 		return true;
 	}
 	
 }
