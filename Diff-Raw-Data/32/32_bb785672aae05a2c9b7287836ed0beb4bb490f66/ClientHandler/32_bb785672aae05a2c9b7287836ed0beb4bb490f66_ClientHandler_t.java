 package ar.edu.itba.pdc.duta.proxy;
 
 import java.nio.channels.SocketChannel;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.apache.log4j.Logger;
 
 import ar.edu.itba.pdc.duta.admin.Stats;
 import ar.edu.itba.pdc.duta.http.model.MessageHeader;
 import ar.edu.itba.pdc.duta.http.model.RequestHeader;
 import ar.edu.itba.pdc.duta.http.parser.MessageParser;
 import ar.edu.itba.pdc.duta.http.parser.RequestParser;
 import ar.edu.itba.pdc.duta.net.buffer.DataBuffer;
 import ar.edu.itba.pdc.duta.proxy.operation.Operation;
 
 public class ClientHandler extends AbstractChannelHandler {
 
 	private static Logger logger = Logger.getLogger(ClientHandler.class);
 
 	private BlockingQueue<Operation> ops;
 	
 	private Map<Operation, BlockingQueue<DataBuffer>> queuedOutput;
 	
 	protected Operation currentOperation;
 
 	public ClientHandler() {
 		super();
 		ops = new LinkedBlockingQueue<Operation>();
 		queuedOutput = new HashMap<Operation, BlockingQueue<DataBuffer>>();
 	}
 
 	@Override
 	public void close() {
 		super.close();
 		Stats.closeInbound();
 	}
 
 	@Override
 	public void abort() {
 		super.abort();
 
		logger.fatal("current " + currentOperation + " ops " + ops + " buff " + buffer);
		
 		currentOperation = null;
 		buffer = null;
		
		Operation op;
		while ((op = ops.poll()) != null) {
 			op.abort();
 			for (DataBuffer buff : queuedOutput.get(op)) {
 				buff.release();
 			}
 		}
 
 		close();
 	}
 
 	@Override
 	public void wroteBytes(long bytes) {
 		Stats.addClientTraffic(bytes);
 	}
 
 	public synchronized void operationComplete() {
 		logger.debug("Detaching from op...");
 		ops.poll();
 		
 		if (!ops.isEmpty()) {
 			Operation op = ops.peek();
 			for (DataBuffer buff : queuedOutput.get(op)) {
 				super.queueOutput(buff, op);
 				buff.release();
 			}
 		}
 	}
 
 	@Override
 	protected void processBody() {
 		currentOperation.addClientBody();
 
 		if (currentOperation.isClientMessageComplete()) {
 			currentOperation = null;
 			buffer = null;
 		}
 	}
 
 	@Override
 	protected void processHeader(MessageHeader header, SocketChannel channel) {
 		
 		currentOperation = new Operation(this);
 		ops.add(currentOperation);
 		queuedOutput.put(currentOperation, new LinkedBlockingQueue<DataBuffer>());
 		
 		buffer = currentOperation.setClientHeader((RequestHeader) header, channel);
 		if (buffer != null) {
 			buffer.retain();
 		}
 		
 		if (currentOperation.isClientMessageComplete()) {
 			currentOperation = null;
 			buffer = null;
 		}
 	}
 
 	@Override
 	protected MessageParser newParser() {
 		return new RequestParser();
 	}
 	
 	@Override
 	protected synchronized boolean canWrite(Operation op) {
 		return ops.peek() == op;
 	}
 	
 	@Override
 	public synchronized void queueOutput(DataBuffer output, Operation op) {
 		
 		if (canWrite(op)) {
 			super.queueOutput(output, op);
 		} else {
 			output.retain();
 			queuedOutput.get(op).add(output);
 		}
 		
 		
 	}
 }
