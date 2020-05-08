 package ch.ubx.startlist.server.maint;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import ch.ubx.startlist.server.FlightEntryDAOobjectify2;
 import ch.ubx.startlist.shared.FeDate;
 import ch.ubx.startlist.shared.FeFlightEntry;
 import ch.ubx.startlist.shared.FePlace;
 import ch.ubx.startlist.shared.FeYear;
 
 public class ValidateDb {
 	private static final Logger log = Logger.getLogger(ValidateDb.class.getName());
 	private static final FlightEntryDAOobjectify2 dao = new FlightEntryDAOobjectify2();
 
 	public static void validate() {
 
 		log.log(Level.INFO, "Start...");
 
 		List<FeYear> emptyYears = new ArrayList<FeYear>();
 		List<FePlace> emptyPlaces = new ArrayList<FePlace>();
 		List<FeDate> emptyDates = new ArrayList<FeDate>();
 		List<FeYear> years = dao.listYear();
		int totalFlightEntries = dao.listflightEntry().size();
		int inTreeFlightEntries = 0;
		
 		for (FeYear year : years) {
 			log.log(Level.INFO, "Doing Year, id=" + year.getId());
 			List<FePlace> places = dao.listAirfield(year);
 			if (places.size() == 0) {
 				emptyYears.add(year);
 			} else {
 				for (FePlace place : places) {
 					List<FeDate> dates = dao.listDate(place);
 					if (dates.size() == 0) {
 						emptyPlaces.add(place);
 					} else {
 						for (FeDate date : dates) {
 							List<FeFlightEntry> flightEntries = dao.listflightEntry(date);
 							if (flightEntries.size() == 0) {
 								emptyDates.add(date);
							} else {
								inTreeFlightEntries = inTreeFlightEntries + flightEntries.size();
 							}
 						}
 					}
 				}
 			}
 		}
 
 		for (FeYear year : emptyYears) {
 			log.log(Level.WARNING, "Empty Year, id=" + year.getId());
 		}
 
 		for (FePlace place : emptyPlaces) {
 			log.log(Level.WARNING, "Empty Place, id=" + place.getId());
 		}
 
 		for (FeDate date : emptyDates) {
 			log.log(Level.WARNING, "Empty Date, id=" + date.getId());
 		}
		
		if (totalFlightEntries != inTreeFlightEntries) {
			log.log(Level.WARNING, "FlightEntries, total="+ totalFlightEntries + ", in Tree="+inTreeFlightEntries);
		}
 
 	}
 
 }
