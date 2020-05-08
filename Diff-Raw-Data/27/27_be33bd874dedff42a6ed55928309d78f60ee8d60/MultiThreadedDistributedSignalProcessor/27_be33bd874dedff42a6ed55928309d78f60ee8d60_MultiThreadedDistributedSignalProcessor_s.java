 package ar.edu.itba.pod.legajo51190.impl;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.jgroups.Address;
 import org.jgroups.ChannelListener;
 import org.jgroups.Message;
 
 import ar.edu.itba.pod.api.NodeStats;
 import ar.edu.itba.pod.api.Result;
 import ar.edu.itba.pod.api.Result.Item;
 import ar.edu.itba.pod.api.SPNode;
 import ar.edu.itba.pod.api.Signal;
 import ar.edu.itba.pod.api.SignalProcessor;
 
 /**
  * SignalProcessor implementation
  * 
  * @author cris
  */
 public class MultiThreadedDistributedSignalProcessor implements
 		JGroupSignalProcessor, SignalProcessor, SPNode {
 
 	private static class RemoteQuery {
 		private final CountDownLatch latch;
 		private final Set<Address> remoteNodes;
 		private final Set<Result> results;
 
 		public RemoteQuery(final Set<Address> allButMe) {
 			remoteNodes = Collections
 					.newSetFromMap(new ConcurrentHashMap<Address, Boolean>());
 			remoteNodes.addAll(allButMe);
 			results = new HashSet<>();
 			latch = new CountDownLatch(allButMe.size());
 		}
 
 		public CountDownLatch getLatch() {
 			return latch;
 		}
 
 		public Set<Result> getResults() {
 			return results;
 		}
 	}
 
 	private final Semaphore sem = new Semaphore(0);
	private final ThreadPoolExecutor localProcessingService;
 	private final ExecutorService requestProcessingService;
 	private final int threads;
 	private final NodeReceiver networkState;
 	private final Node node;
 	private final ConcurrentHashMap<Integer, RemoteQuery> queries;
 	private final AtomicInteger processingCount = new AtomicInteger(0);
 	private final AtomicInteger awaitQueryCount = new AtomicInteger(0);
 	private final AtomicInteger queryIdGenerator = new AtomicInteger(0);
 	private final NodeLogger nodeLogger;
 
 	public MultiThreadedDistributedSignalProcessor(final int threads)
 			throws Exception {
 		this(threads, null, null);
 	}
 
 	public MultiThreadedDistributedSignalProcessor(final int threads,
 			final Set<ChannelListener> listeners,
 			final NodeListener nodeListener) throws Exception {
 		this.threads = threads;
 		queries = new ConcurrentHashMap<>();
 		requestProcessingService = Executors.newCachedThreadPool();
		localProcessingService = new ThreadPoolExecutor(1, 1, 1,
				TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>()) {
			@Override
			protected void afterExecute(final Runnable r, final Throwable t) {

			}
		};
 
 		node = new Node(nodeListener, this);
 		networkState = new NodeReceiver(node, this);
 		node.getChannel().setDiscardOwnMessages(true);
 
 		if (networkState != null) {
 			node.getChannel().setReceiver(networkState);
 			node.getChannel().addChannelListener(networkState);
 		}
 
 		if (listeners != null) {
 			for (ChannelListener channelListener : listeners) {
 				node.getChannel().addChannelListener(channelListener);
 			}
 		}
 
 		nodeLogger = new NodeLogger(node);
 	}
 
 	@Override
 	public void add(final Signal signal) throws RemoteException {
 		synchronized (node.getToDistributeSignals()) {
 			node.getToDistributeSignals().add(signal);
 		}
 	}
 
 	private void askRemoteQueries(final Signal signal, final int queryId) {
 
 		Set<Address> allButMe = new HashSet<>(node.getAliveNodes());
 		allButMe.remove(node.getAddress());
 		System.out.println(node.getAddress() + ": Asking to " + allButMe);
 
 		queries.put(queryId, new RemoteQuery(allButMe));
 		Message msg = new Message(null, new QueryNodeMessage(signal, queryId,
 				allButMe));
 		if (node.isOnline() && node.getChannel().isConnected()) {
 			try {
 				node.getChannel().send(msg);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	private BlockingQueue<Signal> buildQuerySignalSet() {
 
 		Set<Signal> sigs = new HashSet<>();
 		synchronized (node.getToDistributeSignals()) {
 			synchronized (node.getLocalSignals()) {
 				synchronized (node.getRedistributionSignals()) {
 					sigs.addAll(node.getToDistributeSignals());
 					sigs.addAll(node.getRedistributionSignals());
 					sigs.addAll(node.getLocalSignals());
 				}
 			}
 		}
 		return new LinkedBlockingQueue<>(sigs);
 	}
 
 	@Override
 	public void exit() throws RemoteException {
 		node.exit();
 	}
 
 	@Override
 	public Result findSimilarTo(final Signal signal) throws RemoteException {
 		if (signal == null) {
 			throw new IllegalArgumentException("Signal cannot be null");
 		}
 
 		Result result = new Result(signal);
 
 		awaitQueryCount.incrementAndGet();
 		int queryId = queryIdGenerator.getAndIncrement();
 
 		askRemoteQueries(signal, queryId);
 		nodeLogger.log("Asking remotes for the query!");
 
 		nodeLogger.log("Resolving local query!");
 		result = resolveLocalQueries(signal, result);
 		nodeLogger.log("Resolved local query!");
 
 		nodeLogger.log("Awaiting remote answers!");
 		result = awaitRemoteAnswers(result, queryId);
 		nodeLogger.log("Got remote answers!");
 
 		if (result == null) {
 			try {
 				nodeLogger.log("Waiting for node recovery!");
 				sem.tryAcquire(30000, TimeUnit.MILLISECONDS);
 				nodeLogger.log("Node recovery done!");
 
 				result = new Result(signal);
 
 				askRemoteQueries(signal, queryId);
 				nodeLogger.log("Asking remotes for the query!");
 
 				nodeLogger.log("Resolving local query!");
 				result = resolveLocalQueries(signal, result);
 				nodeLogger.log("Resolved local query!");
 
 				nodeLogger.log("Awaiting remote answers!");
 				result = awaitRemoteAnswers(result, queryId);
 				nodeLogger.log("Got remote answers!");
 
 			} catch (InterruptedException e) {
 				throw new RemoteException(e.getMessage());
 			}
 		}
 		nodeLogger.log(result.toString());
 		return result;
 	}
 
 	private Result awaitRemoteAnswers(Result result, final int queryId) {
 		try {
 			RemoteQuery query = queries.get(queryId);
 			if (!query.getLatch().await(10000, TimeUnit.MILLISECONDS)) {
 				return result;
 			}
 
 			for (Result res : query.getResults()) {
 				for (Item item : res.items()) {
 					result = result.include(item);
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 		return result;
 	}
 
 	@Override
 	public JGroupNode getJGroupNode() {
 		return node;
 	}
 
 	@Override
 	public NodeStats getStats() throws RemoteException {
 		return node.getStats();
 	}
 
 	@Override
 	public void join(final String clusterName) throws RemoteException {
 		try {
 			node.joinChannel(clusterName);
 		} catch (Exception e) {
 			throw new RemoteException(e.getMessage());
 		}
 	}
 
 	@Override
 	public void onQueryReception(final QueryNodeMessage query,
 			final Address from) {
 		// if (!query.getReceiptMembers().contains(node.getAddress())) {
 		// return;
 		// }
 
 		requestProcessingService.submit(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					Result result = new Result(query.getSignal());
 
 					result = resolveLocalQueries(query.getSignal(), result);
 
 					Message msg = new Message(from, new QueryResultNodeMessage(
 							query.getQueryId(), result));
 
 					if (node.getChannel().isConnected()) {
 						try {
 							node.getChannel().send(msg);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 
 	}
 
 	@Override
 	public void onResultReception(final QueryResultNodeMessage answer,
 			final Address from) {
 		RemoteQuery query = queries.get(answer.getMessageId());
 		if (query.remoteNodes.contains(from)) {
 			query.getResults().add(answer.getResult());
 			query.getLatch().countDown();
 		}
 	}
 
 	private Result resolveLocalQueries(final Signal signal, Result result) {
 		final BlockingQueue<Signal> querySignals = buildQuerySignalSet();
		Set<Signal> copy = new HashSet<Signal>(querySignals);
 		final List<SearchCall> queries = new ArrayList<>();
 
 		for (int i = 0; i < threads; i++) {
 			queries.add(new SearchCall(querySignals, signal));
 		}
 
 		try {
 			List<Future<List<Item>>> results = localProcessingService
 					.invokeAll(queries);
 
 			for (Future<List<Item>> future : results) {
 				for (Item item : future.get()) {
 					result = result.include(item);
 				}
 			}
 
 		} catch (InterruptedException | ExecutionException e) {
 			e.printStackTrace();
 		}
 
		System.out.println(node.getAddress() + " Exploring on " + copy.size());

 		return result;
 	}
 
 	@Override
 	public void onNodeGoneFixed() {
 		System.out.println("GONE NODE FIXED!");
 		sem.release(awaitQueryCount.getAndSet(0));
 	}
 
 	@Override
 	public boolean isWorking() {
 		return processingCount.get() == 0;
 	}
 }
