 package no.runsafe.ItemControl;
 
 import no.runsafe.framework.api.IConfiguration;
 import no.runsafe.framework.api.IWorld;
 import no.runsafe.framework.api.block.IBlock;
 import no.runsafe.framework.api.event.player.IPlayerDeathEvent;
 import no.runsafe.framework.api.event.player.IPlayerInteractEvent;
 import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.log.IDebug;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.minecraft.event.player.RunsafePlayerDeathEvent;
 import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEvent;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 import no.runsafe.worldguardbridge.WorldGuardInterface;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class PlayerListener implements IPlayerInteractEvent, IPlayerDeathEvent, IConfigurationChanged
 {
 	public PlayerListener(WorldGuardInterface worldGuardInterface, Globals globals, IDebug debugger)
 	{
 		this.worldGuardInterface = worldGuardInterface;
 		this.globals = globals;
 		this.debugger = debugger;
 	}
 
 	@Override
 	public void OnPlayerInteractEvent(RunsafePlayerInteractEvent event)
 	{
 		if (!event.isRightClick())
 			return;
 
 		IPlayer player = event.getPlayer();
 		IWorld world = player.getWorld();
 		RunsafeMeta usingItem = player.getItemInHand();
 
 		String playerName = player.getName();
 
 		if (globals.itemIsDisabled(world, usingItem.getItemId()))
 		{
 			this.debugger.debugFine("%s tried to use disabled item %s", playerName, usingItem.getItemId());
 			if (globals.blockedItemShouldBeRemoved())
 				player.removeItem(usingItem.getItemType());
 
 			event.cancel();
 		}
 
 		IBlock targetBlock = event.getBlock();
 		if (!player.canBuildNow() || targetBlock == null)
 			return;
 
 		if (usingItem.is(Item.Miscellaneous.MonsterEgg.Any)
 			&& this.globals.blockShouldDrop(world, Item.Unavailable.MobSpawner.getTypeID()))
 		{
 			this.debugger.debugFine("Monster Egg placement detected by " + playerName);
 
 			// If the block has an interface or is interact block, don't let them place a spawner
 			if (targetBlock.hasInterface() || targetBlock.isInteractBlock())
 				return;
 
 			if (this.globals.createSpawner(player, event.getTargetBlock(), usingItem))
 				player.removeItem(usingItem.getItemType(), 1);
 
 			event.cancel();
 		}
 	}
 
 	@Override
 	public void OnPlayerDeathEvent(RunsafePlayerDeathEvent event)
 	{
 		IPlayer player = event.getEntity();
 		String currentWorld = player.getWorldName();
 		boolean stopItems = false;
 
 		if (!this.noDeathItemsWorlds.contains(currentWorld))
 		{
 			List<String> regions = this.worldGuardInterface.getApplicableRegions(player);
 
 			if (regions != null && this.noDeathItemsRegions.containsKey(currentWorld))
 				for (String region : regions)
 					if (this.noDeathItemsRegions.get(currentWorld).contains(region))
 						stopItems = true;
 		}
 		else
 		{
 			stopItems = true;
 		}
 
 		if (stopItems)
 		{
 			event.setDrops(new ArrayList<RunsafeMeta>());
 			event.setNewExp(0);
 			event.setNewLevel(0);
 		}
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		this.noDeathItemsRegions.clear();
 		this.noDeathItemsWorlds.clear();
 
 		List<String> nodes = configuration.getConfigValueAsList("preventDeathItemDrops");
 
 		for (String node : nodes)
 		{
 			if (node.contains("."))
 			{
 				String[] parts = node.split("\\.");
 				if (!this.noDeathItemsRegions.containsKey(parts[0]))
 					this.noDeathItemsRegions.put(parts[0], new ArrayList<String>());
 
 				this.noDeathItemsRegions.get(parts[0]).add(parts[1]);
 			}
 			else
 			{
 				this.noDeathItemsWorlds.add(node);
 			}
 		}
 	}
 
 	private HashMap<String, List<String>> noDeathItemsRegions = new HashMap<String, List<String>>();
 	private List<String> noDeathItemsWorlds = new ArrayList<String>();
 	private WorldGuardInterface worldGuardInterface;
 
 	private final Globals globals;
 	private IDebug debugger;
 }
