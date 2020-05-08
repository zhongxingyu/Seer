 package me.xXLupoXx.NoSpawn;
 
 import java.io.File;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class NoSpawn extends JavaPlugin
 {
   NoSpawnEntityListener el;
   NoSpawnWorldListener wl;
   ConfigBuffer cb;
   MobCounter mc;
   private static String configfile = "plugins/NoSpawn/config.yml";
 
   CommandHandler cmh;
 
   public void onEnable()
   {
 	  this.cb = new ConfigBuffer(this);
 	  
 	  for(World w : this.getServer().getWorlds())
 	  {
 		  cb.worldSpawns.put(w, new Spawns(cb));
 	  }
     
 
     File ft = new File(configfile);
     if (!ft.exists())
     {
     	cb.setupConfig();
     	cb.readConfig();
 
     }else
     {
     	cb.readConfig();
     }
 
 
     cb.plugin = this;
     this.el = new NoSpawnEntityListener(this.cb);
     mc = new MobCounter(this.getServer(),cb);
     wl = new NoSpawnWorldListener(cb);
     this.cmh = new CommandHandler(this.getServer(), this.cb);
     PluginDescriptionFile pdfFile = getDescription();
     
     if(setupPermissions()){
     	
     	System.out.println("[NoSpawn] Permissions found, using Permissions!");
     	
     }else{
     	
     	System.out.println("[NoSpawn] No Permissions found, Op's can use commands!");
     	
     }
     
     System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
     
     
     getServer().getPluginManager().registerEvent(Event.Type.CREATURE_SPAWN, this.el, Event.Priority.Normal, this);
     getServer().getPluginManager().registerEvent(Event.Type.WORLD_LOAD, this.wl, Event.Priority.Normal, this);
     
 
   }
 
 
 public void onDisable()
   {
 	cb.worldSpawns = null;
	getServer().getScheduler().cancelTasks(this);
     PluginDescriptionFile pdfFile = getDescription();
     System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled... Oh my god they are coming!!!");
   }
 
 
   
   
   @Override
   public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 
 	  if(command.getName().equals("nospawn")){
 		  
 		  if(args.length > 0){
 			  if(args[0].equals("allowspawn")){
 				  
 				  return cmh.allowSpawn(sender, args);
 				  
 			  } else if (args[0].equals("denyspawn")){
 				  
 				  return cmh.denySpawn(sender, args);
 				  
 			  } else if (args[0].equals("despawn")){
 				  
 				  return cmh.despawnMobs(sender, args);
 				  
 			  } else if (args[0].equals("spawn")){
 				  
 				  return cmh.SpawnMob(sender, args);
 				  
 			  } else if (args[0].equals("setmoblimit")){
 				  
 				  return cmh.setMobLimit(sender, args);
 				  
 			  } else if (args[0].equals("settotalmoblimit")){
 				  
 				  return cmh.setTotalMobLimit(sender, args);
 				  
 			  } else if (args[0].equals("settimer")){
 				  
 				  return cmh.setMobTimer(sender, args);
 				  
 			  } else {
 				  
 				  sendNospawnMessage(sender,args[0] + " isn't a valid parameter! Please use allowspawn, denyspawn, despawn, spawn, setmoblimit, settotalmoblimit or settimer ",ChatColor.RED);
 				  return false;
 				  
 			  }
 		  } else {
 			  
 				  sendNospawnMessage(sender, "No arguments given. Please use allowspawn, denyspawn, despawn or spawn",ChatColor.RED);
 				  
 		  }
 	  }
 	  
 	  return false;
   }
   
   private boolean setupPermissions(){
 	    Plugin perm = this.getServer().getPluginManager().getPlugin("Permissions");
 
 		if (cb.Permissions == null) {
 		        if (perm != null) {
 		            cb.Permissions = ((Permissions)perm).getHandler();
 		            return true;
 		            
 		        } else {
 		            return false;
 		        }
 		    }
 		    return false;
 		}
   
   public void sendNospawnMessage(CommandSender sender, String Message, ChatColor Color){
 	  
 	  if(sender instanceof Player){
 		  
 		  Player player = (Player)sender;
 		  player.sendMessage(Color + Message);
 		  
 	  } else {
 		  
 		  System.out.println("[NoSpawn] " + Message);
 		  
 	  }
 	  
   }
 
 }
