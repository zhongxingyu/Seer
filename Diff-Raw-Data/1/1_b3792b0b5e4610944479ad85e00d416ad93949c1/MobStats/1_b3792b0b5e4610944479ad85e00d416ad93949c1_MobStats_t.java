 package mobstats;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Scanner;
 import java.util.UUID;
 
 import mobstats.listeners.Commands;
 import mobstats.listeners.Entities;
 import mobstats.listeners.Players;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class MobStats extends JavaPlugin {
     private PluginDescriptionFile info;
     private PluginManager manager;
     private File directory, config;
     private HashMap<UUID, Integer> health = new HashMap<UUID, Integer>();
     public HashMap<World, ArrayList<Location>> origin = new HashMap<World, ArrayList<Location>>();
     public HashMap<UUID, Integer> levels = new HashMap<UUID, Integer>();
     public int size = 16;
     public String message;
     public String tpMessage;
     public String joinMessage;
     public String respawnMessage;
     public String portalMessage;
     public boolean sendMessage;
     public boolean sendTpMessage;
     public boolean sendJoinMessage;
     public boolean sendRespawnMessage;
     public boolean sendPortalMessage;
     
     @Override
     public void onDisable() {
         info = getDescription();
         
         System.out.println("[" + info.getName() + "] disabled");
     }
     
     @Override
     public void onEnable() {
         info = getDescription();
         manager = getServer().getPluginManager();
         directory = getDataFolder();
         message = "You are now in a level +level zone";
         tpMessage = "You have just teleported into a +level zone";
         joinMessage = "You have just joined into a +level zone";
         respawnMessage = "You have just respawned into a +level zone";
         portalMessage = "You have just moved into a +level zone";
         
         manager.registerEvent(Type.ENTITY_DAMAGE, new Entities(this), Priority.Normal, this);
         manager.registerEvent(Type.CREATURE_SPAWN, new Entities(this), Priority.Normal, this);
         
         manager.registerEvent(Type.PLAYER_MOVE, new Players(this), Priority.Normal, this);
         manager.registerEvent(Type.PLAYER_JOIN, new Players(this), Priority.Normal, this);
         manager.registerEvent(Type.PLAYER_TELEPORT, new Players(this), Priority.Normal, this);
         manager.registerEvent(Type.PLAYER_RESPAWN, new Players(this), Priority.Normal, this);
         manager.registerEvent(Type.PLAYER_PORTAL, new Players(this), Priority.Normal, this);
         
         getCommand("zone").setExecutor(new Commands(this));
          
         if (!directory.exists()) {
             directory.mkdirs();
         }
         
         config = new File (directory, "config.yml");
         
         if (!config.exists()) {
             BufferedWriter out = null;
             List<World> worlds = getServer().getWorlds();
             try {
                 out = new BufferedWriter(new FileWriter(config));
                 out.write("#Where it says 'zone:', type the size of the zone that you want.");
                 out.newLine();
                 out.write("#This number is the distance from the spawn that changes the levels.");
                 out.newLine();
                 out.write("#eg: 'zone: 16' means that when a player moves away from the spawn, the level of the zone increases every 16 blocks (1 chunk).");
                 out.newLine();
                 out.write("#For all the 'message:' areas write the appropiate message.");
                 out.newLine();
                 out.write("#If you type +level, it will be replaced with the level zone that the player just walked into.");
                 out.newLine();
                 out.write("#If you type 'false' or type nothing next to a message area, it will turn off that message.");
                 out.newLine();
                 out.write("#Type the name of the world then a location, or the word 'spawn' to set it as an origin.");
                 out.newLine();
                 out.write("#Write locations in the form of x,y,z");
                 out.newLine();
                 out.write("#The zone level will be determined by the closest origin.");
                 out.newLine();
                 out.write("#You can put more than one origin");
                 out.newLine();
                 out.write("#Lines that start with '#' or that are empty are ignored.");
                 out.newLine();
                 out.newLine();
                 out.write("zone: 16");
                 out.newLine();
                 out.write("message: You have just entered a +level zone");
                 out.newLine();
                 out.write("tpmessage: You have just teleported into a +level zone");
                 out.newLine();
                 out.write("joinmessage: You have just joined into a +level zone");
                 out.newLine();
                 out.write("respawnmessage: You have just respawned into a +level zone");
                 out.newLine();
                 out.write("portalmessage: You have just moved into a +level zone");
                out.newLine();
                 for (int b = 0; b < worlds.size(); b++) {
                     out.write(worlds.get(b).getName() + ": spawn");
                     out.newLine();
                 }
             } catch (IOException ex) {
                 System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
             } finally {
                 try {
                     out.close();
                 } catch (IOException ex){
                     System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
                 }
             }
         }
         
         sendMessage = true;
         sendTpMessage = true;
         sendJoinMessage = true;
         sendRespawnMessage = true;
         sendPortalMessage = true;
         Scanner scan = null;
         try {
             scan = new Scanner(config);
         } catch (IOException ex) {
             System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
         }
         while (scan.hasNextLine()) {
             String line = scan.nextLine();
             char[] lin = line.toCharArray();
             if (!(lin.length > 0)) continue;
             Character lane = lin[0];
             if (lane.equals('#')) continue;
             String[] parts = line.split(": ");
             if (parts.length != 2) {
                 parts = line.split(":");
                 if (parts.length != 2) continue;
             }
             if (parts[0].equalsIgnoreCase("zone")) size = Integer.parseInt(parts[1]);
             if (parts[0].equalsIgnoreCase("message")) message = parts[1];
             if (parts[0].equalsIgnoreCase("tpmessage")) tpMessage = parts[1];
             if (parts[0].equalsIgnoreCase("joinmessage")) joinMessage = parts[1];
             if (parts[0].equalsIgnoreCase("respawnMessage")) respawnMessage = parts[1];
             if (parts[0].equalsIgnoreCase("portalmessage")) portalMessage = parts[1];
             else {
                 if (getServer().getWorld(parts[0]) != null) {
                     if (parts[1].equals("spawn")) {
                         ArrayList<Location> start = new ArrayList<Location>();
                         if (origin.get(getServer().getWorld(parts[1])) != null) {
                             start = origin.get(getServer().getWorld(parts[1]));
                         }
                         start.add(getServer().getWorld(parts[0]).getSpawnLocation());
                         origin.put(getServer().getWorld(parts[0]), start);
                     }
                     else {
                         String[] coo = parts[1].split(",");
                         if (coo.length != 3) continue;
                         double x = Double.parseDouble(coo[0]);
                         double y = Double.parseDouble(coo[1]);
                         double z = Double.parseDouble(coo[2]);
                         Location start = new Location(getServer().getWorld(parts[0]), x, y, z);
                         if (origin.get(getServer().getWorld(parts[0])) == null) {
                             ArrayList<Location> starts = new ArrayList<Location>();
                             starts.add(start);
                             origin.put(getServer().getWorld(parts[0]), starts);
                         }
                         else {
                             ArrayList<Location> starts = origin.get(getServer().getWorld(parts[0]));
                             origin.remove(getServer().getWorld(parts[0]));
                             starts.add(start);
                             origin.put(getServer().getWorld(parts[0]), starts);
                         }
                     }
                 }
             }
         }
         if (message == null || message.equalsIgnoreCase("false")) sendMessage = false;
         if (tpMessage == null || tpMessage.equalsIgnoreCase("false")) sendTpMessage = false;
         if (joinMessage == null || joinMessage.equalsIgnoreCase("false")) sendJoinMessage = false;
         if (respawnMessage == null || joinMessage.equalsIgnoreCase("false")) sendRespawnMessage = false;
         if (portalMessage == null || joinMessage.equalsIgnoreCase("false")) sendPortalMessage = false;
         
         List<World> worlds = getServer().getWorlds();
         for (int x = 0; x < worlds.size(); x++) {
             List<Entity> entities = worlds.get(x).getEntities();
             for (int y = 0; y < entities.size(); y++) {
                 if ((!(entities.get(y) instanceof LivingEntity)) && (!(entities.get(y) instanceof org.bukkit.entity.Player))) continue;
                 LivingEntity found = (LivingEntity) entities.get(y);
                 int level = level(closestOriginDistance(found.getLocation()));
                 UUID id = found.getUniqueId();
                 levels.put(id, level);
                 health.put(id, health(level));
             }
         }
         System.out.println("[" + info.getName() + "] ENABLED");
     }
     
     public int level(double distance) {
         int level = (int) distance/size;
         return level;
     }
     
     public int damage(int level) {
         int damage = (int) ((int) level * 0.25);
         return damage;
     }
     
     public int health(int level) {
         int hearts = (int) ((int) level * 0.75);
         if (hearts < 0) hearts = 1;
         return hearts;
     }
     
     public Double closestOriginDistance(Location loco) {
         Double dis = null;
         for (int a = 0; a < origin.get(loco.getWorld()).size(); a++) {
             double dist = loco.distance(origin.get(loco.getWorld()).get(a));
             if (dis == null) dis = dist;
             if (dist < dis) dis = dist;
         }
         return dis;
     }
     
     public void setHealth(Entity entity) {
         if (health.get(entity.getUniqueId()) != null) health.remove(entity.getUniqueId());
             health.put(entity.getUniqueId(), health(level(closestOriginDistance(entity.getLocation()))));
     }
     
     public void damage(LivingEntity entity, int damage) {
         if (health.get(entity.getUniqueId()) == null) reSetup(entity);
         int heart = health.get(entity.getUniqueId());
         heart = heart - damage;
         health.remove(entity.getUniqueId());
         health.put(entity.getUniqueId(), heart);
         if (heart <= 0) entity.damage(entity.getHealth());
     }
     
     public void reSetup(Entity entity) {
         levels.put(entity.getUniqueId(), level(closestOriginDistance(entity.getLocation())));
         health.put(entity.getUniqueId(), health(levels.get(entity.getUniqueId())));
     }
 }
