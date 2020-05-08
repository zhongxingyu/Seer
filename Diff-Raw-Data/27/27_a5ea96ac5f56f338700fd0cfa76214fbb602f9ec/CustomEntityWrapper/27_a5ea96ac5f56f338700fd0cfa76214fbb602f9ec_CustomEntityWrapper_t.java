 package com.github.customentitylibrary.entities;
 
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.github.customentitylibrary.CustomEntitySpawnEvent;
 import com.github.customentitylibrary.utils.DefaultPathfinders;
 import com.github.customentitylibrary.utils.NMS;
 import net.minecraft.server.v1_7_R1.*;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
 import org.bukkit.craftbukkit.v1_7_R1.entity.CraftEntity;
 import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
 import org.bukkit.craftbukkit.v1_7_R1.util.UnsafeList;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 
 import com.github.customentitylibrary.CustomEntityLibrary;
 
 public class CustomEntityWrapper
 {
 	private static Map<EntityInsentient, CustomEntityWrapper> customEntities = new HashMap<EntityInsentient, CustomEntityWrapper>();
 	
 	private EntityInsentient entity;
 	private String name;
 	private double maxHealth;
 	private double health;
 	private EntityType type;
 	Map<String, Double> damagers = new LinkedHashMap<String, Double>();
 	List<PathfinderGoal> goalSelectors;
 	List<PathfinderGoal> targetSelectors;
 	
 	private boolean immune;
 	
 	public CustomEntityWrapper(final EntityInsentient entity, final World world, final double x, final double y, final double z, EntityType type)
 	{
 		//Set basic variables
 		this.entity = entity;
 		this.type = type;
 		entity.world = ((CraftWorld) world).getHandle();
 		this.name = type.getName();
 		immune = true;					//Entity is immune to damage until its position is normal again
 		entity.setPosition(x, y+5, z);	//Will be set back to normal at the end of the constructor. Offset like this to fix an invisible entity bug
 
 		//Set items
 		org.bukkit.inventory.ItemStack[] items = type.getItems();
 		if(items != null)
 		{
 			for(int i = 0; i <= 4 && i < items.length; i++)
 			{
 				if(items[i] != null)
 				{
 					ItemStack item = CraftItemStack.asNMSCopy(items[i]);
 					if(item != null)
 						entity.setEquipment(i, item);
 				}
 			}
 		}
 
 		setupPathfinders();
 
 		//Set health
 		health = type.getMaxHealth();
 		maxHealth = type.getMaxHealth();
 
 		if(type.getSpawnCommand().length() > 0)
 			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), type.getSpawnCommand());
 		
 		customEntities.put(entity, this);
 		//Reload visibility
 		Bukkit.getScheduler().scheduleSyncDelayedTask(CustomEntityLibrary.plugin, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				if(entity.getHealth() > 0)
 				{
 					entity.setPosition(x, y, z);
 					entity.setPosition(x, y, z);
 					entity.setPosition(x, y, z);
 					immune = false;
 					reloadPathfinders();
 
 					//The entity may have picked a target immediately after spawning. In case this target was picked using code that was later removed, remove the target
 					entity.setGoalTarget(null);
 				}
 			}
 		},1L);
 	}
 	
     public Player getAssistAttacker()
     {
     	String p = null;
     	String p2 = null;
 		double damage = 0;
     	for(Entry<String, Double> e: damagers.entrySet())
     	{
     		if(e.getValue() > damage)
     		{
     			p2 = p;
     			p = e.getKey();
     			damage = e.getValue();
     		}
     	}
     	if(p2 == null)
     		return null;
     	return Bukkit.getPlayer(p2);
     }
     
 	public Player getBestAttacker()
     {
     	String p = null;
     	double damage = 0;
     	for(Entry<String, Double> e: damagers.entrySet())
     	{
     		if(e.getValue() > damage)
     		{
     			p = e.getKey();
     			damage = e.getValue();
     		}
     	}
     	if(p == null)
     		return null;
     	return Bukkit.getPlayer(p);
     }
 	
 	public EntityInsentient getEntity()
 	{
 		return entity;
 	}
 	
 	public double getHealth()
 	{
 		return health;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 	
 	public EntityType getType()
 	{
 		return type;
 	}
 	
 	public void restoreHealth()
 	{
 		health = maxHealth;
 	}
 	
 	public void setHealth(double health)
 	{
 		this.health = health;
 	}
 	
 	public void setMaxHealth(double health)
 	{
 		maxHealth = health;
 	}
 	
 	public double getMaxHealth()
 	{
 		return maxHealth;
 	}
 
 	public List<PathfinderGoal> getGoalSelectors()
 	{
 		return goalSelectors;
 	}
 
 	public List<PathfinderGoal> getTargetSelectors()
 	{
 		return targetSelectors;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public void reloadPathfinders()
 	{
 		//Get the goal/target selectors, and reset them
 		PathfinderGoalSelector goalSelector;
 		PathfinderGoalSelector targetSelector;
 		try
 		{
 			Field goalSelectorField = EntityInsentient.class.getDeclaredField("goalSelector");
 			Field targetSelectorField = EntityInsentient.class.getDeclaredField("targetSelector");
 
 			goalSelectorField.setAccessible(true);
 			targetSelectorField.setAccessible(true);
 			
 			goalSelector = (PathfinderGoalSelector) goalSelectorField.get(entity);
 			targetSelector = (PathfinderGoalSelector) targetSelectorField.get(entity);
 			
 			//Enable PathfinderGoalSelector's "a" field to be editable
 			Field gsa = PathfinderGoalSelector.class.getDeclaredField(NMS.PATHFINDER_LIST);
 			gsa.setAccessible(true);
 
 			//Now take the instances goals/targets and set them as new lists so they can be rewritten
 			gsa.set(goalSelector, new UnsafeList());
 			gsa.set(targetSelector, new UnsafeList());
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 			return;
 		}
 
 		for(int i = 0; i < goalSelectors.size(); i++)
 		{
 			goalSelector.a(i, goalSelectors.get(i));
 		}
 		for(int i = 0; i < targetSelectors.size(); i++)
 		{
 			targetSelector.a(i, targetSelectors.get(i));
 		}
 	}
 
 	private void setupPathfinders()
 	{
 		if(type.getGoalSelectors(entity) == null)
 		{
 			goalSelectors = DefaultPathfinders.getGoalSelectors(entity, type);
 		} else
 		{
 			goalSelectors = type.getGoalSelectors(entity);
 		}
 		if(type.getTargetSelectors(entity) == null)
 		{
 			targetSelectors = DefaultPathfinders.getTargetSelectors(entity, type);
 		} else
 		{
 			targetSelectors = type.getTargetSelectors(entity);
 		}
 	}
 
 	public void setImmune(boolean immune)
 	{
 		this.immune = immune;
 	}
 
 	public boolean isImmune()
 	{
 		return immune;
 	}
 	
 	public void addAttack(Player p, double damage)
     {
     	double damagex = 0;
     	if(damagers.get(p.getName()) != null)
     		damagex = damagers.get(p.getName());
     	damagers.put(p.getName(), damage + damagex);
     }
 	
 	/**
 	 * Allows for a simpler way of checking if an Entity is an instance of a CustomEntityWrapper
 	 * @param entity the entity being checked
 	 * @return whether or not an Entity is an instanceof a CustomEntityWrapper
 	 */
 	public static boolean instanceOf(Entity entity)
 	{
 		return customEntities.containsKey(((CraftEntity) entity).getHandle());
 	}
 	
 	public static Map<EntityInsentient, CustomEntityWrapper> getCustomEntities()
 	{
 		return customEntities;
 	}
 	
 	/**
 	 * Allows for a simpler way of converting an Entity to a CustomEntity
 	 * @param entity being converted to a CustomEntityWrapper
 	 * @return a CustomEntityWrapper instance of the entity, or null if none exists
 	 */
 	public static CustomEntityWrapper getCustomEntity(Entity entity)
 	{
 		if(customEntities.containsKey(((CraftEntity) entity).getHandle()))
 			return customEntities.get(((CraftEntity) entity).getHandle());
 		return null;
 	}
 	
 	public static CustomEntityWrapper spawnCustomEntity(EntityInsentient entity, World world, double x, double y, double z, EntityType type)
 	{
 		entity.getBukkitEntity().getLocation().getChunk().load();
 		//Event cancelled
 		if(!((CraftWorld) world).getHandle().addEntity(entity, SpawnReason.CUSTOM))
 			return null;
 		CustomEntityWrapper customEnt = new CustomEntityWrapper(entity, world, x, y, z, type);
 		CustomEntitySpawnEvent event = new CustomEntitySpawnEvent(customEnt, new Location(world, x, y, z));
 		Bukkit.getPluginManager().callEvent(event);
 		if(event.isCancelled())
 		{
 			customEnt.getEntity().setHealth(0);
 			customEntities.remove(entity);
 			return null;
 		}
 		return customEnt;
 	}
 	
 	public static CustomEntityWrapper spawnCustomEntity(EntityInsentient entity, Location location, EntityType type)
 	{
 		return spawnCustomEntity(entity, location.getWorld(), location.getX(), location.getY(), location.getZ(), type);
 	}
 
 	public static CustomEntityWrapper spawnCustomEntity(EntityType type, World world, double x, double y, double z)
 	{
 		org.bukkit.entity.EntityType bukkitType = org.bukkit.entity.EntityType.valueOf(type.getPreferredType().toUpperCase().replaceAll(" ", "_"));
 		net.minecraft.server.v1_7_R1.World nmsWorld = ((CraftWorld)world).getHandle();
 		EntityInsentient entity;
 		switch(bukkitType)
 		{
 			case BAT:
 				entity = new EntityBat(nmsWorld);
 				break;
 			case CREEPER:
 				entity = new EntityCreeper(nmsWorld);
 				break;
 			case ENDERMAN:
 				entity = new EntityEnderman(nmsWorld);
 				break;
 			case GHAST:
 				entity = new EntityGhast(nmsWorld);
 				break;
 			case GIANT:
 				entity = new CustomGiant(nmsWorld);
 				break;
 			case IRON_GOLEM:
 				entity = new EntityIronGolem(nmsWorld);
 				break;
 			case MAGMA_CUBE:
 				entity = new EntityMagmaCube(nmsWorld);
 				break;
 			case OCELOT:
 				entity = new EntityOcelot(nmsWorld);
 				break;
 			case PIG_ZOMBIE:
				entity = new CustomPigZombie(nmsWorld, true);
 				break;
 			case SKELETON:
 				entity = new EntitySkeleton(nmsWorld);
 				break;
 			case SLIME:
 				entity = new EntitySlime(nmsWorld);
 				break;
 			case SNOWMAN:
 				entity = new EntitySnowman(nmsWorld);
 				break;
 			case SPIDER:
 				entity = new EntitySpider(nmsWorld);
 				break;
 			case SQUID:
 				entity = new EntitySquid(nmsWorld);
 				break;
 			case VILLAGER:
 				entity = new EntityVillager(nmsWorld);
 				break;
 			case WITCH:
 				entity = new EntityWitch(nmsWorld);
 				break;
 			case WITHER:
 				entity = new EntityWither(nmsWorld);
 				break;
 			case WOLF:
 				entity = new EntityWolf(nmsWorld);
 				break;
 			case ZOMBIE:
 				entity = new EntityZombie(nmsWorld);
 				break;
 			default:
 				// :(
 				entity = null;
 		}
 		return spawnCustomEntity(entity, world, x, y, z, type);
 	}
 
 	public static CustomEntityWrapper spawnCustomEntity(EntityType type, Location location)
 	{
 		return spawnCustomEntity(type, location.getWorld(), location.getX(), location.getY(), location.getZ());
 	}
 }
