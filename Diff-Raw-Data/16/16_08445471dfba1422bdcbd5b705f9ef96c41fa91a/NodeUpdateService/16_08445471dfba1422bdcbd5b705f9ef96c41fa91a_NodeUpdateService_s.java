 package ar.edu.itba.pod.legajo51190.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
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
 
 	private static final int CHUNK_SIZE = 5000;
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
 	 * Latch for awaiting the signal from the rest of the nodes of the group on
 	 * the notification of readyness after a gone member
 	 */
 	private CountDownLatch memberGoneLatch;
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
 
 						if (node.getToDistributeSignals().size() > 0) {
 
 							nodeLogger.log("Sending nodes..."
 									+ node.getToDistributeSignals().size());
 
 							// The copy of the signals to send must be isolated
 							final Set<Signal> signalsCopy = new HashSet<>();
 							synchronized (node.getToDistributeSignals()) {
 								int k = 0;
 								for (Signal signal : node
 										.getToDistributeSignals()) {
 									signalsCopy.add(signal);
 									k++;
 									if (k == CHUNK_SIZE) {
 										break;
 									}
 								}
 							}
 
 							List<Address> allMembersButMyself = getAllNodesButMe(
 									node, node.getAliveNodes());
 
 							Multimap<Address, Signal> copyOfBackupSignals = HashMultimap
 									.create();
 
 							boolean isOk = true;
 
 							isOk = syncMembers(
 									Lists.newArrayList(allMembersButMyself),
 									Lists.newArrayList(node.getAliveNodes()),
 									signalsCopy, copyOfBackupSignals);
 
 							if (!isOk) {
 
 								System.out.println("node fallen on sync!");
 								List<Address> allMinusWaiting = new ArrayList<>(
 										node.getAliveNodes());
 								allMinusWaiting.removeAll(waitingAddresses);
 
 								resolveGoneMembers(waitingAddresses,
 										allMinusWaiting);
 							}
 
 							synchronized (node.getToDistributeSignals()) {
 								node.getToDistributeSignals().removeAll(
 										signalsCopy);
 							}
 
 							if (node.getToDistributeSignals().isEmpty()
 									&& node.getListener() != null) {
 								node.getListener().onNodeSyncDone();
 							}
 
 							nodeLogger.log("Sent nodes!!!");
 						}
 
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 
 		}, 0, 1000);
 
 	}
 
 	private List<Address> getAllNodesButMe(final Node node,
 			final Collection<Address> list) {
 		List<Address> allMembersButMyself = Lists.newArrayList(list);
 		allMembersButMyself.remove(node.getAddress());
 		return allMembersButMyself;
 	}
 
 	public void updateFromView(final View new_view) {
 		nodeSyncService.submit(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					if (node.getLastView() != null
 							&& node.getToDistributeSignals().size() == 0) {
 						Set<Address> newMembers = detectNewMembers(new_view);
 						Set<Address> goneMembers = detectGoneMembers(new_view);
 
 						if (goneMembers.size() > 0) {
 							resolveGoneMembers(goneMembers,
 									new_view.getMembers());
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
 
 							awaitLatch = new CountDownLatch(1);
 
 							boolean isOK = true;
 							do {
 								int k = 0;
 
 								Set<Signal> signalsCopyToSend = new HashSet<>();
 								Multimap<Address, Signal> copyOfBackupSignalsToSend = HashMultimap
 										.create();
 
 								prepareChunkOfData(signalsCopy,
 										copyOfBackupSignals, k,
 										signalsCopyToSend,
 										copyOfBackupSignalsToSend);
 
 								nodeLogger.log("Sending nodes..."
 										+ (signalsCopyToSend.size() + copyOfBackupSignalsToSend
 												.size()));
 
 								isOK = syncMembers(
 										Lists.newArrayList(newMembers),
 										new_view.getMembers(),
 										signalsCopyToSend,
 										copyOfBackupSignalsToSend);
 
 								if (isOK) {
 									removeCopyData(signalsCopy,
 											copyOfBackupSignals,
 											signalsCopyToSend,
 											copyOfBackupSignalsToSend);
 								} else {
 									break;
 								}
 							} while (copyOfBackupSignals.size() > 0
 									|| signalsCopy.size() > 0);
 
 							if (isOK) {
 								if (!awaitLatch.await(10000,
 										TimeUnit.MILLISECONDS)) {
 									nodeLogger.log("TIMEOUTED!!!");
 								} else {
 									nodeLogger.log("New node sync call for "
 											+ newMembers.toString());
 								}
 							} else {
 								List<Address> allMinusWaiting = new ArrayList<>(
 										new_view.getMembers());
 								allMinusWaiting.removeAll(waitingAddresses);
 
 								resolveGoneMembers(waitingAddresses,
 										allMinusWaiting);
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
 
 	private void removeCopyData(final Set<Signal> signalsCopy,
 			final Multimap<Address, Signal> copyOfBackupSignals,
 			final Set<Signal> signalsCopyToSend,
 			final Multimap<Address, Signal> copyOfBackupSignalsToSend) {
 		signalsCopy.removeAll(signalsCopyToSend);
 		for (java.util.Map.Entry<Address, Signal> e : copyOfBackupSignalsToSend
 				.entries()) {
 			copyOfBackupSignals.remove(e.getKey(), e.getValue());
 		}
 	}
 
 	private void prepareChunkOfData(final Set<Signal> signalsCopy,
 			final Multimap<Address, Signal> copyOfBackupSignals, int k,
 			final Set<Signal> signalsCopyToSend,
 			final Multimap<Address, Signal> copyOfBackupSignalsToSend) {
 		for (Signal s : signalsCopy) {
 			signalsCopyToSend.add(s);
 			k++;
 			if (k == CHUNK_SIZE / 2) {
 				break;
 			}
 		}
 
 		k = 0;
 		for (java.util.Map.Entry<Address, Signal> e : copyOfBackupSignals
 				.entries()) {
 			copyOfBackupSignalsToSend.put(e.getKey(), e.getValue());
 			k++;
 			if (k == CHUNK_SIZE / 2) {
 				break;
 			}
 		}
 	}
 
 	private boolean syncMembers(final List<Address> newMembers,
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
 						backupSignalsToSend.put(backupAddr, sign);
 					}
 				}
 			}
 
 		}
 
 		boolean isOK = true;
 		if (node.isOnline()) {
 			// We send the signals to the group members
 			isOK = sendSignalsToMembers(signalsToKeep, signalsToSend,
 					backupSignalsToSend, copyMode, allMembersButMe, allMembers);
 		}
 
 		if (node.isOnline() && isOK) {
 			// We save the signals in a locked action.
 			safelySaveSignals(signalsToSend, signalsToKeep, backupSignalsToSend);
 		}
 
 		return isOK;
 	}
 
 	/**
 	 * Save signals in a transaction, must be done after all ACKs are received
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
 	private boolean sendSignalsToMembers(final Set<Signal> signalsToKeep,
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
 
 		return handleTimeouts(signalsToSend, backupSignalsToSend, copyMode,
 				allMembers);
 	}
 
 	private boolean handleTimeouts(
 			final Multimap<Address, Signal> signalsToSend,
 			final Multimap<Address, Signal> backupSignalsToSend,
 			final boolean copyMode, final List<Address> allMembers) {
 		try {
 			if (!ackLatch.await(10000, TimeUnit.MILLISECONDS)) {
 				throw new Exception("First timeout");
 			}
 		} catch (Exception e) {
 			for (Address address : waitingAddresses) {
 				if (node.isOnline()) {
 					sendSyncMessageToAddress(signalsToSend,
 							backupSignalsToSend, copyMode, allMembers, address);
 				}
 			}
 
 			if (node.isOnline()) {
 				ackLatch = new CountDownLatch(waitingAddresses.size());
 
 				try {
 					if (!ackLatch.await(10000, TimeUnit.MILLISECONDS)) {
 						throw new Exception(
 								"Second timeout, some nodes are not answering");
 					}
 				} catch (Exception e1) {
 					return false;
 				}
 			}
 
 		}
 		return true;
 	}
 
 	/**
 	 * Handler to send messages
 	 */
 	private void sendSyncMessageToAddress(
 			final Multimap<Address, Signal> signalsToSend,
 			final Multimap<Address, Signal> backupSignalsToSend,
 			final boolean copyMode, final List<Address> allMembers,
 			final Address address) {
 
 		if (node.isOnline()) {
 			try {
 				node.getChannel().send(
 						new Message(address, new GlobalSyncNodeMessage(
 								signalsToSend, backupSignalsToSend, copyMode,
 								allMembers)));
 			} catch (Exception e1) {
 				if (node.isOnline()) {
 					e1.printStackTrace();
 				}
 			}
 
 		}
 	}
 
 	/**
 	 * Recovers the backups from the fallen nodes. If more than one member is
 	 * gone we cannot guarantee the recovery of all messages.
 	 */
 	private void resolveGoneMembers(final Collection<Address> goneMembers,
 			final List<Address> allMembers) {
 		if (goneMembers.size() > 1) {
 			nodeLogger
 					.log("More than one member is gone, we might have lost messages :(");
 		}
 		synchronized (node.getBackupSignals()) {
 			synchronized (node.getLocalSignals()) {
 				synchronized (node.getToDistributeSignals()) {
 					synchronized (node.getRedistribuitionSignals()) {
 						for (Address address : goneMembers) {
 							nodeLogger.log("Recovering backups from "
 									+ address.toString()
 									+ " size: "
 									+ node.getBackupSignals().get(address)
 											.size());
 							node.getRedistribuitionSignals().addAll(
 									node.getBackupSignals().get(address));
 						}
 						node.getRedistribuitionSignals().addAll(
 								node.getLocalSignals());
 						node.getRedistribuitionSignals().addAll(
 								node.getToDistributeSignals());
 						node.getBackupSignals().clear();
 						node.getLocalSignals().clear();
 						node.getToDistributeSignals().clear();
 						timerEnabled.set(false);
 					}
 				}
 			}
 		}
 
 		Set<Signal> signalsCopy = null;
 		synchronized (node.getRedistribuitionSignals()) {
 			signalsCopy = new HashSet<>(node.getRedistribuitionSignals());
 		}
 
 		System.out.println("Total redistribution size: " + signalsCopy.size());
 
 		Multimap<Address, Signal> copyOfBackupSignals = HashMultimap
 				.create(node.getBackupSignals());
 
 		List<Address> allMembersButMyself = getAllNodesButMe(node, allMembers);
 
 		memberGoneLatch = new CountDownLatch(allMembersButMyself.size());
 
 		do {
 			int k = 0;
 
 			Set<Signal> signalsCopyToSend = new HashSet<>();
 			Multimap<Address, Signal> copyOfBackupSignalsToSend = HashMultimap
 					.create();
 
 			prepareChunkOfData(signalsCopy, copyOfBackupSignals, k,
 					signalsCopyToSend, copyOfBackupSignalsToSend);
 
 			nodeLogger.log("Sending nodes..."
 					+ (signalsCopyToSend.size() + copyOfBackupSignalsToSend
 							.size()));
 			syncMembers(allMembersButMyself, allMembers, signalsCopyToSend,
 					copyOfBackupSignalsToSend);
 
 			removeCopyData(signalsCopy, copyOfBackupSignals, signalsCopyToSend,
 					copyOfBackupSignalsToSend);
 		} while (copyOfBackupSignals.size() > 0 || signalsCopy.size() > 0);
 
 		if (node.isOnline()) {
 			tellOtherNodesImDoneRedistributingData();
 		}
 
 		try {
 			memberGoneLatch.await(10000, TimeUnit.MILLISECONDS);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		node.getRedistribuitionSignals().clear();
 
 		if (node.getListener() != null) {
 			node.getListener().onNodeGoneSyncDone();
 		}
 		node.getSignalProcessor().onNodeGoneFixed();
 
 	}
 
 	private void tellOtherNodesImDoneRedistributingData() {
 
 		Message tellImDone = new Message(null, new GoneMessageSentNodeMessage());
 
 		try {
 			node.getChannel().send(tellImDone);
 		} catch (Exception e1) {
 			e1.printStackTrace();
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
 
 	public void notifyGoneMessage() {
 		if (memberGoneLatch != null) {
 			memberGoneLatch.countDown();
 		}
 	}
 }
