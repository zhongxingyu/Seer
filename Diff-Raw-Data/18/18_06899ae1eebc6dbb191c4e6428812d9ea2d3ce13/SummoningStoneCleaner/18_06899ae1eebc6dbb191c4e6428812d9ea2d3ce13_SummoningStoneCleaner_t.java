 package no.runsafe.warpdrive.summoningstone;
 
 import no.runsafe.framework.api.ILocation;
 import no.runsafe.framework.api.IScheduler;
 import no.runsafe.framework.api.event.plugin.IPluginDisabled;
 import no.runsafe.framework.api.event.plugin.IPluginEnabled;
import no.runsafe.framework.api.log.IConsole;
 
 import java.util.Map;
 
 public class SummoningStoneCleaner implements IPluginEnabled, IPluginDisabled
 {
	public SummoningStoneCleaner(SummoningEngine summoningEngine, SummoningStoneRepository summoningStoneRepository, IConsole output, IScheduler scheduler)
 	{
 		this.summoningEngine = summoningEngine;
 		this.summoningStoneRepository = summoningStoneRepository;
		this.console = output;
 		this.scheduler = scheduler;
 	}
 
 	@Override
 	public void OnPluginDisabled()
 	{
 		// Server shutting down, clean up any stones in the world.
 		for (Map.Entry<Integer, SummoningStone> node : this.summoningEngine.getLoadedStones().entrySet())
 		{
 			SummoningStone stone = node.getValue();
 			stone.remove();
 
 			if (stone.hasTimer())
 				this.scheduler.cancelTask(stone.getTimerID());
 
 			this.summoningStoneRepository.deleteSummoningStone(node.getKey());
			this.console.logInformation("Removing summoning portal: %s", stone.getLocation());
 		}
 	}
 
 	@Override
 	public void OnPluginEnabled()
 	{
 		// If we have any stones stored here, the server crashed, reset them all.
 		for (ILocation stoneLocation : this.summoningStoneRepository.getStoneList())
 		{
 			SummoningStone stone = new SummoningStone(stoneLocation);
 			stone.reset();
			this.console.logInformation("Reset leftover portal: %s", stoneLocation);
 		}
 
 		// Wipe any stones we had remembered
 		this.summoningStoneRepository.wipeStoneList();
 	}
 
 	private final SummoningEngine summoningEngine;
 	private final SummoningStoneRepository summoningStoneRepository;
	private final IConsole console;
 	private final IScheduler scheduler;
 }
