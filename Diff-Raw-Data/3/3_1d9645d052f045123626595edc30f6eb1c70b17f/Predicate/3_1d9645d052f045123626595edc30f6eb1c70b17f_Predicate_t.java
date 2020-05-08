 package uk.co.jezuk.mango;
 
 /**
  * A <code>Predicate</code> is a special form of a <code>UnaryFunction</code>, whose result represents the
  * truth or otherwise of some condition.
 * <br>
 * Returns <code>true</code> if the condition the Predicate tests for is satisfied, <code>false</code> otherwise.
  * @author Jez Higgins, jez@jezuk.co.uk
  * @version $Id$
  */
 public interface Predicate
 {
   abstract public boolean test(Object x);
 } // Predicate
 
