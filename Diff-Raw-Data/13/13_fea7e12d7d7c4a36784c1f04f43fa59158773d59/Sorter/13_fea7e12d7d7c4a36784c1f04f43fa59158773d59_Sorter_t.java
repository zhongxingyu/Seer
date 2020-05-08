 package main;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import util.Time;
 
 /**
  * The class that handles the sorting of drivers for the sorted file
  * 
  * @author Team08
  *
  */
 public class Sorter {
 	private ArrayList<Driver> driverList;
 	private Time raceTime;
 	
 /**
  * Creates a sorter without attributes
  */
 	public Sorter(){
 		
 	}
 
 /**
  * Sorts the list in the correct order
  * 
  * @param old - a list where the drivers are unsorted
  * @param raceTime - the time that the total time must be over for the driver to have finished the race
  * @return a list where the drivers are sorted
  */
 	public ArrayList<Driver> lapSort(ArrayList<Driver> old, Time raceTime){
 		this.raceTime = raceTime;
 		driverList = old;
 		Collections.sort(driverList, new lapSortComparator());
 		return driverList;
 	}
 
 /**
  * The class that handles the comparison during sorting
  * 
  * @author Team08
  *
  */
 	public class lapSortComparator implements Comparator<Driver>{
 
 		/**
 		 * Compares the two drivers. First checks if the race time is met, 
 		 * then by number of laps and then by the total time
 		 * @param d1 - a driver to compare
 		 * @param d2 - the other driver to compare
 		 * @return 1 if d1 should be before d2 and -1 if d2 should be before d1 (0 if the drivers are exactly alike)
 		 */
 		public int compare(Driver d1, Driver d2) {
 			Integer d1T = (new Time(d1.totalTime()).lesserThan(raceTime)) ? -1 : 0; //-1 om ingen totaltid, 0 annars
 			Integer d2T = (new Time(d2.totalTime()).lesserThan(raceTime)) ? -1 : 0; //-1 om ingen totaltid, 0 annars
 	
 			int c1 = d1T.compareTo(d2T); 
 
			if(c1 > 0) {
 				return -1;
 			}
			if(c1 < 0) {
 				return 1;
 			}
 			
 			Integer d1Laps = new Integer(d1.getNumberOfLaps());
 			Integer d2Laps = new Integer(d2.getNumberOfLaps());
 			
 			int c2 = d1Laps.compareTo(d2Laps);
 
			if(c2 > 0) {
				return -1;
			}
			if(c2 < 0) {
				return 1;
 			}
 
 			return ((Integer)d1.totalTime()).compareTo((Integer)d2.totalTime());		
 		}
 	}
 }
