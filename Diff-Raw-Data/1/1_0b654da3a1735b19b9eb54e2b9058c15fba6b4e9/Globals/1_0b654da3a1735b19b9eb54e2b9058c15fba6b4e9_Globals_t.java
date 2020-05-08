 package no.runsafe.ItemControl;
 
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.event.IConfigurationChanged;
 import no.runsafe.framework.output.ChatColour;
 import no.runsafe.framework.output.ConsoleColors;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.RunsafeWorld;
 import no.runsafe.framework.server.item.RunsafeItemStack;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.CreatureSpawner;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.EntityType;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
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
 		this.disabledItems.putAll(this.loadConfigurationIdList(config, "disabledItems"));
 		this.worldBlockDrops.putAll(this.loadConfigurationIdList(config, "blockDrops"));
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
 
 	private boolean setSpawnerEntityID(Block block, EntityType entityType)
 	{
 		if (block == null || block.isEmpty())
 			return false;
 
 		BlockState state = block.getState();
 		if (!(state instanceof CreatureSpawner))
 			return false;
 
 		CreatureSpawner spawner = (CreatureSpawner) state;
 		spawner.setSpawnedType(entityType);
 		spawner.update(true);
 		return true;
 	}
 
 	public boolean createSpawner(RunsafePlayer actor, RunsafeWorld world, RunsafeLocation location, RunsafeItemStack itemInHand)
 	{
 		Block target = world.getRaw().getChunkAt(location.getRaw()).getBlock(
 			location.getBlockX(),
 			location.getBlockY(),
 			location.getBlockZ()
 		);
 		EntityType spawnerType = EntityType.fromId(itemInHand.getRaw().getData().getData());
 		if (target.isEmpty() && spawnerTypeValid(spawnerType, actor))
 		{
 			target.setType(Material.MOB_SPAWNER);
 			if (setSpawnerEntityID(target, spawnerType))
 				return true;
 			target.setType(Material.AIR);
 		}
 		return false;
 	}
 
 	public boolean spawnerTypeValid(EntityType entityType, RunsafePlayer actor)
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
 		switch (entityType)
 		{
 			case DROPPED_ITEM:
 			case EXPERIENCE_ORB:
 			case PAINTING:
 			case ARROW:
 			case SNOWBALL:
 			case SMALL_FIREBALL:
 			case ENDER_PEARL:
 			case ENDER_SIGNAL:
 			case THROWN_EXP_BOTTLE:
 			case PRIMED_TNT:
 			case FALLING_BLOCK:
 			case MINECART:
 			case BOAT:
 			case GIANT:
 			case SLIME:
 			case GHAST:
 			case PIG_ZOMBIE:
 			case ENDERMAN:
 			case MAGMA_CUBE:
 			case ENDER_DRAGON:
 			case PIG:
 			case SHEEP:
 			case COW:
 			case CHICKEN:
 			case SQUID:
 			case WOLF:
 			case MUSHROOM_COW:
 			case SNOWMAN:
 			case OCELOT:
 			case IRON_GOLEM:
 			case VILLAGER:
 			case ENDER_CRYSTAL:
 			case SPLASH_POTION:
 			case EGG:
 			case FISHING_HOOK:
 			case LIGHTNING:
 			case WEATHER:
 			case PLAYER:
 			case COMPLEX_PART:
 			case UNKNOWN:
 			case ITEM_FRAME:
 			case WITHER_SKULL:
 			case WITHER:
 			case BAT:
 			case WITCH:
 			case FIREBALL:
 			case FIREWORK:
 			default:
 				if (actor != null)
 					console.write(
 						ChatColour.ToConsole(
 							String.format(
 								"SPAWNER WARNING: %s tried to create/break an invalid %s spawner [%s,%d,%d,%d]!",
 								ConsoleColors.FromMinecraft(actor.getPrettyName()),
 								entityType.name(),
 								actor.getWorld().getName(),
 								actor.getLocation().getBlockX(),
 								actor.getLocation().getBlockY(),
 								actor.getLocation().getBlockZ()
 							)
 						)
 					);
 				return false;
 
 			case SKELETON:
 			case SPIDER:
 			case ZOMBIE:
 			case SILVERFISH:
 			case CAVE_SPIDER:
 			case BLAZE:
			case CREEPER:
 				return true;
 		}
 	}
 
 	private HashMap<String, List<Integer>> loadConfigurationIdList(IConfiguration config, String configurationValue)
 	{
 		HashMap<String, List<Integer>> returnMap = new HashMap<String, List<Integer>>();
 		ConfigurationSection disabledItems = config.getSection(configurationValue);
 
 		if (disabledItems == null)
 			return null;
 
 		Set<String> keys = disabledItems.getKeys(true);
 		if (keys == null)
 			return null;
 
 		for (String key : keys)
 		{
 			if (!returnMap.containsKey(key))
 				returnMap.put(key, disabledItems.getIntegerList(key));
 		}
 
 		return returnMap;
 	}
 
 	private final HashMap<String, List<Integer>> worldBlockDrops = new HashMap<String, List<Integer>>();
 	private final HashMap<String, List<Integer>> disabledItems = new HashMap<String, List<Integer>>();
 	private final IOutput console;
 }
