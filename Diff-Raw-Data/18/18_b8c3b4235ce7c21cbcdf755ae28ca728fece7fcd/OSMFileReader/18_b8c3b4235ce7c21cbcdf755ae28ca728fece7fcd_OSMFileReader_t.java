 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Blob;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 import java.util.Vector;
 
 import org.openstreetmap.osm.data.IDataSet;
 import org.openstreetmap.osm.data.MemoryDataSet;
 import org.openstreetmap.osm.data.Selector;
 import org.openstreetmap.osm.data.coordinates.Bounds;
 import org.openstreetmap.osm.data.coordinates.LatLon;
 import org.openstreetmap.osmosis.core.domain.v0_6.Node;
 import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
 import org.openstreetmap.osmosis.core.domain.v0_6.Way;
 import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
 import org.openstreetmap.travelingsalesman.routing.IRouter;
 import org.openstreetmap.travelingsalesman.routing.IVehicle;
 import org.openstreetmap.travelingsalesman.routing.Route;
 import org.openstreetmap.travelingsalesman.routing.routers.MultiTargetDijkstraRouter;
 
 import com.kangaroo.ActiveDayPlan;
 import com.kangaroo.DayPlan;
 import com.kangaroo.DayPlanConsistency;
 import com.kangaroo.DayPlanOptimizer;
 import com.kangaroo.GreedyTaskInsertionOptimizer;
 import com.kangaroo.calendar.CalendarAccessAdapter;
import com.kangaroo.calendar.CalendarAccessAdapterMemory;
 import com.kangaroo.calendar.CalendarEvent;
 import com.kangaroo.task.Task;
 import com.kangaroo.task.TaskConstraintDate;
 import com.kangaroo.task.TaskConstraintDayTime;
 import com.kangaroo.task.TaskConstraintDuration;
import com.kangaroo.task.TaskConstraintHelper;
 import com.kangaroo.task.TaskConstraintLocation;
 import com.kangaroo.task.TaskConstraintPOI;
