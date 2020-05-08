 package no.runsafe.creativetoolbox;
 
 import no.runsafe.creativetoolbox.database.*;
 import no.runsafe.creativetoolbox.event.PlotApprovedEvent;
 import no.runsafe.framework.api.IConfiguration;
 import no.runsafe.framework.api.IDebug;
 import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
 import no.runsafe.framework.api.event.plugin.IPluginEnabled;
 import no.runsafe.framework.minecraft.RunsafeLocation;
 import no.runsafe.framework.minecraft.RunsafeServer;
 import no.runsafe.framework.minecraft.RunsafeWorld;
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 import no.runsafe.worldguardbridge.WorldGuardInterface;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.joda.time.Period;
 import org.joda.time.PeriodType;
 import org.joda.time.format.PeriodFormat;
 
 import java.awt.geom.Rectangle2D;
 import java.util.*;
 
 public class PlotManager implements IConfigurationChanged, IPluginEnabled
 {
 	public PlotManager(
 		PlotFilter plotFilter,
 		WorldGuardInterface worldGuardInterface,
 		PlotEntranceRepository plotEntranceRepository,
 		ApprovedPlotRepository approvedPlotRepository,
 		PlotVoteRepository voteRepository, PlotCalculator plotCalculator,
 		IDebug debugger,
 		PlotLogRepository plotLog)
 	{
 		filter = plotFilter;
 		worldGuard = worldGuardInterface;
 		plotEntrance = plotEntranceRepository;
 		plotApproval = approvedPlotRepository;
 		this.voteRepository = voteRepository;
 		calculator = plotCalculator;
 		console = debugger;
 		this.plotLog = plotLog;
 	}
 
 	public String getCurrentRegionFiltered(RunsafePlayer player)
 	{
 		List<String> regions = filter.apply(worldGuard.getRegionsAtLocation(player.getLocation()));
 		if (regions == null || regions.size() == 0)
 			return null;
 		return regions.get(0);
 	}
 
 	public java.util.List<RunsafeLocation> getPlotEntrances()
 	{
 		ArrayList<RunsafeLocation> entrances = new ArrayList<RunsafeLocation>();
 		for (String plot : filter.getFiltered())
 			entrances.add(plotEntrance.get(plot).getLocation());
 		return entrances;
 	}
 
 	public java.util.List<RunsafeLocation> getFreePlotEntrances()
 	{
 		return freePlots;
 	}
 
 	public boolean plotIsTaken(RunsafeLocation location)
 	{
 		List<String> regions = worldGuard.getRegionsAtLocation(location);
 		if (regions == null || regions.isEmpty())
 			return false;
 		boolean ok = true;
 		for (String region : regions)
 			if (!ignoredRegions.contains(region))
 				ok = false;
 		if (!ok)
 		{
 			setTaken(calculator.getColumn(location.getBlockX()), calculator.getRow(location.getBlockZ()));
 			freePlots.remove(location);
 		}
 		return !ok;
 	}
 
 	public RunsafeLocation getPlotEntrance(String plot)
 	{
 		if (world == null)
 			return null;
 		PlotEntrance entrance = plotEntrance.get(plot);
 		Rectangle2D rect = worldGuard.getRectangle(world, plot);
 		if (rect == null)
 			return null;
 		if (entrance != null)
 		{
 			if (!rect.contains(entrance.getLocation().getBlockX(), entrance.getLocation().getBlockZ()))
 				plotEntrance.delete(plot);
 			else
 				return entrance.getLocation();
 		}
 		return calculator.getDefaultEntrance(worldGuard.getRegionLocation(world, plot));
 	}
 
 	public Map<String, String> getOldPlots()
 	{
 		if (!worldGuard.serverHasWorldGuard())
 			return null;
 
 		List<String> approvedPlots = plotApproval.getApprovedPlots();
 		Map<String, Set<String>> checkList = worldGuard.getAllRegionsWithOwnersInWorld(getWorld());
 		Map<String, String> hits = new HashMap<String, String>();
 		for (String region : filter.apply(new ArrayList<String>(checkList.keySet())))
 		{
 			if (approvedPlots.contains(region))
 				continue;
 
 			Duration status = getPlotStatus(checkList.get(region));
 			if (status != null && (status.equals(Duration.ZERO) || status.isShorterThan(limit)))
 				continue;
 			hits.put(region, formatReason(status));
 		}
 		return hits;
 	}
 
 	private String formatReason(Duration status)
 	{
 		if (status == null)
 			return "&cnull&r";
 
 		if (status.equals(BANNED))
 			return "&cbanned&r";
 
 		return PeriodFormat.getDefault().print(new Period(status, DateTime.now(), PeriodType.yearMonthDay()));
 	}
 
 	private Duration getPlotStatus(Set<String> owners)
 	{
 		Duration result = null;
 		for (String owner : owners)
 		{
 			Duration ownerSeen = getSeen(owner);
 			if (ownerSeen == null)
 				return null;
 			if (ownerSeen.isEqual(Duration.ZERO))
 				return Duration.ZERO;
 			if (result == null || !result.isEqual(BANNED))
 				result = ownerSeen;
 		}
 		return result;
 	}
 
 	private Duration getSeen(String playerName)
 	{
 		playerName = playerName.toLowerCase();
 		if (lastSeen.containsKey(playerName))
 			return lastSeen.get(playerName);
 
 		RunsafePlayer player = RunsafeServer.Instance.getPlayer(playerName);
 		if (player == null)
 			return null;
 		if (player.isOnline())
 			lastSeen.put(playerName, Duration.ZERO);
 		else if (player.isBanned())
 			lastSeen.put(playerName, BANNED);
 		else
 		{
 			DateTime logout = player.lastLogout();
 			if (logout == null)
 				lastSeen.put(playerName, null);
 			else
 				lastSeen.put(playerName, new Duration(player.lastLogout(), DateTime.now()));
 		}
 		return lastSeen.get(playerName);
 	}
 
 	public String getOldPlotPointer(RunsafePlayer player)
 	{
 		if (oldPlotPointers.containsKey(player.getName()))
 			return oldPlotPointers.get(player.getName());
 		return null;
 	}
 
 	public void setOldPlotPointer(RunsafePlayer player, String value)
 	{
 		oldPlotPointers.put(player.getName(), value);
 	}
 
 	public Map<String, String> getOldPlotWorkList(RunsafePlayer player)
 	{
 		if (!oldPlotList.containsKey(player.getName()))
 			oldPlotList.put(player.getName(), getOldPlots());
 		return oldPlotList.get(player.getName());
 	}
 
 	public void clearOldPlotWorkList(RunsafePlayer player)
 	{
 		if (oldPlotList.containsKey(player.getName()))
 			oldPlotList.remove(player.getName());
 	}
 
 	public boolean voteValid(RunsafePlayer player, String region)
 	{
		return !worldGuard.getOwners(world, region).contains(player.getName())
 			&& !worldGuard.getMembers(world, region).contains(player.getName());
 	}
 
 	public boolean vote(RunsafePlayer player, String region)
 	{
 		boolean voted = voteRepository.recordVote(player, region);
 		int score = voteRepository.tally(region, voteranks);
 		if (score >= autoApprove)
 		{
 			PlotApproval approval = plotApproval.get(region);
 			if (approval == null)
 				approve("Popular vote", region);
 		}
 		return voted;
 	}
 
 	public PlotApproval approve(String approver, String plot)
 	{
 		PlotApproval approval = new PlotApproval();
 		approval.setApproved(DateTime.now());
 		approval.setApprovedBy(approver);
 		approval.setName(plot);
 		plotApproval.persist(approval);
 		approval = plotApproval.get(plot);
 		if (approval != null)
 		{
 			for (String owner : worldGuard.getOwners(world, plot))
 			{
 				int approved = 0;
 				for (String region : worldGuard.getOwnedRegions(RunsafeServer.Instance.getOfflinePlayerExact(owner), world))
 					if (plotApproval.get(region) != null)
 						approved++;
 
 				new PlotApprovedEvent(owner, approval, approved).Fire();
 			}
 		}
 		return approval;
 	}
 
 	public boolean claim(RunsafePlayer claimer, RunsafePlayer owner, String plotName, Rectangle2D region)
 	{
 		if (!claimer.getWorld().equals(world))
 			return false;
 		if (worldGuard.createRegion(
 			owner, world, plotName,
 			calculator.getMinPosition(world, region),
 			calculator.getMaxPosition(world, region)
 		))
 		{
 			plotLog.log(plotName, claimer.getName());
 			setTaken(calculator.getColumn((long) region.getCenterX()), calculator.getRow((long) region.getCenterY()));
 			return true;
 		}
 		return false;
 	}
 
 	public void extendPlot(RunsafePlayer player, String target, RunsafeLocation location)
 	{
 		if (!player.getWorld().equals(world))
 			return;
 
 		Rectangle2D area = worldGuard.getRectangle(world, target);
 		PlotDimension currentSize = calculator.getPlotDimensions(area);
 		PlotDimension targetSize = currentSize.expandToInclude(calculator.getPlotArea(location));
 		ScanTakenPlots();
 		long firstCol = currentSize.getMinimumColumn();
 		long lastCol = currentSize.getMaximumColumn();
 		long firstRow = currentSize.getMinimumRow();
 		long lastRow = currentSize.getMaximumRow();
 		long targetCol = targetSize.getMaximumColumn();
 		long targetRow = targetSize.getMaximumRow();
 		console.fine("Extending plot %s to %s", currentSize, targetSize);
 		for (long column = targetSize.getMinimumColumn(); column <= targetCol; ++column)
 		{
 			for (long row = targetSize.getMinimumRow(); row <= targetRow; ++row)
 			{
 				if (column >= firstCol && column <= lastCol && row >= firstRow && row <= lastRow)
 					continue;
 
 				if (isTaken(column, row))
 				{
 					console.fine("Plot (%d,%d) is taken!", column, row);
 					player.sendColouredMessage("Unable to extend plot here, overlap detected!");
 					return;
 				}
 			}
 		}
 		if (worldGuard.redefineRegion(world, target, targetSize.getMinPosition(), targetSize.getMaxPosition()))
 		{
 			for (long column = targetSize.getMinimumColumn(); column <= targetCol; ++column)
 				for (long row = targetSize.getMinimumRow(); row <= targetRow; ++row)
 					setTaken(column, row);
 
 			player.sendColouredMessage("The plot has been extended!");
 		}
 		else
 			player.sendColouredMessage("An error occurred while extending plot.");
 	}
 
 	public void delete(String region)
 	{
 		Rectangle2D area = worldGuard.getRectangle(world, region);
 		long col = calculator.getColumn((int) area.getCenterX());
 		long row = calculator.getRow((int) area.getCenterY());
 		setFree(col, row);
 		worldGuard.deleteRegion(filter.getWorld(), region);
 		plotEntrance.delete(region);
 	}
 
 	RunsafeWorld getWorld()
 	{
 		return world;
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration config)
 	{
 		world = RunsafeServer.Instance.getWorld(config.getConfigValueAsString("world"));
 		ignoredRegions = config.getConfigValueAsList("free.ignore");
 		limit = new Period(0, 0, 0, config.getConfigValueAsInt("old_after"), 0, 0, 0, 0).toDurationTo(DateTime.now());
 		autoApprove = config.getConfigValueAsInt("vote.approved");
 		voteranks = config.getConfigValuesAsIntegerMap("vote.rank");
 	}
 
 	@Override
 	public void OnPluginEnabled()
 	{
 		ScanTakenPlots();
 		ScanFreePlots();
 	}
 
 	private void ScanTakenPlots()
 	{
 		Map<String, Rectangle2D> taken = worldGuard.getRegionRectanglesInWorld(filter.getWorld());
 		if (taken != null)
 			for (String region : taken.keySet())
 			{
 				if (!ignoredRegions.contains(region))
 				{
 					long col = calculator.getColumn((int) taken.get(region).getCenterX());
 					long row = calculator.getRow((int) taken.get(region).getCenterY());
 					setTaken(col, row);
 				}
 			}
 	}
 
 	private boolean isTaken(long col, long row)
 	{
 		return takenPlots.containsKey(col) && takenPlots.get(col).contains(row);
 	}
 
 	private void setFree(long col, long row)
 	{
 		if (takenPlots.containsKey(col))
 			takenPlots.get(col).remove(row);
 	}
 
 	private void setTaken(long col, long row)
 	{
 		if (!takenPlots.containsKey(col))
 			takenPlots.put(col, new ArrayList<Long>());
 		if (!takenPlots.get(col).contains(row))
 			takenPlots.get(col).add(row);
 	}
 
 	private void ScanFreePlots()
 	{
 		if (world != null)
 			for (long column : calculator.getColumns())
 				for (long row : calculator.getRows())
 					if (!(takenPlots.containsKey(column) && takenPlots.get(column).contains(row)))
 						freePlots.add(calculator.getDefaultEntrance(column, row));
 	}
 
 	private static final Duration BANNED = new Duration(Long.MAX_VALUE);
 	private final PlotFilter filter;
 	private final WorldGuardInterface worldGuard;
 	private final PlotEntranceRepository plotEntrance;
 	private final ApprovedPlotRepository plotApproval;
 	private final PlotVoteRepository voteRepository;
 	private final PlotCalculator calculator;
 	private final IDebug console;
 	private final PlotLogRepository plotLog;
 	private final Map<String, String> oldPlotPointers = new HashMap<String, String>();
 	private final Map<String, Map<String, String>> oldPlotList = new HashMap<String, Map<String, String>>();
 	private final HashMap<String, Duration> lastSeen = new HashMap<String, Duration>();
 	private final HashMap<Long, ArrayList<Long>> takenPlots = new HashMap<Long, ArrayList<Long>>();
 	private final ArrayList<RunsafeLocation> freePlots = new ArrayList<RunsafeLocation>();
 	private RunsafeWorld world;
 	private List<String> ignoredRegions;
 	private Duration limit;
 	private int autoApprove;
 	private Map<String, Integer> voteranks;
 }
