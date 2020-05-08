 package com.github.bcap.dht.client;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.log4j.Logger;
 
 import com.github.bcap.dht.message.request.Request;
 import com.github.bcap.dht.message.response.Response;
 import com.github.bcap.dht.node.Contact;
 
 public class ConcurrentMessageSender implements MessageSender {
 
 	private static final Logger logger = Logger.getLogger(ConcurrentMessageSender.class);
 	
 	private ThreadPoolExecutor workerThreadPool;
 	private LinkedBlockingDeque<Runnable> workerQueue;
 	
 	private ConcurrentMessageSender thisRef = this;
 	
 	public ConcurrentMessageSender(int maxConcurrentMessages) {
 		this.workerQueue = new LinkedBlockingDeque<Runnable>();
		this.workerThreadPool = new ThreadPoolExecutor(maxConcurrentMessages, maxConcurrentMessages, 30, TimeUnit.SECONDS, workerQueue);
 
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			public void run() {
 				thisRef.shutdown();
 			}
 		});
 	}
 	
 	public void shutdown() {
 		workerThreadPool.shutdown();
 	}
 
 	public void send(Request request, ResponseHandler handler) {
 		logger.debug("Adding request " + request + " to the queue");
 		this.workerThreadPool.execute(new Worker(request, handler));
 		logger.debug("Request " + request + " added to the queue");
 	}
 	
 	class Worker implements Runnable {
 		private Request request;
 		private ResponseHandler handler;
 		
 		protected Worker(Request request, ResponseHandler handler) {
 			this.request = request;
 			this.handler = handler;
 		}
 		
 		public void run() {
 			Contact destination = request.getDestination();
 			logger.info("Sending message of type " + request.getClass().getSimpleName() + " to " + destination.getIp() + ":" + destination.getPort());
 
 			ObjectInputStream inStream = null;
 			ObjectOutputStream outStream = null;
 			Socket socket = new Socket();
 			
 			try {
 				socket.connect(new InetSocketAddress(destination.getIp(), destination.getPort()));
 			
 				try {
 					outStream = new ObjectOutputStream(socket.getOutputStream());
 				} catch (IOException e) {
 					logger.error("IOException occured while trying to open the socket outputStream");
 					throw e;
 				}
 
 				logger.debug("Writing object " + request + " to socket output stream");
 				outStream.writeObject(request);
 
 				try {
 					inStream = new ObjectInputStream(socket.getInputStream());
 				} catch (IOException e) {
 					logger.error("IOException occured while trying to open the socket inputStream");
 					throw e;
 				}
 				
 				Object readObj = null;
 
 				try {
 					logger.debug("Reading object from the socket input stream");
 					readObj = inStream.readObject();
 				} catch (IOException e) {
 					logger.error("IOException occured while trying to read the object from the socket");
 					throw e;
 				} catch (ClassNotFoundException e) {
 					logger.error("ClassNotFoundException occured while trying to read object from the socket");
 					throw e;
 				}
 
 				if (readObj instanceof Response) {
 					Response response = (Response) readObj;
 					logger.debug("Received response: " + response);
 					handler.handleResponse(response);
 				} else {
 					logger.warn("Object read from the socket is of an unsupported type (not instance of " + Response.class + "): " + readObj.getClass());
 				}
 
 			} catch (Exception e) {
 				logger.error(null, e);
 			} finally {
 				closeResources(socket, inStream, outStream);
 			}
 		}
 
 		private void closeResources(Socket socket, InputStream inputStream, OutputStream outputStream) {
 			if (inputStream != null) {
 				try {
 					inputStream.close();
 				} catch (IOException e) {
 					logger.error("Error while trying to close the inputstream " + inputStream, e);
 				}
 			}
 
 			if (outputStream != null) {
 				try {
 					outputStream.close();
 				} catch (IOException e) {
 					logger.error("Error while trying to close the outputStream " + outputStream, e);
 				}
 			}
 
 			if (socket != null) {
 				try {
 					socket.close();
 				} catch (IOException e) {
 					logger.error("Error while trying to close the socket " + socket, e);
 				}
 			}
 		}
 	}
 }
