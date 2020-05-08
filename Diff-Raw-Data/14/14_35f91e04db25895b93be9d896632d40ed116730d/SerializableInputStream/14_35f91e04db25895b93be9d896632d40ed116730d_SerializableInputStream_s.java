 package com.dajudge.serinstream;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * An input stream that can be sent over RMI. It uses an encrypted temp file to
  * store the deserialized data.
  * 
  * @author Alex Stockinger
  */
 public final class SerializableInputStream extends InputStream implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	private static final transient Logger LOG = LoggerFactory.getLogger(SerializableInputStream.class);
 
 	private transient InputStream stream;
	private transient int bufferSize;
 	private transient TempStoreInstance tempStoreInstance;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param stream
 	 *            the stream to serialize.
 	 */
 	public SerializableInputStream(final InputStream stream) {
 		this(stream, config().getDefaultWriteChunkSize());
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param stream
 	 *            the stream to serialize.
 	 * @param bufferSize
 	 *            the buffer size to be used when serializing.
 	 */
 	public SerializableInputStream(final InputStream stream, final int bufferSize) {
 		if (stream == null) {
 			throw new IllegalArgumentException("stream must not be null");
 		}
 		if (bufferSize <= 0) {
 			throw new IllegalArgumentException("bufferSize must be > 0");
 		}
		this.bufferSize = bufferSize;
 		this.stream = stream;
 	}
 
 	private void writeObject(final ObjectOutputStream out) throws IOException {
		LOG.debug("Serializing input stream with buffer size " + bufferSize);
 		if (stream == null) {
 			throw new IllegalArgumentException("Serializable input stream not ready for serialization");
 		}
		final byte[] data = new byte[bufferSize];
 		int read;
 		while ((read = stream.read(data)) > 0) {
 			LOG.trace("Writing " + read + " bytes");
 			out.writeInt(read);
 			out.write(data, 0, read);
 		}
 		out.writeInt(0);
 	}
 
 	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
 		final SerializationTempStore tempStore = config().getSerializationTempStore();
 		tempStoreInstance = tempStore.createTempStoreInstance();
 		LOG.debug("Deserializing input stream to " + tempStore);
 		final Integer overallBytes = tempStoreInstance.store(new PipeToTempStoreCallback(in));
 		LOG.debug("Successfully read " + overallBytes + " bytes");
 		stream = tempStoreInstance.retrieve();
		bufferSize = config().getDefaultWriteChunkSize();
 	}
 
 	private static SerializableInputStreamConfiguration config() {
 		return SerializableInputStreamConfiguration.getInstance();
 	}
 
 	@Override
 	public int read() throws IOException {
 		checkReadyToRead();
 		return stream.read();
 	}
 
 	@Override
 	public int read(final byte[] b) throws IOException {
 		checkReadyToRead();
 		return stream.read(b);
 	}
 
 	@Override
 	public int read(final byte[] b, final int off, final int len) throws IOException {
 		checkReadyToRead();
 		return stream.read(b, off, len);
 	}
 
 	@Override
 	public int available() throws IOException {
 		checkReadyToRead();
 		return stream.available();
 	}
 
 	@Override
 	public boolean markSupported() {
 		checkReadyToRead();
 		return stream.markSupported();
 	}
 
 	@Override
 	public synchronized void mark(final int readlimit) {
 		checkReadyToRead();
 		stream.mark(readlimit);
 	}
 
 	@Override
 	public long skip(final long n) throws IOException {
 		checkReadyToRead();
 		return stream.skip(n);
 	}
 
 	@Override
 	public synchronized void reset() throws IOException {
 		checkReadyToRead();
 		stream.reset();
 	}
 
 	private void checkReadyToRead() {
 		if (stream == null) {
 			throw new IllegalArgumentException(getClass().getName() + " not ready for reading");
 		}
 	}
 
 	@Override
 	public void close() throws IOException {
 		checkReadyToRead();
 		stream.close();
 		if (tempStoreInstance != null) {
 			tempStoreInstance.release();
 		}
 	}
 }
