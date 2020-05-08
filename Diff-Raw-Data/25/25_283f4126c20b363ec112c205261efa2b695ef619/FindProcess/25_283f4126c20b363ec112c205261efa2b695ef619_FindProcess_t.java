 package org.peerbox.kademlia;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.peerbox.kademlia.messages.FindRequest;
 import org.peerbox.kademlia.messages.FindResponse;
 
 
 /**
  * 
  * Object created when a node or a value is searched for
  *
  * @param <FRT> the type of FindResponse FindNodeResponse or a FindValueResponse
  */
 public class FindProcess<FRT extends FindResponse> {
 	protected final int maxRequests;
 	protected final int searchSetSize;				
 	protected final Set<Node> prevQueried;			// previously queried nodes
 	protected final Set<Node> nearestSet;		// nodes that have been found through all searches
 	protected final Set<Node> current;				// nodes currently being queried but haven't replied
 	protected final FindRequest findRequest;						// target being searched for
 	protected final NetworkInstance networkInstance;
 	protected final ResponseListener<FRT> callback;
 	protected final Class<FRT> responseClass;
 	protected FRT lastResponse;
 	protected FRT foundResponse;
 	protected final boolean stopOnFound;
 	protected boolean done;
 
 	//TODO: a FindProcessResponse<FRT> object should be sent to the callback to send store to the nearest nodes that do not find the value
 	
 	
 	private FindProcess(NetworkInstance ni, FindRequest request, boolean stopOnFound, Class<FRT> responseClass, ResponseListener<FRT> responseListener){
 		networkInstance = ni;
 		maxRequests = networkInstance.getConfiguration().getAlpha() * networkInstance.getConfiguration().getAlpha();
 		searchSetSize = networkInstance.getConfiguration().getK() * 2;
 		findRequest = request;
 		prevQueried = new HashSet<Node>();
 		nearestSet = Collections.synchronizedSet(new TreeSet<Node>(new IdentifiableDistanceComparator(findRequest.getTargetIdentifier())));
 		nearestSet.addAll(networkInstance.getBuckets().getNearestNodes(findRequest.getTargetIdentifier(), searchSetSize));
 		current = new HashSet<Node>();
 		callback = responseListener;
 		this.responseClass = responseClass;
 		this.stopOnFound = stopOnFound;
 		lastResponse = null;
 		foundResponse = null;
 		done = false;
 	}
 	
 	/**
 	 * 
 	 * To initialize the find process
 	 * @param <T> type of FindResponse
 	 * @param ni NetworkInstance
 	 * @param request the request to be sent
 	 * @param responseListener callback
 	 */
	public static <T extends FindResponse> void execute(NetworkInstance ni, FindRequest request, boolean stopOnFound, Class<T> responseClass, ResponseListener<T> responseListener){
 		FindProcess<T> sh = new FindProcess<T>(ni, request, stopOnFound, responseClass, responseListener);
 		LinkedList<Node> unsearchedNodes = sh.computeUnsearchedNodes();
 		if(unsearchedNodes.isEmpty()){
 			responseListener.onFailure();
			return;
 		}
 		sh.nextIteration(unsearchedNodes);
 	}
 	
 	
 	/**
 	 * To actually run the FindProcess
 	 * 
 	 * 
 	 */
 	private void nextIteration(final LinkedList<Node> unsearchedNodes){
 		// Until upto maxRequests are made
 		while(current.size() < maxRequests && !unsearchedNodes.isEmpty()){
 			// send the findRequest RPC to the first one in the unsearched nodes
 			final Node nextRequestDestination = unsearchedNodes.poll();		
 						// poll retrieves and removes the first element
 		  	prevQueried.add(nextRequestDestination);
 		  	current.add(nextRequestDestination);
 			networkInstance.sendRequestRPC(nextRequestDestination, findRequest, responseClass, new ResponseListener<FRT>(){
 		    	// event that a message was received
 		    	public void onResponseReceived(FRT response) {
 		    		// if the reply contains the target then trigger identifier found
 	          		nearestSet.addAll(response.getNearbyNodes());
 	    			networkInstance.getBuckets().addAll(response.getNearbyNodes());
 	          		current.remove(nextRequestDestination);
 		          	if(response.isFound()){
 		          		if(stopOnFound){
		          			if(!done) {
		          				done = true;
 		          				callback.onResponseReceived(response);
		          			}
		          			
 		          		}
 		          		else{
 		          			foundResponse = response;
 		          			attemptDone(computeUnsearchedNodes());
 		          		}
 		          	}
 		          	// otherwise add the nodes to the nearestSet and to the k-buckets 
 		          	else{
 						lastResponse = response;
 						attemptDone(computeUnsearchedNodes());
 		          	}
 		      	}		    	
 		    	
 		    	// event that the message timed out
 		      	public void onFailure(){
 		      		current.remove(nextRequestDestination);
 		      		attemptDone(unsearchedNodes);
 		      	}
 		    });
 		}
 	}
 	
 	private void attemptDone(LinkedList<Node> unsearchedNodes) {
 		if(!done) {
 			// if no new unsearched nodes and queue is over means the requested value wasn't found
 			if(unsearchedNodes.isEmpty() && current.isEmpty()) {
 				FRT response = foundResponse == null ? lastResponse : foundResponse;
 				if(response != null) {
 					// if there has been at least one response and no more unsearched nodes
 					// then callback with the k nearest sets
 					List<Node> nearbyNodes = new ArrayList<Node>(nearestSet);
 					int k = networkInstance.getConfiguration().getK();
 					List<Node> kNearbyNodes = nearbyNodes.subList(0, nearestSet.size() > k ? k : nearestSet.size());
 					response.setNearbyNodes(kNearbyNodes);
					done = true;
 					callback.onResponseReceived(response);	
 				}
 				else {
					done = true;
 		  			callback.onFailure();
 		  		}
 			}
 			else if(!unsearchedNodes.isEmpty()){
 				nextIteration(unsearchedNodes);
 			}
 		}
 	}
 	
 	private LinkedList<Node> computeUnsearchedNodes(){			
 		// resize the nearestSet by removing the last elements
 /*		while(nearestSet.size() > nearestSetSize){
 			nearestSet.remove(nearestSet.last());				
 		}*/
 		//If we iterate over nearest set, remember to do so in a synchronized block
 		//synchronized(nearestSet) { }
 		
 		// find the unsearched nodes by removing the previously queried from the nearest set
 		LinkedList<Node> unsearchedNodes = new LinkedList<Node>();
 		int iteration = 0;
 		for(Node node : nearestSet){
 			if(iteration == searchSetSize)
 				break;
 			if(!prevQueried.contains(node)){
 				unsearchedNodes.add(node);				
 			}
 			iteration++;
 		}
 		return unsearchedNodes;		
 	}
 	
 }
