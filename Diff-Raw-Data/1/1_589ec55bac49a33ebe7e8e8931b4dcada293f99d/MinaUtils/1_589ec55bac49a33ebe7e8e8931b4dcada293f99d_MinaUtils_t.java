 package org.lastbamboo.common.util.mina;
 
 import java.nio.charset.CharacterCodingException;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.mina.common.ByteBuffer;
 import org.apache.mina.common.DefaultIoFilterChainBuilder;
 import org.apache.mina.common.IoServiceConfig;
 import org.apache.mina.common.IoServiceListener;
 import org.apache.mina.common.ThreadModel;
 import org.apache.mina.filter.codec.ProtocolCodecFactory;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.filter.executor.ExecutorFilter;
 import org.apache.mina.transport.socket.nio.SocketAcceptor;
 import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Apache MINA utility functions.
  */
 public class MinaUtils
     {
     
     private static final Logger LOG = LoggerFactory.getLogger(MinaUtils.class);
     
     private static final CharsetDecoder DECODER =
         Charset.forName("US-ASCII").newDecoder();
 
     /**
      * Useful for debugging.  Turns the given buffer into an ASCII string.
      * 
      * @param buf The buffer to convert to a string.
      * @return The string.
      */
     public static String toAsciiString(final ByteBuffer buf)
         {
         DECODER.reset();
         final int position = buf.position();
         final int limit = buf.limit();
         try
             {
             final String bufString = buf.getString(DECODER);
             buf.position(position);
             buf.limit(limit);
             return bufString;
             }
         catch (final CharacterCodingException e)
             {
             LOG.error("Could not decode: "+buf, e);
             return StringUtils.EMPTY;
             }
         }
 
     /**
      * Copies the specified buffer to a byte array.
      * 
      * @param buf The buffer to copy.
      * @return The byte array.
      */
     public static byte[] toByteArray(final ByteBuffer buf)
         {
         final byte[] bytes = new byte[buf.remaining()];
         buf.get(bytes);
         return bytes;
         }
     
     /**
      * Splits the specified <code>ByteBuffer</code> into smaller 
      * <code>ByteBuffer</code>s of the specified size.  The remaining bytes 
      * in the buffer must be greater than the chunk size.  This method will
      * create smaller buffers of the specified size until there are fewer 
      * remaining bytes than the chunk size, when it will simply add a buffer
      * the same size as the number of bytes remaining.
      * 
      * @param buffer The <code>ByteBuffer</code> to split.
      * @param chunkSize The size of the smaller buffers to create.
      * @return A <code>Collection</code> of <code>ByteBuffer</code>s of the
      * specified size.  The final buffer in the <code>Collection</code> will
      * have a size > 0 and <= the chunk size.
      */
     public static Collection<ByteBuffer> split(final ByteBuffer buffer, 
         final int chunkSize)
         {
         final Collection<ByteBuffer> buffers = new LinkedList<ByteBuffer>();
         final int originalLimit = buffer.limit();
         
         int totalSent = 0;
         while ((totalSent + chunkSize) < originalLimit)
             {
             buffer.limit(totalSent + chunkSize);
             buffers.add(createBuffer(buffer));            
             totalSent += chunkSize;
             }
         
         // Send any remaining bytes.
         buffer.limit(originalLimit);
         buffers.add(createBuffer(buffer));
         return buffers;
         }
     
     private static ByteBuffer createBuffer(final ByteBuffer buffer)
         {
         // We calculate this here because the final split buffer will not
         // necessarily have a size equal to the chunk size -- it will
         // usually be smaller.
         final ByteBuffer data = ByteBuffer.allocate(
             buffer.limit() - buffer.position());
         
         data.put(buffer);
         data.flip();
         return data;
         }
 
     /**
      * Splits the specified <code>ByteBuffer</code> into smaller 
      * <code>ByteBuffer</code>s of the specified size.  The remaining bytes 
      * in the buffer must be greater than the chunk size.  This method will
      * create smaller buffers of the specified size until there are fewer 
      * remaining bytes than the chunk size, when it will simply add a buffer
      * the same size as the number of bytes remaining.
      * 
      * @param buffer The <code>ByteBuffer</code> to split.
      * @param chunkSize The size of the smaller buffers to create.
      * @return A <code>Collection</code> of <code>ByteBuffer</code>s of the
      * specified size.  The final buffer in the <code>Collection</code> will
      * have a size > 0 and <= the chunk size.
      */
     public static Collection<byte[]> splitToByteArrays(final ByteBuffer buffer, 
         final int chunkSize)
         {
         final Collection<byte[]> buffers = new LinkedList<byte[]>();
         final int originalLimit = buffer.limit();
         
         int totalSent = 0;
         while ((totalSent + chunkSize) < originalLimit)
             {
             buffer.limit(totalSent + chunkSize);
             
             // This will just read up to the limit.
             buffers.add(toByteArray(buffer));            
             totalSent += chunkSize;
             }
         
         // Send any remaining bytes.
         buffer.limit(originalLimit);
         buffers.add(toByteArray(buffer));
         return buffers;
         }
 
     /**
      * Puts an unsigned byte into the buffer.
      * 
      * @param bb The buffer.
      * @param value The value to insert.
      */
     public static void putUnsignedByte(final ByteBuffer bb, final int value)
         {
         bb.put((byte) (value & 0xff));
         }
 
     /**
      * Puts an unsigned byte into the buffer.
      * 
      * @param bb The buffer.
      * @param position The index in the buffer to insert the value.
      * @param value The value to insert.
      */
     public static void putUnsignedByte(final ByteBuffer bb, final int position, 
         final int value)
         {
         bb.put(position, (byte) (value & 0xff));
         }
 
     /**
      * Puts an unsigned byte into the buffer.
      * 
      * @param bb The buffer.
      * @param value The value to insert.
      */
     public static void putUnsignedShort(final ByteBuffer bb, final int value)
         {
         bb.putShort((short) (value & 0xffff));
         }
 
     /**
      * Puts an unsigned byte into the buffer.
      * 
      * @param bb The buffer.
      * @param position The index in the buffer to insert the value.
      * @param value The value to insert.
      */
     public static void putUnsignedShort(final ByteBuffer bb, final int position, 
         final int value)
         {
         bb.putShort(position, (short) (value & 0xffff));
         }
 
     /**
      * Puts an unsigned byte into the buffer.
      * 
      * @param bb The buffer.
      * @param value The value to insert.
      */
     public static void putUnsignedInt(final ByteBuffer bb, final long value)
         {
         bb.putInt((int) (value & 0xffffffffL));
         }
 
     /**
      * Puts an unsigned byte into the buffer.
      * 
      * @param bb The buffer.
      * @param position The index in the buffer to insert the value.
      * @param value The value to insert.
      */
     public static void putUnsignedInt(final ByteBuffer bb, final int position, 
         final long value)
         {
         bb.putInt(position, (int) (value & 0xffffffffL));
         }
 
     /**
      * Utility method for creating a TCP acceptor with our default 
      * configuration.
      * 
      * @param codecFactory The codec factory to use with the acceptor.
      * @param ioServiceListener The listener for IO service events.
      * @return The new acceptor.
      */
     public static SocketAcceptor createTcpAcceptor(
         final ProtocolCodecFactory codecFactory, 
         final IoServiceListener ioServiceListener)
         {
         final Executor threadPool = Executors.newCachedThreadPool();
         final SocketAcceptor acceptor = new SocketAcceptor(
             Runtime.getRuntime().availableProcessors() + 1, threadPool);
         final IoServiceConfig acceptorConfig = acceptor.getDefaultConfig();
         acceptorConfig.setThreadModel(ThreadModel.MANUAL);
         
         final SocketAcceptorConfig cfg = new SocketAcceptorConfig();
         
         // Just hoping this method does what it sounds like it does.
         cfg.setDisconnectOnUnbind(true);
         
         // Not sure why we do this, but almost all the MINA examples set 
         // acceptors to reuse the address.
         cfg.getSessionConfig().setReuseAddress(true);
         
         acceptor.addListener(ioServiceListener);
         
         final DefaultIoFilterChainBuilder filterChainBuilder = 
             cfg.getFilterChain();
         filterChainBuilder.addLast("codec", 
             new ProtocolCodecFilter(codecFactory));
         filterChainBuilder.addLast("threadPool", 
             new ExecutorFilter(Executors.newCachedThreadPool()));
        acceptor.setDefaultConfig(cfg);
         return acceptor;
         }
     }
