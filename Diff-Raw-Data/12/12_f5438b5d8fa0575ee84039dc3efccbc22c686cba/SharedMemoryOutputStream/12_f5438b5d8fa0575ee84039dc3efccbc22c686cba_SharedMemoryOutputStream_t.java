 package edu.berkeley.icsi.cdfs;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import edu.berkeley.icsi.cdfs.sharedmem.SharedMemoryProducer;
 
 final class SharedMemoryOutputStream extends OutputStream {
 
 	private static final Log LOG = LogFactory.getLog(SharedMemoryOutputStream.class);
 
 	private final Socket socket;
 
 	private SharedMemoryProducer smp = null;
 
 	private ByteBuffer sharedMemoryBuffer = null;
 
 	private long totalWritten = 0L;
 
 	SharedMemoryOutputStream(final Socket socket) throws IOException {
 		this.socket = socket;
 	}
 
 	@Override
 	public void write(int b) throws IOException {
 
 		throw new UnsupportedOperationException("Write 1");
 	}
 
 	@Override
 	public void close() throws IOException {
 
 		if (this.sharedMemoryBuffer != null) {
 			if (this.sharedMemoryBuffer.position() != 0) {
 				this.totalWritten += this.sharedMemoryBuffer.position();
 				this.smp.unlockSharedMemory();
 			}
 		}
 
		if (this.smp == null) {
			// No data has been written, but we lock/unlock the shared buffer once to establish communication anyway
			this.smp = new SharedMemoryProducer(this.socket);
			this.smp.lockSharedMemory();
			this.smp.unlockSharedMemory();
 		}
 
		this.smp.close();

 		LOG.info("Wrote " + this.totalWritten + " bytes to output stream");
 	}
 
 	@Override
 	public void flush() {
 
 		throw new UnsupportedOperationException("Flush");
 	}
 
 	@Override
 	public void write(final byte[] b) throws IOException {
 
 		write(b, 0, b.length);
 	}
 
 	@Override
 	public void write(final byte[] b, final int off, final int len) throws IOException {
 
 		if (this.smp == null) {
 			this.smp = new SharedMemoryProducer(this.socket);
 		}
 
 		int written = 0;
 		while (written < len) {
 
 			if (this.sharedMemoryBuffer == null) {
 				this.sharedMemoryBuffer = this.smp.lockSharedMemory();
 			}
 			final int bytesToWrite = Math.min(this.sharedMemoryBuffer.remaining(), len - written);
 
 			this.sharedMemoryBuffer.put(b, off + written, bytesToWrite);
 			written += bytesToWrite;
 
 			if (!this.sharedMemoryBuffer.hasRemaining()) {
 				this.totalWritten += this.sharedMemoryBuffer.capacity();
 				this.smp.unlockSharedMemory();
 				this.sharedMemoryBuffer = null;
 			}
 		}
 	}
 }
