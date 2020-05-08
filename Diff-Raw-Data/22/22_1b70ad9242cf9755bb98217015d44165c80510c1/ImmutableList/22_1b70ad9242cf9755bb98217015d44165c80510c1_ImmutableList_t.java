 package suite.immutable;
 
 import java.util.ArrayDeque;
 import java.util.Deque;
 import java.util.Iterator;
 
 import suite.util.FunUtil;
 import suite.util.FunUtil.Source;
 
 /**
  * Immutable linked list.
  * 
  * @author ywsing
  */
 public class ImmutableList<T> implements Iterable<T> {
 
 	private final static ImmutableList<?> end = new ImmutableList<Object>(null, null);
 
 	private T head;
 	private ImmutableList<T> tail;
 
 	public ImmutableList(T head, ImmutableList<T> tail) {
 		this.head = head;
 		this.tail = tail;
 	}
 
 	public static <T> ImmutableList<T> end() {
 		@SuppressWarnings("unchecked")
 		ImmutableList<T> end = (ImmutableList<T>) ImmutableList.end;
 		return end;
 	}
 
	@SafeVarargs
	public static <T> ImmutableList<T> asList(T... ts) {
		ImmutableList<T> list = ImmutableList.<T> end();
		for (T t : ts)
			list = new ImmutableList<>(t, list);
		return list;
 	}
 
 	public static <T> ImmutableList<T> cons(T t, ImmutableList<T> list) {
 		return new ImmutableList<>(t, list);
 	}
 
 	public boolean isEmpty() {
 		return this == end;
 	}
 
 	@Override
 	public Iterator<T> iterator() {
 		return FunUtil.iterator(new Source<T>() {
 			private ImmutableList<T> current = ImmutableList.this;
 
 			public T source() {
 				T t;
 				if (current != end) {
 					t = current.head;
 					current = current.tail;
 				} else
 					t = null;
 				return t;
 			}
 		});
 	}
 
 	public Deque<T> reverse() {
 		Deque<T> deque = new ArrayDeque<>();
 		for (T t : this)
 			deque.addFirst(t);
 		return deque;
 	}
 
 	public T getHead() {
 		return head;
 	}
 
 	public ImmutableList<T> getTail() {
 		return tail;
 	}
 
 }
