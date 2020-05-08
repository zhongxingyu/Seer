 package com.mcnsa.instanceportals.managers;
 
 import java.util.HashMap;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 import com.mcnsa.instanceportals.InstancePortals;
 import com.mcnsa.instanceportals.containers.Instance;
 import com.mcnsa.instanceportals.containers.InstanceSet;
 import com.mcnsa.instanceportals.containers.Portal;
 import com.mcnsa.instanceportals.containers.PortalRegion;
 import com.mcnsa.instanceportals.util.ColourHandler;
 
 public class PlayerManager {
 	private InstancePortals plugin;
 	private HashMap<Player, Portal> definingPortals = new HashMap<Player, Portal>();
 	private HashMap<Player, InstanceSet> definingInstanceSets = new HashMap<Player, InstanceSet>();
 	private HashMap<Player, Instance> definingInstances = new HashMap<Player, Instance>();
 	
 	public PlayerManager(InstancePortals instance) {
 		plugin = instance;
 	}
 	
 	public boolean onBlockSelect(Player player, Block block) {
 		// make sure we're tracking the player
 		if(definingPortals.containsKey(player)) {
 			if(definingPortals.get(player).entrance.nextIsMax()) {
 				// change the max point!
 				definingPortals.get(player).entrance.updateMax(block.getLocation());
 			}
 			else {
 				// change the min point!
 				definingPortals.get(player).entrance.updateMin(block.getLocation());
 			}
 			ColourHandler.sendMessage(player, "&a(&f"+block.getLocation().getBlockX()+"&a,&f"+block.getLocation().getBlockY()+"&a,&f"+block.getLocation().getBlockZ()+"&a) has been registered!");
 			
 			// see if the portal region is fully defined
 			if(definingPortals.get(player).entrance.portalDefined()) {
 				ColourHandler.sendMessage(player, "&2Your portal entrance is now fully defined!");
 				
 				// now reorder the min and max
 				definingPortals.get(player).entrance.properlyOrder();
 				
 				// see if they're completely done with their portal
 				if(definingPortals.get(player).exit != null) {
 					ColourHandler.sendMessage(player, "&2Your portal now fully defined! Type /pdone to save and activate it now!");
 				}
 			}
 			
 			return true;
 		}
 		else if(definingInstances.containsKey(player)) {
 			// make sure it's allowed
 			if(!definingInstanceSets.containsKey(player)) {
 				ColourHandler.sendMessage(player, "&4Error - you have to start defining an instance set first!");
 				return true;
 			}
 			
 			// they're defining an instance
 			/*if(definingInstances.get(player).departure.nextIsMax()) {
 				// change the max point!
 				definingInstances.get(player).departure.updateMax(block.getLocation());
 			}
 			else {
 				// change the min point!
 				definingInstances.get(player).departure.updateMin(block.getLocation());
 			}
 			ColourHandler.sendMessage(player, "&a(&f"+block.getLocation().getBlockX()+"&a,&f"+block.getLocation().getBlockY()+"&a,&f"+block.getLocation().getBlockZ()+"&a) has been registered!");
 			
 			if(definingInstances.get(player).departure.portalDefined()) {
 				ColourHandler.sendMessage(player, "&2Your portal entrance is now fully defined!");
 				
 				// now reorder the min and max
 				definingInstances.get(player).departure.properlyOrder();
 			}*/
 			
 			// see what they're defining
 			if(!definingInstances.get(player).container.portalDefined()) {
 				// need to define the container..
 				if(definingInstances.get(player).container.nextIsMax()) {
 					// change the max point!
 					definingInstances.get(player).container.updateMax(block.getLocation());
 				}
 				else {
 					// change the min point!
 					definingInstances.get(player).container.updateMin(block.getLocation());
 				}
 				ColourHandler.sendMessage(player, "&a(&f"+block.getLocation().getBlockX()+"&a,&f"+block.getLocation().getBlockY()+"&a,&f"+block.getLocation().getBlockZ()+"&a) has been registered as a corner of your container!");
 			}
 			else if(!definingInstances.get(player).departure.portalDefined()) {
 				// need to define the portal
 				if(definingInstances.get(player).departure.nextIsMax()) {
 					// change the max point!
 					definingInstances.get(player).departure.updateMax(block.getLocation());
 				}
 				else {
 					// change the min point!
 					definingInstances.get(player).departure.updateMin(block.getLocation());
 				}
 				ColourHandler.sendMessage(player, "&a(&f"+block.getLocation().getBlockX()+"&a,&f"+block.getLocation().getBlockY()+"&a,&f"+block.getLocation().getBlockZ()+"&a) has been registered as a corner of your portal!");
 			}
 			else {
 				// ok, they have it all defined!
 				ColourHandler.sendMessage(player, "&2Your portal entrance and container are now fully defined!");
 				
 				// now reorder the min and max
 				definingInstances.get(player).container.properlyOrder();
 				definingInstances.get(player).departure.properlyOrder();
 			}
 			
 			return true;
 		}
 		else if(definingInstanceSets.containsKey(player)) {
 			// they're defining an instance set entrance
 			if(definingInstanceSets.get(player).entrance.nextIsMax()) {
 				// change the max point!
 				definingInstanceSets.get(player).entrance.updateMax(block.getLocation());
 			}
 			else {
 				// change the min point!
 				definingInstanceSets.get(player).entrance.updateMin(block.getLocation());
 			}
 			ColourHandler.sendMessage(player, "&a(&f"+block.getLocation().getBlockX()+"&a,&f"+block.getLocation().getBlockY()+"&a,&f"+block.getLocation().getBlockZ()+"&a) has been registered!");
 			
 			if(definingInstanceSets.get(player).entrance.portalDefined()) {
 				ColourHandler.sendMessage(player, "&2Your portal entrance is now fully defined!");
 				
 				// now reorder the min and max
 				definingInstanceSets.get(player).entrance.properlyOrder();
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	
 	/**************** Portals ****************/
 	
 	public boolean playerDefiningPortal(Player player) {
 		return definingPortals.containsKey(player);
 	}
 	
 	public void startPortalDefinition(Player player, String name) {
 		// firt make sure that portal doesn't exist
 		if(plugin.transportManager.portalExists(name)) {
 			ColourHandler.sendMessage(player, "&4Error - a portal already exists with that name!");
 			return;
 		}
 		if(definingPortals.containsKey(player)) {
 			ColourHandler.sendMessage(player, "&4Error - you're already defining a portal!");
 			return;
 		}
 		
 		Portal portal = new Portal(plugin, name);
 		portal.entrance = new PortalRegion(plugin, player);
 		definingPortals.put(player, portal);
 		
 		// and inform them
 		ColourHandler.sendMessage(player, "&3To define the entrance portal, right click on the two corners of your portal");
 		ColourHandler.sendMessage(player, "&3To define where your portal will go to, stand in that spot and type &f/pexit");
 		ColourHandler.sendMessage(player, "&3When you're done, type &f/pdone&3. To cancel at any time, type &f/pcancel");
 	}
 	
 	public void definePortalExit(Player player) {
 		// make sure we're tracking the player
 		if(!definingPortals.containsKey(player)) {
 			return;
 		}
 		
 		definingPortals.get(player).exit = player.getLocation();
 		ColourHandler.sendMessage(player, "&2Your portal exit is now defined!");
 		if(definingPortals.get(player).entrance.portalDefined()) {
 			ColourHandler.sendMessage(player, "&2Your portal now fully defined! Type /pdone to save and activate it now!");
 		}
 	}
 	
 	public void endPortalDefinition(Player player) {
 		// make sure we're tracking the player
 		if(!definingPortals.containsKey(player)) {
 			return;
 		}
 		
 		// check to make sure it's been fully defined
 		if(!definingPortals.get(player).entrance.portalDefined()) {
 			ColourHandler.sendMessage(player, "&4Error - your portal entrance is not fully defined!");
 			return;
 		}
 		if(definingPortals.get(player).exit == null) {
 			ColourHandler.sendMessage(player, "&4Error - your portal exit is not defined!");
 			return;
 		}
 		
 		// ok, it's defined! add it to the main portal list
 		plugin.transportManager.addPortal(definingPortals.get(player));
 		
 		// notify them
 		ColourHandler.sendMessage(player, "&2Success, your portal ("+definingPortals.get(player).name+") is now implemented!");
 		
 		//save all the current portals
 		plugin.persistanceManager.writePersistance();
 		
 		// and stop tracking it
 		definingPortals.remove(player);
 	}
 	
 	public void cancelPortalDefinition(Player player) {
 		// make sure we're tracking the player
 		if(!definingPortals.containsKey(player)) {
 			return;
 		}
 		
 		// and stop tracking it
 		definingPortals.remove(player);
 		
 		// and tell them about it!
 		ColourHandler.sendMessage(player, "&2You are no longer creating a portal!");
 	}
 	
 	
 	/**************** Instances ****************/
 	
 	
 	public boolean playerDefiningInstance(Player player) {
 		return definingInstances.containsKey(player);
 	}
 	
 	public void startInstanceDefinintion(Player player) {
 		if(definingInstances.containsKey(player)) {
 			ColourHandler.sendMessage(player, "&4Error - you're already defining an instance!");
 			return;
 		}
 		if(!definingInstanceSets.containsKey(player)) {
 			ColourHandler.sendMessage(player, "&4Error - you have to start defining an instance set first!");
 			return;
 		}
 		
 		// create the instance
 		Instance instance = new Instance(definingInstanceSets.get(player));
 		instance.container = new PortalRegion(plugin, player);
 		instance.departure = new PortalRegion(plugin, player);
 		definingInstances.put(player, instance);
 		
 		// and alert them
 		ColourHandler.sendMessage(player, "&3To define the start of this instance, stand in the spot and type &f/ienter");
 		ColourHandler.sendMessage(player, "&3To define the reset container of this instance, right click on the two corners of your instance container");
 		ColourHandler.sendMessage(player, "&3To define the exit portal of this instance, right click on the two corners of your portal");
 		ColourHandler.sendMessage(player, "&3When you're done, type &f/idone&3. To cancel at any time, type &f/icancel");
 	}
 	
 	public void defineInstanceArrival(Player player) {
 		// make sure we're tracking the player
 		if(!definingInstances.containsKey(player)) {
 			return;
 		}
 		// make sure it's allowed
 		if(!definingInstanceSets.containsKey(player)) {
 			ColourHandler.sendMessage(player, "&4Error - you have to start defining an instance set first!");
 			return;
 		}
 		
 		definingInstances.get(player).arrival = player.getLocation();
 		ColourHandler.sendMessage(player, "&2Your instance arrival is now defined!");
 	}
 	
 	public void endInstanceDefinition(Player player) {
 		// make sure we're tracking the player
 		if(!definingInstances.containsKey(player)) {
 			return;
 		}
 		// make sure it's allowed
 		if(!definingInstanceSets.containsKey(player)) {
 			ColourHandler.sendMessage(player, "&4Error - you have to start defining an instance set first!");
 			return;
 		}
 		
 		// check to make sure it's been fully defined
 		if(!definingInstances.get(player).container.portalDefined()) {
 			ColourHandler.sendMessage(player, "&4Error - your instance container is not fully defined!");
 			return;
 		}
 		if(!definingInstances.get(player).departure.portalDefined()) {
 			ColourHandler.sendMessage(player, "&4Error - your departure portal is not fully defined!");
 			return;
 		}
 		if(definingInstances.get(player).arrival == null) {
 			ColourHandler.sendMessage(player, "&4Error - your instance arrival is not defined!");
 			return;
 		}
 		
 		// make sure the portals are properly ordered
 		definingInstances.get(player).departure.properlyOrder();
 		definingInstances.get(player).container.properlyOrder();
 		
 		// ok, it's defined! add it to the instance set
 		definingInstanceSets.get(player).addInstance(definingInstances.get(player));
 		
 		// notify them
 		ColourHandler.sendMessage(player, "&2Success, your instance has been added to the instance set: &f" + definingInstanceSets.get(player).name);
 		
 		// and stop tracking it
 		definingInstances.remove(player);
 	}
 	
 	public void cancelInstanceDefinition(Player player) {
 		// make sure we're tracking the player
 		if(!definingPortals.containsKey(player)) {
 			return;
 		}
 		// make sure it's allowed
 		if(!definingInstanceSets.containsKey(player)) {
 			ColourHandler.sendMessage(player, "&4Error - you have to start defining an instance set first!");
 			return;
 		}
 		
 		// and stop tracking it
 		definingInstances.remove(player);
 		
 		// and tell them about it!
 		ColourHandler.sendMessage(player, "&2You are no longer creating an instance!");
 	}
 	
 	
 	/**************** Instance Sets ****************/
 
 	
 	public boolean playerDefiningInstanceSet(Player player) {
 		return definingInstanceSets.containsKey(player);
 	}
 	
 	public void startInstanceSetDefinition(Player player, String name, Integer max) {
 		// firt make sure that portal doesn't exist
 		if(plugin.transportManager.instanceSetExists(name)) {
 			ColourHandler.sendMessage(player, "&4Error - an instance set already exists with that name!");
 			return;
 		}
 		if(definingInstanceSets.containsKey(player)) {
 			ColourHandler.sendMessage(player, "&4Error - you're already defining an instance set!");
 			return;
 		}
 		
 		// create the instance set
 		InstanceSet set = new InstanceSet(plugin, name, max);
 		set.entrance = new PortalRegion(plugin, player);
 		definingInstanceSets.put(player, set);
 		
 		// and inform them
 		ColourHandler.sendMessage(player, "&3To define the entrance portal, right click on the two corners of your portal");
 		ColourHandler.sendMessage(player, "&3To define the exit of this instance set, stand where you want it and type &f/isexit");
 		ColourHandler.sendMessage(player, "&3To define the boot location for this instance set, stand where you want it and type &f/isboot");
 		ColourHandler.sendMessage(player, "&3Then to create an instance hooked into this set using &f/icreate");
 		ColourHandler.sendMessage(player, "&3When you're done, type &f/isdone&3. To cancel at any time, type &f/iscancel");
 	}
 	
 	public void defineInstanceSetExit(Player player) {
 		// make sure we're tracking the player
 		if(!definingInstanceSets.containsKey(player)) {
 			return;
 		}
 		
 		definingInstanceSets.get(player).exit = player.getLocation();
 		ColourHandler.sendMessage(player, "&2Your instance exit is now defined!");
 	}
 	
 	public void defineInstanceSetBoot(Player player) {
 		// make sure we're tracking the player
 		if(!definingInstanceSets.containsKey(player)) {
 			return;
 		}
 		
 		definingInstanceSets.get(player).bootEntrance = player.getLocation();
 		ColourHandler.sendMessage(player, "&2Your instance boot location is now defined!");
 	}
 	
 	public void endInstanceSetDefinition(Player player) {
 		// make sure we're tracking the player
 		if(!definingInstanceSets.containsKey(player)) {
 			return;
 		}
 		
 		// check to make sure it's been fully defined
 		if(!definingInstanceSets.get(player).entrance.portalDefined()) {
 			ColourHandler.sendMessage(player, "&4Error - your entrance portal is not fully defined!");
 			return;
 		}
 		if(definingInstanceSets.get(player).exit == null) {
 			ColourHandler.sendMessage(player, "&4Error - your instance set exit is not defined!");
 			return;
 		}
 		if(definingInstanceSets.get(player).bootEntrance == null) {
 			ColourHandler.sendMessage(player, "&4Error - your boot location is not defined!");
 			return;
 		}
 		if(definingInstanceSets.get(player).instances.size() < 1) {
 			ColourHandler.sendMessage(player, "&4Error - your instance set doesn't have any instances defined!");
 			return;
 		}
 		
 		// ok, it's defined! add it to the transport manager
 		plugin.transportManager.addInstanceSet(definingInstanceSets.get(player));
 		
 		// notify them
 		ColourHandler.sendMessage(player, "&2Success, your instance set (&f"+definingInstanceSets.get(player).name+"&2) has been added!");
 		
		//save all the current instances
		plugin.persistanceManager.writePersistance();
		
 		// and stop tracking it
 		if(definingInstances.containsKey(player)) {
 			definingInstances.remove(player);
 		}
 		definingInstanceSets.remove(player);
 	}
 	
 	public void cancelInstanceSetDefinition(Player player) {
 		// make sure we're tracking the player
 		if(!definingInstanceSets.containsKey(player)) {
 			return;
 		}
 		
 		// and stop tracking it
 		if(definingInstances.containsKey(player)) {
 			definingInstances.remove(player);
 		}
 		definingInstanceSets.remove(player);
 		
 		// and tell them about it!
 		ColourHandler.sendMessage(player, "&2You are no longer creating an instance set!");
 	}
 }
