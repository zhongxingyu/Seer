 package ar.edu.itba.pod.legajo51190.impl;
 
 import java.rmi.RemoteException;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentSkipListSet;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.annotation.Nullable;
 
 import org.jgroups.Address;
 import org.jgroups.Channel;
 import org.jgroups.JChannel;
 import org.jgroups.View;
 
 import ar.edu.itba.pod.api.NodeStats;
 import ar.edu.itba.pod.api.Signal;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Multimaps;
 
 public class Node implements JGroupNode {
 	private final Multimap<Address, Signal> backupSignals;
 	private Set<Address> aliveNodes = new HashSet<>();
 	private Set<String> aliveNodeNames = new HashSet<>();
 	private View lastView;
 	private Address nodeAddress;
 	private final AtomicBoolean isDegraded = new AtomicBoolean(false);
 	private final Set<Signal> signals = Collections
 			.newSetFromMap(new ConcurrentHashMap<Signal, Boolean>());
 	private final BlockingQueue<Signal> toDistributeSignals = new LinkedBlockingQueue<>();
 	private final Set<Signal> redistributionSignals = Collections
 			.newSetFromMap(new ConcurrentHashMap<Signal, Boolean>());
 	private Channel channel;
 	private final NodeListener listener;
 	private final AtomicBoolean online = new AtomicBoolean(false);
 	private final JGroupSignalProcessor signalProcessor;
 	private boolean isNew = true;
 	private final Semaphore newNodeSemaphore = new Semaphore(0);
 	private final Set<Signal> temporalSignals = Collections
 			.newSetFromMap(new ConcurrentHashMap<Signal, Boolean>());
	private final AtomicInteger queryCount = new AtomicInteger(0);
 
 	public Node(final NodeListener listener,
 			final JGroupSignalProcessor signalProcessor) throws Exception {
 		super();
 		this.signalProcessor = signalProcessor;
 		channel = new JChannel("udp-largecluster.xml");
 		this.listener = listener;
 		Multimap<Address, Signal> sig = HashMultimap.create();
 		backupSignals = Multimaps.synchronizedMultimap(sig);
 	}
 
 	@Override
 	public void reset() throws Exception {
 		setIsNew(true);
 		online.set(false);
 		newNodeSemaphore.drainPermits();
 		setNodeView(null);
 		setNodeAddress(null);
 		channel = new JChannel("udp-largecluster.xml");
 	}
 
 	@Override
 	public Address getAddress() {
 		return nodeAddress;
 	}
 
 	@Override
 	public Set<String> getAliveNodeNames() {
 		return aliveNodeNames;
 	}
 
 	@Override
 	public Set<Address> getAliveNodes() {
 		return aliveNodes;
 	}
 
 	@Override
 	public Channel getChannel() {
 		return channel;
 	}
 
 	@Override
 	public AtomicBoolean getIsDegraded() {
 		return isDegraded;
 	}
 
 	@Override
 	public View getLastView() {
 		return lastView;
 	}
 
 	@Override
 	public Set<Signal> getLocalSignals() {
 		return signals;
 	}
 
 	@Override
 	public BlockingQueue<Signal> getToDistributeSignals() {
 		return toDistributeSignals;
 	}
 
 	@Override
 	public Set<Signal> getTemporalSignals() {
 		return temporalSignals;
 	}
 
 	public void setDegraded(final boolean b) {
 		isDegraded.set(b);
 	}
 
 	public void setNodeAddress(final Address address) {
 		nodeAddress = address;
 	}
 
 	public void setNodeView(final View view) {
 		aliveNodes = new ConcurrentSkipListSet<>(view.getMembers());
 		aliveNodeNames = new ConcurrentSkipListSet<String>(
 				Collections2.transform(aliveNodes,
 						new Function<Address, String>() {
 							@Override
 							public String apply(@Nullable final Address input) {
 								return input.toString();
 							}
 						}));
 		lastView = view;
 	}
 
 	@Override
 	public Multimap<Address, Signal> getBackupSignals() {
 		return backupSignals;
 	}
 
 	@Override
 	public NodeStats getStats() {
		return new NodeStats(getAddress() != null ? getAddress().toString()
				: "Sin canal", queryCount.get(), signals.size()
 				+ toDistributeSignals.size(), backupSignals.size(), false);
 	}
 
 	@Override
 	public NodeListener getListener() {
 		return listener;
 	}
 
 	@Override
 	public Set<Signal> getRedistributionSignals() {
 		return redistributionSignals;
 	}
 
 	@Override
 	public boolean isOnline() {
 		return online.get();
 	}
 
 	@Override
 	public void joinChannel(final String name) throws RemoteException {
 		try {
 			getChannel().connect(name);
 			online.set(true);
 		} catch (Exception e) {
 			throw new RemoteException(e.getMessage());
 		}
 
 	}
 
 	public void exit() {
 		online.set(false);
 		getLocalSignals().clear();
 		getToDistributeSignals().clear();
 		getRedistributionSignals().clear();
 		getTemporalSignals().clear();
 		getChannel().disconnect();
 	}
 
 	@Override
 	public JGroupSignalProcessor getSignalProcessor() {
 		return signalProcessor;
 	}
 
 	public void setIsNew(final boolean b) {
 		isNew = b;
 	}
 
 	public boolean isNew() {
 		return isNew;
 	}
 
 	public Semaphore getNewSemaphore() {
 		return newNodeSemaphore;
 	}
 
	public AtomicInteger getQueryCount() {
		return queryCount;
	}

 }
