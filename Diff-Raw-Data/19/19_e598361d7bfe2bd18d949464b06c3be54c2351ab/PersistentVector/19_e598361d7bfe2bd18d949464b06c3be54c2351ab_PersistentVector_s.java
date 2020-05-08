 package ash.lang;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 import ash.parser.Parser;
 import bruce.common.utils.CommonUtils;
 
 //TODO : make more efficient
 public class PersistentVector<E> implements Iterable<E>, PersistentCollection<E> {
 	private static final long serialVersionUID = 7068696961107511500L;
 	private final List<E> ls;
 	
 	public PersistentVector() {
 		this(new ArrayList<E>());
 	}
 	
 	@SafeVarargs
 	public PersistentVector(E... es) {
 		this(Arrays.asList(es));
 	}
 
 	public PersistentVector(List<E> initls) {
 		ls = initls;
 	}
 
 	public PersistentVector(Collection<E> values) {
 		this(new ArrayList<>(values));
 	}
 
 	@Override
 	public PersistentVector<E> conj(E e) {
 		List<E> newLs = new ArrayList<>(ls);
 		newLs.add(e);
 		return new PersistentVector<>(newLs);
 	}
 
 	public PersistentVector<E> assoc(int index, E element) {
 		List<E> newLs = new ArrayList<>(ls);
 		newLs.set(index, element);
 		return new PersistentVector<>(newLs);
 	}
 
 	@Override
 	public PersistentCollection<E> merge(PersistentCollection<? extends E> c) {
 		List<E> newLs = new ArrayList<>(ls);
 		newLs.addAll(c.getCollection());
 		return new PersistentVector<>(newLs);
 	}
 
 	public PersistentVector<E> merge(int index, PersistentCollection<? extends E> c) {
 		List<E> newLs = new ArrayList<>(ls);
 		newLs.addAll(index, c.getCollection());
 		return new PersistentVector<>(newLs);
 	}
 
 	@Override
 	public boolean contains(E o) {
 		return ls.contains(o);
 	}
 
 	@Override
 	public boolean containsAll(PersistentCollection<? extends E> c) {
 		return ls.containsAll(c.getCollection());
 	}
 
 	public E get(int index) {
 		return ls.get(index);
 	}
 
 	public int indexOf(E o) {
 		return ls.indexOf(o);
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return ls.isEmpty();
 	}
 
 	@Override
 	public Iterator<E> iterator() {
 		return ls.iterator();
 	}
 
 	public int lastIndexOf(E o) {
 		return ls.lastIndexOf(o);
 	}
 
 	public ListIterator<E> listIterator() {
 		return ls.listIterator();
 	}
 
 	public ListIterator<E> listIterator(int index) {
 		return ls.listIterator(index);
 	}
 
 	@Override
 	public PersistentVector<E> disj(E o) {
 		List<E> newLs = new ArrayList<>(ls);
 		newLs.remove(o);
 		return new PersistentVector<E>(newLs);
 	}
 
 	public PersistentVector<E> disjAt(int index) {
 		List<E> newLs = new ArrayList<>(ls);
 		newLs.remove(index);
 		return new PersistentVector<E>(newLs);
 	}
 
 	@Override
 	public PersistentVector<E> disjAll(PersistentCollection<? extends E> c) {
 		List<E> newLs = new ArrayList<>(ls);
 		newLs.removeAll(c.getCollection());
 		return new PersistentVector<E>(newLs);
 	}
 
 	@Override
 	public PersistentVector<E> retainAll(PersistentCollection<? extends E> c) {
 		List<E> newLs = new ArrayList<>(ls);
 		newLs.retainAll(c.getCollection());
 		return new PersistentVector<E>(newLs);
 	}
 
 	public PersistentVector<E> conj(int index, E element) {
 		List<E> newLs = new ArrayList<>(ls);
 		newLs.add(index, element);
 		return new PersistentVector<E>(newLs);
 	}
 
 	@Override
 	public int size() {
 		return ls.size();
 	}
 
 	public PersistentVector<E> subList(int fromIndex, int toIndex) {
 		return new PersistentVector<E>(ls.subList(fromIndex, toIndex));
 	}
 
 	@Override
 	public Object[] toArray() {
 		return ls.toArray();
 	}
 
 	@Override
 	public <T> T[] toArray(T[] a) {
 		return ls.toArray(a);
 	}
 
 	@Override
 	public Collection<E> getCollection() {
 		return ls;
 	}
 
 	@Override
 	public int hashCode() {
 		return 31 + ((ls == null) ? 0 : ls.hashCode());
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) return true;
 		if (obj == null) return false;
 		if (getClass() != obj.getClass()) return false;
 		PersistentVector<?> other = (PersistentVector<?>) obj;
 		if (ls == null) {
 			if (other.ls != null)
 				return false;
 		} else if (!ls.equals(other.ls))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return CommonUtils.buildString(Parser.VECTOR_START,
				CommonUtils.displayArray(toArray(), " "),
 				Parser.VECTOR_END);
 	}
 }
