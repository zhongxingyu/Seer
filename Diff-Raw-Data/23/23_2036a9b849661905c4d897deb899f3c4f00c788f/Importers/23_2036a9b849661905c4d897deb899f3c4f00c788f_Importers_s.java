 package no.runsafe.creativetoolbox;
 
 import no.runsafe.creativetoolbox.database.PlotEntrance;
 import no.runsafe.creativetoolbox.database.PlotEntranceRepository;
 import no.runsafe.creativetoolbox.database.PlotLogRepository;
 import no.runsafe.creativetoolbox.database.PlotMemberRepository;
 import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.IDebug;
 import no.runsafe.framework.api.IWorld;
 import no.runsafe.framework.api.event.IServerReady;
 import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
 import no.runsafe.worldguardbridge.WorldGuardInterface;
 
 import java.util.List;
 
 public class Importers implements IConfigurationChanged, IServerReady
 {
 	public Importers(PlotManager manager, WorldGuardInterface worldGuard, PlotLogRepository logRepository, IDebug console, PlotMemberRepository memberRepository, PlotEntranceRepository plotEntrance, PlotFilter plotFilter)
 	{
 		this.manager = manager;
 		this.worldGuard = worldGuard;
 		this.logRepository = logRepository;
 		this.console = console;
 		this.memberRepository = memberRepository;
 		this.plotEntrance = plotEntrance;
 		this.plotFilter = plotFilter;
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		manager.OnConfigurationChanged(configuration);
 		plotEntrance.OnConfigurationChanged(configuration);
 		if (!configuration.getConfigValueAsBoolean("imported.plots"))
 		{
 			importRegions();
 			configuration.setConfigValue("imported.plots", true);
 		}
 		if (!configuration.getConfigValueAsBoolean("imported.entrances"))
 		{
 			importEntrances();
 			configuration.setConfigValue("imported.entrances", true);
 		}
 		configuration.save();
 	}
 
 	@Override
 	public void OnServerReady()
 	{
 		importEntrances();
 	}
 
 	private void importRegions()
 	{
 		IWorld world = manager.getWorld();
 		List<String> regions = worldGuard.getRegionsInWorld(world);
 		int members = 0;
 		int owners = 0;
 		for (String region : regions)
 		{
 			String claim = logRepository.getClaim(region);
 			if (claim == null)
 				if (!logRepository.log(region, "unknown"))
 					console.logWarning("Unable to import region &c%s&r to claim repository!", region);
 
 			for (String member : worldGuard.getMembers(world, region))
 			{
 				memberRepository.addMember(region, member, false);
 				members++;
 			}
 
 			for (String member : worldGuard.getOwners(world, region))
 			{
 				memberRepository.addMember(region, member, true);
 				owners++;
 			}
 		}
 		console.logInformation("Imported &a%d&r owners and &a%d&r members to &a%d&r plots.", owners, members, regions.size());
 	}
 
 	private void importEntrances()
 	{
 		List<String> skip = plotEntrance.getPlots();
 		List<String> all = plotFilter.apply(logRepository.getPlots());
 		int imported = 0;
 		for (String plot : all)
 		{
 			if (skip.contains(plot))
 			{
 				console.debugFiner("Plot %s entrance is already stored", plot);
 				continue;
 			}
 
 			PlotEntrance entrance = plotEntrance.get(plot);
 			if (entrance != null && entrance.getLocation() != null)
 			{
 				console.debugFine("Plot %s entrance is not stored, but exists?!", plot);
 				continue;
 			}
 
 			PlotEntrance store = new PlotEntrance();
 			store.setName(plot);
 			store.setLocation(manager.getPlotEntrance(plot));
 			if (store.getLocation() == null)
 				console.logWarning("Unable to get entrance for plot '%s'", plot);
 			else
 			{
 				plotEntrance.persist(store);
 				imported++;
 			}
 		}
 		console.logInformation("Imported %d entrances to database.", imported);
 	}
 
 	private final PlotManager manager;
 	private final WorldGuardInterface worldGuard;
 	private final PlotLogRepository logRepository;
 	private final IDebug console;
 	private final PlotMemberRepository memberRepository;
 	private final PlotEntranceRepository plotEntrance;
 	private final PlotFilter plotFilter;
 }
