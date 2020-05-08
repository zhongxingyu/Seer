 package uk.co.jezuk.mango;
 
 /**
 * A Pair holds two Objects.
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
