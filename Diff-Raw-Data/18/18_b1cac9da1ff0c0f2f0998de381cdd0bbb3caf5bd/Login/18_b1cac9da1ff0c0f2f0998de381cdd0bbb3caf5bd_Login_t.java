 package no.runsafe.UserControl.events;
 
 import no.runsafe.UserControl.LoginRedirectManager;
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.event.IConfigurationChanged;
 import no.runsafe.framework.event.player.IPlayerJoinEvent;
 import no.runsafe.framework.event.player.IPlayerPreLoginEvent;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.RunsafeWorld;
 import no.runsafe.framework.server.event.player.RunsafePlayerJoinEvent;
 import no.runsafe.framework.server.event.player.RunsafePlayerPreLoginEvent;
 import no.runsafe.framework.server.player.RunsafePlayer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Login implements IPlayerJoinEvent, IConfigurationChanged, IPlayerPreLoginEvent
 {
 	public Login(LoginRedirectManager loginRedirectManager)
 	{
 		this.loginRedirectManager = loginRedirectManager;
 	}
 
 	@Override
 	public void OnBeforePlayerLogin(RunsafePlayerPreLoginEvent event)
 	{
 		if (event.getPlayer().isNew())
 			this.newPlayers.add(event.getPlayer().getName());
 	}
 
 	@Override
 	public void OnPlayerJoinEvent(RunsafePlayerJoinEvent event)
 	{
 		RunsafePlayer player = event.getPlayer();
 		if (this.loginRedirectManager.hasRedirectLocation())
 		{
 			player.teleport(this.loginRedirectManager.getRedirectLocation());
 		}
		else if (this.newPlayers.contains(player.getName()) && firstSpawnLocation != null)
 		{
 			player.teleport(firstSpawnLocation);
 			this.newPlayers.remove(player.getName());
 		}
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration config)
 	{
		String initialWorld = config.getConfigValueAsString("firstJoinLocation.world");
		this.firstSpawnLocation = null;
		if (initialWorld != null)
		{
			RunsafeWorld world = RunsafeServer.Instance.getWorld(initialWorld);
			this.firstSpawnLocation = new RunsafeLocation(
				world,
 				config.getConfigValueAsDouble("firstJoinLocation.x"),
 				config.getConfigValueAsDouble("firstJoinLocation.y"),
 				config.getConfigValueAsDouble("firstJoinLocation.z"),
 				config.getConfigValueAsFloat("firstJoinLocation.yaw"),
 				config.getConfigValueAsFloat("firstJoinLocation.pitch")
			);
		}
 	}
 
 	private List<String> newPlayers = new ArrayList<String>();
 	private RunsafeLocation firstSpawnLocation;
 	private LoginRedirectManager loginRedirectManager;
 }
