 package demos;
 
 import vitro.util.*;
 import vitro.model.*;
 import vitro.model.graph.*;
 import vitro.controller.*;
 import java.util.*;
 import static vitro.util.Groups.*;
 
 public class WumpusBrain implements Agent<WumpusWorld.Hunter> {
 
 	private static class Room {
 		public Set<Room> adjacent = new HashSet<Room>();
 		public boolean flapping = false;
 		public boolean wind     = false;
 		public boolean scent    = false;
 		public boolean visited  = false;
 	}
 
 	private final ReversibleMap<Node, Room> worldToPrivate = new ReversibleMap<Node, Room>();
 	private final ReversibleMap<Room, Node> privateToWorld = worldToPrivate.reverse();
 
 	public Action choose(WumpusWorld.Hunter me, Set<Action> options) {
 
 		// we're in an unknown place, so start building a graph
 		// and clear any path we may have plotted.
 		if (!worldToPrivate.containsKey(me.location())) {
 			worldToPrivate.put(me.location(), new Room());
 		}
 
 		// update our map with local information:
 		Room here = worldToPrivate.get(me.location());
 		here.flapping = me.flapping();
 		here.wind     = me.wind();
 		here.scent    = me.scent();
 		here.visited  = true;
 			
 		// build links to adjacent rooms:
 		for(Edge edge : me.location().edges) {
 			if (!worldToPrivate.containsKey(edge.end)) {
 				worldToPrivate.put(edge.end, new Room());
 			}
 			here.adjacent.add(worldToPrivate.get(edge.end));
			worldToPrivate.get(edge.end).adjacent.add(here);
 		}
 
 		// find likely wumpus locations:
 		for(Room room : privateToWorld.keySet()) {
 			if (room.visited) { continue; }
 			int wumpusCounter = 0;
 			for(Room other : room.adjacent) {
 				if (other.scent) { wumpusCounter++; }
 			}
 			if (wumpusCounter >= 2) {
 				Node wumpusPosition = privateToWorld.get(room);
 				// if we're adjacent to a likely Wumpus, shoot it!
 				if (here.adjacent.contains(room)) {
 					return me.create(wumpusPosition, WumpusWorld.Arrow.class, options);
 				}
 				// otherwise, move into position:
 				return me.moveToward(wumpusPosition, options);
 			}
 		}
 
 		// find safe exploration locations:
 		for(Room room : privateToWorld.keySet()) {
 			if (room.flapping || room.wind || room.scent) { continue; }
 			for(Room other : room.adjacent) {
 				if (!other.visited) {
 					return me.moveToward(privateToWorld.get(room), options);
 				}
 			}
 		}
 
 		// find likely bat locations:
 		for(Room room : privateToWorld.keySet()) {
 			if (room.visited) { continue; }
 			int batCounter = 0;
 			for(Room other : room.adjacent) {
 				if (other.flapping) { batCounter++; }
 			}
 			if (batCounter >= 2) {
 				return me.moveToward(privateToWorld.get(room), options);
 			}
 		}
 
 		// give up and walk randomly:
 		return any(ofType(MoveAction.class, options));
 	}
 }
