 package org.rsbot.script.web;
 
 import org.rsbot.script.methods.MethodContext;
 import org.rsbot.script.methods.MethodProvider;
 import org.rsbot.script.web.methods.TeleportTab;
 import org.rsbot.script.wrappers.RSTile;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * The class that handles all activities.
  *
  * @author Timer
  */
 public class TransportationHandler extends MethodProvider {
 	private List<Teleport> teleports = new ArrayList<Teleport>();
 
 	public TransportationHandler(final MethodContext ctx) {
 		super(ctx);
 		Tablets tablets = new Tablets();
 		teleports.add(tablets.CAMELOT);
 		teleports.add(tablets.VARROCK);
 	}
 
 	public boolean canPreform(final RSTile destination) {
 		Iterator<Teleport> teleportIterator = teleports.listIterator();
 		while (teleportIterator.hasNext()) {
 			Teleport teleport = teleportIterator.next();
 			if (teleport.meetsPrerequisites() && teleport.isApplicable(methods.players.getMyPlayer().getLocation(), destination)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean preform(final RSTile destination) {
 		Teleport bestTeleport = null;
 		double dist = 0.0D;
 		Iterator<Teleport> teleportIterator = teleports.listIterator();
 		while (teleportIterator.hasNext()) {
 			Teleport teleport = teleportIterator.next();
 			if (teleport.meetsPrerequisites() && teleport.isApplicable(methods.players.getMyPlayer().getLocation(), destination)) {
				if (dist == 0.0D || dist < teleport.getDistance(destination)) {
 					dist = teleport.getDistance(destination);
 					bestTeleport = teleport;
 				}
 			}
 		}
 		if (bestTeleport != null) {
 			return bestTeleport.preform();
 		}
 		return false;
 	}
 
 	private class Tablets {
 		public final TeleportTab VARROCK = new TeleportTab(methods, 8007, new RSTile(3212, 3428, 0));
 		public final TeleportTab CAMELOT = new TeleportTab(methods, 8010, new RSTile(2757, 3478, 0));
 	}
 }
