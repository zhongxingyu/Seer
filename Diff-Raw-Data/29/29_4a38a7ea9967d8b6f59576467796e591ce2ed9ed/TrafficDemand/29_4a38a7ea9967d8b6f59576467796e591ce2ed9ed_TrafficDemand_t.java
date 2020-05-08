 package nl.tudelft.otsim.TrafficDemand;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import nl.tudelft.otsim.Activities.ConnectZones;
 import nl.tudelft.otsim.FileIO.ParsedNode;
 import nl.tudelft.otsim.FileIO.StaXWriter;
 import nl.tudelft.otsim.GUI.Model;
 import nl.tudelft.otsim.GUI.Storable;
 import nl.tudelft.otsim.GUI.WED;
 import nl.tudelft.otsim.GeoObjects.ActivityLocation;
 import nl.tudelft.otsim.GeoObjects.MicroZone;
 import nl.tudelft.otsim.GeoObjects.Network;
 import nl.tudelft.otsim.GeoObjects.Node;
 import nl.tudelft.otsim.ShortesPathAlgorithms.DijkstraAlgorithm;
 import nl.tudelft.otsim.ShortesPathAlgorithms.Path;
 import nl.tudelft.otsim.SpatialTools.SpatialQueries;
 import nl.tudelft.otsim.Utilities.Sorter;
 
 /**
  * Traffic Demand: describes the characteristics of trip patterns that arise from the activities by the population
  * 
  * @author gtamminga
  */
 public class TrafficDemand implements Storable {
 	/** XML node in for a TrafficDemand */
 	public static final String XMLTAG = "Demand";
     /** Extension of traffic demand file */
 	public static final String FILETYPE = "otsd";
 	
 	private Model model;
 	private ArrayList<TripPattern> tripPatternList = new ArrayList<TripPattern>();
 	private HashMap<String, TrafficClass> trafficClasses = new HashMap<String, TrafficClass>();
 	/** To identify  a Zone in the XML file with the description of TripPatterns */
 	public static final String ZONE_PREFIX = "z";
 	/** To identify  an Activity Location in the XML file with the description of TripPatterns */
     public static final String ACTIVTITYLOCATION_PREFIX = "a";
     private boolean modified = false;
 
     /**
 	 * Create new TrafficDemand
 	 * @param model {@link Model} that will own this TrafficDemand
 	 * list of successive locations (zones, activities, nodes) where activities are conducted 
 	 */
     public TrafficDemand(Model model) {
 		this.model = model;
 	}
     
     /**
      * Look up a {@link TrafficClass}.
      * @param name String; name of the {@link TrafficClass}
      * @return {@link TrafficClass}; the TrafficClass with the specified name, 
      * or null if no TrafficClass with the specified name exists
      */
     public TrafficClass lookupTrafficClass(String name) {
     	return trafficClasses.get(name);
     }
 	
 	public TrafficDemand(Model model, ArrayList<TripPattern> tripPatternList) {
 		this.model = model;
 		this.tripPatternList = tripPatternList;
 	}
 
 	public TrafficDemand(Model model, ParsedNode demandRoot) throws Exception {
 		this.model = model;
 		//System.out.print(demandRoot.toString(""));
 		double sumFractions = 0;
 		for (int index = 0; index < demandRoot.size(TrafficClass.XMLTAG); index++) {
 			TrafficClass tc = new TrafficClass(demandRoot.getSubNode(TrafficClass.XMLTAG, index));
 			trafficClasses.put(tc.getName(), tc);
 			sumFractions += tc.getDefaultFraction();
 		}
 		if ((trafficClasses.size() > 0) && (Math.abs(sumFractions - 1.0) > 0.0001))
 			throw new Exception("Sum of " + TrafficClass.XMLTAG + " probabilities is not 1.0");
 		for (int index = 0; index < demandRoot.size(TripPattern.XMLTAG); index++)
 			tripPatternList.add(new TripPattern(this, demandRoot.getSubNode(TripPattern.XMLTAG, index)));
 	}
 	
	/**
	 * Rebuild this TrafficDemand after something may have changed.
	 */
 	public void rebuild() {
		if (0 == trafficClasses.size()) {
			// Create two simple classes
			trafficClasses.put("passengerCar", new TrafficClass("passengerCar", 0.9, 4.0, 160.0, 1.0));
			trafficClasses.put("truck", new TrafficClass("truck", 0.1, 15.0, 85.0, 1.0));
		}
 		if (this.tripPatternList != null)
 			createLocationToNodesList();
 	}
 
 	/**
 	 * This methods creates node Paths
 	 * The {@link TripPattern} list contains information of different types of locations:
 	 * {@link MicroZone} or {@link nl.tudelft.otsim.GeoObjects.PolyZone} objects; these zones may refer to a list of 
 	 * {@link Node} elements 
 	 * {@link ActivityLocation} objects
 	 * {@link Node} objects
 	 * @param tripPatternList
 	 */
 	private void createLocationToNodesList()   {
 		/*		
 		 * Initially a list of TripPatterns is read with an ordered list of activity locations
 		 * This method connects the locations to nodes in the network
 		 * Every zone has a list of associated nodes
 		 * For every activity location the nearest node is looked up
 		 */	
 		for (TripPattern tripPattern : tripPatternList) {
 			int numberOfLocations = tripPattern.getLocationList().size();
 			double numberOfTrips = tripPattern.getNumberOfTrips();
 			ArrayList<ArrayList<Node>> listNodeList = new ArrayList<ArrayList<Node>>();
 			
 			/* The total amount of possible paths:
 			 * For instance we have a Pattern "Z12 Z13 Z15" that represents activities at three locations
 			 * And the corresponding nodes are:
 			 * - Z12: node numbers 6, 8 and 9
 			 * - Z13: node numbers 7, 12 and 12
 			 * - Z15: node numbers 122 and 125
 			 *  the total amount of paths is 18 (3*3*2)
 			*/
 			int totalPathCombinations = 1;
 			for (int i=0; i < numberOfLocations; i++) {
 				String locationList = (String) tripPattern.getLocationList().get(i);
 				// Retrieve the separate locations 
 				for (String location: locationList.split("\\s")) { 
 					int n = 0;
 					ArrayList<Node> nodeList = new ArrayList<Node>();
 					/*
 					 * If the trip pattern uses a {@link Zone} as location type, connect the zones with 
 					 * the associated nodes 
 					 * Else if the location is a ActivityLocation search the nearest node/link/lane
 					 */
 					if (location.startsWith(ZONE_PREFIX)) {
 						int locationNumber = Integer.parseInt(location.substring(1));
 						MicroZone microZone = model.network.lookupMicroZone(locationNumber);
 						if (null == microZone)
 							WED.showProblem(WED.ENVIRONMENTERROR, "Cannot find micro zone %d", locationNumber);
 						else
 							for (Integer nodeNumber : microZone.getNodeList())  {
 								Node node = model.network.lookupNode(nodeNumber, true);
 								nodeList.add(node);
 								n++;
 							}
 					} else if (location.startsWith(ACTIVTITYLOCATION_PREFIX))  {
 						int locationNumber = Integer.parseInt(location.substring(1));
 						ActivityLocation activityLocation = model.network.lookupActivityLocation(locationNumber);									
 						double bound = 1000;
 						if (activityLocation != null) {
 							double maxSpeed = 80;
 							double maxSearchDistance = 1000;
 							// find the nearest node 
 							ConnectZones.NearestPointAtLink(SpatialQueries.createLinkQuery(model.network.getLinkList(), bound), activityLocation, maxSearchDistance, maxSpeed);
 							nodeList.add(activityLocation.getFromNodeNearLocation());
 						}
 						n++;
 					}
 					totalPathCombinations = totalPathCombinations * n;
 					listNodeList.add(nodeList);
 				}
 			}
 			numberOfLocations = listNodeList.size();
 			double weight = 1.0 / totalPathCombinations;
 			// for every ActivityLocation or Zone we create an index pointing to the corresponding Nodes 
 			int[] index = new int[numberOfLocations];
 			// loop through all corresponding nodes and start with activity/zone with index 0 and 1
 			int start = 0;
 			int next = 1;
 			int step = 1;
 			TripPatternPath tripPatternPath = null;
 			tripPattern.setTripPatternPathList(new ArrayList<TripPatternPath>());
 			// for all combinations of nodes (associated to a tripPattern from A, B, C etc.) 
 			// the routes are being prepared
 			for (int i = 0; i < totalPathCombinations; i++) {				
 				ArrayList<Node> nodeList = new ArrayList<Node>();
 				// add the first combination of nodes (all index values are 0)
 				for (int j = 0; j < numberOfLocations; j++)
 					nodeList.add(listNodeList.get(j).get(index[j]));
 				index[start]++;
 				// when reaching the last item 
 				if (index[start] >= listNodeList.get(start).size()) {
 					// reset the index[start] to 0
 					index[start] = 0;
 					if (next > start + step) {
 						index[start + step]++;
 						if (index[start + step] >= listNodeList.get(start + step).size())  {
 							index[start + step] = 0;
 							step++;
 							index[start + step]++;
 							if (next == start + step) {
 								start = 0;
 								index[next] = 0;
 								next++;
 								index[next]++;
 								step = 1;
 							}
 							step = 1;
 						}
 					} else {
 						start = 0;
 						index[next]++;
 						while (index[next] >= listNodeList.get(next).size()  && next < index.length) {
 							index[next] = 0;
 							next = next + 1;
 							if (next < index.length)
 								index[next]++;
 							else
 								break;
 						}							
 					}
 				}
 				
 				tripPatternPath = new TripPatternPath(tripPattern,  weight * numberOfTrips, nodeList);
 				// Create the associated Paths, with only the departing and arriving node
 				// the Paths will be expanded later by the createShortestPath method
 				Node prevNode = null;
 				for (Node node : nodeList) {
 					if (null != prevNode) {
 						ArrayList<Node> nodePair = new ArrayList<Node>(2);
 						nodePair.add(prevNode);
 						nodePair.add(node);
 						tripPatternPath.addPath(new Path(tripPatternPath, nodePair));
 					}
 					prevNode = node;					
 				}
 				tripPattern.addTripPatternPath(tripPatternPath);
 			}	
 		}
 	}
 
 /*	public TripPattern createTripPatternList(PersonTripPlan personTripPlan) {
 		LinkedList<Long> timeList = new LinkedList<Long>();
 		for (ActivityType activityType: personTripPlan.getActivityTypeList()) {
 			Long startTimeSeconds = ( activityType.getPreferredStartTime().getTime() - model.settings.getSimulationSettings().getStartTimeSimulation().getTime() ) / 1000;
 			timeList.add(startTimeSeconds);
 		}		
 		TripPattern tripPattern = new TripPattern(personTripPlan.getMovingPerson(), 
 				personTripPlan.getActivityLocationList(), timeList);
 		return tripPattern;
 	}*/
 	
 	/**
 	 * The export of traffic demand to the simulation invokes conversion the list of trip patterns 
 	 * Data are written to the result and imported in the LaneSimulator
 	 * As we have sequential trips, we have to check that a connecting trip does not start before finishing the activity!
 	 */
 /*    public String exportTrafficDemand() {
     	String result = "";
     	//List<Lane> laneList = createLaneList();
 		for(TripPattern tripPattern: tripPatternList) {
 			result += String.format(Main.locale, "TripChain\tPersonID\t%d", tripPattern.getMovingPerson().getID());
 			java.util.Iterator<ActivityLocation> itActivityLocation = tripPattern.getActivityLocationList().iterator();
 			java.util.Iterator<Long> itDepartureTime = tripPattern.getDepartureTimeList().iterator();
 			while (itActivityLocation.hasNext())  {
 				ActivityLocation activityLocation = itActivityLocation.next();
 				Long time = itDepartureTime.next();
 				result += String.format(Main.locale, "\tTime\t%d", time);
 				result += String.format(Main.locale, "\tBuilding\tX\t%f", activityLocation.getX());
 				result += String.format(Main.locale, "\tY\t%f", activityLocation.getY());
 				result += String.format(Main.locale, "\tZ\t%f", activityLocation.getZ());
 				result += "\n";
 			}
 		}
 		return result;
     }*/
 
 	public ArrayList<TripPattern> getTripPatternList() {
 		return tripPatternList;
 	}
 
 	/**
 	 * Modify the TripPattern list.
 	 * @param tripPatternList ArrayList<TripPattern>; 
 	 */
 	public void setTripPatternList(ArrayList<TripPattern> tripPatternList) {
 		this.tripPatternList = tripPatternList;
 		modified = true;
 	}
 
 	/**
 	 * write the TripPattern list to file.
 	 */
 	private boolean writeTripPatternsXML(StaXWriter staXWriter) {
 		for (TripPattern tp : getTripPatternList())
 			if (! tp.writeXML(staXWriter))
 				return false;
 		return true;
 	}
 	
 	/**
 	 * Export this TrafficDemand in textual form.
 	 * @return String; the textual representation of this TrafficDemand
 	 */
     public String exportTripPattern() {
     	String result = "";
     	for (String tcName : trafficClassNames()) {
     		TrafficClass tc = lookupTrafficClass(tcName);
     		result += String.format(Locale.US, "TrafficClass\t%s\t%.3f\t%.3f\t%.3f\n", tcName, tc.getLength(), tc.getMaximumSpeed(), tc.getMaximumDeceleration());
     	}
     	CreatePathsTripPatterns();
         for (TripPattern tripPattern : getTripPatternList()) {
         	Double totalTrips = tripPattern.getNumberOfTrips();
         	result += String.format(Locale.US, "TripPattern\tnumberOfTrips:\t%.2f\tLocationPattern:\t%s\tFractions%s\n", totalTrips, tripPattern.getLocationList().toString(), tripPattern.getClasslFlows());
             for (TripPatternPath tripPatternPath : tripPattern.getTripPatternPathList()) {
             	double numberOfTrips = 0;
             	if (totalTrips > 0)
             		numberOfTrips = tripPatternPath.getNumberOfTrips();
             	result += String.format(Locale.US, "TripPatternPath\tnumberOfTrips:\t%.3f\tNodePattern:\t%s\n", numberOfTrips, tripPatternPath.getNodeList().toString());
 				int index = 0;
             	for (Path path : tripPatternPath.getPathList()) {
 		     		result += String.format("Path:\t%d\tnodes:", index);
             		for (Node node : path.getNodeList())
 			     		result += String.format("\t%d", node.getNodeID());
     				result += "\n";
     				index++;
             	}
             }
 		}
 		return result;
     }
 
 	@Override
 	public boolean writeXML(StaXWriter staXWriter) {
 		return staXWriter.writeNodeStart(XMLTAG)
 				&& writeTrafficClassesXML(staXWriter)
 				&& writeTripPatternsXML(staXWriter)
 				&& staXWriter.writeNodeEnd(XMLTAG);
 		// TODO Should also write the ActivityLocationIDList of the tripPattern
 		
 	}
 
 	private boolean writeTrafficClassesXML(StaXWriter staXWriter) {
 		for (String name : Sorter.asSortedList(trafficClasses.keySet()))
 			if (! trafficClasses.get(name).writeXML(staXWriter))
 				return false;
 		return true;
 	}
 
 	@Override
 	public String description() {
 		return "traffic demand";
 	}
 
 	@Override
 	public String fileType() {
 		return FILETYPE;
 	}
 
 	@Override
 	public boolean isModified() {
 		return modified;
 	}
 
 	@Override
 	public void clearModified() {
 		modified = false;
 	}
 
 	private String fileName = null;
 	
 	@Override
 	public String storageName() {
 		return fileName;
 	}
 
 	@Override
 	public void setStorageName(String name) {
 		fileName = name;
 	}
 
 	/**
 	 * Retrieve the set of names of the defined (@link TrafficClass TrafficClasses}.
 	 * @return Set&lt;String&gt;; the set of names of the defined {@link TrafficClass TrafficClasses}
 	 */
 	public List<String> trafficClassNames() {
 		return Sorter.asSortedList(trafficClasses.keySet());
 	}
 	
     /**
      * Create paths between all pairs of nodes, if possible.
      * Save the paths in a list of paths
      */
     private void CreatePathsTripPatterns() {
     	class CompareNodeNumbers implements Comparator<Path> {
 			@Override
 			public int compare(Path path1, Path path2) {
 				return path1.getNodeList().get(0).getNodeID() - path2.getNodeList().get(0).getNodeID();
 			}
 		}
     	
     	// Generate a list of all paths from the tripPatternList
     	ArrayList<Path> pathList = new ArrayList<Path>();
 		for (TripPattern tp : tripPatternList)
 			for (TripPatternPath tpp : tp.getTripPatternPathList())
 				pathList.addAll(tpp.getPathList());
 		// sort pathList by NodeID of start node
 		Collections.sort(pathList, new CompareNodeNumbers());
 		
 		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(model.network);
 		// Collect the starting nodes of all paths in pathList
 		ArrayList<Node> startNodes = new ArrayList<Node>();
 		Node prevNode = null;
 		for (Path path : pathList) {
 			Node startNode = path.getNodeList().get(0);
 			if ((null == prevNode) || (startNode.getNodeID() != prevNode.getNodeID()))
 				startNodes.add(startNode);
 			prevNode = startNode;
 		}
 		// TODO There is no need for ArrayList<Node> startNodes; the loop below 
 		// that uses one startNode at a time can be merged with the loop above 
 		// that generates the startNodes.
         int pathIndex = 0;
 		for (Node startNode: startNodes) {
 	        // Find all routes from a certain node to all other nodes
 	        dijkstra.execute(startNode);
 	        
 	        while (pathIndex < pathList.size()) {
 	        	Path path = pathList.get(pathIndex);
 	        	if (path.getNodeList().get(0).getNodeID() > startNode.getNodeID())
 	        		break;
 	        	if (path.getNodeList().get(0).equals(startNode)) {
 			        Node toNode = path.getNodeList().get(path.getNodeList().size() - 1);
 			        Node fromNode = path.getNodeList().get(0);
 		        	if (fromNode.equals(startNode)) {
 		        		ArrayList<Node> getPath = dijkstra.getPathNodes(toNode);
 		        		if (getPath != null)
 		        			path.setNodeList(getPath);
 		        		else	// TODO Do you really want to continue when this happens???
 		        			System.out.println("no valid path found between " + fromNode.getNodeID() + " and " + toNode.getNodeID());
 		        	}
 				}
 	        	pathIndex++; 
         	}
         }
     }
 
 
 }
