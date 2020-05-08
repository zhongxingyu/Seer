 package nl.naxanria.showdown.events;
 
import nl.naxanria.showdown.Core;
 import nl.naxanria.showdown.handlers.PlayerHandler;
 import no.runsafe.framework.api.event.player.IPlayerDeathEvent;
 import no.runsafe.framework.minecraft.event.player.RunsafePlayerDeathEvent;
 
 public class PlayerDeath implements IPlayerDeathEvent
 {
 
	public PlayerDeath(PlayerHandler playerHandler, Core core)
 	{
 		this.playerHandler = playerHandler;
		this.core = core;
 	}
 
 	@Override
 	public void OnPlayerDeathEvent(RunsafePlayerDeathEvent event)
 	{
 		if (playerHandler.isInGame(event.getEntity()))
 		{
 			playerHandler.removePlayer(event.getEntity());
			if(playerHandler.getAmountPlayers() == 1)
				core.winner();

 		}
 	}
 
 	private final PlayerHandler playerHandler;
	private final Core core;
 
 }
