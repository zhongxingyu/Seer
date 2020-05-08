 import ohm.roth.lbs.Waypoint;
 import ohm.roth.lbs.distance;
 
 import java.util.ArrayList;
 import java.util.List;
 
 // Klasse f�r das Traveling Salesman Problem
 // TODO: BruteForce
 // TODO: Optimierte Brute Force
 
 public class TSP {
     public static enum tspType {
         ORGINAL,
         BRUTE_FORCE,
 		CLOSEST_NEIGHBOR
 
 	}
 	private tspType selectedType;
 
 	public TSP(tspType selectedType) {
 		super();
 		this.selectedType = selectedType;
 	}
 
 	public tspType getSelectedType() {
 		return selectedType;
 	}
 
 	public void setSelectedType(tspType selectedType) {
 		this.selectedType = selectedType;
 	}
 
 	public List<Waypoint> sort(List<Waypoint> places) {
 		switch (this.selectedType) {
 		case CLOSEST_NEIGHBOR:
 			return this.closestNeighbor(places);
         case BRUTE_FORCE:
             return this.bruteForce(places);
         case ORGINAL:
 			return places;
 		default:
 			return null;
 		}
 	}
 
     private List<Waypoint> closestNeighbor(List<Waypoint> places) {
         List<Waypoint> workingCopy = places;
 		List<Waypoint> orderdPlaces = new ArrayList<Waypoint>();
 		Waypoint curr = workingCopy.get(0);
 		Waypoint candy;
 		workingCopy.remove(0);
 		do {
 			candy = null;
 			for (Waypoint currTest : workingCopy) {
 				if (candy == null) {
 					candy = currTest;
 					continue;
 				}
 				if (distance.calcDist(candy, curr) > distance.calcDist(currTest, curr)) {
 					candy = currTest;
 				}
 			}
 			orderdPlaces.add(candy);
 			workingCopy.remove(candy);
 		} while (workingCopy.size() > 0);
 		return orderdPlaces;
 	}
 
     private void testPath(List<Waypoint> best, List<Waypoint> source, List<Waypoint> curr, Waypoint start) {
         if (curr.size() == (source.size() + 1) && best.size() != (source.size() + 1)) {
             best.addAll(curr);
             return;
         }
         // neuer bester pfasd
         if (curr.size() == (source.size() + 1) && best.size() == (source.size() + 1) && distance.listLength(best) > distance.listLength(curr)) {
             best.clear();
             best.addAll(curr);
             return;
         }
         if (curr.size() == source.size()) {
             curr.add(start);
             testPath(best, source, curr, start);
         }
 
         for (Waypoint currWaypoint : source) {
             if (curr.contains(currWaypoint)) { continue; }
            List<Waypoint> newCurr = new ArrayList<Waypoint>();
            newCurr.addAll(curr);
            newCurr.add(currWaypoint);
            testPath(best, source, newCurr, start);
         }
     }
 
     private List<Waypoint> bruteForce(List<Waypoint> places) {
 		List<Waypoint> bestPlaces = new ArrayList<Waypoint>();
 		for (Waypoint currTest : places) {
             List<Waypoint> curr = new ArrayList<Waypoint>();
             curr.add(currTest);
             testPath(bestPlaces, places, curr, currTest);
         }
 		return bestPlaces;
 	}
 
 }
