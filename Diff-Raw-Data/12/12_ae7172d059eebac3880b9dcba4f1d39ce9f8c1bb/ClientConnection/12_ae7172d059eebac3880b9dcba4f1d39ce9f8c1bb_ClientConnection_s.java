 package ch.ethz.mlmq.net;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.nio.channels.SocketChannel;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Logger;
 
 import ch.ethz.mlmq.logging.LoggerUtil;
 import ch.ethz.mlmq.logging.PerformanceLogger;
 import ch.ethz.mlmq.logging.PerformanceLoggerManager;
 import ch.ethz.mlmq.net.request.Request;
 import ch.ethz.mlmq.net.request.RequestResponseFactory;
 import ch.ethz.mlmq.net.response.Response;
 
 /**
  * Network Connection of a client which is connected to a broker
  */
 public class ClientConnection implements Closeable {
 
 	private static final Logger logger = Logger.getLogger(ClientConnection.class.getSimpleName());
 
 	private final PerformanceLogger perfLog = PerformanceLoggerManager.getLogger();
 
 	private final String host;
 	private final int port;
 
 	private SocketChannel clientSocket;
 
 	private RequestResponseFactory reqRespFactory;
 
 	private final Timer requestTimeoutTimer;
 
 	/**
 	 * Defines how much time our server has to respond to our request in milliseconds
 	 */
 	private final long responseTimeoutTime;
 
 	/**
 	 * TODO Allocate via ByteBufferPool
 	 */
 	private ByteBuffer ioBuffer = ByteBuffer.allocate(Protocol.CLIENT_IO_BUFFER_CAPACITY);
 
 	public ClientConnection(String host, int port, long responseTimeoutTime) {
 		this.responseTimeoutTime = responseTimeoutTime;
 		this.host = host;
 		this.port = port;
 		this.reqRespFactory = new RequestResponseFactory();
 
 		this.requestTimeoutTimer = new Timer("ClientTimerTo" + host, true);
 		logger.info("Created new ClientConnection to " + host + ":" + port);
 	}
 
 	public Response submitRequest(Request request) throws IOException {
 		long requestStartTime = System.currentTimeMillis();
 
 		Response response = null;
 		TimeoutTimerTask timeoutTask = null;
		logger.info("Submitting Request " + (request == null ? "Null" : request.getClass().getSimpleName()));
 
 		try {
 			writeToSocket(request);
 
 			logger.info("Wait for response");
 
 			int numBytes = -1;
 			int responseLenght = -1;
 
 			// schedule TimeouTimer
 			timeoutTask = new TimeoutTimerTask(responseTimeoutTime);
 			requestTimeoutTimer.schedule(timeoutTask, responseTimeoutTime);
 
 			while ((numBytes = clientSocket.read(ioBuffer)) >= 0) {
 				if (ioBuffer.position() >= Protocol.LENGH_FIELD_LENGHT && responseLenght == -1) {
 					// there is enough data to read an int
 
 					// read BufferPosition 0 to 3
 					responseLenght = ioBuffer.getInt(0);
 
 					logger.fine("Read ResponseLenght " + responseLenght);
 				}
 
 				if (ioBuffer.position() >= Protocol.LENGH_FIELD_LENGHT + responseLenght && responseLenght != -1) {
 					// enough data received to deserialize the response message
 
 					// switch to read-mode
 					ioBuffer.flip();
 					// consume header int-value
 					ioBuffer.getInt();
 					response = reqRespFactory.deserializeResponse(ioBuffer);
 					ioBuffer.compact();
 
 					if (ioBuffer.position() != 0) {
 						logger.warning("Discarding " + ioBuffer.position() + " bytes from inputbuffer");
 					}
 
 					break;
 				}
 
 			}
 
 			if (numBytes <= 0) {
 				throw new IOException("Connection remotely closed by host");
 			}
 
			perfLog.log(System.currentTimeMillis() - requestStartTime, "ClientSendRequest#" + (request == null ? "Null" : request.getClass().getSimpleName())
					+ ":" + (response == null ? "Null" : response.getClass().getSimpleName()));
 		} catch (Exception ex) {
 			logger.severe("Exception while sending Message " + ex + " " + LoggerUtil.getStackTraceString(ex));
 
			perfLog.log(System.currentTimeMillis() - requestStartTime, "ClientSendRequestError#"
					+ (request == null ? "Null" : request.getClass().getSimpleName()) + ":" + (response == null ? "Null" : response.getClass().getSimpleName()));
 
 			throw ex;
 		} finally {
 			// cancel TimeoutTimer
 			if (timeoutTask != null) {
 				timeoutTask.cancel();
 			}
 		}
 
 		return response;
 	}
 
 	/**
 	 * Write 4 byte (as int) which indicate the length in bytes of the serialized Request object then writes the serialized object to the socket
 	 * 
 	 * 
 	 * @param request
 	 * @throws IOException
 	 */
 	private void writeToSocket(Request request) throws IOException {
 
 		reqRespFactory.serializeRequestWithHeader(request, ioBuffer);
 
 		// write message to the socket
 		ioBuffer.flip();
 		int numBytes = clientSocket.write(ioBuffer);
 		logger.fine("Client wrote " + numBytes + " bytes");
 
 		ioBuffer.clear();
 	}
 
 	public void connect() throws UnknownHostException, IOException {
 
 		logger.info("Try connect to " + host + ":" + port + "...");
 
 		try {
 
 			// Create client SocketChannel
 			clientSocket = SocketChannel.open();
 
 			// Connection to host port
 			InetSocketAddress adr = new InetSocketAddress(host, port);
 			if (!clientSocket.connect(adr)) {
 				throw new IOException("Could not connect to " + host + ":" + port);
 			}
 
 			boolean result = clientSocket.finishConnect();
 
 			logger.info("Connection established " + result);
 		} catch (Exception ex) {
 			if (clientSocket != null) {
 				clientSocket.close();
 			}
 
 			throw ex;
 		}
 	}
 
 	public boolean isConnected() {
 		if (clientSocket != null) {
 			return clientSocket.isConnected();
 		}
 
 		return false;
 	}
 
 	public void close() {
 		logger.info("Closing Connection to " + host + ":" + port);
 
 		if (clientSocket != null) {
 			try {
 				clientSocket.close();
 			} catch (IOException e) {
 				logger.severe("Error while closing Socket to " + host + ":" + port + " " + LoggerUtil.getStackTraceString(e));
 			}
 		}
 	}
 
 	private class TimeoutTimerTask extends TimerTask {
 
 		/**
 		 * for logging purpouses only
 		 */
 		private long timeout;
 
 		public TimeoutTimerTask(long timeout) {
 			this.timeout = timeout;
 		}
 
 		@Override
 		public void run() {
 			try {
 				logger.severe("Request Timeout " + timeout + " - closing connection");
 				clientSocket.close();
 			} catch (IOException e) {
 				logger.severe("Error while closing timeouted Socket " + LoggerUtil.getStackTraceString(e));
 			}
 		}
 	};
 }
