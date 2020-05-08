 package ui.isometric.abstractions;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import ui.isometric.IsoInterface;
 import game.Container;
 import game.GameThing;
 import game.GameWorld;
 import game.Level;
 import game.Location;
 import game.WorldDelta;
 import game.things.EquipmentGameThing;
 
 /**
  * Wrapper around the needlessly complex methods for getting info about the current player
  * 
  * @author melby
  *
  */
 public class IsoPlayer {
 	private Container inventory;
 	private Container equipment;
 	private Container openContainer;
 	private String containerName;
 	private GameWorld world;
 	private GameThing thing;
 	private String name;
 	private Set<ShowContainerListener> listeners = new HashSet<ShowContainerListener>();
 	
 	/**
 	 * An interface for receiving notifications about showing containers
 	 * 
 	 * @author ruarusmelb
 	 *
 	 */
 	public static interface ShowContainerListener {
 		/**
 		 * A given container has been requested to be shown
 		 * @param which
 		 */
 		void showContainer(Container show);
 
 		/**
 		 * Hide a given container
 		 * @param openContainer
 		 */
 		void hideContainer(Container openContainer);
 	}
 	
 	/**
 	 * Create an IsoPlayer with a given world, player GameThing, interface and character name
 	 * @param world
 	 * @param thing
 	 * @param inter
 	 * @param name - the character, not user name
 	 */
 	public IsoPlayer(final GameWorld world, GameThing thing, IsoInterface inter, String name) {
 		this.world = world;
 		this.thing = thing;
 		this.name = name;
 		
 		world.addDeltaWatcher(new GameWorld.DeltaWatcher() {
 			@Override
 			public void delta(WorldDelta delta) {
 				WorldDelta.Action action = delta.action();
 				if(action instanceof WorldDelta.ShowContainer){
 					WorldDelta.ShowContainer show = (WorldDelta.ShowContainer)action;
 					if(show.what().equals("Inventory")) {
 						inventory = show.which(world);
 					}
 					else if(show.what().equals("Equipment")) {
 						equipment = show.which(world);
 					}
 					else {
 						containerName = show.what();
 						openContainer = show.which(world);
 						
 						for(ShowContainerListener l : listeners) {
 							l.showContainer(openContainer);
 						}
 					}
 				}
 				if(action instanceof WorldDelta.HideContainer) {
 					if(((WorldDelta.HideContainer)action).which(world).equals(openContainer)) {
 						for(ShowContainerListener l : listeners) {
 							l.hideContainer(openContainer);
 						}
 						
 						containerName = "";
 						openContainer = null;
 					}
 				}
 			}
 		});
 		
 		inter.performActionOn("_showinventory", thing);
 		inter.performActionOn("_showequipment", thing);
 	}
 	
 	/**
 	 * This players inventory
 	 * @return
 	 */
 	public Container inventory() {
 		return inventory;
 	}
 	
 	/**
 	 * Get the GameThing for a given slot
 	 * @param slot
 	 * @return
 	 */
 	public GameThing getEquipmentForSlot(EquipmentGameThing.Slot slot) {
 		if(equipment != null) {
 			for(GameThing g : equipment) {
 				String value = g.info().get(EquipmentGameThing.SLOT);
 				if(value != null) {
 					EquipmentGameThing.Slot got = EquipmentGameThing.Slot.valueOf(value);
 					if(got.equals(slot)) {
 						return g;
 					}
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * This players equipment
 	 * @return
 	 */
 	public Container equipment() {
 		return equipment;
 	}
 	
 	/**
 	 * This players open container
 	 * @return
 	 */
 	public Container openContainer() {
 		return openContainer;
 	}
 	
 	/**
 	 * The raw GameThing that backs this
 	 * @return
 	 */
 	public GameThing thing() {
 		return thing;
 	}
 	
 	/**
 	 * The world this player is in
 	 * @return
 	 */
 	public GameWorld world() {
 		return world;
 	}
 
 	/**
 	 * Get the name of the character this player is using
 	 * @return
 	 */
 	public String characterName() {
 		return name;
 	}
 	
 	/**
 	 * Add a ShowContainerListener
 	 * @param l
 	 */
 	public void addShowContainerListener(ShowContainerListener l) {
 		listeners.add(l);
 	}
 	
 	/**
 	 * Remove a given ShowContainerListener
 	 * @param l
 	 */
 	public void removeShowContainerListener(ShowContainerListener l) {
 		listeners.remove(l);
 	}
 	
 	/**
 	 * Get the location of this player, throws a RuntimeException if this doesn't exist
 	 * @return
 	 */
 	public Level.Location location() {
 		Location l = thing.location();
 		if(l instanceof Level.Location) {
 			return (Level.Location)l;
 		}
 		else {
 			throw new RuntimeException("No Level.Location for Player");
 		}
 	}
 
 	/**
 	 * Get weather this player is a light.
 	 * This is equivalent to is the player on a dark level.
 	 * @return
 	 */
 	public boolean isLight() {
 		return thing.info().get("luminance") != null;
 	}
 
 	/**
 	 * Get the open container name
 	 * @return
 	 */
 	public String containerName() {
		if (containerName == null) return "";
 		return containerName;
 	}
 }
