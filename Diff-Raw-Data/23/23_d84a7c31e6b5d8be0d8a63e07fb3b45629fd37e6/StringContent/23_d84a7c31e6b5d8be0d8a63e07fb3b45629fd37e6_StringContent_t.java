 package net.bodz.bas.std.rfc.mime;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 
 import net.bodz.bas.err.UnexpectedException;
 import net.bodz.bas.io.res.IStreamInputSource;
 import net.bodz.bas.io.res.builtin.CharArrayResource;
 
 public class StringContent
         extends AbstractContent {
 
     private static final long serialVersionUID = 1L;
 
     private String string;
     private transient byte[] bytes;
 
     public StringContent(String string) {
         this(ContentTypes.text_plain, string);
     }
 
     public StringContent(ContentType contentType, String string) {
         super(contentType);
         if (string == null)
             throw new NullPointerException("string");
         this.string = string;
     }
 
     public synchronized byte[] getBytes() {
         if (bytes == null)
             try {
                 bytes = string.getBytes(getCharset());
             } catch (UnsupportedEncodingException e) {
                 throw new UnexpectedException(e.getMessage(), e);
             }
         return bytes;
     }
 
     @Override
     public Long getContentLength() {
         byte[] bytes = getBytes();
         return (long) bytes.length;
     }
 
     @Override
     public IStreamInputSource getInputSource(Charset charset) {
        CharArrayResource res = new CharArrayResource(string.toCharArray());
        res.setCharset(charset);
        return res;
     }
 
 }
