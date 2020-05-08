 package yang.util;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 
 public class NonConcurrentList<E> implements List<E> {
 	
 	private class Node {
 		E elem;
 		public Node prev;
 		public Node next;
 
 		Node(E e, Node p, Node n) {
 			elem = e;
 			prev = p;
 			next = n;
 		}
 	}
 
 	public class NonConcurrentIterator<T> implements ListIterator<T> {
 
		Node current = null;
		Node dummy = new Node(null, null, null);
 
 		public boolean hasNext() {
 			return current.next != null;
 		}
 
 		@SuppressWarnings("unchecked")
 		public T next() {
 			if(current.next==null)
 				return null;
 			current = current.next;
 			return (T) current.elem;
 		}
 
 		public void remove() {
 			NonConcurrentList.this.remove((Node)current, (E)current.elem);
 		}
 		
 		public void prepare(int idx) {
 			current = (Node) first;
 			for (int i = 0; i < idx; i++) current = current.next;
 			
 			dummy.next = current;
			dummy.prev = current;
 			current = dummy;
 		}
 		
 		public void prepare(Node startElement) {
 			current = (Node) startElement;
 			
 			dummy.next = current;
			dummy.prev = current;
 			current = dummy;
 		}
 
 		@SuppressWarnings("unchecked")
 		public void add(T e) {
 			NonConcurrentList.this.add((Node)current, (E)e, false);
 		}
 
 		public boolean hasPrevious() {
			return current.prev != null;
 		}
 
 		public int nextIndex() {
 			throw new UnsupportedOperationException();
 		}
 
 		@SuppressWarnings("unchecked")
 		public T previous() {
 			if(current.prev==null)
 				return null;
 			current = current.prev;
 			return (T) current.elem;
 		}
 
 		public int previousIndex() {
 			throw new UnsupportedOperationException();
 		}
 
 		@SuppressWarnings("unchecked")
 		public void set(T e) {
 			current.elem = (E) e;
 		}
 	}
 	
 	public Node first;
 	public Node last;
 	public int defaultIteratorIndex;
 	private NonConcurrentIterator<E>[] iterators;
 	private int size;
 	
 	@SuppressWarnings("unchecked")
 	public NonConcurrentList(int iteratorCount) {
 		iterators = new NonConcurrentIterator[iteratorCount];
 		for (int i = 0; i < iteratorCount; i++) iterators[i] = new NonConcurrentIterator<E>();
 		defaultIteratorIndex = 0;
 	}
 	
 	public NonConcurrentList() {
 		this(2);
 	}
 	
 	public boolean add(E element) {	//end of list
 		add(last, element, true);
 		return true;
 	}
 
 	public void add(int index, E element) {	//insert (before this item)
 		if (index > size || index < 0) {
 			throw new IndexOutOfBoundsException("item: "+index +" size: "+size);
 		}
 		
 		if (index == size) {
 			add(element);	//end of list
 		} else {
 			Node curr = first;
 			for(int i = 0; i < index; i++) curr = curr.next;
 			add(curr, element, index==size);
 		}		
 	}
 
 	public boolean addAll(Collection<? extends E> c) {
 		for(E e : c) add(e);
 		return true;
 	}
 
 	public boolean addAll(int index, Collection<? extends E> c) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void clear() {
 		first = null;
 		last = null;
 		size = 0;
 	}
 
 	public boolean contains(Object o) {
 		Node curr = first;
 		while (curr != null) {
 			if (curr.elem.equals(o)) return true;
 			curr = curr.next;
 		}
 		return false;
 	}
 
 	public boolean containsAll(Collection<?> c) {
 		throw new UnsupportedOperationException();
 	}
 
 	public E get(int index) {
 		if (validIdx(index)) {
 			Node curr = first;
 			for (int i = 0; i < index; i++) {
 				curr = curr.next;
 			}
 			return curr.elem;
 		}
 		return null;
 	}
 
 	public int indexOf(Object o) {
 		int idx = -1;
 		Node curr = first;
 		while (curr != null) {
 			idx++;
 			if (curr.elem.equals(o)) return idx;
 			curr = curr.next;
 		}
 		return idx;
 	}
 
 	public boolean isEmpty() {
 		return size == 0;
 	}
 
 	public Iterator<E> iterator() {
 		NonConcurrentIterator<E> iterator = iterators[defaultIteratorIndex];
 		iterator.prepare(0);
 		return iterator;
 	}
 
 	public int lastIndexOf(Object o) {
 		int idx = -1;
 		int counter = -1;
 		Node curr = first;
 		while (curr != null) {
 			counter++;
 			if (curr.elem.equals(o)) idx = counter;
 			curr = curr.next;
 		}
 		return idx;
 	}
 	
 	public ListIterator<E> listIterator() {
 		NonConcurrentIterator<E> iterator = iterators[defaultIteratorIndex];
 		iterator.prepare(0);
 		return iterator;
 	}
 
 	public ListIterator<E> listIterator(Node startNode) {
 		NonConcurrentIterator<E> iterator = iterators[defaultIteratorIndex];
 		iterator.prepare(startNode);
 		return iterator;
 	}
 
 	public ListIterator<E> listIterator(E startElement) {
 		NonConcurrentIterator<E> iterator = iterators[defaultIteratorIndex];
 		Node curr = first;
 		while (curr != null) {
 			if (curr.elem.equals(startElement)) break;
 			curr = curr.next;
 		}
 
 		if (!curr.elem.equals(startElement)) throw new RuntimeException("start element not found: "+startElement); 
 
 		iterator.prepare(curr);
 		return iterator;
 	}
 
 	public ListIterator<E> listIterator(int startIndex) {
 		NonConcurrentIterator<E> iterator = iterators[defaultIteratorIndex];
 		if (validIdx(startIndex)) {
 			iterator.prepare(startIndex);
 			return iterator;
 		}
 		return null;
 	}
 
 	public boolean remove(Object o) {
 		Node curr = first;
 		while (curr != null) {
 			if (curr.elem.equals(o)) {
 				remove(curr, curr.elem);
 				return true;
 			}
 			curr = curr.next;
 		}
 		return false;
 	}
 
 	public E remove(int index) {
 		if (validIdx(index)) {
 			Node curr = first;
 			for (int i = 0; i < index; i++) curr = curr.next;
 			remove(curr, curr.elem);
 			return curr.elem;
 		}
 		return null;
 	}
 
 	public E set(int index, E element) {
 		if (validIdx(index)) {
 			Node curr = first;
 			for (int i = 0; i < index; i++) curr = curr.next;
 			curr.elem = element;
 		}
 		return null;
 	}
 
 	public int size() {
 		return size;
 	}
 	
 	public boolean removeAll(Collection<?> c) {
 		throw new UnsupportedOperationException();
 	}
 
 	public boolean retainAll(Collection<?> c) {
 		throw new UnsupportedOperationException();
 	}
 
 	public List<E> subList(int fromIndex, int toIndex) {
 		throw new UnsupportedOperationException();
 	}
 
 	public Object[] toArray() {
 		throw new UnsupportedOperationException();
 	}
 
 	public <T> T[] toArray(T[] a) {
 		throw new UnsupportedOperationException();
 	}	
 	
 	private boolean validIdx(int index) {
 		if (index >= size || index < 0) {
 			throw new IndexOutOfBoundsException("item: "+index +" size: "+size);
 		}
 		return true;
 	}
 	
 	public void add(Node node, E elem, boolean after) {
 		size++;
 
 		if (first == null) {
 			first = new Node(elem, null, null);
 			last = first;
 		} else {
 			if (after) {
 				//add after node
 				Node newNode = new Node(elem, node, node.next);
 				if (node == last) last = newNode;
 				node.next = newNode;
 				if (newNode.next != null) newNode.next.prev = newNode;
 				
 			} else {
 				//add before node
 				Node newNode = new Node(elem, node.prev, node);
 				if (node == first) first = newNode;
 				
 				node.prev = newNode;
 				if (newNode.prev != null) newNode.prev.next = newNode;
 			}
 		}
 	}
 	
 	public void remove(Node node, E elem) {
 		size--;
 		if (node == first) first = node.next;
 		if (node == last) last = node.prev;
 		
 		Node prev = node.prev;
 		Node next = node.next;
 		
 		if (prev != null) {
 			prev.next = node.next;
 		}
 		
 		if (next != null) {
 			next.prev = node.prev;
 		}
 		
 	}
 	
 	@Override
 	public String toString() {
 		String list = "[ | ";
 		
 		Node curr = first;
 		while(curr != null) {
 			list+= curr.elem.toString()+" | ";
 			curr = curr.next;
 		}
 		
 		return size + " items " + list+"] ";
 	}
 	
 //	public static void main(String[] args) {
 //		NonConcurrentList<String> list = new NonConcurrentList<String>(2);
 //		System.out.println("add/remove test:");
 //		String foo = "z";
 //
 //		list.add("a");
 //		list.add("b");
 //		list.add("c");
 //		list.add("d");
 //		list.add("e");
 //		list.add("f");
 //		list.add("g");
 //		list.add(foo);
 //		list.add("h");
 //		list.add("i");
 //		list.add("j");
 //		list.add("k");
 //
 //		list.remove(7);
 //		System.out.println(list);
 //
 //		System.out.println("\nmultiple iterator test:");
 //		list.defaultIteratorIndex = 0;
 //		for (String string : list) {
 //			list.defaultIteratorIndex = 1;
 //			for (String string2 : list) {
 //				System.out.print("["+string +" "+string2+"]");
 //			}
 //			System.out.println();
 //		}
 //
 //		System.out.println("\niterator start from node test:");
 //		ListIterator<String> nodeIter = list.listIterator(list.first.next.next);
 //		while (nodeIter.hasNext()) {
 //			System.out.print(nodeIter.next());
 //		}
 //
 //		System.out.println("\n\niterator start from elem");
 //		ListIterator<String> elemIter = list.listIterator(list.first.next.next.next.next.elem);
 //		while (elemIter.hasNext()) {
 //			System.out.print(elemIter.next());
 //		}
 //		System.out.println();
//		
//		System.out.print("\n\nfw-iterator: -> ");
//		ListIterator<String> fwIter = list.listIterator();
//		while(fwIter.hasNext()) {
//			System.out.print(fwIter.next());
//		}
//		System.out.println();
//		
//		System.out.print("\n\nbw-iterator: -> ");
//		ListIterator<String> bwIter = list.listIterator(list.size-1);
//		while (bwIter.hasPrevious()) {
//			System.out.print(bwIter.previous());
//		}
//		System.out.println();
 //	}
 }
