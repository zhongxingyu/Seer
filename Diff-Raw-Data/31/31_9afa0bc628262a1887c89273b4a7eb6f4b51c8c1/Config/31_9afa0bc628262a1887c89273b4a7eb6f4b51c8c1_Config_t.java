 package net.lotrcraft.strategycraft;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import net.lotrcraft.strategycraft.buildings.Building;
 import net.lotrcraft.strategycraft.buildings.BuildingManager;
 import net.lotrcraft.strategycraft.buildings.Castle;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.util.config.Configuration;
 
@SuppressWarnings("deprecation")
 public class Config {
 	
 	public static int coreBlock;
 	
	static Configuration playerConfig;
 	static Building tmpB;
 	static Castle castle;
 	public static File playerDataFolder = new File("plugins" + File.separator + "StrategyCraft" + File.separator + "PlayerCastles");
 	public static File buildingFolder = new File("plugins" + File.separator + "StrategyCraft" + File.separator + "Buildings");
 	public static long saveFrequency;
 	public static int maxBuildings;
 
 	public static void load() {
 		
 		Main.log.info("[StrategyCraft] Loading Buildings...");
 		if (!buildingFolder.exists()){
 			buildingFolder.mkdirs();
 		} else {
 		
 			File[] buildings = buildingFolder.listFiles();
 			if (buildings.length != 0){
 				
 				for (int y = 0; y < buildings.length; y++){
 					if (!buildings[y].getName().contains(".jar") || !buildings[y].isFile())
 						continue;
 					
					File tmp = new File("jar:file://" + buildings[y].getPath() + "!" + File.separator);
					File bldgCnfTmp = new File("jar:file://" + buildings[y].getPath() + File.separator + "configuration.yml");
 					
					Main.log.info("[StrategyCraft] Found building " + buildings[y].getName() +". Atempting to read...");
 					
 					if (!bldgCnfTmp.exists()){
 						Main.log.warning("[StrategyCraft] Missing configuration file for Building " + buildings[y].getName() + ", go nag the author for an update.");
						Main.log.info(tmp + "\n" + bldgCnfTmp);
						bldgCnfTmp.mkdirs();
						try {
							bldgCnfTmp.createNewFile();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
 						continue;
 					}
 					
 					Configuration bldgConfig = new Configuration(bldgCnfTmp);
 					bldgConfig.load();
 					String buildingClassPath, unitClassPath, author = bldgConfig.getString("unit", "someone");
 					
 					if ((unitClassPath = bldgConfig.getString("unit", null)) == null){
 						Main.log.warning("[StrategyCraft] Building " + buildings[y].getName() + " is missing the unit class in its conf, go nag the author for an update.");
 						continue;
 					}
 					if ((buildingClassPath = bldgConfig.getString("building", null)) == null){
 						Main.log.warning("[StrategyCraft] Building " + buildings[y].getName() + " is missing the building class in its conf, go nag the author for an update.");
 						continue;
 					}
 					
 					Class buildingClass, unitClass;
 					
					Main.log.info("[StrategyCraft] Config for building " + buildings[y].getName() +" read. Loading classes...");
					
 					
 					try {
 						//ClassLoaderUtil.addFile(tmp);
 						
 						URL url = tmp.toURI().toURL();
 						
 						URLClassLoader jcl = new URLClassLoader(new URL[]{url});
 						
 						unitClass =  jcl.loadClass(unitClassPath);
 						buildingClass =  jcl.loadClass(buildingClassPath);
 						
 						BuildingManager.addBuildingType(buildingClass);
 						
 					
 					} catch (MalformedURLException e) {
 						e.printStackTrace();
 					} catch (ClassNotFoundException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					
					Main.log.info("[StrategyCraft] Building " + buildings[y].getName() +" successfully loaded!");
 				}
 				
 			} else {
 				Main.log.severe("[StrategyCraft] Can't find any buildings!");
 			}
 		}
 		
 
 
 		
 		
 		
 		Main.log.info("[StrategyCraft] Finished loading buildings.");
 	
 		Configuration config = Main.config;
 		config.load();
 		coreBlock =  getInt("coreBlock", 49, config);
 		saveFrequency = getInt("saveFrequency", 3600, config );
 		maxBuildings = getInt("maxBuildings", 10, config );
 		
 		Main.log.info("[StrategyCraft] Loading Castles...");
 		
 		if (!playerDataFolder.exists()){
 			playerDataFolder.mkdirs();
 		} else {
 			File[] playerFiles = playerDataFolder.listFiles();
 			if (playerFiles == null) return;
 			
 			for (int counter = 0; counter < playerFiles.length; counter++){
 				
 				if (!playerFiles[counter].getName().endsWith(".yml"))
 					continue;
 				
 				String playerName = playerFiles[counter].getName().substring(0, playerFiles[counter].getName().indexOf('.'));
 				
 				if (!playerFiles[counter].canRead()){
 					Main.log.warning("Can't read file!");
 				}
 				
 				playerConfig = new Configuration(playerFiles[counter]);
 				playerConfig.load();
 				
 				if (isNull("Castle.Citadel.X", playerConfig)){
 					Main.log.info("[StrategyCraft] Config for " + playerName + " missing X for citadel. Rejecting...");
 					continue;
 				}
 				int x = playerConfig.getInt("Castle.Citadel.X", -1);
 				if (isNull("Castle.Citadel.Y", playerConfig)){
 					Main.log.info("[StrategyCraft] Config for " + playerName + " missing Y for citadel. Rejecting...");
 					continue;
 				}
 				int y = playerConfig.getInt("Castle.Citadel.Y", -1);
 				if (isNull("Castle.Citadel.Z", playerConfig)){
 					Main.log.info("[StrategyCraft] Config for " + playerName + " missing Z for citadel. Rejecting...");
 					continue;
 				}
 				int z = playerConfig.getInt("Castle.Citadel.Z", -1);
 				
 				Main.log.info(Bukkit.getWorlds().get(1).getName());
 				World world = Bukkit.getWorld(playerConfig.getString("Castle.Citadel.world", null));
 				
 				if (world == null) {
 					Main.log.info("[StrategyCraft] Config for " + playerName + " missing world for citadel. Rejecting...");
 					continue;
 				}
 				
 				Location location = new Location(world, x, y, z);
 				
 				castle = new Castle(location, playerName);
 				BuildingManager.addCastle(playerName, castle);
 				
 				playerConfig.save();
 				
 				Main.log.info("[StrategyCraft] Castle for " + playerName + " loaded!");
 				
 			}
 		}
 		
 		Main.log.info("[StrategyCraft] Finished loading castles.");
 		
 		Main.config.save();
 	}
 
 	public Object getProperty(String path, Object def, Configuration config) {
 		if(isNull(path, config))
 			return setProperty(path, def, config);
 		return config.getProperty(path);
 	}
 
 	public static int getInt(String path, Integer def, Configuration config) {
 		if(isNull(path, config))
 			return (Integer) setProperty(path, def, config);
 		return config.getInt(path, def);
 	}
 
 	public static Boolean getBoolean(String path, Boolean def, Configuration config) {
 		if(isNull(path, config))
 			return (Boolean) setProperty(path, def, config);
 		return config.getBoolean(path, def);
 	}
 
 	private static Object setProperty(String path, Object val, Configuration config) {
 		config.setProperty(path, val);
 		return val;
 	}
 
 	private static boolean isNull(String path, Configuration config) {
 		return config.getProperty(path) == null;
 	}
 
 	public static void removePlayerConf(String owner) {
 		File file = new File(File.pathSeparator + "StrategyCraft" + File.pathSeparator + "PlayerCastles" + File.pathSeparator + owner + ".yml");
 		if (file.exists()){
 			file.delete();
 		}
 	}
 }
