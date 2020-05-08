 package com.github.customentitylibrary.entities;
 
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 
 import com.github.customentitylibrary.CustomEntitySpawnEvent;
 import com.github.customentitylibrary.utils.DefaultPathfinders;
 import com.github.customentitylibrary.utils.NMS;
 import net.minecraft.server.v1_5_R3.EntityLiving;
 import net.minecraft.server.v1_5_R3.ItemStack;
 import net.minecraft.server.v1_5_R3.PathfinderGoal;
 import net.minecraft.server.v1_5_R3.PathfinderGoalSelector;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.craftbukkit.v1_5_R3.CraftWorld;
 import org.bukkit.craftbukkit.v1_5_R3.entity.CraftEntity;
 import org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack;
 import org.bukkit.craftbukkit.v1_5_R3.util.UnsafeList;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 
 import com.github.customentitylibrary.CustomEntityLibrary;
 
 public class CustomEntityWrapper
 {
 	private static Map<EntityLiving, CustomEntityWrapper> customEntities = new HashMap<EntityLiving, CustomEntityWrapper>();
 	
 	private EntityLiving entity;
 	private String name;
 	private int maxHealth;
 	private int health;
 	private EntityType type;
 	Map<String, Integer> damagers = new LinkedHashMap<String, Integer>();
 	
 	public boolean immune;
 	
 	public CustomEntityWrapper(final EntityLiving entity, World world, final double x, final double y, final double z, EntityType type)
 	{
 		//Set basic variables
 		this.entity = entity;
 		this.type = type;
 		entity.world = ((CraftWorld) world).getHandle();
 		this.name = type.toString();
 		immune = true;					//Entity is immune to damage until its position is normal again
 		entity.setPosition(x, y-5, z);	//Will be set back to normal at the end of the constructor. Offset like this to fix an invisible entity bug
 
 		//Set pathfinders
 		setupPathfinders();
 
 		//Set items
 		org.bukkit.inventory.ItemStack[] items = type.getItems();
 		if(items != null)
 		{
 			for(int i = 0; i <= 4; i++)
 			{
 				if(items[i] != null)
 				{
 					ItemStack item = CraftItemStack.asNMSCopy(items[i]);
 					if(item != null)
 						entity.setEquipment(i, item);
 				}
 			}
 		}
 
 		//Set health
 		health = type.getMaxHealth();
 		
 		//And then speed
 		setSpeed(type.getSpeed());
 		
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
 					immune = false;
 				}
 			}
 		},1L);
 	}
 	
     public Player getAssistAttacker()
     {
     	String p = null;
     	String p2 = null;
     	int damage = 0;
     	for(Entry<String, Integer> e: damagers.entrySet())
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
     	int damage = 0;
     	for(Entry<String, Integer> e: damagers.entrySet())
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
 	
 	public EntityLiving getEntity()
 	{
 		return entity;
 	}
 	
 	public int getHealth()
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
 	
 	public void setHealth(int health)
 	{
 		this.health = health;
 	}
 	
 	public void setMaxHealth(int health)
 	{
 		maxHealth = health;
 	}
 	
 	public int getMaxHealth()
 	{
 		return maxHealth;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	private void setupPathfinders()
 	{
 		//Get the goal/target selectors, and reset them
 		PathfinderGoalSelector goalSelector;
 		PathfinderGoalSelector targetSelector;
 		try
 		{
 			Field goalSelectorField = EntityLiving.class.getDeclaredField("goalSelector");
 			Field targetSelectorField = EntityLiving.class.getDeclaredField("targetSelector");
 
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
 
 		//Take the reset goal/target selectors and readd new pathfinders from the given entity type
 		if(type.getGoalSelectors(entity, type) == null)
 		{
 			for(Entry<Integer, PathfinderGoal> e : DefaultPathfinders.getGoalSelectors(entity, type).entrySet())
 			{
 				targetSelector.a(e.getKey(), e.getValue());
 			}
 		} else
 		{
 			for(Entry<Integer, PathfinderGoal> e : type.getGoalSelectors(entity, type).entrySet())
 			{
 				goalSelector.a(e.getKey(), e.getValue());
 			}
 		}
 		if(type.getTargetSelectors(entity, type) == null)
 		{
 			for(Entry<Integer, PathfinderGoal> e : DefaultPathfinders.getTargetSelectors(entity, type).entrySet())
 			{
 				targetSelector.a(e.getKey(), e.getValue());
 			}
 		} else
 		{
 			for(Entry<Integer, PathfinderGoal> e : type.getTargetSelectors(entity, type).entrySet())
 			{
 				targetSelector.a(e.getKey(), e.getValue());
 			}
 		}
 	}
 	
 	private void setSpeed(float speed)
 	{
 		Field f;
 		try
 		{
 			f = EntityLiving.class.getDeclaredField(NMS.SPEED);
 
 			f.setAccessible(true);
 			f.setFloat(entity, speed);
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public void addAttack(Player p, int damage)
     {
     	int damagex = 0;
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
 		if(customEntities.containsKey(((CraftEntity) entity).getHandle()))
 			return true;
 		return false;
 	}
 	
 	public static Map<EntityLiving, CustomEntityWrapper> getCustomEntities()
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
 	
 	public static CustomEntityWrapper spawnCustomEntity(EntityLiving entity, World world, double x, double y, double z, EntityType type)
 	{
		((CraftWorld) world).getHandle().addEntity(entity, SpawnReason.CUSTOM);
 		CustomEntityWrapper customEnt = new CustomEntityWrapper(entity, world, x, y, z, type);
 		CustomEntitySpawnEvent event = new CustomEntitySpawnEvent(customEnt, new Location(world, x, y, z));
 		Bukkit.getPluginManager().callEvent(event);
 		if(event.isCancelled())
 		{
 			customEnt.getEntity().setHealth(0);
 			return null;
 		}
 		return customEnt;
 	}
 	
 	public static CustomEntityWrapper spawnCustomEntity(EntityLiving entity, Location location, EntityType type)
 	{
 		return spawnCustomEntity(entity, location.getWorld(), location.getX(), location.getY(), location.getZ(), type);
 	}
 }
