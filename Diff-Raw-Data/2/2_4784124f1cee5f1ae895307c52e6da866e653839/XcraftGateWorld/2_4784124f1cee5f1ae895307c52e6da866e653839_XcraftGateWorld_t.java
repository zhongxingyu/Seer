 package de.xcraft.engelier.XcraftGate;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.entity.*;
 
 public class XcraftGateWorld {
 	public String name;
 	public Environment environment;
 	public Boolean allowAnimals = true;
 	public Boolean allowMonsters = true;
 	public Boolean allowPvP = false;
 	public Boolean allowWeatherChange = true;
 	public Integer creatureLimit = 0;
 	public Integer border = 0;
 	public Weather setWeather = Weather.SUN;
 		
 	private XcraftGate plugin;
 	private Server server;
 	private World world;
 	
 	public XcraftGateWorld (XcraftGate instance) {
 		this.plugin = instance;
 		this.server = plugin.getServer();
 	}
 	
 	public enum Weather {
 		SUN(0),
 		STORM(1);
 
 		private final int id;
 		private static final Map<Integer, Weather> lookup = new HashMap<Integer, Weather>();
 
 		private Weather(int id) {
 			this.id = id;
 		}
 
 		public int getId() {
 			return id;
 		}
 
 		public static Weather getWeather(int id) {
 			return lookup.get(id);
 		}
 
 		static {
 			for (Weather env : values()) {
 				lookup.put(env.getId(), env);
 			}
 		}
 	}
 	
 	public void load(String name, Environment env) {
 		this.name = name;
 		this.environment = env;
 		this.plugin.log.info(plugin.getNameBrackets() + "loading world " + name + "(" + env.toString() + ")");
 		this.world = server.createWorld(name, env);
 	}
 
 	public Map<String, Object> toMap() {
 		Map<String, Object> values = new HashMap<String, Object>();
 		values.put("name", name);
 		values.put("type", environment.toString());
 		values.put("border", border);
 		values.put("creatureLimit", creatureLimit);
 		values.put("allowAnimals", allowAnimals);
 		values.put("allowMonsters", allowMonsters);
 		values.put("allowPvP", allowPvP);
 		values.put("allowWeatherChange", allowWeatherChange);
 		values.put("setWeather", setWeather.toString());
 		return values;
 	}
 	
 	public Location getSafeDestination(Location loc) {
 		if (loc == null)
 			return loc;
 		
 		int x = (int) Math.floor(loc.getX());
 		int y = (int) Math.floor(loc.getY());
 		int z = (int) Math.floor(loc.getZ());
 		
 		int distance = 0;
 		int maxDistance = 64;
 
 		while (isBlockAboveAir(x, y, z) || isObstructed(x, y, z)) {
 			distance++;
 			if (distance > maxDistance) return null;
 			
 			for (int cx = 0 - distance; cx <= distance; cx++) {
 				if (!isBlockAboveAir(x + cx, y, z) && !isObstructed(x + cx, y, z)) {
 					return new Location(world, x + cx, y, z, loc.getYaw(), loc.getPitch());					
 				}
 			}
 
 			for (int cy = 0 - distance; cy <= distance; cy++) {
 				if (!isBlockAboveAir(x, y + cy, z) && !isObstructed(x, y + cy, z)) {
 					return new Location(world, x, y + cy, z, loc.getYaw(), loc.getPitch());					
 				}
 			}
 
 			for (int cz = 0 - distance; cz <= distance; cz++) {
 				if (!isBlockAboveAir(x, y, z + cz) && !isObstructed(x, y, z + cz)) {
 					return new Location(world, x, y, z + cz, loc.getYaw(), loc.getPitch());					
 				}
 			}
 		}
 		
 		return null;											
 	}
 
 	private boolean isBlockAboveAir(int x, int y, int z) {
 		return world.getBlockAt(x, y - 1, z).getType() == Material.AIR;
 	}
 
 	public boolean isObstructed(int x, int y, int z)
 	{
 		if ((world.getBlockAt(x, y, z).getType() != Material.AIR) || (world.getBlockAt(x, y + 1, z).getType() != Material.AIR))
 			return true;
 		else
 			return false;
 	}
 	
 	public void checkCreatureLimit() {
 		Double max = creatureLimit.doubleValue();
 		Integer alive = world.getLivingEntities().size() - world.getPlayers().size();
 
 		if (max <= 0) return;
 
 		if (alive >= max) {
 			((CraftWorld) world).getHandle().allowAnimals = false;
 			((CraftWorld) world).getHandle().allowMonsters = false;
 		} else if (alive <= max * 0.8) {
 			((CraftWorld) world).getHandle().allowAnimals = true;
 			((CraftWorld) world).getHandle().allowMonsters = true;
 		}
 		
 	}		
 		
 	public void killAllMonsters() {
 		for (LivingEntity entity : world.getLivingEntities()) {
 			if (entity instanceof Zombie || entity instanceof Skeleton
 					|| entity instanceof PigZombie || entity instanceof Creeper
 					|| entity instanceof Ghast || entity instanceof Spider
 					|| entity instanceof Giant || entity instanceof Slime)
 				entity.remove();
 		}
 	}
 
 	public void killAllAnimals() {
 		for (LivingEntity entity : world.getLivingEntities()) {
 			if (entity instanceof Pig || entity instanceof Sheep
 					|| entity instanceof Wolf || entity instanceof Cow
 					|| entity instanceof Squid || entity instanceof Chicken)
 				entity.remove();
 		}
 	}
 	
 	public void setCreatureLimit(Integer limit) {
 		this.creatureLimit = (limit != null ? limit : 0);
 		killAllMonsters();
 		killAllAnimals();
 	}
 	
 	public void setAllowAnimals(Boolean allow) {
 		this.allowAnimals = (allow != null ? allow : true);
 		((CraftWorld) world).getHandle().allowAnimals = allow;
 		if (!allow) killAllAnimals();
 	}
 
 	public void setAllowMonsters(Boolean allow) {
		this.allowMonsters = (allow != null ? allow : true);
 		((CraftWorld) world).getHandle().allowMonsters = allow;
 		if (!allow) killAllMonsters();
 	}
 	
 	public void setAllowWeatherChange(Boolean allow) {
 		this.allowWeatherChange = (allow != null ? allow : true);
 	}
 	
 	public void setBorder(Integer border) {
 		this.border = (border != null ? border : 0);
 	}
 	
 	public void setAllowPvP(Boolean allow) {
 		this.allowPvP = (allow != null ? allow : false);
 		this.world.setPVP(this.allowPvP);
 	}
 	
 	public void setWeather(Weather weather) {
 		boolean backup = this.allowWeatherChange;
 		this.allowWeatherChange = true;
 		this.world.setStorm(weather.getId() == Weather.STORM.id);
 		this.setWeather = weather;
 		this.allowWeatherChange = backup;
 	}
 	
 	public boolean checkBorder(Location location) {
 		return (border > 0 && Math.abs(location.getX()) <= border && Math.abs(location.getZ()) <= border) || border == 0;
 	}
 }
