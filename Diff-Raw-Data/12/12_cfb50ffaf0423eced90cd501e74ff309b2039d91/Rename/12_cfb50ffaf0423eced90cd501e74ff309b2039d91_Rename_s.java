 package com.insofar.actor.commands.author;
 
 import com.insofar.actor.ActorAPI;
 import com.insofar.actor.EntityActor;
 import com.insofar.actor.Recording;
 import com.insofar.actor.permissions.PermissionHandler;
 import com.insofar.actor.permissions.PermissionNode;
 
 /**
  * ActorPlugin command to rename an actor
  * 
  * @author Joshua Weinberg
  *
  */
 public class Rename extends AuthorBaseCommand {
 
 	public Rename()
 	{
 		super();
 	}
 	
 	/*************************************************************************
 	 * 
 	 * BUKKIT COMMAND
 	 * 
 	*************************************************************************/
 
 	@Override
 	/**
 	 * rename an actor
 	 */
 	public boolean execute()
 	{
 		if (!PermissionHandler.has(player, PermissionNode.COMMAND_RENAME))
 		{
 			player.sendMessage("Lack permission: "
 					+ PermissionNode.COMMAND_RENAME.getNode());
 			return true;
 		}
 		if (args.length != 3)
 		{
 			player.sendMessage("usage: /actor rename currentName newName");
 			return true;
 		}
 		String actorName = args[1];
 		String newName = args[2];
 		
 		int count = 0;
		for (EntityActor actor : plugin.actors)
 		{
 			if (actor.getOwner() == player && (actor.getActorName().equals(actorName)))
 			{
 				Recording recording = actor.getRecording();
 				Fire.doFire(actor, player, true);
 				ActorAPI.actor(recording, newName, player.getWorld());
 				count++;
 			}
 		}
 		
 		if (count>0)
 		{
 			player.sendMessage("Renamed "+count+" actor" + (count == 1 ? "" : "s") + ".");
 		}
 		
 		return true;
 	}
 }
