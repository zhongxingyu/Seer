 package allocation;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.drools.runtime.ObjectFilter;
 import org.drools.runtime.StatefulKnowledgeSession;
 
 import allocation.facts.CommonPool;
 
 import com.google.inject.Inject;
 
 import uk.ac.imperial.presage2.core.environment.EnvironmentService;
 import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
 
 public class PoolService extends EnvironmentService {
 
 	final StatefulKnowledgeSession session;
 	Map<Integer, CommonPool> pools = new HashMap<Integer, CommonPool>();
 
 	@Inject
 	public PoolService(EnvironmentSharedStateAccess sharedState,
 			StatefulKnowledgeSession session) {
 		super(sharedState);
 		this.session = session;
 	}
 
 	private void loadPools() {
 		if (pools.isEmpty()) {
 			Collection<Object> ps = session.getObjects(new ObjectFilter() {
 
 				@Override
 				public boolean accept(Object object) {
 					return object instanceof CommonPool;
 				}
 			});
 			for (Object o : ps) {
 				if (o instanceof CommonPool) {
 					CommonPool p = (CommonPool) o;
 					pools.put(p.getId(), p);
 				}
 			}
 		}
 	}
 
 	public Phase getPoolState(int poolId) {
 		loadPools();
		try {
			return pools.get(poolId).getState();
		} catch (NullPointerException e) {
			return null;
		}
	}

	public int getRoundNumber(int poolId) {
		loadPools();
		try {
			return pools.get(poolId).getRound();
		} catch (NullPointerException e) {
			return 0;
		}
 	}
 
 }
