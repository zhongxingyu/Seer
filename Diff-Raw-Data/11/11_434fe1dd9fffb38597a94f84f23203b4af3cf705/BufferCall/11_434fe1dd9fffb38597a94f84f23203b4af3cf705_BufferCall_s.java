 package org.kari.call.event;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.zip.DataFormatException;
 import java.util.zip.Deflater;
 import java.util.zip.Inflater;
 
 import org.kari.call.CallType;
 import org.kari.call.CallUtil;
 import org.kari.call.Handler;
 import org.kari.call.io.BufferPool;
 import org.kari.call.io.DirectByteArrayOutputStream;
 
 /**
  * Remote call using intermediate buffer
  *
  * @author kari
  */
 public final class BufferCall extends ServiceCall {
     public static final int DEFAULT_COMPRESS_THRESHOLD = 500;
     /**
      * Expected compression level to pre-allocate buffers
      */
     public static final float ASSUMED_COMPRESS_RATIO = 1.4f;
     /**
      * block size for socket read: MAX == 8192
      */
     private static final int READ_BLOCK_SIZE = 8192;
 
     /**
      * For decoding call
      */
     public BufferCall() {
         super();
     }
 
     /**
      * @param pSessionId Identifies session for session authentication,
      * can be null
      * @param pParams null if no params
      */
     public BufferCall(
             Object pSessionId,
             boolean pSessionIdChanged,
             short pServiceUUID,
             short pMethodId,
             Object[] pParams)
     {
         super(pSessionId, pSessionIdChanged, pServiceUUID, pMethodId, pParams);
     }
 
     @Override
     public CallType getType() {
         return CallType.BUFFER_CALL;
     }
 
     @Override
     protected void write(Handler pHandler, DataOutputStream pOut)
         throws Exception
     {
         ObjectOutputStream oo = pHandler.createObjectOut();
         writeObjectOut(oo);
         pHandler.finishObjectOut(oo);
 
         writeBuffer(pHandler, pOut);
     }
 
     @Override
     protected void read(Handler pHandler, DataInputStream pIn)
         throws IOException,
             ClassNotFoundException
     {
         ObjectInputStream oi = readBuffer(pHandler, pIn);
         readObjectIn( oi );
         pHandler.finishObjectIn(oi);
     }
 
     /**
      * Write contents of {@link Handler#getByteOut()} into pOut
      */
     protected static void writeBuffer(
             final Handler pHandler,
             final DataOutputStream pOut)
             throws IOException
     {
         final byte[] data;
         final int totalCount;
         {
             final DirectByteArrayOutputStream bout = pHandler.getByteOut();
             data = bout.getBuffer();
             totalCount = bout.size();
         }
 
         boolean compressed = totalCount > pHandler.getCompressThreshold();
 
         if (compressed) {
             final BufferPool pool = pHandler.getBufferPool();
            byte[] out = pool.allocate(totalCount);
             try {
                 final Deflater deflater = pHandler.getDeflater();
 
                 deflater.setInput(data, 0,  totalCount);
                 deflater.finish();
 
                 int compressTotal = 0;
                 int offset = 0;
                 while (!deflater.finished()) {
                    // grow may occur only if compression ration is really bad
                    if (offset + Handler.BUFFER_SIZE > out.length) {
                        out = pool.grow(out, out.length + Handler.BUFFER_SIZE);
                    }

                     int count = deflater.deflate(out, offset, out.length - offset);
                     if (count > 0) {
                         compressTotal += count;
                         offset += count;
                     }
                 }
                 deflater.reset();
 
                 compressed = compressTotal < totalCount * 0.95;
                 if (compressed) {
                     pOut.writeBoolean(true);
                     CallUtil.writeCompactInt(pOut, compressTotal);
                     CallUtil.writeCompactInt(pOut, totalCount);
                     pOut.write(out, 0, compressTotal);
                 }
             } finally {
                 pool.release(out);
             }
         }
 
         if (!compressed) {
             pOut.writeBoolean(false);
             if (pHandler.isReuseObjectStream()) {
                 CallUtil.writeCompactInt(pOut, totalCount);
             }
             pOut.write(data, 0, totalCount);
         }
     }
 
     /**
      * Read data from pIn to buffer
      */
     protected static ObjectInputStream readBuffer(
             final Handler pHandler,
             final DataInputStream pIn)
         throws IOException
     {
         ObjectInputStream result;
 
         final boolean compressed = pIn.readBoolean();
 
 
         if (compressed) {
             final BufferPool pool = pHandler.getBufferPool();
             final int compressCount = CallUtil.readCompactInt(pIn);
             final int totalCount = CallUtil.readCompactInt(pIn);
 
             byte[] data = pool.allocate(compressCount);
             final DirectByteArrayOutputStream out = pHandler.getByteOut();
             try {
                 // read stream into "data"
                 {
                     int offset = 0;
                     int remaining = compressCount;
                     while (remaining > 0) {
                         final int count = pIn.read(
                                 data,
                                 offset,
                                 Math.min(
                                         READ_BLOCK_SIZE,
                                         remaining));
                         if (count > 0) {
                             remaining -= count;
                             offset += count;
                         }
                     }
                 }
 
                 // inflate "data" into "out"
                 {
                     final byte[] buffer = pHandler.getBuffer();
                     final Inflater inflater = pHandler.getInflater();
 
                     inflater.setInput(data, 0, compressCount);
                     out.reset();
                     out.ensureCapacity(totalCount);
 
                     while (!inflater.finished()) {
                         try {
                             int count = inflater.inflate(buffer, 0, buffer.length);
                             if (count > 0) {
                                 out.write(buffer, 0, count);
                             }
                         } catch (DataFormatException e) {
                             throw new IOException(e);
                         }
                     }
                     inflater.reset();
                 }
             } finally {
                 pool.release(data);
             }
             result = pHandler.createObjectIn(out.size());
         } else {
             if (pHandler.isReuseObjectStream()) {
                 // read stream into "data"
                 final int totalCount = CallUtil.readCompactInt(pIn);
                 final byte[] data = pHandler.prepareByteOut(totalCount);
 
                 int remaining = totalCount;
                 int offset = 0;
                 while (remaining > 0) {
                     final int count = pIn.read(
                             data,
                             offset,
                             Math.min(READ_BLOCK_SIZE, totalCount - offset));
                     if (count > 0) {
                         remaining -= count;
                         offset += count;
                     }
                 }
                 result = pHandler.createObjectIn(totalCount);
             } else {
                 result = pHandler.getIOFactory().createObjectInput(pIn);
             }
         }
 
         return result;
     }
 
     @Override
     protected Result createResult(Object pResult) {
         return new BufferResult(pResult);
     }
 
 }
