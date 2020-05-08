 package edu.x3m.kas.core.structures;
 
 
 /**
 * Class represents actual node.
 *
 * @see AbstractNode
 * @see GroupNode
  * @author Hans
  */
 public class SimpleNode extends AbstractNode {
 
 
     public int character;
     public String finalCode;
     public int codeLength;
 
 
 
     /**
      * Constructor when encoding
      *
      * @param character
      */
     public SimpleNode (int character) {
         this.character = character;
     }
 
 
 
     /**
      * Constructor when decoding
      *
      * @param character
      * @param count
      */
     public SimpleNode (int character, int count) {
         this.character = character;
         this.count = count;
     }
 
 
 
     @Override
     public String toString () {
         return String.format ("[N %s %dx '%s']", (char) character, count, finalCode);
     }
 
 
 
     /**
      * Closes code and reverses it.
      */
     public void endCode () {
         finalCode = code.reverse ().toString ();
         codeLength = finalCode.length ();
     }
 
 
 
     @Override
     public SimpleNode find (byte[] data, int from, int pos) {
         return this;
     }
 }
