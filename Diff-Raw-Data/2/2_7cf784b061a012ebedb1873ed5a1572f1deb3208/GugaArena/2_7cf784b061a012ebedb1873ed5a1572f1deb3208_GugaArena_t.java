 package me.Guga.Guga_SERVER_MOD;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class GugaArena 
 {
 	GugaArena(Guga_SERVER_MOD gugaSM)
 	{
 		plugin = gugaSM;
 		this.spawnIndex = 0;
 	}
 	
 	public void ArenaKill(Player killer, Player victim)
 	{
 		if (victim != killCache)
 		{
 			GugaProfession prof;
 			if ((prof = plugin.professions.get(killer.getName())) != null)
 			{
 				prof.GainExperience(100);
 			}
 			Integer multiKill = multiKillCounter.get(killer.getName());
 			if (multiKill == null)
 				multiKillCounter.put(killer.getName(), 1);
 			else
 				multiKillCounter.put(killer.getName(), multiKill.intValue() + 1);
 			DisableLeave(killer,60);
 			IncreasePvpStats(killer, victim);
 			multiKillCounter.put(victim.getName(), 0);
 			SavePvpStats();
 		}
 	}
 	public void PlayerJoin(Player p)
 	{
 		if (!IsArena(p.getLocation()))
 		{
 			baseLocation.put(p.getName(), p.getLocation());
 		}
 		p.getServer().broadcastMessage(ChatColor.DARK_GREEN + "[ARENA]: " +p.getName() + " vstoupil do Areny!");
 		InventoryBackup.InventoryClearWrapped(p);
 		GiveItems(p);
 		if (this.actualSpawn != null)
 		{
 			p.teleport(actualSpawn.GetLocation());
 		}
 		else
 		{
 			p.teleport(plugin.getServer().getWorld("arena").getSpawnLocation());
 		}
 		if(playerImortality.containsKey(p.getName()))
 			playerImortality.remove(p.getName());
 		playerImortality.put(p.getName(), System.currentTimeMillis() + 3000);
 		DisableLeave(p, 60);
 	}
 	public void PlayerLeave(Player p)
 	{
 		if (cannotLeave.contains(p.getName()))
 		{
 			p.sendMessage("Na opusteni areny je prilis brzy!");
 			return;
 		}
 		if (!IsArena(p.getLocation()))
 		{
 			p.sendMessage("Tento prikaz jde pouzit pouze v arene!");
 			return;
 		}
 		if (baseLocation.get(p.getName()) != null)
 		{
 			p.teleport(baseLocation.get(p.getName()));
 			baseLocation.remove(p.getName());
 		}
 		else
 		{
 			p.teleport(plugin.getServer().getWorld("world").getSpawnLocation());
 		}
 		p.getServer().broadcastMessage(ChatColor.DARK_GREEN + "[ARENA]: " +p.getName() + " opustil Arenu");
 		InventoryBackup.InventoryReturnWrapped(p, true);
 	}
 	public boolean IsArena(Location loc)
 	{
 		if (loc.getWorld().getName().matches("arena"))
 			return true;
 		return false;
 	}
 	private void DisableLeave(Player p, int seconds)
 	{
 		final String pName = p.getName();
 		int serverTicks = seconds*20;
 		cannotLeave.add(pName);
 		plugin.scheduler.scheduleAsyncDelayedTask(plugin, new Runnable(){
 			public void run()
 			{
 				cannotLeave.remove(pName);
 			}
 		}, serverTicks);
 	}
 	public Location GetPlayerBaseLocation(Player p)
 	{
 		return baseLocation.get(p.getName());
 
 	}
 	public void SetPlayerBaseLocation(Player p, Location newLoc)
 	{
 		baseLocation.put(p.getName(), newLoc);
 	}
 	public void RemovePlayerBaseLocation(Player p)
 	{
 		baseLocation.remove(p.getName());
 	}
 	public int GetPlayerStats(Player p)
 	{
 		return this.pvpStats.get(p.getName());
 	}
 	public void IncreasePvpStats(Player p, Player victim)
 	{
 		String pName = p.getName();
 		Integer kills = pvpStats.get(pName);
 		int oldKills;
 		if (kills == null)
 		{
 			kills = 1;
 			oldKills = 0;
 		}
 		else
 		{
 			oldKills = kills.intValue();
 			kills++;
 		}
 		Integer victimKills;
 		if (( victimKills = pvpStats.get(victim.getName())) != null)
 		{
 			int victimTier = ArenaTier.GetTier(victimKills).intValue();
 			int killerTier = ArenaTier.GetTier(kills).intValue();
 			if (victimTier > killerTier)
 			{
 				kills += (victimTier - killerTier);
 			}
 		}
 		Integer multiKill;
 		if ( (multiKill = multiKillCounter.get(pName)) != null)
 		{
 			if (multiKill.intValue() > 3)
 			{
 				plugin.getServer().broadcastMessage(ChatColor.DARK_GREEN + "[ARENA]: " +pName + " je na killing spree! Ma " + multiKill + " killu v rade!");
 				kills++;
 			}
 		}
 		if ((multiKill = multiKillCounter.get(victim.getName())) != null)
 		{
 			if (multiKill.intValue() >= 3 )
 			{
 				plugin.getServer().broadcastMessage(ChatColor.DARK_GREEN + "[ARENA]: " + pName + " prerusil killing spree hrace " + victim.getName() + "!");
 				kills++;
 			}
 		}
 		ArenaTier tier = ArenaTier.GetTier(kills);
 		if (ArenaTier.GetTier(oldKills) != tier || tier == ArenaTier.RANK19)
 		{
 			if (tier == ArenaTier.RANK19)
 			{
 				WinRound(p);
 				return;
 			}
 			//plugin.getServer().broadcastMessage(ChatColor.DARK_GREEN + "[ARENA]: " + p.getName() + " byl povysen na " + tier.toString());
 			p.sendMessage(ChatColor.DARK_GREEN + "[ARENA]: Byl jste povysen na " + tier.toString());
 			GiveItems(p);
 		}
 		p.sendMessage(ChatColor.DARK_GREEN + "[ARENA]: " + "Zabil jsi " + victim.getName() + "!   +" + (kills.intValue() - oldKills) + " bodu");
 		pvpStats.put(pName, kills);
 	}
 	public void ShowPvpStats(Player sender)
 	{		
 		Iterator<Entry<String,Integer>> iterator;
 		iterator = pvpStats.entrySet().iterator();
 		ArrayList<String> values = new ArrayList<String>();
 		ArrayList <String> buffer = new ArrayList<String>();
 		while (iterator.hasNext())
 		{
 			String element;
 			String value;
 		
 			element = iterator.next().toString();
 			value = element.split("=")[1];
 			
 			buffer.add(element);
 			
 			values.add(value);
 		}
 		Object[] valuesArray = values.toArray(); 
 		int[] valuesInt = new int[valuesArray.length];
 		
 		int i = 0;
 		while (i < valuesInt.length)
 		{
 			valuesInt[i] = Integer.parseInt(valuesArray[i].toString());
 			i++;
 		}
 		Arrays.sort(valuesInt);
 		i = 0;
 		sender.sendMessage("******************************");
 		sender.sendMessage("    ARENA STATS (NAME - KILLS)");
 		while (i < valuesInt.length)
 		{
 			String pName = "";
 			Iterator<String> iter = buffer.iterator();
 			while (iter.hasNext())
 			{
 				String element = iter.next().toString();
 				String name = element.split("=")[0];
 				String value = element.split("=")[1];
 				if (value.matches(Integer.toString(valuesInt[i])))
 				{
 					pName = name;
 					buffer.remove(element);
 					break;
 				}
 			}
 			sender.sendMessage(pName+" - "+valuesInt[i]);
 			i++;
 		}
 	}
 	public void SaveArenas()
 	{
 		plugin.log.info("Saving Arena Data...");
 		GugaFile file = new GugaFile(GugaArena.arenaFile, GugaFile.WRITE_MODE);
 		file.Open();
 		Iterator<ArenaSpawn> i = this.spawnList.iterator();
 		while (i.hasNext())
 		{
 			ArenaSpawn spawn = i.next();
 			Location loc = spawn.GetLocation();
 			String line = spawn.GetName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
 			file.WriteLine(line);
 		}
 		file.Close();
 	}
 	public void LoadArenas()
 	{
 		plugin.log.info("Loading Arena Data...");
 		GugaFile file = new GugaFile(GugaArena.arenaFile, GugaFile.READ_MODE);
 		file.Open();
 		String line;
 		while ((line = file.ReadLine()) != null)
 		{
 			String[] split = line.split(";");
 			Location loc = new Location(this.plugin.getServer().getWorld("arena"), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]));
 			this.spawnList.add(new ArenaSpawn(split[0], loc));
 		}
 		file.Close();
 		if (spawnList.size() == 0)
 			this.spawnList.add(new ArenaSpawn("default", this.plugin.getServer().getWorld("arena").getSpawnLocation()));
 		this.actualSpawn = this.spawnList.get(0);
 	}
 	public void SavePvpStats()
 	{
 			plugin.log.info("Saving PvpStats Data...");
 			File arena = new File(statsFile);
 			if (!arena.exists())
 			{
 				try 
 				{
 					arena.createNewFile();
 					
 				} 
 				catch (IOException e) 
 				{
 					e.printStackTrace();
 				}
 			}
 			try 
 			{
 				FileWriter fStream = new FileWriter(arena, false);
 				BufferedWriter bWriter;
 				bWriter = new BufferedWriter(fStream);
 				String line;
 					
 				Iterator<Entry<String,Integer>> iterator;
 				iterator = pvpStats.entrySet().iterator();
 				ArrayList<String> values = new ArrayList<String>();
 				ArrayList <String> buffer = new ArrayList<String>();
 				while (iterator.hasNext())
 				{
 					String element;
 					String value;
 				
 					element = iterator.next().toString();
 					value = element.split("=")[1];
 					
 					buffer.add(element);
 					values.add(value);
 				}
 				Object[] valuesArray = values.toArray(); 
 				int[] valuesInt = new int[valuesArray.length];
 				
 				int i = 0;
 				while (i < valuesInt.length)
 				{
 					valuesInt[i] = Integer.parseInt(valuesArray[i].toString());
 					i++;
 				}
 				i = 0;
 				while (i<valuesInt.length)
 				{
 					Iterator<String> iter = buffer.iterator();
 					String pName = "";
 					while (iter.hasNext())
 					{
 						String element = iter.next().toString();
 						String name = element.split("=")[0];
 						String value = element.split("=")[1];
 						if (value.matches(Integer.toString(valuesInt[i])))
 						{
 							pName = name;
 							buffer.remove(element);
 							break;
 						}
 					}
 					line = pName + ";" + valuesInt[i];
 					
 					bWriter.write(line);
 					bWriter.newLine();
 					i++;
 				}
 				bWriter.close();
 				fStream.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	}
 	public void LoadPvpStats()
 	{
 		plugin.log.info("Loading PvpStats Data...");
 		File arena = new File(statsFile);
 		if (!arena.exists())
 		{
 			try 
 			{
 				arena.createNewFile();
 			} 
 			catch (IOException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 		else
 		{
 			try 
 			{
 				FileInputStream fRead = new FileInputStream(arena);
 				DataInputStream inStream = new DataInputStream(fRead);
 				BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));		
 				String line;
 				String[] splittedLine;
 				String name;
 				String kills;
 				try {
 					while ((line = bReader.readLine()) != null)
 					{
 						splittedLine = line.split(";");
 						name = splittedLine[0];
 						kills = splittedLine[1];
 						pvpStats.put(name, Integer.parseInt(kills));
 					}
 					bReader.close();
 					inStream.close();
 					fRead.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}			
 			} 
 			catch (FileNotFoundException e) 
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	public void GiveItems(Player p)
 	{
 		PlayerInventory inv = p.getInventory();
 		if (inv.getContents().length > 0)
 		{
 			inv.clear();
 			inv.setArmorContents(null);
 		}
 		Integer kills;
 		if ((kills = pvpStats.get(p.getName())) == null)
 			kills = 0;
 		ArenaTier tier = ArenaTier.GetTier(kills.intValue());
 		int[] armor = tier.GetArmor();
 		if (armor[0] != 0)
 			inv.setHelmet(new ItemStack(armor[0], 1));
 		if (armor[1] != 0)
 			inv.setChestplate(new ItemStack(armor[1], 1));
 		if (armor[2] != 0)
 			inv.setLeggings(new ItemStack(armor[2], 1));
 		if (armor[3] != 0)
 			inv.setBoots(new ItemStack(armor[3], 1));
 		
 		int i = 0;
 		int[][] inventory = tier.GetItems();
 		while (i < (inventory[0].length))
 		{
 			inv.addItem(new ItemStack(inventory[0][i], inventory[1][i]));
 			i++;
 		}
 	}
 	public void WinRound(Player winner)
 	{
 		plugin.getServer().broadcastMessage(ChatColor.DARK_GREEN + "[ARENA]: " + ChatColor.GOLD + winner.getName() + ChatColor.DARK_GREEN + " VYHRAVA TOTO KOLO A ZISKAVA 5 KREDITU!");
 		plugin.getServer().broadcastMessage(ChatColor.DARK_GREEN + "[ARENA]: ZACINA NOVE KOLO V ARENE!");
 		plugin.FindPlayerCurrency(winner.getName()).AddCurrency(5);
 		winner.sendMessage("Ziskal jste +5 kreditu!");
 		ClearStats();
 		this.RotateArena();
 		
 	}
 	public void ClearStats()
 	{
 		this.pvpStats.clear();
 		Object[] p = this.plugin.getServer().getWorld("arena").getPlayers().toArray();
 		int i = 0;
 		while (i < p.length)
 		{
 			if (p[i] != null)
 				GiveItems((Player)p[i]);
 			i++;
 		}
 		this.multiKillCounter.clear();
 	}
 	public void AddArena(String name, Location loc)
 	{
 		if (this.ContainsArena(name) || !this.IsArena(loc))
 			return;
 		this.spawnList.add(new ArenaSpawn(name, loc));
 		this.SaveArenas();
 	}
 	public ArrayList<ArenaSpawn> GetArenaList()
 	{
 		return this.spawnList;
 	}
 	public void RemoveArena(String name)
 	{
 		Iterator<ArenaSpawn> i = this.spawnList.iterator();
 		ArenaSpawn rem = null;
 		while (i.hasNext())
 		{
 			ArenaSpawn spawn = i.next();
 			if (spawn.GetName().equalsIgnoreCase(name))
 			{
 				rem = spawn;
 				break;
 			}
 		}
 		if (rem != null)
 			this.spawnList.remove(rem);
 		this.SaveArenas();
 	}
 	public void RotateArena()
 	{
 		this.spawnIndex++;
 		if ( !(this.spawnIndex < this.spawnList.size()) )
 			this.spawnIndex = 0;
 		this.actualSpawn = this.spawnList.get(this.spawnIndex);
 		Object[] p = this.plugin.getServer().getWorld("arena").getPlayers().toArray();
 		int i = 0;
 		while (i < p.length)
 		{
 			if (p != null)
 				((Player)p[i]).teleport(this.actualSpawn.GetLocation());
 			i++;
 		}
 	}
 	public boolean ContainsArena(String name)
 	{
 		Iterator<ArenaSpawn> i = this.spawnList.iterator();
 		while (i.hasNext())
 		{
 			if (i.next().GetName().equalsIgnoreCase(name))
 				return true;
 		}
 		return false;
 	}
 	
 	public boolean IsImortal(String pName)
 	{
		if(playerImortality.get(pName) == null)
			return false;
 		if(playerImortality.get(pName) > System.currentTimeMillis())
 			return true;
 		return false;
 	}
 	public enum ArenaTier
 	{
 		RANK1		(0, new int[][]{{268, 297}, {5, 10}}, new int[]{0, 0, 0, 0}), 
 		RANK2	(1, new int[][]{{268, 297}, {5, 10}}, new int[]{298, 0, 0, 0}), 
 		RANK3		(3, new int[][]{{268, 297}, {5, 10}}, new int[]{298, 0, 0, 301}), 
 		RANK4		(6, new int[][]{{268, 297}, {5, 10}}, new int[]{298, 0, 300, 301}), 
 		RANK5		(9, new int[][]{{268, 297}, {5, 10}}, new int[]{298, 299, 300, 301}),
 		RANK6		(12, new int[][]{{268, 297, 261, 262}, {5, 10, 1, 2}}, new int[]{298, 299, 300, 301}),
 		RANK7		(15, new int[][]{{272, 297, 261, 262}, {5, 10, 1, 5}}, new int[]{298, 299, 300, 301}),
 		RANK8		(18, new int[][]{{272, 297, 261, 262}, {5, 10, 1, 10}}, new int[]{314, 299, 300, 301}),
 		RANK9		(21, new int[][]{{272, 297, 261, 262}, {5, 10, 1, 15}}, new int[]{314, 299, 300, 317}),
 		RANK10		(24, new int[][]{{272, 297, 261, 262}, {5, 10, 1, 20}}, new int[]{314, 299, 316, 317}),
 		RANK11		(27, new int[][]{{272, 297, 261, 262}, {5, 10, 1, 25}}, new int[]{314, 315, 316, 317}),
 		RANK12		(30, new int[][]{{283, 297, 261, 262}, {5, 10, 1, 50}}, new int[]{314, 315, 316, 317}),
 		RANK13		(35, new int[][]{{283, 297, 261, 262}, {5, 10, 1, 50}}, new int[]{302, 315, 316, 317}),
 		RANK14		(40, new int[][]{{283, 297, 261, 262}, {5, 10, 1, 50}}, new int[]{302, 315, 316, 305}),
 		RANK15		(45, new int[][]{{283, 297, 261, 262}, {5, 10, 1, 50}}, new int[]{302, 315, 304, 305}),
 		RANK16		(50, new int[][]{{283, 297, 261, 262}, {5, 10, 1, 50}}, new int[]{302, 303, 304, 305}),
 		RANK17		(60, new int[][]{{267, 297, 261, 262}, {5, 10, 1, 50}}, new int[]{302, 303, 304, 305}),
 		RANK18		(70, new int[][]{{277, 297}, {5, 10}}, new int[]{0, 303, 304, 305}),
 		RANK19		(75, new int[][]{{267}, {1}}, new int[]{0, 0, 0, 0});
 	//	TIER20		(160, new int[][]{{267, 297, 261, 262}, {5, 10, 1, 50}}, new int[]{306, 303, 308, 309}),
 	//	TIER21		(180, new int[][]{{267, 297, 261, 262}, {5, 10, 1, 50}}, new int[]{306, 307, 308, 309});
 		
 		private ArenaTier(int killsRequired, int[][] items, int[] armor)
 		{
 			this.killsRequired = killsRequired;
 			this.items = items;
 			this.armor = armor;
 		}
 		public int GetKillsReq()
 		{
 			return this.killsRequired;
 		}
 		public int[][] GetItems()
 		{
 			return this.items;
 		}
 		public int[] GetArmor()
 		{
 			return this.armor;
 		}
 		public static ArenaTier GetTier(int kills)
 		{
 			ArenaTier[] vals = ArenaTier.values();
 			int i = vals.length - 1;
 			while (i >= 0)
 			{
 				if (kills >= vals[i].GetKillsReq())
 					return vals[i];
 				i--;
 			}
 			return null;
 		}
 		public int intValue()
 		{
 			int i = 0;
 			ArenaTier[] vals = ArenaTier.values();
 			while (i < vals.length)
 			{
 				if (vals[i] == this)
 					return i;
 				i++;
 			}
 			return 0;
 		}
 		private int killsRequired;
 		private int[][] items; 
 		private int[] armor;
 	}
 	private Player killCache;
 	
 	private HashMap<String,Integer> pvpStats = new HashMap<String,Integer>();
 	private HashMap<String, Integer> multiKillCounter = new HashMap<String, Integer>();
 	private HashMap<String,Location> baseLocation = new HashMap<String,Location>();
 	private List<String> cannotLeave = Collections.synchronizedList(new ArrayList<String>());
 	private HashMap<String, Long> playerImortality = new HashMap<String, Long>();
 	private int spawnIndex;
 	private ArenaSpawn actualSpawn;
 	private ArrayList<ArenaSpawn> spawnList = new ArrayList<ArenaSpawn>();
 	private Guga_SERVER_MOD plugin;
 	private static final String arenaFile = "plugins/Arenas.dat";
 	private static final String statsFile = "plugins/ArenaStats.dat";
 	public class ArenaSpawn
 	{
 		public ArenaSpawn(String name, Location location)
 		{
 			this.location = location;
 			this.name = name;
 		}
 		public Location GetLocation()
 		{
 			return this.location;
 		}
 		public String GetName()
 		{
 			return this.name;
 		}
 		private Location location;
 		private String name;
 	}
 }
