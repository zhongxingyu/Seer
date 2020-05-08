 package com.infinitewarp.simpleworldwarp.executors;
 
 import org.bukkit.World.Environment;
 import org.bukkit.WorldCreator;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.infinitewarp.simpleworldwarp.StylizedMessager;
 
 public class CreateExecutor extends AbstractExecutor {
 
     public CreateExecutor(JavaPlugin plugin, String permissionRoot, StylizedMessager messager) {
         super(plugin, permissionRoot, messager);
     }
 
     protected boolean execute(CommandSender sender, String[] args) {
         if (args.length != 2 && args.length != 3) {
             return false;
         }
 
         String worldName = args[0];
         if (worldExists(worldName)) {
             getMessager().send(sender, "World '" + worldName + "' already exists!");
             return false;
         }
 
         Environment environment = null;
         try {
             environment = Environment.valueOf(args[1].toUpperCase());
         } catch (IllegalArgumentException e) {
             getMessager().send(sender, "Invalid environment '" + args[1] + "'!");
             return false;
         }
 
         WorldCreator worldCreator = new WorldCreator(worldName);
         worldCreator.environment(environment);
 
         if (args.length == 3) {
             Long seed = Long.getLong(args[2]);
             if (seed != null) {
                 worldCreator.seed(seed);
             }
         }
 
         getMessager().send(sender, "Creating world '" + worldName + "'...");
 
         getPlugin().getServer().createWorld(worldCreator);
        getPlugin().getConfig().set("worlds." + worldName + ".environment", environment);
         getPlugin().getConfig().set("worlds." + worldName + ".seed",
                 getPlugin().getServer().getWorld(worldName).getSeed());
         getPlugin().saveConfig();
 
         getMessager().send(sender, "World '" + worldName + "' successfully created!");
 
         return true;
     }
 
 }
