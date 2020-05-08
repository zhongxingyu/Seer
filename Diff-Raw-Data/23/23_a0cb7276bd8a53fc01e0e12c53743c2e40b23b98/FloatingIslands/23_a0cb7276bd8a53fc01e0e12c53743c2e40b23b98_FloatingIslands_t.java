 package me.tobi.FloatingIslands;
 
 
 import java.io.File;
 import java.util.List;
 
 import me.tobi.FloatingIslands.Listeners.PlayerRespawnListener;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.WorldType;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class FloatingIslands extends JavaPlugin{
 	
 	public static final String VERSION="0.1";
 	private FloatingIslandsConfig config;
 	private World floatingIslandsWorld;
 	
 	@Override
 	public void onEnable(){
 		
 		/*ensure that the plugin data folder and the config file exist*/
 		if(!this.getDataFolder().exists()){
 			boolean created=this.getDataFolder().mkdir();
 			if(!created) this.getLogger().warning("Unable to create plugin data folder!");
 		}
 		if(!new File(this.getConfig().getCurrentPath()).exists()){
			this.saveDefaultConfig();
 		}
 		
 		/*load and parse configuratiion parameters*/
 		config=new FloatingIslandsConfig(this.getConfig());
 		config.parse();
 		this.getLogger().info(config.getConfigurationAsString());
 		
 		/*generate/load the floatings islands world*/
 		WorldCreator wc=new WorldCreator("FloatingIslandsWorld");
 		wc.environment(World.Environment.NORMAL);
 		wc.generateStructures(config.generateStructures);
 		wc.generator(new FloatingIslandsChunkGenerator(config));
 		//TODO: seed?
 		wc.type(WorldType.NORMAL);
 		floatingIslandsWorld=getServer().createWorld(wc);
 		Util.ensureValidSpawn(floatingIslandsWorld, this.getDataFolder(), config);
 
 		/*register event handlers*/
 		getServer().getPluginManager().registerEvents(
 				new PlayerRespawnListener(floatingIslandsWorld), this);
 		
 		getLogger().info("FloatingIslands version "+VERSION+" enabled.");
 	}
 	
 	@Override
 	public void onDisable(){
 		getLogger().info("FloatingIslands disabled.");
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args){
 		//floating islands plugin commands
 		if(cmd.getName().equalsIgnoreCase("floatingIslands")){
 			//check for correct number of arguments
 			if(args.length==0 || args.length>1){
 				sender.sendMessage("Invalid number of arguments.");
 				return false;
 			}
 			//check for join option
 			if(args[0].equalsIgnoreCase("join")){
 				if(sender instanceof Player){
 					Player player=(Player)sender;
 					if(player.getWorld()!=floatingIslandsWorld){
 						FloatingIslandsPlayerHandler ph=Util.deserializePlayerHandler(
 								this.getDataFolder(), player.getName());
 						if(ph==null){
 							ph=new FloatingIslandsPlayerHandler(this.getServer(),
 									player.getName());
 						}
 						else ph.setServer(this.getServer());
 						ph.setRegularSpawn(player.getLocation());
 						ph.setRegularInventory(player.getInventory());
 						Location fiSpawn=ph.getFloatingIslandsSpawn();
 						if(!ph.hasJoinedFloatingIslandsBefore()){
 							ItemStack []items=new ItemStack[3]; //TODO: read from config
 							items[0]=new ItemStack(Material.ICE, 1);
 							items[1]=new ItemStack(Material.LAVA_BUCKET);
 							items[2]=new ItemStack(Material.MELON_SEEDS);
 							player.getInventory().setContents(items);
 						}
 						else{
 							ph.fillFloatingIslandsInventory(player.getInventory());
 						}
 						Util.serializePlayerHandler(ph, this.getDataFolder());
 						if(fiSpawn!=null){
 							player.teleport(fiSpawn);
 						}
 						else{
 							player.teleport(floatingIslandsWorld.getSpawnLocation());
 						}
 					}
 					else{
 						player.sendMessage("You are already in the " +
 								"FloatingIslands realm.");
 					}
 				}
 				else{
 					sender.sendMessage("This command can only be executed ingame.");
 				}
 				return true;
 			}
 			//check for leave option
 			else if(args[0].equalsIgnoreCase("leave")){
 				if(sender instanceof Player){
 					Player player=(Player)sender;
 					if(player.getWorld()==floatingIslandsWorld){
 						List<World> worlds=this.getServer().getWorlds();
 						if(worlds.size()>0){
 							FloatingIslandsPlayerHandler ph=Util.deserializePlayerHandler(
 									this.getDataFolder(), player.getName());
 							if(ph==null){
 								ph=new FloatingIslandsPlayerHandler(this.getServer(),
 										player.getName());
 							}
 							else ph.setServer(this.getServer());
 							ph.setFloatingIslandsSpawn(player.getLocation());
 							ph.setFloatingIslandsInventory(player.getInventory());
 							Location regularSpawn=ph.getRegularSpawn();
 							ph.fillRegularInventory(player.getInventory());
 							Util.serializePlayerHandler(ph, this.getDataFolder());
 							if(regularSpawn!=null){
 								player.teleport(regularSpawn);
 							}
 							else{
 								player.teleport(worlds.get(0).getSpawnLocation());
 							}
 						}
 						else{
 							player.sendMessage("Unable to find regular world."
 									+"Abort.");
 						}
 					}
 					else{
 						player.sendMessage("You are not in the " +
 								"FloatingIslands realm, so you can't leave.");
 					}
 				}
 				else{
 					sender.sendMessage("This command can only be executed ingame.");
 				}
 				return true;
 			}
 			//invalid option
 			else{
 				sender.sendMessage("Unknown option \""+args[0]+"\".");
 				return false;
 			}
 		}
 		//unknwon command, not interpreted by this plugin
 		else return false;
 	}
 }
