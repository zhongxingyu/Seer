 package de.tum.in.sonar.log4j.appender;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.concurrent.ArrayBlockingQueue;
 
 import org.apache.log4j.Appender;
 import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
 import org.apache.log4j.spi.LoggingEvent;
 import org.apache.thrift.TException;
 import org.apache.thrift.protocol.TBinaryProtocol;
 import org.apache.thrift.protocol.TProtocol;
 import org.apache.thrift.transport.TFramedTransport;
 import org.apache.thrift.transport.TSocket;
 import org.apache.thrift.transport.TTransport;
 import org.apache.thrift.transport.TTransportException;
 
 import de.tum.in.sonar.collector.CollectService.Client;
 import de.tum.in.sonar.collector.Identifier;
 import de.tum.in.sonar.collector.LogMessage;
 
 /**
  * Implements a Sonar client to send logs to a remote Sonar server
  * 
  * @author Deepak Srinathan
  * 
  */
 public class SonarAppender extends AppenderSkeleton implements Appender {
 
	// Logger
	private static final Logger logger = Logger.getLogger(SonarAppender.class);

 	// the default server
 	private String server = "localhost";
 
 	// port number of the remote sonar server
 	private int port = 7922;
 
 	// the host name of the machine from whcih logs are generated
 	private String hostname = null;
 
 	// number of messages to be held in the processing queue
 	private int bufferSize = 3024;
 
 	// name of the sensor to be logged to
 	private String sensor = null;
 
 	// Sonar client
 	private TTransport transport = null;
 	private Client client = null;
 
 	private ArrayBlockingQueue<Object> messageQueue = new ArrayBlockingQueue<Object>(bufferSize);
 
 	private Thread consumer;
 
 	private Runnable messageProcessor = null;
 
 	public SonarAppender() {
 		super();
 		startConsumerThread();
 	}
 
 	/**
 	 * Send a log message to the Sonar Log Server.
 	 */
 	@Override
 	protected void append(LoggingEvent event) {
 		Identifier id = new Identifier();
 		// convert timestamp to unix timestamp
 		long timestamp = event.getTimeStamp() / 1000;
 		id.setTimestamp(timestamp);
 		id.setSensor(getSensor());
 		id.setHostname(getHostname());
 
 		// get the log message and level from the logging event
 		// we can get the syslog equivalent log level for putting the log level
 		// into the server
 		LogMessage message = new LogMessage();
 		message.setLogLevel(event.getLevel().toInt());
 		message.setLogMessage(event.getMessage().toString());
 		if (null != event.getThrowableInformation()) {
 			StringBuffer buffer = new StringBuffer();
 			buffer.append(message.getLogMessage());
 			buffer.append("\n");
 			String[] str = event.getThrowableStrRep();
 			for (String s : str) {
 				buffer.append(s);
 				buffer.append("\n");
 			}
 			message.setLogMessage(buffer.toString());
 		}
 		message.setProgramName(event.getLoggerName());
 		message.setTimestamp(timestamp);
 
 		LogPayload payload = new LogPayload(id, message);
 		messageQueue.offer(payload);
 	}
 
 	boolean running = true;
 
 	private void startConsumerThread() {
 		messageProcessor = new Runnable() {
 			@Override
 			public void run() {
				logger.info("Sonar Appender Thread started");
 				while (running || messageQueue.isEmpty() == false) {
 					try {
 						Object payload = messageQueue.take();
 
 						LogPayload logPayload = (LogPayload) payload;
 						// check if it is needed to connect or the old
 						// connection has been
 						// retained
 						boolean connected = connectIfNeeded();
 						if (!connected) {
 							getErrorHandler().error("No connection to the client");
 							return;
 						}
 
 						try {
 							client.logMessage(logPayload.getId(), logPayload.getMessage());
 						} catch (TException e) {
 							handleError("TException occured while logging", e);
 						} catch (Exception e) {
 							handleError("Exception occured while logging", e);
 						}
 
 					} catch (InterruptedException e) {
 						continue;
 					} catch (ClassCastException e) {
 						continue;
 					}
 				}
 			}
 
 		};
 
 		consumer = new Thread(messageProcessor);
 		consumer.start();
 	}
 
 	/**
 	 * Close the connection to the server
 	 */
 	@Override
 	public void close() {
		logger.info("Shutting down Sonar appender thread");
 		running = false;
 
 		consumer.interrupt();
 		try {
 			consumer.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		if (isConnected())
 			transport.close();
 
 	}
 
 	/**
 	 * Does not require a layout, so return false
 	 */
 	@Override
 	public boolean requiresLayout() {
 		return false;
 	}
 
 	/**
 	 * Establish connection with the Sonar Server
 	 * 
 	 * @throws TTransportException
 	 * @throws UnknownHostException
 	 * @throws IOException
 	 */
 	private void establishConnection() throws TTransportException, UnknownHostException, IOException {
 		final TSocket socket = new TSocket(getServer(), getPort());
 		transport = new TFramedTransport(socket);
 		transport.open();
 
 		TProtocol protocol = new TBinaryProtocol(transport);
 		client = new Client(protocol);
 	}
 
 	/**
 	 * Check if connected, if not , drop the current connection and establish a new connection
 	 * 
 	 * @return
 	 */
 	private boolean connectIfNeeded() {
 		if (isConnected())
 			return true;
 
 		// connection was dropped, establish new connection
 		if (transport != null && !transport.isOpen())
 			transport.close();
 
 		try {
 			establishConnection();
 			return true;
 		} catch (TTransportException e) {
 			handleError("TTransportException on connect", e);
 		} catch (UnknownHostException e) {
 			handleError("UnknownHostException on connect", e);
 		} catch (IOException e) {
 			handleError("IOException on connect", e);
 		} catch (Exception e) {
 			handleError("Unhandled Exception on connect", e);
 		}
 
 		return false;
 	}
 
 	/**
 	 * Check if connected
 	 * 
 	 * @return true if connected false if not connected
 	 */
 	synchronized boolean isConnected() {
 		return transport != null && transport.isOpen();
 	}
 
 	/**
 	 * General error handler
 	 * 
 	 * @param failure
 	 * @param e
 	 */
 	void handleError(final String failure, final Exception e) {
		getErrorHandler().error(
				"Failure in SonarAppender: name=[" + name + "], failure=[" + failure + "], exception=["
						+ e.getMessage() + "]");
 	}
 
 	/**
 	 * Get the server hostname / ip address
 	 * 
 	 * @return The server hostname / ip
 	 */
 	public String getServer() {
 		return server;
 	}
 
 	/**
 	 * Set the server address
 	 * 
 	 * @param server
 	 */
 	public void setServer(String server) {
 		Validator.notEmptyString(server, "Server field cannot be empty");
 		this.server = server;
 	}
 
 	/**
 	 * Get Sonar server port
 	 * 
 	 * @return
 	 */
 	public int getPort() {
 		return port;
 	}
 
 	/**
 	 * Set the Sonar server port
 	 * 
 	 * @param port
 	 */
 	public void setPort(int port) {
 		Validator.positiveInteger(port, "Port number should be a possitive integer");
 		this.port = port;
 	}
 
 	/**
 	 * Get the host name of the local machine
 	 * 
 	 * @return
 	 */
 	public String getHostname() {
 		if (hostname == null) {
 			try {
 				InetAddress addr = InetAddress.getLocalHost();
 				hostname = addr.getHostName();
 			} catch (UnknownHostException uhe) {
 				hostname = "UNKNOWN_HOST";
 			}
 		}
 		return hostname;
 	}
 
 	/**
 	 * Setter method for host name
 	 * 
 	 * @param hostname
 	 */
 	public void setHostname(String hostname) {
 		this.hostname = hostname;
 	}
 
 	/**
 	 * Get the sensor name
 	 * 
 	 * @return
 	 */
 	public String getSensor() {
 		return sensor;
 	}
 
 	/**
 	 * Set the sensor name
 	 * 
 	 * @param sensor
 	 */
 	public void setSensor(String sensor) {
 		Validator.notEmptyString(sensor, "Sensor field cannot be empty");
 		this.sensor = sensor;
 	}
 }
