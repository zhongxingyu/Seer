 package com.bluespot.logic.adapters;
 
 import com.bluespot.logic.Adapters;
 import com.bluespot.logic.predicates.AdaptingPredicate;
 import com.bluespot.logic.predicates.Predicate;
 
 /**
  * Adapts a given value to another type. Many common adapters can be obtained
  * from the static factory methods in {@link Adapters}. Adapters are also widely
  * used by {@link Predicate} objects and their builders.
  * <p>
  * Adapters follow a small set of guidelines, similar in spirit to the
 * {@link #equals(Object)} and {@link Predicate} contracts:
  * <ul>
  * <li><em>Adapters are consistent.</em> Adapters convert a given value the same
  * way consistently. This implies that adapters are immutable.
  * <li><em>Adapters have no side-effects.</em> Adapters should convert values
  * without modifying the given value.
  * <li><em>Adapters throw exceptions in exceptional circumstances.</em>
  * Adapters, when given a value that is invalid or unexpected, should throw an
  * appropriate exception.
  * <li><em>Adapters gracefully handle null values</em>. Adapters should silently
  * return {@code null} unless they're explicitly expecting that value.
  * <li><em>Adapters should not guess.</em> Adapters do not attempt to guess the
  * intentions of a ill-formed value. If it is convertible in its current state,
  * it should be converted. If it cannot be converted, an exception should be
  * raised.
  * </ul>
  * 
  * @author Aaron Faanes
  * 
  * @param <S>
  *            the source type
  * @param <D>
  *            the destination type
  * @see Adapters
  * @see AdaptingPredicate
  */
 public interface Adapter<S, D> {
 
     /**
      * Adapts the specified value to this adapter's destination type. Adapters
      * should make a best effort to return appropriate values for the widest
      * range of inputs; exceptions should rarely be thrown. Forgiving adapters
      * allow them to be used freely without unexpected results cropping up.
      * However, null values are commonly preserved in adapters; a null source
      * value should be converted to a null value unless the adapter explicitly
      * states otherwise.
      * 
      * @param source
      *            the source value to adapt
      * @return the adapted value
      */
     public D adapt(S source);
 }
