 package no.runsafe.creativetoolbox;
 
 import no.runsafe.creativetoolbox.database.*;
 import no.runsafe.creativetoolbox.event.PlotApprovedEvent;
 import no.runsafe.creativetoolbox.event.PlotDeletedEvent;
 import no.runsafe.framework.api.IConfiguration;
 import no.runsafe.framework.api.ILocation;
 import no.runsafe.framework.api.IServer;
 import no.runsafe.framework.api.IWorld;
 import no.runsafe.framework.api.event.IServerReady;
 import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
 import no.runsafe.framework.api.hook.IPlayerDataProvider;
 import no.runsafe.framework.api.log.IConsole;
 import no.runsafe.framework.api.log.IDebug;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.worldguardbridge.IRegionControl;
 import org.apache.commons.lang.StringUtils;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.joda.time.Period;
 import org.joda.time.PeriodType;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.PeriodFormat;
 
 import java.awt.geom.Rectangle2D;
 import java.util.*;
 
 public class PlotManager implements IConfigurationChanged, IServerReady, IPlayerDataProvider
 {
 	public PlotManager(
 		PlotFilter plotFilter,
 		IRegionControl worldGuardInterface,
 		PlotEntranceRepository plotEntranceRepository,
 		ApprovedPlotRepository approvedPlotRepository,
 		PlotVoteRepository voteRepository, PlotTagRepository tagRepository, PlotMemberRepository memberRepository, PlotCalculator plotCalculator,
 		PlotMemberBlacklistRepository blackList, PlotList plotList,
 		IConsole console, IDebug debugger, IServer server, PlotLogRepository plotLog)
 	{
 		filter = plotFilter;
 		worldGuard = worldGuardInterface;
 		plotEntrance = plotEntranceRepository;
 		plotApproval = approvedPlotRepository;
 		this.voteRepository = voteRepository;
 		this.tagRepository = tagRepository;
 		this.memberRepository = memberRepository;
 		calculator = plotCalculator;
 		this.blackList = blackList;
 		this.plotList = plotList;
 		this.console = console;
 		this.debugger = debugger;
 		this.server = server;
 		this.plotLog = plotLog;
 	}
 
 	public String getCurrentRegionFiltered(IPlayer player)
 	{
 		if (!world.equals(player.getWorld()))
 			return null;
 		List<String> regions = filter.apply(worldGuard.getRegionsAtLocation(player.getLocation()));
 		if (regions == null || regions.size() == 0)
 			return null;
 		return regions.get(0);
 	}
 
 	public boolean isCurrentClaimable(IPlayer player)
 	{
 		if (!world.equals(player.getWorld()))
 			return false;
 		List<String> regions = worldGuard.getRegionsAtLocation(player.getLocation());
 		return regions == null || ignoredRegions.containsAll(regions);
 	}
 
 	public java.util.List<ILocation> getPlotEntrances()
 	{
 		ArrayList<ILocation> entrances = new ArrayList<ILocation>();
 		for (String plot : filter.getFiltered())
 			entrances.add(plotEntrance.get(plot).getLocation());
 		return entrances;
 	}
 
 	public java.util.List<ILocation> getFreePlotEntrances()
 	{
 		return freePlots;
 	}
 
 	public boolean plotIsTaken(ILocation location)
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
 
 	public ILocation getPlotEntrance(String plot)
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
 
 		IPlayer player = server.getPlayer(playerName);
 		if (player == null)
 			return null;
 		if (player.isOnline())
 			lastSeen.put(playerName, Duration.ZERO);
 		else if (!player.isNotBanned())
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
 
 	public boolean disallowVote(IPlayer player, String region)
 	{
 		return !world.equals(player.getWorld())
 			|| (voteBlacklist.containsKey(region) && voteBlacklist.get(region).contains(player.getName().toLowerCase()))
 			|| worldGuard.getOwners(world, region).contains(player.getName().toLowerCase())
 			|| worldGuard.getMembers(world, region).contains(player.getName().toLowerCase());
 	}
 
 	public boolean vote(IPlayer player, String region)
 	{
 		boolean voted = voteRepository.recordVote(player, region);
 		int score = voteRepository.tally(region, voteRanks);
 		if (score >= autoApprove)
 		{
 			PlotApproval approval = plotApproval.get(region);
 			if (approval == null)
 				approve("Popular vote", region);
 		}
 		return voted;
 	}
 
 	public List<String> tag(IPlayer player, List<String> plotNames)
 	{
 		if (plotNames == null)
 			return null;
 		List<String> tagged = new ArrayList<String>();
 		for (String plot : plotNames)
 			tagged.add(tag(player, plot));
 		return tagged;
 	}
 
 	public String tag(IPlayer player, String plot)
 	{
 		List<String> tags = new ArrayList<String>();
 		tags.add(plot);
 		if (player.hasPermission("runsafe.creative.approval.read"))
 		{
 			PlotApproval approved = plotApproval.get(plot);
 			if (approved != null && approved.getApproved() != null)
 				tags.add(String.format("&2[approved &a%s&2]&r", dateFormat.print(approved.getApproved())));
 		}
 		if (player.hasPermission("runsafe.creative.vote.tally"))
 		{
 			int voteCount = voteRepository.tally(plot);
 			if (voteCount > 0)
 				tags.add(String.format("&2[&a%d&2 vote%s]&r", voteCount, voteCount > 1 ? "s" : ""));
 		}
 		return StringUtils.join(tags, " ");
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
 				for (String region : worldGuard.getOwnedRegions(server.getOfflinePlayerExact(owner), world))
 					if (plotApproval.get(region) != null)
 						approved++;
 
 				new PlotApprovedEvent(owner, approval, approved).Fire();
 			}
 		}
 		return approval;
 	}
 
 	public boolean claim(IPlayer claimer, IPlayer owner, String plotName, Rectangle2D region)
 	{
 		if (!world.equals(claimer.getWorld()))
 			return false;
 		if (worldGuard.createRegion(
 			owner, world, plotName,
 			calculator.getMinPosition(world, region),
 			calculator.getMaxPosition(world, region)
 		))
 		{
 			voteRepository.clear(plotName);
 			PlotApproval approval = plotApproval.get(plotName);
 			if (approval != null)
 				plotApproval.delete(approval);
 			if (!plotLog.log(plotName, claimer.getName()))
 				console.logWarning("Unable to log plot %s claimed by %s", plotName, claimer.getPrettyName());
 			setTaken(calculator.getColumn((long) region.getCenterX()), calculator.getRow((long) region.getCenterY()));
 			PlotEntrance entrance = new PlotEntrance();
 			entrance.setName(plotName);
 			entrance.setLocation(calculator.getDefaultEntrance(worldGuard.getRegionLocation(world, plotName)));
 			plotEntrance.persist(entrance);
 			return true;
 		}
 		return false;
 	}
 
 	public void extendPlot(IPlayer player, String target, ILocation location)
 	{
 		if (!world.equals(player.getWorld()))
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
 		debugger.debugFine("Extending plot %s to %s", currentSize, targetSize);
 		for (long column = targetSize.getMinimumColumn(); column <= targetCol; ++column)
 		{
 			for (long row = targetSize.getMinimumRow(); row <= targetRow; ++row)
 			{
 				if (column >= firstCol && column <= lastCol && row >= firstRow && row <= lastRow)
 					continue;
 
 				if (isTaken(column, row))
 				{
 					debugger.debugFine("Plot (%d,%d) is taken!", column, row);
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
 
 	public void delete(IPlayer deletor, String region)
 	{
 		Rectangle2D area = worldGuard.getRectangle(world, region);
 		long col = calculator.getColumn((int) area.getCenterX());
 		long row = calculator.getRow((int) area.getCenterY());
 		setFree(col, row);
 		worldGuard.deleteRegion(filter.getWorld(), region);
 		plotEntrance.delete(region);
 		tagRepository.setTags(region, null);
 		voteRepository.clear(region);
 		plotLog.delete(region);
 		plotList.remove(region);
 		new PlotDeletedEvent(deletor, region).Fire();
 	}
 
 	public IWorld getWorld()
 	{
 		return world;
 	}
 
 	public boolean isInWrongWorld(IPlayer player)
 	{
 		return !world.equals(player.getWorld());
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration config)
 	{
 		world = config.getConfigValueAsWorld("world");
 		debugger.debugFine("World %s is %s", config.getConfigValueAsString("world"), world);
 		ignoredRegions = config.getConfigValueAsList("free.ignore");
		limit = new Period(0, 0, 0, config.getConfigValueAsInt("old_after"), 0, 0, 0, 0).toStandardDuration();
 		autoApprove = config.getConfigValueAsInt("vote.approved");
 		voteRanks = config.getConfigValuesAsIntegerMap("vote.rank");
 	}
 
 	@Override
 	public void OnServerReady()
 	{
 		ScanTakenPlots();
 		ScanFreePlots();
 		CleanStaleData();
 	}
 
 	@Override
 	public HashMap<String, String> GetPlayerData(IPlayer player)
 	{
 		HashMap<String, String> data = new HashMap<String, String>();
 		data.put("runsafe.creative.blacklisted", blackList.isBlacklisted(player) ? "true" : "false");
 
 		List<String> plots = memberRepository.getPlots(player.getName(), true, false);
 		if (!plots.isEmpty())
 			data.put("runsafe.creative.owner", plots.toString());
 
 		plots = memberRepository.getPlots(player.getName(), false, true);
 		if (!plots.isEmpty())
 			data.put("runsafe.creative.member", plots.toString());
 
 		return data;
 	}
 
 	public void removeMember(IPlayer player)
 	{
 		for (String region : worldGuard.getRegionsInWorld(world))
 		{
 			Set<String> members = worldGuard.getMembers(world, region);
 			if (members != null && members.contains(player.getName().toLowerCase()))
 			{
 				debugger.debugFiner("Removing member %s from %s.", player.getPrettyName(), region);
 				worldGuard.removeMemberFromRegion(world, region, player);
 				memberRepository.removeMember(region, player.getName().toLowerCase());
 			}
 		}
 	}
 
 	public void memberRemoved(String plot, String name)
 	{
 		if (!voteBlacklist.containsKey(plot))
 			voteBlacklist.put(plot, new ArrayList<String>());
 		voteBlacklist.get(plot).add(name.toLowerCase());
 	}
 
 	private void CleanStaleData()
 	{
 		IWorld world = filter.getWorld();
 		if (world == null)
 			debugger.debugFine("No world defined!");
 		Map<String, Rectangle2D> regions = worldGuard.getRegionRectanglesInWorld(world);
 		if (regions == null)
 		{
 			debugger.debugFine("No regions in world!");
 			return;
 		}
 		Set<String> current = regions.keySet();
 
 		List<String> loggedPlots = plotLog.getPlots();
 		int deleted = 0;
 		for (String plot : loggedPlots)
 			if (!current.contains(plot))
 			{
 				plotLog.delete(plot);
 				deleted++;
 			}
 
 		List<String> taggedPlots = tagRepository.getTaggedPlots();
 		int cleared = 0;
 		for (String plot : taggedPlots)
 			if (!current.contains(plot))
 			{
 				tagRepository.setTags(plot, null);
 				cleared++;
 			}
 
 		int cleaned = memberRepository.cleanStaleData();
 
 		for (IPlayer player : blackList.getBlacklist())
 		{
 			removeMember(player);
 			cleaned++;
 		}
 
 		console.logInformation(
 			"Deleted &a%d&r plots, cleared tags from &a%d&r deleted plots and &a%d&r members.",
 			deleted, cleared, cleaned
 		);
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
 	private final IRegionControl worldGuard;
 	private final PlotEntranceRepository plotEntrance;
 	private final ApprovedPlotRepository plotApproval;
 	private final PlotVoteRepository voteRepository;
 	private final PlotTagRepository tagRepository;
 	private final PlotMemberRepository memberRepository;
 	private final PlotCalculator calculator;
 	private final PlotMemberBlacklistRepository blackList;
 	private final PlotList plotList;
 	private final IConsole console;
 	private final IDebug debugger;
 	private final IServer server;
 	private final PlotLogRepository plotLog;
 	private final Map<String, String> oldPlotPointers = new HashMap<String, String>();
 	private final Map<String, Map<String, String>> oldPlotList = new HashMap<String, Map<String, String>>();
 	private final HashMap<String, Duration> lastSeen = new HashMap<String, Duration>();
 	private final HashMap<Long, ArrayList<Long>> takenPlots = new HashMap<Long, ArrayList<Long>>();
 	private final ArrayList<ILocation> freePlots = new ArrayList<ILocation>();
 	private final Map<String, List<String>> voteBlacklist = new HashMap<String, List<String>>();
 	private IWorld world;
 	private List<String> ignoredRegions;
 	private Duration limit;
 	private int autoApprove;
 	private Map<String, Integer> voteRanks;
 	private final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("dd.MM.YYYY");
 }
