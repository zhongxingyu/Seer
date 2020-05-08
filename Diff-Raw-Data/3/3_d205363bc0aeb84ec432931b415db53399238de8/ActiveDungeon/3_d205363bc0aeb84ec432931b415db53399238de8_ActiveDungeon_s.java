 package de.kumpelblase2.dragonslair.api;
 
 import java.util.*;
 import org.bukkit.*;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.map.*;
 import org.bukkit.map.MapView.Scale;
 import de.kumpelblase2.dragonslair.DragonsLairMain;
 import de.kumpelblase2.dragonslair.logging.LoggingManager;
 import de.kumpelblase2.dragonslair.logging.Recoverable;
 import de.kumpelblase2.dragonslair.map.DLMapRenderer;
 import de.kumpelblase2.dragonslair.utilities.WorldUtility;
 
 public class ActiveDungeon
 {
 	private int dungeonid;
 	private Party currentParty;
 	private Chapter currentChapter;
 	private Objective currentObjective;
 	private Map<String, SavedPlayer> playerSaves;
 	
 	public ActiveDungeon(Dungeon d, Party party)
 	{
 		this.dungeonid = d.getID();
 		this.currentParty = party;
 		this.currentChapter = party.getCurrentChapter();
 		this.currentObjective = party.getCurrentObjective();
 		this.playerSaves = new HashMap<String, SavedPlayer>();
 		this.loadParty(party);
 	}
 	
 	public void save()
 	{
 		this.currentParty.save();
 		for(String playername : this.currentParty.getMembers())
 		{
 			Player p = Bukkit.getPlayer(playername);
 			new PlayerSave(p, this.currentParty).save();
 			this.playerSaves.get(playername).restore();
 		}
 		this.playerSaves.clear();
 	}
 	
 	public void loadParty(Party p)
 	{
 		for(String player : p.getMembers())
 		{
 			Player pl = Bukkit.getPlayer(player);
 			this.playerSaves.put(player, new SavedPlayer(pl));
 			pl.getInventory().clear();
 			PlayerSave save = new PlayerSave(pl, p);
 			if(!save.restore())
 			{
 				WorldUtility.enhancedTelepot(pl, this.getInfo().getStartingPosition());
 			}
 			save.remove();
 		}
 		this.currentChapter = p.getCurrentChapter();
 		this.currentObjective = p.getCurrentObjective();
 	}
 	
 	public Party getCurrentParty()
 	{
 		return this.currentParty;
 	}
 	
 	public Dungeon getInfo()
 	{
 		return DragonsLairMain.getSettings().getDungeons().get(this.dungeonid);
 	}
 	
 	public Chapter getCurrentChapter()
 	{
 		return this.currentChapter;
 	}
 	
 	public Objective getCurrentObjective()
 	{
 		return this.currentObjective;
 	}
 	
 	public void setNextChapter(Chapter c)
 	{
 		this.currentChapter = c;
 		this.currentParty.setCurrentChapter(c.getID());
 	}
 	
 	public void setNextObjective(Objective o)
 	{
 		this.currentObjective = o;
 		this.currentParty.setCurrentObjective(o.getID());
 	}
 	
 	public void stop()
 	{
 		this.save();
 	}
 	
 	public void stop(boolean save)
 	{
 		if(save)
 		{
 			this.stop();
 			this.playerSaves.clear();
 			this.sendMessage(this.getInfo().getEndMessage());
 		}
 		else
 		{
 			for(SavedPlayer p : this.playerSaves.values())
 			{
 				p.restore();
 			}
 			this.playerSaves.clear();
 			this.currentParty.remove();
 			for(String member : this.currentParty.getMembers())
 			{
 				new PlayerSave(Bukkit.getPlayer(member), this.currentParty.getID()).remove();
 			}
 		}
 		
 		if(DragonsLairMain.getInstance().getLoggingManager().getEntriesForDungeon(this.getInfo().getName()) != null && DragonsLairMain.getInstance().getLoggingManager().getEntriesForDungeon(this.getInfo().getName()).containsKey(this.currentParty.getID()))
 		{
 			Map<Location, Recoverable> entries = DragonsLairMain.getInstance().getLoggingManager().getEntriesForDungeon(this.getInfo().getName()).get(this.currentParty.getID());
 			if(entries.size() > 0)
 			{
 				List<Location> toRemove = new ArrayList<Location>();
 				for(Location key : entries.keySet())
 				{
 					entries.get(key).recover();
 					if(!save)
 					{
 						entries.get(key).remove();
 						toRemove.add(key);
 					}
 				}
 				
 				for(Location l : toRemove)
 				{
 					entries.remove(l);
 				}
 				
 				if(entries.size() == 0)
 				{
 					DragonsLairMain.getInstance().getLoggingManager().getEntriesForDungeon(this.getInfo().getName()).remove(this.getCurrentParty().getID());
 					if(DragonsLairMain.getInstance().getLoggingManager().getEntriesForDungeon(this.getInfo().getName()).size() == 0)
 						DragonsLairMain.getInstance().getLoggingManager().getEntriesForDungeon(this.getInfo().getName()).remove(this.getInfo().getName());
 				}
 				toRemove.clear();
 			}
 		}
 		this.currentChapter = null;
 		this.currentObjective = null;
 		this.currentParty = null;
 		this.playerSaves = null;
 	}
 	
 	public void setNextChapter(int id)
 	{
 		this.setNextChapter(DragonsLairMain.getSettings().getChapters().get((Integer)id));
 	}
 
 	public void giveMaps()
 	{
 		for(String member : this.currentParty.getMembers())
 		{
 			ItemStack map = new ItemStack(Material.MAP);
 			map.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
             MapView mapview = Bukkit.getServer().getMap(map.getDurability());
             mapview.setCenterX(0);
             mapview.setCenterZ(0);
             mapview.setScale(Scale.FARTHEST);
             for(MapRenderer r : mapview.getRenderers())
     		{
     			mapview.removeRenderer(r);
     		}
     		mapview.addRenderer(new DLMapRenderer());
             Player p = Bukkit.getPlayer(member);
 			p.sendMap(mapview);
 			p.getInventory().addItem(map);
 			DragonsLairMain.getDungeonManager().addMapHolder(p);
 		}
 	}
 	
 	public void sendMessage(String message)
 	{
 		for(String member : this.currentParty.getMembers())
 		{
 			Bukkit.getPlayer(member).sendMessage(message);
 		}
 	}
 
 	public void reloadProgress()
 	{
 		LoggingManager logManager = DragonsLairMain.getInstance().getLoggingManager();
 		if(logManager.getEntriesForDungeon(this.getInfo().getName()) != null)
 		{
 			if(logManager.getEntriesForDungeon(this.getInfo().getName()).containsKey(this.getCurrentParty().getID()))
 			{
 				Map<Location, Recoverable> entries = logManager.getEntriesForDungeon(this.getInfo().getName()).get(this.getCurrentParty().getID());
 				for(Location key : entries.keySet())
 				{
 					entries.get(key).setNew();
 				}
 			}
 		}
 	}
 	
 	public Map<String, SavedPlayer> getSavedPlayers()
 	{
 		return this.playerSaves;
 	}
 }
