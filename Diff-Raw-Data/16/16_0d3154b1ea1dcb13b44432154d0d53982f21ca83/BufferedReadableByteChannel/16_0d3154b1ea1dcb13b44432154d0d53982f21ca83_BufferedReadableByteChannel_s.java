 package ar.edu.itba.pdc.duta.net;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.ReadableByteChannel;
 
 
 public class BufferedReadableByteChannel {
 
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
 
			bytes += buffer.capacity();
 			buffer.put(this.buffer);
 
 			if (oldLimit != -1) {
 
 				this.buffer.limit(oldLimit);
 				return bytes;
 			}
 
 			this.buffer.position(0);
 			this.buffer.limit(capacity);
 
 			int temp = channel.read(this.buffer);
 
 			if (temp <= 0) {
 
 				this.buffer.limit(0);
 				return bytes;
 			}
 
 			this.buffer.limit(temp);
 		}
 	}
 }
