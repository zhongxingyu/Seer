 package at.genetic.culture.schoolbus;
 
 import java.util.ArrayList;
 
 public class SchoolArea {
 
 	public BusStop school;
 	private BusStop[] busStops;
 
 	private int busSize;
 	private double costPerBus = 100;
 	private double costPerKm = 0.7;
 
 	public SchoolArea(BusStop school, BusStop[] busStops, int busSize)
 			throws Exception {
 		this.school = school;
 		this.busStops = busStops;
 		this.busSize = busSize;
 
 		for (BusStop busStop : busStops) {
 			if (busStop.numberOfPupils > busSize) {
 				throw new Exception(
 						"There are more pupils waiting at a busstopt ("
 								+ busStop.numberOfPupils
 								+ ") than a bus can carry (" + busSize
 								+ "), that is not ok.");
 			}
 		}
 	}
 	
 	public void setCostPerBus (double costPerBus) {
 		this.costPerBus = costPerBus;
 	}
 	
 	public void setCostPerKm (double costPerKm) {
 		this.costPerKm = costPerKm;
 	}
 
 	public double[] getCostsForSchedule(int[] schedule) {
 		if (schedule.length != busStops.length) {
 			// throw new Exception("The given schedule with " + schedule.length
 			// + " elements doesn't match the busStops " + busStops.length +
 			// ".");
 		}
 
		int kmCosts = 0;
		int busCosts = 0;
 		int inBus = 0;
 		boolean atSchool = true;
 
		// TODO way from school to first stop
 		busCosts += costPerBus; // first bus
 		kmCosts += distance(school, busStops[schedule[0]]) * costPerKm;
 
 		for (int i = 0; i < schedule.length; i++) {
 
 			if (busStops[schedule[i]].numberOfPupils + inBus > busSize) {
 				if (i - 1 < 0) {
 					// Exception, bus can't be full on first tour
 				}
 				// have to go to school cause bus is full
 				kmCosts += distance(busStops[schedule[i - 1]], school)
 						* costPerKm;
 				busCosts += costPerBus;
 				kmCosts += distance(school, busStops[schedule[i]]) * costPerKm;
 
 				// put pupils into bus
 				inBus = busStops[schedule[i]].numberOfPupils;
 				atSchool = false;
 			} else {
 				if (!atSchool)
 					kmCosts += distance(busStops[schedule[i - 1]],
 							busStops[schedule[i]]) * costPerKm;
 				inBus += busStops[schedule[i]].numberOfPupils;
 				atSchool = false;
 			}
 
 		}
 
 		// way from last stop to school
 		kmCosts += distance(busStops[schedule[schedule.length - 1]], school)
 				* costPerKm;
 
 		return new double[] { kmCosts, busCosts };
 	}
 
 	public int countStops() {
 		return busStops.length;
 	}
 	
 	public BusStop[] getAllStops () {
 		ArrayList<BusStop> stops = new ArrayList<BusStop>();
 		stops.add(school);
 		for (BusStop busStop : busStops) {
 			stops.add(busStop);
 		}
 		
 		return stops.toArray(new BusStop[stops.size()]);
 	}
 	
 	public BusStop[] getRoute (Integer[] path) {
 		ArrayList<BusStop> stops = new ArrayList<BusStop>();
 
 		stops.add(school);
 		
 		int inBus = 0;
 		for (int i = 0; i < path.length; i++) {
 			
 			
 			if (busStops[path[i]].numberOfPupils + inBus > busSize) {
 				// put pupils into bus
 				stops.add(school);
 				stops.add(busStops[path[i]]);
 				inBus = busStops[path[i]].numberOfPupils;
 			} else {
 				stops.add(busStops[path[i]]);
 				inBus += busStops[path[i]].numberOfPupils;
 			}
 			
 		}
 		stops.add(school);
 		
 		return stops.toArray(new BusStop[stops.size()]);
 	}
 
 	private double distance(BusStop a, BusStop b) {
		// System.out.println("dist between " + a + " and " + b);
		return Math.sqrt((a.x + b.x) * (a.x + b.x) + (a.y + b.y) * (a.y + b.y));
 	}
 }
