 package it.polimi.elet.selflet.message;
 
 import it.polimi.elet.selflet.id.ISelfLetID;
 import it.polimi.elet.selflet.istantiator.IVirtualMachineIPManager;
 import it.polimi.elet.selflet.istantiator.SelfletIstantiatorThread;
 import it.polimi.elet.selflet.istantiator.VirtualMachineIPManager;
 import it.polimi.elet.selflet.negotiation.nodeState.INodeState;
 import it.polimi.elet.selflet.negotiation.nodeState.NodeState;
 import it.polimi.elet.selflet.nodeState.INodeStateManager;
 import it.polimi.elet.selflet.nodeState.NodeStateManager;
 import it.polimi.elet.thread.ThreadPool;
 
 import org.apache.log4j.Logger;
 
 import polimi.reds.Message;
 import polimi.reds.TCPDispatchingService;
 
 /**
  * A thread that receives messages and takes the appropriate action
  * 
  * 
  * @author Nicola Calcavecchia <calcavecchia@gmail.com>
  * */
 public class MessageDispatchingThread extends Thread {
 
 	private static final Logger LOG = Logger
 			.getLogger(MessageDispatchingThread.class);
 
 	private static final int WAIT_STEP_MS = 50;
 
 	private final INodeStateManager nodeStateManager = NodeStateManager
 			.getInstance();
 	private final ISelfletNeighbors selfletNeighbors = SelfletNeighbors
 			.getInstance();
 	private final IVirtualMachineIPManager virtualMachineIPManager = VirtualMachineIPManager
 			.getInstance();
 
 	private final TCPDispatchingService dispatchingService;
 	private boolean stop;
 
 	public MessageDispatchingThread(TCPDispatchingService dispatchingService) {
 		this.dispatchingService = dispatchingService;
 		this.stop = false;
 	}
 
 	@Override
 	public void run() {
 
 		try {
 			while (!stop) {
 
 				while (dispatchingService.hasMoreMessages()) {
 					Message message = dispatchingService.getNextMessage();
 
 					if (message instanceof RedsMessage) {
 						RedsMessage redsMessage = (RedsMessage) message;
 						SelfLetMsg selfletMessage = redsMessage.getMessage();
 						LOG.info("Received selflet message: " + selfletMessage);
 						analyzeMessage(selfletMessage);
 					} else {
 						LOG.info("Received other kind of message: " + message);
 					}
 				}
 
 				killZombieSelflets();
 
 				goToSleep();
 
 			}
 		} catch (Exception e) {
 			LOG.error(e);
 		}
 
 	}
 
 	private void goToSleep() {
 		// Wait
 		try {
 			Thread.sleep(WAIT_STEP_MS);
 		} catch (InterruptedException e) {
 			LOG.error(e);
 		}
 
 	}
 
 	private void analyzeMessage(SelfLetMsg selfletMessage) {
 		switch (selfletMessage.getType()) {
 
 		case ALIVE_SELFLET:
 			aliveSelfletMessage(selfletMessage);
 			break;
 
 		case NODE_STATE:
 			nodeStateMessage(selfletMessage);
 			break;
 
 		case ISTANTIATE_NEW_SELFLET:
 			istantiateNewSelfletMessage(selfletMessage);
 			break;
 
 		case REMOVE_SELFLET:
 			removeSelflet(selfletMessage);
 			break;
 
 		default:
 			LOG.warn("Ignoring message: " + selfletMessage);
 		}
 	}
 
 	private void removeSelflet(SelfLetMsg selfletMessage) {
 		LOG.debug("Received request to remove selflet "
 				+ selfletMessage.getFrom());
 		ISelfLetID selfletToBeRemoved = selfletMessage.getFrom();
 		removeSelflet(selfletToBeRemoved);
 	}
 	
 	private void removeSelflet(ISelfLetID selfletToBeRemoved){
 		nodeStateManager.removeNodeStateOfNeighbor(selfletToBeRemoved);
 		virtualMachineIPManager.freeIPOfSelflet(selfletToBeRemoved);
 	}
 
 	private void istantiateNewSelfletMessage(SelfLetMsg selfletMessage) {
 		SelfletIstantiatorThread selfletIstantiatorThread = new SelfletIstantiatorThread(
 				dispatchingService, selfletMessage);
 		ThreadPool.submitGenericJob(selfletIstantiatorThread);
 		LOG.debug("SelfletIstantiatorThread started");
 	}
 
 	private void nodeStateMessage(SelfLetMsg selfletMessage) {
 		LOG.debug("Received node state from " + selfletMessage.getFrom());
 		nodeStateManager.addState((INodeState) selfletMessage.getContent());
 	}
 
 	private void aliveSelfletMessage(SelfLetMsg selfletMessage) {
 		ISelfLetID from = selfletMessage.getFrom();
 		selfletNeighbors.addNeighbor(from);
 	}
 
 	private void killZombieSelflets() {
 		for (INodeState nodeState : nodeStateManager.getStates()) {
 			if (!selfletNeighbors.getNeighbors().contains(
 					nodeState.getSelfletID())) {
				virtualMachineIPManager.freeIPOfSelflet(nodeState.getSelfletID());
 			}
 		}
 	}
 
 }
