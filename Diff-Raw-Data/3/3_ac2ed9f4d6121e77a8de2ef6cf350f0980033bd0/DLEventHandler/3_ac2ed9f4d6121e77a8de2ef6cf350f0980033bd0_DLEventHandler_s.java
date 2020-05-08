 package de.kumpelblase2.dragonslair;
 
 import java.util.*;
 import org.bukkit.*;
 import org.bukkit.block.*;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.*;
 import org.bukkit.event.*;
 import org.bukkit.event.block.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.*;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.Bed;
 import org.bukkit.material.Door;
 import com.topcat.npclib.entity.HumanNPC;
 import com.topcat.npclib.nms.*;
 import com.topcat.npclib.nms.NpcEntityTargetEvent.NpcTargetReason;
 import de.kumpelblase2.dragonslair.api.*;
 import de.kumpelblase2.dragonslair.api.Event;
 import de.kumpelblase2.dragonslair.api.NPC;
 import de.kumpelblase2.dragonslair.events.conversation.*;
 import de.kumpelblase2.dragonslair.events.dungeon.*;
 import de.kumpelblase2.dragonslair.logging.TNTList;
 import de.kumpelblase2.dragonslair.logging.TNTList.TNTEntry;
 import de.kumpelblase2.dragonslair.map.DLMap;
 import de.kumpelblase2.dragonslair.utilities.InventoryUtilities;
 
 public class DLEventHandler implements Listener
 {
 	private Map<TriggerType, Set<Trigger>> triggers = new HashMap<TriggerType, Set<Trigger>>();
 	private Set<TriggerLocationEntry> locations = new HashSet<TriggerLocationEntry>();
 	private TNTList tntList = new TNTList();
 	
 	public void reloadTriggers()
 	{
 		this.triggers.clear();
 		this.locations.clear();
 		
 		for(Trigger t : DragonsLairMain.getSettings().getTriggers().values())
 		{
 			if(t.getOption("x") == null || t.getOption("y") == null || t.getOption("z") == null)
 			{
 				if(!this.triggers.containsKey(t.getType()))
 					this.triggers.put(t.getType(), new HashSet<Trigger>());
 				
 				this.triggers.get(t.getType()).add(t);
 			}
 			else
 			{
 				String x, y, z, x2, y2, z2;
 				x = t.getOption("x");
 				y = t.getOption("y");
 				z = t.getOption("z");
 				x2 = t.getOption("x2");
 				y2 = t.getOption("y2");
 				z2 = t.getOption("z2");
 				String world = t.getOption("world");
 				
 				if(x2 == null)
 					x2 = x;
 				if(y2 == null)
 					y2 = y;
 				if(z2 == null)
 					z2 = z;
 				
 				int minx = 0, maxx = 0, miny = 0, maxy = 0, minz = 0, maxz = 0;
 				try
 				{
 					minx = Integer.parseInt(x);
 					maxx = Integer.parseInt(x2);
 					miny = Integer.parseInt(y);
 					maxy = Integer.parseInt(y2);
 					minz = Integer.parseInt(z);
 					maxz = Integer.parseInt(z2);
 				}
 				catch(Exception e)
 				{
 					DragonsLairMain.Log.warning("The was an error parsing the location of trigger " + t.getID() + ".");
 					continue;
 				}
 				
 				for(int posx = minx; posx <= maxx; posx++ )
 				{
 					for(int posy = miny; posy <= maxy; posy++)
 					{
 						for(int posz = minz; posz <= maxz; posz++)
 						{
 							Location loc = new Location(Bukkit.getWorld(world), posx, posy, posz);
 							boolean added = false;
 							for(TriggerLocationEntry entry : this.locations)
 							{
 								if(entry.equals(loc))
 								{
 									entry.addTrigger(t);
 									added = true;
 								}
 							}
 							if(!added)
 							{
 								TriggerLocationEntry entry = new TriggerLocationEntry(loc);
 								entry.addTrigger(t);
 								this.locations.add(entry);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onInteractNPC(EntityTargetEvent event)
 	{
 		if(!DragonsLairMain.isWorldEnabled(event.getEntity().getWorld().getName()))
 			return;
 		
 		if(event instanceof NpcEntityTargetEvent)
 		{
 			if(((NpcEntityTargetEvent)event).getNpcReason() == NpcTargetReason.NPC_RIGHTCLICKED)
 			{
 				if(!this.triggers.containsKey(TriggerType.NPC_INTERACT))
 					return;
 				
 				HumanNPC entitynpc = DragonsLairMain.getDungeonManager().getNPCByEntity(event.getEntity());
 				NPC npc = DragonsLairMain.getSettings().getNPCByName(entitynpc.getName());
 				
 				for(Trigger t : this.triggers.get(TriggerType.NPC_INTERACT))
 				{
 					String npcid = t.getOption("npc_id");
 					if(npcid == null)
 						continue;
 					
 					if(npcid.equals(npc.getID() + ""))
 					{
 						DragonsLairMain.getDungeonManager().callTrigger(t, (Player)event.getTarget());
 					}
 				}
 			}
 			else if(((NpcEntityTargetEvent)event).getNpcReason() == NpcTargetReason.NPC_BOUNCED)
 			{
 				if(!this.triggers.containsKey(TriggerType.NPC_TOUCH))
 					return;
 				
 				HumanNPC entitynpc = DragonsLairMain.getDungeonManager().getNPCByEntity(event.getEntity());
 				NPC npc = DragonsLairMain.getSettings().getNPCByName(entitynpc.getName());
 				
 				for(Trigger t : this.triggers.get(TriggerType.NPC_TOUCH))
 				{
 					String npcid = t.getOption("npc_id");
 					if(npcid == null)
 						continue;
 					
 					if(npcid.equals(npc.getID() + ""))
 					{
 						DragonsLairMain.getDungeonManager().callTrigger(t, (Player)event.getTarget());
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onNPCDamage(NpcDamageEvent event)
 	{
 		if(!(event.getDamager() instanceof Player))
 			return;
 
 		if(!this.triggers.containsKey(TriggerType.NPC_DAMAGE))
 			return;
 		
 		if(!DragonsLairMain.isWorldEnabled(event.getDamager().getWorld().getName()))
 			return;
 		
 		HumanNPC entitynpc = DragonsLairMain.getDungeonManager().getNPCByEntity(event.getDamager());
 		NPC npc = DragonsLairMain.getSettings().getNPCByName(entitynpc.getName());
 		
 		for(Trigger t : this.triggers.get(TriggerType.NPC_DAMAGE))
 		{
 			String npcid = t.getOption("npc_id");
 			if(npcid == null)
 				continue;
 			
 			if(npcid.equals(npc.getID() + ""))
 			{
 				DragonsLairMain.getDungeonManager().callTrigger(t, (Player)event.getDamager());
 			}
 		}
 		
 		if(npc.isInvincible())
 			event.setDamage(0);
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onMove(PlayerMoveEvent event)
 	{		
 		Location from = event.getFrom();
 		Location to = event.getTo();
 		Player p = event.getPlayer();
 		
 		if(to.getBlockX() - from.getBlockX() == 0 && to.getBlockY() - from.getBlockY() == 0 && to.getBlockZ() - from.getBlockZ() == 0)
 			return;
 		
 		if(!DragonsLairMain.isWorldEnabled(to.getWorld().getName()))
 			return;
 		
 		if(this.locations.size() > 0)
 		{
 			Location newLoc = new Location(p.getWorld(), to.getBlockX(), to.getY(), to.getBlockZ());
 			for(TriggerLocationEntry entry : this.locations)
 			{
 				if(entry.equals(newLoc))
 				{
 					for(Trigger t : entry.getTriggersForType(TriggerType.MOVEMENT))
 					{
 						DragonsLairMain.getDungeonManager().callTrigger(t, p);
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onNextDialog(ConversationNextDialogEvent event)
 	{
 		if(!DragonsLairMain.isWorldEnabled(((Player)event.getConversation().getForWhom()).getWorld().getName()))
 			return;
 		
 		if(this.triggers.containsKey(TriggerType.DIALOG_OCCUR))
 		{
 			for(Trigger t : this.triggers.get(TriggerType.DIALOG_OCCUR))
 			{
 				if(!t.getOption("npc_id").equals(event.getNPC().getID() + ""))
 					continue;
 				
 				if(t.getOption("dialog_id") != null)
 				{
 					String id = t.getOption("dialog_id");
 					if(id.equals(event.getNextDialogID() + ""))
 					{
 						DragonsLairMain.getDungeonManager().callTrigger(t, (Player)event.getConversation().getForWhom());
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerChat(PlayerChatEvent event)
 	{
 		Player p = event.getPlayer();
 		if(!DragonsLairMain.isWorldEnabled(p.getWorld().getName()))
 			return;
 		
 		ActiveDungeon d = DragonsLairMain.getDungeonManager().getDungeonOfPlayer(p.getName());
 		if(d == null)
 			return;
 		
 		if(d.getInfo().getSafeWord().equalsIgnoreCase(event.getMessage()))
 			DragonsLairMain.getInstance().getConversationHandler().startSafeWordConversation(p);	
 	}
 	
 	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
 	public void onBlockPlace(BlockPlaceEvent event)
 	{
 		Player p = event.getPlayer();
 		Location placed = event.getBlock().getLocation();
 		if(!DragonsLairMain.isWorldEnabled(placed.getWorld().getName()))
 			return;
 		
 		ActiveDungeon ad = DragonsLairMain.getDungeonManager().getDungeonOfPlayer(p.getName());
 		if(ad != null)
 		{
 			DragonsLairMain.getInstance().getLoggingManager().logBlockPlace(ad, event.getBlock().getState());
 		}
 		
 		if(event.getBlock().getType() == Material.TNT)
 			this.tntList.addEntry(ad.getInfo().getName(), placed);
 		
 		
 		for(TriggerLocationEntry entry : this.locations)
 		{
 			if(entry.equals(event.getBlock().getLocation()))
 			{
 				for(Trigger t : entry.getTriggersForType(TriggerType.BLOCK_PLACE))
 				{
 					String block_id = t.getOption("block_id");
 					if(block_id != null)
 					{
 						try
 						{
 							int id = Integer.parseInt(block_id);
 							Material m = Material.getMaterial(id);
 							if(m != null)
 							{
 								if(placed.getBlock().getType() != m)
 									continue;
 							}
 						}
 						catch(Exception e)
 						{
 							Material m = Material.getMaterial(block_id.replace(" ", "_").toUpperCase());
 							if(m != null)
 							{
 								if(placed.getBlock().getType() != m)
 									continue;
 							}
 						}
 					}
 					DragonsLairMain.getDungeonManager().callTrigger(t, p);
 				}
 			}
 		}
 	}
 	
 	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
 	public void onBlockBreak(BlockBreakEvent event)
 	{
 		Player p = event.getPlayer();
 		Block placed = event.getBlock();
 		if(!DragonsLairMain.isWorldEnabled(placed.getWorld().getName()))
 			return;
 		
 		ActiveDungeon ad = DragonsLairMain.getDungeonManager().getDungeonOfPlayer(p.getName());
 		if(ad != null)
 		{
 			DragonsLairMain.getInstance().getLoggingManager().logBlockBreak(ad, placed.getState());
 			switch(placed.getType())
 			{
 				case BED_BLOCK:
 					Bed b = (Bed)placed.getState().getData();
 					if(b.isHeadOfBed())
 						DragonsLairMain.getInstance().getLoggingManager().logBlockBreak(ad, placed.getRelative(b.getFacing().getOppositeFace()).getState());
 					else
 						DragonsLairMain.getInstance().getLoggingManager().logBlockBreak(ad, placed.getRelative(b.getFacing()).getState());
 					break;
 				case WOODEN_DOOR:
 				case IRON_DOOR_BLOCK:
 					Door d = (Door)placed.getState().getData();
 					if(d.isTopHalf())
 						DragonsLairMain.getInstance().getLoggingManager().logBlockBreak(ad, placed.getRelative(BlockFace.DOWN).getState());
 					else
 						DragonsLairMain.getInstance().getLoggingManager().logBlockBreak(ad, placed.getRelative(BlockFace.UP).getState());
 					break;
 			}
 		}
 		
 		for(TriggerLocationEntry entry : this.locations)
 		{
 			if(entry.equals(placed.getLocation()))
 			{
 				for(Trigger t : entry.getTriggersForType(TriggerType.BLOCK_BREAK))
 				{
 					String block_id = t.getOption("block_id");
 					if(block_id != null)
 					{
 						try
 						{
 							int id = Integer.parseInt(block_id);
 							Material m = Material.getMaterial(id);
 							if(m != null)
 							{
 								if(placed.getType() != m)
 									continue;
 							}
 						}
 						catch(Exception e)
 						{
 							Material m = Material.getMaterial(block_id.replace(" ", "_").toUpperCase());
 							if(m != null)
 							{
 								if(placed.getType() != m)
 									continue;
 							}
 						}
 					}
 					DragonsLairMain.getDungeonManager().callTrigger(t, p);
 				}
 			}
 		}
 	}
 	
 	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
 	public void onExplode(EntityExplodeEvent event)
 	{
 		TNTEntry e = this.tntList.getEntry(event.getLocation());
 		if(e != null)
 		{
 			ActiveDungeon ad = DragonsLairMain.getDungeonManager().getActiveDungeonByName(e.getDungeon());
 			if(ad == null)
 				return;
 			
 			List<Block> blocks = event.blockList();
 			for(Block b : blocks)
 			{
 				DragonsLairMain.getInstance().getLoggingManager().logBlockBreak(ad, b.getState());				
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onInteract(PlayerInteractEvent event)
 	{
 		Player p = event.getPlayer();
 		Action a = event.getAction();
 		if(a != Action.LEFT_CLICK_BLOCK && a != Action.RIGHT_CLICK_BLOCK && a != Action.PHYSICAL)
 			return;
 		
 		Block interactedBlock = event.getClickedBlock();
 		Location interacted = interactedBlock.getLocation();
 		if(!DragonsLairMain.isWorldEnabled(interacted.getWorld().getName()))
 			return;
 		
 		for(TriggerLocationEntry entry : this.locations)
 		{
 			if(entry.equals(interactedBlock.getLocation()))
 			{
 				for(Trigger t : entry.getTriggersForType(TriggerType.BLOCK_INTERACT))
 				{
 					String block_id = t.getOption("block_id");
 					if(block_id != null)
 					{
 						try
 						{
 							int id = Integer.parseInt(block_id);
 							Material m = Material.getMaterial(id);
 							if(m != null)
 							{
 								if(interactedBlock.getType() != m)
 									continue;
 							}
 						}
 						catch(Exception e)
 						{
 							Material m = Material.getMaterial(block_id.replace(" ", "_").toUpperCase());
 							if(m != null)
 							{
 								if(interactedBlock.getType() != m)
 									continue;
 							}
 						}
 					}
 					DragonsLairMain.getDungeonManager().callTrigger(t, p);
 				}
 			}
 		}
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onChapterChange(ChapterChangeEvent event)
 	{		
 		if(!this.triggers.containsKey(TriggerType.CHAPTER_CHANGE))
 			return;
 		
 		if(!DragonsLairMain.isWorldEnabled(Bukkit.getPlayer(event.getActiveDungeon().getCurrentParty().getMembers()[0]).getWorld().getName()))
 			return;
 		
 		int dungeon = event.getDungeon().getID();
 		int chapterid = event.getNextChapter().getID();
 		for(Trigger t : this.triggers.get(TriggerType.CHAPTER_CHANGE))
 		{
 			String d = t.getOption("dungeon_id");
 			String chapter = t.getOption("chapter_id");
 			if(d != null)
 			{
 				if(!d.equals(dungeon + ""))
 					continue;
 			}
 			
 			if(!chapter.equals(chapterid + ""))
 				continue;
 			
 			DragonsLairMain.getDungeonManager().callTrigger(t, null);
 		}
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onObjectiveChange(ObjectiveChangeEvent event)
 	{
 		if(!this.triggers.containsKey(TriggerType.OBJECTIVE_CHANGE))
 			return;
 		
 		if(!DragonsLairMain.isWorldEnabled(Bukkit.getPlayer(event.getActiveDungeon().getCurrentParty().getMembers()[0]).getWorld().getName()))
 			return;
 	
 		int dungeon = event.getDungeon().getID();
 		int objectiveid = event.getNextObjective().getID();
 		for(Trigger t : this.triggers.get(TriggerType.OBJECTIVE_CHANGE))
 		{
 			String d = t.getOption("dungeon_id");
 			String objective = t.getOption("objective_id");
 			if(d != null)
 			{
 				if(!d.equals(dungeon + ""))
 					continue;
 			}
 			
 			if(!objective.equals(objectiveid + ""))
 				continue;
 			
 			DragonsLairMain.getDungeonManager().callTrigger(t, null);
 		}
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onMapScroll(PlayerInteractEvent event)
 	{
 		Player p = event.getPlayer();
 		ItemStack current = p.getItemInHand();
 		if(current == null || current.getType() != Material.MAP)
 			return;
 
 		if(DragonsLairMain.getDungeonManager().getDungeonOfPlayer(p.getName()) == null)
 			return;
 		
 		DLMap map = DragonsLairMain.getDungeonManager().getMapOfPlayer(p);
 		if(map == null)
 			return;
 		
 		Action a = event.getAction();
 		if(a == Action.LEFT_CLICK_AIR || a == Action.LEFT_CLICK_BLOCK)
 			map.scrollUp();
 		else if(a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK)
 			map.scrollDown();
 		
 		event.setCancelled(true);
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onItemDrop(PlayerDropItemEvent event)
 	{
 		Player p = event.getPlayer();
 		ItemStack dropped = event.getItemDrop().getItemStack();
 		if(DragonsLairMain.getDungeonManager().getDungeonOfPlayer(p.getName()) == null)
 			return;
 		
 		if(dropped.getType() != Material.MAP)
 			return;
 		
 		if(dropped.getEnchantments().containsKey(Enchantment.ARROW_INFINITE))
 			event.setCancelled(true);
 	}
 	
 	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
 	public void onInventoryClick(InventoryClickEvent event)
 	{
 		Player p = (Player)event.getWhoClicked();
 		if(!DragonsLairMain.isWorldEnabled(p.getWorld().getName()))
 			return;
 		
 		ActiveDungeon ad = DragonsLairMain.getDungeonManager().getDungeonOfPlayer(p.getName());
 		if(ad == null)
 			return;
 		
 		if(event.getInventory().getType() == InventoryType.PLAYER || event.getInventory().getType() == InventoryType.ENCHANTING || event.getInventory().getType() == InventoryType.CRAFTING)
 			return;
 		
 		if(event.getRawSlot() == -999)
 			return;
 		
 		final BlockState holder = (BlockState)event.getInventory().getHolder();
 		
 		if(event.getView().getTopInventory().getSize() > event.getRawSlot())
 		{
 			if(event.getCursor().getType() != Material.AIR || event.getCurrentItem().getType() != Material.AIR)
 			{
 				Map<String, String> newItem = new HashMap<String, String>();
 				Map<String, String> oldItem = new HashMap<String, String>();
 				if(event.getCursor().getType() == Material.AIR)	
 					newItem.put("slot" + event.getSlot(), InventoryUtilities.itemToString(null));
 				else
 					oldItem.put("slot" + event.getSlot(), InventoryUtilities.itemToString(event.getCursor()));
 				
 				DragonsLairMain.getInstance().getLoggingManager().logBlockContentChange(ad, holder, newItem, oldItem);
 			}
 		}
 		else
 		{
 			if(event.isShiftClick())
 				DragonsLairMain.getInstance().getLoggingManager().logBlockContentChange(ad, holder, InventoryUtilities.getChangedStates(event.getView().getTopInventory(), (event.getCursor().getType() == Material.AIR ? event.getCurrentItem() : event.getCursor())), new HashMap<String, String>());
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerExit(PlayerQuitEvent event)
 	{
 		Player p = event.getPlayer();
 		ActiveDungeon ad = DragonsLairMain.getDungeonManager().getDungeonOfPlayer(p.getName());
 		if(ad == null)
 			return;
 		
 		DragonsLairMain.getDungeonManager().stopDungeon(ad.getInfo().getName());
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onPickup(PlayerPickupItemEvent event)
 	{
 		Player p = event.getPlayer();
 		if(!DragonsLairMain.isWorldEnabled(p.getWorld().getName()))
 			return;
 		
 		if(!this.triggers.containsKey(TriggerType.GATHER_ITEM))
 			return;
 		
 		Inventory i = p.getInventory();
 		ItemStack pickedUp = event.getItem().getItemStack();
 		
 		for(Trigger t : this.triggers.get(TriggerType.GATHER_ITEM))
 		{
 			Material m;
 			int amount;
 			int dungeonid;
 			try
 			{
 				m = Material.getMaterial(Integer.parseInt(t.getOption("item_id")));
 			}
 			catch(Exception e)
 			{
 				m = Material.getMaterial(t.getOption("item_id").replace(" ", "_").toUpperCase());
 			}
 			
 			if(m == null)
 				continue;
 			
 			try
 			{
 				amount = Integer.parseInt(t.getOption("amount"));
 			}
 			catch(Exception e)
 			{
 				continue;
 			}
 			
 			if(t.getOption("dungeon_id") == null)
 				dungeonid = -1;
 			else
 			{
 				try
 				{
 					dungeonid = Integer.parseInt(t.getOption("dungeon_id"));
 				}
 				catch(Exception e)
 				{
 					continue;
 				}
 			}
 			
 			if(dungeonid != -1)
 			{
 				ActiveDungeon ad = DragonsLairMain.getDungeonManager().getDungeonOfPlayer(p.getName());
 				if(ad == null)
 					continue;
 				
 				if(ad.getInfo().getID() != dungeonid)
 					continue;
 			}
 			
 			if(pickedUp.getType() == m)
 			{
 				if(pickedUp.getAmount() >= amount)
 					DragonsLairMain.getDungeonManager().callTrigger(t, p);
 				else
 				{
 					amount -= pickedUp.getAmount();
 					
 					if(i.contains(m))
 					{
 						HashMap<Integer, ? extends ItemStack> items = i.all(m);
 						for(ItemStack item : items.values())
 						{
 							if(item.getAmount() >= amount)
 								DragonsLairMain.getDungeonManager().callTrigger(t, p);
 							else
 								amount -= item.getAmount();
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onEntityDeath(EntityDeathEvent event)
 	{
 		if(!this.triggers.containsKey(TriggerType.MOBS_KILLED) && !this.triggers.containsKey(TriggerType.NPC_DEATH))
 			return;
 		
 		Player killer = event.getEntity().getKiller();
 		if(killer == null)
 			return;
 		
 		if(!DragonsLairMain.isWorldEnabled(killer.getWorld().getName()))
 			return;
 		
 		ActiveDungeon ad = DragonsLairMain.getDungeonManager().getDungeonOfPlayer(killer.getName());
 		if(ad == null)
 			return;
 		
 		LivingEntity e = event.getEntity();
 		EntityType type = e.getType();
 		
 		if(type == EntityType.COMPLEX_PART)
 		{
 			type = EntityType.ENDER_DRAGON;
 			e = ((EnderDragonPart)e).getParent();
 		}
 		
 		if(type == EntityType.PLAYER)
 		{
 			if(!this.triggers.containsKey(TriggerType.NPC_DEATH))
 				return;
 			
 			NPC n = DragonsLairMain.getSettings().getNPCByName(DragonsLairMain.getDungeonManager().getNPCByEntity(e).getName());
 			if(n != null)
 			{
 				for(Trigger t : this.triggers.get(TriggerType.NPC_DEATH))
 				{
 					if(Integer.parseInt(t.getOption("dungeon_id")) != ad.getInfo().getID())
 						continue;
 					
 					
 					if(!(n.getID() + "").equals(t.getOption("npc_id")))
 						continue;
 					
 					DragonsLairMain.getDungeonManager().callTrigger(t, killer);
 				}
 			}
 		}
 		else
 		{
 			if(!this.triggers.containsKey(TriggerType.MOBS_KILLED))
 				return;
 			
 			Event spawnEvent = DragonsLairMain.getDungeonManager().getEventFromMob(e);
 			int amount = DragonsLairMain.getDungeonManager().addMobKill(ad, e, spawnEvent);
 			for(Trigger t : this.triggers.get(TriggerType.MOBS_KILLED))
 			{
 				if(Integer.parseInt(t.getOption("amount")) > amount)
 					continue;
 				
 				if(Integer.parseInt(t.getOption("dungeon_id")) != ad.getInfo().getID())
 					continue;
 					
 				if(t.getOption("spawned_by") != null && Integer.parseInt(t.getOption("spawned_by")) != spawnEvent.getID())
 					continue;
 				
 				if(t.getOption("mob_id") != null)
 				{
 					try
 					{
 						int id = Integer.parseInt(t.getOption("mob_id"));
 						EntityType searchedType = EntityType.fromId(id);
 						if(searchedType != type)
 							continue;
 					}
 					catch(Exception ex)
 					{
 						EntityType searchedType = EntityType.fromName(t.getOption("mob_id"));
 						if(searchedType != type)
 							continue;
 					}
 				}
 				
 				DragonsLairMain.getDungeonManager().callTrigger(t, killer);
 			}
 		}
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onLevelChange(PlayerLevelChangeEvent event)
 	{
 		if(!this.triggers.containsKey(TriggerType.LEVEL_ACHIEVE))
 			return;
 		
 		Player p = event.getPlayer();
 		ActiveDungeon ad = DragonsLairMain.getDungeonManager().getDungeonOfPlayer(p.getName());
 		for(Trigger t : this.triggers.get(TriggerType.LEVEL_ACHIEVE))
 		{
 			int level = Integer.parseInt(t.getOption("amount"));
 			if(level == event.getNewLevel())
 			{
 				String dungeonid = t.getOption("dungeon_id");
 				if(dungeonid == null)
 				{
 					DragonsLairMain.getDungeonManager().callTrigger(t, p);
 				}
 				else
 				{
 					if(ad == null)
 						continue;
 					
 					try
 					{
 						int id = Integer.parseInt(dungeonid);
 						if(ad.getInfo().getID() == id)
 							DragonsLairMain.getDungeonManager().callTrigger(t, p);
 					}
 					catch(Exception e)
 					{
 						if(ad.getInfo().getName().equals(dungeonid))
 							DragonsLairMain.getDungeonManager().callTrigger(t, p);
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
 	public void onInteractLog(PlayerInteractEvent event)
 	{
 		Player p = event.getPlayer();
 		Action a = event.getAction();
 		
 		if(a == Action.LEFT_CLICK_AIR || a == Action.RIGHT_CLICK_AIR)
 			return;
 		
 		if(a == Action.LEFT_CLICK_BLOCK && p.getGameMode() == GameMode.CREATIVE)
 			return;
 		
 		Block clicked = event.getClickedBlock();
 		
 		if(!DragonsLairMain.isWorldEnabled(clicked.getWorld().getName()))
 			return;
 		
 		ActiveDungeon ad = DragonsLairMain.getDungeonManager().getDungeonOfPlayer(p.getName());
 		if(ad == null)
 			return;
 		
 		DragonsLairMain.getInstance().getLoggingManager().logBlockDataChange(ad, clicked.getState());
 	}
 	
 	
 	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
 	public void onPlayerCraft(CraftItemEvent event)
 	{
 		Material outcome = event.getRecipe().getResult().getType();
 		
 		for(Trigger t : this.triggers.get(TriggerType.ITEM_CRAFT))
 		{
 			Material m;
 			try
 			{
 				m = Material.getMaterial(Integer.parseInt(t.getOption("item_id")));
 			}
 			catch(Exception e)
 			{
 				m = Material.getMaterial(t.getOption("item_id").replace(" ", "_").toUpperCase());
 			}
 			
 			if(m == outcome)
 				DragonsLairMain.getDungeonManager().callTrigger(t, (Player)event.getWhoClicked());
 		}
 	}
 }
