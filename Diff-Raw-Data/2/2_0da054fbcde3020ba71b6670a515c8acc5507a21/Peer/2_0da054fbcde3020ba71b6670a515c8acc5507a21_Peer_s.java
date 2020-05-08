 package br.edu.ufcg.lsd.oursim.entities.grid;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import br.edu.ufcg.lsd.oursim.entities.ActiveEntity;
 import br.edu.ufcg.lsd.oursim.entities.accounting.ReplicaAccounting;
 import br.edu.ufcg.lsd.oursim.entities.allocation.Allocation;
 import br.edu.ufcg.lsd.oursim.entities.request.PeerRequest;
 import br.edu.ufcg.lsd.oursim.events.peer.WorkerState;
 
 public class Peer extends ActiveEntity {
 
 	private Map<String, WorkerState> workersStates = new HashMap<String, WorkerState>();
 	private Set<String> brokersIds = new HashSet<String>();
 	private Map<Long, PeerRequest> requests = new LinkedHashMap<Long, PeerRequest>();
 	
 	private Map<String, Allocation> allocations = new HashMap<String, Allocation>();
 	private Map<String, Double> balances = new HashMap<String, Double>();
 	
 	private String dsId;
	private Set<String> providers;
 	
 	private Map<Long, List<ReplicaAccounting>> replicaAccountings = new HashMap<Long, List<ReplicaAccounting>>();
 	
 	public PeerRequest getRequest(long requestId) {
 		return requests.get(requestId);
 	}
 	
 	public void addRequest(PeerRequest request) {
 		requests.put(request.getSpec().getId(), request);
 	}
 	
 	public PeerRequest removeRequest(long requestId) {
 		return requests.remove(requestId);
 	}
 	
 	public void addWorker(String workerId) {
 		workersStates.put(workerId, WorkerState.UNAVAILABLE);
 	}
 	
 	public Set<String> getWorkersIds() {
 		return workersStates.keySet();
 	}
 	
 	public void addBroker(String brokerId) {
 		brokersIds.add(brokerId);
 	}
 	
 	public void setDiscoveryServiceId(String dsId) {
 		this.dsId = dsId;
 	}
 	
 	public String getDiscoveryServiceId() {
 		return dsId;
 	}
 
 	public void setWorkerState(String workerId, WorkerState state) {
 		workersStates.put(workerId, state);
 	}
 
 	public WorkerState getWorkerState(String workerId) {
 		return workersStates.get(workerId);
 	}
 
 	public void addAllocation(Allocation allocation) {
 		allocations.put(allocation.getWorker(), allocation);
 	}
 
 	public Allocation removeAllocation(String workerId) {
 		return allocations.remove(workerId);
 	}
 
 	public List<Allocation> getAllocations() {
 		return new LinkedList<Allocation>(allocations.values());
 	}
 
 	public Map<String, Double> getBalances() {
 		return new HashMap<String, Double>(balances);
 	}
 
 	/**
 	 * @return Requests in insertion order 
 	 */
 	public List<PeerRequest> getRequests() {
 		return new LinkedList<PeerRequest>(requests.values());
 	}
 
 	public Allocation getAllocation(String workerId) {
 		return allocations.get(workerId);
 	}
 
 	public void setWorkerProviders(Set<String> providers) {
 		this.providers = providers;
 	}
 	
 	public Set<String> getWorkerProviders() {
 		return providers;
 	}
 
 	public void addReplicaAccounting(ReplicaAccounting replicaAccounting) {
 		List<ReplicaAccounting> accountings = replicaAccountings.get(
 				replicaAccounting.getRequestId());
 		
 		if (accountings == null) {
 			accountings = new LinkedList<ReplicaAccounting>();
 			replicaAccountings.put(replicaAccounting.getRequestId(), accountings);
 		}
 		
 		accountings.add(replicaAccounting);
 	}
 	
 	public List<ReplicaAccounting> getReplicaAccountings(long requestId) {
 		return replicaAccountings.containsKey(requestId) ? 
 				new LinkedList<ReplicaAccounting>(replicaAccountings.get(requestId)) 
 				: new LinkedList<ReplicaAccounting>();
 	}
 
 	public void setBalance(String remotePeer, double balance) {
 		balances.put(remotePeer, balance);
 	}
 
 	public void removeReplicaAccountings(long requestId) {
 		replicaAccountings.remove(requestId);
 	}
 }
