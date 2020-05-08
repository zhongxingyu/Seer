 package logic.extlib;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Observable;
import defs.FormatHelper;
 
 /**
  * A implementation of the Graph interface based on incidence lists (at each vertex a list of 
  * all the edges incident to this vertex are are stored).
  * @author ps
  * 
  * @param <V> the type of the elements stored at the vertices of this graph
  * @param <E> the type of the elements stored at the edges of this graph
  */
 public class IncidenceListGraph<V,E> implements Graph<V, E> {
 
 	private HashSet <ILGVertex> vertices = new HashSet<ILGVertex>();
 	private HashSet<ILGEdge> edges = new HashSet<ILGEdge>();
 	private boolean isDirected = false;
 	
 	public IncidenceListGraph (boolean isDirected){
 		this.isDirected = isDirected;
 	}
 	
 	@Override
 	public Vertex<V> aVertex() {
 		if (numberOfVertices() > 0) return vertices.iterator().next();
 		else return null;
 	}
 
 	@Override
 	public int numberOfVertices() {
 		return vertices.size();
 	}
 
 	@Override
 	public int NumberOfEdges() {
 		return edges.size();
 	}
 
 	@Override
 	public boolean isDirected() {
 		return isDirected;
 	}
 
 	@Override
 	public Iterator<Vertex<V>> vertices() {
 		final Iterator<ILGVertex> it = vertices.iterator();
 		return new Iterator<Vertex<V>>() {
 			
 			@Override
 			public boolean hasNext() {
 				return it.hasNext();
 			}
 
 			@Override
 			public Vertex<V> next() {
 				return it.next();
 			}
 
 			@Override
 			public void remove() {
 				it.remove();
 			}
 		};
 	}
 
 	@Override
 	public Iterator<Edge<E>> edges() {
 		final Iterator<ILGEdge> it = edges.iterator();
 		return new Iterator<Edge<E>>() {
 			
 			@Override
 			public boolean hasNext() {
 				return it.hasNext();
 			}
 
 			@Override
 			public Edge<E> next() {
 				return it.next();
 			}
 
 			@Override
 			public void remove() {
 				it.remove();
 			}
 		};
 	}
 
 	@Override
 	public Iterator<Edge<E>> incidentEdges(Vertex<V> v) {
 		ILGVertex w = (ILGVertex) v;
 		if (w.thisGraph != this) throw new RuntimeException("Invalid Vertex!");
 		final Iterator<Entry<ILGVertex,ILGEdge>> it = w.iEdges.entrySet().iterator();
 		return new Iterator<Edge<E>>() {		
 			@Override
 			public boolean hasNext() {
 				return it.hasNext();
 			}
 
 			@Override
 			public Edge<E> next() {
 				return it.next().getValue();
 			}
 
 			@Override
 			public void remove() {
 				it.remove();
 			}
 		};
 
 	}
 
 	@Override
 	public Iterator<Edge<E>> incidentInEdges(Vertex<V> v) {
 		ILGVertex w = (ILGVertex) v;
 		if (w.thisGraph != this) throw new RuntimeException("Invalid Vertex!");
 		if (! isDirected) throw new RuntimeException("undirected graph!");
 		final Iterator<Entry<ILGVertex,ILGEdge>> it = w.inIEdges.entrySet().iterator();
 		return new Iterator<Edge<E>>() {		
 			@Override
 			public boolean hasNext() {
 				return it.hasNext();
 			}
 
 			@Override
 			public Edge<E> next() {
 				return it.next().getValue();
 			}
 
 			@Override
 			public void remove() {
 				it.remove();
 			}
 		};
 
 	}
 
 	@Override
 	public Iterator<Edge<E>> incidentOutEdges(Vertex<V> v) {
 		ILGVertex w = (ILGVertex) v;
 		if (w.thisGraph != this) throw new RuntimeException("Invalid Vertex!");
 		if (! isDirected) throw new RuntimeException("undirected graph!");
 		final Iterator<Entry<ILGVertex,ILGEdge>> it = w.outIEdges.entrySet().iterator();
 		return new Iterator<Edge<E>>() {		
 			@Override
 			public boolean hasNext() {
 				return it.hasNext();
 			}
 
 			@Override
 			public Edge<E> next() {
 				return it.next().getValue();
 			}
 
 			@Override
 			public void remove() {
 				it.remove();
 			}
 		};
 
 	}
 
 	@Override
 	public int degree(Vertex<V> v) {
 		ILGVertex w = (ILGVertex) v;
 		if (w.thisGraph != this) throw new RuntimeException("Invalid Vertex!");
 		return w.iEdges.size();
 	}
 
 	@Override
 	public int inDegree(Vertex<V> v) {
 		ILGVertex w = (ILGVertex) v;
 		if (w.thisGraph != this) throw new RuntimeException("Invalid Vertex!");
 		if (! isDirected) throw new RuntimeException("undirected graph!");
 		return w.inIEdges.size();
 	}
 
 	@Override
 	public int outDegree(Vertex<V> v) {
 		ILGVertex w = (ILGVertex) v;
 		if (w.thisGraph != this) throw new RuntimeException("Invalid Vertex!");
 		if (! isDirected) throw new RuntimeException("undirected graph!");
 		return w.outIEdges.size();
 	}
 
 	@Override
 	public Vertex<V> origin(Edge<E> e) {
 		ILGEdge iEdge = (ILGEdge) e;
 		if (iEdge.thisGraph != this) throw new RuntimeException("Invalid Edge!");
 		if (! isDirected) throw new RuntimeException("undirected graph!");
 		return iEdge.from;
 	}
 
 	@Override
 	public Vertex<V> destination(Edge<E> e) {
 		ILGEdge iEdge = (ILGEdge) e;
 		if (iEdge.thisGraph != this) throw new RuntimeException("Invalid Edge!");
 		if (! isDirected) throw new RuntimeException("undirected graph!");
 		return iEdge.to;
 	}
 
 	@Override
 	public Vertex<V>[] endVertices(Edge<E> e) {
 		ILGEdge iEdge = (ILGEdge) e;
 		if (iEdge.thisGraph != this) throw new RuntimeException("Invalid Edge!");
 		Vertex <V>  [] v = new Vertex[2];
 		v[0]=iEdge.from; 
 		v[1]=iEdge.to;
 		return (Vertex<V> []) v;
 	}
 
 	@Override
 	public boolean areAdjacent(Vertex<V> v1, Vertex<V> v2) {
 		ILGVertex w1 = (ILGVertex) v1;
 		if (w1.thisGraph != this) throw new RuntimeException("Invalid Vertex!");
 		ILGVertex w2 = (ILGVertex) v2;
 		if (w2.thisGraph != this) throw new RuntimeException("Invalid Vertex!");
 		return (w1.iEdges.get(w2)!=null);
 	}
 
 	@Override
 	public Vertex<V> insertVertex(V elem) {
 		ILGVertex v = new ILGVertex(elem);
 		vertices.add(v);
 		return v;
 	}
 
 	@Override
 	public Edge<E> insertEdge(Vertex<V> from, Vertex<V> to, E elem) {
 		ILGVertex fromV = (ILGVertex) from;
 		if (fromV.thisGraph != this) throw new RuntimeException("Invalid Vertex!");
 		ILGVertex toV = (ILGVertex) to;
 		if (toV.thisGraph != this) throw new RuntimeException("Invalid Vertex!");
 		ILGEdge ed = new ILGEdge(elem, fromV, toV);
 		edges.add(ed);
 		return ed;
 	}
 	
 	@Override
 	public E removeEdge(Edge<E> e) {
 		ILGEdge iEdge = (ILGEdge) e;
 		if (iEdge.thisGraph != this) throw new RuntimeException("Invalid Edge!");
 		
 	    if (isDirected){
 			iEdge.from.outIEdges.remove(iEdge.to);
 			iEdge.to.inIEdges.remove(iEdge.from);	    		    	
 	    }
 	    iEdge.from.iEdges.remove(iEdge.to);
 	    iEdge.to.iEdges.remove(iEdge.from);	    	
 		edges.remove(iEdge);
 		iEdge.thisGraph = null;
 		return iEdge.element();
 	}
 
 	@Override
 	public V removeVertex(Vertex<V> v) {
 		ILGVertex w = (ILGVertex) v;
 		if (w.thisGraph != this) throw new RuntimeException("Invalid Vertex!");
 		// first we remove all edges!
 		Object [] el = new Object[degree(w)];
 		el = w.iEdges.entrySet().toArray();
 		for (Object e:el){
 			removeEdge((ILGEdge)((Map.Entry)e).getValue());
 		}
 		vertices.remove(w);
 		w.thisGraph=null;
 		return w.element;
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString(){
 		StringBuffer sb = new StringBuffer();
 		String con = "---";
 		if (isDirected){
 			sb.append("Type: directed\n");
 			con = "-->";
 		}
 		else sb.append("Type: undirected\n");
 		sb.append("Vertices:\n");
 		Iterator<ILGVertex> it = vertices.iterator();
 		while (it.hasNext()){
 			ILGVertex v = it.next();
 			sb.append("  "+v.toString()+"\n");
 			Iterator<Edge<E>> eit;
 			if (! isDirected){
 				eit = incidentEdges(v);
 				if (eit.hasNext()) sb.append("       Incident Edges:\n");
 				while(eit.hasNext()){
 					Edge<E> e = eit.next();
 					sb.append("       "+e.toString()+"\n");
 				}
 			}
 			else {
 				eit = incidentOutEdges(v);
 				if (eit.hasNext()) sb.append("       outgoing Edges:\n");
 				while(eit.hasNext()){
 					Edge<E> e = eit.next();
 					sb.append("       "+e.toString()+"\n");
 				}
 				eit = incidentInEdges(v);
 				if (eit.hasNext()) sb.append("       incoming Edges:\n");
 				while(eit.hasNext()){
 					Edge<E> e = eit.next();
 					sb.append("       "+e.toString()+"\n");
 				}
 			}
 
 		}
 		sb.append("Edges:\n");
 		Iterator<ILGEdge> eit = edges.iterator();
 		while (eit.hasNext()){
 			ILGEdge ev= eit.next(); 
 			sb.append(ev.from.toString() +con+ev.to.toString()+"  "+ev.toString()+"\n");
 		}
 		return sb.toString();
 	}
 
 	@Override
 	public Vertex<V> opposite(Edge<E> e, Vertex<V> v) {
 		ILGVertex w = (ILGVertex) v;
 		if (w.thisGraph != this) throw new RuntimeException("Invalid Vertex!");
 		ILGEdge iEdge = (ILGEdge) e;
 		if (iEdge.thisGraph != this) throw new RuntimeException("Invalid Edge!");
 		if (iEdge.from==w) return iEdge.to;
 		else if (iEdge.to==w) return iEdge.from;
 		else throw new RuntimeException(w+" is not an endpoint of "+iEdge);
 	}
 
 
 	private class ILGVertex extends IGLDecorable implements Vertex<V>{
 		private V element;
 		private IncidenceListGraph<V,E> thisGraph = IncidenceListGraph.this;
 		private HashMap<ILGVertex,ILGEdge> iEdges;
 		private HashMap<ILGVertex,ILGEdge> inIEdges; 
 		private HashMap<ILGVertex,ILGEdge> outIEdges;
 
 		private ILGVertex(V e){
 			iEdges = new HashMap<ILGVertex,ILGEdge>(4);
 			if (isDirected){
 				inIEdges = new HashMap<ILGVertex,ILGEdge>(4);
 				outIEdges = new HashMap<ILGVertex,ILGEdge>(4);
 			}
 			element=e;
 		}
 		
 		@Override
 		public V element() {
 			return element;
 		}
 
 		public String toString(){
 			if (element == null) return "null";
 			else return element.toString();
 		}
 
 	}
 	
 	private class ILGEdge extends IGLDecorable implements Edge<E>{
 		private E element;
 		private Object thisGraph = IncidenceListGraph.this;
 		private ILGVertex from;
 		private ILGVertex to;
 			
 		ILGEdge(E e, ILGVertex from, ILGVertex to){
 			element=e;
 			this.from = from;
 			this.to = to;
 			if (isDirected){
 				if (from.outIEdges.containsKey(to)) throw new RuntimeException("Parallel edges not allowed!");
 				from.outIEdges.put(to,this);
 				to.inIEdges.put(from, this);				
 			}
 			if (! isDirected && from.iEdges.containsKey(to)) throw new RuntimeException("Parallel edges not allowed!");
 			from.iEdges.put(to,this);
 			to.iEdges.put(from, this);
 		}
 		
 		@Override
 		public E element() {
 			return element;
 		}
 		
 		public String toString(){
 			if (element == null) return "null";
 			else return element.toString();
 		}		
 	}
 	
 	private class IGLDecorable extends Observable implements Decorable {
 		private HashMap<Object,Object> attrs = new HashMap<Object,Object>(2);
 		private final Object DUMMY = new Object();
 		@Override
 		public Object get(Object attr) {
 			Object ret = attrs.get(attr);
 			if (ret==null) throw new RuntimeException("no attribute "+attr);
 			if (ret==DUMMY) ret=null;
 			return ret;
 		}
 
 		@Override
 		public boolean has(Object attr) {
 			Object o = attrs.get(attr);
 			return (o!=null);
 		}
 
 		@Override
 		public void set(Object attr, Object val) {
 			Object value = DUMMY;
 			if (val != null) value = val;
 			attrs.put(attr, value);
 			FormatHelper.updateFormat(this);
 			this.setChanged();
 			this.notifyObservers();
 		}
 
 		@Override
 		public Object destroy(Object attr) {
 			Object ret = attrs.get(attr);
 			attrs.remove(attr);
 			FormatHelper.updateFormat(this);
 			this.setChanged();
 			this.notifyObservers();
 			return ret;
 			
 		}
 
 		@Override
 		public void clearAll() {
 			attrs.clear();
 			this.setChanged();
 			this.notifyObservers();
 		}
 
 	}
 
 } 
