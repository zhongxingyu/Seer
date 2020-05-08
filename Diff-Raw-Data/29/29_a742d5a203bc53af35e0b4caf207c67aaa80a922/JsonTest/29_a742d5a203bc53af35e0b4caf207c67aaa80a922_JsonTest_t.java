 package data;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.Stack;
 
 //import java.io.File;
 //import com.fasterxml.jackson.core.*;
 //import com.fasterxml.jackson.annotation.*;
 
 //import com.fasterxml.jackson.core.JsonParseException;
 //import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.DeserializationFeature;
 
 //import java.net.URL;
 
 public class JsonTest
 {
 	public enum TrainLine {
 		BLUE,
 		RED,
 		ORANGE,
 		TRANSFER;
 	}
 	
 	private final static String BLUE_LINE = "blue";
 	private final static String RED_LINE = "red";
 	private final static String RED_A_LINE = "red_a";
 	private final static String RED_B_LINE = "red_b";
 	private final static String ORANGE_LINE = "orange";
 	private final static String TRANSFER_LINE = "transfer";
 	private final static String LIVE_DATA_BLUE = "http://developer.mbta.com/lib/rthr/blue.json";
 	private final static String LIVE_DATA_ORANGE = "http://developer.mbta.com/lib/rthr/orange.json";
 	private final static String LIVE_DATA_RED = "http://developer.mbta.com/lib/rthr/red.json";
 	
 	private final static String testBlue = "Resources/Test Files/TestBlue_2012_10_19.json";
 	private final static String testOrange = "Resources/Test Files/TestOrange_2012_10_19.json";
 	private final static String testRed = "Resources/Test Files/TestRed_2012_10_19.json";
 	private final static String shortTestBlue = "Resources/Test Files/shortTestBlue.json";
 	private final static String shortTestBlue2 = "Resources/Test Files/shortTestBlue2.json";
 	private final static String shortTestOrange = "Resources/Test Files/shortTestOrange.json";
 	private final static String shortTestRed = "Resources/Test Files/shortTestRed.json";
 	
     public enum Stop {
         WONDERLAND ("Wonderland", BLUE_LINE),             // Blue line trains
         REVERE_BEACH ("Revere Beach", BLUE_LINE),
         BEACHMONT ("Beachmont", BLUE_LINE),
         SUFFOLK_DOWNS ("Suffolk Downs", BLUE_LINE),
         ORIENT_HEIGHTS ("Orient Heights", BLUE_LINE),
         WOOD_ISLAND ("Wood Island", BLUE_LINE),
         AIRPORT ("Airport", BLUE_LINE),
         MAVERICK ("Maverick", BLUE_LINE),
         AQUARIUM ("Aquarium", BLUE_LINE),
         STATE ("State Street", TRANSFER_LINE),
         GOVERNMENT_CENTER ("Government Center", BLUE_LINE),
         BOWDOIN ("Bowdoin", BLUE_LINE),
         OAK_GROVE ("Oak Grove", ORANGE_LINE),                // Orange line trains
         MALDEN_CENTER ("Malden Center", ORANGE_LINE),
         WELLINGTON ("Wellington", ORANGE_LINE),
         SULLIVAN_SQUARE ("Sullivan", ORANGE_LINE),		// Sullivan, or sullican square?
         COMMUNITY_COLLEGE ("Community College", ORANGE_LINE),
         NORTH_STATION ("North Station", ORANGE_LINE),
         HAYMARKET_STATION ("Haymarket", ORANGE_LINE),		// haymarket or haymarket station?
         // STATE_STREET ("State Street", TRANSFER_LINE), --- blue line has it covered
         DOWNTOWN_CROSSING ("Downtown Crossing", TRANSFER_LINE),
         CHINATOWN ("Chinatown", ORANGE_LINE),
         TUFTS_MEDICAL_CENTER ("Tufts Medical", ORANGE_LINE),
         BACK_BAY ("Back Bay", ORANGE_LINE),
         MASSACHUSETTS_AVENUE ("Mass Ave", ORANGE_LINE),
         RUGGLES ("Ruggles", ORANGE_LINE),
         ROXBURY_CROSSING ("Roxbury Crossing", ORANGE_LINE),
         JACKSON_SQUARE ("Jackson Square", ORANGE_LINE),
         STONY_BROOK ("Stony Brook", ORANGE_LINE),
         GREEN_STREET ("Green Street", ORANGE_LINE),
         FOREST_HILLS ("Forest Hills", ORANGE_LINE),
         ALEWIFE ("Alewife", RED_LINE),	// Red line begins
         DAVIS ("Davis", RED_LINE),
         PORTER_SQUARE ("Porter Square", RED_LINE),
         HARVARD_SQUARE ("Harvard Square", RED_LINE),
         CENTRAL_SQUARE ("Central Square", RED_LINE),
         KENDALL_MIT ("Kendall/MIT", RED_LINE),
         CHARLES_MGH ("Charles/MGH", RED_LINE),
         PARK_STREET ("Park Street", RED_LINE),
         //	DtX --- taken care of by orange line
         SOUTH_STATION ("South Station", RED_LINE),
         BROADWAY ("Broadway", RED_LINE),
         ANDREW ("Andrew", RED_LINE),
         JFK_UMASS ("JFK/UMass", TRANSFER_LINE),
         NORTH_QUINCY ("North Quincy", RED_A_LINE),	// subsection of red line-A
         WOLLASTON ("Wollaston", RED_A_LINE),
         QUINCY_CENTER ("Quincy Center", RED_A_LINE),
         QUINCY_ADAMS ("Quincy Adams", RED_A_LINE),
         BRAINTREE ("Braintree", RED_A_LINE),
         SAVIN_HILL ("Savin Hill", RED_B_LINE),	// subsection of red line-B
         FIELDS_CORNER ("Fields Corner", RED_B_LINE),
         SHAWMUT ("Shawmut", RED_B_LINE),
         ASHMONT ("Ashmont", RED_B_LINE);
 
         private final String name;
         private final String line;
         Stop (String name, String line) {
             this.name = name;
             this.line = line;
         }
         
         public String getName() {
         	return name;
         }
         public String getline() {
         	return line;
         }
         
         public static ArrayList<Station> getArrayListOfStations() {
         	ArrayList<Station> arrs = new ArrayList<Station>();
         	for (Stop s : Stop.values()) {
         		Station station = new Station(s.name);
         		arrs.add(station);
         	}
         	return arrs;
         }
     }
 
     
     public static Graph getGraphFromInternet() {
     	TripList tripl = new TripList();
     	Graph g = new Graph();
     	
         ArrayList<String> files = new ArrayList<String>();
         files.add(LIVE_DATA_BLUE);
         files.add(LIVE_DATA_ORANGE);
         files.add(LIVE_DATA_RED);
     	
         try {
         	tripl = jsonFilesToTripList(files, true);
             tripListToGraph(tripl, g);
         } catch (IOException e) {
         	e.printStackTrace();
         }
         
         return g;
     }
 
     public static Graph getGraphFromFiles(ArrayList<String> files) {
     	TripList tripl = new TripList();
     	Graph g = new Graph();
     	
         try {
         	tripl = jsonFilesToTripList(files, false);
             tripListToGraph(tripl, g);
         } catch (IOException e) {
         	e.printStackTrace();
         }
         
         return g;
     }
     
     
     public static void main(String[] args) {
     		/*
             ArrayList<String> files = new ArrayList<String>();
             files.add(shortTestBlue2);
             files.add(shortTestOrange);
             files.add(shortTestRed);
 
             Graph g = new Graph();
             g = getGraphFromFiles(files);
 */
     		/*
             int currentTime = (int) System.currentTimeMillis();
             long currTime = System.currentTimeMillis();
             long ct = System.currentTimeMillis()/1000;
             int cti = (int) ct;
 
 
             //int currentTime = 0;
 
             int largeInt = 20000 + currentTime;
             System.out.println("currentTime=" + currentTime);
             System.out.println("currTime=" + currTime);
             System.out.println("ct=" + ct);
             System.out.println("cti=" + cti);
             System.out.println("1762123476");
             */
             /*
             Station airport = g.getStationByName("Airport");
             Station aquarium = g.getStationByName("Aquarium");
             Station maverick = g.getStationByName("Maverick");
             Station gov = g.getStationByName("Government Center");
             Station backbay = g.getStationByName("Back Bay");
             Station porter = g.getStationByName("Porter Square");
             Station harvard = g.getStationByName("Harvard Square");
             Station savHill = g.getStationByName("Savin Hill");
             Station braintree = g.getStationByName("Braintree");
             
             System.out.println("Graph:" + g.toString());
             */
             
             // A thing to note, we define quickest path as a path that gets you there the soonest
             //  rather than one that gets you there the soonest and in the least amount of travel time
             //  although, I don't think the latter would be overly difficult to implement.
 
 
             /*
             Pathway<TrainConnection> am = g.depthFirstSearch(airport, maverick, currentTime, 0, largeInt);
             Pathway<TrainConnection> am2 = fastestPathWithNoContraints(airport, maverick);
             System.out.println(am.toString());
             System.out.println(am2.toString());
             
             System.out.println("+++++++++++++++++++++++++++++");
             
             Pathway<TrainConnection> ma = g.depthFirstSearch(maverick, airport, currentTime, 0, largeInt);
             Pathway<TrainConnection> ma2 = fastestPathWithNoContraints(maverick, airport);
             System.out.println(ma.toString());
             System.out.println(ma2.toString());
             
             System.out.println("+++++++++++++++++++++++++++++");
 */
             //currentTime = 1350686050;
             
             //  1354672388
             
             /*     SITUATION_1
             CurrentTime = 		1354668399
             Airport=			1354671871
             Maverick=			1354672113
             Aquarium=			1354672251
             State Street=		1354672325
             Government Center=	1354672406
             
             
             
             SITUATION_2
             CurrentTime=		1354688399
             Airport=			1354671923
             Maverick=			1354672068
             Aquarium=			1354672204
             State Street=		1354672278
             Government Center=	1354672359
             */
             /*
             System.out.println("LARGE_INT EVERYONE=" + largeInt);
             Pathway<TrainConnection> ag = g.depthFirstSearch(airport, gov, currentTime, currentTime, largeInt);
             Pathway<TrainConnection> ag2 = fastestPathWithNoContraints(airport, gov);
             System.out.println("+++++++++++++++++++++++++++++ RESULT 1 +++++++++++++++++++++++++++++");
             System.out.println(ag.toString());
             System.out.println("+++++++++++++++++++++++++++++ RESULT 2 +++++++++++++++++++++++++++++");
             System.out.println(ag2.toString());
 
             System.out.println("+++++++++++++++++++++++++++++");
             
             */
             
             //System.out.println("LARGE_INT EVERYONE=" + largeInt);
             
             
             int tNow = JsonTest.getCurrentEpochTime();
             //Pathway<TrainConnection> ag = g.depthFirstSearch(airport, aquarium, cti, cti, largeInt, false);
             Pathway<TrainConnection> ag = fastestPathWithDepart(Stop.AIRPORT.name, Stop.AQUARIUM.name, tNow);
             Pathway<TrainConnection> ag2 = fastestPathWithNoContraints(Stop.AIRPORT.name, Stop.AQUARIUM.name);
             System.out.println("+++++++++++++++++++++++++++++ RESULT 1 +++++++++++++++++++++++++++++");
             System.out.println(ag.toString());
             System.out.println("+++++++++++++++++++++++++++++ RESULT 2 +++++++++++++++++++++++++++++");
             System.out.println(ag2.toString());
 
             System.out.println("+++++++++++++++++++++++++++++");
             
             
             //ArrayList<ArrayList<String>> aas = new ArrayList<ArrayList<String>>();
             
             ArrayList<String> aas = new  ArrayList<String>();
             aas.add(Stop.AIRPORT.name);
             aas.add(Stop.AQUARIUM.name);
             aas.add(Stop.GOVERNMENT_CENTER.name);
             //System.out.println("PERMUTATIONS=" + permutationsOf(aas));
             Pathway<TrainConnection> aag = fastestUnsortedPath(aas, false, 0, false, 0, false);
             System.out.println(aag.toString());
 
             /*
             Pathway<TrainConnection> g1 = g.depthFirstSearch(airport, gov, 0, 0, largeInt);
             Pathway<TrainConnection> g2 = fastestPathWithNoContraints(airport, gov);
             System.out.println("+++++++++++++++++++++++++++++ RESULT 3 +++++++++++++++++++++++++++++");
             System.out.println(g1.toString());
             System.out.println("+++++++++++++++++++++++++++++ RESULT 4 +++++++++++++++++++++++++++++");
             System.out.println(g2.toString());
 
             System.out.println("+++++++++++++++++++++++++++++");
             
 /*
             Pathway<TrainConnection> ga = g.depthFirstSearch(gov, airport, currentTime, 0, largeInt);
             Pathway<TrainConnection> ga2 = fastestPathWithNoContraints(gov, airport);
             System.out.println(ga.toString());
             System.out.println(ga2.toString());
             
             System.out.println("+++++++++++++++++++++++++++++");
 
             Pathway<TrainConnection> abb = g.depthFirstSearch(airport, backbay, currentTime, 0, largeInt);
             Pathway<TrainConnection> abb2 = fastestPathWithNoContraints(airport, backbay);
             System.out.println(abb.toString());
             System.out.println(abb2.toString());
 
             System.out.println("+++++++++++++++++++++++++++++");
 
             Pathway<TrainConnection> bba = g.depthFirstSearch(backbay, airport, currentTime, 0, largeInt);
             Pathway<TrainConnection> bba2 = fastestPathWithNoContraints(backbay, airport);
             System.out.println(bba.toString());
             System.out.println(bba2.toString());
 
             System.out.println("+++++++++++++++++++++++++++++");
 
             
             // RED
             Pathway<TrainConnection> ph = g.depthFirstSearch(harvard, porter, currentTime, 0, largeInt);
             Pathway<TrainConnection> ph2 = fastestPathWithNoContraints(harvard, porter);
             System.out.println(ph.toString());
             System.out.println(ph2.toString());
             
             System.out.println("+++++++++++++++++++++++++++++");
 
             Pathway<TrainConnection> hp = g.depthFirstSearch(porter, harvard, currentTime, 0, largeInt);
             Pathway<TrainConnection> hp2 = fastestPathWithNoContraints(porter, harvard);
             System.out.println(hp.toString());
             System.out.println(hp2.toString());
             
             System.out.println("+++++++++++++++++++++++++++++");
 
             Pathway<TrainConnection> sb = g.depthFirstSearch(savHill, braintree, currentTime, 0, largeInt);
             Pathway<TrainConnection> sb2 = fastestPathWithNoContraints(savHill, braintree);
             System.out.println(sb.toString());
             System.out.println(sb2.toString());
             
             System.out.println("+++++++++++++++++++++++++++++");
 
             Pathway<TrainConnection> bs = g.depthFirstSearch(braintree, savHill, currentTime, 0, largeInt);
             Pathway<TrainConnection> bs2 = fastestPathWithNoContraints(braintree, savHill);
             System.out.println(bs.toString());
             System.out.println(bs2.toString());
 */
     }
 
 
     public static TripList jsonFilesToTripList(ArrayList<String> filenames, boolean fromInternet) throws IOException {
         TripList tl1 = jfttl(filenames.get(0), fromInternet);
         
         for (int i = 1; i < filenames.size(); i++) {
         	tl1.append(jfttl(filenames.get(i), fromInternet));
         }
         return tl1;
     }
 
 
     public static TripList jfttl(String filename, boolean fromInternet) throws IOException {
         ObjectMapper mapper = new ObjectMapper();
         TripList tripl = new TripList();
 
         try {
         	
         	BufferedReader fileReader;
             if (fromInternet) {
             	URL url = new URL(filename);
                 fileReader = new BufferedReader(new InputStreamReader(url.openStream()));
             } else {
             	fileReader = new BufferedReader(new FileReader(filename));
             }
             
             JsonNode rootNode = mapper.readTree(fileReader);
 
             // IMPORTANT
             // without this option set adding new fields breaks old code
             mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
 
             JsonNode triplNode = rootNode.path("TripList");
             int ct = triplNode.get("CurrentTime").asInt();
             
             tripl.setCurrentEpochTime(ct);
             String line = triplNode.get("Line").asText();
             tripl.setLine(line);
             
             JsonNode tNode = triplNode.path("Trips");
 
             Iterator<JsonNode> ite = tNode.elements();
 
             while(ite.hasNext()) {
 
                 JsonNode tempTripNode = ite.next();
                 Trip tempTrip = new Trip();
 
                 tempTrip.setLine(line);
                 String tID = tempTripNode.get("TripID").asText();
                 tempTrip.setTripID(tID);
                 String dest = tempTripNode.get("Destination").asText();
                 tempTrip.setDestination(dest);
 
                 boolean hasPosition = tempTripNode.has("Position");
                 if(hasPosition) {
 
                     JsonNode posNode = tempTripNode.path("Position");
                     Position pos = new Position();
 
                     int tTStamp = posNode.get("Timestamp").asInt();
                     pos.setTimestamp(tTStamp);
                     int tTrain = posNode.get("Train").asInt();
                     pos.setTrain(tTrain);
                     double tLat = posNode.get("Lat").asDouble();
                     pos.setLat(tLat);
                     double tLong = posNode.get("Long").asDouble();
                     pos.setLong(tLong);
                     int tHead = posNode.get("Heading").asInt();
                     pos.setHeading(tHead);
 
                     tempTrip.setPosition(pos);
                 }
 
                 JsonNode pNode = tempTripNode.path("Predictions");
                 Iterator<JsonNode> itePred = pNode.elements();
 
                 while(itePred.hasNext()) {
 
                     JsonNode tPredNode = itePred.next();
                     Prediction tempPred = new Prediction();
 
                     int tStopID = tPredNode.get("StopID").asInt();
                     tempPred.setStopID(tStopID);
                     String tStop = tPredNode.get("Stop").asText();
                     tempPred.setStop(tStop);
                     int tSeconds = tPredNode.get("Seconds").asInt();
                     if (tSeconds < 0) {
                     	continue;
                     }
                     if(hasPosition) {
                     	tempPred.setSeconds(tSeconds + tempTrip.getPosition().getTimestamp());
                     } else {
                     	tempPred.setSeconds(tSeconds + tripl.getCurrentEpochTime());
                     }
                     
 
                     tempTrip.addPrediction(tempPred);
                 }
 
                 tripl.addTrip(tempTrip);
             }
         }
         catch (IOException e) {
             e.printStackTrace();
         }
         return tripl;
     }
 
     
     public static void tripListToGraph(TripList t, Graph g) {
     	ArrayList<Station> arrs = new ArrayList<Station>();
     	arrs = Stop.getArrayListOfStations();
     	Station nullStation = new Station("NULL_STATION");
     	arrs.add(nullStation);
     	g.addAllStations(arrs);
     	
     	
     	int dataTime = t.getCurrentEpochTime();
     	//System.out.println("DataTime=" + dataTime);
     	
     	for (Trip trip : t.getTrips()) {    		
     		for (int i = 0, j = 1; j < trip.getPredictions().size(); i++, j++) {
         		Prediction p1 = trip.getPredictions().get(i);
         		Prediction p2 = trip.getPredictions().get(j);
         		
         		int weight = p2.getSeconds();
         		String line = trip.getLine();
         		String tripID = trip.getTripID();
         		String startStation = p1.getStop();
         		String endStation = p2.getStop();
         		Station ss = getStationByName(arrs, startStation);
         		Station es = getStationByName(arrs, endStation);
         		String destination = trip.getDestination();
         		g.addEdge(weight, line, tripID, ss, es, destination);
         		
         		int w = p1.getSeconds();
     			if ((i == 0) && (w >= 0)) {			// If at the first station in the list, 
     				g.addEdge(w, line, tripID, nullStation, ss, destination);
     			}
         	}
     	}
     }
     	
     public static Station getStationByName(ArrayList<Station> arrs, String name) {
     	for (Station s : arrs) {
     		if (s.getName().equals(name)) {
     			return s;
     		}
     	}
     	throw new IllegalArgumentException(name + " is not in the list");
     }
 
     
     private static Pathway<TrainConnection> fastestPath(String start, String end, int currentTime, int departByTime, int arriveByTime, boolean withFewestTransfers) {
         ArrayList<String> fileLocations = new ArrayList<String>();
         fileLocations.add(shortTestBlue2);
         fileLocations.add(shortTestOrange);
         fileLocations.add(shortTestRed);
 
         Graph g = new Graph();
         g = getGraphFromInternet();
         //g = getGraphFromFiles(fileLocations);
         Station startStation = g.getStationByName(start);
         Station endStation = g.getStationByName(end);
         
         return g.depthFirstSearch(startStation, endStation, currentTime, departByTime, arriveByTime, withFewestTransfers);
     }
     
     public static Pathway<TrainConnection> fastestPathWithDepart(String start, String end, int departByTime) {
     	int timeNow = JsonTest.getCurrentEpochTime();
     	int inf = (int) Double.POSITIVE_INFINITY;
 
     	return JsonTest.fastestPath(start, end, timeNow, departByTime, inf, false);
     }
     public static Pathway<TrainConnection> fastestPathWithNoContraints(String start, String end) {
     	int timeNow = JsonTest.getCurrentEpochTime();
     	int inf = (int) Double.POSITIVE_INFINITY;
 
     	return JsonTest.fastestPath(start, end, timeNow, 0, inf, false);
     }
     public static Pathway<TrainConnection> fastestPathWithDepartArrive(String start, String end, int departByTime, int arriveByTime) {
     	int timeNow = JsonTest.getCurrentEpochTime();
 
     	return JsonTest.fastestPath(start, end, timeNow, departByTime, arriveByTime, false);
     }
     public static Pathway<TrainConnection> fastestPathWithArrive(String start, String end, int arriveByTime) {
     	int timeNow = JsonTest.getCurrentEpochTime();
 
     	return JsonTest.fastestPath(start, end, timeNow, timeNow, arriveByTime, false);
     }
     public static Pathway<TrainConnection> fastestPathWithArriveFewestTransfers(String start, String end,int arriveByTime) {
     	int timeNow = JsonTest.getCurrentEpochTime();
     	
     	return JsonTest.fastestPath(start, end, timeNow, timeNow, arriveByTime, true);
     }
     public static Pathway<TrainConnection> 
     				fastestPathWithDepartArriveFewestTransfers(
     											String start, String end, 
     											int departBy, int arriveByTime) {
     	int timeNow = JsonTest.getCurrentEpochTime();
 
     	//return JsonTest.fastestPath(start, end, timeNow, timeNow, arriveByTime, true);
     	return null;																	/// FIXME
     }
     public static Pathway<TrainConnection> fastestPathWithDepartFewestTransfers(String start, String end, int departByTime) {
     	int timeNow = JsonTest.getCurrentEpochTime();
     	int inf = (int) Double.POSITIVE_INFINITY;
     	
     	return JsonTest.fastestPath(start, end, timeNow, departByTime, inf, true);
     }
     public static Pathway<TrainConnection> fastestPathWithFewestTransfers(String start, String end) {
        	int timeNow = JsonTest.getCurrentEpochTime();
     	int inf = (int) Double.POSITIVE_INFINITY;
     	
     	return JsonTest.fastestPath(start, end, timeNow, timeNow, inf, true);
     }
     
     public static Pathway<TrainConnection> fastestSortedPath(ArrayList<String> arrs, 
     												boolean shouldDepartBy, int departByTime, 
     												boolean shouldArriveBy, int arriveByTime, 
     												boolean withFewestTransfers) {
     	Pathway<TrainConnection> path = new Pathway<TrainConnection>(0);
     	
     	for (int i = 0, j = 1; j < arrs.size(); i++, j++) {
     		Pathway<TrainConnection> tempPath = new Pathway<TrainConnection>(0);
     		if (!(path.size() == 0)) {
     			departByTime = path.getTime();
     		}
     		
     		if (shouldDepartBy) {
     			if (shouldArriveBy) {
     				if (withFewestTransfers) {
     					tempPath = fastestPathWithDepartArriveFewestTransfers(arrs.get(i), arrs.get(j), 
     																	departByTime, arriveByTime);
     				} else {
     					tempPath = fastestPathWithDepartArrive(arrs.get(i), arrs.get(j), 
     													departByTime, arriveByTime);
     				}
     			} else {
     				if (withFewestTransfers) {
     					tempPath = fastestPathWithDepartFewestTransfers(arrs.get(i), arrs.get(j),
     																departByTime);
     				} else {
     					tempPath = fastestPathWithDepart(arrs.get(i), arrs.get(j), departByTime);
     				}
     			}
     		} else if (shouldArriveBy) {
     			if (withFewestTransfers) {
     				tempPath = fastestPathWithArriveFewestTransfers(arrs.get(i), arrs.get(j),
     														arriveByTime);
     			} else {
     				tempPath = fastestPathWithArrive(arrs.get(i), arrs.get(j), arriveByTime);
     			}
     		} else {
     			if (withFewestTransfers) {
     				tempPath = fastestPathWithFewestTransfers(arrs.get(i), arrs.get(j));
     			} else {
     				tempPath = JsonTest.fastestPathWithNoContraints(arrs.get(i), arrs.get(j));
     			}
     		}
     		
     		if (tempPath.size() == 0) {
     			path.clear();
     			break;
     		} else {
         		path.append(tempPath);
     		}
     	}
     	return path;
     }
     
     public static Pathway<TrainConnection> fastestUnsortedPath(ArrayList<String> arrs, 
 														boolean shouldDepartBy, int departByTime, 
 														boolean shouldArriveBy, int arriveByTime, 
 														boolean withFewestTransfers) {
     	
     	int inf = (int) Double.POSITIVE_INFINITY;
     	Pathway<TrainConnection> pathway = new Pathway<TrainConnection>(inf);
     	ArrayList<String> loStations = new ArrayList<String>();
     	ArrayList<ArrayList<String>> loStationsPermutations = new ArrayList<ArrayList<String>>();
     	loStationsPermutations = permutationsOf(arrs);
     	
     	for (ArrayList<String> los : loStationsPermutations) {
     		Pathway<TrainConnection> tempPathway = new Pathway<TrainConnection>(inf);
         	tempPathway = JsonTest.fastestSortedPath(los, shouldDepartBy, departByTime, shouldArriveBy, arriveByTime, withFewestTransfers);
 
     		if (tempPathway.size() > 0) {
     			if (withFewestTransfers) {
     				if (tempPathway.numTransfers() < pathway.numTransfers()) {
     					pathway = tempPathway;
     				}
     			} else {
     				if (tempPathway.getTime() < pathway.getTime()) {
     					pathway = tempPathway;
     				}
     			}
     		}
     	}
     	return pathway;
     }
     
     
     public static ArrayList<Station> listOfStationNameToListOfStations(ArrayList<String> arrString, Graph g) {
     	ArrayList<Station> arrStation = new ArrayList<Station>();
     	for (String s : arrString) {
     		arrStation.add(g.getStationByName(s));
     	}
     	return arrStation;
     }
     
     public static int getCurrentEpochTime() {
         long ct = System.currentTimeMillis()/1000;
         int cti = (int) ct;
         return cti;
     }
     
     public static ArrayList<Train> getAllTrains(boolean fromInternet) {
     	// need to find the files somehow
         ArrayList<String> fileLocations = new ArrayList<String>();
         fileLocations.add(shortTestBlue2);
         fileLocations.add(shortTestOrange);
         fileLocations.add(shortTestRed);
 
         Graph g = new Graph();
         g = getGraphFromInternet();
     	
         Station s = g.getStationByName("NULL_STATION");
         ArrayList<Train> arrT = new ArrayList<Train>();
         for (Edge e : s.getAllOutgoingEdges()) {
         	int time = e.getWeight();
         	String destination = e.getDestination();
         	
         	Train tempTrain = new Train(s.getName(), time, destination);
         	arrT.add(tempTrain);
         }
         
     	return arrT;
     }
 
     public static ArrayList<ArrayList<String>> permutationsOf(ArrayList<String> arrs) {
     	ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
     	int length = arrs.size();
     	
     	if (length == 1) {
     		result.add(arrs);
     		return result;
     	} else {
     		String first = arrs.get(0);
     		ArrayList<String> rest = getSubset(arrs, 1, (length));
     		ArrayList<ArrayList<String>> simpler = permutationsOf(rest);
     		
     		for (ArrayList<String> permutation : simpler) {
     			ArrayList<ArrayList<String>> additions = insertAtAllPositions(first, permutation);
     			result.addAll(additions);
     			}
     		return result;
     	}
     }
     
     private static ArrayList<ArrayList<String>> insertAtAllPositions(String s, ArrayList<String> arrs) {
     	ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
     	for (int i = 0; i < arrs.size(); i++) {
     		ArrayList<String> inserted = getSubset(arrs, 0, i);
     		inserted.add(s);
     		inserted.addAll(getSubset(arrs, i, (arrs.size())));
     		result.add(inserted);
     	}
     	return result;
     }
     private static ArrayList<String> getSubset(ArrayList<String> arrs, int from, int to) {
     	ArrayList<String> temp = new ArrayList<String>();
     	
     	for (int i = from; i < to; i++) {
     		temp.add(arrs.get(i));
     	}
     	return temp;
     }
     
    public static ArrayList<Train> getAllTrainsIntoStation(String s, boolean fromInternet){
         Graph g = new Graph();
         g = getGraphFromInternet();
         
        //Station s = g.getStationByName("NULL_STATION");
        ArrayList<Train> arrt = new ArrayList<Train>();
        Station station = g.getStationByName(s);
        
         ArrayList<Station> arrS = new ArrayList<Station>();
        for (Edge e : station.getAllIncomingEdges()) {
         	// int time = e.getWeight();
         	// String destination = e.getDestination();
        	String stationName = station.getName();
        	int arrivalTime = e.getWeight();
        	String destination = e.getDestination();
        	Train tempTrain = new Train(stationName, arrivalTime, destination);
         	
        	arrt.add(tempTrain);
         }
        return arrt;
     }
 }
 
