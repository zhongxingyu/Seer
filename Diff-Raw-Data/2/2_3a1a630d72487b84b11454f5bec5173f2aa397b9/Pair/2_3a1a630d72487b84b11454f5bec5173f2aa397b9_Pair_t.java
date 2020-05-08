 package uk.co.jezuk.mango;
 
 /**
 * A <code>Pair</code> holds two <code>Object</code>s.
  * @author Jez Higgins, Jez UK Ltd
  * @version $Id$
  */
 public class Pair
 {
   public Pair()
   {
     first = null;
     second = null;
   } // Pair
 
   public Pair(Object one, Object two)
   {
     first = one;
     second = two;
   } // Pair
 
   public Object first;
   public Object second;
 } // class Pair
