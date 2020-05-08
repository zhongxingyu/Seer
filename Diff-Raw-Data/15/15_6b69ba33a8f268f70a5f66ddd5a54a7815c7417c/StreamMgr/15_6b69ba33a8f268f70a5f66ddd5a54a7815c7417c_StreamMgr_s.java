 package com.robonobo.mina.instance;
 
 import java.io.IOException;
 import java.util.*;
 
 import org.apache.commons.logging.Log;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.common.exceptions.SeekInnerCalmException;
 import com.robonobo.core.api.proto.CoreApi.EndPoint;
 import com.robonobo.core.api.proto.CoreApi.Node;
 import com.robonobo.mina.agoric.AuctionState;
 import com.robonobo.mina.external.FoundSourceListener;
 import com.robonobo.mina.external.buffer.*;
 import com.robonobo.mina.message.proto.MinaProtocol.SourceStatus;
 import com.robonobo.mina.message.proto.MinaProtocol.StreamStatus;
 import com.robonobo.mina.message.proto.MinaProtocol.UnAdvSource;
 import com.robonobo.mina.network.*;
 
 /**
  * Main class for dealing with stream broadcast and reception.
  * 
  * @author macavity
  * @syncpriority 200
  */
 public class StreamMgr {
 	protected Log log;
 	protected MinaInstance mina;
 	protected Set<String> broadcastingSids = new HashSet<String>();
 	protected Set<String> receivingSids = new HashSet<String>();
 	protected Set<String> rebroadcastingSids = new HashSet<String>();
 	protected Map<String, Set<FoundSourceListener>> listeners = new HashMap<String, Set<FoundSourceListener>>();
 	protected Map<String, Integer> streamPriorities = new HashMap<String, Integer>();
 	/**
 	 * We keep page buffers for 'live' streams - those that either we are receiving, or that we are actively
 	 * broadcasting to one or more listeners
 	 */
 	protected Map<String, PageBuffer> liveStreams = new HashMap<String, PageBuffer>();
 
 	public StreamMgr(MinaInstance mina) {
 		this.mina = mina;
 		log = mina.getLogger(getClass());
 	}
 
 	/**
 	 * Checks to see if we still want sources, and if we don't, notifies the network
 	 * 
 	 * @syncpriority 200
 	 */
 	private boolean checkWantingSources(String sid) {
 		// Don't do anything if we are shutting down, when we disconnect the
 		// supernode will figure it out
 		if (mina.getCCM().isShuttingDown())
 			return false;
 		boolean wantingSources = false;
 		synchronized (this) {
 			if (receivingSids.contains(sid))
 				wantingSources = true;
 			else if (listeners.containsKey(sid) && listeners.get(sid).size() > 0)
 				wantingSources = true;
 		}
 		if (!wantingSources)
 			mina.getSourceMgr().dontWantSources(sid);
 		return wantingSources;
 	}
 
 	/**
 	 * Makes this stream live - must be called only inside sync block
 	 */
 	private void wakePageBuf(String sid) {
 		if (!liveStreams.containsKey(sid))
 			liveStreams.put(sid, mina.getPageBufProvider().getPageBuf(sid));
 	}
 
 	/**
 	 * Must be called only inside sync block
 	 */
 	private void sleepPageBuf(String sid) {
 		PageBuffer pageBuf = liveStreams.remove(sid);
 		if (pageBuf != null) {
 			try {
 				pageBuf.sleep();
 			} catch (IOException e) {
 				log.error("Error sleeping pagebuf for stream " + sid, e);
 			}
 		}
 	}
 
 	/**
 	 * @return 'Live' streams are those that either we are receiving, or that we are actively broadcasting to one or
 	 *         more listeners
 	 * @syncpriority 200
 	 */
 	public synchronized String[] getLiveStreamIds() {
 		String[] result = new String[liveStreams.size()];
 		liveStreams.keySet().toArray(result);
 		return result;
 	}
 
 	/**
 	 * @return The stream ids that are receiving. Safe to iterate over.
 	 * @syncpriority 200
 	 */
 	public synchronized String[] getReceivingStreamIds() {
 		String[] result = new String[receivingSids.size()];
 		receivingSids.toArray(result);
 		return result;
 	}
 
 	/**
 	 * @return The stream ids that are [re]broadcasting. Safe to iterate over.
 	 * @syncpriority 200
 	 */
 	public synchronized List<String> getAdvertisingStreamIds() {
 		List<String> result = new ArrayList<String>();
 		result.addAll(broadcastingSids);
 		result.addAll(rebroadcastingSids);
 		return result;
 
 	}
 
 	/**
 	 * @syncpriority 200
 	 */
 	public synchronized void clearStreamPriorities() {
 		streamPriorities.clear();
 	}
 
 	/**
 	 * @syncpriority 200
 	 */
 	public synchronized void addFoundSourceListener(String sid, FoundSourceListener listener) {
 		boolean sendWantSources = false;
 		if (!listeners.containsKey(sid)) {
 			sendWantSources = true;
 			listeners.put(sid, new HashSet<FoundSourceListener>());
 		}
 		listeners.get(sid).add(listener);
 		if (sendWantSources)
 			mina.getSourceMgr().wantSources(sid);
 	}
 
 	/**
 	 * @syncpriority 200
 	 */
 	public synchronized void removeFoundSourceListener(String sid, FoundSourceListener listener) {
 		Set<FoundSourceListener> set = listeners.get(sid);
 		if (set != null) {
 			set.remove(listener);
 			if (set.size() == 0)
 				listeners.remove(sid);
 		}
 		checkWantingSources(sid);
 	}
 
 	/**
 	 * @syncpriority 180
 	 */
 	public Set<Node> getKnownSources(String sid) {
 		Set<Node> result = mina.getSourceMgr().getReadyNodes(sid);
 		for (ConnectionPair lcp : mina.getSCM().getListenConns(sid)) {
 			result.add(lcp.getCC().getNode());
 		}
 		return result;
 	}
 
 	public Set<String> getSourceNodeIds(String sid) {
 		Set<String> result = mina.getSourceMgr().getReadyNodeIds(sid);
 		for (ConnectionPair lcp : mina.getSCM().getListenConns(sid)) {
 			result.add(lcp.getCC().getNode().getId());
 		}
 		return result;
 	}
 
 	/**
 	 * @syncpriority 160
 	 */
 	public int numSources(String sid) {
 		int result = mina.getSourceMgr().numReadySources(sid);
 		result += mina.getSCM().getNumListenConns(sid);
 		return result;
 	}
 
 	public StreamStatus buildStreamStatus(String sid, String toNodeId) {
 		StreamStatus.Builder bldr = StreamStatus.newBuilder();
 		if (toNodeId != null) {
 			bldr.setToNodeId(toNodeId);
 			bldr.setFromNodeId(mina.getMyNodeId());
 		}
 		bldr.setStreamId(sid);
 		PageBuffer pageBuf;
 		synchronized (this) {
 			if (liveStreams.containsKey(sid))
 				pageBuf = liveStreams.get(sid);
 			else
 				pageBuf = mina.getPageBufProvider().getPageBuf(sid);
 		}
 		if (pageBuf.getTotalPages() > 0)
 			bldr.setTotalPages(pageBuf.getTotalPages());
 		StreamPosition sp = pageBuf.getStreamPosition();
 		bldr.setLastContiguousPage(sp.getLastContiguousPage());
 		if (sp.getPageMap() > 0)
 			bldr.setPageMap(sp.getPageMap());
 		return bldr.build();
 	}
 
 	/**
 	 * @syncpriority 200
 	 */
 	public void foundSource(SourceStatus sourceStat, StreamStatus streamStat) {
 		String fromNodeId = sourceStat.getFromNode().getId();
 		String sid = streamStat.getStreamId();
 		// If we're already listening to this guy, ignore it
 		ControlConnection cc = mina.getCCM().getCCWithId(fromNodeId);
 		if (cc != null && cc.getLCPair(sid) != null)
 			return;
 		synchronized (this) {
 			// If we're not receiving, then we're just caching sources to pass to an external listener - so cache it
 			if (!receivingSids.contains(sid)) {
 				mina.getSourceMgr().cacheSourceUntilReady(sourceStat, streamStat);
 				notifyListenersOfSource(sid, fromNodeId);
 				return;
 			}
 			// If we haven't learned the total number of pages for this stream, do it now
 			if (streamStat.getTotalPages() > 0) {
 				PageBuffer pageBuf = liveStreams.get(sid);
 				if (pageBuf == null) {
 					log.error("Found source for stream " + sid + ", but that stream is not live");
 					return;
 				}
 				if (pageBuf.getTotalPages() <= 0)
 					pageBuf.setTotalPages(streamStat.getTotalPages());
 			}
 		}
 		// Check agorics are acceptable
 		if (mina.getConfig().isAgoric()) {
 			AuctionState as = new AuctionState(sourceStat.getAuctionState());
 			if (!mina.getBidStrategy().worthConnectingTo(sid, as)) {
 				log.debug("Not connecting to node " + fromNodeId + " for " + sid + " - bid strategy says no");
 				mina.getSourceMgr().cacheSourceUntilAgoricsAcceptable(sourceStat.getFromNode(), sid);
 				return;
 			}
 			if (!mina.getBuyMgr().canListenTo(as, mina.getBidStrategy().getStreamVelocity(sid))) {
 				log.debug("Not connecting to node " + fromNodeId + " for " + sid
 						+ " - no available listener slots at my price level");
 				mina.getSourceMgr().cacheSourceUntilAgoricsAcceptable(sourceStat.getFromNode(), sid);
 				return;
 			}
 		}
 		// Check that they have data useful to us
 		StreamPosition sp = new StreamPosition(streamStat.getLastContiguousPage(), streamStat.getPageMap());
 		if (!mina.getPRM().isUsefulSource(sid, sp)) {
 			// Useless at the moment - cache them and ask again later
 			mina.getSourceMgr().cacheSourceUntilDataAvailable(sourceStat.getFromNode(), sid);
 			return;
 		}
 		// This is a useful source - let our listeners know
 		notifyListenersOfSource(sid, fromNodeId);
 		// If we're already listening to our max sources, cache this one
 		if (mina.getSCM().getNumListenConns(sid) >= mina.getConfig().getMaxSources()) {
 			mina.getSourceMgr().cacheSourceUntilReady(sourceStat, streamStat);
 			return;
 		}
 		// Let's do this thing
 		mina.getSCM().makeListenConnectionTo(sid, sourceStat);
 	}
 
 	/**
 	 * @syncpriority 200
 	 */
 	public synchronized void broadcastTo(String sid, ControlConnection cc, EndPoint listenEp, List<Long> pages) {
 		wakePageBuf(sid);
 		mina.getSCM().makeBroadcastConnectionTo(sid, cc, listenEp, pages);
 	}
 
 	private void notifyListenersOfSource(final String sid, final String sourceId) {
 		final FoundSourceListener[] lisArr;
 		synchronized (this) {
 			Set<FoundSourceListener> set = listeners.get(sid);
 			if (set == null)
 				return;
 			lisArr = new FoundSourceListener[set.size()];
 			set.toArray(lisArr);
 		}
 		mina.getExecutor().execute(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				for (FoundSourceListener listener : lisArr) {
 					listener.foundBroadcaster(sid, sourceId);
 				}
 			}
 		});
 	}
 
 	/**
 	 * @syncpriority 200
 	 */
 	public void receivePage(String sid, Page p) {
 		if (!isReceiving(sid))
 			return;
 		PageBuffer pageBuf;
 		synchronized (this) {
 			pageBuf = liveStreams.get(sid);
 			if (pageBuf == null) {
 				log.error("Received page " + p.getPageNumber() + " for stream " + sid
 						+ ", but that streams is not live");
 				return;
 			}
 		}
 		try {
 			pageBuf.putPage(p);
 		} catch (IOException e) {
 			log.error("Error putting page into buffer for stream " + sid, e);
 			stopReception(sid);
 			return;
 		}
 		mina.getPRM().notifyPageReceived(sid, p.getPageNumber());
 		updateStreamStatus(sid);
 		if (isReceiving(sid) && !isRebroadcasting(sid))
 			startRebroadcast(sid);
 		if (pageBuf.getLastContiguousPage() == (pageBuf.getTotalPages() - 1))
 			receptionCompleted(sid);
 	}
 
 	/**
 	 * @syncpriority 160
 	 */
 	private void updateStreamStatus(String sid) {
 		StreamStatus ss = buildStreamStatus(sid, null);
 		for (BCPair bcPair : mina.getSCM().getBroadcastConns(sid)) {
 			bcPair.getCC().sendMessage("StreamStatus", ss);
 		}
 	}
 
 	/**
 	 * @syncpriority 200
 	 */
 	public synchronized boolean isBroadcasting(String sid) {
 		return broadcastingSids.contains(sid);
 	}
 
 	/**
 	 * @syncpriority 200
 	 */
 	public synchronized boolean isRebroadcasting(String sid) {
 		return rebroadcastingSids.contains(sid);
 	}
 
 	/**
 	 * @syncpriority 200
 	 */
 	public boolean isReceiving(String sid) {
 		return receivingSids.contains(sid);
 	}
 
 	/**
 	 * @syncpriority 200
 	 */
 	public void notifyDeadConnection(ConnectionPair pair) {
 		ControlConnection cc = pair.getCC();
 		String nid = cc.getNodeId();
 		String sid = pair.getStreamId();
 		synchronized (this) {
 			mina.getSCM().removeConnectionPair(pair);
 			if (pair instanceof LCPair) {
 				mina.getPRM().notifyDeadConnection(sid, nid);
 				if (mina.getSCM().getNumListenConns(sid) < mina.getConfig().getMaxSources())
 					requestCachedSources(sid);
 				if (cc.getLCPairs().length == 0)
 					mina.getBidStrategy().cleanupNode(nid);
 				// Make note of them in case they come back
 				LCPair lcp = (LCPair) pair;
 				mina.getSourceMgr().cachePossiblyDeadSource(lcp.getCC().getNode(), sid);
 			} else {
 				if (mina.getSCM().getNumBroadcastConns(sid) == 0 && !receivingSids.contains(sid))
 					sleepPageBuf(sid);
 			}
 		}
 		if (isReceiving(sid))
 			mina.getEventMgr().fireReceptionConnsChanged(sid);
 	}
 
 	/**
 	 * @syncpriority 200
 	 */
 	public void requestCachedSources(String sid) {
 		if (!isReceiving(sid))
 			return;
 		Set<SourceStatus> sources = mina.getSourceMgr().getReadySources(sid);
 		for (SourceStatus sourceStat : sources) {
 			// This is a bit kludgy - get the streamstatus that applies to us
 			StreamStatus streamStat = null;
 			for (StreamStatus testSs : sourceStat.getSsList()) {
 				if (testSs.getStreamId().equals(sid)) {
 					streamStat = testSs;
 					break;
 				}
 			}
 			if (streamStat == null)
 				throw new SeekInnerCalmException();
 			foundSource(sourceStat, streamStat);
 		}
 	}
 
 	/**
 	 * @syncpriority 200
 	 */
 	public synchronized void startBroadcast(String sid) {
 		if (broadcastingSids.contains(sid))
 			throw new SeekInnerCalmException();
 		log.info("Starting broadcast for stream " + sid);
 		broadcastingSids.add(sid);
 		// If we're already rebroadcasting, this means that we've completed
 		// reception and are now a broadcaster - so we don't need to announce
 		// ourselves again
 		if (rebroadcastingSids.contains(sid))
 			rebroadcastingSids.remove(sid);
 		else
 			mina.getStreamAdvertiser().advertiseStream(sid);
 	}
 
 	/**
 	 * Bulk-add broadcasts - called at startup, we might have 10^4+ available broadcasting streams
 	 * 
 	 * @syncpriority 200
 	 */
 	public synchronized void startBroadcasts(Collection<String> sids) {
 		log.info("Starting broadcast for " + sids.size() + " streams");
 		// If we're adding a lot of broadcasts, replace the hashset with a correctly-sized version, otherwise as we add
 		// it will be repeatedly resized, which is expensive
 		if (sids.size() > broadcastingSids.size()) {
 			HashSet<String> newSet = new HashSet<String>(sids.size() + broadcastingSids.size());
 			newSet.addAll(broadcastingSids);
 			newSet.addAll(sids);
 			broadcastingSids = newSet;
 		} else
 			broadcastingSids.addAll(sids);
 		mina.getStreamAdvertiser().advertiseStreams(sids);
 	}
 
 	/**
 	 * @syncpriority 200
 	 */
 	public synchronized void startRebroadcast(String sid) {
 		if (rebroadcastingSids.contains(sid))
 			throw new SeekInnerCalmException();
 		rebroadcastingSids.add(sid);
 		log.info("Beginning rebroadcast for stream " + sid);
 		mina.getStreamAdvertiser().advertiseStream(sid);
 	}
 
 	/**
 	 * @syncpriority 200
 	 */
 	public synchronized void startReception(String sid) {
 		if (receivingSids.contains(sid))
 			throw new SeekInnerCalmException();
 		wakePageBuf(sid);
 		// If we're already finished, just start rebroadcasting
 		PageBuffer pageBuf = liveStreams.get(sid);
 		if (pageBuf.isComplete())
 			startRebroadcast(sid);
 		else {
 			receivingSids.add(sid);
 			requestCachedSources(sid);
 			mina.getSourceMgr().wantSources(sid);
 		}
 	}
 
 	/**
 	 * Assumes all streaming conns have been shut down already, and that page buffers will be safely persisted after
 	 * mina is closed
 	 * 
 	 * @syncpriority 200
 	 */
 	public synchronized void stop() {
 		log.debug("StreamMgr stopping");
 		liveStreams.clear();
 		receivingSids.clear();
 		rebroadcastingSids.clear();
 		broadcastingSids.clear();
 		streamPriorities.clear();
 		listeners.clear();
 	}
 
 	/**
 	 * @syncpriority 200
 	 */
 	public synchronized void stopBroadcast(String sid) {
 		if (!broadcastingSids.contains(sid))
 			throw new SeekInnerCalmException();
 		broadcastingSids.remove(sid);
 		log.info("Stopping broadcast for stream " + sid);
 		deadvertiseStream(sid);
 		mina.getSCM().closeAllBroadcastConns(sid);
 	}
 
 	/**
 	 * @syncpriority 200
 	 */
 	public synchronized void stopRebroadcast(String sid) {
 		if (!rebroadcastingSids.contains(sid))
 			throw new SeekInnerCalmException();
 		rebroadcastingSids.remove(sid);
 		// If we are broadcasting, it means we completed our reception and
 		// became a broadcaster - so keep our broadcast conns alive, and don't
 		// de-advertise ourselves
 		if (!broadcastingSids.contains(sid)) {
 			mina.getSCM().closeAllBroadcastConns(sid);
 			deadvertiseStream(sid);
 		}
 	}
 
 	private void deadvertiseStream(String sid) {
 		// Don't deadvertise if we're shutting down
 		if (!mina.getCCM().isShuttingDown()) {
 			List<String> streamIds = new ArrayList<String>();
 			streamIds.add(sid);
 			UnAdvSource uas = UnAdvSource.newBuilder().addAllStreamId(streamIds).build();
 			mina.getCCM().sendMessageToNetwork("UnAdvSource", uas);
 		}
 	}
 
 	/**
 	 * @syncpriority 200
 	 */
 	public synchronized void stopReception(String sid) {
 		log.info("Stopping reception for stream " + sid);
 		if (!receivingSids.contains(sid)) // Leftover thread
 			return;
 		receivingSids.remove(sid);
 		// If we're still wanting to hear about sources, keep track of the ones we're receiving from
 		if (checkWantingSources(sid)) {
 			for (LCPair lcp : mina.getSCM().getListenConns(sid)) {
 				mina.getSourceMgr().cacheSourceUntilReady(lcp.getLastSourceStatus(), lcp.getLastStreamStatus());
 			}
 		}
 		// Cleanup
 		mina.getSCM().closeAllListenConns(sid);
 		if (mina.getSCM().getNumBroadcastConns(sid) == 0)
 			sleepPageBuf(sid);
 		mina.getBidStrategy().cleanupStream(sid);
 		mina.getPRM().cleanupStream(sid);
 	}
 
 	private void receptionCompleted(String sid) {
 		// Huzzah
 		log.info("Completed reception of " + sid);
 		// If we want to become a broadcaster when reception is completed
 		// (probable), then there needs to be a receptionlistener that calls
 		// startbroadcast in response to this event
 		mina.getEventMgr().fireReceptionCompleted(sid);
 		stopReception(sid);
 	}
 
 	public int getPriority(String sid) {
 		return streamPriorities.get(sid);
 	}
 
 	public void setPriority(String sid, int priority) {
 		streamPriorities.put(sid, priority);
 	}
 }
