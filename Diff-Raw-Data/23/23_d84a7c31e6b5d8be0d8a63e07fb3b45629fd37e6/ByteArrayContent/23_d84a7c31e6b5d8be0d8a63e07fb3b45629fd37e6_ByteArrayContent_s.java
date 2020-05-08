 package net.bodz.bas.std.rfc.mime;
 
 import java.nio.charset.Charset;
 
 import net.bodz.bas.io.res.IStreamInputSource;
 import net.bodz.bas.io.res.builtin.ByteArrayResource;
 
 public class ByteArrayContent
         extends AbstractContent {
 
     private static final long serialVersionUID = 1L;
 
     private byte[] array;
     private int off;
     private int len;
 
     public ByteArrayContent(byte[] array) {
         this(ContentTypes.application_octet_stream, array, 0, array.length);
     }
 
     public ByteArrayContent(byte[] array, int off, int len) {
         this(ContentTypes.application_octet_stream, array, off, len);
     }
 
     public ByteArrayContent(ContentType contentType, byte[] array) {
         this(contentType, array, 0, array.length);
     }
 
     public ByteArrayContent(ContentType contentType, byte[] array, int off, int len) {
         super(contentType);
 
         if (array == null)
             throw new NullPointerException("array");
         if (off < 0 || off > array.length)
             throw new IllegalArgumentException("off out of bound: " + off);
         if (len < 0 || len > array.length - off)
             throw new IllegalArgumentException("length out of bound: " + len);
 
         this.array = array;
         this.off = off;
         this.len = len;
     }
 
     @Override
     public Long getContentLength() {
         return (long) len;
     }
 
     @Override
     public IStreamInputSource getInputSource(Charset charset) {
        return new ByteArrayResource(array, off, len);
     }
 
 }
