 package uk.co.jacekk.bukkit.spawnertest;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.SpawnerEntry;
 import org.bukkit.block.Block;
 import org.bukkit.block.CreatureSpawner;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Firework;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.TNTPrimed;
 import org.bukkit.util.Vector;
 
import uk.co.jacekk.bukkit.baseplugin.command.BaseCommandExecutor;
import uk.co.jacekk.bukkit.baseplugin.command.CommandHandler;
 
 public class SetSpawnerPropertiesExecutor extends BaseCommandExecutor<SpawnerTest> {
 
     public SetSpawnerPropertiesExecutor(SpawnerTest plugin) {
         super(plugin);
     }
 
     @CommandHandler(names = { "clean" }, description = "Removes all entities from a world.")
     public void clean(CommandSender sender, String label, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage(ChatColor.RED + "You must be a Player to use this command");
             return;
         }
 
         Player player = (Player) sender;
 
         for (Entity entity : player.getWorld().getEntities()) {
             entity.remove();
         }
     }
 
     @CommandHandler(names = { "getspawnerproperties", "gsp" }, description = "Gets the properties of spawners")
     public void getSpawnerProperties(CommandSender sender, String label, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage(ChatColor.RED + "You must be a Player to use this command");
             return;
         }
 
         Player player = (Player) sender;
         Block target = player.getTargetBlock(null, 10);
 
         if (target.getType() != Material.MOB_SPAWNER) {
             player.sendMessage(ChatColor.RED + "You must be looking at a spawner");
             return;
         }
 
         CreatureSpawner spawner = (CreatureSpawner) target.getState();
 
         player.sendMessage("============================");
         player.sendMessage("Type: " + spawner.getSpawnedType().name());
         player.sendMessage("Max delay: " + spawner.getMaxDelay());
         player.sendMessage("Min delay: " + spawner.getMinDelay());
         player.sendMessage("Count: " + spawner.getCount());
         player.sendMessage("Range: " + spawner.getRange());
         player.sendMessage("Max entities: " + spawner.getMaxNearbyEntities());
         player.sendMessage("Player range: " + spawner.getRequiredPlayerRange());
         player.sendMessage("Spawn potentials:");
         
         for (SpawnerEntry entry : spawner.getSpawnPotentials()){
             player.sendMessage(entry.getEntityType().name() + " " + entry.getWeight());
         }
         
         player.sendMessage("============================");
     }
 
     @CommandHandler(names = { "setspawnerproperties", "ssp" }, description = "Sets the properties of spawners")
     public void setSpawnerProperties(CommandSender sender, String label, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage(ChatColor.RED + "You must be a Player to use this command");
             return;
         }
 
         if (args.length != 9) {
             sender.sendMessage(ChatColor.RED + "/" + label + " <type> <max_delay> <min_delay> <count> <range> <max_entities> <player_range> <above> <below>");
             return;
         }
 
         Player player = (Player) sender;
         Block target = player.getTargetBlock(null, 10);
 
         if (target.getType() != Material.MOB_SPAWNER) {
             player.sendMessage(ChatColor.RED + "You must be looking at a spawner");
             return;
         }
 
         EntityType type = EntityType.PIG;
 
         try {
             type = EntityType.valueOf(args[0]);
         } catch (IllegalArgumentException e) {
             player.sendMessage(ChatColor.RED + "Invalid entity type");
 
             for (EntityType valid : EntityType.values()) {
                 player.sendMessage("  " + valid.name());
             }
 
             return;
         }
 
         CreatureSpawner spawner = (CreatureSpawner) target.getState();
 
         spawner.setSpawnedType(type);
         spawner.setMaxDelay(Integer.parseInt(args[1]));
         spawner.setMinDelay(Integer.parseInt(args[2]));
         spawner.setCount(Integer.parseInt(args[3]));
         spawner.setRange(Integer.parseInt(args[4]));
         spawner.setMaxNearbyEntities(Integer.parseInt(args[5]));
         spawner.setRequiredPlayerRange(Integer.parseInt(args[6]));
         spawner.setVerticalRange(Integer.parseInt(args[8]), Integer.parseInt(args[7]));
 
         sender.sendMessage(ChatColor.GREEN + "Spawner updated");
     }
 
     @CommandHandler(names = { "tntspawner", "tnt" }, description = "makes a horrible tnt spawner")
     public void tntspawner(CommandSender sender, String label, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage(ChatColor.RED + "You must be a Player to use this command");
             return;
         }
 
         Player player = (Player) sender;
         Block target = player.getTargetBlock(null, 10);
 
         if (target.getType() != Material.MOB_SPAWNER) {
             player.sendMessage(ChatColor.RED + "You must be looking at a spawner");
             return;
         }
 
         CreatureSpawner spawner = (CreatureSpawner) target.getState();
 
         TNTPrimed tnt = player.getWorld().spawn(target.getLocation(), TNTPrimed.class);
         tnt.setFuseTicks(100);
         tnt.setIsIncendiary(true);
         tnt.setVelocity(new Vector(0.0d, 1.5d, 0.0d));
 
         spawner.setSpawnedEntity(tnt, false);
         spawner.setMaxDelay(1);
         spawner.setMinDelay(1);
         spawner.setCount(1);
         spawner.setRange(10);
         spawner.setMaxNearbyEntities(1000);
         spawner.setRequiredPlayerRange(4);
 
         sender.sendMessage(ChatColor.GREEN + "Spawner updated");
     }
 
     @CommandHandler(names = { "fireworkspawner", "fw" }, description = "makes a horrible tnt spawner")
     public void fireworkspawner(CommandSender sender, String label, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage(ChatColor.RED + "You must be a Player to use this command");
             return;
         }
 
         Player player = (Player) sender;
         Block target = player.getTargetBlock(null, 10);
 
         if (target.getType() != Material.MOB_SPAWNER) {
             player.sendMessage(ChatColor.RED + "You must be looking at a spawner");
             return;
         }
 
         CreatureSpawner spawner = (CreatureSpawner) target.getState();
 
         Firework firework = player.getWorld().spawn(target.getLocation(), Firework.class);
         firework.setVelocity(new Vector(0.0d, 1.5d, 0.0d));
         
         spawner.setSpawnedEntity(firework, (args.length == 0));
         spawner.setMaxDelay(1);
         spawner.setMinDelay(1);
         spawner.setCount(1);
         spawner.setRange(10);
         spawner.setMaxNearbyEntities(1000);
         spawner.setRequiredPlayerRange(20);
         
         sender.sendMessage(ChatColor.GREEN + "Spawner updated");
     }
     
     @CommandHandler(names = { "multispawner", "multi" }, description = "makes a horrible tnt spawner")
     public void multispawner(CommandSender sender, String label, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage(ChatColor.RED + "You must be a Player to use this command");
             return;
         }
 
         Player player = (Player) sender;
         Block target = player.getTargetBlock(null, 10);
 
         if (target.getType() != Material.MOB_SPAWNER) {
             player.sendMessage(ChatColor.RED + "You must be looking at a spawner");
             return;
         }
 
         CreatureSpawner spawner = (CreatureSpawner) target.getState();
         
         spawner.clearSpawnPotentials();
         spawner.addPotentialSpawnedType(EntityType.SHEEP, 1);
         spawner.addPotentialSpawnedType(EntityType.COW, 1);
         spawner.addPotentialSpawnedType(EntityType.PIG, 1);
         spawner.addPotentialSpawnedType(EntityType.WOLF, 1);
         
         spawner.setMaxDelay(1);
         spawner.setMinDelay(1);
         spawner.setCount(1);
         spawner.setRange(10);
         spawner.setMaxNearbyEntities(1000);
         spawner.setRequiredPlayerRange(4);
     }
 
 }
