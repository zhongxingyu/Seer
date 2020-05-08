 package com.bluebarracudas.model;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.bluebarracudas.app.TMapPanel;
 
 
 /** A factory to provide unique instances of TStops and TTrips */
 public class TFactory {
 
 	public static double top = -180;
 	public static double bottom = 180;
 	public static double right = -180;
 	public static double left = 180;
 
 	/** Whether or not we are initialized */
 	private static boolean bInitialized = false;
 	/** A map from IDs to the corresponding TStop */
 	private static Map<Integer, TStop> stopMap;
 	/** A map from IDs to the corresponding TTrip */
 	private static Map<String, TTrip> tripMap;
 
 	/** Private default constructor */
 	private TFactory() { }
 	
 	/** Set up our factory */
 	private static void initialize() {
 		// Set up our stop map
 		stopMap= new HashMap<Integer, TStop>();
 		// Set up our trip map
 		tripMap= new HashMap<String, TTrip>();
 		
 		// Initialize the TLines and Lists of TLines to assign to stops
 		
 		TLine orange = new TLine("orange");
 		TLine red = new TLine("red");
 		TLine blue = new TLine("blue");
 		
 		List<TLine> oLine = new ArrayList<TLine>();
 		oLine.add(orange);
 		List<TLine> rLine = new ArrayList<TLine>();
 		rLine.add(red);
 		List<TLine> bLine = new ArrayList<TLine>();
 		bLine.add(blue);
 		
 		// Lists of 2 TLines for transfer stops
 		List<TLine> roLine = new ArrayList<TLine>();
 		roLine.add(red);
 		roLine.add(orange);
 		List<TLine> boLine = new ArrayList<TLine>();
 		boLine.add(blue);
 		boLine.add(orange);
 		
 		// Note that even numbered stop IDs are North Bound
 		// and odd numbered stop IDs are South Bound
 		
 		// Note that positions are specified in longitude, latitude
 
 		// Red Line Stops
 		stopMap.put( 70061, new TStop(70061, "Alewife",           new Point2D.Double(-71.141099, 42.395704), rLine) );
 		stopMap.put( 70062, new TStop(70062, "Alewife",           new Point2D.Double(-71.141099, 42.395704), rLine) );
 		stopMap.put( 70063, new TStop(70063, "Davis",             new Point2D.Double(-71.121926, 42.396197), rLine) );
 		stopMap.put( 70064, new TStop(70064, "Davis",             new Point2D.Double(-71.121926, 42.396197), rLine) );
 		stopMap.put( 70065, new TStop(70065, "Porter",            new Point2D.Double(-71.119148, 42.388505), rLine) );
 		stopMap.put( 70066, new TStop(70066, "Porter",            new Point2D.Double(-71.119148, 42.388505), rLine) );
 		stopMap.put( 70067, new TStop(70067, "Harvard",           new Point2D.Double(-71.118954, 42.373653), rLine) );
 		stopMap.put( 70068, new TStop(70068, "Harvard",           new Point2D.Double(-71.118954, 42.373653), rLine) );
 		stopMap.put( 70069, new TStop(70069, "Central",           new Point2D.Double(-71.103644, 42.365457), rLine) );
 		stopMap.put( 70070, new TStop(70070, "Central",           new Point2D.Double(-71.103644, 42.365457), rLine) );
 		stopMap.put( 70071, new TStop(70071, "Kendall/MIT",       new Point2D.Double(-71.086382, 42.362413), rLine) );
 		stopMap.put( 70072, new TStop(70072, "Kendall/MIT",       new Point2D.Double(-71.086382, 42.362413), rLine) );
 		stopMap.put( 70073, new TStop(70073, "Charles/MGH",       new Point2D.Double(-71.070825, 42.361255), rLine) );
 		stopMap.put( 70074, new TStop(70074, "Charles/MGH",       new Point2D.Double(-71.070825, 42.361255), rLine) );
 		stopMap.put( 70075, new TStop(70075, "Park St.",          new Point2D.Double(-71.061952, 42.356332), rLine) );
 		stopMap.put( 70076, new TStop(70076, "Park St.",          new Point2D.Double(-71.061952, 42.356332), rLine) );
 		stopMap.put( 70077, new TStop(70077, "Downtown Crossing", new Point2D.Double(-71.060493, 42.355515), roLine) );
 		stopMap.put( 70078, new TStop(70078, "Downtown Crossing", new Point2D.Double(-71.060493, 42.355515), roLine) );
 		stopMap.put( 70079, new TStop(70079, "South Station",     new Point2D.Double(-71.056008, 42.351971), rLine) );
 		stopMap.put( 70080, new TStop(70080, "South Station",     new Point2D.Double(-71.056008, 42.351971), rLine) );
 		stopMap.put( 70081, new TStop(70081, "Broadway",          new Point2D.Double(-71.057349, 42.342884), rLine) );
 		stopMap.put( 70082, new TStop(70082, "Broadway",          new Point2D.Double(-71.057349, 42.342884), rLine) );
 		stopMap.put( 70083, new TStop(70083, "Andrew",            new Point2D.Double(-71.056845, 42.329362), rLine) );
 		stopMap.put( 70084, new TStop(70084, "Andrew",            new Point2D.Double(-71.056845, 42.329362), rLine) );
 		stopMap.put( 70085, new TStop(70085, "JFK/UMASS",         new Point2D.Double(-71.051717, 42.320732), rLine) );
 		stopMap.put( 70086, new TStop(70086, "JFK/UMASS",         new Point2D.Double(-71.051717, 42.320732), rLine) );
 		stopMap.put( 70095, new TStop(70095, "JFK/UMASS",         new Point2D.Double(-71.051717, 42.320732), rLine) );
 		stopMap.put( 70096, new TStop(70096, "JFK/UMASS",         new Point2D.Double(-71.051717, 42.320732), rLine) );
 
 		// Red Line - Ashmont
 		stopMap.put( 70087, new TStop(70087, "Savin Hill",        new Point2D.Double(-71.052747, 42.312672), rLine) );
 		stopMap.put( 70088, new TStop(70088, "Savin Hill",        new Point2D.Double(-71.052747, 42.312672), rLine) );
 		stopMap.put( 70089, new TStop(70089, "Fields Corner",     new Point2D.Double(-71.061667, 42.300093), rLine) );
 		stopMap.put( 70090, new TStop(70090, "Fields Corner",     new Point2D.Double(-71.061667, 42.300093), rLine) );
 		stopMap.put( 70091, new TStop(70091, "Shawmut",           new Point2D.Double(-71.06573796, 42.29312583), rLine) );
 		stopMap.put( 70092, new TStop(70092, "Shawmut",           new Point2D.Double(-71.06573796, 42.29312583), rLine) );
 		stopMap.put( 70093, new TStop(70093, "Ashmont",           new Point2D.Double(-71.064489, 42.284652), rLine) );
 		stopMap.put( 70094, new TStop(70094, "Ashmont",           new Point2D.Double(-71.064489, 42.284652), rLine) );
 
 		// Red Line - Braintree
 		stopMap.put( 70097, new TStop(70097, "North Quincy",      new Point2D.Double(-71.029583, 42.275275), rLine) );
 		stopMap.put( 70098, new TStop(70098, "North Quincy",      new Point2D.Double(-71.029583, 42.275275), rLine) );
 		stopMap.put( 70099, new TStop(70099, "Wollaston",         new Point2D.Double(-71.0203369, 42.2665139), rLine) );
 		stopMap.put( 70100, new TStop(70100, "Wollaston",         new Point2D.Double(-71.0203369, 42.2665139), rLine) );
 		stopMap.put( 70101, new TStop(70101, "Quincy Center",     new Point2D.Double(-71.005409, 42.251809), rLine) );
 		stopMap.put( 70102, new TStop(70102, "Quincy Center",     new Point2D.Double(-71.005409, 42.251809), rLine) );
 		stopMap.put( 70103, new TStop(70103, "Quincy Adams",      new Point2D.Double(-71.007153, 42.233391), rLine) );
 		stopMap.put( 70104, new TStop(70104, "Quincy Adams",      new Point2D.Double(-71.007153, 42.233391), rLine) );
 		stopMap.put( 70105, new TStop(70105, "Braintree",         new Point2D.Double(-71.0011385, 42.2078543), rLine) );
 		stopMap.put( 70106, new TStop(70106, "Braintree",         new Point2D.Double(-71.0011385, 42.2078543), rLine) );
 
 		// Orange Line
 		stopMap.put( 70037, new TStop(70037, "Oak Grove",		  new Point2D.Double(-71.071097, 42.43668), oLine) );
 		stopMap.put( 70036, new TStop(70036, "Oak Grove",         new Point2D.Double(-71.071097, 42.43668), oLine) );
 		stopMap.put( 70035, new TStop(70035, "Malden",            new Point2D.Double(-71.07411, 42.426632), oLine) );
 		stopMap.put( 70034, new TStop(70034, "Malden",            new Point2D.Double(-71.07411, 42.426632), oLine) );
 		stopMap.put( 70033, new TStop(70033, "Wellington",        new Point2D.Double(-71.077082, 42.40237), oLine) );
 		stopMap.put( 70032, new TStop(70032, "Wellington",        new Point2D.Double(-71.077082, 42.40237), oLine) );
 		stopMap.put( 70031, new TStop(70031, "Sullivan",          new Point2D.Double(-71.076994, 42.383975), oLine) );
 		stopMap.put( 70030, new TStop(70030, "Sullivan",          new Point2D.Double(-71.076994, 42.383975), oLine) );
 		stopMap.put( 70029, new TStop(70029, "Community College", new Point2D.Double(-71.069533, 42.373622), oLine) );
 		stopMap.put( 70028, new TStop(70028, "Community College", new Point2D.Double(-71.069533, 42.373622), oLine) );
 		stopMap.put( 70027, new TStop(70027, "North Station",     new Point2D.Double(-71.06129, 42.365577), oLine) );
 		stopMap.put( 70026, new TStop(70026, "North Station",     new Point2D.Double(-71.06129, 42.365577), oLine) );
 		stopMap.put( 70025, new TStop(70025, "Haymarket",         new Point2D.Double(-71.05829, 42.363021), oLine) );
 		stopMap.put( 70024, new TStop(70024, "Haymarket",         new Point2D.Double(-71.05829, 42.363021), oLine) );
 		stopMap.put( 70023, new TStop(70023, "State Street",      new Point2D.Double(-71.057598, 42.358978), boLine) );
 		stopMap.put( 70022, new TStop(70022, "State Street",      new Point2D.Double(-71.057598, 42.358978), boLine) );
 		stopMap.put( 70021, new TStop(70021, "Downtown Crossing", new Point2D.Double(-71.060225, 42.355518), roLine) );
 		stopMap.put( 70020, new TStop(70020, "Downtown Crossing", new Point2D.Double(-71.060225, 42.355518), roLine) );
 		stopMap.put( 70019, new TStop(70019, "Chinatown",         new Point2D.Double(-71.062752, 42.352547), oLine) );
 		stopMap.put( 70018, new TStop(70018, "Chinatown",         new Point2D.Double(-71.062752, 42.352547), oLine) );
 		stopMap.put( 70017, new TStop(70017, "Tufts Medical Center",  new Point2D.Double(-71.063917, 42.349662), oLine) );
 		stopMap.put( 70016, new TStop(70016, "Tufts Medical Center",  new Point2D.Double(-71.063917, 42.349662), oLine) );
 		stopMap.put( 70015, new TStop(70015, "Back Bay",          new Point2D.Double(-71.075727, 42.34735), oLine) );
 		stopMap.put( 70014, new TStop(70014, "Back Bay",          new Point2D.Double(-71.075727, 42.34735), oLine) );
 		stopMap.put( 70013, new TStop(70013, "Mass Ave",          new Point2D.Double(-71.083423, 42.341512), oLine) );
 		stopMap.put( 70012, new TStop(70012, "Mass Ave",          new Point2D.Double(-71.083423, 42.341512), oLine) );
 		stopMap.put( 70011, new TStop(70011, "Ruggles",           new Point2D.Double(-71.088961, 42.336377), oLine) );
 		stopMap.put( 70010, new TStop(70010, "Ruggles",           new Point2D.Double(-71.088961, 42.336377), oLine) );
 		stopMap.put( 70009, new TStop(70009, "Roxbury Crossing",  new Point2D.Double(-71.095451, 42.331397), oLine) );
 		stopMap.put( 70008, new TStop(70008, "Roxbury Crossing",  new Point2D.Double(-71.095451, 42.331397), oLine) );
 		stopMap.put( 70007, new TStop(70007, "Jackson Square",    new Point2D.Double(-71.099592, 42.323132), oLine) );
 		stopMap.put( 70006, new TStop(70006, "Jackson Square",    new Point2D.Double(-71.099592, 42.323132), oLine) );
 		stopMap.put( 70005, new TStop(70005, "Stony Brook",       new Point2D.Double(-71.104248, 42.317062), oLine) );
 		stopMap.put( 70004, new TStop(70004, "Stony Brook",       new Point2D.Double(-71.104248, 42.317062), oLine) );
 		stopMap.put( 70003, new TStop(70003, "Green Street",      new Point2D.Double(-71.107414, 42.310525), oLine) );
 		stopMap.put( 70002, new TStop(70002, "Green Street",      new Point2D.Double(-71.107414, 42.310525), oLine) );
 		stopMap.put( 70001, new TStop(70001, "Forest Hills",      new Point2D.Double(-71.113686, 42.300523), oLine) );
 		stopMap.put( 70000, new TStop(70000, "Forest Hills",      new Point2D.Double(-71.113686, 42.300523), oLine) );
 
 		// Blue Line
 		stopMap.put( 70060, new TStop(70060, "Wonderland",        new Point2D.Double(-70.991648, 42.41342), bLine) );
 		stopMap.put( 70059, new TStop(70059, "Wonderland",        new Point2D.Double(-70.991648, 42.41342), bLine) );
 		stopMap.put( 70058, new TStop(70058, "Revere",            new Point2D.Double(-70.99253321, 42.40784254), bLine) );
 		stopMap.put( 70057, new TStop(70057, "Revere",            new Point2D.Double(-70.99253321, 42.40784254), bLine) );
 		stopMap.put( 70056, new TStop(70056, "Beachmont",         new Point2D.Double(-70.99231944, 42.39754234), bLine) );
 		stopMap.put( 70055, new TStop(70055, "Beachmont",         new Point2D.Double(-70.99231944, 42.39754234), bLine) );
 		stopMap.put( 70054, new TStop(70054, "Suffolk Downs",     new Point2D.Double(-70.99712259, 42.39050067), bLine) );
 		stopMap.put( 70053, new TStop(70053, "Suffolk Downs",     new Point2D.Double(-70.99712259, 42.39050067), bLine) );
 		stopMap.put( 70052, new TStop(70052, "Orient Heights",    new Point2D.Double(-71.004736, 42.386867), bLine) );
 		stopMap.put( 70051, new TStop(70051, "Orient Heights",    new Point2D.Double(-71.004736, 42.386867), bLine) );
 		stopMap.put( 70050, new TStop(70050, "Wood Island",       new Point2D.Double(-71.02286539, 42.3796403), bLine) );
 		stopMap.put( 70049, new TStop(70049, "Wood Island",       new Point2D.Double(-71.02286539, 42.3796403), bLine) );
 		stopMap.put( 70048, new TStop(70048, "Airport",           new Point2D.Double(-71.030395, 42.374262), bLine) );
 		stopMap.put( 70047, new TStop(70047, "Airport",           new Point2D.Double(-71.030395, 42.374262), bLine) );
 		stopMap.put( 70046, new TStop(70046, "Maverick",          new Point2D.Double(-71.03952958, 42.36911856), bLine) );
 		stopMap.put( 70045, new TStop(70045, "Maverick",          new Point2D.Double(-71.03952958, 42.36911856), bLine) );
 		stopMap.put( 70044, new TStop(70044, "Aquarium",          new Point2D.Double(-71.051652, 42.359784), bLine) );
 		stopMap.put( 70043, new TStop(70043, "Aquarium",          new Point2D.Double(-71.051652, 42.359784), bLine) );
 		stopMap.put( 70042, new TStop(70042, "State",             new Point2D.Double(-71.057598, 42.358978), boLine) );
 		stopMap.put( 70041, new TStop(70041, "State",             new Point2D.Double(-71.057598, 42.358978), boLine) );
 		stopMap.put( 70040, new TStop(70040, "Government Center", new Point2D.Double(-71.059215, 42.359705), bLine) );
 		stopMap.put( 70039, new TStop(70039, "Government Center", new Point2D.Double(-71.059215, 42.359705), bLine) );
 		stopMap.put( 70038, new TStop(70038, "Bowdoin",           new Point2D.Double(-71.062037, 42.361365), bLine) );
 		//stopMap.put( 70037, new TStop(70037, "Bowdoin",           new Point2D.Double(-71.062037, 42.361365), bLine) );
 
 		setLatLongExtremes();
 		
 		// Set our flag
 		bInitialized = true;
 	}
 	
 	/** Sets the extreme high/low for longitude and latitude */
 	private static void setLatLongExtremes() {
 		for (TStop stop : stopMap.values()) {
 			Point2D position = stop.getPosition();
 
			if (position.getX() > top) {
				top = position.getX();
 			}
			if (position.getX() < bottom) {
				bottom = position.getX();
 			}
			if (position.getY() < left) {
				left = position.getY();
 			}
			if (position.getY() > right) {
				right = position.getY();
 			}
 		}
 	}
 
 	/** Converts longitude to grid units used for rendering */
 	public static int longitudeToUnits(double longitude, int width){
 		return (int) Math.round((longitude - left)/(right - left) * TMapPanel.WIDTH) + TMapPanel.PADDING;
 	}
 	
 	/** Converts latitude to grid units used for rendering */
 	public static int latitudeToUnits(double latitude, int height){
 		return (int) Math.round(TMapPanel.HEIGHT - (latitude - bottom)/(top - bottom) * TMapPanel.HEIGHT)  + TMapPanel.PADDING;
 	}
 
 	/**
 	 * Get the appropriate TStop
 	 * 
 	 * @param nID The ID of the desired TStop
 	 */
 	public static synchronized TStop getStop(int nID) {
 		// If the factory hasn't been initialized yet, do so
 		if (!bInitialized) initialize();
 
 		// Otherwise look up the stop in the map
 		return stopMap.get(nID);
 	}
 	
 	/**
 	 * Get all of the TStops
 	 */
 	public static synchronized Map<Integer, TStop> getAllStops() {
 		// If the factory hasn't been initialized yet, do so
 		if (!bInitialized) initialize();
 		
 		// Otherwise return the map of stops
 		return stopMap;
 	}
 	
 	/**
 	 * Get the appropriate TTrip, create a new one if it doesn't exist.
 	 * 
 	 * @param nID The ID of the desired TTrip
 	 */
 	public static synchronized TTrip getTrip(String sID) {
 		// If the factory hasn't been initialized yet, do so
 		if (!bInitialized) initialize();
 
 		// Otherwise look up the trip in the map
 		TTrip result= tripMap.get(sID);
 
 		// If the entry doesn't exist, create a new one
 		if (result == null)
 			result= new TTrip( sID );
 			tripMap.put(sID, result);
 
 		return result;
 	}
 	
 	/**
 	 * Get all of the existing TTrips
 	 */
 	public static synchronized Map<String, TTrip> getAllTrips() {
 		// If the factory hasn't been initialized yet, do so
 		if (!bInitialized) initialize();
 		
 		// Otherwise return the map of trips
 		return tripMap;
 	}
 }
