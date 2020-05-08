 package net.dmulloy2.swornparkour;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import net.dmulloy2.swornparkour.parkour.objects.EnchantmentType;
 import net.dmulloy2.swornparkour.parkour.objects.ParkourField;
 import net.dmulloy2.swornparkour.parkour.objects.ParkourReward;
 import net.dmulloy2.swornparkour.parkour.objects.ParkourSign;
 import net.dmulloy2.swornparkour.parkour.objects.ParkourZone;
 import net.dmulloy2.swornparkour.util.FormatUtil;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 
 /**
  * @author dmulloy2
  */
 
 public class FileHelper 
 {
 	public SwornParkour plugin;
 	public FileHelper(SwornParkour plugin)
 	{
 		this.plugin = plugin;
 	}
 	
 	public void save(ParkourZone pz)
 	{
 		try
 		{
 			File folder = new File(plugin.getDataFolder(), "games");
 			File file = new File(folder, pz.getId() + ".dat");
 			
 			if (file.exists()) file.delete();
 			
 			file.createNewFile();
 			
 			FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
 			fc.set("id", pz.getId());
 
 			ParkourField field = pz.getField();
 			fc.set("world", field.getWorld().getName());
 			
 			fc.set("minx", field.getMinx());
 			fc.set("miny", field.getMiny());
 			fc.set("minz", field.getMinz());
 			
 			fc.set("maxx", field.getMaxx());
 			fc.set("maxy", field.getMaxy());
 			fc.set("maxz", field.getMaxz());
 			
 			Location spawn = pz.getSpawn();
 			String spath = "spawn.";
 			fc.set(spath + "x", spawn.getBlockX());
 			fc.set(spath + "y", spawn.getBlockY());
 			fc.set(spath + "z", spawn.getBlockZ());
 			
 			Location check1 = pz.getCheckpoint1();
 			String cpath1 = "check1.";
 			fc.set(cpath1 + "x", check1.getBlockX());
 			fc.set(cpath1 + "y", check1.getBlockY());
 			fc.set(cpath1 + "z", check1.getBlockZ());
 			
 			Location check2 = pz.getCheckpoint2();
 			String cpath2 = "check2.";
 			fc.set(cpath2 + "x", check2.getBlockX());
 			fc.set(cpath2 + "y", check2.getBlockY());
 			fc.set(cpath2 + "z", check2.getBlockZ());
 			
 			Location end = pz.getEnd();
 			String epath = "end.";
 			fc.set(epath + "x", end.getBlockX());
 			fc.set(epath + "y", end.getBlockY());
 			fc.set(epath + "z", end.getBlockZ());
 			
 			fc.set("timesPlayed", pz.getTimesPlayed());
 			
 			fc.save(file);
 		}
 		catch (Exception e)
 		{
 			plugin.outConsole(Level.SEVERE, "Error saving file \"{0}\": {1}", pz.getId() + ".dat", e.getMessage());
 		}
 	}
 	
 	public void load(File file)
 	{
 		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
 		int id = fc.getInt("id");
 		
 		ParkourZone pz = new ParkourZone(plugin, id);
 		
 		World world = plugin.getServer().getWorld(fc.getString("world"));
 		Location min = new Location(world, fc.getInt("minx"), fc.getInt("miny"), fc.getInt("minz"));
 		Location max = new Location(world, fc.getInt("maxx"), fc.getInt("maxy"), fc.getInt("maxz"));
 		
 		ParkourField field = new ParkourField(min, max, world);
 		pz.setField(field);
 		
 		Location spawn = new Location(world, fc.getInt("spawn.x"), fc.getInt("spawn.y"), fc.getInt("spawn.z"));
 		pz.setSpawn(spawn);
 		
 		Location check1 = new Location(world, fc.getInt("check1.x"),fc.getInt("check1.y"), fc.getInt("check1.z"));
 		pz.setCheckpoint1(check1);
 		
 		Location check2 = new Location(world, fc.getInt("check2.x"),fc.getInt("check2.y"), fc.getInt("check2.z"));
 		pz.setCheckpoint2(check2);
 		
 		Location end = new Location(world, fc.getInt("end.x"), fc.getInt("end.y"), fc.getInt("end.z"));
 		pz.setEnd(end);
 		
 		pz.setTimesPlayed(fc.getInt("timesPlayed"));
 		
 		plugin.loadedArenas.add(pz);
 		
 		plugin.outConsole("Loaded game number {0}!", id);
 	}
 
 	public void readReward(int pointValue, String line)
 	{
 		boolean readEnchants = line.contains("enchants:");
 		boolean readName = line.contains("name:");
 		boolean readLore = line.contains("lore:");
 		
 		int itemId = 0;
 		byte dat = 0;
 		int amt = 0;
 		
 		String[] split = line.split(" ");
 		
 		String idSection = split[0];
 		if (idSection.contains(":"))
 		{
 			itemId = Integer.parseInt(idSection.split(":")[0]);
 			dat = (byte) Integer.parseInt(idSection.split(":")[1]);
 		}
 		else
 		{
 			itemId = Integer.parseInt(idSection);
 		}
 		
 		amt = Integer.parseInt(split[1]);
 		
 		Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
 		if (readEnchants)
 		{
 			String str = line.substring(line.indexOf("enchants:") + 9);
 			if (str.contains(","))
 			{
 				String[] split2 = str.split(",");
 				for (String s : split2)
 				{
 					String[] split3 = s.split(":");
 					Enchantment enchant = EnchantmentType.toEnchantment(split3[0]);
 					
 					int level = 0;
 					if (split3[1].contains(" name"))
 					{
 						String[] split4 = split3[1].split(" ");
 						level = Integer.parseInt(split4[0]);
 						
 					}
 					else
 					{
 						level = Integer.parseInt(split3[1]);
 					}
 					
 					if (enchant != null && level > 0)
 					{
 						enchantments.put(enchant, level);
 					}
 				}
 			}
 			else
 			{
 				String[] split2 = str.split(":");
 				Enchantment enchant = EnchantmentType.toEnchantment(split2[0]);
 				int level = Integer.parseInt(split2[1]);
 				
 				enchantments.put(enchant, level);
 			}
 		}
 		
 		String displayName = ""; 
 		if (readName)
 		{
 			String str = line.substring(line.indexOf("name:") + 5);
 			str = FormatUtil.format(str);
 			displayName = str.replaceAll("_", " ");
 		}
 		
 		List<String> lore = new ArrayList<String>();
 		if (readLore)
 		{
 			String str = line.substring(line.indexOf("lore:") + 5);
 			if (str.contains(","))
 			{
 				String[] split2 = str.split(",");
 				for (String s : split2)
 				{
 					lore.add(FormatUtil.format(s));
 				}
 			}
 			else
 			{
 				lore.add(FormatUtil.format(str));
 			}
 		}
 		
 		ParkourReward reward = new ParkourReward(itemId, dat, amt, enchantments, displayName, lore);
 		plugin.parkourRewards.put(pointValue, reward);
 	}
 	
 	public boolean deleteFile(int gameId)
 	{
 		File folder = new File(plugin.getDataFolder(), "games");
 		File file = new File(folder, gameId + ".dat");
 		
 		try
 		{
 			file.delete();
 		}
 		catch (Exception e)
 		{
 			plugin.outConsole(Level.SEVERE, "Error deleting file \"{0}\":", gameId + ".dat");
 			e.printStackTrace();
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public void updateSignSave()
 	{
 		File file = new File(plugin.getDataFolder(), "signs.yml");
 		if (file.exists())
 		{
 			file.delete();
 		}
 		
 		try 
 		{ 
 			file.createNewFile();
 		}
 		catch (Exception e) 
 		{
 			plugin.outConsole(Level.SEVERE, "Could not update sign save: {0}", e.getMessage());
 			return;
 		}
 		
 		YamlConfiguration fc = YamlConfiguration.loadConfiguration(file);
 		
 		for (ParkourSign sign : plugin.signs)
 		{
			String path = "signs." + sign.getId() + ".";
 
 			Location loc = sign.getLocation();
 			
 			fc.set(path + "gameId", sign.getZone().getId());
 			
 			fc.set(path + "world", loc.getWorld().getName());
 			fc.set(path + "x", loc.getBlockX());
 			fc.set(path + "y", loc.getBlockY());
 			fc.set(path + "z", loc.getBlockZ());
 		}
 		
 		fc.set("total", plugin.signs.size());
 		
 		try 
 		{ 
 			fc.save(file);
 		}
 		catch (Exception e) 
 		{
 			plugin.outConsole(Level.SEVERE, "Could not update sign save: {0}", e.getMessage());
 			return;
 		}
 	}
 	
 	public void loadSigns()
 	{
 		File file = new File(plugin.getDataFolder(), "signs.yml");
 		if (! file.exists()) return;
 		
 		YamlConfiguration fc = YamlConfiguration.loadConfiguration(file);
 		
 		for (int i = 0; i < fc.getInt("total"); i++)
 		{
			String path = "signs." + i + ".";
 			if (fc.isSet(path))
 			{
 				World world = plugin.getServer().getWorld(fc.getString(path + "world"));
 				if (world != null)
 				{
 					Location loc = new Location(world, fc.getInt(path + "x"), fc.getInt(path + "y"), fc.getInt(path + "z"));
 					if (loc != null)
 					{
 						ParkourZone zone = plugin.getParkourZone(fc.getInt(path + "gameId"));
 						if (zone != null)
 						{
 							ParkourSign sign = new ParkourSign(plugin, loc, zone, i);
 							plugin.signs.add(sign);
 						}
 					}
 				}
 			}
 		}
 	}
 }
