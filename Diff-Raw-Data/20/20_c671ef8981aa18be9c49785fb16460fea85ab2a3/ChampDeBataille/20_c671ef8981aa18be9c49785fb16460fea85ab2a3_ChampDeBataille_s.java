 package fr.vaevictis.vvcdb;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.util.Vector;
 
 import com.massivecraft.factions.FPlayers;
 
 import fr.vaevictis.timertasks.TimerTaskGainPoints;
 
 public class ChampDeBataille 
 {
 	public ChampDeBataille(String nom)
 	{
 		this.nom = nom.toLowerCase();
 		this.pointsToGet = 50;
 	}
 	public void ajouterMap()
 	{
 		champsdebataille.put(this.nom, this);
 	}
 	public void soustraireMap()
 	{
 		champsdebataille.remove(this.nom);
 	}
 	public static ChampDeBataille getMap(String key)
 	{
		return getMap(key);
 	}
 	
 	public String getNom()
 	{
 		return this.nom;
 	}
 	public Location getDefaultSpawn()
 	{
 		return this.defaultSpawn;
 	}
 	public void setDefaultSpawn(Location l)
 	{
 		this.defaultSpawn = l;
 	}
 	public ArrayList<Player> getJoueurs()
 	{
 		return this.joueurs;
 	}
 	public void playerJoin(Player p)
 	{
 		joueurs.add(p);
 		locationsjoueurs.add(p.getLocation());
 		inventairesjoueurs.add(p.getInventory());
 		VVCdB.equipPlayerArmor(p);
 		boolean lancer = false;
 		for (Player p2 : joueurs)
 		{
 			if (FPlayers.i.get(p).getFaction().getId() != FPlayers.i.get(p2).getFaction().getId())
 			{
 				lancer = true;
 			}
 		}
 		if (lancer == true && cdbstarted == false)
 		{
 			lancerAttaque();
 		}
 		p.teleport(defaultSpawn);
 		playerwholeft.remove(p);
 		pointsToGet = 50 + 5 * (joueurs.size() + playerwholeft.size());
 	}
 	public void playerLeave(Player p)
 	{
 		p.getInventory().clear();
 		p.getInventory().setArmorContents(inventairesjoueurs.get(joueurs.lastIndexOf(p)).getArmorContents());
 		p.getInventory().setContents(inventairesjoueurs.get(joueurs.lastIndexOf(p)).getContents());
 		p.teleport(locationsjoueurs.get(joueurs.lastIndexOf(p)));
 		joueurs.remove(p);
 		playerwholeft.add(p);
 	}
 	public static void arret()
 	{
 		for (VVFaction f : VVFaction.factions)
 		{
 			f.setPoints(0);
 		}
 		ChampDeBataille cdb = getMap(usedCdB);
 		usedCdB = "";
 		cdbstarted = false;
 		cdb.pointsToGet = 50;
 		for (Position p : cdb.positions)
 		{
 			p.setFaction("");
 			p.timer = -1;
 			p.setFactionTaking("");
 		}
 		for (Player p : cdb.joueurs)
 		{
 			VVCdB.setArmure(p, "");
 			cdb.playerLeave(p);
 		}
 		for (Player p : cdb.playerwholeft)
 		{
 			VVCdB.setArmure(p, "");
 		}
 		cdb.playerwholeft.clear();
 	}
 	public static void lancerAttaque()
 	{
 		for (VVFaction f : VVFaction.factions)
 		{
 			f.setPoints(0);
 		}
 		ChampDeBataille cdb = getMap(usedCdB);
 		cdbstarted = true;
 		Bukkit.getServer().getScheduler().runTaskTimer(Bukkit.getServer().getPluginManager().getPlugin("VVCdB"), new TimerTaskGainPoints(), 400, 400);
 	}
 	public ArrayList<Position> getPositions()
 	{
 		return this.positions;
 	}
 	public void spawnPlayer(Player p)
 	{
 		int nombrepos = 0;
 		for (Position pos : positions)
 		{
 			if (pos.getFaction() == FPlayers.i.get(p).getFaction().getId())
 			{
 				nombrepos++;
 			}
 		}
 		int i = (int) Math.random() * nombrepos;
 		int nombreposbis = nombrepos;
 		nombrepos = 0;
 		if (nombreposbis !=0)
 		{
 			for (Position pos : positions)
 			{
 				if (pos.getFaction() == FPlayers.i.get(p).getFaction().getId())
 				{
 					nombrepos++;
 				}
 				if (nombrepos == i)
 				{
 					int direction = (int) (Math.random() * 4);
 					int x = (int) (Math.random() * 20);
 					Location l = new Location(Bukkit.getWorld("world"), 0D, 0D, 0D);
 					if (direction == 0)
 					{
 						l = pos.getLocation().add(new Vector(x, 0, Math.sqrt(400 - Math.pow((double) x, 2))));
 					}
 					else if (direction == 1)
 					{
 						l = pos.getLocation().add(new Vector(-x, 0, Math.sqrt(400 - Math.pow((double) x, 2))));
 					}
 					else if (direction == 2)
 					{
 						l = pos.getLocation().add(new Vector(x, 0, -Math.sqrt(400 - Math.pow((double) x, 2))));
 					}
 					else if (direction == 3)
 					{
 						l = pos.getLocation().add(new Vector(-x, 0, -Math.sqrt(400 - Math.pow((double) x, 2))));
 					}
 					p.teleport(l);
 				}
 			}
 		}
 		else if (nombrepos == 0)
 		{
 			p.teleport(spawns.get( (int) (Math.random() * this.spawns.size())));
 		}
 	}
 	public ArrayList<Location> getSpawns()
 	{
 		return spawns;
 	}
 	public int pointsToGet;
 	private ArrayList<Location> spawns;
 	private Location defaultSpawn;
 	private ArrayList<Player> joueurs;
 	private ArrayList<Player> playerwholeft;
 	private ArrayList<Location> locationsjoueurs;
 	private ArrayList<PlayerInventory> inventairesjoueurs;
 	private ArrayList<Position> positions;
 	private String nom;
 	public static HashMap<String, ChampDeBataille> champsdebataille;
 	public static String usedCdB;
 	public static boolean cdbstarted;
 	
 	public void save(VVCdB plugin, int numero)
 	{
 		FileConfiguration config = plugin.getConfig();
 		config.set("champsdebataille." + numero, this.nom);
 		config.set("champsdebataille." + numero + ".defaultspawn.X", this.defaultSpawn.getBlockX());
 		config.set("champsdebataille." + numero + ".defaultspawn.Y", this.defaultSpawn.getBlockY());
 		config.set("champsdebataille." + numero + ".defaultspawn.Z", this.defaultSpawn.getBlockZ());
 		config.set("champsdebataille." + numero + ".defaultspawn.world", this.defaultSpawn.getWorld().getName());
 		
 		config.set("champsdebataille." + numero + ".positions", this.positions.size());
 		int positionsnb = 0;
 		for (Position p : this.positions)
 		{
 			config.set("champsdebataille." + numero + ".positions." + String.valueOf(positionsnb), p.getNom());
 			config.set("champsdebataille." + numero + ".positions." + String.valueOf(positionsnb) + ".location.X", p.getLocation().getBlockX());
 			config.set("champsdebataille." + numero + ".positions." + String.valueOf(positionsnb) + ".location.Y", p.getLocation().getBlockY());
 			config.set("champsdebataille." + numero + ".positions." + String.valueOf(positionsnb) + ".location.Z", p.getLocation().getBlockZ());
 			config.set("champsdebataille." + numero + ".positions." + String.valueOf(positionsnb) + ".location.world", p.getLocation().getWorld().getName());
 			config.set("champsdebataille." + numero + ".positions." + String.valueOf(positionsnb) + ".couverte", p.isCouverte());
 			positionsnb++;
 		}
 		
 		config.set("champsdebataille." + numero + ".spawns", this.spawns.size());
 		int spawnsnb = 0;
 		for (Location l : spawns)
 		{
 			config.set("champsdebataille." + numero + ".spawns." + String.valueOf(spawnsnb) + ".X", l.getBlockX());
 			config.set("champsdebataille." + numero + ".spawns." + String.valueOf(spawnsnb) + ".Y", l.getBlockY());
 			config.set("champsdebataille." + numero + ".spawns." + String.valueOf(spawnsnb) + ".Z", l.getBlockZ());
 			config.set("champsdebataille." + numero + ".spawns." + String.valueOf(spawnsnb) + ".world", l.getWorld().getName());
 			spawnsnb++;
 		}
 		plugin.saveConfig();
 	}
 	public static void saveAll(VVCdB plugin)
 	{
 		FileConfiguration config = plugin.getConfig();
 		config.set("champsdebataille", champsdebataille.size());
 		int champsdebataillenb = 0;
 		for (ChampDeBataille cdb : champsdebataille.values())
 		{
 			config.set("champsdebataille." + String.valueOf(champsdebataillenb), cdb.nom);
 			cdb.save(plugin, champsdebataillenb);
 			champsdebataillenb++;
 		}
 		plugin.saveConfig();		
 	}
 	
 }
