 package net.dmulloy2.swornparkour;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.material.MaterialData;
 
 import net.dmulloy2.swornparkour.parkour.ParkourGame;
 import net.dmulloy2.swornparkour.parkour.objects.CompositeEnchantment;
 import net.dmulloy2.swornparkour.parkour.objects.EnchantmentType;
 import net.dmulloy2.swornparkour.parkour.objects.ParkourField;
 import net.dmulloy2.swornparkour.parkour.objects.ParkourPlayer;
 import net.dmulloy2.swornparkour.parkour.objects.ParkourReward;
 import net.dmulloy2.swornparkour.parkour.objects.ParkourZone;
 import net.dmulloy2.swornparkour.parkour.objects.SavedParkourPlayer;
 import net.dmulloy2.swornparkour.util.FormatUtil;
 
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
 
 	public void savePlayer(ParkourPlayer player, boolean shutdown)
 	{
 		try
 		{
 			ParkourGame game = plugin.getParkourManager().getParkourGame(player);
 			
 			File folder = new File(plugin.getDataFolder(), "players");
 			if (!folder.exists()) folder.createNewFile();
 			
 			File file = new File(folder, player.getPlayer().getName() + ".dat");
 			if (file.exists()) file.delete();
 			file.createNewFile();
 			
 			FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
 			fc.set("name", player.getPlayer().getName());
 			
 			Location spawnBack = player.getSpawnBack();
 			fc.set("locx", spawnBack.getBlockX());
 			fc.set("locy", spawnBack.getBlockY());
 			fc.set("locz", spawnBack.getBlockZ());
 			
 			fc.set("world", spawnBack.getWorld().getName());
 			
 			int i=0;
 			for (ItemStack stack : game.getSavedInventory())
 			{
 				String path = "item." + i + ".";
 				fc.set(path + "type", stack.getTypeId());
 				fc.set(path + "amt", stack.getAmount());
 				fc.set(path + "dat", stack.getData().getData());
 				
 				Map<Enchantment, Integer> enchantments = stack.getEnchantments();
 				boolean hasEnchants = enchantments.size() > 0;
 				int enchantsAmt = enchantments.size();
 				
 				fc.set(path + "hasEnchants", hasEnchants);
 				fc.set(path + "enchantsAmt", enchantsAmt);
 				
 				
 				int ii=0;
 				for (Entry<Enchantment, Integer> entrySet : enchantments.entrySet())
 				{
 					String epath = path + "enchantments." + ii + ".";
 					fc.set(epath + "name", entrySet.getKey().getName());
 					fc.set(epath + "level", entrySet.getValue());
 					ii++; 
 				}
 				
 				ItemMeta meta = stack.getItemMeta();
 				fc.set(path + "hasName", meta.hasDisplayName());
 				if (meta.hasDisplayName())
 				{
 					fc.set(path + "name", meta.getDisplayName());
 				}
 				
 				fc.set("hasLore", meta.hasLore());
 				if (meta.hasLore())
 				{
 					int iii = 0;
 					for (String lore : meta.getLore())
 					{
 						fc.set(path + "lore." + iii, lore);
 						iii++;
 					}
 					fc.set("loreamt", iii);
 				}
 				i++;
 			}
 			fc.set("itemAmt", i);
 			
 			int a=0;
			for (ItemStack stack : game.getSavedArmor())
 			{
 				String path = "item." + a + ".";
 				fc.set(path + "type", stack.getTypeId());
 				fc.set(path + "amt", stack.getAmount());
 				fc.set(path + "dat", stack.getData().getData());
 				
 				Map<Enchantment, Integer> enchantments = stack.getEnchantments();
 				boolean hasEnchants = enchantments.size() > 0;
 				int enchantsAmt = enchantments.size();
 				
 				fc.set(path + "hasEnchants", hasEnchants);
 				fc.set(path + "enchantsAmt", enchantsAmt);
 				
 				
 				int aa=0;
 				for (Entry<Enchantment, Integer> entrySet : enchantments.entrySet())
 				{
 					String epath = path + "enchantments." + aa + ".";
 					fc.set(epath + "name", entrySet.getKey().getName());
 					fc.set(epath + "level", entrySet.getValue());
 					aa++; 
 				}
 				
 				ItemMeta meta = stack.getItemMeta();
 				fc.set(path + "hasName", meta.hasDisplayName());
 				if (meta.hasDisplayName())
 				{
 					fc.set(path + "name", meta.getDisplayName());
 				}
 				
 				fc.set("hasLore", meta.hasLore());
 				if (meta.hasLore())
 				{
 					int aaa=0;
 					for (String lore : meta.getLore())
 					{
 						fc.set(path + "lore." + aaa, lore);
 						aaa++;
 					}
 					fc.set("loreamt", aaa);
 				}
 				a++;
 			}
 			fc.set("armorAmt", a);
 			
 			fc.save(file);
 			
 			if (!shutdown)
 			{
 				plugin.savedPlayers.add(loadSavedPlayer(player.getPlayer().getName()));
 			}
 		}
 		catch (Exception e)
 		{
 			plugin.outConsole("Error saving player \"{0}\": {1}", player.getPlayer().getName(), e.getMessage());
 		}
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
 		
 		List<CompositeEnchantment> enchantments = new ArrayList<CompositeEnchantment>();
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
 						CompositeEnchantment enchantment = new CompositeEnchantment(enchant, level);
 						enchantments.add(enchantment);
 					}
 				}
 			}
 			else
 			{
 				String[] split2 = str.split(":");
 				Enchantment enchant = EnchantmentType.toEnchantment(split2[0]);
 				int level = Integer.parseInt(split2[1]);
 				
 				CompositeEnchantment enchantment = new CompositeEnchantment(enchant, level);
 				enchantments.add(enchantment);
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
 	
 	public List<SavedParkourPlayer> loadSavedPlayers()
 	{
 		List<SavedParkourPlayer> savedPlayers = new ArrayList<SavedParkourPlayer>();
 		File folder = new File(plugin.getDataFolder(), "players");
 		
 		File[] children = folder.listFiles();
 		for (File file : children)
 		{
 			YamlConfiguration fc = YamlConfiguration.loadConfiguration(file);
 			String name = fc.getString("name");
 			
 			World world = plugin.getServer().getWorld(fc.getString("world"));
 			Location spawnback = new Location(world, fc.getInt("locx"), fc.getInt("locy"),fc.getInt("locz"));
 			
 			SavedParkourPlayer savedPlayer = new SavedParkourPlayer(name, spawnback);
 
 			for (int i=0; i<fc.getInt("itemAmt"); i++)
 			{
 				String path = "item." + i + ".";
 				int id = fc.getInt(path + "type");
 				int amt = fc.getInt(path + "amt");
 				int dat = fc.getInt(path + "dat");
 
 				ItemStack stack = new ItemStack(id, amt);
 				if (dat > 0)
 				{
 					MaterialData data = stack.getData();
 					data.setData((byte)dat);
 					stack.setData(data);
 				}
 				
 				if (fc.getBoolean(path + "hasEnchants"))
 				{
 					int enchantsAmt = fc.getInt(path + "enchantsAmt");
 					for (int e=0; e<enchantsAmt; e++)
 					{
 						String epath = path + "enchantment" + e;
 						Enchantment enchantment = Enchantment.getByName(fc.getString(epath + "name"));
 						int level = fc.getInt(epath + "level");
 						
 						stack.addUnsafeEnchantment(enchantment, level);
 					}
 				}
 				
 				ItemMeta meta = stack.getItemMeta();
 				if (fc.getBoolean(path + "hasName"))
 				{
 					meta.setDisplayName(fc.getString(path + "name"));
 				}
 				
 				if (fc.getBoolean(path + "hasLore"))
 				{
 					List<String> lore = new ArrayList<String>();
 					
 					int loreAmt = fc.getInt(path + "loreAmt");
 					for (int m=0; m<loreAmt; m++)
 					{
 						String mpath = path + "lore." + m;
 						lore.add(fc.getString(mpath));
 					}
 					
 					meta.setLore(lore);
 				}
 				
 				stack.setItemMeta(meta);
 
 				savedPlayer.addItem(stack);
 			}
 			
 			for (int i=0; i<fc.getInt("armorAmt"); i++)
 			{
 				String path = "armor" + i;
 				int id = fc.getInt(path + "type");
 				int amt = fc.getInt(path + "amt");
 				int dat = fc.getInt(path + "dat");
 				
 				ItemStack stack = new ItemStack(id, amt);
 				if (dat > 0)
 				{
 					MaterialData data = stack.getData();
 					data.setData((byte)dat);
 					stack.setData(data);
 				}
 				
 				if (fc.getBoolean(path + "hasEnchants"))
 				{
 					int enchantsAmt = fc.getInt(path + "enchantsAmt");
 					for (int e=0; e<enchantsAmt; e++)
 					{
 						String epath = path + "enchantment" + e;
 						Enchantment enchantment = EnchantmentType.toEnchantment(fc.getString(epath + "name"));
 						int level = fc.getInt(epath + "level");
 						
 						stack.addUnsafeEnchantment(enchantment, level);
 					}
 				}
 				
 				ItemMeta meta = stack.getItemMeta();
 				if (fc.getBoolean(path + "hasName"))
 				{
 					meta.setDisplayName(fc.getString(path + "name"));
 				}
 				
 				if (fc.getBoolean(path + "hasLore"))
 				{
 					List<String> lore = new ArrayList<String>();
 					
 					int loreAmt = fc.getInt(path + "loreAmt");
 					for (int m=0; m<loreAmt; m++)
 					{
 						String mpath = path + "lore." + m;
 						lore.add(fc.getString(mpath));
 					}
 					
 					meta.setLore(lore);
 				}
 				
 				stack.setItemMeta(meta);
 				
 				savedPlayer.addArmor(stack);
 			}
 
 			savedPlayers.add(savedPlayer);
 		}
 		
 		plugin.outConsole("Loaded {0} player files!", children.length);
 		
 		return savedPlayers;
 	}
 	
 	public SavedParkourPlayer loadSavedPlayer(String key)
 	{
 		File folder = new File(plugin.getDataFolder(), "players");
 		File file = new File(folder, key + ".dat");
 		
 		YamlConfiguration fc = YamlConfiguration.loadConfiguration(file);
 		String name = fc.getString("name");
 		
 		World world = plugin.getServer().getWorld(fc.getString("world"));
 		Location spawnback = new Location(world, fc.getInt("locx"), fc.getInt("locy"),fc.getInt("locz"));
 		
 		SavedParkourPlayer savedPlayer = new SavedParkourPlayer(name, spawnback);
 		
 		for (int i=0; i<fc.getInt("items"); i++)
 		{
 			String path = "item." + i + ".";
 			int id = fc.getInt(path + "type");
 			int amt = fc.getInt(path + "amt");
 			int dat = fc.getInt(path + "dat");
 			
 			ItemStack stack = new ItemStack(id, amt);
 			if (dat > 0)
 			{
 				MaterialData data = stack.getData();
 				data.setData((byte)dat);
 				stack.setData(data);
 			}
 			
 			if (fc.getBoolean(path + "hasEnchants"))
 			{
 				int enchantsAmt = fc.getInt(path + "enchantsAmt");
 				for (int e=0; e<enchantsAmt; e++)
 				{
 					String epath = path + "enchantment" + e;
 					Enchantment enchantment = EnchantmentType.toEnchantment(fc.getString(epath + "name"));
 					int level = fc.getInt(epath + "level");
 					
 					stack.addUnsafeEnchantment(enchantment, level);
 				}
 			}
 			
 			ItemMeta meta = stack.getItemMeta();
 			if (fc.getBoolean(path + "hasName"))
 			{
 				meta.setDisplayName(fc.getString(path + "name"));
 			}
 			
 			if (fc.getBoolean(path + "hasLore"))
 			{
 				List<String> lore = new ArrayList<String>();
 				
 				int loreAmt = fc.getInt(path + "loreAmt");
 				for (int m=0; m<loreAmt; m++)
 				{
 					String mpath = path + "lore." + m;
 					lore.add(fc.getString(mpath));
 				}
 				
 				meta.setLore(lore);
 			}
 			
 			stack.setItemMeta(meta);
 			
 			savedPlayer.addItem(stack);
 		}
 		
 		for (int i=0; i<fc.getInt("armor"); i++)
 		{
 			String path = "armor" + i;
 			int id = fc.getInt(path + "type");
 			int amt = fc.getInt(path + "amt");
 			int dat = fc.getInt(path + "dat");
 			
 			ItemStack stack = new ItemStack(id, amt);
 			if (dat > 0)
 			{
 				MaterialData data = stack.getData();
 				data.setData((byte)dat);
 				stack.setData(data);
 			}
 			
 			if (fc.getBoolean(path + "hasEnchants"))
 			{
 				int enchantsAmt = fc.getInt(path + "enchantsAmt");
 				for (int e=0; e<enchantsAmt; i++)
 				{
 					String epath = path + "enchantment" + e;
 					Enchantment enchantment = EnchantmentType.toEnchantment(fc.getString(epath + "name"));
 					int level = fc.getInt(epath + "level");
 					
 					stack.addUnsafeEnchantment(enchantment, level);
 				}
 			}
 			
 			ItemMeta meta = stack.getItemMeta();
 			if (fc.getBoolean(path + "hasName"))
 			{
 				meta.setDisplayName(fc.getString(path + "name"));
 			}
 			
 			if (fc.getBoolean(path + "hasLore"))
 			{
 				List<String> lore = new ArrayList<String>();
 				
 				int loreAmt = fc.getInt(path + "loreAmt");
 				for (int m=0; m<loreAmt; m++)
 				{
 					String mpath = path + "lore." + m;
 					lore.add(fc.getString(mpath));
 				}
 				
 				meta.setLore(lore);
 			}
 			
 			stack.setItemMeta(meta);
 			
 			savedPlayer.addArmor(stack);
 		}
 
 		return savedPlayer;
 	}
 	
 	public void deleteSavedPlayer(String name)
 	{
 		File folder = new File(plugin.getDataFolder(), "players");
 		File file = new File(folder, name + ".dat");
 		
 		if (!file.exists()) return;
 		
 		file.delete();
 	}
 }
