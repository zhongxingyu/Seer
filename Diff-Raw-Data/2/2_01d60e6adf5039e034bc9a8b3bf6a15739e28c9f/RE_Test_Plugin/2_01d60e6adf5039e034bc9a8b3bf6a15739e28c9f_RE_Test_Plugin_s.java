 package com.github.idragonfire.RemoteEntitesTestPlugin;
 
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.kumpelblase2.remoteentities.EntityManager;
 import de.kumpelblase2.remoteentities.RemoteEntities;
 import de.kumpelblase2.remoteentities.api.RemoteEntity;
 import de.kumpelblase2.remoteentities.api.RemoteEntityType;
 import de.kumpelblase2.remoteentities.api.thinking.goals.DesireLookAtNearest;
 import de.kumpelblase2.remoteentities.api.thinking.goals.DesireWanderAround;
 
 public class RE_Test_Plugin extends JavaPlugin {
     private EntityManager manager;
     private HashMap<String, RemoteEntityType> types;
 
     @Override
     public void onEnable() {
         manager = RemoteEntities.createManager(this);
 
         types = new HashMap<>();
 
         try {
             Field[] declaredFields = RemoteEntityType.class.getDeclaredFields();
             for (Field field : declaredFields) {
                 if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                     Object o = field.get(null);
                     if (o instanceof RemoteEntityType) {
                         RemoteEntityType t = (RemoteEntityType) o;
                         types.put(t.name().toLowerCase(), t);
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command,
             String label, String[] args) {
         try {
 
             if (!(sender instanceof Player)) {
                 sender.sendMessage("not supported");
                 return false;
             }
             Player player = (Player) sender;
             Location loc = player.getLocation();
             String cmd = args[0];
             if (cmd.equalsIgnoreCase("h")) {
                 spawnHuman(loc, "test-h");
             } else if (cmd.equalsIgnoreCase("h2")) {
                 spawnHumanWander(loc, "test-h2");
             } else if (cmd.equalsIgnoreCase("sp")) {
                 spawnSpider(loc);
             } else if (cmd.equalsIgnoreCase("sp2")) {
                 spawnSpider(loc);
             } else if (cmd.equalsIgnoreCase("z")) {
                 spawnZombie(loc);
             } else if (cmd.equalsIgnoreCase("z2")) {
                 spawnZombie2(loc);
             } else if (cmd.equalsIgnoreCase("s")) {
                 if (args.length != 2
                         || !types.containsKey(args[1].toLowerCase())) {
                     for (Entry<String, RemoteEntityType> type : types.entrySet()) {
                         player.sendMessage(type.getValue().name());
                     }
                 } else {
                     spawn(loc, types.get(args[1].toLowerCase()));
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return true;
     }
 
     public void spawn(Location loc, RemoteEntityType type) {
        manager.createNamedEntity(type, loc, null, true);
     }
 
     public void spawnHuman(Location loc, String name) {
         RemoteEntity entity = manager.createNamedEntity(RemoteEntityType.Human,
                 loc, name, true);
         entity.getMind().addMovementDesire(
                 new DesireLookAtNearest(Player.class, 8), 0);
     }
 
     public void spawnHumanWander(Location loc, String name) {
         RemoteEntity entity = manager.createNamedEntity(RemoteEntityType.Human,
                 loc, name, true);
         entity.getMind().addMovementDesire(new DesireWanderAround(), 0);
     }
 
     public void spawnSpider(Location loc) {
         manager.createNamedEntity(RemoteEntityType.Spider, loc, null, true);
     }
 
     public void spawnSpider2(Location loc) {
         RemoteEntity entity = manager.createNamedEntity(
                 RemoteEntityType.Spider, loc, null, true);
         entity.getMind().addMovementDesire(new DesireWanderAround(),
                 entity.getMind().getHighestMovementPriority() + 1);
     }
 
     public void spawnZombie(Location loc) {
         manager.createNamedEntity(RemoteEntityType.Zombie, loc, null, true);
     }
 
     public void spawnZombie2(Location loc) {
         RemoteEntity entity = manager.createNamedEntity(
                 RemoteEntityType.Zombie, loc, null, true);
         entity.getMind().addMovementDesire(new DesireWanderAround(),
                 entity.getMind().getHighestMovementPriority() + 1);
     }
 }
