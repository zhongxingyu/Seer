 // Released under GPLv2 or later. See http://www.gnu.org/ for details.
 package tags.proto;
 
 import tags.util.Union.U2;
 import tags.util.Maps.U2Map;
 import java.util.List;
 import java.util.Set;
 import java.util.Map;
 
 /**
 ** DOCUMENT.
 **
 ** @param <T> Type of tag
 ** @param <A> Type of address
 ** @param <W> Type of arc-attribute
 */
 public interface AddressScheme<T, A, W> {
 
 	/**
 	** Whether the scheme is complete for the backing graph. If this is {@code
 	** true}, then further information could be extracted by completing a tag
 	** in the backing graph (ie. loading it and all its out-arcs, out-nodes).
 	*/
 	public boolean isIncomplete();
 
 	/**
 	** Sets this address scheme as being incomplete.
 	*/
 	public void setIncomplete();
 
 	/**
 	** Return the seed tag.
 	*/
 	public T seedTag();
 
 	/**
 	** Returns the tag most relevant to the seed tag, from the given iterable.
 	**
 	** @throws IllegalArgumentException if the iterable contains a tag unknown
 	**         to this address scheme
 	** @throws NullPointerException if {@code tags} is {@code null}.
 	*/
 	public T getMostRelevant(Iterable<T> tags);
 
 	/**
 	** Returns the set of tags contained within this scheme.
 	*/
 	public Set<T> tagSet();
 
 	/**
 	** Returns a map of tags to their arc-attributes, w.r.t. the seed tag.
 	*/
 	public Map<T, W> arcAttrMap();
 
 	/**
 	** Returns a list of nodes in ascending order from their shortest distance
 	** to the seed tag.
 	*/
 	public List<U2<T, A>> nodeList();
 
 	/**
 	** Returns a map of nodes to their orderings in the shortest-distance list.
 	*/
 	public U2Map<T, A, Integer> indexMap();
 
 	/**
 	** Returns a map of nodes to the shortest path between it and the seed tag.
 	** The path starts from the seed tag, and does not contain the node itself.
 	*/
 	public U2Map<T, A, List<T>> pathMap();
 
 	/**
 	** Returns a map of nodes to the set of all its ancestors.
 	*/
 	public U2Map<T, A, Set<T>> ancestorMap();
 
 	/**
 	** Attaches the given node to the address scheme, with a set of incoming
 	** neighbours. Only nodes already in the scheme (ie. nearer to the seed)
 	** will be added as incoming neighbours; the rest will be filtered out.
 	**
 	** It is '''assumed''' that nodes are added in shortest-path order. It is
 	** up to the caller to ensure that this holds.
 	**
 	** @param node The node to push onto this address scheme
 	** @param parent The immediate parent of the node in the shortest-path tree
 	** @param inc The incoming neighbours of the node
 	** @throws IllegalArgumentException if the scheme or {@code inc} contains
 	**         {@code tag}, or if either of them do not contain {@code parent}.
 	** @throws NullPointerException if {@code parent} is {@code null}.
 	*/
 	public void pushNode(U2<T, A> node, T parent, Set<T> inc);
 
 	/**
	** @see #pushNode(Union.U2, Object, Set)
 	*/
 	public void pushNodeT(T tag, T parent, Set<T> inc);
 
 	/**
	** @see #pushNode(Union.U2, Object, Set)
 	*/
 	public void pushNodeG(A addr, T parent, Set<T> inc);
 
 }
