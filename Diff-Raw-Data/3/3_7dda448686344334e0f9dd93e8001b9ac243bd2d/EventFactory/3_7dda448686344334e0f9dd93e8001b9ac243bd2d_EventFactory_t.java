 package no.runsafe.vanishbridge;
 
 import no.runsafe.framework.server.event.player.RunsafeCustomEvent;
import no.runsafe.framework.wrapper.ObjectWrapper;
 import org.bukkit.entity.Player;
 
 public class EventFactory
 {
 	public void Fire(Player player, boolean vanished)
 	{
 		new RunsafeCustomEvent(ObjectWrapper.convert(player), "vanished", vanished).Fire();
 	}
 }
