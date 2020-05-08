 package edu.berkeley.icsi.cdfs.sharedmem;
 
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileChannel.MapMode;
 
 import edu.berkeley.icsi.cdfs.conf.ConfigConstants;
 import edu.berkeley.icsi.cdfs.utils.NumberUtils;
 
 public final class SharedMemoryConsumer extends AbstractSharedMemoryComponent {
 
 	private final RandomAccessFile memoryMappedFile;
 
 	private final MappedByteBuffer sharedMemoryBuffer;
 
 	private boolean bufferReady = false;
 
 	public SharedMemoryConsumer(final Socket socket) throws IOException {
 		super(socket);
 
 		// Receive information about memory mapped file
 		final String filename = readFilename();
 
 		this.memoryMappedFile = new RandomAccessFile(filename, "r");
 		final FileChannel fc = this.memoryMappedFile.getChannel();
 
 		this.sharedMemoryBuffer = fc.map(MapMode.READ_ONLY, 0, ConfigConstants.BUFFER_SIZE);
 	}
 
 	public ByteBuffer lockSharedMemory() throws IOException {
 
 		if (!this.bufferReady) {
 			final int bufferSize = readBufferSize();
 			this.sharedMemoryBuffer.position(0);
 			this.sharedMemoryBuffer.limit(bufferSize);
 			this.bufferReady = true;
 		}
 
 		return this.sharedMemoryBuffer;
 	}
 
 	public void unlockSharedMemory() throws IOException {
 
 		this.bufferReady = false;
 		sendACK();
 	}
 
 	private void sendACK() throws IOException {
 
 		this.outputStream.write(ACK_BYTE);
 	}
 
 	private int readBufferSize() throws IOException {
 
 		final byte[] buf = new byte[4];
 		readFully(buf, buf.length);
 
 		return NumberUtils.byteArrayToInteger(buf, 0);
 	}
 
 	private String readFilename() throws IOException {
 
 		final byte[] lenBuf = new byte[4];
 		readFully(lenBuf, lenBuf.length);
 		final int len = NumberUtils.byteArrayToInteger(lenBuf, 0);
 		final byte[] filenameBuf = new byte[len];
 		readFully(filenameBuf, len);
 
 		return new String(filenameBuf);
 	}
 
 	public void close() throws IOException {
 
 		this.memoryMappedFile.close();
 		super.close();
 	}
 }
