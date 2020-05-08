 package plugin.bleachisback.LogiBlocks;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.inventory.meta.SkullMeta;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import plugin.bleachisback.LogiBlocks.Listeners.LogiBlocksCraftListener;
 import plugin.bleachisback.LogiBlocks.Listeners.LogiBlocksInteractListener;
 import plugin.bleachisback.LogiBlocks.Listeners.LogiBlocksRedstoneListener;
 import plugin.bleachisback.LogiBlocks.Listeners.LogiFlagListener;
 
 public class LogiBlocksMain extends JavaPlugin
 {
 	protected Logger log;
 	private Server server;
 	private PluginDescriptionFile desc;
 	private PluginManager pm;
 	
 	protected FileConfiguration flagConfig;
 	protected Configuration config;
 	
 	protected static File flagFile;
 	protected HashMap<String, FlagListener> flags= new HashMap<String, FlagListener>();	
 	
 	private Entity lastRedstone=null;
 	
 	private LogiFlagListener flagListener;
 			
 	public void onEnable()
 	{
 		server=getServer();
 		log=getLogger();
 		desc=getDescription();
 		pm=server.getPluginManager();
 		
 		flagFile=new File(getDataFolder(), "flags");		
 		flagConfig=YamlConfiguration.loadConfiguration(flagFile);
 		convertOldFlags();
 		
 		saveDefaultConfig();
 		config=getConfig();
 		updateConfig();
 		
 		getCommand("command").setExecutor(new BaseCommandListener(this));
 		getCommand("logicif").setExecutor(new LogiCommandListener(this));
 		
		if(!config.getBoolean("disable-filtering",false))
		{
			pm.registerEvents(new FilterCommandListener(this),this);
		}
 		if(config.getBoolean("allow-crafting", true))
 		{
 			pm.registerEvents(new LogiBlocksCraftListener(this), this);
 			setupRecipe();
 		}
 		if(config.getBoolean("allow-command-insertion",true))
 		{
 			pm.registerEvents(new LogiBlocksInteractListener(this), this);
 			setupPermissions();
 		}
 		if(config.getBoolean("listen-for-redstone",true))
 		{
 			pm.registerEvents(new LogiBlocksRedstoneListener(this), this);
 		}
 		
 		flagListener=new LogiFlagListener(this);
 		registerFlag("getFlag",flagListener);
 		registerFlag("isFlag",flagListener);
 		registerFlag("getGlobalFlag",flagListener);
 		registerFlag("isGlobalFlag",flagListener);
 		registerFlag("hasequip",flagListener);
 		registerFlag("inventory",flagListener);
 		registerFlag("exists",flagListener);
 		registerFlag("hasPassenger",flagListener);
 		registerFlag("isPassenger",flagListener);		
 		
 		log.info(desc.getFullName()+" is enabled");
 	}
 	
 	public void onDisable()
 	{
 		trace(desc.getName()+" is disabled");
 	}
 	
 	private void trace(String string)
 	{
 		log.info(string);
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		switch(cmd.getName())
 		{
 			case "phantom":
 				if(sender.hasPermission("c.hidden.phantom")&&args.length>1)
 				{
 					server.getPlayer(args[0]).remove();
 					return true;
 				}
 				break;				
 		}
 		return false;
 	}
 	
 	private char[][] getchar={{'a','b','c'},{'d','e','f'},{'g','h','i'}};
 	private void setupRecipe()
 	{
 		Material[][] mats=new Material[3][3];
 		HashMap<Material,Character> chars=new HashMap<Material,Character>();
 		for(int i=0;i<3;i++)
 		{
 			String[] units=config.getStringList("crafting-recipe").get(i).replace(" ","").split(",");
 			for(int j=0;j<3;j++)
 			{
 				Material material=Material.matchMaterial(units[j]);
 				if(material==null)
 				{
 					material=Material.getMaterial(Integer.parseInt(units[j]));
 				}
 				mats[i][j]=material;
 				if(!chars.containsKey(material))
 				{
 					chars.put(material,getchar[i][j]);
 				}
 			}
 		}		
 		ItemStack result=new ItemStack(Material.COMMAND,1);
 		ShapedRecipe recipe=new ShapedRecipe(result);
 		String[] shape=new String[3];
 		for(int i=0;i<3;i++)
 		{
 			shape[i]="";
 			for(int j=0;j<3;j++)
 			{
 				Material material=mats[i][j];
 				shape[i]=shape[i]+chars.get(material).toString();
 			}
 		}
 		recipe.shape(shape);
 		for(int i=0;i<3;i++)
 		{
 			for(int j=0;j<3;j++)
 			{
 				Material material=mats[i][j];
 				if(material!=Material.AIR)
 				{
 					recipe.setIngredient(chars.get(material), material);
 				}				
 			}
 		}		
 		server.addRecipe(recipe);
 	}
 	
 	private void setupPermissions()
 	{
 		for(String perm:config.getConfigurationSection("permissions").getKeys(false))
 		{
 			pm.addPermission(new Permission("c.permission."+perm,PermissionDefault.OP));
 		}
 	}
 	
 	private void convertOldFlags()
 	{
 		//Converts old flag files to new ones
 		//All old flags are now global flags
 		for(String name:flagConfig.getKeys(false))
 		{
 			if(!name.equals("global")&&!name.equals("local"))
 			{
 				flagConfig.set("global."+name, flagConfig.getBoolean(name));
 				flagConfig.set(name, null);
 				try 
 				{
 					flagConfig.save(flagFile);
 				} 
 				catch (IOException e) 
 				{
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	private void updateConfig()
 	{
 		Map<String,Object> map=config.getDefaults().getValues(true);
 		for(String key:map.keySet())
 		{
 			if(config.get(key,null)==null)
 			{
 				config.set(key, map.get(key));				
 			}
 		}		
 		saveConfig();
 	}
 	
 	
 	
 	public boolean filter(String[] args, CommandSender sender, Command command, Location loc)
 	{
 		for(int currentArg=0;currentArg<args.length;currentArg++)
 		{
 			String string=args[currentArg];
 			if(string.equals("&&"))
 			{	
 				String[] originalCommand=Arrays.copyOfRange(args,0,currentArg);
 				String commandLine=command.getName()+" ";
 				for(String commandPart:originalCommand)
 				{
 					commandLine=commandLine+commandPart+" ";
 				}
 				Bukkit.dispatchCommand(sender, commandLine);
 				
 				String[] newCommand=Arrays.copyOfRange(args,currentArg+1,args.length);
 				String commandLine2="";
 				for(String commandPart:newCommand)
 				{
 					commandLine2=commandLine2+commandPart+" ";
 				}
 				Bukkit.dispatchCommand(sender, commandLine2);
 				return false;
 			}
 			else if(string.equals("\\&&"))
 			{
 				args[currentArg]="&&";
 			}
 			else if(string.startsWith("@"))
 			{
 				switch(string.substring(1,string.contains("[")?string.indexOf("["):string.length()))
 				{
 					case "e":
 						World world=loc.getWorld();
 						//search properties
 						EntityType type=null;
 						double x=loc.getX();
 						double y=loc.getY();
 						double z=loc.getZ();
 						int r=0;
 						int rm=0;
 						int c=1;
 						boolean rand=false;
 						
 						if(string.contains("[")&&string.contains("]"))
 						{
 							if(!string.contains("="))
 							{
 								return true;
 							}
 							for(String args1:string.substring(string.indexOf("[")+1, string.indexOf("]")).split(","))
 							{
 								if(args1.length()<3||!args1.contains("="))
 								{
 									continue;
 								}
 								if(args1.length()<=args1.indexOf("="))
 								{
 									continue;
 								}
 								switch(args1.substring(0,args1.indexOf("=")))
 								{
 									case "t":
 										type=EntityType.fromName(args1.substring(2,args1.length()));
 										break;
 									case "x":
 										x=Double.parseDouble(args1.substring(2,args1.length()));
 										break;
 									case "y":
 										y=Double.parseDouble(args1.substring(2,args1.length()));
 										break;
 									case "z":
 										z=Double.parseDouble(args1.substring(2,args1.length()));
 										break;
 									case "loc":
 										String[] locArray=args1.substring(2,args1.length()).split("\\|");
 										switch(locArray.length)
 										{
 											default:
 											case 3:
 												z=Double.parseDouble(locArray[2]);
 											case 2:
 												y=Double.parseDouble(locArray[1]);
 											case 1:
 												x=Double.parseDouble(locArray[0]);
 												break;
 											case 0:
 												break;
 										}
 										break;
 									case "r":
 										r=Integer.parseInt(args1.substring(2,args1.length()));
 										break;
 									case "rm":
 										rm=Integer.parseInt(args1.substring(3,args1.length()));
 										break;
 									case "c":
 										c=Integer.parseInt(args1.substring(2,args1.length()));
 										break;
 									case "rand":
 										rand=Boolean.parseBoolean(args1.substring(5,args1.length()));
 										break;
 								}
 							}
 						}
 
 						List<Entity> nearbyEntities=loc.getWorld().getEntities();
 						
 						for(int i=0;i<nearbyEntities.size();i++)
 						{
 							if(i<0)
 							{
 								continue;
 							}
 							Entity entity=nearbyEntities.get(i);
 							if((entity.getLocation().distance(new Location(world,x,y,z))>r&&r>0)
 									||(entity.getLocation().distance(new Location(world,x,y,z))<rm))
 							{																
 								nearbyEntities.remove(entity);
 								i--;
 							}
 							if(type!=null)
 							{
 								if(entity.getType()!=type)
 								{
 									nearbyEntities.remove(entity);
 									i--;
 								}
 							}
 						}
 						if(nearbyEntities.size()==0)
 						{
 							return false;
 						}
 						
 						if(c<1)
 						{
 							c=nearbyEntities.size();
 						}
 						
 						Entity[] entities=new Entity[c];
 
 						if(rand)
 						{
 							for(int i=0;i<c;i++)
 							{
 								Entity entity=nearbyEntities.get(new Random().nextInt(nearbyEntities.size()));
 								entities[i]=entity;
 								nearbyEntities.remove(entity);
 							}
 						}
 						else
 						{
 							for(int i=0;i<c;i++)
 							{
 								double distance=nearbyEntities.get(0).getLocation().distance(loc);
 								Entity entity=nearbyEntities.get(0);
 								for(Entity e:nearbyEntities)
 								{
 									if(loc.distance(e.getLocation())<distance)
 									{
 										distance=loc.distance(e.getLocation());
 										entity=e;
 									}
 								}
 								entities[i]=entity;
 								nearbyEntities.remove(entity);
 							}
 						}
 						String entid="";
 						if(c==1)
 						{
 							if(entities[0] instanceof Player)
 							{
 								args[currentArg]=((Player)entities[0]).getName();
 							}
 							else
 							{
 								entid=""+entities[0].getEntityId();
 								args[currentArg]="@e["+entid+"]";
 							}							
 						}
 						else
 						{
 							String[] newArgs=args;
 							for(int i=0;i<newArgs.length;i++)
 							{
 								if(newArgs[i].equals("&&"))
 								{
 									newArgs[i]="\\&&";
 								}
 							}
 							for(int i=0;i<c;i++)
 							{
 								String newArg="";
 								if(entities[i] instanceof Player)
 								{
 									newArg=((Player)entities[0]).getName();
 								}
 								else
 								{
 									newArg="@e["+entities[i].getEntityId()+"]";
 								}
 								for(int j=currentArg+1;j<args.length;j++)
 								{
 									if(args[currentArg].equals(args[j]))
 									{
 										newArgs[j]=newArg;
 									}
 								}
 								newArgs[currentArg]=newArg;								
 								command.execute(sender, command.getLabel(), newArgs);								
 							}
 							return false;
 						}
 						return true;
 						//End @e
 					case "lr":
 						if(lastRedstone==null)
 						{
 							return false;
 						}
 						else if(lastRedstone instanceof Player)
 						{
 							args[currentArg]=((Player)lastRedstone).getName();
 						}
 						else
 						{
 							args[currentArg]="@e["+lastRedstone.getEntityId()+"]";
 						}						
 						return true;
 						//end @r
 				}
 			}
 		}
 		return true;
 	}
 	
 	public void setLastRedstone(Entity ent)
 	{
 		lastRedstone=ent;
 	}
 	
 	public Entity getLastRedstone()
 	{
 		return lastRedstone;
 	}
 	
 	public FileConfiguration getFlagConfig()
 	{
 		return flagConfig;
 	}
 	
 	public static ItemStack parseItemStack(String itemString)
 	{
 		if(itemString.startsWith("@i[")&&itemString.endsWith("]"))
 		{
 			Material mat=Material.AIR;
 			int amount=1;
 			short damage=0;
 			String name=null;
 			String lore=null;
 			HashMap<Enchantment, Integer> enchantments= new HashMap<Enchantment, Integer>();
 			String owner=null;
 			
 			for(String arg:itemString.substring(itemString.indexOf("[")+1,itemString.indexOf("]")).split(","))
 			{
 				if(arg.length()<3||!arg.contains("="))
 				{
 					continue;
 				}
 				if(arg.length()<=arg.indexOf("="))
 				{
 					continue;
 				}				
 				switch(arg.substring(0,arg.indexOf("=")))
 				{
 					case "m":
 					case "mat":
 					case "material":
 					case "id":
 						mat=Material.matchMaterial(arg.substring(arg.indexOf("=")+1,arg.length()).toUpperCase());
 						break;
 					case "c":
 					case "count":
 					case "amount":
 						amount=Integer.parseInt(arg.substring(arg.indexOf("=")+1,arg.length()));
 						break;
 					case "d":
 					case "data":
 					case "damage":
 					case "durability":
 						damage=Short.parseShort(arg.substring(arg.indexOf("=")+1,arg.length()));
 						break;
 					case "n":
 					case "name":
 						name=arg.substring(arg.indexOf("=")+1,arg.length()).replace("_", " ");
 						break;
 					case "l":
 					case "lore":
 					case "description":
 						lore=arg.substring(arg.indexOf("=")+1,arg.length());
 						break;
 					case "e":
 					case "enchant":
 					case "enchantment":
 						String enchString=arg.substring(arg.indexOf("=")+1,arg.length());
 						if(enchString.contains("|"))
 						{							
 							String[] enchStringArray=enchString.split("\\|");
 							enchantments.put(Enchantment.getByName(enchStringArray[0].toUpperCase()), Integer.valueOf(enchStringArray[1]));
 						}
 						break;
 					case "o":
 					case "owner":
 						owner=arg.substring(arg.indexOf("=")+1,arg.length());
 						break;
 				}				
 			}
 			if(mat==null)
 			{
 				mat=Material.AIR;
 			}
 			ItemStack item=new ItemStack(mat,amount,damage);
 			ItemMeta meta=item.getItemMeta();
 			if(name!=null)
 			{
 				meta.setDisplayName(name);
 			}
 
 			if(lore!=null)
 			{
 				meta.setLore(Arrays.asList(lore.replace("_"," ").split("\\|")));
 			}
 			if(item.getType()==Material.SKULL_ITEM&&owner!=null)
 			{
 				((SkullMeta) meta).setOwner(owner);
 			}
 			item.setItemMeta(meta);
 			for(Enchantment ench:enchantments.keySet())
 			{
 				if(ench==null||enchantments.get(ench)==null)
 				{
 					continue;
 				}
 				item.addUnsafeEnchantment(ench, enchantments.get(ench));
 			}			
 			return item;
 		}
 		return null;		
 	}
 	
 	public static Entity parseEntity(String entity, World world)
 	{		
 		if(entity.startsWith("@e[")&&entity.endsWith("]"))
 		{
 			int id=0;
 			try
 			{
 				id=Integer.parseInt(entity.substring(3, entity.length()-1));
 			}
 			catch(Exception e)
 			{
 				return null;
 			}
 			for(Entity ent:world.getEntities())
 			{
 				if(ent.getEntityId()==id)
 				{
 					return ent;
 				}
 			}
 		}
 		return Bukkit.getPlayer(entity);
 	}
 	
 	public static Location parseLocation(String locString, Location def)
 	{
 		if(locString.startsWith("@l[")&&locString.endsWith("]"))
 		{
 			World world=Bukkit.getWorlds().get(0);					
 			double x=0;
 			double y=0;
 			double z=0;
 			float yaw=0;
 			float pitch=0;
 			boolean rand=false;
 			int radius=100;
 			int minRadius=0;
 			
 			if(def!=null)
 			{
 				world=def.getWorld();
 				x=def.getX();
 				y=def.getY();
 				z=def.getZ();
 				yaw=def.getYaw();
 				pitch=def.getPitch();
 			}
 			
 			for(String arg:locString.substring(locString.indexOf("[")+1,locString.indexOf("]")).split(","))
 			{
 				if(arg.length()<3||!arg.contains("="))
 				{
 					continue;
 				}
 				if(arg.length()<=arg.indexOf("="))
 				{
 					continue;
 				}				
 				switch(arg.substring(0,arg.indexOf("=")))
 				{
 					case "w":
 					case "world":
 						world=Bukkit.getWorld(arg.substring(arg.indexOf("=")+1,arg.length()));
 						break;
 					case "c":
 					case "coords":
 					case "coordinates":
 						String[] coords=arg.substring(arg.indexOf("=")+1,arg.length()).split("\\|");
 						switch(coords.length)
 						{
 							default:
 							case 3:
 								z=Double.parseDouble(coords[2]);
 							case 2:
 								y=Double.parseDouble(coords[1]);
 							case 1:
 								x=Double.parseDouble(coords[0]);
 								break;
 							case 0:
 								break;
 						}
 						break;
 					case "x":
 						x=Double.parseDouble(arg.substring(arg.indexOf("=")+1,arg.length()));
 						break;
 					case "y":
 						y=Double.parseDouble(arg.substring(arg.indexOf("=")+1,arg.length()));
 						break;
 					case "z":
 						z=Double.parseDouble(arg.substring(arg.indexOf("=")+1,arg.length()));
 						break;
 					case "p":
 					case "pitch":
 						pitch=Float.parseFloat(arg.substring(arg.indexOf("=")+1,arg.length()));
 						break;
 					case "ya":
 					case "yaw":
 						yaw=Float.parseFloat(arg.substring(arg.indexOf("=")+1,arg.length()));
 						break;
 					case "rand":
 						rand=Boolean.parseBoolean(arg.substring(arg.indexOf("=")+1,arg.length()));
 						break;
 					case "r":
 						radius=Integer.parseInt(arg.substring(arg.indexOf("=")+1,arg.length()));
 						break;
 					case "mr":
 						minRadius=Integer.parseInt(arg.substring(arg.indexOf("=")+1,arg.length()));
 						break;
 				}				
 			}
 			Location location=new Location(world,x,y,z,yaw,pitch);
 			if(rand)
 			{
 				Random randGen=new Random();
 				while(true)
 				{
 					double _x=randGen.nextInt(radius*2)-radius+x;
 					double _y=randGen.nextInt(radius*2)-radius+y;
 					double _z=randGen.nextInt(radius*2)-radius+z;
 					Location randLoc=new Location(world,_x,_y,_z,yaw,pitch);
 					if(randLoc.distance(location)<=radius&&randLoc.distance(location)>=minRadius)
 					{
 						return randLoc;
 					}
 				}				
 			}
 			else
 			{
 				return location;
 			}
 		}
 		else if(parseEntity(locString,def.getWorld())!=null)
 		{
 			return parseEntity(locString,def.getWorld()).getLocation();
 		}
 		return def;		
 	}
 	
 	public void registerFlag(String flag, FlagListener listener)
 	{
 		if(listener==null)
 		{			
 			return;
 		}
 		flags.put(flag.toLowerCase().replace(" ", "_"), listener);
 	}
 }
