 package us.exultant.ahs.io;
 
 import us.exultant.ahs.core.*;
 import us.exultant.ahs.util.*;
 import java.io.*;
 import java.nio.*;
 import java.nio.channels.*;
 
 /**
  * Interface which is implemented at the bottom of an IO stack and is responsible for
  * framing individual messages. For example, there are implementations of this that do
  * line-break delimited frames, literal binary "frames", implementations that produce HTTP
  * requests or responses, implementations that do a naked passthru, etc.
  * 
  * @author Eric Myhre <tt>hash@exultant.us</tt>
  * 
  * @param <$MSG>
  *                the type of message we accept. {@link java.nio.ByteBuffer} is a common
  *                choice.
  */
 public interface ChannelReader<$MSG> {
 	/**
 	 * <p>
	 * Reads a message from a byte channel based on this reader's concept of a message
	 * frame.
 	 * </p>
 	 * 
 	 * <p>
 	 * This method either returns an object of the message type, or it may return null
 	 * for any given invocation if there is not immediately (i.e. in a nonblocking
 	 * sense) sufficient readable data to produce a full message.
 	 * </p>
 	 * 
 	 * @param $channel
 	 * @return a message or null
 	 * @throws IOException
 	 *                 if the problems are encountered while trying to read from the
 	 *                 channel; this is probably a fairly fatal scenario for this
 	 *                 channel.
 	 * @throws TranslationException
 	 *                 if a message was corrupt or nonsensical. Depending on the
 	 *                 protocol framing type, future messages on the same channel may
 	 *                 still be readable.
 	 */
 	public $MSG read(ReadableByteChannel $channel) throws IOException, TranslationException;
 	
 	
 	
	//public static class NoopFramer implements ChannelReader<$MSG> {}
 
 	
 	
 	//public static class LinedFramer implements ChannelReader<$MSG> {}
 	
 	
 	
 	//public static class IfsFramer implements ChannelReader<$MSG> {}
 	
 	
 	
 	/**
 	 * <p>
 	 * Writes messages framed by the binary length delimiters: the bytes that are
 	 * written to the channel look like a series of 4-byte signed integer lengths,
 	 * followed by an arbitrary blob of the size specified by the int.
 	 * </p>
 	 * 
 	 * @author Eric Myhre <tt>hash@exultant.us</tt>
 	 */
 	public static class BinaryFramer implements ChannelReader<ByteBuffer> {
 		private final ByteBuffer	$preint	= ByteBuffer.allocate(4);
 		private int			$messlen = -1;
 		private ByteBuffer		$mess;
 		
 		public ByteBuffer read(ReadableByteChannel $base) throws IOException, TranslationException {
 			if ($messlen < 0) {
 				// try to read enough info to figure out what length of message we expect
 				try {
 					$base.read($preint);
 				} catch (ClosedChannelException $e) {
 					/* this is the one place a binary frame protocol for a smooth shutdown to be legal... the pump should just notice the channel being closed before next time around. */
 					if ($preint.remaining() == 4) return null;
 					throw new TranslationException("malformed babble -- partial message length header read before unexpected EOF", $e);
 				} /* any other IOException is a channel break. */
 				
 				if ($preint.remaining() > 0) return null; // don't have a size header yet.  keep waiting for more data.
 				$messlen = Primitives.intFromByteArray($preint.array());
 				$preint.rewind();
 				if ($messlen < 1) throw new TranslationException("malformed babble -- message length header not positive");
 				$mess = ByteBuffer.allocate($messlen);
 			} else if ($messlen == 0) {
 				// i don't know who sends empty chunks, but okay...
 				$messlen = -1;
 			}
 			// if procedure gets here, we either had messlen state from the last round or we have it now.
 			
 			// get the message (or at least part of it, if possible)
 			$base.read($mess);	/* an exception here is a channel break. */
 			
 			if ($mess.remaining() > 0) return null; // we just don't have as much information as this chunk should contain yet.  keep waiting for more data.
 			
 			$messlen = -1;
 			$mess.rewind();
 			return $mess;
 		}
 	}
 	
 	
 	
 	//public static class HttpRequestFramer implements ChannelReader<$MSG> {}
 	
 	
 	
 	//public static class HttpResponseFramer implements ChannelReader<$MSG> {}
 }
