 package com.v2soft.styxlib.library.io;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 
 import com.v2soft.styxlib.library.StyxFile;
 import com.v2soft.styxlib.library.core.Messenger;
 
 /**
  * 
  * @author V.Shcriyabets (vshcryabets@gmail.com)
  *
  */
 public class StyxFileBufferedInputStream extends BufferedInputStream {
     private StyxFile mFile;
 
     /**
      * 
      * @param messenger
      * @param file
      * @param iounit
      * @param autocloseFile close StyxFile object with this stream
      */
     public StyxFileBufferedInputStream(Messenger messenger, 
             StyxFile file, 
             int iounit, 
             boolean autocloseFile) {
        super(new StyxUnbufferedInputStream(file, messenger), iounit);
         if ( autocloseFile ) {
             mFile = file;
         }
     }
 
     @Override
     public void close() throws IOException {
         if ( mFile != null ) {
             mFile.close();
         }
         super.close();
     }
 }
