 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 
 public class Statistics {
 	
 	private static ScoreLabel totalCollisions, totalInfectiousCollisions;
 	
 	// time elapsed -> (virus -> count)
 	private static TreeMap<Long, HashMap<Virus, Integer>> virusCount = new TreeMap<Long, HashMap<Virus, Integer>>();
 	// time before virus extinction
 	private static HashMap<Virus, Long> extinction = new HashMap<Virus, Long>();
 	
 	private static HashSet<Person[]> hardConnections = new HashSet<Person[]>();
 	private static HashSet<Person[]> softConnections = new HashSet<Person[]>();
 	private static HashMap<Person, Integer> connectionCounts = new HashMap<Person, Integer>();
 	
 	private static HashSet<LinkedList<Person>> components = new HashSet<LinkedList<Person>>();
 	
 	private static TreeMap<Long, Double> numComponents = new TreeMap<Long, Double>();
 	private static TreeMap<Long, Double> maxComponentSizes = new TreeMap<Long, Double>();
 	private static TreeMap<Long, Double> numHardConnections = new TreeMap<Long, Double>();
 	private static TreeMap<Long, Double> hardSoftRatio = new TreeMap<Long, Double>();
 	private static TreeMap<Long, Double> clusteringCoeff = new TreeMap<Long, Double>();
 	private static TreeMap<Long, Double> largestPhysicalDistance = new TreeMap<Long, Double>();
 	private static TreeMap<Long, Double> averagePhysicalDistance = new TreeMap<Long, Double>();
 	private static TreeMap<Long, Double> infectiousCollisions = new TreeMap<Long, Double>();
 	
 	private static StopWatch stopWatch = new StopWatch();
 	
 	private static VirusStatsComparator vsc = new VirusStatsComparator();
 	
 	private static long lastRetrievedVirusCount = 0;
 	
 	private static int numViruses = 0, numPeople = 0;
 	private static int maxCount = 0, maxNumComponents = 0, maxMaxComponentSize = 0;
 
 	private static double maxNumHardConnections = 0.0, maxHardSoftRatio = 0.0, maxClusteringCoeff = 0.0;
 	private static double maxLargestPhysicalDistance = 0.0, maxAveragePhysicalDistance = 0.0;
 	private static double maxInfectiousCollisions = 0.0;
 	private static double edgeProbDenom = 0.0;
 	
 	private static GraphType graphType = GraphType.COUNT;
 	
 	public static void setup(ScoreLabel c, ScoreLabel d) {
 		totalCollisions = c;
 		totalInfectiousCollisions = d;
 	}
 	
 	public static void setupPeople(Person[] people) {
 		HashMap<Virus, Integer> viruses = new HashMap<Virus, Integer>();
 		for (Person p : people) {
 			Virus v = p.getVirus();
 			Integer i = viruses.get(v);
 			if (i == null)
 				i = 0;
 			viruses.put(v, i+1);
 		}
 		numViruses = viruses.size();
 		numPeople = people.length;
 		edgeProbDenom = numPeople*(numPeople-1)/2.0;
 		lastRetrievedVirusCount = stopWatch.getElapsedTime();
 		virusCount.put(lastRetrievedVirusCount, viruses);
 	}
 	
 	public static boolean isOver() {
 		return extinction.size() == numViruses-1;
 	}
 	
 	public static void collision() {
 		totalCollisions.addValue(1);
 	}
 	
 	public static void transferVirus(Virus lost, Virus gained) {
 		HashMap<Virus, Integer> v = virusCount.get(lastRetrievedVirusCount);
 		HashMap<Virus, Integer> viruses = new HashMap<Virus, Integer>();
 		lastRetrievedVirusCount = stopWatch.getElapsedTime();
 		for (Map.Entry<Virus, Integer> e : v.entrySet()) {
 			Virus key = e.getKey();
 			int value = e.getValue();
 			if (key.equalsVirus(lost))
 				value--;
 			if (key.equalsVirus(gained))
 				value++;
 			if (value == 0 && extinction.get(key) == null)
 				extinction.put(key, lastRetrievedVirusCount);
 			if (value > maxCount)
 				maxCount = value;
 			viruses.put(key, value);
 		}
 		virusCount.put(lastRetrievedVirusCount, viruses);
 		totalInfectiousCollisions.addValue(1);
 	}
 	
 	public static void updateConnections(Person infected, Person infector) {
 		boolean sameColor = (infected.getVirus().equalsVirus(infector.getVirus()));
 		boolean sameColorConnectionExists = false;
 		Iterator<Person[]> it = hardConnections.iterator();
 		while (it.hasNext()) {
 			Person[] connection = it.next();
 			if (sameColor) {
 				if ((infected.equals(connection[0]) && infector.equals(connection[1])) ||
 						(infected.equals(connection[1]) && infector.equals(connection[0]))) {
 					sameColorConnectionExists = true;
 				}
 			}
 			// Remove connections to the connected person.
 			else if (infected.equals(connection[0]) || infected.equals(connection[1])) {
 				it.remove();
 				// Reduce connection count.
 				Integer connectionCount = connectionCounts.get(connection[0]);
 				if (connectionCount != null && connectionCount > 0)
 					connectionCounts.put(connection[0], connectionCount-1);
 				connectionCount = connectionCounts.get(connection[1]);
 				if (connectionCount != null && connectionCount > 0)
 					connectionCounts.put(connection[1], connectionCount-1);
 			}
 		}
 
 		// Create a new connection.
 		if (!sameColor || !sameColorConnectionExists) {
 			hardConnections.add(new Person[] {infected, infector});
 			// Increase connection count.
 			Integer infectedConnections = connectionCounts.get(infected);
 			if (infectedConnections == null)
 				infectedConnections = 0;
 			connectionCounts.put(infected, infectedConnections+1);
 			Integer infectorConnections = connectionCounts.get(infector);
 			if (infectorConnections == null)
 				infectorConnections = 0;
 			connectionCounts.put(infector, infectorConnections+1);
 		}
 		
 		boolean addNewSoftConnection = true;
 		for (Person[] connection : softConnections) {
 			// If we find a connection, don't add it again.
 			if ((infected.equals(connection[0]) && infector.equals(connection[1])) ||
 					(infected.equals(connection[1]) && infector.equals(connection[0]))) {
 				addNewSoftConnection = false;
 				break;
 			}
 		}
 		if (addNewSoftConnection)
 			softConnections.add(new Person[] {infected, infector});
 		
 		updateComponents();
 	}
 	
 	public static void updateComponents() {
 		if (ViralSpread.debug) { // Print out all of the connections.
 			System.out.println("----");
 			for (Person[] c : hardConnections) {
 				System.out.println(c[0] +"\t\t"+ c[1]);
 			}
 		}
 		
 		// Create an ordered copy of the list of connections so that we can modify it.
 		LinkedList<Person[]> connectionsCopy = new LinkedList<Person[]>();
 		for (Person[] connection : hardConnections)
 			connectionsCopy.add(connection);
 		// Discover the set of components.
 		HashSet<LinkedList<Person>> newComponents = new HashSet<LinkedList<Person>>();
 		updateComponentConnections(newComponents, connectionsCopy);
 		components = newComponents;
 		
 		if (ViralSpread.debug) { // Print out each component.
 			System.out.println();
 			for (LinkedList<Person> component : components) {
 				System.out.println(component.size() +" "+ component.get(0).getVirus());
 				for (Person p : component) {
 					System.out.println("\t"+ p);
 				}
 			}
 		}
 	}
 	
 	private static void updateComponentConnections(
 			HashSet<LinkedList<Person>> components,
 			LinkedList<Person[]> connections
 			) {
 		// We don't have any components yet among our list of connections,
 		// and none of the nodes in the connections should be in an existing component,
 		// so start the chain by adding the nodes in the first connection to a new component.
 		LinkedList<Person> component = new LinkedList<Person>();
 		Person[] connection = connections.get(0);
 		component.add(connection[0]);
 		component.add(connection[1]);
 		// Remove the connection from the list since it has already been processed
 		connections.remove(connection);
 		// Look at all the connections and add them to the component if they are part of it.
 		LinkedList<Person[]> connectionsNotYetInComponents = new LinkedList<Person[]>();
 		processComponentConnections(connections, component, connectionsNotYetInComponents);
 		// We've now finished building the component, so add it to our set of components.
 		components.add(component);
 		// Repeat the process starting with the (now reduced) copy instead of the list of all connections.
 		if (connectionsNotYetInComponents.size() > 0)
 			updateComponentConnections(components, connectionsNotYetInComponents);
 	}
 	
 	private static void processComponentConnections(
 			LinkedList<Person[]> connections,
 			LinkedList<Person> component,
 			LinkedList<Person[]> connectionsNotYetInComponents) {
 		for (int i = 0; i < connections.size(); i++) {
 			Person[] connection = connections.get(i);
 			boolean aInComponent = false, bInComponent = false;
 			// If one of the nodes in the connection is in the component, add the other to the component too
 			if (objectInCollection(connection[0], component) && !objectInCollection(connection[1], component)) {
 				aInComponent = true;
 				component.add(connection[1]);
 			}
 			else if (objectInCollection(connection[1], component) && !objectInCollection(connection[0], component)) {
 				bInComponent = true;
 				component.add(connection[0]);
 			}
 			// If both nodes are in the collection, don't revisit the connection
 			else if (objectInCollection(connection[0], component) && objectInCollection(connection[1], component)) {
 				connectionsNotYetInComponents.remove(connection);
 				continue;
 			}
 			// If this connection doesn't connect to the component as we currently know it, save it for later
 			else if (!connectionsNotYetInComponents.contains(connection)) {
 				connectionsNotYetInComponents.add(connection);
 			}
 			// If we added to the component
 			if (aInComponent || bInComponent) {
 				// The connection has now been added, so remove it from the list of those that haven't
 				connectionsNotYetInComponents.remove(connection);
 				// Recursively check all the touched connections that aren't in the component yet.
 				// Since we just added a node, they might connect now.
 				processComponentConnections(connectionsNotYetInComponents, component, connectionsNotYetInComponents);
 			}
 		}
 	}
 	
 	private static <T> boolean objectInCollection(T item, Iterable<T> collection) {
 		for (Object member : collection)
 			if (member.equals(item))
 				return true;
 		return false;
 	}
 	
 	public static void startTiming() {
 		stopWatch.start();
 	}
 	
 	public static void stopTiming() {
 		stopWatch.stop();
 	}
 	
 	public static String getElapsedTime() {
 		int seconds = (int) (stopWatch.getElapsedTime()/1000);
 		return seconds +" second"+ (seconds == 1 ? "" : "s");
 	}
 	
 	public static HashMap<Virus, Integer> getVirusCounts() {
 		return virusCount.get(lastRetrievedVirusCount);
 	}
 
 	public static int getLargestPhysicalDistance() {
 		double maxDist = 0.0;
 		for (Person[] connection : hardConnections) {
 			double dist = Math.sqrt(
 					(connection[0].getX()-connection[1].getX())*(connection[0].getX()-connection[1].getX()) +
 					(connection[0].getY()-connection[1].getY())*(connection[0].getY()-connection[1].getY())
 			);
 			if (dist > maxDist)
 				maxDist = dist;
 		}
 		if (maxDist > maxLargestPhysicalDistance)
 			maxLargestPhysicalDistance = maxDist;
 		largestPhysicalDistance.put(stopWatch.getElapsedTime(), maxDist);
 		return (int) maxDist;
 	}
 	public static int getAveragePhysicalDistance() {
 		double totalDist = 0.0;
 		for (Person[] connection : hardConnections) {
 			double dist = Math.sqrt(
 					(connection[0].getX()-connection[1].getX())*(connection[0].getX()-connection[1].getX()) +
 					(connection[0].getY()-connection[1].getY())*(connection[0].getY()-connection[1].getY())
 			);
 			totalDist += dist;
 		}
 		double num = totalDist / hardConnections.size();
 		if (num > maxAveragePhysicalDistance)
 			maxAveragePhysicalDistance = num;
 		averagePhysicalDistance.put(stopWatch.getElapsedTime(), num);
 		return (int) num;
 	}
 
 	public static int getNumHardConnections() {
 		long time = stopWatch.getElapsedTime();
 		int num = hardConnections.size();
 		if (num / (double) numPeople > maxNumHardConnections)
 			maxNumHardConnections = num / (double) numPeople;
 		numHardConnections.put(time, num / (double) numPeople);
 		double soft = softConnections.size();
 		if (soft > 0.0) {
 			if (num / soft > maxHardSoftRatio)
 				maxHardSoftRatio = num / soft;
 			hardSoftRatio.put(time, num / soft);
 		}
 		double edgeProb = num / edgeProbDenom;
 		if (edgeProb > maxClusteringCoeff)
 			maxClusteringCoeff = edgeProb;
 		clusteringCoeff.put(time, edgeProb);
 		return num;
 	}
 	
 	public static int getNumSoftConnections() {
 		return softConnections.size();
 	}
 	
 	public static int getMaxConnections() {
 		int max = 0;
 		for (Map.Entry<Person, Integer> e : connectionCounts.entrySet()) {
 			if (e.getValue() > max)
 				max = e.getValue();
 		}
 		return max;
 	}
 	
 	public static double getPercentInfectiousCollisions() {
 		double fraction = totalInfectiousCollisions.getValue() / (double) totalCollisions.getValue();
 		if (fraction > maxInfectiousCollisions)
 			maxInfectiousCollisions = fraction;
 		infectiousCollisions.put(stopWatch.getElapsedTime(), fraction);
 		return Math.round(fraction * 1000.0) / 1000.0;
 	}
 	
 	public static int getNumComponents() {
 		int num = components.size();
 		if (num > maxNumComponents)
 			maxNumComponents = num;
 		numComponents.put(stopWatch.getElapsedTime(), (double) num);
 		return num;
 	}
 	
 	public static int getLargestConnectedComponent() {
 		int max = 0;
 		for (LinkedList<Person> component : components)
 			if (component.size() > max)
 				max = component.size();
 		if (max > maxMaxComponentSize)
 			maxMaxComponentSize = max;
 		maxComponentSizes.put(stopWatch.getElapsedTime(), max / (double) numPeople);
 		return max;
 	}
 	
 	public static int getDiameter() {
 		// Find the largest connected component.
 		LinkedList<Person> nodes = new LinkedList<Person>();
 		for (LinkedList<Person> component : components)
 			if (component.size() > nodes.size())
 				nodes = component;
 		// Calculate its diameter (find the longest shortest path).
 		long start = System.nanoTime();
 	    int diameter = 0;
 	    for (int i = 0; i < nodes.size(); i++) {
 	        for (int j = i+1; j < nodes.size(); j++) {
 	            int distance = distance(nodes.get(i), nodes.get(j));
 	            if (distance > diameter)
 	                diameter = distance;
 	            // Abort if we're taking too long.
 	            if (System.nanoTime() - start > 60000000)
 	            	return -diameter;
 	        }
 	    }
 	    return diameter;
 	}
 	// Get the shortest path between two nodes.
 	private static int distance(Person a, Person b) {
 		HashSet<Person> visited = new HashSet<Person>();
 		HashMap<Person, Person> previous = new HashMap<Person, Person>();
 		LinkedList<Person> queue = new LinkedList<Person>();
 		queue.add(a);
 		visited.add(a);
 		Person p = a;
 		while (!queue.isEmpty()) {
 			p = queue.remove();
 			if (p.equals(b)) {
 				break;
 			}
 			else {
 				for (Person q : getNeighbors(p)) {
 					if (!visited.contains(q)) {
 						queue.add(q);
 						visited.add(q);
 						previous.put(q, p);
 					}
 				}
 			}
 		}
 		if (!p.equals(b))
 			return 0; // No path found
		int distance = 0;
 		for (p = b; p != null; p = previous.get(p))
 			distance++;
 		return distance;
 	}
 	// Get all nodes connected to p.
 	private static HashSet<Person> getNeighbors(Person p) {
 		HashSet<Person> neighbors = new HashSet<Person>();
 		for (Person[] edge : hardConnections) {
 			if (edge[0].equals(p))
 				neighbors.add(edge[1]);
 			else if (edge[1].equals(p))
 				neighbors.add(edge[0]);
 		}
 		return neighbors;
 	}
 	
 	public static String getExtinctTime(Virus v) {
 		Long extinctTime = extinction.get(v);
 		if (extinctTime != null) {
 			int seconds = (int) (extinctTime/1000);
 			return seconds +" second"+ (seconds == 1 ? "" : "s");
 		}
 		return "alive";
 	}
 	
 	public static String getVirusCountTable() {
 		String output = "<html><table><thead><tr><td><u>Color</u></td><td><u>Count</u></td><td><u>Extinct</u></td></tr></thead><tbody>";
 		VirusStats[] viruses = getVirusStats();
 		for (VirusStats v : viruses) {
 			output += "<tr>";
 			output += "<td>"+ v.getVirusName() +"</td>";
 			output += "<td>"+ v.getCount() +"</td>";
 			output += "<td>"+ v.getExtinctTime() +"</td>";
 			output += "</tr>";
 		}
 		return output +"</tbody></table></html>";
 	}
 	
 	private static VirusStats[] getVirusStats() {
 		TreeMap<Integer, LinkedList<Virus>> invert = new TreeMap<Integer, LinkedList<Virus>>();
 		HashMap<Virus, Integer> virusCounts = getVirusCounts();
 		// Invert the map
 		for (Entry<Virus, Integer> entry : virusCounts.entrySet()) {
 			int count = entry.getValue();
 			Virus v = entry.getKey();
 			LinkedList<Virus> viruses;
 			// Create or update the bucket
 			if (invert.containsKey(count))
 				viruses = invert.get(count);
 			else
 				viruses = new LinkedList<Virus>();
 			viruses.add(v);	
 			invert.put(count, viruses);
 		}
 		// Append all of the buckets (they'll already be sorted thanks to the TreeMap)
 		LinkedList<Virus> answer = new LinkedList<Virus>();
 		for (LinkedList<Virus> l : invert.values()) {
 			if (l != null)
 				answer.addAll(l);
 		}
 		int k = answer.size();
 		VirusStats[] array = new VirusStats[k];
 		for (int i=0; i<k; i++) {
 			Virus v = answer.get(k-i-1);
 			array[i] = new VirusStats(virusCounts.get(v), v, getExtinctTime(v));
 		}
 		Arrays.sort(array, vsc);
 		return array;
 	}
 
 	public enum GraphType {
 		COUNT,
 		DEGREE,
 		NUM_COMPONENTS,
 		LARGEST_COMPONENT,
 		CONNECTIONS_PER_NODE,
 		HARD_SOFT_RATIO,
 		CLUSTERING_COEFF,
 		LARGEST_EDGE_DIST,
 		AVG_EDGE_DIST,
 		INFECTIOUS_COLLISIONS
 	}
 	
 	public static GraphType getGraphType() {
 		return graphType;
 	}
 	
 	public static void setGraphType(GraphType g) {
 		graphType = g;
 	}
 	
 	public static void drawGraph(Graphics g, GraphPanel graphPanel) {
 		switch (graphType) {
 		case COUNT:
 			drawCountGraph(g, graphPanel); break;
 		case DEGREE:
 			drawDegreeGraph(g, graphPanel); break;
 		case NUM_COMPONENTS:
 			drawNumComponentsGraph(g, graphPanel); break;
 		case LARGEST_COMPONENT:
 			drawLargestComponentGraph(g, graphPanel); break;
 		case CONNECTIONS_PER_NODE:
 			drawConnectionsPerNodeGraph(g, graphPanel); break;
 		case HARD_SOFT_RATIO:
 			drawHardSoftRatioGraph(g, graphPanel); break;
 		case CLUSTERING_COEFF:
 			drawClusteringCoeffGraph(g, graphPanel); break;
 		case LARGEST_EDGE_DIST:
 			drawLargestEdgeLengthGraph(g, graphPanel); break;
 		case AVG_EDGE_DIST:
 			drawAverageEdgeLengthGraph(g, graphPanel); break;
 		case INFECTIOUS_COLLISIONS:
 			drawCollisionsGraph(g, graphPanel); break;
 		}
 	}
 
 	private static void drawCountGraph(Graphics g, GraphPanel graphPanel) {
 		double scalarX = lastRetrievedVirusCount / (double) graphPanel.getWidth();
 		double scalarY = maxCount / (double) graphPanel.getHeight();
 		HashMap<Color, int[]> lastPoints = new HashMap<Color, int[]>();
 		for (Map.Entry<Long, HashMap<Virus, Integer>> e : virusCount.entrySet()) {
 			for (Map.Entry<Virus, Integer> ee : e.getValue().entrySet()) {
 				Color c = ee.getKey().getColor();
 				g.setColor(c);
 				int[] lastPos = lastPoints.get(c);
 				int x = (int) (e.getKey() / scalarX), y = (int) (graphPanel.getHeight() - (ee.getValue() / scalarY));
 				if (lastPos != null) {
 					g.drawLine(lastPos[0], lastPos[1], x, y);
 				}
 				lastPoints.put(c, new int[] {x, y});
 			}
 		}
 	}
 
 	private static void drawDegreeGraph(Graphics g, GraphPanel graphPanel) {
 		// Calculate connections per node
 		HashMap<Person, Integer> connectionsPerNode = new HashMap<Person, Integer>();
 		for (Person[] connection : hardConnections) {
 			for (Person p : connection) {
 				int count = 1;
 				if (connectionsPerNode.containsKey(p))
 					count = connectionsPerNode.get(p)+1;
 				connectionsPerNode.put(p, count);
 			}
 		}
 		// Map degree to # nodes with that degree
 		int maxCount = 0, maxDegree = 0;
 		TreeMap<Integer, Integer> connectionCounts = new TreeMap<Integer, Integer>();
 		for (Map.Entry<Person, Integer> e : connectionsPerNode.entrySet()) {
 			int count = 1;
 			if (connectionCounts.containsKey(e.getValue()))
 				count = connectionCounts.get(e.getValue())+1;
 			if (count > maxCount)
 				maxCount = count;
 			if (e.getValue() > maxDegree)
 				maxDegree = e.getValue();
 			connectionCounts.put(e.getValue(), count);
 		}
 		// Graph
 		g.setColor(Color.BLACK);
 		double interval = graphPanel.getWidth() / (maxDegree+1.0);
 		double scalarY = graphPanel.getHeight() / (maxCount+1.0);
 		for (int i = 0; i <= maxDegree; i++) {
 			int x = (int) (interval * i + interval * 0.125);
 			if (connectionCounts.containsKey(i)) {
 				int y = (int) (connectionCounts.get(i) * scalarY);
 				g.fillRect(x, graphPanel.getHeight() - y, (int) (interval * 0.75), y);
 			}
 		}
 	}
 
 	private static void drawNumComponentsGraph(Graphics g, GraphPanel graphPanel) {
 		drawSingleLineGraph(g, graphPanel, numComponents, maxNumComponents);
 	}
 
 	private static void drawLargestComponentGraph(Graphics g,
 			GraphPanel graphPanel) {
 		drawSingleLineGraph(g, graphPanel, maxComponentSizes, maxMaxComponentSize / (double) numPeople);
 	}
 
 	private static void drawConnectionsPerNodeGraph(Graphics g,
 			GraphPanel graphPanel) {
 		drawSingleLineGraph(g, graphPanel, numHardConnections, maxNumHardConnections);
 	}
 
 	private static void drawHardSoftRatioGraph(Graphics g, GraphPanel graphPanel) {
 		drawSingleLineGraph(g, graphPanel, hardSoftRatio, maxHardSoftRatio);
 	}
 
 	private static void drawClusteringCoeffGraph(Graphics g,
 			GraphPanel graphPanel) {
 		drawSingleLineGraph(g, graphPanel, clusteringCoeff, maxClusteringCoeff);
 	}
 
 	private static void drawLargestEdgeLengthGraph(Graphics g,
 			GraphPanel graphPanel) {
 		drawSingleLineGraph(g, graphPanel, largestPhysicalDistance, maxLargestPhysicalDistance);
 	}
 
 	private static void drawAverageEdgeLengthGraph(Graphics g,
 			GraphPanel graphPanel) {
 		drawSingleLineGraph(g, graphPanel, averagePhysicalDistance, maxAveragePhysicalDistance);
 	}
 	
 	private static void drawCollisionsGraph(Graphics g, GraphPanel graphPanel) {
 		drawSingleLineGraph(g, graphPanel, infectiousCollisions, maxInfectiousCollisions);
 	}
 
 	private static void drawSingleLineGraph(
 			Graphics g,
 			GraphPanel graphPanel,
 			TreeMap<Long, Double> map,
 			double max
 	) {
 		if (map.size() == 0)
 			return;
 		double scalarX = map.lastKey() / (double) graphPanel.getWidth();
 		double scalarY = max / (double) graphPanel.getHeight();
 		int[] lastPoint = new int[] {0, 0};
 		g.setColor(Color.BLACK);
 		for (Map.Entry<Long, Double> e : map.entrySet()) {
 			int x = (int) (e.getKey() / scalarX), y = (int) (graphPanel.getHeight() - (e.getValue() / scalarY));
 			g.drawLine(lastPoint[0], lastPoint[1], x, y);
 			lastPoint = new int[] {x, y};
 		}
 	}
 	
 	public static void drawHardConnections(Graphics g) {
 		g.setColor(Color.BLACK);
 		for (Person[] connection : hardConnections) {
 			Person a = connection[0], b = connection[1];
 			int x1 = (int) a.getX() + Person.RADIUS;
 			int y1 = (int) a.getY() + Person.RADIUS;
 			int x2 = (int) b.getX() + Person.RADIUS;
 			int y2 = (int) b.getY() + Person.RADIUS;
 			g.drawLine(x1, y1, x2, y2);
 		}
 	}
 	
 	public static void drawSoftConnections(Graphics g) {
 		g.setColor(Color.LIGHT_GRAY);
 		for (Person[] connection : softConnections) {
 			Person a = connection[0], b = connection[1];
 			int x1 = (int) a.getX() + Person.RADIUS;
 			int y1 = (int) a.getY() + Person.RADIUS;
 			int x2 = (int) b.getX() + Person.RADIUS;
 			int y2 = (int) b.getY() + Person.RADIUS;
 			g.drawLine(x1, y1, x2, y2);
 		}
 	}
 
 	public static void reset() {
 		totalCollisions.resetValue();
 		totalInfectiousCollisions.resetValue();
 		virusCount = new TreeMap<Long, HashMap<Virus, Integer>>();
 		extinction = new HashMap<Virus, Long>();
 		hardConnections = new HashSet<Person[]>();
 		softConnections = new HashSet<Person[]>();
 		connectionCounts = new HashMap<Person, Integer>();
 		components = new HashSet<LinkedList<Person>>();
 		numComponents = new TreeMap<Long, Double>();
 		maxComponentSizes = new TreeMap<Long, Double>();
 		numHardConnections = new TreeMap<Long, Double>();
 		hardSoftRatio = new TreeMap<Long, Double>();
 		clusteringCoeff = new TreeMap<Long, Double>();
 		largestPhysicalDistance = new TreeMap<Long, Double>();
 		averagePhysicalDistance = new TreeMap<Long, Double>();
 		infectiousCollisions = new TreeMap<Long, Double>();
 		stopWatch = new StopWatch();
 		vsc = new VirusStatsComparator();
 		lastRetrievedVirusCount = 0;
 		numViruses = 0;
 		numPeople = 0;
 		maxCount = 0;
 		maxNumComponents = 0;
 		maxMaxComponentSize = 0;
 		maxNumHardConnections = 0.0;
 		maxHardSoftRatio = 0.0;
 		maxClusteringCoeff = 0.0;
 		maxInfectiousCollisions = 0.0;
 		edgeProbDenom = 0.0;
 		//graphType = GraphType.COUNT;
 	}
 
 	private static class VirusStatsComparator implements Comparator<VirusStats> {
 		@Override
 		public int compare(VirusStats v1, VirusStats v2) {
 			Long extinct1 = extinction.get(v1.getVirus());
 			Long extinct2 = extinction.get(v2.getVirus());
 			if (extinct1 == null && extinct2 == null)
 				return 0;
 			else if (extinct1 == null)
 				return -1;
 			else if (extinct2 == null)
 				return 1;
 			return -extinct1.compareTo(extinct2);
 		}
 	}
 
 }
