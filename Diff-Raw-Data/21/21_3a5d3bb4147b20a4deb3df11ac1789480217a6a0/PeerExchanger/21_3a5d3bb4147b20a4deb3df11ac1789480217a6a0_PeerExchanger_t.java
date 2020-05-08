 package edu.teco.dnd.network;
 
 import java.net.InetSocketAddress;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import edu.teco.dnd.network.messages.PeerMessage;
 import edu.teco.dnd.network.messages.Response;
 import edu.teco.dnd.util.FutureListener;
 import edu.teco.dnd.util.FutureNotifier;
 
 public class PeerExchanger implements ConnectionListener, MessageHandler<PeerMessage> {
 	/**
 	 * The logger for this class.
 	 */
 	private static final Logger LOGGER = LogManager.getLogger(PeerExchanger.class);
 
 	private final Map<UUID, Set<InetSocketAddress>> modules = new HashMap<UUID, Set<InetSocketAddress>>();
 
 	private final AtomicReference<PeerMessage> peerMessage = new AtomicReference<PeerMessage>(new PeerMessage());
 
 	private final ReadWriteLock modulesLock = new ReentrantReadWriteLock();
 
 	private final TCPConnectionManager connectionManager;
 
 	public PeerExchanger(final TCPConnectionManager connectionManager) {
 		this.connectionManager = connectionManager;
 		this.connectionManager.addConnectionListener(this);
 		this.connectionManager.addHandler(PeerMessage.class, this);
 	}
 
 	public boolean addModule(final UUID uuid, final Collection<? extends InetSocketAddress> addresses) {
 		if (addresses == null || addresses.isEmpty()) {
 			return false;
 		}
 
 		boolean changed = false;
 		modulesLock.writeLock().lock();
 		try {
 			changed = putAddress(uuid, addresses);
 			if (changed) {
 				peerMessage.set(new PeerMessage(modules));
 			}
 		} finally {
 			modulesLock.writeLock().unlock();
 		}
 		return changed;
 	}
 
 	public boolean addModules(final Map<UUID, ? extends Collection<? extends InetSocketAddress>> modules) {
 		boolean changed = false;
 		modulesLock.writeLock().lock();
 		try {
 			for (final Entry<UUID, ? extends Collection<? extends InetSocketAddress>> entry : modules.entrySet()) {
 				changed = changed || putAddress(entry.getKey(), entry.getValue());
 			}
 			if (changed) {
 				peerMessage.set(new PeerMessage(modules));
 			}
 		} finally {
 			modulesLock.writeLock().unlock();
 		}
 		return changed;
 	}
 
 	private boolean putAddress(final UUID uuid, final Collection<? extends InetSocketAddress> addresses) {
 		Set<InetSocketAddress> oldSet = modules.get(uuid);
 		if (oldSet == null) {
 			oldSet = new HashSet<InetSocketAddress>();
 			modules.put(uuid, oldSet);
 		}
 		return oldSet.addAll(addresses);
 	}
 
 	@Override
 	public void connectionEstablished(final UUID uuid) {
 		connectionManager.sendMessage(uuid, peerMessage.get()).addListener(
 				new FutureListener<FutureNotifier<? super Response>>() {
 					@Override
 					public void operationComplete(final FutureNotifier<? super Response> future) throws Exception {
 						if (LOGGER.isDebugEnabled()) {
 							LOGGER.debug("{}: {} {}", future, future.getNow(), future.cause());
 						}
 					}
 				});
 	}
 
 	@Override
 	public void connectionClosed(final UUID uuid) {
 	}
 
 	@Override
	public Response handleMessage(final UUID remoteUUID, final PeerMessage message) {
 		LOGGER.entry(connectionManager, remoteUUID, message);
 		if (addModules(message.getModules())) {
 			final PeerMessage newMessage = peerMessage.get();
 			for (final UUID uuid : connectionManager.getConnectedModules()) {
 				if (remoteUUID.equals(uuid)) {
 					continue;
 				}
 				connectionManager.sendMessage(uuid, newMessage);
 			}
 		}
 		LOGGER.exit(null);
 		return null;
 	}
 }
