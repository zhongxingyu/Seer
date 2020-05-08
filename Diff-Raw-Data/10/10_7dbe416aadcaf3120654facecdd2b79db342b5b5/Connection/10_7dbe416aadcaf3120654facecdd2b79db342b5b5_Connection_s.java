 package com.eyecall.connection;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.util.Iterator;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fasterxml.jackson.core.JsonFactory;
 import com.fasterxml.jackson.core.JsonGenerator;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 
 /**
  * An abstraction of a connection between two sockets. A connection holds an 
  * internal state. All messages passed across the connection should be handled
  * by implementing a {@link ProtocolHandler}. Messages can be sent through the
  * connection by using the {@link #send(Message)} function. Do note that this
  * method call is non blocking. A connection has an internal message buffer
  * that it stores messages to send in. This message buffer is cleared on a
  * seperate thread.
  * 
  * To start up the sending and receiving of messages, the connection needs to
  * be initiated by calling the {@link #init()} method.
  * @author Nicker
  *
  */
 
 
 //prevent warnings being generated, java generic system is flawed
 @SuppressWarnings({"rawtypes", "unchecked"})
 public class Connection {
 	
 	private static final Logger logger = LoggerFactory.getLogger(Connection.class);
 	
 	/**
 	 * socket over which messages are passed
 	 */
 	private Socket s;
 	
 	/**
 	 * ProtocolHandler handling incoming messages
 	 */
 	private ProtocolHandler handler;
 	
 	/**
 	 * internal state of the connection
 	 */
 	private State state;
 	
 	/**
 	 * queue that buffers messages
 	 */
 	private OutQueue<Message> messages;
 	
 	/**
 	 * construct a new Connection
 	 * @param s socket over which messages are passed
 	 * @param handler ProtocolHandler handling incoming messages
 	 * @param state starting state of the connection
 	 */
 	public Connection(Socket s, ProtocolHandler handler, State state) {
 		this.s = s;
 		this.handler = handler;
 		this.state = state;
 		this.messages = new OutQueue<Message>(s);
 	}
 
 	/**
 	 * send a message over this connection. This method is non-blocking; the
 	 * message is buffered before being sent.
 	 * @param m
 	 */
 	public void send(Message m){
 		messages.add(m);
 	}
 	
 	/**
 	 * closes this connection and corresponding Socket. blocks until OutQueue
 	 * is being emptied
 	 * @throws IOException
 	 */
 	public void close() throws IOException{
 		
 		//block until OutQueue is empty
 		synchronized(messages){
 			while(!messages.isEmpty()){
 				try {
 					messages.wait();
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 		
 		s.getOutputStream().flush();
 		
 		//close the Socket
 		s.close();
 		logger.debug("Connection closed gracefully");
 	}
 	
 	/**
 	 * Initiates this Connection. Fires up ConnectionHandler thread
 	 */
 	public void init(){
 		//block until ConnectionHandler thread is started up
 		ConnectionHandler handler = new ConnectionHandler();
 		Thread connectionHandlerThread = new Thread(handler);
 		connectionHandlerThread.start();
 		while(!connectionHandlerThread.isAlive()){
 			synchronized (handler) {
 				try {
 					handler.wait();
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 	}
 	
 	/**
 	 * This Thread responsible of handling the InputStream of the Socket. 
 	 * Before this thread is fired up, it fires up the OutQueue thread first. 
 	 * It maps incoming JSON messages to {@link Message} objects. This message
 	 * is passed to the {@link ProtocolHandler} of this Connection that 
 	 * processes the message. When a state change is needed, this class performs
 	 * that state change.
 	 * @author Nicker
 	 *
 	 */
 	private class ConnectionHandler implements Runnable{
 		
 		@Override
 		public void run() {
 			
 			//notify that the thread is started
 			synchronized(this){
 				notifyAll();
 			}
 
 			
 			//initiate and configure ObjectMapper
 			ObjectMapper mapper = new ObjectMapper();
 			mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
 			
 			//fire up outputqueue thread
 			Thread outQueueThread = new Thread(messages);
 			outQueueThread.start();
 			
 			//block until outQueue thread is started up
 			while(!outQueueThread.isAlive()){
 				synchronized (messages) {
 					try {
 						messages.wait();
 						
 					} catch (InterruptedException e) {
 					}
 				}
 			}
 			
 			//initiate message iterator that iterates over the InputStream
 			Iterator<Message> iterator = null;
 			try {
 				iterator = mapper.readValues(new JsonFactory().createParser(s.getInputStream()), Message.class);
 			} catch (IOException e) {
 				logger.warn("unexpected IOException while reading message: {}", e.toString());
 				//close socket
 				try {
 					s.close();
 				} catch (IOException e1) {
 				}
 			}
 			
 			//iterate over the messages and handle the incoming messages
 			
 			try{
 				if(iterator != null){
 					while(iterator.hasNext()){
 						Message m = iterator.next();
 						State newState = handler.handleMessage(state, m, messages); 
 						if(newState == null){
 							newState = new DefaultProtocolHandler().handleMessage(state, m, messages);
 						}
 						state = newState;
 					}
 				}
 			//eww nasty solution by Jackson ObjectMapper. Throws a RuntimeException when the socket is closed...
 			} catch(RuntimeException e){
 				logger.debug("message iterator seems to be done. Is the socket closed?: {}", e.toString());
 			}
 			//close the socket
 			try {
 				s.close();
 			} catch (IOException e) {
 			}
 		}
 	}
 }
