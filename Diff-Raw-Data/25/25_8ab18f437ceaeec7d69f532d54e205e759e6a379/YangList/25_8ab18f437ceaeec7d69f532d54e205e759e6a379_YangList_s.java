 package yang.util;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 
 public class YangList<E> implements List<E> {
 
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
 
 	public class YangListIterator<T> implements ListIterator<T> {
 
 		Node current = null;
 		Node dummy = new Node(null, null, null);
 
 		@Override
 		public boolean hasNext() {
 			return current.next != null;
 		}
 
 		@Override
 		@SuppressWarnings("unchecked")
 		public T next() {
 			if(current.next==null)
 				return null;
 			current = current.next;
 			return (T) current.elem;
 		}
 
 		@Override
 		public void remove() {
 			YangList.this.remove(current, current.elem);
 		}
 
 		public void prepare(int idx) {
 			current = first;
 			for (int i = 0; i < idx; i++) current = current.next;
 
 			dummy.next = current;
 			dummy.prev = current;
 			current = dummy;
 		}
 
 		public void prepare(Node startElement) {
 			current = startElement;
 
 			dummy.next = current;
 			dummy.prev = current;
 			current = dummy;
 		}
 
 		@Override
 		@SuppressWarnings("unchecked")
 		public void add(T e) {
 			YangList.this.add(current, (E)e, false);
 		}
 
 		@Override
 		public boolean hasPrevious() {
 			return current.prev != null;
 		}
 
 		@Override
 		public int nextIndex() {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		@SuppressWarnings("unchecked")
 		public T previous() {
 			if(current.prev==null)
 				return null;
 			current = current.prev;
 			return (T) current.elem;
 		}
 
 		@Override
 		public int previousIndex() {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		@SuppressWarnings("unchecked")
 		public void set(T e) {
 			current.elem = (E) e;
 		}
 	}
 
 	public Node first;
 	public Node last;
 	public int defaultIteratorIndex;
 	private YangListIterator<E>[] iterators;
 	private int size;
 
 	@SuppressWarnings("unchecked")
 	public YangList(int iteratorCount) {
 		iterators = new YangListIterator[iteratorCount];
 		for (int i = 0; i < iteratorCount; i++) iterators[i] = new YangListIterator<E>();
 		defaultIteratorIndex = 0;
 	}
 
 	public YangList() {
 		this(2);
 	}
 
 	@Override
 	public boolean add(E element) {	//end of list
 		add(last, element, true);
 		return true;
 	}
 
 	@Override
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
 
 	@Override
 	public boolean addAll(Collection<? extends E> c) {
 		for(E e : c) add(e);
 		return true;
 	}
 
 	@Override
 	public boolean addAll(int index, Collection<? extends E> c) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void clear() {
 		first = null;
 		last = null;
 		size = 0;
 	}
 
 	@Override
 	public boolean contains(Object o) {
 		Node curr = first;
 		while (curr != null) {
 			if (curr.elem.equals(o)) return true;
 			curr = curr.next;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean containsAll(Collection<?> c) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
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
 
 	@Override
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
 
 	@Override
 	public boolean isEmpty() {
 		return size == 0;
 	}
 
 	@Override
 	public Iterator<E> iterator() {
 		YangListIterator<E> iterator = iterators[defaultIteratorIndex];
 		iterator.prepare(0);
 		return iterator;
 	}
 
 	@Override
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
 
 	@Override
 	public ListIterator<E> listIterator() {
 		YangListIterator<E> iterator = iterators[defaultIteratorIndex];
 		iterator.prepare(0);
 		return iterator;
 	}
 
 	public ListIterator<E> listIteratorLast() {
 		return listIterator(size-1);
 	}
 
 	public ListIterator<E> listIterator(Node startNode) {
 		YangListIterator<E> iterator = iterators[defaultIteratorIndex];
 		iterator.prepare(startNode);
 		return iterator;
 	}
 
 	public ListIterator<E> listIterator(E startElement) {
 		YangListIterator<E> iterator = iterators[defaultIteratorIndex];
 		Node curr = first;
 		while (curr != null) {
 			if (curr.elem.equals(startElement)) break;
 			curr = curr.next;
 		}
 
 		if (!curr.elem.equals(startElement)) throw new RuntimeException("start element not found: "+startElement);
 
 		iterator.prepare(curr);
 		return iterator;
 	}
 
 	@Override
 	public ListIterator<E> listIterator(int startIndex) {
 		YangListIterator<E> iterator = iterators[defaultIteratorIndex];
 		if (validIdx(startIndex)) {
 			iterator.prepare(startIndex);
 			return iterator;
 		}
 		return null;
 	}
 
 	@Override
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
 
 	@Override
 	public E remove(int index) {
 		if (validIdx(index)) {
 			Node curr = first;
 			for (int i = 0; i < index; i++) curr = curr.next;
 			remove(curr, curr.elem);
 			return curr.elem;
 		}
 		return null;
 	}
 
 	@Override
 	public E set(int index, E element) {
 		if (validIdx(index)) {
 			Node curr = first;
 			for (int i = 0; i < index; i++) curr = curr.next;
 			curr.elem = element;
 		}
 		return null;
 	}
 
 	@Override
 	public int size() {
 		return size;
 	}
 
 	@Override
 	public boolean removeAll(Collection<?> c) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public boolean retainAll(Collection<?> c) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public List<E> subList(int fromIndex, int toIndex) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Object[] toArray() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
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
 
 	public E getFirst() {
 		if (first != null) return first.elem;
 		else return null;
 	}
 
 	public E getLast() {
 		if (last != null) return last.elem;
 		else return null;
 	}
 
 	public void incIteratorIndex() {
 		defaultIteratorIndex++;
 	}
 
 	public void decIteratorIndex() {
 		defaultIteratorIndex--;
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
