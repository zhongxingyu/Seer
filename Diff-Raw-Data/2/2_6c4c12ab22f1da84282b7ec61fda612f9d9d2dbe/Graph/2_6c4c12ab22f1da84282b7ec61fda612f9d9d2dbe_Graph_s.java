 package net.sf.javagimmicks.graph;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Represents a graph data structure.
  * <p>
  * Graphs consist of two types of elements: vertices and {@link Edge edges}.
  * <p>
  * The first ones - <b>vertices</b> can by of an Java type and can have any
  * value. Nevertheless it is strongly recommended that they have a well-defined
  * {@link Object#equals(Object)} method. Concrete {@link Graph} might also have
  * additional requirements (like a well-defined {@link Object#hashCode()}
  * method).
  * <p>
  * Edges must implement the {@link Edge} interface (or one of it's
  * sub-interfaces) - please refer to the respective interface {@link Edge
  * documentation} for more details
  * 
  * @param <VertexType>
  *           the type of vertices of this {@link Graph}
  * @param <EdgeType>
  *           the type of {@link Edge}s of this {@link Graph}
  */
 public interface Graph<VertexType, EdgeType extends Edge<VertexType, EdgeType>>
 {
    /**
     * Returns the number of vertices that this instance contains.
     * 
     * @return the number of vertices that this instance contains
     */
    int size();
 
    /**
     * Returns if this instance is empty (it contains no vertices).
     * 
     * @return if this instance is empty
     */
    boolean isEmpty();
 
    /**
     * Returns a {@link Set} of all vertices contained within this instance.
     * 
     * @return a {@link Set} of all vertices contained within this instance
     */
    Set<VertexType> vertexSet();
 
    /**
     * Returns a {@link Map} of all vertices contained within this instance
     * mapped to a {@link Set} of all {@link Edge}s connected to the vertex.
     * 
     * @return a {@link Map} of all vertices contained within this instance
     *         mapped to a {@link Set} of all {@link Edge}s connected to the
     *         vertex
     */
    Map<VertexType, Set<EdgeType>> edgeMap();
 
    /**
     * Checks and returns of a given vertex is contained within this instance.
     * 
     * @param vertex
     *           the vertex to check
     * @return if the given vertex is contained within this instance
     */
    boolean containsVertex(VertexType vertex);
 
    /**
     * Adds a new vertex to this instance (without any {@link Edge}s).
     * 
     * @param vertex
     *           the vertex to add
     * @return if the vertex was new (and this instance was updated)
     */
    boolean addVertex(VertexType vertex);
 
    /**
     * Removes a given vertex from this instance.
     * 
     * @param vertex
     *           the vertex to remove
     * @return the {@link Set} of all {@link Edge}s that were connected to the
     *         remove vertex
     */
    Set<EdgeType> removeVertex(VertexType vertex);
 
    /**
     * Returns the {@link Set} of {@link Edge}s connected to a given vertex.
     * 
     * @param vertex
     *           the vertex to retrieve the {@link Edge}s for
     * @return the {@link Set} of {@link Edge}s connected to the given vertex
     */
    Set<EdgeType> edgesOf(VertexType vertex);
 
    /**
     * Retrieves the {@link Set} of vertices that are connected to a given
     * vertex.
     * 
     * @param vertex
     *           the vertex to get the connected vertices for
     * @return the {@link Set} of vertices that are connected to the given vertex
     */
    Set<VertexType> targetsOf(VertexType vertex);
 
    /**
     * Checks if two given vertices are connected within this instance.
     * <p>
     * If the {@link Edge}s are not {@link DirectedEdge}s, it does not matter,
     * which vertex is named first.
     * 
     * @param source
     *           the source vertex of the connection to check
     * @param target
     *           the target vertex of the connection to check
     * @return if the two given vertices are connected within this instance
     */
    boolean isConnected(VertexType source, VertexType target);
 
    /**
     * Return the {@link Set} of {@link Edge}s that connect two given vertices.
     * 
     * @param source
     *           the source vertex of the {@link Edge}s to retrieve
     * @param target
     *           the target vertex of the {@link Edge}s to retrieve
     * @return the resulting {@link Set} of {@link Edge}s (will be empty if the
     *         vertices are not connected within this instance)
     */
    Set<EdgeType> getEdges(VertexType source, VertexType target);
 
    /**
     * Return the first {@link Edge} that connects two given vertices.
     * 
     * @param source
     *           the source vertex of the {@link Edge} to retrieve
     * @param target
     *           the target vertex of the {@link Edge} to retrieve
     * @return the resulting {@link Edge} or {@code null} if the two given
     *         vertices are not connected within this instance
     */
    EdgeType getEdge(VertexType source, VertexType target);
 
    /**
     * Adds two given vertices to this instance (if not yet contained) and
     * connects them together with an {@link Edge}.
     * <p>
     * <b>Important note:</b> it depends on the concrete {@link Graph}
     * implementation if a new {@link Edge} is created for two already connected
     * vertices or the existing one is reused and returned.
     * 
     * @param source
     *           the source vertex of the new connection
     * @param target
     *           the target vertex of the new connection
     * @return the resulting {@link Edge}
     */
    EdgeType addEdge(VertexType source, VertexType target);
 
    /**
     * A bulk version of {@link #addEdge(Object, Object)} that connects (and
     * optionally adds) a bunch of target vertices to one single source vertex.
     * 
     * @param source
     *           the source vertex of the new connections
     * @param targets
     *           the target vertices of the new connections
    * @return
     */
    Set<EdgeType> addEdges(VertexType source, Collection<? extends VertexType> targets);
 
    EdgeType removeEdge(VertexType source, VertexType target);
 
    Set<EdgeType> removeEdges(VertexType source, VertexType target);
 
    Set<EdgeType> removeEdges(VertexType source, Collection<? extends VertexType> targets);
 }
