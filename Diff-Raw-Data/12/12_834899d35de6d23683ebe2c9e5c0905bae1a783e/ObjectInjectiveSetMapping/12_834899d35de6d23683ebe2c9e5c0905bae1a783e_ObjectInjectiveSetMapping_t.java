 package org.eclipse.jst.jsf.common.sets.internal.provisional.mapping;
 
 import java.util.Iterator;
 
 import org.eclipse.jst.jsf.common.sets.internal.provisional.AxiomaticSet;
 
 /**
 * A type of axiomatic set mapping that operates injectively on the output
 * set to create its result set.  Note that the injection (one-to-oneness)
 * of the mapping is based on Java objects and not on the value of the object.
  * Therefore, the result set is not guaranteed to be injective on the basis
  * of value.
  * 
 * For example, consider a set of integers:
  * 
  * X = {new Integer(4), new Integer(6), new Integer(9)}
  * 
  * an ObjectInjectiveSetMapping may be defined that maps this set
  * to a result based on:
  * 
  * map(x) = new Boolean(x < 8) for all x in X
  * 
  * An ObjectiveInjectiveSetMapping result set would look like this:
  * 
  * map(X) = {new Boolean(true), new Boolean(true), new Boolean(false)}
  * 
  * Note that boolean TRUE maps twice, so based on the value of the set members,
  * the mapping is not injective.  However, each Java object in X maps to a
 * distinct Java object in map(X)
  * 
  * This interface should not be implemented by clients. Clients should
  * sub-class AbstractObjectInjectiveSetMapping to avoid future API breakage.
  * 
  * @author cbateman
  *
  */
 public interface ObjectInjectiveSetMapping extends AxiomaticSetMapping
 {
     /**
      * This method optimizes the case where a client wants to apply some
      * mapping or constraint to each element of a set until a particular
      * condition applies and then stop.
      * 
      * Mapping's that implement this
      * interface should generally implement their map(set) method as:
      * 
      *  for (Iterator it = mapIterator(set); it.hasNext();)
      *  {
      *      result.add(doMapping(it.next));
      *  }
      *  
      * @param set
      * @return an iterator that allows a client to apply the mapping
      * one element of set at a time.  The iterator is immutable and should
      * throw UnsupportedOperationException if remove() is called.
      */
     public Iterator mapIterator(AxiomaticSet set);
 }
