 package ar.edu.itba.pod.legajo51190.impl;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.jgroups.Address;
 import org.jgroups.Message;
 import org.jgroups.View;
 
 import ar.edu.itba.pod.api.Signal;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Sets;
 
 public class NodeUpdateService {
 
 	/**
 	 * Represents the addresses awaiting for response when a synchronization
 	 * action is done
 	 */
 	private final BlockingQueue<Address> waitingAddresses = new LinkedBlockingQueue<>();
 	/**
 	 * Single thread pool, that handles all the incoming tasks and executes one
 	 * at a time.
 	 */
 	private final ThreadPoolExecutor nodeSyncService;
 	/**
 	 * Node to sync.
 	 */
 	private final Node node;
 	/**
 	 * Latch for awaiting the ACKs from the members of the group.
 	 */
 	private CountDownLatch ackLatch;
 	/**
 	 * Latch for awaiting the signal from new nodes to synchronize, needed to
 	 * have many incoming nodes at the same time without exploding
 	 */
 	private CountDownLatch awaitLatch;
 	/**
 	 * Timer that polls the "to distribute signals" for new elements in order to
 	 * send them.
 	 */
 	private final Timer dataUpdateTimer;
 	/**
 	 * The timer can only be activated then no new node is synchronizing so it
 	 * can be controlled with this.
 	 */
 	private final AtomicBoolean timerEnabled = new AtomicBoolean(true);
 	private final NodeLogger nodeLogger;
 
 	public NodeUpdateService(final Node node) {
 		this.node = node;
 		nodeSyncService = new ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES,
 				new LinkedBlockingQueue<Runnable>()) {
 			@Override
 			protected void afterExecute(final Runnable r, final Throwable t) {
 				node.setDegraded(getQueue().isEmpty());
 				timerEnabled.set(getQueue().isEmpty());
 			}
 		};
 
 		nodeLogger = new NodeLogger(node);
 
 		dataUpdateTimer = new Timer();
 		dataUpdateTimer.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 				try {
 
 					// If there is connection and the timer can work.
 					if (timerEnabled.get() && node.getChannel() != null
 							&& node.getChannel().isConnected()) {
 
 						// The copy of the signals to send must be isolated
 						final Set<Signal> signalsCopy = new HashSet<>();
 						synchronized (node.getToDistributeSignals()) {
 							signalsCopy.addAll(node.getToDistributeSignals());
							node.getToDistributeSignals().clear();
 						}
 
 						if (signalsCopy.size() > 0) {
 
 							// TODO: DRY this references if possible
 							List<Address> allMembersButMyself = Lists
 									.newArrayList(node.getAliveNodes());
 							allMembersButMyself.remove(node.getAddress());
 
 							// TODO: Take this out.
 							Multimap<Address, Signal> copyOfBackupSignals = null;
 							synchronized (node.getBackupSignals()) {
 								copyOfBackupSignals = HashMultimap.create();
 							}
 
 							nodeLogger.log("Updating my new nodes...");
 
 							// This synchronized might be a bit too much.
 							synchronized (node) {
 								syncMembers(
 										Lists.newArrayList(allMembersButMyself),
 										Lists.newArrayList(node.getAliveNodes()),
 										signalsCopy, copyOfBackupSignals);
 							}
 
 							if (node.getToDistributeSignals().isEmpty()
 									&& node.getListener() != null) {
 								node.getListener().onNodeSyncDone();
 							}
 						}
 
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}, 0, 1000);
 
 	}
 
 	public void updateFromView(final View new_view) {
 		nodeSyncService.submit(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					if (node.getLastView() != null) {
 						Set<Address> newMembers = detectNewMembers(new_view);
 						Set<Address> goneMembers = detectGoneMembers(new_view);
 
 						if (goneMembers.size() > 0) {
 							resolveGoneMembers(goneMembers);
 						}
 						if (newMembers.size() > 0
 								&& node.getLocalSignals().size() > 0) {
 
 							Set<Signal> signalsCopy = null;
 							synchronized (node.getLocalSignals()) {
 								signalsCopy = new HashSet<>(node
 										.getLocalSignals());
 							}
 
 							Multimap<Address, Signal> copyOfBackupSignals = null;
 
 							synchronized (node.getBackupSignals()) {
 								copyOfBackupSignals = HashMultimap.create(node
 										.getBackupSignals());
 							}
 
 							// This synchronized might be a bit too much.
 							synchronized (node) {
 								awaitLatch = new CountDownLatch(1);
 
 								syncMembers(Lists.newArrayList(newMembers),
 										new_view.getMembers(), signalsCopy,
 										copyOfBackupSignals);
 
 								if (!awaitLatch.await(3000,
 										TimeUnit.MILLISECONDS)) {
 									nodeLogger.log("TIMEOUTED!!!");
 								} else {
 									nodeLogger.log("New node sync call for "
 											+ newMembers.toString());
 								}
 
 							}
 
 							if (node.getListener() != null) {
 								node.getListener().onNodeSyncDone();
 							}
 
 						}
 
 					}
 					node.setNodeView(new_view);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	private void syncMembers(final List<Address> newMembers,
 			final List<Address> allMembers, final Set<Signal> signalsCopy,
 			final Multimap<Address, Signal> backupMapCopy) {
 
 		Multimap<Address, Signal> signalsToSend = HashMultimap.create();
 
 		Set<Signal> signalsToKeep = new HashSet<>();
 
 		// From all the signals we are going to send
 		// We distribute them 'evenly' across all members
 		// And we keep some and send some.
 		for (Signal signal : signalsCopy) {
 			Address addressToSendData = getAddressForSignal(signal, allMembers,
 					allMembers);
 			if (newMembers.contains(addressToSendData)) {
 				signalsToSend.put(addressToSendData, signal);
 			} else {
 				signalsToKeep.add(signal);
 			}
 		}
 
 		List<Address> allMembersButMe = new ArrayList<>(allMembers);
 		allMembersButMe.remove(node.getAddress());
 
 		Multimap<Address, Signal> backupSignalsToSend = HashMultimap.create();
 
 		boolean copyMode = allMembers.size() - newMembers.size() == 1;
 
 		// If we're in copy mode we're gonna send twice as much of data,
 		// Because we need to send the copy from our data.
 		if (copyMode) {
 			// Since it can be a simple 'to myself' synchronization
 			// We need to check this.
 			if (allMembers.size() > 1) {
 				// From all the signals we have to keep
 				// We send a backup to all the members but ourselves.
 				// This way, for each signal we store, there is a backup.
 				for (Signal signal : signalsToKeep) {
 					Address addressToSendData = getAddressForSignal(signal,
 							allMembersButMe, allMembers);
 					backupSignalsToSend.put(addressToSendData, signal);
 				}
 			}
 		} else {
 			// If we're not in copy mode we're only going to send
 			// data from our backups.
 			List<Address> newMembersAndMe = new ArrayList<>(newMembers);
 			newMembersAndMe.add(node.getAddress());
 			for (Address backupAddr : backupMapCopy.keySet()) {
 				for (Signal sign : backupMapCopy.get(backupAddr)) {
 					Address addressToSendData = getAddressForSignal(sign,
 							allMembersButMe, allMembers);
 					if (!addressToSendData.equals(node.getAddress())
 							&& newMembersAndMe.contains(addressToSendData)) {
 						// TODO: Check if we're not sending data badly..
 						// Should we add !newMembersAndMe.contains(backupAddr) ?
 						backupSignalsToSend.put(backupAddr, sign);
 					}
 				}
 			}
 
 		}
 
 		// We send the signals to the group members
 		sendSignalsToMembers(signalsToKeep, signalsToSend, backupSignalsToSend,
 				copyMode, allMembersButMe, allMembers);
 
 		// We save the signals in a locked action.
 		safelySaveSignals(signalsToSend, signalsToKeep, backupSignalsToSend);
 	}
 
 	/**
 	 * Save signals in a transaction, must be done after all ACKs are received
 	 * TODO: Improve and dont do when not all are received
 	 * 
 	 * @param signalsToSend
 	 *            Signals that will be send as data to store to the new nodes /
 	 *            or members
 	 * @param signalsToKeep
 	 *            Signals that will instead be kept as local data.
 	 * @param backupSignalsToSend
 	 *            Backup signals that will be shared to the new nodes
 	 */
 	private void safelySaveSignals(
 			final Multimap<Address, Signal> signalsToSend,
 			final Set<Signal> signalsToKeep,
 			final Multimap<Address, Signal> backupSignalsToSend) {
 		synchronized (node.getLocalSignals()) {
 			synchronized (node.getBackupSignals()) {
 				for (Address addr : signalsToSend.keySet()) {
 					node.getLocalSignals().removeAll(signalsToSend.get(addr));
 				}
 
 				Set<Address> addresses = Sets.newHashSet(node
 						.getBackupSignals().keySet());
 
 				for (Address addr : addresses) {
 					for (Signal signal : backupSignalsToSend.get(addr)) {
 						node.getBackupSignals().remove(addr, signal);
 					}
 				}
 
 				node.getLocalSignals().addAll(signalsToKeep);
 			}
 		}
 	}
 
 	/**
 	 * Sends the signals to the members of the group.
 	 */
 	private void sendSignalsToMembers(final Set<Signal> signalsToKeep,
 			final Multimap<Address, Signal> signalsToSend,
 			final Multimap<Address, Signal> backupSignalsToSend,
 			final boolean copyMode, final List<Address> receptors,
 			final List<Address> allMembers) {
 
 		// Must be started before any message is sent, we don't want unexpected
 		// countdowns :)
 		ackLatch = new CountDownLatch(receptors.size());
 
 		try {
 
 			for (Address receptor : receptors) {
 				sendSyncMessageToAddress(signalsToSend, backupSignalsToSend,
 						copyMode, allMembers, receptor);
 			}
 
 			// If we're on copy mode, we save all the data we sent as backup
 			// It's logical isn't it?
 			if (copyMode) {
 				for (Address addr : signalsToSend.keySet()) {
 					if (!addr.equals(node.getAddress())) {
 
 						node.getBackupSignals().putAll(addr,
 								signalsToSend.get(addr));
 
 					}
 				}
 			}
 			waitingAddresses.addAll(receptors);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		handleTimeouts(signalsToSend, backupSignalsToSend, copyMode, allMembers);
 	}
 
 	private void handleTimeouts(final Multimap<Address, Signal> signalsToSend,
 			final Multimap<Address, Signal> backupSignalsToSend,
 			final boolean copyMode, final List<Address> allMembers) {
 		try {
 			if (!ackLatch.await(5, TimeUnit.SECONDS)) {
 				throw new Exception("First timeout");
 			}
 		} catch (Exception e) {
 			for (Address address : waitingAddresses) {
 				System.out.println("Sending to " + address);
 				sendSyncMessageToAddress(signalsToSend, backupSignalsToSend,
 						copyMode, allMembers, address);
 			}
 
 			ackLatch = new CountDownLatch(waitingAddresses.size());
 
 			try {
 				if (!ackLatch.await(5, TimeUnit.SECONDS)) {
 					throw new Exception(
 							"Second timeout, some nodes are not answering");
 				}
 			} catch (Exception e1) {
 				// TODO: Handle this.
 				e1.printStackTrace();
 			}
 
 		}
 	}
 
 	/**
 	 * Handler to send messages
 	 */
 	private void sendSyncMessageToAddress(
 			final Multimap<Address, Signal> signalsToSend,
 			final Multimap<Address, Signal> backupSignalsToSend,
 			final boolean copyMode, final List<Address> allMembers,
 			final Address address) {
 		try {
 			node.getChannel().send(
 					new Message(address, new GlobalSyncNodeMessage(
 							signalsToSend, backupSignalsToSend, copyMode,
 							allMembers)));
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	/**
 	 * Recovers the backups from the fallen nodes. If more than one member is
 	 * gone we cannot guarantee the recovery of all messages.
 	 * 
 	 * TODO: Implements this well.
 	 */
 	private void resolveGoneMembers(final Set<Address> goneMembers) {
 		if (goneMembers.size() > 1) {
 			nodeLogger
 					.log("More than one member is gone, we might have lost messages :(");
 		}
 		synchronized (node.getBackupSignals()) {
 			synchronized (node.getLocalSignals()) {
 				for (Address address : goneMembers) {
 					nodeLogger.log("Recovering backups from "
 							+ address.toString() + " size: "
 							+ node.getBackupSignals().get(address).size());
 					node.getToDistributeSignals().addAll(
 							node.getBackupSignals().get(address));
 				}
 				node.getToDistributeSignals().addAll(node.getLocalSignals());
 				node.getBackupSignals().clear();
 				node.getLocalSignals().clear();
 			}
 		}
 	}
 
 	private Set<Address> detectGoneMembers(final View new_view) {
 		Set<Address> goneMembers = new HashSet<Address>();
 		for (Address address : node.getLastView().getMembers()) {
 			if (!new_view.containsMember(address)) {
 				nodeLogger.log("Gone member! bye! " + address);
 				node.setDegraded(true);
 				goneMembers.add(address);
 			}
 		}
 		return goneMembers;
 	}
 
 	private Set<Address> detectNewMembers(final View new_view) {
 		Set<Address> newMembers = new HashSet<Address>();
 		for (Address address : new_view.getMembers()) {
 			if (!node.getLastView().containsMember(address)) {
 				// nodeLogger.log("New member! hey! " + address);
 				node.setDegraded(true);
 				newMembers.add(address);
 			}
 		}
 		return newMembers;
 	}
 
 	/**
 	 * Assigns a node to a signal, given a set of nodes to consider, and all the
 	 * nodes of the group. It's very important that the set of nodes is the same
 	 * for each distribution of the nodes. If that condition is not met the
 	 * assignment will be erroneous and the data will be spread badly.
 	 * Consistency and synchronization on this method is very important
 	 */
 	public Address getAddressForSignal(final Signal sig,
 			final List<Address> membersToPutIn, final List<Address> allMembers) {
 		int size = membersToPutIn.size();
 		int allMembersSize = allMembers.size();
 		int index = Math.abs(sig.hashCode()) % allMembersSize % size;
 		return membersToPutIn.get(index);
 	}
 
 	/**
 	 * ACK of the synchronization of a member, if it's not received then we
 	 * retry, if not, we must handle the error.
 	 */
 	public void notifyNodeAnswer(final GlobalSyncNodeMessageAnswer message) {
 		waitingAddresses.remove(message.getOwner());
 		ackLatch.countDown();
 	}
 
 	/**
 	 * Notification for when a new node is ready and has all it's copies. No
 	 * other synchronization action can be taken before this message is
 	 * received, otherwise things get nasty.
 	 */
 	public void notifyNewNodeReady() {
 		if (awaitLatch != null) {
 			awaitLatch.countDown();
 		}
 	}
 }
