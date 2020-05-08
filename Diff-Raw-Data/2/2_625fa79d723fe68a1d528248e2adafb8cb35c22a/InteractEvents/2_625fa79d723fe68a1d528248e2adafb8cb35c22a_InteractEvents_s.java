 package no.runsafe.creativetoolbox.event;
 
 import no.runsafe.creativetoolbox.PlotFilter;
 import no.runsafe.creativetoolbox.PlotManager;
 import no.runsafe.creativetoolbox.database.PlotLogRepository;
 import no.runsafe.creativetoolbox.database.PlotTagRepository;
 import no.runsafe.framework.api.IConfiguration;
 import no.runsafe.framework.api.event.IAsyncEvent;
 import no.runsafe.framework.api.event.player.IPlayerInteractEntityEvent;
 import no.runsafe.framework.api.event.player.IPlayerRightClickBlock;
 import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
 import no.runsafe.framework.minecraft.RunsafeLocation;
 import no.runsafe.framework.minecraft.RunsafeServer;
 import no.runsafe.framework.minecraft.block.RunsafeBlock;
 import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEntityEvent;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 import no.runsafe.worldguardbridge.WorldGuardInterface;
 import org.bukkit.craftbukkit.libs.joptsimple.internal.Strings;
 
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class InteractEvents implements IPlayerRightClickBlock, IPlayerInteractEntityEvent, IConfigurationChanged, IAsyncEvent
 {
 	public InteractEvents(
 		PlotFilter plotFilter,
 		WorldGuardInterface worldGuard,
 		PlotManager manager,
 		PlotTagRepository tagRepository, PlotLogRepository logRepository)
 	{
 		this.worldGuardInterface = worldGuard;
 		this.plotFilter = plotFilter;
 		this.manager = manager;
 		this.tagRepository = tagRepository;
 		this.logRepository = logRepository;
 	}
 
 	@Override
 	public boolean OnPlayerRightClick(RunsafePlayer player, RunsafeMeta itemInHand, RunsafeBlock block)
 	{
 		if (extensions.containsKey(player.getName()))
 		{
 			String target = extensions.get(player.getName());
 			extensions.remove(player.getName());
 			manager.extendPlot(player, target, block.getLocation());
 			return false;
 		}
 
 		if (itemInHand != null && itemInHand.getItemId() == listItem)
 		{
 			this.listPlotsByLocation(block.getLocation(), player);
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public void OnPlayerInteractEntityEvent(RunsafePlayerInteractEntityEvent event)
 	{
 		if (event.getRightClicked() instanceof RunsafePlayer && event.getPlayer().hasPermission("runsafe.creative.list"))
 		{
 			if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getItemId() == listItem)
 			{
 				this.listPlotsByPlayer((RunsafePlayer) event.getRightClicked(), event.getPlayer());
				event.setCancelled(true);
 			}
 		}
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		listItem = configuration.getConfigValueAsInt("list_item");
 	}
 
 	public void startPlotExtension(RunsafePlayer player, String plot)
 	{
 		extensions.put(player.getName(), plot);
 	}
 
 	private void listPlotsByPlayer(RunsafePlayer checkPlayer, RunsafePlayer triggerPlayer)
 	{
 		if (!this.worldGuardInterface.serverHasWorldGuard())
 		{
 			triggerPlayer.sendMessage("Error: No WorldGuard installed.");
 			return;
 		}
 
 		List<String> regions = plotFilter.apply(worldGuardInterface.getOwnedRegions(checkPlayer, checkPlayer.getWorld()));
 
 		if (!regions.isEmpty())
 			triggerPlayer.sendColouredMessage(Strings.join(
 				manager.tag(triggerPlayer, regions),
 				"\n"
 			));
 		else
 			triggerPlayer.sendColouredMessage("%s does not own any plots.", checkPlayer.getPrettyName());
 	}
 
 	private void listPlotsByLocation(RunsafeLocation location, RunsafePlayer player)
 	{
 		if (!this.worldGuardInterface.serverHasWorldGuard())
 		{
 			player.sendMessage("Error: No WorldGuard installed.");
 			return;
 		}
 
 		List<String> regions = plotFilter.apply(worldGuardInterface.getRegionsAtLocation(location));
 
 		if (regions != null && !regions.isEmpty())
 			for (String regionName : regions)
 			{
 				player.sendColouredMessage("&6Plot: &l%s", manager.tag(player, regionName));
 				listClaimInfo(player, regionName);
 				listTags(player, regionName);
 				listPlotMembers(player, regionName);
 			}
 		else
 			player.sendMessage("No plots found at this location.");
 	}
 
 	private void listClaimInfo(RunsafePlayer player, String regionName)
 	{
 		if (player.hasPermission("runsafe.creative.claim.log"))
 		{
 			String claim = logRepository.getClaim(regionName);
 			if (claim == null)
 				return;
 
 			player.sendColouredMessage("&bClaimed: %s", claim);
 		}
 	}
 
 	private void listTags(RunsafePlayer player, String regionName)
 	{
 		if (player.hasPermission("runsafe.creative.tag.read"))
 		{
 			List<String> tags = tagRepository.getTags(regionName);
 			if (!tags.isEmpty())
 				player.sendColouredMessage("&7Tags: &o%s&r", Strings.join(tags, " "));
 		}
 	}
 
 	private void listPlotMembers(RunsafePlayer player, String regionName)
 	{
 		Set<String> owners = worldGuardInterface.getOwners(manager.getWorld(), regionName);
 		for (String owner : owners)
 			listPlotMember(player, "&2Owner&r", owner, true);
 
 		Set<String> members = worldGuardInterface.getMembers(manager.getWorld(), regionName);
 		for (String member : members)
 			listPlotMember(player, "&3Member&r", member, false);
 	}
 
 	private void listPlotMember(RunsafePlayer player, String label, String member, boolean showSeen)
 	{
 		RunsafePlayer plotMember = RunsafeServer.Instance.getPlayer(member);
 		if (plotMember != null)
 		{
 			player.sendColouredMessage("   %s: %s", label, plotMember.getPrettyName());
 
 			if (showSeen && player.hasPermission("runsafe.creative.list.seen"))
 			{
 				String seen = plotMember.getLastSeen(player);
 				player.sendColouredMessage("     %s&r", (seen == null ? "Player never seen" : seen));
 			}
 		}
 	}
 
 	private final WorldGuardInterface worldGuardInterface;
 	private int listItem;
 	private final PlotManager manager;
 	private final PlotFilter plotFilter;
 	private final PlotTagRepository tagRepository;
 	private final PlotLogRepository logRepository;
 	private final ConcurrentHashMap<String, String> extensions = new ConcurrentHashMap<String, String>();
 }
