 package com.github.phoenix9876.MobHostility;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.World;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class MobHostility extends JavaPlugin
 {
 	private boolean ValidConfig;
 	private String ConfigErrorMessage = "";
 	
     @Override
     public void onEnable()
     {
     	this.ValidConfig = false;
     	if (!(new File(this.getDataFolder()+"/config.yml").exists()))
     	{
         	this.saveDefaultConfig();
     	}
     	CheckConfig();
     	
     	if(this.ValidConfig)
     	{
 	    	new EventListener(this);
 	    	
 	    	getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 	    		public void run() {
 	    			CheckOnlinePlayers();
 	    		}
	    	}, 60L, getConfig().getLong("time"));	
     	}
     	else
     	{
     		getLogger().severe(this.ConfigErrorMessage);
     	}
     }
  
     @Override
     public void onDisable()
     {
     	getServer().getScheduler().cancelTasks(this);
     }
     
     public void CheckConfig()
     {
     	List<String> ValidWorldTypes = new ArrayList<String>();
     	List<String> ValidWorldNames = new ArrayList<String>();
     	
     	ValidWorldTypes.add("overworld");
     	ValidWorldTypes.add("nether");
     	ValidWorldTypes.add("end");
     	ValidWorldTypes.add("all");
     	ValidWorldTypes.add("none");
     	
     	for(World w : getServer().getWorlds())
     	{
     		ValidWorldNames.add(w.getName());
     	}
     	
     	if((getConfig().contains("worldtypes") && ValidWorldTypes.containsAll(getConfig().getStringList("worldtypes"))) || 
     			(getConfig().contains("worldnames") && ValidWorldNames.containsAll(getConfig().getStringList("worldnames"))))
     	{
     		this.ValidConfig = true;
     	}
     	else
     	{
     		this.ValidConfig = false;
     		this.ConfigErrorMessage = "Invalid world type or name! Disabling MobHostility...";
     	}
     	
     	if(getConfig().isSet("time") && getConfig().getLong("time") >= 1L)
     	{
     		this.ValidConfig = true;
     	}
     	else
     	{
     		this.ValidConfig = false;
     		this.ConfigErrorMessage = "MobHostility schedule time is not set or invalid! Set to 1 second or greater. Disabling MobHostility...";
     	}
     	
     	if(getConfig().isSet("hostileradius") && getConfig().getDouble("hostileradius") > 0)
     	{
     		this.ValidConfig = true;
     	}
     	else
     	{
     		this.ValidConfig = false;
     		this.ConfigErrorMessage = "MobHostility hostile radius is less than 0! Disabling MobHostility...";
     	}
     	
     	Set<String> MobNames = getConfig().getConfigurationSection("mobitems").getKeys(false);
     	for(String MobName : MobNames)
     	{
     		if(!(MobName.equals("zombie")) && !(MobName.equals("skeleton")) && !(MobName.equals("spider")) && !(MobName.equals("cave_spider"))
     				&& !(MobName.equals("creeper"))	&& !(MobName.equals("slime")) && !(MobName.equals("silverfish")) && !(MobName.equals("enderman"))
     				&& !(MobName.equals("ender_dragon")) && !(MobName.equals("pig_zombie")) && !(MobName.equals("blaze")) && !(MobName.equals("ghast"))
     				&& !(MobName.equals("magma_cube")) && !(MobName.equals("wolf")) && !(MobName.equals("giant")) && !(MobName.equals("all")))
     		{
         		this.ValidConfig = false;
     			this.ConfigErrorMessage = "An invalid mob name was specified. Check your MobHostility config file! Disabling MobHostility...";
     		}
     	}
     }
     
     public void CheckOnlinePlayers()
     {
     	List<World> Worlds = getServer().getWorlds();
 		for(World w : Worlds)
 		{
 			if((getConfig().getStringList("worldtypes").contains(w.getEnvironment().toString().toLowerCase()) || getConfig().getStringList("worldnames").contains(w.getName())
 					|| getConfig().getStringList("worldtypes").contains("all")) && !(getConfig().getStringList("worldtypes").contains("none")))
 			{
 				for(Player p : w.getPlayers())
 				{
 					List<Entity> NearbyMobs = p.getNearbyEntities(getConfig().getDouble("hostileradius"), getConfig().getDouble("hostileradius"), getConfig().getDouble("hostileradius"));
 					for(Entity e : NearbyMobs)
 					{
 						if(e instanceof Creature)
 						{
 							Creature c = (Creature) e;
 							if(getConfig().isSet("mobitems.all"))
 							{
 								if(checkPlayerInventoryForForbiddenItemsByEntityType(p,"all","hostile"))
 								{
 									setTargetAndAnger(c,p);
 								}
 							}
 							if(getConfig().getConfigurationSection("mobitems").getKeys(false).contains(e.getType().toString().toLowerCase()))
 							{
 								if(checkPlayerInventoryForForbiddenItemsByEntityType(p,e.getType().toString().toLowerCase(),"hostile"))
 								{
 									setTargetAndAnger(c,p);
 								}
 							}	
 						}
 					}
 				}
 			}
 		}
     }
     
     public boolean checkPlayerInventoryForForbiddenItemsByEntityType(Player p, String entitytype, String ControlType)
     {
 		boolean ReturnValue = false;
 		if(p.getInventory() != null)
 		{
 			if(p.getInventory().getHelmet() != null && getConfig().getIntegerList("mobitems."+entitytype+"."+ControlType+".helmets") != null)
 			{
 				if((getConfig().getIntegerList("mobitems."+entitytype+"."+ControlType+".helmets").contains(p.getInventory().getHelmet().getTypeId())))
 				{
 					ReturnValue = true;
 				}
 			}
 			if(p.getInventory().getChestplate() != null && getConfig().getIntegerList("mobitems."+entitytype+"."+ControlType+".chestplates") != null)
 			{
 				if((getConfig().getIntegerList("mobitems."+entitytype+"."+ControlType+".chestplates").contains(p.getInventory().getChestplate().getTypeId())))
 				{
 					ReturnValue = true;
 				}
 			}
 			if(p.getInventory().getLeggings() != null && getConfig().getIntegerList("mobitems."+entitytype+"."+ControlType+".leggings") != null)
 			{
 				if((getConfig().getIntegerList("mobitems."+entitytype+"."+ControlType+".leggings").contains(p.getInventory().getLeggings().getTypeId())))
 				{
 					ReturnValue = true;
 				}
 			}
 			if(p.getInventory().getBoots() != null && getConfig().getIntegerList("mobitems."+entitytype+"."+ControlType+".boots") != null)
 			{
 				if((getConfig().getIntegerList("mobitems."+entitytype+"."+ControlType+".boots").contains(p.getInventory().getBoots().getTypeId())))
 				{
 					ReturnValue = true;
 				}
 			}
 			if(getConfig().getIntegerList("mobitems."+entitytype+"."+ControlType+".items") != null)
 			{
 				if(p.getInventory().getContents() != null)
 				{	
 					for(ItemStack i : p.getInventory().getContents())
 					{
 						if(i != null)
 						{
 							if(compareItemIDsWithDamage(getConfig().getStringList("mobitems."+entitytype+"."+ControlType+".items"), i))
 							{
 								ReturnValue = true;
 							}
 						}
 					}
 				}
 			}
 		}
 		return ReturnValue;
     }
     
     public boolean compareItemIDsWithDamage(List<String> ConfigItems, ItemStack i)
     {
     	String ItemIDWithDamage = String.valueOf(i.getTypeId());
 		if(i.getDurability() > 0)
 		{
 			ItemIDWithDamage = ItemIDWithDamage + ":" + String.valueOf(i.getDurability());
 		}
 		if(ConfigItems.contains(ItemIDWithDamage))
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
     }
     
     public void setTargetAndAnger(Creature c, Player p)
     {
     	if(c != null && p != null)
     	{
 			if(c.getType() == EntityType.PIG_ZOMBIE)
 			{
 				PigZombie pz = (PigZombie) c;
 				pz.setAngry(true);
 			}
 			if(c.getType() == EntityType.WOLF)
 			{
 				Wolf wolf = (Wolf) c;
 				wolf.setAngry(true);
 			}
 			c.setTarget(p);
     	}
     }
     
     public void removeTargetAndAnger(Creature c)
     {
     	if(c != null)
     	{
 			if(c.getType() == EntityType.PIG_ZOMBIE)
 			{
 				PigZombie pz = (PigZombie) c;
 				pz.setAngry(false);
 			}
 			if(c.getType() == EntityType.WOLF)
 			{
 				Wolf wolf = (Wolf) c;
 				wolf.setAngry(false);
 			}
 			c.setTarget(null);
     	}
     }
 }
