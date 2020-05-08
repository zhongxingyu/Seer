 package ar.edu.itba.pdc.duta.net;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.ReadableByteChannel;
 
import org.apache.log4j.Logger;

 
 public class BufferedReadableByteChannel {
 
	private static final Logger logger = Logger.getLogger(BufferedReadableByteChannel.class);

 	private ReadableByteChannel channel;
 
 	private final int capacity = 0x1000;
 
 	private ByteBuffer buffer = ByteBuffer.allocate(capacity);
 
 
 	public BufferedReadableByteChannel(ReadableByteChannel channel) {
 
 		this.channel = channel;
 
 		buffer.position(0);
 		buffer.limit(capacity);
 	}
 
 	public int read(ByteBuffer buffer) throws IOException {
 
 		int bytes = 0;
 
 		while (true) {
 
 			int oldLimit = -1;
 
 			if (this.buffer.remaining() > buffer.remaining()) {
 
 				oldLimit = this.buffer.limit();
 				this.buffer.limit(this.buffer.position() + buffer.remaining());
 			}
 
			bytes += this.buffer.remaining();
 			buffer.put(this.buffer);
 
 			if (oldLimit != -1) {
 
 				this.buffer.limit(oldLimit);
 				return bytes;
 			}
 
 			this.buffer.position(0);
 			this.buffer.limit(capacity);
 
 			int temp = channel.read(this.buffer);
			this.buffer.position(0);
 
 			if (temp <= 0) {
 
 				this.buffer.limit(0);

				if (bytes == 0) {
					return temp;
				}

 				return bytes;
 			}
 
 			this.buffer.limit(temp);
 		}
 	}
 }
