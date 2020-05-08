 package kademlia;
 
 import java.io.Serializable;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import kademlia.messages.FindNodeRequest;
 import kademlia.messages.FindNodeResponse;
 import kademlia.messages.FindValueRequest;
 import kademlia.messages.FindValueResponse;
 import kademlia.messages.PingRequest;
 import kademlia.messages.PingResponse;
 import kademlia.messages.Request;
 import kademlia.messages.Response;
 import kademlia.messages.StoreRequest;
 import kademlia.messages.StoreResponse;
 import rpc.RPCEvent;
 import rpc.RPCHandler;
 import rpc.RPCResponseListener;
 
 import com.google.gson.Gson;
 
 import dht.DistributedMap;
 import dht.LocalDataStore;
 import dht.MapDataFilter;
 

 /**
 * 
 *
  */
 public class NetworkInstance {
 	protected Identifier localIdentifier;
 	protected final Buckets buckets;
 	protected final Gson gson;
 	protected final String rpcServiceName;
 	protected final Map<String, MapDataFilter<String, String>> dataFilters;
 	protected final LocalDataStore localDataStore;
 	protected final RPCHandler rpcHandler;
 	
 	public NetworkInstance(RPCHandler rpcHandler) {
 		gson = new Gson();
 		rpcServiceName = "kad";
 		buckets = new Buckets(this);
 		dataFilters = new HashMap<String, MapDataFilter<String, String>>();
 		localDataStore = new LocalDataStore();
 		this.rpcHandler = rpcHandler;
 		rpcHandler.registerServiceListener(rpcServiceName, new KademliaRequestListener(this));
 		localIdentifier = Identifier.generateRandom(); // Possibly remember for future restarts
 	}
 	
 	
 	// TODO: this constructor only for testing of buckets
 	protected NetworkInstance(byte[] bytes){
 		gson = null;
 		rpcServiceName = "kad";
 		buckets = new Buckets(this);
 		dataFilters = null;
 		localDataStore = null;
 		this.rpcHandler = null;
 		localIdentifier = Identifier.fromBytes(bytes); // Possibly remember for future restarts		
 	}
 	
 	LocalDataStore getLocalDataStore() {
 		return localDataStore;
 	}
 	
 	public void registerDataFilter(String primaryKey, MapDataFilter<String, String> dataFilter) {
 		localDataStore.registerDataFilter(primaryKey, dataFilter);
 	}
 	
 	public Configuration getConfiguration() {
 		return new Configuration();
 	}
 	
 	public Identifier getLocalNodeIdentifier() {
 		return localIdentifier;
 	}
 	
 	public DistributedMap<Serializable, Serializable> getPrimaryDHT() {
 		return null;
 	}
 	
 	protected <T extends Response> void sendRequestRPC(Node destination, Request request, final Class<T> responseClass, final ResponseListener<T> callback) {
 		String requestData = gson.toJson(request);
 		getRPC().sendRequest(destination.getNetworkURI(), rpcServiceName, requestData, new RPCResponseListener() {
 			public void onResponseReceived(RPCEvent event) {
 				try {
 					callback.onResponseReceived(gson.fromJson(event.getDataString(), responseClass));
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	public RPCHandler getRPC() {
 		return rpcHandler;
 	}
 
 	public Buckets getBuckets() {
 		return buckets;
 	}
 	
 	public void findNode(Identifier targetNodeId, boolean stopOnFound, ResponseListener<FindNodeResponse> responseListener) {
 		FindNodeRequest request = new FindNodeRequest(getLocalNodeIdentifier(), targetNodeId);
 		FindProcess.execute(this, request, stopOnFound, FindNodeResponse.class, responseListener);
 	}
 	
 	public void findNode(Identifier targetNodeId, ResponseListener<FindNodeResponse> responseListener) {
 		findNode(targetNodeId, true, responseListener);
 	}
 	
 	public void findValue(Key targetKey, ResponseListener<FindValueResponse> responseListener) {
 		String value = getLocalDataStore().get(targetKey);
 		if (value != null) {
 			responseListener.onResponseReceived(new FindValueResponse(value, new LinkedList<Node>()));
 			//NOTE: should we include nearby nodes since we have the value locally?
 		}
 		
 		FindValueRequest request = new FindValueRequest(getLocalNodeIdentifier(), targetKey);
 		FindProcess.execute(this, request, true, FindValueResponse.class, responseListener);
 	}
	
 	public void storeValue(Key key, String value, boolean publish, ResponseListener<StoreResponse> responseListener) {
 		getLocalDataStore().put(key, value);
 		StoreRequest request = new StoreRequest(getLocalNodeIdentifier(), key, value);
 		StoreProcess.execute(this, request, responseListener);
 	}
 	
 	//Maybe put in Node
 	public void ping(Node targetNode, ResponseListener<PingResponse> responseListener) {
 		PingRequest request = new PingRequest(getLocalNodeIdentifier(), targetNode.getIdentifier());
 		this.sendRequestRPC(targetNode, request, PingResponse.class, responseListener);
 	}
 	
 	public void bootstrap(List<URI> friends, BootstrapListener bootstrapListener) {
 		BootstrapProcess.execute(this, friends, bootstrapListener);
 	}
 }
