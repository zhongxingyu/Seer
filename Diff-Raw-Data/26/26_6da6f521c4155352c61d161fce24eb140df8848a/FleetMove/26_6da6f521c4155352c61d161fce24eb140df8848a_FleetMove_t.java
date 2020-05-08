 package riskyspace.logic;
 
 import java.util.Map;
 import java.util.Set;
 
 import riskyspace.model.Fleet;
 import riskyspace.model.Player;
 import riskyspace.model.Position;
 import riskyspace.model.World;
 import riskyspace.services.Event;
 import riskyspace.services.EventBus;
 
 public class FleetMove {
 	
	private static boolean moving = false;
 	private static Object lock = new Object();
 	private static Thread mover = null;
 	
 	public static void interrupt() {
 		synchronized (lock) {
			moving = false;
 		}
 	}
 	
 	public static synchronized void move(final World world, final Map<Fleet, Path> fleetPaths, final Player player) {
		moving = true;
 		final Set<Fleet> fleets = fleetPaths.keySet();
 		Runnable runner = new Runnable() {
 			@Override
 			public void run() {
				while(!checkIfDone(fleetPaths, player) && moving) {
 					synchronized(lock) {
 						for (Fleet fleet : fleets) {
 							if (fleet.getOwner().equals(player) && fleetPaths.get(fleet).getLength() > 0 && fleet.useEnergy()) {
 								if (world.getTerritory(fleetPaths.get(fleet).getCurrentPos()).getFleets().contains(fleet)) {
 									world.getTerritory(fleetPaths.get(fleet).getCurrentPos()).getFleets().remove(fleet);
 									Event evt = new Event(Event.EventTag.TERRITORY_CHANGED, world.getTerritory(fleetPaths.get(fleet).getCurrentPos()));
 									EventBus.INSTANCE.publish(evt);
 									world.getTerritory(fleetPaths.get(fleet).step()).addFleet(fleet);
 									evt = new Event(Event.EventTag.TERRITORY_CHANGED, world.getTerritory(fleetPaths.get(fleet).getCurrentPos()));
 									EventBus.INSTANCE.publish(evt);
 								}
 							}
 						}
 					}
 					try {
 						Thread.sleep(100);
 					} catch (InterruptedException e) {}
 				}
				moving = false;
 				EventBus.INSTANCE.publish(new Event(Event.EventTag.MOVES_COMPLETE, null));
 			}
 		};
 		mover = new Thread(runner);
 		mover.start();
 	}
 	
 	/**
 	 * Returns if there are currently fleets being moved.
 	 */
 	public static boolean isMoving() {
		return moving;
 	}
 
 	private static boolean checkIfDone(Map<Fleet, Path> fleetPaths, Player player) {
 		Set<Fleet> fleets = fleetPaths.keySet();
 		boolean done = true;
 		for (Fleet fleet : fleets) {
			if (fleet.getOwner().equals(player)) {
				if (fleet.hasEnergy() && fleetPaths.get(fleet).getLength() > 0) {
					done = false;
				}
 			}
 		}
 		return done;
 	}
 }
