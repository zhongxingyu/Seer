 package com.v2soft.styxlib.library.io;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import com.v2soft.styxlib.library.StyxClientConnection;
 import com.v2soft.styxlib.library.StyxFile;
 import com.v2soft.styxlib.library.core.Messenger;
 import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
 import com.v2soft.styxlib.library.messages.StyxRReadMessage;
 import com.v2soft.styxlib.library.messages.StyxTReadMessage;
 import com.v2soft.styxlib.library.messages.base.StyxMessage;
 import com.v2soft.styxlib.library.types.ULong;
 
 /**
  * Unbuffered input styx output stream
  * @author mrco
  *
  */
 public class StyxUnbufferedInputStream extends InputStream {
     private long mTimeout = StyxClientConnection.DEFAULT_TIMEOUT;
     private byte[] mSingleByteArray = new byte[1];
     private StyxFile mFile;
     private Messenger mMessenger;
     private ULong mFileOffset = ULong.ZERO;
    private int mIOUnitSize;
 
    StyxUnbufferedInputStream(StyxFile file, Messenger messnger, int iounit) {
         if ( file == null ) throw new NullPointerException("File is null");
         if ( messnger == null ) throw new NullPointerException("messnger is null");
        mIOUnitSize = iounit;
         mFile = file;
         mMessenger = messnger;
     }
 
     public long getTimeout() {
         return mTimeout;
     }
 
     public void setTimeout(long mTimeout) {
         this.mTimeout = mTimeout;
     }
 
     @Override
     public int read(byte[] b, int off, int len) throws IOException {
        if ( len > mIOUnitSize ) {
            len = mIOUnitSize;
        }
         int readed = 0;
         try {
             // send Tread
             StyxTReadMessage tRead = new StyxTReadMessage(mFile.getFID(), mFileOffset, len);
             mMessenger.send(tRead);
             StyxMessage rMessage = tRead.waitForAnswer(mTimeout);
             StyxErrorMessageException.doException(rMessage);
 
             StyxRReadMessage rRead = (StyxRReadMessage) rMessage;
             readed = rRead.getDataLength();
             if ( readed > 0 ) {
                 System.arraycopy(rRead.getDataBuffer(), 0, b, 0, readed);
                 mFileOffset = mFileOffset.add(readed);
             } else {
                 readed = -1;
             }
         } catch (Exception e) {
             throw new IOException(e);
         }
         return readed;
     }
     @Override
     public int read(byte[] b) throws IOException {
         return read(b, 0, b.length);
     }
     @Override
     public int read() throws IOException {
         read(mSingleByteArray);
         return mSingleByteArray[0];
     }        
 }
