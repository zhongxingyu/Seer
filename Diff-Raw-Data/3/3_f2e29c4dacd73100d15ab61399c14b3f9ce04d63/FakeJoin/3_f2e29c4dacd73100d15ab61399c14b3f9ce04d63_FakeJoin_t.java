 package no.runsafe.vanishbridge.command;
 
 import no.runsafe.framework.command.RunsafeAsyncPlayerCommand;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.event.player.RunsafePlayerJoinEvent;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.framework.timer.IScheduler;
 import no.runsafe.vanishbridge.PlayerVanishManager;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class FakeJoin extends RunsafeAsyncPlayerCommand
 {
 	public FakeJoin(IScheduler scheduler, PlayerVanishManager playerVanishManager)
 	{
 		super("fakejoin", scheduler);
 		manager = playerVanishManager;
 	}
 
 	@Override
 	public String requiredPermission()
 	{
 		return "runsafe.vanish.fakequit";
 	}
 
 	@Override
 	public String OnExecute(RunsafePlayer player, String[] strings)
 	{
		manager.setVanished(player, false);
 		RunsafePlayerJoinEvent fake = new RunsafePlayerJoinEvent(new PlayerJoinEvent(player.getRawPlayer(), null));
 		fake.Fire();
 		RunsafeServer.Instance.broadcastMessage(fake.getJoinMessage());
 		return null;
 	}
 
 	PlayerVanishManager manager;
 }
