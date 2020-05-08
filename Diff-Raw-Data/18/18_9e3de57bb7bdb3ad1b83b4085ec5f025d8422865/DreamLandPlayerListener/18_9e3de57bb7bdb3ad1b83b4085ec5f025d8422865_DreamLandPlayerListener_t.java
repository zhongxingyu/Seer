 package me.cmesh.DreamLand;
 
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Block;
 import java.io.OutputStreamWriter;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 //import org.bukkit.event.player.PlayerBedLeaveEvent;
 import org.bukkit.event.player.PlayerBedEnterEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.BufferedReader;
 import java.util.logging.Logger;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 
 import java.io.BufferedWriter;
 
 public class DreamLandPlayerListener extends PlayerListener
 {
 	public static DreamLand plugin;
 	public static final Logger log = Logger.getLogger("Minecraft");
 	
 	public DreamLandPlayerListener(DreamLand instance)
 	{
 		plugin = instance;
 	}
 
 	//Main Functions
 	public void onPlayerPortal(PlayerPortalEvent event)
 	{
 		if (playerInDreamLand(event.getPlayer()))
 		{
 			event.setCancelled(true);
 		}
 	}
   
 	public void onPlayerInteract(PlayerInteractEvent event)
 	{
 		if (plugin.dreamFly)
 		{
 			Player player = event.getPlayer();
 			if (plugin.checkpermissions(player,"dreamland.fly",true))
 			{
 				if (playerInDreamLand(player))
 				{
 					if (plugin.flyTool.contains("-1") || plugin.flyTool.contains(Integer.toString(event.getPlayer().getItemInHand().getTypeId())))
 					{
 						if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
 						{
 						
 							Vector dir = player.getLocation().getDirection().multiply(plugin.flySpeed);
 							dir.setY(0.75);
 							player.setVelocity(dir);
 							player.setFallDistance(0);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public void onPlayerMove(PlayerMoveEvent event)
 	{
 		Player player = event.getPlayer();
 
 		if (playerDreaming(player))
 		{
 			Boolean tick = tick();
 			
 			if(tick)
 			{
				noWeather(player);
 			}
 			if (event.getTo().getY() < 0)
 			{
 				leaveDream(player);
 			}
 			if(playerSpawn(player))
 			{
 				Location loc = getSpawn(player.getWorld());
 				try
 				{
 					player.teleport(loc);
 				}
 				catch (java.lang.NullPointerException e)
 				{
 					if(playerInNightmare(player))
 					{
 						loc = plugin.nightmareWorld().getSpawnLocation();
 					}
 					else
 					{
 						loc = plugin.dreamWorld().getSpawnLocation();
 					}
 					player.teleport(loc);
 				}
 			}
 			else
 			{
 				if(tick && playerInNightmare(player))
 		    	{
 		    		player.setFireTicks(3*30);
 		    	}
 			}
 			if(plugin.morningReturn)
 			{
				long time = plugin.getServer().getWorlds().get(0).getTime();
 				if(time >=0 && time <= 12000)
 				{
 					player.sendMessage("It is morning, WAKEUP!");
 					leaveDream(player);
 				}
 			}
 		}
 		else
 		{
 			if(playerSpawn(player))//for respawn (is this nessesary)
 			{
 				leaveDream(player);
 			}
 		}
 	}
 	
 	public void onPlayerBedEnter(PlayerBedEnterEvent event)
 	{
 		Player player = event.getPlayer();
 		if (plugin.checkpermissions(player,"dreamland.goto",true) && !getLock(player) && !playerDreaming(player))
 		{
 			if(plugin.attemptWait == 0 || getWait(player))
 			{
 				if (new Random().nextInt(plugin.chance) == 0)
 				{
 					enterDream(player, event.getBed().getLocation(), new Random().nextInt(plugin.nightmareChance) == 0);
 	    		}
     		}
     		if(!attemptFile(player).exists())
     		{
     			setAttemptTime(player);
     		}
     	}
     }
 	
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
     	Player player = event.getPlayer();
     	
     	if (playerDreaming(player) && plugin.teleportOnQuit)
 		{
     		leaveDream(player);
 		}
 	}
 
 	public void onPlayerRespawn(PlayerRespawnEvent event)
 	{
 		Player player = event.getPlayer();
     	if (playerDreaming(player))
 		{
     		playerSetSpawn(player);
 		}
 	}
 
 	//helper functions
  
 	private Boolean playerDreaming(Player player)
 	{
 		return playerInDreamLand(player) || (playerInNightmare(player) && plugin.nightmare);
 	}
 	
 	private Boolean playerInDreamLand(Player player)
 	{
 		return player.getWorld().getName().equalsIgnoreCase(plugin.dreamWorld().getName());
 	}
 
 	private Boolean playerInNightmare(Player player)
 	{
 		return player.getWorld().getName().equalsIgnoreCase(plugin.nightmareWorld);
 	}
 	
 	private void noWeather(Player player)
 	{
 		World world = player.getWorld();
 		world.setStorm(false);
 		world.setThundering(false);
 		world.setWeatherDuration(0);
 		//TODO have this happen less often
 	}
 
     private void message(Player player)
     { 
     	File save = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "message.txt");
 		if (!save.exists()) 
 		{
 			return;
 		}
 		
 		try 
 		{
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
 
 			String inputLine = br.readLine();
 			
 			while(inputLine != null) 
 			{
 				player.sendMessage(inputLine);
 				inputLine = br.readLine();
 			}
 			
 		}
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
     }
 
     private void enterDream(Player player, Location bed, Boolean nightmare)
     {
     	createLock(player);
 		
 		saveLocation(player, bed);
 
 		savePlayerHealth(player);
 			
 		if(plugin.seperateInv)
 		{
 			savePlayerInv(player, player.getWorld());
 			if(!nightmare)
 			{
 				loadPlayerInv(player, plugin.dreamWorld());
 			}
 		}
 		
 		clearAttempt(player);
 		
 		Location loc = null; 
 		if(nightmare)
 		{
 			loc = getSpawn(plugin.nightmareWorld());
 		}
 		else
 		{
 			loc = getSpawn(plugin.dreamWorld());
 		}
 						
 		try
 		{
 			player.teleport(loc);
 			playerSetSpawn(player); //set double tp
 		}
 		catch (java.lang.NullPointerException e)
 		{
 			if(!nightmare)
 			{
 				loc = plugin.dreamWorld().getSpawnLocation();
 			}
 			else
 			{
 				loc = plugin.nightmareWorld().getSpawnLocation();
 			}
 			player.teleport(loc);
 			saveSpawn(player);
 			playerSetSpawn(player); //set double tp
 		}
 		
 		removeLock(player);
 		if(plugin.message)
 		{
 			message(player);
 		}
     	log.info(player.getName() + " went to Dream Land");
     	return;
     }
  
     private void leaveDream(Player player)
     {
     	player.setFireTicks(0);
     	loadPlayerHealth(player);
     	Location loc = null;
 		try
 		{
 			player.setFallDistance(0);
 			loc = loadLocation(player);
 			
 			player.setFallDistance(0);
 			player.teleport(checkBedSpawnLoc(loc));
 			player.setFallDistance(0);
 		}
 		catch (java.lang.NullPointerException e)
 		{
 			loc = plugin.getServer().getWorlds().get(0).getSpawnLocation();
 			player.setFallDistance(0);
 			player.teleport(loc);
 			player.setFallDistance(0);
 		}
 		if(plugin.seperateInv)
 		{
 			savePlayerInv(player, plugin.dreamWorld());
 			player.getInventory().clear();
 			loadPlayerInv(player, loc.getWorld());
 		}
 		log.info(player.getName() + " left DreamLand");
 		loadPlayerHealth(player);
     }
 
 	private Boolean tick()
 	{
 		int timeoutSeconds = 3; 
 		if(plugin.tick + timeoutSeconds*30 <= plugin.getServer().getWorlds().get(0).getTime())
 		{
 			plugin.tick = plugin.getServer().getWorlds().get(0).getTime();
 			return true;
 		}
 		return false;
 	}
 
     
     //health
     private File playerHealthFile(Player player)
     {
     	File healthFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Health");
 		if (!healthFolder.exists()) 
 		{
 			healthFolder.mkdir();
 		}
 		return new File(healthFolder + File.separator + player.getName());
     }
     
     private void savePlayerHealth(Player player)
     {
     	BufferedWriter bw;
 		try 
 		{
 			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(playerHealthFile(player))));
 			bw.write(player.getHealth());
 			bw.close();
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
     }
     
     private void loadPlayerHealth(Player player)
     {
     	File save = playerHealthFile(player);
 		if (!save.exists()) 
 		{
 			return;
 		}
 		
 		try 
 		{
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
 			player.setHealth(Integer.parseInt(br.readLine()));
 			br.close();
 		}
 		catch (IOException e) 
 		{
 			return;
 		}
 		catch (java.lang.NumberFormatException e)
 		{
 			return;
 		}
 		catch (java.lang.IllegalArgumentException e)
 		{
 			return;
 		}
     }
     
     
     //wait time
 	private File attemptFile(Player player)
 	{
 		File attemptFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Attempts");
 		if (!attemptFolder.exists()) 
 		{
 			attemptFolder.mkdir();
 		}
 		return new File(attemptFolder + File.separator + player.getName());
 	}
 	
 	private void setAttemptTime(Player player)
 	{
 		BufferedWriter bw;
 		try 
 		{
 			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(attemptFile(player))));
 			Long temp = plugin.getServer().getWorlds().get(0).getTime();
 			bw.write(temp.toString());
 			bw.close();
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	private void clearAttempt(Player player)
 	{
 		attemptFile(player).delete();
 	}
 	
 	private Long getAttemptTime(Player player)
 	{
 		File save = attemptFile(player);
 		Long retVal = Long.parseLong("0");
 		if (!save.exists()) 
 		{
 			return retVal;
 		}
 		
 		try 
 		{
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
 
 			String inputLine = br.readLine();
 			
 			if (inputLine == null) 
 			{
 				return retVal;
 			}
 			
 			inputLine = inputLine.replace(',', '.');
 			return Long.parseLong(inputLine);
 		}
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		catch (java.lang.NumberFormatException e)
 		{
 			return retVal;
 		}
 
 		return retVal;
 	}
 	
 	private Boolean getWait(Player player)
 	{
 		Long time = plugin.getServer().getWorlds().get(0).getTime() - getAttemptTime(player);
 		if(time >= plugin.attemptWait)
 		{
 			setAttemptTime(player);
 			return true;
 		}
    		else
    		{
    			player.sendMessage("Wait " + ((Long)((plugin.attemptWait - time)/30)).toString() + "s before trying again");
 			return false;
    		}
 	}
 
 	
 	//Inventory store/switcher
 	private File playerInv(Player player, World world)
 	{
 		File invFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Inventories");
 		if (!invFolder.exists()) 
 		{
 			invFolder.mkdir();
 		}
 		return new File(invFolder + File.separator + player.getName() + "." + world.getName());
 	}
 	
 	private void savePlayerInv(Player player, World world)
 	{
 		BufferedWriter bw;
 		try 
 		{
 			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(playerInv(player, world))));
 			
 			ItemStack [] inv =player.getInventory().getContents();
 			
 			for(int i = 0; i<inv.length; i++)
 			{
 				ItemStack item = inv[i];
 				String temp = "Empty";
 				try
 				{
 					temp = i + " " + item.getTypeId() + " " + item.getAmount() + " "+ item.getDurability();
 					bw.write(temp);
 					bw.newLine();
 				}
 				catch (java.lang.NullPointerException e)
 				{
 					//log.info("Exception");
 				}
 			}
 			bw.close();
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	private void stringToInv(Player player, List<String> inv)
 	{
 		player.getInventory().clear();
 		
 		for(String item : inv)
 		{
 			try
 			{
 				String [] split = item.split(" ", 4);
 				
 				int spot = Integer.parseInt(split[0]);
 				int itemId = Integer.parseInt(split[1]);
 				int ammount = Integer.parseInt(split[2]);
 				short damage = (short)Integer.parseInt(split[3]);
 				
 				player.getInventory().setItem(spot, new ItemStack(itemId, ammount, damage));
 
 			}
 			catch (java.lang.NumberFormatException e)
 			{
 				player.sendMessage("There was an issue loading your inventory");
 			}
 		}
 		//only way to get inv to update 
 		//TODO Do this properly!
 		player.updateInventory();
 	}
 
 	@SuppressWarnings("deprecation")
 	private void loadPlayerInv(Player player, World world)
 	{
 		
 		File save = playerInv(player, world);
 		try 
 		{
 			if (!save.exists()) 
 			{
 				if(plugin.kit && world.getName() == plugin.dreamWorld().getName())
 				{
 					save = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "kit.txt");
 					if (!save.exists()) 
 					{
 						return;
 					}
 					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
 					
 					List<String> inv = new ArrayList<String>();
 					String inputLine = br.readLine();
 					
 					int count =0;
 					while (inputLine != null)
 					{
 						inv.add(count + " " + inputLine + " 0");
 						inputLine = br.readLine();
 						count++;
 					}
 					
 					stringToInv(player, inv);
 					return;
 				}
 				else
 				{
 					if(plugin.seperateInvInitial)
 					{
 						player.getInventory().clear();
 						//only way to get inv to update 
 						//TODO Do this properly!
 						player.updateInventory();
 					}
 					return;
 				}
 			}
 		
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
 			
 			List<String> inv = new ArrayList<String>();
 			String inputLine = br.readLine();
 			
 			while (inputLine != null)
 			{
 				inv.add(inputLine);
 				inputLine = br.readLine();
 			}
 			
 			stringToInv(player, inv);
 			return;
 		}
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		catch (java.lang.NumberFormatException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	
 	//used to tp 2 extra times (accounts for chunk loading time)
 	private File spawnFile(Player player)
 	{
 		File lockFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Spawning");
 		if (!lockFolder.exists()) 
 		{
 			lockFolder.mkdir();
 		}
 		return new File(lockFolder + File.separator + player.getName());
 	}
 	
 	private Boolean playerSpawn(Player player)
 	{
 		File file1 = new File(spawnFile(player) + ".1");
 		File file2 = new File(spawnFile(player) + ".2");
 		if((file2).exists())
 		{
 			file2.delete();
 			return false;
 		}
 		if((file1).exists())
 		{
 			file1.delete();
 			return true;
 		}
 		return false;
 	}
 	
 	private void playerSetSpawn(Player player)
 	{
 		File file1 = new File(spawnFile(player) + ".1");
 		File file2 = new File(spawnFile(player) + ".2");
 		try
 		{
 			file1.createNewFile();
 			file2.createNewFile();
 		}
 		catch (IOException e)
 		{
 			e.toString();
 		}
 	}
 
 	
 	//saves bed locations of players
 	private Location loadLocation(Player player) 
 	{
 		File save = getBedFile(player);
 		if (!save.exists()) 
 		{
 			return null;
 		}
 		
 		try 
 		{
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
 			
 			String world = br.readLine();
 			String inputLine = br.readLine();
 			
 			if (inputLine == null || world == null) 
 			{
 				return null;
 			}
 			
 			inputLine = inputLine.replace(',', '.');
 			
 			String splits[] = inputLine.split(" ", 3);
 			return new Location(plugin.getServer().getWorld(world), Double.parseDouble(splits[0]), Double.parseDouble(splits[1]), Double.parseDouble(splits[2]));
 		}
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		catch (java.lang.NumberFormatException e)
 		{
 			return null;
 		}
 
 		return null;
 	}
 	
 	public void saveLocation(Player player, Location location) 
 	{
 		BufferedWriter bw;
 		try 
 		{
 			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getBedFile(player))));
 			bw.write(player.getWorld().getName());
 			bw.newLine();
 			bw.write(String.format("%f %f %f", location.getX(), location.getY(), location.getZ()));
 			bw.close();
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private File getBedFile(Player player)
 	{
 		File bedFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "BedLocations");
 		if (!bedFolder.exists()) 
 		{
 			bedFolder.mkdir();
 		}
 		return new File(bedFolder + File.separator + player.getName());
 	}
 
 
 	//used to manage lock file to prevent concurrent modification exception
 	private File lockFile(Player player)
 	{
 		File lockFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Lock");
 		if (!lockFolder.exists()) 
 		{
 			lockFolder.mkdir();
 		}
 		return new File(lockFolder + File.separator + player.getName() + ".lock");
 	}
 	
 	private void createLock(Player player)
 	{
 		try
 		{
 			lockFile(player).createNewFile();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private Boolean getLock(Player player)
 	{
 		return lockFile(player).exists();
 	}
 	
 	private void removeLock(Player player)
 	{
 		lockFile(player).delete();
 	}
 
 	
 	//used to save the spawn location of a skylands world
 	private File spawnWorldFile(World world)
 	{
 		File spawnFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "WorldSpawn");
 		if (!spawnFolder.exists()) 
 		{
 			spawnFolder.mkdir();
 		}
 		return new File(spawnFolder + File.separator + world.getName() + ".spawn");
 	}
 	
 	private Location getSpawn(World world) 
 	{
 		File save = spawnWorldFile(world);
 		if (!save.exists()) 
 		{
 			return null;
 		}
 		
 		try 
 		{
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
 			String inputLine = br.readLine();
 			if (inputLine == null) 
 			{
 				return null;
 			}
 			inputLine = inputLine.replace(',', '.');
 			String splits[] = inputLine.split(" ", 3);
 			return new Location(world, Double.parseDouble(splits[0]), Double.parseDouble(splits[1]), Double.parseDouble(splits[2]));
 		}
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		catch (java.lang.NumberFormatException e)
 		{
 			return null;
 		}
 
 		return null;
 	}
 
 	public void saveSpawn(Player player)
 	{
 		if(plugin.checkpermissions(player,"dreamland.setdreamspawn",true) && playerDreaming(player) && player.getLocation().getY() > 0)
  		{
 			Location location = player.getLocation();
 			//location.setY(location.getY() + 1);
 			File save = spawnWorldFile(player.getWorld());
 			BufferedWriter bw;
 			try {
 				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(save)));
 				bw.write(String.format("%f %f %f", location.getX(), location.getY(), location.getZ()));
 				bw.close();
 			}
 			catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 			catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public Location checkBedSpawnLoc(Location location)
 	{
 		Block block = location.getBlock();
 		Block blockCheck = null;
 		double spawnoffset = 0.5;
 		
 		if (block.getRelative(BlockFace.NORTH).getType() == Material.AIR) 
 		{
 			blockCheck = block.getRelative(BlockFace.NORTH);
 			Location blockCheckLoc = blockCheck.getLocation();
 			
 			if (blockCheck.getRelative(BlockFace.UP).getType() == Material.AIR) 
 			{
 				blockCheckLoc.setX(blockCheckLoc.getX()-spawnoffset);
 				
 				return blockCheckLoc;
 			}
 		}
 		else if (block.getRelative(BlockFace.EAST).getType() == Material.AIR) 
 		{
 			blockCheck = block.getRelative(BlockFace.EAST);
 			Location blockCheckLoc = blockCheck.getLocation();
 			
 			if (blockCheck.getRelative(BlockFace.UP).getType() == Material.AIR) 
 			{
 				blockCheckLoc.setZ(blockCheckLoc.getZ()-spawnoffset);
 				
 				return blockCheckLoc;
 			}
 		}
 		else if (block.getRelative(BlockFace.SOUTH).getType() == Material.AIR) 
 		{
 			blockCheck = block.getRelative(BlockFace.SOUTH);
 			Location blockCheckLoc = blockCheck.getLocation();
 			
 			if (blockCheck.getRelative(BlockFace.UP).getType() == Material.AIR) 
 			{
 				blockCheckLoc.setX(blockCheckLoc.getX()+spawnoffset);
 				
 				return blockCheckLoc;
 			}
 		}
 		else if (block.getRelative(BlockFace.WEST).getType() == Material.AIR) 
 		{
 			blockCheck = block.getRelative(BlockFace.WEST);
 			Location blockCheckLoc = blockCheck.getLocation();
 			
 			if (blockCheck.getRelative(BlockFace.UP).getType() == Material.AIR) 
 			{
 				blockCheckLoc.setZ(blockCheckLoc.getZ()+spawnoffset);
 				
 				return blockCheckLoc;
 			}
 		}
 		
 		// Return default weird spawn
 		location.setY(location.getY()+1.5);
 		
 		return location;
 	}
 }
