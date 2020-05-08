 package de.newsarea.homecockpit.connector.util;
 
 import de.newsarea.homecockpit.connector.Connector;
 import de.newsarea.homecockpit.connector.event.ConnectorEventListener;
 import org.apache.commons.lang3.event.EventListenerSupport;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.net.ConnectException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class ConnectorConnectionHelper {
 	
 	private static Logger log = LoggerFactory.getLogger(ConnectorConnectionHelper.class);
 	
 	private Connector connector;
 	private EventListenerSupport<ConnectorEventListener> connectorEventListeners;
 	
 	private ConnectionWorkerThread connectionWorkerThread;
 
 	public ConnectorConnectionHelper(Connector connector) {
 		this.connector = connector;
 		this.connectionWorkerThread = new ConnectionWorkerThread(connector);
 		this.connectionWorkerThread.addEventListener(new ConnectorEventListener() {			
 			@Override
 			public void stateChanged(Connector connector, State state) {
 				fireEvent(state);
 			}
 		});
 	}
 	
 	public void open() throws ConnectException {
 		fireEvent(ConnectorEventListener.State.OPEN);
 		connector.open();
 		fireEvent(ConnectorEventListener.State.CONNECTED);
 	}
 
 	public void open(int retryTimeout) {
 		connectionWorkerThread.setRetryTimeout(retryTimeout);
 		connectionWorkerThread.start();
 	}
 	
 	public void close() {
 		connectionWorkerThread.exit();
 		try {
 			connectionWorkerThread.join();
             log.debug("connectionWorkerThread joined - {}", connector);
 		} catch (InterruptedException e) {
 			log.error(e.getMessage(), e);
 		}
 		connector.close();
         log.debug("connector closed - {}", connector);
 		fireEvent(ConnectorEventListener.State.CLOSED);
 	}
 	
 	/* EVENT */
 	
 	public void addEventListener(ConnectorEventListener connectorEventListener) {
        if(connectorEventListeners == null) {
            connectorEventListeners = EventListenerSupport.create(ConnectorEventListener.class);
        }
 		connectorEventListeners.addListener(connectorEventListener);
 	}
 
 	private void fireEvent(ConnectorEventListener.State state) {
 		connectorEventListeners.fire().stateChanged(connector, state);
 	}
 	
 	/* THREAD */
 	
 	private static class ConnectionWorkerThread extends Thread {
 
         private EventListenerSupport<ConnectorEventListener> connectorEventListeners;
 		
 		private Connector connector;
 		private boolean exit = false;
 		
 		private int retryTimeout = 0;
         private long lastTryTime = 0;
 		
 		public void setRetryTimeout(int timeout) {
 			this.retryTimeout = timeout;
 		}
 		
 		public ConnectionWorkerThread(Connector connector) {
 			this.connector = connector;
 		}
 		
 		public void run() {
             exit = false;
             while(!exit) {
                 long tryTimeDiff = (new Date()).getTime() - lastTryTime;
                 if(tryTimeDiff > retryTimeout) {
                     try {
                         fireEvent(ConnectorEventListener.State.OPEN);
                         connector.open();
                         fireEvent(ConnectorEventListener.State.CONNECTED);
                         return;
                     } catch(ConnectException e) {
                     } catch(Exception e) {
                         log.error(e.getMessage(), e);
                     }
                     lastTryTime = (new Date()).getTime();
                 }
 				//
 				try {
 					Thread.sleep(1);
 				} catch (InterruptedException e) {
 					log.error(e.getMessage(), e);
 				}
 			}
 		}
 		
 		public void exit() {
 			exit = true;
 		}
 		
 		public void addEventListener(ConnectorEventListener eventListener) {
            if(connectorEventListeners == null) {
                connectorEventListeners = EventListenerSupport.create(ConnectorEventListener.class);
            }
 			connectorEventListeners.addListener(eventListener);
 		}
 
 		private void fireEvent(ConnectorEventListener.State state) {
 			connectorEventListeners.fire().stateChanged(connector, state);
 		}
 		
 	}
 
     @Override
     public String toString() {
         return "ConnectorConnectionHelper{" +
                 "connector=" + connector +
                 '}';
     }
 }
