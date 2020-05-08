 package helpers;
 
import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.vecmath.Tuple3f;
 import javax.vecmath.Vector3f;
 
 public final class StaticHelpers {
 	public static Iterable<Float> flatten(Iterable<Vector3f> v) {
 		List<Float> l = new ArrayList<>();
 		for (Tuple3f x : v) {
 			l.add(x.x);
 			l.add(x.y);
 			l.add(x.z);
 		}
 		return l;
 	}
 	
 	public static <A,B> Pair<A, B> pair(A a, B b) { return new Pair<A, B>(a, b); }
 	
 	public static float coord(Tuple3f v, int c) {
 		if (c == 0) return v.x;
 		else if (c == 1) return v.y;
 		else if (c == 2) return v.z;
 		else throw new IllegalArgumentException();
 	}
 	
 	public static int ithBit(int i, long v) {
 		return (int) (1 & (v >> i));
 	}
 	
 	public static int[] asArray(int... is) {
 		return is;
 	}
 	
 	public static String resourcePath(String path) {
 		return StaticHelpers.class.getResource(path).getFile();
 	}
 	
 	/** All directions (including diagonals) in the 0bXYZ format */
 	public static Iterable<Integer> directions0bXYZ() {
 		List<Integer> a = new ArrayList<>(7);
 		for (int i = 0; i < 8; i++) {
 			a.add(i);
 		}
 		return a;
 	}
 
 	public static float cot(float x) {
 		return (float) (1 / (1e-12 + Math.tan(x)));
 	}
 
 	public static <B, A extends Comparable<A>> A maximize(Function<B, A> f,
 			List<B> l) {
 		return Collections.max(map(f, l));
 	}
 
 	public static <B, A extends Comparable<A>> A minimize(Function<B, A> f,
 			List<B> l) {
 		return Collections.min(map(f, l));
 	}
 
 	public static <A, B> List<A> map(Function<B, A> f, Iterable<B> m) {
 		List<A> l = new ArrayList<A>();
 		for (B b : m) {
 			l.add(f.call(b));
 		}
 		return l;
 	}
 
 	public static <A, B, C> Function<A, C> comp(final Function<B, C> g,
 			final Function<A, B> f) {
 		return new Function<A, C>() {
 			@Override
 			public C call(A a) {
 				return g.call(f.call(a));
 			}
 		};
 	}
 
 	public static float norm(Tuple3f v) {
 		return (float) Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
 	}
 
 	public static <A, B> Iterable<Pair<A, B>> zip(Iterable<A> a, Iterable<B> b) {
 		Iterator<A> iter_a = a.iterator();
 		Iterator<B> iter_b = b.iterator();
 
 		List<Pair<A, B>> ret = new ArrayList<Pair<A, B>>();
 
 		while (iter_a.hasNext() && iter_b.hasNext()) {
 			ret.add(new Pair<A, B>(iter_a.next(), iter_b.next()));
 		}
 		return ret;
 	}
 
 	public static <A, B> Iterable<Pair<A, B>> zip(Iterator<A> a, Iterator<B> b) {
 		return zip(iter(a), iter(b));
 	}
 
 	public static <A, B> Iterable<Pair<A, B>> zip(Iterable<A> a, Iterator<B> b) {
 		return zip(iter(a), iter(b));
 	}
 
 	public static <A, B> Iterable<Pair<A, B>> zip(Iterator<A> a, Iterable<B> b) {
 		return zip(iter(a), iter(b));
 	}
 	
 	public static Iterator<Integer> count() {
 		return count(0);
 	}
 
 	public static Iterator<Integer> count(final int start) {
 		return new Iterator<Integer>() {
 			int current = start;
 
 			@Override
 			public boolean hasNext() {
 				return true;
 			}
 
 			@Override
 			public Integer next() {
 				return current++;
 			}
 
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}
 		};
 	}
 	
 	public static <A> Iterable<Indexed<A>> withIndex(Iterable<A> l) {
 		ArrayList<Indexed<A>> x = new ArrayList<>();
 		int i = 0;
 		for (A a: l) {
 			x.add(new Indexed<A>(i++, a));
 		}
 		return x;
 	}
 
 	public static <A> Iterable<A> iter(Iterable<A> x) {
 		return x;
 	}
 
 	public static <A> Iterable<A> iter(Iterator<A> x) {
 		return new TrivialIterable<A>(x);
 	}
 
 	public static <A> long len(Iterable<A> x) {
 		int len = 0;
 		for (A a : x) {
 			len++;
 		}
 		return len;
 	}
 
 	public static <A> long len(Iterator<A> x) {
 		return len(iter(x));
 	}
 
 	public static <A> List<A> list(Iterable<A> x) {
 		ArrayList<A> l = new ArrayList<A>();
 		for (A a : x)
 			l.add(a);
 		return l;
 	}
 
 	public static <A> List<A> list(Iterator<A> x) {
 		return list(iter(x));
 	}
 	
 
 	public static List<Float> list(float[] x) {
 		ArrayList<Float> l = new ArrayList<Float>();
 		for (float a : x)
 			l.add(a);
 		return l;
 	}
 
 	public static <A> Set<A> set(Iterable<A> x) {
 		HashSet<A> l = new HashSet<A>();
 		for (A a : x)
 			l.add(a);
 		return l;
 	}
 
 	public static <A> Set<A> set(Iterator<A> x) {
 		return set(iter(x));
 	}
 
 	public static class Pair<A, B> {
 		@Override
 		public String toString() {
 			return "(" + a + ", " + b + ")";
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((a == null) ? 0 : a.hashCode());
 			result = prime * result + ((b == null) ? 0 : b.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			Pair other = (Pair) obj;
 			if (a == null) {
 				if (other.a != null)
 					return false;
 			} else if (!a.equals(other.a))
 				return false;
 			if (b == null) {
 				if (other.b != null)
 					return false;
 			} else if (!b.equals(other.b))
 				return false;
 			return true;
 		}
 
 		public A a;
 		public B b;
 
 		public Pair(A a, B b) {
 			this.a = a;
 			this.b = b;
 		}
 	}
 	
 	public static class Indexed<A> extends Pair<Integer, A> {
 		public Indexed(Integer a, A b) {
 			super(a, b);
 		}
 		public int index() { return a; }
 		public A value() { return b; }
 	}
 
 	public static float[] concat(float[] x, float[] y) {
 		float[] new_array = new float[x.length + y.length];
 		for (Pair<Integer, Float> p : zip(count(0), iter(x))) {
 			new_array[p.a] = p.b;
 		}
 		for (Pair<Integer, Float> p : zip(count(x.length), iter(y))) {
 			new_array[p.a] = p.b;
 		}
 		return new_array;
 	}
 
 	public static <A> Iterable<A> iter(A[] y) {
 		return new ArrayIterable<>(y);
 	}
 
 	public static Iterable<Float> iter(float[] y) {
 		return new ArrayIterableFloat(y);
 	}
 }
 
 class ArrayIterableFloat implements Iterable<Float> {
 	private float[] as;
 
 	public ArrayIterableFloat(float[] as) {
 		this.as = as;
 	}
 
 	@Override
 	public Iterator<Float> iterator() {
 		return new ArrayIter();
 	}
 
 	final class ArrayIter implements Iterator<Float> {
 		int n = 0;
 
 		@Override
 		public boolean hasNext() {
 			return n < as.length;
 		}
 
 		@Override
 		public Float next() {
 			return as[n++];
 		}
 
 		@Override
 		public void remove() {
 			throw new AssertionError("Removal not supported");
 		}
 	}
 }
 
 class ArrayIterable<A> implements Iterable<A> {
 	private A[] as;
 
 	public ArrayIterable(A[] as) {
 		this.as = as;
 	}
 
 	@Override
 	public Iterator<A> iterator() {
 		return new ArrayIter();
 	}
 
 	final class ArrayIter implements Iterator<A> {
 		int n = 0;
 
 		@Override
 		public boolean hasNext() {
 			return n < as.length;
 		}
 
 		@Override
 		public A next() {
 			return as[n++];
 		}
 
 		@Override
 		public void remove() {
 			throw new AssertionError("Removal not supported");
 		}
 
 	}
 }
 
 class TrivialIterable<A> implements Iterable<A> {
 	private Iterator<A> iter;
 
 	public TrivialIterable(Iterator<A> x) {
 		this.iter = x;
 	}
 
 	@Override
 	public Iterator<A> iterator() {
 		return iter;
 	}
 }
