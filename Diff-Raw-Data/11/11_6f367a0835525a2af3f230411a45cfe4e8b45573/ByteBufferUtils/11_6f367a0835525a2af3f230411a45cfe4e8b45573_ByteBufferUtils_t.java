 /*
  ** 2013 December 5
  **
  ** The author disclaims copyright to this source code.  In place of
  ** a legal notice, here is a blessing:
  **    May you do good and not evil.
  **    May you find forgiveness for yourself and forgive others.
  **    May you share freely, never taking more than you give.
  */
 package info.ata4.util.io;
 
 import java.nio.ByteBuffer;
 
 /**
  * ByteBuffer utility class.
  * 
  * @author Nico Bergemann <barracuda415 at yahoo.de>
  */
 public class ByteBufferUtils {
    
    private static final ByteBuffer EMPTY = ByteBuffer.allocate(0);
 
     private ByteBufferUtils() {
     }
     
     public static ByteBuffer getSlice(ByteBuffer bb, int offset, int length) {
        if (length == 0) {
            // very funny
            return EMPTY;
        }
        
         // get current position and limit
         int pos = bb.position();
         int limit = bb.limit();
         
         bb.position(offset);
         
         // set new limit if length is provided, use current limit otherwise
         if (length > 0) {
             bb.limit(offset + length);
         }
         
         // do the actual slicing
         ByteBuffer bbSlice = bb.slice();
         
         // restore original limit and position
         bb.limit(limit);
         bb.position(pos);
         
         return bbSlice;
     }
     
     public static ByteBuffer getSlice(ByteBuffer bb, int offset) {
        return getSlice(bb, offset, -1);
     }
 }
