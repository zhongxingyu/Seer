 package de.kumpelblase2.dragonslair;
 
 import java.sql.PreparedStatement;
 import java.util.*;
 import org.bukkit.Bukkit;
 import org.bukkit.conversations.ConversationFactory;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import com.topcat.npclib.NPCManager;
 import com.topcat.npclib.entity.HumanNPC;
 import de.kumpelblase2.dragonslair.api.*;
 import de.kumpelblase2.dragonslair.api.eventexecutors.EventExecutor;
 import de.kumpelblase2.dragonslair.events.EventCallEvent;
 import de.kumpelblase2.dragonslair.events.TriggerCallEvent;
 import de.kumpelblase2.dragonslair.map.DLMap;
 import de.kumpelblase2.dragonslair.map.MapList;
 import de.kumpelblase2.dragonslair.settings.Settings;
 import de.kumpelblase2.dragonslair.utilities.EnumChange;
 
 public class DungeonManager
 {
 	private final Map<String, Dungeon> dungeons = new HashMap<String, Dungeon>();
 	private final ConversationFactory cfactory;
 	private final NPCManager npcManager;
 	private final Settings settings = new Settings();
 	private final Map<EventActionType, EventExecutor> executors = new HashMap<EventActionType, EventExecutor>();
 	private final Set<ActiveDungeon> activeDungeons = new HashSet<ActiveDungeon>();
 	private final PlayerQueue queue = new PlayerQueue();
 	private final MapList maps = new MapList();
 	private final Map<String, Integer> m_playerDungeons = new HashMap<String, Integer>();
 	private final Set<EventMonster> spawnedEntities = Collections.synchronizedSet(new HashSet<EventMonster>());
 	private final Map<String, Map<Integer, Map<EntityType, Integer>>> killedMobs = new HashMap<String, Map<Integer, Map<EntityType, Integer>>>();
 	
 	public DungeonManager()
 	{
 		this.cfactory = new ConversationFactory(DragonsLairMain.getInstance());
 		this.npcManager = new NPCManager(DragonsLairMain.getInstance());
 	}
 	
 	public ConversationFactory getConversationFactory()
 	{
 		return this.cfactory;
 	}
 	
 	public Map<String, Dungeon> getDungeons()
 	{
 		return this.dungeons;
 	}
 	
 	public NPCManager getNPCManager()
 	{
 		return this.npcManager;
 	}
 
 	public Settings getSettings()
 	{
 		return this.settings;
 	}
 	
 	public Map<Integer, com.topcat.npclib.entity.NPC> getSpawnedNPCIDs()
 	{
 		return this.npcManager.getNPCs();
 	}
 	
 	public HumanNPC getNPCByName(String inName)
 	{
 		NPC n = DragonsLairMain.getSettings().getNPCByName(inName);
 		if(n == null)
 			return null;
 		
 		return this.npcManager.getNPC(n.getID());
 	}
 	
 	public HumanNPC getNPCByEntity(Entity e)
 	{
 		return (HumanNPC)this.npcManager.getNPC(this.npcManager.getNPCIdFromEntity(e));
 	}
 	
	public de.kumpelblase2.dragonslair.api.NPC getNPCByNPCEntity(Entity npc)
	{
		return this.getSettings().getNPCs().get(this.npcManager.getNPCIdFromEntity(npc));
	}
	
 	public boolean executeEvent(Event e, Player p)
 	{
 		ActiveDungeon ad = this.getDungeonOfPlayer(p.getName());
 		String name = (ad == null) ? "_GENERAL_" : ad.getInfo().getName();
 		boolean onCD = this.isOnCooldown(name, e);
 		EventCallEvent event = new EventCallEvent(e, p, onCD);		
 		Bukkit.getPluginManager().callEvent(event);
 		if(event.isCancelled())
 		{
 			if(event.isOnCooldown() != onCD)
 			{
 				if(event.isOnCooldown())
 					this.addCooldown(name, e);
 				else
 					this.removeCooldown(name, e);
 			}
 			return true;
 		}
 		
 		if(this.executors.containsKey(e.getActionType()) && !this.isOnCooldown(name, e))
 		{
 			boolean result = this.executors.get(e.getActionType()).executeEvent(e, p);
 			if(result)
 				this.addCooldown(name, e);
 			
 			return result;
 		}
 		
 		return false;
 	}
 	
 	public void callTrigger(final Trigger t, final Player p)
 	{
 		ActiveDungeon ad = this.getDungeonOfPlayer(p.getName());
 		final String name = (ad == null) ? "_GENERAL_" : ad.getInfo().getName();
 		boolean onCD = this.isOnCooldown(name, t);
 		TriggerCallEvent event = new TriggerCallEvent(t, p, onCD);
 		Bukkit.getPluginManager().callEvent(event);
 		if(event.isCancelled())
 		{
 			if(event.isOnCooldown() != onCD)
 			{
 				if(!event.isOnCooldown())
 					this.removeCooldown(name, t);
 				else
 					this.addCooldown(name, t);
 			}
 			return;
 		}
 		
 		if(event.isOnCooldown())
 			return;
 		
 		int delay = 0;
 		if(t.getOption("delay") != null)
 		{
 			try
 			{
 				delay = Integer.parseInt(t.getOption("delay"));
 			}
 			catch(Exception e)
 			{
 				DragonsLairMain.Log.info("Unable to parse delay for trigger: " + t.getOption("delay"));
 			}
 		}
 		
 		if(delay > 0)
 			Bukkit.getScheduler().scheduleSyncDelayedTask(DragonsLairMain.getInstance(), new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					List<Integer> ids = t.getEventIDs();
 					for(Integer eid : ids)
 					{
 						if(eid == 0)
 							continue;
 						
 						final Event e = DragonsLairMain.getSettings().getEvents().get(eid);
 						if(e == null)
 							continue;
 						
 						if(e.getOption("delay") != null)
 						{
 							Bukkit.getScheduler().scheduleSyncDelayedTask(DragonsLairMain.getInstance(), new Runnable()
 							{
 								
 								@Override
 								public void run()
 								{
 									DragonsLairMain.getDungeonManager().executeEvent(e, p);
 								}
 							}, Integer.parseInt(e.getOption("delay")) * 20L);
 						}
 						else
 						{
 							DragonsLairMain.getDungeonManager().executeEvent(e, p);
 						}
 					}
 					t.use(name);
 				}
 			}, delay * 20);
 		else
 		{
 			List<Integer> ids = t.getEventIDs();
 			for(Integer eid : ids)
 			{
 				if(eid == 0)
 					continue;
 				
 				final Event e = DragonsLairMain.getSettings().getEvents().get(eid);
 				if(e == null)
 					continue;
 				
 				if(e.getOption("delay") != null)
 				{
 					Bukkit.getScheduler().scheduleSyncDelayedTask(DragonsLairMain.getInstance(), new Runnable()
 					{
 						
 						@Override
 						public void run()
 						{
 							DragonsLairMain.getDungeonManager().executeEvent(e, p);
 						}
 					}, Integer.parseInt(e.getOption("delay")) * 20L);
 				}
 				else
 				{
 					DragonsLairMain.getDungeonManager().executeEvent(e, p);
 				}
 			}
 			t.use(name);
 		}
 	}
 	
 	public void setEventExecutor(EventActionType action, EventExecutor executor)
 	{
 		this.executors.put(action, executor);
 	}
 	
 	public void spawnNPC(String name)
 	{
 		NPC npc = DragonsLairMain.getSettings().getNPCByName(name);
 		this.spawnNPC(npc);
 	}
 	
 	public void spawnNPC(Integer id)
 	{
 		NPC npc = DragonsLairMain.getSettings().getNPCs().get(id);
 		if(npc == null)
 		{
 			DragonsLairMain.Log.warning("Unable to spawn NPC with id " + id + ".");
 			return;
 		}
 		this.spawnNPC(npc);
 	}
 	
 	public void spawnNPC(NPC npc)
 	{
 		HumanNPC hnpc = (HumanNPC)this.getNPCManager().spawnHumanNPC(npc);
 		if(hnpc != null)
 		{
 			if(npc.getSkin() != null && !npc.getSkin().equals(""))
 				hnpc.setSkin(npc.getSkin());
 			
 			hnpc.setItemInHand(npc.getHeldItem());
 			hnpc.getInventory().setArmorContents(npc.getArmorParts());
 		}
 	}
 	
 	public boolean despawnNPC(String name)
 	{
 		NPC n = DragonsLairMain.getSettings().getNPCByName(name);
 		if(n == null)
 			return false;
 		
 		return this.despawnNPC(n.getID());
 	}
 	
 	public boolean despawnNPC(Integer id)
 	{
 		return this.npcManager.despawnById(id);
 	}
 	
 	public void spawnNPCs()
 	{
 		Bukkit.getScheduler().scheduleSyncDelayedTask(DragonsLairMain.getInstance(), new Runnable() {
 			public void run()
 			{
 				for(NPC n : DragonsLairMain.getSettings().getNPCs().values())
 				{
 					if(n.shouldSpawnAtBeginning())
 						DragonsLairMain.getDungeonManager().spawnNPC(n);
 				}
 			}
 		});
 	}
 	
 	public ActiveDungeon startDungeon(int id, String[] players)
 	{
 		Dungeon d = this.getSettings().getDungeons().get(id);
 		ActiveDungeon ad = new ActiveDungeon(d, Party.getPartyOfPlayers(players, id));
 		this.activeDungeons.add(ad);
 		for(String p : players)
 		{
 			this.m_playerDungeons.put(p, ad.getInfo().getID());
 		}
 		ad.giveMaps();
 		ad.sendMessage(ad.getInfo().getStartingMessage());
 		ad.reloadProgress();
 		return ad;
 	}
 	
 	public ActiveDungeon startDungeon(String name)
 	{		
 		if(this.isDungeonStarted(name))
 			return null;
 		
 		Dungeon d = this.getSettings().getDungeonByName(name);
 		if(d == null)
 			return null;
 		
 		return this.queue.start(d);
 	}
 	
 	public ActiveDungeon startDungeon(int id)
 	{
 		return this.startDungeon(this.getSettings().getDungeons().get(id).getName());
 	}
 	
 	public boolean hasRegistered(String player)
 	{
 		return this.queue.isInQueue(Bukkit.getPlayer(player));
 	}
 	
 	public void stopDungeon(int id)
 	{
 		this.stopDungeon(id, true);
 	}
 	
 	public void stopDungeon(int id, boolean save)
 	{
 		Iterator<ActiveDungeon> dungeons = this.activeDungeons.iterator();
 		while(dungeons.hasNext())
 		{
 			ActiveDungeon ad = dungeons.next();
 			if(ad.getInfo().getID() == id)
 			{
 				for(String p : ad.getCurrentParty().getMembers())
 				{
 					this.maps.removeMap(Bukkit.getPlayer(p));
 					this.m_playerDungeons.remove(p);
 				}
 				ad.stop(save);
 				this.clearMobs(ad.getInfo().getID());
 				this.killedMobs.remove(ad.getInfo().getName());
 				dungeons.remove();
 				return;
 			}
 		}
 	}
 	
 	public void stopDungeon(String name)
 	{
 		this.stopDungeon(name, true);
 	}
 	
 	public void stopDungeon(String name, boolean save)
 	{
 		int id = -1;
 		for(ActiveDungeon d : this.activeDungeons)
 		{
 			if(d.getInfo().getName().equals(name))
 			{
 				id = d.getInfo().getID();
 				break;
 			}
 		}
 		if(id != -1)
 			this.stopDungeon(id, save);
 	}
 	
 	public ActiveDungeon getDungeonOfPlayer(String name)
 	{
 		if(this.m_playerDungeons.containsKey(name))
 		{
 			int id = this.m_playerDungeons.get(name);
 			for(ActiveDungeon ad : this.activeDungeons)
 			{
 				if(ad.getInfo().getID() == id)
 					return ad;
 			}
 		}
 		return null;
 	}
 	
 	public Set<ActiveDungeon> getActiveDungeons()
 	{
 		return this.activeDungeons;
 	}
 	
 	public PlayerQueue getQueue()
 	{
 		return this.queue;
 	}
 	
 	public boolean isDungeonStarted(String name)
 	{
 		for(ActiveDungeon d : this.activeDungeons)
 		{
 			if(d.getInfo().getName().equalsIgnoreCase(name))
 				return true;
 		}
 		return false;
 	}
 	
 	public void queuePlayer(String name, String dungeon)
 	{
 		this.queue.queuePlayer(dungeon, Bukkit.getPlayer(name));
 	}
 	
 	public void queuePlayer(String name, int id)
 	{
 		this.queuePlayer(name, this.getSettings().getDungeons().get(id).getName());
 	}
 	
 	public void addMapHolder(Player player)
 	{
 		this.maps.addMap(player, new DLMap(player));
 	}
 	
 	public DLMap getMapOfPlayer(Player p)
 	{
 		return this.maps.getMapOfPlayer(p);
 	}
 
 	public void stopDungeons()
 	{
 		
 	}
 	
 	public void addCooldown(String name, Trigger t)
 	{
 		t.use(name);
 	}
 	
 	public boolean isOnCooldown(String name, Trigger t)
 	{
 		return t.canUse(name);
 	}
 	
 	public void addCooldown(String name, Event e)
 	{
 		e.use(name);
 	}
 	
 	public boolean isOnCooldown(String name, Event e)
 	{
 		return e.canUse(name);
 	}
 	
 	public void removeCooldown(String name, Trigger t)
 	{
 		t.removeCooldown(name);
 	}
 	
 	public void removeCooldown(String name, Event e)
 	{
 		e.removeCooldown(name);
 	}
 
 	public Set<EventMonster> getSpawnedMobs()
 	{
 		return this.spawnedEntities;
 	}
 	
 	public Event getEventFromMob(LivingEntity entity)
 	{
 		for(EventMonster mob : this.spawnedEntities)
 		{
 			if(mob.equals(entity))
 				return mob.getEvent();
 		}
 		return null;
 	}
 	
 	public void clearMobs(int id)
 	{
 		Iterator<EventMonster> mobs = this.spawnedEntities.iterator();
 		while(mobs.hasNext())
 		{
 			EventMonster mob = mobs.next();
 			if(mob.getDungeon().getInfo().getID() != id)
 				continue;
 			
 			mob.getMonster().remove();
 			mobs.remove();
 		}
 	}
 	
 	public Map<Integer, Map<EntityType, Integer>> getKills(ActiveDungeon ad)
 	{
 		return this.killedMobs.get(ad.getInfo().getName());
 	}
 	
 	public int addMobKill(ActiveDungeon ad, LivingEntity entity, Event e)
 	{
 		Iterator<EventMonster> spawned = this.spawnedEntities.iterator();
 		while(spawned.hasNext())
 		{
 			EventMonster mob = spawned.next();
 			if(mob.equals(entity))
 			{
 				spawned.remove();
 				break;
 			}
 		}
 		
 		Map<Integer, Map<EntityType, Integer>> mobKills = this.getKills(ad);
 		if(mobKills != null)
 		{
 			Map<EntityType, Integer> killed = mobKills.get(e.getID());
 			if(killed != null)
 			{
 				Integer amount = killed.get(entity.getType());
 				amount = (amount == null) ? 1 : amount + 1; 
 				killed.put(entity.getType(), amount);
 				return amount;
 			}
 			else
 			{
 				killed = new HashMap<EntityType, Integer>();
 				killed.put(entity.getType(), 1);
 				mobKills.put(e.getID(), killed);
 				return 1;
 			}
 		}
 		else
 		{
 			mobKills = new HashMap<Integer, Map<EntityType, Integer>>();
 			Map<EntityType, Integer> killed = new HashMap<EntityType, Integer>();
 			killed.put(entity.getType(), 1);
 			mobKills.put(e.getID(), killed);
 			return 1;
 		}
 	}
 	
 	public void saveCooldowns()
 	{
 		for(Event e : this.getSettings().getEvents().values())
 		{
 			e.save();
 		}
 		
 		for(Trigger t : this.getSettings().getTriggers().values())
 		{
 			t.save();
 		}
 	}
 	
 	public void registerEventType(String type, boolean addToDb)
 	{
 		EnumChange.addEnum(EventActionType.class, type);
 		if(addToDb)
 		{
 			try
 			{
 				PreparedStatement st = DragonsLairMain.createStatement("ALTER TABLE `events` CHANGE COLUMN `event_action_type` `event_action_type` enum(?)");
 				StringBuilder sb = new StringBuilder();
 				for(EventActionType ctype : EventActionType.values())
 				{
 					sb.append("'" + ctype.toString().toLowerCase() + "',");
 				}
 				if(sb.length() > 1)
 					sb.substring(0, sb.length() - 1);
 				st.setString(1, sb.toString());
 			}
 			catch(Exception e)
 			{
 				DragonsLairMain.Log.warning("Unable to register type at database:");
 				DragonsLairMain.Log.warning(e.getMessage());
 			}
 		}
 	}
 	
 	public void removeEventType(String type, boolean removeFromDb)
 	{
 		EnumChange.removeEnum(EventActionType.class, type);
 		if(removeFromDb)
 		{
 			try
 			{
 				PreparedStatement st = DragonsLairMain.createStatement("ALTER TABLE `events` CHANGE COLUMN `event_action_type` `event_action_type` enum(?)");
 				StringBuilder sb = new StringBuilder();
 				for(EventActionType ctype : EventActionType.values())
 				{
 					if(!ctype.toString().equalsIgnoreCase(type))
 						sb.append("'" + ctype.toString().toLowerCase() + "',");
 				}
 				if(sb.length() > 1)
 					sb.substring(0, sb.length() - 1);
 				st.setString(1, sb.toString());
 			}
 			catch(Exception e)
 			{
 				DragonsLairMain.Log.warning("Unable to remove type at database:");
 				DragonsLairMain.Log.warning(e.getMessage());
 			}
 		}
 	}
 	
 	public void registerTriggerType(String type, boolean addToDb)
 	{
 		EnumChange.addEnum(TriggerType.class, type);
 		if(addToDb)
 		{
 			try
 			{
 				PreparedStatement st = DragonsLairMain.createStatement("ALTER TABLE `triggers` CHANGE COLUMN `trigger_type` `trigger_type` enum(?)");
 				StringBuilder sb = new StringBuilder();
 				for(TriggerType ctype : TriggerType.values())
 				{
 					sb.append("'" + ctype.toString().toLowerCase() + "',");
 				}
 				if(sb.length() > 1)
 					sb.substring(0, sb.length() - 1);
 				st.setString(1, sb.toString());
 			}
 			catch(Exception e)
 			{
 				DragonsLairMain.Log.warning("Unable to register type at database:");
 				DragonsLairMain.Log.warning(e.getMessage());
 			}
 		}
 	}
 	
 	public void removeTriggerType(String type, boolean removeFromDb)
 	{
 		EnumChange.removeEnum(TriggerType.class, type);
 		if(removeFromDb)
 		{
 			try
 			{
 				PreparedStatement st = DragonsLairMain.createStatement("ALTER TABLE `triggers` CHANGE COLUMN `trigger_type` `trigger_type` enum(?)");
 				StringBuilder sb = new StringBuilder();
 				for(TriggerType ctype : TriggerType.values())
 				{
 					if(!ctype.toString().equalsIgnoreCase(type))
 						sb.append("'" + ctype.toString().toLowerCase() + "',");
 				}
 				if(sb.length() > 1)
 					sb.substring(0, sb.length() - 1);
 				st.setString(1, sb.toString());
 			}
 			catch(Exception e)
 			{
 				DragonsLairMain.Log.warning("Unable to remove type at database:");
 				DragonsLairMain.Log.warning(e.getMessage());
 			}
 		}
 	}
 	
 	public ActiveDungeon getActiveDungeonByName(String name)
 	{
 		for(ActiveDungeon ad : this.activeDungeons)
 		{
 			if(ad.getInfo().getName().equals(name))
 				return ad;
 		}
 		return null;
 	}
 
 	public HumanNPC getNPCByID(Integer npcID)
 	{
 		return this.npcManager.getNPC(npcID);
 	}
 
 	public boolean despawnNPC(NPC n)
 	{
 		return this.despawnNPC(n.getID());
 	}
 }
