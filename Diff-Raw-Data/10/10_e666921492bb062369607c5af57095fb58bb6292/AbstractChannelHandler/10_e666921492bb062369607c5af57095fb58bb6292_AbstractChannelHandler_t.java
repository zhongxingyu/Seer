 package ar.edu.itba.pdc.duta.proxy;
 
 import java.io.IOException;
 import java.nio.channels.SocketChannel;
 import java.util.Deque;
 import java.util.concurrent.LinkedBlockingDeque;
 
 import net.jcip.annotations.GuardedBy;
 import net.jcip.annotations.ThreadSafe;
 
 import org.apache.log4j.Logger;
 
 import ar.edu.itba.pdc.duta.http.model.MessageHeader;
 import ar.edu.itba.pdc.duta.http.parser.MessageParser;
 import ar.edu.itba.pdc.duta.http.parser.ParseException;
 import ar.edu.itba.pdc.duta.net.BufferedReadableByteChannel;
 import ar.edu.itba.pdc.duta.net.ChannelHandler;
 import ar.edu.itba.pdc.duta.net.Reactor.ReactorKey;
 import ar.edu.itba.pdc.duta.net.buffer.DataBuffer;
 
 @ThreadSafe
 public abstract class AbstractChannelHandler implements ChannelHandler {
 
 	private static Logger logger = Logger.getLogger(AbstractChannelHandler.class);
 
 	@GuardedBy("keyLock")
 	protected ReactorKey key;
 
 	private Deque<DataBuffer> outputQueue = new LinkedBlockingDeque<DataBuffer>();
 
 	protected boolean close = false;
 
 	protected Object keyLock = new Object();
 
 	protected Object lock = new Object();
 
 	private MessageParser parser;
 
 	protected DataBuffer buffer;
 
 	private BufferedReadableByteChannel channel;
 
 	public AbstractChannelHandler() {
 	}
 
 	@Override
 	public void queueOutput(DataBuffer output) {
 
 		if (close) {
 			throw new IllegalStateException();
 		}
 
 		synchronized (outputQueue) {
 			if (outputQueue.peekLast() != output) {
 				outputQueue.addLast(output);
 				output.retain();
 			}
 		}
 
 		synchronized (keyLock) {
 			if (key != null) {
 				key.setInterest(true, true);
 			}
 		}
 	}
 
 	protected abstract void wroteBytes(long bytes);
 
 	@Override
 	public void write(SocketChannel channel) throws IOException {
 
 		while (!outputQueue.isEmpty()) {
 
 			DataBuffer buffer = outputQueue.peekFirst();
 			try {
 
 				int pos = buffer.getReadIndex();
 				buffer.writeTo(channel);
 				wroteBytes(buffer.getReadIndex() - pos);
 
 			} catch (IOException e) {
 
 				logger.warn("Writing to socket failed.", e);
 				abort();
 				return;
 			}
 
 			if (buffer.hasReadableBytes()) {
 				// If we didn't write the whole thing, the socket won't accept
 				// more
 				break;
 			} else {
 				outputQueue.removeFirst().release();
 			}
 		}
 
 		if (outputQueue.isEmpty()) {
 
 			synchronized (keyLock) {
 				key.setInterest(true, false);
 
 				if (close) {
 					logger.debug("Closing...");
 					key.close();
 				}
 			}
 		}
 	}
 
 	@Override
 	public ReactorKey getKey() {
 		return key;
 	}
 
 	@Override
 	public void setKey(ReactorKey key, BufferedReadableByteChannel channel) {
 
 		this.key = key;
 		if (key != null) {
 			key.setInterest(true, !outputQueue.isEmpty());
 		}
 
 		this.channel = channel;
 	}
 
 	public void close() {
 
 		close = true;
 
 		synchronized (keyLock) {
 
 			if (key != null && !outputQueue.isEmpty()) {
 				key.setInterest(true, true);
 			}
 
 			if (key != null && outputQueue.isEmpty()) {
 				logger.debug("Closed");
 				key.close();
 			}
 		}
 	}
 
 	@Override
 	public void read(SocketChannel channel) throws IOException {
 
 
 		do {
			if (buffer == null) {
				parser = newParser();
				buffer = new DataBuffer();
			}
			
 			buffer.readFrom(this.channel);
 	
 			if (parser != null) {
 				parseHeader(channel);
 			} else {
 				processBody();
 			}
 		} while(this.channel.hasInput());
 	}
 
 	private void parseHeader(SocketChannel channel) {
 
 		int pos = buffer.getWriteIndex();
 
 		try {
 			parser.parse(buffer, this instanceof ClientHandler);
 		} catch (ParseException e) {
 			logger.error("Aborting request due to malformed headers", e);
 			abort();
 			return;
 		} catch (IOException e) {
 			logger.error("Failed to read headers, aborting", e);
 			abort();
 			return;
 		}
 
 		wroteBytes(buffer.getWriteIndex() - pos);
 
 		MessageHeader header = parser.getHeader();
 
 		if (header != null) {
 
 			if (logger.isTraceEnabled()) {
 				logger.trace(header);
 			}
 
 			buffer.release();
 			buffer = null;
 
 			parser = null;
 
 			processHeader(header, channel);
 		}
 	}
 
 	@Override
 	public void abort() {
 
 		if (buffer != null) {
 			buffer.release();
 			buffer = null;
 		}
 
 		for (DataBuffer buffer : outputQueue) {
 			buffer.release();
 		}
 
 		outputQueue.clear();
 	}
 
 	@Override
 	public Object keyLock() {
 		return keyLock;
 	}
 
 	@Override
 	public Object lock() {
 		return lock;
 	}
 
 	protected abstract MessageParser newParser();
 
 	protected abstract void processHeader(MessageHeader header, SocketChannel channel);
 
 	protected abstract void processBody();
 
 
 }
