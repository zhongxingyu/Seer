 package demos.sweeper;
 
 import vitro.*;
 import vitro.grid.*;
 import vitro.util.*;
 import java.util.*;
 
 public class Sweeper extends Grid {
 	public final Player player = new Player();
 
 	public final Set<Location> hidden = new HashSet<Location>();
 
 	public Sweeper(int width, int height, int numMines) {
 		super(width, height);
 		actors.add(player);
 
 		hidden.addAll(allCells());
 		generateMines(numMines);
 	}
 
 	protected void generateMines(int numMines) {
 		for(Actor actor : actors) {
 			if(actor instanceof Mine) { actors.remove(actor); }
 		}
 
 		Random rnd = new Random();
 		for(int m = 0; m < numMines; m++) {
 			int pos = rnd.nextInt(hidden.size() - m);
 
 			Iterator<Location> iter = hidden.iterator();
 			for(int l = 0; l < pos - 1; l++) {
 				iter.next();
 			}
 
 			Location toPlace = iter.next();
 			while(Groups.containsType(Mine.class, actorsAt(toPlace))) {
 				toPlace = iter.next();
 			}
 
 			locations.put(new Mine(), toPlace);
 		}
 	}
 
 	public boolean done() {
 		for(Actor actor : Groups.ofType(Mine.class, actors)) {
 			if(locations.get(actor) != null && !hidden.contains(locations.get(actor))) { return true; }
 		}
 
 		return false;
 	}
 
 	public int count(Location location) {
 		if(Groups.containsType(Mine.class, actorsAt(location))) { return -1; }
 		return Groups.ofType(Mine.class, actorsAt(neighbors(location, ADJACENT))).size();
 	}
 
 
 	public class Mine extends Actor { }
 
 	public class Player extends Actor {
 
 
 		public Set<Action> actions() {
 			Set<Action> ret = super.actions();
 			for(Location location : hidden) {
 				ret.add(new FlipAction(location));
 			}
 			return ret;
 		}
 	}
 
 	public class FlipAction implements Action {
 		public final Location location;
 
 		private Set<Location> cascade = null;
 
 		public FlipAction(Location location) {
 			this.location = location;
 		}
 
 		public void apply() {
 			if(cascade == null) {
 				cascade = new HashSet<Location>();
 
 				// pumping empties
 				Set<Location> toAdd = new HashSet<Location>();
 				toAdd.add(location);
 
 				while(!toAdd.isEmpty()) {
 					cascade.addAll(toAdd);
 					toAdd.clear();
 
 					for(Location location : cascade) {
 						if(count(location) == 0) {
 							for(Location neighbor : neighbors(location, ADJACENT)) {
 								toAdd.add(neighbor);
 							}
 						}
 					}
 					toAdd.removeAll(cascade);
 				}
 			}
 
 			hidden.removeAll(cascade);
 		}
 
 		public void undo() {
 			hidden.addAll(cascade);
 		}
 	}
 }
