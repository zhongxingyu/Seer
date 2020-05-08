 package com.au_craft.GGTeleport;
 
 import java.io.File;
 import java.util.Random;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class GGTeleport extends JavaPlugin implements Listener {
 	
 	public static int[] blockList = {8,9,10,11};
 	public static double[] testLocation={0,0,0,0};
 	
 	@Override
 	public void onEnable(){
 		File file = new File(getDataFolder() + File.separator + "config.yml");
 		if (!file.exists()) {
 			createConfig();
 		}
 		if (testIfNumber(getConfig().getString("xRadius")) == false || testIfNumber(getConfig().getString("zRadius")) == false){
 			getLogger().warning("[GG Teleport] Config File Error: Regenerating Config.");
 			createConfig();
 		}
 			
 		getLogger().info("Enabled");
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 		
 	@Override
 	public void onDisable(){
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		if (!(sender instanceof Player)){ //Check for Player as Sender
 			sender.sendMessage("[GG Teleport] You need to be a player in game to access this command!");
 			return true;
 		}
 		if (cmd.getName().equalsIgnoreCase("tpr")){
 			if (args.length == 0) {
 				int xRadius = getConfig().getInt("xRadius");
 				int zRadius = getConfig().getInt("zRadius");
				sender.sendMessage("[GG Teleportat] Started with a " + xRadius + " x " + zRadius + " radius!");
 				Player player = (Player) sender;
 				tpr(player, xRadius, zRadius);
 			} else if (args.length == 1){
 				if (testIfNumber(args[0]) == false){
 					sender.sendMessage("[GG Teleport] '" + args[0] + "' is not a valid number.");
 				} else {
 					sender.sendMessage("[GG Teleport] Started with a " + args[0] + " radius!");
 					Player player = (Player) sender;
 					tpr(player, Integer.parseInt(args[0]), Integer.parseInt(args[0]));
 				}
 			} else if (args.length == 2){
 				if (testIfNumber(args[0]) == false || testIfNumber(args[1]) == false){
 					sender.sendMessage("[GG Teleport] '" + args[0] + "' or '" + args[1] + "' is not a valid number.");
 				} else {
 					sender.sendMessage("[GG Teleport] Started with a " + args[0] + " x " + args[1] + " radius!");
 					Player player = (Player) sender;
 					tpr(player, Integer.parseInt(args[0]), Integer.parseInt(args[1]));
 				}
 			} else {
 				 sender.sendMessage("[GG Teleport] Too many Arguments");
 			}
 		} else if (cmd.getName().equalsIgnoreCase("tpreload")){
 			this.reloadConfig();
 			this.getLogger().info("[GG Teleport] Config Reloaded from file");
 			sender.sendMessage("[GG Teleport] Config Reloaded!");
 		}
 		return true;
 	}
 	private boolean testIfNumber(String s){
 		try {
			@SuppressWarnings("unused")
 			int n = Integer.parseInt(s);
 		} catch (NumberFormatException nfe){
 			return false;
 		}
 		return true;
 	}
 	
 	private void createConfig(){
 		getConfig().addDefault("xRadius", 500);
 		getConfig().addDefault("zRadius", 500);
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		getLogger().info("config.yml generated");
 	}
 	
 	private void tpr(Player player, int xRadius, int zRadius) {
 		Location currentLocation = player.getLocation();
 		World w = currentLocation.getWorld();
 		Random random = new Random();
 		
 		int tooManyTimes = 0;
 		
 		while (testLocation[0] != 1 && tooManyTimes < 100){
 									
 			double x = random.nextInt(xRadius * 2) - xRadius;
 			double z = random.nextInt(zRadius * 2) - zRadius;
 			
 			testLocation[1] = x + currentLocation.getX();
 			testLocation[3] = z + currentLocation.getZ();
 			testLocation[2] = w.getHighestBlockYAt((int) testLocation[1], (int) testLocation[3]) - 1;
 			
 			getLogger().info("testLocation[1]: " + Double.toString(testLocation[1]));
 			getLogger().info("testLocation[2]: " + Double.toString(testLocation[2]));
 			getLogger().info("testLocation[3]: " + Double.toString(testLocation[3]));
 			
 			int testBlock = w.getBlockTypeIdAt((int) testLocation[1], (int) testLocation[2], (int) testLocation[3]);
 			
 			getLogger().info("testBlock: " + Integer.toString(testBlock));
 			
 			int isSafe = 0;
 			for (int i=0; i < blockList.length; i++){
 				if (testBlock != blockList[i]){
 					isSafe++;
 				}
 			}
 			if (isSafe == blockList.length){
 				testLocation[0] = 1;
 			} else {
 				testLocation[0] = 0;
 			}
			
 
 			tooManyTimes++;
 		}
 		
 		getLogger().info("tooManyTimes: " + Integer.toString(tooManyTimes));
 		
 		if (testLocation[0] == 1){
 			Location finalLocation = currentLocation;
 			
 			if (testLocation[1] < 0){
 				finalLocation.setX(testLocation[1]+1);
 			} else {
 				finalLocation.setX(testLocation[1]);
 			}
 			
 			finalLocation.setY(testLocation[2] + 1);
 			
 			if (testLocation[3] < 0){
 				finalLocation.setZ(testLocation[3]+1);
 			} else {
 				finalLocation.setZ(testLocation[3]);
 			}
 			
 			player.teleport(finalLocation);
 			player.sendMessage("[GG Teleport] Complete!");
 		} else {
 			player.sendMessage("[GG Teleport] Failed!");
 		}
 		testLocation[0] = 0;
 	}
 }
