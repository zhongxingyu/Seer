 package ar.edu.itba.pod.legajo51190.impl;
 
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.jgroups.Address;
 import org.jgroups.Channel;
 import org.jgroups.Message;
 import org.jgroups.View;
 
 import ar.edu.itba.pod.api.Signal;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 /**
  * Handles all the incoming data, messages, and events of a node. It must handle
  * the receipt of data from other nodes, and send the messages to the required
  * component, which can be itself.
  * 
  * @author cris
  * 
  */
 public class NodeReceiver extends BaseJGroupNodeReceiver {
 
 	private final NodeUpdateService updateService;
 	private final Node node;
 	private final BlockingQueue<Callable<Void>> connectionPendingTasks;
 	private final ExecutorService connectionService;
 	private final NodeLogger nodeLogger;
 	private final AtomicInteger newNodePartsCount = new AtomicInteger(0);
 
 	public NodeReceiver(
 			final Node node,
 			final MultiThreadedDistributedSignalProcessor multiThreadedDistributedSignalProcessor) {
 		this.node = node;
 
 		updateService = new NodeUpdateService(node,
 				multiThreadedDistributedSignalProcessor);
 		connectionService = Executors.newCachedThreadPool();
 		connectionPendingTasks = new LinkedBlockingQueue<>();
 		nodeLogger = new NodeLogger(node);
 	}
 
 	@Override
 	public void viewAccepted(final View new_view) {
 		updateService.updateFromView(new_view);
 	}
 
 	@Override
 	public void suspect(final Address suspected_mbr) {
 		nodeLogger.log("Member might have gone: " + suspected_mbr);
 	}
 
 	@Override
 	public void receive(final Message msg) {
 		if (msg.getObject() instanceof MemberWelcomeNodeMessage) {
 			MemberWelcomeNodeMessage message = (MemberWelcomeNodeMessage) msg
 					.getObject();
 			handleNewNodeCallback(message.getAllMembers(),
 					message.getDestinations(), true);
 		} else if (msg.getObject() instanceof GlobalSyncNodeMessage) {
 			// If the message is a globalsync, we might not have our view info
 			// ready. So we must take that into account.
 			safelyProcess(msg);
 		} else if (msg.getObject() instanceof GlobalSyncNodeMessageAnswer) {
 			// In this case that's not the same, there is no problem.
 			onNewNodeSyncAnswer((GlobalSyncNodeMessageAnswer) msg.getObject());
 		} else if (msg.getObject() instanceof NewNodeReadyMessage) {
 			onNotifyNewNodeReady();
 		} else if (msg.getObject() instanceof SyncDoneNodeMessage) {
 			onNotifyGoneMessageReceived();
 		} else if (msg.getObject() instanceof QueryNodeMessage) {
			node.getQueryCount().incrementAndGet();
 			node.getSignalProcessor().onQueryReception(
 					(QueryNodeMessage) msg.getObject(), msg.getSrc());
 		} else if (msg.getObject() instanceof QueryResultNodeMessage) {
 			node.getSignalProcessor().onResultReception(
 					(QueryResultNodeMessage) msg.getObject(), msg.getSrc());
 		}
 	}
 
 	private void onNotifyGoneMessageReceived() {
 		updateService.notifySyncMessage();
 	}
 
 	private void onNotifyNewNodeReady() {
 		updateService.notifyNewNodeReady();
 	}
 
 	private void safelyProcess(final Message msg) {
 		if (node.getChannel().isConnected()) {
 			onNewNodeSync(msg, (GlobalSyncNodeMessage) msg.getObject());
 
 		} else {
 			nodeLogger.log("Submitted for future tasks!");
 			// Submit the task for further execution
 			connectionPendingTasks.add(new Callable<Void>() {
 				@Override
 				public Void call() {
 					try {
 
 						onNewNodeSync(msg,
 								(GlobalSyncNodeMessage) msg.getObject());
 
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					return null;
 				}
 			});
 		}
 	}
 
 	private void onNewNodeSyncAnswer(final GlobalSyncNodeMessageAnswer message) {
 		updateService.notifyNodeAnswer(message);
 	}
 
 	private void onNewNodeSync(final Message msg,
 			final GlobalSyncNodeMessage message) {
 		nodeLogger.log("Got data! " + message.getSignalsMap().size()
 				+ " signals " + message.getBackupSignals().size() + " backups");
 
 		// We save the signals that were sent to us
 		storeLocalSignals(message);
 
 		// We either just store the data we receive
 		// Or we move it, depending on which our role is
 		handleBackupSignals(msg, message);
 
 		// We send a message telling that we made the transaction
 		// This is an ACK that we're not dead
 		tellWereDone(msg);
 
 		// If we're a new node we tell we're no longer one
 		// After we got all the messages from all our neighbours
 
 		handleNewNodeCallback(message.getAllMembers(),
 				message.getDestinations(), message.isLastSyncMessage());
 
 		// nodeLogger.log("We are done!");
 	}
 
 	private synchronized void handleNewNodeCallback(
 			final List<Address> allMembers, final Set<Address> destinations,
 			final boolean isLastSyncMessage) {
 		if (node.isNew() && isLastSyncMessage) {
 			newNodePartsCount.addAndGet(1);
 			nodeLogger.log("New node check");
 			if (newNodePartsCount.get() == allMembers.size()
 					- destinations.size()) {
 				final Message newNodeReply = new Message(null);
 				newNodeReply.setObject(new NewNodeReadyMessage());
 				sendSafeAnswer(newNodeReply);
 				node.setIsNew(false);
 				updateService.allowSync();
 			}
 
 		}
 
 	}
 
 	private void tellWereDone(final Message msg) {
 		final Message reply = msg.makeReply();
 		reply.setObject(new GlobalSyncNodeMessageAnswer(node.getAddress()));
 		sendSafeAnswer(reply);
 
 	}
 
 	private void handleBackupSignals(final Message msg,
 			final GlobalSyncNodeMessage message) {
 		synchronized (node.getBackupSignals()) {
 			// Copy mode is for when no backups are done
 			// If backups are already distributed then we proceed from this
 			// branch
 			if (message.isCopyMode()) {
 				// If this is a copy mode operation then each
 				// receiver stores the senders data a as backup
 				copyBackups(msg, message);
 			} else {
 				if (message.getDestinations().contains(node.getAddress())) {
 					// If i'm one of the original receivers of this data,
 					// Then I save my backup copies, which are for me
 					saveMyBackups(message);
 				} else {
 					// If i'm not, then the message contains
 					// data that I have backed up for the source
 					// but the source has moved that data.
 					// I must update my backups for that
 					redistributeBackupsFromSource(msg, message);
 				}
 			}
 
 		}
 	}
 
 	private void redistributeBackupsFromSource(final Message msg,
 			final GlobalSyncNodeMessage message) {
 		Set<Signal> signals = Sets.newHashSet(node.getBackupSignals().get(
 				msg.getSrc()));
 
 		node.getBackupSignals().removeAll(msg.getSrc());
 
 		signals.removeAll(message.getSignalsMap().values());
 
 		List<Address> allThirdKindMembers = Lists.newArrayList(message
 				.getAllMembers());
 		List<Address> allMembers = Lists.newArrayList(message.getAllMembers());
 
 		allThirdKindMembers.removeAll(message.getDestinations());
 		allThirdKindMembers.remove(msg.getSrc());
 
 		for (Address address : message.getSignalsMap().keySet()) {
 			for (Signal signal : message.getSignalsMap().get(address)) {
 				Address owner = updateService.getAddressForSignal(signal,
 						allThirdKindMembers, allMembers);
 
 				if (owner.equals(node.getAddress())) {
 					node.getBackupSignals().put(address, signal);
 				}
 
 			}
 		}
 
 		node.getBackupSignals().putAll(msg.getSrc(), signals);
 	}
 
 	/**
 	 * Saves the backups that were delegated to him, which are not from the same
 	 * source, but were owned by the source
 	 */
 	private void saveMyBackups(final GlobalSyncNodeMessage message) {
 		for (Address addr : message.getBackupSignals().keySet()) {
 			node.getBackupSignals().putAll(addr,
 					message.getBackupSignals().get(addr));
 		}
 	}
 
 	/**
 	 * Stores inside the node the messages that correspond to the member of the
 	 * group
 	 */
 	private void storeLocalSignals(final GlobalSyncNodeMessage message) {
 		synchronized (node.getLocalSignals()) {
 			if (message.getDestinations().contains(node.getAddress())) {
 
 				node.getLocalSignals().addAll(
 						message.getSignalsMap().get(node.getAddress()));
 			}
 		}
 	}
 
 	/**
 	 * Copies the data the source sent, and saves it as a backup, this is done
 	 * when new nodes first join, or when new signals arrive
 	 */
 	private void copyBackups(final Message msg,
 			final GlobalSyncNodeMessage message) {
 
 		for (Address addr : message.getBackupSignals().keySet()) {
 			if (addr.equals(node.getAddress())) {
 				node.getBackupSignals().putAll(msg.getSrc(),
 						message.getBackupSignals().get(addr));
 			}
 		}
 	}
 
 	private void sendSafeAnswer(final Message reply) {
 		try {
 			synchronized (connectionPendingTasks) {
 				if (node.getChannel().isConnected()) {
 					node.getChannel().send(reply);
 				} else {
 					connectionPendingTasks.add(new Callable<Void>() {
 						@Override
 						public Void call() {
 							try {
 								node.getChannel().send(reply);
 							} catch (Exception e) {
 								e.printStackTrace();
 							}
 							return null;
 						}
 					});
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void channelConnected(final Channel channel) {
 		synchronized (connectionPendingTasks) {
 			node.setNodeAddress(channel.getAddress());
 			node.setNodeView(channel.getView());
 			for (Callable<Void> task : connectionPendingTasks) {
 				connectionService.submit(task);
 			}
 			connectionPendingTasks.clear();
 		}
 
 		System.out.println("Connected!");
 	}
 
 }
