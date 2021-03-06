 package org.dcache.services.info.gathers;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.dcache.services.info.base.FloatingPointStateValue;
 import org.dcache.services.info.base.IntegerStateValue;
 import org.dcache.services.info.base.StatePath;
 import org.dcache.services.info.base.StateUpdate;
 import org.dcache.services.info.base.StateUpdateManager;
 import org.dcache.services.info.base.StringStateValue;
 import org.dcache.services.info.stateInfo.SpaceInfo;
 
 import diskCacheV111.pools.PoolCostInfo;
 import diskCacheV111.pools.PoolCostInfo.NamedPoolQueueInfo;
 import diskCacheV111.pools.PoolCostInfo.PoolQueueInfo;
 import diskCacheV111.pools.PoolCostInfo.PoolSpaceInfo;
 import diskCacheV111.vehicles.CostModulePoolInfoTable;
 import dmg.cells.nucleus.UOID;
 
 /**
  * This class processing incoming CellMessages that contain CostModulePoolInfoTable
  *
  * @author Paul Millar <paul.millar@desy.de>
  */
 public class PoolCostMsgHandler extends CellMessageHandlerSkel {
 
 	private static Logger _log = LoggerFactory.getLogger( PoolCostMsgHandler.class);
 
 	public PoolCostMsgHandler(StateUpdateManager sum, MessageMetadataRepository<UOID> msgMetaRepo) {
 		super(sum, msgMetaRepo);
 	}
 
 	@Override
     public void process(Object msgPayload, long msgDeliveryPeriod) {
 
 		long metricLifetime = (long) (msgDeliveryPeriod * 2.5); // Give metrics a lifetime of 2.5* message deliver period
 
 		if( !(msgPayload instanceof CostModulePoolInfoTable)) {
 			_log.error( "received non-CostModulePoolInfoTable object in message\n");
 			return;
 		}
 
 		CostModulePoolInfoTable poolInfoTbl = (CostModulePoolInfoTable) msgPayload;
 
 		Collection<PoolCostInfo> poolInfos = poolInfoTbl.poolInfos();
 		StatePath poolsPath = new StatePath("pools");
 
 		StateUpdate update = new StateUpdate();
 
 		for( PoolCostInfo thisPoolInfo : poolInfos) {
 
 			String poolName = thisPoolInfo.getPoolName();
 
 			StatePath pathToThisPool = poolsPath.newChild(poolName);
 			StatePath pathToQueues = pathToThisPool.newChild("queues");
 
 
 			/**
 			 *  Add all the standard queues
 			 */
 
 			addQueueInfo( update, pathToQueues, "store", thisPoolInfo.getStoreQueue(), metricLifetime);
 			addQueueInfo( update, pathToQueues, "restore", thisPoolInfo.getRestoreQueue(), metricLifetime);
 			addQueueInfo( update, pathToQueues, "mover", thisPoolInfo.getMoverQueue(), metricLifetime);
 			addQueueInfo( update, pathToQueues, "p2p-queue", thisPoolInfo.getP2pQueue(), metricLifetime);
 			addQueueInfo( update, pathToQueues, "p2p-clientqueue", thisPoolInfo.getP2pClientQueue(), metricLifetime);
 
 
 			/**
 			 *  Add the "extra" named queues
 			 */
 
 			addNamedQueues( update, pathToQueues, thisPoolInfo, metricLifetime);
 
 
 			/**
 			 *  Add information about our default queue's name, if we have one.
 			 */
 
 			String defaultQueue = thisPoolInfo.getDefaultQueueName();
 			if( defaultQueue != null)
 				update.appendUpdate(pathToQueues.newChild("default-queue"),
 						new StringStateValue( defaultQueue, metricLifetime));
 
 
 			/**
 			 *  Add information about this pool's space utilisation.
 			 */
 
 			addSpaceInfo( update, pathToThisPool.newChild("space"), thisPoolInfo.getSpaceInfo(), metricLifetime);
 		}
 
		applyUpdates( update);
 	}
 
 
 
 	/**
 	 * Add information about a specific queue to a pool's portion of dCache state.
 	 * The state tree looks like:
 	 *
 	 * <pre>
 	 * [dCache]
 	 *  |
 	 *  +--[pools]
 	 *  |   |
 	 *  |   +--[&lt;poolName>]
 	 *  |   |   |
 	 *  |   |   +--[queues]
 	 *  |   |   |   |
 	 *  |   |   |   +--[&lt;queueName1>]
 	 *  |   |   |   |    |
 	 *  |   |   |   |    +--active: nnn
 	 *  |   |   |   |    +--max-active: nnn
 	 *  |   |   |   |    +--queued: nnn
 	 *  |   |   |   |
 	 *  |   |   |   +--[&lt;queueName2>]
 	 * </pre>
 	 *
 	 * @param pathToQueues the StatePath pointing to queues (e.g.,
 	 * "pools.mypool_1.queues")
 	 * @param queueName the name of the queue.
 	 */
 	private void addQueueInfo( StateUpdate stateUpdate, StatePath pathToQueues,
 								String queueName, PoolQueueInfo info, long lifetime) {
 		StatePath queuePath = pathToQueues.newChild(queueName);
 
 		stateUpdate.appendUpdate(queuePath.newChild("active"),
 					new IntegerStateValue(info.getActive(), lifetime));
 		stateUpdate.appendUpdate(queuePath.newChild("max-active"),
 				new IntegerStateValue(info.getMaxActive(), lifetime));
 		stateUpdate.appendUpdate(queuePath.newChild("queued"),
 				new IntegerStateValue(info.getQueued(), lifetime));
 	}
 
 
 	/**
 	 * Adds information from a pool's PoolSpaceInfo object.
 	 * We add this into the state in the following way:
 	 *
 	 * <pre>
 	 * [dCache]
 	 *  |
 	 *  +--[pools]
 	 *  |   |
 	 *  |   +--[&lt;poolName>]
 	 *  |   |   |
 	 *  |   |   +--[space]
 	 *  |   |   |   |
 	 *  |   |   |   +--total: nnn
 	 *  |   |   |   +--free: nnn
 	 *  |   |   |   +--precious: nnn
 	 *  |   |   |   +--removable: nnn
 	 *  |   |   |   +--pinned: nnn
 	 *  |   |   |   +--used: nnn
 	 *  |   |   |   +--gap: nnn
 	 *  |   |   |   +--break-even: nnn
 	 *  |   |   |   +--LRU-seconds: nnn
 	 * </pre>
 	 *
 	 * @param stateUpdate the StateUpdate we will append
 	 * @param path the StatePath pointing to the space branch
 	 * @param info the space information to include.
 	 */
 	private void addSpaceInfo( StateUpdate stateUpdate, StatePath pathToSpace,
 							PoolSpaceInfo info, long lifetime) {
 		SpaceInfo si = new SpaceInfo( info);
 
 		si.addMetrics(stateUpdate, pathToSpace, lifetime);
 
 		stateUpdate.appendUpdate( pathToSpace.newChild("gap"),
 				new IntegerStateValue( info.getGap(), lifetime));
 		stateUpdate.appendUpdate( pathToSpace.newChild("break-even"),
 				new FloatingPointStateValue( info.getBreakEven(), lifetime));
 		stateUpdate.appendUpdate( pathToSpace.newChild("LRU-seconds"),
 				new IntegerStateValue( info.getLRUSeconds(), lifetime));
 	}
 
 
 	/**
 	 * Add information about all "named" queues.  The available information is the
 	 * same as with regular queues, but there are arbirary number of these. The
 	 * information is presented underneath the named-queues branch of the queues
 	 * branch:
 	 *
 	 * <pre>
 	 * [dCache]
 	 *  |
 	 *  +--[pools]
 	 *  |   |
 	 *  |   +--[&lt;poolName>]
 	 *  |   |   |
 	 *  |   |   +--[queues]
 	 *  |   |   |   |
 	 *  |   |   |   +--[named-queues]
 	 *  |   |   |   |   |
 	 *  |   |   |   |   +--[&lt;namedQueue1>]
 	 *  |   |   |   |   |    |
 	 *  |   |   |   |   |    +--active: nnn
 	 *  |   |   |   |   |    +--max-active: nnn
 	 *  </pre>
 	 *
 	 * @param update the StateUpdate we are appending to
 	 * @param pathToQueues the StatePath pointing to [queues] above
 	 * @param thisPoolInfo the information about this pool.
 	 */
 	private void addNamedQueues( StateUpdate update, StatePath pathToQueues, PoolCostInfo thisPoolInfo, long lifetime)	{
 		Map<String, NamedPoolQueueInfo> namedQueuesInfo = thisPoolInfo.getExtendedMoverHash();
 
 		if( namedQueuesInfo == null)
 			return;
 
 		StatePath pathToNamedQueues = pathToQueues.newChild("named-queues");
 
 		for( Iterator<NamedPoolQueueInfo> namedQueueItr = namedQueuesInfo.values().iterator();
 					namedQueueItr.hasNext();) {
 			NamedPoolQueueInfo thisNamedQueueInfo = namedQueueItr.next();
 			addQueueInfo( update, pathToNamedQueues, thisNamedQueueInfo.getName(), thisNamedQueueInfo, lifetime);
 		}
 	}
 }
