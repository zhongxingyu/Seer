 package moca.graphs.edges;
 
 import java.util.ArrayList;
 import java.lang.UnsupportedOperationException;
 import java.util.NoSuchElementException;
 import java.util.Collection;
 
 /**
  * An edge collection implemented by neighbours lists.
  * It supports only double-edgedless graphs, and is not optimized for undirected graphs.
  */
 public class NeighboursLists<E> extends AbstractEdgeCollection<E> implements EdgeCollection<E> {
 
 	public int size() {
 		return _size;
 	}
 
 	public NeighboursLists<E> subset(int idBegin, int idEnd) {
 		NeighboursLists<E> result = new NeighboursLists<E>();
 		int cpt = 0;
 		for (int i = idBegin ; i < idEnd ; i++) {
 			onVertexAdded(i-idBegin);
 			for (int j = 0 ; j < _neighbours.get(i).size() ; j++) {
 				if (_neighbours.get(i).get(j).getIDV() < idEnd) {
 					try {
 						result.add(i-idBegin,j-idBegin,_neighbours.get(i).get(j).getValue()/*.clone()*/);
 					}
 					catch (IllegalEdgeException e) { }
 				}
 			}
 		}
 		return result;
 	}
 
 	public void onVertexAdded(int idV) {
 		NeighbourEdge<E> edge = null;
 		_neighbours.add(idV,new ArrayList<NeighbourEdge<E> >());
 		if (idV < _neighbours.size()-1) {
 			for (int i = 0 ; i < _neighbours.size() ; i++) {
 				for (int j = 0 ; j < _neighbours.get(i).size() ; j++) {
 					edge = _neighbours.get(i).get(j);
 					if (edge.getIDV() >= idV)
 						edge.setIDV(edge.getIDV()+1);
 				}
 			}
 		}
 	}
 
 	public void onVertexRemoved(int idV) {
 		int i;
 		NeighbourEdge<E> edge = null; 
 		_neighbours.remove(idV);
 		for (ArrayList<NeighbourEdge<E> > list : _neighbours) {
 			for (i = 0 ; i < list.size() ; i++) {
 				edge = list.get(i);
 				if (edge.getIDV() > idV) 
 					edge.setIDV(edge.getIDV()-1);
 				else if (edge.getIDV() == idV)
 					list.remove(i);
 			}
 		}
 	}
 
 	public void onVertexContracted(int idU, int idV) {
 		_neighbours.set(idU,_neighbours.get(idV));
 	}
 
 	public E getValue(int idU, int idV) throws NoSuchElementException {
 		for (NeighbourEdge<E> edge : _neighbours.get(idU))
 			if (edge.getIDV() == idV)
 				return edge.getValue();
 		throw new NoSuchElementException();
 	}
 
 	public int getNeighbourAt(int idU, int index) throws NoSuchElementException {
		if ((idU > _neighbours.size()) || (index > _neighbours.get(idU).size()))
 			throw new NoSuchElementException();
 		return _neighbours.get(idU).get(index).getIDV();
 	}
 
 	public void add(int idU, int idV, E value) throws NoSuchElementException, IllegalEdgeException {
 		if ((idU >= _neighbours.size()) || (idV >= _neighbours.size()))
 			throw new NoSuchElementException();
 		if (contains(idU,idV))
 			throw new IllegalEdgeException();
 		_neighbours.get(idU).add(new NeighbourEdge<E>(idV,value));
 		_size++;
 	}
 
 	public void remove(int idU, int idV) throws NoSuchElementException {
 		for (int i = 0 ; i < _neighbours.get(idU).size() ; i++) {
 			if (_neighbours.get(idU).get(i).getIDV() == idV) {
 				_neighbours.get(idU).remove(i);
 				_size--;
 				return;
 			}
 		}
 		throw new NoSuchElementException();
 	}
 
 	public void clear() {
 		_neighbours.clear();
 		_size = 0;
 	}
 
 	public boolean remove(Edge<E> edge) {
 		if (super.remove(edge)) { 
 			return true;
 		}
 		else
 			return false;
 	}
 
 	public java.util.Iterator<Edge<E> > iterator() {
 		return new Iterator(_neighbours);
 	}
 
 	public java.util.Iterator<NeighbourEdge<E> > neighbourIterator(int id) {
 		return _neighbours.get(id).iterator();
 	}
 
 	protected ArrayList<ArrayList<NeighbourEdge<E> > > _neighbours = new ArrayList<ArrayList<NeighbourEdge<E> > >();
 	protected int _size = 0;
 
 	
 	protected class Iterator implements java.util.Iterator<Edge<E> > {
 	
 		public Iterator(ArrayList<ArrayList<NeighbourEdge<E> > > source) {
 			_source = source;
 			_current = 0;
 			_neighbourIndex = -1;
 			processNextEdge();
 		}
 
 		public boolean hasNext() {
 			return (_next != null);
 		}
 
 		public Edge<E> next() throws NoSuchElementException {
 			if (!hasNext())
 				throw new NoSuchElementException();
 			Edge<E> currentEdge = _next;
 			processNextEdge();	
 			return currentEdge;
 		}
 
 		public void remove() throws UnsupportedOperationException {
 			throw new UnsupportedOperationException();
 		}
 
 		protected void processNextEdge() {
 			if (_neighbourIndex < _source.get(_current).size() - 1) {
 				_neighbourIndex++;
 				NeighbourEdge<E> edge = _source.get(_current).get(_neighbourIndex);
 				_next = new Edge<E>(_current,edge.getIDV(),edge.getValue());
 			}
 			else if (_current < _source.size() - 1) {
 				_current++;
 				_neighbourIndex = -1;
 				processNextEdge();
 			}
 			else {
 				_next = null;
 			}
 		}
 
 		private ArrayList<ArrayList<NeighbourEdge<E> > > _source = null;
 		private int _current;
 		private int _neighbourIndex;
 		private Edge<E> _next;
 
 	};
 
 };
 
