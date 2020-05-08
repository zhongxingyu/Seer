 package awesome.tony.fastfind;
 
import java.io.ByteArrayInputStream;
import java.io.InputStream;
 import java.util.Arrays;
 
 /**
  * This class is immutable and thread safe.  It serves as a wrapper for byte[]
  * search terms.
  * @author
  */
 public class SearchTerm {
 
     private final byte[] term;
     private final int hashCode;
 
     /**
      * Constructs a search term wrapping the given byte array
      * @param term 
      */
     public SearchTerm(final byte[] term) {
         if(term == null || term.length == 0){
             throw new IllegalArgumentException();
         }
         this.term = Arrays.copyOf(term, term.length);
         this.hashCode = Arrays.hashCode(this.term);
     }
     
     /**
      * @return a defensive copy of the byte array of this term
      */
     public byte[] getTerm(){
         return Arrays.copyOf(term, term.length);
     }
     
     /**
      * @param index
      * @return integer representing the unsigned byte at the given index
      */
     public int get(final int index){
         return (term[index] & 0xff);
     }
     
     /**
      * @return size (in bytes) of the underlying byte array
      */
     public int size(){
         return term.length;
     }
 
     /**
      * @return the hash code comprised of the hash code of the underlying array
      */
     @Override
     public int hashCode() {
         return hashCode;
     }
 
     /**
      * Ensures the two objects are of the same type and have the same underlying byte array contents
      * @param obj
      * @return true if object is same type and has the same contents; false otherwise
      */
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final SearchTerm other = (SearchTerm) obj;
         if (this.hashCode != other.hashCode) {
             return false;
         }
         return Arrays.equals(this.term, other.term);
     }
     
     /**
      * @return String representation of the underlying byte array in the platform
      * default character set.
      */
     @Override
     public String toString(){
         return new String(term);
     }
 }
