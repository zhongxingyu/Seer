 package kademlia;
 
 import java.util.List;
 
 import kademlia.messages.StoreRequest;
 import kademlia.messages.StoreResponse;
 
 /**
  * Storing a key value pair in the k closest nodes
  * 
  *
  */

 public class StoreProcess {
 	
 	protected final int threshhold;
 	protected final NetworkInstance networkInstance;
 	protected final StoreRequest request;
 	protected final ResponseListener<StoreResponse> callback;
 	protected final List<Node> recipients;
 	protected int successes;
 	protected int failures;
 	
 	
 	/**
	 * constructor 
 	 * threshhold - Number of Nodes store should be successful to return success
 	 * 				(half the number of recipients)
 	 * @param ni
 	 * @param request
 	 * @param responseListener
 	 */
 	private StoreProcess(NetworkInstance ni, StoreRequest request, ResponseListener<StoreResponse> responseListener){
 		this.networkInstance = ni;
 		this.request = request;
 		this.callback = responseListener;
 		this.recipients = networkInstance.getBuckets().getNearestNodes(request.getKey().getIdentifier(), networkInstance.getConfiguration().getK());
 		this.threshhold = recipients.size()/2;
 		this.successes = 0;
 		this.failures = 0;
 	}
 	
 	
 	/**
 	 * function called to initiate and run the request 
 	 * @param ni NetworkInstance
 	 * @param request the storerequest
 	 * @param responseListener its callback that receives the final confirmation or failure 
 	 */
 	
 	public static void execute(NetworkInstance ni, StoreRequest request, ResponseListener<StoreResponse> responseListener){
 		StoreProcess sp = new StoreProcess(ni, request, responseListener);
 		sp.performStore();
 	}
 	
 	private void performStore(){
 		for(Node node : recipients){
 			networkInstance.sendRequestRPC(node, request, new ResponseListener<StoreResponse>(){
 
 				@Override
 				public void onFailure() {
 					failures++;
 					makeCallback();
 				}
 
 				@Override
 				public void onResponseReceived(StoreResponse response) {
 					successes++;
 					makeCallback();
 				}
 				
 				public void makeCallback(){
 					if(successes + failures == recipients.size()){
 						if(successes >= threshhold){
 							callback.onResponseReceived(new StoreResponse(true));
 						}
 						else{
 							callback.onFailure();
 						}
 					}
 				}
 				
 			});
 		}
 	}
 
 }