import com.kangaroo.task.TaskPriorityComparator;
 import com.kangaroo.tsm.osm.io.FileLoader;
 import com.mobiletsm.osm.MobileTSMDatabaseWriter;
 import com.mobiletsm.osm.OsmHelper;
 import com.mobiletsm.osm.data.MobileDataSet;
 import com.mobiletsm.osm.data.adapters.RoutingSQLiteAdapter;
 import com.mobiletsm.osm.data.providers.DatabaseMDSProvider;
 import com.mobiletsm.osm.data.providers.MobileDataSetProvider;
 import com.mobiletsm.osm.data.searching.CombinedSelector;
 import com.mobiletsm.osm.data.searching.POICode;
 import com.mobiletsm.osm.data.searching.POINodeSelector;
 import com.mobiletsm.osmosis.core.domain.v0_6.MobileWay;
 import com.mobiletsm.routing.AllStreetVehicle;
 import com.mobiletsm.routing.Place;
 import com.mobiletsm.routing.RouteParameter;
 import com.mobiletsm.routing.RoutingEngine;
 import com.mobiletsm.routing.Vehicle;
 import com.mobiletsm.routing.metrics.MobileRoutingMetric;
 import com.mobiletsm.routing.routers.MobileMultiTargetDijkstraRouter;
 
 
 
 public class OSMFileReader {
 	
 	
 	public static void testTaskPriorityComparator() {
 		
 		Task task1 = new Task();
 		task1.setName("task1");
 		task1.addConstraint(new TaskConstraintDuration(5));
 		task1.addConstraint(new TaskConstraintPOI(new POICode(POICode.SHOP_BAKERY)));
 		
 		Task task2 = new Task();
 		task2.setName("task2");
 		task2.addConstraint(new TaskConstraintDuration(10));
 		task2.addConstraint(new TaskConstraintDate(new Date(2010 - 1900, 3, 10), new Date(2010 - 1900, 5, 2)));
 		task2.addConstraint(new TaskConstraintDayTime(new Date(2010 - 1900, 5, 2, 15, 59), 
 				new Date(2010 - 1900, 5, 2, 18, 00)));
 		
 		Task task3 = new Task();
 		task3.setName("task3");
 		task3.addConstraint(new TaskConstraintDuration(5));
 		task3.addConstraint(new TaskConstraintDate(new Date(2010 - 1900, 4, 2)));
 		
 		DayPlan dayPlan = new DayPlan();
 		dayPlan.addTask(task1);
 		dayPlan.addTask(task2);
 		dayPlan.addTask(task3);
 		
 		List<Task> tasks = new ArrayList<Task>(dayPlan.getTasks());
 		
 		Collections.sort(tasks, new TaskPriorityComparator());
 		
 		Iterator<Task> task_itr = tasks.iterator();
 		int i = 0;
 		while (task_itr.hasNext()) {
 			
 			Task task = task_itr.next();
 			
 			TaskConstraintHelper helper = new TaskConstraintHelper(task);
 			Date now = new Date(2010 - 1900, 3, 12, 15, 00);
 			//System.out.println(now.toString());
 			
 			System.out.println(">" + i + " " + task.toString() + ", isAllowed(Date) = " + 
 					helper.isAllowed(now));
 			i++;
 		}
 		
 	}
 	
 	
 	public static void testActiveDayPlan() {
 		
 		RoutingEngine routingEngine = new TestRoutingEngine();
 		routingEngine.init("jdbc:sqlite:/Users/andreaswalz/Downloads/maps/out/map-fr.db");
 		routingEngine.enableRoutingCache();
 		
 		
 		/* create and add some events */
         
        CalendarAccessAdapter adapter = new CalendarAccessAdapterMemory();
         ActiveDayPlan activeDayPlan = new ActiveDayPlan();
         activeDayPlan.setCalendarAccessAdapter(adapter);
         
         Date now = new Date(2010 - 1900, 3, 10, 19, 00);
         Place home = new Place(48.0064241, 7.8521991);
         Vehicle vehicle = new AllStreetVehicle(50.0);
         
         
         CalendarEvent event1 = new CalendarEvent();
         event1.setStartDate(new Date(2010 - 1900, 3, 10, 19, 30));
         event1.setEndDate(new Date(2010 - 1900, 3, 10, 20, 00));
         event1.setLocationLatitude(48.00);
         event1.setLocationLongitude(7.852);
 
         CalendarEvent event2 = new CalendarEvent();
         event2.setStartDate(new Date(2010 - 1900, 3, 10, 20, 45));
         event2.setEndDate(new Date(2010 - 1900, 3, 10, 21, 00));
         event2.setLocationLatitude(48.000);
         event2.setLocationLongitude(7.852);
 
         CalendarEvent event3 = new CalendarEvent();
         event3.setStartDate(new Date(2010 - 1900, 3, 10, 21, 20));
         event3.setEndDate(new Date(2010 - 1900, 3, 10, 21, 40));
         event3.setLocationLatitude(47.987);
         event3.setLocationLongitude(7.852);
 
         CalendarEvent event4 = new CalendarEvent();
         event4.setStartDate(new Date(2010 - 1900, 3, 10, 21, 45));
         event4.setEndDate(new Date(2010 - 1900, 3, 10, 21, 50));
         event4.setLocationLatitude(47.987);
         event4.setLocationLongitude(7.852);        
 
         CalendarEvent event5 = new CalendarEvent();
         event5.setStartDate(new Date(2010 - 1900, 3, 10, 22, 0));
         event5.setEndDate(new Date(2010 - 1900, 3, 10, 22, 40));
         event5.setLocationLatitude(47.983);
         event5.setLocationLongitude(7.852);        
 
         CalendarEvent event6 = new CalendarEvent();
         event6.setStartDate(new Date(2010 - 1900, 3, 10, 23, 0));
         event6.setEndDate(new Date(2010 - 1900, 3, 10, 23, 40));
         event6.setLocationLatitude(48.983);
         event6.setLocationLongitude(7.852);  
         
         CalendarEvent event7 = new CalendarEvent();
         event7.setStartDate(new Date(2010 - 1900, 3, 10, 23, 45));
         event7.setEndDate(new Date(2010 - 1900, 3, 10, 23, 50));
         event7.setLocationLatitude(47.983);
         event7.setLocationLongitude(7.852); 
         
         activeDayPlan.addEvent(event1);
         activeDayPlan.addEvent(event2);
         activeDayPlan.addEvent(event3);
         activeDayPlan.addEvent(event4);
         activeDayPlan.addEvent(event5);
         activeDayPlan.addEvent(event6);
         activeDayPlan.addEvent(event7);        
                 
         
         /* add and create some tasks */
         
 		Task task1 = new Task();
 		task1.setName("Schnell was essen");
 		task1.addConstraint(new TaskConstraintDuration(5));
 		task1.addConstraint(new TaskConstraintPOI(new POICode(POICode.AMENITY_FAST_FOOD)));
 		task1.addConstraint(new TaskConstraintDayTime(new Date(0, 0, 0, 19, 00), new Date(0, 0, 0, 20, 01)));
 		
 		Task task2 = new Task();
 		task2.setName("Frisr");
 		task2.addConstraint(new TaskConstraintDuration(3));
 		task2.addConstraint(new TaskConstraintPOI(new POICode(POICode.SHOP_HAIRDRESSER)));
 		task2.addConstraint(new TaskConstraintDayTime(18, 00, 23, 00));
 		
 		Task task3 = new Task();
 		task3.setName("Oma anrufen");
 		task3.addConstraint(new TaskConstraintDuration(3));
 		task3.addConstraint(new TaskConstraintDate(new Date(2010 - 1900, 5, 2)));
 		
 		Task task4 = new Task();
 		task4.setName("Brtchen kaufen");
 		task4.addConstraint(new TaskConstraintDuration(3));
 		task4.addConstraint(new TaskConstraintPOI(new POICode(POICode.SHOP_BAKERY)));		
 		//task4.addConstraint(new TaskConstraintDayTime(18, 00, 19, 10));
 		
 		Task task5 = new Task();
 		task5.setName("Blumen kaufen");
 		task5.addConstraint(new TaskConstraintDuration(30));
 		task5.addConstraint(new TaskConstraintPOI(new POICode(POICode.SHOP_FLORIST)));	
 		task5.addConstraint(new TaskConstraintDayTime(18, 00, 23, 00));
 
 		Task task6 = new Task();
 		task6.setName("Buch kaufen");
 		task6.addConstraint(new TaskConstraintDuration(30));
 		task6.addConstraint(new TaskConstraintPOI(new POICode(POICode.SHOP_BOOKS)));	
 		task6.addConstraint(new TaskConstraintDayTime(18, 00, 23, 00));
 		
 		
 		activeDayPlan.addTask(task1);
 		activeDayPlan.addTask(task2);
 		activeDayPlan.addTask(task3);
 		activeDayPlan.addTask(task4);        
 		activeDayPlan.addTask(task5);		
 		activeDayPlan.addTask(task6);		
         
         
         if (routingEngine.initialized()) {
 	        activeDayPlan.setRoutingEngine(routingEngine);
 
 			System.out.println("---> " + activeDayPlan.toString());
 			System.out.println("---> " + activeDayPlan.checkConsistency(vehicle, now).toString());
 	        
 	        /* check consistency */
 	        /*
 			DayPlanConsistency consistency = 
 				activeDayPlan.checkConsistency(vehicle, now);
 			if (consistency != null) {
 				System.out.println("consistency = " + consistency.toString());		
 			}
 			*/
 			
 			
 			/* optimize plan */
 			
 			DayPlanOptimizer optimizer = new GreedyTaskInsertionOptimizer();
 			activeDayPlan.setOptimizer(optimizer);
 			DayPlan optimizedDayPlan = activeDayPlan.optimize(now, home, vehicle);
 
 			System.out.println("---> " + optimizedDayPlan.toString());
 			System.out.println("---> " + optimizedDayPlan.checkConsistency(vehicle, now).toString());
 		}
         
         
         
         routingEngine.shutdown();
 		
 	}
 	
 	
 	public static IDataSet loadMapFile(String filename) {
 		File mapFile = new File(filename);		
 		IDataSet map = (new FileLoader(mapFile)).parseOsm();
 		System.out.println("FileLoader: output: # nodes = " + OsmHelper.getNumberOfNodes(map));
 		System.out.println("FileLoader: output: # ways = " + OsmHelper.getNumberOfWays(map));
 		return map;
 	}
 	
 	
 	public static void writeDatabase(IDataSet map, String filename) {
 		MobileTSMDatabaseWriter writer = new MobileTSMDatabaseWriter(filename);
 		writer.setLogStream(System.out);		
 		writer.openDatabase();
 		writer.writeDatabaseV2(map);
 		System.out.print("closing database...");
 		if (writer.closeDatabase()) {
 			System.out.println("successful!");
 		} else {
 			System.out.println("failed!");
 		}	
 	}
 	
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {	
 		
 		testActiveDayPlan();
 		
 		//testTaskPriorityComparator();		
 		
 		//IDataSet map = loadMapFile("/Users/andreaswalz/Downloads/maps/in/map-fr.osm");
 		
 		//writeDatabase(map, "jdbc:sqlite:/Users/andreaswalz/Downloads/maps/out/map-fr.db");
 		
 		//testTaskConstraintHelper();
 		
 		/*
 		// test routing cache
 		RoutingEngine routingEngine = new TestRoutingEngine();
 		routingEngine.init("jdbc:sqlite:/Users/andreaswalz/Downloads/maps/out/map.db");
 		
 		// Am Kurzarm
 		Place home = new Place(48.1208603, 7.8581893);
 		System.out.println("from.hashCode() = " + home.hashCode());		
 		
 		Place poi1 = routingEngine.getNearestPOINode(home, new POINodeSelector(POICode.AMENITY_SCHOOL), null);
 		System.out.println("poi1 = " + poi1.toString());		
 				
 		routingEngine.enableRoutingCache();
 		
 		RouteParameter route1 = routingEngine.routeFromTo(home, poi1, new AllStreetVehicle(5.0));		
 		
 		System.out.println(route1.toString());	
 		
 		Place poi2 = routingEngine.getNearestPOINode(home, new POINodeSelector(POICode.SHOP_BAKERY), null);
 		System.out.println("poi2 = " + poi2.toString());		
 		
 		RouteParameter route2 = routingEngine.routeFromTo(home, poi2, new AllStreetVehicle(5.0));		
 		
 		System.out.println(route2.toString());
 		
 		RouteParameter route3 = routingEngine.routeFromTo(poi1, poi2, new AllStreetVehicle(5.0));		
 		
 		System.out.println(route3.toString());
 
 		System.out.println(routingEngine.routeFromTo(home, poi2, new AllStreetVehicle(5.0)).toString());
 		routingEngine.shutdown();
 			
 		
 		// check equals() and hashCode() for Place
 		System.out.println("from.hashCode() = " + home.hashCode());
 		System.out.println("poi1.hashCode() = " + poi1.hashCode());
 		System.out.println("poi2.hashCode() = " + poi2.hashCode());
 		
 		System.out.println("poi1.equals(poi2) = " + poi1.equals(poi2));
 		System.out.println("poi1.equals(poi1) = " + poi1.equals(poi1));
 		
 		System.out.println("poi1.serialize() = " + poi1.serialize());
 		Place poi1_ = Place.deserialize(poi1.serialize());
 		System.out.println("poi1_.hashCode() = " + poi1_.hashCode());		
 		System.out.println("poi1_.equals(poi1) = " + poi1_.equals(poi1));
 		System.out.println("poi1_.equals(poi2) = " + poi1_.equals(poi2));		
 		
 		System.out.println("==> " + home.equals(Place.deserialize(home.serialize())));
 		System.out.println("==> " + poi1.equals(Place.deserialize(poi1.serialize())));
 		System.out.println("==> " + poi2.equals(Place.deserialize(poi2.serialize())));
 		System.out.println("==> " + poi1_.equals(Place.deserialize(poi1_.serialize())));
 		System.out.println("==> " + poi1_.equals(Place.deserialize(poi1.serialize())));
 		System.out.println("  > " + poi1_.equals(Place.deserialize(poi2.serialize())));
 		*/
 		
 		
 		/*
 		RoutingCache cache = new RoutingCache();		
 		cache.putElement(route);
 		System.out.println("without cache: " + route.toString());		
 		vehicle = new AllStreetVehicle(4.0);		
 		if (cache.hasElement(from, poi, vehicle)) {
 			System.out.println("from cache: " + cache.getElement(from, poi, vehicle).toString());
 		} else {
 			System.out.println("cache element not found!");
 		}
 		*/
 		
 		
 		
 		/*
 		POICode poiCode = new POICode(POICode.AMENITY_BANK);
 		Place home = new Place(48.1208603, 7.8581893); 
 		
 		Place poiPlace = provider.getNearestPOINode(home, new POINodeSelector(poiCode), null);
 		
 		if (poiPlace != null) {
 			System.out.println("Place = " + poiPlace.toString());
 			System.out.println("dist = " + home.distanceTo(poiPlace) + " Meter");
 		} else {
 			System.out.println("did not find any POI node of type " + poiCode.getType());
 		}
 		
 		provider.close();
 		*/
 		
 		/*
 		// check routing
 		long fromNodeId = 469369005;
 		long toNodeId = 251508961;
 
 		MobileDataSet routingMap = provider.getRoutingDataSet(fromNodeId, toNodeId, null);			
 		
 		System.out.println("provider.getRoutingDataSet: # nodes = " + routingMap.getNodesCount());
 		System.out.println("provider.getRoutingDataSet: # ways  = " + routingMap.getWaysCount());		
 		
 		//check(routingMap, 469369005, 251508961, 1307.8384132660117);
 		
 		
 		IRouter router = new MobileMultiTargetDijkstraRouter();
 		router.setMetric(new MobileRoutingMetric());
 		Node fromNode = routingMap.getNodeByID(fromNodeId);
 		Node toNode = routingMap.getNodeByID(toNodeId);	
 		
 		Route route = router.route(routingMap, toNode, fromNode, vehicle);
 		
 		OsmHelper.followRouteOnMap(routingMap, route, vehicle, System.out);
 		OsmHelper.followRouteOnMap(map, route, vehicle, System.out);
 		
 		provider.close();			
 		*/
 		
 	}
 	
 	
 	
 	private static void testTaskConstraintHelper() {
 		
 		RoutingEngine routingEngine = new TestRoutingEngine();
 		routingEngine.init("jdbc:sqlite:/Users/andreaswalz/Downloads/maps/out/map.db");
 		
 		// Am Kurzarm
 		Place home = new Place(48.1208603, 7.8581893);
 		
 		
 		Task task = new Task();
 		task.setName("task");
 		task.addConstraint(new TaskConstraintDuration(5));
 		task.addConstraint(new TaskConstraintDuration(9));
 		//task.addConstraint(new TaskConstraintLocation(home));
 		task.addConstraint(new TaskConstraintPOI(new POICode(POICode.SHOP_BAKERY)));
 		//task.addConstraint(new TaskConstraintPOI(new POICode(POICode.SHOP_SUPERMARKET)));
 		
 		TaskConstraintHelper helper = new TaskConstraintHelper(task);
 		
 		System.out.println("duration = " + helper.getDuration());
 //		System.out.println("location = " + helper.getLocation(home, null));
 		
 		routingEngine.shutdown();
 		
 	}
 
 
 	private static void check(IDataSet dataSet, long fromId, long toId, double dist) {
 		IRouter router = new MobileMultiTargetDijkstraRouter();
 		router.setMetric(new MobileRoutingMetric());
 		Route route = router.route(dataSet, dataSet.getNodeByID(toId), 
 				dataSet.getNodeByID(fromId), new AllStreetVehicle());
 		double myDist = OsmHelper.getRouteLength(route);
 		System.out.println("check: myDist = " + myDist + ", (external dist = " + dist + ")");
 	}
 	
 	
 	
 	public static void getBuckets(IDataSet map) throws Exception {
 		final double bucketSize_lat = 0.1;
 		final double bucketSize_lon = 0.1;
 		
 		Buckets buckets = new Buckets(map, bucketSize_lat, bucketSize_lon);
 		
 		Iterator<Node> nodes_itr = map.getNodes(null);
 		while (nodes_itr.hasNext()) {			
 			buckets.add(nodes_itr.next());			
 		}
 		
 		Iterator<Way> ways_itr = map.getWays(Bounds.WORLD);
 		while (ways_itr.hasNext()) {
 			buckets.add(ways_itr.next());
 		}
 		
 		List<Bucket> bucket_collection = buckets.getBuckets();
 		Collections.sort(bucket_collection);
 		Iterator<Bucket> bucket_itr = bucket_collection.iterator();
 		
 		int numberOfNodes = 0;
 		int numberOfWays = 0;
 		
 		int maxNodes = 0;
 		int maxWays = 0;
 		
 		while (bucket_itr.hasNext()) {
 			Bucket bucket = bucket_itr.next();
 			System.out.println(bucket.toString());
 			numberOfNodes += bucket.getNumberOfNodes();
 			numberOfWays += bucket.getNumberOfWays();
 			
 			if (bucket.getNumberOfNodes() > maxNodes)
 				maxNodes = bucket.getNumberOfNodes();
 			if (bucket.getNumberOfWays() > maxWays)
 				maxWays = bucket.getNumberOfWays();
 		}
 		
 		System.out.println("# buckets = " + bucket_collection.size());
 		System.out.println("# nodes = " + numberOfNodes);
 		System.out.println("# ways = " + numberOfWays);
 		System.out.println("max # nodes = " + maxNodes);
 		System.out.println("max # ways = " + maxWays);
 		
 		//buckets.drawBucketMatrix(System.out);
 	}
 	
 	
 	public static void execSQL(String sql, Statement statement) throws SQLException {
 		if (statement != null) {
 			try {
 				statement.executeUpdate(sql);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				System.out.println(e.toString());
 				System.out.println(sql);
 			}	
 		} else
 			System.out.println(sql);
 	}
 		
 	
 	public static String escape(String input) {
 		return input.replaceAll("'", "''");		
 	}
 	
 }
 
 
 
 
 /* STUFF:
 
 
 		// test amenity selector
 		Amenity foo = new Amenity(Amenity.SCHOOL);
 		System.out.println("id = " + foo.getId() + ", type = " + foo.getType());		
 		AmenityPOINodeSelector sel1 = new AmenityPOINodeSelector();
 		AmenityPOINodeSelector sel2 = new AmenityPOINodeSelector(foo);		
 		Collection<Tag> tags = new Vector<Tag>();
 		tags.add(new Tag("amenity", Amenity.SCHOOL));
 		Node node = new Node(0, 0, (Date)null, null, 0, tags, 0, 0);		
 		System.out.println("sel1.isAllowed() = " + sel1.isAllowed(null, node));
 		System.out.println("sel2.isAllowed() = " + sel2.isAllowed(null, node));
 		
 		
 		
 		// compare routing on two maps
 		IDataSet routingMap = OsmHelper.simplifyDataSet(map, new AllStreetVehicle());
 		OsmHelper.compareRouting(map, routingMap, new AllStreetVehicle(), System.out);
 		
 
 		//<bounds minlat="48.032" minlon="7.784" maxlat="48.147" maxlon="8.011"/>
 		double minLat = 48.032;
 		double maxLat = 48.147;
 		double minLon = 7.784;
 		double maxLon = 8.011;
 		
 		double a1 = Place.distance(minLat, minLon, minLat, maxLon);
 		double a2 = Place.distance(maxLat, minLon, maxLat, maxLon);
 		double b = Place.distance(minLat, minLon, maxLat, minLon);
 		
 		System.out.println("a1 = " + a1);
 		System.out.println("a2 = " + a2);
 		System.out.println("b = " + b);
 		
 		System.out.println("area = " + (a1*b)/1000000 + " km^2");
 
 
 
 */
