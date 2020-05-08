 package rageteam.cookieslap.maps;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import rageteam.cookieslap.main.CookieSlap;
 
 public class Map {
 	CookieSlap cookieslap;
 	String name;
 	File file;
 	private FileConfiguration config;
 	int spawncount;
 	int floorcount;
 	boolean usable;
 	
 	public Map(CookieSlap plugin, String name) {
 		this.cookieslap = plugin;
 		
 		this.name = name;
 		
 		this.spawncount = 0;
 		this.floorcount = 0;
 	}
 	
 	public void load()  {
 		cookieslap.chat.log("Loading map " + this.name + ".");
 		
 		usable = false;
 		
 		this.file = new File(cookieslap.getDataFolder(), this.name + ".yml");
 		
 		try {
 			if (!this.file.exists())
 				this.file.createNewFile();
 		}catch (IOException e){
 		}
 		
 		this.setConfig(YamlConfiguration.loadConfiguration(file));
 		
 		save();
 		loadSpawns();
 		loadFloors();
 		
		if(this.spawncount > 0) {
 			usable = true;
 		} else {
 			cookieslap.chat.log("Spawn count is 0");
 			usable = false;
 		}
 		
		if(this.floorcount > 0){
 			usable = true;
 		} else {
 			cookieslap.chat.log("No floors are setup");
 			usable = false;
 		}
 		
 		if (usable) {
 			cookieslap.chat.log("Map is usable");
 		} else {
 			cookieslap.chat.log("---<>---[PLEASE SETUP THE MAP!]---<>---");
 		}
 		
 		cookieslap.chat.log("Load Complete");
 	}
 	
 	public boolean isUsable(){
 		return this.usable;
 	}
 	
 	public void delete() {
 		this.file.delete();
 	}
 	
 	public void savenumbers() {
 		getConfig().set("Spawn.count", spawncount);
 	}
 	
 	public void save(){
 		try{
 			this.getConfig().save(this.file);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void setSpawn(int id, Location l) {
 		int x = l.getBlockX();
 		int y = l.getBlockY();
 		int z = l.getBlockZ();
 		
 		float pitch = l.getPitch();
 		float yaw = l.getYaw();
 		
 		String worldname = l.getWorld().getName();
 		
 		getConfig().set("Spawns." + id + ".world", worldname);
 		getConfig().set("Spawns." + id + ".x", x);
 		getConfig().set("Spawns." + id + ".y", y);
 		getConfig().set("Spawns." + id + ".z", z);
 		getConfig().set("Spawns." + id + ".pitch", pitch);
 		getConfig().set("Spawns." + id + ".yaw", yaw);
 		
 		save();
 	}
 	
 	public Location getSpawn(int id){
 		int x,y,z;
 		float yaw,pitch;
 		World world;
 		
 		x = getConfig().getInt("Spawns." + id + ".x");
 		y = getConfig().getInt("Spawns." + id + ".y");
 		z = getConfig().getInt("Spawns." + id + ".z");
 		
 		yaw = getConfig().getInt("Spawns." + id + ".yaw");
 		pitch = getConfig().getInt("Spawns." + id + ".pitch");
 		
 		world = Bukkit.getWorld(getConfig().getString("Spawns." + id + ".world"));
 		
 		return new Location(world, x + 0.5D, y + 0.5D, z + 0.5D, yaw, pitch);
 	}
 	
 	public String getName(){
 		return name;
 	}
 	
 	public void loadSpawns(){
 		for(int a = 1; a <= getCount(); a++){
 			this.spawncount = a;
 		}
 	}
 	
 	public void loadFloors(){
 		for(int a = 1; a <= getFloors(); a++){
 			this.floorcount = a;
 		}
 	}
 	
 	public void addSpawn(Location l) {
 		this.spawncount += 1;
 		
 		savenumbers();
 		
 		setSpawn(spawncount, l);
 	}
 	
 	public int getCount(){
 		return getConfig().getInt("Spawns.count");
 	}
 	
 	public int getSpawnCount(){
 		return this.spawncount;
 	}
 	
 	public void setConfig(FileConfiguration  config){
 		this.config = config;
 	}
 	
 	public FileConfiguration getConfig(){
 		return config;
 	}
 
 	public void addFloor(Location p1, Location p2) {
 		this.floorcount += 1;
 		
 		savenumbers();
 		
 		config.set("Floors." + floorcount + ".p1.x", p1.getBlockX());
 		config.set("Floors." + floorcount + ".p1.y", p1.getBlockY());
 		config.set("Floors." + floorcount + ".p1.z", p1.getBlockZ());
 		config.set("Floors." + floorcount + ".p1.world", p1.getWorld().getName());
 		
 		config.set("Floors." + floorcount + ".p2.x", p2.getBlockX());
 		config.set("Floors." + floorcount + ".p2.y", p2.getBlockY());
 		config.set("Floors." + floorcount + ".p2.z", p2.getBlockZ());
 		config.set("Floors." + floorcount + ".p2.world", p2.getWorld().getName());
 		
 		save();
 	}
 
 	public Location getFloor(int id, String pos) {
 		int x,y,z;
 		String world;
 		x = config.getInt("Floors." + id + ".p" + pos + ".x");
 		y = config.getInt("Floors." + id + ".p" + pos + ".y");
 		z = config.getInt("Floors." + id + ".p" + pos + ".z");
 		world = config.getString("Floors." + id + ".p" + pos + ".world");
 		return new Location(Bukkit.getWorld(world), x, y, z);
 	}
 	
 	public int getFloors(){
 		return getConfig().getInt("Floors.count");
 	}
 }
