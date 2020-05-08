 package de.deterministicarts.lib.functional;
 
 import java.io.Serializable;
 import java.lang.reflect.Array;
 import java.util.Comparator;
 
 /**
  * Generic 2-tuple of values.
  * 
  * <p><strong>Thread-safety</strong> Instances of this class are
  * immutable and thus safe to share among multiple concurrently
  * running threads. Note, though, that there may still be concurrency
  * issues, if the component values of a pair do not offer similar
  * guarantees.
  * 
  * <p><strong>Serialization</strong> Instances of this class are
  * serializable if (and only if) both component values are 
  * serializable.
  * 
  * <p><strong>Ordering</strong> Instances of this class are not
  * {@linkplain Comparable ordered} by default. However, the functions
  * {@link #lexicographicOrdering()} and {@link #lexicographicOrdering(Comparator, Comparator)}
  * may be used to obtain comparator objects, which use lexicographic
  * ordering of pairs based on the ordering of the component values.
  * The resulting ordering is consistent with equals if, and only if,
  * the ordering over the component values is consistent with equals
  * for both components.
  *
  * @param <F>	type of first component value
  * @param <S>	type of second component value
  */
 
 public final class Pair<F,S> 
 implements Serializable, Tuple2<F,S> {
 	
 	private static final long serialVersionUID = -5107625752120263352L;
 	
 	private final F first;
 	private final S second;
 	
 	private Pair(F first, S second) {
 		this.first = first;
 		this.second = second;
 	}
 	
 	/**
 	 * Obtains the first component value.
 	 * @return	the first component value of this pair
 	 */
 	
 	public F first() {
 		return first;
 	}
 	
 	/**
 	 * Obtains the second component value.
 	 * @return	the pair's second component value.
 	 */
 	
 	public S second() {
 		return second;
 	}
 	
 	/**
 	 * Creates a new pair. This function returns a new {@linkplain Pair pair},
 	 * with {@code first} being the pair's first component value, and 
 	 * {@code second} being the pair's second component value.
 	 * 
 	 * @param <F>	type of first component value
 	 * @param <S>	type of second component value
 	 * 
 	 * @param first		first component value of the new pair
 	 * @param second	second component value of the new pair
 	 * 
 	 * @return	a new pair composed of {@code first} and {@code second}
 	 */
 	
 	public static <F,S> Pair<F,S> of(F first, S second) {
 		return new Pair<F,S>(first, second);
 	}
 	
	public static <F,S> Pair<F,S> valueOf(Tuple2<? extends F,? extends S> tuple) {
 		if (tuple instanceof Pair<?,?>) return (Pair<F,S>)tuple;
 		return Pair.<F,S>of(tuple.first(), tuple.second());
 	}
 	
 	public int hashCode() {
 		return (first == null? 0 : first.hashCode()) * 31
 			 + (second == null? 0 : second.hashCode());
  	}
 	
 	/**
 	 * Convert this pair into an array. This method returns a new array
 	 * of component type {@link Object}, which contains exactly the values
 	 * of this pair's components.
 	 * 
 	 * @return	a new array containing the pair's components
 	 */
 	
 	public Object[] toArray() {
 		return new Object[] { first, second };
 	}
 	
 	/**
 	 * Convert this pair into an array. This method returns a new array
 	 * of component type {@code T}, which contains exactly the values
 	 * of this pair's components. The type {@code T} must be assignable
 	 * from the pair's first component tpe as well as from the pair's
 	 * second component type.
 	 * 
 	 * @return	a new array containing the pair's components
 	 */
 	
 	@SuppressWarnings("unchecked") public <T> T[] toArray(T[] proto) {
 		if( proto == null ) throw new IllegalArgumentException();
 		else {
 			if( proto.length < 2 ) proto = (T[])Array.newInstance(proto.getClass().getComponentType(), 2);
 			final Object[] temp = proto;		// make array covariance work for us...
 	        temp[0] = first;
 	        temp[1] = second;
 	        return proto;
 		}
 	}
 	
 	public boolean equals(Object rhs) {
 		
 		if( rhs == this ) return true;
 		else {
 			
 			if( !(rhs instanceof Pair<?,?>) ) return false;
 			else {
 				final Pair<?,?> p = (Pair<?,?>)rhs;
 				
 				return (first == p.first || first != null && first.equals(p.first)) 
 					&& (second == p.second || second != null && second.equals(p.second));
 			}
 		}
 	}
 	
 	public String toString() {
 		return "Pair(" + first + ", " + second + ")";
 	}
 	
 	/**
 	 * Comparator using the specified component orderings. This
 	 * function returns a {@link Comparator}, which compares pairs using
 	 * lexicographic ordering. The components of pairs comparable with
 	 * the comparator are compared using the given explicit comparantor
 	 * objects.
 	 * 
 	 * @param <A>	type of first component
 	 * @param <B>	type of second component
 	 * 
 	 * @param first		comparator for the first component
 	 * @param second	comparator for the second component
 	 * 
 	 * @return	a comparator providing lexicographic ordering of 
 	 * 			pairs
 	 */
 	
 	public static <A,B> Comparator<Pair<A,B>> lexicographicOrdering(final Comparator<? super A> first, final Comparator<? super B> second) {
 		if( first == null ) throw new IllegalArgumentException();
 		if( second == null ) throw new IllegalArgumentException();
 		return new Comparator<Pair<A,B>>() {
 			public int compare(Pair<A,B> o1, Pair<A,B> o2) {
 				final int fd = first.compare(o1.first(), o2.first());
 				return fd != 0? fd : second.compare(o1.second(), o2.second());
 			}
 		};
 	}
 	
 	/**
 	 * Comparator using the natural ordering of pair components. This
 	 * function returns a {@link Comparator}, which compares pairs using
 	 * lexicographic ordering. The components of pairs comparable with
 	 * the comparator returned by this function must have a natural
 	 * ordering defined.
 	 * 
 	 * @param <A>
 	 * @param <B>
 	 * 
 	 * @return	a comparator providing lexicographic ordering of 
 	 * 			pairs
 	 */
 	
 	public static <A extends Comparable<? super A>,B extends Comparable<? super B>> Comparator<Pair<A,B>> lexicographicOrdering() {
 		return new Comparator<Pair<A,B>>() {
 			public int compare(Pair<A,B> o1, Pair<A,B> o2) {
 				final int fd = o1.first().compareTo(o2.first());
 				return fd != 0? fd : o1.second().compareTo(o2.second());
 			}
 		};
 	}
 	
 	/**
 	 * Getter for the first component. This function returns a 
 	 * transformation, which acts as the projection from pairs
 	 * to their first component. The transformation is {@code null}-strict,
 	 * i.e., if the input argument is {@code null}, then the 
 	 * result of the transformation is {@code null}.
 	 * 
 	 * @param <A>	type of first component of a pair
 	 * 
 	 * @return	a transformation, which will yield the first
 	 * 			component of a pair, it is actually applied to
 	 */
 	
 	public static <A> AbstractTransformation<A,Pair<A,?>> firstGetter() {
 		@SuppressWarnings("unchecked") final AbstractTransformation<A,Pair<A,?>> getter = FIRST;
 		return getter;
 	}
 	
 	@SuppressWarnings("rawtypes") private static final AbstractTransformation FIRST = new AbstractTransformation() {
 		public Object transform(Object argument) {
 			return argument == null? null : ((Pair)argument).first();
 		}
 	};
 	
 	/**
 	 * Getter for the second component. This function returns a 
 	 * transformation, which acts as the projection from pairs
 	 * to their second component. The transformation is {@code null}-strict,
 	 * i.e., if the input argument is {@code null}, then the 
 	 * result of the transformation is {@code null}.
 	 * 
 	 * @param <A>	type of second component of a pair
 	 * 
 	 * @return	a transformation, which will yield the second
 	 * 			component of a pair, it is actually applied to
 	 */
 	
 	public static <A> AbstractTransformation<A,Pair<?,A>> secondGetter() {
 		@SuppressWarnings("unchecked") final AbstractTransformation<A,Pair<?,A>> getter = SECOND;
 		return getter;
 	}
 	
 	@SuppressWarnings("rawtypes") private static final AbstractTransformation SECOND = new AbstractTransformation() {
 		public Object transform(Object argument) {
 			return argument == null? null : ((Pair)argument).second();
 		}
 	};
 }
