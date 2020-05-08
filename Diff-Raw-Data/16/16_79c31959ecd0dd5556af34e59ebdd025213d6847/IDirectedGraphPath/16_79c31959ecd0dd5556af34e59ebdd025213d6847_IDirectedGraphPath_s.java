 package be.uclouvain.jail.graph;
 
 import be.uclouvain.jail.adapt.IAdaptable;
 
 /**
  * Defines a path inside a directed graph.
  * 
  * <p>A graph path is defined as a sequence of vertices separated by edges.
  * Edges are considered as more representative of the path than vertices;
  * For this reason path size is defined as the number of edges, and 
  * <code>Iterable</code> implementation always returns an iterator on edges.</p>
  * 
  * <p>This interface provides iterators on vertices and edges as well as a 
  * visitable pattern implementation. Moreover, graph paths are required to 
  * be adaptable ; at least DirectedGraph is indeed a natural adaptation target 
  * and sould then be implemented.</p>
  * 
  * <p>This interface may be implemented. A default implementation is provided
 * by {@link DefaultDirectedGraphPath}.</p>
  * 
  * @author blambeau
  */
 public interface IDirectedGraphPath extends IAdaptable, Iterable<Object> {
 
 	/** 
 	 * Provides a contract for visitors of graph paths.
 	 * 
 	 * <p>A visit of a graph path always starts in the first vertex, so 
 	 * that the first invocation of {@link #visit(Object, Object)} method
 	 * always uses a null value for the first argument.</p>  
 	 */
 	public static interface IVisitor {
 		
 		/**
 		 * Fired when a vertex is entered using some edge.
 		 * 
 		 * @param edge edge used to reach the visited vertex, or null
 		 *        if vertex is the first one visited.
 		 * @param vertex visited vertex.
 		 */
 		public void visit(Object edge, Object vertex);
 		
 	}
 	
 	/** Returns the size of the path, defined as the number of used edges in 
 	 * the path. The number of visited vertices is equal to getPathSize()+1
 	 * by definition. */
 	public int size();
 
 	/** Returns an iterator on path edges. */
 	public Iterable<Object> edges();
 
 	/** Returns an iterator on path vertices. */
 	public Iterable<Object> vertices();
 
 	/** Accepts a visitor. */
 	public void accept(IVisitor visitor);
 	
 }
