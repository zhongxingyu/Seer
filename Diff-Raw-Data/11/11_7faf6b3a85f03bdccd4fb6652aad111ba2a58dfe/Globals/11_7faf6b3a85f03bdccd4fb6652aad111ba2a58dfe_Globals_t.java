 package no.runsafe.ItemControl;
 
 import no.runsafe.framework.api.IConfiguration;
 import no.runsafe.framework.api.ILocation;
 import no.runsafe.framework.api.IWorld;
 import no.runsafe.framework.api.block.IBlock;
 import no.runsafe.framework.api.block.IBlockState;
 import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.log.IConsole;
 import no.runsafe.framework.api.minecraft.RunsafeEntityType;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.minecraft.block.RunsafeCreatureSpawner;
 import no.runsafe.framework.minecraft.entity.EntityType;
 import no.runsafe.framework.minecraft.item.RunsafeItemStack;
 import no.runsafe.framework.text.ConsoleColour;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class Globals implements IConfigurationChanged
 {
 	public Globals(IConsole output)
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
 
 	public Boolean itemIsDisabled(IWorld world, int itemID)
 	{
 		return (this.disabledItems.containsKey("*") && this.disabledItems.get("*").contains(itemID))
 			|| (this.disabledItems.containsKey(world.getName()) && this.disabledItems.get(world.getName()).contains(itemID));
 	}
 
 	public Boolean blockShouldDrop(IWorld world, Integer blockId)
 	{
 		return (this.worldBlockDrops.containsKey("*") && this.worldBlockDrops.get("*").contains(blockId))
 			|| (this.worldBlockDrops.containsKey(world.getName()) && this.worldBlockDrops.get(world.getName()).contains(blockId));
 	}
 
 	public boolean createSpawner(IPlayer actor, ILocation location, RunsafeItemStack itemInHand)
 	{
 		IBlock target = location.getBlock();
 		Item inHand = itemInHand.getItemType();
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
 
 	private boolean spawnerTypeValid(byte data, IPlayer actor)
 	{
 		return spawnerTypeValid(EntityType.Get(data), actor);
 	}
 
 	public boolean spawnerTypeValid(RunsafeEntityType entityType, IPlayer actor)
 	{
 		if (entityType == null && actor != null)
 		{
 			console.logInformation(
 				"SPAWNER WARNING: %s tried to create/break a NULL spawner [%s,%d,%d,%d]!",
 				ConsoleColour.FromMinecraft(actor.getPrettyName()),
 				actor.getWorld().getName(),
 				actor.getLocation().getBlockX(),
 				actor.getLocation().getBlockY(),
 				actor.getLocation().getBlockZ()
 			);
 			return false;
 		}
 
 		if (entityType == null || !validSpawners.contains(entityType.getName().toLowerCase()))
 		{
 			if (actor != null)
 				console.logInformation(
 					"SPAWNER WARNING: %s tried to create/break an invalid %s spawner [%s,%d,%d,%d]!",
 					ConsoleColour.FromMinecraft(actor.getPrettyName()),
 					entityType,
 					actor.getWorld().getName(),
 					actor.getLocation().getBlockX(),
 					actor.getLocation().getBlockY(),
 					actor.getLocation().getBlockZ()
 				);
 			return false;
 		}
 		return true;
 	}
 
 	public boolean blockedItemShouldBeRemoved()
 	{
 		return removeBlocked;
 	}
 
 	private boolean setSpawnerEntityID(IBlock block, RunsafeEntityType entityType)
 	{
 		if (block == null || block.isAir())
 			return false;
 
 		IBlockState state = block.getBlockState();
 		if (!(state instanceof RunsafeCreatureSpawner))
 			return false;
 
 		RunsafeCreatureSpawner spawner = (RunsafeCreatureSpawner) state;
 		spawner.setCreature(entityType);
 		spawner.update(true);
 		return true;
 	}
 
 	private final HashMap<String, List<Integer>> worldBlockDrops = new HashMap<String, List<Integer>>();
 	private final HashMap<String, List<Integer>> disabledItems = new HashMap<String, List<Integer>>();
 	private final List<String> validSpawners = new ArrayList<String>();
 	private final IConsole console;
 	private boolean removeBlocked;
 }
