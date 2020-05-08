 package list;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 public class LinkedList<T> implements List<T> {
 	
 	private Node<T> first;
 	private Node<T> last;
 	private int size;
 
 	private static class Node<T> {
 		T data;
 		Node<T> next;
 
 		public Node(T data) {
 			this(data, null);
 		}
 
 		public Node(T data, Node<T> next) {
 			this.data = data;
 			this.next = next;
 		}
 	}
 	
 	public LinkedList() {
 		first = null;
 		last = null;
 		size = 0;
 	}
 
 	public boolean add(T elem) {
 		add(size, elem);
 		return true;
 	}
 	 
 	public void add(int i, T elem) {
 		if (i == size) {
 			Node<T> newNode = new Node<T>(elem);
 			if (size != 0) {
 				last.next = newNode;
 			} else {
 				first = newNode;
 			}
 			last = newNode;
 		} else if (i == 0) {
 			first = new Node<T>(elem, first);
 		} else {
 			ensureIndex(i);
 			Node<T> aux = first;
 			for (int pos = 0; pos < i - 1; pos++) {
 				aux = aux.next;
 			}
 			aux.next = new Node<T>(elem, aux.next);
 		}
 		size++;
 	}
 	
 	private void ensureIndex(int i) {
 		if (i < 0 || i >= size()) {
 			throw new IndexOutOfBoundsException();
 		}
 	}
 	 
 	public boolean addAll(Collection<? extends T> collection) {
 		for (T c : collection) {
 			add(c);
 		}
 		return true;
 	}
 	 
 	public boolean addAll(int i, Collection<? extends T> collection) {
 		for (T c : collection) {
 			add(i, c); // In this approach it has to iterate many times to do that.
			i++; // I have to fix it.
 		}
 		return true;
 	}
 	 
 	public void clear() {
 		throw new UnsupportedOperationException("Not yet implemented.");	
 	}
 	 
 	public boolean contains(Object n) {
 		throw new UnsupportedOperationException("Not yet implemented.");	
 	}
 	 
 	public boolean containsAll(Collection<?> collection) {
 		throw new UnsupportedOperationException("Not yet implemented.");	
 	}
 	 
 	public T get(int i) {
 		ensureIndex(i);
 		if (i == size - 1) {
 			return last.data;
 		} else {
 			Node<T> aux = first;
 			for (int pos = 0; pos < i; pos++) {
 				aux = aux.next;
 			}
 			return aux.data;
 		}
 	}
 	 
 	public int indexOf(Object o) {
 		throw new UnsupportedOperationException("Not yet implemented.");
 	}
  
 	public boolean isEmpty() {
 		return size == 0;
 	}
 	 
 	public Iterator<T> iterator() {
 		return new LinkedListIterator();
 	}
 	
 	private class LinkedListIterator implements Iterator<T> {
 		Node<T> current;
 
 		public LinkedListIterator(){
 			current = first;
 		}
 		
 		public boolean hasNext() {
 			return current != null;
 		}
 
 		public T next() {
 			T next = current.data;
 			current = current.next;
 			return next;
 		}
 
 		public void remove() {
 			throw new UnsupportedOperationException();		
 		}		
 	}
 	 
 	public int lastIndexOf(Object o) {
 		throw new UnsupportedOperationException("Not yet implemented.");	
 	}
 	 
 	public ListIterator<T> listIterator() {
 		throw new UnsupportedOperationException("Not yet implemented.");	
 	}
  
 	public ListIterator<T> listIterator(int i) {
 		throw new UnsupportedOperationException("Not yet implemented.");	
 	}
  
 	public boolean remove(Object o) {
 		throw new UnsupportedOperationException("Not yet implemented.");	
 	}
  
 	public T remove(int i) {
 		ensureIndex(i);
 		T toRemove = null;
 		if (i == 0) {
 			toRemove = first.data;
 			first = first.next;
 			if (first == null) {
 				last = null;
 			}
 		} else {
 			Node<T> aux = first;
 			for (int pos = 0; pos < i - 1; pos++) {
 				aux = aux.next;
 			}
 			if (i == (size - 1)) {
 				last = aux;
 				toRemove = (last.next).data;
 				last.next = null;
 			} else {
 				toRemove = (aux.next).data;
 				aux.next = (aux.next).next;
 			}
 		}
 		size--;
 		return toRemove;
 	}
  
 	public boolean removeAll(Collection<?> collection) {
 		for (Object c : collection) {
 			remove(c);
 		}
 		return true;
 	}
 
 	public boolean retainAll(Collection<?> collection) {
 		throw new UnsupportedOperationException("Not yet implemented.");	
 	}
  
 	public T set(int i, T elem) {
 		ensureIndex(i);
 		T previous = null;
 		if (i == size - 1) {
 			previous = last.data;
 			last.data = elem;
 		} else {
 			Node<T> aux = first;
 			for (int pos = 0; pos < i; pos++) {
 				aux = aux.next;
 			}
 			
 			previous = aux.data;
 			aux.data = elem;
 		}
 		return previous;
 	}
 
 	public int size() {
 		return size;
 	}
  
 	public List<T> subList(int x, int y) {
 		throw new UnsupportedOperationException("Not yet implemented.");	
 	}
 
 	public Object[] toArray() {
 		throw new UnsupportedOperationException("Not yet implemented.");
 	}
 
 	public <T> T[] toArray(T[] a) {
 		throw new UnsupportedOperationException("Not yet implemented.");	
 	}
 
 }
