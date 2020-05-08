 package no.runsafe.ItemControl;
 
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.event.IConfigurationChanged;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.output.ChatColour;
 import no.runsafe.framework.output.ConsoleColors;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.RunsafeWorld;
 import no.runsafe.framework.server.block.RunsafeBlock;
 import no.runsafe.framework.server.block.RunsafeBlockState;
 import no.runsafe.framework.server.block.RunsafeCreatureSpawner;
 import no.runsafe.framework.server.entity.EntityType;
 import no.runsafe.framework.server.entity.RunsafeEntityType;
 import no.runsafe.framework.server.item.RunsafeItemStack;
 import no.runsafe.framework.server.player.RunsafePlayer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class Globals implements IConfigurationChanged
 {
 	public Globals(IOutput output)
 	{
 		console = output;
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration config)
 	{
 		this.disabledItems.clear();
 		this.worldBlockDrops.clear();
 		this.validSpawners.clear();
 		this.disabledItems.putAll(config.getConfigSectionsAsIntegerList("disabledItems"));
 		this.worldBlockDrops.putAll(config.getConfigSectionsAsIntegerList("blockDrops"));
 		this.validSpawners.addAll(config.getConfigValueAsList("spawner.allow"));
 		this.removeBlocked = config.getConfigValueAsBoolean("remove.disabledItems");
 	}
 
 	public Boolean itemIsDisabled(RunsafeWorld world, int itemID)
 	{
 		return (this.disabledItems.containsKey("*") && this.disabledItems.get("*").contains(itemID))
 			|| (this.disabledItems.containsKey(world.getName()) && this.disabledItems.get(world.getName()).contains(itemID));
 	}
 
 	public Boolean blockShouldDrop(RunsafeWorld world, Integer blockId)
 	{
 		return (this.worldBlockDrops.containsKey("*") && this.worldBlockDrops.get("*").contains(blockId))
 			|| (this.worldBlockDrops.containsKey(world.getName()) && this.worldBlockDrops.get(world.getName()).contains(blockId));
 	}
 
 	public boolean createSpawner(RunsafePlayer actor, RunsafeLocation location, RunsafeItemStack itemInHand)
 	{
 		RunsafeBlock target = location.getBlock();
		Item inHand = Item.Get(itemInHand);
 		RunsafeEntityType spawnerType = EntityType.Get(inHand);
 		if (target.isAir() && spawnerTypeValid(inHand.getData(), actor))
 		{
 			Item.Unavailable.MobSpawner.Place(location);
 			if (setSpawnerEntityID(target, spawnerType))
 				return true;
 			Item.Unavailable.Air.Place(location);
 		}
 		return false;
 	}
 
 	private boolean spawnerTypeValid(byte data, RunsafePlayer actor)
 	{
 		return spawnerTypeValid(EntityType.Get(data), actor);
 	}
 
 	public boolean spawnerTypeValid(RunsafeEntityType entityType, RunsafePlayer actor)
 	{
 		if (entityType == null && actor != null)
 		{
 			console.write(
 				ChatColour.ToConsole(
 					String.format(
 						"SPAWNER WARNING: %s tried to create/break a NULL spawner [%s,%d,%d,%d]!",
 						ConsoleColors.FromMinecraft(actor.getPrettyName()),
 						actor.getWorld().getName(),
 						actor.getLocation().getBlockX(),
 						actor.getLocation().getBlockY(),
 						actor.getLocation().getBlockZ()
 					)
 				)
 			);
 			return false;
 		}
 
 		if (entityType == null || !validSpawners.contains(entityType.getName().toLowerCase()))
 		{
 			if (actor != null)
 				console.write(
 					ChatColour.ToConsole(
 						String.format(
 							"SPAWNER WARNING: %s tried to create/break an invalid %s spawner [%s,%d,%d,%d]!",
 							ConsoleColors.FromMinecraft(actor.getPrettyName()),
 							entityType,
 							actor.getWorld().getName(),
 							actor.getLocation().getBlockX(),
 							actor.getLocation().getBlockY(),
 							actor.getLocation().getBlockZ()
 						)
 					)
 				);
 			return false;
 		}
 		return true;
 	}
 
 	public boolean blockedItemShouldBeRemoved()
 	{
 		return removeBlocked;
 	}
 
 	private boolean setSpawnerEntityID(RunsafeBlock block, RunsafeEntityType entityType)
 	{
 		if (block == null || block.isAir())
 			return false;
 
 		RunsafeBlockState state = block.getBlockState();
 		if (!(state instanceof RunsafeCreatureSpawner))
 			return false;
 
 		RunsafeCreatureSpawner spawner = (RunsafeCreatureSpawner) state;
 		spawner.SetCreature(entityType);
 		spawner.update(true);
 		return true;
 	}
 
 	private final HashMap<String, List<Integer>> worldBlockDrops = new HashMap<String, List<Integer>>();
 	private final HashMap<String, List<Integer>> disabledItems = new HashMap<String, List<Integer>>();
 	private final List<String> validSpawners = new ArrayList<String>();
 	private final IOutput console;
 	private boolean removeBlocked;
 }
