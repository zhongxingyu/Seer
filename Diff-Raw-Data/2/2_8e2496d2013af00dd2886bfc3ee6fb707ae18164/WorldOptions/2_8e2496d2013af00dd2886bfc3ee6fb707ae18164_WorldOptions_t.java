 package net.betterverse.worldmanager;
 
 import java.io.File;
 
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Blaze;
 import org.bukkit.entity.Chicken;
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.EnderDragon;
 import org.bukkit.entity.Enderman;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Ocelot;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Silverfish;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Slime;
 import org.bukkit.entity.Spider;
 import org.bukkit.entity.Squid;
 import org.bukkit.entity.Villager;
 import org.bukkit.entity.Wolf;
 import org.bukkit.entity.Zombie;
 
 import net.betterverse.worldmanager.util.YamlFile;
 
 public class WorldOptions {
     private final WorldManager plugin;
     private final YamlFile file;
     private World world;
     private Location spawn;
     private GameMode gameMode;
     private boolean animals;
     private boolean monsters;
     private boolean pvp;
     private boolean redstone;
     private String weather;
 
     public WorldOptions(WorldManager plugin, World world, File worldFile) {
         this.plugin = plugin;
         this.world = world;
         file = new YamlFile(plugin, worldFile);
 
         populateDefaults();
     }
 
     public boolean canCreatureSpawn(LivingEntity entity) {
         if (entity instanceof Chicken || entity instanceof Cow || entity instanceof Ocelot || entity instanceof Sheep || entity instanceof Squid || entity instanceof Villager
                 || entity instanceof Wolf) {
             return animals;
         } else if (entity instanceof Blaze || entity instanceof Creeper || entity instanceof EnderDragon || entity instanceof Enderman || entity instanceof Ghast || entity instanceof PigZombie
                 || entity instanceof Silverfish || entity instanceof Skeleton || entity instanceof Slime || entity instanceof Spider || entity instanceof Zombie) {
             return monsters;
         } else {
             return false;
         }
     }
 
     public GameMode getGameMode() {
         return gameMode;
     }
 
     public Location getSpawnLocation() {
         return spawn;
     }
 
     public boolean isPvPAllowed() {
         return pvp;
     }
 
     public boolean isRedstoneAllowed() {
         return redstone;
     }
 
     public boolean isWeather(String check) {
         return weather.equals(check);
     }
 
     public void load() {
         spawn = new Location(plugin.getServer().getWorld(file.getString("spawn.world")), file.getDouble("spawn.x"), file.getDouble("spawn.y"), file.getDouble("spawn.z"), file.getLong("spawn.yaw"),
                 file.getLong("spawn.pitch"));
         gameMode = GameMode.valueOf(file.getString("game-mode"));
         animals = file.getBoolean("animals");
         monsters = file.getBoolean("monsters");
         pvp = file.getBoolean("pvp");
         redstone = file.getBoolean("redstone");
 
         // Set the weather of the world
         setWeather(file.getString("weather"));
     }
 
     public void setAnimals(boolean animals) {
         this.animals = animals;
         file.set("animals", animals);
         file.save();
     }
 
     public void setGameMode(GameMode gameMode) {
         this.gameMode = gameMode;
         file.set("game-mode", gameMode.name());
         file.save();
     }
 
     public void setMonsters(boolean monsters) {
         this.monsters = monsters;
         file.set("monsters", monsters);
         file.save();
     }
 
     public void setPvP(boolean pvp) {
         this.pvp = pvp;
         file.set("pvp", pvp);
         file.save();
     }
 
     public void setRedstone(boolean redstone) {
         this.redstone = redstone;
        file.set("redstone", redstone);
         file.save();
     }
 
     public void setSpawnLocation(Location spawn) {
         this.spawn = spawn;
         file.set("spawn.world", spawn.getWorld().getName());
         file.set("spawn.x", spawn.getX());
         file.set("spawn.y", spawn.getY());
         file.set("spawn.z", spawn.getZ());
         file.set("spawn.pitch", spawn.getPitch());
         file.set("spawn.yaw", spawn.getYaw());
 
         file.save();
     }
 
     public void setWeather(String weather) {
         this.weather = weather;
         file.set("weather", weather);
         file.save();
 
         if (weather.equals("SUNNY")) {
             world.setStorm(false);
         } else if (weather.equals("RAINY")) {
             world.setStorm(true);
             world.setThundering(false);
         } else if (weather.equals("STORMY")) {
             world.setStorm(true);
             world.setThundering(true);
         }
     }
 
     private void populateDefaults() {
         if (!file.containsKey("animals")) {
             setAnimals(world.getAllowAnimals());
         }
         if (!file.containsKey("game-mode")) {
             setGameMode(GameMode.SURVIVAL);
         }
         if (!file.containsKey("monsters")) {
             setMonsters(world.getAllowMonsters());
         }
         if (!file.containsKey("pvp")) {
             setPvP(world.getPVP());
         }
         if (!file.containsKey("redstone")) {
             setRedstone(true);
         }
         if (!file.containsKey("spawn")) {
             setSpawnLocation(world.getSpawnLocation());
         }
         if (!file.containsKey("weather")) {
             setWeather("DEFAULT");
         }
     }
 }
