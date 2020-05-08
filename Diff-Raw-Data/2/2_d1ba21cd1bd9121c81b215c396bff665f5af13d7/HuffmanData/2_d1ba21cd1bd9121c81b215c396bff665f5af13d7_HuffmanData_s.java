 
 /**
  * Data for Huffman code tree nodes.
  * 
  * @author Josh Gillham
  * @version 12-3-12
  */
 public class HuffmanData {
     /** The Huffman code for this node; null if none. */
     private String code = null;
     /** Maximum difference to accept two double values as equal. */
     public static final double EPSILON = 0.001;
     /** The frequency stored at this node. */
     private double frq = 0;
     /** The symbol stored at this node; null if none. */
     private Character sym = null;
     
     /**
      * Simple constructor - set all fields to null or 0.
      */
     public HuffmanData() {
     }
     
     /**
      * Constructor that sets the symbol only.
      * 
      * @param symbol the symbol.
      */
     public HuffmanData( Character symbol ) {
         this.sym = symbol;
     }
     
     /**
      * Constructor that sets the symbol and frequency.
      * 
      * @param symbol the symbol.
      * @param frequency the frequency.
      */
     public HuffmanData( Character symbol, double frequency ) {
         this( symbol );
         this.frq = frequency;
     }
     
     /**
      * Constructor that sets the symbol and frequency.
      * 
      * @param symbol the symbol.
      * @param frequency the frequency.
      * @param code the binary code.
      */
     public HuffmanData( Character symbol, double frequency, String code ) {
         this( symbol, frequency );
         this.code = code;
     }
     
     /**
      * Comparison method considers frequency only.
      * 
      * @param n another instance.
      * 
      * @return a negative integer, zero, or a positive integer as 
      *  this object is less than, equal to, or greater than the 
      *  specified object.
      */
     public int compareTo( HuffmanData n ) {
         if ( this.frq > n.getFrequency() ) {
             return 1;
         }
         if ( this.frq == n.getFrequency() ) {
             return 0;
         }
         return -1;
     }
     
     /**
      * Equals predicate considers the symbol and frequency only.
      * 
      * @param o another object.
      * 
      * @return true if both the symbol and the frequency agree; false otherwise.
      */
     public boolean equals( Object o ) {
         if ( !(o instanceof HuffmanData) ) {
             return false;
         }
         HuffmanData hdOther = (HuffmanData)o;
         if ( this.frq != hdOther.getFrequency() ||
                 this.sym != hdOther.getSymbol() ) {
             return false;
         }
         return true;
     }
     
     /**
      * Access the code.
      * 
      * @return the code associated with the symbol.
      */
     public String getCode() {
         return this.code;
     }
     
     /**
      * Access the frequency.
      * 
      * @return the frequency of occurrence.
      */
     public double getFrequency() {
         return this.frq;
     }
     
     /**
      * Access the symbol.
      * 
      * @return the symbol.
      */
     public Character getSymbol() {
         return this.sym;
     }
     
     /**
      * Define hashcode for HuffmanData.
      * 
      * @return the hash code.
      */
     public int hashCode() {
         int symHash = this.sym != null? this.sym.hashCode() : 0;
         return symHash + 256 * (int)this.frq;
     }
     
     /**
      * Modify the frequency.
      * 
      * @param frequency the new frequency.
      */
     public void setFrequency( int frequency ) {
         this.frq = frequency;
     }
     
     /**
      * Modify the symbol.
      * 
      * @param symbol the new symbol.
      */
     public void setSymbol( Character symbol ) {
         this.sym = symbol;
     }
     
     /**
      * Modify the code.
      * 
      * @param code the new code.
      */
     public void setCode( String code ) {
         this.code = code;
     }
     
     /**
      * String representation of this object.
      * 
      * @return the string representing the object.
      */
     public String toString() {
        return "(" + this.sym + ", " + this.frq + ")";
     }
 }
