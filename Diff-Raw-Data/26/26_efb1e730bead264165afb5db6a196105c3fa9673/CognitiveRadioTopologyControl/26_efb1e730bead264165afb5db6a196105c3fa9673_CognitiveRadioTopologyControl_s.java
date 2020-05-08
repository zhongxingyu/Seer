 package bobcat.applications;
 
 import bobcat.network.*;
 import bobcat.simulation.Draw;
 import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
 import edu.uci.ics.jung.algorithms.shortestpath.PrimMinimumSpanningTree;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.util.Pair;
 import ilog.concert.*;
 import ilog.cplex.IloCplex;
 import org.apache.commons.collections15.Transformer;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 
 import java.util.*;
 import java.io.*;
 import java.util.Collections;
 
 public class CognitiveRadioTopologyControl {
 
     static int MAX_CLIQUE_SIZE = 4;
 
     public static Boolean grows_clique(HashSet clique, Edge edge, 
 				       Network network, int channel) {
 	if (clique.size() > network.getEdgeCount()) { return (false); }
 	if (edge.channels[channel] <= 1.0) { return (false); }
 	for (Object o : clique) {
 	    Edge e = (Edge) o;
 
 	    if (e.id == edge.id) { return (false); }
 
 	    if ((!network.interferes[e.id][edge.id][channel]) 
 		&& (!network.interferes[edge.id][e.id][channel])) {
 		return (false);
 	    }
 	}
 	return (true);
     }
 
     public static Vertex find_gw(Network network) {
 	for (Object o: network.getVertices()) {
 	    Vertex v = (Vertex)o;
 	    if (v.isGateway) {
 		v.type = 0;
 		return(v);
 	    }
 	}
 	return(null);
     }
 
     public static int compute_coverage(List<Vertex> available, 
 				       Vector<Vertex> inRange) {
 	HashSet s1 = new HashSet(available);
 	HashSet s2 = new HashSet(inRange);
 	s1.retainAll(s2);
 	return(s1.size());
     }
 
     public static List<Vertex> in_range(Network network, Vertex v) {
 	// Build a coverage map
 	Vector<Vertex> inRange = new Vector<Vertex>();
 	double range = v.calculateRange();
 	for(Object p: network.getVertices()) {
 	    Vertex m = (Vertex)p;
 	    if (v.distanceTo(m) < range && network.findEdge(v,m) != null) {
 		inRange.add(m);
 	    }
 	}
 	return(inRange);
     }
 
     public static List<Vertex> find_aps(Network network, int count) {
 	Vector<Vertex> aps = new Vector<Vertex>();
 	HashMap coverage = new HashMap();
 	Vector<Vertex> inRange = new Vector<Vertex>();
 
 	// Build a coverage map
 	for(Object o: network.getVertices()) {
 	    Vertex n = (Vertex)o;
 	    coverage.put(n, in_range(network, n));
 	}
 
 	Vector<Vertex> available = new Vector<Vertex>(network.getVertices());
 	while (count > 0 && available.size() > 0) {
 	    int max_coverage = 0;
 	    Vertex choice = null;
 	    for(Object i: coverage.keySet()) {
 		Vertex v = (Vertex)i;
 		inRange = (Vector<Vertex>)coverage.get(v);
 		int c = compute_coverage(available, inRange);
 		if (c > max_coverage && ! v.isGateway) {
 		    max_coverage = c;
 		    choice = v;
 		}
 	    }
 	    choice.isAP = true;
 	    choice.type = 1;
 	    aps.add(choice);
 	    count -= 1;
 	    for(Object n: (Vector<Vertex>)coverage.get(choice)) {
 		Vertex v = (Vertex)n;
 		available.remove(v);
 	    }
 	}
 	return(aps);
     }
 
     public static HashMap enumerate_cliques(Network network) {
 	HashMap clique_list = new HashMap();
 
 	// For each edge
 	for (Object o : network.getEdges()) {
 	    Edge e = (Edge) o;
 	    HashMap channel_cliques = new HashMap();
 
 	    // For each channel
 	    for (int k = 0; k < network.numChannels * 3; k++) {
 		HashMap size_cliques = new HashMap();
 		HashSet cliques = new HashSet();
 		HashSet clique = new HashSet();
 		clique.clear();
 
 		// Clique of size 1, if it's using channel k
 		if (Math.abs(e.channels[k]) > 1.0) {
 
 		    clique.add(e);
 		    cliques.add(new HashSet(clique));
 		    size_cliques.put(1, new HashSet(cliques));
 		    cliques.clear();
 
 		    // Check all sizes up to max
 		    for (int i = 2; i < MAX_CLIQUE_SIZE; i++) {
 
 			// For each clique of the size i-1
 			for (Object p : (HashSet) size_cliques.get(i - 1)) {
 			    clique = (HashSet) p;
 
 			    // Check to see if adding an edge will grow them
 			    for (Object q : network.getEdges()) {
 				Edge edge = (Edge) q;
 
 				// If it grows the clique
 				if (grows_clique(clique, edge, network, k)) {
 
 				    HashSet cliqueCopy = new HashSet(clique);
 				    // clique.clear();
 				    cliqueCopy.add(edge);
 
 				    if (cliqueCopy.size() == i) {
 					// Store a copy under this size list
 					cliques.add(cliqueCopy);
 				    }
 				}
 			    }
 			}
 			size_cliques.put(i, new HashSet(cliques));
 			cliques.clear();
 		    }
 		    // store cliques
 		    channel_cliques.put(k, new HashMap(size_cliques));
 		    size_cliques.clear();
 		}
 	    }
 	    // roll out of the loop, storing results
 	    clique_list.put(e.id, new HashMap(channel_cliques));
 	    channel_cliques.clear();
 	}
 
 	return (clique_list);
     }
 
     public static void main(String[] args) {
 	NetworkGenerator networkGenerator;
 	Network network, nnet;
 	CognitiveRadioTopologyControlOptions options = new CognitiveRadioTopologyControlOptions();
 	CmdLineParser parser = new CmdLineParser(options);
 	Draw drawing = null;
 	ChannelSelection cs = null;
 	double rcsThpt;
 	PrimMST psp = null, dpsp = null;
 	Graph primTree = null, dprimTree = null;
 	Boolean find_seed = false;
 	Double DEMAND = 2e7;
 	int NUM_PATHS = 5;
 
 	parser.setUsageWidth(80);
 
 	try {
 	    parser.parseArgument(args);
 	} catch (CmdLineException e) {
 	    System.out.println("Failed to parse command line.");
 	    parser.printUsage(System.err);
 	    System.exit(1);
 	}
 
 	if (options.seed == 0) {
 	    find_seed = true;
 	    options.seed = System.nanoTime();
 	}
 
 	options.height = options.width;
 
 	do {
 	    if (find_seed) {
 		options.seed++;
 	    }
 
 	    Random gen = new Random(options.seed);
 	    networkGenerator = Network.getGenerator(options.relays, 
 						    options.subscribers, 
 						    options.width, 
 						    options.height, gen, 
 						    options.channels, 
 						    options.channelProb);
 
 	    network = networkGenerator.create();
 
 	    Transformer<Edge, Double> pTransformer = new Transformer<Edge, Double>() {
 
 		public Double transform(Edge e) {
 		    return (e.bottleNeckWeight() + e.length);
 		}
 	    };
 
 	    // Set all edge and vertex types to 0
 	    for (Object o : network.getVertices()) {
 		Vertex v = (Vertex) o;
 	    }
 	    for (Object o : network.getEdges()) {
 		Edge e = (Edge) o;
 		e.type = 0;
 	    }
 
 	    psp = new PrimMST(networkGenerator.networkFactory, pTransformer);
 	    primTree = psp.transform(network);
 
 	    // Remove all MST edges
 	    for(Object o: primTree.getEdges()) {
 		network.removeEdge((Edge)o);
 	    }
 
 	    // Find another MST
 	    dpsp = new PrimMST(networkGenerator.networkFactory, pTransformer);
 	    dprimTree = psp.transform(network);
 
 	    // Put all MST edges back
 	    for(Object o: primTree.getEdges()) {
 		Edge e = (Edge)o;
 		network.addEdge(e, primTree.getIncidentVertices(e));
 	    }
 	} while (network.getVertexCount() == 0 
 		 || network.getVertexCount() != primTree.getVertexCount() 
 		 || network.getVertexCount() != dprimTree.getVertexCount());
 
 	String fname = "CRTC-"+options.seed+"-"+options.width+"-"
 	    +options.relays+"-"+options.subscribers+"-"+options.channels;
 
 	if (options.output) {
 	    try {
 		String fn = fname+".out";
 		System.setOut(new PrintStream(new FileOutputStream(fn)));
 	    } catch (FileNotFoundException e) {
 		System.out.println("Failed to redirect output to file.");
 	    }
 	}
 	// Handle options that matter
 	if (options.verbose) {
 	    System.out.println("Random Seed: " + options.seed);
 	}
 
 	// MST
 	for (Object e : primTree.getEdges()) {
 	    ((Edge) e).type = 2;
 	}
 
 	// Second MST
 	for (Object e: dprimTree.getEdges()) {
 	    ((Edge)e).type=3;
 	}
 		
 	HashSet edges_to_save = new HashSet(primTree.getEdges());
 	edges_to_save.addAll(dprimTree.getEdges());
 
 	Vector toRemove = new Vector();
 	for (Object e : network.getEdges()) {
 	    if (! edges_to_save.contains(e)) { toRemove.addElement(e); }
 	}
 	for (Object e : toRemove) { network.removeEdge((Edge) e); }
 
 	// Renumber nodes
 	List<Vertex> nodes = new ArrayList<Vertex>(network.getVertices());
 	Collections.sort(nodes);
 	int nid = 0;
 	for(Object o : nodes) { Vertex v = (Vertex)o; v.id = nid; nid += 1; }
 
 	// Renumber edges
 	List<Edge> elist = new ArrayList<Edge>(network.getEdges());
 	Collections.sort(elist);
 	int eid = 0;
 	for (Object o : elist) { Edge e = (Edge) o; e.setId(eid); eid += 1; }
 
 	if (options.verbose) {
 	    System.out.println("Prim Tree: ");
 	    System.out.print(primTree.toString());
 	    System.out.println("");
 	}
 
 	if (options.verbose && options.backup) {
 	    System.out.println("2nd Prim Tree: ");
 	    System.out.print(dprimTree.toString());
 	    System.out.println("");
 	}
 
 	DijkstraShortestPath<Vertex, Edge> dspath = new DijkstraShortestPath(primTree);
 	DijkstraShortestPath<Vertex, Edge> dspath2 = new DijkstraShortestPath(dprimTree);
 
 	
 	Double[] overflow = new Double[network.getEdgeCount()];
 	Double[] demand1 = new Double[network.getEdgeCount()];
 	Double[] demand2 = new Double[network.getEdgeCount()];
 	HashMap paths = new HashMap();
 	HashMap spaths = new HashMap();
 
 	for(Object o : network.getEdges()) {
 	    Edge e = (Edge)o;
 	    overflow[e.id] = 0.0d;
 	    demand1[e.id] = 0.0d;
 	    demand2[e.id] = 0.0d;
 	}
 
 	// Find the gateway
 	Vertex gw = find_gw(network);
 
 	if (options.aps > 0){
 	    // for each ap, get the path (gw, ap_i)
 	    int i = 0;
 	    for (Object ap: find_aps(network, options.aps)) {
 		Vertex s = gw;
 		Vertex t = (Vertex)ap;
 		List<Edge> spath = dspath.getPath(s,t);
 		paths.put(i, spath);
 		if (options.verbose) {
 		    System.out.println("MST Path #"+i+": ("+s+","+t+") :"
 				       + spath);
 		}
 		for(Object o: spath) {
 		    Edge e = (Edge)o;
 		    demand1[e.id] += DEMAND;
 		}
 		if (options.backup) {
 		    List<Edge> spath2 = dspath2.getPath(s,t);
 		    spaths.put(i, spath2);
 		    if (options.verbose) {
 			System.out.println("MST2 Path #"+i+": ("+s+","+t+") :"
 					   + spath2);
 		    }
 		    for(Object o: spath2) {
 			Edge e = (Edge)o;
 			if (options.verbose) {
 			    System.out.println("Edge: "+e.id+" ["
 					       +network.getEdgeCount()+"]");
 			}
 			demand2[e.id] += DEMAND;
 		    }
 		}
 		i += 1;
 	    }
 	} else {
 	    // Select a random set of (s,t) and set the connection
 	    // requests along that path to 2e7
 	    for (int i = 0; i < NUM_PATHS; i++) {
 		Vertex s = network.getVertex(network.random.nextInt(network.getVertexCount()));
 		Vertex t = network.getVertex(network.random.nextInt(network.getVertexCount()));
 		if (s != t && s != null && t != null) {
 		    List<Edge> spath = dspath.getPath(gw,t);
 		    paths.put(i, spath);
 		    if (options.verbose) {
 			System.out.println("MST Path #"+i+": ("+s+","+t+") :"
 					   + spath);
 		    }
 		    for(Object o: spath) {
 			Edge e = (Edge)o;
 			demand1[e.id] += DEMAND;
 		    }
 		    if (options.backup) {
 			List<Edge> spath2 = dspath2.getPath(gw,t);
 			spaths.put(i, spath2);
 			if (options.verbose) {
 			    System.out.println("MST2 Path #"+i+": ("+s+"," 
 					       + t +") :"+spath2);
 			}
 			for(Object o: spath2) {
 			    Edge e = (Edge)o;
 			    if (options.verbose) {
 				System.out.println("Edge: "+e.id+" ["
 						   + network.getEdgeCount()
 						   +"]");
 			    }
 			    demand2[e.id] += DEMAND;
 			}
 		    }
 		} else {
 		    i--;
 		}
 	    }
 	}
 
 	if (options.verbose) {
 	    System.out.println(network.getEdgeCount() + " Edges");
 	    for (Object e : network.getEdges()) {
 		Edge z = (Edge)e;
 		System.out.println("\t" + z + " Bottleneck Capacity: " 
 				   + z.bottleNeckCapacity() 
 				   + " Demand (MST): "+demand1[z.id]
 				   + " Demand (MST2): "+demand2[z.id]);
 	    }
 	}
 
 	// Initialize a hash of cliques by channel, the value is a set
 	// so we don't get duplicates indexed by edge, then channel,
 	// then a list of cliques
 	HashMap clique_list = enumerate_cliques(network);
 	
 	if (options.quick) {
 	    findSln(options, network, DEMAND, NUM_PATHS, demand1,
 		    demand2, paths, spaths, clique_list);
 	} else {
 	    // Build ILP
 	    try {
 		Double cplexTotal = Double.MAX_VALUE;
 		IloCplex cplex = new IloCplex();
 		Boolean solved = false;
 		
 		// Variable Definitions for ILP
 		// c
 		IloIntVar[] c = new IloIntVar[network.numChannels * 3];
 		for (int i = 0; i < network.numChannels * 3; i++) {
 		    c[i] = cplex.intVar(0, 1, "c(" + i + ")");
 		}
 		
 		// x
 		IloIntVar[][][] x = new IloIntVar[network.getEdgeCount()][network.numChannels * 3][MAX_CLIQUE_SIZE];
 		for (Object o : network.getEdges()) {
 		    Edge e = (Edge) o;
 		    for (int k = 0; k < network.numChannels * 3; k++) {
 			for (int tc = 1; tc < MAX_CLIQUE_SIZE; tc++) {
 			    x[e.id][k][tc] = cplex.intVar(0, 1, "x(" + e.id 
 							  + ")(" + k + ")(" 
 							  + tc + ")");
 			}
 		    }
 		}
 		
 		// D
 		Double[][][] D = new Double[network.getEdgeCount()][network.numChannels * 3][MAX_CLIQUE_SIZE];
 		for (Object o : network.getEdges()) {
 		    Edge e = (Edge) o;
 		    for (int k = 0; k < network.numChannels * 3; k++) {
 			for (int size = 1; size < MAX_CLIQUE_SIZE; size++) {
 			    if (e.channels[k] >= 0) {
 				D[e.id][k][size] = e.channels[k] / size;	
 			    } else {
 				D[e.id][k][size] = 0.0d;
 			    }
 			}
 		    }
 		}
 
 		// o's
 		IloNumVar[] o = new IloNumVar[network.getEdgeCount()];
 		for (Object oe: network.getEdges()) {
 		    Edge e = (Edge)oe;
 		    o[e.id] = cplex.numVar(0.0, Double.MAX_VALUE, 
 					   "o("+e.id+")");
 		}
 	    
 		// O 
 		IloNumVar O = cplex.numVar(0.0, Double.MAX_VALUE, "O");
 	    
 		// d
 		IloNumVar[] d1 = new IloNumVar[NUM_PATHS];
 		IloNumVar[] d2 = new IloNumVar[NUM_PATHS];
 		for (int i = 0; i < NUM_PATHS; i++) {
 		    d1[i] = cplex.numVar(0.0, Double.MAX_VALUE, "d1("+i+")");
 		    d2[i] = cplex.numVar(0.0, Double.MAX_VALUE, "d2("+i+")");
 		}
 
 		// y
 		IloIntVar[][] y = new IloIntVar[network.getEdgeCount()][network.numChannels * 3];
 		for (Object of : network.getEdges()) {
 		    Edge e = (Edge) of;
 		    for (int k = 0; k < network.numChannels * 3; k++) {
 			y[e.id][k] = cplex.intVar(0, 1, "y(" + e.id + ")(" 
 						  + k + ")");
 		    }
 		}
 
 		// Objective function in Equation 3 - Minimize Overall
 		// Channel Costs
 
 		IloLinearNumExpr cost = cplex.linearNumExpr();
 
 		// Costs for ubiquity hardware:
 		// 900MHz - $170, 2.4GHz - $85, 5GHz - $290
 		// 900 MHz = 2.0, 2.4GHz = 1.0, 5GHz - 3.0
 		// Initialize Channel Costs: Channel costs are all the
 		// same 1.0 for now
 		double[] channel_costs = new double[network.numChannels * 3];
 		for (int k = 0; k < network.numChannels * 3; k++) {
 		    if (k % 3 == 0) {
 			// 700/900
 			if (options.variable_cost) {
 			    channel_costs[k] = 2.0;
 			} else {
 			    channel_costs[k] = 1.0;
 			}
 		    }
 		    if (k % 3 == 1) {
 			// 2.4/3.0
 			channel_costs[k] = 1.0;
 		    }
 		    if (k % 3 == 2) {
 			// 5.0
 			if (options.variable_cost) {
 			    channel_costs[k] = 3.0;
 			} else {
 			    channel_costs[k] = 1.0;
 			}
 		    }
 		}
 
 		IloObjective objective;
 		// Objective: Minimize channel costs 
 		// Channel usage array (Number of edges * number of
 		// channels per edge)
 		for (int k = 0; k < network.numChannels * 3; k++) {
 		    cost.addTerm(channel_costs[k], c[k]);
 		}
 		objective = cplex.minimize(cost);
 		cplex.add(objective);
 		if (options.verbose) {
 		    System.out.println("Objective : " + objective);
 		    
 		    // Constraint 1:
 		    System.out.println("Constraint 1:");
 		}
 		
 		for (Object og : network.getEdges()) {
 		    Edge e = (Edge) og;
 		    for (int k = 0; k < network.numChannels * 3; k++) {
 			// Sum acros clique sizes
 			IloNumExpr irj = cplex.numExpr();
 			irj = x[e.id][k][1];
 			for (int tc = 2; tc < MAX_CLIQUE_SIZE; tc++) {
 			    irj = cplex.sum(x[e.id][k][tc], irj);
 			}
 			if (options.verbose) {
 			    System.out.println("\t"+y[e.id][k] + " = " + irj);
 			    System.out.println("\t"+y[e.id][k] + " <= " + c[k]);
 			}
 			cplex.addEq(y[e.id][k], irj);
 			cplex.addLe(y[e.id][k], c[k]);
 		    }
 		}
 
 		if (options.verbose) {
 		    // Constraint 2: 
 		    System.out.println("Constraint 2:");
 		}
 		for (Object oh : network.getEdges()) {
 		    Edge e = (Edge) oh;
 		    for (int k = 0; k < network.numChannels * 3; k++) {
 			// Sum acros clique sizes
 			IloNumExpr irj = cplex.numExpr();
 			irj = x[e.id][k][1];
 			for (int tc = 2; tc < MAX_CLIQUE_SIZE; tc++) {
 			    irj = cplex.sum(x[e.id][k][tc], irj);
 			}
 			if (options.verbose) {
 			    System.out.println("\t"+irj+" <= " + 1);
 			}
 			cplex.addLe(irj, 1);
 		    }
 		}
 
 		if (options.verbose) {
 		    // Constraint 3:
 		    System.out.println("Constraint 3:");
 		}
 		// Make interim data structure to make this easier
 		HashMap cl = new HashMap();
 		for (Object oi : network.getEdges()) {
 		    Edge e = (Edge) oi;
 		    HashMap edge_cliques = (HashMap) clique_list.get(e.id);
 		    for (int k = 0; k < network.numChannels * 3; k++) {
 			HashMap cliques_of_size = (HashMap)edge_cliques.get(k);
 			if (cliques_of_size != null) {
 			    for (int c1 = 1; c1 < MAX_CLIQUE_SIZE; c1++) {
 				HashSet cliques = (HashSet)cliques_of_size.get(c1);
 				HashSet cls = (HashSet)cl.get(k);
 				if (cls == null) {
 				    cl.put(k, new HashSet(cliques));
 				} else {
 				    cls.addAll(cliques);
 				    cl.put(k, cls);
 				}
 			    }
 			}
 		    }
 		}
 
 		for (int k = 0; k < network.numChannels * 3; k++) {
 		    HashSet cliques = (HashSet)cl.get(k);
 		    if (cliques != null) {
 			for (Object oj : cliques) {
 			    HashSet clique = (HashSet)oj;
 			    IloNumExpr cs2 = cplex.numExpr();
 			    Object[] cl2 = clique.toArray();
 			    Edge xx = (Edge)cl2[0];
 			    cs2 = y[xx.id][k];
 			    for (int i = 1; i < clique.size(); i++) {
 				Edge xxx = (Edge)cl2[i];
 				cs2 = cplex.sum(y[xxx.id][k], cs2);
 			    }
 			    IloNumExpr c8 = cplex.diff(clique.size(), cs2);
 			    for (Object p : clique) {
 				Edge e = (Edge)p;
 				for (int i = 1; i < clique.size() - 1; i++) {
 				    if (options.verbose) {
 					System.out.println("\t"+x[e.id][k][i] 
 							   + " <= " + c8);
 				    }
 				    cplex.addLe(x[e.id][k][i], c8);
 				}
 			    }
 			}
 		    }
 		}
 
 		if (options.verbose) {
 		    // Constraint 4: Total capacity constraint
 		    System.out.println("Constraint 4:");
 		}
 		for (Object zz : network.getEdges()) {
 		    Edge e = (Edge) zz;
 		    IloNumExpr lhs = cplex.numExpr();
 		    for (int k = 0; k < network.numChannels * 3; k++) {
 			for (int tc = 1; tc < MAX_CLIQUE_SIZE; tc++) {
 			    lhs = cplex.sum(cplex.prod(D[e.id][k][tc], 
 						       x[e.id][k][tc]),
 					    lhs);
 
 			}
 		    }
 
 		    if (options.backup) {
 			IloNumExpr rhs = cplex.numExpr();
 			for (int i = 0; i < NUM_PATHS; i++) {
 			    List<Edge> p = (List<Edge>)paths.get(i);
 			    List<Edge>sp = (List<Edge>)spaths.get(i);
 			    if (p.contains(e)) {
 				rhs = cplex.sum(rhs, d1[i]);
 			    }
 			    if (sp.contains(e)) {
 				rhs = cplex.sum(rhs, d2[i]);
 			    }
 			}
 			cplex.addGe(lhs, rhs);
 			// Calculate demand
 			if (options.verbose) {
 			    System.out.println("\t"+lhs + " >= "+ rhs);
 			}
 		    } else {
 			Double rhs = demand1[e.id] + demand2[e.id];
 			cplex.addGe(lhs, rhs);
 			// Calculate demand
 			if (options.verbose) {
 			    System.out.println("\t"+lhs + " >= "+ rhs);
 			}
 		    }
 
 		}
 
 		if (options.backup) {
 		    if (options.verbose) {
 			System.out.println("Constraint 5:");
 		    }
 		    for(int i = 0; i < NUM_PATHS; i++) {
 			cplex.addEq(cplex.sum(d1[i], d2[i]), DEMAND);
 		    }
 		}
 
 		// constraint on O/o's
 		for (Object ok: network.getEdges()) {
 		    Edge e = (Edge)ok;
 		    cplex.addLe(o[e.id], O);
 		}
 
 		if (options.verbose) {
 		    // Write the model out to validate
 		    cplex.exportModel(fname+".lp");
 		} else {
 		    cplex.setOut(null);
 		}
 
 		solved = cplex.solve();
 		if (solved) {
 		    cplexTotal = cplex.getObjValue();
 		    List<Edge> elist2 = new ArrayList<Edge>(network.getEdges());
 		    Collections.sort(elist2);
 		    for (Object ol : elist2) {
 			Edge e = (Edge) ol;
 			double sum = 0.0;
 			for (int k = 0; k < network.numChannels * 3; k++) {
 			    for (int tc = 1; tc < MAX_CLIQUE_SIZE; tc++) {
 				double value = cplex.getValue(x[e.id][k][tc]);
 				if (value > 0) {
 				    sum += D[e.id][k][tc];
 				    System.out.println("\tEdge: "+e.id
 						       +" Channel: "+k
 						       +" Throughput: "
 						       +D[e.id][k][tc]);
 				}
 			    }
 			}
 		    }
 
 		    if (options.verbose) {
 			for (Object om : network.getEdges()) {
 			    Edge e = (Edge)om;
 			    for (int k = 0; k < network.numChannels * 3; k++) {
 				System.out.println("y("+e.id+")("+k+") = " 
 						   + cplex.getValue(y[e.id][k]));
 			    }
 			}
 
 			for (int i = 0; i < network.numChannels * 3; i++) {
 			    System.out.println("c("+i+") = "+cplex.getValue(c[i]));
 			}
 		    }
 
 		    if (options.verbose) {
 			System.out.println("Solution status = " 
 					   + cplex.getStatus());
 		    }
 
 		
 		    // Show graph
 		    if (options.display) {
 			drawing = new Draw(network, 1024, 768,
 					   "Routing and Channel Selection Application");
 			drawing.draw();
 		    }
 
 		    System.out.println("Variation, Seed, Width, Height, Nodes, Users, Channels, APs, Cost");
 		    String variation = null;
 		    if (options.backup) {
 			variation = "mst2";
 			if (options.variable_cost) {
 			    variation += "-vc";
 			}
 		    } else {
 			variation = "mst";
 			if (options.variable_cost) {
 			    variation += "-vc";
 			}
 		    }
 
 		    System.out.println(variation + ", " +options.seed + ", " 
 				       + options.width + ", " + options.height 
 				       + ", " + options.relays + ", " 
 				       + options.subscribers + ", " 
 				       + options.channels + ", "+ options.aps 
 				       + ", " + cplexTotal);
 
 		} else {
 		    System.out.println("Couldn't solve problem!");
 		    solved = true;
 		}
 		cplex.end();
 	    } catch (IloException e) {
 		System.err.println("Concert exception '" + e + "' caught.");
 	    }
 	}
     }
 
     static Double find_O(Double[] o) {
 	Double max = 0.0d;
 	for(int i = 0; i < o.length; i++) {
 	    if (o[i] > max) {
 		max = o[i];
 	    }
 	}
 	return(max);
     }
 
     static List<Edge> find_E(Double[] o, Double O, Collection edges) {
 	Vector<Edge> E = new Vector<Edge>();
 	for(int i = 0; i < o.length; i++) {
	    //	    System.out.println(o[i] + " <?> " + O);
 	    if (O.compareTo(o[i]) == 0) {
 		for(Object oe: edges) {
 		    Edge e = (Edge)oe;
 		    if (e.id == i) {
 			E.add(e);
 		    }
 		}
 	    }
 	}
 	return(E);
     }
 
     static void findSln(CognitiveRadioTopologyControlOptions options, 
 			Network network, Double DEMAND, int NUM_PATHS,
 			Double[] demand1, Double[]  demand2,
 			HashMap paths, HashMap spaths,
 			HashMap clique_list) {
 	Double O = 0.001, O1 = 0.0d;
 	Double[] o, o1;
 	List<Edge> E, E1;
 	Integer[] A = new Integer[network.numChannels*3];
 	Integer[][] y = new Integer[network.getEdgeCount()][network.numChannels*3];
 
 	for(int i = 0; i < A.length; i++) {
 	    A[i] = 0;
 	}
 
 	// Set all y_ik = 0
 	for(int i = 0; i < network.getEdgeCount(); i++) {
 	    for(int j = 0; j < network.numChannels*3; j++) {
 		y[i][j] = 0;
 	    }
 	}
 	
 	// loop until O == 0
 	int outer_counter = 0;
 	outerloop:
 	while(O > 0) {
 	    o = makeILP(options, network, DEMAND, NUM_PATHS,
 			demand1, demand2, paths, spaths, clique_list, y);
 	    O = find_O(o);
 	    E = find_E(o, O, network.getEdges());
 	    Boolean foundimprovement = false;
 
 	    innerloop:
 	    for(Object oa: E) {
 		System.out.print("A: ");
 		for(int i = 0; i < network.numChannels*3; i++) {
 		    System.out.print(A[i]+" ");
 		}
 		System.out.print("\n");
 		
 		System.out.println("Checking edge "+oa);
 		Edge e = (Edge)oa;
 		// check each existing channel
 		//		for(int i = 0; i < A.length; i++) {
 		for(int i = 0; i < 1; i++) {
 		    if (A[i] == 1) {
 			System.out.println("Checking channel "+i);
 			y[e.id][i] = 1;
 			System.out.println("y:");
 			for(int ii = 0; ii < network.getEdgeCount(); ii++) {
 			    System.out.print(ii+": ");
 			    for(int j = 0; j < network.numChannels*3; j++) {
 				System.out.print(y[ii][j]+" ");
 			    }
 			    System.out.print("\n");
 			}
 			o1 = makeILP(options, network, DEMAND, NUM_PATHS,
 				     demand1, demand2, paths, spaths, 
 				     clique_list, y);
 			O1 = find_O(o1);
 			E1 = find_E(o1, O1, network.getEdges());
 
 			System.out.println("E: "+E.size());
 			System.out.println("E1: "+E1.size());
 			System.out.println("O: "+o[e.id]);
 			System.out.println("O1: "+o1[e.id]);
 
 			if (E1.size()<E.size() 
 			    || (E1.size()==E.size() && O1 < O)) {
 			    System.out.println("Yep, going back to the start.");
 			    foundimprovement = true;
 			    break innerloop;
 			} else {
 			    System.out.println("Nope, checking next channel.");
 			    y[e.id][i] = 0;
 			}
 		    }
 		}
 
 		System.out.println("No existing channels improve things.");
 		if (! foundimprovement) {
 		    for(int i = 0; i < A.length; i++) {
 			if(A[i] == 0) {
 			    System.out.println("Turning on channel: " + i);
 			    A[i] = 1;
 			    break innerloop;
 			}
 		    }
 		}
 	    }
 	}
     }
 
     static Double[] makeILP(CognitiveRadioTopologyControlOptions options, 
 			    Network network, Double DEMAND, int NUM_PATHS,
 			    Double[] demand1, Double[]  demand2,
 			    HashMap paths, HashMap spaths,
 			    HashMap clique_list, Integer[][] ys) {
 	Double[] os = new Double[network.getEdgeCount()];
 	// Build ILP
 	try {
 	    Double cplexTotal = Double.MAX_VALUE;
 	    IloCplex cplex = new IloCplex();
 
 	    // Variable Definitions for ILP
 	    // c
 	    IloIntVar[] c = new IloIntVar[network.numChannels * 3];
 	    for (int i = 0; i < network.numChannels * 3; i++) {
 		c[i] = cplex.intVar(0, 1, "c(" + i + ")");
 	    }
 
 	    // x
 	    IloIntVar[][][] x = new IloIntVar[network.getEdgeCount()][network.numChannels * 3][MAX_CLIQUE_SIZE];
 	    for (Object o : network.getEdges()) {
 		Edge e = (Edge) o;
 		for (int k = 0; k < network.numChannels * 3; k++) {
 		    for (int tc = 1; tc < MAX_CLIQUE_SIZE; tc++) {
 			x[e.id][k][tc] = cplex.intVar(0, 1, "x(" + e.id 
 						      + ")(" + k + ")(" 
 						      + tc + ")");
 		    }
 		}
 	    }
 
 	    // D
 	    Double[][][] D = new Double[network.getEdgeCount()][network.numChannels * 3][MAX_CLIQUE_SIZE];
 	    for (Object o : network.getEdges()) {
 		Edge e = (Edge) o;
 		for (int k = 0; k < network.numChannels * 3; k++) {
 		    for (int size = 1; size < MAX_CLIQUE_SIZE; size++) {
 			if (e.channels[k] >= 0) {
 			    D[e.id][k][size] = e.channels[k] / size;	
 			} else {
 			    D[e.id][k][size] = 0.0d;
 			}
 		    }
 		}
 	    }
 
 	    // o's
 	    IloNumVar[] o = new IloNumVar[network.getEdgeCount()];
 	    for (Object oe: network.getEdges()) {
 		Edge e = (Edge)oe;
 		o[e.id] = cplex.numVar(0.0, Double.MAX_VALUE, 
 				       "o("+e.id+")");
 	    }
 	    
	    // O 
	    IloNumVar O = cplex.numVar(0.0, Double.MAX_VALUE, "O");
	    
 	    // d
 	    IloNumVar[] d1 = new IloNumVar[NUM_PATHS];
 	    IloNumVar[] d2 = new IloNumVar[NUM_PATHS];
 	    for (int i = 0; i < NUM_PATHS; i++) {
 		d1[i] = cplex.numVar(0.0, Double.MAX_VALUE, "d1("+i+")");
 		d2[i] = cplex.numVar(0.0, Double.MAX_VALUE, "d2("+i+")");
 	    }
 
 	    // y
 	    IloIntVar[][] y = new IloIntVar[network.getEdgeCount()][network.numChannels * 3];
 	    for (Object of : network.getEdges()) {
 		Edge e = (Edge) of;
 		for (int k = 0; k < network.numChannels * 3; k++) {
 		    y[e.id][k] = cplex.intVar(0, 1, "y(" + e.id + ")(" 
 					      + k + ")");
 		}
 	    }
 
 	    // Objective function in Equation 3 - Minimize Overall
 	    // Channel Costs
 
	    IloLinearNumExpr cost = cplex.linearNumExpr();
 
 	    // Costs for ubiquity hardware:
 	    // 900MHz - $170, 2.4GHz - $85, 5GHz - $290
 	    // 900 MHz = 2.0, 2.4GHz = 1.0, 5GHz - 3.0
 	    // Initialize Channel Costs: Channel costs are all the
 	    // same 1.0 for now
 	    double[] channel_costs = new double[network.numChannels * 3];
 	    for (int k = 0; k < network.numChannels * 3; k++) {
 		if (k % 3 == 0) {
 		    // 700/900
 		    if (options.variable_cost) {
 			channel_costs[k] = 2.0;
 		    } else {
 			channel_costs[k] = 1.0;
 		    }
 		}
 		if (k % 3 == 1) {
 		    // 2.4/3.0
 		    channel_costs[k] = 1.0;
 		}
 		if (k % 3 == 2) {
 		    // 5.0
 		    if (options.variable_cost) {
 			channel_costs[k] = 3.0;
 		    } else {
 			channel_costs[k] = 1.0;
 		    }
 		}
 	    }
 
 	    IloObjective objective = cplex.minimize(O);
 
 	    // Objective
 	    cplex.add(objective);
 
 	    // Constraint 1:
 	    for (Object og : network.getEdges()) {
 		Edge e = (Edge) og;
 		for (int k = 0; k < network.numChannels * 3; k++) {
 		    // Sum acros clique sizes
 		    IloNumExpr irj = cplex.numExpr();
 		    irj = x[e.id][k][1];
 		    for (int tc = 2; tc < MAX_CLIQUE_SIZE; tc++) {
 			irj = cplex.sum(x[e.id][k][tc], irj);
 		    }
 		    cplex.addEq(y[e.id][k], irj);
 		    cplex.addLe(y[e.id][k], c[k]);
 		}
 	    }
 
 	    // Constraint 2: 
 	    for (Object oh : network.getEdges()) {
 		Edge e = (Edge) oh;
 		for (int k = 0; k < network.numChannels * 3; k++) {
 		    // Sum acros clique sizes
 		    IloNumExpr irj = cplex.numExpr();
 		    irj = x[e.id][k][1];
 		    for (int tc = 2; tc < MAX_CLIQUE_SIZE; tc++) {
 			irj = cplex.sum(x[e.id][k][tc], irj);
 		    }
 		    cplex.addLe(irj, 1);
 		}
 	    }
 
 	    // Constraint 3:
 	    HashMap cl = new HashMap();
 	    for (Object oi : network.getEdges()) {
 		Edge e = (Edge) oi;
 		HashMap edge_cliques = (HashMap) clique_list.get(e.id);
 		for (int k = 0; k < network.numChannels * 3; k++) {
 		    HashMap cliques_of_size = (HashMap)edge_cliques.get(k);
 		    if (cliques_of_size != null) {
 			for (int c1 = 1; c1 < MAX_CLIQUE_SIZE; c1++) {
 			    HashSet cliques = (HashSet)cliques_of_size.get(c1);
 			    HashSet cls = (HashSet)cl.get(k);
 			    if (cls == null) {
 				cl.put(k, new HashSet(cliques));
 			    } else {
 				cls.addAll(cliques);
 				cl.put(k, cls);
 			    }
 			}
 		    }
 		}
 	    }
 
 	    for (int k = 0; k < network.numChannels * 3; k++) {
 		HashSet cliques = (HashSet)cl.get(k);
 		if (cliques != null) {
 		    for (Object oj : cliques) {
 			HashSet clique = (HashSet)oj;
 			IloNumExpr cs2 = cplex.numExpr();
 			Object[] cl2 = clique.toArray();
 			Edge xx = (Edge)cl2[0];
 			cs2 = y[xx.id][k];
 			for (int i = 1; i < clique.size(); i++) {
 			    Edge xxx = (Edge)cl2[i];
 			    cs2 = cplex.sum(y[xxx.id][k], cs2);
 			}
 			IloNumExpr c8 = cplex.diff(clique.size(), cs2);
 			for (Object p : clique) {
 			    Edge e = (Edge)p;
 			    for (int i = 1; i < clique.size() - 1; i++) {
 				cplex.addLe(x[e.id][k][i], c8);
 			    }
 			}
 		    }
 		}
 	    }
 
 	    // Constraint 4
 	    for (Object zz : network.getEdges()) {
 		Edge e = (Edge) zz;
 		IloLinearNumExpr lhs = cplex.linearNumExpr();
 		for (int k = 0; k < network.numChannels * 3; k++) {
 		    for (int tc = 1; tc < MAX_CLIQUE_SIZE; tc++) {
 			lhs.addTerm(D[e.id][k][tc], x[e.id][k][tc]);
 		    }
 		}
 		lhs.addTerm(1.0, o[e.id]);
 
 		if (options.backup) {
 		    IloNumExpr rhs = cplex.numExpr();
 		    for (int i = 0; i < NUM_PATHS; i++) {
 			List<Edge> p = (List<Edge>)paths.get(i);
 			List<Edge>sp = (List<Edge>)spaths.get(i);
 			if (p.contains(e)) {
 			    rhs = cplex.sum(rhs, d1[i]);
 			}
 			if (sp.contains(e)) {
 			    rhs = cplex.sum(rhs, d2[i]);
 			}
 		    }
 		    cplex.addGe(lhs, rhs);
 		    // Calculate demand
 		} else {
 		    Double rhs = demand1[e.id] + demand2[e.id];
 		    cplex.addGe(lhs, rhs);
 		    // Calculate demand
 		}
 
 	    }
 
 	    if (options.backup) {
 		for(int i = 0; i < NUM_PATHS; i++) {
 		    cplex.addEq(cplex.sum(d1[i], d2[i]), DEMAND);
 		}
 	    }
 
 	    // constraint on O/o's
 	    for (Object ok: network.getEdges()) {
 		Edge e = (Edge)ok;
 		cplex.addLe(o[e.id], O);
 	    }
 
 	    // Pin y's
 	    for(int i = 0; i < network.getEdgeCount(); i++) {
 		for (int j = 0; j < network.numChannels*3; j++) {
 		    cplex.addEq(y[i][j], ys[i][j]);
 		}
 	    }
 
 	    // Solve
 	    cplex.setOut(null);
 
 	    if (cplex.solve()) {
 		cplexTotal = cplex.getObjValue();
 		List<Edge> elist2 = new ArrayList<Edge>(network.getEdges());
 		Collections.sort(elist2);
 		for (Object ol : elist2) {
 		    Edge e = (Edge) ol;
		    double sum = 0.0;
 		    os[e.id] = cplex.getValue(o[e.id]);
 		    for (int k = 0; k < network.numChannels * 3; k++) {
 			for (int tc = 1; tc < MAX_CLIQUE_SIZE; tc++) {
 			    double value = cplex.getValue(x[e.id][k][tc]);
			    sum += D[e.id][k][tc];
 			    System.out.println("Edge: "+e.id
 					       +" Channel: "+k
 					       +" Clique Size: "+tc
					       +" Throughput: "
					       +D[e.id][k][tc]
 					       +" x "+value
 					       +" Overflow: "+os[e.id]);
 			}
 		    }
 		}
 	    } else {
 		System.out.println("Couldn't solve problem!");
 	    }
 	    cplex.end();
 	} catch (IloException e) {
 	    System.err.println("Concert exception '" + e + "' caught.");
 	}
 	return(os);
     }
 }
 
