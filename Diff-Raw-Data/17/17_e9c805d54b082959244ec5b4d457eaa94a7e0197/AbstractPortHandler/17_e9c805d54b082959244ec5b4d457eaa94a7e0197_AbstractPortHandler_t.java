 package dk.dtu.imm.distributedsystems.projects.sensornetwork.common.components;
 
 import java.net.SocketException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.exceptions.ConnectionHandlerException;
 
 /**
  * The Class AbstractPortHandler.
  *
  * @author Maciej Kucharek <a href="mailto:s091828 (at) student.dtu.dk">s091828 (at) student.dtu.dk</a>
  */
 public abstract class AbstractPortHandler extends Thread {
 
 	/** The logger. */
 	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
 	
 	/** The node id. */
 	protected String nodeId;
 
 	/**
 	 * Instantiates a new abstract port handler.
 	 *
 	 * @param portNumber the port number
 	 */
 	protected AbstractPortHandler(String nodeId) {
 		this.nodeId = nodeId;
 	}
 	
 	/**
 	 * Gets the node id.
 	 *
 	 * @return the node id
 	 */
 	public String getNodeId() {
 		return nodeId;
 	}
 
 	/**
 	 * Sets the up.
 	 *
 	 * @throws SocketException the socket exception
 	 */
 	protected abstract void setUp() throws ConnectionHandlerException;
 	
 	/**
 	 * Handle connection.
 	 *
 	 * @throws ConnectionHandlerException the connection handler exception
 	 */
 	protected abstract void handleConnection() throws ConnectionHandlerException, InterruptedException;
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Thread#run()
 	 */
 	@Override
 	public void run() {
 		
 		try {
 			setUp();
 			
 		} catch (ConnectionHandlerException e) {
			logger.error(e.getMessage(), e);
 			return;
 		}
 
 		while (true) {
 
 			try {
 				
 				handleConnection();
 
 			} catch (InterruptedException e) {
 				logger.debug("Thread interrupted");
 				return;
 			} catch (ConnectionHandlerException e) {
				
				// check if the thread was interrupted - if so, 
				// this is because of the closed socket or sth.
				if(!this.isInterrupted()) {
					// not interrupted - error occured.
					logger.error(e.getMessage(), e);
				}
 				return;
 			}
 		}
 
 	}
 
 }
