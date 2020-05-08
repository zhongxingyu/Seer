 package no.runsafe.warpdrive;
 
 import no.runsafe.framework.api.*;
import no.runsafe.framework.api.event.player.IPlayerCommandPreprocessEvent;
 import no.runsafe.framework.api.event.player.IPlayerDamageEvent;
 import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
 import no.runsafe.framework.api.event.plugin.IPluginDisabled;
 import no.runsafe.framework.api.log.IConsole;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.minecraft.event.entity.RunsafeEntityDamageEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerCommandPreprocessEvent;
 import no.runsafe.framework.timer.ForegroundWorker;
 import no.runsafe.warpdrive.database.SmartWarpChunkRepository;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class SmartWarpDrive extends ForegroundWorker<String, ILocation>
	implements IPlayerDamageEvent, IPlayerCommandPreprocessEvent, IConfigurationChanged, IPluginDisabled
 {
 	public SmartWarpDrive(IScheduler scheduler, SmartWarpChunkRepository smartWarpChunks, Engine engine, IServer server, IConsole console)
 	{
 		super(scheduler);
 		this.scheduler = scheduler;
 		this.smartWarpChunks = smartWarpChunks;
 		this.engine = engine;
 		this.server = server;
 		this.console = console;
 		setInterval(10);
 	}
 
 	public void EngageSurface(IPlayer player, IWorld target, boolean lock)
 	{
 		if (lockedSurfaceLocation != null)
 		{
 			if (skyFall)
 				fallen.add(player.getName());
 			Push(player.getName(), lockedSurfaceLocation);
 			return;
 		}
 		ILocation candidate = scan(target, false);
 		if (candidate == null)
 			return;
 		if (skyFall)
 		{
 			fallen.add(player.getName());
 			candidate.setY(300);
 			candidate.setPitch(90);
 		}
 		if (lock)
 		{
 			lockedSurfaceLocation = candidate;
 			scheduler.startSyncTask(new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					unlockSurface();
 				}
 			}, 20);
 		}
 		Push(player.getName(), candidate);
 	}
 
 	public void EngageCave(IPlayer player, IWorld target, boolean lock)
 	{
 		if (lockedCaveLocation != null)
 		{
 			Push(player.getName(), lockedCaveLocation);
 			return;
 		}
 		ILocation candidate = scan(target, true);
 		if (candidate == null)
 			return;
 		if (lock)
 		{
 			lockedCaveLocation = candidate;
 			scheduler.startSyncTask(new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					unlockCave();
 				}
 			}, 20);
 		}
 		Push(player.getName(), candidate);
 	}
 
 	@Override
 	public void process(String playerName, ILocation target)
 	{
 		IPlayer player = server.getPlayerExact(playerName);
 		if (player == null)
 			return;
 
 		if (!player.teleport(target) && fallen.contains(playerName))
 			fallen.remove(playerName);
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		skyFall = configuration.getConfigValueAsBoolean("smart.skyfall");
 	}
 
 	@Override
 	public void OnPlayerDamage(IPlayer player, RunsafeEntityDamageEvent event)
 	{
 		if (event.getCause() == RunsafeEntityDamageEvent.RunsafeDamageCause.FALL)
 		{
 			if (fallen.contains(player.getName()))
 			{
 				event.cancel();
 				fallen.remove(player.getName());
 			}
 		}
 	}
 
 	@Override
	public void OnBeforePlayerCommand(RunsafePlayerCommandPreprocessEvent event)
	{
		if (fallen.contains(event.getPlayer().getName()))
			event.cancel();
	}

	@Override
 	public void OnPluginDisabled()
 	{
 		if (!fallen.isEmpty())
 			console.logInformation("Teleporting %d falling players due to plugin shutdown.", fallen.size());
 		for (String playerName : fallen)
 		{
 			IPlayer player = server.getPlayerExact(playerName);
 			if (player != null)
 				player.teleport(player.getLocation().findTop());
 		}
 	}
 
 	private ILocation scan(IWorld target, boolean cave)
 	{
 		while (true)
 		{
 			ILocation candidate = smartWarpChunks.getTarget(target, true);
 			if (candidate == null)
 				return null;
 			if (engine.targetFloorIsSafe(candidate, true))
 			{
 				candidate.incrementX(0.5);
 				candidate.incrementZ(0.5);
 				return candidate;
 			}
 			smartWarpChunks.setUnsafe(candidate);
 		}
 	}
 
 	private void unlockCave()
 	{
 		lockedCaveLocation = null;
 	}
 
 	private void unlockSurface()
 	{
 		lockedSurfaceLocation = null;
 	}
 
 	private ILocation lockedCaveLocation;
 	private ILocation lockedSurfaceLocation;
 	private boolean skyFall = false;
 	private final List<String> fallen = new ArrayList<String>(0);
 	private final IScheduler scheduler;
 	private final SmartWarpChunkRepository smartWarpChunks;
 	private final Engine engine;
 	private final IServer server;
 	private final IConsole console;
 }
