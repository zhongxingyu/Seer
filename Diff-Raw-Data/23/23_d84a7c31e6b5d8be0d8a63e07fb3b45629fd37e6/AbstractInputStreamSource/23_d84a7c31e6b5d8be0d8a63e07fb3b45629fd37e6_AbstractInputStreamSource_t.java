 package net.bodz.bas.io.res;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.nio.file.OpenOption;
 
 import net.bodz.bas.io.IByteIn;
 import net.bodz.bas.io.ICharIn;
 import net.bodz.bas.io.adapter.InputStreamByteIn;
 import net.bodz.bas.io.impl.DecodedCharIn;
 
 public abstract class AbstractInputStreamSource
         extends AbstractStreamInputSource {
 
     @Override
     protected abstract InputStream _newInputStream(OpenOption... options)
             throws IOException;
 
     @Override
     protected Reader _newReader(OpenOption... options)
             throws IOException {
         InputStream in = newInputStream(options);
         return new InputStreamReader(in, getCharset());
     }
 
     @Override
    protected IByteIn _newByteIn(OpenOption... options)
             throws IOException {
         InputStream in = newInputStream(options);
         return new InputStreamByteIn(in);
     }
 
     @Override
    protected ICharIn _newCharIn(OpenOption... options)
             throws IOException {
         IByteIn in = _newByteIn(options);
         return new DecodedCharIn(in, getCharset().newDecoder());
     }
 
 }
