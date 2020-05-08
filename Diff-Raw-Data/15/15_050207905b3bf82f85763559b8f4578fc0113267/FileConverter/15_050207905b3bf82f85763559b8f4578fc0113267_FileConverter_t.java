 package net.dmulloy2.autocraft.legacy;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 
 import com.google.common.io.Files;
 
 import net.dmulloy2.autocraft.AutoCraft;
 import net.dmulloy2.autocraft.types.ShipData;
 
 public class FileConverter {
 	private final AutoCraft plugin;
 	
 	public FileConverter(AutoCraft plugin) {
 		this.plugin = plugin;
 	}
 	
 	public void run() {
 		if (! needsConversion()) {
 			return;
 		}
 		
 		long start = System.currentTimeMillis();
 		
 		plugin.getLogHandler().log("Beginning File conversion task.");
 		
 		File dataFolder = plugin.getDataFolder();
 		
 		File archiveFile = new File(dataFolder, "archive");
 		archiveFile.mkdir();
 		
 		for (File file : dataFolder.listFiles()) {
 			if (file.isDirectory()) // We will handle this later
 				continue;
 			
 			File to = new File(archiveFile, file.getName());
 			if (! to.exists()) {
 				try {
 					to.createNewFile();
 				} catch (IOException e) {
 					//
 				} finally {
 					try {
 						Files.move(file, to);
 					} catch (IOException e) {
 						//
 					}
 				}
 			}
 		}
 		
 		File shipArchive = new File(archiveFile, "ships");
 		shipArchive.mkdir();
 		
 		ACPropertiesManager manager = new ACPropertiesManager(plugin);
 		for (Entry<String, ACProperties> ships : manager.getACs().entrySet()) {
 			convertShip(ships.getValue());
 		}
 		
 		File shipFile = new File(dataFolder, "ships");
 		for (File file : shipFile.listFiles()) {
 			File to = new File(shipArchive, file.getName());
 			if (! to.exists()) {
 				try {
 					to.createNewFile();
 				} catch (IOException e) {
 					//
 				} finally {
 					try {
 						Files.move(file, to);
 					} catch (IOException e) {
 						//
 					}
 				}
 			}
 		}
 		
 		plugin.saveDefaultConfig();
 		
 		plugin.getLogHandler().log("File conversion task completed. Took {0} ms!", System.currentTimeMillis() - start);
 	}
 	
 	public void convertShip(ACProperties ship) {
 		ShipData data = new ShipData(plugin);
 		
 		data.setAllowedBlocks(ship.ALLOWED_BLOCKS);
 		data.setCannonMaterial(ship.CANNON_MATERIAL);
 		data.setDropsBomb(ship.DROPS_BOMB);
 		data.setDropsNapalm(ship.DROPS_NAPALM);
 		data.setFastFlyAtSize(ship.FAST_FLY_AT_SIZE);
 		data.setFiresTnt(ship.FIRES_TNT);
 		data.setFiresTorpedo(ship.FIRES_TORPEDO);
 		data.setIgnoreAttachments(ship.IGNORE_ATTACHMENTS);
 		data.setMainType(ship.MAIN_TYPE);
 		data.setMaxAltitude(ACProperties.MAX_ALTITUDE);
 		data.setMaxBlocks(ship.MAX_BLOCKS);
 		data.setMaxCannonLength(ship.MAX_CANNON_LENGTH);
 		data.setMaxNumberOfCannons(ship.MAX_NUMBER_OF_CANNONS);
 		data.setMaxShipDimensions(ship.MAX_SHIP_DIMENSIONS);
 		data.setMinAltitude(ACProperties.MIN_ALTITUDE);
 		data.setMinBlocks(ship.MIN_BLOCKS);
 		data.setMoveSpeed(ship.MOVE_SPEED);
 		data.setShipType(ship.SHIP_TYPE);
 		
 		try {
 			data.save();
 		} catch (Exception e) {
 			plugin.getLogHandler().log(Level.SEVERE, "Could not save ship {0}: {1}", data.getShipType(), e);
 		}
 	}
 	
 	public boolean needsConversion() {
 		File dataFolder = plugin.getDataFolder();
 		for (File file : dataFolder.listFiles()) {
 			if (file.isDirectory())
 				continue;
 			
			if (! file.getName().endsWith(".yml") && ! file.getName().endsWith(".properties"))
 				return true;
 		}
 		
 		return false;
 	}
 }
