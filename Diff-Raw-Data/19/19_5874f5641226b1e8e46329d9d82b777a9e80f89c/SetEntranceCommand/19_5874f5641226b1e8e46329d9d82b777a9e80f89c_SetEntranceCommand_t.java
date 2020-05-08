 package no.runsafe.creativetoolbox.command;
 
 import no.runsafe.creativetoolbox.PlotFilter;
 import no.runsafe.creativetoolbox.database.PlotEntrance;
 import no.runsafe.creativetoolbox.database.PlotEntranceRepository;
 import no.runsafe.framework.command.RunsafeAsyncPlayerCommand;
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.framework.timer.IScheduler;
 import no.runsafe.worldguardbridge.WorldGuardInterface;
 
 import java.util.List;
 
 public class SetEntranceCommand extends RunsafeAsyncPlayerCommand
 {
 	public SetEntranceCommand(
 		PlotEntranceRepository repository,
 		IOutput output,
 		PlotFilter filter,
		IScheduler scheduler,
		WorldGuardInterface worldGuard
 	)
 	{
 		super("setentrance", scheduler);
 		this.repository = repository;
 		this.console = output;
 		this.plotFilter = filter;
		this.worldGuard = worldGuard;
 	}
 
 	@Override
 	public boolean CanExecute(RunsafePlayer player, String[] args)
 	{
 		String region = getCurrentRegion(player);
 		console.fine(String.format("Player is in region %s", region));
 		if (worldGuard.getOwners(player.getWorld(), region).contains(player.getName().toLowerCase()))
 			return true;
 
 		return player.hasPermission("runsafe.creative.entrance.set");
 	}
 
 	@Override
 	public String OnExecute(RunsafePlayer executor, String[] args)
 	{
 		String currentRegion = getCurrentRegion(executor);
 		if (currentRegion == null)
 			return "No plot at your current location.";
 
 		PlotEntrance entrance = new PlotEntrance();
 		entrance.setName(currentRegion);
 		entrance.setLocation(executor.getLocation());
 		repository.persist(entrance);
 		return String.format("Entrance for %s set.", currentRegion);
 	}
 
 	private String getCurrentRegion(RunsafePlayer player)
 	{
 		List<String> regions = plotFilter.apply(worldGuard.getRegionsAtLocation(player.getLocation()));
 		if (regions == null || regions.size() == 0)
 			return null;
 		return regions.get(0);
 	}
 
	private final PlotEntranceRepository repository;
	private final IOutput console;
	private final PlotFilter plotFilter;
	private final WorldGuardInterface worldGuard;
 }
