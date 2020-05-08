 package us.exultant.ahs.io;
 
 import us.exultant.ahs.core.*;
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
  *                the type of message we write. {@link java.nio.ByteBuffer} is a common
  *                choice.
  */
 public interface ChannelWriter<$MSG> {
 	/**
 	 * <p>
 	 * Writes a message from a byte channel based on this writer's concept of a
 	 * message frame.
 	 * </p>
 	 * 
 	 * <p>
 	 * This method may fail to write all of the bytes of a message in a single
 	 * invocation, in which case the writer shall buffer that message and will attempt
 	 * to finish writing it on future calls. In this case, the caller should not call
 	 * the method with a new piece of data until the previous piece has been flushed
 	 * (if the caller tries to shove too much data in, the behavior may be undefined).
 	 * Look at whether the call returns true or false to determine whether the writing
 	 * process is hitting a need to buffer.
 	 * </p>
 	 * 
 	 * @param $channel
 	 * @return true if the entire last piece of data we were given has been written
 	 *         and you can give us another one; false if we still have more to do in a
 	 *         future call.
 	 * @throws IOException
 	 *                 if the problems are encountered while trying to write to the
 	 *                 channel; this is probably a fairly fatal scenario for this
 	 *                 channel.
 	 * @throws TranslationException
 	 *                 if a message was corrupt or nonsensical.
 	 */
 	public boolean write(WritableByteChannel $channel, $MSG $data) throws TranslationException, IOException;
 	
 	
 	
 	//public static class NoopFramer implements ChannelWriter<$MSG> {}
 
 	
 	
 	//public static class LinedFramer implements ChannelWriter<$MSG> {}
 	
 	
 	
 	//public static class IfsFramer implements ChannelWriter<$MSG> {}	// well that would be an odd name, if symmetrical.  but yeah, probs wrong.
 	
 	
 	
 	/**
 	 * <p>
 	 * Reads messages framed by the binary length delimiters: the bytes that are read
 	 * from the channel look like a series of 4-byte signed integer lengths, followed
 	 * by an arbitrary blob of the size specified by the int. The size bytes are
 	 * discarded after being used to determine the blob, and the blob is wrapped in a
 	 * ByteBuffer and returned as the result of the translation.
 	 * </p>
 	 * 
 	 * @author Eric Myhre <tt>hash@exultant.us</tt>
 	 */
 	public static class BinaryFramer implements ChannelWriter<ByteBuffer> {
 		private final ByteBuffer $preint = ByteBuffer.allocate(4);
 		private ByteBuffer $blob;	// i don't think i should need to put volatile on this since even though a work target can be powered from different threads over time, there's enough locks around somewhere in that handoff that this should already be safe.
 		
 		public boolean write(WritableByteChannel $channel, ByteBuffer $data) throws IOException, TranslationException {
 			if ($data != null) {
 				if ($blob != null) throw new IllegalStateException("you may not push more data into a ChannelWriter that already has data in a buffer!");
 				$blob = $data;
 				/* I trust that you've already $blob.rewind() if that's what you want. */
 				$preint.putInt($blob.remaining());
 				$preint.rewind();
 			}
 			
 			if ($preint.remaining() > 0) $channel.write($preint);	/* we might have done this already in a previous invocation, but so?  the buffer remembers how much is remaining. */
 			/* ohgod: even if you're treating this channel single-threadedly in the jvm, 
 			 * the *kernel* is still draining at random, so you can't just happily jump 
 			 * onto trying to write the blob and assume that if the preint didn't finish 
 			 * writing then the blob will also fail properly. */
 			if ($preint.remaining() > 0) return false;
 			$channel.write($blob);
 			if ($blob.remaining() == 0) {
 				$blob = null;
 				return true;
			} else return false;
 		}
 	}
 	
 	
 	
 	//public static class HttpRequestFramer implements ChannelWriter<$MSG> {}
 	
 	
 	
 	//public static class HttpResponseFramer implements ChannelWriter<$MSG> {}
 }
