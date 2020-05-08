 package me.cmesh.DreamLand;
 
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Block;
 import java.io.OutputStreamWriter;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerBedEnterEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerLoginEvent;
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
 		if (playerDreaming(event.getPlayer()))
 		{
 			event.setCancelled(true);
 		}
 	}
   
 	public void onPlayerInteract(PlayerInteractEvent event)
 	{
 		if (plugin.dreamFly)
 		{
 			Player player = event.getPlayer();
 			if (plugin.anyoneCanGo || plugin.checkPermissions(player,"dreamland.fly",true))
 			{
 				if (playerInDreamLand(player))
 				{
 					if (plugin.flyTool.equals("-1") || plugin.flyTool.equals(Integer.toString(event.getPlayer().getItemInHand().getTypeId())))
 					{
 						if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
 						{
 							Vector dir = player.getLocation().getDirection().multiply(plugin.flySpeed);
 							dir.setY(dir.getY()+0.50);
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
 			if (event.getTo().getY() < 0)
 			{
 				leaveDream(player);
 				log.info(player.getName() + " woke up");
 				return;
 			}
 			if(respawn(player))
 			{
 				player.teleport(player.getWorld().getSpawnLocation());
 			}
 			if(playerInNightmare(player))
 			{
 				player.setFireTicks(3*30);
 			}
 			if(plugin.morningReturn)
 			{
 				long time = loadBed(player).getWorld().getTime();
 				if(time >=0 && time <= 12000)
 				{
 					player.sendMessage("It is morning, WAKEUP!");
 					log.info(player.getName() + " woke up");
 					leaveDream(player); 
 				}
 			}
 		}
 		else
 		{
 			if(respawn(player))
 			{
 				player.teleport(checkSpawnLoc(loadBed(player)));
 				if(plugin.seperateInv)
 				{
 					loadPlayerInv(player, player.getWorld());
 				}
 				log.info(player.getName() + " woke up");
 			}
 		}
 	}
 	
 	public void onPlayerBedEnter(PlayerBedEnterEvent event)
 	{
 		Player player = event.getPlayer();
 		if (plugin.anyoneCanGo || plugin.checkPermissions(player,"dreamland.goto",true))
 		{
 			if(!playerDreaming(player))
 			{
 				if ((plugin.attemptWait == 0 || getWait(player)) && new Random().nextInt(100) < plugin.dreamChance)
 				{
 					event.setCancelled(true);
 					
 					Boolean nightmare = (plugin.nightmareChance != 0) && new Random().nextInt(100) < plugin.nightmareChance;
 					
 					enterDream(player, event.getBed().getLocation(),nightmare);
 					plugin.Attempt.put(player.getName(), new Long(0));
 				}
 				else
 				{
 					if(getWait(player))
 					{
 						plugin.Attempt.put(player.getName(), plugin.getServer().getWorlds().get(0).getTime());
 					}
 				}
 			}
 		}
 	}
 	
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
 		Player player = event.getPlayer();
 		if (playerDreaming(player))
 		{
 			leaveDream(player);
 			log.info(player.getName() + " woke up");
 		}
 	}
 
 	public void onPlayerRespawn(PlayerRespawnEvent event)
 	{
 		Player player = event.getPlayer();
 		if (playerDreaming(player))
 		{
 			plugin.Respawn.put(player.getName(), 2);
 		}
 	}
 
 	public void onPlayerKick(PlayerKickEvent event)
 	{
 		//TODO make this only for when moving between worlds
 		if(event.getReason().contains("moved too quickly")) 
 		{
 			event.setCancelled(true);
 		}
 	}
 	
 	public void onPlayerLogin(PlayerLoginEvent event)
 	{
 		Player player = event.getPlayer();
 		if(!plugin.Attempt.containsKey(player.getName()))
 			plugin.Attempt.put(player.getName(), new Long(0));
 		if(!plugin.Respawn.containsKey(player.getName()))
 			plugin.Respawn.put(player.getName(), 0);
 	}
 
 	
 	//helper functions
 	public Boolean playerDreaming(Player player)
 	{
 		return playerInDreamLand(player) || (playerInNightmare(player) && plugin.nightmareChance != 0);
 	}
 	
 	private Boolean playerInDreamLand(Player player)
 	{
 		return player.getWorld().equals(plugin.dreamWorld());
 	}
 
 	private Boolean playerInNightmare(Player player)
 	{
 		return player.getWorld().equals(plugin.nightmareWorld());
 	}
 
 	private void enterDream(Player player, Location bed, Boolean nightmare)
 	{
 		saveBed(player, bed);
 		savePlayerHealth(player);
 		
 		Location loc = plugin.dreamWorld().getSpawnLocation(); 
 		if(nightmare)
 		{
 			loc = plugin.nightmareWorld().getSpawnLocation();
 		}
 		
 		loc = checkSpawnLoc(loc);
 		
 		if(plugin.seperateInv)
 		{
 			savePlayerInv(player, player.getWorld());
 		}
 		
 		player.teleport(loc);
 		
 		if(plugin.seperateInv)
 		{
 			loadPlayerInv(player, player.getWorld());
 		}
 		
 		plugin.Respawn.put(player.getName(), 2);
 		if(!plugin.message.isEmpty())
 		{
 			player.sendMessage(plugin.message);
 		}
 		log.info(player.getName() + " is dreaming");
 		return;
 	}
  
 	private void leaveDream(Player player)
 	{
 		player.setFireTicks(0);
 		Location loc = loadBed(player);
 		plugin.loadChunk(loc);
 
 		if(plugin.seperateInv)
 		{
 			savePlayerInv(player, player.getWorld());
 			loadPlayerInv(player, loc.getWorld());
 		}
 
 		player.setFallDistance(0);
 		player.teleport(checkSpawnLoc(loc));
 		player.setFallDistance(0);
 		
 		loadPlayerHealth(player);
 	}
 
 	private Boolean getWait(Player player)
 	{
 		Long time = plugin.getServer().getWorlds().get(0).getTime() - plugin.Attempt.get(player.getName());
 		if(time >= plugin.attemptWait)
 		{
 			plugin.Attempt.put(player.getName(), plugin.getServer().getWorlds().get(0).getTime());
 			return true;
 		}
    		else
    		{
    			player.sendMessage("Wait " + ((Long)((plugin.attemptWait - time)/30)).toString() + "s before trying again");
 			return false;
    		}
 	}
 
 	private Boolean respawn(Player player)
 	{
 		int value = plugin.Respawn.get(player.getName());
 		if(value > 0)
 		{
 			plugin.Respawn.put(player.getName(), value-1);
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
 			bw.write(((Integer)player.getHealth()).toString());
 			bw.close();
 		}
 		catch (FileNotFoundException e) {}
 		catch (IOException e) {}
 	}
 	
 	private void loadPlayerHealth(Player player)
 	{
 		File save = playerHealthFile(player);
 		if (save.exists()) 
 		{
 			try 
 			{
 				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
 				player.setHealth(Integer.parseInt(br.readLine()));
 				br.close();
 			}
 			catch (IOException e) {}
 			catch (java.lang.NumberFormatException e){}
 			catch (java.lang.IllegalArgumentException e){}
 		}
 		return;
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
 		if(world.equals(plugin.nightmareWorld()))
 		{
 			return;
 		}
 		
 		BufferedWriter bw;
 		try 
 		{
 			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(playerInv(player, world))));
 			
 			ItemStack [] inv =player.getInventory().getContents();
 			for(int i = 0; i<inv.length; i++)
 			{
 				ItemStack item = inv[i];
 				if(item != null)
 				{
 					String temp = i + " " + item.getTypeId() + " " + item.getAmount() + " "+ item.getDurability();
 					bw.write(temp);
 					bw.newLine();
 				}
 			}
 			bw.close();
 		}
 		catch (FileNotFoundException e) {}
 		catch (IOException e) {}
 	}
 
 	private void stringToInv(Player player, List<String> inv)
 	{
 		for(String item : inv)
 		{
 			String [] split = item.split(" ", 4);
 			int spot = Integer.parseInt(split[0]);
 			int itemId = Integer.parseInt(split[1]);
 			int ammount = Integer.parseInt(split[2]);
 			short damage = (short)Integer.parseInt(split[3]);
 			
 			player.getInventory().setItem(spot, new ItemStack(itemId, ammount, damage));
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	private void loadPlayerInv(Player player, World world)
 	{	
 
 		if(world.equals(plugin.nightmareWorld()))
 		{
 			player.getInventory().clear();
 			player.updateInventory();
 			return;
 		}
 		
 		File save = playerInv(player, world);
 		try 
 		{
 			if (!save.exists()) 
 			{
 				if(playerInDreamLand(player) && plugin.kit.size() != 0)
 				{
 					player.getInventory().clear();
 					stringToInv(player, plugin.kit);
 					player.updateInventory();
 					return;
 				}
 				if(plugin.seperateInvInitial)
 				{
 					player.getInventory().clear();
 					player.updateInventory();
 				}
 				return;
 			}
 			player.getInventory().clear();
 			
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
 			
 			List<String> inv = new ArrayList<String>();
 			String inputLine = br.readLine();
 			
 			while (inputLine != null)
 			{
 				inv.add(inputLine);
 				inputLine = br.readLine();
 			}
 			stringToInv(player, inv);
 			
 			player.updateInventory();
 			return;
 		}
 		catch (IOException e){}
 		catch (java.lang.NumberFormatException e){}
 		player.sendMessage("There was an issue loading your inventory");
 	}
 	
 	//saves bed locations of players
 	private Location loadBed(Player player) 
 	{
 		if(plugin.Beds.containsKey(player.getName()))
 		{
 			return plugin.Beds.get(player.getName());
 		}
 		File save = getBedFile(player);
 		if (save.exists()) 
 		{
 			try 
 			{
 				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
 				
 				String world = br.readLine();
 				String inputLine = br.readLine();
 				
 				if (inputLine != null && world != null) 
 				{
 					String splits[] = inputLine.replace(',', '.').split(" ", 3);
 					return new Location(plugin.getServer().getWorld(world), Double.parseDouble(splits[0]), Double.parseDouble(splits[1]), Double.parseDouble(splits[2]));
 				}
 			}
 			catch (IOException e) {log.info("There was an issue loading a player's bed location");}
 			catch (java.lang.NumberFormatException e){log.info("There was an loading saving a player's bed location");}
 		}
                // This now returns null, but needs to return the default world spawn location.
		return null;
 	}
 	
 	public void saveBed(Player player, Location location) 
 	{
 		BufferedWriter bw;
 		try 
 		{
 			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getBedFile(player))));
 			bw.write(player.getWorld().getName());
 			bw.newLine();
 			bw.write(String.format("%f %f %f", location.getX(), location.getY(), location.getZ()));
 			bw.close();
 			plugin.Beds.put(player.getName(), location);
 			return;
 		}
 		catch (FileNotFoundException e){}
 		catch (IOException e){}
 		log.info("There was an issue saving a player's bed location");
 		
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
 
 	
 	public Location checkSpawnLoc(Location location)
 	{
 		Block block = location.getBlock();
 		Block blockCheck = null;
 		double spawnoffset = 0.0;
 		
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
