 package edu.x3m.kas.core.structures;
 
 
 /**
  * Abstract class representing node in Huffmann's encoding and decoding
  *
  * @author Hans
  */
 public abstract class AbstractNode implements Comparable<AbstractNode> {
 
 
     private static final int DEFAULT_COUNT = 1;
     private static final int DEFAULT_CODE_SIZE = 4;
     private static final int DEFAULT_BALANCE_INDEX = 256;
     //
     public int count = DEFAULT_COUNT;
     protected StringBuilder code = new StringBuilder (DEFAULT_CODE_SIZE);
 
 
 
     /**
      * Appends one char at the end of the code
      *
      * @param character
      */
     public void append (char character) {
         code.append (character);
     }
 
 
 
     /**
      * Increments count
      */
     public void inc () {
         count++;
     }
 
 
 
     @Override
     public int compareTo (AbstractNode o) {
         return getCount () - o.getCount ();
     }
 
 
 
     /**
      * Method finds {@link SimpleNode} which contains correct prefix in data.
      *
      * @param data array of bytes
      * @param from which position look
      * @param pos  index of searched char
      * @return null or node
      */
     public abstract SimpleNode find (byte[] data, int from, int pos);
     //--------------------------------------
     //# Privates
     //--------------------------------------
     //--------------------------------------
 
 
 
     /**
      * Method return weighted count (with character ordinality)
      *
     * @return weighted count used when comparing
      */
     protected int getCount () {
         return count * DEFAULT_BALANCE_INDEX;
     }
 }
