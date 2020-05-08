 package net.bodz.bas.io.typemeta;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 
 import net.bodz.bas.err.ParseException;
 import net.bodz.bas.sio.IByteOut;
 import net.bodz.bas.sio.OutputStreamByteOut;
 import net.bodz.bas.traits.AbstractCommonTraits;
 import net.bodz.bas.traits.IParser;
 
 public class ByteOutTraits
         extends AbstractCommonTraits<IByteOut> {
 
     public ByteOutTraits() {
         super(IByteOut.class);
     }
 
     @Override
     protected Object query(int traitsIndex) {
        if (traitsIndex == IParser.traitsIndex)
             return this;
         return null;
     }
 
     @Override
     public IByteOut parse(String path)
             throws ParseException {
         FileOutputStream out;
         try {
             out = new FileOutputStream(path);
         } catch (FileNotFoundException e) {
             throw new RuntimeException(e.getMessage(), e);
         }
         return new OutputStreamByteOut(out);
     }
 
 }
