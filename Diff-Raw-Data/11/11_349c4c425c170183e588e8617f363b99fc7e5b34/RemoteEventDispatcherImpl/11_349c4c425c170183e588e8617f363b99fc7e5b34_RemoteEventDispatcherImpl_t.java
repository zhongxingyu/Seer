 package ar.edu.itba.pod.legajo48421.event;
 
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import ar.edu.itba.event.EventInformation;
 import ar.edu.itba.event.RemoteEventDispatcher;
 import ar.edu.itba.node.Node;
 import ar.edu.itba.node.NodeInformation;
 import ar.edu.itba.node.api.ClusterAdministration;
 import ar.edu.itba.pod.agent.runner.Agent;
 
 public class RemoteEventDispatcherImpl implements RemoteEventDispatcher {
 
 
 	private final BlockingQueue<Object> queue;
 	private final NodeInformation node;
 	private final BlockingQueue<Object> processingQueue;
 
 
 	public RemoteEventDispatcherImpl(final NodeInformation node){
 		this.queue = new LinkedBlockingQueue<Object>();
 		this.node = node;
 		this.processingQueue = new LinkedBlockingQueue<Object>();
		UnicastRemoteObject.exportObject(this, 0);
 		Thread getQueueEvent = new Thread(){
 			@Override
 			public void run() {
 				while(true){
 					Object event = queue.poll();
 					if(event!=null){
 						try {
 							System.out.println(event);
 							((EventInformation)event).setReceivedTime(System.currentTimeMillis());
 							processingQueue.add((EventInformation)event);
 							Registry registry = LocateRegistry.getRegistry(node.host(), node.port());
 							ClusterAdministration cluster = (ClusterAdministration)registry.lookup(Node.CLUSTER_COMUNICATION);
 							for(NodeInformation connectedNode : cluster.connectedNodes()){
 								if(!connectedNode.equals(node)){
 									Registry connectedRegistry = LocateRegistry.getRegistry(connectedNode.host(), connectedNode.port());
									RemoteEventDispatcher remoteEventDispatcher = (RemoteEventDispatcher)connectedRegistry.lookup(Node.DISTRIBUTED_EVENT_DISPATCHER);
									remoteEventDispatcher.publish((EventInformation)event);
 								}
 							}
 
 							//} catch (InterruptedException e) {
 							//	e.printStackTrace();
 						} catch (RemoteException e) {
 							e.printStackTrace();
 						} catch (NotBoundException e) {
 							e.printStackTrace();
 						}
 					}
 					try {
 						Thread.sleep(3000);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		};
 		getQueueEvent.start();
 
 		Thread getNewEvent = new Thread(){
 			@Override
 			public void run() {
 				Registry registry;
 				try {
 					registry = LocateRegistry.getRegistry(node.host(), node.port());
 					ClusterAdministration cluster = (ClusterAdministration)registry.lookup(Node.CLUSTER_COMUNICATION);
 					for(NodeInformation connectedNode : cluster.connectedNodes()){
 						registry = LocateRegistry.getRegistry(connectedNode.host(), connectedNode.port());
						RemoteEventDispatcher connectedEventDispatcher = (RemoteEventDispatcher)registry.lookup(Node.DISTRIBUTED_EVENT_DISPATCHER);
						queue.addAll(connectedEventDispatcher.newEventsFor(node));
 					}
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				} catch (NotBoundException e) {
 					e.printStackTrace();
 				}
 			}
 		};
 		getNewEvent.start();
 	}
 
 	@Override
 	public boolean publish(EventInformation event) throws RemoteException,
 	InterruptedException {
 		if(!queue.contains(event) && !processingQueue.contains(event)){
 			queue.add(event);
 			return true;
 		}
 		return false;
 	}
 
 	private Set<EventInformation> findInQueue(String nodeId) {
 		Set<EventInformation> result = new HashSet<EventInformation>();
 		for(Object event : queue){
 			EventInformation eventInfo = (EventInformation)event;
 			if(eventInfo.nodeId().equals(nodeId))
 				result.add(eventInfo);
 		}
 		return result;
 	}
 
 	@Override
 	public Set<EventInformation> newEventsFor(NodeInformation nodeInformation)
 			throws RemoteException {
 		return findInQueue(nodeInformation.id());
 	}
 
 	@Override
 	public BlockingQueue<Object> moveQueueFor(Agent agent)
 			throws RemoteException {
 		return null;
 	}
 
 }
