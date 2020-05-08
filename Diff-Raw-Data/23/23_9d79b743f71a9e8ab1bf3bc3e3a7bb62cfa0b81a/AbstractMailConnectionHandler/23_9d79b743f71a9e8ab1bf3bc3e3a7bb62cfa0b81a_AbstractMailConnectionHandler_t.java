 /*-
  * Copyright (c) 2008, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer. 
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution. 
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.logicprobe.LogicMail.mail;
 
 import java.io.IOException;
 
 import net.rim.device.api.i18n.ResourceBundle;
 import net.rim.device.api.system.EventLogger;
 
 import org.logicprobe.LogicMail.AppInfo;
 import org.logicprobe.LogicMail.LogicMailResource;
 import org.logicprobe.LogicMail.util.Queue;
 import org.logicprobe.LogicMail.util.StringParser;
 
 /**
  * This class is responsible for managing the lifecycle of a mail
  * server network connection.  It also encapsulates the necessary
  * hooks for UI interactions.
  */
 public abstract class AbstractMailConnectionHandler {
 	protected static ResourceBundle resources = ResourceBundle.getBundle(LogicMailResource.BUNDLE_ID, LogicMailResource.BUNDLE_NAME);
 	private MailClient client;
 	private ConnectionThread connectionThread;
 	private int state;
 	private Queue requestQueue;
 	private MailConnectionHandlerListener listener;
 	private int retryCount;
 	private boolean invalidLogin;
 	private boolean shutdownInProgress;
 	private Object[] requestInProgress;
 	
     public static final int REQUEST_DISCONNECT = 1;
     
 	// The various states of a mail connection
 	public static final int STATE_CLOSED   = 0;
 	public static final int STATE_OPENING  = 1;
 	public static final int STATE_OPENED   = 2;
 	public static final int STATE_REQUESTS = 3;
 	public static final int STATE_IDLE     = 4;
 	public static final int STATE_CLOSING  = 5;
 
 	protected AbstractMailConnectionHandler(MailClient client) {
 		this.client = client;
 		this.connectionThread = new ConnectionThread();
 		this.state = STATE_CLOSED;
 		this.requestQueue = new Queue();
 		this.listener = null;
 		this.retryCount = 0;
 		this.invalidLogin = false;
 		this.shutdownInProgress = false;
 	}
 
 	/**
 	 * Start the mail connection handler thread.
 	 */
 	public void start() {
 		if(!connectionThread.isAlive()) {
 			if(connectionThread.isShutdown()) {
 				connectionThread = new ConnectionThread();
 			}
 			connectionThread.start();
 		}
     }
 
 	/**
 	 * Shutdown the mail connection handler thread.
 	 * 
 	 * @param wait If true, wait for all pending requests to finish.
 	 */
 	public void shutdown(boolean wait) {
 		if(!connectionThread.isAlive()) {
 			return;
 		}
 		synchronized(requestQueue) {
 			shutdownInProgress = true;
 		}
 		if(wait) {
 			Object element;
 			synchronized(requestQueue) {
 				element = requestQueue.element();
 			}
 			while(element != null) {
 				synchronized(requestQueue) {
 					element = requestQueue.element();
 				}
 				Thread.yield();
 			}
 		}
 		setConnectionState(STATE_CLOSING);
 		connectionThread.shutdown();
 		try {
 			connectionThread.join();
 		} catch (InterruptedException e) { }
 		
 		synchronized(requestQueue) {
 			shutdownInProgress = false;
 		}
 		MailConnectionManager.getInstance().fireMailConnectionStateChanged(
 				client.getConnectionConfig(),
 				MailConnectionStateEvent.STATE_DISCONNECTED);
 	}
 
 	/**
 	 * Gets whether the connection thread is currently running.
 	 * 
 	 * @return True if running, false if shutdown.
 	 */
 	public boolean isRunning() {
 		return connectionThread.isAlive();
 	}
 	
 	/**
 	 * Sets the listener for events from this class.
 	 * The listener is the class that handles results from requests.
 	 * 
 	 * @param listener The listener
 	 */
 	public void setListener(MailConnectionHandlerListener listener) {
 		this.listener = listener;
 	}
 
 	/**
 	 * Gets the listener for events from this class.
 	 * 
 	 * @return The listener
 	 */
 	protected MailConnectionHandlerListener getListener() {
 		return this.listener;
 	}
 	
 	/**
 	 * Add a request to the queue.
 	 * If the connection is shutting down, all requests will be ignored.
 	 * 
 	 * @param type Type of request
 	 * @param params Parameters passed to the corresponding
 	 *               AbstractMailStore.requestXXXX() method. 
      * @param tag Tag reference to pass along with the request
 	 */
 	public void addRequest(int type, Object[] params, Object tag) {
 		synchronized(requestQueue) {
 			if(!shutdownInProgress) {
 				requestQueue.add(new Object[] {new Integer(type), params, tag});
 				requestQueue.notifyAll();
 			}
 		}
 	}
 	
 	/**
 	 * Handles the CLOSED state.
 	 */
 	private void handleClosedConnection() {
 		showStatus(null);
 		synchronized(requestQueue) {
 			if(requestQueue.element() != null) {
 				setConnectionState(STATE_OPENING);
 			}
 			else if(!connectionThread.isShutdown()) {
 				try {
 					requestQueue.wait();
 				} catch (InterruptedException e) { }
 			}
 		}
 	}
 	
     /**
      * Handles the OPENING state.
      * Open a new connection, and authenticates with the server.
      * This method should typically establish a socket connection and
      * then send the protocol-specific login commands.
      * 
      * @throws IOException on I/O errors
      * @throws MailException on protocol errors
      */
 	private void handleOpeningConnection() throws IOException, MailException {
 		showStatus(resources.getString(LogicMailResource.MAILCONNECTION_OPENING_CONNECTION));
 		if(checkLogin(client)) {
 			if(client.open()) {
 				invalidLogin = false;
 				setConnectionState(STATE_OPENED);
 				MailConnectionManager.getInstance().fireMailConnectionStateChanged(
 						client.getConnectionConfig(),
 						MailConnectionStateEvent.STATE_CONNECTED);
 				return;
 			}
 			else {
 				invalidLogin = true;
 				return;
 			}
 		}
 		// Unable to open, so transition to closing and clear the queue
         setConnectionState(STATE_CLOSING);
         synchronized(requestQueue) {
             clearRequestQueue(null);
         }
 	}
     
 	/**
 	 * Handles the OPENED state.
 	 * 
      * @throws IOException on I/O errors
      * @throws MailException on protocol errors
 	 */
 	private void handleOpenedConnection() throws IOException, MailException {
 		showStatus(null);
 		retryCount = 0;
 		synchronized(requestQueue) {
 			if(requestQueue.element() != null) {
 				setConnectionState(STATE_REQUESTS);
 			}
 			else {
 				setConnectionState(STATE_IDLE);
 			}
 		}
 	}
 
 	/**
      * Handles the REQUESTS state to process any pending server requests.
      * 
      * @throws IOException on I/O errors
      * @throws MailException on protocol errors
      */
 	private void handlePendingRequests() throws IOException, MailException {
 		Queue requestQueue = getRequestQueue();
 		Object element;
 		synchronized(requestQueue) {
 			element = requestQueue.element();
 		}
 		while(element != null) {
 			synchronized (requestQueue) {
 				requestQueue.remove();
 			}
 			Object[] request = (Object[])element;
 			int type = ((Integer)request[0]).intValue();
 			Object[] params = (Object[])request[1];
             Object tag = request[2];
 			
 			// Delegate to subclasses to handle the specific request
 			requestInProgress = request;
 			handleRequest(type, params, tag);
 			requestInProgress = null;
 			
 			synchronized(requestQueue) {
 				element = requestQueue.element();
 			}
 		}
 		setConnectionState(STATE_IDLE);
 	}
 
 	/**
 	 * Handles a specific request during the REQUESTS state.
 	 * <p>
 	 * Subclasses should implement this to dispatch requests for all
 	 * the request types they know how to handle.
 	 * Subclasses should also call {@link #showStatus(String)} and/or
 	 * {@link #showStatus(String, int)} as necessary to update the
 	 * user on the status of the operation.
 	 * </p>
 	 * 
 	 * @param type Type identifier for the request.
 	 * @param params Parameters for the request.
      * @param tag Tag reference to pass along with the request
      * @throws IOException on I/O errors
      * @throws MailException on protocol errors
 	 */
 	protected abstract void handleRequest(int type, Object[] params, Object tag) throws IOException, MailException;
 	
 	/**
 	 * Handles a specific request being removed from the queue due to errors.
 	 * <p>
 	 * Subclasses should use this as a hook to notify any listeners of the
 	 * request failing.
 	 * </p>
 	 * 
      * @param type Type identifier for the request.
      * @param params Parameters for the request.
      * @param tag Tag reference to pass along with the request
 	 * @param exception Exception that was thrown to fail the request,
 	 * or null if it failed due to a queue flush
 	 */
 	protected void handleRequestFailed(int type, Object[] params, Object tag, Throwable exception) {
         if(this.listener != null) {
            listener.mailConnectionRequestFailed(type, tag, exception);
         }
 	}
 	
     /**
      * Handles the IDLE connection state which occurs after all
      * pending requests have been handled.  This method is called
      * at a periodic interval when no requests are pending.
      * 
      * @throws IOException on I/O errors
      * @throws MailException on protocol errors
      */
 	private void handleIdleConnection() throws IOException, MailException {
 		showStatus(null);
 		handleBeginIdle();
 		
 		synchronized(requestQueue) {
 			if(requestQueue.element() != null) {
 				setConnectionState(STATE_REQUESTS);
 			}
 			else if(connectionThread.isShutdown()) {
 				setConnectionState(STATE_CLOSING);
 			}
 		}
 	}
     
 	/**
 	 * Handles the start of the IDLE state.
 	 * <p>
 	 * Subclasses should do anything they need to do when their connection
 	 * enters an idle state with the server.  This could involve sending
 	 * a command, initializing a timer, or doing nothing at all.
 	 * After this method completes, the connection handler will sleep until
 	 * a new request arrives, or it is commanded to shutdown.
 	 * </p>
      * 
      * @throws IOException on I/O errors
      * @throws MailException on protocol errors
 	 */
 	protected abstract void handleBeginIdle() throws IOException, MailException;
 	
     /**
      * Handles the CLOSING state to close an existing connection.
      * This method should send protocol-specific logout commands and
      * then terminate the connection.
      * 
      * @throws IOException on I/O errors
      * @throws MailException on protocol errors
      */
 	private void handleClosingConnection() throws IOException, MailException {
 		showStatus(resources.getString(LogicMailResource.MAILCONNECTION_CLOSING_CONNECTION));
 		handleBeforeClosing();
 		try { client.close(); } catch (IOException e) {} catch (MailException e) {}
 		setConnectionState(STATE_CLOSED);
 		if(!shutdownInProgress) {
 			MailConnectionManager.getInstance().fireMailConnectionStateChanged(
 					client.getConnectionConfig(),
 					MailConnectionStateEvent.STATE_DISCONNECTED);
 		}
 	}
 
 	/**
 	 * Called at the start of the CLOSING state.
 	 * This method should be overridden if the subclass needs
 	 * to do any cleanup before its connection is closed.
 	 */
 	protected void handleBeforeClosing() { }
 	
 	/**
 	 * Gets the current connection state.
 	 * 
 	 * @return Connection state
 	 */
 	public synchronized int getConnectionState() {
 		return this.state;
 	}
 	
 	/**
 	 * Sets the current connection state.
 	 * 
 	 * @param state Connection state
 	 */
 	protected synchronized void setConnectionState(int state) {
 		if(state >= STATE_CLOSED && state <= STATE_CLOSING) {
 			this.state = state;
 		}
 	}
 	
 	/**
 	 * Gets the request queue.
 	 * Any use of the queue must be synchronized.
 	 * 
 	 * @return Request queue
 	 */
 	protected Queue getRequestQueue() {
 		return this.requestQueue;
 	}
 	
 	/**
 	 * Gets whether a shutdown is currently in progress.
 	 * @return True if shutdown is in progress.
 	 */
 	protected boolean getShutdownInProgress() {
 		return this.shutdownInProgress;
 	}
 	
 	/**
 	 * Sleep the connection thread.
 	 * @param time Time to sleep, in milliseconds.
 	 */
 	protected void sleepConnectionThread(long time) {
 		if(!connectionThread.isShutdown()) {
 			try {
 				ConnectionThread.sleep(time);
 			} catch (InterruptedException e) { }
 		}
 	}
 	
 	/**
 	 * Show a status message.
 	 * 
 	 * @param message The message to show
 	 */
 	protected void showStatus(String message) {
 		MailConnectionManager.getInstance().fireMailConnectionStatus(client.getConnectionConfig(), message);
 	}
 	
 	/**
 	 * Show a status message with a progress percentage.
 	 * 
 	 * @param message The message to show
 	 * @param progress The progress percentage
 	 */
 	protected void showStatus(String message, int progress) {
 		MailConnectionManager.getInstance().fireMailConnectionStatus(client.getConnectionConfig(), message, progress);
 	}
 	
 	/**
 	 * Gets a progress handler for a request with the provided status message.
 	 * <p>
 	 * This default progress handler displays the message along with the
 	 * amount of data downloaded for network updates, and percentage complete
 	 * for processing updates.
 	 * </p>
 	 * 
 	 * @param message The status message for the request
 	 * @return The progress handler
 	 */
 	protected MailProgressHandler getProgressHandler(String message) {
 		return new MailConnectionProgressHandler(message);
 	}
 	
 	/**
 	 * Show an error message.
 	 * 
 	 * @param message The message to show
 	 */
 	private void showError(String message) {
 		MailConnectionManager.getInstance().fireMailConnectionError(client.getConnectionConfig(), message);
 	}
 	
 	/**
 	 * Check the login information, requesting a prompt if necessary.
 	 * 
 	 * @return True if the information was validated, false otherwise.
 	 */
 	private boolean checkLogin(MailClient client) {
         String username = client.getUsername();
         String password = client.getPassword();
         // If the username and password are not null,
         // but are empty, request login information.
         if(invalidLogin || ((username != null && password != null) &&
            (username.trim().equals("") || password.trim().equals("")))) {
 
         	MailConnectionLoginEvent e = new MailConnectionLoginEvent(
         			this,
         			client.getConnectionConfig(),
         			username,
         			password);
     		MailConnectionManager.getInstance().fireMailConnectionLogin(e);
         	
            if(!e.isCanceled()) {
                client.setUsername(e.getUsername());
                client.setPassword(e.getPassword());
                return true;
            }
            else {
         	   return false;
            }
         }
         else {
         	return true;
         }
 	}
 
 	/**
 	 * Handles <tt>IOException</tt>s that occur during a connection.
 	 * These are typically due to network errors, and are handled
 	 * in different ways depending on the connection state:
 	 * <ul>
 	 * <li><b>OPENING</b> - Connection failure, switch to CLOSED and clear the queue.</li>
 	 * <li><b>CLOSING</b> - Disconnection failure, switch to CLOSED and clear the queue.</li>
 	 * <li><b>OPENED/REQUESTS/IDLE</b> - Connection timeout or loss, switch to OPENING to attempt reconnection</li>
 	 * </ul>
 	 * 
 	 * @param e Exception data.
 	 */
 	private void handleIOException(IOException e) {
 		EventLogger.logEvent(AppInfo.GUID, e.toString().getBytes(), EventLogger.ERROR);
 
 		int state = getConnectionState();
 		
 		if(state == STATE_OPENING || state == STATE_CLOSING || retryCount < 2) {
 			// Switch to the CLOSING state and clear the request queue.
 			synchronized (requestQueue) {
 				setConnectionState(STATE_CLOSING);
 				clearRequestQueue(e);
 			}
 		}
 		else {
 			retryCount++;
 			setConnectionState(STATE_OPENING);
 		}
 		showError(e.getMessage());
 
         // Notify failure of the current request-in-progress, if applicable
         if(requestInProgress != null) {
             handleRequestFailed(
                     ((Integer)requestInProgress[0]).intValue(),
                     (Object[])requestInProgress[1],
                     requestInProgress[2],
                     e);
             requestInProgress = null;
         }
 	}
 	
 	/**
 	 * Handles <tt>MailException</tt>s that occur during a connection.
 	 * These are typically due to protocol errors, and can often be recovered
 	 * from.  If they are recoverable, then they are not fatal to the connection.
 	 * Otherwise, they result in a graceful logout and connection termination.
 	 * 
 	 * @param e Exception data.
 	 */
 	private void handleMailException(MailException e) {
 	    boolean disconnect = (e.getCause() == REQUEST_DISCONNECT);
 	    
 	    if(!disconnect) { 
 	        EventLogger.logEvent(AppInfo.GUID, e.toString().getBytes(), EventLogger.ERROR);
 	    }
 	    
 		if(e.isFatal()) {
 			// Switch to the CLOSING state and clear the request queue.
 			synchronized (requestQueue) {
 				setConnectionState(STATE_CLOSING);
 				clearRequestQueue(e);
 			}
 		}
 		if(!disconnect) {
 		    showError(e.getMessage());
 		}
 
         // Notify failure of the current request-in-progress, if applicable
         if(requestInProgress != null) {
             handleRequestFailed(
                     ((Integer)requestInProgress[0]).intValue(),
                     (Object[])requestInProgress[1],
                     requestInProgress[2],
                     e);
             requestInProgress = null;
         }
 	}
 	
 	/**
 	 * Handles any other <tt>Throwable</tt> that occurs during a connection.
 	 * These include any other unknown exception, and are usually caused by
 	 * software bugs that occur during server response parsing.  Since it is
 	 * impossible to know what their cause is, they are handled similarly
 	 * to a fatal <tt>MailException</tt>.
 	 * 
 	 * @param t Throwable data.
 	 */
 	private void handleThrowable(Throwable t) {
 		EventLogger.logEvent(AppInfo.GUID, t.toString().getBytes(), EventLogger.ERROR);
 
 		// Switch to the CLOSING state and clear the request queue.
 		synchronized (requestQueue) {
 			setConnectionState(STATE_CLOSING);
 			clearRequestQueue(t);
 		}
 		showError(t.getMessage());
 
 		// Notify failure of the current request-in-progress, if applicable
 		if(requestInProgress != null) {
             handleRequestFailed(
                     ((Integer)requestInProgress[0]).intValue(),
                     (Object[])requestInProgress[1],
                     requestInProgress[2],
                     t);
             requestInProgress = null;
 		}
 	}
 	
 	/**
 	 * Clear the request queue, sending any necessary failure notifications.
 	 * 
 	 * @param t exception that lead to the queue flush, if applicable
 	 */
 	private void clearRequestQueue(Throwable t) {
 	    synchronized (requestQueue) {
 	        Object element = requestQueue.element();
 	        while(element != null) {
 	            Object[] request = (Object[])element;
 	            requestQueue.remove();
                 handleRequestFailed(
                         ((Integer)request[0]).intValue(),
                         (Object[])request[1],
                         request[2],
                         null);
 	            element = requestQueue.element();
 	        }
 	    }
 	}
 	
 	/**
 	 * Thread for the standard mail connection lifecycle.
 	 */
 	private class ConnectionThread extends Thread {
 		private boolean shutdown = false;
 		private boolean shutdownComplete = false;
 		
 		public ConnectionThread() {
 			super("ConnectionThread");
 		}
 		
 		public void shutdown() {
 			synchronized(this) {
 				shutdown = true;
 			}
 			synchronized(requestQueue) {
 				requestQueue.notifyAll();
 			}
 		}
 		
 		public boolean isShutdown() {
 			boolean result;
 			synchronized(this) {
 				result = shutdown;
 			}
 			return result;
 		}
 		
         public void run() {
         	while(!shutdownComplete) {
         		try {
 	        		switch(state) {
 	        		case STATE_CLOSED:
 	        			handleClosedConnection();
 	        			if(shutdown) {
 	        				shutdownComplete = true;
 	        			}
 	        			break;
 	        		case STATE_OPENING:
 	    				handleOpeningConnection();
 	        			break;
 	        		case STATE_OPENED:
 	    				handleOpenedConnection();
 	        			break;
 	        		case STATE_REQUESTS:
 	    				handlePendingRequests();
 	        			break;
 	        		case STATE_IDLE:
 	    				handleIdleConnection();
 	        			break;
 	        		case STATE_CLOSING:
 	    				handleClosingConnection();
 	        			break;
 	        		}
         		} catch (IOException e) {
         			if(state == STATE_CLOSING) {
         				shutdownComplete = true;
         			}
         			else {
         				handleIOException(e);
         			}
         		} catch (MailException e) {
         			if(state == STATE_CLOSING) {
         				shutdownComplete = true;
         			}
         			else {
         				handleMailException(e);
         			}
         		} catch (Throwable t) {
         			if(state == STATE_CLOSING) {
         				shutdownComplete = true;
         			}
         			else {
         				handleThrowable(t);
         			}
         		}
         	}
         }
 	}
 	
 	/**
 	 * Standard progress handler to be used for mail connection handlers.
 	 * This takes the operation's message and appends a relevant status
 	 * indicator to it.
 	 */
 	private class MailConnectionProgressHandler implements MailProgressHandler {
 		private int total = 0;
 		private int lastTotal = 0;
 		private int threshold = 128;
 		private String messageStart;
 		private String networkMessageEnd = ")...";
 		private String processingMessageEnd = "%)...";
 		
 		public MailConnectionProgressHandler(String message) {
 			this.messageStart = message + " (";
 		}
 		
 		public void mailProgress(int type, int count, int max) {
 			if(type == MailProgressHandler.TYPE_NETWORK) {
 				total += count;
 				if((total - lastTotal) >= threshold) {
 					showStatus(messageStart + StringParser.toDataSizeString(total) + networkMessageEnd);
 					lastTotal = total;
 					if(threshold < 1024 && total >= 1024) { threshold = 1024; }
 				}
 			}
 			else if(type == MailProgressHandler.TYPE_PROCESSING && max > 0){
 				if(count > 0) { count = (count * 100) / max; }
 				showStatus(messageStart + count + processingMessageEnd);
 			}
 		}
 	};
 }
