 package com.robonobo.mina.instance;
 
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.logging.Log;
 
import com.google.protobuf.GeneratedMessage;
 import com.robonobo.common.concurrent.Attempt;
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.core.api.proto.CoreApi.EndPoint;
 import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.external.StreamingDetails;
import com.robonobo.mina.external.StreamingNode;
import com.robonobo.mina.external.buffer.PageBuffer;
 import com.robonobo.mina.message.proto.MinaProtocol.SourceStatus;
 import com.robonobo.mina.network.*;
import com.robonobo.mina.util.MinaConnectionException;
 
 /**
  * @syncpriority 180
  */
 public class StreamConnsMgr {
 	private final Log log;
 	private final MinaInstance mina;
 	/** Map<sid, Set<nid>> */
 	private final Map<String, Set<String>> pendingCons;
 	private final Map<String, Set<LCPair>> lcPairs;
 	private final Map<String, Set<BCPair>> bcPairs;
 
 	StreamConnsMgr(MinaInstance mina) {
 		this.mina = mina;
 		log = mina.getLogger(getClass());
 		pendingCons = new HashMap<String, Set<String>>();
 		lcPairs = new HashMap<String, Set<LCPair>>();
 		bcPairs = new HashMap<String, Set<BCPair>>();
 	}
 
 	public synchronized void abort() {
 		List<ConnectionPair> cpList = new ArrayList<ConnectionPair>();
 		for (String sid : lcPairs.keySet()) {
 			cpList.addAll(lcPairs.get(sid));
 		}
 		for(String sid : bcPairs.keySet()) {
 			cpList.addAll(bcPairs.get(sid));
 		}
 		for (ConnectionPair cp : cpList) {
 			cp.abort();
 		}
 		bcPairs.clear();
 		lcPairs.clear();
 		pendingCons.clear();
 	}
 
 	/**
 	 * @syncpriority 180
 	 */
 	public void closeAllStreamConns() {
 		log.debug("Closing down all stream conns");
 		Set<BCPair> bcpSet = new HashSet<BCPair>();
 		Set<LCPair> lcpSet = new HashSet<LCPair>();
 		synchronized (this) {
 			for (String sid : bcPairs.keySet()) {
 				for (BCPair bcp : bcPairs.get(sid)) {
 					bcpSet.add(bcp);
 				}
 			}
 			for (String sid : lcPairs.keySet()) {
 				for (LCPair lcp : lcPairs.get(sid)) {
 					lcpSet.add(lcp);
 				}
 			}
 		}
 		for (BCPair bcp : bcpSet) {
 			bcp.die();
 		}
 		for (LCPair lcp : lcpSet) {
 			lcp.die();
 		}
 	}
 
 	/**
 	 * Close everyone we are broadcasting to
 	 * 
 	 * @syncpriority 180
 	 */
 	public void closeAllBroadcastConns(String sid) {
 		for (BCPair bcp : getBroadcastConns(sid)) {
 			bcp.die();
 		}
 		synchronized (this) {
 			if(bcPairs.containsKey(sid))
 				bcPairs.get(sid).clear();
 		}
 	}
 
 	/**
 	 * Close everyone we are listening to
 	 * 
 	 * @syncpriority 180
 	 */
 	public void closeAllListenConns(String sid) {
 		for (LCPair lcp : getListenConns(sid)) {
 			lcp.die();
 		}
 		synchronized (this) {
 			if(lcPairs.containsKey(sid))
 				lcPairs.get(sid).clear();
 		}
 	}
 
 	/**
 	 * Copied out, so safe to iterate over
 	 * 
 	 * @syncpriority 180
 	 */
 	public synchronized LCPair[] getListenConns(String sid) {
 		Set<LCPair> lcpSet = lcPairs.get(sid);
 		if(lcpSet == null)
 			return new LCPair[0];
 		LCPair[] lcpArr = new LCPair[lcpSet.size()];
 		lcpSet.toArray(lcpArr);
 		return lcpArr;
 	}
 
 	/**
 	 * Copied out, so safe to iterate over
 	 * 
 	 * @syncpriority 180
 	 */
 	public synchronized BCPair[] getBroadcastConns(String sid) {
 		Set<BCPair> bcpSet = bcPairs.get(sid);
 		if(bcpSet == null)
 			return new BCPair[0];
 		BCPair[] bcpArr = new BCPair[bcpSet.size()];
 		bcpSet.toArray(bcpArr);
 		return bcpArr;
 	}
 
 	/**
 	 * The number of people we are broadcasting to
 	 * 
 	 * @syncpriority 180
 	 */
 	public synchronized int getNumBroadcastConns(String sid) {
 		Set<BCPair> bcpSet = bcPairs.get(sid);
 		if(bcpSet == null)
 			return 0;
 		return bcpSet.size();
 	}
 
 	/**
 	 * The number of people we are listening to
 	 * 
 	 * @syncpriority 180
 	 */
 	public synchronized int getNumListenConns(String sid) {
 		Set<LCPair> lcpSet = lcPairs.get(sid);
 		if(lcpSet == null)
 			return 0;
 		return lcpSet.size();
 	}
 
 	/**
 	 * @syncpriority 180
 	 */
 	public synchronized int getNumPendingCons() {
 		return pendingCons.size();
 	}
 
 //	public StreamingDetails getStreamingDetails(String sid) {
 //		StreamingNode[] bNodes = getBroadcastStreamingNodes(sid);
 //		StreamingNode[] lNodes = getListenStreamingNodes(sid);
 //		StreamingDetails sd = new StreamingDetails(sid);
 //		sd.setReceivingFromNodes(lNodes);
 //		sd.setSendingToNodes(bNodes);
 //		PageBuffer pb = mina.getPageBufProvider().getPageBuf(sid);
 //		if(pb != null)
 //			sd.setBytesDownloaded(pb.getBytesReceived());
 //		return sd;
 //	}
 
 	/**
 	 * Don't call this directly - call StreamMgr.broadcastTo() instead
 	 * @syncpriority 180
 	 */
 	public synchronized void makeBroadcastConnectionTo(String sid, ControlConnection cc, EndPoint listenEp, List<Long> pages) {
 		if (mina.getCCM().isShuttingDown()) {
 			log.debug("Not making broadcast connection to " + cc.getNodeId() + ": shutting down");
 			return;
 		}
 		if (cc.getBCPair(sid) != null) {
 			log.error("Error: not creating broadcasting connection to already-receiving node " + cc.getNodeId());
 			return;
 		}
 		if(!bcPairs.containsKey(sid))
 			bcPairs.put(sid, new HashSet<BCPair>());
 		bcPairs.get(sid).add(new BCPair(mina, sid, cc, listenEp, pages));
 	}
 
 	/**
 	 * @syncpriority 200
 	 */
 	public void makeListenConnectionTo(final String sid, final SourceStatus ss) {
 		Node node = ss.getFromNode();
 		final String nodeId = node.getId();
 		if (mina.getCCM().isShuttingDown()) {
 			log.debug("Not making listen connection to " + nodeId + ": shutting down");
 			return;
 		}
 
 		// If we have no currency client, wait til we do
 		if (mina.getConfig().isAgoric() && !mina.getCurrencyClient().isReady()) {
 			log.debug("Not making lcpair to " + nodeId + " for "+sid+" as currency client is not ready - waiting 5s");
 			mina.getExecutor().schedule(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					makeListenConnectionTo(sid, ss);
 				}
 			}, 5, TimeUnit.SECONDS);
 			return;
 		}
 
 		ControlConnection cc = mina.getCCM().getCCWithId(nodeId);
 		if (cc == null) {
 			synchronized (this) {
 				if(!pendingCons.containsKey(sid))
 					pendingCons.put(sid, new HashSet<String>());
 				if(pendingCons.get(sid).contains(nodeId)) {
 					log.debug("Not making listen conn to "+nodeId+" for stream "+sid+" - attempt already in progress");
 					return;
 				}
 				Attempt getCCAttempt = new GetCCAttempt(sid, ss);
 				pendingCons.get(sid).add(nodeId);
 				getCCAttempt.start();
 				log.debug("Making connection to " + nodeId + " for listening to " + sid);
 				mina.getCCM().makeCCTo(node, getCCAttempt);
 			}
 		} else {
 			if(cc.isClosing()) {
 				// We have a CC, but we're already shutting it down (probably cleaning up the end of a previous stream), so try again in a little while
 				log.debug("Not making lcpair to " + nodeId + " for "+sid+" as existing CC is closing - waiting 5s");
 				mina.getExecutor().schedule(new CatchingRunnable() {
 					public void doRun() throws Exception {
 						makeListenConnectionTo(sid, ss);
 					}
 				}, 5, TimeUnit.SECONDS);
 				return;
 			}
 			try {
 				startListeningTo(cc, sid, ss);
 			} catch (IOException e) {
 				log.error("Caught exception setting up lcp to "+cc.getNodeId()+" for "+sid, e);
 				mina.getSourceMgr().cachePossiblyDeadSource(cc.getNode(), sid);
 				// Request more if we need them
 				mina.getStreamMgr().requestCachedSources(sid);
 			}
 		}
 	}
 
 	/**
 	 * @throws IOException 
 	 * @syncpriority 200
 	 */
 	private void startListeningTo(final ControlConnection cc, String sid, SourceStatus ss) throws IOException {
 		String nodeId = cc.getNodeId();
 		if (mina.getCCM().isShuttingDown()) {
 			log.debug("Not making listen connection to " + nodeId + ": shutting down");
 			return;
 		}
 		if(cc.getLCPair(sid) != null) {
 			log.error("Error: Asked to make ListenConnection to already-listening node " + nodeId);
 			return;			
 		}
 		log.info("Starting listening to " + nodeId + " for stream " + sid);
 		synchronized (this) {
 			LCPair lcp = new LCPair(mina, sid, cc, ss);
 			if(!lcPairs.containsKey(sid))
 				lcPairs.put(sid, new HashSet<LCPair>());
 			lcPairs.get(sid).add(lcp);
 		}
 		mina.getEventMgr().fireReceptionConnsChanged(sid);
 	}
 
 	/**
 	 * Must only be called from inside sync block
 	 */
 	private void removePendingConn(String sid, String nid) {
 		Set<String> set = pendingCons.get(sid);
 		if(set == null)
 			return;
 		set.remove(nid);
 		if(set.size() == 0)
 			pendingCons.remove(sid);
 	}
 
 	/**
 	 * @syncpriority 180
 	 */
 	public synchronized void removeConnectionPair(ConnectionPair pair) {
 		String sid = pair.getStreamId();
 		String nid = pair.getCC().getNodeId();
 		if (pair instanceof LCPair) {
 			removePendingConn(sid, nid);
 			Set<LCPair> set = lcPairs.get(sid);
 			if(set != null) {
 				set.remove(pair);
 				if(set.size() == 0)
 					lcPairs.remove(sid);
 			}
 		} else if (pair instanceof BCPair) {
 			Set<BCPair> set = bcPairs.get(sid);
 			if(set != null) {
 				set.remove(pair);
 				if(set.size() == 0)
 					bcPairs.remove(sid);
 			}
 		}
 	}
 //
 //	/**
 //	 * @syncpriority 180
 //	 */
 //	public synchronized void sendToBroadcastConns(String msgName, GeneratedMessage msg, String except) {
 //		Iterator<BCPair> i = bcPairs.values().iterator();
 //		while (i.hasNext()) {
 //			BCPair bcPair = i.next();
 //			if (bcPair.getCC().getNodeId().equals(except))
 //				continue;
 //			bcPair.sendMessage(msgName, msg);
 //		}
 //	}
 //
 //	/**
 //	 * @syncpriority 180
 //	 */
 //	public synchronized void sendToListenConns(String msgName, GeneratedMessage msg) {
 //		sendToListenConns(msgName, msg, null);
 //	}
 //
 //	/**
 //	 * @syncpriority 180
 //	 */
 //	public synchronized void sendToListenConns(String msgName, GeneratedMessage msg, String except) {
 //		Iterator<LCPair> i = lcPairs.values().iterator();
 //		while (i.hasNext()) {
 //			ConnectionPair lcPair = i.next();
 //			if (lcPair.getCC().getNodeId().equals(except))
 //				continue;
 //			lcPair.sendMessage(msgName, msg);
 //		}
 //	}
 //
 //	/**
 //	 * @syncpriority 180
 //	 */
 //	public synchronized void sendToNonLocalBroadcastConns(String msgName, GeneratedMessage msg, String except) {
 //		Iterator<BCPair> i = bcPairs.values().iterator();
 //		while (i.hasNext()) {
 //			BCPair bcPair = i.next();
 //			if (bcPair.getCC().getNodeId().equals(except))
 //				continue;
 //			if (bcPair.getCC().isLocal())
 //				continue;
 //			bcPair.sendMessage(msgName, msg);
 //		}
 //	}
 //
 //	/**
 //	 * @syncpriority 180
 //	 */
 //	private synchronized StreamingNode[] getBroadcastStreamingNodes(String sid) {
 //		StreamingNode[] bNodes = new StreamingNode[bcPairs.size()];
 //		int i = 0;
 //		for (Iterator<BCPair> iter = bcPairs.values().iterator(); iter.hasNext();) {
 //			BCPair bcp = iter.next();
 //			bNodes[i++] = new StreamingNode(bcp.getCC().getNodeId().toString(), bcp.getFlowRate());
 //		}
 //		return bNodes;
 //	}
 //
 //	/**
 //	 * @syncpriority 180
 //	 */
 //	private synchronized StreamingNode[] getListenStreamingNodes(String sid) {
 //		int i;
 //		StreamingNode[] lNodes = new StreamingNode[lcPairs.size()];
 //		i = 0;
 //		for (Iterator<LCPair> iter = lcPairs.values().iterator(); iter.hasNext();) {
 //			LCPair lcp = iter.next();
 //			StreamingNode node = new StreamingNode(lcp.getCC().getNodeId().toString(), lcp.getFlowRate());
 //			node.setConnected(true);
 //			node.setComplete(lcp.isComplete());
 //			lNodes[i++] = node;
 //		}
 //		return lNodes;
 //	}
 
 	private class GetCCAttempt extends Attempt {
 		private SourceStatus sourceStat;
 		private String sid;
 		private String nid;
 
 		public GetCCAttempt(String sid, SourceStatus ss) {
 			// This should never timeout, it should be handled by CCMgr, but just in case...
 			super(mina.getExecutor(), 2 * mina.getConfig().getConnectTimeout() * 1000, "GetCCAttempt");
 			this.sourceStat = ss;
 			this.sid = sid;
 			this.nid = ss.getFromNode().getId();
 		}
 
 		/**
 		 * @syncpriority 200
 		 */
 		protected void onFail() {
 			log.info("Failed to connect to " + nid + " for stream '" + sid + "'");
 			synchronized (StreamConnsMgr.this) {
 				removePendingConn(sid, nid);
 			}
 			mina.getSourceMgr().cachePossiblyDeadSource(sourceStat.getFromNode(), sid);
 			// Request more if we need them
 			mina.getStreamMgr().requestCachedSources(sid);
 		}
 
 		/**
 		 * @syncpriority 200
 		 */
 		protected void onSuccess() {
 			synchronized (StreamConnsMgr.this) {
 				removePendingConn(sid, nid);
 			}
 			ControlConnection cc = mina.getCCM().getCCWithId(nid);
 			if (cc == null) {
 				// Oops, CC has disappeared due to an ill-timed network snafu
 				onFail();
 				return;
 			}
 			log.info("Successfully got CC " + cc.getNodeId() + " for listening to stream " + sid);
 			try {
 				startListeningTo(cc, sid, sourceStat);
 			} catch (IOException e) {
 				// Oops, a network errot
 				log.error("Caught exception starting listening to "+cc.getNodeId()+" for "+sid, e);
 				onFail();
 			}
 		}
 
 		/**
 		 * @syncpriority 180
 		 */
 		protected void onTimeout() {
 			log.error("SCM.GetCCAttempt to "+nid+" for "+sid+" timed out!");
 			onFail();
 		}
 	}
 }
