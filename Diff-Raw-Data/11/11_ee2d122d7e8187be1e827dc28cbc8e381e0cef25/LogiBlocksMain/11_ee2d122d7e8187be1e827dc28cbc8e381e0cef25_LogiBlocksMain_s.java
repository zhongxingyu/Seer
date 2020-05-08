 package plugin.bleachisback.LogiBlocks;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.BlockCommandSender;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.inventory.meta.SkullMeta;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class LogiBlocksMain extends JavaPlugin implements FlagListener
 {
 	protected Logger log;
 	private Server server;
 	private PluginDescriptionFile desc;
 	private PluginManager pm;
 	
 	protected FileConfiguration flagConfig;
 	protected Configuration config;
 	
 	protected static File flagFile;
 	protected HashMap<String, FlagListener> flags= new HashMap<String, FlagListener>();	
 	private HashMap<String,Integer> inventorySubs=new HashMap<String,Integer>();
 			
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
 		
 		getCommand("command").setExecutor(new BaseCommandListener(this));
 		getCommand("logicif").setExecutor(new LogiCommandListener(this));
 		
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
 		
 		registerFlag("getFlag",this);
 		registerFlag("isFlag",this);
 		registerFlag("getGlobalFlag",this);
 		registerFlag("isGlobalFlag",this);
 		registerFlag("hasequip",this);
 		registerFlag("inventory",this);
 		registerFlag("exists",this);
 		registerFlag("hasPassenger",this);
 		registerFlag("isPassenger",this);
 		
 		inventorySubs.put("contains",1);
 		inventorySubs.put("containsexact",1);
 		inventorySubs.put("isfull",0);
 		inventorySubs.put("isempty",0);
 		inventorySubs.put("slot",2);		
 		
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
 	
 	public boolean onFlag(String flag, String[] args, BlockCommandSender sender) throws FlagFailureException
 	{
 		switch(flag)
 		{
 			case "getflag":
				if(!(args.length<1))
 				{
 					throw new FlagFailureException();
 				}
 				if(flagConfig.contains("local."+sender.getName()+"."+args[0]))
 				{
 					return flagConfig.getBoolean("local."+sender.getName()+"."+args[0]);
 				}
 				else
 				{
 					throw new FlagFailureException();
 				}
 				//end getflag
 			case "isflag":
				if(!(args.length<1))
 				{
 					throw new FlagFailureException();
 				}
 				if(flagConfig.contains("local."+sender.getName()+"."+args[0]))
 				{
 					return true;
 				}
 				else
 				{
 					return false;
 				}
 				//end isflag
 			case "getglobalflag":
				if(!(args.length<1))
 				{
 					throw new FlagFailureException();
 				}
 				if(flagConfig.contains("global."+args[0]))
 				{
 					return flagConfig.getBoolean("global."+args[0]);
 				}
 				else
 				{
 					throw new FlagFailureException();
 				}
 				//end getglobalflag
 			case "isglobalflag":
				if(!(args.length<1))
 				{
 					throw new FlagFailureException();
 				}
 				if(flagConfig.contains("global."+args[0]))
 				{
 					return true;
 				}
 				else
 				{
 					return false;
 				}
 				//end isglobalflag
 			case "hasequip":
 				switch(args.length)
 				{
 					case 1:
 						Entity equipEntity=LogiBlocksMain.parseEntity(args[0],sender.getBlock().getWorld());				
 						if(equipEntity==null||!(equipEntity instanceof LivingEntity))
 						{
 							equipEntity=server.getPlayer(args[0]);
 							if(equipEntity==null)
 							{
 								throw new FlagFailureException();
 							}
 						}
 						ItemStack[] equip=((LivingEntity) equipEntity).getEquipment().getArmorContents();
 						return equip[0].getType()==Material.AIR?equip[1].getType()==Material.AIR?equip[2].getType()==Material.AIR?equip[3].getType()==Material.AIR?false:true:true:true:true;
 					case 2:
 						Entity equipEntity1=LogiBlocksMain.parseEntity(args[0],sender.getBlock().getWorld());				
 						if(equipEntity1==null||!(equipEntity1 instanceof LivingEntity))
 						{
 							equipEntity1=server.getPlayer(args[0]);
 							if(equipEntity1==null)
 							{
 								throw new FlagFailureException();
 							}
 						}
 						ItemStack[] equip1=((LivingEntity) equipEntity1).getEquipment().getArmorContents();
 						switch(args[1])
 						{
 							case "head":
 							case "helmet":
 								return equip1[3].getType()!=Material.AIR;
 							case "chest":
 							case "chestplate":
 							case "shirt":
 								return equip1[2].getType()!=Material.AIR;
 							case "legs":
 							case "leggings":
 							case "pants":
 								return equip1[1].getType()!=Material.AIR;
 							case "feet":
 							case "boots":
 							case "shoes":
 								return equip1[0].getType()!=Material.AIR;
 						}
 						break;
 					case 3:
 						ItemStack item=LogiBlocksMain.parseItemStack(args[2]);
 						if(item==null)
 						{
 							throw new FlagFailureException();
 						}
 						Entity equipEntity2=LogiBlocksMain.parseEntity(args[0],sender.getBlock().getWorld());				
 						if(equipEntity2==null||!(equipEntity2 instanceof LivingEntity))
 						{
 							equipEntity2=server.getPlayer(args[0]);
 							if(equipEntity2==null)
 							{
 								throw new FlagFailureException();
 							}
 						}
 						ItemStack[] equip2=((LivingEntity) equipEntity2).getEquipment().getArmorContents();
 						switch(args[1])
 						{
 							case "head":
 							case "helmet":
 								return equip2[3].equals(item);
 							case "chest":
 							case "chestplate":
 							case "shirt":
 								return equip2[2].equals(item);
 							case "legs":
 							case "leggings":
 							case "pants":
 								return equip2[1].equals(item);
 							case "feet":
 							case "boots":
 							case "shoes":
 								return equip2[0].equals(item);
 						}
 						break;
 				}
 				break;
 				//end hasequip
 			case "inventory":
 				Inventory inventory= null;
 				if(args.length<1)
 				{
 					throw new FlagFailureException();
 				}
 				if((args[0].startsWith("@l[")&&args[1].endsWith("]"))||inventorySubs.containsKey(args[0]))
 				{
 					Location invLocation=LogiBlocksMain.parseLocation(args[0], sender.getBlock().getLocation());
 					loop: for(int x=-1;x<=1;x++)
 					{
 						for(int y=-1;y<=1;y++)
 						{
 							for(int z=-1;z<=1;z++)
 							{
 								Block testBlock=invLocation.clone().add(x, y, z).getBlock();
 								if(testBlock.getState() instanceof InventoryHolder)
 								{
 									inventory=((InventoryHolder) testBlock.getState()).getInventory();
 									break loop;
 								}
 							}
 						}
 					}
 				}
 				else
 				{
 					Player player=Bukkit.getPlayer(args[0]);
 					if(player==null)
 					{
 						throw new FlagFailureException();
 					}
 					inventory=player.getInventory();
 				}
 				if(inventory==null)
 				{
 					throw new FlagFailureException();
 				}
 				int subIndex=inventorySubs.containsKey(args[0])?0:1;
 				if(!inventorySubs.containsKey(args[subIndex]))
 				{
 					throw new FlagFailureException();
 				}
 				if(inventorySubs.get(args[subIndex])+subIndex+1>args.length)
 				{
 					throw new FlagFailureException();
 				}					
 				switch(args[subIndex].toLowerCase())
 				{
 					case "contains":
 						ItemStack item=LogiBlocksMain.parseItemStack(args[subIndex+1]);
 						if(item==null)
 						{
 							throw new FlagFailureException();
 						}
 						for(ItemStack testItem:inventory.getContents())
 						{
 							if(testItem==null)
 							{
 								continue;
 							}
 							else if(item.isSimilar(testItem))
 							{
 								return true;
 							}
 						}
 						return false;
 					case "containsexact":
 						ItemStack item1=LogiBlocksMain.parseItemStack(args[subIndex+1]);
 						if(item1==null)
 						{
 							throw new FlagFailureException();
 						}
 						return inventory.contains(item1);
 					case "isfull":
 						return inventory.firstEmpty()==-1;
 					case "isempty":
 						for(ItemStack testItem:inventory.getContents())
 						{
 							if(testItem!=null)
 							{
 								throw new FlagFailureException();
 							}
 						}
 						return true;
 					case "slot":
 						int slot=Integer.parseInt(args[subIndex+1]);
 						if(slot>=inventory.getSize())
 						{
 							slot=inventory.getSize()-1;
 						}
 						if(slot<0)
 						{
 							slot=0;
 						}
 						switch(args[subIndex+2].toLowerCase())
 						{
 							case "is":
 								if(args.length<=subIndex+3)
 								{
 									throw new FlagFailureException();
 								}
 								ItemStack item2=LogiBlocksMain.parseItemStack(args[subIndex+3]);
 								if(item2==null)
 								{
 									throw new FlagFailureException();
 								}
 								return item2.isSimilar(inventory.getItem(slot));
 							case "isexact":
 								if(args.length<=subIndex+3)
 								{
 									throw new FlagFailureException();
 								}
 								ItemStack item3=LogiBlocksMain.parseItemStack(args[subIndex+3]);
 								if(item3==null)
 								{
 									throw new FlagFailureException();
 								}
 								return item3.equals(inventory.getItem(slot));
 							case "isenchanted":
 								ItemStack item4=inventory.getItem(slot);
 								if(item4==null)
 								{
 									throw new FlagFailureException();
 								}
 								return !item4.getEnchantments().isEmpty();
 						}
 						break;
 				}
 				break;
 				//end inventory
 			case "exists":
 				if(args.length<1)
 				{
 					throw new FlagFailureException();
 				}
 				return parseEntity(args[0],sender.getBlock().getWorld())!=null;
 				//end exists
 			case "haspassenger":
 				if(args.length<1)
 				{
 					throw new FlagFailureException();
 				}
 				Entity entity = parseEntity(args[0],sender.getBlock().getWorld());
 				if(entity==null)
 				{
 					throw new FlagFailureException();
 				}
 				return entity.getPassenger()!=null;
 				//end hasPassanger
 			case "ispassenger":
 				if(args.length<1)
 				{
 					throw new FlagFailureException();
 				}
 				Entity entity1 = parseEntity(args[0],sender.getBlock().getWorld());
 				if(entity1==null)
 				{
 					throw new FlagFailureException();
 				}
 				return entity1.getVehicle()!=null;
 				//end isPassenger
 		}
 		throw new FlagFailureException();
 	}
 	
 	public static boolean filter(String[] args, BlockCommandSender block, Command command)
 	{
 		for(int currentArg=0;currentArg<args.length;currentArg++)
 		{
 			String string=args[currentArg];
 			if(string.startsWith("@"))
 			{
 				switch(string.charAt(1))
 				{
 					case 'e':
 						World world=block.getBlock().getWorld();
 						//search properties
 						EntityType type=null;
 						double x=block.getBlock().getX();
 						double y=block.getBlock().getY();
 						double z=block.getBlock().getZ();
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
 
 						List<Entity> nearbyEntities=block.getBlock().getWorld().getEntities();
 						
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
 							Location loc=block.getBlock().getLocation();							
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
 							entid=""+entities[0].getEntityId();
 							args[currentArg]="@e["+entid+"]";
 						}
 						else
 						{
 							String[] newArgs=args;							
 							for(int i=0;i<c;i++)
 							{
 								String newArg="@e["+entities[i].getEntityId()+"]";
 								for(int j=currentArg+1;j<args.length;j++)
 								{
 									Bukkit.getLogger().info("Arg comparison: "+args[currentArg]+" == "+args[j]);
 									if(args[currentArg].equals(args[j]))
 									{
 										Bukkit.getLogger().info("True");
 										newArgs[j]=newArg;
 									}
 								}
 								newArgs[currentArg]=newArg;								
 								command.execute(block, command.getLabel(), newArgs);								
 							}
 							return false;
 						}
 						return true;
 						//End @e
 				}
 			}
 		}
 		return true;
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
 			World world=def.getWorld();
 			double x=def.getX();
 			double y=def.getY();
 			double z=def.getZ();
 			float yaw=def.getYaw();
 			float pitch=def.getPitch();
 			
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
 				}				
 			}
 			return new Location(world,x,y,z,yaw,pitch);
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
