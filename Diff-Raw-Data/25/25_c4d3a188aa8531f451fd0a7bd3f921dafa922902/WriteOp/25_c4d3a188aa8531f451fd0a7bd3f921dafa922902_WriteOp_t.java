 package edu.berkeley.icsi.cdfs.datanode;
 
 import java.io.Closeable;
import java.io.EOFException;
 import java.io.IOException;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 
 import edu.berkeley.icsi.cdfs.cache.Buffer;
 import edu.berkeley.icsi.cdfs.cache.BufferPool;
 import edu.berkeley.icsi.cdfs.compression.Compressor;
 import edu.berkeley.icsi.cdfs.conf.ConfigConstants;
 import edu.berkeley.icsi.cdfs.sharedmem.SharedMemoryConsumer;
 import edu.berkeley.icsi.cdfs.utils.ConfigUtils;
 import edu.berkeley.icsi.cdfs.utils.NumberUtils;
 
 final class WriteOp implements Closeable {
 
 	private static final Log LOG = LogFactory.getLog(WriteOp.class);
 
 	private final FileSystem hdfs;
 
 	private final Configuration conf;
 
 	private final Compressor compressor;
 
 	private final SharedMemoryConsumer sharedMemoryConsumer;
 
 	private List<Buffer> uncompressedBuffers = null;
 
 	private List<Buffer> compressedBuffers = null;
 
 	private int bytesWrittenInBlock = 0;
 
 	WriteOp(final Socket socket, final FileSystem hdfs, final Configuration conf) throws IOException {
 
 		this.hdfs = hdfs;
 		this.conf = conf;
 		this.sharedMemoryConsumer = new SharedMemoryConsumer(socket);
 		this.compressor = new Compressor(ConfigConstants.BUFFER_SIZE);
 	}
 
 	boolean write(final Path hdfsPath, final int blockSize) throws IOException {
 
 		// Create new cache lists
 		this.uncompressedBuffers = new ArrayList<Buffer>();
 		this.compressedBuffers = new ArrayList<Buffer>();
 
 		boolean cacheUncompressed = this.conf.getBoolean(ConfigUtils.ENABLE_UNCOMPRESSED_CACHING_KEY,
 			ConfigUtils.DEFAULT_ENABLE_UNCOMPRESSED_CACHING);
 
 		boolean cacheCompressed = this.conf.getBoolean(ConfigUtils.ENABLE_COMPRESSED_CACHING_KEY,
 			ConfigUtils.DEFAULT_ENABLE_COMPRESSED_CACHING);
 
 		byte[] uncompressedBuffer = null;
 		byte[] compressedBuffer = null;
 		int numberOfBytesInCompressedBuffer = 0;
 
 		if (!cacheUncompressed) {
 			uncompressedBuffer = new byte[ConfigConstants.BUFFER_SIZE];
 		}
 
 		if (!cacheCompressed) {
 			compressedBuffer = new byte[ConfigConstants.BUFFER_SIZE];
 		}
 
 		// Open HDFS output stream
 		final FSDataOutputStream hdfsOutputStream = this.hdfs.create(hdfsPath, true);
 
 		boolean readEOF = false;
 
 		BufferPool bufferPool = BufferPool.get();
 
 		int readBytes = 0;
 		while (readBytes < blockSize) {
 
			final ByteBuffer sharedBuffer;
			try {
				sharedBuffer = this.sharedMemoryConsumer.lockSharedMemory();
			} catch (EOFException e) {
 				readEOF = true;
 				break;
 			}
 
 			// Check out how much data we can read before we cross the block boundary
 			final int bytesToRead = Math.min(sharedBuffer.remaining(), blockSize - readBytes);
 
 			// Get buffer to cache if uncompressed caching is enabled
 			if (cacheUncompressed) {
 				uncompressedBuffer = bufferPool.lockBuffer();
 				if (uncompressedBuffer == null) {
 					clearUncompressedBuffers();
 					cacheUncompressed = false;
 					uncompressedBuffer = new byte[ConfigConstants.BUFFER_SIZE];
 				}
 			}
 
 			// Copy data from shared buffer
 			sharedBuffer.get(uncompressedBuffer, 0, bytesToRead);
 
 			if (cacheUncompressed) {
 				final Buffer buffer = new Buffer(uncompressedBuffer, bytesToRead);
 				this.uncompressedBuffers.add(buffer);
 			}
 
 			// Update number of bytes read from the stream
 			readBytes += bytesToRead;
 
 			if (!sharedBuffer.hasRemaining()) {
 				this.sharedMemoryConsumer.unlockSharedMemory();
 			}
 
 			// uncompressedBuffer now contains bytesToRead bytes ready to compress
 			final int numberOfCompressedBytes = this.compressor.compress(uncompressedBuffer, bytesToRead);
 
 			// Make sure we have a buffer to write the compressed data to
 			while (true) {
 
 				// We still have an compressed buffer
 				if (compressedBuffer != null) {
 					if (numberOfBytesInCompressedBuffer + numberOfCompressedBytes + 4 <= compressedBuffer.length) {
 						// There is enough memory left in the buffer, we can write to it
 						break;
 					} else {
 						// The buffer is full, write it to HDFS and then decide what to do with it
 						hdfsOutputStream.write(compressedBuffer, 0, numberOfBytesInCompressedBuffer);
 						if (cacheCompressed) {
 							final Buffer buffer = new Buffer(compressedBuffer, numberOfBytesInCompressedBuffer);
 							this.compressedBuffers.add(buffer);
 							numberOfBytesInCompressedBuffer = 0;
 							compressedBuffer = null;
 						} else {
 							numberOfBytesInCompressedBuffer = 0;
 							break;
 						}
 					}
 				} else {
 
 					if (cacheCompressed) {
 						compressedBuffer = bufferPool.lockBuffer();
 						if (compressedBuffer == null) {
 							compressedBuffer = new byte[ConfigConstants.BUFFER_SIZE];
 							clearCompressedBuffers();
 							cacheCompressed = false;
 						}
 						break;
 					} else {
 						System.out.println("Illegal state2!!!!");
 					}
 
 				}
 			}
 
 			// Write the number of compressed bytes to the compressed buffer
 			NumberUtils.integerToByteArray(numberOfCompressedBytes, compressedBuffer, numberOfBytesInCompressedBuffer);
 			numberOfBytesInCompressedBuffer += 4;
 			// Write the compressed data itself
 			System.arraycopy(this.compressor.getCompressedBuffer(), 0, compressedBuffer,
 				numberOfBytesInCompressedBuffer, numberOfCompressedBytes);
 			numberOfBytesInCompressedBuffer += numberOfCompressedBytes;
 		}
 
 		// We still need to write the remaining data from the compressed buffer to HDFS
 		if (numberOfBytesInCompressedBuffer > 0) {
 			hdfsOutputStream.write(compressedBuffer, 0, numberOfBytesInCompressedBuffer);
 			if (cacheCompressed) {
 				final Buffer buffer = new Buffer(compressedBuffer, numberOfBytesInCompressedBuffer);
 				this.compressedBuffers.add(buffer);
 			}
 		}
 
 		// Clean up
 		this.bytesWrittenInBlock = readBytes;
 		hdfsOutputStream.close();
 
 		LOG.info("Finished block after " + readBytes + " " + readEOF);
 
 		return readEOF;
 	}
 
 	List<Buffer> getUncompressedBuffers() {
 
 		return Collections.unmodifiableList(this.uncompressedBuffers);
 	}
 
 	List<Buffer> getCompressedBuffers() {
 
 		return Collections.unmodifiableList(this.compressedBuffers);
 	}
 
 	int getBytesWrittenInBlock() {
 
 		return this.bytesWrittenInBlock;
 	}
 
 	private final void clearUncompressedBuffers() {
 
 		final Iterator<Buffer> it = this.uncompressedBuffers.iterator();
 		while (it.hasNext()) {
 			BufferPool.get().releaseBuffer(it.next().getData());
 		}
 
 		this.uncompressedBuffers.clear();
 	}
 
 	private final void clearCompressedBuffers() {
 
 		final Iterator<Buffer> it = this.compressedBuffers.iterator();
 		while (it.hasNext()) {
 			BufferPool.get().releaseBuffer(it.next().getData());
 		}
 
 		this.compressedBuffers.clear();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void close() throws IOException {
 
 		this.sharedMemoryConsumer.close();
 	}
 }
