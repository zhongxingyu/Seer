 package ar.edu.itba.pdc.duta.proxy;
 
 import java.io.IOException;
 import java.nio.channels.SocketChannel;
 import java.util.Deque;
 import java.util.concurrent.LinkedBlockingDeque;
 
 import net.jcip.annotations.ThreadSafe;
 
 import org.apache.log4j.Logger;
 
 import ar.edu.itba.pdc.duta.http.model.MessageHeader;
 import ar.edu.itba.pdc.duta.http.parser.MessageParser;
 import ar.edu.itba.pdc.duta.http.parser.ParseException;
 import ar.edu.itba.pdc.duta.net.ChannelHandler;
 import ar.edu.itba.pdc.duta.net.Reactor.ReactorKey;
 import ar.edu.itba.pdc.duta.net.buffer.DataBuffer;
 
 @ThreadSafe
 public abstract class AbstractChannelHandler implements ChannelHandler {
 
 	private static Logger logger = Logger.getLogger(AbstractChannelHandler.class);
 
 	protected ReactorKey key;
 
 	private Deque<DataBuffer> outputQueue;
 
 	protected boolean close = false;
 
 	protected Object keyLock;
 
 	private MessageParser parser;
 
 	protected DataBuffer buffer;
 
 	public AbstractChannelHandler() {
 
 		outputQueue = new LinkedBlockingDeque<DataBuffer>();
 		key = null;
 		keyLock = new Object();
 	}
 
 	@Override
 	public void queueOutput(DataBuffer output) {
 
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
 
 	public abstract void wroteBytes(long bytes);
 
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
 
 				// This should mean the pipe was broken. We bail in that case.
 				synchronized (keyLock) {
 					key.close();
 				}
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
 
 	public boolean hasOutputQueued() {
 		return !outputQueue.isEmpty();
 	}
 
 	public ReactorKey getKey() {
 		return key;
 	}
 
 	@Override
 	public void setKey(ReactorKey key) {
 
 		synchronized (keyLock) {
 			this.key = key;
 			if (key != null) {
 				key.setInterest(true, !outputQueue.isEmpty());
 			}
 		}
 	}
 
 	public void close() {
 		close = true;
 
 		synchronized (keyLock) {
 
 			if (key != null && !outputQueue.isEmpty()) {
 				// FIXME: Somehow, the correct value for the interest is lost
 				// sometimes
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
 
 		if (buffer == null) {
 			parser = newParser();
 			buffer = new DataBuffer();
 		}
 		
 		buffer.readFrom(channel);
 
 		if (parser != null) {
 			parseHeader();
 		} else {
 			processBody();
 		}
 	}
 
 	private void parseHeader() {
 
 		int pos = buffer.getWriteIndex();
 
 		try {
 			parser.parse(buffer);
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
 
 			logger.debug("Have full header...");
 			logger.debug(header);
 
 			parser = null;
 
 			buffer.release();
 			buffer = null;
 
 			processHeader(header);
 		}
 	}
 
 	@Override
 	public void abort() {
		buffer.release();
		buffer = null;
 		
 		for (DataBuffer buffer : outputQueue) {
 			buffer.release();
 		}
 		
 		outputQueue.clear();
 	}
 	
 	protected abstract MessageParser newParser();
 
 	protected abstract void processHeader(MessageHeader header);
 
 	protected abstract void processBody();
 }
