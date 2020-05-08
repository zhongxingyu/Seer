 package storage;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import query.QueryPlan;
 
 import spiderman.Direction;
 import spiderman.Relationship;
 
 /**
  * A graph is the central data structure for spiderman. It represents data in
  * the form of a graph, which is a series of nodes connected by
  * edges/relationships. Both the vertices and the edges can contains arbitrary
  * data.
  * 
  * @author Varley
  * 
  */
 public interface Graph {
 
 	/**
 	 * This returns all the nodes contains within the graph. Implementations
 	 * should take care that the edits to this collection will not edit the
 	 * graph (and thus violate any invariants).
 	 * 
 	 * @return All the nodes in the graph in an unmodifiable collection.
 	 */
 	public Collection<Node<?>> getNodes();
 
 	/**
 	 * This returns all the relationships contains within the graph.
 	 * Implementations should take care that the edits to this collection will
 	 * not edit the graph (and thus violate any invariants).
 	 * 
 	 * @return All the relationships in the graph in an unmodifiable collection.
 	 */
 	public Set<RelationshipHolder> getRelationships();
 
 	/**
 	 * This will add a new node to the graph.
 	 * 
 	 * @param value
 	 *            The node that contains the passed in value.
 	 */
 	public <T> Node<T> addNode(T value);
 
 	/**
 	 * This method is used when the container node is needed, but only the value
 	 * is known. This will return all the nodes that equal the passed in
 	 * parameter as defined by {@link Object#equals(Object)}.
 	 * 
 	 * <p>
 	 * 
 	 * If the graph enforces uniqueness, there should be only one element in the
 	 * returned list.
 	 * 
 	 * <p>
 	 * 
 	 * If there is no container that contains the parameter, an empty list
 	 * should be returned.
 	 * 
 	 * @param value
 	 *            The raw data that corresponds to a list of containing nodes in
 	 *            the Graph
 	 * 
 	 * @return The list of nodes that contain the value passed in.
 	 */
 	public List<Node<?>> getNodes(Object value);
 
 	/**
 	 * This will add a relationship to the graph. The relationship will go from
 	 * the specified origin to the destination. Queries should be aware of
 	 * undirected edges as implementations make no guarantee of placing an
 	 * undirected edge in the outgoing and incoming list of edges.
 	 * 
 	 * <p>
 	 * 
 	 * If either the origin or destination are not in the graph, this method
 	 * should throw an {@link IllegalArgumentException}.
 	 * 
 	 * @param r
 	 *            The relationship to store.
 	 * @param dir
 	 *            The direction the relationship is in.
 	 * @param origin
 	 *            The origin node.
 	 * @param destination
 	 *            The destination node.
 	 * 
 	 * @thorws IllegalArgumentException If the origin or destination is not in
 	 *         the graph.
 	 */
 	public void addRelationship(Relationship r, Direction dir, Node<?> origin,
 			Node<?> destination);
 
 	/**
 	 * This will remove the node from the graph. As a side effect, all
 	 * relationships that the parameter is a part of will be removed from the
 	 * graph and the node on the other side of the edge.
 	 * 
 	 * @param node
 	 *            The node to be removed.
 	 */
 	public void removeNode(Node<?> node);
 
 	/**
 	 * This will remove the specified relationship from the graph and from the
 	 * adjacency lists of both nodes.
 	 * 
 	 * <p>
 	 * 
 	 * If the specified relationship is not present, implementations have the
 	 * option to fail silently without modifying the graph or to throw an
 	 * exception. Implementations that throw an exception should document this
 	 * clearly.
 	 * 
 	 * @param r
 	 *            The relationship to store.
 	 * @param dir
 	 *            The direction the relationship is in.
 	 * @param origin
 	 *            The origin node.
 	 * @param destination
 	 *            The destination node.
 	 */
 	public void removeRelationship(Relationship r, Direction dir,
 			Node<?> origin, Node<?> destination);
 
 	/**
 	 * 
 	 * This will return in the query plan all nodes that are contained in the
 	 * parameters. If the direction specified is directed, only the destination
 	 * node is included in the result.
 	 * 
 	 * @param r
 	 *            The relationship that is wanted
 	 * @return Returns a plan that contains all nodes that fit the parameters
 	 *         specified.
 	 */
	public QueryPlan hasRelation(Relationship r);
 
 }
