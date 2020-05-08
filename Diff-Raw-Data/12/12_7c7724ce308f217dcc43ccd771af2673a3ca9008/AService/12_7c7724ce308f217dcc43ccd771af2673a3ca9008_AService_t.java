 package org.paxle.p2p.services.impl;
 
 import net.jxta.discovery.DiscoveryService;
 import net.jxta.endpoint.Message;
 import net.jxta.peergroup.PeerGroup;
 import net.jxta.pipe.InputPipe;
 import net.jxta.pipe.PipeService;
 import net.jxta.protocol.PipeAdvertisement;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.paxle.p2p.impl.P2PManager;
 import org.paxle.p2p.services.IService;
 
 public abstract class AService extends Thread implements IService {
 	protected Log logger = LogFactory.getLog(this.getClass().getName());
 	
 	/**
 	 * paxle p2p-manager. required for access to the application-peer-group.
 	 */
 	protected P2PManager p2pManager = null;
 
 	/**
 	 * The {@link #pg p2p-group} where the service should be published to
 	 */
 	protected PeerGroup pg = null;
 
 	/**
 	 * JXTA pipe-service. Required for creation of input- and output-pipes.
 	 */
 	protected PipeService pgPipeService = null;
 	
 	/**
 	 * JXTA discovery-service. Required to publish and lookup advertisements
 	 */
 	protected DiscoveryService pgDiscoveryService = null;	
 	
 	/**
 	 * indicates if this thread was terminated
 	 * @see #terminate()
 	 */
 	protected boolean stopped = false;
 
 	/**
 	 * The {@link PipeAdvertisement pipe-advertisement} used to create the
 	 * {@link #serviceInputPipe input-pipe}
 	 */
 	protected PipeAdvertisement servicePipeAdv = null;
 	
 	/**
 	 * a input-pipe to receive incoming requests
 	 */
 	protected InputPipe serviceInputPipe = null;	
 	
 	/**
 	 * The amount of messages received via the {@link #serviceInputPipe input-pipe}
 	 * @see #getReceivedMessageCount()
 	 */
 	protected long receivedMsgCount = 0;
 	
 	/**
 	 * The amount of bytes received via the {@link #serviceInputPipe input-pipe}
 	 * @see #getRecievedBytesCount()
 	 */
 	protected long receivedBytes = 0;
 	
 	/**
 	 * The amount of messages sent out to a remote peer
 	 * @see #getSentMessageCount()
 	 */
 	protected long sentMsgCount = 0;
 	
 	/**
 	 * The amount of bytes sent out to a remote peer
 	 * @see #getSentBytesCount()
 	 */
 	protected long sentBytes = 0;
 	
 	
 	protected AService(P2PManager p2pManager) {
 		if (p2pManager == null) throw new NullPointerException("P2PManager is null");
 		this.p2pManager = p2pManager;
 		
 		// get required services from peer group (e.g. pipe- and discovery-service)
 		this.getPeerGroupServices(this.p2pManager.getPeerGroup());
 		
 		// start this thread
 		this.start();
 	}	
 	
 	private void getPeerGroupServices(PeerGroup appPeerGroup) {
 		if (appPeerGroup == null) throw new NullPointerException("Peer group is null");
 
 		// the peer the service should be deployed to
 		this.pg = appPeerGroup;
 
 		// some peer group services needed later
 		this.pgPipeService = this.pg.getPipeService();
 		this.pgDiscoveryService = this.pg.getDiscoveryService();
 	}
 	
 	/**
 	 * This function is used to listen for new incoming messages. 
 	 * Received messages are then passed to function {@link #process(Message)}.
 	 */
 	public void run() {
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		
 		// init server side of service
 		this.init();
 		
 		while (!this.stopped && !Thread.interrupted()) {
             try {
             	// fetch the next message
             	Message nextMsg = serviceInputPipe.waitForMessage();
             	this.receivedMsgCount++;
             	this.receivedBytes += nextMsg.getByteLength();
             	System.out.println(nextMsg);
 
             	// process message
             	this.process(nextMsg);
             } catch (InterruptedException e) {
                 Thread.interrupted();
                 this.stopped = true;
             } catch (Exception e) {
             	this.logger.error(String.format("Unexpected %s while listening for new messages on pipe %s [%s]",
             			e.getClass().getName(),
             			this.serviceInputPipe.getName(),
             			this.serviceInputPipe.getPipeID()
             	),e);
             }
         }
 	}
 	
 	/**
 	 * Function to terminate this service
 	 */
 	public void terminate() {
 		this.stopped = true;
 		this.interrupt();
 		try {
 			this.join(15000);
 		} catch (InterruptedException e) {
 			/* ignore this */
 		}		
 	};	
 	
 	protected Message createMessage() {
		Message msg = new Message();
		
		/* TODO: add some data about the peer, e.g.
		 * - version number
		 * - peer ID?
		 * - processing time?
		 */
		return msg;
 	}
 	
 	public long getReceivedMessageCount() {
 		return this.receivedMsgCount;
 	}
 	
 	public long getRecievedBytesCount() {
 		return this.receivedBytes;
 	}
 	
 	public long getSentMessageCount() {
 		return this.sentMsgCount;
 	}
 	
 	public long getSentBytesCount() {
 		return this.sentBytes;
 	}
 	
 	/**
 	 * This method is executed from within {@link #run()} and is used to init the service
 	 */
 	protected abstract void init();
 	
 	/**
 	 * Function to create the advertisement for the {@link #serviceInputPipe input-pipe} 
 	 * @return
 	 */
 	protected abstract PipeAdvertisement createPipeAdvertisement();	
 	
 	
 	/**
 	 * Process a request message received via the {@link #serviceInputPipe input-pipe}
 	 * @param msg a request message received via the {@link #serviceInputPipe input-pipe}
 	 */
 	protected abstract void process(Message msg);		
 	
 	/**
 	 * @see IService#pauseService()
 	 */
 	public void pauseService() {
 		// TODO close the input queue for a while?
 		throw new RuntimeException("Not implemented.");
 	}
 	
 	/**
 	 * @see IService#resumeService()
 	 */
 	public void resumeService() {
 		// TODO
 		throw new RuntimeException("Not implemented.");
 	}
 	
 	/**
 	 * @see IService#isPaused()
 	 */
 	public boolean isPaused() {
 		// TODO
 		throw new RuntimeException("Not implemented.");
 	}
 }
