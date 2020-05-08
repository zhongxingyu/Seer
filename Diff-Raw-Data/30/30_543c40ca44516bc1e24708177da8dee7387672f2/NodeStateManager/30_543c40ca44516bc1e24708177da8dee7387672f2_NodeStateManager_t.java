 package it.polimi.elet.selflet.nodeState;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
import java.util.Set;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import it.polimi.elet.selflet.configuration.DispatcherConfiguration;
 import it.polimi.elet.selflet.exceptions.NotFoundException;
 import it.polimi.elet.selflet.id.ISelfLetID;
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
 
 	private static final long THRESHOLD = DispatcherConfiguration.stateMaximumAgeInSec * 1000;
 
 	private static final NodeStateManager instance = new NodeStateManager();
	private static final ISelfletNeighbors selfletNeighbors = SelfletNeighbors
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
 
	// FIXME trying to manage "ghost selflets". It's not the best solution, but
	// can help finding the problem...
 	public void cleanOldStates() {
		Set<Entry<ISelfLetID, INodeState>> oldsSet = nodeStates.entrySet();

 		for (Entry<ISelfLetID, INodeState> entry : nodeStates.entrySet()) {
 			if (isOldNodeState(entry.getValue())) {
 				nodeStates.remove(entry.getKey());
 				selfletNeighbors.removeNeighbor(entry.getKey());
 			}
 		}

		// FIXME Double check, trying to solve the bug of "ghost selflets" in
		// dispatcher neighbors
		for (Entry<ISelfLetID, INodeState> oldEntry : oldsSet) {

			if (!nodeStates.containsKey(oldEntry.getKey())
					&& selfletNeighbors.getNeighbors().contains(
							oldEntry.getKey()))
				selfletNeighbors.removeNeighbor(oldEntry.getKey());
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
 }
