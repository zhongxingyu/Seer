 package no.runsafe.toybox.command;
 
 import no.runsafe.framework.api.command.player.PlayerCommand;
 import no.runsafe.framework.api.event.player.IPlayerInteractEvent;
 import no.runsafe.framework.minecraft.block.RunsafeBlock;
 import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEvent;
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 import no.runsafe.toybox.handlers.LockedObjectHandler;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class Lock extends PlayerCommand implements IPlayerInteractEvent
 {
 	public Lock(LockedObjectHandler handler)
 	{
 		super("lock", "Locks an object.", "runsafe.toybox.lock");
 		this.handler = handler;
 	}
 
 	@Override
 	public String OnExecute(RunsafePlayer executor, HashMap<String, String> parameters)
 	{
 		String playerName = executor.getName();
 		if (this.lockingPlayers.contains(playerName))
 		{
 			this.lockingPlayers.remove(playerName);
 			return "&eLocking disabled.";
 		}
 		else
 		{
 			this.lockingPlayers.add(playerName);
 			return "&2Locking enabled: Right click objects to lock them.";
 		}
 	}
 
 	@Override
 	public void OnPlayerInteractEvent(RunsafePlayerInteractEvent event)
 	{
 		RunsafeBlock block = event.getBlock();
 		RunsafePlayer player = event.getPlayer();
 		String playerName = player.getName();
 
 		if (block != null)
 		{
 			if (this.lockingPlayers.contains(playerName))
 			{
 				if (this.handler.isLockedBlock(block))
 				{
 					this.handler.unlockBlock(block);
 					player.sendColouredMessage("&2Object unlocked");
 				}
 				else
 				{
 					if (this.handler.canLockBlock(block))
 					{
 						this.handler.lockBlock(block);
 						player.sendColouredMessage("&2Object locked.");
 					}
 					else
 					{
 						player.sendColouredMessage("&cYou cannot lock that object.");
 					}
 				}
				event.cancel();
 			}
 			else if (this.handler.isLockedBlock(block))
 			{
 				player.sendColouredMessage("&cThat object has been locked by wizards.");
				event.cancel();
 			}
 		}
 	}
 
 	private List<String> lockingPlayers = new ArrayList<String>();
 	private LockedObjectHandler handler;
 }
