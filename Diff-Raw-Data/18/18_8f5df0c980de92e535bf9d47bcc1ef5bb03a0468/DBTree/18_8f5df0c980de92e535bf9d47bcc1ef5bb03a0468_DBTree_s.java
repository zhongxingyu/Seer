 package excitedmind;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.logging.Logger;
 
 import prefuse.util.PrefuseLib;
 
 import com.orientechnologies.orient.core.db.record.ORecordLazyList;
 import com.tinkerpop.blueprints.Direction;
 import com.tinkerpop.blueprints.Edge;
 import com.tinkerpop.blueprints.Element;
 import com.tinkerpop.blueprints.Features;
 import com.tinkerpop.blueprints.Graph;
 import com.tinkerpop.blueprints.GraphQuery;
 import com.tinkerpop.blueprints.Index;
 import com.tinkerpop.blueprints.Vertex;
 import com.tinkerpop.blueprints.impls.orient.OrientGraph;
 
 public class DBTree implements Graph {
     Logger m_logger = Logger.getLogger(this.getClass().getName());
 
 	public final static String EDGE_TYPE_PROP_NAME = PrefuseLib.FIELD_PREFIX + "edgeType";
 	public final static String CHILD_EDGES_PROP_NAME = PrefuseLib.FIELD_PREFIX + "childEdges";
     public final static String INHERIT_PATH_PROP_NAME = PrefuseLib.FIELD_PREFIX + "inheritPath";
 
 	private final static String ROOT_INDEX_NAME = PrefuseLib.FIELD_PREFIX + "rootIndex";
 	private final static String ROOT_KEY_NAME = PrefuseLib.FIELD_PREFIX + "root";
 
 	private final static String TRASH_INDEX_NAME = PrefuseLib.FIELD_PREFIX + "trashIndex";
 	private final static String TRASH_KEY_NAME = PrefuseLib.FIELD_PREFIX + "vertex";
 
 	private final static String SAVED_PARENT_ID_PROP_NAME = PrefuseLib.FIELD_PREFIX + "parent";
 	private final static String SAVED_POS_PROP_NAME = PrefuseLib.FIELD_PREFIX + "pos";
 	public final static String SAVED_REFERER_INFO_PROP_NAME = PrefuseLib.FIELD_PREFIX + "referers";
 
 	enum EdgeType { INCLUDE, REFERENCE};
 
     enum InheritDirection {SELF, LINEAL_SIBLING,  COLLATERAL_SIBLING,
                           LINEAL_ANCESTOR, COLLATERAL_ANCESTOR,
                           LINEAL_DESCENDANT, COLLATERAL_DESCENDANT};
 
     protected static final int ADDING_EDGE_END = 0x7FFFFFFF;
 
 	public OrientGraph m_graph;
 
 	private Index<Vertex> m_rootIndex;
 	private Index<Vertex> m_trashIndex;
 
 	String m_path;
 
 	DBTree (String path)
 	{
 		m_graph = new OrientGraph (path);
 		m_path = path;
 		createIndices ();
 	}
 
 	@Override
 	public Edge addEdge(Object arg0, Vertex arg1, Vertex arg2, String arg3) {
 		//disable the method, to preserve a tree structure
 		assert (false);
 		return m_graph.addEdge(arg0, arg1, arg2, arg3);
 	}
 	
 	@Override
 	public Vertex addVertex(Object arg0) {
 		Vertex vertex = m_graph.addVertex(arg0);
         m_graph.commit();
         return m_graph.getVertex(vertex.getId());
 	}
 	@Override
 	public Edge getEdge(Object arg0) {
 		return m_graph.getEdge(arg0);
 	}
 	@Override
 	public Iterable<Edge> getEdges() {
 		return m_graph.getEdges();
 	}
 	@Override
 	public Iterable<Edge> getEdges(String arg0, Object arg1) {
 		return m_graph.getEdges(arg0, arg1);
 	}
 	@Override
 	public Features getFeatures() {
 		return m_graph.getFeatures();
 	}
 	@Override
 	public Vertex getVertex(Object arg0) {
 		return m_graph.getVertex(arg0);
 	}
 	@Override
 	public Iterable<Vertex> getVertices() {
 		return m_graph.getVertices();
 	}
 	@Override
 	public Iterable<Vertex> getVertices(String arg0, Object arg1) {
 		return m_graph.getVertices(arg0, arg1);
 	}
 	@Override
 	public void removeEdge(Edge arg0) {
 		//disable the method, to preserve a tree structure
 		assert (false);
 		m_graph.removeEdge(arg0);
 	}
 	@Override
 	public void removeVertex(Vertex arg0) {
 		//disable the method, to preserve a tree structure
 		assert (false);
 		m_graph.removeVertex(arg0);
 	}
 	@Override
 	public void shutdown() {
 		cleanTrash ();
 		m_graph.shutdown();
 	}
 	
 	public void commit() {
 		m_graph.commit();
 	}
 	
 	private void createIndices ()
 	{
 		m_rootIndex = m_graph.getIndex (ROOT_INDEX_NAME, Vertex.class);
 		
 		if (m_rootIndex == null)
 		{
 			m_rootIndex = m_graph.createIndex(ROOT_INDEX_NAME, Vertex.class);
 		}
 		
 		m_trashIndex = m_graph.getIndex (TRASH_INDEX_NAME, Vertex.class);
 		
 		if (m_trashIndex == null)
 		{
 			m_trashIndex = m_graph.createIndex(TRASH_INDEX_NAME, Vertex.class);
 		}
 	}
 	
 	public Vertex addRoot ()
 	{
 		Vertex root = addVertex(null);
 		m_rootIndex.put(ROOT_KEY_NAME, ROOT_KEY_NAME, root);
         root.setProperty(INHERIT_PATH_PROP_NAME, new ArrayList ());
 		Object rootId = root.getId();
 		Object oldId = rootId;
 		m_logger.info(root.getId().toString());
 		root = m_graph.getVertex(rootId);
 		rootId = root.getId();
 		m_logger.info(root.getId().toString());
 		return root;
 	}
 
 	public Vertex getRoot ()
 	{
 		return m_rootIndex.get(ROOT_KEY_NAME, ROOT_KEY_NAME).iterator().next();
 	}
 	
 	public Vertex getEdgeSource (Edge edge)
 	{
 		return edge.getVertex(Direction.OUT);
 	}
 	
 	public Vertex getEdgeTarget (Edge edge)
 	{
 		return edge.getVertex(Direction.IN);
 	}
 	
 	public ArrayList<Object> getContainerProperty (Vertex source, String propName, boolean ifNullCreate)
 	{
 		//Because outEdgeArray must be convert to ORecordLazyList, so its type is not ArrayList.
 		Object outEdgeArray = source.getProperty(propName);
 		if (outEdgeArray == null)
 		{
 			if (ifNullCreate)
 			{
 				outEdgeArray = new ArrayList<Object> ();
 				source.setProperty(propName, outEdgeArray);
 			}
 			else
 			{
 				return null;
 			}
 		}
 		
 		if (outEdgeArray instanceof ORecordLazyList)
 		{
 			ORecordLazyList implArray = (ORecordLazyList)outEdgeArray;
 			implArray.setAutoConvertToRecord(false);
 		}
 		
 		return (ArrayList<Object>)outEdgeArray;
 	}
 	
 	private ArrayList<Object> getEdgeIDsToChildren (Vertex source, boolean ifNullCreate)
 	{
 		return getContainerProperty (source, CHILD_EDGES_PROP_NAME, ifNullCreate);
 	}
 
     private ArrayList<Object> getInheritPath (Vertex source, boolean ifNullCreate)
     {
         return getContainerProperty (source, INHERIT_PATH_PROP_NAME, ifNullCreate);
     }
 
     public InheritDirection getInheritDirection(ArrayList fromInheritPath, ArrayList toInheritPath)
     {
         int fromGeneration = fromInheritPath.size();
         int toGeneration = toInheritPath.size();
 
         int i;
 
         for (i=0; i<fromGeneration && i<toGeneration; i++)
         {
             if (! fromInheritPath.get(i).equals(toInheritPath.get(i)))
             {
                 break;
             }
         }
 
         if (fromGeneration == toGeneration) {
             if (i==fromGeneration)
                 return InheritDirection.LINEAL_SIBLING;
             else
                 return InheritDirection.COLLATERAL_SIBLING;
         } else if (fromGeneration < toGeneration) {
             if (i==fromGeneration)
                return InheritDirection.LINEAL_ANCESTOR;
             else
                return InheritDirection.COLLATERAL_ANCESTOR;
 
         } else {
             if (i==toGeneration)
                return InheritDirection.LINEAL_DESCENDANT;
             else
                return InheritDirection.COLLATERAL_DESCENDANT;
         }
 
     }
 
     public InheritDirection getInheritRelation (Vertex from, Vertex to)
     {
         if (from.getId() == to.getId()) {
             return InheritDirection.SELF;
         }
 
         ArrayList fromInheritPath = getInheritPath(from, true);
         ArrayList toInheritPath = getInheritPath(to, true);
 
         return getInheritDirection(fromInheritPath, toInheritPath);
 
     }
 	
 	public EdgeType getEdgeType (Edge edge)
 	{
 		return EdgeType.values()[(Integer)edge.getProperty(EDGE_TYPE_PROP_NAME)];
 	}
 	
 	private Edge addEdge (Vertex source, Vertex target, int pos, EdgeType edgeType)
 	{
 		//the label should not be null or "", 
 		//it is related to the implement of blueprints
 		Edge edge = m_graph.addEdge(null, source, target, "a");
 
 		edge.setProperty(EDGE_TYPE_PROP_NAME, edgeType.ordinal()); 
 		
 		//to make the edge'id is to local db
 		commit ();
 
         edge = m_graph.getEdge(edge.getId());
 		
 		ArrayList<Object> outEdgeArray = getEdgeIDsToChildren(source, true);
 		
 		if (pos == ADDING_EDGE_END || pos >= outEdgeArray.size())
 		{
 			outEdgeArray.add(edge.getId());
 		}
 		else
 		{
 			outEdgeArray.add(pos, edge.getId());
 		}
 		
 		//NOTICE: the container property must be reset to Vertex.
 		//If not, the last item will not be save to db.
 		//it is the bug of blueprints or orientdb
 		
 		source.setProperty(CHILD_EDGES_PROP_NAME, outEdgeArray);
 		commit ();
 		return edge;
 	}
 	
 	//only addRefEdge and removeRefEdge is public
 	
 	public Edge addRefEdge (Vertex referer, Vertex referee, int pos)
 	{
 		assert (referer.getId() != referee.getId());
 		return addEdge (referer, referee, pos, EdgeType.REFERENCE);
 	}
 
     private void removeEdge (Vertex source, int pos, EdgeType assert_type)
     {
         ArrayList<Object> outEdgeArray = getEdgeIDsToChildren(source, false);
         Object edgeId = outEdgeArray.get(pos);
 
         Edge edge = m_graph.getEdge(edgeId);
         assert (getEdgeType(edge) == assert_type);
 
         m_graph.removeEdge(m_graph.getEdge(edgeId));
 
         outEdgeArray.remove(pos);
         //NOTICE: the container property must be reset to Vertex.
         //If not, the last item will not be save to db.
         //it is the bug of blueprints or orientdb
         source.setProperty(CHILD_EDGES_PROP_NAME, outEdgeArray);
     }
 
 	public void removeRefEdge (Vertex source, int pos)
 	{
         removeEdge(source, pos, EdgeType.REFERENCE);
 	}
 
     public Edge getEdge (Vertex source, int pos)
 	{
 		ArrayList<Object> childEdgeArray = getEdgeIDsToChildren(source, false);
 		
 		if (childEdgeArray == null)
 		{
 			return null;
 		}
 		else
 		{
 			return getEdge(childEdgeArray.get(pos));
 		}
 	}
 
 	public class EdgeVertex {
 		final public Vertex m_vertex;
 		final public Edge m_edge;
 		
 		public EdgeVertex(Edge edge, Vertex vertex) {
 			m_vertex = vertex;
 			m_edge = edge;
 		}
 	};
 	
 	public EdgeVertex addChild (Vertex parent, int pos)
 	{
 		Vertex child = addVertex(null);
 		Edge edge = addEdge(parent, child, pos, EdgeType.INCLUDE);
 
         ArrayList<Object> inheritPath = new ArrayList<Object> ();
         ArrayList<Object> parentInheritPath = getInheritPath(parent, false);
 
         if (parentInheritPath != null)
         {
             inheritPath.addAll(parentInheritPath);
         }
         inheritPath.add(parent.getId());
 
         child.setProperty(INHERIT_PATH_PROP_NAME, inheritPath);
 
 		return new EdgeVertex(edge, child);
 	}
 
 	public EdgeVertex getChildOrReferee(Vertex parent, int pos)
 	{
 		Edge edge = getEdge(parent, pos);
 		if (edge == null)
 		{
 			return null;
 		}
 		else
 		{
 			Vertex child = getEdgeTarget(edge);
 			return new EdgeVertex(edge, child);
 		}
 	}
 	
 	public ArrayList<EdgeVertex> getChildrenAndReferees(Vertex parent)
 	{
 		ArrayList<Object> edgeIDsToChildren = getEdgeIDsToChildren(parent, false);
 		
 		if (edgeIDsToChildren == null)
 			return null;
 		
 		ArrayList<EdgeVertex> children = new ArrayList<EdgeVertex>();
 
 		children.ensureCapacity(edgeIDsToChildren.size());
 
 		for (Object edgeId : edgeIDsToChildren)
 		{
 			Edge edgeToChild = getEdge(edgeId);
 			Vertex child = getEdgeTarget(edgeToChild);
 			children.add(new EdgeVertex(edgeToChild, child));
 		}
 
 		return children;
 	}
 	
 	public EdgeVertex getParent(Vertex vertex)
 	{
 		Iterator<Edge> edgeIterator = vertex.getEdges(Direction.IN).iterator();
 		Edge parentToVertex = null;
 		
 		while (edgeIterator.hasNext())
 		{
 			parentToVertex = edgeIterator.next();
 			
 			if (getEdgeType(parentToVertex) == EdgeType.INCLUDE)
 				break;
 		}
 
 		if (parentToVertex == null) {
 			return null;
 			
 		} else {
 			Vertex parent = getEdgeSource(parentToVertex);
 			return new EdgeVertex(parentToVertex, parent);
 		}
 	}
 
     public EdgeVertex moveChild (Vertex oldParent, int oldPos, Vertex newParent, int newPos)
     {
         Vertex child = getChildOrReferee(oldParent, oldPos).m_vertex;
         removeEdge (oldParent, oldPos, EdgeType.INCLUDE);
         return new EdgeVertex(addEdge(newParent, child, newPos, EdgeType.INCLUDE), child);
     }
 
     public void changeChildPos (Vertex parent, int oldPos, int newPos)
     {
         if (oldPos == newPos)
             return;
 
         ArrayList<Object> outEdgeArray = getEdgeIDsToChildren(parent, true);
         Object edgeId = outEdgeArray.remove(oldPos);
         outEdgeArray.add(newPos, edgeId);
         parent.setProperty(CHILD_EDGES_PROP_NAME, outEdgeArray);
     }
 
 
 
     public ArrayList<EdgeVertex> getReferers(Vertex referee)
 	{
 		Iterator<Edge> edgeIterator = referee.getEdges(Direction.IN).iterator();
 		Edge refEdge = null;
 		ArrayList<EdgeVertex> refererArray = new ArrayList<EdgeVertex> ();
 		
 		
 		while (edgeIterator.hasNext())
 		{
 			refEdge = edgeIterator.next();
 			
 			if (getEdgeType(refEdge) == EdgeType.REFERENCE)
 			{
 				Vertex referer = getEdgeSource(refEdge);
 				refererArray.add(new EdgeVertex(refEdge, referer));
 			}
 		}
 		
 		return refererArray.size() == 0 ? null : refererArray;
 	}
 	
 	private interface Processor 
 	{
 		//return: true: continue deeper, false stop
 	 	abstract public boolean run (Vertex vertex, int level);
 	}
 	
 	private void deepTraverse (Vertex vertex, Processor proc, int level)
 	{
 		if (proc.run(vertex, level))
 		{
 			ArrayList<EdgeVertex> children = getChildrenAndReferees(vertex);
 			
 			if (children != null)
 			{
 
 				for (EdgeVertex child : children)
 				{
 					if (getEdgeType(child.m_edge) == EdgeType.INCLUDE)
 					{
 						deepTraverse(child.m_vertex, proc, level+1);
 					}
 				}
 			}
 		}
 	}
 	
 	private void deepTraverse (Vertex vertex, Processor proc)
 	{
 		deepTraverse (vertex, proc, 0);
 	}
 	
 	//remove vertex, the children append to 
 	public static class RefLinkInfo {
 		final Object m_referer;
 		final Object m_referee;
 		final int m_pos;
 		
 		RefLinkInfo (Object referer, Object referee, int pos)
 		{
 			m_referer = referer;
 			m_referee = referee;
 			m_pos = pos;
 		}
 	}
 	
 	//return the removed vertex
 	public Vertex trashSubTree(Vertex parent, int pos)
 	{
 		assert (parent != getRoot());
 		
 		EdgeVertex edgeVertex = getChildOrReferee(parent, pos);
 		
 		assert (getEdgeType(edgeVertex.m_edge) == EdgeType.INCLUDE);
 		
 		Vertex removedVertex = edgeVertex.m_vertex;
 
         //collect the nodes and edges which refer the removed treee
         final ArrayList<RefLinkInfo> refLinkInfos = new ArrayList<RefLinkInfo> ();
 		deepTraverse(removedVertex, new Processor() {
 			
 			public boolean run(Vertex vertex, int level) {
 				ArrayList<EdgeVertex> referers = getReferers(vertex);
 				
 				if (referers != null)
 				{
 					for (EdgeVertex referer : referers)
 					{
 						ArrayList<Object> edgeArray = getEdgeIDsToChildren(referer.m_vertex, false);
 						int edgeIndex = edgeArray.indexOf(referer.m_edge.getId());
 
 						refLinkInfos.add(new RefLinkInfo(referer.m_vertex.getId(), vertex.getId(), edgeIndex));
 
 						removeRefEdge(referer.m_vertex, edgeIndex);
 					}
 				}
 				
 				return true;
 			}
 		});
 		
 		removedVertex.setProperty(SAVED_PARENT_ID_PROP_NAME, parent.getId());
 		removedVertex.setProperty(SAVED_POS_PROP_NAME, pos);
 		removedVertex.setProperty(SAVED_REFERER_INFO_PROP_NAME, refLinkInfos);
 
         removeEdge(parent, pos, EdgeType.INCLUDE);
 
 		m_trashIndex.put(TRASH_KEY_NAME, TRASH_KEY_NAME, removedVertex);
 		
 		commit ();
 		
 		return removedVertex;
 	}
 
     public static class TrashedTreeContext {
         Object m_parentId;
         int m_pos;
         ArrayList<RefLinkInfo> m_refLinkInfos;
 
         TrashedTreeContext (Object parentId, int pos, ArrayList<RefLinkInfo> refLinkInfos)
         {
             m_parentId = parentId;
             m_pos = pos;
             m_refLinkInfos = refLinkInfos;
         }
     }
 
     public TrashedTreeContext getTrashedTreeContext (Vertex vertex)
     {
         Object parentId = vertex.getProperty(SAVED_PARENT_ID_PROP_NAME);
         int pos = (Integer)vertex.getProperty(SAVED_POS_PROP_NAME);
         ArrayList<Object> refLinkInfos = getContainerProperty (vertex, SAVED_REFERER_INFO_PROP_NAME, false);
 
         assert (parentId != null);
 
         if (refLinkInfos == null) {
             return new TrashedTreeContext (parentId, pos, null);
 
         } else {
             ArrayList<RefLinkInfo> new_array = new ArrayList<RefLinkInfo>();
             for (Object obj : refLinkInfos)
             {
                 RefLinkInfo refLinkInfo = (RefLinkInfo) obj;
                 new_array.add(refLinkInfo);
             }
 
             return new TrashedTreeContext (parentId, pos, new_array);
         }
 
     }
 	
 	//return parent vertex, and 
 	public EdgeVertex restoreTrashedSubTree(Vertex vertex)
 	{
 		Object parentId = vertex.getProperty(SAVED_PARENT_ID_PROP_NAME);
 		int pos = (Integer)vertex.getProperty(SAVED_POS_PROP_NAME);
 		ArrayList<Object> refLinkInfos = getContainerProperty (vertex, SAVED_REFERER_INFO_PROP_NAME, false);
 		
 		Vertex parent = getVertex(parentId);
 		Edge edge = addEdge(parent, vertex, pos, EdgeType.INCLUDE);
 		
 		if (refLinkInfos != null)
 		{
 			for (Object obj : refLinkInfos)
 			{
 				RefLinkInfo refLinkInfo = (RefLinkInfo) obj;
 				addRefEdge(getVertex(refLinkInfo.m_referer), 
 						getVertex(refLinkInfo.m_referee), 
 						refLinkInfo.m_pos);
 			}
 		}
 		
 		vertex.removeProperty(SAVED_PARENT_ID_PROP_NAME);
 		vertex.removeProperty(SAVED_POS_PROP_NAME);
 		vertex.removeProperty(SAVED_REFERER_INFO_PROP_NAME);
 
         m_trashIndex.remove(TRASH_KEY_NAME, TRASH_KEY_NAME, vertex);
         commit ();
 		return new EdgeVertex(edge, parent);
 	}
 	
 	private void cleanTrash ()
 	{
         //TODO
 		
 	}
 	
 	
 	private void copyProperty(Element from, Element to)
 	{
 		for (String key : from.getPropertyKeys())
 		{
 			if (key != CHILD_EDGES_PROP_NAME)
 			{
 				to.setProperty(key, from.getProperty(key));
 			}
 		}
 		
 	}
 
 	public GraphQuery query() {
 		// TODO Auto-generated method stub
 		return m_graph.query();
 	}
 	
 }
