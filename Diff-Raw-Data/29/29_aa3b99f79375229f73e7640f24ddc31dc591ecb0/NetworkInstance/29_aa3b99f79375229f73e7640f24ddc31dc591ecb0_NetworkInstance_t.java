 package kademlia;
 
 import java.io.Serializable;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.net.URI;
 import java.util.HashMap;
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
 import rpc.ServiceRequestListener;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
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
 	
 	protected <T extends Response> void sendRequestRPC(Node destination, Request request, final ResponseListener<T> callback) {
 		String requestData = gson.toJson(request);
 		getRPC().sendRequest(destination.getNetworkURI(), rpcServiceName, requestData, new RPCResponseListener() {
 			@SuppressWarnings("unchecked")
 			@Override
 			public void onResponseReceived(RPCEvent event) {
 				try {
					Type responseType = ((ParameterizedType) new TypeToken<List<T>>(){}.getType()).getActualTypeArguments()[0]; //TODO: Type detection is broken
 					callback.onResponseReceived((T) gson.fromJson(event.getDataString(), responseType));
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
 	
 	public void findNode(Identifier targetNodeId, ResponseListener<FindNodeResponse> responseListener) {
 		FindNodeRequest request = new FindNodeRequest(getLocalNodeIdentifier(), targetNodeId);
 		FindProcess.execute(this, request, responseListener);
 	}
 	
 	public void findValue(Key targetKey, ResponseListener<FindValueResponse> responseListener) {
 		FindValueRequest request = new FindValueRequest(getLocalNodeIdentifier(), targetKey);
 		FindProcess.execute(this, request, responseListener);
 	}
 	
 	public void storeValue(Key key, String value, ResponseListener<StoreResponse> responseListener, boolean publish) {
 		StoreRequest request = new StoreRequest(getLocalNodeIdentifier(), key, value);
 		StoreProcess.execute(this, request, responseListener);
 	}
 	
 	//Maybe put in Node
 	public void ping(Node targetNode, ResponseListener<PingResponse> responseListener) {
 		PingRequest request = new PingRequest(getLocalNodeIdentifier(), targetNode.getIdentifier());
 		this.sendRequestRPC(targetNode, request, responseListener);
 	}
 	
 	public void bootstrap(List<URI> friends, BootstrapListener bootstrapListener) {
 		BootstrapProcess.execute(this, friends, bootstrapListener);
 	}
 }
