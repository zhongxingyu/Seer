 package de.skuzzle.polly.parsing;
 
 import java.io.Serializable;
 
 /**
  * Represents a span within a one-lined String. It consists of a inclusive 
  * start position and an inclusive end position.
  * 
  * This means that for a Position that spans one character, the start index equals 
  * the end index.
  * 
  * For convenience, a Position has methods to retrieve substrings of a given String:
  * {@link #prefix(String)} which returns a String which consists of all characters 
  * berfore this Position.
  * {@link #substring(String)} which returns exactly the String which Position this
  * object represents and {@link #postfix(String)} which returns a String consisting of
  * all characters which follow of this span.   
  * 
  * @author Simon
  */
 public class Position implements Serializable {
     
     private static final long serialVersionUID = 1L;
 
     public final static Position EMPTY = new Position(0, 1);
     
     private int start;
     private int end;
     
     
     /**
      * Creates a new Position Object with given start and end index.
      * 
      * @param start The inclusive start index of this Position.
      * @param end The inclusive end index of this postion.
      */
     public Position(int start, int end) {
         this.start = start;
         this.end = end;
     }
     
     
     
     /**
      * Creates a new Position using the positions of the given tokens. The new
      * position will span from the start of the left token until the end of the 
      * right token.
      * 
      * @param left The left token.
      * @param right The right token.
      */
     public Position(Token left, Token right) {
         this(left.getPosition(), right.getPosition());
     }
     
     
     
     /**
      * Creates a new Position using the start and end indizes of the given positions.
      * The new Position will span from the start of the left position until the end of
      * the right position.
      * 
      * @param left The left position.
      * @param right The right position.
      */
     public Position(Position left, Position right) {
         this(left.getStart(), right.getEnd());
     }
     
     
     /**
      * Returns the starting index of this Position.
      * @return The inclusive start index.
      */
     public int getStart() {
         return this.start;
     }
     
     
     
     /**
      * Returns the end index of this Position.
      * @return The inclusive end index.
      */
     public int getEnd() {
         return this.end;
     }
     
     
     
     public int getWidth() {
         return this.end - this.start + 1;
     }
     
     
     
     /**
      * Returns a prefix of the given String consisting of all characters with index
      * lower than {@link #start}.
      * 
      * @param original The String in which this object represents a Position.
      * @return A substring of the original String.
      * @throws IllegalArgumentException If the original String is too short.
      */
     public String prefix(String original) {
         if (this.start > original.length()) {
             throw new IllegalArgumentException("Original String is too short!");
         }
         return original.substring(0, this.start);
     }
     
     
     
     public String postfix(String original) {
         if (this.start == this.end && this.start == original.length()) {
             return "";
         }
         return original.substring(this.end + 1);
     }
     
     
     
     public String substring(String original) {
         if (this.start == this.end && this.start == original.length()) {
             return " ";
         }
         return original.substring(this.start, this.end + 1);
     }
     
     
     
     public String mark(String original) {
         return this.mark(original, "4\u001F", "\u001F");
     }
     
     
     
     public String mark(String original, String begin, String end) {
         StringBuilder result = new StringBuilder(original.length() + 19);
 
         result.append(this.prefix(original));
         result.append(begin);
         result.append(this.substring(original));
         result.append(end);
         result.append(this.postfix(original));
         return result.toString();
     }
     
     
     
     @Override
     public String toString() {
         if (this.start == this.end) {
             return Integer.toString(this.start);
         }
         return (this.start) + "-" + (this.end);
     }
     
     
     
     @Override
     public boolean equals(Object obj) {
         if (obj == this) {
             return true;
        } else if (obj == null) {
            return false;
         }
         
         if (obj.getClass() != this.getClass()) {
             return false;
         }
         
         Position other = (Position) obj;
         return this.start == other.start && this.end == other.end;
     }
 }
