 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package superfy.functions.structures;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.annotation.meta.When;
 import superfy.common.annotations.advice.Factory;
 
 /**
  * This class defines a 2-tuple.  This class is iterable, can contain nulls, and immutable.
  * Equality of a tuple is based on the elements as well as the type of the tuple.  
  * Equality is also order-dependent.
  * 
  * @author haswellj
  */
 public final class Pair<T, U> {
 
 	public final U snd;
 	public final T fst;
 
 	/**
 	 * Create a new pair containing the specified elements
 	 * @param fst the first element in this pair
 	 * @param snd the second element in this pair
 	 */
 	public Pair(@Nullable T fst, @Nullable U snd) {
 		this.fst = fst;
 		this.snd = snd;
 	}
 
 	public Pair(@Nonnull(when = When.ALWAYS) Pair<T, U> pair) {
 		if(pair == null) {
 			throw new IllegalArgumentException(
 				"Error:  cannot construct a pair from null fst and snd"
 			);
 		}
 		this.fst = pair.fst;
 		this.snd = pair.snd;
 	}
 
 	@Nullable
 	public T getFst() {
 		return fst;
 	}
 
 	@Nullable 
 	public U getSnd() {
 		return snd;
 	}
 
 	/**
 	 * Construct a new pair from the specified elements
 	 * @param <T> The type of the first element
 	 * @param <U> The type of the second element
 	 * @param fst the first element in this pair (may be null)
 	 * @param snd the second element in this pair (may be null)
 	 * @return a new Pair of type {@code Pair<T, U>} containing fst and snd
 	 */
 	@Nonnull(when = When.ALWAYS)
 	@Factory(returns = Pair.class)
 	public static <T, U> Pair<T, U> pair(@Nullable T fst, @Nullable U snd) {
 		return new Pair(fst, snd);
 	}
 
 	/**
 	 * Shallow-copy the specified pair into a new pair
 	 * @param <T> the type of the first element
 	 * @param <U> the type of the second element
 	 * @param pair the pair to copy
 	 * @return a copy of the pair containing the first pair's elements
 	 * @throws IllegalArgumentException if {@code pair} is null
 	 */
 	@Nonnull(when = When.ALWAYS)
 	@Factory(returns = Pair.class)
 	public static <T, U> Pair<T, U> pair(@Nonnull(when = When.ALWAYS) Pair<T, U> pair) {
 		return new Pair(pair);
 	}
 
 	@Override
 	@SuppressWarnings("RefusedBequest")
 	public boolean equals(Object o) {
 		if(o == this) return true;
 		if(o == null) return false;
 		if(o.getClass().equals(Pair.class)) {
 			final Pair pair = (Pair) o;
 			final Object pFst = pair.fst;
 			final Object pSnd = pair.snd;
 			final boolean fstEqPFst = 
 				fst == null ? pFst == null : 
 				fst == pFst || fst.equals(pFst);
 
 			final boolean sndEqPSnd = 
 				snd == null ? pSnd == null : 
 				snd == pSnd || snd.equals(pSnd);
 
 			return fstEqPFst && sndEqPSnd;
 		}
 		return false;
 	}
 
 	@Override
 	public int hashCode() {
 		int hashCode = 7;
 		return  (hashCode + 
 			(fst == null ? 0 : fst.hashCode()) * 17 + 
 			(snd == null ? 0 : snd.hashCode()) * 23) << 3;
 	}
 
 	@Override
 	public String toString() {
 		return String.format(
 			"Pair[%s, %s](fst=%s, snd=%s)", 
 			typeForElement(fst),
 			typeForElement(snd),
 			fst, snd
 		);
 	}
 
 	private String typeForElement(Object o) {
		return o == null ? "null" : o.getClass().getName();	
 	}
 }
