 package me.hammale.mob;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class mob extends JavaPlugin {
 
 	Logger log = Logger.getLogger("Minecraft");
 	private final mobEntity entity = new mobEntity(this);
 	
 	public boolean active = false;
 	
 	public FileConfiguration config;
 	
 	@Override
 	public void onEnable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info("[NerdMob] Version: " + pdfFile.getVersion() + " Enabled!");
 		PluginManager pm = getServer().getPluginManager();
 		loadConfiguration();
 	    pm.registerEvent(Event.Type.CREATURE_SPAWN, entity, Event.Priority.Normal, this);
 	}
 
 	public void loadConfiguration(){
 	    if(exists() == false){
 		    config = getConfig();
 		    config.options().copyDefaults(false);
 		    String path = "StopNaturalMobs";
 		    config.addDefault(path, "coliseum");
 		    config.options().copyDefaults(true);
 		    saveConfig();
 	    }
 	}
 	
 	public String getStopWorlds(){
 	    config = getConfig();
 	    String message = config.getString("StopNaturalMobs"); 
 	    return message;
 	}
 	
 	private boolean exists() {	
 			try{
 			File file = new File("plugins/NerdMobs/config.yml"); 
 	        if (file.exists()) { 
 	        	return true;
 	        }else{
 	        	return false;
 	        }
 
 			}catch (Exception e){
 			  System.err.println("Error: " + e.getMessage());
 			  return true;
 			}
 	}
 	
 	  public boolean onCommand(final CommandSender sender, Command cmd, String commandLabel, String[] args){
 			if(cmd.getName().equalsIgnoreCase("butcher")){
 				if(args.length >= 1){
 					World w = getServer().getWorld(args[0]);			
 					sender.sendMessage(ChatColor.GREEN + "Killed " + removeMobs(w) + " mobs in " + args[0]);
 				}else{
 					if(sender instanceof Player){
 						Player p = (Player) sender;					
 						World w = p.getWorld();
 						sender.sendMessage(ChatColor.GREEN + "Killed " + removeMobs(w) + " mobs in " + w.getName());
 					}
 				}
 			}else if(cmd.getName().equalsIgnoreCase("spawn")){
 				if(args.length == 1){
 					if(sender instanceof Player){
 						Player p = (Player) sender;
 						World w = p.getWorld();
 						Location l = p.getTargetBlock(null, 300).getLocation();					
 						if(isValid(args[0])){
 							String name = args[0].toUpperCase();
 							CreatureType ct = CreatureType.valueOf(name);
 							sender.sendMessage("Creature: " + ct + " Name: " + name);
 							w.spawnCreature(l, ct);
 							p.sendMessage(ChatColor.LIGHT_PURPLE + "Spawned a " + args[0]);
 						}else{
 							p.sendMessage(ChatColor.RED + "Invalid name! Try /spawn list");
 						}
 					}
 				}else if(args.length == 2){
 					if(sender instanceof Player){
 						Player p = (Player) sender;
 						World w = p.getWorld();
 						Location l = p.getTargetBlock(null, 300).getLocation();					
 						if(isValid(args[0])){
 							String name = args[0].toUpperCase();
 							CreatureType ct = CreatureType.valueOf(name);
							int num = Integer.parseInt(args[0]);
 							for(int i=1;i<=num;i++){
 								w.spawnCreature(l, ct);
 							}							
 							p.sendMessage(ChatColor.LIGHT_PURPLE + "Spawned " + args[1] + " " + args[0]);
 						}else{
 							p.sendMessage(ChatColor.RED + "Invalid name! Try /spawn list");
 						}
 					}
 				}
 			}
 			return true;	
 	  }
 	
 	  public boolean isValid(String s){
 		String mob = s.toUpperCase();
 		
 		if(mob.equals("BLAZE")){
 			return true;
 		}
 
 		else if(mob.equals("CAVE_SPIDER")){
 			return true;
 		}
 		
 		else if(mob.equals("CHICKEN")){
 			return true;
 		}
 		
 		else if(mob.equals("COW")){
 			return true;
 		}
 		
 		else if(mob.equals("CREEPER")){
 			return true;
 		}
 		
 		else if(mob.equals("ENDER_DRAGON")){
 			return true;
 		}
 		
 		else if(mob.equals("ENDERMAN")){
 			return true;
 		}
 		
 		else if(mob.equals("GHAST")){
 			return true;
 		}
 		
 		else if(mob.equals("GIANT")){
 			return true;
 		}
 		
 		else if(mob.equals("MAGMA_CUBE")){
 			return true;
 		}
 		
 		else if(mob.equals("MUSHROOM_COW")){
 			return true;
 		}
 		
 		else if(mob.equals("PIG")){
 			return true;
 		}
 		
 		else if(mob.equals("PIG_ZOMBIE")){
 			return true;
 		}
 		
 		else if(mob.equals("SHEEP")){
 			return true;
 		}
 		
 		else if(mob.equals("SILVERFISH")){
 			return true;
 		}
 		
 		else if(mob.equals("SKELETON")){
 			return true;
 		}
 		
 		else if(mob.equals("SLIME")){
 			return true;
 		}
 		
 		else if(mob.equals("SNOWMAN")){
 			return true;
 		}
 		
 		else if(mob.equals("SPIDER")){
 			return true;
 		}
 		
 		else if(mob.equals("SQUID")){
 			return true;
 		}
 		
 		else if(mob.equals("VILLAGER")){
 			return true;
 		}
 		
 		else if(mob.equals("WOLF")){
 			return true;
 		}
 		
 		else if(mob.equals("ZOMBIE")){
 			return true;
 		}else{
 		  return false;
 		}
 	  }
 	  
 	public int removeMobs(World w){
 		int i = 0;
 		for(Entity e : w.getEntities()){
 			if(e instanceof LivingEntity){
 				LivingEntity le = (LivingEntity) e;
 				if(!(le instanceof Player)){				
 					le.setHealth(0);
 					i++;
 				}
 			}
 		}
 		return i;
 	}	
 	
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info("[NerdMob] Version: " + pdfFile.getVersion() + " Disabled!");
 	}
 	
 }
