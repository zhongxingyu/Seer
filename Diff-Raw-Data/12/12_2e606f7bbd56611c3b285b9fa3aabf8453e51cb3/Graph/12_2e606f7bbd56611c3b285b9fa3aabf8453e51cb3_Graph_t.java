 package moca.graphs;
 
 import moca.operators.OperatorPlus1T;
 
 import moca.lists.LinkedList;
 import moca.lists.Lifo;
 import moca.lists.Fifo;
 
 import moca.graphs.vertices.Vertex;
 import moca.graphs.vertices.VertexCollection;
 import moca.graphs.vertices.VertexBinaryFunction;
 import moca.graphs.vertices.ParentFunction;
 import moca.graphs.edges.Edge;
 import moca.graphs.edges.NeighbourEdge;
 import moca.graphs.edges.EdgeCollection;
 import moca.graphs.edges.IllegalEdgeException;
 
 
 import java.util.Collection;
 import java.lang.Iterable;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.lang.UnsupportedOperationException;
 import java.util.ArrayList;
 import java.util.PriorityQueue;
 import java.util.Comparator;
 
 
 /**
  * Generic graph class.
  * It is instantiated by giving two parameters : the vertex collection (empty if possible), and the edge collection (same).
  * The copy constructor may be overriden to provide more efficient copy for specific graphs.
  */
 public class Graph<V,E> implements Iterable<V> {
 
 	/* CONSTRUCTORS */
 
 	public Graph(VertexCollection<V> vertices, EdgeCollection<E> edges) throws IllegalConstructionException {
 		if ((vertices == null) || (edges == null))
 			throw new IllegalConstructionException();
 		_vertices = vertices;
 		_edges = edges;
 	}
 
 	public Graph(Graph<V,E> g) throws IllegalConstructionException {
 		Graph<V,E> gclone = g.clone();
 		this._vertices = gclone._vertices;
 		this._edges = gclone._edges;
 	}
 
 	public void clear() {
 		_vertices.clear();
 		_edges.clear();
 	}
 	
 	public Graph<V,E> clone() {
 		try {
 			VertexCollection<V> vertices = _vertices.getClass().newInstance();
 			EdgeCollection<E> edges = _edges.getClass().newInstance();
 			Graph<V,E> graph = new Graph<V,E>(vertices,edges);
 			for (V v : this)
 				graph.addVertex(v);
 			for (Iterator<Edge<E> > e = edgeIterator() ; e.hasNext() ; ) {
 				Edge<E> edge = e.next();
 				graph.addEdge(edge.getIDU(),edge.getIDV(),edge.getValue());
 			}
 			return graph;
 		}
 		catch (Exception e) {
 			return null;
 		}
 	}			
 
 	/**
 	 * @return A subgraph composed by the vertices between idBegin (included) and idEnd (not included) and all the remaining edges 
 	 */
 	public Graph<V,E> subgraph(int idBegin, int idEnd) {
 		try {
 			if (idEnd > getNbVertices())
 				idEnd = getNbVertices();
 			if (idBegin < 0)
 				idBegin = 0;
 			return new Graph<V,E>(_vertices.subset(idBegin,idEnd),_edges.subset(idBegin,idEnd));
 		}
 		catch (IllegalConstructionException e) {
 			return null;
 		}
 	}
 
 	/* VERTICES */
 
 	public int getNbVertices() {
 		return _vertices.size();
 	}
 
 	public Vertex<V> getVertex(int id) throws NoSuchElementException {
 		return _vertices.get(id);
 	}
 
	public Vertex<V> getNeighbour(int vertexID, int index) throws UnsupportedOperationException, NoSuchElementException {
 		return getVertex(_edges.getNeighbourAt(vertexID,index));
 	}
 
	public V getNeighbourValue(int vertexID, int index) throws UnsupportedOperationException, NoSuchElementException {
 		return getNeighbour(vertexID,index).getValue();
 	}
 
 	public void removeVertex(int id) {
 		_edges.onVertexRemoved(id);
 		_vertices.remove(id);
 	}
 
 	
 	public V get(int id) throws NoSuchElementException {
 		return getVertex(id).getValue();
 	}
 
 	public void add(V value) {
 		addVertex(value);
 	}
 
 	public void addVertex(V value) {
 		Vertex<V> v = new Vertex<V>(value);
 		_vertices.add(v);					// this method should change vertex id to match its index
 		_edges.onVertexAdded(v.getID());	// uses directly vertex class ?
 	}
 
 	public void contract(Vertex<V> u, Vertex<V> v) {
 		contract(u.getID(),v.getID());
 	}
 
 	public void contract(int idU, int idV) {
 		if (idU != idV) {
 			NeighbourEdge<E> edge = null;
 			E loopValue = null;
 			for (Iterator<NeighbourEdge<E> > iterator = neighbourIterator(idU) ; iterator.hasNext() ; ) {
 				edge = iterator.next();
 				try {addEdge(idV,edge.getIDV(),edge.getValue());}
 				catch (IllegalEdgeException e) { }
 			}
 			setVertex(idU,getVertex(idV));
 			_edges.onVertexContracted(idU,idV);
 		}
 	}
 
 	public VertexCollection<V> getVertexCollection() {
 		return _vertices;
 	}
 	
 	/** protected because no real check 
 	 * (used by vertex contraction) */
 	protected void setVertex(int i, Vertex<V> u) {
 		_vertices.set(i,u);
 	}
 
 	/* EDGES */
 
 	public int getNbEdges() {
 		return _edges.size();
 	}
 
 	public boolean isEdge(int idU, int idV) {
 		return _edges.contains(idU,idV);
 	}
 
 	public E getEdgeValue(int idU, int idV) throws NoSuchElementException {
 		return _edges.getValue(idU,idV);
 	}
 	
 	public E getEdgeValue(Vertex<V> u, Vertex<V> v) throws NoSuchElementException {
 		return getEdgeValue(u.getID(),v.getID());
 	}
 
 	public Edge<E> getEdge(Vertex<V> u, Vertex<V> v) throws NoSuchElementException {
 		return _edges.get(u.getID(),v.getID());
 	}
 
 	public Edge<E> getEdge(int idU, int idV) throws NoSuchElementException {
 		return _edges.get(idU,idV);
 	}
 
 	public void addNeighbourEdge(int idU, NeighbourEdge<E> edge) throws NoSuchElementException, IllegalEdgeException {
 		addEdge(idU, edge.getIDV(), edge.getValue());
 	}
 
 	public void addEdge(int idU, int idV, E value) throws NoSuchElementException, IllegalEdgeException {
 		if ((idU >= getNbVertices()) || (idV >= getNbVertices()))
 			throw new NoSuchElementException();
 		_edges.add(idU, idV, value);
 	}
 
 	public void addEdge(Vertex<V> u, Vertex<V> v, E value) throws NoSuchElementException, IllegalEdgeException {
 		addEdge(u.getID(),v.getID(),value);
 	}
 
 	public void addEdge(Edge<E> edge) throws NoSuchElementException, IllegalEdgeException {
 		if ((edge.getIDU() >= getNbVertices()) || (edge.getIDV() >= getNbVertices()))
 			throw new NoSuchElementException();
 		_edges.add(edge);
 	}
 
 	public void removeEdge(int idU, int idV) throws NoSuchElementException {
 		if ((idU >= getNbVertices()) || (idV >= getNbVertices()))
 			throw new NoSuchElementException();
 		_edges.remove(idU,idV);
 	}
 
 	public void removeEdge(Vertex<V> u, Vertex<V> v) throws NoSuchElementException {
 		removeEdge(u.getID(),v.getID());
 	}
 
 	public EdgeCollection<E> getEdgeCollection() {
 		return _edges;
 	}
 
 
 	/** ITERATORS */
 
 	public Iterator<V> iterator() {
 		return new VertexValueIterator(_vertices.iterator());
 	}
 
 	public Iterator<Vertex<V> > vertexIterator() {
 		return _vertices.iterator();
 	}
 
 	public Iterator<Edge<E> > edgeIterator() {
 		return _edges.iterator();
 	}
 
 	public Iterator<NeighbourEdge<E> > neighbourIterator(int id) throws NoSuchElementException {
 		if (id > getNbVertices())
 			throw new NoSuchElementException();
 		return _edges.neighbourIterator(id);
 	}
 
 	public WalkIterator BFSIterator() {
 		return new BFSIterator(this,0,null);
 	}
 
 	public WalkIterator BFSIterator(int root) {
 		return new BFSIterator(this,root,null);
 	}
 
 	public WalkIterator BFSIterator(int root, VertexBinaryFunction<V> function) {
 		return new BFSIterator(this,root,function);
 	}
 
 	public WalkIterator DFSIterator() {
 		return new DFSIterator(this,0,null);
 	}
 
 	public WalkIterator DFSIterator(int root) {
 		return new DFSIterator(this,root,null);
 	}
 
 	public WalkIterator DFSIterator(int root, VertexBinaryFunction<V> function) {
 		return new DFSIterator(this,root,function);
 	}
 
 
 	/* ALGORITHMS */
 
 	public boolean isCyclic() {
 		WalkIterator iterator = BFSIterator();
 		while (iterator.hasNext()) {
 			iterator.next();
 			if (iterator.isCyclic())
 				return true;
 		}
 		return false;
 	}
 
 	public boolean isAcyclic() {
 		return !isCyclic();
 	}
 
 	public ParentFunction<V> Dijsktra(int root, E zeroValue, OperatorPlus1T<E> plus, Comparator<E> compareEdge) {
 		ArrayList<Vertex<V> > ends = new ArrayList<Vertex<V> >(0);
 		return AStar(root,
 					 zeroValue,
 					 plus,
 					 compareEdge,
 					 ends,
 					 null);
 	}
 
 	public ParentFunction<V> AStar(int root, 
 								   E zeroValue,
 								   OperatorPlus1T<E> plus,
 								   Comparator<E> compareEdge,
 								   ArrayList<Vertex<V> > ends, 
 								   ArrayList<E> heuristique) {
 		ArrayList<E> weights = new ArrayList<E>(getNbVertices());	// contains the total weights of edges between root and index vertices
 		for (int i = 0 ; i < getNbVertices() ; i++)
 			weights.add(null);
 		weights.set(root,zeroValue);
 		Vertex<V> u = null;
 		NeighbourEdge<E> e = null;
 		Graph<V,E>.AStarVertexComparator compareVertex = new AStarVertexComparator(zeroValue,plus,compareEdge,weights,heuristique);
 		PriorityQueue<Vertex<V> > queue = new PriorityQueue<Vertex<V> >(11,compareVertex);
 		ParentFunction<V> parent = new ParentFunction<V>(getNbVertices());
 		u = getVertex(root);
 		while (u != null) {
 			if (ends.contains(u))
 				return parent;
 			for (Iterator<NeighbourEdge<E> > it = neighbourIterator(u.getID()) ; it.hasNext() ; ) {
 				e = it.next();
 				if ((weights.get(e.getIDV()) == null) || 
 					(compareEdge.compare(plus.exec(weights.get(u.getID()),e.getValue()),
 								  weights.get(e.getIDV())) < 0)) {
 					queue.remove(getVertex(e.getIDV()));
 					weights.set(e.getIDV(),plus.exec(weights.get(u.getID()),e.getValue()));
 					parent.exec(u,getVertex(e.getIDV()));
 					queue.add(getVertex(e.getIDV()));
 				}
 			}
 			u = queue.poll();
 		}
 		return parent;
 	}
 	private class AStarVertexComparator implements Comparator<Vertex<V> > {
 		public AStarVertexComparator(E zeroValue, OperatorPlus1T<E> plus, Comparator<E> compareEdge, ArrayList<E> weights, ArrayList<E> heuristique) {
 			_zeroValue = zeroValue;
 			_weights = weights;
 			_heuristique = heuristique;
 			_plus = plus;
 			_compareEdge = compareEdge;
 		}
 		/* the specific check is here to avoid to find multiple paths.
 		 * furthermore it will reduce the number of iterations needed to find it.
 		 */
 		public int compare(Vertex<V> u, Vertex<V> v) {
 			int result = _compareEdge.compare(get(u.getID()),get(v.getID()));
 			if (result == 0)	// specific check against same values
 				result = _compareEdge.compare(heuristique(u.getID()),heuristique(v.getID()));
 			return result;
 		}
 		public E get(int id) {
 			return _plus.exec(_weights.get(id),heuristique(id));
 		}
 		public E heuristique(int id) {
 			try {
 				return _heuristique.get(id);
 			}
 			catch (Exception e) {
 				return _zeroValue;
 			}
 		}
 		private ArrayList<E> _weights;
 		private ArrayList<E> _heuristique;
 		private E _zeroValue;
 		private OperatorPlus1T<E> _plus;
 		private Comparator<E> _compareEdge;
 	}
 
 
 	protected VertexCollection<V> _vertices = null;
 	protected EdgeCollection<E> _edges = null;
 
 
 	/**
 	 * Graph vertex value iterator nested class.
 	 * It is used to provide a generic value iterator based on the vertex collection one.
 	 * You have just to override the vertexIterator() graph method to make this one works.
 	 */
 	protected class VertexValueIterator implements Iterator<V> {
 		
 		public VertexValueIterator(Iterator<Vertex<V> > iterator) {
 			_iterator = iterator;
 		}
 
 		public boolean hasNext() {
 			return _iterator.hasNext();
 		}
 
 		public V next() {
 			return _iterator.next().getValue();
 		}
 
 		public void remove() throws UnsupportedOperationException {
 			_iterator.remove();
 		}
 
 		private Iterator<Vertex<V> > _iterator;
 
 	}
 
 	/**
 	 * Graph walk iterator nested class.
 	 * This class is a generic walk algorithm iterator, its waiting list been unimplemented, 
 	 * this class cannot be instantiated, use BFSIterator or DFSIterator instead of.
 	 */
 	public abstract class WalkIterator implements Iterator<Vertex<V> > {
 
 		protected WalkIterator(Graph<V,E> source, int rootID, VertexBinaryFunction<V> function) {
 			if (source.getNbVertices() == 0)
 				_current = null;
 			else {
 				_source = source;
 				_current = source.getVertex(rootID);
 				_function = function;
 				_colors = new int[source.getNbVertices()];
 				_neighbourIterators = new ArrayList<Iterator<NeighbourEdge<E> > >(source.getNbVertices());
 				for (int i = 0 ; i < source.getNbVertices() ; i++) {
 					_neighbourIterators.add(_source.neighbourIterator(i));
 					_colors[i] = 0;
 				}
 				_colors[rootID] = 1;
 			}
 		}
 
 		protected WalkIterator(Graph<V,E> source, Vertex<V> root, VertexBinaryFunction<V> function) {
 			if (source.getNbVertices() == 0)
 				_current = null;
 			else {
 				_source = source;
 				_current = root;
 				_function = function;
 				_colors = new int[source.getNbVertices()];
 				_neighbourIterators = new ArrayList<Iterator<NeighbourEdge<E> > >(source.getNbVertices());
 				for (int i = 0 ; i < source.getNbVertices() ; i++) {
 					_neighbourIterators.add(_source.neighbourIterator(i));
 					_colors[i] = 0;
 				}
 				_colors[root.getID()] = 1;
 			}
 		}
 
 
 		public VertexBinaryFunction<V> getFunction() {
 			return _function;
 		}
 
 		public Vertex<V> getCurrent() throws NoSuchElementException {
 			if (_current == null)
 				throw new NoSuchElementException();
 			return _current;
 		}
 
 		public Vertex<V> processNext() {
 			if (!hasNext())
 				throw new NoSuchElementException();
 			Vertex<V> head = _waitingList.get();
 			Vertex<V> neighbour = null;
 			NeighbourEdge<E> edge = null;
 			if (_neighbourIterators.get(head.getID()).hasNext()) {
 				edge = _neighbourIterators.get(head.getID()).next();
 				neighbour = _source.getVertex(edge.getIDV());
 				if (_colors[neighbour.getID()] == 0) {
 					_colors[neighbour.getID()] = 1;
 					_waitingList.put(neighbour);
 					if (_function != null)
 						_function.exec(head,neighbour);
 					return neighbour;
 				}
 				else {
 					_isCyclic = true;
 					return processNext();
 				}
 			}
 			else {
 				_waitingList.pop();
 				return processNext();
 			}
 		}
 
 		public Vertex<V> next() throws NoSuchElementException {
 			if (!hasNext())
 				throw new NoSuchElementException();
 			Vertex<V> old = _current;
 			try {_current = processNext();}
 			catch (NoSuchElementException e) { _current = null; }	// may only be thrown by LinkedList.get()
 			return old;
 		}
 
 		public boolean hasNext() {
 			return (_current != null);
 		}
 
 		public void remove() throws UnsupportedOperationException {
 			throw new UnsupportedOperationException();
 		}
 	
 		public boolean isCyclic() {
 			return _isCyclic;
 		}
 
 		protected Graph<V,E> _source = null;
 		protected Vertex<V> _current = null;
 		protected VertexBinaryFunction<V> _function = null;
 		protected LinkedList<Vertex<V> > _waitingList = null;
 		protected int _colors[];
 		protected ArrayList<Iterator<NeighbourEdge<E> > > _neighbourIterators = null;
 		protected boolean _isCyclic = false;
 	};
 
 	/**
 	 * Graph breadth first search iterator nested class.
 	 * It extends the walk iterator, and provide a FIFO waiting list.
 	 */
 	protected class BFSIterator extends WalkIterator {
 		public BFSIterator(Graph<V,E> source, int root, VertexBinaryFunction<V> function) {
 			super(source,root,function);
 			_waitingList = new Fifo<Vertex<V> >();
 			_waitingList.put(_current);
 		}
 	};
 
 	/**
 	 * Graph depth first search iterator nested class.
 	 * It extends the walk iterator, and provide a LIFO waiting list.
 	 */
 	protected class DFSIterator extends WalkIterator {
 		public DFSIterator(Graph<V,E> source, int root, VertexBinaryFunction<V> function) {
 			super(source,root,function);
 			_waitingList = new Lifo<Vertex<V> >();
 			_waitingList.put(_current);
 		}
 	};
 
 	private StronglyConnectedComponents _stronglyConnectedComponents = null;
 	public void resetStronglyConnectedComponents() {
 		_stronglyConnectedComponents = new StronglyConnectedComponents();
 	}
 	public int getNbComponents() {
 		if ((_stronglyConnectedComponents == null) && (getNbVertices() > 0))
 			resetStronglyConnectedComponents();
 		return _stronglyConnectedComponents.getNbComponents();
 	}
 	public ArrayList<ArrayList<Vertex<V> > > getComponents() {
 		if ((_stronglyConnectedComponents == null) && (getNbVertices() > 0))
 			resetStronglyConnectedComponents();
 		return _stronglyConnectedComponents.getComponents();
 	}
 	public ArrayList<Vertex<V> > getComponent(int i) {
 		if ((_stronglyConnectedComponents == null) && (getNbVertices() > 0))
 			resetStronglyConnectedComponents();
 		return _stronglyConnectedComponents.getComponent(i);
 	}
 	public Vertex<V> getRootComponent(int vertexID) {
 		if ((_stronglyConnectedComponents == null) && (getNbVertices() > 0))
 			resetStronglyConnectedComponents();	
 		return _stronglyConnectedComponents.getRootComponent(vertexID);
 	}
 	public int getComponentID(int vertexID) {
 		return getComponentID(getVertex(vertexID));
 	}
 	public int getComponentID(Vertex<V> v) {
 		if ((_stronglyConnectedComponents == null) && (getNbVertices() > 0))
 			resetStronglyConnectedComponents();	
 		return _stronglyConnectedComponents.getComponentID(v);
 
 	}
 	private class StronglyConnectedComponents {
 		public StronglyConnectedComponents() {
 			init();
 			process();
 		}
 		public int getNbComponents() {
 			return _components.size();
 		}
 		public ArrayList<ArrayList<Vertex<V> > > getComponents() {
 			return _components;
 		}
 		public ArrayList<Vertex<V> > getComponent(int i) {
 			if ((i < 0) || (i >= _components.size()))
 				return null;
 			return _components.get(i);
 		}
 		public Vertex<V> getRootComponent(int vertexID) {
 			if ((vertexID < 0) || (vertexID >= _rootComponent.size()))
 				return null;
 			return _rootComponent.get(vertexID);
 		}
 		public int getComponentID(Vertex<V> v) {
 			Vertex<V> root = getRootComponent(v.getID());
 			for (int i = 0 ; i < _components.size() ; i++) {
 				if (_components.get(i).get(0) == root)
 					return i;
 			}
 			return -1;
 		}
 		protected void init() {
 			_components = new ArrayList<ArrayList<Vertex<V> > >();
 			_rootComponent = new ArrayList<Vertex<V> >(getNbVertices());
 			_prev = new ArrayList<ArrayList<Vertex<V> > >(getNbVertices());
 			_revList = new Fifo<Vertex<V> >();
 			_colors = new int[getNbVertices()];
 			for (int i = 0 ; i < getNbVertices() ; i++) {
 				_rootComponent.add(null);
 				_prev.add(new ArrayList<Vertex<V> >());
 				_colors[i] = WHITE;
 			}
 			for (int i = 0 ; i < getNbVertices() ; i++) {
 				for (Iterator<NeighbourEdge<E> > neighbourIterator = neighbourIterator(i) ; neighbourIterator.hasNext() ;)
 					_prev.get(neighbourIterator.next().getIDV()).add(getVertex(i));
 			}
 		}
 		protected void process() {
 			Vertex<V> current = null;
 			// first walk
 			for (Iterator<Vertex<V> > iterator = vertexIterator() ; iterator.hasNext() ;) {
 				current = iterator.next();
 				if (_colors[current.getID()] == WHITE)
 					firstVisit(current);
 			}
 			// vertex colors reinit for the second walk
 			for (int i = 0 ; i < getNbVertices() ; i++)
 				_colors[i] = WHITE;
 			// second walk
 			for (Iterator<Vertex<V> > iterator = _revList.iterator() ; iterator.hasNext() ; ) {
 				current = iterator.next();
 				if (_colors[current.getID()] == WHITE) {
 					_components.add(new ArrayList<Vertex<V> >());
 					secondVisit(current,current,_components.get(_components.size()-1));
 				}
 			}
 		}
 		protected void firstVisit(Vertex<V> root) {
 			_colors[root.getID()] = BLACK;
 			Vertex<V> current = null;
 			for (Iterator<NeighbourEdge<E> > neighbourIterator = neighbourIterator(root.getID()) ; neighbourIterator.hasNext() ; ) {
 				current = getVertex(neighbourIterator.next().getIDV());
 				if (_colors[current.getID()] == WHITE)
 					firstVisit(current);
 			}
 			_revList.addFirst(root);
 		}
 		protected void secondVisit(Vertex<V> u, Vertex<V> root, ArrayList<Vertex<V> > comp) {
 			Vertex<V> current = null;
 			comp.add(u);
 			_rootComponent.set(u.getID(),root);
 			_colors[u.getID()] = BLACK;
 			for (Iterator<Vertex<V> > iterator = _prev.get(u.getID()).iterator() ; iterator.hasNext() ;) {
 				current = iterator.next();
 				if (_colors[current.getID()] == WHITE)
 					secondVisit(current,root,comp);
 			}
 		}
 		private ArrayList<ArrayList<Vertex<V> > > _components;
 		private ArrayList<Vertex<V> > _rootComponent;
 		private ArrayList<ArrayList<Vertex<V> > > _prev;
 		private Fifo<Vertex<V> > _revList;
 		private int _colors[];
 		private static final int WHITE = 0;
 		private static final int BLACK = 1;
 	}
 
 	public void resetStrongConnectedComponentsGraph() {
 		try {
 			_stronglyConnectedComponentsGraph = new DirectedSimpleGraph<ArrayList<Vertex<V> >, Boolean>();
 			// vertices
 			for (int i = 0 ; i < getNbComponents() ; i++)
 				_stronglyConnectedComponentsGraph.addVertex(getComponent(i));
 			// edges
 			Vertex<V> root = null;
 			int componentID = -1;
 			int size = 0;
 			Vertex<ArrayList<Vertex<V> > > vertex = null;
 			Vertex<V> current = null;
 			int idV = -1;
 			for (Iterator<Vertex<ArrayList<Vertex<V> > > > vertexIterator = _stronglyConnectedComponentsGraph.vertexIterator() ; vertexIterator.hasNext() ; ) {
 				vertex = vertexIterator.next();
 				root = vertex.getValue().get(0);
 				componentID = getComponentID(root);
 				size = vertex.getValue().size();
 				for (int i = 1 ; i < size ; i++) {
 					current = vertex.getValue().get(i);
 					for (Iterator<NeighbourEdge<E> > neighbourIterator = neighbourIterator(current.getID()) ; neighbourIterator.hasNext() ; ) {
 						idV = neighbourIterator.next().getIDV();
 						if (getRootComponent(idV) != root) {
 							// there is an edge from the component to another one
 							try {
 								_stronglyConnectedComponentsGraph.addEdge(vertex.getID(),getComponentID(idV),new Boolean(true));
 							}
 							catch (IllegalEdgeException e) { /* edge already exists */ }
 						}
 					}
 				}
 			}
 		}
 		catch (IllegalConstructionException e) {
 			System.out.println(e);
 			e.printStackTrace();
 		}
 
 	}
 	public DirectedSimpleGraph<ArrayList<Vertex<V> >,Boolean> getStronglyConnectedComponentsGraph() {
 		if ((_stronglyConnectedComponentsGraph == null) && (getNbVertices() > 0))
 			resetStrongConnectedComponentsGraph();
 		return _stronglyConnectedComponentsGraph;
 	}
 	private DirectedSimpleGraph<ArrayList<Vertex<V> >,Boolean> _stronglyConnectedComponentsGraph = null;
 
 
 };
 
