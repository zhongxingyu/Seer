 package storage;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentSkipListSet;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import query.QueryPlan;
 import spiderman.Direction;
 import spiderman.Relationship;
 
 /**
  * A graph designed for concurrent access, insertions, and queries. Supports
  * unique and non-unique graphs.
  * 
  * @author varley
  * @see {@link Node}
  */
 public class BasicGraph implements Graph {
 	private final Map<Object, List<Node<?>>> dataIndex;
 	private final Collection<Node<?>> nodes;
 	private final Set<RelationshipHolder> relationships;
 	private final Map<String, Set<RelationshipHolder>> relationshipIndex;
 	private final boolean unique;
 
 	/**
 	 * Constructs a graph designed for concurrent access, insertions, and
 	 * queries. Defaults uniqueness to <code>false</code>
 	 */
 	public BasicGraph() {
 		this(false);
 	}
 
 	/**
 	 * 
 	 * Constructs a graph designed for concurrent access, insertions, and
 	 * queries.
 	 * 
 	 * @param unique
 	 *            A boolean toggle that determines if the graph is to maintain
 	 *            uniqueness in the data that stored in the graph data.
 	 */
 	public BasicGraph(boolean unique) {
 		this.unique = unique;
 		if (unique) {
 			this.nodes = new ConcurrentSkipListSet<Node<?>>();
 		} else {
 			this.nodes = new CopyOnWriteArrayList<Node<?>>();
 		}
 		this.relationships = new ConcurrentSkipListSet<RelationshipHolder>();
 		this.relationshipIndex = new ConcurrentHashMap<String, Set<RelationshipHolder>>(
 				100);
 		this.dataIndex = new ConcurrentHashMap<Object, List<Node<?>>>(100);
 	}
 
 	@Override
 	public final Collection<Node<?>> getNodes() {
 		return Collections.unmodifiableCollection(this.nodes);
 	}
 
 	@Override
 	public final Set<RelationshipHolder> getRelationships() {
 		return Collections.unmodifiableSet(this.relationships);
 	}
 
 	/**
 	 * @throws IllegalStateException
 	 *             If the graph is to be unique, and the parameter was already
 	 *             present in the graph.
 	 */
 	@Override
 	public final <T> Node<T> addNode(final T value) {
 		Node<T> newNode = new Node<T>(value);
 		if (this.unique && this.nodes.contains(newNode)) {
 			throw new IllegalStateException(value.toString()
 					+ " is already in this unique value graph");
 		}
 		this.nodes.add(newNode);
 		List<Node<?>> list = this.dataIndex.get(value);
 		if (list == null) {
 			list = new CopyOnWriteArrayList<Node<?>>();
 		}
 		list.add(newNode);
 		this.dataIndex.put(value, list);
 		return newNode;
 	}
 
 	@Override
 	public final List<Node<?>> getNodes(final Object value) {
 		List<Node<?>> list = this.dataIndex.get(value);
 		if (list == null) {
 			return Collections.emptyList();
 		}
 		return list;
 	}
 
 	@Override
 	public final void addRelationship(final Relationship r,
 			final Direction dir, final Node<?> origin, final Node<?> destination) {
 		if (!this.nodes.contains(origin)) {
 			throw new IllegalArgumentException(
 					"The graph does not contain the specified origin node "
 							+ origin.toString());
 		}
 		if (!this.nodes.contains(destination)) {
 			throw new IllegalArgumentException(
 					"The graph does not contain the specified destination node "
 							+ destination.toString());
 		}
 		RelationshipHolder holder = new RelationshipHolder(r, dir, origin,
 				destination);
 		this.relationships.add(holder);
 		Set<RelationshipHolder> relops = this.relationshipIndex.get(r
 				.identifier());
 		if (relops == null) {
 			relops = new ConcurrentSkipListSet<RelationshipHolder>();
 		}
 		relops.add(holder);
 		this.relationshipIndex.put(r.identifier(), relops);
 	}
 
 	@Override
 	public void removeNode(final Node<?> node) {
 		// Remove relationships that contain this node
 		Collection<RelationshipHolder> rels = new LinkedList<RelationshipHolder>();
 		rels.addAll(node.entranceRelations);
 		rels.addAll(node.exitRelations);
 		for (RelationshipHolder h : rels) {
 			// Remove from relationship index
 			String rel = h.getRelationship().identifier();
 			Set<RelationshipHolder> list = this.relationshipIndex.get(rel);
 			list.remove(h);
 			// Remove from both nodes
 			h.getOrigin().removeRelationship(h);
 			h.getDestination().removeRelationship(h);
 			// Remove from set of relationships
 			this.relationships.remove(h);
 		}
 		List<Node<?>> nodes = this.dataIndex.get(node.getData());
 		for (Iterator<Node<?>> it = nodes.iterator(); it.hasNext();) {
 			if (node == it.next()) {
 				it.remove();
 				break;
 			}
 		}
 		// Remove node
 		this.nodes.remove(node);
 	}
 
 	@Override
 	public final void removeRelationship(final Relationship r,
 			final Direction dir, final Node<?> origin, final Node<?> destination) {
 		if (!this.nodes.contains(origin)) {
 			throw new IllegalArgumentException(
 					"The graph does not contain the specified origin node "
 							+ origin.toString());
 		}
 		if (!this.nodes.contains(destination)) {
 			throw new IllegalArgumentException(
 					"The graph does not contain the specified destination node "
 							+ destination.toString());
 		}
 		RelationshipHolder holder = new RelationshipHolder(r, dir, origin,
 				destination);
 		holder.getOrigin().removeRelationship(holder);
 		holder.getDestination().removeRelationship(holder);
 		this.relationshipIndex.get(holder.getRelationship().identifier())
 				.remove(holder);
 		this.relationships.remove(holder);
 	}
 
 	@Override
	public QueryPlan hasRealtion(Relationship r) {
 		Set<Node<?>> list = new HashSet<Node<?>>();
 
 		Set<RelationshipHolder> holders = this.relationshipIndex.get(r.identifier());
 		
 		for(RelationshipHolder holder : holders) {
 			list.add(holder.getDestination());
 			if(holder.getDirection() == Direction.Undirected) {
 				list.add(holder.getOrigin());
 			}
 		}
 
 		return new QueryPlan(list.iterator());
 	}
 }
