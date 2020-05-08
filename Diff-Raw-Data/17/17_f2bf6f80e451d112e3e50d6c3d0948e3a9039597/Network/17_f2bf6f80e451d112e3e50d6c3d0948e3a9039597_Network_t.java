 package modularizator.logic;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.Set;
 
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.jgrapht.Graphs;
 import org.jgrapht.graph.DefaultDirectedGraph;
 import org.jgrapht.graph.DefaultEdge;
 
 /**
  * Represents a network, i.e. a directed multigraph.
  * Extends the default directed graph class from jGrapht library
  * with vertices being java compilation units.
  * Also contains clusters - groups of vertices
  * @author vahan
  *
  */
 public class Network extends DefaultDirectedGraph<ICompilationUnit, DefaultEdge> {
 	/**
 	 * Randomly generated serial version UID
 	 */
 	private static final long serialVersionUID = 2143531432815293678L;
 	/**
 	 * Associative array giving the cluster for each vertex
 	 */
 	private HashMap<ICompilationUnit, Cluster> clusters;
 	/**
 	 * The name of the network. Used for GUI purposes
 	 */
 	private String name;
 	/**
 	 * Constructor
 	 * @param edgeClass
 	 * @param clusters
 	 * @param name
 	 */
 	public Network(Class<? extends DefaultEdge> edgeClass, HashMap<ICompilationUnit, Cluster> clusters, String name) {
 		super(edgeClass);
 		this.clusters = clusters;
 		initVertices();
 		this.name = getProjectName() + name;
 	}
 	
 	public HashMap<ICompilationUnit, Cluster> getClusters() {
 		return clusters;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name) {
 		this.name = getProjectName() + name;
 	}
 	
 	public Cluster getCluster(Object vertex) {
 		return clusters.get(vertex);
 	}
 
 	public int clustersCount() {
 		return new HashSet<Cluster>(clusters.values()).size();
 	}
 	
 	/**
 	 * Gives a randomly chosen vertex from the network.
 	 * @param rnd	Pseudo-random number generator
 	 * @return		A randomly chosen vertex
 	 */
 	public ICompilationUnit getRandomVertex(Random rnd) {
 		Set<ICompilationUnit> vertexSet = this.vertexSet();
 		ICompilationUnit[] verteces = vertexSet.toArray(new ICompilationUnit[vertexSet.size()]);
 		int rndIndex = rnd.nextInt(verteces.length);
 
 		return verteces[rndIndex];
 	}
 
 	/**
 	 * Move the Object from a cluster into a new one
 	 * @param vertex		Object to move
 	 * @param newCluster	The new cluster where the object will be moved
 	 */
 	public void changeClusterAssignment(ICompilationUnit vertex, Cluster newCluster) {
 		clusters.get(vertex).getVertices().remove(vertex);
 		clusters.put(vertex, newCluster);
 		newCluster.getVertices().add(vertex);
 	}
 	
 	/**
 	 * Adds a new vertex to a cluster representing the given java element
 	 * @param compUnit	The vertex to add
 	 * @param elem		The java element that contains the class represented by the vertex
 	 */
 	public void add(ICompilationUnit compUnit, IJavaElement elem) {
 		Cluster cluster = clusters.get(compUnit);
 		if (cluster == null)
 			cluster = new Cluster(elem);
 		add(compUnit, cluster);
 	}
 	
 	
 	public void add(ICompilationUnit compUnit, Cluster cluster) {
 		clusters.put(compUnit, cluster);
 		cluster.getVertices().add(compUnit);
 		if (clusters.size() == 1)
 			name = getProjectName() + name;
 	}
 	
 	
 	public double getCF(Cluster cluster) {
 		double intraEdgesSum = 0;
 		double interEdgesSum = 0;
 		
 		for (ICompilationUnit v : cluster.getVertices()) {
 			for (Object u : Graphs.neighborListOf(this, v)) {
 				if (cluster.equals(clusters.get(u)))
 					intraEdgesSum++;
 				else
 					interEdgesSum++;
 			}
 		}
 		
 		if (intraEdgesSum == 0)
 			return 0;
 		double cf = intraEdgesSum / (intraEdgesSum + interEdgesSum / 2);
 		return cf;
 	}
 	
 	
 	public IJavaProject getProject() {
 		if (clusters.size() == 0)
 			return null;
 		Entry<ICompilationUnit, Cluster> entry = clusters.entrySet().iterator().next();
 		ICompilationUnit compUnit = entry.getKey();
 		IJavaProject project = compUnit.getJavaProject();
 		return project;
 	}
 	
 	
 	@Override
 	/**
 	 * Makes a new copy of the network.
 	 * Vertices and edges are shallow copied, clusters are copied one level deeper
 	 */
 	public Object clone() {
 		Network cloned = (Network) super.clone();
 
 		HashMap<ICompilationUnit, Cluster> clonedClusters = new HashMap<ICompilationUnit, Cluster>();
 		for (Entry<ICompilationUnit, Cluster> entry : clusters.entrySet()) {
 			clonedClusters.put(entry.getKey(), (Cluster) entry.getValue().clone());
 		}
 		cloned.clusters = clonedClusters;
 		
 		return cloned;
 	}
 	
 	/**
 	 * Adds all vertices of all clusters into the network
 	 */
 	private void initVertices() {
 		for (ICompilationUnit vertex : clusters.keySet()) {
 			super.addVertex(vertex);
 		}
 	}
 	
 	/**
 	 * Finds out the name of the project
 	 * @return	Name of the project
 	 */
 	private String getProjectName() {
 		IJavaProject project = getProject();
		if (project == null)
			return "";
 		return project.getElementName() + "_";
 	}
 
 }
