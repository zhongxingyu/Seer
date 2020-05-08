 package net.bodz.bas.io.resource.builtin;
 
 import java.io.IOException;
 import java.nio.CharBuffer;
 
 import net.bodz.bas.io.resource.AbstractStreamResource;
 import net.bodz.bas.sio.CharBufferCharIn;
 import net.bodz.bas.sio.CharBufferCharOut;
 import net.bodz.bas.sio.IByteIn;
 import net.bodz.bas.sio.IByteOut;
 import net.bodz.bas.sio.ICharIn;
 import net.bodz.bas.sio.ICharOut;
 import net.bodz.bas.sio.nio.DecodedByteOut;
 import net.bodz.bas.sio.nio.EncodedByteIn;
 
 public class CharArrayResource
         extends AbstractStreamResource {
 
     private final char[] array;
     private final int offset;
     private final int length;
 
     /**
      * @throws NullPointerException
      *             If <code>array</code> is <code>null</code>.
      * @throws IndexOutOfBoundsException
      *             If either <code>off</code> or <code>len</code> is out of range.
      */
     public CharArrayResource(char[] array, int offset, int length) {
         if (array == null)
             throw new NullPointerException("array");
        if (offset < 0 || offset >= array.length)
             throw new IndexOutOfBoundsException("Bad offset: " + offset);
        if (length < 0 || (offset + length) >= array.length)
             throw new IndexOutOfBoundsException("Bad length: " + length);
         this.array = array;
         this.offset = offset;
         this.length = length;
     }
 
     public char[] array() {
         return array;
     }
 
     public int arrayOffset() {
         return offset;
     }
 
     public int arrayLength() {
         return length;
     }
 
     @Override
     public ICharIn newCharIn()
             throws IOException {
         CharBuffer charBuffer = CharBuffer.wrap(array, offset, length);
         return new CharBufferCharIn(charBuffer);
     }
 
     @Override
     public ICharOut newCharOut()
             throws IOException {
         CharBuffer charBuffer = CharBuffer.wrap(array, offset, length);
         return new CharBufferCharOut(charBuffer);
     }
 
     @Override
     public IByteIn newByteIn()
             throws IOException {
         return new EncodedByteIn(newCharIn(), getCharset().newEncoder());
     }
 
     @Override
     public IByteOut newByteOut()
             throws IOException {
         return new DecodedByteOut(newCharOut(), getCharset().newDecoder());
     }
 
 }
