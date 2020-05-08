 package edu.berkeley.icsi.cdfs.sharedmem;
 
 import java.io.EOFException;
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileChannel.MapMode;
 
 import edu.berkeley.icsi.cdfs.conf.ConfigConstants;
 import edu.berkeley.icsi.cdfs.utils.NumberUtils;
 
 public final class SharedMemoryProducer extends AbstractSharedMemoryComponent {
 
 	private RandomAccessFile memoryMappedFile = null;
 
 	private MappedByteBuffer sharedMemoryBuffer = null;
 
 	private boolean bufferReady;
 
 	public SharedMemoryProducer(final Socket socket) throws IOException {
 		super(socket);
 
 		this.bufferReady = true;
 	}
 
 	private ByteBuffer getBuffer() throws IOException {
 
 		if (this.memoryMappedFile == null) {
 
 			final File file = getTmpFile();
 
 			// Open file and send filename to consumer
 			this.memoryMappedFile = new RandomAccessFile(file, "rw");
 			final FileChannel fc = this.memoryMappedFile.getChannel();
 			this.sharedMemoryBuffer = fc.map(MapMode.READ_WRITE, 0, ConfigConstants.BUFFER_SIZE);
 			writeFilename(file.getAbsolutePath());
 		}
 
 		return this.sharedMemoryBuffer;
 	}
 
 	private static File getTmpFile() throws IOException {
 
		final File tmpFile = File.createTempFile("cdfs_", "_pipe.dat");
 		tmpFile.deleteOnExit();
 
 		return tmpFile;
 	}
 
 	public ByteBuffer lockSharedMemory() throws IOException {
 
 		if (!this.bufferReady) {
 			waitForACK();
 			this.bufferReady = true;
 		}
 
 		final ByteBuffer buf = getBuffer();
 		buf.clear();
 
 		return buf;
 	}
 
 	public void unlockSharedMemory() throws IOException {
 
 		if (this.sharedMemoryBuffer == null) {
 			throw new IllegalStateException("Shared memory buffer has never been locked before");
 		}
 
 		this.sharedMemoryBuffer.flip();
 
 		// Write buffer size
 		writeBufferSize(this.sharedMemoryBuffer.limit());
 		this.bufferReady = false;
 	}
 
 	private void writeBufferSize(final int bufferSize) throws IOException {
 
 		final byte[] buf = new byte[4];
 		NumberUtils.integerToByteArray(bufferSize, buf, 0);
 		this.outputStream.write(buf);
 	}
 
 	private void waitForACK() throws IOException {
 
 		final int r = this.inputStream.read();
 		if (r < 0) {
 			throw new EOFException();
 		}
 
 		if (r != ACK_BYTE) {
 			throw new IOException("Received unexpected value for acknowlegment");
 		}
 	}
 
 	private void writeFilename(final String filename) throws IOException {
 
 		final byte[] lenBuf = new byte[4];
 		final byte[] filenameBuf = filename.getBytes();
 		NumberUtils.integerToByteArray(filenameBuf.length, lenBuf, 0);
 		this.outputStream.write(lenBuf);
 		this.outputStream.write(filenameBuf);
 	}
 
 	public void close() throws IOException {
 
 		this.memoryMappedFile.close();
 		super.close();
 	}
 }
