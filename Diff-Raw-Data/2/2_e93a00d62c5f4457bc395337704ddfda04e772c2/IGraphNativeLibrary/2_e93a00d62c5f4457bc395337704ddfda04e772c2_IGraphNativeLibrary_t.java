 package genlab.igraph.natjna;
 
 import genlab.core.commons.NotImplementedException;
 import genlab.core.commons.ProgramException;
 import genlab.core.commons.WrongParametersException;
 import genlab.core.usermachineinteraction.GLLogger;
 import genlab.core.usermachineinteraction.ListOfMessages;
 import genlab.core.usermachineinteraction.ListsOfMessages;
 import genlab.igraph.natjna.IGraphRawLibrary.IGraphBarabasiAlgorithm;
 
 import java.util.Iterator;
 
 import com.sun.jna.Memory;
 import com.sun.jna.Native;
 import com.sun.jna.NativeLong;
 import com.sun.jna.Pointer;
 import com.sun.jna.ptr.DoubleByReference;
 import com.sun.jna.ptr.IntByReference;
 import com.sun.jna.ptr.PointerByReference;
 
 /**
  * An implementation of IGraphLibrary using JNA for a direct connection.
  * 
  * @author Samuel Thiriot
  *
  */
 public class IGraphNativeLibrary {
 
 	/**
 	 * THE native library to use.
 	 */
 	//private final IGraphIGraphRawLibraryrary IGraphRawLibrary;
 	
 	public static String versionString = null; 
 	
 	public final static String GRAPH_KEY_DIAMETER = "diameter";
 	public final static String GRAPH_KEY_AVERAGE_PATH_LENGTH = "average path length";
 	
 	public final static String GRAPH_KEY_CONNECTED = "connected";
 	public final static String GRAPH_KEY_COMPONENTS_COUNT = "components.count";
 	public final static String GRAPH_KEY_COMPONENTS_GIANT_SIZE = "components.giant.size";
 	public final static String GRAPH_KEY_CLUSTERING_GLOBAL = "stats.clustering.global";
 	public final static String GRAPH_KEY_CLUSTERING_GLOBAL_AVG = "stats.clustering.global.avg";
 
 	/**
 	 * Stores a double array containing for each vertex the id of its cluster.
 	 */
 	public final static String GRAPH_KEY_COMPONENTS_MEMBERSHIP = "components.giant.membership";
 
 	/**
 	 * Stores a double array containing for each component id its size.
 	 */
 	public final static String GRAPH_KEY_COMPONENTS_CLUSTER_SIZES = "components.giant.sizes";
 
 	
 	public final boolean paramUseCache = true;
 	
 	protected ListOfMessages listOfMessages = ListsOfMessages.getGenlabMessages();
 	
 	// TODO errors !
 	//private final static int IGRAPH_ERROR_IGRAPH_ENOMEM = ;
 	
 
 	
 	/**
 	 * Creates a novel accessor to a raw (native) igraph library.
 	 */
 	public IGraphNativeLibrary() {
 
 		try {
 			
 		if (versionString == null)
 			retrieveVersion();
 		} catch (UnsatisfiedLinkError e) {
 			final String errorMsg = "the igraph library was not correctingly started. Probably the native igraph library can not be found, or it is not compliant with the current execution environment (platform, OS...)"; 
 			GLLogger.errorTech(errorMsg, getClass(), e); 
 			e.printStackTrace();
 			throw new ProgramException(errorMsg, e);
 		}
 		// define a seed for the random generator
 		//Pointer p = IGraphRawLibrary.igraph_rng_default();
 		//IGraphRawLibrary.igraph_rng_seed(p, new NativeLong((long)Math.round(Math.random()*65000)));
 		
 		GLLogger.traceTech("loaded the native igraph library version: "+versionString, getClass());
 	}
 
 	
 
 	/**
 	 * Retrieves the version and stores it into attributes. 
 	 * Is supposed to be called only once.
 	 */
 	private void retrieveVersion() {
 
 		PointerByReference str = new PointerByReference();
 		IntByReference major = new IntByReference();
 		IntByReference minor = new IntByReference();
 		IntByReference subminor = new IntByReference();
 		
 		IGraphRawLibrary.igraph_version(str, major, minor, subminor);
 				
 		Pointer p = str.getValue();
 		versionString = p.getString(0);
 
 	}
 
 	/**
 	 * Give this library a place to export information to the user.
 	 * If null, this will fall back to the main one.
 	 * @param messages
 	 */
 	public void setListOfMessages(ListOfMessages messages) {
 		if (messages == null)
 			listOfMessages = ListsOfMessages.getGenlabMessages();
 		else
 			listOfMessages = messages;
 	}
 	
 	/**
 	 * Returns the igraph version string (efficient, 
 	 * because it is cached locally)
 	 * @return
 	 */
 	public String getVersionString() {
 		return versionString;
 	}
 	
 	protected InternalGraphStruct createEmptyGraph() {
 		return new InternalGraphStruct();
 	}
 	
 	public InternalGraphStruct createEmptyGraph(int nodesPlanned, int edgesPlanned, boolean directed) {
 		return new InternalGraphStruct(directed, nodesPlanned, edgesPlanned);
 	}
 	
 	public void clearGraphMemory(IGraphGraph g) {
 		
 		
 		if (g == null)
 			return;
 		
 		IGraphRawLibrary.igraph_destroy(g.getPointer());
 	}
 	
 	/**
 	 * Check the integer code returned by igraph.
 	 * @param code
 	 */
 	protected void checkIGraphResult(int code) {
 		
 		if (code != 0)
 			// TODO !
 			throw new ProgramException("error during the computation");
 		
 	}
 	
 	public IGraphGraph generateErdosRenyiGNP(int size, double proba, boolean directed, boolean allowLoops) {
 
 		final InternalGraphStruct g = createEmptyGraph();
 				
 		//GLLogger.debugTech("calling igraph", getClass());
 		final long startTime = System.currentTimeMillis();
 		final int res = IGraphRawLibrary.igraph_erdos_renyi_game_gnp(
 				g,
 				size,
 				proba,
 				directed,
 				allowLoops
 		);
 		final long duration = System.currentTimeMillis() - startTime;
 		//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 		listOfMessages.debugTech("processing took "+duration+" ms", getClass());
 		// detect errors
 		checkIGraphResult(res);
 		
 		IGraphGraph result = new IGraphGraph(this, g, directed);
 		
 		// basic checks
 		// TODO
 		
 		result.setMultiGraph(false); // looks like the algo never generates double links
 		
 		return result;
 		
 	}
 	
 
 	public IGraphGraph generateErdosRenyiGNM(int size, double m, boolean directed, boolean allowLoops) {
 
 		final InternalGraphStruct g = createEmptyGraph();
 				
 		//GLLogger.debugTech("calling igraph", getClass());
 		final long startTime = System.currentTimeMillis();
 		final int res = IGraphRawLibrary.igraph_erdos_renyi_game_gnm(
 				g,
 				size,
 				m,
 				directed,
 				allowLoops
 		);
 		final long duration = System.currentTimeMillis() - startTime;
 		//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 		listOfMessages.debugTech("processing took "+duration+" ms", getClass());
 		// detect errors
 		checkIGraphResult(res);
 		
 		IGraphGraph result = new IGraphGraph(this, g, directed);
 		
 		// basic checks
 		// TODO
 
 		result.setMultiGraph(false); // looks like the algo never generates double links
 		
 		return result;
 		
 	}
 	
 	public IGraphGraph generateForestFire(int size, double fw_prob, double bw_factor,
 		    int pambs, boolean directed) {
 
 		final InternalGraphStruct g = createEmptyGraph();
 				
 		//GLLogger.debugTech("calling igraph", getClass());
 		final long startTime = System.currentTimeMillis();
 		final int res = IGraphRawLibrary.igraph_forest_fire_game(
 				g, 
 				size, 
 				fw_prob, 
 				bw_factor, 
 				pambs, 
 				directed
 				);
 		final long duration = System.currentTimeMillis() - startTime;
 		//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 		listOfMessages.debugTech("processing took "+duration+" ms", getClass());
 		// detect errors
 		checkIGraphResult(res);
 		
 		IGraphGraph result = new IGraphGraph(this, g, directed);
 		
 		// basic checks
 		// TODO
 		
 		result.setMultiGraph(true); // looks like the algo never generates double links
 		
 		return result;
 		
 	}
 	
 	public IGraphGraph generateBarabasiAlbert(int size, int m, double power, double zeroAppeal, boolean directed, boolean outputPref, double A) {
 
 		final InternalGraphStruct g = createEmptyGraph();
 		
 		//GLLogger.debugTech("calling igraph", getClass());
 		final long startTime = System.currentTimeMillis();
 		final int res = IGraphRawLibrary.igraph_barabasi_game(
 				g, 
 				size, 
 				power, 
 				m, 
 				null, 
 				outputPref, 
 				A, 
 				directed, 
 				// always use this algo which is said to be ok for any combination of power and m
 				IGraphBarabasiAlgorithm.IGRAPH_BARABASI_PSUMTREE.ordinal(), 
 				null
 				);
 		final long duration = System.currentTimeMillis() - startTime;
 		//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 		listOfMessages.debugTech("processing took "+duration+" ms", getClass());
 		// detect errors
 		checkIGraphResult(res);
 		
 		IGraphGraph result = new IGraphGraph(this, g, directed);
 		
 		// basic checks
 		// TODO
 		
 		result.setMultiGraph(false); // looks like this PSUMTREE algo never generates double links
 		
 		return result;
 		
 	}
 	
 	public IGraphGraph generateInterconnectedIslands(
 			int islands_n, 
 			int islands_size,
 			double islands_pin, 
 			int n_inter,
 			boolean simplifyLoops,
 			boolean simplifyMultiplex
 			) {
 
 		if (islands_n < 0)
 			throw new WrongParametersException("the number of islands should be greater than 0");
 		
 		if (islands_size < 0)
 			throw new WrongParametersException("the size of islands should be greater than 0");
 		
 		if (islands_pin < 0.0 || islands_pin > 1.0)
 			throw new WrongParametersException("the probability to create links within islands should be between 0.0 and 1.0");
 		
 		if (n_inter < 0)
 			throw new WrongParametersException("the number of links inter islands should be greater than 0");
 		
 		if (n_inter > islands_size)
 			throw new WrongParametersException("the number of links inter islands should be lower than the size of islands");
 		
 		final InternalGraphStruct g = createEmptyGraph();
 				
 		//GLLogger.debugTech("calling igraph", getClass());
 		final long startTime = System.currentTimeMillis();
 		int res = IGraphRawLibrary.igraph_simple_interconnected_islands_game(g, islands_n, islands_size, islands_pin, n_inter);
 		
 		final long duration = System.currentTimeMillis() - startTime;
 		//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 		listOfMessages.debugTech("processing took "+duration+" ms", getClass());
 		// detect errors
 		checkIGraphResult(res);
 		
 		if (simplifyLoops || simplifyMultiplex) {
 			res = IGraphRawLibrary.igraph_simplify(g, simplifyMultiplex, simplifyLoops);
 			checkIGraphResult(res);
 		}
 		
 		IGraphGraph result = new IGraphGraph(this, g, false);
 		
 		result.setMultiGraph(!simplifyMultiplex); 
 
 		// basic checks
 		// TODO
 		
 		return result;
 		
 	}
 	
 	public IGraphGraph generateWattsStrogatz(int size, int dimension, double proba, int nei, boolean allowLoops, boolean allowMultiple) {
 		
 		final InternalGraphStruct g = createEmptyGraph();
 		
 		//GLLogger.debugTech("calling igraph", getClass());
 		final long startTime = System.currentTimeMillis();
 		int res = IGraphRawLibrary.igraph_watts_strogatz_game(
 				g,
 				dimension, 
 				size, 
 				nei, 
 				proba, 
 				allowLoops, 
 				allowMultiple
 				);
 		final long duration = System.currentTimeMillis() - startTime;
 		//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 
 		// detect errors
 		checkIGraphResult(res);
 		
 		IGraphGraph result = new IGraphGraph(this, g, false);
 		
		result.setMultiGraph(allowMultiple); // looks like the algo never generates double links
 
 		return result;
 	}
 	
 	public IGraphGraph generateGRG(int nodes, double radius, boolean torus) {
 		
 		final InternalGraphStruct g = createEmptyGraph();
 		
 		InternalVectorStruct x = new InternalVectorStruct();
 		IGraphRawLibrary.igraph_vector_init(x, 0);
 		InternalVectorStruct y = new InternalVectorStruct();
 		IGraphRawLibrary.igraph_vector_init(y, 0);
 		
 		try {
 		
 			//GLLogger.debugTech("calling igraph", getClass());
 			final long startTime = System.currentTimeMillis();
 			int res = IGraphRawLibrary.igraph_grg_game(
 					g,
 					nodes,
 					radius,
 					torus,
 					x,
 					y
 					);
 			final long duration = System.currentTimeMillis() - startTime;
 			//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 	
 			// detect errors
 			checkIGraphResult(res);
 			
 			IGraphGraph result = new IGraphGraph(this, g, false);
 			result.xPositions = x.asDoubleArray(nodes);
 			result.yPositions = y.asDoubleArray(nodes);
 					
 			return result;
 
 		} finally {
 			IGraphRawLibrary.igraph_vector_destroy(x);
 			IGraphRawLibrary.igraph_vector_destroy(y);
 		}
 	}
 	
 	public IGraphGraph generateLCF(int nodes, int[] paramShifts, int repeats) {
 		
 		final InternalGraphStruct g = createEmptyGraph();
 		
 		if (repeats < 1)
 			throw new WrongParametersException("argument repeat should be positive");
 		if (nodes < 1)
 			throw new WrongParametersException("argument nodes should be positive");
 
 		// init the shifts param
 		InternalVectorStruct shifts = new InternalVectorStruct();
 		{
 			Pointer shitsPointer = new Memory(paramShifts.length * Native.getNativeSize(Double.TYPE));
 			for (int dloop=0; dloop<paramShifts.length; dloop++) {
 				// populate the array with junk data (just for the sake of the example)
 				shitsPointer.setDouble(dloop * Native.getNativeSize(Double.TYPE), (double)paramShifts[dloop]);
 			}
 			IGraphRawLibrary.igraph_vector_init_copy(shifts, shitsPointer, paramShifts.length);
 
 		}
 
 		try {
 		
 			//GLLogger.debugTech("calling igraph", getClass());
 			final long startTime = System.currentTimeMillis();
 			int res = IGraphRawLibrary.igraph_lcf_vector(
 					g, 
 					nodes, 
 					shifts, 
 					repeats
 					);
 			final long duration = System.currentTimeMillis() - startTime;
 			//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 	
 			// detect errors
 			checkIGraphResult(res);
 			
 			IGraphGraph result = new IGraphGraph(this, g, false);
 
 			return result;
 
 		} finally {
 			IGraphRawLibrary.igraph_vector_destroy(shifts);
 		}
 	}
 	
 	public void simplifyGraph(
 			IGraphGraph g,
 			boolean removeMultiple, boolean removeLoops
 			) {
 	
 		
 		final long startTime = System.currentTimeMillis();
 		final int res = IGraphRawLibrary.igraph_simplify(g.getStruct(), removeMultiple, removeLoops);
 		
 		final long duration = System.currentTimeMillis() - startTime;
 		//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 		listOfMessages.debugTech("processing took "+duration+" ms", getClass());
 		// detect errors
 		checkIGraphResult(res);
 		
 		if (removeMultiple)
 			g.setMultiGraph(false); 
 
 		
 	}
 	
 	/**
 	 * sets the seed of the default random number generator
 	 * @param seed
 	 */
 	public void setSeed(long seed) {
 		
 		Pointer p = IGraphRawLibrary.igraph_rng_default();
 		int res = IGraphRawLibrary.igraph_rng_seed(p, new NativeLong(seed));
 		checkIGraphResult(res);
 		
 	}
 
 	public IGraphGraph copyGraph(IGraphGraph original) {
 
 		final InternalGraphStruct theCopy = createEmptyGraph();
 
 		IGraphRawLibrary.igraph_copy(original.getStruct(), theCopy);
 		
 		IGraphGraph theCopyRes = new IGraphGraph(original, this, theCopy);
 		
 		return theCopyRes;
 		
 	}
 	
 	public InternalVectorStruct createEmptyVector(int size) {
 	
 		final InternalVectorStruct vector = new InternalVectorStruct();
 		
 		if (size > 0) {
 			int res = IGraphRawLibrary.igraph_vector_init(vector, size);
 			checkIGraphResult(res);
 		}
 		
 		return vector;
 	}
 	
 	public void clearVector(InternalVectorStruct vector) {
 		
 		IGraphRawLibrary.igraph_vector_destroy(vector);
 	}
 	
 	public IGraphGraph generateEmpty(int size, boolean directed) {
 	
 		final InternalGraphStruct g = createEmptyGraph();
 				
 		//GLLogger.debugTech("calling igraph", getClass());
 		final long startTime = System.currentTimeMillis();
 		final int res = IGraphRawLibrary.igraph_empty(
 				g, 
 				size, 
 				directed
 				);
 		final long duration = System.currentTimeMillis() - startTime;
 		//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 
 		// detect errors
 		checkIGraphResult(res);
 		
 		IGraphGraph result = new IGraphGraph(this, g, directed, size);
 
 		
 		// basic checks
 		// TODO
 		
 		return result;
 	}
 	
 	
 	public IGraphGraph generateErdosRenyiGNP(int size, double proba) {
 		return generateErdosRenyiGNP(size, proba, false, false);
 	}
 	
 	/**
 	 * adds count vertices to this igraph graph
 	 * @param g
 	 * @param count
 	 */
 	public void addVertices(IGraphGraph g, int count) {
 		
 		if (count == 0)
 			return;
 		
 		g.graphChanged();
 
 		if (count < 0)
 			throw new WrongParametersException("count of vertices should be positive");
 
 		final int res = IGraphRawLibrary.igraph_add_vertices(g.getPointer(), count, null);
 		
 		checkIGraphResult(res);
 		
 		
 	}
 	
 	public void addEdge(IGraphGraph g, int from, int to) {
 		
 		g.graphChanged();
 		
 		if (from < 0 || to < 0)
 			throw new WrongParametersException("ids of nodes should be positive");
 		
 		// TODO check parameters
 		
 		final int res = IGraphRawLibrary.igraph_add_edge(g.getPointer(), from, to);
 		
 		checkIGraphResult(res);
 	}
 	
 	public void addEdges(IGraphGraph g, InternalVectorStruct edges) {
 		
 		g.graphChanged();
 		
 		edges.write();
 		final int res = IGraphRawLibrary.igraph_add_edges(g.getPointer(), edges, null);
 		
 		checkIGraphResult(res);
 	}
 	
 	
 	public void rewire(IGraphGraph g, int count) {
 		
 		g.graphChanged();
 		
 		if (count <= 0)
 			throw new WrongParametersException("count of nodes should be positive");
 		
 		int res = IGraphRawLibrary.igraph_rewire(g.getPointer(), count, 0);
 		
 		checkIGraphResult(res);
 
 		//res = IGraphRawLibrary.igraph_simplify(g.getStruct(), true, true);
 		
 		//checkIGraphResult(res);
 
 		//g.setMultiGraph(false); 
 
 	}
 	
 	public int getVertexCount(IGraphGraph g) {
 		
 		return IGraphRawLibrary.igraph_vcount(g.getPointer());
 	}
 	
 	public int getVertexCount(InternalGraphStruct g) {
 		
 		return IGraphRawLibrary.igraph_vcount(g.getPointer());
 	}
 	
 
 	public int getEdgeCount(IGraphGraph g) {
 				
 		return IGraphRawLibrary.igraph_ecount(g.getPointer());
 	}
 	
 	
 	public boolean isDirected(IGraphGraph g) {
 	
 		return IGraphRawLibrary.igraph_is_directed(g.getPointer());
 		
 	}
 	
 	public double computeAveragePathLength(IGraphGraph g) {
 		
 		if (paramUseCache && g.hasCachedProperty(GRAPH_KEY_AVERAGE_PATH_LENGTH)) {
 			return (Double) g.getCachedProperty(GRAPH_KEY_AVERAGE_PATH_LENGTH);
 		}
 		
 		DoubleByReference res = new DoubleByReference();
 		
 		//GLLogger.debugTech("calling igraph to compute average path length...", getClass());
 
 		final long startTime = System.currentTimeMillis();
 		
 		final int res2 = IGraphRawLibrary.igraph_average_path_length(g.getPointer(), res, g.directed, true);
 
 		final long duration = System.currentTimeMillis() - startTime;
 		//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 
 		
 		checkIGraphResult(res2);
 		
 		final double length = res.getValue();
 		
 		g.setCachedProperty(GRAPH_KEY_AVERAGE_PATH_LENGTH, length);
 		
 		//System.err.println("igraph/ average path length: "+length);
 
 		return length;
 		
 	}
 	
 	public boolean computeIsomorphicm(IGraphGraph g1, IGraphGraph g2) {
 		
 		IntByReference res = new IntByReference();
 		
 		//GLLogger.debugTech("calling igraph to compute average path length...", getClass());
 
 		final long startTime = System.currentTimeMillis();
 
 		
 		final int res2 = IGraphRawLibrary.igraph_isomorphic(g1.getPointer(), g2.getPointer(), res);
 
 		final long duration = System.currentTimeMillis() - startTime;
 		//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 		
 		checkIGraphResult(res2);
 		
 		final Boolean iso = (res.getValue() != 0);
 		
 
 		return iso;
 		
 	}
 	
 	public boolean computeIsomorphismVF2(IGraphGraph g1, IGraphGraph g2) {
 		
 		IntByReference res = new IntByReference();
 		
 		//GLLogger.debugTech("calling igraph to compute average path length...", getClass());
 
 		final long startTime = System.currentTimeMillis();
 
 		
 		final int res2 = IGraphRawLibrary.igraph_isomorphic_vf2(
 				g1.getPointer(), g2.getPointer(), 
 				null, null, null, null, 
 				res, 
 				null, null, 
 				null, 
 				null, null
 				);
 
 		final long duration = System.currentTimeMillis() - startTime;
 		//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 		
 		checkIGraphResult(res2);
 		
 		final Boolean iso = (res.getValue() != 0);
 		
 
 		return iso;
 		
 	}
 	
 	
 	public int computeIsomorphismVF2Count(IGraphGraph g1, IGraphGraph g2) {
 		
 		IntByReference res = new IntByReference();
 		
 		//GLLogger.debugTech("calling igraph to compute average path length...", getClass());
 
 		final long startTime = System.currentTimeMillis();
 
 		
 		final int res2 = IGraphRawLibrary.igraph_count_isomorphisms_vf2(
 				g1.getPointer(), g2.getPointer(), 
 				null, null, null, null, 
 				res, 
 				null, null, 
 				null
 				);
 
 		final long duration = System.currentTimeMillis() - startTime;
 		//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 		
 		checkIGraphResult(res2);
 		
 		return res.getValue();
 	
 	}
 	
 	public boolean computeVF2Isomorphicm(IGraphGraph g1, IGraphGraph g2) {
 		
 		IntByReference res = new IntByReference();
 		
 		//GLLogger.debugTech("calling igraph to compute average path length...", getClass());
 
 		final long startTime = System.currentTimeMillis();
 
 		
 		final int res2 = IGraphRawLibrary.igraph_isomorphic(g1.getPointer(), g2.getPointer(), res);
 
 		final long duration = System.currentTimeMillis() - startTime;
 		//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 		
 		checkIGraphResult(res2);
 		
 		final Boolean iso = res.getValue()==0;
 		
 
 		return iso;
 		
 	}
 	
 
 	public int computeDiameter(IGraphGraph g) {
 		
 		if (paramUseCache && g.hasCachedProperty(GRAPH_KEY_DIAMETER)) {
 			return (Integer) g.getCachedProperty(GRAPH_KEY_DIAMETER);
 		}
 		
 		// compute 
 		IntByReference res = new IntByReference();
 		
 		//GLLogger.debugTech("calling igraph to compute the diameter...", getClass());
 
 		final long startTime = System.currentTimeMillis();
 		
 		final int res2 = IGraphRawLibrary.igraph_diameter(
 				g.getPointer(), 
 				res,
 				null, 
 				null, 
 				null, 
 				g.directed, 
 				true
 				);
 		
 		final long duration = System.currentTimeMillis() - startTime;
 		//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 		
 		checkIGraphResult(res2);
 		
 		
 		// use result
 		final int length = res.getValue();
 		
 		// store in cache
 		g.setCachedProperty(GRAPH_KEY_DIAMETER, new Integer(length));
 		
 		//System.err.println("igraph/ diameter: "+length);
 
 		return length;
 		
 	}
 	
 
 	public boolean isConnected(IGraphGraph g) {
 		
 		if (paramUseCache && g.hasCachedProperty(GRAPH_KEY_CONNECTED)) {
 			return (Boolean) g.getCachedProperty(GRAPH_KEY_CONNECTED);
 		}
 		
 		IntByReference res = new IntByReference();
 		
 		//GLLogger.debugTech("calling igraph to compute the diameter...", getClass());
 
 		final long startTime = System.currentTimeMillis();
 		
 		final int res2 = IGraphRawLibrary.igraph_is_connected(
 				g.getPointer(), 
 				res,
 				1
 				);
 		
 		final long duration = System.currentTimeMillis() - startTime;
 		//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 		
 		checkIGraphResult(res2);
 		
 		final boolean connected = res.getValue()>0;
 		//System.err.println("igraph/ connected: "+connected);
 
 		g.setCachedProperty(GRAPH_KEY_CONNECTED, new Boolean(connected));
 
 		return connected;
 		
 	}
 	
 
 	protected void computeComponentThings(IGraphGraph g) {
 
 		IntByReference res = new IntByReference();
 		
 
 		final long startTime = System.currentTimeMillis();
 		
 		//GLLogger.debugTech("calling igraph to initialize vectors...", getClass());
 
 		InternalVectorStruct membership = new InternalVectorStruct();
 		IGraphRawLibrary.igraph_vector_init(membership, 0);
 		InternalVectorStruct csize = new InternalVectorStruct();
 		IGraphRawLibrary.igraph_vector_init(csize, 0);
 		IntByReference count = new IntByReference();
 		
 		try {
 			//GLLogger.debugTech("calling igraph to compute the clusters...", getClass());
 	
 			final int res2 = IGraphRawLibrary.igraph_clusters(
 					g.getPointer(), 
 					membership, 
 					csize, 
 					count, 
 					1
 					);
 			
 			final long duration = System.currentTimeMillis() - startTime;
 			//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 			
 			checkIGraphResult(res2);
 			
 			// process and store results
 			
 			// count
 			{
 				final int countInt = count.getValue();
 				//System.err.println("igraph/ components: "+countInt);
 				g.setCachedProperty(GRAPH_KEY_COMPONENTS_COUNT, new Integer(countInt));
 			
 			}
 			
 			// memberships
 			{
 				final int membershipSize = IGraphRawLibrary.igraph_vector_size(membership);
 				final double[] memberships = membership.asDoubleArray(membershipSize);
 				g.setCachedProperty(GRAPH_KEY_COMPONENTS_MEMBERSHIP, membership);
 			}
 			
 			// size
 			{
 				final int csizeSize = IGraphRawLibrary.igraph_vector_size(csize);
 				final double[] csizes = csize.asDoubleArray(csizeSize);
 				g.setCachedProperty(GRAPH_KEY_COMPONENTS_CLUSTER_SIZES, csizes);
 				double max = 0;
 				for (int i=0; i<csizes.length; i++) {
 					max = Math.max(csizes[i], max);
 				}
 				//System.err.println("igraph/ giant cluster: "+max);
 				g.setCachedProperty(GRAPH_KEY_COMPONENTS_GIANT_SIZE, new Integer((int)max));
 			}
 			
 		} finally {
 			IGraphRawLibrary.igraph_vector_destroy(csize);
 			IGraphRawLibrary.igraph_vector_destroy(membership);
 		}
 		
 	}
 	
 	/**
 	 * Unstable !
 	 * @param g
 	 * @param directed
 	 */
 	public double[] computeNodeBetweeness(IGraphGraph g, boolean directed) {
 
 		if (true)
 			throw new NotImplementedException("unfortunately, igraph is crashing on this");
 		
 		final long startTime = System.currentTimeMillis();
 		
 		final int verticesCount = IGraphRawLibrary.igraph_vcount(g.getPointer());
 		
 		//GLLogger.debugTech("calling igraph to initialize vectors...", getClass());
 
 		InternalVectorStruct res = new InternalVectorStruct();
 		int resA =IGraphRawLibrary.igraph_vector_init(res, verticesCount);
 		checkIGraphResult(resA);
 
 		//InternalVertexSelector vids = IGraphRawLibrary.igraph_vss_all();
 
 		Pointer vids = IGraphRawLibrary.igraph_vss_none();
 		resA = IGraphRawLibrary.igraph_vss_all(vids);
 		checkIGraphResult(resA);
 	
 		try {
 	
 			final int res2 = IGraphRawLibrary.igraph_betweenness(
 					g.getPointer(), 
 					res, 
 					null, //vids, 
 					directed, 
 					null, // weights
 					true // ! true here creates a complete failure o_O
 					);
 					
 			
 			final long duration = System.currentTimeMillis() - startTime;
 			//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 			
 			checkIGraphResult(res2);
 			
 			// process and store results
 			final int resSize = IGraphRawLibrary.igraph_vector_size(res);
 			final double[] resRes = res.asDoubleArray(resSize);
 			//System.err.println("betweeness: "+Arrays.toString(resRes));
 			// TODO store inside cache
 			return resRes;
 		
 		} finally {
 			
 			IGraphRawLibrary.igraph_vector_destroy(res);
 			
 		}
 		
 	}
 	
 	/**
 	 * Unstable !
 	 * @param g
 	 * @param directed
 	 */
 	public double[] computeNodeBetweenessEstimate(IGraphGraph g, boolean directed, double cutoff) {
 
 
 		if (true)
 			throw new NotImplementedException("unfortunately, igraph is crashing on this");
 		
 		final long startTime = System.currentTimeMillis();
 		
 		final int verticesCount = IGraphRawLibrary.igraph_vcount(g.getPointer());
 		
 		//GLLogger.debugTech("calling igraph to initialize vectors...", getClass());
 
 		InternalVectorStruct res = new InternalVectorStruct();
 		IGraphRawLibrary.igraph_vector_init(res, verticesCount);
 		
 		Pointer vids = IGraphRawLibrary.igraph_vss_none();
 		int resA = IGraphRawLibrary.igraph_vss_all(vids);
 		checkIGraphResult(resA);
 	
 		try {
 	
 			final int res2 = IGraphRawLibrary.igraph_betweenness_estimate(
 					g.getPointer(), 
 					res, 
 					vids, 
 					directed, 
 					cutoff,
 					(InternalVectorStruct)null, // weights
 					true
 					);
 					
 			
 			final long duration = System.currentTimeMillis() - startTime;
 			//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 			
 			checkIGraphResult(res2);
 			
 			// process and store results
 			final int resSize = IGraphRawLibrary.igraph_vector_size(res);
 			final double[] resRes = res.asDoubleArray(resSize);
 			//System.err.println("betweeness: "+Arrays.toString(resRes));
 			// TODO store inside cache
 			return resRes;
 		
 		} finally {
 			
 			IGraphRawLibrary.igraph_vector_destroy(res);
 			
 		}
 		
 	}
 	
 	public double[] computeEdgeBetweeness(IGraphGraph g, boolean directed) {
 
 
 		final long startTime = System.currentTimeMillis();
 		
 		//GLLogger.debugTech("calling igraph to initialize vectors...", getClass());
 
 		final int edgesCount = IGraphRawLibrary.igraph_ecount(g.getPointer());
 		
 		InternalVectorStruct res = new InternalVectorStruct();
 		IGraphRawLibrary.igraph_vector_init(res, edgesCount);
 	
 		InternalVectorStruct weights = new InternalVectorStruct();
 		int resA = IGraphRawLibrary.igraph_vector_init(weights, edgesCount);
 		checkIGraphResult(resA);
 
 		try {
 			//GLLogger.debugTech("calling igraph to compute the clusters...", getClass());
 	
 			final int res2 = IGraphRawLibrary.igraph_edge_betweenness(
 					g.getPointer(), 
 					res, 
 					directed, 
 					null
 					);
 					
 			
 			final long duration = System.currentTimeMillis() - startTime;
 			//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 			
 			checkIGraphResult(res2);
 			
 			// process and store results
 			
 						
 			// memberships
 			{
 				final int resSize = IGraphRawLibrary.igraph_vector_size(res);
 				final double[] resRes = res.asDoubleArray(resSize);
 				//System.err.println("betweeness: "+Arrays.toString(resRes));
 				// TODO !!
 				// TODO cache!
 				
 				return resRes;
 			}
 			
 			
 		} finally {
 			
 			IGraphRawLibrary.igraph_vector_destroy(res);
 			
 			
 			IGraphRawLibrary.igraph_vector_destroy(weights);
 
 			
 		}
 		
 	}
 
 	public double[] computeEdgeBetweenessEstimate(IGraphGraph g, boolean directed, double cutoff) {
 
 
 		final long startTime = System.currentTimeMillis();
 		
 		//GLLogger.debugTech("calling igraph to initialize vectors...", getClass());
 
 		final int edgesCount = IGraphRawLibrary.igraph_ecount(g.getPointer());
 		
 		InternalVectorStruct res = new InternalVectorStruct();
 		IGraphRawLibrary.igraph_vector_init(res, edgesCount);
 	
 		InternalVectorStruct weights = new InternalVectorStruct();
 		int resA = IGraphRawLibrary.igraph_vector_init(weights, edgesCount);
 		checkIGraphResult(resA);
 
 		try {
 			//GLLogger.debugTech("calling igraph to compute the clusters...", getClass());
 	
 			final int res2 = IGraphRawLibrary.igraph_edge_betweenness_estimate(
 					g.getPointer(), 
 					res, 
 					directed,
 					cutoff,
 					null //weights,
 					);
 					
 			
 			final long duration = System.currentTimeMillis() - startTime;
 			//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 			
 			checkIGraphResult(res2);
 			
 			// process and store results
 			
 						
 			// memberships
 			{
 				final int resSize = IGraphRawLibrary.igraph_vector_size(res);
 				final double[] resRes = res.asDoubleArray(resSize);
 				//System.err.println("betweeness: "+Arrays.toString(resRes));
 				// TODO !!
 				// TODO cache!
 				
 				return resRes;
 			}
 			
 			
 		} finally {
 			
 			IGraphRawLibrary.igraph_vector_destroy(res);
 			
 			
 			IGraphRawLibrary.igraph_vector_destroy(weights);
 
 			
 		}
 		
 	}
 	
 	public int computeComponentsCount(IGraphGraph g) {
 		
 		if (paramUseCache && g.hasCachedProperty(GRAPH_KEY_COMPONENTS_COUNT)) {
 			return (Integer) g.getCachedProperty(GRAPH_KEY_COMPONENTS_COUNT);
 		}
 		
 		computeComponentThings(g);
 		
 		return (Integer) g.getCachedProperty(GRAPH_KEY_COMPONENTS_COUNT);
 		
 	}
 	
 	public int computeGiantCluster(IGraphGraph g) {
 		
 		if (paramUseCache && g.hasCachedProperty(GRAPH_KEY_COMPONENTS_GIANT_SIZE)) {
 			return (Integer) g.getCachedProperty(GRAPH_KEY_COMPONENTS_GIANT_SIZE);
 		}
 		
 		computeComponentThings(g);
 		
 		return (Integer) g.getCachedProperty(GRAPH_KEY_COMPONENTS_GIANT_SIZE);
 		
 	}
 	
 	public Double computeGlobalClustering(IGraphGraph g) {
 		
 		if (paramUseCache && g.hasCachedProperty(GRAPH_KEY_CLUSTERING_GLOBAL)) {
 			return (Double) g.getCachedProperty(GRAPH_KEY_CLUSTERING_GLOBAL);
 		}
 		
 		// compute 
 		DoubleByReference res = new DoubleByReference();
 		
 		//GLLogger.debugTech("calling igraph to compute the global clustering...", getClass());
 
 		final long startTime = System.currentTimeMillis();
 		
 		final int res2 = IGraphRawLibrary.igraph_transitivity_undirected(
 				g.getPointer(), 
 				res,
 				0
 				);
 		
 		final long duration = System.currentTimeMillis() - startTime;
 		//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 		
 		checkIGraphResult(res2);
 		
 		// use result
 		final double clustering = res.getValue();
 		
 		// store in cache
 		g.setCachedProperty(GRAPH_KEY_CLUSTERING_GLOBAL, new Double(clustering));
 		
 		//System.err.println("igraph/ clustering global: "+clustering);
 
 		return clustering;
 		
 	}
 	
 	public Double computeGlobalClusteringLocal(IGraphGraph g) {
 		
 		if (paramUseCache && g.hasCachedProperty(GRAPH_KEY_CLUSTERING_GLOBAL_AVG)) {
 			return (Double) g.getCachedProperty(GRAPH_KEY_CLUSTERING_GLOBAL_AVG);
 		}
 		
 		// compute 
 		DoubleByReference res = new DoubleByReference();
 		
 		//GLLogger.debugTech("calling igraph to compute the average clustering...", getClass());
 
 		final long startTime = System.currentTimeMillis();
 		
 		final int res2 = IGraphRawLibrary.igraph_transitivity_avglocal_undirected(
 				g.getPointer(), 
 				res,
 				0
 				);
 		
 		final long duration = System.currentTimeMillis() - startTime;
 		//GLLogger.debugTech("back from igraph after "+duration+" ms", getClass());
 		
 		checkIGraphResult(res2);
 		
 		// use result
 		final double clustering = res.getValue();
 		
 		// store in cache
 		g.setCachedProperty(GRAPH_KEY_CLUSTERING_GLOBAL_AVG, new Float(clustering));
 		
 		//System.err.println("igraph/ average clustering: "+clustering);
 
 		return clustering;
 		
 	}
 	
 	/**
 	 * An edge iterator which uses igraph JNA calls to retrieve edge
 	 * information. Slow. Use the internal graph struct iterator instead.
 	 * 
 	 * @author Samuel Thiriot
 	 *
 	 */
 	protected class EdgesIterator implements Iterator<IGraphEdge> {
 
 		private final IGraphGraph g;
 		
 		int lastEdgeId = 0;
 		final int maxEdge;
 		
 		public EdgesIterator(IGraphGraph g) {
 			this.g = g;
 			maxEdge = getEdgeCount(g);
 		}
 		
 		@Override
 		public boolean hasNext() {
 			
 			return lastEdgeId < maxEdge;
 		}
 
 		@Override
 		public IGraphEdge next() {
 			IntByReference from = new IntByReference();
 			IntByReference to = new IntByReference();
 			IGraphRawLibrary.igraph_edge(g.getPointer(), lastEdgeId, from, to);
 			
 			return new IGraphEdge(lastEdgeId++, from.getValue(), to.getValue());
 		}
 
 		@Override
 		public void remove() {
 			throw new NotImplementedException();
 		}
 		
 	}
 
 	/**
 	 * Returns a slow iterator which relies on igraph JNA calls to retrieve
 	 * the list of edges and each edge. Use is not recommanded.
 	 * @param g
 	 * @return
 	 */
 	public Iterator<IGraphEdge> getEdgeIterator(IGraphGraph g) {
 		return new EdgesIterator(g);
 	}
 	
 	/*
 	public IGenlabGraph computeClusterInfos(IGraphGraph g) {
 		
 		if (paramUseCache && g.hasCachedProperty(GRAPH_KEY_COMPONENTS_)) {
 			return (Integer) g.getCachedProperty(GRAPH_KEY_COMPONENTS_GIANT_SIZE);
 		}
 		
 		computeComponentThings(g);
 		
 		return (Integer) g.getCachedProperty(GRAPH_KEY_COMPONENTS_GIANT_SIZE);
 		
 	}
 	*/
 
 	public void installProgressCallback(IIGraphProgressCallback cb) {
 		IGraphRawLibrary.igraph_set_progress_handler(cb);
 		
 	}
 	
 	public void uninstallProgressCallback() {
 		IGraphRawLibrary.igraph_set_progress_handler(null);
 		
 	}
 }
