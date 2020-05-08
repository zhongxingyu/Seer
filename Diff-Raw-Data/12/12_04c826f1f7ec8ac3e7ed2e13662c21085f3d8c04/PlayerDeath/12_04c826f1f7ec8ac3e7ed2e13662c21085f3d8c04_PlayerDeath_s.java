 package nl.naxanria.showdown.events;
 
 import nl.naxanria.showdown.handlers.PlayerHandler;
 import no.runsafe.framework.api.event.player.IPlayerDeathEvent;
 import no.runsafe.framework.minecraft.event.player.RunsafePlayerDeathEvent;
 
 public class PlayerDeath implements IPlayerDeathEvent
 {
 
	public PlayerDeath(PlayerHandler playerHandler)
 	{
 		this.playerHandler = playerHandler;
 	}
 
 	@Override
 	public void OnPlayerDeathEvent(RunsafePlayerDeathEvent event)
 	{
 		if (playerHandler.isInGame(event.getEntity()))
 		{
 			playerHandler.removePlayer(event.getEntity());
 		}
 	}
 
 	private final PlayerHandler playerHandler;
 
 }
