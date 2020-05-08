 package it.polimi.elet.selflet.nodeState;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import it.polimi.elet.selflet.configuration.DispatcherConfiguration;
 import it.polimi.elet.selflet.exceptions.NotFoundException;
 import it.polimi.elet.selflet.id.ISelfLetID;
 import it.polimi.elet.selflet.istantiator.IVirtualMachineIPManager;
 import it.polimi.elet.selflet.istantiator.VirtualMachineIPManager;
 import it.polimi.elet.selflet.message.ISelfletNeighbors;
 import it.polimi.elet.selflet.message.SelfletNeighbors;
 import it.polimi.elet.selflet.negotiation.nodeState.INodeState;
 import it.polimi.elet.selflet.utilities.CollectionUtils;
 
 /**
  * An implementation of node state manager
  * 
  * @author Nicola Calcavecchia <calcavecchia@gmail.com>
  * */
 public class NodeStateManager implements INodeStateManager {
 
 	private static final int MAX_NEIGHBORS_PER_SELFLET = DispatcherConfiguration.maxNeighborsPerSelflet;
 	private static final long THRESHOLD = DispatcherConfiguration.stateMaximumAgeInSec * 1000;
 
 	private static final NodeStateManager instance = new NodeStateManager();
 	private static final ISelfletNeighbors selfletNeighbors = SelfletNeighbors
 			.getInstance();
 	private final IVirtualMachineIPManager virtualMachineIPManager = VirtualMachineIPManager
 			.getInstance();
 
 	private final Map<ISelfLetID, INodeState> nodeStates = Maps
 			.newConcurrentMap();
 
 	private NodeStateManager() {
 		/* Private constructor */
 	}
 
 	public static INodeStateManager getInstance() {
 		return instance;
 	}
 
 	public void addState(INodeState nodeState) {
 		nodeStates.put(nodeState.getSelfletID(), nodeState);
 	}
 
 	public INodeState getNodeState(ISelfLetID selfletID) {
 		return nodeStates.get(selfletID);
 	}
 
 	public void cleanOldStates() {
 
 		for (Entry<ISelfLetID, INodeState> entry : nodeStates.entrySet()) {
 			if (isOldNodeState(entry.getValue())) {
 				nodeStates.remove(entry.getKey());
 				selfletNeighbors.removeNeighbor(entry.getKey());
 				virtualMachineIPManager.freeIPOfSelflet(entry.getKey());
 			}
 		}
 	}
 
 	private boolean isOldNodeState(INodeState value) {
 		Date now = new Date();
 		long delta = now.getTime() - value.getTimeStamp().getTime();
 		return delta > THRESHOLD;
 	}
 
 	@Override
 	public boolean haveStateOfSelflet(ISelfLetID selflet) {
 		return nodeStates.containsKey(selflet);
 	}
 
 	@Override
 	public List<INodeState> getStates() {
 		return Lists.newArrayList(nodeStates.values());
 	}
 
 	@Override
 	public void removeNodeStateOfNeighbor(ISelfLetID selflet) {
 		selfletNeighbors.removeNeighbor(selflet);
 		nodeStates.remove(selflet);
 	}
 
 	@Override
 	public ISelfLetID getRandomSelfletHavingService(String serviceName) {
 		List<ISelfLetID> selfletIDs = getSelfletsWithService(serviceName);
 		if (selfletIDs.isEmpty()) {
 			throw new NotFoundException(
 					"Cannot find a selflet offering service " + serviceName);
 		}
 		return CollectionUtils.randomElement(selfletIDs);
 	}
 
 	private List<ISelfLetID> getSelfletsWithService(String serviceName) {
 		List<ISelfLetID> selfletIDs = Lists.newArrayList();
 		for (INodeState nodeState : nodeStates.values()) {
 			if (nodeState.getAvailableServices().contains(serviceName)) {
 				selfletIDs.add(nodeState.getSelfletID());
 			}
 		}
 		return selfletIDs;
 	}
 
 	@Override
 	public boolean isNeighborhoodFull(ISelfLetID selflet) {
 		if (haveStateOfSelflet(selflet)) {
 			INodeState nodestate = getNodeState(selflet);
 			boolean isFull = true;
 			isFull = isFull
 					& nodestate.getKnownNeighbors().size() >= MAX_NEIGHBORS_PER_SELFLET;
 			for (ISelfLetID neighbor : nodestate.getKnownNeighbors()) {
				if (haveStateOfSelflet(neighbor)) {
					INodeState nodestateOfNeighbor = getNodeState(neighbor);
					isFull = isFull
							& nodestateOfNeighbor.getKnownNeighbors().size() >= MAX_NEIGHBORS_PER_SELFLET;
				} else {
					return false;
				}
 			}
 			return isFull;
 		}
 		return false;
 	}
 }
