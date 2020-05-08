 package cah.melonar;
 
 import java.io.File;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Server;
 import org.bukkit.Chunk;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.world.ChunkUnloadEvent;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import lib.PatPeter.SQLibrary.*;
 
 public class ChunkyBlocks extends JavaPlugin implements Listener {
 	private File pluginPath = new File("plugins" + File.pathSeparator + "ChunkyBlocks");
 	private final Logger logger = Logger.getLogger("Minecraft");
 	private PluginManager pm;
 	private PluginDescriptionFile info = this.getDescription();
 	private FileConfiguration myConfig = this.getConfig();
 	private boolean debugMessages;
 	private boolean useBlock;
 	private int loadRange;
 	private int minHeight;
 	private int maxHeight;
 	private int maxChunks;
 	private Material clMaterial;
 	private SQLite cbDatabase;
 
 	@Override
 	public void onDisable(){
 		logMessage(Level.INFO, info.getName() + " is now disabled.");
 	}
 
 	@Override
 	public void onEnable(){
 		pm = getServer().getPluginManager();
 		pm.registerEvents(this, this);
 		logMessage(Level.INFO, info.getName() + " version " + info.getVersion() + " is enabled.");
 		loadConfig();
 		cbDatabase = new SQLite(logger, "ChunkyBlocks", "ChunkyBlocks", pluginPath.getPath());
 		loadDatabase();
 	}
 
 	private void loadDatabase() {
 		cbDatabase.open();
 		if(!cbDatabase.checkTable("Chunks")) {
 			logMessage(Level.INFO, "Initializing table for first use.");
			String query = "CREATE TABLE chunks (rowid INT AUTO_INCREMENT PRIMARY_KEY, player VARCHAR(16), tag VARCHAR(32), world VARCHAR(64), x INT, z INT);";
 			cbDatabase.createTable(query);
 		}
 	}
 
 	private void loadConfig(){
 		debugMessages = myConfig.getBoolean("debug",false);
 		useBlock = myConfig.getBoolean("useBlock", true);
 		loadRange = myConfig.getInt("radius", 1);
 		minHeight = myConfig.getInt("minHeight",54);
 		maxHeight = myConfig.getInt("maxHeight",74);
 		clMaterial = Material.getMaterial(myConfig.getInt("blockType", 19));
 		maxChunks = myConfig.getInt("maxChunksPerUser",1);
 
 	}
 
 	@EventHandler
 	public final void cbChunkUnload(ChunkUnloadEvent cuEvent){
 		World currentWorld = cuEvent.getWorld();
 		for (int worldX = (-1 * loadRange); worldX <= loadRange; worldX++){
 			for (int worldZ = (-1 * loadRange); worldZ <= loadRange; worldZ++){
 				Chunk currentChunk = currentWorld.getChunkAt(cuEvent.getChunk().getX()+worldX, cuEvent.getChunk().getZ()+worldZ);
 				String query = "SELECT player, tag from chunks where world = '" + currentChunk.getWorld().getName() + "' AND x = " + currentChunk.getX() + " AND z = " + currentChunk.getZ();
 				ResultSet results = cbDatabase.query(query);
 				try {
 					if(results.first()){
 						if(debugMessages){
 							logMessage(Level.FINE, "Chunk (" + currentChunk.getWorld().toString() + ": " + worldX + ", " + worldZ + ") kept loaded by " + results.getString("player") + " with a tag of " + results.getString("tag"));
 						}
 					}
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 				if(useBlock==true){
 					for (int chunkX = 0; chunkX <= 15; chunkX++){
 						for (int chunkZ = 0; chunkZ <= 15; chunkZ++){
 							for (int chunkY = minHeight; chunkY <= maxHeight; chunkY++){
 								Block thisBlock = currentChunk.getBlock(chunkX, chunkY, chunkZ);
 								if (thisBlock.getType() == clMaterial){
 									if(debugMessages){
 										logMessage(Level.FINE, "Chunk (" + currentChunk.getWorld().toString() + ": " + worldX + ", " + worldZ + ") kept loaded by block at (" + thisBlock.getLocation().toString() + ")");
 									}
 									cuEvent.setCancelled(true);
 									break;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		Player player = null;
 		if(sender instanceof Player) {
 			player = (Player) sender;
 		} else {
 			sender.sendMessage("ChunkyBlocks commands are not available from the console.");
 		}
 		String commandName = cmd.getName().toLowerCase();
 		if (commandName.equals("setchunk")){
 			String tag = args[0];
 			if(tag.isEmpty()) {
 				tag = "default";
 			}
 			if(!player.hasPermission("chunkyblocks.set")){
 				player.sendMessage("You do not have permission to use this command.");
 				this.getServer().broadcast("ChunkyBlocks: " + player.getName() + " tried to use /setchunk but does not have permission", Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 				return false;
 			}
 			String query = "SELECT sq1.owned, world, x, z FROM chunks, (SELECT count(*) AS owned FROM chunks WHERE player = '" + player.getName() + "') sq1 WHERE player = '" + player.getName() + "' AND tag = '" + tag + ";";
 			ResultSet results = cbDatabase.query(query);
 			Chunk here = player.getLocation().getChunk();
 			try {
 				if(results.first()){
 					if(results.getString("world").isEmpty()){
 						if(results.getInt("owned") < maxChunks){
 							String insert = "INSERT INTO chunks(player, tag, world, x, z) VALUES('" + player.getName() + "','" + tag + "','" + here.getWorld().getName() + "'," + here.getX() + "," + here.getZ() + ");";
 							cbDatabase.query(insert);
 							player.sendMessage("Location (" + tag + ") added to chunkloading list.");
 							return true;
 						} else {
 							player.sendMessage("You have already reached your chunkloading limit.");
 							return false;
 						}
 					} else {
 						String insert = "UPDATE chunks SET world = '" + here.getWorld().getName() + "', x = " + here.getX() + ", z = " + here.getZ() + " WHERE player = '" + player.getName() + "' AND tag = '" + tag + "';";
 						cbDatabase.query(insert);
 						player.sendMessage("Location (" + tag + ") added to chunkloading list.");
 						return true;
 					}
 				} else {
 					String update = "INSERT INTO chunks(player, tag, world, x, z) VALUES('" + player.getName() + "','" + tag + "','" + here.getWorld().getName() + "'," + here.getX() + "," + here.getZ() + ");";
 					cbDatabase.query(update);
 					player.sendMessage("Location (" + tag + ") updated in chunkloading list.");
 					return true;
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		if (commandName.equals("removechunk")){
 			String tag = args[0];
 			if(tag.isEmpty()) {
 				tag = "default";
 			}
 			if(!player.hasPermission("chunkyblocks.remove")){
 				player.sendMessage("You do not have permission to use this command.");
 				this.getServer().broadcast("ChunkyBlocks: " + player.getName() + " tried to use /removechunk but does not have permission", Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 				return false;
 			}
 			String query = "SELECT rowid FROM chunks WHERE player = '" + player.getName() + "' AND tag = '" + tag + ";";
 			ResultSet results = cbDatabase.query(query);
 			try {
 				if(!results.first()){
 					player.sendMessage("A chunk with the label of " + tag + " was not found for your username.");
 					return false;
 				} else {
 					String delete = "DELETE FROM chunks WHERE rowid = " + results.getInt("rowid") + ";";
 					cbDatabase.query(delete);
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		if (commandName.equals("mychunks")){
 			String page = args[0];
 			if(page.isEmpty()){
 				page = "1";
 			}
 			if(!player.hasPermission("chunkyblocks.list")){
 				player.sendMessage("You do not have permission to use this command.");
 				this.getServer().broadcast("ChunkyBlocks: " + player.getName() + " tried to use /mychunks but does not have permission", Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 				return false;
 			}
 			String query = "SELECT tag, world, x, z FROM chunks WHERE player = '" + player.getName() + "';";
 			ResultSet results = cbDatabase.query(query);
 			try {
 				if(!results.first()){
 					player.sendMessage("No chunks are registered to your name");
 				} else {
 					while(!results.isAfterLast()){
 						
 					}
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		if (commandName.equals("listchunks")){
 
 		}
 		if (commandName.equals("telechunk")){	
 
 		}
 		if (commandName.equals("removechunkadmin")){
 
 		}
 		return false;
 	}
 
 	private void logMessage(Level logLevel, String message) {
 		logger.log(logLevel, "[" + info.getName() + "]: " + message);
 	}
 }
